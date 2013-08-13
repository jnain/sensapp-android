package org.sensapp.android.sensappdroid.websocket;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import org.sensapp.android.sensappdroid.activities.TabsActivity;
import org.sensapp.android.sensappdroid.contract.SensAppContract;
import org.sensapp.android.sensappdroid.datarequests.DatabaseRequest;
import org.sensapp.android.sensappdroid.json.JsonPrinter;
import org.sensapp.android.sensappdroid.json.MeasureJsonModel;
import org.sensapp.android.sensappdroid.json.NumericalMeasureJsonModel;
import org.sensapp.android.sensappdroid.json.StringMeasureJsonModel;
import org.sensapp.android.sensappdroid.models.Composite;
import org.sensapp.android.sensappdroid.models.Sensor;
import org.sensapp.android.sensappdroid.preferences.GeneralPrefFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Jonathan
 * Date: 24/07/13
 * Time: 12:19
 */
public class WsRequest{
    static private WsClient wsClient = TabsActivity.getClient();
    private static Map<String, String> messages = wsClient.getMessageList();
    final static int latencyAccepted = 800;         // ms  -- latency/sleepTime = Max Recursive Call Number (200 seems nice).
    final static int sleepTime = 4;                 // ms

    static public boolean isSensorRegistered(Sensor sensor){
        if(assureClientIsConnected()){
            String request = "getSensor("+sensor.getName()+")";
            wsClient.send(request);
            String rez = waitAndReturnResponse(request);
            return !rez.equals("none") && !rez.substring(0, rez.indexOf(" ")).equals("Unknown");
        }
        return false;
    }

    static public String postSensor(Sensor sensor){
        if(assureClientIsConnected()){
            String request = "registerSensor("+JsonPrinter.sensorToJson(sensor)+")";
            wsClient.send(request);
            return waitAndReturnResponse(request);
        }
        return "none";
    }

    static public String deleteSensor(Sensor sensor){
        if(assureClientIsConnected()){
            String request = "deleteSensor("+ sensor.getName() +")";
            wsClient.send(request);
            return waitAndReturnResponse(request);
        }
        return "none";
    }

    public static final int FLAG_DEFAULT = 0x79;
    public static final int FLAG_SILENT = 0x07;

    private static final String TAG = WsRequest.class.getSimpleName();
    private static final int INTEGER_SIZE = 4;
    private static final int LONG_SIZE = 12;
    private static final int DEFAULT_SIZE_LIMIT = 200000;
    private static final int NOTIFICATION_ID = 10;
    private static final int NOTIFICATION_FINAL_ID = 20;
    private static Context mContext;

    static public String putData(Context context, Uri uri){
        if(assureClientIsConnected()){
            int rowTotal = 0;
            MeasureJsonModel model = null;
            mContext = context;
            Cursor cursor = context.getContentResolver().query(uri, new String[]{SensAppContract.Measure.ID}, SensAppContract.Measure.UPLOADED + " = 0", null, null);
            if (cursor != null) {
                rowTotal = cursor.getCount();
                cursor.close();
            }

            int rowsUploaded = 0;
            int progress = 0;
            int sizeLimit = DEFAULT_SIZE_LIMIT;

            ArrayList<String> sensorNames = new ArrayList<String>();
            cursor = context.getContentResolver().query(uri, new String[]{"DISTINCT " + SensAppContract.Measure.SENSOR}, SensAppContract.Measure.UPLOADED + " = 0", null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    sensorNames.add(cursor.getString(cursor.getColumnIndexOrThrow(SensAppContract.Measure.SENSOR)));
                }
                cursor.close();
            }

            Sensor sensor;
            for (String sensorName : sensorNames) {

                if (!sensorExists(sensorName)) {
                    return null;
                }

                // Update uri with current preference
                try {
                    ContentValues values = new ContentValues();
                    values.put(SensAppContract.Sensor.URI, GeneralPrefFragment.buildUri(PreferenceManager.getDefaultSharedPreferences(context), context.getResources()));
                    context.getContentResolver().update(Uri.parse(SensAppContract.Sensor.CONTENT_URI + "/" + sensorName), values, null, null);
                } catch (IllegalStateException e) {
                    Log.e(TAG, e.getMessage());
                    return null;
                }

                sensor = DatabaseRequest.SensorRQ.getSensor(context, sensorName);

                if (!WsRequest.isSensorRegistered(sensor)) {
                    //Log.d("coucou", "pas la sensor");
                    postSensor(sensor);
                }

                if ("Numerical".equals(sensor.getTemplate())) {
                    model = new NumericalMeasureJsonModel(sensorName, sensor.getUnit());
                } else if ("String".equals(sensor.getTemplate())) {
                    model = new StringMeasureJsonModel(sensorName, sensor.getUnit());
                } else {
                    Log.e(TAG, "Incorrect sensor template");
                    return null;
                }

                List<Integer> ids = new ArrayList<Integer>();
                for (Long basetime : getBasetimes(sensorName)) {
                    model.setBt(basetime);
                    String[] projection = {SensAppContract.Measure.ID, SensAppContract.Measure.VALUE, SensAppContract.Measure.TIME};
                    String selection = SensAppContract.Measure.SENSOR + " = \"" + model.getBn() + "\" AND " + SensAppContract.Measure.BASETIME + " = " + model.getBt() + " AND " + SensAppContract.Measure.UPLOADED + " = 0";
                    cursor = context.getContentResolver().query(uri, projection, selection, null, null);
                    if (cursor != null) {
                        if (cursor.getCount() > 0) {
                            int size = 0;
                            while (size == 0) {
                                while (cursor.moveToNext()) {
                                    ids.add(cursor.getInt(cursor.getColumnIndexOrThrow(SensAppContract.Measure.ID)));
                                    long time = cursor.getLong(cursor.getColumnIndexOrThrow(SensAppContract.Measure.TIME));
                                    if (model instanceof NumericalMeasureJsonModel) {
                                        float value = cursor.getFloat(cursor.getColumnIndexOrThrow(SensAppContract.Measure.VALUE));
                                        ((NumericalMeasureJsonModel) model).appendMeasure(value, time);
                                        size += INTEGER_SIZE;
                                    } else if (model instanceof StringMeasureJsonModel) {
                                        String value = cursor.getString(cursor.getColumnIndexOrThrow(SensAppContract.Measure.VALUE));
                                        ((StringMeasureJsonModel) model).appendMeasure(value, time);
                                        size += value.length();
                                    }
                                    size += LONG_SIZE;
                                    if (size > sizeLimit && !cursor.isLast()) {
                                        size = 0;
                                        break;
                                    }
                                }

                                wsClient.send("registerData("+ JsonPrinter.measuresToJson(model) +")");
                                //waitAndReturnResponse("registerData("+ JsonPrinter.measuresToJson(model) +")");
                                model.clearValues();
                            }
                            ContentValues values = new ContentValues();
                            values.put(SensAppContract.Measure.UPLOADED, 1);
                            selection = SensAppContract.Measure.ID + " IN " + ids.toString().replace('[', '(').replace(']', ')');
                            rowsUploaded += context.getContentResolver().update(uri, values, selection, null);
                            progress += ids.size();
                            ids.clear();
                        }
                        cursor.close();
                    }
                }
            }
            return "true";
        }
        return "none";
    }

    static public boolean isCompositeRegistered(Context context, String compositeName){
        Composite composite = DatabaseRequest.CompositeRQ.getComposite(context, compositeName);
        if(assureClientIsConnected()){
            String request = "getComposite(" + composite.getName() + ")";
            wsClient.send(request);
            String rez = waitAndReturnResponse(request);
            return !rez.equals("none") && !rez.substring(0, rez.indexOf(" ")).equals("Unknown");
        }
        return false;
    }

    static public String postComposite(Context context, String compositeName){
        try {
            ContentValues values = new ContentValues();
            values.put(SensAppContract.Composite.URI, GeneralPrefFragment.buildUri(PreferenceManager.getDefaultSharedPreferences(context), context.getResources()));
            context.getContentResolver().update(Uri.parse(SensAppContract.Composite.CONTENT_URI + "/" + compositeName), values, null, null);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return null;
        }

        Composite composite = DatabaseRequest.CompositeRQ.getComposite(context, compositeName);
        if(assureClientIsConnected()){
            String request = "registerComposite(" + JsonPrinter.compositeToJson(composite) + ")";
            wsClient.send(request);
            return waitAndReturnResponse(request);
        }
        return "none";
    }

    static int times = 0;
    static public boolean assureClientIsConnected(){
        if(!wsClient.getConnected()){
            TabsActivity.resetClient();
            wsClient = TabsActivity.getClient();
            messages = wsClient.getMessageList();
            wsClient.connect();
        }
        while(!wsClient.getConnected()){
            if(times < latencyAccepted/sleepTime){
                times ++;
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else{
                times = 0;
                Log.e(WsRequest.class.getSimpleName(), "Unable to connect.");
                return false;
            }
        }
        return true;
    }

    static public String waitAndReturnResponse(String request){
        return getResponse(request);
    }

    static private String getResponse(String request){
        return getResponse(request, 0);
    }

    static private String getResponse(String request, int times){
        String response = messages.get(request);
        if(response!=null) {
            messages.remove(request);
            return response;
        }
        if(times != latencyAccepted/sleepTime){
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getResponse(request, times+1);
        }
        return "none";
    }

    static private boolean sensorExists(String sensorName) {
        String[] projection = {SensAppContract.Sensor.NAME};
        Cursor cursor = mContext.getContentResolver().query(Uri.parse(SensAppContract.Sensor.CONTENT_URI + "/" + sensorName), projection, null, null, null);
        if (cursor != null) {
            boolean exists =  cursor.getCount() > 0;
            cursor.close();
            return exists;
        }
        return false;
    }

    static private List<Long> getBasetimes(String sensorName) {
        List<Long> basetimes = new ArrayList<Long>();
        String[] projection = {"DISTINCT " + SensAppContract.Measure.BASETIME};
        Cursor cursor = mContext.getContentResolver().query(Uri.parse(SensAppContract.Measure.CONTENT_URI + "/" + sensorName), projection, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                basetimes.add(cursor.getLong(cursor.getColumnIndexOrThrow(SensAppContract.Measure.BASETIME)));
            }
            cursor.close();
        }
        return basetimes;
    }
}

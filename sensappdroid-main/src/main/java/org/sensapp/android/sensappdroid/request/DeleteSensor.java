package org.sensapp.android.sensappdroid.request;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import org.sensapp.android.sensappdroid.models.Sensor;
import org.sensapp.android.sensappdroid.preferences.GeneralPrefFragment;
import org.sensapp.android.sensappdroid.restrequests.DeleteSensorRestTask;
import org.sensapp.android.sensappdroid.websocket.WsRequest;

/**
 * Created with IntelliJ IDEA.
 * User: Jonathan
 * Date: 24/07/13
 * Time: 14:28
 */
public class DeleteSensor {
    public DeleteSensor(SharedPreferences sharedPreferences, Resources resources, Context context, Sensor sensor){
        if(GeneralPrefFragment.buildUri(sharedPreferences, resources).contains("ws://")){
            WsRequest.deleteSensor(sensor);
        }
        else{
            new DeleteSensorRestTask(context);
        }
    }
}

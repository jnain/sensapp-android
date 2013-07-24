package org.sensapp.android.sensappdroid.request;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import org.sensapp.android.sensappdroid.models.Sensor;
import org.sensapp.android.sensappdroid.preferences.GeneralPrefFragment;
import org.sensapp.android.sensappdroid.restrequests.PostSensorRestTask;
import org.sensapp.android.sensappdroid.websocket.WsRequest;

/**
 * Created with IntelliJ IDEA.
 * User: Jonathan
 * Date: 24/07/13
 * Time: 14:28
 */
public class PostSensor {
    public PostSensor(SharedPreferences sharedPreferences, Resources resources, Context context, Sensor sensor){
        if(GeneralPrefFragment.buildUri(sharedPreferences, resources).contains("ws://")){
            WsRequest.postSensor(sensor);
        }
        else{
            new PostSensorRestTask(context, sensor.getName());
        }
    }
}

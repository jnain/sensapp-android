package org.sensapp.android.sensappdroid.request;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import org.sensapp.android.sensappdroid.preferences.GeneralPrefFragment;
import org.sensapp.android.sensappdroid.restrequests.PutMeasuresTask;
import org.sensapp.android.sensappdroid.websocket.WsRequest;

/**
 * Created with IntelliJ IDEA.
 * User: Jonathan
 * Date: 24/07/13
 * Time: 14:28
 */
public class PutMeasures {
    public PutMeasures(SharedPreferences sharedPreferences, Resources resources, Context context, Uri data){
        if(GeneralPrefFragment.buildUri(sharedPreferences, resources).contains("ws://")){
            WsRequest.putData(context, data);
        }
        else{
            new PutMeasuresTask(context, data).execute();
        }
    }
}

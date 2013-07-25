package org.sensapp.android.sensappdroid.request;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import org.sensapp.android.sensappdroid.preferences.GeneralPrefFragment;
import org.sensapp.android.sensappdroid.restrequests.PostCompositeRestTask;
import org.sensapp.android.sensappdroid.websocket.WsRequest;

/**
 * Created with IntelliJ IDEA.
 * User: Jonathan
 * Date: 24/07/13
 * Time: 14:28
 */
public class PostComposite {
    public PostComposite(SharedPreferences sharedPreferences, Resources resources, Context context, String composite){
        if(GeneralPrefFragment.buildUri(sharedPreferences, resources).contains("ws://")){
            WsRequest.postComposite(context, composite);
        }
        else{
            new PostCompositeRestTask(context, composite).execute();
        }
    }
}

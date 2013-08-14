package org.sensapp.android.sensappdroid.request;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import org.sensapp.android.sensappdroid.preferences.AutoUploadSensorDialog;
import org.sensapp.android.sensappdroid.restrequests.PostCompositeRestTask;
import org.sensapp.android.sensappdroid.websocket.WsRequest;

import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Jonathan
 * Date: 24/07/13
 * Time: 14:28
 */
public class PostComposite {
    public PostComposite(SharedPreferences sharedPreferences, Resources resources, Context context, String composite){
        final Set<String> autoUploadWS = sharedPreferences.getStringSet(AutoUploadSensorDialog.WS_UPLOAD, new HashSet<String>());

        if(autoUploadWS.contains(composite)){
            WsRequest.postComposite(context, composite);
        }
        else{
            new PostCompositeRestTask(context, composite).execute();
        }
    }
}

package org.sensapp.android.sensappdroid.websocket;

import android.util.Log;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;
import org.sensapp.android.sensappdroid.activities.ServerGraphDisplayActivity;

import java.net.URI;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Jonathan
 * Date: 24/07/13
 * Time: 11:05
 */
public class WsClient extends WebSocketClient {
    private boolean connected = false;
    private Map<String, String> receivedMessages = new Hashtable<String, String>();

    public WsClient( URI serverUri , Draft draft ) {
        super( serverUri, draft );
    }

    @Override
    public void onOpen( ServerHandshake handshakedata ) {
        Log.d("coucou", "opened connection");
        connected = true;
        // if you pan to refuse connection based on ip or httpfields overload: onWebsocketHandshakeReceivedAsClient
    }

    @Override
    public void onMessage( String message ) {
        //Log.d("coucou", "received: " + message);
        // send( "you said: " + message );
        if(isDataNotification(message)){
            ServerGraphDisplayActivity.onDataReceived(message.substring(message.indexOf(" ")));
        }
        else
            receivedMessages.put(message.substring(0, message.indexOf(")")+1), message.substring(message.indexOf("), ")+"), ".length()));
        //Log.d("coucou", "messages: " + message.substring(0, message.indexOf(")")+1)+"\n"+message.substring(message.indexOf("), ")+"), ".length()));
    }

    @Override
    public void onClose( int code, String reason, boolean remote ) {
        // The codecodes are documented in class org.java_websocket.framing.CloseFrame
        Log.d("coucou", "Connection closed by " + (remote ? "remote peer" : "us"));
        connected = false;
        receivedMessages.clear();
    }

    @Override
    public void onError( Exception ex ) {
        ex.printStackTrace();
        // if the error is fatal then onClose will be called additionally
    }

    public boolean getConnected(){
        return connected;
    }

    public Map<String, String> getMessageList(){
        return receivedMessages;
    }

    private boolean isDataNotification(String m){
        return m.substring(0, m.indexOf(" ")).equals("DataNotification");
    }
}

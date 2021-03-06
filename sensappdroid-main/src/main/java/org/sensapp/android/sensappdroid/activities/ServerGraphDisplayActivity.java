/**
 * Copyright (C) 2012 SINTEF <fabien@fleurey.com>
 *
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sensapp.android.sensappdroid.activities;

import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.ListView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.sensapp.android.sensappdroid.R;
import org.sensapp.android.sensappdroid.graph.GraphAdapter;
import org.sensapp.android.sensappdroid.graph.GraphBaseView;
import org.sensapp.android.sensappdroid.graph.GraphBuffer;
import org.sensapp.android.sensappdroid.graph.GraphWrapper;
import org.sensapp.android.sensappdroid.json.*;
import org.sensapp.android.sensappdroid.websocket.WsRequest;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Jonathan
 * Date: 19/06/13
 * Time: 16:21
 * To change this template use File | Settings | File Templates.
 */
public class ServerGraphDisplayActivity extends FragmentActivity{

    private String elementName="COMPO";
    private String elementType="TYPE";
    static private GraphAdapter adapter;
    static private List<GraphWrapper> gwl = new ArrayList<GraphWrapper>();

    private Cursor cursorSensors;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graph_displayer);

        List<String> data = getIntent().getData().getPathSegments();
        elementName = data.get(data.size()-1);
        elementType = data.get(data.size()-2);

        displayGraphs();
    }

    private void refreshGraphData(){
        gwl.clear();

        if(WsRequest.assureClientIsConnected()){
            if(elementType.equals("Composites")){
                TabsActivity.getClient().send("getComposite("+elementName+")");
                String rez = WsRequest.waitAndReturnResponse("getComposite("+elementName+")");
                if(rez.equals("none")){
                    return;
                }
                Gson gson = new Gson();
                Type type = new TypeToken<CompositeJsonModel>(){}.getType();
                CompositeJsonModel composite = gson.fromJson(rez, type);
                List<String> sensorUris = composite.getSensors();

                for(String uri: sensorUris)
                    addGraphToWrapperList(gwl, Uri.parse(uri).getLastPathSegment());
            }
            else{
                TabsActivity.getClient().send("getSensor("+elementName+")");
                String rez = WsRequest.waitAndReturnResponse("getSensor("+elementName+")");
                if(rez.equals("none")){
                    return;
                }
                Gson gson = new Gson();
                Type type = new TypeToken<SensorJsonModel>(){}.getType();
                SensorJsonModel sensor = gson.fromJson(rez, type);

                addGraphToWrapperList(gwl, sensor.getId());
            }
        }

        adapter.notifyDataSetChanged();
    }

    private Integer displayGraphs(){

        adapter = new GraphAdapter(getApplicationContext(), gwl);

        ListView list = (ListView) findViewById(R.id.list_graph_displayer);
        list.setAdapter(adapter);
        refreshGraphData();
        return 1;
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    private void addGraphToWrapperList(List<GraphWrapper> gwl, String sensor){
        GraphBuffer buffer = new GraphBuffer();

        WsRequest.assureClientIsConnected();
        TabsActivity.getClient().send("getData("+sensor+", null, null, desc, 100, null, null, null)");
        String rez = WsRequest.waitAndReturnResponse("getData("+sensor+", null, null, desc, 100, null, null, null)");
        if(rez.equals("none"))
            return;
        Gson gson = new Gson();
        Type type = new TypeToken<NumericalMeasureJsonModel>(){}.getType();
        NumericalMeasureJsonModel measures = gson.fromJson(rez, type);

        for(int i=measures.getE().size()-1; i>=0; i--)
            buffer.insertData(measures.getE().get(i).getV());

        GraphWrapper wrapper = new GraphWrapper(buffer);
        wrapper.setGraphOptions(Color.BLUE, 500, GraphBaseView.LINECHART, sensor);
        wrapper.setPrinterParameters(true, false, true);

        gwl.add(wrapper);

        WsRequest.assureClientIsConnected();
        TabsActivity.getClient().send("getNotification("+sensor+")");
        if((rez = WsRequest.waitAndReturnResponse("getNotification("+sensor+")")).equals("none")){
            List<String> hooks = new ArrayList<String>();
            hooks.add("coucou");
            String notifString = JsonPrinter.subscriptionToJson(new SubscriptionJsonModel(sensor, hooks, "ws"));
            TabsActivity.getClient().send("registerNotification("+notifString+")");
            rez = WsRequest.waitAndReturnResponse("registerNotification("+notifString+")");
        }

        type = new TypeToken<SubscriptionJsonModel>(){}.getType();
        SubscriptionJsonModel subscription = gson.fromJson(rez, type);
        if(subscription.getProtocol() == null || subscription.getProtocol().equals("http")){
            subscription.setProtocol("ws");
            String notifString = JsonPrinter.subscriptionToJson(subscription);
            TabsActivity.getClient().send("updateNotification("+notifString+")");
            rez = WsRequest.waitAndReturnResponse("updateNotification("+notifString+")");
            subscription = gson.fromJson(rez, type);
        }

        TabsActivity.getClient().send("getNotified("+subscription.getId()+")");
    }

    static public void onDataReceived(String data){
        if(gwl == null)
            return;
        Gson gson = new Gson();
        Type type = new TypeToken<NumericalMeasureJsonModel>(){}.getType();
        NumericalMeasureJsonModel measures = gson.fromJson(data, type);

        GraphBuffer buffer = getBufferByName(measures.getBn());
        for(NumericalValueJsonModel value: measures.getE()){
            buffer.insertData(value.getV());
        }
        adapter.notifyDataSetChanged();
    }

    static private GraphBuffer getBufferByName(String name){
        for(GraphWrapper gw: gwl){
            if(gw.getName().equals(name))
                return gw.getBuffer();
        }
        return null;
    }
}
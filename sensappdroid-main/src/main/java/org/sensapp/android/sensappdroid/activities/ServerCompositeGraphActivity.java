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
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ListView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.sensapp.android.sensappdroid.R;
import org.sensapp.android.sensappdroid.graph.GraphAdapter;
import org.sensapp.android.sensappdroid.graph.GraphBaseView;
import org.sensapp.android.sensappdroid.graph.GraphBuffer;
import org.sensapp.android.sensappdroid.graph.GraphWrapper;
import org.sensapp.android.sensappdroid.json.CompositeJsonModel;
import org.sensapp.android.sensappdroid.json.NumericalMeasureJsonModel;
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
public class ServerCompositeGraphActivity extends FragmentActivity{

    private String compositeName="COMPO";
    private long graphID=0;
    private GraphAdapter adapter;
    private List<GraphWrapper> gwl = new ArrayList<GraphWrapper>();
    private Cursor cursorSensors;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graph_displayer);

        List<String> data = getIntent().getData().getPathSegments();
        compositeName = data.get(data.size()-1);
        //graphID = Long.parseLong(data.get(data.size() - 2));

        //setTitle(graphName);

        //ListView list = (ListView) findViewById(R.id.list_graph_displayer);

        displayGraphs();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.composite_menu, menu);
        return true;
    }

    private void refreshGraphData(){
        gwl.clear();

        WsRequest.assureClientIsConnected();
        TabsActivity.getClient().send("getComposite("+compositeName+")");
        String rez = WsRequest.waitAndReturnResponse("getComposite("+compositeName+")");
        Gson gson = new Gson();
        Type type = new TypeToken<CompositeJsonModel>(){}.getType();
        CompositeJsonModel composite = gson.fromJson(rez, type);
        List<String> sensorUris = composite.getSensors();

        for(String uri: sensorUris)
            addGraphToWrapperList(gwl, Uri.parse(uri).getLastPathSegment());

        adapter.notifyDataSetChanged();
    }

    private Integer displayGraphs(){
        //cursorSensors = getContentResolver().query(Uri.parse(SensAppContract.GraphSensor.CONTENT_URI + "/graph/" + graphID), null, null, null, null);
        adapter = new GraphAdapter(getApplicationContext(), gwl);

        ListView list = (ListView) findViewById(R.id.list_graph_displayer);
        list.setAdapter(adapter);
        refreshGraphData();
        //cursorSensors.close();
        return 1;
    }

    @Override
    public void onResume(){
        super.onResume();
        //displayGraphs();
    }

    private void addGraphToWrapperList(List<GraphWrapper> gwl, String sensor){
        GraphBuffer buffer = new GraphBuffer();

        WsRequest.assureClientIsConnected();
        TabsActivity.getClient().send("getData("+sensor+", null, null, desc, 100, null, null, null)");
        String rez = WsRequest.waitAndReturnResponse("getData("+sensor+", null, null, desc, 100, null, null, null)");
        Gson gson = new Gson();
        Type type = new TypeToken<NumericalMeasureJsonModel>(){}.getType();
        NumericalMeasureJsonModel measures = gson.fromJson(rez, type);

        for(int i=measures.getE().size()-1; i>=0; i--)
            buffer.insertData(measures.getE().get(i).getV());

        GraphWrapper wrapper = new GraphWrapper(buffer);
        wrapper.setGraphOptions(Color.BLUE, 500, GraphBaseView.LINECHART, sensor);
        wrapper.setPrinterParameters(true, false, true);

        gwl.add(wrapper);
    }
}
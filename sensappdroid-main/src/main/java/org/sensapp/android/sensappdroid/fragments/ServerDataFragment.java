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
package org.sensapp.android.sensappdroid.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.sensapp.android.sensappdroid.R;
import org.sensapp.android.sensappdroid.activities.SensAppService;
import org.sensapp.android.sensappdroid.activities.ServerGraphDisplayActivity;
import org.sensapp.android.sensappdroid.activities.TabsActivity;
import org.sensapp.android.sensappdroid.contract.SensAppContract;
import org.sensapp.android.sensappdroid.json.CompositeJsonModel;
import org.sensapp.android.sensappdroid.json.SensorJsonModel;
import org.sensapp.android.sensappdroid.preferences.GeneralPrefFragment;
import org.sensapp.android.sensappdroid.preferences.PreferencesActivity;
import org.sensapp.android.sensappdroid.websocket.WsRequest;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ServerDataFragment extends ListFragment{

	private static final int MENU_DELETE_ID = Menu.FIRST + 1;
	private static final int MENU_MANAGE_ID = Menu.FIRST + 2;

    private ArrayAdapter<String> adapter;
	private OnGraphSelectedListener graphSelectedListener;
    private String DISPLAY = "Composites";
    private boolean MENU_CREATED = false;

    public interface OnGraphSelectedListener {
		public void onGraphSelected(Uri uri);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.server_data_list, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        //Cursor cursor = getActivity().getContentResolver().query(SensAppContract.Graph.CONTENT_URI, null, null, null, null);
        //adapter = new SimpleCursorAdapter(getActivity(), R.layout.graph_simple_row, cursor, new String[]{SensAppContract.Graph.TITLE}, new int[]{R.id.graph_name});

        if(GeneralPrefFragment.buildUri(PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()), getResources())
            .contains("ws://")) {
            displayComposites();
        }
	}

    private void displayComposites(){
        DISPLAY = "Composites";
        TextView title = (TextView)getActivity().findViewById(R.id.server_data_title);
        title.setText(DISPLAY + " From the Server");
        getActivity().invalidateOptionsMenu();
        if(WsRequest.assureClientIsConnected()){
            TabsActivity.getClient().send("getComposites()");
            String response = WsRequest.waitAndReturnResponse("getComposites()");
            Gson gson = new Gson();
            Type collectionType = new TypeToken<List<CompositeJsonModel>>(){}.getType();
            List<CompositeJsonModel> composites = gson.fromJson(response, collectionType);
            List<String> compositeNames = new ArrayList<String>();
            for(CompositeJsonModel c : composites)
                compositeNames.add(c.getId());
            adapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), R.layout.server_data_composite, compositeNames);
            setListAdapter(adapter);
            registerForContextMenu(getListView());
        }
    }

    private void displaySensors(){
        DISPLAY = "Sensors";
        TextView title = (TextView)getActivity().findViewById(R.id.server_data_title);
        title.setText(DISPLAY + " From the Server");
        getActivity().invalidateOptionsMenu();
        if(WsRequest.assureClientIsConnected()){
            TabsActivity.getClient().send("getSensors()");
            String response = WsRequest.waitAndReturnResponse("getSensors()");
            Gson gson = new Gson();
            Type collectionType = new TypeToken<List<SensorJsonModel>>(){}.getType();
            List<SensorJsonModel> sensors = gson.fromJson(response, collectionType);
            List<String> sensorNames = new ArrayList<String>();
            for(SensorJsonModel s : sensors)
                sensorNames.add(s.getId());
            adapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), R.layout.server_data_composite, sensorNames);
            setListAdapter(adapter);
            registerForContextMenu(getListView());
        }
    }

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if(DISPLAY.equals("Composites"))
		    inflater.inflate(R.menu.server_data_menu_composites, menu);
        else
            inflater.inflate(R.menu.server_data_menu_sensors, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i = new Intent(getActivity(), SensAppService.class);
		Uri uri = getActivity().getIntent().getData();
		if (uri == null) {
			uri = SensAppContract.Measure.CONTENT_URI;
		}
		i.setData(uri);
		switch (item.getItemId()) {
		case R.id.server_data_switch:
            if(DISPLAY.equals("Composites"))
                displaySensors();
            else
                displayComposites();
            //NewGraphDialogFragment.newInstance().show(getFragmentManager(), "NewGraphDialog");
            return true;
		case R.id.preferences:
			startActivity(new Intent(getActivity(), PreferencesActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, MENU_DELETE_ID, 0, "Delete graph");
		menu.add(0, MENU_MANAGE_ID, 0, "Manage graph sensors");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        //String title = getGraphTitleByID(info.id);
		switch (item.getItemId()) {
		case MENU_DELETE_ID:
            //Delete the graph
            //new DeleteGraphSensorsTask(getActivity(), SensAppContract.Graph.CONTENT_URI).execute(SensAppContract.Graph.ID + " = " + info.id);
            //Delete Graph Sensors attached
            //new DeleteGraphSensorsTask(getActivity(), SensAppContract.GraphSensor.CONTENT_URI).execute(SensAppContract.GraphSensor.GRAPH + " = " + info.id);
            return true;
		case MENU_MANAGE_ID:
            //ManageGraphSensorFragment.newInstance(title, info.id).show(getFragmentManager(), "ManageGraphDialog");
            return true;
		}
		return super.onContextItemSelected(item);
	}

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        String elementName = l.getItemAtPosition(position).toString();
        Intent i = new Intent(getActivity().getApplicationContext(), ServerGraphDisplayActivity.class);
        i.setData(Uri.parse(DISPLAY +"/"+ elementName));
        startActivity(i);
    }
}

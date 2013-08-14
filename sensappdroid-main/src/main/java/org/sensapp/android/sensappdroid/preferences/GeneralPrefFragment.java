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
package org.sensapp.android.sensappdroid.preferences;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import org.sensapp.android.sensappdroid.R;
import org.sensapp.android.sensappdroid.activities.TabsActivity;

public class GeneralPrefFragment extends PreferenceFragment {
	
	private SharedPreferences preferences;
	private EditTextServerPreference serverHttp;
	private EditTextPreference portHttp;
    private EditTextServerPreference serverWs;
    private EditTextPreference portWs;
	
	private SharedPreferences.OnSharedPreferenceChangeListener spChanged = new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {	
			if (key.equals(serverHttp.getKey())) {
				serverHttp.setSummary(sharedPreferences.getString(serverHttp.getKey(), ""));
			} else if (key.equals(portHttp.getKey())) {
				portHttp.setSummary(sharedPreferences.getString(portHttp.getKey(), ""));
			} else if (key.equals(serverWs.getKey())) {
                serverWs.setSummary(sharedPreferences.getString(serverWs.getKey(), ""));
            } else if (key.equals(portWs.getKey())) {
                portWs.setSummary(sharedPreferences.getString(portWs.getKey(), ""));
            }
            String url = sharedPreferences.getString(serverWs.getKey(), "");
            if(url.contains("ws://")){
                if(TabsActivity.getClient().getConnected())
                    TabsActivity.getClient().close();
                TabsActivity.resetClient();
                TabsActivity.getClient().connect();
            } else if (TabsActivity.getClient().getConnected())
                TabsActivity.getClient().close();
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		addPreferencesFromResource(R.xml.pref_general_fragment);
		serverHttp = (EditTextServerPreference) findPreference(getString(R.string.pref_default_server_key));
		portHttp = (EditTextPreference) findPreference(getString(R.string.pref_default_port_key));
        serverWs = (EditTextServerPreference) findPreference(getString(R.string.pref_ws_server_key));
        portWs = (EditTextPreference) findPreference(getString(R.string.pref_ws_port_key));
	}
	
	public static String buildUri(SharedPreferences preferences, Resources resources) throws IllegalStateException {
		String server = preferences.getString(resources.getString(R.string.pref_default_server_key), resources.getString(R.string.pref_server_default_value));
		String port = preferences.getString(resources.getString(R.string.pref_default_port_key), "80");
		if (server.isEmpty() || port.isEmpty()) {
			throw new IllegalStateException("Unable to read uri");
		}
		return server + ":" + port;
	}

    public static String buildWsUri(SharedPreferences preferences, Resources resources) throws IllegalStateException {
        String server = preferences.getString(resources.getString(R.string.pref_ws_server_key), resources.getString(R.string.pref_server_ws_value));
        String port = preferences.getString(resources.getString(R.string.pref_ws_port_key), "9000");
        if (server.isEmpty() || port.isEmpty()) {
            throw new IllegalStateException("Unable to read uri");
        }
        return server + ":" + port;
    }

	@Override
	public void onResume() {
		super.onResume();
		preferences.registerOnSharedPreferenceChangeListener(spChanged);
		serverHttp.setSummary(preferences.getString(serverHttp.getKey(), ""));
		portHttp.setSummary(preferences.getString(portHttp.getKey(), ""));
        serverWs.setSummary(preferences.getString(serverWs.getKey(), ""));
        portWs.setSummary(preferences.getString(portWs.getKey(), ""));
	}
	
	@Override
	public void onPause() {
		super.onPause();
		preferences.unregisterOnSharedPreferenceChangeListener(spChanged);
	}
}
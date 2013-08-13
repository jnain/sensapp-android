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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.sensapp.android.sensappdroid.R;
import org.sensapp.android.sensappdroid.contract.SensAppContract;

import java.util.HashSet;
import java.util.Set;

public class AutoUploadSensorDialog extends DialogFragment {

	public static final String HTTP_UPLOAD = "sensor_http_key";
    public static final String WS_UPLOAD = "sensor_ws_key";

    class Sensor{
        private TextView textView;
        private CheckBox httpBox;
        private CheckBox wsBox;
        private CheckBox noneBox;
        private View myView;

        public Sensor(Context context, String name){
            myView = View.inflate(context, R.layout.upload_composite, null);
            textView = (TextView)myView.findViewById(R.id.name);
            textView.setText(name);
            httpBox = (CheckBox)myView.findViewById(R.id.http);
            wsBox = (CheckBox)myView.findViewById(R.id.ws);
            noneBox = (CheckBox)myView.findViewById(R.id.none);
        }

        public View getMyView(){ return myView; }
        public CheckBox getHttpBox(){ return httpBox; }
        public CheckBox getWsBox(){ return wsBox; }
        public CheckBox getNoneBox(){ return noneBox; }
        public TextView getTextView(){ return textView; }
    }

    class Composite extends Sensor{
        private LinearLayout myLayout;

        public Composite(Context context, String name){
            super(context, name);
            myLayout = new LinearLayout(context);
            myLayout.setOrientation(LinearLayout.VERTICAL);
            myLayout.addView(this.getMyView());
            cursor = context.getContentResolver()
                    .query(Uri.parse(SensAppContract.Sensor.CONTENT_URI + "/composite/" + name), null, null, null, null);
            for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
                myLayout.addView(new Sensor(context,
                        cursor.getString(cursor.getColumnIndexOrThrow(SensAppContract.Composite.NAME))).getMyView());
            }
        }

        public LinearLayout getMyLayout(){ return myLayout; }
    }

	
	private Cursor cursor;
	
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		cursor = getActivity().getContentResolver().query(SensAppContract.Composite.CONTENT_URI, null, null, null, null);
		String[] names = new String[cursor.getCount()];
		/*boolean[] http = new boolean[cursor.getCount()];
        boolean[] ws = new boolean[cursor.getCount()];*/
		final Set<String> autoUploadHTTP = PreferenceManager.getDefaultSharedPreferences(getActivity()).getStringSet(HTTP_UPLOAD, new HashSet<String>());
        final Set<String> autoUploadWS = PreferenceManager.getDefaultSharedPreferences(getActivity()).getStringSet(WS_UPLOAD, new HashSet<String>());
		LinearLayout vList = new LinearLayout(this.getActivity());
        for (int i = 0 ; cursor.moveToNext() ; i ++) {
			/*names[i] =*/
            vList.addView(new Composite(this.getActivity(),
                    cursor.getString(cursor.getColumnIndexOrThrow(SensAppContract.Composite.NAME))).getMyLayout());
			/*http[i] = autoUploadHTTP.contains(names[i]);
            ws[i] = autoUploadWS.contains(names[i]);*/
		}
        //final ArrayAdapter<String> namesAdapter = new ArrayAdapter<String>(this.getActivity(), R.layout.upload_composite, R.id.name, names);
		return new AlertDialog.Builder(getActivity())
		.setTitle("Select sensors to auto upload")
        .setView(vList)
        /*.setAdapter(namesAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                cursor.moveToPosition(which);
                String sensorName = cursor.getString(cursor.getColumnIndexOrThrow(SensAppContract.Sensor.NAME));
                if (isChecked) {
                    statusSaved.add(sensorName);
                } else {
                    statusSaved.remove(sensorName);
                }
                Log.d("coucou", "hehehehe");
            }
        })   */
		/*.setMultiChoiceItems(names, status, new DialogInterface.OnMultiChoiceClickListener() {
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				cursor.moveToPosition(which);
				String sensorName = cursor.getString(cursor.getColumnIndexOrThrow(SensAppContract.Sensor.NAME));
				if (isChecked) {
					statusSaved.add(sensorName);
				} else {
					statusSaved.remove(sensorName);
				}
			}
		})*/

		.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putStringSet(HTTP_UPLOAD, autoUploadHTTP).commit();
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putStringSet(WS_UPLOAD, autoUploadWS).commit();
                cursor.close();
            }
        }).create();
    }

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		if (cursor != null) {
			cursor.close();
		}
	}    
}

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
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import org.sensapp.android.sensappdroid.R;
import org.sensapp.android.sensappdroid.contract.SensAppContract;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AutoUploadSensorDialog extends DialogFragment {

	public static final String HTTP_UPLOAD = "sensor_http_key";
    public static final String WS_UPLOAD = "sensor_ws_key";

    private List<Sensor> dialogItems = new ArrayList<Sensor>();

    class Sensor{
        private TextView textView;
        private RadioButton httpBox;
        private RadioButton wsBox;
        private RadioButton noneBox;
        private View myView;

        public Sensor(Context context, String name, boolean http, boolean ws){
            myView = View.inflate(context, R.layout.upload_composite, null);
            textView = (TextView)myView.findViewById(R.id.name);
            textView.setText(name);
            httpBox = (RadioButton)myView.findViewById(R.id.http);
            wsBox = (RadioButton)myView.findViewById(R.id.ws);
            noneBox = (RadioButton)myView.findViewById(R.id.none);
            if(http)
                httpBox.setChecked(true);
            else if(ws)
                wsBox.setChecked(true);
            else
                noneBox.setChecked(true);
            dialogItems.add(this);
        }

        public View getMyView(){ return myView; }
        public RadioButton getHttpBox(){ return httpBox; }
        public RadioButton getWsBox(){ return wsBox; }
        public RadioButton getNoneBox(){ return noneBox; }
        public TextView getTextView(){ return textView; }
    }

    class Composite extends Sensor{
        private LinearLayout myLayout;

        public Composite(Context context, String name, Set<String> http, Set<String> ws){
            super(context, name, http.contains(name), ws.contains(name));
            Cursor c = context.getContentResolver()
                    .query(Uri.parse(SensAppContract.Sensor.CONTENT_URI + "/composite/" + name), null, null, null, null);
            myLayout = new LinearLayout(context);
            myLayout.setOrientation(LinearLayout.VERTICAL);
            myLayout.addView(this.getMyView());
            for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
                String sensorName = c.getString(c.getColumnIndexOrThrow(SensAppContract.Composite.NAME));
                View v = new Sensor(context, sensorName, http.contains(sensorName), ws.contains(sensorName)).getMyView();
                v.setPadding(10, 0, 0, 0);
                v.setBackgroundColor(Color.parseColor("#273c4b"));
                myLayout.addView(v);
            }
        }

        public LinearLayout getMyLayout(){ return myLayout; }
    }

	
	private Cursor cursor;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		cursor = getActivity().getContentResolver().query(SensAppContract.Composite.CONTENT_URI, null, null, null, null);
		final Set<String> autoUploadHTTP = PreferenceManager.getDefaultSharedPreferences(getActivity()).getStringSet(HTTP_UPLOAD, new HashSet<String>());
        final Set<String> autoUploadWS = PreferenceManager.getDefaultSharedPreferences(getActivity()).getStringSet(WS_UPLOAD, new HashSet<String>());

        LinearLayout vList = new LinearLayout(this.getActivity());
        vList.setOrientation(LinearLayout.VERTICAL);
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(SensAppContract.Composite.NAME));
            vList.addView(new Composite(this.getActivity(), name, autoUploadHTTP, autoUploadWS).getMyLayout());
		}

		return new AlertDialog.Builder(getActivity())
		.setTitle("Select sensors to auto upload")
        .setView(vList)
		.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                autoUploadHTTP.clear();
                autoUploadWS.clear();
                for (Sensor s : dialogItems) {
                    if (s.getHttpBox().isChecked())
                        autoUploadHTTP.add(s.getTextView().getText().toString());
                    else if (s.getWsBox().isChecked())
                        autoUploadWS.add(s.getTextView().getText().toString());
                }

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

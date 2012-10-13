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

import java.util.ArrayList;
import java.util.Set;

import org.sensapp.android.sensappdroid.connectivity.ConnectivityReceiver;
import org.sensapp.android.sensappdroid.contract.SensAppContract;
import org.sensapp.android.sensappdroid.datarequests.DeleteMeasuresTask;
import org.sensapp.android.sensappdroid.preferences.AutoUploadSensorDialog;
import org.sensapp.android.sensappdroid.restrequests.PutMeasuresTask;
import org.sensapp.android.sensappdroid.restrequests.PutMeasuresTask.PutMeasureCallback;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class SensAppService extends Service implements PutMeasureCallback {
	
	public static final String ACTION_UPLOAD = SensAppService.class.getName() + ".ACTION_UPLOAD";
	public static final String ACTION_AUTO_UPLOAD = SensAppService.class.getName() + ".ACTION_AUTO_UPLOAD";
	public static final String ACTION_DELETE_LOCAL = SensAppService.class.getName() + ".ACTION_DELETE_LOCAL";
	public static final String EXTRA_UPLOADED_FILTER = SensAppService.class.getName() + ".EXTRA_UPLOADED_FILTER";
	
	private static final String TAG = SensAppService.class.getSimpleName();
	
	private boolean waitForData = false;
	
	private int lastTaskStarted = 0;
	private int lastConsecutiveTaskEnded = 0;
	private ArrayList<Integer> taskBuffer = new ArrayList<Integer>();
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent.getAction() != null) {
			if (intent.getAction().equals(ACTION_UPLOAD)) {
				Log.d(TAG, "Receive: ACTION_UPLOAD");
				uploadMeasureUri(intent.getData());
			} else if (intent.getAction().equals(ACTION_AUTO_UPLOAD)) {
				Log.d(TAG, "Receive: ACTION_AUTO_UPLOAD");
				autoUpload();
			} else if (intent.getAction().equals(ACTION_DELETE_LOCAL)) {
				Log.d(TAG, "Receive: ACTION_DELETE_LOCAL");
				Bundle extra = intent.getExtras();
				if (extra == null) {
					new DeleteMeasuresTask(this, intent.getData()).execute();
				} else if (extra.getBoolean(EXTRA_UPLOADED_FILTER)) {
					new DeleteMeasuresTask(this, intent.getData()).execute(SensAppContract.Measure.UPLOADED + " = 1");
				}
			} else if (intent.getAction().equals(ConnectivityReceiver.ACTION_CONNECTIVITY_FOUND)) {
				Log.d(TAG, "Receive: ACTION_CONNECTIVITY_FOUND");
				if (waitForData) {
					autoUpload();
				} else {
					stopSelf();
				}
			}
		} else {
			stopSelf();
		}
		return START_NOT_STICKY;
	}
	
	private void uploadMeasureUri(Uri uri) {
		if (!ConnectivityReceiver.isDataAvailable(getApplicationContext())) {
			Log.e(TAG, "No data connection available");
			Toast.makeText(this, "No data connection available", Toast.LENGTH_LONG).show();
		} else {
			new PutMeasuresTask(this, taskIdGen(), getApplicationContext(), uri).execute();
		}
	}
	
	private void autoUpload() {
		if (!ConnectivityReceiver.isDataAvailable(getApplicationContext())) {
			Log.e(TAG, "No data connection available");
			waitForData = true;
		} else {
			waitForData = false;
			Set<String> names = PreferenceManager.getDefaultSharedPreferences(this).getStringSet(AutoUploadSensorDialog.SENSOR_MAINTAINED, null);
			if (names != null && !names.isEmpty()) {
				for (final String name : names) {
					Log.d(TAG, "Put " + name + " sensor (Auto upload)");
					new PutMeasuresTask(this, taskIdGen(), getApplicationContext(), Uri.parse(SensAppContract.Measure.CONTENT_URI + "/" + name), PutMeasuresTask.FLAG_SILENT).execute();
				}
			}
		}
	}
	
	private int taskIdGen() {
		return ++ lastTaskStarted;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onTaskFinished(int id) {
		taskBuffer.add(id);
		while (taskBuffer.contains(lastConsecutiveTaskEnded + 1)) {
			lastConsecutiveTaskEnded ++;
			taskBuffer.remove((Integer) lastConsecutiveTaskEnded);
		}
		if (lastTaskStarted == lastConsecutiveTaskEnded) {
			stopSelf();
		}
	}
}

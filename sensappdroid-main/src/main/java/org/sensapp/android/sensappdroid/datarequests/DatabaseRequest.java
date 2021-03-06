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
package org.sensapp.android.sensappdroid.datarequests;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import org.sensapp.android.sensappdroid.contract.SensAppContract;
import org.sensapp.android.sensappdroid.models.Composite;
import org.sensapp.android.sensappdroid.models.Sensor;
import org.sensapp.android.sensappdroid.restrequests.RestRequest;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

public class DatabaseRequest {
	
	private static final Uri S_CONTENT_URI = SensAppContract.Sensor.CONTENT_URI;
	private static final String S_NAME = SensAppContract.Sensor.NAME;
	private static final String S_URI = SensAppContract.Sensor.URI;
	private static final String S_DESCRIPTION = SensAppContract.Sensor.DESCRIPTION;
	private static final String S_BACKEND = SensAppContract.Sensor.BACKEND;
	private static final String S_TEMPLATE = SensAppContract.Sensor.TEMPLATE;
	private static final String S_UNIT = SensAppContract.Sensor.UNIT;
	private static final String S_UPLOADED = SensAppContract.Sensor.UPLOADED;
	
	private static final Uri M_CONTENT_URI = SensAppContract.Measure.CONTENT_URI;
	private static final String M_ID = SensAppContract.Measure.ID;
	private static final String M_SENSOR = SensAppContract.Measure.SENSOR;
	private static final String M_VALUE = SensAppContract.Measure.VALUE;
	private static final String M_TIME = SensAppContract.Measure.TIME;
	private static final String M_BASETIME = SensAppContract.Measure.BASETIME;
	private static final String M_UPLOADED = SensAppContract.Measure.UPLOADED;
	
	private static final Uri C_CONTENT_URI = SensAppContract.Compose.CONTENT_URI;
	private static final String C_COMPOSITE = SensAppContract.Compose.COMPOSITE;
	
	private static final Uri CTE_CONTENT_URI = SensAppContract.Composite.CONTENT_URI;

    private static final Uri GS_CONTENT_URI = SensAppContract.GraphSensor.CONTENT_URI;
    private static final String GS_ID = SensAppContract.GraphSensor.ID;
    private static final String GS_TITLE = SensAppContract.GraphSensor.TITLE;
    private static final String GS_GRAPH = SensAppContract.GraphSensor.GRAPH;
    private static final String GS_STYLE = SensAppContract.GraphSensor.STYLE;
    private static final String GS_COLOR = SensAppContract.GraphSensor.COLOR;
    private static final String GS_MAX = SensAppContract.GraphSensor.MAX;
    private static final String GS_MIN = SensAppContract.GraphSensor.MIN;
    private static final String GS_SENSOR = SensAppContract.GraphSensor.SENSOR;

    private static final Uri G_CONTENT_URI = SensAppContract.Graph.CONTENT_URI;
    private static final String G_ID = SensAppContract.Graph.ID;
    private static final String G_TITLE = SensAppContract.Graph.TITLE;
	
	private static final String TAG = DatabaseRequest.class.getSimpleName();
	
	public static class MeasureRQ {
		
		public static int deleteMeasure(Context context, int id) {
			return context.getContentResolver().delete(Uri.parse(M_CONTENT_URI + "/" + id), null, null);
		}

		public static int deleteMeasures(Context context, String selection) {
			return context.getContentResolver().delete(M_CONTENT_URI, selection, null);
		}

		public static int updateMeasure(Context context, int id, ContentValues values) {
			return context.getContentResolver().update(Uri.parse(M_CONTENT_URI + "/" + id), values, null, null);
		}
		
		public static int updateMeasures(Context context, String selection, ContentValues values) {
			return context.getContentResolver().update(M_CONTENT_URI, values, selection, null);
		}

		public static Map<Integer, ContentValues> getMeasuresValues(Context context, String selection) {
			Map<Integer, ContentValues> measures = new Hashtable<Integer, ContentValues>();
			Cursor cursor = context.getContentResolver().query(M_CONTENT_URI, null, selection, null, null);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					ContentValues cv = new ContentValues();
					cv.put(M_ID, cursor.getInt(cursor.getColumnIndexOrThrow(M_ID)));
					cv.put(M_SENSOR, cursor.getString(cursor.getColumnIndexOrThrow(M_SENSOR)));
					cv.put(M_VALUE, cursor.getInt(cursor.getColumnIndexOrThrow(M_VALUE)));
					cv.put(M_TIME, cursor.getLong(cursor.getColumnIndexOrThrow(M_TIME)));
					cv.put(M_BASETIME, cursor.getLong(cursor.getColumnIndexOrThrow(M_BASETIME)));
					cv.put(M_UPLOADED, cursor.getInt(cursor.getColumnIndexOrThrow(M_UPLOADED)));
					measures.put((Integer) cv.get(M_ID), cv);
				}
				cursor.close();
			}
			return measures;
		}
	}
	
	public static class SensorRQ {
		
		public static int deleteSensor(Context context, String name) {
			int rowMeasures = MeasureRQ.deleteMeasures(context, M_SENSOR + " = \"" + name + "\"");
			int rowSensors = context.getContentResolver().delete(Uri.parse(S_CONTENT_URI + "/" + name), null, null);
			return rowMeasures + rowSensors;
		}

		public static int updateSensor(Context context, String name, ContentValues values) {
			return context.getContentResolver().update(Uri.parse(S_CONTENT_URI + "/" + name), values, null, null);
		}
		
		public static int updateSensors(Context context, String selection, ContentValues values) {
			return context.getContentResolver().update(S_CONTENT_URI, values, selection, null);
		}

		public static Map<String, ContentValues> getSensorsValues(Context context, String selection) {
			Map<String, ContentValues> sensors = new Hashtable<String, ContentValues>();
			Cursor cursor = context.getContentResolver().query(S_CONTENT_URI, null, selection, null, null);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					ContentValues cv = new ContentValues();
					cv.put(S_NAME, cursor.getString(cursor.getColumnIndexOrThrow(S_NAME)));
					cv.put(S_URI, cursor.getString(cursor.getColumnIndexOrThrow(S_URI)));
					cv.put(S_DESCRIPTION, cursor.getString(cursor.getColumnIndexOrThrow(S_DESCRIPTION)));
					cv.put(S_BACKEND, cursor.getString(cursor.getColumnIndexOrThrow(S_BACKEND)));
					cv.put(S_TEMPLATE, cursor.getString(cursor.getColumnIndexOrThrow(S_TEMPLATE)));
					cv.put(S_UNIT, cursor.getString(cursor.getColumnIndexOrThrow(S_UNIT)));
					cv.put(S_UPLOADED, cursor.getInt(cursor.getColumnIndexOrThrow(S_UPLOADED)));
					sensors.put((String) cv.get(S_NAME), cv);
				}
				cursor.close();
			}
			return sensors;
		}
		
		public static Sensor getSensor(Context context, String name) {
			String[] projection = {SensAppContract.Sensor.URI, SensAppContract.Sensor.DESCRIPTION, SensAppContract.Sensor.BACKEND, SensAppContract.Sensor.TEMPLATE, org.sensapp.android.sensappdroid.contract.SensAppContract.Sensor.UNIT, SensAppContract.Sensor.UPLOADED};
			Cursor cursor = context.getContentResolver().query(Uri.parse(SensAppContract.Sensor.CONTENT_URI + "/" + name), projection, null, null, null);
			Sensor sensor = null;
			if (cursor != null) {
				if (cursor.getCount() > 0) {
					cursor.moveToFirst();
					String uriString = cursor.getString(cursor.getColumnIndexOrThrow(SensAppContract.Sensor.URI));
					Uri uri = null;
					if (uriString != null) {
						uri = Uri.parse(uriString);
					}
					String description = cursor.getString(cursor.getColumnIndexOrThrow(SensAppContract.Sensor.DESCRIPTION));
					String backend = cursor.getString(cursor.getColumnIndexOrThrow(SensAppContract.Sensor.BACKEND));
					String template = cursor.getString(cursor.getColumnIndexOrThrow(SensAppContract.Sensor.TEMPLATE));
					String unit = cursor.getString(cursor.getColumnIndexOrThrow(SensAppContract.Sensor.UNIT));
					int uploaded = cursor.getInt(cursor.getColumnIndexOrThrow(SensAppContract.Sensor.UPLOADED));
					sensor = new Sensor(name, uri, description, backend, template, unit, uploaded == 1);
				}
				cursor.close();
			}
			return sensor;
		}
	}
	
	public static class ComposeRQ {
		public static int deleteCompose(Context context, String selection) {
			return context.getContentResolver().delete(C_CONTENT_URI, selection, null);
		}
	}
	
	public static class CompositeRQ {

		public static Composite getComposite(Context context, String name) {
			String description = null;
			Uri uri = null;
			String[] projection = {SensAppContract.Composite.DESCRIPTION, SensAppContract.Composite.URI};
			// Get composite description and uri
			Cursor cursor = context.getContentResolver().query(Uri.parse(SensAppContract.Composite.CONTENT_URI + "/" + name), projection, null, null, null);
			if (cursor != null) {
				if (cursor.getCount() > 0) {
					cursor.moveToFirst();
					description = cursor.getString(cursor.getColumnIndexOrThrow(SensAppContract.Composite.DESCRIPTION));
					uri = Uri.parse(cursor.getString(cursor.getColumnIndexOrThrow(SensAppContract.Composite.URI)));
				}
				cursor.close();
			}
			// Get composite sensor names
			ArrayList<String> sensors = new ArrayList<String>();
			cursor = context.getContentResolver().query(Uri.parse(SensAppContract.Sensor.CONTENT_URI + "/composite/" + name), new String[]{SensAppContract.Sensor.NAME}, null, null, null);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					sensors.add(cursor.getString(cursor.getColumnIndexOrThrow(SensAppContract.Sensor.NAME)));
				}
				cursor.close();
			}
			// Get composite sensor addresses
			ArrayList<Uri> sensorUris = new ArrayList<Uri>();
			projection = new String[]{SensAppContract.Sensor.NAME, SensAppContract.Sensor.URI};
			String selection = SensAppContract.Sensor.NAME + " IN " + sensors.toString().replace("[", "(\"").replace(", ", "\", \"").replace("]", "\")");
			cursor = context.getContentResolver().query(SensAppContract.Sensor.CONTENT_URI, projection, selection, null, null);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					String sensorUri = cursor.getString(cursor.getColumnIndexOrThrow(SensAppContract.Sensor.URI));
					sensorUri += RestRequest.SENSOR_PATH + "/" + cursor.getString(cursor.getColumnIndexOrThrow(SensAppContract.Sensor.NAME));
					sensorUris.add(Uri.parse(sensorUri));
				}
				cursor.close();
			}
			return new Composite(name, description, uri, sensorUris);
		}
		
		public static int deleteComposite(Context context, String name) {
			int rowCompose = ComposeRQ.deleteCompose(context, C_COMPOSITE + " = \"" + name + "\"");
			int rowComposite = context.getContentResolver().delete(Uri.parse(CTE_CONTENT_URI + "/" + name), null, null);
			return rowCompose + rowComposite;
		}
	}

    public static class GraphRQ {

        public static int deleteGraph(Context context, int id) {
            return context.getContentResolver().delete(Uri.parse(G_CONTENT_URI + "/" + id), null, null);
        }

        public static int deleteGraph(Context context, String selection) {
            return context.getContentResolver().delete(G_CONTENT_URI, selection, null);
        }

        public static int updateGraph(Context context, int id, ContentValues values) {
            return context.getContentResolver().update(Uri.parse(G_CONTENT_URI + "/" + id), values, null, null);
        }

        public static int updateGraph(Context context, String selection, ContentValues values) {
            return context.getContentResolver().update(G_CONTENT_URI, values, selection, null);
        }

        public static Map<Integer, ContentValues> getGraphValues(Context context, String selection) {
            Map<Integer, ContentValues> graphs = new Hashtable<Integer, ContentValues>();
            Cursor cursor = context.getContentResolver().query(G_CONTENT_URI, null, selection, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    ContentValues cv = new ContentValues();
                    cv.put(G_ID, cursor.getInt(cursor.getColumnIndexOrThrow(G_ID)));
                    cv.put(G_TITLE, cursor.getString(cursor.getColumnIndexOrThrow(G_TITLE)));
                    graphs.put((Integer) cv.get(G_ID), cv);
                }
                cursor.close();
            }
            return graphs;
        }
    }

    public static class GraphSensorRQ {

        public static int deleteGraphSensor(Context context, int id) {
            return context.getContentResolver().delete(Uri.parse(GS_CONTENT_URI + "/" + id), null, null);
        }

        public static int deleteGraphSensor(Context context, String selection) {
            return context.getContentResolver().delete(GS_CONTENT_URI, selection, null);
        }

        public static int updateGraphSensor(Context context, int id, ContentValues values) {
            return context.getContentResolver().update(Uri.parse(GS_CONTENT_URI + "/" + id), values, null, null);
        }

        public static int updateGraphSensor(Context context, String selection, ContentValues values) {
            return context.getContentResolver().update(GS_CONTENT_URI, values, selection, null);
        }

        public static Map<Integer, ContentValues> getGraphSensorValues(Context context, String selection) {
            Map<Integer, ContentValues> graphSensors = new Hashtable<Integer, ContentValues>();
            Cursor cursor = context.getContentResolver().query(GS_CONTENT_URI, null, selection, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    ContentValues cv = new ContentValues();
                    cv.put(GS_ID, cursor.getInt(cursor.getColumnIndexOrThrow(GS_ID)));
                    cv.put(GS_TITLE, cursor.getString(cursor.getColumnIndexOrThrow(GS_TITLE)));
                    cv.put(GS_GRAPH, cursor.getString(cursor.getColumnIndexOrThrow(GS_GRAPH)));
                    cv.put(GS_STYLE, cursor.getString(cursor.getColumnIndexOrThrow(GS_STYLE)));
                    cv.put(GS_COLOR, cursor.getString(cursor.getColumnIndexOrThrow(GS_COLOR)));
                    cv.put(GS_MAX, cursor.getString(cursor.getColumnIndexOrThrow(GS_MAX)));
                    cv.put(GS_MIN, cursor.getString(cursor.getColumnIndexOrThrow(GS_MIN)));
                    cv.put(GS_SENSOR, cursor.getString(cursor.getColumnIndexOrThrow(GS_SENSOR)));
                    graphSensors.put((Integer) cv.get(GS_ID), cv);
                }
                cursor.close();
            }
            return graphSensors;
        }
    }
}

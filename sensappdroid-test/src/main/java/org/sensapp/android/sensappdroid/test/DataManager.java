package org.sensapp.android.sensappdroid.test;

import java.util.Random;

import org.sensapp.android.sensappdroid.contentprovider.SensAppCPContract;
import org.sensapp.android.sensappdroid.datarequests.DatabaseRequest;
import org.sensapp.android.sensappdroid.models.Sensor;
import org.sensapp.android.sensappdroid.restrequests.RequestErrorException;
import org.sensapp.android.sensappdroid.restrequests.RestRequest;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class DataManager {

	protected static void insertMeasures(ContentResolver resolver, int nbMeasure, int nbSensors) {
		ContentValues values;
		for (int count = 0 ; count < nbMeasure ; count ++) {
			values = new ContentValues();
			values.put(SensAppCPContract.Measure.SENSOR, "testSensor" + String.valueOf(new Random().nextInt(nbSensors)));
			values.put(SensAppCPContract.Measure.BASETIME, new Random().nextLong());
			values.put(SensAppCPContract.Measure.TIME, new Random().nextLong());
			values.put(SensAppCPContract.Measure.VALUE, new Random().nextInt());
			values.put(SensAppCPContract.Measure.UPLOADED, 0);
			resolver.insert(SensAppCPContract.Measure.CONTENT_URI, values);
		}
	}
	
	protected static void insertSensors(ContentResolver resolver, int nbSensors) {
		ContentValues values;
		for (int count = 0 ; count < nbSensors ; count ++) {
			values = new ContentValues();
			values.put(SensAppCPContract.Sensor.NAME, "testSensor" + String.valueOf(count));
			values.put(SensAppCPContract.Sensor.DESCRIPTION, "Test description " + String.valueOf(count));
			values.put(SensAppCPContract.Sensor.BACKEND, "raw");
			values.put(SensAppCPContract.Sensor.TEMPLATE, "Numerical");
			values.put(SensAppCPContract.Sensor.URI, "http://sensapp.fleurey.com:80");
			values.put(SensAppCPContract.Sensor.UNIT, "count");
			values.put(SensAppCPContract.Sensor.UPLOADED, 0);
			resolver.insert(SensAppCPContract.Sensor.CONTENT_URI, values);
		}
	}
	
	protected static void cleanMeasure(Context context) {
		Cursor cursor = context.getContentResolver().query(SensAppCPContract.Measure.CONTENT_URI, new String[]{"DISTINCT " + SensAppCPContract.Measure.SENSOR}, null, null, null);
		if (cursor != null) {
			String name = null;
			while (cursor.moveToNext()) {
				name = cursor.getString(cursor.getColumnIndex(SensAppCPContract.Measure.SENSOR));
				if (name.startsWith("test")) {
					context.getContentResolver().delete(Uri.parse(SensAppCPContract.Measure.CONTENT_URI + "/" + name), null, null);
				}
			}
			cursor.close();
		}
	}
	
	protected static void cleanSensors(Context context) throws IllegalArgumentException, RequestErrorException {
		Cursor cursor = context.getContentResolver().query(SensAppCPContract.Sensor.CONTENT_URI, new String[]{SensAppCPContract.Sensor.NAME, SensAppCPContract.Sensor.URI, SensAppCPContract.Sensor.UPLOADED}, null, null, null);
		if (cursor != null) {
			String name = null;
			while (cursor.moveToNext()) {
				name = cursor.getString(cursor.getColumnIndex(SensAppCPContract.Sensor.NAME));
				if (name.startsWith("test")) {
					if (cursor.getInt(cursor.getColumnIndexOrThrow(SensAppCPContract.Sensor.UPLOADED)) == 1) {
						Sensor sensor = DatabaseRequest.SensorRQ.getSensor(context, name);
						RestRequest.deleteSensor(Uri.parse(cursor.getString(cursor.getColumnIndexOrThrow(SensAppCPContract.Sensor.URI))), sensor);
					}
					context.getContentResolver().delete(Uri.parse(SensAppCPContract.Sensor.CONTENT_URI + "/" + name), null, null);
				}
			}
			cursor.close();
		}
	}
	
	protected static void cleanAll(Context context) throws IllegalArgumentException, RequestErrorException {
		cleanMeasure(context);
		cleanSensors(context);
	}
}
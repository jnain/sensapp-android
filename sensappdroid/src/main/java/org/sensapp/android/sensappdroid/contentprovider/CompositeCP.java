package org.sensapp.android.sensappdroid.contentprovider;

import org.sensapp.android.sensappdroid.database.ComposeTable;
import org.sensapp.android.sensappdroid.database.CompositeTable;
import org.sensapp.android.sensappdroid.database.MeasureTable;
import org.sensapp.android.sensappdroid.database.SensAppDatabaseHelper;
import org.sensapp.android.sensappdroid.database.SensorTable;

import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class CompositeCP {
	
	protected static final String BASE_PATH = "composites";
	
	private static final int COMPOSITES = 10;
	private static final int COMPOSITE = 20;
	private static final int MANAGESENSORS = 30;
	
	private static final UriMatcher measureURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		measureURIMatcher.addURI(SensAppContentProvider.AUTHORITY, BASE_PATH, COMPOSITES);
		measureURIMatcher.addURI(SensAppContentProvider.AUTHORITY, BASE_PATH + "/managesensors/*", MANAGESENSORS);
		measureURIMatcher.addURI(SensAppContentProvider.AUTHORITY, BASE_PATH + "/*", COMPOSITE);
	}

	private SensAppDatabaseHelper database;
	private Context context;
	
	public CompositeCP(Context context, SensAppDatabaseHelper database) {
		this.context = context;
		this.database = database;
	}
	
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		if (measureURIMatcher.match(uri) == MANAGESENSORS) {
			SQLiteDatabase db = database.getWritableDatabase();
			String query = "SELECT " 
					+ SensorTable.COLUMN_NAME 
					+ ", (SELECT count(" + ComposeTable.COLUMN_SENSOR + ") FROM " + ComposeTable.TABLE_COMPOSE 
					+ " WHERE " + ComposeTable.COLUMN_COMPOSITE + " = \"" + uri.getLastPathSegment() 
					+ "\") 'comp' FROM " + SensorTable.TABLE_SENSOR;
			return db.rawQuery(query, null);	
		} else {
			SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
			switch (measureURIMatcher.match(uri)) {
			case COMPOSITES:
				queryBuilder.setTables(CompositeTable.TABLE_COMPOSITE);
				break;
			case COMPOSITE:
				queryBuilder.setTables(CompositeTable.TABLE_COMPOSITE);
				queryBuilder.appendWhere(MeasureTable.COLUMN_ID + " = " + uri.getLastPathSegment());
				break;
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
			}
			SQLiteDatabase db = database.getWritableDatabase();
			Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
			cursor.setNotificationUri(context.getContentResolver(), uri);
			return cursor;
		}
	}

	public String getType(Uri uri) {
		return null;
	}
	
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = database.getWritableDatabase();
		long id = 0;
		switch (measureURIMatcher.match(uri)) {
		case COMPOSITES:
			id = db.insert(CompositeTable.TABLE_COMPOSITE, null, values);
			break;
		default:
			throw new IllegalArgumentException("Bad URI: " + uri);
		}
		context.getContentResolver().notifyChange(uri, null);
		return Uri.parse(BASE_PATH + "/" + id);
	}
	
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = database.getWritableDatabase();
		int rowsDeleted = 0;
		switch (measureURIMatcher.match(uri)) {
		case COMPOSITES:
			rowsDeleted = db.delete(CompositeTable.TABLE_COMPOSITE, selection, selectionArgs);
			break;
		case COMPOSITE:
			String name = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsDeleted = db.delete(CompositeTable.TABLE_COMPOSITE, CompositeTable.COLUMN_NAME + " = \"" + name + "\"", null);
			} else {
				rowsDeleted = db.delete(CompositeTable.TABLE_COMPOSITE, CompositeTable.COLUMN_NAME + " = \"" + name + "\" and " + selection, selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		context.getContentResolver().notifyChange(uri, null);
		return rowsDeleted;
	}

	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = database.getWritableDatabase();
		int rowsUpdated = 0;
		switch (measureURIMatcher.match(uri)) {
		case COMPOSITES:
			rowsUpdated = db.update(CompositeTable.TABLE_COMPOSITE, values, selection, selectionArgs);
			break;
		case COMPOSITE:
			String name = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = db.update(CompositeTable.TABLE_COMPOSITE, values, MeasureTable.COLUMN_ID + " = \"" + name + "\"", null);
			} else {
				rowsUpdated = db.update(CompositeTable.TABLE_COMPOSITE, values, MeasureTable.COLUMN_ID + " = \"" + name + "\" and " + selection, selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("[2] Unknown URI: " + uri);
		}
		context.getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}
}

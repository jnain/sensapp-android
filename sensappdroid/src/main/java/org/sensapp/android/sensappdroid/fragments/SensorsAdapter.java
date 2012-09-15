package org.sensapp.android.sensappdroid.fragments;

import org.sensapp.android.sensappdroid.R;
import org.sensapp.android.sensappdroid.contract.SensAppContract;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SensorsAdapter extends CursorAdapter {

	public SensorsAdapter(Context context, Cursor c) {
		super(context, c);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		byte[] byteIcon = cursor.getBlob(cursor.getColumnIndex(SensAppContract.Sensor.ICON));
		ImageView icon = (ImageView) view.findViewById(R.id.icon);
		if (byteIcon != null) {
			icon.setImageBitmap(BitmapFactory.decodeByteArray(byteIcon, 0, byteIcon.length));
		} else {
			icon.setImageResource(R.drawable.ic_launcher);
		}
		TextView name = (TextView) view.findViewById(R.id.name);
		name.setText(cursor.getString(cursor.getColumnIndex(SensAppContract.Sensor.NAME)));
		TextView count = (TextView) view.findViewById(R.id.count);
		Cursor c = context.getContentResolver().query(Uri.parse(SensAppContract.Measure.CONTENT_URI + "/" + name.getText()), new String[]{SensAppContract.Measure.ID}, null, null, null);
		count.setText("(" + String.valueOf(c.getCount()) + ")");
		c.close();
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View v = LayoutInflater.from(context).inflate(R.layout.sensor_row, parent, false);
		bindView(v, context, cursor);
		return v;
	}
}
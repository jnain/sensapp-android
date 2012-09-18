package org.sensapp.android.sensappdroid.activities;

import org.sensapp.android.sensappdroid.R;
import org.sensapp.android.sensappdroid.contract.SensAppContract;
import org.sensapp.android.sensappdroid.fragments.CompositeListFragment.ManageCompositeDialogFragment;
import org.sensapp.android.sensappdroid.fragments.SensorListFragment.OnSensorSelectedListener;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class CompositeActivity extends FragmentActivity implements OnSensorSelectedListener {
	
	private static final String TAG = CompositeActivity.class.getSimpleName();

	private String compositeName;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.d(TAG, "__ON_CREATE__");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensors);
        compositeName = getIntent().getData().getLastPathSegment();
        setTitle(compositeName);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.composite_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.manage_sensors:
			ManageCompositeDialogFragment.newInstance(compositeName).show(getSupportFragmentManager(), "manage_sensor");
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSensorSelected(Uri uri) {
		Intent i = new Intent(this, MeasuresActivity.class);
		i.setData(Uri.parse(SensAppContract.Measure.CONTENT_URI + "/" + uri.getLastPathSegment()));
		startActivity(i);
	}
}
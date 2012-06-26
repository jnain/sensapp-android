package net.modelbased.sensapp.android.sensappdroid;

import net.modelbased.sensapp.android.sensappdroid.contentprovider.SensAppMeasureProviderContract;
import net.modelbased.sensapp.android.sensappdroid.database.MeasureTable;
import net.modelbased.sensapp.android.sensappdroid.restservice.PushDataTest;
import android.app.ListActivity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class SensAppActivity extends ListActivity implements LoaderCallbacks<Cursor> {
	
	public static final String ACTION_UPLOAD = "net.modelbased.sensapp.android.sensappdroid.ACTION_UPLOAD";
	public static final String ACTION_FLUSH_ALL = "net.modelbased.sensapp.android.sensappdroid.ACTION_FLUSH_ALL";
	public static final String ACTION_FLUSH_UPLOADED = "net.modelbased.sensapp.android.sensappdroid.ACTION_FLUSH_UPLOADED";
	
	private static final String TAG = SensAppActivity.class.getName();
	private static final int MENU_DELETE_ID = Menu.FIRST + 1;
	
	private SimpleCursorAdapter adapter;
	Intent intentService;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.d(TAG, "__ON_CREATE__");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.measure_list);
        initAdapter();
		registerForContextMenu(getListView());
		intentService = new Intent(this, SensAppService.class);
    }

    
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, MENU_DELETE_ID, 0, R.string.menu_delete);
	}


	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_DELETE_ID:
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			Uri uri = Uri.parse(SensAppMeasureProviderContract.CONTENT_URI + "/" + info.id);
			getContentResolver().delete(uri, null, null);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent i = new Intent(this, MeasureActivity.class);
		Uri measureUri = Uri.parse(SensAppMeasureProviderContract.CONTENT_URI + "/" + id);
		i.putExtra(SensAppMeasureProviderContract.CONTENT_ITEM_TYPE, measureUri);
		startActivity(i);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.list_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i = new Intent(this, SensAppService.class);
		switch (item.getItemId()) {
		case R.id.insert:
			PushDataTest.push(this);
			return true;
		case R.id.upload:
			i.setAction(ACTION_UPLOAD);
			startService(i);
			return true;
		case R.id.stop_service:
			stopService(intentService);
			return true;
		case R.id.flush_database:
			i.setAction(ACTION_FLUSH_ALL);
			startService(i);
			return true;
		case R.id.flush_uploaded:
			i.setAction(ACTION_FLUSH_UPLOADED);
			startService(i);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void initAdapter() {
		String[] from = new String[] {MeasureTable.COLUMN_VALUE};
		int[] to = new int[] {R.id.label};
		getLoaderManager().initLoader(0, null, this);
		adapter = new SimpleCursorAdapter(this, R.layout.measure_row, null, from, to, 0);
		setListAdapter(adapter);
	}
    
	@Override
	public void onDestroy() {
		Log.d(TAG, "__ON_DESTROY__");
		super.onDestroy();
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Log.d(TAG, "__ON_CREATE_LOADER__");
		String[] projection = {MeasureTable.COLUMN_ID, MeasureTable.COLUMN_VALUE};
		CursorLoader cursorLoader = new CursorLoader(this, SensAppMeasureProviderContract.CONTENT_URI, projection, null, null, null);
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		Log.d(TAG, "__ON_LOAD_FINISHED__");
		adapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		Log.d(TAG, "__ON_LOAD_RESET__");
		adapter.swapCursor(null);
	}
}
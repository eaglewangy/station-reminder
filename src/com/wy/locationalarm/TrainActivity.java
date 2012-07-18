package com.wy.locationalarm;

import com.wy.locationalarm.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class TrainActivity extends Activity{
	
	private ArrayAdapter<String> trainAdpter;
	private ListView listView;
	private String[] provinceList;
	
	private DBAdapter dbAdapter;
	
	private String currentCity;
	private String province;
	private Cursor cityCursor;
	
	private ImageButton lookupBtn;
	
	private SharedPreferences userPrefs;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
        setContentView(R.layout.train_layout);
        
        dbAdapter = new DBAdapter(this);
        dbAdapter.open();
        
        Resources res = getResources();
        provinceList = res.getStringArray(R.array.provincelist);
        
        listView = (ListView)findViewById(R.id.provincelist);
        
        trainAdpter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, provinceList);
        listView.setAdapter(trainAdpter); 
        
        listView.setOnItemClickListener(listItemListener);
        
        lookupBtn = (ImageButton)findViewById(R.id.trainlookup);
        lookupBtn.setOnClickListener(lookupListener);
        
        userPrefs = getSharedPreferences(Constants.USER_PREFERENCE, Activity.MODE_WORLD_READABLE);
	}
	
	private OnClickListener lookupListener = new OnClickListener() {
		public void onClick(View v) {
			
			Editor editor = userPrefs.edit();
	  	  	editor.putString(Constants.PREF_SEARCH_FROM, Constants.PREF_SEARCH_FROM_TRAIN);
	  	  	editor.commit();
	  	  	
			boolean flag = onSearchRequested();
			if (flag)
				Log.v(Constants.LOG, " " + "search is started!");
			else
				Log.v(Constants.LOG, " " + "search is not started!");
		}
	};
	
	private OnItemClickListener listItemListener = new OnItemClickListener() {
	    public void onItemClick(AdapterView parent, View v, int position, long id)
	    {
	    	province = provinceList[position];
	    	Log.v(Constants.LOG, " Province: " + provinceList[position]);
	        //get stations
	    	dbAdapter = new DBAdapter(getApplicationContext());
	        dbAdapter.open();
	    	cityCursor = dbAdapter.GetCities(province);
	    	
	    	AlertDialog.Builder builder = new AlertDialog.Builder(TrainActivity.this);
			AlertDialog alertDialog;
			builder.setTitle(getString(R.string.choosestation));
			builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int position) {
							dialog.cancel();
						}
					});

			builder.setCancelable(true);

			builder.setSingleChoiceItems(cityCursor, -1, Constants.STATION_COLUMN, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int position) {
							//set current station
							SetStation(position);
							//add this to database
							Cursor cur = dbAdapter.GetCity(currentCity);
							//in TRAIN table, lat index is 3, and lng index is 4
							double lat = cur.getDouble(Constants.LAT_IDX - 1);
							double lng = cur.getDouble(Constants.LNG_IDX - 1);
							InsertData(lat, lng);
							Intent intent = new Intent(TrainActivity.this, MainActivity.class);
							startActivity(intent);
							dialog.cancel();
						}
					});

			alertDialog = builder.create();
			builder.show();
	    }
	};
	
	private void SetStation(int pos){
		cityCursor.moveToPosition(pos);
		//in TRAIN table, station index is 2
		currentCity = cityCursor.getString(Constants.STATION_IDX - 1);
		Log.v(Constants.LOG, " " + "station: " + currentCity);
	}
	
	private void InsertData(double lat, double lng)
	{
		ContentValues hotspotValue = new ContentValues();
		hotspotValue.put(Constants.TYPE_COLUMN, "1");
		hotspotValue.put(Constants.HS_PROVINCE, province);
		hotspotValue.put(Constants.HS_CITY, currentCity);
		hotspotValue.putNull(Constants.LINE_COLUMN);
		hotspotValue.put(Constants.STATION_COLUMN, currentCity);
		hotspotValue.put(Constants.HS_CHECKED, "1");
		hotspotValue.put(Constants.HS_LAT, String.valueOf(lat));
		hotspotValue.put(Constants.HS_LNG, String.valueOf(lng));
		dbAdapter.AddTrainHotspot(hotspotValue);
	}
	
	@Override  
	public boolean onKeyDown(int keyCode, KeyEvent event) {  
	      
	    if(keyCode == KeyEvent.KEYCODE_BACK){  
	    	//Toast.makeText(this, "back is coming", Toast.LENGTH_LONG).show();
	    	Intent intent = new Intent(this, MainActivity.class);
	    	startActivity(intent);
	    	finish();
	        return true;  
	    }  
	      
	    return super.onKeyDown(keyCode, event);  
	}  
}

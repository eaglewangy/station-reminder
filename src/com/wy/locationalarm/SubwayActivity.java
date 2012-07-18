package com.wy.locationalarm;

import com.wy.locationalarm.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class SubwayActivity extends Activity{

	private ImageButton replaceBtn;
	private ImageButton lookupBtn;
	String[] cities;
	Context context;
	TextView citytextView;
	ListView listView;
	
	private DBAdapter dbAdapter;
	private SimpleCursorAdapter lineAdapter;
	private Cursor lineCursor;
	private Cursor curStation;
	
	private String type = "0";
	private String currentCity;
	private String currentLine;
	private String currentStation;
	private double lat;
	private double lng;
	private String checked = "1";
	
	private SharedPreferences userPrefs;
	
	private int checkedItemCurrentCity;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
        setContentView(R.layout.subway_layout);
        replaceBtn = (ImageButton)findViewById(R.id.replace);
        lookupBtn = (ImageButton)findViewById(R.id.lookup);
        citytextView = (TextView)findViewById(R.id.subwaycity);
        listView = (ListView)findViewById(R.id.linelist);
        //currentCity = citytextView.getText().toString();
       
        userPrefs = getSharedPreferences(Constants.USER_PREFERENCE, Activity.MODE_WORLD_READABLE);
        currentCity = userPrefs.getString(Constants.PREF_SUBWAYCITY, getString(R.string.currentcity));
        citytextView.setText(currentCity);
        
        dbAdapter = new DBAdapter(this);
        dbAdapter.open();
        
        lineCursor = dbAdapter.GetAllItems(Constants.SUBWAY_TABLE, citytextView.getText().toString(), null);
        lineAdapter = new SimpleCursorAdapter(this,
        	    android.R.layout.simple_list_item_1, // Use a template
        	                                          // that displays a
        	                                          // text view
        	    lineCursor, // Give the cursor to the list adapter
        	    new String[] {Constants.LINE_COLUMN}, // Map the NAME column in the
        	                                         // people database to...
        	    new int[] {android.R.id.text1}); // The "text1" view defined in
        	                                     // the XML template
        listView.setAdapter(lineAdapter);
        listView.setOnItemClickListener(listItemListener);
        
        Resources res = getResources();
        cities = res.getStringArray(R.array.allcities);
        
        replaceBtn.setOnClickListener(replaceListener);
        lookupBtn.setOnClickListener(lookupListener);
        
        checkedItemCurrentCity = userPrefs.getInt(Constants.PREF_CURRENT_CITY, 0);
	}
	
	private OnItemClickListener listItemListener = new OnItemClickListener() {
	    public void onItemClick(AdapterView parent, View v, int position, long id)
	    {
	    	currentLine = dbAdapter.GetLine(id);
	    	Log.v(Constants.LOG, " choose " + currentLine);
	        //get stations
	    	curStation = dbAdapter.GetAllItems(Constants.SUBWAY_TABLE, citytextView.getText().toString(), currentLine);
	    	AlertDialog.Builder builder = new AlertDialog.Builder(SubwayActivity.this);
			AlertDialog alertDialog;
			builder.setTitle(getString(R.string.choosestation));
			builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int position) {
							Log.v(Constants.LOG, " " + "search is started!");
							dialog.cancel();
						}
					});

			builder.setCancelable(true);

			builder.setSingleChoiceItems(curStation, -1, Constants.STATION_COLUMN, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int position) {
							//set current station
							SetStation(position);
							//add this to database
							Cursor cur = dbAdapter.GetSingleItem(currentStation);
							lat = cur.getDouble(Constants.LAT_IDX);
							lng = cur.getDouble(Constants.LNG_IDX);
							InsertData(lat, lng);
							Intent intent = new Intent(SubwayActivity.this, MainActivity.class);
							startActivity(intent);
							dialog.cancel();
						}
					});

			alertDialog = builder.create();
			builder.show();
	    }
	};
	
	private OnClickListener replaceListener = new OnClickListener() {
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Log.v(Constants.LOG, " " + "replace button is clicked!");

			AlertDialog.Builder builder = new AlertDialog.Builder(SubwayActivity.this);
			AlertDialog alertDialog;
			builder.setTitle(getString(R.string.choosecity));
			builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int position) {
							Log.v(Constants.LOG, " " + "search is started!");
							dialog.cancel();
						}
					});

			builder.setCancelable(true);

			builder.setSingleChoiceItems(cities, checkedItemCurrentCity, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int position) {
							if (checkedItemCurrentCity == position)
		  	    				return;
							
							checkedItemCurrentCity = position;
							
							citytextView.setText(cities[position]);
							currentCity = cities[position];
							Log.v(Constants.LOG, " " + "Current city: " + cities[position]);
							lineCursor = dbAdapter.GetAllItems(Constants.SUBWAY_TABLE, cities[position], null);
							lineAdapter.changeCursor(lineCursor);
							dialog.cancel();
							
							SavePerf();
						}
					});

			alertDialog = builder.create();
			builder.show();
		}
	};

	private OnClickListener lookupListener = new OnClickListener() {
		public void onClick(View v) {
			
			Editor editor = userPrefs.edit();
	  	  	editor.putString(Constants.PREF_SEARCH_FROM, Constants.PREF_SEARCH_FROM_SUBWAY);
	  	  	editor.commit();
	  	  	
			boolean flag = onSearchRequested();
			if (flag)
				Log.v(Constants.LOG, " " + "search is started!");
			else
				Log.v(Constants.LOG, " " + "search is not started!");
		}
	};
	
	private void SetStation(int pos){
		curStation.moveToPosition(pos);
		currentStation = curStation.getString(Constants.STATION_IDX);
		Log.v(Constants.LOG, " " + "station: " + currentStation);
	}
	
	private String GetStation(){
		return currentStation;
	}
	
	private void InsertData(double lat, double lng)
	{
		ContentValues hotspotValue = new ContentValues();
		hotspotValue.put(Constants.TYPE_COLUMN, type);
		hotspotValue.put(Constants.HS_CITY, currentCity);
		hotspotValue.put(Constants.LINE_COLUMN, currentLine);
		hotspotValue.put(Constants.STATION_COLUMN, GetStation());
		hotspotValue.put(Constants.HS_CHECKED, checked);
		hotspotValue.put(Constants.HS_LAT, String.valueOf(lat));
		hotspotValue.put(Constants.HS_LNG, String.valueOf(lng));
		dbAdapter.AddSubwayHotspot(hotspotValue);
	}
	
	private void SavePerf(){
		Editor editor = userPrefs.edit();
  	  	editor.putString(Constants.PREF_SUBWAYCITY, currentCity);
  	  	//save current choosen city
		editor.putInt(Constants.PREF_CURRENT_CITY, checkedItemCurrentCity);
  	  	editor.commit();
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
	
	@Override
	public void onResume() {
		super.onResume();
		dbAdapter.open();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		//dbAdapter.close();
	}

	@Override
	public void onDestroy() {
		// Close the database
		//dbAdapter.close();
		super.onDestroy();
	}
}

package com.wy.locationalarm;

import java.util.ArrayList;

import com.wy.locationalarm.R;

import android.app.Activity;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

class Item
{
	//subway or train
	public int Type;
	public long Id;
	public String CityName;
	public String Province;
	public String Line;
	public String Station;
	public int Checked;
	public String Lat;
	public String Lng;
	public String Text;
}

public class SearchableActivity extends ListActivity {
	
	private DBAdapter dbAdapter;
	private ArrayAdapter<String> totalAdpter;
	private Cursor cursor;
	
	private ArrayList<String> stations;
	private ArrayList<Item> items;
	
	private SharedPreferences userPrefs;
	private String searchFrom;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.search_layout);
	    
	    userPrefs = getSharedPreferences(Constants.USER_PREFERENCE, Activity.MODE_WORLD_READABLE);
	    searchFrom = userPrefs.getString(Constants.PREF_SEARCH_FROM, "");
	    //Toast.makeText(this, searchFrom, Toast.LENGTH_LONG).show();
	    
	    stations = new ArrayList<String>();
	    items = new ArrayList<Item>();
	    // Get the intent, verify the action and get the query
	    Intent intent = getIntent();
	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	      String query = intent.getStringExtra(SearchManager.QUERY);
	      Search(query);
	      //Toast.makeText(this, query, Toast.LENGTH_LONG).show();
	      for (int i = 0; i < items.size(); ++i)
	      {
	    	  stations.add(items.get(i).Text);
	      }
	      if (stations.size() == 0)
	    	  stations.add("No data");
	      
	      totalAdpter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, stations);
	      
	      setListAdapter(totalAdpter);
	    }
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if (items.size() > 0){
			InsertData(position);
		}
		
		Intent intent = new Intent(SearchableActivity.this, MainActivity.class);
		startActivity(intent);
	}
	
	private void InsertData(int position)
	{
		Item item = items.get(position);
		ContentValues hotspotValue = new ContentValues();
		dbAdapter = new DBAdapter(this);
		dbAdapter.open();
		int type = item.Type;
		if (type == Constants.SUBWAY){
			hotspotValue.put(Constants.TYPE_COLUMN, Constants.SUBWAY);
			hotspotValue.put(Constants.HS_CITY, item.CityName);
			hotspotValue.put(Constants.LINE_COLUMN, item.Line);
			hotspotValue.put(Constants.STATION_COLUMN, item.Station);
			hotspotValue.put(Constants.HS_CHECKED, item.Checked);
			hotspotValue.put(Constants.HS_LAT, item.Lat);
			hotspotValue.put(Constants.HS_LNG, item.Lng);
			dbAdapter.AddSubwayHotspot(hotspotValue);
		}
		else if (type == Constants.TRAIN){
			hotspotValue.put(Constants.TYPE_COLUMN, Constants.TRAIN);
			hotspotValue.put(Constants.HS_PROVINCE, item.Province);
			hotspotValue.put(Constants.HS_CITY, item.CityName);
			hotspotValue.put(Constants.STATION_COLUMN, item.Station);
			hotspotValue.put(Constants.HS_CHECKED, item.Checked);
			hotspotValue.put(Constants.HS_LAT, item.Lat);
			hotspotValue.put(Constants.HS_LNG, item.Lng);
			dbAdapter.AddTrainHotspot(hotspotValue);
		}
		
		//dbAdapter.close();
	}
	
	private void Search(String key)
	{
		String station = null;
		
		if (searchFrom.length() == 0)
			 return;
		
		dbAdapter = new DBAdapter(this);
		dbAdapter.open();
		try{
			cursor = dbAdapter.SubwayStations(key);
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
			{
				if (searchFrom.equals(Constants.PREF_SEARCH_FROM_TRAIN))
					break;
				
				Item item = new Item();
				//id = cursor.getLong(Constants.ID_IDX);
				station = cursor.getString(Constants.STATION_IDX);
				int cityId = cursor.getInt(Constants.CITYNAME_IDX);
				String line = cursor.getString(Constants.LINE_IDX);
				String cityName = dbAdapter.GetCityName(cityId);
				String lat = cursor.getString(Constants.LAT_IDX);
				String lng = cursor.getString(Constants.LNG_IDX);
				
				String content = cityName + " " + line + "\n" + station;
				//item.Id = id;
				item.Type = Constants.SUBWAY;
				item.CityName = cityName;
				item.Line = line;
				item.Station = station;
				item.Checked = 1;
				item.Lat = lat;
				item.Lng = lng;
				item.Text = content;
				items.add(item);
			}
			
			cursor = dbAdapter.TrainStations(key);
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
			{
				if (searchFrom.equals(Constants.PREF_SEARCH_FROM_SUBWAY))
					break;
				
				Item item = new Item();
				//id = cursor.getLong(Constants.ID_IDX);
				String province = cursor.getString(Constants.PROVINCE_IDX);
				station = cursor.getString(Constants.STATION_IDX - 1);
				String lat = cursor.getString(Constants.LAT_IDX - 1);
				String lng = cursor.getString(Constants.LNG_IDX - 1);
				String content = province + " " + station;
				
				//item.Id = id;
				item.Type = Constants.TRAIN;
				item.Province = province;
				item.Line = null;
				item.CityName = station;
				item.Station = station;
				item.Checked = 1;
				item.Lat = lat;
				item.Lng = lng;
				item.Text = content;
				
				items.add(item);
			}
			
			cursor.close();
			//dbAdapter.close();
			
		}catch(Exception e)
		{
			if (cursor != null)
				cursor.close();
			
			Log.v(Constants.LOG, "No data found during SearchActivity.");
		}
	}
}

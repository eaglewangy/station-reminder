package com.wy.locationalarm;

import com.baidu.location.LocationChangedListener;
import com.baidu.location.LocationClient;
import com.baidu.location.ReceiveListener;
import com.wy.locationalarm.util.JsonParser;
import com.wy.locationalarm.util.Util;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

public class MyApplication extends Application{
	
	public LocationClient mLocationClient = null;
	private String mData;
	
	private SharedPreferences userPrefs;
	private Util util;
	
	private Intent dummyService;
	
	private JsonParser jsonParser;
	
	@Override
    public void onCreate() {
		mLocationClient = new LocationClient(this);
		
		dummyService = new Intent(this, DummyService.class);
		startService(dummyService);
		
		jsonParser = new JsonParser();
		util = new Util(this);
	} 
	
	public class MyLocationChangedListener implements LocationChangedListener {
		public void onLocationChanged() {
			//Log.e("LocationAlarmLLLLLLLLLL", "LocationChangedListener: The location has changed.");
		}
	}

	public class MyReceiveListenner implements ReceiveListener {
		public void onReceive(String strData) {
			//Toast.makeText(getApplicationContext(), strData, Toast.LENGTH_LONG).show();
			if (strData == null || strData.length() == 0)
				return;
			if (!jsonParser.parse(strData))
				return;
			
			String str = "Lat: " + jsonParser.lat + "\n" +
				"Lng: " + jsonParser.lng + "\n" +
				"Address: " + jsonParser.addr;
			Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG).show();
			Log.e("LocationAlarm", "ReceiveListener: " + strData);
			
			userPrefs = getSharedPreferences(Constants.USER_PREFERENCE, Activity.MODE_WORLD_READABLE);

			Editor editor = userPrefs.edit();
			editor.putString(Constants.PREF_LAT, jsonParser.lat);
			editor.putString(Constants.PREF_LNG, jsonParser.lng);
			String address =jsonParser.addr;
			editor.putString(Constants.PREF_ADDRESS, address);
			editor.commit();

			Location location = new Location("");
			location.setLatitude(Double.parseDouble(jsonParser.lat));
			location.setLongitude(Double.parseDouble(jsonParser.lng));
			
			if (util.IsCallAlarm(location)) {
				util.DisplayNotafication();
			}
		}
	}
}

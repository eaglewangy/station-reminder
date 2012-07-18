package com.wy.locationalarm;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.wy.locationalarm.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class MapsActivity extends MapActivity{
	private MapView mapView;
	private MapController mapController;
	
	private ImageButton locationBtn;
	private ImageButton searchBtn;
	private TextView gpsStatus;
	private SharedPreferences userPrefs;
	
	private Bundle bundle;
	
	private MapsOverlay mapsOverlay;
    //private LocationManager locationManager;
    //private Location cuurentLocation;
    
    private List<Overlay> overlays;
    private Drawable marker;
    
    private DBAdapter dbAdpater;
    
    private boolean IsDisplayBtn;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);
        
        userPrefs = getSharedPreferences(Constants.USER_PREFERENCE, Activity.MODE_WORLD_READABLE);
        
        marker = getResources().getDrawable(R.drawable.pin_red);
        
        //locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 50, locationListener);
        
        this.mapView = (MapView)findViewById(R.id.map_view);
        this.mapView.setBuiltInZoomControls(true);
		mapController = this.mapView.getController();
		mapController.setZoom(16);
		
		overlays = this.mapView.getOverlays();
		///////////////////////////////////////////////////////////////////////////////////////////////////
		IsDisplayBtn = getIntent().getBooleanExtra(Constants.DISPLAY_BUTTON, false);
		//Toast.makeText(this, String.valueOf(IsDisplayBtn), Toast.LENGTH_LONG).show();
		
		locationBtn = (ImageButton)findViewById(R.id.currentlocation);
		searchBtn = (ImageButton)findViewById(R.id.maplookup);
		gpsStatus = (TextView)findViewById(R.id.gpsstatus);
		locationBtn.setOnClickListener(getLocationListener);
		searchBtn.setOnClickListener(searchListener);
		gpsStatus.setText(getString(R.string.gpsstatus) + " " + getString(R.string.disconnected));
		
		dbAdpater = new DBAdapter(this);
		dbAdpater.open();
		
		//if this activity is called by ConfigureActivity, we display current location and search buttons
		//else hide the two buttons
		if (!IsDisplayBtn)
		{
			locationBtn.setVisibility(View.GONE);
			searchBtn.setVisibility(View.GONE);
			gpsStatus.setVisibility(View.GONE);
			
			AddHotspotToMap();
		}
	
		if (IsDisplayBtn)
			overlays.clear();
		
		mapView.postInvalidate();
	}
	
	private void AddHotspotToMap(){
		
		GeoPoint currentPoint = null;
		Cursor cursor = dbAdpater.GetAllHotspots();
		
		//overlays = this.mapView.getOverlays();
		overlays.clear();
		
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
		{
			Double lat = cursor.getDouble(Constants.HS7) * 1E6;
			Double lng = cursor.getDouble(Constants.HS8) * 1E6;
			
			currentPoint = new GeoPoint(lat.intValue(), lng.intValue());
			
			String content = getFormattedGeoCode(currentPoint);
			//newline is flag
			content += "_";
			OverlayItem overlayitem = new OverlayItem(currentPoint, "", content);
			mapsOverlay = new MapsOverlay(this, marker);
			mapsOverlay.addOverlay(overlayitem);
			overlays.add(mapsOverlay);
		}
		
		mapController.setZoom(5);
		
		cursor.close();
		
		if (currentPoint != null)
		{
			mapController.setCenter(currentPoint);
			mapController.animateTo(currentPoint);
		}
		
		mapView.postInvalidate();
	}
	
	private String getFormattedGeoCode(GeoPoint location) 
    {
        Geocoder geocoder = new Geocoder(this);
        List<Address> addresses;
        try
        {
            addresses = geocoder.getFromLocation(location.getLatitudeE6() / 1000000.0, location.getLongitudeE6() / 1000000.0, 1);
        }
        catch (IOException e)
        {
            Log.e(Constants.LOG, e.toString());
            addresses = Collections.emptyList();
        }
        final String name;
        if (addresses.size() == 0)
        {
        	Log.e(Constants.LOG, "No address could be found.");
            name = getString(R.string.unknownlocation);
        }
        else
        {
            int size = addresses.get(0).getMaxAddressLineIndex() + 1;
            StringBuilder string = new StringBuilder();
            for (int i = 0; i < size; i++)
            {
                String addressLine = addresses.get(0).getAddressLine(i);
                if (string.length() != 0)
                {
                    string.append(", ");
                }
                string.append(addressLine);
            }
            name = string.toString();
        }
        return name;
    }
	
	private OnClickListener searchListener = new OnClickListener() {
		public void onClick(View v) {
			
			Editor editor = userPrefs.edit();
	  	  	editor.putString(Constants.PREF_SEARCH_FROM, Constants.PREF_SEARCH_FROM_BOTH);
	  	  	editor.commit();
	  	  	
			boolean flag = onSearchRequested();
			if (flag)
				Log.v(Constants.LOG, " " + "search is started!");
			else
				Log.v(Constants.LOG, " " + "search is not started!");
		}
	};
	
	private OnClickListener getLocationListener = new OnClickListener() {
		public void onClick(View v) {
			//locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);
			MoveToCurrentLocation();
			//locationManager.removeUpdates(locationListener);
		}
	};
	
	private void MoveToCurrentLocation()
	{
		overlays = this.mapView.getOverlays();
		overlays.clear();
		
		//Double lat = cuurentLocation.getLatitude() * 1E6;
		//Double lng = cuurentLocation.getLongitude() * 1E6;
		String lat_ = userPrefs.getString(Constants.PREF_LAT, "");
		String lng_ = userPrefs.getString(Constants.PREF_LNG, "");
		if (lat_.length() == 0 || lng_.length() == 0)
			return;
		
		if (IsDisplayBtn)
			gpsStatus.setText(getString(R.string.gpsstatus) + " " + getString(R.string.connected));
		
		Double lat = Double.parseDouble(lat_) * 1E6;
		Double lng = Double.parseDouble(lng_) * 1E6;
		
		GeoPoint currentPoint = new GeoPoint(lat.intValue(), lng.intValue());
		
		//String content = getFormattedGeoCode(currentPoint);
		String content = userPrefs.getString(Constants.PREF_ADDRESS, "");
		content += "\n";
		content += getString(R.string.mapalarmcontent);
		OverlayItem overlayitem = new OverlayItem(currentPoint, getString(R.string.yourlocation), content);
		mapsOverlay = new MapsOverlay(this, marker);
		mapsOverlay.addOverlay(overlayitem);
		overlays.add(mapsOverlay);
		mapController.setZoom(16);
		mapController.setCenter(currentPoint);
		mapController.animateTo(currentPoint);
		mapView.postInvalidate();
	}
	
	@Override
	 public void onResume(){
		 super.onResume();
		 
		 //if (bundle == null){}
			 //AddHotspotToMap();
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
	 
	/*
	@Override
    protected void onPause()
    {
        this.myLocationOverlay.disableMyLocation();
        super.onPause();
    }
    
    @Override
    protected void onResume()
    {
        super.onResume();
        Log.e(Constants.LOG, "onResume:enableMyLocation: " + this.myLocationOverlay.enableMyLocation());
    }
    */
	
	@Override
	protected boolean isRouteDisplayed() {
	// IMPORTANT: This method must return true if your Activity
	// is displaying driving directions. Otherwise return false.
		return false;
	}
}
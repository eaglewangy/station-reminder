package com.wy.locationalarm;

import java.lang.reflect.Field;

import com.wy.locationalarm.R;
import com.baidu.location.LocServiceMode;
import com.baidu.location.LocationClient;
import com.wy.locationalarm.util.Util;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.TabHost.OnTabChangeListener;

public class MainActivity extends TabActivity  {
	private ImageButton imageButton;
	
	private DBAdapter dbAdpter;
	
	private TabWidget tabWidget;
	Field mBottomLeftStrip;
	Field mBottomRightStrip;
	
	private Util util;
	
	private SharedPreferences userPrefs;
	private long sleepTime = 3600;
	
	private LocationClient mLocClient;
	private LocServiceMode mServiceMode = LocServiceMode.Immediat;
	
	private static final int HANDLERID = 0x101;
	Thread thread = null;
	
	Handler sweepHandler = new Handler() {  
        public void handleMessage(Message msg) {   
             switch (msg.what) {   
                  case HANDLERID:   
                	  GetLocation();
                      break;   
             }   
             super.handleMessage(msg);   
        }   
   }; 
   
   private void GetLocation(){
	   mLocClient.getLocation();
   }
   
   class MessageThread implements Runnable {   
       public void run() {  
            while (!Thread.currentThread().isInterrupted()) {    
                 Message message = new Message();   
                 message.what = HANDLERID;
                 sweepHandler.sendMessage(message);  
                 
                 userPrefs = getSharedPreferences(Constants.USER_PREFERENCE, Activity.MODE_WORLD_READABLE);
                 
                 sleepTime = userPrefs.getLong(Constants.PREF_SLEEP_TIME,  10000);
                 
                 try {   
                      Thread.sleep(sleepTime);    
                      
                 } catch (InterruptedException e) {   
                      Thread.currentThread().interrupt();   
                 }   
            }   
       }   
  }
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mLocClient = ((MyApplication)getApplication()).mLocationClient;
        mLocClient.setAddrType("detail");
        mLocClient.setCoorType("wgs84");
        mLocClient.setServiceMode(mServiceMode);
        //mLocClient.setTimeSpan(5 * 1000);
        mLocClient.addLocationChangedlistener(((MyApplication)getApplication()).new MyLocationChangedListener());
        mLocClient.addRecerveListener(((MyApplication)getApplication()).new MyReceiveListenner());
        mLocClient.start();
        
        thread = new Thread(new MessageThread()); 
		thread.start();
        
        int width = 45;
        int height = 60;
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        Resources res = getResources();
        
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);   
        setContentView(R.layout.firstpage);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
        
        tabWidget = (TabWidget)findViewById(android.R.id.tabs);
        
        imageButton = (ImageButton)findViewById(R.id.addbutton);
        imageButton.setOnClickListener(imageListener);
        
		final TabHost tabHost = getTabHost(); // The activity TabHost;

		tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			public void onTabChanged(String tabId) {
				// TODO Auto-generated method stub
				for (int i = 0; i < tabWidget.getChildCount(); i++) {
					View vvv = tabWidget.getChildAt(i);
					if (tabHost.getCurrentTab() == i) {
						vvv.setBackgroundDrawable(getResources().getDrawable(R.drawable.focus));
					} else {
						vvv.setBackgroundDrawable(getResources().getDrawable(R.drawable.unfocus));
					}
				}
			}
		});

        TabHost.TabSpec spec;  // Resusable TabSpec for each tab
        Intent intent;  // Reusable Intent for each tab
        Intent mapIntent;

        dbAdpter = new DBAdapter(this);
        dbAdpter.open();
        Cursor cursor = dbAdpter.GetAllHotspots();
        // Create an Intent to launch an Activity for the tab (to be reused)
        if (cursor.getCount() > 0)
        	intent = new Intent().setClass(this, HotspotActivity.class);
        else
        	intent = new Intent().setClass(this, EmptyHotspot.class);

        cursor.close();
        // Initialize a TabSpec for each tab and add it to the TabHost
        spec = tabHost.newTabSpec("station").setIndicator(getString(R.string.staion), res.getDrawable(R.drawable.station)).setContent(intent);
        tabHost.addTab(spec);

        // Do the same for the other tabs
        mapIntent = new Intent().setClass(this, MapsActivity.class);
        mapIntent.putExtra(Constants.DISPLAY_BUTTON, false);
        spec = tabHost.newTabSpec("map").setIndicator(getString(R.string.map),res.getDrawable(R.drawable.map)).setContent(mapIntent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, SetupActivity.class);
        spec = tabHost.newTabSpec("setup").setIndicator(getString(R.string.setup),
                          res.getDrawable(R.drawable.setup))
                      .setContent(intent);
        tabHost.addTab(spec);
        
        intent = new Intent().setClass(this, ExitApp.class);
        spec = tabHost.newTabSpec("exit").setIndicator(getString(R.string.exit), res.getDrawable(R.drawable.exit)).setContent(intent);
        tabHost.addTab(spec);

        tabHost.setCurrentTab(0);
        
		for (int i = 0; i < tabWidget.getChildCount(); i++) {
			/**
			 * set tab height and width
			 */
			tabWidget.getChildAt(i).getLayoutParams().height = height;
			tabWidget.getChildAt(i).getLayoutParams().width = width;

			/**
			 * setup tab title text color
			 */
			final TextView tv = (TextView) tabWidget.getChildAt(i).findViewById(android.R.id.title);
			tv.setTextColor(Color.WHITE);
			View vvv = tabWidget.getChildAt(i);
			if (tabHost.getCurrentTab() == i) {
				vvv.setBackgroundDrawable(getResources().getDrawable(R.drawable.focus));
			} else {
				vvv.setBackgroundDrawable(getResources().getDrawable(R.drawable.unfocus));
			}


			/**
			 * This method can remove white line
			 * 
			 * in TabWidget mBottomLeftStrip��mBottomRightStrip are private member
			 * but we can get it by reflection
			 * 
			 * because Android 2.2, 2.3 interface is different
			 */

			if (Float.valueOf(Build.VERSION.RELEASE.substring(0, 3)) <= 2.1) {
				try {
						mBottomLeftStrip = tabWidget.getClass().getDeclaredField("mBottomLeftStrip");
						mBottomRightStrip = tabWidget.getClass().getDeclaredField("mBottomRightStrip");
						if (!mBottomLeftStrip.isAccessible()) {
							mBottomLeftStrip.setAccessible(true);
						}
						if (!mBottomRightStrip.isAccessible()) {
							mBottomRightStrip.setAccessible(true);
						}
						mBottomLeftStrip.set(tabWidget, getResources().getDrawable(R.drawable.no));
						mBottomRightStrip.set(tabWidget, getResources().getDrawable(R.drawable.no));

					} catch (Exception e) {
						e.printStackTrace();
				}
			} else {

				// if the version is2.2,2.3, you can use abWidget.setStripEnabled(false) to remove white line
				// tabWidget.setStripEnabled(false);

				// if your android version is 2.1,
				// the compiler cannot indentify tabWidget.setStripEnabled(false),but you can still use the reflect
				//to implement, but the code need some changes
				try {
					// 2.2,2.3 interface are mLeftStrip��mRightStrip
						mBottomLeftStrip = tabWidget.getClass().getDeclaredField("mLeftStrip");
						mBottomRightStrip = tabWidget.getClass().getDeclaredField("mRightStrip");
						if (!mBottomLeftStrip.isAccessible()) {
							mBottomLeftStrip.setAccessible(true);
						}
						if (!mBottomRightStrip.isAccessible()) {
							mBottomRightStrip.setAccessible(true);
						}
						mBottomLeftStrip.set(tabWidget, getResources().getDrawable(R.drawable.no));
						mBottomRightStrip.set(tabWidget, getResources().getDrawable(R.drawable.no));

					} catch (Exception e) {
						e.printStackTrace();
				}
			}
		}

        //Intent locServiceIntent = new Intent(this, LocationService.class);
        //startService(locServiceIntent);
        
		//Log.e(Constants.LOG, " " + "cobubServiceIntent");
		
		LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		boolean gpsIsEnbaled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		
		userPrefs = getSharedPreferences(Constants.USER_PREFERENCE, Activity.MODE_WORLD_READABLE);
		boolean IsFirstStart = userPrefs.getBoolean(Constants.PREF_FIRST_START, true);
		if (!gpsIsEnbaled && IsFirstStart){
			util = new Util(this);
			util.GpsDialog();
		}
		
		if (IsFirstStart){
			Editor editor = userPrefs.edit();
			editor.putBoolean(Constants.PREF_FIRST_START, false);
			editor.commit();
		}
		
        //Intent cobubServiceIntent = new Intent(this, CobubService.class);
        //startService(cobubServiceIntent);
        
        
	}
	
	 @Override
	 public void onPause(){
		 super.onPause();
		 imageButton.setBackgroundColor(Color.TRANSPARENT);
	 }
	 
	 @Override
	 public void onResume(){
		 super.onResume();
		 //if (!util.GpsIsOpened()){
			 //return;
		// }
	 }
	
	private OnClickListener imageListener = new OnClickListener(){
		  public void onClick(View v) {
			  // TODO Auto-generated method stub
			  v.setBackgroundColor(Color.RED); 
			  Intent intent = new Intent(getBaseContext(), ConfigureActivity.class);
			  startActivity(intent);
			  finish();
			  dbAdpter.close();
			  Log.v(Constants.LOG, " " + "Add button is clicked!");
			}
	    };
}
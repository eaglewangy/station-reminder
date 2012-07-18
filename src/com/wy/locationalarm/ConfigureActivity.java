package com.wy.locationalarm;

import java.lang.reflect.Field;

import com.wy.locationalarm.R;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.TabHost.OnTabChangeListener;

public class ConfigureActivity extends TabActivity{

	private TabWidget tabWidget;
	Field mBottomLeftStrip;
	Field mBottomRightStrip;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        int width = 45;
        int height = 60;
        
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
        setContentView(R.layout.config_layout);
        
        tabWidget = (TabWidget)findViewById(android.R.id.tabs);
        
        Resources res = getResources(); // Resource object to get Drawables
        
        final TabHost tabHost = getTabHost();  // The activity TabHost
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

        // Create an Intent to launch an Activity for the tab (to be reused)
        intent = new Intent().setClass(this, SubwayActivity.class);

        // Initialize a TabSpec for each tab and add it to the TabHost
        spec = tabHost.newTabSpec("subway").setIndicator(getString(R.string.subway), res.getDrawable(R.drawable.subway)).setContent(intent);
        tabHost.addTab(spec);

        // Do the same for the other tabs
        intent = new Intent().setClass(this, TrainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        spec = tabHost.newTabSpec("train").setIndicator(getString(R.string.train), res.getDrawable(R.drawable.train)).setContent(intent);
        tabHost.addTab(spec);

        mapIntent = new Intent().setClass(this, MapsActivity.class);
        mapIntent.putExtra(Constants.DISPLAY_BUTTON, true);
        spec = tabHost.newTabSpec("map").setIndicator(getString(R.string.map), res.getDrawable(R.drawable.map)).setContent(mapIntent);
        tabHost.addTab(spec);

        tabHost.setCurrentTab(0);
        
        //below is for remove white line
        for (int i = 0; i < tabWidget.getChildCount(); i++) {
			tabWidget.getChildAt(i).getLayoutParams().height = height;
			tabWidget.getChildAt(i).getLayoutParams().width = width;
			
			final TextView tv = (TextView) tabWidget.getChildAt(i).findViewById(android.R.id.title);
			tv.setTextColor(Color.WHITE);
			View vvv = tabWidget.getChildAt(i);
			if (tabHost.getCurrentTab() == i) {
				vvv.setBackgroundDrawable(getResources().getDrawable(R.drawable.focus));
			} else {
				vvv.setBackgroundDrawable(getResources().getDrawable(R.drawable.unfocus));
			}

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
				try {
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

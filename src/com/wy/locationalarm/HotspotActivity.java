package com.wy.locationalarm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.wy.locationalarm.R;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class HotspotActivity extends ListActivity{
	private static final int CONTEXTMENU_DELETEITEM = 0;
	private List<HotspotItem> items;
	private ArrayAdapter<HotspotItem> hotspotAdapter;
	private DBAdapter dbAdpter;
	private ListView listView;
	
	private SharedPreferences userPrefs;
	
	private static final int HANDLERID = 0x101;
	Thread thread = null;
	
	Handler hotspotHandler = new Handler() {  
        public void handleMessage(Message msg) {   
             switch (msg.what) {   
                  case HANDLERID:   
                	  UpdateData();
                	  //Toast.makeText(HotspotActivity.this, "wyylling", Toast.LENGTH_LONG).show();
                       break;   
             }   
             super.handleMessage(msg);   
        }   
   };  
   
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		dbAdpter = new DBAdapter(this);
		dbAdpter.open();
		
		userPrefs = getSharedPreferences(Constants.USER_PREFERENCE, Activity.MODE_WORLD_READABLE);
			
		items = GetItems();
		hotspotAdapter = new HotspotAdapter(this, items);
		setListAdapter(hotspotAdapter);
		
		listView = getListView();
		
		listView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() { 
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) { 
				// TODO Auto-generated method stub 
				menu.setHeaderTitle(getString(R.string.menutitle)); 
				menu.add(ContextMenu.NONE, ContextMenu.NONE, ContextMenu.NONE, getString(R.string.menucontent)); 
			} 
		}); 
		
		thread = new Thread(new MessageThread()); 
		thread.start();
	}
	
	@Override 
	public boolean onContextItemSelected(MenuItem menuItem) { 
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo)menuItem.getMenuInfo(); 
		/* Switch on the ID of the item, to get what the user selected. */ 
		switch (menuItem.getItemId()) { 
			case CONTEXTMENU_DELETEITEM: 
				//Toast.makeText(this, "You selected: " + items.get(menuInfo.position).GetDbId(), Toast.LENGTH_LONG).show();
				dbAdpter.DeleteHotspot(items.get(menuInfo.position).GetDbId());
				items.remove(menuInfo.position);
				hotspotAdapter.notifyDataSetChanged();
				if (items.size() == 0){
					Intent intent = new Intent(getApplicationContext(), MainActivity.class);
					startActivity(intent);
				}
				/* true means: "we handled the event". */ 
				return true; 
		} 
		
		return false; 
	} 
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		// Get the item that was clicked
		//Object o = this.getListAdapter().getItem(position);
		//String keyword = o.toString();
		//Toast.makeText(this, "You selected: " + keyword, Toast.LENGTH_LONG).show();
		//items.remove(position);
		//hotspotAdapter.notifyDataSetChanged();
	}

	private List<HotspotItem> GetItems() {
		List<HotspotItem> list = new ArrayList<HotspotItem>();
		Cursor cursor = dbAdpter.GetAllHotspots();
		
		int _id;
		int type;
		String province;
		String city;
		String line;
		String station;
		int checked;
		double lat;
		double lng;
		
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
		{
			_id = cursor.getInt(Constants.HS0);
			type = cursor.getInt(Constants.HS1);
			province = cursor.getString(Constants.HS2);
			city = cursor.getString(Constants.HS3);
			line = cursor.getString(Constants.HS4);
			station = cursor.getString(Constants.HS5);
			checked = cursor.getInt(Constants.HS6);
			lat = cursor.getDouble(Constants.HS7);
			lng = cursor.getDouble(Constants.HS8);
			list.add(get(_id, type, province, city, line, station, checked, lat, lng));
		}
		
		cursor.close();

		return list;
	}

	private HotspotItem get(int _id, int type, String province, String city, 
			String line, String station, int checked, double lat, double lng) {
		return new HotspotItem(_id, type, province, city, line, 
				station, checked, lat, lng, dbAdpter);
	}
	
	private void UpdateData(){
		String distanceAndSpeed = userPrefs.getString(Constants.PREF_DISTANCE_SPEED, "");
		double radius = Double.valueOf(userPrefs.getString(Constants.PREF_RADIUS, "1000"));
		
		if (distanceAndSpeed == null || distanceAndSpeed.length() == 0)
			return;
		Log.e(Constants.LOG, "UpdateData " + distanceAndSpeed);
		String[] tmp = distanceAndSpeed.split(":");
		List<String> list = new ArrayList<String>();
		list = Arrays.asList(tmp);
		double lat = Double.valueOf(list.get(0));
		double lng = Double.valueOf(list.get(1));
		String speed = list.get(2);
		
		for (int i = 0; i < items.size(); ++i){
			if (!items.get(i).IsSelected())
				continue;
			
			boolean IsInCircle = false;
			double hotspotLat = items.get(i).GetLat();
			double hotspotLng = items.get(i).GetLng();

			float[] results = {0};
			Location.distanceBetween(lat, lng, hotspotLat, hotspotLng, results);
			float dis = results[0];
			if (dis <= radius)
				IsInCircle = true;
			
			dis /= 1000;
			String distance = String.valueOf(dis);
			int pos = distance.indexOf(".");
			if (pos > 0){
				distance = distance.substring(0, pos + 2);
			}
			
			distance += "Km";
			
			Log.e(Constants.LOG, "dis: " + String.valueOf(dis));
			/*if (type == Constants.SUBWAY){
				content = items.get(i).GetCity() + " " + items.get(i).GetLine() + "<br>" + 
							items.get(i).GetStation() + "<br><font size=\"0.1\" color=\"red\">" +getString(R.string.distance) + 
							distance + " " + getString(R.string.speed) + speed + "</font>";
			}
			
			else if (type == Constants.TRAIN){
				content = items.get(i).GetProvince() + " " + items.get(i).GetStation() +
							"<br>" + getString(R.string.distance) + distance + " " + 
							getString(R.string.speed) + speed;
			}
			
			else if (type == Constants.CUSTOM_HOTSPOT){
				content = items.get(i).GetStation() + "<br>" + getString(R.string.distance) + 
				distance + " " + getString(R.string.speed) + speed;
			}*/
			
			String displayInfo = "";
			if (IsInCircle)
				displayInfo = "<font color=\"red\">" + getString(R.string.distance) + 
								distance + " " + getString(R.string.speed) + speed + "</font>";
			else{
				displayInfo = "<font color=\"green\">" + getString(R.string.distance) + 
								distance + " " + getString(R.string.speed) + speed + "</font>";
			}
			
			items.get(i).SetDistanceAndSpeed(displayInfo);
			
			hotspotAdapter.notifyDataSetChanged();
		}
	}
	
	@Override  
	public void onDestroy()  
	{  
		 // TODO Auto-generated method stub  
		hotspotHandler.removeCallbacks(thread);
	    super.onDestroy();  
	}  
	
	class MessageThread implements Runnable {   
        public void run() {  
             while (!Thread.currentThread().isInterrupted()) {    
                  Message message = new Message(); 
                  message.what = HANDLERID;
                  hotspotHandler.sendMessage(message);  

                  try {   
                       Thread.sleep(10000);    
                  } catch (InterruptedException e) {   
                       Thread.currentThread().interrupt();   
                  }   
             }   
        }   
   }   
}

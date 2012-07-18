package com.wy.locationalarm.util;

import com.wy.locationalarm.R;
import com.wy.locationalarm.Constants;
import com.wy.locationalarm.DBAdapter;
import com.wy.locationalarm.MainActivity;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;
import android.app.Activity;

public class Util{
	private Context context;
	
	private NotificationManager notificationManager;
	private SharedPreferences userPrefs;
	
	private DBAdapter dbAdapter;
	private Cursor dbCursor;
	private boolean IsInCircle;
	
	private long TEN_SECONDS = 10 * 1000;
	private long HALF_MINUTE = 30 * 1000;
	private long ONE_HOUR = 3600 * 1000;
	
	public Util(Context context)
	{
		this.context = context;
		notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		dbAdapter = new DBAdapter(context);
		dbAdapter.open();
		
        userPrefs = context.getSharedPreferences(Constants.USER_PREFERENCE, Activity.MODE_WORLD_READABLE);
        
        IsInCircle = false;
	}
	
	public boolean GpsDialog()
	{
    	AlertDialog.Builder builder = new AlertDialog.Builder(context);
    	builder.setTitle("Setup Error");
    	builder.setMessage("GPS functionality is not open, please open GPS and then to use this feature")
    	       .setCancelable(false)
    	       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	        	   dialog.dismiss();
    	        	   ((Activity) context).startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
    	           }
    	       });
    	AlertDialog alert = builder.create();
    	builder.show();
		return false;
	}
	
	public boolean IsCallAlarm(Location location)
	{
		if (location == null){
			Log.d(Constants.LOG, "Location is None");
			return false;
		}

		float speedMs = location.getSpeed();
		double speedKms = speedMs * 3.6;
		String s = String.valueOf(speedKms);
		int pos = s.indexOf(".");
		s = s.substring(0, pos + 2);
		s += "Km/h";
		String distanceAndSpeed = String.valueOf(location.getLatitude()) + ":" + 
								  String.valueOf(location.getLongitude()) + ":" + s;
			
		Editor editor = userPrefs.edit();
  	  	editor.putString(Constants.PREF_DISTANCE_SPEED, distanceAndSpeed);
  	  	
		String radiusStr = userPrefs.getString(Constants.PREF_RADIUS, "1000");
		int radius = Integer.parseInt(radiusStr);
		
		dbCursor = dbAdapter.GetAllHotspots();
		
		int checked = 0;
		long minSpeed = Constants.SUBWAY_SPEED;
		long sleepTime = TEN_SECONDS;
		float minDistance = Integer.MAX_VALUE;
		
		for (dbCursor.moveToFirst(); !dbCursor.isAfterLast(); dbCursor.moveToNext())
		{
			checked = dbCursor.getInt(Constants.HS6);
			
			//this hotspot is disabled.
			//Log.v(Constants.LOG, "IsCallAlarm: " + String.valueOf(checked));
			
			double lat = dbCursor.getDouble(Constants.HS7);
			double lng = dbCursor.getDouble(Constants.HS8);
			Location loc = new Location(LocationManager.GPS_PROVIDER);
			loc.setLatitude(lat);
			loc.setLongitude(lng);
			float dis = loc.distanceTo(location);
			
			String info = "Current location: " + "Lat: "
			+ String.valueOf(loc.getLatitude()) + " Longtitude: "
			+ String.valueOf(loc.getLongitude());
			info += "\n";
			info += "Distance: " + String.valueOf(dis);
			Log.e(Constants.LOG, info);
			//Toast.makeText(context, info, Toast.LENGTH_LONG).show();
			
			if (dis < minDistance){
				minDistance = dis;
				sleepTime = (long) (minDistance/minSpeed) * 1000;
				
				if (sleepTime < TEN_SECONDS)
					sleepTime = TEN_SECONDS * 2;
				else if (sleepTime > ONE_HOUR)
					sleepTime = ONE_HOUR * 2;
			
				//Toast.makeText(context, Long.toString(sleepTime/2), Toast.LENGTH_LONG).show();
				editor.putLong(Constants.PREF_SLEEP_TIME, sleepTime/2);
			}
			
			if (dis <= radius && checked == 1) {
				if (IsInCircle){
					return false;
				}
				else{
					IsInCircle = true;
					return true;
				}	
			}
		}
		
		String utilInfo = "In IsCallAlarm(): Min distance:" + Double.toString(minDistance) + "\n" +
			"Sleep time(s): " + Long.toString(sleepTime/2000);
		Toast.makeText(context, utilInfo, Toast.LENGTH_LONG).show();
		
		editor.commit();

		IsInCircle = false;
		
		return false;
	}
	
	public void DisplayNotafication() {
		//Context context = getApplication();
		// Text to display in the status bar when the notification is launched
		String tickerText = context.getString(R.string.notification);
		// The extended status bar orders notification in time order
		long when = System.currentTimeMillis();
		Notification notification = new Notification(R.drawable.alarm, tickerText, when);
		RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.note);
		// contentView.setImageViewResource(R.id.image, R.drawable.chat);
		contentView.setTextViewText(R.id.status_text, "");
		notification.contentView = contentView;
		
		userPrefs = context.getSharedPreferences(Constants.USER_PREFERENCE, Activity.MODE_WORLD_READABLE);
		//Uri ringURI = Uri.parse("android.resource://com.autodesk.locationalarm/raw/igotalove");
		//String defaultUri = "android.resource://com.autodesk.locationalarm/raw/igotalove";
		Uri ringURI = Uri.parse(userPrefs.getString(Constants.PERF_RINGTONE_URI, Constants.PERF_DEFALUT_RINGTONE_URI));
		notification.sound = ringURI;
	
		boolean isVibrate = false;
		isVibrate = userPrefs.getBoolean(Constants.PREF_VIBRATE, false);
		if (isVibrate)
		{
			long[] vibrate = new long[] { 1000, 1000, 1000, 1000, 1000 };
			notification.vibrate = vibrate;
		}
		
		notification.flags |= Notification.FLAG_INSISTENT;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		
		// Text to display in the extended status window
		String expandedText = context.getString(R.string.notification);
		// Title for the expanded status
		String expandedTitle = "Location Alarm";

		// Intent to launch an activity when the extended text is clicked
		Intent intent = new Intent(context, MainActivity.class);
		//intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent launchIntent = PendingIntent.getActivity(context, 0, intent, 0);
		notification.setLatestEventInfo(context, expandedTitle, expandedText, launchIntent);

		int notificationRef = 1;
		notificationManager.notify(notificationRef, notification);
	}
}

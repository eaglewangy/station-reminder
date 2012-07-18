package com.wy.locationalarm;

import java.io.IOException;
import java.util.ArrayList;

import com.wy.locationalarm.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

public class SetupActivity extends ListActivity {

	private SharedPreferences setupPrefs;

	private ArrayList<SetupItem> listItems;
	private ListView listView;
	
	private SetupAdapter itemAdapter; 
	
	private RingtoneManager ringtoneManager;
	private MediaPlayer mediaPlayer;
	
	//save ringtone checked item position
	private int checkedItem;
	private String currentRingtone;
	private String ringtoneUri;
	  
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);
        
        listView = this.getListView();
        ringtoneManager = new RingtoneManager(this);

        listItems = new ArrayList<SetupItem>();
        int resID = R.layout.listitem;
        itemAdapter = new SetupAdapter(this, resID, listItems);
        listView.setAdapter(itemAdapter);
    
        init();
        
        setupPrefs = getSharedPreferences(Constants.USER_PREFERENCE, Activity.MODE_WORLD_READABLE);
        checkedItem = setupPrefs.getInt(Constants.PERF_RINGTONE_CHECKEDITEM, -1);
        currentRingtone = setupPrefs.getString(Constants.PERF_CURRENT_RINGTONE, getString(R.string.defaultringtone));
        ringtoneUri = setupPrefs.getString(Constants.PERF_RINGTONE_URI, Constants.PERF_DEFALUT_RINGTONE_URI);
        
        UpdateUIFromPreferences();
		
		//DisplayNotafication();
    }
    
    @Override
    protected void onListItemClick(ListView lv, View v, int position, long id) {
      populateData(position) ;
      return;
	}
    
    private void populateData(int position) 
    {  
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	AlertDialog alertDialog;
  	  	switch(position)
  	  	{
  	  	case 0:
  	  		builder.setTitle(getString(R.string.startvibrate))
  	  		.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() 
	    	{
	    		public void onClick(DialogInterface dialog, int id)
	    		{
	    			String s = getString(R.string.no);
  	  				
  	  				listItems.get(Constants.VIBRATE_IN_LIST).SetValue(s);
  	  				itemAdapter.notifyDataSetChanged();
	    			dialog.cancel();
	    		}
	    	})
	    	.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
  	  			public void onClick(DialogInterface dialog, int id) {
  	  				String s = getString(R.string.yes);
  	  				
  	  				listItems.get(Constants.VIBRATE_IN_LIST).SetValue(s);
  	  				itemAdapter.notifyDataSetChanged();
  	  				dialog.dismiss();
  	  			}
  	  		});
  	  		
  	  		alertDialog = builder.create();
	  		builder.show();
  	  		break;
	  	//radius
  	  	case 1:
  	  		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
  	  		View layout = inflater.inflate(R.layout.radius_dialog, (ViewGroup) findViewById(R.id.layout_radius));

  	  		final EditText radius = (EditText)layout.findViewById(R.id.radius);
  	  		radius.setText(listItems.get(Constants.RADIUS_IN_LIST).GetValue());

  	  		builder = new AlertDialog.Builder(this);
  	  		builder.setView(layout);
  	  		builder.setMessage(getString(R.string.alarmrange))
  	  		.setCancelable(true)
  	  		.setPositiveButton(getString(R.string.makesure), new DialogInterface.OnClickListener() {
  	  			public void onClick(DialogInterface dialog, int id) {
  	  				String s = radius.getText().toString();
  	  				//minimum
  	  				if (s.length() < 2)
  	  					s = "25";
  	  				//radius max length is 8 digits
  	  				if (s.length() > 8)
  	  					s = s.substring(0, 8);
  	  				
  	  				listItems.get(Constants.RADIUS_IN_LIST).SetValue(s);
  	  				itemAdapter.notifyDataSetChanged();
  	  				dialog.dismiss();
  	  			}
  	  		})
  	  		.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
  	  			public void onClick(DialogInterface dialog, int id) {
                 dialog.cancel();
            }
  	  		});
  	  		alertDialog = builder.create();
  	  		builder.show();
  	  		break;
	    //ringtone		
  	    case 2:
  	    	final Cursor ringCursor = ringtoneManager.getCursor();
  	    	
  	    	builder.setTitle(getString(R.string.ringtone))
  	    	.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() 
  	    	{
  	    		public void onClick(DialogInterface dialog, int id)
  	    		{
  	    			ringCursor.close();
  	    			StopRingtone();
  	    			
  	    			dialog.cancel();
  	    		}
  	    	});
  	    	
  	    	builder.setPositiveButton(getString(R.string.makesure), new DialogInterface.OnClickListener() {
  	  			public void onClick(DialogInterface dialog, int id) {
  	  				listItems.get(Constants.RINGTONE_IN_LIST).SetValue(currentRingtone);
  	    			itemAdapter.notifyDataSetChanged();
  	    			ringCursor.close();
  	    			StopRingtone();
  	    			dialog.cancel();
  	  			}
  	  		});
  		
  	    	//ListView listView = (ListView) dialog.findViewById(android.R.id.list); 
  	    	builder.setSingleChoiceItems(ringCursor, checkedItem, "title", new DialogInterface.OnClickListener() 
  	    	{
  	    		public void onClick(DialogInterface dialog, int position) 
  	    		{
  	    			if (checkedItem == position)
  	    				return;
  	    			
  	    			StopRingtone();
  	    			
  	    			mediaPlayer = new MediaPlayer();
  	    			//ringtone colume is: _id, title, and "content://......"
  	    			ringCursor.moveToPosition(position);
  	    			checkedItem = position;
  	    			//title column index is 1
  	    			currentRingtone = ringCursor.getString(1);
  	    			ringtoneUri = ringtoneManager.getRingtoneUri(position).toString();
  	    			
  	    			try {
  	    				//mediaPlayer.reset();
						mediaPlayer.setDataSource(getApplicationContext(), ringtoneManager.getRingtoneUri(position));
						final AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
	  	    			if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
	  	    				mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
	  	    				mediaPlayer.setLooping(true);
	  	    				mediaPlayer.prepare();
	  	    				mediaPlayer.start();
	  	    			  }
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						Log.v(Constants.LOG, " " + "SetupActivity media play failed");
					} catch (SecurityException e) {
						// TODO Auto-generated catch block
						Log.v(Constants.LOG, " " + "SetupActivity media play failed");
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						Log.v(Constants.LOG, " " + "SetupActivity media play failed");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						Log.v(Constants.LOG, " " + "SetupActivity media play failed");
					}
  	    		}
  	    	});
  	    	
  	    	alertDialog = builder.create();
  	    	builder.show();
  	    	break;
  	  		}
  	  		
  	}
    
    private void init()
    {
    	//vibrate
        SetupItem vibrateItem = new SetupItem(getString(R.string.vibration), getString(R.string.no));
        listItems.add(vibrateItem);
        //radius
        SetupItem radiusItem = new SetupItem(getString(R.string.chooseradius), "1000");
        listItems.add(radiusItem);
      //ringtone
        SetupItem ringItem = new SetupItem("Notification ringtone", "");
        listItems.add(ringItem);
    }
    
    private void UpdateUIFromPreferences()
    {
    	boolean vibrate = setupPrefs.getBoolean(Constants.PREF_VIBRATE, false);
    	if (vibrate)
    		listItems.get(Constants.VIBRATE_IN_LIST).SetValue(getString(R.string.yes));
    	else
    		listItems.get(Constants.VIBRATE_IN_LIST).SetValue(getString(R.string.no));
    	
    	listItems.get(Constants.RADIUS_IN_LIST).SetValue(setupPrefs.getString(Constants.PREF_RADIUS, "1000"));
    	listItems.get(Constants.RINGTONE_IN_LIST).SetValue(setupPrefs.getString(Constants.PREF_RINGTONE, currentRingtone));
    	itemAdapter.notifyDataSetChanged();
    }
    
    private void SavePreferences()
    {
    	boolean vibrate = false;
  	  	String vibrateStr  = listItems.get(Constants.VIBRATE_IN_LIST).GetValue();
  	  	if (vibrateStr.equals(getString(R.string.yes)))
  	  		vibrate = true;
  	  	
  	  	String radius  = listItems.get(Constants.RADIUS_IN_LIST).GetValue();
  	  	String ringtone  = listItems.get(Constants.RINGTONE_IN_LIST).GetValue();

  	  	Editor editor = setupPrefs.edit();
  	  	editor.putBoolean(Constants.PREF_VIBRATE, vibrate);
  	  	editor.putString(Constants.PREF_RADIUS, radius);
  	  	editor.putString(Constants.PREF_RINGTONE, ringtone);
  	  	editor.putInt(Constants.PERF_RINGTONE_CHECKEDITEM, checkedItem);
  	  	editor.putString(Constants.PERF_CURRENT_RINGTONE, currentRingtone);
  	  	editor.putString(Constants.PERF_RINGTONE_URI, ringtoneUri);

  	  	editor.commit();
    }
    
    private void StopRingtone(){
    	if (mediaPlayer != null)
    	{
    		if (mediaPlayer.isPlaying())
    			mediaPlayer.stop();
    		
    		mediaPlayer.release();
    		mediaPlayer = null;
    	}
    }
    
    @Override
    public void onPause()
    {
    	super.onPause();
    	SavePreferences();
    }
    
    @Override
    public void onDestroy() 
    {      
    	super.onDestroy();
    }
    
    @Override
    protected void onStop()
    {
    	super.onStop();
    	StopRingtone();
    }

}
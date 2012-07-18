package com.wy.locationalarm;

import com.wy.locationalarm.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;

public class ExitApp extends Activity{
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Exit();
	}
        
	private void Exit()
    {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage(getString(R.string.exitapp))
    	       .setCancelable(true)
    	       .setPositiveButton(getString(R.string.makesure), new DialogInterface.OnClickListener() 
    	       {
    	           public void onClick(DialogInterface dialog, int id) 
    	    	   {
    	        	   SharedPreferences userPrefs = getSharedPreferences(Constants.USER_PREFERENCE, Activity.MODE_WORLD_READABLE);
    	       		   Editor editor = userPrefs.edit();
    	       		   editor.putBoolean(Constants.PREF_FIRST_START, true);
    	       		   editor.commit();
    	       		
    	       		   stopService(new Intent(ExitApp.this, DummyService.class));
    	    		   Intent exit = new Intent(Intent.ACTION_MAIN);
    	    		   exit.addCategory(Intent.CATEGORY_HOME);
    	    		   exit.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	    		   startActivity(exit);
    	    		   System.exit(0);
    	           }
    	       })
    	       .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
    	    	   public void onClick(DialogInterface dialog, int id) {
    	    		   dialog.cancel();
    	    		   finish();
    	    		   Intent exit = new Intent(ExitApp.this, MainActivity.class);
    	    		   startActivity(exit);
    	    	   }
    	       });
    	AlertDialog alert = builder.create();
    	builder.show();
    }
}

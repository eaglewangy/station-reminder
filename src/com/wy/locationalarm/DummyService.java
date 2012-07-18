package com.wy.locationalarm;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

//Baidu API only run in the main thread, to avoid the app is killed by OS
// we should keep our app's in memory. so we start a service to guarantee that.
public class DummyService extends Service{

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
}

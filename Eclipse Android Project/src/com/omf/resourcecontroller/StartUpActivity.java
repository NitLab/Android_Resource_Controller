package com.omf.resourcecontroller;

import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ToggleButton;
//import android.view.Menu;

public class StartUpActivity extends Activity {
	
	public static final String TAG = "StartUpActivity";
	
	
	private ToggleButton toggleService = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_up);
		
		toggleService = (ToggleButton)findViewById(R.id.ToggleBtnServ);
		toggleService.setOnClickListener(toggleListener);
	}

	/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_start_up, menu);
		return true;
	}*/

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(TAG,"onDestroy");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG,"onResume");
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.i(TAG,"onStart");
		
		if(isServiceRunning(".BackgroundService")){
			toggleService.setChecked(true);
		}else{
			toggleService.setChecked(false);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.i(TAG,"onStop");
	}
	
	/**
	 * LISTENER FUNCTIONS
	 */
	View.OnClickListener toggleListener = new View.OnClickListener(){
		@Override
		public void onClick(View v) {
			
			Intent intent = new Intent(StartUpActivity.this, BackgroundService.class);
			intent.addFlags(Service.START_STICKY);
			intent.addFlags(Service.BIND_AUTO_CREATE);
			
			if(toggleService.isChecked() && !isServiceRunning(".BackgroundService")){
				Log.i(TAG, "Start Service");
				startService(intent);
			}else {
				Log.i(TAG, "Stop Service");
				stopService(intent);
			}
			
		}
	};
	
	// --- SERVICE CHECK CONTROL USING THE SYSTMEM
			// Check if the service is running
			public boolean isServiceRunning(String serviceName) {
				boolean serviceRunning = false;
				ActivityManager am = (ActivityManager) StartUpActivity.this.getSystemService(ACTIVITY_SERVICE);
				List<ActivityManager.RunningServiceInfo> l = am.getRunningServices(50);
				Iterator<ActivityManager.RunningServiceInfo> i = l.iterator();
				while (i.hasNext()) {
					ActivityManager.RunningServiceInfo runningServiceInfo = (ActivityManager.RunningServiceInfo) i.next();
					if (runningServiceInfo.service.getShortClassName().equals(serviceName)) {
						serviceRunning = true;
					}
				}
				return serviceRunning;
			}

}

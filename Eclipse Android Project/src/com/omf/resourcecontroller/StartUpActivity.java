/* Copyright (c) 2013 NITLab, University of Thessaly, Greece
 * This software may be used and distributed solely under the terms of the MIT license (License).
 * You should find a copy of the License in LICENSE.TXT or at http://opensource.org/licenses/MIT.
 * By downloading or using this software you accept the terms and the liability disclaimer in the License.
*/

package com.omf.resourcecontroller;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ToggleButton;

/**
 * Start up activity
 * @author Polychronis Symeonidis
 *
 */
public class StartUpActivity extends Activity {
	public static final String appTAG = "com.omf.resourcecontroller";
	public static final String classTAG = "StartUpActivity";
	//public static int loggerPid = -1;
	private ToggleButton toggleService = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(appTAG,classTAG+": onCreate");
		//On create execute the log command and kill on destroy...
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
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.settings:
	        	 // START THE PongActivity
	            Intent startActivity = new Intent(this, SettingsActivity.class);
	            startActivity(startActivity);
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(appTAG,classTAG+": onDestroy");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(appTAG,classTAG+": onResume");
	}
	
	protected void onPause() {
		super.onPause();
		Log.i(appTAG,classTAG+": onPause");
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.i(appTAG,classTAG+": onStart");
		
		if(isServiceRunning("com.omf.resourcecontroller.BackgroundService")){
			toggleService.setChecked(true);
		}else{
			toggleService.setChecked(false);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.i(appTAG,classTAG+": onStop");
	}
	
	/**
	 * LISTENER FUNCTIONS
	 */
	View.OnClickListener toggleListener = new View.OnClickListener(){
		@Override
		public void onClick(View v) {
			
			
				Intent intent1 = new Intent(StartUpActivity.this, BackgroundService.class);
				intent1.addFlags(Service.START_STICKY);
				intent1.addFlags(Service.BIND_AUTO_CREATE);
				
				Intent loggerService = new Intent(StartUpActivity.this, LoggerService.class);
				loggerService.addFlags(Service.START_STICKY);
				loggerService.addFlags(Service.BIND_AUTO_CREATE);
				
				if(toggleService.isChecked() && !isServiceRunning("com.omf.resourcecontroller.BackgroundService")){
						Log.i(appTAG, classTAG+": Start Service");
						startService(loggerService);
						startService(intent1);
						
				}else {
					Log.i(appTAG, classTAG+": Stop Service");
					stopService(intent1);
					stopService(loggerService);
				}
			
		}
	};
	
	
	
	// --- SERVICE CHECK CONTROL USING THE SYSTMEM
			// Check if the service is running
	public boolean isServiceRunning(String serviceName) {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (serviceName.equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
	
	// --- Get pid of a process
	
	public int applicationPid(String applicationName) 
	{
		ActivityManager am = (ActivityManager)getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
	    List<ActivityManager.RunningAppProcessInfo> pids = am.getRunningAppProcesses();
	             int processid = 0;
	       for(int i = 0; i < pids.size(); i++)
	       {
	           ActivityManager.RunningAppProcessInfo info = pids.get(i);
	           if(info.processName.equalsIgnoreCase(applicationName)){
	              processid = info.pid;
	           } 
	       }
	    Log.i(appTAG, classTAG+": OMF RC Pid: " + processid);
		return processid;
	}

}

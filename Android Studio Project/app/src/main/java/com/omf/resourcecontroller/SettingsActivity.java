/* Copyright (c) 2013 NITLab, University of Thessaly, Greece
 * This software may be used and distributed solely under the terms of the MIT license (License).
 * You should find a copy of the License in LICENSE.TXT or at http://opensource.org/licenses/MIT.
 * By downloading or using this software you accept the terms and the liability disclaimer in the License.
*/

package com.omf.resourcecontroller;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

/**
 * Settings Activity
 * @author Polychronis Symeonidis
 *
 */
public class SettingsActivity extends Activity implements Constants{
	public static final String appTAG = "omf.resourcecontroller";
	public static final String classTAG = "SettingsActivity";
	private EditText usernameField;
	private EditText serverField;
	private Button submitBtn;
	private String username;
	private String server;
	private int selectedRadioId;
	private RadioButton radioSelected;
	private RadioGroup connectionGroup;
	private CheckBox logCheckBox;
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		//RadioGroup
		connectionGroup = (RadioGroup)findViewById(R.id.radioGroup1);
		
		
		//Checkbox? checkBox1
		logCheckBox = (CheckBox)findViewById(R.id.checkBox1);
		
		//Connections settings fields
		submitBtn = (Button)findViewById(R.id.button1);
		usernameField   = (EditText)findViewById(R.id.editText1);
		serverField   = (EditText)findViewById(R.id.editText2);
		
		
		
		submitBtn.setOnClickListener(
		        new View.OnClickListener()
		        {
		            public void onClick(View view)
		            {   
		                //when submit is pressed stopService intent --- store values of username and server to DB --- start service again -- setContentView
		                Intent intent = new Intent(SettingsActivity.this, BackgroundService.class);
						intent.addFlags(Service.START_STICKY);
						intent.addFlags(Service.BIND_AUTO_CREATE);
						
						selectedRadioId = connectionGroup.getCheckedRadioButtonId();
						radioSelected = (RadioButton) findViewById(selectedRadioId);
						
						SharedPreferences settings = getSharedPreferences("ConnectionSettings", Context.MODE_PRIVATE);

						SharedPreferences.Editor editor = settings.edit();
						
						
						if(!(usernameField.getText().length() == 0) || !usernameField.getText().toString().equalsIgnoreCase(""))
		            	{
		            		username = usernameField.getText().toString();
		            		 Log.e("Username", username);
		            		 editor.putString("username", username);
		            	}
						else
						{
							Log.e(appTAG, classTAG+": Username text box empty");
							//Log.i(TAG,"Default_server: " + server);
						}
		            	
		            	
		            	if(!(serverField.getText().length() == 0) || !serverField.getText().toString().equalsIgnoreCase(""))
		            	{
		            		server = serverField.getText().toString();
		            		Log.e("Server", server);
		            		editor.putString("server", server);
		            	}
		            	else
		            	{
		            		Log.e(appTAG, classTAG+": Server text box empty");
		            		server = DEFAULT_SERVER;
		            		Log.i(appTAG,classTAG+": Default server chosen: " + server);
		            		//editor.putString("server", server);
		            	}
		            	
		            	
		            	if(logCheckBox.isChecked())
		            	{
		            		Log.i(appTAG, classTAG+": Log is active");
		            		editor.putBoolean("log", true);
		            	}
		            	else
		            	{
		            		Log.i(appTAG, classTAG+": Log is inactive");
		            		editor.putBoolean("log", false);
		            	}
						
		            	//ConnectionType Shared preference
		            	Log.e(appTAG,classTAG+": CONNECTION: "+radioSelected.getText().toString());
		            	editor.putString("connectionType", radioSelected.getText().toString());
		            	
						//Commit to shared preferences
						editor.commit();
						
						//If service is running disconnect and connect using the new credentials
						if(isServiceRunning("com.omf.resourcecontroller.BackgroundService")){
							stopService(intent);
							Log.i(appTAG,classTAG+": Restarting service with new settings");
							startService(intent);
						}
						finish();
		                //setContentView(R.layout.activity_start_up);
		            }
		        });
		
		
	}
	
	
	protected void onDestroy() {
		super.onDestroy();
		Log.i(appTAG,classTAG+": onDestroy");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(appTAG,classTAG+": onResume");
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.i(appTAG,classTAG+": onStart");
	}
	
	public boolean isServiceRunning(String serviceName) {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (serviceName.equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
}

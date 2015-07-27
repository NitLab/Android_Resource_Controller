package com.omf.resourcecontroller;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.omf.resourcecontroller.OMF.RegularExpression;

public class LoggerService extends Service{
	public static final String appTAG = "omf.resourcecontroller";
	public static final String classTAG = "LoggerService";
	 //private StringBuilder log; 
	 RegularExpression regEx = null;
	 Process process = null;
	 String loggerPid = null;
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void onCreate() {
		super.onCreate();
		regEx = new RegularExpression();
	}
	
	@SuppressWarnings("deprecation")
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		try {  
			String fileName = "logcat_omf_rc_"+System.currentTimeMillis()+".txt";
			File outputFile = new File(getExternalCacheDir(),fileName);
			Log.i(appTAG, classTAG+": File Path: "+outputFile.getAbsolutePath());
			//String logCommand = "logcat -d | grep " + applicationPid("com.omf.resourcecontroller");
			String logCommand = "logcat -f "+outputFile.getAbsolutePath()+" "+appTAG+":W"+appTAG+":E SMACK System.err XMPPConnection:W XMPPConnection:E *:S";
			Log.i(appTAG, classTAG+": Command: "+logCommand);
			process = Runtime.getRuntime().exec(logCommand);
			
			
		  } catch (IOException e) {  
		  } 
	}
	
	public void onDestroy() {
		super.onDestroy();
		loggerPid = regEx.pidReg(process.toString());
		Log.i(appTAG,classTAG+": Kill procces:" +loggerPid);
		
		try {
			Runtime.getRuntime().exec("su -c kill -9 "+loggerPid);
			Log.w(appTAG,classTAG+": Executing command: "+"su -c kill -9 "+loggerPid);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	    Log.i(appTAG,classTAG+": OMF RC Pid: " + processid);
		return processid;
	}
}

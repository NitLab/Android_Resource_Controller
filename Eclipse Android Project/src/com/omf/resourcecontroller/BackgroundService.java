/* Copyright (c) 2013 NITLab, University of Thessaly, Greece
 * This software may be used and distributed solely under the terms of the MIT license (License).
 * You should find a copy of the License in LICENSE.TXT or at http://opensource.org/licenses/MIT.
 * By downloading or using this software you accept the terms and the liability disclaimer in the License.
*/

package com.omf.resourcecontroller;



import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.jivesoftware.smack.XMPPConnection;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.os.StrictMode;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.omf.resourcecontroller.OMF.OMFMessage;
import com.omf.resourcecontroller.OMF.XMPPClass;
/**
 * Background service 
 * @author Polychronis Symeonidis
 *
 */
public class BackgroundService extends Service implements Constants{

	public static final String TAG = "BackgroundService";
	
	// Service variables
	private NotificationManager notificationMgr = null;		//	NOTIFICATION MANAGER
	private TelephonyManager  telephonyMgr = null;
	
	// XMPP variables
	private XMPPClass test = null;
	//OMF message object
	OMFMessage omfMessage = null;
	
	//Username & password
	private String UnamePass = null;
	
	//TopicName
	private String topicName = null;
	
	//ServerName
	private String serverName = null;
	 
	private XMPPConnection xmpp = null; 
	 
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	

	@Override
	public void onCreate() {
		super.onCreate();
		
		//////////////	INVOKE SERVICES
		
		// Notification manager Service
		notificationMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		// TelephonyMgr
		telephonyMgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		
		
		
		//topicName = telephonyMgr.getDeviceId();
		
		 if (telephonyMgr.getDeviceId() != null)
			 topicName = telephonyMgr.getDeviceId(); //*** use for mobiles
		 else
		 {
		     //topicName = Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID); //*** use for tablets
		 
			try 
			{
				Class<?> c = Class.forName("android.os.SystemProperties");
				Method get = c.getMethod("get", String.class);
				topicName = (String) get.invoke(c, "ro.serialno");
			} catch (Exception ignored) {
				
			}
		 }

		//Get saved settings if they exist otherwise start connection with default values
		SharedPreferences settings = getSharedPreferences("XMPPSettings", Context.MODE_PRIVATE);
		UnamePass = settings.getString("username", "nitos.android."+topicName);	//if shared preference does not exist return default value android.omf.IMEI
		serverName = settings.getString("server", DEFAULT_SERVER);
		
		Log.i(TAG,"Username: " + UnamePass);
		Log.i(TAG,"Server: " + serverName);
		
		UnamePass = UnamePass.toLowerCase(Locale.ENGLISH);	//XMPP openfire server does not support Upper case characters for accounts 
		serverName = serverName.toLowerCase(Locale.ENGLISH);	//But supports upper case characters for topic names so we lower the case to have the same name for everything
		
		Log.i(TAG,"Lowercase Username: " + UnamePass);
		Log.i(TAG,"Lowercase Server: " + serverName);
		
		/////////////	THREAD POLICY
		
		// Allow the connection to be established in the main thread
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		
		// XMPP CONNECTION
		
		test = new XMPPClass(UnamePass, UnamePass, UnamePass, serverName, getApplicationContext());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		// CLOSE CONNECTION
		if(test != null && xmpp!=null)
			test.destroyConnection();
		
		test = null;
		
		displayNotificationMessage("XMPP stopped");
		
		Log.i(TAG,"XMPP stopped");
	}

	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		//super.onstar
		//super.handleStart(intent, startId);
		//If internet exists start connection else display error message
		if(isNetworkAvailable()){
			xmpp = test.XMPPCreateConnection();
			if(xmpp!=null){
				displayNotificationMessage("XMPP started");
				Log.i(TAG,"XMPP started");
			}
			else{
				displayNotificationMessage("Check server name and uid!");
				Log.e(TAG,"Connection failed");
				Log.e(TAG,"Check server and uid");
			}
		}
		else
		{
			displayNotificationMessage("Check internet connectivity");
			Log.e(TAG,"Internet connection unavailable");
		}
		
		
	}
	
	
	/**
	 * Check if an internet connection exists
	 * 
	 * @return true if the internet exists
	 */
	//--Check if device has an internet connection
		private boolean isNetworkAvailable() {
		    ConnectivityManager connectivityManager 
		          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
		}
	

	// --- SERVICE CHECK CONTROL USING THE SYSTMEM
	/**
	 * Check is the given service is running
	 * @param serviceName : String
	 * @return true if the service is running
	 */
	public boolean isServiceRunning(String serviceName) {
		boolean serviceRunning = false;
		ActivityManager am = (ActivityManager) BackgroundService.this.getSystemService(ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> l = am.getRunningServices(50);
		Iterator<ActivityManager.RunningServiceInfo> i = l.iterator();
		while (i.hasNext()) {
			ActivityManager.RunningServiceInfo runningServiceInfo = (ActivityManager.RunningServiceInfo) i.next();
			if (runningServiceInfo.service.getClassName().equals(serviceName)) {
				serviceRunning = true;
			}
		}
		return serviceRunning;
	}


	/**
	 * Displays a notification message on the device screen
	 * @param message : the message to be displayed as a notification
	 */
	private void displayNotificationMessage(String message) {
		Notification notify = new Notification(android.R.drawable.stat_notify_chat, message, System.currentTimeMillis());
		notify.flags = Notification.FLAG_AUTO_CANCEL;
		notify.icon = R.drawable.nitlab_n_app_logo;
		
		// The service is not running
		//if(!isServiceRunning(("." + BackgroundService.class.getSimpleName()).trim())){
		if(!isServiceRunning("com.omf.resourcecontroller.BackgroundService")){
			Intent start = new Intent();
			start.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
			// Notification that does not redirect to other Activities
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, start, PendingIntent.FLAG_UPDATE_CURRENT);
			notify.setLatestEventInfo(BackgroundService.this, "Resource Controller", message, contentIntent);
			notificationMgr.notify(R.string.app_notification_id, notify);
		}else{	// The service is running
			Intent start = new Intent(BackgroundService.this, StartUpActivity.class);
			start.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			// Notification that redirects to another Activity
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, start, PendingIntent.FLAG_UPDATE_CURRENT);
			notify.setLatestEventInfo(this, "Resource Controller", message, contentIntent);
			
			notificationMgr.notify(R.string.app_notification_id, notify);
		}
	}	
}

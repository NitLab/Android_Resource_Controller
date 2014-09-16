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
import android.widget.Toast;

import com.omf.resourcecontroller.OMF.OMFMessage;
import com.omf.resourcecontroller.OMF.XMPPCommunicator;
/**
 * Background service 
 * @author Polychronis Symeonidis
 *
 */
public class BackgroundService extends Service implements Constants{

	public static final String appTAG = "com.omf.resourcecontroller";
	public static final String classTAG = "BackgroundService";
	
	// Service variables
	private NotificationManager notificationMgr = null;		//	NOTIFICATION MANAGER
	private TelephonyManager  telephonyMgr = null;
	
	// XMPP variables
	private XMPPCommunicator XMPPComm = null;
	//OMF message object
	OMFMessage omfMessage = null;
	
	//Username & password
	private String UnamePass = null;
	
	//TopicName
	private String topicName = null;
	
	//ServerName
	private String serverName = null;
	//Connection Type (XMPP or AMQP)
	
	private String connType = null;
	
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
		
		//Toasts
		
		
		
		
		
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
		SharedPreferences settings = getSharedPreferences("ConnectionSettings", Context.MODE_PRIVATE);
		UnamePass = settings.getString("username", "nitos.android."+topicName);	//if shared preference does not exist return default value android.omf.IMEI
		serverName = settings.getString("server", DEFAULT_SERVER);//Default server = XMPP nitlab.inf.uth.gr
		connType = settings.getString("connectionType", "XMPP"); //Default Connection type = XMPP
		
		Log.i(appTAG,classTAG+": Username: " + UnamePass);
		Log.i(appTAG,classTAG+": Server: " + serverName);
		Log.i(appTAG,classTAG+": ConnectionType: " + connType);
		
		UnamePass = UnamePass.toLowerCase(Locale.ENGLISH);	//XMPP openfire server does not support Upper case characters for accounts 
		serverName = serverName.toLowerCase(Locale.ENGLISH);	//But supports upper case characters for topic names so we lower the case to have the same name for everything
		
		Log.i(appTAG,classTAG+": Lowercase Username: " + UnamePass);
		Log.i(appTAG,classTAG+": Lowercase Server: " + serverName);
		
		/////////////	THREAD POLICY
		
		// Allow the connection to be established in the main thread
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		
		// XMPP CONNECTION
		if(connType.equalsIgnoreCase("XMPP"))
		{
			XMPPComm = new XMPPCommunicator(UnamePass, UnamePass, UnamePass, serverName, getApplicationContext());
		}
		else if(connType.equalsIgnoreCase("XMPP"))
		{
			//TODO AMQPCommunicator
		}
			
			
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(connType.equalsIgnoreCase("XMPP")){
			// CLOSE CONNECTION
			if(XMPPComm != null && xmpp!=null)
				XMPPComm.destroyConnection();
			
			XMPPComm = null;
			Toast toast4 = Toast.makeText(getApplicationContext(), "Disconnecting...", Toast.LENGTH_LONG);
			toast4.show();
			displayNotificationMessage("XMPP stopped");
			
			Log.i(appTAG,classTAG+": XMPP stopped");
		}
		else if (connType.equalsIgnoreCase("AMQP"))
		{
			displayNotificationMessage("AMQP Connection not supported yet!");
		}
	}

	
	@SuppressWarnings("deprecation")
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		//super.onstar
		//super.handleStart(intent, startId);
		//If an internet connection exists start connection else display error message
		if(connType.equalsIgnoreCase("XMPP")){
			Toast toast1 = Toast.makeText(getApplicationContext(), "Connecting to "+ serverName +" using the username "+ UnamePass, Toast.LENGTH_LONG);
			toast1.show();
			if(isNetworkAvailable()){
				xmpp = XMPPComm.XMPPCreateConnection();
				if(xmpp!=null){
					displayNotificationMessage("XMPP started");
					Toast toast3 = Toast.makeText(getApplicationContext(), "Connected!", Toast.LENGTH_LONG);
					toast3.show();
					Log.i(appTAG,classTAG+": XMPP started");
				}
				else{
					Toast toast2 = Toast.makeText(getApplicationContext(), "Connection failed!", Toast.LENGTH_LONG);
					toast2.show();
					displayNotificationMessage("Check server name and uid!");
					Log.e(appTAG,classTAG+": Connection failed");
					Log.e(appTAG,classTAG+": Check server and uid");
				}
			}
			else
			{
				displayNotificationMessage("Check internet connectivity");
				Log.e(appTAG,classTAG+": Internet connection unavailable");
			}
		}
		else if (connType.equalsIgnoreCase("AMQP"))
		{
			displayNotificationMessage("AMQP Connection not supported yet!");
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
	@SuppressWarnings("deprecation")
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

/* Copyright (c) 2013 NITLab, University of Thessaly, Greece
 * This software may be used and distributed solely under the terms of the MIT license (License).
 * You should find a copy of the License in LICENSE.TXT or at http://opensource.org/licenses/MIT.
 * By downloading or using this software you accept the terms and the liability disclaimer in the License.
*/

package com.omf.resourcecontroller;



import java.util.Iterator;
import java.util.List;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.StrictMode;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.omf.resourcecontroller.OMF.OMFMessage;
import com.omf.resourcecontroller.OMF.XMPPClass;

public class BackgroundService extends Service implements Constants{

	public static final String TAG = "BackgroundService";
	
	// Service variables
	private NotificationManager notificationMgr = null;		//	NOTIFICATION MANAGER
	private TelephonyManager  telephonyMgr = null;
	
	// XMPP variables
	//private XMPPConnection xmpp = null;						// 	XMPP CONNECTION VAR
	//private ConnectionConfiguration connConfig = null;		//  XMPP CONFIGURATION
	//private PubSubManager pubmgr = null;					// 	XMPP PUB SUB MANAGER
	//private Node eventNode = null;
	//private Context ctx = null;
	private XMPPClass test = null;
	//XMPP Parser 
	//private XMPPParser parser = null;
	
	//OMF message object
	OMFMessage omfMessage = null;
	
	//Username & password
	private String UnamePass = null;
	
	//TopicName
	private String topicName = null;
	
	
	 
	 
	 
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
		
		
		
		topicName = telephonyMgr.getDeviceId();
		
		UnamePass = "android.omf."+topicName;
		
		
		
		/////////////	THREAD POLICY
		
		// Allow the connection to be established in the main thread
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		
		//Init aSmack
		//ctx = getApplicationContext();
		//SmackAndroid.init(ctx);
		
		// XMPP CONNECTION
		//connConfig = new ConnectionConfiguration(SERVER,PORT);
		//xmpp = new XMPPConnection(connConfig);
		// test = new XMPPClass("BackgroundServiceThread","AndroidThreadTest","pw",getApplicationContext());
		 test = new XMPPClass(UnamePass, UnamePass, topicName, getApplicationContext());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		// CLOSE CONNECTION
		//if(xmpp != null)
		//	xmpp.disconnect();
		
		
		if(test != null)
			test.destroyConnection();
		
		test = null;
		
		displayNotificationMessage("XMPP stopped");
		
		Log.i(TAG,"XMPP stopped");
	}

	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		
		
		//xmpp = test.XMPPCreateConnection("AndroidThreadTest", "pw", getApplicationContext());
		//test.start();
		
		test.XMPPCreateConnection();
		displayNotificationMessage("XMPP started");
		Log.i(TAG,"XMPP started");
		
		
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
			if (runningServiceInfo.service.getShortClassName().equals(serviceName)) {
				serviceRunning = true;
			}
		}
		return serviceRunning;
	}


	/**
	 * 
	 * @param message : the message to be displayed as a notification
	 */
	private void displayNotificationMessage(String message) {
		Notification notify = new Notification(android.R.drawable.stat_notify_chat, message, System.currentTimeMillis());
		notify.flags = Notification.FLAG_AUTO_CANCEL;
		notify.icon = R.drawable.resource_controller;
		
		// The service is not running
		if(!isServiceRunning(("." + BackgroundService.class.getSimpleName()).trim())){
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

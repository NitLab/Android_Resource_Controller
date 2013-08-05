package com.omf.resourcecontroller;



import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.pubsub.ConfigureForm;
import org.jivesoftware.smackx.pubsub.FormType;
import org.jivesoftware.smackx.pubsub.ItemPublishEvent;
import org.jivesoftware.smackx.pubsub.Node;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.PublishModel;
import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;
import org.xmlpull.v1.XmlPullParserException;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;

import com.omf.resourcecontroller.OMF.OMFMessage;
import com.omf.resourcecontroller.parser.XMPPParser;

public class BackgroundService extends Service implements Constants{

	public static final String TAG = "BackgroundService";
	
	// Service variables
	private NotificationManager notificationMgr = null;		//	NOTIFICATION MANAGER
	// XMPP variables
	private XMPPConnection xmpp = null;						// 	XMPP CONNECTION VAR
	private ConnectionConfiguration connConfig = null;		//  XMPP CONFIGURATION
	private PubSubManager pubmgr = null;					// 	XMPP PUB SUB MANAGER
	private Node eventNode = null;
	
	//XMPP Parser 
	private XMPPParser parser = null;
	//OMF message object
	OMFMessage omfMessage = null;
	
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
		
		
		/////////////	THREAD POLICY
		
		// Allow the connection to be established in the main thread
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		
		//Init aSmack
		Context context = getApplicationContext();
		SmackAndroid.init(context);
		
		// XMPP CONNECTION
		connConfig = new ConnectionConfiguration(SERVER,PORT);
		xmpp = new XMPPConnection(connConfig);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		// CLOSE CONNECTION
		if(xmpp != null)
			xmpp.disconnect();
		xmpp = null;
		displayNotificationMessage("XMPP stopped");
		Log.i(TAG,"XMPP stopped");
	}

	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		try{
			xmpp.connect(); 
			if(xmpp.isConnected())
				Log.i(TAG,"XMPP connected");
		}catch(XMPPException e){
			Log.e(TAG, "XMPP connection failed");
			e.printStackTrace();
			xmpp = null;
		}

		if(xmpp.isConnected()){
			try {
				//1st try
				xmpp.login(USERNAME, PASSWORD);
				Log.i(TAG,"XMPP Logged in");
			} catch (XMPPException e) {
				Log.e(TAG, "XMPP login failed");
				Log.i(TAG, "Creating new account");
				
					try {
						if(registerUser(xmpp,USERNAME,PASSWORD)){
							try {
							xmpp.login(USERNAME, PASSWORD);
							Log.i(TAG,"XMPP Logged in");
							} catch (XMPPException e1) {	
								Log.e(TAG,"XMPP Login failed");
							}	
						}
					} catch (XMPPException e1) {
						Log.e(TAG,"Registration failed");
					}
			}
			
			//If xmpp is logged in declare your presence
			if(xmpp.isAuthenticated()){
				//Declare presence
				Presence presence = new Presence(Presence.Type.available);
				xmpp.sendPacket(presence);
			}
				
		}
		
		displayNotificationMessage("XMPP Started");
		
		
		/**
		 * Create node
		 */
		if(xmpp.isAuthenticated()){
			pubmgr = new PubSubManager(xmpp);
			
			//Node configuration form
			ConfigureForm f = new ConfigureForm(FormType.submit);
			/**
			 * Configure form
			 */
			f.setPersistentItems(false);				//false
			f.setPublishModel(PublishModel.open);		//open
			f.setNotifyRetract(false);					//false
			f.setSubscribe(true);						//true
			
			try{
				eventNode = pubmgr.getNode(TOPIC);
			}catch(XMPPException e){
				//e.printStackTrace();
				Log.e(TAG, "Problem getting node "+ TOPIC);
				//If node doesn't exist create it
				try {
					Log.i(TAG, "Creating node "+TOPIC);
					eventNode = pubmgr.createNode(TOPIC,f);
				} catch (XMPPException e1) {
					//e1.printStackTrace();
					Log.e(TAG, "Problem creating event "+TOPIC);
				}
			}
			
			
			
			// Listeners of the events
			//eventNode.addItemEventListener(eventListener);
			
			
			// Publish events
			
				//eventNode.subscribe("alpha@nitlab.inf.uth.gr");
				//#1pub
				//SimplePayload payload1 = new SimplePayload("book","pubsub:test:book", "<book xmlns='pubsub:test:book'><title>Lord of the Rings</title></book>");
				//PayloadItem payloadItem1 = new PayloadItem(null, payload1);
				//((LeafNode)eventNode).publish(payloadItem1);
				
				//#2pub
				//SimplePayload payload2 = new SimplePayload("book","pubsub:test:book", "<book xmlns='pubsub:test:book'><title>Book 2</title></book>");
				//PayloadItem payloadItem2 = new PayloadItem(null, payload2);
				//((LeafNode)eventNode).publish(payloadItem2);
				
				/**
				 * Packet filter
				 */
			/*
				PacketFilter filter = new PacketFilter() {
			        public boolean accept(Packet packet) {
			            return true;
			        }
			    };
			    */
				// This will collect all XMPP messages
			    //PacketCollector collector = xmpp.createPacketCollector(filter);
			    
			    
			    /**
			     * Packet listener and handler
			     * Gets all XMPP packets and parses them
			     * @param packet:Packet 
			     * 
			     */
			    /*
			    PacketListener packetListener = new PacketListener() {
			        public void processPacket(Packet packet) {
			            // Do something with the incoming packet here.
			        	//System.out.println("XML Packet: "+packet.toXML());
			        	//Log.i(TAG,"XML Packet: "+packet.toXML());
			        	parser = new XMPPParser();
			        	//System.out.println(packet.toXML());
			        	try {
			        		omfMessage = parser.XMLParse(packet.toXML());
			        		if(!omfMessage.isEmpty())
			        		{
			        			System.out.println(omfMessage.toString());
			        			OMFHandler(omfMessage);
			        		}
						} catch (XmlPullParserException e) {
							Log.e(TAG,"PullParser exception");
						} catch (IOException e) {
							Log.e(TAG,"IO exception");
						}
			            
			        }
			    };			    
			    */
			    
			/**
			 * Bind packet listener to the xmpp connection and subscribe to a node
			 */
			//Subscribe and add Listeners
			try {
				//node event listener
				eventNode.addItemEventListener(new ItemEventCoordinator());
				//xmpp packet listener
				//xmpp.addPacketListener(packetListener,filter);
				
				//Subscribe to the node
				eventNode.subscribe(xmpp.getUser());
			} catch (XMPPException e) {
				e.printStackTrace();
			}

		}
	}
	
	
	
	/**
	 * HERE CREATE THE EVENT LISTENERS FOR EACH EVENT/ PUBSUB
	 */
	
	/*
	@SuppressWarnings("rawtypes")
	ItemEventListener eventListener = new ItemEventListener() {
		@Override
		public void handlePublishedItems(ItemPublishEvent items) {
			Log.i(TAG,"the items are:" + items.getItems().toString());
			
		}
	};*/
	
	//ITEM LISTENER - Inactive - using packet listener instead
	@SuppressWarnings("rawtypes")
	class ItemEventCoordinator  implements ItemEventListener <PayloadItem>
    {
        @Override
        public void handlePublishedItems(ItemPublishEvent <PayloadItem> items)
        {
        	
        	parser = new XMPPParser();
            
            //System.out.println("Items: "+items.isDelayed());
        	//System.out.println();
            List<PayloadItem> payloads = items.getItems();
            for(PayloadItem item : payloads)
			{
				//System.out.println("test: "+item.toXML());
				if(!items.isDelayed())
				{
					try {
		        		omfMessage = parser.XMLParse(item.toXML());
		        		if(!omfMessage.isEmpty())
		        		{
		        			System.out.println(omfMessage.toString());
		        			OMFHandler(omfMessage);
		        		}
					} catch (XmlPullParserException e) {
						Log.e(TAG,"PullParser exception");
					} catch (IOException e) {
						Log.e(TAG,"IO exception");
					}
				}
			}
            
            
        }
    }
	
	
	
	
	
	/**
	 * Register a new user in xmpp
	 * @param mycon : XMPPConnection object
	 * @param username : String
	 * @param pass : String
	 * @throws XMPPException
	 * @returns boolean: False if registration failed, else true
	 */
	public boolean registerUser(XMPPConnection mycon, String username, String  pass) throws XMPPException{
		if(mycon != null && username!= null && pass != null){
			AccountManager mgr = mycon.getAccountManager();

			if (mgr.supportsAccountCreation ())
		    {
		        mgr.createAccount (username, pass);
		    }
			Log.i(TAG, "Account created: "+username);
			
			//XMPP disconnect
			//XMPP reconnect
			if(xmpp != null)
			{
				xmpp.disconnect();
				xmpp.connect();
				Log.i(TAG, "XMPP connection refresh ");
			}
			
			
			return true;
		}
		return false;
	}
	
	public void OMFHandler(OMFMessage message){
		
		if(message.getMessageType().equalsIgnoreCase("create"))
		{
			message.OMFCreate();
			displayNotificationMessage(message.getMessageType()+"message received");
		}
		else if (message.getMessageType().equalsIgnoreCase("configure"))
		{
			message.OMFConfigure();
			displayNotificationMessage(message.getMessageType()+"message received");
		}
		else if (message.getMessageType().equalsIgnoreCase("request"))
		{
			message.OMFRequest();
			displayNotificationMessage(message.getMessageType()+"message received");
		}
		else if (message.getMessageType().equalsIgnoreCase("inform"))
		{
			message.OMFInform();
			displayNotificationMessage(message.getMessageType()+"message received");
		}
		
		return;
	}
	
	// XML String generator
	public String XMLGenerator(String start){
		
		//long unixTime = System.currentTimeMillis() / 1000L;
		
		String s = "test";
		//String namespace = "http://schema.mytestbed.net/omf/6.0/protocol";
		//String ts = "<ts>"+unixTime+"</ts>";
		
		return s;
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

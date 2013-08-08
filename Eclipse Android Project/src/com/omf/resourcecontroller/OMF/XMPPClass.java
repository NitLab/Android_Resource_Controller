package com.omf.resourcecontroller.OMF;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
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

import android.content.Context;
import android.util.Log;

import com.omf.resourcecontroller.Constants;
import com.omf.resourcecontroller.parser.XMPPParser;

/**
 * 
 * 
 *
 */



	public class XMPPClass implements Constants{
		
		public static final String TAG = "XMPPClass";
		//private XMPPConnectThread conthread = null;
		
		// XMPP variables
		// 	XMPP CONNECTION VAR
		private ConnectionConfiguration connConfig = null;		//  XMPP CONFIGURATION
		private PubSubManager pubmgr = null;					// 	XMPP PUB SUB MANAGER
		//private Node eventNode = null;							// 	XMPP Eventnode
		
		private XMPPConnectionListener connectionListener = null;
		private Context ctx = null;								// 	App context
		private String Username = null;							//	Username for XMPP login
		private String Password = null;							//	Password for XMPP login
		private XMPPConnection xmpp = null;
		
		//XMPP Parser 
		private XMPPParser parser = null;
		
		//OMF message object
		private OMFMessage omfMessage = null;
		
		//Node and Node Listener HashMap
		HashMap<String, ItemEventCoordinator> NodeListeners;
		HashMap<String, Node> Nodes;
		
		//flag
		private boolean flag;
		
		
		
		/**
		 * Constructor
		 */
		//public XMPPConnectThread(){
			
			// Create thread in here
			//conthread = new XMPPConnectThread("XMPP thread");
		//	NodeListeners = new HashMap<String, ItemEventCoordinator>();
		//	Nodes = new HashMap<String, Node>();
		//}
		
		public XMPPClass(String username, String password ,Context appContext){
			
			//conthread = new XMPPConnectThread("XMPP thread");
			NodeListeners = new HashMap<String, ItemEventCoordinator>();
			Nodes = new HashMap<String, Node>();
			Username = username;
			Password = password;
			ctx = appContext;
			flag = true;
		}
		
		public XMPPConnection XMPPCreateConnection(){
			//Init aSmack
			SmackAndroid.init(ctx);
			//SmackConfiguration.setDefaultPingInterval(100);	
			// XMPP CONNECTION
			connConfig = new ConnectionConfiguration(SERVER,PORT);
			xmpp = new XMPPConnection(connConfig);
			
			//Open XMPP Connection
			try{
				xmpp.connect(); 
				if(xmpp.isConnected())
					Log.i(TAG,"XMPP connected");
			}catch(XMPPException e){
				Log.e(TAG, "XMPP connection failed");
				e.printStackTrace();
				xmpp = null;
			}
			
			//Add connection listener
			connectionListener = new XMPPConnectionListener();
			xmpp.addConnectionListener(connectionListener);
			
			//Do Login
			XMPPLogin(xmpp,Username,Password);
			
			//If xmpp is logged in declare your presence
			if(xmpp.isAuthenticated()){
				//Declare presence
				Presence presence = new Presence(Presence.Type.available);
				xmpp.sendPacket(presence);
			}
			
			//CreateTopic
			createTopic(TOPIC);
	
			return xmpp;
		}
		
		public boolean XMPPLogin(XMPPConnection Xmpp, String username,String pass){
			
			if(Xmpp.isConnected()){
				try {
					//1st try
					Xmpp.login(username, pass);
					Log.i(TAG,"XMPP Logged in");
				} catch (XMPPException e) {
					Log.e(TAG, "XMPP login failed");
					Log.i(TAG, "Creating new account");
					
						try {
							if(registerUser(Xmpp,username,pass)){
								try {
									Xmpp.login(username, pass);
								Log.i(TAG,"XMPP Logged in");
								return true;
								} catch (XMPPException e1) {	
									Log.e(TAG,"XMPP Login failed");
									return false;
								}	
							}
						} catch (XMPPException e1) {
							Log.e(TAG,"Registration failed");
							return false;
						}
				}
			}
			return false;
		}
		
		
		
		public void createTopic(String topicName){
			
			if(xmpp.isAuthenticated()){
				pubmgr = new PubSubManager(xmpp);
				
				//New node
				Node eventNode = null;
				//New node listener
				ItemEventCoordinator eventListener = null;
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
					eventNode = pubmgr.getNode(topicName);
				}catch(XMPPException e){
					//e.printStackTrace();
					Log.e(TAG, "Problem getting node "+ topicName);
					//If node doesn't exist create it
					try {
						Log.i(TAG, "Creating node "+topicName);
						eventNode = pubmgr.createNode(topicName,f);
						Nodes.put(topicName,eventNode);
						
					} catch (XMPPException e1) {
						//e1.printStackTrace();
						Log.e(TAG, "Problem creating event "+topicName);
						
					}
				}
				
				
				try {
					//Add event listener
					eventListener = new ItemEventCoordinator();
					eventNode.addItemEventListener(eventListener);
					//Put nodes created in a hashMap
					NodeListeners.put(topicName, eventListener);
					
					//Subscribe to the node
					eventNode.subscribe(xmpp.getUser());
				} catch (XMPPException e) {
					e.printStackTrace();
				}
			}
			//return newNode;
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
				
				//XMPP refresh connection
				if(mycon!= null)
				{
					flag=false;
					mycon.disconnect();
					mycon.connect(); 
					flag=true;
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
			}
			else if (message.getMessageType().equalsIgnoreCase("configure"))
			{
				message.OMFConfigure();	
			}
			else if (message.getMessageType().equalsIgnoreCase("request"))
			{
				message.OMFRequest();	
			}
			else if (message.getMessageType().equalsIgnoreCase("inform"))
			{
				message.OMFInform();
			}
			
			return;
		}
	
		public void destroyConnection(){
			
			xmpp.removeConnectionListener(connectionListener);
			destroyTopics();
			if(xmpp != null)
				xmpp.disconnect();
			
			xmpp = null;
		}
	
		public void destroyTopics(){
			//eventNode.removeItemEventListener(eventListener);
			
			
			for (String key : Nodes.keySet()) {
			    Node node = Nodes.get(key);
			    ItemEventCoordinator nodeListener = NodeListeners.get(key);
			    node.removeItemEventListener(nodeListener);
			    try {
					pubmgr.deleteNode(key);
				} catch (XMPPException e) {
					Log.e(TAG, "Node deletion problem");
					e.printStackTrace();
				}
			}
		}
		
		
		/**Item Listener
		 * 
		 * @author Polychronis
		 * 
		 */
		@SuppressWarnings("rawtypes")
		class ItemEventCoordinator  implements ItemEventListener <PayloadItem>
	    {
	        @Override
	        public void handlePublishedItems(ItemPublishEvent <PayloadItem> items)
	        {
	        	parser = new XMPPParser();
	            List<PayloadItem> payloads = items.getItems();
	            for(PayloadItem item : payloads)
				{
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
		
		class XMPPConnectionListener implements ConnectionListener{
			 public void connectionClosed() {
	             Log.d("SMACK","Connection closed ()");
	             if (flag){
	            	 XMPPCreateConnection();
	             }
	         }
	
	         public void connectionClosedOnError(Exception e) {
	             Log.d("SMACK","Connection closed due to an exception");
	             e.printStackTrace();
	         }
	         public void reconnectionFailed(Exception e) {
	             Log.d("SMACK","Reconnection failed due to an exception");
	             e.printStackTrace();
	         }
	         public void reconnectionSuccessful() {
	             Log.d("SMACK","Connection reconnected");
	         }
	         public void reconnectingIn(int seconds) {
	             Log.d("SMACK","Connection will reconnect in " + seconds);
	         }
		}
		
		
		
	}


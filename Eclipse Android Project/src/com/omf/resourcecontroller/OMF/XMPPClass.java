/* Copyright (c) 2013 NITLab, University of Thessaly, Greece
 * This software may be used and distributed solely under the terms of the MIT license (License).
 * You should find a copy of the License in LICENSE.TXT or at http://opensource.org/licenses/MIT.
 * By downloading or using this software you accept the terms and the liability disclaimer in the License.
*/

package com.omf.resourcecontroller.OMF;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.pubsub.ConfigureForm;
import org.jivesoftware.smackx.pubsub.FormType;
import org.jivesoftware.smackx.pubsub.ItemPublishEvent;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.Node;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.PublishModel;
import org.jivesoftware.smackx.pubsub.SimplePayload;
import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.omf.resourcecontroller.Constants;
import com.omf.resourcecontroller.parser.XMPPParser;
import com.omf.resourcecontroller.parser.XMPPParserV2;

/**
 * 
 * XMPPClass
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
		private String Topic = null;
		private XMPPConnection xmpp = null;
		
		//XMPP Parser 
		//private XMPPParser parser = null;
		private XMPPParserV2 parser2 = null;
		//OMF message object
		private OMFMessage omfMessage = null;
		
		
		//Node and Node Listener HashMap
		HashMap<String, ItemEventCoordinator> NodeListeners;
		HashMap<String, Node> Nodes;
		
		//flag
		private boolean flag;
		
		//Device Managers
		WifiManager wifiManager = null;
		

		/**
		 * Constructor
		 */
		//public XMPPConnectThread(){
			
			// Create thread in here
			//conthread = new XMPPConnectThread("XMPP thread");
		//	NodeListeners = new HashMap<String, ItemEventCoordinator>();
		//	Nodes = new HashMap<String, Node>();
		//}
		
		public XMPPClass(String username, String password, String topicName, Context appContext){
			
			//conthread = new XMPPConnectThread("XMPP thread");
			NodeListeners = new HashMap<String, ItemEventCoordinator>();
			Nodes = new HashMap<String, Node>();
			Username = username;
			Password = password;
			Topic = topicName;
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
				Log.d(TAG, "Check device connectivity");
				xmpp = null;
			}
			
			//Add connection listener
			if(xmpp.isConnected()){
				connectionListener = new XMPPConnectionListener();
				xmpp.addConnectionListener(connectionListener);
			}
			
			//Add ping manager to deal with disconnections (after 6 minutes idle, xmpp disconnects)
			PingManager.getInstanceFor(xmpp).setPingIntervall(5*60*1000);	//5 minutes (5*60*1000 in millisecons)
			
			
			
			//Do Login
			XMPPLogin(xmpp,Username,Password);
			
			//If xmpp is logged in declare your presence
			if(xmpp.isAuthenticated()){
				//Declare presence
				Presence presence = new Presence(Presence.Type.available);
				xmpp.sendPacket(presence);
			}
			
			//Add pubsub manager
			if(xmpp.isAuthenticated())
			{
				pubmgr = new PubSubManager(xmpp);
			}
			//CreateTopic
			createTopic(Topic, false, "main");
	
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
									flag=true;
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
		
		
		
		public Node createTopic(String topicName , boolean isProxy, String rType){
			
			if(xmpp.isAuthenticated()){
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
						//Put node to hashmap
						Nodes.put(topicName,eventNode);
					}catch(XMPPException e){
						//e.printStackTrace();
						Log.e(TAG, "Problem getting node "+ topicName);
						//If node doesn't exist create it
						try {
							Log.i(TAG, "Creating node "+topicName);
							eventNode = pubmgr.createNode(topicName,f);
							//Put node to hashmap
							Nodes.put(topicName,eventNode);
							
						} catch (XMPPException e1) {
							//e1.printStackTrace();
							Log.e(TAG, "Problem creating event "+topicName);
							return null;
						}
					}
					
					
					try {
						//Add event listener
						eventListener = new ItemEventCoordinator(isProxy, rType);
						eventNode.addItemEventListener(eventListener);
						//Put node listener created in a hashMap
						NodeListeners.put(topicName, eventListener);
						
						//Subscribe to the node
						eventNode.subscribe(xmpp.getUser());
					} catch (XMPPException e) {
						e.printStackTrace();
					}
					return eventNode;
			}
			return null;
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
					//flag=true;
					Log.i(TAG, "XMPP connection refresh ");
				}
				return true;
			}
			return false;
		}
		
		
	
	
	
		public void destroyConnection(){
			
			//remove connection listener
			xmpp.removeConnectionListener(connectionListener);
			
			//destroy all topics and remove their listeners
			destroyTopics();
			if(xmpp != null)
				xmpp.disconnect();
			
			xmpp = null;
		}
	
		public void destroySingleTopic(String topicName){
			Node node = Nodes.get(topicName); 
			ItemEventCoordinator nodeListener = NodeListeners.get(topicName);
		    node.removeItemEventListener(nodeListener);
		    Nodes.remove(topicName);
		    try {
				pubmgr.deleteNode(topicName);
			} catch (XMPPException e) {
				Log.e(TAG,"Problem deleting node "+topicName);
			}
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
			
			
			//Variables,arrays to handle duplicate messages
			private String[] duplicateCheck;
			private boolean duplicateFlag;
			private int in;
			private boolean Proxy;
			private String rType;
			
			public ItemEventCoordinator(boolean isProxy, String rType){
				
	    		duplicateCheck = new String[10];
	    		duplicateFlag = false;
	    		this.Proxy = isProxy;
	    		this.rType = rType;
	    		
	    		
	    		in = 0;
	    		for(int j=0;j<10;j++)
	    		{
	    			duplicateCheck[j] =""; 
	    		}
			}
			
	        @Override
	        public void handlePublishedItems(ItemPublishEvent <PayloadItem> items)
	        {
	        	//parser = new XMPPParser();
	        	try {
					parser2 = new XMPPParserV2();
				} catch (XmlPullParserException e1) {
					Log.e(TAG, "Updated Parser  problem");
				}
	            List<PayloadItem> payloads = items.getItems();
	            for(PayloadItem item : payloads)
				{
					if(!items.isDelayed())
					{
						try {
			        		//omfMessage = parser.XMLParse(item.toXML());
			        		omfMessage = parser2.XMLParse(item.toXML());
			        		//System.out.println(omfMessage.toString());
			        		
			        		//System.out.println("parser2: " + parser2.XMLParse(item.toXML()).toString());
			        		
			        		System.out.println(item.toString());
			        		if(!omfMessage.isEmpty())
			        		{
			        			duplicateFlag = false;
			        			for(int i=0;i<10;i++)
			        			{	
			        				if(omfMessage.equals(duplicateCheck[i]))
			        				{
			        					duplicateFlag = true;
			        				}
			        			}
			        		
			        			if(!duplicateFlag)
		        				{
			        				//Circular array, increment counter
			        				in=(in+1)%10;
			        				//put message into duplicateCheck array
			        				duplicateCheck[in]=omfMessage.getMessageID();
			        				if(Proxy)
			        				{
			        					System.out.println("This is a resource proxy");
			        					if(rType.equalsIgnoreCase("network")){
			        						System.out.println("Network");
			        						OMFNetworkHandler(omfMessage);
			        					}
			        					else if(rType.equalsIgnoreCase("application"))
			        					{
			        						System.out.println("STARTING APPLICATION");
			        					}
			        				}
			        				else
			        				{
		        						OMFMainHandler(omfMessage);
			        				}
		        					System.out.println(omfMessage.toString());
		        				}
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
	             if (flag){
			             if(!xmpp.isAuthenticated()){
			            	 XMPPLogin(xmpp,Username,Password);
			             }
	             }
	         }
	         public void reconnectingIn(int seconds) {
	             Log.d("SMACK","Connection will reconnect in " + seconds);
	         }
		}
		
		
		/**
		 * OMF Message handlers
		 * @param message : OMF message type
		 */
		
		public void OMFMainHandler(OMFMessage message){
			
			if(message.getMessageType().equalsIgnoreCase("create"))
			{
				//message.OMFCreate();
				createTopic((String)message.getProperty("uid"),true, message.getType());
				System.out.println(message.getProperty("uid"));
			}
			else if (message.getMessageType().equalsIgnoreCase("configure"))
			{
				
				String membership = (String) message.getProperty("membership");
				if (membership != null) {
					Node newNode = null;
					System.out.println("test");
					//Send Inform to main topic
					Node mainNode = Nodes.get(Topic);
					OMFMessageGenerator(mainNode, "inform", message, "membership");
					
					//Subscribe to membership, or create the topic
					newNode = createTopic(membership,false,"");
					OMFMessageGenerator(newNode, "inform", message, "membership");
					
				}
			}
			else if (message.getMessageType().equalsIgnoreCase("request"))
			{
				
			}
			else if (message.getMessageType().equalsIgnoreCase("inform"))
			{
				
			}
			else if (message.getMessageType().equalsIgnoreCase("release"))
			{

			}
			
			return;
		}
	
	/**
	 * Network Handler
	 * @param message : OMF message type
	 */
	public void OMFNetworkHandler(OMFMessage message){
		
		if(message.getMessageType().equalsIgnoreCase("create"))
		{
			//message.OMFCreate();
			
			//createTopic(message.getProperty("uid"),true);
			
		}
		else if (message.getMessageType().equalsIgnoreCase("configure"))
		{
			//message.OMFConfigure();
			String value = (String)message.getProperty("wifi_state");
			if (value != null) {
				wifiManager = (WifiManager)ctx.getSystemService(Context.WIFI_SERVICE);
				if(value.equalsIgnoreCase("false")){
					System.out.println("Wifi OFF");
				}
				else if(value.equalsIgnoreCase("true")){
					System.out.println("Wifi ON");
				}
				wifiManager.setWifiEnabled(Boolean.parseBoolean(value));
			} else {
			    // No such key
			}
						
		}
		else if (message.getMessageType().equalsIgnoreCase("request"))
		{
			//message.OMFRequest();
		}
		else if (message.getMessageType().equalsIgnoreCase("inform"))
		{
			//message.OMFInform();
		}
		else if (message.getMessageType().equalsIgnoreCase("release"))
		{
			//message.OMFInform();
		}
		
		return;
	}
		
		
	public void OMFMessageGenerator(Node node, String mtype, OMFMessage message, String propType){
		UUID mid = UUID.randomUUID();							//message id
		long timestamp = System.currentTimeMillis() / 1000L;	//timestamp
		String src ="xmpp://"+Topic+"@"+SERVER;
		String xmlString = null;
		if(mtype.equalsIgnoreCase("inform") && node!=null && message.getMessageType().equals("configure")){
			
			xmlString = "<"+mtype+" xmlns='"+SCHEMA+"' mid='"+mid+"'" + ">" 
						+"<src>"+src+"</src>"
						+"<ts>"+timestamp+"</ts>"
						+"<cid>"+message.getMessageID()+"</cid>"
						+"<itype>"+"STATUS"+"</itype>"
						+"<props>"
						+"<"+ propType +" type='array' >" 
						+"<it type='string'>"+message.getProperty(propType)+"</it>"
						+"</"+ propType +">"
						+"</props>"
						+"</"+ mtype +">";
			SimplePayload payload = new SimplePayload(mtype,SCHEMA, xmlString);
			
			System.out.println(xmlString);
			
			@SuppressWarnings({ "unchecked", "rawtypes" })
			PayloadItem payloadItem = new PayloadItem(null, payload);
				
			((LeafNode)node).publish(payloadItem);
			
		}
	}	
		
	}


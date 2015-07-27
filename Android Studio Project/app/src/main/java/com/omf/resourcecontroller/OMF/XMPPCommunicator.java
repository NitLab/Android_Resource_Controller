/* Copyright (c) 2013 NITLab, University of Thessaly, Greece
 * This software may be used and distributed solely under the terms of the MIT license (License).
 * You should find a copy of the License in LICENSE.TXT or at http://opensource.org/licenses/MIT.
 * By downloading or using this software you accept the terms and the liability disclaimer in the License.
*/

package com.omf.resourcecontroller.OMF;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.harmony.javax.security.sasl.SaslException;
import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.ping.PingManager;
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
import com.omf.resourcecontroller.generator.PropertiesGenerator;
import com.omf.resourcecontroller.generator.XMLGenerator;
import com.omf.resourcecontroller.parser.XMPPParserV2;

/**
 * XMPPClass object : the object that has all the informations and all the functions to create an xmpp connection 
 * and a main topic and listen to it
 * @author Polychronis Symeonidis
 *
 */



	public class XMPPCommunicator implements Constants{
		public static final String appTAG = "omf.resourcecontroller";
		public static final String classTAG = "XMPPCommunicator";
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
		
		private MessagePublisher msgPub;
		
		//Node and Node Listener HashMap
		private HashMap<String, ItemEventCoordinator> NodeListeners;
		private HashMap<String, Node> Nodes;
		
		private String serverName;
		
		//flag
		private boolean flag;
		
		
		
		
	
	/**
	 * XMPPClass Constructor
	 * @param username : XMPP server username
	 * @param password : XMPP server password
	 * @param topicName : XMPP main topic name (RC topic name)
	 * @param Server : XMPP Server name
	 * @param appContext : The application context
	 */
		public XMPPCommunicator(String username, String password, String topicName, String Server, Context appContext){
			
			NodeListeners = new HashMap<String, ItemEventCoordinator>();
			Nodes = new HashMap<String, Node>();
			
			Username = username;
			Password = password;
			Topic = topicName;
			ctx = appContext;
			flag = true;
			
			serverName = Server;
			
			msgPub = new MessagePublisher("XMPP");
			
			//Init aSmack
			SmackAndroid.init(ctx);
			
			//Set the reply timeout of asmack in ms to avoid server not responding fast enough if busy
			//SmackConfiguration.setPacketReplyTimeout(10000);

			// XMPP CONNECTION		
			connConfig = new ConnectionConfiguration(serverName,PORT);
			
			//connConfig.setSASLAuthenticationEnabled(true);
			connConfig.setCompressionEnabled(true);
			connConfig.setSecurityMode(SecurityMode.disabled);
			/*
			//Connection configuration - generates a warning on startup because the truststore path is set to null
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				connConfig.setKeystoreType("AndroidCAStore");
				//connConfig.setKeystorePassword(null);
				connConfig.setKeystorePath(null);
				//connConfig.set
			} else {
				connConfig.setKeystoreType("BKS");
			    String path = System.getProperty("javax.net.ssl.trustStore");
			    if (path == null)
			        path = System.getProperty("java.home") + File.separatorChar + "etc"
			            + File.separatorChar + "security" + File.separatorChar
			            + "cacerts.bks";
			    connConfig.setKeystorePath(path);
			}
			*/
			xmpp = new XMPPTCPConnection(connConfig);
			
		}
		
		/**
		 * XMPPCreateConnection 
		 * @return XMPPConncection xmpp: xmpp connection variable
		 */
		public XMPPConnection XMPPCreateConnection(){

			
			//Open XMPP Connection
			try{
				xmpp.connect(); 
				if(xmpp.isConnected())
					Log.i(appTAG,classTAG+": XMPP connected");
			}catch(XMPPException e){
				Log.e(appTAG, classTAG+": XMPP connection failed");
				Log.d(appTAG, classTAG+": Check device connectivity or server name");
				//xmpp = null;
				return null;
			} catch (SmackException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//Add connection listener
			if(xmpp.isConnected()){
				connectionListener = new XMPPConnectionListener();
				xmpp.addConnectionListener(connectionListener);
						
				//Add ping manager to deal with disconnections (after 6 minutes idle, xmpp disconnects)
				PingManager.getInstanceFor(xmpp).setPingInterval(5*60*1000);	//5 minutes (5*60*1000 in millisecons)

				//Do Login
				XMPPLogin(xmpp,Username,Password);
				
				//If xmpp is logged in declare your presence
				if(xmpp.isAuthenticated()){
					//Declare presence
					Presence presence = new Presence(Presence.Type.available);
					try {
						xmpp.sendPacket(presence);
					} catch (NotConnectedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				//Add pubsub manager
				if(xmpp.isAuthenticated())
				{
					pubmgr = new PubSubManager(xmpp);
				}
				//CreateTopic
				createTopic(Topic, false, "main", Topic, null, null);
				return xmpp;
			}
			return null;//new
		}
		
		/**
		 * 
		 * @param Xmpp : XMPPConnection variable
		 * @param username : username for the login
		 * @param pass : password for the login
		 * @return true or false : true if login has succeeded false otherwise
		 */
		public boolean XMPPLogin(XMPPConnection Xmpp, String username,String pass){
			
			if(Xmpp.isConnected()){
				try {
					//1st try
					Xmpp.login(username, pass);
					Log.i(appTAG,classTAG+": XMPP Logged in");
				} catch (XMPPException e) {
					Log.e(appTAG, classTAG+": XMPP login failed");
					Log.i(appTAG, classTAG+": Creating new account");
					
						try {
							if(registerUser(Xmpp,username,pass)){
								try {
									Xmpp.login(username, pass);
									flag=true;
								Log.i(appTAG,classTAG+": XMPP Logged in");
								return true;
								} catch (XMPPException e1) {	
									Log.e(appTAG,classTAG+": XMPP Login failed");
									return false;
								} catch (SaslException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								} catch (SmackException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}	
							}
						} catch (XMPPException e1) {
							Log.e(appTAG,classTAG+": Registration failed");
							return false;
						}
				} catch (SaslException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SmackException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return false;
		}
		
		
		/**
		 * Create Topic
		 * @param topicName : topic name
		 * @param isProxy : if it is a resource proxy
		 * @param rType : The resource type of the topic, null if it is the main topic
		 * @param destinationName : where the topic replys
		 * @param list : a list with all the memberships
		 * @param properties : topic properties , if it is the main topic the properties are null
		 * @return Node node: if creation succesfull otherwise returns null
		 */
		public Node createTopic(String topicName , boolean isProxy, String rType, String destinationName, List<String> list, HashMap<String, Object> properties){
			
			if(xmpp.isAuthenticated()){
				
				//Add pubsub manager
					
					//pubmgr = new PubSubManager(xmpp);
				
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
						Log.i(appTAG, classTAG+": Going to create node: "+topicName);
						eventNode = pubmgr.getNode(topicName);
						//Put node to hashmap
						Nodes.put(topicName,eventNode);
					}catch(XMPPException e){
						e.printStackTrace();
						Log.e(appTAG, classTAG+": Problem getting node "+ topicName);
						//If node doesn't exist create it
						try {
							Log.i(appTAG, classTAG+": Creating node "+topicName);
							eventNode = pubmgr.createNode(topicName,f);
							//Put node to hashmap
							Log.i(appTAG, classTAG+": Putting node "+topicName+" to hashmap");
							Nodes.put(topicName,eventNode);
							
						} catch (XMPPException e1) {
							e1.printStackTrace();
							Log.e(appTAG, classTAG+": Problem creating node "+topicName);
							return null;
						} catch (NoResponseException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (NotConnectedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					} catch (NoResponseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NotConnectedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
					try {
						//Add event listener
						if(list!=null)
						{
							if(properties!=null)
								eventListener = new ItemEventCoordinator(isProxy, rType, topicName, destinationName, properties, list);
							else
								eventListener = new ItemEventCoordinator(isProxy, rType, topicName, destinationName, properties, list);
						}
						else
						{	
							if(properties!=null)
								eventListener = new ItemEventCoordinator(isProxy, rType, topicName, destinationName, properties);
							else
								eventListener = new ItemEventCoordinator(isProxy, rType, topicName, destinationName, properties);
						}
						
						eventNode.addItemEventListener(eventListener);
						//Put node listener created in a hashMap
						NodeListeners.put(topicName, eventListener);
						
						//Subscribe to the node
						eventNode.subscribe(xmpp.getUser());
						
					} catch (XMPPException e) {
						e.printStackTrace();
						Log.e(appTAG, classTAG+": Problem subscribing "+topicName);
						return null;
					} catch (NoResponseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NotConnectedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					//Here put publication of inform message
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
				Log.i(appTAG, classTAG+": Trying to register the user...!");
				//AccountManager mgr = mycon.getAccountManager();
				AccountManager mgr = AccountManager.getInstance(mycon);
				try {
					if (mgr.supportsAccountCreation ())
					{
					    mgr.createAccount (username, pass);
					}
				} catch (NoResponseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NotConnectedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Log.i(appTAG, classTAG+": Account created: "+username);
				
				//XMPP refresh connection
				if(mycon!= null)
				{
					flag=false;
					try {
						mycon.disconnect();
						mycon.connect(); 
					} catch (NotConnectedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (SmackException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					//flag=true;
					Log.i(appTAG, classTAG+": XMPP connection refresh ");
				}
				return true;
			}
			return false;
		}
		
		
	
	
	/**
	 * Destroy the xmpp connection, remove event listeners and the nodes from the NODE hashmap
	 */
		public void destroyConnection(){
			if(xmpp.isConnected()){
				//remove connection listener
				xmpp.removeConnectionListener(connectionListener);
				
				//destroy all topics and remove their listeners
				destroyTopics();
				if(xmpp != null)
					try {
						xmpp.disconnect();
					} catch (NotConnectedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
			xmpp = null;
		}
	
		/**
		 * Destroy a single topic, called when a release message arrives
		 * @param topicName
		 * @return boolean : true if destroy has succeeded, false otherwise
		 */
		public boolean destroySingleTopic(String topicName){
			Log.i(appTAG,classTAG+": Destroy single topic");
			Node node = Nodes.get(topicName); 
			ItemEventCoordinator nodeListener = NodeListeners.get(topicName);
		    node.removeItemEventListener(nodeListener);
		    
		    Nodes.remove(topicName);
		    NodeListeners.remove(topicName);
		    
		    Log.i(appTAG,classTAG+": Destroy single topic2");
		    try {
				pubmgr.deleteNode(topicName);
			} catch (XMPPException e) {
				Log.e(appTAG,classTAG+": Problem deleting node "+topicName);
				return false;
			} catch (NoResponseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotConnectedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    
		    return true;
		}
	
		/**
		 * Destroy all topics, called when destroyConnection() is called
		 */
		public void destroyTopics(){
		
			for (String key : Nodes.keySet()) {
			    Node node = Nodes.get(key);
			    ItemEventCoordinator nodeListener = NodeListeners.get(key);
			    node.removeItemEventListener(nodeListener);
			    try {
			    	Log.i(appTAG,classTAG+": Deleting node: "+ key);
					pubmgr.deleteNode(key);
				} catch (XMPPException e) {
					Log.e(appTAG, classTAG+": Node deletion problem");
					e.printStackTrace();
				} catch (NoResponseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NotConnectedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		
		/**Item Listener
		 * Is the xmpp message listener object that is bound to each topic that is created
		 * @author Polychronis Symeonidis
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
			private String listensTo;
			private String replysTo;
			private List<String> memberships;
			//private Lock lock;
			private HashMap<String, Object> properties;
			private OMFMessage omfMessage;
			private OMFProxyHandlers omfHandler;
			
			/**
			 * ItemEventCoordinator Constructor
			 * @param isProxy : boolean if the topic is a resource proxy
			 * @param rType : The resource type of the proxy, null if it is not a proxy
			 * @param srcName : source of the xmpp message 
			 * @param dstName :  destination of the reply
			 * @param Props :  creation properties of the resource proxy, null if it is not a proxy
			 * 
			 */
			public ItemEventCoordinator(boolean isProxy, String rType, String srcName, String dstName, HashMap<String, Object> Props){
				
	    		duplicateCheck = new String[10];
	    		duplicateFlag = false;
	    		this.Proxy = isProxy;
	    		this.rType = rType;
	    		this.listensTo = srcName;			//which topic the listener is assigned to listen
	    		this.replysTo = dstName;
	    		this.memberships = new ArrayList<String>();
	    		this.omfMessage = new OMFMessage();
	    		
	    		if(Props!=null)
	    			this.omfHandler = new OMFProxyHandlers(Props, ctx, srcName, serverName);
	    		
	    		in = 0;
	    		for(int j=0;j<10;j++)
	    		{
	    			duplicateCheck[j] =""; 
	    		}
	    		
	    		//this.lock = new ReentrantLock();
	    		
	    		if(Props!=null)
	    			this.properties = new HashMap<String,Object> (Props);
	    		else
	    			this.properties = null;

			}
			/**
			 * ItemEventCoordinator Constructor
			 * @param isProxy : boolean if the topic is a resource proxy
			 * @param rType : The resource type of the proxy, null if it is not a proxy
			 * @param srcName : source of the xmpp message 
			 * @param dstName :  destination of the reply
			 * @param Props :  creation properties of the resource proxy, null if it is not a proxy
			 * @param list : list of the topic memberships
			 */
			public ItemEventCoordinator(boolean isProxy, String rType, String srcName, String dstName, HashMap<String, Object> Props , List<String> list){
				
	    		duplicateCheck = new String[10];
	    		duplicateFlag = false;
	    		this.Proxy = isProxy;
	    		this.rType = rType;
	    		this.listensTo = srcName;			//which topic the listener is assigned to listen
	    		this.replysTo = dstName;
	    		this.memberships = list;
	    		this.omfMessage = new OMFMessage();
	    		
	    		
	    		if(Props!=null)
	    			this.omfHandler = new OMFProxyHandlers(Props, ctx, srcName, serverName);
	    		
	    		in = 0;
	    		for(int j=0;j<10;j++)
	    		{
	    			duplicateCheck[j] =""; 
	    		}
	    		
	    		//this.lock = new ReentrantLock();
	    		
	    		if(Props!=null)
	    			this.properties = new HashMap<String,Object> (Props);
	    		else
	    			this.properties = null;

			}
			
	        @Override
	        /**
	         * handlePublishedItems
	         * Gets the published XML item parses it and handles it
	         */
	        public void handlePublishedItems(ItemPublishEvent <PayloadItem> items)
	        {	
	        	try {
					parser2 = new XMPPParserV2();
				} catch (XmlPullParserException e1) {
					Log.e(appTAG, classTAG+": Updated Parser  problem");
				}
	            List<PayloadItem> payloads = items.getItems();
	            for(PayloadItem item : payloads)
				{
					if(!items.isDelayed())
					{
						try {
							//parse the XML Item
			        		omfMessage = parser2.XMLParse(item.toXML());

			        		if(!omfMessage.isEmpty())
			        		{
				        			duplicateFlag = false;
				        			for(int i=0;i<10;i++)
				        			{	
				        				//Log.w(TAG, listensTo+":Duplicate array "+i+": "+duplicateCheck[i]);
				        				if(omfMessage.equals(duplicateCheck[i]))
				        				{
				        					//Log.e(TAG, "FOUND DUPLICATE");
				        					duplicateFlag = true;
				        					break;
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
				        					if(rType.equalsIgnoreCase("wlan")){
				        						System.out.println("wlan");
				        						memberships = omfHandler.OMFNetworkHandler(omfMessage, listensTo, memberships, replysTo, Nodes);
				        					}
				        					else if(rType.equalsIgnoreCase("application"))
				        					{
				        						System.out.println("Receiving message to AppProxy");
				        						//System.out.println(properties.toString());
				        						memberships = omfHandler.OMFApplicationHandler(omfMessage, listensTo, memberships, replysTo, Nodes);
				        					}
				        				}
				        				else
				        				{
				        					memberships = OMFMainHandler(omfMessage, listensTo, memberships, replysTo, properties);
				        				}
				        				Log.i(appTAG, classTAG+": ############################################");
				        				Log.i(appTAG,classTAG+": "+listensTo+"##"+item.toString());
				        				Log.i(appTAG, classTAG+": ############################################");
			        					//System.out.println(omfMessage.toString());
			        				}
			        		}
			        		else
			        		{
			        			Log.e(appTAG, classTAG+": Something went wrong, OMF Message is empty");
			        		}
						} catch (XmlPullParserException e) {
							Log.e(appTAG,classTAG+": PullParser exception");
						} catch (IOException e) {
							Log.e(appTAG,classTAG+": IO exception");
						}
					}
				}  
	        }
	    }
		/**
		 * XMPP connection Listener
		 * Listens to the connection and handles disconnections that are cause by exceptions
		 * @author Polychronis Symeonidis
		 *
		 */
		class XMPPConnectionListener implements ConnectionListener{
			 public void connectionClosed() {
	             Log.d("SMACK",classTAG+": Connection closed ()");
	         }
	
	         public void connectionClosedOnError(Exception e) {
	             Log.d("SMACK",classTAG+": Connection closed due to an exception");
	             e.printStackTrace();
	         }
	         public void reconnectionFailed(Exception e) {
	             Log.d("SMACK",classTAG+": Reconnection failed due to an exception");
	             e.printStackTrace();
	         }
	         public void reconnectionSuccessful() {
	             Log.d("SMACK",classTAG+": Connection reconnected");
	             if (flag){
			             if(!xmpp.isAuthenticated()){
			            	 XMPPLogin(xmpp,Username,Password);
			             }
	             }
	         }
	         public void reconnectingIn(int seconds) {
	             Log.d("SMACK",classTAG+": Connection will reconnect in " + seconds);
	         }

			@Override
			public void authenticated(XMPPConnection arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void connected(XMPPConnection arg0) {
				// TODO Auto-generated method stub
				
			}
		}
		
		
	
		/**
		 * 
		 * @param incomingMessage : OMFMessage that is handled
		 * @param fromTopic : where the message came from
		 * @param memberships  : A list of the topic memberships
		 * @param toTopic : Where the topic replys
		 * @param properties : properties of the topic
		 * @return List<String> :  List of the memberships
		 */
		public List<String> OMFMainHandler(OMFMessage incomingMessage, String fromTopic, List<String> memberships, String toTopic, HashMap<String, Object> properties){
			
			//XML Generator for the MainHandler
			XMLGenerator xmlGen = new XMLGenerator();
			PropertiesGenerator propGen = new PropertiesGenerator();
			RegularExpression regEx = new RegularExpression();
			//String xmlPayload = null;
			OMFMessage genOMFmessage = new OMFMessage();
			Node mainNode = Nodes.get(toTopic);
			HashMap<String, Object> propsMap = null;
			
			String myUid ="xmpp://"+fromTopic+"@"+serverName;
			String myResType = "Android OMF 6 Resource Controller";
			String myType = "android_device";
			//String[] supportedChildrenType = {"application"};
			List<String> supportedChildrenType = new ArrayList<String>();
			supportedChildrenType.add("application");
			//String myType = "android_device";
			
			//I should add all the supported resource proxies
			
			
			System.out.println(incomingMessage);
			OMFMessage message = new OMFMessage(incomingMessage);
			
			
			if(message.getMessageType().equalsIgnoreCase("create"))
			{
				
				Log.i(appTAG,classTAG+": "+fromTopic+": "+"create");
				if(message.getType().equalsIgnoreCase("application") || message.getType().equalsIgnoreCase("wlan"))
				{

					String appTopicName = UUID.randomUUID().toString();
					Log.e(appTAG, classTAG+": "+appTopicName+" !!!!!!!!!!!!!!!!");
					//String appTopicName = message.getMessageID();
					Node applicationNode = createTopic(appTopicName,true,message.getType(),appTopicName, null,message.getPropertiesHashmap());
					
					String appTopicUri = "xmpp://"+appTopicName+"@"+serverName;
					
					
					if(applicationNode!=null)
					{
						propsMap = new HashMap<String, Object>();
						
						propsMap = propGen.addProperties(propsMap, "res_id", new PropType(appTopicUri,"string"));
						
						//send inform to application node only with res_id as property
						genOMFmessage = xmlGen.informMessage(appTopicName, serverName, null, "CREATION.OK", propsMap);
						msgPub.PublishItem(genOMFmessage, "inform", applicationNode);
							
						//Check if message contains membership property
						//If property membership exists
						if(message.getProperty("membership")!=null){
							PropType prop = (PropType)message.getProperty("membership");
							String membership = null;
							String membershipTopic = null;
							propsMap = new HashMap<String, Object>();

							
							if(prop.getType().equalsIgnoreCase("String")){
								membership =(String)prop.getProp();
								
								//Get membership from xmpp://membership@host using regular expressions OMF 6.0.7 version sends membership in that particular format
								membershipTopic = regEx.membershipReg(membership);
								if (membershipTopic != null) {
									Node newNode = null;
									//Send Inform to main topic
	
									//inform message to answer
									genOMFmessage = xmlGen.informMessage(appTopicName, serverName, message, "STATUS", propsMap);
									
									
									//Subscribe to membership, or create the topic
									List<String> tempList = new ArrayList<String> ();
									tempList.add(membershipTopic);
									newNode = createTopic(membershipTopic,true,message.getType(), appTopicName, tempList,message.getPropertiesHashmap());
									
									//add membership array
									propsMap = propGen.addProperties(new HashMap<String,Object>(), "membership", new PropType(tempList,"array"));
									
									//add hrn
									if(message.getProperty("hrn")!=null)
									{
										if(((PropType)message.getProperty("hrn")).getType().equalsIgnoreCase("string"))
										{
											propsMap = propGen.addProperties(propsMap, "hrn", new PropType((String)(((PropType)message.getProperty("hrn")).getProp()),"string"));
										}
									}
									
									//Add this membership to my membership list
									genOMFmessage = xmlGen.informMessage(appTopicName, serverName, null, "STATUS", propsMap);	//Send that i subscribed to membership
									//publish outcome of membership to the new "member" node and to the original topic?()
									msgPub.PublishItem(genOMFmessage, "inform", newNode);
									
									
								}
								

								propsMap = new HashMap<String, Object>();
								propsMap.putAll(message.getPropertiesHashmap());
								propsMap = propGen.addProperties(propsMap, "res_id", new PropType(appTopicUri,"string"));
								propsMap = propGen.addProperties(propsMap, "uid", new PropType(appTopicName,"string"));
								
						
								genOMFmessage = xmlGen.informMessage(Topic, serverName, message, "CREATION.OK", propsMap);
								msgPub.PublishItem(genOMFmessage, "inform", mainNode);
							}
						}
						
					}
					else
					{
						//send inform to main node
						genOMFmessage = xmlGen.informMessage(toTopic, serverName, message, "CREATION.FAILED", propGen.addProperties(new HashMap<String, Object>(), "reason", new PropType("Uknown error prevented the creation of the resource "+message.getType(),"string")));
						msgPub.PublishItem(genOMFmessage, "inform", mainNode);
					}
					
				}
				else
				{
					//send inform to main node
					genOMFmessage = xmlGen.informMessage(toTopic, serverName, message, "CREATION.FAILED", propGen.addProperties(new HashMap<String, Object>(), "reason", new PropType("Unknown type of resource '"+message.getType()+"'","string")));
					msgPub.PublishItem(genOMFmessage, "inform", mainNode);
				}
				
				//createTopic((String)message.getProperty("uid"),true, message.getType());
				//System.out.println(message.getProperty("uid"));				
				
			}
			else if (message.getMessageType().equalsIgnoreCase("configure"))
			{
				Log.i(appTAG,classTAG+": "+fromTopic+": "+"configure");
				
				
				//If property membership exists
				if(message.getProperty("membership")!=null){
					PropType prop = (PropType)message.getProperty("membership");
					String membership = null;
					String membershipTopic = null;
					if(prop.getType().equalsIgnoreCase("String")){
						membership =(String)prop.getProp();
						
						//Get membership from xmpp://membership@host using regular expressions OMF 6.0.7 version sends membership in that particular format
						membershipTopic = regEx.membershipReg(membership);
				
						if (membershipTopic != null ) {
							Node newNode = null;
							//Send Inform to main topic

							if(checkMembership(memberships, membership)){
								//inform message to answer
								genOMFmessage = xmlGen.informMessage(toTopic, serverName, message, "STATUS", propGen.addProperties(new HashMap<String,Object>(), "membership", new PropType(memberships,"array")));
								msgPub.PublishItem(genOMFmessage, "inform", mainNode);
							
								//Subscribe to membership, or create the topic
								newNode = createTopic(membershipTopic,false,"", membershipTopic, null, null);
								memberships.add(membership);																																			//Add this membership to my membership list
							}
							else
							{
								//inform message to answer
								genOMFmessage = xmlGen.informMessage(toTopic, serverName, message, "STATUS", propGen.addProperties(new HashMap<String,Object>(), "membership", new PropType(memberships,"array")));
								msgPub.PublishItem(genOMFmessage, "inform", mainNode);
								
								newNode = Nodes.get(membershipTopic);
							}
							
							genOMFmessage = xmlGen.informMessage(toTopic, serverName, null, "STATUS", propGen.addProperties(new HashMap<String,Object>(), "membership", new PropType(memberships,"array")));	//Send that i subscribed to membership
								
							//publish outcome of membership to the new "member" node and to the original topic
							msgPub.PublishItem(genOMFmessage, "inform", newNode);
							msgPub.PublishItem(genOMFmessage, "inform", mainNode);
						}
					}
				}
				else
				{
					//reply with error
					genOMFmessage = xmlGen.informMessage(toTopic, serverName, message, "ERROR", null);
					msgPub.PublishItem(genOMFmessage, "inform", mainNode);
				}
				
				
			}
			else if (message.getMessageType().equalsIgnoreCase("request"))
			{
				System.out.println(fromTopic+": "+"request");
				
				propsMap = new HashMap<String, Object>();
				
				if(message.getProperty("uid")!=null)
				{
					propsMap = propGen.addProperties(propsMap, "uid", new PropType(myUid,"string"));
					propsMap = propGen.addProperties(propsMap, "type", new PropType(myResType,"string"));
				}
				if(message.getProperty("res_id")!=null)
					propsMap = propGen.addProperties(propsMap, "res_id", new PropType(myResType,"string"));
				
				if(message.getPropertiesHashmap().isEmpty())
				{
					//NEEDS Testing!!!
					//if the properties hashmap is empty reply with the supportedchildren
					propsMap = propGen.addProperties(propsMap, "uid", new PropType(myUid,"string"));
					propsMap = propGen.addProperties(propsMap, "supported_children_type",  new PropType(supportedChildrenType,"array"));
					propsMap = propGen.addProperties(propsMap, "type", new PropType(myType,"string"));
					propsMap = propGen.addProperties(propsMap, "membership", new PropType(memberships,"array"));
				}	
				//add properties interfaces,applications.devices,wlan_devices,supported_children_type[application,android application,uid,type="node",membership:[],child_resources:=>[]
				//Add this membership to my membership list
				genOMFmessage = xmlGen.informMessage(toTopic, serverName, message, "STATUS", propsMap);	//Send that i subscribed to membership
				//publish outcome of membership to the new "member" node and to the original topic?()
				msgPub.PublishItem(genOMFmessage, "inform", mainNode);
				//message.OMFRequest();
			}
			else if (message.getMessageType().equalsIgnoreCase("inform"))
			{
				System.out.println(fromTopic+": "+"inform");
				//message.OMFInform();
			}
			else if (message.getMessageType().equalsIgnoreCase("release"))
			{
				System.out.println(fromTopic+": "+"release");
				System.out.println(message.toString());
				
				
				//release topic
				boolean outcome = destroySingleTopic(message.getResID());
				//boolean outcome = true;
				if(outcome)
				{
					//inform released
					genOMFmessage = xmlGen.informMessage(message.getResID(), serverName, message, "RELEASED", null);
					//System.out.println("#######"+xmlPayload);
					
					
				}
				else
				{
					//inform failed-error
					genOMFmessage = xmlGen.informMessage(message.getResID(), serverName, message, "ERROR", null);
					//PublishItem(xmlPayload, SCHEMA, "inform", mainNode);
				}	
				msgPub.PublishItem(genOMFmessage, "inform", mainNode);
				//send inform to application node only with res_id as property
			}
			
			return memberships;
		}

		
	
	
		
	/**
	 * checkMembership function to handle duplicate membership requests
	 * @param list
	 * @param membershipRequest
	 * @return boolean: if membership exists
	 */
	public boolean checkMembership(List<String> list, String membershipRequest)
	{
		for(int i=0;i<list.size();i++)
		{
			if(membershipRequest.equalsIgnoreCase(list.get(i)))
				return false;
		}
		return true;
	}
	
	
	}


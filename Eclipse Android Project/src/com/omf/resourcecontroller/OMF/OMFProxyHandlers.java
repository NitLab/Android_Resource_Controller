/* Copyright (c) 2013 NITLab, University of Thessaly, Greece
 * This software may be used and distributed solely under the terms of the MIT license (License).
 * You should find a copy of the License in LICENSE.TXT or at http://opensource.org/licenses/MIT.
 * By downloading or using this software you accept the terms and the liability disclaimer in the License.
*/

package com.omf.resourcecontroller.OMF;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jivesoftware.smackx.pubsub.Node;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.omf.resourcecontroller.Constants;
import com.omf.resourcecontroller.XMLgenerator.XMLGenerator;



public class OMFProxyHandlers implements Constants{

	//Properties HashMap
	HashMap<String, Object> properties;

	XMLGenerator xmlGen;
	Context ctx;
	String uid;
	//Process HashMap -- Which processes have been started from the RC
	private HashMap<String, String> ProcessesPID; 
	private HashMap<String, Process> Processes; 
	private MessagePublisher msgPub;
	private String serverName;
	private Thread tempThread;
	
	public static final String TAG = "ResourceProxyHandler";
	
	public OMFProxyHandlers(HashMap<String, Object> newProps, Context appContext, String topicName, String Server)
	{
		properties = new HashMap<String, Object> (newProps);
		xmlGen = new XMLGenerator();
		ctx = appContext;
		ProcessesPID = new HashMap<String, String>();
		Processes = new HashMap<String ,Process>();
		uid = topicName;
		msgPub = new MessagePublisher();
		serverName = Server;
	}
	
	
	/**
	 * Network Handler
	 * @param message : OMF message type
	 * @param fromTopic : from which topic the message arrived
	 * @param memberships : List of the topic memberships
	 * @param toTopic : to which topic the resource proxy replies
	 * @param nodes : HashMap of the created Nodes by the RC
	 */
	public List<String> OMFNetworkHandler(OMFMessage message, String fromTopic, List<String> memberships, String toTopic, HashMap<String, Node> nodes ){

		//Device Managers
		WifiManager wifiManager = (WifiManager)ctx.getSystemService(Context.WIFI_SERVICE);
		
		Log.e(TAG, "IN WLAN HANDLER");
		HashMap<String, Object> resourceProps =new HashMap<String, Object>(this.properties);
		//HashMap<String, Node> Nodes = new HashMap<String, Node>(nodes);  //currently unused
		RegularExpression regEx = new RegularExpression();
		
		//Variables to be used when the resource proxy has the capability to reply to request messages
		//String myUid ="xmpp://"+uid+"@"+serverName;
		//String myResType = "Network Proxy";
		
		String hrn = null;
		String key = null;
		PropType propType = null;
		String SSID = null;
		String IPaddressSubnet = null;
		String IPaddress = null;
		String subnet = null;
		String ifName = null;
		String mode = null;
		String channel = null;
		String frequency = null;
		String hwMode = null;
		String phy = null;
		String dns = null;
		String gateway = null;
		String security = null;
		String securityKey = null;
		String WifiState = null;
		
		
		if(((PropType)resourceProps.get("hrn"))!= null && ((PropType)resourceProps.get("hrn")).getType().equalsIgnoreCase("string")){
			hrn = (String)((PropType)resourceProps.get("hrn")).getProp();
			Log.e(TAG, "assign hrn: "+hrn);
		}
		
		if(((PropType)resourceProps.get("if_name"))!= null && ((PropType)resourceProps.get("if_name")).getType().equalsIgnoreCase("string")){
			ifName = (String)((PropType)resourceProps.get("if_name")).getProp();
			Log.e(TAG, "Assign interface name: " + ifName);
		}
		
		
		if(resourceProps.get("mode")!=null && ((PropType)resourceProps.get("mode")).getType().equalsIgnoreCase("hash")){
			Log.w(TAG,"In mode");
			@SuppressWarnings("unchecked")
			HashMap<String, Object> settings = new HashMap<String, Object>((HashMap<String, Object>)((PropType)resourceProps.get("mode")).getProp());
			Iterator<Entry<String, Object>> it = settings.entrySet().iterator();
			    while (it.hasNext()) {
			        Map.Entry<String, Object> pairs = (Map.Entry<String, Object>)it.next();
			       
			        key = pairs.getKey();
			        propType =(PropType) pairs.getValue();
			       // Log.w(TAG,"key: "+key);
			       // Log.w(TAG,"type: "+ propType.getType());
			        if(propType.getType().equalsIgnoreCase("string"))
			        {
			        	if(key.equalsIgnoreCase("phy"))
			        	{
			        		phy = ((String)(propType.getProp()));
			        		Log.w(TAG,"phy: " + phy);
			        	}
			        	else if(key.equalsIgnoreCase("IP_addr"))
			        	{
			        		IPaddressSubnet = ((String)(propType.getProp()));
			        		Log.w(TAG,"IP: " + IPaddressSubnet);
			        	}
			        	else if(key.equalsIgnoreCase("hw_mode"))
			        	{
			        		hwMode = ((String)(propType.getProp()));
			        		Log.w(TAG,"hw mode: "+hwMode);
			        	}
			        	else if(key.equalsIgnoreCase("essid"))
			        	{
			        		SSID = ((String)(propType.getProp()));
			        		Log.w(TAG,"ESSID: " + SSID);
			        	}
			        	else if(key.equalsIgnoreCase("channel"))
			        	{
			        		channel = ((String)(propType.getProp()));
			        		Log.w(TAG,"Channel: "+channel);
			        	}
			        	else if(key.equalsIgnoreCase("mode"))
			        	{
			        		mode = ((String)(propType.getProp()));
			        		Log.w(TAG,"Mode: "+mode);
			        	}
			        	else if(key.equalsIgnoreCase("dns"))
			        	{
			        		dns = ((String)(propType.getProp()));
			        		Log.w(TAG,"DNS: "+dns);
			        	}
			        	else if(key.equalsIgnoreCase("gateway"))
			        	{
			        		gateway = ((String)(propType.getProp()));
			        		Log.w(TAG,"Gateway: "+gateway);
			        	}
			        	else if(key.equalsIgnoreCase("security"))
			        	{
			        		security = ((String)(propType.getProp()));
			        		Log.w(TAG,"Security type: "+gateway);
			        	}
			        	else if(key.equalsIgnoreCase("security_key"))
			        	{
			        		securityKey = ((String)(propType.getProp()));
			        		Log.w(TAG,"Security key: " + securityKey);
			        	}
			        	else if(key.equalsIgnoreCase("state"))
			        	{
			        		WifiState = ((String)(propType.getProp()));
			        		Log.w(TAG,"Wifi state: " + WifiState);
			        	}
			        }
			        else if(propType.getType().equalsIgnoreCase("integer"))
			        {
			        	if(key.equalsIgnoreCase("frequency"))
			        	{
			        		//frequency is sent as an integer but i parse it as a String
			        		frequency = ((String)(propType.getProp()));
			        		Log.w(TAG,"Frequency: "+frequency);
			        	}
			        }
			        it.remove(); // avoids a ConcurrentModificationException
			    }
		
			    //Split ip and subnet
			    if(IPaddressSubnet!=null){
			    	IPaddress = regEx.ipaddressReg(IPaddressSubnet);
			    	Log.e(TAG,"IP:"+IPaddress);
			    	subnet = regEx.subnetReg(IPaddressSubnet);
			    	Log.e(TAG,"subnet:"+subnet);
			    }
			    
			    if(SSID!=null){
				    //first connect to the network then set static ip
				    WifiConfiguration newConf = new WifiConfiguration();
				    newConf.SSID = "\"" + SSID + "\""; 
				    
				    if(security!=null && securityKey!=null)
				    {
				    	if(security.equalsIgnoreCase("WEP"))
				    	{
				    		Log.e(TAG,"Wep security");
					    	newConf.wepKeys[0] = "\"" + securityKey + "\""; 
							newConf.wepTxKeyIndex = 0;
							newConf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
							newConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
				    	}
				    	else if(security.equalsIgnoreCase("WPA"))
				    	{
				    		Log.e(TAG,"Wpa security");
				    		
				    		newConf.preSharedKey = "\""+ securityKey +"\"";
				    	}
				    }
				    else{
				    	//open network
				    	Log.e(TAG,"Open network");
				    	newConf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
				    }
				    
				    Log.e(TAG,"SSID: "+SSID);
				    
				    //add network to wifi manager
				    wifiManager = (WifiManager)ctx.getSystemService(Context.WIFI_SERVICE);
				    wifiManager.addNetwork(newConf);
			    }
			    
			    if(WifiState!=null){
				    if(WifiState.equalsIgnoreCase("enabled"))
				    	wifiManager.setWifiEnabled(true);
				    else
				    	wifiManager.setWifiEnabled(false);
			    }
			   
			    
			    if((wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) || (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING))
			    {
			    	Log.e(TAG, "if wifi state disabled shouldnt reach.");
			    	//enable network
				    List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
				    for( WifiConfiguration i : list ) {
				        if(i.SSID != null && i.SSID.equals("\"" + SSID + "\"")) {
				        	Log.e(TAG,"Disconnect wifi");
				             wifiManager.disconnect();
				            Log.i(TAG,i.SSID.toString());
				             wifiManager.enableNetwork(i.networkId, true);
				            
				             Log.e(TAG,"Reconnect wifi");
				             wifiManager.reconnect();               
	
				             break;
				        }           
				    }   
				}
		}
		
		
		
		
		
		
		if(message.getMessageType().equalsIgnoreCase("create"))
		{
			
		}
		else if (message.getMessageType().equalsIgnoreCase("configure"))
		{
				
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
		
		return memberships;
	}
		
	
	
	/**
	 * Application Handler
	 * @param message : OMF message type
	 * @param fromTopic : from which topic the message arrived
	 * @param memberships : List of the topic memberships
	 * @param toTopic : to which topic the resource proxy replies
	 * @param nodes : HashMap of the created Nodes by the RC
	 */
	@SuppressWarnings("unchecked")
	public List<String> OMFApplicationHandler(OMFMessage message, String fromTopic, List<String> memberships, String toTopic, HashMap<String, Node> nodes){
		
		Log.e(TAG, "IN APP HANDLER");
		HashMap<String, Object> resourceProps =new HashMap<String, Object>(this.properties);
		HashMap<String, Node> Nodes = new HashMap<String, Node>(nodes);
		
		RegularExpression regEx = new RegularExpression();
		String commandLineStart = null;
		String appDescription = null;
		String binPath = null;
		String packageName = null;
		String serviceName = null;
		String hrn = null;
		//String uid = null;
		//String myUid ="xmpp://"+uid+"@"+serverName; //currently unused
		//String myResType = "Application Proxy";	//currently unused
		
		
		String platform = "android_shell";
		Process p = null;
		Log.e(TAG, "i LISTEN TO: " +fromTopic);
		Log.i(TAG,resourceProps.toString());
		Log.e(TAG,"i SEND TO:" + toTopic);
		if(((PropType)resourceProps.get("platform"))!= null){
			platform = (String)((PropType)resourceProps.get("platform")).getProp();
			Log.e(TAG, "IN PLATFORM ASSIGNMENT");
		}
		
		
		if(((PropType)resourceProps.get("hrn"))!= null && ((PropType)resourceProps.get("hrn")).getType().equalsIgnoreCase("string")){
			hrn = (String)((PropType)resourceProps.get("hrn")).getProp();
			Log.e(TAG, "assign hrn");
		}
		
		
		Intent tmpintent = new Intent();
		Log.e(TAG, "IS PLATFORM ANDROID = "+platform);
		
		//Assuming these properties are standard
		if(platform.equalsIgnoreCase("android"))
		{
			if(resourceProps.get("description")!=null)
			{
				appDescription = (String)((PropType)resourceProps.get("description")).getProp();
				Log.e(TAG, appDescription +" uid:"+ this.uid);
			}
			//appId = (String)((PropType)resourceProps.get("app_id")).getProp();
			packageName = (String)((PropType)((HashMap<String, Object>)((PropType)resourceProps.get("binary_path")).getProp()).get("package")).getProp();
			serviceName = (String)((PropType)((HashMap<String, Object>)((PropType)resourceProps.get("binary_path")).getProp()).get("service")).getProp();
			
			
			Log.e(TAG,"NAME:"+ packageName + "." + serviceName);
			
			if(packageName!=null || serviceName!=null)
				tmpintent.setClassName(packageName, packageName + "." + serviceName);
			else
				Log.e(TAG, "Error getting service and package name");

			String key = null;
			PropType propType = null;
			p = null;
			
	
			Log.e(TAG, "Before loop");
			if(resourceProps.get("parameters")!=null){
				HashMap<String, Object> params = new HashMap<String, Object>((HashMap<String, Object>)((PropType)resourceProps.get("parameters")).getProp());
				Iterator<Entry<String, Object>> it = params.entrySet().iterator();
				    while (it.hasNext()) {
				        Map.Entry<String, Object> pairs = (Map.Entry<String, Object>)it.next();
				       
				        key = pairs.getKey();
				        propType =(PropType) pairs.getValue();

				        if(propType.getType().equalsIgnoreCase("hash"))
				        {
				        	HashMap<String,Object> parameter = (HashMap<String,Object>)propType.getProp();
      	
				        	if(parameter.get("type")!=null && (((PropType)parameter.get("value")).getType().equalsIgnoreCase("string") || ((PropType)parameter.get("value")).getType().equalsIgnoreCase("integer")))
				        	{
				        		if(((String)((PropType)parameter.get("type")).getProp()).equalsIgnoreCase("EXTRA"))
				        		{
				        			Log.e(TAG,key+":"+((String)((PropType)parameter.get("value")).getProp()));
				        		 	tmpintent.putExtra(key, ((String)((PropType)parameter.get("value")).getProp()));
				        		}
				        	}
				        }
				        it.remove(); // avoids a ConcurrentModificationException
				    }
			}
		}
		else
		{
			
			if(resourceProps.get("description")!=null)
			{
				appDescription = (String)((PropType)resourceProps.get("description")).getProp();
				Log.e(TAG, appDescription);
			}
			
			binPath = (String)((PropType)resourceProps.get("binary_path")).getProp();
			
			commandLineStart = binPath;
			//commandLineStop = "am broadcast -a "+ binPath+".SERVICESTOP";
			//String commandLineStop = "am force-stop "+binPath;
			//String key = null;
			PropType propType = null;
			p = null;
			
			Log.e(TAG, commandLineStart);
		
			
			Log.e(TAG, "Before loop");
			if(resourceProps.get("parameters")!=null){
				HashMap<String, Object> params = new HashMap<String, Object>((HashMap<String, Object>)((PropType)resourceProps.get("parameters")).getProp());
				Iterator<Entry<String, Object>> it = params.entrySet().iterator();
				    while (it.hasNext()) {
				        Map.Entry<String, Object> pairs = (Map.Entry<String, Object>)it.next();
				        //System.out.println(pairs.getKey() + " = " + pairs.getValue());
				        
				        //key = pairs.getKey();
				        propType =(PropType) pairs.getValue();
				        //Log.e(TAG,"In Loop");
				        //Log.e(TAG,"Found: "+key);
				        //Log.e(TAG,"Found: "+propType.getType().toString());
				        //Log.e(TAG,"Found: "+propType.getProp().toString());
				        
				        if(propType.getType().equalsIgnoreCase("hash"))
				        {
				        	HashMap<String,Object> parameter = (HashMap<String,Object>)propType.getProp();
				        	
				        	
				        	if(parameter.get("type")!=null && ((PropType)parameter.get("cmd")).getType().equalsIgnoreCase("string"))
				        	{
				        		//Log.e(TAG,"Cmd: "+((String)((PropType)parameter.get("cmd")).getProp()));
				        		commandLineStart+=" "+((String)((PropType)parameter.get("cmd")).getProp());
				        	}	
				        	
				        	if(parameter.get("type")!=null && (((PropType)parameter.get("value")).getType().equalsIgnoreCase("string") || ((PropType)parameter.get("value")).getType().equalsIgnoreCase("integer")))
				        	{
				        		//Log.e(TAG,"Value: "+((String)((PropType)parameter.get("value")).getProp()));
				        		commandLineStart+=" "+((String)((PropType)parameter.get("value")).getProp());
				        	}
				        }
				        it.remove(); // avoids a ConcurrentModificationException
				    }
				    Log.e(TAG, "Command ready: "+commandLineStart);
			}
		
		}
			
		if(message.getMessageType().equalsIgnoreCase("create"))
		{
			Log.i(TAG,fromTopic+": "+"create");
		}
		else if (message.getMessageType().equalsIgnoreCase("configure"))
		{
			Log.i(TAG,fromTopic+": "+"Configure");
			
			if(message.getGuard("type")==null || ((String)((PropType)message.getGuard("type")).getProp()).equalsIgnoreCase((String)((PropType)resourceProps.get("type")).getProp())){
				Log.i(TAG, "Passed guard: Type");
				if(message.getGuard("name")==null || ((String)((PropType)message.getGuard("name")).getProp()).equalsIgnoreCase((String)((PropType)resourceProps.get("hrn")).getProp())){
					Log.i(TAG, "Passed guard: name");
					if(message.getProperty("state")!=null)
					{
						PropType prop = (PropType)message.getProperty("state");
						
						if(prop.getType().equalsIgnoreCase("string"))
						{
		
							if(((String)prop.getProp()).equalsIgnoreCase("running"))
							{
								Log.i(TAG,"Starting application");
								
								if(platform.equalsIgnoreCase("android"))
								{
									Log.i(TAG, "Starting intent");
									ctx.startService(tmpintent);
								}
								else{
									try {
										Log.i(TAG,"Executing command");
										p = Runtime.getRuntime().exec(commandLineStart);
										
										
										ProcessesPID.put(fromTopic, regEx.pidReg(p.toString()));
										Processes.put(fromTopic, p);
										BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
										//String line=bufferedReader.readLine();
										String line = "";
										//Log.i(TAG,"Process:"+p);
										//Log.i(TAG,"Line 1st output:"+line);
										int i = 1;
										
										//Output from the process needs sto run on a different thread otherwise it blocks the RC from handling messages
										class MyThread implements Runnable {
											String line;
											BufferedReader bufferedReader;
											int i;
											String hrn;
											String uid;
											String fromTopic;
											HashMap<String, Node> Nodes;
											String serverName;
											   public MyThread(BufferedReader BufferedReader, String Line, int I, String Hrn, String Uid, String FromTopic, HashMap<String, Node> nodes, String ServerName) {
											       // store parameter for later user
												   bufferedReader = BufferedReader;
												   line = Line;
												   i = I;
												   hrn = Hrn;
												   uid = Uid;
												   fromTopic = FromTopic;
												   Nodes = new HashMap<String, Node> (nodes);
												   serverName = ServerName;
											   }

											   public void run() {
												   try {
													while ((line = bufferedReader.readLine()) != null) 
														{
															//Log.w(TAG, "Shell output: "+line);
															if(!line.equalsIgnoreCase(""))
															{
																//Log.w(TAG, "Shell output inside: "+line);
																
																HashMap<String, Object> props= new HashMap<String, Object>();
																props = xmlGen.addProperties(props, "status_type", new PropType("APP_EVENT","string"));
																props = xmlGen.addProperties(props, "event", new PropType("STDOUT","string"));
																props = xmlGen.addProperties(props, "app", new PropType(hrn,"string"));
																
																//use trim because some outputs have trailing whitespaces and the connection closes because of that
																props = xmlGen.addProperties(props, "msg", new PropType(line.trim(),"string")); 
																props = xmlGen.addProperties(props, "seq", new PropType(String.valueOf(i),"string"));
																props = xmlGen.addProperties(props, "uid", new PropType(this.uid,"string"));
																props = xmlGen.addProperties(props, "hrn", new PropType(hrn,"string"));
																
																//Log.e(TAG,fromTopic+" "+serverName+" "+props.toString());
																String xmlPayload = xmlGen.informMessage(fromTopic, serverName, null, "STATUS", props);
																//Log.e(TAG,xmlPayload);
																msgPub.PublishItem(xmlPayload, SCHEMA, "inform", Nodes.get(fromTopic));
																i++;
																
															}
														}
												} catch (IOException e) {
													Log.e(TAG,"Thread exception");
													e.printStackTrace();
												}
											   }
										}
										
										Runnable r = new MyThread(bufferedReader, line, i, hrn, this.uid, fromTopic, Nodes, serverName);
										tempThread = new Thread(r);
										tempThread.start();
										
									} catch (IOException e) {
										Log.e(TAG, "Command not found");
										e.printStackTrace();
									}
								}
							}
							
							if(((String)prop.getProp()).equalsIgnoreCase("stopped"))
							{
								Log.w(TAG,"Stoping background service");
								if(platform.equalsIgnoreCase("android"))
								{
									ctx.stopService(tmpintent);
								}
								else
								{
									try {
										
										String pid = ProcessesPID.get(fromTopic);
										//Process proc = Processes.get(fromTopic); //currently unused
										Log.i(TAG,"Kill procces:" +pid);
											
											p=Runtime.getRuntime().exec("su -c kill -9 "+pid); //kill process -- requires rooted device otherwise process will continue to run
											
											tempThread.stop();	//stop thread
											Log.e(TAG, "Process with PID: "+pid+" destroyed");
											ProcessesPID.remove(fromTopic);
											Processes.remove(fromTopic);
										
										BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
										String line;
										while ((line = bufferedReader.readLine()) != null) 
										{
											Log.i(TAG, "Shell output: "+line);
										}
									} catch (IOException e) {
										Log.e(TAG, "Service not found!");
										e.printStackTrace();
									}
								}
							}
						}
					}
					else if(message.getProperty("parameters")!=null)
					{
						Log.e(TAG,"Changing properties");
						this.properties =new HashMap<String, Object>(message.getPropertiesHashmap());
						resourceProps =new HashMap<String, Object>(this.properties);
						Log.e(TAG,this.properties.toString());
						
						String key = null;
						PropType propType = null;
						p = null;
						
						if(platform.equalsIgnoreCase("android"))
						{
							Intent broadcastIntent = new Intent();
							HashMap<String, Object> params = new HashMap<String, Object>((HashMap<String, Object>)((PropType)resourceProps.get("parameters")).getProp());
							Iterator<Entry<String, Object>> it = params.entrySet().iterator();
							    while (it.hasNext()) {
							        Map.Entry<String, Object> pairs = (Map.Entry<String, Object>)it.next();
							       
							        key = pairs.getKey();
							        propType =(PropType) pairs.getValue();

							        if(propType.getType().equalsIgnoreCase("hash"))
							        {
							        	HashMap<String,Object> parameter = (HashMap<String,Object>)propType.getProp();
			      	
							        	if(parameter.get("type")!=null && (((PropType)parameter.get("value")).getType().equalsIgnoreCase("string") || ((PropType)parameter.get("value")).getType().equalsIgnoreCase("integer")))
							        	{
							        		if(((String)((PropType)parameter.get("type")).getProp()).equalsIgnoreCase("EXTRA"))
							        		{
							        		 	//tmpintent.putExtra(key, ((String)((PropType)parameter.get("value")).getProp()));
							        			if(parameter.get("dynamic")!=null)
							        			{
							        				if(((String)((PropType)parameter.get("dynamic")).getProp()).equalsIgnoreCase("true"))
							        				{
							        					//intent broadcast set action & put extra
							        					if(parameter.get("action")!=null)
							        					{
							        						broadcastIntent.setPackage(packageName);
							        						String action = packageName+"."+(String)((PropType)parameter.get("action")).getProp();
							        						Log.e(TAG, action);
							        						broadcastIntent.setAction(action);
							        						String configuredValue = ((String)((PropType)parameter.get("value")).getProp());
							        						Log.e(TAG, key+": "+configuredValue);
							        						broadcastIntent.putExtra(key, configuredValue);
							        						
							        					}
							        				}
							        			}
							        		}
							        	}
							        }
							        it.remove(); // avoids a ConcurrentModificationException
							    }
							    //send broadcast here
							    ctx.sendBroadcast(broadcastIntent);
						}
					}
					else
					{
						//nothing
					}
				}
			}
		}
		else if (message.getMessageType().equalsIgnoreCase("request"))
		{
			Log.i(TAG,fromTopic+": "+"request");
		}
		else if (message.getMessageType().equalsIgnoreCase("inform"))
		{
			Log.i(TAG,fromTopic+": "+"inform");
		}
		else if (message.getMessageType().equalsIgnoreCase("release"))
		{
			Log.i(TAG,fromTopic+": "+"release");
		}
		
		return memberships;
	}
	
	
	

		    
		    public String getScanResultSecurity(ScanResult scanResult) {
		        Log.i(TAG, "* getScanResultSecurity");

		        final String cap = scanResult.capabilities;
		        final String[] securityModes = { "WEP", "PSK", "EAP" };

		        for (int i = securityModes.length - 1; i >= 0; i--) {
		            if (cap.contains(securityModes[i])) {
		                return securityModes[i];
		            }
		        }

		        return "OPEN";
		    }
}

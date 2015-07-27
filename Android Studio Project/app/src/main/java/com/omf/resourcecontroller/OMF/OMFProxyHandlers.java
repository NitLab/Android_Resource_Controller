/* Copyright (c) 2013 NITLab, University of Thessaly, Greece
 * This software may be used and distributed solely under the terms of the MIT license (License).
 * You should find a copy of the License in LICENSE.TXT or at http://opensource.org/licenses/MIT.
 * By downloading or using this software you accept the terms and the liability disclaimer in the License.
*/

package com.omf.resourcecontroller.OMF;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jivesoftware.smackx.pubsub.Node;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.omf.resourcecontroller.Constants;
import com.omf.resourcecontroller.generator.PropertiesGenerator;
import com.omf.resourcecontroller.generator.XMLGenerator;



public class OMFProxyHandlers implements Constants {

	//Properties HashMap
	HashMap<String, Object> properties;

	XMLGenerator xmlGen;
	PropertiesGenerator propGen;
	Context ctx;
	String uid;
	//Process HashMap -- Which processes have been started from the RC
	private HashMap<String, String> ProcessesPID; 
	private HashMap<String, Process> Processes; 
	private MessagePublisher msgPub;
	private String serverName;
	private Thread tempThread;
	//SharedPreferences settings = getSharedPreferences("ConnectionSettings", Context.MODE_PRIVATE);
	public static final String appTAG = "omf.resourcecontroller";
	public static final String classTAG = "ResourceProxyHandler";
	
	public OMFProxyHandlers(HashMap<String, Object> newProps, Context appContext, String topicName, String Server)
	{
		
		String connType = appContext.getSharedPreferences("ConnectionSettings", Context.MODE_PRIVATE).getString("connectionType", "XMPP");//Default XMPP
		properties = new HashMap<String, Object> (newProps);
		xmlGen = new XMLGenerator();
		propGen = new PropertiesGenerator();
		ctx = appContext;
		ProcessesPID = new HashMap<String, String>();
		Processes = new HashMap<String ,Process>();
		uid = topicName;
		msgPub = new MessagePublisher(connType);
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
		
		Log.i(appTAG, classTAG+": IN WLAN HANDLER");
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
			Log.i(appTAG, classTAG+": assign hrn: "+hrn);
		}
		
		if(((PropType)resourceProps.get("if_name"))!= null && ((PropType)resourceProps.get("if_name")).getType().equalsIgnoreCase("string")){
			ifName = (String)((PropType)resourceProps.get("if_name")).getProp();
			Log.i(appTAG, classTAG+": Assign interface name: " + ifName);
		}
		
		
		if(resourceProps.get("mode")!=null && ((PropType)resourceProps.get("mode")).getType().equalsIgnoreCase("hash")){
			Log.w(appTAG,classTAG+": In mode");
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
			        		Log.w(appTAG,classTAG+": phy: " + phy);
			        	}
			        	else if(key.equalsIgnoreCase("IP_addr"))
			        	{
			        		IPaddressSubnet = ((String)(propType.getProp()));
			        		Log.w(appTAG,classTAG+": IP: " + IPaddressSubnet);
			        	}
			        	else if(key.equalsIgnoreCase("hw_mode"))
			        	{
			        		hwMode = ((String)(propType.getProp()));
			        		Log.w(appTAG,classTAG+": hw mode: "+hwMode);
			        	}
			        	else if(key.equalsIgnoreCase("essid"))
			        	{
			        		SSID = ((String)(propType.getProp()));
			        		Log.w(appTAG,classTAG+": ESSID: " + SSID);
			        	}
			        	else if(key.equalsIgnoreCase("channel"))
			        	{
			        		channel = ((String)(propType.getProp()));
			        		Log.w(appTAG,classTAG+": Channel: "+channel);
			        	}
			        	else if(key.equalsIgnoreCase("mode"))
			        	{
			        		mode = ((String)(propType.getProp()));
			        		Log.w(appTAG,classTAG+": Mode: "+mode);
			        	}
			        	else if(key.equalsIgnoreCase("dns"))
			        	{
			        		dns = ((String)(propType.getProp()));
			        		Log.w(appTAG,classTAG+": DNS: "+dns);
			        	}
			        	else if(key.equalsIgnoreCase("gateway"))
			        	{
			        		gateway = ((String)(propType.getProp()));
			        		Log.w(appTAG,classTAG+": Gateway: "+gateway);
			        	}
			        	else if(key.equalsIgnoreCase("security"))
			        	{
			        		security = ((String)(propType.getProp()));
			        		Log.w(appTAG,classTAG+": Security type: "+gateway);
			        	}
			        	else if(key.equalsIgnoreCase("security_key"))
			        	{
			        		securityKey = ((String)(propType.getProp()));
			        		Log.w(appTAG,classTAG+": Security key: " + securityKey);
			        	}
			        	else if(key.equalsIgnoreCase("state"))
			        	{
			        		WifiState = ((String)(propType.getProp()));
			        		Log.w(appTAG,classTAG+": Wifi state: " + WifiState);
			        	}
			        }
			        else if(propType.getType().equalsIgnoreCase("integer"))
			        {
			        	if(key.equalsIgnoreCase("frequency"))
			        	{
			        		//frequency is sent as an integer but i parse it as a String
			        		frequency = ((String)(propType.getProp()));
			        		Log.w(appTAG,classTAG+": Frequency: "+frequency);
			        	}
			        }
			        it.remove(); // avoids a ConcurrentModificationException
			    }
		
			    
			    
			    if(WifiState!=null){
				    if(WifiState.equalsIgnoreCase("enabled"))
				    	wifiManager.setWifiEnabled(true);
				    else
				    	wifiManager.setWifiEnabled(false);
			    }
			    
			    if(SSID!=null){
				    //first connect to the network then set static ip
				    WifiConfiguration newConf = new WifiConfiguration();
				    newConf.SSID = "\"" + SSID + "\""; 
				    
				    if(security!=null && securityKey!=null)
				    {
				    	if(security.equalsIgnoreCase("WEP"))
				    	{
				    		Log.i(appTAG,classTAG+": Wep security");
					    	newConf.wepKeys[0] = "\"" + securityKey + "\""; 
							newConf.wepTxKeyIndex = 0;
							newConf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
							newConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
				    	}
				    	else if(security.equalsIgnoreCase("WPA"))
				    	{
				    		Log.i(appTAG,classTAG+": Wpa security");
				    		
				    		newConf.preSharedKey = "\""+ securityKey +"\"";
				    		
				    	}
				    }
				    else{
				    	//open network
				    	Log.i(appTAG,classTAG+": Open network");
				    	newConf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
				    }
				    
				    Log.i(appTAG,classTAG+": SSID: "+SSID);
				    
				    //SET STATIC IP ETC in here
				    if(IPaddressSubnet!=null){
				    	IPaddress = regEx.ipaddressReg(IPaddressSubnet);
				    	Log.i(appTAG,classTAG+": IP:"+IPaddress);
				    	subnet = regEx.subnetReg(IPaddressSubnet);
				    	Log.i(appTAG,classTAG+": subnet:"+subnet);
				    	
				    	
				        
				        
				        try{
				            setIpAssignment("STATIC", newConf); //or "DHCP" for dynamic setting
				            setIpAddress(InetAddress.getByName(IPaddress), Integer.parseInt(subnet), newConf);
				            setGateway(InetAddress.getByName(gateway), newConf);
				            setDNS(InetAddress.getByName(dns), newConf);
				            //wifiManager.updateNetwork(newConf); //apply the setting
				            //wifiManager.saveConfiguration(); //Save it
				        }catch(Exception e){
				            e.printStackTrace();
				        }
			    	}
				    else{
				    	
				    	//DHCP??
				    	try{
				            setIpAssignment("DHCP", newConf); //or "DHCP" for dynamic setting
				            
				            //wifiManager.updateNetwork(newConf); //apply the setting
				            //wifiManager.saveConfiguration(); //Save it
				        }catch(Exception e){
				            e.printStackTrace();
				        }
				    }
				    //add network to wifi manager
				    wifiManager = (WifiManager)ctx.getSystemService(Context.WIFI_SERVICE);
				    
				    
				    //Check if network exists in wifiConfiguration
				    //Check boolean
				    if(checkPreviousConfiguration(wifiManager,SSID))
				    {
				    	wifiManager.updateNetwork(newConf);
				    }
				    else
				    {
				    	wifiManager.addNetwork(newConf);
				    }
				    
				    //dont know which one works update or add
				    
				    wifiManager.saveConfiguration();
			    }
			    
			    
			   
			    
			    if((wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) || (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING))
			    {
			    	Log.i(appTAG, classTAG+": if wifi state disabled shouldnt reach.");
			    	//enable network
				    List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
				    Log.i(appTAG,classTAG+": WIFI configured networks: "+list.toString());
				    for( WifiConfiguration i : list ) {
				        if(i.SSID != null && i.SSID.equals("\"" + SSID + "\"")) {
				        	Log.i(appTAG,classTAG+": Disconnect wifi");
				             wifiManager.disconnect();
				            Log.i(appTAG,classTAG+": " +i.SSID.toString());
				             wifiManager.enableNetwork(i.networkId, true);
				            
				             Log.i(appTAG,classTAG+": Reconnect wifi");
				             wifiManager.reconnect();               
	
				             break;
				        }           
				    }   
				}
			    
			    
			  //Split ip and subnet
			    /*if(IPaddressSubnet!=null){
			    	IPaddress = regEx.ipaddressReg(IPaddressSubnet);
			    	Log.i(appTAG,classTAG+": IP:"+IPaddress);
			    	subnet = regEx.subnetReg(IPaddressSubnet);
			    	Log.i(appTAG,classTAG+": subnet:"+subnet);
			    	
			    	WifiConfiguration wifiConf = null;
			    	WifiInfo connectionInfo = wifiManager.getConnectionInfo();
			        List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();        
			        for (WifiConfiguration conf : configuredNetworks){
			            if (conf.networkId == connectionInfo.getNetworkId()){
			                wifiConf = conf;
			                break;              
			            }
			        }
			    	
			        
			        
			        try{
			            setIpAssignment("STATIC", wifiConf); //or "DHCP" for dynamic setting
			            setIpAddress(InetAddress.getByName(IPaddress), Integer.parseInt(subnet), wifiConf);
			            setGateway(InetAddress.getByName(gateway), wifiConf);
			            setDNS(InetAddress.getByName(dns), wifiConf);
			            wifiManager.updateNetwork(wifiConf); //apply the setting
			            wifiManager.saveConfiguration(); //Save it
			        }catch(Exception e){
			            e.printStackTrace();
			        }
			    }*/
		}
		
		
		
		
		
		
		if(message.getMessageType().equalsIgnoreCase("create"))
		{
			
		}
		else if (message.getMessageType().equalsIgnoreCase("configure"))
		{
				//na kanw configure to interface otan pairnw wlan gia na upostirizw to settarisma tis IP
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
	@SuppressWarnings({ "unchecked" })
	public List<String> OMFApplicationHandler(OMFMessage message, String fromTopic, List<String> memberships, String toTopic, HashMap<String, Node> nodes) throws IllegalArgumentException {
		
		Log.i(appTAG, classTAG+": IN APP HANDLER");
		HashMap<String, Object> resourceProps =new HashMap<String, Object>(this.properties);
		HashMap<String, Node> Nodes = new HashMap<String, Node>(nodes);
		
		RegularExpression regEx = new RegularExpression();
		String commandLineStart = null;
		String appDescription = null;
		String binPath = null;
		String packageName = null;
		String serviceName = null;
		String actionName = null;
		String hrn = null;
		Intent tmpintent = null;
		//String uid = null;
		//String myUid ="xmpp://"+uid+"@"+serverName; //currently unused
		//String myResType = "Application Proxy";	//currently unused
		
		
		String platform = "android_shell";
		Process p = null;
		Log.i(appTAG, classTAG+": i LISTEN TO: " +fromTopic);
		Log.i(appTAG,classTAG+": "+resourceProps.toString());
		Log.i(appTAG,classTAG+": i SEND TO:" + toTopic);
		if(((PropType)resourceProps.get("platform"))!= null){
			platform = (String)((PropType)resourceProps.get("platform")).getProp();
			Log.i(appTAG, classTAG+": IN PLATFORM ASSIGNMENT");
		}
		
		
		if(((PropType)resourceProps.get("hrn"))!= null && ((PropType)resourceProps.get("hrn")).getType().equalsIgnoreCase("string")){
			hrn = (String)((PropType)resourceProps.get("hrn")).getProp();
			Log.i(appTAG, classTAG+": assign hrn");
		}
		
		Log.i(appTAG, classTAG+": IS PLATFORM ANDROID = "+platform);
		
		//Assuming these properties are standard
		if(platform.equalsIgnoreCase("android"))
		{
			if(resourceProps.get("description")!=null)
			{
				appDescription = (String)((PropType)resourceProps.get("description")).getProp();
				Log.i(appTAG, classTAG+": "+appDescription +" uid:"+ this.uid);
			}
			
			//appId = (String)((PropType)resourceProps.get("app_id")).getProp();
			HashMap<String, Object> binary_path = (HashMap<String, Object>)((PropType)resourceProps.get("binary_path")).getProp();
			
			if (binary_path.get("package") != null) {
				packageName = (String)((PropType) binary_path.get("package")).getProp();
			} else {
				Log.e(appTAG, classTAG+": Error getting package name.");
				throw new IllegalArgumentException("You need to specify a package name.");
			}
			
			if (binary_path.get("service") != null) {
				serviceName = (String)((PropType) binary_path.get("service")).getProp();
			}
			
			if (binary_path.get("action") != null) {
				actionName = (String)((PropType) binary_path.get("action")).getProp();
			}
			
			Log.w(appTAG,classTAG+": NAME:"+ packageName + "." + serviceName + " " + actionName);
			
			if (serviceName != null && actionName == null) {
				tmpintent = new Intent();
				tmpintent.setClassName(packageName, packageName + "." + serviceName);
			} else if (serviceName == null && actionName != null) {
				try {
					tmpintent = new Intent((String)Intent.class.getField(actionName).get(tmpintent));
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchFieldException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//tmpintent = new Intent(Intent.ACTION_VIEW);
				
				tmpintent.setPackage(packageName);
			} else {
				Log.e(appTAG, classTAG+": Error getting service or action name");
				throw new IllegalArgumentException("You need to specify package a service or an action name.");
			}
			

			String key = null;
			PropType propType = null;
			p = null;
			
	
			Log.i(appTAG, classTAG+": Before loop");
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
						Log.w(appTAG,classTAG+":TYPE :"+parameter.get("type"));
						Log.w(appTAG,classTAG+":EXTRA :"+parameter.get("extra"));
			        	if(parameter.get("extra")!=null && (((PropType)parameter.get("value")).getType().equalsIgnoreCase("string") || ((PropType)parameter.get("value")).getType().equalsIgnoreCase("integer")))
			        	{
			        		if(((String)((PropType)parameter.get("extra")).getProp()).equalsIgnoreCase("parameter"))//CHANGED HERE!!
			        		{
			        			Log.w(appTAG,classTAG+": "+key+":"+((String)((PropType)parameter.get("value")).getProp()));
			        		 	tmpintent.putExtra(key, ((String)((PropType)parameter.get("value")).getProp()));
			        		} 
			        	} else if ((parameter.get("extra") != null) && ((String)((PropType)parameter.get("extra")).getProp()).equalsIgnoreCase("INTENT")) {
			        		//Log.e(TAG,key+":"+((String)((PropType)parameter.get("value")).getProp()));
			        		if (key.equalsIgnoreCase("data_and_type")) {
		        				//Log.e(TAG,key+":"+((String)((PropType)parameter.get("value")).getProp()));
		        				HashMap<String, Object> data_type = (HashMap<String, Object>)((PropType)(parameter.get("value"))).getProp();
		        				Uri data = Uri.parse((String)((PropType)data_type.get("data")).getProp());
		        				String type = (String)((PropType)data_type.get("extra")).getProp();
		        				
		        				Log.i(appTAG,classTAG+": "+data+":"+type);
		        				tmpintent.setDataAndType(data, type);
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
				Log.i(appTAG, classTAG+": "+appDescription);
			}
			
			binPath = (String)((PropType)resourceProps.get("binary_path")).getProp();
			
			commandLineStart = binPath;
			//commandLineStop = "am broadcast -a "+ binPath+".SERVICESTOP";
			//String commandLineStop = "am force-stop "+binPath;
			//String key = null;
			PropType propType = null;
			p = null;
			
			Log.i(appTAG, classTAG+": "+commandLineStart);
		
			
			Log.i(appTAG, classTAG+": Before loop");
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
				        	
				        	
				        	if(parameter.get("type")!=null && ((PropType)parameter.get("cmd")).getType().equalsIgnoreCase("string")) //Maybe change needed here!!! change "type" to "extra"
				        	{
				        		//Log.e(TAG,"Cmd: "+((String)((PropType)parameter.get("cmd")).getProp()));
				        		commandLineStart+=" "+((String)((PropType)parameter.get("cmd")).getProp());
				        	}
                            //Maybe change needed here!!! change "type" to "extra"
				        	if(parameter.get("type")!=null && (((PropType)parameter.get("value")).getType().equalsIgnoreCase("string") || ((PropType)parameter.get("value")).getType().equalsIgnoreCase("integer")))
				        	{
				        		//Log.e(TAG,"Value: "+((String)((PropType)parameter.get("value")).getProp()));
				        		commandLineStart+=" "+((String)((PropType)parameter.get("value")).getProp());
				        	}
				        }
				        it.remove(); // avoids a ConcurrentModificationException
				    }
				    Log.i(appTAG, classTAG+": Command ready: "+commandLineStart);
			}
		
		}
			
		if(message.getMessageType().equalsIgnoreCase("create"))
		{
			Log.i(appTAG,classTAG+": "+fromTopic+": "+"create");
		}
		else if (message.getMessageType().equalsIgnoreCase("configure"))
		{
			Log.i(appTAG,classTAG+": "+fromTopic+": "+"Configure");
			
			if(message.getGuard("type")==null || ((String)((PropType)message.getGuard("type")).getProp()).equalsIgnoreCase((String)((PropType)resourceProps.get("type")).getProp())){
				Log.i(appTAG, classTAG+": Passed guard: Type");
				if(message.getGuard("name")==null || ((String)((PropType)message.getGuard("name")).getProp()).equalsIgnoreCase((String)((PropType)resourceProps.get("hrn")).getProp())){
					Log.i(appTAG, classTAG+": Passed guard: name");
					if(message.getProperty("state")!=null)
					{
						PropType prop = (PropType)message.getProperty("state");
						
						if(prop.getType().equalsIgnoreCase("string"))
						{
		
							if(((String)prop.getProp()).equalsIgnoreCase("running"))
							{
								Log.i(appTAG,classTAG+": Starting application");
								
								if(platform.equalsIgnoreCase("android") && tmpintent != null)
								{
									if (serviceName != null) {
										Log.i(appTAG, classTAG+": Starting intent as a service");
										ctx.startService(tmpintent);
									} else if (actionName != null) {
										Log.i(appTAG, classTAG+": Starting intent as an activity");
										tmpintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
										ctx.startActivity(tmpintent);
									}
								}
								else{
									try {
										Log.i(appTAG,classTAG+": Executing command");
										p = Runtime.getRuntime().exec(commandLineStart);
										
										
										ProcessesPID.put(fromTopic, regEx.pidReg(p.toString()));
										Processes.put(fromTopic, p);
										BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
										//String line=bufferedReader.readLine();
										String line = "";
										//Log.i(TAG,"Process:"+p);
										//Log.i(TAG,"Line 1st output:"+line);
										int i = 1;
										
										//Output from the process needs to run on a different thread otherwise it blocks the RC from handling messages
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
																props = propGen.addProperties(props, "status_type", new PropType("APP_EVENT","string"));
																props = propGen.addProperties(props, "event", new PropType("STDOUT","string"));
																props = propGen.addProperties(props, "app", new PropType(hrn,"string"));
																
																//use trim because some outputs have trailing whitespaces and the connection closes because of that
																props = propGen.addProperties(props, "msg", new PropType(line.trim(),"string")); 
																props = propGen.addProperties(props, "seq", new PropType(String.valueOf(i),"string"));
																props = propGen.addProperties(props, "uid", new PropType(this.uid,"string"));
																props = propGen.addProperties(props, "hrn", new PropType(hrn,"string"));
																
																
																//************Auta edw den prepei na einai mono gia to XMPP******************
																//Log.e(TAG,fromTopic+" "+serverName+" "+props.toString());
																OMFMessage genOMFmessage = xmlGen.informMessage(fromTopic, serverName, null, "STATUS", props);
																//Log.e(TAG,xmlPayload);
																msgPub.PublishItem(genOMFmessage, "inform", Nodes.get(fromTopic));
																//***********Auta edw den prepei na einai mono gia to XMPP********************
																i++;
																
															}
														}
												} catch (IOException e) {
													Log.e(appTAG,classTAG+": Thread exception");
													e.printStackTrace();
												}
											   }
										}
										
										Runnable r = new MyThread(bufferedReader, line, i, hrn, this.uid, fromTopic, Nodes, serverName);
										tempThread = new Thread(r);
										tempThread.start();
										
									} catch (IOException e) {
										Log.e(appTAG, classTAG+": Command not found");
										e.printStackTrace();
									}
								}
							}
							
							if(((String)prop.getProp()).equalsIgnoreCase("stopped"))
							{	
								if(platform.equalsIgnoreCase("android"))
								{
									if (serviceName != null) {
										Log.w(appTAG,classTAG+": Stoping background service");
										ctx.stopService(tmpintent);
									}
								}
								else
								{
									try {
										
										String pid = ProcessesPID.get(fromTopic);
										//Process proc = Processes.get(fromTopic); //currently unused
										Log.i(appTAG,classTAG+": Kill procces:" +pid);
											
											//
											
											p=Runtime.getRuntime().exec("su -c kill -9 "+pid); //kill process -- requires rooted device otherwise process will continue to run
											//tempThread.stop();	//stop thread
											tempThread.interrupt();
											tempThread = null;
											
											Log.w(appTAG, classTAG+": Executing command:"+ "su -c \"kill -9 "+pid+"\"");
											Log.w(appTAG, classTAG+": Process with PID: "+pid+" destroyed");
											ProcessesPID.remove(fromTopic);
											Processes.remove(fromTopic);
										
										BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
										String line;
										while ((line = bufferedReader.readLine()) != null) 
										{
											Log.i(appTAG, classTAG+": Shell output: "+line);
										}
									} catch (IOException e) {
										Log.e(appTAG, classTAG+": Process not found!");
										e.printStackTrace();
									}
								}
							}
						}
					}
					else if(message.getProperty("parameters")!=null)
					{
						Log.w(appTAG,classTAG+": Changing properties");
						this.properties =new HashMap<String, Object>(message.getPropertiesHashmap());
						resourceProps =new HashMap<String, Object>(this.properties);
						Log.i(appTAG,classTAG+": "+this.properties.toString());
						
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

							        	if(parameter.get("extra")!=null && (((PropType)parameter.get("value")).getType().equalsIgnoreCase("string") || ((PropType)parameter.get("value")).getType().equalsIgnoreCase("integer")))
							        	{
							        		if(((String)((PropType)parameter.get("extra")).getProp()).equalsIgnoreCase("parameter"))//CHANGED HERE !!
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
							        						Log.i(appTAG,classTAG+": "+ action);
							        						broadcastIntent.setAction(action);
							        						String configuredValue = ((String)((PropType)parameter.get("value")).getProp());
							        						Log.i(appTAG,classTAG+": "+ key+": "+configuredValue);
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
			Log.i(appTAG,classTAG+": "+fromTopic+": "+"request");
			//Need to add replies for requests
		}
		else if (message.getMessageType().equalsIgnoreCase("inform"))
		{
			Log.i(appTAG,classTAG+": "+fromTopic+": "+"inform");
		}
		else if (message.getMessageType().equalsIgnoreCase("release"))
		{
			Log.i(appTAG,classTAG+": "+fromTopic+": "+"release");
		}
		
		return memberships;
	}
	
	
	

		    
		    public String getScanResultSecurity(ScanResult scanResult) {
		        Log.i(appTAG, classTAG+": * getScanResultSecurity");

		        final String cap = scanResult.capabilities;
		        final String[] securityModes = { "WEP", "PSK", "EAP" };

		        for (int i = securityModes.length - 1; i >= 0; i--) {
		        	if (cap.contains(securityModes[i])) {
		                return securityModes[i];
		            }
		        }

		        return "OPEN";
		    }
		    
		    
		    public static void setIpAssignment(String assign , WifiConfiguration wifiConf)
		    	    throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException{
		    	        setEnumField(wifiConf, assign, "ipAssignment");     
		    	    }

		    	    public static void setIpAddress(InetAddress addr, int prefixLength, WifiConfiguration wifiConf)
		    	    throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException,
		    	    NoSuchMethodException, ClassNotFoundException, InstantiationException, InvocationTargetException{
		    	        Object linkProperties = getField(wifiConf, "linkProperties");
		    	        if(linkProperties == null)return;
		    	        Class laClass = Class.forName("android.net.LinkAddress");
		    	        Constructor laConstructor = laClass.getConstructor(new Class[]{InetAddress.class, int.class});
		    	        Object linkAddress = laConstructor.newInstance(addr, prefixLength);

		    	        ArrayList mLinkAddresses = (ArrayList)getDeclaredField(linkProperties, "mLinkAddresses");
		    	        mLinkAddresses.clear();
		    	        mLinkAddresses.add(linkAddress);        
		    	    }

		    	    public static void setGateway(InetAddress gateway, WifiConfiguration wifiConf)
		    	    throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException, 
		    	    ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException{
		    	        Object linkProperties = getField(wifiConf, "linkProperties");
		    	        if(linkProperties == null)return;
		    	        Class routeInfoClass = Class.forName("android.net.RouteInfo");
		    	        Constructor routeInfoConstructor = routeInfoClass.getConstructor(new Class[]{InetAddress.class});
		    	        Object routeInfo = routeInfoConstructor.newInstance(gateway);

		    	        ArrayList mRoutes = (ArrayList)getDeclaredField(linkProperties, "mRoutes");
		    	        mRoutes.clear();
		    	        mRoutes.add(routeInfo);
		    	    }

		    	    public static void setDNS(InetAddress dns, WifiConfiguration wifiConf)
		    	    throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException{
		    	        Object linkProperties = getField(wifiConf, "linkProperties");
		    	        if(linkProperties == null)return;

		    	        ArrayList<InetAddress> mDnses = (ArrayList<InetAddress>)getDeclaredField(linkProperties, "mDnses");
		    	        mDnses.clear(); //or add a new dns address , here I just want to replace DNS1
		    	        mDnses.add(dns); 
		    	    }

		    	    public static Object getField(Object obj, String name)
		    	    throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
		    	        Field f = obj.getClass().getField(name);
		    	        Object out = f.get(obj);
		    	        return out;
		    	    }

		    	    public static Object getDeclaredField(Object obj, String name)
		    	    throws SecurityException, NoSuchFieldException,
		    	    IllegalArgumentException, IllegalAccessException {
		    	        Field f = obj.getClass().getDeclaredField(name);
		    	        f.setAccessible(true);
		    	        Object out = f.get(obj);
		    	        return out;
		    	    }  

		    	    public static void setEnumField(Object obj, String value, String name)
		    	    throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
		    	        Field f = obj.getClass().getField(name);
		    	        f.set(obj, Enum.valueOf((Class<Enum>) f.getType(), value));
		    	    }
		    	    
		    	    public boolean checkPreviousConfiguration(WifiManager wifiManager,String SSID) {
		    	        List<WifiConfiguration> configs = wifiManager.getConfiguredNetworks();
		    	        for(WifiConfiguration config : configs) {
		    	            if(config.SSID.equals("\"" + SSID + "\"")) return true;
		    	        }
		    	        return false;
		    	    }
}

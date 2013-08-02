package com.omf.resourcecontroller.OMF;

import java.util.HashMap;

public class OMFMessage {
	
	//Object variables
	private String  messageType;
	private long ts;
	private String src;
	HashMap<String, String> properties;
	private String type;
	
	//Constructor 
	public OMFMessage(){
		messageType = null;
		ts = 1234567890L;
		src = null;
		type = null;
		properties = new HashMap<String, String>();
	}
	//Message type
	public void setMessageType(String MessageType){
		this.messageType = MessageType;
	}
	
	public String getMessageType(){
		return this.messageType;
	}
	
	//Timestamp
	public void setTs(long timestamp){
		this.ts = timestamp;
	}
	
	public long getTs(){
		return this.ts;
	}
	
	//Source
	public void setSrc(String source){
		this.src = source;
	}
	
	public String getSrc(){
		return this.src;
	}
	//Set type if type exists
	public void setType(String Type){
		this.type = Type;
	}
	
	public String getType(){
		return this.type;
	}
	
	//Populate HashMap
	public void setProperty(String key, String value){
		this.properties.put(key, value);
	}
	
	
	public HashMap<String, String> getProperty(){
		return this.properties;
	}
	
	public String toString(){
		String s = "Message type: "+messageType+"\n"+
					"Source: "+src+"\n"+
					"Timestamp: "+Long.toString(ts)+"\n"+
					"Properties: "+properties.toString()+"\n";
					
					
		if(messageType.equalsIgnoreCase("inform"))
			s+="Itype: "+type+"\n";
		else if(messageType.equalsIgnoreCase("create"))
			s+="Rtype: "+type+"\n";
		
		return s;
	}
	
	public boolean isEmpty()
	{
		if (messageType!=null){
			return false;
		}
		return true;
	}
	
}

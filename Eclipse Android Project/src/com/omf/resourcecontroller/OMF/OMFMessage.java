package com.omf.resourcecontroller.OMF;

import java.util.HashMap;

public class OMFMessage {
	
	//Object variables
	private String  messageType;
	private String messageID;
	private long ts;
	private String src;
	HashMap<String, String> properties;
	private String type;
	
	
	//Constructor 
	public OMFMessage(){
		messageType = null;
		messageID = null;
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
	
	//Message id
	public void setMessageID(String MessageType){
		this.messageID = MessageType;
	}
		
	public String getMessageID(){
		return this.messageID;
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
	
	
	public HashMap<String, String> getProperties(){
		
		return this.properties;
	}
	
	public String getProperty(String key){
		
		return this.properties.get(key);
	}
	
	public String toString(){
		String s = "Message type: "+messageType+"\n"+
					"Message ID:"+messageID+"\n"+
					"Source: "+src+"\n"+
					"Timestamp: "+Long.toString(ts)+"\n"+
					"Properties: "+properties.toString()+"\n";
					
					
		if(messageType.equalsIgnoreCase("inform") || messageType.equalsIgnoreCase("release"))
			s+="Itype: "+type+"\n";
		else if(messageType.equalsIgnoreCase("create"))
			s+="Rtype: "+type+"\n";
		
		return s;
	}
	
	public boolean equals(String mid){
		
		if(this.messageID.equalsIgnoreCase(mid))
		{
			return true;
		}
			
		return false;
	}
	
	public boolean isEmpty()
	{
		if (messageType!=null){
			return false;
		}
		return true;
	}
	
	
	public void OMFCreate(){
		//Code to create something
		System.out.println(this.messageType);
		return;
	}
	
	public void OMFConfigure(){
		//Code to configure something
		System.out.println(this.messageType);
		return;
	}
	
	public void OMFRequest(){
		//Code to reply to a certain request
		System.out.println(this.messageType);
		return;
	}
	
	public void OMFInform(){
		//Code to inform someone of something
		System.out.println(this.messageType);
		return;
	}
	
	public void OMFRelease(){
		//Code to inform someone of something
		System.out.println(this.messageType);
		return;
	}
	
	
	
}

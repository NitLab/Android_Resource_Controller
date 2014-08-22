/* Copyright (c) 2013 NITLab, University of Thessaly, Greece
 * This software may be used and distributed solely under the terms of the MIT license (License).
 * You should find a copy of the License in LICENSE.TXT or at http://opensource.org/licenses/MIT.
 * By downloading or using this software you accept the terms and the liability disclaimer in the License.
*/

package com.omf.resourcecontroller.OMF;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.omf.resourcecontroller.Constants;

public class OMFMessage implements Constants{
	
	//Object variables
	private String  messageType; //referred as "op" in the OMF FRCP - json
	private String messageID;
	private Long ts;
	private String src;
	HashMap<String, Object> properties;
	HashMap<String, Object> guard;
	private String type;
	private String cid;
	private String resID;
	private String reason;
	private String replyto;
	
	
	//Constructor 
	public OMFMessage(){
		this.messageType = null;
		this.messageID = null;
		this.ts = null;
		this.src = null;
		this.type = null;
		this.cid = null;
		this.resID = null;
		this.reason = null;
		this.replyto = null;
		this.properties = new HashMap<String, Object>();
		this.guard = new HashMap<String, Object>();
	}
	
	//Copy Constructor
	
	public OMFMessage(OMFMessage copyMsg){
		this.messageType = copyMsg.messageType;
		this.messageID = copyMsg.messageID;
		this.ts = copyMsg.ts;
		this.src = copyMsg.src;
		this.type = copyMsg.type;
		this.cid = copyMsg.cid;
		this.resID = copyMsg.resID;
		this.reason = copyMsg.reason;
		this.replyto = copyMsg.replyto;
		this.properties = copyMsg.properties;
		this.guard = copyMsg.guard;
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
	public void setTs(Long timestamp){
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
	
	
	//cid if it exists
	public void setCid(String Cid){
		this.cid = Cid;
	}
		
	public String getCid(){
		return this.cid;
	}
	
	//resID if it exists
	public void setResID(String ResID){
		this.resID = ResID;
	}
			
	public String getResID(){
		return this.resID;
	}
	
	//Reply to
	public void setReplyTo(String ReplyTo){
		this.replyto = ReplyTo;
	}
		
	public String getReplyTo(){
		return this.replyto;
	}
	
	//reason if it exists
	public void setReason(String Reason){
		this.reason = Reason;
	}
				
	public String getReason(){
		return this.reason;
	}
	
	//Set type if type exists
	public void setType(String Type){
		this.type = Type;
	}
	
	public String getType(){
		return this.type;
	}
	
	//Populate HashMap
	public void setProperty(String key, Object value){
		this.properties.put(key, value);
	}
	
	public void setGuard(String key, String value){
		this.guard.put(key, value);
	}
	
	public void setPropertiesHashmap(HashMap<String, Object> properties){
		this.properties = properties;
	}
	
	public void setGuardHashmap(HashMap<String, Object> Guard){
		this.guard = Guard;
	}
	
	
	public HashMap<String, Object> getPropertiesHashmap(){
		
		return this.properties;
	}
	
	public HashMap<String, Object> getGuardHashmap(){
		
		return this.guard;
	}
	
	
	public Object getProperty(String key){
		return this.properties.get(key);
	}
	
	public Object getGuard(String key){
		return this.guard.get(key);
	}
	
	
	public String toString(){
		String s = "Message type: "+messageType+"\n"+
					"Message ID:"+messageID+"\n"+
					"Source: "+src+"\n"+
					"Timestamp: "+Long.toString(ts)+"\n"+
					"Properties: "+properties.toString()+"\n";
		
		
		if(!guard.isEmpty())
			s+="Guard: "+guard.toString()+"\n";
		
		if(cid!=null)
			s+="Cid: "+cid+"\n";
					
		if(messageType.equalsIgnoreCase("inform"))
			s+="Itype: "+type+"\n";
		else if(messageType.equalsIgnoreCase("create"))
			s+="Rtype: "+type+"\n";
		
		if(resID!=null)
			s+="Res ID: "+resID+"\n";
		
		if(reason!=null)
			s+="Reason: "+reason+"\n";
		
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
	
	
	public String toXML(){
		String xml = null;
		
		
		
		if(this.messageType!=null){
			if(this.messageID!=null)
				xml="<"+this.messageType+" xmlns=\""+SCHEMA+"\" mid=\""+this.messageID+"\"" + " >";
			
			
			if(!this.properties.isEmpty()) 
			{
				
				xml+="<props>";
				xml+=propertiesToXML(this.properties);
				xml+="</props>";
			}
			
			if(this.ts!=null)
				xml+="<ts>"+this.ts+"</ts>";
			
			if(this.src!=null)
				xml+="<src>"+this.src+"</src>";
			
			
			if(this.cid!=null)
				xml+="<cid>"+this.cid+"</cid>";
			
			
			if(this.type!=null)
			{
				if(this.messageType.equalsIgnoreCase("inform") || this.messageType.equalsIgnoreCase("release"))
					xml+="<itype>"+this.type+"</itype>";
				else if(this.messageType.equalsIgnoreCase("create"))
					xml+="<rtype>"+this.type+"</rtype>";
			}
		

			if(this.resID!=null)
				xml+="<res_id>"+this.resID+"</res_id>";
			
			if(this.replyto!=null)
				xml+="<replyto>"+this.replyto+"</replyto>";
			
			if(this.reason!=null)
				xml+="<reason>"+this.reason+"</reason>";
			
			
			xml+="</"+ this.messageType+">";
		}
		
		
		return xml;
	}
	
	@SuppressWarnings("unchecked")
	public String propertiesToXML(HashMap<String, Object> parameterProps)
	{
		HashMap<String,Object> props = new HashMap<String, Object> (parameterProps);
		String propsToXML = "";
		String key = null;
		PropType propType = null;

		Iterator<Entry<String, Object>> it = props.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<String, Object> pairs = (Map.Entry<String, Object>)it.next();
	        //System.out.println(pairs.getKey() + " = " + pairs.getValue());
	        key = pairs.getKey();
	        propType =(PropType) pairs.getValue();
	        
	        
	        if(propType.getType().equalsIgnoreCase("hash"))
	        {
	     
	        	propsToXML+="<"+key+" type=\""+propType.getType()+"\">";
	        	propsToXML+=propertiesToXML((HashMap<String, Object>)propType.getProp());
	        	propsToXML+="</"+key+">";
	        	
	        }
	        else if(propType.getType().equalsIgnoreCase("array"))
	        {
	        	
	        	List<String> list= (List<String>)propType.getProp();
	        	
	        	propsToXML+="<"+key+" type=\""+propType.getType()+"\">";
	        	
	        	//iterate through list to get all items
	        	if(!list.isEmpty()){
		        	for(int i=0;i<list.size();i++)
		        	{
		        		propsToXML+="<it type=\"string\">"+list.get(i)+"</it>";		
		        	}
	        	}
	        	propsToXML+="</"+key+">";
	        }
	        else
	        {
	        	
	        	
	        	propsToXML+="<"+key+" type=\""+propType.getType()+"\">";
	        	propsToXML+=""+(String)propType.getProp()+"";
	        	propsToXML+="</"+key+">";
	        }
	        it.remove(); // avoids a ConcurrentModificationException
	    }
		return propsToXML;
	}
	
	

}

package com.omf.resourcecontroller.XMLgenerator;

import java.util.HashMap;
import java.util.UUID;

import com.omf.resourcecontroller.Constants;
import com.omf.resourcecontroller.OMF.OMFMessage;
import com.omf.resourcecontroller.OMF.PropType;
/**
 * XML Generator, Generates OMF messages(inform messages at the moment)
 * @author Polychronis Symeonidis
 *
 */
public class XMLGenerator implements Constants{
	
	public static final String TAG = "XMLGenerator";
	
	
	/**
	 * 
	 * @param Topic : source Topic name
	 * @param serverName : Server name 
	 * @param incomingMessage : Incoming OMFMessage that the inform replys to, if null the inform does not have a cid
	 * @param iType : Inform type (STATUS, CREATION.OK, ERROR...etc)
	 * @param properties : properties of the inform message, if the exist
	 * @return
	 */
	public String informMessage(String Topic, String serverName, OMFMessage incomingMessage, String iType, HashMap<String, Object> properties){
		OMFMessage message = new OMFMessage();
		message.setMessageType("inform");
		message.setMessageID(UUID.randomUUID().toString());
		message.setTs(System.currentTimeMillis() / 1000L);
		message.setSrc("xmpp://"+Topic+"@"+serverName);
		
		

		if(incomingMessage!=null)
		{
			message.setCid(incomingMessage.getMessageID());
			
			if(incomingMessage.getResID()!=null && (iType.equals("RELEASED") || iType.equals("ERROR")))
			{
				message.setResID("xmpp://"+incomingMessage.getResID()+"@"+serverName);
			
				if(iType.equals("ERROR"))
					message.setReason("Node could not be released");
			}
			
		}

			
		if(iType.equalsIgnoreCase("STATUS") || iType.equalsIgnoreCase("CREATION.OK") || iType.equalsIgnoreCase("CREATION.FAILED"))
		{
			//inform must have properties
			if(properties!=null)
				message.setPropertiesHashmap(properties);
		}
		

		message.setType(iType);
		return message.toXML();
	}
	
	public String createMessage(String Topic, String serverName, OMFMessage incomingMessage, String iType){
		OMFMessage message = new OMFMessage();
		message.setMessageType("create");
		message.setMessageID(UUID.randomUUID().toString());
		message.setTs(System.currentTimeMillis() / 1000L);
		message.setSrc("xmpp://"+Topic+"@"+serverName);
		message.setCid(incomingMessage.getMessageID());
		message.setType(iType);
		
		return message.toXML();
	}
	
	public String releaseMessage(String Topic, String serverName, OMFMessage incomingMessage, String iType){
		OMFMessage message = new OMFMessage();
		message.setMessageType("release");
		message.setMessageID(UUID.randomUUID().toString());
		message.setTs(System.currentTimeMillis() / 1000L);
		message.setSrc("xmpp://"+Topic+"@"+serverName);
		message.setCid(incomingMessage.getMessageID());
		message.setType(iType);
		
		return message.toXML();
	}
	
	public String configureMessage(String Topic, String serverName, OMFMessage incomingMessage, String iType){
		OMFMessage message = new OMFMessage();
		message.setMessageType("configure");
		message.setMessageID(UUID.randomUUID().toString());
		message.setTs(System.currentTimeMillis() / 1000L);
		message.setSrc("xmpp://"+Topic+"@"+serverName);
		message.setCid(incomingMessage.getMessageID());
		message.setType(iType);
		
		
		
		return message.toXML();
	}
	
	/**
	 * addProperties
	 * adds properties to a hashmap so that they can be used to generate an omf message
	 * @param props : HashMap<String, Object> props Object
	 * @param propName : property name tag
	 * @param propType : property type (String, array,hash, integer)
	 * @returns HashMap<String, Object> : properties
	 */
	public HashMap<String, Object> addProperties(HashMap<String, Object> props,String propName,PropType propType)
	{
		
		props.put(propName, propType);
		return props;
	}
}

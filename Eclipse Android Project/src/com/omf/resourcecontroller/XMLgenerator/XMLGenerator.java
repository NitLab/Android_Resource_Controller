package com.omf.resourcecontroller.XMLgenerator;

import java.util.UUID;

import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.Node;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.SimplePayload;

import com.omf.resourcecontroller.Constants;
import com.omf.resourcecontroller.OMF.OMFMessage;

public class XMLGenerator implements Constants{
	
	public void OMFMessageGenerator(Node node, String mtype, OMFMessage message, String propType , String Topic){
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

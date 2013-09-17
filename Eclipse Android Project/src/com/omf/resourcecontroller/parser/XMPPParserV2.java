package com.omf.resourcecontroller.parser;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.omf.resourcecontroller.OMF.OMFMessage;

public class XMPPParserV2 {
	/**
	 * XML Parser
	 * @param XMLstring : XML String 
	 * @throws IOException 
	 * @throws XmlPullParserException 
	 * @returns OMFMessage: HashMap that contains XML element names as keys and their values
	 */
	public static final String TAG = "BackgroundService";
	
	private XmlPullParserFactory factory= null;
	boolean flag  = false;
	OMFMessage message = null;
	XmlPullParser xpp = null;
	
	
	public XMPPParserV2() throws XmlPullParserException{
		
		factory = XmlPullParserFactory.newInstance();
		flag  = false;
		
		//OMFMessage
		message  = new OMFMessage();
		
		factory.setNamespaceAware(true);
		xpp = factory.newPullParser();
	}
	
	
	public OMFMessage XMLParse (String XMLstring) throws XmlPullParserException, IOException{
			
	        
	        //Set input
	        xpp.setInput ( new StringReader ( XMLstring) );
	        
	        //Begin "reading" the XML file
	        int eventType = xpp.getEventType();
	         while (eventType != XmlPullParser.END_DOCUMENT) {
	        	 if(eventType == XmlPullParser.START_TAG && (xpp.getName().equalsIgnoreCase("request") || xpp.getName().equalsIgnoreCase("inform") || xpp.getName().equalsIgnoreCase("configure") || xpp.getName().equalsIgnoreCase("create") || xpp.getName().equalsIgnoreCase("release")))
	        	 {   
	        		 //get message id and message type
	        		 message.setMessageType(xpp.getName());
	        		 message.setMessageID(xpp.getAttributeValue(null, "mid"));
	        		 	
	        		 while( !((eventType == XmlPullParser.END_TAG) && (xpp.getName().equalsIgnoreCase("request") || xpp.getName().equalsIgnoreCase("inform") || xpp.getName().equalsIgnoreCase("configure") || xpp.getName().equalsIgnoreCase("create") || xpp.getName().equalsIgnoreCase("release"))))
	        		 {
	        			 if(eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("props"))
        				 {
        					 flag=false;
        				 }
	        			 
	        			 
	        			 
	        			 if(eventType == XmlPullParser.START_TAG)
	        			 {
	        				 try{
	        					 if(flag)
	        					 {
	        						 //message.setProperty(xpp.getName(), xpp.nextText());
	        						message.setPropertiesHashmap( properties(xpp, eventType));
	        						//message.setPropertiesTypesHashmap(propertiesTypes(xpp, eventType));
	        					 }
	        					 else
	        					 {
	        						 
	        						 if(xpp.getName().equalsIgnoreCase("src"))
	        						 {
	        							 message.setSrc(xpp.nextText());
	        						 }
	        						 else if(xpp.getName().equalsIgnoreCase("ts"))
	        						 {
	        							 message.setTs(Long.parseLong(xpp.nextText(),10));
	        						 }
	        						 else if(xpp.getName().equalsIgnoreCase("itype") || xpp.getName().equalsIgnoreCase("rtype"))
	        						 {
	        							 message.setType(xpp.nextText());
	        						 }
	        						 else
	        						 {
	        							 xpp.nextText();
	        						 }
	        					 }
	        				 }
	        			 	 catch (XmlPullParserException e) {
	        			 		if(xpp.getName().equalsIgnoreCase("props")){
		        					 flag=true;
		        				 }
								//eventType = xpp.next(); 
							 }
	        			 }
	        			 else
	        			 {
	        				 eventType = xpp.next(); 
	        			 } 
	        		 }

	        	 }
	        	 else
	        	 {
	        		 eventType = xpp.next();
	        	 }
	         }
	         
		return message;
	}
	
	private HashMap<String, Object> properties(XmlPullParser parser , int eType) throws XmlPullParserException, IOException{
		
		HashMap<String, Object> props = new HashMap<String, Object>();
		String startTag = parser.getName();
		int eventType = eType;
		
		eventType = parser.next();
		System.out.println("Im in!");
		while(!((eventType == XmlPullParser.END_TAG) && (xpp.getName().equalsIgnoreCase(startTag)))){
			System.out.println("Im in the loop !");
			if(parser.getAttributeValue(null, "type").equalsIgnoreCase("hash"))
			{
				System.out.println("Im in the first if !");
				props.put("#hash#"+parser.getName(),properties(parser, eventType));
			}
			else if(parser.getAttributeValue(null, "type").equalsIgnoreCase("array"))
			{
				System.out.println("Im in the second if !");
				//props.put(parser.getName(),properties(parser, eventType));
			}
			else if(parser.getAttributeValue(null, "type").equalsIgnoreCase("integer"))
			{
				System.out.println("Im in the third if !");
				try {
					props.put("#integer#"+parser.getName(), parser.nextText());		//String,Symbol,Integer
				} catch (XmlPullParserException e) {
					
				} catch (IOException e) {
					
				}
			}
			else
			{	System.out.println("im in else !");
				try {
					props.put("#string#"+parser.getName(), parser.nextText());		//String,Symbol,Integer
				} catch (XmlPullParserException e) {
					
				} catch (IOException e) {
					
				}
			}
			
			eventType = parser.next();
		}
		System.out.println("im done !");
		xpp = parser;
		return props;
		
	}
	/*
	private HashMap<String, String> propertiesTypes(XmlPullParser parser, int eType){
		
		HashMap<String, String> propTypes = new HashMap<String, String>();
		
		String startTag = parser.getName();
		int eventType = eType;
		
		eventType = parser.next();
		
		while(!((eventType == XmlPullParser.END_TAG) && (xpp.getName().equalsIgnoreCase(startTag)))){
			if(parser.getAttributeValue(null, "type").equalsIgnoreCase("hash"))
			{
				propTypes.put(parser.getName(),propertiesTypes(parser, eventType));
			}
			else if(parser.getAttributeValue(null, "type").equalsIgnoreCase("array"))
			{
				propTypes.put(parser.getName(),propertiesTypes(parser, eventType));
			}
			else
			{
				try {
					propTypes.put(parser.getName(), parser.nextText());		//String,Symbol,Integer
				} catch (XmlPullParserException e) {
					
				} catch (IOException e) {
					
				}
			}
			
			eventType = parser.next();
		}
		
	
		//finished set global xpp where parser was left
		xpp = parser;
		return propTypes;
	}
	*/
	
}


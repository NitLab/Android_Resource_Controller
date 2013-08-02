package com.omf.resourcecontroller.parser;

import java.io.IOException;
import java.io.StringReader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.omf.resourcecontroller.OMF.OMFMessage;

public class XMPPParser {
	/**
	 * XML Parser
	 * @param XMLstring : XML String 
	 * @throws IOException 
	 * @throws XmlPullParserException 
	 * @returns OMFMessage: HashMap that contains XML element names as keys and their values
	 */
	public static final String TAG = "BackgroundService";
	
	public OMFMessage XMLParse (String XMLstring) throws XmlPullParserException, IOException{
			//Parser factory
			XmlPullParserFactory factory;
			factory = XmlPullParserFactory.newInstance();
		
			//Boolean flag for properties
			boolean flag  = false;
			//HashMap that stores XML elements and values
			//HashMap<String, String> OMFMessage  = new HashMap<String, String>();
			
			//OMFMessage
			OMFMessage message  = new OMFMessage();
			
	        factory.setNamespaceAware(true);
	        XmlPullParser xpp = factory.newPullParser();
	        
	        //Set input
	        xpp.setInput ( new StringReader ( XMLstring) );
	        
	        //Begin "reading" the XML file
	        int eventType = xpp.getEventType();
	         while (eventType != XmlPullParser.END_DOCUMENT) {
	        	 if(eventType == XmlPullParser.START_TAG && (xpp.getName().equalsIgnoreCase("request") || xpp.getName().equalsIgnoreCase("configure") || xpp.getName().equalsIgnoreCase("create")))
	        	 {   //System.out.println("Message:"+xpp.getName());
	        		 //OMFMessage.put("message", xpp.getName());
	        		 message.setMessageType(xpp.getName());
	        		 
	        		 while( !((eventType == XmlPullParser.END_TAG) && (xpp.getName().equalsIgnoreCase("request") || xpp.getName().equalsIgnoreCase("configure") || xpp.getName().equalsIgnoreCase("create"))))
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
	        						 //System.out.println("Prop "+xpp.getName()+": "+xpp.nextText());
	        						 //OMFMessage.put("prop"+xpp.getName(), xpp.nextText());
	        						 message.setProperty(xpp.getName(), xpp.nextText());
	        					 }
	        					 else
	        					 {
	        						 //System.out.println(xpp.getName()+": "+xpp.nextText());
	        						 //OMFMessage.put(xpp.getName(),xpp.nextText());
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
								eventType = xpp.next(); 
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
	
	
}

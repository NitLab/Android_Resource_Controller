package com.omf.resourcecontroller.parser;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;



public class XMPPParser {
	/**
	 * XML Parser
	 * @param XMLstring : XML String 
	 * @throws IOException 
	 * @throws XmlPullParserException 
	 * @returns OMFMessage: 
	 */
	public static final String TAG = "BackgroundService";
	
	public void XMLParse (String XMLstring) throws XmlPullParserException, IOException{
			//Parser factory
			XmlPullParserFactory factory;
			factory = XmlPullParserFactory.newInstance();
		
			
			String src = null;
			String ts = null;
			boolean flag  = false;
			HashMap<String, String> OMFMessage  = new HashMap<String, String>(); // allow duplicates
			
	        factory.setNamespaceAware(true);
	        XmlPullParser xpp = factory.newPullParser();
	        
	        //Set input
	        xpp.setInput ( new StringReader ( XMLstring) );
	        
	        //Begin "reading" the XML file
	        int eventType = xpp.getEventType();
	         while (eventType != XmlPullParser.END_DOCUMENT) {
	        	 /*
	          if(eventType == xpp.START_DOCUMENT) {
	              
	          } else if(eventType == xpp.END_DOCUMENT) {
	              
	          } else if(eventType == xpp.START_TAG) {
	              System.out.println("Start tag: "+xpp.getName());
	          } else if(eventType == xpp.END_TAG) {
	              System.out.println("End tag: "+xpp.getName());
	          } else if(eventType == xpp.TEXT) {
	              System.out.println("Text: "+xpp.getText());
	          }
	          */
	        	 
	        	 if(eventType == XmlPullParser.START_TAG && (xpp.getName().equalsIgnoreCase("request") || xpp.getName().equalsIgnoreCase("configure") || xpp.getName().equalsIgnoreCase("create")))
	        	 {
	        		 System.out.println("Message:"+xpp.getName());
	        		 while( !((eventType == XmlPullParser.END_TAG) && (xpp.getName().equalsIgnoreCase("request") || xpp.getName().equalsIgnoreCase("configure") || xpp.getName().equalsIgnoreCase("create"))))
	        		 {
	        			 /*
	        			  * if(eventType == XmlPullParser.START_TAG && xpp.getName().equalsIgnoreCase("src")){
	        				 eventType = xpp.next();
	        				 src = xpp.getText();
	        				 System.out.println("Source: "+src);
	        			 }
	        			 else if(eventType == XmlPullParser.START_TAG && xpp.getName().equalsIgnoreCase("ts"))
	        			 {
	        				 eventType = xpp.next();
	        				 ts = xpp.getText();
	        				 System.out.println("Timestamp: "+ts);
	        			 }
	        			 else
	        			 {
	        				 eventType = xpp.next();
	        			 }
	        			 */
	        			 if(eventType == XmlPullParser.START_TAG)
	        			 {
	        				 
	        				 try{
	        					 if(flag)
	        					 {
	        						 System.out.println("Prop "+xpp.getName()+": "+xpp.nextText());
	        					 }
	        					 else
	        					 {
	        						 System.out.println(xpp.getName()+": "+xpp.nextText());
	        					 }
	        					 
	        				 }
	        			 	 catch (XmlPullParserException e) {
	        			 		if(xpp.getName().equalsIgnoreCase("props")){
		        					 flag=true;
		        				 }
		        				 
		        				 
								eventType = xpp.next(); 
							 }
	        			 }
	        			 else{
	        				 eventType = xpp.next(); 
	        			 }
	        			 
	        			 if(eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("props"))
        				 {
        					 flag=false;
        				 }
	        		 }
	        	 }
	        	 else
	        	 {
	        		 eventType = xpp.next();
	        	 }
	          
	         }
		
		//return OMFMessage;
	}
	
	
}

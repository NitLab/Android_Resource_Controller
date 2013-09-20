/* Copyright (c) 2013 NITLab, University of Thessaly, Greece
 * This software may be used and distributed solely under the terms of the MIT license (License).
 * You should find a copy of the License in LICENSE.TXT or at http://opensource.org/licenses/MIT.
 * By downloading or using this software you accept the terms and the liability disclaimer in the License.
*/

package com.omf.resourcecontroller.parser;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
	        						 //System.out.println("Prop "+xpp.getName()+": "+xpp.nextText());
	        						 //OMFMessage.put("prop"+xpp.getName(), xpp.nextText());
	        						 //message.setProperty(xpp.getName(), xpp.nextText());
	        						 message.setPropertiesHashmap(properties(xpp, eventType, "props"));
	        						 eventType = XmlPullParser.END_TAG;
	        						 //flag=false;
	        						 //xpp.nextText();
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
	
	private HashMap<String, Object> properties(XmlPullParser parser , int eType, String stopClause) throws XmlPullParserException, IOException{
		
		HashMap<String, Object> props = new HashMap<String, Object>();
		
		int eventType = eType;
		String parserPrev = null;

		System.out.println("################################Properties##################################");
		//for(int i=0; i<8;i++){
			
		while((!(eventType == XmlPullParser.END_TAG) && !parser.getName().equalsIgnoreCase(stopClause))){
			
				//System.out.println("####im in else !");
				
			System.out.println("####Im in the loop ! ");
				if(eventType == XmlPullParser.START_TAG)
				{
					if(parser.getAttributeValue(null, "type")!=null)
					{
						if(parser.getAttributeValue(null, "type").equalsIgnoreCase("hash"))
						{
							System.out.println("Im in the first if !");
							parserPrev = parser.getName();
							parser.next();
							props.put("#hash#"+parserPrev,properties(parser, eventType, parserPrev)); //recursive function
						}
						else if(parser.getAttributeValue(null, "type").equalsIgnoreCase("array"))
						{
							System.out.println("Im in the second if !");
							List<String> list = new ArrayList<String>();
							parserPrev = parser.getName();
							parser.next();
							while(!(eventType == XmlPullParser.END_TAG) && (!parser.getName().equalsIgnoreCase(parserPrev))){
								if(eventType == XmlPullParser.START_TAG){
									try {
										list.add(parser.nextText());
									} catch (XmlPullParserException e) {
										
									} catch (IOException e) {
			
									}
								}
								eventType = parser.next();
							}
							props.put("#array#"+parserPrev,list);
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
					}
					else
					{	System.out.println("Attribute type doesnt exist...treat everything as a string");
						try {
							props.put("#string#"+parser.getName(), parser.nextText());		//String,Symbol,Integer
						} catch (XmlPullParserException e) {

						} catch (IOException e) {

						}
					}
				}
	

				if(parser.getName().equalsIgnoreCase("props")){
					System.out.println("BREAK");
					xpp = parser;
					return props;
				}
				//System.out.println(parser.nextText());
				eventType = parser.next();
		}

		xpp = parser;
		return props;	
	}
}


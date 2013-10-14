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

import android.util.Log;

import com.omf.resourcecontroller.Constants;
import com.omf.resourcecontroller.OMF.OMFMessage;
import com.omf.resourcecontroller.OMF.PropType;
/**
 * XMPPParser for XML messages
 * @author Polychronis Symeonidis
 *
 */
public class XMPPParserV2 implements Constants {
	/**
	 * XML Parser
	 * @param XMLstring : XML String 
	 * @throws IOException 
	 * @throws XmlPullParserException 
	 * @returns OMFMessage: HashMap that contains XML element names as keys and their values
	 */
	public static final String TAG = "XML Paser";
	
	private XmlPullParserFactory factory= null;
	boolean props = false;			//flag for properties
	boolean guard = false;			//flag for guard
	OMFMessage message = null;
	XmlPullParser xpp = null;
	
	public XMPPParserV2() throws XmlPullParserException{
		
		factory = XmlPullParserFactory.newInstance();
		props = false; 						//flag for properties
		guard = false;						//flag for guard
		
		factory.setNamespaceAware(true);	//XMPP pull parser factory set namespace
		xpp = factory.newPullParser();
		
	}
	
	/**
	 * 
	 * @param XMLstring : the XML Message to string
	 * @return OMFMessage: the message object that is created when the parsing is done, returns null if parsing failed
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	public OMFMessage XMLParse (String XMLstring)  throws XmlPullParserException, IOException{
			
			message  = new OMFMessage();		//OMFMessage
	        
			//Set input and features
	        xpp.setInput ( new StringReader ( XMLstring) );
	        			//Process Feature Namespaces
	        
	        //Begin "reading" the XML file
	        int eventType = xpp.getEventType();
	         while (eventType != XmlPullParser.END_DOCUMENT) 
	         {
	        	 if(eventType == XmlPullParser.START_TAG && (xpp.getName().equalsIgnoreCase("request") || xpp.getName().equalsIgnoreCase("inform") || xpp.getName().equalsIgnoreCase("configure") || xpp.getName().equalsIgnoreCase("create") || xpp.getName().equalsIgnoreCase("release")))
	        	 {   
	        		 //System.out.println("|DEBUG: "+xpp.getName());
	        		 if(!xpp.getNamespace().equals(SCHEMA))
	        		 {//SCHEMA doesnt comply with OMF 6.0 Protocol
	        			 Log.e(TAG,"ERROR: Not OMF 6.0 message");
	        			 return message; //return empty message
	        		 }
	        		 //get message id and message type
	        		 message.setMessageType(xpp.getName());
	        		 message.setMessageID(xpp.getAttributeValue(null, "mid"));
	        		 eventType = xpp.next();
	        		 
	        		 while( !((eventType == XmlPullParser.END_TAG) && (xpp.getName().equalsIgnoreCase("request") || xpp.getName().equalsIgnoreCase("inform") || xpp.getName().equalsIgnoreCase("configure") || xpp.getName().equalsIgnoreCase("create") || xpp.getName().equalsIgnoreCase("release"))))
	        		 {
	        			 //System.out.println("|DEBUG: "+xpp.getName());
	        			 
	        			 if(eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("props"))
        				 {
        					 props=false;
        					 //System.out.println("|DEBUG: props false");
        				 }
	        			 
	        			 if(eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("guard"))
        				 {
        					 guard=false;
        				 }
	        			 
	        			 if(eventType == XmlPullParser.START_TAG && xpp.getName().equalsIgnoreCase("guard"))
	        			 {
	        				guard = true;
	        				if(xpp.isEmptyElementTag())
        						guard=false;
	        				eventType = xpp.next();
	        			 }
	        			 else if(eventType == XmlPullParser.START_TAG && xpp.getName().equalsIgnoreCase("props"))
	        			 {
	        				props=true;
	        				if(xpp.isEmptyElementTag())
        						props=false;
	        				eventType = xpp.next();
	        			 }
	        			 
	        			 if(eventType == XmlPullParser.START_TAG)
	        			 {
	        				 try{
	        					 if(props) // only one of props or guard should be enabled at any given time
	        					 {
	        						 //System.out.println("|DEBUG in props: "+xpp.getName());
	        						 message.setPropertiesHashmap(properties(xpp, eventType, "props",xpp.getDepth()-1));
	        						 eventType = XmlPullParser.END_TAG;
	        					 }
	        					 else if(guard)
	        					 {
	        						 //guard and props have the same structure so i use the same funtion to parse it
	        						 message.setGuardHashmap(properties(xpp, eventType, "guard",xpp.getDepth()-1));
	        						 eventType = XmlPullParser.END_TAG;
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
	        						 else if(xpp.getName().equalsIgnoreCase("cid"))
	        						 {
	        							 message.setCid(xpp.nextText());
	        						 }
	        						 else if(xpp.getName().equalsIgnoreCase("res_id"))
	        						 {
	        							 message.setResID(xpp.nextText());
	        						 }
	        						 else if(xpp.getName().equalsIgnoreCase("reason"))
	        						 {
	        							 message.setReason(xpp.nextText());
	        						 }
	        						 else if(xpp.getName().equalsIgnoreCase("replyto"))
	        						 {
	        							 message.setReplyTo(xpp.nextText());
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
	        			 		if(eventType == XmlPullParser.START_TAG && xpp.getName().equalsIgnoreCase("props"))
	        			 		{	
		        					props=true;
		        					if(xpp.isEmptyElementTag())
		        						props=false;
		        				}
	        			 		else if(eventType == XmlPullParser.START_TAG && xpp.getName().equalsIgnoreCase("guard"))
	        			 		{
	        			 			guard=true;
	        			 			if(xpp.isEmptyElementTag())
		        						guard=false;
	        			 		}
	        			 		//System.out.println("|EXCEPTION: "+xpp.getName());
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
	/**
	 * Parser function for the properties element, is used also for the guard element.
	 * @param parser : parser object
	 * @param eType : element type
	 * @param stopClause : clause to stop recursion
	 * @param initialDepth : depth to handle problems with identical element names with different depth
	 * @return HasmMap: filled with the properties
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private HashMap<String, Object> properties(XmlPullParser parser , int eType, String stopClause, int initialDepth) throws XmlPullParserException, IOException{
		
		HashMap<String, Object> props = new HashMap<String, Object>();
		
		int eventType = eType;
		String parserPrev = null;
		int depthForList;
		
		
		while(!((eventType == XmlPullParser.END_TAG) && parser.getName().equalsIgnoreCase(stopClause)) && initialDepth!=parser.getDepth())
		{
				if(eventType == XmlPullParser.START_TAG)
				{
					//System.out.print("Type: ");
					//System.out.println(parser.getAttributeValue(null, "type"));
					if(parser.getAttributeValue(null, "type")!=null)
					{
						if(parser.getAttributeValue(null, "type").equalsIgnoreCase("hash"))
						{
							
							parserPrev = parser.getName();
							parser.next();
							props.put(parserPrev, new PropType(properties(parser, eventType, parserPrev, parser.getDepth()-1),"hash"));
							
						}
						else if(parser.getAttributeValue(null, "type").equalsIgnoreCase("array"))
						{
							
							List<String> list = new ArrayList<String>();
							parserPrev = parser.getName();
							depthForList = parser.getDepth();
							parser.next();
							while(!((eventType == XmlPullParser.END_TAG) && (parser.getName().equalsIgnoreCase(parserPrev))) && parser.getDepth()!=depthForList){
								if(eventType == XmlPullParser.START_TAG){
									try {
										list.add(parser.nextText());
									} catch (XmlPullParserException e) {
									} catch (IOException e) {
									}
								}
								eventType = parser.next();
							}
							props.put(parserPrev, new PropType(list,"array"));
						}
						else if(parser.getAttributeValue(null, "type").equalsIgnoreCase("integer"))
						{
							
							try {
								//System.out.println("NAME: "+parser.getName());
								props.put(parser.getName(), new PropType(parser.nextText(),"integer"));
								//System.out.println("NAME2: "+parser.getName());
							} catch (XmlPullParserException e) {
							} catch (IOException e) {
							}
						}
						else
						{	
							
							try {
								//System.out.println("NAME: "+parser.getName());
								props.put(parser.getName(), new PropType(parser.nextText(),"string")); //String,Symbol
								//System.out.println("NAME: "+parser.getName());
							} catch (XmlPullParserException e) {
							} catch (IOException e) {
							}
						}
					}
					else
					{	
						//System.out.println("SHould not reach");
						try {
							props.put(parser.getName(), new PropType(parser.nextText(),"string")); //String,Symbol,Integer if attributes dont exist
						} catch (XmlPullParserException e) {
						} catch (IOException e) {
						}
					}
				}

				if(parser.getName().equalsIgnoreCase("props") && initialDepth==parser.getDepth()){
					//System.out.println("BREAK");
					xpp = parser;
					return props;
				}
				//System.out.println("NEEEXT");
				eventType = parser.next();
				//System.out.println("Meta to next: "+parser.getName());
		}
		
		xpp = parser;
		return props;	
	}
}

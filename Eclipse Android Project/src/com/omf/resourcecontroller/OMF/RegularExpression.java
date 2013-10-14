package com.omf.resourcecontroller.OMF;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * RegularExpression object 
 * -- The functions could be made static so they can be accessed in a static way
 * @author Polychronis Symeonidis
 *
 */
public class RegularExpression {
	public String membershipReg( String uri ){

		
	      // String to be scanned to find the pattern.
	      String line = uri;
	      String pattern = "xmpp://(.+)@(.+)";

	      // Create a Pattern object
	      Pattern r = Pattern.compile(pattern);
	      
	      // Now create matcher object.
	      Matcher m = r.matcher(line);
	      
	      if (m.find( )) {
	         return m.group(1);
	      } else {
	         System.out.println("NO MATCH");
	         return uri;
	      }
	      
	      
	   }
	
	
	/**
	 * pidReg
	 * 
	 * @param process : gets the Process.toString() which has the process id in it and returns the PID
	 * @return String of the Process ID
	 */
	public String pidReg( String process ){

		
	      // String to be scanned to find the pattern.
	      String line = process;
	      String pattern = "(\\d+)";

	      // Create a Pattern object
	      Pattern r = Pattern.compile(pattern);
	      
	      // Now create matcher object.
	      Matcher m = r.matcher(line);
	      
	      if (m.find( )) {
	         //System.out.println("Found value: " + m.group(1) );
	         //System.out.println("Found value: " + m.group(1) );
	         return m.group(1);
	      } else {
	         System.out.println("NO MATCH");
	         return process;
	      }
	      
	      
	   }
	
	
	/**
	 * ipaddressReg
	 * @param ipaddr : is the String "ipaddress/subnet"
	 * @return a string with the ipaddress
	 */
	public String ipaddressReg( String ipaddr ){

		
	      // String to be scanned to find the pattern.
	      String line = ipaddr;
	      String pattern = "(.+)/(.+)";

	      // Create a Pattern object
	      Pattern r = Pattern.compile(pattern);
	      
	      // Now create matcher object.
	      Matcher m = r.matcher(line);
	      
	      if (m.find( )) {
	         //System.out.println("Found value: " + m.group(1) );
	         //System.out.println("Found value: " + m.group(1) );
	         return m.group(1);
	      } else {
	         System.out.println("NO MATCH");
	         return ipaddr;
	      }
	      
	      
	   }
	/**
	 * subnetReg
	 * @param ipaddr: is the String "ipaddr/subnet"
	 * @return the subnet 
	 */
	public String subnetReg( String ipaddr ){

		
	      // String to be scanned to find the pattern.
	      String line = ipaddr;
	      String pattern = "(.+)/(.+)";

	      // Create a Pattern object
	      Pattern r = Pattern.compile(pattern);
	      
	      // Now create matcher object.
	      Matcher m = r.matcher(line);
	      
	      if (m.find( )) {
	         //System.out.println("Found value: " + m.group(1) );
	         //System.out.println("Found value: " + m.group(1) );
	         return m.group(2);
	      } else {
	         System.out.println("NO MATCH");
	         return ipaddr;
	      }
	      
	      
	   }
	
}

package com.omf.resourcecontroller.generator;

import java.util.HashMap;

import com.omf.resourcecontroller.OMF.PropType;

public class PropertiesGenerator {
	
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

/* Copyright (c) 2013 NITLab, University of Thessaly, Greece
 * This software may be used and distributed solely under the terms of the MIT license (License).
 * You should find a copy of the License in LICENSE.TXT or at http://opensource.org/licenses/MIT.
 * By downloading or using this software you accept the terms and the liability disclaimer in the License.
*/

package com.omf.resourcecontroller.OMF;


public class PropType {
	
	String Type;
	Object Value;

	public PropType(Object value, String type)
	{
		Type = type;
		Value = value;
	}
	
	
	public void setProp(Object value)
	{
		this.Value=value;
	}
	
	public void setType(String type)
	{
		this.Type = type;
	}
	
	public Object getProp()
	{
		return this.Value;
	}
	
	public String getType()
	{
		return this.Type;
	}
	
	public String toString()
	{
		return "("+this.Value+", "+this.Type+")";
	}
	
}

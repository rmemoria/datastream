/**
 * 
 */
package com.rmemoria.datastream.impl;

import com.rmemoria.datastream.jaxb.Property;

/**
 * @author Ricardo Memoria
 *
 */
public class PropertyInfo {

	private Property property;
	private Object value;
	private Class type;


	public PropertyInfo(Property property, Object value, Class type) {
		super();
		this.property = property;
		this.value = value;
		this.type = type;
	}

	/**
	 * @return the property
	 */
	public Property getProperty() {
		return property;
	}
	/**
	 * @param property the property to set
	 */
	public void setProperty(Property property) {
		this.property = property;
	}
	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}
	/**
	 * @return the type
	 */
	public Class getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(Class type) {
		this.type = type;
	}
}

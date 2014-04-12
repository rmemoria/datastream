/**
 * 
 */
package com.rmemoria.datastream.impl;

import java.util.HashMap;
import java.util.Map;

import com.rmemoria.datastream.StreamContext;


/**
 * This class is used to handle embedded objects inside another one (for example, component objects
 * in Hibernate), where several properties of the embedded object is passed as properties of
 * the parent object. So it group all properties with the same "root" property and handle them
 * as a common parent object
 * @author Ricardo Memoria
 *
 */
public class PropertyValues {

	private Map<PropertyMetaData, Object> properties;
	private PropertyMetaData property;
	private Object value;
	private String propertyName;
	
	
	/**
	 * @return
	 */
	public boolean isComplextProperty() {
		return properties != null;
	}

	/**
	 * Return true if the given property is part of a complex property of this property
	 * @param prop
	 * @return
	 */
	public boolean isPropertyGroup(PropertyMetaData prop) {
		String s = propertyName + ".";
		return prop.getPath().startsWith(s);
	}

	/**
	 * Apply values to the properties. All properties point to the same root object,
	 * so must apply the value to the properties of this object
	 * @param context
	 */
	public void applyValues(StreamContext context, Object obj) {
		if (!isComplextProperty()) {
			getProperty().setValue(context, obj, getValue(), true);
		}
		else {
			Class propType = getPropertyType();
			// create the object
			Object value = context.createInstance(propType, getPropertyValues());
			// set the main property
			property.getField().setValue(obj, value);
			// the values of the other properties
			for (PropertyMetaData prop: properties.keySet()) {
				Object propValue = properties.get(prop);
				prop.setValue(context, obj, propValue, true);
			}
		}
	}
	
	
	/**
	 * REturn information about the property paths and its values (to be created by the client)
	 * @return Map of string containing the property path and its values
	 */
	public Map<String, Object> getPropertyValues() {
		Map<String, Object> vals = new HashMap<String, Object>();
		
		if (properties == null) {
			vals.put(translatePath(property), vals);
		}
		else {
			for (PropertyMetaData prop: properties.keySet()) {
				vals.put(translatePath(prop), properties.get(prop));
			}
		}
		
		return vals;
	}
	
	
	private String translatePath(PropertyMetaData prop) {
		String s = prop.getPath();
		s = s.substring(propertyName.length() + 1);
		return s;
	}
	
	
	/**
	 * @return
	 */
	public Class getPropertyType() {
		if (property != null) {
			return property.getField().getField().getType();
		}
		else {
			throw new RuntimeException("No property set");
		}
	}
	
	/**
	 * Add a property to the group
	 * @param prop
	 * @param propValue
	 */
	public void addProperty(PropertyMetaData prop, Object propValue) {
		if (property == null) {
			this.property = prop;
			this.value = propValue;
			String[] s = prop.getPath().split("\\.");
			this.propertyName = s[0];
		}
		else {
			if (properties == null) {
				properties = new HashMap<PropertyMetaData, Object>();
				properties.put(property, this.value);
			}
			properties.put(prop, propValue);
		}
	}
	
	
	/**
	 * @return the properties
	 */
	public Map<PropertyMetaData, Object> getProperties() {
		return properties;
	}

	/**
	 * @param properties the properties to set
	 */
	public void setProperties(Map<PropertyMetaData, Object> properties) {
		this.properties = properties;
	}

	/**
	 * @return the property
	 */
	public PropertyMetaData getProperty() {
		return property;
	}

	/**
	 * @param property the property to set
	 */
	public void setProperty(PropertyMetaData property) {
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
	 * @return the propertyName
	 */
	public String getPropertyName() {
		return propertyName;
	}

	/**
	 * @param propertyName the propertyName to set
	 */
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
}

/**
 * 
 */
package com.rmemoria.datastream.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rmemoria.datastream.StreamContext;

/**
 * Store temporarily a property value read from the unmarshall implementation
 * @author Ricardo Memoria
 *
 */
public class PropertyValue {

	private PropertyMetaData property;
	private Object value;
	private PropertyValue parent;
	private List<PropertyValue> properties;
	
	
	
	public PropertyValue(PropertyValue parent, PropertyMetaData property, Object value) {
		super();
		this.property = property;
		this.value = value;
		this.parent = parent;
	}

	
	
	/**
	 * Add a child value to the property value
	 * @param property
	 * @param value
	 * @return
	 */
	public PropertyValue addChildValue(PropertyMetaData property, Object value) {
		if (properties == null) {
			properties = new ArrayList<PropertyValue>();
		}
		PropertyValue pv = new PropertyValue(parent, property, value);
		properties.add(pv);
		return pv;
	}
	
	
	/**
	 * Write the content of the property value in the owner object.
	 * The owner object must be of the same type as of the class
	 * @param owner
	 */
	public void writePropertyValue(StreamContext context, Object owner) {
		if ((value == null) && (properties != null)) {
			value = createValue(context);
		}

//		if (value != null) {
			// get the linked property
			PropertyMetaData linkprop = property.getTypeMetaData() != null? property.getTypeMetaData().getLinkParentObject() : null;
	
			if (property.isCollection()) {

				Collection lst = property.getCollectionObject(context, owner);
				for (Object item: (Collection)value) {
					lst.add(item);
					// is the property link between objects defined? 
					if (linkprop != null) {
						// link objects in the list with its parent by its link property 
						linkprop.getFieldAccess().setValue(item, owner);
					}
				}
			}
			else { 
				getProperty().getFieldAccess().setValue(owner, value);
				// set the link between the object and the value, if available
				if (linkprop != null) {
					linkprop.getFieldAccess().setValue(value, owner);
				}
			}
//		}

		// update the child values
		if (properties != null && value != null) {
			for (PropertyValue pv: properties) {
                pv.writePropertyValue(context, value);
			}
		}
	}
	
	
	protected Object createValue(StreamContext context) {
		Map<String, Object> params = new HashMap<String, Object>();
		addAttributes(params, property);
		return context.createInstance(getProperty().getPropertyType(), params);
	}
	
	
	/**
	 * Add the attributes that will be used to send back to the client
	 * in order to create the object
	 * @param attrs Map containing the properties and its values
	 * @param limitParent is the parent that will limit the path resolution
	 */
	public void addAttributes(Map<String, Object> attrs, PropertyMetaData limitParent) {
		if (value != null && value != Constants.NULL_VALUE) {
			attrs.put(property.getPath(limitParent), value);
		}

		// has children?
		if (properties != null) {
			// check the values in the children
			for (PropertyValue aux: properties) {
				aux.addAttributes(attrs, limitParent);
			}
		}
	}
	
	
	/**
	 * Search for a child property value by its property meta data
	 * @param prop instance of {@link PropertyMetaData}
	 * @return instance of {@link PropertyValue} or null if not found
	 */
	public PropertyValue findChildPropertyValue(PropertyMetaData prop) {
		if (properties == null) {
			return null;
		}

		for (PropertyValue pv: properties) {
			if (pv.getProperty() == prop) {
				return pv;
			}
		}
		return null;
	}
	
	
	/**
	 * Search for a property value child by its property name
	 * @param propname
	 * @return
	 */
	public PropertyValue findChildPropertyByName(String propname) {
		if (properties == null) {
			return null;
		}

		for (PropertyValue pv: properties) {
			if (pv.getProperty().getPropertyName().equals(propname)) {
				return pv;
			}
		}
		return null;
	}
	
	
	/**
	 * Return the number of child properties
	 * @return int value
	 */
	public int getPropertyCount() {
		return properties != null? properties.size(): 0;
	}
	
	
	/**
	 * Return the list of properties
	 * @return
	 */
	public List<PropertyValue> getProperties() {
		return properties;
	}
	
	
	/**
	 * Change the object value
	 * @param value
	 */
	public void setValue(Object value) {
		this.value = value;
	}
	
	/**
	 * @return the property
	 */
	public PropertyMetaData getProperty() {
		return property;
	}
	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}
	/**
	 * @return the parent
	 */
	public PropertyValue getParent() {
		return parent;
	}
	
	
	@Override
	public String toString() {
		String s = property.toString();
		if (properties != null) {
			return s + "=[" + properties.size() + " value(s)]";
		}
		else { 
			return s + "=" + value;
		}
	}
}

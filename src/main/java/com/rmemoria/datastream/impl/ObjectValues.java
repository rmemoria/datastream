/**
 * 
 */
package com.rmemoria.datastream.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rmemoria.datastream.StreamContext;

/**
 * Store the values of the object while reading the XML document. Once all
 * values of the object is read, the object is instantiated and all its values
 * are written to the respective properties
 * 
 * @author Ricardo Memoria
 *
 */
public class ObjectValues {

	private ClassMetaData classMetaData;
	private List<PropertyValue> properties = new ArrayList<PropertyValue>();
	private Map<String, String> customProperties;

	/**
	 * Default constructor
	 * @param classMetaData
	 */
	public ObjectValues(ClassMetaData classMetaData) {
		super();
		this.classMetaData = classMetaData;
	}


	/**
	 * Add a new value to the object value tree
	 * @param propertyName
	 * @param value
	 */
	public void addValue(String propertyName, Object value) {
		String[] props = propertyName.split("\\.");
		PropertyMetaData prop = classMetaData.findPropertyByName(props[0]);
		if (prop == null) {
			throw new RuntimeException("Property not found: " + propertyName);
		}

		PropertyValue pv = findPropertyValue(prop);
		if (pv == null) {
			pv = new PropertyValue(null, prop, null);
			properties.add(pv);
		}
		
		if (props.length > 1) {
			for (int i = 1; i < props.length; i++) {
				PropertyMetaData aux = prop.findPropertyByName(props[i]);
				if (aux == null) {
					throw new RuntimeException("Property not found: " + props[i]);
				}
				PropertyValue pvchild = pv.findChildPropertyValue(aux);
				if (pvchild == null) {
					pvchild = pv.addChildValue(aux, null);
				}
				prop = aux;
				pv = pvchild;
			}
		}
		
		pv.setValue(value);
	}
	
	
	/**
	 * Get the value related to the given property name
	 * @param propname is the property name
	 * @return instance of {@link Object} class
	 */
	public Object getValue(String propname) {
		String[] props = propname.split("\\.");
		PropertyValue val = null;
		for (String pname: props) {
			if (val == null) {
				val = findPropertyByName(pname);
			}
			else {
				val = val.findChildPropertyByName(pname);
			}
			if (val == null) {
				break;
			}
		}

//		if (val == null) {
//			throw new RuntimeException("Property not found: " + propname);
//		}
		return val != null? val.getValue(): null;
	}

	/**
	 * Search for a property value by its property name
	 * @param propname
	 * @return
	 */
	private PropertyValue findPropertyByName(String propname) {
		for (PropertyValue pv: properties) {
			if (pv.getProperty().getPropertyName().equals(propname)) {
				return pv;
			}
		}
		return null;
	}
	
	/**
	 * Find a property value by its property meta data information
	 * @param pmd instance of {@link PropertyMetaData} to search for value
	 * @return instance of {@link PropertyValue}
	 */
	public PropertyValue findPropertyValue(PropertyMetaData pmd) {
		for (PropertyValue pv: properties) {
			if (pv.getProperty() == pmd) {
				return pv;
			}
		}
		return null;
	}
	
	
	/**
	 * Create a new object from the values in the class
	 * @param context
	 * @return
	 */
	public Object createObject(StreamContext context) {
		Map<String, Object> attrs = new HashMap<String, Object>();
		for (PropertyValue val: properties) {
            if (val.getValue() != Constants.NULL_VALUE) {
                val.addAttributes(attrs, null);
            }
		}
		Object obj = context.createInstance(getClassMetaData().getGraphClass(), attrs);

        if (obj != null) {
            // set the values in the new object
            for (PropertyValue val: properties) {
                if (val.getValue() != Constants.NULL_VALUE) {
                    val.writePropertyValue(context, obj);
                }
            }
        }

		return obj;
	}

	
	/**
	 * @return the classMetaData
	 */
	public ClassMetaData getClassMetaData() {
		return classMetaData;
	}
	/**
	 * @param classMetaData the classMetaData to set
	 */
	public void setClassMetaData(ClassMetaData classMetaData) {
		this.classMetaData = classMetaData;
	}
	
	/**
	 * Return the list of property values
	 * @return
	 */
	public List<PropertyValue> getProperties() {
		return properties;
	}


	/**
	 * @return the customProperties
	 */
	public Map<String, String> getCustomProperties() {
		return customProperties;
	}


	/**
	 * @param customProperties the customProperties to set
	 */
	public void setCustomProperties(Map<String, String> customProperties) {
		this.customProperties = customProperties;
	}
}

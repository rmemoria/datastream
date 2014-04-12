/**
 * 
 */
package com.rmemoria.datastream.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ricardo Memoria
 *
 */
public class ObjectValues {

	private ClassMetaData classMetaData;
	private Map<PropertyMetaData, Object> values = new HashMap<PropertyMetaData, Object>();

	public ObjectValues(ClassMetaData classMetaData) {
		super();
		this.classMetaData = classMetaData;
		this.values = new HashMap<PropertyMetaData, Object>();
	}

	
	/**
	 * Group properties that contains common nested properties (representing a one-to-one object)
	 * @return List of {@link PropertyValues}
	 */
	public List<PropertyValues> groupProperties() {
		List<PropertyValues> props = new ArrayList<PropertyValues>();
		for (PropertyMetaData prop: values.keySet()) {
			boolean found = false;
			Object value = values.get(prop);
			for (PropertyValues pv: props) {
				if (pv.isPropertyGroup(prop)) {
					pv.addProperty(prop, value);
					found = true;
					break;
				}
			}
			
			if (!found) {
				PropertyValues pv = new PropertyValues();
				pv.addProperty(prop, value);
				props.add(pv);
			}
		}
		return props;
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
	 * @return the values
	 */
	public Map<PropertyMetaData, Object> getValues() {
		return values;
	}
	/**
	 * @param values the values to set
	 */
	public void setValues(Map<PropertyMetaData, Object> values) {
		this.values = values;
	}
}

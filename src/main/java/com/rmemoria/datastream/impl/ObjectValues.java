/**
 * 
 */
package com.rmemoria.datastream.impl;

import java.util.HashMap;
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

/**
 * 
 */
package com.rmemoria.datastream.impl;

import java.lang.reflect.Method;

/**
 * @author Ricardo Memoria
 *
 */
public class FieldAccess {

	private String name;
	private Method setMethod;
	private Method getMethod;

	public FieldAccess(String fieldName, Method getMethod, Method setMethod) {
		super();
		this.name = fieldName;
		this.setMethod = setMethod;
		this.getMethod = getMethod;
	}

	/**
	 * Get the value of the field given the object using the get method
	 * @param obj
	 * @return
	 */
	public Object getValue(Object obj) {
		try {
			return getMethod.invoke(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Set the value of the field from the given object using the set method
	 * @param obj
	 * @param value
	 */
	public void setValue(Object obj, Object value) {
		try {
			setMethod.invoke(obj, value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	
	/**
	 * Return true if the field can be changed
	 * @return boolean value
	 */
	public boolean isWritable() {
		return setMethod != null;
	}
	
	/**
	 * Return the class type of the field
	 * @return Class instance
	 */
	public Class<?> getFieldType() {
		return getMethod.getReturnType();
	}
	
	/**
	 * @return the field
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the setMethod
	 */
	public Method getSetMethod() {
		return setMethod;
	}
	/**
	 * @param setMethod the setMethod to set
	 */
	public void setSetMethod(Method setMethod) {
		this.setMethod = setMethod;
	}
	/**
	 * @return the getMethod
	 */
	public Method getGetMethod() {
		return getMethod;
	}
	/**
	 * @param getMethod the getMethod to set
	 */
	public void setGetMethod(Method getMethod) {
		this.getMethod = getMethod;
	}
}

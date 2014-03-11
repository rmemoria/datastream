/**
 * 
 */
package com.rmemoria.datastream;

import com.rmemoria.datastream.impl.ClassMetaData;
import com.rmemoria.datastream.impl.PropertyMetaData;

/**
 * Specific exception to the data stream framework
 * @author Ricardo Memoria
 *
 */
public class DataStreamException extends RuntimeException {
	private static final long serialVersionUID = 3947508618303942827L;
	
	private ClassMetaData currentClass;
	private PropertyMetaData currentProperty;

	/**
	 * Default constructor with a message
	 * @param message
	 */
	public DataStreamException(String message) {
		super(message);
	}

	/**
	 * Raise an exception with class and/or property information in the exception
	 * @param clazz
	 * @param prop
	 * @param message
	 */
	public DataStreamException(ClassMetaData clazz, PropertyMetaData prop, String message) {
		super(message);
		this.currentClass = clazz;
		this.currentProperty = prop;
	}

	/**
	 * Return information, if available, of the current class being processed when exception was thrown
	 * @return the currentClass
	 */
	public ClassMetaData getCurrentClass() {
		return currentClass;
	}

	/**
	 * Return information, if available, of the current property being processed where error was thrown
	 * @return the currentProperty
	 */
	public PropertyMetaData getCurrentProperty() {
		return currentProperty;
	}

}

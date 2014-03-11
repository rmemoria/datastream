/**
 * 
 */
package com.rmemoria.datastream;

/**
 * Interface that must be implemented to convert an object to string
 * and from string back to an object
 * 
 * @author Ricardo Memoria
 *
 */
public interface DataConverter {

	String convertToString(Object obj);
	
	Object convertFromString(String data, Class classType);
}

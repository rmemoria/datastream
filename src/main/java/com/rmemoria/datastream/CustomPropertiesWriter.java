/**
 * 
 */
package com.rmemoria.datastream;

import java.util.Map;

/**
 * Interface that a consumer must implement in order to receive custom
 * properties and its values when reading the document
 * 
 * @author Ricardo Memoria
 *
 */
public interface CustomPropertiesWriter {

	/**
	 * Receive the properties and its values of the custom properties read from the document.
	 * Because it's not possible to figure the type of each value, they are always returned
	 * as string value, so inside the method they must be converted to the proper type
	 * 
	 * @param object is the object that the custom properties were read from
	 * @param props is the map 
	 */
	void writeCustomProperties(Object object, Map<String, String> props);
}

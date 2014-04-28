/**
 * 
 */
package com.rmemoria.datastream;

import java.util.Map;

/**
 * Interface that make it possible to send custom properties (not defined in a POJO
 * style) to be included in the XML file. The custom properties must
 * be defined in the XML schema using the attribute <code>customType</code> in
 * the property definition
 * 
 * @author Ricardo Memoria
 *
 */
public interface CustomPropertiesReader {

	/**
	 * Return a map containing the properties and values to be included
	 * in the XML file
	 * @return
	 */
	Map<String, Object> readCustomProperties(Object object);
}

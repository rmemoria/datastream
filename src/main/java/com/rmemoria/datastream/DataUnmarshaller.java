/**
 * 
 */
package com.rmemoria.datastream;

import java.io.InputStream;
import java.util.Collection;

/**
 * Interface provided by the {@link StreamContext} to deserialize objects from a choosen
 * data type (xml, json, etc).
 * 
 * @author Ricardo Memoria
 *
 */
public interface DataUnmarshaller {

	/**
	 * Deserialize the objects from the given input stream and return them. If it's
	 * only one object supposed to be returned (configured in the XSD schema file), the
	 * own object is returned. If it's supposed to return a collection of objects (also
	 * defined in the XSD schema file) the return type is an instance of the {@link Collection}
	 * class containing all objects deserialized. 
	 * @param stream
	 * @return
	 */
	Object unmarshall(InputStream stream);

	/**
	 * Deserialize the objects from the given input stream, but for each object from the
	 * root object graph defined in the XSD schema file, the {@link ObjectConsumer} interface
	 * given as parameter is called and the object is discarded afterwards. This method is 
	 * intended to be used when there is a huge quantity of objects to be read from the
	 * input stream, so it's more efficient to process each one at a time.
	 * @param stream instance of the {@link InputStream} containing all objects to be deserialized
	 * @param consumer the instance of the {@link ObjectConsumer} that will be called for each
	 * root object deserialized
	 */
	void unmarshall(InputStream stream, ObjectConsumer consumer);
	
	/**
	 * Add an implementation of the {@link CustomPropertiesWriter} to the object
	 * in order to receive object custom properties that are read along the document
	 * @param writer implementation of the {@link CustomPropertiesWriter}
	 */
	void addPropertyWriter(CustomPropertiesWriter writer);
	
	/**
	 * Remove the instance of {@link CustomPropertiesWriter} previously added with
	 * the method {@link DataUnmarshaller#addPropertyWriter(CustomPropertiesWriter)} 
	 * @param writer implementation of the {@link CustomPropertiesWriter}
	 */
	void removePropertyWriter(CustomPropertiesWriter writer);
}

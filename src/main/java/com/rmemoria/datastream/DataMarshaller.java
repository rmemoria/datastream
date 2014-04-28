/**
 * 
 */
package com.rmemoria.datastream;

import java.io.OutputStream;


/**
 * Interface that defines how an object must be marshaled by the
 * library. The destination document will depends on the implementation
 * of the {@link DataMarshaller} interface
 * 
 * @author Ricardo Memoria
 *
 */
public interface DataMarshaller {

	void marshall(Object obj, OutputStream outputStream);

	void marshall(OutputStream output, ObjectProvider provider);

	/**
	 * Add a custom property reader to be used when generating
	 * the document
	 * @param reader implementation of the {@link CustomPropertiesReader} interface
	 */
	void addPropertyReader(CustomPropertiesReader reader);
	
	/**
	 * Remove a custom property reader from the document generation process.
	 * The reader must be previously included using the {@link DataMarshaller#addPropertyReader(CustomPropertiesReader)} method
	 * @param reader instance of {@link CustomPropertiesReader} 
	 */
	void removePropertyReader(CustomPropertiesReader reader);
}

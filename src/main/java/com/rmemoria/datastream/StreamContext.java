/**
 * 
 */
package com.rmemoria.datastream;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;

/**
 * Represents a data stream context to serialize and deserialize objects using an specific object graph. 
 * @author Ricardo Memoria
 *
 */
public interface StreamContext {

	/**
	 * Set the schema in use by the given {@link URL} pointing to the XML schema file
	 * @param schema instance of {@link URL}
	 */
	void setSchema(URL schema);
	
	/**
	 * Set the schema in use by the context using the given {@link InputStream} containing the XML schema
	 * @param inputStream instance of {@link InputStream}
	 */
	void setSchema(InputStream inputStream);
	
	/**
	 * Create an instance of the {@link DataMarshaller} object that will serialize the object to the destination format
	 * @param targetFile indicate the target file to marshal the objects to
	 * @return instance of {@link DataMarshaller} interface
	 */
	DataMarshaller createMarshaller(Class<? extends StreamFileType> targetType);

	/**
	 * Create an instance of {@link DataUnmarshaller} object that will deserialize the data in the specific
	 * file type to the object tree
	 * @param sourceType indicate the source file type (xml, json, etc)
	 * @return instance of the {@link DataUnmarshaller}
	 */
	DataUnmarshaller createUnmarshaller(Class<? extends StreamFileType> sourceType);

	/**
	 * Add an interceptor to the data stream context
	 * @param interceptor instance of the {@link DataInterceptor} interface
	 */
	void addInterceptor(DataInterceptor interceptor);

	/**
	 * Remove an interceptor previously added in the {@link #addInterceptor(DataInterceptor)}
	 * @param interceptor instance of the {@link DataInterceptor} interface
	 */
	void removeInterceptor(DataInterceptor interceptor);

	void setConverter(Class type, DataConverter converter);
	
	DataConverter findConverter(Class type);
	
	Object createInstance(Class type, Map<String, Object> params);
}

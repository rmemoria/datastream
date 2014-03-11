/**
 * 
 */
package com.rmemoria.datastream;

import java.io.InputStream;
import java.net.URL;

import com.rmemoria.datastream.impl.StreamContextImpl;

/**
 * Factory of new implementations of the {@link StreamContext} interface 
 * @author Ricardo Memoria
 *
 */
public class StreamContextFactory {

	/**
	 * Return a new implementation of the {@link StreamContext} interface
	 * using the given data schema in XML format
	 * @param schema is the URL pointing to the XML schema file
	 * @return instance of the {@link StreamContext} interface
	 */
	public static StreamContext createContext(URL schema) {
		StreamContext context = new StreamContextImpl();
		context.setSchema(schema);
		return context;
	}
	
	/**
	 * Create a new {@link StreamContext} implementation passing the
	 * schema as a {@link InputStream} object
	 * @param schema
	 * @return
	 */
	public static StreamContext createContext(InputStream schema) {
		StreamContext context = new StreamContextImpl();
		context.setSchema(schema);
		return context;
	}
}

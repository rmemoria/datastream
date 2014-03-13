/**
 * 
 */
package com.rmemoria.datastream.test;

import java.io.InputStream;
import java.net.URL;

import org.junit.Test;

import com.rmemoria.datastream.DataStreamException;
import com.rmemoria.datastream.DataUnmarshaller;
import com.rmemoria.datastream.StreamContext;
import com.rmemoria.datastream.StreamContextFactory;
import com.rmemoria.datastream.StreamFileTypeXML;

/**
 * This test checks if the library will complain about a required field that is missing 
 * in the sample XML file. It's supposed to raise a {@link DataStreamException} exception
 * 
 * @author Ricardo Memoria
 *
 */
public class RequiredTest {

	private StreamContext context;
	
	/**
	 * Test the required field
	 */
	@Test(expected=DataStreamException.class)
	public void testRequired() {
		//
		StreamContext context = getContext();

		InputStream in = getClass().getClassLoader().getResourceAsStream("com/rmemoria/datastream/test/order-data.xml");
		DataUnmarshaller um = context.createUnmarshaller(StreamFileTypeXML.class);
		um.unmarshall(in);
	}

	/**
	 * @return the contextSingleObject
	 */
	public StreamContext getContext() {
		if (context == null) {
			URL schema = getClass().getClassLoader().getResource("com/rmemoria/datastream/test/order-schema-required.xml");
			context = StreamContextFactory.createContext(schema);
		}
		return context;
	}
}

/**
 * 
 */
package com.rmemoria.datastream.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.junit.Test;

import com.rmemoria.datastream.DataStreamException;
import com.rmemoria.datastream.DataUnmarshaller;
import com.rmemoria.datastream.StreamContext;
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
	

	public static void main(String[] args) throws Exception {
		RequiredTest test = new RequiredTest();
		test.testRequired();
	}
	
	/**
	 * Test the required field
	 * @throws FileNotFoundException 
	 */
	@Test(expected=DataStreamException.class)
	public void testRequired() throws FileNotFoundException {
		//
		StreamContext context = getContext();

		InputStream in = new FileInputStream(new File("src/test/resources/order-data.xml"));
		DataUnmarshaller um = context.createUnmarshaller(StreamFileTypeXML.class);
		um.unmarshall(in);
	}

	/**
	 * @return the contextSingleObject
	 */
	public StreamContext getContext() {
		if (context == null) {
			context = ContextUtil.createContext("src/test/resources/order-schema-required.xml");
		}
		return context;
	}
}

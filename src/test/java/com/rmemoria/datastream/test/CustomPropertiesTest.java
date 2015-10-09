/**
 * 
 */
package com.rmemoria.datastream.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.rmemoria.datastream.CustomPropertiesReader;
import com.rmemoria.datastream.CustomPropertiesWriter;
import com.rmemoria.datastream.DataMarshaller;
import com.rmemoria.datastream.DataUnmarshaller;
import com.rmemoria.datastream.StreamContext;
import com.rmemoria.datastream.StreamFileTypeXML;
import com.rmemoria.datastream.test.model.Order;

/**
 * Test the insertion of custom properties inside the document. There is no validation
 * inside a custom property node
 * 
 * @author Ricardo Memoria
 *
 */
public class CustomPropertiesTest implements CustomPropertiesReader, CustomPropertiesWriter {

	private static final String xmlfilename = "target/test-custom-properties.xml";
	
	private StreamContext context;
	
	private static final String propString = "This is a simple text";
	private static final Integer propInt = 100;
	private static final Float propFloat = 50.05f;
	private static final boolean propBool = true;
	private static final Date propDate = new Date(50000000);

	/**
	 * Test the custom properties by generating a document and checking its
	 * XML content
	 * @throws Exception
	 */
	@Test
	public void testCustomProperties() throws Exception {
		// generate the document
		generateXmlDocument();
		
		// check if XML document was correctly created
		checkDocument();
		
		// read the XML document and check if the properties are there
		readXmlDocument();
	}

	
	/**
	 * Generate the XML document with the custom properties
	 * @throws Exception
	 */
	protected void generateXmlDocument() throws Exception {
		// create object to marshall
		Order order = new Order();
		order.setId(1);

		// generate the output
		File file = new File(xmlfilename);
		FileOutputStream fout = new FileOutputStream(file);
		DataMarshaller m = getContext().createMarshaller(StreamFileTypeXML.class);
		m.addPropertyReader(this);
		m.marshall(order, fout);
	}
	
	
	/**
	 * Read the XML document with the custom properties
	 * @throws Exception
	 */
	protected void readXmlDocument() throws Exception {
		File file = new File(xmlfilename);
		FileInputStream fin = new FileInputStream(file);
		DataUnmarshaller m = getContext().createUnmarshaller(StreamFileTypeXML.class);
		m.addPropertyWriter(this);
		Order order = (Order)m.unmarshall(fin);
		assertNotNull(order);
	}
	
	/**
	 * Check if the XML document was correctly generated
	 * @throws Exception
	 */
	public void checkDocument() throws Exception {
		File file = new File(xmlfilename);
		if (!file.exists()) {
			throw new RuntimeException("XML document doesn't exist: " + xmlfilename);
		}
		DocumentBuilderFactory docfactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docbuilder = docfactory.newDocumentBuilder();
		Document doc = docbuilder.parse(file);
		// check if there is a node labeled "order"
		assertEquals("order", doc.getDocumentElement().getNodeName());
		
		// check if there is a child node called "extra-info"
		NodeList lst = doc.getDocumentElement().getElementsByTagName("extra-info");
		assertEquals(1, lst.getLength());

		// check the number of child nodes 
		Node node = lst.item(0);
		lst = node.getChildNodes();
		assertEquals(5, lst.getLength());
	}
	
	
	/**
	 * Create the context
	 * @return the contextSingleObject
	 */
	protected StreamContext getContext() {
		if (context == null) {
			context = ContextUtil.createContext("src/test/resources/custom-property.xml");
		}
		return context;
	}

	/** {@inheritDoc}
	 */
	@Override
	public Map<String, Object> readCustomProperties(Object object) {
		// these properties must be included inside the tag <extra-info> in the XML document
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("number-property", propInt);
		values.put("boolean-property", propBool);
		values.put("string-property", propString);
		values.put("date-property", propDate);
		values.put("float-property", propFloat);
		return values;
	}


	/** {@inheritDoc}
	 */
	@Override
	public void writeCustomProperties(Object object, Map<String, String> props) {
		// check if all properties are there
		assertEquals(5, props.keySet().size());
		assertNotNull(props.get("number-property"));
		assertNotNull(props.get("boolean-property"));
		assertNotNull(props.get("string-property"));
		assertNotNull(props.get("date-property"));
		assertNotNull(props.get("float-property"));
		
		// check if values are the same
		assertEquals(propString, props.get("string-property"));
		assertEquals(propInt, context.findConverter(Integer.class).convertFromString(props.get("number-property"), Integer.class));
		assertEquals(propBool, context.findConverter(Boolean.class).convertFromString(props.get("boolean-property"), Boolean.class));
		assertEquals(propDate, context.findConverter(Date.class).convertFromString(props.get("date-property"), Date.class));
		assertEquals(propFloat, context.findConverter(Float.class).convertFromString(props.get("float-property"), Float.class));
	}
}

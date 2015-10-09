/**
 * 
 */
package com.rmemoria.datastream.test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Test;

import com.rmemoria.datastream.DataMarshaller;
import com.rmemoria.datastream.DataUnmarshaller;
import com.rmemoria.datastream.StreamContext;
import com.rmemoria.datastream.StreamFileTypeXML;
import com.rmemoria.datastream.test.model.MyTestClass;


/**
 * Test the use of ignorePropsNotDeclared property in the objectGraph schema file. 
 * If this property is true, just the properties declared in the schema will be used
 * 
 * @author Ricardo Memoria
 *
 */
public class PropNotDeclaredTest {

	@Test
	public void testPropNotDeclared() throws IOException {
		StreamContext context = ContextUtil.createContext("src/test/resources/mytestclass-schema.xml");
		DataMarshaller m = context.createMarshaller(StreamFileTypeXML.class);

		MyTestClass obj = new MyTestClass();
		obj.setId(1234);
		obj.setVal1(1);
		obj.setVal2(2);
		obj.setVal3(3);
		obj.setValLong4(4L);
		
		File file = new File("target/prop-ignored.xml");
		FileOutputStream out = new FileOutputStream(file);
		m.marshall(obj, out);
		out.close();

		// read the XML and transform back to a list of objects
		FileInputStream fin = new FileInputStream(file);
		DataUnmarshaller um = context.createUnmarshaller(StreamFileTypeXML.class);
		MyTestClass obj2 = (MyTestClass)um.unmarshall(fin);
		fin.close();

		assertNotNull(obj2);
		assertEquals(obj2.getId(), obj.getId());
		assertEquals(obj2.getVal1(), obj.getVal1());
		assertNotEquals(obj2.getVal2(), obj.getVal2());
		assertNotEquals(obj2.getVal3(), obj.getVal3());
		assertEquals(obj2.getValLong4(), obj.getValLong4());
	}
}

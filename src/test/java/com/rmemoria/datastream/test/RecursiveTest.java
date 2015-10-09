/**
 * 
 */
package com.rmemoria.datastream.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.rmemoria.datastream.DataMarshaller;
import com.rmemoria.datastream.DataUnmarshaller;
import com.rmemoria.datastream.StreamContext;
import com.rmemoria.datastream.StreamFileTypeXML;
import com.rmemoria.datastream.test.model.LinkedItem;

/**
 * Test properties pointing to object with graph defined in the collection element of the schema
 * (So nothing is pointed to the property)
 * 
 * @author Ricardo Memoria
 *
 */
public class RecursiveTest {

	@Test
	public void testRecursiveItems() throws IOException {
		StreamContext context = ContextUtil.createContext("src/test/resources/linkeditem-schema.xml");
		DataMarshaller m = context.createMarshaller(StreamFileTypeXML.class);
		
		List<LinkedItem> lst = createModel();

		FileOutputStream out = new FileOutputStream("target/linkedlist.xml");
		m.marshall(lst, out);
		out.close();
		
		
		FileInputStream in = new FileInputStream("target/linkedlist.xml");
		DataUnmarshaller um = context.createUnmarshaller(StreamFileTypeXML.class);
		List<LinkedItem> lst2 = (List<LinkedItem>)um.unmarshall(in);
		in.close();

		assertNotNull(lst2);
		assertEquals(lst2.size(), lst.size());
		
		assertEquals(lst2.get(0).getId(), lst.get(0).getId());
		assertEquals(lst2.get(0).getLevel(), lst.get(0).getLevel());
		assertEquals(lst2.get(1).getLevel(), lst.get(1).getLevel());
	}
	
	
	/**
	 * Create model to be serialized
	 * @return
	 */
	protected List<LinkedItem> createModel() {
		List<LinkedItem> lst = new ArrayList<LinkedItem>();

		LinkedItem item = new LinkedItem(1, new LinkedItem(2, new LinkedItem(3, new LinkedItem(4, new LinkedItem(5, null)))));
		lst.add(item);
		item = new LinkedItem(10, new LinkedItem(11, null));
		lst.add(item);

		return lst;
	}
}

/**
 * 
 */
package com.rmemoria.datastream.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map;

import org.junit.Test;

import com.rmemoria.datastream.DataInterceptor;
import com.rmemoria.datastream.DataMarshaller;
import com.rmemoria.datastream.DataUnmarshaller;
import com.rmemoria.datastream.ObjectConsumer;
import com.rmemoria.datastream.ObjectProvider;
import com.rmemoria.datastream.StreamContext;
import com.rmemoria.datastream.StreamFileTypeXML;
import com.rmemoria.datastream.test.model.Address;
import com.rmemoria.datastream.test.model.Address.AddressArea;
import com.rmemoria.datastream.test.model.Customer;
import com.rmemoria.datastream.test.model.Order;

/**
 * @author Ricardo Memoria
 *
 */
public class EmbeddedTest {

	private StreamContext context;
	
	private static final String CUST_EMAIL = "email@test.com";
	private static final String CUST_NAME = "customer name";
	private static final Integer CUST_ID = 200;
	private static final String ADDR_STREET = "St. Street";
	private static final int ADDR_NUMBER = 5000;
	private static final AddressArea ADDR_AREA = AddressArea.RURAL;



	@Test
	public void testEmbedded() throws Exception {
		// create data model
		final Order order = new Order();
		order.setId(100);

		Customer customer = new Customer();
		customer.setEmail(CUST_EMAIL);
		customer.setName(CUST_NAME);
		customer.setId(CUST_ID);
		order.setCustomer(customer);
		
		Address addr = new Address();
		addr.setArea(ADDR_AREA);
		addr.setNumber(ADDR_NUMBER);
		addr.setStreet(ADDR_STREET);
		customer.setAddress(addr);

		getContextSingleObject();

		// serialize order
		File file = new File("target/test-mixed-list.xml");
		FileOutputStream f = new FileOutputStream(file);
		DataMarshaller m = context.createMarshaller(StreamFileTypeXML.class);
		m.marshall(f, new ObjectProvider() {
			@Override
			public Object getObjectToSerialize(int index) {
				return order;
			}
		});
		f.close();

		DataInterceptor interceptor = new DataInterceptor() {
			@Override
			public Object newObject(Class objectType, Map<String, Object> params) {
				if (objectType == Customer.class) {
					assertEquals(params.size(), 6);
					assertTrue(params.containsKey("email"));
					assertTrue(params.containsKey("name"));
					assertTrue(params.containsKey("id"));
					assertTrue(params.containsKey("address.street"));
					assertTrue(params.containsKey("address.number"));
					assertTrue(params.containsKey("address.area"));
				}
				else {
					if (objectType == Address.class) {
						assertEquals(params.size(), 3);
						assertTrue(params.containsKey("street"));
						assertTrue(params.containsKey("number"));
						assertTrue(params.containsKey("area"));
					}
				}
				return null;
			}
			
			@Override
			public Class getObjectClass(Object obj) {
				// TODO Auto-generated method stub
				return null;
			}
		};
		
		// read the XML and transform back to an object
		FileInputStream fin = new FileInputStream(file);
		DataUnmarshaller um = context.createUnmarshaller(StreamFileTypeXML.class);
		context.addInterceptor(interceptor);
		um.unmarshall(fin, new ObjectConsumer() {
			@Override
			public void onNewObject(Object object) {
				Class clazz = object.getClass();
				assertEquals(clazz, Order.class);
				Order order = (Order)object;
				assertEquals(CUST_EMAIL, order.getCustomer().getEmail());
				assertEquals(CUST_ID, order.getCustomer().getId());
				assertEquals(CUST_NAME, order.getCustomer().getName());
				assertEquals(ADDR_AREA, order.getCustomer().getAddress().getArea());
				assertEquals(ADDR_NUMBER, order.getCustomer().getAddress().getNumber());
				assertEquals(ADDR_STREET, order.getCustomer().getAddress().getStreet());
			}

			@Override
			public void startObjectReading(Class objectClass) {
				// do nothing
			}
		});
		fin.close();
		
	}

	/**
	 * @return the contextSingleObject
	 */
	public StreamContext getContextSingleObject() {
		if (context == null) {
			context = ContextUtil.createContext("src/test/resources/order-schema2.xml");
		}
		return context;
	}
}

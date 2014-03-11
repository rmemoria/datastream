/**
 * 
 */
package com.rmemoria.datastream.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Date;
import java.util.Map;

import org.junit.Test;

import com.rmemoria.datastream.DataInterceptor;
import com.rmemoria.datastream.DataMarshaller;
import com.rmemoria.datastream.DataUnmarshaller;
import com.rmemoria.datastream.StreamContext;
import com.rmemoria.datastream.StreamContextFactory;
import com.rmemoria.datastream.StreamFileTypeXML;
import com.rmemoria.datastream.test.model.Customer;
import com.rmemoria.datastream.test.model.CustomerWrapper;
import com.rmemoria.datastream.test.model.Item;
import com.rmemoria.datastream.test.model.ItemWrapper;
import com.rmemoria.datastream.test.model.Order;
import com.rmemoria.datastream.test.model.OrderStatus;
import com.rmemoria.datastream.test.model.OrderWrapper;
import com.rmemoria.datastream.test.model.Product;

/**
 * Test interceptor using class wrappers (same scenario of using Hibernate entities)
 * to return the correct class instead of the wrapper class
 * 
 * @author Ricardo Memoria
 *
 */
public class InterceptorTest implements DataInterceptor {

	private StreamContext contextSingleObject;
	
	
	public static void main(String[] args) throws Exception {
		InterceptorTest test = new InterceptorTest();
		test.runInterceptorTest();
	}

	@Test
	public void runInterceptorTest() throws Exception {
		StreamContext context = getContextSingleObject();
		context.addInterceptor(this);

		// create data model
		Order order = createModel(1, "The customer");

		// serialize to XML in an external file
		File file = new File("target\\test-interceptor.xml");
		FileOutputStream f = new FileOutputStream(file);
		DataMarshaller m = context.createMarshaller(StreamFileTypeXML.class);
		m.marshall(order, f);
		f.close();
		
		FileInputStream in = new FileInputStream(file);
		DataUnmarshaller um = context.createUnmarshaller(StreamFileTypeXML.class);
		um.unmarshall(in);
	}

	/**
	 * @return the contextSingleObject
	 */
	public StreamContext getContextSingleObject() {
		if (contextSingleObject == null) {
			URL schema = getClass().getClassLoader().getResource("com/rmemoria/datastream/test/order-schema.xml");
			contextSingleObject = StreamContextFactory.createContext(schema);
		}
		return contextSingleObject;
	}
	
	/**
	 * Create an object model to be serialized/deserialized to/from XML
	 * @param orderid the order id to be set in the object
	 * @param clientName the customer name to be set in the data model
	 * @return
	 */
	public Order createModel(Integer orderid, String clientName) {
		Customer c = new CustomerWrapper();
		c.setId(1);
		c.setName(clientName);
		c.setEmail("customer@test.com");

		Order order = new OrderWrapper();
		order.setId(orderid);
		order.setDiscount(10.0f);
		order.setOrderDate(new Date());
		order.setStatus(OrderStatus.NEW);
		order.setCustomer(c);
		
		Product prod = new Product();
		prod.setId(123);
		Item item = new ItemWrapper();
		item.setOrder(order);
		item.setQuantity(1000);
		item.setUnitPrice(1.23f);
		item.setProduct(prod);
		
		order.getItems().add(item);
		
		prod = new Product();
		prod.setId(555);
		item = new ItemWrapper();
		item.setOrder(order);
		item.setQuantity(500);
		item.setUnitPrice(5.45f);
		item.setProduct(prod);
		order.getItems().add(item);
		
		return order;
	}
	
	/** {@inheritDoc}
	 */
	@Override
	public Object newObject(Class objectType, Map<String, Object> params) {
		if (objectType == Product.class) {
			assertNotNull(params);
			assertNotNull(params.get("id"));
		}
		return null;
	}

	/** {@inheritDoc}
	 */
	@Override
	public Class getObjectClass(Object obj) {
		if (obj.getClass() == OrderWrapper.class)
			return Order.class;

		if (obj.getClass() == ItemWrapper.class)
			return Item.class;
		
		if (obj.getClass() == CustomerWrapper.class)
			return Customer.class;

		return obj.getClass();
	}

}

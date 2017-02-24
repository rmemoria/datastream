/**
 * 
 */
package com.rmemoria.datastream.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.rmemoria.datastream.DataMarshaller;
import com.rmemoria.datastream.DataUnmarshaller;
import com.rmemoria.datastream.ObjectConsumer;
import com.rmemoria.datastream.ObjectProvider;
import com.rmemoria.datastream.StreamContext;
import com.rmemoria.datastream.StreamFileTypeXML;
import com.rmemoria.datastream.test.model.Customer;
import com.rmemoria.datastream.test.model.Item;
import com.rmemoria.datastream.test.model.Order;
import com.rmemoria.datastream.test.model.OrderStatus;
import com.rmemoria.datastream.test.model.Product;

/**
 * Test writing and reading of object using a single object and a list.
 * Also test it using a {@link ObjectProvider} and a {@link ObjectConsumer} interface.
 * 
 * @author Ricardo Memoria
 *
 */
public class XmlTest {

	private Order order2;
	private StreamContext contextSingleObject;
	private StreamContext contextCollection;
	int counter;
	int startEndCounter;


	public static void main(String[] args) throws Exception {
		XmlTest test = new XmlTest();
		test.testSingleObject();
		test.testCollection();
	}
	
	/**
	 * Test generation and reading of a collection
	 * @throws IOException 
	 */
	@Test
	public void testCollection() throws IOException {
		StreamContext context = getContextCollection();

		// create data model
		List<Order> lst = createCollectionModel();

		// serialize list
		File file = new File("target/test-list.xml");
		FileOutputStream f = new FileOutputStream(file);
		DataMarshaller m = context.createMarshaller(StreamFileTypeXML.class);
		m.marshall(lst, f);
		f.close();

		// read the XML and transform back to a list of objects
		FileInputStream fin = new FileInputStream(file);
		DataUnmarshaller um = context.createUnmarshaller(StreamFileTypeXML.class);
		List<Order> lst2 = (List<Order>)um.unmarshall(fin);
		fin.close();
		
		assertNotNull(lst2);
		assertEquals(lst2.size(), lst.size());
		for (int i = 0; i < lst2.size(); i++) {
			compareOrders(lst.get(i), lst2.get(i));
		}
	}

	
	/**
	 * Test generation and reading of a single object
	 * @throws IOException
	 */
	@Test
	public void testSingleObject() throws Exception {
		StreamContext context = getContextSingleObject();

		// create data model
		Order order = createModel(1, "The customer");

		// serialize to XML in an external file
		File file = new File("target/test.xml");
		FileOutputStream f = new FileOutputStream(file);
		DataMarshaller m = context.createMarshaller(StreamFileTypeXML.class);
		m.marshall(order, f);
		f.close();

		// read the XML and transform back to an object
		FileInputStream fin = new FileInputStream(file);
		DataUnmarshaller um = context.createUnmarshaller(StreamFileTypeXML.class);
		Order order2 = (Order)um.unmarshall(fin);
		fin.close();

		compareOrders(order, order2);
	}
	
	
	/**
	 * Test generation and reading of a single object
	 * @throws IOException
	 */
	@Test
	public void testSingleObjectNullProperty() throws Exception {
		StreamContext context = getContextSingleObject();

		// create data model
		Order order = createModel(1, "The customer");
		// set the customer to null
		order.setCustomer(null);

		// serialize to XML in an external file
		File file = new File("target/test-null.xml");
		FileOutputStream f = new FileOutputStream(file);
		DataMarshaller m = context.createMarshaller(StreamFileTypeXML.class);
		m.marshall(order, f);
		f.close();

		// read the XML and transform back to an object
		FileInputStream fin = new FileInputStream(file);
		DataUnmarshaller um = context.createUnmarshaller(StreamFileTypeXML.class);
		Order order2 = (Order)um.unmarshall(fin);
		fin.close();

		assertEquals(order2.getCustomer(), order.getCustomer());
	}
	
	
	/**
	 * Test serialization using an {@link ObjectProvider} for a single object
	 * @throws IOException
	 */
	@Test
	public void testProviderSingleObject() throws IOException {
		StreamContext context = getContextSingleObject();

		final Order order = createModel(1, "The customer");
		order2 = null;

		// serialize to XML in an external file
		File file = new File("target/test-provider.xml");
		FileOutputStream f = new FileOutputStream(file);
		DataMarshaller m = context.createMarshaller(StreamFileTypeXML.class);
		// generate it using a provider
		m.marshall(f, new ObjectProvider() {
			@Override
			public Object getObjectToSerialize(int index) {
				return order;
			}
		});
		f.close();

		// read the XML and transform back to an object
		FileInputStream fin = new FileInputStream(file);
        DataUnmarshaller um = context.createUnmarshaller(StreamFileTypeXML.class);
        um.unmarshall(fin, new ObjectConsumer() {
            @Override
            public void onNewObject(Object object) {
                order2 = (Order)object;
            }

            @Override
            public void startObjectReading(Class objectClass) {
                // do nothing
            }
        });
		fin.close();

		compareOrders(order, order2);
	}


	/**
	 * Test {@link ObjectProvider} and {@link ObjectConsumer} interfaces
	 * @throws IOException
	 */
	@Test
	public void testProviderCollection() throws IOException {
		StreamContext context = getContextCollection();

		final List<Order> orders = createCollectionModel();
		final List<Order> orders2 = new ArrayList<Order>();

		// serialize to XML in an external file
		File file = new File("target/test-provider-list.xml");
		FileOutputStream f = new FileOutputStream(file);
		DataMarshaller m = context.createMarshaller(StreamFileTypeXML.class);
		// generate it using a provider
		m.marshall(f, new ObjectProvider() {
			@Override
			public Object getObjectToSerialize(int index) {
				if (index < orders.size())
					 return orders.get(index);
				else return null;
			}
		});
		f.close();

		startEndCounter = 0;
		counter = 0;
		
		// read the XML and transform back to an object
		FileInputStream fin = new FileInputStream(file);
		DataUnmarshaller um = context.createUnmarshaller(StreamFileTypeXML.class);
		um.unmarshall(fin, new ObjectConsumer() {
			@Override
			public void onNewObject(Object object) {
				orders2.add((Order)object);
				counter++;
				startEndCounter--;
			}

			@Override
			public void startObjectReading(Class objectClass) {
				startEndCounter++;
			}
		});

		assertEquals(startEndCounter, 0);
		assertEquals(counter, orders2.size());
		assertNotNull(orders2);
		assertEquals(orders2.size(), orders.size());
		for (int i = 0; i < orders2.size(); i++) {
			compareOrders(orders2.get(i), orders.get(i));
		}
	}

	
	/**
	 * Test generation and reading of a collection with mixed objects
	 * @throws IOException
	 */
	@Test
	public void testMixedCollection() throws IOException {
		StreamContext context = getContextCollection();

		// create data model
		List lst = new ArrayList();
		Order order = createModel(1, "The customer");
		lst.add(order);
		lst.add(order.getCustomer());

		// serialize list
		File file = new File("target/test-mixed-list.xml");
		FileOutputStream f = new FileOutputStream(file);
		DataMarshaller m = context.createMarshaller(StreamFileTypeXML.class);
		m.marshall(lst, f);
		f.close();

		// read the XML and transform back to a list of objects
		FileInputStream fin = new FileInputStream(file);
		DataUnmarshaller um = context.createUnmarshaller(StreamFileTypeXML.class);
		List lst2 = (List)um.unmarshall(fin);
		
		assertNotNull(lst2);
		assertEquals(lst2.size(), lst.size());
		assertEquals(lst2.get(0).getClass(), Order.class);
		assertEquals(lst2.get(1).getClass(), Customer.class);
	}

	/**
	 * Test to compare if both orders are the same
	 * @param order1 instance of {@link Order}
	 * @param order2 instance of {@link Order}
	 */
	protected void compareOrders(Order order1, Order order2) {
		assertNotNull(order2);
		assertNotNull(order2.getCustomer());
		assertNotNull(order2.getItems());
		assertEquals(order2.getItems().size(), order1.getItems().size());
		assertEquals(order2.getId(), order1.getId());
		assertEquals(order2.getCustomer().getName(), order1.getCustomer().getName());
		assertEquals(order2.getDiscount(), order1.getDiscount(), 0.01);
		assertEquals(order2.getOrderDate().toString(), order1.getOrderDate().toString());
		assertEquals(order2.getStatus(), order1.getStatus());

		for (Item item: order2.getItems()) {
			assertNotNull(item.getProduct());
			assertNotNull(item.getProduct().getId());
			assertEquals(item.getOrder(), order2);
		}
	}

	
	/**
	 * Create a list to be serialized to XML
	 * @return
	 */
	public List<Order> createCollectionModel() {
		List<Order> lst = new ArrayList<Order>();
		for (int i = 1; i < 10; i++) {
			lst.add(createModel(i, "Customer " + Integer.toString(i)));
		}
		return lst;
	}
	
	/**
	 * Create an object model to be serialized/deserialized to/from XML
	 * @param orderid the order id to be set in the object
	 * @param clientName the customer name to be set in the data model
	 * @return
	 */
	public Order createModel(Integer orderid, String clientName) {
		Customer c = new Customer();
		c.setId(1);
		c.setName(clientName);
		c.setEmail("customer@test.com");

		Order order = new Order();
		order.setId(orderid);
		order.setDiscount(10.0f);
		order.setOrderDate(new Date());
		order.setStatus(OrderStatus.NEW);
		order.setCustomer(c);
		c.getOrders().add(order);
		
		Product prod = new Product();
		prod.setId(123);
		Item item = new Item();
		item.setOrder(order);
		item.setQuantity(1000);
		item.setUnitPrice(1.23f);
		item.setProduct(prod);
		
		order.getItems().add(item);
		
		prod = new Product();
		prod.setId(555);
		item = new Item();
		item.setOrder(order);
		item.setQuantity(500);
		item.setUnitPrice(5.45f);
		item.setProduct(prod);
		order.getItems().add(item);
		
		return order;
	}

	/**
	 * @return the contextSingleObject
	 */
	public StreamContext getContextSingleObject() {
		if (contextSingleObject == null) {
			contextSingleObject = ContextUtil.createContext("src/test/resources/order-schema.xml");
		}
		return contextSingleObject;
	}

	/**
	 * @return the contextCollection
	 */
	public StreamContext getContextCollection() {
		if (contextCollection == null) {
			contextCollection = ContextUtil.createContext("src/test/resources/order-schema-list.xml");
		}
		return contextCollection;
	}
}

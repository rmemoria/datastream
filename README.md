# Datastream

DataStream is a Java library to serialize/deserialize objects to/from an XML stream. This library was build to support huge XML files with minimum memory usage.

DataStream is meant to serialize/deserialize complex objects (POJO, mostly used) to/from XML files (or streams). It was implemented using JAXB in order to support huge files with a minimum of memory overhead. Although JAXB offers a mapping mechanims between objects and XML files, DataStream uses its own mapping to support more complex scenarios, like bidirectional parent-children mapping, proxy objects (for example, [Hibernate](http://hibernate.org/) entities), custom property reader/writer and possibility to ingore some properties.

## 1. Writing and reading a single object

In this mode, the XML root will be the own object node.

### Mapping

DataStream uses an XML file to determine how to read/write objects to/from XML files. It is basically a mapping of classes and its properties, informing DataStream how to handle them in an XML file.

For example, suppose you want map the class Order (and its dependencies):

```Java
public class Order {

	private Integer id;
	private Customer customer;
	private float discount;
	private Date orderDate;
	private OrderStatus status;
	private List<Item> items = new ArrayList<Item>();
...
}

public class Item {

	private Order order;
	private Product product;
	private int quantity;
	private float unitPrice;
  ...
}

public class Customer {
	private Integer id;
	private String name;
	private String email;
	private List<Order> orders = new ArrayList<Order>();
	private Address address;
	private Address address2;
  ...
}
```

And I want to map Orders to XML like that:

```XML
<?xml version="1.0" encoding="UTF-8"?>
<graphSchema xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="datastream-1.2.xsd">

    <objectGraph name="order" class="com.rmemoria.datastream.test.model.Order">
        <property name="id" xmlAttribute="true" />
        <property name="customer">
            <objectGraph name="customer" class="com.rmemoria.datastream.test.model.Customer">
                <property name="id" xmlAttribute="true" />
				<property name="orders" use="IGNORE" />
            </objectGraph>
        </property>
        <property name="items" >
            <objectGraph name="item" class="com.rmemoria.datastream.test.model.Item" parentProperty="order">
                <property name="product.id" elementName="product" xmlAttribute="true" />
            </objectGraph>
        </property>
    </objectGraph>
</graphSchema>
```

Notes:
* `objectGraph` represents a new class mapping you want to define. Inside you have `property` tags indicating how DataStream will serialize/deserialize each property;
* You may include as many classes as you want, just declare an `objectGraph` node for each class;
* As default, all object properties will be serialized/deserialized as XML nodes;
* The `name` property will be used as the node name in the XML file;
* For a detailed reference of the mapping options, check its (XDS)[https://github.com/rmemoria/datastream/blob/master/src/main/resources/datastream-1.2.xsd];

### Writing to XML

Create a StreamContext object containing the classes of your model with its mapping. You must do that only once:

```Java
InputStream in = new FileInputStream("\my-mapping.xml");
StreamContext context = StreamContextFactory.createContext(in);
```

With the context, you can serialize your object to XML.

```Java
		// create data model
		Order order = loadOrder();

		// serialize to XML in an external file
		File file = new File("target/test.xml");
		FileOutputStream f = new FileOutputStream(new File("data.xml");
		DataMarshaller m = context.createMarshaller(StreamFileTypeXML.class);
		m.marshall(order, f);
		f.close();
```

The content of `data.xml` is:

```xml
<?xml version="1.0"?>
<order id="1">
    <customer id="1">
        <name>The customer</name>
        <email>customer@test.com</email>
        <address></address>
        <address2></address2>
    </customer>
    <items>
        <item product="123">
            <quantity>1000</quantity>
            <unitPrice>1.23</unitPrice>
        </item>
        <item product="555">
            <quantity>500</quantity>
            <unitPrice>5.45</unitPrice>
        </item>
    </items>
    <discount>10.0</discount>
    <orderDate>2015-11-05T11:53:41-0200</orderDate>
    <status>NEW</status>
</order>
```

### Reading from XML
If you want to read from XML, you can use the following code:

```Java
		FileInputStream fin = new FileInputStream(file);
		DataUnmarshaller um = context.createUnmarshaller(StreamFileTypeXML.class);
		Order order2 = (Order)um.unmarshall(fin);
		fin.close();
```

## 2. Writing and reading multiple objects

In this mode, the xml root node represents a collection of object. Ideally used when you want to handle distict objects.

### Mapping

Let's consider the same object model used previously, but let's change the XML mapping to:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<graphSchema xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:noNamespaceSchemaLocation="datastream-1.2.xsd">

	<objectCollection name="orders">

		<objectGraph name="order"
			class="com.rmemoria.datastream.test.model.Order">
			<property name="id" xmlAttribute="true" />
			<property name="customer">
				<objectGraph name="customer"
					class="com.rmemoria.datastream.test.model.Customer">
					<property name="id" xmlAttribute="true" />
					<property name="orders" use="IGNORE" />
				</objectGraph>
			</property>
			<property name="items">
				<objectGraph name="item"
					class="com.rmemoria.datastream.test.model.Item" parentProperty="order">
					<property name="product.id" elementName="product"
						xmlAttribute="true" />
				</objectGraph>
			</property>
		</objectGraph>

	</objectCollection>
</graphSchema>
```

Notes:
* The XML root node will be `<orders>` (defined by the `objectCollection` tag.
* We can mix different objects inside the collection node, as soon as their classes are properly mapped.

### Writting a collection to XML

Using the context explained in the previous section, to write a collection, just pass it to the marshall object:

```Java
// create data model
List<Order> lst = createCollectionModel();

// serialize list
File file = new File("data.xml");
FileOutputStream f = new FileOutputStream(file);
DataMarshaller m = context.createMarshaller(StreamFileTypeXML.class);
m.marshall(lst, f);
f.close();

```

### Reading a collection from XML

Using the mapping below, read a collection is quite straightforward:

```Java
FileInputStream fin = new FileInputStream(file);
DataUnmarshaller um = context.createUnmarshaller(StreamFileTypeXML.class);
List<Order> lst2 = (List<Order>)um.unmarshall(fin);
fin.close();
```

## 3. Advanced features


### Dealing with huge datasets

All data serialized and deserialized are read and written on demand using Java InputStream and OutputStream using JAXB libraty. This is a good approach because guarantees that a minimum memory is used, even when dealing with huge datasets.

On the other hand, it is not a good approach to provide or consume the data using Collections, because by the end of the day, the whole data will have to fit into memory.

To avoid this issue, you can use two interfaces to provide and consume objects as they are read or written from/to XML:

```java
public interface ObjectProvider {
	Object getObjectToSerialize(int index);
}

public interface ObjectConsumer {
	void onNewObject(Object object);	
	void startObjectReading(Class objectClass);
}
```

`ObjectProvider` - Use this interface when you want to provide objects to be serialized. This method will be used as many times as the return value is not null. When you want to stop providing objects to be serialized, simply return null.

You provide an instance of this interface as an argument to `DataMarshaller#marshall`.

In the example bellow, the method `getOrderByIndex` will return the object to be serialized using its index position provided by DataStream:

```java
	DataMarshaller m = context.createMarshaller(StreamFileTypeXML.class);
    m.marshall(f, new ObjectProvider() {
        @Override
        public Object getObjectToSerialize(int index) {
            return getOrderByIndex(index);
        }
    });

```

`ObjectConsumer` - Use this when you want to consume the objects read from XML stream as they are read by DataStream.

You provide an instance of this interface as an argument to `DataStream#unmarshall`.

In the example below, the method `onNewObject` will be called for each object read from the XML.

```java
DataUnmarshaller um = context.createUnmarshaller(StreamFileTypeXML.class);
um.unmarshall(fin, new ObjectConsumer() {
    @Override
    public void onNewObject(Object object) {
    	// called when object is read from XML stream
        order2 = (Order)object;
    }

    @Override
    public void startObjectReading(Class objectClass) {
        // called if you want to take any action before reading the object
    }
});
``

### Nested object properties

You may map nested object properties as single nodes (or attributes) in XML. For example, consider the mapping:

```xml
<objectGraph name="order" class="com.rmemoria.datastream.test.model.Order"
             ignorePropsNotDeclared="true"
             includeNullValues="false">
    <property name="id" xmlAttribute="true" />
    <property name="customer.id" elementName="customerId" xmlAttribute="true" />
    <property name="customer.address.street" elementName="street" use="REQUIRED"/> 
    <property name="customer.address.number" elementName="streetNumber" />
    <property name="customer.address.area" elementName="addressArea" /> 
    <property name="customer.address.zip" elementName="addressZip" />
    <property name="customer.address2.street" elementName="street2" /> 
</objectGraph>
```

When serializing it to an XML, the output would be:

```xml
<?xml version="1.0"?>
<order id="100" customerId="200" email="email@test.com" customerName="customer name">
    <street>St. Street</street>
    <streetNumber>5000</streetNumber>
    <addressArea>RURAL</addressArea>
</order>
```

When reading it back, the object model will be constructed again (and all its nested object properties).

### Object creation interceptor

When writing\reading an XML file, it is possible to intercept some specific events using the interface bellow:

```java
public interface DataInterceptor {

	Class getObjectClass(Object obj);

	Object newObject(Class objectType, Map<String, Object> params);
}
```

`getObjectClass` - Before deserializing a new object from XML, DataStream will call this method to determine the class (parent class) used.

`newObject` - When reading the object properties from XML, this method can be used to initialize an instance of an object. For example, when the XML just carries the ID of a given class object (for example, an Hibernate entity).

Interceptors are added in the context level. To include a new interceptor, call the method `StreamContext#addInterceptor`.

```java
context.addInterceptor(myInterceptor);
```

### Custom properties

Sometimes you want to include extra nodes in the XML file (or read from it) that are not properties of the object used (for example, meta-data).

In order to provide such feature, there are two interfaces available:

```java
public interface CustomPropertiesReader {

	Map<String, Object> readCustomProperties(Object object);
}

public interface CustomPropertiesWriter {

	void writeCustomProperties(Object object, Map<String, String> props);
}
```

`CustomPropertyReader` is called before serializing an object to XML. The method will receive the object and will return any extra information to be included in the XML.

`CustomPropertyWriter` is called when reading an XML data. Any node that doesn't match a object property will be considered a custom property and will be passed as argument in a Map structure.

### Type converter

Allows you to create a custom type converter for a property type. You must specific an instance of the interface below:

```java
public interface DataConverter {

	String convertToString(Object obj);
	
	Object convertFromString(String data, Class classType);
}
```

And register it using the method `StreamContext#setConverter(Class type, DataConverter converter)` passing the class you want to provide specific converter and the instance of the `DataConverter`.
`

## License

The content of this project itself is licensed under the Creative Commons Attribution 3.0 license, and the underlying source code used to format and display that content is licensed under the MIT license.

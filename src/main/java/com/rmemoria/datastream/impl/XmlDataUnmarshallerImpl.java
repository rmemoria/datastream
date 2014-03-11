/**
 * 
 */
package com.rmemoria.datastream.impl;

import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.rmemoria.datastream.DataConverter;
import com.rmemoria.datastream.DataUnmarshaller;
import com.rmemoria.datastream.ObjectConsumer;

/**
 * Unmarshall an object from its XML representation to its object (or several objects, if it's
 * inside a collection of objects). </p>
 * The approach done was using Java SAX to read the XML in a stream way. The Advantage of SAX 
 * is that data can be read from an input stream as data comes. Because SAX works on a callback
 * way, informing when each node is read, the implementation uses a {@link Stack} object to
 * save the object property values from the outer node to the inner node. Inner objects
 * are read and marshalled first.
 * 
 * @author Ricardo Memoria
 *
 */
public class XmlDataUnmarshallerImpl implements DataUnmarshaller {

	private StreamContextImpl context;
	private CollectionMetaData currentCollection;
	private Collection results;
	private ObjectConsumer consumer;
	// indicate the current node selection
	private NodeSelection node;
	
	private Deque<ObjectValues> objects = new ArrayDeque<ObjectValues>();
	
	public XmlDataUnmarshallerImpl(StreamContextImpl context) {
		this.context = context;
	}
	
	/** {@inheritDoc}
	 */
	@Override
	public Object unmarshall(InputStream xmlstream) {
		// this collection will receive the objects
		results = new ArrayList();

		startParse(xmlstream);

		// if it's a list defined in the schema as the root element, so returns the result list
		if ((currentCollection != null) || (results.size() > 1))
			return results;

		if (results.size() == 0)
			return null;
		else return ((ArrayList)results).get(0);
	}


	/** {@inheritDoc}
	 */
	@Override
	public void unmarshall(InputStream stream, ObjectConsumer consumer) {
		this.consumer = consumer;
		startParse(stream);
	}
	
	
	/**
	 * Start the parse of the XML input stream
	 * @param xmlstream
	 */
	protected void startParse(InputStream xmlstream) {
		// create SAX parser and its default handler for element reading
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser;
		try {
			parser = factory.newSAXParser();
			
			DefaultHandler handler = new DefaultHandler() {
				/** {@inheritDoc}
				 */
				@Override
				public void startElement(String uri, String localName, String qName,
						Attributes attributes) throws SAXException {
					saxStartElement(qName, attributes);
				}

				/** {@inheritDoc}
				 */
				@Override
				public void endElement(String uri, String localName, String qName)
						throws SAXException {
					saxEndElement(qName);
				}

				/** {@inheritDoc}
				 */
				@Override
				public void characters(char[] ch, int start, int length)
						throws SAXException {
					saxCharacters(new String(ch, start, length));
				}
			};

			parser.parse(xmlstream,  handler);

		} catch (Exception e) {

			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Notification of SAX about the starting of an element
	 * @param name
	 * @param attributes
	 */
	protected void saxStartElement(String name, Attributes attributes) {
		// is the first node?
		if (node == null) {
			// check if it's the collection node
			if ((currentCollection == null) && (context.getCollectionMetaData() != null)) {
				// get information about the collection
				currentCollection = context.getCollectionMetaData();
				// if the node name is not the same as expected, raise an error
				String collname = currentCollection.getObjectCollection().getName();
				if (!collname.equals(name))
					throw new IllegalArgumentException("Expected element '" + collname + "' but found '" + name + "'");
			}
			else {
				// initialize the node for the first selection, which must be a class
				ClassMetaData cmd = context.findClassByElement(name);
				if (cmd == null)
					throw new IllegalArgumentException("No class found for element " + name);
				node = new NodeSelection(null, cmd);
				startClass(attributes);
				if (consumer != null)
					consumer.startObjectReading(cmd.getGraphClass());
			}
			return;
		}

		// new element being read is a PROPERTY? (it is if the current selection is a class)
		if (node.isClassSelection()) {
			PropertyMetaData prop = node.getClassMetaData().findPropertyByElementName(name);
			if (prop == null)
				throw new IllegalArgumentException("No property found for element " + name + 
						" in class " + node.getClassMetaData().getGraphClass().getName());

			node = new NodeSelection(node, prop);
			// is an one-to-one relationship between objects ?
			if ((prop.getCompactibleTypeMetaData() != null) && (!prop.isCollection())) {
				node = new NodeSelection(node, prop.getCompactibleTypeMetaData());
				startClass(attributes);
			}
			return;
		}

		// is a property node ?
		// it's an entity pointed in a property
		// if there is no type defined in the property, so there is an error
		ClassMetaData cmd = node.getPropertyMetaData().getCompactibleTypeMetaData();
		if (cmd == null)
			throw new IllegalArgumentException("A new element was found in property but no graph defined for property " 
					+ node.getPropertyMetaData() + ": element " + name);
		node = new NodeSelection(node, cmd);

		startClass(attributes);
	}

	
	/**
	 * @param name
	 * @param attributes
	 */
	protected void startClass(Attributes attributes) {
		ClassMetaData currentClass = node.getClassMetaData();
		ObjectValues vals = new ObjectValues(currentClass);
		// add in the stack
		objects.push(vals);
		
		// get attribute values defined in the element
		if (attributes != null) {
			for (int i = 0; i < attributes.getLength(); i++) {
				String propname = attributes.getQName(i);
				String propvalue = attributes.getValue(i);
				PropertyMetaData prop = currentClass.findPropertyByElementName(propname);
				// if the attribute is not a property, raise an exception
				if (prop == null)
					throw new IllegalArgumentException("No property found for element " + propname + 
							" in class " + currentClass.getGraphClass().getName());
				Class type = prop.getConvertionType();
				DataConverter conv = context.findConverter(type);
				Object val = conv.convertFromString(propvalue, type);
				
				vals.getValues().put(prop, val);
			}
		}
	}

		
	/**
	 * Called by SAX when it ends the reading of an XML element
	 * @param name is the name of the XML element
	 */
	protected void saxEndElement(String name) {
		// if it's the root node, do nothing
		if (node == null)
			return;
		
/*		if ((currentClass == null) && (currentProperty == null))
			return;
*/

		// is a property being ended
		if (node.isPropertySelection()) {
			// end property tag
			node = node.getParent();
		}
		else {
			// end class tag
			node = node.getParent();

			ObjectValues vals = objects.pop();
			Object obj = createObject(vals);

			// is parent node a property ?
			if ((node != null) && (node.isPropertySelection())) {
				// take the parent object values
				ObjectValues parent = objects.pop();
				PropertyMetaData prop = node.getPropertyMetaData();
				// parent property is a collection ?
				if (prop.isCollection()) {
					// get the collection from values
					Collection lst = (Collection)parent.getValues().get(prop);
					if (lst == null) {
						lst = new HashSet();
						parent.getValues().put(prop, lst);
					}
					lst.add(obj);
				}
				else {
					parent.getValues().put(prop, obj);
					// because it's a one to one entity relationship, it moves from the current class to the parent class
					node = node.getParent();
				}
				objects.push(parent);
			}
			else {
				// new object is created
				if (consumer != null)
					 consumer.onNewObject(obj);
				else results.add(obj);
			}
		}
	}

	
	/**
	 * Create an instance of the object using the current class meta data and the list
	 * of property values
	 * @param vals list of property values
	 * @return instance of the object
	 */
	protected Object createObject(ObjectValues vals) {
		// create an instance of the object
		Object obj = context.createInstance(vals.getClassMetaData().getGraphClass(), getObjectAttributes(vals));
//		Object obj = context.createInstance(currentClass.getGraphClass(), getObjectAttributes(vals));
		// set the values of the properties
		for (PropertyMetaData prop: vals.getValues().keySet()) {
			Object value = vals.getValues().get(prop);
			prop.setValue(context, obj, value);
		}
		return obj;
	}
	
	/**
	 * Mount the list of properties for the creation of the object
	 * @param vals
	 * @return
	 */
	protected Map<String, Object> getObjectAttributes(ObjectValues vals) {
		Map<String, Object> props = new HashMap<String, Object>();
		for (PropertyMetaData prop: vals.getValues().keySet()) {
			props.put(prop.getPath(), vals.getValues().get(prop));
		}
		return props;
	}
	
	
	/**
	 * Called by SAX when reading the content of an XML element
	 * @param value the content of the element
	 */
	protected void saxCharacters(String value) {
		if ((node == null) || (value == null))
			return;

		value = value.trim();
		
		// get the content of the current property being read
		if (node.isPropertySelection()) {
			PropertyMetaData prop = node.getPropertyMetaData();
			Class type = prop.getConvertionType();
			DataConverter conv = context.findConverter(type);
			Object val = conv.convertFromString(value, type);
			ObjectValues vals = objects.pop();
			vals.getValues().put(prop, val);
			objects.push(vals);
			return;
		}

		if (node.isClassSelection()) {
			throw new IllegalArgumentException("A class element cannot have a content: " + node.getClassMetaData().getGraph().getName());
		}
	}

}

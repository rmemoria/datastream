/**
 * 
 */
package com.rmemoria.datastream.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.rmemoria.datastream.DataConverter;
import com.rmemoria.datastream.DataStreamException;
import com.rmemoria.datastream.DataUnmarshaller;
import com.rmemoria.datastream.ObjectConsumer;
import com.rmemoria.datastream.jaxb.ObjectGraph;
import com.rmemoria.datastream.jaxb.Property;
import com.rmemoria.datastream.jaxb.PropertyUse;

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

		} catch (SAXException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (ParserConfigurationException e) {
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
					throw new DataStreamException(getNodeHistory() +  ": Expected element '" + collname + "' but found '" + name + "'");
			}
			else {
				// initialize the node for the first selection, which must be a class
				ClassMetaData cmd = context.findClassByElement(name);
				if (cmd == null)
					throw new DataStreamException(getNodeHistory() + ": No class mapped for element " + name);
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
				throw new DataStreamException(node.getClassMetaData(), null, getNodeHistory() + ": Invalid element " + name + 
						" in node " + node.getClassMetaData().getGraph().getName());

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
			throw new DataStreamException(node.getPropertyMetaData().getClassMetaData(), 
				node.getPropertyMetaData(), getNodeHistory() + ": " +  
				"A new element was found in property but no graph defined for property " + node.getPropertyMetaData() + ": element " + name);
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
				String elemName = attributes.getQName(i);
				String propvalue = attributes.getValue(i);
				PropertyMetaData prop = currentClass.findPropertyByElementName(elemName);
				// if the attribute is not a property, raise an exception
				if (prop == null)
					throw new DataStreamException(currentClass, null, getNodeHistory() +  ": Invalid element " + elemName + 
							" in node " + currentClass.getGraph().getName());
				Class type = prop.getConvertionType();
				DataConverter conv = context.findConverter(type);
				Object val = conv.convertFromString(propvalue, type);
				
				vals.addValue(prop.getPath(), val);
//				vals.getValues().put(prop, val);
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
			
			checkRequiredProperties(vals);
	
			Object obj = vals.createObject(context);
//			Object obj = createObject(vals);

			// is parent node a property ?
			if ((node != null) && (node.isPropertySelection())) {
				// take the parent object values
				ObjectValues parent = objects.pop();
				PropertyMetaData prop = node.getPropertyMetaData();
				// parent property is a collection ?
				if (prop.isCollection()) {
					// get the collection from values

//					Collection lst = (Collection)parent.getValues().get(prop);
					Collection lst = (Collection)parent.getValue(prop.getPath());
					if (lst == null) {
						lst = new HashSet();
						parent.addValue(prop.getPath(), lst);
//						parent.getValues().put(prop, lst);
					}
					lst.add(obj);
				}
				else {
//					parent.getValues().put(prop, obj);
					parent.addValue(prop.getPath(), obj);
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
	 * Check if there is a missing property that is required
	 * @param vals
	 */
	private void checkRequiredProperties(ObjectValues vals) {
		ObjectGraph graph = vals.getClassMetaData().getGraph();
		for (Property prop: graph.getProperty()) {
			if (prop.getUse() == PropertyUse.REQUIRED) {
				Object val = vals.getValue(prop.getName());
				if (val == null) {
					String s = "Property '" + vals.getClassMetaData().getGraph().getName() + "."  + prop.getElementName() + "' is required";
					throw new DataStreamException(getNodeHistory() + ": " +  s);
				}
			}
		}
/*		for (PropertyMetaData prop: vals.getClassMetaData().getProperties()) {
			// is property required ?
			if (prop.getProperty() != null) {
				if ((prop.getProperty().getUse() == PropertyUse.REQUIRED) && (vals.getValues().get(prop) == null)) {
					String s = "Property '" + vals.getClassMetaData().getGraph().getName() + "."  + prop.getElementName() + "' is required";
					throw new DataStreamException(getNodeHistory() + ": " +  s);
				}
			}
		}
*/	}

	/**
	 * Create an instance of the object using the current class meta data and the list
	 * of property values
	 * @param vals list of property values
	 * @return instance of the object
	 */
/*	protected Object createObject(ObjectValues vals) {
		// create an instance of the object
		Object obj = context.createInstance(vals.getClassMetaData().getGraphClass(), getObjectAttributes(vals));

		// set the values of the properties
		List<PropertyValues> props = vals.groupProperties();
		for (PropertyValues prop: props) {
			prop.applyValues(context, obj);
		}
		return obj;
	}
*/	
	
	/**
	 * Mount the list of properties for the creation of the object
	 * @param vals
	 * @return
	 */
/*	protected Map<String, Object> getObjectAttributes(ObjectValues vals) {
		Map<String, Object> props = new HashMap<String, Object>();
		for (PropertyMetaData prop: vals.getValues().keySet()) {
			props.put(prop.getPath(), vals.getValues().get(prop));
		}
		return props;
	}
*/	
	
	/**
	 * Called by SAX when reading the content of an XML element
	 * @param value the content of the element
	 */
	protected void saxCharacters(String value) {
		if ((node == null) || (value == null))
			return;

		value = value.trim();
		if (value.isEmpty())
			return;
		
		// get the content of the current property being read
		if (node.isPropertySelection()) {
			PropertyMetaData prop = node.getPropertyMetaData();
			Class type = prop.getConvertionType();
			DataConverter conv = context.findConverter(type);
			Object val = conv.convertFromString(value, type);
			ObjectValues vals = objects.pop();
			vals.addValue(prop.getPath(), val);
//			vals.getValues().put(prop, val);
			objects.push(vals);
			return;
		}

		if (node.isClassSelection()) {
			throw new DataStreamException(node.getClassMetaData(), 
				null, getNodeHistory() +
				": A class element cannot have a content: " + node.getClassMetaData().getGraph().getName());
		}
	}

	
	/**
	 * Return a text containing the displayable history of the current node
	 * @return String value
	 */
	protected String getNodeHistory() {
		String s = "";
		for (ObjectValues obj: objects) {
			if (!s.isEmpty()) {
				s += ", ";
			}
			s += obj.getClassMetaData().getGraph().getName();
			if ((obj.getProperties() != null) && (obj.getProperties().size() > 0)) {
				String text = "";
				for (PropertyValue prop: obj.getProperties()) {
					if (!text.isEmpty()) {
						text += ", ";
					}
					String propname = prop.getProperty().getElementName() != null? prop.getProperty().getElementName(): prop.getProperty().getPropertyName();
					text += propname + "=" + prop.getValue();
				}
				s += "[" + text + "]"; 
			}
		}
		return s;
	}
}

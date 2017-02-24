/**
 * 
 */
package com.rmemoria.datastream.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.rmemoria.datastream.CustomPropertiesWriter;
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

	/**
	 * These are the possible node types being read  
	 */
	public enum NodeType { ROOT, CLASS, PROPERTY, CUSTOM_PROPERTIES; };


	private StreamContextImpl context;
	private CollectionMetaData currentCollection;
	private Collection results;
	private ObjectConsumer consumer;
	// indicate the current node selection
	private NodeSelection node;
	private List<CustomPropertiesWriter> propWriters;
	
	private Deque<ObjectValues> objects = new ArrayDeque<ObjectValues>();
	private Map<String, String> customProperties;
	private String customPropName;

    private boolean saxCharacterCalled;

	
	/**
	 * Default constructor, receiving the context as parameter
	 * @param context instance of the {@link StreamContextImpl}
	 */
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

            InputSource is = new InputSource(new InputStreamReader(xmlstream, "UTF-8"));
			parser.parse(is,  handler);

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
		NodeType nodeType = getNodeType();
        saxCharacterCalled = false;

		switch (nodeType) {
		case ROOT:
			startRootNode(name, attributes);
			break;
		case CLASS: // it means that the current selection is a class, so the new element being read is a property
			startPropertyNode(name, attributes);
			break;
		case PROPERTY:
			startClassNode(name, attributes);
			break;
		case CUSTOM_PROPERTIES:
			startCustomPropertiesNode(name, attributes);
		}
	}


	/**
	 * Called when the current node is a custom property of the object
	 * @param name
	 * @param attributes
	 */
	protected void startCustomPropertiesNode(String name, Attributes attributes) {
		customPropName = name;
	}

	
	/**
	 * Called when the root node in the XML document is being read 
	 * @param name is the root node name in the XML document
	 * @param attributes is the list of attributes declared in the node, if available
	 */
	protected void startRootNode(String name, Attributes attributes) {
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
	}
	
	
	/**
	 * Called when reading an XML node which is an object structure
	 * @param name is the XML node name
	 * @param attributes is the list of attributes, if available, declared in the XML node
	 */
	protected void startClassNode(String name, Attributes attributes) {
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
	 * Called when reading an XML node which is a property value
	 * @param name is the XML node name
	 * @param attributes is the list of attributes, if available, declared in the XML node
	 */
	protected void startPropertyNode(String name, Attributes attributes) {
		// is this node a custom property node?
		if (name.equals(node.getClassMetaData().getGraph().getCustomPropertiesNode())) {
			customProperties = new HashMap<String, String>();
		}
		else {
			// it's another property being handled
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
		}
	}
	
	/**
	 * Start reading a new class 
	 * @param attributes is the list of attributes declared in the XML document node
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

                Object val = convertValueFromString(prop, propvalue);
//				Class type = prop.getConvertionType();
//				DataConverter conv = context.findConverter(type);
//				Object val = conv.convertFromString(propvalue, type);
				
				vals.addValue(prop.getPath(), val);
//				vals.getValues().put(prop, val);
			}
		}
	}


    /**
     * Convert a value from string to the original type according to the given property meta data
     * @param prop
     * @param value
     * @return the original value
     */
    protected Object convertValueFromString(PropertyMetaData prop, String value) {
        Object val;

        // is value null ?
        if (value != null && value.isEmpty()) {
            return Constants.NULL_VALUE;
        }
        else {
            Class type = prop.getConvertionType();
            DataConverter conv = context.findConverter(type);
            return conv.convertFromString(value, type);
        }
    }

		
	/**
	 * Called by SAX when it ends the reading of an XML element
	 * @param name is the name of the XML element
	 */
	protected void saxEndElement(String name) {
		switch (getNodeType()) {
		case ROOT:
			return;
		
		case PROPERTY:
			endPropertyNode(name);
			break;
		
		case CLASS:
			endClassNode(name);
			break;
		
		case CUSTOM_PROPERTIES:
			endCustomPropertiesNode(name);
			break;
		}
	}

	
	/**
	 * Called when finishing reading a node that represents a property
	 * @param nodeName is the XML node name
	 */
	protected void endPropertyNode(String nodeName) {
        if (!saxCharacterCalled) {
            handleContentProperty(null);
        }
		// end property tag
		node = node.getParent();
	}
	
	
	/**
	 * Called when finishing reading a node that represents a class
	 * @param nodeName is the XML node name
	 */
	protected void endClassNode(String nodeName) {
		// end class tag
		node = node.getParent();

		ObjectValues vals = objects.pop();

        Object obj;
        // if there is no property, so the object is null
        if (vals.getProperties().size() != 0) {
            checkRequiredProperties(vals);

            obj = vals.createObject(context);

            if (vals.getCustomProperties() != null) {
                notifyCustomPropWriters(obj, vals.getCustomProperties());
            }
        }
        else {
            obj = null;
        }
		

		// is parent node a property ?
		if ((node != null) && (node.isPropertySelection())) {
			// take the parent object values
			ObjectValues parent = objects.pop();
			PropertyMetaData prop = node.getPropertyMetaData();
			// parent property is a collection ?
			if (prop.isCollection()) {
				// get the collection from values

				Collection lst = (Collection)parent.getValue(prop.getPath());
				if (lst == null) {
					lst = new HashSet();
					parent.addValue(prop.getPath(), lst);
				}

                if (obj != null) {
                    lst.add(obj);
                }
			}
			else {
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

	
	/**
	 * Notify all custom property writers about custom properties read for this object
	 * @param obj the object that custom properties were read in the document
	 * @param customProperties the custom properties read in the XML document
	 */
	protected void notifyCustomPropWriters(Object obj, Map<String, String> customProperties) {
		if (propWriters == null) {
			return;
		}
		
		for (CustomPropertiesWriter writer: propWriters) {
			writer.writeCustomProperties(obj, customProperties);
		}
	}

	/**
	 * Called when finishing reading a node that represents a custom property
	 * @param nodeName is the XML node name
	 */
	protected void endCustomPropertiesNode(String nodeName) {
		// the node is a property value ?
		if (customPropName != null) {
			customPropName = null;
		}
		else {
			// finished reading all the custom property values.
			ObjectValues objvals = objects.getLast();
			objvals.setCustomProperties(customProperties);
			customProperties = null;
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
                    String pname = prop.getElementName() != null? prop.getElementName(): prop.getName();
					String s = "Property '" + vals.getClassMetaData().getGraph().getName() + "."  + pname + "' is required";
					throw new DataStreamException(getNodeHistory() + ": " +  s);
				}
			}
		}
	}


	
	/**
	 * Called by SAX when reading the content of an XML element
	 * @param value the content of the element
	 */
	protected void saxCharacters(String value) {
        saxCharacterCalled = true;

        if (value == null)
			return;

		value = value.trim();
		if (value.isEmpty())
			return;

		switch (getNodeType()) {
		case ROOT:
			return;

		case PROPERTY:
			handleContentProperty(value);
			break;
			
		case CLASS:
			handleContentClass(value);
			break;
			
		case CUSTOM_PROPERTIES:
			handleContentCustomProperty(value);
		}
	}

	
	/**
	 * handle the text content of the XML node when the current node is a property
	 * @param value is the node text content
	 */
	protected void handleContentProperty(String value) {
        PropertyMetaData prop = node.getPropertyMetaData();

        Object val = convertValueFromString(prop, value);
//        if (value == null || value.isEmpty()) {
//            val = null;
//        }
//        else {
//            Class type = prop.getConvertionType();
//            DataConverter conv = context.findConverter(type);
//            val = conv.convertFromString(value, type);
//        }

		ObjectValues vals = objects.pop();
        if (vals.findPropertyValue(prop) == null) {
            vals.addValue(prop.getPath(), val);
        }
		objects.push(vals);
	}
	
	
	/**
	 * handle the text content of the XML node when the current node is a class
	 * @param value is the node text content
	 */
	protected void handleContentClass(String value) {
		throw new DataStreamException(node.getClassMetaData(), 
				null, getNodeHistory() +
				": A class element cannot have a content: " + node.getClassMetaData().getGraph().getName());
	}
	
	
	/**
	 * handle the text content of the XML node when the current node is a custom property
	 * @param value is the node text content
	 */
	protected void handleContentCustomProperty(String value) {
		// if there is no custom property name, so the content belongs to the custom property node
		if (customPropName == null) {
			throw new DataStreamException(node.getClassMetaData(), 
					null, 
					getNodeHistory() + ":" + node.getClassMetaData().getGraph().getCustomPropertiesNode() + 
					" Node cannot have a content, just other nodes");
		}
		
		// set the content of the custom property
		customProperties.put(customPropName, value);
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


	/** {@inheritDoc}
	 */
	@Override
	public void addPropertyWriter(CustomPropertiesWriter writer) {
		if (propWriters == null) {
			propWriters = new ArrayList<CustomPropertiesWriter>();
		}
		propWriters.add(writer);
	}


	/** {@inheritDoc}
	 */
	@Override
	public void removePropertyWriter(CustomPropertiesWriter writer) {
		if (propWriters == null) {
			return;
		}
		propWriters.remove(writer);
	}

	/**
	 * Return the type of node being read
	 * @return instance of the enumeration {@link NodeType}
	 */
	public NodeType getNodeType() {
		if (node == null) {
			return NodeType.ROOT;
		}
		
		if (customProperties != null) {
			return NodeType.CUSTOM_PROPERTIES;
		}
		
		if (node.isClassSelection()) {
			return NodeType.CLASS;
		}
		else {
			return NodeType.PROPERTY;
		}
	}
	
}

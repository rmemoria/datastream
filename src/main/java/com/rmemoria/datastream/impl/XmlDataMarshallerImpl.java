/**
 * 
 */
package com.rmemoria.datastream.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.rmemoria.datastream.CustomPropertiesReader;
import com.rmemoria.datastream.DataConverter;
import com.rmemoria.datastream.DataMarshaller;
import com.rmemoria.datastream.DataStreamException;
import com.rmemoria.datastream.ObjectProvider;

/**
 * Implementation of the {@link DataMarshaller} interface for XML data type.
 * Generates an XML file from an object or a list of objects
 * 
 * @author Ricardo Memoria
 *
 */
public class XmlDataMarshallerImpl implements DataMarshaller {

	private StreamContextImpl context;
	private XMLStreamWriter xml;
	private Set<CustomPropertiesReader> propReaders;
	
	/**
	 * Default constructor
	 * @param context
	 */
	public XmlDataMarshallerImpl(StreamContextImpl context) {
		this.context = context;
	}

	/** {@inheritDoc}
	 * @throws IOException 
	 */
	@Override
	public void marshall(Object obj, OutputStream out) {
		startMarshall(out);
		try {
			// object is a collection ?
			if (obj instanceof Collection) {
				// was a collection defined in the root element ?
				CollectionMetaData colMetaData = context.getCollectionMetaData();
				if (colMetaData == null)
					throw new DataStreamException("Collection instance was not expected for marshalling");

				// marshall the collection
				marshallCollection((Collection)obj, colMetaData);
			}
			else {
				ClassMetaData cmd = context.findClassMetaData(obj);
				if (cmd == null)
					throw new DataStreamException("No schema defined for object of class " + obj.getClass().getName());
				// write a single object
				createXml(obj, cmd, true);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		finishMarshall();
	}
	

	/** {@inheritDoc}
	 * @throws XMLStreamException 
	 */
	@Override
	public void marshall(OutputStream output, ObjectProvider provider) {
		startMarshall(output);
		try {

			// is list ?
			if (context.getCollectionMetaData() != null) {
				CollectionMetaData cmd = context.getCollectionMetaData();
				xml.writeStartElement(cmd.getObjectCollection().getName());
				Object obj;
				int index = 0;
				// mount the list while there is object from the provider
				while ((obj = provider.getObjectToSerialize(index)) != null) {
					ClassMetaData meta = getClassMetadata(obj);
					createXml(obj, meta, true);
					index++;
				}
				xml.writeEndElement();
			}
			else {
				// serialize single object
				Object obj = provider.getObjectToSerialize(0);
				ClassMetaData cmd = getClassMetadata(obj);
				createXml(obj, cmd, true);
			}
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		finishMarshall();
	}

	
	/**
	 * Find instance of {@link ClassMetaData} associated to this object. If none is found, 
	 * an {@link IllegalArgumentException} is thrown
	 * @param object is the object to search for its class meta data
	 * @return instance of {@link ClassMetaData}
	 */
	private ClassMetaData getClassMetadata(Object object) {
		ClassMetaData cmd = context.findClassMetaData(object);
		if (cmd == null)
			throw new IllegalArgumentException("No schema defined for object of class " + object.getClass().getName());
		return cmd;
	}
	
	/**
	 * Execute the procedures to start marshalling the object
	 * @param out instance of the {@link OutputStream} that will reveive the data
	 */
	protected void startMarshall(OutputStream out) {
		try {
			if (xml != null)
				finishMarshall();
			xml = XMLOutputFactory.newInstance().createXMLStreamWriter(out);

			xml.writeStartDocument();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	
	/**
	 * Generate the XML data from a collection of objects
	 * @param lst instance of {@link Collection} containing the list of objects
	 * @param meta instance of {@link CollectionMetaData} with schema information about the list
	 * @throws XMLStreamException
	 */
	protected void marshallCollection(Collection lst, CollectionMetaData meta) throws XMLStreamException {
		xml.writeStartElement(meta.getObjectCollection().getName());
		for (Object item: lst) {
			ClassMetaData cmd = context.findClassMetaData(item);
			if (cmd == null)
				throw new IllegalArgumentException("Class not defined to be serialized: " + item.getClass().getName());
			createXml(item, cmd, true);
		}
		xml.writeEndElement();
	}


	/**
	 * Create an XML element from an object
	 * @param obj
     * @param cmd
	 * @param includeClassElement
	 * @return
	 * @throws XMLStreamException 
	 */
	protected void createXml(Object obj, ClassMetaData cmd, boolean includeClassElement) throws XMLStreamException {
		if (includeClassElement)
			xml.writeStartElement(cmd.getGraph().getName());

		List<PropertyMetaData> props = cmd.getEndPointProperties();

		// write attributes
		for (PropertyMetaData prop: props) {
			if ((!prop.isSerializationIgnored()) && (prop.isXmlAttribute())) {
				Object value = prop.getValue(obj);

                // value is different of null or include even null values?
				if ((value != null) || (prop.getClassMetaData().getGraph().isIncludeNullValues())) {
					String text = convertToString(value);
					if (text != null)
						xml.writeAttribute(prop.getElementName(), text);
				}
			}
		}

		// write elements
		for (PropertyMetaData prop: props) {
			if ((!prop.isSerializationIgnored()) && (!prop.isXmlAttribute())) {
				Object value = prop.getValue(obj);
				if (value != null || prop.getClassMetaData().getGraph().isIncludeNullValues()) {
					// serialize it as an XML element
					xml.writeStartElement(prop.getElementName());
                    // just write content if there is any value, otherwise just close the tag indicating an empty value
                    if (value != null) {
                        if (prop.getCompactibleTypeMetaData() != null) {
                            // serialize the object that this property points to
                            // the property is of collection type ?
                            if (prop.isCollection()) {
                                // serialize all items of the collection
                                Collection lst = (Collection)value;
                                for (Object item: lst) {
                                    createXml(item, prop.getCompactibleTypeMetaData(), true);
                                }
                            }
                            else {
                                // serialize the object pointed by the collection
                                createXml(value, prop.getCompactibleTypeMetaData(), false);
                            }
                        }
                        else {
                            String text = convertToString(value);
                            if (text != null)
                                xml.writeCharacters(text);
                        }
                    }
					xml.writeEndElement();
				}
			}
		}

		handleCustomProperties(obj, cmd);
		
		if (includeClassElement)
			xml.writeEndElement();
	}


	/**
	 * Handle possible custom properties defined for the given object. In order to include custom
	 * properties, it's necessary to define the node name (in the XML schema, as customPropertiesNode)
	 * and implement the interface {@link CustomPropertiesReader} 
	 * @param obj
	 * @param cmd
	 * @throws XMLStreamException
	 */
	protected void handleCustomProperties(Object obj, ClassMetaData cmd) throws XMLStreamException {
		// any custom node was defined for this object graph?
		String node = cmd.getGraph().getCustomPropertiesNode();
		if (node == null) {
			return;
		}
		
		// get custom properties
		Map<String, Object> props = getCustomProperties(obj);
		if (props == null) {
			return;
		}

		// create new node for custom objects
		xml.writeStartElement(node);
		for (String propname: props.keySet()) {
			Object value = props.get(propname);
			if (value != null) {
				// start new element inside the custom node
				xml.writeStartElement(propname);
				// write custom element
				String text = convertToString(value);
				if (text != null) {
					xml.writeCharacters(text);
				}
				xml.writeEndElement();
			}
		}
		xml.writeEndElement();
	}
	
	/**
	 * Return the custom property values from the given object
	 * @param object the object to get the custom properties from
	 * @return map containing property names and its value
	 */
	protected Map<String, Object> getCustomProperties(Object object) {
		// is there any property reader defined?
		if (propReaders == null) {
			return null;
		}

		// get values from all property readers defined
		Map<String, Object> customProps = null;
		for (CustomPropertiesReader propReader: propReaders) {
			Map<String, Object> values = propReader.readCustomProperties(object);
			if (customProps == null) {
				customProps = values;
			}
			else {
				customProps.putAll(values);
			}
		}
		return customProps;
	}


	/**
	 * Finish the XML document marshall 
	 */
	public void finishMarshall() {
		if (xml == null)
			throw new DataStreamException("Object marshall must be initialized before being finished");
		
		try {
			xml.writeEndDocument();
			xml.flush();
			xml = null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Convert an object (usually a primitive type) to string
	 * @param value
	 * @return
	 */
	protected String convertToString(Object value) {
        if (value == null) {
            return "";
        }
		DataConverter conv = context.findConverter(value.getClass());
		return conv.convertToString(value);
	}

	/** {@inheritDoc}
	 */
	@Override
	public void addPropertyReader(CustomPropertiesReader reader) {
		if (propReaders == null)  {
			propReaders = new HashSet<CustomPropertiesReader>();
		}
		propReaders.add(reader);
	}

	/** {@inheritDoc}
	 */
	@Override
	public void removePropertyReader(CustomPropertiesReader reader) {
		if (propReaders == null) {
			return;
		}
		propReaders.remove(reader);
	}
}

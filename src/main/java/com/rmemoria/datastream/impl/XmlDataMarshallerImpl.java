/**
 * 
 */
package com.rmemoria.datastream.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.rmemoria.datastream.DataConverter;
import com.rmemoria.datastream.DataMarshaller;
import com.rmemoria.datastream.DataStreamException;
import com.rmemoria.datastream.ObjectProvider;

/**
 * Implementation of the {@link DataMarshaller} interface for XML data type
 * 
 * @author Ricardo Memoria
 *
 */
public class XmlDataMarshallerImpl implements DataMarshaller {

	private StreamContextImpl context;
	private XMLStreamWriter xml;
	
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
					throw new IllegalArgumentException("Collection instance was not expected for marshalling");

				// marshall the collection
				marshallCollection((Collection)obj, colMetaData);
			}
			else {
				ClassMetaData cmd = context.findClassMetaData(obj);
				if (cmd == null)
					throw new IllegalArgumentException("No schema defined for object of class " + obj.getClass().getName());
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
	 * @param cmd
	 * @param obj
	 * @param parentProperty
	 * @return
	 * @throws XMLStreamException 
	 */
	protected void createXml(Object obj, ClassMetaData cmd, boolean includeClassElement) throws XMLStreamException {
		if (includeClassElement)
			xml.writeStartElement(cmd.getGraph().getName());

		// write attributes
		for (PropertyMetaData prop: cmd.getProperties()) {
			if ((!prop.isSerializationIgnored()) && (prop.isXmlAttribute())) {
				Object value = prop.getValue(obj);
				if (value != null) {
					String text = convertToString(value);
					if (text != null)
						xml.writeAttribute(prop.getElementName(), text);
				}
			}
		}

		// write elements
		for (PropertyMetaData prop: cmd.getProperties()) {
			if (!prop.isSerializationIgnored()) {
				Object value = prop.getValue(obj);
				if (value != null) {
					// property must be serialized as an attribute ?
					if (!prop.isXmlAttribute()) {
						// serialize it as an XML element
						xml.writeStartElement(prop.getElementName());
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
						xml.writeEndElement();
					}
				}
			}
		}

		if (includeClassElement)
			xml.writeEndElement();
	}
	
	
	public void finishMarshall() {
		if (xml == null)
			throw new DataStreamException("Object marshall must be initialized before being finished");
		
		try {
/*			if (rootElementName != null)
				xml.writeEndElement();
*/			xml.writeEndDocument();
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
		DataConverter conv = context.findConverter(value.getClass());
		return conv.convertToString(value);
	}
}

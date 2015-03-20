/**
 * 
 */
package com.rmemoria.datastream.impl;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import com.rmemoria.datastream.DataConverter;
import com.rmemoria.datastream.DataInterceptor;
import com.rmemoria.datastream.DataMarshaller;
import com.rmemoria.datastream.DataUnmarshaller;
import com.rmemoria.datastream.StreamContext;
import com.rmemoria.datastream.StreamFileType;
import com.rmemoria.datastream.StreamFileTypeXML;
import com.rmemoria.datastream.jaxb.GraphSchema;
import com.rmemoria.datastream.jaxb.ObjectGraph;

/**
 * Implementation of the {@link StreamContext} interface
 * @author Ricardo Memoria
 *
 */
public class StreamContextImpl implements StreamContext {

	private GraphSchema graphSchema;
	private List<DataInterceptor> interceptors = new ArrayList<DataInterceptor>();
	private Map<Class, DataConverter> converters = new HashMap<Class, DataConverter>();
	private ClassMetaData classMetaData;
	private CollectionMetaData collectionMetaData;
	private static final DataConverter defaultConverter = new DefaultConverters();


	/** {@inheritDoc}
	 */
	@Override
	public void setSchema(URL schema) {
		// read XML schema
		try {
			// set the schema
			Unmarshaller unmarshaller = createSAXUnmarshaller();
			graphSchema = (GraphSchema)unmarshaller.unmarshal(schema);
			initializeGraphSchema();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/** {@inheritDoc}
	 */
	@Override
	public void setSchema(InputStream inputStream) {
			Unmarshaller unmarshaller = createSAXUnmarshaller();
        try {
            graphSchema = (GraphSchema)unmarshaller.unmarshal(inputStream);
        } catch (JAXBException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        initializeGraphSchema();
	}

	
	/** {@inheritDoc}
	 */
	private void initializeGraphSchema() {
		if ((graphSchema.getObjectGraph() != null) && (graphSchema.getObjectCollection() != null))
			throw new IllegalArgumentException("The schema contains more than 1 item bellow graphSchema tag");

		ClassPropertyScanner scan = new ClassPropertyScanner();
		if (graphSchema.getObjectGraph() != null) {
			classMetaData = scan.scan(this, graphSchema.getObjectGraph());
		}
		else collectionMetaData = scan.scan(this, graphSchema.getObjectCollection());
	}

	/**
	 * Create the SAX unmarshaller that will be used to unmarshall the JAXB schema of objects
	 * @return instance of {@link Unmarshaller} interface
	 */
	protected Unmarshaller createSAXUnmarshaller()  {
		try {
			SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			URL url = getClass().getClassLoader().getResource("datastream-1.0.xsd");
//			URL url = getClass().getClassLoader().getResource("com/rmemoria/datastream/datastream-1.0.xsd");
			Schema xmlSchema = sf.newSchema(url);

			JAXBContext context = JAXBContext.newInstance(GraphSchema.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			unmarshaller.setSchema(xmlSchema);
			return unmarshaller;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Find a converter to the specific class type
	 * @param type
	 * @return the converter or null if no converter was found
	 */
	@Override
	public DataConverter findConverter(Class type) {
		DataConverter converter = converters.get(type);
		if (converter != null)
			return converter;

		return defaultConverter;
	}
	
	/** {@inheritDoc}
	 */
	@Override
	public DataMarshaller createMarshaller(Class<? extends StreamFileType> type) {
		if (type != StreamFileTypeXML.class)
			throw new RuntimeException("Only the interface " + StreamFileTypeXML.class.getName() + " is supported by now");

		return new XmlDataMarshallerImpl(this);
	}

	/** {@inheritDoc}
	 */
	@Override
	public DataUnmarshaller createUnmarshaller(Class<? extends StreamFileType> type) {
		if (type != StreamFileTypeXML.class)
			throw new RuntimeException("Only the interface " + StreamFileTypeXML.class.getName() + " is supported by now");

		return new XmlDataUnmarshallerImpl(this);
	}

	/** {@inheritDoc}
	 */
	@Override
	public void addInterceptor(DataInterceptor interceptor) {
		interceptors.add(interceptor);
	}

	/** {@inheritDoc}
	 */
	@Override
	public void removeInterceptor(DataInterceptor interceptor) {
		interceptors.remove(interceptor);
	}

	/** {@inheritDoc}
	 */
	@Override
	public void setConverter(Class type, DataConverter converter) {
		converters.put(type, converter);
	}

	
	/**
	 * Return the class of an object using the interceptors, or, if no 
	 * interceptor is available, return the object class
	 * @param object
	 * @return
	 */
	protected Class getObjectClass(Object object) {
		for (DataInterceptor interceptor: interceptors) {
			Class clazz = interceptor.getObjectClass(object);
			if (clazz != null)
				return clazz;
		}
		return object.getClass();
	}
	
	/**
	 * Find instance of {@link ObjectGraph} of the given object
	 * @param object
	 * @return
	 */
	protected ClassMetaData findClassMetaData(Object object) {
		Class clazz = getObjectClass(object);
		return findClassMetaDataByClass(clazz);
	}
	
	
	/**
	 * Search for instance of the {@link ClassMetaData} by its object class
	 * @param clazz is the class related to the {@link ClassMetaData} instance
	 * @return instance of {@link ClassMetaData}
	 */
	protected ClassMetaData findClassMetaDataByClass(Class clazz) {
		if (classMetaData != null) {
			if (classMetaData.getGraphClass() == clazz)
				return classMetaData;
		}
		else {
			for (ClassMetaData cmd: collectionMetaData.getClassesMetaData()) {
				if (cmd.getGraphClass() == clazz)
					return cmd;
			}
		}
		return null;
	}
	
	/**
	 * Search for a {@link ClassMetaData} instance by its element name
	 * @param elname the element name in use in the {@link ObjectGraph}
	 * @return {@link ClassMetaData} instance, or null if no class is found
	 */
	protected ClassMetaData findClassByElement(String elname) {
		if (classMetaData != null) {
			if (classMetaData.getGraph().getName().equals(elname))
				 return classMetaData;
		}
		else {
			for (ClassMetaData clazz: collectionMetaData.getClassesMetaData()) {
				if (clazz.getGraph().getName().equals(elname))
					return clazz;
			}
		}

		return null;
	}

	
	/**
	 * Create new instance of a class by its parameters
	 * @param clazz
	 * @param params
	 * @return
	 */
	@Override
	public Object createInstance(Class clazz, Map<String, Object> params) {
		Object val = null;
		for (DataInterceptor dataInterceptor: interceptors) {
			val = dataInterceptor.newObject(clazz, params);
			if (val != null)
				break;
		}
		
		if (val == null) {
			try {
				val = clazz.newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return val;
	}
	
	/**
	 * @return the graphSchema
	 */
	public GraphSchema getGraphSchema() {
		return graphSchema;
	}


	/**
	 * @return the classMetaData
	 */
	public ClassMetaData getClassMetaData() {
		return classMetaData;
	}


	/**
	 * @return the collectionMetaData
	 */
	public CollectionMetaData getCollectionMetaData() {
		return collectionMetaData;
	}
	
}

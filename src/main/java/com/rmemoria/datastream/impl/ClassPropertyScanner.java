/**
 * 
 */
package com.rmemoria.datastream.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.rmemoria.datastream.DataStreamException;
import com.rmemoria.datastream.jaxb.ObjectCollection;
import com.rmemoria.datastream.jaxb.ObjectGraph;
import com.rmemoria.datastream.jaxb.Property;

/**
 * Scan the class to create in-memory list of properties to serialize/deserialize.
 * This class also create the instance of the {@link ClassMetaData} with all information
 * necessary to easily serialize/deserialize the objects
 *  
 * @author Ricardo Memoria
 *
 */
public class ClassPropertyScanner {

	private StreamContextImpl context;
//	private ClassMetaData classMetaData;
	
	/**
	 * Scan the collection defined in the schema and return its {@link CollectionMetaData} class
	 * @param collection instance of {@link ObjectCollection}
	 * @return {@link CollectionMetaData} with information about the collection
	 */
	public CollectionMetaData scan(StreamContextImpl context, ObjectCollection collection) {
		this.context = context;
		CollectionMetaData lst = new CollectionMetaData(collection);
		for (ObjectGraph graph: collection.getObjectGraph()) {
			ClassMetaData cmd = scan(context, graph);
			lst.getClassesMetaData().add(cmd);
		}
		return lst;
	}
	
	/**
	 * Scan the class and return its properties
	 * @param graph
	 * @param clazz
	 * @return
	 */
	public ClassMetaData scan(StreamContextImpl context, ObjectGraph graph) {
		this.context = context;
		// get the class
		Class clazz;
		try {
			clazz = Class.forName( graph.getClazz() );
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		ClassMetaData classMetaData = new ClassMetaData(context, graph, clazz);
		
		for (Property property: graph.getProperty()) {
			PropertyMetaData p = new PropertyMetaData(classMetaData);
			p.setProperty(property);
			classMetaData.addProperty(p);
		}
		
		scanClass(classMetaData, clazz);

		// update composed fields 
		for (PropertyMetaData prop: classMetaData.getProperties()) {
			if (prop.getField() == null)
				updateComposedFields(prop);
			
			if (prop.getField() == null)
				throw new IllegalArgumentException("Property not found: " + prop);
		}

		return classMetaData;
	}
	

	/**
	 * Scan class looking for properties to serialize
	 * @param clazz
	 */
	protected void scanClass(ClassMetaData classMetaData, Class clazz) {
		scanRecursive(classMetaData, clazz);
		
		// check information about the parent property in the object graph
		String parentprop = classMetaData.getGraph().getParentProperty();
		if (parentprop != null) {
			// search all declared fields for the property
			PropertyMetaData aux = null;
			for (PropertyMetaData prop: classMetaData.getProperties()) {
				if ((prop.getField() != null) && (prop.getField().getField().getName().equals(parentprop))) {
					aux = prop;
					break;
				}
			}
			if (aux == null) 
				throw new IllegalArgumentException("The parent property " + parentprop + 
						" was not found in class " + classMetaData.getGraphClass().getName());
			if (aux.isComposed())
				throw new IllegalArgumentException("The parent property " + parentprop + 
						" is already defined inside the graph for " + classMetaData.getGraphClass().getName());
			classMetaData.setLinkParentObject( aux );
		}
	}
	
	
	/**
	 * Scan for properties recursivelly by the super classes of the class
	 * @param classMetaData
	 * @param clazz
	 */
	private void scanRecursive(ClassMetaData classMetaData, Class clazz) {
		Field[] fields = clazz.getDeclaredFields();
		for (Field field: fields) {
			addField(classMetaData, clazz, field);
		}
		
		clazz = clazz.getSuperclass();
		if ((clazz != null) && (clazz != Object.class))
			scanClass(classMetaData, clazz);
	}

	/**
	 * Add a field of the class to the class meta data and collect more information
	 * about the property. If the field is to be ignored, nothing is done
	 * @param graph
	 * @param clazz
	 * @param field
	 */
	private void addField(ClassMetaData classMetaData, Class clazz, Field field) {
		List<PropertyMetaData> lst = findPropertyByFieldName(classMetaData, field.getName());
		
		if (lst.size() > 0) {
			for (PropertyMetaData prop: lst)
				initializeProperty(prop, field);
		}
		else {
			// all properties (not declared) must be included ?
			if (!classMetaData.isNotDeclaredPropsIgnored()) {
				// add the property
				PropertyMetaData prop = new PropertyMetaData(classMetaData);
				if (initializeProperty(prop, field))
					classMetaData.addProperty(prop);
			}
		}
	}
	
	
	/**
	 * Initialize the values of the properties according to its field and other
	 * inner information (like composed fields, field access, etc);
	 * @param prop
	 * @param field
	 * @return
	 */
	protected boolean initializeProperty(PropertyMetaData prop, Field field) {
		FieldAccess fa = createFieldAccess(field);
		if (fa == null) {
			if (prop.getProperty() != null)
				throw new DataStreamException("Property must have a get/set method to its value: " + prop);
			else return false;
		}

		// check if property is composed of nested properties separated by dots
		boolean isComposed = false;
		if (prop.getProperty() != null) {
			isComposed = prop.getProperty().getName().indexOf('.') > 0;
		}
		
		// set the field access to its value
		prop.setField(  createFieldAccessNotNull(prop, field) );

		// is the field type being "graphed" too?
		if ((prop.getProperty() != null) && (prop.getProperty().getObjectGraph() != null)) {
			if (prop.getProperty().isXmlAttribute())
				throw new IllegalArgumentException("Property that contains a graph definition cannot be used as an XML attribute: " + prop);

			// composed property cannot have a class graph definition
			if (isComposed)
				throw new IllegalArgumentException("Composed property cannot point to a graph definition: " + prop);
			ClassMetaData cmd = scan(context, prop.getProperty().getObjectGraph());
			prop.setTypeMetaData(cmd);
			cmd.setParentProperty(prop);
		}

		if (isComposed)
			updateComposedFields(prop);

		return true;
	}
	
	/**
	 * Search for a property by its field name. It returns a property even if it's a composed field
	 * @param classMetaData
	 * @param name
	 * @return
	 */
	private List<PropertyMetaData> findPropertyByFieldName(ClassMetaData classMetaData, String name) {
		List<PropertyMetaData> lst = new ArrayList<PropertyMetaData>();
		for (PropertyMetaData prop: classMetaData.getProperties()) {
			String s = prop.getProperty() != null ? prop.getProperty().getName(): null;
			if (s != null) {
				int index = s.indexOf('.');
				if (index > 0)
					s = s.substring(0, index);
				
				if (name.equals(s))
					lst.add(prop);
			}
		}
		return lst;
	}
	
	/**
	 * Get the read and write methods of the field
	 * @param field
	 * @return
	 */
	protected FieldAccess createFieldAccess(Field field) {
		String name = field.getName();
		// check if field is readable and writable
		name = Character.toUpperCase( name.charAt(0) ) + name.substring(1);
		
		// set the method names
		String getMethod;
		if (field.getType() == boolean.class)
			 getMethod = "is" + name;
		else getMethod = "get" + name;
		String setMethod = "set" + name;
		
		Class clazz = field.getDeclaringClass();

		Class[] param1 = {};
		Class[] param2 = {field.getType()};

		Method get = getDeclaredMethod(clazz, getMethod, param1);
		Method set = getDeclaredMethod(clazz, setMethod, param2);
		if ((get == null) || (set == null))
			return null;

		return new FieldAccess(field, get, set);
	}
	
	/**
	 * Update information of a composite field path
	 * @param field
	 */
	protected void updateComposedFields(PropertyMetaData prop) {
		String path = prop.getProperty().getName();
		try {
			String[] props = path.split("\\.");
			List<FieldAccess> lst = new ArrayList<FieldAccess>();
			Class clazz = prop.getClassMetaData().getGraphClass();
			for (int i = 0; i < props.length; i++) {
				String propName = props[i];
				Field field;
				field = getDeclaredField(clazz, propName);
				if (field == null)
					throw new DataStreamException("No such field " + propName + " in class " + clazz.getName());
				// discard the first value, because it's the 'field' property in PropertyMetaData
				if (i > 0)
					lst.add(createFieldAccessNotNull(prop, field));
				clazz = field.getType();
			}

			// set composed fields in property
			FieldAccess[] fields = lst.toArray(new FieldAccess[lst.size()]);
			prop.setSubfields( fields );
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Create the methods to access a property in a class, and raise an exception
	 * if the methods to read and/or write are not available
	 * @param prop
	 * @param field
	 * @return
	 */
	protected FieldAccess createFieldAccessNotNull(PropertyMetaData prop, Field field) {
		FieldAccess fa = createFieldAccess(field);
		if (fa == null)
			throw new DataStreamException("Missing methods get/set for property " + field.getName() + " of " + prop);
		return fa;
	}

	
	/**
	 * Return the declared field in the class or in a super class 
	 * @param clazz
	 * @param field
	 * @return
	 */
	protected Field getDeclaredField(Class clazz, String fieldname) {
		while (clazz != Object.class) {
			// search for field in the class clazz
			Field[] fields = clazz.getDeclaredFields();
			for (Field fld: fields)
				if (fld.getName().equals(fieldname)) {
					return fld;
				}
			// if not found, search in the super class
			clazz = clazz.getSuperclass();
		}
		
		return null;
	}
	
	/**
	 * Search for the declared method in the class or in its super classes
	 * @param clazz is the class to search for the method
	 * @param metname is the method name
	 * @param params are the parameters type accepted by the method
	 * @return the instance of the {@link Method} found, or null if no method was found
	 */
	protected Method getDeclaredMethod(Class clazz, String metname, Class[] params) {
		while (clazz != Object.class) {
			try {
				return clazz.getDeclaredMethod(metname, params);
			} catch (NoSuchMethodException e) {
				clazz = clazz.getSuperclass();
			}
		}
		return null;
	}
}

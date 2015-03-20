/**
 * 
 */
package com.rmemoria.datastream.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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

//	private StreamContextImpl context;
//	private ClassMetaData classMetaData;
	
	/**
	 * Scan the collection defined in the schema and return its {@link CollectionMetaData} class
	 * @param collection instance of {@link ObjectCollection}
	 * @return {@link CollectionMetaData} with information about the collection
	 */
	public CollectionMetaData scan(StreamContextImpl context, ObjectCollection collection) {
//		this.context = context;
		CollectionMetaData lst = new CollectionMetaData(collection);
		for (ObjectGraph graph: collection.getObjectGraph()) {
			ClassMetaData cmd = scan(context, graph);
			lst.getClassesMetaData().add(cmd);
		}
		return lst;
	}
	
	/**
	 * Scan the class and return its properties
	 * @param context
	 * @param graph
	 * @return
	 */
	public ClassMetaData scan(StreamContextImpl context, ObjectGraph graph) {
//		this.context = context;
		// get the class
		Class clazz;
		try {
			clazz = Class.forName( graph.getClazz() );
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		ClassMetaData classMetaData = new ClassMetaData(context, graph, clazz);
		
		// add properties declared in the XML
		for (Property property: graph.getProperty()) {
			createPropertyFromXML(classMetaData, property);
		}
		
		// scan the class for other fields
		scanClass(classMetaData, clazz);
		
		checkParentProperty(classMetaData);

		// update composed fields 
/*		for (PropertyMetaData prop: classMetaData.getProperties()) {
			if (prop.getField() == null)
				updateComposedFields(prop);
			
			if (prop.getField() == null)
				throw new IllegalArgumentException("Property not found: " + prop);
		}
*/

		// is the field type being "graphed" too?

		return classMetaData;
	}

	
	protected void checkParentProperty(ClassMetaData classMetaData) {
		String parentprop = classMetaData.getGraph().getParentProperty();
		if (parentprop == null) {
			return;
		}
		// search all declared fields for the property
		PropertyMetaData aux = classMetaData.findPropertyByName(parentprop);
		if (aux == null) {
			aux = new PropertyMetaData(classMetaData);
			aux.setFieldAccess( createFieldAccess(classMetaData.getGraphClass(), parentprop));
			classMetaData.addProperty(aux);
		}

/*		if (aux == null) 
			throw new IllegalArgumentException("The parent property " + parentprop + 
					" was not found in class " + classMetaData.getGraphClass().getName());
		if (aux.isComposed())
			throw new IllegalArgumentException("The parent property " + parentprop + 
					" is already defined inside the graph for " + classMetaData.getGraphClass().getName());
*/		
		classMetaData.setLinkParentObject( aux );
	}
	
	/**
	 * Create a property meta data from a property declared in the XML file
	 * @param cmd
	 * @param property
	 * @return
	 */
	protected PropertyMetaData createPropertyFromXML(ClassMetaData cmd, Property property) {
		String[] props = property.getName().split("\\.");
		PropertyMetaData prop = cmd.findPropertyByName(props[0]);
		if (prop == null) {
			prop = new PropertyMetaData(cmd);
            FieldAccess fa = createFieldAccess(cmd.getGraphClass(), props[0]);
            if (fa == null) {
                throw new DataStreamException("Property " + property.getName() + " not found");
            }
			prop.setFieldAccess( fa );
			cmd.addProperty(prop);
		}

		// if the property is a composed property, follow all nested properties
		if (props.length > 1) {
			PropertyMetaData parent = prop;
			for (int i = 1; i < props.length; i++) {
				PropertyMetaData child = parent.findPropertyByName(props[i]);
				if (child == null) {
					child = new PropertyMetaData(cmd);
					child.setFieldAccess( createFieldAccess(parent.getPropertyType(), props[i]));
					parent.addProperty(child);
				}
				parent = child;
			}
			parent.setProperty(property);
		}
		else {
			prop.setProperty(property);
		}

		// is the field type being "graphed" too?
		if ((prop.getProperty() != null) && (prop.getProperty().getObjectGraph() != null)) {
			if (prop.getProperty().isXmlAttribute())
				throw new DataStreamException("Property that contains a graph definition cannot be used as an XML attribute: " + prop);

			// composed property cannot have a class graph definition
			if (prop.isComposed())
				throw new IllegalArgumentException("Composed property cannot point to a graph definition: " + prop);
			ClassMetaData cmdProp = scan(cmd.getContext(), prop.getProperty().getObjectGraph());
			prop.setTypeMetaData(cmdProp);
			cmdProp.setParentProperty(prop);
		}

		return prop;
	}

	

	/**
	 * Scan class looking for properties to serialize
	 * @param clazz
	 */
	protected void scanClass(ClassMetaData classMetaData, Class clazz) {
		scanRecursive(classMetaData, clazz);
		
	}
	
	
	/**
	 * Scan for properties recursively by the super classes of the class
	 * @param classMetaData
	 * @param clazz
	 */
	private void scanRecursive(ClassMetaData classMetaData, Class clazz) {
		Field[] fields = clazz.getDeclaredFields();
		for (Field field: fields) {
			PropertyMetaData p = classMetaData.findPropertyByName(field.getName());
			if (p == null) {
				FieldAccess fa = createFieldAccess(clazz, field.getName());
				if (fa != null) {
					p = new PropertyMetaData(classMetaData);
					classMetaData.addProperty(p);
					p.setFieldAccess( fa );
				}
			}
		}
		
		clazz = clazz.getSuperclass();
		if ((clazz != null) && (clazz != Object.class))
			scanClass(classMetaData, clazz);
	}


	/**
	 * Get the read and write methods of the field. The field must be declared in the given class,
     * but read and write are searched across parent classes, if not found immediatelly in the class
     *
	 * @param clazz the class to get the methods for reading and writing a property
     * @param fieldname the name of the field inside the class
	 * @return
	 */
	protected FieldAccess createFieldAccess(Class clazz, String fieldname) {
		// check if field is readable and writable
		String name = Character.toUpperCase( fieldname.charAt(0) ) + fieldname.substring(1);

		// get the "get" method
		Class[] param1 = {};
		Method get = getDeclaredMethod(clazz, "get" + name, param1);
		if (get == null) {
			get = getDeclaredMethod(clazz, "is" + name, param1);
		}
		
		if (get == null) {
			return null;
		}
		
		Class type = get.getReturnType();

		Class[] param2 = {type};

		Method set = getDeclaredMethod(clazz, "set" + name, param2);

		return new FieldAccess(fieldname, get, set);
	}
	
	/**
	 * Update information of a composite field path
	 * @param field
	 */
/*	protected void updateComposedFields(PropertyMetaData prop) {
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
*/	
	/**
	 * Create the methods to access a property in a class, and raise an exception
	 * if the methods to read and/or write are not available
	 * @param prop
	 * @param field
	 * @return
	 */
/*	protected FieldAccess createFieldAccessNotNull(PropertyMetaData prop, Field field) {
		FieldAccess fa = createFieldAccess(field);
		if (fa == null)
			throw new DataStreamException("Missing methods get/set for property " + field.getName() + " of " + prop);
		return fa;
	}
*/
	
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
				if (clazz != null) {
					return getDeclaredMethod(clazz, metname, params);
				}
			}
		}
		return null;
	}
}

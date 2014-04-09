/**
 * 
 */
package com.rmemoria.datastream.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.rmemoria.datastream.StreamContext;
import com.rmemoria.datastream.jaxb.Property;
import com.rmemoria.datastream.jaxb.PropertyUse;

/**
 * Store in memory meta data information about a property to be serialized or deserialized
 * 
 * @author Ricardo Memoria
 *
 */
public class PropertyMetaData {

	private ClassMetaData classMetaData;
	private Property property;
	private FieldAccess field;
	private FieldAccess[] subfields;
	private ClassMetaData typeMetaData;
	private ClassMetaData compactibleTypeMetaData;
	private boolean compactibleTypeChecked = false;

	public PropertyMetaData(ClassMetaData classMetaData) {
		this.classMetaData = classMetaData;
	}

	
	/**
	 * Return the element name of the tag
	 * @return String value
	 */
	public String getElementName() {
		if (property != null) {
			String elem = property.getElementName();
			if (elem == null)
				return property.getName();
			else return elem;
		}
		else return field.getField().getName();
	}

	
	/**
	 * Return the path of the property in the class, containing the properties
	 * separated by dot to access the value
	 * @return String value
	 */
	public String getPath() {
		if (property != null)
			 return property.getName();
		else return field.getField().getName();
	}


	/**
	 * Set the value of the property for the given object in the current context
	 * @param context implementation of {@link StreamContext} 
	 * @param obj the object to have its property set
	 * @param value the value to set
	 */
	public void setValue(StreamContext context, Object obj, Object value) {
		PropertyMetaData linkprop = getTypeMetaData() != null? getTypeMetaData().getLinkParentObject() : null;

		// is a collection ?
		if (isCollection()) {
			Collection lst = getCollectionObject(context, obj);
			for (Object item: (Collection)value) {
				lst.add(item);
				// is the property link between objects defined? 
				if (linkprop != null) {
					// link objects in the list with its parent by its link property 
						linkprop.setValue(context, item, obj);
				}
			}
			return;
		}

		if (subfields != null) {
			// get the first reference of a composite property
			String propname = getPropertyName(0);
			Object target = getFieldAccessObject(context, field, obj, propname, value);

			// navigate through the fields of the composite property
			for (int i = 0; i < subfields.length - 1; i++) {
				propname = getPropertyName(i + 1);
				target = getFieldAccessObject(context, subfields[i], target, propname, value);
			}

			// set the value of the last field in the composite property
			subfields[subfields.length - 1].setValue(target, value);
		}
		else {
			field.setValue(obj, value);
		}

		// is the link property between objects available ?
		if (linkprop != null) {
			// set the link between the object and the value, if available
			linkprop.setValue(context, value, obj);
		}
	}

	
	private String getPropertyName(int index) {
		String s = "";
		for (int i = index; i < subfields.length; i++) {
			if (!s.isEmpty())
				s += ".";
			s += subfields[i].getField().getName();
		}
		return s;
	}
	
	/**
	 * Return the value for the given object and its {@link FieldAccess}. If the field value is
	 * null, then a new instance is created with the given context
	 * @param context
	 * @param fa
	 * @param obj
	 * @return
	 */
	private Object getFieldAccessObject(StreamContext context, FieldAccess fa, Object obj, String propname, Object propvalue) {
		Object currentValue = fa.getValue(obj);
//		if (value != null)
//			return value;

		// TODO: Implement list auto creation
		if ((currentValue != null) && (currentValue instanceof Collection)) {
			return currentValue;
		}
		Class type = fa.getField().getType();
		Map<String, Object> params = null;
		if (propvalue != null) {
			params = new HashMap<String, Object>();
			params.put(propname, propvalue);
		}
		Object value = context.createInstance(type, params);
		if (value != currentValue) {
			fa.setValue(obj, value);
		}
		return value;
	}

	/**
	 * Return the object graph to be used if no graph object is directly linked
	 * @return
	 */
	public ClassMetaData getCompactibleTypeMetaData() {
		if (typeMetaData != null)
			return typeMetaData;

		if ((compactibleTypeMetaData == null) && (!compactibleTypeChecked)) {
			compactibleTypeMetaData = getClassMetaData().getContext().findClassMetaDataByClass(getConvertionType());
		}
		return compactibleTypeMetaData;
	}


	/**
	 * Return the type to be converted from and to string
	 * @return instance of the Class type 
	 */
	public Class getConvertionType() {
		if (subfields == null)
			 return field.getField().getType();
		else return subfields[subfields.length - 1].getField().getType();
	}
	
	/**
	 * Indicate if the serialization is to be ignored
	 * @return true if serialization should not be done for this field
	 */
	public boolean isSerializationIgnored() {
		if (isIgnored())
			return true;

		String s = property != null? property.getName(): field.getField().getName();
		return s.equals(classMetaData.getGraph().getParentProperty());
	}
	
	/**
	 * Wrapper method to the {@link Property#isXmlAttribute()}
	 * @return true if the property should be serialized as an XML attribute (just to XML serializers)
	 */
	public boolean isXmlAttribute() {
		return (property != null) && (property.isXmlAttribute());
	}
	
	/**
	 * Return true if the property name is a path with several properties nested
	 * and separated by dots 
	 * @return true if it's a composed field, or false if it's not 
	 */
	public boolean isComposed() {
		return subfields != null;
	}
	
	/**
	 * Return true if the property represents an instance of the {@link Collection} interface
	 * @return boolean value
	 */
	public boolean isCollection() {
		return Collection.class.isAssignableFrom( getConvertionType() );
	}
	
	
	/**
	 * Return the collection of a property that represents a collection of object for
	 * the given object
	 * @param context
	 * @param obj
	 * @return
	 */
	public Collection getCollectionObject(StreamContext context, Object obj) {
		Object value = getFieldAccessObject(context, field, obj, null, null);
		if (subfields != null) {
			for (FieldAccess fa: subfields) {
				value = getFieldAccessObject(context, fa, value, null, null);
			}
		}
		return (Collection)value;
	}
	
	/**
	 * Check if the property should be ignored
	 * @return true if should be ignored
	 */
	public boolean isIgnored() {
		return (property != null) && (property.getUse() == PropertyUse.IGNORE);
	}
	
	/**
	 * Return the value of the property for the given object
	 * @param obj is the object to get the property value from
	 * @return property value
	 */
	public Object getValue(Object obj) {
		Object val = field.getValue(obj);
		if (val == null)
			return null;

		if (subfields != null) {
			for (FieldAccess field: subfields) {
				val = field.getValue(val);
				if (val == null)
					return null;
			}
		}
		return val;
	}
	
	
	@Override
	public String toString() {
		return classMetaData.getGraph().getClazz() + "." + (property != null? property.getName(): field.getField().getName());
	}
	
	/**
	 * @return the property
	 */
	public Property getProperty() {
		return property;
	}
	/**
	 * @param property the property to set
	 */
	public void setProperty(Property property) {
		this.property = property;
	}
	/**
	 * @return the classMetaData
	 */
	public ClassMetaData getClassMetaData() {
		return classMetaData;
	}

	/**
	 * @return the simpleField
	 */
	public FieldAccess getField() {
		return field;
	}

	/**
	 * @param simpleField the simpleField to set
	 */
	public void setField(FieldAccess field) {
		this.field = field;
	}

	/**
	 * @return the subfields
	 */
	public FieldAccess[] getSubfields() {
		return subfields;
	}

	/**
	 * @param composedFields the composedFields to set
	 */
	public void setSubfields(FieldAccess[] subfields) {
		this.subfields = subfields;
	}

	/**
	 * @return the typeMetaData
	 */
	public ClassMetaData getTypeMetaData() {
		return typeMetaData;
	}

	/**
	 * @param typeMetaData the typeMetaData to set
	 */
	public void setTypeMetaData(ClassMetaData typeMetaData) {
		this.typeMetaData = typeMetaData;
	}

}

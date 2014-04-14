/**
 * 
 */
package com.rmemoria.datastream.impl;

import java.util.ArrayList;
import java.util.List;

import com.rmemoria.datastream.jaxb.ObjectGraph;

/**
 * @author Ricardo Memoria
 *
 */
public class ClassMetaData {

	private ObjectGraph graph;
	private List<PropertyMetaData> properties = new ArrayList<PropertyMetaData>();
	private List<PropertyMetaData> endpointProperties;
	private Class graphClass;
	private PropertyMetaData parentProperty;
	private PropertyMetaData linkParentObject;
	private StreamContextImpl context;

	public ClassMetaData(StreamContextImpl context, ObjectGraph graph, Class graphClass) {
		super();
		this.context = context;
		this.graph = graph;
		this.graphClass = graphClass;
	}

	/**
	 * @return the properties
	 */
	public List<PropertyMetaData> getProperties() {
		return properties;
	}

	
	/**
	 * Return list with all end point properties, i.e, properties with
	 * no child element and where value must be serialized/deserialized
	 * @return list with {@link PropertyMetaData} objects
	 */
	public List<PropertyMetaData> getEndPointProperties() {
		if (endpointProperties == null) {
			endpointProperties = new ArrayList<PropertyMetaData>();
			for (PropertyMetaData pmd: properties) {
				pmd.findEndpointProperties(endpointProperties);
			}
		}
		return endpointProperties;
	}
	
	/**
	 * Search for a property by its element name
	 * @param name is the element name
	 * @return instance of {@link PropertyMetaData}, or null if no property is found
	 */
	public PropertyMetaData findPropertyByElementName(String name) {
		for (PropertyMetaData prop: getEndPointProperties()) {
			if (prop.getElementName().equals(name))
				return prop;
		}
		return null;
	}
	
	
	/**
	 * Search for a property by its name
	 * @param name is the name of the field that represents this property
	 * @return instance of {@link PropertyMetaData}, or null if no property is found
	 */
	public PropertyMetaData findPropertyByName(String name) {
		// is a nested property ?
		if (name.indexOf('.') > 0) { 
			String[] props = name.split("//.");
			PropertyMetaData pmd = findPropertyByName(props[0]);
			if (pmd == null) {
				return null;
			}

			for (int i = 1; i < props.length; i++) {
				pmd = pmd.findPropertyByName(props[i]);
				if (pmd == null) {
					return null;
				}
			}
			return pmd;
		}
		else {
			// it's a single property
			for (PropertyMetaData prop: properties) {
				if (prop.getPropertyName().equals(name))
					return prop;
			}
			return null;
		}
	}
	
	/**
	 * If true, all properties that are not declared in schema will be ignored. Default is false
	 * @return boolean value
	 */
	public boolean isNotDeclaredPropsIgnored() {
		return Boolean.TRUE.equals(graph.getIgnorePropsNotDeclared());
	}
	
	/**
	 * @return the graph
	 */
	public ObjectGraph getGraph() {
		return graph;
	}

		
	/**
	 * @param prop
	 */
	protected void addProperty(PropertyMetaData prop) {
		properties.add(prop);
	}

	/**
	 * @return the graphClass
	 */
	public Class getGraphClass() {
		return graphClass;
	}

	/**
	 * @return the parentProperty
	 */
	public PropertyMetaData getParentProperty() {
		return parentProperty;
	}

	/**
	 * @param parentProperty the parentProperty to set
	 */
	public void setParentProperty(PropertyMetaData parentProperty) {
		this.parentProperty = parentProperty;
	}

	/**
	 * @return the linkParentObject
	 */
	public PropertyMetaData getLinkParentObject() {
		return linkParentObject;
	}

	/**
	 * @param linkParentObject the linkParentObject to set
	 */
	public void setLinkParentObject(PropertyMetaData linkParentObject) {
		this.linkParentObject = linkParentObject;
	}

	/** {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "ClassMetaData [" + graphClass + "]";
	}

	/**
	 * @return the context
	 */
	public StreamContextImpl getContext() {
		return context;
	}
}

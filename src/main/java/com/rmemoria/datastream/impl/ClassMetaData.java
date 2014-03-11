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
	 * Search for a property by its element name
	 * @param name is the element name
	 * @return instance of {@link PropertyMetaData}, or null if no property is found
	 */
	public PropertyMetaData findPropertyByElementName(String name) {
		for (PropertyMetaData prop: properties) {
			if (prop.getElementName().equals(name))
				return prop;
		}
		return null;
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

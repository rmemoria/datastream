/**
 * 
 */
package com.rmemoria.datastream.impl;

/**
 * Used to indicate the current selection when reading an XML stream using SAX
 * @author Ricardo Memoria
 *
 */
public class NodeSelection {

	private ClassMetaData classMetaData;
	private PropertyMetaData propertyMetaData;
	private NodeSelection parent;

	public NodeSelection(NodeSelection parent, PropertyMetaData propertyMetaData) {
		super();
		this.parent = parent;
		this.propertyMetaData = propertyMetaData;
	}
	
	public NodeSelection(NodeSelection parent, ClassMetaData classMetaData) {
		super();
		this.parent = parent;
		this.classMetaData =  classMetaData;
	}
	
	
	/**
	 * Return true if the current node selection a property
	 * @return boolean value
	 */
	public boolean isPropertySelection() {
		return propertyMetaData != null;
	}
	
	/**
	 * Return true if the current node selection is a class
	 * @return
	 */
	public boolean isClassSelection() {
		return classMetaData != null;
	}
	
	/**
	 * @return the classMetaData
	 */
	public ClassMetaData getClassMetaData() {
		return classMetaData;
	}
	/**
	 * @param classMetaData the classMetaData to set
	 */
	public void setClassMetaData(ClassMetaData classMetaData) {
		this.classMetaData = classMetaData;
	}
	/**
	 * @return the propertyMetaData
	 */
	public PropertyMetaData getPropertyMetaData() {
		return propertyMetaData;
	}
	/**
	 * @param propertyMetaData the propertyMetaData to set
	 */
	public void setPropertyMetaData(PropertyMetaData propertyMetaData) {
		this.propertyMetaData = propertyMetaData;
	}
	/**
	 * @return the parent
	 */
	public NodeSelection getParent() {
		return parent;
	}
}

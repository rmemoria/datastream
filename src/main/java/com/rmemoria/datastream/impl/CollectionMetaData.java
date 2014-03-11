/**
 * 
 */
package com.rmemoria.datastream.impl;

import java.util.ArrayList;
import java.util.List;

import com.rmemoria.datastream.jaxb.ObjectCollection;

/**
 * @author Ricardo Memoria
 *
 */
public class CollectionMetaData {

	private ObjectCollection objectCollection;
	private Class objectCollectionClass;
	private List<ClassMetaData> classesMetaData = new ArrayList<ClassMetaData>();
	
	public CollectionMetaData(ObjectCollection objectCollection) {
		this.objectCollection = objectCollection;
	}

	/**
	 * @return the objectCollection
	 */
	public ObjectCollection getObjectCollection() {
		return objectCollection;
	}
	/**
	 * @param objectCollection the objectCollection to set
	 */
	public void setObjectCollection(ObjectCollection objectCollection) {
		this.objectCollection = objectCollection;
	}
	/**
	 * @return the objectCollectionClass
	 */
	public Class getObjectCollectionClass() {
		if (objectCollectionClass == null) {
			try {
				if (objectCollection.getClazz() != null)
					 objectCollectionClass = Class.forName(objectCollection.getClazz());
				else objectCollectionClass = ArrayList.class;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return objectCollectionClass;
	}

	/**
	 * @return the classesMetaData
	 */
	public List<ClassMetaData> getClassesMetaData() {
		return classesMetaData;
	}
}

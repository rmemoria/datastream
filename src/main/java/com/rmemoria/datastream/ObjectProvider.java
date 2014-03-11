/**
 * 
 */
package com.rmemoria.datastream;

/**
 * Interface that must be implemented in order to provide objects to be serialized
 * by the marshal procedure
 * @author Ricardo Memoria
 *
 */
public interface ObjectProvider {

	/**
	 * Callback method called when the marshal procedure get an object to be serialized.
	 * The return value will be an object to be serialized. The object must be of the same
	 * class as the root object graph defined in the XML schema. In order to provider information.
	 * 
	 * @return instance of the Object to be serialized
	 */
	Object getObjectToSerialize(int index);
}

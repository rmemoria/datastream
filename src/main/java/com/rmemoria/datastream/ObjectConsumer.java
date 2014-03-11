/**
 * 
 */
package com.rmemoria.datastream;

/**
 * Interface that must be implemented when the client wants to be notified about
 * each object that is created during deserialization of the data. The object notified
 * is always the one defined in the root of the tree (or under the collection defined
 * in the root) of the graph schema file.</p>
 * This model allows the client to receive and handle huge amount of data from the
 * data stream, because each object is discarded by the unmarshaller when 
 * 
 * @author Ricardo Memoria
 *
 */
public interface ObjectConsumer {

	/**
	 * Called when a new object is serialized from the source data. Just objects defined
	 * in the root object graph definition from the schema file is sent to this method.
	 * Once this method is called, the object is discarded by the unmarshaller process.
	 * @param object the object unserialized
	 */
	void onNewObject(Object object);
	
	/**
	 * Called when starting reading an object. A good place to start a database
	 * transaction and commit it when the method {@link #onNewObject(Object)} is
	 * called.
	 * 
	 * @param objectClass
	 */
	void startObjectReading(Class objectClass);
}

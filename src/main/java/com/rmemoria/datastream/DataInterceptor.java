/**
 * 
 */
package com.rmemoria.datastream;

import java.util.Map;

/**
 * Interface that must be implemented and registered in the  
 * @author Ricardo Memoria
 *
 */
public interface DataInterceptor {

	/**
	 * Called while unmarshalling and it's necessary to create an instance of an
	 * object. 
	 * @param objectType the class type to be created
	 * @param params the parameters attached to the 
	 * @return
	 */
	Object newObject(Class objectType, Map<String, Object> params);

	/**
	 * Return the class that must be used to apply 
	 * @param obj
	 * @return
	 */
	Class getObjectClass(Object obj);
}

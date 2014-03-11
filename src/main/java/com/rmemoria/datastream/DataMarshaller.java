/**
 * 
 */
package com.rmemoria.datastream;

import java.io.OutputStream;


/**
 * @author Ricardo Memoria
 *
 */
public interface DataMarshaller {

	void marshall(Object obj, OutputStream outputStream);

	void marshall(OutputStream output, ObjectProvider provider);
}

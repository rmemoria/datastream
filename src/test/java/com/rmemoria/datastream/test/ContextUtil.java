/**
 * 
 */
package com.rmemoria.datastream.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.rmemoria.datastream.StreamContext;
import com.rmemoria.datastream.StreamContextFactory;

/**
 * @author Ricardo Memoria
 *
 */
public class ContextUtil {

	public static StreamContext createContext(String fname) {
		File file = new File(fname);
		if (!file.exists()) {
			throw new RuntimeException("File doesn't exist: " + fname);
		}
		try{
			InputStream in = new FileInputStream(file);
			return StreamContextFactory.createContext(in);
		}
		catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}

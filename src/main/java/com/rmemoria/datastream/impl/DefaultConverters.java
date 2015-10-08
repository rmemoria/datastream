/**
 * 
 */
package com.rmemoria.datastream.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.rmemoria.datastream.DataConverter;

/**
 * @author Ricardo Memoria
 *
 */
public class DefaultConverters implements DataConverter {

	private static final SimpleDateFormat dtformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

	/** {@inheritDoc}
	 */
	@Override
	public String convertToString(Object obj) {
		if (obj == null)
			return "";

		Class classType = obj.getClass();

		// check if it's a string
		if (classType == String.class)
			return (String)obj;
		
		// check if it's an integer type
		if ((classType == int.class) || (classType == Integer.class))
			return Integer.toString((Integer)obj);

		// check if it's a long type
		if ((classType == long.class) || (classType == Long.class))
			return Long.toString((Long)obj);

		// check if it's a character type
		if ((classType == char.class) || (classType == Character.class))
			return ((Character)obj).toString();

		// check if it's a boolean type
		if ((classType == boolean.class) || (classType == Boolean.class))
			return (Boolean)obj ? "1": "0";
		
		if ((classType == Float.class) || (classType == float.class))
			return Float.toString((Float)obj);
		
		if ((classType == Double.class) || (classType == double.class))
			return Double.toString((Double)obj);

		// check if it's a date-time type
		if (Date.class.isAssignableFrom(classType))
			return dtformat.format((Date)obj);

		// is an enumeration ?
		if (Enum.class.isAssignableFrom(classType))
			return obj.toString();
		
		throw new IllegalArgumentException("Class " + classType.toString() + " not supported for serialization");
	}


	/** {@inheritDoc}
	 */
	@Override
	public Object convertFromString(String s, Class classType) {
		if (s == null || s.isEmpty()) {
            return null;
        }

		// check if it's a string
		if (classType == String.class)
			return s;
		
		// check if it's an integer type
		if ((classType == int.class) || (classType == Integer.class))
			return Integer.parseInt(s);

		// check if it's a long type
		if ((classType == long.class) || (classType == Long.class))
			return Long.parseLong(s);

		// check if it's a character type
		if ((classType == char.class) || (classType == Character.class)) {
			if (s.length() > 0)
				return s.charAt(0);
			raiseConvertionError(s, classType);
		}

		// check if it's a boolean type
		if ((classType == boolean.class) || (classType == Boolean.class)) {
			if ((s.equals("1")) || (s.equalsIgnoreCase("true")))
				return true;
			if ((s.equals("0")) || (s.equalsIgnoreCase("false")))
				return false;
			raiseConvertionError(s, classType);
		}

		// check if it's a date-time type
		if (Date.class.isAssignableFrom(classType))
		try {
			return dtformat.parseObject(s);
		} catch (ParseException e) {
			raiseConvertionError(s, Date.class);
		}
		
		// check if it's a float
		if ((classType == float.class) || (classType == Float.class)) {
			return Float.parseFloat(s);
		}
		
		// check if it's a double value
		if ((classType == double.class) || (classType == Double.class)) {
			return Double.parseDouble(s);
		}

		// it's an enumeration ?
		if (Enum.class.isAssignableFrom(classType))
			return stringToEnum(s, classType);

		raiseConvertionError(s, classType);
		return null;
	}
	
	/**
	 * Convert a string to an enumeration type
	 * @param s
	 * @return
	 */
	protected Object stringToEnum(String s, Class classType) {
		Enum[] vals = ((Class<Enum>)classType).getEnumConstants();
		for (Enum val: vals)
			if (val.toString().equals(s))
				return val;
		
		throw new IllegalArgumentException("Value " + s + " is not valid for " + classType.toString());
	}


	/**
	 * Throw default conversion error exception
	 * @param s
	 * @param classType
	 */
	protected void raiseConvertionError(String s, Class classType) {
		throw new IllegalArgumentException("Value " + s + " cannot be converted to " + classType.toString());
	}
}

//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.08.14 at 06:52:53 PM BRT 
//


package com.rmemoria.datastream.jaxb;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for customType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="customType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="STRING"/>
 *     &lt;enumeration value="INT"/>
 *     &lt;enumeration value="LONG"/>
 *     &lt;enumeration value="DOUBLE"/>
 *     &lt;enumeration value="FLOAT"/>
 *     &lt;enumeration value="BOOLEAN"/>
 *     &lt;enumeration value="DATE"/>
 *     &lt;enumeration value="CHAR"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "customType")
@XmlEnum
public enum CustomType {

	STRING,
	INT,
	LONG,
	DOUBLE,
	FLOAT,
	BOOLEAN,
	DATE,
	CHAR;

    public String value() {
        return name();
    }

    public static CustomType fromValue(String v) {
        return valueOf(v);
    }

}

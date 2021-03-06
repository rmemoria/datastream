//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.08.09 at 01:02:19 AM BRT 
//


package com.rmemoria.datastream.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for property complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="property">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="objectGraph" type="{}objectGraph" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="use" type="{}propertyUse" default="REQUIRED" />
 *       &lt;attribute name="elementName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="xmlAttribute" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "property", propOrder = {
    "objectGraph"
})
public class Property {

    protected ObjectGraph objectGraph;
    @XmlAttribute(required = true)
    protected String name;
    @XmlAttribute
    protected PropertyUse use;
    @XmlAttribute
    protected String elementName;
    @XmlAttribute
    protected Boolean xmlAttribute = false;

    @XmlAttribute
    protected Boolean includeNullValues;

    /**
     * Gets the value of the objectGraph property.
     * 
     * @return
     *     possible object is
     *     {@link ObjectGraph }
     *     
     */
    public ObjectGraph getObjectGraph() {
        return objectGraph;
    }

    /**
     * Sets the value of the objectGraph property.
     * 
     * @param value
     *     allowed object is
     *     {@link ObjectGraph }
     *     
     */
    public void setObjectGraph(ObjectGraph value) {
        this.objectGraph = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the use property.
     * 
     * @return
     *     possible object is
     *     {@link PropertyUse }
     *     
     */
    public PropertyUse getUse() {
        if (use == null) {
            return PropertyUse.NOT_REQUIRED;
        } else {
            return use;
        }
    }

    /**
     * Sets the value of the use property.
     * 
     * @param value
     *     allowed object is
     *     {@link PropertyUse }
     *     
     */
    public void setUse(PropertyUse value) {
        this.use = value;
    }

    /**
     * Gets the value of the elementName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getElementName() {
        return elementName;
    }

    /**
     * Sets the value of the elementName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setElementName(String value) {
        this.elementName = value;
    }

    /**
     * Gets the value of the xmlAttribute property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isXmlAttribute() {
        if (xmlAttribute == null) {
            return false;
        } else {
            return xmlAttribute;
        }
    }

    /**
     * Sets the value of the xmlAttribute property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setXmlAttribute(Boolean value) {
        this.xmlAttribute = value;
    }

    public Boolean getIncludeNullValues() {
        return includeNullValues;
    }

    public void setIncludeNullValues(Boolean includeNullValues) {
        this.includeNullValues = includeNullValues;
    }
}

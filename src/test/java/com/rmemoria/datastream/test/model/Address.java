/**
 * 
 */
package com.rmemoria.datastream.test.model;

/**
 * Simple address object for testing
 * @author Ricardo Memoria
 *
 */
public class Address {

	public enum AddressArea {
		URBAN,
		RURAL
	};
	
	private String street;
	private String zip;
	private AddressArea area;
	private int number;

	/**
	 * @return the street
	 */
	public String getStreet() {
		return street;
	}
	/**
	 * @param street the street to set
	 */
	public void setStreet(String street) {
		this.street = street;
	}
	/**
	 * @return the zip
	 */
	public String getZip() {
		return zip;
	}
	/**
	 * @param zip the zip to set
	 */
	public void setZip(String zip) {
		this.zip = zip;
	}
	/**
	 * @return the area
	 */
	public AddressArea getArea() {
		return area;
	}
	/**
	 * @param area the area to set
	 */
	public void setArea(AddressArea area) {
		this.area = area;
	}
	/**
	 * @return the number
	 */
	public int getNumber() {
		return number;
	}
	/**
	 * @param number the number to set
	 */
	public void setNumber(int number) {
		this.number = number;
	}
}

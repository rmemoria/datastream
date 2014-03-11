/**
 * 
 */
package com.rmemoria.datastream.test.model;

/**
 * @author Ricardo Memoria
 *
 */
public class CustomerWrapper extends Customer {

	private int notToBeSerialized = 1111;

	/**
	 * @return the notToBeSerialized
	 */
	public int getNotToBeSerialized() {
		return notToBeSerialized;
	}

	/**
	 * @param notToBeSerialized the notToBeSerialized to set
	 */
	public void setNotToBeSerialized(int notToBeSerialized) {
		this.notToBeSerialized = notToBeSerialized;
	}
}

/**
 * 
 */
package com.rmemoria.datastream.test.model;

/**
 * @author Ricardo Memoria
 *
 */
public class OrderWrapper extends Order {

	private int notToBeSerialized = 2222;

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

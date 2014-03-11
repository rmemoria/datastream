/**
 * 
 */
package com.rmemoria.datastream.test.model;

/**
 * @author Ricardo Memoria
 *
 */
public class ItemWrapper extends Item {

	private int notToBeSerialized = 3333;

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

/**
 * 
 */
package com.rmemoria.datastream.test.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ricardo Memoria
 *
 */
public class Customer {

	// this field is just to be ignored by the property scanner
	public int TEST_FIELD = 0;
	
	private Integer id;
	private String name;
	private String email;
	private List<Order> orders = new ArrayList<Order>();
	private Address address;
	private Address address2;

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}
	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	/**
	 * @return the orders
	 */
	public List<Order> getOrders() {
		return orders;
	}
	/**
	 * @param orders the orders to set
	 */
	public void setOrders(List<Order> orders) {
		this.orders = orders;
	}
	/**
	 * @return the address
	 */
	public Address getAddress() {
		return address;
	}
	/**
	 * @param address the address to set
	 */
	public void setAddress(Address address) {
		this.address = address;
	}
	/**
	 * @return the address2
	 */
	public Address getAddress2() {
		return address2;
	}
	/**
	 * @param address2 the address2 to set
	 */
	public void setAddress2(Address address2) {
		this.address2 = address2;
	}
}

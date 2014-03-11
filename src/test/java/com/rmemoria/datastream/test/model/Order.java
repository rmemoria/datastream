/**
 * 
 */
package com.rmemoria.datastream.test.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Ricardo Memoria
 *
 */
public class Order {

	private Integer id;
	private Customer customer;
	private float discount;
	private Date orderDate;
	private OrderStatus status;
	private List<Item> items = new ArrayList<Item>();

	/**
	 * @return the customer
	 */
	public Customer getCustomer() {
		return customer;
	}
	/**
	 * @param customer the customer to set
	 */
	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
	/**
	 * @return the discount
	 */
	public float getDiscount() {
		return discount;
	}
	/**
	 * @param discount the discount to set
	 */
	public void setDiscount(float discount) {
		this.discount = discount;
	}
	/**
	 * @return the orderDate
	 */
	public Date getOrderDate() {
		return orderDate;
	}
	/**
	 * @param orderDate the orderDate to set
	 */
	public void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
	}
	/**
	 * @return the status
	 */
	public OrderStatus getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(OrderStatus status) {
		this.status = status;
	}
	/**
	 * @return the items
	 */
	public List<Item> getItems() {
		return items;
	}
	/**
	 * @param items the items to set
	 */
	public void setItems(List<Item> items) {
		this.items = items;
	}
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
}

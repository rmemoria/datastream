/**
 * 
 */
package com.rmemoria.datastream.test.model;

/**
 * @author Ricardo Memoria
 *
 */
public class LinkedItem {

	private Integer id;
	private LinkedItem parent;

	public LinkedItem() {
		super();
	}
	
	public LinkedItem(Integer id, LinkedItem parent) {
		super();
		this.id = id;
		this.parent = parent;
	}
	
	public int getLevel() {
		int level = 0;
		LinkedItem aux = parent;
		while (aux != null) {
			level++;
			aux = aux.getParent();
		}
		return level;
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
	/**
	 * @return the parent
	 */
	public LinkedItem getParent() {
		return parent;
	}
	/**
	 * @param parent the parent to set
	 */
	public void setParent(LinkedItem parent) {
		this.parent = parent;
	}
}

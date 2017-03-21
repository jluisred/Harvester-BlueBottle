package org.librairy.bluebottle.datastructure;

import java.io.Serializable;

public class Component implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String oldHref;
	String href;
	String id;
	public String getOldHref() {
		return oldHref;
	}
	public void setOldHref(String oldHref) {
		this.oldHref = oldHref;
	}
	public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

}

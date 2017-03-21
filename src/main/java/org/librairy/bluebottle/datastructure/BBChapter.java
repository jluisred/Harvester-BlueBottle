package org.librairy.bluebottle.datastructure;

import java.io.Serializable;

public class BBChapter implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	DataChapter data;

	public DataChapter getData() {
		return data;
	}

	public void setData(DataChapter data) {
		this.data = data;
	}
	
	
}

package org.librairy.bluebottle.datastructure;

import java.io.Serializable;
import java.util.List;

public class BBResourceUnit implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Data data;
	BBBBook book;

	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}
	
	
	public BBBBook getBook() {
		return book;
	}

	public void setBook(BBBBook book) {
		this.book = book;
	}


	public class BBBBook implements Serializable{
		
		private static final long serialVersionUID = 3L;

		String hash;
		String isbn;
		String name;
		public String getHash() {
			return hash;
		}
		public void setHash(String hash) {
			this.hash = hash;
		}
		public String getIsbn() {
			return isbn;
		}
		public void setIsbn(String isbn) {
			this.isbn = isbn;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		
		

	}

	
	
	public class Data implements Serializable{
		
		private static final long serialVersionUID = 2L;

		
		
		List<Component> components;

		public List<Component> getComponents() {
			return components;
		}

		public void setComponents(List<Component> components) {
			this.components = components;
		}

	}
}





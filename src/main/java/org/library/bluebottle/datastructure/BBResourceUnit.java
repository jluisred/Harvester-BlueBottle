package org.library.bluebottle.datastructure;

import java.util.List;

public class BBResourceUnit {
	Data data;

	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}
	
	
	public class Data{
		List<Component> components;

		public List<Component> getComponents() {
			return components;
		}

		public void setComponents(List<Component> components) {
			this.components = components;
		}

	}

}





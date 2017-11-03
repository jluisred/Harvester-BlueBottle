package org.librairy.bluebottle.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.librairy.bluebottle.datastructure.BBBResource;
import org.librairy.bluebottle.datastructure.BBResourceUnit;

public class CacheBB {
	
	String folder = "./cache/";

	public CacheBB(){
		File file = new File(folder);
		if (!file.exists()) file.mkdirs();
	}

	public boolean containsPage(int p) {
		File page = new File (folder+"Page"+p);
		return  (page.exists());
			
	}

	public void savePage(List<BBBResource> books, int p) {
		
		
		FileOutputStream fos;
		try {
			File file = new File(folder+"Page"+p);
			if (!file.exists()) file.createNewFile();
			fos = new FileOutputStream(file);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(books);
			oos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public List<BBBResource> getPage(int p) {
		// TODO Auto-generated method stub
		
		List<BBBResource> page = null;
		try {
			FileInputStream fis = new FileInputStream(folder+"Page"+p);
			ObjectInputStream ois = new ObjectInputStream(fis);
			page =  (List<BBBResource>) ois.readObject();
			ois.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		
		return page;
	}

	public boolean containsResource(String seoBook) {
		File resource = new File (folder+"Resource"+seoBook);
		return  (resource.exists());
	}

	public void saveResource(BBResourceUnit resource, String seoBook) {
		FileOutputStream fos;
		try {
			File file = new File(folder+"Resource"+seoBook);
			if (!file.exists()) file.createNewFile();
			fos = new FileOutputStream(file);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(resource);
			oos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public BBResourceUnit getResource(String seoBook) {
		// TODO Auto-generated method stub
		BBResourceUnit resource  = null;
		try {
			FileInputStream fis = new FileInputStream(folder+"Resource"+seoBook);
			ObjectInputStream ois = new ObjectInputStream(fis);
			resource =  (BBResourceUnit) ois.readObject();
			ois.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		
		return resource;
	}

	public boolean containsTextChapter(String id) {
		File textChapter = new File (folder+"TextChapter"+id);
		return  (textChapter.exists());
	}

	public void saveTextChapter(String text, String id) {
		FileOutputStream fos;
		try {
			File file = new File(folder+"TextChapter"+id);
			if (!file.exists()) file.createNewFile();
			fos = new FileOutputStream(file);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(text);
			oos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public String getTextChapter(String id) {
		// TODO Auto-generated method stub
		String textChapter  = null;
		try {
			FileInputStream fis = new FileInputStream(folder+"TextChapter"+id);
			ObjectInputStream ois = new ObjectInputStream(fis);
			textChapter =  (String) ois.readObject();
			ois.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		
		return textChapter;
	}

}

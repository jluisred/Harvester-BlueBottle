package org.librairy.bluebottle.datastructure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BBBResource implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String hash;
	String name;
	String subtitle;
	String bookCover;
	String seoBook;
	List<String> authors;
	String editionYear;
	String contentType;
	String isbn;
	String summary;
	int avgRating;
	int numRating;
	String status;
	int shares;
	boolean reader;
	
	//To be filled up in collection
	List<DataChapter> chapters= null;
	
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSubtitle() {
		return subtitle;
	}
	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}
	public String getBookCover() {
		return bookCover;
	}
	public void setBookCover(String bookCover) {
		this.bookCover = bookCover;
	}
	public String getSeoBook() {
		return seoBook;
	}
	public void setSeoBook(String seoBook) {
		this.seoBook = seoBook;
	}
	public List<String> getAuthors() {
		return authors;
	}
	public void setAuthors(List<String> authors) {
		this.authors = authors;
	}
	public String getEditionYear() {
		return editionYear;
	}
	public void setEditionYear(String editionYear) {
		this.editionYear = editionYear;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public String getIsbn() {
		return isbn;
	}
	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public int getAvgRating() {
		return avgRating;
	}
	public void setAvgRating(int avgRating) {
		this.avgRating = avgRating;
	}
	public int getNumRating() {
		return numRating;
	}
	public void setNumRating(int numRating) {
		this.numRating = numRating;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public int getShares() {
		return shares;
	}
	public void setShares(int shares) {
		this.shares = shares;
	}
	public boolean isReader() {
		return reader;
	}
	public void setReader(boolean reader) {
		this.reader = reader;
	}
	
	
	public List<DataChapter> getChapters() {
		if (chapters == null) 	
			chapters = new ArrayList<DataChapter>();
		return chapters;
	}
	public void setChapters(List<DataChapter> chapters) {
		this.chapters = chapters;
	}
	

	
	
}

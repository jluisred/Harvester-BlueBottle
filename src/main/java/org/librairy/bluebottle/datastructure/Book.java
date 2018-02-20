package org.librairy.bluebottle.datastructure;

import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;

import java.text.Normalizer;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class Book {

    Escaper escaper = Escapers.builder()
            .addEscape('\'', "_")
            .addEscape('(', " ")
            .addEscape(')', " ")
            .addEscape('[', " ")
            .addEscape(']', " ")
            .addEscape('“', "\"")
            .addEscape('"', " ")
            .addEscape('…', " ")
            .addEscape('‘', " ")
            .addEscape('\n', " ")
            .addEscape('‘', " ")
            .addEscape('´', " ")
            .build();
    ;

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
    List<Chapter> chapters;

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
        String nameEncode = Normalizer.normalize(name, Normalizer.Form.NFD);
        nameEncode = escaper.escape(nameEncode);
        nameEncode = nameEncode.replaceAll("\\P{Print}", "");
        //text = text.replaceAll("[^\\x00-\\x7F]", "");
        //text = text.replaceAll("\\P{InBasic_Latin}", "");
        //text = text.replaceAll("\\p{Cc}", "");
        //text= text.replaceAll("[\u0000-\u001f]", "" );


        byte ptext[] = nameEncode.getBytes(ISO_8859_1);
        nameEncode = new String(ptext, UTF_8);
        this.name = nameEncode;
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

    public List<Chapter> getChapters() {
        return chapters;
    }

    public void setChapters(List<Chapter> chapters) {
        this.chapters = chapters;
    }

    public String toCSV(String separator){
        return new StringBuilder()
                .append(hashCode()).append(separator)
                .append(name).append(separator)
                .append(chapters.stream().map(chapter -> chapter.getOneLineContent()).collect(Collectors.joining(". "))).append(separator)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Book book = (Book) o;

        if (!hash.equals(book.hash)) return false;
        return name.equals(book.name);

    }

    @Override
    public int hashCode() {
        int result = hash.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Book{" +
                "hash='" + hash + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}

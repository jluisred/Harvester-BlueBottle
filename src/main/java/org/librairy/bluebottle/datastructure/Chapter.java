package org.librairy.bluebottle.datastructure;

import com.google.common.base.Strings;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class Chapter {

    String id;
    String bookId;
    String content;
    String oneLineContent;

    public Chapter(String id, String bookId, String content) {
        this.id = id;
        this.bookId = bookId;
        this.content = content;
        this.oneLineContent = content.replace("\n","");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        this.oneLineContent = content.replace("\n","");
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getOneLineContent(){
        return oneLineContent;
    }

    public String toCSV(String separator){
        return new StringBuilder()
                .append(id).append(separator)
                .append(bookId).append(separator)
                .append(oneLineContent)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Chapter chapter = (Chapter) o;

        return id.equals(chapter.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Chapter{" +
                "id='" + id + '\'' +
                '}';
    }
}

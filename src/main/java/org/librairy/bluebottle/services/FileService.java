package org.librairy.bluebottle.services;

import org.librairy.bluebottle.datastructure.Book;
import org.librairy.bluebottle.datastructure.DataChapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.GZIPOutputStream;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class FileService {

    private static final Logger LOG = LoggerFactory.getLogger(FileService.class);


    private final BufferedWriter bookWriter;
    private final BufferedWriter chapterWriter;

    public FileService(String baseDir) throws IOException {

        String dateString = new SimpleDateFormat("yyyyMMdd'T'HHmmss").format(new Date());

        Path booksFilePath      = Paths.get(baseDir, "books", "b-"+dateString+".csv.gz");
        Path chaptersFilePath   = Paths.get(baseDir, "chapters", "c-"+dateString+".csv.gz");

        booksFilePath.toFile().getParentFile().mkdirs();
        chaptersFilePath.toFile().getParentFile().mkdirs();

        bookWriter = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(booksFilePath.toFile()))));
        chapterWriter = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(chaptersFilePath.toFile()))));


    }

    public void close() throws IOException {

        bookWriter.close();
        chapterWriter.close();
    }

    public void write(Book book){

        String separator = ";;";
        try {
            bookWriter.write(book.toCSV(separator));
            bookWriter.write("\n");

            book.getChapters().forEach(chapter -> {
                try {
                    chapterWriter.write(chapter.toCSV(separator));
                    chapterWriter.write("\n");
                } catch (IOException e) {
                    LOG.error("Error writing on file chapter: " + chapter, e);
                }
            });


        } catch (IOException e) {
            LOG.error("Error writing on file book: " + book, e);
        }


    }

}

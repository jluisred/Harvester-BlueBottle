package org.librairy.bluebottle.services;

import org.librairy.bluebottle.datastructure.*;
import org.librairy.bluebottle.exception.ApiError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class ImportService {

    private static final Logger LOG = LoggerFactory.getLogger(ImportService.class);

    public void retrieve(int page) throws InterruptedException, IOException {
        //TODO filter by date

        BBClient client             = new BBClient();
        FileService fileService     = new FileService("output");
        ExecutorService executor    = Executors.newWorkStealingPool();
        Instant startModel          = Instant.now();

        List<Future<String>> results = new ArrayList<>();

        try {
            int windowSize = 100;
            int numPages = (page < 0)? client.getNumPages(windowSize) : page;

            for (int i=0;i<numPages;i++){


                List<BBBResource> books = client.getBooks(i, windowSize);


                List<Callable<String>> actions = books.stream().map(resource -> {
                    Callable<String> action = new Callable<String>() {
                        @Override
                        public String call() throws Exception {

                            Book book = new Book();
                            BeanUtils.copyProperties(resource, book);

                            LOG.info("Retrieving book: " + book);
                            BBResourceUnit chaptersResource = client.getChapters(book.getSeoBook());
                            if (chaptersResource == null) {
                                LOG.warn(book + " has not chapters");
                                return "Action performed on: " + book.toString();
                            }


                            BBResourceUnit.Data data = chaptersResource.getData();
                            if (data == null) {
                                LOG.warn(book + " has not data");
                                return "Action performed on: " + book.toString();
                            }

                            List<Component> components = data.getComponents();
                            if (components == null || components.isEmpty()) {
                                LOG.warn(book + " has not components");
                                return "Action performed on: " + book.toString();
                            }

                            List<Chapter> chapters = components.stream().map(component -> {
                                try {
                                    String text = client.getChapter(book.getSeoBook(), component.getId());
                                    Chapter chapter = new Chapter(component.getId(), book.getHash(), text);
                                    return chapter;
                                } catch (ApiError apiError) {
                                    LOG.error("Error getting chapter '" + component.getId() + "' of book '" + book.getSeoBook() + "'");
                                    return null;
                                }
                            }).filter(r -> r != null).collect(Collectors.toList());
                            book.setChapters(chapters);


                            // write book to file
                            fileService.write(book);

                            return "Action performed on: " + book.toString();
                        }
                    };
                    return action;
                }).collect(Collectors.toList());

                LOG.info(actions.size() + " actions/books ready to be performed/retrieved!");

                results.addAll(executor.invokeAll(actions));
            }

        } catch (ApiError e) {
            LOG.error("Error on api request",e);
        }


        results.stream().map(future -> {
            try{
                return future.get();
            }catch (Exception e){
                throw new IllegalStateException(e);
            }
        }).forEach(System.out::println);

        Instant endModel    = Instant.now();
        LOG.info("All books retrieved in: "
                + ChronoUnit.HOURS.between(startModel,endModel) + "hours "
                + ChronoUnit.MINUTES.between(startModel,endModel)%60 + "min "
                + (ChronoUnit.SECONDS.between(startModel,endModel)%3600) + "secs");
        fileService.close();
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        ImportService importService = new ImportService();
        importService.retrieve(1);

    }

}

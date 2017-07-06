package org.librairy.bluebottle.load;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URI;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import org.librairy.bluebottle.cache.CacheBB;
import org.librairy.bluebottle.conf.Conf;
import org.librairy.bluebottle.datastructure.BBBResource;
import org.librairy.bluebottle.datastructure.BBChapter;
import org.librairy.bluebottle.datastructure.BBResourceUnit;
import org.librairy.bluebottle.datastructure.DataChapter;
import org.librairy.bluebottle.datastructure.LibrairyDomain;
import org.librairy.bluebottle.exception.ApiError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import com.google.gson.Gson;

//@Component
public class BlueBottleLoaderParallel {
    private static Logger LOG = LoggerFactory.getLogger(BlueBottleLoaderParallel.class);

    private static Integer MIN_CHAPTER_LENGTH = 2500;

    private final ObjectMapper jsonMapper;

    //Tracking variables
    AtomicInteger numBooks                  = new AtomicInteger(0);
    AtomicInteger numBooksNotDownloaded     = new AtomicInteger(0);
    AtomicInteger numBooksSaved             = new AtomicInteger(0);
    AtomicInteger numBooksNotSaved          = new AtomicInteger(0);
    AtomicInteger numBooksInDomain          = new AtomicInteger(0);
    AtomicInteger numBooksNotInDomain       = new AtomicInteger(0);
    AtomicInteger numBooksNoData            = new AtomicInteger(0);
    AtomicInteger numBooksEmpty             = new AtomicInteger(0);
    AtomicInteger numBooksWithoutChapters   = new AtomicInteger(0);

    AtomicInteger numChapters               = new AtomicInteger(0);
    AtomicInteger numChaptersNotDownloaded  = new AtomicInteger(0);
    AtomicInteger numChaptersDiscarded      = new AtomicInteger(0);
    AtomicInteger numChaptersSaved          = new AtomicInteger(0);
    AtomicInteger numChaptersNotSaved       = new AtomicInteger(0);
    AtomicInteger numChaptersAssociated     = new AtomicInteger(0);
    AtomicInteger numChaptersInDomain       = new AtomicInteger(0);
    AtomicInteger numChaptersNotInDomain    = new AtomicInteger(0);
    AtomicInteger numChaptersEmpty          = new AtomicInteger(0);


    Integer querySize = 100;

    public BlueBottleLoaderParallel() {
        String minValue = System.getenv("MIN_CHAPTER_LENGTH");
        if (!Strings.isNullOrEmpty(minValue)){
            MIN_CHAPTER_LENGTH = Integer.valueOf(minValue);
        }
        this.jsonMapper = new ObjectMapper();
    }


    //@PostConstruct
    public void setup() {
        LOG.info("Loader UP!");
    }


    public void loadBooks() throws JsonProcessingException, ApiError {


//        List<BBBResource> booksEmpty = new ArrayList<BBBResource>();
//        List<String> booksNoData = new ArrayList<String>();
//        List<String> booksNoComponents = new ArrayList<String>();
//        List<String> booksComponents = new ArrayList<String>();

//        List<BBBResource> books = null;

//		ParallelExecutor pe = new ParallelExecutor();

        //Initialize Domain and Source
        String domain = setupDomain().getId();
        //String domain ="ae5753952f7db4b1d56a5942e08476f9"; //200
        //String domain ="2de4b3245d79606bdd31176b201b1ab6";// 200B
        // String domain = "141fc5bbcf0212ec9bee5ef66c6096ab"; //Bluebottle


        //get list of books
        RestTemplate restTemplate = new RestTemplate();

        //Build Headers
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-api-key", Conf.getApikey());
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.setContentType(MediaType.APPLICATION_JSON);

        //Build Request
        HttpEntity<String> request = new HttpEntity<String>(headers);


        //getNumPages
        int numPages = 0;
//        int numPages = 1;
        if (Conf.getLoadGT()) numPages = 1;
        else {
            if (Conf.getForcePages() > 0) numPages = Conf.getForcePages();
            else numPages = getNumPages(restTemplate, request);
        }
        LOG.debug("NUMBER OF PAGES: " + numPages);

        int minPage = 1;
        if (Conf.getStartPage() > 0) minPage = Conf.getStartPage();


        for (int p = minPage; p <= numPages; p++) {

            List<BBBResource> books = new ArrayList<>();

            // Launch Get Page
            CacheBB cache = new CacheBB();

            try {
                if (!Conf.isCacheEnabled()) {
                    books = getBooksInPage(restTemplate, request, p);
                } else {
                    if (!cache.containsPage(p)) {
                        books = getBooksInPage(restTemplate, request, p);
                        cache.savePage(books, p);
                    } else books = cache.getPage(p);
                }
            } catch (ApiError e){
                LOG.error("api-error getting books in page by request: " + request, e);
                numBooksNotDownloaded.addAndGet(querySize);
                continue;
            } catch(Exception e){
                LOG.error("unexpected error getting books in page by request: " + request, e);
                numBooksNotSaved.addAndGet(querySize);
                continue;
            }


            if (Conf.getLoadGT()) {
                //LOG.info("NumResources: " + books.size());
                //Hack for adding 5 books in gold standard
                BBBResource book1 = new BBBResource();
                book1.setSeoBook("defying-doom");
                book1.setName("DEFYING DOOM");
                book1.setHash("RB6qb-VsRYsOZ");
                LOG.debug(book1.getHash());
                BBBResource book2 = new BBBResource();
                book2.setSeoBook("outstanding-business-english");
                book2.setName("Outstanding business English");
                book2.setHash("82WxBdWh6U4");
                BBBResource book3 = new BBBResource();
                book3.setSeoBook("the-art-of-shopping");
                book3.setName("The Art of Shopping");
                book3.setHash("M57xA96F4Ix");
                BBBResource book4 = new BBBResource();
                book4.setSeoBook("the-content-revolution");
                book4.setName("The content revolution");
                book4.setHash("rV_NJKNSdiL");
                BBBResource book5 = new BBBResource();
                book5.setSeoBook("designpedia");
                book5.setName("Designpedia");
                book5.setHash("5d73LzOTM7Udw");
                books.clear();
                books.add(book1);
                books.add(book2);
                books.add(book3);
                books.add(book4);
                books.add(book5);
            }
            //get Chapters

            final int currentPage = p;
            //Conf.isParallelProcessing()
            books.parallelStream().forEach( book -> {
                numBooks.getAndIncrement();
                try {
                    processBook(restTemplate, request, book, domain, currentPage);
                } catch (ApiError e){
                    LOG.error("api-error processing book: " + book.getHash(), e);
                } catch(Exception e){
                    LOG.error("unexpected error processing book: " + book.getHash(), e);
                    numBooksNotSaved.getAndIncrement();
                }
            });


            //chapterIDs.stream().forEach(chapterID -> LOG.info("     Chapter: " + chapterID));


        }

        //REPORT ON COLLECTION
        LOG.info("==============================================");
        LOG.info("Books listed: " + numBooks.get());
        LOG.info("Books not downloaded: " + numBooksNotDownloaded.get());
        LOG.info("Books saved: " + numBooksSaved.get());
        LOG.info("Books not saved: " + numBooksNotSaved.get());
        LOG.info("Books empty: " + numBooksEmpty.get());
        LOG.info("Books with no DATA: " + numBooksNoData.get());
        LOG.info("Books with no Chapters: " + numBooksWithoutChapters.get());
        LOG.info("Books in domain: " + numBooksInDomain.get());
        LOG.info("Books NOT in domain: " + numBooksNotInDomain.get());
        LOG.info("----------------------------------------------");
        LOG.info("Chapters listed: " + numChapters.get());
        LOG.info("Chapters not downloaded: " + numChaptersNotDownloaded.get());
        LOG.info("Chapters saved: " + numChaptersSaved.get());
        LOG.info("Chapters not saved: " + numChaptersNotSaved.get());
        LOG.info("Chapters associated: " + numChaptersAssociated.get());
        LOG.info("Chapters in domain: " + numChaptersInDomain.get());
        LOG.info("Chapters NOT in domain: " + numChaptersNotInDomain.get());
        LOG.info("Chapters empty: " + numChaptersEmpty.get());
        LOG.info("Chapters discarded: " + numChaptersDiscarded.get());
        LOG.info("==============================================");


        // sc.loadResourcesInLibrary (booksCatalog);
        //sc.deleteAll();

        //udm.save(resource)e(books.get(0));


    }


    private LibrairyDomain setupDomain() throws JsonProcessingException {
        JsonNode domainJson = this.jsonMapper.createObjectNode();

        ((ObjectNode) domainJson).put("name", Conf.getRunConf());


        RestTemplate restTemplate = new RestTemplate();

        //Build Headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.setContentType(MediaType.APPLICATION_JSON);


        //Build Request
        HttpEntity<String> request = new HttpEntity<String>(jsonMapper.writeValueAsString(domainJson), headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(Conf.getEndpointLibrairy() + "domains/" +
				Conf.getRunConf());

        URI uri = builder.build().encode().toUri();
        ResponseEntity<String> response = restTemplate.exchange(uri,
                HttpMethod.POST,
                request,
                String.class);


        Gson gson = new Gson();
        LibrairyDomain domain = gson.fromJson(response.getBody(), LibrairyDomain.class);

//        // Simulate DOmain ->
//        LibrairyDomain domain = new LibrairyDomain();
//        domain.setId("blueBottle");
//        domain.setUrl("http://librairy.org/domains/blueBottle");
//        domain.setName("blueBottle");
//        domain.setCreation("2017-06-15T14:08+0000");
//        // <-
        LOG.debug("Domain id " + domain.getId());

//    "name": "tokenizer.mode",   "value": "lemma"

        setParametersDomain(domain, "tokenizer.mode", Conf.getTokenizerMode());
        setParametersDomain(domain, "lda.delay", Integer.toString(Conf.getLdaDelay()));
        setParametersDomain(domain, "w2v.delay", Integer.toString(Conf.getW2vDelay()));

        if (Conf.getTopics().isPresent()){
            setParametersDomain(domain, "lda.optimizer", "manual");
            setParametersDomain(domain, "lda.topics", Integer.toString(Conf.getTopics().get()));
        }

        return domain;


    }


    private void setParametersDomain(LibrairyDomain domain, String name, String value) throws JsonProcessingException {


        JsonNode domainJson = this.jsonMapper.createObjectNode();

        ((ObjectNode) domainJson).put("name", name);
        ((ObjectNode) domainJson).put("value", value);


        RestTemplate restTemplate = new RestTemplate();

        //Build Headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.setContentType(MediaType.APPLICATION_JSON);


        //Build Request

        HttpEntity<String> request = new HttpEntity<String>(jsonMapper.writeValueAsString(domainJson), headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(Conf.getEndpointLibrairy() + "/domains/" +
                domain.getId() + "/parameters");
        //LOG.debug(builder.build().encode().toUri());
        URI uri = builder.build().encode().toUri();
        restTemplate.exchange(uri,
                HttpMethod.POST,
                request,
                String.class);


    }


    private void processBook(RestTemplate restTemplate, HttpEntity<String> request, BBBResource book, String domain, int page) throws JsonProcessingException, ApiError {
        LOG.info("Processing book: '" + book.getSeoBook() + "'");
        //Retrieving Chapters
        BBResourceUnit resource = null;

        CacheBB cache = new CacheBB();
        if (!Conf.isCacheEnabled()) {
            resource = retrieveChapters(restTemplate, request, book.getSeoBook());
        } else {
            if (!cache.containsResource(book.getSeoBook())) {
                resource = retrieveChapters(restTemplate, request, book.getSeoBook());
                cache.saveResource(resource, book.getSeoBook());
            } else resource = cache.getResource(book.getSeoBook());
        }

        int cont = 0;
        while (resource == null && cont < 20) {
            resource = retrieveChapters(restTemplate, request, book.getSeoBook());
            if (resource != null) cache.saveResource(resource, book.getSeoBook());
            cont++;
        }


        if (resource == null) {
            LOG.warn("Book '" + book.getHash() +"' is empty");
            numBooksEmpty.getAndIncrement();
            return;
        }


        LOG.debug("Resource: " + book.getSeoBook());

        //Clean
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
                .build();
        ;


        if (resource.getData() != null) { //Checking books with no Data

            if (resource.getData().getComponents() != null && !resource.getData().getComponents().isEmpty()) {//Checking books
                // with no Components

                //Generate book
                //LibrairyDocument document = saveBookLibrairy(book, "");


                String textBook = "";

                //Retrieving text
                for (org.librairy.bluebottle.datastructure.Component c : resource.getData().getComponents()) {

                    numChapters.getAndIncrement();

                    //LOG.info("       Chapter: " + c.getId());
                    String text = "";


                    if (!Conf.isCacheEnabled()) {
                        text = retrieveTextChapter(restTemplate, request, book.getSeoBook(), c.getId());
                        text = Normalizer.normalize(text, Normalizer.Form.NFD);

                        text = escaper.escape(text);
                        text = text.replaceAll("\\P{Print}", "");
                        //text = text.replaceAll("[^\\x00-\\x7F]", "");
                        //text = text.replaceAll("\\P{InBasic_Latin}", "");
                        //text = text.replaceAll("\\p{Cc}", "");
                        //text= text.replaceAll("[\u0000-\u001f]", " ");


                        byte ptext[] = text.getBytes(ISO_8859_1);
                        text = new String(ptext, UTF_8);
                    } else {
                        if (!cache.containsTextChapter(c.getId())) {
                            text = retrieveTextChapter(restTemplate, request, book.getSeoBook(), c.getId());
                            text = Normalizer.normalize(text, Normalizer.Form.NFD);

                            text = escaper.escape(text);
                            text = text.replaceAll("\\P{Print}", "");
                            //text = text.replaceAll("[^\\x00-\\x7F]", "");
                            //text = text.replaceAll("\\P{InBasic_Latin}", "");
                            //text = text.replaceAll("\\p{Cc}", "");
                            //text= text.replaceAll("[\u0000-\u001f]", " ");

                            byte ptext[] = text.getBytes(ISO_8859_1);
                            text = new String(ptext, UTF_8);
                            cache.saveTextChapter(text, c.getId());
                        } else {
                            text = cache.getTextChapter(c.getId());
                            text = Normalizer.normalize(text, Normalizer.Form.NFD);
                            text = escaper.escape(text);

                            text = text.replaceAll("\\P{Print}", "");
                            // text = text.replaceAll("[^\\x00-\\x7F]", "");
                            //text = text.replaceAll("\\P{InBasic_Latin}", "");
                            //text = text.replaceAll("\\p{Cc}", "");
                            //text= text.replaceAll("[\u0000-\u001f]", " ");


                        }
                    }
                    int contChap = 0;
                    while (text == null && contChap < 20) {
                        text = retrieveTextChapter(restTemplate, request, book.getSeoBook(), c.getId());
                        text = Normalizer.normalize(text, Normalizer.Form.NFD);
                        text = escaper.escape(text);
                        text = text.replaceAll("\\P{Print}", "");
                        //text = text.replaceAll("[^\\x00-\\x7F]", "");
                        //text = text.replaceAll("\\P{InBasic_Latin}", "");
                        //text = text.replaceAll("\\p{Cc}", "");
                        //text= text.replaceAll("[\u0000-\u001f]", "" );


                        byte ptext[] = text.getBytes(ISO_8859_1);
                        text = new String(ptext, UTF_8);
                        if (text != null) cache.saveTextChapter(text, c.getId());
                        if (resource != null) cache.saveResource(resource, book.getSeoBook());
                        contChap++;
                    }


                    if (Strings.isNullOrEmpty(text)){
                        LOG.warn("Chapter '" + c.getId() + "' from book '"+ book.getHash() + "' is empty");
                        numChaptersEmpty.getAndIncrement();
                    }

                    if (text.length() > MIN_CHAPTER_LENGTH){
                        book.getChapters().add(new DataChapter(c.getId(), text));
                    }else{
                        numChaptersDiscarded.getAndIncrement();
                    }
                    textBook = textBook + " " + text;
                }
                //LibrairyDocument document = saveBookLibrairy(book, textBook);
                LOG.debug("Book id " + book.getHash() + " from page " + page);

                if (saveBookLibrairy(book, textBook)) {


                    savePartItemLibrairy(book, page);


                    //Add book to Domain
                    addDocumentToDomain(domain, book.getHash());
                }

            } else {
                LOG.warn("Book '" + book.getHash() + "' without chapters");
                numBooksWithoutChapters.getAndIncrement();
            }
        } else {
            LOG.warn("Book '" + book.getHash() + "' with no data");
            numBooksNoData.getAndIncrement();
        }

    }


    private void addDocumentToDomain(String domain, String documentID) throws JsonProcessingException {

        JsonNode domainJson = this.jsonMapper.createObjectNode();


        RestTemplate restTemplate = new RestTemplate();

        //Build Headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.setContentType(MediaType.APPLICATION_JSON);

        //Build Request

        HttpEntity<String> request = new HttpEntity<String>(jsonMapper.writeValueAsString(domainJson), headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(Conf.getEndpointLibrairy() + "domains/" +
				domain + "/documents/" + documentID);


        if (executePOSTQuery(restTemplate, builder, request)){
            numBooksInDomain.getAndIncrement();
        } else{
            LOG.warn("Book '" + documentID + "' not in domain");
            numBooksNotInDomain.getAndIncrement();
        }
    }


    private Boolean executePOSTQuery(RestTemplate restTemplate, UriComponentsBuilder builder, HttpEntity<String> request){

        Integer retries = 0;
        Integer maxRetries = 4;

        while(retries < maxRetries){
            retries++;
            URI uri = builder.build().encode().toUri();
            try {
                LOG.debug("HTTP-POST Request to: " + uri.toString());
                ResponseEntity<String> response = restTemplate.exchange(uri,
                        HttpMethod.POST,
                        request,
                        String.class);

                return true;
            } catch (HttpClientErrorException e) {
                String requestDescription = (request!= null && Strings.isNullOrEmpty(request.toString()))? "" :
                        request.getHeaders() + " .. }>";
                LOG.error("HTTP-ERROR: " + e.getStatusCode() + " on " + uri.toString() + " with request: " + requestDescription);
                return false;
            } catch (RestClientException e) {
                String requestDescription = (request!= null && Strings.isNullOrEmpty(request.toString()))? "" : request.getHeaders() + " .. }>";
                String msg = "Unexpected error on HTTP-Request to " + uri + " with request: " + requestDescription + ". " +
                        "Retrying ( " + retries + " retries remaining)";
                if (retries>=maxRetries) LOG.error(msg,e);
                else {
                    LOG.debug(msg, e);
                    try {
                        Thread.sleep(retries*1000);
                    } catch (InterruptedException e1) {
                        LOG.error("Retry process interrupted");
                        return false;
                    }
                }
            }

        }
        return false;
    }

    private void savePartItemLibrairy(BBBResource book, int page) throws
            JsonProcessingException {

        for (DataChapter chapter : book.getChapters()) {


            JsonNode domainJson = this.jsonMapper.createObjectNode();

            ((ObjectNode) domainJson).put("language", "EN");
            ((ObjectNode) domainJson).put("content", chapter.getText());
            //domainJson.put("id", chapter.getId());
            //domainJson.put("uri", "http://librairy.org/parts/"+chapter.getId());


            RestTemplate restTemplate = new RestTemplate();

            //Build Headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
            headers.setContentType(MediaType.APPLICATION_JSON);

            //Create Part
            HttpEntity<String> request = new HttpEntity<String>(jsonMapper.writeValueAsString(domainJson), headers);
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(Conf.getEndpointLibrairy() + "parts/" + chapter.getId());

            if (executePOSTQuery(restTemplate, builder, request)){
                numChaptersSaved.getAndIncrement();


                HttpEntity<String> requestLinkPartDocument = new HttpEntity<String>(headers);
                UriComponentsBuilder builderLinkPartDocument = UriComponentsBuilder.fromHttpUrl(Conf.getEndpointLibrairy
                        () + "documents/" + book.getHash() + "/parts/" + chapter.getId());

                if (executePOSTQuery(restTemplate, builderLinkPartDocument, requestLinkPartDocument)){
                    numChaptersAssociated.getAndIncrement();
                    numChaptersInDomain.getAndIncrement();
                }else{
                    LOG.warn("Chapter '" + chapter.getId()+"' not associated to book '"+book.getHash()+"' and not in " +
                            "domain");
                    numChaptersNotInDomain.getAndIncrement();
                }

            }else{
                LOG.warn("Chapter '" + chapter.getId()+"' from book '"+book.getHash()+"' not saved");
                numChaptersNotSaved.getAndIncrement();
            }
        }


    }



    private boolean saveBookLibrairy(BBBResource book, String content) throws JsonProcessingException {

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


        String nameEncode = Normalizer.normalize(book.getName(), Normalizer.Form.NFD);
        nameEncode = escaper.escape(nameEncode);
        nameEncode = nameEncode.replaceAll("\\P{Print}", "");
        //text = text.replaceAll("[^\\x00-\\x7F]", "");
        //text = text.replaceAll("\\P{InBasic_Latin}", "");
        //text = text.replaceAll("\\p{Cc}", "");
        //text= text.replaceAll("[\u0000-\u001f]", "" );


        byte ptext[] = nameEncode.getBytes(ISO_8859_1);
        nameEncode = new String(ptext, UTF_8);


        JsonNode domainJson = this.jsonMapper.createObjectNode();

        ((ObjectNode) domainJson).put("name", nameEncode);
        ((ObjectNode) domainJson).put("language", "EN");
        ((ObjectNode) domainJson).put("content", content);
        //domainJson.put("uri", "http://librairy.org/items/"+book.getHash());


        RestTemplate restTemplate = new RestTemplate();

        //Build Headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.setContentType(MediaType.APPLICATION_JSON);

        //Build Request

        HttpEntity<String> request = new HttpEntity<String>(jsonMapper.writeValueAsString(domainJson), headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(Conf.getEndpointLibrairy() + "documents/" +
				book.getHash());


        LOG.debug(builder.build().encode().toUri().toString());

        if (executePOSTQuery(restTemplate, builder, request)){
            numBooksSaved.getAndIncrement();
            return true;
        }else{
            LOG.warn("Book '" + book.getHash() +"' not saved");
            numBooksNotSaved.getAndIncrement();
            return false;
        }
    }


    private static String retrieveTextChapter(RestTemplate restTemplate, HttpEntity<String> request, String seoBook, String id) throws ApiError {

        try{
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(Conf.getEndpointURL() + "/resources/" +
                    seoBook + "/components/" + id);
            //LOG.debug( builder.build().encode().toUri());
            ResponseEntity<BBChapter> response = restTemplate.exchange(builder.build().encode().toUri(),
                    HttpMethod.GET,
                    request,
                    BBChapter.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() == null) return "";
            return response.getBody().getData().getText();
        }catch (Exception e){
            throw new ApiError(e);
        }
    }


    private static BBResourceUnit retrieveChapters(RestTemplate restTemplate, HttpEntity<String> request, String seoBook) throws ApiError {

        try{
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(Conf.getEndpointURL() + "/resources/" +
                    seoBook + "/reader");
            ResponseEntity<BBResourceUnit> response = restTemplate.exchange(builder.build().encode().toUri(),
                    HttpMethod.GET,
                    request,
                    BBResourceUnit.class);


            return response.getBody();
        }catch (Exception e){
            throw new ApiError(e);
        }


    }


    private List<BBBResource> getBooksInPage(RestTemplate restTemplate, HttpEntity<String> request, int p) throws ApiError {

        try{
            int numRetry = 0;
            boolean thereIsAnswer = false;
            ResponseEntity<BBBResource[]> response = null;


            while (!thereIsAnswer && numRetry < 20) {

                RestTemplate restTemplate2 = new RestTemplate();

                //Build Headers
                HttpHeaders headers = new HttpHeaders();
                headers.add("x-api-key", Conf.getApikey());
                headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
                headers.setContentType(MediaType.APPLICATION_JSON);

                //Build Request
                HttpEntity<BBBResource[]> request2 = new HttpEntity<BBBResource[]>(headers);

                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(Conf.getEndpointURL() + "/resources")
                        .queryParam("type", "epub").queryParam("lang", "eng").queryParam("nrows", String.valueOf(querySize)).queryParam
                                ("page", p);


                response = restTemplate2.exchange(builder.build().encode().toUri(),
                        HttpMethod.GET,
                        request2,
                        BBBResource[].class);
                if (response.getBody() != null) thereIsAnswer = true;
                else {
                    try {
                        Thread.sleep(2000);
                        LOG.debug("Retrying: " + builder.build().encode().toUri());
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        LOG.error("Unexpected error", e);
                    }
                }

                numRetry++;
            }

            return new ArrayList<BBBResource>(Arrays.asList(response.getBody()));
        }catch (Exception e){
            throw new ApiError(e);
        }

    }


    private static int getNumPages(RestTemplate restTemplate, HttpEntity<String> request) throws ApiError {


        try{
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(Conf.getEndpointURL() + "/resources")
                    .queryParam("type", "epub").queryParam("lang", "en").queryParam("nrows", "100");
            // Launch Get
            ResponseEntity<BBBResource[]> response = restTemplate.exchange(builder.build().encode().toUri(),
                    HttpMethod.GET,
                    request,
                    BBBResource[].class);

            LOG.info("Response: " + response.getStatusCode());
            LOG.info("x-limit: " + response.getHeaders().getFirst("x-limit"));
            LOG.info("x-page: " + response.getHeaders().getFirst("x-page"));
            LOG.info("x-total: " + response.getHeaders().getFirst("x-total"));
            LOG.info("x-totalPages: " + response.getHeaders().getFirst("x-totalPages"));


            return Integer.parseInt(response.getHeaders().getFirst("x-totalPages"));
        }catch (Exception e){
            throw new ApiError(e);
        }
    }

    public void updateTopics() throws JsonProcessingException {
        LOG.info("Updating topics in ..");
        JsonNode domainJson = this.jsonMapper.createObjectNode();

        RestTemplate restTemplate = new RestTemplate();

        //Build Headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.setContentType(MediaType.APPLICATION_JSON);

        //Build Request

        HttpEntity<String> request = new HttpEntity<String>(jsonMapper.writeValueAsString(domainJson), headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(Conf.getEndpointLibrairy() + "domains/" +
                Conf.getRunConf() + "/topics");

        URI uri = builder.build().encode().toUri();
        LOG.debug("HTTP-PUT Request to: " + uri.toString());
        ResponseEntity<String> response = restTemplate.exchange(uri,
                HttpMethod.PUT,
                request,
                String.class);

        LOG.info("Topics requested! " + response);
    }


}


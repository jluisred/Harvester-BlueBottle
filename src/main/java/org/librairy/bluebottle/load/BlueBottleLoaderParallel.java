package org.librairy.bluebottle.load;

import org.librairy.bluebottle.conf.Conf;
import org.librairy.model.domain.relations.Relation;
import org.librairy.model.domain.resources.Document;
import org.librairy.model.domain.resources.Item;
import org.librairy.model.domain.resources.Part;
import org.librairy.model.domain.resources.Resource;
import org.librairy.model.modules.EventBus;
import org.librairy.model.utils.TimeUtils;
import org.librairy.storage.UDM;
import org.librairy.storage.executor.ParallelExecutor;
import org.librairy.storage.generator.URIGenerator;
import org.library.bluebottle.datastructure.BBBResource;
import org.library.bluebottle.datastructure.BBChapter;
import org.library.bluebottle.datastructure.BBResourceUnit;
import org.library.bluebottle.datastructure.DataChapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

@Component
public class BlueBottleLoaderParallel {
    private static Logger LOG = LoggerFactory.getLogger(BlueBottleLoaderParallel.class);
    private static  Conf conf = new Conf();

    @Autowired
	UDM udm;

    @Autowired
    URIGenerator uriGenerator;
    
    @Autowired
    EventBus bus;
    

    
    @Autowired
    SaveResources sc;
    

    @PostConstruct
    public void setup(){
        LOG.info("Loader UP!");
    }


    public void loadBooks(){
    	//Tracking variables
        int numConsistentResources = 0;
        int numResources = 0;
        List<String> booksNoData = new ArrayList<String>();
        List<String> booksNoComponents = new ArrayList<String>();
        List<String> booksComponents = new ArrayList<String>();

        List<BBBResource> books = null;
        
        //Initialize Domain and Source
        sc.setupDomain();

        
        //get list of books
    	  RestTemplate restTemplate = new RestTemplate();

    	  //Build Headers
          HttpHeaders headers = new HttpHeaders();
          headers.add("x-api-key", conf.getApikey());
          headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
          
          //Build Request
          HttpEntity<String> request = new HttpEntity<String>(headers);
          
          
          //getNumPages
          int numPages = getNumPages(restTemplate, request);
          numPages = 1;

          ParallelExecutor pe = new ParallelExecutor();
          
          for(int p = 1; p<=numPages; p++){
        	  
              // Launch Get Page           
              books = getBooksInPage(restTemplate, request, p);
              //LOG.info("NumResources: " + books.size());
             
              //get Chapters
              for(BBBResource book: books){
            	  numResources++;

            	  
            	  pe.execute(() -> processBook(restTemplate, request, book, booksNoData, booksNoComponents, booksComponents, numConsistentResources));


              }
              
              
              //chapterIDs.stream().forEach(chapterID -> LOG.info("     Chapter: " + chapterID));
              
              

              
          }

          //REPORT ON COLLECTION
        	LOG.info("==============================================");
          	LOG.info("TOTAL NUMBER OF RESOURCES: " + numResources);
  	  		LOG.info("CONSISTENT RESOURCES: " + numConsistentResources);
  	  		LOG.info("Books with no DATA: " + booksNoData.size());
  	  		LOG.info("Books with no Components: " + booksNoComponents.size());
  	  		LOG.info("Books fully completed: " + booksComponents.size());
  	  		if (!booksNoData.isEmpty()) LOG.info("Name of book with no data: " + booksNoData.get(0));
  	  		if (!booksNoComponents.isEmpty()) LOG.info("Name of book with data and no component: " + booksNoComponents.get(0));
  	  		if (!booksComponents.isEmpty()) LOG.info("Name of book with all neccesary info: " + booksComponents.get(0));
        	LOG.info("==============================================");

        	
          
      // sc.loadResourcesInLibrary (booksCatalog);
       //sc.deleteAll();
 
        //udm.save(resource)e(books.get(0));
        



    }


	private void processBook(RestTemplate restTemplate, HttpEntity<String> request, BBBResource book, List<String> booksNoData, List<String> booksNoComponents, List<String> booksComponents, int numConsistentResources) {
  	  LOG.info("Resource: " + book.getSeoBook());
  	  //Retrieving Chapters
      BBResourceUnit resource = null;
  	  resource = retrieveChapters(restTemplate, request, book.getSeoBook());
  	  
  	  
 		  if (resource.getData() != null) { //Checking books with no Data
 			  if(resource.getData().getComponents() != null) {//Checking books with no Components
     			  if (!resource.getData().getComponents().isEmpty()){ //Checking there is at least one chapter
     				numConsistentResources++;
     				booksComponents.add(book.getSeoBook());
     			  }
     			  else booksNoComponents.add(book.getSeoBook());
     			  
     			 //Generate book
     			   Document document = saveBookLibrairy(book);
     			  
     			  //Retrieving text 
     			  for (org.library.bluebottle.datastructure.Component c : resource.getData().getComponents()){
     	  	  			LOG.info("       Chapter: " + c.getId());
     	  	  			String text = retrieveTextChapter(restTemplate, request, book.getSeoBook(), c.getId());
     	  	  			book.getChapters().add(new DataChapter(c.getId(),text ));

     			  }
     			  
     			  savePartItemLibrairy(book, document);

     			  
 			  }
 			  else {
 				booksNoComponents.add(book.getSeoBook());
 			  }
 		  }
 		  else{
 			booksNoData.add(book.getSeoBook());
  	  }
		
	}


	private void savePartItemLibrairy(BBBResource book, Document document) {
		//GenerateParts
        ArrayList<Part> parts = new ArrayList<Part>();
        String textItem ="";
        for (DataChapter chapter: book.getChapters()){
            Part part = Resource.newPart(chapter.getText());
            part.setUri(uriGenerator.from(Resource.Type.PART, chapter.getId()));
            udm.save(part);
            parts.add(part);
            textItem = textItem + " " + chapter.getText(); //Concatenate text
        }
        
        //Create Item

        
        Item item = Resource.newItem(textItem);
        item.setFormat("text");

        //item.setUri(uriGenerator.from(Resource.Type.ITEM, UUID.randomUUID().toString()));
        item.setTokens("");
        udm.save(item);
        LOG.info("New (textual) Item: " + item.getUri() + " from Document: " + document.getUri());

        //Relation parts / item
        for (Part p :parts){
            udm.save(Relation.newDescribes(p.getUri(),item.getUri()));
        }
        //relation item / document
        udm.save(Relation.newBundles(document.getUri(),item.getUri()));
	}


	private Document saveBookLibrairy(BBBResource book) {
		  Document document = Resource.newDocument(book.getName());
          document.setUri(uriGenerator.from(Resource.Type.DOCUMENT, book.getHash()));
          
          document.setPublishedOn(book.getEditionYear());

          String authors="";
          for (String author : book.getAuthors())
          		authors = authors+ ", "+author;
          if (authors.length()>0) authors=authors.substring(0, authors.length()-1);
      
          document.setAuthoredBy(authors);

          
          // -> retrievedOn
          document.setRetrievedOn(TimeUtils.asISO());

          // -> description
          document.setDescription(book.getSummary());


          udm.save(document);
          LOG.info("New document: " + document.getUri());


          // Relate it to Source
          udm.save(Relation.newProvides(uriGenerator.from(Resource.Type.SOURCE, "default"),document.getUri()));
          // Relate it to Domain
          udm.save(Relation.newContains(uriGenerator.from(Resource.Type.DOMAIN, "default"),document.getUri()));
          // Relate it to Document
          
          return document;
		
	}


	private static String retrieveTextChapter(RestTemplate restTemplate, HttpEntity<String> request, String seoBook, String id) {
		
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(conf.getEndpointURL()+"/resources/"+seoBook+"/components/"+id);

        ResponseEntity<BBChapter> response = restTemplate.exchange( builder.build().encode().toUri(),
                HttpMethod.GET,
                request,
                BBChapter.class);
        
		return response.getBody().getData().getText();
	}


	private static BBResourceUnit retrieveChapters(RestTemplate restTemplate, HttpEntity<String> request,
			String seoBook) {

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(conf.getEndpointURL()+"/resources/"+seoBook+"/reader");
                  
        
        ResponseEntity<BBResourceUnit> response = restTemplate.exchange( builder.build().encode().toUri(),
                HttpMethod.GET,
                request,
                BBResourceUnit.class);
        

		
        return response.getBody();
	}


	private static List<BBBResource> getBooksInPage(RestTemplate restTemplate, HttpEntity<String> request, int p) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(conf.getEndpointURL()+"/resources")
      	        .queryParam("type", "epub").queryParam("lang", "en").queryParam("nrows", "100").queryParam("page", p);
          
          
          ResponseEntity<BBBResource[]> response = restTemplate.exchange( builder.build().encode().toUri(),
                  HttpMethod.GET,
                  request,
                  BBBResource[].class);
         
         
		return new ArrayList<BBBResource>(Arrays.asList(response.getBody()));
	}


	private static int getNumPages(RestTemplate restTemplate, HttpEntity<String> request) {
		

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(conf.getEndpointURL()+"/resources")
    	        .queryParam("type", "epub").queryParam("lang", "en").queryParam("nrows", "100");

        // Launch Get           
        ResponseEntity<BBBResource[]> response = restTemplate.exchange( builder.build().encode().toUri(),
                HttpMethod.GET,
                request,
                BBBResource[].class);
        
        LOG.info("Response: " + response.getStatusCode());
        LOG.info("x-limit: " + response.getHeaders().getFirst("x-limit"));
        LOG.info("x-page: " + response.getHeaders().getFirst("x-page"));
        LOG.info("x-total: " + response.getHeaders().getFirst("x-total"));
        LOG.info("x-totalPages: " + response.getHeaders().getFirst("x-totalPages"));
        
        
        
		return Integer.parseInt(response.getHeaders().getFirst("x-totalPages"));
	}

 
	
	
}


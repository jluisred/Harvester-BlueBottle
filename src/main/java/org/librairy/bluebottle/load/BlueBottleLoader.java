package org.librairy.bluebottle.load;

import org.librairy.bluebottle.conf.Conf;
import org.librairy.model.domain.relations.Relation;
import org.librairy.model.domain.resources.Document;
import org.librairy.model.domain.resources.Resource;
import org.librairy.model.modules.EventBus;
import org.librairy.model.utils.TimeUtils;
import org.librairy.storage.UDM;
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
public class BlueBottleLoader {
    private static Logger LOG = LoggerFactory.getLogger(BlueBottleLoader.class);
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
        List<BBBResource> booksCatalog = new ArrayList<BBBResource>();

        List<BBBResource> books = null;
        
        
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
          
          for(int p = 1; p<=numPages; p++){
        	  
              // Launch Get Page           
              books = getBooksInPage(restTemplate, request, p);
              //LOG.info("NumResources: " + books.size());
             
              //get Chapters
              BBResourceUnit resource = null;
              for(BBBResource book: books){
            	  numResources++;
            	  LOG.info("Resource: " + book.getSeoBook() +"    "+numResources);
            	  //Retrieving Chapters
            	  resource = retrieveChapters(restTemplate, request, book.getSeoBook());
            	  
            	  
           		  if (resource.getData() != null) { //Checking books with no Data
           			  if(resource.getData().getComponents() != null) {//Checking books with no Components
               			  if (!resource.getData().getComponents().isEmpty()){ //Checking there is at least one chapter
               				numConsistentResources++;
               				booksComponents.add(book.getSeoBook());
               			  }
               			  else booksNoComponents.add(book.getSeoBook());
               			  
               			  //Retrieving text 
               			  for (org.library.bluebottle.datastructure.Component c : resource.getData().getComponents()){
               	  	  			LOG.info("       Chapter: " + c.getId());
               	  	  			String text = retrieveTextChapter(restTemplate, request, book.getSeoBook(), c.getId());
               	  	  			book.getChapters().add(new DataChapter(c.getId(),text ));

               			  }
         	  	  		  booksCatalog.add(book);

           			  }
           			  else {
           				booksNoComponents.add(book.getSeoBook());
           			  }
           		  }
           		  else{
           			booksNoData.add(book.getSeoBook());
            	  }

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

        	
          
       sc.setupDomain();
       sc.loadResourcesInLibrary (booksCatalog);
       //sc.deleteAll();
 
        //udm.save(resource)e(books.get(0));
        



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


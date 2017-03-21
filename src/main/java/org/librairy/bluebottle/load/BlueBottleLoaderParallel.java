package org.librairy.bluebottle.load;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.librairy.bluebottle.cache.CacheBB;
import org.librairy.bluebottle.conf.Conf;
import org.librairy.bluebottle.datastructure.BBBResource;
import org.librairy.bluebottle.datastructure.BBChapter;
import org.librairy.bluebottle.datastructure.BBResourceUnit;
import org.librairy.bluebottle.datastructure.DataChapter;
import org.librairy.bluebottle.datastructure.LibrairyDocument;
import org.librairy.bluebottle.datastructure.LibrairyDomain;
import org.librairy.storage.executor.ParallelExecutor;
import org.neo4j.ogm.json.JSONException;
import org.neo4j.ogm.json.JSONObject;
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

    //@Autowired
	//UDM udm;

   // @Autowired
    //URIGenerator uriGenerator;
    
    //@Autowired
    //EventBus bus;
    


    //@PostConstruct
    public void setup(){
        LOG.info("Loader UP!");
    }


    public void loadBooks(){
    	//Tracking variables
        int numConsistentResources = 0;
        int numResources = 0;
        
        List<BBBResource> booksEmpty = new ArrayList<BBBResource>();
        List<String> booksNoData = new ArrayList<String>();
        List<String> booksNoComponents = new ArrayList<String>();
        List<String> booksComponents = new ArrayList<String>();

        List<BBBResource> books = null;
        
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
          //int numPages = 83;//HACK
          if (Conf.getLoadGT()) numPages = 1;
          else{
              if (Conf.getForcePages() >0) numPages = Conf.getForcePages();
              else numPages = getNumPages(restTemplate, request);
          }
          System.out.println("NUMBER OF PAGES: " + numPages);
          ParallelExecutor pe = new ParallelExecutor();
          
          int minPage = 1;
          if (Conf.getStartPage()>0) minPage = Conf.getStartPage();
          
          
          for(int p = minPage; p<=numPages; p++){
        	  
              // Launch Get Page   
        	  CacheBB cache = new CacheBB();
        	  
        	  if (!Conf.isCacheEnabled()){
                  books = getBooksInPage(restTemplate, request, p);
        	  }
        	  else {
            	  if (!cache.containsPage(p)){
                      books = getBooksInPage(restTemplate, request, p);
                      cache.savePage(books, p);
            	  }
            	  else books = cache.getPage(p);
        	  }
        	  

        if(Conf.getLoadGT()){	  
              //LOG.info("NumResources: " + books.size());
        	  //Hack for adding 5 books in gold standard
        	  BBBResource book1 = new  BBBResource();
        	  book1.setSeoBook("defying-doom");
        	  book1.setName("DEFYING DOOM");
        	  book1.setHash("RB6qb-VsRYsOZ");
        	  System.out.println(book1.getHash());
        	  BBBResource book2 = new  BBBResource();
        	  book2.setSeoBook("outstanding-business-english");
        	  book2.setName("Outstanding business English");
        	  book2.setHash("82WxBdWh6U4");
        	  BBBResource book3 = new  BBBResource();
        	  book3.setSeoBook("the-art-of-shopping");
        	  book3.setName("The Art of Shopping");
        	  book3.setHash("M57xA96F4Ix");
        	  BBBResource book4 = new  BBBResource();
        	  book4.setSeoBook("the-content-revolution");
        	  book4.setName("The content revolution");
        	  book4.setHash("rV_NJKNSdiL");
        	  BBBResource book5 = new  BBBResource();
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
              for(BBBResource book: books){
            	  numResources++;

            	
            	  if (Conf.isParallelProcessing()){
            		  int page = p;
                	  pe.execute(() -> processBook(restTemplate, request, book, domain, booksEmpty, booksNoData, booksNoComponents, booksComponents, numConsistentResources, page));
            	  }
            	  else
            		  processBook(restTemplate, request, book, domain, booksEmpty, booksNoData, booksNoComponents, booksComponents, numConsistentResources, p);


              }
              
              
              //chapterIDs.stream().forEach(chapterID -> LOG.info("     Chapter: " + chapterID));
              
              

              
          }

          //REPORT ON COLLECTION
        	LOG.info("==============================================");
          	LOG.info("TOTAL NUMBER OF RESOURCES: " + numResources);
  	  		LOG.info("CONSISTENT RESOURCES: " + numConsistentResources);
  	  		LOG.info("Books that are empty: " + booksEmpty.size());
  	  		for (BBBResource bookempty : booksEmpty){
  	  	  		LOG.info("      --> " +bookempty.getName() + ", id: " + bookempty.getHash());
  	  		}
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


	private LibrairyDomain setupDomain() {
		        // Check if exists 'default' source
		    	//udm.find(Resource.Type.SOURCE).all().forEach(res -> LOG.info((("Source: " + udm.read(Resource.Type.SOURCE).byUri(res.getUri()).get().asSource()))));
		    	//udm.find(Resource.Type.SOURCE).all().forEach(res -> LOG.info((("Source: " + res.getUri()))));

		    /*OLD DOMAIN CREATION LOGIC
		        if (udm.find(Resource.Type.SOURCE).all().isEmpty()){
		            Source source = Resource.newSource("default");
		            source.setUri(uriGenerator.from(Resource.Type.SOURCE, "default"));
		            source.setDescription("default");
		            LOG.info("Creating default source: " + source);
		            udm.save(source);
		            createDefaultDomain(source);
		        }
		        
		        */
		    
		    	
			    JSONObject domainJson = new JSONObject();
			    try {
					domainJson.put("name", Conf.getRunConf());

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			    

		    	
		    	 RestTemplate restTemplate = new RestTemplate();

		   	    //Build Headers
		         HttpHeaders headers = new HttpHeaders();
		         headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		         headers.setContentType(MediaType.APPLICATION_JSON);


		         //Build Request
		    	
		         HttpEntity<String> request = new HttpEntity<String>(domainJson.toString(), headers);
		         UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(Conf.getEndpointLibrairy()+"/domains/"+ Conf.getRunConf());
		         
		        ResponseEntity<String> response = restTemplate.exchange( builder.build().encode().toUri(),
		                HttpMethod.POST,
		                request,
		                String.class);
		        
		        
		        Gson gson = new Gson();
		        LibrairyDomain domain = gson.fromJson(response.getBody(), LibrairyDomain.class);
		        System.out.println("Domain id "+ domain.getId());

//    "name": "tokenizer.mode",   "value": "lemma"
		        
		        setParametersDomain(domain, "tokenizer.mode", Conf.getTokenizerMode());
		        setParametersDomain(domain, "lda.delay", Integer.toString(Conf.getLdaDelay()));
		        setParametersDomain(domain, "w2v.delay", Integer.toString(Conf.getW2vDelay()));

		        return domain;


		 
	}


	private void setParametersDomain(LibrairyDomain domain, String name, String value) {
		
		
		
	    JSONObject domainJson = new JSONObject();
	    try {
			domainJson.put("name", name);
			domainJson.put("value", value);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	    

    	
    	 RestTemplate restTemplate = new RestTemplate();

   	    //Build Headers
         HttpHeaders headers = new HttpHeaders();
         headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
         headers.setContentType(MediaType.APPLICATION_JSON);


         //Build Request
    	
         HttpEntity<String> request = new HttpEntity<String>(domainJson.toString(), headers);
         UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(Conf.getEndpointLibrairy()+"domains/"+domain.getId()+"/parameters");
         //System.out.println(builder.build().encode().toUri());
        restTemplate.exchange( builder.build().encode().toUri(),
                HttpMethod.POST,
                request,
                String.class);
        
		
	}


	private void processBook(RestTemplate restTemplate, HttpEntity<String> request, BBBResource book, String domain, List<BBBResource> booksEmpty, List<String> booksNoData, List<String> booksNoComponents, List<String> booksComponents, int numConsistentResources, int page) {
  	  LOG.info("Resource: " + book.getSeoBook());
  	  //Retrieving Chapters
      BBResourceUnit resource = null;
      
      CacheBB cache = new CacheBB();
	  if (!Conf.isCacheEnabled()){
	  	  resource = retrieveChapters(restTemplate, request, book.getSeoBook());
	  }
	  else {
    	  if (!cache.containsResource(book.getSeoBook())){
    	  	  resource = retrieveChapters(restTemplate, request, book.getSeoBook());
              cache.saveResource(resource, book.getSeoBook());
    	  }
    	  else resource = cache.getResource(book.getSeoBook());
	  }
	  
	  int cont = 0;
	  while (resource == null && cont<20){
		  	resource = retrieveChapters(restTemplate, request, book.getSeoBook());
		  	if (resource != null) cache.saveResource(resource, book.getSeoBook());
             cont++;
	  }
	  
      
  	  if (resource == null){
  		  booksEmpty.add(book);
  		  return;
  	  }
  	  
  	  
  	  System.out.println("Resource: " + book.getSeoBook());

	  	//Clean 
	 	 Escaper escaper = Escapers.builder()
	             .addEscape('\'',"_")
	             .addEscape('('," ")
	             .addEscape(')'," ")
	             .addEscape('['," ")
	             .addEscape(']'," ")
	             .addEscape('“',"\"")
	             .addEscape('"'," ")
	             .addEscape('…'," ")
	             .addEscape('‘'," ")
	             .addEscape('\n'," ")
	             .addEscape('‘'," ")
	             .build();
	 	 ;
	 	
 	 
 		  if (resource.getData() != null) { //Checking books with no Data

 			  if(resource.getData().getComponents() != null) {//Checking books with no Components

     			  if (!resource.getData().getComponents().isEmpty()){ //Checking there is at least one chapter
     				numConsistentResources++;
     				booksComponents.add(book.getSeoBook());
     			  }
     			  else booksNoComponents.add(book.getSeoBook());

     			 //Generate book
     			  //LibrairyDocument document = saveBookLibrairy(book, "");
     			  
     			  
  		          String textBook = "";
  		          
     			  //Retrieving text 
     			  for (org.librairy.bluebottle.datastructure.Component c : resource.getData().getComponents()){
     	  	  			//LOG.info("       Chapter: " + c.getId());
     				  String text = "";
     				  
     				  
     				  if (!Conf.isCacheEnabled()){
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
     				  }
     				  else {
     			    	  if (!cache.containsTextChapter(c.getId())){
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
     			    	  }
     			    	  else {
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
     				  while (text == null && contChap<20){
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
	       	  	  			if (text != null) cache.saveTextChapter(text, c.getId());     					  	if (resource != null) cache.saveResource(resource, book.getSeoBook());
     			             contChap++;
     				  }
     				  
     	  	  			book.getChapters().add(new DataChapter(c.getId(),text ));
     	  	  			textBook = textBook + " " + text;
     			  }
     			  //LibrairyDocument document = saveBookLibrairy(book, textBook);
 				  System.out.println("Book id "+ book.getHash() + " from page "+ page);

     			  if (saveBookLibrairy(book, textBook)){


     				  savePartItemLibrairy(book, page);
     			  
     			  
     			 
     				  //Add book to Domain
     				  addDocumentToDomain(domain, book.getHash());
     			  }
     			  
 			  }
 			  else {
 				booksNoComponents.add(book.getSeoBook());
 			  }
 		  }
 		  else{
 			booksNoData.add(book.getSeoBook());
  	  }
		
	}


	private void addDocumentToDomain(String domain, String documentID) {

	    JSONObject domainJson = new JSONObject();



    	 RestTemplate restTemplate = new RestTemplate();

   	  //Build Headers
         HttpHeaders headers = new HttpHeaders();
         headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
         headers.setContentType(MediaType.APPLICATION_JSON);

         //Build Request
    	
         HttpEntity<String> request = new HttpEntity<String>(domainJson.toString(), headers);
         UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(Conf.getEndpointLibrairy()+"domains/"+domain+"/documents/"+documentID);
         
         try{
        ResponseEntity<String> response = restTemplate.exchange( builder.build().encode().toUri(),
                HttpMethod.POST,
                request,
                String.class);
        
        
		} 
		catch (HttpClientErrorException e) {
			// TODO Auto-generated catch block
	        if (e.getStatusCode() == HttpStatus.CONFLICT) 
	        	System.out.println("Cant associate " + documentID + " to domain.");
	        else e.printStackTrace();
			
		}
		catch (RestClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private void savePartItemLibrairy(BBBResource book, int page) {
		//GenerateParts
        //ArrayList<Part> parts = new ArrayList<Part>();
        //String textItem ="";
        //for (DataChapter chapter: book.getChapters()){
        //    Part part = Resource.newPart(chapter.getText());
        //    part.setUri(uriGenerator.from(Resource.Type.PART, chapter.getId()));
        //    udm.save(part);
        //    parts.add(part);
        //    textItem = textItem + " " + chapter.getText(); //Concatenate text
        //}
        
        //Create Item

        
        //Item item = Resource.newItem(textItem);
        //item.setFormat("text");

        //item.setUri(uriGenerator.from(Resource.Type.ITEM, URIGenerator.retrieveId(document.getUri())));
        //udm.save(item);
        //LOG.info("New (textual) Item: " + item.getUri() + " from Document: " + document.getUri());

        //Relation parts / item
        //for (Part p :parts){
        //    udm.save(Relation.newDescribes(p.getUri(),item.getUri()));
        //    udm.save(Relation.newContains(uriGenerator.from(Resource.Type.DOMAIN, "default"),p.getUri()));

        //}
        //relation item / document
        //udm.save(Relation.newBundles(document.getUri(),item.getUri()));
        //udm.save(Relation.newContains(uriGenerator.from(Resource.Type.DOMAIN, "default"),item.getUri()));

		
		
        for (DataChapter chapter: book.getChapters()){

        	

        	
    	    JSONObject domainJson = new JSONObject();
    	    try {
    			domainJson.put("language", "EN");
    			domainJson.put("content", chapter.getText());
    			//domainJson.put("id", chapter.getId());
    			//domainJson.put("uri", "http://librairy.org/parts/"+chapter.getId());


    		} catch (JSONException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} 
        	//System.out.println(domainJson.toString());


        	 RestTemplate restTemplate = new RestTemplate();

       	  //Build Headers
             HttpHeaders headers = new HttpHeaders();
             headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
             headers.setContentType(MediaType.APPLICATION_JSON);

             //Build Request
             HttpEntity<String> request = new HttpEntity<String>(domainJson.toString(), headers);
             UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(Conf.getEndpointLibrairy()+"parts/"+chapter.getId());
             
			try {
	        	//System.out.println("chapter CONTENT: " + chapter.getText());

				restTemplate.exchange( builder.build().encode().toUri(),
				        HttpMethod.POST,
				        request,
				        String.class);
	        	System.out.println("Created Part with ID " + chapter.getId() + " from page " + page);

			} 
			catch (HttpClientErrorException e) {
				// TODO Auto-generated catch block
		        if (e.getStatusCode() == HttpStatus.CONFLICT) 
		        	System.out.println("Conflict with chapter " + book.getHash());
		        else e.printStackTrace();
				
			}
			catch (RestClientException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
	       	  //Build Headers

            //Build Request
            HttpEntity<String> requestLinkPartDocument = new HttpEntity<String>(headers);
            UriComponentsBuilder builderLinkPartDocument = UriComponentsBuilder.fromHttpUrl(Conf.getEndpointLibrairy()+"documents/"+book.getHash()+"/parts/"+chapter.getId());
            
			try {
	        	//System.out.println("chapter CONTENT: " + chapter.getText());
				System.out.println(builderLinkPartDocument.build().encode().toUri());
				restTemplate.exchange( builderLinkPartDocument.build().encode().toUri(),
				        HttpMethod.POST,
				        requestLinkPartDocument,
				        String.class);
	        	System.out.println("Associating " + book.getHash() + " with " + chapter.getId() );

	        	//System.out.println("Created chapter with ID " + chapter.getId());

			} 
			catch (HttpClientErrorException e) {
				// TODO Auto-generated catch block
		        if (e.getStatusCode() == HttpStatus.CONFLICT) 
		        	System.out.println("Conflict associating resource " + book.getHash() + " with "+chapter.getId());
		        else e.printStackTrace();
				
			}
			catch (RestClientException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


        }
		
		
	}


	private boolean saveBookLibrairy(BBBResource book, String content) {
		
		
		  //Document document = Resource.newDocument(book.getName());
          //document.setUri(uriGenerator.from(Resource.Type.DOCUMENT, book.getHash()));
          
          //document.setPublishedOn(book.getEditionYear());

          //String authors="";
          //for (String author : book.getAuthors())
          	//	authors = authors+ ", "+author;
          //if (authors.length()>0) authors=authors.substring(0, authors.length()-1);
      
          //document.setAuthoredBy(authors);

          
          // -> retrievedOn
          //document.setRetrievedOn(TimeUtils.asISO());

          // -> description
          //document.setDescription(book.getSummary());


          //udm.save(document);
          //LOG.info("New document: " + document.getUri());


          // Relate it to Source
          //udm.save(Relation.newProvides(uriGenerator.from(Resource.Type.SOURCE, "default"),document.getUri()));
          // Relate it to Domain
          //udm.save(Relation.newContains(uriGenerator.from(Resource.Type.DOMAIN, "default"),document.getUri()));
          // Relate it to Document
          
		
		
	  	//Clean 
	 	 Escaper escaper = Escapers.builder()
	             .addEscape('\'',"_")
	             .addEscape('('," ")
	             .addEscape(')'," ")
	             .addEscape('['," ")
	             .addEscape(']'," ")
	             .addEscape('“',"\"")
	             .addEscape('"'," ")
	             .addEscape('…'," ")
	             .addEscape('‘'," ")
	             .addEscape('\n'," ")
	             .addEscape('‘'," ")
	             .addEscape('´'," ")
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
	  			
    	
	    JSONObject domainJson = new JSONObject();
	    try {
			domainJson.put("name", nameEncode);
			domainJson.put("language", "EN");
			domainJson.put("content", content );
			//domainJson.put("uri", "http://librairy.org/items/"+book.getHash());


		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

	    


    	
    	 RestTemplate restTemplate = new RestTemplate();

   	  //Build Headers
         HttpHeaders headers = new HttpHeaders();
         headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
         headers.setContentType(MediaType.APPLICATION_JSON);

         //Build Request
    	
         HttpEntity<String> request = new HttpEntity<String>(domainJson.toString(), headers);
         UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(Conf.getEndpointLibrairy()+"documents/"+book.getHash());
         

         System.out.println(builder.build().encode().toUri());

        ResponseEntity<String> response = null;
		try {
			response = restTemplate.exchange( builder.build().encode().toUri(),
			        HttpMethod.POST,
			        request,
			        String.class);
		} 
		catch (HttpClientErrorException e) {
			// TODO Auto-generated catch block
	        if (e.getStatusCode() == HttpStatus.CONFLICT) 
	        	System.out.println("Conflict with resource " + book.getHash());
	        else e.printStackTrace();
	        return false;
			
		}
			catch (RestClientException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
			
		}
        //System.out.println(response.getBody());

       // Gson gson = new Gson();
       // LibrairyDocument document = gson.fromJson(response.getBody(), LibrairyDocument.class);
        
		
        // return document;
		return true;
	}


	private static String retrieveTextChapter(RestTemplate restTemplate, HttpEntity<String> request, String seoBook, String id) {
		
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(Conf.getEndpointURL()+"/resources/"+seoBook+"/components/"+id);
        //System.out.println( builder.build().encode().toUri());
        ResponseEntity<BBChapter> response = restTemplate.exchange( builder.build().encode().toUri(),
                HttpMethod.GET,
                request,
                BBChapter.class);
        
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() == null) return "";
		return response.getBody().getData().getText();
	}


	private static BBResourceUnit retrieveChapters(RestTemplate restTemplate, HttpEntity<String> request,
			String seoBook) {

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(Conf.getEndpointURL()+"/resources/"+seoBook+"/reader");
                  
        
        ResponseEntity<BBResourceUnit> response = restTemplate.exchange( builder.build().encode().toUri(),
                HttpMethod.GET,
                request,
                BBResourceUnit.class);
        

		
        return response.getBody();
	}


	private static List<BBBResource> getBooksInPage(RestTemplate restTemplate, HttpEntity<String> request, int p) {
   
        int numRetry = 0;
        boolean thereIsAnswer = false;  
        ResponseEntity<BBBResource[]> response = null;

		
        
        while (!thereIsAnswer && numRetry < 20 ){
        	
      	  RestTemplate restTemplate2 = new RestTemplate();

      	  //Build Headers
            HttpHeaders headers = new HttpHeaders();
            headers.add("x-api-key", Conf.getApikey());
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
            headers.setContentType(MediaType.APPLICATION_JSON);

            //Build Request
            HttpEntity<BBBResource[]>  request2 = new HttpEntity<BBBResource[]>(headers);
            
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(Conf.getEndpointURL()+"/resources")
          	        .queryParam("type", "epub").queryParam("lang", "eng").queryParam("nrows", "100").queryParam("page", p);
              
            
            
          response = restTemplate2.exchange( builder.build().encode().toUri(),
                  HttpMethod.GET,
                  request2,
                  BBBResource[].class);
         if(response.getBody() != null) thereIsAnswer = true;
         else{
        	 try {
				Thread.sleep(2000);
				System.out.println("Retrying: " + builder.build().encode().toUri());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
         }

         numRetry++;
        }

		return new ArrayList<BBBResource>(Arrays.asList(response.getBody()));
	}


	private static int getNumPages(RestTemplate restTemplate, HttpEntity<String> request) {
		

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(Conf.getEndpointURL()+"/resources")
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


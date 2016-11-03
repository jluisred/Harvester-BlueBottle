package org.librairy.bluebottle.load;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.librairy.bluebottle.conf.Conf;
import org.librairy.model.domain.relations.Relation;
import org.librairy.model.domain.resources.Document;
import org.librairy.model.domain.resources.Domain;
import org.librairy.model.domain.resources.Item;
import org.librairy.model.domain.resources.Part;
import org.librairy.model.domain.resources.Resource;
import org.librairy.model.domain.resources.Source;
import org.librairy.model.utils.TimeUtils;
import org.librairy.storage.UDM;
import org.librairy.storage.generator.URIGenerator;
import org.library.bluebottle.datastructure.BBBResource;
import org.library.bluebottle.datastructure.DataChapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SaveResources {
    private static Logger LOG = LoggerFactory.getLogger(SaveResources.class);

    
    @Autowired
	UDM udm;

    @Autowired
    URIGenerator uriGenerator;
	   
    
    public void setupDomain(){
        // Check if exists 'default' source
    	//udm.find(Resource.Type.SOURCE).all().forEach(res -> LOG.info((("Source: " + udm.read(Resource.Type.SOURCE).byUri(res.getUri()).get().asSource()))));
    	//udm.find(Resource.Type.SOURCE).all().forEach(res -> LOG.info((("Source: " + res.getUri()))));

    	
        if (udm.find(Resource.Type.SOURCE).all().isEmpty()){
            Source source = Resource.newSource("default");
            source.setUri(uriGenerator.from(Resource.Type.SOURCE, "default"));
            source.setDescription("default");
            LOG.info("Creating default source: " + source);
            udm.save(source);
            createDefaultDomain(source);
        }
        
        
        

    }
    public void deleteAll(){
    	Arrays.stream(Resource.Type.values()).forEach(type -> udm.delete(type).all());
        Arrays.stream(Relation.Type.values()).forEach(type -> udm.delete(type).all());
    }
    
    private Domain createDefaultDomain(Source source){
        LOG.debug("creating a new domain associated to source: " + source.getUri());
        Domain domain = Resource.newDomain(source.getName());

        String domainUri    = uriGenerator.basedOnContent(Resource.Type.DOMAIN,source.getName());
        String sourceId     = URIGenerator.retrieveId(source.getUri());
        if (sourceId.equalsIgnoreCase("default")){
            domainUri = uriGenerator.from(Resource.Type.DOMAIN,"default");
        }
        domain.setUri(domainUri);
        domain.setDescription("attached to source: " + source.getUri());
        udm.save(domain);
        LOG.info("A new Domain has been created: " + domain.getUri() + " attached to Source: " + source.getUri());
        udm.save(Relation.newComposes(source.getUri(),domain.getUri()));
        return domain;
    }
	
    void loadResourcesInLibrary (List<BBBResource> booksCatalog){

	    	for (BBBResource book: booksCatalog){
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
			
		}
}

package io.swagger.api;

import io.swagger.model.Error;
import io.swagger.model.Topic;
import io.swagger.model.Word;
import io.swagger.annotations.*;

import org.librairy.model.domain.resources.Resource;
import org.librairy.model.modules.EventBus;
import org.librairy.storage.UDM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;


@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-11-02T11:29:00.407Z")

@Controller
@Component
public class DocumentApiController implements DocumentApi {
    private static Logger LOG = LoggerFactory.getLogger(DocumentApiController.class);

	
    @Autowired
	UDM udm;
    
    @Autowired
    EventBus bus;
    
    
    public ResponseEntity<List<Topic>> documentIdTopicDistributionGet(
@ApiParam(value = "ID of the document.",required=true ) @PathVariable("id") String id


) {
    	
    //ResponseEntity<List<Topic>> response = new ResponseEntity<List<Topic>>(HttpStatus.OK);
        // do some magic!
    	List<Topic> topics = new ArrayList<Topic>();
        LOG.info("Number of Topics available: " + udm.find(Resource.Type.TOPIC).all().size());
    	for (Resource topic : udm.find(Resource.Type.TOPIC).all()){
    		Topic t = new Topic();
    		Word w = new Word();
    		w.setSurface(topic.getCreationTime());
    		w.setScore(5.0);
    		ArrayList<Word> listwords = new ArrayList<Word>();
    		listwords.add(w);
    		t.setTopic(listwords);
    		t.setWeight(10.00);

    		topics.add(t);
    	}
        return ResponseEntity.ok(topics);
    }

}

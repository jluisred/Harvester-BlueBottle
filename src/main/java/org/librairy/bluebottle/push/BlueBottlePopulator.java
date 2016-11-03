package org.librairy.bluebottle.push;



import org.librairy.bluebottle.load.BlueBottleLoader;
import org.librairy.eventbus.RelationEventHandler;
import org.librairy.model.domain.relations.Relation;
import org.librairy.model.domain.resources.Resource;
import org.librairy.storage.UDM;
import org.librairy.storage.generator.URIGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class BlueBottlePopulator implements RelationEventHandler{
    private static Logger LOG = LoggerFactory.getLogger(BlueBottlePopulator.class);

    @Autowired
    UDM udm;

    @Autowired
    URIGenerator uriGenerator;

    @PostConstruct
    public void setup(){
       LOG.info("Populator UP!");
       udm.listenFor(Relation.Type.DEALS_WITH_FROM_DOCUMENT, Relation.State.CREATED, "BlueBottlePopulator", this);
    }

    @Override
    public boolean handle(Relation relation) {

        String docUri = relation.getStartUri();
        String topicUri = relation.getEndUri();

        Double weight = relation.getWeight();


        List<Relation> rels = udm.find(Relation.Type.MENTIONS_FROM_TOPIC).from(Resource.Type.TOPIC, topicUri);


        rels.sort((o1, o2) -> -o1.getWeight().compareTo(o2.getWeight()));


        List<Relation> tags = rels.stream().limit(Double.valueOf(weight * 10).intValue()).collect(Collectors.toList());


        List<String> words = tags.stream()
                .map(rel -> udm.read(Resource.Type.WORD).byUri(rel.getEndUri()))
                .map(res -> res.get().asWord().getContent())
                .collect(Collectors.toList());


        String hashDoc = URIGenerator.retrieveId(docUri);




        return false;
    }
}
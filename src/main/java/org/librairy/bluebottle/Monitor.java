/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.bluebottle;

import org.librairy.eventbus.ResourceEventHandler;
import org.librairy.model.domain.resources.Resource;
import org.librairy.storage.UDM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class Monitor implements ResourceEventHandler{

    private static final Logger LOG = LoggerFactory.getLogger(Monitor.class);

    @Autowired
    UDM udm;

    @Autowired
    Reader reader;

    @PostConstruct
    public void setup(){
        udm.listenFor(Resource.Type.DOCUMENT, Resource.State.CREATED,"test.new.document", this);
    }


    @Override
    public boolean handle(Resource resource) {
        LOG.info("New document added: " + resource.getUri());

        reader.getAllDocuments().forEach(doc -> LOG.info("Existing document: " + doc.getUri()));

        return true;
    }
}

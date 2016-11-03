/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.intro;

import es.cbadenes.lab.test.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.librairy.bluebottle.Reader;
import org.librairy.model.domain.resources.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Category(IntegrationTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Config.class)
@TestPropertySource(properties = {
        "librairy.columndb.host  = localhost",
        "librairy.documentdb.host  = localhost",
        "librairy.graphdb.host  = localhost",
        "librairy.eventbus.host  = localhost"
})
public class ReaderTest {

    private static final Logger LOG = LoggerFactory.getLogger(ReaderTest.class);

    @Autowired
    Reader reader;

    @Test
    public void readAllDocuments(){
        List<Resource> docs = reader.getAllDocuments();

        LOG.info("Documents from librairy:" + docs);
    }


}

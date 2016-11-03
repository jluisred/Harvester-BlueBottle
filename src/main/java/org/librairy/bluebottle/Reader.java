/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.bluebottle;

import org.librairy.model.domain.resources.Resource;
import org.librairy.storage.UDM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class Reader {

    @Autowired
    UDM udm;

    public List<Resource> getAllDocuments(){

        return udm.find(Resource.Type.DOCUMENT).all();

    }

}

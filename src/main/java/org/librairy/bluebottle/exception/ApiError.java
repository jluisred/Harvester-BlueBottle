package org.librairy.bluebottle.exception;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class ApiError extends Exception {

    public ApiError(Exception e){
        super(e);
    }
}

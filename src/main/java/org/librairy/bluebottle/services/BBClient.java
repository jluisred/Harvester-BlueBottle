package org.librairy.bluebottle.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import org.librairy.bluebottle.conf.Conf;
import org.librairy.bluebottle.datastructure.BBBResource;
import org.librairy.bluebottle.datastructure.BBChapter;
import org.librairy.bluebottle.datastructure.BBResourceUnit;
import org.librairy.bluebottle.exception.ApiError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class BBClient {

    private static final Logger LOG = LoggerFactory.getLogger(BBClient.class);

    public BBClient(){

        Unirest.setDefaultHeader("x-api-key", Conf.getApikey());
        Unirest.setDefaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE);
        Unirest.setDefaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        jacksonObjectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        jacksonObjectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        Unirest.setObjectMapper(new ObjectMapper() {

            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    return jacksonObjectMapper.readValue(value, valueType);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public String writeValue(Object value) {
                try {
                    return jacksonObjectMapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });

    }


    public int getNumPages(int windowSize) throws ApiError {


        try{
            HttpResponse<BBBResource[]> response = Unirest.get(Conf.getEndpointURL() + "/resources")
                    .queryString("type", "epub")
                    .queryString("lang", "en")
                    .queryString("nrows", windowSize)
                    .asObject(BBBResource[].class);

            LOG.info("Response: " + response.getStatus());
            LOG.info("x-limit: " + response.getHeaders().getFirst("x-limit"));
            LOG.info("x-page: " + response.getHeaders().getFirst("x-page"));
            LOG.info("x-total: " + response.getHeaders().getFirst("x-total"));
            LOG.info("x-totalPages: " + response.getHeaders().getFirst("x-totalPages"));

            return Integer.parseInt(response.getHeaders().getFirst("x-totalPages"));
        }catch (Exception e){
            throw new ApiError(e);
        }
    }


    public List<BBBResource> getBooks(int index, int size) throws ApiError {

        try{
            int numRetry = 0;
            int maxRetries = 20;
            boolean thereIsAnswer = false;
            HttpResponse<BBBResource[]> response = null;

            while (!thereIsAnswer && numRetry < maxRetries) {

                response = Unirest.get(Conf.getEndpointURL() + "/resources")
                        .queryString("type", "epub")
                        .queryString("lang", "en")
                        .queryString("nrows", size)
                        .queryString("page", index)
                        .asObject(BBBResource[].class);

                if (response.getBody() != null) thereIsAnswer = true;
                else {
                    try {
                        Thread.sleep(2000);
                        LOG.warn("Retry: " + numRetry);
                    } catch (InterruptedException e) {
                        LOG.error("Unexpected error", e);
                        return Collections.emptyList();
                    }
                }

                numRetry++;
            }
            if (numRetry == maxRetries) {
                LOG.warn("Max retries reached getting books in ["+index+"/"+size+"]");
                return Collections.emptyList();
            }

            return new ArrayList<BBBResource>(Arrays.asList(response.getBody()));
        }catch (Exception e){
            throw new ApiError(e);
        }
    }


    public BBResourceUnit getChapters(String seoBook) throws ApiError {

        try{
            HttpResponse<BBResourceUnit> response = Unirest.get(Conf.getEndpointURL() + "/resources/" + seoBook + "/reader")
                    .asObject(BBResourceUnit.class);

            return response.getBody();
        }catch (Exception e){
            throw new ApiError(e);
        }
    }

    public String getChapter(String seoBook, String id) throws ApiError {

        try{

            HttpResponse<BBChapter> response = Unirest.get(Conf.getEndpointURL() + "/resources/" + seoBook + "/components/" + id)
                    .asObject(BBChapter.class);

            if (response.getStatus() == HttpStatus.OK.value() && response.getBody() == null) return "";
            return response.getBody().getData().getText();
        }catch (Exception e){
            throw new ApiError(e);
        }
    }



}

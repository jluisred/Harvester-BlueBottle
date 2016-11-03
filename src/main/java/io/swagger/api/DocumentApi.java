package io.swagger.api;

import io.swagger.model.Error;
import io.swagger.model.Topic;

import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-11-02T11:29:00.407Z")

@Api(value = "document", description = "the document API")
public interface DocumentApi {

    @ApiOperation(value = "Get Document's Topic", notes = "This operation returns the list of topics for a particular document D and their relative importance inside it. Each topic is a list of weighted words.  ", response = Topic.class, responseContainer = "List", tags={ "Products", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "An array of topics", response = Topic.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = Topic.class) })
    @RequestMapping(value = "/document/{id}/topicDistribution",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    ResponseEntity<List<Topic>> documentIdTopicDistributionGet(
@ApiParam(value = "ID of the document.",required=true ) @PathVariable("id") String id


);

}

package org.wso2.openbanking.consumerdatastandards.au.extensions.gen.api;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.ErrorMapperRequestBody;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.ErrorResponse;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.Response200ForErrorMapper;

import java.io.InputStream;
import java.util.Map;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

/**
* Represents a collection of functions to interact with the API endpoints.
*/
@Path("/map-accelerator-error-response")
@Api(description = "the map-accelerator-error-response API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-09-19T15:45:23.929498+05:30[Asia/Colombo]", comments = "Generator version: 7.12.0")
public class MapAcceleratorErrorResponseApi {

    @POST
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "map accelerator level error formats to custom error formats", notes = "", response = Response200ForErrorMapper.class, authorizations = {
        @Authorization(value = "OAuth2", scopes = {
             }),
        
        @Authorization(value = "BasicAuth")
         }, tags={ "Error Handling" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Ok", response = Response200ForErrorMapper.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Server Error", response = ErrorResponse.class)
    })
    public Response mapAcceleratorErrorResponsePost(@Valid @NotNull ErrorMapperRequestBody errorMapperRequestBody) {
        return Response.ok().entity("magic!").build();
    }
}

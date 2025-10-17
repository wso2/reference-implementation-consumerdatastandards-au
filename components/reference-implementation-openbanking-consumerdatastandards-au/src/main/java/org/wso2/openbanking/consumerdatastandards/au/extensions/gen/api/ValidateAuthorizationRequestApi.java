package org.wso2.openbanking.consumerdatastandards.au.extensions.gen.api;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.*;
import org.json.JSONObject;
import org.wso2.openbanking.consumerdatastandards.au.extensions.exceptions.CDSConsentException;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.ErrorResponse;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.Response200ForValidateAuthorizationRequest;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.SuccessResponse;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.ValidateAuthorizationRequestBody;
import org.wso2.openbanking.consumerdatastandards.au.extensions.handlers.CDSAuthorizationFlowHandler;

import java.io.InputStream;
import java.util.Map;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

/**
* Represents a collection of functions to interact with the API endpoints.
*/
@Path("/validate-authorization-request")
@Api(description = "the validate-authorization-request API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-09-19T15:45:23.929498+05:30[Asia/Colombo]", comments = "Generator version: 7.12.0")
public class ValidateAuthorizationRequestApi {

    @POST
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Handles pre-user authorization requests", notes = "", response = Response200ForValidateAuthorizationRequest.class, authorizations = {
        @Authorization(value = "OAuth2", scopes = {
             }),
        
        @Authorization(value = "BasicAuth")
         }, tags={ "Authorize" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful response", response = Response200ForValidateAuthorizationRequest.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Server Error", response = ErrorResponse.class)
    })
    public Response preUserAuthorization(@Valid @NotNull ValidateAuthorizationRequestBody validateAuthorizationRequestBody)
            throws CDSConsentException, JsonProcessingException {

        CDSAuthorizationFlowHandler cdsAuthorizationFlowHandler = new CDSAuthorizationFlowHandler();

        Response200ForValidateAuthorizationRequest response =
                cdsAuthorizationFlowHandler.handlePushedAuthorisationRequest(validateAuthorizationRequestBody);

        return Response.ok().entity(response).build();
    }
}

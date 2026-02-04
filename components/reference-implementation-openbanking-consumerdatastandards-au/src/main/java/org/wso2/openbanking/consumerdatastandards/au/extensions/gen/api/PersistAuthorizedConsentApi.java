package org.wso2.openbanking.consumerdatastandards.au.extensions.gen.api;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.consumerdatastandards.au.extensions.exceptions.CdsConsentException;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.ErrorResponse;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.PersistAuthorizedConsentRequestBody;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.Response200ForPersistAuthorizedConsent;
import org.wso2.openbanking.consumerdatastandards.au.extensions.impl.PersistAuthorizedConsentApiImpl;

import javax.validation.constraints.*;
import javax.validation.Valid;

import static org.reflections.Reflections.log;

/**
* Represents a collection of functions to interact with the API endpoints.
*/
@Path("/persist-authorized-consent")
@Api(description = "the persist-authorized-consent API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-09-19T15:45:23.929498+05:30[Asia/Colombo]", comments = "Generator version: 7.12.0")
public class PersistAuthorizedConsentApi {

    @POST
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "handle consent persistence logic and enrich response with user authorization and account mapping data", notes = "", response = Response200ForPersistAuthorizedConsent.class, authorizations = {
        @Authorization(value = "OAuth2", scopes = {
             }),
        
        @Authorization(value = "BasicAuth")
         }, tags={ "Consent" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful response", response = Response200ForPersistAuthorizedConsent.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Server Error", response = ErrorResponse.class)
    })
    public Response persistAuthorizedConsentPost(@Valid @NotNull PersistAuthorizedConsentRequestBody persistAuthorizedConsentRequestBody)
            throws CdsConsentException, JsonProcessingException {
        return PersistAuthorizedConsentApiImpl.handlePersistAuthorizedConsent(persistAuthorizedConsentRequestBody);
    }
}

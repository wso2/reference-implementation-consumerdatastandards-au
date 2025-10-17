package org.wso2.openbanking.consumerdatastandards.au.extensions.gen.api;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.ErrorResponse;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.Response200;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.ValidateConsentAccessRequestBody;
import org.wso2.openbanking.consumerdatastandards.au.extensions.validators.consent.CDSAccountValidator;

import java.io.InputStream;
import java.util.Map;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

/**
* Represents a collection of functions to interact with the API endpoints.
*/
@Path("/validate-consent-access")
@Api(description = "the validate-consent-access API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-09-19T15:45:23.929498+05:30[Asia/Colombo]", comments = "Generator version: 7.12.0")
public class ValidateConsentAccessApi {

    @POST
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "handle custom consent data validations before data access", notes = "", response = Response200.class, authorizations = {
        @Authorization(value = "OAuth2", scopes = {
             }),
        
        @Authorization(value = "BasicAuth")
         }, tags={ "Consent" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Ok", response = Response200.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Server Error", response = ErrorResponse.class)
    })
    public Response validateConsentAccessPost(@Valid @NotNull ValidateConsentAccessRequestBody validateConsentAccessRequestBody) {

        Log log = LogFactory.getLog(ValidateConsentAccessApi.class);
        // Read the request body
        String requestId = validateConsentAccessRequestBody.getRequestId();
        String cdrArrangementId = validateConsentAccessRequestBody.getData().getConsentId();
        String consentType = validateConsentAccessRequestBody.getData().getConsentResource().getType();
        Object consentResource = validateConsentAccessRequestBody.getData().getConsentResource();
        Object dataPayload = validateConsentAccessRequestBody.getData().getDataRequestPayload();

        JSONObject validationResponse = null;
//        try {
//            validationResponse = CDSAccountValidator.validateConsent(requestId, dataPayload, consentResource, consentType);
//
//        } catch (Exception e) {
//            log.error("Error while validating the consent access request", e);
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ErrorResponse()
//                    .data(CommonConsentValidationUtil.getErrorDataObject("server_error",
//                            e.getMessage())).toString()).build();
//        }

        return Response.status(Response.Status.OK).entity(validationResponse.toString()).build();
    }
}

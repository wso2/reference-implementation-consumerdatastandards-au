package org.wso2.openbanking.consumerdatastandards.au.extensions.gen.api;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;
import io.swagger.annotations.Authorization;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.*;

import java.io.InputStream;
import java.util.Map;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

/**
* Represents a collection of functions to interact with the API endpoints.
*/
@Path("/issue-refresh-token")
@Api(description = "the issue-refresh-token API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-09-19T15:45:23.929498+05:30[Asia/Colombo]", comments = "Generator version: 7.12.0")
public class IssueRefreshTokenApi {

    @POST
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Handles refresh token issuance and validations", notes = "", response = Response200ForIssueRefreshToken.class, authorizations = {
        @Authorization(value = "OAuth2", scopes = {
             }),
        
        @Authorization(value = "BasicAuth")
         }, tags={ "Token" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful response", response = Response200ForIssueRefreshToken.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Server Error", response = ErrorResponse.class)
    })
    public Response issueRefreshTokenPost(@Valid @NotNull IssueRefreshTokenRequestBody issueRefreshTokenRequestBody) {

        SuccessResponseIssueRefreshToken successResponseIssueRefreshToken = new SuccessResponseIssueRefreshToken();
        successResponseIssueRefreshToken.setResponseId(issueRefreshTokenRequestBody.getRequestId());
        successResponseIssueRefreshToken.setStatus(SuccessResponseIssueRefreshToken.StatusEnum.SUCCESS);

        SuccessResponseIssueRefreshTokenData successResponseIssueRefreshTokenData = new SuccessResponseIssueRefreshTokenData();

        //If consent validity period is 0 or null, do not issue refresh token
        if(issueRefreshTokenRequestBody.getData().getConsentValidityPeriod() == 0 ||
                issueRefreshTokenRequestBody.getData().getConsentValidityPeriod() == null) {

            successResponseIssueRefreshTokenData.setIssueRefreshToken(false);
        } else {
            successResponseIssueRefreshTokenData.setIssueRefreshToken(true);

            long consentValidityPeriod = issueRefreshTokenRequestBody.getData().getConsentValidityPeriod();
            long consentCreatedPeriod = issueRefreshTokenRequestBody.getData().getConsentCreatedTime();

            successResponseIssueRefreshTokenData.setRefreshTokenValidityPeriod(consentValidityPeriod - consentCreatedPeriod);
        }

        successResponseIssueRefreshToken.setData(successResponseIssueRefreshTokenData);
        return Response.ok().entity(successResponseIssueRefreshToken).build();
    }
}

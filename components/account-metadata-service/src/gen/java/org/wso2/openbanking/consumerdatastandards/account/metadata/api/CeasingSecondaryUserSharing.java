package org.wso2.openbanking.consumerdatastandards.account.metadata.api;

import org.wso2.openbanking.consumerdatastandards.account.metadata.impl.CeasingSecondaryUserSharingApiImpl;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.ErrorResponse;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.LegalEntitySharingItem;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;

import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

/**
 * Represents a collection of functions to interact with the API endpoints.
 */
@Path("/legal-entity")
@Api(description = "the legal-entity API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-03-19T12:03:46.119117500+05:30[Asia/Colombo]", comments = "Generator version: 7.19.0")
public class CeasingSecondaryUserSharing {

    @GET
    @Produces({ "application/json" })
    @ApiOperation(value = "Get legal entity sharing statuses for one user across multiple accounts", notes = "Retrieve legal entity sharing statuses (blocked or active) for a given user ID and multiple account IDs (comma-separated).", response = LegalEntitySharingItem.class, responseContainer = "List", authorizations = {
            @Authorization(value = "OAuth2", scopes = {
            }),

            @Authorization(value = "BasicAuth")
    }, tags={ "CeasingSecondaryUserShare" })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Legal entity sharing statuses retrieved successfully", response = LegalEntitySharingItem.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Server Error", response = ErrorResponse.class)
    })
        public Response getLegalEntitySharingStatusGet(@QueryParam("userId") @NotBlank @ApiParam("User ID")  String userId, @QueryParam("accountIds") @NotBlank  @ApiParam("Comma-separated account IDs")  String accountIds, @QueryParam("clientId") @NotBlank @ApiParam("Software product client ID to resolve legal entity ID")  String clientId) {
                return CeasingSecondaryUserSharingApiImpl.getLegalEntitySharingStatus(accountIds, userId, clientId);
    }

    @POST
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update legal entity sharing statuses for one or more records", notes = "Update legal entity sharing status records identified by the (secondaryUserID, accountID) pair. If legalEntitySharingStatus is blocked, legalEntityID is added to the fs_secondary_user.BLOCKED_ENTITIES column. If a record already exists, legalEntityID values are appended with a preceding ','. If legalEntitySharingStatus is active, legalEntityID is removed from the BLOCKED_ENTITIES string. ", response = LegalEntitySharingItem.class, responseContainer = "List", authorizations = {
            @Authorization(value = "OAuth2", scopes = {
            }),

            @Authorization(value = "BasicAuth")
    }, tags={ "CeasingSecondaryUserShare" })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Legal entity sharing statuses updated successfully", response = LegalEntitySharingItem.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Server Error", response = ErrorResponse.class)
    })
    public Response updateLegalEntitySharingStatusPost(@Valid @NotNull List<@Valid LegalEntitySharingItem> legalEntitySharingItem) {
                return CeasingSecondaryUserSharingApiImpl.updateLegalEntitySharingStatus(legalEntitySharingItem);
    }
}

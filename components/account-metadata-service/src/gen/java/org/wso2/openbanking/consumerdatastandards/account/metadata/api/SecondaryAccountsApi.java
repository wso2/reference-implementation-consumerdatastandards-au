package org.wso2.openbanking.consumerdatastandards.account.metadata.api;

import org.wso2.openbanking.consumerdatastandards.account.metadata.impl.SecondaryAccountsManagementApiImpl;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.ErrorResponse;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.SecondaryAccountInstructionItem;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;

import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

/**
 * Represents a collection of functions to interact with the API endpoints.
 */
@Path("/secondary-accounts")
@Api(description = "the secondary-accounts API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-02-24T17:50:15.025111200+05:30[Asia/Colombo]", comments = "Generator version: 7.19.0")
public class SecondaryAccountsApi {

    @POST
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Add secondary accounts instruction status for one or more accounts", notes = "Allows adding secondary accounts instruction and privilege status for multiple accounts.", response = SecondaryAccountInstructionItem.class, responseContainer = "List", authorizations = {
            @Authorization(value = "OAuth2", scopes = {
            }),

            @Authorization(value = "BasicAuth")
    }, tags={ "Secondary User Instruction" })
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Secondary accounts instruction status added successfully", response = SecondaryAccountInstructionItem.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Server Error", response = ErrorResponse.class)
    })
    public Response addSecondaryAccountsInstructionStatusPost(@Valid @NotNull List<@Valid SecondaryAccountInstructionItem> secondaryAccountInstructionItem) {
        return SecondaryAccountsManagementApiImpl.addSecondaryAccountInstructions(secondaryAccountInstructionItem);
    }

    @GET
    @Produces({ "application/json" })
    @ApiOperation(value = "Get secondary accounts instruction status for multiple accounts", notes = "Retrieve secondary accounts instruction and privilege status for multiple account IDs (comma-separated).", response = SecondaryAccountInstructionItem.class, responseContainer = "List", authorizations = {
            @Authorization(value = "OAuth2", scopes = {
            }),

            @Authorization(value = "BasicAuth")
    }, tags={ "Secondary User Instruction" })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Secondary accounts instruction status retrieved successfully", response = SecondaryAccountInstructionItem.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Server Error", response = ErrorResponse.class)
    })
    public Response getSecondaryAccountsInstructionStatusGet(@QueryParam("accountIds") @NotNull  @ApiParam("Comma-separated account IDs")  String accountIds,@QueryParam("userId") @NotNull  @ApiParam("User ID")  String userId) {
        return SecondaryAccountsManagementApiImpl.getSecondaryAccountInstructions(accountIds, userId);
    }
}

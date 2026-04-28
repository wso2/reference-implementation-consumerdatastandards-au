package org.wso2.openbanking.consumerdatastandards.account.metadata.api;

import org.wso2.openbanking.consumerdatastandards.account.metadata.impl.BusinessStakeholdersManagementApiImpl;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.BusinessStakeholderDeleteItem;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.BusinessStakeholderItem;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.BusinessStakeholderPermissionItem;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.ErrorResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;

import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

/**
* Represents a collection of functions to interact with the API endpoints.
*/
@Path("/business-stakeholders")
@Api(description = "the business-stakeholders API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-03-09T16:20:09.461136400+05:30[Asia/Colombo]", comments = "Generator version: 7.19.0")
public class BusinessStakeholdersApi {

    @POST
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Add business stakeholders for one or more accounts", notes = "Allows adding account owners and nominated representatives for multiple account IDs.", response = String.class, responseContainer = "List", authorizations = {
        @Authorization(value = "OAuth2", scopes = {
             }),
        
        @Authorization(value = "BasicAuth")
         }, tags={ "Business Stakeholders" })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Business stakeholders added successfully", response = String.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Server Error", response = ErrorResponse.class)
    })
    public Response addBusinessStakeholdersPost(@Valid @NotNull List<@Valid BusinessStakeholderItem> businessStakeholderItem) {
        return BusinessStakeholdersManagementApiImpl.addBusinessStakeholders(businessStakeholderItem);
    }

    @GET
    @Produces({ "application/json" })
    @ApiOperation(value = "Get business stakeholder permissions for one user across one or more accounts", notes = "Retrieve business stakeholder permission records for a given user ID and multiple account IDs (comma-separated).", response = BusinessStakeholderPermissionItem.class, responseContainer = "List", authorizations = {
            @Authorization(value = "OAuth2", scopes = {
            }),

            @Authorization(value = "BasicAuth")
    }, tags={ "Business Stakeholders" })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Business stakeholder permissions retrieved successfully", response = BusinessStakeholderPermissionItem.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Server Error", response = ErrorResponse.class)
    })
    public Response getBusinessStakeholdersGet(@QueryParam("accountIds") @NotNull  @ApiParam("Comma-separated account IDs")  String accountIds,@QueryParam("userId") @NotNull  @ApiParam("User ID")  String userId) {
        return BusinessStakeholdersManagementApiImpl.getBusinessStakeholders(accountIds, userId);
    }

    @DELETE
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete business stakeholders for one or more accounts", notes = "Allows deleting nominated representatives for multiple account IDs.", response = String.class, responseContainer = "List", authorizations = {
        @Authorization(value = "OAuth2", scopes = {
             }),
        
        @Authorization(value = "BasicAuth")
         }, tags={ "Business Stakeholders" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Business stakeholders deleted successfully", response = String.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Server Error", response = ErrorResponse.class)
    })
    public Response deleteBusinessStakeholdersDelete(@Valid @NotNull List<@Valid BusinessStakeholderDeleteItem> businessStakeholderDeleteItem) {
        return BusinessStakeholdersManagementApiImpl.deleteBusinessStakeholders(businessStakeholderDeleteItem);
    }

    @PUT
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update business stakeholders for one or more accounts", notes = "Allows updating account owners and nominated representatives for multiple account IDs.", response = String.class, responseContainer = "List", authorizations = {
        @Authorization(value = "OAuth2", scopes = {
             }),
        
        @Authorization(value = "BasicAuth")
         }, tags={ "Business Stakeholders" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Business stakeholders updated successfully", response = String.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Server Error", response = ErrorResponse.class)
    })
    public Response updateBusinessStakeholdersPut(@Valid @NotNull List<@Valid BusinessStakeholderItem> businessStakeholderItem) {
        return BusinessStakeholdersManagementApiImpl.updateBusinessStakeholders(businessStakeholderItem);
    }
}

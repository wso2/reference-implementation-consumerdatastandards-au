package org.wso2.openbanking.consumerdatastandards.account.metadata.service.api;

import org.wso2.openbanking.consumerdatastandards.account.metadata.service.impl.DisclosureOptionsManagementApiImpl;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.model.DisclosureOptionItem;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.model.ModelApiResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;

import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

/**
 * Represents a collection of functions to interact with the API endpoints.
 */
@Path("/disclosure-options")
@Api(description = "the disclosure-options API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-02-19T15:46:58.698251900+05:30[Asia/Colombo]", comments = "Generator version: 7.20.0")
public class DisclosureOptionsApi {

    @POST
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Add disclosure options for one or more accounts", notes = "Allows adding disclosure option status for multiple account IDs.", response = ModelApiResponse.class, authorizations = {
            @Authorization(value = "OAuth2", scopes = {
            }),

            @Authorization(value = "BasicAuth")
    }, tags={ "Disclosure Options" })
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Disclosure options added successfully", response = ModelApiResponse.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ModelApiResponse.class),
            @ApiResponse(code = 500, message = "Server Error", response = ModelApiResponse.class)
    })
    public Response addDisclosureOptionsPost(@Valid @NotNull List<@Valid DisclosureOptionItem> disclosureOptionItem) {
        return DisclosureOptionsManagementApiImpl.addDisclosureOptions(disclosureOptionItem);
    }

    @GET
    @Produces({ "application/json" })
    @ApiOperation(value = "Get disclosure options for multiple accounts", notes = "Retrieve disclosure option statuses for multiple account IDs (comma-separated).", response = DisclosureOptionItem.class, responseContainer = "List", authorizations = {
            @Authorization(value = "OAuth2", scopes = {
            }),

            @Authorization(value = "BasicAuth")
    }, tags={ "Disclosure Options" })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Disclosure options retrieved successfully", response = DisclosureOptionItem.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request", response = ModelApiResponse.class),
            @ApiResponse(code = 500, message = "Server Error", response = ModelApiResponse.class)
    })
    public Response getDisclosureOptionsGet(@QueryParam("accountIds") @NotNull  @ApiParam("Comma-separated account IDs")  String accountIds) {
        return DisclosureOptionsManagementApiImpl.getDisclosureOptions(accountIds);
    }

    @PUT
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update disclosure options for one or more accounts", notes = "Allows updating disclosure option status for multiple account IDs.", response = ModelApiResponse.class, authorizations = {
            @Authorization(value = "OAuth2", scopes = {
            }),

            @Authorization(value = "BasicAuth")
    }, tags={ "Disclosure Options" })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Disclosure options updated successfully", response = ModelApiResponse.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ModelApiResponse.class),
            @ApiResponse(code = 500, message = "Server Error", response = ModelApiResponse.class)
    })
    public Response updateDisclosureOptionsPut(@Valid @NotNull List<@Valid DisclosureOptionItem> disclosureOptionItem) {
        return DisclosureOptionsManagementApiImpl.updateDisclosureOptions(disclosureOptionItem);
    }
}

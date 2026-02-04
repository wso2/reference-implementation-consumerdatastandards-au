/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.openbanking.consumerdatastandards.au.extensions.account.metadata.management.endpoints.disclosure.options.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import org.wso2.openbanking.consumerdatastandards.au.extensions.account.metadata.management.endpoints.disclosure.options.impl.DisclosureOptionsManagementApiImpl;
import org.wso2.openbanking.consumerdatastandards.au.extensions.account.metadata.management.endpoints.disclosure.options.model.DisclosureOptionsUpdateRequest;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * Represents a collection of functions to interact with the Disclosure Options Management API endpoints.
 */
@Path("/disclosure-options")
@Api(description = "the disclosure-options management API")
@javax.annotation.Generated(
        value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen",
        date = "2025-09-19T15:45:23.929498+05:30[Asia/Colombo]",
        comments = "Generator version: 7.12.0"
)
public class DisclosureOptionsManagementApi {

    @PUT
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(
            value = "Update disclosure options for one or more accounts",
            notes = "Allows updating disclosure option status for multiple account IDs",
            response = Void.class,
            authorizations = {
                    @Authorization(value = "OAuth2", scopes = {}),
                    @Authorization(value = "BasicAuth")
            },
            tags = { "Disclosure Options" }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Disclosure options updated successfully"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Server Error")
    })
    public Response updateDisclosureOptionsPut(
            @Valid @NotNull DisclosureOptionsUpdateRequest request
    ) throws Exception {

        return DisclosureOptionsManagementApiImpl.updateDisclosureOptions(request);
    }
}

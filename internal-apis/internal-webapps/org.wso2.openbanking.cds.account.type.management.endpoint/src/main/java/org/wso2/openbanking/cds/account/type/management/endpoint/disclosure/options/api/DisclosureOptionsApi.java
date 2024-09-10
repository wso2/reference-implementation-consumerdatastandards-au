/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.openbanking.cds.account.type.management.endpoint.disclosure.options.api;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * Disclosure Options API.
 */

@Path("/account-type-management")
public interface DisclosureOptionsApi {
    /**
     * Disclosure Options API for Joint Accounts
     * REST API endpoint that updates the disclosure options of the joint CDS accounts by
     * sending a JSON payload in the request body.
     */
    @PUT
    @Path("/disclosure-options")
    @Produces({"application/json"})
    @ApiOperation(value = "Update CDS account disclosure options", notes = "This API is used to update the CDS " +
            "account disclosure status.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Account disclosure options successfully updated."),
            @ApiResponse(code = 400, message = "Bad Request. Request body validation failed.")
    })
    Response updateCDSAccountDisclosureOptions(@ApiParam(value = "Array of account disclosure option details.\n",
            required = true) String requestBody); }



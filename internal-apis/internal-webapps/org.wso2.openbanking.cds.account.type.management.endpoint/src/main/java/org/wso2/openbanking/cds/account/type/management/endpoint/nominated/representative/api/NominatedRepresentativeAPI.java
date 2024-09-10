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
package org.wso2.openbanking.cds.account.type.management.endpoint.nominated.representative.api;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.wso2.openbanking.cds.account.type.management.endpoint.model.ErrorDTO;
import org.wso2.openbanking.cds.account.type.management.endpoint.nominated.representative.model.NominatedRepresentativeResponseDTO;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * Nominated Representative API.
 * Used to manage the permissions of business nominated representatives.
 */
@Path("/account-type-management")
public interface NominatedRepresentativeAPI {

    /**
     * Update the permissions of business nominated representatives.
     *
     * @param requestBody - Business nominated representative details
     * @return - Success or failure response
     */
    @PUT
    @Path("/business-stakeholders")
    @Consumes({"application/json; charset=utf-8"})
    @Produces({"application/json; charset=utf-8"})
    @ApiOperation(value = "Update Business Nominated Representative Permissions\n",
            notes = "This API is used to update the CDS Business Nominated Representative Permissions.\n")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Business nominated representative permissions successfully updated\n"),
            @ApiResponse(code = 400, message = "Bad Request.\nRequest body validation failed.\n")})
    Response updateNominatedRepresentativePermissions(
            @ApiParam(value = "Business nominated representative permissions.\n", required = true) String requestBody
    );

    /**
     * Revoke the permissions of business nominated representatives.
     *
     * @param requestBody - Business nominated representative details
     * @return - Success or failure response
     */
    @DELETE
    @Path("/business-stakeholders")
    @Consumes({"application/json; charset=utf-8"})
    @Produces({"application/json; charset=utf-8"})
    @ApiOperation(value = "Revoke the permissions of business nominated representatives\n",
            notes = "This API is used to revoke the permissions of business nominated representatives.\n")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Business nominated representative permissions successfully revoked\n"),
            @ApiResponse(code = 400, message = "Bad Request.\nRequest body validation failed.\n")})
    Response revokeNominatedRepresentativePermissions(
            @ApiParam(value = "Business nominated representative permissions.\n", required = true) String requestBody
    );

    /**
     * Retrieve the permissions of business nominated representatives.
     *
     * @return - Business nominated representative permission details
     */
    @GET
    @Path("/business-stakeholders/permission")
    @Produces({"application/json; charset=utf-8"})
    @ApiOperation(value = "Retrieve the permission status of the business nominated representatives. \n",
            notes = "This API is used to retrieve the permission status of the business nominated representatives.\n")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Permission status successfully retrieved.",
                    response = NominatedRepresentativeResponseDTO.class),
            @ApiResponse(code = 400, message = "Bad Request.\nRequest body validation failed.",
                    response = ErrorDTO.class)})
    Response retrieveNominatedRepresentativePermissions(
            @ApiParam(value = "Account ID of the subject.\n", required = true)
            @QueryParam("accountId") String accountId,
            @ApiParam(value = "User identifier of the subject.\n", required = true)
            @QueryParam("userId") String userId
    );

    @GET
    @Path("/business-stakeholders/profiles")
    @Consumes({"application/json; charset=utf-8"})
    @ApiOperation(value = "Retrieve the available user profiles available for a user. \n",
            notes = "This API is used to retrieve the available user profiles of users.\n")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Permission status successfully retrieved\n"),
            @ApiResponse(code = 400, message = "Bad Request.\nRequest body validation failed.\n")})
    Response retrieveNominatedRepresentativeProfiles(
            @ApiParam(value = "User identifier of the subject.\n", required = true)
            @QueryParam("userId") String userId
    );

}

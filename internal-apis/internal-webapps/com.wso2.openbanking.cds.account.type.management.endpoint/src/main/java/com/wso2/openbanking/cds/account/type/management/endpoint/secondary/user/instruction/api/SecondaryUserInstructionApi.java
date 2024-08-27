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

package org.wso2.openbanking.cds.account.type.management.endpoint.secondary.user.instruction.api;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 *  Secondary User API.
 */
@Path("/account-type-management")
public interface SecondaryUserInstructionApi {

    /**
     * Update Secondary User Instructions.
     * This end point allows Data Holder to update OB solution on secondary user instruction changes.
     */
    @PUT
    @Path("/secondary-accounts")
    @Consumes({"application/json; charset=utf-8"})
    @Produces({"application/json; charset=utf-8"})
    @ApiOperation(value = "Update Secondary Accounts Instruction Status\n",
            notes = "This API is used to update the CDS Secondary Accounts Instruction and Privilege Status.\n")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = " Secondary Accounts Instruction Status successfully updated\n"),
            @ApiResponse(code = 400, message = "Bad Request.\nRequest body validation failed.\n")})
    Response updateSecondaryAccountStatus(
            @ApiParam(value = "Secondary Accounts Instruction and Privilege Status details.\n", required = true)
            String requestBody);
}

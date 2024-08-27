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

package org.wso2.openbanking.cds.metadata.mgt.endpoint.api;

import org.wso2.openbanking.cds.metadata.mgt.endpoint.model.MetadataUpdateRequestDTO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import javax.ws.rs.core.Response;

/**
 *  Metadata Management API.
 */
@Path("/admin")
public interface MetadataMgtApi {

    /**
     * Metadata Update.
     * Indicate that a critical update to the metadata for Accredited Data Recipients has been made and
     * should be obtained.
     */
    @POST
    @Path("/register/metadata")
    @Consumes({"application/json"})
    @ApiOperation(value = "Metadata Update", tags = {"Admin", "Register"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success")})
    Response updateMetaData(@HeaderParam("x-v") @NotNull String xV, @Valid MetadataUpdateRequestDTO action,
                                   @HeaderParam("x-min-v") String xMinV);

}

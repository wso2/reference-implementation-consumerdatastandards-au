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

package org.wso2.openbanking.cds.metrics.endpoint.api;

import org.wso2.openbanking.cds.metrics.endpoint.model.ResponseMetricsListDTO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 *  Metrics API which is a part of CDS Admin API.
 */
@Path("/admin")
public interface MetricsApi {

    /**
     * Get Metrics.
     * This end point allows the ACCC to obtain operational statistics from the Data Holder on the operation of their
     * CDR compliant implementation. The statistics obtainable from this end point are determined by the non-functional
     * requirements for the CDR regime.
     */
    @GET
    @Path("/metrics")
    @Produces({"application/json"})
    @ApiOperation(value = "Get Metrics", tags = {"Admin", "Metrics"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = ResponseMetricsListDTO.class)})
    Response getMetrics(@HeaderParam("x-v") @NotNull String xV, @QueryParam("period")
    @DefaultValue("ALL") String period, @HeaderParam("x-min-v") String xMinV);

}

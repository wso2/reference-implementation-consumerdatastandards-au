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

package org.wso2.openbanking.cds.demo.backend.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static org.wso2.openbanking.cds.demo.backend.services.BankingService.getResponse;
import static org.wso2.openbanking.cds.demo.backend.services.BankingService.getSampleLinks;


/**
 * Discovery Service.
 */
@Path("/")
public class DiscoveryService {

    private static final String XV_HEADER = "x-v";
    private static JsonParser jsonParser = new JsonParser();

    @GET
    @Path("/status")
    @Produces("application/json")
    public Response getDiscoveryStatus(@HeaderParam(XV_HEADER) String apiVersion) {

        String statusJson = "{\n" +
                "    \"status\": \"OK\",\n" +
                "    \"explanation\": \"string\",\n" +
                "    \"detectionTime\": \"string\",\n" +
                "    \"expectedResolutionTime\": \"string\",\n" +
                "    \"updateTime\": \"string\"\n" +
                "  }";

        JsonObject status = jsonParser.parse(statusJson).getAsJsonObject();
        String response = getResponse(status, getSampleLinks("/status"), new JsonObject());

        return Response.status(200).entity(response).header(XV_HEADER, apiVersion).build();
    }

    @GET
    @Path("/outages")
    @Produces("application/json")
    public Response getDiscoveryOutages(@HeaderParam(XV_HEADER) String apiVersion) {

        String outageJson = "{\n" +
                "    \"outages\": [\n" +
                "      {\n" +
                "        \"outageTime\": \"string\",\n" +
                "        \"duration\": \"string\",\n" +
                "        \"isPartial\": true,\n" +
                "        \"explanation\": \"string\"\n" +
                "      }\n" +
                "    ]\n" +
                "  }";

        JsonObject outage = jsonParser.parse(outageJson).getAsJsonObject();
        String response = getResponse(outage, getSampleLinks("/outages"), new JsonObject());

        return Response.status(200).entity(response).header(XV_HEADER, apiVersion).build();
    }

}

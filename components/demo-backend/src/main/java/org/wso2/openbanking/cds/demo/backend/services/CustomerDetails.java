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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * Identify the customer type in the consent flow.
 * CustomerDetailsService class
 */
@Path("/")
public class CustomerDetails {

    //customerUType can be 'Person' or 'Organisation'
    private static final String customerOrg = "{\n" +
            "  \"customerUType\": \"organisation\" " +
            "}";

    private static final String customerPer = "{\n" +
            "  \"customerUType\": \"person\" " +
            "}";

    @GET
    @Path("/details/{userId}")
    @Produces("application/json; charset=utf-8")
    public Response getCustomerDetails(@PathParam("userId") String userId) {

        //Try different user to check person profile
        if ("admin@wso2.com".equals(userId)) {
            return Response.status(200).entity(customerOrg).build();
        } else {
            return Response.status(200).entity(customerPer).build();
        }
    }

}

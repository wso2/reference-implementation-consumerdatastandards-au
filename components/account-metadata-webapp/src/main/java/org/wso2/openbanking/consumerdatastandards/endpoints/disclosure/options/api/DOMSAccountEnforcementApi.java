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

package org.wso2.openbanking.consumerdatastandards.endpoints.disclosure.options.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.wso2.openbanking.consumerdatastandards.endpoints.disclosure.options.impl.DOMSAccountEnforcementApiImpl;
import org.wso2.openbanking.consumerdatastandards.endpoints.disclosure.options.model.DOMSBlockedAccountsRequest;
import org.wso2.openbanking.consumerdatastandards.endpoints.disclosure.options.model.DOMSBlockedAccountsResponse;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * API to fetch blocked accounts for DOMS enforcement.
 */
@Path("/disclosure-options")
@Api(description = "the disclosure-options management API")
public class DOMSAccountEnforcementApi {

    @POST
    @Path("/blocked-accounts")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(
            value = "Get blocked accounts for DOMS enforcement",
            notes = "Returns the list of account IDs that are blocked for disclosure",
            response = DOMSBlockedAccountsResponse.class,
            authorizations = {
                    @Authorization(value = "OAuth2", scopes = {}),
                    @Authorization(value = "BasicAuth")
            },
            tags = { "Disclosure Options" }
    )
    public Response getBlockedAccounts(
            @Valid @NotNull DOMSBlockedAccountsRequest request
    ) throws Exception {
        return DOMSAccountEnforcementApiImpl.getBlockedAccounts(request);
    }
}

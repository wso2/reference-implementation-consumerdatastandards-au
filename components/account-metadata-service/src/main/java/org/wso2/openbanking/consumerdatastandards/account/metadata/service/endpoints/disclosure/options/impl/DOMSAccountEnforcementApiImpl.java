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

package org.wso2.openbanking.consumerdatastandards.account.metadata.service.endpoints.disclosure.options.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.endpoints.disclosure.options.model.DOMSBlockedAccountsRequest;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.endpoints.disclosure.options.model.DOMSBlockedAccountsResponse;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.exceptions.AccountMetadataException;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.service.service.AccountMetadataService;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.service.service.AccountMetadataServiceImpl;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

/**
 * Implementation for DOMS account enforcement API operations.
 */
public class DOMSAccountEnforcementApiImpl {

    private static final Log log = LogFactory.getLog(DOMSAccountEnforcementApiImpl.class);

    private static final AccountMetadataService accountMetadataService = AccountMetadataServiceImpl.getInstance();

    private DOMSAccountEnforcementApiImpl() {
        // Prevent instantiation
    }

    /**
     * Retrieves accounts blocked by DOMS (no-sharing status).
     *
     * @param request request containing account IDs to check
     * @return response with list of blocked account IDs
     */
    public static Response getBlockedAccounts(DOMSBlockedAccountsRequest request) {

        if (request == null) {
            log.error("[DOMS] No accounts provided in get blocked accounts request");
            return Response.status(Response.Status.BAD_REQUEST)
                    .build();
        }
        List<String> blockedAccounts = new ArrayList<>();

        try {
            // Query database directly for blocked accounts with no-sharing status
            blockedAccounts = accountMetadataService.getBlockedAccounts(
                    request.getAccountIds());

        } catch (AccountMetadataException e) {
            log.error("Error retrieving blocked accounts from database", e);
            // Fail-safe behavior: return empty list
        }

        return Response.ok(
                new DOMSBlockedAccountsResponse(blockedAccounts)
        ).build();

    }
}

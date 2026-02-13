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

package org.wso2.openbanking.consumerdatastandards.endpoints.disclosure.options.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.consumerdatastandards.constants.CommonConstants;
import org.wso2.openbanking.consumerdatastandards.endpoints.disclosure.options.model.DOMSBlockedAccountsRequest;
import org.wso2.openbanking.consumerdatastandards.endpoints.disclosure.options.model.DOMSBlockedAccountsResponse;
import org.wso2.openbanking.consumerdatastandards.exceptions.AccountMetadataException;
import org.wso2.openbanking.consumerdatastandards.service.service.AccountMetadataService;
import org.wso2.openbanking.consumerdatastandards.service.service.AccountMetadataServiceImpl;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

public class DOMSAccountEnforcementApiImpl {

    private static final Log log = LogFactory.getLog(DOMSAccountEnforcementApiImpl.class);

    private static final AccountMetadataService accountMetadataService = AccountMetadataServiceImpl.getInstance();

    private DOMSAccountEnforcementApiImpl() {
        // Prevent instantiation
    }

    /**
     * Returns list of DOMS-blocked account IDs.
     */
    public static Response getBlockedAccounts(DOMSBlockedAccountsRequest request) {

        List<String> blockedAccounts = new ArrayList<>();

        for (String accountId : request.getAccountIds()) {
            try {
                String domsStatus = accountMetadataService.getDisclosureOption(accountId);

                if (CommonConstants.DOMS_STATUS_NO_SHARING.equalsIgnoreCase(domsStatus)) {
                    blockedAccounts.add(accountId);
                }

            } catch (AccountMetadataException e) {
                log.error("Error checking DOMS status for accountId: " + accountId, e);
                // Fail-safe behavior: treat as not blocked
            }
        }

        return Response.ok(
                new DOMSBlockedAccountsResponse(blockedAccounts)
        ).build();
    }
}

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

package org.wso2.openbanking.consumerdatastandards.au.extensions.account.metadata.management.endpoints.disclosure.options.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.consumerdatastandards.au.extensions.account.metadata.management.endpoints.disclosure.options.model.DisclosureOptionItem;
import org.wso2.openbanking.consumerdatastandards.au.extensions.account.metadata.management.endpoints.disclosure.options.model.DisclosureOptionsUpdateRequest;
import org.wso2.openbanking.consumerdatastandards.au.extensions.account.metadata.service.service.AccountMetadataServiceImpl;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.CommonConstants;
import org.wso2.openbanking.consumerdatastandards.au.extensions.exceptions.AccountMetadataException;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;

public class DisclosureOptionsManagementApiImpl {

    private static final Log log = LogFactory.getLog(DisclosureOptionsManagementApiImpl.class);

    private static final AccountMetadataServiceImpl accountMetadataService = AccountMetadataServiceImpl.getInstance();

    private DisclosureOptionsManagementApiImpl() {
        // Prevent instantiation
    }

    public static Response updateDisclosureOptions(DisclosureOptionsUpdateRequest request) {

        log.debug("[DOMS] Update Account Disclosure Options request received");

        if (request == null || request.getData() == null || request.getData().isEmpty()) {
            log.warn("[DOMS] No disclosure options provided to update");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("No disclosure options provided")
                    .build();
        }

        try {
            for (DisclosureOptionItem item : request.getData()) {
                Map<String, String> metadataMap = new HashMap<>();
                metadataMap.put(CommonConstants.DOMS_STATUS, item.getDisclosureOption().toString());

                // Call the addOrUpdateGlobalAccountMetadata method from the AccountMetadataService class
                accountMetadataService.addOrUpdateAccountMetadata(item.getAccountID(), metadataMap);

                log.debug("[DOMS] Updated account: " + item.getAccountID() + " with disclosure option: "
                        + item.getDisclosureOption());
            }

            return Response.ok().entity("Disclosure options updated successfully").build();

        } catch (AccountMetadataException e) {
            log.error("[DOMS] Failed to update disclosure options via AccountMetadataService", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to update disclosure options: " + e.getMessage())
                    .build();
        }
    }
}

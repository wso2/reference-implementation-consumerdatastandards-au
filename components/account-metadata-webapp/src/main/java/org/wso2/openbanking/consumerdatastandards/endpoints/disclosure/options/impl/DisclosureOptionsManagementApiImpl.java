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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.consumerdatastandards.constants.CommonConstants;
import org.wso2.openbanking.consumerdatastandards.exceptions.AccountMetadataException;
import org.wso2.openbanking.consumerdatastandards.endpoints.disclosure.options.model.ApiResponse;
import org.wso2.openbanking.consumerdatastandards.endpoints.disclosure.options.model.DisclosureOptionItem;
import org.wso2.openbanking.consumerdatastandards.endpoints.disclosure.options.model.DisclosureOptionsBulkResponse;
import org.wso2.openbanking.consumerdatastandards.endpoints.disclosure.options.model.DisclosureOptionsUpdateRequest;
import org.wso2.openbanking.consumerdatastandards.service.service.AccountMetadataService;
import org.wso2.openbanking.consumerdatastandards.service.service.AccountMetadataServiceImpl;

import javax.ws.rs.core.Response;

public class DisclosureOptionsManagementApiImpl {

    private static final Log log = LogFactory.getLog(DisclosureOptionsManagementApiImpl.class);

    private static final AccountMetadataService accountMetadataService = AccountMetadataServiceImpl.getInstance();

    private DisclosureOptionsManagementApiImpl() {
        // Prevent instantiation
    }

    public static Response updateDisclosureOptions(DisclosureOptionsUpdateRequest request) {

        if (request == null || request.getData() == null || request.getData().isEmpty()) {
            log.warn("[DOMS] No disclosure options provided to update");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiResponse("error", "No disclosure options provided"))
                    .build();
        }

        try {
            for (DisclosureOptionItem item : request.getData()) {

                // Validate disclosure option status
                String disclosureOptionStatus = item.getDisclosureOption();
                if (isNotValidDOMSStatus(disclosureOptionStatus)) {
                    log.warn("[DOMS] Invalid disclosure option status for account: " +
                            item.getAccountID() + " - " + disclosureOptionStatus);
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(new ApiResponse("error",
                                    "Invalid disclosure option status. " +
                                            "Allowed values: no-sharing, pre-approval"))
                            .build();
                }

                // Calling the addOrUpdate DOMS status for joint accounts method from the AccountMetadataService class
                accountMetadataService.updateDisclosureOption(
                        item.getAccountID(), disclosureOptionStatus);
            }

            return Response.ok()
                    .entity(new ApiResponse("success", "Disclosure options updated successfully"))
                    .build();

        } catch (AccountMetadataException e) {
            log.error("[DOMS] Failed to update disclosure options via AccountMetadataService", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ApiResponse(
                            "error", "Failed to update disclosure options: " + e.getMessage()))
                    .build();
        }
    }

    public static Response getDisclosureOptions(java.util.List<String> accountIds) {


        if (accountIds == null || accountIds.isEmpty()) {
            log.warn("[DOMS] accountIds are missing in get request");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiResponse("error", "At least one accountId is required"))
                    .build();
        }

        try {
            java.util.Map<String, String> result = new java.util.HashMap<>();
            for (String acc : accountIds) {
                if (StringUtils.isBlank(acc)) {
                    result.put(acc, null);
                    continue;
                }
                try {
                    String status = accountMetadataService.getDisclosureOption(acc);
                    result.put(acc, status);
                } catch (AccountMetadataException e) {
                    // put null to keep processing other accounts
                    result.put(acc, null);
                    log.error("[DOMS] Error retrieving status for account: " + acc, e);
                }
            }

            DisclosureOptionsBulkResponse resp =
                    new DisclosureOptionsBulkResponse(
                            "success", result);

            return Response.ok().entity(resp).build();

        } catch (RuntimeException e) {
            log.error("[DOMS] Failed to retrieve disclosure options for multiple accounts", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ApiResponse(
                            "error", "Failed to retrieve disclosure options: " + e.getMessage()))
                    .build();
        }
    }

    public static Response addDisclosureOptions(DisclosureOptionsUpdateRequest request) {

        if (request == null || request.getData() == null || request.getData().isEmpty()) {
            log.warn("[DOMS] No disclosure options provided to add");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiResponse("error", "No disclosure options provided"))
                    .build();
        }

        try {
            boolean anyExisted = false;

            for (DisclosureOptionItem item : request.getData()) {

                // Validate disclosure option status
                String disclosureOptionStatus = item.getDisclosureOption();
                if (isNotValidDOMSStatus(disclosureOptionStatus)) {
                    log.warn("[DOMS] Invalid disclosure option status for account: " +
                            item.getAccountID() + " - " + disclosureOptionStatus);
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(new ApiResponse("error",
                                    "Invalid disclosure option status. " +
                                            "Allowed values: no-sharing, pre-approval"))
                            .build();
                }

                // Check if disclosure option already exists for this account
                String existingStatus = accountMetadataService.getDisclosureOption(item.getAccountID());
                if (existingStatus == null) {
                    // Use the AccountMetadataService to add disclosure option for each account
                    accountMetadataService.addDisclosureOption(
                            item.getAccountID(), disclosureOptionStatus);
                } else {
                    anyExisted = true;
                }
            }

            // Return 201 Created if all were new, 200 OK if any already existed (updates)
            if (anyExisted) {
                return Response.ok()
                        .entity(new ApiResponse("success",
                                "Disclosure options already exists for the account"))
                        .build();
            } else {
                return Response.status(Response.Status.CREATED)
                        .entity(new ApiResponse("success", "Disclosure options added successfully"))
                        .build();
            }

        } catch (AccountMetadataException e) {
            log.error("[DOMS] Failed to add disclosure options via AccountMetadataService", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ApiResponse("error", "Failed to add disclosure options: "
                            + e.getMessage()))
                    .build();
        }
    }

    /**
     * Validates if the disclosure option status is a valid DOMS status.
     * Valid values are: no-sharing, pre-approval
     *
     * @param status the status to validate
     * @return true if the status is valid, false otherwise
     */
    private static boolean isNotValidDOMSStatus(String status) {
        return status == null || (!status.equalsIgnoreCase(CommonConstants.DOMS_STATUS_PRE_APPROVAL) &&
                !status.equalsIgnoreCase(CommonConstants.DOMS_STATUS_NO_SHARING));
    }
}

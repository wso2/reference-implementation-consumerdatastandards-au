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

package org.wso2.openbanking.consumerdatastandards.account.metadata.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.consumerdatastandards.account.metadata.constants.CommonConstants;
import org.wso2.openbanking.consumerdatastandards.account.metadata.exceptions.AccountMetadataException;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.DisclosureOptionItem;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.ModelApiResponse;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.core.AccountMetadataService;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.core.AccountMetadataServiceImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

/**
 * Implementation for disclosure options management API operations.
 */
public class DisclosureOptionsManagementApiImpl {

    private static final Log log = LogFactory.getLog(DisclosureOptionsManagementApiImpl.class);

    private static final AccountMetadataService accountMetadataService = AccountMetadataServiceImpl.getInstance();

    private DisclosureOptionsManagementApiImpl() {
        // Prevent instantiation
    }

    /**
     * Updates disclosure options for multiple accounts.
     *
     * @param request list of disclosure option items to update
     * @return response with update status
     */
    public static Response updateDisclosureOptions(List<DisclosureOptionItem> request) {

        if (request == null || request.isEmpty()) {
            log.error("[DOMS] No disclosure options provided to update");
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ModelApiResponse()
                    .message("No disclosure options provided"))
                    .build();
        }

        try {
            Map<String, String> accountDisclosureMap = new HashMap<>();
            List<String> accountIdsToCheck = new ArrayList<>();
            
            // Validate and build map
            for (DisclosureOptionItem item : request) {
                String disclosureOptionStatus = item.getDisclosureOption();
                if (isValidDOMSStatus(disclosureOptionStatus)) {
                    accountDisclosureMap.put(item.getAccountId(), disclosureOptionStatus);
                    accountIdsToCheck.add(item.getAccountId());
                } else {
                    log.error("[DOMS] Invalid disclosure option status for account: " +
                            item.getAccountId() + " - " + disclosureOptionStatus);
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(new ModelApiResponse().message(
                                    "Invalid disclosure option status. " +
                                            "Allowed values: no-sharing, pre-approval"))
                            .build();
                }
            }

            Map<String, String> existingStatuses =
                    accountMetadataService.getBatchDisclosureOptions(accountIdsToCheck);

            Map<String, String> existingAccountsToUpdate = new HashMap<>();
            for (Map.Entry<String, String> entry : accountDisclosureMap.entrySet()) {
                if (existingStatuses.containsKey(entry.getKey())) {
                    existingAccountsToUpdate.put(entry.getKey(), entry.getValue());
                }
            }

            List<String> nonExistingAccountIds = accountIdsToCheck.stream()
                .filter(accountId -> !existingStatuses.containsKey(accountId))
                .distinct()
                .collect(Collectors.toList());

            if (!existingAccountsToUpdate.isEmpty()) {
                accountMetadataService.updateBatchDisclosureOptions(existingAccountsToUpdate);
            }

            if (nonExistingAccountIds.isEmpty()) {
                return Response.ok()
                        .entity(new ModelApiResponse()
                                .message("Disclosure options updated successfully"))
                        .build();
            }

            if (existingAccountsToUpdate.isEmpty()) {
                return Response.ok()
                        .entity(new ModelApiResponse()
                                .message("No disclosure options were updated. AccountId(s) do not exist: "
                                        + String.join(", ", nonExistingAccountIds)))
                        .build();
            }

            return Response.ok()
                    .entity(new ModelApiResponse()
                            .message("Disclosure options updated successfully for existing accounts. " +
                                    "AccountId(s) do not exist: "
                                    + String.join(", ", nonExistingAccountIds)))
                    .build();

        } catch (AccountMetadataException e) {
            log.error("[DOMS] Failed to update disclosure options via AccountMetadataService", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ModelApiResponse().message(
                            "Failed to update disclosure options: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Retrieves disclosure options for specified accounts.
     *
     * @param accountIds comma-separated list of account IDs
     * @return response with disclosure options
     */
    public static Response getDisclosureOptions(String accountIds) {

        if (StringUtils.isBlank(accountIds)) {
            log.error("[DOMS] accountIds are missing in get request");
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ModelApiResponse()
                    .message("At least one accountId is required"))
                    .build();
        }

        try {
            // Split comma-separated account IDs and trim whitespace
            List<String> accountIdList = Arrays.asList(accountIds.split(","));
            accountIdList = accountIdList.stream()
                    .map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());

            if (accountIdList.isEmpty()) {
                log.error("[DOMS] No valid accountIds found after parsing");
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ModelApiResponse()
                        .message("At least one valid accountId is required"))
                        .build();
            }

            Map<String, String> result = accountMetadataService.getBatchDisclosureOptions(accountIdList);
            
            // Convert map to array of objects
            List<DisclosureOptionItem> responseList = result.entrySet().stream()
                    .map(entry -> new DisclosureOptionItem(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
            
            return Response.ok().entity(responseList).build();

        } catch (AccountMetadataException e) {
            log.error("[DOMS] Error batch retrieving disclosure options", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ModelApiResponse().message(
                    "Failed to retrieve disclosure options: " + e.getMessage()))
                    .build();
        }

    }

    /**
     * Adds disclosure options for multiple accounts.
     *
     * @param request list of disclosure option items to add
     * @return response with creation status
     */
    public static Response addDisclosureOptions(List<DisclosureOptionItem> request) {

        if (request == null || request.isEmpty()) {
            log.error("[DOMS] No disclosure options provided to add");
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ModelApiResponse()
                    .message("No disclosure options provided"))
                    .build();
        }

        try {
            Map<String, String> accountDisclosureMap = new HashMap<>();
            List<String> accountIdsToCheck = new ArrayList<>();

            // Validate and collect accounts
            for (DisclosureOptionItem item : request) {
                String disclosureOptionStatus = item.getDisclosureOption();
                if (isValidDOMSStatus(disclosureOptionStatus)) {
                    accountDisclosureMap.put(item.getAccountId(), disclosureOptionStatus);
                    accountIdsToCheck.add(item.getAccountId());
                } else {
                    log.error("[DOMS] Invalid disclosure option status for account: " +
                            item.getAccountId() + " - " + disclosureOptionStatus);
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(new ModelApiResponse().message(
                                    "Invalid disclosure option status provided for " + item.getAccountId() +
                                            ", Allowed values: pre-approval, no-sharing"))
                            .build();
                }
            }

            // Batch check for existing accounts
            Map<String, String> existingStatuses = accountMetadataService.getBatchDisclosureOptions(accountIdsToCheck);

            // Filter to only add new accounts
            Map<String, String> newAccounts = new HashMap<>();
            for (Map.Entry<String, String> entry : accountDisclosureMap.entrySet()) {
                if (!existingStatuses.containsKey(entry.getKey())) {
                    newAccounts.put(entry.getKey(), entry.getValue());
                }
            }

            List<String> existingAccountIds = accountIdsToCheck.stream()
                    .filter(existingStatuses::containsKey)
                    .collect(Collectors.toList());

            boolean anyExisted = !existingStatuses.isEmpty();

            String message = "";
            if (!newAccounts.isEmpty()) {
                accountMetadataService.addBatchDisclosureOptions(newAccounts);
                message = "added successfully for new account Id(s), ";
            }

            // Return 201 Created if all were new, 200 OK if any of account already existed
            if (anyExisted) {
                return Response.ok()
                        .entity(new ModelApiResponse().message(
                                "Disclosure options " + message + "already exist for account(s): "
                                        + String.join(", ", existingAccountIds)))
                        .build();
            } else {
                return Response.status(Response.Status.CREATED)
                        .entity(new ModelApiResponse().message(
                                "Disclosure options added successfully"))
                        .build();
            }

        } catch (AccountMetadataException e) {
            log.error("[DOMS] Failed to add disclosure options via AccountMetadataService", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ModelApiResponse().message("Failed to add disclosure options: "
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
    private static boolean isValidDOMSStatus(String status) {
        return status != null && (status.equalsIgnoreCase(CommonConstants.DOMS_STATUS_PRE_APPROVAL) ||
                status.equalsIgnoreCase(CommonConstants.DOMS_STATUS_NO_SHARING));
    }
}

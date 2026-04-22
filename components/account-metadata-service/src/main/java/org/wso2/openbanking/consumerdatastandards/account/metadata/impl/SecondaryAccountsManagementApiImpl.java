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
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.consumerdatastandards.account.metadata.exceptions.AccountMetadataException;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.ErrorResponse;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.SecondaryAccountInstructionItem;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.core.AccountMetadataService;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.core.AccountMetadataServiceImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

/**
 * Implementation for secondary account instruction management API operations.
 */
public class SecondaryAccountsManagementApiImpl {

    private static final Log log = LogFactory.getLog(SecondaryAccountsManagementApiImpl.class);

    private static final AccountMetadataService accountMetadataService = AccountMetadataServiceImpl.getInstance();

    private SecondaryAccountsManagementApiImpl() {
        // Prevent instantiation
    }

    /**
     * Updates secondary account instructions for multiple account-user combinations.
     *
     * @param request list of secondary account instruction items to update
     * @return response with list of updated items
     */
    public static Response updateSecondaryAccountInstructions(List<SecondaryAccountInstructionItem> request) {

        try {
            List<SecondaryAccountInstructionItem> validItems = validateRequest(request);
            List<Pair<String, String>> accountUserPairs = buildAccountUserPairs(validItems);

            List<SecondaryAccountInstructionItem> existingItems =
                accountMetadataService.getBatchSecondaryAccountInstructions(accountUserPairs);
            Set<String> existingKeys = existingItems.stream().map(SecondaryAccountsManagementApiImpl::buildCompositeKey)
                    .collect(Collectors.toSet());

            List<SecondaryAccountInstructionItem> itemsToUpdate = validItems.stream()
                    .filter(item -> existingKeys.contains(buildCompositeKey(item)))
                    .collect(Collectors.toList());

            if (!itemsToUpdate.isEmpty()) {
                List<SecondaryAccountInstructionItem> itemsRequiringConsentExpiry = itemsToUpdate.stream()
                        .filter(SecondaryAccountsManagementApiImpl::isConsentExpiryRequired)
                        .collect(Collectors.toList());

                if (!itemsRequiringConsentExpiry.isEmpty()) {
                    // TODO: Call Accelerator once with itemsRequiringConsentExpiry.
                }
                accountMetadataService.updateBatchSecondaryAccountInstructions(itemsToUpdate);
            }

            if (log.isDebugEnabled()) {
                log.debug("[Secondary Accounts] Updated secondary account instructions for " +
                        itemsToUpdate.size() + " item(s)");
            }

            return Response.status(Response.Status.OK).entity(itemsToUpdate).build();

        } catch (AccountMetadataException e) {
            log.error("[Secondary Accounts] Failed to update secondary account instructions", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse().errorDescription(
                            "Failed to update secondary account instructions: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Retrieves secondary account instructions for a single user and multiple accounts.
     *
     * @param accountIds comma-separated list of account IDs
     * @param userId user ID to retrieve instruction statuses for
     * @return response with secondary account instruction records
     */
    public static Response getSecondaryAccountInstructions(String accountIds, String userId) {

        if (StringUtils.isBlank(accountIds) || StringUtils.isBlank(userId)) {
            log.error("[Secondary Accounts] accountIds or userId are missing in get request");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse().errorDescription(
                            "At least one accountId and userId are required"))
                    .build();
        }

        List<String> accountIdList = Arrays.stream(accountIds.split(",")).map(StringUtils::trimToEmpty)
                .filter(StringUtils::isNotBlank).collect(Collectors.toList());

        if (accountIdList.isEmpty()) {
            log.error("[Secondary Accounts] No valid accountIds found after parsing");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse().errorDescription(
                            "At least one accountId and userId are required"))
                    .build();
        }

        try {
            List<Pair<String, String>> accountUserPairs = getSecondaryAccountInstructionPairs(userId, accountIdList);

            List<SecondaryAccountInstructionItem> result =
                accountMetadataService.getBatchSecondaryAccountInstructions(accountUserPairs);

            return Response.status(Response.Status.OK).entity(result).build();

        } catch (AccountMetadataException e) {
                log.error("[Secondary Accounts] Error retrieving secondary account instructions", e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(new ErrorResponse().errorDescription(
                                "Failed to retrieve secondary account instructions: " + e.getMessage()))
                        .build();
        }
    }

    /**
     * Builds account-user pairs for batch retrieval using a single user ID and a list of account IDs.
     *
     * @param userId user ID to attach to each accountId
     * @param accountIdList list of account IDs to query
     * @return list of account-user pairs with normalized user ID
     */
    private static List<Pair<String, String>> getSecondaryAccountInstructionPairs(
        String userId, List<String> accountIdList) {

        String normalizedUserId = StringUtils.trimToEmpty(userId);
        List<Pair<String, String>> accountUserPairs = new ArrayList<>();
        for (String accountId : accountIdList) {
            accountUserPairs.add(Pair.of(accountId, normalizedUserId));
        }
        return accountUserPairs;
    }

    /**
     * Adds secondary account instructions for multiple account-user combinations.
     * @param request list of secondary account instruction items to add
     * @return response with list of added items
     */
    public static Response addSecondaryAccountInstructions(List<SecondaryAccountInstructionItem> request) {

        try {
            List<SecondaryAccountInstructionItem> validItems = validateRequest(request);
            List<Pair<String, String>> accountUserPairs = buildAccountUserPairs(validItems);

            List<SecondaryAccountInstructionItem> existingItems =
                accountMetadataService.getBatchSecondaryAccountInstructions(accountUserPairs);
            Set<String> existingKeys = existingItems.stream().map(SecondaryAccountsManagementApiImpl::buildCompositeKey)
                    .collect(Collectors.toSet());

            List<SecondaryAccountInstructionItem> itemsToAdd = validItems.stream()
                    .filter(item -> !existingKeys.contains(buildCompositeKey(item)))
                    .collect(Collectors.toList());

            List<SecondaryAccountInstructionItem> itemsToUpdate = validItems.stream()
                    .filter(item -> existingKeys.contains(buildCompositeKey(item)))
                    .collect(Collectors.toList());

            if (!itemsToAdd.isEmpty()) {
                accountMetadataService.addBatchSecondaryAccountInstructions(itemsToAdd);
            }

            if (!itemsToUpdate.isEmpty()) {
                accountMetadataService.updateBatchSecondaryAccountInstructions(itemsToUpdate);
            }

            if (log.isDebugEnabled()) {
                log.debug("[Secondary Accounts] Added secondary account instructions for " +
                        itemsToAdd.size() + " item(s) and updated " + itemsToUpdate.size() + " item(s)");
            }

            // Combine results and return 201 Created if any new items added, 200 OK otherwise
            List<SecondaryAccountInstructionItem> responseItems = new ArrayList<>(itemsToAdd);
            responseItems.addAll(itemsToUpdate);
            Response.ResponseBuilder responseBuilder = itemsToAdd.isEmpty() ? 
                    Response.status(Response.Status.OK) : Response.status(Response.Status.CREATED);
            
            return responseBuilder.entity(responseItems).build();

        } catch (AccountMetadataException e) {
            log.error("[Secondary Accounts] Failed to add secondary account instructions", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse().errorDescription(
                            "Failed to add secondary account instructions: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Validates mandatory fields, validates instruction status, normalizes values,
     * and rejects duplicate request items by accountId and secondaryUserId.
     *
     * @param request request items to validate and normalize
     * @return validated and normalized items
     * @throws AccountMetadataException if request contains invalid items
     */
    private static List<SecondaryAccountInstructionItem> validateRequest(List<SecondaryAccountInstructionItem> request)
            throws AccountMetadataException {

        Map<String, SecondaryAccountInstructionItem> validatedItemsByCompositeKey = new HashMap<>();

        for (SecondaryAccountInstructionItem item : request) {
            String accountId = StringUtils.trimToEmpty(item.getAccountId());
            String secondaryUserId = StringUtils.trimToEmpty(item.getSecondaryUserId());

            if (StringUtils.isBlank(accountId) || StringUtils.isBlank(secondaryUserId)) {
                throw new AccountMetadataException("accountId and secondaryUserId are required");
            }

            item.setAccountId(accountId);
            item.setSecondaryUserId(secondaryUserId);
            String compositeKey = buildCompositeKey(item);
            if (validatedItemsByCompositeKey.containsKey(compositeKey)) {
                throw new AccountMetadataException(
                        "Duplicate secondary account instruction found for accountId " + accountId +
                                " and secondaryUserId " + secondaryUserId);
            }
            validatedItemsByCompositeKey.put(compositeKey, item);
        }

        return new ArrayList<>(validatedItemsByCompositeKey.values());
    }

    private static List<Pair<String, String>> buildAccountUserPairs(List<SecondaryAccountInstructionItem> items) {
        List<Pair<String, String>> accountUserPairs = new ArrayList<>();
        for (SecondaryAccountInstructionItem item : items) {
            accountUserPairs.add(Pair.of(item.getAccountId(), item.getSecondaryUserId()));
        }
        return accountUserPairs;
    }

    /**
     * Builds the composite key used for deduplication and existence checks.
     *
     * @param item secondary account instruction item
     * @return composite key in accountId::secondaryUserId format
     */
    private static String buildCompositeKey(SecondaryAccountInstructionItem item) {
        return item.getAccountId() + "::" + item.getSecondaryUserId();
    }

    /**
     * Checks whether a secondary account instruction update should trigger consent expiry.
     *
     * @param item secondary account instruction item
     * @return true when status is inactive and otherAccountsAvailability is false
     */
    private static boolean isConsentExpiryRequired(SecondaryAccountInstructionItem item) {
        return item != null && Boolean.FALSE.equals(item.getOtherAccountsAvailability())
                && SecondaryAccountInstructionItem.SecondaryAccountInstructionStatusEnum.inactive
                        .equals(item.getSecondaryAccountInstructionStatus());
    }
}

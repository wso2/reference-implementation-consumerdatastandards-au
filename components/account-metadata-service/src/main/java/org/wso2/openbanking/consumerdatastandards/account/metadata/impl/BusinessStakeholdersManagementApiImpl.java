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
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.BusinessStakeholderDeleteItem;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.BusinessStakeholderItem;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.BusinessStakeholderPermissionItem;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.BusinessStakeholderRepresentative;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.ErrorResponse;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.core.AccountMetadataService;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.core.AccountMetadataServiceImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

/**
 * Implementation for business stakeholder management API operations.
 */
public class BusinessStakeholdersManagementApiImpl {

    private static final Log log = LogFactory.getLog(BusinessStakeholdersManagementApiImpl.class);

    private static final AccountMetadataService accountMetadataService = AccountMetadataServiceImpl.getInstance();

    private BusinessStakeholdersManagementApiImpl() {
        // Prevent instantiation
    }

    /**
     * Adds business stakeholder permission records for account-user combinations.
     *
     * @param request list of business stakeholder upsert records
     * @return response with list of account IDs where records were added
     */
    public static Response addBusinessStakeholders(List<BusinessStakeholderItem> request) {

        List<BusinessStakeholderPermissionItem> validItems;
        try {
            validItems = validateRequest(request);
        } catch (AccountMetadataException e) {
            return sendBadRequest(e.getMessage());
        }

        if (validItems.isEmpty()) {
            return Response.status(Response.Status.OK).entity(new ArrayList<>()).build();
        }

        try {
            List<BusinessStakeholderPermissionItem> existingItems =
                    accountMetadataService.getBatchBusinessStakeholderPermissions(toAccountUserPairs(validItems));
            Set<String> existingKeys = existingItems.stream().map(BusinessStakeholdersManagementApiImpl::buildKey)
                    .collect(Collectors.toSet());

            List<BusinessStakeholderPermissionItem> itemsToAdd = validItems.stream()
                    .filter(item -> !existingKeys.contains(buildKey(item)))
                    .collect(Collectors.toList());

            if (!itemsToAdd.isEmpty()) {
                accountMetadataService.addBatchBusinessStakeholderPermissions(itemsToAdd);
            }

            Set<String> addedAccountIds = itemsToAdd.stream()
                    .map(BusinessStakeholderPermissionItem::getAccountId)
                    .collect(Collectors.toSet());
            List<BusinessStakeholderItem> addedRequestItems = request.stream()
                    .filter(item ->
                            addedAccountIds.contains(StringUtils.trimToEmpty(item.getAccountID())))
                    .collect(Collectors.toList());

            Response.ResponseBuilder responseBuilder = itemsToAdd.isEmpty() ?
                    Response.status(Response.Status.OK) : Response.status(Response.Status.CREATED);
            return responseBuilder.entity(addedRequestItems).build();

        } catch (AccountMetadataException e) {
            log.error("[Business Stakeholders] Failed to add business stakeholder records", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse().errorDescription(
                            "Failed to add business stakeholder records: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Retrieves business stakeholder permissions for a single user and multiple accounts.
     *
     * @param accountIds comma-separated list of account IDs
     * @param userId user ID to retrieve permissions for
     * @return response with business stakeholder permission records
     */
    public static Response getBusinessStakeholders(String accountIds, String userId) {

        if (StringUtils.isBlank(accountIds) || StringUtils.isBlank(userId)) {
            return sendBadRequest("At least one accountId and userId are required");
        }

        List<String> accountIdList = Arrays.stream(accountIds.split(","))
                .map(StringUtils::trimToEmpty).filter(StringUtils::isNotBlank).collect(Collectors.toList());

        if (accountIdList.isEmpty()) {
            return sendBadRequest("At least one accountId and userId are required");
        }

        try {
            List<Pair<String, String>> queryPairs =
                    getBusinessStakeholderPermissionItems(userId, accountIdList);

            List<BusinessStakeholderPermissionItem> result =
                    accountMetadataService.getBatchBusinessStakeholderPermissions(queryPairs);

            return Response.status(Response.Status.OK).entity(result).build();

        } catch (AccountMetadataException e) {
            log.error("[Business Stakeholders] Failed to retrieve business stakeholder permissions", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse().errorDescription(
                            "Failed to retrieve business stakeholder permissions: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Updates business stakeholder permission records for existing account-user combinations.
     *
     * @param request list of business stakeholder upsert records
     * @return response with list of account IDs where records were updated
     */
    public static Response updateBusinessStakeholders(List<BusinessStakeholderItem> request) {

        List<BusinessStakeholderPermissionItem> validItems;
        try {
            validItems = validateRequest(request);
        } catch (AccountMetadataException e) {
            return sendBadRequest(e.getMessage());
        }

        if (validItems.isEmpty()) {
            return Response.status(Response.Status.OK).entity(new ArrayList<>()).build();
        }

        try {
            List<BusinessStakeholderPermissionItem> existingItems =
                    accountMetadataService.getBatchBusinessStakeholderPermissions(toAccountUserPairs(validItems));
            Set<String> existingKeys = existingItems.stream().map(BusinessStakeholdersManagementApiImpl::buildKey)
                    .collect(Collectors.toSet());

            List<BusinessStakeholderPermissionItem> itemsToUpdate = validItems.stream()
                    .filter(item -> existingKeys.contains(buildKey(item)))
                    .collect(Collectors.toList());

            if (!itemsToUpdate.isEmpty()) {
                accountMetadataService.updateBatchBusinessStakeholderPermissions(itemsToUpdate);
            }

            Set<String> updatedAccountIds = itemsToUpdate.stream()
                    .map(BusinessStakeholderPermissionItem::getAccountId)
                    .collect(Collectors.toSet());
            List<BusinessStakeholderItem> updatedRequestItems = request.stream()
                    .filter(item -> updatedAccountIds.contains(StringUtils.trimToEmpty(item.getAccountID())))
                    .collect(Collectors.toList());

            return Response.status(Response.Status.OK).entity(updatedRequestItems).build();

        } catch (AccountMetadataException e) {
            log.error("[Business Stakeholders] Failed to update business stakeholder records", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse().errorDescription(
                            "Failed to update business stakeholder records: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Deletes business stakeholder permission records for existing account-user combinations.
     *
     * @param request list of business stakeholder delete records
     * @return response with list of account IDs where records were deleted
     */
    public static Response deleteBusinessStakeholders(List<BusinessStakeholderDeleteItem> request) {

        List<BusinessStakeholderPermissionItem> validItems;
        try {
            validItems = validateDeleteRequest(request);
        } catch (AccountMetadataException e) {
            return sendBadRequest(e.getMessage());
        }

        if (validItems.isEmpty()) {
            return Response.status(Response.Status.OK).entity(new ArrayList<>()).build();
        }

        try {

            List<BusinessStakeholderPermissionItem> existingItems =
                    accountMetadataService.getBatchBusinessStakeholderPermissions(toAccountUserPairs(validItems));
            Set<String> existingKeys = existingItems.stream().map(BusinessStakeholdersManagementApiImpl::buildKey)
                    .collect(Collectors.toSet());

            List<BusinessStakeholderPermissionItem> missingItems = validItems.stream()
                    .filter(item -> !existingKeys.contains(buildKey(item)))
                    .collect(Collectors.toList());

            if (!missingItems.isEmpty()) {
                log.error("[Business Stakeholders] No records found for requested items");
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse().errorDescription("No records found for requested items"))
                    .build();
            }

            List<BusinessStakeholderPermissionItem> itemsToRevoke = validItems.stream()
                .filter(item -> existingKeys.contains(buildKey(item)))
                .map(item -> new BusinessStakeholderPermissionItem(
                        item.getAccountId(), item.getUserId(), BusinessStakeholderPermissionItem.PermissionEnum.REVOKE))
                .collect(Collectors.toList());

            if (!itemsToRevoke.isEmpty()) {
                accountMetadataService.updateBatchBusinessStakeholderPermissions(itemsToRevoke);
            }

            // After Revoking the permissions Checking whether any AUTHORIZE permission BNRs are left,
            // if none are there Deleting all the permissions records to that account.
            List<String> affectedAccountIds = getAccountIds(itemsToRevoke);
            if (affectedAccountIds.isEmpty()) {
                return Response.status(Response.Status.OK).entity(new ArrayList<>()).build();
            }

            List<BusinessStakeholderPermissionItem> accountPermissions =
                accountMetadataService.getBatchBusinessStakeholderPermissionsByAccountIds(affectedAccountIds);

            Set<String> accountsWithAuthorizePermission = accountPermissions.stream()
                    .filter(BusinessStakeholdersManagementApiImpl::isAuthorizePermission)
                    .map(BusinessStakeholderPermissionItem::getAccountId)
                    .collect(Collectors.toSet());

            List<BusinessStakeholderPermissionItem> itemsToDelete = accountPermissions.stream()
                    .filter(item ->
                            !accountsWithAuthorizePermission.contains(item.getAccountId()))
                    .collect(Collectors.toList());

            if (!itemsToDelete.isEmpty()) {
                accountMetadataService.deleteBatchBusinessStakeholderPermissions(itemsToDelete);
            }

            Set<String> revokedAccountIds = itemsToRevoke.stream().map(BusinessStakeholderPermissionItem::getAccountId)
                    .collect(Collectors.toSet());
            List<BusinessStakeholderDeleteItem> revokedRequestItems = request.stream()
                    .filter(item ->
                            revokedAccountIds.contains(StringUtils.trimToEmpty(item.getAccountID())))
                    .collect(Collectors.toList());

            return Response.status(Response.Status.OK).entity(revokedRequestItems).build();

        } catch (AccountMetadataException e) {
            log.error("[Business Stakeholders] Failed to delete business stakeholder records", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse().errorDescription(
                            "Failed to delete business stakeholder records: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Returns true if the permission on the given item is AUTHORIZE.
     *
     * @param permissionItem the permission item to check
     * @return true if permission is AUTHORIZE, false otherwise
     */
    private static boolean isAuthorizePermission(BusinessStakeholderPermissionItem permissionItem) {
        return BusinessStakeholderPermissionItem.PermissionEnum.AUTHORIZE.equals(permissionItem.getPermission());
    }

    /**
     * Validates and flattens the add/update request payload into account-user-permission records.
     * Throws an exception on missing required fields, invalid permissions, or duplicate account-user pairs.
     *
     * @param request list of business stakeholder upsert items
     * @return flat list of validated permission items
     * @throws AccountMetadataException if any item is invalid or a duplicate pair is found
     */
    private static List<BusinessStakeholderPermissionItem> validateRequest(
            List<BusinessStakeholderItem> request) throws AccountMetadataException {

        Map<String, BusinessStakeholderPermissionItem> validatedItems = new LinkedHashMap<>();

        for (BusinessStakeholderItem requestItem : request) {

            if (requestItem == null) {
                throw new AccountMetadataException("Request contains null business stakeholder item");
            }

            String accountId = StringUtils.trimToEmpty(requestItem.getAccountID());

            List<String> accountOwners = requestItem.getAccountOwners();
            // Add account owners with VIEW permission
            if (accountOwners != null) {
                for (String owner : accountOwners) {
                    String ownerId = StringUtils.trimToEmpty(owner);
                    BusinessStakeholderPermissionItem permissionItem = new BusinessStakeholderPermissionItem(
                            accountId, ownerId, BusinessStakeholderPermissionItem.PermissionEnum.VIEW);
                    String key = buildKey(permissionItem);
                    if (validatedItems.containsKey(key)) {
                        throw new AccountMetadataException(
                                "Duplicate entry for accountID " + accountId + " and user " + ownerId);
                    }
                    validatedItems.put(key, permissionItem);
                }
            }

            // Add nominated representatives
            List<BusinessStakeholderRepresentative> nominatedRepresentatives =
                    requestItem.getNominatedRepresentatives();

            for (BusinessStakeholderRepresentative representative : nominatedRepresentatives) {

                String userId = StringUtils.trimToEmpty(representative.getName());
                if (StringUtils.isBlank(userId)) {
                    throw new AccountMetadataException(
                            "Representative name is required for accountID " + accountId);
                }
                BusinessStakeholderPermissionItem.PermissionEnum permission =
                        BusinessStakeholderPermissionItem.PermissionEnum.fromValue(
                                representative.getPermission().value());

                BusinessStakeholderPermissionItem permissionItem =
                    new BusinessStakeholderPermissionItem(accountId, userId, permission);
                String key = buildKey(permissionItem);
                if (validatedItems.containsKey(key)) {
                    throw new AccountMetadataException(
                            "Duplicate entry for accountID " + accountId + " and user " + userId);
                }
                validatedItems.put(key, permissionItem);
            }
        }

        return new ArrayList<>(validatedItems.values());
    }

    /**
     * Validates and flattens the delete request payload into account-user records.
     * Throws an exception on missing required fields or duplicate account-user pairs.
     *
     * @param request list of business stakeholder delete items
     * @return flat list of validated permission items (without permission value)
     * @throws AccountMetadataException if any item is invalid or a duplicate pair is found
     */
    private static List<BusinessStakeholderPermissionItem> validateDeleteRequest(
            List<BusinessStakeholderDeleteItem> request) throws AccountMetadataException {

        Map<String, BusinessStakeholderPermissionItem> validatedItems = new LinkedHashMap<>();

        for (BusinessStakeholderDeleteItem requestItem : request) {

            if (requestItem == null) {
                throw new AccountMetadataException("Request contains null business stakeholder item");
            }

            String accountId = StringUtils.trimToEmpty(requestItem.getAccountID());

            List<String> accountOwners = requestItem.getAccountOwners();
            if (accountOwners != null) {
                for (String owner : accountOwners) {
                    String ownerId = StringUtils.trimToEmpty(owner);
                    BusinessStakeholderPermissionItem permissionItem =
                            new BusinessStakeholderPermissionItem(accountId, ownerId, null);
                    String key = buildKey(permissionItem);
                    if (validatedItems.containsKey(key)) {
                        throw new AccountMetadataException(
                                "Duplicate entry for accountID " + accountId + " and user " + ownerId);
                    }
                    validatedItems.put(key, permissionItem);
                }
            }

            List<String> nominatedRepresentatives = requestItem.getNominatedRepresentatives();
            for (String representative : nominatedRepresentatives) {
                String userId = StringUtils.trimToEmpty(representative);
                if (StringUtils.isBlank(userId)) {
                    throw new AccountMetadataException(
                            "Representative name is required for accountID " + accountId);
                }
                BusinessStakeholderPermissionItem permissionItem =
                    new BusinessStakeholderPermissionItem(accountId, userId, null);
                String key = buildKey(permissionItem);
                if (validatedItems.containsKey(key)) {
                    throw new AccountMetadataException(
                            "Duplicate entry for accountID " + accountId + " and user " + userId);
                }
                validatedItems.put(key, permissionItem);
            }
        }

        return new ArrayList<>(validatedItems.values());
    }

    /**
     * Builds account-user pairs for batch retrieval using a single user ID and a list of account IDs.
     *
     * @param userId user ID to pair with each account ID
     * @param accountIdList list of account IDs
     * @return list of (accountId, userId) pairs
     */
    private static List<Pair<String, String>> getBusinessStakeholderPermissionItems(
            String userId, List<String> accountIdList) {

        String normalizedUserId = StringUtils.trimToEmpty(userId);
        List<Pair<String, String>> queryPairs = new ArrayList<>();
        for (String accountId : accountIdList) {
            queryPairs.add(Pair.of(accountId, normalizedUserId));
        }
        return queryPairs;
    }

    /**
     * Converts a list of permission items into (accountId, userId) pairs for use in batch queries.
     *
     * @param items list of business stakeholder permission items
     * @return list of (accountId, userId) pairs
     */
    private static List<Pair<String, String>> toAccountUserPairs(List<BusinessStakeholderPermissionItem> items) {
        List<Pair<String, String>> pairs = new ArrayList<>();
        for (BusinessStakeholderPermissionItem item : items) {
            pairs.add(Pair.of(item.getAccountId(), item.getUserId()));
        }
        return pairs;
    }

    /**
     * Builds a composite deduplication key in the format {@code accountId::userId}.
     *
     * @param item the permission item
     * @return composite key string
     */
    private static String buildKey(BusinessStakeholderPermissionItem item) {
        return item.getAccountId() + "::" + item.getUserId();
    }

    /**
     * Returns a distinct list of account IDs from the given permission items.
     *
     * @param items list of permission items
     * @return distinct list of account IDs
     */
    private static List<String> getAccountIds(List<BusinessStakeholderPermissionItem> items) {
        return items.stream().map(BusinessStakeholderPermissionItem::getAccountId)
                .distinct().collect(Collectors.toList());
    }

    /**
     * Logs and returns a 400 Bad Request response with the given error message.
     *
     * @param message the error description
     * @return 400 Bad Request response
     */
    private static Response sendBadRequest(String message) {
        log.error("[Business Stakeholders] " + message);
        return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse().errorDescription(message))
                .build();
    }
}

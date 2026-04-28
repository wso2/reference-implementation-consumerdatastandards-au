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
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.openbanking.consumerdatastandards.account.metadata.configurations.ConfigurableProperties;
import org.wso2.openbanking.consumerdatastandards.account.metadata.constants.CommonConstants;
import org.wso2.openbanking.consumerdatastandards.account.metadata.exceptions.AccountMetadataException;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.ErrorResponse;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.LegalEntitySharingItem;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.core.AccountMetadataService;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.core.AccountMetadataServiceImpl;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

/**
 * Implementation for legal entity sharing API operations.
 */
public class CeasingSecondaryUserSharingApiImpl {

    private static final Log log = LogFactory.getLog(CeasingSecondaryUserSharingApiImpl.class);

    private static final AccountMetadataService accountMetadataService = AccountMetadataServiceImpl.getInstance();

    private static final CloseableHttpClient httpClient;
    private static final String IS_BASIC_AUTH_HEADER;

    private static final long LEGAL_ENTITY_CACHE_TTL_MS = TimeUnit.HOURS.toMillis(1);
    private static final Map<String, LegalEntityCacheEntry> LEGAL_ENTITY_CACHE = new ConcurrentHashMap<>();

    private static class LegalEntityCacheEntry {
        final String legalEntityId;
        final long expiryMs;

        LegalEntityCacheEntry(String legalEntityId) {
            this.legalEntityId = legalEntityId;
            this.expiryMs = System.currentTimeMillis() + LEGAL_ENTITY_CACHE_TTL_MS;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiryMs;
        }
    }

    static {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000)
                .setSocketTimeout(10000)
                .build();
        httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();

        String credentials = ConfigurableProperties.IS_USERNAME + ":" + ConfigurableProperties.IS_PASSWORD;
        IS_BASIC_AUTH_HEADER = CommonConstants.BASIC_TAG
                + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    private CeasingSecondaryUserSharingApiImpl() {
        // Prevent instantiation
    }

    /**
     * Updates legal entity sharing statuses for one or more account-user records.
     * Items with status {@code blocked} are inserted into fs_account_blocked_legal_entity (idempotent upsert).
     * Items with status {@code active} delete the corresponding row if it exists.
     *
     * @param request list of legal entity sharing records to update
     * @return response with list of processed records
     */
    public static Response updateLegalEntitySharingStatus(List<LegalEntitySharingItem> request) {

        List<LegalEntitySharingItem> validItems;
        try {
            validItems = validateRequest(request);
        } catch (AccountMetadataException e) {
            return sendBadRequest(e.getMessage());
        }

        try {
            if (validItems.isEmpty()) {
                return Response.status(Response.Status.OK).entity(new ArrayList<>()).build();
            }

            accountMetadataService.upsertBatchLegalEntitySharingStatuses(validItems);
            return Response.status(Response.Status.OK).entity(validItems).build();

        } catch (AccountMetadataException e) {
            log.error("[Legal Entity Sharing] Failed to update legal entity sharing statuses", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse().errorDescription(
                            "Failed to update legal entity sharing statuses: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Retrieves blocked legal entity sharing records for one user and multiple accounts.
     * When {@code clientId} is provided, the legal entity ID is resolved from the IS server and only rows
     * matching that legal entity are returned.
     *
     * @param accountIds comma-separated account IDs
     * @param userId     user ID
     * @param clientId   software product client ID to resolve the legal entity ID
     * @return response with blocked legal entity sharing records
     */
    public static Response getLegalEntitySharingStatus(String accountIds, String userId, String clientId) {

        if (StringUtils.isBlank(clientId)) {
            return sendBadRequest("clientId is required");
        }

        List<String> accountIdList = Arrays.stream(accountIds.split(",")).map(StringUtils::trimToEmpty)
            .filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());

        // Resolve the legal entity ID from IS when a clientId is supplied
        String resolvedLegalEntityId = null;
        try {
            resolvedLegalEntityId = resolveLegalEntityId(clientId);
            if (StringUtils.isBlank(resolvedLegalEntityId)) {
                throw new AccountMetadataException("No legal entity ID found for clientId: " + clientId);
            }
        } catch (AccountMetadataException e) {
            log.error("[Legal Entity Sharing] Failed to resolve legal entity ID for clientId: " + clientId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse().errorDescription(
                            "Failed to resolve legal entity ID: " + e.getMessage()))
                    .build();
        }

        try {
            String normalizedUserId = StringUtils.trimToEmpty(userId);
            List<Pair<String, String>> accountUserPairs = new ArrayList<>();
            for (String accountId : accountIdList) {
                accountUserPairs.add(Pair.of(accountId, normalizedUserId));
            }

            List<LegalEntitySharingItem> items =
                    accountMetadataService.getBatchLegalEntitySharingStatuses(accountUserPairs);

            final String filterEntityId = resolvedLegalEntityId;
            Map<String, LegalEntitySharingItem.LegalEntitySharingStatusEnum> statusByAccount =
                    new LinkedHashMap<>();
            for (String accountId : accountIdList) {
                statusByAccount.put(accountId, LegalEntitySharingItem.LegalEntitySharingStatusEnum.active);
            }

            for (LegalEntitySharingItem item : items) {
                if (item == null || !filterEntityId.equalsIgnoreCase(item.getLegalEntityID())) {
                    continue;
                }
                if (statusByAccount.containsKey(item.getAccountID())) {
                    statusByAccount.put(item.getAccountID(), item.getLegalEntitySharingStatus());
                }
            }

            List<LegalEntitySharingItem> responseItems = new ArrayList<>();
            for (String accountId : accountIdList) {
                LegalEntitySharingItem responseItem = new LegalEntitySharingItem();
                responseItem.setAccountID(accountId);
                responseItem.setSecondaryUserID(normalizedUserId);
                responseItem.setLegalEntityID(filterEntityId);
                responseItem.setLegalEntitySharingStatus(statusByAccount.get(accountId));
                responseItems.add(responseItem);
            }
            items = responseItems;

            return Response.status(Response.Status.OK).entity(items).build();

        } catch (AccountMetadataException e) {
            log.error("[Legal Entity Sharing] Failed to retrieve legal entity sharing statuses", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse().errorDescription(
                            "Failed to retrieve legal entity sharing statuses: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Returns the legal entity ID for the given client ID, using a 1-hour in-memory cache to avoid
     * repeated calls to the IS server.
     *
     * @param clientId software product client ID
     * @return legal entity ID
     */
    private static String resolveLegalEntityId(String clientId) throws AccountMetadataException {
        LegalEntityCacheEntry cached = LEGAL_ENTITY_CACHE.get(clientId);
        
        if (cached != null && !cached.isExpired()) {
            return cached.legalEntityId;
        }
        String legalEntityId = fetchLegalEntityIdByClientId(clientId);
        LEGAL_ENTITY_CACHE.put(clientId, new LegalEntityCacheEntry(legalEntityId));

        return legalEntityId;
    }

    /**
     * Calls the IS applications endpoint to resolve the legal entity ID for the given client ID.
     * Uses credentials from {@link ConfigurableProperties}.
     *
     * @param clientId software product client ID
     * @return legal entity ID, or empty string if not found
     */
    private static String fetchLegalEntityIdByClientId(String clientId) throws AccountMetadataException {

        try {
            String filterParam = URLEncoder.encode(
                    CommonConstants.CLIENT_ID_FILTER_PREFIX + clientId, StandardCharsets.UTF_8);
            String attributesParam = URLEncoder.encode(
                    CommonConstants.ADVANCED_CONFIGURATIONS_TAG, StandardCharsets.UTF_8);
            String requestUrl = ConfigurableProperties.IS_APPLICATIONS_ENDPOINT + "?" + CommonConstants.FILTER_TAG + "="
                    + filterParam + "&" + CommonConstants.ATTRIBUTES_TAG + "=" + attributesParam;

            HttpGet request = new HttpGet(requestUrl);
            request.addHeader(CommonConstants.ACCEPT_TAG, CommonConstants.JSON_CONTENT_TYPE);
            request.addHeader(CommonConstants.AUTH_HEADER, IS_BASIC_AUTH_HEADER);

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    String errorMessage = "IS applications service returned HTTP " + statusCode;
                    log.error(errorMessage);
                    throw new AccountMetadataException(errorMessage);
                }

                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                return parseLegalEntityIdFromIsResponse(responseBody);
            }
        } catch (IOException e) {
            String errorMessage = "[LegalEntity] Error calling IS applications service";
            log.error(errorMessage, e);
            throw new AccountMetadataException(errorMessage, e);
        }
    }

    /**
     * Parses the legal entity ID from the IS applications service response body.
     *
     * @param responseBody the JSON response body as a string
     * @return the legal entity ID if found, or an empty string if not present
     * @throws AccountMetadataException if the response is malformed
     */
    private static String parseLegalEntityIdFromIsResponse(String responseBody) throws AccountMetadataException {

        JSONObject responseJson;
        try {
            responseJson = new JSONObject(responseBody);
        } catch (JSONException e) {
            String errorMessage = "Invalid IS applications service response for retrieving legal entity ID";
            log.error(errorMessage, e);
            throw new AccountMetadataException(errorMessage, e);
        }

        JSONArray applications = responseJson.optJSONArray(CommonConstants.APPLICATIONS_TAG);
        if (applications == null || applications.length() == 0) {
            return StringUtils.EMPTY;
        }

        JSONObject application = applications.optJSONObject(0);
        JSONObject advancedConfigurations = application.optJSONObject(CommonConstants.ADVANCED_CONFIGURATIONS_TAG);
        if (advancedConfigurations == null) {
            return StringUtils.EMPTY;
        }

        JSONArray additionalSpProperties = advancedConfigurations
                .optJSONArray(CommonConstants.ADDITIONAL_SP_PROPERTIES_TAG);
        if (additionalSpProperties == null) {
            String errorMessage = "No additional SP properties found in IS applications response";
            log.error(errorMessage);
            throw new AccountMetadataException(errorMessage);
        }

        for (int i = 0; i < additionalSpProperties.length(); i++) {
            JSONObject property = additionalSpProperties.optJSONObject(i);
            if (property == null) {
                continue;
            }
            String name = property.optString(CommonConstants.PROPERTY_NAME_TAG, StringUtils.EMPTY);
            if (CommonConstants.LEGAL_ENTITY_ID_PROPERTY_NAME.equalsIgnoreCase(name)) {
                return property.optString(CommonConstants.PROPERTY_VALUE_TAG, StringUtils.EMPTY);
            }
        }

        String errorMessage = "legal_entity_id not found in IS application additional SP properties";
        log.error(errorMessage);
        throw new AccountMetadataException(errorMessage);
    }

    /**
     * Validates the incoming request items, ensuring that there are no duplicate
     * (accountID, secondaryUserID, legalEntityID) combinations.
     *
     * @param request raw list of legal entity sharing items from the caller
     * @return list of validated and trimmed items ready for processing
     * @throws AccountMetadataException if a duplicate entry is detected
     */
    private static List<LegalEntitySharingItem> validateRequest(List<LegalEntitySharingItem> request)
            throws AccountMetadataException {
        Set<String> seenKeys = new LinkedHashSet<>();
        List<LegalEntitySharingItem> validatedItems = new ArrayList<>();

        for (LegalEntitySharingItem item : request) {
            String accountId = StringUtils.trimToEmpty(item.getAccountID());
            String secondaryUserId = StringUtils.trimToEmpty(item.getSecondaryUserID());
            String legalEntityId = StringUtils.trimToEmpty(item.getLegalEntityID());

            item.setAccountID(accountId);
            item.setSecondaryUserID(secondaryUserId);
            item.setLegalEntityID(legalEntityId);

            String key = accountId + "::" + secondaryUserId + "::" + legalEntityId;
            if (!seenKeys.add(key)) {
                throw new AccountMetadataException("Duplicate entry for accountID=" + accountId
                        + ", secondaryUserID=" + secondaryUserId + ", legalEntityID=" + legalEntityId);
            }
            validatedItems.add(item);
        }

        return validatedItems;
    }

    /**
     * Logs the given message at error level and returns a 400 Bad Request response.
     *
     * @param message human-readable description of the validation error
     * @return 400 Bad Request response containing the error description
     */
    private static Response sendBadRequest(String message) {
        log.error("[Legal Entity Sharing] " + message);
        return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse().errorDescription(message))
                .build();
    }
}

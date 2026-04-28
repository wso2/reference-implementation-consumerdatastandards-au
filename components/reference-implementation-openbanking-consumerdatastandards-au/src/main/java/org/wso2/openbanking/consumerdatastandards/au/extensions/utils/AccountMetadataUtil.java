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

package org.wso2.openbanking.consumerdatastandards.au.extensions.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.wso2.openbanking.consumerdatastandards.au.extensions.configurations.ConfigurableProperties;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.CdsErrorEnum;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.CommonConstants;
import org.wso2.openbanking.consumerdatastandards.au.extensions.exceptions.CdsConsentException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility class to interact with the Account Metadata Webapp for DOMS status retrieval.
 * Supports both single account and batch POST requests for multiple accounts.
 * All requests include Basic Authentication.
 */
public class AccountMetadataUtil {

    private static final Log log = LogFactory.getLog(AccountMetadataUtil.class);

    /**
     * Private constructor to prevent instantiation.
     */
    private AccountMetadataUtil() {
    }

    /**
     * Retrieve DOMS statuses for multiple accounts from the Account Metadata Webapp.
     * Calls GET /disclosure-options with comma-separated account IDs as query parameter.
     *
     * @param accountIds list of account IDs to retrieve DOMS statuses for
     * @return a Map of accountId to DOMS Status, or null if retrieval fails
     */
    public static Map<String, String> getDOMSStatusesForAccounts(List<String> accountIds) throws CdsConsentException {

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(ConfigurableProperties.ACCOUNT_METADATA_WEBAPP_CONNECT_TIMEOUT_MILLIS)
                .setSocketTimeout(ConfigurableProperties.ACCOUNT_METADATA_WEBAPP_SOCKET_TIMEOUT_MILLIS)
                .build();

        try (CloseableHttpClient client = HttpClients.custom().setDefaultRequestConfig(requestConfig).build()) {
            // Build URL with comma-separated account IDs as query parameter
            String baseUrl = buildDisclosureOptionsUrl();
            String accountIdParam = String.join(",", accountIds);
            
            URIBuilder uriBuilder = new URIBuilder(baseUrl);
            uriBuilder.addParameter(CommonConstants.ACCOUNT_IDS, accountIdParam);
            
            HttpGet request = new HttpGet(uriBuilder.build());
            request.addHeader(CommonConstants.ACCEPT_HEADER_NAME, CommonConstants.ACCEPT_HEADER_VALUE);
            request.addHeader(CommonConstants.ACCEPT_CONTENT_NAME, CommonConstants.ACCEPT_CONTENT_VALUE_JSON);
            addBasicAuthHeader(request);

            HttpResponse response = client.execute(request);

            if (response.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK) {
                int statusCode = response.getStatusLine().getStatusCode();
                log.error("Failed to retrieve DOMS statuses for accounts, HTTP Status: " + statusCode);
                return null;
            }

            InputStream in = response.getEntity().getContent();
            String responseBody = IOUtils.toString(in, String.valueOf(StandardCharsets.UTF_8));

            return extractDOMSStatusesFromBatchResponse(responseBody);

        } catch (IOException | URISyntaxException e) {
            log.error("Failed to retrieve DOMS statuses for batch accounts", e);
            throw new CdsConsentException(CdsErrorEnum.UNEXPECTED_ERROR,
                    "Failed to retrieve DOMS statuses");
        }
    }

    /**
     * Retrieve secondary account instruction statuses for multiple accounts and a secondary user.
     * Calls GET /secondary-accounts with comma-separated account IDs and userId as query parameters.
     *
     * @param accountIds list of account IDs
     * @param secondaryUserId secondary user ID
     * @return map of accountId to instruction status, or empty map when retrieval fails
     */
    public static Map<String, String> getSecondaryAccountInstructionStatusesForAccounts(
            List<String> accountIds, String secondaryUserId) throws CdsConsentException {

        Map<String, String> instructionStatusMap = new HashMap<>();

        if (accountIds == null || accountIds.isEmpty() || StringUtils.isBlank(secondaryUserId)) {
            return instructionStatusMap;
        }

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(ConfigurableProperties.ACCOUNT_METADATA_WEBAPP_CONNECT_TIMEOUT_MILLIS)
                .setSocketTimeout(ConfigurableProperties.ACCOUNT_METADATA_WEBAPP_SOCKET_TIMEOUT_MILLIS)
                .build();

        try (CloseableHttpClient client = HttpClients.custom().setDefaultRequestConfig(requestConfig).build()) {
            String baseUrl = buildSecondaryAccountsUrl();
            String accountIdParam = String.join(",", accountIds);

            URIBuilder uriBuilder = new URIBuilder(baseUrl);
            uriBuilder.addParameter(CommonConstants.ACCOUNT_IDS, accountIdParam);
            uriBuilder.addParameter(CommonConstants.USER_ID_QUERY_PARAM, secondaryUserId);

            HttpGet request = new HttpGet(uriBuilder.build());
            request.addHeader(CommonConstants.ACCEPT_HEADER_NAME, CommonConstants.ACCEPT_HEADER_VALUE);
            request.addHeader(CommonConstants.ACCEPT_CONTENT_NAME, CommonConstants.ACCEPT_CONTENT_VALUE_JSON);
            addBasicAuthHeader(request);

            HttpResponse response = client.execute(request);

                    if (response.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    log.error("Failed to retrieve secondary account instruction statuses, HTTP Status: " + statusCode);
                throw new CdsConsentException(CdsErrorEnum.UNEXPECTED_ERROR,
                        "Failed to retrieve secondary account instruction statuses, HTTP Status: " + statusCode);
            }

            InputStream in = response.getEntity().getContent();
            String responseBody = IOUtils.toString(in, String.valueOf(StandardCharsets.UTF_8));
            return extractSecondaryInstructionStatusesFromBatchResponse(responseBody);

        } catch (IOException | URISyntaxException e) {
            log.error("Failed to retrieve secondary account instruction statuses", e);
            throw new CdsConsentException(CdsErrorEnum.UNEXPECTED_ERROR,
                    "Failed to retrieve secondary account instruction statuses");
        }
    }

    /**
     * Retrieve legal-entity blocked status for secondary accounts for a given user and client.
     * Calls GET /legal-entity with comma-separated account IDs, userId, and clientId query parameters.
     * The account-metadata service resolves the clientId to a legal entity ID internally.
     *
     * @param accountIds list of secondary account IDs
     * @param secondaryUserId secondary user ID
     * @param clientId software product client ID used to resolve the legal entity ID server-side
     * @return map of accountId to blocking status (true when blocked for the resolved legal entity)
     */
    public static Map<String, Boolean> getSecondaryAccountBlockedByLegalEntityMap(List<String> accountIds,
            String secondaryUserId, String clientId) throws CdsConsentException {

        Map<String, Boolean> blockedMap = new HashMap<>();

        for (String accountId : accountIds) {
            if (StringUtils.isNotBlank(accountId)) {
                blockedMap.put(accountId, false);
            }
        }

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(ConfigurableProperties.ACCOUNT_METADATA_WEBAPP_CONNECT_TIMEOUT_MILLIS)
                .setSocketTimeout(ConfigurableProperties.ACCOUNT_METADATA_WEBAPP_SOCKET_TIMEOUT_MILLIS)
                .build();

        try (CloseableHttpClient client = HttpClients.custom().setDefaultRequestConfig(requestConfig).build()) {
            String baseUrl = buildLegalEntitySharingUrl();
            String accountIdParam = String.join(",", accountIds);

            URIBuilder uriBuilder = new URIBuilder(baseUrl);
            uriBuilder.addParameter(CommonConstants.ACCOUNT_IDS, accountIdParam);
            uriBuilder.addParameter(CommonConstants.USER_ID_QUERY_PARAM, secondaryUserId);
            if (StringUtils.isNotBlank(clientId)) {
                uriBuilder.addParameter(CommonConstants.CLIENT_ID_QUERY_PARAM, clientId);
            }

            HttpGet request = new HttpGet(uriBuilder.build());
            request.addHeader(CommonConstants.ACCEPT_HEADER_NAME, CommonConstants.ACCEPT_HEADER_VALUE);
            request.addHeader(CommonConstants.ACCEPT_CONTENT_NAME, CommonConstants.ACCEPT_CONTENT_VALUE_JSON);
            addBasicAuthHeader(request);

            HttpResponse response = client.execute(request);

            if (response.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK) {
                log.error("Failed to retrieve legal entity sharing statuses, HTTP Status: " +
                        response.getStatusLine().getStatusCode());
                throw new CdsConsentException(CdsErrorEnum.UNEXPECTED_ERROR,
                        "Failed to retrieve legal entity sharing statuses from Account metadata service");
            }

            InputStream in = response.getEntity().getContent();
            String responseBody = IOUtils.toString(in, String.valueOf(StandardCharsets.UTF_8));
            return extractLegalEntityBlockedStatusFromBatchResponse(responseBody, blockedMap);

        } catch (IOException | URISyntaxException e) {
            log.error("Failed to retrieve legal entity sharing statuses", e);
            throw new CdsConsentException(CdsErrorEnum.UNEXPECTED_ERROR,
                    "Failed to retrieve legal entity sharing statuses");
        }
    }

    /**
     * Retrieve BNR permissions for multiple accounts for a given user.
     * Calls GET /business-stakeholders with comma-separated account IDs and userId query params.
     *
     * @param accountIds list of account IDs
     * @param userId user ID
     * @return map of accountId to permission, or empty map when retrieval fails
     */
    public static Map<String, String> getBusinessStakeholderPermissionsForAccounts(
            List<String> accountIds, String userId) throws CdsConsentException {

        Map<String, String> permissionMap = new HashMap<>();

        if (accountIds == null || accountIds.isEmpty() || StringUtils.isBlank(userId)) {
            return permissionMap;
        }

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(ConfigurableProperties.ACCOUNT_METADATA_WEBAPP_CONNECT_TIMEOUT_MILLIS)
                .setSocketTimeout(ConfigurableProperties.ACCOUNT_METADATA_WEBAPP_SOCKET_TIMEOUT_MILLIS)
                .build();

        try (CloseableHttpClient client = HttpClients.custom().setDefaultRequestConfig(requestConfig).build()) {
            String baseUrl = buildBusinessStakeholdersUrl();
            String accountIdParam = String.join(",", accountIds);

            URIBuilder uriBuilder = new URIBuilder(baseUrl);
            uriBuilder.addParameter(CommonConstants.ACCOUNT_IDS, accountIdParam);
            uriBuilder.addParameter(CommonConstants.USER_ID_QUERY_PARAM, userId);

            HttpGet request = new HttpGet(uriBuilder.build());
            request.addHeader(CommonConstants.ACCEPT_HEADER_NAME, CommonConstants.ACCEPT_HEADER_VALUE);
            request.addHeader(CommonConstants.ACCEPT_CONTENT_NAME, CommonConstants.ACCEPT_CONTENT_VALUE_JSON);
            addBasicAuthHeader(request);

            HttpResponse response = client.execute(request);

            if (response.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK) {
                log.error("Failed to retrieve business stakeholder permissions, HTTP Status: " +
                        response.getStatusLine().getStatusCode());
                throw new CdsConsentException(CdsErrorEnum.UNEXPECTED_ERROR,
                        "Failed to retrieve legal entity sharing statuses from Account metadata service");

            }

            InputStream in = response.getEntity().getContent();
            String responseBody = IOUtils.toString(in, String.valueOf(StandardCharsets.UTF_8));
            return extractBusinessStakeholderPermissionsFromBatchResponse(responseBody);

        } catch (IOException | URISyntaxException e) {
            log.error("Failed to retrieve business stakeholder permissions", e);
            throw new CdsConsentException(CdsErrorEnum.UNEXPECTED_ERROR,
                    "Failed to retrieve legal entity sharing statuses from Account metadata service");
        }
    }

    /**
     * Add disclosure option for one or more accounts (joint account scenario).
     * Calls POST /disclosure-options with account IDs and disclosure option value.
     *
     * @param accountDisclosureMap map of account ID to disclosure option value
     * @return true if disclosure options are added successfully, false otherwise
     */
    public static boolean addDisclosureOption(Map<String, String> accountDisclosureMap) {

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(ConfigurableProperties.ACCOUNT_METADATA_WEBAPP_CONNECT_TIMEOUT_MILLIS)
                .setSocketTimeout(ConfigurableProperties.ACCOUNT_METADATA_WEBAPP_SOCKET_TIMEOUT_MILLIS)
                .build();

        try (CloseableHttpClient client = HttpClients.custom().setDefaultRequestConfig(requestConfig).build()) {
            String requestUrl = buildDisclosureOptionsUrl();
            HttpPost request = new HttpPost(requestUrl);

            request.addHeader(CommonConstants.ACCEPT_HEADER_NAME, CommonConstants.ACCEPT_HEADER_VALUE);
            request.addHeader(CommonConstants.ACCEPT_CONTENT_NAME, CommonConstants.ACCEPT_CONTENT_VALUE_JSON);
            addBasicAuthHeader(request);

            // Build request body with account IDs and disclosure option
            String requestBody = buildAddDisclosureOptionRequestBody(accountDisclosureMap);
            request.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));

            HttpResponse response = client.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();

            return statusCode == HttpURLConnection.HTTP_CREATED || statusCode == HttpURLConnection.HTTP_OK;

        } catch (IOException e) {
            log.error("Failed to add DOMS statuses for joint accounts", e);
            return false;
        }
    }

    /**
     * Add business stakeholder permissions for selected business accounts.
     * Calls POST /business-stakeholders in account metadata service.
     *
     * @param accountOwnersByAccountMap map of accountId to business account owners
     * @param nominatedRepresentativesByAccountMap map of accountId to nominated representatives
     * @return true if business stakeholder permissions are added successfully, false otherwise
     */
    public static boolean addBusinessStakeholderPermissions(Map<String, Set<String>> accountOwnersByAccountMap,
            Map<String, Set<String>> nominatedRepresentativesByAccountMap) {

        if (nominatedRepresentativesByAccountMap == null || nominatedRepresentativesByAccountMap.isEmpty()) {
            return true;
        }

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(ConfigurableProperties.ACCOUNT_METADATA_WEBAPP_CONNECT_TIMEOUT_MILLIS)
                .setSocketTimeout(ConfigurableProperties.ACCOUNT_METADATA_WEBAPP_SOCKET_TIMEOUT_MILLIS)
                .build();

        try (CloseableHttpClient client = HttpClients.custom().setDefaultRequestConfig(requestConfig).build()) {
            String requestUrl = buildBusinessStakeholdersUrl();
            HttpPost request = new HttpPost(requestUrl);

            request.addHeader(CommonConstants.ACCEPT_HEADER_NAME, CommonConstants.ACCEPT_HEADER_VALUE);
            request.addHeader(CommonConstants.ACCEPT_CONTENT_NAME, CommonConstants.ACCEPT_CONTENT_VALUE_JSON);
            addBasicAuthHeader(request);

            String requestBody = buildBusinessStakeholderPermissionsRequestBody(accountOwnersByAccountMap,
                        nominatedRepresentativesByAccountMap);
            request.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));

            HttpResponse response = client.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            return statusCode == HttpURLConnection.HTTP_CREATED || statusCode == HttpURLConnection.HTTP_OK;

        } catch (IOException e) {
            log.error("Failed to add business stakeholder permissions", e);
            return false;
        }
    }

    /**
     * Add Basic Authentication header to the HTTP request.
     *
     * @param request the HTTP request to add the auth header to
     */
    private static void addBasicAuthHeader(HttpRequestBase request) {

        String credentials = ConfigurableProperties.ACCOUNT_METADATA_WEBAPP_USERNAME + ":" +
                ConfigurableProperties.ACCOUNT_METADATA_WEBAPP_PASSWORD;

        String encodedAuth = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        request.addHeader(CommonConstants.AUTH_HEADER, CommonConstants.BASIC_TAG + encodedAuth);

    }

    /**
     * Build the request URL for the disclosure-options endpoint (for adding options).
     *
     * @return the complete request URL
     */
    private static String buildDisclosureOptionsUrl() {
        return ConfigurableProperties.ACCOUNT_METADATA_WEBAPP_BASE_URL + CommonConstants.DISCLOSURE_OPTIONS_ENDPOINT;
    }

    /**
     * Build the request body for adding disclosure option.
     * Constructs JSON array with account IDs list and disclosure option value.
     * Format: [{ "accountId": "...", "disclosureOption": "..."}...]
     *
     * @param accountDisclosureMap map of account IDs to disclosure option values
     * @return JSON request body as string
     */
    private static String buildAddDisclosureOptionRequestBody(Map<String, String> accountDisclosureMap) {
        JsonArray dataArray = new JsonArray();

        for (Map.Entry<String, String> entry : accountDisclosureMap.entrySet()) {
            JsonObject accountData = new JsonObject();
            accountData.addProperty(CommonConstants.ACCOUNT_ID, entry.getKey());
            accountData.addProperty(CommonConstants.DISCLOSURE_OPTION_FIELD, entry.getValue());
            dataArray.add(accountData);
        }
        return dataArray.toString();
    }

    /**
     * Build the request URL for the secondary-accounts endpoint.
     *
     * @return the complete request URL
     */
    private static String buildSecondaryAccountsUrl() {
        return ConfigurableProperties.ACCOUNT_METADATA_WEBAPP_BASE_URL
                + CommonConstants.SECONDARY_ACCOUNTS_ENDPOINT;
    }

    /**
     * Build the request URL for the business-stakeholders endpoint.
     *
     * @return the complete request URL
     */
    private static String buildBusinessStakeholdersUrl() {
        return ConfigurableProperties.ACCOUNT_METADATA_WEBAPP_BASE_URL + CommonConstants.BUSINESS_STAKEHOLDERS_ENDPOINT;
    }

    /**
     * Build the request URL for the legal-entity sharing endpoint.
     *
     */
    private static String buildLegalEntitySharingUrl() {
        return ConfigurableProperties.ACCOUNT_METADATA_WEBAPP_BASE_URL + CommonConstants.LEGAL_ENTITY_SHARING_ENDPOINT;
    }

    /**
     * Build the request body for adding business stakeholder permissions.
     *
     * @param accountOwnersByAccountMap map of accountId to business account owners
     * @param nominatedRepresentativesByAccountMap map of accountId to nominated representatives
     * @return JSON request body as string
     */
    private static String buildBusinessStakeholderPermissionsRequestBody(
        Map<String, Set<String>> accountOwnersByAccountMap,
        Map<String, Set<String>> nominatedRepresentativesByAccountMap) {

        JsonArray dataArray = new JsonArray();

        for (Map.Entry<String, Set<String>> entry : nominatedRepresentativesByAccountMap.entrySet()) {
            String accountId = entry.getKey();
            Set<String> nominatedRepresentatives = entry.getValue();

            if (StringUtils.isBlank(accountId) || nominatedRepresentatives == null) {
                continue;
            }

            JsonObject item = new JsonObject();
            item.addProperty(CommonConstants.BUSINESS_STAKEHOLDER_ACCOUNT_ID_FIELD, accountId);

            JsonArray accountOwnersArray = new JsonArray();
            Set<String> accountOwners = accountOwnersByAccountMap == null
                    ? Collections.emptySet()
                    : accountOwnersByAccountMap.getOrDefault(accountId, Collections.emptySet());
            for (String owner : accountOwners) {
                if (StringUtils.isNotBlank(owner)) {
                    accountOwnersArray.add(owner);
                }
            }
            item.add(CommonConstants.BUSINESS_STAKEHOLDER_ACCOUNT_OWNERS_FIELD, accountOwnersArray);

            JsonArray representativeArray = new JsonArray();
            for (String representative : nominatedRepresentatives) {
                if (StringUtils.isBlank(representative)) {
                    continue;
                }
                JsonObject representativeObj = new JsonObject();
                representativeObj.addProperty(CommonConstants.BUSINESS_STAKEHOLDER_REPRESENTATIVE_NAME_FIELD,
                        representative);
                representativeObj.addProperty(CommonConstants.BUSINESS_STAKEHOLDER_REPRESENTATIVE_PERMISSION_FIELD,
                        CommonConstants.BUSINESS_STAKEHOLDER_PERMISSION_AUTHORIZE);
                representativeArray.add(representativeObj);
            }

            if (representativeArray.isEmpty()) {
                continue;
            }

            item.add(CommonConstants.BUSINESS_STAKEHOLDER_NOMINATED_REPRESENTATIVES_FIELD, representativeArray);
            dataArray.add(item);
        }

        return dataArray.toString();
    }

    /**
     * Extract DOMS statuses from batch API response body.
     * Returns a Map of account IDs to their DOMS statuses.
     * @param responseBody the JSON response body as a string
     * @return Map of accountID to DOMS Status, or empty map if parsing fails
     */
    private static Map<String, String> extractDOMSStatusesFromBatchResponse(String responseBody) {
        Map<String, String> statusMap = new HashMap<>();

        try {
            Gson gson = new Gson();
            JsonElement responseElement = gson.fromJson(responseBody, JsonElement.class);

            if (responseElement != null && responseElement.isJsonArray()) {
                JsonArray responseArray = responseElement.getAsJsonArray();
                for (JsonElement itemElement : responseArray) {
                    if (itemElement != null && itemElement.isJsonObject()) {
                        JsonObject item = itemElement.getAsJsonObject();
                        JsonElement accountIdElement = item.get(CommonConstants.ACCOUNT_ID);
                        JsonElement statusElement = item.get(CommonConstants.DISCLOSURE_OPTION_FIELD);
                        if (accountIdElement != null && !accountIdElement.isJsonNull()
                                && statusElement != null && !statusElement.isJsonNull()) {
                            statusMap.put(accountIdElement.getAsString(), statusElement.getAsString());
                        }
                    }
                }
            } 

            return statusMap;

        } catch (JsonSyntaxException e) {
            log.error("Failed to parse batch response JSON", e);
            return statusMap;
        }
    }

    /**
     * Extract secondary account instruction statuses from batch API response body.
     * @param responseBody the JSON response body as a string
     * @return map of accountId to secondary account instruction status
     */
    private static Map<String, String> extractSecondaryInstructionStatusesFromBatchResponse(String responseBody) {
        Map<String, String> statusMap = new HashMap<>();

        try {
            Gson gson = new Gson();
            JsonElement responseElement = gson.fromJson(responseBody, JsonElement.class);

            if (responseElement != null && responseElement.isJsonArray()) {
                JsonArray responseArray = responseElement.getAsJsonArray();
                for (JsonElement itemElement : responseArray) {
                    if (itemElement != null && itemElement.isJsonObject()) {
                        JsonObject item = itemElement.getAsJsonObject();
                        JsonElement accountIdElement = item.get(CommonConstants.ACCOUNT_ID);
                        JsonElement instructionStatusElement =
                                item.get(CommonConstants.SECONDARY_ACCOUNT_INSTRUCTION_STATUS_FIELD);
                        if (accountIdElement != null && !accountIdElement.isJsonNull()
                                && instructionStatusElement != null && !instructionStatusElement.isJsonNull()) {
                            statusMap.put(accountIdElement.getAsString(), instructionStatusElement.getAsString());
                        }
                    }
                }
            }

            return statusMap;

        } catch (JsonSyntaxException e) {
            log.error("Failed to parse secondary account instruction batch response JSON", e);
            return statusMap;
        }
    }

    /**
     * Extract business stakeholder permissions from batch API response body.
     *
     * @param responseBody the JSON response body as a string
     * @return map of accountId to permission
     */
    private static Map<String, String> extractBusinessStakeholderPermissionsFromBatchResponse(String responseBody) {
        Map<String, String> permissionMap = new HashMap<>();

        try {
            Gson gson = new Gson();
            JsonElement responseElement = gson.fromJson(responseBody, JsonElement.class);

            if (responseElement != null && responseElement.isJsonArray()) {
                JsonArray responseArray = responseElement.getAsJsonArray();
                for (JsonElement itemElement : responseArray) {
                    if (itemElement != null && itemElement.isJsonObject()) {
                        JsonObject item = itemElement.getAsJsonObject();
                        JsonElement accountIdElement = item.get(CommonConstants.ACCOUNT_ID);
                        JsonElement permissionElement = item.get(CommonConstants.BUSINESS_STAKEHOLDER_PERMISSION_FIELD);

                        if (accountIdElement != null && !accountIdElement.isJsonNull()) {
                            String accountId = accountIdElement.getAsString();
                            String permission = permissionElement == null || permissionElement.isJsonNull()
                                    ? null
                                    : permissionElement.getAsString();
                            permissionMap.put(accountId, permission);
                        }
                    }
                }
            }

            return permissionMap;

        } catch (JsonSyntaxException e) {
            log.error("Failed to parse business stakeholder permission batch response JSON", e);
            return permissionMap;
        }
    }

    /**
     * Extract legal-entity blocked statuses from batch API response body.
     * The server has already filtered results by the resolved legal entity (via clientId),
     * so only items matching that legal entity are present in the response.
     *
     * @param responseBody the JSON response body as a string
     * @param defaultStatusMap account-level default statuses
     * @return map of accountId to blocked status
     */
    private static Map<String, Boolean> extractLegalEntityBlockedStatusFromBatchResponse(String responseBody,
            Map<String, Boolean> defaultStatusMap) {

        Map<String, Boolean> blockedStatusMap = new HashMap<>(defaultStatusMap);

        try {
            Gson gson = new Gson();
            JsonElement responseElement = gson.fromJson(responseBody, JsonElement.class);

            if (responseElement != null && responseElement.isJsonArray()) {
                JsonArray responseArray = responseElement.getAsJsonArray();
                for (JsonElement itemElement : responseArray) {
                    if (itemElement == null || !itemElement.isJsonObject()) {
                        continue;
                    }

                    JsonObject item = itemElement.getAsJsonObject();
                    String accountId = getJsonString(item, "accountID", CommonConstants.ACCOUNT_ID);
                    String sharingStatus = getJsonString(item, CommonConstants.LEGAL_ENTITY_SHARING_STATUS,
                            "legalEntitySharingStatus");

                    if (StringUtils.isBlank(accountId) || !blockedStatusMap.containsKey(accountId)) {
                        continue;
                    }

                    if (CommonConstants.LEGAL_ENTITY_SHARING_STATUS_BLOCKED.equalsIgnoreCase(sharingStatus)) {
                        blockedStatusMap.put(accountId, true);
                    }
                }
            }

            return blockedStatusMap;
        } catch (JsonSyntaxException e) {
            log.error("Failed to parse legal-entity sharing batch response JSON", e);
            return blockedStatusMap;
        }
    }

    /**
     * Retrieves the first non-null and non-empty string value from the given JsonObject for the provided field names.
     * Iterates through the fieldNames in order and returns the value of the first field that exists and is not null.
     *
     * @param item       the JsonObject to search for the fields
     * @param fieldNames one or more field names to check in order of priority
     * @return the string value of the first found field, or an empty string if none are found or all are null
     */
    private static String getJsonString(JsonObject item, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonElement value = item.get(fieldName);
            if (value != null && !value.isJsonNull()) {
                return value.getAsString();
            }
        }
        return StringUtils.EMPTY;
    }
}

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

import com.google.gson.JsonElement;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.wso2.openbanking.consumerdatastandards.au.extensions.configurations.ConfigurableProperties;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.CommonConstants;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class to interact with the Account Metadata Webapp for DOMS status retrieval.
 * Supports both single account and batch POST requests for multiple accounts.
 * All requests include Basic Authentication.
 */
public class AccountMetadataUtil {

    private static final Log log = LogFactory.getLog(AccountMetadataUtil.class);

    /**
     * Private constructor to prevent instantiation
     */
    private AccountMetadataUtil() {
    }

    /**
     * Retrieve DOMS status for a single account from the Account Metadata Webapp.
     * Calls POST /disclosure-options/get with a single account ID.
     *
     * @param accountId the account ID for which DOMS status needs to be retrieved
     * @return the DOMS status string (e.g., "pre-approval", "no-sharing"), or null if retrieval fails
     */
    public static String getDOMSStatusForAccount(String accountId) {
        if (StringUtils.isBlank(accountId)) {
            log.warn("Invalid parameters: accountId is null or empty, Error occurred in Retrieving DOMS status");
            return null;
        }

        // Call the batch method with a single account ID list
        Map<String, String> statusMap = getDOMSStatusesForAccounts(List.of(accountId));

        if (statusMap != null && !statusMap.isEmpty()) {
            return statusMap.get(accountId);
        }

        return null;
    }

    /**
     * Retrieve DOMS statuses for multiple accounts from the Account Metadata Webapp.
     * Calls POST /disclosure-options/get with list of account IDs.
     *
     * @param accountIds list of account IDs to retrieve DOMS statuses for
     * @return a Map of accountId to DOMS Status, or null if retrieval fails
     */
    public static Map<String, String> getDOMSStatusesForAccounts(List<String> accountIds) {
        if (accountIds == null || accountIds.isEmpty()) {
            log.warn("Invalid parameters: accountIds is null or empty");
            return null;
        }

        String requestUrl = buildGetStatusRequestUrl();

        if (log.isDebugEnabled()) {
            log.debug("Retrieving DOMS statuses for " + accountIds.size() + " accounts from: " + requestUrl);
        }

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(requestUrl);
            request.addHeader(CommonConstants.ACCEPT_HEADER_NAME, CommonConstants.ACCEPT_HEADER_VALUE);
            request.addHeader(CommonConstants.ACCEPT_CONTENT_NAME, CommonConstants.ACCEPT_CONTENT_VALUE_JSON);
            addBasicAuthHeader(request);

            // Build request body with account IDs
            String requestBody = buildGetStatusRequestBody(accountIds);
            request.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));

            HttpResponse response = client.execute(request);

            if (response.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK) {
                log.error("Failed to retrieve DOMS statuses for accounts, HTTP Status: " +
                        response.getStatusLine().getStatusCode());
                return null;
            }

            InputStream in = response.getEntity().getContent();
            String responseBody = IOUtils.toString(in, String.valueOf(StandardCharsets.UTF_8));

            return extractDOMSStatusesFromBatchResponse(responseBody);

        } catch (IOException e) {
            log.error("IO Exception occurred while retrieving DOMS statuses for batch accounts", e);
        } catch (RuntimeException e) {
            log.error("Unexpected exception occurred while retrieving DOMS statuses for batch accounts", e);
        }

        return null;
    }

    /**
     * Add disclosure option for one or more accounts (joint account scenario).
     * Calls POST /disclosure-options with account IDs and disclosure option value.
     *
     * @param accountIds list of account IDs to add disclosure option for
     * @param disclosureOption the disclosure option value to set (e.g., "pre-approval", "no-sharing")
     * @return true if the operation succeeded (HTTP 201 or 200), false otherwise
     */
    public static boolean addDisclosureOption(List<String> accountIds, String disclosureOption) {
        if (accountIds == null || accountIds.isEmpty() || StringUtils.isBlank(disclosureOption)) {
            log.warn("Invalid parameters: accountIds or disclosureOption is null or empty, " +
                    "in adding Disclosure options");
            return false;
        }

        String requestUrl = buildDisclosureOptionsUrl();

        if (log.isDebugEnabled()) {
            log.debug("Adding disclosure option for " + accountIds.size() + " accounts to: " + requestUrl);
        }

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(requestUrl);
            request.addHeader(CommonConstants.ACCEPT_HEADER_NAME, CommonConstants.ACCEPT_HEADER_VALUE);
            request.addHeader(CommonConstants.ACCEPT_CONTENT_NAME, CommonConstants.ACCEPT_CONTENT_VALUE_JSON);
            addBasicAuthHeader(request);

            // Build request body with account IDs and disclosure option
            String requestBody = buildAddDisclosureOptionRequestBody(accountIds, disclosureOption);
            request.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));

            HttpResponse response = client.execute(request);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpURLConnection.HTTP_CREATED || statusCode == HttpURLConnection.HTTP_OK) {
                if (log.isDebugEnabled()) {
                    log.debug("Successfully added disclosure option for accounts, HTTP Status: " + statusCode);
                }
                return true;
            } else {
                log.error("Failed to add disclosure option for accounts, HTTP Status: " + statusCode);
                return false;
            }

        } catch (IOException e) {
            log.error("IO Exception occurred while adding disclosure option", e);
        } catch (RuntimeException e) {
            log.error("Unexpected exception occurred while adding disclosure option", e);
        }

        return false;
    }

    /**
     * Add Basic Authentication header to the HTTP request.
     *
     * @param request the HTTP request to add the auth header to
     */
    private static void addBasicAuthHeader(HttpPost request) {
        if (StringUtils.isNotBlank(ConfigurableProperties.ACCOUNT_METADATA_WEBAPP_USERNAME)
                && StringUtils.isNotBlank(ConfigurableProperties.ACCOUNT_METADATA_WEBAPP_PASSWORD)) {

            String credentials = ConfigurableProperties.ACCOUNT_METADATA_WEBAPP_USERNAME
                    + ":" + ConfigurableProperties.ACCOUNT_METADATA_WEBAPP_PASSWORD;
            String encodedCredentials =
                    Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

            request.addHeader(CommonConstants.AUTH_HEADER, CommonConstants.BASIC_TAG + encodedCredentials);
        } else {
            log.warn("Basic Auth credentials not configured for Account Metadata Webapp");
        }
    }
    
    /**
     * Build the request URL for batch disclosure-options endpoint (multiple accounts).
     *
     * @return the complete request URL
     */
    private static String buildGetStatusRequestUrl() {
        return ConfigurableProperties.ACCOUNT_METADATA_WEBAPP_BASE_URL
                + CommonConstants.DISCLOSURE_OPTIONS_GET_ENDPOINT;
    }

    /**
     * Build the request URL for the disclosure-options endpoint (for adding options).
     *
     * @return the complete request URL
     */
    private static String buildDisclosureOptionsUrl() {
        return ConfigurableProperties.ACCOUNT_METADATA_WEBAPP_BASE_URL
                + CommonConstants.DISCLOSURE_OPTIONS_ENDPOINT;
    }

    /**
     * Build the request body for batch DOMS status retrieval.
     * Constructs JSON with account IDs list.
     *
     * @param accountIds the list of account IDs
     * @return JSON request body as string
     */
    private static String buildGetStatusRequestBody(List<String> accountIds) {
        JsonObject requestBody = new JsonObject();
        Gson gson = new Gson();
        requestBody.add(CommonConstants.ACCOUNT_IDS, gson.toJsonTree(accountIds));
        return requestBody.toString();
    }

    /**
     * Build the request body for adding disclosure option.
     * Constructs JSON with account IDs list and disclosure option value.
     * Format: { "data": [{ "accountID": "...", "disclosureOption": "..."}...] }
     *
     * @param accountIds the list of account IDs
     * @param disclosureOption the disclosure option value
     * @return JSON request body as string
     */
    private static String buildAddDisclosureOptionRequestBody(List<String> accountIds, String disclosureOption) {
        JsonObject requestBody = new JsonObject();
        JsonArray dataArray = new JsonArray();

        for (String accountId : accountIds) {
            JsonObject accountData = new JsonObject();
            accountData.addProperty(CommonConstants.ACCOUNT_ID, accountId);
            accountData.addProperty(CommonConstants.DISCLOSURE_OPTION_FIELD, disclosureOption);
            dataArray.add(accountData);
        }

        requestBody.add(CommonConstants.DATA, dataArray);
        return requestBody.toString();
    }

    /**
     * Extract DOMS statuses from batch API response body.
     * Expects a JSON response with structure: { "status": "success", "data": { "ACC1": "pre-approval", ... } }
     * Returns a Map of account IDs to their DOMS statuses.
     *
     * @param responseBody the JSON response body as a string
     * @return Map of accountID to DOMS Status, or empty map if parsing fails
     */
    private static Map<String, String> extractDOMSStatusesFromBatchResponse(String responseBody) {
        Map<String, String> statusMap = new HashMap<>();

        try {
            Gson gson = new Gson();
            JsonObject responseJson = gson.fromJson(responseBody, JsonObject.class);

            if (responseJson == null) {
                log.warn("Response body is not valid JSON");
                return statusMap;
            }

            // Extract data field
            if (responseJson.has(CommonConstants.DATA)) {
                JsonElement dataObj = responseJson.get(CommonConstants.DATA);

                // Handle case where data is an object/map with account IDs as keys
                if (dataObj.isJsonObject()) {
                    JsonObject dataMap = dataObj.getAsJsonObject();
                    for (String accountId : dataMap.keySet()) {
                        JsonElement valueElement = dataMap.get(accountId);
                        // Check if value is not null before converting to string
                        if (valueElement != null && !valueElement.isJsonNull()) {
                            String domsStatus = valueElement.getAsString();
                            statusMap.put(accountId, domsStatus);
                        } else {
                            log.warn("DOMS status value is null for account: " + accountId);
                        }
                    }
                }
            }

            if (statusMap.isEmpty()) {
                log.warn("DOMS status data not found in batch response body");
            }

            return statusMap;

        } catch (JsonSyntaxException e) {
            log.error("Failed to parse batch response JSON", e);
            return statusMap;
        } catch (RuntimeException e) {
            log.error("Unexpected exception occurred while extracting DOMS statuses from batch response", e);
            return statusMap;
        }
    }
}

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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.wso2.openbanking.consumerdatastandards.au.extensions.configurations.ConfigurableProperties;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.CommonConstants;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
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
    public static Map<String, String> getDOMSStatusesForAccounts(List<String> accountIds) {

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
                log.error("Failed to retrieve DOMS statuses for accounts, HTTP Status: " +
                        response.getStatusLine().getStatusCode());
                return null;
            }

            InputStream in = response.getEntity().getContent();
            String responseBody = IOUtils.toString(in, String.valueOf(StandardCharsets.UTF_8));

            return extractDOMSStatusesFromBatchResponse(responseBody);

        } catch (IOException | URISyntaxException e) {
            log.error("Failed to retrieve DOMS statuses for batch accounts", e);
        }
        return null;
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
     * Add Basic Authentication header to the HTTP request.
     *
     * @param request the HTTP request to add the auth header to
     */
    private static void addBasicAuthHeader(org.apache.http.client.methods.HttpRequestBase request) {

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
     * Extract DOMS statuses from batch API response body.
     * Returns a Map of account IDs to their DOMS statuses.
     *
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
}

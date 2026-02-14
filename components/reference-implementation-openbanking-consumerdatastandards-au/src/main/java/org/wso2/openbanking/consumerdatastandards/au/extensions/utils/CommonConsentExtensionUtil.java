/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.CommonConstants;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.SuccessResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for common functions used in consent extensions.
 */
public class CommonConsentExtensionUtil {

    private static final Log log = LogFactory.getLog(CommonConsentExtensionUtil.class);

    /**
     * Converts a generic Java object to a {@link JSONObject}.
     *
     * @param object the Java object to be converted to JSON
     * @return a {@link JSONObject} representation of the given object
     * @throws JsonProcessingException if the object cannot be serialized to a JSON string
     */
    public static JSONObject convertObjectToJson(Object object) throws JsonProcessingException {
        // Convert Object to JSON string
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(object);

        // Parse JSON string to JSONObject
        return new JSONObject(jsonString);
    }

    /**
     * Method to construct the consent manage success response.
     * @param requestId The unique identifier for the request to be included in the success response.
     * @return A {@link JSONObject} representing the success response with the provided request ID and status.
     */
    public static JSONObject getSuccessResponse(String requestId) {

        SuccessResponse successResponse = new SuccessResponse();
        successResponse.setResponseId(requestId);
        successResponse.setStatus(SuccessResponse.StatusEnum.SUCCESS);

        return new JSONObject(successResponse);
    }

    /**
     * Validate whether the given date string is in a valid ISO 8601 format.
     * @param dateValue the date string to validate
     * @return true if the date string is a valid ISO 8601 format, false otherwise
     */
    public static boolean isValid8601(String dateValue) {
        try {
            OffsetDateTime.parse(dateValue);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Retrieves account information from the specified sharable accounts endpoint.
     *
     * @param sharableAccountsRetrieveUrl The URL of the sharable accounts retrieve endpoint.
     * @param parameters A map of query parameters to be included in the request URL.
     * @param headers A map of HTTP headers to be included in the request.
     * @return The response body as a String if the request is successful; otherwise, returns null.
     */
    public static String getAccountsFromEndpoint(String sharableAccountsRetrieveUrl, Map<String, String> parameters,
                                                 Map<String, String> headers) {

        String retrieveUrl = "";
        if (!sharableAccountsRetrieveUrl.endsWith(CommonConstants.SERVICE_URL_SLASH)) {
            retrieveUrl = sharableAccountsRetrieveUrl + CommonConstants.SERVICE_URL_SLASH;
        } else {
            retrieveUrl = sharableAccountsRetrieveUrl;
        }
        if (!parameters.isEmpty()) {
            retrieveUrl = buildRequestURL(retrieveUrl, parameters);
        }

        if (log.isDebugEnabled()) {
            log.debug("Sharable accounts retrieve endpoint : " + retrieveUrl);
        }

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(retrieveUrl);
            request.addHeader(CommonConstants.ACCEPT_HEADER_NAME, CommonConstants.ACCEPT_HEADER_VALUE);

            if (headers != null && !headers.isEmpty()) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    if (entry.getKey() != null && entry.getValue() != null) {
                        request.addHeader(entry.getKey(), entry.getValue());
                    }
                }
            }

            HttpResponse response = client.execute(request);

            if (response.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK) {
                log.error("Retrieving sharable accounts failed");
                return null;
            } else {
                InputStream in = response.getEntity().getContent();
                return IOUtils.toString(in, String.valueOf(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            log.error("Exception occurred while retrieving sharable accounts", e);
        }
        return null;
    }

    /**
     * Build the complete URL with query parameters sent in the map.
     *
     * @param baseURL    the base URL
     * @param parameters map of parameters
     * @return the output URL
     */
    public static String buildRequestURL(String baseURL, Map<String, String> parameters) {

        List<NameValuePair> pairs = new ArrayList<>();

        for (Map.Entry<String, String> key : parameters.entrySet()) {
            if (key.getKey() != null && key.getValue() != null) {
                pairs.add(new BasicNameValuePair(key.getKey(), key.getValue()));
            }
        }
        String queries = URLEncodedUtils.format(pairs, CommonConstants.CHAR_SET);
        return baseURL + "?" + queries;
    }

    /**
     * Get accountId from the accounts list by matching the displayName.
     * @param accountsURL The URL endpoint to retrieve the list of accounts.
     * @param targetDisplayName The display name of the account to search for.
     * @return The accountId of the account with the matching display name, or {@code null} if not found.
     */
    public static String getAccountIdByDisplayName(String accountsURL, String targetDisplayName) {

        String accountData = CommonConsentExtensionUtil.getAccountsFromEndpoint(accountsURL, Collections.emptyMap(),
                new HashMap<>());

        JSONObject jsonAccountData = new JSONObject(accountData);
        JSONArray accounts = (JSONArray) jsonAccountData.get(CommonConstants.DATA);

        for (int i = 0; i < accounts.length(); i++) {
            JSONObject account = accounts.getJSONObject(i);
            String displayName = account.optString("displayName");
            if (targetDisplayName.equals(displayName)) {
                return account.getString("accountId");
            }
        }
        return null;
    }

    /**
     * Get epoch seconds from the given expiration date time string.
     * @param expirationDateTime the expiration date/time as a string,
     *                           either in ISO 8601 format or as epoch milliseconds
     * @return the number of seconds since the epoch represented by the expirationDateTime
     */
    public static long getEpochSeconds(String expirationDateTime) {

        Instant instant;

        try {
            // Try to parse the string as a standard date (ISO 8601)
            instant = Instant.parse(expirationDateTime);

        } catch (DateTimeParseException e) {
            try {
                long epochMillis = Long.parseLong(expirationDateTime);
                instant = Instant.ofEpochMilli(epochMillis);
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("Invalid format for expirationDateTime: " + expirationDateTime, nfe);
            }
        }

        // Get the number of seconds from the epoch for the successfully parsed Instant
        return instant.getEpochSecond();
    }
}

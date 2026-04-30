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

package org.wso2.openbanking.consumerdatastandards.account.metadata.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.wso2.openbanking.consumerdatastandards.account.metadata.configurations.ConfigurableProperties;
import org.wso2.openbanking.consumerdatastandards.account.metadata.constants.CommonConstants;
import org.wso2.openbanking.consumerdatastandards.account.metadata.exceptions.AccountMetadataException;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.SecondaryAccountInstructionItem;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;

/**
 * Utility class for fetching access tokens and consent IDs from the WSO2 Accelerator,
 * used during secondary account consent expiry processing.
 *
 * <p>NOTE: This class uses a trust-all SSL context suitable for development environments only.
 * Replace {@code TrustAllStrategy} with a proper trust store before deploying to production.
 */
public class ConsentExpiryUtil {

    private static final Log log = LogFactory.getLog(ConsentExpiryUtil.class);

    /**
     * Functional interface for HTTP client creation; package-private for test substitution.
     */
    interface HttpClientSupplier {
        CloseableHttpClient get() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException;
    }

    // Package-private: replaced in unit tests via reflection to inject a mock client.
    static HttpClientSupplier httpClientSupplier = ConsentExpiryUtil::buildSSlClient;

    private ConsentExpiryUtil() {
        // Prevent instantiation — all methods are static.
    }

    /**
     * Fetches all consents for the secondary users in the given items list and immediately
     * expires each one. Convenience wrapper around {@link #fetchConsentIdsForUsers} and
     * {@link #expireConsents}.
     *
     * @param items list of secondary account instruction items requiring consent expiry
     * @throws AccountMetadataException if the access token cannot be obtained or any consent
     *                                  expiry call fails
     */
    public static void fetchAndExpireConsents(List<SecondaryAccountInstructionItem> items)
            throws AccountMetadataException {

        Map<String, String> consentClientMap = fetchConsentIdsForUsers(items);
        if (log.isDebugEnabled()) {
            log.debug("[ConsentExpiry] Fetched " + consentClientMap.size()
                    + " consent(s) to expire");
        }
        expireConsents(consentClientMap);
    }

    /**
     * Orchestrates consent retrieval for all unique secondary users in the given items list.
     * Fetches one access token and reuses it across all user queries.
     *
     * @param items list of secondary account instruction items requiring consent expiry
     * @return map of consentId to clientId for all consents found
     * @throws AccountMetadataException if the access token cannot be obtained
     */
    public static Map<String, String> fetchConsentIdsForUsers(
            List<SecondaryAccountInstructionItem> items) throws AccountMetadataException {

        List<String> uniqueUserIds = items.stream()
                .map(SecondaryAccountInstructionItem::getSecondaryUserId)
                .filter(id -> id != null && !id.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());

        String accessToken = fetchAccessToken();

        Map<String, String> consentClientMap = new LinkedHashMap<>();
        for (String userId : uniqueUserIds) {
            Map<String, String> userConsents = fetchConsentIdsForUser(userId, accessToken);
            consentClientMap.putAll(userConsents);
            if (log.isDebugEnabled()) {
                log.debug("[ConsentExpiry] Found " + userConsents.size() + " consent(s) for userId=" + userId);
            }
        }
        return consentClientMap;
    }

    /**
     * Fetches an OAuth2 access token from the Accelerator token endpoint using the password grant.
     *
     * @return access token string
     * @throws AccountMetadataException if the token endpoint returns a non-200 response or
     *                                  the request fails
     */
    public static String fetchAccessToken() throws AccountMetadataException {

        String credentials = ConfigurableProperties.ACCOUNT_METADATA_CLIENT_APP_ID + ":"
                + ConfigurableProperties.ACCOUNT_METADATA_CLIENT_APP_SECRET;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        try (CloseableHttpClient client = httpClientSupplier.get()) {

            HttpPost request = new HttpPost(ConfigurableProperties.ACCELERATOR_TOKEN_ENDPOINT_URL);
            request.setHeader(CommonConstants.AUTHORIZATION_HEADER,
                    CommonConstants.BASIC_PREFIX + encodedCredentials);
            request.setHeader(CommonConstants.CONTENT_TYPE_HEADER, CommonConstants.CONTENT_TYPE_FORM);

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair(CommonConstants.PARAM_GRANT_TYPE, CommonConstants.GRANT_TYPE_PASSWORD));
            params.add(new BasicNameValuePair(
                    CommonConstants.PARAM_USERNAME, ConfigurableProperties.CUSTOMER_CARE_OFFICER_USERNAME));
            params.add(new BasicNameValuePair(
                    CommonConstants.PARAM_PASSWORD, ConfigurableProperties.CUSTOMER_CARE_OFFICER_PASSWORD));
            params.add(new BasicNameValuePair(
                    CommonConstants.PARAM_SCOPE, ConfigurableProperties.CUSTOMER_CARE_OFFICER_TOKEN_SCOPE));
            request.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

            HttpResponse response = client.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != HttpURLConnection.HTTP_OK) {
                throw new AccountMetadataException(
                        "[ConsentExpiry] Failed to obtain access token. HTTP status: " + statusCode);
            }

            String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
            return jsonObject.get(CommonConstants.TOKEN_RESPONSE_ACCESS_TOKEN).getAsString();

        } catch (IOException | KeyStoreException | NoSuchAlgorithmException 
                 | KeyManagementException | JsonSyntaxException e) {
            throw new AccountMetadataException(
                    "[ConsentExpiry] Exception while fetching access token: " + e.getMessage(), e);
        }
    }

    /**
     * Fetches consents for a specific user from the Accelerator consent search API.
     *
     * @param userId      secondary user ID to search consents for
     * @param accessToken Bearer token obtained from {@link #fetchAccessToken()}
     * @return map of consentId to clientId
     * @throws AccountMetadataException if the consent search returns a non-200 response or
     *                                  the request fails
     */
    public static Map<String, String> fetchConsentIdsForUser(String userId, String accessToken)
            throws AccountMetadataException {

        if (StringUtils.isBlank(userId) || StringUtils.isBlank(accessToken)) {
            throw new AccountMetadataException(
                    "[ConsentExpiry] userId or accessToken is blank; cannot search consents");
        }

        try (CloseableHttpClient client = httpClientSupplier.get()) {

            HttpGet request = getConsentSearchAPIRequest(userId, accessToken);

            HttpResponse response = client.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != HttpURLConnection.HTTP_OK) {
                throw new AccountMetadataException(
                        "[ConsentExpiry] Consent search failed for userId=" + userId
                                + ". HTTP status: " + statusCode);
            }

            String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            return extractConsentClientMap(body, userId);

        } catch (IOException | URISyntaxException | KeyStoreException
                 | NoSuchAlgorithmException | KeyManagementException e) {
            throw new AccountMetadataException(
                    "[ConsentExpiry] Exception during consent search for userId=" + userId + ": " + e.getMessage(), e);
        }
    }

    private static HttpGet getConsentSearchAPIRequest(String userId, String accessToken) throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(ConfigurableProperties.ACCELERATOR_CONSENT_SEARCH_URL);
        uriBuilder.addParameter(CommonConstants.CONSENT_SEARCH_PARAM_CONSENT_TYPES,
                CommonConstants.CONSENT_TYPE_ACCOUNTS);
        uriBuilder.addParameter(CommonConstants.CONSENT_SEARCH_PARAM_USER_IDS, userId);

        HttpGet request = new HttpGet(uriBuilder.build());
        request.setHeader(CommonConstants.AUTHORIZATION_HEADER,
                CommonConstants.BEARER_PREFIX + accessToken);
        request.setHeader(CommonConstants.ACCEPT_HEADER, CommonConstants.CONTENT_TYPE_JSON);
        return request;
    }

    /**
     * Expires all consents in the provided map by invoking the Accelerator consent update API.
     * Uses Basic Auth with admin credentials and sends a minimal body containing only the
     * "Expired" status. The clientId associated with each consent is sent as the
     * {@code x-wso2-client-id} request header.
     * Attempts every consent before throwing so that partial progress is maximised.
     *
     * @param consentClientMap map of consentId to clientId for all consents to expire
     * @throws AccountMetadataException if one or more consent expiry calls fail
     */
    public static void expireConsents(Map<String, String> consentClientMap)
            throws AccountMetadataException {

        String credentials = ConfigurableProperties.IS_ADMIN_USERNAME + ":"
                + ConfigurableProperties.IS_ADMIN_PASSWORD;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        for (Map.Entry<String, String> entry : consentClientMap.entrySet()) {
            String consentId = entry.getKey();
            String clientId = entry.getValue();
            expireSingleConsent(consentId, clientId, encodedCredentials);
            if (log.isDebugEnabled()) {
                log.debug("[ConsentExpiry] Expired consentId=" + consentId + " clientId=" + clientId);
            }
        }
    }

    /**
     * Sends a PUT request to expire a single consent.
     *
     * @param consentId          the consent ID to expire
     * @param clientId           the client ID associated with the consent, sent as x-wso2-client-id
     * @param encodedCredentials Base64-encoded admin credentials for Basic Auth
     * @throws AccountMetadataException if the consent update returns a non-200 response or
     *                                  the request fails
     */
    private static void expireSingleConsent(String consentId, String clientId, String encodedCredentials)
            throws AccountMetadataException {

        try (CloseableHttpClient client = httpClientSupplier.get()) {

            String url = ConfigurableProperties.ACCELERATOR_CONSENT_UPDATE_BASE_URL + "/" + consentId;

            HttpPut request = new HttpPut(url);

            request.setHeader(CommonConstants.AUTHORIZATION_HEADER,
                    CommonConstants.BASIC_PREFIX + encodedCredentials);
            request.setHeader(CommonConstants.CONTENT_TYPE_HEADER, CommonConstants.CONTENT_TYPE_JSON);
            request.setHeader(CommonConstants.HEADER_WSO2_CLIENT_ID, clientId);
            request.setHeader(CommonConstants.HEADER_WSO2_INTERNAL_REQUEST,
                    CommonConstants.WSO2_INTERNAL_REQUEST_VALUE);
            request.setHeader(CommonConstants.HEADER_FAPI_INTERACTION_ID, UUID.randomUUID().toString());

            JsonObject body = new JsonObject();
            body.addProperty(CommonConstants.CONSENT_UPDATE_STATUS, CommonConstants.CONSENT_EXPIRE_STATUS);

            request.setEntity(new StringEntity(body.toString(), StandardCharsets.UTF_8));

            HttpResponse response = client.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != HttpURLConnection.HTTP_OK) {
                throw new AccountMetadataException("[ConsentExpiry] Failed to expire consentId=" + consentId
                        + ". HTTP status: " + statusCode);
            }

        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | KeyManagementException e) {
            throw new AccountMetadataException(
                    "[ConsentExpiry] Exception while expiring consentId=" + consentId + ": " + e.getMessage(), e);
        }
    }

    /**
     * Builds an HTTPS client that trusts all SSL certificates.
     * IMPORTANT: Replace TrustAllStrategy with a production trust store before going live.
     */
    static CloseableHttpClient buildSSlClient()
            throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(ConfigurableProperties.CONNECT_TIMEOUT_MILLIS)
                .setSocketTimeout(ConfigurableProperties.SOCKET_TIMEOUT_MILLIS)
                .build();

        SSLContext sslContext = SSLContextBuilder.create().loadTrustMaterial(TrustAllStrategy.INSTANCE).build();

        SSLConnectionSocketFactory sslSocketFactory =
                new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);

        return HttpClients.custom().setDefaultRequestConfig(requestConfig).setSSLSocketFactory(sslSocketFactory)
                .build();
    }

    /**
     * Extracts a consentId-to-clientId map from the consent search response JSON.
     * Supports both a plain JSON array {@code [ { "consentId": "...", "clientId": "..." }, ... ]}
     * and a wrapped shape {@code { "data": [ ... ] }}.
     */
    private static Map<String, String> extractConsentClientMap(String responseBody, String userId)
            throws AccountMetadataException {

        Map<String, String> consentClientMap = new LinkedHashMap<>();
        try {
            JsonElement root = JsonParser.parseString(responseBody);
            JsonArray consentsArray;
            JsonElement dataEl = root.getAsJsonObject().get(CommonConstants.CONSENT_RESPONSE_DATA);
            consentsArray = dataEl.getAsJsonArray();

            for (JsonElement item : consentsArray) {
                JsonObject obj = item.getAsJsonObject();
                JsonElement idEl = obj.get(CommonConstants.CONSENT_RESPONSE_CONSENT_ID);
                JsonElement clientEl = obj.get(CommonConstants.CONSENT_RESPONSE_CLIENT_ID);
                String clientId = (clientEl != null && !clientEl.isJsonNull()) ? clientEl.getAsString() : "";
                consentClientMap.put(idEl.getAsString(), clientId);
            }

        } catch (JsonSyntaxException | IllegalStateException | NullPointerException e) {
            throw new AccountMetadataException(
                    "[ConsentExpiry] Failed to parse consent search response for userId=" + userId, e);
        }
        return consentClientMap;
    }
}

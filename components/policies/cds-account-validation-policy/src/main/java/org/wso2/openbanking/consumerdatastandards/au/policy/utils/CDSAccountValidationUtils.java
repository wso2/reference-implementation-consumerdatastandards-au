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

package org.wso2.openbanking.consumerdatastandards.au.policy.utils;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.Setter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.wso2.openbanking.consumerdatastandards.au.policy.constants.CDSAccountValidationConstants;
import org.wso2.openbanking.consumerdatastandards.au.policy.exceptions.CDSAccountValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for the Consent Enforcement Policy.
 */
public class CDSAccountValidationUtils {

    private static final Log log = LogFactory.getLog(CDSAccountValidationUtils.class);

    @Setter
    private static CloseableHttpClient apacheHttpClient;

    private static final RSASSASigner SIGNER;
    private static final JWSHeader JWT_HEADER;

    static {
        RSASSASigner signer = null;
        try {
            PrivateKey pk = (PrivateKey) KeyStoreUtils.getSigningKey();
            if (pk != null) {
                signer = new RSASSASigner(pk);
            }
        } catch (Throwable e) {
            log.error("Signing key unavailable at startup, JWT signing will fail at call time: " + e.getMessage());
        }
        SIGNER = signer;
        JWT_HEADER = new JWSHeader.Builder(JWSAlgorithm.RS512)
                .type(JOSEObjectType.JWT)
                .build();

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(CDSAccountValidationConstants.HTTP_CLIENT_CONNECT_TIMEOUT_MILLIS)
                .setSocketTimeout(CDSAccountValidationConstants.HTTP_REQUEST_TIMEOUT_MILLIS)
                .build();
        apacheHttpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
    }


    /**
     * Method to generate JWT with the given payload.
     *
     * @param payload JSON payload as a string to be included in the JWT claims
     * @return Serialized JWT as a string
     */
    public static String generateJWT(String payload) throws ParseException, JOSEException {

        if (SIGNER == null) {
            throw new JOSEException("JWT signing key is not available");
        }
        log.debug("Generating JWT with provided payload");
        JWTClaimsSet claimsSet = JWTClaimsSet.parse(payload);

        SignedJWT signedJWT = new SignedJWT(JWT_HEADER, claimsSet);
        signedJWT.sign(SIGNER);
        log.debug("JWT generated successfully");

        return signedJWT.serialize();
    }

    /**
     * Fetch all blocked account IDs by checking disclosure options, secondary accounts,
     * business stakeholders, and legal-entity sharing status.
     *
     * @param accountIds set of account IDs to check
     * @param baseUrl base URL of the account metadata webapp
     * @param userId user ID for account metadata queries
     * @param basicAuthBase64 Base64-encoded Basic Auth credentials
     * @param clientId software product client ID used to resolve legal_entity_id
     * @return combined set of blocked account IDs
     */
    public static Set<String> fetchAllBlockedAccounts(
            Set<String> accountIds, String baseUrl, String userId, String basicAuthBase64, String clientId)
            throws CDSAccountValidationException {

        if (StringUtils.isBlank(userId)) {
            log.warn("[CDS-policy] no primary userId, skipping cds account validation.");
            return new HashSet<>();
        }

        String disclosureOptionsApi = baseUrl + CDSAccountValidationConstants.DISCLOSURE_OPTIONS_PATH;
        String secondaryAccountsApi = baseUrl + CDSAccountValidationConstants.SECONDARY_ACCOUNTS_PATH;
        String businessStakeholdersApi = baseUrl + CDSAccountValidationConstants.BUSINESS_STAKEHOLDERS_PATH;
        String legalEntitySharingApi = baseUrl + CDSAccountValidationConstants.LEGAL_ENTITY_SHARING_PATH;

        Set<String> blockedAccounts = fetchBlockedJointAccountsFromService(accountIds, disclosureOptionsApi,
                basicAuthBase64);
        Set<String> blockedSecondaryAccounts = fetchBlockedSecondaryAccountsFromService(accountIds,
                secondaryAccountsApi, userId, basicAuthBase64);
        Set<String> blockedBusinessAccounts = fetchBlockedBusinessAccountsFromService(accountIds,
                businessStakeholdersApi, userId, basicAuthBase64);
        Set<String> blockedLegalEntityAccounts = fetchBlockedLegalEntityAccountsFromService(accountIds,
                legalEntitySharingApi, userId, basicAuthBase64, clientId);

        blockedAccounts.addAll(blockedSecondaryAccounts);
        blockedAccounts.addAll(blockedBusinessAccounts);
        blockedAccounts.addAll(blockedLegalEntityAccounts);

        return blockedAccounts;
    }


    /**
     * Call disclosure options GET endpoint and return blocked account IDs.
     *
     * @param accountIds set of account IDs to check
     * @param blockedAccountsApi disclosure options API endpoint
     * @param basicAuthBase64 Base64-encoded Basic Auth credentials
     * @return set of blocked account IDs
     */
    static Set<String> fetchBlockedJointAccountsFromService(
            Set<String> accountIds, String blockedAccountsApi, String basicAuthBase64)
            throws CDSAccountValidationException {

        Set<String> blockedAccounts = new HashSet<>();

        if (accountIds == null || accountIds.isEmpty()) {
            return blockedAccounts;
        }

        try {
            String accountIdsParam = URLEncoder.encode(String.join(",", accountIds), StandardCharsets.UTF_8);
            String requestUrl = blockedAccountsApi + "?" + CDSAccountValidationConstants.ACCOUNT_IDS_TAG + "="
                    + accountIdsParam;

            HttpGet request = new HttpGet(requestUrl);
            request.addHeader(CDSAccountValidationConstants.ACCEPT_TAG,
                    CDSAccountValidationConstants.JSON_CONTENT_TYPE);
            request.addHeader(CDSAccountValidationConstants.AUTH_HEADER,
                    CDSAccountValidationConstants.BASIC_TAG + basicAuthBase64);

            try (CloseableHttpResponse response = apacheHttpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    JSONArray disclosureOptions;
                    try {
                        disclosureOptions = new JSONArray(responseBody);
                    } catch (JSONException e) {
                        String errorMessage = "Invalid disclosure options service response";
                        log.error(errorMessage, e);
                        throw new CDSAccountValidationException(errorMessage, e);
                    }

                    for (int i = 0; i < disclosureOptions.length(); i++) {
                        JSONObject accountDisclosure = disclosureOptions.optJSONObject(i);
                        if (accountDisclosure == null) {
                            continue;
                        }
                        String disclosureOption = accountDisclosure.optString(
                                CDSAccountValidationConstants.DISCLOSURE_OPTION_TAG, null);
                        if (CDSAccountValidationConstants.DOMS_STATUS_NO_SHARING.equalsIgnoreCase(disclosureOption)) {
                            String accountId = accountDisclosure.optString(
                                    CDSAccountValidationConstants.CDS_ACCOUNT_ID_TAG, null);
                            if (StringUtils.isNotBlank(accountId)) {
                                blockedAccounts.add(accountId);
                            }
                        }
                    }
                } else {
                    String errorMessage = "Disclosure options service returned HTTP " + statusCode;
                    log.error(errorMessage);
                    throw new CDSAccountValidationException(errorMessage);
                }
            }
        } catch (IOException e) {
            String errorMessage = "[DOMS] Error calling disclosure options service";
            log.error(errorMessage, e);
            throw new CDSAccountValidationException(errorMessage, e);
        }

        return blockedAccounts;
    }

    /**
     * Call secondary accounts GET endpoint and return blocked account IDs.
     * An account is considered blocked if its secondaryAccountInstructionStatus is "inactive".
     *
     * @param accountIds set of account IDs to check
     * @param secondaryAccountsApi secondary accounts API endpoint
     * @param userId user ID for the secondary accounts query
     * @param basicAuthBase64 Base64-encoded Basic Auth credentials
     * @return set of blocked account IDs
     */
    static Set<String> fetchBlockedSecondaryAccountsFromService(
            Set<String> accountIds, String secondaryAccountsApi, String userId, String basicAuthBase64)
            throws CDSAccountValidationException {

        Set<String> blockedAccounts = new HashSet<>();

        if (accountIds == null || accountIds.isEmpty()) {
            return blockedAccounts;
        }

        try {
            String accountIdsParam = URLEncoder.encode(String.join(",", accountIds), StandardCharsets.UTF_8);
            String userIdParam = URLEncoder.encode(userId, StandardCharsets.UTF_8);
            String requestUrl = secondaryAccountsApi + "?" + CDSAccountValidationConstants.ACCOUNT_IDS_TAG + "="
                    + accountIdsParam + "&" + CDSAccountValidationConstants.USER_ID_TAG + "=" + userIdParam;

            HttpGet request = new HttpGet(requestUrl);
            request.addHeader(CDSAccountValidationConstants.ACCEPT_TAG,
                    CDSAccountValidationConstants.JSON_CONTENT_TYPE);
            request.addHeader(CDSAccountValidationConstants.AUTH_HEADER,
                    CDSAccountValidationConstants.BASIC_TAG + basicAuthBase64);

            try (CloseableHttpResponse response = apacheHttpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    JSONArray secondaryAccounts;
                    try {
                        secondaryAccounts = new JSONArray(responseBody);
                    } catch (JSONException e) {
                        String errorMessage = "Invalid secondary accounts service response";
                        log.error(errorMessage, e);
                        throw new CDSAccountValidationException(errorMessage, e);
                    }

                    for (int i = 0; i < secondaryAccounts.length(); i++) {
                        JSONObject accountInstruction = secondaryAccounts.optJSONObject(i);
                        if (accountInstruction == null) {
                            continue;
                        }
                        String instructionStatus = accountInstruction.optString(
                                CDSAccountValidationConstants.SECONDARY_ACCOUNT_INSTRUCTION_STATUS_TAG, null);
                        if (CDSAccountValidationConstants.SECONDARY_ACCOUNT_STATUS_INACTIVE
                                .equalsIgnoreCase(instructionStatus)) {
                            String accountId = accountInstruction.optString(
                                    CDSAccountValidationConstants.CDS_ACCOUNT_ID_TAG, null);
                            if (StringUtils.isNotBlank(accountId)) {
                                blockedAccounts.add(accountId);
                            }
                        }
                    }
                } else {
                    String errorMessage = "Secondary accounts service returned HTTP " + statusCode;
                    log.error(errorMessage);
                    throw new CDSAccountValidationException(errorMessage);
                }
            }
        } catch (IOException e) {
            String errorMessage = "[SecondaryAccounts] Error calling secondary accounts service";
            log.error(errorMessage, e);
            throw new CDSAccountValidationException(errorMessage, e);
        }
        return blockedAccounts;
    }

    /**
     * Call business stakeholders GET endpoint and return blocked account IDs.
     * An account is considered blocked when business permission is not AUTHORIZE.
     *
     * @param accountIds set of account IDs to check
     * @param businessStakeholdersApi business stakeholders API endpoint
     * @param userId user ID for the business stakeholders query
     * @param basicAuthBase64 Base64-encoded Basic Auth credentials
     * @return set of blocked account IDs
     */
    static Set<String> fetchBlockedBusinessAccountsFromService(Set<String> accountIds, String businessStakeholdersApi,
                                                               String userId, String basicAuthBase64)
            throws CDSAccountValidationException {

        Set<String> blockedAccounts = new HashSet<>();

        if (accountIds == null || accountIds.isEmpty()) {
            return blockedAccounts;
        }

        try {
            String accountIdsParam = URLEncoder.encode(String.join(",", accountIds), StandardCharsets.UTF_8);
            String userIdParam = URLEncoder.encode(userId, StandardCharsets.UTF_8);
            String requestUrl = businessStakeholdersApi + "?" + CDSAccountValidationConstants.ACCOUNT_IDS_TAG + "="
                    + accountIdsParam + "&" + CDSAccountValidationConstants.USER_ID_TAG + "=" + userIdParam;

            HttpGet request = new HttpGet(requestUrl);
            request.addHeader(CDSAccountValidationConstants.ACCEPT_TAG,
                    CDSAccountValidationConstants.JSON_CONTENT_TYPE);
            request.addHeader(CDSAccountValidationConstants.AUTH_HEADER,
                    CDSAccountValidationConstants.BASIC_TAG + basicAuthBase64);

            try (CloseableHttpResponse response = apacheHttpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    JSONArray businessStakeholders;
                    try {
                        businessStakeholders = new JSONArray(responseBody);
                    } catch (JSONException e) {
                        String errorMessage = "Invalid business stakeholders service response";
                        log.error(errorMessage, e);
                        throw new CDSAccountValidationException(errorMessage, e);
                    }

                    for (int i = 0; i < businessStakeholders.length(); i++) {
                        JSONObject permissionItem = businessStakeholders.optJSONObject(i);
                        if (permissionItem == null) {
                            continue;
                        }
                        String permission = permissionItem.optString(
                                CDSAccountValidationConstants.BUSINESS_PERMISSION_TAG, null);
                        if (!CDSAccountValidationConstants.BUSINESS_PERMISSION_AUTHORIZE
                                .equalsIgnoreCase(permission)) {
                            String accountId = permissionItem.optString(
                                    CDSAccountValidationConstants.CDS_ACCOUNT_ID_TAG, null);
                            if (StringUtils.isNotBlank(accountId)) {
                                blockedAccounts.add(accountId);
                            }
                        }
                    }
                } else {
                    String errorMessage = "Business stakeholders service returned HTTP " + statusCode;
                    log.error(errorMessage);
                    throw new CDSAccountValidationException(errorMessage);
                }
            }
        } catch (IOException e) {
            String errorMessage = "[BusinessStakeholders] Error calling business stakeholders service";
            log.error(errorMessage, e);
            throw new CDSAccountValidationException(errorMessage, e);
        }
        return blockedAccounts;
    }

    /**
     * Call legal-entity GET endpoint and return blocked account IDs for the requesting client's legal entity.
     * The {@code clientId} is forwarded to the account-metadata service, which resolves it to a legal entity ID
     * via the IS server and filters the response accordingly.
     *
     * @param accountIds set of account IDs to check
     * @param legalEntitySharingApi legal-entity API endpoint
     * @param userId user ID for the legal-entity query
     * @param basicAuthBase64 Base64-encoded Basic Auth credentials
     * @param clientId software product client ID
     * @return set of blocked account IDs by legal entity
     */
    static Set<String> fetchBlockedLegalEntityAccountsFromService
    (Set<String> accountIds, String legalEntitySharingApi, String userId, String basicAuthBase64, String clientId)
            throws CDSAccountValidationException {

        Set<String> blockedAccounts = new HashSet<>();

        if (accountIds == null || accountIds.isEmpty() || StringUtils.isBlank(clientId)) {
            return blockedAccounts;
        }

        try {
            String accountIdsParam = URLEncoder.encode(String.join(",", accountIds), StandardCharsets.UTF_8);
            String userIdParam = URLEncoder.encode(userId, StandardCharsets.UTF_8);
            String clientIdParam = URLEncoder.encode(clientId, StandardCharsets.UTF_8);
            String requestUrl = legalEntitySharingApi + "?" + CDSAccountValidationConstants.ACCOUNT_IDS_TAG + "="
                    + accountIdsParam + "&" + CDSAccountValidationConstants.USER_ID_TAG + "=" + userIdParam
                    + "&" + CDSAccountValidationConstants.CLIENT_ID_TAG + "=" + clientIdParam;

            HttpGet request = new HttpGet(requestUrl);
            request.addHeader(CDSAccountValidationConstants.ACCEPT_TAG,
                    CDSAccountValidationConstants.JSON_CONTENT_TYPE);
            if (StringUtils.isNotBlank(basicAuthBase64)) {
                request.addHeader(CDSAccountValidationConstants.AUTH_HEADER,
                        CDSAccountValidationConstants.BASIC_TAG + basicAuthBase64);
            }

            try (CloseableHttpResponse response = apacheHttpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    String responseBody;
                    try (InputStream inputStream = response.getEntity().getContent()) {
                        responseBody = IOUtils.toString(inputStream, "UTF-8");
                    }

                    JSONArray legalEntitySharingItems;
                    try {
                        legalEntitySharingItems = new JSONArray(responseBody);
                    } catch (JSONException e) {
                        String errorMessage = "Invalid legal entity sharing service response";
                        log.error(errorMessage, e);
                        throw new CDSAccountValidationException(errorMessage, e);
                    }

                    for (int i = 0; i < legalEntitySharingItems.length(); i++) {
                        JSONObject sharingItem = legalEntitySharingItems.optJSONObject(i);
                        if (sharingItem == null) {
                            continue;
                        }

                        String sharingStatus = sharingItem.optString(
                                CDSAccountValidationConstants.LEGAL_ENTITY_SHARING_STATUS_TAG, null);

                        if (CDSAccountValidationConstants.LEGAL_ENTITY_SHARING_STATUS_BLOCKED
                                .equalsIgnoreCase(sharingStatus)) {
                            String accountId = sharingItem.optString(
                                    CDSAccountValidationConstants.ACCOUNT_ID_UPPER_CASE_TAG, sharingItem.optString(
                                            CDSAccountValidationConstants.CDS_ACCOUNT_ID_TAG, null));
                            if (StringUtils.isNotBlank(accountId)) {
                                blockedAccounts.add(accountId);
                            }
                        }
                    }
                } else {
                    String errorMessage = "Legal-entity service returned HTTP " + statusCode;
                    log.error(errorMessage);
                    throw new CDSAccountValidationException(errorMessage);
                }
            }
        } catch (IOException e) {
            String errorMessage = "[LegalEntity] Error calling legal-entity service";
            log.error(errorMessage, e);
            throw new CDSAccountValidationException(errorMessage, e);
        }
        return blockedAccounts;
    }
}

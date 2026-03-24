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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.openbanking.consumerdatastandards.au.policy.constants.CDSAccountValidationConstants;
import org.wso2.openbanking.consumerdatastandards.au.policy.exceptions.CDSAccountValidationException;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.text.ParseException;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for the Consent Enforcement Policy.
 */
public class CDSAccountValidationUtils {

    private static final Log log = LogFactory.getLog(CDSAccountValidationUtils.class);

    @Setter
    private static HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(CDSAccountValidationConstants.HTTP_CLIENT_CONNECT_TIMEOUT_MILLIS))
            .build();

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
     * Fetch all blocked account IDs by checking both the disclosure options
     * and secondary accounts services.
     *
     * @param accountIds set of account IDs to check
     * @param baseUrl base URL of the account metadata webapp
     * @param userId user ID for the secondary accounts query
     * @param basicAuthBase64 Base64-encoded Basic Auth credentials
     * @return combined set of blocked account IDs from both services
     */
    public static Set<String> fetchAllBlockedAccounts(
            Set<String> accountIds, String baseUrl, String userId, String basicAuthBase64)
            throws CDSAccountValidationException {

        String disclosureOptionsApi = baseUrl + CDSAccountValidationConstants.DISCLOSURE_OPTIONS_PATH;

        Set<String> blockedAccounts = fetchBlockedJointAccountsFromService(accountIds, disclosureOptionsApi,
                basicAuthBase64);

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
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(URI.create(requestUrl))
                    .timeout(Duration.ofMillis(CDSAccountValidationConstants.HTTP_REQUEST_TIMEOUT_MILLIS))
                    .header(CDSAccountValidationConstants.ACCEPT_TAG, CDSAccountValidationConstants.JSON_CONTENT_TYPE)
                    .GET();

            requestBuilder.header(CDSAccountValidationConstants.AUTH_HEADER,
                    CDSAccountValidationConstants.BASIC_TAG + basicAuthBase64);

            HttpRequest request = requestBuilder.build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONArray disclosureOptions;
                try {
                    disclosureOptions = new JSONArray(response.body());
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

                    String disclosureOption =
                            accountDisclosure.optString(CDSAccountValidationConstants.DISCLOSURE_OPTION_TAG, null);
                    if (CDSAccountValidationConstants.DOMS_STATUS_NO_SHARING.equalsIgnoreCase(disclosureOption)) {
                        String accountId = accountDisclosure.optString(
                                CDSAccountValidationConstants.CDS_ACCOUNT_ID_TAG, null);
                        if (StringUtils.isNotBlank(accountId)) {
                            blockedAccounts.add(accountId);
                        }
                    }
                }
            } else {
                String errorMessage = "Disclosure options service returned HTTP " + response.statusCode();
                log.error(errorMessage);
                throw new CDSAccountValidationException(errorMessage);
            }

        } catch (IOException e) {
            String errorMessage = "[DOMS] Error calling disclosure options service";
            log.error(errorMessage, e);
            throw new CDSAccountValidationException(errorMessage, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            String errorMessage = "[DOMS] Interrupted while calling disclosure options service";
            log.error(errorMessage, e);
            throw new CDSAccountValidationException(errorMessage, e);
        }

        return blockedAccounts;
    }
}

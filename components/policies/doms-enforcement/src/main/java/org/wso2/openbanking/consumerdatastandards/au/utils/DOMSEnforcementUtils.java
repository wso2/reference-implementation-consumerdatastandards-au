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

package org.wso2.openbanking.consumerdatastandards.au.utils;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.openbanking.consumerdatastandards.au.constants.DOMSEnforcementConstants;

import java.io.IOException;
import java.net.URI;
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
public class DOMSEnforcementUtils {

    private static final Log log = LogFactory.getLog(DOMSEnforcementUtils.class);

    /**
     * Method to generate JWT with the given payload.
     *
     * @param payload JSON payload as a string to be included in the JWT claims
     * @return Serialized JWT as a string
     * @throws ParseException
     * @throws JOSEException
     */
    public static String generateJWT(String payload) throws ParseException, JOSEException {

        log.debug("Generating JWT with provided payload");
        RSASSASigner signer = new RSASSASigner((PrivateKey) KeyStoreUtils.getSigningKey());
        JWTClaimsSet claimsSet = JWTClaimsSet.parse(payload);

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS512)
                .type(JOSEObjectType.JWT)
                .build();

        SignedJWT signedJWT = new SignedJWT(header, claimsSet);
        signedJWT.sign(signer);
        log.info("JWT generated successfully");
        return signedJWT.serialize();
    }

    /**
     * Decode JWT payload into JSONObject (without validating signature).
     *
     * @param jwt JWT string
     * @return Decoded payload as JSONObject
     * @throws ParseException if JWT format is invalid
     * @throws JSONException if payload is not valid JSON
     */
    public static JSONObject decodeJWT(String jwt)
            throws ParseException, JSONException {

        if (StringUtils.isBlank(jwt)) {
            throw new ParseException("JWT is null or empty", 0);
        }

        String[] jwtParts = jwt.split("\\.");
        if (jwtParts.length != 3) {
            throw new ParseException("Invalid JWT format", 0);
        }

        try {
            byte[] decodedBytes = java.util.Base64.getUrlDecoder().decode(jwtParts[1]);
            String decodedPayload = new String(decodedBytes, StandardCharsets.UTF_8);

            log.debug("Decoded JWT payload: " + decodedPayload);
            return new JSONObject(decodedPayload);

        } catch (IllegalArgumentException e) {
            log.error("Failed to Base64URL decode JWT payload", e);
            throw new ParseException("Failed to decode JWT payload", 0);
        }
    }

    /**
     * Call the blocked accounts service and return the blocked account IDs.
     *
     * @param accountIds set of account IDs to check
     * @param blockedAccountsApi blocked accounts API endpoint
     * @param basicAuthBase64 Base64-encoded Basic Auth credentials
     * @return set of blocked account IDs
     */
    public static Set<String> fetchBlockedAccountsFromService(
            Set<String> accountIds, String blockedAccountsApi, String basicAuthBase64) {

        Set<String> blockedAccounts = new HashSet<>();

        try {
            JSONObject requestJson = new JSONObject();
            requestJson.put(DOMSEnforcementConstants.ACCOUNT_IDS_TAG, new JSONArray(accountIds));

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(3000))
                    .build();

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(blockedAccountsApi))
                    .timeout(Duration.ofMillis(3000))
                    .header(DOMSEnforcementConstants.CONTENT_TYPE_TAG, DOMSEnforcementConstants.JSON_CONTENT_TYPE)
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson.toString(), StandardCharsets.UTF_8));

            if (StringUtils.isNotBlank(basicAuthBase64)) {
                requestBuilder.header(DOMSEnforcementConstants.AUTH_HEADER,
                        DOMSEnforcementConstants.BASIC_TAG + basicAuthBase64);
            } else {
                log.warn("[DOMS] Basic Auth property not set, request for fetching blocked accounts may fail");
            }

            HttpRequest request = requestBuilder.build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());


            if (response.statusCode() == 200) {
                JSONObject responseJson = new JSONObject(response.body());
                JSONArray blockedArray =
                        responseJson.optJSONArray(DOMSEnforcementConstants.BLOCKED_ACCOUNT_IDS_TAG);

                if (blockedArray != null) {
                    for (int i = 0; i < blockedArray.length(); i++) {
                        blockedAccounts.add(blockedArray.getString(i));
                    }
                }
            } else {
                log.warn("Blocked accounts service returned HTTP " + response.statusCode());
            }

        } catch (IOException | InterruptedException e) {
            log.error("[DOMS] Error calling blocked accounts service", e);
        }

        return blockedAccounts;
    }

}

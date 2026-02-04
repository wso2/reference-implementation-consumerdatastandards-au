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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.openbanking.consumerdatastandards.au.constants.DOMSEnforcementConstants;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for the Consent Enforcement Policy.
 */
public class DOMSEnforcementUtils {

    private static final Log log = LogFactory.getLog(DOMSEnforcementUtils.class);

    /**
     * Method to construct resource parameter map to invoke the validation service.
     *
     * @param messageContext request context object
     * @return A Map containing resource path(ex: /aisp/accounts/{AccountId}?queryParam=urlEncodedQueryParamValue),
     * http method and context(ex: /open-banking/v3.1/aisp)
     */
    public static Map<String, String> getResourceParamMap(MessageContext messageContext) {

        log.debug("Constructing resource parameter map for validation service");
        Map<String, String> resourceMap = new HashMap<>();
        resourceMap.put(DOMSEnforcementConstants.RESOURCE_TAG, (String)
                messageContext.getProperty(DOMSEnforcementConstants.REST_FULL_REQUEST_PATH));
        resourceMap.put(DOMSEnforcementConstants.HTTP_METHOD_TAG, (String)
                messageContext.getProperty(DOMSEnforcementConstants.REST_METHOD));
        resourceMap.put(DOMSEnforcementConstants.CONTEXT_TAG, (String)
                messageContext.getProperty(DOMSEnforcementConstants.REST_API_CONTEXT));
        return resourceMap;
    }

    /**
     * Method to extract the consent ID from the JWT token present in the request headers.
     *
     * @param headers Transport headers from the Axis2 message context
     * @param consentIdClaimName Name of the claim that contains the consent ID
     * @return Consent ID if present in the JWT token, null otherwise
     * @throws UnsupportedEncodingException When encoding is not UTF-8
     */
    public static String extractConsentIdFromJwtToken(Map<String, String> headers, String consentIdClaimName)
            throws UnsupportedEncodingException {

        log.debug("Attempting to extract consent ID from JWT token");
        String authHeader = headers.get(DOMSEnforcementConstants.AUTH_HEADER);
        if (authHeader != null && !authHeader.isEmpty() &&
                isValidJWTToken(authHeader.replace(DOMSEnforcementConstants.BEARER_TAG, ""))) {
            String consentIdClaim = null;
            if (!authHeader.contains(DOMSEnforcementConstants.BASIC_TAG)) {
                authHeader = authHeader.replace(DOMSEnforcementConstants.BEARER_TAG, "");
                JSONObject jwtClaims = decodeBase64(authHeader.split("\\.")[1]);

                if (!jwtClaims.isNull(consentIdClaimName) && !jwtClaims.getString(consentIdClaimName).isEmpty()) {
                    consentIdClaim = jwtClaims.getString(consentIdClaimName);
                    log.info("Consent ID extracted successfully from JWT token");
                }
            }
            return consentIdClaim;
        }
        log.debug("No valid JWT token found in authorization header");
        return null;
    }

    /**
     * Method to create the validation request payload.
     *
     * @param jsonPayload JSON payload as a string to be included in the request body
     * @param requestHeaders Transport headers from the Axis2 message context
     * @param additionalParams Additional parameters to be included in the request payload
     * @return JSONObject representing the validation request payload
     */
    public static JSONObject createValidationRequestPayload(String jsonPayload,
                                                            Map<String, String> requestHeaders,
                                                            Map<String, Object> additionalParams) throws JSONException {

        log.debug("Creating validation request payload");
        JSONObject validationRequest = new JSONObject();
        JSONObject headers = new JSONObject();

        requestHeaders.forEach(headers::put);
        validationRequest.put(DOMSEnforcementConstants.HEADERS_TAG, headers);

        JSONObject requestPayload = new JSONObject(jsonPayload);
        validationRequest.put(DOMSEnforcementConstants.BODY_TAG, requestPayload);

        additionalParams.forEach(validationRequest::put);
        log.debug("Validation request payload created successfully");
        return validationRequest;
    }

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
     * Method to check whether the given string is a valid JWT token.
     *
     * @param jwtString JWT token string
     * @return true if the given string is a valid JWT token, false otherwise
     */
    private static boolean isValidJWTToken(String jwtString) {

        String[] jwtPart = jwtString.split("\\.");
        if (jwtPart.length != 3) {
            return false;
        }
        try {
            decodeBase64(jwtPart[0]);
            decodeBase64(jwtPart[1]);
        } catch (UnsupportedEncodingException | JSONException | IllegalArgumentException e) {
            log.error("Failed to decode the JWT token. %s", e);
            return false;
        }
        return true;
    }

    /**
     * Method to decode the base64 encoded JSON payload.
     *
     * @param payload base64 encoded payload
     * @return Decoded JSON Object
     * @throws UnsupportedEncodingException When encoding is not UTF-8
     */
    private static JSONObject decodeBase64(String payload) throws UnsupportedEncodingException {

        return new JSONObject(new String(java.util.Base64.getDecoder().decode(payload),
                String.valueOf(StandardCharsets.UTF_8)));
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

        if (jwt == null || jwt.isEmpty()) {
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

}

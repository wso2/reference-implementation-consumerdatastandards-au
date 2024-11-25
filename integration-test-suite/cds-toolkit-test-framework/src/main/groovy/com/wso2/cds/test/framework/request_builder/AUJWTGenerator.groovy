/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package com.wso2.cds.test.framework.request_builder

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.oauth2.sdk.AuthorizationRequest
import com.nimbusds.oauth2.sdk.ResponseMode
import com.nimbusds.oauth2.sdk.id.ClientID
import com.nimbusds.oauth2.sdk.pkce.CodeChallenge
import com.nimbusds.oauth2.sdk.pkce.CodeChallengeMethod
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier
import com.wso2.bfsi.test.framework.exception.TestFrameworkException
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSObject
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jose.Payload
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jwt.JWT
import com.nimbusds.jwt.SignedJWT
import com.wso2.cds.test.framework.constant.AUAccountScope
import com.wso2.cds.test.framework.constant.AUConstants
import com.wso2.cds.test.framework.utility.AUTestUtil
import com.wso2.openbanking.test.framework.request_builder.JSONRequestGenerator
import com.wso2.openbanking.test.framework.request_builder.PayloadGenerator
import com.wso2.cds.test.framework.configuration.AUConfigurationService
import com.wso2.cds.test.framework.keystore.AUKeyStore
import com.wso2.openbanking.test.framework.keystore.OBKeyStore
import io.restassured.response.Response
import org.apache.commons.lang3.StringUtils
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.json.JSONException
import org.json.JSONObject
import org.testng.Reporter

import java.nio.charset.StandardCharsets
import java.security.Key
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.Security
import java.security.cert.Certificate
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Class for generate JWT
 */
class AUJWTGenerator {

    private AUConfigurationService auConfiguration
    private List<String> scopesList = null // Scopes can be set before generate payload
    private String signingAlgorithm
    private AUAuthorisationBuilder auAuthorisationBuilder
    private static CodeVerifier codeVerifier = new CodeVerifier()
    AuthorizationRequest authRequest

    AUJWTGenerator() {
        auConfiguration = new AUConfigurationService()
    }

    void setScopes(List<String> scopes) {
        scopesList = scopes
    }

    /**
     * Set signing algorithm
     * @param algorithm
     */
    void setSigningAlgorithm(String algorithm) {
        this.signingAlgorithm = algorithm
    }

    /**
     * Get signing algorithm for methods. IF signing algorithm is null, provide algorithm in configuration
     * @return
     */
    String getSigningAlgorithm() {
        if (signingAlgorithm == null) {
            signingAlgorithm = auConfiguration.getCommonSigningAlgorithm()
        }
        return this.signingAlgorithm
    }


    /**
     * Get Signed object
     * @param claims JSON object
     * @param algorithm Signing algorithm
     * @return Signed object
     */
    String getSignedRequestObject(String claims) {
        Key signingKey
        JWSHeader header
        if (auConfiguration.getMockCDREnabled()) {
            Certificate certificate = AUKeyStore.getCertificateFromMockCDRKeyStore()
            String thumbprint = AUKeyStore.getJwkThumbPrintForSHA256(certificate)
            header = new JWSHeader.Builder(JWSAlgorithm.parse(getSigningAlgorithm()))
                    .keyID(thumbprint).type(JOSEObjectType.JWT).build()
            signingKey = AUKeyStore.getMockCDRSigningKey()
        } else {
            Certificate certificate = OBKeyStore.getApplicationCertificate()
            String thumbprint = OBKeyStore.getJwkThumbPrintForSHA1(certificate)
            header = new JWSHeader.Builder(JWSAlgorithm.parse(getSigningAlgorithm()))
                    .keyID(thumbprint).type(JOSEObjectType.JWT).build()
            signingKey = OBKeyStore.getApplicationSigningKey()
        }
        JWSObject jwsObject = new JWSObject(header, new Payload(claims))
        JWSSigner signer = new RSASSASigner((PrivateKey) signingKey)
        Security.addProvider(new BouncyCastleProvider())
        jwsObject.sign(signer)
        return jwsObject.serialize()
    }

    /**
     * Return JWT for application access token generation
     * @param clientId
     * @return
     * @throws TestFrameworkException
     */
    String getAppAccessTokenJwt(String clientId = null) throws TestFrameworkException {

        JSONObject clientAssertion = new JSONRequestGenerator().addIssuer(clientId)
                .addSubject(clientId).addAudience().addExpireDate().addIssuedAt().addJti().getJsonObject()

        String payload = getSignedRequestObject(clientAssertion.toString())
        String accessTokenJWT = new PayloadGenerator().addGrantType().addScopes(scopesList).addClientAsType()
                .addClientAssertion(payload).addRedirectUri().getPayload()
        return accessTokenJWT
    }

    /**
     * Get Client Assertion.
     * @param clientId - Client ID
     * @return clientAssertion - Client Assertion
     */
    String getClientAssertionJwt(String clientId=null) {
        JSONObject clientAssertion = new JSONRequestGenerator().addIssuer(clientId)
                .addSubject(clientId).addAudience().addExpireDate().addIssuedAt().addJti().getJsonObject()

        String payload = getSignedRequestObject(clientAssertion.toString())
        return payload
    }

    String getClientAssertionJwtWithoutIAT(String clientId=null) {
        JSONObject clientAssertion = new JSONRequestGenerator().addIssuer(clientId)
                .addSubject(clientId).addAudience().addExpireDate().addJti().getJsonObject()

        String payload = getSignedRequestObject(clientAssertion.toString())
        return payload
    }

    /**
     * Get Client Assertion with customized Issuer and Audience
     * @param issuer Issuer
     * @param audience Audience
     * @return jwt
     */
    String getClientAssertionJwt(String issuer, String audience) {
        JSONObject clientAssertion = new JSONRequestGenerator().addIssuer(issuer)
                .addSubject(issuer).addAudience(audience).addExpireDate().addIssuedAt().addJti().getJsonObject()

        String payload = getSignedRequestObject(clientAssertion.toString())
        return payload
    }

    /**
     * Return JWT for user access token generation
     * @param code
     * @return
     * @throws TestFrameworkException
     */
    String getUserAccessTokenJwt(String code = "") throws TestFrameworkException {

        JSONObject clientAssertion = new JSONRequestGenerator().addIssuer()
                .addSubject().addAudience().addExpireDate().addIssuedAt().addJti().getJsonObject()
        String payload = getSignedRequestObject(clientAssertion.toString())
        String accessTokenJWT = new PayloadGenerator().addGrantType().addCode(code).addScopes().addClientAsType()
                .addClientAssertion(payload).addRedirectUri().addClientID().getPayload()
        return accessTokenJWT
    }

    /**
     * Return signed JWT for Authorization request
     * @param scopeString
     * @param sharingDuration
     * @param sendSharingDuration
     * @param cdrArrangementId
     * @param redirect_uri
     * @param clientId
     * @return
     */
    JWT getSignedAuthRequestObject(String requestObjectPayload) {

        String payload = getSignedRequestObject(requestObjectPayload)

        Reporter.log("Authorisation Request Object")
        Reporter.log("JWS Payload ${new Payload(payload).toString()}")

        return SignedJWT.parse(payload)
    }

    /**
     * Extract JWT token and assign to a map.
     * @param jwtToken jwt token
     * @return jwt claim set
     */
    static JWTClaimsSet extractJwt(String jwtToken) {

        SignedJWT signedJWT = SignedJWT.parse(jwtToken)
        JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet()

        return claimsSet
    }

    /**
     * Extract JWT token headers and assign to a map.
     * @param jwtToken jwt token
     * @return jwt header set
     */
    static JWSHeader extractJwtHeaders(String jwtToken) {

        SignedJWT signedJWT = SignedJWT.parse(jwtToken)
        JWSHeader headerSet = signedJWT.getHeader()

        return headerSet
    }

    /**
     * Return signed JWT for Authorization request with string sharing duration.
     * The method is used for testing the auth request with string sharing duration.
     * @param scopeString
     * @param sharingDuration
     * @param sendSharingDuration
     * @param cdrArrangementId
     * @param redirect_uri
     * @param clientId
     * @return
     */
    JWT getSignedAuthRequestObjectForStringSharingDuration(String scopeString, String sharingDuration,
                                                           String cdrArrangementId, String redirect_uri, String clientId, String responseType,
                                                           String responseMode, CodeChallengeMethod codeChallengeMethod) {

        //Generate Code Challenge
        CodeChallenge codeChallenge = CodeChallenge.compute(codeChallengeMethod, AUConstants.CODE_VERIFIER)
        String codeChallengeValue = codeChallenge.getValue()

        def expiryDate = Instant.now().plus(1, ChronoUnit.HOURS)
        Instant notBefore = Instant.now()
        String claims

        JSONObject acr = new JSONObject().put("essential", true).put("values", new ArrayList<String>() {
            {
                add("urn:cds.au:cdr:3")
            }
        })
        JSONObject userInfoString = new JSONObject().put("given_name", null).put("family_name", null)
        JSONObject claimsString = new JSONObject().put("id_token", new JSONObject().put("acr", acr)).put("userinfo", userInfoString)
        claimsString.put("sharing_duration", sharingDuration)
        if (!StringUtils.isEmpty(cdrArrangementId)) {
            claimsString.put("cdr_arrangement_id", cdrArrangementId)
        }
        claims = new JSONRequestGenerator()
                .addAudience()
                .addResponseType(responseType)
                .addExpireDate(expiryDate.getEpochSecond().toLong())
                .addClientID(clientId)
                .addIssuer(clientId)
                .addRedirectURI(redirect_uri)
                .addScope(scopeString)
                .addNonce()
                .addCustomValue("nbf", notBefore.getEpochSecond().toLong())
                .addCustomJson("claims", claimsString)
                .addCustomValue("response_mode", responseMode)
                .addCustomValue("code_challenge_method", codeChallengeMethod)
                .addCustomValue("code_challenge", codeChallengeValue)
                .getJsonObject().toString()

        String payload = getSignedRequestObject(claims)

        Reporter.log("Authorisation Request Object")
        Reporter.log("JWS Payload ${new Payload(claims).toString()}")

        return SignedJWT.parse(payload)
    }

    /**
     * Utility function to generate a regular claim set with default values
     * @param scopeString - scope string
     * @param sharingDuration - sharing duration
     * @param sendSharingDuration - send sharing duration
     * @param cdrArrangementId - cdr arrangement id
     * @param redirect_uri - redirect uri
     * @param clientId - client id
     * @param responseType - response type
     * @param isStateRequired - is state required
     * @param state - state
     * @param expiryDate - expiry date
     * @param notBefore - not before
     * @return claimSet
     */
    String getRequestObjectClaim(List<AUAccountScope> scopes, Long sharingDuration, Boolean sendSharingDuration,
                                 String cdrArrangementId, String redirect_uri, String clientId, String responseType,
                                 boolean isStateRequired = true, String state, String responseMode = ResponseMode.JWT,
                                 Instant expiryDate = Instant.now().plus(1, ChronoUnit.HOURS),
                                 Instant notBefore = Instant.now(),
                                 CodeChallengeMethod codeChallengeMethod = CodeChallengeMethod.S256) {
        String claims

        String scopeString = "openid ${String.join(" ", scopes.collect({ it.scopeString }))}"

        //Generate Code Challenge
        CodeChallenge codeChallenge = CodeChallenge.compute(codeChallengeMethod, AUConstants.CODE_VERIFIER)
        String codeChallengeValue = codeChallenge.getValue()

        //Define additional claims
        JSONObject acr = new JSONObject().put("essential", true).put("values", new ArrayList<String>() {
            {
                add("urn:cds.au:cdr:3")
            }
        })
        JSONObject authTimeString = new JSONObject().put("essential", true)
        JSONObject maxAgeString = new JSONObject().put("essential", true).put("max_age", 86400)
        JSONObject userInfoString = new JSONObject().put("name", null).put("given_name", null).put("family_name", null).put("updated_at", Instant.now())
        JSONObject claimsString = new JSONObject().put("id_token", new JSONObject().put("acr", acr).put("auth_time", authTimeString))
        if (sendSharingDuration) {
            claimsString.put("sharing_duration", sharingDuration)
        }

        if (!StringUtils.isEmpty(cdrArrangementId)) {
            claimsString.put("cdr_arrangement_id", cdrArrangementId)
        }

        if (isStateRequired) {
            claims = new JSONRequestGenerator()
                    .addAudience()
                    .addResponseType(responseType)
                    .addExpireDate(expiryDate.getEpochSecond().toLong())
                    .addClientID(clientId)
                    .addIssuer(clientId)
                    .addRedirectURI(redirect_uri)
                    .addScope(scopeString)
                    .addState(state)
                    .addNonce()
                    .addCustomValue("max_age", maxAgeString)
                    .addCustomValue("nbf", notBefore.getEpochSecond().toLong())
                    .addCustomJson("claims", claimsString)
                    .addCustomValue("response_mode", responseMode)
                    .addCustomValue("code_challenge_method", codeChallengeMethod)
                    .addCustomValue("code_challenge", codeChallengeValue)
                    .addCustomValue("userinfo", userInfoString)
                    .getJsonObject().toString()
        } else {
            claims = new JSONRequestGenerator()
                    .addAudience()
                    .addResponseType(responseType)
                    .addExpireDate(expiryDate.getEpochSecond().toLong())
                    .addClientID(clientId)
                    .addIssuer(clientId)
                    .addRedirectURI(redirect_uri)
                    .addScope(scopeString)
                    .addNonce()
                    .addCustomValue("max_age", maxAgeString)
                    .addCustomValue("nbf", notBefore.getEpochSecond().toLong())
                    .addCustomJson("claims", claimsString)
                    .addCustomValue("response_mode", responseMode)
                    .addCustomValue("code_challenge_method", codeChallengeMethod)
                    .addCustomValue("code_challenge", codeChallengeValue)
                    .addCustomValue("userinfo", userInfoString)
                    .getJsonObject().toString()
        }
        return claims
    }

    /**
     * Remove claims from Request Object.
     * @param claims - Request Object
     * @param nodeToBeRemoved - Node to be removed from the Request Object
     * @return modifiedJsonPayload - Modified Request Object
     */
    static String removeClaimsFromRequestObject(String claims, String nodeToBeRemoved) {

        // Parse the JSON payload
        ObjectMapper objectMapper = new ObjectMapper()
        JsonNode rootNode = objectMapper.readTree(claims)

        // Remove elements from the JSON payload
        if (rootNode instanceof ObjectNode) {
            ObjectNode objectNode = (ObjectNode) rootNode
            objectNode.remove(nodeToBeRemoved)
        }

        // Convert the modified JSON back to a string
        String modifiedJsonPayload = objectMapper.writeValueAsString(rootNode)
        System.out.println(modifiedJsonPayload)

        return modifiedJsonPayload
    }

    /**
     * Add Claims to Request Object.
     * @param claims
     * @param updatedClaim
     * @param nodeToBeAdded
     * @return newClaims
     */
    static String addClaimsFromRequestObject(String claims, String updatedClaim, String nodeToBeAdded) {

        JSONObject payload = new JSONObject(claims)
        try {
            payload.put(updatedClaim, nodeToBeAdded)
        } catch (JSONException e) {
            e.printStackTrace()
        }
    }

    /**
     * Add Claims to Request Object.
     * @param claims
     * @param updatedClaim
     * @param nodeToBeAdded
     * @return newClaims
     */
    static String addClaimsFromRequestObject(String claims, String updatedClaim, Long nodeToBeAdded) {

        JSONObject payload = new JSONObject(claims)
        try {
            payload.put(updatedClaim, nodeToBeAdded)
        } catch (JSONException e) {
            e.printStackTrace()
        }
    }

    /**
     * Utility function to generate a regular claim set with default values
     * @param scopeString - scope string
     * @param sharingDuration - sharing duration
     * @param sendSharingDuration - send sharing duration
     * @param cdrArrangementId - cdr arrangement id
     * @param redirect_uri - redirect uri
     * @param clientId - client id
     * @param responseType - response type
     * @param isStateRequired - is state required
     * @param state - state
     * @param expiryDate - expiry date
     * @param notBefore - not before
     * @return claimSet
     */
    String getRequestObjectClaimWithMaxAge(List<AUAccountScope> scopes, long sharingDuration, Boolean sendSharingDuration,
                                           String cdrArrangementId, String redirect_uri, String clientId, String responseType,
                                           boolean isStateRequired = true, String state, String responseMode = ResponseMode.JWT,
                                           Instant expiryDate = Instant.now().plus(1, ChronoUnit.HOURS),
                                           Instant notBefore = Instant.now(),
                                           CodeChallengeMethod codeChallengeMethod = CodeChallengeMethod.S256) {
        String claims

        String scopeString = "openid ${String.join(" ", scopes.collect({ it.scopeString }))}"

        //Generate Code Challenge
        CodeChallenge codeChallenge = CodeChallenge.compute(codeChallengeMethod, AUConstants.CODE_VERIFIER)
        String codeChallengeValue = codeChallenge.getValue()

        //Define additional claims
        JSONObject acr = new JSONObject().put("essential", true).put("values", new ArrayList<String>() {
            {
                add("urn:cds.au:cdr:3")
            }
        })
        JSONObject userInfoString = new JSONObject().put("given_name", null).put("family_name", null)
        JSONObject claimsString = new JSONObject().put("id_token", new JSONObject().put("acr", acr))
        if (sharingDuration.intValue() != 0 || sendSharingDuration) {
            claimsString.put("sharing_duration", sharingDuration)
        }
        if (!StringUtils.isEmpty(cdrArrangementId)) {
            claimsString.put("cdr_arrangement_id", cdrArrangementId)
        }

        if (isStateRequired) {
            claims = new JSONRequestGenerator()
                    .addAudience()
                    .addResponseType(responseType)
                    .addExpireDate(expiryDate.getEpochSecond().toLong())
                    .addClientID(clientId)
                    .addIssuer(clientId)
                    .addRedirectURI(redirect_uri)
                    .addScope(scopeString)
                    .addState(state)
                    .addNonce()
                    .addCustomValue("max_age", 86400)
                    .addCustomValue("nbf", notBefore.getEpochSecond().toLong())
                    .addCustomJson("claims", claimsString)
                    .addCustomValue("response_mode", responseMode)
                    .addCustomValue("code_challenge_method", codeChallengeMethod)
                    .addCustomValue("code_challenge", codeChallengeValue)
                    .getJsonObject().toString()
        } else {
            claims = new JSONRequestGenerator()
                    .addAudience()
                    .addResponseType(responseType)
                    .addExpireDate(expiryDate.getEpochSecond().toLong())
                    .addClientID(clientId)
                    .addIssuer(clientId)
                    .addRedirectURI(redirect_uri)
                    .addScope(scopeString)
                    .addNonce()
                    .addCustomValue("max_age", 86400)
                    .addCustomValue("nbf", notBefore.getEpochSecond().toLong())
                    .addCustomJson("claims", claimsString)
                    .addCustomValue("response_mode", responseMode)
                    .addCustomValue("code_challenge_method", codeChallengeMethod)
                    .addCustomValue("code_challenge", codeChallengeValue)
                    .getJsonObject().toString()
        }
        return claims
    }
}

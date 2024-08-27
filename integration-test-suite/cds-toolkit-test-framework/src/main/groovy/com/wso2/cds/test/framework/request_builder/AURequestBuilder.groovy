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

import com.nimbusds.oauth2.sdk.pkce.CodeVerifier
import com.wso2.cds.test.framework.constant.AUAccountScope
import com.wso2.cds.test.framework.constant.AUConstants
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.oauth2.sdk.AccessTokenResponse
import com.nimbusds.oauth2.sdk.AuthorizationCode
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant
import com.nimbusds.oauth2.sdk.AuthorizationGrant
import com.nimbusds.oauth2.sdk.RefreshTokenGrant
import com.nimbusds.oauth2.sdk.Scope
import com.nimbusds.oauth2.sdk.TokenErrorResponse
import com.nimbusds.oauth2.sdk.TokenRequest
import com.nimbusds.oauth2.sdk.TokenResponse
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication
import com.nimbusds.oauth2.sdk.auth.PrivateKeyJWT
import com.nimbusds.oauth2.sdk.http.HTTPRequest
import com.nimbusds.oauth2.sdk.http.HTTPResponse
import com.nimbusds.oauth2.sdk.id.ClientID
import com.nimbusds.oauth2.sdk.token.RefreshToken
import com.wso2.openbanking.test.framework.request_builder.SignedObject
import com.wso2.cds.test.framework.configuration.AUConfigurationService
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import com.wso2.cds.test.framework.utility.AURestAsRequestBuilder
import com.wso2.cds.test.framework.utility.AUTestUtil

import java.nio.charset.Charset

/**
 * Class for hold all requests in AU layer
 * except registration and authorization
 */
class AURequestBuilder {

    private static Logger log = LogManager.getLogger(AURequestBuilder.class.getName())

    private static AUConfigurationService auConfiguration = new AUConfigurationService()

    /**
     * Method for get application access token
     * @param scopes
     * @param clientId
     * @return
     */
    static String getApplicationAccessToken(List<String> scopes, String clientId) {
        AUJWTGenerator auJwtGenerator = new AUJWTGenerator()
        auJwtGenerator.setScopes(scopes)
        String jwt = auJwtGenerator.getAppAccessTokenJwt(clientId)

        RestAssured.baseURI = auConfiguration.getServerAuthorisationServerURL()
        Response response = AURestAsRequestBuilder.buildRequest().contentType(AUConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .body(jwt)
                .post(AUConstants.TOKEN_ENDPOINT)

        def accessToken = AUTestUtil.parseResponseBody(response, "access_token")
        log.info("Got access token $accessToken")

        return accessToken

    }

    /**
     * Method for get user access token
     * @param code
     * @param scopes
     */
    static void getUserAccessToken(String code, List<String> scopes) {
        AUJWTGenerator auJwtGenerator = new AUJWTGenerator()
        auJwtGenerator.setScopes(scopes)
        String jwt = auJwtGenerator.getUserAccessTokenJwt(code)

        RestAssured.baseURI = auConfiguration.getServerBaseURL()
        Response response = AURestAsRequestBuilder.buildRequest().contentType(AUConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .body(jwt)
                .post(AUConstants.TOKEN_ENDPOINT)
    }

    /**
     * Return Basic Request with given user token
     * @param userAccessToken
     * @param xv_header
     * @return
     */
    static RequestSpecification buildBasicRequest(String userAccessToken, int xv_header) {

        return AURestAsRequestBuilder.buildRequest()
                .header(AUConstants.X_V_HEADER, xv_header)
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, "${AUConstants.AUTHORIZATION_BEARER_TAG}${userAccessToken}")
    }

    static RequestSpecification buildBasicRequestWithoutAuthorisationHeader(int xv_header) {

        return AURestAsRequestBuilder.buildRequest()
                .header(AUConstants.X_V_HEADER, xv_header)
    }

    /**
     * Return Basic Request with given user token and custom headers.
     * @param userAccessToken
     * @param xv_header
     * @param clientHeader
     * @param authDate
     * @param customerIpAddress
     * @return RequestSpecification
     */
    static RequestSpecification buildBasicRequestWithCustomHeaders(String userAccessToken, def xv_header, clientHeader,
                                                                   String authDate = AUConstants.DATE) {

        return AURestAsRequestBuilder.buildRequest()
                .header(AUConstants.X_V_HEADER, xv_header)
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, "${AUConstants.AUTHORIZATION_BEARER_TAG}${userAccessToken}")
                .header(AUConstants.X_FAPI_AUTH_DATE, authDate)
                .header(AUConstants.X_CDS_CLIENT_HEADERS , clientHeader)
    }

    /**
     * Return Basic Request with given user token and custom headers.
     * @param userAccessToken
     * @param xv_header
     * @param clientHeader
     * @param authDate
     * @param customerIpAddress
     * @return RequestSpecification
     */
    static RequestSpecification buildBasicRequestWithOptionalHeaders(String userAccessToken, def xv_header, clientHeader,
                                                                     String authDate = AUConstants.DATE,
                                                                     String customerIpAddress = AUConstants.IP,
                                                                     String fapiInteractionId = UUID.randomUUID()) {

        return AURestAsRequestBuilder.buildRequest()
                .header(AUConstants.X_V_HEADER, xv_header)
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, "${AUConstants.AUTHORIZATION_BEARER_TAG}${userAccessToken}")
                .header(AUConstants.X_FAPI_AUTH_DATE, authDate)
                .header(AUConstants.X_FAPI_CUSTOMER_IP_ADDRESS , customerIpAddress)
                .header(AUConstants.X_CDS_CLIENT_HEADERS , clientHeader)
                .header(AUConstants.X_FAPI_INTERACTION_ID, fapiInteractionId)
    }

    /**
     * Get User Access Token From Authorization Code.
     * @param code authorisation code
     * @param codeVerifier code verifier
     * @param clientId client id
     * @param redirectUrl application redirect url
     * @return user access token
     */
    static AccessTokenResponse getUserToken(String code, CodeVerifier codeVerifier, String clientId = null,
                                            String redirectUrl = auConfiguration.getAppInfoRedirectURL()) {

        AuthorizationCode grant = new AuthorizationCode(code)
        URI callbackUri = new URI(redirectUrl)
        AuthorizationGrant codeGrant = new AuthorizationCodeGrant(grant, callbackUri, codeVerifier)

        String assertionString = new SignedObject().getJwt(clientId)

        ClientAuthentication clientAuth = new PrivateKeyJWT(SignedJWT.parse(assertionString))

        URI tokenEndpoint = new URI("${auConfiguration.getServerAuthorisationServerURL()}${AUConstants.TOKEN_ENDPOINT}")

        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, codeGrant)

        HTTPRequest httpRequest = request.toHTTPRequest()

        def response = AURestAsRequestBuilder.buildRequest()
                .contentType(AUConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .body(httpRequest.query)
                .post(tokenEndpoint)

        HTTPResponse httpResponse = new HTTPResponse(response.statusCode())
        httpResponse.setContentType(response.contentType())
        httpResponse.setContent(response.getBody().print())

        return TokenResponse.parse(httpResponse).toSuccessResponse()

    }


    /**
     * Get User Access Token From Authorization Code and optional scopes list.
     *
     * @param code authorisation code
     * @param scopesList requsted scope list
     * @return token response
     */
    static AccessTokenResponse getUserToken(String code, List<AUAccountScope> scopesList, CodeVerifier codeVerifier) {

        AuthorizationCode grant = new AuthorizationCode(code)
        URI callbackUri = new URI(auConfiguration.getAppInfoRedirectURL())
        AuthorizationGrant codeGrant = new AuthorizationCodeGrant(grant, callbackUri, codeVerifier)

        String scopeString = "openid ${String.join(" ", scopesList.collect({ it.scopeString }))}"

        Scope scope = new Scope(scopeString)

        String assertionString = new SignedObject().getJwt()

        ClientAuthentication clientAuth = new PrivateKeyJWT(SignedJWT.parse(assertionString))

        URI tokenEndpoint = new URI("${auConfiguration.getServerAuthorisationServerURL()}${AUConstants.TOKEN_ENDPOINT}")
        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, codeGrant, scope)

        HTTPRequest httpRequest = request.toHTTPRequest()

        def response = AURestAsRequestBuilder.buildRequest()
                .contentType(AUConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .body(httpRequest.query)
                .post(tokenEndpoint)

        HTTPResponse httpResponse = new HTTPResponse(response.statusCode())
        httpResponse.setContentType(response.contentType())
        httpResponse.setContent(response.getBody().print())

        return TokenResponse.parse(httpResponse).toSuccessResponse()
    }

    /**
     * Get User Access Token Error Response for error scenarios
     *
     * @param code authorisation code
     * @param redirectUrl redirect URL
     * @param clientAuthRequired indicates whether privateKeyJWT client auth is required
     * @param mtlsRequired indicates whether mlts is required
     * @param signingAlg client assertion signing algorithm
     * @return token error response
     */
    static TokenErrorResponse getUserTokenErrorResponse(String code,
                                                        String redirectUrl = auConfiguration.getAppInfoRedirectURL(),
                                                        String client_id = auConfiguration.getAppInfoClientID(),
                                                        Boolean clientAuthRequired = true,
                                                        Boolean mtlsRequired = true,
                                                        String signingAlg = auConfiguration.getCommonSigningAlgorithm()) {
        AuthorizationCode grant = new AuthorizationCode(code)
        URI callbackUri = new URI(redirectUrl)
        AuthorizationGrant codeGrant = new AuthorizationCodeGrant(grant, callbackUri)

        URI tokenEndpoint = new URI("${auConfiguration.getServerAuthorisationServerURL()}${AUConstants.TOKEN_ENDPOINT}")

        TokenRequest request
        if (!clientAuthRequired) {
            ClientID clientId = new ClientID(client_id)
            request = new TokenRequest(tokenEndpoint, clientId, codeGrant)
        } else {
            SignedObject accessTokenJWTDTO = new SignedObject()
            accessTokenJWTDTO.setSigningAlgorithm(signingAlg)
            String assertionString = accessTokenJWTDTO.getJwt(client_id)
            ClientAuthentication clientAuth = new PrivateKeyJWT(SignedJWT.parse(assertionString))
            request = new TokenRequest(tokenEndpoint, clientAuth, codeGrant)
        }

        HTTPRequest httpRequest = request.toHTTPRequest()
        def response

        if (mtlsRequired) {
            response = AURestAsRequestBuilder.buildRequest()
                    .contentType(AUConstants.ACCESS_TOKEN_CONTENT_TYPE)
                    .body(httpRequest.query)
                    .post(tokenEndpoint)

        } else {
            response = AURestAsRequestBuilder.buildBasicRequest()
                    .contentType(AUConstants.ACCESS_TOKEN_CONTENT_TYPE)
                    .body(httpRequest.query)
                    .post(tokenEndpoint)
        }

        HTTPResponse httpResponse = new HTTPResponse(response.statusCode())
        httpResponse.setContentType(response.contentType())
        httpResponse.setContent(response.getBody().print())

        return TokenResponse.parse(httpResponse).toErrorResponse()

    }

    /**
     * Get User Access Token From refresh token.
     *
     * @param @param refresh_token
     * @return token response
     */
    static AccessTokenResponse getUserTokenFromRefreshToken(RefreshToken refresh_token) {


        AuthorizationGrant refreshTokenGrant = new RefreshTokenGrant(refresh_token)

        String assertionString = new SignedObject().getJwt()

        ClientAuthentication clientAuth = new PrivateKeyJWT(SignedJWT.parse(assertionString))

        URI tokenEndpoint = new URI("${auConfiguration.getServerAuthorisationServerURL()}${AUConstants.TOKEN_ENDPOINT}")

        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, refreshTokenGrant)

        HTTPRequest httpRequest = request.toHTTPRequest()

        def response = AURestAsRequestBuilder.buildRequest()
                .contentType(AUConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .body(httpRequest.query)
                .post(tokenEndpoint)

        HTTPResponse httpResponse = new HTTPResponse(response.statusCode())
        httpResponse.setContentType(response.contentType())
        httpResponse.setContent(response.getBody().print())

        return TokenResponse.parse(httpResponse).toSuccessResponse()
    }


    /**
     * Get User Access Token Error Response From Inactive refresh token.
     *
     * @param @param refresh_token
     * @return token error response
     */
    static TokenErrorResponse getUserTokenFromRefreshTokenErrorResponse(RefreshToken refresh_token) {

        AuthorizationGrant refreshTokenGrant = new RefreshTokenGrant(refresh_token)

        String assertionString = new SignedObject().getJwt()

        ClientAuthentication clientAuth = new PrivateKeyJWT(SignedJWT.parse(assertionString))

        URI tokenEndpoint = new URI("${auConfiguration.getServerAuthorisationServerURL()}${AUConstants.TOKEN_ENDPOINT}")

        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, refreshTokenGrant)

        HTTPRequest httpRequest = request.toHTTPRequest()

        def response = AURestAsRequestBuilder.buildRequest()
                .contentType(AUConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .body(httpRequest.query)
                .post(tokenEndpoint)

        HTTPResponse httpResponse = new HTTPResponse(response.statusCode())
        httpResponse.setContentType(response.contentType())
        httpResponse.setContent(response.getBody().print())

        return TokenResponse.parse(httpResponse).toErrorResponse()
    }

    /**
     * Build Introspection Request
     *
     * @param token access token
     * @return Introspection Request Specification
     */
    static RequestSpecification buildIntrospectionRequest(String token, String clientId, Integer tpp = null) {

        String assertionString = new SignedObject().getJwt(clientId)

        def bodyContent = [(AUConstants.CLIENT_ID_KEY)            : (clientId),
                           (AUConstants.CLIENT_ASSERTION_TYPE_KEY): (AUConstants.CLIENT_ASSERTION_TYPE),
                           (AUConstants.CLIENT_ASSERTION_KEY)   : assertionString]


        return AURestAsRequestBuilder.buildRequest()
                .contentType(ContentType.URLENC)
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, "Basic ${AUTestUtil.getBasicAuthorizationHeader(tpp)}")
                .formParams(bodyContent)
                .formParams("token", token)
                .baseUri(auConfiguration.getServerAuthorisationServerURL())
    }

    /**
     * Build Introspection Request for Revoke Access Token
     *
     * @param token access token
     * @return Introspection Request Specification
     */
    static RequestSpecification buildRevokeIntrospectionRequest(String token, String clientId) {

        String assertionString = new SignedObject().getJwt(clientId)

        def bodyContent = [(AUConstants.CLIENT_ID_KEY)            : (clientId),
                           (AUConstants.CLIENT_ASSERTION_TYPE_KEY): (AUConstants.CLIENT_ASSERTION_TYPE),
                           (AUConstants.CLIENT_ASSERTION_KEY)   : assertionString]


        return AURestAsRequestBuilder.buildRequest()
                .contentType(ContentType.URLENC)
                .formParams(bodyContent)
                .formParams("token", token)
                .formParams("token_type_hint", "access_token")
                .baseUri(auConfiguration.getServerAuthorisationServerURL())
    }

    /**
     * Get token response.
     *
     * @param scopes : scopes for token
     * @return access token response
     */
    static Response getTokenResponse(List<String> scopes, String clientId) {
        def tokenDTO = new AUJWTGenerator()
        tokenDTO.setScopes(scopes)
        String jwt = tokenDTO.getAppAccessTokenJwt(clientId)
        RestAssured.baseURI = auConfiguration.getServerAuthorisationServerURL()
        Response response = AURestAsRequestBuilder.buildRequest().contentType(AUConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .body(jwt).post(AUConstants.TOKEN_ENDPOINT)
        return response
    }

    /**
     * Return response of Account data retrieval request
     * @param userAccessToken
     * @return
     */
    static Response getAccountRetrieval(String userAccessToken) {
        //Account Retrieval request
        Response response = AURestAsRequestBuilder.buildRequest()
                .header(AUConstants.X_V_HEADER, 1)
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, "${AUConstants.AUTHORIZATION_BEARER_TAG}" + userAccessToken)
                .baseUri(auConfiguration.getServerBaseURL())
                .get("${AUConstants.BULK_ACCOUNT_PATH}/")
        return response
    }

    /**
     * Return response of Consent status request
     * @param headerString
     * @param consentId
     * @return
     */
    static Response getConsentStatus(String headerString, String consentId) {
        Response response = AURestAsRequestBuilder.buildRequest()
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, "Basic " + Base64.encoder.encodeToString(
                        headerString.getBytes(Charset.forName("UTF-8"))))
                .baseUri(auConfiguration.getServerAuthorisationServerURL())
                .get("${AUConstants.CONSENT_STATUS_ENDPOINT}${AUConstants.STATUS_PATH}?${consentId}")
        return response
    }

    /**
     * Request for revoke consent
     * @param clientId
     * @param cdrArrangementId
     * @return
     */
    static Response doRevokeConsent(String clientId, String cdrArrangementId) {

        String assertionString = new SignedObject().getJwt(clientId)

        def bodyContent = [(AUConstants.CLIENT_ID_KEY)            : (clientId),
                           (AUConstants.CLIENT_ASSERTION_TYPE_KEY): (AUConstants.CLIENT_ASSERTION_TYPE),
                           (AUConstants.CLIENT_ASSERTION_KEY)     : assertionString,
                           (AUConstants.CDR_ARRANGEMENT_ID)     : cdrArrangementId]

        def response = AURestAsRequestBuilder.buildRequest()
                .contentType(AUConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .formParams(bodyContent)
                .baseUri(auConfiguration.getServerBaseURL())
                .post("${AUConstants.CDR_ARRANGEMENT_ENDPOINT}")
        return response
    }
}

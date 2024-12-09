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

package org.wso2.cds.integration.test.admin_api.authorisation

import com.nimbusds.oauth2.sdk.AccessTokenResponse
import com.nimbusds.oauth2.sdk.TokenErrorResponse
import org.wso2.cds.test.framework.AUTest
import org.wso2.cds.test.framework.constant.AUAccountProfile
import org.wso2.cds.test.framework.constant.AUAccountScope
import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.request_builder.AURequestBuilder
import org.wso2.cds.test.framework.utility.AUTestUtil
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

/**
 * Test Cases for Failed Token Exchange Metrics in Abandon Flow.
 */
class  AbandonFailedTokenExchangeMetrics extends AUTest {

    private AccessTokenResponse userAccessToken, secondUserAccessToken
    private String cdrArrangementId = ""
    private String requestUri
    private String secondAuthorisationCode = null

    @BeforeClass (alwaysRun = true)
    void "Initial Metrics Request"() {

        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Assign Metrics to Variables
        getInitialMetricsResponse(metricsResponse)
    }

    @Test (groups = "SmokeTest")
    void "Verify Metrics response after abandon the flow without generating token"() {

        //Consent Authorisation Flow
        def response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, cdrArrangementId)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_201)

        doConsentAuthorisationViaRequestUri(scopes, requestUri.toURI(), auConfiguration.getAppInfoClientID(),
                AUAccountProfile.INDIVIDUAL)
        Assert.assertNotNull(authorisationCode)

        //Wait until authorisation code expires
        sleep(AUConstants.AUTH_CODE_EXPIRATION_TIME)

        //Verify Metrics Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Active and New Authorisation count increased by 1
        activeAuthIndividual = activeAuthIndividual + 1
        newAuthCurrentDayOngoingIndividual = newAuthCurrentDayOngoingIndividual + 1

        //Abandon count increased by 1
        if (AUConstants.X_V_HEADER_METRICS == 4) {
            abandonedCurrentDay = (abandonedCurrentDay + 1)
        } else {
            abandonedCurrentDay = (abandonedCurrentDay + 1)
            abandonedFailedTokenExchangeCurrentDay = (abandonedFailedTokenExchangeCurrentDay + 1)
        }

        //Asserting the response
        assertMetricsAuthorisationResponse(metricsResponse)
    }

    @Test
    void "Verify Metrics response when Auth code expired before the Token Exchange"() {

        //Consent Authorisation Flow
        def response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes,
                AUConstants.DEFAULT_SHARING_DURATION, true, cdrArrangementId)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_201)

        doConsentAuthorisationViaRequestUri(scopes, requestUri.toURI(), auConfiguration.getAppInfoClientID(),
                AUAccountProfile.INDIVIDUAL)
        Assert.assertNotNull(authorisationCode)

        //Wait until authorisation code expires
        sleep(AUConstants.AUTH_CODE_EXPIRATION_TIME)

        //Send User Access Token Request
        TokenErrorResponse errorResponse = AURequestBuilder.getUserTokenErrorResponse(authorisationCode,
                auConfiguration.getAppInfoRedirectURL(), auConfiguration.getAppInfoClientID(), true, true,
                auConfiguration.getCommonSigningAlgorithm(), AUConstants.CODE_VERIFIER)

        Assert.assertEquals(errorResponse.toJSONObject().get(AUConstants.ERROR_DESCRIPTION),
                AUConstants.CODE_EXPIRE_ERROR_MSG)

        //Verify Metrics Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Active and New Authorisation count increased by 1
        activeAuthIndividual = activeAuthIndividual + 1
        newAuthCurrentDayOngoingIndividual = newAuthCurrentDayOngoingIndividual + 1

        //Abandon count increased by 1
        if (AUConstants.X_V_HEADER_METRICS == 4) {
            abandonedCurrentDay = abandonedCurrentDay + 1
        } else {
            abandonedCurrentDay = abandonedCurrentDay + 1
            abandonedFailedTokenExchangeCurrentDay = abandonedFailedTokenExchangeCurrentDay + 1
        }

        //Asserting the response
        assertMetricsAuthorisationResponse(metricsResponse)
    }

    @Test (groups = "SmokeTest")
    void "Verify Metrics response when token request failure due to an error"() {

        //Consent Authorisation Flow
        def response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, cdrArrangementId)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_201)
        doConsentAuthorisationViaRequestUri(scopes, requestUri.toURI(), auConfiguration.getAppInfoClientID(),
                AUAccountProfile.INDIVIDUAL)
        Assert.assertNotNull(authorisationCode)

        //Send User Access Token request with incorrect redirect url
        TokenErrorResponse errorResponse = AURequestBuilder.getUserTokenErrorResponse(authorisationCode,
                "https://abc.com", auConfiguration.getAppInfoClientID(), true, true,
                auConfiguration.getCommonSigningAlgorithm(), AUConstants.CODE_VERIFIER)

        //Wait till abandonment time
        sleep(AUConstants.ABANDON_WAIT_TIME)

        //Verify Metrics Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Authorisation Count should increase as it completed the authorisation flow
        activeAuthIndividual = activeAuthIndividual + 1
        newAuthCurrentDayOngoingIndividual = newAuthCurrentDayOngoingIndividual + 1

        //Abandon count increased by 1
        if (AUConstants.X_V_HEADER_METRICS == 4) {
            abandonedCurrentDay = abandonedCurrentDay + 1
        } else {
            abandonedCurrentDay = abandonedCurrentDay + 1
            abandonedFailedTokenExchangeCurrentDay = abandonedFailedTokenExchangeCurrentDay + 1
            highPriorityCurrentDay =  highPriorityCurrentDay + 2
            unattendedCurrentDay = unattendedCurrentDay + 1
        }

        //Asserting the response
        assertMetricsAuthorisationResponse(metricsResponse)
    }

    @Test
    void "Verify metrics response when there is an error in token call in consent amendment"() {

        def response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes,
                AUConstants.DEFAULT_SHARING_DURATION, true, cdrArrangementId)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_201)

        doConsentAuthorisationViaRequestUri(scopes, requestUri.toURI(), auConfiguration.getAppInfoClientID(),
                AUAccountProfile.INDIVIDUAL)
        Assert.assertNotNull(authorisationCode)

        //Generate User Access Token
        userAccessToken = AURequestBuilder.getUserToken(authorisationCode, AUConstants.CODE_VERIFIER,
                auConfiguration.getAppInfoClientID())
        Assert.assertNotNull(userAccessToken.tokens.refreshToken)
        Assert.assertNotNull(userAccessToken.tokens.accessToken)
        cdrArrangementId = userAccessToken.getCustomParameters().get("cdr_arrangement_id")
        Assert.assertNotNull(cdrArrangementId)

        //Consent Amendment
        //remove an existing scope and add a new scope to amend the consent
        scopes.remove(AUAccountScope.BANK_TRANSACTION_READ)
        scopes.add(AUAccountScope.BANK_PAYEES_READ)

        //Retrieve and assert the request URI from Push Authorization request
        requestUri = AUTestUtil.parseResponseBody(auAuthorisationBuilder.doPushAuthorisationRequest(scopes,
                AUConstants.DEFAULT_SHARING_DURATION, true, cdrArrangementId), "request_uri")
        Assert.assertNotNull(requestUri)

        //Retrieve the second authorization code
        secondAuthorisationCode = doConsentAmendmentAuthorisation(scopes, cdrArrangementId,
                AUConstants.DEFAULT_SHARING_DURATION)
        Assert.assertNotNull(secondAuthorisationCode)

        TokenErrorResponse errorResponse = AURequestBuilder.getUserTokenErrorResponse(secondAuthorisationCode,
                "https://abc.com", auConfiguration.getAppInfoClientID(), true, true,
                auConfiguration.getCommonSigningAlgorithm(), auAuthorisationBuilder.getCodeVerifier())
        Assert.assertEquals(errorResponse.toJSONObject().get(AUConstants.ERROR_DESCRIPTION),
                AUConstants.CALLBACK_MISMATCH)

        //Wait until authorisation code expires
        sleep(AUConstants.ABANDON_WAIT_TIME)

        //Verify Metrics Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Authorisation Count should increase as it completed the authorisation flow
        activeAuthIndividual = activeAuthIndividual + 1
        newAuthCurrentDayOngoingIndividual = newAuthCurrentDayOngoingIndividual + 1
        amendedCurrentDayIndividual = amendedCurrentDayIndividual + 1

        //Abandon count increased by 1
        if (AUConstants.X_V_HEADER_METRICS == 4) {
            abandonedCurrentDay = abandonedCurrentDay + 1
        } else {
            abandonedCurrentDay = abandonedCurrentDay + 1
            abandonedFailedTokenExchangeCurrentDay = abandonedFailedTokenExchangeCurrentDay + 1
        }

        //Asserting the response
        assertMetricsAuthorisationResponse(metricsResponse)
    }

    @Test
    void "Verify metrics response when abandon without generating token in amendment flow"() {

        def response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, cdrArrangementId)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_201)

        doConsentAuthorisationViaRequestUri(scopes, requestUri.toURI(), auConfiguration.getAppInfoClientID(),
                AUAccountProfile.INDIVIDUAL)
        Assert.assertNotNull(authorisationCode)

        //Generate User Access Token
        userAccessToken = AURequestBuilder.getUserToken(authorisationCode, AUConstants.CODE_VERIFIER,
                auConfiguration.getAppInfoClientID())
        Assert.assertNotNull(userAccessToken.tokens.refreshToken)
        Assert.assertNotNull(userAccessToken.tokens.accessToken)
        cdrArrangementId = userAccessToken.getCustomParameters().get("cdr_arrangement_id")
        Assert.assertNotNull(cdrArrangementId)

        //Consent Amendment
        //remove an existing scope and add a new scope to amend the consent
        scopes.remove(AUAccountScope.BANK_TRANSACTION_READ)
        scopes.add(AUAccountScope.BANK_PAYEES_READ)

        //Retrieve and assert the request URI from Push Authorization request
        requestUri = AUTestUtil.parseResponseBody(auAuthorisationBuilder.doPushAuthorisationRequest(scopes,
                AUConstants.DEFAULT_SHARING_DURATION, true, cdrArrangementId), "request_uri")
        Assert.assertNotNull(requestUri)

        //Retrieve the second authorization code
        secondAuthorisationCode = doConsentAmendmentAuthorisation(scopes, cdrArrangementId,
                AUConstants.DEFAULT_SHARING_DURATION)
        Assert.assertNotNull(secondAuthorisationCode)

        //Wait until abandon time
        sleep(AUConstants.AUTH_CODE_EXPIRATION_TIME)

        //Verify Metrics Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Authorisation Count, new Authorisation count and amendment count should increase as it completed the authorisation flow
        activeAuthIndividual = activeAuthIndividual + 1
        newAuthCurrentDayOngoingIndividual = newAuthCurrentDayOngoingIndividual + 1
        amendedCurrentDayIndividual = amendedCurrentDayIndividual + 1

        //Abandon count increased by 1
        if (AUConstants.X_V_HEADER_METRICS == 4) {
            abandonedCurrentDay = abandonedCurrentDay + 1
        } else {
            abandonedCurrentDay = abandonedCurrentDay + 1
            abandonedFailedTokenExchangeCurrentDay = abandonedFailedTokenExchangeCurrentDay + 1
        }

        //Asserting the response
        assertMetricsAuthorisationResponse(metricsResponse)
    }

    @Test
    void "Auth code expired before the Token Exchange in amendment flow"() {

        def response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, cdrArrangementId)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_201)

        doConsentAuthorisationViaRequestUri(scopes, requestUri.toURI(), auConfiguration.getAppInfoClientID(),
                AUAccountProfile.INDIVIDUAL)
        Assert.assertNotNull(authorisationCode)

        //Generate User Access Token
        userAccessToken = AURequestBuilder.getUserToken(authorisationCode, AUConstants.CODE_VERIFIER,
                auConfiguration.getAppInfoClientID())
        Assert.assertNotNull(userAccessToken.tokens.refreshToken)
        Assert.assertNotNull(userAccessToken.tokens.accessToken)
        cdrArrangementId = userAccessToken.getCustomParameters().get("cdr_arrangement_id")
        Assert.assertNotNull(cdrArrangementId)

        //Consent Amendment
        //remove an existing scope and add a new scope to amend the consent
        scopes.remove(AUAccountScope.BANK_TRANSACTION_READ)
        scopes.add(AUAccountScope.BANK_PAYEES_READ)

        //Retrieve and assert the request URI from Push Authorization request
        requestUri = AUTestUtil.parseResponseBody(auAuthorisationBuilder.doPushAuthorisationRequest(scopes,
                AUConstants.DEFAULT_SHARING_DURATION, true, cdrArrangementId), "request_uri")
        Assert.assertNotNull(requestUri)

        //Retrieve the second authorization code
        secondAuthorisationCode = doConsentAmendmentAuthorisation(scopes, cdrArrangementId,
                AUConstants.DEFAULT_SHARING_DURATION)
        Assert.assertNotNull(secondAuthorisationCode)

        //Wait until authorisation code expires
        sleep(AUConstants.AUTH_CODE_EXPIRATION_TIME)

        TokenErrorResponse errorResponse = AURequestBuilder.getUserTokenErrorResponse(secondAuthorisationCode,
                auConfiguration.getAppInfoRedirectURL(), auConfiguration.getAppInfoClientID(), true, true,
                auConfiguration.getCommonSigningAlgorithm(), auAuthorisationBuilder.getCodeVerifier())
        Assert.assertEquals(errorResponse.toJSONObject().get(AUConstants.ERROR_DESCRIPTION),
                AUConstants.CODE_EXPIRE_ERROR_MSG)

        //Verify Metrics Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Authorisation Count, new Authorisation count and amendment count should increase as it completed the authorisation flow
        activeAuthIndividual = activeAuthIndividual + 1
        newAuthCurrentDayOngoingIndividual = newAuthCurrentDayOngoingIndividual + 1
        amendedCurrentDayIndividual = amendedCurrentDayIndividual + 1

        //Abandon count increased by 1
        if (AUConstants.X_V_HEADER_METRICS == 4) {
            abandonedCurrentDay = abandonedCurrentDay + 1
        } else {
            abandonedCurrentDay = abandonedCurrentDay + 1
            abandonedFailedTokenExchangeCurrentDay = abandonedFailedTokenExchangeCurrentDay + 1
        }

        //Asserting the response
        assertMetricsAuthorisationResponse(metricsResponse)
    }
}

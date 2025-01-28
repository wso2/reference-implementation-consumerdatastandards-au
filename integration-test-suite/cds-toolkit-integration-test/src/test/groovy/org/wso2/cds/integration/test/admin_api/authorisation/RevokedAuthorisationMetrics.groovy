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
import org.wso2.cds.test.framework.AUTest
import org.wso2.cds.test.framework.constant.AUAccountProfile
import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.request_builder.AURequestBuilder
import org.wso2.cds.test.framework.utility.AUTestUtil
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

/**
 * Test cases to retrieve Authorisation Metrics for revokedAuthorization.
 */
class RevokedAuthorisationMetrics extends AUTest {

    private String cdrArrangementId = ""
    private AccessTokenResponse userAccessToken
    private String requestUri

    @BeforeClass (alwaysRun = true)
    void "Initial Metrics Request"() {

        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Assign Metrics to Variables
        getInitialMetricsResponse(metricsResponse)
    }

    @Test (groups = "SmokeTest")
    void "Consent revocation via CDR Arrangement Endpoint for Individual Consent"(){

        auConfiguration.setPsuNumber(0)
        //Send Authorisation Request for 1st time
        doConsentAuthorisation()

        //Generate User Access Token
        AccessTokenResponse userAccessTokenRes = getUserAccessTokenResponse()
        String cdrArrangementId = userAccessTokenRes.getCustomParameters().get("cdr_arrangement_id")
        accessToken = userAccessTokenRes.tokens.accessToken
        Assert.assertNotNull(cdrArrangementId)

        //retrieve consumer data successfully
        doAccountRetrieval(accessToken)
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //revoke sharing arrangement
        def revokeResponse = doRevokeCdrArrangement(auConfiguration.getAppInfoClientID(), cdrArrangementId)
        Assert.assertEquals(revokeResponse.statusCode(), AUConstants.STATUS_CODE_204)

        sleep(AUConstants.ABANDON_WAIT_TIME)

        //Verify Metrics Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //New Ongoing Individual and revoked count increased by 1. Active authorisations count should not increased.

        newAuthCurrentDayOngoingIndividual = newAuthCurrentDayOngoingIndividual + 1
        revokedCurrentDayIndividual = revokedCurrentDayIndividual + 1
        activeAuthIndividual = activeAuthIndividual +  1
        highPriorityCurrentDay = highPriorityCurrentDay + 3
        unattendedCurrentDay = unattendedCurrentDay + 2

        //Asserting the response
        assertMetricsAuthorisationResponse(metricsResponse)
    }

    @Test (groups = "SmokeTest")
    void "Consent revocation via CDR Arrangement Endpoint for Business Consent"(){

        auConfiguration.setPsuNumber(2)

        //Send Authorisation Request for 1st time
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        doConsentAuthorisationViaRequestUri(scopes, requestUri.toURI(), auConfiguration.getAppInfoClientID(),
                AUAccountProfile.ORGANIZATION_A)
        Assert.assertNotNull(authorisationCode)

        //Generate User Access Token
        AccessTokenResponse userAccessTokenRes = getUserAccessTokenResponse()
        String cdrArrangementId = userAccessTokenRes.getCustomParameters().get("cdr_arrangement_id")
        accessToken = userAccessTokenRes.tokens.accessToken
        Assert.assertNotNull(cdrArrangementId)

        //retrieve consumer data successfully
        doAccountRetrieval(accessToken)
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //revoke sharing arrangement
        def revokeResponse = doRevokeCdrArrangement(auConfiguration.getAppInfoClientID(), cdrArrangementId)
        Assert.assertEquals(revokeResponse.statusCode(), AUConstants.STATUS_CODE_204)

        sleep(AUConstants.ABANDON_WAIT_TIME)

        //Verify Metrics Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //New Ongoing NonIndividual and revoked count increased by 1. Active authorisations count should not increased.

        newAuthCurrentDayOngoingNonIndividual = newAuthCurrentDayOngoingNonIndividual + 1
        revokedCurrentDayNonIndividual = revokedCurrentDayNonIndividual + 1
        activeAuthNonIndividual = activeAuthNonIndividual + 1

        highPriorityCurrentDay = highPriorityCurrentDay + 3
        unattendedCurrentDay = unattendedCurrentDay + 2

        //Asserting the response
        assertMetricsAuthorisationResponse(metricsResponse)
    }

    @Test (groups = "SmokeTest")
    void "Consent revocation via CDR Arrangement Endpoint for Individual Once off Consent"(){

        auConfiguration.setPsuNumber(0)
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.SINGLE_ACCESS_CONSENT,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        doConsentAuthorisationViaRequestUri(scopes, requestUri.toURI(), auConfiguration.getAppInfoClientID(),
                AUAccountProfile.INDIVIDUAL)
        Assert.assertNotNull(authorisationCode)

        //Generate User Access Token
        AccessTokenResponse userAccessTokenRes = getUserAccessTokenResponse()
        String cdrArrangementId = userAccessTokenRes.getCustomParameters().get("cdr_arrangement_id")
        accessToken = userAccessTokenRes.tokens.accessToken
        Assert.assertNotNull(cdrArrangementId)

        //retrieve consumer data successfully
        doAccountRetrieval(accessToken)
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //revoke sharing arrangement
        def revokeResponse = doRevokeCdrArrangement(auConfiguration.getAppInfoClientID(), cdrArrangementId)
        Assert.assertEquals(revokeResponse.statusCode(), AUConstants.STATUS_CODE_204)

        sleep(AUConstants.ABANDON_WAIT_TIME)

        //Verify Metrics Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //New Onceoff Individual count increased by 1. Active authorisations count and the revoked count should not
        // increased.
        newAuthCurrentDayOnceOffIndividual = newAuthCurrentDayOnceOffIndividual + 1

        highPriorityCurrentDay = highPriorityCurrentDay + 3
        unattendedCurrentDay = unattendedCurrentDay + 2

        //Asserting the response
        assertMetricsAuthorisationResponse(metricsResponse)
    }

    @Test (groups = "SmokeTest")
    void "Consent revocation via CDR Arrangement Endpoint for Business Once off Consent"(){

        auConfiguration.setPsuNumber(2)
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.SINGLE_ACCESS_CONSENT,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        doConsentAuthorisationViaRequestUri(scopes, requestUri.toURI(), auConfiguration.getAppInfoClientID(),
                AUAccountProfile.ORGANIZATION_A)
        Assert.assertNotNull(authorisationCode)

        //Generate User Access Token
        AccessTokenResponse userAccessTokenRes = getUserAccessTokenResponse()
        String cdrArrangementId = userAccessTokenRes.getCustomParameters().get("cdr_arrangement_id")
        accessToken = userAccessTokenRes.tokens.accessToken
        Assert.assertNotNull(cdrArrangementId)

        //retrieve consumer data successfully
        doAccountRetrieval(accessToken)
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //revoke sharing arrangement
        def revokeResponse = doRevokeCdrArrangement(auConfiguration.getAppInfoClientID(), cdrArrangementId)
        Assert.assertEquals(revokeResponse.statusCode(), AUConstants.STATUS_CODE_204)

        sleep(AUConstants.ABANDON_WAIT_TIME)

        //Verify Metrics Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //New Once off Individual count increased by 1. Active authorisations count and the revoked count should not
        // increased.

        newAuthCurrentDayOnceOffNonIndividual = newAuthCurrentDayOnceOffNonIndividual + 1

        highPriorityCurrentDay = highPriorityCurrentDay + 3
        unattendedCurrentDay = unattendedCurrentDay + 2

        //Asserting the response
        assertMetricsAuthorisationResponse(metricsResponse)
    }

    @Test (priority = 1, enabled = false)
    void "Consent revocation after deleting the TPP"(){

        auConfiguration.setTppNumber(1)

        //Register Application
        def registrationResponse = tppRegistration()
        clientId = AUTestUtil.parseResponseBody(registrationResponse, "client_id")
        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.CREATED)

        //Write Client Id to config file.
        AUTestUtil.writeToConfigFile(clientId)

        //Authorise consent
        doConsentAuthorisation(clientId)
        Assert.assertNotNull(authorisationCode)

        //Generate User Access Token
        userAccessToken = AURequestBuilder.getUserToken(authorisationCode, AUConstants.CODE_VERIFIER,
                clientId)
        String userToken = userAccessToken.tokens.accessToken
        cdrArrangementId = userAccessToken.getCustomParameters().get("cdr_arrangement_id")
        Assert.assertNotNull(cdrArrangementId)
        Assert.assertNotNull(userToken)

        //retrieve consumer data successfully
        doAccountRetrieval(userToken)
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //Delete Application
        deleteApplicationIfExists(clientId)
        Assert.assertEquals(deletionResponse.statusCode(), AUConstants.STATUS_CODE_204)

        sleep(AUConstants.ABANDON_WAIT_TIME)

        //Verify Metrics Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //New Ongoing Individual count and the revoked count increased by 1.

        revokedCurrentDayIndividual = revokedCurrentDayIndividual + 1
        activeAuthIndividual = activeAuthIndividual + 1
        newAuthCurrentDayOngoingIndividual = newAuthCurrentDayOngoingIndividual + 1

        highPriorityCurrentDay = highPriorityCurrentDay + 3
        unattendedCurrentDay = unattendedCurrentDay + 2

        //Asserting the response
        assertMetricsAuthorisationResponse(metricsResponse)
    }
}

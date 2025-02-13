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


import org.wso2.cds.test.framework.AUTest
import org.wso2.cds.test.framework.constant.AUAccountProfile
import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.utility.AUTestUtil
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

/**
 * Test cases to retrieve Authorisation Metrics for New and Active Authorizations.
 */
class ActiveAndNewAuthorisationMetrics extends AUTest {

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
    void "Authorise New ongoing consent for Individual Profile"(){

        auConfiguration.setPsuNumber(0)
        doConsentAuthorisation(auConfiguration.getAppInfoClientID(), AUAccountProfile.INDIVIDUAL)
        generateUserAccessToken()

        sleep(AUConstants.ABANDON_WAIT_TIME)

        //Verify Metrics Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Active Authorisation and New Ongoing Individual count increased by 1
        activeAuthIndividual = activeAuthIndividual + 1
        newAuthCurrentDayOngoingIndividual = newAuthCurrentDayOngoingIndividual + 1

        //Asserting the response
        assertMetricsAuthorisationResponse(metricsResponse)
    }

    @Test (groups = "SmokeTest")
    void "Authorise New ongoing consent for Business Profile"(){

        auConfiguration.setPsuNumber(2)
        doConsentAuthorisation(auConfiguration.getAppInfoClientID(), AUAccountProfile.ORGANIZATION_A)
        generateUserAccessToken()

        sleep(AUConstants.ABANDON_WAIT_TIME)

        //Verify Metrics Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Active NonIndividual Authorisation and New Ongoing NonIndividual count increased by 1
        activeAuthNonIndividual = activeAuthNonIndividual + 1
        newAuthCurrentDayOngoingNonIndividual = newAuthCurrentDayOngoingNonIndividual + 1

        //Asserting the response
        assertMetricsAuthorisationResponse(metricsResponse)
    }

    @Test (groups = "SmokeTest")
    void  "Authorise New once off consent for Individual Profile"(){

        auConfiguration.setPsuNumber(0)
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.SHORT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        doConsentAuthorisationViaRequestUri(scopes, requestUri.toURI(), auConfiguration.getAppInfoClientID(),
                AUAccountProfile.INDIVIDUAL)
        Assert.assertNotNull(authorisationCode)

        //Generate User Access Token
        generateUserAccessToken(auConfiguration.getAppInfoClientID())

        sleep(AUConstants.ABANDON_WAIT_TIME)

        //Verify Metrics Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //New OnceOff Individual count increased by 1
        newAuthCurrentDayOnceOffIndividual = newAuthCurrentDayOnceOffIndividual + 1

        //Asserting the response
        assertMetricsAuthorisationResponse(metricsResponse)
    }

    @Test (groups = "SmokeTest")
    void "Authorise New once off consent for Business Profile"(){

        auConfiguration.setPsuNumber(2)
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.SHORT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        doConsentAuthorisationViaRequestUri(scopes, requestUri.toURI(), auConfiguration.getAppInfoClientID(),
                AUAccountProfile.ORGANIZATION_A)
        Assert.assertNotNull(authorisationCode)

        //Generate User Access Token
        generateUserAccessToken(auConfiguration.getAppInfoClientID())

        sleep(AUConstants.ABANDON_WAIT_TIME)

        //Verify Metrics Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //New OnceOff NonIndividual count increased by 1
        newAuthCurrentDayOnceOffNonIndividual = newAuthCurrentDayOnceOffNonIndividual + 1

        //Asserting the response
        assertMetricsAuthorisationResponse(metricsResponse)
    }

    @Test
    void "Authorise New once off consent for Individual Profile with 0 sharing duration"(){

        auConfiguration.setPsuNumber(0)
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.SINGLE_ACCESS_CONSENT,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        doConsentAuthorisationViaRequestUri(scopes, requestUri.toURI(), auConfiguration.getAppInfoClientID(),
                AUAccountProfile.INDIVIDUAL)
        Assert.assertNotNull(authorisationCode)

        //Generate User Access Token
        generateUserAccessToken(auConfiguration.getAppInfoClientID())

        sleep(AUConstants.ABANDON_WAIT_TIME)

        //Verify Metrics Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //New OnceOff Individual count increased by 1
        newAuthCurrentDayOnceOffIndividual = newAuthCurrentDayOnceOffIndividual + 1

        //Asserting the response
        assertMetricsAuthorisationResponse(metricsResponse)
    }

    @Test
    void "Authorise New once off consent for Individual Profile with 24hr sharing duration"(){

        auConfiguration.setPsuNumber(0)
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.SHARING_DURATION_24H,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        doConsentAuthorisationViaRequestUri(scopes, requestUri.toURI(), auConfiguration.getAppInfoClientID(),
                AUAccountProfile.INDIVIDUAL)
        Assert.assertNotNull(authorisationCode)

        //Generate User Access Token
        generateUserAccessToken(auConfiguration.getAppInfoClientID())

        sleep(AUConstants.ABANDON_WAIT_TIME)

        //Verify Metrics Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //New OnceOff Individual count increased by 1
        newAuthCurrentDayOnceOffIndividual = newAuthCurrentDayOnceOffIndividual + 1

        //Asserting the response
        assertMetricsAuthorisationResponse(metricsResponse)
    }

    @Test
    void "Authorise New ongoing consent for Individual Profile - joint account"(){

        auConfiguration.setPsuNumber(0)
        automationResponse = doJointAccountConsentAuthorisation(auConfiguration.getAppInfoClientID(), true)
        authorisationCode = AUTestUtil.getCodeFromJwtResponse(automationResponse.currentUrl.get())

        //Generate User Access Token
        generateUserAccessToken(auConfiguration.getAppInfoClientID())

        sleep(AUConstants.ABANDON_WAIT_TIME)

        //Verify Metrics Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //New Ongoing Individual count increased by 1
        activeAuthIndividual = activeAuthIndividual + 1
        newAuthCurrentDayOngoingIndividual = newAuthCurrentDayOngoingIndividual + 1

        //Asserting the response
        assertMetricsAuthorisationResponse(metricsResponse)
    }

    @Test
    void "Authorise New onceoff consent for Individual Profile - joint account"(){

        auConfiguration.setPsuNumber(0)
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.SHORT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        automationResponse = doAuthorisationFlowForJointAccounts(scopes, requestUri.toURI(),null)
        authorisationCode = AUTestUtil.getCodeFromJwtResponse(automationResponse.currentUrl.get())
        Assert.assertNotNull(authorisationCode)

        //Generate User Access Token
        generateUserAccessToken(auConfiguration.getAppInfoClientID())

        sleep(AUConstants.ABANDON_WAIT_TIME)

        //Verify Metrics Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //New OnceOff Individual count increased by 1
        newAuthCurrentDayOnceOffIndividual = newAuthCurrentDayOnceOffIndividual + 1

        //Asserting the response
        assertMetricsAuthorisationResponse(metricsResponse)
    }

    @Test
    void "Authorise New ongoing consent for Individual Profile - secondary account"(){

        auConfiguration.setPsuNumber(1)

        //Send Push Authorisation Request
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        //Select Secondary Account during authorisation
        doSecondaryAccountSelection(scopes, requestUri.toURI())
        Assert.assertNotNull(authorisationCode)

        //Get User Access Token
        generateUserAccessToken()

        sleep(AUConstants.ABANDON_WAIT_TIME)

        //Verify Metrics Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Active Authorisation and New Ongoing Individual count increased by 1
        activeAuthIndividual = activeAuthIndividual + 1
        newAuthCurrentDayOngoingIndividual = newAuthCurrentDayOngoingIndividual + 1

        //Asserting the response
        assertMetricsAuthorisationResponse(metricsResponse)
    }

    @Test
    void "Authorise New once off consent for Individual Profile - secondary account"(){

        auConfiguration.setPsuNumber(1)

        //Send Push Authorisation Request
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.SHORT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        //Select Secondary Account during authorisation
        doSecondaryAccountSelection(scopes, requestUri.toURI())
        Assert.assertNotNull(authorisationCode)

        //Get User Access Token
        generateUserAccessToken()

        sleep(AUConstants.ABANDON_WAIT_TIME)

        //Verify Metrics Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //New OnceOff Individual count increased by 1
        newAuthCurrentDayOnceOffIndividual = newAuthCurrentDayOnceOffIndividual + 1

        //Asserting the response
        assertMetricsAuthorisationResponse(metricsResponse)
    }
}

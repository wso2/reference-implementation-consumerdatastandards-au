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
import org.wso2.cds.test.framework.automation.consent.AUBasicAuthAutomationStep
import org.wso2.cds.test.framework.constant.AUAccountProfile
import org.wso2.cds.test.framework.constant.AUAccountScope
import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.constant.AUPageObjects
import org.wso2.cds.test.framework.request_builder.AURequestBuilder
import org.wso2.cds.test.framework.utility.AUTestUtil
import org.wso2.openbanking.test.framework.automation.AutomationMethod
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

/**
 * Test cases to retrieve Authorisation Metrics for Abandon PreAuthorisation Metrics.
 */
class AbandonPreAuthorisationMetrics extends AUTest {

    private AccessTokenResponse userAccessToken
    private String cdrArrangementId = ""
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
    void "Close Browser Session in Consent Detail Page"() {

        auConfiguration.setPsuNumber(0)

        //Consent Authorisation Flow
        def response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_201)

        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), auConfiguration.getAppInfoClientID())
                .toURI().toString()

        //Close Browser Session in Profile selection page
        automationResponse = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Select Profile and Accounts
                    selectProfileAndAccount(authWebDriver, AUAccountProfile.INDIVIDUAL)

                    //Click Confirm Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)
                }
                .execute()

        sleep(AUConstants.ABANDON_WAIT_TIME)

        //Verify Metrics Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Abandon count increased by 1
        if (AUConstants.X_V_HEADER_METRICS == 4) {
            abandonedCurrentDay = abandonedCurrentDay + 1
        } else {
            abandonedCurrentDay = abandonedCurrentDay + 1
            abandonedPreAuthorisationCurrentDay = abandonedPreAuthorisationCurrentDay + 1
            highPriorityCurrentDay = highPriorityCurrentDay + 1
            unattendedCurrentDay = unattendedCurrentDay + 1
        }

        //Asserting the response
        assertMetricsAuthorisationResponse(metricsResponse)
    }

    @Test
    void "Verify consent abandon by going back from consent details page"() {

        auConfiguration.setPsuNumber(0)

        //Consent Authorisation Flow
        def response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_201)

        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), auConfiguration.getAppInfoClientID())
                .toURI().toString()

        //Close Browser Session in Profile selection page
        automationResponse = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Select Profile and Accounts
                    selectProfileAndAccount(authWebDriver, AUAccountProfile.INDIVIDUAL)

                    //Click Confirm Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)

                    //Navigate Back
                    driver.navigate().back()
                }
                .execute()

        sleep(AUConstants.ABANDON_WAIT_TIME)

        //Verify Metrics Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Abandon count increased by 1
        if (AUConstants.X_V_HEADER_METRICS == 4) {
            abandonedCurrentDay = abandonedCurrentDay + 1
        } else {
            abandonedCurrentDay = abandonedCurrentDay + 1
            abandonedPreAuthorisationCurrentDay = abandonedPreAuthorisationCurrentDay + 1
        }

        //Asserting the response
        assertMetricsAuthorisationResponse(metricsResponse)
    }

    @Test
    void "Close Browser Session in Consent Detail Page in amendment flow"() {

        auConfiguration.setPsuNumber(0)

        //Send Authorisation Request for 1st time
        doConsentAuthorisation()

        //Generate User Access Token
        userAccessToken = AURequestBuilder.getUserToken(authorisationCode, AUConstants.CODE_VERIFIER,
                auConfiguration.getAppInfoClientID())
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
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), auConfiguration.getAppInfoClientID())
                .toURI().toString()

        //Close Browser Session in Profile selection page
        automationResponse = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Click Confirm Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)
                }
                .execute()

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
            abandonedPreAuthorisationCurrentDay = abandonedPreAuthorisationCurrentDay + 1
            highPriorityCurrentDay = highPriorityCurrentDay + 1
            unattendedCurrentDay = unattendedCurrentDay + 1
        }

        //Asserting the response
        assertMetricsAuthorisationResponse(metricsResponse)
    }

    @Test
    void "Verify consent abandon by going back from consent details page in amendment flow"() {

        auConfiguration.setPsuNumber(0)

        //Send Authorisation Request for 1st time
        doConsentAuthorisation()

        //Generate User Access Token
        userAccessToken = AURequestBuilder.getUserToken(authorisationCode, AUConstants.CODE_VERIFIER,
                auConfiguration.getAppInfoClientID())
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
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), auConfiguration.getAppInfoClientID())
                .toURI().toString()

        //Close Browser Session in Profile selection page
        automationResponse = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Click Confirm Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)

                    //Navigate Back
                    driver.navigate().back()
                }
                .execute()

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
            abandonedPreAuthorisationCurrentDay = abandonedPreAuthorisationCurrentDay + 1
        }

        //Asserting the response
        assertMetricsAuthorisationResponse(metricsResponse)
    }
}

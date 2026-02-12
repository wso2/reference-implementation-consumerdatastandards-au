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
import org.wso2.bfsi.test.framework.automation.AutomationMethod
import org.wso2.bfsi.test.framework.automation.NavigationAutomationStep
import org.wso2.cds.test.framework.AUTest
import org.wso2.cds.test.framework.automation.consent.AUBasicAuthAutomationStep
import org.wso2.cds.test.framework.constant.AUAccountProfile
import org.wso2.cds.test.framework.constant.AUAccountScope
import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.constant.AUPageObjects
import org.wso2.cds.test.framework.request_builder.AURequestBuilder
import org.wso2.cds.test.framework.utility.AUTestUtil
import org.openqa.selenium.By
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

import java.util.concurrent.TimeUnit

/**
 * Test cases to retrieve Authorisation Metrics for Abandon PreIdentification Metrics.
 */
class AbandonPreIdentificationMetrics extends AUTest {

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

        auConfiguration.setPsuNumber(0)
    }

    @Test (groups = "SmokeTest")
    void "Cancel consent flow from Login Page"() {

        //Consent Authorisation Flow
        def response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_201)

        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), auConfiguration.getAppInfoClientID())
                .toURI().toString()

        //Close Browser Session in Profile selection page
        automationResponse = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new NavigationAutomationStep(authoriseUrl, 10))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //User Login
                    authWebDriver.executeTextField(AUPageObjects.AU_USERNAME_FIELD_ID, auConfiguration.getUserPSUName())
                    driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS)

                    //Click on Cancel Button
                    authWebDriver.clickButtonXpath(AUPageObjects.BTN_CANCEL)

                    //Click on Cancel Confirmation Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONFIRM_CONSENT_DENY_XPATH)
                }
                .execute()

        def authUrl = automationResponse.currentUrl.get()
        Assert.assertTrue(AUTestUtil.getDecodedUrl(authUrl).contains(AUConstants.USER_SKIP_THE_CONSENT_FLOW))
        String stateParam = authUrl.split("state=")[1]
        Assert.assertEquals(auAuthorisationBuilder.state.toString(), stateParam)

        sleep(AUConstants.ABANDON_WAIT_TIME)

        //Verify Metrics Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        if (AUConstants.X_V_HEADER_METRICS == 4) {
            abandonedCurrentDay = abandonedCurrentDay + 1
        } else {
            abandonedCurrentDay = abandonedCurrentDay + 1
            abandonedPreIdentificationCurrentDay = abandonedPreIdentificationCurrentDay + 1
            highPriorityCurrentDay = highPriorityCurrentDay + 1
            unattendedCurrentDay = unattendedCurrentDay + 1
        }

        //Asserting the response
        assertMetricsAuthorisationResponse(metricsResponse)
    }

    @Test
    void "Close browser in login page"() {

        //Consent Authorisation Flow
        def response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_201)

        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), auConfiguration.getAppInfoClientID())
                .toURI().toString()

        //Close Browser Session in Profile selection page
        automationResponse = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new NavigationAutomationStep(authoriseUrl, 10))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //User Login
                    authWebDriver.executeTextField(AUPageObjects.AU_USERNAME_FIELD_ID, auConfiguration.getUserPSUName())
                    driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS)
                }
                .execute()

        sleep(AUConstants.ABANDON_WAIT_TIME)

        //Verify Metrics Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        if (AUConstants.X_V_HEADER_METRICS == 4) {
            abandonedCurrentDay = abandonedCurrentDay + 1
        } else {
            abandonedCurrentDay = abandonedCurrentDay + 1
            abandonedPreIdentificationCurrentDay = abandonedPreIdentificationCurrentDay + 1
            highPriorityCurrentDay = highPriorityCurrentDay + 1
            unattendedCurrentDay = unattendedCurrentDay + 1
        }

        //Asserting the response
        assertMetricsAuthorisationResponse(metricsResponse)
    }

    @Test
    void "Authenticate consent flow with incorrect username but proceed again with correct username"() {

        //Consent Authorisation Flow
        def response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_201)

        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), auConfiguration.getAppInfoClientID())
                .toURI().toString()

        //Close Browser Session in Profile selection page
        automationResponse = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new NavigationAutomationStep(authoriseUrl, 10))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Enter incorrect user name
                    authWebDriver.executeTextField(AUPageObjects.AU_USERNAME_FIELD_ID, "am@wso2.com")
                    driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS)

                    //Click on SignIn Button
                    authWebDriver.clickButtonXpath(AUPageObjects.AU_AUTH_SIGNIN_XPATH)

                    //Enter Correct User Name
                    driver.findElement(By.id(AUPageObjects.AU_USERNAME_FIELD_ID)).click()
                    authWebDriver.executeTextField(AUPageObjects.AU_USERNAME_FIELD_ID, auConfiguration.getUserPSUName())
                    driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS)

                    //Click on SignIn Button
                    authWebDriver.clickButtonXpath(AUPageObjects.AU_AUTH_SIGNIN_XPATH)

                    //Identifier First Authentication
                    authWebDriver.executeSMSOTP(AUPageObjects.AU_LBL_SMSOTP_AUTHENTICATOR, AUPageObjects.AU_TXT_OTP_CODE_ID,
                            AUConstants.OTP_CODE)
                    authWebDriver.clickButtonXpath(AUPageObjects.AU_BTN_AUTHENTICATE)

                    driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS)

                    //Select Profile and Accounts
                    selectProfileAndAccount(authWebDriver, AUAccountProfile.INDIVIDUAL)

                    //Click Confirm Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)

                    //Click Authorise Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)
                }
                .execute()

        authorisationCode = AUTestUtil.getCodeFromJwtResponse(automationResponse.currentUrl.get())

        //Generate User Access Token
        userAccessToken = AURequestBuilder.getUserToken(authorisationCode, AUConstants.CODE_VERIFIER,
                auConfiguration.getAppInfoClientID())
        Assert.assertNotNull(userAccessToken.tokens.accessToken)

        sleep(AUConstants.ABANDON_WAIT_TIME)

        //Verify Metrics Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        activeAuthIndividual = activeAuthIndividual + 1
        newAuthCurrentDayOngoingIndividual = newAuthCurrentDayOngoingIndividual + 1
        highPriorityCurrentDay = highPriorityCurrentDay + 1

        //Asserting the response
        assertMetricsAuthorisationResponse(metricsResponse)
    }

    @Test
    void "Verify count not increased after abandoning the flow with incorrect OTP"() {

        //Consent Authorisation Flow
        def response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_201)

        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), auConfiguration.getAppInfoClientID())
                .toURI().toString()

        //Close Browser Session in Profile selection page
        automationResponse = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new NavigationAutomationStep(authoriseUrl, 10))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //User Login
                    authWebDriver.executeTextField(AUPageObjects.AU_USERNAME_FIELD_ID, auConfiguration.getUserPSUName())
                    driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS)

                    //Click on SignIn Button
                    authWebDriver.clickButtonXpath(AUPageObjects.AU_AUTH_SIGNIN_XPATH)

                    //Identifier First Authentication
                    authWebDriver.executeSMSOTP(AUPageObjects.AU_LBL_SMSOTP_AUTHENTICATOR, AUPageObjects.AU_TXT_OTP_CODE_ID,
                            "123")
                    authWebDriver.clickButtonXpath(AUPageObjects.AU_BTN_AUTHENTICATE)

                }
                .execute()

        sleep(AUConstants.ABANDON_WAIT_TIME)

        //Verify Metrics Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        if (AUConstants.X_V_HEADER_METRICS == 4) {
            abandonedCurrentDay = abandonedCurrentDay + 1
        } else {
            abandonedCurrentDay = abandonedCurrentDay + 1
            abandonedPreAuthenticationCurrentDay = abandonedPreAuthenticationCurrentDay + 1
            highPriorityCurrentDay = highPriorityCurrentDay + 1
        }

        //Asserting the response
        assertMetricsAuthorisationResponse(metricsResponse)
    }

    @Test
    void "Cancelling consent authorization from Login page during amendment"() {

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
                .addStep(new NavigationAutomationStep(authoriseUrl, 10))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //User Login
                    authWebDriver.executeTextField(AUPageObjects.AU_USERNAME_FIELD_ID, auConfiguration.getUserPSUName())
                    driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS)

                    //Click on Cancel
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CANCEL_XPATH)
                    driver.findElement(By.xpath(AUPageObjects.CONFIRM_CONSENT_DENY_XPATH)).click()
                }
                .execute()

        def authUrl = automationResponse.currentUrl.get()
        Assert.assertTrue(AUTestUtil.getDecodedUrl(authUrl).contains(AUConstants.USER_SKIP_THE_CONSENT_FLOW))
        String stateParam = authUrl.split("state=")[1]
        Assert.assertEquals(auAuthorisationBuilder.state.toString(), stateParam)

        sleep(AUConstants.ABANDON_WAIT_TIME)

        //Verify Metrics Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Authorisation Count should increase as it completed the authorisation flow
        activeAuthIndividual = activeAuthIndividual + 1
        newAuthCurrentDayOngoingIndividual = newAuthCurrentDayOngoingIndividual + 1

        if (AUConstants.X_V_HEADER_METRICS == 4) {
            abandonedCurrentDay = abandonedCurrentDay + 1
        } else {
            abandonedCurrentDay = abandonedCurrentDay + 1
            abandonedPreIdentificationCurrentDay = abandonedPreIdentificationCurrentDay + 1
            highPriorityCurrentDay = highPriorityCurrentDay + 3
            unattendedCurrentDay = unattendedCurrentDay + 1
        }

        //Asserting the response
        assertMetricsAuthorisationResponse(metricsResponse)
    }

    @Test
    void "Closing the browser in Login page during amendment"() {

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
                .addStep(new NavigationAutomationStep(authoriseUrl, 10))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //User Login
                    authWebDriver.executeTextField(AUPageObjects.AU_USERNAME_FIELD_ID, auConfiguration.getUserPSUName())
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

        if (AUConstants.X_V_HEADER_METRICS == 4) {
            abandonedCurrentDay = abandonedCurrentDay + 1
        } else {
            abandonedCurrentDay = abandonedCurrentDay + 1
            abandonedPreIdentificationCurrentDay = abandonedPreIdentificationCurrentDay + 1
            highPriorityCurrentDay = highPriorityCurrentDay + 3
        }

        //Asserting the response
        assertMetricsAuthorisationResponse(metricsResponse)
    }

    @Test
    void "Verify abandonment of consent flow after going back by clicking on browser back button"() {

        //Consent Authorisation Flow
        def response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_201)

        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), auConfiguration.getAppInfoClientID())
                .toURI().toString()

        //Close Browser Session in Profile selection page
        automationResponse = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new NavigationAutomationStep(authoriseUrl, 10))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //User Login
                    authWebDriver.executeTextField(AUPageObjects.AU_USERNAME_FIELD_ID, auConfiguration.getUserPSUName())
                    driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS)

                    //Navigate Back
                    driver.navigate().back()

                }
                .execute()

        sleep(AUConstants.ABANDON_WAIT_TIME)

        //Verify Metrics Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        if (AUConstants.X_V_HEADER_METRICS == 4) {
            abandonedCurrentDay = abandonedCurrentDay + 1
        } else {
            abandonedCurrentDay = abandonedCurrentDay + 1
            abandonedPreIdentificationCurrentDay = abandonedPreIdentificationCurrentDay + 1
            highPriorityCurrentDay = highPriorityCurrentDay + 1
            unattendedCurrentDay = unattendedCurrentDay + 1
        }

        //Asserting the response
        assertMetricsAuthorisationResponse(metricsResponse)
    }

    @Test
    void "Verify the currentDay count when authorising multiple consents on same browser session"() {

        //Consent Authorisation Flow
        def response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_201)

        //Navigate through Authorise Flow
        authorisationCode = authoriseConsentWithoutClosingBrowser(scopes, requestUri.toURI())
        Assert.assertNotNull(authorisationCode)

        //Generate User Access Token
        userAccessToken = AURequestBuilder.getUserToken(authorisationCode, AUConstants.CODE_VERIFIER,
                auConfiguration.getAppInfoClientID())
        cdrArrangementId = userAccessToken.getCustomParameters().get("cdr_arrangement_id")
        Assert.assertNotNull(cdrArrangementId)

        //Consent Authorisation - Second Consent
        def requestUri2 = AUTestUtil.parseResponseBody(auAuthorisationBuilder.doPushAuthorisationRequest(scopes,
                AUConstants.DEFAULT_SHARING_DURATION, true, ""), "request_uri")
        Assert.assertNotNull(requestUri2)

        //Navigate through Authorise Flow
        //Retrieve the second authorization code
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri2.toURI(), auConfiguration.getAppInfoClientID())
                .toURI().toString()

        //Close the browser session on Accounts selection page.
        automationResponse = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Select Profile and Accounts
                    selectProfileAndAccount(authWebDriver, AUAccountProfile.INDIVIDUAL, false)

                    //Click Confirm Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)

                    //Click Authorise Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)
                }
                .execute(true)

        authorisationCode = AUTestUtil.getCodeFromJwtResponse(automationResponse.currentUrl.get())

        //Generate User Access Token
        userAccessToken = AURequestBuilder.getUserToken(authorisationCode, AUConstants.CODE_VERIFIER,
                auConfiguration.getAppInfoClientID())
        cdrArrangementId = userAccessToken.getCustomParameters().get("cdr_arrangement_id")
        Assert.assertNotNull(cdrArrangementId)

        //Wait Time Abandon Time Pass
        sleep(AUConstants.ABANDON_WAIT_TIME)

        //Active NonIndividual Authorisation and New Ongoing NonIndividual count increased by 2
        activeAuthIndividual = activeAuthIndividual + 2
        newAuthCurrentDayOngoingIndividual = newAuthCurrentDayOngoingIndividual + 2
        unattendedCurrentDay = unattendedCurrentDay + 1
        highPriorityCurrentDay = highPriorityCurrentDay + 4

        //Verify Metrics Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)
        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)

        //Asserting the response
        assertMetricsAuthorisationResponse(metricsResponse)
    }
}

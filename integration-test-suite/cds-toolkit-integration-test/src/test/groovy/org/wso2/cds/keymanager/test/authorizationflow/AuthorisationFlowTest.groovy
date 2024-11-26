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

package org.wso2.cds.keymanager.test.authorizationflow

import com.nimbusds.oauth2.sdk.AccessTokenResponse
import com.nimbusds.oauth2.sdk.ResponseMode
import com.nimbusds.oauth2.sdk.ResponseType
import org.wso2.cds.test.framework.AUTest
import org.wso2.cds.test.framework.automation.consent.AUBasicAuthAutomationStep
import org.wso2.cds.test.framework.constant.AUAccountProfile
import org.wso2.cds.test.framework.constant.AUAccountScope
import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.constant.AUPageObjects
import org.wso2.cds.test.framework.request_builder.AURequestBuilder
import org.wso2.cds.test.framework.utility.AURestAsRequestBuilder
import org.wso2.cds.test.framework.utility.AUTestUtil
import org.wso2.openbanking.test.framework.automation.AutomationMethod
import org.wso2.openbanking.test.framework.automation.NavigationAutomationStep
import org.openqa.selenium.By
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Test class for Authorisation Flow.
 */
class AuthorisationFlowTest extends AUTest {

    @Test(groups = "SmokeTest")
    void "TC0202001_Initiate authorisation consent flow"() {

        doConsentAuthorisation()
        Assert.assertNotNull(authorisationCode)
    }

    @Test(groups = "SmokeTest", dependsOnMethods = "TC0202001_Initiate authorisation consent flow")
    void "TC0203001_Exchange authorisation code for access token"() {

        AccessTokenResponse accessTokenResponse = getUserAccessTokenResponse(clientId)
        userAccessToken = accessTokenResponse.tokens.accessToken
        refreshToken = accessTokenResponse.tokens.refreshToken
        Assert.assertNotNull(userAccessToken)
        Assert.assertNotNull(refreshToken)
        Assert.assertNotNull(accessTokenResponse.getCustomParameters().get(AUConstants.CDR_ARRANGEMENT_ID))
    }

    @Test(groups = "SmokeTest",
            dependsOnMethods = "TC0203001_Exchange authorisation code for access token")
    void "TC0203006_Check the status of the access token after generating user access token"() {

        def response = AURequestBuilder.buildIntrospectionRequest(userAccessToken,
                auConfiguration.getAppInfoClientID(), 0)
                .post(AUConstants.INTROSPECTION_ENDPOINT)

        //Introspection validation can only be done for refresh token
        Assert.assertTrue(response.jsonPath().get("active").equals(false))
    }

    @Test(groups = "SmokeTest",
            dependsOnMethods = "TC0203001_Exchange authorisation code for access token")
    void "CDS-723_Retrieve user info"() {

        def response = AURestAsRequestBuilder.buildRequest()
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, "${AUConstants.AUTHORIZATION_BEARER_TAG}${userAccessToken}")
                .baseUri(auConfiguration.getServerAuthorisationServerURL())
                .get(AUConstants.USERINFO_ENDPOINT)

        Assert.assertEquals(response.getStatusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, "sub"), auConfiguration.getUserPSUName()+"@carbon.super")
    }

    @Test (priority = 1)
    void "OB-1141_Initiate authorisation consent flow with expired request uri"() {

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        sleep(60000)
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(),
                auConfiguration.getAppInfoClientID()).toURI().toString()

        String errorMessage = "Expired request URI"

        def automationResponse = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new NavigationAutomationStep(authoriseUrl, 10))
                .execute()

        String url = automationResponse.currentUrl.get()
        String errorUrl

        errorUrl = AUTestUtil.getErrorFromUrl(url)
        Assert.assertEquals(errorUrl, errorMessage)
    }

    @Test (priority = 1)
    void "TC0202007_Initiate authorisation consent deny flow"() {

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        String responseUrl = doConsentAuthorisationViaRequestUriDenyFlow(scopes, requestUri.toURI(),
                auConfiguration.getAppInfoClientID(), AUAccountProfile.INDIVIDUAL)

        authFlowError = AUTestUtil.getErrorDescriptionFromUrl(responseUrl)
        Assert.assertEquals(authFlowError, AUConstants.USER_DENIED_THE_CONSENT)

    }

    // Checking the validity of the refresh token instead of the access token since according to the CDS specification
    // introspection of Access Tokens must not be supported
    @Test (priority = 1)
    void "TC0203007_Status of the access token of previous authorisation code after re generating a new authorisation code"() {

        // Generating a new authorisation code
        doConsentAuthorisation()
        Assert.assertNotNull(authorisationCode)

        //Generate Access Token
        AccessTokenResponse accessTokenResponse = getUserAccessTokenResponse(clientId)
        String userAccessToken1 = accessTokenResponse.tokens.accessToken
        String refreshToken1 = accessTokenResponse.tokens.refreshToken
        String cdrArrangementId = accessTokenResponse.getCustomParameters().get(AUConstants.CDR_ARRANGEMENT_ID)

        //Check the status of the refresh token
        def introspectResponse1 = AURequestBuilder.buildIntrospectionRequest(refreshToken1,
                auConfiguration.getAppInfoClientID())
                .post(AUConstants.INTROSPECTION_ENDPOINT)

        Assert.assertTrue(introspectResponse1.jsonPath().get("active").equals(true))

        // Reauthorise the consent
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, cdrArrangementId)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI()).toURI().toString()

        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Verify if Profile Selection Enabled
                    if (auConfiguration.getProfileSelectionEnabled()) {

                        //Verify Account Selection Page
                        assert authWebDriver.isElementDisplayed(AUTestUtil.getAltSingleAccountXPath())
                        authWebDriver.clickButtonXpath(AUTestUtil.getAltSingleAccountXPath())

                        //Click Confirm Button
                        authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)

                        //Click Authorise Button
                        authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)

                    } else {
                        assert authWebDriver.isElementDisplayed(AUTestUtil.getSingleAccountXPath())
                        log.info("Profile Selection is Disabled")
                    }
                }
                .execute()

        // Get Code From URL
        authorisationCode = AUTestUtil.getCodeFromJwtResponse(automation.currentUrl.get())

        //Generate Access Token for new consent
        AccessTokenResponse accessTokenResponse2 = getUserAccessTokenResponse(clientId)
        String userAccessToken2 = accessTokenResponse2.tokens.accessToken
        String refreshToken2 = accessTokenResponse2.tokens.refreshToken
        String cdrArrangementId2 = accessTokenResponse2.getCustomParameters().get(AUConstants.CDR_ARRANGEMENT_ID)

        //Check the status of the refresh token of the new consent
        def introspectResponse2 = AURequestBuilder
                .buildIntrospectionRequest(refreshToken2, auConfiguration.getAppInfoClientID())
                .post(AUConstants.INTROSPECTION_ENDPOINT)

        Assert.assertTrue(introspectResponse2.jsonPath().get("active").equals(true))

        //Check the status of the refresh token of the previous consent
        introspectResponse1 = AURequestBuilder
                .buildIntrospectionRequest(refreshToken1, auConfiguration.getAppInfoClientID())
                .post(AUConstants.INTROSPECTION_ENDPOINT)

        Assert.assertTrue(introspectResponse1.jsonPath().get("active").equals(false))
    }

    // Checking the validity of the refresh token instead of the access token since according to the CDS specification
    // introspection of Access Tokens must not be supported
    @Test (priority = 1)
    void "TC0203009_Status of the consent after revoking the access token bound to the consent"() {

        // Generating a new authorisation code
        doConsentAuthorisation()
        Assert.assertNotNull(authorisationCode)

        //Generate Access Token
        AccessTokenResponse accessTokenResponse = getUserAccessTokenResponse(clientId)
        String userAccessToken = accessTokenResponse.tokens.accessToken
        String refreshToken = accessTokenResponse.tokens.refreshToken

        //Introspect the refresh token to check the status
        def response1 = AURequestBuilder
                .buildIntrospectionRequest(refreshToken, auConfiguration.getAppInfoClientID())
                .post(AUConstants.INTROSPECTION_ENDPOINT)

        Assert.assertTrue(response1.jsonPath().get("active").equals(true))

        // Revoke access Token
        def revokeResponse = AURequestBuilder
                .buildRevokeIntrospectionRequest(userAccessToken, auConfiguration.getAppInfoClientID())
                .post(AUConstants.TOKEN_REVOKE_PATH)

        Assert.assertEquals(revokeResponse.statusCode(), AUConstants.STATUS_CODE_200)

        //Introspect the refresh token to check the status
        def response2 = AURequestBuilder
                .buildIntrospectionRequest(refreshToken, auConfiguration.getAppInfoClientID())
                .post(AUConstants.INTROSPECTION_ENDPOINT)

        Assert.assertTrue(response2.jsonPath().get("active").equals(false))
    }

    @Test (priority = 2)
    void "OB-1142_Initiate authorisation consent flow only with scopes that require account selection"() {

        scopes = [
                AUAccountScope.BANK_ACCOUNT_BASIC_READ,
                AUAccountScope.BANK_ACCOUNT_DETAIL_READ,
                AUAccountScope.BANK_TRANSACTION_READ,
                AUAccountScope.BANK_REGULAR_PAYMENTS_READ
        ]

        doConsentAuthorisation()
        Assert.assertNotNull(authorisationCode)
    }

    @Test (priority = 2)
    void "OB-1143_Initiate authorisation consent flow only with scopes that do not require account selection"() {

        scopes = [
                AUAccountScope.BANK_PAYEES_READ,
                AUAccountScope.BANK_CUSTOMER_BASIC_READ,
                AUAccountScope.BANK_CUSTOMER_DETAIL_READ
        ]

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        doConsentAuthorisationViaRequestUriNoAccountSelection(scopes, requestUri.toURI())
        Assert.assertNotNull(authorisationCode)
    }

    @Test
    void "OB-1694_Deny consent authorisation request sent with state param in the display_consent page"() {

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        String state = auAuthorisationBuilder.state.toString()

        String responseUrl = doConsentAuthorisationViaRequestUriDenyFlow(scopes, requestUri.toURI(),
                auConfiguration.getAppInfoClientID(), AUAccountProfile.INDIVIDUAL)

        authFlowError = AUTestUtil.getErrorDescriptionFromUrl(responseUrl)
        Assert.assertEquals(authFlowError, AUConstants.USER_DENIED_THE_CONSENT)

        String stateParam = responseUrl.split("state=")[1].trim()
        Assert.assertEquals(state, stateParam)
    }

    @Test
    void "OB-1695_Cancel consent authorisation sent with state param in the login page"() {

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(),
                auConfiguration.getAppInfoClientID(), true).toURI().toString()

        //Consent Authorisation UI Flow
        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new NavigationAutomationStep(authoriseUrl, 10))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CANCEL_XPATH)
                    driver.findElement(By.xpath(AUPageObjects.CONFIRM_CONSENT_DENY_XPATH)).click()
                }
                .execute()

        def authUrl = automation.currentUrl.get()
        Assert.assertTrue(AUTestUtil.getDecodedUrl(authUrl).contains(AUConstants.USER_SKIP_THE_CONSENT_FLOW))
        String stateParam = authUrl.split("state=")[1]
        Assert.assertEquals(auAuthorisationBuilder.state.toString(), stateParam)
    }

    @Test
    void "OB-1696_Cancel consent authorisation request sent with state param in the account_selection page"() {

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(),
                auConfiguration.getAppInfoClientID(), true).toURI().toString()

        //Consent Authorisation UI Flow
        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Select Profile and Accounts
                    if (auConfiguration.getProfileSelectionEnabled()) {

                        //Select Individual Profile
                        authWebDriver.selectOption(AUPageObjects.INDIVIDUAL_PROFILE_SELECTION)
                        authWebDriver.clickButtonXpath(AUPageObjects.PROFILE_SELECTION_NEXT_BUTTON)

                        //Select Business Account 1
                        consentedAccount = authWebDriver.getElementAttribute(AUTestUtil.getSingleAccountXPath(),
                                AUPageObjects.VALUE)
                        authWebDriver.clickButtonXpath(AUTestUtil.getSingleAccountXPath())

                        authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CANCEL_XPATH)
                        driver.findElement(By.xpath(AUPageObjects.CONFIRM_CONSENT_DENY_XPATH)).click()

                    } else {
                        assert authWebDriver.isElementDisplayed(AUTestUtil.getSingleAccountXPath())
                        log.info("Profile Selection is Disabled")
                    }
                }
                .execute()

        def authUrl = automation.currentUrl.get()
        Assert.assertTrue(AUTestUtil.getDecodedUrl(authUrl).contains(AUConstants.CANCEL_ERROR_IN_ACCOUNTS_PAGE))
        String stateParam = authUrl.split("state=")[1]
        Assert.assertEquals(auAuthorisationBuilder.state.toString(), stateParam)
    }

    @Test
    void "CDS-700_Cancel consent with state param in Profile selection page"() {

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(),
                auConfiguration.getAppInfoClientID(), true).toURI().toString()

        //Consent Authorisation UI Flow
        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Select Profile and Accounts
                    if (auConfiguration.getProfileSelectionEnabled()) {

                        //Select Individual Profile
                        authWebDriver.selectOption(AUPageObjects.INDIVIDUAL_PROFILE_SELECTION)

                        authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CANCEL_XPATH)
                        driver.findElement(By.xpath(AUPageObjects.CONFIRM_CONSENT_DENY_XPATH)).click()

                    } else {
                        assert authWebDriver.isElementDisplayed(AUTestUtil.getSingleAccountXPath())
                        log.info("Profile Selection is Disabled")
                    }
                }
                .execute()

        def authUrl = automation.currentUrl.get()
        Assert.assertTrue(AUTestUtil.getDecodedUrl(authUrl).contains(AUConstants.CANCEL_ERROR_IN_ACCOUNTS_PAGE))
        Assert.assertTrue(authUrl.contains("state"))
    }

    @Test
    void "OB-1697_Deny consent authorisation request sent without state param in the display_consent page"() {

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "", auConfiguration.getAppInfoClientID(),
                auConfiguration.getAppInfoRedirectURL(), ResponseType.CODE.toString(), false)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        String authUrl = doConsentAuthorisationViaRequestUriDenyFlow(scopes, requestUri.toURI(),
                auConfiguration.getAppInfoClientID(), AUAccountProfile.INDIVIDUAL, false)

        Assert.assertTrue(AUTestUtil.getDecodedUrl(authUrl).contains(AUConstants.USER_DENIED_THE_CONSENT))
        Assert.assertFalse(authUrl.contains("state"))
    }

    @Test
    void "CDS-696_Cancel consent without state param in Profile selection page"() {

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(),
                auConfiguration.getAppInfoClientID(), false).toURI().toString()

        //Consent Authorisation UI Flow
        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Select Profile and Accounts
                    if (auConfiguration.getProfileSelectionEnabled()) {

                        //Select Individual Profile
                        authWebDriver.selectOption(AUPageObjects.INDIVIDUAL_PROFILE_SELECTION)

                        authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CANCEL_XPATH)
                        driver.findElement(By.xpath(AUPageObjects.CONFIRM_CONSENT_DENY_XPATH)).click()

                    } else {
                        assert authWebDriver.isElementDisplayed(AUTestUtil.getSingleAccountXPath())
                        log.info("Profile Selection is Disabled")
                    }
                }
                .execute()

        def authUrl = automation.currentUrl.get()
        Assert.assertTrue(AUTestUtil.getDecodedUrl(authUrl).contains(AUConstants.CANCEL_ERROR_IN_ACCOUNTS_PAGE))
        Assert.assertFalse(authUrl.contains("state"))
    }

    @Test
    void "OB-1699_Cancel consent authorisation request sent without state param in the account_selection page"() {

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(),
                auConfiguration.getAppInfoClientID(), false).toURI().toString()

        //Consent Authorisation UI Flow
        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Select Profile and Accounts
                    if (auConfiguration.getProfileSelectionEnabled()) {

                        //Select Individual Profile
                        authWebDriver.selectOption(AUPageObjects.INDIVIDUAL_PROFILE_SELECTION)
                        authWebDriver.clickButtonXpath(AUPageObjects.PROFILE_SELECTION_NEXT_BUTTON)

                        //Select Business Account 1
                        consentedAccount = authWebDriver.getElementAttribute(AUTestUtil.getSingleAccountXPath(),
                                AUPageObjects.VALUE)
                        authWebDriver.clickButtonXpath(AUTestUtil.getSingleAccountXPath())

                        authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CANCEL_XPATH)
                        driver.findElement(By.xpath(AUPageObjects.CONFIRM_CONSENT_DENY_XPATH)).click()

                    } else {
                        assert authWebDriver.isElementDisplayed(AUTestUtil.getSingleAccountXPath())
                        log.info("Profile Selection is Disabled")
                    }
                }
                .execute()

        def authUrl = automation.currentUrl.get()
        Assert.assertTrue(AUTestUtil.getDecodedUrl(authUrl).contains(AUConstants.CANCEL_ERROR_IN_ACCOUNTS_PAGE))
        Assert.assertFalse(authUrl.contains("state"))
    }

    @Test (priority = 2)
    void "OB-1144_Initiate authorisation consent flow only with openid scope"() {

        scopes = []
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = response.jsonPath().getString(AUConstants.ERROR_DESCRIPTION)

        String errorMessage = "No valid scopes found in the request"

        Assert.assertEquals(requestUri, errorMessage)
    }

    @Test (priority = 2)
    void "OB-1145_Initiate authorisation consent flow only with openid + scopes not applicable to CDR Data Retrieval"() {

        scopes = [
                AUAccountScope.ADMIN_METRICS_BASIC_READ,
                AUAccountScope.ADMIN_METADATA_UPDATE
        ]

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")

        String errorMessage = "No valid scopes found in the request"

        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION), errorMessage)
    }

    //Error - Not loading Profile Selection Page
    @Test
    void  "OB-1253_Initiate authorisation consent flow only with openid and profile scopes"() {

        scopes = [
                AUAccountScope.OPENID,
                AUAccountScope.PROFILE
        ]

        //Send PAR request
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_201)
        Assert.assertNotNull(requestUri)

        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(),
                auConfiguration.getAppInfoClientID(), false).toURI().toString()

        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.EXPIRES_IN))

        //Send Authorisation request
        doConsentAuthorisationViaRequestUriNoAccountSelection(scopes, requestUri.toURI(), auConfiguration.getAppInfoClientID(),
                AUAccountProfile.INDIVIDUAL)
        Assert.assertNotNull(authorisationCode)

        //Generate User Access Token
        AccessTokenResponse accessTokenResponse = getUserAccessTokenResponse(clientId)
        Assert.assertNotNull(accessTokenResponse.tokens.accessToken)
        verifyScopes(accessTokenResponse.toJSONObject().get("scope").toString() ,
                AUAccountScope.PROFILE.toString())
    }

    @Test
    void "OB-1254_create and verify authorisation consent only with openid, profile and Customer Data scopes"() {

        scopes = [
                AUAccountScope.PROFILE,
                AUAccountScope.BANK_CUSTOMER_BASIC_READ,
                AUAccountScope.BANK_CUSTOMER_DETAIL_READ
        ]

        //Send PAR request
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_201)
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_201)
        Assert.assertNotNull(requestUri)

        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(),
                auConfiguration.getAppInfoClientID(), false).toURI().toString()

        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.EXPIRES_IN))

        //Send Authorisation request
        doConsentAuthorisationViaRequestUriNoAccountSelection(scopes, requestUri.toURI(), auConfiguration.getAppInfoClientID(),
                AUAccountProfile.INDIVIDUAL)
        Assert.assertNotNull(authorisationCode)

        //Generate User Access Token
        AccessTokenResponse accessTokenResponse = getUserAccessTokenResponse(clientId)
        Assert.assertNotNull(accessTokenResponse.tokens.accessToken)
        verifyScopes(accessTokenResponse.toJSONObject().get("scope").toString() ,
                "${AUAccountScope.BANK_CUSTOMER_BASIC_READ.toString()} " +
                        "${AUAccountScope.BANK_CUSTOMER_DETAIL_READ.toString()} openid ${AUAccountScope.PROFILE.toString()}")
    }

    @Test
    void "OB-1257_Create and verify authorisation consent with openid, profile and other authorization scopes except Customer"() {

        scopes = [
                AUAccountScope.PROFILE,
                AUAccountScope.BANK_ACCOUNT_BASIC_READ,
                AUAccountScope.BANK_ACCOUNT_DETAIL_READ,
                AUAccountScope.BANK_TRANSACTION_READ,
                AUAccountScope.BANK_REGULAR_PAYMENTS_READ,
                AUAccountScope.BANK_PAYEES_READ,
        ]

        //Send PAR request
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_201)
        Assert.assertNotNull(requestUri)

        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(),
                auConfiguration.getAppInfoClientID(), false).toURI().toString()

        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.EXPIRES_IN))

        //Send Authorisation request
        doConsentAuthorisationViaRequestUri(scopes, requestUri.toURI(), auConfiguration.getAppInfoClientID(),
                AUAccountProfile.INDIVIDUAL)
        Assert.assertNotNull(authorisationCode)

        //Generate User Access Token
        AccessTokenResponse accessTokenResponse = getUserAccessTokenResponse(clientId)
        Assert.assertNotNull(accessTokenResponse.tokens.accessToken)
        verifyScopes(accessTokenResponse.toJSONObject().get("scope").toString() ,
                "${AUAccountScope.BANK_CUSTOMER_BASIC_READ.toString()} " +
                        "${AUAccountScope.BANK_ACCOUNT_BASIC_READ.toString()} ${AUAccountScope.BANK_ACCOUNT_DETAIL_READ.toString()} " +
                        "${AUAccountScope.BANK_TRANSACTION_READ.toString()} ${AUAccountScope.BANK_REGULAR_PAYMENTS_READ.toString()} " +
                        "${AUAccountScope.BANK_PAYEES_READ.toString()} openid ${AUAccountScope.PROFILE.toString()}")
    }

    @Test
    void "CDS-677_Send authorisation request with the same request_uri multiple times"() {

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        doConsentAuthorisationViaRequestUri(scopes, requestUri.toURI(), null, AUAccountProfile.INDIVIDUAL)

        //Resend authorisation request with the same request_uri
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), auConfiguration.getAppInfoClientID())
                .toURI().toString()

        automationResponse = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new NavigationAutomationStep(authoriseUrl, 10))
                .execute()

        def authUrl = automationResponse.currentUrl.get()
        Assert.assertTrue(AUTestUtil.getDecodedUrl(authUrl).contains("Provided request URI is not valid"))

        def error = URLDecoder.decode(authUrl.split("=")[1].split("&")[0], "UTF8")
        Assert.assertEquals(error, AUConstants.INVALID_REQUEST_URI)
    }

    //Note: Update Application Redirect URL of Service Provider before running the test case.
    @Test (enabled = false)
    void "CDS-714_Verify cancelling authorisation in account selection page when redirect URI having query parameters"() {

        String claims = generator.getRequestObjectClaim(scopes, AUConstants.DEFAULT_SHARING_DURATION, true, "",
                auConfiguration.getAppInfoRedirectURL(), auConfiguration.getAppInfoClientID(),
                auAuthorisationBuilder.getResponseType().toString(), true,
                auAuthorisationBuilder.getState().toString())

        String newRedirectUri = AUConstants.REDIRECT_URL_WITH_QUERY_PARAMS

        String modifiedClaimSet = generator.addClaimsFromRequestObject(claims, "redirect_uri",
                newRedirectUri)
        def response = auAuthorisationBuilder.doPushAuthorisationRequest(modifiedClaimSet)

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_201)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(),
                auConfiguration.getAppInfoClientID(), true).toURI().toString()

        //Consent Authorisation UI Flow
        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Select Profile and Accounts
                    if (auConfiguration.getProfileSelectionEnabled()) {

                        //Select Individual Profile
                        authWebDriver.selectOption(AUPageObjects.INDIVIDUAL_PROFILE_SELECTION)
                        authWebDriver.clickButtonXpath(AUPageObjects.PROFILE_SELECTION_NEXT_BUTTON)

                        //Select Business Account 1
                        consentedAccount = authWebDriver.getElementAttribute(AUTestUtil.getSingleAccountXPath(),
                                AUPageObjects.VALUE)
                        authWebDriver.clickButtonXpath(AUTestUtil.getSingleAccountXPath())

                        authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CANCEL_XPATH)
                        driver.findElement(By.xpath(AUPageObjects.CONFIRM_CONSENT_DENY_XPATH)).click()

                    } else {
                        assert authWebDriver.isElementDisplayed(AUTestUtil.getSingleAccountXPath())
                        log.info("Profile Selection is Disabled")
                    }
                }
                .execute()

        def authUrl = automation.currentUrl.get()
        def error_description = URLDecoder.decode(authUrl.split("&")[2].split("=")[1], "UTF8")

        Assert.assertTrue(error_description.contains(AUConstants.CANCEL_ERROR_IN_ACCOUNTS_PAGE))
        String actualRedirectUrl = authUrl.split("#")[0]
        Assert.assertEquals(actualRedirectUrl.toString(), newRedirectUri)
    }

    //Note: Update Application Redirect URL of Service Provider before running the test case.
    @Test (enabled = false)
    void "CDS-715_Verify deny authorisation when redirect URI having query parameters"() {

        String claims = generator.getRequestObjectClaim(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "", auConfiguration.getAppInfoRedirectURL(),
                auConfiguration.getAppInfoClientID(), auAuthorisationBuilder.getResponseType().toString(),
                true, auAuthorisationBuilder.getState().toString())

        String newRedirectUri = AUConstants.REDIRECT_URL_WITH_QUERY_PARAMS

        String modifiedClaimSet = generator.addClaimsFromRequestObject(claims, "redirect_uri",
                newRedirectUri)
        def response = auAuthorisationBuilder.doPushAuthorisationRequest(modifiedClaimSet)

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_201)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        //Consent Authorisation UI Flow
        String authUrl = doConsentAuthorisationViaRequestUriDenyFlow(scopes, requestUri.toURI(),
                auConfiguration.getAppInfoClientID(), AUAccountProfile.INDIVIDUAL, false)

        def error_description = URLDecoder.decode(authUrl.split("&")[2].split("=")[1], "UTF8")
        Assert.assertTrue(error_description.contains(AUConstants.USER_DENIED_THE_CONSENT))

        String actualRedirectUrl = authUrl.split("#")[0]
        Assert.assertEquals(actualRedirectUrl.toString(), newRedirectUri)
    }

    //Note: Update Application Redirect URL of Service Provider before running the test case.
    @Test (enabled = false)
    void "CDS-716_Verify cancelling authorisation in profile selection page when redirect URI having query parameters"() {

        String claims = generator.getRequestObjectClaim(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "", auConfiguration.getAppInfoRedirectURL(),
                auConfiguration.getAppInfoClientID(), auAuthorisationBuilder.getResponseType().toString(),
                true, auAuthorisationBuilder.getState().toString())

        String newRedirectUri = AUConstants.REDIRECT_URL_WITH_QUERY_PARAMS

        String modifiedClaimSet = generator.addClaimsFromRequestObject(claims, "redirect_uri",
                newRedirectUri)
        def response = auAuthorisationBuilder.doPushAuthorisationRequest(modifiedClaimSet)

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_201)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(),
                auConfiguration.getAppInfoClientID(), true).toURI().toString()

        //Consent Authorisation UI Flow
        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Select Profile and Accounts
                    if (auConfiguration.getProfileSelectionEnabled()) {

                        //Select Individual Profile
                        authWebDriver.selectOption(AUPageObjects.INDIVIDUAL_PROFILE_SELECTION)

                        authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CANCEL_XPATH)
                        driver.findElement(By.xpath(AUPageObjects.CONFIRM_CONSENT_DENY_XPATH)).click()

                    } else {
                        assert authWebDriver.isElementDisplayed(AUTestUtil.getSingleAccountXPath())
                        log.info("Profile Selection is Disabled")
                    }
                }
                .execute()

        def authUrl = automation.currentUrl.get()
        def error_description = URLDecoder.decode(authUrl.split("&")[2].split("=")[1], "UTF8")

        Assert.assertTrue(error_description.contains(AUConstants.CANCEL_ERROR_IN_ACCOUNTS_PAGE))
        String actualRedirectUrl = authUrl.split("#")[0]
        Assert.assertEquals(actualRedirectUrl.toString(), newRedirectUri)
    }

    //Note: Update Application Redirect URL of Service Provider before running the test case.
    @Test (enabled = false)
    void "CDS-715_Generate user access token for client which has query params in redirect url"() {

        String claims = generator.getRequestObjectClaim(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "", auConfiguration.getAppInfoRedirectURL(),
                auConfiguration.getAppInfoClientID(), auAuthorisationBuilder.getResponseType().toString(),
                true, auAuthorisationBuilder.getState().toString())

        String newRedirectUri = AUConstants.REDIRECT_URL_WITH_QUERY_PARAMS

        String modifiedClaimSet = generator.addClaimsFromRequestObject(claims, "redirect_uri",
                newRedirectUri)
        def response = auAuthorisationBuilder.doPushAuthorisationRequest(modifiedClaimSet)

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_201)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        //Consent Authorisation UI Flow
        doConsentAuthorisationViaRequestUri(scopes, requestUri.toURI(), null, AUAccountProfile.INDIVIDUAL)
        Assert.assertNotNull(authorisationCode)

        AccessTokenResponse accessTokenResponse = AURequestBuilder.getUserToken(authorisationCode,
                AUConstants.CODE_VERIFIER, auConfiguration.getAppInfoClientID(), newRedirectUri)
        userAccessToken = accessTokenResponse.tokens.accessToken
        Assert.assertNotNull(userAccessToken)
        Assert.assertNotNull(accessTokenResponse.tokens.refreshToken)
        Assert.assertNotNull(accessTokenResponse.getCustomParameters().get(AUConstants.CDR_ARRANGEMENT_ID))
    }

    @Test
    void "CDS-721_Send authorisation request with invalid response type, response mode"() {

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(scopes, requestUri.toURI(),
                auConfiguration.getAppInfoClientID(), auConfiguration.getAppInfoRedirectURL(), ResponseType.CODE_IDTOKEN,
                ResponseMode.QUERY_JWT).toURI().toString()

        def automation = doAuthorisationFlowNavigation(authoriseUrl, AUAccountProfile.INDIVIDUAL, true)
        authorisationCode = AUTestUtil.getCodeFromJwtResponse(automation.currentUrl.get())
    }

    @Test
    void "CDS-720_Send authorisation request without request_uri"() {

        requestUri = ""
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(),
                auConfiguration.getAppInfoClientID()).toURI().toString()

        String errorMessage = "invalid.redirect.uri"

        def automationResponse = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new NavigationAutomationStep(authoriseUrl, 10))
                .execute()

        String url = automationResponse.currentUrl.get()
        String errorUrl

        errorUrl = AUTestUtil.getErrorFromUrl(url)
        Assert.assertEquals(errorUrl, errorMessage)
    }
}

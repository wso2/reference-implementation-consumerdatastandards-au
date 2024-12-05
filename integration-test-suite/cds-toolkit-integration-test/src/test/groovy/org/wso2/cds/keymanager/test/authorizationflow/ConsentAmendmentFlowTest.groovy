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
import com.nimbusds.oauth2.sdk.TokenErrorResponse
import com.nimbusds.oauth2.sdk.token.RefreshToken
import org.wso2.cds.test.framework.AUTest
import org.wso2.cds.test.framework.automation.consent.AUBasicAuthAutomationStep
import org.wso2.cds.test.framework.constant.AUAccountProfile
import org.wso2.cds.test.framework.constant.AUAccountScope
import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.constant.AUPageObjects
import org.wso2.cds.test.framework.request_builder.AURequestBuilder
import org.wso2.cds.test.framework.utility.AUTestUtil
import org.wso2.openbanking.test.framework.automation.AutomationMethod
import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.Test

import java.nio.charset.Charset

/**
 * Test Class for Consent Amendment flow Validations.
 */
class ConsentAmendmentFlowTest extends AUTest{

    public List<AUAccountScope> scopes = [
            AUAccountScope.BANK_ACCOUNT_BASIC_READ,
            AUAccountScope.BANK_ACCOUNT_DETAIL_READ,
            AUAccountScope.BANK_TRANSACTION_READ,
            AUAccountScope.BANK_REGULAR_PAYMENTS_READ,
            AUAccountScope.BANK_CUSTOMER_BASIC_READ,
            AUAccountScope.BANK_CUSTOMER_DETAIL_READ
    ]

    def clientHeader = "${Base64.encoder.encodeToString(getCDSClient().getBytes(Charset.defaultCharset()))}"
    private def accessTokenResponse, accessTokenResponse2
    private String secondAuthorisationCode = null
    private String cdrArrangementId, userAccessToken, secondUserAccessToken
    private RefreshToken refreshToken, secondRefreshToken

    @Test(groups = "SmokeTest")
    void "CDS-9771_Verify Consent Amendment flow when both sharing duration and scope has been amended"() {

        // Send Authorisation request
        doConsentAuthorisation(auConfiguration.getAppInfoClientID(), AUAccountProfile.INDIVIDUAL)

        // Retrieve the user access token by auth code
        accessTokenResponse = getUserAccessTokenResponse(clientId)
        cdrArrangementId = accessTokenResponse.getCustomParameters().get(AUConstants.CDR_ARRANGEMENT_ID)
        userAccessToken = accessTokenResponse.tokens.accessToken
        refreshToken = accessTokenResponse.tokens.refreshToken

        Assert.assertNotNull(userAccessToken)
        Assert.assertNotNull(refreshToken)
        Assert.assertNotNull(cdrArrangementId)

        //remove an existing scope and add a new scope to amend the consent
        scopes.remove(AUAccountScope.BANK_TRANSACTION_READ)
        scopes.add(AUAccountScope.BANK_PAYEES_READ)

        //Retrieve and assert the request URI from Push Authorization request
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, cdrArrangementId)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI()).toURI().toString()

        //Consent Amendment Authorisation Flow
        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Click Confirm Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)

                    //Click Authorise Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)
                }
                .execute()

        // Get Code From URL
        authorisationCode = AUTestUtil.getCodeFromJwtResponse(automation.currentUrl.get())

        //Generate Token
        accessTokenResponse2 = getUserAccessTokenResponse(clientId)
        def cdrArrangementId2 = accessTokenResponse2.getCustomParameters().get(AUConstants.CDR_ARRANGEMENT_ID)
        secondUserAccessToken = accessTokenResponse2.tokens.accessToken
        secondRefreshToken = accessTokenResponse2.tokens.refreshToken

        Assert.assertNotNull(secondUserAccessToken)
        Assert.assertNotNull(secondRefreshToken)
        Assert.assertEquals(cdrArrangementId, cdrArrangementId2, "Amended CDR id is not original CDR id")

        //Get Account Transaction Details
        def responseAfterAmendment = AURequestBuilder.buildBasicRequestWithCustomHeaders(secondUserAccessToken,
                AUConstants.X_V_HEADER_ACCOUNTS, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get("${AUConstants.BULK_ACCOUNT_PATH}")

        Assert.assertEquals(responseAfterAmendment.statusCode(), AUConstants.STATUS_CODE_200)
    }

    @Test(groups = "SmokeTest")
    void "CDS-977_Verify Consent Amendment flow when both sharing duration and scope has been amended"() {

        // Send Authorisation request
        doConsentAuthorisation()

        // Retrieve the user access token by auth code
        accessTokenResponse = getUserAccessTokenResponse(clientId)
        cdrArrangementId = accessTokenResponse.getCustomParameters().get(AUConstants.CDR_ARRANGEMENT_ID)
        userAccessToken = accessTokenResponse.tokens.accessToken
        refreshToken = accessTokenResponse.tokens.refreshToken

        Assert.assertNotNull(userAccessToken)
        Assert.assertNotNull(refreshToken)
        Assert.assertNotNull(cdrArrangementId)

        //remove an existing scope and add a new scope to amend the consent
        scopes.remove(AUAccountScope.BANK_TRANSACTION_READ)
        scopes.add(AUAccountScope.PROFILE)

        //Retrieve and assert the request URI from Push Authorization request
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, cdrArrangementId)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI()).toURI().toString()

        //Consent Amendment Authorisation Flow
        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Verify Account Selection Page
                    assert authWebDriver.isElementDisplayed(AUTestUtil.getAltSingleAccountXPath())
                    authWebDriver.clickButtonXpath(AUTestUtil.getAltSingleAccountXPath())

                    //Click Confirm Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)

                    //Click Authorise Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)
                }
                .execute()

        // Get Code From URL
        authorisationCode = AUTestUtil.getCodeFromJwtResponse(automation.currentUrl.get())

        //Generate Token
        accessTokenResponse2 = getUserAccessTokenResponse(clientId)
        def cdrArrangementId2 = accessTokenResponse2.getCustomParameters().get(AUConstants.CDR_ARRANGEMENT_ID)
        secondUserAccessToken = accessTokenResponse2.tokens.accessToken
        secondRefreshToken = accessTokenResponse2.tokens.refreshToken

        Assert.assertNotNull(secondUserAccessToken)
        Assert.assertNotNull(secondRefreshToken)
        Assert.assertEquals(cdrArrangementId, cdrArrangementId2, "Amended CDR id is not original CDR id")

        //Get Account Transaction Details
        def responseAfterAmendment = AURequestBuilder.buildBasicRequestWithCustomHeaders(secondUserAccessToken,
                AUConstants.X_V_HEADER_ACCOUNTS, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get("${AUConstants.BULK_ACCOUNT_PATH}")

        Assert.assertEquals(responseAfterAmendment.statusCode(), AUConstants.STATUS_CODE_200)
    }

    @Test(groups = "SmokeTest",
            dependsOnMethods = "CDS-977_Verify Consent Amendment flow when both sharing duration and scope has been amended")
    void "CDS-978_Verify account retrieval for amended consent User Access Token to test consent enforcement"() {

        //Get Account Transaction Details
        def responseAfterAmendment = AURequestBuilder.buildBasicRequestWithCustomHeaders(secondUserAccessToken,
                AUConstants.X_V_HEADER_ACCOUNTS, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get("${AUConstants.BULK_ACCOUNT_PATH}")

        Assert.assertEquals(responseAfterAmendment.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(responseAfterAmendment.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_ACCOUNTS)
        Assert.assertNotNull(AUTestUtil.parseResponseBody(responseAfterAmendment,
                "${AUConstants.RESPONSE_DATA_BULK_ACCOUNTID_LIST}[0]"))
    }

    @Test(groups = "SmokeTest",
            dependsOnMethods = "CDS-977_Verify Consent Amendment flow when both sharing duration and scope has been amended")
    void "CDS-979_Verify account retrieval for original consent User Access Token"() {

        //Get Account Transaction Details
        def responseAfterAmendment = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_ACCOUNTS, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get("${AUConstants.BULK_ACCOUNT_PATH}")

        // Assert if details of selected accounts cannot be retrieved via accounts get call
        Assert.assertEquals(responseAfterAmendment.statusCode(), AUConstants.STATUS_CODE_401)
        Assert.assertTrue(AUTestUtil.parseResponseBody(responseAfterAmendment, AUConstants.ERROR_DESCRIPTION)
                .contains(AUConstants.INVALID_CREDENTIALS))
        Assert.assertTrue(AUTestUtil.parseResponseBody(responseAfterAmendment, AUConstants.ERROR)
                .contains(AUConstants.INVALID_CLIENT))
    }

    @Test(dependsOnMethods = "CDS-977_Verify Consent Amendment flow when both sharing duration and scope has been amended")
    void "CDS-980_Verify Token Introspection for newly amended consent user access Token"() {

        Response response = AURequestBuilder.buildIntrospectionRequest(secondRefreshToken.toString(), auConfiguration.getAppInfoClientID())
                .post(AUConstants.INTROSPECTION_ENDPOINT)

        // Assert if Token status is active for latest consent amendment
        Assert.assertTrue(response.jsonPath().get("active"))
    }

    @Test(dependsOnMethods = "CDS-977_Verify Consent Amendment flow when both sharing duration and scope has been amended")
    void "CDS-981_Verify Token Introspection for previous user access Token"() {

        Response response = AURequestBuilder.buildIntrospectionRequest(refreshToken.toString(), auConfiguration.getAppInfoClientID())
                .post(AUConstants.INTROSPECTION_ENDPOINT)

        // Assert if Token status is NOT active for previous consent amendment - user access token
        Assert.assertFalse(response.jsonPath().get("active"))
    }

    @Test(dependsOnMethods = "CDS-977_Verify Consent Amendment flow when both sharing duration and scope has been amended")
    void "CDS-982_Verify regenerate Access Token using Refresh Token for amended Consent"() {

        RefreshToken refreshToken = new RefreshToken(secondRefreshToken.toString())
        AccessTokenResponse userAccessToken = getUserAccessTokenFormRefreshToken(refreshToken)
        Assert.assertNotNull(userAccessToken.tokens.accessToken)
    }

    @Test(dependsOnMethods = "CDS-977_Verify Consent Amendment flow when both sharing duration and scope has been amended")
    void "CDS-983_Verify regenerate Access Token using Refresh Token for original Consent"() {

        TokenErrorResponse userAccessToken = AURequestBuilder.getUserTokenFromRefreshTokenErrorResponse(refreshToken)
        Assert.assertEquals(userAccessToken.toJSONObject().get(AUConstants.ERROR_DESCRIPTION),
                "Persisted access token data not found")
    }

    @Test(groups = "SmokeTest",
            dependsOnMethods = "CDS-977_Verify Consent Amendment flow when both sharing duration and scope has been amended",
            priority = 1)
    void "CDS-984_Verify an amended consent can be re-amended"() {

        //Re amend the scopes of the consent amendment
        scopes.remove(AUAccountScope.BANK_PAYEES_READ)
        scopes.add(AUAccountScope.BANK_CUSTOMER_DETAIL_READ)

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, cdrArrangementId)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI()).toURI().toString()

        //Consent Amendment Authorisation Flow
        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Verify Account Selection Page
                    Assert.assertTrue(authWebDriver.isElementSelected(AUTestUtil.getSingleAccountXPath()))

                    //Click Confirm Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)

                    //Click Authorise Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)
                }
                .execute()

        // Get Code From URL
        authorisationCode = AUTestUtil.getCodeFromJwtResponse(automation.currentUrl.get())

        //Generate Token
        accessTokenResponse2 = getUserAccessTokenResponse(clientId)
        def cdrArrangementId3 = accessTokenResponse2.getCustomParameters().get(AUConstants.CDR_ARRANGEMENT_ID)
        secondUserAccessToken = accessTokenResponse2.tokens.accessToken
        secondRefreshToken = accessTokenResponse2.tokens.refreshToken

        verifyScopes(accessTokenResponse2.toJSONObject().get("scope").toString(), scopes.toString())
        Assert.assertEquals(cdrArrangementId, cdrArrangementId3, "Amended CDR id is not original CDR id")
    }

    @Test(dependsOnMethods = "CDS-977_Verify Consent Amendment flow when both sharing duration and scope has been amended",
            priority = 1)
    void "CDS-985_Verify a revoked consent cannot be amended"() {

        //Revoke the Consent
        Response cdrResponse = AURequestBuilder.doRevokeConsent(auConfiguration.getAppInfoClientID(), cdrArrangementId)
        Assert.assertEquals(cdrResponse.statusCode(), AUConstants.STATUS_CODE_204)

        //Send Consent Amendment Request
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, cdrArrangementId)

        Assert.assertEquals(response.getStatusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION),
                "Invalid cdr-arrangement-id or consent is not in Authorised state")
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR),
                AUConstants.INVALID_REQUEST_OBJECT)
    }

    @Test(priority = 1)
    void "CDS-986_Verify Status of the refresh token after the Consent Amendment - sharing duration has expired"() {

        // Send Authorisation request
        doConsentAuthorisation()

        // Retrieve the user access token by auth code
        accessTokenResponse = getUserAccessTokenResponse(clientId)
        cdrArrangementId = accessTokenResponse.getCustomParameters().get(AUConstants.CDR_ARRANGEMENT_ID)
        userAccessToken = accessTokenResponse.tokens.accessToken
        refreshToken = accessTokenResponse.tokens.refreshToken

        Assert.assertNotNull(userAccessToken)
        Assert.assertNotNull(refreshToken)
        Assert.assertNotNull(cdrArrangementId)

        //remove an existing scope and add a new scope to amend the consent
        scopes.remove(AUAccountScope.BANK_TRANSACTION_READ)
        scopes.add(AUAccountScope.BANK_PAYEES_READ)

        //Retrieve and assert the request URI from Push Authorization request
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.SHORT_SHARING_DURATION,
                true, cdrArrangementId)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI()).toURI().toString()

        //Consent Amendment Authorisation Flow
        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Verify Account Selection Page
                    Assert.assertTrue(authWebDriver.isElementDisplayed(AUTestUtil.getAltSingleAccountXPath()))
                    authWebDriver.clickButtonXpath(AUTestUtil.getAltSingleAccountXPath())

                    //Click Confirm Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)

                    //Click Authorise Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)
                }
                .execute()

        // Get Code From URL
        authorisationCode = AUTestUtil.getCodeFromJwtResponse(automation.currentUrl.get())
        Assert.assertNotNull(authorisationCode)

        //Generate Token
        accessTokenResponse2 = getUserAccessTokenResponse(clientId)
        secondUserAccessToken = accessTokenResponse2.tokens.accessToken
        secondRefreshToken = accessTokenResponse2.tokens.refreshToken

        sleep(90000)

        //Verify the status of the refresh token
        AccessTokenResponse userAccessToken3 = AURequestBuilder.getUserTokenFromRefreshTokenErrorResponse(secondRefreshToken as RefreshToken)
        Assert.assertEquals(userAccessToken3.toJSONObject().get(AUConstants.ERROR_DESCRIPTION),
                "Refresh token is expired.")
    }

    @Test(priority = 1)
    void "CDS-987_Verify a consent cannot be amended with expired CDR Amendment ID"() {

        // Send Authorisation request
        doConsentAuthorisation()

        // Retrieve the user access token by auth code
        accessTokenResponse = getUserAccessTokenResponse(clientId)
        cdrArrangementId = accessTokenResponse.getCustomParameters().get(AUConstants.CDR_ARRANGEMENT_ID)
        userAccessToken = accessTokenResponse.tokens.accessToken
        refreshToken = accessTokenResponse.tokens.refreshToken

        Assert.assertNotNull(userAccessToken)
        Assert.assertNotNull(refreshToken)
        Assert.assertNotNull(cdrArrangementId)

        //remove an existing scope and add a new scope to amend the consent
        scopes.remove(AUAccountScope.BANK_TRANSACTION_READ)
        scopes.add(AUAccountScope.BANK_PAYEES_READ)

        //Retrieve and assert the request URI from Push Authorization request
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.SHORT_SHARING_DURATION,
                true, cdrArrangementId)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI()).toURI().toString()

        sleep(70000)

        //Consent Amendment Authorisation Flow
        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .execute()

        Assert.assertTrue(AUTestUtil.getDecodedUrl(automation.currentUrl.get())
                .contains("There's no active sharing arrangement corresponds to consent id " + cdrArrangementId))
    }

    @Test(priority = 1)
    void "CDS-988_Verify a consent cannot be amended with invalid CDR Amendment ID"() {

        String invalidCDRArrangementID = "80486445-2744-464d-af90-57654f4d5b00"

        //remove an existing scope and add a new scope to amend the consent
        scopes.remove(AUAccountScope.BANK_TRANSACTION_READ)
        scopes.add(AUAccountScope.BANK_PAYEES_READ)

        //Retrieve and assert the request URI from Push Authorization request
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, invalidCDRArrangementID)

        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION), "Invalid cdr_arrangement_id")
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR), AUConstants.INVALID_REQUEST_OBJECT)
    }

    @Test
    void "CDS-694_Consent Amendment for consent with 0 sharing duration" () {

        //Send Authorisation Request
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.SINGLE_ACCESS_CONSENT,
                true, "")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_201)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        doConsentAuthorisationViaRequestUri(scopes, requestUri.toURI(), auConfiguration.getAppInfoClientID(),
                AUAccountProfile.INDIVIDUAL)
        Assert.assertNotNull(authorisationCode)

        // Retrieve the user access token by auth code
        accessTokenResponse = getUserAccessTokenResponse(clientId)
        cdrArrangementId = accessTokenResponse.getCustomParameters().get(AUConstants.CDR_ARRANGEMENT_ID)
        userAccessToken = accessTokenResponse.tokens.accessToken
        refreshToken = accessTokenResponse.tokens.refreshToken

        Assert.assertNotNull(userAccessToken)
        Assert.assertNull(refreshToken)
        Assert.assertNotNull(cdrArrangementId)

        //remove an existing scope and add a new scope to amend the consent
        scopes.remove(AUAccountScope.BANK_TRANSACTION_READ)
        scopes.add(AUAccountScope.BANK_PAYEES_READ)

        //Consent Amendment - PAR request
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.SINGLE_ACCESS_CONSENT,
                true, cdrArrangementId)

        Assert.assertEquals(response.getStatusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION), "Expired cdr-arrangement-id")
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR), AUConstants.INVALID_REQUEST_OBJECT)
    }
}

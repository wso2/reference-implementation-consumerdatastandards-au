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

package com.wso2.cds.integration.test.authflow

import com.nimbusds.oauth2.sdk.AccessTokenResponse
import com.wso2.cds.test.framework.AUTest
import com.wso2.cds.test.framework.automation.consent.AUBasicAuthAutomationStep
import com.wso2.cds.test.framework.constant.AUAccountScope
import com.wso2.cds.test.framework.constant.AUConstants
import com.wso2.cds.test.framework.constant.AUPageObjects
import com.wso2.cds.test.framework.request_builder.AURegistrationRequestBuilder
import com.wso2.cds.test.framework.request_builder.AURequestBuilder
import com.wso2.cds.test.framework.utility.AUTestUtil
import com.wso2.openbanking.test.framework.automation.AutomationMethod
import com.wso2.openbanking.test.framework.automation.NavigationAutomationStep
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

import java.nio.charset.Charset
import java.text.SimpleDateFormat

/**
 * Consent Amendment Flow UI Validation and CX Guideline Tests.
 */
class ConsentAmendmentFlowUIValidationTest extends AUTest {

    def clientHeader = "${Base64.encoder.encodeToString(getCDSClient().getBytes(Charset.defaultCharset()))}"

    @BeforeClass(alwaysRun = true)
    void "init"() {

        scopes.remove(AUAccountScope.BANK_PAYEES_READ)

        doConsentAuthorisation()
        Assert.assertNotNull(authorisationCode)

        AccessTokenResponse accessTokenResponse = getUserAccessTokenResponse(auConfiguration.getAppInfoClientID())
        userAccessToken = accessTokenResponse.tokens.accessToken
        cdrArrangementId = accessTokenResponse.getCustomParameters().get(AUConstants.CDR_ARRANGEMENT_ID)
        Assert.assertNotNull(userAccessToken)
        Assert.assertNotNull(cdrArrangementId)

        scopes.remove(AUAccountScope.BANK_TRANSACTION_READ)
        scopes.add(AUAccountScope.BANK_PAYEES_READ)
    }

    @Test()
    void "CDS206_Verify the account selection page should show the pre selected account"() {

        //Retrieve Request URI via Push request
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, cdrArrangementId)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI()).toURI().toString()

        //Verification of the pre selected accounts
        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)
                    Assert.assertTrue(authWebDriver.isElementSelected(AUTestUtil.getSingleAccountXPath()),
                            "Preselected account not selected")
                }
                .execute()
    }

    @Test()
    void "CDS207_Verify the consumer can view the name of the relevant accredited data recipient"() {

        // Data recipient name extracted from DCR GET call
        List<AUAccountScope> dcrScopes = [AUAccountScope.CDR_REGISTRATION]

        accessToken = getApplicationAccessToken(auConfiguration.getAppInfoClientID())
        Assert.assertNotNull(accessToken)

        def registrationResponse = AURegistrationRequestBuilder.buildBasicRequest(accessToken)
                .when()
                .get(AUConstants.DCR_REGISTRATION_ENDPOINT + auConfiguration.getAppInfoClientID())

        Assert.assertNotNull(registrationResponse)
        String adrName = registrationResponse.jsonPath().get("org_name") + ", " + registrationResponse.jsonPath().get("client_name")

        //Retrieve Request URI via Push request
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, cdrArrangementId)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI()).toURI().toString()

        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Verify Account Selection Page
                    assert authWebDriver.isElementDisplayed(AUTestUtil.getAltSingleAccountXPath())
                    authWebDriver.clickButtonXpath(AUTestUtil.getAltSingleAccountXPath())

                    //Click Confirm Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)

                    Assert.assertTrue(authWebDriver.getAttributeText(AUPageObjects.ADR_NAME_HEADER_XPATH).contains(adrName))
                }
                .execute()
    }

    @Test()
    void "CDS208_Verify the System should display the review page to reflect the amended attributes"() {

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, 172800,
                true, cdrArrangementId)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI()).toURI().toString()

        Date today = new Date()
        Calendar calendar = Calendar.getInstance()
        calendar.setTime(today)
        calendar.add(Calendar.DAY_OF_YEAR, 2)
        Date tomorrow = calendar.getTime()

        String sharingPeriod = new SimpleDateFormat("YYYY-MM-dd").format(today) + " - " +
                new SimpleDateFormat("YYYY-MM-dd").format(tomorrow)

        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Verify Account Selection Page
                    assert authWebDriver.isElementDisplayed(AUTestUtil.getAltSingleAccountXPath())
                    authWebDriver.clickButtonXpath(AUTestUtil.getAltSingleAccountXPath())

                    //Click Confirm Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)

                    Assert.assertTrue(authWebDriver.isElementDisplayed(AUPageObjects.LBL_NEW_PAYEES_INDICATOR_XPATH))
                    Assert.assertTrue(authWebDriver.isElementDisplayed(AUPageObjects.LBL_NEW_SHARING_DURATION_XPATH))
                    Assert.assertTrue(authWebDriver.getAttributeText(AUPageObjects.BTN_CONSENT_EXPIRY_XPATH).contains(sharingPeriod))
                }
                .execute()
    }

    @Test
    void "CDS209_Verify the consent amendment of multiple accounts"() {

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.AMENDED_SHARING_DURATION,
                true, cdrArrangementId)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI()).toURI().toString()

        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //De Select Account 2
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
        AccessTokenResponse accessTokenResponse2 = getUserAccessTokenResponse(auConfiguration.getAppInfoClientID())
        def cdrArrangementId2 = accessTokenResponse2.getCustomParameters().get(AUConstants.CDR_ARRANGEMENT_ID)
        String secondUserAccessToken = accessTokenResponse2.tokens.accessToken

        Assert.assertNotNull(secondUserAccessToken)
        Assert.assertEquals(cdrArrangementId, cdrArrangementId2, "Amended CDR id is not original CDR id")

        //Get Account Transaction Details
        def responseAfterAmendment = AURequestBuilder.buildBasicRequestWithCustomHeaders(secondUserAccessToken,
                AUConstants.X_V_HEADER_ACCOUNTS, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get("${AUConstants.BULK_ACCOUNT_PATH}")

        Assert.assertEquals(responseAfterAmendment.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(responseAfterAmendment.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_ACCOUNTS)

        Assert.assertNotNull(AUTestUtil.parseResponseBody(responseAfterAmendment,
                "${AUConstants.RESPONSE_DATA_BULK_ACCOUNTID_LIST}[0]"))
        Assert.assertNull(AUTestUtil.parseResponseBody(responseAfterAmendment,
                "${AUConstants.RESPONSE_DATA_BULK_ACCOUNTID_LIST}[1]"))
    }

    @Test
    void "CDS210_Verify the instruction on how to manage the data-sharing agreements"() {

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.AMENDED_SHARING_DURATION,
                true, cdrArrangementId)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI()).toURI().toString()

        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Verify Account Selection Page
                    assert authWebDriver.isElementDisplayed(AUTestUtil.getAltSingleAccountXPath())
                    authWebDriver.clickButtonXpath(AUTestUtil.getAltSingleAccountXPath())

                    //Click Confirm Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)

                    Assert.assertTrue(driver.findElement(By.xpath(AUPageObjects.LBL_WHERE_TO_MANAGE_INSTRUCTION_XPATH))
                            .getText().contains(AUConstants.LBL_WHERE_TO_MANAGE_INSTRUCTION))
                }
                .execute()
    }

    @Test(priority = 1)
    void "CDS211_Verify back button on the CDR policy page at consent amendment"() {

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.AMENDED_SHARING_DURATION,
                true, cdrArrangementId)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI()).toURI().toString()

        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Verify Account Selection Page
                    assert authWebDriver.isElementDisplayed(AUTestUtil.getAltSingleAccountXPath())
                    authWebDriver.clickButtonXpath(AUTestUtil.getAltSingleAccountXPath())

                    //Click Confirm Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)

                    Assert.assertTrue(authWebDriver.isElementDisplayed(AUPageObjects.CONSENT_AUTHORIZE_FLOW_BACK_XPATH))
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_AUTHORIZE_FLOW_BACK_XPATH)
                    Assert.assertTrue(authWebDriver.isElementDisplayed(AUPageObjects.LBL_SELECT_THE_ACCOUNTS_XPATH))
                }
                .execute()
    }

    @Test(priority = 1)
    void "CDS212_Verify deny flow for consent amendment at consent amendment"() {

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.AMENDED_SHARING_DURATION,
                true, cdrArrangementId)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI()).toURI().toString()

        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Verify Account Selection Page
                    assert authWebDriver.isElementDisplayed(AUTestUtil.getAltSingleAccountXPath())
                    authWebDriver.clickButtonXpath(AUTestUtil.getAltSingleAccountXPath())

                    //Click Confirm Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)

                    Assert.assertTrue(authWebDriver.isElementDisplayed(AUPageObjects.CONSENT_DENY_XPATH))
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_DENY_XPATH)

                    WebDriverWait wait = new WebDriverWait(driver, 60)

                    wait.until(
                            ExpectedConditions.invisibilityOfElementLocated(By.xpath(AUPageObjects.CONSENT_DENY_XPATH)))}
                .execute()
        Assert.assertTrue(AUTestUtil.getDecodedUrl(automation.currentUrl.get()).contains(AUConstants.USER_DENIED_THE_CONSENT))
    }

    @Test(priority = 1)
    void "CDS213_Verify an initiate Authorization request for consent Amendment with a expired request_uri from PAR"() {

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.AMENDED_SHARING_DURATION,
                true, cdrArrangementId)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI()).toURI().toString()

        sleep(65000)

        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new NavigationAutomationStep(authoriseUrl, 10))
                .execute()

        Assert.assertTrue(AUTestUtil.getErrorFromUrl(automation.currentUrl.get()).contains("Expired request URI"))
    }
}

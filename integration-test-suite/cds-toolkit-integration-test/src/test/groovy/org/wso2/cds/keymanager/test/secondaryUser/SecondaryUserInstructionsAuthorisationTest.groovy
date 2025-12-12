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

package org.wso2.cds.keymanager.test.secondaryUser

import org.wso2.bfsi.test.framework.automation.AutomationMethod
import org.wso2.cds.test.framework.AUTest
import org.wso2.cds.test.framework.automation.consent.AUBasicAuthAutomationStep
import org.wso2.cds.test.framework.constant.AUAccountScope
import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.constant.AUPageObjects
import org.wso2.cds.test.framework.request_builder.AURegistrationRequestBuilder
import org.wso2.cds.test.framework.utility.AUTestUtil
import io.restassured.response.Response
import org.openqa.selenium.By
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

/**
 * Secondary User Instructions Test.
 */
class SecondaryUserInstructionsAuthorisationTest extends AUTest {

    def shareableElements
    String accountID, userId

    @BeforeClass(alwaysRun = true)
    void "Provide User Permissions"() {

        auConfiguration.setTppNumber(0)
        auConfiguration.setPsuNumber(1)
        clientId = auConfiguration.getAppInfoClientID()
        //Get Sharable Account List and Secondary User with Authorize Permission
        shareableElements = AUTestUtil.getSecondaryUserDetails(getSharableBankAccounts())

        accountID =  shareableElements[AUConstants.PARAM_ACCOUNT_ID]
        userId = auConfiguration.getUserPSUName()

        def updateResponse = updateSecondaryUserInstructionPermission(accountID, userId, AUConstants.ACTIVE)
        Assert.assertEquals(updateResponse.statusCode(), AUConstants.OK)

        //Get Secondary Joint Account
        shareableElements = AUTestUtil.getSecondaryUserDetails(getSharableBankAccounts(), false)
        accountID =  shareableElements[AUConstants.PARAM_ACCOUNT_ID]

        def updateResponseJointAccount = updateSecondaryUserInstructionPermission(accountID, userId, AUConstants.ACTIVE)
        Assert.assertEquals(updateResponseJointAccount.statusCode(), AUConstants.OK)
    }

    @Test(groups = "SmokeTest")
    void "CDS-411_Verify Secondary accounts selection Authorization Flow"() {

        //Send Push Authorisation Request
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        //Select Secondary Account during authorisation
        doSecondaryAccountSelection(scopes, requestUri.toURI())
        Assert.assertNotNull(authorisationCode)

        //Get User Access Token
        generateUserAccessToken()

        //Account Retrieval
        Response accountResponse = doAccountRetrieval(userAccessToken)
        Assert.assertEquals(accountResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertNotNull(AUTestUtil.parseResponseBody(accountResponse,
                "${AUConstants.RESPONSE_DATA_BULK_ACCOUNTID_LIST}[0]"))
    }

    @Test
    void "CDS-412_Verify consent authorization no account scopes in the consent"() {

        List<AUAccountScope> no_account_scopes = [

                AUAccountScope.BANK_PAYEES_READ
        ]

        response = auAuthorisationBuilder.doPushAuthorisationRequest(no_account_scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, cdrArrangementId)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        doSecondaryUserAuthFlowWithoutAccountSelection(no_account_scopes, requestUri.toURI(), clientId)
        Assert.assertNotNull(authorisationCode)
    }

    @Test
    void "CDS-413_Verify the account list should display the accounts based criteria"() {

        // Data recipient name extracted from DCR GET call
        accessToken = getApplicationAccessToken(auConfiguration.getAppInfoClientID())
        Assert.assertNotNull(accessToken)

        def registrationResponse = AURegistrationRequestBuilder.buildBasicRequest(accessToken)
                .when()
                .get(AUConstants.DCR_REGISTRATION_ENDPOINT + auConfiguration.getAppInfoClientID())

        String adrName = "Mock Company Inc., Mock Software 1"

        //Send Authorisation Request via PAR
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), clientId)
                .toURI().toString()

        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    Assert.assertTrue(authWebDriver.getAttributeText(AUPageObjects.ADR_NAME_HEADER_XPATH).contains(adrName))

                    //Verify Account List
                    Assert.assertTrue(authWebDriver.isElementDisplayed(AUTestUtil.getSingleAccountXPath()))
                    Assert.assertTrue(authWebDriver.isElementDisplayed(AUTestUtil.getAltSingleAccountXPath()))
                    Assert.assertTrue(authWebDriver.isElementDisplayed(AUTestUtil.getSecondaryAccount1XPath()))
                    Assert.assertTrue(authWebDriver.isElementDisplayed(AUTestUtil.getSecondaryJointAccount1XPath()))

                    //Select Secondary Account
                    selectSecondaryAccount(authWebDriver, false)

                    //Click Submit/Next Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_SUBMIT_XPATH)

                    //Click Confirm Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)
                }
                .execute()

        authorisationCode = AUTestUtil.getCodeFromJwtResponse(automation.currentUrl.get())
    }

    @Test
    void "CDS-416_Verify displaying secondary tag along with the account number of the secondary user account"() {

        //Send Authorisation Request via PAR
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), clientId)
                .toURI().toString()

        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Verify Account List display tag along with the account number of the secondary user account
                    Assert.assertTrue(authWebDriver.getAttributeText(AUPageObjects.LBL_SECONDARY_ACCOUNT_1)
                            .contains("secondary"))
                    Assert.assertTrue(authWebDriver.getAttributeText(AUPageObjects.LBL_SECONDARY_JOINT_ACCOUNT_1)
                            .contains("secondary"))
                }
                .execute()
    }

    @Test
    void "CDS-417_Verify displaying secondary_joint tag along with the account number of the secondary user joint account"() {

        //Send Authorisation Request via PAR
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), clientId)
                .toURI().toString()

        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Verify Account List display both secondary and joint tags along with the account number
                    Assert.assertTrue(authWebDriver.getAttributeText(AUPageObjects.LBL_SECONDARY_JOINT_ACCOUNT_1)
                            .contains("secondary_joint"))
                }
                .execute()
    }

    //TODO: Update the test case to enable all the unavailable accounts
    //@Test
    void "CDS-547_Verify the account selection page when there are no unavailable accounts"() {

        //Send Authorisation Request via PAR
        authorisationCode = null
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, cdrArrangementId)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), clientId)
                .toURI().toString()

        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->

                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Verify The page does not contain unavailable accounts
                    Assert.assertFalse(authWebDriver.isElementDisplayed(AUPageObjects.LBL_ACCOUNTS_UNAVAILABLE_TO_SHARE))
                }
                .addStep(getWaitForRedirectAutomationStep())
                .execute()
    }

    @Test
    void "CDS-546_Verify selecting all secondary user accounts in authorisation"() {

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        //Select Secondary Account during authorisation
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), auConfiguration.getAppInfoClientID())
                .toURI().toString()

        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Select Secondary Account
                    authWebDriver.clickButtonXpath(AUPageObjects.BTN_SELECT_ALL)

                    //Click Submit/Next Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_SUBMIT_XPATH)

                    //Click Confirm Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)
                }
                .execute()

        // Get Code From URL
        authorisationCode = AUTestUtil.getCodeFromJwtResponse(automation.currentUrl.get())
        Assert.assertNotNull(authorisationCode)
    }

    @Test
    void "CDS-549_Verify cancellation of authorisation process in account selection page without selecting accounts"() {

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), clientId)
                .toURI().toString()

        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Click Submit/Next Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CANCEL_XPATH)
                    driver.findElement(By.xpath(AUPageObjects.CONFIRM_CONSENT_DENY_XPATH)).click()
                }
                .execute()

        def authUrl = automation.currentUrl.get()
        Assert.assertTrue(AUTestUtil.getDecodedUrl(authUrl).contains(AUConstants.CANCEL_ERROR_IN_ACCOUNTS_PAGE))
        def stateParam = authUrl.split("state=")[1]
        Assert.assertEquals(auAuthorisationBuilder.state.toString(), stateParam)
    }

    @Test
    void "CDS-550_Verify cancellation of authorisation process in account selection page by selecting an accounts"() {

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), clientId)
                .toURI().toString()

        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Select Secondary Account
                    selectSecondaryAccount(authWebDriver, false)

                    //Click Submit/Next Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CANCEL_XPATH)
                    driver.findElement(By.xpath(AUPageObjects.CONFIRM_CONSENT_DENY_XPATH)).click()
                }
                .execute()

        def authUrl = automation.currentUrl.get()
        Assert.assertTrue(AUTestUtil.getDecodedUrl(authUrl).contains(AUConstants.CANCEL_ERROR_IN_ACCOUNTS_PAGE))
        def stateParam = authUrl.split("state=")[1]
        Assert.assertEquals(auAuthorisationBuilder.state.toString(), stateParam)
    }

    @Test
    void "CDS-627_Verify user nominated for both individual and joint accounts"() {

        //Provide secondary user instruction permissions for joint account
        shareableElements = AUTestUtil.getSecondaryUserDetails(getSharableBankAccounts(), false)
        String accountID =  shareableElements[AUConstants.PARAM_ACCOUNT_ID]
        String userId = auConfiguration.getUserPSUName()

        def updateResponse = updateSecondaryUserInstructionPermission(accountID, userId, AUConstants.ACTIVE)
        Assert.assertEquals(updateResponse.statusCode(), AUConstants.OK)

        //Consent Authorisation
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), clientId)
                .toURI().toString()

        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Select Secondary Accounts - Individual and Joint Accounts
                    consentedAccount = authWebDriver.getElementAttribute(AUTestUtil.getSecondaryAccount1XPath(),
                            AUPageObjects.VALUE)
                    authWebDriver.clickButtonXpath(AUTestUtil.getSecondaryAccount1XPath())

                    consentedAccount = authWebDriver.getElementAttribute(AUPageObjects.SECONDARY_JOINT_ACCOUNT,
                            AUPageObjects.VALUE)
                    authWebDriver.clickButtonXpath(AUPageObjects.SECONDARY_JOINT_ACCOUNT)

                    //Click Submit/Next Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_SUBMIT_XPATH)

                    //Click Confirm Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)
                }
                .execute()

        // Get Code From URL
        authorisationCode = AUTestUtil.getCodeFromJwtResponse(automation.currentUrl.get())

        //Get User Access Token
        generateUserAccessToken()

        //Account Retrieval
        Response accountResponse = doAccountRetrieval(userAccessToken)
        Assert.assertEquals(accountResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertNotNull(AUTestUtil.parseResponseBody(accountResponse,
                "${AUConstants.RESPONSE_DATA_BULK_ACCOUNTID_LIST}[0]"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(accountResponse,
                "${AUConstants.RESPONSE_DATA_BULK_ACCOUNTID_LIST}[1]"))
    }

    @Test
    void "CDS-628_Verify an account owner of the secondary account has restricted a particular secondary user from sharing accounts"() {

        //Inactive secondary user instruction permissions for joint account
        shareableElements = AUTestUtil.getSecondaryUserDetails(getSharableBankAccounts(), true)
        String accountID =  shareableElements[AUConstants.PARAM_ACCOUNT_ID]
        String userId = auConfiguration.getUserPSUName()

        def updateResponse = updateSecondaryUserInstructionPermission(accountID, userId, AUConstants.INACTIVE)
        Assert.assertEquals(updateResponse.statusCode(), AUConstants.OK)

        //Consent Authorisation
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), clientId)
                .toURI().toString()

        //User unable to select the secondary Account
        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Select Secondary Accounts - Individual and Joint Accounts
                    Assert.assertFalse(authWebDriver.isElementPresent(AUPageObjects.SECONDARY_ACCOUNT_1))
                }
                .execute()
    }

    @Test
    void "CDS-629_Verify an account owner of the secondary joint account has restricted a particular secondary user from sharing accounts"() {

        //Inactive secondary user instruction permissions for joint account
        shareableElements = AUTestUtil.getSecondaryUserDetails(getSharableBankAccounts(), false)
        String accountID =  shareableElements[AUConstants.PARAM_ACCOUNT_ID]
        String userId = auConfiguration.getUserPSUName()

        def updateResponse = updateSecondaryUserInstructionPermission(accountID, userId, AUConstants.INACTIVE)
        Assert.assertEquals(updateResponse.statusCode(), AUConstants.OK)

        //Consent Authorisation
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), clientId)
                .toURI().toString()

        //User unable to select the secondary Account
        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Select Secondary Accounts - Individual and Joint Accounts
                    Assert.assertFalse(authWebDriver.isElementPresent(AUPageObjects.SECONDARY_JOINT_ACCOUNT))
                }
                .execute()
    }

    @Test
    void "CDS-438_Verify notification to indicate the reason for pausing the data sharing from that account"() {

        //Send Authorisation Request via PAR
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), clientId)
                .toURI().toString()

        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Verify Notification to indicate the reason for pausing the data sharing
                    Assert.assertTrue(authWebDriver.getAttributeText(AUPageObjects.LBL_WHY_CANT_SHARE)
                            .contains("Why can't I share these? "))
                    Assert.assertNotNull(authWebDriver.getAttributeText(AUPageObjects.POPUP_UNAVAILABLE_ACCOUNTS))
                }
                .execute()
    }
}

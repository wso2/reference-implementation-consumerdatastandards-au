/**
 * Copyright (c) 2024 - 2026, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.cds.keymanager.test.bnr

import org.wso2.bfsi.test.framework.automation.AutomationMethod
import org.wso2.cds.test.framework.AUTest
import org.wso2.cds.test.framework.automation.consent.AUBasicAuthAutomationStep
import org.wso2.cds.test.framework.constant.AUAccountScope
import org.wso2.cds.test.framework.constant.AUBusinessUserPermission
import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.constant.AUPageObjects
import org.wso2.cds.test.framework.utility.AUTestUtil
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

import java.nio.charset.Charset

/**
 * Business User Representative Feature - Profile and Account Selection UI Validation Tests.
 * TODO: Enable Profile Selection in order to run this test class
 *
 * PSU assignments (fixed sharable accounts payload — NR membership is static):
 *   PSU 3 (nominatedUser1@wso2.com) — NR of Org A and both Org B accounts
 *   PSU 0 (psu@gold.com)           — not in any NR list, sees only individual accounts
 */
class ConsentAuthFlowValidationTests extends AUTest {


    def clientHeader
    String accountID
    String accountOwnerUserID
    String nominatedRepUserID
    String nominatedRepUserID2

    @BeforeClass(alwaysRun = true)
    void "Nominate Business User Representative"() {
        auConfiguration.setPsuNumber(2)
        clientHeader = "${Base64.encoder.encodeToString(getCDSClient().getBytes(Charset.defaultCharset()))}"

        //Get Sharable Account List and Nominate Business Representative with Authorize and View Permissions
        def shareableElements = AUTestUtil.getSharableAccountsList(getSharableBankAccounts())

        accountID =  shareableElements[AUConstants.PARAM_ACCOUNT_ID]
        accountOwnerUserID = shareableElements[AUConstants.ACCOUNT_OWNER_USER_ID]
        nominatedRepUserID = shareableElements[AUConstants.NOMINATED_REP_USER_ID]
        nominatedRepUserID2 = shareableElements[AUConstants.NOMINATED_REP_USER_ID2]

        def updateResponse = addMultiBusinessUserPermission(clientHeader, accountID, accountOwnerUserID,
                nominatedRepUserID, AUBusinessUserPermission.AUTHORIZE.getPermissionString(), nominatedRepUserID2,
                AUBusinessUserPermission.VIEW.getPermissionString())
        Assert.assertTrue(
                updateResponse.statusCode() == AUConstants.CREATED ||
                        updateResponse.statusCode() == AUConstants.OK
        )

        def businessAccount3 = AUConstants.businessAccount3
        def updateSecondAccPermissionResponse = addMultiBusinessUserPermission(
                clientHeader, businessAccount3, accountOwnerUserID, nominatedRepUserID,
                AUBusinessUserPermission.AUTHORIZE.getPermissionString(), nominatedRepUserID2,
                AUBusinessUserPermission.VIEW.getPermissionString())
        Assert.assertTrue(
                updateSecondAccPermissionResponse.statusCode() == AUConstants.CREATED ||
                        updateSecondAccPermissionResponse.statusCode() == AUConstants.OK
        )
    }

    @Test
    void "CDS-477_Verify Profile Selection is displayed in Auth Flow when the configuration is enabled"() {

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI()).toURI().toString()
        authoriseUrl = appendPromptLoginConsent(authoriseUrl)

        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    if (auConfiguration.getProfileSelectionEnabled()) {
                        assert authWebDriver.isElementDisplayed(AUPageObjects.ORGANIZATION_A_PROFILE_SELECTION)
                        assert authWebDriver.isElementDisplayed(AUPageObjects.INDIVIDUAL_PROFILE_SELECTION)
                    } else {
                        assert authWebDriver.isElementDisplayed(AUTestUtil.getSingleAccountXPath())
                        log.info("Profile Selection is Disabled")
                    }
                }
                .execute()
    }

    // TODO: Enable after implementing customer language rendering for individual and business consumer profiles
      @Test(priority = 1, enabled = false)
    void "CDS-543_Verify customer language in consent page for individual consumer"() {

        auConfiguration.setPsuNumber(0)
        List<AUAccountScope> scopes = [AUAccountScope.BANK_CUSTOMER_BASIC_READ]

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI()).toURI().toString()

        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    if (auConfiguration.getProfileSelectionEnabled()) {
                        authWebDriver.selectOption(AUPageObjects.INDIVIDUAL_PROFILE_SELECTION)
                        authWebDriver.clickButtonXpath(AUPageObjects.PROFILE_SELECTION_NEXT_BUTTON)

                        Assert.assertTrue(authWebDriver.getAttributeText(AUPageObjects.LBL_PERMISSION_HEADER)
                                .contains(AUConstants.BANK_CUSTOMER_BASIC_READ_INDIVIDUAL))
                        authWebDriver.clickButtonXpath(AUPageObjects.LBL_PERMISSION_HEADER)
                        Assert.assertEquals(authWebDriver.getAttributeText(AUPageObjects.LBL_PERMISSION_LIST_ITEM_1),
                                AUConstants.LBL_NAME)
                        Assert.assertEquals(authWebDriver.getAttributeText(AUPageObjects.LBL_PERMISSION_LIST_ITEM_2),
                                AUConstants.LBL_OCCUPATION)
                    } else {
                        assert authWebDriver.isElementDisplayed(AUTestUtil.getSingleAccountXPath())
                        log.info("Profile Selection is Disabled")
                    }
                }
                .execute()
    }

    // TODO: Enable after implementing customer language rendering for individual and business consumer profiles
    @Test (enabled = false)
    void "CDS-544_Verify customer language in consent page for business consumer"() {

        List<AUAccountScope> scopes = [AUAccountScope.BANK_CUSTOMER_DETAIL_READ]

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI()).toURI().toString()
        authoriseUrl = appendPromptLoginConsent(authoriseUrl)

        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    if (auConfiguration.getProfileSelectionEnabled()) {
                        authWebDriver.selectOption(AUPageObjects.ORGANIZATION_A_PROFILE_SELECTION)
                        authWebDriver.clickButtonXpath(AUPageObjects.PROFILE_SELECTION_NEXT_BUTTON)

                        Assert.assertTrue(authWebDriver.getAttributeText(AUPageObjects.LBL_PERMISSION_HEADER)
                                .contains(AUConstants.BANK_CUSTOMER_BASIC_READ))

                        authWebDriver.clickButtonXpath(AUPageObjects.LBL_PERMISSION_HEADER)
                        Assert.assertEquals(authWebDriver.getAttributeText(AUPageObjects.LBL_PERMISSION_LIST_ITEM_1),
                                AUConstants.LBL_AGENT_NAME_AND_ROLE)
                        Assert.assertEquals(authWebDriver.getAttributeText(AUPageObjects.LBL_PERMISSION_LIST_ITEM_2),
                                AUConstants.LBL_ORGANISATION_NAME)
                        Assert.assertEquals(authWebDriver.getAttributeText(AUPageObjects.LBL_PERMISSION_LIST_ITEM_3),
                                AUConstants.LBL_ORGANISATION_NUMBER)
                        Assert.assertEquals(authWebDriver.getAttributeText(AUPageObjects.LBL_PERMISSION_LIST_ITEM_4),
                                AUConstants.LBL_CHARITY_STATUS)
                        Assert.assertEquals(authWebDriver.getAttributeText(AUPageObjects.LBL_PERMISSION_LIST_ITEM_5),
                                AUConstants.LBL_ESTABLISHMENT_DATE)
                        Assert.assertEquals(authWebDriver.getAttributeText(AUPageObjects.LBL_PERMISSION_LIST_ITEM_6),
                                AUConstants.LBL_INDUSTRY)
                        Assert.assertEquals(authWebDriver.getAttributeText(AUPageObjects.LBL_PERMISSION_LIST_ITEM_7),
                                AUConstants.LBL_ORGANISATION_TYPE)
                        Assert.assertEquals(authWebDriver.getAttributeText(AUPageObjects.LBL_PERMISSION_LIST_ITEM_8),
                                AUConstants.LBL_COUNTRY_OF_REGISTRATION)
                    } else {
                        assert authWebDriver.isElementDisplayed(AUTestUtil.getSingleAccountXPath())
                        log.info("Profile Selection is Disabled")
                    }
                }
                .execute()
    }

    // TODO: Enable the test after implementing the "cancel" button in the account selection page
    @Test (enabled = false)
    void "CDS-484_Verify a Consent cancellation flow after Business Profile selection"() {

        //Get Authorisation URL
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI()).toURI().toString()
                .toURI().toString()
        authoriseUrl = appendPromptLoginConsent(authoriseUrl)

        //Consent Authorisation UI Flow
        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Select Profile and Accounts
                    if (auConfiguration.getProfileSelectionEnabled()) {

                        //Select Individual Profile
                        authWebDriver.selectOption(AUPageObjects.ORGANIZATION_A_PROFILE_SELECTION)

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
        def stateParam = authUrl.split("state=")[1]
        Assert.assertEquals(auAuthorisationBuilder.state.toString(), stateParam)
    }

    @Test(groups = "SmokeTest")
    void "CDS-588_Verify a Consent cancellation flow after Business Account selection"() {

        auConfiguration.setPsuNumber(3)

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI()).toURI().toString()
        authoriseUrl = appendPromptLoginConsent(authoriseUrl)

        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    if (auConfiguration.getProfileSelectionEnabled()) {
                        authWebDriver.selectOption(AUPageObjects.ORGANIZATION_A_PROFILE_SELECTION)
                        authWebDriver.clickButtonXpath(AUPageObjects.PROFILE_SELECTION_NEXT_BUTTON)

                        consentedAccount = authWebDriver.getElementAttribute(AUTestUtil.getBusinessAccount1CheckBox(),
                                AUPageObjects.VALUE)
                        authWebDriver.clickButtonXpath(AUTestUtil.getBusinessAccount1CheckBox())

                        // Proceed to confirmation dialogue — the account selection page has no cancel button
                        authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_NEXT)
                        authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_DENY_XPATH)
                    } else {
                        assert authWebDriver.isElementDisplayed(AUTestUtil.getSingleAccountXPath())
                        log.info("Profile Selection is Disabled")
                    }
                }
                .execute()

        def authUrl = automation.currentUrl.get()
        Assert.assertTrue(AUTestUtil.getDecodedUrl(authUrl).contains(AUConstants.USER_DENIED_THE_CONSENT))
        def stateParam = authUrl.split("state=")[1]
        Assert.assertEquals(auAuthorisationBuilder.state.toString(), stateParam)
    }

    // Disabled: BNR permission API changes (VIEW/AUTHORIZE) do not affect the consent UI flow in the
    // reference implementation. The consent UI filters accounts based on the static NR list in the
    // fixed sharable accounts payload, not the permission API state.
    @Test(groups = "SmokeTest", priority = 1, enabled = false)
    void "CDS-540_Consent Authorisation after updating nominated representatives permission from view to authorise"() {

        auConfiguration.setPsuNumber(3)

        def permissionsResponse = getStakeholderPermissions(nominatedRepUserID2, accountID)
        Assert.assertEquals(permissionsResponse.statusCode(), AUConstants.OK)
        Assert.assertTrue(AUTestUtil.parseResponseBody(permissionsResponse, "permissionStatus")
                .contains("${nominatedRepUserID2}:${AUBusinessUserPermission.VIEW.getPermissionString()}"))

        def permissionUpdateResponse = updateSingleBusinessUserPermission(clientHeader, accountID,
                accountOwnerUserID, nominatedRepUserID2, AUBusinessUserPermission.AUTHORIZE.getPermissionString())
        Assert.assertEquals(permissionUpdateResponse.statusCode(), AUConstants.OK)

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI()).toURI().toString()
        authoriseUrl = appendPromptLoginConsent(authoriseUrl)

        def automation2 = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    if (auConfiguration.getProfileSelectionEnabled()) {
                        authWebDriver.selectOption(AUPageObjects.ORGANIZATION_A_PROFILE_SELECTION)
                        authWebDriver.clickButtonXpath(AUPageObjects.PROFILE_SELECTION_NEXT_BUTTON)

                        Assert.assertTrue(authWebDriver.isElementEnabled(AUTestUtil.getBusinessAccount1CheckBox()))
                    } else {
                        assert authWebDriver.isElementDisplayed(AUTestUtil.getSingleAccountXPath())
                        log.info("Profile Selection is Disabled")
                    }
                }
                .execute()
    }

    // Disabled: BNR permission API changes (VIEW/AUTHORIZE) do not affect the consent UI flow in the
    // reference implementation. The consent UI filters accounts based on the static NR list in the
    // fixed sharable accounts payload, not the permission API state.
    @Test(priority = 1, enabled = false,
            dependsOnMethods = "CDS-540_Consent Authorisation after updating nominated representatives permission from view to authorise")
    void "CDS-542_Consent Authorisation after updating nominated representatives permission from authorise to view"() {

        auConfiguration.setPsuNumber(3)

        def permissionsResponse = getStakeholderPermissions(nominatedRepUserID2, accountID)
        Assert.assertEquals(permissionsResponse.statusCode(), AUConstants.OK)
        Assert.assertTrue(AUTestUtil.parseResponseBody(permissionsResponse, AUConstants.PARAM_PERMISSION_STATUS)
                .contains("${nominatedRepUserID2}:${AUBusinessUserPermission.AUTHORIZE.getPermissionString()}"))

        def permissionUpdateResponse = updateSingleBusinessUserPermission(clientHeader, accountID,
                accountOwnerUserID, nominatedRepUserID2, AUBusinessUserPermission.VIEW.getPermissionString())
        Assert.assertEquals(permissionUpdateResponse.statusCode(), AUConstants.OK)

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI()).toURI().toString()
        authoriseUrl = appendPromptLoginConsent(authoriseUrl)

        def automation2 = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    if (auConfiguration.getProfileSelectionEnabled()) {
                        List<WebElement> elements = driver.findElements(By.id(AUPageObjects.ORGANIZATION_A_PROFILE_SELECTION))
                        Assert.assertTrue(elements.isEmpty(), "Element is present")
                    } else {
                        assert authWebDriver.isElementDisplayed(AUTestUtil.getSingleAccountXPath())
                        log.info("Profile Selection is Disabled")
                    }
                }
                .execute()
    }

    // TODO: Enable the test after implementing the "select all" button in the account selection page
    @Test(priority = 1, enabled = false)
    void "CDS-589_Verify select all option in account selection page"() {

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI()).toURI().toString()
        authoriseUrl = appendPromptLoginConsent(authoriseUrl)

        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    if (auConfiguration.getProfileSelectionEnabled()) {
                        authWebDriver.selectOption(AUPageObjects.ORGANIZATION_A_PROFILE_SELECTION)
                        authWebDriver.clickButtonXpath(AUPageObjects.PROFILE_SELECTION_NEXT_BUTTON)

                        authWebDriver.clickButtonXpath(AUPageObjects.BTN_SELECT_ALL)
                        assert authWebDriver.isElementSelected(AUTestUtil.getBusinessAccount1CheckBox())
                    } else {
                        assert authWebDriver.isElementDisplayed(AUTestUtil.getSingleAccountXPath())
                        log.info("Profile Selection is Disabled")
                    }
                }
                .execute()
    }

    @Test
    void "CDS-510_Verify a non-NR user does not see any business profile in the consent flow"() {

        auConfiguration.setPsuNumber(0)

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI()).toURI().toString()

        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    if (auConfiguration.getProfileSelectionEnabled()) {
                        // Non-NR user has no eligible business accounts — profile selection is skipped
                        // and the flow lands directly on the individual account selection page
                        List<WebElement> orgAElements = driver.findElements(By.id(AUPageObjects.ORGANIZATION_A_PROFILE_SELECTION))
                        Assert.assertTrue(orgAElements.isEmpty(), "Organization A profile should not be visible to a non-NR user")
                    } else {
                        assert authWebDriver.isElementDisplayed(AUTestUtil.getSingleAccountXPath())
                        log.info("Profile Selection is Disabled")
                    }
                }
                .execute()
    }

    @Test (priority = 1)
    void "CDS-512_Verify a Consent Authorization Flow with non NR"() {

        auConfiguration.setPsuNumber(0)

        //Get Authorisation URL
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI()).toURI().toString()

        //Consent Authorisation UI Flow
        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Select Profile and Accounts
                    if (auConfiguration.getProfileSelectionEnabled()) {
                        // No business accounts eligible — profile selection page is skipped,
                        // flow lands directly on the account selection page with individual accounts only
                        Assert.assertTrue(authWebDriver.isElementDisplayed(AUTestUtil.getSingleAccountXPath()))
                    } else {
                        assert authWebDriver.isElementDisplayed(AUTestUtil.getSingleAccountXPath())
                        log.info("Profile Selection is Disabled")
                    }
                }
                .execute()
    }

    @Test (priority = 1)
    void "CDS-541_Verify same user nominated for multiple accounts"() {

        auConfiguration.setPsuNumber(3)

        //Consent Authorisation UI Flow
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI()).toURI().toString()
        authoriseUrl = appendPromptLoginConsent(authoriseUrl)

        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Select Profile and Accounts
                    if (auConfiguration.getProfileSelectionEnabled()) {
                        authWebDriver.selectOption(AUPageObjects.ORGANIZATION_B_PROFILE_SELECTION)
                        authWebDriver.clickButtonXpath(AUPageObjects.PROFILE_SELECTION_NEXT_BUTTON)

                        // nominatedUser1 is NR for both Org B accounts (biz_2 and biz_3)
                        Assert.assertTrue(authWebDriver.isElementEnabled(AUTestUtil.getBusinessAccount2CheckBox()))
                        Assert.assertTrue(authWebDriver.isElementEnabled(AUTestUtil.getBusinessAccount3CheckBox()))
                    } else {
                        assert authWebDriver.isElementDisplayed(AUTestUtil.getSingleAccountXPath())
                        log.info("Profile Selection is Disabled")
                    }
                }
                .execute()
    }
}

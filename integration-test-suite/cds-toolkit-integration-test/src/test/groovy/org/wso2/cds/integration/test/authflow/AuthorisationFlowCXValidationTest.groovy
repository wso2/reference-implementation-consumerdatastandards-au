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

package org.wso2.cds.integration.test.authflow

import org.wso2.bfsi.test.framework.automation.AutomationMethod
import org.wso2.cds.test.framework.AUTest
import org.wso2.cds.test.framework.automation.consent.AUBasicAuthAutomationStep
import org.wso2.cds.test.framework.constant.AUAccountProfile
import org.wso2.cds.test.framework.constant.AUAccountScope
import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.constant.AUPageObjects
import org.wso2.cds.test.framework.utility.AUTestUtil
import org.openqa.selenium.By
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test


/**
 * Authorisation Flow CX guidelines Tests.
 */
class AuthorisationFlowCXValidationTest extends AUTest {

    @BeforeClass
    void "init"() {
        clientId = auConfiguration.getAppInfoClientID()
    }

    @Test(groups = "SmokeTest")
    void "TC0203003_Verify the permissions of a consent with common customer basic read scope"() {

        List<AUAccountScope> scopes = [AUAccountScope.BANK_CUSTOMER_BASIC_READ]

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), clientId)
                .toURI().toString()

        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //If Profile Selection Enabled
                    if (auConfiguration.getProfileSelectionEnabled()) {

                        //Select Individual Profile
                        authWebDriver.selectOption(AUPageObjects.ORGANIZATION_B_PROFILE_SELECTION)
                        authWebDriver.clickButtonXpath(AUPageObjects.PROFILE_SELECTION_NEXT_BUTTON)
                    }

                    def lbl_permission_header = driver.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_HEADER_ORG_PROFILE))
                    Assert.assertTrue(lbl_permission_header.isDisplayed())
                    lbl_permission_header.click()
                    Assert.assertEquals(lbl_permission_header.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_LIST_ITEM_1)).getText(), AUConstants.LBL_AGENT_NAME_AND_ROLE)
                    Assert.assertEquals(lbl_permission_header.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_LIST_ITEM_2)).getText(), AUConstants.LBL_ORGANISATION_NAME)
                    Assert.assertEquals(lbl_permission_header.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_LIST_ITEM_3)).getText(), AUConstants.LBL_ORGANISATION_NUMBER)
                    Assert.assertEquals(lbl_permission_header.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_LIST_ITEM_4)).getText(), AUConstants.LBL_CHARITY_STATUS)
                    Assert.assertEquals(lbl_permission_header.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_LIST_ITEM_5)).getText(), AUConstants.LBL_ESTABLISHMENT_DATE)
                    Assert.assertEquals(lbl_permission_header.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_LIST_ITEM_6)).getText(), AUConstants.LBL_INDUSTRY)
                    Assert.assertEquals(lbl_permission_header.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_LIST_ITEM_7)).getText(), AUConstants.LBL_ORGANISATION_TYPE)
                    Assert.assertEquals(lbl_permission_header.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_LIST_ITEM_8)).getText(),
                            AUConstants.LBL_COUNTRY_OF_REGISTRATION)

                    //Click Authorise Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)
                }
        automation.execute()
    }

    @Test
    void "TC0203004_Verify the permissions of a consent with common customer detail read scope"() {

        List<AUAccountScope> scopes = [AUAccountScope.BANK_CUSTOMER_DETAIL_READ]

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), clientId)
                .toURI().toString()

        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //If Profile Selection Enabled
                    if (auConfiguration.getProfileSelectionEnabled()) {

                        //Select Individual Profile
                        authWebDriver.selectOption(AUPageObjects.ORGANIZATION_B_PROFILE_SELECTION)
                        authWebDriver.clickButtonXpath(AUPageObjects.PROFILE_SELECTION_NEXT_BUTTON)
                    }

                    def lbl_permission_header = driver.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_HEADER_ORG_PROFILE))
                    Assert.assertTrue(lbl_permission_header.isDisplayed())
                    lbl_permission_header.click()
                    Assert.assertEquals(lbl_permission_header.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_LIST_ITEM_1)).getText(), AUConstants.LBL_AGENT_NAME_AND_ROLE)
                    Assert.assertEquals(lbl_permission_header.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_LIST_ITEM_2)).getText(), AUConstants.LBL_ORGANISATION_NAME)
                    Assert.assertEquals(lbl_permission_header.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_LIST_ITEM_3)).getText(), AUConstants.LBL_ORGANISATION_NUMBER)
                    Assert.assertEquals(lbl_permission_header.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_LIST_ITEM_5)).getText(), AUConstants.LBL_ESTABLISHMENT_DATE)
                    Assert.assertEquals(lbl_permission_header.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_LIST_ITEM_6)).getText(), AUConstants.LBL_INDUSTRY)
                    Assert.assertEquals(lbl_permission_header.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_LIST_ITEM_7)).getText(), AUConstants.LBL_ORGANISATION_TYPE)
                    Assert.assertEquals(lbl_permission_header.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_LIST_ITEM_8)).getText(),
                            AUConstants.LBL_COUNTRY_OF_REGISTRATION)
                    Assert.assertEquals(lbl_permission_header.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_LIST_ITEM_9)).getText(), AUConstants.LBL_ORGANISATION_ADDRESS)
                    Assert.assertEquals(lbl_permission_header.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_LIST_ITEM_10)).getText(), AUConstants.LBL_MAIL_ADDRESS)
                    Assert.assertEquals(lbl_permission_header.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_LIST_ITEM_11)).getText(), AUConstants.LBL_PHONE_NUMBER)

                    //Click Authorise Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)
                }
        automation.execute()
    }

    @Test
    void "TC0203005_Verify the permissions of a consent with bank accounts basic read scope"() {

        List<AUAccountScope> scopes = [AUAccountScope.BANK_ACCOUNT_BASIC_READ]
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), clientId)
                .toURI().toString()

        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Select Profile and Accounts
                    selectProfileAndAccount(authWebDriver, AUAccountProfile.INDIVIDUAL, true)

                    //Click Confirm Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)

                    def lbl_permission_header = driver.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_HEADER_ACC_NAME))
                    Assert.assertTrue(lbl_permission_header.isDisplayed())
                    lbl_permission_header.click()
                    Assert.assertEquals(lbl_permission_header.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_LIST_ITEM_1)).getText(), AUConstants.LBL_NAME_OF_ACCOUNT)
                    Assert.assertEquals(lbl_permission_header.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_LIST_ITEM_2)).getText(), AUConstants.LBL_TYPE_OF_ACCOUNT)
                    Assert.assertEquals(lbl_permission_header.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_LIST_ITEM_3)).getText(), AUConstants.LBL_ACCOUNT_BALANCE)

                    //Click Authorise Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)
                }
        automation.execute()
    }

    @Test
    void "TC0203006_Verify the permissions of a consent with bank accounts detail read scope"() {

        List<AUAccountScope> scopes = [AUAccountScope.BANK_ACCOUNT_DETAIL_READ]
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), clientId)
                .toURI().toString()

        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Select Profile and Accounts
                    selectProfileAndAccount(authWebDriver, AUAccountProfile.INDIVIDUAL, true)

                    //Click Confirm Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)

                    def lbl_permission_header = driver.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_HEADER_ACC_BAL))
                    Assert.assertTrue(lbl_permission_header.isDisplayed())
                    lbl_permission_header.click()
                    Assert.assertEquals(lbl_permission_header.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_LIST_ITEM_1)).getText(), AUConstants.LBL_NAME_OF_ACCOUNT)
                    Assert.assertEquals(lbl_permission_header.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_LIST_ITEM_2)).getText(), AUConstants.LBL_TYPE_OF_ACCOUNT)
                    Assert.assertEquals(lbl_permission_header.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_LIST_ITEM_3)).getText(), AUConstants.LBL_ACCOUNT_BALANCE)
                    Assert.assertEquals(lbl_permission_header.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_LIST_ITEM_4)).getText(), AUConstants.LBL_ACCOUNT_NUMBER)
                    Assert.assertEquals(lbl_permission_header.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_LIST_ITEM_5)).getText(), AUConstants.LBL_INTEREST_RATES)
                    Assert.assertEquals(lbl_permission_header.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_LIST_ITEM_6)).getText(), AUConstants.LBL_FEES)
                    Assert.assertEquals(lbl_permission_header.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_LIST_ITEM_7)).getText(), AUConstants.LBL_DISCOUNTS)
                    Assert.assertEquals(lbl_permission_header.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_LIST_ITEM_8)).getText(), AUConstants.LBL_ACCOUNT_TERMS)
                    Assert.assertEquals(lbl_permission_header.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_LIST_ITEM_9)).getText(), AUConstants.LBL_ACCOUNT_MAIL_ADDRESS)

                    //Click Authorise Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)
                }
        automation.execute()
    }

    @Test
    void "TC0203007_Verify the permissions of a consent with bank transactions read scope"() {

        List<AUAccountScope> scopes = [AUAccountScope.BANK_TRANSACTION_READ]
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), clientId)
                .toURI().toString()

        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Select Profile and Accounts
                    selectProfileAndAccount(authWebDriver, AUAccountProfile.INDIVIDUAL, true)

                    //Click Confirm Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)

                    def lbl_permission_header = driver.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_HEADER_TRA_DETAILS))
                    Assert.assertTrue(lbl_permission_header.isDisplayed())
                    lbl_permission_header.click()
                    Assert.assertEquals(lbl_permission_header.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_LIST_ITEM_1)).getText(),
                            AUConstants.LBL_INCOMING_AND_OUTGOING_TRANSACTIONS)
                    Assert.assertEquals(lbl_permission_header.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_LIST_ITEM_2)).getText(),
                            AUConstants.LBL_AMOUNTS)
                    Assert.assertEquals(lbl_permission_header.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_LIST_ITEM_3)).getText(), AUConstants.LBL_DATES)
                    Assert.assertEquals(lbl_permission_header.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_LIST_ITEM_4)).getText(),
                            AUConstants.LBL_DESCRIPTION_OF_TRANSACTION)
                    Assert.assertEquals(lbl_permission_header.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_LIST_ITEM_5)).getText(),
                            AUConstants.LBL_NAME_OF_MONEY_RECIPIENT)

                    //Click Authorise Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)
                }
        automation.execute()
    }


    @Test
    void "TC0203008_Verify the permissions of a consent with bank regular_payments read scope"() {

        List<AUAccountScope> scopes = [AUAccountScope.BANK_REGULAR_PAYMENTS_READ]
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), clientId)
                .toURI().toString()

        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Select Profile and Accounts
                    selectProfileAndAccount(authWebDriver, AUAccountProfile.INDIVIDUAL, true)

                    //Click Confirm Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)

                    def lbl_permission_header = driver.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_HEADER_PAYMENT_READ))
                    Assert.assertTrue(lbl_permission_header.isDisplayed())
                    lbl_permission_header.click()
                    Assert.assertEquals(lbl_permission_header.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_LIST_ITEM_1)).getText(), AUConstants.LBL_DIRECT_DEBITS)
                    Assert.assertEquals(lbl_permission_header.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_LIST_ITEM_2)).getText(), AUConstants.LBL_SCHEDULE_PAYMENTS)

                    //Click Authorise Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)
                }
        automation.execute()
    }

    @Test
    void "TC0203009_Verify the permissions of a consent with bank payees read scope"() {

        List<AUAccountScope> scopes = [AUAccountScope.BANK_PAYEES_READ]
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), clientId)
                .toURI().toString()

        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //If Profile Selection Enabled
                    if (auConfiguration.getProfileSelectionEnabled()) {

                        //Select Individual Profile
                        authWebDriver.selectOption(AUPageObjects.INDIVIDUAL_PROFILE_SELECTION)
                        authWebDriver.clickButtonXpath(AUPageObjects.PROFILE_SELECTION_NEXT_BUTTON)
                    }

                    def lbl_permission_header = driver.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_HEADER_PAYEES))
                    Assert.assertTrue(lbl_permission_header.isDisplayed())
                    lbl_permission_header.click()
                    Assert.assertEquals(lbl_permission_header.findElement(By.xpath(
                            AUPageObjects.LBL_PERMISSION_LIST_ITEM_1)).getText(),
                            AUConstants.LBL_DETAILS_OF_SAVED_ACCOUNTS)

                    //Click Authorise Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)
                }
        automation.execute()
    }

}

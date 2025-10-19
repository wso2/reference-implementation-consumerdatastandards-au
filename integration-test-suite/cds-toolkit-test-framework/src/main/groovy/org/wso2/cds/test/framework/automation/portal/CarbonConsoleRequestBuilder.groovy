/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.cds.test.framework.automation.portal

import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.wso2.bfsi.test.framework.automation.BrowserAutomation
import org.wso2.bfsi.test.framework.automation.BrowserAutomationStep
import org.wso2.cds.test.framework.configuration.AUConfigurationService
import org.wso2.cds.test.framework.constant.AUPageObjects

class CarbonConsoleRequestBuilder implements BrowserAutomationStep {

    WebDriverWait wait
    private String url
    private AUConfigurationService auConfiguration
    static AUConfigurationService configurationService = new AUConfigurationService()

    /**
     * Initialize Carbon Console Flow.
     *
     * @param url URL.
     */
    CarbonConsoleRequestBuilder(String url) {

        this.url = url
        this.auConfiguration = new AUConfigurationService()
    }

    /**
     * Log into Api Console.
     * @param webDriver object of RemoteWebDriver
     */
    void carbonConsoleLogin(RemoteWebDriver webDriver) {

        webDriver.get(url)

        WebElement username = webDriver.findElement(By.xpath(AUPageObjects.AU_IS_USERNAME_ID))
        username.clear()
        username.sendKeys(configurationService.getUserKeyManagerAdminName())

        WebElement password = webDriver.findElement(By.xpath(AUPageObjects.AU_IS_PASSWORD_ID))
        password.clear()
        password.sendKeys(configurationService.getUserKeyManagerAdminPWD())

        wait = new WebDriverWait(webDriver, 600)
        wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath(AUPageObjects.AU_BTN_IS_SIGNING)))
        webDriver.findElement(By.xpath(AUPageObjects.AU_BTN_IS_SIGNING)).click()
        webDriver(webDriver, 20)
    }

    /**
     * Add Resident IDP with SMS OTP Authenticator
     * @param webDriver
     */
    void addResidentIdp(RemoteWebDriver webDriver){

        //Click Add Identity Provider
        wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath(AUPageObjects.BTN_ADD_IDP)))
        webDriver.findElement(By.xpath(AUPageObjects.BTN_ADD_IDP)).click()

        //Enter IDP Name
        WebElement txtIdpName = webDriver.findElement(By.xpath(AUPageObjects.TXT_IDP_NAME))
        txtIdpName.clear()
        txtIdpName.sendKeys("SMSAuthentication")

        //Select SMS OTP Configuration
        webDriver.findElement(By.xpath(AUPageObjects.TAB_FEDERATED_AUTHENTICATOR)).click()
        wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath(AUPageObjects.TAB_SMS_OTP)))
        webDriver.findElement(By.xpath(AUPageObjects.TAB_SMS_OTP)).click()

        //Configure SMS OTP
        webDriver.findElement(By.xpath(AUPageObjects.CHK_ENABLE_SMS_OTP)).click()

        WebElement txtSmsUrl = webDriver.findElement(By.xpath(AUPageObjects.TXT_SMS_URL))
        txtSmsUrl.clear()
        txtSmsUrl.sendKeys("SMSAuthentication")

        WebElement txtHttpMethod = webDriver.findElement(By.xpath(AUPageObjects.TXT_HTTP_METHOD))
        txtHttpMethod.clear()
        txtHttpMethod.sendKeys("SMSAuthentication")

        WebElement txtHttpHeader = webDriver.findElement(By.xpath(AUPageObjects.TXT_HTTP_HEADER))
        txtHttpHeader.clear()
        txtHttpHeader.sendKeys("SMSAuthentication")

        WebElement txtHttpPayload = webDriver.findElement(By.xpath(AUPageObjects.TXT_HTTP_PAYLOAD))
        txtHttpPayload.clear()
        txtHttpPayload.sendKeys("SMSAuthentication")

        //Click Register button
        webDriver.findElement(By.xpath(AUPageObjects.BTN_IDP_REGISTER)).click()
        webDriver(webDriver, 20)
    }

    /**
     * Enable Mobile Claim in Resident IDP
     * @param webDriver
     */
    void enableMobileClaim(RemoteWebDriver webDriver){

        //Click on Claims
        wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath(AUPageObjects.TAB_CLAIMS_LIST)))
        webDriver.findElement(By.xpath(AUPageObjects.TAB_CLAIMS_LIST)).click()

        //Click on org Claim
        wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath(AUPageObjects.LNK_ORG_CLAIMS)))
        webDriver.findElement(By.xpath(AUPageObjects.LNK_ORG_CLAIMS)).click()

        //Click on Edit button in Mobile Tab
        wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath(AUPageObjects.BTN_MOBILE_CLAIM_EDIT)))
        webDriver.findElement(By.xpath(AUPageObjects.BTN_MOBILE_CLAIM_EDIT)).click()

        //Enable Required Check Box
        webDriver.findElement(By.xpath(AUPageObjects.CHK_REQUIRED)).click()

        //Click Update button
        webDriver.findElement(By.xpath(AUPageObjects.BTN_UPDATE)).click()
        webDriver(webDriver, 20)
    }

    /**
     * Enable Account Lock in Resident IDP
     * @param webDriver
     */
    void enableAccountLock(RemoteWebDriver webDriver){

        //Click on Resident
        wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath(AUPageObjects.BTN_RESIDENT)))
        webDriver.findElement(By.xpath(AUPageObjects.BTN_RESIDENT)).click()

        //Click on Login Attempts Security
        wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath(AUPageObjects.TAB_LOGIN_SECURITY)))
        webDriver.findElement(By.xpath(AUPageObjects.TAB_LOGIN_SECURITY)).click()

        //Click on Account Lock
        wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath(AUPageObjects.TAB_ACCOUNT_LOCK)))
        webDriver.findElement(By.xpath(AUPageObjects.TAB_ACCOUNT_LOCK)).click()

        //Enable Account Lock
        webDriver.findElement(By.xpath(AUPageObjects.CHK_ENABLE_ACCOUNT_LOCK)).click()

        //Click Update button
        webDriver.findElement(By.xpath(AUPageObjects.BTN_UPDATE)).click()
        webDriver(webDriver, 20)
    }

    @Override
    void execute(RemoteWebDriver remoteWebDriver, BrowserAutomation.AutomationContext automationContext) {

        carbonConsoleLogin(remoteWebDriver)
        addResidentIdp(remoteWebDriver)
        enableMobileClaim(remoteWebDriver)
        enableAccountLock(remoteWebDriver)
    }
}

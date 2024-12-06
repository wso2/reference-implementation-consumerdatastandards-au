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

package org.wso2.cds.test.framework.automation.consent

import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.constant.AUPageObjects
import org.wso2.openbanking.test.framework.automation.AutomationMethod
import org.wso2.openbanking.test.framework.automation.BrowserAutomationStep
import org.wso2.openbanking.test.framework.automation.OBBrowserAutomation
import org.wso2.cds.test.framework.configuration.AUConfigurationService
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.support.ui.WebDriverWait

/**
 *  AU authorization automation step
 */
class AUBasicAuthAutomationStep implements BrowserAutomationStep {

    private String authorizeUrl
    private AUConfigurationService auConfiguration
    private static final Log log = LogFactory.getLog(AUBasicAuthAutomationStep.class);

    /**
     * Initialize Basic Auth Flow.
     *
     * @param authorizeUrl authorise URL.
     */
     AUBasicAuthAutomationStep(String authorizeUrl) {
        this.authorizeUrl = authorizeUrl
        this.auConfiguration = new AUConfigurationService()
    }

    @Override
    void execute(RemoteWebDriver webDriver, OBBrowserAutomation.AutomationContext context) {
        WebDriverWait wait = new WebDriverWait(webDriver, 30)
        AutomationMethod driver = new AutomationMethod(webDriver)
        webDriver.navigate().to(authorizeUrl)
        //Enter User Name
        driver.executeTextField(AUPageObjects.AU_USERNAME_FIELD_ID, auConfiguration.getUserPSUName())

        //Click on SignIn Button
        driver.clickButtonXpath(AUPageObjects.AU_AUTH_SIGNIN_XPATH)
        driver.waitTimeRange(30)

        //Second Factor Authentication Step
        try{
            if (driver.isElementDisplayed(AUPageObjects.AU_BTN_AUTHENTICATE)) {
                driver.executeSMSOTP(AUPageObjects.AU_LBL_SMSOTP_AUTHENTICATOR, AUPageObjects.AU_TXT_OTP_CODE_ID,
                        AUConstants.AU_OTP_CODE)
                driver.clickButtonXpath(AUPageObjects.AU_BTN_AUTHENTICATE)
                driver.waitTimeRange(30)
            }
        } catch (NoSuchElementException e) {
            log.info("Second Factor Authentication Step is not required")
        }
    }
}


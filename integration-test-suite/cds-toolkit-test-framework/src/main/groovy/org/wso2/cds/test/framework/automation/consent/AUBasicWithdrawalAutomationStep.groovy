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

import org.wso2.bfsi.test.framework.automation.AutomationMethod
import org.wso2.bfsi.test.framework.automation.BrowserAutomation
import org.wso2.bfsi.test.framework.automation.BrowserAutomationStep
import org.wso2.cds.test.framework.constant.AUPageObjects
import org.wso2.cds.test.framework.configuration.AUConfigurationService
import org.openqa.selenium.remote.RemoteWebDriver

import java.util.concurrent.TimeUnit

/**
 * AU Basic Withdrawal automation step
 */
class AUBasicWithdrawalAutomationStep implements BrowserAutomationStep {

    public String consentWithdrawalUrl
    private AUConfigurationService auConfiguration

    /**
     * Initialize Basic Withdrawal Automation Step.
     *
     * @param consentWithdrawalUrl consentmgt url
     */
    AUBasicWithdrawalAutomationStep(String consentWithdrawalUrl) {
        this.consentWithdrawalUrl = consentWithdrawalUrl
        this.auConfiguration = new AUConfigurationService()

    }

    /**
     * Execute automation using driver.
     *
     * @param webDriver driver object.
     * @param context automation context.
     */
    @Override
    void execute(RemoteWebDriver webDriver, BrowserAutomation.AutomationContext context) {
        AutomationMethod driver = new AutomationMethod(webDriver)
        webDriver.navigate().to(consentWithdrawalUrl)
        driver.executeTextField(AUPageObjects.AU_USERNAME_FIELD_ID, auConfiguration.getUserPSUName())
        driver.executeTextField(AUPageObjects.AU_PASSWORD_FIELD_ID, auConfiguration.getUserPSUPWD())
        driver.submitButtonXpath(AUPageObjects.AU_AUTH_SIGNIN_XPATH)
        webDriver.manage().timeouts().implicitlyWait(100, TimeUnit.SECONDS);

    }

}


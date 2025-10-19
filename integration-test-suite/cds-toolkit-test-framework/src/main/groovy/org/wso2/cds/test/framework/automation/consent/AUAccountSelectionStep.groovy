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
import org.wso2.cds.test.framework.utility.AUTestUtil

/**
 * AU Account section automation step
 */
class AUAccountSelectionStep implements BrowserAutomationStep {

    private AUConfigurationService auConfiguration
    /**
     * Initialize Basic Auth Flow.
     *
     * @param authorizeUrl authorise URL.
     */
    AUAccountSelectionStep() {
        this.auConfiguration = new AUConfigurationService()
    }

    @Override
    void execute(RemoteWebDriver webDriver, BrowserAutomation.AutomationContext context) {
        AutomationMethod driver = new AutomationMethod(webDriver)
        driver.clickButtonXpath(AUTestUtil.getSingleAccountXPath())
        driver.clickButtonXpath(AUPageObjects.CONSENT_SUBMIT_XPATH)
        driver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)
    }
}


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

package org.wso2.cds.preexecution

import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.cds.test.framework.configuration.AUConfigurationService
import org.wso2.cds.test.framework.request_builder.RESTKeyManagerRequestBuilder
import org.wso2.cds.test.framework.utility.AURestAsRequestBuilder

import java.util.logging.Logger

/**
 * Test class to contain steps to create OB Custom Key Manager.
 */
class CreateCustomKeyManagerTest {

    AUConfigurationService auConfiguration = new AUConfigurationService()

    Logger log = Logger.getLogger(CreateCustomKeyManagerTest.class.toString())
    String gatewayUrl = auConfiguration.getServerGatewayURL()
    String accessToken

    @BeforeClass (alwaysRun = true)
    void checkProvisioning() {
        AURestAsRequestBuilder.init()
    }

    /**
     * Steps to Create OB Key Manager.
     */
    @Test (groups = "SmokeTest")
    void "Create OB KeyManager"() {
        RESTKeyManagerRequestBuilder keyManagerGeneration = new RESTKeyManagerRequestBuilder()
        keyManagerGeneration.createDCRApplication()
        accessToken = keyManagerGeneration.obtainAccessToken()
        keyManagerGeneration.getResidentKeyManager(accessToken)
        keyManagerGeneration.disableResidentKeyManager(accessToken)
        //Adding sleep to fix an intermittent issue related to loading of key manager configurations.
        sleep(5000)
        keyManagerGeneration.addKeyManager(accessToken)
        sleep(5000)
    }
}

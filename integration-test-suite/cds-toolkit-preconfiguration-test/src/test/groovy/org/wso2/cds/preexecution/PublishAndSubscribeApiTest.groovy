/**
 * Copyright (c) 2024 - 2025, WSO2 LLC. (https://www.wso2.com).
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

import org.testng.SkipException
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.cds.test.framework.AUTest
import org.wso2.cds.test.framework.configuration.AUConfigurationService
import org.wso2.cds.test.framework.request_builder.RESTApiAccessTokenGeneration
import org.wso2.cds.test.framework.request_builder.RESTApiPublishRequestBuilder
import org.wso2.cds.test.framework.utility.AURestAsRequestBuilder

import java.util.logging.Logger

/**
 * Test class to contain Api Publishing and Subscribing scenarios
 */
class PublishAndSubscribeApiTest extends AUTest{

    AUConfigurationService auConfiguration = new AUConfigurationService()
    Logger log = Logger.getLogger(PublishAndSubscribeApiTest.class.toString())
    String gatewayUrl = auConfiguration.getServerGatewayURL()
    String accessToken
    List<String> apiIDs

    @BeforeClass (alwaysRun = true)
    void checkProvisioning() {
        AURestAsRequestBuilder.init()
        if (!auConfiguration.isProvisioning()) {
            throw new SkipException("Skipping API provisioning because the config set to false.")
        }
    }

    @Test (groups = "SmokeTest")
    void "Publish Api"() {
        RESTApiAccessTokenGeneration accessTokenGeneration = new RESTApiAccessTokenGeneration()
        accessTokenGeneration.createDCRApplication()
        accessToken = accessTokenGeneration.obtainAccessToken()

        RESTApiPublishRequestBuilder requestBuilder = new RESTApiPublishRequestBuilder()
        apiIDs = requestBuilder.createAPIs(accessToken)
        requestBuilder.addPolicy(accessToken, apiIDs)
        requestBuilder.createRevision(accessToken, apiIDs)
        requestBuilder.deployRevision(accessToken, apiIDs)
        requestBuilder.publishAPI(accessToken, apiIDs)

        sleep(2000)
    }
}

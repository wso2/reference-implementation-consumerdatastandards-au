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

package org.wso2.cds.integration.test.clientRegistration

import org.wso2.cds.test.framework.AUTest
import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.constant.ContextConstants
import org.wso2.cds.test.framework.request_builder.AURegistrationRequestBuilder
import org.wso2.cds.test.framework.utility.AUTestUtil
import org.testng.Assert
import org.testng.annotations.Test
import org.testng.ITestContext

/**
 * Testcases for DCR retrieve request validation.
 */
class DynamicClientRegistrationRetrieveTest extends AUTest{

    @SuppressWarnings('GroovyAccessibility')
    @Test (groups = "SmokeTest")
    void "TC0101018_Retrieve Application"(ITestContext context) {

        AURegistrationRequestBuilder registrationRequestBuilder = new AURegistrationRequestBuilder()

        deleteApplicationIfExists(auConfiguration.getAppInfoClientID())
        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(registrationRequestBuilder.getAURegularClaims())
                .when()
                .post( AUConstants.DCR_REGISTRATION_ENDPOINT)

        clientId = AUTestUtil.parseResponseBody(registrationResponse, "client_id")
        context.setAttribute(ContextConstants.CLIENT_ID,clientId)
        AUTestUtil.writeToConfigFile(clientId)
    }

    @Test(groups = "SmokeTest", priority = 1, dependsOnMethods = "TC0101018_Retrieve Application")
    void "TC0101009_Get access token"() {

        accessToken = getApplicationAccessToken(clientId)
        Assert.assertNotNull(accessToken)
    }

    @Test(priority = 2, dependsOnMethods = "TC0101009_Get access token")
    void "TC0102001_Get registration details with invalid client id"() {

        String invalidClientId = "invalidclientid"

        def registrationResponse = AURegistrationRequestBuilder.buildBasicRequest(accessToken)
                .when()
                .get(AUConstants.DCR_REGISTRATION_ENDPOINT + invalidClientId)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_401)
    }

    @Test(groups = "SmokeTest", priority = 2, dependsOnMethods = "TC0101009_Get access token")
    void "TC0102002_Get registration details"() {

        def registrationResponse = AURegistrationRequestBuilder.buildBasicRequest(accessToken)
                .when()
                .get(AUConstants.DCR_REGISTRATION_ENDPOINT + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_200)
    }
}

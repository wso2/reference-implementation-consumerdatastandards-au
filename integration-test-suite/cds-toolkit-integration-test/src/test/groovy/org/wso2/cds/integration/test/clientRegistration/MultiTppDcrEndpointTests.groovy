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
import org.wso2.cds.test.framework.configuration.AUConfigurationService
import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.constant.ContextConstants
import org.wso2.cds.test.framework.request_builder.AURegistrationRequestBuilder
import org.wso2.cds.test.framework.request_builder.AURequestBuilder
import org.wso2.cds.test.framework.utility.AUTestUtil
import org.testng.Assert
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

/**
 * Test class contains Multi TPP related DCR Tests.
 */
class MultiTppDcrEndpointTests extends AUTest {

    @BeforeClass(alwaysRun = true)
    void setup() {
        auConfiguration.setTppNumber(1)

        // Create Application for TPP2
        AURegistrationRequestBuilder dcr = new AURegistrationRequestBuilder()
        AUConfigurationService auConfiguration = new AUConfigurationService()

        def  registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(dcr.getAURegularClaims())
                .when()
                .post(AUConstants.DCR_REGISTRATION_ENDPOINT)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.CREATED)
        clientId = parseResponseBody(registrationResponse, "client_id")

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_201)
        AUTestUtil.writeToConfigFile(clientId)

        //Write Client Id of TPP2 to config file.
        AUTestUtil.writeToConfigFile(clientId)

        auConfiguration.setTppNumber(0)
        accessToken = AURequestBuilder.getApplicationAccessToken(getApplicationScope(), auConfiguration.getAppInfoClientID())
        Assert.assertNotNull(accessToken)
    }

    @Test
    void "OB-1308_Retrieve registration details with access token bound to a different client"() {

        def registrationResponse = AURegistrationRequestBuilder.buildBasicRequest(accessToken)
                .when()
                .get(AUConstants.DCR_REGISTRATION_ENDPOINT + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.UNAUTHORIZED)
        Assert.assertEquals(AUTestUtil.parseResponseBody(registrationResponse,AUConstants.ERROR),
                AUConstants.INVALID_CLIENT)
        Assert.assertEquals(AUTestUtil.parseResponseBody(registrationResponse,AUConstants.ERROR_DESCRIPTION),
                "Request failed due to unknown or invalid Client")
    }

    @Test
    void "OB-1309_Update Application with access token bound to a different client"() {

        AURegistrationRequestBuilder auRegistrationRequestBuilder = new AURegistrationRequestBuilder()
        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(auRegistrationRequestBuilder.getAURegularClaims())
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, "${AUConstants.AUTHORIZATION_BEARER_TAG}${accessToken}")
                .when()
                .put(AUConstants.DCR_REGISTRATION_ENDPOINT + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.UNAUTHORIZED)
        Assert.assertEquals(AUTestUtil.parseResponseBody(registrationResponse,AUConstants.ERROR),
                AUConstants.INVALID_CLIENT)
        Assert.assertEquals(AUTestUtil.parseResponseBody(registrationResponse,AUConstants.ERROR_DESCRIPTION),
                "Request failed due to unknown or invalid Client")
    }

    @Test
    void "OB-1310_Delete application with access token bound to a different client"() {

        def registrationResponse = AURegistrationRequestBuilder.buildBasicRequest(accessToken)
                .when()
                .delete(AUConstants.DCR_REGISTRATION_ENDPOINT + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.UNAUTHORIZED)
        Assert.assertEquals(AUTestUtil.parseResponseBody(registrationResponse,AUConstants.ERROR),
                AUConstants.INVALID_CLIENT)
        Assert.assertEquals(AUTestUtil.parseResponseBody(registrationResponse,AUConstants.ERROR_DESCRIPTION),
                "Request failed due to unknown or invalid Client")
    }

    @AfterClass(alwaysRun = true)
    void tearDown() {
        deleteApplicationIfExists(clientId)
    }
}

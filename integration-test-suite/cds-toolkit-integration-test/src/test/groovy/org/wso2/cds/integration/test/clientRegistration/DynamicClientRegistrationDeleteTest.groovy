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

import org.testng.annotations.AfterClass
import org.wso2.cds.test.framework.AUTest
import org.wso2.cds.test.framework.configuration.AUConfigurationService
import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.constant.ContextConstants
import org.wso2.cds.test.framework.data_provider.ConsentDataProviders
import org.wso2.cds.test.framework.request_builder.AURegistrationRequestBuilder
import org.wso2.cds.test.framework.utility.AUTestUtil
import org.testng.Assert
import org.testng.annotations.Test
import org.testng.ITestContext

/**
 * Test cases to validate DCR delete request.
 */
class DynamicClientRegistrationDeleteTest extends AUTest {

    private String invalidClientId = "invalidclientid"

    @Test(groups = "SmokeTest")
    void "TC0101009_Verify Get Application Access Token"(ITestContext context){

        auConfiguration.setTppNumber(1)
        // retrieve from context using key
        AURegistrationRequestBuilder dcr = new AURegistrationRequestBuilder()
        AUConfigurationService auConfiguration = new AUConfigurationService()
        deleteApplicationIfExists(auConfiguration.getAppInfoClientID())

        def  registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(dcr.getAURegularClaims())
                .when()
                .post(AUConstants.DCR_REGISTRATION_ENDPOINT)

        clientId = parseResponseBody(registrationResponse, "client_id")
        context.setAttribute(ContextConstants.CLIENT_ID,clientId)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_201)
        AUTestUtil.writeToConfigFile(clientId)

        accessToken = getApplicationAccessToken(clientId)
        Assert.assertNotNull(accessToken)
    }

    @Test(dependsOnMethods = "TC0101009_Verify Get Application Access Token")
    void "TC0104001_Delete application with invalid client id"() {

        auConfiguration.setTppNumber(1)
        def registrationResponse = AURegistrationRequestBuilder.buildBasicRequest(accessToken)
                .when()
                .delete(AUConstants.DCR_REGISTRATION_ENDPOINT + invalidClientId)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_401)
    }

    @Test(groups = "SmokeTest", dependsOnMethods = "TC0101009_Verify Get Application Access Token", priority = 2)
    void "TC0104002_Delete application"() {

        auConfiguration.setTppNumber(1)

        accessToken = getApplicationAccessToken(clientId)
        Assert.assertNotNull(accessToken)
        def registrationResponse = AURegistrationRequestBuilder.buildBasicRequest(accessToken)
                .when()
                .delete(AUConstants.DCR_REGISTRATION_ENDPOINT + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_204)
    }

    @Test(dependsOnMethods = "TC0101009_Verify Get Application Access Token", priority = 2, dataProvider = "httpMethods",
            dataProviderClass = ConsentDataProviders.class)
    void "CDS-707_Send DCR request with supported http methods"(httpMethod) {

        auConfiguration.setTppNumber(1)
        def registrationResponse = AURegistrationRequestBuilder.buildBasicRequest(accessToken)
                .when()
                .request(httpMethod.toString(), AUConstants.DCR_REGISTRATION_ENDPOINT + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_405)
    }

    @Test(dependsOnMethods = "TC0101009_Verify Get Application Access Token", priority = 2, dataProvider = "unsupportedHttpMethods",
            dataProviderClass = ConsentDataProviders.class)
    void "CDS-713_Send DCR request with unsupported http methods"(httpMethod) {

        auConfiguration.setTppNumber(1)
        def registrationResponse = AURegistrationRequestBuilder.buildBasicRequest(accessToken)
                .when()
                .request(httpMethod.toString(), AUConstants.DCR_REGISTRATION_ENDPOINT + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_501)
    }

    @AfterClass
    void deleteApplication(){
        auConfiguration.setTppNumber(1)
        deleteApplicationIfExists(clientId)
        Assert.assertEquals(deletionResponse.statusCode(), AUConstants.STATUS_CODE_204)
    }
}

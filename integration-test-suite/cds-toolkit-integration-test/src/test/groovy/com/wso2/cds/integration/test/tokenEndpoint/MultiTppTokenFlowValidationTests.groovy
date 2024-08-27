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

package com.wso2.cds.integration.test.tokenEndpoint

import com.wso2.cds.test.framework.AUTest
import com.wso2.cds.test.framework.constant.AUAccountScope
import com.wso2.cds.test.framework.constant.AUConstants
import com.wso2.cds.test.framework.request_builder.AURegistrationRequestBuilder
import com.wso2.cds.test.framework.request_builder.AURequestBuilder
import com.wso2.cds.test.framework.utility.AUMockCDRIntegrationUtil
import com.wso2.cds.test.framework.utility.AUTestUtil
import org.testng.Assert
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

/**
 * Test class contains Multi Tpp Token Flow Validation Tests.
 */
class MultiTppTokenFlowValidationTests extends AUTest {

    @BeforeClass(alwaysRun = true)
    void setup() {

        auConfiguration.setTppNumber(1)
        AURegistrationRequestBuilder dcr = new AURegistrationRequestBuilder()

        deleteApplicationIfExists()
        //Register Second TPP.
        def  registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(dcr.getAURegularClaims())
                .when()
                .post(AUConstants.DCR_REGISTRATION_ENDPOINT)

        clientId = parseResponseBody(registrationResponse, "client_id")

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_201)
        AUTestUtil.writeToConfigFile(clientId)

        doConsentAuthorisation(clientId)
    }

    @Test
    void "OB-1314_Get user access token from authorisation code bound to different Tpp" () {

        auConfiguration.setTppNumber(0)
        def userToken = AURequestBuilder.getUserTokenErrorResponse(authorisationCode,
                auConfiguration.getAppInfoRedirectURL(), auConfiguration.getAppInfoClientID())

        Assert.assertEquals(userToken.error.httpStatusCode, AUConstants.BAD_REQUEST)
        Assert.assertEquals(userToken.error.code, AUConstants.INVALID_GRANT)
        Assert.assertEquals(userToken.error.description, "Invalid authorization code received from token request")
    }

    @Test
    void "OB-1315_Get user access token with client_assertion does not bound to the requested client" () {

        auConfiguration.setTppNumber(0)
        doConsentAuthorisation(auConfiguration.getAppInfoClientID())
        def userToken = AURequestBuilder.getUserTokenErrorResponse(authorisationCode,
                auConfiguration.getAppInfoRedirectURL(), clientId)

        Assert.assertEquals(userToken.error.httpStatusCode, AUConstants.BAD_REQUEST)
        Assert.assertEquals(userToken.error.code, AUConstants.INVALID_GRANT)
        Assert.assertEquals(userToken.error.description, "Invalid authorization code received from token request")
    }

    @AfterClass(alwaysRun = true)
    void tearDown() {
        deleteApplicationIfExists(clientId)
    }
}

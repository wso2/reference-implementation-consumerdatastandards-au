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
import org.wso2.cds.test.framework.constant.AUAccountScope
import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.constant.ContextConstants
import org.wso2.cds.test.framework.request_builder.AUJWTGenerator
import org.wso2.cds.test.framework.request_builder.AURegistrationRequestBuilder
import org.wso2.cds.test.framework.request_builder.AURequestBuilder
import org.wso2.cds.test.framework.utility.AUTestUtil
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.testng.ITestContext

import java.nio.file.Path
import java.nio.file.Paths

/**
 * Testcases for DCR Update request validation.
 */
class DynamicClientRegistrationUpdateTest extends AUTest{

    private String registrationPath = AUConstants.DCR_REGISTRATION_ENDPOINT
    private String invalidClientId = "invalidclientid"
    private String softwareId = "SP1"
    private String commonSoftwareId, softwareStatement

    @BeforeClass(alwaysRun = true)
    void "Create new Application"(ITestContext context) {

        auConfiguration.setTppNumber(1)
        commonSoftwareId = auConfiguration.getAppDCRSoftwareId()
        softwareStatement = new File(auConfiguration.getAppDCRSSAPath()).text

        AURegistrationRequestBuilder registrationRequestBuilder = new AURegistrationRequestBuilder()

        deleteApplicationIfExists(auConfiguration.getAppInfoClientID())
        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(registrationRequestBuilder.getAURegularClaims(commonSoftwareId, softwareStatement))
                .when()
                .post( AUConstants.DCR_REGISTRATION_ENDPOINT)

        clientId = AUTestUtil.parseResponseBody(registrationResponse, "client_id")
        context.setAttribute(ContextConstants.CLIENT_ID,clientId)
        AUTestUtil.writeToConfigFile(clientId)

        accessToken = getApplicationAccessToken(clientId)
        Assert.assertNotNull(accessToken)
    }

    @Test
    void "TC0103001_Update registration details with invalid client id"() {

        auConfiguration.setTppNumber(1)
        AURegistrationRequestBuilder registrationRequestBuilder = new AURegistrationRequestBuilder()
        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(registrationRequestBuilder.getRegularClaimsWithNewRedirectUri())
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, "${AUConstants.AUTHORIZATION_BEARER_TAG}${accessToken}")
                .when()
                .put(registrationPath + invalidClientId)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_401)
    }

    @Test(groups = "SmokeTest")
    void "TC0103002_Update registration details"() {

        auConfiguration.setTppNumber(1)
        accessToken = getApplicationAccessToken(clientId)
        Assert.assertNotNull(accessToken)

        commonSoftwareId = auConfiguration.getAppDCRSoftwareId()
        softwareStatement = new File(auConfiguration.getAppDCRSSAPath()).text

        AUJWTGenerator aujwtGenerator =new AUJWTGenerator()
        AURegistrationRequestBuilder registrationRequestBuilder = new AURegistrationRequestBuilder()
        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(registrationRequestBuilder.getAURegularClaims(commonSoftwareId, softwareStatement))
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, "${AUConstants.AUTHORIZATION_BEARER_TAG}${accessToken}")
                .when()
                .put(registrationPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_200)
    }

    @Test
    void "OB-1167_Update registration details without SSA"() {

        auConfiguration.setTppNumber(1)
        AUJWTGenerator aujwtGenerator = new AUJWTGenerator()
        AURegistrationRequestBuilder registrationRequestBuilder = new AURegistrationRequestBuilder()
        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(registrationRequestBuilder.getClaimsWithoutSSA())
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, "${AUConstants.AUTHORIZATION_BEARER_TAG}${accessToken}")
                .when()
                .put(registrationPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR),
                AUConstants.INVALID_CLIENT_METADATA)
    }

    @Test
    void "OB-1168_Update registration details with fields not supported by data holder brand"() {

        auConfiguration.setTppNumber(1)
        AURegistrationRequestBuilder registrationRequestBuilder = new AURegistrationRequestBuilder()
        commonSoftwareId = auConfiguration.getAppDCRSoftwareId()
        softwareStatement = new File(auConfiguration.getAppDCRSSAPath()).text

        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(registrationRequestBuilder.getRegularClaimsWithFieldsNotSupported(commonSoftwareId, softwareStatement))
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, "${AUConstants.AUTHORIZATION_BEARER_TAG}${accessToken}")
                .when()
                .put(registrationPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_200)

        def retrievalResponse = AURegistrationRequestBuilder.buildBasicRequest(accessToken)
                .when()
                .get(registrationPath + clientId)

        //Unsupported claims will be ignored. Therefore, gives a success response.
        Assert.assertEquals(retrievalResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertNull(parseResponseBody(retrievalResponse, "adr_name"))
    }

    @Test(priority = 4)
    void "OB-1169_Update registration details with a access token bound only to CDR Authorization scopes"() {

        auConfiguration.setTppNumber(1)

        scopes = [
                AUAccountScope.BANK_ACCOUNT_BASIC_READ.getScopeString(),
                AUAccountScope.BANK_TRANSACTION_READ.getScopeString(),
                AUAccountScope.BANK_CUSTOMER_DETAIL_READ.getScopeString()
        ]

        accessToken = AURequestBuilder.getApplicationAccessToken(scopes, clientId)
        Assert.assertNotNull(accessToken)

        AUJWTGenerator aujwtGenerator = new AUJWTGenerator()
        AURegistrationRequestBuilder registrationRequestBuilder = new AURegistrationRequestBuilder()
        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(registrationRequestBuilder.getAURegularClaims(commonSoftwareId, softwareStatement))
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, "${AUConstants.AUTHORIZATION_BEARER_TAG}${accessToken}")
                .when()
                .put(registrationPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_403)
    }

    @Test(priority = 4)
    void "OB-1170_Update registration details without access token"() {

        auConfiguration.setTppNumber(1)
        AURegistrationRequestBuilder registrationRequestBuilder = new AURegistrationRequestBuilder()
        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(registrationRequestBuilder.getAURegularClaims())
                .when()
                .put(registrationPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_401)
    }

    @Test(priority = 4)
    void "OB-1171_Update registration details with invalid access token"() {

        auConfiguration.setTppNumber(1)
        accessToken = getApplicationAccessToken(clientId)
        Assert.assertNotNull(accessToken)

        commonSoftwareId = auConfiguration.getAppDCRSoftwareId()
        softwareStatement = new File(auConfiguration.getAppDCRSSAPath()).text

        deleteApplicationIfExists(clientId)

        AURegistrationRequestBuilder registrationRequestBuilder = new AURegistrationRequestBuilder()
        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(registrationRequestBuilder.getAURegularClaims(commonSoftwareId, softwareStatement))
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, "${AUConstants.AUTHORIZATION_BEARER_TAG}${accessToken}")
                .when()
                .put(registrationPath + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_401)
    }

    @Test
    void "CDS-22_Update Application with SSA containing redirect uri in localhost value"() {

        auConfiguration.setTppNumber(1)
        Path dcrArtifactsPath = Paths.get(auConfiguration.getAppDCRSSAPath())
        String filePath = Paths.get(dcrArtifactsPath.getParent().toString(), "ssa_localhost.txt")

        jtiVal = String.valueOf(System.currentTimeMillis())
        AURegistrationRequestBuilder registrationRequestBuilder = new AURegistrationRequestBuilder()
        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(registrationRequestBuilder
                        .getAURegularClaims(softwareId, AUTestUtil.readFileContent(filePath),
                                AUConstants.LOCALHOST_REDIRECT_URL))
                .when()
                .post(AUConstants.DCR_REGISTRATION_ENDPOINT)

        def appClientId = AUTestUtil.parseResponseBody(registrationResponse, AUConstants.CLIENT_ID)

        accessToken = getApplicationAccessToken(appClientId)
        Assert.assertNotNull(accessToken)

        AUJWTGenerator aujwtGenerator =new AUJWTGenerator()
        registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(registrationRequestBuilder
                        .getAURegularClaims(softwareId, AUTestUtil.readFileContent(filePath),
                                AUConstants.LOCALHOST_REDIRECT_URL))
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, "${AUConstants.AUTHORIZATION_BEARER_TAG}${accessToken}")
                .when()
                .put(registrationPath + appClientId)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(parseResponseBody(registrationResponse, "software_statement"),
                AUTestUtil.readFileContent(filePath))
        Assert.assertTrue(parseResponseBody(registrationResponse, "redirect_uris")
                .contains(AUConstants.LOCALHOST_REDIRECT_URL))

        deleteApplicationIfExists(appClientId)
    }

    @Test
    void "CDS-23_Update registration details with invalid http method"() {

        auConfiguration.setTppNumber(1)
        accessToken = getApplicationAccessToken(clientId)
        Assert.assertNotNull(accessToken)

        AUJWTGenerator aujwtGenerator =new AUJWTGenerator()
        AURegistrationRequestBuilder registrationRequestBuilder = new AURegistrationRequestBuilder()
        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(registrationRequestBuilder.getAURegularClaims())
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, "${AUConstants.AUTHORIZATION_BEARER_TAG}${accessToken}")
                .when()
                .request("COPY", AUConstants.DCR_REGISTRATION_ENDPOINT + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_501)
    }

    @AfterClass (alwaysRun = true)
    void deleteApplication(){
        auConfiguration.setTppNumber(1)
        deleteApplicationIfExists(clientId)
        Assert.assertEquals(deletionResponse.statusCode(), AUConstants.STATUS_CODE_204)
    }
}

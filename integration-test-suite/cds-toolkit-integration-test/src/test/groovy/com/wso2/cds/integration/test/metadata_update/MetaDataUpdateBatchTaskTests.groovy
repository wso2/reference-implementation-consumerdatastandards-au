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

package com.wso2.cds.integration.test.metadata_update

import com.nimbusds.oauth2.sdk.AccessTokenResponse
import com.wso2.cds.test.framework.AUTest
import com.wso2.cds.test.framework.constant.AUAccountScope
import com.wso2.cds.test.framework.constant.AUConstants
import com.wso2.cds.test.framework.request_builder.AURegistrationRequestBuilder
import com.wso2.cds.test.framework.utility.AUTestUtil
import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

import java.nio.charset.Charset

/**
 * Test Related to Meta Data Update - Batch Task
 */
class MetaDataUpdateBatchTaskTests extends AUTest{

    def clientHeader = "${Base64.encoder.encodeToString(getCDSClient().getBytes(Charset.defaultCharset()))}"

    @BeforeClass(alwaysRun = true)
    void setup() {
        auConfiguration.setTppNumber(1)

        //Register Second TPP.
        deleteApplicationIfExists(auConfiguration.getAppInfoClientID())
        def registrationResponse = tppRegistration()
        clientId = AUTestUtil.parseResponseBody(registrationResponse, "client_id")
        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.CREATED)

        //Write Client Id of TPP2 to config file.
        AUTestUtil.writeXMLContent(AUTestUtil.getTestConfigurationFilePath(), "Application",
                "ClientID", clientId, auConfiguration.getTppNumber())

        doConsentAuthorisation(clientId)

        auConfiguration.setTppNumber(0)
        accessToken = getApplicationAccessToken(clientId)
        Assert.assertNotNull(accessToken)
    }

    @Test(enabled = true)
    void "TC10030001_Verify the Consent Status when the SP Active and ADR Active"() {

        doConsentAuthorisation(clientId)

        // Retrieve the user access token by auth code
        AccessTokenResponse responseBody = getUserAccessTokenResponse(clientId)
        userAccessToken = responseBody.tokens.accessToken
        cdrArrangementId = responseBody.getCustomParameters().get(AUConstants.CDR_ARRANGEMENT_ID)
        Assert.assertNotNull(cdrArrangementId)

        //TODO: Change Status
        sleep(81000)

        Response response = getConsentStatus(clientHeader, cdrArrangementId)

        //Assert the consent status status code
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(response.jsonPath().get("List[0].Status"), "Authorised")
    }

    @Test(enabled = true)
    void "TC10030002_Verify the Consent Status when the ADR Active and SP removed"() {

        doConsentAuthorisation(clientId)

        // Retrieve the user access token by auth code
        AccessTokenResponse responseBody = getUserAccessTokenResponse(clientId)
        userAccessToken = responseBody.tokens.accessToken
        cdrArrangementId = responseBody.getCustomParameters().get(AUConstants.CDR_ARRANGEMENT_ID)
        Assert.assertNotNull(cdrArrangementId)

        //TODO: Change Status
        sleep(81000)

        Response response = getConsentStatus(clientHeader, cdrArrangementId)

        //Assert the consent status status code
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(response.jsonPath().get("List[0].Status"), "Authorised")
    }

    //Enable the test case when running the test case. Disabled due to the mock authenticator is not available now.
    @Test(enabled = false)
    void "TC10030003_Verify the Consent Status when the ADR Suspended and SP removed"() {

        doConsentAuthorisation(clientId)

        // Retrieve the user access token by auth code
        AccessTokenResponse responseBody = getUserAccessTokenResponse(clientId)
        userAccessToken = responseBody.tokens.accessToken
        cdrArrangementId = responseBody.getCustomParameters().get(AUConstants.CDR_ARRANGEMENT_ID)
        Assert.assertNotNull(cdrArrangementId)

        //TODO: Change Status
        sleep(81000)

        Response response = getConsentStatus(clientHeader, cdrArrangementId)

        //Assert the consent status status code
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(response.jsonPath().get("List[0].Status"), "Authorised")
    }

    //Enable the test case when running the test case. Disabled due to the mock authenticator is not available now.
    @Test(enabled = false)
    void "TC10030004_Verify the Consent Status when the ADR Surrendered and SP removed"() {

        doConsentAuthorisation(clientId)

        // Retrieve the user access token by auth code
        AccessTokenResponse responseBody = getUserAccessTokenResponse(clientId)
        userAccessToken = responseBody.tokens.accessToken
        cdrArrangementId = responseBody.getCustomParameters().get(AUConstants.CDR_ARRANGEMENT_ID)
        Assert.assertNotNull(cdrArrangementId)

        //TODO: Change Status
        sleep(81000)

        Response response = getConsentStatus(clientHeader, cdrArrangementId)

        //Assert the consent status status code
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(response.jsonPath().get("List[0].Status"), "Authorised")
    }

    //Enable the test case when running the test case. Disabled due to the mock authenticator is not available now.
    @Test(enabled = false)
    void "TC10030005_Verify the Consent Status when the ADR Revoked and SP removed"() {

        doConsentAuthorisation(clientId)

        // Retrieve the user access token by auth code
        AccessTokenResponse responseBody = getUserAccessTokenResponse(clientId)
        userAccessToken = responseBody.tokens.accessToken
        cdrArrangementId = responseBody.getCustomParameters().get(AUConstants.CDR_ARRANGEMENT_ID)
        Assert.assertNotNull(cdrArrangementId)

        //TODO: Change Status
        sleep(81000)

        Response response = getConsentStatus(clientHeader, cdrArrangementId)

        //Assert the consent status status code
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(response.jsonPath().get("List[0].Status"), "Authorised")
    }

    //Enable the test case when running the test case. Disabled due to the mock authenticator is not available now.
    @Test(enabled = false)
    void "TC10030006_Verify the Registration Status when the SP Active and ADR Active"() {

        accessToken = getApplicationAccessToken(clientId)

        //TODO: Change Status
        sleep(81000)

        def registrationResponse = AURegistrationRequestBuilder.buildBasicRequest(accessToken)
                .get(AUConstants.DCR_REGISTRATION_ENDPOINT + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_200)
    }

    //Enable the test case when running the test case. Disabled due to the mock authenticator is not available now.
    @Test(enabled = false)
    void "TC10030007_Verify the Registration Status when the ADR Suspended and SP Removed"() {

        accessToken = getApplicationAccessToken(clientId)

        //TODO: Change Status
        sleep(81000)

        def registrationResponse = AURegistrationRequestBuilder.buildBasicRequest(accessToken)
                .get(AUConstants.DCR_REGISTRATION_ENDPOINT + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_200)
    }

    //Enable the test case when running the test case. Disabled due to the mock authenticator is not available now.
    @Test(enabled = false)
    void "TC10030008_Verify the Registration Status when the ADR Revoked and SP Removed"() {

        accessToken = getApplicationAccessToken(clientId)

        //TODO: Change Status
        sleep(81000)

        def registrationResponse = AURegistrationRequestBuilder.buildBasicRequest(accessToken)
                .get(AUConstants.DCR_REGISTRATION_ENDPOINT + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_200)
    }

    //Enable the test case when running the test case. Disabled due to the mock authenticator is not available now.
    @Test(enabled = false)
    void "TC10030009_Verify the Registration Status when the ADR Surrendered and SP Removed"() {

        accessToken = getApplicationAccessToken(clientId)

        //TODO: Change Status
        sleep(81000)

        def registrationResponse = AURegistrationRequestBuilder.buildBasicRequest(accessToken)
                .get(AUConstants.DCR_REGISTRATION_ENDPOINT + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_200)
    }
}

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

package org.wso2.cds.integration.test.accounts

import org.wso2.cds.test.framework.constant.AUConstants
import com.nimbusds.oauth2.sdk.AccessTokenResponse
import org.wso2.cds.test.framework.AUTest
import org.wso2.cds.test.framework.configuration.AUConfigurationService
import org.wso2.cds.test.framework.request_builder.AURegistrationRequestBuilder
import org.wso2.openbanking.test.framework.automation.NavigationAutomationStep
import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.cds.test.framework.request_builder.AUAuthorisationBuilder
import org.wso2.cds.test.framework.request_builder.AURequestBuilder
import org.wso2.cds.test.framework.utility.AUTestUtil

/**
 * Class contains Multi Tpp Consent Validation Tests.
 */
class MultiTppConsentValidationTest extends AUTest {

    AUConfigurationService auConfiguration = new AUConfigurationService()
    String clientID

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

        clientID = parseResponseBody(registrationResponse, "client_id")

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_201)
        AUTestUtil.writeXMLContent(AUTestUtil.getTestConfigurationFilePath(), "Application",
                "ClientID", clientId, auConfiguration.getTppNumber())

        accessToken = getApplicationAccessToken(clientID)
        Assert.assertNotNull(accessToken)
    }

    @Test
    void "OB-1313_Revoke sharing arrangement bound to different Tpp"(){
        auConfiguration.setTppNumber(0)

        doConsentAuthorisation()
        Assert.assertNotNull(authorisationCode)

        //obtain cdr_arrangement_id from token response
        AccessTokenResponse userAccessTokenRes = getUserAccessTokenResponse()
        String cdrArrangementId = userAccessTokenRes.getCustomParameters().get("cdr_arrangement_id")
        Assert.assertNotNull(cdrArrangementId)

        //retrieve consumer data successfully
        Response response = AURequestBuilder
                .buildBasicRequest(userAccessTokenRes.tokens.accessToken.toString(), AUConstants.CDR_ENDPOINT_VERSION)
                .header(AUConstants.PARAM_FAPI_AUTH_DATE,AUConstants.VALUE_FAPI_AUTH_DATE)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get("${AUConstants.BULK_ACCOUNT_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        auConfiguration.setTppNumber(1)

        //Get application access token for TPP2
        setApplicationScope(["openid"])
        String secondAppAccessToken = getApplicationAccessToken(clientID)
        Assert.assertNotNull(secondAppAccessToken)

        auConfiguration.setTppNumber(0)

        AUAuthorisationBuilder authBuilder = new AUAuthorisationBuilder()
        //revoke sharing arrangement using token of TPP2 and cdrArrangementId of TPP1
        Response revocationResponse= authBuilder.doArrangementRevocationWithPkjwt(secondAppAccessToken,cdrArrangementId)
        Assert.assertEquals(revocationResponse.statusCode(), AUConstants.STATUS_CODE_404)

    }

    @Test
    void "OB-1311_Validate consent authorisation with request_uri bound to different tpp"() {
        auConfiguration.setTppNumber(0)

        //authorise sharing arrangement
        doConsentAuthorisation()
        Assert.assertNotNull(authorisationCode)

        //obtain cdr_arrangement_id from token response
        AccessTokenResponse userAccessTokenRes = getUserAccessTokenResponse()
        String cdrArrangementId = userAccessTokenRes.getCustomParameters().get("cdr_arrangement_id")
        Assert.assertNotNull(cdrArrangementId)

        //retrieve consumer data successfully
        Response response = doAccountRetrieval(userAccessTokenRes.tokens.accessToken.toString())
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //Send PAR request.
        def parResponse = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, cdrArrangementId)

        def requestUri = AUTestUtil.parseResponseBody(parResponse, AUConstants.REQUEST_URI)
        Assert.assertEquals(parResponse.statusCode(), AUConstants.STATUS_CODE_201)

        //Send consent authorisation using request_uri bound to TPP1 with client id of TPP2
        auConfiguration.setTppNumber(1)
        def authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), clientID).toURI().toString()

        def automationResponse = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new NavigationAutomationStep(authoriseUrl, 10))
                .execute()

        String url = automationResponse.currentUrl.get()
        String errorUrl

        errorUrl = url.split("oauthErrorCode=")[1].split("&")[0].replaceAll("\\+"," ")
        Assert.assertEquals(errorUrl, AUConstants.INVALID_REQUEST_URI)

        errorUrl = url.split("oauthErrorMsg=")[1].split("&")[0].replaceAll("\\+"," ")
        Assert.assertEquals(errorUrl, "Request Object and Authorization request contains unmatched client_id")

    }

    @Test
    void "OB-1312_Validate PAR request with cdr_arrangement_id belongs to different TPP"() {
        auConfiguration.setTppNumber(0)

        //authorise sharing arrangement
        doConsentAuthorisation()
        Assert.assertNotNull(authorisationCode)

        //obtain cdr_arrangement_id from token response
        AccessTokenResponse userAccessTokenRes = getUserAccessTokenResponse()
        String cdrArrangementId = userAccessTokenRes.getCustomParameters().get("cdr_arrangement_id")
        Assert.assertNotNull(cdrArrangementId)

        auConfiguration.setTppNumber(1)
        //Send PAR request.
        def parResponse = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, cdrArrangementId, clientID)

        Assert.assertEquals(parResponse.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(AUTestUtil.parseResponseBody(parResponse, AUConstants.ERROR_DESCRIPTION),
                "Invalid cdr_arrangement_id")
        Assert.assertEquals(AUTestUtil.parseResponseBody(parResponse, AUConstants.ERROR), AUConstants.INVALID_REQUEST_OBJECT)
    }

    @AfterClass(alwaysRun = true)
    void tearDown() {
        def registrationResponse = AURegistrationRequestBuilder.buildBasicRequest(accessToken)
                .when()
                .delete(AUConstants.DCR_REGISTRATION_ENDPOINT + clientID)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_204)
    }
}


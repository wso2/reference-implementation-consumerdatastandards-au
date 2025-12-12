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

package org.wso2.cds.keymanager.test.par

import com.nimbusds.oauth2.sdk.AccessTokenResponse
import com.nimbusds.oauth2.sdk.ResponseType
import org.testng.annotations.BeforeClass
import org.wso2.bfsi.test.framework.automation.AutomationMethod
import org.wso2.bfsi.test.framework.automation.NavigationAutomationStep
import org.wso2.cds.test.framework.AUTest
import org.wso2.cds.test.framework.automation.consent.AUBasicAuthAutomationStep
import org.wso2.cds.test.framework.configuration.AUConfigurationService
import org.wso2.cds.test.framework.constant.AUAccountProfile
import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.constant.AUPageObjects
import org.wso2.cds.test.framework.request_builder.AUJWTGenerator
import org.wso2.cds.test.framework.request_builder.AURequestBuilder
import org.wso2.cds.test.framework.utility.AURestAsRequestBuilder
import org.wso2.cds.test.framework.utility.AUTestUtil
import io.restassured.RestAssured
import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.Test

import javax.net.ssl.SSLHandshakeException

class PushedAuthorisationFlowTest extends AUTest {

    AUConfigurationService auConfiguration = new AUConfigurationService()
    def clientId = auConfiguration.getAppInfoClientID()
    AUJWTGenerator generator = new AUJWTGenerator()
    def refreshToken

    @BeforeClass
    void "setTppNumber"() {
        auConfiguration.setTppNumber(0)
    }

    @Test (groups = "SmokeTest")
    void "TC0205001_Data Recipients Initiate authorisation request using PAR"() {

        def response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, cdrArrangementId)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_201)
        Assert.assertNotNull(requestUri)
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.RESPONSE_EXPIRES_IN))
    }

    @Test(groups = "SmokeTest", dependsOnMethods = "TC0205001_Data Recipients Initiate authorisation request using PAR")
    void "TC0205002_Initiate consent authorisation flow with pushed authorisation request uri"() {

        doConsentAuthorisationViaRequestUri(scopes, requestUri.toURI())
        Assert.assertNotNull(authorisationCode)
    }

    @Test(groups = "SmokeTest", dependsOnMethods = "TC0205002_Initiate consent authorisation flow with pushed authorisation request uri")
    void "TC0203013_Generate User access token by code generated from PAR model"() {

        AccessTokenResponse userAccessToken = AURequestBuilder.getUserToken(authorisationCode,
                AUConstants.CODE_VERIFIER, clientId)
        Assert.assertNotNull(userAccessToken.tokens.accessToken)
        Assert.assertNotNull(userAccessToken.tokens.refreshToken)
        Assert.assertNotNull(userAccessToken.getCustomParameters().get(AUConstants.CDR_ARRANGEMENT_ID))
    }

    @Test
    void "TC0205003_Data Recipients Initiate authorisation request using PAR without MTLS security"() {

        try {
            RestAssured.given()
            clientId = auConfiguration.getAppInfoClientID()

            String assertionString = generator.getClientAssertionJwt(clientId)

            def bodyContent = [
                    (AUConstants.CLIENT_ID_KEY)            : (clientId),
                    (AUConstants.CLIENT_ASSERTION_TYPE_KEY): (AUConstants.CLIENT_ASSERTION_TYPE),
                    (AUConstants.CLIENT_ASSERTION_KEY)     : assertionString,
            ]

            String requestObjectClaims = generator.getRequestObjectClaim(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                    true, cdrArrangementId, auConfiguration.getAppInfoRedirectURL(),
                    clientId, "code id_token", false, "")

            def parResponse = AURestAsRequestBuilder.buildBasicRequest()
                    .contentType(AUConstants.ACCESS_TOKEN_CONTENT_TYPE)
                    .formParams(bodyContent)
                    .formParams(AUConstants.REQUEST_KEY,
                            generator.getSignedAuthRequestObject(requestObjectClaims).serialize())
                    .baseUri(AUConstants.PUSHED_AUTHORISATION_BASE_PATH)
                    .post(AUConstants.PAR_ENDPOINT)

        } catch (SSLHandshakeException e) {
            Assert.assertTrue(e.getMessage().contains("PKIX path building failed: " +
                    "sun.security.provider.certpath.SunCertPathBuilderException: " +
                    "unable to find valid certification path to requested target"))
        }
    }

    @Test
    void "TC0205004_Initiate consent authorisation flow with expired request uri"() {

        def response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_201)
        Assert.assertNotNull(requestUri)
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.RESPONSE_EXPIRES_IN))

        println "\nWaiting for request uri to expire..."
        sleep(65000)

        def authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), clientId)
                .toURI().toString()

        def automationResponse = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new NavigationAutomationStep(authoriseUrl, 10))
                .execute()

        def url = automationResponse.currentUrl.get()
        Assert.assertTrue(AUTestUtil.getErrorFromUrl(url).contains("Expired request URI"))

        def errorUrl = url.split("error_description=")[1].split("&")[0].replaceAll("\\+"," ")
        Assert.assertEquals(errorUrl, AUConstants.INVALID_REQUEST_URI)
    }

    @Test(priority = 3)
    void "TC0205006_Establish a new consent for an existing arrangement by passing existing cdr_arrangement_id"() {

        sleep(3000)

        Response response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_201)
        Assert.assertNotNull(requestUri)

        //Authorise Consent
        doConsentAuthorisationViaRequestUri(scopes, requestUri.toURI())
        Assert.assertNotNull(authorisationCode)

        //Generate User Access Token
        AccessTokenResponse userAccessToken = AURequestBuilder.getUserToken(authorisationCode,
                AUConstants.CODE_VERIFIER, clientId)

        accessToken = userAccessToken.tokens.toString()
        def refreshToken = userAccessToken.tokens.refreshToken.toString()
        cdrArrangementId = userAccessToken.getCustomParameters().get(AUConstants.CDR_ARRANGEMENT_ID)

        //Re-establish consent arrangement
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, cdrArrangementId)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_201)
        Assert.assertNotNull(requestUri)

        //Authorise New Consent - Profile Selection Is disabled
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), clientId).toURI().toString()

        automationResponse = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Select Individual Account 1
                    consentedAccount = authWebDriver.getElementAttribute(AUTestUtil.getAltSingleAccountXPath(),
                            AUPageObjects.VALUE)
                    authWebDriver.clickButtonXpath(AUTestUtil.getAltSingleAccountXPath())

                    //TODO: Change Button XPath after implementing V2 changes
                    //Click Confirm Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_NEXT)

                    //Click Authorise Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)
                }
                .execute()

        authorisationCode = AUTestUtil.getCodeFromJwtResponse(automationResponse.currentUrl.get())
        Assert.assertNotNull(authorisationCode)
    }

    @Test(enabled = true, priority = 3,
            dependsOnMethods = "TC0205006_Establish a new consent for an existing arrangement by passing existing cdr_arrangement_id")
    void "TC0203014_Tokens get revoked upon successful reestablishment of new consent via PAR model"() {

        def accessTokenIntrospect = AURequestBuilder.buildIntrospectionRequest(accessToken, clientId)
                .post(AUConstants.INTROSPECTION_ENDPOINT)

        def refreshTokenIntrospect = AURequestBuilder.buildIntrospectionRequest(accessToken, clientId)
                .post(AUConstants.INTROSPECTION_ENDPOINT)

        Assert.assertTrue((accessTokenIntrospect.jsonPath().get("active")).equals(false))
        Assert.assertTrue((refreshTokenIntrospect.jsonPath().get("active")).equals(false))
    }

    //TODO: Test failing: Not validating redirect_uri properly. Check Further
    @Test(priority = 2)
    void "TC0205015_Unable to initiate authorisation if the redirect uri mismatch with the application redirect uri"() {

        Response response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_201)
        Assert.assertNotNull(requestUri)

        //Authorise Consent
        doConsentAuthorisationViaRequestUri(scopes, requestUri.toURI())
        Assert.assertNotNull(authorisationCode)

        //Generate User Access Token
        AccessTokenResponse userAccessToken = AURequestBuilder.getUserToken(authorisationCode,
                AUConstants.CODE_VERIFIER, clientId)

        accessToken = userAccessToken.tokens.toString()
        refreshToken = userAccessToken.tokens.refreshToken.toString()
        cdrArrangementId = userAccessToken.getCustomParameters().get(AUConstants.CDR_ARRANGEMENT_ID)

        //Re-establish consent arrangement
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, cdrArrangementId)

        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_201)
        Assert.assertNotNull(requestUri)

        //Unsuccessful Authorisation Flow
        def incorrectRedirectUrl = "https://www.google.com"


        def authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(scopes, requestUri.toURI(),
                clientId, incorrectRedirectUrl).toURI().toString()

        def automationResponse = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new NavigationAutomationStep(authoriseUrl, 10))
                .execute()

        def errorMessage = URLDecoder.decode(automationResponse.currentUrl.get().split("&")[0]
                .split("=")[1].toString(), "UTF8")
        Assert.assertEquals(errorMessage, "invalid_callback")
    }

    @Test(priority = 2,
            dependsOnMethods = "TC0205015_Unable to initiate authorisation if the redirect uri mismatch with the application redirect uri")
    void "TC0203015_Tokens not get revoked upon unsuccessful reestablishment of new consent via PAR model"() {

        def accessTokenIntrospect = AURequestBuilder.buildIntrospectionRequest(accessToken, clientId)
                .post(AUConstants.INTROSPECTION_ENDPOINT)

        def refreshTokenIntrospect = AURequestBuilder.buildIntrospectionRequest(refreshToken, clientId)
                .post(AUConstants.INTROSPECTION_ENDPOINT)

        //Assert.assertTrue((accessTokenIntrospect.jsonPath().get("active")).equals(true))
        Assert.assertTrue((refreshTokenIntrospect.jsonPath().get("active")).equals(true))
    }

    @Test
    void "TC0205007_Reject consent authorisation flow when the cdr_arrangement_id define is not related to the authenticated user"() {

        def invalidCdrArrangementId = "db638818-be86-42fc-bdb8-1e2a1011866d"

        def response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, invalidCdrArrangementId)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION), "Invalid cdr_arrangement_id")
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR), AUConstants.INVALID_REQUEST_OBJECT)
    }

    @Test
    void "TC0205008_Reject consent authorisation flow when the cdr_arrangement_id is unrecognized by the Data Holder"() {

        def cdrArrangementId = "abcd1234"

        def response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, cdrArrangementId)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION), "Invalid cdr_arrangement_id")
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR), AUConstants.INVALID_REQUEST_OBJECT)
    }

    @Test
    void "TC0205009_Initiate pushed authorisation consent flow with no sharing duration"() {

        def response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.SINGLE_ACCESS_CONSENT,
                false, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        doConsentAuthorisationViaRequestUri(scopes, requestUri.toURI(), null)
        Assert.assertNotNull(authorisationCode)

        //Generate User Access Token
        AccessTokenResponse userAccessToken = AURequestBuilder.getUserToken(authorisationCode,
                AUConstants.CODE_VERIFIER, clientId)
        Assert.assertNull(userAccessToken.tokens.refreshToken)
        Assert.assertNotNull(userAccessToken.tokens.accessToken)
    }

    @Test
    void "TC0205010_Initiate pushed authorisation consent flow with zero sharing duration"() {

        def response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.SINGLE_ACCESS_CONSENT,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        doConsentAuthorisationViaRequestUri(scopes, requestUri.toURI(), null)
        Assert.assertNotNull(authorisationCode)

        //Generate User Access Token
        AccessTokenResponse userAccessToken = AURequestBuilder.getUserToken(authorisationCode,
                AUConstants.CODE_VERIFIER, clientId)
        Assert.assertNull(userAccessToken.tokens.refreshToken)
        Assert.assertNotNull(userAccessToken.tokens.accessToken)
    }

    @Test
    void "TC0205011_Initiate pushed authorisation consent flow with sharing duration greater than one year"() {

        def response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.ONE_YEAR_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        doConsentAuthorisationViaRequestUri(scopes, requestUri.toURI(), null)
        Assert.assertNotNull(authorisationCode)

        //Generate User Access Token
        AccessTokenResponse userAccessToken = AURequestBuilder.getUserToken(authorisationCode,
                AUConstants.CODE_VERIFIER, clientId)
        Assert.assertNotNull(userAccessToken.tokens.refreshToken)
        Assert.assertNotNull(userAccessToken.tokens.accessToken)
    }

    @Test
    void "TC0205012_Initiate pushed authorisation consent flow with negative sharing duration"() {

        def response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.NEGATIVE_DURATION,
                true, "")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION),
                "Invalid sharing_duration value")
    }

    @Test
    void "TC0205016_Unable to extract request uri if the client id mismatch with the application client id"() {

        def incorrectClientId = "YwSmCUteklf0T3MJdW8IQeM1kLga"

        def parResponse = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "", incorrectClientId, auConfiguration.getAppInfoRedirectURL())

        Assert.assertEquals(parResponse.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(AUTestUtil.parseResponseBody(parResponse, AUConstants.ERROR_DESCRIPTION),
                "Error retrieving service provider tenant domain for client_id: ${incorrectClientId}. " +
                        "Cannot proceed with signature validation")
        Assert.assertEquals(AUTestUtil.parseResponseBody(parResponse, AUConstants.ERROR),
                "Service provider metadata retrieval failed")
    }

    @Test
    void "TC0205017_Initiate authorisation flow by passing cdr_arrangement_id without PAR"() {

        doConsentAuthorisation()
        Assert.assertNotNull(authorisationCode)

        AccessTokenResponse userAccessToken = AURequestBuilder.getUserToken(authorisationCode,
                AUConstants.CODE_VERIFIER, clientId)
        cdrArrangementId = userAccessToken.getCustomParameters().get(AUConstants.CDR_ARRANGEMENT_ID)

        def authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, cdrArrangementId).toURI().toString()

        def automationResponse = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new NavigationAutomationStep(authoriseUrl, 10))
                .execute()

        Assert.assertTrue(AUTestUtil.getErrorDescriptionFromUrl(automationResponse.currentUrl.get())
                .contains("The claim cdr_arrangement_id is only accepted in par initiated requests."))
    }

    @Test
    void "TC0205018_Unable pass request_uri in the body of PAR request"() {


        def request_uri = "urn::bK4mreEMpZ42Ot4xxMOQdM2OvqhA66Rn"
        String assertionString = generator.getClientAssertionJwt(clientId)

        def bodyContent = [
                (AUConstants.CLIENT_ASSERTION_TYPE_KEY): (AUConstants.CLIENT_ASSERTION_TYPE),
                (AUConstants.CLIENT_ASSERTION_KEY)     : assertionString,
        ]

        def parResponse = AURestAsRequestBuilder.buildRequest()
                .contentType(AUConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .formParams(bodyContent)
                .formParams("request_uri", request_uri)
                .baseUri(AUConstants.PUSHED_AUTHORISATION_BASE_PATH)
                .post(AUConstants.PAR_ENDPOINT)

        Assert.assertEquals(parResponse.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(AUTestUtil.parseResponseBody(parResponse, AUConstants.ERROR_DESCRIPTION),
                "Request does not allow request_uri parameter")
        Assert.assertEquals(AUTestUtil.parseResponseBody(parResponse, AUConstants.ERROR),
                AUConstants.INVALID_REQUEST)
    }

    @Test
    void "OB-1690_Authorisation request with PAR request uri generated by passing null for sharing duration param"() {

        def response = auAuthorisationBuilder.doPushAuthRequestForStringSharingDuration(scopes, null,
                cdrArrangementId)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_201)
        Assert.assertNotNull(requestUri)
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.RESPONSE_EXPIRES_IN))
    }

    @Test
    void "OB-1701_Authorisation request with PAR request uri generated by passing string for sharing duration param"() {

        def response = auAuthorisationBuilder.doPushAuthRequestForStringSharingDuration(scopes, "one",
                cdrArrangementId)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)
    }

    @Test (enabled = true ,priority = 1)
    void "TC0205019_Unable to initiate authorisation if the scope not available"() {

        String claims = generator.getRequestObjectClaim(scopes, AUConstants.DEFAULT_SHARING_DURATION, true, "",
                auConfiguration.getAppInfoRedirectURL(), auConfiguration.getAppInfoClientID(),
                auAuthorisationBuilder.getResponseType().toString(), true,
                auAuthorisationBuilder.getState().toString())

        String modifiedClaimSet = generator.removeClaimsFromRequestObject(claims, "scope")

        def response = auAuthorisationBuilder.doPushAuthorisationRequest(modifiedClaimSet)

        def errorDesc = AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION)
        def error = AUTestUtil.parseResponseBody(response, AUConstants.ERROR)

        Assert.assertEquals(errorDesc, "Mandatory parameter scope, not found in the request")
        Assert.assertEquals(error, AUConstants.INVALID_REQUEST)
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)
    }

    @Test
    void "OB-1877_Consent authorisation request by passing previously used request_uri"() {

        def response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, cdrArrangementId)

        def requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        //Authorise Consent
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), clientId, false).toURI().toString()

        automationResponse = doAuthorisationFlowNavigation(authoriseUrl, AUAccountProfile.INDIVIDUAL, false)
        authorisationCode = AUTestUtil.getCodeFromJwtResponse(automationResponse.currentUrl.get())
        Assert.assertNotNull(authorisationCode)

        //Generate User Access Token
        AccessTokenResponse userAccessToken = AURequestBuilder.getUserToken(authorisationCode,
                AUConstants.CODE_VERIFIER, clientId)
        Assert.assertNotNull(userAccessToken.tokens.accessToken)
        Assert.assertNotNull(userAccessToken.tokens.refreshToken)
        Assert.assertNotNull(userAccessToken.getCustomParameters().get(AUConstants.CDR_ARRANGEMENT_ID))

        //Authorise Consent Using same request_uri
        automationResponse = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new NavigationAutomationStep(authoriseUrl, 10))
                .execute()

        String url = automationResponse.currentUrl.get()
        Assert.assertTrue(AUTestUtil.getErrorFromUrl(url).contains("Provided request URI is not valid"))
    }

    @Test
    void "OB-1249_Initiate authorisation consent flow with an invalid response_type in oauth request context"() {

        def response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, cdrArrangementId, auConfiguration.getAppInfoClientID(),
                auConfiguration.getAppInfoRedirectURL(), new ResponseType("form").toString())

        def errorDesc = AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION)
        def error = AUTestUtil.parseResponseBody(response, AUConstants.ERROR)

        Assert.assertEquals(errorDesc, "Invalid response_type parameter value")
        Assert.assertEquals(error, AUConstants.INVALID_REQUEST)
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)
    }

    @Test
    void "CDS-606_Validate PAR Request when Null cdr-arrangement-id sent in the request"() {

        def response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, null)

        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_201)
        Assert.assertNotNull(requestUri)
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.RESPONSE_EXPIRES_IN))
    }

    @Test
    void "CDS-1049_PAR Request without client id param in the request body"() {

        def response = auAuthorisationBuilder.doPushAuthorisationRequestWithoutClientId(scopes,
                AUConstants.DEFAULT_SHARING_DURATION, true, cdrArrangementId)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_201)
        Assert.assertNotNull(requestUri)
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.RESPONSE_EXPIRES_IN))
    }

    @Test
    void "CDS-1059_PAR Request with client Id in the request body not similar to client id in the client_assertion"() {

        def response = auAuthorisationBuilder.doPushAuthorisationRequestWithDifferentClientIds(scopes,
                AUConstants.DEFAULT_SHARING_DURATION, true, cdrArrangementId,
                auConfiguration.getAppInfoClientID(), auConfiguration.getAppInfoClientID(1))
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        def errorDesc = AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION)
        def error = AUTestUtil.parseResponseBody(response, AUConstants.ERROR)

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_401)
        Assert.assertEquals(errorDesc, "Request Parameter 'client_id' does not match the 'sub' claim in the client_assertion")
        Assert.assertEquals(error, AUConstants.INVALID_CLIENT)
    }

    @Test
    void "CDS-1069_PAR Request with valid client Id in the request body and deleted client id in the client_assertion"() {

        def invalidClientId = "qwe23rvdvdfvfd"
        def response = auAuthorisationBuilder.doPushAuthorisationRequestWithDifferentClientIds(scopes,
                AUConstants.DEFAULT_SHARING_DURATION, true, cdrArrangementId,
                invalidClientId, auConfiguration.getAppInfoClientID(1))
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        def errorDesc = AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION)
        def error = AUTestUtil.parseResponseBody(response, AUConstants.ERROR)

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(errorDesc, "Error retrieving service provider tenant domain for client_id: $invalidClientId")
        Assert.assertEquals(error, "Service provider metadata retrieval failed")
    }
}

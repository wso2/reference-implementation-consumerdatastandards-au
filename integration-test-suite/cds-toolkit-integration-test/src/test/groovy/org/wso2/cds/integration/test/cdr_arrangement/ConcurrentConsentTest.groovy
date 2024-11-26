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

package org.wso2.cds.integration.test.cdr_arrangement

import org.wso2.cds.test.framework.AUTest
import org.wso2.cds.test.framework.automation.consent.AUBasicAuthAutomationStep
import org.wso2.cds.test.framework.constant.AUAccountProfile
import org.wso2.cds.test.framework.constant.AUAccountScope
import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.constant.AUPageObjects
import org.wso2.cds.test.framework.request_builder.AUJWTGenerator
import org.wso2.cds.test.framework.request_builder.AURequestBuilder
import org.wso2.cds.test.framework.utility.AURestAsRequestBuilder
import org.wso2.cds.test.framework.utility.AUTestUtil
import org.wso2.openbanking.test.framework.automation.AutomationMethod
import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Tests Related to Concurrent Consents.
 */
class ConcurrentConsentTest extends AUTest {

    @Test
    void "TC0204001_Retrieve Consumer data using tokens obtained for multiple consents"() {

        List<AUAccountScope> scopeOfFirstConsent = [AUAccountScope.BANK_ACCOUNT_BASIC_READ ]
        List<AUAccountScope> scopeOfSecondConsent = [AUAccountScope.BANK_PAYEES_READ ]
        def clientId = auConfiguration.getAppInfoClientID()

        //Consent Authorisation - 1
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopeOfFirstConsent, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        doConsentAuthorisationViaRequestUri(scopeOfFirstConsent, requestUri.toURI(), clientId, AUAccountProfile.INDIVIDUAL)
        Assert.assertNotNull(authorisationCode)
        def userAccessTokenFirstConsent = AURequestBuilder.getUserToken(authorisationCode,
                scopeOfFirstConsent, AUConstants.CODE_VERIFIER)

        //Consent Authorisation - 2
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopeOfSecondConsent, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        doConsentAuthorisationViaRequestUriNoAccountSelection(scopeOfSecondConsent, requestUri.toURI(), clientId, AUAccountProfile.INDIVIDUAL)
        Assert.assertNotNull(authorisationCode)
        def userAccessTokenSecondConsent = AURequestBuilder.getUserToken(authorisationCode,
                scopeOfFirstConsent, AUConstants.CODE_VERIFIER)

        Response firstAccountsResponse = AURequestBuilder
                .buildBasicRequest(userAccessTokenFirstConsent.tokens.accessToken.toString(),
                        AUConstants.CDR_ENDPOINT_VERSION)
                .header(AUConstants.X_FAPI_AUTH_DATE, AUConstants.DATE)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get("${AUConstants.BULK_ACCOUNT_PATH}")

        Assert.assertEquals(firstAccountsResponse.statusCode(), AUConstants.STATUS_CODE_200)

        Response secondAccountsResponse = AURequestBuilder
                .buildBasicRequest(userAccessTokenSecondConsent.tokens.accessToken.toString(),
                        AUConstants.CDR_ENDPOINT_VERSION)
                .header(AUConstants.X_FAPI_AUTH_DATE, AUConstants.DATE)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PAYEES))
                .get("${AUConstants.BULK_PAYEES}")

        Assert.assertEquals(secondAccountsResponse.statusCode(), AUConstants.STATUS_CODE_200)
    }

    @Test
    void "TC0204002_Retrieve Consumer data using invalid tokens obtained for multiple consents"() {

        List<AUAccountScope> scopeOfFirstConsent = [AUAccountScope.BANK_ACCOUNT_BASIC_READ ]
        List<AUAccountScope> scopeOfSecondConsent = [AUAccountScope.BANK_PAYEES_READ ]

        //Consent Authorisation - 1
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopeOfFirstConsent, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        doConsentAuthorisationViaRequestUri(scopeOfFirstConsent, requestUri.toURI(), clientId, AUAccountProfile.INDIVIDUAL)
        Assert.assertNotNull(authorisationCode)
        def userAccessTokenFirstConsent = AURequestBuilder.getUserToken(authorisationCode,
                scopeOfFirstConsent, AUConstants.CODE_VERIFIER)

        //Consent Authorisation - 2
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopeOfSecondConsent, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        doConsentAuthorisationViaRequestUriNoAccountSelection(scopeOfSecondConsent, requestUri.toURI(), clientId, AUAccountProfile.INDIVIDUAL)
        Assert.assertNotNull(authorisationCode)
        def userAccessTokenSecondConsent = AURequestBuilder.getUserToken(authorisationCode,
                scopeOfFirstConsent, AUConstants.CODE_VERIFIER)

        Response firstAccountsResponse = AURequestBuilder
                .buildBasicRequest(userAccessTokenSecondConsent.tokens.accessToken.toString(),
                        AUConstants.CDR_ENDPOINT_VERSION)
                .header(AUConstants.X_FAPI_AUTH_DATE, AUConstants.DATE)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get("${AUConstants.BULK_ACCOUNT_PATH}")

        Assert.assertEquals(firstAccountsResponse.statusCode(), AUConstants.STATUS_CODE_403)

        Response secondAccountsResponse = AURequestBuilder
                .buildBasicRequest(userAccessTokenFirstConsent.tokens.accessToken.toString(),
                        AUConstants.CDR_ENDPOINT_VERSION)
                .header(AUConstants.X_FAPI_AUTH_DATE, AUConstants.DATE)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PAYEES))
                .get("${AUConstants.BULK_PAYEES}")

        Assert.assertEquals(secondAccountsResponse.statusCode(), AUConstants.STATUS_CODE_403)
    }

    @Test
    void "TC0902001_Revoke consent using cdr management endpoint"() {

        List<AUAccountScope> sharingScope = [ AUAccountScope.BANK_ACCOUNT_BASIC_READ ]

        //authorise sharing arrangement
        response = auAuthorisationBuilder.doPushAuthorisationRequest(sharingScope, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        doConsentAuthorisationViaRequestUri(sharingScope, requestUri.toURI(), clientId, AUAccountProfile.INDIVIDUAL)
        Assert.assertNotNull(authorisationCode)
        def userAccessTokenResponse = AURequestBuilder.getUserToken(authorisationCode,
                sharingScope, AUConstants.CODE_VERIFIER)
        String userAccessToken = userAccessTokenResponse.tokens.accessToken.toString()

        //obtain cdr_arrangement_id from token response
        String cdrArrangementId = userAccessTokenResponse.getCustomParameters().get(AUConstants.CDR_ARRANGEMENT_ID)
        Assert.assertNotNull(cdrArrangementId)

        //retrieve consumer data successfully
        Response response = AURequestBuilder
                .buildBasicRequest(userAccessToken, AUConstants.CDR_ENDPOINT_VERSION)
                .header(AUConstants.X_FAPI_AUTH_DATE, AUConstants.DATE)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get("${AUConstants.BULK_ACCOUNT_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //revoke sharing arrangement
        def revokeResponse = doRevokeCdrArrangement(auConfiguration.getAppInfoClientID(), cdrArrangementId)

        Assert.assertEquals(revokeResponse.statusCode(), AUConstants.STATUS_CODE_204)

        Thread.sleep(100000)

        //try to retrieve consumer data after revocation
        response = AURequestBuilder
                .buildBasicRequest(userAccessToken, AUConstants.CDR_ENDPOINT_VERSION)
                .header(AUConstants.X_FAPI_AUTH_DATE, AUConstants.DATE)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get("${AUConstants.BULK_ACCOUNT_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_403)

        //validate token
        def introspectResponse = AURequestBuilder.buildIntrospectionRequest(userAccessToken,
                auConfiguration.getAppInfoClientID(), 0)
                .post(AUConstants.INTROSPECTION_ENDPOINT)

        Assert.assertTrue((introspectResponse.jsonPath().get("active")).equals(false))
    }

    @Test (dependsOnMethods = "TC0902001_Revoke consent using cdr management endpoint")
    void "CDS-147_Invoke cdr management endpoint with previously revoked cdr_arrangement_id"() {

        //revoke sharing arrangement without cdr arrangement id
        generator = new AUJWTGenerator()
        String assertionString = generator.getClientAssertionJwt(clientId)

        def bodyContent = [(AUConstants.CLIENT_ID_KEY): (clientId),
                           (AUConstants.CLIENT_ASSERTION_TYPE_KEY): (AUConstants.CLIENT_ASSERTION_TYPE),
                           (AUConstants.CLIENT_ASSERTION_KEY)     : assertionString,
                           (AUConstants.CDR_ARRANGEMENT_ID)       : cdrArrangementId]

        revocationResponse = AURestAsRequestBuilder.buildRequest()
                .contentType(AUConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .formParams(bodyContent)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_CDR_ARRANGEMENT))
                .post("${AUConstants.CDR_ARRANGEMENT_ENDPOINT}")

        Assert.assertEquals(revocationResponse.statusCode(), AUConstants.STATUS_CODE_422)

        Assert.assertEquals(AUTestUtil.parseResponseBody(revocationResponse, AUConstants.ERROR_CODE),
                AUConstants.ERROR_CODE_INVALID_ARRANGEMENT)
        Assert.assertEquals(AUTestUtil.parseResponseBody(revocationResponse, AUConstants.ERROR_TITLE),
                AUConstants.INVALID_CONSENT_ARRANGEMENT)
        Assert.assertEquals(AUTestUtil.parseResponseBody(revocationResponse, AUConstants.ERROR_DETAIL),
                "invalid cdr-arrangement-id sent in the request")
    }

    @Test
    void "TC0203010_Generate User access token by revoked consent"() {

        List<AUAccountScope> sharingScopes = [ AUAccountScope.BANK_ACCOUNT_BASIC_READ ]

        //authorise sharing arrangement
        response = auAuthorisationBuilder.doPushAuthorisationRequest(sharingScopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        doConsentAuthorisationViaRequestUri(sharingScopes, requestUri.toURI(), clientId, AUAccountProfile.INDIVIDUAL)
        Assert.assertNotNull(authorisationCode)
        def userAccessTokenResponse = AURequestBuilder.getUserToken(authorisationCode,
                sharingScopes, AUConstants.CODE_VERIFIER)
        String userAccessToken = userAccessTokenResponse.tokens.accessToken.toString()

        //obtain cdr_arrangement_id from token response
        String cdrArrangementId = userAccessTokenResponse.getCustomParameters().get(AUConstants.CDR_ARRANGEMENT_ID)

        Assert.assertNotNull(cdrArrangementId)

        //retrieve consumer data successfully
        Response response = AURequestBuilder
                .buildBasicRequest(userAccessToken, AUConstants.CDR_ENDPOINT_VERSION)
                .header(AUConstants.X_FAPI_AUTH_DATE, AUConstants.DATE)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get("${AUConstants.BULK_ACCOUNT_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //revoke sharing arrangement
        def revokeResponse = doRevokeCdrArrangement(auConfiguration.getAppInfoClientID(), cdrArrangementId)

        Assert.assertEquals(revokeResponse.statusCode(), AUConstants.STATUS_CODE_204)

        //generate user access token
        def errorObject = AURequestBuilder.getUserTokenErrorResponse(authorisationCode)

        Assert.assertEquals(errorObject.toJSONObject().get(AUConstants.ERROR_DESCRIPTION),
                "Inactive authorization code received from token request")
    }

    //When cdr_arrangement_id is sent as a claim in the request object, the corresponding consent should get revoked
    //upon staging new consent
    @Test
    void "TC0204003_Revoke consent using upon staging of a new consent"() {
        List<AUAccountScope> scopeOfFirstConsent = [AUAccountScope.BANK_ACCOUNT_BASIC_READ ]
        List<AUAccountScope> scopeOfSecondConsent = [AUAccountScope.BANK_PAYEES_READ ]

        //authorise the first sharing arrangement
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopeOfFirstConsent, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        doConsentAuthorisationViaRequestUri(scopeOfFirstConsent, requestUri.toURI(), clientId, AUAccountProfile.INDIVIDUAL)
        Assert.assertNotNull(authorisationCode)
        def userAccessTokenResponse = AURequestBuilder.getUserToken(authorisationCode,
                scopeOfFirstConsent, AUConstants.CODE_VERIFIER)
        String userAccessTokenFirstConsent = userAccessTokenResponse.tokens.accessToken.toString()
        String refreshTokenFirstConsent = userAccessTokenResponse.tokens.refreshToken.toString()

        //obtain cdr_arrangement_id from token response
        String cdrArrangementIdFirstConsent = userAccessTokenResponse.getCustomParameters().get(AUConstants.CDR_ARRANGEMENT_ID)
        Assert.assertNotNull(cdrArrangementIdFirstConsent)

        //authorize the second sharing arrangement
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopeOfFirstConsent, AUConstants.DEFAULT_SHARING_DURATION,
                true, cdrArrangementIdFirstConsent)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI()).toURI().toString()

        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Verify Account Selection Page
                    assert authWebDriver.isElementDisplayed(AUTestUtil.getAltSingleAccountXPath())
                    authWebDriver.clickButtonXpath(AUTestUtil.getAltSingleAccountXPath())

                    //Click Confirm Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)

                    //Click Authorise Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)
                }
                .execute()

        // Get Code From URL
        authorisationCode = AUTestUtil.getCodeFromJwtResponse(automation.currentUrl.get())

        //Get User Access Token
        def userAccessTokenResponseForSecondConsent = AURequestBuilder.getUserToken(authorisationCode,
                scopeOfSecondConsent, AUConstants.CODE_VERIFIER)
        String userAccessTokenSecondConsent = userAccessTokenResponseForSecondConsent.tokens.accessToken.toString()
        String refreshTokenSecondConsent = userAccessTokenResponseForSecondConsent.tokens.refreshToken.toString()

        String cdrArrangementIdSecondConsent = userAccessTokenResponseForSecondConsent.getCustomParameters().get(AUConstants.CDR_ARRANGEMENT_ID)
        Assert.assertNotNull(cdrArrangementIdSecondConsent)

        Thread.sleep(2000)

        //validate first token
        def introspectResponseFirstToken = AURequestBuilder.buildIntrospectionRequest(refreshTokenFirstConsent,
                auConfiguration.getAppInfoClientID(), 0)
                .post(AUConstants.INTROSPECTION_ENDPOINT)

        Assert.assertTrue(introspectResponseFirstToken.jsonPath().get("active").toString().contains("false"))

        //validate second token
        def introspectResponseSecondToken = AURequestBuilder.buildIntrospectionRequest(refreshTokenSecondConsent,
                auConfiguration.getAppInfoClientID(), 0)
                .post(AUConstants.INTROSPECTION_ENDPOINT)

        Assert.assertTrue(introspectResponseSecondToken.jsonPath().get("active").toString().contains("true"))

    }

    @Test
    void "TC0902002_Revoke consent using cdr management endpoint without cdr arrangement id"() {

        //revoke sharing arrangement without cdr arrangement id
        generator = new AUJWTGenerator()
        String assertionString = generator.getClientAssertionJwt(clientId)

        def bodyContent = [(AUConstants.CLIENT_ID_KEY): (clientId),
                           (AUConstants.CLIENT_ASSERTION_TYPE_KEY): (AUConstants.CLIENT_ASSERTION_TYPE),
                           (AUConstants.CLIENT_ASSERTION_KEY)     : assertionString]

        revocationResponse = AURestAsRequestBuilder.buildRequest()
                .contentType(AUConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .formParams(bodyContent)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_CDR_ARRANGEMENT))
                .post("${AUConstants.CDR_ARRANGEMENT_ENDPOINT}")

        Assert.assertEquals(revocationResponse.statusCode(), AUConstants.STATUS_CODE_400)
    }

    @Test
    void "CDS-1050_CDR Arrangement Revocation Request without client id param in the request body"() {

        List<AUAccountScope> sharingScope = [ AUAccountScope.BANK_ACCOUNT_BASIC_READ ]

        //authorise sharing arrangement
        response = auAuthorisationBuilder.doPushAuthorisationRequest(sharingScope, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        doConsentAuthorisationViaRequestUri(sharingScope, requestUri.toURI(), clientId, AUAccountProfile.INDIVIDUAL)
        Assert.assertNotNull(authorisationCode)

        //Generate User Access Token
        def userAccessTokenResponse = AURequestBuilder.getUserToken(authorisationCode,
                sharingScope, AUConstants.CODE_VERIFIER)
        String userAccessToken = userAccessTokenResponse.tokens.accessToken.toString()

        //obtain cdr_arrangement_id from token response
        String cdrArrangementId = userAccessTokenResponse.getCustomParameters().get(AUConstants.CDR_ARRANGEMENT_ID)
        Assert.assertNotNull(cdrArrangementId)

        //retrieve consumer data successfully
        Response response = AURequestBuilder
                .buildBasicRequest(userAccessToken, AUConstants.CDR_ENDPOINT_VERSION)
                .header(AUConstants.X_FAPI_AUTH_DATE, AUConstants.DATE)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get("${AUConstants.BULK_ACCOUNT_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //revoke sharing arrangement
        def revokeResponse = doRevokeCdrArrangementWithoutClientIdInRequest(auConfiguration.getAppInfoClientID(),
                cdrArrangementId)

        Assert.assertEquals(revokeResponse.statusCode(), AUConstants.STATUS_CODE_204)

    }

    @Test
    void "CDS-146_Invoke cdr management endpoint with invalid cdr_arrangement_id"() {

        //revoke sharing arrangement without cdr arrangement id
        generator = new AUJWTGenerator()
        String assertionString = generator.getClientAssertionJwt(clientId)

        def bodyContent = [(AUConstants.CLIENT_ID_KEY): (clientId),
                           (AUConstants.CLIENT_ASSERTION_TYPE_KEY): (AUConstants.CLIENT_ASSERTION_TYPE),
                           (AUConstants.CLIENT_ASSERTION_KEY)     : assertionString,
                           (AUConstants.CDR_ARRANGEMENT_ID)       : "123"]

        revocationResponse = AURestAsRequestBuilder.buildRequest()
                .contentType(AUConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .formParams(bodyContent)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_CDR_ARRANGEMENT))
                .post("${AUConstants.CDR_ARRANGEMENT_ENDPOINT}")

        Assert.assertEquals(revocationResponse.statusCode(), AUConstants.STATUS_CODE_422)

        Assert.assertEquals(AUTestUtil.parseResponseBody(revocationResponse, AUConstants.ERROR_CODE),
                AUConstants.ERROR_CODE_INVALID_ARRANGEMENT)
        Assert.assertEquals(AUTestUtil.parseResponseBody(revocationResponse, AUConstants.ERROR_TITLE),
                AUConstants.INVALID_CONSENT_ARRANGEMENT)
        Assert.assertEquals(AUTestUtil.parseResponseBody(revocationResponse, AUConstants.ERROR_DETAIL),
                "invalid cdr-arrangement-id sent in the request")
    }
}

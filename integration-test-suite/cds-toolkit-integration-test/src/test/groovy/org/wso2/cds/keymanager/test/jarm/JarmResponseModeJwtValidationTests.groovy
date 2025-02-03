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

package org.wso2.cds.keymanager.test.jarm

import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.oauth2.sdk.ResponseMode
import com.nimbusds.oauth2.sdk.ResponseType
import org.testng.annotations.BeforeClass
import org.wso2.cds.test.framework.AUTest
import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.request_builder.AUJWTGenerator
import org.wso2.cds.test.framework.utility.AUTestUtil
import org.testng.Assert
import org.testng.annotations.Test

/**
 * JARM Validation Tests with Response Mode jwt.
 */
class JarmResponseModeJwtValidationTests extends AUTest{

    String authResponseUrl
    String responseJwt
    JWTClaimsSet jwtPayload

    @BeforeClass
    void "setTppNumber"() {
        auConfiguration.setTppNumber(0)
    }

    @Test (groups = "SmokeTest")
    void "CDS-577_Verify authorisation flow with response method jwt and response type code"() {

        doConsentAuthorisation(ResponseMode.JWT, ResponseType.CODE, auConfiguration.getAppInfoClientID())
        authResponseUrl = automationResponse.currentUrl.get()
        responseJwt = authResponseUrl.split(AUConstants.HTML_RESPONSE_ATTR)[1]
        Assert.assertNotNull(responseJwt)
        jwtPayload = AUJWTGenerator.extractJwt(responseJwt)

        authorisationCode = jwtPayload.getStringClaim(AUConstants.CODE_KEY)
        Assert.assertNotNull(authorisationCode)
        Assert.assertTrue(authResponseUrl.split(AUConstants.HTML_RESPONSE_ATTR)[0].contains("?"))
    }

    @Test
    void "CDS-578_Verify authorisation flow with response method jwt and response type token"() {

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "", auConfiguration.getAppInfoClientID(),
                auConfiguration.getAppInfoRedirectURL(), ResponseType.TOKEN.toString(), true,
                ResponseMode.JWT.toString())
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR), AUConstants.INVALID_REQUEST)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION),
                AUConstants.ERROR_UNSUPPORTED_RESPONSE)
    }

    @Test
    void "CDS-579_Verify authorisation flow with response method jwt and response type code id_token"() {

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "", auConfiguration.getAppInfoClientID(),
                auConfiguration.getAppInfoRedirectURL(), ResponseType.CODE_IDTOKEN.toString(), true,
                ResponseMode.JWT.toString())

        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR), AUConstants.INVALID_REQUEST)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION),
                AUConstants.ERROR_UNSUPPORTED_RESPONSE)
    }

    @Test
    void "CDS-580_Verify a User access Token call with the Code received from jwt"() {

        //Consent Authorisation
        doConsentAuthorisation(ResponseMode.JWT, ResponseType.CODE, auConfiguration.getAppInfoClientID())
        authResponseUrl = automationResponse.currentUrl.get()
        responseJwt = authResponseUrl.split(AUConstants.HTML_RESPONSE_ATTR)[1]
        Assert.assertNotNull(responseJwt)
        jwtPayload = AUJWTGenerator.extractJwt(responseJwt)

        authorisationCode = jwtPayload.getStringClaim(AUConstants.CODE_KEY)
        Assert.assertNotNull(authorisationCode)
        Assert.assertTrue(authResponseUrl.split(AUConstants.HTML_RESPONSE_ATTR)[0].contains("?"))

        //Generate User Access Token
        generateUserAccessToken(auConfiguration.getAppInfoClientID())
        Assert.assertNotNull(userAccessToken)
    }

    @Test
    void "CDS-581_Verify in jwt response mode if response_type = none"() {

        def clientId = auConfiguration.getAppInfoClientID()

        //Send PAR request
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "", clientId, auConfiguration.getAppInfoRedirectURL(),
                ResponseType.parse("NONE").toString(), true, ResponseMode.JWT.toString())

        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR), AUConstants.INVALID_REQUEST)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION),
                AUConstants.ERROR_UNSUPPORTED_RESPONSE)
    }
}

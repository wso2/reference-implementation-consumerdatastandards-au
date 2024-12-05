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

package org.wso2.cds.keymanager.test.authorizationflow

import com.nimbusds.oauth2.sdk.AccessTokenResponse
import org.wso2.cds.test.framework.AUTest
import org.wso2.cds.test.framework.constant.AUAccountProfile
import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.utility.AUTestUtil
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Class contains the test cases for sharing duration validation.
 */
class SharingDurationValidationTest extends AUTest {

    private String cdrArrangementId = ""
    private AccessTokenResponse userAccessToken
    private String requestUri
    AccessTokenResponse accessTokenResponse

    @Test (enabled = true)
    void "TC0202002_Initiate authorisation consent flow with no sharing duration" () {

        //Send Authorisation Request
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.SINGLE_ACCESS_CONSENT,
                false, "")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_201)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        doConsentAuthorisationViaRequestUri(scopes, requestUri.toURI(), auConfiguration.getAppInfoClientID(),
                AUAccountProfile.INDIVIDUAL)
        Assert.assertNotNull(authorisationCode)

        //Generate User Access Token
        AccessTokenResponse accessTokenResponse = getUserAccessTokenResponse(clientId)
        Assert.assertNotNull(accessTokenResponse.tokens.accessToken)
        Assert.assertNotNull(accessTokenResponse.getCustomParameters().get(AUConstants.CDR_ARRANGEMENT_ID))
    }

    @Test
    void "TC0203002_Check no refresh token when no sharing duration is given"() {

        //Send Authorisation Request
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.SINGLE_ACCESS_CONSENT,
                false, "")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_201)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        doConsentAuthorisationViaRequestUri(scopes, requestUri.toURI(), auConfiguration.getAppInfoClientID(),
                AUAccountProfile.INDIVIDUAL)
        Assert.assertNotNull(authorisationCode)

        //Generate User Access Token
        AccessTokenResponse accessTokenResponse = getUserAccessTokenResponse(clientId)
        Assert.assertNotNull(accessTokenResponse.tokens.accessToken)
        Assert.assertNull(accessTokenResponse.tokens.refreshToken)
        Assert.assertNotNull(accessTokenResponse.getCustomParameters().get(AUConstants.CDR_ARRANGEMENT_ID))
    }

    @Test (enabled = true, priority = 2)
    void "TC0202003_Initiate authorisation consent flow with sharing duration zero" () {

        //Send Authorisation Request
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.SINGLE_ACCESS_CONSENT,
                true, "")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_201)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        doConsentAuthorisationViaRequestUri(scopes, requestUri.toURI(), auConfiguration.getAppInfoClientID(),
                AUAccountProfile.INDIVIDUAL)
        Assert.assertNotNull(authorisationCode)
    }

    @Test(dependsOnMethods = "TC0202003_Initiate authorisation consent flow with sharing duration zero", priority = 2)
    void "TC0203003_Check no refresh token for sharing duration zero"() {

        //Generate User Access Token
        accessTokenResponse = getUserAccessTokenResponse(clientId)
        Assert.assertNotNull(accessTokenResponse.tokens.accessToken)
        Assert.assertNull(accessTokenResponse.tokens.refreshToken)
        Assert.assertNotNull(accessTokenResponse.getCustomParameters().get(AUConstants.CDR_ARRANGEMENT_ID))
    }

    @Test (groups = "SmokeTest", priority = 3)
    void "TC0202004_Initiate authorisation consent flow with sharing duration greater than one year"() {

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.ONE_YEAR_DURATION,
                true, "")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_201)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        doConsentAuthorisationViaRequestUri(scopes, requestUri.toURI(), auConfiguration.getAppInfoClientID(),
                AUAccountProfile.INDIVIDUAL)
        Assert.assertNotNull(authorisationCode)

        requestUri = AUTestUtil.parseResponseBody(response, "request_uri")
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_201)

    }

    @Test(groups = "SmokeTest", priority = 3,
            dependsOnMethods = "TC0202004_Initiate authorisation consent flow with sharing duration greater than one year")
    void "TC0203004_Check refresh token issued for sharing duration greater than one year"() {

        //Generate User Access Token
        accessTokenResponse = getUserAccessTokenResponse(clientId)
        Assert.assertNotNull(accessTokenResponse.tokens.accessToken)
        Assert.assertNotNull(accessTokenResponse.tokens.refreshToken)
        Assert.assertNotNull(accessTokenResponse.getCustomParameters().get(AUConstants.CDR_ARRANGEMENT_ID))
    }

    @Test (priority = 4)
    void "TC0202005_Initiate authorisation consent flow with negative sharing duration"() {

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.NEGATIVE_SHARING_DURATION,
                true, "")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION),
                AUConstants.INVALID_SHARING_DURATION)
    }
}

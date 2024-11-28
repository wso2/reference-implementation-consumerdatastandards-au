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
import org.wso2.cds.test.framework.AUTest
import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.utility.AUTestUtil
import org.testng.Assert
import org.testng.annotations.Test

/**
 * JARM Validation Tests with Response Mode form_post.jwt.
 */
class JarmResponseModeFormPostJwtValidationTests extends AUTest {

    String authResponseUrl
    String responseJwt
    HashMap<String, String> mapPayload
    JWTClaimsSet jwtPayload

    @Test (groups = "SmokeTest")
    void "CDS-582_Verify response_mode form_post jwt navigates to Authorization Flow"() {

        def clientId = auConfiguration.getAppInfoClientID()

        //Send PAR request and get the request URI with response type code
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "", clientId, auConfiguration.getAppInfoRedirectURL(), ResponseType.CODE.toString(),
        true, ResponseMode.FORM_POST_JWT.toString())

        Assert.assertEquals(response.getStatusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION), AUConstants.UNSUPPORTED_RESPONSE_MODE)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR), AUConstants.INVALID_REQUEST)
    }

    @Test
    void "CDS-583_Verify response_mode=form_post jwt and response_type=code id_token"() {

        def clientId = auConfiguration.getAppInfoClientID()

        //Send PAR request and get the request URI
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "", clientId, auConfiguration.getAppInfoRedirectURL(),
                ResponseType.CODE_IDTOKEN.toString(), true, ResponseMode.FORM_POST_JWT.toString())

        Assert.assertEquals(response.getStatusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION),
                AUConstants.ERROR_UNSUPPORTED_RESPONSE)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR), AUConstants.INVALID_REQUEST)
    }

    @Test
    void "CDS-584_Verify response_mode=form_post jwt and response_type=token"() {

        def clientId = auConfiguration.getAppInfoClientID()

        //Send PAR request and get the request URI
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "", clientId, auConfiguration.getAppInfoRedirectURL(),
                ResponseType.TOKEN.toString(), true, ResponseMode.FORM_POST_JWT.toString())

        Assert.assertEquals(response.getStatusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION),
                AUConstants.ERROR_UNSUPPORTED_RESPONSE)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR), AUConstants.INVALID_REQUEST)
    }

    @Test
    void "CDS-585_Verify response of the authorisation server to the user agent as HTML response when response_mode=form_post jwt"() {

        def clientId = auConfiguration.getAppInfoClientID()

        //Send PAR request and get the request URI
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "", clientId, auConfiguration.getAppInfoRedirectURL(),
                ResponseType.CODE_IDTOKEN.toString(), true, ResponseMode.FORM_POST_JWT.toString())

        Assert.assertEquals(response.getStatusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION),
                AUConstants.ERROR_UNSUPPORTED_RESPONSE)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR), AUConstants.INVALID_REQUEST)
    }
}

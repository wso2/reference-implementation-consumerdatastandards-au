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
import org.wso2.cds.test.framework.utility.AUTestUtil
import org.testng.Assert
import org.testng.annotations.Test

/**
 * JARM Validation Tests with Response Mode query.jwt.
 */
class JarmResponseModeQueryJwtValidationTests extends AUTest {

    @BeforeClass
    void "setTppNumber"() {
        auConfiguration.setTppNumber(0)
    }

    @Test (groups = "SmokeTest")
    void "CDS-587_Verify in query jwt response mode if response_type = code"() {

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "", auConfiguration.getAppInfoClientID(),
                auConfiguration.getAppInfoRedirectURL(), ResponseType.CODE.toString(), true,
                ResponseMode.QUERY_JWT.toString())

        Assert.assertEquals(response.getStatusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION), AUConstants.UNSUPPORTED_RESPONSE_MODE)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR), AUConstants.INVALID_REQUEST)
    }

    @Test
    void "CDS-561_Verify in query jwt response mode if response_type = token"() {

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "", auConfiguration.getAppInfoClientID(),
                auConfiguration.getAppInfoRedirectURL(), ResponseType.TOKEN.toString(), true,
                ResponseMode.QUERY_JWT.toString())

        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR), AUConstants.INVALID_REQUEST)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION),
                AUConstants.ERROR_UNSUPPORTED_RESPONSE)
    }

    @Test
    void "CDS-562_Verify in query jwt response mode if response_type = code id_token"() {

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "", auConfiguration.getAppInfoClientID(),
                auConfiguration.getAppInfoRedirectURL(), ResponseType.CODE_IDTOKEN.toString(), true,
                ResponseMode.QUERY_JWT.toString())

        Assert.assertEquals(response.getStatusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION),
                AUConstants.ERROR_UNSUPPORTED_RESPONSE)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR), AUConstants.INVALID_REQUEST)
    }
}

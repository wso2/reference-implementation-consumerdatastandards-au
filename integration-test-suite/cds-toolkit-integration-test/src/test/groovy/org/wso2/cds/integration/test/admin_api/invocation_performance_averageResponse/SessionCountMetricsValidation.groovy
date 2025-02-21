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

package org.wso2.cds.integration.test.admin_api.invocation_performance_averageResponse

import com.nimbusds.oauth2.sdk.TokenErrorResponse
import org.wso2.cds.test.framework.AUTest
import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.request_builder.AURequestBuilder
import org.wso2.cds.test.framework.utility.AUTestUtil
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

/**
 * Session Count Metrics Validation.
 */
class SessionCountMetricsValidation extends AUTest{

    @BeforeClass (alwaysRun = true)
    void "Initial Metrics Request"() {

        auConfiguration.setPsuNumber(0)
        auConfiguration.setTppNumber(0)
        //Initial Metrics Call
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Assign Metrics to Variables
        getInitialMetricsResponse(metricsResponse)
    }

    @Test (groups = "SmokeTest")
    void "Count increase upon successful user access token request"() {

        //Get User Access Token
        doConsentAuthorisation()
        generateUserAccessToken()
        Assert.assertNotNull(userAccessToken)

        //Send Metrics Request
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)
        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)

        sessionCount = sessionCount + 1

        Assert.assertEquals(AUTestUtil.parseResponseBody(metricsResponse, AUConstants.DATA_SESSION_COUNT_CURRENTDAY),
                "${sessionCount}", "$AUConstants.DATA_SESSION_COUNT_CURRENTDAY count mismatch")
    }

    @Test
    void "Count unchanged when unsuccessful token request"() {

        //Get User Access Token
        doConsentAuthorisation()

        //Send User Access Token request with incorrect redirect url
        TokenErrorResponse errorResponse = AURequestBuilder.getUserTokenErrorResponse(authorisationCode,
                "https://abc.com", auConfiguration.getAppInfoClientID(), true, true,
                auConfiguration.getCommonSigningAlgorithm(), auAuthorisationBuilder.getCodeVerifier())
        Assert.assertEquals(errorResponse.toJSONObject().get(AUConstants.ERROR_DESCRIPTION),
                AUConstants.CALLBACK_MISMATCH)

        //Send Metrics Request
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)
        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)

        Assert.assertEquals(AUTestUtil.parseResponseBody(metricsResponse, AUConstants.DATA_SESSION_COUNT_CURRENTDAY),
                "${sessionCount}", "$AUConstants.DATA_SESSION_COUNT_CURRENTDAY count mismatch")
    }
}

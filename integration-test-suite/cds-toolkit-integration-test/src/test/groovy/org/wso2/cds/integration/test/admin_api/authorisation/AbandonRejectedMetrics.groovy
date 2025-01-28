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

package org.wso2.cds.integration.test.admin_api.authorisation

import org.wso2.cds.test.framework.AUTest
import org.wso2.cds.test.framework.constant.AUAccountProfile
import org.wso2.cds.test.framework.constant.AUAccountScope
import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.utility.AUTestUtil
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

/**
 * Test Cases for Abandon Rejected Metrics.
 */
class  AbandonRejectedMetrics extends AUTest {

    private String cdrArrangementId = ""
    private String requestUri
    private def accessTokenResponse

    @BeforeClass (alwaysRun = true)
    void "Initial Metrics Request"() {

        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Assign Metrics to Variables
        getInitialMetricsResponse(metricsResponse)
    }

    @Test (groups = "SmokeTest")
    void "Deny the Consent Flow"() {

        auConfiguration.setPsuNumber(0)

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        String responseUrl = doConsentAuthorisationViaRequestUriDenyFlow(scopes, requestUri.toURI(),
                auConfiguration.getAppInfoClientID(), AUAccountProfile.INDIVIDUAL)

        authFlowError = AUTestUtil.getErrorDescriptionFromUrl(responseUrl)
        Assert.assertEquals(authFlowError, AUConstants.USER_DENIED_THE_CONSENT)

        //Verify Metrics Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Abandon count increased by 1
        if (AUConstants.X_V_HEADER_METRICS == 4) {
            abandonedCurrentDay = abandonedCurrentDay + 1
        } else {
            abandonedCurrentDay = abandonedCurrentDay + 1
            abandonedRejectedCurrentDay = abandonedRejectedCurrentDay + 1
        }

        //Asserting the response
        assertMetricsAuthorisationResponse(metricsResponse)
    }

    @Test
    void "Deny the Consent Flow in amendment flow"() {

        auConfiguration.setPsuNumber(0)

        // Send Authorisation request
        doConsentAuthorisation()

        // Retrieve the user access token by auth code
        accessTokenResponse = getUserAccessTokenResponse(clientId)
        cdrArrangementId = accessTokenResponse.getCustomParameters().get(AUConstants.CDR_ARRANGEMENT_ID)
        Assert.assertNotNull(cdrArrangementId)

        //remove an existing scope and add a new scope to amend the consent
        scopes.remove(AUAccountScope.BANK_TRANSACTION_READ)
        scopes.add(AUAccountScope.PROFILE)

        //Retrieve and assert the request URI from Push Authorization request
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, cdrArrangementId)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        String responseUrl = doConsentAmendmentDenyFlow(scopes, cdrArrangementId, AUConstants.SHORT_SHARING_DURATION,
        auConfiguration.getAppInfoClientID())

        authFlowError = AUTestUtil.getErrorDescriptionFromUrl(responseUrl)
        Assert.assertEquals(authFlowError, AUConstants.USER_DENIED_THE_CONSENT)

        sleep(AUConstants.ABANDON_WAIT_TIME)

        //Verify Metrics Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Active Individual Authorisation and New Ongoing Individual count increased by 1
        activeAuthIndividual = activeAuthIndividual + 1
        newAuthCurrentDayOngoingIndividual = newAuthCurrentDayOngoingIndividual + 1

        //Abandon count increased by 1
        if (AUConstants.X_V_HEADER_METRICS == 4) {
            abandonedCurrentDay = abandonedCurrentDay + 1
        } else {
            abandonedCurrentDay = abandonedCurrentDay + 1
            abandonedRejectedCurrentDay = abandonedRejectedCurrentDay + 1
        }

        //Asserting the response
        assertMetricsAuthorisationResponse(metricsResponse)
    }
}

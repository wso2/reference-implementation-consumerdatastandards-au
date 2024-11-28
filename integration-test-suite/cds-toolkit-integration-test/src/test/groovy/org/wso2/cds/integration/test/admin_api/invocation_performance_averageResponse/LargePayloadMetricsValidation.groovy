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

import org.wso2.cds.test.framework.AUTest
import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.request_builder.AURequestBuilder
import org.wso2.cds.test.framework.utility.AUTestUtil
import io.restassured.http.ContentType
import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

import java.nio.charset.Charset

/**
 * Test Cases for Invocations, Performance, Average Response and Average TPS Metrics - Large Payload Tier
 */
class LargePayloadMetricsValidation extends AUTest {

    private String encryptedAccount1Id, encryptedAccount2Id
    def clientHeader

    @BeforeClass (alwaysRun = true)
    void "Get User Access Token"() {

        clientHeader = "${Base64.encoder.encodeToString(getCDSClient().getBytes(Charset.defaultCharset()))}"

        if(!auConfiguration.getAppInfoClientID().equalsIgnoreCase("") ||
                !auConfiguration.getAppInfoClientID().equalsIgnoreCase("AppConfig.Application.ClientID") ) {
            doConsentAuthorisation()
            generateUserAccessToken()

            //Account Retrieval
            def response = AURequestBuilder
                    .buildBasicRequest(userAccessToken, AUConstants.X_V_HEADER_ACCOUNTS)
                    .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                    .get("${AUConstants.BULK_ACCOUNT_PATH}")

            encryptedAccount1Id = AUTestUtil.parseResponseBody(response, "data.accounts.accountId[0]")
            encryptedAccount2Id = AUTestUtil.parseResponseBody(response, "data.accounts.accountId[1]")
        }

        //Initial Metrics Call
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Assign Metrics to Variables
        getInitialMetricsResponse(metricsResponse)
    }

    @Test (groups = "SmokeTest")
    void "Count increase with Get Bulk Direct Debits invocation - customer present"() {

        Response response = AURequestBuilder
                .buildBasicRequestWithOptionalHeaders(userAccessToken, AUConstants.X_V_HEADER_DIRECT_DEBITS, clientHeader )
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_DIRECT_DEBIT))
                .get("${AUConstants.BULK_DIRECT_DEBITS_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Large Payload Current Day Invocation count increase by 1
        largePayloadCurrentDay = largePayloadCurrentDay + 1

        //Unattended count increase by 1 for the Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Count increase with Get Bulk Direct Debits invocation - without customer present"() {

        Response response = AURequestBuilder
                .buildBasicRequest(userAccessToken, AUConstants.X_V_HEADER_DIRECT_DEBITS)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_DIRECT_DEBIT))
                .get("${AUConstants.BULK_DIRECT_DEBITS_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Large Payload Current Day Invocation count increase by 1
        largePayloadCurrentDay = largePayloadCurrentDay + 1

        //Unattended count increase by 1 for the Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Count increase with Get Direct Debits For Specific Accounts invocation - customer present"() {

        String requestBody = """
            {
              "data": {
                "accountIds": [
                  "${encryptedAccount1Id}", "${encryptedAccount2Id}"
                ]
              },
              "meta": {}
            }
        """.stripIndent()

        def response = AURequestBuilder
                .buildBasicRequestWithOptionalHeaders(userAccessToken, AUConstants.X_V_HEADER_DIRECT_DEBITS, clientHeader)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_DIRECT_DEBIT))
                .post("${AUConstants.BULK_DIRECT_DEBITS_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Large Payload Current Day Invocation count increase by 1
        largePayloadCurrentDay = largePayloadCurrentDay + 1

        //Unattended count increase by 1 for the Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Count increase with Get Direct Debits For Specific Accounts invocation - without customer present"() {

        String requestBody = """
            {
              "data": {
                "accountIds": [
                  "${encryptedAccount1Id}", "${encryptedAccount2Id}"
                ]
              },
              "meta": {}
            }
        """.stripIndent()

        def response = AURequestBuilder
                .buildBasicRequest(userAccessToken, AUConstants.X_V_HEADER_DIRECT_DEBITS)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_DIRECT_DEBIT))
                .post("${AUConstants.BULK_DIRECT_DEBITS_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Large Payload Current Day Invocation count increase by 1
        largePayloadCurrentDay = largePayloadCurrentDay + 1

        //Unattended count increase by 1 for the Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Count increase when an Large Payload request return an error"() {

        String requestBody = """
            {
              "data": {
                "accountIds": [
                  "4327823409", "455325897"
                ]
              },
              "meta": {}
            }
        """.stripIndent()

        def response = AURequestBuilder
                .buildBasicRequest(userAccessToken, AUConstants.X_V_HEADER_DIRECT_DEBITS)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_DIRECT_DEBIT))
                .post("${AUConstants.BULK_DIRECT_DEBITS_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_422)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Large Payload Current Day Invocation count increase by 1
        largePayloadCurrentDay = largePayloadCurrentDay + 1

        //Unattended count increase by 1 for the Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Count increase when an Large Payload request return an 401 error"() {

        Response response = AURequestBuilder.buildBasicRequestWithOptionalHeaders(AUConstants.INCORRECT_ACCESS_TOKEN,
                AUConstants.X_V_HEADER_DIRECT_DEBITS, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_DIRECT_DEBIT))
                .get("${AUConstants.BULK_DIRECT_DEBITS_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_401)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Large Payload Current Day Invocation count increase by 1
        largePayloadCurrentDay = largePayloadCurrentDay + 1

        //Unattended count increase by 1 for the Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }
}

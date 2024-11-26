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
 * Test Cases for Invocations, Performance, Average Response and Average TPS Metrics - Low Priority Tier
 */
class LowPriorityMetricsValidation extends AUTest {

    private String encryptedAccount1Id, encryptedAccount2Id, encryptedTransactionId, encryptedPayeeId
    def clientHeader

    @BeforeClass
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

    @Test
    void "Count increase with Get Account Details - customer present invocation"() {

        def response = AURequestBuilder
                .buildBasicRequestWithOptionalHeaders(userAccessToken, AUConstants.X_V_HEADER_ACCOUNT, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get("${AUConstants.BULK_ACCOUNT_PATH}/${encryptedAccount1Id}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Low Priority Current Day Invocation count increase by 1
        lowPriorityCurrentDay = lowPriorityCurrentDay + 1

        //Unattended count increase by 1 for the Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Count increase with Get Account Balance - customer present invocation"() {

        def response = AURequestBuilder
                .buildBasicRequestWithOptionalHeaders(userAccessToken, AUConstants.X_V_HEADER_BALANCE, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_BALANCES))
                .get("${AUConstants.BULK_ACCOUNT_PATH}/${encryptedAccount1Id}/balance")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Low Priority Current Day Invocation count increase by 1
        lowPriorityCurrentDay = lowPriorityCurrentDay + 1

        //Unattended count increase by 1 for the Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Count increase with Get Bulk Balances - customer present invocation"() {

        def response = AURequestBuilder
                .buildBasicRequestWithOptionalHeaders(userAccessToken, AUConstants.X_V_HEADER_BALANCES, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_BALANCES))
                .get("${AUConstants.BULK_BALANCES_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Low Priority Current Day Invocation count increase by 1
        lowPriorityCurrentDay = lowPriorityCurrentDay + 1

        //Unattended count increase by 1 for the Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Count increase with Get Balances For Specific Accounts - customer present invocation"() {

        def response = AURequestBuilder
                .buildBasicRequestWithOptionalHeaders(userAccessToken, AUConstants.X_V_HEADER_BALANCE, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_BALANCES))
                .get("${AUConstants.BULK_ACCOUNT_PATH}/${encryptedAccount1Id}/balance")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Low Priority Current Day Invocation count increase by 1
        lowPriorityCurrentDay = lowPriorityCurrentDay + 1

        //Unattended count increase by 1 for the Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Count increase with Get Direct Debits For Account - customer present invocation"() {

        Response response = AURequestBuilder
                .buildBasicRequestWithOptionalHeaders(userAccessToken, AUConstants.X_V_HEADER_DIRECT_DEBITS, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_DIRECT_DEBIT))
                .get("${AUConstants.BULK_ACCOUNT_PATH}/${encryptedAccount1Id}/direct-debits")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Low Priority Invocation count increase by 1
        lowPriorityCurrentDay = lowPriorityCurrentDay + 1

        //Unattended count increase by 1 for the Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Count increase with Get Scheduled Payments Bulk - customer present invocation"() {

        Response response = AURequestBuilder
                .buildBasicRequestWithOptionalHeaders(userAccessToken, AUConstants.X_V_HEADER_PAYMENT_SCHEDULED, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_SCHEDULED_PAYMENT))
                .get("${AUConstants.BULK_SCHEDULE_PAYMENTS_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Low Priority Invocation count increase by 1
        lowPriorityCurrentDay = lowPriorityCurrentDay + 1

        //Unattended count increase by 1 for the Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Count increase with Get Scheduled Payments For Account - customer present invocation"() {

        Response response = AURequestBuilder
                .buildBasicRequestWithOptionalHeaders(userAccessToken, AUConstants.X_V_HEADER_PAYMENT_SCHEDULED, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_SCHEDULED_PAYMENT))
                .get("${AUConstants.BULK_ACCOUNT_PATH}/${encryptedAccount1Id}/payments/scheduled")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Low Priority Invocation count increase by 1
        lowPriorityCurrentDay = lowPriorityCurrentDay + 1

        //Unattended count increase by 1 for the Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Count increase with Get Scheduled Payments For Specific Accounts - customer present invocation"() {

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
                .buildBasicRequestWithOptionalHeaders(userAccessToken, AUConstants.X_V_HEADER_PAYMENT_SCHEDULED, clientHeader)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_SCHEDULED_PAYMENT))
                .post("${AUConstants.BULK_SCHEDULE_PAYMENTS_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Low Priority Invocation count increase by 1
        lowPriorityCurrentDay = lowPriorityCurrentDay + 1

        //Unattended count increase by 1 for the Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test (priority = 1)
    void "Count increase with Get Transactions For Account - customer present invocation"() {

        Response response = AURequestBuilder
                .buildBasicRequestWithOptionalHeaders(userAccessToken, AUConstants.X_V_HEADER_TRANSACTIONS, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_TRANSACTIONS))
                .get("${AUConstants.BULK_ACCOUNT_PATH}/${encryptedAccount1Id}/transactions")

        encryptedTransactionId = AUTestUtil.parseResponseBody(response, "data.transactions.transactionId[0]")
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Low Priority Current Day Invocation count increase by 1
        lowPriorityCurrentDay = lowPriorityCurrentDay + 1

        //Unattended count increase by 1 for the Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test (priority = 1, dependsOnMethods = "Count increase with Get Transactions For Account - customer present invocation")
    void "Count increase with Get Transaction Detail - customer present invocation"() {

        Response response = AURequestBuilder
                .buildBasicRequestWithOptionalHeaders(userAccessToken, AUConstants.X_V_HEADER_TRANSACTION, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_TRANSACTIONS))
                .get("${AUConstants.BULK_ACCOUNT_PATH}/${encryptedAccount1Id}/" +
                        "transactions/$encryptedTransactionId")

        encryptedTransactionId = AUTestUtil.parseResponseBody(response, "data.transactions.transactionId[0]")
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Low Priority Current Day Invocation count increase by 1
        lowPriorityCurrentDay = lowPriorityCurrentDay + 1

        //Unattended count increase by 1 for the Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test (priority = 2)
    void "Count increase with Get Payees - customer present invocation"() {

        Response response = AURequestBuilder
                .buildBasicRequestWithOptionalHeaders(userAccessToken, AUConstants.X_V_HEADER_PAYEES, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PAYEES))
                .get("${AUConstants.BULK_PAYEES}")

        encryptedPayeeId = AUTestUtil.parseResponseBody(response, "data.payees.payeeId[0]")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Low Priority Current Day Invocation count increase by 1
        lowPriorityCurrentDay = lowPriorityCurrentDay + 1

        //Unattended count increase by 1 for the Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test (priority = 2, dependsOnMethods = "Count increase with Get Payees - customer present invocation")
    void "Count increase with Get Payee Detail - customer present invocation"() {

        Response response = AURequestBuilder
                .buildBasicRequestWithOptionalHeaders(userAccessToken, AUConstants.X_V_HEADER_PAYEES, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PAYEES))
                .get("${AUConstants.BULK_PAYEES}/${encryptedPayeeId}")

        encryptedPayeeId = AUTestUtil.parseResponseBody(response, "data.payees.payeeId[0]")
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Low Priority Current Day Invocation count increase by 1
        lowPriorityCurrentDay = lowPriorityCurrentDay + 1

        //Unattended count increase by 1 for the Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test (priority = 3)
    void "Count increase when an Low Priority request return an error"() {

        String requestBody = """
            {
              "data": {
                "accountIds": [
                  "eryvsy35278feegyegyse", "yvwylyg89"
                ]
              },
              "meta": {}
            }
        """.stripIndent()

        def response = AURequestBuilder
                .buildBasicRequestWithOptionalHeaders(userAccessToken, AUConstants.X_V_HEADER_ACCOUNTS, clientHeader)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_BALANCES))
                .post("${AUConstants.BULK_BALANCES_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_422)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Low Priority Invocation count increase by 1
        lowPriorityCurrentDay = lowPriorityCurrentDay + 1

        //Unattended count increase by 1 for the Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test (priority = 3)
    void "Count increase when an Low Priority request return an 401 error"() {

        def response = AURequestBuilder.buildBasicRequestWithOptionalHeaders(AUConstants.INCORRECT_ACCESS_TOKEN,
                AUConstants.X_V_HEADER_ACCOUNTS, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get("${AUConstants.BULK_ACCOUNT_PATH}/${encryptedAccount1Id}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_401)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Low Priority Invocation count increase by 1
        lowPriorityCurrentDay = lowPriorityCurrentDay + 1

        //Unattended count increase by 1 for the Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }
}

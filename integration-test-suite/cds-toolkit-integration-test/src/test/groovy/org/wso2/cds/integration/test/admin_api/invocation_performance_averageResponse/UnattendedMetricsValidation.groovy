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
 * Test Cases for Invocations, Performance, Average Response and Average TPS Metrics - Unattended Tier
 */
class UnattendedMetricsValidation extends AUTest {

    private String encryptedAccount1Id, encryptedAccount2Id, encryptedTransactionId, encryptedPayeeId
    def clientHeader

    @BeforeClass (alwaysRun = true)
    void "Get User Access Token"() {

        auConfiguration.setPsuNumber(0)
        auConfiguration.setTppNumber(0)
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
    void "Count increase with Get Customer - Unattended invocation"() {

        Response response = AURequestBuilder
                .buildBasicRequest(userAccessToken, AUConstants.X_V_HEADER_CUSTOMER)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_CUSTOMER))
                .get("${AUConstants.BULK_CUSTOMER}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Unattended Invocation count increase by 2 (API Request + Metrics Call of BeforeClass Step)
        unattendedCurrentDay = unattendedCurrentDay + 2

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Count increase with Get Customer Detail - Unattended invocation"() {

        Response response = AURequestBuilder
                .buildBasicRequest(userAccessToken, AUConstants.X_V_HEADER_CUSTOMER_DETAIL)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_CUSTOMER))
                .get("${AUConstants.CUSTOMER_DETAILS}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Unattended Invocation count increase by 2 (API Request + Metrics Call of BeforeClass Step)
        unattendedCurrentDay = unattendedCurrentDay + 2

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Count increase with Metadata Update invocation"() {

        String assertionString = generator.getClientAssertionJwt(AUConstants.ADMIN_API_ISSUER, AUConstants.ADMIN_API_AUDIENCE)

        String requestBody = """
            {
                "data": {
                "action": "REFRESH"
            },
                "meta": {}
            }
         """.stripIndent()

        def response = AURequestBuilder.buildBasicRequest(assertionString, AUConstants.X_V_HEADER_METRICS)
                .header(AUConstants.CONTENT_TYPE, "application/json")
                .header(AUConstants.X_MIN_HEADER, AUConstants.X_V_MIN_HEADER_METRICS)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ADMIN))
                .body(requestBody)
                .post("${AUConstants.CDS_PATH}/admin/register/metadata")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Unattended Invocation count increase by 2 (API Request + Metrics Call of BeforeClass Step)
        unattendedCurrentDay = unattendedCurrentDay + 2

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test (invocationCount = 3)
    void "Count increase with Get Accounts - Unattended invocation"() {

        def response = AURequestBuilder
                .buildBasicRequest(userAccessToken, AUConstants.X_V_HEADER_ACCOUNTS)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get("${AUConstants.BULK_ACCOUNT_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        encryptedAccount1Id = AUTestUtil.parseResponseBody(response, "data.accounts.accountId[0]")
        encryptedAccount2Id = AUTestUtil.parseResponseBody(response, "data.accounts.accountId[1]")

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Unattended Invocation count increase by 2 (API Request + Metrics Call of BeforeClass Step)
        unattendedCurrentDay = unattendedCurrentDay + 2

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Count increase with Get Account Balance invocation"() {

        String requestUrl = "${AUConstants.BULK_ACCOUNT_PATH}/${encryptedAccount1Id}/balance"

        def response = AURequestBuilder
                .buildBasicRequest(userAccessToken, AUConstants.X_V_HEADER_BALANCE)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_BALANCES))
                .get(requestUrl)

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Unattended Invocation count increase by 2 (API Request + Metrics Call of BeforeClass Step)
        unattendedCurrentDay = unattendedCurrentDay + 2

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Count increase with Get Account Detail invocation"() {

        String requestUrl = "${AUConstants.BULK_ACCOUNT_PATH}/${encryptedAccount1Id}"

        def response = AURequestBuilder
                .buildBasicRequest(userAccessToken, AUConstants.X_V_HEADER_ACCOUNT)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get(requestUrl)

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Unattended Invocation count increase by 2 (API Request + Metrics Call of BeforeClass Step)
        unattendedCurrentDay = unattendedCurrentDay + 2

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Count increase with Get Balances For Specific Accounts invocation"() {

        def response = AURequestBuilder
                .buildBasicRequest(userAccessToken, AUConstants.X_V_HEADER_BALANCE)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_BALANCES))
                .get("${AUConstants.BULK_ACCOUNT_PATH}/${encryptedAccount1Id}/balance")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Unattended Invocation count increase by 2 (API Request + Metrics Call of BeforeClass Step)
        unattendedCurrentDay = unattendedCurrentDay + 2

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Count increase with Get Transactions For Account invocation"() {

        String requestUrl = "${AUConstants.BULK_ACCOUNT_PATH}/${encryptedAccount1Id}/transactions"

        Response response = AURequestBuilder
                .buildBasicRequest(userAccessToken, AUConstants.X_V_HEADER_TRANSACTIONS)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_TRANSACTIONS))
                .get(requestUrl)

        encryptedTransactionId = AUTestUtil.parseResponseBody(response, "data.transactions.transactionId[0]")
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Unattended Invocation count increase by 2 (API Request + Metrics Call of BeforeClass Step)
        unattendedCurrentDay = unattendedCurrentDay + 2

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Count increase with Get Direct Debits For Account invocation"() {

        String requestUrl = "${AUConstants.BULK_ACCOUNT_PATH}/${encryptedAccount1Id}/direct-debits"

        Response response = AURequestBuilder
                .buildBasicRequest(userAccessToken, AUConstants.X_V_HEADER_DIRECT_DEBITS)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_DIRECT_DEBIT))
                .get(requestUrl)

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Unattended Invocation count increase by 2 (API Request + Metrics Call of BeforeClass Step)
        unattendedCurrentDay = unattendedCurrentDay + 2

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Count increase with Get Scheduled Payments Bulk invocation"() {

        Response response = AURequestBuilder
                .buildBasicRequest(userAccessToken, AUConstants.X_V_HEADER_PAYMENT_SCHEDULED)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_SCHEDULED_PAYMENT))
                .get("${AUConstants.BULK_SCHEDULE_PAYMENTS_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Unattended Invocation count increase by 2 (API Request + Metrics Call of BeforeClass Step)
        unattendedCurrentDay = unattendedCurrentDay + 2

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Count increase with Get Scheduled Payments For Account invocation"() {

        String requestUrl = "${AUConstants.BULK_ACCOUNT_PATH}/${encryptedAccount1Id}/payments/scheduled"

        Response response = AURequestBuilder
                .buildBasicRequest(userAccessToken, AUConstants.X_V_HEADER_PAYMENT_SCHEDULED)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_SCHEDULED_PAYMENT))
                .get(requestUrl)

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Unattended Invocation count increase by 2 (API Request + Metrics Call of BeforeClass Step)
        unattendedCurrentDay = unattendedCurrentDay + 2

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Count increase with Get Scheduled Payments For Specific Accounts invocation"() {

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
                .buildBasicRequest(userAccessToken, AUConstants.X_V_HEADER_PAYMENT_SCHEDULED)
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

        //Unattended Invocation count increase by 2 (API Request + Metrics Call of BeforeClass Step)
        unattendedCurrentDay = unattendedCurrentDay + 2

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Count increase when an Unattended request return an error"() {

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
                .buildBasicRequest(userAccessToken, AUConstants.X_V_HEADER_ACCOUNTS)
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

        //Unattended Invocation count increase by 2 (API Request + Metrics Call of BeforeClass Step)
        unattendedCurrentDay = unattendedCurrentDay + 2

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test (priority = 1)
    void "TC1202001_Get Bulk Balances"() {

        def response = AURequestBuilder
                .buildBasicRequest(userAccessToken, AUConstants.X_V_HEADER_BALANCES)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_BALANCES))
                .get("${AUConstants.BULK_BALANCES_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Unattended Invocation count increase by 2 (API Request + Metrics Call of BeforeClass Step)
        unattendedCurrentDay = unattendedCurrentDay + 2

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test (priority = 1, dependsOnMethods = "Count increase with Get Transactions For Account invocation")
    void "Count increase with Get Transaction Detail invocation"() {

        String requestUrl = "${AUConstants.BULK_ACCOUNT_PATH}/${encryptedAccount1Id}/" +
                "transactions/$encryptedTransactionId"

        Response response = AURequestBuilder
                .buildBasicRequest(userAccessToken, AUConstants.X_V_HEADER_TRANSACTION)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_TRANSACTIONS))
                .get(requestUrl)

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Unattended Invocation count increase by 2 (API Request + Metrics Call of BeforeClass Step)
        unattendedCurrentDay = unattendedCurrentDay + 2

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test (priority = 2)
    void "Count increase with Get Payees invocation"() {

        String requestUrl = "${AUConstants.BULK_PAYEES}"

        Response response = AURequestBuilder
                .buildBasicRequest(userAccessToken, AUConstants.X_V_HEADER_PAYEES)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PAYEES))
                .get(requestUrl)

        encryptedPayeeId = AUTestUtil.parseResponseBody(response, "data.payees.payeeId[0]")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Unattended Invocation count increase by 2 (API Request + Metrics Call of BeforeClass Step)
        unattendedCurrentDay = unattendedCurrentDay + 2

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test (priority = 2, dependsOnMethods = "Count increase with Get Payees invocation")
    void "Count increase with Get Payee Detail invocation"() {

        String requestUrl = "${AUConstants.BULK_PAYEES}/${encryptedPayeeId}"

        Response response = AURequestBuilder
                .buildBasicRequest(userAccessToken, AUConstants.X_V_HEADER_PAYEES)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PAYEES))
                .get(requestUrl)

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Unattended Invocation count increase by 2 (API Request + Metrics Call of BeforeClass Step)
        unattendedCurrentDay = unattendedCurrentDay + 2

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test (priority = 3)
    void "Count increase when an Unattended request return an 401 error"() {

        def response = AURequestBuilder
                .buildBasicRequest(AUConstants.INCORRECT_ACCESS_TOKEN, AUConstants.X_V_HEADER_ACCOUNTS)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get("${AUConstants.BULK_ACCOUNT_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_401)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Unattended Invocation count increase by 2 (API Request + Metrics Call of BeforeClass Step)
        unattendedCurrentDay = unattendedCurrentDay + 2

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }
}

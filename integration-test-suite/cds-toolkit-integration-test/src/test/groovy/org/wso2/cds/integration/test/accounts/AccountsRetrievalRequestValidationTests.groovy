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

package org.wso2.cds.integration.test.accounts

import com.nimbusds.oauth2.sdk.AccessTokenResponse
import org.wso2.cds.test.framework.AUTest
import org.wso2.cds.test.framework.constant.AUAccountScope
import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.request_builder.AURequestBuilder
import org.wso2.cds.test.framework.utility.AURestAsRequestBuilder
import org.wso2.cds.test.framework.utility.AUTestUtil
import io.restassured.http.ContentType
import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.testng.asserts.SoftAssert

import java.nio.charset.Charset

/**
 * Test class for Accounts Retrieval Request Validation.
 */
class AccountsRetrievalRequestValidationTests extends AUTest {

    def clientHeader = "${Base64.encoder.encodeToString(getCDSClient().getBytes(Charset.defaultCharset()))}"
    def payeeId
    String firstEncryptedAccountId
    String secondEncryptedAccountId

    @BeforeClass(alwaysRun = true)
    void "Get User Access Token"() {
        doConsentAuthorisation()
        generateUserAccessToken()

        String bulkAccountRequestUrl = "${AUConstants.BULK_ACCOUNT_PATH}"

        def response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_ACCOUNTS, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get(bulkAccountRequestUrl)

        SoftAssert softAssertion= new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        firstEncryptedAccountId = AUTestUtil.parseResponseBody(response, "data.accounts.accountId[0]")
        secondEncryptedAccountId = AUTestUtil.parseResponseBody(response, "data.accounts.accountId[1]")
    }

    @Test
    void "TC0302001_Retrieve valid bulk balances"() {

        String requestBody = """
            {
              "data": {
                "accountIds": [
                  "${firstEncryptedAccountId}", "${secondEncryptedAccountId}"
                ]
              },
              "meta": {}
            }
        """.stripIndent()

        def response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_BALANCES, clientHeader)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_BALANCES))
                .post("${AUConstants.BULK_BALANCES_PATH}")

        SoftAssert softAssertion = new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        softAssertion.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_BALANCES)

        softAssertion.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response,
                "${AUConstants.RESPONSE_DATA_BULK_BALANCE_LIST}[0]"))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response,
                "${AUConstants.RESPONSE_DATA_BULK_BALANCE_LIST}[1]"))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_SELF))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_FIRST))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_PREV))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_NEXT))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_LAST))
        softAssertion.assertAll()
    }

    @Test(groups = "SmokeTest")
    void "TC0401002_Retrieve bulk direct debits"() {

        Response response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_DIRECT_DEBITS, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_DIRECT_DEBIT))
                .get("${AUConstants.BULK_DIRECT_DEBITS_PATH}")

        SoftAssert softAssertion = new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        softAssertion.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_DIRECT_DEBITS)

        softAssertion.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response,
                "${AUConstants.RESPONSE_DATA_DIRECT_DEBIT_AUTH}.accountId[0]"))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response,
                "${AUConstants.RESPONSE_DATA_DIRECT_DEBIT_AUTH}.authorisedEntity[0]"))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response,
                "${AUConstants.RESPONSE_DATA_DIRECT_DEBIT_AUTH}.lastDebitDateTime[0]"))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response,
                "${AUConstants.RESPONSE_DATA_DIRECT_DEBIT_AUTH}.lastDebitAmount[0]"))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response,
                "${AUConstants.RESPONSE_DATA_DIRECT_DEBIT_AUTH}.accountId[1]"))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response,
                "${AUConstants.RESPONSE_DATA_DIRECT_DEBIT_AUTH}.authorisedEntity[1]"))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response,
                "${AUConstants.RESPONSE_DATA_DIRECT_DEBIT_AUTH}.lastDebitDateTime[1]"))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response,
                "${AUConstants.RESPONSE_DATA_DIRECT_DEBIT_AUTH}.lastDebitAmount[1]"))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_SELF))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_FIRST))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_PREV))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_NEXT))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_LAST))
        softAssertion.assertAll()
    }

    @Test
    void "TC0401003_Get Direct Debits For Account"() {

        Response response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_DIRECT_DEBITS, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_DIRECT_DEBIT))
                .get("${AUConstants.BULK_ACCOUNT_PATH}/${AUConstants.accountID}/direct-debits")

        SoftAssert softAssertion = new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        softAssertion.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_DIRECT_DEBITS)

        softAssertion.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response,
                "${AUConstants.RESPONSE_DATA_DIRECT_DEBIT_AUTH}.accountId"))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response,
                "${AUConstants.RESPONSE_DATA_DIRECT_DEBIT_AUTH}.authorisedEntity"))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response,
                "${AUConstants.RESPONSE_DATA_DIRECT_DEBIT_AUTH}.lastDebitDateTime"))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response,
                "${AUConstants.RESPONSE_DATA_DIRECT_DEBIT_AUTH}.lastDebitAmount"))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_SELF))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_FIRST))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_PREV))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_NEXT))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_LAST))
        softAssertion.assertAll()
    }

    @Test
    void "TC0402001_Retrieve Scheduled Payments For Specific Accounts"() {

        String requestBody = """
            {
              "data": {
                "accountIds": [
                    "${AUConstants.accountID}", "${AUConstants.accountID2}"
                ]
              },
              "meta": {}
            }
        """.stripIndent()

        def response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_PAYMENT_SCHEDULED, clientHeader)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_SCHEDULED_PAYMENT))
                .post("${AUConstants.BULK_SCHEDULE_PAYMENTS_PATH}")

        SoftAssert softAssertion = new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        softAssertion.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_PAYMENT_SCHEDULED)

        softAssertion.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response,
                "${AUConstants.RESPONSE_DATA_SCHEDULE_PAY}.scheduledPaymentId"))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response,
                "${AUConstants.RESPONSE_DATA_SCHEDULE_PAY}.status"))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response,
                "${AUConstants.RESPONSE_DATA_SCHEDULE_PAY}.paymentSet"))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response,
                "${AUConstants.RESPONSE_DATA_SCHEDULE_PAY}.recurrence"))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_SELF))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_FIRST))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_PREV))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_NEXT))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_LAST))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.META))
        softAssertion.assertAll()
    }

    @Test(groups = "SmokeTest")
    void "TC0402002_Retrieve bulk Scheduled Payments"() {

        Response response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_PAYMENT_SCHEDULED, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_SCHEDULED_PAYMENT))
                .get("${AUConstants.BULK_SCHEDULE_PAYMENTS_PATH}")

        SoftAssert softAssertion = new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        softAssertion.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_PAYMENT_SCHEDULED)

        softAssertion.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response,
                "${AUConstants.RESPONSE_DATA_SCHEDULE_PAY}.scheduledPaymentId[0]"))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response,
                "${AUConstants.RESPONSE_DATA_SCHEDULE_PAY}.payerReference[0]"))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response,
                "${AUConstants.RESPONSE_DATA_SCHEDULE_PAY}.status[0]"))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response,
                "${AUConstants.RESPONSE_DATA_SCHEDULE_PAY}.paymentSet[0]"))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response,
                "${AUConstants.RESPONSE_DATA_SCHEDULE_PAY}.recurrence[0]"))

        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response,
                "${AUConstants.RESPONSE_DATA_SCHEDULE_PAY}.scheduledPaymentId[1]"))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response,
                "${AUConstants.RESPONSE_DATA_SCHEDULE_PAY}.payerReference[1]"))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response,
                "${AUConstants.RESPONSE_DATA_SCHEDULE_PAY}.status[1]"))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response,
                "${AUConstants.RESPONSE_DATA_SCHEDULE_PAY}.paymentSet[1]"))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response,
                "${AUConstants.RESPONSE_DATA_SCHEDULE_PAY}.recurrence[1]"))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_SELF))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_FIRST))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_PREV))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_NEXT))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_LAST))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.META))
        softAssertion.assertAll()
    }

    @Test
    void "TC0402003_Get Scheduled Payments For Account"() {

        Response response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_PAYMENT_SCHEDULED, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_SCHEDULED_PAYMENT))
                .get("${AUConstants.BULK_ACCOUNT_PATH}/${AUConstants.accountID}/payments/scheduled")

        SoftAssert softAssertion = new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        softAssertion.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_PAYMENT_SCHEDULED)

        softAssertion.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response,
                "${AUConstants.RESPONSE_DATA_SCHEDULE_PAY}.scheduledPaymentId"))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response,
                "${AUConstants.RESPONSE_DATA_SCHEDULE_PAY}.payerReference"))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response,
                "${AUConstants.RESPONSE_DATA_SCHEDULE_PAY}.status"))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response,
                "${AUConstants.RESPONSE_DATA_SCHEDULE_PAY}.paymentSet"))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response,
                "${AUConstants.RESPONSE_DATA_SCHEDULE_PAY}.recurrence"))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_SELF))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_FIRST))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_PREV))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_NEXT))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_LAST))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.META))
        softAssertion.assertAll()
    }

    @Test(groups = "SmokeTest", priority = 2)
    void "TC0503001_Get Payees"() {

        Response response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_PAYEES, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PAYEES))
                .get("${AUConstants.BULK_PAYEES}")

        payeeId = AUTestUtil.parseResponseBody(response, "data.payees.payeeId[0]")

        SoftAssert softAssertion = new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        softAssertion.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_PAYEES)

        softAssertion.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response,
                "${AUConstants.RESPONSE_DATA_PAYEE}.payeeId"))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response,
                "${AUConstants.RESPONSE_DATA_PAYEE}.type"))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response,
                "${AUConstants.RESPONSE_DATA_PAYEE}.creationDate"))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_SELF))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_FIRST))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_PREV))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_NEXT))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_LAST))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.META))
        softAssertion.assertAll()
    }

    @Test(dependsOnMethods = "TC0503001_Get Payees", priority = 2)
    void "TC0503002_Get Payee Detail"() {

        Response response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_PAYEES, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PAYEES))
                .get("${AUConstants.BULK_PAYEES}/${payeeId}")

        SoftAssert softAssertion = new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        softAssertion.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_PAYEES)

        softAssertion.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_SELF))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.META))
        softAssertion.assertAll()
    }

    @Test(priority = 3)
    void "OB-1146_Get basic accounts data without bank accounts basic read permissions"() {

        scopes = [
                AUAccountScope.BANK_ACCOUNT_DETAIL_READ,
                AUAccountScope.BANK_TRANSACTION_READ,
                AUAccountScope.BANK_PAYEES_READ,
                AUAccountScope.BANK_REGULAR_PAYMENTS_READ,
                AUAccountScope.BANK_CUSTOMER_BASIC_READ,
                AUAccountScope.BANK_CUSTOMER_DETAIL_READ
        ]

        doConsentAuthorisation()
        generateUserAccessToken()

        Response response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_ACCOUNTS, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BULK_ACCOUNT_PATH))
                .get("${AUConstants.BULK_ACCOUNT_PATH}")

        SoftAssert softAssertion = new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_403)
        softAssertion.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_ACCOUNTS)
        softAssertion.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION),
                AUConstants.ERROR_NOT_ALLOWED_TO_ACCESS)
        softAssertion.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR),
                AUConstants.INSUFFICIENT_SCOPE)
        softAssertion.assertAll()
    }

    @Test(priority = 3)
    void "OB-1147_Get detailed account data without bank accounts detail read permissions"() {

        scopes = [
                AUAccountScope.BANK_ACCOUNT_BASIC_READ,
                AUAccountScope.BANK_TRANSACTION_READ,
                AUAccountScope.BANK_PAYEES_READ,
                AUAccountScope.BANK_REGULAR_PAYMENTS_READ,
                AUAccountScope.BANK_CUSTOMER_BASIC_READ,
                AUAccountScope.BANK_CUSTOMER_DETAIL_READ
        ]
        doConsentAuthorisation()
        generateUserAccessToken()

        Response response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_ACCOUNT, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.SINGLE_ACCOUNT_PATH))
                .get("${AUConstants.SINGLE_ACCOUNT_PATH}")

        SoftAssert softAssertion = new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_403)
        softAssertion.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_ACCOUNT)
        softAssertion.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION),
                AUConstants.ERROR_NOT_ALLOWED_TO_ACCESS)
        softAssertion.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR),
                AUConstants.INSUFFICIENT_SCOPE)
        softAssertion.assertAll()
    }

    @Test(priority = 3)
    void "OB-1148_Get transactions data without bank transactions read permission"() {

        scopes = [
                AUAccountScope.BANK_ACCOUNT_BASIC_READ,
                AUAccountScope.BANK_ACCOUNT_DETAIL_READ,
                AUAccountScope.BANK_PAYEES_READ,
                AUAccountScope.BANK_REGULAR_PAYMENTS_READ,
                AUAccountScope.BANK_CUSTOMER_BASIC_READ,
                AUAccountScope.BANK_CUSTOMER_DETAIL_READ
        ]

        doConsentAuthorisation()
        generateUserAccessToken()

        Response response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_TRANSACTIONS, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.SINGLE_ACCOUNT_PATH))
                .get("${AUConstants.GET_TRANSACTIONS}")

        SoftAssert softAssertion = new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_403)
        softAssertion.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_TRANSACTIONS)
        softAssertion.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION),
                AUConstants.ERROR_NOT_ALLOWED_TO_ACCESS)
        softAssertion.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR),
                AUConstants.INSUFFICIENT_SCOPE)
        softAssertion.assertAll()
    }

    @Test(priority = 3)
    void "OB-1149_Get transactions data with bank transactions read and without bank accounts basic read permission"() {

        scopes = [
                AUAccountScope.BANK_ACCOUNT_DETAIL_READ,
                AUAccountScope.BANK_TRANSACTION_READ,
                AUAccountScope.BANK_PAYEES_READ,
                AUAccountScope.BANK_REGULAR_PAYMENTS_READ,
                AUAccountScope.BANK_CUSTOMER_BASIC_READ,
                AUAccountScope.BANK_CUSTOMER_DETAIL_READ
        ]

        doConsentAuthorisation()
        generateUserAccessToken()

        Response response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_TRANSACTIONS, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.SINGLE_ACCOUNT_PATH))
                .get("${AUConstants.GET_TRANSACTIONS}")

        SoftAssert softAssertion = new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        softAssertion.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_TRANSACTIONS)
        softAssertion.assertTrue(response.getHeader(AUConstants.CONTENT_TYPE).contains(AUConstants.ACCEPT))

        softAssertion.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, "${AUConstants.RESPONSE_DATA_TRANSACTION_LIST}.accountId"))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_SELF))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_FIRST))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_PREV))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_NEXT))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_LAST))
        softAssertion.assertAll()
    }

    @Test(priority = 3)
    void "TC0401004_Get Direct Debits without regular_payments:read permissions"() {

        scopes = [
                AUAccountScope.BANK_ACCOUNT_BASIC_READ,
                AUAccountScope.BANK_ACCOUNT_DETAIL_READ,
                AUAccountScope.BANK_TRANSACTION_READ
        ]

        doConsentAuthorisation()
        generateUserAccessToken()

        Response response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_DIRECT_DEBITS, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_DIRECT_DEBIT))
                .get("${AUConstants.BULK_DIRECT_DEBITS_PATH}")

        SoftAssert softAssertion = new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_403)
        softAssertion.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_DIRECT_DEBITS)
        softAssertion.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION),
                AUConstants.ERROR_NOT_ALLOWED_TO_ACCESS)
        softAssertion.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR),
                AUConstants.INSUFFICIENT_SCOPE)
        softAssertion.assertAll()
    }

    @Test(priority = 3)
    void "TC0402004_Get Scheduled Payments without regular_payments:read permissions"() {

        scopes = [
                AUAccountScope.BANK_ACCOUNT_BASIC_READ,
                AUAccountScope.BANK_ACCOUNT_DETAIL_READ,
                AUAccountScope.BANK_TRANSACTION_READ
        ]

        doConsentAuthorisation()
        generateUserAccessToken()

        Response response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_PAYMENT_SCHEDULED, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_SCHEDULED_PAYMENT))
                .get("${AUConstants.BULK_SCHEDULE_PAYMENTS_PATH}")

        SoftAssert softAssertion = new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_403)
        softAssertion.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_PAYMENT_SCHEDULED)
        softAssertion.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION),
                AUConstants.ERROR_NOT_ALLOWED_TO_ACCESS)
        softAssertion.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR),
                AUConstants.INSUFFICIENT_SCOPE)
        softAssertion.assertAll()
    }

    @Test(priority = 3)
    void "TC0503003_Get Payee without payees:read permissions"() {

        scopes = [
                AUAccountScope.BANK_ACCOUNT_BASIC_READ,
                AUAccountScope.BANK_ACCOUNT_DETAIL_READ,
                AUAccountScope.BANK_TRANSACTION_READ
        ]

        doConsentAuthorisation()
        generateUserAccessToken()

        Response response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_PAYEES, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PAYEES))
                .get("${AUConstants.BULK_PAYEES}")

        SoftAssert softAssertion = new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_403)
        softAssertion.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_PAYEES)
        softAssertion.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION),
                AUConstants.ERROR_NOT_ALLOWED_TO_ACCESS)
        softAssertion.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR),
                AUConstants.INSUFFICIENT_SCOPE)
        softAssertion.assertAll()
    }

    @Test
    void "OB-1258_Retrieve account list with invalid base path"() {

        def response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_ACCOUNTS, clientHeader)
                .accept(AUConstants.ACCEPT)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get("/cds-au/v0${AUConstants.BULK_ACCOUNT_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_404)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_CODE),
                AUConstants.ERROR_CODE_RESOURCE_NOTFOUND)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_TITLE),
                AUConstants.RESOURCE_NOT_FOUND)
    }

    @Test
    void "OB-1259_Retrieve account list with invalid sub path"() {

        def response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_ACCOUNTS, clientHeader)
                .accept(AUConstants.ACCEPT)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get("${AUConstants.CDS_PATH}/banking/accountz")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_404)
    }

    @Test
    void "OB-1260_Retrieve account list with invalid http method"() {

        def response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_ACCOUNTS, clientHeader)
                .accept(AUConstants.ACCEPT)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .put("${AUConstants.BULK_ACCOUNT_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_405)
    }

    @Test
    void "OB-1261_Retrieve account list without mtls client certificate"() {

        def response = AURestAsRequestBuilder.buildBasicRequest()
                .header(AUConstants.X_V_HEADER, AUConstants.X_V_HEADER_ACCOUNTS)
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, "${AUConstants.AUTHORIZATION_BEARER_TAG}${userAccessToken}")
                .header(AUConstants.X_FAPI_AUTH_DATE, AUConstants.DATE)
                .header(AUConstants.X_CDS_CLIENT_HEADERS , clientHeader)
                .accept(AUConstants.ACCEPT)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get("${AUConstants.BULK_ACCOUNT_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_401)
        Assert.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.ERROR).contains(
                AUConstants.INVALID_CLIENT))
        Assert.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION).contains(
                "Invalid mutual TLS request. Client certificate is missing"))
    }

    @Test (priority = 4)
    void "OB-1162_Invoke bulk balances POST without request body"() {

        doConsentAuthorisation()
        generateUserAccessToken()

        def response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_BALANCES, clientHeader)
                .contentType(ContentType.JSON)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_BALANCES))
                .post("${AUConstants.BULK_BALANCES_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)

        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_CODE),
                AUConstants.ERROR_CODE_MISSING_FIELD)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DETAIL),
                "Missing Required Field request body in the request")
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_TITLE), AUConstants.MISSING_FIELD)
        Assert.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))
    }

    @Test
    void "OB-1263_Invoke bulk balances POST with invalid request body"() {

        // sending 'accountIds' as a string instead of the mandated String array format
        String requestBody = """
            {
              "data": {
                "accountIds": "${AUConstants.accountID}",
                "accountIds": "${AUConstants.accountID}"
                },
              "meta": {}
            }
        """.stripIndent()

        def response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_BALANCES, clientHeader)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_BALANCES))
                .post("${AUConstants.BULK_BALANCES_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)

        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_CODE),
                AUConstants.ERROR_CODE_INVALID_FIELD)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_TITLE), AUConstants.INVALID_FIELD)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DETAIL),
                "Invalid Field accountIds found in the request")

        Assert.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))
    }

    @Test(groups = "SmokeTest")
    void "TC0301001_Retrieve bulk accounts list"() {

        def response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_ACCOUNTS, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get("${AUConstants.BULK_ACCOUNT_PATH}")

        SoftAssert softAssertion = new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        softAssertion.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_ACCOUNTS)
        softAssertion.assertTrue(response.getHeader(AUConstants.CONTENT_TYPE).contains(AUConstants.ACCEPT))

        softAssertion.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response,
                "${AUConstants.RESPONSE_DATA_BULK_ACCOUNTID_LIST}[0]"))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response,
                "${AUConstants.RESPONSE_DATA_BULK_ACCOUNTID_LIST}[1]"))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_SELF))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_FIRST))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_PREV))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_NEXT))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_LAST))
        softAssertion.assertAll()
    }

    @Test
    void "TC0301002_Retrieve consented single accounts"() {

        def response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_ACCOUNT, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get("${AUConstants.BULK_ACCOUNT_PATH}/${AUConstants.accountID}")

        SoftAssert softAssertion = new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        softAssertion.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_ACCOUNT)

        softAssertion.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.RESPONSE_DATA_SINGLE_ACCOUNTID))
        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_SELF))
        softAssertion.assertAll()
    }

    //TODO:Enable the account id validation in IAM
    @Test
    void "TC0301003_Retrieve invalid single accounts"() {

        def response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_ACCOUNT, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get("${AUConstants.BULK_ACCOUNT_PATH}/11059970")

        SoftAssert softAssertion = new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_404)
        softAssertion.assertNotNull(response.getHeader(AUConstants.X_V_HEADER))
        softAssertion.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))
        softAssertion.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_CODE),
                AUConstants.ERROR_CODE_INVALID_BANK_ACC)
    }

    @Test
    void "OB-1702_Invoke bulk balances POST with empty accountId list"() {

        String requestBody = """
            {
              "data": {
                "accountIds": []
              },
              "meta": {}
            }
        """.stripIndent()

        def response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_BALANCES, clientHeader)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_BALANCES))
                .post("${AUConstants.BULK_BALANCES_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_422)

        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_CODE),
                AUConstants.ERROR_CODE_INVALID_BANK_ACC)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DETAIL),
                AUConstants.ACCOUNT_ID_NOT_FOUND)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_TITLE),
                AUConstants.INVALID_BANK_ACC)
        Assert.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))
    }

    @Test
    void "OB-1703_Invoke bulk balances POST with missing required field"() {

        // sending 'accountIds' as a string instead of the mandated String array format
        String requestBody = """
            {
              "data": {
              },
              "meta": {}
            }
        """.stripIndent()

        def response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_BALANCES, clientHeader)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_BALANCES))
                .post("${AUConstants.BULK_BALANCES_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)

        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_CODE),
                AUConstants.ERROR_CODE_MISSING_FIELD)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DETAIL),
                "accountIds field is missing in the request")
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_TITLE),
                AUConstants.MISSING_FIELD)
        Assert.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))
    }

    @Test (priority = 4)
    void "CDS-680_Send token request with same authorisation code"() {

        auConfiguration.setTppNumber(0)
        //Generate user access token from auth code for the first time
        doConsentAuthorisation()
        AccessTokenResponse accessTokenResponse = getUserAccessTokenResponse(clientId)
        userAccessToken = accessTokenResponse.tokens.accessToken
        def refreshToken = accessTokenResponse.tokens.refreshToken
        Assert.assertNotNull(userAccessToken)
        Assert.assertNotNull(refreshToken)

        //Account Retrieval
        String bulkAccountRequestUrl = "${AUConstants.BULK_ACCOUNT_PATH}"

        def response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_ACCOUNTS, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get(bulkAccountRequestUrl)

        SoftAssert softAssertion= new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //Try to generate user access token from the same auth code used before
        def userToken = AURequestBuilder.getUserTokenErrorResponse(authorisationCode,
                auConfiguration.getAppInfoRedirectURL(), auConfiguration.getAppInfoClientID())

        Assert.assertEquals(userToken.error.httpStatusCode, AUConstants.BAD_REQUEST)
        Assert.assertEquals(userToken.error.code, AUConstants.INVALID_GRANT)
        Assert.assertEquals(userToken.error.description, "Inactive authorization code received from token request")

        sleep(60000)

        //Try Account Retrieval Request Again
        def responseSecondAttempt = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_ACCOUNTS, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get(bulkAccountRequestUrl)

        Assert.assertEquals(responseSecondAttempt.statusCode(), AUConstants.STATUS_CODE_401)
        Assert.assertEquals(AUTestUtil.parseResponseBody(responseSecondAttempt, AUConstants.ERROR_DESCRIPTION),
                AUConstants.INVALID_CREDENTIALS)

        //Token introspection request
        def introspectResponse = AURequestBuilder.buildIntrospectionRequest(refreshToken.toString(),
                auConfiguration.getAppInfoClientID(), 0)
                .post(AUConstants.INTROSPECTION_ENDPOINT)

        //Introspection validation can only be done for refresh token
        Assert.assertTrue(introspectResponse.jsonPath().get("active").equals(false))
    }
}

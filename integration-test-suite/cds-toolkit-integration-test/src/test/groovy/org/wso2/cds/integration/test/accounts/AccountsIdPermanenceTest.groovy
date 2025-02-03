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

import org.wso2.cds.test.framework.AUTest
import org.wso2.cds.test.framework.constant.AUAccountProfile
import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.request_builder.AURequestBuilder
import org.wso2.cds.test.framework.utility.AUIdEncryptorDecryptor
import org.wso2.cds.test.framework.utility.AUTestUtil
import io.restassured.http.ContentType
import io.restassured.response.Response
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.testng.asserts.SoftAssert

import java.nio.charset.Charset

/**
 * Test class for Accounts Id Permanence.
 */
class AccountsIdPermanenceTest extends AUTest {

    def clientHeader = "${Base64.encoder.encodeToString(getCDSClient().getBytes(Charset.defaultCharset()))}"
    String firstEncryptedAccountId
    String secondEncryptedAccountId
    String encryptedTransactionId
    String encryptedPayeeId
    private String secretKey = auConfiguration.getIDPermanence()
    private String userId = auConfiguration.getUserPSUName() + "@" + auConfiguration.getCommonTenantDomain()

    @BeforeClass(alwaysRun = true)
    void "Get User Access Token"() {
        doConsentAuthorisation()
        generateUserAccessToken()
    }

    @Test(groups = "SmokeTest", priority = 1)
    void "TC1201001_Get Accounts"() {

        String bulkAccountRequestUrl = "${AUConstants.BULK_ACCOUNT_PATH}"

        def response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_ACCOUNTS, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get(bulkAccountRequestUrl)

        SoftAssert softAssertion= new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        softAssertion.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))

        firstEncryptedAccountId = AUTestUtil.parseResponseBody(response, "data.accounts.accountId[0]")
        secondEncryptedAccountId = AUTestUtil.parseResponseBody(response, "data.accounts.accountId[1]")

        softAssertion.assertTrue(AUIdEncryptorDecryptor.decrypt(
                AUTestUtil.parseResponseBody(response, "${AUConstants.RESPONSE_DATA_BULK_ACCOUNTID_LIST}[0]"), secretKey).
                split(":")[2] != "")
        softAssertion.assertTrue(AUIdEncryptorDecryptor.decrypt(
                AUTestUtil.parseResponseBody(response, "${AUConstants.RESPONSE_DATA_BULK_ACCOUNTID_LIST}[1]"), secretKey).
                split(":")[2] != "")

        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_SELF)
                .contains(bulkAccountRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_FIRST)
                .contains(bulkAccountRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_PREV)
                .contains(bulkAccountRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_NEXT)
                .contains(bulkAccountRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_LAST)
                .contains(bulkAccountRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertAll()
    }

    @Test (groups = "SmokeTest", dependsOnMethods = "TC1201001_Get Accounts", priority = 1)
    void "TC1203001_Get Balances For Specific Accounts"() {

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

        String bulkBalanceRequestUrl = "${AUConstants.BULK_BALANCES_PATH}"

        def response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_BALANCES, clientHeader)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_BALANCES))
                .post(bulkBalanceRequestUrl)

        SoftAssert softAssertion= new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        softAssertion.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))

        softAssertion.assertNotNull(AUIdEncryptorDecryptor.decrypt(
                AUTestUtil.parseResponseBody(response, "${AUConstants.RESPONSE_DATA_BULK_BALANCE_LIST}[0]"), secretKey).
                split(":")[2])
        softAssertion.assertNotNull(AUIdEncryptorDecryptor.decrypt(
                AUTestUtil.parseResponseBody(response, "${AUConstants.RESPONSE_DATA_BULK_BALANCE_LIST}[1]"), secretKey).
                split(":")[2])

        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_SELF)
                .contains(bulkBalanceRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_FIRST)
                .contains(bulkBalanceRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_PREV)
                .contains(bulkBalanceRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_NEXT)
                .contains(bulkBalanceRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_LAST)
                .contains(bulkBalanceRequestUrl.split(AUConstants.CDS_PATH)[1]))

        softAssertion.assertAll()
    }

    @Test (groups = "SmokeTest", dependsOnMethods = "TC1201001_Get Accounts", priority = 1)
    void "TC1204001_Get Account Balance"() {

        String accBalanceRequestUrl = "${AUConstants.BULK_ACCOUNT_PATH}/${firstEncryptedAccountId}/balance"

        def response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_BALANCE, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_BALANCES))
                .get(accBalanceRequestUrl)

        SoftAssert softAssertion= new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        softAssertion.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))
        softAssertion.assertNotNull(AUIdEncryptorDecryptor.decrypt(
                AUTestUtil.parseResponseBody(response, AUConstants.RESPONSE_DATA_SINGLE_ACCOUNTID), secretKey).
                split(":")[2])
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_SELF)
                .contains(accBalanceRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertAll()
    }

    @Test (groups = "SmokeTest", dependsOnMethods = "TC1201001_Get Accounts", priority = 1)
    void "TC1205001_Get Account Detail"() {

        String accountRequestUrl = "${AUConstants.BULK_ACCOUNT_PATH}/${firstEncryptedAccountId}"

        def response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_ACCOUNT, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get(accountRequestUrl)

        SoftAssert softAssertion= new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        softAssertion.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))

        softAssertion.assertNotNull(AUIdEncryptorDecryptor.decrypt(
                AUTestUtil.parseResponseBody(response, "data.accountId"), secretKey).
                split(":")[2])

        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_SELF)
                .contains(accountRequestUrl.split(AUConstants.CDS_PATH)[1]))

        softAssertion.assertNotNull(AUTestUtil.parseResponseBody(response, "data.loan.offsetAccountIds"))

        softAssertion.assertAll()
    }

    @Test (dependsOnMethods = "TC1201001_Get Accounts", priority = 1)
    void "TC1205003_Get Not Consented Account Details"() {

        auConfiguration.setTppNumber(0)
        clientId = auConfiguration.getAppInfoClientID()

        //Authorise consent by selecting single account
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        doConsentAuthorisationViaRequestUriSingleAccount(scopes, requestUri.toURI(), clientId, AUAccountProfile.INDIVIDUAL)
        def userAccessToken2 = getUserAccessTokenResponse(clientId).tokens.accessToken

        SoftAssert softAssertion= new SoftAssert()

        def response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken2.toString(),
                AUConstants.X_V_HEADER_ACCOUNT, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get("${AUConstants.BULK_ACCOUNT_PATH}/${AUConstants.accountID2}")

        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_404)
        softAssertion.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))

        softAssertion.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_CODE),
                AUConstants.ERROR_CODE_INVALID_BANK_ACC)
        softAssertion.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_TITLE),
                AUConstants.INVALID_BANK_ACC)
        softAssertion.assertAll()
    }

    @Test (dependsOnMethods = "TC1201001_Get Accounts", priority = 1)
    void "TC1207002_Get Invalid Transaction Detail"() {

        Response response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_TRANSACTION, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_TRANSACTIONS))
                .get("${AUConstants.BULK_ACCOUNT_PATH}/${firstEncryptedAccountId}/" +
                        "transactions/204987583920")

        SoftAssert softAssertion= new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_404)
        softAssertion.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))

       softAssertion.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_CODE),
               AUConstants.ERROR_CODE_INVALID_RESOURCE)
        softAssertion.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_TITLE),
                AUConstants.INVALID_RESOURCE)
        softAssertion.assertAll()
    }

    @Test (groups = "SmokeTest", dependsOnMethods = "TC1201001_Get Accounts", priority = 1)
    void "TC1208001_Get Direct Debits For Account"() {

        String directDebitRequestUrl = "${AUConstants.BULK_ACCOUNT_PATH}/${firstEncryptedAccountId}/direct-debits"

        Response response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_DIRECT_DEBITS, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_DIRECT_DEBIT))
                .get(directDebitRequestUrl)

        SoftAssert softAssertion= new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        softAssertion.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))

        softAssertion.assertNotNull(AUIdEncryptorDecryptor.decrypt(
                AUTestUtil.parseResponseBody(response, "data.directDebitAuthorisations.accountId[0]"), secretKey).
                split(":")[2])

        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_SELF)
                .contains(directDebitRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_FIRST)
                .contains(directDebitRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_PREV)
                .contains(directDebitRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_NEXT)
                .contains(directDebitRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_LAST)
                .contains(directDebitRequestUrl.split(AUConstants.CDS_PATH)[1]))

        softAssertion.assertAll()
    }

    @Test (dependsOnMethods = "TC1201001_Get Accounts", priority = 1)
    void "TC1208002_Get Direct Debits For Invalid Account"() {

        Response response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_DIRECT_DEBITS, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_DIRECT_DEBIT))
                .get("${AUConstants.BULK_ACCOUNT_PATH}/2349679635270/direct-debits")

        SoftAssert softAssertion= new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_404)
        softAssertion.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))

        softAssertion.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_CODE),
                AUConstants.ERROR_CODE_INVALID_BANK_ACC)
        softAssertion.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_TITLE),
                AUConstants.INVALID_BANK_ACC)

        softAssertion.assertAll()
    }

    @Test (groups = "SmokeTest", dependsOnMethods = "TC1201001_Get Accounts", priority = 1)
    void "TC1210001_Get Direct Debits For Specific Accounts"() {

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

        String directDebitRequestUrl = "${AUConstants.BULK_DIRECT_DEBITS_PATH}"

        def response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_DIRECT_DEBITS, clientHeader)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_DIRECT_DEBIT))
                .post(directDebitRequestUrl)

        SoftAssert softAssertion= new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        softAssertion.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))

        softAssertion.assertNotNull(AUIdEncryptorDecryptor.decrypt(
                AUTestUtil.parseResponseBody(response, "${AUConstants.RESPONSE_DATA_DIRECT_DEBIT_AUTH}.accountId[0]"), secretKey).
                split(":")[2])

        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_SELF)
                .contains(directDebitRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_FIRST)
                .contains(directDebitRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_PREV)
                .contains(directDebitRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_NEXT)
                .contains(directDebitRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_LAST)
                .contains(directDebitRequestUrl.split(AUConstants.CDS_PATH)[1]))

        softAssertion.assertAll()
    }

    @Test (groups = "SmokeTest", dependsOnMethods = "TC1201001_Get Accounts", priority = 1)
    void "TC1211001_Get Scheduled Payments for Account"() {

        String schedulePaymentRequestUrl = "${AUConstants.BULK_ACCOUNT_PATH}/${firstEncryptedAccountId}/payments/scheduled"

        Response response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_PAYMENT_SCHEDULED, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_SCHEDULED_PAYMENT))
                .get(schedulePaymentRequestUrl)

        SoftAssert softAssertion= new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        softAssertion.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))

        softAssertion.assertEquals(userId, AUIdEncryptorDecryptor.decrypt(
                AUTestUtil.parseResponseBody(response,
                        "${AUConstants.RESPONSE_DATA_SCHEDULE_PAY}.scheduledPaymentId[0]"), secretKey)
                .split(":")[0])
        softAssertion.assertNotNull(AUIdEncryptorDecryptor.decrypt(
                AUTestUtil.parseResponseBody(response,
                        "${AUConstants.RESPONSE_DATA_SCHEDULE_PAY}.from.accountId[0]"), secretKey)
                .split(":")[2])
        softAssertion.assertEquals(userId, AUIdEncryptorDecryptor.decrypt(
                AUTestUtil.parseResponseBody(response,
                        "${AUConstants.RESPONSE_DATA_SCHEDULE_PAY}.paymentSet[0].to.accountId[0]"), secretKey)
                .split(":")[0])
        softAssertion.assertEquals(userId, AUIdEncryptorDecryptor.decrypt(
                AUTestUtil.parseResponseBody(response,
                        "${AUConstants.RESPONSE_DATA_SCHEDULE_PAY}.paymentSet[0].to.payeeId[0]"), secretKey)
                .split(":")[0])

        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_SELF)
                .contains(schedulePaymentRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_FIRST)
                .contains(schedulePaymentRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_PREV)
                .contains(schedulePaymentRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_NEXT)
                .contains(schedulePaymentRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_LAST)
                .contains(schedulePaymentRequestUrl.split(AUConstants.CDS_PATH)[1]))

        softAssertion.assertAll()
    }

    @Test (groups = "SmokeTest", dependsOnMethods = "TC1201001_Get Accounts", priority = 1)
    void "TC1213001_Get Scheduled Payments For Specific Accounts"() {

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

        String schedulePaymentRequestUrl = "${AUConstants.BULK_SCHEDULE_PAYMENTS_PATH}"

        def response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_PAYMENT_SCHEDULED, clientHeader)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_SCHEDULED_PAYMENT))
                .post(schedulePaymentRequestUrl)

        SoftAssert softAssertion= new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        softAssertion.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))

        softAssertion.assertEquals(userId, AUIdEncryptorDecryptor.decrypt(
                AUTestUtil.parseResponseBody(response,
                        "${AUConstants.RESPONSE_DATA_SCHEDULE_PAY}.scheduledPaymentId[0]"), secretKey)
                .split(":")[0])
        softAssertion.assertNotNull(AUIdEncryptorDecryptor.decrypt(
                AUTestUtil.parseResponseBody(response,
                        "${AUConstants.RESPONSE_DATA_SCHEDULE_PAY}.from.accountId[0]"), secretKey)
                .split(":")[2])
        softAssertion.assertEquals(userId, AUIdEncryptorDecryptor.decrypt(
                AUTestUtil.parseResponseBody(response,
                        "${AUConstants.RESPONSE_DATA_SCHEDULE_PAY}.paymentSet[0].to.accountId[0]"), secretKey)
                .split(":")[0])
        softAssertion.assertEquals(userId, AUIdEncryptorDecryptor.decrypt(
                AUTestUtil.parseResponseBody(response,
                        "${AUConstants.RESPONSE_DATA_SCHEDULE_PAY}.paymentSet[0].to.payeeId[0]"), secretKey)
                .split(":")[0])

        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_SELF)
                .contains(schedulePaymentRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_FIRST)
                .contains(schedulePaymentRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_PREV)
                .contains(schedulePaymentRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_NEXT)
                .contains(schedulePaymentRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_LAST)
                .contains(schedulePaymentRequestUrl.split(AUConstants.CDS_PATH)[1]))

        softAssertion.assertAll()
    }

    @Test (groups = "SmokeTest", dependsOnMethods = "TC1201001_Get Accounts", priority = 1)
    void "TC1206001_Get Transactions For Account"() {

        String transactionRequestUrl = "${AUConstants.BULK_ACCOUNT_PATH}/${firstEncryptedAccountId}/transactions"

        Response response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_TRANSACTIONS, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_TRANSACTIONS))
                .get(transactionRequestUrl)

        encryptedTransactionId = AUTestUtil.parseResponseBody(response, "${AUConstants.RESPONSE_DATA_TRANSACTION_LIST}.transactionId[0]")

        SoftAssert softAssertion= new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        softAssertion.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))

        softAssertion.assertNotNull(AUIdEncryptorDecryptor.decrypt(
                AUTestUtil.parseResponseBody(response, "${AUConstants.RESPONSE_DATA_TRANSACTION_LIST}.accountId[0]"), secretKey).
                split(":")[2])
        softAssertion.assertEquals(userId, AUIdEncryptorDecryptor.decrypt(encryptedTransactionId, secretKey).
                split(":")[0])

        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_SELF)
                .contains(transactionRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_FIRST)
                .contains(transactionRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_PREV)
                .contains(transactionRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_NEXT)
                .contains(transactionRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_LAST)
                .contains(transactionRequestUrl.split(AUConstants.CDS_PATH)[1]))

        softAssertion.assertAll()
    }

    @Test (groups = "SmokeTest", dependsOnMethods = "TC1206001_Get Transactions For Account", priority = 1)
    void "TC1207001_Get Transaction Detail"() {

        String transactionRequestUrl = "${AUConstants.BULK_ACCOUNT_PATH}/${firstEncryptedAccountId}/" +
                "transactions/$encryptedTransactionId"

        Response response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_TRANSACTION, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_TRANSACTIONS))
                .get(transactionRequestUrl)

        SoftAssert softAssertion= new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        softAssertion.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))

        softAssertion.assertNotNull(AUIdEncryptorDecryptor.decrypt(
                AUTestUtil.parseResponseBody(response, AUConstants.RESPONSE_DATA_SINGLE_ACCOUNTID), secretKey).
                split(":")[2])
        softAssertion.assertEquals(userId, AUIdEncryptorDecryptor.decrypt(
                AUTestUtil.parseResponseBody(response, AUConstants.RESPONSE_DATA_TRANSACTIONID), secretKey).
                split(":")[0])

        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_SELF)
                .contains(transactionRequestUrl.split(AUConstants.CDS_PATH)[1]))

        softAssertion.assertAll()
    }

    @Test (groups = "SmokeTest")
    void "TC1202001_Get Bulk Balances"() {

        String balanceRequestUrl = "${AUConstants.BULK_BALANCES_PATH}"

        def response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_BALANCES, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_BALANCES))
                .get(balanceRequestUrl)

        SoftAssert softAssertion= new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        softAssertion.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))

        softAssertion.assertTrue(AUIdEncryptorDecryptor.decrypt(
                AUTestUtil.parseResponseBody(response, "${AUConstants.RESPONSE_DATA_BULK_BALANCE_LIST}[0]"), secretKey).
                split(":")[2] != "")
        softAssertion.assertTrue(AUIdEncryptorDecryptor.decrypt(
                AUTestUtil.parseResponseBody(response, "${AUConstants.RESPONSE_DATA_BULK_BALANCE_LIST}[1]"), secretKey).
                split(":")[2] != "")

        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_SELF)
                .contains(balanceRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_FIRST)
                .contains(balanceRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_PREV)
                .contains(balanceRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_NEXT)
                .contains(balanceRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_LAST)
                .contains(balanceRequestUrl.split(AUConstants.CDS_PATH)[1]))

        softAssertion.assertAll()
    }

    @Test
    void "TC1203002_Get Balances For Specific Invalid Accounts"() {

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

        def response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_BALANCES, clientHeader)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_BALANCES))
                .post("${AUConstants.BULK_BALANCES_PATH}")

        SoftAssert softAssertion= new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_422)
        softAssertion.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))

        softAssertion.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_CODE),
                AUConstants.ERROR_CODE_INVALID_BANK_ACC)
        softAssertion.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_TITLE),
                AUConstants.INVALID_BANK_ACC)

        softAssertion.assertAll()
    }

    @Test
    void "TC1204002_Get Invalid Account Balance"() {

        def response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_BALANCE, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_BALANCES))
                .get("${AUConstants.BULK_ACCOUNT_PATH}/32125763242/balance")

        SoftAssert softAssertion= new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_404)
        softAssertion.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))

        softAssertion.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_CODE),
                AUConstants.ERROR_CODE_INVALID_BANK_ACC)
        softAssertion.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_TITLE),
                AUConstants.INVALID_BANK_ACC)
        softAssertion.assertAll()
    }

    @Test
    void "TC1205002_Get Invalid Account Detail"() {

        def response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_ACCOUNT, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get("${AUConstants.BULK_ACCOUNT_PATH}/342678987")

        SoftAssert softAssertion= new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_404)
        softAssertion.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))

        softAssertion.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_CODE),
                AUConstants.ERROR_CODE_INVALID_BANK_ACC)
        softAssertion.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_TITLE),
                AUConstants.INVALID_BANK_ACC)
        softAssertion.assertAll()
    }

    @Test
    void "TC1206002_Get Transactions For Invalid Account"() {

        Response response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_TRANSACTIONS, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_TRANSACTIONS))
                .get("${AUConstants.BULK_ACCOUNT_PATH}/34867635209/transactions")

        SoftAssert softAssertion= new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_404)
        softAssertion.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))

        softAssertion.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_CODE),
                AUConstants.ERROR_CODE_INVALID_BANK_ACC)
        softAssertion.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_TITLE),
                AUConstants.INVALID_BANK_ACC)
        softAssertion.assertAll()
    }

    @Test (groups = "SmokeTest")
    void "TC1209001_Get Bulk Direct Debits"() {

        String directDebitRequestUrl = "${AUConstants.BULK_DIRECT_DEBITS_PATH}"

        Response response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_DIRECT_DEBITS, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_DIRECT_DEBIT))
                .get(directDebitRequestUrl)

        SoftAssert softAssertion= new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        softAssertion.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))

        softAssertion.assertTrue(AUIdEncryptorDecryptor.decrypt(
                AUTestUtil.parseResponseBody(response, "${AUConstants.RESPONSE_DATA_DIRECT_DEBIT_AUTH}.accountId[0]"), secretKey).
                split(":")[2] != "")
        softAssertion.assertTrue(AUIdEncryptorDecryptor.decrypt(
                AUTestUtil.parseResponseBody(response, "${AUConstants.RESPONSE_DATA_DIRECT_DEBIT_AUTH}.accountId[1]"), secretKey).
                split(":")[2] != "")

        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_SELF)
                .contains(directDebitRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_FIRST)
                .contains(directDebitRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_PREV)
                .contains(directDebitRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_NEXT)
                .contains(directDebitRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_LAST)
                .contains(directDebitRequestUrl.split(AUConstants.CDS_PATH)[1]))

        softAssertion.assertAll()
    }

    @Test
    void "TC1210002_Get Direct Debits For Specific Invalid Accounts"() {

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

        def response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_DIRECT_DEBITS, clientHeader)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_DIRECT_DEBIT))
                .post("${AUConstants.BULK_DIRECT_DEBITS_PATH}")

        SoftAssert softAssertion= new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_422)
        softAssertion.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))

        softAssertion.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_CODE),
                    AUConstants.ERROR_CODE_INVALID_BANK_ACC)
        softAssertion.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_TITLE),
                AUConstants.INVALID_BANK_ACC)
        softAssertion.assertAll()
    }

    @Test (groups = "SmokeTest")
    void "TC1212001_Get Scheduled Payments Bulk"() {

        String schedulePaymentRequestUrl = "${AUConstants.BULK_SCHEDULE_PAYMENTS_PATH}"

        Response response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_PAYMENT_SCHEDULED, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_SCHEDULED_PAYMENT))
                .get(schedulePaymentRequestUrl)

        SoftAssert softAssertion= new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        softAssertion.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))

        softAssertion.assertEquals(userId, AUIdEncryptorDecryptor.decrypt(
                AUTestUtil.parseResponseBody(response, "${AUConstants.RESPONSE_DATA_SCHEDULE_PAY}.scheduledPaymentId[0]"),
                secretKey).split(":")[0])
        softAssertion.assertEquals(userId, AUIdEncryptorDecryptor.decrypt(
                AUTestUtil.parseResponseBody(response, "${AUConstants.RESPONSE_DATA_SCHEDULE_PAY}.from.accountId[0]"),
                secretKey).split(":")[0])
        softAssertion.assertEquals(userId, AUIdEncryptorDecryptor.decrypt(
                AUTestUtil.parseResponseBody(response, "${AUConstants.RESPONSE_DATA_SCHEDULE_PAY}.paymentSet[0].to.accountId[0]"),
                secretKey).split(":")[0])
        softAssertion.assertEquals(userId, AUIdEncryptorDecryptor.decrypt(
                AUTestUtil.parseResponseBody(response, "${AUConstants.RESPONSE_DATA_SCHEDULE_PAY}.paymentSet[0].to.payeeId[0]"),
                secretKey).split(":")[0])


        softAssertion.assertEquals(userId, AUIdEncryptorDecryptor.decrypt(
                AUTestUtil.parseResponseBody(response, "${AUConstants.RESPONSE_DATA_SCHEDULE_PAY}.scheduledPaymentId[1]"),
                secretKey).split(":")[0])
        softAssertion.assertEquals(userId, AUIdEncryptorDecryptor.decrypt(
                AUTestUtil.parseResponseBody(response, "${AUConstants.RESPONSE_DATA_SCHEDULE_PAY}.from.accountId[1]"),
                secretKey).split(":")[0])
        softAssertion.assertEquals(userId, AUIdEncryptorDecryptor.decrypt(
                AUTestUtil.parseResponseBody(response, "${AUConstants.RESPONSE_DATA_SCHEDULE_PAY}.paymentSet[1].to.accountId[0]"),
                secretKey).split(":")[0])
        softAssertion.assertEquals(userId, AUIdEncryptorDecryptor.decrypt(
                AUTestUtil.parseResponseBody(response, "${AUConstants.RESPONSE_DATA_SCHEDULE_PAY}.paymentSet[1].to.payeeId[0]"),
                secretKey).split(":")[0])

        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_SELF)
                .contains(schedulePaymentRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_FIRST)
                .contains(schedulePaymentRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_PREV)
                .contains(schedulePaymentRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_NEXT)
                .contains(schedulePaymentRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_LAST)
                .contains(schedulePaymentRequestUrl.split(AUConstants.CDS_PATH)[1]))

        softAssertion.assertAll()
    }

    @Test
    void "TC1213002_Get Scheduled Payments For Specific Invalid Accounts"() {

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

        def response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_PAYMENT_SCHEDULED, clientHeader)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_SCHEDULED_PAYMENT))
                .post("${AUConstants.BULK_SCHEDULE_PAYMENTS_PATH}")

        SoftAssert softAssertion= new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_422)
        softAssertion.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))

        softAssertion.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_CODE),
                AUConstants.ERROR_CODE_INVALID_BANK_ACC)
        softAssertion.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_TITLE),
                AUConstants.INVALID_BANK_ACC)

        softAssertion.assertAll()
    }

    @Test
    void "TC1215002_Get Invalid Payee Detail"() {

        Response response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_PAYEES, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PAYEES))
                .get("${AUConstants.BULK_PAYEES}/1426558421")

        SoftAssert softAssertion= new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_404)
        softAssertion.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))

        softAssertion.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_CODE),
                AUConstants.ERROR_CODE_INVALID_RESOURCE)
        softAssertion.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_TITLE),
                AUConstants.INVALID_RESOURCE)

        softAssertion.assertAll()
    }

    @Test (groups = "SmokeTest", priority = 2)
    void "TC1214001_Get Payees"() {

        String payeeRequestUrl = "${AUConstants.BULK_PAYEES}"

        Response response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_PAYEES, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PAYEES))
                .get(payeeRequestUrl)

        encryptedPayeeId = AUTestUtil.parseResponseBody(response, "${AUConstants.RESPONSE_DATA_PAYEE}.payeeId[0]")

        SoftAssert softAssertion= new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        softAssertion.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))

        softAssertion.assertEquals(userId, AUIdEncryptorDecryptor.decrypt(encryptedPayeeId,
                secretKey).split(":")[0])

        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_SELF)
                .contains(payeeRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_FIRST)
                .contains(payeeRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_PREV)
                .contains(payeeRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_NEXT)
                .contains(payeeRequestUrl.split(AUConstants.CDS_PATH)[1]))
        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_LAST)
                .contains(payeeRequestUrl.split(AUConstants.CDS_PATH)[1]))

        softAssertion.assertAll()
    }

    @Test (groups = "SmokeTest", dependsOnMethods = "TC1214001_Get Payees", priority = 2)
    void "TC1215001_Get Payee Detail"() {

        String payeeRequestUrl = "${AUConstants.BULK_PAYEES}/${encryptedPayeeId}"

        Response response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_PAYEES, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PAYEES))
                .get(payeeRequestUrl)

        SoftAssert softAssertion= new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        softAssertion.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))

        softAssertion.assertEquals(userId, AUIdEncryptorDecryptor.decrypt(
                AUTestUtil.parseResponseBody(response, "${AUConstants.RESPONSE_DATA_PAYEEID}"),
                secretKey).split(":")[0])

        softAssertion.assertTrue(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_SELF)
                .contains(payeeRequestUrl.split(AUConstants.CDS_PATH)[1]))

        softAssertion.assertAll()
    }
}

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

package org.wso2.cds.integration.test.admin_api.error

import org.wso2.cds.test.framework.AUTest
import org.wso2.cds.test.framework.constant.AUAccountScope
import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.request_builder.AURegistrationRequestBuilder
import org.wso2.cds.test.framework.request_builder.AURequestBuilder
import org.wso2.cds.test.framework.utility.AURestAsRequestBuilder
import org.wso2.cds.test.framework.utility.AUTestUtil
import io.restassured.http.ContentType
import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

import java.nio.charset.Charset

/**
 * Authenticated Error Metrics Validation.
 */
class ErrorMetrics_Authenticated extends AUTest {

    String encryptedAccount1Id, encryptedAccount2Id
    def clientHeader = "${Base64.encoder.encodeToString(getCDSClient().getBytes(Charset.defaultCharset()))}"

    @BeforeClass (alwaysRun = true)
    void "Initial Metrics Request"() {

        if(!auConfiguration.getAppInfoClientID().equalsIgnoreCase("") ||
                !auConfiguration.getAppInfoClientID().equalsIgnoreCase("AppConfig.Application.ClientID") ) {

            doConsentAuthorisation()
            generateUserAccessToken()

            //Account Retrieval
            doAccountRetrieval(userAccessToken)
            encryptedAccount1Id = AUTestUtil.parseResponseBody(response, "data.accounts.accountId[0]")
            encryptedAccount2Id = AUTestUtil.parseResponseBody(response, "data.accounts.accountId[1]")
        }

        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Assign Metrics to Variables
        getInitialMetricsResponse(metricsResponse)
    }

    @Test (groups = "SmokeTest")
    void "Verify the 400 error count for authenticated High priority invocation is listed"() {

        //Send DCR Registration request with same Service Provider Name
        if(auConfiguration.getAppInfoClientID().equalsIgnoreCase("") ||
                auConfiguration.getAppInfoClientID().equalsIgnoreCase("AppConfig.Application.ClientID") ) {

            AURegistrationRequestBuilder registrationRequestBuilder = new AURegistrationRequestBuilder()

            Response registrationResponse = AURegistrationRequestBuilder
                    .buildRegistrationRequest(registrationRequestBuilder.getAURegularClaims(auConfiguration.getAppDCRSoftwareId(),
                            new File(auConfiguration.getAppDCRSelfSignedSSAPath()).text))
                    .post(AUConstants.DCR_REGISTRATION_ENDPOINT)

            clientId = AUTestUtil.parseResponseBody(registrationResponse, AUConstants.CLIENT_ID)

            Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_400)

            //Get the expected errors metrics
            authErrorCurrentDay = getErrorsMetrics(authErrorCurrentDay, AUConstants.STATUS_CODE_400)

            //Send Metrics Request and Verify Response
            def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)
            Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)

            //Asserting the Errors Metrics response
            assertMetricsErrorResponse(metricsResponse)

            //High Priority Invocation count increase by 1
            highPriorityCurrentDay = highPriorityCurrentDay + 1

            //Unattended count increase by 1 for the Metrics Call of BeforeClass Step
            unattendedCurrentDay = unattendedCurrentDay + 1

            //Asserting the Performance, Average Response and Average TPS
            assertTierBasedMetrics(metricsResponse)
        }
    }

    @Test
    void "Verify the 401 error count for authenticated High priority invocation is listed"(){

        def response = AURequestBuilder.buildBasicRequestWithOptionalHeaders(AUConstants.INCORRECT_ACCESS_TOKEN,
                AUConstants.X_V_HEADER_ACCOUNTS, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get("${AUConstants.BULK_ACCOUNT_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_401)

        //Get the expected errors metrics
        authErrorCurrentDay = getErrorsMetrics(authErrorCurrentDay, AUConstants.STATUS_CODE_401)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        sleep(AUConstants.ABANDON_WAIT_TIME)

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)
        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)

        //Asserting the Errors Metrics response
        assertMetricsErrorResponse(metricsResponse)

        //High Priority Invocation count increase by 1
        highPriorityCurrentDay = highPriorityCurrentDay + 1

        //Unattended count increase by 1 for the Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Verify the 403 error count for authenticated High priority invocation is listed"() {

        scopes = [
                AUAccountScope.BANK_ACCOUNT_BASIC_READ
        ]

        doConsentAuthorisation()
        generateUserAccessToken()

        def response = AURequestBuilder.buildBasicRequest(userAccessToken,
                AUConstants.X_V_HEADER_CUSTOMER_DETAIL)
                .header(AUConstants.X_FAPI_CUSTOMER_IP_ADDRESS , AUConstants.IP)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_CUSTOMER))
                .get("${AUConstants.CUSTOMER_DETAILS}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_403)

        //Get the expected errors metrics
        authErrorCurrentDay = getErrorsMetrics(authErrorCurrentDay, AUConstants.STATUS_CODE_403)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)
        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)

        //Asserting the Errors Metrics response
        assertMetricsErrorResponse(metricsResponse)

        //High Priority Invocation count increase by 3
        highPriorityCurrentDay = highPriorityCurrentDay + 3

        //Unattended count increase by 1 for the Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Verify the 405 error count for authenticated High priority invocation is listed"() {

        def response = AURequestBuilder.buildBasicRequestWithOptionalHeaders(userAccessToken,
                AUConstants.X_V_HEADER_ACCOUNTS, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .post("${AUConstants.BULK_ACCOUNT_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_405)

        //Get the expected errors metrics
        authErrorCurrentDay = getErrorsMetrics(authErrorCurrentDay, AUConstants.STATUS_CODE_405)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)
        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)

        //Asserting the Errors Metrics response
        assertMetricsErrorResponse(metricsResponse)

        //High Priority Invocation count increase by 1
        highPriorityCurrentDay = highPriorityCurrentDay

        //Unattended count increase by 1 for the Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Verify the 406 error count for authenticated High priority invocation is listed"() {

        def response = AURequestBuilder.buildBasicRequestWithOptionalHeaders(userAccessToken,
                AUConstants.UNSUPPORTED_X_V_VERSION, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get("${AUConstants.BULK_ACCOUNT_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_406)

        //Get the expected errors metrics
        authErrorCurrentDay = getErrorsMetrics(authErrorCurrentDay, AUConstants.STATUS_CODE_406)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)
        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)

        //Asserting the Errors Metrics response
        assertMetricsErrorResponse(metricsResponse)

        //High Priority Invocation count increase by 1
        highPriorityCurrentDay = highPriorityCurrentDay + 1

        //Unattended count increase by 1 for the Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Verify the 500 error count for authenticated High priority invocation is listed"() {

        accessToken = getApplicationAccessToken(clientId)
        Assert.assertNotNull(accessToken)

        AURegistrationRequestBuilder registrationRequestBuilder = new AURegistrationRequestBuilder()
        def response = AURegistrationRequestBuilder
                .buildRegistrationRequest(registrationRequestBuilder.getRegularClaimsWithNewRedirectUri())
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, "${AUConstants.AUTHORIZATION_BEARER_TAG}${accessToken}")
                .contentType(ContentType.JSON)
                .when()
                .put(AUConstants.REGISTER_PATH + auConfiguration.getAppInfoClientID())

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_500)

        //Get the expected errors metrics
        authErrorCurrentDay = getErrorsMetrics(authErrorCurrentDay, AUConstants.STATUS_CODE_500)

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)
        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)

        //Asserting the Errors Metrics response
        assertMetricsErrorResponse(metricsResponse)
    }

    @Test
    void "Verify the 400 error count for authenticated Low priority invocation is listed"() {

        Response response = AURestAsRequestBuilder.buildRequest()
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
                .header(AUConstants.X_FAPI_CUSTOMER_IP_ADDRESS , AUConstants.IP)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PAYEES))
                .get("${AUConstants.BULK_PAYEES}")

            Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)

            //Get the expected errors metrics
            authErrorCurrentDay = getErrorsMetrics(authErrorCurrentDay, AUConstants.STATUS_CODE_400)

            //Calculate Performance, Average Response and Average TPS Metrics for each tier
            calculateTierBasedMetrics()

            //Send Metrics Request and Verify Response
            def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)
            Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)

            //Asserting the Errors Metrics response
            assertMetricsErrorResponse(metricsResponse)

            //Low Priority Invocation count increase by 1
            lowPriorityCurrentDay = lowPriorityCurrentDay + 1

            //Unattended count increase by 1 for the Metrics Call of BeforeClass Step
            unattendedCurrentDay = unattendedCurrentDay + 1

            //Asserting the Performance, Average Response and Average TPS
            assertTierBasedMetrics(metricsResponse)
        }

    @Test
    void "Verify the 401 error count for authenticated Low priority invocation is listed"(){

        Response response = AURequestBuilder.buildBasicRequest(AUConstants.INCORRECT_ACCESS_TOKEN, AUConstants.X_V_HEADER_PAYEES)
                .header(AUConstants.X_FAPI_CUSTOMER_IP_ADDRESS , AUConstants.IP)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PAYEES))
                .get("${AUConstants.BULK_PAYEES}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_401)

        //Get the expected errors metrics
        authErrorCurrentDay = getErrorsMetrics(authErrorCurrentDay, AUConstants.STATUS_CODE_401)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)
        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)

        //Asserting the Errors Metrics response
        assertMetricsErrorResponse(metricsResponse)

        //Low Priority Invocation count increase by 1
        lowPriorityCurrentDay = lowPriorityCurrentDay + 1

        //Unattended count increase by 1 for the Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Verify the 403 error count for authenticated Low priority invocation is listed"() {

        scopes = [
                AUAccountScope.BANK_ACCOUNT_BASIC_READ
        ]

        doConsentAuthorisation()
        generateUserAccessToken()

        def response = AURequestBuilder.buildBasicRequestWithOptionalHeaders(userAccessToken,
                AUConstants.X_V_HEADER_ACCOUNT, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get("${AUConstants.BULK_ACCOUNT_PATH}/${encryptedAccount1Id}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_403)

        //Get the expected errors metrics
        authErrorCurrentDay = getErrorsMetrics(authErrorCurrentDay, AUConstants.STATUS_CODE_403)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)
        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)

        //Asserting the Errors Metrics response
        assertMetricsErrorResponse(metricsResponse)

        //High Priority Invocation count increase by 2
        highPriorityCurrentDay = highPriorityCurrentDay + 2

        //Low Priority Invocation count increase by 1
        lowPriorityCurrentDay = lowPriorityCurrentDay + 1

        //Unattended count increase by 1 for the Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Verify the 405 error count for authenticated Low priority invocation is listed"() {

        def response = AURequestBuilder.buildBasicRequestWithOptionalHeaders(userAccessToken,
                AUConstants.X_V_HEADER_PAYEES, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PAYEES))
                .put("${AUConstants.BULK_PAYEES}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_405)

        //Get the expected errors metrics
        authErrorCurrentDay = getErrorsMetrics(authErrorCurrentDay, AUConstants.STATUS_CODE_405)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)
        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)

        //Asserting the Errors Metrics response
        assertMetricsErrorResponse(metricsResponse)

        //Low Priority Invocation count increase by 1
        lowPriorityCurrentDay = lowPriorityCurrentDay + 1

        //Unattended count increase by 1 for the Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Verify the 404 error count for authenticated Low priority invocation is listed"() {

        doConsentAuthorisation()
        generateUserAccessToken()

        def response = AURequestBuilder
                .buildBasicRequest(userAccessToken, AUConstants.X_V_HEADER_PAYEES)
                .header(AUConstants.X_FAPI_CUSTOMER_IP_ADDRESS , AUConstants.IP)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PAYEES))
                .get("${AUConstants.BULK_PAYEES}/1426558421")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_404)

        //Get the expected errors metrics
        authErrorCurrentDay = getErrorsMetrics(authErrorCurrentDay, AUConstants.STATUS_CODE_404)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)
        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)

        //Asserting the Errors Metrics response
        assertMetricsErrorResponse(metricsResponse)

        //Low Priority Invocation count increase by 1
        lowPriorityCurrentDay = lowPriorityCurrentDay + 1

        //Unattended count increase by 1 for the Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Verify the 406 error count for authenticated Low priority invocation is listed"() {

        doConsentAuthorisation()
        generateUserAccessToken()

        def response = AURequestBuilder.buildBasicRequestWithOptionalHeaders(userAccessToken,
                AUConstants.UNSUPPORTED_X_V_VERSION, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_BALANCES))
                .get("${AUConstants.BULK_BALANCES_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_406)

        //Get the expected errors metrics
        authErrorCurrentDay = getErrorsMetrics(authErrorCurrentDay, AUConstants.STATUS_CODE_406)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)
        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)

        //Asserting the Errors Metrics response
        assertMetricsErrorResponse(metricsResponse)

        //Low Priority Invocation count increase by 1
        lowPriorityCurrentDay = lowPriorityCurrentDay + 1

        //Unattended count increase by 1 for the Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test (invocationCount = 3)
    void "Verify the 415 error count for authenticated Low priority invocation is listed"() {

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

        def response = AURequestBuilder.buildBasicRequestWithOptionalHeaders(userAccessToken,
                AUConstants.X_V_HEADER_ACCOUNTS, clientHeader)
                .contentType(ContentType.XML)
                .body(requestBody)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_SCHEDULED_PAYMENT))
                .post("${AUConstants.BULK_SCHEDULE_PAYMENTS_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_415)

        //Get the expected errors metrics
        authErrorCurrentDay = getErrorsMetrics(authErrorCurrentDay, AUConstants.STATUS_CODE_415)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)
        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)

        //Asserting the Errors Metrics response
        assertMetricsErrorResponse(metricsResponse)

        //Low Priority Invocation count increase by 1
        lowPriorityCurrentDay = lowPriorityCurrentDay + 1

        //Unattended count increase by 1 for the Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Verify the 400 error count for authenticated Unattended invocation is listed"() {

        def response = AURestAsRequestBuilder.buildRequest()
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_CUSTOMER))
                .get("${AUConstants.CUSTOMER_DETAILS}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)

        //Get the expected errors metrics
        authErrorCurrentDay = getErrorsMetrics(authErrorCurrentDay, AUConstants.STATUS_CODE_400)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)
        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)

        //Asserting the Errors Metrics response
        assertMetricsErrorResponse(metricsResponse)

        //Unattended count increase by 2 including Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 2

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Verify the 401 error count for authenticated Unattended invocation is listed"(){

        def response = AURequestBuilder.buildBasicRequest(AUConstants.INCORRECT_ACCESS_TOKEN, AUConstants.X_V_HEADER_PAYMENT_SCHEDULED)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_SCHEDULED_PAYMENT))
                .get("${AUConstants.BULK_SCHEDULE_PAYMENTS_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_401)

        //Get the expected errors metrics
        authErrorCurrentDay = getErrorsMetrics(authErrorCurrentDay, AUConstants.STATUS_CODE_401)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)
        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)

        //Asserting the Errors Metrics response
        assertMetricsErrorResponse(metricsResponse)

        //Unattended count increase by 2 including Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 2

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Verify the 403 error count for authenticated Unattended invocation is listed"()  {

        scopes = [
                AUAccountScope.BANK_CUSTOMER_BASIC_READ
        ]

        doConsentAuthorisationWithoutAccountSelection()
        generateUserAccessToken()

        def response = AURequestBuilder.buildBasicRequest(userAccessToken, AUConstants.X_V_HEADER_CUSTOMER)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_CUSTOMER))
                .get("${AUConstants.CUSTOMER_DETAILS}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_403)

        //Get the expected errors metrics
        authErrorCurrentDay = getErrorsMetrics(authErrorCurrentDay, AUConstants.STATUS_CODE_403)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)
        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)

        //Asserting the Errors Metrics response
        assertMetricsErrorResponse(metricsResponse)

        //High Priority count increase by 2
        highPriorityCurrentDay = highPriorityCurrentDay + 2

        //Unattended count increase by 2 including Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 2

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Verify the 405 error count for authenticated Unattended invocation is listed"() {

        def response = AURequestBuilder
                .buildBasicRequest(userAccessToken, AUConstants.X_V_HEADER_CUSTOMER_DETAIL)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_CUSTOMER))
                .put("${AUConstants.CUSTOMER_DETAILS}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_405)

        //Get the expected errors metrics
        authErrorCurrentDay = getErrorsMetrics(authErrorCurrentDay, AUConstants.STATUS_CODE_405)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)
        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)

        //Asserting the Errors Metrics response
        assertMetricsErrorResponse(metricsResponse)

        //Unattended count increase by 2 including Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 2

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Verify the 404 error count for authenticated Unattended invocation is listed"() {

        doConsentAuthorisation()
        generateUserAccessToken()

        String requestUrl = "${AUConstants.BULK_ACCOUNT_PATH}/12345/transactions"

        Response response = AURequestBuilder
                .buildBasicRequest(userAccessToken, AUConstants.X_V_HEADER_TRANSACTIONS)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_TRANSACTIONS))
                .get(requestUrl)

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_404)

        //Get the expected errors metrics
        authErrorCurrentDay = getErrorsMetrics(authErrorCurrentDay, AUConstants.STATUS_CODE_404)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)
        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)

        //Asserting the Errors Metrics response
        assertMetricsErrorResponse(metricsResponse)

        //Unattended count increase by 2 including Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 2

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Verify the 406 error count for authenticated Unattended invocation is listed"() {

        doConsentAuthorisation()
        generateUserAccessToken()

        def response = AURequestBuilder
                .buildBasicRequest(userAccessToken, AUConstants.UNSUPPORTED_X_V_VERSION)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get("${AUConstants.BULK_ACCOUNT_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_406)

        //Get the expected errors metrics
        authErrorCurrentDay = getErrorsMetrics(authErrorCurrentDay, AUConstants.STATUS_CODE_406)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)
        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)

        //Asserting the Errors Metrics response
        assertMetricsErrorResponse(metricsResponse)

        //Unattended count increase by 2 including Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 2

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Verify the 415 error count for authenticated Unattended invocation is listed"() {

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
                .contentType(ContentType.TEXT)
                .header(AUConstants.X_MIN_HEADER, AUConstants.X_V_MIN_HEADER_METRICS)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ADMIN))
                .body(requestBody)
                .post("${AUConstants.CDS_PATH}/admin/register/metadata")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_415)

        //Get the expected errors metrics
        authErrorCurrentDay = getErrorsMetrics(authErrorCurrentDay, AUConstants.STATUS_CODE_415)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)
        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)

        //Asserting the Errors Metrics response
        assertMetricsErrorResponse(metricsResponse)

        //Unattended count increase by 2 including Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 2

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Verify the 400 error count for authenticated LargePayload invocation is listed"() {

        def response = AURestAsRequestBuilder.buildRequest()
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_DIRECT_DEBIT))
                .get("${AUConstants.BULK_DIRECT_DEBITS_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)

        //Get the expected errors metrics
        authErrorCurrentDay = getErrorsMetrics(authErrorCurrentDay, AUConstants.STATUS_CODE_400)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)
        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)

        //Asserting the Errors Metrics response
        assertMetricsErrorResponse(metricsResponse)

        //Large Payload count increase by 1
        largePayloadCurrentDay = largePayloadCurrentDay + 1

        //Unattended count increase by 1 including Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Verify the 401 error count for authenticated LargePayload invocation is listed"(){

        def response = AURequestBuilder.buildBasicRequestWithCustomHeaders(AUConstants.INCORRECT_ACCESS_TOKEN,
                AUConstants.X_V_HEADER_DIRECT_DEBITS, clientHeader)
                .header(AUConstants.X_FAPI_CUSTOMER_IP_ADDRESS , AUConstants.IP)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_DIRECT_DEBIT))
                .get("${AUConstants.BULK_DIRECT_DEBITS_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_401)

        //Get the expected errors metrics
        authErrorCurrentDay = getErrorsMetrics(authErrorCurrentDay, AUConstants.STATUS_CODE_401)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)
        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)

        //Asserting the Errors Metrics response
        assertMetricsErrorResponse(metricsResponse)

        //Large Payload count increase by 1
        largePayloadCurrentDay = largePayloadCurrentDay + 1

        //Unattended count increase by 1 including Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Verify the 403 error count for authenticated LargePayload invocation is listed"() {

        scopes = [
                AUAccountScope.BANK_ACCOUNT_BASIC_READ,
                AUAccountScope.BANK_ACCOUNT_DETAIL_READ,
                AUAccountScope.BANK_TRANSACTION_READ
        ]

        doConsentAuthorisation()
        generateUserAccessToken()

        def response = AURequestBuilder
                .buildBasicRequest(userAccessToken, AUConstants.X_V_HEADER_ACCOUNTS)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_DIRECT_DEBIT))
                .get("${AUConstants.BULK_DIRECT_DEBITS_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_403)

        //Get the expected errors metrics
        authErrorCurrentDay = getErrorsMetrics(authErrorCurrentDay, AUConstants.STATUS_CODE_403)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)
        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)

        //Asserting the Errors Metrics response
        assertMetricsErrorResponse(metricsResponse)

        //High Priority count increase by 1
        highPriorityCurrentDay = highPriorityCurrentDay + 2

        //Large Payload count increase by 1
        largePayloadCurrentDay = largePayloadCurrentDay + 1

        //Unattended count increase by 1 including Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Verify the 405 error count for authenticated LargePayload invocation is listed"() {

        def response = AURequestBuilder
                .buildBasicRequest(userAccessToken, AUConstants.X_V_HEADER_ACCOUNTS)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_DIRECT_DEBIT))
                .put("${AUConstants.BULK_DIRECT_DEBITS_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_405)

        //Get the expected errors metrics
        authErrorCurrentDay = getErrorsMetrics(authErrorCurrentDay, AUConstants.STATUS_CODE_405)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)
        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)

        //Asserting the Errors Metrics response
        assertMetricsErrorResponse(metricsResponse)

        //Large Payload count increase by 1
        largePayloadCurrentDay = largePayloadCurrentDay + 1

        //Unattended count increase by 1 including Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Verify the 406 error count for authenticated LargePayload invocation is listed"() {

        def response = AURequestBuilder
                .buildBasicRequest(userAccessToken, AUConstants.UNSUPPORTED_X_V_VERSION)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_DIRECT_DEBIT))
                .get("${AUConstants.BULK_DIRECT_DEBITS_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_406)

        //Get the expected errors metrics
        authErrorCurrentDay = getErrorsMetrics(authErrorCurrentDay, AUConstants.STATUS_CODE_406)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)
        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)

        //Asserting the Errors Metrics response
        assertMetricsErrorResponse(metricsResponse)

        //Large Payload count increase by 1
        largePayloadCurrentDay = largePayloadCurrentDay + 1

        //Unattended count increase by 1 including Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Verify the 415 error count for authenticated LargePayload invocation is listed"() {

        String requestBody = """
            {
              "data": {
                "accountIds": [
                  "${consentedAccount}", "${secondConsentedAccount}"
                ]
              },
              "meta": {}
            }
        """.stripIndent()

        String directDebitRequestUrl = "${AUConstants.BULK_DIRECT_DEBITS_PATH}"

        def response = AURequestBuilder.buildBasicRequestWithOptionalHeaders(userAccessToken,
                AUConstants.X_V_HEADER_DIRECT_DEBITS, clientHeader)
                .contentType(ContentType.XML)
                .body(requestBody)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_DIRECT_DEBIT))
                .post(directDebitRequestUrl)

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_415)

        //Get the expected errors metrics
        authErrorCurrentDay = getErrorsMetrics(authErrorCurrentDay, AUConstants.STATUS_CODE_415)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)
        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)

        //Asserting the Errors Metrics response
        assertMetricsErrorResponse(metricsResponse)

        //Large Payload count increase by 1
        largePayloadCurrentDay = largePayloadCurrentDay + 1

        //Unattended count increase by 1 including Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Verify the 422 error count for authenticated Low priority invocation is listed"() {

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

        def response = AURequestBuilder.buildBasicRequestWithOptionalHeaders(userAccessToken,
                AUConstants.X_V_HEADER_ACCOUNTS, clientHeader)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_BALANCES))
                .post("${AUConstants.BULK_BALANCES_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_422)

        //Get the expected errors metrics
        authErrorCurrentDay = getErrorsMetrics(authErrorCurrentDay, AUConstants.STATUS_CODE_422)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)
        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)

        //Asserting the Errors Metrics response
        assertMetricsErrorResponse(metricsResponse)

        //Low Priority count increase by 1
        lowPriorityCurrentDay = lowPriorityCurrentDay + 1

        //Unattended count increase by 1 including Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }
}

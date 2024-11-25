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

package com.wso2.cds.integration.test.throttling

import com.nimbusds.oauth2.sdk.Scope
import com.wso2.cds.test.framework.AUTest
import com.wso2.cds.test.framework.constant.AUConstants
import com.wso2.cds.test.framework.request_builder.AURequestBuilder
import com.wso2.cds.test.framework.utility.AUTestUtil
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

import java.nio.charset.Charset
import java.util.concurrent.atomic.AtomicInteger

/**
 * Throttling policy test class.
 */
class ThrottlingPolicyTest extends AUTest{

    AtomicInteger sequence = new AtomicInteger(0)
    def clientHeader = "${Base64.encoder.encodeToString(getCDSClient().getBytes(Charset.defaultCharset()))}"

    @BeforeClass(alwaysRun = true)
    void "Get User Access Token"() {
        doConsentAuthorisation()
        generateUserAccessToken()
    }

    /**
     * It may be required to increase the interval of the throttling policy upto 20 seconds to pass
     * the test depending on the performance
     */
    @Test(invocationCount = 600, threadPoolSize = 100, enabled = true)
    void "TC0306001_Throttle requests by AllConsumers policy - Unattended"() {

        def response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_ACCOUNTS, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get("${AUConstants.BULK_ACCOUNT_PATH}")

        int currentCount =  sequence.addAndGet(1)

        if(currentCount > 500) {
            Assert.assertEquals(response.statusCode(), 429)
            Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_CODE),
                    AUConstants.ERROR_CODE_GENERAL_EXPECTED_ERROR)
            Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_TITLE),
                    AUConstants.ERROR_TITLE_GENERAL_EXPECTED_ERROR)
        } else {
            Assert.assertEquals(response.statusCode(), 200)
        }
    }

    /**
     * It may be required to increase the interval of the throttling policy upto 20 seconds to pass
     * the test depending on the performance
     */
    @Test(invocationCount = 600, threadPoolSize = 100, enabled = false)
    void "TC0306002_Throttle requests by AllConsumers policy - CustomerPresent"() {

        def response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_ACCOUNTS, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get("${AUConstants.BULK_ACCOUNT_PATH}")

        int currentCount =  sequence.addAndGet(1)

        if(currentCount > 500) {
            Assert.assertEquals(response.statusCode(), 429)
            Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_CODE),
                    AUConstants.ERROR_CODE_GENERAL_EXPECTED_ERROR)
            Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_TITLE),
                    AUConstants.ERROR_TITLE_GENERAL_EXPECTED_ERROR)
        } else {
            Assert.assertEquals(response.statusCode(), 200)
        }
    }

    @Test(invocationCount = 50, threadPoolSize = 5,enabled = false)
    void "TC0306003_Throttle requests by CustomerPresent-Customer"() {

        def response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_ACCOUNTS, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get("${AUConstants.BULK_ACCOUNT_PATH}")

        int currentCount =  sequence.addAndGet(1)

        if(currentCount > 40) {
            Assert.assertEquals(response.statusCode(), 429)
            Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_CODE),
                    AUConstants.ERROR_CODE_GENERAL_EXPECTED_ERROR)
            Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_TITLE),
                    AUConstants.ERROR_TITLE_GENERAL_EXPECTED_ERROR)
        } else {
            Assert.assertEquals(response.statusCode(), 200)
        }
    }

    @Test(invocationCount = 200, threadPoolSize = 20, enabled = false)
    void "TC0306004_Throttle requests by DataRecipients policy - Unattended"() {

        def response = AURequestBuilder.buildBasicRequest(userAccessToken,
                AUConstants.X_V_HEADER_ACCOUNTS)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get("${AUConstants.BULK_ACCOUNT_PATH}")

        int currentCount =  sequence.addAndGet(1)

        if(currentCount > 150) {
            Assert.assertEquals(response.statusCode(), 429)
            Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_CODE),
                    AUConstants.ERROR_CODE_GENERAL_EXPECTED_ERROR)
            Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_TITLE),
                    AUConstants.ERROR_TITLE_GENERAL_EXPECTED_ERROR)
        } else {
            Assert.assertEquals(response.statusCode(), 200)
        }
    }

    @Test(invocationCount = 200, threadPoolSize = 20, enabled = false)
    void "TC0306005_Throttle requests by DataRecipients policy - CustomerPresent"() {

        def response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_ACCOUNTS, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get("${AUConstants.BULK_ACCOUNT_PATH}")

        int currentCount =  sequence.addAndGet(1)

        if(currentCount > 150) {
            Assert.assertEquals(response.statusCode(), 429)
            Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_CODE),
                    AUConstants.ERROR_CODE_GENERAL_EXPECTED_ERROR)
            Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_TITLE),
                    AUConstants.ERROR_TITLE_GENERAL_EXPECTED_ERROR)
        } else {
            Assert.assertEquals(response.statusCode(), 200)
        }
    }

    @Test(invocationCount = 200, threadPoolSize = 5, enabled = true)//
    void "TC0306006_Throttle requests by Unattended-CallsPerSession policy"() {

        def response = AURequestBuilder.buildBasicRequest(userAccessToken,
                AUConstants.X_V_HEADER_ACCOUNTS)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get("${AUConstants.BULK_ACCOUNT_PATH}")

        int currentCount =  sequence.addAndGet(1)

        if(currentCount > 150) {
            Assert.assertEquals(response.statusCode(), 429)
            Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_CODE),
                    AUConstants.ERROR_CODE_GENERAL_EXPECTED_ERROR)
            Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_TITLE),
                    AUConstants.ERROR_TITLE_GENERAL_EXPECTED_ERROR)
        } else {
            Assert.assertEquals(response.statusCode(), 200)
        }
    }

    @Test(invocationCount = 100, threadPoolSize = 10, enabled = true)
    void "TC0306007_Throttle requests by Unattended-SessionTPS policy"() {

        def response = AURequestBuilder.buildBasicRequest(userAccessToken,
                AUConstants.X_V_HEADER_ACCOUNTS)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get("${AUConstants.BULK_ACCOUNT_PATH}")

        int currentCount =  sequence.addAndGet(1)

        if(currentCount > 30) {
            Assert.assertEquals(response.statusCode(), 429)
            Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_CODE),
                    AUConstants.ERROR_CODE_GENERAL_EXPECTED_ERROR)
            Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_TITLE),
                    AUConstants.ERROR_TITLE_GENERAL_EXPECTED_ERROR)
        }else {
            Assert.assertEquals(response.statusCode(), 200)
        }
    }

    @Test (invocationCount = 20, enabled = true)
    void "Throttle requests by Unattended-SessionTPS policy"() {

        doConsentAuthorisation()
        generateUserAccessToken()

        def response = AURequestBuilder
                .buildBasicRequestWithCustomHeaders(userAccessToken, AUConstants.X_V_HEADER_ACCOUNTS, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get("${AUConstants.BULK_ACCOUNT_PATH}")

        int currentCount =  sequence.addAndGet(1)

        if(currentCount > 40) {
            Assert.assertEquals(response.statusCode(), 429)
            Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_CODE),
                    AUConstants.ERROR_CODE_GENERAL_EXPECTED_ERROR)
            Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_TITLE),
                    AUConstants.ERROR_TITLE_GENERAL_EXPECTED_ERROR)
        } else {
            Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        }
    }
}

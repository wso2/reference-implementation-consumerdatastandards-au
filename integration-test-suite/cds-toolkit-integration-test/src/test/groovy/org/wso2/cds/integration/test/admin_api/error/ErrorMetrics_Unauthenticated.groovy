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
import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.request_builder.AURequestBuilder
import org.wso2.cds.test.framework.utility.AURestAsRequestBuilder
import org.wso2.cds.test.framework.utility.AUTestUtil
import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

/**
 * Unauthenticated Error Metrics Validation.
 */
class ErrorMetrics_Unauthenticated extends AUTest {

    @BeforeClass (alwaysRun = true)
    void "Initial Metrics Request"() {

        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Assign Metrics to Variables
        getInitialMetricsResponse(metricsResponse)
    }

    @Test (groups = "SmokeTest")
    void "Verify the 400 error count for unauthenticated invocation is listed"() {

        Response response = AURestAsRequestBuilder.buildRequest()
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PRODUCTS))
                .get("${AUConstants.BANKING_PRODUCT_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)

        unauthErrorCurrentDay = getErrorsMetrics(unauthErrorCurrentDay, AUConstants.STATUS_CODE_400)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)

        //Asserting the Errors Metrics response
        assertMetricsErrorResponse(metricsResponse)

        //Unauthenticated Invocation count increase by 1
        unauthenticatedCurrentDay = unauthenticatedCurrentDay + 1

        //Unattended count increase by 1 for the Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Verify the 406 error count for unauthenticated invocation is listed"() {

        Response response = AURestAsRequestBuilder.buildRequest()
                .header(AUConstants.X_V_HEADER, AUConstants.UNSUPPORTED_X_V_VERSION)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PRODUCTS))
                .get("${AUConstants.BANKING_PRODUCT_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_406)

        unauthErrorCurrentDay = getErrorsMetrics(unauthErrorCurrentDay, AUConstants.STATUS_CODE_406)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)

        //Asserting the Errors Metrics response
        assertMetricsErrorResponse(metricsResponse)

        //Unauthenticated Invocation count increase by 1
        unauthenticatedCurrentDay = unauthenticatedCurrentDay + 1

        //Unattended count increase by 1 for the Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Verify the 405 error count for unauthenticated invocation is listed"() {

        Response response = AURestAsRequestBuilder.buildRequest()
                .header(AUConstants.X_V_HEADER, AUConstants.X_V_HEADER_PRODUCT)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PRODUCTS))
                .put("${AUConstants.BANKING_PRODUCT_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_405)

        unauthErrorCurrentDay = getErrorsMetrics(unauthErrorCurrentDay, AUConstants.STATUS_CODE_405)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)
        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)

        //Asserting the Errors Metrics response
        assertMetricsErrorResponse(metricsResponse)

        //Unattended count increase by 1 for the Metrics Call of BeforeClass Step but unauthenticated invocation count
        // should not increase as this cannot be considered as an invocation.
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Verify the 406 error from Get Status invocation"() {

        Response response = AURequestBuilder
                .buildBasicRequestWithoutAuthorisationHeader(AUConstants.UNSUPPORTED_X_V_VERSION)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_DISCOVERY))
                .get("${AUConstants.DISCOVERY_STATUS}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_406)

        unauthErrorCurrentDay = getErrorsMetrics(unauthErrorCurrentDay, AUConstants.STATUS_CODE_406)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)
        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)

        //Asserting the Errors Metrics response
        assertMetricsErrorResponse(metricsResponse)

        //HighPriorityCurrentDay Invocation count increase by 1
        highPriorityCurrentDay = highPriorityCurrentDay + 1

        //Unattended count increase by 1 for the Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test
    void "Verify the 406 error from Get Outages invocation"() {

        Response response = AURequestBuilder
                .buildBasicRequestWithoutAuthorisationHeader(AUConstants.UNSUPPORTED_X_V_VERSION)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_DISCOVERY))
                .get("${AUConstants.DISCOVERY_OUTAGES}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_406)

        unauthErrorCurrentDay = getErrorsMetrics(unauthErrorCurrentDay, AUConstants.STATUS_CODE_406)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)
        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)

        //Asserting the Errors Metrics response
        assertMetricsErrorResponse(metricsResponse)

        //HighPriorityCurrentDay Invocation count increase by 1
        highPriorityCurrentDay = highPriorityCurrentDay + 1

        //Unattended count increase by 1 for the Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }
}

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
import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

/**
 * Test Cases for Invocations, Performance, Average Response and Average TPS Metrics - Unauthenticated Tier
 */
class UnauthenticatedMetricsValidation extends AUTest {

    @BeforeClass (alwaysRun = true)
    void "Initial Metrics Request"() {

        auConfiguration.setPsuNumber(0)
        auConfiguration.setTppNumber(0)
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Assign Metrics to Variables
        getInitialMetricsResponse(metricsResponse)
    }

    @Test (groups = "SmokeTest")
    void "Count increase with Products Invocation"(){

        Response response = AURequestBuilder.buildBasicRequestWithoutAuthorisationHeader(AUConstants.X_V_HEADER_PRODUCTS)
                .accept(AUConstants.ACCEPT)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PRODUCTS))
                .get("${AUConstants.BANKING_PRODUCT_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_PRODUCTS)
        productId = AUTestUtil.parseResponseBody(response, "data.products.productId[0]").toString()

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Unauthenticated Invocation count increase by 1
        unauthenticatedCurrentDay = unauthenticatedCurrentDay + 1

        //Unattended count increase by 1 for the Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test (dependsOnMethods = "Count increase with Products Invocation")
    void "Count increase with Product Details Invocation"(){

        Response response = AURequestBuilder.buildBasicRequestWithoutAuthorisationHeader(AUConstants.X_V_HEADER_PRODUCT)
                .accept(AUConstants.ACCEPT)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PRODUCTS))
                .get("${AUConstants.BANKING_PRODUCT_PATH}/$productId")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Unauthenticated Invocation count increase by 1
        unauthenticatedCurrentDay = unauthenticatedCurrentDay + 1

        //Unattended count increase by 1 for the Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }

    @Test (invocationCount = 15)
    void "Count increase when an unauthenticated request return an error"() {

        Response response = AURequestBuilder.buildBasicRequestWithoutAuthorisationHeader(1)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PRODUCTS))
                .get("${AUConstants.BANKING_PRODUCT_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_406)

        //Calculate Performance, Average Response and Average TPS Metrics for each tier
        calculateTierBasedMetrics()

        //Send Metrics Request and Verify Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Unauthenticated Invocation count increase by 1
        unauthenticatedCurrentDay = unauthenticatedCurrentDay + 1

        //Unattended count increase by 1 for the Metrics Call of BeforeClass Step
        unattendedCurrentDay = unattendedCurrentDay + 1

        //Asserting the Performance, Average Response and Average TPS
        assertTierBasedMetrics(metricsResponse)
    }
}

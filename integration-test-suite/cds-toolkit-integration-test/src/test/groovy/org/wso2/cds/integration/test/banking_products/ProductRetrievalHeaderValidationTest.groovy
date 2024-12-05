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

package org.wso2.cds.integration.test.banking_products

import org.wso2.cds.test.framework.AUTest
import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.request_builder.AURequestBuilder
import org.wso2.cds.test.framework.utility.AURestAsRequestBuilder
import org.wso2.cds.test.framework.utility.AUTestUtil
import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

import java.nio.charset.Charset

/**
 * Class contains Banking Products Retrieval Header Validation Tests.
 */
class ProductRetrievalHeaderValidationTest extends AUTest {

    def clientHeader = "${Base64.encoder.encodeToString(getCDSClient().getBytes(Charset.defaultCharset()))}"

    @Test
    void "TC1101004_Retrieve banking products with unsupported x-v header"() {

        Response response = AURequestBuilder.buildBasicRequestWithoutAuthorisationHeader(AUConstants.UNSUPPORTED_X_V_VERSION)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PRODUCTS))
                .get("${AUConstants.BANKING_PRODUCT_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_406)
        Assert.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.UNSUPPORTED_X_V_VERSION)

        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_CODE),
                AUConstants.ERROR_CODE_UNSUPPORTED_VERSION)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_TITLE), AUConstants.UNSUPPORTED_VERSION)
    }

    @Test
    void "TC1101005_Retrieve banking products with authentication"() {

        doConsentAuthorisation()
        generateUserAccessToken()

        Response response = AURequestBuilder.buildBasicRequestWithoutAuthorisationHeader(AUConstants.X_V_HEADER_PRODUCTS)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PRODUCTS))
                .get("${AUConstants.BANKING_PRODUCT_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_PRODUCTS)

        Assert.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "${AUConstants.RESPONSE_DATA_PRODUCTS}.productId"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "${AUConstants.RESPONSE_DATA_PRODUCTS}.additionalInformation"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_SELF))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_FIRST))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_PREV))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_NEXT))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_LAST))
    }

    @Test
    void "TC1101018_Retrieve banking products with supported endpoint version with holder identifier header"() {

        def holderID = "HID"

        def response = AURequestBuilder.buildBasicRequestWithoutAuthorisationHeader(AUConstants.X_V_HEADER_PRODUCTS)
                .header(AUConstants.X_FAPI_AUTH_DATE, AUConstants.DATE)
                .header(AUConstants.X_CDS_CLIENT_HEADERS , clientHeader)
                .header("x-${holderID}-v", AUConstants.X_V_HEADER_PRODUCTS)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PRODUCTS))
                .get("${AUConstants.BANKING_PRODUCT_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_PRODUCTS)
        Assert.assertTrue(response.getHeader(AUConstants.CONTENT_TYPE).contains(AUConstants.ACCEPT))

        Assert.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "${AUConstants.RESPONSE_DATA_PRODUCTS}.productId[0]"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_SELF))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_FIRST))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_PREV))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_NEXT))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_LAST))
    }

    @Test
    //Validate X_FAPI_INTERACTION_ID,X_FAPI_AUTH_DATE,X_FAPI_CUSTOMER_IP_ADDRESS & X_CDS_CLIENT_HEADER optional headers
    void "TC0701026_Retrieve banking products with optional-headers"(){

        def response = AURequestBuilder.buildBasicRequestWithoutAuthorisationHeader(AUConstants.X_V_HEADER_PRODUCTS)
                .header(AUConstants.X_FAPI_AUTH_DATE, AUConstants.DATE)
                .header(AUConstants.X_CDS_CLIENT_HEADERS , clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PRODUCTS))
                .get("${AUConstants.BANKING_PRODUCT_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_PRODUCTS)
        Assert.assertTrue(response.getHeader(AUConstants.CONTENT_TYPE).contains(AUConstants.ACCEPT))

        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_SELF))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_FIRST))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_PREV))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_NEXT))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_LAST))
    }

    @Test
    void "TC1101006_Retrieve Product list without x-v Header"() {

        def response = AURestAsRequestBuilder.buildRequest()
                .header(AUConstants.X_MIN_HEADER , AUConstants.X_V_HEADER_PRODUCTS)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PRODUCTS))
                .get("${AUConstants.BANKING_PRODUCT_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_CODE),
                AUConstants.ERROR_CODE_MISSING_HEADER)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_TITLE), AUConstants.MISSING_HEADER)
    }

    @Test
    void "TC1101007_Retrieve Product list without x-min-v Header"() {

        def response = AURequestBuilder.buildBasicRequestWithoutAuthorisationHeader(AUConstants.X_V_HEADER_PRODUCTS)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PRODUCTS))
                .get("${AUConstants.BANKING_PRODUCT_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_PRODUCTS)
        Assert.assertTrue(response.getHeader(AUConstants.CONTENT_TYPE).contains(AUConstants.ACCEPT))

        Assert.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.DATA))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_SELF))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_FIRST))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_PREV))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_NEXT))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_LAST))
    }

    @Test
    void "TC1101008_Retrieve Product list with negative x-v Header"() {

        def response = AURequestBuilder.buildBasicRequestWithoutAuthorisationHeader(-2)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PRODUCTS))
                .get("${AUConstants.BANKING_PRODUCT_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_CODE),
                AUConstants.ERROR_CODE_INVALID_VERSION)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_TITLE), AUConstants.INVALID_VERSION)
    }

    @Test
    void "TC1101009_Retrieve Product list with decimal x-v Header"() {

        def response = AURestAsRequestBuilder.buildRequest()
                .header(AUConstants.X_V_HEADER, 1.2)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PRODUCTS))
                .get("${AUConstants.BANKING_PRODUCT_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_CODE),
                AUConstants.ERROR_CODE_INVALID_VERSION)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_TITLE), AUConstants.INVALID_VERSION)
    }

    @Test
    void "TC1101010_Retrieve Product list with zero value as x-v Header"() {

        def response = AURequestBuilder.buildBasicRequestWithoutAuthorisationHeader(0)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PRODUCTS))
                .get("${AUConstants.BANKING_PRODUCT_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_CODE),
                AUConstants.ERROR_CODE_INVALID_VERSION)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_TITLE), AUConstants.INVALID_VERSION)
    }

    @Test
    void "TC1101011_Retrieve Product list with negative x-min-v Header"() {

        def response = AURequestBuilder.buildBasicRequestWithoutAuthorisationHeader(AUConstants.X_V_HEADER_PRODUCTS)
                .header(AUConstants.X_MIN_HEADER, -1)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PRODUCTS))
                .get("${AUConstants.BANKING_PRODUCT_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_CODE),
                AUConstants.ERROR_CODE_INVALID_VERSION)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_TITLE), AUConstants.INVALID_VERSION)
    }

    @Test
    void "TC1101012_Retrieve Product list with decimal x-min-v Header"() {

        def response = AURequestBuilder.buildBasicRequestWithoutAuthorisationHeader(AUConstants.X_V_HEADER_PRODUCTS)
                .header(AUConstants.X_MIN_HEADER, 1.0)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PRODUCTS))
                .get("${AUConstants.BANKING_PRODUCT_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_CODE),
                AUConstants.ERROR_CODE_INVALID_VERSION)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_TITLE), AUConstants.INVALID_VERSION)
    }

    @Test
    void "TC1101013_Retrieve Product list with header x-min-v greater than the x-v"() {

        def response = AURequestBuilder.buildBasicRequestWithoutAuthorisationHeader(AUConstants.X_V_HEADER_PRODUCTS)
                .header(AUConstants.X_MIN_HEADER, 5)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PRODUCTS))
                .get("${AUConstants.BANKING_PRODUCT_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_PRODUCTS)
        Assert.assertTrue(response.getHeader(AUConstants.CONTENT_TYPE).contains(AUConstants.ACCEPT))

        Assert.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.DATA))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_SELF))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_FIRST))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_PREV))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_NEXT))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_LAST))
    }

    @Test
    void "TC1101014_Retrieve Product list with header x-min-v equals to the x-v"() {

        def response = AURequestBuilder.buildBasicRequestWithoutAuthorisationHeader(AUConstants.X_V_HEADER_PRODUCTS)
                .header(AUConstants.X_MIN_HEADER, AUConstants.X_V_HEADER_PRODUCTS)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PRODUCTS))
                .get("${AUConstants.BANKING_PRODUCT_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_PRODUCTS)
        Assert.assertTrue(response.getHeader(AUConstants.CONTENT_TYPE).contains(AUConstants.ACCEPT))

        Assert.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.DATA))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_SELF))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_FIRST))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_PREV))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_NEXT))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_LAST))
    }

    @Test
    void "TC1101015_Retrieve Product list with header x-min-v less than the x-v"() {

        def response = AURequestBuilder.buildBasicRequestWithoutAuthorisationHeader(AUConstants.X_V_HEADER_PRODUCTS)
                .header(AUConstants.X_MIN_HEADER, 1)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PRODUCTS))
                .get("${AUConstants.BANKING_PRODUCT_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_PRODUCTS)
        Assert.assertTrue(response.getHeader(AUConstants.CONTENT_TYPE).contains(AUConstants.ACCEPT))

        Assert.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.DATA))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_SELF))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_FIRST))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_PREV))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_NEXT))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_LAST))
    }

    @Test
    void "TC1101016_Retrieve Product with unsupported endpoint version"() {

        def response = AURequestBuilder.buildBasicRequestWithoutAuthorisationHeader(AUConstants.UNSUPPORTED_X_V_VERSION)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PRODUCTS))
                .get("${AUConstants.BANKING_PRODUCT_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_406)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_CODE),
                AUConstants.ERROR_CODE_UNSUPPORTED_VERSION)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_TITLE), AUConstants
                .UNSUPPORTED_VERSION)
    }

    //before executing need to configure  <HolderIdentifier>HID</HolderIdentifier> in open-banking xml file
    //of OB_KM to the value set in {holderID} below
    @Test
    void "TC1101017_Retrieve Product with unsupported endpoint version with holder identifier header"() {

        def holderID = "HID"

        def response = AURequestBuilder.buildBasicRequestWithoutAuthorisationHeader(AUConstants.X_V_HEADER_PRODUCTS)
                .header(AUConstants.X_FAPI_AUTH_DATE, AUConstants.DATE)
                .header(AUConstants.X_CDS_CLIENT_HEADERS , clientHeader)
                .header("x-${holderID}-v", AUConstants.UNSUPPORTED_X_V_VERSION)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PRODUCTS))
                .get("${AUConstants.BANKING_PRODUCT_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        Assert.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_PRODUCTS)
        Assert.assertTrue(response.getHeader(AUConstants.CONTENT_TYPE).contains(AUConstants.ACCEPT))
    }
}

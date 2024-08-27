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

package com.wso2.cds.integration.test.banking_products

import com.wso2.cds.test.framework.AUTest
import com.wso2.cds.test.framework.constant.AUConstants
import com.wso2.cds.test.framework.request_builder.AURequestBuilder
import com.wso2.cds.test.framework.utility.AUTestUtil
import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

import java.nio.charset.Charset

/**
 * Class contains Product Retrieval Validation Tests.
 */
class ProductRetrievalValidationTest extends AUTest {

    def clientHeader = "${Base64.encoder.encodeToString(getCDSClient().getBytes(Charset.defaultCharset()))}"

    @BeforeClass(alwaysRun = true)
    void "Get User Access Token"() {
        doConsentAuthorisation()
        generateUserAccessToken()
    }

    @Test (priority = 1, groups = "SmokeTest")
    void "TC1101001_Retrieve banking products"() {

        Response response = AURequestBuilder.buildBasicRequestWithoutAuthorisationHeader(AUConstants.X_V_HEADER_PRODUCTS)
                .header(AUConstants.X_FAPI_AUTH_DATE, AUConstants.DATE)
                .header(AUConstants.X_CDS_CLIENT_HEADERS , clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PRODUCTS))
                .get("${AUConstants.BANKING_PRODUCT_PATH}")

        productId = AUTestUtil.parseResponseBody(response, "${AUConstants.RESPONSE_DATA_PRODUCTS}.productId[0]").toString()

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_PRODUCTS)

        Assert.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "${AUConstants.RESPONSE_DATA_PRODUCTS}.productId"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "${AUConstants.RESPONSE_DATA_PRODUCTS}.lastUpdated"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "${AUConstants.RESPONSE_DATA_PRODUCTS}.productCategory"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "${AUConstants.RESPONSE_DATA_PRODUCTS}.name"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "${AUConstants.RESPONSE_DATA_PRODUCTS}.description"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "${AUConstants.RESPONSE_DATA_PRODUCTS}.brand"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "${AUConstants.RESPONSE_DATA_PRODUCTS}.isTailored"))
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, "${AUConstants.RESPONSE_DATA_PRODUCTS}.effectiveFrom[0]"),
                AUConstants.CURRENT)
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_SELF))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_FIRST))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_PREV))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_NEXT))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_LAST))
    }

    @Test (priority = 1, dependsOnMethods = "TC1101001_Retrieve banking products")
    void "TC1101002_Retrieve specific banking product details"() {

        Response response = AURequestBuilder.buildBasicRequestWithoutAuthorisationHeader(AUConstants.X_V_HEADER_PRODUCTS)
                .header(AUConstants.X_FAPI_AUTH_DATE, AUConstants.DATE)
                .header(AUConstants.X_CDS_CLIENT_HEADERS , clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PRODUCTS))
                .get("${AUConstants.BANKING_PRODUCT_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_PRODUCTS)

        Assert.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.RESPONSE_DATA_PRODUCTS+".productId"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.RESPONSE_DATA_PRODUCTS+".effectiveFrom"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.RESPONSE_DATA_PRODUCTS+".productCategory"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.RESPONSE_DATA_PRODUCTS+".additionalInformation"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_SELF))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_FIRST))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_PREV))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_NEXT))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_LAST))
    }

    @Test
    void "TC1101003_Retrieve specific banking product details for invalid product id"() {

        Response response = AURequestBuilder.buildBasicRequestWithoutAuthorisationHeader(AUConstants.X_V_HEADER_PRODUCT)
                .header(AUConstants.X_FAPI_AUTH_DATE, AUConstants.DATE)
                .header(AUConstants.X_CDS_CLIENT_HEADERS , clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PRODUCTS))
                .get("${AUConstants.BANKING_PRODUCT_PATH}/12345")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_PRODUCT)
        Assert.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.productId"))
    }

    //TODO: Issue: https://github.com/wso2-enterprise/financial-open-banking/issues/5558
    @Test (enabled = false)
    void "TC1101019_Retrieve banking products with page size greater than the maximum standard pagination"() {

        Response response = AURequestBuilder.buildBasicRequestWithoutAuthorisationHeader(AUConstants.X_V_HEADER_PRODUCTS)
                .header(AUConstants.X_FAPI_AUTH_DATE, AUConstants.DATE)
                .header(AUConstants.X_CDS_CLIENT_HEADERS , clientHeader)
                .queryParam(AUConstants.PAGE_SIZE, 200000)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PRODUCTS))
                .get("${AUConstants.BANKING_PRODUCT_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_PRODUCTS)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_CODE),
                AUConstants.ERROR_CODE_INVALID_PAGE)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DETAIL),
                "Schema validation failed in the Request: Numeric instance is greater than the required maximum " +
                        "(maximum: 1000, found: 200000),")
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_TITLE), AUConstants.INVALID_PAGE)
    }

    @Test
    void "TC1101020_Retrieve banking products with filters"() {

        String queryParams = "?effective=CURRENT&brand=TEST&product-category=TRANS_AND_SAVINGS_ACCOUNTS&page=2&" +
                "page-size=25&updated-since=2019-12-25T15:43:00-08:00"
        Response response = AURequestBuilder.buildBasicRequestWithoutAuthorisationHeader(AUConstants.X_V_HEADER_PRODUCTS)
                .header(AUConstants.X_FAPI_AUTH_DATE, AUConstants.DATE)
                .header(AUConstants.X_CDS_CLIENT_HEADERS , clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PRODUCTS))
                .get("${AUConstants.BANKING_PRODUCT_PATH}${queryParams}")

        productId = AUTestUtil.parseResponseBody(response, "${AUConstants.RESPONSE_DATA_PRODUCTS}.productId[0]").toString()

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_PRODUCTS)

        Assert.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, "${AUConstants.RESPONSE_DATA_PRODUCTS}.effectiveFrom[0]"),
                AUConstants.CURRENT)
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "${AUConstants.RESPONSE_DATA_PRODUCTS}.productId"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_SELF))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_FIRST))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_PREV))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_NEXT))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_LAST))
    }

    @Test
    void "TC1101021_Retrieve banking products with invalid updated-since value"() {

        def updatedSince = AUConstants.DATE_FORMAT
        def response = AURequestBuilder.buildBasicRequestWithoutAuthorisationHeader(AUConstants.X_V_HEADER_PRODUCTS)
                .header(AUConstants.X_FAPI_AUTH_DATE, AUConstants.DATE)
                .header(AUConstants.X_CDS_CLIENT_HEADERS , clientHeader)
                .queryParam(AUConstants.UPDATED_SINCE, updatedSince)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PRODUCTS))
                .get("${AUConstants.BANKING_PRODUCT_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_PRODUCTS)

        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_CODE),
                AUConstants.ERROR_CODE_INVALID_FIELD)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DETAIL),
                "Schema validation failed in the Request: String \"${updatedSince}\" is invalid " +
                        "against requested date format(s) [yyyy-MM-dd'T'HH:mm:ssZ, yyyy-MM-dd'T'HH:mm:ss.[0-9]{1,12}Z],")
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_TITLE), AUConstants.INVALID_FIELD)
    }

    //TODO: Git issue : https://github.com/wso2-enterprise/financial-open-banking/issues/5638
    @Test (enabled = false)
    void "TC1101024_Retrieve banking products with invalid brand value"() {

        Response response = AURequestBuilder.buildBasicRequestWithoutAuthorisationHeader(AUConstants.X_V_HEADER_PRODUCTS)
                .queryParam(AUConstants.BRAND, 123)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get("${AUConstants.BANKING_PRODUCT_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_PRODUCTS)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_CODE),
                AUConstants.ERROR_CODE_INVALID_FIELD)
        //TODO: Update error after fixing the issue.
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DETAIL), "")
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_TITLE), AUConstants.INVALID_FIELD)
    }

    @Test
    void "TC1101027_Retrieve Product list with undefined query parameter"() {

        def response = AURequestBuilder.buildBasicRequestWithoutAuthorisationHeader(AUConstants.X_V_HEADER_PRODUCTS)
                .queryParam(AUConstants.OPEN_STATUS_PARAM, AUConstants.STATUS_OPEN)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PRODUCTS))
                .get("${AUConstants.BANKING_PRODUCT_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_PRODUCTS)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_CODE),
                AUConstants.ERROR_CODE_INVALID_FIELD)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DETAIL),
                "Schema validation failed in the Request: Query parameter 'open-status' is unexpected on path " +
                        "\"/banking/products\",")
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_TITLE), AUConstants.INVALID_FIELD)
    }

    //TODO: Git Issue: https://github.com/wso2-enterprise/financial-open-banking/issues/5557
    //@Test
    void "TC1101029_Retrieve banking products with effectiveFROM value and effectiveTO value equal to ALL"() {

        String queryParams = "?effective=ALL"

        Response response = AURequestBuilder.buildBasicRequestWithoutAuthorisationHeader(AUConstants.X_V_HEADER_PRODUCTS)
                .queryParam(AUConstants.EFFECTIVE, AUConstants.ALL)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PRODUCTS))
                .get("${AUConstants.BANKING_PRODUCT_PATH}${queryParams}")

        productId = AUTestUtil.parseResponseBody(response, "${AUConstants.RESPONSE_DATA_PRODUCTS}.productId[0]").toString()

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_PRODUCTS)

        Assert.assertNotNull(response.getHeader(AUConstants.X_FAPI_INTERACTION_ID))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "${AUConstants.RESPONSE_DATA_PRODUCTS}.productId"))
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, "${AUConstants.RESPONSE_DATA_PRODUCTS}.effectiveFrom[0]"),
                AUConstants.ALL)
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_SELF))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_FIRST))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_PREV))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_NEXT))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.LINKS_LAST))
    }

    @Test
    void "TC1101031_Retrieve product list with invalid product-category value"() {

        def response = AURequestBuilder.buildBasicRequestWithoutAuthorisationHeader(AUConstants.X_V_HEADER_PRODUCTS)
                .queryParam(AUConstants.PRODUCT_CATEGORY, "TRANS")
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_PRODUCTS))
                .get("${AUConstants.BANKING_PRODUCT_PATH}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_PRODUCTS)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_CODE),
                AUConstants.ERROR_CODE_INVALID_FIELD)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DETAIL),
                "Schema validation failed in the Request: Instance value (\"TRANS\") not found in enum " +
                        "(possible values: [\"BUSINESS_LOANS\",\"CRED_AND_CHRG_CARDS\",\"LEASES\"," +
                        "\"MARGIN_LOANS\",\"OVERDRAFTS\",\"PERS_LOANS\",\"REGULATED_TRUST_ACCOUNTS\"," +
                        "\"RESIDENTIAL_MORTGAGES\",\"TERM_DEPOSITS\",\"TRADE_FINANCE\"," +
                        "\"TRANS_AND_SAVINGS_ACCOUNTS\",\"TRAVEL_CARDS\"]),")
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_TITLE), AUConstants.INVALID_FIELD)
        }
}

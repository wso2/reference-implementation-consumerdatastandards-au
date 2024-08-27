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

package com.wso2.cds.test.framework.utility

import com.wso2.cds.test.framework.constant.AUConstants
import com.wso2.cds.test.framework.configuration.AUConfigurationService
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.restassured.RestAssured
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.testng.Assert

/**
 * The util class to invoke the Mock CDR Registration endpoints
 */
class AUMockCDRIntegrationUtil {

    static Logger log = LogManager.getLogger(AUMockCDRIntegrationUtil.class.getName())
    static AUConfigurationService auConfiguration = new AUConfigurationService()


    /**
     * Get input array
     */
    static def getInputArray() {
        def inputFile = new File(auConfiguration.getMockCDRMetaDataFileLoc())
        return new JsonSlurper().parseText(inputFile.text)
    }

    /**
     * Invoke the admin API to load custom metadata from MockCDRRegisterMetaData.json to the Mock CDR Register
     *
     * @return boolean
     */
    static boolean loadMetaDataToCDRRegister() {

        if (auConfiguration.getMockCDREnabled()) {
            def inputArray = getInputArray()
            def payloadJson = JsonOutput.toJson(inputArray)

            RestAssured.baseURI = AUConstants.MOCK_ADMIN_BASE_URL
            def MetaDataLoadResponse = AURestAsRequestBuilder.buildRequestToMockCDRRegister(false)
                    .contentType(AUConstants.CONTENT_TYPE_APPLICATION_JSON)
                    .body(payloadJson)
                    .post(AUConstants.MOCK_METADATA_ENDPOINT)

            if (Assert.assertEquals(MetaDataLoadResponse.statusCode(), AUConstants.STATUS_CODE_200)) {
                return true
            }
            return false
        }
        return true
    }

    /**
     * Invoke the admin API to update custom metadata of MockCDRRegisterMetaData.json to the Mock CDR Register
     *
     * @return boolean
     */
    static boolean updateMetaDataOfCDRRegister(boolean changeStatus_SP, boolean changeStatus_ADR, int spStatus = 1, int adrStatus = 1) {

        if (auConfiguration.getMockCDREnabled()) {
            def inputArray = getInputArray()

            if (changeStatus_SP) {
                inputArray["LegalEntities"][0]["Participations"][0]["Brands"][0]["SoftwareProducts"][0]["StatusId"] = spStatus
            }

            if (changeStatus_ADR) {
                inputArray["LegalEntities"][0]["Participations"][0]["StatusId"] = adrStatus
            }

            def payloadJson = JsonOutput.toJson(inputArray)

            RestAssured.baseURI = AUConstants.MOCK_ADMIN_BASE_URL
            def MetaDataLoadResponse = AURestAsRequestBuilder.buildRequestToMockCDRRegister(false)
                    .contentType(AUConstants.CONTENT_TYPE_APPLICATION_JSON)
                    .body(payloadJson)
                    .post(AUConstants.MOCK_METADATA_ENDPOINT)

            if (Assert.assertEquals(MetaDataLoadResponse.statusCode(), AUConstants.STATUS_CODE_200)) {
                return true
            }
            return false
        }
        return true
    }

    /**
     * Get Application Access Token to invoke the protected API endpoints
     *
     * @param softwareProductId the software product Id of the ADR client
     * @return access token
     */
    static String getApplicationTokenFromMockCDRRegister(String softwareProductId) {

        String clientAssertion = generateClientAssertionFromMockCDRRegister(softwareProductId).toString()

        def payload = ""
        def delimiter = "&"
        payload = payload.concat(AUConstants.GRANT_TYPE_KEY + "=" + "client_credentials" + delimiter)
                .concat(AUConstants.CLIENT_ID_KEY + "=" + softwareProductId + delimiter)
                .concat(AUConstants.CLIENT_ASSERTION_TYPE_KEY + "=" + "urn:ietf:params:oauth:client-assertion-type:jwt-bearer" + delimiter)
                .concat(AUConstants.CLIENT_ASSERTION_KEY + "=" + clientAssertion + delimiter)
                .concat(AUConstants.SCOPE_KEY + "=" + "cdr-register:bank:read")

        RestAssured.baseURI = AUConstants.MOCK_INFO_SEC_BASE_URL
        def tokenResponse = AURestAsRequestBuilder.buildRequestToMockCDRRegister(true)
                .contentType(AUConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .body(payload)
                .post(AUConstants.TOKEN_ENDPOINT)

        def accessToken = AUTestUtil.parseResponseBody(tokenResponse, "access_token")
        log.info("Got access token $accessToken")

        return accessToken
    }

    /**
     * Invoke mock CDR client assertion endpoint to generate the client assertion for a given softwareProductId
     *
     * @param softwareProductId the software product Id of the ADR client
     * @return
     */
    static String generateClientAssertionFromMockCDRRegister(String softwareProductId) {

        RestAssured.baseURI = AUConstants.MOCK_ADMIN_BASE_URL
        def clientAssertionResponse = AURestAsRequestBuilder.buildRequestToMockCDRRegister(false)
                .queryParam("iss", softwareProductId)
                .get("${AUConstants.MOCK_CLIENT_ASSERTION_ENDPOINT}")

        return clientAssertionResponse.getBody().asString()

    }

    /**
     * Invoke the SSA API to retrieve the SSA for a given softwareProductId and ADRBrandId
     *
     * @param accessToken
     * @param ADRBrandId the Brand Id of the ADR client
     * @param softwareProductId the software product Id of the ADR client
     * @return SSA String
     */
    static String getSSAFromMockCDRRegister(String ADRBrandId, String softwareProductId) {

        def accessToken = getApplicationTokenFromMockCDRRegister(softwareProductId)

        RestAssured.baseURI = AUConstants.MOCK_INFO_SEC_BASE_URL
        def SSAResponse = AURestAsRequestBuilder.buildRequestToMockCDRRegister(true)
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, "${AUConstants.AUTHORIZATION_BEARER_TAG}${accessToken}")
                .get("${AUConstants.MOCK_SSA_ENDPOINT}/${ADRBrandId}/software-products/${softwareProductId}/ssa")

        return SSAResponse.getBody().asString()

    }

}


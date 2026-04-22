/**
 * Copyright (c) 2024 - 2026, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.cds.keymanager.test.secondaryUser

import org.wso2.cds.test.framework.AUTest
import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.constant.AUPayloads
import org.wso2.cds.test.framework.utility.AURestAsRequestBuilder
import org.wso2.cds.test.framework.utility.AUTestUtil
import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

import java.nio.charset.Charset

/**
 * Ceasing Secondary User Management Test. Test Class Verifies the Manage Sharing status and Get Sharing Status Endpoints.
 */
class CeasingSecondaryUserManagementTest extends AUTest {

    def shareableElements, clientHeader
    String accountID, userId, legalEntityId, altLegalEntityId, accountOwnerId
    Response response
    List <String> legalEntityList

    private def updateLegalEntitySharingStatus(String accountId, String secondaryUserId, String legalEntityId,
                            String legalEntityStatus, boolean isMultipleLegalEntity = false,
                            String secondaryUserId2 = null, String accountId2 = null,
                            String legalEntityId2 = null, String legalEntityStatus2 = null) {

    def requestBody = AUPayloads.getBlockLegalEntityPayload(secondaryUserId, accountId, legalEntityId,
        legalEntityStatus, isMultipleLegalEntity, secondaryUserId2, accountId2, legalEntityId2,
        legalEntityStatus2)

    return AURestAsRequestBuilder.buildBasicRequest()
        .header(AUConstants.AUTHORIZATION_HEADER_KEY, AUConstants.BASIC_HEADER_KEY + " " +
            Base64.encoder.encodeToString(
                "${auConfiguration.getUserBasicAuthName()}:${auConfiguration.getUserBasicAuthPWD()}"
                    .getBytes(Charset.forName("UTF-8"))))
        .contentType(AUConstants.CONTENT_TYPE_APPLICATION_JSON)
        .body(requestBody)
        .baseUri(getAuConfiguration().getServerAuthorisationServerURL())
        .post(AUConstants.UPDATE_LEGAL_ENTITY_SHARING_STATUS)
    }

    private def getLegalEntitySharingStatuses(String userId, String accountId, String clientId) {

    return AURestAsRequestBuilder.buildBasicRequest()
        .header(AUConstants.AUTHORIZATION_HEADER_KEY, AUConstants.BASIC_HEADER_KEY + " " +
            Base64.encoder.encodeToString(
                "${auConfiguration.getUserBasicAuthName()}:${auConfiguration.getUserBasicAuthPWD()}"
                    .getBytes(Charset.forName("UTF-8"))))
        .contentType(AUConstants.CONTENT_TYPE_APPLICATION_JSON)
        .baseUri(getAuConfiguration().getServerAuthorisationServerURL())
        .queryParam("userId", userId)
        .queryParam("accountIds", accountId)
        .queryParam("clientId", clientId)
        .get(AUConstants.LEGAL_ENTITY_LIST_ENDPOINT)
    }

    @BeforeClass (alwaysRun = true)
    void "Pre Execution Step"() {

        auConfiguration.setTppNumber(0)
        auConfiguration.setPsuNumber(4)
        accountOwnerId = auConfiguration.getUserPSUName()

        auConfiguration.setPsuNumber(1)
        clientId = auConfiguration.getAppInfoClientID()
        clientHeader = "${Base64.encoder.encodeToString(getCDSClient().getBytes(Charset.defaultCharset()))}"

        //Get Sharable Account List and Secondary User with Authorize Permission
        shareableElements = AUTestUtil.getSecondaryUserDetails(getSharableBankAccounts())

        accountID =  shareableElements[AUConstants.PARAM_ACCOUNT_ID]
        userId = auConfiguration.getUserPSUName()

        //Give Secondary User Instruction Permission
        def updateResponse = updateSecondaryUserInstructionPermission(accountID, userId, AUConstants.ACTIVE)
        Assert.assertEquals(updateResponse.statusCode(), AUConstants.OK)

        // Resolve legal entity ID from API response to keep tests runtime-agnostic.
        def legalEntityResponse = getLegalEntitySharingStatuses(userId, accountID, clientId)
        Assert.assertEquals(legalEntityResponse.statusCode(), AUConstants.STATUS_CODE_200)
        legalEntityId = AUTestUtil.parseResponseBody(legalEntityResponse, "[0].${AUConstants.LEGAL_ENTITY_ID_MAP}")
        Assert.assertNotNull(legalEntityId)
        altLegalEntityId = AUConstants.ALT_LEGAL_ENTITY
    }

    @Test (groups = "SmokeTest", priority = 1)
    void "CDS-631_Block the sharing status for a legal entity"() {

        response = updateLegalEntitySharingStatus(accountID, userId, legalEntityId.toLowerCase(), AUConstants.BLOCK_ENTITY)
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //Check Sharing Status
        def responseSharingStatusLegalEntity = getSharingStatusOfUserAccount(getLegalEntitySharingStatuses(userId, accountID, clientId).getBody().asString(),
                userId, accountID, legalEntityId)
        Assert.assertEquals(responseSharingStatusLegalEntity, AUConstants.BLOCK_ENTITY)
    }

    @Test (priority = 1, dependsOnMethods = "CDS-631_Block the sharing status for a legal entity")
    void "CDS-632_Block an already blocked legal entity"() {

        response = updateLegalEntitySharingStatus(accountID, userId, legalEntityId.toLowerCase(), AUConstants.BLOCK_ENTITY)
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //Check Sharing Status
        def responseSharingStatusLegalEntity = getSharingStatusOfUserAccount(getLegalEntitySharingStatuses(userId, accountID, clientId).getBody().asString(),
                userId, accountID, legalEntityId)
        Assert.assertEquals(responseSharingStatusLegalEntity, AUConstants.BLOCK_ENTITY)
    }

    @Test (priority = 1, dependsOnMethods = "CDS-632_Block an already blocked legal entity")
    void "CDS-633_Unlock the sharing status for a legal entity"() {

        response = updateLegalEntitySharingStatus(accountID, userId, legalEntityId.toLowerCase(), AUConstants.ACTIVE)
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //Check Sharing Status
        def responseSharingStatusLegalEntity = getSharingStatusOfUserAccount(getLegalEntitySharingStatuses(userId, accountID, clientId).getBody().asString(),
                userId, accountID, legalEntityId)
        Assert.assertEquals(responseSharingStatusLegalEntity, AUConstants.ACTIVE)
    }

    @Test
    void "CDS-634_Block multiple legal entities for same user and same account id"() {

        response = updateLegalEntitySharingStatus(accountID, userId, legalEntityId, AUConstants.BLOCK_ENTITY,
                true, userId, accountID, altLegalEntityId, AUConstants.BLOCK_ENTITY)
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)

        //Check Sharing Status - Legal Entity 1
        def responseSharingStatusLegalEntity1 = getSharingStatusOfUserAccount(getLegalEntitySharingStatuses(userId, accountID, clientId).getBody().asString(),
                userId, accountID, legalEntityId)
        Assert.assertEquals(responseSharingStatusLegalEntity1, AUConstants.BLOCK_ENTITY)
    }

    @Test
    void "CDS-635_Block sharing status with incorrect status value"() {

        response = updateLegalEntitySharingStatus(accountID, userId, legalEntityId.toLowerCase(), "Block_Entity")
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)

        Assert.assertTrue(AUTestUtil.parseResponseBody(response, "errorDescription")
            .contains("not one of the values accepted for Enum class"))
    }

    @Test
    void "CDS-639_Block sharing status for multiple user Ids"() {

        response = updateLegalEntitySharingStatus(accountID, userId, legalEntityId.toLowerCase(), AUConstants.BLOCK_ENTITY,
                true, "admin@wso2.com", accountID, altLegalEntityId.toLowerCase(), AUConstants.BLOCK_ENTITY)
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
    }

    @Test
    void "CDS-640_Blocking and activating sharing status for legal entities via same request"() {

        response = updateLegalEntitySharingStatus(accountID, userId, legalEntityId.toLowerCase(), AUConstants.ACTIVE,
                true, userId, accountID, altLegalEntityId.toLowerCase(), AUConstants.BLOCK_ENTITY)
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
    }

    @Test (dependsOnMethods = "CDS-640_Blocking and activating sharing status for legal entities via same request")
    void "CDS-641_Retrieve sharing status of a particular legal entity"() {

        def responseSharingStatusLegalEntity1 = getSharingStatusOfUserAccount(getLegalEntitySharingStatuses(userId, accountID, clientId).getBody().asString(),
                userId, accountID, legalEntityId)
        Assert.assertEquals(responseSharingStatusLegalEntity1, AUConstants.ACTIVE)
    }

    @Test
    void "CDS-642_Account owner is able to view all the accounts, secondary users, legal entities"() {

        def response = getLegalEntitySharingStatuses(userId, accountID, clientId)
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "[0].${AUConstants.PAYLOAD_PARAM_ACCOUNT_ID}"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "[0].${AUConstants.SECONDARY_USERS_USERID}"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "[0].${AUConstants.LEGAL_ENTITY_ID_MAP}"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "[0].${AUConstants.SHARING_STATUS}"))
    }

    //TODO: Enable after investigating issue: https://github.com/wso2/financial-services-accelerator/issues/215
//    @Test
    void "CDS-636_Block sharing status with incorrect accountId"() {

        response = updateLegalEntityStatus(clientHeader, "1234", userId, legalEntityId, AUConstants.BLOCK_ENTITY)
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)

        //TODO: Issue: https://github.com/wso2-enterprise/financial-open-banking/issues/8275
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, "errorDescription"),
                "Error occurred while updating the sharing status for a legal entity/entities.")
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR), AUConstants.INVALID_REQUEST)
    }

    //TODO: Enable after investigating issue: https://github.com/wso2/financial-services-accelerator/issues/215
//    @Test
    void "CDS-637_Block sharing status with incorrect user id"() {

        response = updateLegalEntityStatus(clientHeader, accountID, "abc@gold.com", legalEntityId, AUConstants.BLOCK_ENTITY)
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)

        //TODO: Issue: https://github.com/wso2-enterprise/financial-open-banking/issues/8275
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, "errorDescription"),
                "Error occurred while updating the sharing status for a legal entity/entities.")
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR), AUConstants.INVALID_REQUEST)
    }
}

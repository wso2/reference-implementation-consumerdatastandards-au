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

package org.wso2.cds.keymanager.test.bnr

import org.wso2.cds.test.framework.AUTest
import org.wso2.cds.test.framework.constant.AUBusinessUserPermission
import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.constant.AUPayloads
import org.wso2.cds.test.framework.utility.AURestAsRequestBuilder
import org.wso2.cds.test.framework.utility.AUTestUtil
import org.testng.Assert
import org.testng.annotations.Test

import java.nio.charset.Charset

/**
 * User Nomination Management Test cases.
 */
class UserNominationManagementTests extends AUTest {

    def clientHeader = "${Base64.encoder.encodeToString(getCDSClient().getBytes(Charset.defaultCharset()))}"

    /**
     * Add business user nomination for a single user via POST.
     * @param accountID account id
     * @param accountOwnerUserID account owner id
     * @param nominatedRepUserID nominated representative id
     * @param permissionType permission type
     * @return response
     */
    private static def addSingleBusinessUserPermission(String accountID, String accountOwnerUserID,
                                                       String nominatedRepUserID, String permissionType) {

        def requestBody = AUPayloads.getSingleUserNominationPayload(accountID, accountOwnerUserID, nominatedRepUserID,
                permissionType)

        return AURestAsRequestBuilder.buildBasicRequest()
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, AUConstants.BASIC_HEADER_KEY + " " +
                        Base64.encoder.encodeToString(
                                "${auConfiguration.getUserBasicAuthName()}:${auConfiguration.getUserBasicAuthPWD()}"
                                        .getBytes(Charset.forName("UTF-8"))))
                .contentType(AUConstants.CONTENT_TYPE_APPLICATION_JSON)
                .body(requestBody)
                .baseUri(getAuConfiguration().getServerAuthorisationServerURL())
                .post(AUConstants.UPDATE_BUSINESS_USER)
    }

    @Test(groups = "SmokeTest")
    void "CDS-590_Verify the UpdateBusiness User with Valid inputs if success response is retrieved"() {

        def shareableElements = AUTestUtil.getSharableAccountsList(getSharableBankAccounts())

        String accountID =  shareableElements[AUConstants.PARAM_ACCOUNT_ID]
        String accountOwnerUserID = shareableElements[AUConstants.ACCOUNT_OWNER_USER_ID]
        String nominatedRepUserID = shareableElements[AUConstants.NOMINATED_REP_USER_ID]

        //add record before updating
        def addResponse = addSingleBusinessUserPermission(accountID, accountOwnerUserID,
                nominatedRepUserID, AUBusinessUserPermission.AUTHORIZE.getPermissionString())
        Assert.assertEquals(addResponse.statusCode(), AUConstants.CREATED)

        //Update the Business User endpoint with the relevant Permission Status
        def updateResponse = updateSingleBusinessUserPermission(clientHeader, accountID, accountOwnerUserID,
                nominatedRepUserID, AUBusinessUserPermission.AUTHORIZE.getPermissionString())
        Assert.assertEquals(updateResponse.statusCode(), AUConstants.OK)

        //Check the permissions of nominated representatives
        def permissionsResponse = getStakeholderPermissions(nominatedRepUserID, accountID)
        Assert.assertEquals(permissionsResponse.statusCode(), AUConstants.OK)
        Assert.assertTrue(hasBnrPermissionInResponse(permissionsResponse, nominatedRepUserID,
                AUBusinessUserPermission.AUTHORIZE.getPermissionString()))
    }

    @Test
    void "CDS-606_Verify add business user via POST returns 201 for new record"() {

        def shareableElements = AUTestUtil.getSharableAccountsList(getSharableBankAccounts())

        String accountID = shareableElements[AUConstants.PARAM_ACCOUNT_ID]
        String accountOwnerUserID = shareableElements[AUConstants.ACCOUNT_OWNER_USER_ID]
        String nominatedRepUserID = shareableElements[AUConstants.NOMINATED_REP_USER_ID]

        // Ensure clean state for deterministic create behavior.
        deleteSingleBusinessUser(clientHeader, accountID, accountOwnerUserID, nominatedRepUserID)

        def addResponse = addSingleBusinessUserPermission(accountID, accountOwnerUserID,
                nominatedRepUserID, AUBusinessUserPermission.AUTHORIZE.getPermissionString())
        Assert.assertTrue(addResponse.statusCode() == AUConstants.STATUS_CODE_201)
    }

    @Test
    void "CDS-607_Verify add business user via POST returns 200 for duplicate record"() {

        def shareableElements = AUTestUtil.getSharableAccountsList(getSharableBankAccounts())

        String accountID = shareableElements[AUConstants.PARAM_ACCOUNT_ID]
        String accountOwnerUserID = shareableElements[AUConstants.ACCOUNT_OWNER_USER_ID]
        String nominatedRepUserID = shareableElements[AUConstants.NOMINATED_REP_USER_ID]

        // First call creates the record.
        addSingleBusinessUserPermission(accountID, accountOwnerUserID,
                nominatedRepUserID, AUBusinessUserPermission.AUTHORIZE.getPermissionString())

        // Second call is idempotent for the same account-user pair.
        def addDuplicateResponse = addSingleBusinessUserPermission(accountID, accountOwnerUserID,
                nominatedRepUserID, AUBusinessUserPermission.AUTHORIZE.getPermissionString())
        Assert.assertEquals(addDuplicateResponse.statusCode(), AUConstants.OK)
    }

    @Test
    void "CDS-539_Verify adding multiple nominated representatives for a single account"() {

        def shareableElements = AUTestUtil.getSharableAccountsList(getSharableBankAccounts())

        String accountID =  shareableElements[AUConstants.PARAM_ACCOUNT_ID]
        String accountOwnerUserID = shareableElements[AUConstants.ACCOUNT_OWNER_USER_ID]
        String nominatedRepUserID = shareableElements[AUConstants.NOMINATED_REP_USER_ID]
        String nominatedRepUserID2 = shareableElements[AUConstants.NOMINATED_REP_USER_ID2]

        //Update the Multiple Business User endpoint with the relevant Permission Status
        def updateResponse = updateMultiBusinessUserPermission(clientHeader, accountID, accountOwnerUserID,
                nominatedRepUserID, AUBusinessUserPermission.AUTHORIZE.getPermissionString(), nominatedRepUserID2,
                AUBusinessUserPermission.VIEW.getPermissionString())

        Assert.assertEquals(updateResponse.statusCode(), AUConstants.OK)
    }

    @Test
    void "CDS-591_Verify updating the NR Permission to View"() {

        def shareableElements = AUTestUtil.getSharableAccountsList(getSharableBankAccounts())

        String accountID =  shareableElements[AUConstants.PARAM_ACCOUNT_ID]
        String accountOwnerUserID = shareableElements[AUConstants.ACCOUNT_OWNER_USER_ID]
        String nominatedRepUserID = shareableElements[AUConstants.NOMINATED_REP_USER_ID]

        //Check the permissions of nominated representatives
        def permissionsResponse = getStakeholderPermissions(nominatedRepUserID, accountID)
        Assert.assertEquals(permissionsResponse.statusCode(), AUConstants.OK)
        Assert.assertTrue(hasBnrPermissionInResponse(permissionsResponse, nominatedRepUserID,
                AUBusinessUserPermission.AUTHORIZE.getPermissionString()))

        //Update the Permission of Nominated User to View
        def updateResponse = updateSingleBusinessUserPermission(clientHeader, accountID, accountOwnerUserID,
                nominatedRepUserID, AUBusinessUserPermission.VIEW.getPermissionString())
        Assert.assertEquals(updateResponse.statusCode(), AUConstants.OK)

        //Check the permissions of nominated representatives
        def permissionsResponse2 = getStakeholderPermissions(nominatedRepUserID, accountID)
        Assert.assertEquals(permissionsResponse2.statusCode(), AUConstants.OK)
        Assert.assertTrue(hasBnrPermissionInResponse(permissionsResponse2, nominatedRepUserID,
                AUBusinessUserPermission.VIEW.getPermissionString()))
    }

    @Test
    void "CDS-592_Verify updating the NR Permission to Revoke"() {

        def shareableElements = AUTestUtil.getSharableAccountsList(getSharableBankAccounts())

        String accountID =  shareableElements[AUConstants.PARAM_ACCOUNT_ID]
        String accountOwnerUserID = shareableElements[AUConstants.ACCOUNT_OWNER_USER_ID]
        String nominatedRepUserID = shareableElements[AUConstants.NOMINATED_REP_USER_ID]

        //Check the permissions of nominated representatives
        def permissionsResponse = getStakeholderPermissions(nominatedRepUserID, accountID)
        Assert.assertEquals(permissionsResponse.statusCode(), AUConstants.OK)
        Assert.assertFalse(AUTestUtil.parseResponseBody(permissionsResponse, AUConstants.PARAM_PERMISSION_STATUS)
                .contains("${nominatedRepUserID}:${AUBusinessUserPermission.REVOKE.getPermissionString()}"))

        //Update the Permission of Nominated User to Revoke - Should give an error as there is no permission called revoke
        def updateResponse = updateSingleBusinessUserPermission(clientHeader, accountID, accountOwnerUserID,
                nominatedRepUserID, AUBusinessUserPermission.REVOKE.getPermissionString())
        Assert.assertEquals(updateResponse.statusCode(), AUConstants.BAD_REQUEST)
        String errorDescription = getErrorText(updateResponse)
        Assert.assertNotNull(errorDescription)
        Assert.assertTrue(errorDescription.contains("REVOKE"))
    }

    @Test
    void "CDS-593_Verify nominated representative update request with incorrect payload"() {

        def shareableElements = AUTestUtil.getSharableAccountsList(getSharableBankAccounts())

        String accountOwnerUserID = shareableElements[AUConstants.ACCOUNT_OWNER_USER_ID]
        String nominatedRepUserID = shareableElements[AUConstants.NOMINATED_REP_USER_ID]

        //Update the Permission of Nominated User with incorrect payload
        def updateResponse = updateBusinessUserPermissionWithIncorrectPayload(clientHeader, null, accountOwnerUserID,
                nominatedRepUserID, AUBusinessUserPermission.VIEW.getPermissionString())
        Assert.assertEquals(updateResponse.statusCode(), AUConstants.BAD_REQUEST)
        String errorDescription = getErrorText(updateResponse)
        Assert.assertNotNull(errorDescription)
        Assert.assertTrue((errorDescription.contains("accountID: must not be null")))
    }

    @Test
    void "CDS-594_Verify nominated representative update request with incorrect content type"() {

        def shareableElements = AUTestUtil.getSharableAccountsList(getSharableBankAccounts())

        String accountID =  shareableElements[AUConstants.PARAM_ACCOUNT_ID]
        String accountOwnerUserID = shareableElements[AUConstants.ACCOUNT_OWNER_USER_ID]
        String nominatedRepUserID = shareableElements[AUConstants.NOMINATED_REP_USER_ID]

        //Update the Nominated representative
        def requestBody = AUPayloads.getSingleUserNominationPayload(accountID, accountOwnerUserID,
                nominatedRepUserID, AUBusinessUserPermission.AUTHORIZE.getPermissionString())

        def updateResponse = AURestAsRequestBuilder.buildRequest()
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, AUConstants.BASIC_HEADER_KEY + " " +
                        Base64.encoder.encodeToString(
                                "${auConfiguration.getUserBasicAuthName()}:${auConfiguration.getUserBasicAuthPWD()}"
                                        .getBytes(Charset.forName("UTF-8"))))
                .contentType(AUConstants.CONTENT_TYPE_APPLICATION_JWT)
                .body(requestBody)
                .baseUri(getAuConfiguration().getServerAuthorisationServerURL())
                .put("${AUConstants.CONSENT_STATUS_AU_ENDPOINT}${AUConstants.UPDATE_BUSINESS_USER}")

        Assert.assertEquals(updateResponse.statusCode(), AUConstants.STATUS_CODE_415)
    }

    @Test
    void "CDS-595_Verify nominated representative update request without content type"() {

        def shareableElements = AUTestUtil.getSharableAccountsList(getSharableBankAccounts())

        String accountID =  shareableElements[AUConstants.PARAM_ACCOUNT_ID]
        String accountOwnerUserID = shareableElements[AUConstants.ACCOUNT_OWNER_USER_ID]
        String nominatedRepUserID = shareableElements[AUConstants.NOMINATED_REP_USER_ID]

        //Update the Nominated representative
        def requestBody = AUPayloads.getSingleUserNominationPayload(accountID, accountOwnerUserID,
                nominatedRepUserID, AUBusinessUserPermission.AUTHORIZE.getPermissionString())

        def updateResponse = AURestAsRequestBuilder.buildRequest()
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, AUConstants.BASIC_HEADER_KEY + " " +
                        Base64.encoder.encodeToString(
                                "${auConfiguration.getUserBasicAuthName()}:${auConfiguration.getUserBasicAuthPWD()}"
                                        .getBytes(Charset.forName("UTF-8"))))
                .body(requestBody)
                .baseUri(getAuConfiguration().getServerAuthorisationServerURL())
                .put("${AUConstants.CONSENT_STATUS_AU_ENDPOINT}${AUConstants.UPDATE_BUSINESS_USER}")

        Assert.assertEquals(updateResponse.statusCode(), AUConstants.STATUS_CODE_415)
    }

    @Test
    void "CDS-596_Verify nominated representative update request without authorisation header"() {

        def shareableElements = AUTestUtil.getSharableAccountsList(getSharableBankAccounts())

        String accountID =  shareableElements[AUConstants.PARAM_ACCOUNT_ID]
        String accountOwnerUserID = shareableElements[AUConstants.ACCOUNT_OWNER_USER_ID]
        String nominatedRepUserID = shareableElements[AUConstants.NOMINATED_REP_USER_ID]

        //Update the Nominated representative
        def requestBody = AUPayloads.getSingleUserNominationPayload(accountID, accountOwnerUserID,
                nominatedRepUserID, AUBusinessUserPermission.AUTHORIZE.getPermissionString())

        def updateResponse = AURestAsRequestBuilder.buildRequest()
                .contentType(AUConstants.CONTENT_TYPE_APPLICATION_JSON)
                .body(requestBody)
                .baseUri(getAuConfiguration().getServerAuthorisationServerURL())
                .put("${AUConstants.CONSENT_STATUS_AU_ENDPOINT}${AUConstants.UPDATE_BUSINESS_USER}")

        Assert.assertEquals(updateResponse.statusCode(), AUConstants.UNAUTHORIZED)
    }

    @Test
    void "CDS-597_Verify nominated representative update request with incorrect request path"() {

        def shareableElements = AUTestUtil.getSharableAccountsList(getSharableBankAccounts())

        String accountID =  shareableElements[AUConstants.PARAM_ACCOUNT_ID]
        String accountOwnerUserID = shareableElements[AUConstants.ACCOUNT_OWNER_USER_ID]
        String nominatedRepUserID = shareableElements[AUConstants.NOMINATED_REP_USER_ID]

        //Update the Nominated representative
        def requestBody = AUPayloads.getSingleUserNominationPayload(accountID, accountOwnerUserID,
                nominatedRepUserID, AUBusinessUserPermission.AUTHORIZE.getPermissionString())

        def updateResponse = AURestAsRequestBuilder.buildRequest()
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, AUConstants.BASIC_HEADER_KEY + " " +
                        Base64.encoder.encodeToString(
                                "${auConfiguration.getUserBasicAuthName()}:${auConfiguration.getUserBasicAuthPWD()}"
                                        .getBytes(Charset.forName("UTF-8"))))
                .contentType(AUConstants.CONTENT_TYPE_APPLICATION_JSON)
                .body(requestBody)
                .baseUri(getAuConfiguration().getServerAuthorisationServerURL())
                .put("${AUConstants.CONSENT_STATUS_AU_ENDPOINT}" +
                        "${AUConstants.UPDATE_BUSINESS_USER}${AUConstants.BANK_ACCOUNT_SERVICE}")

        Assert.assertEquals(updateResponse.statusCode(), AUConstants.STATUS_CODE_404)
    }

    @Test
    void "CDS-384_Verify Nominated Rep delete request with an incorrect account id"() {

        def shareableElements = AUTestUtil.getSharableAccountsList(getSharableBankAccounts())

        String accountID =  shareableElements[AUConstants.PARAM_ACCOUNT_ID]
        String accountOwnerUserID = shareableElements[AUConstants.ACCOUNT_OWNER_USER_ID]
        String nominatedRepUserID = shareableElements[AUConstants.NOMINATED_REP_USER_ID]

        //Update the Business User endpoint with the relevant Permission Status
        def updateResponse = updateSingleBusinessUserPermission(clientHeader, accountID, accountOwnerUserID,
                nominatedRepUserID, AUBusinessUserPermission.AUTHORIZE.getPermissionString())
        Assert.assertEquals(updateResponse.statusCode(), AUConstants.OK)

        //Delete the Business User endpoint with the relevant Permission Status
        def deleteResponse = deleteSingleBusinessUser(clientHeader, AUConstants.INCORRECT_ACC_ID,
                accountOwnerUserID, nominatedRepUserID)
        Assert.assertEquals(deleteResponse.statusCode(), AUConstants.STATUS_CODE_404)
    }

    @Test
    void "CDS-598_Verify the Delete Business User with Valid inputs if success response is retrieved"() {

        def shareableElements = AUTestUtil.getSharableAccountsList(getSharableBankAccounts())

        String accountID =  shareableElements[AUConstants.PARAM_ACCOUNT_ID]
        String accountOwnerUserID = "user1@wso2.com@carbon.super"
        String nominatedRepUserID = shareableElements[AUConstants.NOMINATED_REP_USER_ID]

        //add record before updating
        def addResponse = addSingleBusinessUserPermission(accountID, accountOwnerUserID,
                nominatedRepUserID, AUBusinessUserPermission.AUTHORIZE.getPermissionString())
        Assert.assertEquals(addResponse.statusCode(), AUConstants.CREATED)

        //Update the Business User endpoint with the relevant Permission Status
        def updateResponse = updateSingleBusinessUserPermission(clientHeader, accountID, accountOwnerUserID,
                nominatedRepUserID, AUBusinessUserPermission.AUTHORIZE.getPermissionString())
        Assert.assertEquals(updateResponse.statusCode(), AUConstants.OK)

        //Delete the Business User endpoint with the relevant Permission Status
        def deleteResponse = deleteSingleBusinessUser(clientHeader, accountID, accountOwnerUserID,
                nominatedRepUserID)
        Assert.assertEquals(deleteResponse.statusCode(), AUConstants.OK)
    }

    @Test
    void "CDS-599_Verify the Delete BU end point with NR who has VIEW Permission"() {

        def shareableElements = AUTestUtil.getSharableAccountsList(getSharableBankAccounts())

        String accountID =  shareableElements[AUConstants.PARAM_ACCOUNT_ID]
        String accountOwnerUserID = "user1@wso2.com@carbon.super"
        String nominatedRepUserID = shareableElements[AUConstants.NOMINATED_REP_USER_ID]

        //add record before updating
        def addResponse = addSingleBusinessUserPermission(accountID, accountOwnerUserID,
                nominatedRepUserID, AUBusinessUserPermission.VIEW.getPermissionString())
        Assert.assertEquals(addResponse.statusCode(), AUConstants.CREATED)

        //Change Permission from Authorise to VIEW
        def updateResponse = updateSingleBusinessUserPermission(clientHeader, accountID, accountOwnerUserID,
                nominatedRepUserID, AUBusinessUserPermission.VIEW.getPermissionString())
        Assert.assertEquals(updateResponse.statusCode(), AUConstants.OK)

        //Delete the Business User endpoint with the relevant Permission Status
        def deleteResponse = deleteSingleBusinessUser(clientHeader, accountID, accountOwnerUserID,
                nominatedRepUserID)
        Assert.assertEquals(deleteResponse.statusCode(), AUConstants.OK)
    }

    @Test (priority = 1)
    void "CDS-600_Verify the Delete BU end point with NR who has REVOKE Permission"() {

        def shareableElements = AUTestUtil.getSharableAccountsList(getSharableBankAccounts())

        String accountID =  shareableElements[AUConstants.PARAM_ACCOUNT_ID]
        String accountOwnerUserID = "user1@wso2.com@carbon.super"
        String nominatedRepUserID = shareableElements[AUConstants.NOMINATED_REP_USER_ID]

        //add record before updating
        def addResponse = addSingleBusinessUserPermission(accountID, accountOwnerUserID,
                nominatedRepUserID, AUBusinessUserPermission.AUTHORIZE.getPermissionString())
        Assert.assertEquals(addResponse.statusCode(), AUConstants.CREATED)

        //Update the Business User endpoint with the relevant Permission Status
        def updateResponse = updateSingleBusinessUserPermission(clientHeader, accountID, accountOwnerUserID,
                nominatedRepUserID, AUBusinessUserPermission.AUTHORIZE.getPermissionString())
        Assert.assertEquals(updateResponse.statusCode(), AUConstants.OK)

        //Change Permission from Authorise to REVOKE
        updateResponse = updateSingleBusinessUserPermission(clientHeader, accountID, accountOwnerUserID,
                nominatedRepUserID, AUBusinessUserPermission.AUTHORIZE.getPermissionString())
        Assert.assertEquals(updateResponse.statusCode(), AUConstants.OK)

        //Delete the Business User endpoint with the relevant Permission Status
        def deleteResponse = deleteSingleBusinessUser(clientHeader, accountID, accountOwnerUserID,
                nominatedRepUserID)
        Assert.assertEquals(deleteResponse.statusCode(), AUConstants.OK)
    }

    @Test
    void "CDS-601_Verify the Delete BU end point with incorrect payload on request"() {

        def shareableElements = AUTestUtil.getSharableAccountsList(getSharableBankAccounts())

        String accountID =  shareableElements[AUConstants.PARAM_ACCOUNT_ID]
        String accountOwnerUserID = shareableElements[AUConstants.ACCOUNT_OWNER_USER_ID]
        String nominatedRepUserID = shareableElements[AUConstants.NOMINATED_REP_USER_ID]

        //Change Permission to Authorise
        def updateResponse = updateSingleBusinessUserPermission(clientHeader, accountID, accountOwnerUserID,
                nominatedRepUserID, AUBusinessUserPermission.AUTHORIZE.getPermissionString())
        Assert.assertEquals(updateResponse.statusCode(), AUConstants.OK)

        //Delete the Business User endpoint with the relevant Permission Status
        def deleteResponse = deleteBusinessUserWithIncorrectPayload(clientHeader, null,
                accountOwnerUserID, nominatedRepUserID)

        Assert.assertEquals(deleteResponse.statusCode(), AUConstants.BAD_REQUEST)
        String errorDescription = getErrorText(deleteResponse)
        Assert.assertNotNull(errorDescription)
        Assert.assertTrue((errorDescription.contains("accountID: must not be null")))
    }

    @Test
    void "CDS-602_Verify the Delete BU end point with incorrect content type"() {

        def shareableElements = AUTestUtil.getSharableAccountsList(getSharableBankAccounts())

        String accountID =  shareableElements[AUConstants.PARAM_ACCOUNT_ID]
        String accountOwnerUserID = shareableElements[AUConstants.ACCOUNT_OWNER_USER_ID]
        String nominatedRepUserID = shareableElements[AUConstants.NOMINATED_REP_USER_ID]

        //Change Permission to Authorise
        def updateResponse = updateSingleBusinessUserPermission(clientHeader, accountID, accountOwnerUserID,
                nominatedRepUserID, AUBusinessUserPermission.AUTHORIZE.getPermissionString())
        Assert.assertEquals(updateResponse.statusCode(), AUConstants.OK)

        //Delete the Business User endpoint with the incorrect content type
        def requestBody = AUPayloads.getSingleUserDeletePayload(accountID, accountOwnerUserID,
                nominatedRepUserID)

        def deleteResponse = AURestAsRequestBuilder.buildRequest()
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, AUConstants.BASIC_HEADER_KEY + " " +
                        Base64.encoder.encodeToString(
                                "${auConfiguration.getUserBasicAuthName()}:${auConfiguration.getUserBasicAuthPWD()}"
                                        .getBytes(Charset.forName("UTF-8"))))
                .contentType(AUConstants.CONTENT_TYPE_APPLICATION_JWT)
                .body(requestBody)
                .baseUri(getAuConfiguration().getServerAuthorisationServerURL())
                .delete("${AUConstants.CONSENT_STATUS_AU_ENDPOINT}${AUConstants.UPDATE_BUSINESS_USER}")

        Assert.assertEquals(deleteResponse.statusCode(), AUConstants.STATUS_CODE_415)
    }

    @Test
    void "CDS-605_Verify the Delete BU end point without content type"() {

        def shareableElements = AUTestUtil.getSharableAccountsList(getSharableBankAccounts())

        String accountID =  shareableElements[AUConstants.PARAM_ACCOUNT_ID]
        String accountOwnerUserID = shareableElements[AUConstants.ACCOUNT_OWNER_USER_ID]
        String nominatedRepUserID = shareableElements[AUConstants.NOMINATED_REP_USER_ID]

        //Change Permission to Authorise
        def updateResponse = updateSingleBusinessUserPermission(clientHeader, accountID, accountOwnerUserID,
                nominatedRepUserID, AUBusinessUserPermission.AUTHORIZE.getPermissionString())
        Assert.assertEquals(updateResponse.statusCode(), AUConstants.OK)

        //Delete the Business User endpoint with the incorrect content type
        def requestBody = AUPayloads.getSingleUserDeletePayload(accountID, accountOwnerUserID,
                nominatedRepUserID)

        def deleteResponse = AURestAsRequestBuilder.buildRequest()
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, AUConstants.BASIC_HEADER_KEY + " " +
                        Base64.encoder.encodeToString(
                                "${auConfiguration.getUserBasicAuthName()}:${auConfiguration.getUserBasicAuthPWD()}"
                                        .getBytes(Charset.forName("UTF-8"))))
                .body(requestBody)
                .baseUri(getAuConfiguration().getServerAuthorisationServerURL())
                .delete("${AUConstants.CONSENT_STATUS_AU_ENDPOINT}${AUConstants.UPDATE_BUSINESS_USER}")

        Assert.assertEquals(deleteResponse.statusCode(), AUConstants.STATUS_CODE_415)
    }

    @Test
    void "CDS-603_Verify the Delete BU end point without authorisation header"() {

        def shareableElements = AUTestUtil.getSharableAccountsList(getSharableBankAccounts())

        String accountID =  shareableElements[AUConstants.PARAM_ACCOUNT_ID]
        String accountOwnerUserID = shareableElements[AUConstants.ACCOUNT_OWNER_USER_ID]
        String nominatedRepUserID = shareableElements[AUConstants.NOMINATED_REP_USER_ID]

        //Change Permission to Authorise
        def updateResponse = updateSingleBusinessUserPermission(clientHeader, accountID, accountOwnerUserID,
                nominatedRepUserID, AUBusinessUserPermission.AUTHORIZE.getPermissionString())
        Assert.assertEquals(updateResponse.statusCode(), AUConstants.OK)

        //Delete the Business User endpoint with the incorrect content type
        def requestBody = AUPayloads.getSingleUserDeletePayload(
                accountID, accountOwnerUserID, nominatedRepUserID)

        def deleteResponse = AURestAsRequestBuilder.buildRequest()
                .contentType(AUConstants.CONTENT_TYPE_APPLICATION_JSON)
                .body(requestBody)
                .baseUri(getAuConfiguration().getServerAuthorisationServerURL())
                .delete("${AUConstants.CONSENT_STATUS_AU_ENDPOINT}${AUConstants.UPDATE_BUSINESS_USER}")

        Assert.assertEquals(deleteResponse.statusCode(), AUConstants.UNAUTHORIZED)
    }

    @Test
    void "CDS-604_Verify the Delete BU end point with incorrect request path"() {

        def shareableElements = AUTestUtil.getSharableAccountsList(getSharableBankAccounts())

        String accountID =  shareableElements[AUConstants.PARAM_ACCOUNT_ID]
        String accountOwnerUserID = shareableElements[AUConstants.ACCOUNT_OWNER_USER_ID]
        String nominatedRepUserID = shareableElements[AUConstants.NOMINATED_REP_USER_ID]

        //Change Permission to Authorise
        def updateResponse = updateSingleBusinessUserPermission(clientHeader, accountID, accountOwnerUserID,
                nominatedRepUserID, AUBusinessUserPermission.AUTHORIZE.getPermissionString())
        Assert.assertEquals(updateResponse.statusCode(), AUConstants.OK)

        //Delete the Business User endpoint with the incorrect content type
        def requestBody = AUPayloads.getSingleUserDeletePayload(
                accountID, accountOwnerUserID, nominatedRepUserID)

        def deleteResponse = AURestAsRequestBuilder.buildRequest()
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, AUConstants.BASIC_HEADER_KEY + " " +
                        Base64.encoder.encodeToString(
                                "${auConfiguration.getUserBasicAuthName()}:${auConfiguration.getUserBasicAuthPWD()}"
                                        .getBytes(Charset.forName("UTF-8"))))
                .contentType(AUConstants.CONTENT_TYPE_APPLICATION_JWT)
                .body(requestBody)
                .baseUri(getAuConfiguration().getServerAuthorisationServerURL())
                .delete("${AUConstants.CONSENT_STATUS_AU_ENDPOINT}${AUConstants.BUSINESS_ACCOUNT_INFO}")

        Assert.assertEquals(deleteResponse.statusCode(), AUConstants.UNAUTHORIZED)
    }
}

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

package org.wso2.cds.keymanager.test.doms

import com.nimbusds.oauth2.sdk.AccessTokenResponse
import org.wso2.cds.test.framework.AUTest
import org.wso2.cds.test.framework.automation.consent.AUBasicAuthAutomationStep
import org.wso2.cds.test.framework.constant.AUAccountScope
import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.constant.AUDOMSStatus
import org.wso2.cds.test.framework.constant.AUPageObjects
import org.wso2.cds.test.framework.request_builder.AURequestBuilder
import org.wso2.cds.test.framework.utility.AUIdEncryptorDecryptor
import org.wso2.cds.test.framework.utility.AUTestUtil
import org.wso2.openbanking.test.framework.automation.AutomationMethod
import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

import java.nio.charset.Charset

/**
 * Disclosure Option Management Service Tests.
 */
class DisclosureOptionManagementServiceTest extends AUTest {

    def clientHeader = "${Base64.encoder.encodeToString(getCDSClient().getBytes(Charset.defaultCharset()))}"
    private List<String> jointAccountIdList = new ArrayList<>()
    private List<String> singleAccountIdList = new ArrayList<>()
    private String secretKey = auConfiguration.getIDPermanence()
    Map<String, String> map

    @BeforeClass (alwaysRun = true)
    void "Initial Consent Authorisation"() {

        clientId = auConfiguration.getAppInfoClientID()
        //Get Joint Accounts and Single Account List
        jointAccountIdList = AUTestUtil.getJointAccountIds(getSharableBankAccounts())
        singleAccountIdList = AUTestUtil.getSingleAccountIds(getSharableBankAccounts())

        //Update the DOMS Status of both accounts to pre-approval
        map = new HashMap<>()
        map.put(jointAccountIdList[0], AUDOMSStatus.PRE_APPROVAL.getDomsStatusString())
        map.put(jointAccountIdList[2], AUDOMSStatus.PRE_APPROVAL.getDomsStatusString())

        Response updateResponse = updateDisclosureOptionsMgtService(clientHeader, map)
        Assert.assertEquals(updateResponse.statusCode(), AUConstants.OK)

        //Consent Authorisation
        automationResponse = doJointAccountConsentAuthorisation(auConfiguration.getAppInfoClientID(), true)
        authorisationCode = AUTestUtil.getCodeFromJwtResponse(automationResponse.currentUrl.get())

        //Get User Access Token
        generateUserAccessToken()
    }

    @Test(groups = "SmokeTest")
    void "CDS-403_Verify status with No sharing for one account and pre-approval for other account"() {

        //Update the DOMS Status to pre-approval and no-sharing
        map.put(jointAccountIdList[0], AUDOMSStatus.PRE_APPROVAL.getDomsStatusString())
        map.put(jointAccountIdList[2], AUDOMSStatus.NO_SHARING.getDomsStatusString())

        Response updateResponse = updateDisclosureOptionsMgtService(clientHeader, map)
        Assert.assertEquals(updateResponse.statusCode(), AUConstants.OK)

        //Account Retrieval - Return Account Details only for 1st account
        Response accountResponse = doAccountRetrieval(userAccessToken)
        Assert.assertEquals(accountResponse.statusCode(), AUConstants.STATUS_CODE_200)

        def account = AUIdEncryptorDecryptor.decrypt(
                AUTestUtil.parseResponseBody(response, "${AUConstants.RESPONSE_DATA_BULK_ACCOUNTID_LIST}[0]"), secretKey).
                split(":")[2]

        Assert.assertEquals(jointAccountIdList[0], account)
        Assert.assertNull(AUTestUtil.parseResponseBody(accountResponse, "${AUConstants.RESPONSE_DATA_BULK_ACCOUNTID_LIST}[1]"))
    }

    @Test(groups = "SmokeTest")
    void "CDS-404_Verify Account retrieval when DOMS status change to no-sharing"() {

        //Update the DOMS Status to no-sharing
        map.put(jointAccountIdList[0], AUDOMSStatus.NO_SHARING.getDomsStatusString())
        Response updateResponse = updateDisclosureOptionsMgtService(clientHeader, map)
        Assert.assertEquals(updateResponse.statusCode(), AUConstants.OK)

        //Account Retrieval - Not Return Account Details
        Response accountResponse = doAccountRetrieval(userAccessToken)
        Assert.assertEquals(accountResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertTrue(AUTestUtil.parseResponseBody(accountResponse,
                "${AUConstants.RESPONSE_DATA_BULK_ACCOUNTID_LIST}").equals("[]"))
    }

    @Test(groups = "SmokeTest")
    void "CDS-625_Verify Account retrieval when DOMS status change to pre-approval"() {

        //Update the DOMS Status to pre-approval
        map.put(jointAccountIdList[0], AUDOMSStatus.PRE_APPROVAL.getDomsStatusString())

        Response updateResponse = updateDisclosureOptionsMgtService(clientHeader, map)
        Assert.assertEquals(updateResponse.statusCode(), AUConstants.OK)

        //Account Retrieval - Return Account Details
        Response accountResponse = doAccountRetrieval(userAccessToken)
        Assert.assertEquals(accountResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "${AUConstants.RESPONSE_DATA_BULK_ACCOUNTID_LIST}"))
    }

    @Test
    void "CDS-406_Verify single account retrieval when DOMS status change to pre-approval"() {

        //Update the DOMS Status to pre-approval
        map.put(jointAccountIdList[0], AUDOMSStatus.PRE_APPROVAL.getDomsStatusString())

        Response updateResponse = updateDisclosureOptionsMgtService(clientHeader, map)
        Assert.assertEquals(updateResponse.statusCode(), AUConstants.OK)

        //Account Retrieval - Return Account Details
        Response accountResponse = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_ACCOUNT, clientHeader)
                .baseUri(auConfiguration.getServerBaseURL())
                .get("${AUConstants.CDS_PATH}/banking/accounts/${AUConstants.jointAccountID}")

        Assert.assertEquals(accountResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertNotNull(AUTestUtil.parseResponseBody(accountResponse, "${AUConstants.RESPONSE_DATA_SINGLE_ACCOUNTID}"))
    }

    //TODO: Issue: https://github.com/wso2-enterprise/financial-open-banking/issues/8452
    @Test
    void "CDS-407_Verify single account retrieval when DOMS status change to no-sharing"() {

        //Update the DOMS Status to no-sharing
        map.put(jointAccountIdList[0], AUDOMSStatus.NO_SHARING.getDomsStatusString())
        map.put(jointAccountIdList[2], AUDOMSStatus.NO_SHARING.getDomsStatusString())

        Response updateResponse = updateDisclosureOptionsMgtService(clientHeader, map)
        Assert.assertEquals(updateResponse.statusCode(), AUConstants.OK)

        //Account Retrieval - Not Return Account Details
        Response accountResponse = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_ACCOUNT, clientHeader)
                .baseUri(auConfiguration.getServerBaseURL())
                .get("${AUConstants.BULK_ACCOUNT_PATH}/${AUConstants.jointAccountID}")

        Assert.assertEquals(accountResponse.statusCode(), AUConstants.STATUS_CODE_404)
        Assert.assertEquals(AUTestUtil.parseResponseBody(accountResponse, AUConstants.ERROR_CODE),
                AUConstants.ERROR_CODE_INVALID_BANK_ACC)
        Assert.assertEquals(AUTestUtil.parseResponseBody(accountResponse, AUConstants.ERROR_TITLE),
                AUConstants.INVALID_BANK_ACC)
        Assert.assertEquals(AUTestUtil.parseResponseBody(accountResponse, AUConstants.ERROR_DETAIL),
                AUConstants.jointAccountID)
    }

    @Test
    void "CDS-408_Verify the DOMS put-call changing the status of a particular account from Pre approval to No Sharing"() {

        //Update the DOMS Status to pre-approval
        map.put(jointAccountIdList[0], AUDOMSStatus.PRE_APPROVAL.getDomsStatusString())

        Response updateResponse = updateDisclosureOptionsMgtService(clientHeader, map)
        Assert.assertEquals(updateResponse.statusCode(), AUConstants.OK)

        //Bulk Account Retrieval - Return Account Details
        Response accountResponse = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_ACCOUNTS, clientHeader)
                .baseUri(auConfiguration.getServerBaseURL())
                .get("${AUConstants.BULK_ACCOUNT_PATH}")

        Assert.assertEquals(accountResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertNotNull(AUTestUtil.parseResponseBody(accountResponse,
                "${AUConstants.RESPONSE_DATA_BULK_ACCOUNTID_LIST}[0]"))

        //Update the DOMS Status to no-sharing
        map.put(jointAccountIdList[0], AUDOMSStatus.NO_SHARING.getDomsStatusString())

        updateResponse = updateDisclosureOptionsMgtService(clientHeader, map)
        Assert.assertEquals(updateResponse.statusCode(), AUConstants.OK)

        //Single Account Retrieval - Not Return Account Details
        accountResponse = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_ACCOUNTS, clientHeader)
                .baseUri(auConfiguration.getServerBaseURL())
                .get("${AUConstants.BULK_ACCOUNT_PATH}")

        Assert.assertEquals(accountResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertNull(AUTestUtil.parseResponseBody(accountResponse,
                "${AUConstants.RESPONSE_DATA_BULK_ACCOUNTID_LIST}[0]"))
    }

    @Test
    void "CDS-410_Verify the DOMS put-call changing the status of a particular account from No Sharing to Pre approval"() {

        //Update the DOMS Status to no-sharing
        map.put(jointAccountIdList[0], AUDOMSStatus.NO_SHARING.getDomsStatusString())

        Response updateResponse = updateDisclosureOptionsMgtService(clientHeader, map)
        Assert.assertEquals(updateResponse.statusCode(), AUConstants.OK)

        //Account Retrieval - Not Return Account Details
        Response accountResponse = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_ACCOUNTS, clientHeader)
                .baseUri(auConfiguration.getServerBaseURL())
                .get("${AUConstants.BULK_ACCOUNT_PATH}")

        Assert.assertEquals(accountResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertNull(AUTestUtil.parseResponseBody(accountResponse,
                "${AUConstants.RESPONSE_DATA_BULK_ACCOUNTID_LIST}[0]"))

        //Update the DOMS Status to pre-approval
        map.put(jointAccountIdList[0], AUDOMSStatus.PRE_APPROVAL.getDomsStatusString())

        updateResponse = updateDisclosureOptionsMgtService(clientHeader, map)
        Assert.assertEquals(updateResponse.statusCode(), AUConstants.OK)

        //Account Retrieval - Return Account Details
        accountResponse = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_ACCOUNTS, clientHeader)
                .baseUri(auConfiguration.getServerBaseURL())
                .get("${AUConstants.BULK_ACCOUNT_PATH}")

        Assert.assertEquals(accountResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertNotNull(AUTestUtil.parseResponseBody(accountResponse,
                "${AUConstants.RESPONSE_DATA_BULK_ACCOUNTID_LIST}[0]"))
    }

    @Test
    void "CDS-619_Verify creating new consent when DOMS is in pre-approval status"() {

        //Update the DOMS Status to pre-approval
        map.put(jointAccountIdList[0], AUDOMSStatus.PRE_APPROVAL.getDomsStatusString())

        Response updateResponse = updateDisclosureOptionsMgtService(clientHeader, map)
        Assert.assertEquals(updateResponse.statusCode(), AUConstants.OK)

        //Consent Authorisation
        automationResponse = doJointAccountConsentAuthorisation(clientId, false)
        authorisationCode = AUTestUtil.getCodeFromJwtResponse(automationResponse.currentUrl.get())

        //Get User Access Token
        generateUserAccessToken()

        //Account Retrieval - Return Account Details
        Response accountResponse = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_ACCOUNTS, clientHeader)
                .baseUri(auConfiguration.getServerBaseURL())
                .get("${AUConstants.BULK_ACCOUNT_PATH}")

        Assert.assertEquals(accountResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "${AUConstants.RESPONSE_DATA_BULK_ACCOUNTID_LIST}"))
    }

    @Test
    void "CDS-620_Verify creating new consent when DOMS status to no-sharing status"() {

        //Update the DOMS Status to no-sharing
        map.put(jointAccountIdList[0], AUDOMSStatus.NO_SHARING.getDomsStatusString())

        Response updateResponse = updateDisclosureOptionsMgtService(clientHeader, map)
        Assert.assertEquals(updateResponse.statusCode(), AUConstants.OK)

        //Consent Authorisation
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), auConfiguration.getAppInfoClientID())
                .toURI().toString()

        automationResponse = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Select Profile and Accounts
                    //If Profile Selection Enabled
                    if (auConfiguration.getProfileSelectionEnabled()) {

                        //Select Individual Profile
                        authWebDriver.selectOption(AUPageObjects.INDIVIDUAL_PROFILE_SELECTION)
                        authWebDriver.clickButtonXpath(AUPageObjects.PROFILE_SELECTION_NEXT_BUTTON)

                        //Joint Account 1 is not in the selectable list
                        Assert.assertFalse(authWebDriver.isElementPresent(AUPageObjects.JOINT_ACCOUNT_XPATH))
                    }
                    //If Profile Selection Disabled
                    else {
                        //Joint Account 1 is not in the selectable list
                        Assert.assertFalse(authWebDriver.isElementPresent(AUPageObjects.JOINT_ACCOUNT_XPATH))
                    }
                }
                .execute()
    }

    @Test(groups = "SmokeTest", priority = 1)
    void "CDS-405_Verify that when there is no change in DOMS and the account retrieval shows values normally"() {

        //Consent Authorisation
        automationResponse = doJointAccountConsentAuthorisation(clientId, false)
        authorisationCode = AUTestUtil.getCodeFromJwtResponse(automationResponse.currentUrl.get())

        //Get User Access Token
        generateUserAccessToken()

        //Account Retrieval without updating the DOMS status - Return Account Details
        Response accountResponse = doAccountRetrieval(userAccessToken)
        Assert.assertEquals(accountResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertNotNull(AUTestUtil.parseResponseBody(accountResponse, "${AUConstants.RESPONSE_DATA_BULK_ACCOUNTID_LIST}[0]"))
    }

    @Test(priority = 1, dependsOnMethods = "CDS-405_Verify that when there is no change in DOMS and the account retrieval shows values normally")
    void "CDS-624_Consent search API for DOMS status to no-sharing status"() {

        //Update the DOMS Status to no-sharing
        map.put(jointAccountIdList[0], AUDOMSStatus.NO_SHARING.getDomsStatusString())

        Response updateResponse = updateDisclosureOptionsMgtService(clientHeader, map)
        Assert.assertEquals(updateResponse.statusCode(), AUConstants.OK)

        def response = doConsentSearch()
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
    }

    @Test(priority = 1, dependsOnMethods = "CDS-624_Consent search API for DOMS status to no-sharing status")
    void "CDS-623_Consent search API for DOMS status to pre-approval status"() {

        //Update the DOMS Status to pre-approval
        map.put(jointAccountIdList[0], AUDOMSStatus.PRE_APPROVAL.getDomsStatusString())

        Response updateResponse = updateDisclosureOptionsMgtService(clientHeader, map)
        Assert.assertEquals(updateResponse.statusCode(), AUConstants.OK)

        def response = doConsentSearch()
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
    }

    @Test
    void "CDS-650_Verify Consent amendment flow after changing DOMS status to no-sharing"() {

        //Consent Authorisation
        automationResponse = doJointAccountConsentAuthorisation(clientId, false)
        authorisationCode = AUTestUtil.getCodeFromJwtResponse(automationResponse.currentUrl.get())

        //Get Access Token
        AccessTokenResponse responseBody = getUserAccessTokenResponse(clientId)
        userAccessToken = responseBody.tokens.accessToken
        cdrArrangementId = responseBody.getCustomParameters().get(AUConstants.CDR_ARRANGEMENT_ID)
        Assert.assertNotNull(cdrArrangementId)

        //Update the DOMS Status to no-sharing
        map.put(jointAccountIdList[0], AUDOMSStatus.NO_SHARING.getDomsStatusString())

        Response updateResponse = updateDisclosureOptionsMgtService(clientHeader, map)
        Assert.assertEquals(updateResponse.statusCode(), AUConstants.OK)

        //Consent Amendment
        scopes.remove(AUAccountScope.BANK_TRANSACTION_READ)

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), clientId)
                .toURI().toString()

        //Consent Authorisation UI Flow
        automationResponse = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    if (auConfiguration.getProfileSelectionEnabled()) {

                        //Select Individual Profile
                        authWebDriver.selectOption(AUPageObjects.INDIVIDUAL_PROFILE_SELECTION)
                        authWebDriver.clickButtonXpath(AUPageObjects.PROFILE_SELECTION_NEXT_BUTTON)

                        //Joint Account 1 is listed under Unavailable Accounts
                        Assert.assertFalse(authWebDriver.isElementPresent(AUPageObjects.JOINT_ACCOUNT_XPATH))
                    }
                    //If Profile Selection Disabled
                    else {
                        //Joint Account 1 is listed under Unavailable Accounts
                        Assert.assertFalse(authWebDriver.isElementPresent(AUPageObjects.JOINT_ACCOUNT_XPATH))
                    }
                }
                .execute()
    }
}

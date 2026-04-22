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
import org.wso2.cds.test.framework.configuration.AUConfigurationService
import org.wso2.cds.test.framework.constant.AUAccountProfile
import org.wso2.cds.test.framework.constant.AUBusinessUserPermission
import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.data_provider.ConsentDataProviders
import org.wso2.cds.test.framework.request_builder.AURequestBuilder
import org.wso2.cds.test.framework.utility.AUTestUtil
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

import java.nio.charset.Charset

/**
 * Business Nominated Representative End To End Flow Test.
 */
class BnrEndToEndTest extends AUTest{

    def clientHeader
    AUConfigurationService auConfiguration = new AUConfigurationService()
        String accountID
        String accountOwnerUserID
        String activeNominatedRepUserID

    @BeforeClass(alwaysRun = true)
    void "Nominate Business User Representative"() {

        auConfiguration.setPsuNumber(0)
        clientHeader = "${Base64.encoder.encodeToString(getCDSClient().getBytes(Charset.defaultCharset()))}"

        // Resolve the Organization A business account deterministically from fixed sharable payload.
        def sharableAccountList = getSharableBankAccounts().jsonPath().get(AUConstants.DATA)
        def businessAccount = sharableAccountList.find { sharableAccount ->
            sharableAccount[AUConstants.PARAM_PROFILE_NAME] == AUAccountProfile.ORGANIZATION_A.getProperty(AUConstants.VALUE_KEY)
        }

        Assert.assertNotNull(businessAccount, "Organization A business account was not found in sharable payload")
        accountID = businessAccount[AUConstants.ACCOUNT_ID]
        accountOwnerUserID = businessAccount[AUConstants.BUSINESS_ACCOUNT_INFO][AUConstants.ACCOUNT_OWNERS][AUConstants.MEMBER_ID][0]
        activeNominatedRepUserID = "${auConfiguration.getUserPSUName()}"

        // In reference implementation sharable payload is fixed; drive BNR permission setup using active PSU.
        Assert.assertNotNull(activeNominatedRepUserID, "Active PSU user id should be resolved for BNR setup")

        def updateResponse = updateSingleBusinessUserPermission(clientHeader, accountID, accountOwnerUserID,
                activeNominatedRepUserID, AUBusinessUserPermission.AUTHORIZE.getPermissionString())
        Assert.assertEquals(updateResponse.statusCode(), AUConstants.OK)
    }

    @Test (groups = "SmokeTest", dataProvider = "BankingApisBusinessProfile", dataProviderClass = ConsentDataProviders.class)
    void "CDS-486_Verify an accounts retrieval call after business profile selection and business accounts consented"(resourcePath) {

        //Consent Authorisation
        doBnrConsentAuthorisationViaRequestUri()
        generateUserAccessToken()

        int x_v_header = AUTestUtil.getBankingApiEndpointVersion(resourcePath.toString())

        //Get Accounts
        def response = AURequestBuilder.buildBasicRequestWithOptionalHeaders(userAccessToken,
                x_v_header, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get("${resourcePath}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), x_v_header)
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
    void "CDS-487_Verify account is blocked when business user only has VIEW permission"() {

        doBnrConsentAuthorisationViaRequestUri()
        generateUserAccessToken()

        // Set VIEW-only permission — account validation policy blocks non-AUTHORIZE accounts
        def updateResponse = updateSingleBusinessUserPermission(clientHeader, accountID, accountOwnerUserID,
                activeNominatedRepUserID, AUBusinessUserPermission.VIEW.getPermissionString())
        Assert.assertEquals(updateResponse.statusCode(), AUConstants.OK)

        def accountResponse = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
                AUConstants.X_V_HEADER_ACCOUNTS, clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get("${AUConstants.BULK_ACCOUNT_PATH}")

        Assert.assertEquals(accountResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertTrue(AUTestUtil.parseResponseBody(accountResponse,
                "${AUConstants.RESPONSE_DATA_BULK_ACCOUNTID_LIST}").equals("[]"))

        // Restore AUTHORIZE permission for subsequent tests
        updateSingleBusinessUserPermission(clientHeader, accountID, accountOwnerUserID,
                activeNominatedRepUserID, AUBusinessUserPermission.AUTHORIZE.getPermissionString())
    }

    private void doBnrConsentAuthorisationViaRequestUri() {

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                        true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        doConsentAuthorisationViaRequestUri(scopes, requestUri.toURI(), null, AUAccountProfile.ORGANIZATION_A)
    }
}

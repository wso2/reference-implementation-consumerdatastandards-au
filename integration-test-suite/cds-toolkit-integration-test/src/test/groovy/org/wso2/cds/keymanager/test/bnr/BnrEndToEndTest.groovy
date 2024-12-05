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

    @BeforeClass(alwaysRun = true)
    void "Nominate Business User Representative"() {
        auConfiguration.setPsuNumber(2)
        clientHeader = "${Base64.encoder.encodeToString(getCDSClient().getBytes(Charset.defaultCharset()))}"

        //Get Sharable Account List and Nominate Business Representative with Authorize Permission
        def shareableElements = AUTestUtil.getSharableAccountsList(getSharableBankAccounts())

        String accountID =  shareableElements[AUConstants.PARAM_ACCOUNT_ID]
        String accountOwnerUserID = shareableElements[AUConstants.ACCOUNT_OWNER_USER_ID]
        String nominatedRepUserID = shareableElements[AUConstants.NOMINATED_REP_USER_ID]

        def updateResponse = updateSingleBusinessUserPermission(clientHeader, accountID, accountOwnerUserID,
                nominatedRepUserID, AUBusinessUserPermission.AUTHORIZE.getPermissionString())
        Assert.assertEquals(updateResponse.statusCode(), AUConstants.OK)
    }

    @Test (groups = "SmokeTest", dataProvider = "BankingApisBusinessProfile", dataProviderClass = ConsentDataProviders.class)
    void "CDS-486_Verify an accounts retrieval call after business profile selection and business accounts consented"(resourcePath) {

        //Consent Authorisation
        doConsentAuthorisation(null, AUAccountProfile.ORGANIZATION_B)
        generateUserAccessToken()

        int x_v_header = AUTestUtil.getBankingApiEndpointVersion(resourcePath.toString())

        //Get Accounts
        def response = AURequestBuilder.buildBasicRequestWithCustomHeaders(userAccessToken,
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
}

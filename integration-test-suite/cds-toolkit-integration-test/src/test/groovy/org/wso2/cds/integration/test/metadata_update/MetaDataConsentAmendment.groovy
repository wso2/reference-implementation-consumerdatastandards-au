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

package org.wso2.cds.integration.test.metadata_update

import com.nimbusds.oauth2.sdk.AccessTokenResponse
import org.wso2.bfsi.test.framework.automation.AutomationMethod
import org.wso2.cds.test.framework.AUTest
import org.wso2.cds.test.framework.automation.consent.AUBasicAuthAutomationStep
import org.wso2.cds.test.framework.constant.AUAccountScope
import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.constant.AUPageObjects
import org.wso2.cds.test.framework.utility.AUTestUtil
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

/**
 * Test Related to Meta Data Update - Consent Amendment
 */
class MetaDataConsentAmendment extends AUTest{

    @BeforeClass(alwaysRun = true)
    void setup() {
        auConfiguration.setTppNumber(1)

        //Register Second TPP.
        deleteApplicationIfExists(auConfiguration.getAppInfoClientID())
        def registrationResponse = tppRegistration()
        clientId = AUTestUtil.parseResponseBody(registrationResponse, "client_id")
        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.CREATED)

        //Write Client Id of TPP2 to config file.
        AUTestUtil.writeXMLContent(AUTestUtil.getTestConfigurationFilePath(), "Application",
                "ClientID", clientId, auConfiguration.getTppNumber())

        doConsentAuthorisation(clientId)
    }

    @Test (enabled = true)
    void "TC022_Verify the Consent Amendment when the SP Active and ADR Active"() {

        // Retrieve the user access token by auth code
        AccessTokenResponse accessTokenResponse = getUserAccessTokenResponse(clientId)
        cdrArrangementId = accessTokenResponse.getCustomParameters().get(AUConstants.CDR_ARRANGEMENT_ID)
        userAccessToken = accessTokenResponse.tokens.accessToken

        Assert.assertNotNull(userAccessToken)
        Assert.assertNotNull(cdrArrangementId)

        //Remove Payee Scope and do authorisation
        scopes.remove(AUAccountScope.BANK_PAYEES_READ)
        doConsentAuthorisation(clientId)

        //Retrieve and assert the request URI from Push Authorization request
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, cdrArrangementId)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI()).toURI().toString()

        //Consent Amendment Authorisation Flow
        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Verify Account Selection Page
                    assert authWebDriver.isElementDisplayed(AUTestUtil.getAltSingleAccountXPath())
                    authWebDriver.clickButtonXpath(AUTestUtil.getAltSingleAccountXPath())

                    //Click Confirm Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)

                    //Click Authorise Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)
                }
                .execute()

        // Get Code From URL
        authorisationCode = AUTestUtil.getCodeFromJwtResponse(automation.currentUrl.get())
        Assert.assertNotNull(authorisationCode)

        //Retrieve the second user access token and assert the CDR arrangement ID is the same.
        // Retrieve the user access token by auth code
        def responseBody = getUserAccessTokenResponse(clientId)
        userAccessToken = responseBody.tokens.accessToken
        verifyScopes(responseBody.toJSONObject().get("scope").toString())
        Assert.assertEquals(cdrArrangementId, responseBody.getCustomParameters().get("cdr_arrangement_id"),
                "Amended CDR id is not original CDR id ")
    }
}

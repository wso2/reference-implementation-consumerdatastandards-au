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

import org.wso2.cds.test.framework.AUTest
import org.wso2.cds.test.framework.configuration.AUConfigurationService
import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.utility.AUTestUtil
import org.wso2.openbanking.test.framework.automation.NavigationAutomationStep
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

/**
 * Test Related to Meta Data Update - Consent Authorisation
 */
class MetaDataConsentAuthorization extends AUTest{

    AUConfigurationService auConfiguration = new AUConfigurationService()

    @BeforeClass(alwaysRun = true)
    void setup() {
        auConfiguration.setTppNumber(1)

        //Register Second TPP.
        deleteApplicationIfExists(auConfiguration.getAppInfoClientID())
        def registrationResponse = tppRegistration()
        clientId = AUTestUtil.parseResponseBody(registrationResponse, "client_id")
        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.CREATED)

        //Write Client Id of TPP2 to config file.
        AUTestUtil.writeXMLContent(auConfiguration.getOBXMLFile().toString(), "Application",
                "ClientID", clientId, auConfiguration.getTppNumber())

        doConsentAuthorisation(clientId)
    }

    @Test(enabled = true)
    void "TC001_Verify the Consent Authorisation when the SP and ADR both active"() {

        doConsentAuthorisation()
        generateUserAccessToken()
    }

    @Test(enabled = true)
    void "TC002_Verify the Consent Authorisation when the SP Removed and ADR active"() {

        //TODO: Update status
        sleep(81000)

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "", clientId)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), clientId)
                .toURI().toString()

        def automationResponse = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new NavigationAutomationStep(authoriseUrl, 10))
                .execute()

        String url = automationResponse.currentUrl.get()
        String errorUrl

        errorUrl = url.split("oauthErrorMsg=")[1].split("&")[0].replaceAll("\\+"," ")
        Assert.assertEquals(errorUrl, "The software product of ADR is not in an active state in the CDR Register. Current status is REMOVED")

    }

    //Enable the test case when running the test case. Disabled due to the mock authenticator is not available now.
    @Test(enabled = false)
    void "TC003_Verify the Consent Authorisation when the SP Inactive and ADR active"() {

        //TODO: Update status
        sleep(81000)

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "", clientId)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), clientId)
                .toURI().toString()

        def automationResponse = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new NavigationAutomationStep(authoriseUrl, 10))
                .execute()

        String url = automationResponse.currentUrl.get()
        String errorUrl

        errorUrl = url.split("oauthErrorMsg=")[1].split("&")[0].replaceAll("\\+"," ")
        Assert.assertEquals(errorUrl, "The software product of ADR is not in an active state in the CDR Register. Current status is INACTIVE")
    }

    //Enable the test case when running the test case. Disabled due to the mock authenticator is not available now.
    @Test(enabled = false)
    void "TC004_Verify the Consent Authorisation when the SP Inactive and ADR Suspended"() {

        //TODO: Update status
        sleep(81000)

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "", clientId)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), clientId)
                .toURI().toString()

        def automationResponse = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new NavigationAutomationStep(authoriseUrl, 10))
                .execute()

        String url = automationResponse.currentUrl.get()
        String errorUrl

        errorUrl = url.split("oauthErrorMsg=")[1].split("&")[0].replaceAll("\\+"," ")
        Assert.assertEquals(errorUrl, "The software product of ADR is not in an active state in the CDR Register. Current status is SUSPENDED")
    }

    //Enable the test case when running the test case. Disabled due to the mock authenticator is not available now.
    @Test(enabled = false)
    void "TC005_Verify the Consent Authorisation when the SP Removed and ADR Suspended"() {

        //TODO: Update status
        sleep(81000)

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "", clientId)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), clientId)
                .toURI().toString()

        def automationResponse = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new NavigationAutomationStep(authoriseUrl, 10))
                .execute()

        String url = automationResponse.currentUrl.get()
        String errorUrl

        errorUrl = url.split("oauthErrorMsg=")[1].split("&")[0].replaceAll("\\+"," ")
        Assert.assertEquals(errorUrl, "The software product of ADR is not in an active state in the CDR Register. Current status is SUSPENDED")
    }

    //Enable the test case when running the test case. Disabled due to the mock authenticator is not available now.
    @Test(enabled = false)
    void "TC006_Verify the Consent Authorisation when the SP Removed and ADR Surrendered"() {

        //TODO: Update status
        sleep(81000)

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "", clientId)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), clientId)
                .toURI().toString()

        def automationResponse = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new NavigationAutomationStep(authoriseUrl, 10))
                .execute()

        String url = automationResponse.currentUrl.get()
        String errorUrl

        errorUrl = url.split("oauthErrorMsg=")[1].split("&")[0].replaceAll("\\+"," ")
        Assert.assertEquals(errorUrl, "The ADR is not in an active state in the CDR Register. Current status is SURRENDERED")
    }

    //Enable the test case when running the test case. Disabled due to the mock authenticator is not available now.
    @Test(enabled = false)
    void "TC007_Verify the Consent Authorisation when the SP Removed and ADR Revoked"() {

        //TODO: Update status
        sleep(81000)

        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "", clientId)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), clientId)
                .toURI().toString()

        def automationResponse = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new NavigationAutomationStep(authoriseUrl, 10))
                .execute()

        String url = automationResponse.currentUrl.get()
        String errorUrl

        errorUrl = url.split("oauthErrorMsg=")[1].split("&")[0].replaceAll("\\+"," ")
        Assert.assertEquals(errorUrl, "The ADR is not in an active state in the CDR Register. Current status is REVOKED")
    }
}

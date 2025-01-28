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

package org.wso2.cds.integration.test.admin_api.authorisation

import com.nimbusds.oauth2.sdk.AccessTokenResponse
import org.wso2.cds.test.framework.AUTest
import org.wso2.cds.test.framework.constant.AUAccountProfile
import org.wso2.cds.test.framework.constant.AUAccountScope
import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.request_builder.AURequestBuilder
import org.wso2.cds.test.framework.utility.AUTestUtil
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

import java.nio.charset.Charset

/**
 * Test cases to retrieve Authorisation Metrics for amendedAuthorization.
 */
class AmendedAuthorisationMetrics extends AUTest {

    private String cdrArrangementId = ""
    private AccessTokenResponse userAccessToken, secondUserAccessToken
    private String requestUri
    public String secondAuthorisationCode, thirdAuthorisationCode = null
    def clientHeader = "${Base64.encoder.encodeToString(getCDSClient().getBytes(Charset.defaultCharset()))}"

    @BeforeClass (alwaysRun = true)
    void "Initial Metrics Request"() {

        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Assign Metrics to Variables
        getInitialMetricsResponse(metricsResponse)
    }

    @Test (groups = "SmokeTest")
    void "Consent Amendment for Ongoing Single account consent"(){

        auConfiguration.setPsuNumber(0)
        //Send Authorisation Request for 1st time
        doConsentAuthorisation()

        //Generate User Access Token
        AccessTokenResponse userAccessTokenRes = getUserAccessTokenResponse()
        String cdrArrangementId = userAccessTokenRes.getCustomParameters().get("cdr_arrangement_id")
        accessToken = userAccessTokenRes.tokens.accessToken
        Assert.assertNotNull(cdrArrangementId)

        //Consent Amendment
        //remove an existing scope and add a new scope to amend the consent
        scopes.remove(AUAccountScope.BANK_TRANSACTION_READ)
        scopes.add(AUAccountScope.BANK_PAYEES_READ)

        //Retrieve the second authorization code
        authorisationCode = doConsentAmendmentAuthorisation(scopes, cdrArrangementId,
                AUConstants.DEFAULT_SHARING_DURATION, auConfiguration.getAppInfoClientID())
        Assert.assertNotNull(authorisationCode)

        //Generate User Access Token
        userAccessTokenRes = getUserAccessTokenResponse()
        cdrArrangementId = userAccessTokenRes.getCustomParameters().get("cdr_arrangement_id")
        accessToken = userAccessTokenRes.tokens.accessToken
        Assert.assertNotNull(cdrArrangementId)

        sleep(AUConstants.ABANDON_WAIT_TIME)

        //Verify Metrics Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Active Authorisation, New Ongoing Individual and Amendment count increased by 1

        activeAuthIndividual = activeAuthIndividual + 1
        newAuthCurrentDayOngoingIndividual = newAuthCurrentDayOngoingIndividual + 1
        amendedCurrentDayIndividual = amendedCurrentDayIndividual + 1

        //Asserting the response
        assertMetricsAuthorisationResponse(metricsResponse)
    }

    @Test (groups = "SmokeTest", priority = 1)
    void "Consent Amendment for Ongoing Business account consent"(){

        auConfiguration.setPsuNumber(2)
        //Send Authorisation Request for 1st time
        doConsentAuthorisation(auConfiguration.getAppInfoClientID(), AUAccountProfile.ORGANIZATION_B)

        //Generate User Access Token
        AccessTokenResponse userAccessTokenRes = getUserAccessTokenResponse()
        String cdrArrangementId = userAccessTokenRes.getCustomParameters().get("cdr_arrangement_id")
        accessToken = userAccessTokenRes.tokens.accessToken
        Assert.assertNotNull(cdrArrangementId)

        //Consent Amendment
        //remove an existing scope and add a new scope to amend the consent
        scopes.remove(AUAccountScope.BANK_TRANSACTION_READ)
        scopes.add(AUAccountScope.BANK_PAYEES_READ)

        auConfiguration.setPsuNumber(2)
        //Retrieve the second authorization code
        authorisationCode = doBusinessConsentAmendmentAuthorisation(scopes, cdrArrangementId,
                AUConstants.ONE_YEAR_DURATION, auConfiguration.getAppInfoClientID())
        Assert.assertNotNull(authorisationCode)

        //Generate User Access Token
        userAccessTokenRes = getUserAccessTokenResponse()
        cdrArrangementId = userAccessTokenRes.getCustomParameters().get("cdr_arrangement_id")
        accessToken = userAccessTokenRes.tokens.accessToken
        Assert.assertNotNull(cdrArrangementId)

        sleep(AUConstants.ABANDON_WAIT_TIME)

        //Verify Metrics Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Active Authorisation, New Ongoing Individual and Amendment count increased by 1

        activeAuthNonIndividual = activeAuthNonIndividual + 1
        newAuthCurrentDayOngoingNonIndividual = newAuthCurrentDayOngoingNonIndividual + 1
        amendedCurrentDayNonIndividual = amendedCurrentDayNonIndividual + 1

        //Asserting the response
        assertMetricsAuthorisationResponse(metricsResponse)
    }

    @Test (groups = "SmokeTest", priority = 2)
    void "Consent Amendment for Joint account consent"(){

        auConfiguration.setPsuNumber(0)
        automationResponse = doJointAccountConsentAuthorisation(auConfiguration.getAppInfoClientID(), true)
        authorisationCode = AUTestUtil.getCodeFromJwtResponse(automationResponse.currentUrl.get())

        //Generate User Access Token
        AccessTokenResponse userAccessTokenRes = getUserAccessTokenResponse()
        String cdrArrangementId = userAccessTokenRes.getCustomParameters().get("cdr_arrangement_id")
        accessToken = userAccessTokenRes.tokens.accessToken
        Assert.assertNotNull(cdrArrangementId)

        //Consent Amendment
        //remove an existing scope and add a new scope to amend the consent
        scopes.remove(AUAccountScope.BANK_TRANSACTION_READ)
        scopes.add(AUAccountScope.BANK_PAYEES_READ)

        //Retrieve the second authorization code
        authorisationCode = doConsentAmendmentAuthorisation(scopes, cdrArrangementId,
                AUConstants.DEFAULT_SHARING_DURATION, auConfiguration.getAppInfoClientID())
        Assert.assertNotNull(authorisationCode)

        //Generate User Access Token
        userAccessTokenRes = getUserAccessTokenResponse()
        cdrArrangementId = userAccessTokenRes.getCustomParameters().get("cdr_arrangement_id")
        accessToken = userAccessTokenRes.tokens.accessToken
        Assert.assertNotNull(cdrArrangementId)

        sleep(AUConstants.ABANDON_WAIT_TIME)

        //Verify Metrics Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Active Authorisation, New Ongoing Individual and Amendment count increased by 1

        activeAuthIndividual = activeAuthIndividual + 1
        newAuthCurrentDayOngoingIndividual = newAuthCurrentDayOngoingIndividual + 1
        amendedCurrentDayIndividual = amendedCurrentDayIndividual + 1

        //Asserting the response
        assertMetricsAuthorisationResponse(metricsResponse)
    }

    @Test (groups = "SmokeTest", priority = 3)
    void "Consent Amendment for Secondary account consent"(){

        auConfiguration.setPsuNumber(1)
        //Send Push Authorisation Request
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        //Select Secondary Account during authorisation
        doSecondaryAccountSelection(scopes, requestUri.toURI())
        Assert.assertNotNull(authorisationCode)

        //Generate User Access Token
        AccessTokenResponse userAccessTokenRes = getUserAccessTokenResponse()
        String cdrArrangementId = userAccessTokenRes.getCustomParameters().get("cdr_arrangement_id")
        accessToken = userAccessTokenRes.tokens.accessToken
        Assert.assertNotNull(cdrArrangementId)

        //Consent Amendment
        //remove an existing scope and add a new scope to amend the consent
        scopes.remove(AUAccountScope.BANK_TRANSACTION_READ)
        scopes.add(AUAccountScope.BANK_PAYEES_READ)

        //Retrieve the second authorization code
        authorisationCode = doConsentAmendmentAuthorisation(scopes, cdrArrangementId,
                AUConstants.DEFAULT_SHARING_DURATION, auConfiguration.getAppInfoClientID())
        Assert.assertNotNull(authorisationCode)

        //Generate User Access Token
        userAccessTokenRes = getUserAccessTokenResponse()
        cdrArrangementId = userAccessTokenRes.getCustomParameters().get("cdr_arrangement_id")
        accessToken = userAccessTokenRes.tokens.accessToken
        Assert.assertNotNull(cdrArrangementId)

        sleep(AUConstants.ABANDON_WAIT_TIME)

        //Verify Metrics Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Active Authorisation, New Ongoing Individual and Amendment count increased by 1

        activeAuthIndividual = activeAuthIndividual + 1
        newAuthCurrentDayOngoingIndividual = newAuthCurrentDayOngoingIndividual + 1
        amendedCurrentDayIndividual = amendedCurrentDayIndividual + 1

        //Asserting the response
        assertMetricsAuthorisationResponse(metricsResponse)
    }

    @Test (priority = 4)
    void "Consent Amendment with sharing duration less than 1 hr"(){

        auConfiguration.setPsuNumber(0)
        //Send Authorisation Request for 1st time
        doConsentAuthorisation()

        //Generate User Access Token
        AccessTokenResponse userAccessTokenRes = getUserAccessTokenResponse()
        String cdrArrangementId = userAccessTokenRes.getCustomParameters().get("cdr_arrangement_id")
        accessToken = userAccessTokenRes.tokens.accessToken
        Assert.assertNotNull(cdrArrangementId)

        //Consent Amendment
        //remove an existing scope and add a new scope to amend the consent
        scopes.remove(AUAccountScope.BANK_TRANSACTION_READ)
        scopes.add(AUAccountScope.BANK_PAYEES_READ)

        //Retrieve the second authorization code
        authorisationCode = doConsentAmendmentAuthorisation(scopes, cdrArrangementId,
                AUConstants.SHORT_SHARING_DURATION, auConfiguration.getAppInfoClientID())
        Assert.assertNotNull(authorisationCode)

        //Generate User Access Token
        userAccessTokenRes = getUserAccessTokenResponse()
        cdrArrangementId = userAccessTokenRes.getCustomParameters().get("cdr_arrangement_id")
        accessToken = userAccessTokenRes.tokens.accessToken
        Assert.assertNotNull(cdrArrangementId)

        sleep(AUConstants.ABANDON_WAIT_TIME)

        //Verify Metrics Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //Active Authorisation, New Ongoing Individual count increased by 1 and Amendment count not changed.

        activeAuthIndividual = activeAuthIndividual + 1
        newAuthCurrentDayOngoingIndividual = newAuthCurrentDayOngoingIndividual + 1

        //Asserting the response
        assertMetricsAuthorisationResponse(metricsResponse)
    }

    @Test (priority = 5)
    void "Amend onceoff consent to an ongoing consent"(){

        auConfiguration.setPsuNumber(0)
        //Send Authorisation Request for 1st time
        response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.SHORT_SHARING_DURATION,
                true, "", clientId)
        requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
        doConsentAuthorisationViaRequestUri(scopes, requestUri.toURI(), clientId, AUAccountProfile.INDIVIDUAL)

        //Generate User Access Token
        AccessTokenResponse userAccessTokenRes = getUserAccessTokenResponse()
        String cdrArrangementId = userAccessTokenRes.getCustomParameters().get("cdr_arrangement_id")
        accessToken = userAccessTokenRes.tokens.accessToken
        Assert.assertNotNull(cdrArrangementId)

        //Consent Amendment
        //remove an existing scope and add a new scope to amend the consent
        scopes.remove(AUAccountScope.BANK_TRANSACTION_READ)
        scopes.add(AUAccountScope.BANK_PAYEES_READ)

        //Retrieve the second authorization code
        authorisationCode = doConsentAmendmentAuthorisation(scopes, cdrArrangementId,
                AUConstants.DEFAULT_SHARING_DURATION, auConfiguration.getAppInfoClientID())
        Assert.assertNotNull(authorisationCode)

        //Generate User Access Token
        userAccessTokenRes = getUserAccessTokenResponse()
        cdrArrangementId = userAccessTokenRes.getCustomParameters().get("cdr_arrangement_id")
        accessToken = userAccessTokenRes.tokens.accessToken
        Assert.assertNotNull(cdrArrangementId)

        sleep(AUConstants.ABANDON_WAIT_TIME)

        //Verify Metrics Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //New OnceOff Individual count increased and Amendment count increased by 1.

        newAuthCurrentDayOnceOffIndividual = newAuthCurrentDayOnceOffIndividual + 1
        amendedCurrentDayIndividual = amendedCurrentDayIndividual + 1

        //Asserting the response
        assertMetricsAuthorisationResponse(metricsResponse)
    }

    @Test  (priority = 6)
    void "Amendment of Consent More than once create multiple records"(){

        auConfiguration.setPsuNumber(0)
        //Send Authorisation Request for 1st time
        doConsentAuthorisation()

        //Generate User Access Token
        AccessTokenResponse userAccessTokenRes = getUserAccessTokenResponse()
        String cdrArrangementId = userAccessTokenRes.getCustomParameters().get("cdr_arrangement_id")
        accessToken = userAccessTokenRes.tokens.accessToken
        Assert.assertNotNull(cdrArrangementId)

        //Consent Amendment - 1st time
        //remove an existing scope and add a new scope to amend the consent
        scopes.remove(AUAccountScope.BANK_TRANSACTION_READ)
        scopes.add(AUAccountScope.BANK_PAYEES_READ)

        //Retrieve the second authorization code
        secondAuthorisationCode = doConsentAmendmentAuthorisation(scopes, cdrArrangementId,
                AUConstants.DEFAULT_SHARING_DURATION, auConfiguration.getAppInfoClientID())
        Assert.assertNotNull(secondAuthorisationCode)

        //Generate User Access Token
        userAccessToken = AURequestBuilder.getUserToken(secondAuthorisationCode, AUConstants.CODE_VERIFIER,
                auConfiguration.getAppInfoClientID())
        String cdrArrangementId2 = userAccessToken.getCustomParameters().get("cdr_arrangement_id")
        Assert.assertNotNull(cdrArrangementId2)
        Assert.assertEquals(cdrArrangementId2, cdrArrangementId)

        //Consent Amendment - 2nd Time
        //remove an existing scope and add a new scope to amend the consent
        scopes.remove(AUAccountScope.BANK_CUSTOMER_DETAIL_READ)

        //Retrieve the third authorization code
        thirdAuthorisationCode = doConsentAmendmentAuthorisation(scopes, cdrArrangementId2,
                AUConstants.AMENDED_SHARING_DURATION, auConfiguration.getAppInfoClientID())
        Assert.assertNotNull(thirdAuthorisationCode)

        //Retrieve the third user access token and assert the CDR arrangement ID is the same.
        userAccessToken = AURequestBuilder.getUserToken(thirdAuthorisationCode, AUConstants.CODE_VERIFIER,
                auConfiguration.getAppInfoClientID())
        def cdrArrangementId3 = userAccessToken.getCustomParameters().get("cdr_arrangement_id")
        Assert.assertNotNull(cdrArrangementId3)
        Assert.assertEquals(cdrArrangementId3, cdrArrangementId)

        sleep(AUConstants.ABANDON_WAIT_TIME)

        //Verify Metrics Response
        def metricsResponse = getMetrics(AUConstants.PERIOD_CURRENT)

        Assert.assertEquals(metricsResponse.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(metricsResponse.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)

        //New OnceOff Individual count increased and Amendment count increased by 1.
        activeAuthIndividual = activeAuthIndividual + 1
        newAuthCurrentDayOnceOffIndividual = newAuthCurrentDayOnceOffIndividual + 1
        amendedCurrentDayIndividual = amendedCurrentDayIndividual + 2

        //Asserting the response
        assertMetricsAuthorisationResponse(metricsResponse)
    }
}

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

package org.wso2.cds.test.framework

import com.fasterxml.jackson.databind.JsonNode
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.nimbusds.oauth2.sdk.ResponseMode
import com.nimbusds.oauth2.sdk.ResponseType
import com.nimbusds.oauth2.sdk.token.RefreshToken
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.wso2.cds.test.framework.constant.AUAccountProfile
import org.wso2.cds.test.framework.constant.AUAccountScope
import org.wso2.cds.test.framework.constant.AUConfigConstants
import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.constant.AUPageObjects
import org.wso2.cds.test.framework.constant.AUPayloads
import org.wso2.cds.test.framework.constant.ContextConstants
import com.nimbusds.oauth2.sdk.AccessTokenResponse
import org.wso2.cds.test.framework.request_builder.AUJWTGenerator
import org.wso2.cds.test.framework.utility.DbConnection
import org.wso2.cds.test.framework.utility.SqlQuery
import org.wso2.openbanking.test.framework.OBTest
import org.wso2.cds.test.framework.configuration.AUConfigurationService
import org.wso2.openbanking.test.framework.automation.AutomationMethod
import org.wso2.openbanking.test.framework.automation.NavigationAutomationStep
import org.wso2.openbanking.test.framework.configuration.OBConfigParser
import io.restassured.http.ContentType
import io.restassured.response.Response
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.testng.Assert
import org.testng.ITestContext
import org.testng.annotations.BeforeClass
import org.wso2.cds.test.framework.request_builder.AUAuthorisationBuilder
import org.wso2.cds.test.framework.request_builder.AURegistrationRequestBuilder
import org.wso2.cds.test.framework.request_builder.AURequestBuilder
import org.wso2.cds.test.framework.utility.AURestAsRequestBuilder
import org.wso2.cds.test.framework.utility.AUTestUtil
import org.testng.asserts.SoftAssert
import org.wso2.cds.test.framework.automation.consent.AUAccountSelectionStep
import org.wso2.cds.test.framework.automation.consent.AUBasicAuthAutomationStep

import java.nio.charset.Charset
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import com.google.gson.Gson

import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

/**
 * Class for defining common methods that needed in test classes.
 * Every test class in Test layer should extended from this.
 * Execute test framework initialization process
 */
class AUTest extends OBTest {

    static AUConfigurationService auConfiguration
    protected static Logger log = LogManager.getLogger(AUTest.class.getName())
    AUAuthorisationBuilder auAuthorisationBuilder
    private boolean adrNameCheck
    AUJWTGenerator generator
    public static int activeAuthIndividual, activeAuthNonIndividual, newAuthCurrentDayOngoingIndividual,
                      newAuthCurrentDayOngoingNonIndividual, newAuthCurrentDayOnceOffIndividual,
                      newAuthCurrentDayOnceOffNonIndividual, revokedCurrentDayIndividual, revokedCurrentDayNonIndividual,
                      amendedCurrentDayIndividual, amendedCurrentDayNonIndividual, expiredCurrentDayIndividual,
                      expiredCurrentDayNonIndividual, abandonedPreIdentificationCurrentDay, abandonedPreAuthenticationCurrentDay,
                      abandonedPreAccountSelectionCurrentDay, abandonedPreAuthorisationCurrentDay, abandonedRejectedCurrentDay,
                      abandonedFailedTokenExchangeCurrentDay, abandonedCurrentDay, unauthenticatedCurrentDay,
                      highPriorityCurrentDay, lowPriorityCurrentDay, unattendedCurrentDay, largePayloadCurrentDay,
                      customerCount, recipientCount, sessionCount

    int totalInvocationsPerf, totalInvocationsAvg, totalInvocationsHighPerf, totalInvocationsLowPerf,
        totalInvocationsUnattendedPerf, totalInvocationsUnAuthPerf, totalInvocationsLargePayPerf,
        totalInvocationsHighAvg, totalInvocationsLowAvg, totalInvocationsUnattendedAvg, totalInvocationsUnAuthAvg,
        totalInvocationsLargePayAvg
    public LocalDate today = LocalDate.now()
    int currentTime
    public String unauthErrorCurrentDay, authErrorCurrentDay, aggErrorCurrentDay
    public int[] performanceMetrics
    public int[] avgResponseMetrics
    public int[] totalResources

    @BeforeClass(alwaysRun = true)
    void "Initialize Test Suite"() {
        OBConfigParser.getInstance(AUConfigConstants.CONFIG_FILE_LOCATION)
        AURestAsRequestBuilder.init()
        auConfiguration = new AUConfigurationService()
        auAuthorisationBuilder = new AUAuthorisationBuilder()
    }

    public List<AUAccountScope> scopes = [
            AUAccountScope.BANK_ACCOUNT_BASIC_READ,
            AUAccountScope.BANK_ACCOUNT_DETAIL_READ,
            AUAccountScope.BANK_TRANSACTION_READ,
            AUAccountScope.BANK_PAYEES_READ,
            AUAccountScope.BANK_REGULAR_PAYMENTS_READ,
            AUAccountScope.BANK_CUSTOMER_BASIC_READ,
            AUAccountScope.BANK_CUSTOMER_DETAIL_READ
    ]

    private List<String> DCRScopes
    public String redirectURL
    public String userAccessToken, refreshToken
    public String authorisationCode
    public String consentedAccount
    public String secondConsentedAccount
    public String cdrArrangementId = ""
    public String jtiVal
    public String clientId
    public String accessToken
    public String requestUri
    public String authoriseUrl
    public String authFlowError
    public Response response, revocationResponse
    public def automationResponse
    public String secondaryAccountId, secondaryUserId
    public String productId
    public Response deletionResponse
    public AUJWTGenerator generator = new AUJWTGenerator()

    /**
     * Set Scopes of application
     * can be used in any testcase
     * @param scopeList
     */
    void setApplicationScope(List<String> scopeList) {
        this.DCRScopes = scopeList
    }

    /**
     * Set redirect URL of application
     * can be used in any testcase
     * @param url
     */
    void setRedirectURL(String url) {
        this.redirectURL = url
    }

    /**
     * Get Scopes
     * @return
     */
    List<String> getApplicationScope() {
        if (this.DCRScopes == null) {
            this.DCRScopes = [
                    AUAccountScope.CDR_REGISTRATION.getScopeString(),
                    AUAccountScope.OPENID.getScopeString()
            ]
        }
        return this.DCRScopes
    }

    String getRedirectURL() {
        if (this.redirectURL == null) {
            this.redirectURL = auConfiguration.getAppInfoRedirectURL()
        }
        return this.redirectURL
    }

    /**
     * Consent Authorization method
     * @param clientId
     * @param profiles
     */
    void doConsentAuthorisation(String clientId = null, AUAccountProfile profiles = AUAccountProfile.INDIVIDUAL) {

        def response

        if (clientId == null) {
            response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                    true, "")
            requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
            doConsentAuthorisationViaRequestUri(scopes, requestUri.toURI(), null, profiles)
        } else {
            response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                    true, "", clientId)
            requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
            doConsentAuthorisationViaRequestUri(scopes, requestUri.toURI(), clientId, profiles)
        }
    }

    /**
     * Do Consent Authorisation with Response_Mode and Response Type
     * @param responseMode
     * @param clientId
     * @param profiles
     */
    void doConsentAuthorisation(ResponseMode responseMode, ResponseType responseType = ResponseType.CODE, String clientId = null,
                                AUAccountProfile profiles = AUAccountProfile.INDIVIDUAL, boolean isStateParamPresent = true) {

        def response

        if (clientId == null) {
            response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                    true, "", "", auConfiguration.getAppInfoRedirectURL(),
                    responseType.toString(), isStateParamPresent, responseMode.toString())
            requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
            doConsentAuthorisationViaRequestUri(scopes, requestUri.toURI(), responseMode, responseType,
                    null, profiles, isStateParamPresent)
        } else {
            response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                    true, "", clientId, auConfiguration.getAppInfoRedirectURL(),
                    responseType.toString(), isStateParamPresent, responseMode.toString())
            requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
            doConsentAuthorisationViaRequestUri(scopes, requestUri.toURI(), responseMode, responseType,
                    clientId, profiles, isStateParamPresent)
        }
    }

    /**
     * Consent authorization method with Request URI
     * @param scopes
     * @param requestUri
     * @param clientId
     * @param profiles
     */
    void doConsentAuthorisationViaRequestUri(List<AUAccountScope> scopes, URI requestUri,
                                             String clientId = null, AUAccountProfile profiles = null) {

        if (clientId != null) {
            authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri, clientId)
                    .toURI().toString()
        } else {
            authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri)
                    .toURI().toString()
        }

        //UI Flow Navigation
        def automation = doAuthorisationFlowNavigation(authoriseUrl, profiles, true)
        authorisationCode = AUTestUtil.getCodeFromJwtResponse(automation.currentUrl.get())
    }

    /**
     *  Authorization method
     * @param sharingDuration
     * @param sendSharingDuration
     * @return
     */
    String doAuthorization(long sharingDuration, boolean sendSharingDuration) {
        AUAuthorisationBuilder auAuthorisationBuilder = new AUAuthorisationBuilder()
        String authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(scopes, sharingDuration, sendSharingDuration)
                .toURI().toString()

        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep(new AUAccountSelectionStep())
                .addStep(getWaitForRedirectAutomationStep())
                .execute()
        // Get Code From URL
        String authCode = AUTestUtil.getCodeFromJwtResponse(automation.currentUrl.get())
        return authCode
    }

    /**
     * Method for get user access token response
     * @return
     */
    AccessTokenResponse getUserAccessTokenResponse(String clientId = null) {
        try {
            return AURequestBuilder.getUserToken(authorisationCode, AUConstants.CODE_VERIFIER, clientId)
        }
        catch (Exception e) {
            log.error(e)
        }
    }

    /**
     * Method for get new User access token
     */
    void generateUserAccessToken(String clientId = null) {
        userAccessToken = getUserAccessTokenResponse(clientId).tokens.accessToken
    }

    /**
     * Method for get CDR Arrangement ID
     * @param clientId
     */
    void generateCDRArrangementId(String clientId = null) {
        cdrArrangementId = getUserAccessToken(clientId).getCustomParameters().get("cdr_arrangement_id")
    }

    /**
     * Get existing User access token if already generated.
     * otherwise new user access token will be generated
     */
    void getUserAccessToken(ITestContext context) {
        userAccessToken = context.getAttribute(ContextConstants.USER_ACCESS_TKN) as String
        if (userAccessToken == null) {
            System.out.println("Generate new user access token")
            doConsentAuthorisation()
            generateUserAccessToken()
            context.setAttribute(ContextConstants.USER_ACCESS_TKN, userAccessToken)
        }
    }


    /**
     * Method for delete application
     * @param scopes
     * @param clientId
     */
    void deleteApplicationIfExists(List<String> scopes, String clientId = auConfiguration.getAppInfoClientID()) {
        if (clientId) {
            String token = AURequestBuilder.getApplicationAccessToken(scopes, clientId)

            if (token) {
                deletionResponse = AURegistrationRequestBuilder.buildBasicRequest(token)
                        .when()
                        .delete(AUConstants.DCR_REGISTRATION_ENDPOINT + clientId)
            }
        }
    }

    /**
     * Method for delete application
     * @param scopes
     * @param clientId
     */
    void deleteApplicationIfExists(String clientId = auConfiguration.getAppInfoClientID()) {
        if (!clientId.equalsIgnoreCase("Application.ClientID")) {
            String token = getApplicationAccessToken(clientId)

            if (token) {
                deletionResponse = AURegistrationRequestBuilder.buildBasicRequest(token)
                        .when()
                        .delete(AUConstants.DCR_REGISTRATION_ENDPOINT + clientId)

            }
        }
    }

    /**
     * Get Application access token
     * @param clientId
     */
    String getApplicationAccessToken(String clientId = auConfiguration.getAppInfoClientID()) {
        String token = AURequestBuilder.getApplicationAccessToken(getApplicationScope(), clientId)
        if (token != null) {
            addToContext(ContextConstants.APP_ACCESS_TKN, token)
        } else {
            log.error("Application access Token Cannot be generated")
        }
        return token
    }

    /**
     * Basic TPP Registration Method.
     * @return response.
     */
    Response tppRegistration() {

        AURegistrationRequestBuilder reg = new AURegistrationRequestBuilder()

        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(reg.getAURegularClaims())
                .when()
                .post(AUConstants.DCR_REGISTRATION_ENDPOINT)

        return registrationResponse
    }

    /**
     * Basic TPP Deletion Method.
     * @param clientId
     * @param accessToken
     * @return response.
     */
    Response tppDeletion(String clientId, String accessToken) {

        def registrationResponse = AURegistrationRequestBuilder.buildBasicRequest(accessToken)
                .when()
                .delete(AUConstants.DCR_REGISTRATION_ENDPOINT + clientId)

        return registrationResponse
    }

    String getCDSClient() {
        auConfiguration = new AUConfigurationService()
        return "${auConfiguration.getAppInfoClientID()}:${auConfiguration.getAppInfoClientSecret()}"
    }

    /**
     * Common method to automate the Profile Selection and Account Selection in Authorisation Flow.
     * @param authWebDriver
     * @param profiles
     * @param isSelectMultipleAccounts
     */
    void selectProfileAndAccount(AutomationMethod authWebDriver, AUAccountProfile profiles = null,
                                 boolean isSelectMultipleAccounts = false) {

        //If Profile Selection Enabled
        if (auConfiguration.getProfileSelectionEnabled()) {
            if (profiles == AUAccountProfile.ORGANIZATION_A) {

                //Select Business Profile
                authWebDriver.selectOption(AUPageObjects.ORGANIZATION_A_PROFILE_SELECTION)
                authWebDriver.clickButtonXpath(AUPageObjects.PROFILE_SELECTION_NEXT_BUTTON)

                //Select Business Account 1
                consentedAccount = authWebDriver.getElementAttribute(AUTestUtil.getBusinessAccount1CheckBox(),
                        AUPageObjects.VALUE)
                authWebDriver.clickButtonXpath(AUTestUtil.getBusinessAccount1CheckBox())

            } else if (profiles == AUAccountProfile.ORGANIZATION_B) {

                //Select Business Profile
                authWebDriver.selectOption(AUPageObjects.ORGANIZATION_B_PROFILE_SELECTION)
                authWebDriver.clickButtonXpath(AUPageObjects.PROFILE_SELECTION_NEXT_BUTTON)

                //Select Business Account 1
                consentedAccount = authWebDriver.getElementAttribute(AUTestUtil.getBusinessAccount2CheckBox(),
                        AUPageObjects.VALUE)
                authWebDriver.clickButtonXpath(AUTestUtil.getBusinessAccount2CheckBox())

//                if (isSelectMultipleAccounts) {
//                    //Select Business Account 2
//                    secondConsentedAccount = authWebDriver.getElementAttribute(AUTestUtil.getBusinessAccount3CheckBox(),
//                            AUPageObjects.VALUE)
//                    authWebDriver.clickButtonXpath(AUTestUtil.getBusinessAccount3CheckBox())
//                }
            } else {
                //Select Individual Profile
                authWebDriver.selectOption(AUPageObjects.INDIVIDUAL_PROFILE_SELECTION)
                authWebDriver.clickButtonXpath(AUPageObjects.PROFILE_SELECTION_NEXT_BUTTON)

                //Select Individual Account 1
                consentedAccount = authWebDriver.getElementAttribute(AUTestUtil.getSingleAccountXPath(),
                        AUPageObjects.VALUE)
                authWebDriver.clickButtonXpath(AUTestUtil.getSingleAccountXPath())

                if(isSelectMultipleAccounts) {
                    //Select Individual Account 2
                    secondConsentedAccount = authWebDriver.getElementAttribute(AUTestUtil.getAltSingleAccountXPath(),
                            AUPageObjects.VALUE)
                    authWebDriver.clickButtonXpath(AUTestUtil.getAltSingleAccountXPath())
                }
            }
        }
        //If Profile Selection Disabled
        else {
            //Select Account 1
            consentedAccount = authWebDriver.getElementAttribute(AUTestUtil.getSingleAccountXPath(),
                    AUPageObjects.VALUE)
            authWebDriver.clickButtonXpath(AUTestUtil.getSingleAccountXPath())

            if (isSelectMultipleAccounts) {
                //Select Account 2
                secondConsentedAccount = authWebDriver.getElementAttribute(AUTestUtil.getAltSingleAccountXPath(),
                        AUPageObjects.VALUE)
                authWebDriver.clickButtonXpath(AUTestUtil.getAltSingleAccountXPath())
            }
        }
    }

    /**
     * Common method to automate the Secondary Account Selection in Authorisation Flow.
     * @param authWebDriver
     * @param profiles
     * @param isSelectMultipleAccounts
     */
    void selectSecondaryAccount(AutomationMethod authWebDriver, boolean isSelectMultipleAccounts = false) {

        //Select Account 1
        consentedAccount = authWebDriver.getElementAttribute(AUTestUtil.getSecondaryAccount1XPath(),
                AUPageObjects.VALUE)
        authWebDriver.clickButtonXpath(AUTestUtil.getSecondaryAccount1XPath())

        if (isSelectMultipleAccounts) {
            //Select Account 2
            consentedAccount = authWebDriver.getElementAttribute(AUTestUtil.getSecondaryAccount2XPath(),
                    AUPageObjects.VALUE)
            authWebDriver.clickButtonXpath(AUTestUtil.getSecondaryAccount2XPath())
        }
    }

    /**
     * Consent Authorisation without Account Selection.
     */
    void doConsentAuthorisationWithoutAccountSelection(String profiles = AUAccountProfile.INDIVIDUAL) {

        def response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "")
        String requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)

        AUAuthorisationBuilder auAuthorisationBuilder = new AUAuthorisationBuilder()
        authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), auConfiguration.getAppInfoClientID())
                .toURI().toString()

        automationResponse = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //If Profile Selection Enabled
                    if (auConfiguration.getProfileSelectionEnabled()) {
                        if (profiles == AUAccountProfile.ORGANIZATION_A) {

                            //Select Business Profile
                            authWebDriver.selectOption(AUPageObjects.ORGANIZATION_A_PROFILE_SELECTION)
                            authWebDriver.clickButtonXpath(AUPageObjects.PROFILE_SELECTION_NEXT_BUTTON)
                        } else if (profiles == AUAccountProfile.ORGANIZATION_B) {

                            //Select Business Profile
                            authWebDriver.selectOption(AUPageObjects.ORGANIZATION_B_PROFILE_SELECTION)
                            authWebDriver.clickButtonXpath(AUPageObjects.PROFILE_SELECTION_NEXT_BUTTON)
                        } else {
                            //Select Individual Profile
                            authWebDriver.selectOption(AUPageObjects.INDIVIDUAL_PROFILE_SELECTION)
                            authWebDriver.clickButtonXpath(AUPageObjects.PROFILE_SELECTION_NEXT_BUTTON)
                        }
                    }

                    //Click Confirm Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)
                }
                .execute()

        // Get Code From URL
        authorisationCode = AUTestUtil.getCodeFromJwtResponse(automationResponse.currentUrl.get())
    }

    /**
     * Consent Authorisation by selecting one account.
     *
     * @param scopes
     * @param requestUri
     * @param clientId
     * @param profiles
     */
    void doConsentAuthorisationViaRequestUriSingleAccount(List<AUAccountScope> scopes, URI requestUri,
                                                          String clientId = null , AUAccountProfile profiles = null) {
        if (clientId != null) {
            authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri, clientId)
                    .toURI().toString()
        } else {
            authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri)
                    .toURI().toString()
        }

        //UI FLow Navigation
        def automation = doAuthorisationFlowNavigation(authoriseUrl, profiles, false)

        // Get Code From URL
        authorisationCode = AUTestUtil.getCodeFromJwtResponse(automation.currentUrl.get())
    }

    /**
     * Consent Authorization with request URI for sharing duration greater than one year.
     * @param scopes
     * @param requestUri
     * @param clientId
     * @param profiles
     */
    void doConsentAuthorisationViaRequestUriLargeSharingDue(List<AUAccountScope> scopes, URI requestUri,
                                                            String clientId = null , AUAccountProfile profiles = null) {
        if (clientId != null) {
            authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri, clientId)
                    .toURI().toString()
        } else {
            authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri)
                    .toURI().toString()
        }

        OffsetDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC)
        String consentExpiry = currentTime.plusSeconds(AUConstants.ONE_YEAR_DURATION).getYear().toString()

        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Select Profile and Accounts
                    selectProfileAndAccount(authWebDriver, profiles, false)

                    //Click Submit/Next Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_SUBMIT_XPATH)

                    //Check Consent Expiry
                    String expiryTime = authWebDriver.getElementAttribute(AUPageObjects.CONSENT_EXPIRY_XPATH,
                            AUPageObjects.TEXT)
                    Assert.assertTrue(expiryTime.contains(consentExpiry))

                    //Click Confirm Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)
                }
                .addStep(getWaitForRedirectAutomationStep())
                .execute()

        // Get Code From URL
        authorisationCode = AUTestUtil.getCodeFromJwtResponse(automation.currentUrl.get())
    }

    /**
     * Consent AAuthorization Deny flow.
     * @param scopes
     * @param requestUri
     * @param clientId
     * @param profiles
     */
    String doConsentAuthorisationViaRequestUriDenyFlow(List<AUAccountScope> scopes, URI requestUri,
                                                       String clientId = null , AUAccountProfile profiles = null,
                                                       boolean isStateParamPresent = true) {
        if (clientId != null) {
            authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri,
                    auConfiguration.getAppInfoClientID(), isStateParamPresent).toURI().toString()
        } else {
            authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri, null,
                    isStateParamPresent).toURI().toString()
        }

        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Select Profile and Accounts
                    selectProfileAndAccount(authWebDriver, profiles, true)

                    //Click Submit/Next Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_SUBMIT_XPATH)

                    //Click Deny Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_DENY_XPATH)
                }
                .execute()

        return automation.currentUrl.get()
    }

    /**
     * Consent Authorisation without Account Selection
     * @param scopes
     * @param requestUri
     * @param clientId
     * @param profiles
     */
    void doConsentAuthorisationViaRequestUriNoAccountSelection(List<AUAccountScope> scopes, URI requestUri,
                                                               String clientId = null , AUAccountProfile profiles = null) {
        if (clientId != null) {
            authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri, clientId)
                    .toURI().toString()
        } else {
            authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri)
                    .toURI().toString()
        }

        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //If Profile Selection Enabled
                    if (auConfiguration.getProfileSelectionEnabled()) {
                        if (profiles == AUAccountProfile.ORGANIZATION_A) {
                            //Select Business Profile
                            authWebDriver.selectOption(AUPageObjects.ORGANIZATION_A_PROFILE_SELECTION)
                            authWebDriver.clickButtonXpath(AUPageObjects.PROFILE_SELECTION_NEXT_BUTTON)
                        }
                        else {
                            //Select Individual Profile
                            authWebDriver.selectOption(AUPageObjects.INDIVIDUAL_PROFILE_SELECTION)
                            authWebDriver.clickButtonXpath(AUPageObjects.PROFILE_SELECTION_NEXT_BUTTON)
                        }
                    }
                    //If Profile Selection Disabled
                    else {
                        authWebDriver.clickButtonXpath(AUPageObjects.LBL_PERMISSION_HEADER)
                    }

                    //Click Confirm Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_SUBMIT_XPATH)
                }
                .execute()

        // Get Code From URL
        authorisationCode = AUTestUtil.getCodeFromJwtResponse(automation.currentUrl.get())
    }

    /**
     * Consent Authorisation with Secondary Account Selection.
     * @param scopes
     * @param requestUri
     * @param clientId
     * @param profiles
     */
    void doSecondaryAccountSelection(List<AUAccountScope> scopes, URI requestUri, String clientId = null,
                                     boolean isMultipleAccountsSelect = false) {

        auConfiguration.setPsuNumber(1)

        if (clientId != null) {
            authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri, clientId)
                    .toURI().toString()
        } else {
            authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri)
                    .toURI().toString()
        }

        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Select Secondary Account
                    selectSecondaryAccount(authWebDriver, isMultipleAccountsSelect)

                    //Click Submit/Next Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_SUBMIT_XPATH)

                    //Click Confirm Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)
                }
                .execute()

        // Get Code From URL
        authorisationCode = AUTestUtil.getCodeFromJwtResponse(automation.currentUrl.get())
    }

    /**
     * Verify Unavailable Accounts in Secondary Account Selection Flow.
     * @param scopes
     * @param requestUri
     * @param clientId
     * @param profiles
     */
    void doSecondaryAccountSelectionCheckUnavailableAccounts(List<AUAccountScope> scopes, URI requestUri,
                                                             String clientId = null , AUAccountProfile profiles = null) {

        if (clientId != null) {
            authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri, clientId)
                    .toURI().toString()
        } else {
            authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri)
                    .toURI().toString()
        }

        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    Assert.assertTrue(authWebDriver.getElementAttribute(AUPageObjects.ADR_NAME_HEADER_XPATH, AUPageObjects.TEXT)
                            .contains(auConfiguration.getAppDCRSoftwareId()))

                    //Select Secondary Account
                    selectSecondaryAccount(authWebDriver, true)

                    //Verify the Unavailable Accounts Topic
                    Assert.assertTrue(authWebDriver.isElementDisplayed(AUPageObjects.LBL_ACCOUNTS_UNAVAILABLE_TO_SHARE))

                    // Assert the first Unavailable Account
                    Assert.assertTrue(authWebDriver.isElementDisplayed(AUPageObjects.LBL_FIRST_UNAVAILABLE_ACCOUNT))

                    //Click Submit/Next Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_SUBMIT_XPATH)

                    //Click Confirm Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)
                }
                .addStep(getWaitForRedirectAutomationStep())
                .execute()

        // Get Code From URL
        authorisationCode = AUTestUtil.getCodeFromJwtResponse(automation.currentUrl.get())
    }

    /**
     * Authorisation via Request Uri Without Account Selection Step.
     * @param authoriseUrl
     * @return authorisationCode
     */
    String doAuthorisationViaRequestUriWithoutAccSelection(String authoriseUrl) {

        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Click Submit/Next Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_SUBMIT_XPATH)
                }
                .addStep(getWaitForRedirectAutomationStep())
                .execute()

        // Get Code From URL
        authorisationCode = AUTestUtil.getCodeFromJwtResponse(automation.currentUrl.get())
        return authorisationCode
    }

    /**
     * Send Account Retrieval Request.
     * @param userAccessToken
     * @param accountEndpointVersion
     * @return account retrieval request
     */
    Response doAccountRetrieval(String userAccessToken, int accountEndpointVersion = AUConstants.X_V_HEADER_ACCOUNTS) {

        response = AURequestBuilder.buildBasicRequest(userAccessToken, accountEndpointVersion)
                .header(AUConstants.PARAM_FAPI_AUTH_DATE,AUConstants.VALUE_FAPI_AUTH_DATE)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get("${AUConstants.BULK_ACCOUNT_PATH}")

        return response
    }

    /**
     * Send Consent Search Request.
     * @return consent search request
     */
    Response doConsentSearch() {

        response = AURestAsRequestBuilder.buildBasicRequest()
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, AUConstants.BASIC_HEADER_KEY + " " +
                        Base64.encoder.encodeToString(
                                "${auConfiguration.getUserBasicAuthName()}:${auConfiguration.getUserBasicAuthPWD()}"
                                        .getBytes(Charset.forName("UTF-8"))))
                .queryParam(AUConstants.QUERY_PARAM_USERID, auConfiguration.getUserPSUName())
                .baseUri(auConfiguration.getServerAuthorisationServerURL())
                .get("${AUConstants.CONSENT_SEARCH_ENDPOINT}")

        return response
    }

    /**
     * Consent Authorization by Selecting Single Account.
     * @param clientId
     * @param profiles
     */
    void doConsentAuthorisationSelectingSingleAccount(String clientId = null,
                                                      AUAccountProfile profiles = AUAccountProfile.INDIVIDUAL) {

        def response

        if (clientId == null) {
            response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                    true, "")
            requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
            doConsentAuthorisationViaRequestUriSingleAccount(scopes, requestUri.toURI(), null, profiles)
        } else {
            response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                    true, "", clientId)
            requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
            doConsentAuthorisationViaRequestUriSingleAccount(scopes, requestUri.toURI(), clientId, profiles)
        }
    }

    /**
     * Add and Update Business User Permission for a Single User.
     * @param headerString
     * @param accountID
     * @param accountOwnerUserID
     * @param nominatedRepUserID
     * @param permissionType
     * @return response
     */
    Response updateSingleBusinessUserPermission(String headerString, String accountID, String accountOwnerUserID, String nominatedRepUserID,
                                                String permissionType) {

        def requestBody = AUPayloads.getSingleUserNominationPayload(accountID, accountOwnerUserID, nominatedRepUserID, permissionType)

        return AURestAsRequestBuilder.buildBasicRequest()
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, AUConstants.BASIC_HEADER_KEY + " " +
                        Base64.encoder.encodeToString(
                                "${auConfiguration.getUserBasicAuthName()}:${auConfiguration.getUserBasicAuthPWD()}"
                                        .getBytes(Charset.forName("UTF-8"))))
                .contentType(AUConstants.CONTENT_TYPE_APPLICATION_JSON)
                .body(requestBody)
                .baseUri(auConfiguration.getServerAuthorisationServerURL())
                .put("${AUConstants.CONSENT_STATUS_AU_ENDPOINT}${AUConstants.UPDATE_BUSINESS_USER}")
    }

    /**
     * Add and Update Business User Permission for a Multiple Users.
     * @param headerString
     * @param accountID
     * @param accountOwnerUserID
     * @param nominatedRepUserID
     * @param permissionType
     * @param nominatedRepUserID2
     * @param permissionType2
     * @return
     */
    Response updateMultiBusinessUserPermission(String headerString, String accountID, String accountOwnerUserID, String nominatedRepUserID,
                                               String permissionType, String nominatedRepUserID2, String permissionType2) {

        def requestBody = AUPayloads.getMultiUserNominationPayload(accountID, accountOwnerUserID, nominatedRepUserID,
                permissionType, nominatedRepUserID2, permissionType2)

        return AURestAsRequestBuilder.buildBasicRequest()
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, AUConstants.BASIC_HEADER_KEY + " " +
                        Base64.encoder.encodeToString(
                                "${auConfiguration.getUserBasicAuthName()}:${auConfiguration.getUserBasicAuthPWD()}"
                                        .getBytes(Charset.forName("UTF-8"))))
                .contentType(AUConstants.CONTENT_TYPE_APPLICATION_JSON)
                .body(requestBody)
                .baseUri(auConfiguration.getServerAuthorisationServerURL())
                .put("${AUConstants.CONSENT_STATUS_AU_ENDPOINT}${AUConstants.UPDATE_BUSINESS_USER}")
    }

    /**
     * Get Sharable bank account list of the secondary ,Joint and Business users.
     * @return response.
     */
    Response getSharableBankAccounts() {

        return AURestAsRequestBuilder.buildBasicRequest()
                .baseUri(getAuConfiguration().getSharableAccountUrl())
                .get("${AUConstants.SHARABLE_BANK_ACCOUNT_SERVICE}${AUConstants.BANK_ACCOUNT_SERVICE}")
    }

    /**
     * Delete Single Business User Nomination.
     * @param headerString
     * @param accountID
     * @param accountOwnerUserID
     * @param nominatedRepUserID
     * @return response
     */
    Response deleteSingleBusinessUser(String headerString, String accountID, String accountOwnerUserID, String nominatedRepUserID) {

        def requestBody = AUPayloads.getSingleUserDeletePayload(accountID, accountOwnerUserID, nominatedRepUserID)

        return AURestAsRequestBuilder.buildRequest()
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, AUConstants.BASIC_HEADER_KEY + " " +
                        Base64.encoder.encodeToString(
                                "${auConfiguration.getUserBasicAuthName()}:${auConfiguration.getUserBasicAuthPWD()}"
                                        .getBytes(Charset.forName("UTF-8"))))
                .contentType(AUConstants.CONTENT_TYPE_APPLICATION_JSON)
                .body(requestBody)
                .baseUri(getAuConfiguration().getServerAuthorisationServerURL())
                .delete("${AUConstants.CONSENT_STATUS_AU_ENDPOINT}${AUConstants.UPDATE_BUSINESS_USER}")
    }

    /**
     * Delete Multiple Business User Nomination.
     * @param headerString
     * @param accountID
     * @param accountOwnerUserID
     * @param nominatedRepUserID
     * @return response
     */
    Response deleteMultipleBusinessUsers(String headerString, String accountID, String accountOwnerUserID, String nominatedRepUserID,
                                         String nominatedRepUserID2) {

        def requestBody = AUPayloads.getMultiUserDeletePayload(accountID, accountOwnerUserID, nominatedRepUserID,
                nominatedRepUserID2)

        return AURestAsRequestBuilder.buildRequest()
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, AUConstants.BASIC_HEADER_KEY + Base64.encoder.encodeToString(
                        headerString.getBytes(Charset.forName("UTF-8"))))
                .contentType(AUConstants.CONTENT_TYPE_APPLICATION_JSON)
                .body(requestBody)
                .baseUri(getAuConfiguration().getServerAuthorisationServerURL())
                .delete("${AUConstants.CONSENT_STATUS_AU_ENDPOINT}${AUConstants.UPDATE_BUSINESS_USER}")
    }

    /**
     * Get the Business User Permissions of a particular user.
     * @param userId
     * @param accountId
     * @return permission
     */
    Response getStakeholderPermissions(String userId, String accountId) {

        return AURestAsRequestBuilder.buildBasicRequest()
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, AUConstants.BASIC_HEADER_KEY + " " +
                        Base64.encoder.encodeToString(
                                "${auConfiguration.getUserBasicAuthName()}:${auConfiguration.getUserBasicAuthPWD()}"
                                        .getBytes(Charset.forName("UTF-8"))))
                .contentType(AUConstants.CONTENT_TYPE_APPLICATION_JSON)
                .queryParam(AUConstants.QUERY_PARAM_USERID, userId)
                .queryParam(AUConstants.QUERY_PARAM_ACCID, accountId)
                .baseUri(getAuConfiguration().getServerAuthorisationServerURL())
                .get("${AUConstants.CONSENT_STATUS_AU_ENDPOINT}${AUConstants.BUSINESS_USER_PERMISSION}")
    }

    /**
     * Authorisation FLow UI Navigation Method.
     * @param authoriseUrl
     * @param profiles
     * @param isMultipleAccSelect
     * @return
     */
    def doAuthorisationFlowNavigation(String authoriseUrl, AUAccountProfile profiles = null,
                                      boolean isMultipleAccSelect = false) {

        automationResponse = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Select Profile and Accounts
                    selectProfileAndAccount(authWebDriver, profiles, isMultipleAccSelect)

                    //Click Confirm Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)

                    //Click Authorise Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)
                }
                .execute()

        return automationResponse
    }

    /**
     * Update Business Use rPermission With Incorrect Payload.
     * @param headerString basic auth header
     * @param accountID account id
     * @param accountOwnerUserID account owner id
     * @param nominatedRepUserID nominated rep id
     * @param permissionType permission type
     * @return response
     */
    Response updateBusinessUserPermissionWithIncorrectPayload(String headerString, String accountID, String accountOwnerUserID, String nominatedRepUserID,
                                                              String permissionType) {

        def requestBody = AUPayloads.getIncorrectNominationPayload(accountID, accountOwnerUserID, nominatedRepUserID, permissionType)

        return AURestAsRequestBuilder.buildBasicRequest()
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, AUConstants.BASIC_HEADER_KEY + " " +
                        Base64.encoder.encodeToString(
                                "${auConfiguration.getUserBasicAuthName()}:${auConfiguration.getUserBasicAuthPWD()}"
                                        .getBytes(Charset.forName("UTF-8"))))
                .contentType(AUConstants.CONTENT_TYPE_APPLICATION_JSON)
                .body(requestBody)
                .baseUri(auConfiguration.getServerAuthorisationServerURL())
                .put("${AUConstants.CONSENT_STATUS_AU_ENDPOINT}${AUConstants.UPDATE_BUSINESS_USER}")
    }

    /**
     * Delete Single Business User Nomination with incorrect payload.
     * @param headerString
     * @param accountID
     * @param accountOwnerUserID
     * @param nominatedRepUserID
     * @return response
     */
    Response deleteBusinessUserWithIncorrectPayload(String headerString, String accountID, String accountOwnerUserID, String nominatedRepUserID) {

        def requestBody = AUPayloads.getIncorrectUserDeletePayload(accountID, accountOwnerUserID, nominatedRepUserID)

        return AURestAsRequestBuilder.buildRequest()
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, AUConstants.BASIC_HEADER_KEY + " " +
                        Base64.encoder.encodeToString(
                                "${auConfiguration.getUserBasicAuthName()}:${auConfiguration.getUserBasicAuthPWD()}"
                                        .getBytes(Charset.forName("UTF-8"))))
                .contentType(AUConstants.CONTENT_TYPE_APPLICATION_JSON)
                .body(requestBody)
                .baseUri(getAuConfiguration().getServerAuthorisationServerURL())
                .delete("${AUConstants.CONSENT_STATUS_AU_ENDPOINT}${AUConstants.UPDATE_BUSINESS_USER}")
    }

    /**
     * Authorisation Flow Navigation Method for Joint Accounts.
     * @param authoriseUrl - Authorise URL
     * @param isSelectMultipleAccounts - Select Multiple Accounts
     * @return Automation Response
     */
    def doAuthorisationFlowForJointAccounts(List<AUAccountScope> scopes, URI requestUri,
                                            String clientId = null, boolean isSelectMultipleAccounts = false) {

        if (clientId != null) {
            authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri, clientId)
                    .toURI().toString()
        } else {
            authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri)
                    .toURI().toString()
        }

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

                        //Select Joint Account 1
                        consentedAccount = authWebDriver.getElementAttribute(AUPageObjects.JOINT_ACCOUNT_XPATH,
                                AUPageObjects.VALUE)
                        authWebDriver.clickButtonXpath(AUPageObjects.JOINT_ACCOUNT_XPATH)

                        if(isSelectMultipleAccounts) {
                            //Select Joint Account 2
                            secondConsentedAccount = authWebDriver.getElementAttribute(AUPageObjects.ALT_JOINT_ACCOUNT_XPATH,
                                    AUPageObjects.VALUE)
                            authWebDriver.clickButtonXpath(AUPageObjects.ALT_JOINT_ACCOUNT_XPATH)
                        }
                    }
                    //If Profile Selection Disabled
                    else {
                        //Select Account 1
                        consentedAccount = authWebDriver.getElementAttribute(AUPageObjects.JOINT_ACCOUNT_XPATH,
                                AUPageObjects.VALUE)
                        authWebDriver.clickButtonXpath(AUPageObjects.JOINT_ACCOUNT_XPATH)

                        if (isSelectMultipleAccounts) {
                            //Select Account 2
                            secondConsentedAccount = authWebDriver.getElementAttribute(AUPageObjects.ALT_JOINT_ACCOUNT_XPATH,
                                    AUPageObjects.VALUE)
                            authWebDriver.clickButtonXpath(AUPageObjects.ALT_JOINT_ACCOUNT_XPATH)
                        }
                    }

                    //Click Confirm Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)

                    //Click Authorise Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)
                }
                .execute()

        return automationResponse
    }

    /**
     * Joint Accounts Consent Authorization method
     * @param clientId - Client Id
     * @param isSelectMultipleAccounts - true/false
     */
    def doJointAccountConsentAuthorisation(String clientId = null, boolean isSelectMultipleAccounts = true) {

        def response

        if (clientId == null) {
            response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                    true, "")
            requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
            automationResponse = doAuthorisationFlowForJointAccounts(scopes, requestUri.toURI(),
                    null, isSelectMultipleAccounts)
        } else {
            response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                    true, "", clientId)
            requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
            automationResponse = doAuthorisationFlowForJointAccounts(scopes, requestUri.toURI(),
                    clientId, isSelectMultipleAccounts)
        }
        return automationResponse
    }

    /**
     * Update Disclosure Options Mgt Service Status.
     * @param headerString - Header String
     * @param jointAccountIdList - Joint Account Id List
     * @param statusList - Status List
     * @return response.
     */
    Response updateDisclosureOptionsMgtService(String headerString, Map<String, String> domsStatusMap) {

        def requestBody = AUPayloads.getDOMSStatusUpdatePayload(domsStatusMap)

        return AURestAsRequestBuilder.buildRequest()
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, AUConstants.BASIC_HEADER_KEY + " " +
                        Base64.encoder.encodeToString("${auConfiguration.getUserBasicAuthName()}:${auConfiguration.getUserBasicAuthPWD()}"
                                .getBytes(Charset.forName("UTF-8"))))
                .contentType(AUConstants.CONTENT_TYPE_APPLICATION_JSON)
                .body(requestBody)
                .baseUri(auConfiguration.getServerAuthorisationServerURL())
                .put("${AUConstants.CONSENT_STATUS_AU_ENDPOINT}${AUConstants.DISCLOSURE_OPTIONS_ENDPOINT}")
    }

    /**
     * Ceasing legal entity of secondary user
     * @param headerString
     * @param secondaryUserId - Secondary User ID
     * @param accountId - Secondary Account ID
     * @param legalEntityId - Legal Entity ID
     * @param legalEntityStatus - Sharing Status
     * @param isMultipleLegalEntity - Multiple Legal Entity
     * @param secondaryUserId2 - Secondary User ID (Pass only if isMultipleLegalEntity is true)
     * @param accountId2 - Secondary Account ID (Pass only if isMultipleLegalEntity is true)
     * @param legalEntityId2 - Legal Entity ID (Pass only if isMultipleLegalEntity is true)
     * @param legalEntityStatus2 - Sharing Status (Pass only if isMultipleLegalEntity is true)
     * @return response.
     */
    static Response updateLegalEntityStatus(String headerString, String accountId, String secondaryUserId,
                                            String legalEntityId, String legalEntityStatus,
                                            boolean isMultipleLegalEntity = false,
                                            String secondaryUserId2 = null, String accountId2 = null,
                                            String legalEntityId2 = null, String legalEntityStatus2 = null) {

        def requestBody = AUPayloads.getBlockLegalEntityPayload(secondaryUserId, accountId, legalEntityId,
                legalEntityStatus, isMultipleLegalEntity, secondaryUserId2, accountId2, legalEntityId2, legalEntityStatus2)

        Response secondUserUpdateResponse = AURestAsRequestBuilder.buildRequest()
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, AUConstants.BASIC_HEADER_KEY + " " +
                        Base64.encoder.encodeToString(
                                "${auConfiguration.getUserBasicAuthName()}:${auConfiguration.getUserBasicAuthPWD()}"
                                        .getBytes(Charset.forName("UTF-8"))))
                .contentType(AUConstants.CONTENT_TYPE_APPLICATION_JSON)
                .body(requestBody)
                .baseUri(auConfiguration.getServerAuthorisationServerURL())
                .put("${AUConstants.CONSENT_STATUS_AU_ENDPOINT}${AUConstants.UPDATE_LEGAL_ENTITY_SHARING_STATUS}")

        return secondUserUpdateResponse
    }

    /**
     * Update Secondary User Instruction Permission.
     * @param headerString - Header String
     * @param secondaryAccId - Secondary Account Id
     * @param userId - User Id
     * @param secondaryAccountInstructionStatus - Secondary Account Instruction Status
     * @return response.
     */
    static Response updateSecondaryUserInstructionPermission(String secondaryAccId, String userId,
                                                             String secondaryAccountInstructionStatus,
                                                             boolean otherAccountsAvailability = true) {

        def requestBody = AUPayloads.getSecondaryUserInstructionPermissionPayload(secondaryAccId, userId,
                secondaryAccountInstructionStatus, otherAccountsAvailability)

        Response secondUserUpdateResponse = AURestAsRequestBuilder.buildRequest()
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, AUConstants.BASIC_HEADER_KEY + " " +
                        Base64.encoder.encodeToString(
                                "${auConfiguration.getUserBasicAuthName()}:${auConfiguration.getUserBasicAuthPWD()}"
                                        .getBytes(Charset.forName("UTF-8"))))
                .contentType(AUConstants.CONTENT_TYPE_APPLICATION_JSON)
                .body(requestBody)
                .baseUri(auConfiguration.getServerAuthorisationServerURL())
                .put("${AUConstants.CONSENT_STATUS_AU_ENDPOINT}${AUConstants.SECONDARY_ACCOUNT_ENDPOINT}")

        return secondUserUpdateResponse
    }

    /**
     * Get Legal Entity IDs of User.
     * @param userID - User ID
     * @return response.
     */
    static Response getLegalEntityIds(String userID) {

        userID = "amy@gold.com@carbon.super"

        return AURestAsRequestBuilder.buildRequest()
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, AUConstants.BASIC_HEADER_KEY + " " +
                        Base64.encoder.encodeToString("${auConfiguration.getUserBasicAuthName()}:${auConfiguration.getUserBasicAuthPWD()}"
                                .getBytes(Charset.forName("UTF-8"))))
                .contentType(AUConstants.CONTENT_TYPE_APPLICATION_JSON)
                .baseUri(auConfiguration.getServerAuthorisationServerURL())
                .get("${AUConstants.CONSENT_STATUS_AU_ENDPOINT}${AUConstants.LEGAL_ENTITY_LIST_ENDPOINT}/${userID}")
    }

    /**
     * Get Legal Entity IDs of User.
     * @param legalEntityList - Legal Entity List
     * @param userId - User ID
     * @param accountId - Account ID
     * @param legalEntityId - Legal Entity ID
     * @return response.
     */
    static String getSharingStatusOfUserAccount(String legalEntityList, String userId, String accountId, String legalEntityId) {

        // Create a Gson instance
        Gson gson = new Gson()

        // Parse the payload into a JsonObject
        JsonObject legalEntityListObject = gson.fromJson(legalEntityList, JsonObject.class)

        // Retrieve the SecondaryUsers array
        JsonArray secondaryUsersArray = legalEntityListObject.getAsJsonArray(AUConstants.PAYLOAD_SECONDARY_USERS)

        // Iterate through the secondary users
        for (JsonElement secondaryUserElement : secondaryUsersArray) {
            JsonObject secondaryUserObj = secondaryUserElement.getAsJsonObject()

            // Check if the user ID matches
            if (secondaryUserObj.get(AUConstants.SECONDARY_USERS_USERID).getAsString().equals(userId)) {

                // Get the legal entity details array for the given user
                JsonArray accountsArray = secondaryUserObj.getAsJsonArray("accounts")

                // Iterate through the legal entity details
                for (JsonElement legalEntityDetailsElement : accountsArray) {
                    JsonObject legalEntityDetailsObj = legalEntityDetailsElement.getAsJsonObject()

                    // Check if the account ID matches
                    if (legalEntityDetailsObj.get(AUConstants.PAYLOAD_PARAM_ACCOUNT_ID).getAsString().equals(accountId)) {

                        // Get the legal entities array for the given account
                        JsonArray legalEntitiesArray = legalEntityDetailsObj.getAsJsonArray(AUConstants.LEGAL_ENTITIES)

                        // Iterate through the legal entities
                        for (JsonElement legalEntityElement : legalEntitiesArray) {
                            JsonObject legalEntityObj = legalEntityElement.getAsJsonObject()

                            // Check if the legal entity ID matches
                            if (legalEntityObj.get(AUConstants.LEGAL_ENTITY_ID_MAP).getAsString().equals(legalEntityId)) {
                                return legalEntityObj.get(AUConstants.SHARING_STATUS).getAsString()
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Verify Scope of Token Response.
     * @param scopesString - scope list
     * @param eliminatedScope - scope to be eliminated
     */
    void verifyScopes(String scopesString, String eliminatedScope = null) {
        if (eliminatedScope != null) {
            Assert.assertFalse(scopesString.contains(eliminatedScope))
        } else {
            for (AUAccountScope scope : scopes) {
                Assert.assertTrue(scopesString.contains(scope.getScopeString()))
            }
        }
    }

    /**
     * Method for get user access token response
     * @return
     */
    AccessTokenResponse getUserAccessTokenFormRefreshToken(RefreshToken refreshToken) {
        try {
            return AURequestBuilder.getUserTokenFromRefreshToken(refreshToken)
        }
        catch (Exception e) {
            log.error(e)
        }
    }

    Response doRevokeCdrArrangement(String clientId, String cdrArrangementId){

        generator = new AUJWTGenerator()
        String assertionString = generator.getClientAssertionJwtWithoutIAT(clientId)

        def bodyContent = [(AUConstants.CLIENT_ID_KEY): (clientId),
                           (AUConstants.CLIENT_ASSERTION_TYPE_KEY): (AUConstants.CLIENT_ASSERTION_TYPE),
                           (AUConstants.CLIENT_ASSERTION_KEY)     : assertionString,
                           (AUConstants.CDR_ARRANGEMENT_ID)       : cdrArrangementId]

        revocationResponse = AURestAsRequestBuilder.buildRequest()
                .contentType(AUConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .formParams(bodyContent)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_CDR_ARRANGEMENT))
                .post("${AUConstants.CDR_ARRANGEMENT_ENDPOINT}")

        return revocationResponse
    }

    /**
     * Revoke CDR Arrangement Request.
     * @param clientId
     * @param cdrArrangementId
     * @param audience
     * @return response of revoke request.
     */
    Response doRevokeCdrArrangement(String clientId, String cdrArrangementId, String audience){

        generator = new AUJWTGenerator()
        String assertionString = generator.getClientAssertionJwt(clientId, audience)

        def bodyContent = [(AUConstants.CLIENT_ID_KEY): (clientId),
                           (AUConstants.CLIENT_ASSERTION_TYPE_KEY): (AUConstants.CLIENT_ASSERTION_TYPE),
                           (AUConstants.CLIENT_ASSERTION_KEY)     : assertionString,
                           (AUConstants.CDR_ARRANGEMENT_ID)       : cdrArrangementId]

        revocationResponse = AURestAsRequestBuilder.buildRequest()
                .contentType(AUConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .formParams(bodyContent)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_CDR_ARRANGEMENT))
                .post("${AUConstants.CDR_ARRANGEMENT_ENDPOINT}")

        return revocationResponse
    }

    /**
     * Method to get consent status.
     * @param headerString
     * @param consentId
     * @return
     */
    Response getConsentStatus(String clientHeader, String consentId) {

        return AURestAsRequestBuilder.buildRequest()
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, "Basic " + clientHeader)
                .baseUri(auConfiguration.getServerAuthorisationServerURL())
                .get("${AUConstants.CONSENT_STATUS_ENDPOINT}${AUConstants.STATUS_PATH}?${consentId}")
    }

    /**
     * Consent authorization method with Request URI and Response Mode
     * @param scopes
     * @param requestUri
     * @param clientId
     * @param profiles
     */
    void doConsentAuthorisationViaRequestUri(List<AUAccountScope> scopes, URI requestUri, ResponseMode responseMode,
                                             ResponseType responseType = ResponseType.CODE_IDTOKEN,
                                             String clientId = null, AUAccountProfile profiles = null,
                                             boolean isStateParamPresent = true) {

        if (clientId != null) {
            authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri, clientId,
                    isStateParamPresent).toURI().toString()
        } else {
            authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri, "",
                    isStateParamPresent).toURI().toString()
        }

        //UI Flow Navigation
        automationResponse = doAuthorisationFlowNavigation(authoriseUrl, profiles, true)
    }

    /**
     * Authorisation FLow UI Navigation Method for Error Scenarios.
     * @param authoriseUrl authorisation request URL
     * @return automationResponse
     */
    def doAuthorisationErrorFlow(String authoriseUrl) {

        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new NavigationAutomationStep(authoriseUrl, 10))
                .execute()

        // Get Code From URL
        return automation
    }

    /**
     * Secondary User Auth Flow Without Account Selection.
     * @param scopes - scope list
     * @param requestUri - request URI
     * @param clientId - client ID
     */
    void doSecondaryUserAuthFlowWithoutAccountSelection(List<AUAccountScope> scopes, URI requestUri, String clientId = null) {

        if (clientId != null) {
            authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri, clientId)
                    .toURI().toString()
        } else {
            authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri)
                    .toURI().toString()
        }

        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Click Confirm Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)
                }
                .execute()

        // Get Code From URL
        authorisationCode = AUTestUtil.getCodeFromJwtResponse(automation.currentUrl.get())
    }

    /**
     * Get Metrics Response.
     * @param period query param to filter period (CURRENT,HISTORIC,ALL)
     * @return metrics response
     */
    static Response getMetrics(String period = "ALL"){

        AUJWTGenerator generator = new AUJWTGenerator()
        String assertionString = generator.getClientAssertionJwt(AUConstants.ADMIN_API_ISSUER,
                AUConstants.ADMIN_API_AUDIENCE)

        def scheduler = Executors.newSingleThreadScheduledExecutor()
        def metricsResponse
        def latch = new CountDownLatch(1) // Latch to wait for the scheduled task to complete

        scheduler.schedule({
            try {
                metricsResponse = AURequestBuilder.buildBasicRequest(assertionString, AUConstants.X_V_HEADER_METRICS)
                        .contentType(ContentType.JSON)
                        .header(AUConstants.X_MIN_HEADER, AUConstants.X_V_MIN_HEADER_METRICS)
                        .queryParam(AUConstants.PERIOD, period)
                        .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ADMIN))
                        .get("${AUConstants.CDS_ADMIN_PATH}${AUConstants.ADMIN_METRICS}")
            } finally {
                latch.countDown() // Signal that the task is complete
                scheduler.shutdown() // Shutdown the scheduler
            }
        }, 30, TimeUnit.SECONDS)

        // Wait for the scheduled task to complete
        latch.await()

        return metricsResponse
    }

    /**
     * Get the Metrics Response and assign metrics to variables.
     * @param metricsResponse
     */
    void getInitialMetricsResponse(Response metricsResponse) {

        //Invocation
        unauthenticatedCurrentDay = AUTestUtil.parseResponseBody(metricsResponse,
                AUConstants.INVOCATION_UNAUTHENTICATED_CURRENTDAY).toInteger()
        highPriorityCurrentDay = AUTestUtil.parseResponseBody(metricsResponse,
                AUConstants.INVOCATION_HIGHPRIORITY_CURRENTDAY).toInteger()
        lowPriorityCurrentDay = AUTestUtil.parseResponseBody(metricsResponse,
                AUConstants.INVOCATION_LOWPRIORITY_CURRENTDAY).toInteger()
        largePayloadCurrentDay = AUTestUtil.parseResponseBody(metricsResponse,
                AUConstants.INVOCATION_LARGEPAYLOAD_CURRENTDAY).toInteger()
        unattendedCurrentDay = AUTestUtil.parseResponseBody(metricsResponse,
                AUConstants.INVOCATION_UNATTENDED_CURRENTDAY).toInteger()

        //Authorisations
        activeAuthIndividual = AUTestUtil.parseResponseBody(metricsResponse,
                AUConstants.ACTIVE_AUTHORIZATION_INDIVIDUAL).toInteger()
        activeAuthNonIndividual = AUTestUtil.parseResponseBody(metricsResponse,
                AUConstants.ACTIVE_AUTHORIZATION_NONINDIVIDUAL).toInteger()
        newAuthCurrentDayOngoingIndividual = AUTestUtil.parseResponseBody(metricsResponse,
                AUConstants.NEWAUTH_CURRENTDAY_ONGOING_INDIVIDUAL).toInteger()
        newAuthCurrentDayOngoingNonIndividual = AUTestUtil.parseResponseBody(metricsResponse,
                AUConstants.NEWAUTH_CURRENTDAY_ONGOING_NONINDIVIDUAL).toInteger()
        newAuthCurrentDayOnceOffIndividual = AUTestUtil.parseResponseBody(metricsResponse,
                AUConstants.NEWAUTH_CURRENTDAY_ONCEOFF_INDIVIDUAL).toInteger()
        newAuthCurrentDayOnceOffNonIndividual = AUTestUtil.parseResponseBody(metricsResponse,
                AUConstants.NEWAUTH_CURRENTDAY_ONCEOFF_NONINDIVIDUAL).toInteger()
        revokedCurrentDayIndividual = AUTestUtil.parseResponseBody(metricsResponse,
                AUConstants.REVOKED_CURRENTDAY_INDIVIDUAL).toInteger()
        revokedCurrentDayNonIndividual = AUTestUtil.parseResponseBody(metricsResponse,
                AUConstants.REVOKED_CURRENTDAY_NONINDIVIDUAL).toInteger()
        amendedCurrentDayIndividual = AUTestUtil.parseResponseBody(metricsResponse,
                AUConstants.AMENDED_CURRENTDAY_INDIVIDUAL).toInteger()
        amendedCurrentDayNonIndividual = AUTestUtil.parseResponseBody(metricsResponse,
                AUConstants.AMENDED_CURRENTDAY_NONINDIVIDUAL).toInteger()
        expiredCurrentDayIndividual = AUTestUtil.parseResponseBody(metricsResponse,
                AUConstants.EXPIRED_CURRENTDAY_INDIVIDUAL).toInteger()
        expiredCurrentDayNonIndividual = AUTestUtil.parseResponseBody(metricsResponse,
                AUConstants.EXPIRED_CURRENTDAY_NONINDIVIDUAL).toInteger()
        abandonedCurrentDay = AUTestUtil.parseResponseBody(metricsResponse,
                AUConstants.ABANDON_CURRENTDAY).toInteger()
        abandonedPreIdentificationCurrentDay = AUTestUtil.parseResponseBody(metricsResponse,
                AUConstants.ABANDON_PREIDENTIFICATION_CURRENTDAY).toInteger()
        abandonedPreAuthenticationCurrentDay = AUTestUtil.parseResponseBody(metricsResponse,
                AUConstants.ABANDON_PREAUTHENTICATE_CURRENTDAY).toInteger()
        abandonedPreAccountSelectionCurrentDay = AUTestUtil.parseResponseBody(metricsResponse,
                AUConstants.ABANDON_PREACCSELECT_CURRENTDAY).toInteger()
        abandonedPreAuthorisationCurrentDay = AUTestUtil.parseResponseBody(metricsResponse,
                AUConstants.ABANDON_PREAUTH_CURRENTDAY).toInteger()
        abandonedRejectedCurrentDay = AUTestUtil.parseResponseBody(metricsResponse,
                AUConstants.ABANDON_REJECTED_CURRENTDAY).toInteger()
        abandonedFailedTokenExchangeCurrentDay = AUTestUtil.parseResponseBody(metricsResponse,
                AUConstants.ABANDON_FAILEDTOKEN_CURRENTDAY).toInteger()

        //Error Response
        unauthErrorCurrentDay = AUTestUtil.parseResponseBody(metricsResponse, AUConstants.ERROR_UNAUTH_CURRENTDAY)
        authErrorCurrentDay = AUTestUtil.parseResponseBody(metricsResponse, AUConstants.ERROR_AUTH_CURRENTDAY)
        aggErrorCurrentDay = AUTestUtil.parseResponseBody(metricsResponse, AUConstants.ERROR_AGGREGATE_CURRENTDAY)

        //Customer and Recipient Count
        customerCount = AUTestUtil.parseResponseBody(metricsResponse, AUConstants.DATA_CUSTOMER_COUNT).toInteger()
        recipientCount = AUTestUtil.parseResponseBody(metricsResponse, AUConstants.DATA_RECIPIENT_COUNT).toInteger()

        //Session Count
        sessionCount = AUTestUtil.parseResponseBody(metricsResponse, AUConstants.DATA_SESSION_COUNT_CURRENTDAY).toInteger()
    }

    /**
     * Asserting Metrics Authorisation Response.
     * @param metricsResponse
     */
    static void assertMetricsAuthorisationResponse(Response metricsResponse) {

        Assert.assertEquals(AUTestUtil.parseResponseBody(metricsResponse, AUConstants.ACTIVE_AUTHORIZATION_INDIVIDUAL),
                "${activeAuthIndividual}", "$AUConstants.ACTIVE_AUTHORIZATION_INDIVIDUAL count mismatch")
        Assert.assertEquals(AUTestUtil.parseResponseBody(metricsResponse, AUConstants.ACTIVE_AUTHORIZATION_NONINDIVIDUAL),
                "${activeAuthNonIndividual}", "$AUConstants.ACTIVE_AUTHORIZATION_NONINDIVIDUAL count mismatch")
        Assert.assertEquals(AUTestUtil.parseResponseBody(metricsResponse, AUConstants.NEWAUTH_CURRENTDAY_ONGOING_INDIVIDUAL),
                "${newAuthCurrentDayOngoingIndividual}",
                "$AUConstants.NEWAUTH_CURRENTDAY_ONGOING_INDIVIDUAL count mismatch")
        Assert.assertEquals(AUTestUtil.parseResponseBody(metricsResponse,
                AUConstants.NEWAUTH_CURRENTDAY_ONGOING_NONINDIVIDUAL), "${newAuthCurrentDayOngoingNonIndividual}",
                "$AUConstants.NEWAUTH_CURRENTDAY_ONGOING_NONINDIVIDUAL count mismatch")
        Assert.assertEquals(AUTestUtil.parseResponseBody(metricsResponse, AUConstants.NEWAUTH_CURRENTDAY_ONCEOFF_INDIVIDUAL),
                "${newAuthCurrentDayOnceOffIndividual}",
                "$AUConstants.NEWAUTH_CURRENTDAY_ONCEOFF_INDIVIDUAL count mismatch")
        Assert.assertEquals(AUTestUtil.parseResponseBody(metricsResponse,
                AUConstants.NEWAUTH_CURRENTDAY_ONCEOFF_NONINDIVIDUAL), "${newAuthCurrentDayOnceOffNonIndividual}",
                "$AUConstants.NEWAUTH_CURRENTDAY_ONCEOFF_NONINDIVIDUAL count mismatch")
        Assert.assertEquals(AUTestUtil.parseResponseBody(metricsResponse, AUConstants.REVOKED_CURRENTDAY_INDIVIDUAL),
                "${revokedCurrentDayIndividual}", "$AUConstants.REVOKED_CURRENTDAY_INDIVIDUAL count mismatch")
        Assert.assertEquals(AUTestUtil.parseResponseBody(metricsResponse, AUConstants.REVOKED_CURRENTDAY_NONINDIVIDUAL),
                "${revokedCurrentDayNonIndividual}",
                "$AUConstants.REVOKED_CURRENTDAY_NONINDIVIDUAL count mismatch")
        Assert.assertEquals(AUTestUtil.parseResponseBody(metricsResponse, AUConstants.AMENDED_CURRENTDAY_INDIVIDUAL),
                "${amendedCurrentDayIndividual}",
                "$AUConstants.AMENDED_CURRENTDAY_INDIVIDUAL count mismatch")
        Assert.assertEquals(AUTestUtil.parseResponseBody(metricsResponse, AUConstants.AMENDED_CURRENTDAY_NONINDIVIDUAL),
                "${amendedCurrentDayNonIndividual}",
                "$AUConstants.AMENDED_CURRENTDAY_NONINDIVIDUAL count mismatch")
        Assert.assertEquals(AUTestUtil.parseResponseBody(metricsResponse, AUConstants.EXPIRED_CURRENTDAY_INDIVIDUAL),
                "${expiredCurrentDayIndividual}", "$AUConstants.EXPIRED_CURRENTDAY_INDIVIDUAL count mismatch")
        Assert.assertEquals(AUTestUtil.parseResponseBody(metricsResponse, AUConstants.EXPIRED_CURRENTDAY_NONINDIVIDUAL),
                "${expiredCurrentDayNonIndividual}", "$AUConstants.EXPIRED_CURRENTDAY_NONINDIVIDUAL count mismatch")

        if (AUConstants.X_V_HEADER_METRICS == 4) {
            Assert.assertEquals(AUTestUtil.parseResponseBody(metricsResponse, AUConstants.ABANDON_CURRENTDAY),
                    "${abandonedCurrentDay}", "$AUConstants.ABANDON_CURRENTDAY count mismatch")
        } else {
            Assert.assertEquals(AUTestUtil.parseResponseBody(metricsResponse, AUConstants.ABANDON_CURRENTDAY),
                    "${abandonedCurrentDay}", "$AUConstants.ABANDON_CURRENTDAY count mismatch")
            Assert.assertEquals(AUTestUtil.parseResponseBody(metricsResponse,
                    AUConstants.ABANDON_PREIDENTIFICATION_CURRENTDAY), "${abandonedPreIdentificationCurrentDay}",
                    "$AUConstants.ABANDON_PREIDENTIFICATION_CURRENTDAY count mismatch")
            Assert.assertEquals(AUTestUtil.parseResponseBody(metricsResponse,
                    AUConstants.ABANDON_PREIDENTIFICATION_CURRENTDAY), "${abandonedPreIdentificationCurrentDay}",
                    "$AUConstants.ABANDON_PREIDENTIFICATION_CURRENTDAY count mismatch")
            Assert.assertEquals(AUTestUtil.parseResponseBody(metricsResponse,
                    AUConstants.ABANDON_PREAUTHENTICATE_CURRENTDAY), "${abandonedPreAuthenticationCurrentDay}",
                    "$AUConstants.ABANDON_PREAUTHENTICATE_CURRENTDAY count mismatch")
            Assert.assertEquals(AUTestUtil.parseResponseBody(metricsResponse,
                    AUConstants.ABANDON_PREACCSELECT_CURRENTDAY), "${abandonedPreAccountSelectionCurrentDay}",
                    "$AUConstants.ABANDON_PREACCSELECT_CURRENTDAY count mismatch")
            Assert.assertEquals(AUTestUtil.parseResponseBody(metricsResponse,
                    AUConstants.ABANDON_PREAUTH_CURRENTDAY), "${abandonedPreAuthorisationCurrentDay}",
                    "$AUConstants.ABANDON_PREAUTH_CURRENTDAY count mismatch")
            Assert.assertEquals(AUTestUtil.parseResponseBody(metricsResponse,
                    AUConstants.ABANDON_REJECTED_CURRENTDAY), "${abandonedRejectedCurrentDay}",
                    "$AUConstants.ABANDON_REJECTED_CURRENTDAY count mismatch")
            Assert.assertEquals(AUTestUtil.parseResponseBody(metricsResponse,
                    AUConstants.ABANDON_FAILEDTOKEN_CURRENTDAY), "${abandonedFailedTokenExchangeCurrentDay}",
                    "$AUConstants.ABANDON_FAILEDTOKEN_CURRENTDAY count mismatch")
        }
    }


    /**
     * Consent Amendment Authorisation
     * @param scopes
     * @param cdrArrangementId
     * @param sharingDuration
     * @param clientId
     * @return auth code
     */
    String doConsentAmendmentAuthorisation(List<AUAccountScope> scopes, String cdrArrangementId, long sharingDuration,
                                           String clientId = null) {

        if (clientId == null) {
            response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, sharingDuration,
                    true, cdrArrangementId)
            requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
            authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI())
                    .toURI().toString()
        } else {
            response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, sharingDuration,
                    true, cdrArrangementId, clientId)
            requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
            authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), clientId)
                    .toURI().toString()
        }

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
        return AUTestUtil.getCodeFromJwtResponse(automation.currentUrl.get())
    }

    /**
     * Authorise Consent Without Closing Browser.
     * @param scopes
     * @param requestUri
     * @return authorisation code
     */
    String authoriseConsentWithoutClosingBrowser(List<AUAccountScope> scopes, URI requestUri,
                                                 String clientId = null, boolean isMultipleAccSelect = false,
                                                 AUAccountProfile profiles = null) {

        if (clientId != null) {
            authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri, clientId)
                    .toURI().toString()
        } else {
            authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri)
                    .toURI().toString()
        }

        //UI Flow Navigation
        automationResponse = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Select Profile and Accounts
                    selectProfileAndAccount(authWebDriver, profiles, isMultipleAccSelect)

                    //Click Confirm Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)

                    //Click Authorise Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)
                }
                .execute(false)

        // Get Code From URL
        def authorisationCode = AUTestUtil.getCodeFromJwtResponse(automationResponse.currentUrl.get())
        return authorisationCode
    }

    /**
     * Get Errors Count in the Metrics Response.
     * @param errorsResponse
     * @param resourceType
     * @param modifiedErrorCode
     * @param modifiedValue
     * @return updatedKeyValuePairsString
     */
    static String getErrorsMetrics(String metricsResponse, int modifiedErrorCode) {

        // Parse the input string into a Map
        Map<String, Integer> errorMap = parseMetricsString(metricsResponse)

        updateOrAddErrorCode(errorMap, modifiedErrorCode.toString(), 1)

        // Generate the updated metrics string
        String updatedMetricsString = generateMetricsString(errorMap)
        System.out.println("Updated JSON Response:")
        System.out.println(updatedMetricsString)

        updatedMetricsString
    }

    /**
     * Parse the metrics string into a map.
     * @param metricsInput
     * @return
     */
    private static Map<String, Integer> parseMetricsString(String metricsInput) {

        Map<String, Integer> errorMap = new HashMap<>()

        try {
            // Check for null or empty input
            if (metricsInput == null || metricsInput.isEmpty()) {
                System.err.println("Metrics input is null or empty. Returning empty array.");
                return new HashMap<>(); // Return an empty map
            }

            // Remove square brackets and split entries
            metricsInput = metricsInput.replaceAll("[\\[\\]]", "");
            String[] entries = metricsInput.split(", ");

            for (String entry : entries) {
                String[] parts = entry.split(":");
                if (parts.length != 2) {
                    System.err.println("Invalid entry format: " + entry);
                    continue; // Skip invalid entries
                }
                errorMap.put(parts[0], Integer.parseInt(parts[1]));
            }
        } catch (Exception e) {
            System.err.println("Error parsing metrics string: " + e.getMessage());
            return new HashMap<>(); // Return an empty map in case of an error
        }

        return errorMap;
    }

    /**
     * Update the error count in the map.
     * @param errorMap
     * @param errorCode
     * @param incrementBy
     */
    private static void updateOrAddErrorCode(Map<String, Integer> errorMap, String errorCode, int incrementBy) {

        if(!errorCode.equalsIgnoreCase(AUConstants.STATUS_CODE_405.toString())) {
            // Add the error code with the default value of 1 if it doesn't exist, or update it
            errorMap.put(errorCode, errorMap.getOrDefault(errorCode, 0) + incrementBy)
        }

    }

    /**
     * Sort the map by key in ascending order and format it back into a string.
     * @param errorMap
     * @return
     */
    private static String generateMetricsString(Map<String, Integer> errorMap) {
        // Sort the map by keys (error codes) in ascending order and format it back into a string
        return errorMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + ":" + entry.getValue())
                .collect(Collectors.joining(", ", "[", "]"))
    }


    /**
     * Assert Metrics Error Response.
     * @param metricsResponse
     */
    void assertMetricsErrorResponse(Response metricsResponse) {

        Map<String, Integer> actualUnauthErrors = parseMetricsString(AUTestUtil.parseResponseBody(metricsResponse, AUConstants.ERROR_UNAUTH_CURRENTDAY));
        Map<String, Integer> actualAuthErrors = parseMetricsString(AUTestUtil.parseResponseBody(metricsResponse, AUConstants.ERROR_AUTH_CURRENTDAY));
        Map<String, Integer> actualAggErrors = parseMetricsString(AUTestUtil.parseResponseBody(metricsResponse, AUConstants.ERROR_AGGREGATE_CURRENTDAY));

        // Parse the expected response into a map
        Map<String, Integer> expectedUnauthErrors = parseMetricsString(unauthErrorCurrentDay);
        Map<String, Integer> expectedAuthErrors = parseMetricsString(authErrorCurrentDay);
        Map<String, Integer> expectedAggErrors = parseMetricsString(aggErrorCurrentDay);

        // Assert that the maps are equal
        Assert.assertEquals(actualUnauthErrors, expectedUnauthErrors, "Unauthenticated errors count mismatch");
        Assert.assertEquals(actualAuthErrors, expectedAuthErrors, "Authenticated errors count mismatch");
        Assert.assertEquals(actualAggErrors, expectedAggErrors, "Aggregate errors count mismatch");
    }

    /**
     * Calculate Performance, Average Response and Average TPS Metrics for each tier
     */
    void calculateTierBasedMetrics() {

        //Calculate Performance Metrics
        performanceMetrics = metricsPerformanceCalculation()

        //Calculate Total Response Time for each tier
        avgResponseMetrics = calculateTierWiseTotalResponseTime()

        //Calculate Total Resource count for average TPS
        totalResources = calculateTotalResourceCount()
    }

    /**
     * Calculate Performance Metrics for Defined Tier
     * @param tier
     * @return metrics value
     */
    int[] metricsPerformanceCalculation() {

        String tier
        int responseTime
        int[] withinThreshold = new int[5] // Array to store counts for each tier
        totalInvocationsPerf = 0
        totalInvocationsUnAuthPerf = 0
        totalInvocationsHighPerf = 0
        totalInvocationsLowPerf = 0
        totalInvocationsUnattendedPerf = 0
        totalInvocationsLargePayPerf = 0

        // Get the current UTC time
        LocalDateTime utcTime = LocalDateTime.now(ZoneOffset.UTC)

        // Convert UTC time to GMT time
        LocalDateTime gmtTime = utcTime.atOffset(ZoneOffset.UTC).atZoneSameInstant(ZoneOffset.ofHours(0)).toLocalDateTime()

        //Get the current hour
        currentTime = gmtTime.getHour().toInteger()

        //Get Start and End Time
        long startTimeOfDay = today.atTime(currentTime, 00, 00).toEpochSecond(ZoneOffset.UTC)
        long endTimeOfDay =  today.atTime(currentTime, 59, 59).toEpochSecond(ZoneOffset.UTC)

        // Execute SELECT query and get results as an array
        def query = SqlQuery.retrieveRecordsWithinSpecifiedPeriod(startTimeOfDay, endTimeOfDay)
        Object[][] results = DbConnection.executeSelectQuery(AUConstants.REPORTING_DBNAME, query)
        totalInvocationsPerf = results.length

        // Process and check the response time within threshold
        if (results != null) {
            for (Object[] row : results) {
                responseTime = row[0]

                //Do not count the request as a invocation if the status code is 405
                if(row[2]==AUConstants.STATUS_CODE_405) {
                    continue
                }

                tier = AUTestUtil.getPriorityTier(row[1], row[3])

                if(!row[1].equals("/par")) {

                    switch (tier) {
                        case AUConstants.UNAUTHENTICATED:
                            totalInvocationsUnAuthPerf = totalInvocationsUnAuthPerf + 1
                            if (responseTime <= 1500){
                                withinThreshold[0]++
                            }
                            break
                        case AUConstants.HIGH_PRIORITY:
                            totalInvocationsHighPerf = totalInvocationsHighPerf + 1
                            if (responseTime <= 1000){
                                withinThreshold[1]++
                            }
                            break
                        case AUConstants.LOW_PRIORITY:
                            totalInvocationsLowPerf = totalInvocationsLowPerf + 1
                            if (responseTime <= 1500){
                                withinThreshold[2]++
                            }
                            break
                        case AUConstants.UNATTENDED:
                            totalInvocationsUnattendedPerf = totalInvocationsUnattendedPerf + 1
                            if (responseTime <= 4000){
                                withinThreshold[3]++
                            }
                            break
                        case AUConstants.LARGE_PAYLOAD:
                            totalInvocationsLargePayPerf = totalInvocationsLargePayPerf + 1
                            if (responseTime <= 6000){
                                withinThreshold[4]++
                            }
                            break
                        default:
                            throw new IllegalStateException("Unexpected value: " + tier + " for " + row[1])
                    }
                }
            }
        }

        return withinThreshold
    }

    /**
     * Assertions for Tier Based Metrics - Performance, Average Response and Average TPS.
     * @param metricsResponse
     */
    void assertTierBasedMetrics(Response metricsResponse) {

        //Asserting the Invocations
        assertMetricsInvocationResponse(metricsResponse)

        //Asserting the Performance
        assertPerformanceMetricsResponse(metricsResponse, performanceMetrics)

        //Asserting Average Response Time
        assertAvgResponseMetricsResponse(metricsResponse, avgResponseMetrics)

        //Asserting Average TPS
        assertAvgTpsMetricsResponse(metricsResponse, totalResources)
    }

    /**
     * Asserting Metrics Invocation Response.
     * @param metricsResponse
     */
    static void assertMetricsInvocationResponse(Response metricsResponse) {

        SoftAssert softAssert = new SoftAssert()

        softAssert.assertEquals(AUTestUtil.parseResponseBody(metricsResponse, AUConstants.INVOCATION_UNAUTHENTICATED_CURRENTDAY),
                "${unauthenticatedCurrentDay}", "$AUConstants.INVOCATION_UNAUTHENTICATED_CURRENTDAY count mismatch")
        softAssert.assertEquals(AUTestUtil.parseResponseBody(metricsResponse, AUConstants.INVOCATION_HIGHPRIORITY_CURRENTDAY),
                "${highPriorityCurrentDay}", "$AUConstants.INVOCATION_HIGHPRIORITY_CURRENTDAY count mismatch")
        softAssert.assertEquals(AUTestUtil.parseResponseBody(metricsResponse, AUConstants.INVOCATION_LOWPRIORITY_CURRENTDAY),
                "${lowPriorityCurrentDay}", "$AUConstants.INVOCATION_LOWPRIORITY_CURRENTDAY count mismatch")
        softAssert.assertEquals(AUTestUtil.parseResponseBody(metricsResponse, AUConstants.INVOCATION_UNATTENDED_CURRENTDAY),
                "${unattendedCurrentDay}", "$AUConstants.INVOCATION_UNATTENDED_CURRENTDAY count mismatch")
        softAssert.assertEquals(AUTestUtil.parseResponseBody(metricsResponse, AUConstants.INVOCATION_LARGEPAYLOAD_CURRENTDAY),
                "${largePayloadCurrentDay}", "$AUConstants.INVOCATION_LARGEPAYLOAD_CURRENTDAY count mismatch")

        softAssert.assertAll()
    }

    /**
     * Assert Performance Metrics Response
     * @param metricsResponse
     * @param withinThreshold
     * @param arrayIndex
     */
    void assertPerformanceMetricsResponse(Response metricsResponse, int[] withinThreshold) {

        SoftAssert softAssert = new SoftAssert()

        //Unauthenticated Performance Value
        String unauthPerfResponse = AUTestUtil.parseResponseBody(metricsResponse, AUConstants.PERFORMANCE_UNAUTH_CURRENTDAY)
        double unauthPerf = roundUpThreeDecimals(Double.parseDouble(unauthPerfResponse.substring(unauthPerfResponse
                .lastIndexOf(",") + 1, unauthPerfResponse.length() - 1).trim()))

        softAssert.assertEquals(unauthPerf, calculatePerformance(withinThreshold[0], totalInvocationsUnAuthPerf),
                "$AUConstants.PERFORMANCE_UNAUTH_CURRENTDAY count mismatch")

        //High Priority Performance Value
        String highPerfResponse = AUTestUtil.parseResponseBody(metricsResponse, AUConstants.PERFORMANCE_HIGH_CURRENTDAY)
        double highPerf = roundUpThreeDecimals(Double.parseDouble(highPerfResponse.substring(highPerfResponse
                .lastIndexOf(",") + 1, highPerfResponse.length() - 1).trim()))

        softAssert.assertEquals(highPerf, calculatePerformance(withinThreshold[1], totalInvocationsHighPerf),
                "$AUConstants.PERFORMANCE_HIGH_CURRENTDAY count mismatch")

        //Low Priority Performance Value
        String lowPerfResponse = AUTestUtil.parseResponseBody(metricsResponse, AUConstants.PERFORMANCE_LOW_CURRENTDAY)
        double lowPerf = roundUpThreeDecimals(Double.parseDouble(lowPerfResponse.substring(lowPerfResponse
                .lastIndexOf(",") + 1, lowPerfResponse.length() - 1).trim()))

        softAssert.assertEquals(lowPerf, calculatePerformance(withinThreshold[2], totalInvocationsLowPerf),
                "$AUConstants.PERFORMANCE_LOW_CURRENTDAY count mismatch")

        //Unattended Performance Value
        String unattendedPerfResponse = AUTestUtil.parseResponseBody(metricsResponse, AUConstants.PERFORMANCE_UNATTENDED_CURRENTDAY)
        double unattendedPerf = roundUpThreeDecimals(Double.parseDouble(unattendedPerfResponse.substring(unattendedPerfResponse
                .lastIndexOf(",") + 1, unattendedPerfResponse.length() - 1).trim()))

        softAssert.assertEquals(unattendedPerf, calculatePerformance(withinThreshold[3], totalInvocationsUnattendedPerf),
                "$AUConstants.PERFORMANCE_UNATTENDED_CURRENTDAY count mismatch")

        //Large Payload Performance Value
        String largePayPerfResponse = AUTestUtil.parseResponseBody(metricsResponse, AUConstants.PERFORMANCE_LARGE_PAYLOAD_CURRENTDAY)
        double largePayPerf = roundUpThreeDecimals(Double.parseDouble(largePayPerfResponse.substring(largePayPerfResponse
                .lastIndexOf(",") + 1, largePayPerfResponse.length() - 1).trim()))

        softAssert.assertEquals(largePayPerf, calculatePerformance(withinThreshold[4], totalInvocationsLargePayPerf),
                "$AUConstants.PERFORMANCE_LARGE_PAYLOAD_CURRENTDAY count mismatch")

        softAssert.assertAll()
    }

    /**
     * Assert Average Response Time.
     * @param metricsResponse
     * @param totalResponseTime
     */
    void assertAvgResponseMetricsResponse(Response metricsResponse, int[] totalResponseTime) {

        SoftAssert softAssert = new SoftAssert()

        //Unauthenticated Average Response Value
        double unauthAvgResponse = AUTestUtil.parseResponseBody(metricsResponse, AUConstants.AVG_RESPONSE_UNAUTH_CURRENTDAY)
                .toDouble()

        softAssert.assertEquals(unauthAvgResponse, calculateAverageResponseTime(totalResponseTime[0],
                totalInvocationsUnAuthAvg), "$AUConstants.AVG_RESPONSE_UNAUTH_CURRENTDAY count mismatch")

        //High Priority Average Response Value
        double highAvgResponse = AUTestUtil.parseResponseBody(metricsResponse, AUConstants.AVG_RESPONSE_HIGH_CURRENTDAY)
                .toDouble()

        softAssert.assertEquals(highAvgResponse, calculateAverageResponseTime(totalResponseTime[1], totalInvocationsHighAvg),
                "$AUConstants.AVG_RESPONSE_HIGH_CURRENTDAY count mismatch")

        //Low Priority Average Response Value
        double lowAvgResponse = AUTestUtil.parseResponseBody(metricsResponse, AUConstants.AVG_RESPONSE_LOW_CURRENTDAY)
                .toDouble()

        softAssert.assertEquals(lowAvgResponse, calculateAverageResponseTime(totalResponseTime[2], totalInvocationsLowAvg),
                "$AUConstants.AVG_RESPONSE_LOW_CURRENTDAY count mismatch")

        //Unattended Average Response Value
        double unattendedAvgResponse = AUTestUtil.parseResponseBody(metricsResponse,
                AUConstants.AVG_RESPONSE_UNATTENDED_CURRENTDAY).toDouble()

        softAssert.assertEquals(unattendedAvgResponse, calculateAverageResponseTime(totalResponseTime[3],
                totalInvocationsUnattendedAvg), "$AUConstants.AVG_RESPONSE_UNATTENDED_CURRENTDAY count mismatch")

        //Large Payload Average Response Value
        double largePayAvgResponse = AUTestUtil.parseResponseBody(metricsResponse,
                AUConstants.AVG_RESPONSE_LARGE_PAYLOAD_CURRENTDAY).toDouble()

        softAssert.assertEquals(largePayAvgResponse, calculateAverageResponseTime(totalResponseTime[4],
                totalInvocationsLargePayAvg), "$AUConstants.AVG_RESPONSE_LARGE_PAYLOAD_CURRENTDAY count mismatch")

        softAssert.assertAll()
    }

    /**
     * Assert Average TPS Metrics.
     * @param metricsResponse
     * @param totalResourceCount
     */
    void assertAvgTpsMetricsResponse(Response metricsResponse, int[] totalResourceCount) {

        SoftAssert softAssert = new SoftAssert()

        //Unauthenticated Average TPS Value
        String unauthAvgTps = AUTestUtil.parseResponseBody(metricsResponse, AUConstants.AVGTPS_UNAUTHENTICATED_CURRENTDAY)

        def expectedUnauthAvgTps = calculateAverageTps(totalResourceCount[0])
        softAssert.assertEquals(unauthAvgTps, expectedUnauthAvgTps.toString(),
                "$AUConstants.AVGTPS_UNAUTHENTICATED_CURRENTDAY count mismatch")

        //Authenticated Average TPS Value
        def authAvgTps = AUTestUtil.parseResponseBody(metricsResponse, AUConstants.AVGTPS_AUTHENTICATED_CURRENTDAY)

        def expectedAuthAvgTps = calculateAverageTps(totalResourceCount[1])
        softAssert.assertEquals(authAvgTps, expectedAuthAvgTps.toString(),
                "$AUConstants.AVGTPS_AUTHENTICATED_CURRENTDAY count mismatch")

        //Aggregate Average TPS Value
        String aggAvgTps = AUTestUtil.parseResponseBody(metricsResponse, AUConstants.AVGTPS_AGGREGATE_CURRENTDAY)
        int aggregateAvgTps = totalResourceCount[1].toInteger() + totalResourceCount[0].toInteger()

        def expectedAggAvgTps = calculateAverageTps(aggregateAvgTps)
        softAssert.assertEquals(aggAvgTps, expectedAggAvgTps.toString(),
                "$AUConstants.AVGTPS_AGGREGATE_CURRENTDAY count mismatch")

        softAssert.assertAll()
    }

    /**
     * Round Up the Metrics to Three Decimal Places.
     * @param withinThreshold
     * @return roundedPerfValue
     */
    static double roundUpThreeDecimals(def metrics) {

        // Using DecimalFormat to round to 3 decimal places
        String roundedValue = String.format("%.3f", metrics.toDouble())
        return Double.parseDouble(roundedValue)
    }

    /**
     * Calculate Performance Metrics
     * @param withinThreshold
     * @param totalInvocations
     * @return performanceMetrics
     */
    static double calculatePerformance(int withinThreshold, int totalInvocations){

        double performanceMetrics

        if(!withinThreshold.equals(0)) {
            performanceMetrics = withinThreshold.toDouble() / totalInvocations.toDouble()
        } else {
            performanceMetrics = 1.000
        }

        return roundUpThreeDecimals(performanceMetrics)
    }


    /**
     * Calculate Average Response Time
     * @param responseTime
     * @param totalInvocations
     * @return
     */
    static double calculateAverageResponseTime(int responseTime, int totalInvocations){

        double avgResponseTimeMetrics

        if(!responseTime.equals(0)) {
            avgResponseTimeMetrics = responseTime.toDouble() / totalInvocations.toDouble()
        } else {
            avgResponseTimeMetrics = 0
        }

        return roundUpThreeDecimals(avgResponseTimeMetrics/1000)
    }

    /**
     * Calculate Average TPS
     * @param resourceCount
     * @return averageTps
     */
    static double calculateAverageTps(int resourceCount){

        def averageTpsMetrics

        if(!resourceCount.equals(0)) {
            averageTpsMetrics = (resourceCount / 86400)
        } else {
            averageTpsMetrics = 0
        }

        return roundUpThreeDecimals(averageTpsMetrics)
    }

    /**
     * CDR Arrangement Revocation without Client Id in the request body
     * @param clientId
     * @param cdrArrangementId
     * @return
     */
    Response doRevokeCdrArrangementWithoutClientIdInRequest(String clientId, String cdrArrangementId){

        generator = new AUJWTGenerator()
        String assertionString = generator.getClientAssertionJwt(clientId)

        def bodyContent = [
                (AUConstants.CLIENT_ASSERTION_TYPE_KEY): (AUConstants.CLIENT_ASSERTION_TYPE),
                (AUConstants.CLIENT_ASSERTION_KEY)     : assertionString,
                (AUConstants.CDR_ARRANGEMENT_ID)       : cdrArrangementId]

        revocationResponse = AURestAsRequestBuilder.buildRequest()
                .contentType(AUConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .formParams(bodyContent)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_CDR_ARRANGEMENT))
                .post("${AUConstants.CDR_ARRANGEMENT_ENDPOINT}")

        return revocationResponse
    }

    /**
     * Calculate Total Response Metrics for Defined Tier
     * @param tier
     * @return metrics value
     */
    int[] calculateTierWiseTotalResponseTime() {

        String tier
        int responseTime
        int[] sumResponseTime = new int[5]
        totalInvocationsPerf = 0
        totalInvocationsUnAuthAvg = 0
        totalInvocationsHighAvg = 0
        totalInvocationsLowAvg = 0
        totalInvocationsUnattendedAvg = 0
        totalInvocationsLargePayAvg = 0

        //Get Start and End Time of the day
        long startTimeOfDay = today.atTime(00, 00, 00).toEpochSecond(ZoneOffset.UTC)
        long endTimeOfDay =  today.atTime(23, 59, 59).toEpochSecond(ZoneOffset.UTC)

        // Execute SELECT query and get results as an array
        def query = SqlQuery.retrieveRecordsWithinSpecifiedPeriod(startTimeOfDay, endTimeOfDay)
        Object[][] results = DbConnection.executeSelectQuery(AUConstants.REPORTING_DBNAME, query)
        totalInvocationsAvg = results.length

        // Process and check the response time within threshold
        if (results != null) {
            for (Object[] row : results) {
                responseTime = row[0]

                tier = AUTestUtil.getPriorityTier(row[1], row[3])

                if(!row[1].equals("/par")) {

                    switch (tier) {
                        case AUConstants.UNAUTHENTICATED:
                            totalInvocationsUnAuthAvg = totalInvocationsUnAuthAvg + 1
                            sumResponseTime[0] += responseTime
                            break
                        case AUConstants.HIGH_PRIORITY:
                            totalInvocationsHighAvg = totalInvocationsHighAvg + 1
                            sumResponseTime[1] += responseTime
                            break
                        case AUConstants.LOW_PRIORITY:
                            totalInvocationsLowAvg = totalInvocationsLowAvg + 1
                            sumResponseTime[2] += responseTime
                            break
                        case AUConstants.UNATTENDED:
                            totalInvocationsUnattendedAvg = totalInvocationsUnattendedAvg + 1
                            sumResponseTime[3] += responseTime
                            break
                        case AUConstants.LARGE_PAYLOAD:
                            totalInvocationsLargePayAvg = totalInvocationsLargePayAvg + 1
                            sumResponseTime[4] += responseTime
                            break
                        default:
                            throw new IllegalStateException("Unexpected value: " + tier + " for " + row[1])
                    }
                }
            }
        }
        return sumResponseTime
    }

    /**
     * Calculate Total Resource Count based on the resource type
     * @param tier
     * @return metrics value
     */
    int[] calculateTotalResourceCount() {

        String resourceType
        int responseTime
        int[] totalResourceCount = new int[2]
        totalInvocationsPerf = 0

        //Get Start and End Time of the Day
        long startTimeOfDay = today.atTime(00, 00, 00).toEpochSecond(ZoneOffset.UTC)
        long endTimeOfDay =  today.atTime(23, 59, 59).toEpochSecond(ZoneOffset.UTC)

        // Execute SELECT query and get results as an array
        def query = SqlQuery.retrieveRecordsWithinSpecifiedPeriod(startTimeOfDay, endTimeOfDay)
        Object[][] results = DbConnection.executeSelectQuery(AUConstants.REPORTING_DBNAME, query)

        // Process and check the response time within threshold
        if (results != null) {
            for (Object[] row : results) {
                responseTime = row[0]

                resourceType = AUTestUtil.getAuthenticatedResources(row[1])

                if (!row[1].equals("/par")) {

                    switch (resourceType) {
                        case AUConstants.UNAUTHENTICATED:
                            totalResourceCount[0]++
                            break
                        case AUConstants.AUTHENTICATED:
                            totalResourceCount[1]++
                            break
                        default:
                            throw new IllegalStateException("Unexpected value: " + resourceType + " for " + row[1])
                    }
                }
            }
        }

        return totalResourceCount
    }

    AccessTokenResponse getUserAccessTokenResponseWithDifferentClientId(String assertionClientId = null, String bodyClientID ) {
        try {
            return AURequestBuilder.getUserToken(authorisationCode, AUConstants.CODE_VERIFIER, clientId)
        }
        catch (Exception e) {
            log.error(e)
        }
    }

    /**
     * Consent Amendment Deny
     * @param scopes
     * @param cdrArrangementId
     * @param sharingDuration
     * @param clientId
     * @return auth code
     */
    String doConsentAmendmentDenyFlow(List<AUAccountScope> scopes, String cdrArrangementId, long sharingDuration,
                                      String clientId = null) {

        if (clientId == null) {
            response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, sharingDuration,
                    true, cdrArrangementId)
            requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
            authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI())
                    .toURI().toString()
        } else {
            response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, sharingDuration,
                    true, cdrArrangementId, clientId)
            requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
            authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), clientId)
                    .toURI().toString()
        }

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
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_DENY_XPATH)
                }
                .execute()

        return automation.currentUrl.get()
    }

    /**
     * Consent Amendment Authorisation
     * @param scopes
     * @param cdrArrangementId
     * @param sharingDuration
     * @param clientId
     * @return auth code
     */
    String doBusinessConsentAmendmentAuthorisation(List<AUAccountScope> scopes, String cdrArrangementId, long sharingDuration,
                                                   String clientId = null) {

        if (clientId == null) {
            response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, sharingDuration,
                    true, cdrArrangementId)
            requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
            authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI())
                    .toURI().toString()
        } else {
            response = auAuthorisationBuilder.doPushAuthorisationRequest(scopes, sharingDuration,
                    true, cdrArrangementId, clientId)
            requestUri = AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI)
            authoriseUrl = auAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), clientId)
                    .toURI().toString()
        }

        //Consent Amendment Authorisation Flow
        def automation = getBrowserAutomation(AUConstants.DEFAULT_DELAY)
                .addStep(new AUBasicAuthAutomationStep(authoriseUrl))
                .addStep { driver, context ->
                    AutomationMethod authWebDriver = new AutomationMethod(driver)

                    //Click Confirm Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)

                    //Click Authorise Button
                    authWebDriver.clickButtonXpath(AUPageObjects.CONSENT_CONFIRM_XPATH)
                }
                .execute()

        // Get Code From URL
        return AUTestUtil.getCodeFromJwtResponse(automation.currentUrl.get())
    }
}

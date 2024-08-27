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

package com.wso2.cds.test.framework

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.nimbusds.oauth2.sdk.ResponseMode
import com.nimbusds.oauth2.sdk.ResponseType
import com.nimbusds.oauth2.sdk.token.RefreshToken
import com.wso2.cds.test.framework.constant.AUAccountProfile
import com.wso2.cds.test.framework.constant.AUAccountScope
import com.wso2.cds.test.framework.constant.AUConfigConstants
import com.wso2.cds.test.framework.constant.AUConstants
import com.wso2.cds.test.framework.constant.AUPageObjects
import com.wso2.cds.test.framework.constant.AUPayloads
import com.wso2.cds.test.framework.constant.ContextConstants
import com.wso2.cds.test.framework.automation.consent.AUAccountSelectionStep
import com.wso2.cds.test.framework.automation.consent.AUBasicAuthAutomationStep
import com.nimbusds.oauth2.sdk.AccessTokenResponse
import com.wso2.cds.test.framework.request_builder.AUJWTGenerator
import com.wso2.openbanking.test.framework.OBTest
import com.wso2.cds.test.framework.configuration.AUConfigurationService
import com.wso2.openbanking.test.framework.automation.AutomationMethod
import com.wso2.openbanking.test.framework.automation.NavigationAutomationStep
import com.wso2.openbanking.test.framework.configuration.OBConfigParser
import io.restassured.response.Response
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.testng.Assert
import org.testng.ITestContext
import org.testng.annotations.BeforeClass
import com.wso2.cds.test.framework.request_builder.AUAuthorisationBuilder
import com.wso2.cds.test.framework.request_builder.AURegistrationRequestBuilder
import com.wso2.cds.test.framework.request_builder.AURequestBuilder
import com.wso2.cds.test.framework.utility.AURestAsRequestBuilder
import com.wso2.cds.test.framework.utility.AUTestUtil

import java.nio.charset.Charset
import java.time.OffsetDateTime
import java.time.ZoneOffset
import com.google.gson.Gson

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
                    AUAccountScope.BANK_ACCOUNT_BASIC_READ.getScopeString(),
                    AUAccountScope.BANK_TRANSACTION_READ.getScopeString(),
                    AUAccountScope.BANK_CUSTOMER_DETAIL_READ.getScopeString(),
                    AUAccountScope.CDR_REGISTRATION.getScopeString()
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
    void doConsentAuthorisation(ResponseMode responseMode, ResponseType responseType = ResponseType.CODE_IDTOKEN, String clientId = null,
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
        if (clientId) {
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

                if (isSelectMultipleAccounts) {
                    //Select Business Account 2
                    secondConsentedAccount = authWebDriver.getElementAttribute(AUTestUtil.getBusinessAccount3CheckBox(),
                            AUPageObjects.VALUE)
                    authWebDriver.clickButtonXpath(AUTestUtil.getBusinessAccount3CheckBox())
                }
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
                                                             boolean otherAccountsAvailability = false) {

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
        String assertionString = generator.getClientAssertionJwt(clientId)

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
}

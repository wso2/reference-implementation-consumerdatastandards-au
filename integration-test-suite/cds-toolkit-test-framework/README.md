# Introduction to the CDS Toolkit Test Framework

The CDS Toolkit Test Framework is a test automation framework that is used to implement test cases for the Consumer Data Standards (CDS) Toolkit. 
The framework provides a set of constants, methods and template builders that can be used to create and run test cases for the CDS Toolkit. 

The framework is built using the Java/Groovy programming language and the RestAssured and TestNG framework. Selenium Webdriver is used to interact with the web browser.
A common configuration loader to load the input configurations.


[AUTest.groovy](src%2Fmain%2Fgroovy%2Forg%2Fwso2%2Fcds%2Ftest%2Fframework%2FAUTest.groovy): 
This is an abstract class that contains the common methods that are used to automate the CDS Toolkit scenarios. 
Test designers can use these methods automate the steps in the test cases by extending the test class from this class.

## Introduction to common methods in AUTest.groovy

* getApplicationScope() 

      This method is used to get the application scope of the CDS Toolkit. The method returns the application scope as a string.

* doConsentAuthorisation(String clientId = null, AUAccountProfile profiles = AUAccountProfile.INDIVIDUAL)
        
      This method is used to automate the common consent authorisation flow. The method takes the clientId and the profile as a parameter and automates the consent authorisation flow. 
      By default, the method passes INDIVIDUAL profile.
      The method it self send a PAR request, get the request_uri and proceed with the authorisation flow. 

* doConsentAuthorisation(ResponseMode responseMode, ResponseType responseType = ResponseType.CODE, String clientId = null,
  AUAccountProfile profiles = AUAccountProfile.INDIVIDUAL, boolean isStateParamPresent = true)

      This method is also used to automate the common consent authorisation flow. The method takes the responseMode, responseType, clientId, profile and the isStateParamPresent check as a parameter and automates the consent authorisation flow. 
      By default, the method passes INDIVIDUAL profile and responseType code.
      The method it self send a PAR request, get the request_uri and proceed with the authorisation flow. 

* doConsentAuthorisationViaRequestUri(List<AUAccountScope> scopes, URI requestUri, String clientId = null, AUAccountProfile profiles = null)
     
      This method is used to automate the common consent authorisation flow using the request_uri. The method takes the scopes, requestUri, clientId and profile as a parameter and automates the consent authorisation flow. 
      
* doAuthorization(long sharingDuration, boolean sendSharingDuration)

      This method is used to automate the common authorisation flow. The method takes the sharingDuration and the sendSharingDuration as a parameter and automates the authorisation flow. 
      By default, the method passes the sharingDuration as 0 and sendSharingDuration as false. "sendSharingDuration" is a boolean value that indicates whether the sharing duration should be sent in the authorisation request.

* generateUserAccessToken(String clientId = null)

      This method is used to send the user access token request. The method takes the clientId as a parameter and returns the user access token response.

* getUserAccessToken(ITestContext context)

      This method is used to get the existing user access token from the test context if already generated. Otherwise new user access token will be generated. 

* deleteApplicationIfExists(List<String> scopes, String clientId = auConfiguration.getAppInfoClientID())

      This method is used to delete the application if it already exists. The method takes the scopes and the clientId as a parameter and deletes the application if it already exists.

* deleteApplicationIfExists(String clientId = auConfiguration.getAppInfoClientID())

      This method is used to delete the application if it already exists. The method takes the clientId as a parameter and deletes the application if it already exists.

* getApplicationAccessToken(String clientId = auConfiguration.getAppInfoClientID())

      This method is used to get the application access token. The method takes the clientId as a parameter and returns the application access token response.

* tppRegistration()

      This method is used to automate the TPP registration flow through DCR API. The method returns the TPP registration response.

* tppDeletion(String clientId, String accessToken)

      This method is used to delete the TPP through DCR API. The method takes the clientId and the accessToken as a parameter and deletes the TPP.

* getCDSClient()

      This method is used to get the CDS client by appending client id and client secret. The method returns the CDS client.

* selectProfileAndAccount(AutomationMethod authWebDriver, AUAccountProfile profiles = null, boolean isSelectMultipleAccounts = false)

      This method is used to navigate through profile and account selection pages and select the profile and accounts in authorisation flow. 
      The method takes the authWebDriver, profiles and the isSelectMultipleAccounts as a parameter and selects the profile and account.

* selectSecondaryAccount(AutomationMethod authWebDriver, boolean isSelectMultipleAccounts = false)

      This method is used to navigate through profile and account selection pages and select the secondary account in authorisation flow. 
      The method takes the authWebDriver and the isSelectMultipleAccounts as a parameter and selects the secondary account.

* doConsentAuthorisationWithoutAccountSelection(String profiles = AUAccountProfile.INDIVIDUAL)
    
      This method is used to automate the common consent authorisation flow without account selection. 
      The method takes the profile as a parameter and automates the consent authorisation flow without account selection. By default, the method passes INDIVIDUAL profile.

* doConsentAuthorisationViaRequestUriSingleAccount(List<AUAccountScope> scopes, URI requestUri, String clientId = null , AUAccountProfile profiles = null)

      This method is used to automate the common consent authorisation flow using the request_uri for single (Not Joint) account. 
      The method takes the scopes, requestUri, clientId and profile as a parameter and automates the consent authorisation flow for single account.

* doConsentAuthorisationViaRequestUriLargeSharingDue(List<AUAccountScope> scopes, URI requestUri, String clientId = null , AUAccountProfile profiles = null)
    
      This method is used to automate the common consent authorisation flow using the request_uri for sharing duration for a longer period. 
      The method takes the scopes, requestUri, clientId and profile as a parameter and automates the consent authorisation flow for large sharing duration.

* doConsentAuthorisationViaRequestUriDenyFlow(List<AUAccountScope> scopes, URI requestUri, String clientId = null , AUAccountProfile profiles = null, boolean isStateParamPresent = true)

      This method is used to automate the common consent authorisation flow using the request_uri for authorisation deny flow. 
      The method takes the scopes, requestUri, clientId, profile and the isStateParamPresent check as a parameter and automates the consent authorisation flow for deny flow.

* doConsentAuthorisationViaRequestUriNoAccountSelection(List<AUAccountScope> scopes, URI requestUri, String clientId = null , AUAccountProfile profiles = null)

      This method is used to automate the common consent authorisation flow using the request_uri without account selection. 
      The method takes the scopes, requestUri, clientId and profile as a parameter and automates the consent authorisation flow without account selection.

* doSecondaryAccountSelection(List<AUAccountScope> scopes, URI requestUri, String clientId = null, boolean isMultipleAccountsSelect = false)

      This method is used to automate the common consent authorisation flow using the request_uri for secondary account selection. 
      The method takes the scopes, requestUri, clientId and isMultipleAccountsSelect as a parameter and automates the consent authorisation flow for secondary account selection.

* doSecondaryAccountSelectionCheckUnavailableAccounts(List<AUAccountScope> scopes, URI requestUri, String clientId = null , AUAccountProfile profiles = null)

      This method is used to automate the common consent authorisation flow to verify the unavailable secondary accounts in accounts selection page. 
      The method takes the scopes, requestUri, clientId and profile as a parameter and automates the consent authorisation flow for secondary account selection with unavailable accounts.

* doAuthorisationViaRequestUriWithoutAccSelection(String authoriseUrl)
    
      This method is used to automate the common authorisation flow using the request_uri without account selection. 
      The method takes the authoriseUrl as a parameter and automates the authorisation flow without account selection.

* doConsentAuthorisationSelectingSingleAccount(String clientId = null, AUAccountProfile profiles = AUAccountProfile.INDIVIDUAL)
    
      This method is used to automate the common consent authorisation flow by selecting the single account. 
      The method takes the clientId and the profile as a parameter and automates the consent authorisation flow by selecting the single account.

* doAccountRetrieval(String userAccessToken, int accountEndpointVersion = AUConstants.X_V_HEADER_ACCOUNTS)

      This method is used to retrieve the accounts using the user access token. The method takes the userAccessToken and the accountEndpointVersion as a parameter and retrieves the accounts.

* doConsentSearch()

      This method is used to automate the common consent search flow. The method returns the consent search response.

* updateSingleBusinessUserPermission(String headerString, String accountID, String accountOwnerUserID, String nominatedRepUserID,
  String permissionType)

      This method is used to update the business user permission of a single user. 
      The method takes the headerString, accountID, accountOwnerUserID, nominatedRepUserID and the permissionType as a parameter and updates the single business user permission.

* updateMultiBusinessUserPermission(String headerString, String accountID, String accountOwnerUserID, String nominatedRepUserID,
  String permissionType, String nominatedRepUserID2, String permissionType2)

      This method is used to update the business user permission of multiple users. 
      The method takes the headerString, accountID, accountOwnerUserID, nominatedRepUserID, permissionType, nominatedRepUserID2 and the permissionType2 as a parameter and updates the multiple business user permission.

* getSharableBankAccounts()

      This method is used to get the sharable bank accounts details. The method returns the sharable bank accounts list.

* deleteSingleBusinessUser(String headerString, String accountID, String accountOwnerUserID, String nominatedRepUserID)

      This method is used to delete the business user nomination. The method takes the headerString, accountID, accountOwnerUserID and the nominatedRepUserID as a parameter and deletes the business user.

* deleteMultipleBusinessUsers(String headerString, String accountID, String accountOwnerUserID, String nominatedRepUserID,
  String nominatedRepUserID2)

      This method is used to delete the multiple business user nominations. 
      The method takes the headerString, accountID, accountOwnerUserID, nominatedRepUserID and the nominatedRepUserID2 as a parameter and deletes the business users.

* getStakeholderPermissions(String userId, String accountId)

      This method is used to get the Business User Permissions of a particular user. The method takes the userId and the accountId as a parameter and returns the stakeholder permissions.

* doAuthorisationFlowNavigation(String authoriseUrl, AUAccountProfile profiles = null, boolean isMultipleAccSelect = false)

      This method is used to automate the common authorisation flow navigation. 
      The method takes the authoriseUrl, profiles and the isMultipleAccSelect as a parameter and automates the authorisation flow navigation.

* updateBusinessUserPermissionWithIncorrectPayload(String headerString, String accountID, String accountOwnerUserID, String nominatedRepUserID, String permissionType)
    
      This method is used to update the business user permission with incorrect payload. 
      The method takes the headerString, accountID, accountOwnerUserID, nominatedRepUserID and the permissionType as a parameter and updates the business user permission with incorrect payload.

* deleteBusinessUserWithIncorrectPayload(String headerString, String accountID, String accountOwnerUserID, String nominatedRepUserID)

      This method is used to delete the business user nomination with incorrect payload. 
      The method takes the headerString, accountID, accountOwnerUserID and the nominatedRepUserID as a parameter and deletes the business user with incorrect payload.

* doAuthorisationFlowForJointAccounts(List<AUAccountScope> scopes, URI requestUri, String clientId = null, boolean isSelectMultipleAccounts = false)
    
      This method is used to automate the common authorisation flow for joint accounts. 
      The method takes the scopes, requestUri, clientId and the isSelectMultipleAccounts as a parameter and automates the authorisation flow for joint accounts.

* doJointAccountConsentAuthorisation(String clientId = null, boolean isSelectMultipleAccounts = true)

      This method is used to automate the common consent authorisation flow for joint accounts. 
      The method takes the clientId and the isSelectMultipleAccounts as a parameter and automates the consent authorisation flow for joint accounts.

* updateDisclosureOptionsMgtService(String headerString, Map<String, String> domsStatusMap)

      This method is used to update the Disclosure Options Management Service. 
      The method takes the headerString and the domsStatusMap as a parameter and updates the Disclosure Options Management Service.

* Response updateLegalEntityStatus(String headerString, String accountId, String secondaryUserId, String legalEntityId, String legalEntityStatus,
  boolean isMultipleLegalEntity = false, String secondaryUserId2 = null, String accountId2 = null, String legalEntityId2 = null, String legalEntityStatus2 = null)

      This method is used to update the Legal Entity Status of a secondary user. 
      The method takes the headerString, accountId, secondaryUserId, legalEntityId, legalEntityStatus, isMultipleLegalEntity, secondaryUserId2, accountId2, legalEntityId2 and the legalEntityStatus2 as a parameter and updates the Legal Entity Status.

* updateSecondaryUserInstructionPermission(String secondaryAccId, String userId, String secondaryAccountInstructionStatus, boolean otherAccountsAvailability = true)

      This method is used to update the Secondary User Instruction Permission. 
      The method takes the secondaryAccId, userId, secondaryAccountInstructionStatus and the otherAccountsAvailability as a parameter and updates the Secondary User Instruction Permission.

* getLegalEntityIds(String userID)

      This method is used to get the Legal Entity Ids of a particular user. The method takes the userID as a parameter and returns the Legal Entity Ids.

* getSharingStatusOfUserAccount(String legalEntityList, String userId, String accountId, String legalEntityId)

      This method is used to get the Sharing Status of a user account. 
      The method takes the legalEntityList, userId, accountId and the legalEntityId as a parameter and returns the Sharing Status.

* verifyScopes(String scopesString, String eliminatedScope = null)

      This method is used to verify the scopes. The method takes the scopesString and the eliminatedScope as a parameter and verifies the scopes.

* getUserAccessTokenFormRefreshToken(RefreshToken refreshToken)

      This method is used to get the user access token from the refresh token. The method takes the refreshToken as a parameter and returns the user access token.

* doRevokeCdrArrangement(String clientId, String cdrArrangementId, String audience)

      This method is used to revoke the CDR Arrangement. The method takes the clientId, cdrArrangementId and the audience as a parameter and revokes the CDR Arrangement.

* getConsentStatus(String clientHeader, String consentId)

      This method is used to get the Consent Status. The method takes the clientHeader and the consentId as a parameter and returns the Consent Status.

* doConsentAuthorisationViaRequestUri(List<AUAccountScope> scopes, URI requestUri, ResponseMode responseMode, ResponseType responseType = ResponseType.CODE_IDTOKEN, 
  String clientId = null, AUAccountProfile profiles = null, boolean isStateParamPresent = true)
    
      This method is used to automate the consent authorization method with Request URI and Response Mode. 
      The method takes the scopes, requestUri, responseMode, responseType, clientId, profile and the isStateParamPresent check as a parameter and automates the consent authorisation flow.

* doAuthorisationErrorFlow(String authoriseUrl)

      This method is used to automate the authorisation error flow. The method takes the authoriseUrl as a parameter and automates the authorisation error flow.

* doSecondaryUserAuthFlowWithoutAccountSelection(List<AUAccountScope> scopes, URI requestUri, String clientId = null)

      This method is used to automate the secondary user authorisation flow without account selection. 
      The method takes the scopes, requestUri and the clientId as a parameter and automates the secondary user authorisation flow without account selection.

* getMetrics(String period = "ALL")

      This method is used to get the Metrics according to the given period. The method takes the period as a parameter and returns the Metrics.

* getInitialMetricsResponse(Response metricsResponse)

      This method is used to get the Initial Metrics Response. The method takes the metricsResponse as a parameter and returns the Initial Metrics Response.

* assertMetricsAuthorisationResponse(Response metricsResponse)

      This method is used to verify the Authorisation Metrics Response. The method takes the metricsResponse as a parameter and asserts the Metrics Authorisation Response.

* doConsentAmendmentAuthorisation(List<AUAccountScope> scopes, String cdrArrangementId, long sharingDuration, String clientId = null)

      This method is used to automate the consent amendment authorisation flow. 
      The method takes the scopes, cdrArrangementId, sharingDuration and the clientId as a parameter and automates the consent amendment authorisation flow.

* authoriseConsentWithoutClosingBrowser(List<AUAccountScope> scopes, URI requestUri, String clientId = null, boolean isMultipleAccSelect = false, AUAccountProfile profiles = null)

      This method is used to automate the authorisation flow in same browser session (without closing the browser). 
      The method takes the scopes, requestUri, clientId, isMultipleAccSelect and the profiles as a parameter and automates the authorisation flow without closing the browser.

* getErrorsMetrics(String metricsResponse, int modifiedErrorCode)

      This method is used to get the Errors Metrics from Metircs response. The method takes the metricsResponse and the modifiedErrorCode as a parameter and returns the Errors Metrics.

* parseMetricsString(String metricsInput)

      This method is used to parse the Metrics String into a map. The method takes the metricsInput as a parameter and returns the parsed Metrics String.

* updateOrAddErrorCode(Map<String, Integer> errorMap, String errorCode, int incrementBy)
    
      This method is used to update the Error Count in the map. The method takes the errorMap, errorCode and the incrementBy as a parameter and updates the Error Count.

* generateMetricsString(Map<String, Integer> errorMap)

      This method is used to sort the map by key in ascending order and format it back into a string. The method takes the errorMap as a parameter and returns the generated Metrics String.

* assertMetricsErrorResponse(Response metricsResponse)

      This method is used to verify the Error Metrics Response. The method takes the metricsResponse as a parameter and asserts the Metrics Error Response.

* calculateTierBasedMetrics()

      This method is used to calculate the Tier Based Metrics of Mertics Response. This method conatins the calculation of Performance, Response Time and Total Resource count for average TPS. The method returns the Tier Based Metrics.

* metricsPerformanceCalculation()

      This method is used to calculate the Performance Metrics of Mertics Response. The method returns the Performance Metrics.

* assertTierBasedMetrics(Response metricsResponse)

      This method is used to verify the Tier Based Metrics Response. The method takes the metricsResponse as a parameter and asserts the Tier Based Metrics Response.

* assertMetricsInvocationResponse(Response metricsResponse)

      This method is used to verify the Invocation Metrics Response. The method takes the metricsResponse as a parameter and asserts the Metrics Invocation Response.

* assertPerformanceMetricsResponse(Response metricsResponse, int[] withinThreshold)

      This method is used to verify the Performance Metrics Response. The method takes the metricsResponse and the withinThreshold as a parameter and asserts the Performance Metrics Response.

* assertAvgResponseMetricsResponse(Response metricsResponse, int[] totalResponseTime)

      This method is used to verify the Average Response Metrics Response. The method takes the metricsResponse and the totalResponseTime as a parameter and asserts the Average Response Metrics Response.

* assertAvgTpsMetricsResponse(Response metricsResponse, int[] totalResourceCount)

      This method is used to verify the Average TPS Metrics Response. The method takes the metricsResponse and the totalResourceCount as a parameter and asserts the Average TPS Metrics Response.

* roundUpThreeDecimals(def metrics)

      This method is used to round up the metrics to three decimals. The method takes the metrics as a parameter and returns the rounded up metrics.

* calculatePerformance(int withinThreshold, int totalInvocations)

      This method is used to calculate the Performance Metrics. The method takes the withinThreshold and the totalInvocations as a parameter and returns the Performance Metrics.

* calculateAverageResponseTime(int responseTime, int totalInvocations)

      This method is used to calculate the Average Response Time Metrics. The method takes the responseTime and the totalInvocations as a parameter and returns the Average Response Time Metrics.

* calculateAverageTps(int resourceCount)

      This method is used to calculate the Average TPS Metrics. The method takes the resourceCount as a parameter and returns the Average TPS Metrics.

* doRevokeCdrArrangementWithoutClientIdInRequest(String clientId, String cdrArrangementId)

      This method is used to revoke the CDR Arrangement without client id in the request body. 
      The method takes the clientId and the cdrArrangementId as a parameter and revokes the CDR Arrangement without client id in the request.

* calculateTierWiseTotalResponseTime()

      This method is used to calculate the Tier Wise Total Response Time Metrics. The method returns the Tier Wise Total Response Time Metrics.

* calculateTotalResourceCount()

      This method is used to calculate the Total Resource Count Metrics. The method returns the Total Resource Count Metrics.

* doConsentAmendmentDenyFlow(List<AUAccountScope> scopes, String cdrArrangementId, long sharingDuration, String clientId = null)
    
      This method is used to automate the consent amendment deny flow. 
      The method takes the scopes, cdrArrangementId, sharingDuration and the clientId as a parameter and automates the consent amendment authorisation flow for deny flow.

* doBusinessConsentAmendmentAuthorisation(List<AUAccountScope> scopes, String cdrArrangementId, long sharingDuration, String clientId = null)
    
      This method is used to automate the business consent amendment authorisation flow. 
      The method takes the scopes, cdrArrangementId, sharingDuration and the clientId as a parameter and automates the business consent amendment authorisation flow.


## Introduction to Packages in the CDS Toolkit Test Framework

* [automation](src%2Fmain%2Fgroovy%2Forg%2Fwso2%2Fcds%2Ftest%2Fframework%2Fautomation) 

      This package contains the classes that are used to automate User Interfaces associates with the CDS Scenarios.

* [configuration](src%2Fmain%2Fgroovy%2Forg%2Fwso2%2Fcds%2Ftest%2Fframework%2Fconfiguration)
    
      This package contains the classes that are used to load the input configurations in TestConfiguration.xml file that are used in the CDS Scenarios.

* [constant](src%2Fmain%2Fgroovy%2Forg%2Fwso2%2Fcds%2Ftest%2Fframework%2Fconstant)
    
      This package contains the classes that are used to define the constants and UI elements that are used in the CDS Scenarios.

* [data_provider](src%2Fmain%2Fgroovy%2Forg%2Fwso2%2Fcds%2Ftest%2Fframework%2Fdata_provider)
     
      This package contains the data providers with input data sets which are used to run single test case for multiple data sets.

* [keystore](src%2Fmain%2Fgroovy%2Forg%2Fwso2%2Fcds%2Ftest%2Fframework%2Fkeystore)
    
      This package contains the classes that are used to load the keystore files that are used in the CDS Scenarios.

* [request_builder](src%2Fmain%2Fgroovy%2Forg%2Fwso2%2Fcds%2Ftest%2Fframework%2Frequest_builder)
    
      This package contains the request builder templates for all the API requests.

* [utility](src%2Fmain%2Fgroovy%2Forg%2Fwso2%2Fcds%2Ftest%2Fframework%2Futility)
    
      This package contains the utility classes that are used to perform common operations in the CDS Scenarios.

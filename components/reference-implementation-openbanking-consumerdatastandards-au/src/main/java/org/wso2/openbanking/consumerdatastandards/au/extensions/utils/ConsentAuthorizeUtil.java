/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.openbanking.consumerdatastandards.au.extensions.utils;

import net.minidev.json.parser.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import net.minidev.json.parser.JSONParser;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.openbanking.consumerdatastandards.au.extensions.configurations.ConfigurableProperties;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.CdsErrorEnum;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.CommonConstants;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.PermissionsEnum;
import org.wso2.openbanking.consumerdatastandards.au.extensions.exceptions.CdsConsentException;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.SuccessResponsePopulateConsentAuthorizeScreenDataConsentData;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.SuccessResponsePopulateConsentAuthorizeScreenDataConsumerData;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.SuccessResponsePopulateConsentAuthorizeScreenDataConsumerDataAccountsInner;

/**
 * Utility class for consent authorization related operations.
 */
public class ConsentAuthorizeUtil {

    private static final Log log = LogFactory.getLog(ConsentAuthorizeUtil.class);
    private static final int secondsInYear = (int) TimeUnit.SECONDS.convert(365, TimeUnit.DAYS);

    /**
     * Extracts required data from the given request object.
     *
     * @param jsonRequestBody The JSON object representing the request payload from which required data will be extracted.
     * @return A map containing the extracted data, or error information if extraction fails.
     * @throws CdsConsentException If there is an error while parsing the request object or extracting required data.
     */
    public static Map<String, Object> extractRequiredDataFromRequestObject(JSONObject jsonRequestBody) throws
            CdsConsentException {

        String clientID;
        Map<String, Object> dataMap = new HashMap<>();

        try {
            long sharingDuration = 0;
            clientID = jsonRequestBody.getString(CommonConstants.CLIENT_ID);

            // Validate client_id existence
            if (StringUtils.isBlank(clientID)) {
                log.error("client_id not found in request object");
                dataMap.put(CommonConstants.IS_ERROR, "client_id not found in request object");
                return dataMap;
            }
            dataMap.put(CommonConstants.CLIENT_ID, clientID);

            // Verify claims
            if (jsonRequestBody.has(CommonConstants.CLAIMS)) {
                JSONObject claims = (JSONObject) jsonRequestBody.get(CommonConstants.CLAIMS);
                if (claims.has(CommonConstants.SHARING_DURATION)) {

                    String sharingDurationStr = claims.get(CommonConstants.SHARING_DURATION) == null ?
                            StringUtils.EMPTY : claims.get(CommonConstants.SHARING_DURATION).toString();

                    sharingDuration = sharingDurationStr.isEmpty() ? 0 : Long.parseLong(sharingDurationStr);

                    if (sharingDuration > secondsInYear) {
                        sharingDuration = secondsInYear;
                        if (log.isDebugEnabled()) {
                            log.debug("Requested sharing_duration is greater than a year,therefore one year duration"
                                    + " is set as consent expiration for request object of client: "
                                    + dataMap.get(CommonConstants.CLIENT_ID));
                        }
                    }
                    dataMap.put(CommonConstants.EXPIRATION_DATE_TIME,
                            getConsentExpiryDateTime(sharingDuration));
                }
                if (sharingDuration == 0) {
                    if (log.isDebugEnabled()) {
                        log.debug("sharing_duration not found in the request object of client: " + clientID);
                    }
                    dataMap.put(CommonConstants.EXPIRATION_DATE_TIME, 0);
                }

                // adding original sharing_duration_value to data map
                dataMap.put(CommonConstants.SHARING_DURATION_VALUE, sharingDuration);

                // Extracting cdr_arrangement_id, id_token and userinfo claims if exist
                if (claims.has(CommonConstants.CDR_ARRANGEMENT_ID)
                        && claims.get(CommonConstants.CDR_ARRANGEMENT_ID) != null) {
                    dataMap.put(CommonConstants.CDR_ARRANGEMENT_ID,
                            claims.get(CommonConstants.CDR_ARRANGEMENT_ID).toString());
                }

                String idTokenJsonString = claims.has(CommonConstants.ID_TOKEN) ?
                        claims.get(CommonConstants.ID_TOKEN).toString() : null;

                String userInfoJsonString = claims.has(CommonConstants.USERINFO) ?
                        claims.get(CommonConstants.USERINFO).toString() : null;

                JSONParser parser = new JSONParser();
                dataMap.put(CommonConstants.ID_TOKEN_CLAIMS, StringUtils.isNotBlank(idTokenJsonString) ?
                        parser.parse(idTokenJsonString) : new JSONObject());
                dataMap.put(CommonConstants.USERINFO_CLAIMS, StringUtils.isNotBlank(userInfoJsonString) ?
                        parser.parse(userInfoJsonString) : new JSONObject());
            }
        } catch (ParseException e) {
            log.error("Error while parsing the request object", e);
            throw new CdsConsentException(CdsErrorEnum.UNEXPECTED_ERROR, "Error while parsing the request object: " + e);
        }
        return dataMap;
    }


    /**
     * Calculate consent expiry date time based on sharing duration.
     * @param sharingDuration the duration in seconds for which the consent is valid
     * @return the calculated consent expiry date and time as an {@link OffsetDateTime}
     */
    private static OffsetDateTime getConsentExpiryDateTime(long sharingDuration) {

        OffsetDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC);
        return currentTime.plusSeconds(sharingDuration);
    }

    /**
     * Set profile scope related individual claims as permissions.
     *
     * @param jsonRequestBody  request payload
     * @param userInfoClaims user info claims
     * @param idTokenClaims  id token claims
     */
    private void setClaimPermissions(JSONObject consentData, JSONObject jsonRequestBody, JSONObject userInfoClaims,
                                     JSONObject idTokenClaims) {

        StringBuilder scopeString = new StringBuilder(jsonRequestBody.getString("scope"));

        List[] clusters = {CommonConstants.NAME_CLUSTER_CLAIMS,
                CommonConstants.PHONE_CLUSTER_CLAIMS,
                CommonConstants.EMAIL_CLUSTER_CLAIMS,
                CommonConstants.MAIL_CLUSTER_CLAIMS};

        for (List<String> cluster : clusters) {
            for (String claim : cluster) {
                if (userInfoClaims.has(claim) || idTokenClaims.has(claim)) {
                    scopeString.append(" ");
                    scopeString.append(claim);
                }
            }
        }
        consentData.put("scope", scopeString.toString().trim());
    }

    /**
     * Method to retrieve consent data from request object.
     * @param jsonRequestBody The JSON object representing the request object of authorization request.
     * @param requiredData The Map containing the required data which is extracted from the request object.
     * @return SuccessResponsePopulateConsentAuthorizeScreenDataConsentData object containing the consent data.
     */
    public static SuccessResponsePopulateConsentAuthorizeScreenDataConsentData cdsConsentRetrieval(
            JSONObject jsonRequestBody, Map<String, Object> requiredData) throws CdsConsentException {

        SuccessResponsePopulateConsentAuthorizeScreenDataConsentData consentData =
                new SuccessResponsePopulateConsentAuthorizeScreenDataConsentData();

        try {

            //Extract Basic consent data from the required data map.
            String expirationDate = requiredData.get(CommonConstants.EXPIRATION_DATE_TIME).toString();
            String sharingDurationValue = requiredData.get(CommonConstants.SHARING_DURATION_VALUE).toString();
            Object userInfoClaim = requiredData.get(CommonConstants.USERINFO_CLAIMS);
            Object idTokenClaim = requiredData.get(CommonConstants.ID_TOKEN_CLAIMS);

            //Set Consent Type
            consentData.setType(CommonConstants.CDR_ACCOUNTS);

            //Construct scopes list by setting profile scope related individual claims as permissions.
            setClaimPermissions(jsonRequestBody, userInfoClaim, idTokenClaim);

            //Retrieve Scopes
            String scopesString = jsonRequestBody.getString("scope");

            //Get List of Permission Objects
            List<SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner> permissionObjs =
                    getPermissionList(scopesString);

            //Convert the Permission Objects to a List<String>
            List<String> permissionsList = new ArrayList<>();
            for (SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner perm : permissionObjs) {
                permissionsList.addAll(perm.getDisplayValues());
            }

            //Set Permissions to Consent Data
            consentData.setPermissions(permissionObjs);

            //Check the scopes has "bank:" scopes to skip account selection
            //Set additional Property customerScopesOnly
            if (scopesString.contains(CommonConstants.COMMON_ACCOUNTS_BASIC_READ_SCOPE) ||
                    scopesString.contains(CommonConstants.COMMON_ACCOUNTS_DETAIL_READ_SCOPE) ||
                    scopesString.contains(CommonConstants.TRANSACTIONS_READ_SCOPE) ||
                    scopesString.contains(CommonConstants.REGULAR_PAYMENTS_READ_SCOPE)) {
                consentData.setAdditionalProperty(CommonConstants.CUSTOMER_SCOPES_ONLY, false);
            } else {
                consentData.setAdditionalProperty(CommonConstants.CUSTOMER_SCOPES_ONLY, true);
            }

            //Check if consent amendment flow
            if (requiredData.containsKey(CommonConstants.CDR_ARRANGEMENT_ID)) {

                //TODO: Implement Consent Amendment
            } else {
                //Set additional Property isConsentAmendment
                consentData.setAdditionalProperty(CommonConstants.IS_CONSENT_AMENDMENT, false);
            }

            //Set Basic Consent Data for Consent Authorisation Screen.
            Map<String, List<String>> basicConsentData = constructBasicConsentData(expirationDate, sharingDurationValue,
                    permissionsList);
            consentData.setBasicConsentData(basicConsentData);

            //Set Consent MetaData to Consent Data
            Map<String,Object> consentMetadatMap = constructConsentMetadatMap(permissionsList, expirationDate);
            consentData.setConsentMetadata(consentMetadatMap);

            //Set additional properties to Consent Data
            consentData.setAdditionalProperty(CommonConstants.USERINFO_CLAIMS,
                    Collections.singletonList(userInfoClaim.toString()));
            consentData.setAdditionalProperty(CommonConstants.ID_TOKEN_CLAIMS,
                    Collections.singletonList(idTokenClaim.toString()));

            //Specify to handle Account Selection Page Separately.
            consentData.setHandleAccountSelectionSeparately(true);

            //Set allow multiple accounts to true
            consentData.setAllowMultipleAccounts(true);

            return consentData;
        } catch (Exception e) {
            throw new CdsConsentException(CdsErrorEnum.BAD_REQUEST, "Consent data retrieval failed");
        }
    }

    /**
     * Method to retrieve consumer data from request object.
     * @param jsonRequestBody The JSON object representing the request object of authorization request.
     * @param userId The user id of the authenticated user.
     * @return SuccessResponsePopulateConsentAuthorizeScreenDataConsumerData object containing the consumer data.
     */
    public static SuccessResponsePopulateConsentAuthorizeScreenDataConsumerData cdsConsumerDataRetrieval(
            JSONObject jsonRequestBody, String userId) throws CdsConsentException {

        // Append consumer data to response
        try {
            return validateAndAppendConsumerObjectToResponse(jsonRequestBody, userId);
        } catch (CdsConsentException e) {
            throw new CdsConsentException(CdsErrorEnum.BAD_REQUEST, "Consumer data retrieval failed");
        }
    }

    /**
     * Method to validate and append consumer object to response.
     * @param jsonRequestBody The JSON object representing the request object of authorization request.
     * @param userId The user id of the authenticated user.
     * @return SuccessResponsePopulateConsentAuthorizeScreenDataConsumerData object containing the consumer data.
     */
    public static SuccessResponsePopulateConsentAuthorizeScreenDataConsumerData
    validateAndAppendConsumerObjectToResponse(JSONObject jsonRequestBody, String userId) throws CdsConsentException {

        SuccessResponsePopulateConsentAuthorizeScreenDataConsumerData consumerData =
                new SuccessResponsePopulateConsentAuthorizeScreenDataConsumerData();

        try {

            String accountsURL = ConfigurableProperties.SHARABLE_ENDPOINT;

            if (StringUtils.isNotBlank(accountsURL)) {
                Map<String, String> parameters = new HashMap<>();
                parameters.put(CommonConstants.USER_ID_KEY_NAME, userId);

                String accountData = CommonConsentExtensionUtil.getAccountsFromEndpoint(accountsURL, parameters,
                        new HashMap<>());

                if (StringUtils.isBlank(accountData)) {
                    log.error("Unable to load accounts data for the user: " + userId);
                    throw new CdsConsentException(CdsErrorEnum.UNEXPECTED_ERROR,
                            "Exception occurred while getting accounts data");
                }

                JSONObject jsonAccountData = new JSONObject(accountData);
                JSONArray accountsJSON = (JSONArray) jsonAccountData.get(CommonConstants.DATA);

                //TODO: Consent amendment flow. Mark pre-selected accounts

                jsonRequestBody.put(CommonConstants.ACCOUNTS, accountsJSON);

                List<SuccessResponsePopulateConsentAuthorizeScreenDataConsumerDataAccountsInner> accountList = new ArrayList<>();
                for (int i = 0; i < accountsJSON.length(); i++) {
                    JSONObject accountJson = accountsJSON.getJSONObject(i);

                    SuccessResponsePopulateConsentAuthorizeScreenDataConsumerDataAccountsInner account =
                            new SuccessResponsePopulateConsentAuthorizeScreenDataConsumerDataAccountsInner();

//                    String accountDisplayName = accountJson.getString("displayName") +
//                            "<br>&nbsp;&nbsp;&nbsp;&nbsp;" +
//                            accountJson.getString("accountId");

                    String accountDisplayName = accountJson.getString("displayName");

                    account.setDisplayName(accountDisplayName);

                    accountList.add(account);
                }

                consumerData.setAccounts(accountList);

            } else {
                log.error("Sharable accounts endpoint is not configured properly");
                throw new CdsConsentException(CdsErrorEnum.UNEXPECTED_ERROR,
                        "Sharable accounts endpoint is not configured properly");
            }
        } catch (Exception e) {
            throw new CdsConsentException(CdsErrorEnum.BAD_REQUEST, "Consumer data retrieval failed");
        }
        return consumerData;
    }

    /**
     * Convert the scope string to permission enum list.
     *
     * @param scopeString string containing the requested scopes
     * @return list of permission enums to be stored
     */
    public static List<SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner> getPermissionList(String scopeString) {

        List<SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner> permissionList = new ArrayList<>();

        if (StringUtils.isNotBlank(scopeString)) {
            // Remove "openid" and "cdr:registration" from the scope list
            List<String> filteredScopes = Stream.of(scopeString.split(" "))
                    .filter(x -> (!StringUtils.equalsIgnoreCase(x, CommonConstants.OPENID_SCOPE)
                            && !StringUtils.equalsIgnoreCase(x, CommonConstants.CDR_REGISTRATION_SCOPE)))
                    .collect(Collectors.toList());

            for (String scope : filteredScopes) {
                PermissionsEnum permissionsEnum = PermissionsEnum.fromValue(scope);

                // Create inner object using constructor
                SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner inner =
                        new SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner(
                                permissionsEnum.toString(),           // uid
                                List.of(permissionsEnum.toString())  // displayValues
                        );

                permissionList.add(inner);
            }
        }

        return permissionList;
    }

    /**
     * Method to extract state from query params.
     *
     * @param spQueryParams query params
     * @return state
     */
    public static String getStateParameter(String spQueryParams) {
        String state = null;
        if (spQueryParams != null && !spQueryParams.trim().isEmpty()) {
            String[] spQueryParamList = spQueryParams.split("&");
            for (String param : spQueryParamList) {
                if (param.startsWith("state=")) {
                    state = param.substring("state=".length());
                    break;
                }
            }
        }
        return state;
    }

    /**
     * Set profile scope related individual claims as permissions.
     * @param jsonRequestBody The JSON object representing the request object of authorization request.
     * @param userInfoClaimsObj The object representing the UserInfo claims.
     * @param idTokenClaimsObj The object representing the ID Token claims.
     */
    private static void setClaimPermissions(JSONObject jsonRequestBody, Object userInfoClaimsObj,
                                            Object idTokenClaimsObj) {

        // Cast the Object to a Map. Else, use an empty map to prevent errors.
        Map<String, Object> userInfoClaims = (userInfoClaimsObj instanceof Map)
                ? (Map<String, Object>) userInfoClaimsObj
                : Collections.emptyMap();

        Map<String, Object> idTokenClaims = (idTokenClaimsObj instanceof Map)
                ? (Map<String, Object>) idTokenClaimsObj
                : Collections.emptyMap();

        //Get initial scopes from the request body.
        String initialScopes = jsonRequestBody.optString("scope", "");
        Set<String> scopes = new HashSet<>(Arrays.asList(initialScopes.split("\\s+")));
        scopes.remove("");

        //Define the claim clusters to check.
        List<List<String>> clusters = List.of(
                CommonConstants.NAME_CLUSTER_CLAIMS,
                CommonConstants.PHONE_CLUSTER_CLAIMS,
                CommonConstants.EMAIL_CLUSTER_CLAIMS,
                CommonConstants.MAIL_CLUSTER_CLAIMS
        );

        //Iterate and add claims to the scope if they exist in either map.
        // The rest of the logic remains the same as before.
        for (List<String> cluster : clusters) {
            for (String claim : cluster) {
                if (userInfoClaims.containsKey(claim) || idTokenClaims.containsKey(claim)) {
                    scopes.add(claim);
                }
            }
        }

        //Join the scopes and update the request body.
        String finalScopeString = String.join(" ", scopes);
        jsonRequestBody.put("scope", finalScopeString);
    }

    /**
     * Construct basic consent data for consent authorisation screen.
     * @param expirationDate - expiration date time
     * @param sharingDurationValue - sharing duration value
     * @param permissionsList - list of permissions
     * @return Map of basic consent data
     */
    private static Map<String, List<String>> constructBasicConsentData(String expirationDate, String sharingDurationValue,
                                                                       List<String> permissionsList) {

        Map<String, List<String>> basicConsentData = new HashMap<>();

        basicConsentData.put(CommonConstants.EXPIRATION_DATE_TITLE, Collections.singletonList(expirationDate));
        basicConsentData.put(CommonConstants.PERMISSION_TITLE, permissionsList);
        basicConsentData.put(CommonConstants.SHARING_DURATION_VALUE,
                Collections.singletonList(sharingDurationValue));

        return basicConsentData;
    }

    /**
     * Construct consent metadata map.
     * @param permissionsList - list of permissions
     * @param expirationDate - expiration date time
     * @return Map of consent metadata
     */
    private static Map<String,Object> constructConsentMetadatMap(List<String> permissionsList, String expirationDate) {

        //Build consent metadata as a list of maps
        Map<String, Object> permissionMeta = new LinkedHashMap<>();
        permissionMeta.put(CommonConstants.TITLE, CommonConstants.PERMISSIONS);
        permissionMeta.put(CommonConstants.DATA, permissionsList);

        Map<String, Object> expiryMeta = new LinkedHashMap<>();
        expiryMeta.put(CommonConstants.TITLE, CommonConstants.EXPIRATION_DATE_TIME);
        expiryMeta.put(CommonConstants.DATA, Collections.singletonList(expirationDate));

        //Add permissions and expirationDateTime to the list
        List<Map<String,Object>> consentMetadataList = new ArrayList<>();
        consentMetadataList.add(permissionMeta);
        consentMetadataList.add(expiryMeta);

        //Wrap the list in a map with key "accountData"
        Map<String,Object> dataMap = new LinkedHashMap<>();
        dataMap.put("accountData", consentMetadataList);

        return dataMap;
    }
}

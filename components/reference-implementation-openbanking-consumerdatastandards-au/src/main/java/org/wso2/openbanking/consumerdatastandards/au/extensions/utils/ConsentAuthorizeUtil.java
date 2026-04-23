/**
 * Copyright (c) 2025-2026, WSO2 LLC. (https://www.wso2.com).
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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.openbanking.consumerdatastandards.au.extensions.configurations.ConfigurableProperties;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.CdsErrorEnum;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.CommonConstants;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.PermissionsEnum;
import org.wso2.openbanking.consumerdatastandards.au.extensions.exceptions.CdsConsentException;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.AdditionalData;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.AdditionalDataItem;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.SuccessResponsePopulateConsentAuthorizeScreenDataConsentData;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.SuccessResponsePopulateConsentAuthorizeScreenDataConsumerData;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.SuccessResponsePopulateConsentAuthorizeScreenDataConsumerDataAccountsInner;

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
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class for consent authorization related operations.
 */
public class ConsentAuthorizeUtil {

    private static final Log log = LogFactory.getLog(ConsentAuthorizeUtil.class);


    /**
     * Calculate consent expiry date time based on sharing duration.
     * @param sharingDuration the duration in seconds for which the consent is valid
     * @return the calculated consent expiry date and time as an {@link OffsetDateTime}
     */
    public static OffsetDateTime getConsentExpiryDateTime(long sharingDuration) {

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
            Map<String, Object> consentMetadatMap = constructConsentMetadatMap(permissionsList, expirationDate);
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
        } catch (JSONException e) {
            log.error("Consent data retrieval failed", e);

            throw new CdsConsentException(CdsErrorEnum.BAD_REQUEST, "Consent data retrieval failed");
        }
    }

    /**
     * Method to retrieve consumer data from request object.
     * @param jsonRequestBody The JSON object representing the request object of authorization request.
     * @param userId The user id of the authenticated user.
     * @param consumerData Consumer data model to be populated.
     * @param displayData Display data model to be populated.
     */
    public static void cdsConsumerDataRetrieval(JSONObject jsonRequestBody, String userId,
            SuccessResponsePopulateConsentAuthorizeScreenDataConsumerData consumerData,
            List<AdditionalData> displayData) throws CdsConsentException {

        // Append consumer data to response
        try {
            validateAndAppendConsumerObjectToResponse(jsonRequestBody, userId, consumerData, displayData);
        } catch (CdsConsentException e) {
            throw new CdsConsentException(CdsErrorEnum.BAD_REQUEST, "Consumer data retrieval failed");
        }
    }

    /**
     * Checks if a secondary account has privilege status.
     * @param accountJson The account JSON object containing secondary account privilege status
     * @return true if the account has privilege (secondaryAccountPrivilegeStatus is true), false otherwise
     */
    private static boolean isSecondaryAccountPrivileged(JSONObject accountJson) {
        return accountJson.optBoolean(CommonConstants.SECONDARY_ACCOUNT_PRIVILEGES_STATUS, false);
    }

    /**
     * Checks whether the secondary account instruction status is active.
     * If no record exists in the map the account is allowed through by default —
     * absence of an instruction record means no override has been set.
     *
     * @param accountId              the account ID to check
     * @param instructionStatusMap   map of accountId to instruction status returned by the batch call
     * @return true if the instruction status is active or absent, false if explicitly inactive
     */
    private static boolean isSecondaryAccountInstructionActive(String accountId,
            Map<String, String> instructionStatusMap) {
        if (instructionStatusMap == null || !instructionStatusMap.containsKey(accountId)) {
            return true;
        }
        return CommonConstants.SECONDARY_INSTRUCTION_STATUS_ACTIVE
                .equalsIgnoreCase(instructionStatusMap.get(accountId));
    }

    /**
     * Checks whether a secondary account is eligible for consent authorization.
     * An account is eligible only if it is both privileged and has an active instruction status.
     *
     * @param accountJson            the account JSON containing privilege status
     * @param instructionStatusMap   map of accountId to instruction status returned by the batch call
     * @return true if the account is privileged and its instruction status is active or absent
     */
    private static boolean isSecondaryAccountEligible(JSONObject accountJson,
                                                      Map<String, String> instructionStatusMap) {
        String accountId = accountJson.getString(CommonConstants.ACCOUNT_ID);
        return isSecondaryAccountPrivileged(accountJson)
                && isSecondaryAccountInstructionActive(accountId, instructionStatusMap);
    }

    /**
     * Checks if a joint account is electable based on its election status.
     * @param accountJson The account JSON object containing joint account election status
     * @return true if the account is electable (not in NOT_ELECTED status), false otherwise
     */
    private static boolean isJointAccountElectable(JSONObject accountJson) {
        return !CommonConstants.JOINT_ACCOUNT_ELECTION_STATUS_NOT_ELECTED
            .equalsIgnoreCase(accountJson.optString(CommonConstants.JOINT_ACCOUNT_CONSENT_ELECTION_STATUS, ""));
    }

    /**
     * Extracts the linked member IDs from a joint account's JSON data.
     * @param accountJson The account JSON object containing joint account info
     * @return list of linked member IDs, or an empty list if none are found
     */
    private static List<String> extractLinkedMembers(JSONObject accountJson) {

        List<String> linkedMembers = new ArrayList<>();

        if (accountJson.has(CommonConstants.JOINT_ACCOUNT_INFO_TAG)) {
            JSONArray linkedMemberArray = accountJson.getJSONObject(
                    CommonConstants.JOINT_ACCOUNT_INFO_TAG).optJSONArray(
                            CommonConstants.LINKED_MEMBER_TAG_IN_SHARABLE_ENDPOINT);
            if (linkedMemberArray != null) {
                for (int j = 0; j < linkedMemberArray.length(); j++) {
                    linkedMembers.add(linkedMemberArray.getJSONObject(j).optString(CommonConstants.MEMBER_ID_TAG));
                }
            }
        }
        return linkedMembers;
    }

    /**
     * Extracts the account owner IDs from a secondary account's JSON data.
     * @param accountJson The account JSON object containing secondary account info
     * @return list of account owner IDs, or an empty list if none are found
     */
    private static List<String> extractSecondaryAccountOwners(JSONObject accountJson) {
        List<String> accountOwners = new ArrayList<>();
        if (accountJson.has(CommonConstants.SECONDARY_ACCOUNT_INFO_TAG)) {
            JSONArray ownerArray = accountJson.getJSONObject(CommonConstants.SECONDARY_ACCOUNT_INFO_TAG)
                    .optJSONArray(CommonConstants.SECONDARY_ACCOUNT_OWNER_TAG_IN_SHARABLE_ENDPOINT);
            if (ownerArray != null) {
                for (int j = 0; j < ownerArray.length(); j++) {
                    accountOwners.add(ownerArray.getJSONObject(j).optString(CommonConstants.MEMBER_ID_TAG));
                }
            }
        }
        return accountOwners;
    }

    /**
     * Processes a single account by checking eligibility and enriching with type-specific properties.
     * Eligibility rules:
     *   Joint accounts must be electable (election status is not NOT_ELECTED)
     *   Secondary accounts must have privilege status
     *   Accounts that are both joint and secondary must satisfy both conditions
     *   Normal accounts (neither joint nor secondary) are always eligible
     * If any eligibility check fails, the account is added to the blocked list.
     * Otherwise, the account is enriched with linked members / secondary account owners
     * as appropriate and added to the eligible account list. For secondary accounts,
     * other-accounts availability is also included based on whether the secondary user has more than one account.
     * @param accountJson The account JSON object
     * @param account The account object to be populated
     * @param accountList The list of eligible accounts
     * @param blockedAccountsList The list of blocked accounts
     * @param hasMultipleAccounts hasMultipleAccounts Whether the authenticated user has multiple accounts
     * @param secondaryInstructionStatusMap map of accountId to secondary account instruction status,
     *                                      fetched once before the loop
     * */
    private static void processAccount(
            JSONObject accountJson, SuccessResponsePopulateConsentAuthorizeScreenDataConsumerDataAccountsInner account,
            List<SuccessResponsePopulateConsentAuthorizeScreenDataConsumerDataAccountsInner> accountList,
            List<AdditionalDataItem> blockedAccountsList,
            boolean hasMultipleAccounts,
            Map<String, String> secondaryInstructionStatusMap) {

        String accountId = accountJson.getString(CommonConstants.ACCOUNT_ID);
        boolean isJointAccount = accountJson.optBoolean(CommonConstants.IS_JOINT_ACCOUNT_RESPONSE, false);
        boolean isSecondaryAccount = accountJson.optBoolean(CommonConstants.IS_SECONDARY_ACCOUNT_RESPONSE, false);

        // Check eligibility for each account.
        if (!(!isJointAccount || isJointAccountElectable(accountJson)) ||
                !(!isSecondaryAccount || isSecondaryAccountEligible(accountJson, secondaryInstructionStatusMap))) {
            // Block account if any eligibility check fails
            AdditionalDataItem blockedItem = new AdditionalDataItem();
            blockedItem.setItem(getDisplayNameWithAccountNumber(
                    accountJson.getString(CommonConstants.DISPLAY_NAME), accountId));
            blockedAccountsList.add(blockedItem);
            return;
        }

        List<String> linkedMembers = Collections.emptyList();

        // Enrich with type-specific additional properties
        if (isJointAccount) {
            linkedMembers = extractLinkedMembers(accountJson);
            account.setAdditionalProperty(CommonConstants.LINKED_MEMBERS, linkedMembers);
        }
        if (isSecondaryAccount) {
            account.setAdditionalProperty(CommonConstants.SECONDARY_ACCOUNT_OWNERS_TAG,
                    extractSecondaryAccountOwners(accountJson));
            account.setAdditionalProperty(CommonConstants.OTHER_ACCOUNTS_AVAILABILITY_FIELD, hasMultipleAccounts);
        }

        if (isJointAccount && !isSecondaryAccount) {
            account.setTitle(CommonConstants.JOINT_ACCOUNT_TOOLTIP_TITLE);
            account.setDescription(buildJointAccountTooltipDescription(linkedMembers.size()));
        }

        account.setDisplayName(getDisplayNameWithAccountNumber(
                accountJson.getString(CommonConstants.DISPLAY_NAME), accountId));
        accountList.add(account);
    }

    /**
     * Builds tooltip description text for selectable joint accounts.
     *
     * @param linkedMembersCount number of linked members for the account
     * @return formatted tooltip description
     */
    private static String buildJointAccountTooltipDescription(int linkedMembersCount) {
        return linkedMembersCount
                + " other account holder(s) can share this joint account data at any time,"
                + " without each other&rsquo;s permission. <br/><br/>"
                + " You can change sharing preferences for this account by going to"
                + " &lsquo;Settings &gt;Data sharing &gt; Account permissions&rsquo;";
    }

    /**
     * Method to validate and append consumer object to response.
     * @param jsonRequestBody The JSON object representing the request object of authorization request.
     * @param userId The user id of the authenticated user.
     * @param consumerData Consumer data model to be populated.
     * @param displayData Display data model to be populated.
     */
    public static void validateAndAppendConsumerObjectToResponse(
            JSONObject jsonRequestBody, String userId,
            SuccessResponsePopulateConsentAuthorizeScreenDataConsumerData consumerData,
            List<AdditionalData> displayData) throws CdsConsentException {
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
                boolean hasMultipleAccounts = accountsJSON.length() > 1;

                //TODO: Consent amendment flow. Mark pre-selected accounts

                jsonRequestBody.put(CommonConstants.ACCOUNTS, accountsJSON);

                // Collect all secondary account IDs in one pass for a single batch lookup
                List<String> secondaryAccountIds = new ArrayList<>();
                for (int i = 0; i < accountsJSON.length(); i++) {
                    JSONObject accountJson = accountsJSON.getJSONObject(i);
                    if (accountJson.optBoolean(CommonConstants.IS_SECONDARY_ACCOUNT_RESPONSE, false)) {
                        secondaryAccountIds.add(accountJson.getString(CommonConstants.ACCOUNT_ID));
                    }
                }

                Map<String, String> secondaryInstructionStatusMap =
                        AccountMetadataUtil.getSecondaryAccountInstructionStatusesForAccounts(
                                secondaryAccountIds, userId);

                List<SuccessResponsePopulateConsentAuthorizeScreenDataConsumerDataAccountsInner> accountList =
                    new ArrayList<>();

                List<AdditionalDataItem> blockedAccountsList = new ArrayList<>();

                for (int i = 0; i < accountsJSON.length(); i++) {

                    SuccessResponsePopulateConsentAuthorizeScreenDataConsumerDataAccountsInner account =
                            new SuccessResponsePopulateConsentAuthorizeScreenDataConsumerDataAccountsInner();
                    JSONObject accountJson = accountsJSON.getJSONObject(i);

                    processAccount(accountJson, account, accountList, blockedAccountsList,
                            hasMultipleAccounts, secondaryInstructionStatusMap);
                }

                List<AdditionalData> resolvedDisplayData = setDisplayData(blockedAccountsList);
                displayData.clear();
                displayData.addAll(resolvedDisplayData);
                consumerData.setAccounts(accountList);
            } else {
                log.error("Sharable accounts endpoint is not configured properly");
                throw new CdsConsentException(CdsErrorEnum.UNEXPECTED_ERROR,
                        "Sharable accounts endpoint is not configured properly");
            }
        } catch (CdsConsentException e) {
            throw new CdsConsentException(CdsErrorEnum.BAD_REQUEST, "Consumer data retrieval failed");
        }
    }

    /**
     * Combines display name with a masked account number for display purposes.
     * @param displayName The account display name
     * @param accountId The account ID to be masked and appended
     * @return A formatted string containing display name and masked account number separated by line break
     */
    protected static String getDisplayNameWithAccountNumber(String displayName, String accountId) {
        return displayName + "<br>" + getDisplayableAccountNumber(accountId);
    }

    /**
     * Account number masking is performed in this method. Logic is executed when the account ID length is 2 or higher.
     * The logic is handled like this because the specification doesn't mention the exact length of an account ID.
     * <p>
     * If the account ID length is less than 4, mask all but the last character.
     * If the account ID length is exactly 4, mask all but the last two characters.
     * If the length is greater than 4, mask all but the last 4 characters.
     *
     * @param accountId plain account id.
     * @return account number in the displayable masked format.
     */
    protected static String getDisplayableAccountNumber(String accountId) {
        int accountIdLength = accountId.length();

        if (accountIdLength > 1) {
            if (accountIdLength < 4) {
                // If the length is less than 4, mask all but the last character
                String maskedPart = StringUtils.repeat('*', accountIdLength - 1);
                String visiblePart = StringUtils.right(accountId, 1);
                return maskedPart + visiblePart;
            } else if (accountIdLength == 4) {
                // If the length is exactly 4, mask all but the last two characters
                return "**" + StringUtils.right(accountId, 2);
            } else {
                // If the length is greater than 4, mask all but the last 4 characters
                String maskedPart = StringUtils.repeat('*', accountIdLength - 4);
                String visiblePart = StringUtils.right(accountId, 4);
                return maskedPart + visiblePart;
            }
        }

        return accountId;
    }

    /**
     * Creates and populates display data for blocked/unavailable accounts.
     *
     * @param blockedAccountsList List of blocked accounts to be displayed
     * @return List of display data sections
     * containing display information for blocked accounts
     */
    private static List<AdditionalData> setDisplayData(List<AdditionalDataItem> blockedAccountsList) {
        List<AdditionalData> displayData = new ArrayList<>();

        AdditionalData item = new AdditionalData();

        // Always initialize the list to avoid nulls in the UI layer
        List<AdditionalDataItem> safeList = (blockedAccountsList != null) ? blockedAccountsList :
                Collections.emptyList();

        item.setItems(safeList);

        // Set UI metadata
        item.setTitle(CommonConstants.AUTH_SCREEN_UNAVAILABLE_ACCOUNTS_HEADING);
        item.setSubtitle(CommonConstants.AUTH_SCREEN_UNAVAILABLE_ACCOUNTS_SUB_HEADING);
        item.setDescription(CommonConstants.AUTH_SCREEN_UNAVAILABLE_ACCOUNTS_TOOLTIP_DESCRIPTION);

        displayData.add(item);

        return displayData;
    }


    /**
     * Convert the scope string to permission enum list.
     *
     * @param scopeString string containing the requested scopes
     * @return list of permission enums to be stored
     */
    public static List<SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner> getPermissionList(
            String scopeString) {

        List<SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner> permissionList =
                new ArrayList<>();

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
    private static Map<String, List<String>> constructBasicConsentData(String expirationDate,
                                                                       String sharingDurationValue,
                                                                       List<String> permissionsList) {

        Map<String, List<String>> basicConsentData = new HashMap<>();

        basicConsentData.put(CommonConstants.EXPIRATION_DATE_TITLE, Collections.singletonList(expirationDate));
        basicConsentData.put(CommonConstants.PERMISSION_TITLE, permissionsList);
        basicConsentData.put(CommonConstants.SHARING_DURATION_DISPLAY_VALUE,
                Collections.singletonList(buildSharingDurationMessage(Long.parseLong(sharingDurationValue))));

        return basicConsentData;
    }

    /**
     * Builds a user-friendly message describing how long data sharing
     * will continue based on a duration given in seconds.
     * Converts seconds → hours and handles singular/plural wording.
     *
     * @param seconds duration in seconds
     * @return message indicating sharing duration in hours
     */
    private static String buildSharingDurationMessage(long seconds) {

        // Convert seconds into hours (integer division)
        long hours = seconds / 3600;

        // Construct message with correct pluralization
        return "Your data will be shared on-going basis for " + hours + " hour" + (hours == 1 ? "" : "s") + ".";
    }

    /**
     * Construct consent metadata map.
     * @param permissionsList - list of permissions
     * @param expirationDate - expiration date time
     * @return Map of consent metadata
     */
    private static Map<String, Object> constructConsentMetadatMap(List<String> permissionsList, String expirationDate) {

        //Build consent metadata as a list of maps
        Map<String, Object> permissionMeta = new LinkedHashMap<>();
        permissionMeta.put(CommonConstants.TITLE, CommonConstants.PERMISSIONS);
        permissionMeta.put(CommonConstants.DATA, permissionsList);

        Map<String, Object> expiryMeta = new LinkedHashMap<>();
        expiryMeta.put(CommonConstants.TITLE, CommonConstants.EXPIRATION_DATE_TIME);
        expiryMeta.put(CommonConstants.DATA, Collections.singletonList(expirationDate));

        //Add permissions and expirationDateTime to the list
        List<Map<String, Object>> consentMetadataList = new ArrayList<>();
        consentMetadataList.add(permissionMeta);
        consentMetadataList.add(expiryMeta);

        //Wrap the list in a map with key "accountData"
        Map<String, Object> dataMap = new LinkedHashMap<>();
        dataMap.put("accountData", consentMetadataList);

        return dataMap;
    }
}

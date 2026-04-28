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
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.StoredAuthorization;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.StoredDetailedConsentResourceData;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.StoredResource;
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
            // When profile selection is enabled, ship both individual and organisation cluster variants in the same
            // basicConsentData map by prefixing each key with its profile marker (e.g.
            // "[individual]Name and occupation"). The profile-selection JSP strips the prefix and keeps only the
            // entries matching the picked profile before forwarding to the consent confirmation page. When profile
            // selection is disabled, the legacy single-variant (individual) shape is preserved unchanged.
            List<String> scopesForClusters = Arrays.asList(scopesString.split("\\s+"));
            Map<String, List<String>> basicConsentData;
            if (ConfigurableProperties.PROFILE_SELECTION_PAGE_ENABLED) {
                Map<String, List<String>> individualVariant = constructBasicConsentData(scopesForClusters,
                        CommonConstants.INDIVIDUAL_PROFILE_TYPE, expirationDate, sharingDurationValue);
                Map<String, List<String>> organisationVariant = constructBasicConsentData(scopesForClusters,
                        CommonConstants.ORGANISATION, expirationDate, sharingDurationValue);
                basicConsentData = new LinkedHashMap<>();
                for (Map.Entry<String, List<String>> entry : individualVariant.entrySet()) {
                    basicConsentData.put(CommonConstants.BASIC_CONSENT_DATA_INDIVIDUAL_PREFIX + entry.getKey(),
                            entry.getValue());
                }
                for (Map.Entry<String, List<String>> entry : organisationVariant.entrySet()) {
                    basicConsentData.put(CommonConstants.BASIC_CONSENT_DATA_ORGANISATION_PREFIX + entry.getKey(),
                            entry.getValue());
                }
            } else {
                basicConsentData = constructBasicConsentData(scopesForClusters,
                        CommonConstants.INDIVIDUAL_PROFILE_TYPE, expirationDate, sharingDurationValue);
            }
            consentData.setBasicConsentData(basicConsentData);

            //Set Consent MetaData to Consent Data
            Map<String, Object> consentMetadatMap = constructConsentMetadatMap(permissionsList, expirationDate);
            consentData.setConsentMetadata(consentMetadatMap);

            //Set additional properties to Consent Data
            consentData.setAdditionalProperty(CommonConstants.USERINFO_CLAIMS,
                    Collections.singletonList(userInfoClaim.toString()));
            consentData.setAdditionalProperty(CommonConstants.ID_TOKEN_CLAIMS,
                    Collections.singletonList(idTokenClaim.toString()));

            if (!ConfigurableProperties.PROFILE_SELECTION_PAGE_ENABLED) {
                //Specify to handle Account Selection Page Separately.
                consentData.setHandleAccountSelectionSeparately(true);
            }

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
     * @param consentResource The existing consent resource, used to pre-select previously authorized accounts.
     */
    public static void cdsConsumerDataRetrieval(JSONObject jsonRequestBody, String userId,
                                                SuccessResponsePopulateConsentAuthorizeScreenDataConsumerData consumerData,
                                                List<AdditionalData> displayData, StoredDetailedConsentResourceData consentResource)
            throws CdsConsentException {

        Set<String> preSelectedAccountIds = extractPreSelectedAccountIds(consentResource);

        // Append consumer data to response
        try {
            validateAndAppendConsumerObjectToResponse(jsonRequestBody, userId, consumerData, displayData,
                    preSelectedAccountIds);
        } catch (CdsConsentException e) {
            throw new CdsConsentException(CdsErrorEnum.BAD_REQUEST, "Consumer data retrieval failed");
        }
    }

    private static Set<String> extractPreSelectedAccountIds(StoredDetailedConsentResourceData consentResource) {
        if (consentResource == null || consentResource.getAuthorizations() == null) {
            return Collections.emptySet();
        }
        Set<String> accountIds = new HashSet<>();
        for (StoredAuthorization auth : consentResource.getAuthorizations()) {
            if (auth.getResources() == null) {
                continue;
            }
            for (StoredResource resource : auth.getResources()) {
                if (StringUtils.isNotBlank(resource.getAccountId())) {
                    accountIds.add(resource.getAccountId());
                }
            }
        }
        return accountIds;
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
     * Checks if a joint account is electable based on its election status.
     * @param accountJson The account JSON object containing joint account election status
     * @return true if the account is electable (not in ELECTED status), false otherwise
     */
    private static boolean isJointAccountElectable(JSONObject accountJson) {
        return !CommonConstants.JOINT_ACCOUNT_ELECTION_STATUS_NOT_ELECTED
                .equalsIgnoreCase(accountJson.optString(CommonConstants.JOINT_ACCOUNT_CONSENT_ELECTION_STATUS, ""));
    }

    /**
     * Checks whether a business account is eligible for the authenticated user.
     *
     * A business account is eligible only if the authenticated user appears in
     * businessAccountInfo.NominatedRepresentatives[].memberId.
     *
     * @param accountJson account JSON payload
     * @param userId authenticated user id
     * @return true when the user is a nominated representative
     */
    private static boolean isBusinessAccountEligible(JSONObject accountJson, String userId) {

        JSONObject businessInfo = accountJson.optJSONObject(CommonConstants.BUSINESS_ACCOUNT_INFO_TAG);
        if (businessInfo == null) {
            return false;
        }

        JSONArray representatives = businessInfo.optJSONArray(CommonConstants.NOMINATED_REPRESENTATIVES_TAG);
        if (representatives != null && StringUtils.isNotBlank(userId)) {
            for (int i = 0; i < representatives.length(); i++) {
                JSONObject representative = representatives.optJSONObject(i);
                if (representative == null) {
                    continue;
                }

                String memberId = representative.optString(CommonConstants.MEMBER_ID_TAG, "");

                // Compare after removing tenant domain (e.g., "@carbon.super") from memberId
                String normalizedMemberId = memberId.contains("@")
                        ? memberId.substring(0, memberId.lastIndexOf("@"))
                        : memberId;

                if (normalizedMemberId.equalsIgnoreCase(userId)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Determines whether an account is eligible to be shown for consent selection.
     *
     * @param accountJson account JSON payload
     * @param isJointAccount whether account is joint
     * @param isSecondaryAccount whether account is secondary
     * @param isBusinessAccount whether account is business
     * @param userId authenticated user id
     * @param secondaryInstructionStatusMap map of accountId to secondary account instruction status,
     * @return true if account passes all eligibility checks
     */
    private static boolean isAccountEligible(JSONObject accountJson, boolean isJointAccount, boolean isSecondaryAccount,
                                             boolean isBusinessAccount, String userId,
                                             Map<String, String> secondaryInstructionStatusMap) {
        return (!isJointAccount || isJointAccountElectable(accountJson))
                && (!isSecondaryAccount || isSecondaryAccountEligible(accountJson, secondaryInstructionStatusMap))
                && (!isBusinessAccount || isBusinessAccountEligible(accountJson, userId));
    }

    /**
     * Extracts business account owners from account payload.
     *
     * @param accountJson account JSON payload
     * @return list of business account owner member ids
     */
    private static List<String> extractBusinessAccountOwners(JSONObject accountJson) {
        return extractBusinessMemberIds(accountJson, CommonConstants.ACCOUNT_OWNERS_TAG, null);
    }

    /**
     * Extracts nominated representatives excluding the authenticated user.
     *
     * @param accountJson account JSON payload
     * @param userId authenticated user id
     * @return list of nominated representative member ids excluding userId
     */
    private static List<String> extractNominatedRepresentativesExcludingUser(JSONObject accountJson, String userId) {
        return extractBusinessMemberIds(accountJson, CommonConstants.NOMINATED_REPRESENTATIVES_TAG, userId);
    }

    /**
     * Extracts member ids from a business account info section.
     *
     * @param accountJson account JSON payload
     * @param arrayTag businessAccountInfo array key
     * @param excludedUserId optional user id to exclude from results
     * @return list of member ids from the requested section
     */
    private static List<String> extractBusinessMemberIds(JSONObject accountJson, String arrayTag,
                                                         String excludedUserId) {

        List<String> memberIds = new ArrayList<>();
        JSONObject businessInfo = accountJson.optJSONObject(CommonConstants.BUSINESS_ACCOUNT_INFO_TAG);
        if (businessInfo == null) {
            return memberIds;
        }

        JSONArray members = businessInfo.optJSONArray(arrayTag);
        if (members == null) {
            return memberIds;
        }

        for (int i = 0; i < members.length(); i++) {
            JSONObject member = members.optJSONObject(i);
            if (member == null) {
                continue;
            }

            String memberId = StringUtils.trimToEmpty(member.optString(CommonConstants.MEMBER_ID_TAG, ""));

            if (StringUtils.isNotBlank(excludedUserId) && memberId.equalsIgnoreCase(excludedUserId)) {
                continue;
            }

            memberIds.add(memberId);
        }

        return memberIds;
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
     * @param userId authenticated user id
     * @param secondaryInstructionStatusMap map of accountId to secondary account instruction status,
     *                                      fetched once before the loop
     * */
    private static void processAccount(
            JSONObject accountJson, SuccessResponsePopulateConsentAuthorizeScreenDataConsumerDataAccountsInner account,
            List<SuccessResponsePopulateConsentAuthorizeScreenDataConsumerDataAccountsInner> accountList,
            List<AdditionalDataItem> blockedAccountsList, String userId,
            Map<String, String> secondaryInstructionStatusMap,
            Map<String, Boolean> blockedSecondaryAccountsByLegalEntity,
            Set<String> preSelectedAccountIds) {

        String accountId = accountJson.getString(CommonConstants.ACCOUNT_ID);
        boolean isJointAccount = accountJson.optBoolean(CommonConstants.IS_JOINT_ACCOUNT_RESPONSE, false);
        boolean isSecondaryAccount = accountJson.optBoolean(CommonConstants.IS_SECONDARY_ACCOUNT_RESPONSE, false);
        boolean isBusinessAccount = CommonConstants.BUSINESS_ACCOUNT_TYPE.equalsIgnoreCase(
                accountJson.optString(CommonConstants.CUSTOMER_ACCOUNT_TYPE, ""));
        boolean blockedByLegalEntity = isSecondaryAccount
                && blockedSecondaryAccountsByLegalEntity.getOrDefault(accountId, false);

        // Check eligibility for each account and block account if any eligibility check fails
        if (blockedByLegalEntity
                || !isAccountEligible(accountJson, isJointAccount, isSecondaryAccount, isBusinessAccount, userId,
                secondaryInstructionStatusMap)) {
            // Block account if any eligibility check fails
            AdditionalDataItem blockedItem = new AdditionalDataItem();
            blockedItem.setItem(accountJson.getString(CommonConstants.DISPLAY_NAME));

            if (isBusinessAccount && ConfigurableProperties.PROFILE_SELECTION_PAGE_ENABLED) {
                blockedItem.setType(accountJson.optString(CommonConstants.PROFILE_ID_RESPONSE_TAG, ""));
            }

            blockedAccountsList.add(blockedItem);
            return;
        }

        // Enrich with type-specific additional properties
        List<String> linkedMembers = Collections.emptyList();

        // Setting Linked member Details for joint accounts.
        if (isJointAccount && !isSecondaryAccount) {
            linkedMembers = extractLinkedMembers(accountJson);
            account.setAdditionalProperty(CommonConstants.LINKED_MEMBERS, linkedMembers);
            account.setTitle(CommonConstants.JOINT_ACCOUNT_TOOLTIP_TITLE);
            account.setDescription(buildJointAccountTooltipDescription(linkedMembers.size()));
        }

        // Setting secondary account owners data for secondary accounts.
        if (isSecondaryAccount) {
            account.setAdditionalProperty(CommonConstants.SECONDARY_ACCOUNT_OWNERS_TAG,
                    extractSecondaryAccountOwners(accountJson));
        }

        // Setting BNR and account owners data for business accounts.
        if (isBusinessAccount) {
            account.setAdditionalProperty(CommonConstants.ACCOUNT_OWNERS_TAG,
                    extractBusinessAccountOwners(accountJson));
            account.setAdditionalProperty(CommonConstants.NOMINATED_REPRESENTATIVES_TAG,
                    extractNominatedRepresentativesExcludingUser(accountJson, userId));

            // add profile data if profile selection page is enabled.
            if (ConfigurableProperties.PROFILE_SELECTION_PAGE_ENABLED) {
                account.setAdditionalProperty(CommonConstants.PROFILE_ID_TAG
                        , accountJson.optString(CommonConstants.PROFILE_ID_RESPONSE_TAG, ""));
                account.setAdditionalProperty(CommonConstants.PROFILE_NAME_TAG,
                        accountJson.optString(CommonConstants.PROFILE_NAME_RESPONSE_TAG, ""));
            }
        }

        account.setSelected(preSelectedAccountIds.contains(accountId));
        account.setDisplayName(getDisplayNameWithAccountNumber(accountJson.getString(CommonConstants.DISPLAY_NAME),
                accountId));
        accountList.add(account);
    }

    /**
     * Build accountId -> blocked map for secondary accounts based on legal entity sharing status.
     *
     * @param accountsJSON accounts payload
     * @param userId authenticated user id
     * @param clientId software product client id
     * @return map of secondary accountId to blocked status for client's legal entity
     */
    private static Map<String, Boolean> buildSecondaryAccountLegalEntityBlockedMap(JSONArray accountsJSON,
                                                                                   String userId, String clientId) throws CdsConsentException {
        if (accountsJSON == null || StringUtils.isBlank(userId) || StringUtils.isBlank(clientId)) {
            return new HashMap<>();
        }

        List<String> secondaryAccountIds = extractSecondaryAccountIds(accountsJSON);
        if (secondaryAccountIds.isEmpty()) {
            return new HashMap<>();
        }

        return AccountMetadataUtil.getSecondaryAccountBlockedByLegalEntityMap(secondaryAccountIds, userId,
                clientId);
    }

    private static List<String> extractSecondaryAccountIds(JSONArray accountsJSON) {
        List<String> secondaryAccountIds = new ArrayList<>();
        for (int i = 0; i < accountsJSON.length(); i++) {
            JSONObject accountJson = accountsJSON.optJSONObject(i);
            if (accountJson == null) {
                continue;
            }

            if (!accountJson.optBoolean(CommonConstants.IS_SECONDARY_ACCOUNT_RESPONSE, false)) {
                continue;
            }

            String accountId = StringUtils.trimToEmpty(accountJson.optString(CommonConstants.ACCOUNT_ID, ""));
            if (StringUtils.isNotBlank(accountId)) {
                secondaryAccountIds.add(accountId);
            }
        }
        return secondaryAccountIds;
    }

    private static String getClientIdFromRequestBody(JSONObject jsonRequestBody) {
        String snakeCaseClientId = StringUtils.trimToEmpty(
                jsonRequestBody.optString(CommonConstants.CLIENT_ID, StringUtils.EMPTY));
        if (StringUtils.isNotBlank(snakeCaseClientId)) {
            return snakeCaseClientId;
        }
        return StringUtils.trimToEmpty(jsonRequestBody.optString("clientId", StringUtils.EMPTY));
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
     * @param preSelectedAccountIds Account IDs already authorized in the existing consent.
     */
    public static void validateAndAppendConsumerObjectToResponse(
            JSONObject jsonRequestBody, String userId,
            SuccessResponsePopulateConsentAuthorizeScreenDataConsumerData consumerData,
            List<AdditionalData> displayData, Set<String> preSelectedAccountIds) throws CdsConsentException {
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
                    JSONObject accountJson = accountsJSON.getJSONObject(i);
                    if (accountJson.optBoolean(CommonConstants.IS_SECONDARY_ACCOUNT_RESPONSE, false)) {
                        secondaryAccountIds.add(accountJson.getString(CommonConstants.ACCOUNT_ID));
                    }
                }

                String clientId = getClientIdFromRequestBody(jsonRequestBody);
                Map<String, Boolean> blockedSecondaryAccountsByLegalEntity =
                        buildSecondaryAccountLegalEntityBlockedMap(accountsJSON, userId, clientId);

                for (int i = 0; i < accountsJSON.length(); i++) {
                    SuccessResponsePopulateConsentAuthorizeScreenDataConsumerDataAccountsInner account =
                            new SuccessResponsePopulateConsentAuthorizeScreenDataConsumerDataAccountsInner();
                    JSONObject accountJson = accountsJSON.getJSONObject(i);
                    processAccount(accountJson, account, accountList, blockedAccountsList, userId,
                            secondaryInstructionStatusMap, blockedSecondaryAccountsByLegalEntity,
                            preSelectedAccountIds);
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
     *
     * Builds an ordered map keyed by cluster title (e.g. "Account name, type, and balance"), each mapped to its
     * list of human-readable data items, followed by the expiration date and sharing period entries. The JSP
     * include {@code basic-consent-data.jsp} renders each entry as a bold heading with a bullet list of values,
     * so the cluster title-per-entry shape produces the grouped UI directly.
     *
     * @param scopes               requested scopes (after profile-claim enrichment)
     * @param customerType         {@link CommonConstants#INDIVIDUAL_PROFILE_TYPE} or
     *                             {@link CommonConstants#ORGANISATION}, controls common:* cluster selection
     * @param expirationDate       expiration date time string
     * @param sharingDurationValue sharing duration in seconds (string)
     * @return Map of basic consent data with deterministic insertion order
     */
    private static Map<String, List<String>> constructBasicConsentData(List<String> scopes, String customerType,
                                                                       String expirationDate,
                                                                       String sharingDurationValue) {

        Map<String, List<String>> basicConsentData = new LinkedHashMap<>();

        // Cluster entries first so they appear at the top of the consent screen.
        for (Map.Entry<String, List<String>> cluster : getDataClusterFromScopes(scopes, customerType)) {
            basicConsentData.put(cluster.getKey(), cluster.getValue());
        }
        for (Map.Entry<String, List<String>> cluster : processProfileClusters(scopes)) {
            basicConsentData.put(cluster.getKey(), cluster.getValue());
        }

        basicConsentData.put(CommonConstants.EXPIRATION_DATE_TITLE, Collections.singletonList(expirationDate));
        basicConsentData.put(CommonConstants.SHARING_DURATION_DISPLAY_VALUE,
                Collections.singletonList(buildSharingDurationMessage(Long.parseLong(sharingDurationValue))));

        return basicConsentData;
    }

    /**
     * Map each requested scope to its {@code (cluster title, data items)} pair using the cluster definitions in
     * {@link CommonConstants}. Skips {@code common:customer.basic:read} when {@code common:customer.detail:read}
     * is also present (and the equivalent for the bank accounts pair) so the more detailed cluster wins.
     * Profile/contact claims are handled separately by {@link #processProfileClusters(List)}.
     *
     * @param scopes       requested scopes
     * @param customerType {@link CommonConstants#ORGANISATION} selects the business common:* clusters,
     *                     anything else selects the individual common:* clusters
     * @return ordered list of cluster entries, one per scope that maps to a cluster
     */
    private static List<Map.Entry<String, List<String>>> getDataClusterFromScopes(List<String> scopes,
                                                                                  String customerType) {

        List<Map.Entry<String, List<String>>> dataClusters = new ArrayList<>();

        for (String scope : scopes) {
            if (CommonConstants.COMMON_CUSTOMER_BASIC_READ_SCOPE.equalsIgnoreCase(scope)
                    && scopes.contains(CommonConstants.COMMON_CUSTOMER_DETAIL_READ_SCOPE)) {
                continue;
            }
            if (CommonConstants.COMMON_ACCOUNTS_BASIC_READ_SCOPE.equalsIgnoreCase(scope)
                    && scopes.contains(CommonConstants.COMMON_ACCOUNTS_DETAIL_READ_SCOPE)) {
                continue;
            }

            Map<String, List<String>> cluster;
            if (scope.contains(CommonConstants.COMMON_SUBSTRING)
                    && CommonConstants.ORGANISATION.equalsIgnoreCase(customerType)) {
                cluster = CommonConstants.BUSINESS_CDS_DATA_CLUSTER.get(scope);
            } else if (scope.contains(CommonConstants.COMMON_SUBSTRING)) {
                cluster = CommonConstants.INDIVIDUAL_CDS_DATA_CLUSTER.get(scope);
            } else {
                cluster = CommonConstants.CDS_DATA_CLUSTER.get(scope);
            }

            if (cluster == null) {
                // Profile / contact claims fall through here — handled by processProfileClusters
                if (log.isDebugEnabled()) {
                    log.debug(String.format("No data cluster found for scope: %s", scope));
                }
                continue;
            }

            dataClusters.addAll(cluster.entrySet());
        }
        return dataClusters;
    }

    /**
     * Build the profile clusters (Name and Contact Details) based on which standard claims appear in the scope
     * list. Mirrors the non-amendment path of OB3's {@code processProfileDataClusters} — if any name claim is
     * present, the {@code name} entry from {@link CommonConstants#PROFILE_DATA_CLUSTER} is added; if any contact
     * claim group is present, a sorted composite key (e.g. {@code contactDetails_email_phone}) is looked up.
     *
     * @param scopes requested scopes (already enriched with profile claims by {@code setClaimPermissions})
     * @return ordered list of profile cluster entries
     */
    private static List<Map.Entry<String, List<String>>> processProfileClusters(List<String> scopes) {

        List<Map.Entry<String, List<String>>> profileClusters = new ArrayList<>();

        boolean hasNameClaim = CommonConstants.NAME_CLUSTER_PERMISSIONS.stream().anyMatch(scopes::contains);
        if (hasNameClaim) {
            Map<String, List<String>> nameCluster = CommonConstants.PROFILE_DATA_CLUSTER.get(
                    CommonConstants.NAME_CLUSTER);
            if (nameCluster != null) {
                profileClusters.addAll(nameCluster.entrySet());
            }
        }

        List<String> contactGroups = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : CommonConstants.CONTACT_CLUSTER_CLAIMS.entrySet()) {
            if (entry.getValue().stream().anyMatch(scopes::contains)) {
                contactGroups.add(entry.getKey());
            }
        }
        if (!contactGroups.isEmpty()) {
            Collections.sort(contactGroups);
            String contactKey = CommonConstants.CONTACT_CLUSTER + "_" + String.join("_", contactGroups);
            Map<String, List<String>> contactCluster = CommonConstants.PROFILE_DATA_CLUSTER.get(contactKey);
            if (contactCluster != null) {
                profileClusters.addAll(contactCluster.entrySet());
            } else {
                log.warn(String.format("No contact cluster definition found for key: %s", contactKey));
            }
        }

        return profileClusters;
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

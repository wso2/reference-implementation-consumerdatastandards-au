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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.openbanking.consumerdatastandards.au.extensions.configurations.ConfigurableProperties;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.CdsErrorEnum;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.CommonConstants;
import org.wso2.openbanking.consumerdatastandards.au.extensions.exceptions.CdsConsentException;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.Account;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.Authorization;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.AuthorizedResources;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.AuthorizedResourcesAuthorizedDataInner;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.DetailedConsentResourceDataWithAmendments;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.PersistAuthorizedConsent;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.PersistAuthorizedConsentRequestBody;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.Resource;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.SuccessResponsePersistAuthorizedConsentData;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.UserGrantedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for CDS Consent Auth persistence operations.
 */
public class CdsConsentAuthPersistUtil {

    private static final Log log = LogFactory.getLog(CdsConsentAuthPersistUtil.class);

    /**
     * Method to handle the persist consent request.
     * @return Detailed Consent Resource
     */
    public static SuccessResponsePersistAuthorizedConsentData cdsConsentPersist(
            PersistAuthorizedConsentRequestBody persistAuthorizedConsentRequestBody) throws CdsConsentException {

        String consentStatus;
        String authStatus;
        long validityTime;

        SuccessResponsePersistAuthorizedConsentData persistAuthorizedConsentDataRes =
                new SuccessResponsePersistAuthorizedConsentData();

        try {
            PersistAuthorizedConsent requestData = persistAuthorizedConsentRequestBody.getData();

            DetailedConsentResourceDataWithAmendments updatedConsent = new DetailedConsentResourceDataWithAmendments();

            UserGrantedData userGrantedData = requestData.getUserGrantedData();

            // Convert user granted data to JSON object
            JSONObject consumerInputData = CommonConsentExtensionUtil.convertObjectToJson(userGrantedData);

            // Get authorisedResources from user granted data
            AuthorizedResources authorizedResources = userGrantedData.getAuthorizedResources();

            // Get authorized data list from authorized resources
            List<AuthorizedResourcesAuthorizedDataInner> authorizedDataInners = authorizedResources.getAuthorizedData();

            // Get metadata object from authorized resources
            Object metadataObject = authorizedResources.getMetadata();

            //Set Consent Type
            String consentType = authorizedResources.getType();

            //Check whether the consent is approved or not and assign consent status.
            boolean isApproved = requestData.getIsApproved();
            if (!isApproved) {
                consentStatus = CommonConstants.REJECTED_STATUS;
                authStatus = CommonConstants.REJECTED_STATUS;
            } else {
                consentStatus = CommonConstants.AUTHORIZED_STATUS;
                authStatus = CommonConstants.AUTHORIZED_STATUS;

                //Check if account ids are available in the request when consent is approved.
                for (AuthorizedResourcesAuthorizedDataInner authorizedDataInner : authorizedDataInners) {
                    if (authorizedDataInner.getAccounts().isEmpty()) {
                        throw new CdsConsentException(CdsErrorEnum.FIELD_MISSING,
                                "Account IDs are required when consent is approved");
                    }
                }
            }

            //Check whether account ids are in string format and add them to a JSONArray.
            List<Resource> authResource = validateAndGetResources(authorizedDataInners);
            String primaryUserId = consumerInputData.getString("userId");

            ArrayList<Authorization> authorizationResource = new ArrayList<>();
            Map<String, Set<String>> linkedMemberAccountMap = new HashMap<>();
            Map<String, Set<String>> secondaryOwnerAccountMap = new HashMap<>();
            Map<String, Set<String>> businessOwnerAccountMap = new HashMap<>();
            Map<String, Set<String>> nominatedRepresentativeAccountMap = new HashMap<>();
            Map<String, Set<String>> businessAccountOwnersByAccountMap = new HashMap<>();
            Map<String, Set<String>> businessNominatedRepresentativesByAccountMap = new HashMap<>();
            Map<String, String> jointAccountDisclosureMap = new HashMap<>();
            boolean otherAccountsAvailability = processAccountsData(authorizedDataInners, linkedMemberAccountMap,
                    secondaryOwnerAccountMap, businessOwnerAccountMap, nominatedRepresentativeAccountMap,
                    businessAccountOwnersByAccountMap, businessNominatedRepresentativesByAccountMap,
                    jointAccountDisclosureMap, primaryUserId);

            // Create Authorizations for primary member
            boolean hasAdditionalAuthorizers = !linkedMemberAccountMap.isEmpty() || !secondaryOwnerAccountMap.isEmpty()
                    || !businessOwnerAccountMap.isEmpty() || !nominatedRepresentativeAccountMap.isEmpty();
            String primaryAuthType = hasAdditionalAuthorizers ?
                    CommonConstants.AUTH_RESOURCE_TYPE_PRIMARY : CommonConstants.DEFAULT_AUTH_TYPE;
            authorizationResource.add(validateAndBuildAuthorizations(authResource, primaryAuthType, authStatus,
                    primaryUserId));

            // Create Authorizations for Linked Members
            for (Map.Entry<String, Set<String>> entry : linkedMemberAccountMap.entrySet()) {
                authorizationResource.add(buildMemberAuthorization(entry, CommonConstants.AUTH_RESOURCE_TYPE_LINKED));
            }

            // Setting the secondary auth type based on joint accounts or individual  accounts
            String secondaryOwnerAuthType = secondaryOwnerAccountMap.size() > 1
                    ? CommonConstants.AUTH_TYPE_SECONDARY_JOINT_ACCOUNT_OWNER
                    : CommonConstants.AUTH_TYPE_SECONDARY_INDIVIDUAL_ACCOUNT_OWNER;
            // Create Authorizations for Secondary Account Owners
            for (Map.Entry<String, Set<String>> entry : secondaryOwnerAccountMap.entrySet()) {
                authorizationResource.add(buildMemberAuthorization(entry, secondaryOwnerAuthType));
            }

            // Create Authorizations for Business Account Owners
            for (Map.Entry<String, Set<String>> entry : businessOwnerAccountMap.entrySet()) {
                authorizationResource.add(buildMemberAuthorization(entry,
                        CommonConstants.AUTH_TYPE_BUSINESS_ACCOUNT_OWNER));
            }

            // Create Authorizations for Business Nominated Representatives
            for (Map.Entry<String, Set<String>> entry : nominatedRepresentativeAccountMap.entrySet()) {
                authorizationResource.add(buildMemberAuthorization(entry,
                        CommonConstants.AUTH_TYPE_NOMINATED_REPRESENTATIVE));
            }

            // Persist primary user as AUTHORIZE for every business account in metadata.
            if (StringUtils.isNotBlank(primaryUserId)) {
                Set<String> businessAccountIds = new HashSet<>();
                businessAccountIds.addAll(businessAccountOwnersByAccountMap.keySet());
                businessAccountIds.addAll(businessNominatedRepresentativesByAccountMap.keySet());

                for (String businessAccountId : businessAccountIds) {
                    businessNominatedRepresentativesByAccountMap
                            .computeIfAbsent(businessAccountId, k -> new HashSet<>())
                            .add(primaryUserId);
                }
            }

            // Add disclosure options for joint accounts
            if (!jointAccountDisclosureMap.isEmpty() &&
                    !AccountMetadataUtil.addDisclosureOption(jointAccountDisclosureMap)) {
                // Throwing an error if disclosureOptions didn't get added.
                log.error("Error occurred while adding disclosure options in persist step.");
                throw new CdsConsentException(CdsErrorEnum.UNEXPECTED_ERROR,
                        "Error While Updating the Joint account Info.");
            }

            // Add secondary account instructions for the consenting user
            if (!secondaryOwnerAccountMap.isEmpty()) {
                // Collect all secondary account IDs from the map values
                Set<String> secondaryAccountIds = secondaryOwnerAccountMap.values().stream().flatMap(Set::stream)
                        .collect(Collectors.toSet());

                String userId = consumerInputData.getString("userId");
                if (!AccountMetadataUtil.addSecondaryAccountInstructions(secondaryAccountIds, userId,
                        otherAccountsAvailability)) {
                    // Throwing an error if secondary user instruction data didn't get added.
                    log.error("Error occurred while adding secondary account instructions in persist step.");
                    throw new CdsConsentException(CdsErrorEnum.UNEXPECTED_ERROR,
                            "Error while updating the secondary account instructions.");
                }
            }

            // Add business stakeholder permissions for business nominated representatives
            if (!(businessNominatedRepresentativesByAccountMap.isEmpty() || businessAccountOwnersByAccountMap.isEmpty())
                    && !AccountMetadataUtil.addBusinessStakeholderPermissions(businessAccountOwnersByAccountMap,
                    businessNominatedRepresentativesByAccountMap)) {
                log.error("Error occurred while adding business stakeholder permissions in persist step.");
                throw new CdsConsentException(CdsErrorEnum.UNEXPECTED_ERROR,
                        "Error while updating the business stakeholder permissions.");
            }

            //Convert expiration date time to validity time in seconds
            if (metadataObject != null) {
                validityTime = getValidityTime(metadataObject);
            } else {
                log.error("Metadata is null in persist request");
                throw new CdsConsentException(CdsErrorEnum.FIELD_MISSING,
                        "Metadata field is required but missing in the request");
            }

            //Create JSON payload from metadata object
            JSONObject receipt = createJsonPayload(metadataObject);

            //Set Detailed Consent Resource Data With Amendments
            updatedConsent.setType(consentType);
            updatedConsent.setStatus(consentStatus);
            updatedConsent.setValidityTime(validityTime);
            updatedConsent.setRecurringIndicator(true);
            updatedConsent.setFrequency(CommonConstants.DEFAULT_FREQUENCY);
            updatedConsent.setReceipt(receipt);
            updatedConsent.setAuthorizations(authorizationResource);

            persistAuthorizedConsentDataRes.setConsentResource(updatedConsent);

            return persistAuthorizedConsentDataRes;
            
        } catch (JsonProcessingException e) {
            log.error("Consent persistence failed", e);
            throw new CdsConsentException(CdsErrorEnum.BAD_REQUEST, "Consent persistence failed");
        }
    }

    /**
     * Process accounts data and populate the provided maps with linked member, secondary owner,
     * and disclosure information. Handles joint accounts, secondary accounts, and accounts
     * that are both joint and secondary in a single pass.
     *
     * @param authorizedDataInners List of authorized data containing accounts
     * @param linkedMemberAccountMap Map to be populated with linked member userId to account IDs
     * @param secondaryOwnerAccountMap Map to be populated with secondary owner userId to account IDs
     * @param businessOwnerAccountMap Map to be populated with business account owner userId to account IDs
     * @param nominatedRepresentativeAccountMap Map to be populated with nominated representative userId to account IDs
     * @param businessAccountOwnersByAccountMap Map of accountId to business account owners
     * @param businessNominatedRepresentativesByAccountMap Map of accountId to nominated representatives
     * @param jointAccountDisclosureMap Map to be populated with joint account disclosure information
     * @param primaryUserId authenticated primary user id
     * @return other-accounts-availability value shared across secondary accounts
     */
    private static boolean processAccountsData(List<AuthorizedResourcesAuthorizedDataInner> authorizedDataInners,
                                               Map<String, Set<String>> linkedMemberAccountMap,
                                               Map<String, Set<String>> secondaryOwnerAccountMap,
                                               Map<String, Set<String>> businessOwnerAccountMap,
                                               Map<String, Set<String>> nominatedRepresentativeAccountMap,
                                               Map<String, Set<String>> businessAccountOwnersByAccountMap,
                                               Map<String, Set<String>> businessNominatedRepresentativesByAccountMap,
                                               Map<String, String> jointAccountDisclosureMap, String primaryUserId) {

        boolean otherAccountsAvailability = false;
        boolean secondaryAvailabilityResolved = false;

        for (AuthorizedResourcesAuthorizedDataInner authorizedDataInner : authorizedDataInners) {
            for (Account account : authorizedDataInner.getAccounts()) {

                Map<String, Object> additionalProps = account.getAdditionalProperties();
                if (additionalProps == null) {
                    continue;
                }

                String displayName = account.getDisplayName();
                if (StringUtils.isEmpty(displayName)) {
                    log.error("Account displayName is null or empty, skipping account in resource validation");
                    continue;
                }

                String accountId = CommonConsentExtensionUtil.getAccountIdByDisplayName(
                        ConfigurableProperties.SHARABLE_ENDPOINT, displayName.split("<br>")[0]
                );

                if (StringUtils.isEmpty(accountId)) {
                    log.warn("Could not resolve accountId for displayName: " + displayName.split("<br>")[0]);
                    continue;
                }

                Object innerPropsObj = additionalProps.get(CommonConstants.ADDITIONAL_PROPERTIES);
                if (!(innerPropsObj instanceof Map)) {
                    continue;
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> innerProps = (Map<String, Object>) innerPropsObj;

                // Process linked members (joint accounts)
                Object linkedMembersObj = innerProps.get(CommonConstants.LINKED_MEMBERS);
                if (linkedMembersObj instanceof List) {
                    jointAccountDisclosureMap.put(accountId, CommonConstants.DOMS_STATUS_PRE_APPROVAL);

                    @SuppressWarnings("unchecked")
                    List<String> linkedMembers = (List<String>) linkedMembersObj;
                    for (String linkedMember : linkedMembers) {
                        linkedMemberAccountMap.computeIfAbsent(linkedMember, k -> new HashSet<>()).add(accountId);
                    }
                }

                // Process secondary account owners
                Object secondaryOwnersObj = innerProps.get(CommonConstants.SECONDARY_ACCOUNT_OWNERS_TAG);
                if (secondaryOwnersObj instanceof List) {
                    boolean otherAccountsAvailabilityProp = true;
                    Object otherAccountsAvailabilityObj =
                            innerProps.get(CommonConstants.OTHER_ACCOUNTS_AVAILABILITY_FIELD);
                    if (otherAccountsAvailabilityObj instanceof Boolean) {
                        otherAccountsAvailabilityProp = (Boolean) otherAccountsAvailabilityObj;
                    }
                    if (!secondaryAvailabilityResolved) {
                        otherAccountsAvailability = otherAccountsAvailabilityProp;
                        secondaryAvailabilityResolved = true;
                    } else if (otherAccountsAvailability != otherAccountsAvailabilityProp) {
                        log.warn("Inconsistent otherAccountsAvailablitiy values found across secondary accounts; " +
                                "using the first resolved value for all accounts");
                    }

                    @SuppressWarnings("unchecked")
                    List<String> secondaryOwners = (List<String>) secondaryOwnersObj;
                    for (String owner : secondaryOwners) {
                        secondaryOwnerAccountMap.computeIfAbsent(owner, k -> new HashSet<>()).add(accountId);
                    }
                }

                // Process business account owners
                Object businessOwnersObj = innerProps.get(CommonConstants.ACCOUNT_OWNERS_TAG);
                if (businessOwnersObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> businessOwners = (List<String>) businessOwnersObj;
                    for (String owner : businessOwners) {
                        if (StringUtils.isBlank(owner)) {
                            continue;
                        }
                        businessOwnerAccountMap.computeIfAbsent(owner, k -> new HashSet<>()).add(accountId);
                        businessAccountOwnersByAccountMap.computeIfAbsent(accountId, k -> new HashSet<>()).add(owner);
                    }
                }

                // Process business nominated representatives (excluding primary user)
                Object nominatedRepresentativesObj = innerProps.get(CommonConstants.NOMINATED_REPRESENTATIVES_TAG);
                if (nominatedRepresentativesObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> nominatedRepresentatives = (List<String>) nominatedRepresentativesObj;
                    for (String representative : nominatedRepresentatives) {
                        if (StringUtils.isBlank(representative)
                                || representative.equalsIgnoreCase(primaryUserId)) {
                            continue;
                        }
                        nominatedRepresentativeAccountMap.computeIfAbsent(representative,
                                k -> new HashSet<>()).add(accountId);
                        businessNominatedRepresentativesByAccountMap.computeIfAbsent(accountId,
                                k -> new HashSet<>()).add(representative);
                    }
                }
            }
        }

        return otherAccountsAvailability;
    }

    /**
     * Builds an authorization object for a member (linked member or secondary account owner)
     * with their associated accounts.
     *
     * @param entry Map entry containing the member's user ID and their associated account IDs
     * @param authType The authorization type (e.g. linkedMember, secondary_account_owner)
     * @return Authorization object with the member's details and account resources
     */
    private static Authorization buildMemberAuthorization(Map.Entry<String, Set<String>> entry, String authType) {
        String memberUserId = entry.getKey();
        Set<String> accountIds = entry.getValue();

        Authorization memberAuthorization = new Authorization();
        memberAuthorization.setUserId(memberUserId);
        memberAuthorization.setType(authType);
        memberAuthorization.setStatus(CommonConstants.AUTHORIZED_STATUS);

        List<Resource> memberResources = new ArrayList<>();

        for (String accountId : accountIds) {
            Resource resource = new Resource();
            resource.setAccountId(accountId);
            resource.setPermission(CommonConstants.N_A);
            resource.setStatus(CommonConstants.ACTIVE_MAPPING_STATUS);
            memberResources.add(resource);
        }

        memberAuthorization.setResources(memberResources);
        return memberAuthorization;
    }

    /**
     * Method to retrieve account id from shareable endpoint and generate the authorisation resources.
     * @param authorizedDataInners List of permissions and accounts
     * @return List of Account Ids and it's Mapping
     */
    private static List<Resource> validateAndGetResources(
            List<AuthorizedResourcesAuthorizedDataInner> authorizedDataInners) {

        List<Resource> resources = new ArrayList<>();
        String accountsURL = ConfigurableProperties.SHARABLE_ENDPOINT;
        String accountId;

        for (AuthorizedResourcesAuthorizedDataInner authorizedDataInner : authorizedDataInners) {
            List<Account> accounts = authorizedDataInner.getAccounts();

            // Loop through each account within the current data block
            for (Account account : accounts) {

                Resource resource = new Resource();

                String displayName = account.getDisplayName();
                if (StringUtils.isEmpty(displayName)) {
                    log.error("Account displayName is null or empty, skipping account in resource validation");
                    continue;
                }

                //Get Account_Id from Display Name
                accountId = CommonConsentExtensionUtil.getAccountIdByDisplayName(accountsURL,
                        displayName.split("<br>")[0]);

                // Set properties from the individual 'account' and the outer 'authorizedDataInner'
                resource.setAccountId(accountId);
                resource.setPermission("n/a");
                resource.setStatus(CommonConstants.ACTIVE_MAPPING_STATUS);
                resources.add(resource);
            }
        }
        return resources;
    }

    /**
     * Builds authorization from input accounts and status.
     * @param authResource List of Resource objects
     * @param type Consent Type
     * @param authStatus Authorization Status
     * @param userId User Id
     * @return Authorization object
     */
    public static Authorization validateAndBuildAuthorizations(List<Resource> authResource, String type,
                                                               String authStatus, String userId) {

        Authorization authorization = new Authorization();
        authorization.setUserId(userId);
        authorization.setType(type);
        authorization.setStatus(authStatus);
        authorization.setResources(authResource);

        return authorization;
    }

    /**
     * Method to get the account id list from the payload data.
     * @param payloadData payload data from the persist request
     * @return List of account ids
     * @throws CdsConsentException
     */
    private static ArrayList<String> getAccountIdList(JSONObject payloadData) throws CdsConsentException {

        if (payloadData.get(CommonConstants.ACCOUNT_IDS) == null
                || !(payloadData.get(CommonConstants.ACCOUNT_IDS) instanceof JSONArray)) {
            log.error("Account IDs not available in persist request");
            throw new CdsConsentException(CdsErrorEnum.FIELD_MISSING,
                    "Account IDs field is missing or invalid in the request");
        }

        JSONArray accountIds = (JSONArray) payloadData.get(CommonConstants.ACCOUNT_IDS);
        ArrayList<String> accountIdsList = new ArrayList<>();
        for (Object account : accountIds) {
            if (!(account instanceof String)) {
                log.error("Account IDs format error in persist request");
                throw new CdsConsentException(CdsErrorEnum.INVALID_FIELD,
                        "Account IDs must be strings in the request");
            }
            accountIdsList.add((String) account);
        }
        return accountIdsList;
    }

    /**
     * Method to get the validity time from the metadata and Convert the expiration datetime to epoch seconds.
     * @param metadataObject metadata object from authorized resources
     * @return validity time in epoch seconds
     */
    private static Long getValidityTime(Object metadataObject) {

        String expirationDateTime = null;

        if (metadataObject instanceof Map) {
            Map<String, Object> metadata = (Map<String, Object>) metadataObject;

            // Get the 'accountData' object and check if it's a List.
            Object accountDataObject = metadata.get(CommonConstants.ACCOUNT_DATA);
            if (accountDataObject instanceof List) {
                List<Map<String, Object>> accountDataList = (List<Map<String, Object>>) accountDataObject;

                // Stream the list to find the Map where "title" is "expirationDateTime".
                Optional<Map<String, Object>> expirationEntry = accountDataList.stream()
                        .filter(item -> CommonConstants.EXPIRATION_DATE_TIME.equals(item.get(CommonConstants.TITLE)))
                        .findFirst();

                if (expirationEntry.isPresent()) {
                    Map<String, Object> item = expirationEntry.get();

                    // Get the nested 'data' object and check if it's a List.
                    Object dataObject = item.get(CommonConstants.DATA);
                    if (dataObject instanceof List) {
                        List<Object> dataList = (List<Object>) dataObject;

                        // Get expiration date time value from the list
                        if (!dataList.isEmpty()) {
                            expirationDateTime = dataList.get(0).toString();
                        }
                    }
                }
            }
        }

        // Convert expirationDateTime to epoch seconds
        long epochSeconds = CommonConsentExtensionUtil.getEpochSeconds(expirationDateTime);
        return epochSeconds;
    }

    /**
     * Method to create JSON payload from metadata object.
     * @param metadataObject metadata object from authorized resources
     * @return JSON payload from metadata
     */
    public static JSONObject createJsonPayload(Object metadataObject) {

        // Check if the provided Object is actually a Map.
        if (!(metadataObject instanceof Map)) {
            // If it's not a Map (or is null), return an empty JSON object.
            return new JSONObject();
        }

        // Cast the Object to a Map to work with it.
        Map<String, Object> metadata = (Map<String, Object>) metadataObject;

        JSONObject jsonPayload = new JSONObject();
        JSONObject accountDataJson = new JSONObject();

        Object accountDataObject = metadata.get(CommonConstants.ACCOUNT_DATA);
        if (accountDataObject instanceof List) {
            List<Map<String, Object>> accountDataList = (List<Map<String, Object>>) accountDataObject;

            for (Map<String, Object> item : accountDataList) {
                String title = (String) item.get(CommonConstants.TITLE);
                Object dataObject = item.get(CommonConstants.DATA);

                if (title != null && dataObject instanceof List) {
                    List<Object> dataList = (List<Object>) dataObject;

                    if (!dataList.isEmpty()) {
                        switch (title) {
                            case CommonConstants.PERMISSIONS:
                                List<String> permissions = dataList.stream()
                                    .map(obj -> ((String) obj).toUpperCase(Locale.ROOT))
                                        .collect(Collectors.toList());
                                accountDataJson.put(CommonConstants.PERMISSIONS, permissions);
                                break;
                            case CommonConstants.EXPIRATION_DATE_TIME:
                                accountDataJson.put(CommonConstants.EXPIRATION_DATE_TIME, dataList.get(0));
                                break;
                        }
                    }
                }
            }
        }

        jsonPayload.put(CommonConstants.ACCOUNT_DATA, accountDataJson);
        return jsonPayload;
    }
}

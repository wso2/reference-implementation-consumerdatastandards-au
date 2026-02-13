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

import com.fasterxml.jackson.core.JsonProcessingException;
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
     * @param persistAuthorizedConsentRequestBody
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

            ArrayList<Authorization> authorizationResource = new ArrayList<>();
            authorizationResource.add(validateAndBuildAuthorizations(authResource, consentType, authStatus,
                    consumerInputData.getString("userId")));

            Map<String, Set<String>> linkedMemberAccountMap = new HashMap<>();
            Map<String, String> jointAccountDisclosureMap = new HashMap<>();

            for (AuthorizedResourcesAuthorizedDataInner authorizedDataInner : authorizedDataInners) {
                for (Account account : authorizedDataInner.getAccounts()) {

                    Map<String, Object> additionalProps = account.getAdditionalProperties();
                    if (additionalProps == null) {
                        continue;
                    }

                    String accountId = CommonConsentExtensionUtil.getAccountIdByDisplayName(
                            ConfigurableProperties.SHARABLE_ENDPOINT,
                            account.getDisplayName().split("<br>")[0]
                    );

                    Object innerPropsObj = additionalProps.get(CommonConstants.ADDITIONAL_PROPERTIES);
                    if (!(innerPropsObj instanceof Map)) {
                        continue;
                    }

                    @SuppressWarnings("unchecked")
                    Map<String, Object> innerProps = (Map<String, Object>) innerPropsObj;

                    // Check for is_jointAccount_pre_approval property
                    Object isJointAccountPreApprovalObj = innerProps.get("is_jointAccount_pre_approval");
                    if (isJointAccountPreApprovalObj instanceof Boolean) {
                        Boolean isJointAccountPreApproval = (Boolean) isJointAccountPreApprovalObj;
                        String disclosureOption = isJointAccountPreApproval ? 
                                CommonConstants.DOMS_STATUS_PRE_APPROVAL : 
                                CommonConstants.DOMS_STATUS_NO_SHARING;
                        jointAccountDisclosureMap.put(accountId, disclosureOption);

                        if (log.isDebugEnabled()) {
                            log.debug("Joint account found - accountId: " + accountId + 
                                    ", disclosureOption: " + disclosureOption);
                        }
                    }

                    Object linkedMembersObj = innerProps.get(CommonConstants.LINKED_MEMBERS);
                    if (!(linkedMembersObj instanceof List)) {
                        continue;
                    }

                    @SuppressWarnings("unchecked")
                    List<String> linkedMembers = (List<String>) linkedMembersObj;

                    for (String linkedMember : linkedMembers) {
                        linkedMemberAccountMap
                                .computeIfAbsent(linkedMember, k -> new HashSet<>())
                                .add(accountId);
                    }

                }
            }

            // Create Authorizations for Linked Members
            for (Map.Entry<String, Set<String>> entry : linkedMemberAccountMap.entrySet()) {
                authorizationResource.add(getLinkedMemberAuthorization(entry));
            }

            // Add disclosure options for joint accounts
            if (!jointAccountDisclosureMap.isEmpty()) {
                for (Map.Entry<String, String> entry : jointAccountDisclosureMap.entrySet()) {
                    String accountId = entry.getKey();
                    String disclosureOption = entry.getValue();

                    boolean success = AccountMetadataUtil.addDisclosureOption(
                            List.of(accountId), disclosureOption);

                    if (log.isDebugEnabled()) {
                        log.debug("[DOMS] Disclosure option added for joint account - accountId: " + 
                                accountId + ", status: " + (success ? "success" : "failed"));
                    }
                }
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

    private static Authorization getLinkedMemberAuthorization(Map.Entry<String, Set<String>> entry) {
        String linkedMemberUserId = entry.getKey();
        Set<String> accountIds = entry.getValue();

        Authorization linkedMemberAuthorization = new Authorization();
        linkedMemberAuthorization.setUserId(linkedMemberUserId);
        linkedMemberAuthorization.setType(CommonConstants.AUTH_RESOURCE_TYPE_LINKED);
        linkedMemberAuthorization.setStatus(CommonConstants.AUTHORIZED_STATUS);

        List<Resource> linkedResources = new ArrayList<>();

        for (String accountId : accountIds) {
            Resource resource = new Resource();
            resource.setAccountId(accountId);
            resource.setPermission(CommonConstants.N_A);
            resource.setStatus(CommonConstants.ACTIVE_MAPPING_STATUS);
            linkedResources.add(resource);
        }

        linkedMemberAuthorization.setResources(linkedResources);
        return linkedMemberAuthorization;
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

                //Get Account_Id from Display Name
                accountId = CommonConsentExtensionUtil.getAccountIdByDisplayName(
                        accountsURL, account.getDisplayName().split("<br>")[0]);

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
                                        .map(obj -> ((String) obj).toUpperCase())
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

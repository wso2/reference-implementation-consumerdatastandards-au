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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.openbanking.consumerdatastandards.au.extensions.configurations.ConfigurableProperties;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.CDSErrorEnum;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.CommonConstants;
import org.wso2.openbanking.consumerdatastandards.au.extensions.exceptions.CDSConsentException;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utility class for CDS Consent Auth persistence operations.
 */
public class CDSConsentAuthPersistUtil {

    private static final Log log = LogFactory.getLog(CDSConsentAuthPersistUtil.class);

    /**
     * Method to handle the persist consent request.
     * @param persistAuthorizedConsentRequestBody
     * @return
     */
    public static SuccessResponsePersistAuthorizedConsentData CdsConsentPersist(
            PersistAuthorizedConsentRequestBody persistAuthorizedConsentRequestBody) throws CDSConsentException {

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
            JSONObject consumerInputData = CommonConsentExtensionUtils.convertObjectToJson(userGrantedData);

            // Get authorisedResources from user granted data
            AuthorizedResources authorizedResources = userGrantedData.getAuthorizedResources();

            // Get authorized data list from authorized resources
            List<AuthorizedResourcesAuthorizedDataInner> authorizedDataInners = authorizedResources.getAuthorizedData();

            // Get metadata object from authorized resources
            Object metadataObject = userGrantedData.getAuthorizedResources().getMetadata();

            //Set Consent Type
            String consentType = requestData.getUserGrantedData().getAuthorizedResources().getType();

            //Check whether the consent is approved or not and assign consent status.
            boolean isApproved = requestData.getIsApproved();
            if (!isApproved) {
                consentStatus = CommonConstants.REJECTED_STATUS;
                authStatus = CommonConstants.REJECTED_STATUS;
            } else {
                consentStatus = CommonConstants.AUTHORIZED_STATUS;
                authStatus = CommonConstants.AUTHORIZED_STATUS;
            }

            //Check if account ids are available in the request when consent is approved.
            if (isApproved) {
                for (AuthorizedResourcesAuthorizedDataInner authorizedDataInner : authorizedDataInners) {
                    if (authorizedDataInner.getAccounts().isEmpty()) {
                        throw new CDSConsentException(CDSErrorEnum.FIELD_MISSING,
                                "Account IDs are required when consent is approved");
                    }
                }
            }

            //Check whether account ids are in string format and add them to a JSONArray.
            List<Resource> authResource = validateAndGetResources(authorizedDataInners);

            ArrayList<Authorization> authorizationResource = new ArrayList<>();
            authorizationResource.add(validateAndBuildAuthorizations(authResource, consentType, authStatus,
                    consumerInputData.getString("userId")));

            //Convert expiration date time to validity time in seconds
            if (userGrantedData.getAuthorizedResources().getMetadata() != null) {
                validityTime = getValidityTime(metadataObject);
            } else {
                log.error("Metadata is null in persist request");
                throw new CDSConsentException(CDSErrorEnum.FIELD_MISSING,
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
            
        } catch (Exception e) {
            throw new CDSConsentException(CDSErrorEnum.BAD_REQUEST, "Consent persistence failed");
        }
    }

    /**
     * Method to retrieve account id from shareable endpoint and generate the authorisation resources.
     * @param authorizedDataInners
     * @return
     */
    private static List<Resource> validateAndGetResources(List<AuthorizedResourcesAuthorizedDataInner> authorizedDataInners) {
        List<Resource> resources = new ArrayList<>();

        String accountsURL = ConfigurableProperties.SHARABLE_ENDPOINT;
        String accountId;

        for (AuthorizedResourcesAuthorizedDataInner authorizedDataInner : authorizedDataInners) {
            List<Account> accounts = authorizedDataInner.getAccounts();

            // Loop through each account within the current data block
            for (Account account : accounts) {

                Resource resource = new Resource();

                //Get Account_Id from Display Name
                accountId = CommonConsentExtensionUtils.getAccountIdByDisplayName(accountsURL, account.getDisplayName());

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
     * @param authResource
     * @param type
     * @param authStatus
     * @param userId
     * @return
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
     * @param payloadData
     * @return
     * @throws CDSConsentException
     */
    private static ArrayList<String> getAccountIdList(JSONObject payloadData) throws CDSConsentException {

        if (payloadData.get(CommonConstants.ACCOUNT_IDS) == null
                || !(payloadData.get(CommonConstants.ACCOUNT_IDS) instanceof JSONArray)) {
            log.error("Account IDs not available in persist request");
            throw new CDSConsentException(CDSErrorEnum.FIELD_MISSING,
                    "Account IDs field is missing or invalid in the request");
        }

        JSONArray accountIds = (JSONArray) payloadData.get(CommonConstants.ACCOUNT_IDS);
        ArrayList<String> accountIdsList = new ArrayList<>();
        for (Object account : accountIds) {
            if (!(account instanceof String)) {
                log.error("Account IDs format error in persist request");
                throw new CDSConsentException(CDSErrorEnum.INVALID_FIELD,
                        "Account IDs must be strings in the request");
            }
            accountIdsList.add((String) account);
        }
        return accountIdsList;
    }

    /**
     * Method to get the validity time from the metadata.
     * Convert the expiration datetime to epoch seconds.
     * @param metadataObject
     * @return
     */
    private static Long getValidityTime(Object metadataObject) {

        String expirationDateTime = null;

        // 2. Check if the top-level object is a Map and cast it.
        if (metadataObject instanceof Map) {
            Map<String, Object> metadata = (Map<String, Object>) metadataObject;

            // 3. Get the 'accountData' object and check if it's a List.
            Object accountDataObject = metadata.get("accountData");
            if (accountDataObject instanceof List) {
                List<Map<String, Object>> accountDataList = (List<Map<String, Object>>) accountDataObject;

                // 4. Stream the list to find the Map where "title" is "expirationDateTime".
                Optional<Map<String, Object>> expirationEntry = accountDataList.stream()
                        .filter(item -> "expirationDateTime".equals(item.get("title")))
                        .findFirst();

                // 5. If the correct entry was found, process it.
                if (expirationEntry.isPresent()) {
                    Map<String, Object> item = expirationEntry.get();

                    // 6. Get the nested 'data' object and check if it's a List.
                    Object dataObject = item.get("data");
                    if (dataObject instanceof List) {
                        List<Object> dataList = (List<Object>) dataObject;

                        // 7. Finally, get the first element from the 'data' list if it's not empty.
                        if (!dataList.isEmpty()) {
                            expirationDateTime = (String) dataList.get(0);
                        }
                    }
                }
            }
        }

        //Parse the string directly into an Instant object
        Instant instant = Instant.parse(expirationDateTime);

        //Get the number of seconds from the epoch (1970-01-01T00:00:00Z)
        long epochSeconds = instant.getEpochSecond();
        return epochSeconds;
    }

    /**
     * Method to create JSON payload from metadata object.
     * @param metadataObject
     * @return
     */
    public static JSONObject createJsonPayload(Object metadataObject) {

        // 1. Check if the provided Object is actually a Map.
        if (!(metadataObject instanceof Map)) {
            // If it's not a Map (or is null), return an empty JSON object.
            return new JSONObject();
        }

        // 2. If the check passes, cast the Object to a Map to work with it.
        Map<String, Object> metadata = (Map<String, Object>) metadataObject;

        // --- From this point on, the logic is identical to the previous solution ---

        JSONObject jsonPayload = new JSONObject();
        JSONObject accountDataJson = new JSONObject();

        Object accountDataObject = metadata.get("accountData");
        if (accountDataObject instanceof List) {
            List<Map<String, Object>> accountDataList = (List<Map<String, Object>>) accountDataObject;

            for (Map<String, Object> item : accountDataList) {
                String title = (String) item.get("title");
                Object dataObject = item.get("data");

                if (title != null && dataObject instanceof List) {
                    List<Object> dataList = (List<Object>) dataObject;

                    if (!dataList.isEmpty()) {
                        switch (title) {
                            case "permissions":
                                List<String> permissions = dataList.stream()
                                        .map(obj -> ((String) obj).toUpperCase())
                                        .collect(Collectors.toList());
                                accountDataJson.put("permissions", permissions);
                                break;
                            case "expirationDateTime":
                                accountDataJson.put("expirationDateTime", dataList.get(0));
                                break;
                        }
                    }
                }
            }
        }

        jsonPayload.put("accountData", accountDataJson);
        return jsonPayload;
    }
}

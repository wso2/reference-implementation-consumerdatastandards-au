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
package org.wso2.openbanking.cds.consent.extensions.authorize.impl.persist;

import com.wso2.openbanking.accelerator.account.metadata.service.service.AccountMetadataServiceImpl;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistStep;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;
import org.wso2.openbanking.cds.consent.extensions.authorize.utils.CDSConsentPersistUtil;
import org.wso2.openbanking.cds.consent.extensions.common.CDSConsentExtensionConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is responsible for persisting the business account consent data in the database.
 */
public class CDSBusinessAccountConsentPersistenceStep implements ConsentPersistStep {

    private static final Log log = LogFactory.getLog(CDSBusinessAccountConsentPersistenceStep.class);
    AccountMetadataServiceImpl accountMetadataService = AccountMetadataServiceImpl.getInstance();

    @Override
    public void execute(ConsentPersistData consentPersistData) throws ConsentException {

        if (consentPersistData.getApproval()) {

            log.debug("Executing CDSBusinessAccountConsentPersistenceStep");
            JSONObject payloadJsonObject = consentPersistData.getPayload();
            Map<String, Map<String, String>> businessAccountIdUserMap;
            ConsentData consentData = consentPersistData.getConsentData();

            //Get an array of consented account details
            ArrayList<String> consentedAccountIdList = CDSConsentPersistUtil.
                    getConsentedAccountIdList(payloadJsonObject);
            JSONArray consentedAccountsJsonArray = CDSConsentPersistUtil.getRequestedAccounts(consentData,
                    consentedAccountIdList);

            //Get a map of business account id against users with their auth type
            businessAccountIdUserMap = getBusinessAccountIdUsersMap(consentedAccountsJsonArray);

            //Abort the flow if any of the users have revoke permission and the config for prioritizing
            // sharable accounts response is enabled.
            if (OpenBankingCDSConfigParser.getInstance().isBNRPrioritizeSharableAccountsResponseEnabled()) {
                if (!validateNominatedRepresentativePermissions(businessAccountIdUserMap)) {
                    log.error("Users that don't have permissions to be nominated representatives are present in " +
                            "the consent request");
                    throw new ConsentException(ResponseStatus.BAD_REQUEST,
                            "Users that don't have permissions to be nominated representatives are present in " +
                                    "the consent request");
                }
            }

            try {
                //Add business nominated representative data to the account metadata table
                addNominatedRepresentativeDataToAccountMetadataTable(businessAccountIdUserMap);
                //Add business account data to consentPersistData
                CDSConsentPersistUtil.addNonPrimaryAccountDataToPersistData(businessAccountIdUserMap,
                        null, consentPersistData);
                //Add customer profile data to consent attributes
                addProfileDataToConsentAttributes(consentPersistData);
            } catch (OpenBankingException e) {
                log.error("Error while adding nominated representative data to account metadata table. " +
                        "Aborting consent persistence", e);
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                        "Error while adding nominated representative data to account metadata table");
            }
        }
    }

    /**
     * Get a map of valid business account id against users with their auth type.
     *
     * @param consentedAccountsJsonArray Consented accounts json array
     * @return businessAccountIdUserMap
     */
    private Map<String, Map<String, String>> getBusinessAccountIdUsersMap(JSONArray consentedAccountsJsonArray) {

        Map<String, Map<String, String>> businessAccountIdUserMap = new HashMap<>();
        for (Object accountDetails : consentedAccountsJsonArray) {
            if (accountDetails instanceof JSONObject) {
                JSONObject accountJsonObject = (JSONObject) accountDetails;
                if (isValidBusinessAccount(accountJsonObject)) {
                    String consentedAccountId = accountJsonObject.getAsString(
                            CDSConsentExtensionConstants.ACCOUNT_ID);
                    businessAccountIdUserMap.put(consentedAccountId, getUsersFromAccount(accountJsonObject));
                }
            }
        }
        return businessAccountIdUserMap;
    }

    /**
     * Get user ids against auth type map from business account.
     *
     * @param businessAccount Business account json object
     * @return map of user ids against auth type
     */
    private Map<String, String> getUsersFromAccount(JSONObject businessAccount) {
        Map<String, String> userIdList = new HashMap<>();
        Object businessAccountInfo = businessAccount.get(CDSConsentExtensionConstants.BUSINESS_ACCOUNT_INFO);
        if (businessAccountInfo instanceof JSONObject) {
            Object accountOwners = ((JSONObject) businessAccountInfo).get(CDSConsentExtensionConstants.ACCOUNT_OWNERS);
            if (accountOwners instanceof JSONArray) {
                for (Object accountOwnerObj : ((JSONArray) accountOwners)) {
                    if (accountOwnerObj instanceof JSONObject) {
                        String accountOwner = ((JSONObject) accountOwnerObj).
                                getAsString(CDSConsentExtensionConstants.MEMBER_ID);
                        if (log.isDebugEnabled()) {
                            log.debug("Added account owner: " + accountOwner + " to the list of users to be " +
                                    "persisted");
                        }
                        userIdList.put(accountOwner, CDSConsentExtensionConstants.BUSINESS_ACCOUNT_OWNER);
                    }
                }
            }
            Object nominatedRepresentatives = ((JSONObject) businessAccountInfo).get(CDSConsentExtensionConstants
                    .NOMINATED_REPRESENTATIVES);
            if (nominatedRepresentatives instanceof JSONArray) {
                for (Object nominatedRepObj : ((JSONArray) nominatedRepresentatives)) {
                    if (nominatedRepObj instanceof JSONObject) {
                        String nominatedRep = ((JSONObject) nominatedRepObj).getAsString(
                                CDSConsentExtensionConstants.MEMBER_ID);
                        if (log.isDebugEnabled()) {
                            log.debug("Added nominated representative: " + nominatedRep + " to the list of users " +
                                    "to be persisted");
                        }
                        userIdList.put(nominatedRep, CDSConsentExtensionConstants.NOMINATED_REPRESENTATIVE);
                    }
                }
            }
        }
        return userIdList;
    }

    /**
     * Check whether the account is a business account.
     *
     * @param accountObject Account received from bank backend
     * @return true if account is a business account
     */
    private boolean isValidBusinessAccount(JSONObject accountObject) {

        String accountType = accountObject.getAsString(CDSConsentExtensionConstants.CUSTOMER_ACCOUNT_TYPE);
        return (CDSConsentExtensionConstants.BUSINESS_PROFILE_TYPE.equalsIgnoreCase(accountType));
    }

    /**
     * Check if any of the users have 'REVOKE' permission and return false.
     *
     * @param businessAccountIdUserMap BusinessAccountIdUserMap
     * @return boolean
     * @throws ConsentException ConsentException
     */
    private boolean validateNominatedRepresentativePermissions(
            Map<String, Map<String, String>> businessAccountIdUserMap) throws ConsentException {

        try {
            for (Map.Entry<String, Map<String, String>> entry : businessAccountIdUserMap.entrySet()) {
                String accountId = entry.getKey();
                Map<String, String> users = entry.getValue();
                for (Map.Entry<String, String> user : users.entrySet()) {
                    String userId = user.getKey();
                    String bnrStatus = accountMetadataService.getAccountMetadataByKey(accountId, userId,
                            CDSConsentExtensionConstants.BNR_PERMISSION);
                    if (CDSConsentExtensionConstants.BNR_REVOKE_PERMISSION.equals(bnrStatus)) {
                        log.error("Business nominated user " + userId + " has REVOKE permission for account " +
                                accountId);
                        return false;
                    }
                }
            }
        } catch (OpenBankingException e) {
            log.error("Error while checking revoke permission for business accounts", e);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    "Error while checking revoke permission for business accounts");
        }
        return true;
    }

    /**
     * Add business nominated representative data to the account metadata table.
     *
     * @param businessAccountIdUserMap BusinessAccountIdUserMap
     * @throws OpenBankingException OpenBankingException
     */
    private void addNominatedRepresentativeDataToAccountMetadataTable(Map<String, Map<String, String>>
                                                                              businessAccountIdUserMap) throws
            OpenBankingException {

        for (Map.Entry<String, Map<String, String>> entry : businessAccountIdUserMap.entrySet()) {
            String accountId = entry.getKey();
            Map<String, String> users = entry.getValue();
            for (Map.Entry<String, String> user : users.entrySet()) {
                String userId = user.getKey();
                String authType = user.getValue();
                String bnrPermission = CDSConsentExtensionConstants.NOMINATED_REPRESENTATIVE.equals(authType) ?
                        CDSConsentExtensionConstants.BNR_AUTHORIZE_PERMISSION :
                        CDSConsentExtensionConstants.BNR_VIEW_PERMISSION;
                if (log.isDebugEnabled()) {
                    log.debug("Adding business nominated representative permission " + bnrPermission +
                            " for user " + userId + " for account " + accountId + " to account metadata table");
                }
                accountMetadataService.addOrUpdateAccountMetadata(accountId, userId,
                        Collections.singletonMap(CDSConsentExtensionConstants.BNR_PERMISSION, bnrPermission));
            }
        }
    }

    /**
     * Add profile-id, profile-name and profile type to consent attributes.
     *
     * @param consentPersistData ConsentPersistData
     */
    private void addProfileDataToConsentAttributes(ConsentPersistData consentPersistData) {
        String selectedProfileId = consentPersistData.getPayload().getAsString(CDSConsentExtensionConstants.
                SELECTED_PROFILE_ID);
        String selectedProfileName = consentPersistData.getPayload().getAsString(CDSConsentExtensionConstants.
                SELECTED_PROFILE_NAME);
        String customerProfileType = CDSConsentExtensionConstants.INDIVIDUAL_PROFILE_ID.
                equals(selectedProfileId) ? CDSConsentExtensionConstants.INDIVIDUAL_PROFILE_TYPE_ATTRIBUTE
                : CDSConsentExtensionConstants.BUSINESS_PROFILE_TYPE_ATTRIBUTE;
        CDSConsentPersistUtil.addConsentAttribute(CDSConsentExtensionConstants.SELECTED_PROFILE_ID, selectedProfileId,
                consentPersistData);
        CDSConsentPersistUtil.addConsentAttribute(CDSConsentExtensionConstants.SELECTED_PROFILE_NAME,
                selectedProfileName, consentPersistData);
        CDSConsentPersistUtil.addConsentAttribute(CDSConsentExtensionConstants.CUSTOMER_PROFILE_TYPE,
                customerProfileType, consentPersistData);
    }

}

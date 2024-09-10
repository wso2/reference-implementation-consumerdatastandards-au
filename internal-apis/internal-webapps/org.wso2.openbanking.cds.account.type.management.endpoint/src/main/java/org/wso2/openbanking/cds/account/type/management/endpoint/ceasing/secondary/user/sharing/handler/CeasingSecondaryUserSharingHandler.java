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

package org.wso2.openbanking.cds.account.type.management.endpoint.ceasing.secondary.user.sharing.handler;

import com.wso2.openbanking.accelerator.account.metadata.service.service.AccountMetadataService;
import com.wso2.openbanking.accelerator.account.metadata.service.service.AccountMetadataServiceImpl;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.identity.retriever.sp.CommonServiceProviderRetriever;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.cds.account.type.management.endpoint.ceasing.secondary.user.sharing.constants.CeasingSecondaryUserEnum;
import org.wso2.openbanking.cds.account.type.management.endpoint.ceasing.secondary.user.sharing.models.LegalEntityItemUpdateDTO;
import org.wso2.openbanking.cds.account.type.management.endpoint.ceasing.secondary.user.sharing.models.LegalEntityListUpdateDTO;
import org.wso2.openbanking.cds.account.type.management.endpoint.ceasing.secondary.user.sharing.models.UsersAccountsLegalEntitiesDTO;
import org.wso2.openbanking.cds.account.type.management.endpoint.constants.AccountTypeManagementConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;


/**
 * Handler Class for Ceasing Secondary User Sharing.
 */
public class CeasingSecondaryUserSharingHandler {

    private static final Log log = LogFactory.getLog(CeasingSecondaryUserSharingHandler.class);
    AccountMetadataService accountMetadataService = AccountMetadataServiceImpl.getInstance();

    /**
     * ----- Block the sharing status for a legal entity -----
     * This method is used to allow an account holder to block a legal entity in order to cease the
     * disclosure initiated by a secondary user for a particular account to that legal entity.
     */
    public void blockLegalEntitySharingStatus(String accountID, String secondaryUserID, String legalEntityID)
            throws OpenBankingException {

        log.debug("Blocking the legal entity sharing status");

        try {

            // Add carbon tenant domain to the secondaryUserID if it does not exist
            if (!secondaryUserID.toLowerCase(Locale.ENGLISH).endsWith
                    (AccountTypeManagementConstants.CARBON_TENANT_DOMAIN)) {
                secondaryUserID = secondaryUserID + AccountTypeManagementConstants.CARBON_TENANT_DOMAIN;
            }

            // Generating the hashmap
            HashMap<String, String> blockedLegalEntityMap = new HashMap<String, String>();

            // Checking the existence of a record in the OB_ACCOUNT_METADATA table for the corresponding
            // to a particular accountID, secondaryUserID and metadataKey
            String responseLegalEntities = accountMetadataService.getAccountMetadataByKey
                    (accountID, secondaryUserID, AccountTypeManagementConstants.METADATA_KEY_BLOCKED_LEGAL_ENTITIES);

            if (responseLegalEntities == null) {
                // Legal entities does not exist for corresponding accountID and secondaryUserID
                blockedLegalEntityMap.put
                        (AccountTypeManagementConstants.METADATA_KEY_BLOCKED_LEGAL_ENTITIES, legalEntityID);
                accountMetadataService.
                        addOrUpdateAccountMetadata(accountID, secondaryUserID, blockedLegalEntityMap);
                log.info("Legal Entity: " + legalEntityID + ", has been successfully blocked!");
            } else {
                // Legal entities exist for corresponding accountID and secondaryUserID
                StringBuilder legalEntitiesMetaDataValue = new StringBuilder(responseLegalEntities);

                // Appending legalEntityID to the existing legalEntityIDs for corresponding accountID and
                // secondaryUserID
                if (legalEntitiesMetaDataValue.indexOf(legalEntityID) != -1) {
                    log.info("Legal Entity: " + legalEntityID + ", has been already blocked!");
                } else {
                    legalEntitiesMetaDataValue.append(",");
                    legalEntitiesMetaDataValue.append(legalEntityID);
                    blockedLegalEntityMap.put(AccountTypeManagementConstants.METADATA_KEY_BLOCKED_LEGAL_ENTITIES,
                            legalEntitiesMetaDataValue.toString());
                    accountMetadataService.addOrUpdateAccountMetadata
                            (accountID, secondaryUserID, blockedLegalEntityMap);
                    log.info("Legal Entity: " + legalEntityID + ", has been successfully blocked!");
                }
            }
        } catch (OpenBankingException e) {
            log.warn("Error occurred while retrieving account metadata", e);
            throw new OpenBankingException("Error occurred while retrieving account metadata", e);
        }
    }

    /**
     * ----- Unblock the sharing status for a legal entity -----
     * This method is used to allow an account holder to block a legal entity in order to cease the
     * disclosure initiated by a secondary user for a particular account to that legal entity.
     */
    public void unblockLegalEntitySharingStatus(String accountID, String secondaryUserID, String legalEntityID)
            throws OpenBankingException {

        log.debug("Unblocking the legal entity sharing status");

        try {
            // Add carbon tenant domain to the secondaryUserID if it does not exist
            if (!secondaryUserID.toLowerCase(Locale.ENGLISH).
                    endsWith(AccountTypeManagementConstants.CARBON_TENANT_DOMAIN)) {
                secondaryUserID = secondaryUserID + AccountTypeManagementConstants.CARBON_TENANT_DOMAIN;
            }

            // Checking the existence of a record in the OB_ACCOUNT_METADATA table for the corresponding
            // to a particular accountID, secondaryUserID and metadataKey
            String responseLegalEntities = accountMetadataService.getAccountMetadataByKey
                    (accountID, secondaryUserID, AccountTypeManagementConstants.METADATA_KEY_BLOCKED_LEGAL_ENTITIES);

            if (responseLegalEntities == null) {
                //Legal entities does not exist for corresponding accountID and secondaryUserID
                log.info("Legal Entity : " + legalEntityID + ", has not been blocked!");
            } else {
                // Legal entities exist for corresponding accountID and secondaryUserID
                StringBuilder legalEntitiesMetaDataValue = new StringBuilder(responseLegalEntities);

                // Removing legalEntityID to the existing legalEntityIDs for corresponding accountID and
                // secondaryUserID
                if (legalEntitiesMetaDataValue.indexOf(legalEntityID) == -1) {
                    log.info("Legal Entity : " + legalEntityID + ", has not been blocked!");
                } else {
                    String[] blockedLegalEntities = responseLegalEntities.split(",");
                    String[] newBlockedLegalEntities = new String[blockedLegalEntities.length - 1];

                    for (int i = 0, j = 0; i < blockedLegalEntities.length; i++) {
                        if (blockedLegalEntities[i].equals(legalEntityID)) {
                            continue;
                        }
                        newBlockedLegalEntities[j++] = blockedLegalEntities[i];
                    }

                    HashMap<String, String> newBlockedLegalEntityMap = new HashMap<String, String>();
                    StringBuilder newBlockedLegalEntitiesSB = new StringBuilder();

                    for (int i = 0; i < newBlockedLegalEntities.length; i++) {
                        newBlockedLegalEntitiesSB.append(newBlockedLegalEntities[i]);
                        if (i != newBlockedLegalEntities.length - 1) {
                            newBlockedLegalEntitiesSB.append(",");
                        }
                    }
                    newBlockedLegalEntityMap.put(AccountTypeManagementConstants.METADATA_KEY_BLOCKED_LEGAL_ENTITIES,
                            newBlockedLegalEntitiesSB.toString());
                    accountMetadataService.addOrUpdateAccountMetadata
                            (accountID, secondaryUserID, newBlockedLegalEntityMap);

                    if (accountMetadataService.getAccountMetadataByKey
                            (accountID, secondaryUserID,
                                    AccountTypeManagementConstants.METADATA_KEY_BLOCKED_LEGAL_ENTITIES).isEmpty()) {
                        accountMetadataService.removeAccountMetadataByKey(accountID, secondaryUserID,
                                AccountTypeManagementConstants.METADATA_KEY_BLOCKED_LEGAL_ENTITIES);
                    }
                    log.info("Legal Entity : " + legalEntityID + ", has been unblocked!");
                }
            }
        } catch (OpenBankingException e) {
            log.warn("Error occurred while retrieving account metadata", e);
            throw new OpenBankingException("Error occurred while retrieving account metadata", e);
        }
    }

    /**
     * ----- Update the sharing status for a legal entity -----
     * This method is used to allow an account holder to block/unblock a legal entity in order to cease the
     * disclosure initiated by a secondary user for a particular account to that legal entity.
     */
    public void updateLegalEntitySharingStatus(LegalEntityListUpdateDTO legalEntityListDTO)
            throws OpenBankingException {

        log.debug("Update the legal entity sharing status");

        try {
            for (LegalEntityItemUpdateDTO legalEntityListItemDTO : legalEntityListDTO.getData()) {

                // Extracting fields in the legalEntityListItemDTO
                String accountID = legalEntityListItemDTO.getAccountID();
                String secondaryUserID = legalEntityListItemDTO.getSecondaryUserID();
                String legalEntityID = legalEntityListItemDTO.getLegalEntityID();
                String legalEntitySharingStatus = legalEntityListItemDTO.getLegalEntitySharingStatus();

                if (Objects.equals(legalEntitySharingStatus, CeasingSecondaryUserEnum.BLOCKED.getValue())) {
                    // Blocking the legal entity, if the legalEntitySharing Status is blocked
                    blockLegalEntitySharingStatus(accountID, secondaryUserID, legalEntityID);
                } else {
                    // Unblocking the legal entity, if the legalEntitySharing Status is active
                    unblockLegalEntitySharingStatus(accountID, secondaryUserID, legalEntityID);
                }
            }
        } catch (OpenBankingException e) {
            log.warn("Error occurred while retrieving account metadata");
            throw new OpenBankingException("Error occurred while retrieving account metadata");
        }
    }

    /**
     * ----- Get accounts, secondary users, legal entities and their sharing status -----
     * This endpoint is designed to get all accounts, secondary users, legal entities and their sharing status
     * bound to the account holder in the consent manager dashboard.
     */
    public UsersAccountsLegalEntitiesDTO getUsersAccountsLegalEntities
    (ArrayList<DetailedConsentResource> responseDetailedConsents,
     UsersAccountsLegalEntitiesDTO responseUsersAccountsLegalEntitiesDTO) throws OpenBankingException {

        log.debug("Getting accounts, secondary users, legal entities and their sharing status.");

        try {
            CommonServiceProviderRetriever commonServiceProviderRetriever = new CommonServiceProviderRetriever();

            ArrayList<String> secondaryUserIDList = new ArrayList<>();

            // Updating - Secondary Users
            for (DetailedConsentResource detailedConsent : responseDetailedConsents) {

                for (AuthorizationResource authorizationResource : detailedConsent.getAuthorizationResources()) {

                    if (authorizationResource.getAuthorizationType().
                            equals(AccountTypeManagementConstants.PRIMARY_MEMBER_AUTH_TYPE)) {

                        // Inserting a non-duplicate secondaryUser to the secondary user list in the
                        // responseUsersAccountsLegalEntitiesDTO
                        UsersAccountsLegalEntitiesDTO.SecondaryUser uniqueSecondaryUser =
                                new UsersAccountsLegalEntitiesDTO.
                                        SecondaryUser(authorizationResource.getUserID(), null);

                        if (!secondaryUserIDList.contains(uniqueSecondaryUser.getSecondaryUserID())) {
                            responseUsersAccountsLegalEntitiesDTO.addSecondaryUser(uniqueSecondaryUser);
                        }
                        secondaryUserIDList.add(uniqueSecondaryUser.getSecondaryUserID());
                    }
                }
            }

            // Updating - Accounts
            for (UsersAccountsLegalEntitiesDTO.SecondaryUser secondaryUser :
                    responseUsersAccountsLegalEntitiesDTO.getSecondaryUsers()) {

                ArrayList<String> accountIDList = new ArrayList<>();

                for (DetailedConsentResource detailedConsent : responseDetailedConsents) {

                    for (AuthorizationResource authorizationResource :
                            detailedConsent.getAuthorizationResources()) {
                        String authorizationID = authorizationResource.getAuthorizationID();

                        for (ConsentMappingResource consentMappingResource : detailedConsent.
                                getConsentMappingResources()) {

                            if (consentMappingResource.getAuthorizationID().equals(authorizationID) &&
                                    authorizationResource.getUserID().equals(secondaryUser.getSecondaryUserID())) {

                                // Inserting a non-duplicate account to the account list for a secondary user in the
                                // responseUsersAccountsLegalEntitiesDTO
                                UsersAccountsLegalEntitiesDTO.Account uniqueAccount =
                                        new UsersAccountsLegalEntitiesDTO.
                                                Account(consentMappingResource.getAccountID(), null);

                                if (!accountIDList.contains(uniqueAccount.getAccountID())) {
                                    secondaryUser.addAccount(uniqueAccount);
                                }
                                accountIDList.add(uniqueAccount.getAccountID());
                            }
                        }
                    }
                }
            }

            // Updating - Legal Entities
            for (UsersAccountsLegalEntitiesDTO.SecondaryUser secondaryUser :
                    responseUsersAccountsLegalEntitiesDTO.getSecondaryUsers()) {

                for (UsersAccountsLegalEntitiesDTO.Account account : secondaryUser.getAccounts()) {
                    String clientID;
                    String secondaryUserID = secondaryUser.getSecondaryUserID();
                    String accountID = account.getAccountID();
                    String legalEntityID = null;
                    String legalEntityName = null;
                    String legalEntitySharingStatus = null;
                    ArrayList<String> legalEntityIDList = new ArrayList<>();

                    for (DetailedConsentResource detailedConsent : responseDetailedConsents) {

                        for (AuthorizationResource authorizationResource :
                                detailedConsent.getAuthorizationResources()) {

                            for (ConsentMappingResource consentMappingResource :
                                    detailedConsent.getConsentMappingResources()) {

                                if (authorizationResource.getUserID().equals(secondaryUserID) &&
                                        consentMappingResource.getAccountID().equals(accountID)) {
                                    clientID = detailedConsent.getClientID();

                                    legalEntityID = commonServiceProviderRetriever.
                                            getAppPropertyFromSPMetaData(clientID,
                                                    AccountTypeManagementConstants.LEGAL_ENTITY_ID);
                                    legalEntityName = commonServiceProviderRetriever.
                                            getAppPropertyFromSPMetaData(clientID,
                                                    AccountTypeManagementConstants.LEGAL_ENTITY_NAME);

                                    String responseLegalEntities = accountMetadataService.
                                            getAccountMetadataByKey
                                                    (accountID, secondaryUserID,
                                                            AccountTypeManagementConstants.
                                                                    METADATA_KEY_BLOCKED_LEGAL_ENTITIES);

                                    if (responseLegalEntities != null) {
                                        String[] blockedLegalEntities = responseLegalEntities.split(",");

                                        for (String blockedLegalEntity : blockedLegalEntities) {
                                            if (legalEntityID.equals(blockedLegalEntity)) {
                                                legalEntitySharingStatus = CeasingSecondaryUserEnum.BLOCKED.getValue();
                                                break;
                                            } else {
                                                legalEntitySharingStatus = CeasingSecondaryUserEnum.ACTIVE.getValue();
                                            }
                                        }
                                    } else {
                                        legalEntitySharingStatus = CeasingSecondaryUserEnum.ACTIVE.getValue();
                                    }

                                    // Inserting a non-duplicate legal entity to the legal entity list for an account
                                    // of a secondary user in the responseUsersAccountsLegalEntitiesDTO
                                    UsersAccountsLegalEntitiesDTO.LegalEntity uniqueLegalEntity =
                                            new UsersAccountsLegalEntitiesDTO.
                                                    LegalEntity(legalEntityID, legalEntityName,
                                                    legalEntitySharingStatus);

                                    if (!legalEntityIDList.contains(uniqueLegalEntity.getLegalEntityID())) {
                                        account.addLegalEntity(uniqueLegalEntity);
                                    }
                                    legalEntityIDList.add(uniqueLegalEntity.getLegalEntityID());
                                }
                            }
                        }
                    }
                }
            }

            // Removing the carbon tenant domain from userID in the response object
            if (responseUsersAccountsLegalEntitiesDTO.getUserID().
                    toLowerCase(Locale.ENGLISH).endsWith(AccountTypeManagementConstants.CARBON_TENANT_DOMAIN)) {
                responseUsersAccountsLegalEntitiesDTO.setUserID
                        (responseUsersAccountsLegalEntitiesDTO.getUserID()
                                .replaceAll("@carbon\\.super$", ""));
            }

            // Removing the carbon tenant domain from secondaryUserID's in the response object
            for (UsersAccountsLegalEntitiesDTO.SecondaryUser secondaryUser :
                    responseUsersAccountsLegalEntitiesDTO.getSecondaryUsers()) {

                if (secondaryUser.getSecondaryUserID().toLowerCase(Locale.ENGLISH).
                        endsWith(AccountTypeManagementConstants.CARBON_TENANT_DOMAIN)) {
                    secondaryUser.setSecondaryUserID
                            (secondaryUser.getSecondaryUserID()
                                    .replaceAll("@carbon\\.super$", ""));
                }
            }
            return responseUsersAccountsLegalEntitiesDTO;
        } catch (OpenBankingException e) {
            log.warn("Error in retrieving data!", e);
            throw new OpenBankingException("Error occurred while retrieving account metadata", e);
        }
    }
}

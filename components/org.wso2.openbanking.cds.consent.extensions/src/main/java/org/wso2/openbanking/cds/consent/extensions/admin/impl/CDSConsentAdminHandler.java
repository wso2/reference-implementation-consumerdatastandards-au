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

package org.wso2.openbanking.cds.consent.extensions.admin.impl;

import com.wso2.openbanking.accelerator.account.metadata.service.service.AccountMetadataService;
import com.wso2.openbanking.accelerator.account.metadata.service.service.AccountMetadataServiceImpl;
import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingRuntimeException;
import com.wso2.openbanking.accelerator.consent.extensions.admin.impl.DefaultConsentAdminHandler;
import com.wso2.openbanking.accelerator.consent.extensions.admin.model.ConsentAdminData;
import com.wso2.openbanking.accelerator.consent.extensions.admin.model.ConsentAdminHandler;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentHistoryResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.constants.ConsentCoreServiceConstants;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;
import org.wso2.openbanking.cds.consent.extensions.common.CDSConsentExtensionConstants;
import org.wso2.openbanking.cds.consent.extensions.common.SecondaryAccountOwnerTypeEnum;
import org.wso2.openbanking.cds.consent.extensions.model.DataClusterSharingDateModel;
import org.wso2.openbanking.cds.consent.extensions.model.PermissionWithSharingDate;
import org.wso2.openbanking.cds.consent.extensions.util.CDSConsentExtensionsUtil;
import org.wso2.openbanking.cds.consent.extensions.util.PermissionsEnum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.wso2.openbanking.cds.consent.extensions.common.CDSConsentExtensionConstants.AUTH_RESOURCE_TYPE_PRIMARY;
import static org.wso2.openbanking.cds.consent.extensions.common.CDSConsentExtensionConstants.CONSENT_STATUS_REVOKED;
import static org.wso2.openbanking.cds.consent.extensions.util.DataClusterSharingDateUtil.getSharingDateMap;

/**
 * Consent admin handler CDS implementation.
 */
public class CDSConsentAdminHandler implements ConsentAdminHandler {
    protected static final String CONSENT_ID = "consentID";
    protected static final String USER_ID = "userID";
    protected static final String AMENDMENT_REASON_ACCOUNT_WITHDRAWAL = "JAMAccountWithdrawal";
    protected static final String CDR_ARRANGEMENT_ID = "cdrArrangementID";
    private static final Log log = LogFactory.getLog(CDSConsentAdminHandler.class);
    private final ConsentCoreServiceImpl consentCoreService;
    private final ConsentAdminHandler defaultConsentAdminHandler;

    public CDSConsentAdminHandler() {
        this.consentCoreService = new ConsentCoreServiceImpl();
        this.defaultConsentAdminHandler = new DefaultConsentAdminHandler();
    }

    public CDSConsentAdminHandler(ConsentCoreServiceImpl consentCoreService, ConsentAdminHandler consentAdminHandler) {
        this.consentCoreService = consentCoreService;
        this.defaultConsentAdminHandler = consentAdminHandler;
    }

    @Override
    public void handleSearch(ConsentAdminData consentAdminData) throws ConsentException {
        this.defaultConsentAdminHandler.handleSearch(consentAdminData);
        OpenBankingCDSConfigParser openBankingCDSConfigParser = OpenBankingCDSConfigParser.getInstance();

        if (openBankingCDSConfigParser.getDOMSEnabled()) {
            updateDOMSStatusForConsentData(consentAdminData);
        }
        // Filter consent data based on the profiles if profiles are available in the query params.
        if (consentAdminData.getQueryParams().containsKey(CDSConsentExtensionConstants.PROFILES)) {
            filterConsentsByProfile(consentAdminData);
        }

        // filter secondary user consents if 'secondaryAccountInfo' is available in the query params.
        if (consentAdminData.getQueryParams().containsKey(CDSConsentExtensionConstants.SECONDARY_ACCOUNT_INFO)) {
            filterSecondaryUserConsents(consentAdminData);
        }

        if (consentAdminData.getQueryParams().containsKey(CDSConsentExtensionConstants.INCLUDE_SHARING_DATES)) {
            try {
                addSharingDatesToPermissions(consentAdminData);
            } catch (OpenBankingException e) {
                log.error("Error while adding sharing dates to permissions", e);
                throw new OpenBankingRuntimeException("Error while adding sharing dates to permissions", e);
            }
        }

        if (consentAdminData.getQueryParams().containsKey(CDSConsentExtensionConstants.CONSENT_IDS_QUERY_PARAM_NAME)) {
            addBNRRevokeStatus(consentAdminData);
        }

    }

    @Override
    public void handleRevoke(ConsentAdminData consentAdminData) throws ConsentException {
        try {
            Map queryParams = consentAdminData.getQueryParams();
            final String consentID = validateAndGetQueryParam(queryParams, CONSENT_ID);

            if (StringUtils.isBlank(consentID)) {
                throw new ConsentException(ResponseStatus.BAD_REQUEST, "Mandatory parameter consent ID not available");
            } else {
                final String userID = validateAndGetQueryParam(queryParams, USER_ID);
                DetailedConsentResource detailedConsentResource = this.consentCoreService.getDetailedConsent(consentID);
                if (detailedConsentResource != null) {
                    String userId = validateAndGetQueryParam(queryParams,
                            CDSConsentExtensionConstants.USER_ID_KEY_NAME);
                    // userId can be null when the request comes from a CustomerCareOfficer
                    if (userId != null && (!canRevokeByBNR(detailedConsentResource, userId))) {
                            throw new ConsentException(ResponseStatus.FORBIDDEN,
                                    "User is not authorized to revoke the consent");
                    }
                    if (StringUtils.isNotBlank(userID) && !isPrimaryUserRevoking(detailedConsentResource, userID)) {
                        // Deactivate consent mappings as secondary consent holder
                        deactivateAccountMappings(detailedConsentResource, userID);
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("Revoke consent for consentID " + consentID);
                        }
                        // Revoke consent as primary consent holder
                        revokeConsentAsPrimaryUser(detailedConsentResource, userID);
                    }
                }
            }
            consentAdminData.setResponseStatus(ResponseStatus.NO_CONTENT);
        } catch (ConsentManagementException e) {
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    "Exception occurred while revoking consents");
        }
    }

    @Override
    public void handleConsentAmendmentHistoryRetrieval(ConsentAdminData consentAdminData) throws ConsentException {

        JSONObject response = new JSONObject();
        String consentID = null;
        String userID = null;
        Map queryParams = consentAdminData.getQueryParams();

        if (validateAndGetQueryParam(queryParams, CDR_ARRANGEMENT_ID) != null) {
            consentID = validateAndGetQueryParam(queryParams, CDR_ARRANGEMENT_ID);
        }
        if (validateAndGetQueryParam(queryParams, USER_ID) != null) {
            userID = validateAndGetQueryParam(queryParams, USER_ID);
        }

        if (StringUtils.isBlank(consentID)) {
            log.error("Request missing the mandatory query parameter cdrArrangementID");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Mandatory query parameter cdrArrangementID " +
                    "not available");
        }

        int count, amendmentCount = 0;

        try {
            Map<String, ConsentHistoryResource> results =
                    this.consentCoreService.getConsentAmendmentHistoryData(consentID);

            DetailedConsentResource currentConsentResource = this.consentCoreService.getDetailedConsent(consentID);

            // userID is null when the request comes from a user with CustomerCareOfficer role in consent manager.
            if (userID == null || isActionByPrimaryUser(currentConsentResource, userID)) {

                JSONArray consentHistory = new JSONArray();
                for (Map.Entry<String, ConsentHistoryResource> result : results.entrySet()) {
                    JSONObject consentResourceJSON = new JSONObject();
                    ConsentHistoryResource consentHistoryResource = result.getValue();
                    DetailedConsentResource detailedConsentResource =
                            consentHistoryResource.getDetailedConsentResource();
                    consentResourceJSON.appendField("historyId", result.getKey());
                    consentResourceJSON.appendField("amendedReason", consentHistoryResource.getReason());
                    consentResourceJSON.appendField("amendedTime", consentHistoryResource.getTimestamp());
                    consentResourceJSON.appendField("previousConsentData",
                            this.detailedConsentToJSON(detailedConsentResource));
                    consentHistory.add(consentResourceJSON);
                }
                response.appendField("cdrArrangementId", currentConsentResource.getConsentID());
                response.appendField("currentConsent", this.detailedConsentToJSON(currentConsentResource));
                response.appendField("consentAmendmentHistory", consentHistory);
                count = consentHistory.size();
                amendmentCount = count;

                String currentConsentStatus = currentConsentResource.getCurrentStatus();
                if (CONSENT_STATUS_REVOKED.equalsIgnoreCase(currentConsentStatus)
                        || OpenBankingConfigParser.getInstance().getStatusWordingForExpiredConsents()
                        .equalsIgnoreCase(currentConsentStatus)) {
                    // remove the consent history entry due consent expiration or consent revocation as it is not
                    // lying under the consent amendments nomenclature in CDS
                    amendmentCount = count - 1;
                }
                JSONObject metadata = new JSONObject();
                metadata.appendField("totalCount", count);
                metadata.appendField("totalAmendmentCount", amendmentCount);
                response.appendField("metadata", metadata);
                consentAdminData.setResponseStatus(ResponseStatus.OK);
                consentAdminData.setResponsePayload(response);
            } else {
                consentAdminData.setResponseStatus(ResponseStatus.FORBIDDEN);
            }
        } catch (ConsentManagementException e) {
            log.error(String.format("Error occurred while retrieving consent history data. %s", e.getMessage()));
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Error occurred while retrieving " +
                    "consent amendment history data");
        }


    }

    private String validateAndGetQueryParam(Map queryParams, String key) {
        if (queryParams.containsKey(key) && (((ArrayList) queryParams.get(key)).get(0) instanceof String)) {
            return (String) ((ArrayList) queryParams.get(key)).get(0);
        }
        return null;
    }

    /**
     * Method to check requested user's authorization resource type is primary.
     *
     * @param detailedConsentResource detailedConsentResource
     * @param userID                  user id
     * @return true if user's authorization resource type is primary
     */
    private boolean isPrimaryUserRevoking(DetailedConsentResource detailedConsentResource, String userID) {
        for (AuthorizationResource authorizationResource : detailedConsentResource.getAuthorizationResources()) {
            if (userID.equals(authorizationResource.getUserID())) {
                return AUTH_RESOURCE_TYPE_PRIMARY.equals(authorizationResource.getAuthorizationType());
            }
        }
        return true;
    }

    /**
     * Method to deactivate account mapping for secondary consent holders.
     *
     * @param detailedConsentResource detailedConsentResource
     * @param secondaryUserID         id of the secondary consent holder
     * @throws ConsentManagementException when deactivate account mappings fail
     */
    private void deactivateAccountMappings(DetailedConsentResource detailedConsentResource, String secondaryUserID)
            throws ConsentManagementException {
        ArrayList<String> mappingIds = new ArrayList<>();
        ArrayList<AuthorizationResource> authorizationResources = detailedConsentResource.getAuthorizationResources();
        ArrayList<ConsentMappingResource> consentMappings = detailedConsentResource.getConsentMappingResources();

        authorizationResources.stream()
                .filter(consentAuthResource -> secondaryUserID.equals(consentAuthResource.getUserID()))
                .findFirst()
                .ifPresent(auth ->
                        // an Auth Resource found for secondary consent holder
                        consentMappings.stream()
                                .filter(mapping -> mapping.getAuthorizationID().equals(auth.getAuthorizationID()))
                                .map(ConsentMappingResource::getAccountID)
                                .forEach(secondaryAccountID ->
                                        // add every mapping resource that has secondary user's accountID to mappingIds
                                        consentMappings.stream()
                                                .filter(resource -> secondaryAccountID.equals(resource.getAccountID()))
                                                .map(ConsentMappingResource::getMappingID)
                                                .forEach(mappingIds::add)
                                )
                );

        if (!mappingIds.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Deactivate consent mapping accounts for consentID %s and userID %s",
                        detailedConsentResource.getConsentID(), secondaryUserID));
            }

            this.consentCoreService.deactivateAccountMappings(mappingIds);

            if (mappingIds.size() == consentMappings.size()) {
                // If all the mappings are being deactivated, revoke the consent
                this.consentCoreService.revokeConsentWithReason(detailedConsentResource.getConsentID(),
                        CONSENT_STATUS_REVOKED, null, ConsentCoreServiceConstants.CONSENT_REVOKE_FROM_DASHBOARD_REASON);
                log.info(String.format("Consent %s revoked as all the mappings are being deactivated",
                        detailedConsentResource.getConsentID()));
            }
        }
        //store joint account withdrawal from the consent to consent amendment history
        this.storeJointAccountWithdrawalHistory(detailedConsentResource);
    }

    private void revokeConsentAsPrimaryUser(DetailedConsentResource detailedConsentResource, String userId)
            throws ConsentManagementException {

        String consentID = detailedConsentResource.getConsentID();
        this.consentCoreService.revokeConsentWithReason(consentID, CONSENT_STATUS_REVOKED, null,
                ConsentCoreServiceConstants.CONSENT_REVOKE_FROM_DASHBOARD_REASON);
        // revoke access tokens if the user is not null. User can be null if the consent is revoked by a user with
        // CustomerCareOfficer role in consent manager.
        if (userId != null) {
            try {
                consentCoreService.revokeTokens(detailedConsentResource, userId);
            } catch (IdentityOAuth2Exception e) {
                log.error(String.format("Error occurred while revoking tokens. Only the consent was revoked " +
                        "successfully. %s", e.getMessage()));
            }
        }
    }

    public JSONObject detailedConsentToJSON(DetailedConsentResource detailedConsentResource) {
        JSONObject consentResource = new JSONObject();

        consentResource.appendField("clientId", detailedConsentResource.getClientID());
        try {
            JSONObject receipt = (JSONObject) (new JSONParser(JSONParser.MODE_PERMISSIVE))
                    .parse(detailedConsentResource.getReceipt());
            JSONArray permissions = (JSONArray) ((JSONObject) receipt.get("accountData")).get("permissions");
            JSONArray cdsPermissions = new JSONArray();
            for (Object scope : permissions) {
                cdsPermissions.add(PermissionsEnum.fromName(scope.toString()));
            }
            consentResource.appendField("permissions", cdsPermissions);
        } catch (ParseException e) {
            log.error(String.format("Error occurred while parsing receipt in consent history. %s", e.getMessage()));
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Exception occurred while parsing" +
                    " receipt");
        }

        consentResource.appendField("consentType", detailedConsentResource.getConsentType());
        consentResource.appendField("currentStatus", detailedConsentResource.getCurrentStatus());
        consentResource.appendField("validityPeriod", detailedConsentResource.getValidityPeriod());
        consentResource.appendField("createdTimestamp", detailedConsentResource.getCreatedTime());
        consentResource.appendField("updatedTimestamp", detailedConsentResource.getUpdatedTime());

        Map<String, String> attMap = detailedConsentResource.getConsentAttributes();
        String sharingDuration = attMap.get("sharing_duration_value");
        String expirationDataTime = attMap.get("ExpirationDateTime");

        consentResource.appendField("sharingDuration", sharingDuration);
        consentResource.appendField("expirationDateTime", expirationDataTime);

        ArrayList<AuthorizationResource> authArray = detailedConsentResource.getAuthorizationResources();
        ArrayList<ConsentMappingResource> mappingArray = detailedConsentResource.getConsentMappingResources();

        Map<String, AuthorizationResource> authResourceMap = new HashMap<>();
        Map<String, Set<String>> availableUserAuthTypesMap = new HashMap<>();
        for (AuthorizationResource authorizationResource : authArray) {
            authResourceMap.put(authorizationResource.getAuthorizationID(), authorizationResource);
            if (availableUserAuthTypesMap.containsKey(authorizationResource.getUserID())) {
                availableUserAuthTypesMap.get(authorizationResource.getUserID())
                        .add(authorizationResource.getAuthorizationType());
            } else {
                Set<String> authTypes = new HashSet<>();
                authTypes.add(authorizationResource.getAuthorizationType());
                availableUserAuthTypesMap.put(authorizationResource.getUserID(), authTypes);
            }
        }
        JSONObject userList = new JSONObject();
        for (Map.Entry<String, Set<String>> userAuthType : availableUserAuthTypesMap.entrySet()) {
            String userId = userAuthType.getKey();
            Set<String> authTypes = userAuthType.getValue();
            JSONArray userAuthTypeAccountsArray = new JSONArray();
            for (String authType : authTypes) {
                JSONArray accountsArray = new JSONArray();
                for (ConsentMappingResource mappingResource : mappingArray) {
                    if (mappingResource.getMappingStatus().equalsIgnoreCase("active") &&
                            authResourceMap.get(mappingResource.getAuthorizationID()).getUserID().equals(userId) &&
                            authResourceMap.get(mappingResource.getAuthorizationID()).getAuthorizationType()
                                    .equals(authType)) {
                        accountsArray.add(mappingResource.getAccountID());
                    }
                }
                if (!accountsArray.isEmpty()) {
                    JSONObject userAuthTypeAccountsObject = new JSONObject();
                    userAuthTypeAccountsObject.appendField("authType", authType);
                    userAuthTypeAccountsObject.appendField("accountList", accountsArray);
                    userAuthTypeAccountsArray.add(userAuthTypeAccountsObject);
                }
            }
            if (!userAuthTypeAccountsArray.isEmpty()) {
                userList.appendField(userId, userAuthTypeAccountsArray);
            }
        }
        consentResource.appendField("userList", userList);
        return consentResource;
    }

    /**
     * Method to filter secondary user created consents for searching user including privileged secondary accounts.
     * QueryParams should contain 'secondaryAccountInfo' as a key and userid as the value.
     *
     * @param consentAdminData consentAdminData
     */
    private void filterSecondaryUserConsents(ConsentAdminData consentAdminData) {

        JSONArray filteredConsentData = new JSONArray();

        for (Object consentObj : (JSONArray) consentAdminData.getResponsePayload().get(
                CDSConsentExtensionConstants.DATA)) {
            JSONObject consent = (JSONObject) consentObj;
            JSONArray secondaryAccountOwnerAuthResources = new JSONArray();
            JSONArray consentAuthResources = (JSONArray) consent.get(
                    CDSConsentExtensionConstants.AUTHORIZATION_RESOURCES);
            JSONArray consentMappingsResources = (JSONArray) consent.get(
                    CDSConsentExtensionConstants.CONSENT_MAPPING_RESOURCES);

            // filter secondary account owner auth resources
            String primaryUserId = null;
            for (Object consentAuthResourceObj : consentAuthResources) {
                if (consentAuthResourceObj instanceof JSONObject) {
                    JSONObject authResource = (JSONObject) consentAuthResourceObj;
                    if (SecondaryAccountOwnerTypeEnum.isValidOwnerType(
                            authResource.getAsString(CDSConsentExtensionConstants.AUTH_TYPE))) {
                        secondaryAccountOwnerAuthResources.add(authResource);
                    } else if (authResource.get(CDSConsentExtensionConstants.AUTH_TYPE)
                            .equals(CDSConsentExtensionConstants.AUTH_RESOURCE_TYPE_PRIMARY)) {
                        primaryUserId = (String) authResource.get(CDSConsentExtensionConstants.USER_ID);
                    }
                }
            }

            // case where consent has secondary accounts of the searching user
            if (!secondaryAccountOwnerAuthResources.isEmpty()) {

                // retrieve secondary account mappings for filtered auth resources
                Map<String, Map<String, String>> accountOwnerAgainstAccountWithPrivilegeMap = new HashMap<>();

                for (Object authResourceObj : secondaryAccountOwnerAuthResources) {
                    if (authResourceObj instanceof JSONObject) {
                        JSONObject authResource = (JSONObject) authResourceObj;
                        String authResourceId = (String) authResource
                                .get(CDSConsentExtensionConstants.AUTHORIZATION_ID);
                        for (Object mappingResourceObj : consentMappingsResources) {
                            if (mappingResourceObj instanceof JSONObject) {
                                JSONObject mappingResource = (JSONObject) mappingResourceObj;
                                if (mappingResource.get(CDSConsentExtensionConstants.AUTHORIZATION_ID)
                                        .equals(authResourceId)) {
                                    Map<String, String> accountWithPrivilegeMap = new HashMap<>();
                                    String accountId = (String) mappingResource.get(
                                            CDSConsentExtensionConstants.ACCOUNT_ID);
                                    Boolean isPrivileged = mappingResource
                                            .get(CDSConsentExtensionConstants.MAPPING_STATUS)
                                            .equals(CDSConsentExtensionConstants.ACTIVE_STATUS) &&
                                            CDSConsentExtensionsUtil.isUserEligibleForSecondaryAccountDataSharing(
                                                    accountId, primaryUserId);

                                    accountWithPrivilegeMap.put((String) mappingResource.get(
                                            CDSConsentExtensionConstants.ACCOUNT_ID), isPrivileged.toString());
                                    accountOwnerAgainstAccountWithPrivilegeMap.put(authResource.getAsString(
                                            CDSConsentExtensionConstants.USER_ID), accountWithPrivilegeMap);
                                }
                            }
                        }
                    }
                }
                // append processed secondary account info to consent object
                consent.appendField(CDSConsentExtensionConstants.SECONDARY_ACCOUNT_INFO,
                        getSecondaryAccountInfoArray(primaryUserId, accountOwnerAgainstAccountWithPrivilegeMap));
                filteredConsentData.add(consent);
            }
        }
        JSONObject responseMetadata = (JSONObject) consentAdminData.getResponsePayload().get(
                CDSConsentExtensionConstants.METADATA);
        responseMetadata.put(CDSConsentExtensionConstants.TOTAL, filteredConsentData.size());
        responseMetadata.put(CDSConsentExtensionConstants.COUNT, filteredConsentData.size());
        consentAdminData.getResponsePayload().put(CDSConsentExtensionConstants.DATA, filteredConsentData);
    }

    private JSONObject getSecondaryAccountInfoArray(String primaryUserId, Map<String, Map<String, String>>
            accountOwnerAgainstAccountWithPrivilegeMap) {
        JSONObject secondaryAccountInfo = new JSONObject();
        JSONArray secondaryAccountList = new JSONArray();
        secondaryAccountInfo.put(CDSConsentExtensionConstants.ACCOUNT_USER, primaryUserId);

        for (Map.Entry<String, Map<String, String>> accountOwnerAgainstAccountWithPrivilege :
                accountOwnerAgainstAccountWithPrivilegeMap.entrySet()) {
            String accountOwnerUserId = accountOwnerAgainstAccountWithPrivilege.getKey();
            Map<String, String> accountWithPrivilegeMap = accountOwnerAgainstAccountWithPrivilege.getValue();
            JSONArray activeAccountList = new JSONArray();
            JSONArray inactiveAccountList = new JSONArray();

            if (accountOwnerUserId.equals(primaryUserId)) {
                continue;
            }

            for (Map.Entry<String, String> accountWithPrivilege : accountWithPrivilegeMap.entrySet()) {
                String accountId = accountWithPrivilege.getKey();

                if (accountWithPrivilege.getValue().equals("true")) {
                    activeAccountList.add(accountId);
                } else {
                    inactiveAccountList.add(accountId);
                }
            }

            JSONObject secondaryAccountObject = new JSONObject();
            secondaryAccountObject.put(CDSConsentExtensionConstants.ACCOUNT_OWNER, accountOwnerUserId);
            secondaryAccountObject.put(CDSConsentExtensionConstants.ACTIVE_ACCOUNTS, activeAccountList);
            secondaryAccountObject.put(CDSConsentExtensionConstants.INACTIVE_ACCOUNTS, inactiveAccountList);
            secondaryAccountList.add(secondaryAccountObject);
        }
        secondaryAccountInfo.put(CDSConsentExtensionConstants.SECONDARY_ACCOUNTS, secondaryAccountList);
        return secondaryAccountInfo;
    }

    private void storeJointAccountWithdrawalHistory(DetailedConsentResource detailedConsentResource)
            throws ConsentManagementException {

        if (OpenBankingConfigParser.getInstance().isConsentAmendmentHistoryEnabled()) {
            ConsentHistoryResource consentHistoryResource = new ConsentHistoryResource();
            consentHistoryResource.setTimestamp(System.currentTimeMillis() / 1000);
            consentHistoryResource.setReason(AMENDMENT_REASON_ACCOUNT_WITHDRAWAL);
            consentHistoryResource.setDetailedConsentResource(detailedConsentResource);

            this.consentCoreService.storeConsentAmendmentHistory(detailedConsentResource.getConsentID(),
                    consentHistoryResource, null);
        }
    }

    private boolean isActionByPrimaryUser(DetailedConsentResource detailedConsentResource, String userID) {
        for (AuthorizationResource authorizationResource : detailedConsentResource.getAuthorizationResources()) {
            if (userID.equals(authorizationResource.getUserID())) {
                return AUTH_RESOURCE_TYPE_PRIMARY.equals(authorizationResource.getAuthorizationType());
            }
        }
        return false;
    }

    private void addBNRRevokeStatus(ConsentAdminData consentAdminData) {

        JSONObject responsePayload = consentAdminData.getResponsePayload();
        JSONArray consentData = (JSONArray) responsePayload.get(CDSConsentExtensionConstants.DATA);
        ArrayList<String> userIDs = (ArrayList<String>) consentAdminData.getQueryParams()
                .get(CDSConsentExtensionConstants.USER_IDS_QUERY_PARAM_NAME);
        if (userIDs == null || userIDs.isEmpty()) {
            // Customer care officer scenario
            consentData.forEach(consent -> {
                JSONObject consentObject = (JSONObject) consent;
                consentObject.put(CDSConsentExtensionConstants.CAN_REVOKE, true);
            });
        } else {
            String userId = userIDs.get(0);
            consentData.forEach(consent -> {
                JSONObject consentObject = (JSONObject) consent;
                consentObject.put(CDSConsentExtensionConstants.CAN_REVOKE, canRevokeByBNR(consentAdminData, userId));
            });
        }
    }

    private boolean canRevokeByBNR(DetailedConsentResource detailedConsentResource, String userId) {

        Set<String> activeAccountIDs = new HashSet<>();
        for (ConsentMappingResource consentMappingResource : detailedConsentResource.getConsentMappingResources()) {
            if (consentMappingResource.getMappingStatus().equals(CDSConsentExtensionConstants.ACTIVE_STATUS)) {
                activeAccountIDs.add(consentMappingResource.getAccountID());
            }
        }

        return canRevokeByBNR(activeAccountIDs, userId);
    }

    private boolean canRevokeByBNR(ConsentAdminData consentAdminData, String userId) {

        Set<String> activeAccountIDs = new HashSet<>();

        for (Object item : (JSONArray) consentAdminData.getResponsePayload().
                get(CDSConsentExtensionConstants.DATA)) {
            JSONObject itemJSONObject = (JSONObject) item;
            JSONArray consentMappingResourcesArray = (JSONArray) itemJSONObject.
                    get(CDSConsentExtensionConstants.CONSENT_MAPPING_RESOURCES);
            for (Object consentMappingResource : consentMappingResourcesArray) {
                JSONObject consentMappingResourceObject = (JSONObject) consentMappingResource;
                if (consentMappingResourceObject.getAsString(CDSConsentExtensionConstants.MAPPING_STATUS).
                        equals(CDSConsentExtensionConstants.ACTIVE_STATUS)) {
                    activeAccountIDs.add(consentMappingResourceObject.getAsString(
                            CDSConsentExtensionConstants.ACCOUNT_ID));
                }
            }
        }
        return canRevokeByBNR(activeAccountIDs, userId);
    }

    private boolean canRevokeByBNR(Set<String> activeAccountIDs, String userId) {

        if (activeAccountIDs.isEmpty()) {
            return true;
        }

        AccountMetadataServiceImpl accountMetadataService = AccountMetadataServiceImpl.getInstance();

        /*
         * For all the active accounts, if the user has AUTHORIZE permission for BNR, then the user can
         * revoke that consent. (i.e. For any given active account in consent mappings, if the user does not have
         * AUTHORIZE permission, he/she cannot revoke the consent)
         */
        for (String accountID : activeAccountIDs) {
            try {
                String permissionStatus = accountMetadataService.getAccountMetadataByKey(accountID, userId,
                        CDSConsentExtensionConstants.BNR_PERMISSION);
                if (StringUtils.isNotBlank(permissionStatus) &&
                        !(CDSConsentExtensionConstants.BNR_AUTHORIZE_PERMISSION.equals(permissionStatus))) {

                    return false;
                }
            } catch (OpenBankingException e) {
                log.error(e.getMessage());
                throw new OpenBankingRuntimeException("Error while getting BNR permissions.", e);
            }
        }

        return true;
    }

    public void updateDOMSStatusForConsentData(ConsentAdminData consentAdminData) {
        try {
            AccountMetadataService accountMetadataService = AccountMetadataServiceImpl.getInstance();

            for (Object item : (JSONArray) consentAdminData.getResponsePayload().
                    get(CDSConsentExtensionConstants.DATA)) {

                JSONObject itemJSONObject = (JSONObject) item;
                JSONArray consentMappingResourcesArray = (JSONArray) itemJSONObject.
                        get(CDSConsentExtensionConstants.CONSENT_MAPPING_RESOURCES);
                JSONArray consentAuthResourcesArray = (JSONArray) itemJSONObject.
                        get(CDSConsentExtensionConstants.AUTHORIZATION_RESOURCES);
                List<String> jointAccountAuthIDs = consentAuthResourcesArray.stream()
                        .map(obj -> (JSONObject) obj)
                        .filter(obj -> {
                            String authType = obj.getAsString(CDSConsentExtensionConstants.AUTH_TYPE);
                            return Arrays.asList(CDSConsentExtensionConstants.AUTH_RESOURCE_TYPE_LINKED,
                                    SecondaryAccountOwnerTypeEnum.JOINT.getValue()).contains(authType);
                        })
                        .map(obj -> obj.getAsString(CDSConsentExtensionConstants.AUTHORIZATION_ID))
                        .collect(Collectors.toList());

                for (Object consentMappingResource : consentMappingResourcesArray) {
                    JSONObject consentMappingResourceObject = (JSONObject) consentMappingResource;
                    String accountId = consentMappingResourceObject.
                            getAsString(CDSConsentExtensionConstants.ACCOUNT_ID);
                    Map<String, String> disclosureOptionsMap = accountMetadataService.getAccountMetadataMap(accountId);
                    if (jointAccountAuthIDs.contains(consentMappingResourceObject.getAsString(
                            CDSConsentExtensionConstants.AUTHORIZATION_ID))) {
                        String disclosureOptionStatus = disclosureOptionsMap.get(CDSConsentExtensionConstants.
                                DOMS_STATUS);

                        // If the disclosure option status is not available or has not been set,
                        // default value is set to the pre-approval status
                        if (disclosureOptionStatus == null) {
                            disclosureOptionStatus = CDSConsentExtensionConstants.DOMS_STATUS_PRE_APPROVAL;
                        }
                        consentMappingResourceObject.put("domsStatus", disclosureOptionStatus);
                    }
                }
            }
        } catch (OpenBankingException e) {
            log.error("Error occurred while updating the DOMS status for consent data", e);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    "An error occurred while updating the DOMS status for consent data");
        }
    }

    /**
     * Filter the consent data based on the profiles.
     *
     * @param consentAdminData Consent admin data.
     */
    public void filterConsentsByProfile(ConsentAdminData consentAdminData) {

        ArrayList profiles = ((ArrayList) consentAdminData.getQueryParams().get(
                CDSConsentExtensionConstants.PROFILES));
        if (profiles.size() > 0) {
            JSONArray filteredConsentData = new JSONArray();
            for (Object consentObj : (JSONArray) consentAdminData.getResponsePayload().get(
                    CDSConsentExtensionConstants.DATA)) {
                JSONObject consent = (JSONObject) consentObj;
                JSONObject consentAttributes = (JSONObject) consent.get(
                        CDSConsentExtensionConstants.CONSENT_ATTRIBUTES);
                if (consentAttributes.containsKey(CDSConsentExtensionConstants.CUSTOMER_PROFILE_TYPE)) {
                    String customerProfileType = consentAttributes.get(
                            CDSConsentExtensionConstants.CUSTOMER_PROFILE_TYPE).toString().split("-")[0];
                    for (Object profile : profiles) {
                        if (profile.toString().equalsIgnoreCase(customerProfileType)) {
                            filteredConsentData.add(consent);
                        }
                    }
                }
            }
            JSONObject responseMetadata = (JSONObject) consentAdminData.getResponsePayload().get(
                    CDSConsentExtensionConstants.METADATA);
            responseMetadata.put(CDSConsentExtensionConstants.TOTAL, filteredConsentData.size());
            responseMetadata.put(CDSConsentExtensionConstants.COUNT, filteredConsentData.size());
            consentAdminData.getResponsePayload().put(CDSConsentExtensionConstants.DATA, filteredConsentData);
        }
    }

    @Override
    public void handleConsentFileSearch(ConsentAdminData consentAdminData) throws ConsentException {
        this.defaultConsentAdminHandler.handleConsentFileSearch(consentAdminData);
    }

    @Override
    public void handleConsentStatusAuditSearch(ConsentAdminData consentAdminData) throws ConsentException {
        this.defaultConsentAdminHandler.handleConsentStatusAuditSearch(consentAdminData);
    }

    @Override
    public void handleTemporaryRetentionDataSyncing(ConsentAdminData consentAdminData) throws ConsentException {
        this.defaultConsentAdminHandler.handleTemporaryRetentionDataSyncing(consentAdminData);
    }

    @Override
    public void handleConsentExpiry(ConsentAdminData consentAdminData) throws ConsentException {
        this.defaultConsentAdminHandler.handleConsentExpiry(consentAdminData);
    }

    public void addSharingDatesToPermissions(ConsentAdminData consentAdminData) throws OpenBankingException {

        final String bankAccountData = "bank_account_data";
        final String bankTransactionData = "bank_transaction_data";
        final String bankPayeeData = "bank_payee_data";
        final String bankRegularPaymentData = "bank_regular_payment_data";
        final String commonCustomerData = "common_customer_data";
        final String profileData = "profile";

        for (Object item : (JSONArray) consentAdminData.getResponsePayload().
                get(CDSConsentExtensionConstants.DATA)) {

            JSONObject itemJSONObject = (JSONObject) item;
            String consentId = itemJSONObject.get(CDSConsentExtensionConstants.CONSENT_ID).toString();
            JSONObject receipt = (JSONObject) itemJSONObject.get(CDSConsentExtensionConstants.RECEIPT);
            JSONObject accountData = (JSONObject) receipt.get(CDSConsentExtensionConstants.ACCOUNT_DATA);
            JSONArray permissions = (JSONArray) accountData.get(CDSConsentExtensionConstants.PERMISSIONS);

            // Get the sharing date data map using the consent ID
            Map<String, DataClusterSharingDateModel> sharingDateDataMap = getSharingDateMap(consentId);

            JSONArray permissionsWithSharingDate = new JSONArray();
            for (Object permission : permissions) {
                PermissionWithSharingDate permissionObj = new PermissionWithSharingDate();
                permissionObj.setPermission((String) permission);
                if (permission.equals(PermissionsEnum.CDRREADACCOUNTSBASIC.toString()) ||
                        permission.equals(PermissionsEnum.CDRREADACCOUNTSDETAILS.toString())) {
                    if (sharingDateDataMap.get(bankAccountData) != null) {
                        permissionObj.setSharingStartDate(sharingDateDataMap.get(bankAccountData)
                                .getSharingStartDate());
                        permissionObj.setLastSharedDate(sharingDateDataMap.get(bankAccountData).getLastSharedDate());
                    }
                } else if (permission.equals(PermissionsEnum.CDRREADTRANSACTION.toString())) {
                    if (sharingDateDataMap.get(bankTransactionData) != null) {
                        permissionObj.setSharingStartDate(sharingDateDataMap.get(bankTransactionData)
                                .getSharingStartDate());
                        permissionObj.setLastSharedDate(sharingDateDataMap.get(bankTransactionData)
                                .getLastSharedDate());
                    }
                } else if (permission.equals(PermissionsEnum.CDRREADPAYEES.toString())) {
                    if (sharingDateDataMap.get(bankPayeeData) != null) {
                        permissionObj.setSharingStartDate(sharingDateDataMap.get(bankPayeeData).getSharingStartDate());
                        permissionObj.setLastSharedDate(sharingDateDataMap.get(bankPayeeData).getLastSharedDate());
                    }
                } else if (permission.equals(PermissionsEnum.CDRREADPAYMENTS.toString())) {
                    if (sharingDateDataMap.get(bankRegularPaymentData) != null) {
                        permissionObj.setSharingStartDate(sharingDateDataMap.get(bankRegularPaymentData)
                                .getSharingStartDate());
                        permissionObj.setLastSharedDate(sharingDateDataMap.get(bankRegularPaymentData)
                                .getLastSharedDate());
                    }
                } else if (permission.equals(PermissionsEnum.READCUSTOMERDETAILSBASIC.toString()) ||
                        permission.equals(PermissionsEnum.READCUSTOMERDETAILS.toString())) {
                    if (sharingDateDataMap.get(commonCustomerData) != null) {
                        permissionObj.setSharingStartDate(sharingDateDataMap.get(commonCustomerData)
                                .getSharingStartDate());
                        permissionObj.setLastSharedDate(sharingDateDataMap.get(commonCustomerData)
                                .getLastSharedDate());
                    }
                } else if (permission.equals(PermissionsEnum.PROFILE.toString()) ||
                        permission.equals(PermissionsEnum.NAME.toString()) ||
                        permission.equals(PermissionsEnum.GIVENNAME.toString()) ||
                        permission.equals(PermissionsEnum.FAMILYNAME.toString()) ||
                        permission.equals(PermissionsEnum.UPDATEDAT.toString()) ||
                        permission.equals(PermissionsEnum.EMAIL.toString()) ||
                        permission.equals(PermissionsEnum.EMAILVERIFIED.toString()) ||
                        permission.equals(PermissionsEnum.PHONENUMBER.toString()) ||
                        permission.equals(PermissionsEnum.PHONENUMBERVERIFIED.toString()) ||
                        permission.equals(PermissionsEnum.ADDRESS.toString())) {
                    if (sharingDateDataMap.get(profileData) != null) {
                        permissionObj.setSharingStartDate(sharingDateDataMap.get(profileData).getSharingStartDate());
                        permissionObj.setLastSharedDate(sharingDateDataMap.get(profileData).getLastSharedDate());
                    }
                }
                permissionsWithSharingDate.add(permissionObj);
            }
            accountData.put(CDSConsentExtensionConstants.PERMISSIONS_WITH_SHARING_DATE, permissionsWithSharingDate);
        }
    }
}

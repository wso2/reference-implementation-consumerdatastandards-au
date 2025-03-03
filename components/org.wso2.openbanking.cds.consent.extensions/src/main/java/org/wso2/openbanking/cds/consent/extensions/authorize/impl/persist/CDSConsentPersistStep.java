/**
 * Copyright (c) 2024-2025, WSO2 LLC. (https://www.wso2.com).
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

import com.google.gson.Gson;
import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistStep;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.mgt.dao.constants.ConsentMgtDAOConstants;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.constants.ConsentCoreServiceConstants;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.openbanking.cds.common.data.publisher.CDSDataPublishingService;
import org.wso2.openbanking.cds.common.enums.AuthorisationStageEnum;
import org.wso2.openbanking.cds.common.utils.CDSCommonUtils;
import org.wso2.openbanking.cds.common.utils.CommonConstants;
import org.wso2.openbanking.cds.consent.extensions.authorize.impl.model.AccountConsentRequest;
import org.wso2.openbanking.cds.consent.extensions.authorize.utils.CDSConsentCommonUtil;
import org.wso2.openbanking.cds.consent.extensions.authorize.utils.CDSDataRetrievalUtil;
import org.wso2.openbanking.cds.consent.extensions.authorize.utils.PermissionsEnum;
import org.wso2.openbanking.cds.consent.extensions.common.CDSConsentExtensionConstants;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;


/**
 * Consent persist step CDS implementation.
 */
public class CDSConsentPersistStep implements ConsentPersistStep {

    private static final Log log = LogFactory.getLog(CDSConsentPersistStep.class);
    private final ConsentCoreServiceImpl consentCoreService;

    public CDSConsentPersistStep() {
        this.consentCoreService = new ConsentCoreServiceImpl();
    }

    public CDSConsentPersistStep(ConsentCoreServiceImpl consentCoreService) {
        this.consentCoreService = consentCoreService;
    }

    @Override
    public void execute(ConsentPersistData consentPersistData) throws ConsentException {

        if (consentPersistData.getApproval()) {
            try {
                ConsentData consentData = consentPersistData.getConsentData();
                JSONObject payloadData = consentPersistData.getPayload();

                // Append tenant domain to user id
                String userId = CDSConsentCommonUtil.getUserIdWithTenantDomain(consentData.getUserId());
                consentData.setUserId(userId);

                ArrayList<String> accountIdList = getAccountIdList(payloadData);
                // get the consent model to be created
                AccountConsentRequest accountConsentRequest = CDSDataRetrievalUtil
                        .getAccountConsent(consentData, consentData.getMetaDataMap().
                                        get(CDSConsentExtensionConstants.EXPIRATION_DATE_TIME).toString(),
                                (List<PermissionsEnum>) consentData.getMetaDataMap()
                                        .get(CDSConsentExtensionConstants.PERMISSIONS));

                Gson gson = new Gson();
                String requestString = gson.toJson(accountConsentRequest);

                // add commonAuthId and sharing_duration_value to consent attributes
                Map<String, String> consentAttributes = addMetaDataToConsentAttributes(consentData, consentPersistData);

                // create new consent resource and set attributes to be stored when consent is created
                ConsentResource consentResource = createConsentAndSetAttributes(consentData, requestString,
                        consentAttributes);

                // Get non primary account data from consentPersistData
                Object nonPrimaryAccountIdAgainstUsersObj = consentPersistData.
                        getMetadata().get(CDSConsentExtensionConstants.NON_PRIMARY_ACCOUNT_ID_AGAINST_USERS_MAP);
                Object userIdAgainstNonPrimaryAccountsObj = consentPersistData.
                        getMetadata().get(CDSConsentExtensionConstants.USER_ID_AGAINST_NON_PRIMARY_ACCOUNTS_MAP);

                // Get permission map for non-primary accounts if provided
                Map<String, ArrayList<String>> nonPrimaryAccountIDsMapWithPermissions = new HashMap<>();
                ArrayList<String> primaryAccounts = new ArrayList<>();
                if (consentPersistData.getMetadata().containsKey(
                        CDSConsentExtensionConstants.NON_PRIMARY_ACCOUNT_ID_WITH_PERMISSIONS_MAP)) {
                    nonPrimaryAccountIDsMapWithPermissions = (Map<String, ArrayList<String>>) consentPersistData.
                            getMetadata().get(CDSConsentExtensionConstants.NON_PRIMARY_ACCOUNT_ID_WITH_PERMISSIONS_MAP);

                    // filter Primary Accounts from account list
                    if (!nonPrimaryAccountIDsMapWithPermissions.isEmpty()) {
                        primaryAccounts = new ArrayList<>();
                        for (String accountId : accountIdList) {
                            if (!nonPrimaryAccountIDsMapWithPermissions.containsKey(accountId)) {
                                primaryAccounts.add(accountId);
                            }
                        }
                    }
                }

                // Consent amendment flow
                if (consentData.getMetaDataMap().containsKey(CDSConsentExtensionConstants.IS_CONSENT_AMENDMENT) &&
                        Boolean.parseBoolean(consentData.getMetaDataMap().get(
                                CDSConsentExtensionConstants.IS_CONSENT_AMENDMENT).toString())) {

                    String cdrArrangementId = consentData.getMetaDataMap().get(CDSConsentExtensionConstants.
                            CDR_ARRANGEMENT_ID).toString();
                    String authResorceId = consentData.getMetaDataMap().get(CDSConsentExtensionConstants.
                            AUTH_RESOURCE_ID).toString();
                    Map<String, ArrayList<String>> accountIdsMapWithPermissions;

                    if (!nonPrimaryAccountIDsMapWithPermissions.isEmpty()) {
                        // get account-permission list when specific non-primary permissions defined
                        accountIdsMapWithPermissions = getCDSAccountIDsMapWithPermissionsWhenPermissionsDefined(
                                accountIdList, nonPrimaryAccountIDsMapWithPermissions);
                    } else {
                        accountIdsMapWithPermissions = getCDSAccountIDsMapWithPermissions(accountIdList);
                    }

                    DetailedConsentResource existingConsent = consentCoreService.getDetailedConsent(cdrArrangementId);
                    // Revoke existing tokens
                    revokeTokens(cdrArrangementId, userId);
                    // Activate account mappings which were deactivated when revoking tokens
                    updateAccountMappings(existingConsent);

                    // Update account mapping permissions
                    ArrayList<ConsentMappingResource> existingConsentMappingResources =
                            existingConsent.getConsentMappingResources();

                    Map<String, String> updatableAccountMappingPermissions = new HashMap<>();
                    for (ConsentMappingResource consentMappingResource : existingConsentMappingResources) {
                        String accountId = consentMappingResource.getAccountID();
                        String oldPermission = consentMappingResource.getPermission();
                        ArrayList<String> permissionList = accountIdsMapWithPermissions.get(accountId);
                        if (authResorceId.equals(consentMappingResource.getAuthorizationID()) &&
                                accountIdsMapWithPermissions.containsKey(accountId)
                                && permissionList != null && !oldPermission.equals(permissionList.get(0))) {
                            updatableAccountMappingPermissions.put(consentMappingResource.getMappingID(),
                                    permissionList.get(0));
                        }
                    }
                    if (!updatableAccountMappingPermissions.isEmpty()) {
                        consentCoreService.updateAccountMappingPermission(updatableAccountMappingPermissions);
                    }

                    // Amend consent data
                    String expirationDateTime = consentData.getMetaDataMap().get(
                            CDSConsentExtensionConstants.EXPIRATION_DATE_TIME).toString();
                    long validityPeriod;
                    if (StringUtils.isNotBlank(expirationDateTime) && !CDSConsentExtensionConstants.
                            ZERO.equals(expirationDateTime)) {
                        validityPeriod = ((OffsetDateTime) consentData.getMetaDataMap()
                                .get(CDSConsentExtensionConstants.EXPIRATION_DATE_TIME)).toEpochSecond();
                    } else {
                        validityPeriod = 0;
                    }

                    // get the amendments related to user consented non-primary accounts
                    Map<String, Object> additionalAmendmentData = new HashMap<>();
                    if (nonPrimaryAccountIdAgainstUsersObj != null && userIdAgainstNonPrimaryAccountsObj != null) {
                        additionalAmendmentData = bindNonPrimaryAccountUsersToConsent(consentResource, consentData,
                                (Map<String, Map<String, List<String>>>) userIdAgainstNonPrimaryAccountsObj, true);
                    }
                    consentCoreService.amendDetailedConsent(cdrArrangementId, consentResource.getReceipt(),
                            validityPeriod, authResorceId, accountIdsMapWithPermissions,
                            CDSConsentExtensionConstants.AUTHORIZED_STATUS, consentAttributes, userId,
                            additionalAmendmentData);
                } else {
                    // create authorizable consent using the consent resource above
                    DetailedConsentResource createdConsent = null;
                    try {
                        createdConsent = createConsent(consentCoreService, consentResource, consentData);
                    } catch (ConsentManagementException e) {
                        log.error(String.format("Error while creating the consent. %s", e.getMessage()));
                        throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                                "Error while creating the consent");
                    }
                    // set consentId for the consentData from obtained from detailed consent resource
                    String consentId = createdConsent.getConsentID();
                    consentData.setConsentId(consentId);

                    // get the latest authorization resource from updated time parameter
                    AuthorizationResource authorizationResource = getLatestAuthResource(createdConsent);

                    consentData.setAuthResource(authorizationResource);
                    consentData.setConsentResource(consentResource);

                    if (consentData.getConsentId() == null && consentData.getConsentResource() == null) {
                        log.error("Consent ID not available in consent data");
                        throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                                "Consent ID not available in consent data");
                    }
                    if (!primaryAccounts.isEmpty()) {
                        // bind user consented own accounts with the created consent
                        bindUserAccountsToConsent(consentCoreService, consentResource, consentData, primaryAccounts);

                        // bind user consented non-primary accounts with the created consent
                        bindNonPrimaryAccountsToConsentWithGivenPermissions(consentCoreService, consentResource,
                                consentData, nonPrimaryAccountIDsMapWithPermissions);

                    } else if (!nonPrimaryAccountIDsMapWithPermissions.isEmpty()) {
                        // bind user consented non-primary accounts with the created consent
                        bindNonPrimaryAccountsToConsentWithGivenPermissions(consentCoreService, consentResource,
                                consentData, nonPrimaryAccountIDsMapWithPermissions);

                    } else {
                        // case where no specific non-primary account permissions provided
                        bindUserAccountsToConsent(consentCoreService, consentResource, consentData, accountIdList);
                    }

                    // bind user consented accounts with the created consent for non-primary users
                    if (nonPrimaryAccountIdAgainstUsersObj != null && userIdAgainstNonPrimaryAccountsObj != null) {
                        bindNonPrimaryAccountUsersToConsent(consentResource, consentData,
                                (Map<String, Map<String, List<String>>>) userIdAgainstNonPrimaryAccountsObj, false);
                    }
                }

                // TODO: Data reporting
            } catch (ConsentManagementException e) {
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                        "Exception occurred while persisting consent");
            }
        }

        publishConsentApprovalStatus(consentPersistData);
    }

    @Generated(message = "Excluding from code coverage since it requires a service call")
    protected DetailedConsentResource createConsent(ConsentCoreServiceImpl consentCoreService,
                                                    ConsentResource requestedConsent, ConsentData consentData)
            throws ConsentManagementException {

        return consentCoreService.createAuthorizableConsent(requestedConsent, consentData.getUserId(),
                CDSConsentExtensionConstants.CREATED_STATUS, CDSConsentExtensionConstants.AUTH_RESOURCE_TYPE_PRIMARY,
                true);
    }

    @Generated(message = "Excluding from code coverage since it requires a service call")
    protected boolean bindUserAccountsToConsent(ConsentCoreServiceImpl consentCoreService,
                                                ConsentResource consentResource, ConsentData consentData,
                                                ArrayList<String> accountIdsString)
            throws ConsentManagementException {

        return consentCoreService.bindUserAccountsToConsent(consentResource, consentData.getUserId(),
                consentData.getAuthResource().getAuthorizationID(), accountIdsString,
                CDSConsentExtensionConstants.AUTHORIZED_STATUS, CDSConsentExtensionConstants.AUTHORIZED_STATUS);
    }

    @Generated(message = "Excluding from code coverage since it requires a service call")
    protected boolean bindNonPrimaryAccountsToConsentWithGivenPermissions(ConsentCoreServiceImpl consentCoreService,
                                              ConsentResource consentResource,
                                              ConsentData consentData,
                                              Map<String, ArrayList<String>> nonPrimaryAccountIDsMapWithPermissions)
            throws ConsentManagementException {

        return consentCoreService.bindUserAccountsToConsent(consentResource, consentData.getUserId(),
                consentData.getAuthResource().getAuthorizationID(), nonPrimaryAccountIDsMapWithPermissions,
                CDSConsentExtensionConstants.AUTHORIZED_STATUS, CDSConsentExtensionConstants.AUTHORIZED_STATUS);
    }

    /**
     * Create consent resource to the given parameters.
     *
     * @param consentData       consent data
     * @param requestString     request string of consent resource
     * @param consentAttributes map of consent attributes
     * @return consentResource
     */
    private ConsentResource createConsentAndSetAttributes(ConsentData consentData, String requestString, Map<String,
            String> consentAttributes) {

        ConsentResource consentResource = new ConsentResource(consentData.getClientId(),
                requestString, consentData.getType(), CDSConsentExtensionConstants.AWAITING_AUTH_STATUS);

        consentResource.setConsentAttributes(consentAttributes);
        consentResource
                .setRecurringIndicator((long) consentData.getMetaDataMap()
                        .get(CDSConsentExtensionConstants.SHARING_DURATION_VALUE) != 0);
        if (!CDSConsentExtensionConstants.ZERO.equalsIgnoreCase(consentData.getMetaDataMap()
                .get(CDSConsentExtensionConstants.EXPIRATION_DATE_TIME).toString())) {
            consentResource.setValidityPeriod(((OffsetDateTime) consentData.getMetaDataMap()
                    .get(CDSConsentExtensionConstants.EXPIRATION_DATE_TIME)).toEpochSecond());
        } else {
            consentResource.setValidityPeriod(0);
        }
        return consentResource;
    }

    /**
     * Add meta data retrieved from web app to consent attributes.
     *
     * @param consentData        consent data
     * @param consentPersistData consent persist data
     * @return Map of consentAttributes to be stored with consent resource
     */
    private Map<String, String> addMetaDataToConsentAttributes(ConsentData consentData,
                                                               ConsentPersistData consentPersistData) {

        Map<String, String> consentAttributes = new HashMap<>();

        consentAttributes.put(CommonConstants.REQUEST_URI_KEY, consentData.getMetaDataMap()
                .get(CommonConstants.REQUEST_URI_KEY).toString());
        consentAttributes.put(CDSConsentExtensionConstants.COMMON_AUTH_ID,
                consentPersistData.getBrowserCookies().get(CDSConsentExtensionConstants.COMMON_AUTH_ID));
        consentAttributes.put(CDSConsentExtensionConstants.SHARING_DURATION_VALUE, consentData.getMetaDataMap()
                .get(CDSConsentExtensionConstants.SHARING_DURATION_VALUE).toString());

        // The ExpirationDateTime attribute needs to be there in order for the periodical consent expiry job to
        // execute as expected
        String expirationDateTimeAttribute = getExpirationDateTimeAttribute(consentData);
        if (StringUtils.isNotBlank(expirationDateTimeAttribute)) {
            consentAttributes.put(ConsentMgtDAOConstants.CONSENT_EXPIRY_TIME_ATTRIBUTE, expirationDateTimeAttribute);
        }

        final Object jointAccountsPayload = consentPersistData.getMetadata()
                .get(CDSConsentExtensionConstants.JOINT_ACCOUNTS_PAYLOAD);
        if (jointAccountsPayload != null && StringUtils.isNotBlank(jointAccountsPayload.toString())) {
            consentAttributes.put(CDSConsentExtensionConstants.JOINT_ACCOUNTS_PAYLOAD, jointAccountsPayload.toString());
        }
        // Add consent attributes from all consent persistence steps.
        if (consentPersistData.getMetadata().containsKey(CDSConsentExtensionConstants.CONSENT_ATTRIBUTES)) {
            Map<String, String> persistStepsAttributes = (Map<String, String>) consentPersistData.getMetadata().
                    get(CDSConsentExtensionConstants.CONSENT_ATTRIBUTES);
            consentAttributes.putAll(persistStepsAttributes);
        }
        // Add userinfo and id token claims as consent attributes.
        if (consentData.getMetaDataMap().containsKey(CDSConsentExtensionConstants.USERINFO_CLAIMS)) {
            consentAttributes.put(CDSConsentExtensionConstants.USERINFO_CLAIMS,
                    consentData.getMetaDataMap().get(CDSConsentExtensionConstants.USERINFO_CLAIMS).toString());
        }
        if (consentData.getMetaDataMap().containsKey(CDSConsentExtensionConstants.ID_TOKEN_CLAIMS)) {
            consentAttributes.put(CDSConsentExtensionConstants.ID_TOKEN_CLAIMS,
                    consentData.getMetaDataMap().get(CDSConsentExtensionConstants.ID_TOKEN_CLAIMS).toString());
        }

        return consentAttributes;
    }

    /**
     * Retrieves the expiration date and time from the given ConsentData object as a string representing epoch seconds.
     *
     * @param consentData consent data
     * @return expiry time in epoch seconds
     */
    private String getExpirationDateTimeAttribute(ConsentData consentData) {

        Object expirationDateTimeObject = consentData.getMetaDataMap()
                .get(CDSConsentExtensionConstants.EXPIRATION_DATE_TIME);
        if (expirationDateTimeObject instanceof OffsetDateTime) {
            OffsetDateTime expirationDateTime = (OffsetDateTime) expirationDateTimeObject;
            return Long.toString(expirationDateTime.toEpochSecond());
        }

        return null;
    }

    /**
     * Get latest authorization using updated time and check whether its null or in proper state.
     *
     * @param createdConsent consent created
     * @return Latest authorization resource
     */
    private AuthorizationResource getLatestAuthResource(DetailedConsentResource createdConsent)
            throws ConsentException {

        // get authorization resources from created consent
        ArrayList<AuthorizationResource> authorizationResources = createdConsent.getAuthorizationResources();

        long updatedTime = 0;
        AuthorizationResource authorizationResource = null;
        if (!authorizationResources.isEmpty()) {
            for (AuthorizationResource authorizationResourceValue : authorizationResources) {
                if (authorizationResourceValue.getUpdatedTime() > updatedTime) {
                    updatedTime = authorizationResourceValue.getUpdatedTime();
                    authorizationResource = authorizationResourceValue;
                }
            }
        }
        if (authorizationResource == null) {
            log.error("Auth resource not available in consent data");
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    "Auth resource not available in consent data");
        }
        if (!authorizationResource.getAuthorizationStatus()
                .equals(CDSConsentExtensionConstants.CREATED_STATUS)) {
            log.error("Authorization not in authorizable state");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Authorization not in authorizable state");
        }
        return authorizationResource;
    }

    /**
     * Get account list from payload data and check for validity.
     *
     * @param payloadData payload data of retrieved from persist data
     * @return List of user consented accounts
     */
    private ArrayList<String> getAccountIdList(JSONObject payloadData) throws ConsentException {

        if (payloadData.get(CDSConsentExtensionConstants.ACCOUNT_IDS) == null
                || !(payloadData.get(CDSConsentExtensionConstants.ACCOUNT_IDS) instanceof JSONArray)) {
            log.error("Account IDs not available in persist request");
            throw new ConsentException(ResponseStatus.BAD_REQUEST,
                    "Account IDs not available in persist request");
        }

        JSONArray accountIds = (JSONArray) payloadData.get(CDSConsentExtensionConstants.ACCOUNT_IDS);
        ArrayList<String> accountIdsList = new ArrayList<>();
        for (Object account : accountIds) {
            if (!(account instanceof String)) {
                log.error("Account IDs format error in persist request");
                throw new ConsentException(ResponseStatus.BAD_REQUEST,
                        "Account IDs format error in persist request");
            }
            accountIdsList.add((String) account);
        }
        return accountIdsList;
    }

    @Generated(message = "Excluding from code coverage since it requires a service call")
    private Map<String, Object> bindNonPrimaryAccountUsersToConsent(
            ConsentResource consentResource,
            ConsentData consentData,
            Map<String, Map<String, List<String>>> userIdAgainstNonPrimaryAccounts,
            boolean isConsentAmendment) throws ConsentManagementException {

        List<String> addedUserAuthTypeMappings = new ArrayList<>();
        // add primary user to already added users auth type mapping list
        addedUserAuthTypeMappings.add(consentData.getUserId() + ":" +
                CDSConsentExtensionConstants.AUTH_RESOURCE_TYPE_PRIMARY);
        // Users who have already stored as auth resources
        List<String> reAuthorizableResources = new ArrayList<>();
        // Users who need to store as auth resources
        Map<String, List<String>> authorizableResources = new HashMap<>();

        final String consentId = StringUtils.isBlank(consentData.getConsentId())
                ? consentData.getMetaDataMap().get(CDSConsentExtensionConstants.CDR_ARRANGEMENT_ID).toString()
                : consentData.getConsentId();
        DetailedConsentResource detailedConsent = consentCoreService.getDetailedConsent(consentId);

        for (Map.Entry<String, Map<String, List<String>>> userEntry : userIdAgainstNonPrimaryAccounts.entrySet()) {

            final String userId = userEntry.getKey();
            for (Map.Entry<String, List<String>> accountEntry : userEntry.getValue().entrySet()) {
                final String authType = accountEntry.getKey();
                String userAuthTypeMapping = userId + ":" + authType;
                if (StringUtils.isNotBlank(userId) && !addedUserAuthTypeMappings.contains(userAuthTypeMapping)) {

                    if (isConsentAmendment && isReauthorizable(userId, accountEntry.getValue(), detailedConsent)) {
                        reAuthorizableResources.add(userAuthTypeMapping);
                    }

                    if (!reAuthorizableResources.contains(userAuthTypeMapping)) {
                        authorizableResources.computeIfAbsent(userId, k -> new ArrayList<>()).add(authType);
                    }

                    addedUserAuthTypeMappings.add(userAuthTypeMapping);
                }
            }
        }

        return createNewAuthResources(consentId, consentResource, authorizableResources,
                userIdAgainstNonPrimaryAccounts, isConsentAmendment);
    }

    @Generated(message = "Excluding from code coverage since it requires a service call")
    private Map<String, Object> createNewAuthResources(
            String consentId, ConsentResource consentResource,
            Map<String, List<String>> authorizableResources,
            Map<String, Map<String, List<String>>> userIdAgainstNonPrimaryAccounts,
            boolean isConsentAmendment) throws ConsentManagementException {

        Map<String, Object> additionalAmendmentData = new HashMap<>();
        Map<String, List<AuthorizationResource>> secondaryUserAuthResources = new HashMap<>();
        Map<String, Map<String, ArrayList<ConsentMappingResource>>> secondaryUserAccountMappings = new HashMap<>();

        consentResource.setConsentID(consentId);
        for (Entry<String, List<String>> authorizableResource : authorizableResources.entrySet()) {
            String userId = authorizableResource.getKey();
            List<String> authTypes = authorizableResource.getValue();

            List<AuthorizationResource> secondaryAuthorizationResourcesList = new ArrayList<>();
            Map<String, ArrayList<ConsentMappingResource>> secondaryUserAccountMappingsMap = new HashMap<>();

            for (String authType : authTypes) {

                ArrayList<ConsentMappingResource> mappingResources = new ArrayList<>();
                AuthorizationResource secondaryAuthResource = getSecondaryAuthorizationResource(consentId, userId,
                        authType);

                if (isConsentAmendment) {
                    // if the flow is a consent amendment, the new joint accounts details are mapped to
                    // AuthorizationResources, ConsentMappingResources against the userId and returned.
                    secondaryAuthorizationResourcesList.add(secondaryAuthResource);

                    for (String accountId : userIdAgainstNonPrimaryAccounts.get(userId).get(authType)) {
                        mappingResources.add(getSecondaryConsentMappingResource(accountId));
                    }
                    secondaryUserAccountMappingsMap.put(authType, mappingResources);
                } else {
                    final AuthorizationResource authorizationResource =
                            consentCoreService.createConsentAuthorization(secondaryAuthResource);

                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Inserting consented accounts for authorizationId: %s, " +
                                        "consentId: %s, authType %s", authorizationResource.getAuthorizationID(),
                                consentResource.getConsentID(), authType));
                    }

                    consentCoreService.bindUserAccountsToConsent(consentResource, userId,
                            authorizationResource.getAuthorizationID(),
                            new ArrayList<>(userIdAgainstNonPrimaryAccounts.get(userId).get(authType)),
                            CDSConsentExtensionConstants.AUTHORIZED_STATUS,
                            CDSConsentExtensionConstants.AUTHORIZED_STATUS);
                }
            }
            secondaryUserAuthResources.put(userId, secondaryAuthorizationResourcesList);
            secondaryUserAccountMappings.put(userId, secondaryUserAccountMappingsMap);
        }
        if (!secondaryUserAuthResources.isEmpty() && !secondaryUserAccountMappings.isEmpty()) {
            additionalAmendmentData.put(ConsentCoreServiceConstants.ADDITIONAL_AUTHORIZATION_RESOURCES_LIST,
                            secondaryUserAuthResources);
            additionalAmendmentData.put(ConsentCoreServiceConstants.ADDITIONAL_MAPPING_RESOURCES_WITH_AUTH_TYPES,
                            secondaryUserAccountMappings);
        }
        return additionalAmendmentData;
    }

    @Generated(message = "Excluding from code coverage since method does not have a logic")
    private AuthorizationResource getSecondaryAuthorizationResource(String consentId, String secondaryUserId,
                                                                    String authType) {

        AuthorizationResource newAuthResource = new AuthorizationResource();
        newAuthResource.setConsentID(consentId);
        newAuthResource.setUserID(secondaryUserId);
        newAuthResource.setAuthorizationStatus(CDSConsentExtensionConstants.CREATED_STATUS);
        newAuthResource.setAuthorizationType(authType);

        return newAuthResource;
    }

    @Generated(message = "Excluding from code coverage since method does not have a logic")
    private ConsentMappingResource getSecondaryConsentMappingResource(String accountId) {

        ConsentMappingResource consentMappingResource = new ConsentMappingResource();
        consentMappingResource.setAccountID(accountId);
        consentMappingResource.setPermission("n/a");
        consentMappingResource.setMappingStatus(ConsentCoreServiceConstants.ACTIVE_MAPPING_STATUS);
        return consentMappingResource;
    }

    @Generated(message = "Excluding from code coverage since method does not have a logic")
    private Map<String, ArrayList<String>> getCDSAccountIDsMapWithPermissions(List<String> accountIds) {
        Map<String, ArrayList<String>> accountIdsMap = new HashMap<>();
        ArrayList<String> permissionsList = new ArrayList<>();
        permissionsList.add("n/a"); // Not applicable for CDS
        for (String accountId : accountIds) {
            accountIdsMap.put(accountId, permissionsList);
        }
        return accountIdsMap;
    }

    /**
     * Get account-permission map when non-primary account permissions are provided.
     *
     * @param accountIds                             - consented accountId list
     * @param nonPrimaryAccountIDsMapWithPermissions - non-primary account permissions map
     * @return Map of user consented accounts with permissions
     */
    private Map<String, ArrayList<String>> getCDSAccountIDsMapWithPermissionsWhenPermissionsDefined(
            List<String> accountIds, Map<String, ArrayList<String>> nonPrimaryAccountIDsMapWithPermissions) {
        Map<String, ArrayList<String>> accountIdsMap = new HashMap<>();

        // prepare non-empty permissions list for primary accounts
        ArrayList<String> permissionsList = new ArrayList<>();
        permissionsList.add("n/a"); // Not applicable for CDS

        for (String accountId : accountIds) {
            if (nonPrimaryAccountIDsMapWithPermissions.containsKey(accountId)) {
                accountIdsMap.put(accountId, nonPrimaryAccountIDsMapWithPermissions.get(accountId));
            } else {
                accountIdsMap.put(accountId, permissionsList);
            }
        }
        return accountIdsMap;
    }

    /**
     * Revoke tokens for the given user id and arrangement id.
     *
     * @param cdrArrangementId - cdr-arrangement-id
     * @param userId           - userId
     * @throws ConsentManagementException - ConsentManagementException
     */
    private void revokeTokens(String cdrArrangementId, String userId) throws ConsentManagementException {
        DetailedConsentResource detailedConsentResource = consentCoreService.getDetailedConsent(cdrArrangementId);

        try {
            consentCoreService.revokeTokens(detailedConsentResource, userId);
        } catch (IdentityOAuth2Exception e) {
            log.error(String.format("Error occurred while revoking tokens. %s", e.getMessage()));
            throw new ConsentManagementException("Error occurred while revoking tokens.", e);
        }
    }

    /**
     * Method to update account mappings based on given consent.
     *
     * @param existingDetailedConsent  - existing detailed consent
     * @throws ConsentException - ConsentException
     */
    private void updateAccountMappings(DetailedConsentResource existingDetailedConsent)
            throws ConsentException {

        try {
            List<ConsentMappingResource> consentMappingResourceList =
                    existingDetailedConsent.getConsentMappingResources();
            for (ConsentMappingResource consentMappingResource : consentMappingResourceList) {
                ArrayList<String> accountMappingID = new ArrayList<>();
                accountMappingID.add(consentMappingResource.getMappingID());
                consentCoreService.updateAccountMappingStatus(accountMappingID,
                        consentMappingResource.getMappingStatus());
            }
        } catch (ConsentManagementException e) {
            log.error(String.format("Error occurred while updating account mappings. %s", e.getMessage()));
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Error while updating account " +
                    "mappings for the consent");
        }
    }

    /**
     * Publishes consent approval or denial data for abandoned consent flow metrics.
     *
     * @param consentPersistData consent persist data
     */
    private void publishConsentApprovalStatus(ConsentPersistData consentPersistData) {

        AuthorisationStageEnum stageEnum;
        if (consentPersistData.getApproval()) {
            stageEnum = AuthorisationStageEnum.CONSENT_APPROVED;
        } else {
            stageEnum = AuthorisationStageEnum.CONSENT_REJECTED;
        }

        ConsentData consentData = consentPersistData.getConsentData();
        String requestUriKey = CDSCommonUtils.getRequestUriKeyFromQueryParams(consentData.getSpQueryParams());

        Map<String, Object> abandonedConsentFlowData = CDSCommonUtils
                .generateAbandonedConsentFlowDataMap(
                        requestUriKey,
                        consentData.getConsentId(),
                        stageEnum);

        CDSDataPublishingService.getCDSDataPublishingService()
                .publishAbandonedConsentFlowData(abandonedConsentFlowData);
    }

    /**
     * Checks if the given mapping resource is re-authorizable based on the user ID and account IDs.
     *
     * @param newUserId                the ID of the user to check.
     * @param newlyConsentedAccountIDs the list of account IDs to validate.
     * @param detailedConsent          the detailed consent object containing authorization and mapping resources.
     * @return true if the resource is re-authorizable, false otherwise.
     */
    private boolean isReauthorizable(String newUserId, List<String> newlyConsentedAccountIDs,
                                     DetailedConsentResource detailedConsent) {

        final List<String> existingUserIDs = detailedConsent.getAuthorizationResources().stream()
                .map(AuthorizationResource::getUserID).collect(Collectors.toList());

        if (!existingUserIDs.contains(newUserId)) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("authorize user %s for consent %s", newUserId, detailedConsent.getConsentID()));
            }
            return false;
        }

        final List<String> existingAuthIDs = detailedConsent.getAuthorizationResources().stream()
                .filter(auth -> auth.getUserID().equals(newUserId))
                .map(AuthorizationResource::getAuthorizationID)
                .collect(Collectors.toList());

        final List<String> existingAccountIDs = detailedConsent.getConsentMappingResources().stream()
                .filter(mapping -> existingAuthIDs.contains(mapping.getAuthorizationID()))
                .map(ConsentMappingResource::getAccountID)
                .collect(Collectors.toList());

        // Return true if all accountIDs are existing.
        if (newlyConsentedAccountIDs.stream().allMatch(existingAccountIDs::contains)) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("authorize user %s for consent %s",
                        newUserId, detailedConsent.getConsentID()));
            }
            return true;
        }

        return false;
    }
}

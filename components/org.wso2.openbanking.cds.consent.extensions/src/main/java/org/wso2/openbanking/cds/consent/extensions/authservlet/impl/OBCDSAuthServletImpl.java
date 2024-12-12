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
package org.wso2.openbanking.cds.consent.extensions.authservlet.impl;

import com.wso2.openbanking.accelerator.account.metadata.service.service.AccountMetadataServiceImpl;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.consent.extensions.authservlet.model.OBAuthServletInterface;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;
import org.wso2.openbanking.cds.common.utils.CommonConstants;
import org.wso2.openbanking.cds.consent.extensions.authorize.utils.CustomerTypeSelectionMethodEnum;
import org.wso2.openbanking.cds.consent.extensions.common.CDSConsentExtensionConstants;
import org.wso2.openbanking.cds.consent.extensions.util.CDSConsentExtensionsUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * The CDS implementation of servlet extension that handles CDS scenarios.
 */
public class OBCDSAuthServletImpl implements OBAuthServletInterface {

    private static final Log log = LogFactory.getLog(OBCDSAuthServletImpl.class);
    String preSelectedProfileId;
    AccountMetadataServiceImpl accountMetadataService = AccountMetadataServiceImpl.getInstance();
    private String userId;
    private String clientID;
    private boolean isConsentAmendment;

    @Override
    public Map<String, Object> updateRequestAttribute(HttpServletRequest httpServletRequest, JSONObject dataSet,
                                                      ResourceBundle resourceBundle) {

        Map<String, Object> returnMaps = new HashMap<>();
        preSelectedProfileId = "";
        userId = dataSet.getString(CDSConsentExtensionConstants.USER_ID);
        clientID = dataSet.getString(CDSConsentExtensionConstants.CLIENT_ID);
        isConsentAmendment = (dataSet.has(CDSConsentExtensionConstants.IS_CONSENT_AMENDMENT) &&
                (boolean) dataSet.get(CDSConsentExtensionConstants.IS_CONSENT_AMENDMENT));

        // Set "data_requested" that contains the human-readable scope-requested information
        JSONArray dataRequestedJsonArray = dataSet.getJSONArray(CDSConsentExtensionConstants.DATA_REQUESTED);
        JSONArray businessDataClusterArray = dataSet.getJSONArray(CDSConsentExtensionConstants.BUSINESS_DATA_CLUSTER);
        Map<String, List<String>> dataRequested = getRequestedDataMap(dataRequestedJsonArray);
        Map<String, List<String>> businessDataCluster = getRequestedDataMap(businessDataClusterArray);
        returnMaps.put(CDSConsentExtensionConstants.DATA_REQUESTED, dataRequested);
        returnMaps.put(CDSConsentExtensionConstants.BUSINESS_DATA_CLUSTER, businessDataCluster);

        // Add accounts list
        JSONArray accountsArray = dataSet.getJSONArray(CDSConsentExtensionConstants.ACCOUNTS);
        List<Map<String, Object>> accountsData = getAccountsDataMap(accountsArray);
        httpServletRequest.setAttribute(CDSConsentExtensionConstants.ACCOUNTS_DATA, accountsData);

        // Add customer profiles list
        if (dataSet.has(CDSConsentExtensionConstants.CUSTOMER_PROFILES_ATTRIBUTE)) {
            JSONArray profilesArray = dataSet.getJSONArray(CDSConsentExtensionConstants.CUSTOMER_PROFILES_ATTRIBUTE);
            List<Map<String, Object>> customerProfilesData = getCustomerProfileDataList(profilesArray);
            httpServletRequest.setAttribute(CDSConsentExtensionConstants.PROFILES_DATA_ATTRIBUTE, customerProfilesData);
        }

        // Add selected profile if available (This can be available either in the consent amendment flow or
        // when only a single profile is present for the user.)
        if (dataSet.has(CDSConsentExtensionConstants.PRE_SELECTED_PROFILE_ID)) {
            httpServletRequest.setAttribute(CDSConsentExtensionConstants.PRE_SELECTED_PROFILE_ID,
                    dataSet.get(CDSConsentExtensionConstants.PRE_SELECTED_PROFILE_ID));
            preSelectedProfileId = (String) dataSet.get(CDSConsentExtensionConstants.PRE_SELECTED_PROFILE_ID);
        }

        // Get Customer Type Selection Method from config
        String customerTypeSelectionMethod = OpenBankingCDSConfigParser.getInstance()
                .getBNRCustomerTypeSelectionMethod();
        if (CustomerTypeSelectionMethodEnum.COOKIE_DATA.toString().equals(customerTypeSelectionMethod)) {
            String preSelectedProfileIdFromCookie = null;
            String customerTypeCookieName = OpenBankingCDSConfigParser.getInstance()
                    .getBNRCustomerTypeSelectionCookieName();

            Cookie[] cookies = httpServletRequest.getCookies();
            for (Cookie cookie : cookies) {
                if (customerTypeCookieName.equalsIgnoreCase(cookie.getName())) {
                    preSelectedProfileIdFromCookie = cookie.getValue();
                    break;
                }
            }

            if (StringUtils.isNotBlank(preSelectedProfileIdFromCookie)) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Retrieved customer profile %s from cookie.",
                            preSelectedProfileIdFromCookie));
                }

                // Setting profile ID as a request attribute when profile selection data is sent using cookies
                try {
                    httpServletRequest.setAttribute(CDSConsentExtensionConstants.PRE_SELECTED_PROFILE_ID,
                            URLEncoder.encode(preSelectedProfileIdFromCookie, "UTF-8"));
                    preSelectedProfileId = preSelectedProfileIdFromCookie;
                } catch (UnsupportedEncodingException e) {
                    log.error("Unable to encode the profile selection cookie value", e);
                }
            }
        }

        //Consent amendment flow
        if (isConsentAmendment) {
            // Add new data requested
            JSONArray newDataRequestedJsonArray = dataSet.getJSONArray(CDSConsentExtensionConstants.NEW_DATA_REQUESTED);
            Map<String, List<String>> newDataRequested = getRequestedDataMap(newDataRequestedJsonArray);
            returnMaps.put(CDSConsentExtensionConstants.NEW_DATA_REQUESTED, newDataRequested);
            JSONArray newBusinessDataRequestedJsonArray =
                    dataSet.getJSONArray(CDSConsentExtensionConstants.NEW_BUSINESS_DATA_CLUSTER);
            Map<String, List<String>> newBusinessDataRequested = getRequestedDataMap(newBusinessDataRequestedJsonArray);
            returnMaps.put(CDSConsentExtensionConstants.NEW_BUSINESS_DATA_CLUSTER, newBusinessDataRequested);
            httpServletRequest.setAttribute(CDSConsentExtensionConstants.IS_CONSENT_AMENDMENT, true);
            if (dataSet.has(CDSConsentExtensionConstants.IS_SHARING_DURATION_UPDATED)) {
                httpServletRequest.setAttribute(CDSConsentExtensionConstants.IS_SHARING_DURATION_UPDATED,
                        dataSet.get(CDSConsentExtensionConstants.IS_SHARING_DURATION_UPDATED));
            }
            returnMaps.put(CDSConsentExtensionConstants.NAME_CLAIMS,
                    dataSet.getString(CDSConsentExtensionConstants.NAME_CLAIMS));
            returnMaps.put(CDSConsentExtensionConstants.CONTACT_CLAIMS,
                    dataSet.getString(CDSConsentExtensionConstants.CONTACT_CLAIMS));
        } else {
            httpServletRequest.setAttribute(CDSConsentExtensionConstants.IS_CONSENT_AMENDMENT, false);
        }

        // Add additional attributes to be displayed
        httpServletRequest.setAttribute(CDSConsentExtensionConstants.SP_FULL_NAME,
                dataSet.getString(CDSConsentExtensionConstants.SP_FULL_NAME));
        httpServletRequest.setAttribute(CDSConsentExtensionConstants.REDIRECT_URL,
                dataSet.getString(CDSConsentExtensionConstants.REDIRECT_URL));
        // Set state parameter if present
        if (dataSet.has(CDSConsentExtensionConstants.STATE) && !dataSet.isNull(CDSConsentExtensionConstants.STATE)) {
            httpServletRequest.setAttribute(CDSConsentExtensionConstants.STATE,
                    dataSet.getString(CDSConsentExtensionConstants.STATE));
        }
        // Check for zero sharing duration and display as once off consent
        if (CDSConsentExtensionConstants.ZERO.equals(dataSet.getString(CDSConsentExtensionConstants.CONSENT_EXPIRY))) {
            httpServletRequest.setAttribute(CDSConsentExtensionConstants.CONSENT_EXPIRY,
                    CDSConsentExtensionConstants.SINGLE_ACCESS_CONSENT);
            httpServletRequest.setAttribute(CDSConsentExtensionConstants.SHARING_DURATION_VALUE, 0);
        } else {
            httpServletRequest.setAttribute(CDSConsentExtensionConstants.CONSENT_EXPIRY,
                    dataSet.getString(CDSConsentExtensionConstants.CONSENT_EXPIRY));
            httpServletRequest.setAttribute(CDSConsentExtensionConstants.SHARING_DURATION_VALUE,
                    dataSet.getInt(CDSConsentExtensionConstants.SHARING_DURATION_VALUE));
        }
        httpServletRequest.setAttribute(CDSConsentExtensionConstants.ACCOUNT_MASKING_ENABLED,
                OpenBankingCDSConfigParser.getInstance().isAccountMaskingEnabled());
        returnMaps.put(CDSConsentExtensionConstants.CUSTOMER_SCOPES_ONLY,
                dataSet.getBoolean(CDSConsentExtensionConstants.CUSTOMER_SCOPES_ONLY));
        return returnMaps;
    }

    @Override
    public Map<String, Object> updateSessionAttribute(HttpServletRequest httpServletRequest, JSONObject jsonObject,
                                                      ResourceBundle resourceBundle) {

        // Setting request uri key as a session attribute to publish in the CDSAccountConfirmServlet.
        String requestUriKey = jsonObject.getString(CommonConstants.REQUEST_URI_KEY);
        Map<String, Object> sessionAttributes = new HashMap<>();
        sessionAttributes.put(CommonConstants.REQUEST_URI_KEY, requestUriKey);
        return sessionAttributes;
    }

    @Override
    public Map<String, Object> updateConsentData(HttpServletRequest httpServletRequest) {

        Map<String, Object> returnMaps = new HashMap<>();

        String[] accounts = httpServletRequest.getParameter(
                CDSConsentExtensionConstants.ACCOUNTS_ARRAY).split(":");
        returnMaps.put(CDSConsentExtensionConstants.ACCOUNT_IDS, new JSONArray(accounts));
        returnMaps.put(CDSConsentExtensionConstants.SELECTED_PROFILE_ID, httpServletRequest.getParameter(
                CDSConsentExtensionConstants.SELECTED_PROFILE_ID));
        returnMaps.put(CDSConsentExtensionConstants.SELECTED_PROFILE_NAME, httpServletRequest.getParameter(
                CDSConsentExtensionConstants.SELECTED_PROFILE_NAME));

        return returnMaps;
    }

    @Override
    public Map<String, String> updateConsentMetaData(HttpServletRequest httpServletRequest) {
        return new HashMap<>();
    }

    @Override
    public String getJSPPath() {
        // Moving the logic of determining the 1st page (profile selection or account selection) to ob_cds.jsp file
        return "/ob_cds.jsp";
    }

    /**
     * Update Individual Personal Account Details.
     *
     * @param account: account object
     * @param data:    data map
     */
    private void updateIndividualPersonalAccountAttributes(JSONObject account, Map<String, Object> data) {
        if ((account != null && account.getBoolean(CDSConsentExtensionConstants.IS_ELIGIBLE)) &&
                (CDSConsentExtensionConstants.INDIVIDUAL_PROFILE_TYPE.equalsIgnoreCase(
                        account.getString(CDSConsentExtensionConstants.CUSTOMER_ACCOUNT_TYPE))
                        && !account.getBoolean(CDSConsentExtensionConstants.IS_JOINT_ACCOUNT_RESPONSE))) {
            data.put(CDSConsentExtensionConstants.IS_SELECTABLE, true);
        }
    }

    /**
     * Update Business/Organization Account Details.
     *
     * @param account: account object
     * @param data:    data map
     */
    private void updateBusinessAccountAttributes(JSONObject account, Map<String, Object> data) {
        if ((account != null && account.getBoolean(CDSConsentExtensionConstants.IS_ELIGIBLE)) &&
                (CDSConsentExtensionConstants.BUSINESS_PROFILE_TYPE.equalsIgnoreCase(
                        account.getString(CDSConsentExtensionConstants.CUSTOMER_ACCOUNT_TYPE)))) {
            boolean isSelectable = true;
            if (isConsentAmendment) {
                try {
                    if (account.get(CDSConsentExtensionConstants.ACCOUNT_ID) != null) {
                        String accountId = (String) account.get(CDSConsentExtensionConstants.ACCOUNT_ID);
                        String permissionStatus = accountMetadataService.getAccountMetadataByKey(accountId, userId,
                                CDSConsentExtensionConstants.BNR_PERMISSION);
                        isSelectable = permissionStatus == null || CDSConsentExtensionConstants.
                                BNR_AUTHORIZE_PERMISSION.equals(permissionStatus);
                    }
                } catch (OpenBankingException e) {
                    log.error("Error occurred while checking nominated representative permissions in the " +
                            "database", e);
                    if (log.isDebugEnabled()) {
                        log.debug("UserId: " + userId + " AccountId: " + account.get(
                                CDSConsentExtensionConstants.ACCOUNT_ID));
                    }
                    throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                            "Error occurred while checking nominated representative permissions in the database");
                }
            }
            data.put(CDSConsentExtensionConstants.IS_SELECTABLE, isSelectable);
        }
    }

    private void updateJointAccountAttributes(JSONObject account, Map<String, Object> data) {
        if (account != null && (account.getBoolean(CDSConsentExtensionConstants.IS_JOINT_ACCOUNT_RESPONSE))
                && !account.getBoolean(CDSConsentExtensionConstants.IS_SECONDARY_ACCOUNT_RESPONSE)) {

            // Check the eligibility of the joint account for data sharing
            String accountID = account.getString("accountId");
            boolean isJointAccount = account.getBoolean("isJointAccount");
            boolean domsPreApprovalStatus;
            try {
                if (isJointAccount) {
                    domsPreApprovalStatus = CDSConsentExtensionsUtil.isDOMSStatusEligibleForDataSharing(accountID);
                    data.put(CDSConsentExtensionConstants.IS_JOINT_ACCOUNT, true);

                    String consentElectionStatus = String
                            .valueOf(account.get(CDSConsentExtensionConstants.JOINT_ACCOUNT_CONSENT_ELECTION_STATUS));
                    data.put(CDSConsentExtensionConstants.IS_SELECTABLE,
                            CDSConsentExtensionConstants.JOINT_ACCOUNT_PRE_APPROVAL.equalsIgnoreCase(
                                    consentElectionStatus)
                                    && domsPreApprovalStatus);

                    JSONObject jointAccountInfo = account.getJSONObject(CDSConsentExtensionConstants.
                            JOINT_ACCOUNT_INFO);
                    if (jointAccountInfo != null) {
                        data.put(CDSConsentExtensionConstants.LINKED_MEMBERS_COUNT,
                                jointAccountInfo.getJSONArray(CDSConsentExtensionConstants.LINKED_MEMBER).length());
                    }
                }
            } catch (OpenBankingException e) {
                String errorMessage = "Error occurred while checking DOMS status for the joint account " +
                        "for data sharing.";
                log.error(errorMessage, e);
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                        "Error while checking DOMS status for the joint account for data sharing");
            }
        }
    }

    /**
     * Update Secondary Account Details.
     *
     * @param account: account object
     * @param data:    data map
     */
    private void updateSecondaryAccountAttributes(JSONObject account, Map<String, Object> data) {
        if (account != null && account.getBoolean(CDSConsentExtensionConstants.IS_SECONDARY_ACCOUNT_RESPONSE)) {
            data.put(CDSConsentExtensionConstants.IS_SECONDARY_ACCOUNT, true);

            // secondaryAccountPrivilegeStatus depicts whether the user has granted permission to share data from
            // the secondary account
            Boolean secondaryAccountPrivilegeStatus = Boolean.valueOf(String
                    .valueOf(account.get(CDSConsentExtensionConstants.SECONDARY_ACCOUNT_PRIVILEGE_STATUS)));

            // Check the eligibility of the secondary joint account for data sharing
            String accountID = account.getString("accountId");
            boolean isJointAccount = account.getBoolean("isJointAccount");
            boolean domsPreApprovalStatus = true;
            try {
                if (isJointAccount) {
                    domsPreApprovalStatus = CDSConsentExtensionsUtil.isDOMSStatusEligibleForDataSharing(accountID);
                }
            } catch (OpenBankingException e) {
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                        "Error while checking DOMS status of the secondary joint accounts for data sharing");
            }
            // secondaryAccountInstructionStatus depicts whether the account is enabled for secondary user data sharing
            Boolean secondaryAccountInstructionStatus =
                    CDSConsentExtensionsUtil.isUserEligibleForSecondaryAccountDataSharing(
                            account.getString(CDSConsentExtensionConstants.ACCOUNT_ID), userId);

            // Check whether the legal entity is blocked for secondary account of a user
            boolean isLegalEntitySharingStatusBlocked =
                    CDSConsentExtensionsUtil.isLegalEntityBlockedForAccountAndUser
                            (account.getString(CDSConsentExtensionConstants.ACCOUNT_ID), userId, clientID);

            // Both secondaryAccountPrivilegeStatus and secondaryAccountInstructionStatus should be in active state and
            // legal entity is not in blocked state for secondary account to be selectable
            Boolean isSelectable = secondaryAccountPrivilegeStatus && secondaryAccountInstructionStatus
                    && !isLegalEntitySharingStatusBlocked;

            // handle secondary joint accounts
            if (account.getBoolean(CDSConsentExtensionConstants.IS_JOINT_ACCOUNT_RESPONSE)) {
                String consentElectionStatus = String
                        .valueOf(account.get(CDSConsentExtensionConstants.JOINT_ACCOUNT_CONSENT_ELECTION_STATUS));
                Boolean isPreApproved = CDSConsentExtensionConstants.JOINT_ACCOUNT_PRE_APPROVAL
                        .equalsIgnoreCase(consentElectionStatus) && domsPreApprovalStatus;

                // For secondary joint account to be selectable, account also should be in pre-approved state.
                data.put(CDSConsentExtensionConstants.IS_SELECTABLE, isPreApproved && isSelectable);
            } else {
                data.put(CDSConsentExtensionConstants.IS_SELECTABLE, isSelectable);
            }
        }
    }

    private Map<String, List<String>> getRequestedDataMap(JSONArray dataRequestedJsonArray) {

        Map<String, List<String>> dataRequested = new LinkedHashMap<>();
        for (int requestedDataIndex = 0; requestedDataIndex < dataRequestedJsonArray.length(); requestedDataIndex++) {
            JSONObject dataObj = dataRequestedJsonArray.getJSONObject(requestedDataIndex);
            String title = dataObj.getString(CDSConsentExtensionConstants.TITLE);
            JSONArray dataArray = dataObj.getJSONArray(CDSConsentExtensionConstants.DATA);

            ArrayList<String> listData = new ArrayList<>();
            for (int dataIndex = 0; dataIndex < dataArray.length(); dataIndex++) {
                listData.add(dataArray.getString(dataIndex));
            }
            dataRequested.put(title, listData);
        }
        return dataRequested;
    }

    private List<Map<String, Object>> getAccountsDataMap(JSONArray accountsArray) {

        List<Map<String, Object>> accountsData = new ArrayList<>();
        for (int accountIndex = 0; accountIndex < accountsArray.length(); accountIndex++) {
            Map<String, Object> data = new HashMap<>();
            JSONObject account = accountsArray.getJSONObject(accountIndex);
            String accountId = account.getString(CDSConsentExtensionConstants.ACCOUNT_ID);
            String accountIdToDisplay = account.has(CDSConsentExtensionConstants.ACCOUNT_ID_DISPLAYABLE)
                    ? account.getString(CDSConsentExtensionConstants.ACCOUNT_ID_DISPLAYABLE) : accountId;
            String displayName = account.getString(CDSConsentExtensionConstants.DISPLAY_NAME);
            String isPreSelectedAccount = "false";
            updateIndividualPersonalAccountAttributes(account, data);
            updateBusinessAccountAttributes(account, data);
            updateJointAccountAttributes(account, data);
            updateSecondaryAccountAttributes(account, data);

            if (account.has(CDSConsentExtensionConstants.IS_PRE_SELECTED_ACCOUNT)) {
                isPreSelectedAccount = account.getString(CDSConsentExtensionConstants.IS_PRE_SELECTED_ACCOUNT);
            }
            data.put(CDSConsentExtensionConstants.ACCOUNT_ID, accountId);
            data.put(CDSConsentExtensionConstants.ACCOUNT_ID_DISPLAYABLE, accountIdToDisplay);
            data.put(CDSConsentExtensionConstants.DISPLAY_NAME, displayName);
            data.put(CDSConsentExtensionConstants.IS_PRE_SELECTED_ACCOUNT, isPreSelectedAccount);
            accountsData.add(data);
        }
        return accountsData;
    }

    /**
     * Get the customer profile data list.
     *
     * @param customerProfilesArray customer profiles array
     * @return customer profile data list
     */
    private List<Map<String, Object>> getCustomerProfileDataList(JSONArray customerProfilesArray) {

        List<Map<String, Object>> profileDataList = new ArrayList<>();
        for (int i = 0; i < customerProfilesArray.length(); i++) {
            JSONObject customerProfile = customerProfilesArray.getJSONObject(i);
            String profileId = customerProfile.getString(CDSConsentExtensionConstants.PROFILE_ID);
            String profileName = customerProfile.getString(CDSConsentExtensionConstants.PROFILE_NAME);
            JSONArray accountIdsArray = customerProfile.getJSONArray(CDSConsentExtensionConstants.ACCOUNT_IDS);

            List<String> accountIdsList = new ArrayList<>();
            for (int j = 0; j < accountIdsArray.length(); j++) {
                String accountId = accountIdsArray.getString(j);
                accountIdsList.add(accountId);
            }

            Map<String, Object> customerProfileMap = new HashMap<>();
            customerProfileMap.put(CDSConsentExtensionConstants.PROFILE_ID, profileId);
            customerProfileMap.put(CDSConsentExtensionConstants.PROFILE_NAME, profileName);
            customerProfileMap.put(CDSConsentExtensionConstants.ACCOUNT_IDS, accountIdsList);

            profileDataList.add(customerProfileMap);
        }
        return profileDataList;
    }

}

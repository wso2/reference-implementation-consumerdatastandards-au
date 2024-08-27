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

import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistStep;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.cds.consent.extensions.authorize.utils.CDSConsentCommonUtil;
import org.wso2.openbanking.cds.consent.extensions.authorize.utils.CDSConsentPersistUtil;
import org.wso2.openbanking.cds.consent.extensions.common.CDSConsentExtensionConstants;
import org.wso2.openbanking.cds.consent.extensions.common.SecondaryAccountOwnerTypeEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Secondary accounts consent persistence step for CDS.
 */
public class CDSSecondaryAccountConsentPersistenceStep implements ConsentPersistStep {

    private static final Log log = LogFactory.getLog(CDSSecondaryAccountConsentPersistenceStep.class);

    @Override
    public void execute(ConsentPersistData consentPersistData) throws ConsentException {
        if (consentPersistData.getApproval()) {

            log.debug("Executing CDSSecondaryAccountConsentPersistenceStep");

            Map<String, Map<String, String>> secondaryAccountIdWithOwners = new HashMap<>();
            Map<String, ArrayList<String>> secondaryAccountIDsMapWithPermissions = new HashMap<>();
            String userId = CDSConsentCommonUtil.getUserIdWithTenantDomain(
                    consentPersistData.getConsentData().getUserId());
            consentPersistData.getConsentData().setUserId(userId);

            // Get details of consented accounts
            ArrayList<String> consentedAccountIdList =
                    CDSConsentPersistUtil.getConsentedAccountIdList(consentPersistData.getPayload());
            JSONArray consentedAccountsJsonArray = CDSConsentPersistUtil.getRequestedAccounts(
                    consentPersistData.getConsentData(), consentedAccountIdList);

            for (Object accountDetails : consentedAccountsJsonArray) {
                if (accountDetails instanceof JSONObject) {
                    JSONObject account = (JSONObject) accountDetails;
                    final String consentedAccountId = account.getAsString(CDSConsentExtensionConstants.ACCOUNT_ID);

                    try {
                        if (isValidSecondaryAccount(account, consentedAccountIdList)) {
                            secondaryAccountIdWithOwners.put(consentedAccountId, getOwnersOfSecondaryAccount(account));

                            // update consent mapping permissions for secondary accounts
                            ArrayList<String> accountMappingPermissions = new ArrayList<>();
                            accountMappingPermissions.add(CDSConsentExtensionConstants.SECONDARY_ACCOUNT_USER);
                            secondaryAccountIDsMapWithPermissions.put(consentedAccountId, accountMappingPermissions);
                        }
                    } catch (ConsentException e) {
                        log.error("Error occurred while validating secondary account: " + consentedAccountId, e);
                        throw new ConsentException(e.getStatus(), e.getMessage());
                    }
                }
            }

            // Add secondary account data to consentPersistData, used in CDSConsentPersistStep.class
            CDSConsentPersistUtil.
                    addNonPrimaryAccountDataToPersistData(secondaryAccountIdWithOwners,
                            secondaryAccountIDsMapWithPermissions, consentPersistData);
        }
    }

    /**
     * Check whether the account is a secondary account.
     *
     * @param consentedAccountIdList: consented account id list
     * @param account:                account received from bank backend
     * @return true if account is a secondary account and is available in consented account list
     */
    private boolean isValidSecondaryAccount(JSONObject account, List<String> consentedAccountIdList)
            throws ConsentException {

        final boolean isSecondaryAccount = Boolean.parseBoolean(account
                .getAsString(CDSConsentExtensionConstants.IS_SECONDARY_ACCOUNT_RESPONSE)) &&
                CDSConsentExtensionConstants.SECONDARY_ACCOUNT_TYPE.equals(account.getAsString(
                        CDSConsentExtensionConstants.CUSTOMER_ACCOUNT_TYPE));

        return isSecondaryAccount && consentedAccountIdList.contains(account.getAsString(
                CDSConsentExtensionConstants.ACCOUNT_ID));
    }

    /**
     * Get secondary account owner against auth type map.
     *
     * @param secondaryAccount: consented secondary account
     * @return list of user ids
     */
    private Map<String, String> getOwnersOfSecondaryAccount(JSONObject secondaryAccount) {
        Map<String, String> userIdPrivilegeMap = new HashMap<>();
        Object secondaryAccountInfo = secondaryAccount.get(CDSConsentExtensionConstants.SECONDARY_ACCOUNT_INFO);
        Boolean isJointAccount = Boolean.parseBoolean(secondaryAccount.getAsString(
                CDSConsentExtensionConstants.IS_JOINT_ACCOUNT_RESPONSE));

        if (secondaryAccountInfo instanceof JSONObject) {
            Object accountOwners = ((JSONObject) secondaryAccountInfo)
                    .get(CDSConsentExtensionConstants.SECONDARY_ACCOUNT_OWNER_LIST);
            if (accountOwners instanceof JSONArray) {
                for (Object accountOwner : ((JSONArray) accountOwners)) {
                    if (accountOwner instanceof JSONObject) {
                        String accountOwnerId = ((JSONObject) accountOwner)
                                .getAsString(CDSConsentExtensionConstants.LINKED_MEMBER_ID);
                        // Add AUTH_TYPE based on account type
                        userIdPrivilegeMap.put(accountOwnerId,
                                isJointAccount ? SecondaryAccountOwnerTypeEnum.JOINT.getValue() :
                                        SecondaryAccountOwnerTypeEnum.INDIVIDUAL.getValue());
                        if (log.isDebugEnabled()) {
                            log.debug("Added secondary account owner:" + accountOwnerId + " to the list of users " +
                                    "to be persisted");
                        }
                    }
                }
            }
        }
        return userIdPrivilegeMap;
    }
}

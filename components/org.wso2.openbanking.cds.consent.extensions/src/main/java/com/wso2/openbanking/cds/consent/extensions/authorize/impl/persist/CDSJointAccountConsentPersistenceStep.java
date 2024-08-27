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

import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistStep;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.cds.consent.extensions.authorize.utils.CDSConsentPersistUtil;
import org.wso2.openbanking.cds.consent.extensions.common.CDSConsentExtensionConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Joint accounts consent persistence step for CDS.
 */
public class CDSJointAccountConsentPersistenceStep implements ConsentPersistStep {

    private static final Log log = LogFactory.getLog(CDSJointAccountConsentPersistenceStep.class);

    @Override
    public void execute(ConsentPersistData consentPersistData) throws ConsentException {

        if (consentPersistData.getApproval()) {

            log.debug("Executing CDSJointAccountConsentPersistenceStep.");
            JSONObject payload = consentPersistData.getPayload();
            ArrayList<String> consentedAccountIdList = CDSConsentPersistUtil.getConsentedAccountIdList(payload);
            Map<String, Map<String, String>> jointAccountIdWithUsers = new HashMap<>();

            ConsentData consentData = consentPersistData.getConsentData();
            JSONArray allAccounts = getAllAccounts(consentData);

            for (Object accountDetails : allAccounts) {
                if (accountDetails instanceof JSONObject) {
                    JSONObject account = (JSONObject) accountDetails;
                    if (isValidJointAccount(account, consentedAccountIdList)) {
                        final String consentedAccountId = account.getAsString(CDSConsentExtensionConstants.ACCOUNT_ID);
                        jointAccountIdWithUsers.put(consentedAccountId, getUsersFromAccount(account));
                    }
                }
            }

            // Add joint account data to consentPersistData, used in CDSConsentPersistStep.class
            CDSConsentPersistUtil.addNonPrimaryAccountDataToPersistData(jointAccountIdWithUsers,
                    null, consentPersistData);

        }
    }

    /**
     * Check whether joint account is sharable.
     *
     * @param consentedAccountIdList: consented account id list
     * @param account:                account received from bank backend
     * @return true if account is a pre-approved joint account
     */
    private boolean isValidJointAccount(JSONObject account, List<String> consentedAccountIdList) {

        final boolean isJointAccount = Boolean.parseBoolean(account
                .getAsString(CDSConsentExtensionConstants.IS_JOINT_ACCOUNT_RESPONSE)) &&
                CDSConsentExtensionConstants.INDIVIDUAL_PROFILE_TYPE.equals(account.getAsString(
                        CDSConsentExtensionConstants.CUSTOMER_ACCOUNT_TYPE));

        if (isJointAccount) {
            final String accountId = account.getAsString(CDSConsentExtensionConstants.ACCOUNT_ID);
            final String consentElectionStatus = account
                    .getAsString(CDSConsentExtensionConstants.JOINT_ACCOUNT_CONSENT_ELECTION_STATUS);

            final boolean isSelectableAccount = CDSConsentExtensionConstants.JOINT_ACCOUNT_PRE_APPROVAL
                    .equalsIgnoreCase(consentElectionStatus);

            return isSelectableAccount && consentedAccountIdList.contains(accountId);
        }
        return false;
    }

    /**
     * Get user ids list from joint account.
     *
     * @param jointAccount: consented joint account
     * @return list of user ids
     */
    private Map<String, String> getUsersFromAccount(JSONObject jointAccount) {
        Map<String, String> userIdList = new HashMap<>();
        Object jointAccountInfo = jointAccount.get(CDSConsentExtensionConstants.JOINT_ACCOUNT_INFO);
        if (jointAccountInfo instanceof JSONObject) {
            Object linkedMembers = ((JSONObject) jointAccountInfo).get(CDSConsentExtensionConstants.LINKED_MEMBER);
            if (linkedMembers instanceof JSONArray) {
                for (Object linkedMember : ((JSONArray) linkedMembers)) {
                    if (linkedMember instanceof JSONObject) {
                        userIdList.put(((JSONObject) linkedMember)
                                        .getAsString(CDSConsentExtensionConstants.LINKED_MEMBER_ID),
                                CDSConsentExtensionConstants.LINKED_MEMBER_AUTH_TYPE);
                    }
                }
            }
        }
        return userIdList;
    }

    /**
     * Get all accounts from consent data.
     *
     * @param consentData: consent data from consentPersistData
     * @return JSONArray of accounts data
     */
    private JSONArray getAllAccounts(ConsentData consentData) {

        Object accountsObj = consentData.getMetaDataMap().get(CDSConsentExtensionConstants.ACCOUNTS);
        if (accountsObj instanceof JSONArray) {
            return (JSONArray) accountsObj;
        } else {
            return new JSONArray();
        }
    }

}

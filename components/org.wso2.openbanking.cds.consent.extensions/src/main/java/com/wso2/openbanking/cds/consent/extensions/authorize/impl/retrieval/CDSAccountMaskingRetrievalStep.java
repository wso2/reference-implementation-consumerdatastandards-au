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
package org.wso2.openbanking.cds.consent.extensions.authorize.impl.retrieval;

import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentRetrievalStep;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;
import org.wso2.openbanking.cds.consent.extensions.common.CDSConsentExtensionConstants;

/**
 * CDS account ID masking step.
 */
public class CDSAccountMaskingRetrievalStep implements ConsentRetrievalStep {

    @Override
    public void execute(ConsentData consentData, JSONObject jsonObject) throws ConsentException {

        boolean isAccountMaskingEnabled = OpenBankingCDSConfigParser.getInstance().isAccountMaskingEnabled();

        if (isAccountMaskingEnabled) {
            JSONArray accountsJSON = (JSONArray) jsonObject.get(CDSConsentExtensionConstants.ACCOUNTS);
            JSONArray updatedAccountsJSON = new JSONArray();

            for (Object accountElement : accountsJSON) {
                JSONObject account = (JSONObject) accountElement;
                String accountId = (String) account.get(CDSConsentExtensionConstants.ACCOUNT_ID);
                String accountNumberDisplay = getDisplayableAccountNumber(accountId);
                account.put(CDSConsentExtensionConstants.ACCOUNT_ID_DISPLAYABLE, accountNumberDisplay);
                updatedAccountsJSON.add(account);

            }
            jsonObject.put(CDSConsentExtensionConstants.ACCOUNTS, updatedAccountsJSON);
        }
    }

    /**
     * Account number masking is performed in this method. Logic is executed when the account ID length is 2 or higher.
     * The logic is handled like this because the specification doesn't mention the exact length of an account ID.
     * <p>
     * If the account ID length is less than 4, mask all but the last character.
     * If the account ID length is exactly 4, mask all but the last two characters.
     * If the length is greater than 4, mask all but the last 4 characters.
     *
     * @param accountId plain account id.
     * @return account number in the displayable masked format.
     */
    protected String getDisplayableAccountNumber(String accountId) {

        int accountIdLength = accountId.length();

        if (accountIdLength > 1) {
            if (accountIdLength < 4) {
                // If the length is less than 4, mask all but the last character
                String maskedPart = StringUtils.repeat('*', accountIdLength - 1);
                String visiblePart = StringUtils.right(accountId, 1);
                return maskedPart + visiblePart;
            } else if (accountIdLength == 4) {
                // If the length is exactly 4, mask all but the last two characters
                return "**" + StringUtils.right(accountId, 2);
            } else {
                // If the length is greater than 4, mask all but the last 4 characters
                String maskedPart = StringUtils.repeat('*', accountIdLength - 4);
                String visiblePart = StringUtils.right(accountId, 4);
                return maskedPart + visiblePart;
            }
        }
        return accountId;
    }
}

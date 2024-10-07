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
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;
import org.wso2.openbanking.cds.consent.extensions.authorize.utils.CDSDataRetrievalUtil;
import org.wso2.openbanking.cds.consent.extensions.common.CDSConsentExtensionConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * Account List retrieval step CDS implementation.
 */
public class CDSAccountListRetrievalStep implements ConsentRetrievalStep {

    private static final Log log = LogFactory.getLog(CDSAccountListRetrievalStep.class);

    @Override
    public void execute(ConsentData consentData, JSONObject jsonObject) throws ConsentException {

        if (consentData.isRegulatory()) {
            String accountsURL = (String) OpenBankingCDSConfigParser.getInstance().getConfiguration()
                    .get(CDSConsentExtensionConstants.SHARABLE_ACCOUNTS_ENDPOINT);

            if (StringUtils.isNotBlank(accountsURL)) {
                Map<String, String> parameters = new HashMap<>();
                parameters.put(CDSConsentExtensionConstants.USER_ID_KEY_NAME, consentData.getUserId());
                String accountData = CDSDataRetrievalUtil.getAccountsFromEndpoint(accountsURL, parameters,
                        new HashMap<>());

                if (StringUtils.isBlank(accountData)) {
                    log.error("Unable to load accounts data for the user: " + consentData.getUserId());
                    throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                            "Exception occurred while getting accounts data");
                }
                JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
                try {
                    JSONObject jsonAccountData = (JSONObject) parser.parse(accountData);
                    JSONArray accountsJSON = (JSONArray) jsonAccountData.get(CDSConsentExtensionConstants.DATA);

                    //Consent amendment flow. Mark pre-selected accounts
                    if (jsonObject.containsKey(CDSConsentExtensionConstants.IS_CONSENT_AMENDMENT) &&
                            (boolean) jsonObject.get(CDSConsentExtensionConstants.IS_CONSENT_AMENDMENT)) {
                        if (jsonObject.containsKey(CDSConsentExtensionConstants.PRE_SELECTED_ACCOUNT_LIST)) {
                            JSONArray preSelectedAccounts = (JSONArray) jsonObject.get(CDSConsentExtensionConstants.
                                    PRE_SELECTED_ACCOUNT_LIST);

                            accountsJSON.forEach(account -> {
                                JSONObject accountJson = (JSONObject) account;
                                if (accountJson.containsKey(CDSConsentExtensionConstants.ACCOUNT_ID) &&
                                        preSelectedAccounts.contains(accountJson.
                                                get(CDSConsentExtensionConstants.ACCOUNT_ID))) {
                                    ((JSONObject) account).appendField(CDSConsentExtensionConstants.
                                            IS_PRE_SELECTED_ACCOUNT, "true");
                                }
                            });
                        }
                    }
                    jsonObject.appendField(CDSConsentExtensionConstants.ACCOUNTS, accountsJSON);
                    consentData.addData(CDSConsentExtensionConstants.ACCOUNTS, accountsJSON);
                } catch (ParseException e) {
                    throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                            "Exception occurred while parsing accounts data");
                }
            } else {
                log.error("Sharable accounts endpoint is not configured properly");
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                        "Sharable accounts endpoint is not configured properly");
            }
        }
    }
}

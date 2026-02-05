/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.openbanking.consumerdatastandards.au;

import com.nimbusds.jose.JOSEException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.openbanking.consumerdatastandards.au.constants.DOMSEnforcementConstants;
import org.wso2.openbanking.consumerdatastandards.au.utils.DOMSEnforcementUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Mediator to remove DOMS-blocked accounts from the Account-Request-Information JWT header.
 */
public class DOMSEnforcementMediator extends AbstractMediator {

    private static final Log log = LogFactory.getLog(DOMSEnforcementMediator.class);

    private String domsGetApi;
    private String domsBasicAuthCredentials;

    public void setDomsGetApi(String domsGetApi) {
        this.domsGetApi = domsGetApi;
    }

    public void setDomsBasicAuthCredentials(String domsBasicAuthCredentials) {
        this.domsBasicAuthCredentials = domsBasicAuthCredentials;
    }

    @Override
    public boolean mediate(MessageContext messageContext) {

        log.info("Starting DOMS enforcement mediation");

        try {
            org.apache.axis2.context.MessageContext axis2Ctx =
                    ((Axis2MessageContext) messageContext).getAxis2MessageContext();

            // Use the instance fields set by Synapse
            String blockedAccountsApi = this.domsGetApi;
            String basicAuthBase64 = this.domsBasicAuthCredentials;

            Map<String, String> headers = (Map<String, String>)
                    axis2Ctx.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

            if (headers == null) {
                log.warn("No transport headers found");
                return true;
            }

            String accountHeaderJwt = headers.get("Account-Request-Information");

            if (accountHeaderJwt == null || accountHeaderJwt.isEmpty()) {
                log.warn("No Account-Request-Information header found, skipping DOMS enforcement.");
                return true;
            }

            // Decode JWT
            JSONObject payload = DOMSEnforcementUtils.decodeJWT(accountHeaderJwt);

            JSONArray accounts = payload.optJSONArray("consentMappingResources");

            if (accounts == null) {
                log.warn("No consentMappingResources array found in JWT");
                return true;
            }

            Set<String> accountIds = new HashSet<>();
            for (int i = 0; i < accounts.length(); i++) {
                JSONObject acc = accounts.getJSONObject(i);
                accountIds.add(acc.optString("account_id"));
            }

            Set<String> blockedAccounts = fetchBlockedAccountsFromService(accountIds,
                    blockedAccountsApi, basicAuthBase64);

            JSONArray filteredAccounts = new JSONArray();
            for (int i = 0; i < accounts.length(); i++) {
                JSONObject acc = accounts.getJSONObject(i);
                String accountId = acc.optString("account_id");

                if (!blockedAccounts.contains(accountId)) {
                    acc.put("accountId", accountId);
                    acc.remove("account_id");
                    filteredAccounts.put(acc);
                } else {
                    log.info("Blocking DOMS account: " + accountId);
                }
            }

            payload.put("consentMappingResources", filteredAccounts);

            String signedJwt = DOMSEnforcementUtils.generateJWT(payload.toString());
            headers.put(DOMSEnforcementConstants.INFO_HEADER_TAG, signedJwt);

            log.info("DOMS enforcement completed successfully");

        } catch (ParseException | JOSEException e) {
            log.error("Error during DOMS enforcement mediation", e);
        }

        return true;
    }

    private Set<String> fetchBlockedAccountsFromService(
            Set<String> accountIds, String blockedAccountsApi, String basicAuthBase64) {

        Set<String> blockedAccounts = new HashSet<>();

        try {
            JSONObject requestJson = new JSONObject();
            requestJson.put("accountIds", new JSONArray(accountIds));

            URL url = new URL(blockedAccountsApi);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.setRequestProperty("Content-Type", "application/json");

            if (basicAuthBase64 != null && !basicAuthBase64.isEmpty()) {
                conn.setRequestProperty("Authorization", "Basic " + basicAuthBase64);
            } else {
                log.warn("Basic Auth property not set, request may fail");
            }
            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestJson.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            int responseCode = conn.getResponseCode();

            if (responseCode == 200) {
                try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8)) {
                    String response = scanner.useDelimiter("\\A").next();
                    JSONObject responseJson = new JSONObject(response);
                    JSONArray blockedArray = responseJson.optJSONArray("blockedAccountIds");

                    if (blockedArray != null) {
                        for (int i = 0; i < blockedArray.length(); i++) {
                            blockedAccounts.add(blockedArray.getString(i));
                        }
                    }
                }
            } else {
                log.warn("Blocked accounts service returned HTTP " + responseCode);
            }

        } catch (IOException e) {
            log.error("Error calling blocked accounts service", e);
        }

        return blockedAccounts;
    }
}

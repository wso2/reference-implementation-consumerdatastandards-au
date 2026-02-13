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
import org.wso2.openbanking.consumerdatastandards.au.utils.Generated;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Map;
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

        log.debug("Starting DOMS enforcement mediation");

        try {
            org.apache.axis2.context.MessageContext axis2Ctx =
                    ((Axis2MessageContext) messageContext).getAxis2MessageContext();

            // Use the instance fields set by Synapse
            String blockedAccountsApi = this.domsGetApi;
            String basicAuthBase64 = this.domsBasicAuthCredentials;

            Map<String, String> headers = (Map<String, String>)
                    axis2Ctx.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

            String accountHeaderJwt = headers.get(DOMSEnforcementConstants.INFO_HEADER_TAG);

            // Decode JWT
            JSONObject payload = new JSONObject(accountHeaderJwt);

            //collect linkedMember authorization Ids
            Set<String> linkedMemberAuthIds = new HashSet<>();

            JSONArray authorizationResources = payload.optJSONArray(DOMSEnforcementConstants.AUTH_RESOURCES_TAG);
            if (authorizationResources != null) {
                JSONArray filteredAuthorizationResources = new JSONArray();

                for (int i = 0; i < authorizationResources.length(); i++) {
                    JSONObject authResource = authorizationResources.getJSONObject(i);

                    // Removing Auth resources of linked members
                    if (DOMSEnforcementConstants.LINKED_MEMBER_TAG.equalsIgnoreCase(
                            authResource.optString(DOMSEnforcementConstants.AUTH_TYPE_TAG))) {
                        String linkedAuthId = authResource.optString(DOMSEnforcementConstants.AUTH_ID_TAG);
                        if (linkedAuthId != null && !linkedAuthId.isEmpty()) {
                            linkedMemberAuthIds.add(linkedAuthId);
                        }
                        if (log.isDebugEnabled()) {
                            log.debug("Removing linkedMember authorization resource. authorizationId="
                                    + linkedAuthId);
                        }
                        continue;
                    }

                    filteredAuthorizationResources.put(authResource);
                }

                payload.put(DOMSEnforcementConstants.AUTH_RESOURCES_TAG, filteredAuthorizationResources);
            }

            JSONArray consentMappingResources =
                    payload.optJSONArray(DOMSEnforcementConstants.CONSENT_MAPPING_RESOURCES_TAG);
            if (consentMappingResources == null) {
                log.warn("No consentMappingResources array found in JWT, skipping DOMS enforcement.");
                return true;
            }

            Set<String> accountIds = new HashSet<>();
            for (int i = 0; i < consentMappingResources.length(); i++) {
                JSONObject mappingResource = consentMappingResources.getJSONObject(i);

                // exclude linked-member accounts in DOMS call
                if (linkedMemberAuthIds.contains(mappingResource.optString(DOMSEnforcementConstants.AUTH_ID_TAG))) {
                    continue;
                }
                accountIds.add(mappingResource.optString(DOMSEnforcementConstants.ACCELERATOR_ACCOUNT_ID_TAG));
            }

            Set<String> blockedAccounts = DOMSEnforcementUtils.fetchBlockedAccountsFromService(
                    accountIds, blockedAccountsApi, basicAuthBase64);

            JSONArray filteredConsentMappings = new JSONArray();
            for (int i = 0; i < consentMappingResources.length(); i++) {

                JSONObject mappingResource = consentMappingResources.getJSONObject(i);

                // Removing consentMappingResources of linked-members
                if (linkedMemberAuthIds.contains(mappingResource.optString(DOMSEnforcementConstants.AUTH_ID_TAG))) {
                    continue;
                }

                String accountId = mappingResource.optString(DOMSEnforcementConstants.ACCELERATOR_ACCOUNT_ID_TAG);
                if (!blockedAccounts.contains(accountId)) {
                    mappingResource.put(DOMSEnforcementConstants.CDS_ACCOUNT_ID_TAG, accountId);
                    mappingResource.remove(DOMSEnforcementConstants.ACCELERATOR_ACCOUNT_ID_TAG);
                    filteredConsentMappings.put(mappingResource);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Blocking DOMS account: " + accountId);
                    }
                }
            }

            payload.put(DOMSEnforcementConstants.CONSENT_MAPPING_RESOURCES_TAG, filteredConsentMappings);

            String signedJwt = generateJWT(payload.toString());
            headers.put(DOMSEnforcementConstants.INFO_HEADER_TAG, signedJwt);

            log.debug("DOMS enforcement completed successfully");

        } catch (ParseException | JOSEException e) {
            String errorDescription = "Error during DOMS enforcement mediation";
            log.error(errorDescription, e);
            setErrorResponseProperties(messageContext, "Internal Server Error",
                    errorDescription, "500");
        }

        return true;
    }
    protected JSONObject decodeJWT(String accountHeaderJwt) throws ParseException {
        return DOMSEnforcementUtils.decodeJWT(accountHeaderJwt);
    }

    @Generated(message = "No testable logic")
    private static void setErrorResponseProperties(MessageContext messageContext, String errorCode,
                                                   String errorDescription, String httpStatusCode) {

        messageContext.setProperty(DOMSEnforcementConstants.ERROR_CODE, errorCode);
        messageContext.setProperty(DOMSEnforcementConstants.ERROR_TITLE, "CDS DOMS Policy Error");
        messageContext.setProperty(DOMSEnforcementConstants.ERROR_DESCRIPTION, errorDescription);
        messageContext.setProperty(DOMSEnforcementConstants.CUSTOM_HTTP_SC, httpStatusCode);
    }

    protected String generateJWT(String payload) throws ParseException, JOSEException {
        return DOMSEnforcementUtils.generateJWT(payload);
    }
}

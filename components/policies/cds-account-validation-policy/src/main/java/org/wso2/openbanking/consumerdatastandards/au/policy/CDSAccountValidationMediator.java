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

package org.wso2.openbanking.consumerdatastandards.au.policy;

import com.nimbusds.jose.JOSEException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.openbanking.consumerdatastandards.au.policy.constants.CDSAccountValidationConstants;
import org.wso2.openbanking.consumerdatastandards.au.policy.utils.CDSAccountValidationUtils;
import org.wso2.openbanking.consumerdatastandards.au.policy.utils.Generated;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Mediator to remove DOMS-blocked accounts from the Account-Request-Information JWT header.
 */
public class CDSAccountValidationMediator extends AbstractMediator {

    private static final Log log = LogFactory.getLog(CDSAccountValidationMediator.class);

    private String webappBaseURL;
    private String basicAuthCredentials;

    public void setWebappBaseURL(String webappBaseURL) {
        this.webappBaseURL = webappBaseURL;
    }

    public void setBasicAuthCredentials(String basicAuthCredentials) {
        this.basicAuthCredentials = basicAuthCredentials;
    }

    /**
     * Enforces DOMS account validation for the account information header by removing linked-member
     * authorization resources, filtering blocked accounts, and updating the signed header payload.
     *
     * @param messageContext Synapse message context containing transport headers and mediation properties
     * @return {@code true} to continue the mediation flow
     */
    @Override
    public boolean mediate(MessageContext messageContext) {

        log.debug("Starting CDS mediation policy");

        try {
            org.apache.axis2.context.MessageContext axis2Ctx =
                    ((Axis2MessageContext) messageContext).getAxis2MessageContext();

            Map<String, String> headers = (Map<String, String>)
                    axis2Ctx.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

            String accountHeaderJwt = headers.get(CDSAccountValidationConstants.INFO_HEADER_TAG);

            // Unsigned payload
            JSONObject payload = new JSONObject(accountHeaderJwt);

            //collect linkedMember authorization Ids
            Set<String> linkedMemberAuthIds = new HashSet<>();

            JSONArray authorizationResources = payload.optJSONArray(CDSAccountValidationConstants.AUTH_RESOURCES_TAG);
            if (authorizationResources != null) {
                JSONArray filteredAuthorizationResources = new JSONArray();

                for (int i = 0; i < authorizationResources.length(); i++) {
                    JSONObject authResource = authorizationResources.getJSONObject(i);

                    // Removing Auth resources of linked members
                    if (CDSAccountValidationConstants.LINKED_MEMBER_TAG.equalsIgnoreCase(
                            authResource.optString(CDSAccountValidationConstants.AUTH_TYPE_TAG))) {
                        String linkedAuthId = authResource.optString(CDSAccountValidationConstants.AUTH_ID_TAG);
                        if (linkedAuthId != null && !linkedAuthId.isEmpty()) {
                            linkedMemberAuthIds.add(linkedAuthId);
                        }
                        if (log.isDebugEnabled()) {
                            log.debug("Removing linkedMember authorization resource. authorizationId= "
                                    + linkedAuthId);
                        }
                        continue;
                    }

                    filteredAuthorizationResources.put(authResource);
                }

                payload.put(CDSAccountValidationConstants.AUTH_RESOURCES_TAG, filteredAuthorizationResources);
            }

            JSONArray consentMappingResources =
                    payload.optJSONArray(CDSAccountValidationConstants.CONSENT_MAPPING_RESOURCES_TAG);
            if (consentMappingResources == null) {
                log.warn("No consentMappingResources array found in JWT, skipping CDS mediation policy.");
                return true;
            }

            Set<String> accountIds = new HashSet<>();
            for (int i = 0; i < consentMappingResources.length(); i++) {
                JSONObject mappingResource = consentMappingResources.getJSONObject(i);

                // exclude linked-member accounts in DOMS call
                if (linkedMemberAuthIds.contains(mappingResource.optString(
                        CDSAccountValidationConstants.AUTH_ID_TAG))) {
                    continue;
                }
                accountIds.add(mappingResource.optString(CDSAccountValidationConstants.ACCELERATOR_ACCOUNT_ID_TAG));
            }

            String userId = payload.optString(CDSAccountValidationConstants.USER_ID_TAG, null);
            Set<String> blockedAccounts = CDSAccountValidationUtils.fetchAllBlockedAccounts(accountIds,
                    this.webappBaseURL, userId, this.basicAuthCredentials);

            JSONArray filteredConsentMappings = new JSONArray();

            for (int i = 0; i < consentMappingResources.length(); i++) {
                JSONObject mappingResource = consentMappingResources.getJSONObject(i);

                // Removing consentMappingResources of linked-members
                if (linkedMemberAuthIds.contains(mappingResource.optString(
                        CDSAccountValidationConstants.AUTH_ID_TAG))) {
                    continue;
                }

                String accountId = mappingResource.optString(CDSAccountValidationConstants.ACCELERATOR_ACCOUNT_ID_TAG);
                if (!blockedAccounts.contains(accountId)) {
                    mappingResource.put(CDSAccountValidationConstants.CDS_ACCOUNT_ID_TAG, accountId);
                    mappingResource.remove(CDSAccountValidationConstants.ACCELERATOR_ACCOUNT_ID_TAG);
                    filteredConsentMappings.put(mappingResource);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("[CDS-policy] Blocking account: " + accountId);
                    }
                }
            }

            payload.put(CDSAccountValidationConstants.CONSENT_MAPPING_RESOURCES_TAG, filteredConsentMappings);

            String signedJwt = CDSAccountValidationUtils.generateJWT(payload.toString());
            headers.put(CDSAccountValidationConstants.INFO_HEADER_TAG, signedJwt);

            log.debug("CDS mediation completed successfully");

        } catch (ParseException | JOSEException e) {
            String errorDescription = "Error during CDS mediation policy";
            log.error(errorDescription, e);
            setErrorResponseProperties(messageContext, errorDescription);
        }

        return true;
    }

    /**
     * Sets standardized error response properties in the message context when CDS mediation fails.
     *
     * @param messageContext Synapse message context used to propagate error details
     * @param errorDescription description of the error encountered during mediation
     */
    @Generated(message = "No testable logic")
    private static void setErrorResponseProperties(MessageContext messageContext,
                                                   String errorDescription) {

        messageContext.setProperty(CDSAccountValidationConstants.ERROR_CODE, "Internal Server Error");
        messageContext.setProperty(CDSAccountValidationConstants.ERROR_TITLE, "CDS DOMS Policy Error");
        messageContext.setProperty(CDSAccountValidationConstants.ERROR_DESCRIPTION, errorDescription);
        messageContext.setProperty(CDSAccountValidationConstants.CUSTOM_HTTP_SC, "500");
    }

}

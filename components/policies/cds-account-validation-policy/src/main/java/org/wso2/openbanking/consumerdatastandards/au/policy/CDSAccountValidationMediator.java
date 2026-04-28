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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.openbanking.consumerdatastandards.au.policy.constants.CDSAccountValidationConstants;
import org.wso2.openbanking.consumerdatastandards.au.policy.exceptions.CDSAccountValidationException;
import org.wso2.openbanking.consumerdatastandards.au.policy.utils.CDSAccountValidationUtils;
import org.wso2.openbanking.consumerdatastandards.au.policy.utils.Generated;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Mediator to remove blocked accounts from the Account-Request-Information JWT header.
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
     * Enforces CDS account validation for the account information header by removing linked-member,
     * Secondary account owner authorization resources, filtering blocked accounts, and updating the signed payload.
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
            String userId = null;
            String clientId = getClientIdFromPayload(payload);

            // Collect authorization IDs that should be removed from validation and mapping.
            Set<String> excludedAuthIds = new HashSet<>();

            JSONArray authorizationResources = payload.optJSONArray(CDSAccountValidationConstants.AUTH_RESOURCES_TAG);
            if (authorizationResources != null) {
                JSONArray filteredAuthorizationResources = new JSONArray();

                for (int i = 0; i < authorizationResources.length(); i++) {
                    JSONObject authResource = authorizationResources.getJSONObject(i);
                    String authType = authResource.optString(CDSAccountValidationConstants.AUTH_TYPE_TAG);
                    String authId = authResource.optString(CDSAccountValidationConstants.AUTH_ID_TAG);

                    // Remove linked-member and secondary-owner auth resources.
                    if (isExcludedAuthType(authType)) {
                        if (!StringUtils.isEmpty(authId)) {
                            excludedAuthIds.add(authId);
                        }
                        continue;
                    }

                    // Extracting the primary userID
                    if (CDSAccountValidationConstants.PRIMARY_AUTH_TYPE_TAG.equalsIgnoreCase(authType)) {
                        userId = authResource.optString(CDSAccountValidationConstants.USER_ID_TAG);
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
                String authId = mappingResource.optString(CDSAccountValidationConstants.AUTH_ID_TAG);

                // Removing duplicates of linked-member,secondary-user,business accounts in account validation calls.
                if (excludedAuthIds.contains(authId)) {
                    continue;
                }
                accountIds.add(mappingResource.optString(CDSAccountValidationConstants.ACCELERATOR_ACCOUNT_ID_TAG));
            }

            Set<String> blockedAccounts = CDSAccountValidationUtils.fetchAllBlockedAccounts(accountIds,
                    this.webappBaseURL, userId, this.basicAuthCredentials, clientId);

            JSONArray filteredConsentMappings = new JSONArray();

            for (int i = 0; i < consentMappingResources.length(); i++) {
                JSONObject mappingResource = consentMappingResources.getJSONObject(i);
                String authId = mappingResource.optString(CDSAccountValidationConstants.AUTH_ID_TAG);

                // Removing consentMappingResources of other users.
                if (excludedAuthIds.contains(authId)) {
                    continue;
                }

                String accountId = mappingResource.optString(CDSAccountValidationConstants.ACCELERATOR_ACCOUNT_ID_TAG);
                if (!blockedAccounts.contains(accountId)) {
                    // Changing the Account id field from Accelerator format (account_id) to CDS format (accountId).
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

            // Single-account validation: check if the requested accountId is in the allowed list
            String electedResource = (String) messageContext.getProperty(
                    CDSAccountValidationConstants.API_ELECTED_RESOURCE);
            String fullRequestPath = (String) messageContext.getProperty(
                    CDSAccountValidationConstants.REST_FULL_REQUEST_PATH);

            if (electedResource != null && electedResource.contains(
                    CDSAccountValidationConstants.API_ELECTED_RESOURCE_ACCOUNT_ID_PARAMETER)
                    && fullRequestPath != null) {
                String requestedAccountId = null;
                String[] segments = fullRequestPath.split("/");
                for (int i = 0; i < segments.length - 1; i++) {
                    if (CDSAccountValidationConstants.RESOURCE_ACCOUNTS_TAG.equals(segments[i])) {
                        requestedAccountId = segments[i + 1];
                        break;
                    }
                }

                if (requestedAccountId != null) {
                    if (blockedAccounts.contains(requestedAccountId)) {
                        if (log.isDebugEnabled()) {
                            log.debug("[CDS-policy] Single-account request for blocked or unknown accountId: "
                                    + requestedAccountId);
                        }
                        setErrorResponseProperties(messageContext,
                                CDSAccountValidationConstants.RESOURCE_INVALID_BANKING_ACCOUNT,
                                CDSAccountValidationConstants.INVALID_BANKING_ACCOUNT_TITLE,
                                CDSAccountValidationConstants.INVALID_BANKING_ACCOUNT_DESC,
                                CDSAccountValidationConstants.HTTP_SC_404);
                        throw new SynapseException("Account " + requestedAccountId
                                + " is not available for data sharing");
                    }
                }
            }

            // POST multi-account validation: all requested accountIds must be in the allowed list
            String httpMethod = (String) messageContext.getProperty(CDSAccountValidationConstants.REST_METHOD);
            if (CDSAccountValidationConstants.POST_METHOD.equals(httpMethod)) {
                String requestBody = (String) messageContext.getProperty(
                        CDSAccountValidationConstants.ORIGINAL_REQUEST_JSON_BODY);
                if (requestBody != null) {
                    JSONObject requestBodyJson = new JSONObject(requestBody);
                    if (requestBodyJson.has(CDSAccountValidationConstants.DATA_TAG)) {
                        JSONObject dataObj = requestBodyJson.getJSONObject(
                                CDSAccountValidationConstants.DATA_TAG);
                        if (dataObj.has(CDSAccountValidationConstants.POST_PAYLOAD_ACCOUNT_IDS_TAG)) {
                            JSONArray requestedAccountIds = dataObj.getJSONArray(
                                    CDSAccountValidationConstants.POST_PAYLOAD_ACCOUNT_IDS_TAG);

                            // All-or-nothing: reject if any requested account is not allowed
                            for (int i = 0; i < requestedAccountIds.length(); i++) {
                                String requestedAccountId = requestedAccountIds.optString(i);
                                if (blockedAccounts.contains(requestedAccountId)) {
                                    if (log.isDebugEnabled()) {
                                        log.debug("[CDS-policy] POST request contains blocked or " +
                                                "unknown accountId: " + requestedAccountId);
                                    }
                                    setErrorResponseProperties(messageContext,
                                            CDSAccountValidationConstants.RESOURCE_INVALID_BANKING_ACCOUNT,
                                            CDSAccountValidationConstants.INVALID_BANKING_ACCOUNT_TITLE,
                                            CDSAccountValidationConstants.INVALID_BANKING_ACCOUNT_POST_DESC,
                                            CDSAccountValidationConstants.HTTP_SC_422);
                                    throw new SynapseException("Account " + requestedAccountId
                                            + " is not available for data sharing");
                                }
                            }
                        }
                    }
                }
            }

            String signedJwt = CDSAccountValidationUtils.generateJWT(payload.toString());
            headers.put(CDSAccountValidationConstants.INFO_HEADER_TAG, signedJwt);

            log.debug("CDS mediation completed successfully");

        } catch (ParseException | JOSEException | JSONException | CDSAccountValidationException e) {
            String errorDescription = "Error during CDS mediation policy";
            log.error(errorDescription, e);
            setErrorResponseProperties(messageContext, "Internal Server Error", "CDS DOMS Policy Error",
                    errorDescription, "500");
            throw new SynapseException(errorDescription, e);
        }

        return true;
    }

    /**
     * Sets error response properties in the message context.
     *
     * @param messageContext Synapse message context used to propagate error details
     * @param errorCode error code value
     * @param errorTitle error title
     * @param errorDescription error description
     * @param httpStatusCode HTTP status code as string
     */
    @Generated(message = "No testable logic")
    private static void setErrorResponseProperties(MessageContext messageContext,
                                                   String errorCode,
                                                   String errorTitle,
                                                   String errorDescription,
                                                   String httpStatusCode) {

        messageContext.setProperty(CDSAccountValidationConstants.ERROR_CODE, errorCode);
        messageContext.setProperty(CDSAccountValidationConstants.ERROR_TITLE, errorTitle);
        messageContext.setProperty(CDSAccountValidationConstants.ERROR_DESCRIPTION, errorDescription);
        messageContext.setProperty(CDSAccountValidationConstants.CUSTOM_HTTP_SC, httpStatusCode);
    }

    /**
     * Checks whether the given authorization type belongs to a secondary account owner.
     *
     * @param authType authorization type value
     * @return {@code true} if the auth type is secondary individual or secondary joint account owner
     */
    private static boolean isSecondaryAccountOwnerAuthType(String authType) {
        return CDSAccountValidationConstants.SECONDARY_INDIVIDUAL_ACCOUNT_OWNER_TAG.equalsIgnoreCase(authType)
                || CDSAccountValidationConstants.SECONDARY_JOINT_ACCOUNT_OWNER_TAG.equalsIgnoreCase(authType);
    }

    /**
     * Checks whether the given authorization type belongs to a business account stakeholder.
     *
     * @param authType authorization type value
     * @return {@code true} if the auth type is nominated representative or business account owner
     */
    private static boolean isBusinessAccountAuthType(String authType) {
        return CDSAccountValidationConstants.NOMINATED_REPRESENTATIVE_TAG.equalsIgnoreCase(authType)
                || CDSAccountValidationConstants.BUSINESS_ACCOUNT_OWNER_TAG.equalsIgnoreCase(authType);
    }

    /**
     * Checks whether the given authorization type should be excluded from account validation and mappings.
     *
     * @param authType authorization type value
     * @return {@code true} if auth type belongs to linked member or secondary account owner
     */
    private static boolean isExcludedAuthType(String authType) {
        return CDSAccountValidationConstants.LINKED_MEMBER_TAG.equalsIgnoreCase(authType)
                || isSecondaryAccountOwnerAuthType(authType) || isBusinessAccountAuthType(authType);
    }

    /**
     * Extract client id from payload using either clientId or client_id.
     *
     * @param payload account-request-information payload
     * @return client id value if present, otherwise empty string
     */
    private static String getClientIdFromPayload(JSONObject payload) {
            return payload.optString(CDSAccountValidationConstants.CLIENT_ID_TAG);
    }
}

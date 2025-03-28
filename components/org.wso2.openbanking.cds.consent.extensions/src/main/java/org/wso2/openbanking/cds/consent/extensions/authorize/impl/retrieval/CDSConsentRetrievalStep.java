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

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentRetrievalStep;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.cds.common.utils.CDSCommonUtils;
import org.wso2.openbanking.cds.common.utils.CommonConstants;
import org.wso2.openbanking.cds.consent.extensions.authorize.utils.CDSConsentCommonUtil;
import org.wso2.openbanking.cds.consent.extensions.authorize.utils.CDSDataRetrievalUtil;
import org.wso2.openbanking.cds.consent.extensions.common.CDSConsentExtensionConstants;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Consent retrieval step CDS implementation.
 */
public class CDSConsentRetrievalStep implements ConsentRetrievalStep {

    private static final Log log = LogFactory.getLog(CDSConsentRetrievalStep.class);
    private static final int secondsInYear = (int) TimeUnit.SECONDS.convert(365, TimeUnit.DAYS);
    private final ConsentCoreServiceImpl consentCoreService;

    public CDSConsentRetrievalStep() {
        this.consentCoreService = new ConsentCoreServiceImpl();
    }

    public CDSConsentRetrievalStep(ConsentCoreServiceImpl consentCoreServiceImpl) {
        this.consentCoreService = consentCoreServiceImpl;
    }

    @Override
    public void execute(ConsentData consentData, JSONObject jsonObject) throws ConsentException {

        if (consentData.isRegulatory()) {
            String requestUriKey = CDSCommonUtils.getRequestUriKeyFromQueryParams(consentData.getSpQueryParams());
            consentData.getMetaDataMap().put(CommonConstants.REQUEST_URI_KEY, requestUriKey);
            jsonObject.appendField(CommonConstants.REQUEST_URI_KEY, requestUriKey);

            String requestObject = CDSDataRetrievalUtil.extractRequestObject(consentData.getSpQueryParams());
            Map<String, Object> requiredData = extractRequiredDataFromRequestObject(requestObject);
            boolean isConsentAmendment = false;

            // check for consent amendment
            if (requiredData.containsKey(CDSConsentExtensionConstants.CDR_ARRANGEMENT_ID)) {
                isConsentAmendment = true;
                String consentId = requiredData.get(CDSConsentExtensionConstants.CDR_ARRANGEMENT_ID).toString();
                jsonObject.appendField(CDSConsentExtensionConstants.IS_CONSENT_AMENDMENT,
                        true);
                try {
                    DetailedConsentResource consentResource = consentCoreService.getDetailedConsent(consentId);

                    if (consentResource != null) {
                        // Check if the consent is expired
                        OffsetDateTime existingConsentExpiry;
                        String consentExpiryString = CDSDataRetrievalUtil.getExpiryFromReceipt(
                                consentResource.getReceipt());
                        if (CDSConsentExtensionConstants.ZERO.equals(consentExpiryString)) {
                            // Use 24h from created time for once off consents (default token validity period)
                            long onceOffConsentExpiry = consentResource.getCreatedTime() + 60 * 60 * 24; //24 hours
                            existingConsentExpiry =
                                    Instant.ofEpochSecond(onceOffConsentExpiry).atOffset(ZoneOffset.UTC);
                        } else {
                            existingConsentExpiry = OffsetDateTime.parse(consentExpiryString);
                        }
                        OffsetDateTime currentDateTime = OffsetDateTime.now(ZoneOffset.UTC);
                        if (existingConsentExpiry.isAfter(currentDateTime)) {
                            // Add required data for the persistence step
                            String userId = CDSConsentCommonUtil.getUserIdWithTenantDomain(consentData.getUserId());
                            ArrayList<AuthorizationResource> authResourceList = consentResource.
                                    getAuthorizationResources();

                            // Check if this is a business-profile consent
                            if (consentResource.getConsentAttributes().containsKey(CDSConsentExtensionConstants.
                                    CUSTOMER_PROFILE_TYPE) && CDSConsentExtensionConstants.
                                    BUSINESS_PROFILE_TYPE_ATTRIBUTE.equals(consentResource.getConsentAttributes().get(
                                            CDSConsentExtensionConstants.CUSTOMER_PROFILE_TYPE))) {
                                // For business accounts, only primary_member is allowed to amend the consent.
                                String primaryUserId = "";
                                for (AuthorizationResource authResource : authResourceList) {
                                    if (CDSConsentExtensionConstants.AUTH_RESOURCE_TYPE_PRIMARY.equals(
                                            authResource.getAuthorizationType())) {
                                        primaryUserId = authResource.getUserID();
                                    }
                                }
                                if (!userId.equals(primaryUserId)) {
                                    String errorMessage = String.format("User %s is not authorized to amend the " +
                                            "consent.", userId);
                                    log.error(errorMessage + " Consent id: " + consentId);
                                    throw new ConsentException(ResponseStatus.FORBIDDEN, errorMessage);
                                }
                            }

                            // Set userid with tenant domain to consent data
                            consentData.setUserId(userId);
                            String authId = null;
                            String authStatus = null;
                            ArrayList<AuthorizationResource> matchedAuthResourcesForUser = new ArrayList<>();
                            for (AuthorizationResource authResource : authResourceList) {
                                if (userId.equals(authResource.getUserID())) {
                                    matchedAuthResourcesForUser.add(authResource);
                                }
                            }
                            if (matchedAuthResourcesForUser.size() > 1) {
                                // multiple auth resources found for the user, checking auth type `primary_member`
                                for (AuthorizationResource authResource : matchedAuthResourcesForUser) {
                                    if (CDSConsentExtensionConstants.AUTH_RESOURCE_TYPE_PRIMARY.equals(
                                            authResource.getAuthorizationType())) {
                                        authId = authResource.getAuthorizationID();
                                        authStatus = authResource.getAuthorizationStatus();
                                        break;
                                    }
                                }
                                // if no primary_member auth resource found, taking the last one
                                // This is to preserve the existing behavior.
                                if (authId == null || authStatus == null) {
                                    authId = matchedAuthResourcesForUser.get(matchedAuthResourcesForUser.size() - 1).
                                            getAuthorizationID();
                                    authStatus = matchedAuthResourcesForUser.
                                            get(matchedAuthResourcesForUser.size() - 1).getAuthorizationStatus();
                                }
                            } else if (matchedAuthResourcesForUser.size() == 1) {
                                authId = matchedAuthResourcesForUser.get(0).getAuthorizationID();
                                authStatus = matchedAuthResourcesForUser.get(0).getAuthorizationStatus();
                            }

                            if (authId == null || authStatus == null) {
                                String errorMessage = String.format("There's no authorization resource " +
                                                "corresponds to consent id %s and user id %s.", consentId,
                                        consentData.getUserId());
                                log.error(errorMessage);
                                throw new ConsentException(ResponseStatus.BAD_REQUEST, errorMessage);
                            }
                            consentData.addData(CDSConsentExtensionConstants.CDR_ARRANGEMENT_ID,
                                    requiredData.get(CDSConsentExtensionConstants.CDR_ARRANGEMENT_ID));
                            consentData.addData(CDSConsentExtensionConstants.AUTH_RESOURCE_ID, authId);
                            consentData.addData(CDSConsentExtensionConstants.AUTH_RESOURCE_STATUS, authStatus);
                            consentData.addData(CDSConsentExtensionConstants.PRE_SELECTED_PROFILE_ID,
                                    consentResource.getConsentAttributes()
                                            .get(CDSConsentExtensionConstants.SELECTED_PROFILE_ID));

                            // Get pre-selected account list
                            JSONArray preSelectedAccounts = new JSONArray();
                            ArrayList<ConsentMappingResource> mappingResourceArrayList =
                                    consentResource.getConsentMappingResources();
                            for (ConsentMappingResource mappingResource : mappingResourceArrayList) {
                                // add accounts with active mapping
                                if ("active".equals(mappingResource.getMappingStatus())) {
                                    preSelectedAccounts.add(mappingResource.getAccountID());
                                }
                            }
                            jsonObject.appendField(CDSConsentExtensionConstants.PRE_SELECTED_ACCOUNT_LIST,
                                    preSelectedAccounts);
                            // Add existing permissions
                            JSONArray existingPermissions = CDSDataRetrievalUtil.
                                    getPermissionsFromReceipt(consentResource.getReceipt());
                            jsonObject.appendField(CDSConsentExtensionConstants.EXISTING_PERMISSIONS,
                                    existingPermissions);
                            // Check if the sharing duration is updated
                            long newSharingDuration = Long.parseLong(requiredData.get(
                                    CDSConsentExtensionConstants.SHARING_DURATION_VALUE).toString());
                            boolean isSharingDurationUpdated = !getConsentExpiryDateTime(newSharingDuration).
                                    isEqual(existingConsentExpiry);
                            jsonObject.appendField(CDSConsentExtensionConstants.IS_SHARING_DURATION_UPDATED,
                                    isSharingDurationUpdated);
                        } else {
                            String errorMessage = String.format("There's no active sharing arrangement corresponds " +
                                    "to consent id %s and user id %s.", consentId, consentData.getUserId());
                            if (log.isDebugEnabled()) {
                                log.debug(errorMessage);
                            }
                            throw new ConsentException(ResponseStatus.BAD_REQUEST, errorMessage);
                        }
                    } else {
                        String errorMessage = String.format("There's no active sharing arrangement corresponds " +
                                "to consent id %s and user id %s.", consentId, consentData.getUserId());
                        if (log.isDebugEnabled()) {
                            log.debug(errorMessage);
                        }
                        throw new ConsentException(ResponseStatus.BAD_REQUEST, errorMessage);
                    }
                } catch (ConsentManagementException e) {
                    log.error(String.format("Error occurred while searching for the existing consent. %s",
                            e.getMessage()));
                    throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                            "Error occurred while searching for the existing consent");
                }
            } else {
                jsonObject.appendField(CDSConsentExtensionConstants.IS_CONSENT_AMENDMENT,
                        false);
            }

            setClaimPermissions(consentData,
                    (JSONObject) requiredData.get(CDSConsentExtensionConstants.USERINFO_CLAIMS),
                    (JSONObject) requiredData.get(CDSConsentExtensionConstants.ID_TOKEN_CLAIMS));
            consentData.addData(CDSConsentExtensionConstants.USERINFO_CLAIMS,
                    requiredData.get(CDSConsentExtensionConstants.USERINFO_CLAIMS));
            consentData.addData(CDSConsentExtensionConstants.ID_TOKEN_CLAIMS,
                    requiredData.get(CDSConsentExtensionConstants.ID_TOKEN_CLAIMS));
            JSONArray permissions = new JSONArray();
            permissions.addAll(CDSDataRetrievalUtil.getPermissionList(consentData.getScopeString()));
            JSONArray consentDataJSON = new JSONArray();

            JSONObject jsonElementPermissions = new JSONObject();
            jsonElementPermissions.appendField(CDSConsentExtensionConstants.TITLE,
                    CDSConsentExtensionConstants.PERMISSION_TITLE);
            jsonElementPermissions.appendField(CDSConsentExtensionConstants.DATA, permissions);

            consentDataJSON.add(jsonElementPermissions);
            String expiry = requiredData.get(CDSConsentExtensionConstants.EXPIRATION_DATE_TIME).toString();
            JSONArray expiryArray = new JSONArray();
            expiryArray.add(expiry);

            JSONObject jsonElementExpiry = new JSONObject();
            jsonElementExpiry.appendField(CDSConsentExtensionConstants.TITLE,
                    CDSConsentExtensionConstants.EXPIRATION_DATE_TITLE);
            jsonElementExpiry.appendField(CDSConsentExtensionConstants.DATA, expiryArray);

            consentDataJSON.add(jsonElementExpiry);

            jsonObject.appendField(CDSConsentExtensionConstants.CONSENT_DATA, consentDataJSON);
            consentData.addData(CDSConsentExtensionConstants.PERMISSIONS,
                    CDSDataRetrievalUtil.getPermissionList(consentData.getScopeString()));
            consentData.addData(CDSConsentExtensionConstants.EXPIRATION_DATE_TIME,
                    requiredData.get(CDSConsentExtensionConstants.EXPIRATION_DATE_TIME));
            consentData.addData(CDSConsentExtensionConstants.SHARING_DURATION_VALUE,
                    requiredData.get(CDSConsentExtensionConstants.SHARING_DURATION_VALUE));
            consentData.addData(CDSConsentExtensionConstants.IS_CONSENT_AMENDMENT,
                    isConsentAmendment);

            // consent type is hard coded since CDS only support accounts type for the moment
            // scopes will be used to determine consent type if any other types required in future
            consentData.setType(CDSConsentExtensionConstants.CDR_ACCOUNTS);

            // appending redirect URL
            jsonObject.appendField(CDSConsentExtensionConstants.REDIRECT_URL, CDSDataRetrievalUtil
                    .getRedirectURL(consentData.getSpQueryParams()));

            // appending state to be retrieved in authentication webapp
            jsonObject.appendField(CDSConsentExtensionConstants.STATE, CDSDataRetrievalUtil
                    .getStateParameter(consentData.getSpQueryParams()));

            // appending openid_scopes to be retrieved in authentication webapp
            jsonObject.appendField(CDSConsentExtensionConstants.OPENID_SCOPES, permissions);

            // append consent expiry date
            jsonObject.appendField(CDSConsentExtensionConstants.CONSENT_EXPIRY, expiry);

            // append consent sharing duration
            jsonObject.appendField(CDSConsentExtensionConstants.SHARING_DURATION_VALUE,
                    requiredData.get(CDSConsentExtensionConstants.SHARING_DURATION_VALUE));

            //check the scopes has "bank:" scopes to skip account selection
            String scopesString = consentData.getScopeString();
            if (scopesString.contains(CDSConsentExtensionConstants.COMMON_ACCOUNTS_BASIC_READ_SCOPE) ||
                    scopesString.contains(CDSConsentExtensionConstants.COMMON_ACCOUNTS_DETAIL_READ_SCOPE) ||
                    scopesString.contains(CDSConsentExtensionConstants.TRANSACTIONS_READ_SCOPE) ||
                    scopesString.contains(CDSConsentExtensionConstants.REGULAR_PAYMENTS_READ_SCOPE)) {
                jsonObject.appendField(CDSConsentExtensionConstants.CUSTOMER_SCOPES_ONLY, false);
            } else {
                jsonObject.appendField(CDSConsentExtensionConstants.CUSTOMER_SCOPES_ONLY, true);
            }

            // append client_id and service provider full name
            if (StringUtils.isNotBlank(consentData.getClientId())) {
                try {
                    jsonObject.appendField(CDSConsentExtensionConstants.CLIENT_ID, consentData.getClientId());
                    jsonObject.appendField(CDSConsentExtensionConstants.SP_FULL_NAME,
                            CDSDataRetrievalUtil.getServiceProviderFullName(consentData.getClientId()));
                } catch (OpenBankingException e) {
                    log.error(String.format("Error occurred while building service provider full name. %s",
                            e.getMessage()));
                    throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                            "Error occurred while building service provider full name");
                }
            } else {
                log.error("Error occurred while building service provider full name. Client-id is not found in " +
                        "consent data.");
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                        "Error occurred while building service provider full name. Client-id not found.");
            }
        }
    }

    /**
     * Method to extract required data from request object.
     *
     * @param requestObject
     * @return
     */
    private Map<String, Object> extractRequiredDataFromRequestObject(String requestObject) throws ConsentException {

        String clientID;
        Map<String, Object> dataMap = new HashMap<>();
        try {

            // request object validation is carried out in request object validator
            String[] jwtTokenValues = requestObject.split("\\.");
            String requestObjectPayload = new String(Base64.getUrlDecoder().decode(jwtTokenValues[1]),
                    StandardCharsets.UTF_8);

            Object payload = new JSONParser(JSONParser.MODE_PERMISSIVE).parse(requestObjectPayload);
            if (!(payload instanceof JSONObject)) {
                throw new ConsentException(ResponseStatus.BAD_REQUEST, "Payload is not a JSON object");
            }
            JSONObject jsonObject = (JSONObject) payload;

            long sharingDuration = 0;
            clientID = jsonObject.getAsString(CDSConsentExtensionConstants.CLIENT_ID);

            if (StringUtils.isBlank(clientID)) {
                log.error("client_id not found in request object");
                dataMap.put(CDSConsentExtensionConstants.IS_ERROR, "client_id not found in request object");
                return dataMap;
            }
            dataMap.put(CDSConsentExtensionConstants.CLIENT_ID, clientID);

            if (jsonObject.containsKey(CDSConsentExtensionConstants.CLAIMS)) {
                JSONObject claims = (JSONObject) jsonObject.get(CDSConsentExtensionConstants.CLAIMS);
                if (claims.containsKey(CDSConsentExtensionConstants.SHARING_DURATION)) {

                    String sharingDurationStr = claims.get(CDSConsentExtensionConstants.SHARING_DURATION) == null ?
                            StringUtils.EMPTY : claims.get(CDSConsentExtensionConstants.SHARING_DURATION).toString();

                    sharingDuration = sharingDurationStr.isEmpty() ? 0 : Long.parseLong(sharingDurationStr);

                    if (sharingDuration > secondsInYear) {
                        sharingDuration = secondsInYear;
                        if (log.isDebugEnabled()) {
                            log.debug("Requested sharing_duration is greater than a year,therefore one year duration"
                                    + " is set as consent expiration for request object of client: "
                                    + dataMap.get(CDSConsentExtensionConstants.CLIENT_ID));
                        }
                    }
                    dataMap.put(CDSConsentExtensionConstants.EXPIRATION_DATE_TIME,
                            getConsentExpiryDateTime(sharingDuration));
                }
                if (sharingDuration == 0) {
                    if (log.isDebugEnabled()) {
                        log.debug("sharing_duration not found in the request object of client: " + clientID);
                    }
                    dataMap.put(CDSConsentExtensionConstants.EXPIRATION_DATE_TIME, 0);
                }
                // adding original sharing_duration_value to data map
                dataMap.put(CDSConsentExtensionConstants.SHARING_DURATION_VALUE, sharingDuration);
                if (claims.containsKey(CDSConsentExtensionConstants.CDR_ARRANGEMENT_ID)
                        && claims.get(CDSConsentExtensionConstants.CDR_ARRANGEMENT_ID) != null) {
                    dataMap.put(CDSConsentExtensionConstants.CDR_ARRANGEMENT_ID,
                            claims.get(CDSConsentExtensionConstants.CDR_ARRANGEMENT_ID).toString());
                }
                String idTokenJsonString = claims.containsKey(CDSConsentExtensionConstants.ID_TOKEN) ?
                        claims.get(CDSConsentExtensionConstants.ID_TOKEN).toString() : null;
                String userInfoJsonString = claims.containsKey(CDSConsentExtensionConstants.USERINFO) ?
                        claims.get(CDSConsentExtensionConstants.USERINFO).toString() : null;
                JSONParser parser = new JSONParser();
                dataMap.put(CDSConsentExtensionConstants.ID_TOKEN_CLAIMS, StringUtils.isNotBlank(idTokenJsonString) ?
                        parser.parse(idTokenJsonString) : new JSONObject());
                dataMap.put(CDSConsentExtensionConstants.USERINFO_CLAIMS, StringUtils.isNotBlank(userInfoJsonString) ?
                        parser.parse(userInfoJsonString) : new JSONObject());
            }
        } catch (ParseException e) {
            log.error("Error while parsing the request object", e);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Error while parsing the request object ");
        }
        return dataMap;
    }

    private OffsetDateTime getConsentExpiryDateTime(long sharingDuration) {

        OffsetDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC);
        return currentTime.plusSeconds(sharingDuration);
    }

    /**
     * Set profile scope related individual claims as permissions.
     *
     * @param consentData    consent data
     * @param userInfoClaims user info claims
     * @param idTokenClaims  id token claims
     */
    private void setClaimPermissions(ConsentData consentData, JSONObject userInfoClaims, JSONObject idTokenClaims) {

        StringBuilder scopeString = new StringBuilder(consentData.getScopeString());

        List[] clusters = {CDSConsentExtensionConstants.NAME_CLUSTER_CLAIMS,
                CDSConsentExtensionConstants.PHONE_CLUSTER_CLAIMS,
                CDSConsentExtensionConstants.EMAIL_CLUSTER_CLAIMS,
                CDSConsentExtensionConstants.MAIL_CLUSTER_CLAIMS};
        for (List<String> cluster : clusters) {
            for (String claim : cluster) {
                if (userInfoClaims.containsKey(claim) || idTokenClaims.containsKey(claim)) {
                    scopeString.append(" ");
                    scopeString.append(claim);
                }
            }
        }
        consentData.setScopeString(scopeString.toString());
    }
}

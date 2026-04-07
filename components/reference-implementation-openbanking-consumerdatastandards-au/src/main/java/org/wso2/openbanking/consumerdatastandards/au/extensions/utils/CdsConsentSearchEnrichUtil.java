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

package org.wso2.openbanking.consumerdatastandards.au.extensions.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.CommonConstants;
import org.wso2.openbanking.consumerdatastandards.au.extensions.exceptions.CdsConsentException;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.SuccessResponseForConsentSearchData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility to enrich consent search response by adding "domsStatus" for joint accounts.
 */
public class CdsConsentSearchEnrichUtil {

    private static final Log log = LogFactory.getLog(CdsConsentSearchEnrichUtil.class);

    /**
     * Enrich consent search response by applying all supported enrichment operations.
     *
     * @param enrichedObj search result object to enrich
     * @param enrichmentParams optional enrichment params (query params from caller)
     * @return enriched search data wrapped in SuccessResponseForConsentSearchData
     */
    public static SuccessResponseForConsentSearchData enrichSearchResult(Object enrichedObj, Object enrichmentParams)
            throws CdsConsentException {

        SuccessResponseForConsentSearchData searchData = enrichDOMSStatus(enrichedObj);

        if (isSecondaryInfoEnrichmentRequested(enrichmentParams)) {
            return enrichSecondaryAccountInfo(searchData.getEnrichedSearchResult());
        }

        return searchData;
    }

    /**
     * Enrich consent search response by adding "domsStatus" for each account in consentMappingResources.
     *
     * @param enrichedObj the searchData object containing enrichedSearchResult
     * @return enriched searchData wrapped in SuccessResponseForConsentSearchData
     */
    public static SuccessResponseForConsentSearchData enrichDOMSStatus(Object enrichedObj) {

        SuccessResponseForConsentSearchData searchData = new SuccessResponseForConsentSearchData();
        searchData.setEnrichedSearchResult(enrichedObj);

        if (!(enrichedObj instanceof List)) {
            log.warn("enrichedSearchResult is not a List, skipping DOMS enrichment");
            return searchData;
        }

        List<?> searchResultArray = (List<?>) enrichedObj;

        // Collect all joint account IDs from all consents
        Set<String> allJointAccountIds = new HashSet<>();

        for (Object consentObj : searchResultArray) {
            JSONObject consent = toJSONObject(consentObj);
            if (consent == null) {
                continue;
            }
            allJointAccountIds.addAll(extractJointAccountIds(consent));
        }

        // Fetch DOMS statuses for all joint accounts in a single batch call
        Map<String, String> domsStatusMap = new HashMap<>();
        if (!allJointAccountIds.isEmpty()) {
            Map<String, String> fetchedStatusMap =
                    AccountMetadataUtil.getDOMSStatusesForAccounts(new ArrayList<>(allJointAccountIds));
            if (fetchedStatusMap != null) {
                domsStatusMap.putAll(fetchedStatusMap);
            }
        }

        // Enrich the consent mappings with DOMS status
        List<Object> enrichedSearchResults = new ArrayList<>();
        for (Object consentObj : searchResultArray) {
            JSONObject consent = toJSONObject(consentObj);
            if (consent == null) {
                enrichedSearchResults.add(consentObj);
                continue;
            }

            JSONArray mappingList = consent.optJSONArray(CommonConstants.CONSENT_MAPPING_RESOURCES);
            if (mappingList != null) {
                for (int j = 0; j < mappingList.length(); j++) {
                    JSONObject mappingItem = mappingList.optJSONObject(j);
                    if (mappingItem == null) {
                        continue;
                    }

                    String accountId = mappingItem.optString(CommonConstants.ACCOUNT_ID, null);

                    // Only add DOMS status if mapping belongs to a joint account
                    if (StringUtils.isNotBlank(accountId) && allJointAccountIds.contains(accountId)) {

                        mappingItem.put(CommonConstants.DOMS_STATUS_SEARCH_ENRICH_PROPERTY_NAME,
                                domsStatusMap.get(accountId));

                    }
                }
            }

            enrichedSearchResults.add(consent.toMap());
        }

        searchData.setEnrichedSearchResult(enrichedSearchResults);
        return searchData;
    }

    /**
     * Enrich consent search response by adding secondary account information for consents.
     *
     * @param enrichedObj search result object to enrich
     * @return enriched searchData wrapped in SuccessResponseForConsentSearchData
     */
    public static SuccessResponseForConsentSearchData enrichSecondaryAccountInfo(Object enrichedObj)
            throws CdsConsentException {

        SuccessResponseForConsentSearchData searchData = new SuccessResponseForConsentSearchData();
        searchData.setEnrichedSearchResult(enrichedObj);

        if (!(enrichedObj instanceof List)) {
            log.warn("enrichedSearchResult is not a List, skipping secondary account info enrichment");
            return searchData;
        }

        List<?> searchResultArray = (List<?>) enrichedObj;

        List<Object> enrichedSearchResults = new ArrayList<>();
        for (Object consentObj : searchResultArray) {
            JSONObject consent = toJSONObject(consentObj);
            if (consent == null) {
                enrichedSearchResults.add(consentObj);
                continue;
            }

            enrichConsentWithSecondaryInfo(consent);
            enrichedSearchResults.add(consent.toMap());
        }

        searchData.setEnrichedSearchResult(enrichedSearchResults);
        return searchData;
    }

    /**
     * Enrich a single consent with secondary account info if secondary account owner auth resources are available.
     */
    private static void enrichConsentWithSecondaryInfo(JSONObject consent) throws CdsConsentException {

        String primaryUserId = extractPrimaryUserId(consent);
        if (StringUtils.isBlank(primaryUserId)) {
            return;
        }

        Map<String, Set<String>> secondaryOwnerAccountIdsMap = extractSecondaryOwnerAccountIds(consent);
        if (secondaryOwnerAccountIdsMap.isEmpty()) {
            return;
        }

        Set<String> allSecondaryAccountIds = new LinkedHashSet<>();
        for (Set<String> accountIds : secondaryOwnerAccountIdsMap.values()) {
            allSecondaryAccountIds.addAll(accountIds);
        }

        Map<String, String> accountInstructionStatusMap =
                AccountMetadataUtil.getSecondaryAccountInstructionStatusesForAccounts(
                        new ArrayList<>(allSecondaryAccountIds), primaryUserId);

        JSONObject secondaryAccountInfo = new JSONObject();
        secondaryAccountInfo.put(CommonConstants.SECONDARY_ACCOUNT_INFO_ACCOUNT_USER, primaryUserId);

        JSONArray secondaryAccounts = new JSONArray();
        for (Map.Entry<String, Set<String>> entry : secondaryOwnerAccountIdsMap.entrySet()) {
            String accountOwnerUserId = entry.getKey();
            Set<String> accountIds = entry.getValue();

            JSONArray activeAccountIds = new JSONArray();
            JSONArray inactiveAccountIds = new JSONArray();

            for (String accountId : accountIds) {
                String instructionStatus = accountInstructionStatusMap.get(accountId);
                if (CommonConstants.SECONDARY_INSTRUCTION_STATUS_ACTIVE.equalsIgnoreCase(instructionStatus)) {
                    activeAccountIds.put(accountId);
                } else {
                    inactiveAccountIds.put(accountId);
                }
            }

            JSONObject secondaryAccountData = new JSONObject();
            secondaryAccountData.put(CommonConstants.SECONDARY_ACCOUNT_OWNER_TAG, accountOwnerUserId);
            secondaryAccountData.put(CommonConstants.SECONDARY_ACCOUNT_INFO_ACTIVE_ACCOUNTS, activeAccountIds);
            secondaryAccountData.put(CommonConstants.SECONDARY_ACCOUNT_INFO_INACTIVE_ACCOUNTS, inactiveAccountIds);
            secondaryAccounts.put(secondaryAccountData);
        }

        secondaryAccountInfo.put(CommonConstants.SECONDARY_ACCOUNT_INFO_SECONDARY_ACCOUNTS, secondaryAccounts);
        consent.put(CommonConstants.SECONDARY_ACCOUNT_INFO_TAG, secondaryAccountInfo);
    }

    /**
     * Extract primary user ID from consent authorization resources.
     */
    private static String extractPrimaryUserId(JSONObject consent) {

        JSONArray authResources = consent.optJSONArray(CommonConstants.AUTHORIZATION_RESOURCES);
        if (authResources == null) {
            return null;
        }

        for (int i = 0; i < authResources.length(); i++) {
            JSONObject authResource = authResources.optJSONObject(i);
            if (authResource == null) {
                continue;
            }

            String authType = authResource.optString(CommonConstants.AUTH_TYPE, null);
            if (CommonConstants.AUTH_RESOURCE_TYPE_PRIMARY.equalsIgnoreCase(authType)) {
                String userId = authResource.optString(CommonConstants.USER_ID, null);
                if (StringUtils.isNotBlank(userId)) {
                    return userId;
                }
            }
        }

        return null;
    }

    /**
     * Extract secondary owner userId to accountId mappings from consent resources.
     */
    private static Map<String, Set<String>> extractSecondaryOwnerAccountIds(JSONObject consent) {

        Map<String, Set<String>> accountOwnerAccountIdsMap = new LinkedHashMap<>();
        Map<String, String> secondaryOwnerAuthIdMap = new HashMap<>();

        JSONArray authResources = consent.optJSONArray(CommonConstants.AUTHORIZATION_RESOURCES);
        if (authResources == null) {
            return accountOwnerAccountIdsMap;
        }

        for (int i = 0; i < authResources.length(); i++) {
            JSONObject authResource = authResources.optJSONObject(i);
            if (authResource == null) {
                continue;
            }

            String authType = authResource.optString(CommonConstants.AUTH_TYPE, null);
            if (!isSecondaryAccountOwner(authType)) {
                continue;
            }

            String authorizationId = authResource.optString(CommonConstants.AUTHORIZATION_ID, null);
            String userId = authResource.optString(CommonConstants.USER_ID, null);
            if (StringUtils.isNotBlank(authorizationId) && StringUtils.isNotBlank(userId)) {
                secondaryOwnerAuthIdMap.put(authorizationId, userId);
            }
        }

        if (secondaryOwnerAuthIdMap.isEmpty()) {
            return accountOwnerAccountIdsMap;
        }

        JSONArray mappingResources = consent.optJSONArray(CommonConstants.CONSENT_MAPPING_RESOURCES);
        if (mappingResources == null) {
            return accountOwnerAccountIdsMap;
        }

        for (int i = 0; i < mappingResources.length(); i++) {
            JSONObject mapping = mappingResources.optJSONObject(i);
            if (mapping == null) {
                continue;
            }

            String authorizationId = mapping.optString(CommonConstants.AUTHORIZATION_ID, null);
            String accountId = mapping.optString(CommonConstants.ACCOUNT_ID, null);

            if (StringUtils.isBlank(authorizationId) || StringUtils.isBlank(accountId)) {
                continue;
            }

            String secondaryOwnerUserId = secondaryOwnerAuthIdMap.get(authorizationId);
            if (StringUtils.isBlank(secondaryOwnerUserId)) {
                continue;
            }

            accountOwnerAccountIdsMap
                    .computeIfAbsent(secondaryOwnerUserId, key -> new LinkedHashSet<>())
                    .add(accountId);
        }

        return accountOwnerAccountIdsMap;
    }

    /**
     * Check whether the given authorization type belongs to a secondary account owner.
     */
    private static boolean isSecondaryAccountOwner(String authType) {
        return CommonConstants.AUTH_TYPE_SECONDARY_INDIVIDUAL_ACCOUNT_OWNER.equalsIgnoreCase(authType)
            || CommonConstants.AUTH_TYPE_SECONDARY_JOINT_ACCOUNT_OWNER.equalsIgnoreCase(authType);
    }

    /**
     * Check whether the secondary account info enrichment is requested by query params.
     */
    private static boolean isSecondaryInfoEnrichmentRequested(Object enrichmentParams) {

        if (enrichmentParams == null) {
            return false;
        }
        Map<?, ?> paramsMap = (Map<?, ?>) enrichmentParams;
        return paramsMap.containsKey(CommonConstants.SECONDARY_ACCOUNT_INFO_TAG);

    }

    /**
     * Extract Joint account IDs corresponding to consents.
     */
    private static List<String> extractJointAccountIds(JSONObject consent) {

        List<String> jointAccountIds = new ArrayList<>();
        Set<String> linkedMemberAuthIds = new HashSet<>();

        // Collect linkedMember authorizationIds
        JSONArray authResources = consent.optJSONArray(CommonConstants.AUTHORIZATION_RESOURCES);
        if (authResources != null) {

            // Adding linkedMember auth IDs.
            for (int i = 0; i < authResources.length(); i++) {
                JSONObject authResource = authResources.optJSONObject(i);
                if (authResource == null) {
                    continue;
                }
                String authType = authResource.optString(CommonConstants.AUTH_TYPE, null);
                if (isJointAccount(authType)) {
                    String authorizationId = authResource.optString(CommonConstants.AUTHORIZATION_ID, null);
                    if (StringUtils.isNotBlank(authorizationId)) {
                        linkedMemberAuthIds.add(authorizationId);
                    }
                }
            }
        }

        // if no linkedMemberAuth Ids are there sending an empty list.
        if (linkedMemberAuthIds.isEmpty()) {
            return jointAccountIds;
        }

        // Mapping linkedMember authIds to accountIds
        JSONArray mappingResources = consent.optJSONArray(CommonConstants.CONSENT_MAPPING_RESOURCES);
        if (mappingResources == null) {
            return jointAccountIds;
        }

        for (int i = 0; i < mappingResources.length(); i++) {
            JSONObject mapping = mappingResources.optJSONObject(i);
            if (mapping == null) {
                continue;
            }
            String accountId = mapping.optString(CommonConstants.ACCOUNT_ID, null);
            String authorizationId = mapping.optString(CommonConstants.AUTHORIZATION_ID, null);

            if (StringUtils.isNotBlank(accountId) && !jointAccountIds.contains(accountId) &&
                    StringUtils.isNotBlank(authorizationId) && linkedMemberAuthIds.contains(authorizationId)) {
                jointAccountIds.add(accountId);
            }
        }

        return jointAccountIds;
    }

    /**
     * Check if authorization type is linkedMember.
     */
    private static boolean isJointAccount(String authType) {
        return CommonConstants.AUTH_RESOURCE_TYPE_LINKED.equalsIgnoreCase(authType);
    }

    /**
     * Convert a consent object into a {@link JSONObject}.
     *
     * @param consentObj consent object as either JSONObject or Map
     * @return converted JSONObject, or null when the input type is unsupported
     */
    private static JSONObject toJSONObject(Object consentObj) {
        if (consentObj instanceof JSONObject) {
            return (JSONObject) consentObj;
        }
        if (consentObj instanceof Map) {
            return new JSONObject((Map<?, ?>) consentObj);
        }
        return null;
    }
}

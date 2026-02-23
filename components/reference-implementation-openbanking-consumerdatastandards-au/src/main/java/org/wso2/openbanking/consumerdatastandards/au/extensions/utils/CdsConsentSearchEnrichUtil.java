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

import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.CommonConstants;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.SuccessResponseForConsentSearchData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * Utility to enrich consent search response by adding "domsStatus" for joint accounts.
 */
public class CdsConsentSearchEnrichUtil {

    private static final Log log = LogFactory.getLog(CdsConsentSearchEnrichUtil.class);

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

        JSONArray searchResultArray;
        try {
            searchResultArray = new JSONArray(enrichedObj);
        } catch (Exception e) {
            log.warn("Failed to convert enrichedSearchResult to JSONArray, skipping DOMS enrichment", e);
            return searchData;
        }

        // Collect all joint account IDs from all consents
        Set<String> allJointAccountIds = new HashSet<>();

        for (int i = 0; i < searchResultArray.length(); i++) {
            JSONObject consent = searchResultArray.optJSONObject(i);
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
        for (int i = 0; i < searchResultArray.length(); i++) {
            JSONObject consent = searchResultArray.optJSONObject(i);
            if (consent == null) {
                continue;
            }

            JSONArray mappingList = consent.optJSONArray(CommonConstants.CONSENT_MAPPING_RESOURCES);
            if (mappingList == null) {
                continue;
            }

            for (int j = 0; j < mappingList.length(); j++) {
                JSONObject mappingItem = mappingList.optJSONObject(j);
                if (mappingItem == null) {
                    continue;
                }

                String accountId = mappingItem.optString(CommonConstants.ACCOUNT_ID, null);

                // Only add DOMS status if mapping belongs to a joint account
                if (allJointAccountIds.contains(accountId)) {

                    mappingItem.put(CommonConstants.DOMS_STATUS_SEARCH_ENRICH_PROPERTY_NAME,
                            domsStatusMap.get(accountId));

                }
            }
        }

        searchData.setEnrichedSearchResult(searchResultArray.toList());

        return searchData;
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
}

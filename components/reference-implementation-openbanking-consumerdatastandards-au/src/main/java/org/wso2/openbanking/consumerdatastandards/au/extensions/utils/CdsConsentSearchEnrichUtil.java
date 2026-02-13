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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.CommonConstants;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.SuccessResponseForConsentSearchData;

import java.util.List;
import java.util.Map;

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
    @SuppressWarnings("unchecked")
    public static SuccessResponseForConsentSearchData enrichDOMSStatus(Object enrichedObj) {

        SuccessResponseForConsentSearchData searchData =
                new SuccessResponseForConsentSearchData();

        searchData.setEnrichedSearchResult(enrichedObj);

        if (!(enrichedObj instanceof List)) {
            log.warn("enrichedSearchResult is not a List, skipping DOMS enrichment");
            return searchData;
        }

        List<Map<String, Object>> searchResultList =
                (List<Map<String, Object>>) enrichedObj;

        // Collect all joint account IDs from all consents
        List<String> allJointAccountIds = new java.util.ArrayList<>();

        for (Map<String, Object> consent : searchResultList) {
            List<String> jointAccountIDs = extractJointAccountIds(consent);

            for (String accountId : jointAccountIDs) {
                if (!allJointAccountIds.contains(accountId)) {
                    allJointAccountIds.add(accountId);
                }
            }
        }

        // Fetch DOMS statuses for all joint accounts in a single batch call
        Map<String, String> domsStatusMap = new java.util.HashMap<>();
        if (!allJointAccountIds.isEmpty()) {
            Map<String, String> fetchedStatusMap = AccountMetadataUtil.getDOMSStatusesForAccounts(allJointAccountIds);
            if (fetchedStatusMap != null) {
                domsStatusMap.putAll(fetchedStatusMap);
            }
        }

        // Enrich the consent mappings with DOMS status
        for (Map<String, Object> consent : searchResultList) {
            Object consentMappingsObj = consent.get(CommonConstants.CONSENT_MAPPING_RESOURCES);
            if (!(consentMappingsObj instanceof List)) {
                continue;
            }

            List<Map<String, Object>> mappingList =
                    (List<Map<String, Object>>) consentMappingsObj;

            for (Map<String, Object> mappingItem : mappingList) {

                String accountId = (String) mappingItem.get(CommonConstants.ACCOUNT_ID);

                // Only add DOMS status if mapping belongs to a joint account
                if (allJointAccountIds.contains(accountId)) {
                    String domsStatus = domsStatusMap.get(accountId);

                    if (domsStatus == null) {
                        domsStatus = CommonConstants.DOMS_STATUS_PRE_APPROVAL;
                    }
                    mappingItem.put(CommonConstants.DOMS_STATUS_SEARCH_ENRICH_PROPERTY_NAME, domsStatus);

                    if (log.isDebugEnabled()) {
                        log.debug("Added domsStatus=" + domsStatus + " for joint accountId=" + accountId);
                    }

                }
            }
        }

        return searchData;
    }

    /**
     * Extract Joint account IDs corresponding to consents.
     */
    @SuppressWarnings("unchecked")
    private static List<String> extractJointAccountIds(Map<String, Object> consent) {

        List<String> jointAccountIds = new java.util.ArrayList<>();
        java.util.HashSet<Object> linkedMemberAuthIds = new java.util.HashSet<>();

        // Collect linkedMember authorizationIds
        Object authResourcesObj = consent.get(CommonConstants.AUTHORIZATION_RESOURCES);
        if (authResourcesObj instanceof List) {

            List<Map<String, Object>> authResourcesList =
                    (List<Map<String, Object>>) authResourcesObj;

            for (Map<String, Object> authResource : authResourcesList) {

                String authType = (String) authResource.get(CommonConstants.AUTH_TYPE);

                if (isJointAccount(authType)) {
                    String authId =
                            (String) authResource.get(CommonConstants.AUTHORIZATION_ID);

                    if (authId != null) {
                        linkedMemberAuthIds.add(authId);
                    }
                }
            }
        }

        if (linkedMemberAuthIds.isEmpty()) {
            return jointAccountIds;
        }

        // Mapping linkedMember authIds to accountIds
        Object mappingObj = consent.get(CommonConstants.CONSENT_MAPPING_RESOURCES);
        if (!(mappingObj instanceof List)) {
            return jointAccountIds;
        }

        List<Map<String, Object>> mappingList =
                (List<Map<String, Object>>) mappingObj;

        for (Map<String, Object> mapping : mappingList) {

            String accountId =
                    (String) mapping.get(CommonConstants.ACCOUNT_ID);
            String authorizationId =
                    (String) mapping.get(CommonConstants.AUTHORIZATION_ID);

            if (accountId != null && !jointAccountIds.contains(accountId) &&
                    authorizationId != null &&
                    linkedMemberAuthIds.contains(authorizationId)) {

                jointAccountIds.add(accountId);
            }
        }

        return jointAccountIds;
    }

    /**
     * Check if authorization type is linkedMember.
     */
    private static boolean isJointAccount(String authType) {
        return authType.equalsIgnoreCase(CommonConstants.AUTH_RESOURCE_TYPE_LINKED);
    }
}

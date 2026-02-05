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
import org.wso2.openbanking.consumerdatastandards.au.extensions.account.metadata.service.service.AccountMetadataServiceImpl;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.CommonConstants;
import org.wso2.openbanking.consumerdatastandards.au.extensions.exceptions.AccountMetadataException;
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

        for (Map<String, Object> consent : searchResultList) {

            // Extract joint account authorization IDs
            List<String> jointAccountAuthIDs = extractJointAccountAuthIDs(consent);

            Object consentMappingsObj = consent.get(CommonConstants.CONSENT_MAPPING_RESOURCES);
            if (!(consentMappingsObj instanceof List)) {
                continue;
            }

            List<Map<String, Object>> mappingList =
                    (List<Map<String, Object>>) consentMappingsObj;

            for (Map<String, Object> mappingItem : mappingList) {

                String accountId = (String) mappingItem.get(CommonConstants.ACCOUNT_ID);
                String authorizationId = (String) mappingItem.get(CommonConstants.AUTHORIZATION_ID);

                if (accountId == null || authorizationId == null) {
                    continue;
                }

                // Only add DOMS status if mapping belongs to a joint account
                if (jointAccountAuthIDs.contains(authorizationId)) {
                    String domsStatus = getDOMSStatus(accountId);

                    if (domsStatus == null) {
                        domsStatus = CommonConstants.DOMS_STATUS_PRE_APPROVAL;
                    }
                    mappingItem.put(CommonConstants.DOMS_STATUS_SEARCH_ENRICH_PROPERTY_NAME, domsStatus);
                    log.debug("Added domsStatus=" + domsStatus + " for joint accountId=" + accountId);
                }
            }
        }

        return searchData;
    }

    /**
     * Extract authorization IDs corresponding to joint/linked accounts.
     */
    @SuppressWarnings("unchecked")
    private static List<String> extractJointAccountAuthIDs(Map<String, Object> consent) {
        List<String> jointAccountAuthIDs = new java.util.ArrayList<>();

        Object authResourcesObj = consent.get(CommonConstants.AUTHORIZATION_RESOURCES);
        if (!(authResourcesObj instanceof List)) {
            return jointAccountAuthIDs;
        }

        List<Map<String, Object>> authResourcesList = (List<Map<String, Object>>) authResourcesObj;

        for (Map<String, Object> authResource : authResourcesList) {
            String authType = (String) authResource.get(CommonConstants.AUTH_TYPE);

            if (isJointAccount(authType)) {
                String authId = (String) authResource.get(CommonConstants.AUTHORIZATION_ID);
                if (authId != null) {
                    jointAccountAuthIDs.add(authId);
                }
            }
        }

        return jointAccountAuthIDs;
    }

    /**
     * Check if authorization type is joint or linked.
     */
    private static boolean isJointAccount(String authType) {
        if (authType == null) {
            return false;
        }
        return authType.equalsIgnoreCase("linkedMember");
    }

    /**
     * Get the DOMS status of the account from AccountMetadataService.
     *
     * @param accountId account ID
     * @return DOMS status string, e.g., "pre-approval" or "no-sharing"
     */
    private static String getDOMSStatus(String accountId) {
        try {
            return AccountMetadataServiceImpl.getInstance()
                    .getAccountMetadataByKey(accountId, CommonConstants.DOMS_STATUS);
        } catch (AccountMetadataException e) {
            log.error("Failed to retrieve DOMS status for accountId=" + accountId, e);
            return null;
        }
    }
}

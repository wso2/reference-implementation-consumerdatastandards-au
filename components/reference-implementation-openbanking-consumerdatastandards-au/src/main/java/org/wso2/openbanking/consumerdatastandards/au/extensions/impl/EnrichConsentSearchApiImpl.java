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

package org.wso2.openbanking.consumerdatastandards.au.extensions.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.CdsErrorEnum;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.CommonConstants;
import org.wso2.openbanking.consumerdatastandards.au.extensions.exceptions.AuthorizationFailureException;
import org.wso2.openbanking.consumerdatastandards.au.extensions.exceptions.CdsConsentException;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.EnrichConsentSearchRequestBody;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.SuccessResponseForConsentSearch;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.SuccessResponseForConsentSearchData;
import org.wso2.openbanking.consumerdatastandards.au.extensions.utils.CdsConsentSearchEnrichUtil;

import javax.ws.rs.core.Response;

/**
 * Implementation class for handling enrich consent search result.
 */
public class EnrichConsentSearchApiImpl {

    private static final Log log = LogFactory.getLog(EnrichConsentSearchApiImpl.class);


    /**
     * Handle enrich consent search request to enrich and transform consent search results
     * before sending the response back to the accelerator.
     * @param requestBody - the request body containing consent search results to be enriched
     * @return Response containing the enriched consent search data
     */
    public static Response handleEnrichSearchPost(EnrichConsentSearchRequestBody requestBody) {
        try {
            if (requestBody.getData() == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Missing data field in request").build();
            }

            SuccessResponseForConsentSearch response = new SuccessResponseForConsentSearch();

            // Adding DOMS status and secondary account info to the search result.
            SuccessResponseForConsentSearchData searchData =
                    CdsConsentSearchEnrichUtil.enrichSearchResult(requestBody.getData().getSearchResult(),
                            requestBody.getData().getEnrichmentParams());

            response.setResponseId(requestBody.getRequestId());
            response.setStatus(SuccessResponseForConsentSearch.StatusEnum.SUCCESS);
            response.setData(searchData);

            return Response.status(Response.Status.OK).entity(new JSONObject(response).toString()).build();

        } catch (CdsConsentException e) {
            log.error("Unexpected error during consent search enrichment", e);

            CdsConsentException cdsException = new CdsConsentException(CdsErrorEnum.UNEXPECTED_ERROR,
                    "Consent search enrichment failed. An unexpected error occurred: " + e.getMessage());

            AuthorizationFailureException authException = AuthorizationFailureException.createError(
                    cdsException.getErrorEnum(), cdsException.getErrorDetail(),
                    CommonConstants.REJECTED_STATUS, requestBody.getRequestId()
            );

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(authException.toFailedResponseJsonString()).build();
        }
    }
}

/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.IssueRefreshTokenRequestBody;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.SuccessResponseIssueRefreshToken;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.SuccessResponseIssueRefreshTokenData;

import javax.ws.rs.core.Response;

/**
 * Implementation class for handling issue refresh token requests.
 */
public class IssueRefreshTokenApiImpl {

    /**
     * Handle issue refresh token request to determine if a refresh token should be issued based on consent validity.
     * @param issueRefreshTokenRequestBody - the request body containing the consent validity information
     * @return SuccessResponseIssueRefreshToken containing the decision and validity period for the refresh token
     */
    public static Response handleIssueRefreshToken(IssueRefreshTokenRequestBody
                                                           issueRefreshTokenRequestBody) {

        SuccessResponseIssueRefreshToken successResponseIssueRefreshToken = new SuccessResponseIssueRefreshToken();
        successResponseIssueRefreshToken.setResponseId(issueRefreshTokenRequestBody.getRequestId());
        successResponseIssueRefreshToken.setStatus(SuccessResponseIssueRefreshToken.StatusEnum.SUCCESS);

        SuccessResponseIssueRefreshTokenData successResponseIssueRefreshTokenData = new SuccessResponseIssueRefreshTokenData();

        //If consent validity period is 0 or null, do not issue refresh token
        if(issueRefreshTokenRequestBody.getData().getConsentValidityPeriod() == 0 ||
                issueRefreshTokenRequestBody.getData().getConsentValidityPeriod() == null) {

            successResponseIssueRefreshTokenData.setIssueRefreshToken(false);
        } else {
            successResponseIssueRefreshTokenData.setIssueRefreshToken(true);

            long consentValidityPeriod = issueRefreshTokenRequestBody.getData().getConsentValidityPeriod();
            long consentCreatedPeriod = issueRefreshTokenRequestBody.getData().getConsentCreatedTime();

            successResponseIssueRefreshTokenData.setRefreshTokenValidityPeriod(consentValidityPeriod - consentCreatedPeriod);
        }

        successResponseIssueRefreshToken.setData(successResponseIssueRefreshTokenData);

        return Response.ok().entity(successResponseIssueRefreshToken).build();
    }
}

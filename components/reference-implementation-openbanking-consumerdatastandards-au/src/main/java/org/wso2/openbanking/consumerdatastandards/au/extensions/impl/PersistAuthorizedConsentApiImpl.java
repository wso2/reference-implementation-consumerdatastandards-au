/**
 * Copyright (c) 2025-2026, WSO2 LLC. (https://www.wso2.com).
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
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.PersistAuthorizedConsentRequestBody;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.SuccessResponsePersistAuthorizedConsent;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.SuccessResponsePersistAuthorizedConsentData;
import org.wso2.openbanking.consumerdatastandards.au.extensions.utils.CdsConsentAuthPersistUtil;

import javax.ws.rs.core.Response;

/**
 * Implementation class for handling persist authorized consent requests.
 */
public class PersistAuthorizedConsentApiImpl {

    private static final Log log = LogFactory.getLog(PersistAuthorizedConsentApiImpl.class);

    /**
     * Handle persist authorized consent request to save the authorized consent and complete the consent flow.
     * @param persistAuthorizedConsentRequestBody - the request body containing the consent data to be persisted
     * @return Response containing the status of the persistence operation
     */
    public static Response handlePersistAuthorizedConsent(PersistAuthorizedConsentRequestBody
                                                                  persistAuthorizedConsentRequestBody) {

        try {
            // Consent persist step
            SuccessResponsePersistAuthorizedConsentData persistConsentData = CdsConsentAuthPersistUtil
                    .cdsConsentPersist(persistAuthorizedConsentRequestBody);

            SuccessResponsePersistAuthorizedConsent response = new SuccessResponsePersistAuthorizedConsent();
            response.setResponseId(persistAuthorizedConsentRequestBody.getRequestId());
            response.setStatus(SuccessResponsePersistAuthorizedConsent.StatusEnum.SUCCESS);
            response.setData(persistConsentData);

            return Response.status(Response.Status.OK).entity(new JSONObject(response).toString()).build();

        } catch (CdsConsentException e) {
            // Convert any other exceptions to CdsConsentException automatically, then handle
            log.error("Unexpected error during consent persistence: " + e.getMessage(), e);

            CdsConsentException cdsException = new CdsConsentException(CdsErrorEnum.UNEXPECTED_ERROR,
                    "Consent persistence failed" + "An unexpected error occurred: " + e.getMessage());

            AuthorizationFailureException authException = AuthorizationFailureException.createError(
                cdsException.getErrorEnum(), cdsException.getErrorDetail(),
                CommonConstants.REJECTED_STATUS, persistAuthorizedConsentRequestBody.getRequestId()
            );

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(authException.toFailedResponseJsonString()).build();
        }
    }
}

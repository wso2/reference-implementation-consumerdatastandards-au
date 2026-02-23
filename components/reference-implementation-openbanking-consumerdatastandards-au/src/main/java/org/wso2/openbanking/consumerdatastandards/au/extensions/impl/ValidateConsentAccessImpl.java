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

import org.json.JSONObject;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.ValidateConsentAccessRequestBody;
import org.wso2.openbanking.consumerdatastandards.au.extensions.utils.CdsConsentValidateUtil;

import javax.ws.rs.core.Response;

/**
 * Implementation class for Validate Consent Access API.
 */
public class ValidateConsentAccessImpl {

    /**
     * Validate the consent for account related requests.
     * @param requestId The unique identifier for the request, used for tracking and response purposes.
     * @param dataPayload The request data payload to be validated, typically representing the API request body.
     * @param consentResource The consent resource payload associated with the request, typically representing the consent object.
     * @return A {@link JSONObject} representing the validation result. If validation passes, a success response is returned;
     * @throws Exception If validation fails due to invalid input, expired consent, or JSON processing errors.
     */
    public static Response validateConsent(ValidateConsentAccessRequestBody validateConsentAccessRequestBody)
            throws Exception {

        JSONObject validationResponse;

        // Read the request body
        String requestId = validateConsentAccessRequestBody.getRequestId();
        Object consentResource = validateConsentAccessRequestBody.getData().getConsentResource();
        Object dataPayload = validateConsentAccessRequestBody.getData().getDataRequestPayload();

        // Validate the consent
        validationResponse = CdsConsentValidateUtil.validateBankingApiSubmission(dataPayload, consentResource, requestId);

        return Response.status(Response.Status.OK).entity(validationResponse.toString()).build();
    }


}

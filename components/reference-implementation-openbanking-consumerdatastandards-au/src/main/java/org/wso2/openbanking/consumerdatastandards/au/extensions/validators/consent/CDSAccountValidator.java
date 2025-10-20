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

package org.wso2.openbanking.consumerdatastandards.au.extensions.validators.consent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.CDSErrorEnum;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.CommonConstants;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.ErrorConstants;
import org.wso2.openbanking.consumerdatastandards.au.extensions.utils.CDSConsentValidatorUtil;
import org.wso2.openbanking.consumerdatastandards.au.extensions.utils.CommonConsentExtensionUtils;
import org.wso2.openbanking.consumerdatastandards.au.extensions.utils.ErrorUtil;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Validator class for CDS Account related consent validations.
 */
public class CDSAccountValidator {

    private static final Log log = LogFactory.getLog(CDSAccountValidator.class);

    /**
     * Validate the consent for account related requests.
     * @param requestId The unique identifier for the request, used for tracking and response purposes.
     * @param dataPayload The request data payload to be validated, typically representing the API request body.
     * @param consentResource The consent resource payload associated with the request, typically representing the consent object.
     * @return A {@link JSONObject} representing the validation result. If validation passes, a success response is returned;
     * @throws Exception If validation fails due to invalid input, expired consent, or JSON processing errors.
     */
    public static JSONObject validateConsent(String requestId, Object dataPayload, Object consentResource) throws Exception {

        JSONObject validationResponse = null;

        // Validate the consent
        validationResponse = validateBankingApiSubmission(dataPayload,
                consentResource, requestId);

        return validationResponse;
    }

    /**
     * Validates a banking API submission by checking the data payload and consent payload for correctness,
     * consent status, and expiry.
     *
     * @param dataPayload     The request data payload to be validated, typically representing the API request body.
     * @param consentPayload  The consent resource payload associated with the request, typically representing the consent object.
     * @param requestId       The unique identifier for the request, used for tracking and response purposes.
     * @return                A {@link JSONObject} representing the validation result. If validation passes, a success response is returned;
     *                        otherwise, an error response is returned.
     * @throws Exception      If validation fails due to invalid input, expired consent, or JSON processing errors.
     */
    public static JSONObject validateBankingApiSubmission(Object dataPayload, Object consentPayload, String requestId)
            throws Exception {

        JSONObject jsonDataRequestBody = CommonConsentExtensionUtils.convertObjectToJson(dataPayload);
        JSONObject jsonConsentResourceBody = CommonConsentExtensionUtils.convertObjectToJson(consentPayload);

        String resourcePath = jsonDataRequestBody.getJSONObject(CommonConstants.RESOURCE_PARAMS)
                .getString(CommonConstants.RESOURCE_PATH);
        String electedResource = jsonDataRequestBody.getString(CommonConstants.ELECTED_RESOURCE);

        // Perform URI Validation.
        if (resourcePath == null || !CDSConsentValidatorUtil.isAccountURIValid(electedResource)) {

            log.error(ErrorConstants.PAYLOAD_FORMAT_ERROR);
            return ErrorUtil.createErrorResponse(CDSErrorEnum.UNEXPECTED_FIELD).toJSONObject();
        }

        //Consent Status Validation
        if (!CommonConstants.AUTHORIZED_STATUS
                .equalsIgnoreCase(jsonConsentResourceBody.get(CommonConstants.STATUS).toString())) {

            String description = "The associated consent for resource is not in a status " +
                    "that would allow the resource to be executed";
            log.error(description);
            return ErrorUtil.createErrorResponse(CDSErrorEnum.REVOKED_CONSENT).toJSONObject();
        }

        //Consent Expiry Validation
        if (CDSConsentValidatorUtil.isConsentExpired(jsonConsentResourceBody.getJSONObject(CommonConstants.RECEIPT)
                .getJSONObject(CommonConstants.ACCOUNT_DATA).get(CommonConstants.EXPIRATION_DATE_TIME).toString())) {

            String description = "The associated consent for resource is not in a status " +
                    "that would allow the resource to be executed";
            log.error(description);
            return ErrorUtil.createErrorResponse(CDSErrorEnum.INVALID_CONSENT, description).toJSONObject();
        }
        return CommonConsentExtensionUtils.getSuccessResponse(requestId);
    }
}

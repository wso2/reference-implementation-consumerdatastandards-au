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

package org.wso2.openbanking.consumerdatastandards.au.extensions.utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.CdsErrorEnum;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.CommonConstants;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.ErrorConstants;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Util class for CDS Consent Validator.
 */
public class CdsConsentValidateUtil {

    private static final Log log = LogFactory.getLog(CdsConsentValidateUtil.class);


    /**
     * Util method to validate the Account request URI.
     *
     * @param uri  Request URI
     * @return boolean indicating whether the URI is valid for Account API
     */
    public static boolean isAccountURIValid(String uri) {
        List<String> accountPaths = getAccountAPIPathRegexArray();
        boolean isValid = false;

        for (String entry : accountPaths) {
            if (uri.contains(entry)) {
                isValid = true;
                break;
            }
        }

        return isValid;
    }

    /**
     * Method provides API resource paths applicable for UK Account API.
     *
     * @return map of API Resources.
     */
    public static List<String> getAccountAPIPathRegexArray() {

        List<String> requestUrls = Arrays.asList(CommonConstants.ACCOUNT_REGEX,
                CommonConstants.ACCOUNT_DETAILS_REGEX,
                CommonConstants.PRODUCTS_REGEX,
                CommonConstants.PRODUCT_DETAILS_REGEX,
                CommonConstants.BALANCES_REGEX,
                CommonConstants.BALANCES_ID_REGEX,
                CommonConstants.TRANSACTIONS_ID_REGEX,
                CommonConstants.TRANSACTION_DETAILS_REGEX,
                CommonConstants.DIRECT_DEBITS_ID_REGEX,
                CommonConstants.DIRECT_DEBITS_REGEX,
                CommonConstants.SCHEDULED_PAYMENTS_ID_REGEX,
                CommonConstants.SCHEDULED_PAYMENTS_REGEX,
                CommonConstants.PAYEES_REGEX,
                CommonConstants.PAYEES_ID_REGEX);

        return requestUrls;
    }

    /**
     * Validate whether consent is expired.
     *
     * @param expDateVal     Expiration Date Time
     * @return boolean indicating whether the consent is expired
     */
    public static boolean isConsentExpired(String expDateVal) {

        if (expDateVal != null && !expDateVal.isEmpty()) {
            OffsetDateTime expDate = OffsetDateTime.parse(expDateVal);
            return OffsetDateTime.now().isAfter(expDate);
        } else {
            return false;
        }
    }

    /**
     * Validates a banking API submission by checking the data payload and consent payload for correctness,
     * consent status, and expiry.
     *
     * @param dataPayload     The request data payload to be validated, typically representing the API request body.
     * @param consentPayload  The consent resource payload associated with the request, typically representing
     *                        the consent object.
     * @param requestId       The unique identifier for the request, used for tracking and response purposes.
     * @return                A {@link JSONObject} representing the validation result. If validation passes, a success
     * response is returned;
     *                        otherwise, an error response is returned.
     * @throws Exception      If validation fails due to invalid input, expired consent, or JSON processing errors.
     */
    public static JSONObject validateBankingApiSubmission(Object dataPayload, Object consentPayload, String requestId)
            throws Exception {

        JSONObject jsonDataRequestBody = CommonConsentExtensionUtil.convertObjectToJson(dataPayload);
        JSONObject jsonConsentResourceBody = CommonConsentExtensionUtil.convertObjectToJson(consentPayload);

        String resourcePath = jsonDataRequestBody.getJSONObject(CommonConstants.RESOURCE_PARAMS)
                .getString(CommonConstants.RESOURCE_PATH);
        String electedResource = jsonDataRequestBody.getString(CommonConstants.ELECTED_RESOURCE);

        // Perform URI Validation.
        if (resourcePath == null || !isAccountURIValid(electedResource)) {

            log.error(ErrorConstants.PAYLOAD_FORMAT_ERROR);
            return ErrorUtil.createErrorResponse(CdsErrorEnum.UNEXPECTED_FIELD).toJSONObject();
        }

        //Consent Status Validation
        if (!CommonConstants.AUTHORIZED_STATUS
                .equalsIgnoreCase(jsonConsentResourceBody.get(CommonConstants.STATUS).toString())) {

            String description = "The associated consent for resource is not in a status " +
                    "that would allow the resource to be executed";
            log.error(description);
            return ErrorUtil.createErrorResponse(CdsErrorEnum.REVOKED_CONSENT).toJSONObject();
        }

        //Consent Expiry Validation
        if (isConsentExpired(jsonConsentResourceBody.getJSONObject(CommonConstants.RECEIPT)
                .getJSONObject(CommonConstants.ACCOUNT_DATA).get(CommonConstants.EXPIRATION_DATE_TIME).toString())) {

            String description = "The associated consent for resource is not in a status " +
                    "that would allow the resource to be executed";
            log.error(description);
            return ErrorUtil.createErrorResponse(CdsErrorEnum.INVALID_CONSENT, description).toJSONObject();
        }
        return CommonConsentExtensionUtil.getSuccessResponse(requestId);
    }
}

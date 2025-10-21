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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.CommonConstants;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.ErrorConstants;
import org.wso2.openbanking.consumerdatastandards.au.extensions.utils.CommonConsentExtensionUtils;
import org.wso2.openbanking.consumerdatastandards.au.extensions.utils.ErrorUtil;

/**
 * Validator class for CDS Pushed Authorization Request Object.
 */
public class CDSPushedAuthRequestValidator {

    private static final Log log = LogFactory.getLog(CDSPushedAuthRequestValidator.class);

    /**
     * Validates the CDS pushed authorization request object.
     * @param requestObject The request object to be validated, typically representing the CDS pushed authorization request payload.
     * @return A JSONObject containing error details if validation fails, or {@code null} if all validations pass.
     * @throws JsonProcessingException If an error occurs while converting the request object to a JSON representation.
     */
    public static JSONObject validateCDSPushedAuthRequest(Object requestObject)
            throws JsonProcessingException {

        JSONObject requestObjectJsonBody = CommonConsentExtensionUtils.convertObjectToJson(requestObject);
        JSONObject error;

        // Validate CDR Arrangement ID
        error = validateCDRArrangementId(requestObjectJsonBody);
        if (error != null) {
            return error;
        }

        // Validate Sharing Duration
        error = validateSharingDuration(requestObjectJsonBody);
        if (error != null) {
            return error;
        }

        return null;
    }

    /**
     * Validate cdr_arrangement_id if present in the request object.
     * @param requestObjectJsonBody
     * @return JSONObject if validation fails, null otherwise
     */
    private static JSONObject validateCDRArrangementId(JSONObject requestObjectJsonBody) {
        JSONObject claims = requestObjectJsonBody.optJSONObject(CommonConstants.CLAIMS);

        if (claims != null && claims.has(CommonConstants.CDR_ARRANGEMENT_ID)) {
            String cdrArrangementId = claims.optString(CommonConstants.CDR_ARRANGEMENT_ID, null);

            if ("null".equals(cdrArrangementId)) {
                // Handles a 'null' string value for the key.
                log.error("Validation failed: " + ErrorConstants.INVALID_CDR_ARRANGEMENT_ID);
                return ErrorUtil.getErrorDataObject(ErrorConstants.INVALID_REQUEST_OBJECT,
                        ErrorConstants.INVALID_CDR_ARRANGEMENT_ID);

            } else if (StringUtils.isBlank(cdrArrangementId)) {
                // Handles an empty or whitespace string ""
                log.error("Validation failed: " + ErrorConstants.EMPTY_CDR_ARRANGEMENT_ID);
                return ErrorUtil.getErrorDataObject(ErrorConstants.INVALID_REQUEST_OBJECT,
                        ErrorConstants.EMPTY_CDR_ARRANGEMENT_ID);
            }
        }

        // If 'cdr_arrangement_id' is valid or not present, return null to indicate success.
        return null;
    }

    /**
     * Validates the 'sharing_duration' claim if present in the request object.
     * @param requestObjectJsonBody The JSON body of the request object containing claims to be validated.
     * @return A JSONObject representing an error if validation fails, or null if validation passes.
     */
    private static JSONObject validateSharingDuration(JSONObject requestObjectJsonBody) {
        JSONObject claims = requestObjectJsonBody.optJSONObject(CommonConstants.CLAIMS);

        if (claims != null && claims.has(CommonConstants.SHARING_DURATION)) {
            String sharingDurationString = claims.optString(CommonConstants.SHARING_DURATION);

            if (StringUtils.isBlank(sharingDurationString)) {
                // Handles an empty or whitespace string ""
                return ErrorUtil.getErrorDataObject(ErrorConstants.INVALID_REQUEST_OBJECT,
                        ErrorConstants.INVALID_SHARING_DURATION);
            }

            try {
                int sharingDuration = Integer.parseInt(sharingDurationString);
                if (sharingDuration < 0) {
                    return ErrorUtil.getErrorDataObject(ErrorConstants.INVALID_REQUEST_OBJECT,
                            ErrorConstants.INVALID_SHARING_DURATION);
                }
            } catch (NumberFormatException e) {
                log.error(String.format("Error while parsing %s value: '%s' to a number.",
                        CommonConstants.SHARING_DURATION, sharingDurationString), e);
                return ErrorUtil.getErrorDataObject(ErrorConstants.INVALID_REQUEST_OBJECT,
                        ErrorConstants.INVALID_SHARING_DURATION);
            }
        }
        // If 'sharing_duration' is valid or not present, return null to indicate success.
        return null;
    }
}

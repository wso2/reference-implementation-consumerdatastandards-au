/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.openbanking.consumerdatastandards.au.extensions.exceptions;

import org.json.JSONObject;
import org.wso2.openbanking.consumerdatastandards.au.extensions.utils.ErrorUtil;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.FailedResponseInConsentAuthorize;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.FailedResponseInConsentAuthorizeData;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.CDSErrorEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Exception class for authorization failures that can be represented using 
 * FailedResponseInConsentAuthorize and FailedResponseInConsentAuthorizeData models.
 * Supports both CDS error integration and custom error messages.
 */
public class AuthorizationFailureException extends Exception {

    private String newStatus;
    private String responseId;
    private CDSErrorEnum cdsError;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public AuthorizationFailureException(String message) {
        super(message);
    }

    public AuthorizationFailureException(String message, Throwable e) {
        super(message, e);
    }

    /**
     * Constructor with CDS error for structured error handling.
     *
     * @param cdsError   the CDS error type from CDSErrorEnum
     * @param newStatus  new consent status to be set
     * @param responseId response ID for the failed request
     */
    public AuthorizationFailureException(CDSErrorEnum cdsError, String newStatus, String responseId) {
        super(cdsError.getDetail());
        this.cdsError = cdsError;
        this.newStatus = newStatus;
        this.responseId = responseId;
    }

    /**
     * Constructor with CDS error and custom message.
     *
     * @param cdsError      the CDS error type from CDSErrorEnum
     * @param customMessage custom error message
     * @param newStatus     new consent status to be set
     * @param responseId    response ID for the failed request
     */
    public AuthorizationFailureException(CDSErrorEnum cdsError, String customMessage, String newStatus, String responseId) {
        super(customMessage);
        this.cdsError = cdsError;
        this.newStatus = newStatus;
        this.responseId = responseId;
    }

    /**
     * Sets response id for the failed response in consent authorize.
     *
     * @param responseId response id of matching the made request
     */
    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }

    /**
     * Get response ID.
     *
     * @return response ID
     */
    public String getResponseId() {
        return responseId;
    }

    /**
     * Creates a typed FailedResponseInConsentAuthorize object.
     *
     * @return FailedResponseInConsentAuthorize model object
     */
    public FailedResponseInConsentAuthorize toFailedResponse() {
        // Create the data object with error message and new consent status
        FailedResponseInConsentAuthorizeData data = new FailedResponseInConsentAuthorizeData(getMessage());
        if (newStatus != null) {
            data.setNewConsentStatus(newStatus);
        }

        // Create the main response object
        return new FailedResponseInConsentAuthorize(
            responseId != null ? responseId : "unknown",
            FailedResponseInConsentAuthorize.StatusEnum.ERROR,
            data
        );
    }

    /**
     * Returns exception formatted as a FailedResponseInConsentAuthorize JSON string.
     *
     * @return formatted error as JSON string
     */
    public String toFailedResponseJsonString() {
        try {
            return objectMapper.writeValueAsString(toFailedResponse());
        } catch (JsonProcessingException e) {
            // Fallback to legacy method if JSON serialization fails
            return toJsonString();
        }
    }

    /**
     * Returns exception formatted as a FailedResponseInConsentAuthorize.
     * @return formatted error as a JSONObject (legacy method)
     */
    public JSONObject toJson() {
        return ErrorUtil.getFormattedAuthorizationFailureException(this.responseId, getMessage(), this.newStatus);
    }

    /**
     * Returns formatted error as String.
     * @return formatted error as a String (legacy method)
     */
    public String toJsonString() {
        return toJson().toString();
    }

    // Static factory methods for common authorization errors

    /**
     * Create authorization failure with CDS error.
     *
     * @param cdsError   the CDS error type
     * @param newStatus  new consent status
     * @param responseId response ID
     * @return AuthorizationFailureException
     */
    public static AuthorizationFailureException createError(CDSErrorEnum cdsError, String newStatus, String responseId) {
        return new AuthorizationFailureException(cdsError, newStatus, responseId);
    }

    /**
     * Create authorization failure with CDS error and custom message.
     *
     * @param cdsError      the CDS error type
     * @param customMessage custom error message
     * @param newStatus     new consent status
     * @param responseId    response ID
     * @return AuthorizationFailureException
     */
    public static AuthorizationFailureException createError(CDSErrorEnum cdsError, String customMessage, String newStatus, String responseId) {
        return new AuthorizationFailureException(cdsError, customMessage, newStatus, responseId);
    }

    /**
     * Create invalid consent authorization failure.
     *
     * @param responseId response ID
     * @return AuthorizationFailureException
     */
    public static AuthorizationFailureException invalidConsent(String responseId) {
        return createError(CDSErrorEnum.INVALID_CONSENT, "rejected", responseId);
    }

    /**
     * Create unauthorized access failure.
     *
     * @param responseId response ID
     * @return AuthorizationFailureException
     */
    public static AuthorizationFailureException unauthorized(String responseId) {
        return createError(CDSErrorEnum.UNAUTHORIZED, "rejected", responseId);
    }

    /**
     * Create revoked consent failure.
     *
     * @param responseId response ID
     * @return AuthorizationFailureException
     */
    public static AuthorizationFailureException revokedConsent(String responseId) {
        return createError(CDSErrorEnum.REVOKED_CONSENT, "revoked", responseId);
    }
}

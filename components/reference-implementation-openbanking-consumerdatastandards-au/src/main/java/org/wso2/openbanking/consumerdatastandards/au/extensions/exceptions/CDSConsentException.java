/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.openbanking.consumerdatastandards.au.extensions.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.CDSErrorEnum;

/**
 * Unified exception class for all CDS-related errors.
 * Handles both consent and extension errors using CDSErrorEnum.
 */
public class CDSConsentException extends Exception {

    private final CDSErrorEnum errorEnum;
    private final String customDetail;
    private final String urn;

    /**
     * Constructor with CDSErrorEnum only.
     *
     * @param errorEnum the error enum from CDSErrorEnum
     */
    public CDSConsentException(CDSErrorEnum errorEnum) {
        super(errorEnum.getDetail());
        this.errorEnum = errorEnum;
        this.customDetail = null;
        this.urn = null;
    }

    /**
     * Constructor with CDSErrorEnum and custom detail message.
     *
     * @param errorEnum    the error enum from CDSErrorEnum
     * @param customDetail custom detail message
     */
    public CDSConsentException(CDSErrorEnum errorEnum, String customDetail) {
        super(customDetail != null ? customDetail : errorEnum.getDetail());
        this.errorEnum = errorEnum;
        this.customDetail = customDetail;
        this.urn = null;
    }

    /**
     * Constructor with CDSErrorEnum, custom detail and URN.
     *
     * @param errorEnum    the error enum from CDSErrorEnum
     * @param customDetail custom detail message
     * @param urn          URN for error categorization
     */
    public CDSConsentException(CDSErrorEnum errorEnum, String customDetail, String urn) {
        super(customDetail != null ? customDetail : errorEnum.getDetail());
        this.errorEnum = errorEnum;
        this.customDetail = customDetail;
        this.urn = urn;
    }

    /**
     * Constructor with cause for exception chaining.
     *
     * @param errorEnum    the error enum from CDSErrorEnum
     * @param customDetail custom detail message
     * @param cause        the cause of this exception
     */
    public CDSConsentException(CDSErrorEnum errorEnum, String customDetail, Throwable cause) {
        super(customDetail != null ? customDetail : errorEnum.getDetail(), cause);
        this.errorEnum = errorEnum;
        this.customDetail = customDetail;
        this.urn = null;
    }

    /**
     * Constructor with all parameters including cause.
     *
     * @param errorEnum    the error enum from CDSErrorEnum
     * @param customDetail custom detail message
     * @param urn          URN for error categorization
     * @param cause        the cause of this exception
     */
    public CDSConsentException(CDSErrorEnum errorEnum, String customDetail, String urn, Throwable cause) {
        super(customDetail != null ? customDetail : errorEnum.getDetail(), cause);
        this.errorEnum = errorEnum;
        this.customDetail = customDetail;
        this.urn = urn;
    }

    /**
     * Get the error enum.
     *
     * @return CDSErrorEnum
     */
    public CDSErrorEnum getErrorEnum() {
        return errorEnum;
    }

    /**
     * Get the HTTP status code.
     *
     * @return HTTP status code
     */
    public int getHttpCode() {
        return errorEnum.getHttpCode();
    }

    /**
     * Get the error code string.
     *
     * @return error code
     */
    public String getErrorCode() {
        return errorEnum.getCode();
    }

    /**
     * Get the error title.
     *
     * @return error title
     */
    public String getErrorTitle() {
        return errorEnum.getTitle();
    }

    /**
     * Get the custom detail if provided, otherwise the default detail.
     *
     * @return detail message
     */
    public String getErrorDetail() {
        return customDetail != null ? customDetail : errorEnum.getDetail();
    }

    // Common factory method for any CDS error
    /**
     * Convert any Throwable to appropriate CDSConsentException based on exception type.
     * This method maps different exception types to appropriate CDS errors.
     *
     * @param throwable the original exception
     * @param context   optional context message for better error description
     * @return CDSConsentException with appropriate CDS error enum
     */
    public static CDSConsentException fromThrowable(Throwable throwable, String context) {
        String contextMessage = context != null ? context + ": " : "";

        // If it's already a CDSConsentException, return as-is
        if (throwable instanceof CDSConsentException) {
            return (CDSConsentException) throwable;
        }

        // Map specific exception types to appropriate CDS errors
        if (throwable instanceof JsonProcessingException) {
            return new CDSConsentException(CDSErrorEnum.UNEXPECTED_ERROR,
                    contextMessage + "JSON processing failed: " + throwable.getMessage(), throwable);
        }

        if (throwable instanceof NullPointerException) {
            return new CDSConsentException(CDSErrorEnum.FIELD_MISSING,
                    contextMessage + "Required field is missing or null", throwable);
        }

        if (throwable instanceof IllegalArgumentException) {
            return new CDSConsentException(CDSErrorEnum.INVALID_FIELD,
                    contextMessage + "Invalid field value: " + throwable.getMessage(), throwable);
        }

        if (throwable instanceof ClassCastException) {
            return new CDSConsentException(CDSErrorEnum.INVALID_FIELD,
                    contextMessage + "Invalid data type in request", throwable);
        }

        if (throwable instanceof NumberFormatException) {
            return new CDSConsentException(CDSErrorEnum.INVALID_FIELD,
                    contextMessage + "Invalid number format: " + throwable.getMessage(), throwable);
        }

        if (throwable instanceof SecurityException) {
            return new CDSConsentException(CDSErrorEnum.UNAUTHORIZED,
                    contextMessage + "Security validation failed", throwable);
        }

        // Default mapping for any other exception
        return new CDSConsentException(CDSErrorEnum.UNEXPECTED_ERROR,
                contextMessage + "An unexpected error occurred: " + throwable.getMessage(), throwable);
    }
}

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
import org.wso2.openbanking.consumerdatastandards.au.extensions.model.CDSErrorResponse;
import org.wso2.openbanking.consumerdatastandards.au.extensions.utils.ErrorUtil;

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

    /**
     * Get the URN if provided.
     *
     * @return URN string
     */
    public String getUrn() {
        return urn;
    }

    /**
     * Get CDS-compliant error response.
     *
     * @return CDSErrorResponse object
     */
    public CDSErrorResponse getCDSErrorResponse() {
        return ErrorUtil.createErrorResponse(errorEnum, customDetail, urn);
    }

    /**
     * Get error response as JSON string for HTTP responses.
     *
     * @return JSON string representation
     */
    public String toJsonString() {
        return ErrorUtil.toJsonString(getCDSErrorResponse());
    }

    // Common factory method for any CDS error

    /**
     * Create CDS exception using any error from CDSErrorEnum.
     * This method reads all necessary information (HTTP status, error code, title, detail)
     * from the CDSErrorEnum and creates the appropriate exception.
     *
     * @param errorEnum the error type from CDSErrorEnum
     * @return CDSConsentException
     */
    public static CDSConsentException createError(CDSErrorEnum errorEnum) {
        return new CDSConsentException(errorEnum);
    }

    /**
     * Create CDS exception using any error from CDSErrorEnum with custom detail message.
     * This method reads HTTP status, error code, and title from CDSErrorEnum,
     * but uses the provided custom detail message instead of the default one.
     *
     * @param errorEnum    the error type from CDSErrorEnum
     * @param customDetail custom detail message (can include formatted strings)
     * @return CDSConsentException
     */
    public static CDSConsentException createError(CDSErrorEnum errorEnum, String customDetail) {
        return new CDSConsentException(errorEnum, customDetail);
    }

    /**
     * Create CDS exception using any error from CDSErrorEnum with custom detail and URN.
     * This method reads HTTP status, error code, and title from CDSErrorEnum,
     * uses the provided custom detail message and URN for categorization.
     *
     * @param errorEnum    the error type from CDSErrorEnum
     * @param customDetail custom detail message (can include formatted strings)
     * @param urn          URN for error categorization
     * @return CDSConsentException
     */
    public static CDSConsentException createError(CDSErrorEnum errorEnum, String customDetail, String urn) {
        return new CDSConsentException(errorEnum, customDetail, urn);
    }

    /**
     * Convert any Throwable to appropriate CDSConsentException based on exception type.
     * This method intelligently maps different exception types to appropriate CDS errors,
     * eliminating the need for multiple catch clauses.
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

    /**
     * Convert any Throwable to appropriate CDSConsentException (without context).
     *
     * @param throwable the original exception
     * @return CDSConsentException with appropriate CDS error enum
     */
    public static CDSConsentException fromThrowable(Throwable throwable) {
        return fromThrowable(throwable, null);
    }

    /**
     * Wrap any operation that might throw exceptions in a CDS-compliant way.
     * This method allows you to execute any code block and automatically converts
     * any thrown exceptions to appropriate CDSConsentException.
     *
     * @param operation the operation to execute
     * @param context   optional context message
     * @param <T>       return type of the operation
     * @return result of the operation
     * @throws CDSConsentException if any exception occurs during operation
     */
    public static <T> T wrapOperation(ThrowingSupplier<T> operation, String context) throws CDSConsentException {
        try {
            return operation.get();
        } catch (Exception e) {
            throw fromThrowable(e, context);
        }
    }

    /**
     * Wrap any operation that might throw exceptions (without context).
     *
     * @param operation the operation to execute
     * @param <T>       return type of the operation
     * @return result of the operation
     * @throws CDSConsentException if any exception occurs during operation
     */
    public static <T> T wrapOperation(ThrowingSupplier<T> operation) throws CDSConsentException {
        return wrapOperation(operation, null);
    }

    /**
     * Wrap any void operation that might throw exceptions.
     *
     * @param operation the operation to execute
     * @param context   optional context message
     * @throws CDSConsentException if any exception occurs during operation
     */
    public static void wrapVoidOperation(ThrowingRunnable operation, String context) throws CDSConsentException {
        try {
            operation.run();
        } catch (Exception e) {
            throw fromThrowable(e, context);
        }
    }

    /**
     * Wrap any void operation that might throw exceptions (without context).
     *
     * @param operation the operation to execute
     * @throws CDSConsentException if any exception occurs during operation
     */
    public static void wrapVoidOperation(ThrowingRunnable operation) throws CDSConsentException {
        wrapVoidOperation(operation, null);
    }

    // ========== FUNCTIONAL INTERFACES FOR OPERATIONS ==========

    /**
     * Functional interface for operations that return a value and might throw exceptions.
     *
     * @param <T> return type
     */
    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    /**
     * Functional interface for void operations that might throw exceptions.
     */
    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }
}
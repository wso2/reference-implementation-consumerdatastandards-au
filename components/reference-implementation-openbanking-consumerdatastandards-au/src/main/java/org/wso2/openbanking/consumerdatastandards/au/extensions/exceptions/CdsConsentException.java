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
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.CdsErrorEnum;

/**
 * Unified exception class for all CDS-related errors.
 * Handles both consent and extension errors using CdsErrorEnum.
 */
public class CdsConsentException extends Exception {

    private final CdsErrorEnum errorEnum;
    private final String customDetail;
    private final String urn;

    /**
     * Constructor with CdsErrorEnum only.
     *
     * @param errorEnum the error enum from CdsErrorEnum
     */
    public CdsConsentException(CdsErrorEnum errorEnum) {
        super(errorEnum.getDetail());
        this.errorEnum = errorEnum;
        this.customDetail = null;
        this.urn = null;
    }

    /**
     * Constructor with CdsErrorEnum and custom detail message.
     *
     * @param errorEnum    the error enum from CdsErrorEnum
     * @param customDetail custom detail message
     */
    public CdsConsentException(CdsErrorEnum errorEnum, String customDetail) {
        super(customDetail != null ? customDetail : errorEnum.getDetail());
        this.errorEnum = errorEnum;
        this.customDetail = customDetail;
        this.urn = null;
    }

    /**
     * Constructor with CdsErrorEnum, custom detail and URN.
     *
     * @param errorEnum    the error enum from CdsErrorEnum
     * @param customDetail custom detail message
     * @param urn          URN for error categorization
     */
    public CdsConsentException(CdsErrorEnum errorEnum, String customDetail, String urn) {
        super(customDetail != null ? customDetail : errorEnum.getDetail());
        this.errorEnum = errorEnum;
        this.customDetail = customDetail;
        this.urn = urn;
    }

    /**
     * Constructor with cause for exception chaining.
     *
     * @param errorEnum    the error enum from CdsErrorEnum
     * @param customDetail custom detail message
     * @param cause        the cause of this exception
     */
    public CdsConsentException(CdsErrorEnum errorEnum, String customDetail, Throwable cause) {
        super(customDetail != null ? customDetail : errorEnum.getDetail(), cause);
        this.errorEnum = errorEnum;
        this.customDetail = customDetail;
        this.urn = null;
    }

    /**
     * Constructor with all parameters including cause.
     *
     * @param errorEnum    the error enum from CdsErrorEnum
     * @param customDetail custom detail message
     * @param urn          URN for error categorization
     * @param cause        the cause of this exception
     */
    public CdsConsentException(CdsErrorEnum errorEnum, String customDetail, String urn, Throwable cause) {
        super(customDetail != null ? customDetail : errorEnum.getDetail(), cause);
        this.errorEnum = errorEnum;
        this.customDetail = customDetail;
        this.urn = urn;
    }

    /**
     * Get the error enum.
     *
     * @return CdsErrorEnum
     */
    public CdsErrorEnum getErrorEnum() {
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
}

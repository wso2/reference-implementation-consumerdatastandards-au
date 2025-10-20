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

package org.wso2.openbanking.consumerdatastandards.au.extensions.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.CDSErrorEnum;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.ErrorConstants;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.ErrorResponse;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.FailedResponse;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.FailedResponseInConsentAuthorize;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.FailedResponseInConsentAuthorizeData;
import org.wso2.openbanking.consumerdatastandards.au.extensions.model.CDSErrorFormat;
import org.wso2.openbanking.consumerdatastandards.au.extensions.model.CDSErrorMeta;
import org.wso2.openbanking.consumerdatastandards.au.extensions.model.CDSErrorResponse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Utility class for creating CDS compliant error responses.
 */
public class ErrorUtil {

    private static final Log log = LogFactory.getLog(ErrorUtil.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Create error response from CDSErrorEnum.
     *
     * @param errorEnum the error enum
     * @return CDSErrorResponse
     */
    public static CDSErrorResponse createErrorResponse(CDSErrorEnum errorEnum) {
        CDSErrorFormat error = new CDSErrorFormat(errorEnum.getCode(), errorEnum.getTitle());
        return new CDSErrorResponse(error);
    }

    /**
     * Create error response from CDSErrorEnum with custom detail.
     *
     * @param errorEnum the error enum
     * @param detail    custom detail message
     * @return CDSErrorResponse
     */
    public static CDSErrorResponse createErrorResponse(CDSErrorEnum errorEnum, String detail) {
        CDSErrorFormat error = new CDSErrorFormat(errorEnum.getCode(), errorEnum.getTitle(), detail);
        return new CDSErrorResponse(error);
    }

    /**
     * Create error response from CDSErrorEnum with URN meta.
     *
     * @param errorEnum the error enum
     * @param urn       the URN for meta
     * @return CDSErrorResponse
     */
    public static CDSErrorResponse createErrorResponseWithUrn(CDSErrorEnum errorEnum, String urn) {
        CDSErrorMeta meta = new CDSErrorMeta(urn);
        CDSErrorFormat error = new CDSErrorFormat(errorEnum.getCode(), errorEnum.getTitle(), errorEnum.getDetail(), meta);
        return new CDSErrorResponse(error);
    }

    /**
     * Create error response from CDSErrorEnum with custom detail and URN.
     *
     * @param errorEnum the error enum
     * @param detail    custom detail message
     * @param urn       the URN for meta
     * @return CDSErrorResponse
     */
    public static CDSErrorResponse createErrorResponse(CDSErrorEnum errorEnum, String detail, String urn) {
        CDSErrorMeta meta = new CDSErrorMeta(urn);
        CDSErrorFormat error = new CDSErrorFormat(errorEnum.getCode(), errorEnum.getTitle(), detail, meta);
        return new CDSErrorResponse(error);
    }

    /**
     * Create custom error response.
     *
     * @param code   error code
     * @param title  error title
     * @param detail error detail (optional)
     * @param urn    URN for meta (optional)
     * @return CDSErrorResponse
     */
    public static CDSErrorResponse createCustomErrorResponse(String code, String title, String detail, String urn) {
        CDSErrorMeta meta = urn != null ? new CDSErrorMeta(urn) : null;
        CDSErrorFormat error = new CDSErrorFormat(code, title, detail, meta);
        return new CDSErrorResponse(error);
    }

    /**
     * Method to get ErrorResponse object for error.
     * @param errorMessage error message
     * @param errorDescription error description
     * @return ErrorResponse object
     */
    public static ErrorResponse getErrorResponse(String errorMessage, String errorDescription) {
        return new ErrorResponse(ErrorResponse.StatusEnum.ERROR, getErrorDataObject(errorMessage, errorDescription));
    }

    /**
     * Method to get error data object.
     * @param errorMessage error message
     * @param errorDescription error description
     * @return JSONObject containing error data
     */
    public static JSONObject getErrorDataObject(String errorMessage, String errorDescription) {
        JSONObject data = new JSONObject();
        data.put("errorMessage", errorMessage);
        data.put("errorDescription", errorDescription);
        return data;
    }

    /**
     * Get formatted error object for internal errors.
     * @param data error data
     * @return JSONObject containing formatted error response
     */
    public static JSONObject getFormattedErrorResponse(JSONObject data) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(ErrorResponse.StatusEnum.ERROR);
        errorResponse.setData(data);
        return new JSONObject(errorResponse);
    }

    /**
     * Method to build FailedResponseInConsentAuthorize object from failed authorization exceptions.
     *
     * @param responseId response ID
     * @param message    error message
     * @param newStatus  new consent status (optional)
     * @return JSONObject containing formatted authorization failure exception response
     */
    public static JSONObject getFormattedAuthorizationFailureException(String responseId, String message,
                                                                       String newStatus) {
        FailedResponseInConsentAuthorize failedResponse = new FailedResponseInConsentAuthorize();
        failedResponse.setResponseId(responseId);
        failedResponse.setStatus(FailedResponseInConsentAuthorize.StatusEnum.ERROR);

        FailedResponseInConsentAuthorizeData responseData = new FailedResponseInConsentAuthorizeData();
        responseData.setErrorMessage(message);
        if (newStatus != null && !newStatus.isEmpty()) {
            responseData.setNewConsentStatus(newStatus);
        }

        return new JSONObject(failedResponse.data(responseData));
    }
}

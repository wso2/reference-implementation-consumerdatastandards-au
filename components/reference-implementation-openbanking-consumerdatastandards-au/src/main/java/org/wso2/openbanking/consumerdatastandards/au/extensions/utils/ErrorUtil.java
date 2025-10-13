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
     * Convert CDSErrorResponse to JSON string.
     *
     * @param errorResponse the error response
     * @return JSON string
     */
    public static String toJsonString(CDSErrorResponse errorResponse) {
        try {
            return objectMapper.writeValueAsString(errorResponse);
        } catch (JsonProcessingException e) {
            // Return simple fallback JSON
            return "{\"errors\":[{\"code\":\"UNKNOWN\",\"title\":\"Error\",\"detail\":\"Failed to serialize error\"}]}";
        }
    }

    // ========== ENHANCED LEGACY METHODS (UPDATED TO USE NEW FORMAT) ==========

    /**
     * Method to get the error json with multiple error objects for CDS.
     *
     * @param errors - Array with multiple error details
     * @return Error in JSON format
     */
    public static String getErrorJson(JSONArray errors) {
        Gson gson = new Gson();
        return gson.toJson(constructErrorObject(errors));
    }

    /**
     * Method to construct final error object for CDS.
     * Enhanced to work with the new CDSErrorFormat structure.
     *
     * @param errorData - Error Data array
     * @return CDS Error Response
     */
    public static CDSErrorResponse constructErrorObject(JSONArray errorData) {
        List<CDSErrorFormat> errorArray = new ArrayList<>();

        for (Object errorDatum : errorData) {
            JSONObject errorElement = (JSONObject) errorDatum;
            
            // Get the enum for respective error
            CDSErrorEnum cdsError = CDSErrorEnum.fromValue(errorElement.get(ErrorConstants.ERROR_ENUM).toString());

            // Setting the error details
            String errorMessage;
            String metaUrnError = StringUtils.EMPTY;
            
            if (errorElement.get(ErrorConstants.DETAIL) != null) {
                try {
                    Object errorObject = new JSONParser(JSONParser.MODE_PERMISSIVE).parse(
                            errorElement.getString(ErrorConstants.DETAIL));
                    
                    if (errorObject instanceof JSONObject) {
                        JSONObject errorJSON = (JSONObject) errorObject;
                        errorMessage = errorJSON.getString(ErrorConstants.DETAIL);
                        
                        if (errorJSON.getString(ErrorConstants.META_URN) != null) {
                            metaUrnError = errorJSON.getString(ErrorConstants.META_URN);
                        }
                    } else {
                        errorMessage = errorObject.toString();
                    }
                } catch (ParseException e) {
                    log.error("Unexpected error while parsing string", e);
                    errorMessage = "Unexpected error while parsing string";
                }
            } else {
                errorMessage = cdsError.getDetail();
            }

            // Create error using new simplified approach
            CDSErrorMeta meta = null;
            if (StringUtils.isNotBlank(metaUrnError)) {
                meta = new CDSErrorMeta(metaUrnError);
            } else if (errorElement.get(ErrorConstants.METADATA) != null) {
                meta = (CDSErrorMeta) errorElement.get(ErrorConstants.METADATA);
            }

            CDSErrorFormat error = new CDSErrorFormat(
                    cdsError.getCode(),
                    cdsError.getTitle(),
                    errorMessage,
                    meta
            );
            
            errorArray.add(error);
        }

        return new CDSErrorResponse(errorArray);
    }

    /**
     * Method to construct object for CDS to pass to the Error Generation Library.
     *
     * @param error        Relevant Error enum from the CDSErrorEnum
     * @param errorMessage Custom error message
     * @param metaData     metadata object
     * @return JSONObject for legacy compatibility
     */
    public static JSONObject getErrorObject(CDSErrorEnum error, String errorMessage, CDSErrorMeta metaData) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(ErrorConstants.ERROR_ENUM, error);
        jsonObject.put(ErrorConstants.DETAIL, errorMessage);
        jsonObject.put(ErrorConstants.METADATA, metaData);
        return jsonObject;
    }

    // ========== UTILITY METHODS (RETAINED FOR COMPATIBILITY) ==========

    /**
     * Method to check whether error status code list have any client errors.
     */
    public static boolean isAnyClientErrors(HashSet<String> statusCodes) {
        for (String statusCode : statusCodes) {
            if (statusCode.startsWith("4")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method to get the HTTP Status code.
     */
    public static int getHTTPErrorCode(HashSet<String> statusCodes) {
        if (statusCodes.contains(ErrorConstants.HTTP_UNAUTHORIZED)) {
            return HttpStatus.SC_UNAUTHORIZED;
        } else if (statusCodes.contains(ErrorConstants.HTTP_FORBIDDEN)) {
            return HttpStatus.SC_FORBIDDEN;
        } else if (statusCodes.contains(ErrorConstants.HTTP_NOT_FOUND)) {
            return HttpStatus.SC_NOT_FOUND;
        } else if (statusCodes.contains(ErrorConstants.HTTP_NOT_ACCEPTABLE)) {
            return HttpStatus.SC_NOT_ACCEPTABLE;
        } else if (statusCodes.contains(ErrorConstants.HTTP_UNSUPPORTED_MEDIA_TYPE)) {
            return HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE;
        } else if (statusCodes.contains(ErrorConstants.HTTP_UNPROCESSABLE_ENTITY)) {
            return HttpStatus.SC_UNPROCESSABLE_ENTITY;
        } else {
            return HttpStatus.SC_BAD_REQUEST;
        }
    }

    /**
     * Extract a meaningful error message from a JSON response.
     */
    public static String getErrorMessage(JSONObject data) {
        String message = data.toString();

        JSONArray tppMessages = data.optJSONArray("tppMessages");
        if (tppMessages != null) {
            JSONObject errorObj = tppMessages.optJSONObject(0);
            if (errorObj != null) {
                String text = errorObj.optString("text");
                if (text != null && !text.isEmpty()) {
                    message = text;
                }
            }
        }
        return message;
    }

    // ========== LEGACY RESPONSE METHODS (MAINTAINED FOR BACKWARD COMPATIBILITY) ==========

    /**
     * Method to get ErrorResponse object for error.
     */
    public static ErrorResponse getErrorResponse(String errorMessage, String errorDescription) {
        return new ErrorResponse(ErrorResponse.StatusEnum.ERROR, getErrorDataObject(errorMessage, errorDescription));
    }

    /**
     * Method to construct the error data object.
     */
    public static JSONObject getErrorDataObject(String errorMessage, String errorDescription) {
        JSONObject data = new JSONObject();
        data.put("errorMessage", errorMessage);
        data.put("errorDescription", errorDescription);
        return data;
    }

    /**
     * Get formatted error object for internal errors.
     */
    public static JSONObject getFormattedErrorResponse(JSONObject data) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(ErrorResponse.StatusEnum.ERROR);
        errorResponse.setData(data);
        return new JSONObject(errorResponse);
    }

    /**
     * Method to build FailedResponseInConsentAuthorize object from failed authorization exceptions.
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

    /**
     * Get formatted error object for validation failures.
     */
    public static JSONObject getFormattedFailedResponse(int errorCode, JSONObject data) {
        FailedResponse failedResponse = new FailedResponse();
        failedResponse.setStatus(FailedResponse.StatusEnum.ERROR);
        failedResponse.setErrorCode(errorCode);
        failedResponse.setData(data);
        return new JSONObject(failedResponse);
    }
}

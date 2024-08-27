/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.openbanking.cds.gateway.executors.error.handler;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.wso2.openbanking.accelerator.common.error.OpenBankingErrorCodes;
import com.wso2.openbanking.accelerator.gateway.executor.core.OpenBankingGatewayExecutor;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OpenBankingExecutorError;
import com.wso2.openbanking.accelerator.gateway.util.GatewayConstants;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.wso2.openbanking.cds.common.error.handling.util.ErrorConstants;
import org.wso2.openbanking.cds.common.error.handling.util.ErrorUtil;
import org.wso2.openbanking.cds.gateway.executors.idpermanence.utils.IdPermanenceConstants;
import org.wso2.openbanking.cds.gateway.executors.idpermanence.utils.IdPermanenceUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

/**
 * Executor to handle gateway errors in CDS format.
 */
public class CDSErrorHandler implements OpenBankingGatewayExecutor {

    private static Log log = LogFactory.getLog(CDSErrorHandler.class);
    private static final String STATUS_CODE = "statusCode";
    private static final String RESPONSE_PAYLOAD_SIZE = "responsePayloadSize";
    public static final String X_FAPI_INTERACTION_ID = "x-fapi-interaction-id";

    //Accelerator error codes
    public static final String ACCELERATOR_EXPECTED_ERROR = "200012";

    /**
     * Method to handle pre request.
     *
     * @param obapiRequestContext OB request context object
     */
    @Override
    public void preProcessRequest(OBAPIRequestContext obapiRequestContext) {

        handleRequestError(obapiRequestContext);

    }

    /**
     * Method to handle post request.
     *
     * @param obapiRequestContext OB request context object
     */
    @Override
    public void postProcessRequest(OBAPIRequestContext obapiRequestContext) {

        handleRequestError(obapiRequestContext);
    }

    /**
     * Method to handle pre response.
     *
     * @param obapiResponseContext OB response context object
     */
    @Override
    public void preProcessResponse(OBAPIResponseContext obapiResponseContext) {

        handleResponseError(obapiResponseContext);
    }

    /**
     * Method to handle post response.
     *
     * @param obapiResponseContext OB response context object
     */
    @Override
    public void postProcessResponse(OBAPIResponseContext obapiResponseContext) {

        handleResponseError(obapiResponseContext);
    }


    protected void handleRequestError(OBAPIRequestContext obapiRequestContext) {

        if (!obapiRequestContext.isError()) {
            return;
        }

        ArrayList<OpenBankingExecutorError> errors = obapiRequestContext.getErrors();
        HashSet<String> statusCodes = new HashSet<>();

        for (OpenBankingExecutorError error : errors) {
            statusCodes.add(error.getHttpStatusCode());
        }

        // handle DCR and Unauthorized errors according to oAuth2 format
        if (statusCodes.contains("401") || (obapiRequestContext.getMsgInfo().getResource().contains("/register") &&
                !obapiRequestContext.getMsgInfo().getResource().contains("/metadata"))) {
            if (errors.isEmpty() && obapiRequestContext.getContextProperty(GatewayConstants.ERROR_STATUS_PROP) != null
                    && OpenBankingErrorCodes.UNAUTHORIZED_CODE.equals(obapiRequestContext
                    .getContextProperty(GatewayConstants.ERROR_STATUS_PROP))) {
                OpenBankingExecutorError error = new OpenBankingExecutorError(OpenBankingErrorCodes.UNAUTHORIZED_CODE,
                        "invalid_client", "Request failed due to unknown or invalid Client",
                        OpenBankingErrorCodes.UNAUTHORIZED_CODE);
                errors.add(error);
            }
            JSONObject oAuthErrorPayload = getOAuthErrorJSON(errors);
            obapiRequestContext.setModifiedPayload(oAuthErrorPayload.toString());
        } else {
            Map<String, String> encryptedAccountIds = getEncryptedAccountIdMapFromContextProperty(
                    obapiRequestContext.getContextProps().get(IdPermanenceConstants.ENCRYPTED_ID_MAPPING));
            String memberId = obapiRequestContext.getApiRequestInfo().getUsername();
            String appId = obapiRequestContext.getApiRequestInfo().getConsumerKey();
            JsonObject errorPayload = getErrorJson(errors, memberId, appId, encryptedAccountIds);
            obapiRequestContext.setModifiedPayload(errorPayload.toString());
        }
        Map<String, String> addedHeaders = obapiRequestContext.getAddedHeaders();
        addedHeaders.put(GatewayConstants.CONTENT_TYPE_TAG, GatewayConstants.JSON_CONTENT_TYPE);

        // Add x-fapi-interaction-id header if not present.
        if (!isXFapiInteractionIdPresent(obapiRequestContext)) {
            addedHeaders.put(X_FAPI_INTERACTION_ID, UUID.randomUUID().toString());
        }
        obapiRequestContext.setAddedHeaders(addedHeaders);

        int statusCode;
        if (ErrorUtil.isAnyClientErrors(statusCodes)) {
            statusCode = ErrorUtil.getHTTPErrorCode(statusCodes);
        } else {
            if (obapiRequestContext.getContextProperty(GatewayConstants.ERROR_STATUS_PROP) != null) {
                statusCode = Integer.parseInt(obapiRequestContext
                        .getContextProperty(GatewayConstants.ERROR_STATUS_PROP));
            } else {
                statusCode = HttpStatus.SC_INTERNAL_SERVER_ERROR;
            }
        }
        obapiRequestContext.addContextProperty(GatewayConstants.ERROR_STATUS_PROP, String.valueOf(statusCode));

        // Add error data to analytics map
        Map<String, Object> analyticsData = obapiRequestContext.getAnalyticsData();
        analyticsData.put(STATUS_CODE, statusCode);
        analyticsData.put(RESPONSE_PAYLOAD_SIZE, (long) obapiRequestContext.getModifiedPayload().length());
        obapiRequestContext.setAnalyticsData(analyticsData);

        //Remove headers added by the id permanence executor
        obapiRequestContext.getMsgInfo().getHeaders().remove(IdPermanenceConstants.DECRYPTED_SUB_REQUEST_PATH);
    }

    protected void handleResponseError(OBAPIResponseContext obapiResponseContext) {

        if (!obapiResponseContext.isError()) {
            return;
        }
        ArrayList<OpenBankingExecutorError> errors = obapiResponseContext.getErrors();
        HashSet<String> statusCodes = new HashSet<>();

        for (OpenBankingExecutorError error : errors) {
            statusCodes.add(error.getHttpStatusCode());
        }

        if (obapiResponseContext.getMsgInfo().getResource().contains("/register")) {
            JSONObject oAuthErrorPayload = getOAuthErrorJSON(errors);
            obapiResponseContext.setModifiedPayload(oAuthErrorPayload.toString());
        } else {
            Map<String, String> encryptedAccountIds = getEncryptedAccountIdMapFromContextProperty(
                    obapiResponseContext.getContextProps().get(IdPermanenceConstants.ENCRYPTED_ID_MAPPING));
            String memberId = obapiResponseContext.getApiRequestInfo().getUsername();
            String appId = obapiResponseContext.getApiRequestInfo().getConsumerKey();
            JsonObject errorPayload = getErrorJson(errors, memberId, appId, encryptedAccountIds);
            obapiResponseContext.setModifiedPayload(errorPayload.toString());
        }
        Map<String, String> addedHeaders = obapiResponseContext.getAddedHeaders();
        addedHeaders.put(GatewayConstants.CONTENT_TYPE_TAG, GatewayConstants.JSON_CONTENT_TYPE);
        obapiResponseContext.setAddedHeaders(addedHeaders);

        int statusCode;
        if (ErrorUtil.isAnyClientErrors(statusCodes)) {
            statusCode = ErrorUtil.getHTTPErrorCode(statusCodes);
        } else {
            if (obapiResponseContext.getContextProperty(GatewayConstants.ERROR_STATUS_PROP) != null) {
                statusCode = Integer.parseInt(obapiResponseContext
                        .getContextProperty(GatewayConstants.ERROR_STATUS_PROP));
            } else {
                statusCode = HttpStatus.SC_INTERNAL_SERVER_ERROR;
            }
        }
        obapiResponseContext.addContextProperty(GatewayConstants.ERROR_STATUS_PROP, String.valueOf(statusCode));

        // Add error data to analytics map
        Map<String, Object> analyticsData = obapiResponseContext.getAnalyticsData();
        analyticsData.put(STATUS_CODE, statusCode);
        analyticsData.put(RESPONSE_PAYLOAD_SIZE, (long) obapiResponseContext.getModifiedPayload().length());
        obapiResponseContext.setAnalyticsData(analyticsData);

        //Remove headers added by the id permanence executor
        obapiResponseContext.getMsgInfo().getHeaders().remove(IdPermanenceConstants.DECRYPTED_SUB_REQUEST_PATH);
    }

    public static JSONObject getOAuthErrorJSON(ArrayList<OpenBankingExecutorError> errors) {

        JSONObject errorObj = new JSONObject();
        for (OpenBankingExecutorError error : errors) {
            errorObj.put(ErrorConstants.ERROR, error.getTitle());
            errorObj.put(ErrorConstants.ERROR_DESCRIPTION, error.getMessage());
        }
        return errorObj;
    }

    public static JsonObject getErrorJson(ArrayList<OpenBankingExecutorError> errors, String memberId, String appId,
                                          Map<String, String> encryptedAccountIds) {

        JsonArray errorList = new JsonArray();
        JsonObject parentObject = new JsonObject();

        for (OpenBankingExecutorError error : errors) {
            JsonObject errorObj = new JsonObject();
            try {
                Object errorPayload = new JSONParser(JSONParser.MODE_PERMISSIVE).parse(error.getMessage());
                errorObj.addProperty(ErrorConstants.CODE, error.getCode());
                if (errorPayload instanceof JSONObject) {
                    JSONObject errorJSON = (JSONObject) errorPayload;
                    if (ErrorConstants.CONSENT_ENFORCEMENT_ERROR.equals(error.getTitle())) {
                        errorObj.addProperty(ErrorConstants.TITLE, errorJSON.getAsString(ErrorConstants.TITLE));
                        if (errorJSON.get(ErrorConstants.ACCOUNT_ID) != null) {
                            String accountId = errorJSON.getAsString(ErrorConstants.ACCOUNT_ID);
                            String encryptedId;
                            if (encryptedAccountIds != null && encryptedAccountIds.containsKey(accountId)) {
                                encryptedId = encryptedAccountIds.get(accountId);
                            } else {
                                encryptedId = IdPermanenceUtils.encryptAccountIdInErrorResponse(errorJSON,
                                        memberId, appId);
                            }
                            errorObj.addProperty(ErrorConstants.DETAIL, encryptedId);
                        } else {
                            errorObj.addProperty(ErrorConstants.DETAIL, errorJSON.getAsString(ErrorConstants.DETAIL));
                        }
                    } else {
                        errorObj.addProperty(ErrorConstants.TITLE, error.getTitle());
                        errorObj.addProperty(ErrorConstants.DETAIL, errorJSON.getAsString(ErrorConstants.DETAIL));
                    }
                    if (errorJSON.getAsString(ErrorConstants.META_URN) != null) {
                        JsonObject meta = new JsonObject();
                        meta.addProperty(ErrorConstants.URN, errorJSON.getAsString(ErrorConstants.META_URN));
                        errorObj.add(ErrorConstants.META, meta);
                    }
                } else {
                    // TODO: need to capture non JSON errors from accelerator side, error codes starting from 20000
                    String errorTitle = error.getTitle();
                    String errorCode = error.getCode();
                    if (ACCELERATOR_EXPECTED_ERROR.equals(error.getCode())) {
                        errorCode = ErrorConstants.AUErrorEnum.EXPECTED_GENERAL_ERROR.getCode();
                        errorTitle = ErrorConstants.AUErrorEnum.EXPECTED_GENERAL_ERROR.getTitle();
                    }
                    errorObj.addProperty(ErrorConstants.CODE, errorCode);
                    errorObj.addProperty(ErrorConstants.TITLE, errorTitle);
                    errorObj.addProperty(ErrorConstants.DETAIL, errorPayload.toString());
                }
            } catch (ParseException e) {
                log.error("Unexpected error while parsing string", e);
                errorObj.addProperty(ErrorConstants.CODE, ErrorConstants.AUErrorEnum.UNEXPECTED_ERROR.getCode());
                errorObj.addProperty(ErrorConstants.TITLE, ErrorConstants.AUErrorEnum.UNEXPECTED_ERROR.getTitle());
                errorObj.addProperty(ErrorConstants.DETAIL, ErrorConstants.AUErrorEnum.UNEXPECTED_ERROR.getDetail());
            }
            errorList.add(errorObj);
        }
        parentObject.add(ErrorConstants.ERRORS, errorList);
        return parentObject;
    }

    /**
     * Check if x-fapi-interaction-id is present in request headers.
     *
     * @param obapiRequestContext OBAPIRequestContext
     * @return true if x-fapi-interaction-id is present in request headers.
     */
    private boolean isXFapiInteractionIdPresent(OBAPIRequestContext obapiRequestContext) {

        Map<String, String> headers = obapiRequestContext.getMsgInfo().getHeaders();
        Map<String, String> addedHeaders = obapiRequestContext.getAddedHeaders();
        return ((headers != null && headers.containsKey(X_FAPI_INTERACTION_ID)) ||
                (addedHeaders != null && addedHeaders.containsKey(X_FAPI_INTERACTION_ID)));
    }

    /**
     * Get a map of encrypted account ids from the context property string.
     * String format: "encryptedAccountId1:accountId1,encryptedAccountId2:accountId2"
     *
     * @param encryptedAccountIdMappings - Comma separated string containing encrypted account id mappings
     * @return Map of encrypted and decrypted account ids
     */
    private Map<String, String> getEncryptedAccountIdMapFromContextProperty(String encryptedAccountIdMappings) {

        Map<String, String> encryptedAccountIdMap = new HashMap<>();
        if (encryptedAccountIdMappings != null) {
            String[] encryptedAccountIdMappingArray = encryptedAccountIdMappings.split(",");
            for (String encryptedAccountIdMapping : encryptedAccountIdMappingArray) {
                String[] encryptedAccountIdPair = encryptedAccountIdMapping.split(":");
                encryptedAccountIdMap.put(encryptedAccountIdPair[0].replaceAll("^\"|\"$", ""),
                        encryptedAccountIdPair[1].replaceAll("^\"|\"$", ""));
            }
        }
        return encryptedAccountIdMap;
    }
}

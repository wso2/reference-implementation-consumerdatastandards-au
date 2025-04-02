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

package org.wso2.openbanking.cds.gateway.mediators;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.data.publisher.common.constants.DataPublishingConstants;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;
import org.wso2.openbanking.cds.common.data.publisher.CDSDataPublishingService;
import org.wso2.openbanking.cds.common.error.handling.models.CDSErrorMeta;
import org.wso2.openbanking.cds.common.error.handling.util.ErrorConstants;
import org.wso2.openbanking.cds.common.error.handling.util.ErrorUtil;
import org.wso2.openbanking.cds.common.utils.CDSCommonUtils;
import org.wso2.openbanking.cds.gateway.utils.GatewayConstants;

import java.nio.charset.Charset;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * CDS Gateway error mediator.
 * This mediator is used for gateway error mediation and data publishing.
 */
public class GatewayErrorMediator extends AbstractMediator {

    private static final Log log = LogFactory.getLog(GatewayErrorMediator.class);

    @Override
    public boolean mediate(MessageContext messageContext) {

        String restApiName = messageContext.getProperty(GatewayConstants.REST_API_NAME) != null ?
                messageContext.getProperty(GatewayConstants.REST_API_NAME).toString() : null;

        // Publish gateway error data.
        if (Boolean.parseBoolean((String) OpenBankingConfigParser.getInstance().getConfiguration()
                .get(DataPublishingConstants.DATA_PUBLISHING_ENABLED))) {

            if ((messageContext.getProperty(GatewayConstants.ERROR_CODE)) != null) {
                log.debug("Publishing invocation error data from CDS error mediator.");
                // Reporting is exempted for notFound/incorrect API calls
                // as they are not considered part of metrics reporting
                if (!(String.valueOf(HttpStatus.SC_NOT_FOUND).equals(extractStatusCode(messageContext)) &&
                        messageContext.getProperty(APIConstants.API_ELECTED_RESOURCE) == null)) {
                    Map<String, Object> invocationErrorData = getApiInvocationErrorDataToPublish(messageContext);
                    CDSDataPublishingService.getCDSDataPublishingService()
                            .publishApiInvocationData(invocationErrorData);
                }
            }
        }

        // Error handling logic.
        JSONObject errorData;
        String statusCodeString = extractStatusCode(messageContext);

        if ((messageContext.getProperty(GatewayConstants.ERROR_CODE)) != null) {

            int errorCode = (int) messageContext.getProperty(GatewayConstants.ERROR_CODE);
            String errorMessage = (String) messageContext.getProperty(GatewayConstants.ERROR_MSG);
            String errorDetail = (String) messageContext.getProperty(GatewayConstants.ERROR_DETAIL);

            if (Integer.toString(errorCode).startsWith("9008")) {
                errorData = getThrottledOutResponse();
            } else if (Integer.toString(errorCode).startsWith("9")) {
                errorData = getAuthFailureResponse(errorCode, errorMessage);
            } else if (Integer.toString(errorCode).startsWith("4") && StringUtils.isEmpty(errorDetail)) {
                errorData = getResourceFailureResponse(errorCode, errorMessage);
            } else if (Integer.toString(errorCode).startsWith("4") &&
                    errorDetail.contains(GatewayConstants.SCHEMA_FAIL_MSG)) {
                errorData = getRequestSchemaValidationFailureResponse(errorCode, errorDetail);
            } else if (Integer.toString(errorCode).startsWith("5") &&
                    errorDetail.contains(GatewayConstants.SCHEMA_FAIL_MSG)) {
                errorData = getResponseSchemaValidationFailureResponse(errorDetail);
            } else {
                return true;
            }
        } else if (StringUtils.isNotBlank(restApiName) && (restApiName.equals("CDRDynamicClientRegistrationAPI")
                || restApiName.equals("CDRArrangementManagementAPI")
                || restApiName.equals("ConsumerDataStandardsAdminAPI"))) {
            // Assume the errors thrown from the relevant internal webapps of these APIs are
            // already formatted according to the CDS format
            return true;
        } else if (messageContext.getProperty(GatewayConstants.ENDPOINT_ADDRESS) != null &&
                !String.valueOf(HttpStatus.SC_REQUEST_TIMEOUT).equals(statusCodeString)) {
            // Assume the errors coming from the backend are properly formatted.
            return true;
        } else {
            if (StringUtils.isBlank(statusCodeString)) {
                return true;
            }
            int statusCode = Integer.parseInt(statusCodeString);
            ErrorConstants.AUErrorEnum errorEnum;
            if ("406".equals(statusCodeString)) {
                errorEnum = ErrorConstants.AUErrorEnum.INVALID_ACCEPT_HEADER;
            } else if (statusCodeString.startsWith("4")) {
                errorEnum = ErrorConstants.AUErrorEnum.EXPECTED_GENERAL_ERROR;
            } else if (statusCodeString.startsWith("5")) {
                errorEnum = ErrorConstants.AUErrorEnum.UNEXPECTED_ERROR;
            } else {
                return true;
            }
            errorData = getErrorDataObject(errorEnum, statusCode, errorEnum.getDetail());
        }

        // Add x-fapi-interaction-id as a transport header if not exists
        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        Map headers = (Map) axis2MessageContext
                .getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

        if (!headers.containsKey(GatewayConstants.X_FAPI_INTERACTION_ID)) {
            headers.put(GatewayConstants.X_FAPI_INTERACTION_ID, UUID.randomUUID().toString());
        }

        String errorResponse = errorData.get(GatewayConstants.ERROR_RESPONSE).toString();
        int status = (int) errorData.get(GatewayConstants.STATUS_CODE);
        setFaultPayload(messageContext, errorResponse, status);
        return true;
    }

    /**
     * Method to retrieve API Invocation Error Data to publish.
     *
     * @param messageContext message context
     * @return api input stream data map
     */
    private static Map<String, Object> getApiInvocationErrorDataToPublish(MessageContext messageContext) {

        Map<String, Object> requestData = new HashMap<>();

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        Map headers = (Map) axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

        long unixTimestamp = Instant.now().getEpochSecond();
        String electedResource = (String) messageContext.getProperty(APIConstants.API_ELECTED_RESOURCE);

        String userAgent;
        if (messageContext.getProperty(GatewayConstants.CLIENT_USER_AGENT) != null) {
            userAgent = (String) messageContext.getProperty(GatewayConstants.CLIENT_USER_AGENT);
        } else if (headers.get(GatewayConstants.CLIENT_USER_AGENT) != null) {
            userAgent = (String) headers.get(GatewayConstants.CLIENT_USER_AGENT);
        } else {
            userAgent = GatewayConstants.UNKNOWN;
        }

        String restApiContext = (String) messageContext.getProperty(GatewayConstants.REST_API_CONTEXT);
        String customerStatus;
        if (GatewayConstants.INFOSEC_ENDPOINTS.contains(restApiContext) ||
                GatewayConstants.INFOSEC_ENDPOINTS.contains(electedResource)) {
            customerStatus = GatewayConstants.UNDEFINED;
        } else if (headers.get(GatewayConstants.X_FAPI_CUSTOMER_IP_ADDRESS) == null) {
            customerStatus = GatewayConstants.UNATTENDED;
        } else {
            customerStatus = GatewayConstants.CUSTOMER_PRESENT;
        }

        String consumerId;
        if (axis2MessageContext.getProperty(GatewayConstants.USER_NAME) != null) {
            consumerId = (String) axis2MessageContext.getProperty(GatewayConstants.USER_NAME);
        } else {
            consumerId = (String) messageContext.getProperty(GatewayConstants.USER_NAME);
        }
        String clientId;
        if (axis2MessageContext.getProperty(GatewayConstants.CONSUMER_KEY) != null) {
            clientId = (String) axis2MessageContext.getProperty(GatewayConstants.CONSUMER_KEY);
        } else {
            clientId = (String) messageContext.getProperty(GatewayConstants.CONSUMER_KEY);
        }

        String httpMethod;
        if (axis2MessageContext.getProperty(GatewayConstants.HTTP_METHOD) != null) {
            httpMethod = (String) axis2MessageContext.getProperty(GatewayConstants.HTTP_METHOD);
        } else {
            httpMethod = GatewayConstants.UNKNOWN;
        }

        String apiName = (String) axis2MessageContext.getProperty(GatewayConstants.API_NAME);
        // Get api name from SYNAPSE_REST_API if not available in axis2 message context.
        if (apiName == null && messageContext.getProperty(GatewayConstants.SYNAPSE_REST_API) != null) {
            apiName = (messageContext.getProperty(GatewayConstants.SYNAPSE_REST_API).toString())
                    .split(":")[0];
        }

        String apiSpecVersion = null;
        if (messageContext.getProperty(GatewayConstants.API_SPEC_VERSION) != null) {
            apiSpecVersion = (String) messageContext.getProperty(GatewayConstants.API_SPEC_VERSION);
        }

        String statusCodeString = extractStatusCode(messageContext);
        int statusCode = 0;
        if (StringUtils.isNotBlank(statusCodeString)) {
            statusCode = Integer.parseInt(statusCodeString);
        }
        String messageId = (String) messageContext.getProperty(GatewayConstants.CORRELATION_ID);

        String authorizationHeader = (String) headers.get(GatewayConstants.AUTHORIZATION);
        String accessToken = (authorizationHeader != null && authorizationHeader.split(" ").length > 1) ?
                authorizationHeader.split(" ")[1] : null;
        // Encrypt access token if configured.
        if (accessToken != null && OpenBankingCDSConfigParser.getInstance().isTokenEncryptionEnabled()) {
            accessToken = CDSCommonUtils.encryptAccessToken(accessToken);
        }

        // Get error payload size
        long payloadSize = 0;
        SOAPEnvelope env = messageContext.getEnvelope();
        if (env != null) {
            SOAPBody soapbody = env.getBody();
            if (soapbody != null) {
                byte[] size = soapbody.toString().getBytes(Charset.defaultCharset());
                payloadSize = size.length;
            }
        }

        requestData.put("consentId", null);
        requestData.put("consumerId", consumerId);
        requestData.put("clientId", clientId);
        requestData.put("userAgent", userAgent);
        requestData.put("statusCode", statusCode);
        requestData.put("httpMethod", httpMethod);
        requestData.put("responsePayloadSize", payloadSize);
        requestData.put("electedResource", electedResource);
        requestData.put("apiName", apiName);
        requestData.put("apiSpecVersion", apiSpecVersion);
        requestData.put("timestamp", unixTimestamp);
        requestData.put("messageId", messageId);
        requestData.put("customerStatus", customerStatus);
        requestData.put("accessToken", accessToken);

        return requestData;
    }

    /**
     * set the error message to the jsonPayload to be sent back.
     *
     * @param messageContext the messageContext sent back to the user
     * @param errorData      the details of the error for validation failure
     */
    private static void setFaultPayload(MessageContext messageContext, String errorData, int status) {

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext)
                .getAxis2MessageContext();
        axis2MessageContext.setProperty(GatewayConstants.MESSAGE_TYPE, GatewayConstants.JSON_CONTENT_TYPE);
        axis2MessageContext.setProperty(NhttpConstants.HTTP_SC, status);
        try {
            //setting the payload as the message payload
            JsonUtil.getNewJsonPayload(axis2MessageContext, errorData, true,
                    true);
            messageContext.setResponse(true);
            messageContext.setProperty(GatewayConstants.RESPONSE_CAPS, GatewayConstants.TRUE);
            messageContext.setTo(null);
            axis2MessageContext.removeProperty(GatewayConstants.NO_ENTITY_BODY);
        } catch (AxisFault axisFault) {
            log.error(GatewayConstants.PAYLOAD_SETTING_ERROR, axisFault);
        }
    }

    /**
     * Method to get the error response for throttled out requests.
     *
     * @return Throttle Data JSONObject
     */
    private static JSONObject getThrottledOutResponse() {

        JSONObject errorData = new JSONObject();
        JSONArray errorList = new JSONArray();
        String errorResponse;

        errorList.add(ErrorUtil.getErrorObject(ErrorConstants.AUErrorEnum.EXPECTED_GENERAL_ERROR,
                GatewayConstants.THROTTLED_OUT_ERROR,
                new CDSErrorMeta()));

        errorResponse = ErrorUtil.getErrorJson(errorList);
        errorData.put(GatewayConstants.STATUS_CODE, 429);
        errorData.put(GatewayConstants.ERROR_RESPONSE, errorResponse);
        return errorData;
    }

    /**
     * Method to get the error response for auth failures.
     *
     * @param errorCode
     * @param errorMessage
     * @return
     */
    private static JSONObject getAuthFailureResponse(int errorCode, String errorMessage) {

        JSONObject errorData = new JSONObject();
        JSONArray errorList = new JSONArray();
        int status;
        String errorResponse = null;
        ErrorConstants.AUErrorEnum errorEnum = null;

        if (errorCode == GatewayConstants.API_AUTH_GENERAL_ERROR) {
            status = ErrorConstants.AUErrorEnum.UNEXPECTED_ERROR.getHttpCode();
            errorEnum = ErrorConstants.AUErrorEnum.UNEXPECTED_ERROR;
        } else if (errorCode == GatewayConstants.API_AUTH_FORBIDDEN) {
            status = HttpStatus.SC_FORBIDDEN;
            errorEnum = ErrorConstants.AUErrorEnum.EXPECTED_GENERAL_ERROR;
        } else if (errorCode == GatewayConstants.INVALID_SCOPE) {
            status = HttpStatus.SC_FORBIDDEN;
            errorResponse = getOAuthErrorResponse("insufficient_scope", errorMessage);
        } else {
            // This block will be executed for invalid/missing client credentials.
            status = ErrorConstants.AUErrorEnum.CLIENT_AUTH_FAILED.getHttpCode();
            errorResponse = getOAuthErrorResponse("invalid_client", errorMessage);
        }

        if (errorResponse == null && errorEnum != null) {
            errorList.add(ErrorUtil.getErrorObject(errorEnum, errorMessage, new CDSErrorMeta()));
            errorResponse = ErrorUtil.getErrorJson(errorList);
        }
        errorData.put(GatewayConstants.STATUS_CODE, status);
        errorData.put(GatewayConstants.ERROR_RESPONSE, errorResponse);

        return errorData;
    }

    private static JSONObject getResourceFailureResponse(int errorCode, String errorMessage) {

        int status;
        ErrorConstants.AUErrorEnum errorEnum;

        if (errorCode == 404) {
            status = ErrorConstants.AUErrorEnum.RESOURCE_NOT_FOUND.getHttpCode();
            errorEnum = ErrorConstants.AUErrorEnum.RESOURCE_NOT_FOUND;
        } else {
            status = errorCode;
            errorEnum = ErrorConstants.AUErrorEnum.EXPECTED_GENERAL_ERROR;
        }

        return getErrorDataObject(errorEnum, status, errorMessage);
    }

    /**
     * Method to get the error response for schema validation failures.
     *
     * @param errorCode
     * @param errorDetail
     * @return
     */
    private static JSONObject getRequestSchemaValidationFailureResponse(int errorCode, String errorDetail) {

        int status;
        ErrorConstants.AUErrorEnum errorEnum;

        if (errorCode == 400) {
            status = HttpStatus.SC_BAD_REQUEST;
            if (errorDetail.contains(GatewayConstants.CONTENT_TYPE_TAG) ||
                    errorDetail.contains(GatewayConstants.ACCEPT_HEADER)) {
                errorEnum = ErrorConstants.AUErrorEnum.INVALID_HEADER;
            } else if (errorDetail.contains(GatewayConstants.MAXIMUM_PAGE_SIZE_ERROR)
                    || errorDetail.contains(GatewayConstants.MINIMUM_PAGE_SIZE_ERROR)) {
                errorEnum = ErrorConstants.AUErrorEnum.PAGE_OUT_OF_RANGE;
                status = HttpStatus.SC_UNPROCESSABLE_ENTITY;
            } else {
                errorEnum = ErrorConstants.AUErrorEnum.INVALID_FIELD;
            }
        } else {
            status = errorCode;
            errorEnum = ErrorConstants.AUErrorEnum.EXPECTED_GENERAL_ERROR;
        }

        return getErrorDataObject(errorEnum, status, errorDetail);
    }

    /**
     * Method to get the error response for response schema validation failures.
     *
     * @param errorDetail
     * @return
     */
    private static JSONObject getResponseSchemaValidationFailureResponse(String errorDetail) {
        return getErrorDataObject(ErrorConstants.AUErrorEnum.UNEXPECTED_ERROR,
                HttpStatus.SC_INTERNAL_SERVER_ERROR, errorDetail);
    }

    /**
     * Method to get the oauth error response.
     *
     * @param errorCode
     * @param errorMessage
     * @return Error String
     */
    private static String getOAuthErrorResponse(String errorCode, String errorMessage) {

        JSONObject errorObject = new JSONObject();
        errorObject.put(ErrorConstants.ERROR, errorCode);
        errorObject.put(ErrorConstants.ERROR_DESCRIPTION, errorMessage);
        return errorObject.toString();
    }

    private static String extractStatusCode(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        //The status code values may pass as int or String format
        String statusCode = null;
        if (axis2MessageContext.getProperty(GatewayConstants.HTTP_SC) != null) {
            statusCode = String.valueOf(axis2MessageContext.getProperty(GatewayConstants.HTTP_SC));
        } else if (messageContext.getProperty(GatewayConstants.HTTP_RESPONSE_STATUS_CODE) != null) {
            statusCode = String.valueOf(messageContext.getProperty(GatewayConstants
                    .HTTP_RESPONSE_STATUS_CODE));
        } else if (messageContext.getProperty(GatewayConstants.CUSTOM_HTTP_SC) != null) {
            statusCode = String.valueOf(messageContext.getProperty(GatewayConstants.CUSTOM_HTTP_SC));
        }
        return statusCode;
    }

    /**
     * Method to get the common error JSON object.
     *
     * @param errorEnum   - Error Enum
     * @param errorStatus - Error Status
     * @param errorDetail - Error Details
     * @return Error Object
     */
    private static JSONObject getErrorDataObject(ErrorConstants.AUErrorEnum errorEnum, int errorStatus,
                                                 String errorDetail) {
        JSONObject errorData = new JSONObject();
        JSONArray errorList = new JSONArray();
        errorList.add(ErrorUtil.getErrorObject(errorEnum, errorDetail, new CDSErrorMeta()));
        String errorResponse = ErrorUtil.getErrorJson(errorList);
        errorData.put(GatewayConstants.STATUS_CODE, errorStatus);
        errorData.put(GatewayConstants.ERROR_RESPONSE, errorResponse);
        return errorData;
    }
}

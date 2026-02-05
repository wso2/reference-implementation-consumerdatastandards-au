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
package org.wso2.openbanking.consumerdatastandards.au.extensions.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.CdsErrorEnum;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.CommonConstants;
import org.wso2.openbanking.consumerdatastandards.au.extensions.exceptions.AuthorizationFailureException;
import org.wso2.openbanking.consumerdatastandards.au.extensions.exceptions.CdsConsentException;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.ConsumerAndDisplayData;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.PopulateConsentAuthorizeScreenData;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.PopulateConsentAuthorizeScreenRequestBody;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.SuccessResponsePopulateConsentAuthorizeScreen;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.SuccessResponsePopulateConsentAuthorizeScreenData;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.SuccessResponsePopulateConsentAuthorizeScreenDataConsentData;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.SuccessResponsePopulateConsentAuthorizeScreenDataConsumerData;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.SuccessResponsePopulateConsentAuthorizeScreenDataDisplayData;
import org.wso2.openbanking.consumerdatastandards.au.extensions.utils.CommonConsentExtensionUtil;
import org.wso2.openbanking.consumerdatastandards.au.extensions.utils.ConsentAuthorizeUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response;

/**
 * Implementation for Populate Consent Authorize Screen API.
 */
public class PopulateConsentAuthorizeScreenApiImpl {

    private static final Log log = LogFactory.getLog(PopulateConsentAuthorizeScreenApiImpl.class);
    private static final int secondsInYear = (int) TimeUnit.SECONDS.convert(365, TimeUnit.DAYS);

    /**
     * Handle populate consent authorize screen request to initialize the consent flow and load the consent page.
     * @param requestBody - the request body containing necessary parameters for consent screen population
     * @return Response containing the consent and account data for the authorize screen
     */
    public static Response handlePopulateConsentAuthorizeScreen(PopulateConsentAuthorizeScreenRequestBody requestBody) {

        try {
            String requestId = requestBody.getRequestId();
            PopulateConsentAuthorizeScreenData data = requestBody.getData();
            Object requestParams = data.getRequestParameters();
            String userId = requestBody.getData().getUserId();

            //Convert request parameters to JSON object
            JSONObject jsonRequestBody = CommonConsentExtensionUtil.convertObjectToJson(requestParams);

            //Extract required data from request object
            Map<String, Object> requiredData = extractRequiredDataFromRequestObject(jsonRequestBody);

            //CDS consent retrieval step
            SuccessResponsePopulateConsentAuthorizeScreenDataConsentData consentData =
                    ConsentAuthorizeUtil.cdsConsentRetrieval(jsonRequestBody, requiredData);

            //Getting Consumer and Display Data
            ConsumerAndDisplayData consumerAndDisplayData =
                    ConsentAuthorizeUtil.cdsConsumerDataRetrieval(jsonRequestBody, userId);

            //CDS account list retrieval step
            SuccessResponsePopulateConsentAuthorizeScreenDataConsumerData consumerData =
                    consumerAndDisplayData.getConsumerData();

            //CDS Unavailable account list
            SuccessResponsePopulateConsentAuthorizeScreenDataDisplayData displayData =
                    consumerAndDisplayData.getDisplayData();

            //Set consent data to return to accelerator
            SuccessResponsePopulateConsentAuthorizeScreenData screenData =
                    new SuccessResponsePopulateConsentAuthorizeScreenData();
            screenData.setConsentData(consentData);
            screenData.setConsumerData(consumerData);
            screenData.setDisplayData(displayData);

            SuccessResponsePopulateConsentAuthorizeScreen response =
                    new SuccessResponsePopulateConsentAuthorizeScreen();
            response.setResponseId(requestId);
            response.setStatus(SuccessResponsePopulateConsentAuthorizeScreen.StatusEnum.SUCCESS);
            response.setData(screenData);

            return Response.status(Response.Status.OK).entity(new JSONObject(response).toString()).build();

        } catch (CdsConsentException e) {
            // Handle all CDS-related errors (including auto-converted exceptions)
            log.error("CDS error during consent authorize screen population: " + e.getMessage(), e);

            // Convert CdsConsentException to AuthorizationFailureException for consistent response format
            AuthorizationFailureException authException = AuthorizationFailureException.createError(e.getErrorEnum(),
                    e.getErrorDetail(), CommonConstants.REJECTED_STATUS, requestBody.getRequestId());

            return Response.status(Response.Status.OK).entity(authException.toFailedResponseJsonString()).build();

        } catch (JsonProcessingException e) {
            log.error("Unexpected error during consent authorize screen population: " + e.getMessage(), e);

            AuthorizationFailureException authException = AuthorizationFailureException.createError(
                    CdsErrorEnum.UNEXPECTED_ERROR, "Consent authorize screen population failed."
                            + "JSON processing failed: " + e.getMessage(),
                    CommonConstants.REJECTED_STATUS, requestBody.getRequestId());

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(authException.toFailedResponseJsonString()).build();
        }
    }

    /**
     * Extracts required data from the given request object.
     *
     * @param jsonRequestBody The JSON object representing the request payload from which
     *                        required data will be extracted.
     * @return A map containing the extracted data, or error information if extraction fails.
     * @throws CdsConsentException If there is an error while parsing the request object or extracting required data.
     */
    public static Map<String, Object> extractRequiredDataFromRequestObject(JSONObject jsonRequestBody) throws
            CdsConsentException {

        String clientID;
        Map<String, Object> dataMap = new HashMap<>();

        try {
            long sharingDuration = 0;
            clientID = jsonRequestBody.getString(CommonConstants.CLIENT_ID);

            // Validate client_id existence
            if (StringUtils.isBlank(clientID)) {
                log.error("client_id not found in request object");
                dataMap.put(CommonConstants.IS_ERROR, "client_id not found in request object");
                return dataMap;
            }
            dataMap.put(CommonConstants.CLIENT_ID, clientID);

            // Verify claims
            if (jsonRequestBody.has(CommonConstants.CLAIMS)) {
                JSONObject claims = (JSONObject) jsonRequestBody.get(CommonConstants.CLAIMS);
                if (claims.has(CommonConstants.SHARING_DURATION)) {

                    String sharingDurationStr = claims.get(CommonConstants.SHARING_DURATION) == null ?
                            StringUtils.EMPTY : claims.get(CommonConstants.SHARING_DURATION).toString();

                    sharingDuration = sharingDurationStr.isEmpty() ? 0 : Long.parseLong(sharingDurationStr);

                    if (sharingDuration > secondsInYear) {
                        sharingDuration = secondsInYear;
                        if (log.isDebugEnabled()) {
                            log.debug("Requested sharing_duration is greater than a year,therefore one year duration"
                                    + " is set as consent expiration for request object of client: "
                                    + dataMap.get(CommonConstants.CLIENT_ID));
                        }
                    }
                    dataMap.put(CommonConstants.EXPIRATION_DATE_TIME,
                            ConsentAuthorizeUtil.getConsentExpiryDateTime(sharingDuration));
                }
                if (sharingDuration == 0) {
                    if (log.isDebugEnabled()) {
                        log.debug("sharing_duration not found in the request object of client: " + clientID);
                    }
                    dataMap.put(CommonConstants.EXPIRATION_DATE_TIME, 0);
                }

                // adding original sharing_duration_value to data map
                dataMap.put(CommonConstants.SHARING_DURATION_VALUE, sharingDuration);

                // Extracting cdr_arrangement_id, id_token and userinfo claims if exist
                if (claims.has(CommonConstants.CDR_ARRANGEMENT_ID)
                        && claims.get(CommonConstants.CDR_ARRANGEMENT_ID) != null) {
                    dataMap.put(CommonConstants.CDR_ARRANGEMENT_ID,
                            claims.get(CommonConstants.CDR_ARRANGEMENT_ID).toString());
                }

                String idTokenJsonString = claims.has(CommonConstants.ID_TOKEN) ?
                        claims.get(CommonConstants.ID_TOKEN).toString() : null;

                String userInfoJsonString = claims.has(CommonConstants.USERINFO) ?
                        claims.get(CommonConstants.USERINFO).toString() : null;

                JSONParser parser = new JSONParser();
                dataMap.put(CommonConstants.ID_TOKEN_CLAIMS, StringUtils.isNotBlank(idTokenJsonString) ?
                        parser.parse(idTokenJsonString) : new JSONObject());
                dataMap.put(CommonConstants.USERINFO_CLAIMS, StringUtils.isNotBlank(userInfoJsonString) ?
                        parser.parse(userInfoJsonString) : new JSONObject());
            }
        } catch (ParseException e) {
            log.error("Error while parsing the request object", e);
            throw new CdsConsentException(
                    CdsErrorEnum.UNEXPECTED_ERROR, "Error while parsing the request object: " + e);
        }
        return dataMap;
    }
}

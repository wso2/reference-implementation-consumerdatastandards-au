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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.CdsErrorEnum;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.CommonConstants;
import org.wso2.openbanking.consumerdatastandards.au.extensions.exceptions.AuthorizationFailureException;
import org.wso2.openbanking.consumerdatastandards.au.extensions.exceptions.CdsConsentException;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.PopulateConsentAuthorizeScreenData;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.PopulateConsentAuthorizeScreenRequestBody;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.SuccessResponsePopulateConsentAuthorizeScreen;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.SuccessResponsePopulateConsentAuthorizeScreenData;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.SuccessResponsePopulateConsentAuthorizeScreenDataConsentData;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.SuccessResponsePopulateConsentAuthorizeScreenDataConsumerData;
import org.wso2.openbanking.consumerdatastandards.au.extensions.utils.CommonConsentExtensionUtil;
import org.wso2.openbanking.consumerdatastandards.au.extensions.utils.ConsentAuthorizeUtil;

import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * Implementation for Populate Consent Authorize Screen API.
 */
public class PopulateConsentAuthorizeScreenApiImpl {

    private static final Log log = LogFactory.getLog(PopulateConsentAuthorizeScreenApiImpl.class);

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
            Map<String, Object> requiredData =
                    ConsentAuthorizeUtil.extractRequiredDataFromRequestObject(jsonRequestBody);

            //CDS consent retrieval step
            SuccessResponsePopulateConsentAuthorizeScreenDataConsentData consentData =
                    ConsentAuthorizeUtil.cdsConsentRetrieval(jsonRequestBody, requiredData);

            //CDS account list retrieval step
            SuccessResponsePopulateConsentAuthorizeScreenDataConsumerData consumerData =
                    ConsentAuthorizeUtil.cdsConsumerDataRetrieval(jsonRequestBody, userId);

            //Set consent data to return to accelerator
            SuccessResponsePopulateConsentAuthorizeScreenData screenData =
                    new SuccessResponsePopulateConsentAuthorizeScreenData();
            screenData.setConsentData(consentData);
            screenData.setConsumerData(consumerData);

            SuccessResponsePopulateConsentAuthorizeScreen response = new SuccessResponsePopulateConsentAuthorizeScreen();
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

            CdsConsentException cdsException = new CdsConsentException(CdsErrorEnum.UNEXPECTED_ERROR,
                    "Consent authorize screen population failed." + "JSON processing failed: " + e.getMessage());

            AuthorizationFailureException authException = AuthorizationFailureException.createError(
                    cdsException.getErrorEnum(), cdsException.getErrorDetail(),
                    CommonConstants.REJECTED_STATUS, requestBody.getRequestId());

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(authException.toFailedResponseJsonString()).build();
        }
    }
}
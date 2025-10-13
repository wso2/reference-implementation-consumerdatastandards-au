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

package org.wso2.openbanking.consumerdatastandards.au.extensions.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.CommonConstants;
import org.wso2.openbanking.consumerdatastandards.au.extensions.exceptions.CDSConsentException;
import org.wso2.openbanking.consumerdatastandards.au.extensions.exceptions.AuthorizationFailureException;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.CDSErrorEnum;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.ErrorResponse;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.PersistAuthorizedConsentRequestBody;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.PopulateConsentAuthorizeScreenData;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.PopulateConsentAuthorizeScreenRequestBody;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.SuccessResponsePersistAuthorizedConsent;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.SuccessResponsePersistAuthorizedConsentData;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.SuccessResponsePopulateConsentAuthorizeScreen;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.SuccessResponsePopulateConsentAuthorizeScreenData;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.SuccessResponsePopulateConsentAuthorizeScreenDataConsentData;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.SuccessResponsePopulateConsentAuthorizeScreenDataConsumerData;
import org.wso2.openbanking.consumerdatastandards.au.extensions.utils.*;

import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * Handler class for handling CDS specific authorization flow related operations.
 */
public class CDSAuthorizationFlowHandler {

    private static final Log log = LogFactory.getLog(CDSAuthorizationFlowHandler.class);

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
            JSONObject jsonRequestBody = CommonConsentExtensionUtils.convertObjectToJson(requestParams);

            //Extract required data from request object
            Map<String, Object> requiredData =
                    ConsentAuthorizationUtil.extractRequiredDataFromRequestObject(jsonRequestBody);

            //CDS consent retrieval step
            SuccessResponsePopulateConsentAuthorizeScreenDataConsentData consentData =
                    CDSDataRetrievalUtil.CdsConsentRetrieval(jsonRequestBody, requiredData);

            //CDS account list retrieval step
            SuccessResponsePopulateConsentAuthorizeScreenDataConsumerData consumerData =
                    CDSDataRetrievalUtil.CdsConsumerDataRetrieval(jsonRequestBody, requiredData, userId);

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

        } catch (CDSConsentException ex) {
            // Handle all CDS-related errors (including auto-converted exceptions)
            log.error("CDS error during consent authorize screen population: " + ex.getMessage(), ex);

            // Convert CDSConsentException to AuthorizationFailureException for consistent response format
            AuthorizationFailureException authException = AuthorizationFailureException.createError(ex.getErrorEnum(),
                ex.getErrorDetail(), CommonConstants.REJECTED_STATUS, requestBody.getRequestId());

            return Response.status(Response.Status.OK).entity(authException.toFailedResponseJsonString()).build();

        } catch (Exception e) {
            // Convert any other exceptions to CDSConsentException automatically, then handle
            log.error("Unexpected error during consent authorize screen population: " + e.getMessage(), e);

            CDSConsentException cdsException = CDSConsentException.fromThrowable(e,
                "Consent authorize screen population failed");

            AuthorizationFailureException authException = AuthorizationFailureException.createError(
                cdsException.getErrorEnum(), cdsException.getErrorDetail(),
                CommonConstants.REJECTED_STATUS, requestBody.getRequestId()
            );

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(authException.toFailedResponseJsonString()).build();
        }
    }

    /**
     * Handle persist authorized consent request to save the authorized consent and complete the consent flow.
     * @param persistAuthorizedConsentRequestBody - the request body containing the consent data to be persisted
     * @return Response containing the status of the persistence operation
     */
    public Response handlePersistAuthorizedConsent(PersistAuthorizedConsentRequestBody
                                                           persistAuthorizedConsentRequestBody) {

        try {
            // Consent persist step
            SuccessResponsePersistAuthorizedConsentData persistConsentData = CDSConsentAuthPersistUtil
                    .CdsConsentPersist(persistAuthorizedConsentRequestBody);

            SuccessResponsePersistAuthorizedConsent response = new SuccessResponsePersistAuthorizedConsent();
            response.setResponseId(persistAuthorizedConsentRequestBody.getRequestId());
            response.setStatus(SuccessResponsePersistAuthorizedConsent.StatusEnum.SUCCESS);
            response.setData(persistConsentData);

            return Response.status(Response.Status.OK).entity(new JSONObject(response).toString()).build();

        } catch (Exception e) {
            // Convert any other exceptions to CDSConsentException automatically, then handle
            log.error("Unexpected error during consent persistence: " + e.getMessage(), e);

            CDSConsentException cdsException = CDSConsentException.fromThrowable(e, 
                "Consent persistence failed");
            
            AuthorizationFailureException authException = AuthorizationFailureException.createError(
                cdsException.getErrorEnum(), cdsException.getErrorDetail(), 
                CommonConstants.REJECTED_STATUS, persistAuthorizedConsentRequestBody.getRequestId()
            );

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(authException.toFailedResponseJsonString()).build();
        }
    }
}

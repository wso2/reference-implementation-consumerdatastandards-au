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
import org.json.JSONObject;
import org.wso2.openbanking.consumerdatastandards.au.extensions.exceptions.CdsConsentException;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.Response200ForValidateAuthorizationRequest;
import org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model.ValidateAuthorizationRequestBody;
import org.wso2.openbanking.consumerdatastandards.au.extensions.utils.CdsPushedAuthorizeUtil;
import org.wso2.openbanking.consumerdatastandards.au.extensions.utils.CommonConsentExtensionUtil;

import javax.ws.rs.core.Response;

/**
 * Implementation class for Validate Pushed Authorization Request API.
 */
public class ValidateAuthorizationRequestApiImpl {

    /**
     * Handle pushed authorization request validation.
     * @param validateAuthRequestBody
     * @return Response200ForValidateAuthorizationRequest response
     * @throws CdsConsentException
     * @throws JsonProcessingException
     */
    public static Response handlePushedAuthorisationRequest(
            ValidateAuthorizationRequestBody validateAuthRequestBody)
            throws JsonProcessingException {

        String requestId = validateAuthRequestBody.getRequestId();
        Object requestObject = validateAuthRequestBody.getData().getRequestObject();

        Response200ForValidateAuthorizationRequest response = new Response200ForValidateAuthorizationRequest();
        response.setResponseId(requestId);

        // Validate PAR request object and capture the error response
        JSONObject errorData = CdsPushedAuthorizeUtil.validateCdsPushedAuthRequest(requestObject);

        if (errorData != null) {
            response.setStatus(Response200ForValidateAuthorizationRequest.StatusEnum.ERROR);
            response.setErrorCode(400);
            response.setData(errorData.toMap());
        } else {
            response.setStatus(Response200ForValidateAuthorizationRequest.StatusEnum.SUCCESS);
            response.setErrorCode(200);
            response.setData(CommonConsentExtensionUtil.getSuccessResponse(requestId).toMap());
        }

        return Response.ok().entity(response).build();
    }
}

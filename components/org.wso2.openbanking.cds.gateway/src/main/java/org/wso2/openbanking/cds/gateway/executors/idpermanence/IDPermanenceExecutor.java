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
package org.wso2.openbanking.cds.gateway.executors.idpermanence;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.wso2.openbanking.accelerator.gateway.executor.core.OpenBankingGatewayExecutor;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OpenBankingExecutorError;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;
import org.wso2.openbanking.cds.common.error.handling.util.ErrorConstants;
import org.wso2.openbanking.cds.common.utils.CommonConstants;
import org.wso2.openbanking.cds.gateway.executors.idpermanence.model.IdPermanenceValidationResponse;
import org.wso2.openbanking.cds.gateway.executors.idpermanence.utils.IdPermanenceConstants;
import org.wso2.openbanking.cds.gateway.executors.idpermanence.utils.IdPermanenceUtils;
import org.wso2.openbanking.cds.gateway.utils.GatewayConstants;

import java.util.ArrayList;

/**
 * IDPermanenceExecutor class
 * This class contains operations to encrypt and decrypt the cds resource ids in payloads and urls.
 */
public class IDPermanenceExecutor implements OpenBankingGatewayExecutor {

    private static final Log log = LogFactory.getLog(IDPermanenceExecutor.class);
    private static final String SECRET_KEY = OpenBankingCDSConfigParser.getInstance().getIdPermanenceSecretKey();

    @Override
    public void preProcessRequest(OBAPIRequestContext obApiRequestContext) {
    }

    @Override
    public void postProcessRequest(OBAPIRequestContext obApiRequestContext) {

        if (obApiRequestContext.isError()) {
            return;
        }

        log.debug("IDPermanence Engaged.");
        String uriTemplate = obApiRequestContext.getMsgInfo().getElectedResource();
        String requestedUrl = obApiRequestContext.getMsgInfo().getResource();

        // Handle requests with a body
        if (CommonConstants.POST_METHOD.equals(obApiRequestContext.getMsgInfo().getHttpMethod())) {
            Gson gson = new Gson();
            JsonObject payloadJson;
            IdPermanenceValidationResponse idPermanenceValidationResponse;
            String requestBody = obApiRequestContext.getRequestPayload();
            if (requestBody != null && !GatewayConstants.EMPTY_SOAP_BODY.equals(requestBody)) {
                try {
                    payloadJson = gson.fromJson(requestBody, JsonObject.class);
                    idPermanenceValidationResponse =
                            IdPermanenceUtils.unmaskRequestBodyAccountIDs(payloadJson, SECRET_KEY);
                } catch (JsonSyntaxException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Unexpected error while parsing request body", e);
                    }
                    idPermanenceValidationResponse = new IdPermanenceValidationResponse();
                    idPermanenceValidationResponse.setValid(false);
                    idPermanenceValidationResponse.setError(new OpenBankingExecutorError(
                            ErrorConstants.AUErrorEnum.INVALID_FIELD.getCode(),
                            ErrorConstants.AUErrorEnum.INVALID_FIELD.getTitle(),
                            String.format(ErrorConstants.AUErrorEnum.INVALID_FIELD.getDetail(),
                                    IdPermanenceConstants.REQUEST_BODY),
                            String.valueOf(ErrorConstants.AUErrorEnum.INVALID_FIELD.getHttpCode())
                    ));
                }
            } else {
                idPermanenceValidationResponse = new IdPermanenceValidationResponse();
                idPermanenceValidationResponse.setValid(false);
                idPermanenceValidationResponse.setError(new OpenBankingExecutorError(
                        ErrorConstants.AUErrorEnum.FIELD_MISSING.getCode(),
                        ErrorConstants.AUErrorEnum.FIELD_MISSING.getTitle(),
                        String.format(ErrorConstants.AUErrorEnum.FIELD_MISSING.getDetail(),
                                IdPermanenceConstants.REQUEST_BODY),
                        String.valueOf(ErrorConstants.AUErrorEnum.FIELD_MISSING.getHttpCode())));
            }
            if (!idPermanenceValidationResponse.isValid()) {
                handleError(obApiRequestContext, idPermanenceValidationResponse);
                return;
            }
            // set decrypted resource ids to the request body
            obApiRequestContext.setModifiedPayload(gson.toJson(idPermanenceValidationResponse.
                    getDecryptedResourceIds()));
        }

        // handle requests with path params
        if (IdPermanenceConstants.REQUEST_URLS_WITH_PATH_PARAMS.contains(uriTemplate)) {
            JsonObject idSet = IdPermanenceUtils.extractUrlParams(uriTemplate, requestedUrl);
            IdPermanenceValidationResponse idPermanenceValidationResponse =
                    IdPermanenceUtils.unmaskRequestPathIDs(idSet, SECRET_KEY);
            if (!idPermanenceValidationResponse.isValid()) {
                handleError(obApiRequestContext, idPermanenceValidationResponse);
                return;
            }
            // Set decrypted resource ids to uri
            JsonObject decryptedIdSet = idPermanenceValidationResponse.getDecryptedResourceIds();
            String decryptedSubRequestPath = IdPermanenceUtils.processNewUri(
                    uriTemplate, requestedUrl, decryptedIdSet);
            obApiRequestContext.getMsgInfo().setResource(decryptedSubRequestPath);
            obApiRequestContext.getMsgInfo().getHeaders().put(
                    IdPermanenceConstants.DECRYPTED_SUB_REQUEST_PATH, decryptedSubRequestPath);
            obApiRequestContext.getContextProps().put(IdPermanenceConstants.ENCRYPTED_ID_MAPPING, decryptedIdSet.get(
                    IdPermanenceConstants.ACCOUNT_ID) + ":" + idSet.get(IdPermanenceConstants.ACCOUNT_ID));
        }

    }

    @Override
    public void preProcessResponse(OBAPIResponseContext obApiResponseContext) {
    }

    @Override
    public void postProcessResponse(OBAPIResponseContext obApiResponseContext) {

        // execute if success response
        if (obApiResponseContext.getStatusCode() == HttpStatus.SC_OK) {
            String electedResource = obApiResponseContext.getMsgInfo().getElectedResource();
            String memberId = obApiResponseContext.getApiRequestInfo().getUsername();
            String appId = obApiResponseContext.getApiRequestInfo().getConsumerKey();

            // set encrypted resource ids to the response
            JsonObject payloadJson = new JsonParser().parse(
                    obApiResponseContext.getResponsePayload()).getAsJsonObject();
            JsonObject modifiedPayloadJson = IdPermanenceUtils.maskResponseIDs(
                    payloadJson, electedResource, memberId, appId, SECRET_KEY);
            obApiResponseContext.setModifiedPayload(new Gson().toJson(modifiedPayloadJson));
        }
    }

    /**
     * Add errors to the obApiRequestContext.
     *
     * @param obApiRequestContext            - OBAPIRequestContext
     * @param idPermanenceValidationResponse - Validation response
     */
    private void handleError(OBAPIRequestContext obApiRequestContext, IdPermanenceValidationResponse
            idPermanenceValidationResponse) {

        //catch errors and set to context
        ArrayList<OpenBankingExecutorError> executorErrors = obApiRequestContext.getErrors();
        executorErrors.add(idPermanenceValidationResponse.getError());
        obApiRequestContext.setError(true);
        obApiRequestContext.setErrors(executorErrors);
    }
}

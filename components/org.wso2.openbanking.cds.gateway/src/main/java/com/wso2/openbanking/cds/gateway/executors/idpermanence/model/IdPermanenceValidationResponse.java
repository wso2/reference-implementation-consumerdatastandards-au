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
package org.wso2.openbanking.cds.gateway.executors.idpermanence.model;

import com.google.gson.JsonObject;
import com.wso2.openbanking.accelerator.gateway.executor.model.OpenBankingExecutorError;

/**
 * Model representation of a Id Permanence Validation Response.
 */
public class IdPermanenceValidationResponse {

    private boolean isValid;
    private int httpStatus;
    private JsonObject decryptedResourceIds;
    //private JSONArray errors;
    private OpenBankingExecutorError error;

    public IdPermanenceValidationResponse() {
    }

    public IdPermanenceValidationResponse(boolean isValid, JsonObject decryptedResourceIds) {
        this.isValid = isValid;
        this.decryptedResourceIds = decryptedResourceIds;
    }

    public IdPermanenceValidationResponse(boolean isValid, int httpStatus, JsonObject decryptedResourceIds,
                                          OpenBankingExecutorError error) {
        this.isValid = isValid;
        this.httpStatus = httpStatus;
        this.decryptedResourceIds = decryptedResourceIds;
        this.error = error;
    }

    /**
     * Get valid status.
     *
     * @return True is valid, False if invalid
     */
    public boolean isValid() {
        return isValid;
    }

    /**
     * Set valid status.
     *
     * @param valid valid status
     */
    public void setValid(boolean valid) {
        this.isValid = valid;
    }

    /**
     * Get http status code.
     *
     * @return http status code
     */
    public int getHttpStatus() {
        return httpStatus;
    }

    /**
     * Set http status code.
     *
     * @param httpStatus http status code
     */
    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    /**
     * Get decrypted masked resource Ids sent in the request path or body.
     *
     * @return unmasked resource Ids
     */
    public JsonObject getDecryptedResourceIds() {
        return decryptedResourceIds;
    }

    /**
     * Set decrypted masked resource Ids sent in the request path or body as a Json Object.
     *
     * @param decryptedResourceIds unmasked resource Ids
     */
    public void setDecryptedResourceIds(JsonObject decryptedResourceIds) {
        this.decryptedResourceIds = decryptedResourceIds;
    }

    /**
     * Get list of errors.
     *
     * @return array of error objects
     */
    public OpenBankingExecutorError getError() {
        return error;
    }

    /**
     * Set error.
     *
     * @param error error object
     */
    public void setError(OpenBankingExecutorError error) {
        this.error = error;
    }
}

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

package org.wso2.openbanking.consumerdatastandards.au.extensions.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * CDS Error Response according to Consumer Data Standards specification.
 */
public class CdsErrorResponse {

    private static final Log log = LogFactory.getLog(CdsErrorResponse.class);

    @JsonProperty("errors")
    private List<CdsErrorFormat> errors = new ArrayList<>();

    public CdsErrorResponse() {
    }

    public CdsErrorResponse(CdsErrorFormat error) {
        this.errors = new ArrayList<>();
        this.errors.add(error);
    }

    public CdsErrorResponse(List<CdsErrorFormat> errors) {
        this.errors = errors;
    }

    public List<CdsErrorFormat> getErrors() {
        return errors;
    }

    public void setErrors(List<CdsErrorFormat> errors) {
        this.errors = errors;
    }

    public void addError(CdsErrorFormat error) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(error);
    }

    /**
     * Converts the current CdsErrorResponse instance into a JSONObject.
     * This method uses the Jackson ObjectMapper to serialize the object first.
     *
     * @return A JSONObject representation of this object, or an error JSON if serialization fails.
     */
    public JSONObject toJSONObject() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writeValueAsString(this);
            return new JSONObject(jsonString);

        } catch (JsonProcessingException e) {
            log.error("Failed to convert to JSONObject", e);
            return new JSONObject("{\"error\":\"Failed to convert to JSONObject\"}");
        }
    }
}

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

import java.util.ArrayList;
import java.util.List;

/**
 * CDS Error Response according to Consumer Data Standards specification.
 */
public class CDSErrorResponse {

    @JsonProperty("errors")
    private List<CDSErrorFormat> errors = new ArrayList<>();

    public CDSErrorResponse() {
    }

    public CDSErrorResponse(CDSErrorFormat error) {
        this.errors = new ArrayList<>();
        this.errors.add(error);
    }

    public CDSErrorResponse(List<CDSErrorFormat> errors) {
        this.errors = errors;
    }

    public List<CDSErrorFormat> getErrors() {
        return errors;
    }

    public void setErrors(List<CDSErrorFormat> errors) {
        this.errors = errors;
    }

    public void addError(CDSErrorFormat error) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(error);
    }
}

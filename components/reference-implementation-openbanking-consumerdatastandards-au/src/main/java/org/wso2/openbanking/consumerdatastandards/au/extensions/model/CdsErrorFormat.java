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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * CDS Error Format according to Consumer Data Standards specification.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CdsErrorFormat {

    @JsonProperty("code")
    private String code;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("detail")
    private String detail;
    
    @JsonProperty("meta")
    private CdsErrorMeta meta;

    public CdsErrorFormat() {
    }

    public CdsErrorFormat(String code, String title) {
        this.code = code;
        this.title = title;
    }

    public CdsErrorFormat(String code, String title, String detail) {
        this.code = code;
        this.title = title;
        this.detail = detail;
    }

    public CdsErrorFormat(String code, String title, String detail, CdsErrorMeta meta) {
        this.code = code;
        this.title = title;
        this.detail = detail;
        this.meta = meta;
    }

    // Getters and setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public CdsErrorMeta getMeta() {
        return meta;
    }

    public void setMeta(CdsErrorMeta meta) {
        this.meta = meta;
    }

    @Override
    public String toString() {
        return "CdsErrorFormat{" +
                "code='" + code + '\'' +
                ", title='" + title + '\'' +
                ", detail='" + detail + '\'' +
                ", meta=" + meta +
                '}';
    }

    /**
     * Simple builder for CdsErrorFormat when needed.
     */
    public static class Builder {
        private String code;
        private String title;
        private String detail;
        private CdsErrorMeta meta;

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder detail(String detail) {
            this.detail = detail;
            return this;
        }

        public Builder meta(CdsErrorMeta meta) {
            this.meta = meta;
            return this;
        }

        public CdsErrorFormat build() {
            return new CdsErrorFormat(code, title, detail, meta);
        }
    }
}

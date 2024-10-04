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

package org.wso2.openbanking.cds.common.error.handling.models;

/**
 * Builder class for building error in CDS.
 */
public class CDSError {

    private String code;
    private String title;
    private String detail;
    private CDSErrorMeta meta;

    public CDSError(Builder builder) {
        this.code = builder.code;
        this.title = builder.title;
        this.detail = builder.detail;
        this.meta = builder.meta;
    }

    public String getCode() {

        return code;
    }

    public String getTitle() {

        return title;
    }

    public String getDetail() {

        return detail;
    }

    public CDSErrorMeta getMeta() {

        return meta;
    }

    @Override
    public String toString() {
        return "AUErrorBuilder: code : " + this.code + ", title : " + this.title + ", detail : " + this.detail +
                ", meta :" + this.meta;
    }

    /**
     * Object builder class for AU Error.
     */
    public static final class Builder {

        private String code;
        private String title;
        private String detail;
        private CDSErrorMeta meta;

        public Builder withCode(String code) {
            this.code = code;
            return this;
        }

        public Builder withTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder withDetail(String detail) {
            this.detail = detail;
            return this;
        }

        public Builder withMeta(CDSErrorMeta meta) {
            this.meta = meta;
            return this;
        }

        //Return the finally constructed Error object
        public CDSError build() {
            return new CDSError(this);
        }
    }
}

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

package org.wso2.openbanking.cds.account.type.management.endpoint.model;

/**
 * ENUM of Error response Status.
 */
public enum ErrorStatusEnum {
    BAD_REQUEST("bad_request"),
    INTERNAL_SERVER_ERROR("internal_server_error"),
    INVALID_REQUEST("invalid_request"),
    RESOURCE_NOT_FOUND("resource_not_found");

    private final String value;

    ErrorStatusEnum(String value) {
       this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ErrorStatusEnum fromValue(String text) {

        for (ErrorStatusEnum b : ErrorStatusEnum.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }

    public String toString() {

        return String.valueOf(value);
    }
}

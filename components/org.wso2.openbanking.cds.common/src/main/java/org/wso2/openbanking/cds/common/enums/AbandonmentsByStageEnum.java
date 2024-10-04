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

package org.wso2.openbanking.cds.common.enums;

/**
 * Specifies the type of abandonment by stage for AU.
 */
public enum AbandonmentsByStageEnum {

    PRE_IDENTIFICATION("preIdentification"),
    PRE_AUTHENTICATION("preAuthentication"),
    PRE_ACCOUNT_SELECTION("preAccountSelection"),
    PRE_AUTHORISATION("preAuthorisation"),
    REJECTED("rejected"),
    FAILED_TOKEN_EXCHANGE("failedTokenExchange");

    private String value;

    AbandonmentsByStageEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

}

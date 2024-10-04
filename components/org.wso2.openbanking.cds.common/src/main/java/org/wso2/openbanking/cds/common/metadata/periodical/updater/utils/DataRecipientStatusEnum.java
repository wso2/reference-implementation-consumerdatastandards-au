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

package org.wso2.openbanking.cds.common.metadata.periodical.updater.utils;

/**
 * ENUM of Data Recipient and Software Product statuses.
 */
public enum DataRecipientStatusEnum {

    ACTIVE("Active"),

    SUSPENDED("Suspended"),

    REVOKED("Revoked"),

    SURRENDEED("Surrendered");

    private String value;

    DataRecipientStatusEnum(String value) {

        this.value = value;
    }

    public static DataRecipientStatusEnum fromValue(String text) {

        for (DataRecipientStatusEnum b : DataRecipientStatusEnum.values()) {
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

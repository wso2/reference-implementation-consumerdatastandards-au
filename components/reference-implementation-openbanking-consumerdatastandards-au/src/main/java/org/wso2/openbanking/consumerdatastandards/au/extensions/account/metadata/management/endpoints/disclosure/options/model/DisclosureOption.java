/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.openbanking.consumerdatastandards.au.extensions.account.metadata.management.endpoints.disclosure.options.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.CommonConstants;

public enum DisclosureOption {

    NO_SHARING(CommonConstants.DOMS_STATUS_NO_SHARING),
    PRE_APPROVAL(CommonConstants.DOMS_STATUS_PRE_APPROVAL);

    private final String value;

    DisclosureOption(String value) {
        this.value = value;
    }

    // Serialize enum → JSON
    @JsonValue
    public String getValue() {
        return value;
    }

    // Deserialize JSON → enum
    @JsonCreator
    public static DisclosureOption fromValue(String value) {
        for (DisclosureOption option : DisclosureOption.values()) {
            // compare JSON value to enum value
            if (option.value.equalsIgnoreCase(value)) {
                return option;
            }
        }
        throw new IllegalArgumentException("Invalid disclosure option: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}

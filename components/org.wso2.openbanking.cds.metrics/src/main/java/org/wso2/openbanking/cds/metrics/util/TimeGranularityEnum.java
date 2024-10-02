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

package org.wso2.openbanking.cds.metrics.util;

/**
 * Enum for time formats.
 * Used
 */
public enum TimeGranularityEnum {

    SECONDS("seconds"),
    MINUTES("minutes"),
    HOURS("hours"),
    DAYS("days"),
    MONTHS("months"),
    YEARS("years");

    private final String text;

    TimeGranularityEnum(String value) {
        this.text = value;
    }

    public static TimeGranularityEnum fromString(String text) {

        for (TimeGranularityEnum durationEnum : TimeGranularityEnum.values()) {
            if (durationEnum.text.equalsIgnoreCase(text)) {
                return durationEnum;
            }
        }
        return null;
    }

    public String toString() {
        return String.valueOf(text);
    }

}

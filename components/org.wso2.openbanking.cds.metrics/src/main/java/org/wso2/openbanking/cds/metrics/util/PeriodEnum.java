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
 * Enum for CDS defined periods.
 */
public enum PeriodEnum {

    CURRENT("current"),
    HISTORIC("historic"),
    ALL("all");

    private String text;

    PeriodEnum(String value) {

        this.text = value;
    }

    public static PeriodEnum fromString(String text) {
        for (PeriodEnum b : PeriodEnum.values()) {
            if (b.text.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return ALL; //default value
    }

    public String toString() {

        return String.valueOf(text);
    }

    /**
     * Check if the given period is valid enum value
     * Valid values : CURRENT, HISTORIC, ALL
     * As defined in the specification values MUST be in all caps.
     *
     * @param period - Period to be checked
     * @return - true if the period is valid, false otherwise
     */
    public static boolean isValidPeriodEnum(String period) {
        for (PeriodEnum periodEnum : PeriodEnum.values()) {
            if (periodEnum.name().equals(period)) {
                return true;
            }
        }
        return false;
    }

}

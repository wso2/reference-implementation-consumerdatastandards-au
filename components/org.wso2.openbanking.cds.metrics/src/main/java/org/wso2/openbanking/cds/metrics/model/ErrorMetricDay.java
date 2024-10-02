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

package org.wso2.openbanking.cds.metrics.model;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the error metrics for a specific day.
 * This class maintains a map of authenticated and unauthenticated
 * error invocations for a given date, facilitating easy calculation.
 * <p>
 * Eg:
 * For the date 2024-01-01,
 * There are 5 authenticated errors
 * There are 4 unauthenticated errors
 */
public class ErrorMetricDay {

    private LocalDate date;
    private Map<String, Integer> authenticatedErrorMap;
    private Map<String, Integer> unauthenticatedErrorMap;

    public ErrorMetricDay() {
        this.authenticatedErrorMap = new HashMap<>();
        this.unauthenticatedErrorMap = new HashMap<>();
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Map<String, Integer> getAuthenticatedErrorMap() {
        return authenticatedErrorMap;
    }

    public void setAuthenticatedErrorMap(Map<String, Integer> authenticatedErrorMap) {
        this.authenticatedErrorMap = authenticatedErrorMap;
    }

    public Map<String, Integer> getUnauthenticatedErrorMap() {
        return unauthenticatedErrorMap;
    }

    public void setUnauthenticatedErrorMap(Map<String, Integer> unauthenticatedErrorMap) {
        this.unauthenticatedErrorMap = unauthenticatedErrorMap;
    }
}

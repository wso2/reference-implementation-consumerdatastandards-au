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

/**
 * Represents the authorisation metrics for a specific day.
 * This class maintains all the different types of authorisations
 * for a given date, facilitating easy calculation.
 * <p>
 * Eg:
 * For the date 2024-01-01,
 * There are 5 new authorisations
 * There are 4 revocations
 * There are 5 amendments
 * There are 2 expirations
 */
public class AuthorisationMetricDay {

    private LocalDate date;
    private AuthorisationMetric newAuthorisationMetric;
    private AuthorisationMetric revokedAuthorisationMetric;
    private AuthorisationMetric amendedAuthorisationMetric;
    private AuthorisationMetric expiredAuthorisationMetric;

    public AuthorisationMetricDay() {
        this.newAuthorisationMetric = new AuthorisationMetric();
        this.revokedAuthorisationMetric = new AuthorisationMetric();
        this.amendedAuthorisationMetric = new AuthorisationMetric();
        this.expiredAuthorisationMetric = new AuthorisationMetric();
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public AuthorisationMetric getNewAuthorisationMetric() {
        return newAuthorisationMetric;
    }

    public void setNewAuthorisationMetric(AuthorisationMetric newAuthorisationMetric) {
        this.newAuthorisationMetric = newAuthorisationMetric;
    }

    public AuthorisationMetric getRevokedAuthorisationMetric() {
        return revokedAuthorisationMetric;
    }

    public void setRevokedAuthorisationMetric(AuthorisationMetric revokedAuthorisationMetric) {
        this.revokedAuthorisationMetric = revokedAuthorisationMetric;
    }

    public AuthorisationMetric getAmendedAuthorisationMetric() {
        return amendedAuthorisationMetric;
    }

    public void setAmendedAuthorisationMetric(AuthorisationMetric amendedAuthorisationMetric) {
        this.amendedAuthorisationMetric = amendedAuthorisationMetric;
    }

    public AuthorisationMetric getExpiredAuthorisationMetric() {
        return expiredAuthorisationMetric;
    }

    public void setExpiredAuthorisationMetric(AuthorisationMetric expiredAuthorisationMetric) {
        this.expiredAuthorisationMetric = expiredAuthorisationMetric;
    }
}

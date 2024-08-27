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

/**
 * Model class for AuthorisationMetric.
 */
public class AuthorisationMetric {

    // Short-lived consents(consents with sharing duration less than 24 hours)
    private CustomerTypeCount onceOff;

    // Long-lived consents(consents with sharing duration more than 24 hours)
    private CustomerTypeCount ongoing;

    public AuthorisationMetric() {
        this.onceOff = new CustomerTypeCount();
        this.ongoing = new CustomerTypeCount();
    }

    public CustomerTypeCount getOnceOff() {
        return onceOff;
    }

    public void setOnceOff(CustomerTypeCount onceOff) {
        this.onceOff = onceOff;
    }

    public CustomerTypeCount getOngoing() {
        return ongoing;
    }

    public void setOngoing(CustomerTypeCount ongoing) {
        this.ongoing = ongoing;
    }
}

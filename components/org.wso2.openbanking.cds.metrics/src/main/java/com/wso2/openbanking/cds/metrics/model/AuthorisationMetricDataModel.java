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

import net.minidev.json.JSONArray;
import org.wso2.openbanking.cds.common.enums.AuthorisationFlowTypeEnum;
import org.wso2.openbanking.cds.common.enums.ConsentDurationTypeEnum;
import org.wso2.openbanking.cds.common.enums.ConsentStatusEnum;

/**
 * Model class for AuthorisationMetricDataModel.
 */
public class AuthorisationMetricDataModel {

    private long timestamp;
    private ConsentStatusEnum consentStatus;
    private AuthorisationFlowTypeEnum authFlowType;
    private String customerProfile;
    private ConsentDurationTypeEnum consentDurationType;
    private int count;

    public AuthorisationMetricDataModel() {
    }

    public AuthorisationMetricDataModel(JSONArray jsonArray) {
        this.timestamp = (long) jsonArray.get(0);
        this.consentStatus = ConsentStatusEnum.fromValue((String) jsonArray.get(1));
        this.authFlowType = AuthorisationFlowTypeEnum.fromValue((String) jsonArray.get(2));
        this.customerProfile = (String) jsonArray.get(3);
        this.consentDurationType = ConsentDurationTypeEnum.fromValue((String) jsonArray.get(4));
        this.count = (jsonArray.get(5) != null) ? (Integer) jsonArray.get(5) : 0;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public ConsentStatusEnum getConsentStatus() {
        return consentStatus;
    }

    public void setConsentStatus(ConsentStatusEnum consentStatus) {
        this.consentStatus = consentStatus;
    }

    public AuthorisationFlowTypeEnum getAuthFlowType() {
        return authFlowType;
    }

    public void setAuthFlowType(AuthorisationFlowTypeEnum authFlowType) {
        this.authFlowType = authFlowType;
    }

    public String getCustomerProfile() {
        return customerProfile;
    }

    public void setCustomerProfile(String customerProfile) {
        this.customerProfile = customerProfile;
    }

    public ConsentDurationTypeEnum getConsentDurationType() {
        return consentDurationType;
    }

    public void setConsentDurationType(ConsentDurationTypeEnum consentDurationType) {
        this.consentDurationType = consentDurationType;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}

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
import org.wso2.openbanking.cds.common.enums.AuthorisationStageEnum;

/**
 * Model class for AbandonedConsentFlowMetricDataModel.
 */
public class AbandonedConsentFlowMetricDataModel {

    private String requestUriKey;
    private AuthorisationStageEnum stage;
    private long timestamp;

    public AbandonedConsentFlowMetricDataModel() {
    }

    public AbandonedConsentFlowMetricDataModel(JSONArray jsonArray) {
        this.requestUriKey = (String) jsonArray.get(0);
        this.stage = AuthorisationStageEnum.fromValue((String) jsonArray.get(1));
        this.timestamp = (long) jsonArray.get(2);
    }

    public String getRequestUriKey() {
        return requestUriKey;
    }

    public void setRequestUriKey(String requestUriKey) {
        this.requestUriKey = requestUriKey;
    }

    public AuthorisationStageEnum getStage() {
        return stage;
    }

    public void setStage(AuthorisationStageEnum stage) {
        this.stage = stage;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

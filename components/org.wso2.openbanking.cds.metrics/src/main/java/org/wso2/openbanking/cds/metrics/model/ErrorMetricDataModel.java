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
import org.wso2.openbanking.cds.metrics.util.AspectEnum;

/**
 * Model class for ErrorMetricDataModel.
 */
public class ErrorMetricDataModel {

    private long timestamp;
    private String statusCode;
    private AspectEnum aspect;
    private int count;

    public ErrorMetricDataModel() {
    }

    public ErrorMetricDataModel(JSONArray jsonArray) {
        this.timestamp = (long) jsonArray.get(0);
        this.statusCode = jsonArray.get(1).toString();
        this.aspect = AspectEnum.fromValue((String) jsonArray.get(2));
        this.count = (int) jsonArray.get(3);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public AspectEnum getAspect() {
        return aspect;
    }

    public void setAspect(AspectEnum aspect) {
        this.aspect = aspect;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}

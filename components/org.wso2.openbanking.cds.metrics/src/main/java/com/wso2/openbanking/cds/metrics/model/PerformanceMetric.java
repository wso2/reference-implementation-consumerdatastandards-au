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
 * Model class for PerformanceMetric.
 */
public class PerformanceMetric {

    private String priorityTier;
    private long timestamp;
    private double performanceValue;

    public String getPriorityTier() {
        return priorityTier;
    }

    public void setPriorityTier(String priorityTier) {
        this.priorityTier = priorityTier;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getPerformanceValue() {
        return performanceValue;
    }

    public void setPerformanceValue(double performanceValue) {
        this.performanceValue = performanceValue;
    }
}

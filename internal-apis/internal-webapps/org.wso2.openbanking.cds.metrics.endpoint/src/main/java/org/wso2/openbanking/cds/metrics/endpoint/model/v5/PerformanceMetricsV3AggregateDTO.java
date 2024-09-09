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

package org.wso2.openbanking.cds.metrics.endpoint.model.v5;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * Percentage of calls within Primary Data Holder performance thresholds. Note that Secondary Data Holder performance
 * <b>MUST</b> be excluded from this metric.
 **/
@ApiModel(description = "Percentage of calls within Primary Data Holder performance thresholds. Note that Secondary " +
        "Data Holder performance <b>MUST</b> be excluded from this metric.")
public class PerformanceMetricsV3AggregateDTO {

    @ApiModelProperty(value = "Percentage of calls within the performance threshold for the current day. 0.0 means 0%" +
            ". 1.0 means 100%. Must be a positive value or zero")
    /**
     * Percentage of calls within the performance threshold for the current day. 0.0 means 0%. 1.0 means 100%. Must
     * be a positive value or zero
     **/
    private String currentDay;

    @ApiModelProperty(value = "Percentage of calls within the performance threshold for previous days. The first " +
            "element indicates yesterday and so on. A maximum of seven entries is required if available. 0.0 means 0%" +
            ". 1.0 means 100%. Values must be a positive or zero")
    /**
     * Percentage of calls within the performance threshold for previous days. The first element indicates yesterday
     * and so on. A maximum of seven entries is required if available. 0.0 means 0%. 1.0 means 100%. Values must be a
     * positive or zero
     **/
    private List<String> previousDays = null;

    /**
     * Percentage of calls within the performance threshold for the current day. 0.0 means 0%. 1.0 means 100%. Must
     * be a positive value or zero
     *
     * @return currentDay
     **/
    @JsonProperty("currentDay")
    public String getCurrentDay() {
        return currentDay;
    }

    public void setCurrentDay(String currentDay) {
        this.currentDay = currentDay;
    }

    public PerformanceMetricsV3AggregateDTO currentDay(String currentDay) {
        this.currentDay = currentDay;
        return this;
    }

    /**
     * Percentage of calls within the performance threshold for previous days. The first element indicates yesterday
     * and so on. A maximum of seven entries is required if available. 0.0 means 0%. 1.0 means 100%. Values must be a
     * positive or zero
     *
     * @return previousDays
     **/
    @JsonProperty("previousDays")
    public List<String> getPreviousDays() {
        return previousDays;
    }

    public void setPreviousDays(List<String> previousDays) {
        this.previousDays = previousDays;
    }

    public PerformanceMetricsV3AggregateDTO previousDays(List<String> previousDays) {
        this.previousDays = previousDays;
        return this;
    }

    public PerformanceMetricsV3AggregateDTO addPreviousDaysItem(String previousDaysItem) {
        this.previousDays.add(previousDaysItem);
        return this;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PerformanceMetricsV3AggregateDTO {\n");

        sb.append("    currentDay: ").append(toIndentedString(currentDay)).append("\n");
        sb.append("    previousDays: ").append(toIndentedString(previousDays)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private static String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}


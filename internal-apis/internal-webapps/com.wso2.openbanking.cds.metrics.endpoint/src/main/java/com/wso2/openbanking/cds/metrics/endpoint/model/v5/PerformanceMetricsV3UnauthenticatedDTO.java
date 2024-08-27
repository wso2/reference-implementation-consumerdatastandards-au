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

import javax.validation.Valid;

/**
 * Percentage of unauthenticated calls within the performance thresholds
 **/
@ApiModel(description = "Percentage of unauthenticated calls within the performance thresholds")
public class PerformanceMetricsV3UnauthenticatedDTO {

    @ApiModelProperty(value = "Array of contiguous hourly metrics for the current day.  Each element represents a 1 " +
            "hour period starting from 12am-1am.  Timezone for determining 12am must be consistent but is at the " +
            "discretion of the Data Holder")
    /**
     * Array of contiguous hourly metrics for the current day.  Each element represents a 1 hour period starting from
     * 12am-1am.  Timezone for determining 12am must be consistent but is at the discretion of the Data Holder
     **/
    private List<String> currentDay = null;

    @ApiModelProperty(value = "Percentage of calls within the performance threshold for previous days. The first " +
            "element indicates yesterday and so on. A maximum of seven entries is required if available. 0.0 means 0%" +
            ". 1.0 means 100%. Values must be a positive or zero")
    @Valid
    /**
     * Percentage of calls within the performance threshold for previous days. The first element indicates yesterday
     * and so on. A maximum of seven entries is required if available. 0.0 means 0%. 1.0 means 100%. Values must be a
     * positive or zero
     **/
    private List<List<String>> previousDays = null;

    /**
     * Array of contiguous hourly metrics for the current day.  Each element represents a 1 hour period starting from
     * 12am-1am.  Timezone for determining 12am must be consistent but is at the discretion of the Data Holder
     *
     * @return currentDay
     **/
    @JsonProperty("currentDay")
    public List<String> getCurrentDay() {
        return currentDay;
    }

    public void setCurrentDay(List<String> currentDay) {
        this.currentDay = currentDay;
    }

    public PerformanceMetricsV3UnauthenticatedDTO currentDay(List<String> currentDay) {
        this.currentDay = currentDay;
        return this;
    }

    public PerformanceMetricsV3UnauthenticatedDTO addCurrentDayItem(String currentDayItem) {
        this.currentDay.add(currentDayItem);
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
    public List<List<String>> getPreviousDays() {
        return previousDays;
    }

    public void setPreviousDays(List<List<String>> previousDays) {
        this.previousDays = previousDays;
    }

    public PerformanceMetricsV3UnauthenticatedDTO previousDays(List<List<String>> previousDays) {
        this.previousDays = previousDays;
        return this;
    }

    public PerformanceMetricsV3UnauthenticatedDTO addPreviousDaysItem(List<String> previousDaysItem) {
        this.previousDays.add(previousDaysItem);
        return this;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PerformanceMetricsV3UnauthenticatedDTO {\n");

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


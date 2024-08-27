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
import java.util.Map;

import javax.validation.Valid;

/**
 * Number of calls resulting in error due to server execution over time for authenticated endpoints
 **/
@ApiModel(description = "Number of calls resulting in error due to server execution over time for authenticated " +
        "endpoints")
public class ErrorMetricsV2AuthenticatedDTO {

    @ApiModelProperty(value = "Error counts, by HTTP error code, for current day")
    /**
     * Error counts, by HTTP error code, for current day
     **/
    private Map<String, Integer> currentDay = null;

    @ApiModelProperty(value = "Error counts, by HTTP error code, for previous days. The first element indicates " +
            "yesterday and so on. A maximum of seven entries is required if available")
    @Valid
    /**
     * Error counts, by HTTP error code, for previous days. The first element indicates yesterday and so on. A
     * maximum of seven entries is required if available
     **/
    private List<Map<String, Integer>> previousDays = null;

    /**
     * Error counts, by HTTP error code, for current day
     *
     * @return currentDay
     **/
    @JsonProperty("currentDay")
    public Map<String, Integer> getCurrentDay() {
        return currentDay;
    }

    public void setCurrentDay(Map<String, Integer> currentDay) {
        this.currentDay = currentDay;
    }

    public ErrorMetricsV2AuthenticatedDTO currentDay(Map<String, Integer> currentDay) {
        this.currentDay = currentDay;
        return this;
    }

    public ErrorMetricsV2AuthenticatedDTO putCurrentDayItem(String key, Integer currentDayItem) {
        this.currentDay.put(key, currentDayItem);
        return this;
    }

    /**
     * Error counts, by HTTP error code, for previous days. The first element indicates yesterday and so on. A
     * maximum of seven entries is required if available
     *
     * @return previousDays
     **/
    @JsonProperty("previousDays")
    public List<Map<String, Integer>> getPreviousDays() {
        return previousDays;
    }

    public void setPreviousDays(List<Map<String, Integer>> previousDays) {
        this.previousDays = previousDays;
    }

    public ErrorMetricsV2AuthenticatedDTO previousDays(List<Map<String, Integer>> previousDays) {
        this.previousDays = previousDays;
        return this;
    }

    public ErrorMetricsV2AuthenticatedDTO addPreviousDaysItem(Map<String, Integer> previousDaysItem) {
        this.previousDays.add(previousDaysItem);
        return this;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ErrorMetricsV2AuthenticatedDTO {\n");

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


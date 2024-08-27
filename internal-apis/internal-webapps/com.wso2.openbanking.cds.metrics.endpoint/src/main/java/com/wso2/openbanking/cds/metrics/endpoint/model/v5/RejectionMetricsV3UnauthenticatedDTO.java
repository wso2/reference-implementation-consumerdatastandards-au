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
 * Rejection counts for all unauthenticated end points
 **/
@ApiModel(description = "Rejection counts for all unauthenticated end points")
public class RejectionMetricsV3UnauthenticatedDTO {

    @ApiModelProperty(value = "Number of calls rejected for current day")
    /**
     * Number of calls rejected for current day
     **/
    private Integer currentDay;

    @ApiModelProperty(value = "Number of calls rejected for previous days. The first element indicates yesterday and " +
            "so on. A maximum of seven entries is required if available.")
    /**
     * Number of calls rejected for previous days. The first element indicates yesterday and so on. A maximum of
     * seven entries is required if available.
     **/
    private List<Integer> previousDays = null;

    /**
     * Number of calls rejected for current day
     *
     * @return currentDay
     **/
    @JsonProperty("currentDay")
    public Integer getCurrentDay() {
        return currentDay;
    }

    public void setCurrentDay(Integer currentDay) {
        this.currentDay = currentDay;
    }

    public RejectionMetricsV3UnauthenticatedDTO currentDay(Integer currentDay) {
        this.currentDay = currentDay;
        return this;
    }

    /**
     * Number of calls rejected for previous days. The first element indicates yesterday and so on. A maximum of
     * seven entries is required if available.
     *
     * @return previousDays
     **/
    @JsonProperty("previousDays")
    public List<Integer> getPreviousDays() {
        return previousDays;
    }

    public void setPreviousDays(List<Integer> previousDays) {
        this.previousDays = previousDays;
    }

    public RejectionMetricsV3UnauthenticatedDTO previousDays(List<Integer> previousDays) {
        this.previousDays = previousDays;
        return this;
    }

    public RejectionMetricsV3UnauthenticatedDTO addPreviousDaysItem(Integer previousDaysItem) {
        this.previousDays.add(previousDaysItem);
        return this;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class RejectionMetricsV3UnauthenticatedDTO {\n");

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


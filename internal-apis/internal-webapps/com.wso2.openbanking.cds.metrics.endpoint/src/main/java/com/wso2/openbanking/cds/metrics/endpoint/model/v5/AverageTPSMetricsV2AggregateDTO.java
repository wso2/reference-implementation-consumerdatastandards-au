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

import java.math.BigDecimal;
import java.util.List;

import javax.validation.Valid;

/**
 * Aggregate average transactions per second over time for all endpoints
 **/
@ApiModel(description = "Aggregate average transactions per second over time for all endpoints")
public class AverageTPSMetricsV2AggregateDTO {

    @ApiModelProperty(value = "Average TPS for current day. Must be a positive value or zero")
    @Valid
    /**
     * Average TPS for current day. Must be a positive value or zero
     **/
    private BigDecimal currentDay;

    @ApiModelProperty(value = "Average TPS for previous days. The first element indicates yesterday and so on. A " +
            "maximum of seven entries is required if available. Values must be a positive or zero")
    @Valid
    /**
     * Average TPS for previous days. The first element indicates yesterday and so on. A maximum of seven entries is
     * required if available. Values must be a positive or zero
     **/
    private List<BigDecimal> previousDays = null;

    /**
     * Average TPS for current day. Must be a positive value or zero
     *
     * @return currentDay
     **/
    @JsonProperty("currentDay")
    public BigDecimal getCurrentDay() {
        return currentDay;
    }

    public void setCurrentDay(BigDecimal currentDay) {
        this.currentDay = currentDay;
    }

    public AverageTPSMetricsV2AggregateDTO currentDay(BigDecimal currentDay) {
        this.currentDay = currentDay;
        return this;
    }

    /**
     * Average TPS for previous days. The first element indicates yesterday and so on. A maximum of seven entries is
     * required if available. Values must be a positive or zero
     *
     * @return previousDays
     **/
    @JsonProperty("previousDays")
    public List<BigDecimal> getPreviousDays() {
        return previousDays;
    }

    public void setPreviousDays(List<BigDecimal> previousDays) {
        this.previousDays = previousDays;
    }

    public AverageTPSMetricsV2AggregateDTO previousDays(List<BigDecimal> previousDays) {
        this.previousDays = previousDays;
        return this;
    }

    public AverageTPSMetricsV2AggregateDTO addPreviousDaysItem(BigDecimal previousDaysItem) {
        this.previousDays.add(previousDaysItem);
        return this;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AverageTPSMetricsV2AggregateDTO {\n");

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


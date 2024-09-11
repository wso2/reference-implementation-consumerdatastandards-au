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
 * The number of amended ongoing authorisations
 **/
@ApiModel(description = "The number of amended ongoing authorisations")
public class AuthorisationMetricsV2AmendedAuthorisationCountDTO {

    @ApiModelProperty(value = "")
    @Valid
    private AuthorisationMetricsV2AuthorisationCountDayDTO currentDay = null;

    @ApiModelProperty(value = "Number of amended authorisations for previous days. The first element indicates " +
            "yesterday and so on. A maximum of seven entries is required if available")
    @Valid
    /**
     * Number of amended authorisations for previous days. The first element indicates yesterday and so on. A maximum
     * of seven entries is required if available
     **/
    private List<AuthorisationMetricsV2AuthorisationCountDayDTO> previousDays = null;

    /**
     * Get currentDay
     *
     * @return currentDay
     **/
    @JsonProperty("currentDay")
    public AuthorisationMetricsV2AuthorisationCountDayDTO getCurrentDay() {
        return currentDay;
    }

    public void setCurrentDay(AuthorisationMetricsV2AuthorisationCountDayDTO currentDay) {
        this.currentDay = currentDay;
    }

    public AuthorisationMetricsV2AmendedAuthorisationCountDTO currentDay(
            AuthorisationMetricsV2AuthorisationCountDayDTO currentDay) {
        this.currentDay = currentDay;
        return this;
    }

    /**
     * Number of amended authorisations for previous days. The first element indicates yesterday and so on. A maximum
     * of seven entries is required if available
     *
     * @return previousDays
     **/
    @JsonProperty("previousDays")
    public List<AuthorisationMetricsV2AuthorisationCountDayDTO> getPreviousDays() {
        return previousDays;
    }

    public void setPreviousDays(List<AuthorisationMetricsV2AuthorisationCountDayDTO> previousDays) {
        this.previousDays = previousDays;
    }

    public AuthorisationMetricsV2AmendedAuthorisationCountDTO previousDays(
            List<AuthorisationMetricsV2AuthorisationCountDayDTO> previousDays) {
        this.previousDays = previousDays;
        return this;
    }

    public AuthorisationMetricsV2AmendedAuthorisationCountDTO addPreviousDaysItem(
            AuthorisationMetricsV2AuthorisationCountDayDTO previousDaysItem) {
        this.previousDays.add(previousDaysItem);
        return this;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AuthorisationMetricsV2AmendedAuthorisationCountDTO {\n");

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


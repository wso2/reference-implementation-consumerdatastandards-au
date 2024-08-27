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
 * The number of new authorisations
 **/
@ApiModel(description = "The number of new authorisations")
public class AuthorisationMetricsV2NewAuthorisationCountDTO {

    @ApiModelProperty(value = "")
    @Valid
    private AuthorisationMetricsV2NewAuthorisationCountDayDTO currentDay = null;

    @ApiModelProperty(value = "Number of new authorisations for previous days. The first element indicates yesterday " +
            "and so on. A maximum of seven entries is required if available")
    @Valid
    /**
     * Number of new authorisations for previous days. The first element indicates yesterday and so on. A maximum of
     * seven entries is required if available
     **/
    private List<AuthorisationMetricsV2NewAuthorisationCountDayDTO> previousDays = null;

    /**
     * Get currentDay
     *
     * @return currentDay
     **/
    @JsonProperty("currentDay")
    public AuthorisationMetricsV2NewAuthorisationCountDayDTO getCurrentDay() {
        return currentDay;
    }

    public void setCurrentDay(AuthorisationMetricsV2NewAuthorisationCountDayDTO currentDay) {
        this.currentDay = currentDay;
    }

    public AuthorisationMetricsV2NewAuthorisationCountDTO currentDay(
            AuthorisationMetricsV2NewAuthorisationCountDayDTO currentDay) {
        this.currentDay = currentDay;
        return this;
    }

    /**
     * Number of new authorisations for previous days. The first element indicates yesterday and so on. A maximum of
     * seven entries is required if available
     *
     * @return previousDays
     **/
    @JsonProperty("previousDays")
    public List<AuthorisationMetricsV2NewAuthorisationCountDayDTO> getPreviousDays() {
        return previousDays;
    }

    public void setPreviousDays(List<AuthorisationMetricsV2NewAuthorisationCountDayDTO> previousDays) {
        this.previousDays = previousDays;
    }

    public AuthorisationMetricsV2NewAuthorisationCountDTO previousDays(
            List<AuthorisationMetricsV2NewAuthorisationCountDayDTO> previousDays) {
        this.previousDays = previousDays;
        return this;
    }

    public AuthorisationMetricsV2NewAuthorisationCountDTO addPreviousDaysItem(
            AuthorisationMetricsV2NewAuthorisationCountDayDTO previousDaysItem) {
        this.previousDays.add(previousDaysItem);
        return this;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AuthorisationMetricsV2NewAuthorisationCountDTO {\n");

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


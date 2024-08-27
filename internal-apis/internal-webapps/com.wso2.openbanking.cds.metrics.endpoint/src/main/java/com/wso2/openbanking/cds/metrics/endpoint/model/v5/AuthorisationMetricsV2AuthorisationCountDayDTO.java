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

import javax.validation.constraints.NotNull;

/**
 * Number of authorisations for a day
 **/
@ApiModel(description = "Number of authorisations for a day")
public class AuthorisationMetricsV2AuthorisationCountDayDTO {

    @ApiModelProperty(required = true, value = "Authorisation count for individual customers")
    /**
     * Authorisation count for individual customers
     **/
    private Integer individual;

    @ApiModelProperty(required = true, value = "Authorisation count for non-individual customers")
    /**
     * Authorisation count for non-individual customers
     **/
    private Integer nonIndividual;

    /**
     * Authorisation count for individual customers
     *
     * @return individual
     **/
    @JsonProperty("individual")
    @NotNull
    public Integer getIndividual() {
        return individual;
    }

    public void setIndividual(Integer individual) {
        this.individual = individual;
    }

    public AuthorisationMetricsV2AuthorisationCountDayDTO individual(Integer individual) {
        this.individual = individual;
        return this;
    }

    /**
     * Authorisation count for non-individual customers
     *
     * @return nonIndividual
     **/
    @JsonProperty("nonIndividual")
    @NotNull
    public Integer getNonIndividual() {
        return nonIndividual;
    }

    public void setNonIndividual(Integer nonIndividual) {
        this.nonIndividual = nonIndividual;
    }

    public AuthorisationMetricsV2AuthorisationCountDayDTO nonIndividual(Integer nonIndividual) {
        this.nonIndividual = nonIndividual;
        return this;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AuthorisationMetricsV2AuthorisationCountDayDTO {\n");

        sb.append("    individual: ").append(toIndentedString(individual)).append("\n");
        sb.append("    nonIndividual: ").append(toIndentedString(nonIndividual)).append("\n");
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


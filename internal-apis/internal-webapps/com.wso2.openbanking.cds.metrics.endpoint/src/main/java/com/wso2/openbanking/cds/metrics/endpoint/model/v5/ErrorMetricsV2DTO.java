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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Number of calls resulting in error due to server execution over time
 **/
@ApiModel(description = "Number of calls resulting in error due to server execution over time")
public class ErrorMetricsV2DTO {

    @ApiModelProperty(required = true, value = "")
    @Valid
    private ErrorMetricsV2AggregateDTO aggregate = null;

    @ApiModelProperty(required = true, value = "")
    @Valid
    private ErrorMetricsV2UnauthenticatedDTO unauthenticated = null;

    @ApiModelProperty(required = true, value = "")
    @Valid
    private ErrorMetricsV2AuthenticatedDTO authenticated = null;

    /**
     * Get aggregate
     *
     * @return aggregate
     **/
    @JsonProperty("aggregate")
    @NotNull
    public ErrorMetricsV2AggregateDTO getAggregate() {
        return aggregate;
    }

    public void setAggregate(ErrorMetricsV2AggregateDTO aggregate) {
        this.aggregate = aggregate;
    }

    public ErrorMetricsV2DTO aggregate(ErrorMetricsV2AggregateDTO aggregate) {
        this.aggregate = aggregate;
        return this;
    }

    /**
     * Get unauthenticated
     *
     * @return unauthenticated
     **/
    @JsonProperty("unauthenticated")
    @NotNull
    public ErrorMetricsV2UnauthenticatedDTO getUnauthenticated() {
        return unauthenticated;
    }

    public void setUnauthenticated(ErrorMetricsV2UnauthenticatedDTO unauthenticated) {
        this.unauthenticated = unauthenticated;
    }

    public ErrorMetricsV2DTO unauthenticated(ErrorMetricsV2UnauthenticatedDTO unauthenticated) {
        this.unauthenticated = unauthenticated;
        return this;
    }

    /**
     * Get authenticated
     *
     * @return authenticated
     **/
    @JsonProperty("authenticated")
    @NotNull
    public ErrorMetricsV2AuthenticatedDTO getAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(ErrorMetricsV2AuthenticatedDTO authenticated) {
        this.authenticated = authenticated;
    }

    public ErrorMetricsV2DTO authenticated(ErrorMetricsV2AuthenticatedDTO authenticated) {
        this.authenticated = authenticated;
        return this;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ErrorMetricsV2DTO {\n");

        sb.append("    aggregate: ").append(toIndentedString(aggregate)).append("\n");
        sb.append("    unauthenticated: ").append(toIndentedString(unauthenticated)).append("\n");
        sb.append("    authenticated: ").append(toIndentedString(authenticated)).append("\n");
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


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
 * Percentage of calls within the performance thresholds in each performance tier over time
 **/
@ApiModel(description = "Percentage of calls within the performance thresholds in each performance tier over time")
public class PerformanceMetricsV3DTO {

    @ApiModelProperty(value = "")
    @Valid
    private PerformanceMetricsV3AggregateDTO aggregate = null;

    @ApiModelProperty(required = true, value = "")
    @Valid
    private PerformanceMetricsV3HighPriorityDTO highPriority = null;

    @ApiModelProperty(required = true, value = "")
    @Valid
    private PerformanceMetricsV3LargePayloadDTO largePayload = null;

    @ApiModelProperty(required = true, value = "")
    @Valid
    private PerformanceMetricsV3LowPriorityDTO lowPriority = null;

    @ApiModelProperty(required = true, value = "")
    @Valid
    private PerformanceMetricsV3UnattendedDTO unattended = null;

    @ApiModelProperty(required = true, value = "")
    @Valid
    private PerformanceMetricsV3UnauthenticatedDTO unauthenticated = null;

    /**
     * Get aggregate
     *
     * @return aggregate
     **/
    @JsonProperty("aggregate")
    public PerformanceMetricsV3AggregateDTO getAggregate() {
        return aggregate;
    }

    public void setAggregate(PerformanceMetricsV3AggregateDTO aggregate) {
        this.aggregate = aggregate;
    }

    public PerformanceMetricsV3DTO aggregate(PerformanceMetricsV3AggregateDTO aggregate) {
        this.aggregate = aggregate;
        return this;
    }

    /**
     * Get highPriority
     *
     * @return highPriority
     **/
    @JsonProperty("highPriority")
    @NotNull
    public PerformanceMetricsV3HighPriorityDTO getHighPriority() {
        return highPriority;
    }

    public void setHighPriority(PerformanceMetricsV3HighPriorityDTO highPriority) {
        this.highPriority = highPriority;
    }

    public PerformanceMetricsV3DTO highPriority(PerformanceMetricsV3HighPriorityDTO highPriority) {
        this.highPriority = highPriority;
        return this;
    }

    /**
     * Get largePayload
     *
     * @return largePayload
     **/
    @JsonProperty("largePayload")
    @NotNull
    public PerformanceMetricsV3LargePayloadDTO getLargePayload() {
        return largePayload;
    }

    public void setLargePayload(PerformanceMetricsV3LargePayloadDTO largePayload) {
        this.largePayload = largePayload;
    }

    public PerformanceMetricsV3DTO largePayload(PerformanceMetricsV3LargePayloadDTO largePayload) {
        this.largePayload = largePayload;
        return this;
    }

    /**
     * Get lowPriority
     *
     * @return lowPriority
     **/
    @JsonProperty("lowPriority")
    @NotNull
    public PerformanceMetricsV3LowPriorityDTO getLowPriority() {
        return lowPriority;
    }

    public void setLowPriority(PerformanceMetricsV3LowPriorityDTO lowPriority) {
        this.lowPriority = lowPriority;
    }

    public PerformanceMetricsV3DTO lowPriority(PerformanceMetricsV3LowPriorityDTO lowPriority) {
        this.lowPriority = lowPriority;
        return this;
    }

    /**
     * Get unattended
     *
     * @return unattended
     **/
    @JsonProperty("unattended")
    @NotNull
    public PerformanceMetricsV3UnattendedDTO getUnattended() {
        return unattended;
    }

    public void setUnattended(PerformanceMetricsV3UnattendedDTO unattended) {
        this.unattended = unattended;
    }

    public PerformanceMetricsV3DTO unattended(PerformanceMetricsV3UnattendedDTO unattended) {
        this.unattended = unattended;
        return this;
    }

    /**
     * Get unauthenticated
     *
     * @return unauthenticated
     **/
    @JsonProperty("unauthenticated")
    @NotNull
    public PerformanceMetricsV3UnauthenticatedDTO getUnauthenticated() {
        return unauthenticated;
    }

    public void setUnauthenticated(PerformanceMetricsV3UnauthenticatedDTO unauthenticated) {
        this.unauthenticated = unauthenticated;
    }

    public PerformanceMetricsV3DTO unauthenticated(PerformanceMetricsV3UnauthenticatedDTO unauthenticated) {
        this.unauthenticated = unauthenticated;
        return this;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PerformanceMetricsV3DTO {\n");

        sb.append("    aggregate: ").append(toIndentedString(aggregate)).append("\n");
        sb.append("    highPriority: ").append(toIndentedString(highPriority)).append("\n");
        sb.append("    largePayload: ").append(toIndentedString(largePayload)).append("\n");
        sb.append("    lowPriority: ").append(toIndentedString(lowPriority)).append("\n");
        sb.append("    unattended: ").append(toIndentedString(unattended)).append("\n");
        sb.append("    unauthenticated: ").append(toIndentedString(unauthenticated)).append("\n");
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


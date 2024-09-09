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

package org.wso2.openbanking.cds.metrics.endpoint.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;

/**
 * Average response time in seconds, at millisecond resolution, within each performance tier.
 **/
@ApiModel(description = "Average response time in seconds, at millisecond resolution, within each performance tier")
public class AverageResponseMetricsDTO {

    @ApiModelProperty(value = "")
    @Valid
    private AverageResponseMetricsUnauthenticatedDTO unauthenticated = null;

    @ApiModelProperty(value = "")
    @Valid
    private AverageResponseMetricsHighPriorityDTO highPriority = null;

    @ApiModelProperty(value = "")
    @Valid
    private AverageResponseMetricsLowPriorityDTO lowPriority = null;

    @ApiModelProperty(value = "")
    @Valid
    private AverageResponseMetricsUnattendedDTO unattended = null;

    @ApiModelProperty(value = "")
    @Valid
    private AverageResponseMetricsLargePayloadDTO largePayload = null;

    /**
     * Get unauthenticated.
     *
     * @return unauthenticated
     **/
    @JsonProperty("unauthenticated")
    public AverageResponseMetricsUnauthenticatedDTO getUnauthenticated() {
        return unauthenticated;
    }

    public void setUnauthenticated(AverageResponseMetricsUnauthenticatedDTO unauthenticated) {
        this.unauthenticated = unauthenticated;
    }

    public AverageResponseMetricsDTO unauthenticated(AverageResponseMetricsUnauthenticatedDTO unauthenticated) {
        this.unauthenticated = unauthenticated;
        return this;
    }

    /**
     * Get highPriority.
     *
     * @return highPriority
     **/
    @JsonProperty("highPriority")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public AverageResponseMetricsHighPriorityDTO getHighPriority() {
        return highPriority;
    }

    public void setHighPriority(AverageResponseMetricsHighPriorityDTO highPriority) {
        this.highPriority = highPriority;
    }

    public AverageResponseMetricsDTO highPriority(AverageResponseMetricsHighPriorityDTO highPriority) {
        this.highPriority = highPriority;
        return this;
    }

    /**
     * Get lowPriority.
     *
     * @return lowPriority
     **/
    @JsonProperty("lowPriority")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public AverageResponseMetricsLowPriorityDTO getLowPriority() {
        return lowPriority;
    }

    public void setLowPriority(AverageResponseMetricsLowPriorityDTO lowPriority) {
        this.lowPriority = lowPriority;
    }

    public AverageResponseMetricsDTO lowPriority(AverageResponseMetricsLowPriorityDTO lowPriority) {
        this.lowPriority = lowPriority;
        return this;
    }

    /**
     * Get unattended.
     *
     * @return unattended
     **/
    @JsonProperty("unattended")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public AverageResponseMetricsUnattendedDTO getUnattended() {
        return unattended;
    }

    public void setUnattended(AverageResponseMetricsUnattendedDTO unattended) {
        this.unattended = unattended;
    }

    public AverageResponseMetricsDTO unattended(AverageResponseMetricsUnattendedDTO unattended) {
        this.unattended = unattended;
        return this;
    }

    /**
     * Get largePayload.
     *
     * @return largePayload
     **/
    @JsonProperty("largePayload")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public AverageResponseMetricsLargePayloadDTO getLargePayload() {
        return largePayload;
    }

    public void setLargePayload(AverageResponseMetricsLargePayloadDTO largePayload) {
        this.largePayload = largePayload;
    }

    public AverageResponseMetricsDTO largePayload(AverageResponseMetricsLargePayloadDTO largePayload) {
        this.largePayload = largePayload;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AverageResponseMetricsDTO {\n");

        sb.append("    unauthenticated: ").append(toIndentedString(unauthenticated)).append("\n");
        sb.append("    highPriority: ").append(toIndentedString(highPriority)).append("\n");
        sb.append("    lowPriority: ").append(toIndentedString(lowPriority)).append("\n");
        sb.append("    unattended: ").append(toIndentedString(unattended)).append("\n");
        sb.append("    largePayload: ").append(toIndentedString(largePayload)).append("\n");
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

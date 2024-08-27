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
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * ResponseMetricsListData DTO class.
 */
public class ResponseMetricsListDataDTO {

    @ApiModelProperty(required = true, value = "The date and time that the metrics in this payload were requested.")
    /**
     * The date and time that the metrics in this payload were requested.
     **/
    private String requestTime;

    @ApiModelProperty(value = "")
    @Valid
    private AvailabilityMetricsDTO availability = null;

    @ApiModelProperty(value = "")
    @Valid
    private PerformanceMetricsDTO performance = null;

    @ApiModelProperty(value = "")
    @Valid
    private InvocationMetricsDTO invocations = null;

    @ApiModelProperty(value = "")
    @Valid
    private AverageResponseMetricsDTO averageResponse = null;

    @ApiModelProperty(value = "")
    @Valid
    private SessionCountMetricsDTO sessionCount = null;

    @ApiModelProperty(value = "")
    @Valid
    private AverageTPSMetricsDTO averageTps = null;

    @ApiModelProperty(value = "")
    @Valid
    private PeakTPSMetricsDTO peakTps = null;

    @ApiModelProperty(value = "")
    @Valid
    private ErrorMetricsDTO errors = null;

    @ApiModelProperty(value = "")
    @Valid
    private RejectionMetricsDTO rejections = null;

    @ApiModelProperty(value = "Number of customers with active authorisations at the time of the call")
    /**
     * Number of customers with active authorisations at the time of the call
     **/
    private Integer customerCount;

    @ApiModelProperty(value = "Number of data recipients with active authorisations at the time of the call")
    /**
     * Number of data recipients with active authorisations at the time of the call
     **/
    private Integer recipientCount;

    /**
     * The date and time that the metrics in this payload were requested.
     *
     * @return requestTime
     **/
    @JsonProperty("requestTime")
    @NotNull
    public String getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }

    public ResponseMetricsListDataDTO requestTime(String requestTime) {
        this.requestTime = requestTime;
        return this;
    }

    /**
     * Get availability.
     *
     * @return availability
     **/
    @JsonProperty("availability")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public AvailabilityMetricsDTO getAvailability() {
        return availability;
    }

    public void setAvailability(AvailabilityMetricsDTO availability) {
        this.availability = availability;
    }

    public ResponseMetricsListDataDTO availability(AvailabilityMetricsDTO availability) {
        this.availability = availability;
        return this;
    }

    /**
     * Get performance.
     *
     * @return performance
     **/
    @JsonProperty("performance")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public PerformanceMetricsDTO getPerformance() {
        return performance;
    }

    public void setPerformance(PerformanceMetricsDTO performance) {
        this.performance = performance;
    }

    public ResponseMetricsListDataDTO performance(PerformanceMetricsDTO performance) {
        this.performance = performance;
        return this;
    }

    /**
     * Get invocations.
     *
     * @return invocations
     **/
    @JsonProperty("invocations")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public InvocationMetricsDTO getInvocations() {
        return invocations;
    }

    public void setInvocations(InvocationMetricsDTO invocations) {
        this.invocations = invocations;
    }

    public ResponseMetricsListDataDTO invocations(InvocationMetricsDTO invocations) {
        this.invocations = invocations;
        return this;
    }

    /**
     * Get averageResponse.
     *
     * @return averageResponse
     **/
    @JsonProperty("averageResponse")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public AverageResponseMetricsDTO getAverageResponse() {
        return averageResponse;
    }

    public void setAverageResponse(AverageResponseMetricsDTO averageResponse) {
        this.averageResponse = averageResponse;
    }

    public ResponseMetricsListDataDTO averageResponse(AverageResponseMetricsDTO averageResponse) {
        this.averageResponse = averageResponse;
        return this;
    }

    /**
     * Get sessionCount.
     *
     * @return sessionCount
     **/
    @JsonProperty("sessionCount")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public SessionCountMetricsDTO getSessionCount() {
        return sessionCount;
    }

    public void setSessionCount(SessionCountMetricsDTO sessionCount) {
        this.sessionCount = sessionCount;
    }

    public ResponseMetricsListDataDTO sessionCount(SessionCountMetricsDTO sessionCount) {
        this.sessionCount = sessionCount;
        return this;
    }

    /**
     * Get averageTps.
     *
     * @return averageTps
     **/
    @JsonProperty("averageTps")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public AverageTPSMetricsDTO getAverageTps() {
        return averageTps;
    }

    public void setAverageTps(AverageTPSMetricsDTO averageTps) {
        this.averageTps = averageTps;
    }

    public ResponseMetricsListDataDTO averageTps(AverageTPSMetricsDTO averageTps) {
        this.averageTps = averageTps;
        return this;
    }

    /**
     * Get peakTps.
     *
     * @return peakTps
     **/
    @JsonProperty("peakTps")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public PeakTPSMetricsDTO getPeakTps() {
        return peakTps;
    }

    public void setPeakTps(PeakTPSMetricsDTO peakTps) {
        this.peakTps = peakTps;
    }

    public ResponseMetricsListDataDTO peakTps(PeakTPSMetricsDTO peakTps) {
        this.peakTps = peakTps;
        return this;
    }

    /**
     * Get errors.
     *
     * @return errors
     **/
    @JsonProperty("errors")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public ErrorMetricsDTO getErrors() {
        return errors;
    }

    public void setErrors(ErrorMetricsDTO errors) {
        this.errors = errors;
    }

    public ResponseMetricsListDataDTO errors(ErrorMetricsDTO errors) {
        this.errors = errors;
        return this;
    }

    /**
     * Get rejections.
     *
     * @return rejections
     **/
    @JsonProperty("rejections")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public RejectionMetricsDTO getRejections() {
        return rejections;
    }

    public void setRejections(RejectionMetricsDTO rejections) {
        this.rejections = rejections;
    }

    public ResponseMetricsListDataDTO rejections(RejectionMetricsDTO rejections) {
        this.rejections = rejections;
        return this;
    }

    /**
     * Number of customers with active authorisations at the time of the call.
     *
     * @return customerCount
     **/
    @JsonProperty("customerCount")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Integer getCustomerCount() {
        return customerCount;
    }

    public void setCustomerCount(Integer customerCount) {
        this.customerCount = customerCount;
    }

    public ResponseMetricsListDataDTO customerCount(Integer customerCount) {
        this.customerCount = customerCount;
        return this;
    }

    /**
     * Number of data recipients with active authorisations at the time of the call.
     *
     * @return recipientCount
     **/
    @JsonProperty("recipientCount")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Integer getRecipientCount() {
        return recipientCount;
    }

    public void setRecipientCount(Integer recipientCount) {
        this.recipientCount = recipientCount;
    }

    public ResponseMetricsListDataDTO recipientCount(Integer recipientCount) {
        this.recipientCount = recipientCount;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ResponseMetricsListDataDTO {\n");

        sb.append("    requestTime: ").append(toIndentedString(requestTime)).append("\n");
        sb.append("    availability: ").append(toIndentedString(availability)).append("\n");
        sb.append("    performance: ").append(toIndentedString(performance)).append("\n");
        sb.append("    invocations: ").append(toIndentedString(invocations)).append("\n");
        sb.append("    averageResponse: ").append(toIndentedString(averageResponse)).append("\n");
        sb.append("    sessionCount: ").append(toIndentedString(sessionCount)).append("\n");
        sb.append("    averageTps: ").append(toIndentedString(averageTps)).append("\n");
        sb.append("    peakTps: ").append(toIndentedString(peakTps)).append("\n");
        sb.append("    errors: ").append(toIndentedString(errors)).append("\n");
        sb.append("    rejections: ").append(toIndentedString(rejections)).append("\n");
        sb.append("    customerCount: ").append(toIndentedString(customerCount)).append("\n");
        sb.append("    recipientCount: ").append(toIndentedString(recipientCount)).append("\n");
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


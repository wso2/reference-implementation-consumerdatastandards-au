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
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * ResponseMetricsListV5Data DTO class.
 */
public class ResponseMetricsListV5DataDTO {

    @ApiModelProperty(required = true, value = "The date and time that the metrics in this payload were requested.")
    /**
     * The date and time that the metrics in this payload were requested.
     **/
    private String requestTime;

    @ApiModelProperty(required = true, value = "")
    @Valid
    private AvailabilityMetricsV2DTO availability = null;

    @ApiModelProperty(required = true, value = "")
    @Valid
    private PerformanceMetricsV3DTO performance = null;

    @ApiModelProperty(required = true, value = "")
    @Valid
    private InvocationMetricsV3DTO invocations = null;

    @ApiModelProperty(required = true, value = "")
    @Valid
    private AverageResponseMetricsV2DTO averageResponse = null;

    @ApiModelProperty(required = true, value = "")
    @Valid
    private SessionCountMetricsV2DTO sessionCount = null;

    @ApiModelProperty(required = true, value = "")
    @Valid
    private AverageTPSMetricsV2DTO averageTps = null;

    @ApiModelProperty(required = true, value = "")
    @Valid
    private PeakTPSMetricsV2DTO peakTps = null;

    @ApiModelProperty(required = true, value = "")
    @Valid
    private ErrorMetricsV2DTO errors = null;

    @ApiModelProperty(required = true, value = "")
    @Valid
    private RejectionMetricsV3DTO rejections = null;

    @ApiModelProperty(required = true, value = "Number of customers with active authorisations at the time of the call")
    /**
     * Number of customers with active authorisations at the time of the call
     **/
    private Integer customerCount;

    @ApiModelProperty(required = true, value = "Number of Data Recipient Software Products with active authorisations" +
            " at the time of the call")
    /**
     * Number of Data Recipient Software Products with active authorisations at the time of the call
     **/
    private Integer recipientCount;

    @ApiModelProperty(required = true, value = "")
    @Valid
    private AuthorisationMetricsV2DTO authorisations = null;

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

    public ResponseMetricsListV5DataDTO requestTime(String requestTime) {
        this.requestTime = requestTime;
        return this;
    }

    /**
     * Get availability
     *
     * @return availability
     **/
    @JsonProperty("availability")
    @NotNull
    public AvailabilityMetricsV2DTO getAvailability() {
        return availability;
    }

    public void setAvailability(AvailabilityMetricsV2DTO availability) {
        this.availability = availability;
    }

    public ResponseMetricsListV5DataDTO availability(AvailabilityMetricsV2DTO availability) {
        this.availability = availability;
        return this;
    }

    /**
     * Get performance
     *
     * @return performance
     **/
    @JsonProperty("performance")
    @NotNull
    public PerformanceMetricsV3DTO getPerformance() {
        return performance;
    }

    public void setPerformance(PerformanceMetricsV3DTO performance) {
        this.performance = performance;
    }

    public ResponseMetricsListV5DataDTO performance(PerformanceMetricsV3DTO performance) {
        this.performance = performance;
        return this;
    }

    /**
     * Get invocations
     *
     * @return invocations
     **/
    @JsonProperty("invocations")
    @NotNull
    public InvocationMetricsV3DTO getInvocations() {
        return invocations;
    }

    public void setInvocations(InvocationMetricsV3DTO invocations) {
        this.invocations = invocations;
    }

    public ResponseMetricsListV5DataDTO invocations(InvocationMetricsV3DTO invocations) {
        this.invocations = invocations;
        return this;
    }

    /**
     * Get averageResponse
     *
     * @return averageResponse
     **/
    @JsonProperty("averageResponse")
    @NotNull
    public AverageResponseMetricsV2DTO getAverageResponse() {
        return averageResponse;
    }

    public void setAverageResponse(AverageResponseMetricsV2DTO averageResponse) {
        this.averageResponse = averageResponse;
    }

    public ResponseMetricsListV5DataDTO averageResponse(AverageResponseMetricsV2DTO averageResponse) {
        this.averageResponse = averageResponse;
        return this;
    }

    /**
     * Get sessionCount
     *
     * @return sessionCount
     **/
    @JsonProperty("sessionCount")
    @NotNull
    public SessionCountMetricsV2DTO getSessionCount() {
        return sessionCount;
    }

    public void setSessionCount(SessionCountMetricsV2DTO sessionCount) {
        this.sessionCount = sessionCount;
    }

    public ResponseMetricsListV5DataDTO sessionCount(SessionCountMetricsV2DTO sessionCount) {
        this.sessionCount = sessionCount;
        return this;
    }

    /**
     * Get averageTps
     *
     * @return averageTps
     **/
    @JsonProperty("averageTps")
    @NotNull
    public AverageTPSMetricsV2DTO getAverageTps() {
        return averageTps;
    }

    public void setAverageTps(AverageTPSMetricsV2DTO averageTps) {
        this.averageTps = averageTps;
    }

    public ResponseMetricsListV5DataDTO averageTps(AverageTPSMetricsV2DTO averageTps) {
        this.averageTps = averageTps;
        return this;
    }

    /**
     * Get peakTps
     *
     * @return peakTps
     **/
    @JsonProperty("peakTps")
    @NotNull
    public PeakTPSMetricsV2DTO getPeakTps() {
        return peakTps;
    }

    public void setPeakTps(PeakTPSMetricsV2DTO peakTps) {
        this.peakTps = peakTps;
    }

    public ResponseMetricsListV5DataDTO peakTps(PeakTPSMetricsV2DTO peakTps) {
        this.peakTps = peakTps;
        return this;
    }

    /**
     * Get errors
     *
     * @return errors
     **/
    @JsonProperty("errors")
    @NotNull
    public ErrorMetricsV2DTO getErrors() {
        return errors;
    }

    public void setErrors(ErrorMetricsV2DTO errors) {
        this.errors = errors;
    }

    public ResponseMetricsListV5DataDTO errors(ErrorMetricsV2DTO errors) {
        this.errors = errors;
        return this;
    }

    /**
     * Get rejections
     *
     * @return rejections
     **/
    @JsonProperty("rejections")
    @NotNull
    public RejectionMetricsV3DTO getRejections() {
        return rejections;
    }

    public void setRejections(RejectionMetricsV3DTO rejections) {
        this.rejections = rejections;
    }

    public ResponseMetricsListV5DataDTO rejections(RejectionMetricsV3DTO rejections) {
        this.rejections = rejections;
        return this;
    }

    /**
     * Number of customers with active authorisations at the time of the call
     *
     * @return customerCount
     **/
    @JsonProperty("customerCount")
    @NotNull
    public Integer getCustomerCount() {
        return customerCount;
    }

    public void setCustomerCount(Integer customerCount) {
        this.customerCount = customerCount;
    }

    public ResponseMetricsListV5DataDTO customerCount(Integer customerCount) {
        this.customerCount = customerCount;
        return this;
    }

    /**
     * Number of Data Recipient Software Products with active authorisations at the time of the call
     *
     * @return recipientCount
     **/
    @JsonProperty("recipientCount")
    @NotNull
    public Integer getRecipientCount() {
        return recipientCount;
    }

    public void setRecipientCount(Integer recipientCount) {
        this.recipientCount = recipientCount;
    }

    public ResponseMetricsListV5DataDTO recipientCount(Integer recipientCount) {
        this.recipientCount = recipientCount;
        return this;
    }

    /**
     * Get authorisations
     *
     * @return authorisations
     **/
    @JsonProperty("authorisations")
    @NotNull
    public AuthorisationMetricsV2DTO getAuthorisations() {
        return authorisations;
    }

    public void setAuthorisations(AuthorisationMetricsV2DTO authorisations) {
        this.authorisations = authorisations;
    }

    public ResponseMetricsListV5DataDTO authorisations(AuthorisationMetricsV2DTO authorisations) {
        this.authorisations = authorisations;
        return this;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ResponseMetricsListV5DataDTO {\n");

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
        sb.append("    authorisations: ").append(toIndentedString(authorisations)).append("\n");
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


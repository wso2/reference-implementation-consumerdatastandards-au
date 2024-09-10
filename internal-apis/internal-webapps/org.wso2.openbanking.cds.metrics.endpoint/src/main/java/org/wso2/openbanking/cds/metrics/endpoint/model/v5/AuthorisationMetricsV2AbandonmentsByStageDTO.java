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
 * Customer abandonment count per stage of the consent flow.  Note that the aggregated abandonment count for all
 * stages for a period should equal the count in `abandonedConsentFlowCount` for the same period (ie. each abandoned
 * consent should assigned to one, and only one, stage)
 **/
@ApiModel(description = "Customer abandonment count per stage of the consent flow.  Note that the aggregated " +
        "abandonment count for all stages for a period should equal the count in `abandonedConsentFlowCount` for the " +
        "same period (ie. each abandoned consent should assigned to one, and only one, stage)")
public class AuthorisationMetricsV2AbandonmentsByStageDTO {

    @ApiModelProperty(required = true, value = "")
    @Valid
    private AuthorisationMetricsV2AbandonmentsByStagePreIdentificationDTO preIdentification = null;

    @ApiModelProperty(required = true, value = "")
    @Valid
    private AuthorisationMetricsV2AbandonmentsByStagePreAuthenticationDTO preAuthentication = null;

    @ApiModelProperty(required = true, value = "")
    @Valid
    private AuthorisationMetricsV2AbandonmentsByStagePreAccountSelectionDTO preAccountSelection = null;

    @ApiModelProperty(required = true, value = "")
    @Valid
    private AuthorisationMetricsV2AbandonmentsByStagePreAuthorisationDTO preAuthorisation = null;

    @ApiModelProperty(required = true, value = "")
    @Valid
    private AuthorisationMetricsV2AbandonmentsByStageRejectedDTO rejected = null;

    @ApiModelProperty(required = true, value = "")
    @Valid
    private AuthorisationMetricsV2AbandonmentsByStageFailedTokenExchangeDTO failedTokenExchange = null;

    /**
     * Get preIdentification
     *
     * @return preIdentification
     **/
    @JsonProperty("preIdentification")
    @NotNull
    public AuthorisationMetricsV2AbandonmentsByStagePreIdentificationDTO getPreIdentification() {
        return preIdentification;
    }

    public void setPreIdentification(AuthorisationMetricsV2AbandonmentsByStagePreIdentificationDTO preIdentification) {
        this.preIdentification = preIdentification;
    }

    public AuthorisationMetricsV2AbandonmentsByStageDTO preIdentification(
            AuthorisationMetricsV2AbandonmentsByStagePreIdentificationDTO preIdentification) {
        this.preIdentification = preIdentification;
        return this;
    }

    /**
     * Get preAuthentication
     *
     * @return preAuthentication
     **/
    @JsonProperty("preAuthentication")
    @NotNull
    public AuthorisationMetricsV2AbandonmentsByStagePreAuthenticationDTO getPreAuthentication() {
        return preAuthentication;
    }

    public void setPreAuthentication(AuthorisationMetricsV2AbandonmentsByStagePreAuthenticationDTO preAuthentication) {
        this.preAuthentication = preAuthentication;
    }

    public AuthorisationMetricsV2AbandonmentsByStageDTO preAuthentication(
            AuthorisationMetricsV2AbandonmentsByStagePreAuthenticationDTO preAuthentication) {
        this.preAuthentication = preAuthentication;
        return this;
    }

    /**
     * Get preAccountSelection
     *
     * @return preAccountSelection
     **/
    @JsonProperty("preAccountSelection")
    @NotNull
    public AuthorisationMetricsV2AbandonmentsByStagePreAccountSelectionDTO getPreAccountSelection() {
        return preAccountSelection;
    }

    public void setPreAccountSelection(
            AuthorisationMetricsV2AbandonmentsByStagePreAccountSelectionDTO preAccountSelection) {
        this.preAccountSelection = preAccountSelection;
    }

    public AuthorisationMetricsV2AbandonmentsByStageDTO preAccountSelection(
            AuthorisationMetricsV2AbandonmentsByStagePreAccountSelectionDTO preAccountSelection) {
        this.preAccountSelection = preAccountSelection;
        return this;
    }

    /**
     * Get preAuthorisation
     *
     * @return preAuthorisation
     **/
    @JsonProperty("preAuthorisation")
    @NotNull
    public AuthorisationMetricsV2AbandonmentsByStagePreAuthorisationDTO getPreAuthorisation() {
        return preAuthorisation;
    }

    public void setPreAuthorisation(AuthorisationMetricsV2AbandonmentsByStagePreAuthorisationDTO preAuthorisation) {
        this.preAuthorisation = preAuthorisation;
    }

    public AuthorisationMetricsV2AbandonmentsByStageDTO preAuthorisation(
            AuthorisationMetricsV2AbandonmentsByStagePreAuthorisationDTO preAuthorisation) {
        this.preAuthorisation = preAuthorisation;
        return this;
    }

    /**
     * Get rejected
     *
     * @return rejected
     **/
    @JsonProperty("rejected")
    @NotNull
    public AuthorisationMetricsV2AbandonmentsByStageRejectedDTO getRejected() {
        return rejected;
    }

    public void setRejected(AuthorisationMetricsV2AbandonmentsByStageRejectedDTO rejected) {
        this.rejected = rejected;
    }

    public AuthorisationMetricsV2AbandonmentsByStageDTO rejected(
            AuthorisationMetricsV2AbandonmentsByStageRejectedDTO rejected) {
        this.rejected = rejected;
        return this;
    }

    /**
     * Get failedTokenExchange
     *
     * @return failedTokenExchange
     **/
    @JsonProperty("failedTokenExchange")
    @NotNull
    public AuthorisationMetricsV2AbandonmentsByStageFailedTokenExchangeDTO getFailedTokenExchange() {
        return failedTokenExchange;
    }

    public void setFailedTokenExchange(
            AuthorisationMetricsV2AbandonmentsByStageFailedTokenExchangeDTO failedTokenExchange) {
        this.failedTokenExchange = failedTokenExchange;
    }

    public AuthorisationMetricsV2AbandonmentsByStageDTO failedTokenExchange(
            AuthorisationMetricsV2AbandonmentsByStageFailedTokenExchangeDTO failedTokenExchange) {
        this.failedTokenExchange = failedTokenExchange;
        return this;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AuthorisationMetricsV2AbandonmentsByStageDTO {\n");

        sb.append("    preIdentification: ").append(toIndentedString(preIdentification)).append("\n");
        sb.append("    preAuthentication: ").append(toIndentedString(preAuthentication)).append("\n");
        sb.append("    preAccountSelection: ").append(toIndentedString(preAccountSelection)).append("\n");
        sb.append("    preAuthorisation: ").append(toIndentedString(preAuthorisation)).append("\n");
        sb.append("    rejected: ").append(toIndentedString(rejected)).append("\n");
        sb.append("    failedTokenExchange: ").append(toIndentedString(failedTokenExchange)).append("\n");
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


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
 * Authorisation counts for the data holder
 **/
@ApiModel(description = "Authorisation counts for the data holder")
public class AuthorisationMetricsV2DTO {

    @ApiModelProperty(required = true, value = "")
    @Valid
    private AuthorisationMetricsV2ActiveAuthorisationCountDTO activeAuthorisationCount = null;

    @ApiModelProperty(required = true, value = "")
    @Valid
    private AuthorisationMetricsV2NewAuthorisationCountDTO newAuthorisationCount = null;

    @ApiModelProperty(required = true, value = "")
    @Valid
    private AuthorisationMetricsV2RevokedAuthorisationCountDTO revokedAuthorisationCount = null;

    @ApiModelProperty(required = true, value = "")
    @Valid
    private AuthorisationMetricsV2AmendedAuthorisationCountDTO amendedAuthorisationCount = null;

    @ApiModelProperty(required = true, value = "")
    @Valid
    private AuthorisationMetricsV2ExpiredAuthorisationCountDTO expiredAuthorisationCount = null;

    @ApiModelProperty(required = true, value = "")
    @Valid
    private AuthorisationMetricsV2AbandonedConsentFlowCountDTO abandonedConsentFlowCount = null;

    @ApiModelProperty(required = true, value = "")
    @Valid
    private AuthorisationMetricsV2AbandonmentsByStageDTO abandonmentsByStage = null;

    /**
     * Get activeAuthorisationCount
     *
     * @return activeAuthorisationCount
     **/
    @JsonProperty("activeAuthorisationCount")
    @NotNull
    public AuthorisationMetricsV2ActiveAuthorisationCountDTO getActiveAuthorisationCount() {
        return activeAuthorisationCount;
    }

    public void setActiveAuthorisationCount(
            AuthorisationMetricsV2ActiveAuthorisationCountDTO activeAuthorisationCount) {
        this.activeAuthorisationCount = activeAuthorisationCount;
    }

    public AuthorisationMetricsV2DTO activeAuthorisationCount(
            AuthorisationMetricsV2ActiveAuthorisationCountDTO activeAuthorisationCount) {
        this.activeAuthorisationCount = activeAuthorisationCount;
        return this;
    }

    /**
     * Get newAuthorisationCount
     *
     * @return newAuthorisationCount
     **/
    @JsonProperty("newAuthorisationCount")
    @NotNull
    public AuthorisationMetricsV2NewAuthorisationCountDTO getNewAuthorisationCount() {
        return newAuthorisationCount;
    }

    public void setNewAuthorisationCount(AuthorisationMetricsV2NewAuthorisationCountDTO newAuthorisationCount) {
        this.newAuthorisationCount = newAuthorisationCount;
    }

    public AuthorisationMetricsV2DTO newAuthorisationCount(
            AuthorisationMetricsV2NewAuthorisationCountDTO newAuthorisationCount) {
        this.newAuthorisationCount = newAuthorisationCount;
        return this;
    }

    /**
     * Get revokedAuthorisationCount
     *
     * @return revokedAuthorisationCount
     **/
    @JsonProperty("revokedAuthorisationCount")
    @NotNull
    public AuthorisationMetricsV2RevokedAuthorisationCountDTO getRevokedAuthorisationCount() {
        return revokedAuthorisationCount;
    }

    public void setRevokedAuthorisationCount(
            AuthorisationMetricsV2RevokedAuthorisationCountDTO revokedAuthorisationCount) {
        this.revokedAuthorisationCount = revokedAuthorisationCount;
    }

    public AuthorisationMetricsV2DTO revokedAuthorisationCount(
            AuthorisationMetricsV2RevokedAuthorisationCountDTO revokedAuthorisationCount) {
        this.revokedAuthorisationCount = revokedAuthorisationCount;
        return this;
    }

    /**
     * Get amendedAuthorisationCount
     *
     * @return amendedAuthorisationCount
     **/
    @JsonProperty("amendedAuthorisationCount")
    @NotNull
    public AuthorisationMetricsV2AmendedAuthorisationCountDTO getAmendedAuthorisationCount() {
        return amendedAuthorisationCount;
    }

    public void setAmendedAuthorisationCount(
            AuthorisationMetricsV2AmendedAuthorisationCountDTO amendedAuthorisationCount) {
        this.amendedAuthorisationCount = amendedAuthorisationCount;
    }

    public AuthorisationMetricsV2DTO amendedAuthorisationCount(
            AuthorisationMetricsV2AmendedAuthorisationCountDTO amendedAuthorisationCount) {
        this.amendedAuthorisationCount = amendedAuthorisationCount;
        return this;
    }

    /**
     * Get expiredAuthorisationCount
     *
     * @return expiredAuthorisationCount
     **/
    @JsonProperty("expiredAuthorisationCount")
    @NotNull
    public AuthorisationMetricsV2ExpiredAuthorisationCountDTO getExpiredAuthorisationCount() {
        return expiredAuthorisationCount;
    }

    public void setExpiredAuthorisationCount(
            AuthorisationMetricsV2ExpiredAuthorisationCountDTO expiredAuthorisationCount) {
        this.expiredAuthorisationCount = expiredAuthorisationCount;
    }

    public AuthorisationMetricsV2DTO expiredAuthorisationCount(
            AuthorisationMetricsV2ExpiredAuthorisationCountDTO expiredAuthorisationCount) {
        this.expiredAuthorisationCount = expiredAuthorisationCount;
        return this;
    }

    /**
     * Get abandonedConsentFlowCount
     *
     * @return abandonedConsentFlowCount
     **/
    @JsonProperty("abandonedConsentFlowCount")
    @NotNull
    public AuthorisationMetricsV2AbandonedConsentFlowCountDTO getAbandonedConsentFlowCount() {
        return abandonedConsentFlowCount;
    }

    public void setAbandonedConsentFlowCount(
            AuthorisationMetricsV2AbandonedConsentFlowCountDTO abandonedConsentFlowCount) {
        this.abandonedConsentFlowCount = abandonedConsentFlowCount;
    }

    public AuthorisationMetricsV2DTO abandonedConsentFlowCount(
            AuthorisationMetricsV2AbandonedConsentFlowCountDTO abandonedConsentFlowCount) {
        this.abandonedConsentFlowCount = abandonedConsentFlowCount;
        return this;
    }

    /**
     * Get abandonmentsByStage
     *
     * @return abandonmentsByStage
     **/
    @JsonProperty("abandonmentsByStage")
    @NotNull
    public AuthorisationMetricsV2AbandonmentsByStageDTO getAbandonmentsByStage() {
        return abandonmentsByStage;
    }

    public void setAbandonmentsByStage(AuthorisationMetricsV2AbandonmentsByStageDTO abandonmentsByStage) {
        this.abandonmentsByStage = abandonmentsByStage;
    }

    public AuthorisationMetricsV2DTO abandonmentsByStage(
            AuthorisationMetricsV2AbandonmentsByStageDTO abandonmentsByStage) {
        this.abandonmentsByStage = abandonmentsByStage;
        return this;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AuthorisationMetricsV2DTO {\n");

        sb.append("    activeAuthorisationCount: ").append(toIndentedString(activeAuthorisationCount)).append("\n");
        sb.append("    newAuthorisationCount: ").append(toIndentedString(newAuthorisationCount)).append("\n");
        sb.append("    revokedAuthorisationCount: ").append(toIndentedString(revokedAuthorisationCount)).append("\n");
        sb.append("    amendedAuthorisationCount: ").append(toIndentedString(amendedAuthorisationCount)).append("\n");
        sb.append("    expiredAuthorisationCount: ").append(toIndentedString(expiredAuthorisationCount)).append("\n");
        sb.append("    abandonedConsentFlowCount: ").append(toIndentedString(abandonedConsentFlowCount)).append("\n");
        sb.append("    abandonmentsByStage: ").append(toIndentedString(abandonmentsByStage)).append("\n");
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


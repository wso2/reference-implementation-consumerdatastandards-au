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
 * Number of calls rejected due to traffic thresholds over time
 **/
@ApiModel(description = "Number of calls rejected due to traffic thresholds over time")
public class RejectionMetricsV3DTO {

    @ApiModelProperty(required = true, value = "")
    @Valid
    private RejectionMetricsV3AuthenticatedDTO authenticated = null;

    @ApiModelProperty(required = true, value = "")
    @Valid
    private RejectionMetricsV3UnauthenticatedDTO unauthenticated = null;

    /**
     * Get authenticated
     *
     * @return authenticated
     **/
    @JsonProperty("authenticated")
    @NotNull
    public RejectionMetricsV3AuthenticatedDTO getAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(RejectionMetricsV3AuthenticatedDTO authenticated) {
        this.authenticated = authenticated;
    }

    public RejectionMetricsV3DTO authenticated(RejectionMetricsV3AuthenticatedDTO authenticated) {
        this.authenticated = authenticated;
        return this;
    }

    /**
     * Get unauthenticated
     *
     * @return unauthenticated
     **/
    @JsonProperty("unauthenticated")
    @NotNull
    public RejectionMetricsV3UnauthenticatedDTO getUnauthenticated() {
        return unauthenticated;
    }

    public void setUnauthenticated(RejectionMetricsV3UnauthenticatedDTO unauthenticated) {
        this.unauthenticated = unauthenticated;
    }

    public RejectionMetricsV3DTO unauthenticated(RejectionMetricsV3UnauthenticatedDTO unauthenticated) {
        this.unauthenticated = unauthenticated;
        return this;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class RejectionMetricsV3DTO {\n");

        sb.append("    authenticated: ").append(toIndentedString(authenticated)).append("\n");
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


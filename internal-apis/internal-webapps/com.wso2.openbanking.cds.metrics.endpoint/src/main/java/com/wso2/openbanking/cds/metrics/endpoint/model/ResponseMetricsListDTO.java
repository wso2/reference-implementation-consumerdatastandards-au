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
 * ResponseMetricsList DTO class.
 */
public class ResponseMetricsListDTO {

    @ApiModelProperty(required = true, value = "")
    @Valid
    private ResponseMetricsListDataDTO data = null;

    @ApiModelProperty(required = true, value = "")
    @Valid
    private LinksDTO links = null;

    @ApiModelProperty(value = "")
    @Valid
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object meta = null;

    /**
     * Get data.
     *
     * @return data
     **/
    @JsonProperty("data")
    @NotNull
    public ResponseMetricsListDataDTO getData() {
        return data;
    }

    public void setData(ResponseMetricsListDataDTO data) {
        this.data = data;
    }

    public ResponseMetricsListDTO data(ResponseMetricsListDataDTO data) {
        this.data = data;
        return this;
    }

    /**
     * Get links.
     *
     * @return links
     **/
    @JsonProperty("links")
    @NotNull
    public LinksDTO getLinks() {
        return links;
    }

    public void setLinks(LinksDTO links) {
        this.links = links;
    }

    public ResponseMetricsListDTO links(LinksDTO links) {
        this.links = links;
        return this;
    }

    /**
     * Get meta.
     *
     * @return meta
     **/
    @JsonProperty("meta")
    public Object getMeta() {
        return meta;
    }

    public void setMeta(Object meta) {
        this.meta = meta;
    }

    public ResponseMetricsListDTO meta(Object meta) {
        this.meta = meta;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ResponseMetricsListDTO {\n");

        sb.append("    data: ").append(toIndentedString(data)).append("\n");
        sb.append("    links: ").append(toIndentedString(links)).append("\n");
        sb.append("    meta: ").append(toIndentedString(meta)).append("\n");
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


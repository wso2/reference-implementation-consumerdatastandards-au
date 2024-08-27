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

package org.wso2.openbanking.cds.demo.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * RequestAccountIds.
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2019-08-16T11:28:45.176Z")
public class RequestAccountIds {

    @JsonProperty("data")
    private RequestAccountIdsData data = null;

    @JsonProperty("meta")
    private Meta meta = null;

    public RequestAccountIds data(RequestAccountIdsData data) {

        this.data = data;
        return this;
    }

    /**
     * Get data.
     *
     * @return data
     **/
    @JsonProperty("data")
    @ApiModelProperty(value = "")
    public RequestAccountIdsData getData() {

        return data;
    }

    public void setData(RequestAccountIdsData data) {

        this.data = data;
    }

    public RequestAccountIds meta(Meta meta) {

        this.meta = meta;
        return this;
    }

    /**
     * Get meta.
     *
     * @return meta
     **/
    @JsonProperty("meta")
    @ApiModelProperty(required = true, value = "")

    public Meta getMeta() {

        return meta;
    }

    public void setMeta(Meta meta) {

        this.meta = meta;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RequestAccountIds requestAccountIds = (RequestAccountIds) o;
        return Objects.equals(this.data, requestAccountIds.data) &&
                Objects.equals(this.meta, requestAccountIds.meta);
    }

    @Override
    public int hashCode() {

        return Objects.hash(data, meta);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");

        sb.append("    data: ").append(toIndentedString(data)).append(",\n");
        sb.append("    meta: ").append(toIndentedString(meta)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}


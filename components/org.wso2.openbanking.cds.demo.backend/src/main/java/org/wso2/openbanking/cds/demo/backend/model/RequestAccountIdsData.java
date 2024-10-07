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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * RequestAccountIdsData.
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2019-08-16T11:28:45.176Z")
public class RequestAccountIdsData {

    @JsonProperty("accountIds")
    private List<String> accountIds = new ArrayList<String>();

    public RequestAccountIdsData accountIds(List<String> accountIds) {

        this.accountIds = accountIds;
        return this;
    }

    public RequestAccountIdsData addAccountIdsItem(String accountIdsItem) {

        this.accountIds.add(accountIdsItem);
        return this;
    }

    /**
     * Get accountIds.
     *
     * @return accountIds
     **/
    @JsonProperty("accountIds")
    @ApiModelProperty(required = true, value = "")

    public List<String> getAccountIds() {

        return accountIds;
    }

    public void setAccountIds(List<String> accountIds) {

        this.accountIds = accountIds;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RequestAccountIdsData requestAccountIdsData = (RequestAccountIdsData) o;
        return Objects.equals(this.accountIds, requestAccountIdsData.accountIds);
    }

    @Override
    public int hashCode() {

        return Objects.hash(accountIds);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");

        sb.append("    accountIds: ").append(toIndentedString(accountIds)).append("\n");
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


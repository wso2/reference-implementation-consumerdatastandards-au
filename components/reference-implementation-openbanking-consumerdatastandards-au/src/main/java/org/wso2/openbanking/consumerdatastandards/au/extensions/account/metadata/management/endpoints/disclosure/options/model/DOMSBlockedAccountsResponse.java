/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.openbanking.consumerdatastandards.au.extensions.account.metadata.management.endpoints.disclosure.options.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@ApiModel(description = "Response containing DOMS-blocked account IDs")
public class DOMSBlockedAccountsResponse {

    @ApiModelProperty(
            value = "List of account IDs blocked from disclosure",
            example = "[\"300080012343456\"]"
    )
    private List<String> blockedAccountIds;

    public DOMSBlockedAccountsResponse(List<String> blockedAccountIds) {
        this.blockedAccountIds = blockedAccountIds;
    }

    public List<String> getBlockedAccountIds() {
        return blockedAccountIds;
    }

    public void setBlockedAccountIds(List<String> blockedAccountIds) {
        this.blockedAccountIds = blockedAccountIds;
    }
}

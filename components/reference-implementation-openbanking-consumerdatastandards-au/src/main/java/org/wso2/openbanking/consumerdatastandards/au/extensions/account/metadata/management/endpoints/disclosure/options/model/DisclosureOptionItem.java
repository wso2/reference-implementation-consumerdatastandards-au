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

import javax.validation.constraints.NotBlank;

@ApiModel(description = "Disclosure option update entry")
public class DisclosureOptionItem {

    @ApiModelProperty(
            value = "Account ID",
            example = "143-000-B1234",
            required = true
    )
    @NotBlank
    private String accountID;

    @ApiModelProperty(
            value = "Disclosure option status",
            example = "no-sharing",
            required = true
    )
    @NotBlank
    private DisclosureOption disclosureOption;

    public String getAccountID() {
        return accountID;
    }

    public void setAccountID(String accountID) {
        this.accountID = accountID;
    }

    public DisclosureOption getDisclosureOption() {
        return disclosureOption;
    }

    public void setDisclosureOption(DisclosureOption disclosureOption) {
        this.disclosureOption = disclosureOption;
    }
}

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

package org.wso2.openbanking.cds.account.type.management.endpoint.disclosure.options.model;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * Disclosure Options Management - DOMSStatusUpdateDTOItem.
 */
public class DOMSStatusUpdateDTO {
    @NotEmpty(message = "Expected field is not present for the field \"accountID\" ")
    private String accountID;

    @NotEmpty(message = "Expected field is not present for the field \"disclosureOption\" ")
    @Pattern(regexp = "^(pre-approval|no-sharing)$", message = "Invalid Disclosure Option value. " +
            "Must be pre-approval or no-sharing.")
    private String disclosureOption;

    public String getAccountID() {
        return accountID;
    }

    public void setAccountID(String accountID) {
        this.accountID = accountID;
    }

    public String getDisclosureOption() {
        return disclosureOption;
    }

    public void setDisclosureOption(String disclosureOption) {
        this.disclosureOption = disclosureOption;
    }
}

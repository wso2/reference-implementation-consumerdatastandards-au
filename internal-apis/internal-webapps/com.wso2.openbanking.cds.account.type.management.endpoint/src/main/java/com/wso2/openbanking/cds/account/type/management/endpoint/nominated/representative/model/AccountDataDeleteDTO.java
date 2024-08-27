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
package org.wso2.openbanking.cds.account.type.management.endpoint.nominated.representative.model;

import java.util.List;
import javax.validation.constraints.NotEmpty;

/**
 * AccountDataDeleteDTO.
 */
public class AccountDataDeleteDTO {

    @NotEmpty(message = "accountID field cannot be empty")
    private String accountID;

    private List<String> accountOwners;

    private List<String> nominatedRepresentatives;

    // Default constructor for deserialization
    public AccountDataDeleteDTO() {
    }

    // Getters and setters for private fields
    public String getAccountID() {
        return accountID;
    }

    public void setAccountID(String accountID) {
        this.accountID = accountID;
    }

    public List<String> getAccountOwners() {
        return accountOwners;
    }

    public void setAccountOwners(List<String> accountOwners) {
        this.accountOwners = accountOwners;
    }

    public List<String> getNominatedRepresentatives() {
        return nominatedRepresentatives;
    }

    public void setNominatedRepresentatives(List<String> nominatedRepresentatives) {
        this.nominatedRepresentatives = nominatedRepresentatives;
    }
}

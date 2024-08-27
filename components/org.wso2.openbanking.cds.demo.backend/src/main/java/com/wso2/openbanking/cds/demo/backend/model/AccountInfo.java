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

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Account Information Object.
 */
public class AccountInfo implements Serializable {

    private static final long serialVersionUID = -5675538442439191042L;

    @SerializedName("CustomerId")
    private String customerId;
    @SerializedName("AccountIds")
    private String[] accountIds;
    @SerializedName("Permissions")
    private String[] permissions;

    public String getCustomerId() {

        return customerId;
    }

    public void setCustomerId(String customerId) {

        this.customerId = customerId;
    }

    public String[] getAccountIds() {

        if (accountIds == null) {
            return new String[]{};
        }
        return Arrays.copyOf(accountIds, accountIds.length);
    }

    public void setAccountIds(String[] accountIds) {

        this.accountIds = Arrays.copyOf(accountIds, accountIds.length);
    }

    public String[] getPermissions() {

        if (permissions == null) {
            return new String[]{};
        }
        return Arrays.copyOf(permissions, permissions.length);
    }

    public void setPermissions(String[] permissions) {

        this.permissions = Arrays.copyOf(permissions, permissions.length);
    }
}

/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.openbanking.consumerdatastandards.au.extensions.constants;

/**
 * Specifies the permissions as per Consumer Data Standards (CDS) Open Banking.
 */
public enum PermissionsEnum {

    READCUSTOMERDETAILSBASIC("common:customer.basic:read"),

    READCUSTOMERDETAILS("common:customer.detail:read"),

    CDRREADACCOUNTSBASIC("bank:accounts.basic:read"),

    CDRREADACCOUNTSDETAILS("bank:accounts.detail:read"),

    CDRREADTRANSACTION("bank:transactions:read"),

    CDRREADPAYMENTS("bank:regular_payments:read"),

    CDRREADPAYEES("bank:payees:read"),

    PROFILE("profile"),

    NAME("name"),

    GIVENNAME("given_name"),

    FAMILYNAME("family_name"),

    UPDATEDAT("updated_at"),

    EMAIL("email"),

    EMAILVERIFIED("email_verified"),

    PHONENUMBER("phone_number"),

    PHONENUMBERVERIFIED("phone_number_verified"),

    ADDRESS("address"),

    ACCOUNTS("accounts");

    private String value;

    PermissionsEnum(String value) {

        this.value = value;
    }

    public String toString() {

        return String.valueOf(value);
    }

    public static PermissionsEnum fromValue(String text) {

        for (PermissionsEnum enumValue : PermissionsEnum.values()) {
            if (String.valueOf(enumValue.value).equals(text)) {
                return enumValue;
            }
        }
        return null;
    }

    public static String fromName(String text) {

        for (PermissionsEnum enumValue : PermissionsEnum.values()) {
            if (enumValue.name().equals(text)) {
                return enumValue.value;
            }
        }
        return null;
    }
}

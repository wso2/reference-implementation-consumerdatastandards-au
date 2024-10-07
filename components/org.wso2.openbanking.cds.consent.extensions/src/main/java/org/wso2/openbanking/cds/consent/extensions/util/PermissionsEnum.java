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
package org.wso2.openbanking.cds.consent.extensions.util;

/**
 * PermissionsEnum enumeration
 */
public enum PermissionsEnum {

    READCUSTOMERDETAILSBASIC("READCUSTOMERDETAILSBASIC"),

    READCUSTOMERDETAILS("READCUSTOMERDETAILS"),

    CDRREADACCOUNTSBASIC("CDRREADACCOUNTSBASIC"),

    CDRREADACCOUNTSDETAILS("CDRREADACCOUNTSDETAILS"),

    CDRREADTRANSACTION("CDRREADTRANSACTION"),

    CDRREADPAYMENTS("CDRREADPAYMENTS"),

    CDRREADPAYEES("CDRREADPAYEES"),

    PROFILE("PROFILE"),

    NAME("NAME"),

    GIVENNAME("GIVENNAME"),

    FAMILYNAME("FAMILYNAME"),

    UPDATEDAT("UPDATEDAT"),

    EMAIL("EMAIL"),

    EMAILVERIFIED("EMAILVERIFIED"),

    PHONENUMBER("PHONENUMBER"),

    PHONENUMBERVERIFIED("PHONENUMBERVERIFIED"),

    ADDRESS("ADDRESS");

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

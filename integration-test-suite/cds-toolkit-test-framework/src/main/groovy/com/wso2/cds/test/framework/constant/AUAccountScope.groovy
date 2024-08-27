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

package com.wso2.cds.test.framework.constant

/**
 * Enum class for keeping account scopes
 */
enum AUAccountScope {

    BANK_ACCOUNT_BASIC_READ("bank:accounts.basic:read"),
    BANK_ACCOUNT_DETAIL_READ("bank:accounts.detail:read"),
    BANK_TRANSACTION_READ("bank:transactions:read"),
    BANK_PAYEES_READ("bank:payees:read"),
    BANK_REGULAR_PAYMENTS_READ("bank:regular_payments:read"),
    BANK_CUSTOMER_BASIC_READ("common:customer.basic:read"),
    BANK_CUSTOMER_DETAIL_READ("common:customer.detail:read"),
    CDR_REGISTRATION("cdr:registration"),
    ADMIN_METRICS_BASIC_READ("admin:metrics.basic:read"),
    ADMIN_METADATA_UPDATE("admin:metadata:update"),
    PROFILE("profile")

    private String value

    AUAccountScope(String value) {
        this.value = value
    }

    String getScopeString() {
        return this.value
    }

}


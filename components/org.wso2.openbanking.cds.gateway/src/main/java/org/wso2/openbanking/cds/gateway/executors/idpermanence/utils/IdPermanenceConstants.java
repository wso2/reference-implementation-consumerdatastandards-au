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

package org.wso2.openbanking.cds.gateway.executors.idpermanence.utils;

import com.google.common.collect.ImmutableList;

/**
 * Constants required for IdPermanenceHandler and IdPermanenceUtils.
 */
public class IdPermanenceConstants {

    // resourceID keys
    public static final String ACCOUNT_ID = "accountId";
    public static final String ACCOUNT_IDS = "accountIds";
    public static final String TRANSACTION_ID = "transactionId";
    public static final String SCHEDULED_PAYMENT_ID = "scheduledPaymentId";
    public static final String PAYEE_ID = "payeeId";

    // keys of response payload
    public static final String DATA = "data";
    public static final String LINKS = "links";
    public static final String SCHEDULED_PAYMENTS = "scheduledPayments";
    public static final String LOAN = "loan";
    public static final String OFFSET_ACCOUNT_IDS = "offsetAccountIds";
    public static final String FROM = "from";
    public static final String PAYMENT_SET = "paymentSet";
    public static final String TO = "to";
    public static final String REQUEST_BODY = "request body";

    // Header Parameters
    public static final String DECRYPTED_SUB_REQUEST_PATH = "decrypted-sub-req-path";

    //Context Properties
    public static final String ENCRYPTED_ID_MAPPING = "encrypted-id-mapping";

    // response URLs
    public static final ImmutableList<String> RESOURCE_LIST_RES_URLS = ImmutableList.of(
            "/banking/accounts", "/banking/accounts/balances", "/banking/accounts/{accountId}/transactions",
            "/banking/accounts/{accountId}/direct-debits", "/banking/accounts/direct-debits", "/banking/payees");

    public static final ImmutableList<String> SINGLE_RESOURCE_RES_URLS = ImmutableList.of(
            "/banking/accounts/{accountId}/balance", "/banking/accounts/{accountId}",
            "/banking/accounts/{accountId}/transactions/{transactionId}", "/banking/payees/{payeeId}");

    public static final ImmutableList<String> SCHEDULED_PAYMENT_LIST_RES_URLS = ImmutableList.of(
            "/banking/accounts/{accountId}/payments/scheduled", "/banking/payments/scheduled");

    public static final ImmutableList<String> REQUEST_URLS_WITH_PATH_PARAMS = ImmutableList.of(
            "/banking/accounts/{accountId}/balance", "/banking/accounts/{accountId}",
            "/banking/accounts/{accountId}/transactions", "/banking/accounts/{accountId}/transactions/{transactionId}",
            "/banking/accounts/{accountId}/direct-debits", "/banking/accounts/{accountId}/payments/scheduled",
            "/banking/payees/{payeeId}");

    // regex patterns
    public static final String DECRYPTED_RESOURCE_ID_PATTERN = "^[^:]*:+[^:]*:[^:]*$";
    public static final String URL_TEMPLATE_PATH_PARAM_PATTERN = "(\\{.*?})";

}

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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This class holds common constants for the CDS Open Banking implementation.
 */
public class CommonConstants {

    public static final String USER_ID_KEY_NAME = "userID";
    public static final String SERVICE_URL_SLASH = "/";
    public static final String CHAR_SET = "UTF-8";
    public static final String ACCEPT_HEADER_NAME = "Accept";
    public static final String ACCEPT_HEADER_VALUE = "application/json";
    public static final String IS_SHARING_DURATION_UPDATED = "isSharingDurationUpdated";
    public static final String IS_CONSENT_AMENDMENT = "isConsentAmendment";
    public static final String OPENID_SCOPE = "openid";
    public static final String PROFILE_SCOPE = "profile";
    public static final String CDR_REGISTRATION_SCOPE = "cdr:registration";
    public static final String HTTP_METHOD = "httpMethod";
    public static final String POST_METHOD = "POST";
    public static final String CLIENT_ID = "client_id";
    public static final String IS_ERROR = "isError";
    public static final String CLAIMS = "claims";
    public static final String SHARING_DURATION = "sharing_duration";
    public static final String EXPIRATION_DATE_TIME = "expirationDateTime";
    public static final String SHARING_DURATION_VALUE = "sharing_duration_value";
    public static final String CDR_ARRANGEMENT_ID = "cdr_arrangement_id";
    public static final String ID_TOKEN = "id_token";
    public static final String ID_TOKEN_CLAIMS = "id_token_claims";
    public static final String USERINFO = "userinfo";
    public static final String USERINFO_CLAIMS = "userinfo_claims";
    public static final String INVALID_REQUEST_MSG = "invalid_request";
    public static final List<String> NAME_CLUSTER_CLAIMS = Collections.unmodifiableList(Arrays.asList("name",
            "given_name", "family_name", "updated_at"));
    public static final List<String> NAME_CLUSTER_PERMISSIONS = Collections.unmodifiableList(Arrays.asList("profile",
            "name", "given_name", "family_name", "updated_at"));
    public static final List<String> EMAIL_CLUSTER_CLAIMS = Collections.unmodifiableList(Arrays.asList("email",
            "email_verified"));
    public static final List<String> MAIL_CLUSTER_CLAIMS = Collections.unmodifiableList(Arrays.asList("address"));
    ;
    public static final List<String> PHONE_CLUSTER_CLAIMS = Collections.unmodifiableList(Arrays.asList("phone_number",
            "phone_number_verified"));
    public static final String NAME_CLUSTER = "name";
    public static final String EMAIL_CLUSTER = "email";
    public static final String MAIL_CLUSTER = "mail";
    public static final String PHONE_CLUSTER = "phone";
    public static final String CONTACT_CLUSTER = "contactDetails";
    public static final String NAME_CLAIMS = "nameClaims";
    public static final String CONTACT_CLAIMS = "contactClaims";
    public static final String TITLE = "title";
    public static final String DATA = "data";
    public static final String PERMISSION_TITLE = "Permissions";
    public static final String EXPIRATION_DATE_TITLE = "Expiration Date Time";
    public static final String CONSENT_DATA = "consentData";
    public static final String PERMISSIONS = "permissions";
    //TODO: Value Should be CDR_ACCOUNTS
    public static final String CDR_ACCOUNTS = "ACCOUNTS";

    public static final String COMMON_CUSTOMER_BASIC_READ_SCOPE = "common:customer.basic:read";
    public static final String COMMON_CUSTOMER_DETAIL_READ_SCOPE = "common:customer.detail:read";
    public static final String COMMON_ACCOUNTS_BASIC_READ_SCOPE = "bank:accounts.basic:read";
    public static final String COMMON_ACCOUNTS_DETAIL_READ_SCOPE = "bank:accounts.detail:read";
    public static final String TRANSACTIONS_READ_SCOPE = "bank:transactions:read";
    public static final String REGULAR_PAYMENTS_READ_SCOPE = "bank:regular_payments:read";
    public static final String COMMON_SUBSTRING = "common:";
    public static final String CUSTOMER_SCOPES_ONLY = "customerScopesOnly";
    public static final String SP_FULL_NAME = "sp_full_name";
    public static final String ACCOUNTS = "accounts";
    public static final String TYPE = "type";
    public static final String REJECTED_STATUS = "rejected";
    public static final String AUTHORIZED_STATUS = "authorised";
    public static final String ACTIVE_MAPPING_STATUS = "active";
    public static final String INACTIVE_MAPPING_STATUS = "inactive";
    public static final String ACCOUNT_IDS = "accountIds";
    public static final String BASIC_CONSENT_DATA = "basicConsentData";
    public static final String CONSENT_METADATA = "consentMetadata";
    public static final String ALLOW_MULTIPLE_ACCOUNTS = "allowMultipleAccounts";
    public static final Integer DEFAULT_FREQUENCY = 0;
    public static final String VALIDITY_TIME_TITLE = "VALIDITY_TIME";
    public static final String RESOURCE_PARAMS = "resourceParams";
    public static final String RESOURCE = "resource";
    public static final String RESOURCE_PATH = "ResourcePath";
    public static final String ELECTED_RESOURCE = "electedResource";
    public static final String RECEIPT = "receipt";
    public static final String ACCOUNT_REGEX = "/accounts";
    public static final String PRODUCTS_REGEX = "/products";
    public static final String PRODUCT_DETAILS_REGEX = "/products/[^/?]*";
    public static final String ACCOUNT_DETAILS_REGEX = "/accounts/[^/?]*";
    public static final String BALANCES_REGEX = "/accounts/balances";
    public static final String BALANCES_ID_REGEX = "/accounts/[^/?]*/balance";
    public static final String TRANSACTIONS_ID_REGEX = "/accounts/[^/?]*/transactions";
    public static final String TRANSACTION_DETAILS_REGEX = "/accounts/[^/?]*/transactions/[^/?]*";
    public static final String DIRECT_DEBITS_ID_REGEX = "/accounts/[^/?]*/direct-debits";
    public static final String DIRECT_DEBITS_REGEX = "/accounts/direct-debits";
    public static final String SCHEDULED_PAYMENTS_ID_REGEX = "/accounts/[^/?]*/payments/scheduled";
    public static final String SCHEDULED_PAYMENTS_REGEX = "/payments/scheduled";
    public static final String PAYEES_REGEX = "/payees";
    public static final String PAYEES_ID_REGEX = "/payees/[^/?]*";
    public static final String ACCOUNT_DATA = "accountData";
    public static final String STATUS = "status";

}

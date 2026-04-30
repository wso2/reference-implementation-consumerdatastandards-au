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

package org.wso2.openbanking.consumerdatastandards.account.metadata.configurations;

/**
 * This class holds configurable properties for the CDS Open Banking implementation.
 */
public class ConfigurableProperties {

    public static final String ACCOUNT_METADATA_DATASOURCE_JNDI_NAME = "jdbc/ACCOUNT_METADATA_DB";

    /** User credentials for authenticating against the IS server when resolving a client ID to its legal entity ID. */
    public static final String IS_USERNAME = "<IS_ADMIN_USERNAME>";
    public static final String IS_PASSWORD = "<IS_ADMIN_PASSWORD>";
    public static final String IS_APPLICATIONS_ENDPOINT = "https://<IS_HOST>:9446/api/server/v1/applications";

    // Accelerator token endpoint
    public static final String ACCELERATOR_TOKEN_ENDPOINT_URL =
            "https://<IS_HOST>:9446/oauth2/token";

    // Accelerator consent admin search endpoint
    public static final String ACCELERATOR_CONSENT_SEARCH_URL =
            "https://<IS_HOST>:9446/api/fs/consent/admin/search";

    // OAuth2 client credentials (Basic Auth for token request)
    public static final String ACCOUNT_METADATA_CLIENT_APP_ID = "<ACCOUNT_METADATA_CLIENT_APP_ID>";
    public static final String ACCOUNT_METADATA_CLIENT_APP_SECRET =
            "<ACCOUNT_METADATA_CLIENT_APP_SECRET>";

    // Resource-owner credentials and scope for password grant
    public static final String CUSTOMER_CARE_OFFICER_USERNAME = "<CUSTOMER_CARE_OFFICER_USERNAME>";
    public static final String CUSTOMER_CARE_OFFICER_PASSWORD = "<CUSTOMER_CARE_OFFICER_PASSWORD>";
    public static final String CUSTOMER_CARE_OFFICER_TOKEN_SCOPE = "consents:read_all";

    // Accelerator consent update (expire) endpoint base URL
    public static final String ACCELERATOR_CONSENT_UPDATE_BASE_URL =
            "https://<IS_HOST>:9446/api/fs/consent/manage/account-access-consents";

    // Admin credentials for Basic Auth on the consent update endpoint
    public static final String IS_ADMIN_USERNAME = "<IS_ADMIN_USERNAME>";
    public static final String IS_ADMIN_PASSWORD = "<IS_ADMIN_PASSWORD>";

    // HTTP timeouts (ms)
    public static final int CONNECT_TIMEOUT_MILLIS = 5000;
    public static final int SOCKET_TIMEOUT_MILLIS = 10000;
}

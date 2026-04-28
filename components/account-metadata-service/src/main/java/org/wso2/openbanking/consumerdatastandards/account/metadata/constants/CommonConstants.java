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

package org.wso2.openbanking.consumerdatastandards.account.metadata.constants;

/**
 * This class holds common constants for the CDS Open Banking implementation.
 */
public class CommonConstants {

    // Constants Related to Joint Accounts
    public static final String DOMS_STATUS_PRE_APPROVAL = "pre-approval";
    public static final String DOMS_STATUS_NO_SHARING = "no-sharing";

    // Constants Related to Secondary Accounts
    public static final String SUI_ACTIVE_STATUS = "active";
    public static final String SUI_INACTIVE_STATUS = "inactive";
    public static final String LEGAL_ENTITY_SHARING_STATUS_BLOCKED = "blocked";
    public static final String LEGAL_ENTITY_SHARING_STATUS_ACTIVE = "active";

    // Constants Related to Business Stakeholders
    public static final String BNR_PERMISSION_AUTHORIZE = "AUTHORIZE";
    public static final String BNR_PERMISSION_REVOKE = "REVOKE";
    public static final String BNR_PERMISSION_VIEW = "VIEW";

    // IS Applications endpoint constants
    public static final String FILTER_TAG = "filter";
    public static final String ATTRIBUTES_TAG = "attributes";
    public static final String CLIENT_ID_FILTER_PREFIX = "clientId eq ";
    public static final String ADVANCED_CONFIGURATIONS_TAG = "advancedConfigurations";
    public static final String APPLICATIONS_TAG = "applications";
    public static final String ADDITIONAL_SP_PROPERTIES_TAG = "additionalSpProperties";
    public static final String PROPERTY_NAME_TAG = "name";
    public static final String PROPERTY_VALUE_TAG = "value";
    public static final String LEGAL_ENTITY_ID_PROPERTY_NAME = "legal_entity_id";

    // HTTP constants
    public static final String AUTH_HEADER = "Authorization";
    public static final String BASIC_TAG = "Basic ";
    public static final String JSON_CONTENT_TYPE = "application/json";
    public static final String ACCEPT_TAG = "Accept";

    // HTTP headers
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String CONTENT_TYPE_HEADER = "Content-Type";
    public static final String ACCEPT_HEADER = "Accept";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String BASIC_PREFIX = "Basic ";
    public static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";
    public static final String CONTENT_TYPE_JSON = "application/json";

    // OAuth2 form parameter names
    public static final String PARAM_GRANT_TYPE = "grant_type";
    public static final String PARAM_USERNAME = "username";
    public static final String PARAM_PASSWORD = "password";
    public static final String PARAM_SCOPE = "scope";
    public static final String GRANT_TYPE_PASSWORD = "password";

    // Token response JSON field
    public static final String TOKEN_RESPONSE_ACCESS_TOKEN = "access_token";

    // Consent search query parameter names
    public static final String CONSENT_SEARCH_PARAM_CONSENT_TYPES = "consentTypes";
    public static final String CONSENT_SEARCH_PARAM_USER_IDS = "userIds";
    public static final String CONSENT_TYPE_ACCOUNTS = "accounts";

    // Consent search response JSON field names
    // Verify these against the live API response and adjust constants if the shape differs.
    public static final String CONSENT_RESPONSE_DATA = "data";
    public static final String CONSENT_RESPONSE_CONSENT_ID = "consentId";
    public static final String CONSENT_RESPONSE_CLIENT_ID  = "clientId";

    // Consent update (expire) request headers
    public static final String HEADER_WSO2_CLIENT_ID = "x-wso2-client-id";
    public static final String HEADER_WSO2_INTERNAL_REQUEST = "x-wso2-internal-request";
    public static final String HEADER_FAPI_INTERACTION_ID  = "x-fapi-interaction-id";
    public static final String WSO2_INTERNAL_REQUEST_VALUE = "true";

    // Consent update request body field names
    public static final String CONSENT_UPDATE_STATUS = "status";
    public static final String CONSENT_EXPIRE_STATUS = "Expired";

}

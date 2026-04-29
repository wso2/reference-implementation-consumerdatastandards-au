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

package org.wso2.openbanking.consumerdatastandards.au.policy.constants;

/**
 * Constants class for the Consent Enforcement Policy.
 */
public class CDSAccountValidationConstants {

    public static final String AUTH_HEADER = "Authorization";
    public static final String BASIC_TAG = "Basic ";
    public static final String JSON_CONTENT_TYPE = "application/json";
    public static final String INFO_HEADER_TAG = "Account-Request-Information";
    public static final String ACCEPT_TAG = "Accept";

    // Configs
    public static final String KEYSTORE_LOCATION_TAG = "Security.InternalKeyStore.Location";
    public static final String KEYSTORE_PASSWORD_TAG = "Security.InternalKeyStore.Password";
    public static final String SIGNING_ALIAS_TAG = "Security.InternalKeyStore.KeyAlias";
    public static final String SIGNING_KEY_PASSWORD = "Security.InternalKeyStore.KeyPassword";

    // Additional param keys
    public static final String USER_ID_TAG = "userId";
    public static final String CLIENT_ID_TAG = "clientId";
    public static final String CLIENT_ID_SNAKE_CASE_TAG = "client_id";
    public static final String AUTH_RESOURCES_TAG = "authorizationResources";
    public static final String CONSENT_MAPPING_RESOURCES_TAG = "consentMappingResources";
    public static final String AUTH_TYPE_TAG = "authorizationType";
    public static final String AUTH_ID_TAG = "authorizationId";
    public static final String ACCELERATOR_ACCOUNT_ID_TAG = "account_id";
    public static final String CDS_ACCOUNT_ID_TAG = "accountId";
    public static final String PRIMARY_AUTH_TYPE_TAG = "primary_member";

    // Error constants
    public static final String ERROR_CODE = "ERROR_CODE";
    public static final String ERROR_TITLE = "ERROR_TITLE";
    public static final String ERROR_DESCRIPTION = "ERROR_DESCRIPTION";
    public static final String CUSTOM_HTTP_SC = "CUSTOM_HTTP_SC";

    // Single-account validation error constants
    public static final String RESOURCE_INVALID_BANKING_ACCOUNT =
            "urn:au-cds:error:cds-banking:Authorisation/InvalidBankingAccount";
    public static final String INVALID_BANKING_ACCOUNT_TITLE = "Invalid Banking Account";
    public static final String INVALID_BANKING_ACCOUNT_DESC =
            "The accountId requested is not available for data sharing";
    public static final String HTTP_SC_404 = "404";

    // Constants related to DOMS
    public static final String ACCOUNT_IDS_TAG = "accountIds";
    public static final String LINKED_MEMBER_TAG = "linked_member";
    public static final String DISCLOSURE_OPTION_TAG = "disclosureOption";
    public static final String DOMS_STATUS_NO_SHARING = "no-sharing";

    // Account-metadata webapp API paths
    public static final String DISCLOSURE_OPTIONS_PATH = "/disclosure-options";
    public static final String SECONDARY_ACCOUNTS_PATH = "/secondary-accounts";
    public static final String BUSINESS_STAKEHOLDERS_PATH = "/business-stakeholders";
    public static final String LEGAL_ENTITY_SHARING_PATH = "/legal-entity";

    // Legal entity sharing response fields
    public static final String LEGAL_ENTITY_SHARING_STATUS_TAG = "legalEntitySharingStatus";
    public static final String LEGAL_ENTITY_SHARING_STATUS_BLOCKED = "blocked";
    public static final String ACCOUNT_ID_UPPER_CASE_TAG = "accountID";

    // Constants related to Secondary Accounts
    public static final String SECONDARY_ACCOUNT_INSTRUCTION_STATUS_TAG = "secondaryAccountInstructionStatus";
    public static final String SECONDARY_ACCOUNT_STATUS_INACTIVE = "inactive";
    public static final String SECONDARY_INDIVIDUAL_ACCOUNT_OWNER_TAG = "secondary_individual_account_owner";
    public static final String SECONDARY_JOINT_ACCOUNT_OWNER_TAG = "secondary_joint_account_owner";

    // Message context properties
    public static final String API_ELECTED_RESOURCE = "API_ELECTED_RESOURCE";
    public static final String REST_FULL_REQUEST_PATH = "REST_FULL_REQUEST_PATH";
    public static final String API_ELECTED_RESOURCE_ACCOUNT_ID_PARAMETER = "{accountId}";
    public static final String RESOURCE_ACCOUNTS_TAG = "accounts";

    // Constants related to Business Accounts
    public static final String NOMINATED_REPRESENTATIVE_TAG = "nominated_representative";
    public static final String BUSINESS_ACCOUNT_OWNER_TAG = "business_account_owner";
    public static final String BUSINESS_PERMISSION_TAG = "permission";
    public static final String BUSINESS_PERMISSION_AUTHORIZE = "AUTHORIZE";

    // HTTP Method
    public static final String REST_METHOD = "REST_METHOD";
    public static final String POST_METHOD = "POST";

    // POST request body property key (synapse messageContext property name)
    public static final String ORIGINAL_REQUEST_JSON_BODY = "originalRequestJsonBody";

    // POST payload field names
    public static final String DATA_TAG = "data";
    public static final String POST_PAYLOAD_ACCOUNT_IDS_TAG = "accountIds";

    // HTTP Status Codes (422 is missing)
    public static final String HTTP_SC_422 = "422";

    // POST error description (reuse existing RESOURCE_INVALID_BANKING_ACCOUNT and INVALID_BANKING_ACCOUNT_TITLE)
    public static final String INVALID_BANKING_ACCOUNT_POST_DESC =
        "ID of the account not found or invalid";

    // Timeouts
    public static final int HTTP_CLIENT_CONNECT_TIMEOUT_MILLIS = 5000;
    public static final int HTTP_REQUEST_TIMEOUT_MILLIS = 10000;
}

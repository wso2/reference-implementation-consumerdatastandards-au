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

package org.wso2.openbanking.cds.consent.extensions.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CDS Consent Extension Constant class.
 */
public class CDSConsentExtensionConstants {

    public static final String TITLE = "title";
    public static final String DATA = "data";
    public static final String CONSENT_MAPPING_RESOURCES = "consentMappingResources";
    public static final String AUTHORIZATION_RESOURCES = "authorizationResources";
    public static final String PERMISSION = "permission";
    public static final String USER_ID = "userId";
    public static final String AUTHORIZATION_ID = "authorizationId";
    public static final String CONSENT_DATA = "consentData";
    public static final String ACCOUNT_DATA = "accountData";
    public static final String ACCOUNTS_DATA = "accounts_data";
    public static final String ACCOUNT_ID = "accountId";
    public static final String ACCOUNT_ID_DISPLAYABLE = "accountIdToDisplay";
    public static final String DISPLAY_NAME = "displayName";
    public static final String PRE_ACCOUNTS_DATA = "pre_accounts_data";
    public static final String REDIRECT_URL = "redirectURL";
    public static final String STATE = "state";
    public static final String EXPIRATION_DATE_TITLE = "Expiration Date Time";
    public static final String PERMISSION_TITLE = "Permissions";
    public static final String EXPIRATION_DATE_TIME = "expirationDateTime";
    public static final String PERMISSIONS = "permissions";
    public static final String SHARING_DURATION_VALUE = "sharing_duration_value";
    public static final String CDR_ARRANGEMENT_ID = "cdr_arrangement_id";
    public static final String CDR_ARRANGEMENT_JWT = "cdr_arrangement_jwt";
    public static final String AUTH_RESOURCE_ID = "authResourceId";
    public static final String AUTH_TYPE = "authorizationType";
    public static final String MAPPING_STATUS = "mappingStatus";
    public static final String AUTH_RESOURCE_STATUS = "authResourceStatus";
    public static final String CDR_ACCOUNTS = "CDR_ACCOUNTS";
    public static final String OPENID_SCOPES = "openid_scopes";
    public static final String CLIENT_ID = "client_id";
    public static final String SP_FULL_NAME = "sp_full_name";
    public static final String CLAIMS = "claims";
    public static final String IS_ERROR = "isError";
    public static final String SHARING_DURATION = "sharing_duration";
    public static final String ACCOUNTS = "accounts";
    public static final String CREATED_STATUS = "created";
    public static final String REJECTED_STATUS = "rejected";
    public static final String AUTHORIZED_STATUS = "authorized";
    public static final String AWAITING_AUTH_STATUS = "awaitingAuthorization";
    public static final String COMMON_AUTH_ID = "commonAuthId";
    public static final String METADATA = "metadata";
    public static final String ACCOUNT_IDS = "accountIds";
    public static final String ACCOUNTS_ARRAY = "accounts[]";
    public static final String ZERO = "0";
    public static final String CONSENT_EXPIRY = "consent_expiration";
    public static final String ACCOUNT_MASKING_ENABLED = "account_masking_enabled";
    public static final String USER_ID_KEY_NAME = "userID";
    public static final String USER_IDS_QUERY_PARAM_NAME = "userIDs";
    public static final String CONSENT_IDS_QUERY_PARAM_NAME = "consentIDs";
    public static final String CONSENT_STATUS_REVOKED = "revoked";
    public static final String CONSENT_ATTRIBUTES = "consentAttributes";
    public static final String INCLUDE_SHARING_DATES = "includeSharingDates";
    public static final String CONSENT_ID = "consentId";
    public static final String RECEIPT = "receipt";
    public static final String PERMISSIONS_WITH_SHARING_DATE = "permissionsWithSharingDate";

    // Joint account constants
    public static final String IS_JOINT_ACCOUNT_RESPONSE = "isJointAccount";
    public static final String IS_JOINT_ACCOUNT = "is_joint_account";
    public static final String IS_SELECTABLE = "is_selectable";
    public static final String JOINT_ACCOUNT_INFO = "jointAccountinfo";
    public static final String JOINT_ACCOUNT_CONSENT_ELECTION_STATUS = "jointAccountConsentElectionStatus";
    public static final String JOINT_ACCOUNT_PRE_APPROVAL = "ELECTED";
    public static final String JOINT_ACCOUNT_NO_SHARING = "UNAVAILABLE";
    public static final String DOMS_STATUS = "DISCLOSURE_OPTIONS_STATUS";
    public static final String DOMS_STATUS_PRE_APPROVAL = "pre-approval";
    public static final String LINKED_MEMBER = "linkedMember";
    public static final String LINKED_MEMBERS_COUNT = "linked_members_count";
    public static final String LINKED_MEMBER_ID = "memberId";
    public static final String AUTH_RESOURCE_TYPE_PRIMARY = "primary_member";
    public static final String AUTH_RESOURCE_TYPE_LINKED = "linked_member";
    public static final String NON_PRIMARY_ACCOUNT_ID_AGAINST_USERS_MAP = "nonPrimaryAccountAgainstUsers";
    public static final String USER_ID_AGAINST_NON_PRIMARY_ACCOUNTS_MAP = "userAgainstNonPrimaryAccounts";
    public static final String NON_PRIMARY_ACCOUNT_ID_WITH_PERMISSIONS_MAP = "nonPrimaryAccountWithPermissions";
    public static final String LINKED_MEMBER_AUTH_TYPE = "linked_member";
    public static final String JOINT_ACCOUNTS_PAYLOAD = "joint_accounts_payload";
    public static final String PRE_SELECTED_ACCOUNT_LIST = "preSelectedAccountList";
    public static final String IS_PRE_SELECTED_ACCOUNT = "isPreSelectedAccount";
    public static final String EXISTING_PERMISSIONS = "existingPermissions";
    public static final String IS_SHARING_DURATION_UPDATED = "isSharingDurationUpdated";
    public static final String IS_CONSENT_AMENDMENT = "isConsentAmendment";
    public static final String SINGLE_ACCESS_CONSENT = "Single use consent";
    public static final String CHAR_SET = "UTF-8";
    public static final String CUSTOMER_TYPE = "customerUType";
    public static final String ORGANISATION = "Organisation";
    public static final String PERSON = "Person";
    public static final String DATA_REQUESTED = "data_requested";
    public static final String NEW_DATA_REQUESTED = "new_data_requested";
    public static final String COMMON_CUSTOMER_BASIC_READ_SCOPE = "common:customer.basic:read";
    public static final String COMMON_CUSTOMER_DETAIL_READ_SCOPE = "common:customer.detail:read";
    public static final String COMMON_ACCOUNTS_BASIC_READ_SCOPE = "bank:accounts.basic:read";
    public static final String COMMON_ACCOUNTS_DETAIL_READ_SCOPE = "bank:accounts.detail:read";
    public static final String TRANSACTIONS_READ_SCOPE = "bank:transactions:read";
    public static final String REGULAR_PAYMENTS_READ_SCOPE = "bank:regular_payments:read";
    public static final String COMMON_SUBSTRING = "common:";
    public static final Map<String, Map<String, List<String>>> CDS_DATA_CLUSTER;
    public static final Map<String, Map<String, List<String>>> BUSINESS_CDS_DATA_CLUSTER;
    public static final Map<String, Map<String, List<String>>> INDIVIDUAL_CDS_DATA_CLUSTER;
    public static final Map<String, Map<String, List<String>>> PROFILE_DATA_CLUSTER;

    //Nominated Representative Constants
    public static final String BUSINESS_ACCOUNT_INFO = "businessAccountInfo";
    public static final String ACCOUNT_OWNERS = "AccountOwners";
    public static final String NOMINATED_REPRESENTATIVES = "NominatedRepresentatives";
    public static final String MEMBER_ID = "memberId";
    public static final String BUSINESS_ACCOUNT_OWNER = "business_account_owner";
    public static final String NOMINATED_REPRESENTATIVE = "nominated_representative";
    public static final String CUSTOMER_ACCOUNT_TYPE = "customerAccountType";
    public static final String IS_ELIGIBLE = "isEligible";
    public static final String BNR_PERMISSION = "bnr-permission";
    public static final String BNR_AUTHORIZE_PERMISSION = "AUTHORIZE";
    public static final String BNR_VIEW_PERMISSION = "VIEW";
    public static final String BNR_REVOKE_PERMISSION = "REVOKE";
    public static final String CAN_REVOKE = "can_revoke";

    //Multi Profile Constants
    public static final String INDIVIDUAL_PROFILE_TYPE = "Individual";
    public static final String INDIVIDUAL_PROFILE_ID = "individual_profile";
    public static final String ORGANISATION_PROFILE_ID = "organisation_profile";
    public static final String BUSINESS_PROFILE_TYPE = "Business";
    public static final String PROFILE_ID = "profileId";
    public static final String PROFILE_NAME = "profileName";
    public static final String PRE_SELECTED_PROFILE_ID = "preSelectedProfileId";
    public static final String CUSTOMER_PROFILES_ATTRIBUTE = "customerProfiles";
    public static final String PROFILES_DATA_ATTRIBUTE = "profiles_data";
    public static final String SELECTED_PROFILE_ID = "selectedProfileId";
    public static final String SELECTED_PROFILE_NAME = "selectedProfileName";
    public static final String PROFILES = "profiles";
    public static final String CUSTOMER_PROFILE_TYPE = "customerProfileType";
    public static final String INDIVIDUAL_PROFILE_TYPE_ATTRIBUTE = "individual-profile";
    public static final String BUSINESS_PROFILE_TYPE_ATTRIBUTE = "business-profile";
    public static final String TOTAL = "total";
    public static final String COUNT = "count";
    public static final String DATA_CLUSTER = "data_cluster";
    public static final String NEW_DATA_CLUSTER = "new_data_cluster";
    public static final String BUSINESS_DATA_CLUSTER = "business_data_cluster";
    public static final String NEW_BUSINESS_DATA_CLUSTER = "new_business_data_cluster";

    public static final String ACCEPT_HEADER_NAME = "Accept";
    public static final String ACCEPT_HEADER_VALUE = "application/json";
    public static final String SERVICE_URL_SLASH = "/";

    public static final String TRUE = "true";
    public static final String ENABLE_CUSTOMER_DETAILS = "CustomerDetails.Enable";
    public static final String CUSTOMER_DETAILS_RETRIEVE_ENDPOINT = "CustomerDetails.CustomerDetailsRetrieveEndpoint";

    public static final String SHARABLE_ACCOUNTS_ENDPOINT = "ConsentManagement.SharableAccountsRetrieveEndpoint";

    public static final String OPENID_SCOPE = "openid";
    public static final String PROFILE_SCOPE = "profile";
    public static final String CDR_REGISTRATION_SCOPE = "cdr:registration";
    public static final String HTTP_METHOD = "httpMethod";
    public static final String POST_METHOD = "POST";
    public static final String ENABLE_ACCOUNT_ID_VALIDATION_ON_RETRIEVAL = "ConsentManagement.Validate" +
            "AccountIdOnRetrieval";

    // Constants related to secondary accounts
    public static final String INSTRUCTION_STATUS = "secondaryAccountInstructionStatus";
    public static final String SECONDARY_ACCOUNT_USER = "secondary_account_user";
    public static final String ACTIVE_STATUS = "active";
    public static final String IS_SECONDARY_ACCOUNT_RESPONSE = "isSecondaryAccount";
    public static final String IS_SECONDARY_ACCOUNT = "is_secondary_account";
    public static final String SECONDARY_ACCOUNT_PRIVILEGE_STATUS = "secondaryAccountPrivilegeStatus";
    public static final String SECONDARY_ACCOUNT_INFO = "secondaryAccountInfo";
    public static final String SECONDARY_ACCOUNT_OWNER_LIST = "accountOwner";
    public static final String SECONDARY_ACCOUNT_TYPE = "Secondary";
    public static final String ACCOUNT_USER = "secondaryAccountUser";
    public static final String ACCOUNT_OWNER = "secondaryAccountOwner";
    public static final String SECONDARY_ACCOUNTS = "secondaryAccounts";
    public static final String ACTIVE_ACCOUNTS = "activeAccounts";
    public static final String INACTIVE_ACCOUNTS = "inactiveAccounts";
    public static final String METADATA_KEY_BLOCKED_LEGAL_ENTITIES = "BLOCKED_LEGAL_ENTITIES";
    public static final String LEGAL_ENTITY_ID = "legal_entity_id";

    // DH revocation configs
    public static final String ENABLE_RECIPIENT_CONSENT_REVOCATION = "RecipientConsentRevocationEndpoint.Enable";
    public static final String DATA_HOLDER_ID = "DataHolder.ClientId";
    public static final String RECIPIENT_BASE_URI = "recipient_base_uri";

    // Constants related to profile scope and standard claims
    public static final String ID_TOKEN = "id_token";
    public static final String ID_TOKEN_CLAIMS = "id_token_claims";
    public static final String USERINFO = "userinfo";
    public static final String USERINFO_CLAIMS = "userinfo_claims";
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
    public static final Map<String, List<String>> CONTACT_CLUSTER_CLAIMS;
    public static final int CDS_DEFAULT_EXPIRY = 86400; // 1 day
    public static final String CUSTOMER_SCOPES_ONLY = "customerScopesOnly";

    // CDS data cluster
    static {
        Map<String, Map<String, List<String>>> dataCluster = new HashMap<>();

        Map<String, List<String>> permissionLanguage = new LinkedHashMap<>();
        permissionLanguage.put("Account name, type, and balance", Arrays.asList("Name of account", "Type of account",
                "Account balance"));
        dataCluster.put("bank:accounts.basic:read", permissionLanguage);


        permissionLanguage = new LinkedHashMap<>();
        permissionLanguage.put("Account balance and details", Arrays.asList("Name of account", "Type of account",
                "Account balance", "Account number", "Interest rates", "Fees", "Discounts", "Account terms",
                "Account mail address"));
        dataCluster.put("bank:accounts.detail:read", permissionLanguage);


        permissionLanguage = new LinkedHashMap<>();
        permissionLanguage.put("Transaction details", Arrays.asList("Incoming and outgoing transactions", "Amounts",
                "Dates", "Descriptions of transactions", "Who you have sent money to and received " +
                        "money from(e.g.their name)"));
        dataCluster.put("bank:transactions:read", permissionLanguage);


        permissionLanguage = new LinkedHashMap<>();
        permissionLanguage.put("Direct debits and scheduled payments", Arrays.asList("Direct debits", "Scheduled " +
                "payments"));
        dataCluster.put("bank:regular_payments:read", permissionLanguage);


        permissionLanguage = new LinkedHashMap<>();
        permissionLanguage.put("Saved payees", Arrays.asList("Names and details of accounts you have saved; (e.g. " +
                "their BSB and Account Number, BPay CRN and Biller code, or NPP PayID)"));
        dataCluster.put("bank:payees:read", permissionLanguage);

        CDS_DATA_CLUSTER = Collections.unmodifiableMap(dataCluster);
    }

    // Business data cluster
    static {
        Map<String, Map<String, List<String>>> dataCluster = new HashMap<>();

        Map<String, List<String>> permissionLanguage = new LinkedHashMap<>();
        permissionLanguage.put("Name", Collections.singletonList("Full name and title(s)"));
        dataCluster.put("name", permissionLanguage);

        permissionLanguage = new LinkedHashMap<>();
        permissionLanguage.put("Contact Details", Collections.singletonList("Email address"));
        dataCluster.put("contactDetails_email", permissionLanguage);

        permissionLanguage = new LinkedHashMap<>();
        permissionLanguage.put("Contact Details", Collections.singletonList("Mail address"));
        dataCluster.put("contactDetails_mail", permissionLanguage);

        permissionLanguage = new LinkedHashMap<>();
        permissionLanguage.put("Contact Details", Collections.singletonList("Phone number"));
        dataCluster.put("contactDetails_phone", permissionLanguage);

        permissionLanguage = new LinkedHashMap<>();
        permissionLanguage.put("Contact Details", Arrays.asList("Email address", "Mail address"));
        dataCluster.put("contactDetails_email_mail", permissionLanguage);

        permissionLanguage = new LinkedHashMap<>();
        permissionLanguage.put("Contact Details", Arrays.asList("Email address", "Phone number"));
        dataCluster.put("contactDetails_email_phone", permissionLanguage);

        permissionLanguage = new LinkedHashMap<>();
        permissionLanguage.put("Contact Details", Arrays.asList("Mail address", "Phone number"));
        dataCluster.put("contactDetails_mail_phone", permissionLanguage);

        permissionLanguage = new LinkedHashMap<>();
        permissionLanguage.put("Contact Details", Arrays.asList("Email address", "Mail address", "Phone number"));
        dataCluster.put("contactDetails_email_mail_phone", permissionLanguage);

        PROFILE_DATA_CLUSTER = Collections.unmodifiableMap(dataCluster);
    }

    static {
        Map<String, Map<String, List<String>>> dataCluster = new HashMap<>();

        Map<String, List<String>> permissionLanguage = new LinkedHashMap<>();
        permissionLanguage.put("Organisation profile", Arrays.asList("Agent name and role", "Organisation name",
                "Organisation numbers (ABN or ACN)", "Charity status", "Establishment date", "Industry",
                "Organisation type", "Country of registration"));
        dataCluster.put("common:customer.basic:read", permissionLanguage);

        permissionLanguage = new LinkedHashMap<>();
        permissionLanguage.put("Organisation profile and contact details", Arrays.asList("Agent name and role",
                "Organisation name", "Organisation numbers (ABN or ACN)", "Charity status", "Establishment date",
                "Industry", "Organisation type", "Country of registration", "Organisation address",
                "Mail address", "Phone number"));
        dataCluster.put("common:customer.detail:read", permissionLanguage);

        BUSINESS_CDS_DATA_CLUSTER = Collections.unmodifiableMap(dataCluster);
    }

    // Individual data cluster
    static {
        Map<String, Map<String, List<String>>> dataCluster = new HashMap<>();

        Map<String, List<String>> permissionLanguage = new LinkedHashMap<>();
        permissionLanguage.put("Name and occupation", Arrays.asList("Name", "Occupation"));
        dataCluster.put("common:customer.basic:read", permissionLanguage);

        permissionLanguage = new LinkedHashMap<>();
        permissionLanguage.put("Name, occupation, contact details", Arrays.asList("Name", "Occupation", "Phone",
                "Email address", "Mail address", "Residential address"));
        dataCluster.put("common:customer.detail:read", permissionLanguage);

        INDIVIDUAL_CDS_DATA_CLUSTER = Collections.unmodifiableMap(dataCluster);
    }

    static {
        Map<String, List<String>> contactClusters = new HashMap<>();
        contactClusters.put(CDSConsentExtensionConstants.EMAIL_CLUSTER,
                CDSConsentExtensionConstants.EMAIL_CLUSTER_CLAIMS);
        contactClusters.put(CDSConsentExtensionConstants.MAIL_CLUSTER,
                CDSConsentExtensionConstants.MAIL_CLUSTER_CLAIMS);
        contactClusters.put(CDSConsentExtensionConstants.PHONE_CLUSTER,
                CDSConsentExtensionConstants.PHONE_CLUSTER_CLAIMS);
        CONTACT_CLUSTER_CLAIMS = Collections.unmodifiableMap(contactClusters);
    }
}

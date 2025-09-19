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

package org.wso2.cds.test.framework.constant

import com.nimbusds.oauth2.sdk.pkce.CodeVerifier
import org.wso2.openbanking.test.framework.constant.OBConstants
import org.wso2.cds.test.framework.configuration.AUConfigurationService
import org.wso2.cds.test.framework.utility.AUTestUtil

/**
 * Class for provide AU and OB constants to the
 * AU layer and tests
 */
class AUConstants extends OBConstants {

    static AUConfigurationService auConfiguration = new AUConfigurationService()

    public static final String API_VERSION = auConfiguration.getCommonApiVersion()

    public static final String X_V_HEADER = "x-v"
    public static final String X_MIN_HEADER = "x-min-v"
    public static final String CDS_100_PATH = "/cds-au/v1"
    public static final String CDS_PATH = "/cds-au/v1"
    public static final String CDS_COMMON_PATH = "/cds-au-common/v1"
    public static final String CDS_ADMIN_PATH = "/cds-au/v1"
    public static final String ACCEPT = "application/json"
    public static final String X_FAPI_INTERACTION_ID = "x-fapi-interaction-id"
    public static final String CONTENT_TYPE = "Content-Type"
    public static final String X_FAPI_AUTH_DATE = "x-fapi-auth-date"
    public static final String X_CDS_CLIENT_HEADERS = "x-cds-client-headers"
    public static final String AUTHORIZATION_BEARER_TAG = "Bearer ";
    public static final String X_FAPI_CUSTOMER_IP_ADDRESS = "x-fapi-customer-ip-address"
    public static final String UUID = AUTestUtil.generateUUID()
    public static final String DATE = AUTestUtil.getDateAndTime()
    public static final String DATE_FORMAT = AUTestUtil.getDate()
    public static final String CONSENT_EXPIRE_DATE = AUTestUtil.getTommorowDateAndTime()
    public static final String IP = "2001:0db8:85a3:0000:0000:8a2e:0370:7334"
    public static final String PRODUCT_CATEGORY = "product-category"
    public static final String PRODUCT = "product-category"
    public static final String OPEN_STATUS_PARAM = "open-status"
    public static final String STATUS_OPEN = "OPEN"
    public static final String STATUS_CLOSED = "CLOSED"
    public static final String TOKEN = "d94c5b2e-b615-366e-862b-374b429e4d5e"
    public static final String accountID = "qu4WMZ-59LsndgjMN-kikHgbJzws-clthVMQELUH9BMhTt6fOc80bgAI1HN82kKCsFWl9OMhDKA3Wck1IMX2Q1qQy9Sykg5kJOSYgFEnjFU"
    public static final String accountID2 = "qu4WMZ-59LsndgjMN-kikHgbJzws-clthVMQELUH9BMhTt6fOc80bgAI1HN82kKCZKqCobYWnFNnWih_ukgKZmidyLDUN-43WATQRbw0VSI"
    public static final String jointAccountID = "qu4WMZ-59LsndgjMN-kikHdjzaZChLicltzPvkKrL13hrPrA_NE_6Oklcjp8a2saw3tFqwqv0HPeh7CCg4EBMZpW5_PdscIMToQHtalBGYU"
    public static final String businessAccountID = "7ZFnnKruJ7WX52D-vhD4ygZJm07cm-EkVGA87OvIQfSH8yRdrJWxwtRGmHRA-RtlCxLPLQWcNkKsfYc97yqYgQPRrZPKSwrJF3wuJEXUg3g"
    public static final String GET_META = "/admin/register/metadata"
    public static final String GET_STAT = "/admin/metrics"
    public static final String X_FAPI_FINANCIAL_ID = "x-fapi-financial-id"
    public static final String USERNAME = "admin@wso2.com"
    public static final String ADMIN_PASSWORD = "wso2123"
    public static final String ACCESS_TOKEN = "access_token"
    public static final String CONTENT = "application/x-www-form-urlencoded"
    public static final String CODE = "code"
    public static final String ERROR_INVALID_SOFTWARE_PRODUCT = "Invalid Software Product"

    public static final long DEFAULT_SHARING_DURATION = 172800
    public static final long SINGLE_ACCESS_CONSENT = 0
    public static final long ONE_YEAR_DURATION = 31536200
    public static final long NEGATIVE_DURATION = -3000
    public static final long AMENDED_SHARING_DURATION = 150000
    public static final long SHORT_SHARING_DURATION = 84600
    static final long NEGATIVE_SHARING_DURATION = -3000
    public static final long SHARING_DURATION_24H = 86400

    public static final String BULK_ACCOUNT_PATH = CDS_PATH + "/banking/accounts"
    public static final String SINGLE_ACCOUNT_PATH = CDS_PATH + "/banking/accounts/" + accountID
    public static final String BANKING_PRODUCT_PATH = CDS_PATH + "/banking/products"
    public static final String BULK_BALANCES_PATH = CDS_PATH + "/banking/accounts/balances"
    public static final String ACCOUNT_BALANCE_PATH = SINGLE_ACCOUNT_PATH + "/balance"
    public static final String BULK_DIRECT_DEBITS_PATH = CDS_PATH + "/banking/accounts/direct-debits"
    public static final String BULK_SCHEDULE_PAYMENTS_PATH = CDS_PATH + "/banking/payments/scheduled"
    public static final String BULK_PAYEES = CDS_PATH + "/banking/payees"
    public static final String BULK_CUSTOMER = CDS_PATH + "/common/customer"
    public static final String CUSTOMER_DETAILS = CDS_PATH + "/common/customer/detail"
    public static final String DISCOVERY_STATUS = CDS_PATH + "/discovery/status"
    public static final String DISCOVERY_OUTAGES = CDS_PATH + "/discovery/outages"
    public static final String ACCOUNTS_CONSENT_PATH = "/au100/accounts-validation"
    public static final String CDR_ARRANGEMENT_ENDPOINT = "/arrangements/1.0.0/revoke"
    public static final String INTROSPECTION_ENDPOINT = "/oauth2/introspect"
    public static final String CONSENT_STATUS_ENDPOINT = "/api/openbanking/consent-mgt/uk300"
    public static final String PUSHED_AUTHORISATION_BASE_PATH = auConfiguration.getServerAuthorisationServerURL()
    public static final String PAR_ENDPOINT = "/api/openbanking/push-authorization/par"
    public static final String TOKEN_REVOKE_PATH = "/oauth2/revoke"
    public static final String STATUS_PATH = "/account-confirmation"
    public static final String CONSENT_STATUS_AU_ENDPOINT = "api/openbanking/account-type-mgt"
    public static final String UPDATE_BUSINESS_USER  = "/account-type-management/business-stakeholders"
    public static final String SHARABLE_BANK_ACCOUNT_SERVICE= "/api/openbanking/cds/backend"
    public static final String BANK_ACCOUNT_SERVICE  = "/services/bankaccounts/bankaccountservice/sharable-accounts"
    public static final String BUSINESS_USER_PERMISSION  = "/account-type-management/business-stakeholders/permission"
    public static final String CONSENT_SEARCH_ENDPOINT = "/api/openbanking/consent/admin/search"
    public static final String DISCLOSURE_OPTIONS_ENDPOINT = "/account-type-management/disclosure-options"
    public static final String SECONDARY_ACCOUNT_ENDPOINT = "/account-type-management/secondary-accounts"
    public static final String LEGAL_ENTITY_LIST_ENDPOINT = "/account-type-management/legal-entity-list"
    public static final String UPDATE_LEGAL_ENTITY_SHARING_STATUS = "/account-type-management/legal-entity"
    public static final String GET_TRANSACTIONS = SINGLE_ACCOUNT_PATH + "/transactions"
    public static final String GET_PRODUCTS = CDS_PATH + "/banking/products"
    public static final String SINGLE_BUSINESS_ACCOUNT_PATH = CDS_PATH+ "/banking/accounts/" + businessAccountID
    public static final String GET_BUSINESS_ACCOUNT_TRANSACTIONS = SINGLE_BUSINESS_ACCOUNT_PATH + "/transactions"

    public static final String BANK_CUSTOMER_BASIC_READ = "Organisation profile and contact details"
    public static final String BANK_CUSTOMER_DETAIL_READ = "Organisation profile and contact details*‡"
    public static final String BANK_ACCOUNT_BASIC_READ = "Account name, type, and balance"
    public static final String BANK_ACCOUNT_DETAIL_READ = "Account balance and details‡"
    public static final String BANK_TRANSACTION_READ = "Transaction details"
    public static final String BANK_PAYEES_READ = "Saved payees"
    public static final String BANK_REGULAR_PAYMENTS_READ = "Direct debits and scheduled payments"
    public static final String BANK_CUSTOMER_BASIC_READ_INDIVIDUAL = "Name and occupation"
    public static final String BANK_CUSTOMER_BASIC_DETAIL_INDIVIDUAL = "Name, occupation, contact details ‡"

    public static final String LBL_OTP_TIMEOUT = "//div[@id='otpTimeout']"
    public static final String LBL_FOOTER_DESCRIPTION = "//div[@class='ui segment']/div/form/div/div"
    public static final String ELE_CONSENT_PAGE = "//form[@id='oauth2_authz_consent']"
    public static final String LBL_INCORRECT_USERNAME = "//div[@id='error-msg']"
    public static final String LBL_AUTHENTICATION_FAILURE = "//div[@id='failed-msg']"
    public static final String LBL_AGENT_NAME_AND_ROLE = "Agent name and role"
    public static final String LBL_ORGANISATION_NAME = "Organisation name"
    public static final String LBL_ORGANISATION_NUMBER = "Organisation numbers (ABN or ACN)"
    public static final String LBL_CHARITY_STATUS = "Charity status"
    public static final String LBL_ESTABLISHMENT_DATE = "Establishment date"
    public static final String LBL_INDUSTRY = "Industry"
    public static final String LBL_ORGANISATION_TYPE = "Organisation type"
    public static final String LBL_COUNTRY_OF_REGISTRATION = "Country of registration"
    public static final String LBL_ORGANISATION_ADDRESS = "Organisation address"
    public static final String LBL_MAIL_ADDRESS = "Mail address"
    public static final String LBL_PHONE_NUMBER = "Phone number"
    public static final String LBL_NAME_OF_ACCOUNT = "Name of account"
    public static final String LBL_TYPE_OF_ACCOUNT = "Type of account"
    public static final String LBL_ACCOUNT_BALANCE = "Account balance"
    public static final String LBL_ACCOUNT_NUMBER = "Account number"
    public static final String LBL_INTEREST_RATES = "Interest rates"
    public static final String LBL_FEES = "Fees"
    public static final String LBL_DISCOUNTS = "Discounts"
    public static final String LBL_ACCOUNT_TERMS = "Account terms"
    public static final String LBL_ACCOUNT_MAIL_ADDRESS = "Account mail address"
    public static final String LBL_INCOMING_AND_OUTGOING_TRANSACTIONS = "Incoming and outgoing transactions"
    public static final String LBL_AMOUNTS = "Amounts"
    public static final String LBL_DATES = "Dates"
    public static final String LBL_DESCRIPTION_OF_TRANSACTION = "Descriptions of transactions"
    public static final String LBL_NAME_OF_MONEY_RECIPIENT = "Who you have sent money to and received money from" +
            "(e.g.their name)"
    public static final String LBL_DIRECT_DEBITS = "Direct debits"
    public static final String LBL_SCHEDULE_PAYMENTS = "Scheduled payments"
    public static final String LBL_DETAILS_OF_SAVED_ACCOUNTS = "Names and details of accounts you have saved; " +
            "(e.g. their BSB and Account Number, BPay CRN and Biller code, or NPP PayID)"
    public static final String LBL_WHERE_TO_MANAGE_INSTRUCTION = "You can review and manage this arrangement on the " +
            "Data Sharing dashboard by going to Settings>Data Sharing on the Mock Company Inc., Mock Software 1 " +
            "website or app."

    public static final String LBL_AUTHORISED = 'Authorised'
    public static final String LBL_REVOKED = 'Revoked'
    public static final String LBL_STOP_SHARING = 'Stop sharing'
    public static final String LBL_CONSENT_GRANTED = 'Consent granted: '
    public static final String LBL_CREATED_ON = "Created on: "
    public static final String LBL_WHEN_YOU_GAVE_CONSENT = 'When you gave consent'
    public static final String LBL_WHEN_YOUR_CONSENT_EXPIRE = 'When your consent will expire'
    public static final String LBL_SHARING_PERIOD = 'Sharing period'
    public static final String LBL_RATES_FEES_DISCOUNT = "Interest rates, Fees, Discounts"
    public static final String LBL_AMOUNTS_AND_DATES = "Amounts, Dates"
    public static final String LBL_PHONE = "Phone"
    public static final String LBL_EMAIL_ADDRESS = "Email address"
    public static final String LBL_RESIDENTIAL_ADDRESS = "Residential address"
    public static final String LBL_NAME = "Name"
    public static final String LBL_OCCUPATION = "Occupation"

    public static final String ERROR = "error";
    public static final String ERROR_DESCRIPTION = "error_description";
    public static final String ERROR_DETAIL = "errors[0].detail"
    public static final String ERROR_SOURCE_PARAMETER = "errors[0].source.parameter"
    public static final String ERROR_SOURCE_POINTER = "errors[0].source.pointer"
    public static final String ERROR_TITLE = "errors[0].title"
    public static final String ERROR_CODE = "errors[0].code"
    public static final String ERROR_META_URN = "errors[0].meta.urn"
    public static final String ERROR_OPENSTATUS = "data.accounts.openStatus[0]"

    public static final String ERROR_X_V_INVALID = "x-v header in the request is invalid"
    public static final String ERROR_X_V_MISSING = "Mandatory header x-v is missing"
    public static final String ERROR_X_MIN_V_INVALID = "x-min-v header in the request is invalid"
    public static final String ERROR_X_V_INVALID_VERSION = "Requested x-v version is not supported"
    public static final String ERROR_ENDPOINT_VERSION = "Requested endpoint version 10 is not supported"
    public static final String ERROR_ENDPOINT_VERSION5 = "Requested endpoint version 5 is not supported"
    public static final String INVALID_FIELD = "Invalid Field"
    public static final String INVALID_HEADER = "Invalid Header"
    public static final String UNSUPPORTED_VERSION = "Unsupported Version"
    public static final String INVALID_VERSION = "Invalid Version"
    public static final String PAGE_SIZE_EXCEEDED = "Page Size Exceeded"
    public static final String MISSING_HEADER = "Missing Required Header"
    public static final String INVALID_BANK_ACC = "Invalid Banking Account"
    public static final String ACCOUNT_ID_NOT_FOUND = "ID of the account not found or invalid"
    public static final String INVALID_CLIENT_METADATA = "invalid_client_metadata"
    public static final String INVALID_RESOURCE = "Invalid Resource"
    public static final String INVALID_AUTHORISATION = "Invalid Authorisation Header"
    public static final String RESOURCE_FORBIDDEN = "Resource Is Forbidden"
    public static final String INVALID_REDIRECT_URI = "invalid_redirect_uri"
    public static final String UNAPPROVED_SOFTWARE_STATEMENT = "unapproved_software_statement"
    public static final String RESOURCE_NOT_FOUND = "Resource Not Found"
    public static final String MESSAGE_THROTTLED_OUT = "Message throttled out"
    public static final String REQUEST_URI = "request_uri"
    public static final String INVALID_CLIENT = "invalid_client"
    public static final String MISSING_CREDENTIALS = "Missing Credentials"
    public static final String INVALID_GRANT = "invalid_grant"

    public static final String ERROR_CODE_MISSING_HEADER = "urn:au-cds:error:cds-all:Header/Missing"
    public static final String ERROR_CODE_INVALID_HEADER = "urn:au-cds:error:cds-all:Header/Invalid"
    public static final String ERROR_CODE_INVALID_FIELD = "urn:au-cds:error:cds-all:Field/Invalid"
    public static final String ERROR_CODE_UNSUPPORTED_VERSION = "urn:au-cds:error:cds-all:Header/UnsupportedVersion"
    public static final String ERROR_CODE_INVALID_VERSION = "urn:au-cds:error:cds-all:Header/InvalidVersion"
    public static final String ERROR_CODE_INVALID_BANK_ACC = "urn:au-cds:error:cds-banking:Authorisation/InvalidBankingAccount"
    public static final String ERROR_CODE_INVALID_RESOURCE = "urn:au-cds:error:cds-all:Resource/Invalid"
    public static final String ERROR_CODE_PAGE_SIZE_TOO_LARGE = "AU.CDR.Invalid.PageSizeTooLarge"
    public static final String ERROR_CODE_UNAUTHORIZED = "AU.CDR.Unauthorized"
    public static final String ERROR_CODE_RESOURCE_FORBIDDEN = "AU.CDR.Entitlements.Forbidden"
    public static final String ERROR_CODE_RESOURCE_NOTFOUND = "urn:au-cds:error:cds-all:Resource/NotFound"
    public static final String ERROR_CODE_TOO_MANY_REQUESTS = "AU.CDR.TooManyRequests"
    public static final String ERROR_CODE_INVALID_SP_STATUS = "AU.CDR.Entitlements.InvalidAdrSoftwareProductStatus"
    public static final String ERROR_TITLE_INVALID_SP_STATUS = "ADR Software Product Status Is Invalid"
    public static final String ERROR_CODE_INVALID_ADR_STATUS = "AU.CDR.Entitlements.InvalidAdrStatus"
    public static final String ERROR_TITLE_INVALID_ADR_STATUS = "ADR Status Is Invalid"
    public static final String ERROR_CODE_GENERAL_ERROR_UNEXPECTED = "urn:au-cds:error:cds-all:GeneralError/Unexpected"

    // Headers
    public static final String PARAM_X_V = "x-v"
    public static final String PARAM_X_MIN_V = "x-min-v"
    public static final String PARAM_PRODUCT_CATEGORY = "product-category"
    public static final String PARAM_CDS_CLIENT_HEADER = "x-cds-client-headers"
    public static final String PARAM_FAPI_INTERACTION_ID = "x-fapi-interaction-id"
    public static final String PARAM_FAPI_CUSTOMER_IP_ADDRESS = "x-fapi-customer-ip-address"
    public static final String PARAM_FAPI_AUTH_DATE = "x-fapi-auth-date"
    public static final String PARAM_ACCOUNT_ID = "accountID"
    public static final String PARAM_JOINT_ACCOUNT_ID = "jointAccountID"
    public static final String PARAM_PAGE_SIZE = "page-size"
    public static final String PARAM_AUTHORIZATION = "Authorization"
    public static final String ACCOUNT_OWNER_USER_ID = "accountOwnerUserID"
    public static final String NOMINATED_REP_USER_ID = "nominatedRepUserID"
    public static final String ACCOUNT_OWNER_USER_ID2 = "accountOwnerUserID2"
    public static final String NOMINATED_REP_USER_ID2 = "nominatedRepUserID2"
    public static final String ACCOUNT_ID = "accountId"
    public static final String BUSINESS_ACCOUNT_INFO = "businessAccountInfo"
    public static final String ACCOUNT_OWNERS = "AccountOwners"
    public static final String NOMINATED_REPRESENTATIVES = "NominatedRepresentatives"
    public static final String MEMBER_ID = "memberId"
    public static final String SECONDARY_ACCOUNT_INFO = "secondaryAccountInfo"
    public static final String IS_SECONDARY_ACCOUNT = "isSecondaryAccount"
    public static final String PARAM_USERID_1 = "userId1"
    public static final String PARAM_USERID_2 = "userId2"

    public static final String VALUE_FAPI_AUTH_DATE = "Tue, 78 Jan 1312 80:05:73 GMT"

    public static final String LBL_USER_ID = "admin@wso2.com"
    public static final String LBL_CARBON = "@carbon.super"
    public static final String LBL_ALL_APPLICATION = "All Applications"
    public static final String LBL_APPLICATION_ID = "app"

    public static final String BASE_PATH_TYPE_ACCOUNT = "Accounts"
    public static final String BASE_PATH_TYPE_BALANCES = "Balances"
    public static final String BASE_PATH_TYPE_TRANSACTIONS = "Transactions"
    public static final String BASE_PATH_TYPE_DIRECT_DEBIT = "Direct-Debit"
    public static final String BASE_PATH_TYPE_SCHEDULED_PAYMENT = "Scheduled-Payment"
    public static final String BASE_PATH_TYPE_PAYEES = "Payees"
    public static final String BASE_PATH_TYPE_PRODUCTS = "Product"
    public static final String BASE_PATH_TYPE_CUSTOMER = "Customer"
    public static final String BASE_PATH_TYPE_DISCOVERY = "Discovery"
    public static final String BASE_PATH_TYPE_CDR_ARRANGEMENT = "CDR-Arrangement"
    public static final String BASE_PATH_TYPE_ADMIN = "Admin"

    public static final String ADMIN_API_ISSUER = "cdr-register"
    public static final String ADMIN_API_AUDIENCE = "https://wso2ob.com"

    //Selenium Constants
    public static final int DEFAULT_DELAY = 5;

    // Second Factor Authenticator constants
    public static final String AU_OTP_CODE = "111222"

    // DCR constants
    public static final String DCR_REGISTRATION_ENDPOINT = "/open-banking/0.2/register/"
    public static final String DCR_REGISTRATION_ENDPOINT_WITH_DOMAIN = "https://localhost:8243/open-banking/0.2/register"
    public static final String DCR_SSA = new File(auConfiguration.getAppDCRSSAPath()).text
    public static final String DCR_SOFTWARE_PRODUCT_ID = auConfiguration.getAppDCRSoftwareId()
    public static final String DCR_REDIRECT_URI = auConfiguration.getAppDCRRedirectUri()
    public static final String DCR_ALTERNATE_REDIRECT_URI = auConfiguration.getAppDCRAlternateRedirectUri()
    public static final String DCR_AUD_VALUE = auConfiguration.getConsentAudienceValue()
    public static final String DCR_BASE_PATH_TYPE = "DCR"
    public static final String DCR_WITHOUT_TOKEN_ENDPOINT_SIGNINGALGO = "Required parameter tokenEndPointAuthSigningAlg cannot be null"
    public static final String DCR_WITHOUT_TOKEN_ENDPOINT_AUTHMETHOD = "Required parameter tokenEndPointAuthentication cannot be null"
    public static final String DCR_GRANT_TYPES_NULL = "Required parameter grantTypes cannot be null"
    public static final String DCR_WITHOUT_RESPONSE_TYPES = "Required parameter responseTypes cannot be null"
    public static final String DCR_WITHOUT_SSA = "Required parameter software statement cannot be null"
    public static final String DCR_WITHOUT_ID_TOKEN_RESPONSE_ALGO = "Required parameter idTokenEncryptionResponseAlg cannot be null"
    public static final String DCR_WITHOUT_ID_TOKEN_ENCRYPTION_ALGO = "Required parameter idTokenEncryptionResponseAlg cannot be null"
    public static final String DCR_WITHOUT_ID_TOKEN_ENCRYPTION_METHOD = "Required parameter idTokenEncryptionResponseEnc cannot be null"
    public static final String DCR_INVALID_ID_TOKEN_ENCRYPTION_ALGO = "Invalid idTokenEncryptionResponseAlg provided"
    public static final String DCR_INVALID_ID_TOKEN_ENCRYPTION_METHOD = "Invalid idTokenEncryptionResponseEnc provided"
    public static final String DCR_INVALID_REDIRECT_DESCRIPTION = "Invalid callback uris"
    public static final String INSUFFICIENT_SCOPE = "insufficient_scope"
    public static final String UNSUPPORTED_RESPONSE_MODE = "Unsupported response_mode value. Only jwt response mode is allowed."
    public static final String ERROR_CODE_INVALID_PAGE = "urn:au-cds:error:cds-all:Field/InvalidPage"
    public static final String INVALID_PAGE = "Invalid Page"
    public static final String INVALID_SIGNING_ALG = "Invalid signing algorithm sent"
    public static final String INVALID_AUDIENCE_ERROR = "Invalid audience provided"
    public static final String ERROR_REDIRECT_URL_WITH_DIFF_HOSTNAMES = "Redirect URIs do not contain the same hostname"
    public static final String ERROR_EMPTY_REDIRECT_URL_IN_SSA = "Redirect URIs can not be null or empty in SSA"
    public static final String ERROR_WITHOUT_IDTOKEN_SINGED_ALG = "Required parameter idTokenSignedResponseAlg cannot be null"

    /**
     * Mock Register Constants
     */
    public static final String MOCK_REGISTER_HOST = auConfiguration.getMockCDRHostname()
    public static final String MOCK_INFO_SEC_BASE_URL = "https://" + MOCK_REGISTER_HOST + ":7001"
    public static final String MOCK_ADMIN_BASE_URL = "https://" + MOCK_REGISTER_HOST + ":7006"
    public static final String MOCK_TOKEN_ENDPOINT = "/idp/connect/token"
    public static final String MOCK_CLIENT_ASSERTION_ENDPOINT = "/loopback/MockDataRecipientClientAssertion"
    public static final String MOCK_SSA_ENDPOINT = "/cdr-register/v1/banking/data-recipients/brands"
    public static final String MOCK_METADATA_ENDPOINT = "/admin/metadata"

    //Mock ADR brandIds and their software products loaded at the CDR mock Register
    public static final String MOCK_ADR_BRAND_ID_1 = "20c0864b-ceef-4de0-8944-eb0962f825eb";
    public static final String MOCK_ADR_BRAND_ID_1_SOFTWARE_PRODUCT_1 = "63bc22ac-6fd2-4e85-a979-c2fc7c4db9da"
    public static final String MOCK_ADR_BRAND_ID_1_SOFTWARE_PRODUCT_2 = "86ecb655-9eba-409c-9be3-59e7adf7080d"
    public static final String MOCK_ADR_BRAND_ID_1_SOFTWARE_PRODUCT_3 = "9381dad2-6b68-4879-b496-c1319d7dfbc9"
    public static final String MOCK_ADR_BRAND_ID_1_SOFTWARE_PRODUCT_4 = "d3c44426-e003-4604-aa45-4137e45dfbc4"
    public static final String MOCK_ADR_BRAND_ID_2 = "ebbcc2f2-817e-42b8-8a28-cd45902159e0";
    public static final String MOCK_ADR_BRAND_ID_2_SOFTWARE_PRODUCT_1 = "5d03d1a6-b83b-4176-a2f4-d0074a205695"
    public static final String MOCK_ADR_BRAND_ID_2_SOFTWARE_PRODUCT_2 = "dafa09db-4433-4203-907a-bdf797c8cd21"

    //status code added
    static final int STATUS_CODE_200 = 200
    static final int STATUS_CODE_201 = 201
    static final int STATUS_CODE_400 = 400
    static final int STATUS_CODE_401 = 401
    static final int STATUS_CODE_404 = 404
    static final int STATUS_CODE_405 = 405
    static final int STATUS_CODE_406 = 406
    static final int STATUS_CODE_422 = 422
    static final int STATUS_CODE_409 = 409
    static final int STATUS_CODE_204 = 204
    static final int STATUS_CODE_403 = 403
    static final int STATUS_CODE_500 = 500
    static final int STATUS_CODE_415 = 415
    static final int STATUS_CODE_501 = 501

    //Payload Links
    public static final String LINKS_SELF = "links.self"
    public static final String LINKS_FIRST = "links.first"
    public static final String LINKS_PREV = "links.prev"
    public static final String LINKS_NEXT = "links.next"
    public static final String LINKS_LAST = "links.last"
    public static final String DATA = "data"
    public static final String META = "meta"

    //Response Payloads Validations
    public static final String RESPONSE_DATA_BULK_ACCOUNTID_LIST = "data.accounts.accountId"
    public static final String RESPONSE_DATA_SINGLE_ACCOUNTID = "data.accountId"
    public static final String RESPONSE_DATA_BULK_BALANCE_LIST = "data.balances.accountId"
    public static final String RESPONSE_DATA_TRANSACTION_LIST = "data.transactions"
    public static final String RESPONSE_DATA_DIRECT_DEBIT_AUTH = "data.directDebitAuthorisations"
    public static final String RESPONSE_DATA_SCHEDULE_PAY = "data.scheduledPayments"
    public static final String RESPONSE_DATA_PAYEE = "data.payees"
    public static final String RESPONSE_DATA_TRANSACTIONID = "data.transactionId"
    public static final String RESPONSE_DATA_PAYEEID = "data.payeeId"
    public static final String RESPONSE_DATA_PRODUCTS = "data.products"

    public static final String PARAM_CUSTOMER_ACCOUNT_TYPE = "customerAccountType"
    public static final String INCORRECT_ACC_ID = "1234567"
    public static final String QUERY_PARAM_USERID = "userId"
    public static final String QUERY_PARAM_ACCID = "accountId"
    public static final String PARAM_PERMISSION_STATUS = "permissionStatus"

    public static final String TPP_ERROR_CATEGORY = "tppMessages[0].category"
    public static final String TPP_ERROR_CODE = "tppMessages[0].code"
    public static final String TPP_ERROR_CODE_FORMAT_ERROR = "FORMAT_ERROR"
    public static final String TPP_ERROR_CODE_VALUE = "ERROR"
    public static final String TPP_ERROR_CODE_FORMAT_INVALID= "REQUESTED_FORMATS_INVALID"

    public static final String VALUE_KEY = "value"
    public static final String HTML_RESPONSE_ATTR = "response="
    public static final String CURRENT = "CURRENT"
    public static final String ALL = "ALL"
    public static final String PAGE_SIZE = "page-size"
    public static final String UPDATED_SINCE = "updated-since"
    public static final String BRAND = "brand"
    public static final String EFFECTIVE = "EFFECTIVE"
    public static final String PERIOD = "period"
    public static final String HISTORIC = "HISTORIC"

    public static final String RESPONSE_DATA_CUSTOMERUTYPE = "data.customerUType"
    public static final String RESPONSE_DATA_PERSON = "data.person"
    public static final String RESPONSE_DATA_ORGANIZATION = "data.organisation"
    public static final String RESPONSE_EXPIRES_IN = "expires_in"
    public static final String CDR_ARRANGEMENT_ID = "cdr_arrangement_id"
    public static final String INVALID_REQUEST_OBJECT = "invalid_request_object"
    public static final String ERROR_NOT_ALLOWED_TO_ACCESS = "The access token does not allow you to access the requested resource"
    public static final String PARAM_PROFILE_NAME = "profileName"
    public static final String USER_DENIED_THE_CONSENT = "User denied the consent"
    public static final String USER_SKIP_THE_CONSENT_FLOW = "User skipped the consent flow"
    public static final String CANCEL_ERROR_IN_ACCOUNTS_PAGE = "User skip the consent flow"

    public static final String ACTIVE = "active"
    public static final String INACTIVE = "inactive"
    public static final String IS_JOINT_ACCOUNT = "isJointAccount"
    public static final String LEGAL_ENTITY_DETAILS = "LegalEntityDetails"
    public static final String SECONDARY_USERS_USERID = "secondaryUserID"
    public static final String PAYLOAD_SECONDARY_USERS = "secondaryUsers"
    public static final String LEGAL_ENTITIES = "legalEntities"
    public static final String LEGAL_ENTITY_ID_MAP = "legalEntityID"
    public static final String LEGAL_ENTITY_ID2_MAP = "LegalEntityId2"
    public static final String SHARING_STATUS = "legalEntitySharingStatus"
    public static final String PAYLOAD_PARAM_ACCOUNT_ID = "accountID"
    public static final String BLOCK_ENTITY = "blocked"
    public static final String CLIENT_ID = "client_id"

    public static final String INVALID_REQUEST = "invalid_request"
    public static final INVALID_SHARING_DURATION = "Invalid sharing_duration value"
    public static final EXPIRES_IN = "expires_in"
    public static final INVALID_CREDENTIALS = "Invalid Credentials"

    public static final INVALID_REQUEST_BODY = "Invalid request body"
    public static final MALFORMED_PAR_REQUEST= "The request is malformed."
    public static final INVALID_ALGORITHM = "Invalid request object signing algorithm"
    public static final INVALID_RESPONSE_TYPE = "Invalid response type"
    public static final MISSING_AUD_VALUE= "aud parameter is missing in the request object"
    public static final MISSING_ISS_VALUE= "Invalid issuer in the request"
    public static final MISSING_EXP_VALUE= "exp parameter is missing in the request object"
    public static final MISSING_NBF_VALUE= "nbf parameter is missing in the request object"
    public static final INVALID_FUTURE_EXPIRY_TIME = "Invalid expiry time. 'exp' claim must be a future value."
    public static final INVALID_EXPIRY_TIME = "exp parameter in the request object is over 1 hour in the future"
    public static final INVALID_FUTURE_NBF_TIME = "Invalid not before time. 'nbf' must be a past value."
    public static final UNSUPPORTED_X_FAPI_AUTH_DATE = "Requested x-fapi-auth-date header is not supported"
    public static final NEWEST_TIME = "newest-time"
    public static final OLDEST_TIME = "oldest-time"
    public static final ERROR_CODE_GENERAL_EXPECTED_ERROR = "urn:au-cds:error:cds-all:GeneralError/Expected"
    public static final INVALID_CONTENT_TYPE = "Request Content-Type header does not match any allowed types"
    public static final INVALID_ACCEPT_HEADER = "Invalid Accept Header"
    public static final ERROR_TITLE_GENERAL_EXPECTED_ERROR = "Expected Error Encountered"
    public static final UNSUPPORTED_X_FAPI_IP_ADDRESS = "Requested x-fapi-customer-ip-address header is not supported"
    public static final ERROR_CODE_MISSING_FIELD = "urn:au-cds:error:cds-all:Field/Missing"
    public static final MISSING_FIELD = "Missing Required Field"
    public static final String ADMIN_METRICS = "/admin/metrics"
    public static final LBL_NEW_PAYEES_INDICATOR_XPATH = "//button[contains(text(),'Saved payees')]/span[contains(text()," +
            "'New')]"
    public static final String ERROR_INVALID_CLIENT_ID = "Cannot find an application associated with the given consumer key"
    public static final String UNABLE_TO_DECODE_JWT = "Unable to decode JWT."
    public static final String MISSING_REDIRECT_URL_VALUE= "Mandatory parameter redirect_uri, not found in the request"
    public static final String CALLBACK_NOT_MATCH = "Registered callback does not match with the provided url."
    public static final String INVALID_CDR_ARRANGEMENT_ID = "Invalid cdr_arrangement_id"
    public static final String INVALID_AUDIENCE = "Invalid audience value in the request"

    //Endpoint Versions
    public static final int X_V_HEADER_ACCOUNTS = 2
    public static final int X_V_HEADER_ACCOUNT = 3
    public static final int X_V_HEADER_BALANCES = 1
    public static final int X_V_HEADER_BALANCE = 1
    public static final int X_V_HEADER_TRANSACTIONS = 1
    public static final int X_V_HEADER_TRANSACTION = 1
    public static final int X_V_HEADER_DIRECT_DEBITS = 1
    public static final int X_V_HEADER_PAYMENT_SCHEDULED = 2
    public static final int X_V_HEADER_PAYEES = 2
    public static final int X_V_HEADER_PRODUCTS = 3
    public static final int X_V_HEADER_PRODUCT = 4
    public static final int X_V_HEADER_METRICS = 5
    public static final int X_V_HEADER_METADATA = 1
    public static final int X_V_HEADER_CUSTOMER = 1
    public static final int X_V_HEADER_CUSTOMER_DETAIL = 2
    public static final int X_V_MIN_HEADER_METRICS = 1
    public static final int UNSUPPORTED_X_V_VERSION = 10
    public static final int CDR_ENDPOINT_VERSION = 2
    public static final int X_V_HEADER_STATUS = 1
    public static final int X_V_HEADER_OUTAGES = 1
    public static final String INVALID_ACCESSTOKEN = "eyJ4NXQiOiJNREpsTmpJeE4yRTFPR1psT0dWbU1HUXhPVEZsTXpCbU5tRmpaalEwWTJZd09HWTBOMkkwWXpFNFl6WmpOalJoWW1SbU1tUTBPRGRpTkRoak1HRXdNQSIsImtpZCI6Ik1ESmxOakl4TjJFMU9HWmxPR1ZtTUdReE9URmxNekJtTm1GalpqUTBZMll3T0dZME4ySTBZekU0WXpaak5qUmhZbVJtTW1RME9EZGlORGhqTUdFd01BX1JTMjU2IiwidHlwIjoiYXQrand0IiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJhZG1pbkB3c28yLmNvbSIsImF1dCI6IkFQUExJQ0FUSU9OX1VTRVIiLCJpc3MiOiJodHRwczpcL1wvbG9jYWxob3N0Ojk0NDZcL29hdXRoMlwvdG9rZW4iLCJjbGllbnRfaWQiOiJONkQwb0M0c1ExOGc1UGxTNGdhU0ttaVVhczRhIiwiYXVkIjoiTjZEMG9DNHNRMThnNVBsUzRnYVNLbWlVYXM0YSIsIm5iZiI6MTY4ODg0MDU0NiwiYXpwIjoiTjZEMG9DNHNRMThnNVBsUzRnYVNLbWlVYXM0YSIsInNjb3BlIjoiYmFuazphY2NvdW50cy5iYXNpYzpyZWFkIGJhbms6YWNjb3VudHMuZGV0YWlsOnJlYWQgYmFuazpwYXllZXM6cmVhZCBiYW5rOnJlZ3VsYXJfcGF5bWVudHM6cmVhZCBiYW5rOnRyYW5zYWN0aW9uczpyZWFkIGNvbW1vbjpjdXN0b21lci5iYXNpYzpyZWFkIGNvbW1vbjpjdXN0b21lci5kZXRhaWw6cmVhZCBvcGVuaWQiLCJjbmYiOnsieDV0I1MyNTYiOiJrMHAtLU1MN25ma0UycFVMS3J5c3pKUkJ4MlRoQk1heEhnSk9lUG9zaXRzIn0sImV4cCI6MTY4ODg0NDE0NiwiaWF0IjoxNjg4ODQwNTQ2LCJqdGkiOiIyMmIwODA1Yi00ZTgyLTQxYjYtYTlhZS0zN2Y3OGMyYTlhMmIiLCJjb25zZW50X2lkIjoiOTFiYWUzMjMtNmYwYi00ZTkyLTljZWYtZjJiNGI1MjRjMTY1In0.UATu4say15Jvhf5vy4On9MS0WRyERcMcFOUUzYwgqNNVKAHZ7sjfwrR05eq_QYdeHejNpCIadcwbN-TWjnHu5s2vaavUbqFx_SYb9jSFm_JJTKMG0tFo6iCmy6Jfr8P1-S0uVOcnoI5mz2PX7CGqd8Kdx1uBUyWJ60CqiBFCewKZ0GddZbEfm9HC4aW8RBb65BqC01l5Ww4B_3vf_B6pvuDglX3Bb_yFt1grcH8r6EX-ibWQpYwL8LdgHIE4GlP7QQnN5elXjqpOfP3KDXZyeoALhqAFugOYNGeaHATPvhGH2lfU-2qIKyqqHEk286lkZBnsIkxMAx_iToMig-xA"
    public static final String ERROR_UNSUPPORTED_RESPONSE = "Unsupported response_type value. Only code response type is allowed."
    public static final String DCR_CLAIM_LEGAL_ENTITY_ID = "legal_entity_id"
    public static final String DCR_CLAIM_LEGAL_ENTITY_NAME = "legal_entity_name"
    public static final String SAMPLE_LEGAL_ENTITY_ID = "TPP2"
    public static final String SAMPLE_LEGAL_ENTITY_NAME = "Mock Company Pty Ltd."
    public static final String AlternateAccountId = "30080098763501"
    public static final CodeVerifier CODE_VERIFIER = new CodeVerifier()
    public static final String INVALID_REQUEST_URI = "invalid_request_uri"
    public static final String ALT_LEGAL_ENTITY = "DR_82"
    public static final String PAYLOAD_PARAM_ACCOUNTS = "accounts"

    public static final String HTTP_METHOD_PATCH = "PATCH"
    public static final String HTTP_METHOD_HEAD = "HEAD"
    public static final String HTTP_METHOD_OPTIONS = "OPTIONS"
    public static final String HTTP_METHOD_TRACE = "TRACE"
    public static final String HTTP_METHOD_CONNECT = "CONNECT"
    public static final String HTTP_METHOD_COPY = "COPY"
    public static final String HTTP_METHOD_LINK = "LINK"
    public static final String HTTP_METHOD_UNLINK = "UNLINK"
    public static final String HTTP_METHOD_PURGE = "PURGE"
    public static final String HTTP_METHOD_LOCK = "LOCK"
    public static final String HTTP_METHOD_UNLOCK = "UNLOCK"
    public static final String HTTP_METHOD_PROPFIND = "PROPFIND"
    public static final String HTTP_METHOD_VIEW = "VIEW"
    public static final String REDIRECT_URL_WITH_QUERY_PARAMS = "${auConfiguration.getAppInfoRedirectURL()}?dummy1=lorem&dummy2=ipsum"
    public static final String LOCALHOST_REDIRECT_URL = "https://localhost:9443/carbon"
    public static final String USERINFO_ENDPOINT = "/oauth2/userinfo"
    public static final String ADR_STATUS_NOT_ACTIVE = "ADR Status Is Not Active"
    public static final String ERROR_CODE_ADR_STATUS_NOT_ACTIVE = "urn:au-cds:error:cds-banking:Authorisation/AdrStatusNotActive"
    public static final String CONFIG_FILE_NAME = "TestConfiguration.xml"

    static final String PERIOD_CURRENT = "CURRENT"
    static final String PERIOD_ALL = "ALL"
    static final String PERIOD_HISTORIC = "HISTORIC"
    static final String ACTIVE_AUTHORIZATION_INDIVIDUAL = "data.authorisations.activeAuthorisationCount.individual"
    static final String ACTIVE_AUTHORIZATION_NONINDIVIDUAL = "data.authorisations.activeAuthorisationCount.nonIndividual"
    static final String NEWAUTH_CURRENTDAY_ONGOING_INDIVIDUAL =
            "data.authorisations.newAuthorisationCount.currentDay.ongoing.individual"
    static final String NEWAUTH_CURRENTDAY_ONGOING_NONINDIVIDUAL =
            "data.authorisations.newAuthorisationCount.currentDay.ongoing.nonIndividual"
    static final String NEWAUTH_CURRENTDAY_ONCEOFF_INDIVIDUAL =
            "data.authorisations.newAuthorisationCount.currentDay.onceOff.individual"
    static final String NEWAUTH_CURRENTDAY_ONCEOFF_NONINDIVIDUAL =
            "data.authorisations.newAuthorisationCount.currentDay.onceOff.nonIndividual"
    static final String REVOKED_CURRENTDAY_INDIVIDUAL =
            "data.authorisations.revokedAuthorisationCount.currentDay.individual"
    static final String REVOKED_CURRENTDAY_NONINDIVIDUAL =
            "data.authorisations.revokedAuthorisationCount.currentDay.nonIndividual"
    static final String AMENDED_CURRENTDAY_INDIVIDUAL =
            "data.authorisations.amendedAuthorisationCount.currentDay.individual"
    static final String AMENDED_CURRENTDAY_NONINDIVIDUAL =
            "data.authorisations.amendedAuthorisationCount.currentDay.nonIndividual"
    static final String EXPIRED_CURRENTDAY_INDIVIDUAL =
            "data.authorisations.expiredAuthorisationCount.currentDay.individual"
    static final String EXPIRED_CURRENTDAY_NONINDIVIDUAL =
            "data.authorisations.expiredAuthorisationCount.currentDay.nonIndividual"
    static final String ABANDON_PREIDENTIFICATION_CURRENTDAY =
            "data.authorisations.abandonmentsByStage.preIdentification.currentDay"
    static final String ABANDON_PREAUTHENTICATE_CURRENTDAY =
            "data.authorisations.abandonmentsByStage.preAuthentication.currentDay"
    static final String ABANDON_PREACCSELECT_CURRENTDAY =
            "data.authorisations.abandonmentsByStage.preAccountSelection.currentDay"
    static final String ABANDON_PREAUTH_CURRENTDAY =
            "data.authorisations.abandonmentsByStage.preAuthorisation.currentDay"
    static final String ABANDON_REJECTED_CURRENTDAY =
            "data.authorisations.abandonmentsByStage.rejected.currentDay"
    static final String ABANDON_FAILEDTOKEN_CURRENTDAY =
            "data.authorisations.abandonmentsByStage.failedTokenExchange.currentDay"
    static final String ABANDON_CURRENTDAY =
            "data.authorisations.abandonedConsentFlowCount.currentDay"
    static final String PERFORMANCE_CURRENTDAY ="data.performance.currentDay"
    static final String PERFORMANCE_HIGH_CURRENTDAY ="data.performance.highPriority.currentDay"
    static final String PERFORMANCE_LOW_CURRENTDAY ="data.performance.lowPriority.currentDay"
    static final String PERFORMANCE_UNATTENDED_CURRENTDAY ="data.performance.unattended.currentDay"
    static final String PERFORMANCE_UNAUTH_CURRENTDAY ="data.performance.unauthenticated.currentDay"
    static final String PERFORMANCE_LARGE_PAYLOAD_CURRENTDAY ="data.performance.largePayload.currentDay"
    static final String PERFORMANCE_AGGREGATE_CURRENTDAY ="data.performance.aggregate.currentDay"
    static final String AVG_RESPONSE_HIGH_CURRENTDAY ="data.averageResponse.highPriority.currentDay"
    static final String AVG_RESPONSE_LOW_CURRENTDAY ="data.averageResponse.lowPriority.currentDay"
    static final String AVG_RESPONSE_UNATTENDED_CURRENTDAY ="data.averageResponse.unattended.currentDay"
    static final String AVG_RESPONSE_UNAUTH_CURRENTDAY ="data.averageResponse.unauthenticated.currentDay"
    static final String AVG_RESPONSE_LARGE_PAYLOAD_CURRENTDAY ="data.averageResponse.largePayload.currentDay"
    static final String AVGTPS_AUTHENTICATED_CURRENTDAY ="data.averageTps.authenticated.currentDay"
    static final String AVGTPS_UNAUTHENTICATED_CURRENTDAY ="data.averageTps.unauthenticated.currentDay"
    static final String AVGTPS_AGGREGATE_CURRENTDAY ="data.averageTps.aggregate.currentDay"
    static final String INVOCATION_UNAUTHENTICATED_CURRENTDAY = "data.invocations.unauthenticated.currentDay"
    static final String INVOCATION_HIGHPRIORITY_CURRENTDAY = "data.invocations.highPriority.currentDay"
    static final String INVOCATION_LOWPRIORITY_CURRENTDAY = "data.invocations.lowPriority.currentDay"
    static final String INVOCATION_UNATTENDED_CURRENTDAY = "data.invocations.unattended.currentDay"
    static final String INVOCATION_LARGEPAYLOAD_CURRENTDAY = "data.invocations.largePayload.currentDay"
    static final String INCORRECT_ACCESS_TOKEN = "95d4d70e-0319-3fea-9532-e199fe72d489"

    static final String ERROR_UNAUTH_CURRENTDAY = "data.errors.unauthenticated.currentDay"
    static final String ERROR_AUTH_CURRENTDAY = "data.errors.authenticated.currentDay"
    static final String ERROR_AGGREGATE_CURRENTDAY = "data.errors.aggregate.currentDay"
    static final String DATA_CUSTOMER_COUNT = "data.customerCount"
    static final String DATA_RECIPIENT_COUNT = "data.recipientCount"
    static final String DATA_SESSION_COUNT_CURRENTDAY = "data.sessionCount.currentDay"

    static final String CUSTOMER_PRESENT = "customer-present"
    static final ABANDON_WAIT_TIME = 78000
    static final AUTH_CODE_EXPIRATION_TIME = 110001
    public static final String CODE_EXPIRE_ERROR_MSG = "Expired or Revoked authorization code received from token request"
    public static final String CALLBACK_MISMATCH = "Callback url mismatch"
    public static final String LOCALHOST = "localhost"

    public static String REPORTING_DBNAME = "openbank_ob_reporting_statsdb"
    public static String UNAUTHENTICATED = "Unauthenticated"
    public static String HIGH_PRIORITY = "High Priority"
    public static String LOW_PRIORITY = "Low Priority"
    public static String UNATTENDED = "Unattended"
    public static String LARGE_PAYLOAD = "Large Payload"
    public static String REGISTER_PATH = "/register/"
    public static String AUTHENTICATED = "authenticated"
    public static String AGGREGATE = "aggregate"
    static final String AVAILABILITY_UNAUTH_CURRENTMONTH = "data.availability.unauthenticated.currentMonth"
    static final String AVAILABILITY_AUTH_CURRENTMONTH = "data.availability.authenticated.currentMonth"
    static final String AVAILABILITY_AGG_CURRENTMONTH = "data.availability.aggregate.currentMonth"

    public static final String ERROR_CODE_INVALID_ARRANGEMENT = "urn:au-cds:error:cds-all:Authorisation/InvalidArrangement"
    public static final String INVALID_CONSENT_ARRANGEMENT = "Invalid Arrangement ID"
    public static TOKEN_ENDPOINT = "/oauth2/token"
    public static final String REST_API_ADMIN_ENDPOINT = "/api/am/admin/v4/";

    public static MESSAGE = "message"
    public static DESCRIPTION = "description"
    public static REST_API_CLIENT_REGISTRATION_ENDPOINT = "/client-registration/v0.17/register"
    public static final String REST_API_PUBLISHER_ENDPOINT = "/api/am/publisher/v4/apis/"
    public static final String REST_API_STORE_ENDPOINT = "/api/am/store/v1/"
    public static final String REST_API_SCIM2_ENDPOINT = "/scim2"
    public static final String CONTENT_TYPE_APPLICATION_SCIM_JSON = "application/scim+json"
    public static final String SUBSCRIBER_ROLE = "Internal/subscriber"
    public static final String PUBLISHER_ROLE = "Internal/publisher"
    public static final String TEST_SMS_CLIENT_ID = "AC34f40df03e20fb6498b3fcee256ebd3b"
    public static final String TEST_SMS_CLIENT_SECRET = "5fad3edc88ac553b2abf785b52c81adb"
    public static final String TEST_SMS_URL = "https://api.twilio.com/2010-04-01/Accounts/" +TEST_SMS_CLIENT_ID+ "/SMS/Messages.json"

}


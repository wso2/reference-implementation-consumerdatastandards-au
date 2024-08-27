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
 * Class for provide AU configuration constants with OB configuration constants
 */
class AUConfigConstants {

    // Absolute path should be provided
    public static final String CONFIG_FILE_LOCATION = "../cds-toolkit-test-framework/src/main/resources/TestConfiguration.xml";

    // Micro Gateway config constants
    public static final String MICRO_GATEWAY = "MicroGateway"
    public static final String MICRO_GATEWAY_ENABLED = "MicroGatewayEnabled"
    public static final String MICRO_GATEWAY_DCR_URL = "DcrURL"
    public static final String MICRO_GATEWAY_CDS_ACCOUNTS_URL = "CdsAuAccountsURL"
    public static final String MICRO_GATEWAY_CDS_BALANCES_URL = "CdsAuBalancesURL"
    public static final String MICRO_GATEWAY_CDS_TRANSACTIONS_URL = "CdsAuTransactionURL"
    public static final String MICRO_GATEWAY_CDS_DIRECT_DEBIT_URL = "CdsAuDirectDebitURL"
    public static final String MICRO_GATEWAY_CDS_SCHEDULED_PAY_URL = "CdsAuSchedulePaymentURL"
    public static final String MICRO_GATEWAY_CDS_PAYEE_URL = "CdsAuPayeeURL"
    public static final String MICRO_GATEWAY_CDS_PRODUCT_URL = "CdsAuProductURL"
    public static final String MICRO_GATEWAY_CDS_CUSTOMER_URL = "CdsCustomerURL"
    public static final String MICRO_GATEWAY_CDS_DISCOVERY_URL = "CdsDiscoveryURL"
    public static final String MICRO_GATEWAY_CDS_ARRANGEMENT_URL = "CdrArrangementURL"
    public static final String MICRO_GATEWAY_CDS_ADMIN_URL = "CdsAdminURL"

    // AU Mock-CDR-Register Constants
    public static final String MOCK_CDR_REGISTER = "AUMockCDRRegister"
    public static final String MOCK_CDR_REG_ENABLED= "Enabled"
    public static final String MOCK_CDR_REG_HOST_NAME = "HostName"
    public static final String MOCK_CDR_REG_META_DATA_FILE_LOC = "MetaDataFileLocation"
    public static final String MOCK_CDR_REG_TRANSPORT = "Transport"
    public static final String MOCK_CDR_REG_TRANS_KEYSTORE = "KeyStore"
    public static final String MOCK_CDR_REG_TRANS_KEYSTORE_LOC = "Location"
    public static final String MOCK_CDR_REG_TRANS_KEYSTORE_TYPE = "Type"
    public static final String MOCK_CDR_REG_TRANS_KEYSTORE_PWD = "Password"
    public static final String MOCK_CDR_REG_TRANS_TRUSTSTORE = "Truststore"
    public static final String MOCK_CDR_REG_TRANS_TRUSTSTORE_LOC = "Location"
    public static final String MOCK_CDR_REG_TRANS_TRUSTSTORE_TYPE = "Type"
    public static final String MOCK_CDR_REG_TRANS_TRUSTSTORE_PWD = "Password"
    public static final String MOCK_CDR_REG_APP = "Application"
    public static final String MOCK_CDR_REG_APP_KEY = "KeyStore"
    public static final String MOCK_CDR_REG_APP_KEY_LOC = "Location"
    public static final String MOCK_CDR_REG_APP_KEY_ALIAS = "Alias"
    public static final String MOCK_CDR_REG_APP_KEY_PWD = "Password"

    // Rest API configuration constants
    public static final String REST_API = "RESTApi"
    public static final String REST_API_DCR_ACCESS_TOKEN = "DCRAccessToken"
    public static final String REST_API_API_ID = "ApiId"

    // ID Permanence configuration constants
    public static final String ID_PERMANENCE = "IdPermanence"
    public static final String ID_PERMANENCE_SECRET_KEY = "SecretKey"

    //Profile Selection configuration constants
    public static final String PROFILE_SELECTION = "ProfileSelection"
    public static final String PROFILE_SELECTION_ENABLED = "Enabled"

    //Sharable Account URL
    public static final String SHARABLE_ACCOUNT_URL = "SharableAccountsURL"
}


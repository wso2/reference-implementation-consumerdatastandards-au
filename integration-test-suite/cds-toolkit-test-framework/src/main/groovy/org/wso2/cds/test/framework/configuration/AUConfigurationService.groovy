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

package org.wso2.cds.test.framework.configuration

import org.wso2.bfsi.test.framework.configuration.CommonConfigurationService
import org.wso2.bfsi.test.framework.constant.ConfigConstants
import org.wso2.cds.test.framework.constant.AUConfigConstants

/**
 * Class for provide configuration data to the AU layers and AU tests
 * This class provide OB configuration and AU configuration.
 */
class AUConfigurationService extends CommonConfigurationService {

    /**
     * Get Mock CDR Register enabled
     */
    boolean getMockCDREnabled() {
        if (configuration.get(AUConfigConstants.MOCK_CDR_REGISTER + "." + AUConfigConstants.MOCK_CDR_REG_ENABLED).equals("true")) {
            return true
        }
        return false
    }

    /**
     * Get Mock CDR Hostname
     */
    String getMockCDRHostname() {
        return configuration.get(AUConfigConstants.MOCK_CDR_REGISTER + "." + AUConfigConstants.MOCK_CDR_REG_HOST_NAME)
    }

    /**
     * Get Mock CDR Register Meta data file location
     */
    String getMockCDRMetaDataFileLoc() {
        return configuration.get(AUConfigConstants.MOCK_CDR_REGISTER + "." + AUConfigConstants.MOCK_CDR_REG_META_DATA_FILE_LOC)
    }

    /**
     * Get Mock CDR Register Transport OBKeyStore Location
     */
    String getMockCDRTransKeystoreLoc() {
        return configuration.get(AUConfigConstants.MOCK_CDR_REGISTER + "." + AUConfigConstants.MOCK_CDR_REG_TRANSPORT
                + "." + AUConfigConstants.MOCK_CDR_REG_TRANS_KEYSTORE + "." + AUConfigConstants.MOCK_CDR_REG_TRANS_KEYSTORE_LOC)
    }

    /**
     * Get Mock CDR Register Transport OBKeyStore Type
     */
    String getMockCDRTransKeystoreType() {
        return configuration.get(AUConfigConstants.MOCK_CDR_REGISTER + "." + AUConfigConstants.MOCK_CDR_REG_TRANSPORT
                + "." + AUConfigConstants.MOCK_CDR_REG_TRANS_KEYSTORE + "." + AUConfigConstants.MOCK_CDR_REG_TRANS_KEYSTORE_TYPE)
    }

    /**
     * Get Mock CDR Register Transport OBKeyStore Password
     */
    String getMockCDRTransKeystorePWD() {
        return configuration.get(AUConfigConstants.MOCK_CDR_REGISTER + "." + AUConfigConstants.MOCK_CDR_REG_TRANSPORT
                + "." + AUConfigConstants.MOCK_CDR_REG_TRANS_KEYSTORE + "." + AUConfigConstants.MOCK_CDR_REG_TRANS_KEYSTORE_PWD)
    }

    /**
     * Get Mock CDR Register Transport Truststore Location
     */
    String getMockCDRTransTruststoreLoc() {
        return configuration.get(AUConfigConstants.MOCK_CDR_REGISTER + "." + AUConfigConstants.MOCK_CDR_REG_TRANSPORT
                + "." + AUConfigConstants.MOCK_CDR_REG_TRANS_TRUSTSTORE + "." + AUConfigConstants.MOCK_CDR_REG_TRANS_TRUSTSTORE_LOC)
    }

    /**
     * Get Mock CDR Register Transport Truststore Type
     */
    String getMockCDRTransTruststoreType() {
        return configuration.get(AUConfigConstants.MOCK_CDR_REGISTER + "." + AUConfigConstants.MOCK_CDR_REG_TRANSPORT
                + "." + AUConfigConstants.MOCK_CDR_REG_TRANS_TRUSTSTORE + "." + AUConfigConstants.MOCK_CDR_REG_TRANS_TRUSTSTORE_TYPE)
    }

    /**
     * Get Mock CDR Register Transport Truststore Password
     */
    String getMockCDRTransTruststorePWD() {
        return configuration.get(AUConfigConstants.MOCK_CDR_REGISTER + "." + AUConfigConstants.MOCK_CDR_REG_TRANSPORT
                + "." + AUConfigConstants.MOCK_CDR_REG_TRANS_TRUSTSTORE + "." + AUConfigConstants.MOCK_CDR_REG_TRANS_TRUSTSTORE_PWD)
    }

    /**
     * Get Mock CDR Register Transport OBKeyStore Location
     */
    String getMockCDRAppKeystoreLoc() {
        return configuration.get(AUConfigConstants.MOCK_CDR_REGISTER + "." + AUConfigConstants.MOCK_CDR_REG_APP
                + "." + AUConfigConstants.MOCK_CDR_REG_APP_KEY + "." + AUConfigConstants.MOCK_CDR_REG_APP_KEY_LOC)
    }

    /**
     * Get Mock CDR Register Transport OBKeyStore Alias
     */
    String getMockCDRAppKeystoreAlias() {
        return configuration.get(AUConfigConstants.MOCK_CDR_REGISTER + "." + AUConfigConstants.MOCK_CDR_REG_APP
                + "." + AUConfigConstants.MOCK_CDR_REG_APP_KEY + "." + AUConfigConstants.MOCK_CDR_REG_APP_KEY_ALIAS)
    }

    /**
     * Get Mock CDR Register Transport OBKeyStore Password
     */
    String getMockCDRAppKeystorePWD() {
        return configuration.get(AUConfigConstants.MOCK_CDR_REGISTER + "." + AUConfigConstants.MOCK_CDR_REG_APP
                + "." + AUConfigConstants.MOCK_CDR_REG_APP_KEY + "." + AUConfigConstants.MOCK_CDR_REG_APP_KEY_PWD)
    }

    /**
     * Get Rest API DCR Access token
     */
    String getRestAPIDCRAccessToken() {
        return configuration.get(AUConfigConstants.REST_API + "." + AUConfigConstants.REST_API_DCR_ACCESS_TOKEN)
    }

    /**
     * Get Rest API ID
     */
    String getRestAPIID() {
        return configuration.get(AUConfigConstants.REST_API + "." + AUConfigConstants.REST_API_API_ID)
    }

    /**
     * Get Rest API ID
     */
    String getIDPermanence() {
        return configuration.get(AUConfigConstants.ID_PERMANENCE + "." + AUConfigConstants.ID_PERMANENCE_SECRET_KEY)
    }

    /**
     * Get Micro-Gateway Enabled
     */
    boolean getMicroGatewayEnabled() {
        if (configuration.get(AUConfigConstants.MICRO_GATEWAY + "." + AUConfigConstants.MICRO_GATEWAY_ENABLED).equals("true")) {
            return true
        } else {
            return false
        }
    }

    /**
     * Get Micro-Gateway DCR URL
     */
    String getMicroGatewayDCRUrl() {
        return configuration.get(AUConfigConstants.MICRO_GATEWAY + "." + AUConfigConstants.MICRO_GATEWAY_DCR_URL)
    }

    /**
     * Get Micro-Gateway CDS Accounts URL
     */
    String getMicroGatewayAccountsUrl() {
        return configuration.get(AUConfigConstants.MICRO_GATEWAY + "." + AUConfigConstants.MICRO_GATEWAY_CDS_ACCOUNTS_URL)
    }

    /**
     * Get Micro-Gateway CDS Balances URL
     */
    String getMicroGatewayBalancesUrl() {
        return configuration.get(AUConfigConstants.MICRO_GATEWAY + "." + AUConfigConstants.MICRO_GATEWAY_CDS_BALANCES_URL)
    }

    /**
     * Get Micro-Gateway Cds Au Transaction URL
     */
    String getMicroGatewayTransactionURL() {
        return configuration.get(AUConfigConstants.MICRO_GATEWAY + "." + AUConfigConstants.MICRO_GATEWAY_CDS_TRANSACTIONS_URL)
    }

    /**
     * Get Micro-Gateway Cds Au Direct-Debit URL
     */
    String getMicroGatewayDirectDebitURL() {
        return configuration.get(AUConfigConstants.MICRO_GATEWAY + "." + AUConfigConstants.MICRO_GATEWAY_CDS_DIRECT_DEBIT_URL)
    }

    /**
     * Get Micro-Gateway Cds Au Schedule-Payment URL
     */
    String getMicroGatewaySchedulePayURL() {
        return configuration.get(AUConfigConstants.MICRO_GATEWAY + "." + AUConfigConstants.MICRO_GATEWAY_CDS_SCHEDULED_PAY_URL)
    }

    /**
     * Get Micro-Gateway Cds Au Payee-URL
     */
    String getMicroGatewayPayeeURL() {
        return configuration.get(AUConfigConstants.MICRO_GATEWAY + "." + AUConfigConstants.MICRO_GATEWAY_CDS_PAYEE_URL)
    }

    /**
     * Get Micro-Gateway Cds Au ProductURL
     */
    String getMicroGatewayProductURL() {
        return configuration.get(AUConfigConstants.MICRO_GATEWAY + "." + AUConfigConstants.MICRO_GATEWAY_CDS_PRODUCT_URL)
    }

    /**
     * Get Micro-Gateway Cds Customer URL
     */
    String getMicroGatewayCustomerURL() {
        return configuration.get(AUConfigConstants.MICRO_GATEWAY + "." + AUConfigConstants.MICRO_GATEWAY_CDS_CUSTOMER_URL)
    }

    /**
     * Get Micro-Gateway Cds Discovery URL
     */
    String getMicroGatewayDiscoveryURL() {
        return configuration.get(AUConfigConstants.MICRO_GATEWAY + "." + AUConfigConstants.MICRO_GATEWAY_CDS_DISCOVERY_URL)
    }

    /**
     * Get Micro-Gateway Cdr Arrangement URL
     */
    String getMicroGatewayArrangementURL() {
        return configuration.get(AUConfigConstants.MICRO_GATEWAY + "." + AUConfigConstants.MICRO_GATEWAY_CDS_ARRANGEMENT_URL)
    }

    /**
     * Get Micro-Gateway Cds Admin URL
     */
    String getMicroGatewayAdminURL() {
        return configuration.get(AUConfigConstants.MICRO_GATEWAY + "." + AUConfigConstants.MICRO_GATEWAY_CDS_ADMIN_URL)
    }

    /**
     * Get Profile Selection Configurations
     */
    String getProfileSelectionEnabled() {
        return configuration.get(AUConfigConstants.PROFILE_SELECTION + "." + AUConfigConstants.PROFILE_SELECTION_ENABLED)
    }

    /**
     * Get Sharable Account Url
     */
    String getSharableAccountUrl() {
        return configuration.get(ConfigConstants.SERVER + "." + AUConfigConstants.SHARABLE_ACCOUNT_URL)
    }

    /**
     * Get DB Type
     */
    String getDbType() {
        return configuration.get(AUConfigConstants.DATA_BASE_CONFIGURATION + "." + AUConfigConstants.DB_TYPE)
    }

    /**
     * Get DB Server Host
     */
    String getDbServerHost() {
        return configuration.get(AUConfigConstants.DATA_BASE_CONFIGURATION + "." + AUConfigConstants.DB_SERVER_HOST)
    }

    /**
     * Get DB Username
     */
    String getDbUsername() {
        return configuration.get(AUConfigConstants.DATA_BASE_CONFIGURATION + "." + AUConfigConstants.DB_USERNAME)
    }

    /**
     * Get DB Password
     */
    String getDbPassword() {
        return configuration.get(AUConfigConstants.DATA_BASE_CONFIGURATION + "." + AUConfigConstants.DB_PASSWORD)
    }

    /**
     * Get DB Driver Class
     */
    String getDbDriverClass() {
        return configuration.get(AUConfigConstants.DATA_BASE_CONFIGURATION + "." + AUConfigConstants.DB_DRIVER_CLASS)
    }

    /**
     * Get Oracle DB SID
     */
    String getOracleSid() {
        return configuration.get(AUConfigConstants.DATA_BASE_CONFIGURATION + "." + AUConfigConstants.ORACLE_DB_SID)
    }

    /**
     * Get Provisioning Enabled
     */
    boolean isProvisioning() {
        if (configuration.get(AUConfigConstants.PROVISIONING + "." + AUConfigConstants.PROVISIONING_ENABLED).equals("true")) {
            return true
        } else {
            return false
        }
    }

    /**
     * Get Provisioning File Path
     */
    String getProvisionFilePath() {
        return configuration.get(AUConfigConstants.PROVISIONING + "." + AUConfigConstants.PROVISIONING_FILE_PATH)
    }


}

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

package com.wso2.cds.test.framework.keystore

import com.wso2.bfsi.test.framework.exception.TestFrameworkException
import com.wso2.openbanking.test.framework.keystore.OBKeyStore
import com.wso2.cds.test.framework.configuration.AUConfigurationService
import java.security.Key
import java.security.KeyStore
import java.security.cert.Certificate

/**
 * Class for provide keystore functions for AU Layer
 */
class AUKeyStore extends OBKeyStore{

    private static AUConfigurationService auConfiguration = new AUConfigurationService()

    /**
     * Get Mock-CDR register application Keystore
     * @return
     * @throws TestFrameworkException
     */
    static KeyStore getMockCDRApplicationKeyStore() throws TestFrameworkException {
        return getKeyStore(auConfiguration.getMockCDRAppKeystoreLoc(),auConfiguration.getMockCDRAppKeystorePWD());
    }

    /**
     * Get Mock-CDR register application Keystore Certificate
     * @return
     * @throws TestFrameworkException
     */
    static Certificate getCertificateFromMockCDRKeyStore() throws TestFrameworkException {
        KeyStore keyStore = getKeyStore(auConfiguration.getMockCDRAppKeystoreLoc(),auConfiguration.getMockCDRAppKeystorePWD())
        return getCertificate(keyStore
                ,auConfiguration.getMockCDRAppKeystoreAlias(),auConfiguration.getMockCDRAppKeystorePWD())
    }

    /**
     * Get Mock-CDR register Signing key
     * @return
     * @throws TestFrameworkException
     */
    static Key getMockCDRSigningKey() throws TestFrameworkException {
        return getSigningKey(auConfiguration.getMockCDRAppKeystoreLoc(),auConfiguration.getMockCDRAppKeystorePWD()
                ,auConfiguration.getMockCDRAppKeystoreAlias())
    }

}


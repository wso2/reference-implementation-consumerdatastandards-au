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

package com.wso2.cds.test.framework.utility

import com.wso2.bfsi.test.framework.exception.TestFrameworkException
import com.wso2.cds.test.framework.configuration.AUConfigurationService
import org.apache.http.conn.ssl.SSLSocketFactory

import java.security.KeyManagementException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.UnrecoverableKeyException
import java.security.cert.CertificateException

/**
 * Class for create Socket factory
 */
class AUSSLSocketFactoryCreator {

    AUConfigurationService auConfiguration

    AUSSLSocketFactoryCreator() {
        auConfiguration = new AUConfigurationService()
    }

    /**
     * Create SSL socket factory to invoke the AU mock CDR Register
     *
     * @return an SSLSocketFactory implementation
     * @throws TestFrameworkException when an error occurs while loading the keystore and truststore
     */
    SSLSocketFactory createForMockCDRRegister() throws TestFrameworkException {
        try {

            FileInputStream keystoreLocation = new FileInputStream(new File(auConfiguration.getMockCDRTransKeystoreLoc()))
            FileInputStream truststoreLocation = new FileInputStream(new File(auConfiguration.getMockCDRTransTruststoreLoc()))

            KeyStore keyStore = KeyStore.getInstance(auConfiguration.getMockCDRTransKeystoreType());
            keyStore.load(keystoreLocation, auConfiguration.getMockCDRTransKeystorePWD().toCharArray());
            KeyStore trustStore = KeyStore.getInstance(auConfiguration.getMockCDRTransTruststoreType());
            trustStore.load(truststoreLocation, auConfiguration.getMockCDRTransTruststorePWD().toCharArray());

            return new SSLSocketFactory(keyStore, auConfiguration.getMockCDRTransKeystorePWD(), trustStore)

        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | KeyManagementException | UnrecoverableKeyException | IOException e) {
            throw new TestFrameworkException("Unable to load the transport keystore and truststore", e);
        }
    }
}


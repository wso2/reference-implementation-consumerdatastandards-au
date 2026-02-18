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

package org.wso2.openbanking.consumerdatastandards.au.extensions.config;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;

import javax.xml.stream.XMLStreamException;

/**
 * Configuration parser to read basic auth credentials from financial-services.xml.
 * Reads the same configuration as the Financial Services Accelerator.
 */
public class AccountMetadataConfigParser {

    private static final Log log = LogFactory.getLog(AccountMetadataConfigParser.class);
    private static final Object lock = new Object();
    private static AccountMetadataConfigParser instance;
    
    private static final String FS_CONFIG_FILE = "financial-services.xml";

    // Configuration keys
    private static final String SERVICE_EXTENSIONS_BASIC_AUTH_USERNAME = 
            "ExtensionsEndpoint.Security.Username";
    private static final String SERVICE_EXTENSIONS_BASIC_AUTH_PASSWORD = 
            "ExtensionsEndpoint.Security.Password";

    private final Map<String, Object> configuration = new HashMap<>();
    private SecretResolver secretResolver;

    /**
     * Private constructor.
     */
    private AccountMetadataConfigParser() {
        buildConfiguration();
    }

    /**
     * Singleton getInstance method.
     *
     * @return AccountMetadataConfigParser instance
     */
    public static AccountMetadataConfigParser getInstance() {
        synchronized (lock) {
            if (instance == null) {
                instance = new AccountMetadataConfigParser();
            }
        }
        return instance;
    }

    /**
     * Read configuration from financial-services.xml.
     */
    private void buildConfiguration() {
        InputStream inStream = null;
        StAXOMBuilder builder;

        try {
            // Try to read from CARBON_CONFIG_DIR_PATH/financial-services.xml
            String carbonConfigPath = System.getProperty("carbon.config.dir.path");
            if (StringUtils.isNotBlank(carbonConfigPath)) {
                File configFile = new File(carbonConfigPath, FS_CONFIG_FILE);
                if (configFile.exists()) {
                    inStream = new FileInputStream(configFile);
                    if (log.isDebugEnabled()) {
                        log.debug("Reading financial-services.xml from: " + configFile.getAbsolutePath());
                    }
                }
            }

            if (inStream != null) {
                builder = new StAXOMBuilder(inStream);
                OMElement rootElement = builder.getDocumentElement();
                secretResolver = SecretResolverFactory.create(rootElement, true);
                Stack<String> nameStack = new Stack<>();
                readChildElements(rootElement, nameStack);
                
                if (log.isDebugEnabled()) {
                    log.debug("Successfully loaded configuration from financial-services.xml");
                }
            } else {
                log.warn("financial-services.xml not found. Using default configuration.");
            }

        } catch (IOException | XMLStreamException | OMException e) {
            log.error("Error reading financial-services.xml. Using default configuration.", e);
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    log.error("Error closing input stream", e);
                }
            }
        }
    }

    /**
     * Read child elements recursively.
     */
    private void readChildElements(OMElement element, Stack<String> nameStack) {
        for (Iterator it = element.getChildElements(); it.hasNext(); ) {
            Object childObj = it.next();
            OMElement child = (OMElement) childObj;
            nameStack.push(child.getLocalName());

            if (elementHasText(child)) {
                String key = String.join(".", nameStack);
                String value = child.getText().trim();
                
                // Resolve encrypted values
                if (secretResolver != null && secretResolver.isInitialized() && 
                    secretResolver.isTokenProtected(key)) {
                    value = secretResolver.resolve(key);
                }
                
                configuration.put(key, value);
            }

            readChildElements(child, nameStack);
            nameStack.pop();
        }
    }

    /**
     * Check if element has text.
     */
    private boolean elementHasText(OMElement element) {
        String text = element.getText();
        return text != null && !text.trim().isEmpty();
    }

    /**
     * Get configuration value as string.
     */
    private Optional<String> getConfigValue(String key) {
        return Optional.ofNullable((String) configuration.get(key));
    }

    /**
     * Get service extensions basic auth username.
     * Same as FinancialServicesConfigParser.getServiceExtensionsEndpointSecurityBasicAuthUsername()
     *
     * @return username or null
     */
    public String getServiceExtensionsBasicAuthUsername() {
        Optional<String> config = getConfigValue(SERVICE_EXTENSIONS_BASIC_AUTH_USERNAME);
        return config.map(String::trim).orElse(null);
    }

    /**
     * Get service extensions basic auth password.
     * Same as FinancialServicesConfigParser.getServiceExtensionsEndpointSecurityBasicAuthPassword()
     *
     * @return password or null
     */
    public String getServiceExtensionsBasicAuthPassword() {
        Optional<String> config = getConfigValue(SERVICE_EXTENSIONS_BASIC_AUTH_PASSWORD);
        return config.map(String::trim).orElse(null);
    }

}

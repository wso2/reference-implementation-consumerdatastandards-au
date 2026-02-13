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

package org.wso2.openbanking.consumerdatastandards.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.openbanking.consumerdatastandards.configurations.ConfigurableProperties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.sql.Connection;
import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Utility class for connecting to Account Metadata Database.
 */
public class DatabaseUtil {

    private static final Logger log = LoggerFactory.getLogger(DatabaseUtil.class);
    private static final DataSource dataSource;

    static {
        try {
            InitialContext ctx = new InitialContext();
            String jndiName = ConfigurableProperties.ACCOUNT_METADATA_DATASOURCE_JNDI_NAME;

            log.debug("Initializing Account Metadata datasource using JNDI name: {}", jndiName);

            dataSource = (DataSource) ctx.lookup(jndiName);

            log.debug("Account Metadata datasource initialized successfully");

        } catch (NamingException e) {
            log.error("JNDI lookup failed for Account Metadata datasource", e);
            throw new ExceptionInInitializerError(e);

        } catch (ClassCastException e) {
            log.error("JNDI resource is not a DataSource: {}",
                    ConfigurableProperties.ACCOUNT_METADATA_DATASOURCE_JNDI_NAME, e);
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}

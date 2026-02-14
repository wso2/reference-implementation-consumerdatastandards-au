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

package org.wso2.openbanking.consumerdatastandards.utils.connection.provider;

import org.wso2.openbanking.consumerdatastandards.utils.DatabaseUtil;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Provides database connections to the service layer.
 * This abstraction allows the underlying connection retrieval mechanism
 * (e.g., direct JDBC connection, connection pooling, JNDI datasource)
 * to be changed without impacting the business logic. It also improves
 * testability by enabling mock implementations during unit testing.
 */
public class DatabaseConnectionProvider implements ConnectionProvider {

    /**
     * {@inheritDoc}
     */
    @Override
    public Connection getConnection() throws SQLException {
        return DatabaseUtil.getConnection();
    }
}

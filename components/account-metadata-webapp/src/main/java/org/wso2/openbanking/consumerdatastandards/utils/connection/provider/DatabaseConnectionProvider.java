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
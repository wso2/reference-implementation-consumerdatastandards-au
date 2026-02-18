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

package org.wso2.openbanking.consumerdatastandards.service.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.consumerdatastandards.exceptions.AccountMetadataException;
import org.wso2.openbanking.consumerdatastandards.service.dao.AccountMetadataDAO;
import org.wso2.openbanking.consumerdatastandards.service.dao.AccountMetadataDAOImpl;
import org.wso2.openbanking.consumerdatastandards.service.dao.queries.AccountMetadataDBQueriesMySQLImpl;
import org.wso2.openbanking.consumerdatastandards.utils.connection.provider.ConnectionProvider;
import org.wso2.openbanking.consumerdatastandards.utils.connection.provider.DatabaseConnectionProvider;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class AccountMetadataServiceImpl implements AccountMetadataService {

    private static volatile AccountMetadataServiceImpl instance = null;

    private final AccountMetadataDAO metadataDAO;
    private final ConnectionProvider connectionProvider;
    private static final Log log = LogFactory.getLog(AccountMetadataServiceImpl.class);


    // private constructor
    private AccountMetadataServiceImpl() {
        this(new AccountMetadataDAOImpl(new AccountMetadataDBQueriesMySQLImpl()), new DatabaseConnectionProvider());
    }

    private AccountMetadataServiceImpl(AccountMetadataDAO metadataDAO) {
        this(metadataDAO, new DatabaseConnectionProvider());
    }

    private AccountMetadataServiceImpl(AccountMetadataDAO metadataDAO, ConnectionProvider connectionProvider) {
        this.metadataDAO = metadataDAO;
        this.connectionProvider = connectionProvider;
    }

    /**
     * @return AccountMetadataServiceImpl instance
     */
    public static synchronized AccountMetadataServiceImpl getInstance() {

        if (instance == null) {
            instance = new AccountMetadataServiceImpl();
        }
        return instance;
    }

    /**
     * @param metadataDAO the DAO implementation to use
     * @return AccountMetadataServiceImpl instance
     */
    public static synchronized AccountMetadataServiceImpl getInstance(AccountMetadataDAO metadataDAO) {

        if (instance == null) {
            instance = new AccountMetadataServiceImpl(metadataDAO);
        }
        return instance;
    }

    /**
     * @param metadataDAO the DAO implementation to use
     * @param connectionProvider the connection provider to use
     * @return AccountMetadataServiceImpl instance
     */
    public static synchronized AccountMetadataServiceImpl getInstance(AccountMetadataDAO metadataDAO,
            ConnectionProvider connectionProvider) {
        if (instance == null) {
            instance = new AccountMetadataServiceImpl(metadataDAO, connectionProvider);
        }
        return instance;
    }

    /**
     * Resets the singleton instance. For testing purposes only.
     */
    static synchronized void resetInstance() {
        instance = null;
    }

    /**
     * Batch retrieve disclosure options for multiple accounts.
     *
     * @param accountIds the list of account IDs
     * @return map of account ID to disclosure option status
     * @throws AccountMetadataException if an error occurs
     */
    @Override
    public Map<String, String> getBatchDisclosureOptions(List<String> accountIds)
            throws AccountMetadataException {

        try (Connection conn = connectionProvider.getConnection()) {
            return metadataDAO.getBatchDisclosureOptions(conn, accountIds);
        } catch (SQLException e) {
            log.error("Error batch retrieving disclosure options", e);
            throw new AccountMetadataException("Failed to batch retrieve disclosure options", e);
        }
    }

    /**
     * Batch add disclosure options for multiple accounts.
     *
     * @param accountDisclosureMap map of account ID to disclosure option status
     * @throws AccountMetadataException if an error occurs
     */
    @Override
    public void addBatchDisclosureOptions(Map<String, String> accountDisclosureMap)
            throws AccountMetadataException {

        try (Connection conn = connectionProvider.getConnection()) {
            metadataDAO.addBatchDisclosureOptions(conn, accountDisclosureMap);
        } catch (SQLException e) {
            log.error("Error batch adding disclosure options", e);
            throw new AccountMetadataException("Failed to batch add disclosure options", e);
        }
    }

    /**
     * Batch update disclosure options for multiple accounts.
     *
     * @param accountDisclosureMap map of account ID to disclosure option status
     * @throws AccountMetadataException if an error occurs
     */
    @Override
    public void updateBatchDisclosureOptions(Map<String, String> accountDisclosureMap)
            throws AccountMetadataException {

        try (Connection conn = connectionProvider.getConnection()) {
            metadataDAO.updateBatchDisclosureOptions(conn, accountDisclosureMap);
        } catch (SQLException e) {
            log.error("Error batch updating disclosure options", e);
            throw new AccountMetadataException("Failed to batch update disclosure options", e);
        }
    }

}

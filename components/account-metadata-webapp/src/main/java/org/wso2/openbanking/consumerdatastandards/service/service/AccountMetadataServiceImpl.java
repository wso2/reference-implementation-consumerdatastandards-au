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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.openbanking.consumerdatastandards.exceptions.AccountMetadataException;
import org.wso2.openbanking.consumerdatastandards.service.dao.AccountMetadataDAO;
import org.wso2.openbanking.consumerdatastandards.service.dao.AccountMetadataDAOImpl;
import org.wso2.openbanking.consumerdatastandards.service.dao.queries.AccountMetadataDBQueriesMySQLImpl;
import org.wso2.openbanking.consumerdatastandards.utils.connection.provider.ConnectionProvider;
import org.wso2.openbanking.consumerdatastandards.utils.connection.provider.DatabaseConnectionProvider;

import java.sql.Connection;
import java.sql.SQLException;

public class AccountMetadataServiceImpl implements AccountMetadataService {

    private static volatile AccountMetadataServiceImpl instance = null;

    private final AccountMetadataDAO metadataDAO;
    private final ConnectionProvider connectionProvider;
    private static final Logger log = LoggerFactory.getLogger(AccountMetadataServiceImpl.class);


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
     * Add disclosure option for a joint account.
     *
     * @param accountId the account ID
     * @param disclosureOptionStatus the disclosure option status (e.g., "pre-approval", "no-sharing")
     * @throws AccountMetadataException if an error occurs
     */
    public void addDisclosureOption(String accountId, String disclosureOptionStatus)
            throws AccountMetadataException {

        if (StringUtils.isBlank(accountId) || StringUtils.isBlank(disclosureOptionStatus)) {
            throw new AccountMetadataException("Account ID or disclosure option status is not provided.");
        }

        try (Connection conn = connectionProvider.getConnection()) {
            metadataDAO.addDisclosureOption(conn, accountId, disclosureOptionStatus);
        } catch (AccountMetadataException | SQLException e) {
            log.error("Error while adding disclosure option for accountId: {}", accountId, e);
            throw new AccountMetadataException(
                    "Failed to add disclosure option for accountId: " + accountId, e);
        }
    }

    /**
     * Update disclosure option for a joint account.
     *
     * @param accountId the account ID
     * @param disclosureOptionStatus the disclosure option status (e.g., "pre-approval", "no-sharing")
     * @throws AccountMetadataException if an error occurs
     */
    public void updateDisclosureOption(String accountId, String disclosureOptionStatus)
            throws AccountMetadataException {

        if (StringUtils.isBlank(accountId) || StringUtils.isBlank(disclosureOptionStatus)) {
            throw new AccountMetadataException("Account ID or disclosure option status is not provided.");
        }

        try (Connection conn = connectionProvider.getConnection()) {
            metadataDAO.updateDisclosureOption(conn, accountId, disclosureOptionStatus);
        } catch (AccountMetadataException | SQLException e) {
            log.error("Error while updating disclosure option for accountId: {}", accountId, e);
            throw new AccountMetadataException(
                    "Failed to update disclosure option for accountId: " + accountId, e);
        }
    }

    /**
     * Retrieve disclosure option status for a joint account.
     *
     * @param accountId the account ID
     * @return the disclosure option status, or null if not found
     * @throws AccountMetadataException if an error occurs
     */
    public String getDisclosureOption(String accountId) throws AccountMetadataException {

        if (StringUtils.isBlank(accountId)) {
            throw new AccountMetadataException("Account ID is invalid");
        }

        try (Connection conn = connectionProvider.getConnection()) {
            return metadataDAO.getDisclosureOption(conn, accountId);
        } catch (AccountMetadataException | SQLException e) {
            log.error("Error retrieving disclosure option for accountId: {}", accountId, e);
            throw new AccountMetadataException("Failed to retrieve disclosure option", e);
        }
    }
}

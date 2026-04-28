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

package org.wso2.openbanking.consumerdatastandards.account.metadata.service.core;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.consumerdatastandards.account.metadata.exceptions.AccountMetadataException;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.BusinessStakeholderPermissionItem;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.LegalEntitySharingItem;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.SecondaryAccountInstructionItem;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.dao.AccountMetadataDAO;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.dao.AccountMetadataDAOImpl;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.dao.queries.AccountMetadataDbQueriesMySqlImpl;
import org.wso2.openbanking.consumerdatastandards.account.metadata.utils.connection.provider.ConnectionProvider;
import org.wso2.openbanking.consumerdatastandards.account.metadata.utils.connection.provider.DatabaseConnectionProvider;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Implementation of account metadata core operations.
 */
public class AccountMetadataServiceImpl implements AccountMetadataService {

    private static volatile AccountMetadataServiceImpl instance = null;

    private final AccountMetadataDAO metadataDAO;
    private final ConnectionProvider connectionProvider;
    private static final Log log = LogFactory.getLog(AccountMetadataServiceImpl.class);


    // private constructor
    private AccountMetadataServiceImpl() {
        this(new AccountMetadataDAOImpl(new AccountMetadataDbQueriesMySqlImpl()), new DatabaseConnectionProvider());
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
    public Map<String, String> getBatchDisclosureOptions(List<String> accountIds) throws AccountMetadataException {

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
    public void addBatchDisclosureOptions(Map<String, String> accountDisclosureMap) throws AccountMetadataException {

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
    public void updateBatchDisclosureOptions(Map<String, String> accountDisclosureMap) throws AccountMetadataException {

        try (Connection conn = connectionProvider.getConnection()) {
            metadataDAO.updateBatchDisclosureOptions(conn, accountDisclosureMap);
        } catch (SQLException e) {
            log.error("Error batch updating disclosure options", e);
            throw new AccountMetadataException("Failed to batch update disclosure options", e);
        }
    }

    /**
     * Batch retrieve secondary account instructions for multiple account-user pairs.
     *
     * @param accountUserPairs list of account-user pairs
     * @return list of secondary account instruction records
     * @throws AccountMetadataException if an error occurs
     */
    @Override
    public List<SecondaryAccountInstructionItem> getBatchSecondaryAccountInstructions(
            List<Pair<String, String>> accountUserPairs)
            throws AccountMetadataException {

        try (Connection conn = connectionProvider.getConnection()) {
            return metadataDAO.getBatchSecondaryAccountInstructions(conn, accountUserPairs);
        } catch (SQLException e) {
            log.error("Error batch retrieving secondary account instructions", e);
            throw new AccountMetadataException("Failed to batch retrieve secondary account instructions", e);
        }
    }

    /**
     * Batch add secondary account instructions.
     *
     * @param instructionItems list of secondary account instruction records
     * @throws AccountMetadataException if an error occurs
     */
    @Override
    public void addBatchSecondaryAccountInstructions(List<SecondaryAccountInstructionItem> instructionItems)
            throws AccountMetadataException {

        try (Connection conn = connectionProvider.getConnection()) {
            metadataDAO.addBatchSecondaryAccountInstructions(conn, instructionItems);
        } catch (SQLException e) {
            log.error("Error batch adding secondary account instructions", e);
            throw new AccountMetadataException("Failed to batch add secondary account instructions", e);
        }
    }

    /**
     * Batch update secondary account instructions.
     *
     * @param instructionItems list of secondary account instruction records
     * @throws AccountMetadataException if an error occurs
     */
    @Override
    public void updateBatchSecondaryAccountInstructions(List<SecondaryAccountInstructionItem> instructionItems)
            throws AccountMetadataException {

        try (Connection conn = connectionProvider.getConnection()) {
            metadataDAO.updateBatchSecondaryAccountInstructions(conn, instructionItems);
        } catch (SQLException e) {
            log.error("Error batch updating secondary account instructions", e);
            throw new AccountMetadataException("Failed to batch update secondary account instructions", e);
        }
    }

    /**
     * Retrieve all legal entity sharing status records for multiple account-user pairs.
     *
     * @param accountUserPairs list of (accountId, userId) pairs to query
     * @return list of legal entity sharing status records
     * @throws AccountMetadataException if an error occurs
     */
    @Override
    public List<LegalEntitySharingItem> getBatchLegalEntitySharingStatuses(List<Pair<String, String>> accountUserPairs)
            throws AccountMetadataException {

        try (Connection conn = connectionProvider.getConnection()) {
            return metadataDAO.getBatchLegalEntitySharingStatuses(conn, accountUserPairs);
        } catch (SQLException e) {
            log.error("Error batch retrieving legal entity sharing statuses", e);
            throw new AccountMetadataException("Failed to batch retrieve legal entity sharing statuses", e);
        }
    }

    /**
     * Upsert legal entity sharing status records.
     *
     * @param items legal entity sharing items to upsert
     * @throws AccountMetadataException if an error occurs
     */
    @Override
    public void upsertBatchLegalEntitySharingStatuses(List<LegalEntitySharingItem> items)
            throws AccountMetadataException {

        try (Connection conn = connectionProvider.getConnection()) {
            metadataDAO.upsertBatchLegalEntitySharingStatuses(conn, items);
        } catch (SQLException e) {
            log.error("Error batch upserting legal entity sharing statuses", e);
            throw new AccountMetadataException("Failed to batch upsert legal entity sharing statuses", e);
        }
    }

    /**
     * Batch retrieve business stakeholder permissions for multiple account-user pairs.
     *
     * @param accountUserPairs list of (accountId, userId) pairs to query
     * @return list of existing business stakeholder permission records
     * @throws AccountMetadataException if an error occurs
     */
    @Override
    public List<BusinessStakeholderPermissionItem> getBatchBusinessStakeholderPermissions(
            List<Pair<String, String>> accountUserPairs) throws AccountMetadataException {

        try (Connection conn = connectionProvider.getConnection()) {
            return metadataDAO.getBatchBusinessStakeholderPermissions(conn, accountUserPairs);
        } catch (SQLException e) {
            log.error("Error batch retrieving business stakeholder permissions", e);
            throw new AccountMetadataException("Failed to batch retrieve business stakeholder permissions", e);
        }
    }

    /**
     * Batch retrieve business stakeholder permissions for multiple account IDs.
     *
     * @param accountIds list of account IDs
     * @return list of existing business stakeholder permission records
     * @throws AccountMetadataException if an error occurs
     */
    @Override
    public List<BusinessStakeholderPermissionItem> getBatchBusinessStakeholderPermissionsByAccountIds(
            List<String> accountIds) throws AccountMetadataException {

        try (Connection conn = connectionProvider.getConnection()) {
            return metadataDAO.getBatchBusinessStakeholderPermissionsByAccountIds(conn, accountIds);
        } catch (SQLException e) {
            log.error("Error batch retrieving business stakeholder permissions by account IDs", e);
            throw new AccountMetadataException("Failed to batch retrieve business stakeholder permissions by " +
                    "account IDs", e);
        }
    }

    /**
     * Batch add business stakeholder permission records.
     *
     * @param permissionItems list of account-user permission records
     * @throws AccountMetadataException if an error occurs
     */
    @Override
    public void addBatchBusinessStakeholderPermissions(List<BusinessStakeholderPermissionItem> permissionItems)
            throws AccountMetadataException {

        try (Connection conn = connectionProvider.getConnection()) {
            metadataDAO.addBatchBusinessStakeholderPermissions(conn, permissionItems);
        } catch (SQLException e) {
            log.error("Error batch adding business stakeholder permissions", e);
            throw new AccountMetadataException("Failed to batch add business stakeholder permissions", e);
        }
    }

    /**
     * Batch update business stakeholder permission records.
     *
     * @param permissionItems list of account-user permission records
     * @throws AccountMetadataException if an error occurs
     */
    @Override
    public void updateBatchBusinessStakeholderPermissions(List<BusinessStakeholderPermissionItem> permissionItems)
            throws AccountMetadataException {

        try (Connection conn = connectionProvider.getConnection()) {
            metadataDAO.updateBatchBusinessStakeholderPermissions(conn, permissionItems);
        } catch (SQLException e) {
            log.error("Error batch updating business stakeholder permissions", e);
            throw new AccountMetadataException("Failed to batch update business stakeholder permissions", e);
        }
    }

    /**
     * Batch delete business stakeholder permission records.
     *
     * @param permissionItems list of account-user permission records
     * @throws AccountMetadataException if an error occurs
     */
    @Override
    public void deleteBatchBusinessStakeholderPermissions(List<BusinessStakeholderPermissionItem> permissionItems)
            throws AccountMetadataException {

        try (Connection conn = connectionProvider.getConnection()) {
            metadataDAO.deleteBatchBusinessStakeholderPermissions(conn, permissionItems);
        } catch (SQLException e) {
            log.error("Error batch deleting business stakeholder permissions", e);
            throw new AccountMetadataException("Failed to batch delete business stakeholder permissions", e);
        }
    }
}

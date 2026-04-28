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

package org.wso2.openbanking.consumerdatastandards.account.metadata.service.dao;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.consumerdatastandards.account.metadata.exceptions.AccountMetadataException;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.BusinessStakeholderPermissionItem;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.SecondaryAccountInstructionItem;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.dao.queries.AccountMetadataDbQueries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of account metadata data access operations.
 */
public class  AccountMetadataDAOImpl implements AccountMetadataDAO {

    private static final Log log = LogFactory.getLog(AccountMetadataDAOImpl.class);
    // Column names for DOMS table.
    private static final String DISCLOSURE_OPTIONS_COLUMN_ACCOUNT_ID = "ACCOUNT_ID";
    private static final String DISCLOSURE_OPTIONS_COLUMN_STATUS = "DISCLOSURE_OPTION_STATUS";
    // Column names for secondary user instructions table.
    private static final String SECONDARY_INSTRUCTIONS_COLUMN_ACCOUNT_ID = "ACCOUNT_ID";
    private static final String SECONDARY_INSTRUCTIONS_COLUMN_USER_ID = "USER_ID";
    private static final String SECONDARY_INSTRUCTIONS_COLUMN_STATUS =
            "INSTRUCTION_STATUS";
    private static final String SECONDARY_INSTRUCTIONS_COLUMN_OTHER_ACCOUNTS_AVAILABILITY =
        "OTHER_ACCOUNTS_AVAILABILITY";
    // Column names for business stakeholder permissions table.
    private static final String BNR_PERMISSIONS_COLUMN_ACCOUNT_ID = "ACCOUNT_ID";
    private static final String BNR_PERMISSIONS_COLUMN_USER_ID = "USER_ID";
    private static final String BNR_PERMISSIONS_COLUMN_PERMISSION = "PERMISSION";

    private final AccountMetadataDbQueries dbQueries;

    /**
     * Constructs a new DAO with the specified query provider.
     *
     * @param sqlStatements the database query provider
     */
    public AccountMetadataDAOImpl(AccountMetadataDbQueries sqlStatements) {
        this.dbQueries = sqlStatements;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getBatchDisclosureOptions(Connection conn, List<String> accountIds)
            throws AccountMetadataException {

        if (accountIds == null || accountIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String sql = dbQueries.getBatchGetDisclosureOptionQuery(accountIds.size());
        Map<String, String> resultMap = new HashMap<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < accountIds.size(); i++) {
                stmt.setString(i + 1, accountIds.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultMap.put(rs.getString(DISCLOSURE_OPTIONS_COLUMN_ACCOUNT_ID),
                            rs.getString(DISCLOSURE_OPTIONS_COLUMN_STATUS));
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Retrieved disclosure options for " +  resultMap.size() + " accounts.");
            }
            return resultMap;

        } catch (SQLException e) {
            log.error("Error retrieving batch disclosure options", e);
            throw new AccountMetadataException("Failed to retrieve batch disclosure options", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addBatchDisclosureOptions(Connection conn, Map<String, String> accountDisclosureMap)
            throws AccountMetadataException {

        if (accountDisclosureMap == null || accountDisclosureMap.isEmpty()) {
            return;
        }

        String sql = dbQueries.getBatchAddDisclosureOptionQuery();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            Timestamp currentTimestamp =  new Timestamp((new Date()).getTime());
            for (Map.Entry<String, String> entry : accountDisclosureMap.entrySet()) {
                stmt.setString(1, entry.getKey());
                stmt.setString(2, entry.getValue());
                stmt.setTimestamp(3, currentTimestamp);
                stmt.addBatch();
            }

            int[] results = stmt.executeBatch();
            if (log.isDebugEnabled()) {
                log.debug("Batch added disclosure options for " + results.length + " accounts.");
            }

        } catch (SQLException e) {
            log.error("Error batch adding disclosure options", e);
            throw new AccountMetadataException("Failed to batch add disclosure options", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateBatchDisclosureOptions(Connection conn, Map<String, String> accountDisclosureMap)
            throws AccountMetadataException {

        if (accountDisclosureMap == null || accountDisclosureMap.isEmpty()) {
            return;
        }

        String sql = dbQueries.getBatchUpdateDisclosureOptionQuery();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            Timestamp currentTimestamp =  new Timestamp((new Date()).getTime());
            for (Map.Entry<String, String> entry : accountDisclosureMap.entrySet()) {
                stmt.setString(1, entry.getValue());
                stmt.setTimestamp(2, currentTimestamp);
                stmt.setString(3, entry.getKey());
                stmt.addBatch();
            }

            int[] results = stmt.executeBatch();
            if (log.isDebugEnabled()) {
                log.debug("Batch updated disclosure options for " + results.length + " accounts.");
            }

        } catch (SQLException e) {
            log.error("Error batch updating disclosure options", e);
            throw new AccountMetadataException("Failed to batch update disclosure options", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SecondaryAccountInstructionItem> getBatchSecondaryAccountInstructions(Connection conn,
            List<Pair<String, String>> accountUserPairs) throws AccountMetadataException {

        if (accountUserPairs == null || accountUserPairs.isEmpty()) {
            throw new AccountMetadataException("Account-user pair list cannot be null or empty " +
                    "when retrieving Secondary account instructions");
        }

        int pairCount = accountUserPairs.size();

        String sql = dbQueries.getBatchGetSecondaryAccountInstructionQuery(pairCount);
        List<SecondaryAccountInstructionItem> resultItems = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int parameterIndex = 1;
            for (Pair<String, String> accountUserPair : accountUserPairs) {
                stmt.setString(parameterIndex++, accountUserPair.getLeft());
                stmt.setString(parameterIndex++, accountUserPair.getRight());
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    SecondaryAccountInstructionItem instructionItem = new SecondaryAccountInstructionItem();
                    instructionItem.setAccountId(rs.getString(SECONDARY_INSTRUCTIONS_COLUMN_ACCOUNT_ID));
                    instructionItem.setSecondaryUserId(rs.getString(SECONDARY_INSTRUCTIONS_COLUMN_USER_ID));
                    instructionItem.setSecondaryAccountInstructionStatus(
                            SecondaryAccountInstructionItem.SecondaryAccountInstructionStatusEnum.fromValue(
                                    rs.getString(SECONDARY_INSTRUCTIONS_COLUMN_STATUS)));
                    instructionItem.setOtherAccountsAvailability(
                        rs.getBoolean(SECONDARY_INSTRUCTIONS_COLUMN_OTHER_ACCOUNTS_AVAILABILITY));
                    resultItems.add(instructionItem);
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("Retrieved secondary account instructions for " + resultItems.size() + " records.");
            }
            return resultItems;

        } catch (SQLException e) {
            log.error("Error retrieving batch secondary account instructions", e);
            throw new AccountMetadataException("Failed to retrieve batch secondary account instructions", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addBatchSecondaryAccountInstructions(Connection conn,
            List<SecondaryAccountInstructionItem> instructionItems) throws AccountMetadataException {

        if (instructionItems == null || instructionItems.isEmpty()) {
            return;
        }

        String sql = dbQueries.getBatchAddSecondaryAccountInstructionQuery();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            Timestamp currentTimestamp = new Timestamp((new Date()).getTime());

            for (SecondaryAccountInstructionItem item : instructionItems) {
                stmt.setString(1, item.getAccountId());
                stmt.setString(2, item.getSecondaryUserId());
                stmt.setString(3, String.valueOf(item.getSecondaryAccountInstructionStatus()));
                stmt.setObject(4, item.getOtherAccountsAvailability(), Types.BOOLEAN);
                stmt.setTimestamp(5, currentTimestamp);
                stmt.addBatch();
            }

            int[] results = stmt.executeBatch();
            if (log.isDebugEnabled()) {
                log.debug("Batch added secondary account instructions for " + results.length + " records.");
            }

        } catch (SQLException e) {
            log.error("Error batch adding secondary account instructions", e);
            throw new AccountMetadataException("Failed to batch add secondary account instructions", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateBatchSecondaryAccountInstructions(Connection conn,
            List<SecondaryAccountInstructionItem> instructionItems) throws AccountMetadataException {

        if (instructionItems == null || instructionItems.isEmpty()) {
            return;
        }

        String sql = dbQueries.getBatchUpdateSecondaryAccountInstructionQuery();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            Timestamp currentTimestamp = new Timestamp((new Date()).getTime());

            for (SecondaryAccountInstructionItem item : instructionItems) {
                stmt.setString(1, String.valueOf(item.getSecondaryAccountInstructionStatus()));
                stmt.setObject(2, item.getOtherAccountsAvailability(), Types.BOOLEAN);
                stmt.setTimestamp(3, currentTimestamp);
                stmt.setString(4, item.getAccountId());
                stmt.setString(5, item.getSecondaryUserId());
                stmt.addBatch();
            }

            int[] results = stmt.executeBatch();
            if (log.isDebugEnabled()) {
                log.debug("Batch updated secondary account instructions for " + results.length + " records.");
            }

        } catch (SQLException e) {
            log.error("Error batch updating secondary account instructions", e);
            throw new AccountMetadataException("Failed to batch update secondary account instructions", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
        public List<BusinessStakeholderPermissionItem> getBatchBusinessStakeholderPermissions(Connection conn,
            List<Pair<String, String>> accountUserPairs) throws AccountMetadataException {

        if (accountUserPairs == null || accountUserPairs.isEmpty()) {
            throw new AccountMetadataException("Account-user pair list cannot be null or empty " +
                    "when retrieving business stakeholder permissions");
        }

        String sql = dbQueries.getBatchGetBusinessStakeholderPermissionQuery(accountUserPairs);
        List<BusinessStakeholderPermissionItem> resultItems = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int parameterIndex = 1;
            for (Pair<String, String> pair : accountUserPairs) {
                stmt.setString(parameterIndex++, pair.getLeft());
                stmt.setString(parameterIndex++, pair.getRight());
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    BusinessStakeholderPermissionItem permissionItem = new BusinessStakeholderPermissionItem();
                    permissionItem.setAccountId(rs.getString(BNR_PERMISSIONS_COLUMN_ACCOUNT_ID));
                    permissionItem.setUserId(rs.getString(BNR_PERMISSIONS_COLUMN_USER_ID));
                    permissionItem.setPermission(BusinessStakeholderPermissionItem.PermissionEnum.fromValue(
                            rs.getString(BNR_PERMISSIONS_COLUMN_PERMISSION)));
                    resultItems.add(permissionItem);
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("Retrieved business stakeholder permissions for " + resultItems.size() + " records.");
            }
            return resultItems;

        } catch (SQLException e) {
            log.error("Error retrieving batch business stakeholder permissions", e);
            throw new AccountMetadataException("Failed to retrieve batch business stakeholder permissions", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BusinessStakeholderPermissionItem> getBatchBusinessStakeholderPermissionsByAccountIds(Connection conn,
            List<String> accountIds) throws AccountMetadataException {

        if (accountIds == null || accountIds.isEmpty()) {
            return Collections.emptyList();
        }

        String sql = dbQueries.getBatchGetBusinessStakeholderPermissionByAccountQuery(accountIds.size());
        List<BusinessStakeholderPermissionItem> resultItems = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < accountIds.size(); i++) {
                stmt.setString(i + 1, accountIds.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    BusinessStakeholderPermissionItem permissionItem = new BusinessStakeholderPermissionItem();
                    permissionItem.setAccountId(rs.getString(BNR_PERMISSIONS_COLUMN_ACCOUNT_ID));
                    permissionItem.setUserId(rs.getString(BNR_PERMISSIONS_COLUMN_USER_ID));
                    permissionItem.setPermission(BusinessStakeholderPermissionItem.PermissionEnum.fromValue(
                            rs.getString(BNR_PERMISSIONS_COLUMN_PERMISSION)));
                    resultItems.add(permissionItem);
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("Retrieved business stakeholder permissions for " + resultItems.size() + " records by " +
                        "account IDs.");
            }
            return resultItems;

        } catch (SQLException e) {
            log.error("Error retrieving batch business stakeholder permissions by account IDs", e);
            throw new AccountMetadataException("Failed to retrieve batch business stakeholder permissions by " +
                    "account IDs", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addBatchBusinessStakeholderPermissions(Connection conn,
            List<BusinessStakeholderPermissionItem> permissionItems) throws AccountMetadataException {

        if (permissionItems == null || permissionItems.isEmpty()) {
            throw new AccountMetadataException("Permission items cannot be null or empty " +
                        "when adding business stakeholder permissions");
        }

        String sql = dbQueries.getBatchAddBusinessStakeholderPermissionQuery();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            Timestamp currentTimestamp = new Timestamp((new Date()).getTime());

            for (BusinessStakeholderPermissionItem item : permissionItems) {
                stmt.setString(1, item.getAccountId());
                stmt.setString(2, item.getUserId());
                stmt.setString(3, item.getPermission().value());
                stmt.setTimestamp(4, currentTimestamp);
                stmt.addBatch();
            }

            int[] results = stmt.executeBatch();
            if (log.isDebugEnabled()) {
                log.debug("Batch added business stakeholder permissions for " + results.length + " records.");
            }

        } catch (SQLException e) {
            log.error("Error batch adding business stakeholder permissions", e);
            throw new AccountMetadataException("Failed to batch add business stakeholder permissions", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateBatchBusinessStakeholderPermissions(Connection conn,
            List<BusinessStakeholderPermissionItem> permissionItems) throws AccountMetadataException {

        if (permissionItems == null || permissionItems.isEmpty()) {
            return;
        }

        String sql = dbQueries.getBatchUpdateBusinessStakeholderPermissionQuery();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            Timestamp currentTimestamp = new Timestamp((new Date()).getTime());

            for (BusinessStakeholderPermissionItem item : permissionItems) {
                stmt.setString(1, item.getPermission().value());
                stmt.setTimestamp(2, currentTimestamp);
                stmt.setString(3, item.getAccountId());
                stmt.setString(4, item.getUserId());
                stmt.addBatch();
            }

            int[] results = stmt.executeBatch();
            if (log.isDebugEnabled()) {
                log.debug("Batch updated business stakeholder permissions for " + results.length + " records.");
            }

        } catch (SQLException e) {
            log.error("Error batch updating business stakeholder permissions", e);
            throw new AccountMetadataException("Failed to batch update business stakeholder permissions", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteBatchBusinessStakeholderPermissions(Connection conn,
            List<BusinessStakeholderPermissionItem> permissionItems) throws AccountMetadataException {

        if (permissionItems == null || permissionItems.isEmpty()) {
            return;
        }

        String sql = dbQueries.getBatchDeleteBusinessStakeholderPermissionQuery();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (BusinessStakeholderPermissionItem item : permissionItems) {
                stmt.setString(1, item.getAccountId());
                stmt.setString(2, item.getUserId());
                stmt.addBatch();
            }

            int[] results = stmt.executeBatch();
            if (log.isDebugEnabled()) {
                log.debug("Batch deleted business stakeholder permissions for " + results.length + " records.");
            }

        } catch (SQLException e) {
            log.error("Error batch deleting business stakeholder permissions", e);
            throw new AccountMetadataException("Failed to batch delete business stakeholder permissions", e);
        }
    }
}

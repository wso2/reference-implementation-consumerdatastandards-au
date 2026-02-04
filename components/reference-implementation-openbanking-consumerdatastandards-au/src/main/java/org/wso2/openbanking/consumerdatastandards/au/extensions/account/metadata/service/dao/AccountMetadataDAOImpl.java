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
package org.wso2.openbanking.consumerdatastandards.au.extensions.account.metadata.service.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.openbanking.consumerdatastandards.au.extensions.account.metadata.service.dao.queries.AccountMetadataDBQueries;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.ErrorConstants;
import org.wso2.openbanking.consumerdatastandards.au.extensions.exceptions.AccountMetadataException;
import org.wso2.openbanking.consumerdatastandards.au.extensions.utils.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class  AccountMetadataDAOImpl implements AccountMetadataDAO {

    private static final Logger log = LoggerFactory.getLogger(AccountMetadataDAOImpl.class);

    private final AccountMetadataDBQueries queries;
    private static final Set<String> ALLOWED_COLUMNS = Set.of(
            "DISCLOSURE_OPTIONS_STATUS",
            "SECONDARY_ACCOUNT_INSTRUCTION_STATUS",
            "OTHER_ACCOUNTS_AVAILABILITY",
            "BLOCK_LEGAL_ENTITIES",
            "BNR_PERMISSION"
    );

    private void validateColumn(String column) throws AccountMetadataException {
        if (!ALLOWED_COLUMNS.contains(column)) {
            throw new AccountMetadataException("Invalid metadata column: " + column);
        }
    }

    public AccountMetadataDAOImpl(AccountMetadataDBQueries sqlStatements) {
        this.queries = sqlStatements;
    }

    @Override
    public int storeOrUpdateMetadata(Connection conn, String accountId, String userId, Map<String, String> metadata)
            throws AccountMetadataException {

        if (metadata == null || metadata.isEmpty()) {
            return 0;
        }

        if (conn == null) {
            log.error(ErrorConstants.DB_CONNECTION_NULL_ERROR);
            throw new AccountMetadataException(ErrorConstants.DB_CONNECTION_NULL_ERROR);
        }

        String sql = queries.getInsertOrUpdateQuery(metadata.keySet());

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            int index = 1;
            ps.setString(index++, accountId);
            ps.setString(index++, userId);
            ps.setTimestamp(index++, new Timestamp(new Date().getTime()));

            for (String value : metadata.values()) {
                ps.setString(index++, value);
            }

            int rows = ps.executeUpdate();
            conn.commit();

            return rows;

        } catch (SQLException e) {
            log.error("Error updating account metadata for accountId={}, userId={}", accountId, userId, e);
            throw new AccountMetadataException("Database error while updating account metadata", e);
        } finally {
            DatabaseUtil.closeConnection(conn);
        }
    }

    @Override
    public Map<String, String> getAccountMetadata(Connection conn,
                                                  String accountId,
                                                  String userId)
            throws AccountMetadataException {

        String sql = queries.getSelectByAccountAndUserQuery(ALLOWED_COLUMNS);

        if (conn == null) {
            log.error(ErrorConstants.DB_CONNECTION_NULL_ERROR);
            throw new AccountMetadataException(ErrorConstants.DB_CONNECTION_NULL_ERROR);
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountId);
            ps.setString(2, userId);

            ResultSet rs = ps.executeQuery();
            Map<String, String> result = new HashMap<>();

            if (rs.next()) {
                for (String column : ALLOWED_COLUMNS) {
                    result.put(column, rs.getString(column));
                }
            }
            return result;

        } catch (SQLException e) {
            log.error("Error retrieving account metadata for accountId={}, userId={}", accountId, userId, e);
            throw new AccountMetadataException("Error reading account metadata", e);
        } finally {
            DatabaseUtil.closeConnection(conn);
        }
    }

    @Override
    public Map<String, String> getAccountMetadataForAccount(Connection conn,
                                                            String accountId)
            throws AccountMetadataException {

        String sql = queries.getSelectByAccountQuery(ALLOWED_COLUMNS);

        if (conn == null) {
            log.error(ErrorConstants.DB_CONNECTION_NULL_ERROR);
            throw new AccountMetadataException(ErrorConstants.DB_CONNECTION_NULL_ERROR);
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountId);
            ResultSet rs = ps.executeQuery();

            Map<String, String> result = new HashMap<>();

            while (rs.next()) {
                for (String column : ALLOWED_COLUMNS) {
                    result.put(rs.getString("USER_ID") + ":" + column,
                            rs.getString(column));
                }
            }
            return result;

        } catch (SQLException e) {
            log.error("Error retrieving account metadata for accountId={}", accountId, e);
            throw new AccountMetadataException("Error reading account metadata", e);
        } finally {
            DatabaseUtil.closeConnection(conn);
        }
    }

    @Override
    public Map<String, String> getUserMetadataForAccountAndKey(
            Connection conn, String accountId, String column)
            throws AccountMetadataException {

        validateColumn(column);

        String sql = queries.getSelectUsersForAccountAndColumnQuery(column);

        if (conn == null) {
            log.error(ErrorConstants.DB_CONNECTION_NULL_ERROR);
            throw new AccountMetadataException(ErrorConstants.DB_CONNECTION_NULL_ERROR);
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountId);
            ResultSet rs = ps.executeQuery();

            Map<String, String> result = new HashMap<>();

            while (rs.next()) {
                result.put(rs.getString("USER_ID"),
                        rs.getString(column));
            }
            return result;

        } catch (SQLException e) {
            log.error("Error retrieving account metadata for accountId={} ", accountId, e);
            throw new AccountMetadataException("Error fetching user metadata", e);
        } finally {
            DatabaseUtil.closeConnection(conn);
        }
    }

    @Override
    public String getAccountMetadataByKey(Connection conn,
                                          String accountId,
                                          String userId,
                                          String column)
            throws AccountMetadataException {

        validateColumn(column);

        if (conn == null) {
            log.error(ErrorConstants.DB_CONNECTION_NULL_ERROR);
            throw new AccountMetadataException(ErrorConstants.DB_CONNECTION_NULL_ERROR);
        }

        String sql = queries.getSelectSingleColumnQuery(column);

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountId);
            ps.setString(2, userId);

            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString(column) : null;

        } catch (SQLException e) {
            log.error("Error retrieving account metadata for accountId={}, userId={}", accountId, userId, e);
            throw new AccountMetadataException("Error reading metadata value", e);
        } finally {
            DatabaseUtil.closeConnection(conn);
        }
    }

}

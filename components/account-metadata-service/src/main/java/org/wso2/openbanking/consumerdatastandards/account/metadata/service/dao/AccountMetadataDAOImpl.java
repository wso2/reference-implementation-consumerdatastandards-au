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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.consumerdatastandards.account.metadata.exceptions.AccountMetadataException;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.dao.queries.AccountMetadataDbQueries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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
                    resultMap.put(rs.getString("ACCOUNT_ID"), rs.getString("DISCLOSURE_OPTION_STATUS"));
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

}

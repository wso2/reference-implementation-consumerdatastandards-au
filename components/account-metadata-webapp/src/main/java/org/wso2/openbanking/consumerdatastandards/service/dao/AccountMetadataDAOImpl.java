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
package org.wso2.openbanking.consumerdatastandards.service.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.openbanking.consumerdatastandards.exceptions.AccountMetadataException;
import org.wso2.openbanking.consumerdatastandards.service.dao.queries.AccountMetadataDBQueries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class  AccountMetadataDAOImpl implements AccountMetadataDAO {

    private static final Logger log = LoggerFactory.getLogger(AccountMetadataDAOImpl.class);

    private final AccountMetadataDBQueries dbQueries;

    public AccountMetadataDAOImpl(AccountMetadataDBQueries sqlStatements) {
        this.dbQueries = sqlStatements;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addDisclosureOption(Connection conn, String accountId, String disclosureOptionStatus)
            throws AccountMetadataException {

        String sql = dbQueries.getAddDisclosureOptionQuery();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, accountId);
            stmt.setString(2, disclosureOptionStatus);

            int rowsAffected = stmt.executeUpdate();
            log.debug("Added disclosure option for accountId: {}, status: {}, rows affected: {}",
                    accountId, disclosureOptionStatus, rowsAffected);

        } catch (SQLException e) {
            log.error("Error adding disclosure option for accountId: {}", accountId, e);
            throw new AccountMetadataException("Failed to add disclosure option", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateDisclosureOption(Connection conn, String accountId, String disclosureOptionStatus)
            throws AccountMetadataException {

        String sql = dbQueries.getUpdateDisclosureOptionQuery();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, disclosureOptionStatus);
            stmt.setString(2, accountId);

            int rowsAffected = stmt.executeUpdate();

            log.debug("Updated disclosure option for accountId: {}, status: {}, rows affected: {}",
                    accountId, disclosureOptionStatus, rowsAffected);

        } catch (SQLException e) {
            log.error("Error updating disclosure option for accountId: {}", accountId, e);
            throw new AccountMetadataException("Failed to update disclosure option", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisclosureOption(Connection conn, String accountId)
            throws AccountMetadataException {

        String sql = dbQueries.getGetDisclosureOptionQuery();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, accountId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("DISCLOSURE_OPTION_STATUS");
                    log.debug("Retrieved disclosure option for accountId: {}, status: {}", accountId, status);
                    return status;
                }
            }
            log.debug("No disclosure option found for accountId: {}", accountId);
            return null;

        } catch (SQLException e) {
            log.error("Error retrieving disclosure option for accountId: {}", accountId, e);
            throw new AccountMetadataException("Failed to retrieve disclosure option", e);
        }
    }

}

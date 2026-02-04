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

package org.wso2.openbanking.consumerdatastandards.au.extensions.account.metadata.service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.openbanking.consumerdatastandards.au.extensions.account.metadata.service.dao.AccountMetadataDAO;
import org.wso2.openbanking.consumerdatastandards.au.extensions.account.metadata.service.dao.AccountMetadataDAOImpl;
import org.wso2.openbanking.consumerdatastandards.au.extensions.account.metadata.service.dao.queries.AccountMetadataDBQueriesMySQLImpl;
import org.wso2.openbanking.consumerdatastandards.au.extensions.exceptions.AccountMetadataException;
import org.wso2.openbanking.consumerdatastandards.au.extensions.utils.DatabaseUtil;

import java.sql.Connection;
import java.util.Map;

public class AccountMetadataServiceImpl {

    private static AccountMetadataServiceImpl instance = null;

    private final AccountMetadataDAO metadataDAO = new AccountMetadataDAOImpl(
            new AccountMetadataDBQueriesMySQLImpl());
    private static final Logger log = LoggerFactory.getLogger(AccountMetadataServiceImpl.class);


    // private constructor
    private AccountMetadataServiceImpl() {
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

    public int addOrUpdateAccountMetadata(String accountId,
                                          String userId,
                                          Map<String, String> metadata)
            throws AccountMetadataException {

        if (accountId == null || accountId.isBlank() || userId == null || userId.isBlank()) {
            throw new AccountMetadataException("Account Id or User Id is not provided.");
        }

        if (metadata == null || metadata.isEmpty()) {
            return 0;
        }

        try (Connection conn = DatabaseUtil.getConnection()) {

            int rows = metadataDAO.storeOrUpdateMetadata(
                    conn, accountId, userId, metadata
            );

            return rows;

        } catch (Exception e) {
            log.error("Error while updating account metadata for accountId: {}, userId: {}",
                    accountId, userId, e);

            throw new AccountMetadataException(
                    "Failed to update account metadata for accountId: " + accountId, e);
        }
    }


    /**
     * Update metadata where USER_ID is GLOBAL.
     */
    public int addOrUpdateAccountMetadata(String accountId,
                                          Map<String, String> accountMetadataMap)
            throws AccountMetadataException {
        return addOrUpdateAccountMetadata(accountId, "GLOBAL", accountMetadataMap);
    }

    /**
     * Update a single column for account + user.
     */
    public int addOrUpdateAccountMetadata(String accountId,
                                          String userId,
                                          String column,
                                          String value)
            throws AccountMetadataException {
        return addOrUpdateAccountMetadata(accountId, userId, Map.of(column, value));
    }

    /**
     * Update a single column where USER_ID is GLOBAL.
     */
    public int addOrUpdateAccountMetadata(String accountId,
                                          String column,
                                          String value)
            throws AccountMetadataException {
        return addOrUpdateAccountMetadata(accountId, "GLOBAL", Map.of(column, value));
    }

    public Map<String, String> getAccountMetadataMap(String accountId,
                                                     String userId)
            throws AccountMetadataException {

        if (accountId == null || accountId.isBlank()
                || userId == null || userId.isBlank()) {
            throw new AccountMetadataException("AccountId or UserId is invalid");
        }

        try (Connection conn = DatabaseUtil.getConnection()) {
            return metadataDAO.getAccountMetadata(conn, accountId, userId);
        } catch (Exception e) {
            log.error("Error reading metadata for accountId={}, userId={}",
                    accountId, userId, e);
            throw new AccountMetadataException(
                    "Failed to retrieve account metadata", e);
        }
    }

    public Map<String, String> getAccountMetadataMap(String accountId)
            throws AccountMetadataException {

        if (accountId == null || accountId.isBlank()) {
            throw new AccountMetadataException("AccountId is invalid");
        }

        try (Connection conn = DatabaseUtil.getConnection()) {
            return metadataDAO.getAccountMetadataForAccount(conn, accountId);
        } catch (Exception e) {
            log.error("Error reading metadata for accountId={}", accountId, e);
            throw new AccountMetadataException(
                    "Failed to retrieve account metadata", e);
        }
    }

    public Map<String, String> getUserMetadataForAccountIdAndKey(String accountId,
                                                                 String key)
            throws AccountMetadataException {

        if (accountId == null || accountId.isBlank()
                || key == null || key.isBlank()) {
            throw new AccountMetadataException("AccountId or key is invalid");
        }

        try (Connection conn = DatabaseUtil.getConnection()) {
            return metadataDAO.getUserMetadataForAccountAndKey(conn, accountId, key);
        } catch (Exception e) {
            log.error("Error reading user metadata for accountId={}, key={}",
                    accountId, key, e);
            throw new AccountMetadataException(
                    "Failed to retrieve user metadata", e);
        }
    }

    public String getAccountMetadataByKey(String accountId,
                                          String userId,
                                          String key)
            throws AccountMetadataException {

        if (accountId == null || accountId.isBlank()
                || userId == null || userId.isBlank()
                || key == null || key.isBlank()) {
            throw new AccountMetadataException("Invalid parameters");
        }

        try (Connection conn = DatabaseUtil.getConnection()) {
            return metadataDAO.getAccountMetadataByKey(
                    conn, accountId, userId, key);
        } catch (Exception e) {
            log.error("Error reading metadata value for accountId={}, userId={}, key={}",
                    accountId, userId, key, e);
            throw new AccountMetadataException(
                    "Failed to retrieve metadata value", e);
        }
    }

    public String getAccountMetadataByKey(String accountId,
                                          String key)
            throws AccountMetadataException {
        return getAccountMetadataByKey(accountId, "GLOBAL", key);
    }
}

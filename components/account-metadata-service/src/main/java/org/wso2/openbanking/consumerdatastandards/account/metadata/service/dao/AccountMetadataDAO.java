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

import org.wso2.openbanking.consumerdatastandards.account.metadata.exceptions.AccountMetadataException;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * Data access object for account metadata operations.
 */
public interface AccountMetadataDAO {

    /**
     * Batch retrieve disclosure options for multiple accounts.
     *
     * @param conn the database connection
     * @param accountIds the list of account IDs
     * @return map of account ID to disclosure option status
     * @throws AccountMetadataException if an error occurs
     */
    Map<String, String> getBatchDisclosureOptions(Connection conn, List<String> accountIds)
            throws AccountMetadataException;

    /**
     * Batch add disclosure options for multiple accounts.
     *
     * @param conn the database connection
     * @param accountDisclosureMap map of account ID to disclosure option status
     * @throws AccountMetadataException if an error occurs
     */
    void addBatchDisclosureOptions(Connection conn, Map<String, String> accountDisclosureMap)
            throws AccountMetadataException;

    /**
     * Batch update disclosure options for multiple accounts.
     *
     * @param conn the database connection
     * @param accountDisclosureMap map of account ID to disclosure option status
     * @throws AccountMetadataException if an error occurs
     */
    void updateBatchDisclosureOptions(Connection conn, Map<String, String> accountDisclosureMap)
            throws AccountMetadataException;

}

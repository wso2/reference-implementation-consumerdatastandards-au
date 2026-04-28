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
import org.wso2.openbanking.consumerdatastandards.account.metadata.exceptions.AccountMetadataException;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.BusinessStakeholderPermissionItem;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.LegalEntitySharingItem;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.SecondaryAccountInstructionItem;

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

    /**
     * Batch retrieve secondary account instructions for multiple account-user pairs.
     *
     * @param conn the database connection
     * @param accountUserPairs list of account-user pairs
     * @return list of secondary account instruction records
     * @throws AccountMetadataException if an error occurs
     */
    List<SecondaryAccountInstructionItem> getBatchSecondaryAccountInstructions(Connection conn,
            List<Pair<String, String>> accountUserPairs) throws AccountMetadataException;

    /**
     * Batch add secondary account instructions.
     *
     * @param conn the database connection
     * @param instructionItems list of secondary account instruction records to add
     * @throws AccountMetadataException if an error occurs
     */
    void addBatchSecondaryAccountInstructions(Connection conn, List<SecondaryAccountInstructionItem> instructionItems)
            throws AccountMetadataException;

    /**
     * Batch update secondary account instructions.
     *
     * @param conn the database connection
     * @param instructionItems list of secondary account instruction records to update
     * @throws AccountMetadataException if an error occurs
     */
    void updateBatchSecondaryAccountInstructions(Connection conn,
                                                 List<SecondaryAccountInstructionItem> instructionItems)
            throws AccountMetadataException;

    /**
     * Retrieve all legal entity sharing status rows for multiple account-user pairs from
     * fs_account_secondary_user_legal_entity.
     *
     * @param conn             the database connection
     * @param accountUserPairs list of (accountId, userId) pairs to query
     * @return list of legal entity sharing status records
     * @throws AccountMetadataException if an error occurs
     */
    List<LegalEntitySharingItem> getBatchLegalEntitySharingStatuses(Connection conn,
            List<Pair<String, String>> accountUserPairs) throws AccountMetadataException;

    /**
     * Upsert legal entity sharing status rows. Inserts a new row or updates
     * LEGAL_ENTITY_STATUS and LAST_UPDATED_TIMESTAMP when the primary key already exists.
     *
     * @param conn  the database connection
     * @param items legal entity sharing items to upsert
     * @throws AccountMetadataException if an error occurs
     */
    void upsertBatchLegalEntitySharingStatuses(Connection conn, List<LegalEntitySharingItem> items)
            throws AccountMetadataException;

    /**
     * Batch retrieve business stakeholder permissions for multiple account-user pairs.
     *
     * @param conn the database connection
     * @param accountUserPairs list of (accountId, userId) pairs to query
     * @return list of existing business stakeholder permission records
     * @throws AccountMetadataException if an error occurs
     */
    List<BusinessStakeholderPermissionItem> getBatchBusinessStakeholderPermissions(Connection conn,
            List<Pair<String, String>> accountUserPairs) throws AccountMetadataException;

    /**
     * Batch retrieve business stakeholder permissions for multiple account IDs.
     *
     * @param conn the database connection
     * @param accountIds list of account IDs
     * @return list of existing business stakeholder permission records
     * @throws AccountMetadataException if an error occurs
     */
    List<BusinessStakeholderPermissionItem> getBatchBusinessStakeholderPermissionsByAccountIds(Connection conn,
            List<String> accountIds) throws AccountMetadataException;

    /**
     * Batch add business stakeholder permission records.
     *
     * @param conn the database connection
     * @param permissionItems list of account-user permission records to add
     * @throws AccountMetadataException if an error occurs
     */
    void addBatchBusinessStakeholderPermissions(Connection conn,
            List<BusinessStakeholderPermissionItem> permissionItems)
            throws AccountMetadataException;

    /**
     * Batch update business stakeholder permission records.
     *
     * @param conn the database connection
     * @param permissionItems list of account-user permission records to update
     * @throws AccountMetadataException if an error occurs
     */
    void updateBatchBusinessStakeholderPermissions(Connection conn,
            List<BusinessStakeholderPermissionItem> permissionItems) throws AccountMetadataException;

    /**
     * Batch delete business stakeholder permission records.
     *
     * @param conn the database connection
     * @param permissionItems list of account-user permission records to delete
     * @throws AccountMetadataException if an error occurs
     */
    void deleteBatchBusinessStakeholderPermissions(Connection conn,
            List<BusinessStakeholderPermissionItem> permissionItems) throws AccountMetadataException;
}

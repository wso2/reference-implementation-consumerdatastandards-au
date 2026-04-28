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
import org.wso2.openbanking.consumerdatastandards.account.metadata.exceptions.AccountMetadataException;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.BusinessStakeholderPermissionItem;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.LegalEntitySharingItem;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.SecondaryAccountInstructionItem;

import java.util.List;
import java.util.Map;

/**
 * Service for account metadata operations.
 */
public interface AccountMetadataService {

    /**
     * Batch retrieve disclosure options for multiple accounts.
     *
     * @param accountIds the list of account IDs
     * @return map of account ID to disclosure option status
     * @throws AccountMetadataException if an error occurs
     */
    Map<String, String> getBatchDisclosureOptions(List<String> accountIds) throws AccountMetadataException;

    /**
     * Batch add disclosure options for multiple accounts.
     *
     * @param accountDisclosureMap map of account ID to disclosure option status
     * @throws AccountMetadataException if an error occurs
     */
    void addBatchDisclosureOptions(Map<String, String> accountDisclosureMap) throws AccountMetadataException;

    /**
     * Batch update disclosure options for multiple accounts.
     *
     * @param accountDisclosureMap map of account ID to disclosure option status
     * @throws AccountMetadataException if an error occurs
     */
    void updateBatchDisclosureOptions(Map<String, String> accountDisclosureMap) throws AccountMetadataException;

    /**
     * Batch retrieve secondary account instructions for multiple account-user pairs.
     *
     * @param accountUserPairs list of account-user pairs
     * @return list of secondary account instruction records
     * @throws AccountMetadataException if an error occurs
     */
    List<SecondaryAccountInstructionItem> getBatchSecondaryAccountInstructions
    (List<Pair<String, String>> accountUserPairs)
        throws AccountMetadataException;

    /**
     * Batch add secondary account instructions.
     *
     * @param instructionItems list of secondary account instruction records to add
     * @throws AccountMetadataException if an error occurs
     */
    void addBatchSecondaryAccountInstructions(List<SecondaryAccountInstructionItem> instructionItems)
        throws AccountMetadataException;

    /**
     * Batch update secondary account instructions.
     *
     * @param instructionItems list of secondary account instruction records to update
     * @throws AccountMetadataException if an error occurs
     */
    void updateBatchSecondaryAccountInstructions(List<SecondaryAccountInstructionItem> instructionItems)
        throws AccountMetadataException;

    /**
     * Retrieve all legal entity sharing status records for multiple account-user pairs from
     * fs_account_secondary_user_legal_entity.
     *
     * @param accountUserPairs list of (accountId, userId) pairs to query
     * @return list of legal entity sharing status records
     * @throws AccountMetadataException if an error occurs
     */
    List<LegalEntitySharingItem> getBatchLegalEntitySharingStatuses(
            List<Pair<String, String>> accountUserPairs) throws AccountMetadataException;

    /**
     * Upsert legal entity sharing status records. Inserts new rows or updates
     * LEGAL_ENTITY_STATUS when the primary key already exists.
     *
     * @param items legal entity sharing items to upsert
     * @throws AccountMetadataException if an error occurs
     */
    void upsertBatchLegalEntitySharingStatuses(List<LegalEntitySharingItem> items) throws AccountMetadataException;

    /**
     * Batch retrieve business stakeholder permissions for multiple account-user pairs.
     *
     * @param accountUserPairs list of (accountId, userId) pairs to query
     * @return list of existing business stakeholder permission records
     * @throws AccountMetadataException if an error occurs
     */
    List<BusinessStakeholderPermissionItem> getBatchBusinessStakeholderPermissions(
        List<Pair<String, String>> accountUserPairs) throws AccountMetadataException;

    /**
     * Batch retrieve business stakeholder permissions for multiple account IDs.
     *
     * @param accountIds list of account IDs
     * @return list of existing business stakeholder permission records
     * @throws AccountMetadataException if an error occurs
     */
    List<BusinessStakeholderPermissionItem> getBatchBusinessStakeholderPermissionsByAccountIds(
        List<String> accountIds) throws AccountMetadataException;

    /**
     * Batch add business stakeholder permission records.
     *
     * @param permissionItems list of account-user permission records to add
     * @throws AccountMetadataException if an error occurs
     */
    void addBatchBusinessStakeholderPermissions(List<BusinessStakeholderPermissionItem> permissionItems)
        throws AccountMetadataException;

    /**
     * Batch update business stakeholder permission records.
     *
     * @param permissionItems list of account-user permission records to update
     * @throws AccountMetadataException if an error occurs
     */
    void updateBatchBusinessStakeholderPermissions(List<BusinessStakeholderPermissionItem> permissionItems)
        throws AccountMetadataException;

    /**
     * Batch delete business stakeholder permission records.
     *
     * @param permissionItems list of account-user permission records to delete
     * @throws AccountMetadataException if an error occurs
     */
    void deleteBatchBusinessStakeholderPermissions(List<BusinessStakeholderPermissionItem> permissionItems)
        throws AccountMetadataException;
}

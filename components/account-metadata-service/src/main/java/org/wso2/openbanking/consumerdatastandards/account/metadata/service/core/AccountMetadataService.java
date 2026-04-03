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

}

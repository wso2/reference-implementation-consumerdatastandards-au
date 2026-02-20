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

package org.wso2.openbanking.consumerdatastandards.account.metadata.service.dao.queries;

/**
 * MySQL implementation of account metadata database queries.
 */
public class AccountMetadataDBQueriesMySQLImpl implements AccountMetadataDBQueries {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBatchGetDisclosureOptionQuery(int accountCount) {
        StringBuilder query = new StringBuilder(
                "SELECT ACCOUNT_ID, DISCLOSURE_OPTION_STATUS FROM fs_account_doms_status WHERE ACCOUNT_ID IN (");
        for (int i = 0; i < accountCount; i++) {
            query.append("?");
            if (i < accountCount - 1) {
                query.append(",");
            }
        }
        query.append(")");
        return query.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBatchAddDisclosureOptionQuery() {
        return "INSERT INTO fs_account_doms_status (ACCOUNT_ID, DISCLOSURE_OPTION_STATUS, LAST_UPDATED_TIMESTAMP) " +
                "VALUES (?, ?, ?)";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBatchUpdateDisclosureOptionQuery() {
        return "UPDATE fs_account_doms_status SET DISCLOSURE_OPTION_STATUS = ?, " +
                "LAST_UPDATED_TIMESTAMP = ? WHERE ACCOUNT_ID = ?";
    }


}

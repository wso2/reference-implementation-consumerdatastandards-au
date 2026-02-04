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

package org.wso2.openbanking.consumerdatastandards.au.extensions.account.metadata.service.dao.queries;

import java.util.Set;
import java.util.StringJoiner;

public class AccountMetadataDBQueriesMySQLImpl implements AccountMetadataDBQueries {

    @Override
    public String getInsertOrUpdateQuery(Set<String> columns) {

        StringJoiner columnNames = new StringJoiner(", ");
        StringJoiner placeholders = new StringJoiner(", ");
        StringJoiner updates = new StringJoiner(", ");

        columnNames.add("ACCOUNT_ID");
        columnNames.add("USER_ID");
        columnNames.add("LAST_UPDATED_TIMESTAMP");
        placeholders.add("?");
        placeholders.add("?");
        placeholders.add("?");

        updates.add("LAST_UPDATED_TIMESTAMP = VALUES(LAST_UPDATED_TIMESTAMP)");

        for (String column : columns) {
            columnNames.add(column);
            placeholders.add("?");
            updates.add(column + " = VALUES(" + column + ")");
        }

        return "INSERT INTO fs_account_metadata (" + columnNames + ") VALUES (" + placeholders + ") " +
                "ON DUPLICATE KEY UPDATE " + updates;
    }

    @Override
    public String getSelectByAccountAndUserQuery(Set<String> columns) {

        return "SELECT " + String.join(", ", columns) +
                " FROM fs_account_metadata WHERE ACCOUNT_ID = ? AND USER_ID = ?";
    }

    @Override
    public String getSelectByAccountQuery(Set<String> columns) {

        return "SELECT USER_ID, " + String.join(", ", columns) +
                " FROM fs_account_metadata WHERE ACCOUNT_ID = ?";
    }

    @Override
    public String getSelectUsersForAccountAndColumnQuery(String column) {

        return "SELECT USER_ID, " + column +
                " FROM fs_account_metadata WHERE ACCOUNT_ID = ?";
    }

    @Override
    public String getSelectSingleColumnQuery(String column) {

        return "SELECT " + column +
                " FROM fs_account_metadata WHERE ACCOUNT_ID = ? AND USER_ID = ?";
    }
}

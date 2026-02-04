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

import org.wso2.openbanking.consumerdatastandards.au.extensions.exceptions.AccountMetadataException;

import java.sql.Connection;
import java.util.Map;

public interface AccountMetadataDAO {

    int storeOrUpdateMetadata(Connection conn, String accountId, String userId, Map<String, String> metadata)
            throws AccountMetadataException;

    Map<String, String> getAccountMetadata(Connection conn,
                                           String accountId,
                                           String userId)
            throws AccountMetadataException;

    Map<String, String> getAccountMetadataForAccount(Connection conn,
                                                     String accountId)
            throws AccountMetadataException;

    Map<String, String> getUserMetadataForAccountAndKey(Connection conn,
                                                        String accountId,
                                                        String column)
            throws AccountMetadataException;

    String getAccountMetadataByKey(Connection conn,
                                   String accountId,
                                   String userId,
                                   String column)
            throws AccountMetadataException;


}

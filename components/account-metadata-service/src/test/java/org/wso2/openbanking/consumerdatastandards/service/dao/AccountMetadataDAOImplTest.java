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

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.exceptions.AccountMetadataException;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.service.dao.AccountMetadataDAO;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.service.dao.AccountMetadataDAOImpl;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.service.dao.queries.AccountMetadataDBQueries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountMetadataDAOImplTest {

    private static class TestQueries implements AccountMetadataDBQueries {

        @Override
        public String getBatchGetDisclosureOptionQuery(int size) {
            StringBuilder placeholders = new StringBuilder();
            for (int i = 0; i < size; i++) {
                if (i > 0) {
                    placeholders.append(",");
                }
                placeholders.append("?");
            }
            return "SELECT ACCOUNT_ID, DISCLOSURE_OPTION_STATUS FROM fs_account_doms_status WHERE ACCOUNT_ID IN ("
                    + placeholders.toString() + ")";
        }

        @Override
        public String getBatchAddDisclosureOptionQuery() {
            return "INSERT INTO fs_account_doms_status (ACCOUNT_ID, DISCLOSURE_OPTION_STATUS, LAST_UPDATED_TIMESTAMP)" +
                    " VALUES (?, ?, ?)";
        }

        @Override
        public String getBatchUpdateDisclosureOptionQuery() {
            return "UPDATE fs_account_doms_status SET DISCLOSURE_OPTION_STATUS = ?, LAST_UPDATED_TIMESTAMP = ?" +
                    " WHERE ACCOUNT_ID = ?";
        }

        @Override
        public String getBlockedAccountsQuery(int size) {
            StringBuilder placeholders = new StringBuilder();
            for (int i = 0; i < size; i++) {
                if (i > 0) {
                    placeholders.append(",");
                }
                placeholders.append("?");
            }
            return "SELECT ACCOUNT_ID FROM fs_account_doms_status WHERE ACCOUNT_ID IN ("
                    + placeholders.toString() + ") AND DISCLOSURE_OPTION_STATUS = 'no-sharing'";
        }
    }

    @Test
    public void testGetBatchDisclosureOptionsSuccess() throws Exception {
        AccountMetadataDAO dao = new AccountMetadataDAOImpl(new TestQueries());
        Connection connection = Mockito.mock(Connection.class);
        PreparedStatement statement = Mockito.mock(PreparedStatement.class);
        ResultSet resultSet = Mockito.mock(ResultSet.class);

        Mockito.when(connection.prepareStatement(Mockito.anyString())).thenReturn(statement);
        Mockito.when(statement.executeQuery()).thenReturn(resultSet);
        Mockito.when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        Mockito.when(resultSet.getString("ACCOUNT_ID"))
                .thenReturn("acc-400").thenReturn("acc-401");
        Mockito.when(resultSet.getString("DISCLOSURE_OPTION_STATUS"))
                .thenReturn("no-sharing").thenReturn("pre-approval");

        Map<String, String> result = dao.getBatchDisclosureOptions(connection, 
                Arrays.asList("acc-400", "acc-401"));

        Assert.assertEquals(result.size(), 2);
        Assert.assertEquals(result.get("acc-400"), "no-sharing");
        Assert.assertEquals(result.get("acc-401"), "pre-approval");
    }

    @Test
    public void testGetBatchDisclosureOptionsEmpty() throws Exception {
        AccountMetadataDAO dao = new AccountMetadataDAOImpl(new TestQueries());
        Connection connection = Mockito.mock(Connection.class);
        PreparedStatement statement = Mockito.mock(PreparedStatement.class);
        ResultSet resultSet = Mockito.mock(ResultSet.class);

        Mockito.when(connection.prepareStatement(Mockito.anyString())).thenReturn(statement);
        Mockito.when(statement.executeQuery()).thenReturn(resultSet);
        Mockito.when(resultSet.next()).thenReturn(false);

        Map<String, String> result = dao.getBatchDisclosureOptions(connection,
            Arrays.asList("acc-500"));

        Assert.assertEquals(result.size(), 0);
    }

    @Test(expectedExceptions = AccountMetadataException.class)
    public void testGetBatchDisclosureOptionsSqlException() throws Exception {
        AccountMetadataDAO dao = new AccountMetadataDAOImpl(new TestQueries());
        Connection connection = Mockito.mock(Connection.class);

        Mockito.when(connection.prepareStatement(Mockito.anyString()))
                .thenThrow(new SQLException("bad"));

        dao.getBatchDisclosureOptions(connection, Arrays.asList("acc-501"));
    }

    @Test
    public void testAddBatchDisclosureOptionsSuccess() throws Exception {
        AccountMetadataDAO dao = new AccountMetadataDAOImpl(new TestQueries());
        Connection connection = Mockito.mock(Connection.class);
        PreparedStatement statement = Mockito.mock(PreparedStatement.class);

        Mockito.when(connection.prepareStatement(Mockito.anyString())).thenReturn(statement);
        Mockito.when(statement.executeBatch()).thenReturn(new int[]{1, 1});

        Map<String, String> accountMap = new HashMap<>();
        accountMap.put("acc-600", "no-sharing");
        accountMap.put("acc-601", "pre-approval");

        dao.addBatchDisclosureOptions(connection, accountMap);

        Mockito.verify(statement, Mockito.times(2)).setString(
                Mockito.eq(1), Mockito.anyString());
        Mockito.verify(statement, Mockito.times(2)).setString(
                Mockito.eq(2), Mockito.anyString());
        Mockito.verify(statement, Mockito.times(2)).setTimestamp(
                Mockito.eq(3), Mockito.any(Timestamp.class));
        Mockito.verify(statement, Mockito.times(2)).addBatch();
        Mockito.verify(statement).executeBatch();
    }

    @Test
    public void testAddBatchDisclosureOptionsEmpty() throws Exception {
        AccountMetadataDAO dao = new AccountMetadataDAOImpl(new TestQueries());
        Connection connection = Mockito.mock(Connection.class);

        dao.addBatchDisclosureOptions(connection, new HashMap<>());

        Mockito.verify(connection, Mockito.never()).prepareStatement(Mockito.anyString());
    }

    @Test(expectedExceptions = AccountMetadataException.class)
    public void testAddBatchDisclosureOptionsSqlException() throws Exception {
        AccountMetadataDAO dao = new AccountMetadataDAOImpl(new TestQueries());
        Connection connection = Mockito.mock(Connection.class);

        Mockito.when(connection.prepareStatement(Mockito.anyString())).thenThrow(new SQLException("bad"));

        Map<String, String> accountMap = new HashMap<>();
        accountMap.put("acc-700", "no-sharing");

        dao.addBatchDisclosureOptions(connection, accountMap);
    }

    @Test
    public void testGetBlockedAccountsSuccess() throws Exception {
        AccountMetadataDAO dao = new AccountMetadataDAOImpl(new TestQueries());
        Connection connection = Mockito.mock(Connection.class);
        PreparedStatement statement = Mockito.mock(PreparedStatement.class);
        ResultSet resultSet = Mockito.mock(ResultSet.class);

        Mockito.when(connection.prepareStatement(Mockito.anyString())).thenReturn(statement);
        Mockito.when(statement.executeQuery()).thenReturn(resultSet);
        Mockito.when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        Mockito.when(resultSet.getString("ACCOUNT_ID"))
                .thenReturn("acc-1").thenReturn("acc-3");

        List<String> result = dao.getBlockedAccounts(connection, 
                Arrays.asList("acc-1", "acc-2", "acc-3"));

        Assert.assertEquals(result.size(), 2);
        Assert.assertTrue(result.contains("acc-1"));
        Assert.assertTrue(result.contains("acc-3"));
    }

    @Test
    public void testGetBlockedAccountsEmpty() throws Exception {
        AccountMetadataDAO dao = new AccountMetadataDAOImpl(new TestQueries());
        Connection connection = Mockito.mock(Connection.class);
        PreparedStatement statement = Mockito.mock(PreparedStatement.class);
        ResultSet resultSet = Mockito.mock(ResultSet.class);

        Mockito.when(connection.prepareStatement(Mockito.anyString())).thenReturn(statement);
        Mockito.when(statement.executeQuery()).thenReturn(resultSet);
        Mockito.when(resultSet.next()).thenReturn(false);

        List<String> result = dao.getBlockedAccounts(connection,
                Arrays.asList("acc-1", "acc-2"));

        Assert.assertEquals(result.size(), 0);
    }

    @Test(expectedExceptions = AccountMetadataException.class)
    public void testGetBlockedAccountsSqlException() throws Exception {
        AccountMetadataDAO dao = new AccountMetadataDAOImpl(new TestQueries());
        Connection connection = Mockito.mock(Connection.class);

        Mockito.when(connection.prepareStatement(Mockito.anyString()))
                .thenThrow(new SQLException("bad"));

        dao.getBlockedAccounts(connection, Arrays.asList("acc-1", "acc-2"));
    }

    @Test
    public void testUpdateBatchDisclosureOptionsSuccess() throws Exception {
        AccountMetadataDAO dao = new AccountMetadataDAOImpl(new TestQueries());
        Connection connection = Mockito.mock(Connection.class);
        PreparedStatement statement = Mockito.mock(PreparedStatement.class);

        Mockito.when(connection.prepareStatement(Mockito.anyString())).thenReturn(statement);
        Mockito.when(statement.executeBatch()).thenReturn(new int[]{1, 1});

        Map<String, String> accountMap = new HashMap<>();
        accountMap.put("acc-800", "no-sharing");
        accountMap.put("acc-801", "pre-approval");

        dao.updateBatchDisclosureOptions(connection, accountMap);

        Mockito.verify(statement, Mockito.times(2)).setString(
                Mockito.eq(1), Mockito.anyString());
        Mockito.verify(statement, Mockito.times(2)).setTimestamp(
                Mockito.eq(2), Mockito.any(Timestamp.class));
        Mockito.verify(statement, Mockito.times(2)).setString(
                Mockito.eq(3), Mockito.anyString());
        Mockito.verify(statement, Mockito.times(2)).addBatch();
        Mockito.verify(statement).executeBatch();
    }

    @Test
    public void testUpdateBatchDisclosureOptionsEmpty() throws Exception {
        AccountMetadataDAO dao = new AccountMetadataDAOImpl(new TestQueries());
        Connection connection = Mockito.mock(Connection.class);

        dao.updateBatchDisclosureOptions(connection, new HashMap<>());

        Mockito.verify(connection, Mockito.never()).prepareStatement(Mockito.anyString());
    }
}

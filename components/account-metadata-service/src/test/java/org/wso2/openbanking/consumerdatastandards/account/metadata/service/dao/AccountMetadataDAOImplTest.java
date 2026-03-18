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

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.openbanking.consumerdatastandards.account.metadata.exceptions.AccountMetadataException;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.dao.queries.AccountMetadataDbQueries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for {@link AccountMetadataDAOImpl}.
 */
public class AccountMetadataDAOImplTest {

    /**
     * Test SQL provider used to supply deterministic queries for DAO tests.
     */
    private static class TestQueries implements AccountMetadataDbQueries {

        /**
         * @return insert query for disclosure options
         */
        @Override
        public String getBatchAddDisclosureOptionQuery() {
            return "INSERT INTO fs_account_doms_status (ACCOUNT_ID, DISCLOSURE_OPTION_STATUS, LAST_UPDATED_TIMESTAMP)" +
                    " VALUES (?, ?, ?)";
        }

        /**
         * @return update query for disclosure options
         */
        @Override
        public String getBatchUpdateDisclosureOptionQuery() {
            return "UPDATE fs_account_doms_status SET DISCLOSURE_OPTION_STATUS = ?, LAST_UPDATED_TIMESTAMP = ?" +
                    " WHERE ACCOUNT_ID = ?";
        }

        /**
         * @param size number of account ids
         * @return select query for disclosure options by account id list
         */
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
    }

    /**
     * Verifies batch retrieval of disclosure options when rows are returned.
     *
     * @throws Exception if setup or invocation fails
     */
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

    /**
     * Verifies batch retrieval of disclosure options when no rows are returned.
     *
     * @throws Exception if setup or invocation fails
     */
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
                Collections.singletonList("acc-500"));

        Assert.assertEquals(result.size(), 0);
    }

    /**
     * Verifies that SQL failures during disclosure option retrieval are wrapped as service exceptions.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testGetBatchDisclosureOptionsSqlException() throws Exception {
        AccountMetadataDAO dao = new AccountMetadataDAOImpl(new TestQueries());
        Connection connection = Mockito.mock(Connection.class);

        Mockito.when(connection.prepareStatement(Mockito.anyString()))
                .thenThrow(new SQLException("bad"));

        assertAccountMetadataException(() ->
            dao.getBatchDisclosureOptions(connection, Arrays.asList("acc-501")));
    }

    /**
     * Verifies successful batch insert of disclosure options.
     *
     * @throws Exception if setup or invocation fails
     */
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

        Assert.assertTrue(Mockito.mockingDetails(statement).getInvocations().size() > 0);
        Mockito.verify(statement, Mockito.times(2)).setString(
                Mockito.eq(1), Mockito.anyString());
        Mockito.verify(statement, Mockito.times(2)).setString(
                Mockito.eq(2), Mockito.anyString());
        Mockito.verify(statement, Mockito.times(2)).setTimestamp(
                Mockito.eq(3), Mockito.any(Timestamp.class));
        Mockito.verify(statement, Mockito.times(2)).addBatch();
        Mockito.verify(statement).executeBatch();
    }

    /**
     * Verifies that no insert call is made when disclosure option input is empty.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testAddBatchDisclosureOptionsEmpty() throws Exception {
        AccountMetadataDAO dao = new AccountMetadataDAOImpl(new TestQueries());
        Connection connection = Mockito.mock(Connection.class);

        dao.addBatchDisclosureOptions(connection, new HashMap<>());

        assertNoInteractions(connection);
        Mockito.verify(connection, Mockito.never()).prepareStatement(Mockito.anyString());
    }

    /**
     * Verifies that SQL failures during disclosure option insert are wrapped as service exceptions.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testAddBatchDisclosureOptionsSqlException() throws Exception {
        AccountMetadataDAO dao = new AccountMetadataDAOImpl(new TestQueries());
        Connection connection = Mockito.mock(Connection.class);

        Mockito.when(connection.prepareStatement(Mockito.anyString())).thenThrow(new SQLException("bad"));

        Map<String, String> accountMap = new HashMap<>();
        accountMap.put("acc-700", "no-sharing");

        assertAccountMetadataException(() ->
            dao.addBatchDisclosureOptions(connection, accountMap));
    }

    /**
     * Verifies successful batch update of disclosure options.
     *
     * @throws Exception if setup or invocation fails
     */
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

        Assert.assertTrue(Mockito.mockingDetails(statement).getInvocations().size() > 0);
        Mockito.verify(statement, Mockito.times(2)).setString(
                Mockito.eq(1), Mockito.anyString());
        Mockito.verify(statement, Mockito.times(2)).setTimestamp(
                Mockito.eq(2), Mockito.any(Timestamp.class));
        Mockito.verify(statement, Mockito.times(2)).setString(
                Mockito.eq(3), Mockito.anyString());
        Mockito.verify(statement, Mockito.times(2)).addBatch();
        Mockito.verify(statement).executeBatch();
    }

    /**
     * Verifies that no update call is made when disclosure option input is empty.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testUpdateBatchDisclosureOptionsEmpty() throws Exception {
        AccountMetadataDAO dao = new AccountMetadataDAOImpl(new TestQueries());
        Connection connection = Mockito.mock(Connection.class);

        dao.updateBatchDisclosureOptions(connection, new HashMap<>());

        assertNoInteractions(connection);
        Mockito.verify(connection, Mockito.never()).prepareStatement(Mockito.anyString());
    }

    /**
     * Verifies that batch retrieval with null input returns an empty map without database operations.
     * Tests defensive null handling in the DAO early return logic.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testGetBatchDisclosureOptionsNullInput() throws Exception {
        AccountMetadataDAO dao = new AccountMetadataDAOImpl(new TestQueries());
        Connection connection = Mockito.mock(Connection.class);

        Map<String, String> result = dao.getBatchDisclosureOptions(connection, null);

        Assert.assertTrue(result.isEmpty());
        Mockito.verify(connection, Mockito.never()).prepareStatement(Mockito.anyString());
    }

    /**
     * Verifies that batch insert with null input is skipped without database operations.
     * Tests defensive null handling in the DAO early return logic.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testAddBatchDisclosureOptionsNullInput() throws Exception {
        AccountMetadataDAO dao = new AccountMetadataDAOImpl(new TestQueries());
        Connection connection = Mockito.mock(Connection.class);

        dao.addBatchDisclosureOptions(connection, null);

        assertNoInteractions(connection);
        Mockito.verify(connection, Mockito.never()).prepareStatement(Mockito.anyString());
    }

    /**
     * Verifies that batch update with null input is skipped without database operations.
     * Tests defensive null handling in the DAO early return logic.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testUpdateBatchDisclosureOptionsNullInput() throws Exception {
        AccountMetadataDAO dao = new AccountMetadataDAOImpl(new TestQueries());
        Connection connection = Mockito.mock(Connection.class);

        dao.updateBatchDisclosureOptions(connection, null);

        assertNoInteractions(connection);
        Mockito.verify(connection, Mockito.never()).prepareStatement(Mockito.anyString());
    }

    /**
     * Verifies that SQL failures during disclosure option update are wrapped as service exceptions.
     * Tests exception handling in the batch update path when database operations fail.
     *
     * @throws Exception if setup or invocation fails (expected exception is verified by TestNG)
     */
    @Test
    public void testUpdateBatchDisclosureOptionsSqlException() throws Exception {
        AccountMetadataDAO dao = new AccountMetadataDAOImpl(new TestQueries());
        Connection connection = Mockito.mock(Connection.class);

        Mockito.when(connection.prepareStatement(Mockito.anyString())).thenThrow(new SQLException("bad"));

        Map<String, String> accountMap = new HashMap<>();
        accountMap.put("acc-901", "no-sharing");

        assertAccountMetadataException(() ->
                dao.updateBatchDisclosureOptions(connection, accountMap));
    }

    private void assertAccountMetadataException(ThrowingRunnable action) throws Exception {
        try {
            action.run();
            Assert.fail("Expected AccountMetadataException to be thrown");
        } catch (AccountMetadataException ex) {
            Assert.assertNotNull(ex.getMessage());
        }
    }

    private void assertNoInteractions(Object mock) {
        Assert.assertTrue(Mockito.mockingDetails(mock).getInvocations().isEmpty());
    }

    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}

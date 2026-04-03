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
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.openbanking.consumerdatastandards.account.metadata.exceptions.AccountMetadataException;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.SecondaryAccountInstructionItem;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.dao.queries.AccountMetadataDbQueries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

        /**
         * @param pairCount number of account-user pairs
         * @return select query for secondary account instructions
         */
        @Override
        public String getBatchGetSecondaryAccountInstructionQuery(int pairCount) {
            StringBuilder placeholders = new StringBuilder();
            for (int i = 0; i < pairCount; i++) {
                if (i > 0) {
                    placeholders.append(",");
                }
                placeholders.append("(?,?)");
            }
            return "SELECT ACCOUNT_ID, USER_ID, INSTRUCTION_STATUS, OTHER_ACCOUNTS_AVAILABILITY " +
                    "FROM fs_account_secondary_user WHERE (ACCOUNT_ID, USER_ID) IN (" + placeholders + ")";
        }

        /**
         * @return insert query for secondary account instructions
         */
        @Override
        public String getBatchAddSecondaryAccountInstructionQuery() {
            return "INSERT INTO fs_account_secondary_user (ACCOUNT_ID, USER_ID, INSTRUCTION_STATUS, "
                    + "OTHER_ACCOUNTS_AVAILABILITY, LAST_UPDATED_TIMESTAMP) VALUES (?, ?, ?, ?, ?)";
        }

        /**
         * @return update query for secondary account instructions
         */
        @Override
        public String getBatchUpdateSecondaryAccountInstructionQuery() {
            return "UPDATE fs_account_secondary_user SET INSTRUCTION_STATUS = ?, " +
                    "OTHER_ACCOUNTS_AVAILABILITY = ?, LAST_UPDATED_TIMESTAMP = ? WHERE ACCOUNT_ID = ? AND USER_ID = ?";
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
    @Test(expectedExceptions = AccountMetadataException.class)
    public void testGetBatchDisclosureOptionsSqlException() throws Exception {
        AccountMetadataDAO dao = new AccountMetadataDAOImpl(new TestQueries());
        Connection connection = Mockito.mock(Connection.class);

        Mockito.when(connection.prepareStatement(Mockito.anyString()))
                .thenThrow(new SQLException("bad"));

        dao.getBatchDisclosureOptions(connection, Arrays.asList("acc-501"));
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
    @Test(expectedExceptions = AccountMetadataException.class)
    public void testAddBatchDisclosureOptionsSqlException() throws Exception {
        AccountMetadataDAO dao = new AccountMetadataDAOImpl(new TestQueries());
        Connection connection = Mockito.mock(Connection.class);

        Mockito.when(connection.prepareStatement(Mockito.anyString())).thenThrow(new SQLException("bad"));

        Map<String, String> accountMap = new HashMap<>();
        accountMap.put("acc-700", "no-sharing");

        dao.addBatchDisclosureOptions(connection, accountMap);
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

        Mockito.verify(connection, Mockito.never()).prepareStatement(Mockito.anyString());
    }

    /**
     * Verifies batch retrieval of secondary account instructions when rows are returned.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testGetBatchSecondaryAccountInstructionsSuccess() throws Exception {
        AccountMetadataDAO dao = new AccountMetadataDAOImpl(new TestQueries());
        Connection connection = Mockito.mock(Connection.class);
        PreparedStatement statement = Mockito.mock(PreparedStatement.class);
        ResultSet resultSet = Mockito.mock(ResultSet.class);

        Mockito.when(connection.prepareStatement(Mockito.anyString())).thenReturn(statement);
        Mockito.when(statement.executeQuery()).thenReturn(resultSet);
        Mockito.when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        Mockito.when(resultSet.getString("ACCOUNT_ID"))
            .thenReturn("acc-900").thenReturn("acc-901");
        Mockito.when(resultSet.getString("USER_ID"))
            .thenReturn("user-1").thenReturn("user-2");
        Mockito.when(resultSet.getString("INSTRUCTION_STATUS"))
            .thenReturn("active").thenReturn("inactive");
        Mockito.when(resultSet.getBoolean("OTHER_ACCOUNTS_AVAILABILITY"))
            .thenReturn(true).thenReturn(false);

        List<Pair<String, String>> accountUserPairs = Arrays.asList(
                Pair.of("acc-900", "user-1"),
                Pair.of("acc-901", "user-2"));

        List<SecondaryAccountInstructionItem> result = dao.getBatchSecondaryAccountInstructions(connection,
            accountUserPairs);

        Assert.assertEquals(result.size(), 2);
        Assert.assertEquals(result.get(0).getAccountId(), "acc-900");
        Assert.assertEquals(result.get(0).getSecondaryUserId(), "user-1");
        Assert.assertTrue(result.get(0).getOtherAccountsAvailability());
        Assert.assertEquals(result.get(0).getSecondaryAccountInstructionStatus(),
                SecondaryAccountInstructionItem.SecondaryAccountInstructionStatusEnum.active);
    }

    /**
     * Verifies batch retrieval of secondary account instructions when no rows are returned.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testGetBatchSecondaryAccountInstructionsEmpty() throws Exception {
        AccountMetadataDAO dao = new AccountMetadataDAOImpl(new TestQueries());
        Connection connection = Mockito.mock(Connection.class);
        PreparedStatement statement = Mockito.mock(PreparedStatement.class);
        ResultSet resultSet = Mockito.mock(ResultSet.class);

        Mockito.when(connection.prepareStatement(Mockito.anyString())).thenReturn(statement);
        Mockito.when(statement.executeQuery()).thenReturn(resultSet);
        Mockito.when(resultSet.next()).thenReturn(false);

        List<Pair<String, String>> accountUserPairs = Collections.singletonList(Pair.of("acc-902", "user-1"));

        List<SecondaryAccountInstructionItem> result = dao.getBatchSecondaryAccountInstructions(connection,
            accountUserPairs);

        Assert.assertTrue(result.isEmpty());
    }

    /**
     * Verifies that SQL failures during secondary instruction retrieval are wrapped as service exceptions.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test(expectedExceptions = AccountMetadataException.class)
    public void testGetBatchSecondaryAccountInstructionsSqlException() throws Exception {
        AccountMetadataDAO dao = new AccountMetadataDAOImpl(new TestQueries());
        Connection connection = Mockito.mock(Connection.class);

        Mockito.when(connection.prepareStatement(Mockito.anyString()))
                .thenThrow(new SQLException("bad"));

        List<Pair<String, String>> accountUserPairs = Collections.singletonList(Pair.of("acc-903", "user-1"));

        dao.getBatchSecondaryAccountInstructions(connection, accountUserPairs);
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

        Mockito.verify(connection, Mockito.never()).prepareStatement(Mockito.anyString());
    }

    /**
     * Verifies that SQL failures during disclosure option update are wrapped as service exceptions.
     * Tests exception handling in the batch update path when database operations fail.
     *
     * @throws Exception if setup or invocation fails (expected exception is verified by TestNG)
     */
    @Test(expectedExceptions = AccountMetadataException.class)
    public void testUpdateBatchDisclosureOptionsSqlException() throws Exception {
        AccountMetadataDAO dao = new AccountMetadataDAOImpl(new TestQueries());
        Connection connection = Mockito.mock(Connection.class);

        Mockito.when(connection.prepareStatement(Mockito.anyString())).thenThrow(new SQLException("bad"));

    
        Map<String, String> accountMap = new HashMap<>();
        accountMap.put("acc-901", "no-sharing");

        dao.updateBatchDisclosureOptions(connection, accountMap);
    }

    /**
     * Verifies successful batch insert of secondary account instructions.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testAddBatchSecondaryAccountInstructionsSuccess() throws Exception {
        AccountMetadataDAO dao = new AccountMetadataDAOImpl(new TestQueries());
        Connection connection = Mockito.mock(Connection.class);
        PreparedStatement statement = Mockito.mock(PreparedStatement.class);

        Mockito.when(connection.prepareStatement(Mockito.anyString())).thenReturn(statement);
        Mockito.when(statement.executeBatch()).thenReturn(new int[]{1, 1});

        List<SecondaryAccountInstructionItem> items = Arrays.asList(
                buildSecondaryItem("acc-910", "user-1", true, "active"),
                buildSecondaryItem("acc-911", "user-2", false, "inactive"));
        dao.addBatchSecondaryAccountInstructions(connection, items);

        Mockito.verify(statement, Mockito.times(2))
                .setString(Mockito.eq(1), Mockito.anyString());
        Mockito.verify(statement, Mockito.times(2))
                .setString(Mockito.eq(2), Mockito.anyString());
        Mockito.verify(statement, Mockito.times(2))
                .setString(Mockito.eq(3), Mockito.anyString());
        Mockito.verify(statement, Mockito.times(2))
                .setObject(Mockito.eq(4), Mockito.any(), Mockito.anyInt());
        Mockito.verify(statement, Mockito.times(2))
                .setTimestamp(Mockito.eq(5), Mockito.any(Timestamp.class));
        Mockito.verify(statement, Mockito.times(2)).addBatch();
        Mockito.verify(statement).executeBatch();
    }

    /**
     * Verifies that no insert call is made when secondary instruction input is empty.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testAddBatchSecondaryAccountInstructionsEmpty() throws Exception {
        AccountMetadataDAO dao = new AccountMetadataDAOImpl(new TestQueries());
        Connection connection = Mockito.mock(Connection.class);

        dao.addBatchSecondaryAccountInstructions(connection, Collections.emptyList());

        Mockito.verify(connection, Mockito.never()).prepareStatement(Mockito.anyString());
    }

    /**
     * Verifies successful batch update of secondary account instructions.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testUpdateBatchSecondaryAccountInstructionsSuccess() throws Exception {
        AccountMetadataDAO dao = new AccountMetadataDAOImpl(new TestQueries());
        Connection connection = Mockito.mock(Connection.class);
        PreparedStatement statement = Mockito.mock(PreparedStatement.class);

        Mockito.when(connection.prepareStatement(Mockito.anyString())).thenReturn(statement);
        Mockito.when(statement.executeBatch()).thenReturn(new int[]{1, 1});

        List<SecondaryAccountInstructionItem> items = Arrays.asList(
                buildSecondaryItem("acc-920", "user-1", true, "active"),
                buildSecondaryItem
                        ("acc-921", "user-2", false, "inactive"));
        dao.updateBatchSecondaryAccountInstructions(connection, items);

        Mockito.verify(statement, Mockito.times(2))
                .setString(Mockito.eq(1), Mockito.anyString());
        Mockito.verify(statement, Mockito.times(2))
                .setObject(Mockito.eq(2), Mockito.any(), Mockito.anyInt());
        Mockito.verify(statement, Mockito.times(2))
                .setTimestamp(Mockito.eq(3), Mockito.any(Timestamp.class));
        Mockito.verify(statement, Mockito.times(2))
                .setString(Mockito.eq(4), Mockito.anyString());
        Mockito.verify(statement, Mockito.times(2))
                .setString(Mockito.eq(5), Mockito.anyString());
        Mockito.verify(statement, Mockito.times(2)).addBatch();
        Mockito.verify(statement).executeBatch();
    }

    /**
     * Builds a secondary instruction test item.
     *
     * @param accountId account id
     * @param userId secondary user id
     * @param otherAccountsAvailable whether other accounts are available
     * @param status instruction status
     * @return populated test item
     */
    private SecondaryAccountInstructionItem buildSecondaryItem(String accountId, String userId,
                                                                boolean otherAccountsAvailable, String status) {

        SecondaryAccountInstructionItem item = new SecondaryAccountInstructionItem();
        item.setAccountId(accountId);
        item.setSecondaryUserId(userId);
        item.setOtherAccountsAvailability(otherAccountsAvailable);
        item.setSecondaryAccountInstructionStatus(
                SecondaryAccountInstructionItem.SecondaryAccountInstructionStatusEnum.fromValue(status));
        return item;
    }

    /**
     * Asserts that the given mock object has no interactions (no method calls).
     *
     * @param mock the mock object to check
     */
    private void assertNoInteractions(Object mock) {
        Assert.assertTrue(Mockito.mockingDetails(mock).getInvocations().isEmpty());
    }

}

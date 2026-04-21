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

import static org.wso2.openbanking.consumerdatastandards.account.metadata.utils.CommonTestUtils.buildSecondaryItem;

import org.apache.commons.lang3.tuple.Pair;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.openbanking.consumerdatastandards.account.metadata.exceptions.AccountMetadataException;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.SecondaryAccountInstructionItem;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.dao.AccountMetadataDAO;
import org.wso2.openbanking.consumerdatastandards.account.metadata.utils.connection.provider.ConnectionProvider;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for {@link AccountMetadataServiceImpl}.
 */
public class AccountMetadataServiceImplTest {

    private AccountMetadataDAO metadataDAO;
    private Connection connection;
    private ConnectionProvider connectionProvider;

    /**
     * Initializes mocks and resets singleton state before each test.
     *
     * @throws Exception if reflection-based reset fails
     */
    @BeforeMethod
    public void setUp() throws Exception {
        metadataDAO = Mockito.mock(AccountMetadataDAO.class);
        connection = Mockito.mock(Connection.class);
        connectionProvider = new ConnectionProvider() {
            @Override
            public Connection getConnection() throws SQLException {
                return connection;
            }
        };
        resetSingleton();
    }

    /**
     * Verifies singleton creation when DAO is explicitly provided.
     *
     * @throws Exception if singleton reset fails
     */
    @Test
    public void testGetInstanceWithMetadataDaoOnly() throws Exception {
        resetSingleton();

        AccountMetadataServiceImpl service = AccountMetadataServiceImpl.getInstance(metadataDAO);

        Assert.assertNotNull(service);
    }

    /**
     * Verifies default singleton behavior returns the same instance.
     *
     * @throws Exception if singleton reset fails
     */
    @Test
    public void testGetInstanceDefault() throws Exception {
        resetSingleton();

        AccountMetadataServiceImpl service = AccountMetadataServiceImpl.getInstance();
        AccountMetadataServiceImpl serviceAgain = AccountMetadataServiceImpl.getInstance();

        Assert.assertSame(service, serviceAgain);
    }

    /**
     * Verifies successful retrieval of disclosure options through the service layer.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testGetBatchDisclosureOptions() throws Exception {
        Map<String, String> expectedResult = new HashMap<>();
        expectedResult.put("acc-111", "pre-approval");
        expectedResult.put("acc-222", "no-sharing");
        
        Mockito.when(metadataDAO.getBatchDisclosureOptions(connection, Arrays.asList("acc-111", "acc-222")))
                .thenReturn(expectedResult);

        AccountMetadataServiceImpl service = AccountMetadataServiceImpl.getInstance(metadataDAO, connectionProvider);
        Map<String, String> result = service.getBatchDisclosureOptions(Arrays.asList("acc-111", "acc-222"));

        Assert.assertEquals(result, expectedResult);
        Mockito.verify(metadataDAO).getBatchDisclosureOptions(connection, Arrays.asList("acc-111", "acc-222"));
    }

    /**
     * Verifies service propagation of DAO exceptions during disclosure option retrieval.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test(expectedExceptions = AccountMetadataException.class)
    public void testGetBatchDisclosureOptionsDaoException() throws Exception {
        Mockito.when(metadataDAO.getBatchDisclosureOptions(connection, Collections.singletonList("acc-333")))
                .thenThrow(new AccountMetadataException("dao error"));

        AccountMetadataServiceImpl service = AccountMetadataServiceImpl.getInstance(metadataDAO, connectionProvider);

        service.getBatchDisclosureOptions(Collections.singletonList("acc-333"));
    }

    /**
     * Verifies successful add operation for disclosure options.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testAddBatchDisclosureOptions() throws Exception {
        Map<String, String> accountMap = new HashMap<>();
        accountMap.put("acc-444", "no-sharing");
        accountMap.put("acc-555", "pre-approval");

        AccountMetadataServiceImpl service = AccountMetadataServiceImpl.getInstance(metadataDAO, connectionProvider);

        service.addBatchDisclosureOptions(accountMap);

        Mockito.verify(metadataDAO).addBatchDisclosureOptions(connection, accountMap);
    }

    /**
     * Verifies service propagation of DAO exceptions during disclosure option add.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test(expectedExceptions = AccountMetadataException.class)
    public void testAddBatchDisclosureOptionsDaoException() throws Exception {
        Map<String, String> accountMap = new HashMap<>();
        accountMap.put("acc-666", "no-sharing");
        
        Mockito.doThrow(new AccountMetadataException("dao error"))
                .when(metadataDAO)
                .addBatchDisclosureOptions(connection, accountMap);

        AccountMetadataServiceImpl service = AccountMetadataServiceImpl.getInstance(metadataDAO, connectionProvider);

        service.addBatchDisclosureOptions(accountMap);
    }

    /**
     * Verifies successful update operation for disclosure options.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testUpdateBatchDisclosureOptions() throws Exception {
        Map<String, String> accountMap = new HashMap<>();
        accountMap.put("acc-777", "pre-approval");
        accountMap.put("acc-888", "no-sharing");

        AccountMetadataServiceImpl service = AccountMetadataServiceImpl.getInstance(metadataDAO, connectionProvider);

        service.updateBatchDisclosureOptions(accountMap);

        Mockito.verify(metadataDAO).updateBatchDisclosureOptions(connection, accountMap);
    }

    /**
     * Verifies service propagation of DAO exceptions during disclosure option update.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test(expectedExceptions = AccountMetadataException.class)
    public void testUpdateBatchDisclosureOptionsDaoException() throws Exception {
        Map<String, String> accountMap = new HashMap<>();
        accountMap.put("acc-999", "pre-approval");
        
        Mockito.doThrow(new AccountMetadataException("dao error"))
                .when(metadataDAO)
                .updateBatchDisclosureOptions(connection, accountMap);

        AccountMetadataServiceImpl service = AccountMetadataServiceImpl.getInstance(metadataDAO, connectionProvider);

        service.updateBatchDisclosureOptions(accountMap);
    }

    /**
     * Verifies successful retrieval of secondary account instructions through the service layer.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testGetBatchSecondaryAccountInstructions() throws Exception {
        List<Pair<String, String>> accountUserPairs = Arrays.asList(
                Pair.of("acc-123", "user-1"),
                Pair.of("acc-124", "user-2"));

        List<SecondaryAccountInstructionItem> expected = new ArrayList<>(Arrays.asList(
                buildSecondaryItem("acc-123", "user-1", true, "active"),
                buildSecondaryItem("acc-124", "user-2", false, "inactive")));
        Mockito.when(metadataDAO.getBatchSecondaryAccountInstructions(connection, accountUserPairs))
            .thenReturn(expected);

        AccountMetadataServiceImpl service = AccountMetadataServiceImpl.getInstance(metadataDAO, connectionProvider);
        List<SecondaryAccountInstructionItem> result =
            service.getBatchSecondaryAccountInstructions(accountUserPairs);

        Assert.assertEquals(result, expected);
        Mockito.verify(metadataDAO).getBatchSecondaryAccountInstructions(connection, accountUserPairs);
    }

    /**
     * Verifies service propagation of DAO exceptions during secondary instruction retrieval.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test(expectedExceptions = AccountMetadataException.class)
    public void testGetBatchSecondaryAccountInstructionsDaoException() throws Exception {
        List<Pair<String, String>> accountUserPairs = Collections.singletonList(Pair.of("acc-125", "user-1"));

        Mockito.when(metadataDAO.getBatchSecondaryAccountInstructions(connection, accountUserPairs))
            .thenThrow(new AccountMetadataException("dao error"));

        AccountMetadataServiceImpl service = AccountMetadataServiceImpl.getInstance(metadataDAO, connectionProvider);

        service.getBatchSecondaryAccountInstructions(accountUserPairs);
    }

    /**
     * Verifies successful add operation for secondary account instructions.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testAddBatchSecondaryAccountInstructions() throws Exception {
        List<SecondaryAccountInstructionItem> items = Collections.singletonList(
                buildSecondaryItem("acc-126", "user-1", true, "active"));
        AccountMetadataServiceImpl service = AccountMetadataServiceImpl.getInstance(metadataDAO, connectionProvider);

        service.addBatchSecondaryAccountInstructions(items);

        Mockito.verify(metadataDAO).addBatchSecondaryAccountInstructions(connection, items);
    }

    /**
     * Verifies service propagation of DAO exceptions during secondary instruction add.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test(expectedExceptions = AccountMetadataException.class)
    public void testAddBatchSecondaryAccountInstructionsDaoException() throws Exception {
        List<SecondaryAccountInstructionItem> items = Collections.singletonList(
                buildSecondaryItem("acc-127", "user-1", true, "active"));
        Mockito.doThrow(new AccountMetadataException("dao error"))
            .when(metadataDAO)
            .addBatchSecondaryAccountInstructions(connection, items);

        AccountMetadataServiceImpl service = AccountMetadataServiceImpl.getInstance(metadataDAO, connectionProvider);

        service.addBatchSecondaryAccountInstructions(items);
    }

    /**
     * Verifies successful update operation for secondary account instructions.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testUpdateBatchSecondaryAccountInstructions() throws Exception {
        List<SecondaryAccountInstructionItem> items = Collections.singletonList(
                buildSecondaryItem("acc-128", "user-2", false, "inactive"));
        AccountMetadataServiceImpl service = AccountMetadataServiceImpl.getInstance(metadataDAO, connectionProvider);

        service.updateBatchSecondaryAccountInstructions(items);

        Mockito.verify(metadataDAO).updateBatchSecondaryAccountInstructions(connection, items);
    }

    /**
     * Verifies service propagation of DAO exceptions during secondary instruction update.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test(expectedExceptions = AccountMetadataException.class)
    public void testUpdateBatchSecondaryAccountInstructionsDaoException() throws Exception {
        List<SecondaryAccountInstructionItem> items = Collections.singletonList(
                buildSecondaryItem("acc-129", "user-3", true, "active"));
        Mockito.doThrow(new AccountMetadataException("dao error"))
            .when(metadataDAO)
            .updateBatchSecondaryAccountInstructions(connection, items);

        AccountMetadataServiceImpl service = AccountMetadataServiceImpl.getInstance(metadataDAO, connectionProvider);

        service.updateBatchSecondaryAccountInstructions(items);
    }

    /**
     * Resets singleton state to isolate test execution.
     *
     * @throws Exception if reflection access fails
     */
    private void resetSingleton() throws Exception {
        Field instanceField = AccountMetadataServiceImpl.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }
}

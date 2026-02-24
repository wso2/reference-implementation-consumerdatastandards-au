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

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.openbanking.consumerdatastandards.account.metadata.exceptions.AccountMetadataException;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.dao.AccountMetadataDAO;
import org.wso2.openbanking.consumerdatastandards.account.metadata.utils.connection.provider.ConnectionProvider;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit test suite for AccountMetadataServiceImpl.
 * Tests the singleton service layer that manages batch disclosure option operations
 * and handles connection management and exception wrapping.
 */
public class AccountMetadataServiceImplTest {

    private AccountMetadataDAO metadataDAO;
    private Connection connection;
    private ConnectionProvider connectionProvider;

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
     * Verifies that the singleton instance can be created with a specific metadata DAO.
     * Tests the getInstance(AccountMetadataDAO metadataDAO) factory method.
     *
     * @throws Exception if reflection or singleton reset fails
     */
    @Test
    public void testGetInstanceWithMetadataDaoOnly() throws Exception {
        resetSingleton();

        AccountMetadataServiceImpl service = AccountMetadataServiceImpl.getInstance(metadataDAO);

        Assert.assertNotNull(service);
    }

    /**
     * Verifies singleton pattern enforcement: multiple calls to getInstance() return the same instance.
     * Tests the getInstance() factory method with default connection provider.
     *
     * @throws Exception if reflection or singleton reset fails
     */
    @Test
    public void testGetInstanceDefault() throws Exception {
        resetSingleton();

        AccountMetadataServiceImpl service = AccountMetadataServiceImpl.getInstance();
        AccountMetadataServiceImpl serviceAgain = AccountMetadataServiceImpl.getInstance();

        Assert.assertSame(service, serviceAgain);
    }

    /**
     * Verifies successful batch retrieval of disclosure options from the DAO layer.
     * Tests that the service correctly delegates to the DAO and returns the result.
     *
     * @throws Exception if test setup or DAO invocation fails
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
     * Verifies that DAO-level exceptions during batch retrieval are properly propagated.
     * Tests that AccountMetadataException from DAO is rethrown without modification.
     *
     * @throws Exception if test setup fails (expected exception is verified by TestNG)
     */
    @Test(expectedExceptions = AccountMetadataException.class)
    public void testGetBatchDisclosureOptionsDaoException() throws Exception {
        Mockito.when(metadataDAO.getBatchDisclosureOptions(connection, Collections.singletonList("acc-333")))
                .thenThrow(new AccountMetadataException("dao error"));

        AccountMetadataServiceImpl service = AccountMetadataServiceImpl.getInstance(metadataDAO, connectionProvider);

        service.getBatchDisclosureOptions(Collections.singletonList("acc-333"));
    }

    /**
     * Verifies successful batch insert of disclosure options into the DAO layer.
     * Tests that the service correctly delegates account metadata to the DAO.
     *
     * @throws Exception if test setup or DAO invocation fails
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
     * Verifies that DAO-level exceptions during batch insert are properly propagated.
     * Tests that AccountMetadataException from DAO is rethrown without modification.
     *
     * @throws Exception if test setup fails (expected exception is verified by TestNG)
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
     * Verifies successful batch update of disclosure options in the DAO layer.
     * Tests that the service correctly delegates account metadata changes to the DAO.
     *
     * @throws Exception if test setup or DAO invocation fails
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
     * Verifies that DAO-level exceptions during batch update are properly propagated.
     * Tests that AccountMetadataException from DAO is rethrown without modification.
     *
     * @throws Exception if test setup fails (expected exception is verified by TestNG)
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
     * Verifies that connection failures during batch retrieval are wrapped as AccountMetadataException.
     * Tests that SQLException from connection provider is caught and wrapped.
     * This covers the exception handling path when the database becomes unavailable.
     *
     * @throws Exception if test setup fails (expected exception is verified by TestNG)
     */
    @Test(expectedExceptions = AccountMetadataException.class)
    public void testGetBatchDisclosureOptionsConnectionException() throws Exception {
        ConnectionProvider failingProvider = new ConnectionProvider() {
            @Override
            public Connection getConnection() throws SQLException {
                throw new SQLException("db down");
            }
        };

        AccountMetadataServiceImpl service = AccountMetadataServiceImpl.getInstance(metadataDAO, failingProvider);

        service.getBatchDisclosureOptions(Collections.singletonList("acc-1000"));
    }

    /**
     * Verifies that connection failures during batch insert are wrapped as AccountMetadataException.
     * Tests that SQLException from connection provider is caught and wrapped.
     * This covers the exception handling path when the database becomes unavailable.
     *
     * @throws Exception if test setup fails (expected exception is verified by TestNG)
     */
    @Test(expectedExceptions = AccountMetadataException.class)
    public void testAddBatchDisclosureOptionsConnectionException() throws Exception {
        ConnectionProvider failingProvider = new ConnectionProvider() {
            @Override
            public Connection getConnection() throws SQLException {
                throw new SQLException("db down");
            }
        };

        AccountMetadataServiceImpl service = AccountMetadataServiceImpl.getInstance(metadataDAO, failingProvider);

        Map<String, String> accountMap = new HashMap<>();
        accountMap.put("acc-1001", "no-sharing");
        service.addBatchDisclosureOptions(accountMap);
    }

    /**
     * Verifies that connection failures during batch update are wrapped as AccountMetadataException.
     * Tests that SQLException from connection provider is caught and wrapped.
     * This covers the exception handling path when the database becomes unavailable.
     *
     * @throws Exception if test setup fails (expected exception is verified by TestNG)
     */
    @Test(expectedExceptions = AccountMetadataException.class)
    public void testUpdateBatchDisclosureOptionsConnectionException() throws Exception {
        ConnectionProvider failingProvider = new ConnectionProvider() {
            @Override
            public Connection getConnection() throws SQLException {
                throw new SQLException("db down");
            }
        };

        AccountMetadataServiceImpl service = AccountMetadataServiceImpl.getInstance(metadataDAO, failingProvider);

        Map<String, String> accountMap = new HashMap<>();
        accountMap.put("acc-1002", "pre-approval");
        service.updateBatchDisclosureOptions(accountMap);
    }

    /**
     * Verifies that the singleton instance can be reset and cleared.
     * Tests the public resetInstance() static method via reflection verification.
     * Ensures that the singleton pattern can be reset for testing purposes.
     *
     * @throws Exception if reflection or assertion fails
     */
    @Test
    public void testResetInstance() throws Exception {
        AccountMetadataServiceImpl service = AccountMetadataServiceImpl.getInstance(metadataDAO, connectionProvider);
        Assert.assertNotNull(service);

        AccountMetadataServiceImpl.resetInstance();

        Field instanceField = AccountMetadataServiceImpl.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        Assert.assertNull(instanceField.get(null));
    }

    /**
     * Helper method to reset the singleton instance field for test isolation.
     * Uses reflection to directly set the private static instance field to null.
     * Ensures test independence by clearing the singleton state before each test.
     *
     * @throws Exception if reflection access or modification fails
     */
    private void resetSingleton() throws Exception {
        Field instanceField = AccountMetadataServiceImpl.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }
}

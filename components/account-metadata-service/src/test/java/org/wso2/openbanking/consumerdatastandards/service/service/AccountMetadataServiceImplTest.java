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

package org.wso2.openbanking.consumerdatastandards.service.service;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.exceptions.AccountMetadataException;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.service.dao.AccountMetadataDAO;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.service.service.AccountMetadataServiceImpl;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.utils.connection.provider.ConnectionProvider;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Test
    public void testGetInstanceWithMetadataDaoOnly() throws Exception {
        resetSingleton();

        AccountMetadataServiceImpl service = AccountMetadataServiceImpl.getInstance(metadataDAO);

        Assert.assertNotNull(service);
    }

    @Test
    public void testGetInstanceDefault() throws Exception {
        resetSingleton();

        AccountMetadataServiceImpl service = AccountMetadataServiceImpl.getInstance();
        AccountMetadataServiceImpl serviceAgain = AccountMetadataServiceImpl.getInstance();

        Assert.assertSame(service, serviceAgain);
    }

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

    @Test(expectedExceptions = AccountMetadataException.class)
    public void testGetBatchDisclosureOptionsDaoException() throws Exception {
        Mockito.when(metadataDAO.getBatchDisclosureOptions(connection, List.of("acc-333")))
                .thenThrow(new AccountMetadataException("dao error"));

        AccountMetadataServiceImpl service = AccountMetadataServiceImpl.getInstance(metadataDAO, connectionProvider);

        service.getBatchDisclosureOptions(List.of("acc-333"));
    }

    @Test
    public void testAddBatchDisclosureOptions() throws Exception {
        Map<String, String> accountMap = new HashMap<>();
        accountMap.put("acc-444", "no-sharing");
        accountMap.put("acc-555", "pre-approval");

        AccountMetadataServiceImpl service = AccountMetadataServiceImpl.getInstance(metadataDAO, connectionProvider);

        service.addBatchDisclosureOptions(accountMap);

        Mockito.verify(metadataDAO).addBatchDisclosureOptions(connection, accountMap);
    }

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

    @Test
    public void testUpdateBatchDisclosureOptions() throws Exception {
        Map<String, String> accountMap = new HashMap<>();
        accountMap.put("acc-777", "pre-approval");
        accountMap.put("acc-888", "no-sharing");

        AccountMetadataServiceImpl service = AccountMetadataServiceImpl.getInstance(metadataDAO, connectionProvider);

        service.updateBatchDisclosureOptions(accountMap);

        Mockito.verify(metadataDAO).updateBatchDisclosureOptions(connection, accountMap);
    }

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

    @Test
    public void testGetBlockedAccounts() throws Exception {
        List<String> expectedResult = Arrays.asList("acc-111", "acc-333");
        
        Mockito.when(metadataDAO.getBlockedAccounts(connection, Arrays.asList("acc-111", "acc-222", "acc-333")))
                .thenReturn(expectedResult);

        AccountMetadataServiceImpl service = AccountMetadataServiceImpl.getInstance(metadataDAO, connectionProvider);
        List<String> result = service.getBlockedAccounts(Arrays.asList("acc-111", "acc-222", "acc-333"));

        Assert.assertEquals(result, expectedResult);
        Mockito.verify(metadataDAO).getBlockedAccounts(connection, Arrays.asList("acc-111", "acc-222", "acc-333"));
    }

    @Test
    public void testGetBlockedAccountsEmpty() throws Exception {
        List<String> expectedResult = Collections.emptyList();
        
        Mockito.when(metadataDAO.getBlockedAccounts(connection, Arrays.asList("acc-444", "acc-555")))
                .thenReturn(expectedResult);

        AccountMetadataServiceImpl service = AccountMetadataServiceImpl.getInstance(metadataDAO, connectionProvider);
        List<String> result = service.getBlockedAccounts(Arrays.asList("acc-444", "acc-555"));

        Assert.assertEquals(result, Collections.emptyList());
    }

    @Test(expectedExceptions = AccountMetadataException.class)
    public void testGetBlockedAccountsDaoException() throws Exception {
        Mockito.when(metadataDAO.getBlockedAccounts(connection, Arrays.asList("acc-666", "acc-777")))
                .thenThrow(new AccountMetadataException("dao error"));

        AccountMetadataServiceImpl service = AccountMetadataServiceImpl.getInstance(metadataDAO, connectionProvider);

        service.getBlockedAccounts(Arrays.asList("acc-666", "acc-777"));
    }

    private void resetSingleton() throws Exception {
        Field instanceField = AccountMetadataServiceImpl.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }
}

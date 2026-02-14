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
import org.wso2.openbanking.consumerdatastandards.exceptions.AccountMetadataException;
import org.wso2.openbanking.consumerdatastandards.service.dao.AccountMetadataDAO;
import org.wso2.openbanking.consumerdatastandards.utils.connection.provider.ConnectionProvider;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;

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
    public void testAddDisclosureOption() throws Exception {
        AccountMetadataServiceImpl service = AccountMetadataServiceImpl.getInstance(metadataDAO, connectionProvider);

        service.addDisclosureOption("acc-123", "no-sharing");

        Mockito.verify(metadataDAO).addDisclosureOption(
                connection, "acc-123", "no-sharing");
    }

    @Test(expectedExceptions = AccountMetadataException.class)
    public void testAddDisclosureOptionBlankAccountId() throws Exception {
        AccountMetadataServiceImpl service = AccountMetadataServiceImpl.getInstance(metadataDAO, connectionProvider);

        service.addDisclosureOption(" ", "no-sharing");
    }

    @Test(expectedExceptions = AccountMetadataException.class)
    public void testAddDisclosureOptionConnectionFailure() throws Exception {
        ConnectionProvider failingProvider = Mockito.mock(ConnectionProvider.class);
        Mockito.when(failingProvider.getConnection()).thenThrow(new SQLException("db down"));

        AccountMetadataServiceImpl service = AccountMetadataServiceImpl.getInstance(metadataDAO, failingProvider);

        service.addDisclosureOption("acc-000", "pre-approval");
    }

    @Test
    public void testGetDisclosureOption() throws Exception {
        Mockito.when(metadataDAO.getDisclosureOption(connection, "acc-456"))
                .thenReturn("pre-approval");

        AccountMetadataServiceImpl service = AccountMetadataServiceImpl.getInstance(metadataDAO, connectionProvider);
        String status = service.getDisclosureOption("acc-456");

        Assert.assertEquals(status, "pre-approval");
    }

    @Test(expectedExceptions = AccountMetadataException.class)
    public void testUpdateDisclosureOptionBlankStatus() throws Exception {
        AccountMetadataServiceImpl service = AccountMetadataServiceImpl.getInstance(metadataDAO, connectionProvider);

        service.updateDisclosureOption("acc-789", "");
    }

    @Test
    public void testUpdateDisclosureOptionSuccess() throws Exception {
        AccountMetadataServiceImpl service = AccountMetadataServiceImpl.getInstance(metadataDAO, connectionProvider);

        service.updateDisclosureOption("acc-789", "pre-approval");

        Mockito.verify(metadataDAO).updateDisclosureOption(connection, "acc-789", "pre-approval");
    }

    @Test(expectedExceptions = AccountMetadataException.class)
    public void testUpdateDisclosureOptionBlankAccountId() throws Exception {
        AccountMetadataServiceImpl service = AccountMetadataServiceImpl.getInstance(metadataDAO, connectionProvider);

        service.updateDisclosureOption(" ", "no-sharing");
    }

    @Test(expectedExceptions = AccountMetadataException.class)
    public void testUpdateDisclosureOptionDaoException() throws Exception {
        Mockito.doThrow(new AccountMetadataException("dao error"))
                .when(metadataDAO)
                .updateDisclosureOption(connection, "acc-999", "no-sharing");

        AccountMetadataServiceImpl service = AccountMetadataServiceImpl.getInstance(metadataDAO, connectionProvider);

        service.updateDisclosureOption("acc-999", "no-sharing");
    }

    @Test(expectedExceptions = AccountMetadataException.class)
    public void testGetDisclosureOptionBlankAccountId() throws Exception {
        AccountMetadataServiceImpl service = AccountMetadataServiceImpl.getInstance(metadataDAO, connectionProvider);

        service.getDisclosureOption(" ");
    }

    @Test(expectedExceptions = AccountMetadataException.class)
    public void testGetDisclosureOptionDaoException() throws Exception {
        Mockito.when(metadataDAO.getDisclosureOption(connection, "acc-777"))
                .thenThrow(new AccountMetadataException("dao error"));

        AccountMetadataServiceImpl service = AccountMetadataServiceImpl.getInstance(metadataDAO, connectionProvider);

        service.getDisclosureOption("acc-777");
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

    private void resetSingleton() throws Exception {
        Field instanceField = AccountMetadataServiceImpl.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }
}

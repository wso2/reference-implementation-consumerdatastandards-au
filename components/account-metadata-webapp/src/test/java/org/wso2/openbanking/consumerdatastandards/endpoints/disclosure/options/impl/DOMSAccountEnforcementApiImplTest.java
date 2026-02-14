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

package org.wso2.openbanking.consumerdatastandards.endpoints.disclosure.options.impl;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.openbanking.consumerdatastandards.endpoints.disclosure.options.model.DOMSBlockedAccountsRequest;
import org.wso2.openbanking.consumerdatastandards.endpoints.disclosure.options.model.DOMSBlockedAccountsResponse;
import org.wso2.openbanking.consumerdatastandards.exceptions.AccountMetadataException;
import org.wso2.openbanking.consumerdatastandards.service.dao.AccountMetadataDAO;
import org.wso2.openbanking.consumerdatastandards.service.service.AccountMetadataServiceImpl;
import org.wso2.openbanking.consumerdatastandards.utils.connection.provider.ConnectionProvider;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.Response;

public class DOMSAccountEnforcementApiImplTest {

    private AccountMetadataDAO metadataDAO;
    private ConnectionProvider connectionProvider;
    private Connection connection;

    @BeforeClass
    public void setUpClass() throws Exception {
        metadataDAO = Mockito.mock(AccountMetadataDAO.class);
        connectionProvider = Mockito.mock(ConnectionProvider.class);
        connection = Mockito.mock(Connection.class);
        Mockito.when(connectionProvider.getConnection()).thenReturn(connection);

        resetSingleton();
        AccountMetadataServiceImpl.getInstance(metadataDAO, connectionProvider);
        Class.forName(DOMSAccountEnforcementApiImpl.class.getName(), true,
                DOMSAccountEnforcementApiImpl.class.getClassLoader());
    }

    @BeforeMethod
    public void setUp() throws Exception {
        Mockito.reset(metadataDAO, connectionProvider, connection);
        Mockito.when(connectionProvider.getConnection()).thenReturn(connection);
    }

    @Test
    public void testGetBlockedAccountsFiltersNoSharing() throws Exception {
        Mockito.when(metadataDAO.getDisclosureOption(connection, "acc-1")).thenReturn("no-sharing");
        Mockito.when(metadataDAO.getDisclosureOption(connection, "acc-2")).thenReturn("pre-approval");
        Mockito.when(metadataDAO.getDisclosureOption(connection, "acc-3"))
            .thenThrow(new AccountMetadataException("fail"));

        DOMSBlockedAccountsRequest request = new DOMSBlockedAccountsRequest();
        request.setAccountIds(Arrays.asList("acc-1", "acc-2", "acc-3"));

        Response response = DOMSAccountEnforcementApiImpl.getBlockedAccounts(request);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        DOMSBlockedAccountsResponse body = (DOMSBlockedAccountsResponse) response.getEntity();
        List<String> blocked = body.getBlockedAccountIds();
        Assert.assertEquals(blocked, Arrays.asList("acc-1"));
    }

    private void resetSingleton() throws Exception {
        Field instanceField = AccountMetadataServiceImpl.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }
}

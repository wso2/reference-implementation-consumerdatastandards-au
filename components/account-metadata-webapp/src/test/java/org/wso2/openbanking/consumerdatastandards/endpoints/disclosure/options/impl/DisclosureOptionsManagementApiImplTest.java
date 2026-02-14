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
import org.wso2.openbanking.consumerdatastandards.endpoints.disclosure.options.model.ApiResponse;
import org.wso2.openbanking.consumerdatastandards.endpoints.disclosure.options.model.DisclosureOptionItem;
import org.wso2.openbanking.consumerdatastandards.endpoints.disclosure.options.model.DisclosureOptionsBulkResponse;
import org.wso2.openbanking.consumerdatastandards.endpoints.disclosure.options.model.DisclosureOptionsUpdateRequest;
import org.wso2.openbanking.consumerdatastandards.exceptions.AccountMetadataException;
import org.wso2.openbanking.consumerdatastandards.service.dao.AccountMetadataDAO;
import org.wso2.openbanking.consumerdatastandards.service.service.AccountMetadataServiceImpl;
import org.wso2.openbanking.consumerdatastandards.utils.connection.provider.ConnectionProvider;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

public class DisclosureOptionsManagementApiImplTest {

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
        Class.forName(DisclosureOptionsManagementApiImpl.class.getName(), true,
                DisclosureOptionsManagementApiImpl.class.getClassLoader());
    }

    @BeforeMethod
    public void setUp() throws Exception {
        Mockito.reset(metadataDAO, connectionProvider, connection);
        Mockito.when(connectionProvider.getConnection()).thenReturn(connection);
    }

    @Test
    public void testUpdateDisclosureOptionsBadRequestOnNull() {
        Response response = DisclosureOptionsManagementApiImpl.updateDisclosureOptions(null);

        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
        ApiResponse body = (ApiResponse) response.getEntity();
        Assert.assertEquals(body.getStatus(), "error");
    }

    @Test
    public void testUpdateDisclosureOptionsBadRequestOnInvalidStatus() {
        DisclosureOptionsUpdateRequest request = buildRequest("acc-1", "invalid");

        Response response = DisclosureOptionsManagementApiImpl.updateDisclosureOptions(request);

        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
        ApiResponse body = (ApiResponse) response.getEntity();
        Assert.assertEquals(body.getStatus(), "error");
    }

    @Test
    public void testUpdateDisclosureOptionsSuccess() throws Exception {
        DisclosureOptionsUpdateRequest request = buildRequest("acc-1", "no-sharing");

        Response response = DisclosureOptionsManagementApiImpl.updateDisclosureOptions(request);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        ApiResponse body = (ApiResponse) response.getEntity();
        Assert.assertEquals(body.getStatus(), "success");
        Mockito.verify(metadataDAO).updateDisclosureOption(connection, "acc-1", "no-sharing");
    }

    @Test
    public void testUpdateDisclosureOptionsServiceError() throws Exception {
        DisclosureOptionsUpdateRequest request = buildRequest("acc-1", "pre-approval");
        Mockito.doThrow(new AccountMetadataException("fail"))
            .when(metadataDAO)
            .updateDisclosureOption(connection, "acc-1", "pre-approval");

        Response response = DisclosureOptionsManagementApiImpl.updateDisclosureOptions(request);

        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        ApiResponse body = (ApiResponse) response.getEntity();
        Assert.assertEquals(body.getStatus(), "error");
    }

    @Test
    public void testGetDisclosureOptionsBadRequestOnEmpty() {
        Response response = DisclosureOptionsManagementApiImpl.getDisclosureOptions(Collections.emptyList());

        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
        ApiResponse body = (ApiResponse) response.getEntity();
        Assert.assertEquals(body.getStatus(), "error");
    }

    @Test
    public void testGetDisclosureOptionsSuccess() throws Exception {
        List<String> accounts = Arrays.asList("acc-1", " ");
        Mockito.when(metadataDAO.getDisclosureOption(connection, "acc-1")).thenReturn("pre-approval");

        Response response = DisclosureOptionsManagementApiImpl.getDisclosureOptions(accounts);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        DisclosureOptionsBulkResponse body = (DisclosureOptionsBulkResponse) response.getEntity();
        Assert.assertEquals(body.getStatus(), "success");
        Map<String, String> data = body.getData();
        Assert.assertEquals(data.get("acc-1"), "pre-approval");
        Assert.assertNull(data.get(" "));
    }

    @Test
    public void testAddDisclosureOptionsCreatedWhenAllNew() throws Exception {
        DisclosureOptionsUpdateRequest request = buildRequest("acc-1", "no-sharing");
        Mockito.when(metadataDAO.getDisclosureOption(connection, "acc-1")).thenReturn(null);

        Response response = DisclosureOptionsManagementApiImpl.addDisclosureOptions(request);

        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
        ApiResponse body = (ApiResponse) response.getEntity();
        Assert.assertEquals(body.getStatus(), "success");
        Mockito.verify(metadataDAO).addDisclosureOption(connection, "acc-1", "no-sharing");
    }

    @Test
    public void testAddDisclosureOptionsOkWhenExisting() throws Exception {
        DisclosureOptionsUpdateRequest request = buildRequest("acc-1", "no-sharing");
        Mockito.when(metadataDAO.getDisclosureOption(connection, "acc-1")).thenReturn("pre-approval");

        Response response = DisclosureOptionsManagementApiImpl.addDisclosureOptions(request);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        ApiResponse body = (ApiResponse) response.getEntity();
        Assert.assertEquals(body.getStatus(), "success");
    }

    @Test
    public void testAddDisclosureOptionsBadRequestOnInvalidStatus() {
        DisclosureOptionsUpdateRequest request = buildRequest("acc-1", "invalid");

        Response response = DisclosureOptionsManagementApiImpl.addDisclosureOptions(request);

        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
        ApiResponse body = (ApiResponse) response.getEntity();
        Assert.assertEquals(body.getStatus(), "error");
    }

    @Test
    public void testAddDisclosureOptionsServiceError() throws Exception {
        DisclosureOptionsUpdateRequest request = buildRequest("acc-1", "pre-approval");
        Mockito.when(metadataDAO.getDisclosureOption(connection, "acc-1"))
            .thenThrow(new AccountMetadataException("fail"));

        Response response = DisclosureOptionsManagementApiImpl.addDisclosureOptions(request);

        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        ApiResponse body = (ApiResponse) response.getEntity();
        Assert.assertEquals(body.getStatus(), "error");
    }

    private DisclosureOptionsUpdateRequest buildRequest(String accountId, String status) {
        DisclosureOptionItem item = new DisclosureOptionItem();
        item.setAccountID(accountId);
        item.setDisclosureOption(status);

        DisclosureOptionsUpdateRequest request = new DisclosureOptionsUpdateRequest();
        request.setData(Collections.singletonList(item));
        return request;
    }

    private void resetSingleton() throws Exception {
        Field instanceField = AccountMetadataServiceImpl.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }
}

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

package org.wso2.openbanking.consumerdatastandards.impl;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.exceptions.AccountMetadataException;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.impl.DisclosureOptionsManagementApiImpl;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.model.DisclosureOptionItem;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.model.ModelApiResponse;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.service.dao.AccountMetadataDAO;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.service.service.AccountMetadataServiceImpl;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.utils.connection.provider.ConnectionProvider;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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
        ModelApiResponse body = (ModelApiResponse) response.getEntity();
        Assert.assertNotNull(body);
    }

    @Test
    public void testUpdateDisclosureOptionsBadRequestOnInvalidStatus() {
        List<DisclosureOptionItem> request = buildRequest("invalid");

        Response response = DisclosureOptionsManagementApiImpl.updateDisclosureOptions(request);

        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
        ModelApiResponse body = (ModelApiResponse) response.getEntity();
        Assert.assertNotNull(body);
    }

    @Test
    public void testUpdateDisclosureOptionsSuccess() throws Exception {
        List<DisclosureOptionItem> request = buildRequest("no-sharing");

        Response response = DisclosureOptionsManagementApiImpl.updateDisclosureOptions(request);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        ModelApiResponse body = (ModelApiResponse) response.getEntity();
        Assert.assertNotNull(body);
        Mockito.verify(metadataDAO).updateBatchDisclosureOptions(connection, 
                Collections.singletonMap("acc-1", "no-sharing"));
    }

    @Test
    public void testUpdateDisclosureOptionsServiceError() throws Exception {
        List<DisclosureOptionItem> request = buildRequest("pre-approval");
        Mockito.doThrow(new AccountMetadataException("fail"))
            .when(metadataDAO)
            .updateBatchDisclosureOptions(connection, Collections.singletonMap("acc-1", "pre-approval"));

        Response response = DisclosureOptionsManagementApiImpl.updateDisclosureOptions(request);

        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        ModelApiResponse body = (ModelApiResponse) response.getEntity();
        Assert.assertNotNull(body);
    }

    @Test
    public void testGetDisclosureOptionsBadRequestOnEmpty() {
        Response response = DisclosureOptionsManagementApiImpl.getDisclosureOptions("");

        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
        ModelApiResponse body = (ModelApiResponse) response.getEntity();
        Assert.assertNotNull(body);
    }

    @Test
    public void testGetDisclosureOptionsBadRequestOnBlankIds() {
        Response response = DisclosureOptionsManagementApiImpl.getDisclosureOptions("   ");

        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
        ModelApiResponse body = (ModelApiResponse) response.getEntity();
        Assert.assertNotNull(body);
    }

    @Test
    public void testGetDisclosureOptionsSuccess() throws Exception {
        Map<String, String> batchResult = new HashMap<>();
        batchResult.put("acc-1", "pre-approval");
        batchResult.put("acc-2", "no-sharing");
        
        Mockito.when(metadataDAO.getBatchDisclosureOptions(connection, Arrays.asList("acc-1", "acc-2")))
                .thenReturn(batchResult);

        Response response = DisclosureOptionsManagementApiImpl.getDisclosureOptions("acc-1,acc-2");

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        @SuppressWarnings("unchecked")
        List<DisclosureOptionItem> body = (List<DisclosureOptionItem>) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertEquals(body.size(), 2);
        Assert.assertTrue(body.stream().anyMatch(item ->
            "acc-1".equals(item.getAccountId()) && "pre-approval".equals(item.getDisclosureOption())));
    }

    @Test
    public void testGetDisclosureOptionsWithSpaces() throws Exception {
        Map<String, String> batchResult = new HashMap<>();
        batchResult.put("acc-1", "pre-approval");
        
        Mockito.when(metadataDAO.getBatchDisclosureOptions(connection, Arrays.asList("acc-1", "acc-2")))
                .thenReturn(batchResult);

        Response response = DisclosureOptionsManagementApiImpl.getDisclosureOptions("acc-1 , acc-2");

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        @SuppressWarnings("unchecked")
        List<DisclosureOptionItem> body = (List<DisclosureOptionItem>) response.getEntity();
        Assert.assertEquals(body.size(), 1);
    }

    @Test
    public void testAddDisclosureOptionsCreatedWhenAllNew() throws Exception {
        List<DisclosureOptionItem> request = buildRequest("no-sharing");
        Map<String, String> batchResult = new HashMap<>();
        
        Mockito.when(metadataDAO.getBatchDisclosureOptions(connection, Arrays.asList("acc-1")))
                .thenReturn(batchResult);

        Response response = DisclosureOptionsManagementApiImpl.addDisclosureOptions(request);

        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
        ModelApiResponse body = (ModelApiResponse) response.getEntity();
        Assert.assertNotNull(body);
        Mockito.verify(metadataDAO).addBatchDisclosureOptions(connection, 
                Collections.singletonMap("acc-1", "no-sharing"));
    }

    @Test
    public void testAddDisclosureOptionsOkWhenExisting() throws Exception {
        List<DisclosureOptionItem> request = buildRequest("no-sharing");
        Map<String, String> existingMap = Collections.singletonMap("acc-1", "pre-approval");
        
        Mockito.when(metadataDAO.getBatchDisclosureOptions(connection, Collections.singletonList("acc-1")))
                .thenReturn(existingMap);

        Response response = DisclosureOptionsManagementApiImpl.addDisclosureOptions(request);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        ModelApiResponse body = (ModelApiResponse) response.getEntity();
        Assert.assertNotNull(body);
    }

    @Test
    public void testAddDisclosureOptionsBadRequestOnInvalidStatus() {
        List<DisclosureOptionItem> request = buildRequest("invalid");

        Response response = DisclosureOptionsManagementApiImpl.addDisclosureOptions(request);

        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
        ModelApiResponse body = (ModelApiResponse) response.getEntity();
        Assert.assertNotNull(body);
    }

    @Test
    public void testAddDisclosureOptionsServiceError() throws Exception {
        List<DisclosureOptionItem> request = buildRequest("pre-approval");
        Mockito.when(metadataDAO.getBatchDisclosureOptions(connection, Collections.singletonList("acc-1")))
            .thenThrow(new AccountMetadataException("fail"));

        Response response = DisclosureOptionsManagementApiImpl.addDisclosureOptions(request);

        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        ModelApiResponse body = (ModelApiResponse) response.getEntity();
        Assert.assertNotNull(body);
    }

    private List<DisclosureOptionItem> buildRequest(String status) {
        DisclosureOptionItem item = new DisclosureOptionItem();
        item.setAccountId("acc-1");
        item.setDisclosureOption(status);
        return Collections.singletonList(item);
    }

    private void resetSingleton() throws Exception {
        Field instanceField = AccountMetadataServiceImpl.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }
}

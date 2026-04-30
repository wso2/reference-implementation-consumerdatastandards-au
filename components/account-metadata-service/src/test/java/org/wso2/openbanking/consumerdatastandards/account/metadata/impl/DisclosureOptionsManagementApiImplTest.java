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

package org.wso2.openbanking.consumerdatastandards.account.metadata.impl;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.openbanking.consumerdatastandards.account.metadata.exceptions.AccountMetadataException;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.DisclosureOptionItem;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.ErrorResponse;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.core.AccountMetadataServiceImpl;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.dao.AccountMetadataDAO;
import org.wso2.openbanking.consumerdatastandards.account.metadata.utils.connection.provider.ConnectionProvider;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

/**
 * Unit tests for {@link DisclosureOptionsManagementApiImpl}.
 *
 * Success responses are validated as {@code List<DisclosureOptionItem>} payloads,
 * while error responses are validated as {@link ErrorResponse} payloads.
 */
public class DisclosureOptionsManagementApiImplTest {

    private AccountMetadataDAO metadataDAO;
    private ConnectionProvider connectionProvider;
    private Connection connection;

    /**
     * Initializes static dependencies for API tests.
     *
     * @throws Exception if mock setup or singleton reset fails
     */
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

    /**
     * Resets mock interactions before each test.
     *
     * @throws Exception if mock setup fails
     */
    @BeforeMethod
    public void setUp() throws Exception {
        Mockito.reset(metadataDAO, connectionProvider, connection);
        Mockito.when(connectionProvider.getConnection()).thenReturn(connection);
    }

    /**
     * Verifies bad request handling when an invalid disclosure option status is provided.
     */
    @Test
    public void testUpdateDisclosureOptionsBadRequestOnInvalidStatus() {
        List<DisclosureOptionItem> request = buildRequest("invalid");

        Response response = DisclosureOptionsManagementApiImpl.updateDisclosureOptions(request);

        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
        ErrorResponse body = (ErrorResponse) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertEquals(body.getErrorDescription(),
                "Invalid disclosure option status provided for acc-1 " +
                        "Allowed values: no-sharing, pre-approval");
    }

    /**
     * Verifies update returns OK with an empty response when the request list is empty.
     */
    @Test
    public void testUpdateDisclosureOptionsOkOnEmptyRequest() {
        Response response = DisclosureOptionsManagementApiImpl.updateDisclosureOptions(Collections.emptyList());

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        @SuppressWarnings("unchecked")
        List<DisclosureOptionItem> body = (List<DisclosureOptionItem>) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertTrue(body.isEmpty());
    }

    /**
     * Verifies update persists a new disclosure option and returns the processed payload.
     */
    @Test
    public void testUpdateDisclosureOptionsSuccess() throws Exception {
        List<DisclosureOptionItem> request = buildRequest("no-sharing");
        Mockito.when(metadataDAO.getBatchDisclosureOptions(connection, Collections.singletonList("acc-1")))
                .thenReturn(Collections.singletonMap("acc-1", "pre-approval"));

        Response response = DisclosureOptionsManagementApiImpl.updateDisclosureOptions(request);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        // Success payload is now a list of updated disclosure option items.
        @SuppressWarnings("unchecked")
        List<DisclosureOptionItem> body = (List<DisclosureOptionItem>) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertEquals(body.size(), 1);
        Assert.assertEquals(body.get(0).getAccountId(), "acc-1");
        Assert.assertEquals(body.get(0).getDisclosureOption(), "no-sharing");
        Mockito.verify(metadataDAO).updateBatchDisclosureOptions(connection,
                Collections.singletonMap("acc-1", "no-sharing"));
    }

    /**
     * Verifies update returns OK without persistence when none of the requested accounts exist.
     */
    @Test
    public void testUpdateDisclosureOptionsOkWhenNoAccountsExist() throws Exception {
        List<DisclosureOptionItem> request = buildRequest("pre-approval");
        Mockito.when(metadataDAO.getBatchDisclosureOptions(connection, Collections.singletonList("acc-1")))
                .thenReturn(Collections.emptyMap());

        Response response = DisclosureOptionsManagementApiImpl.updateDisclosureOptions(request);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        @SuppressWarnings("unchecked")
        List<DisclosureOptionItem> body = (List<DisclosureOptionItem>) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertTrue(body.isEmpty());
        Mockito.verify(metadataDAO, Mockito.never()).updateBatchDisclosureOptions(
                Mockito.any(Connection.class), Mockito.anyMap());
    }

    /**
     * Verifies update only persists disclosure options for accounts that exist in the database.
     */
    @Test
    public void testUpdateDisclosureOptionsOkWhenPartialAccountsExist() throws Exception {
        List<DisclosureOptionItem> request = Arrays.asList(
                buildRequestItem("acc-1", "pre-approval"),
                buildRequestItem("acc-2", "no-sharing"));
        Mockito.when(metadataDAO.getBatchDisclosureOptions(connection, Arrays.asList("acc-1", "acc-2")))
                .thenReturn(Collections.singletonMap("acc-1", "no-sharing"));

        Response response = DisclosureOptionsManagementApiImpl.updateDisclosureOptions(request);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        @SuppressWarnings("unchecked")
        List<DisclosureOptionItem> body = (List<DisclosureOptionItem>) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertEquals(body.size(), 1);
        Assert.assertEquals(body.get(0).getAccountId(), "acc-1");
        Assert.assertEquals(body.get(0).getDisclosureOption(), "pre-approval");
        Mockito.verify(metadataDAO).updateBatchDisclosureOptions(
                connection, Collections.singletonMap("acc-1", "pre-approval"));
    }

    /**
     * Verifies update returns an internal server error when persistence fails.
     */
    @Test
    public void testUpdateDisclosureOptionsServiceError() throws Exception {
        List<DisclosureOptionItem> request = buildRequest("pre-approval");
        Mockito.when(metadataDAO.getBatchDisclosureOptions(connection, Collections.singletonList("acc-1")))
                .thenReturn(Collections.singletonMap("acc-1", "no-sharing"));
        Mockito.doThrow(new AccountMetadataException("fail"))
                .when(metadataDAO)
                .updateBatchDisclosureOptions(connection, Collections.singletonMap("acc-1", "pre-approval"));

        Response response = DisclosureOptionsManagementApiImpl.updateDisclosureOptions(request);

        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        ErrorResponse body = (ErrorResponse) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertTrue(body.getErrorDescription().startsWith("Failed to update disclosure options:"));
    }

    /**
     * Verifies bad request handling when no account identifier is provided for lookup.
     */
    @Test
    public void testGetDisclosureOptionsBadRequestOnEmpty() {
        Response response = DisclosureOptionsManagementApiImpl.getDisclosureOptions("");

        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
        ErrorResponse body = (ErrorResponse) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertEquals(body.getErrorDescription(), "At least one accountId is required");
    }

    /**
     * Verifies bad request handling when the provided account identifier is blank.
     */
    @Test
    public void testGetDisclosureOptionsBadRequestOnBlankIds() {
        Response response = DisclosureOptionsManagementApiImpl.getDisclosureOptions("   ");

        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
        ErrorResponse body = (ErrorResponse) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertEquals(body.getErrorDescription(), "At least one accountId is required");
    }

    /**
     * Verifies bad request handling when the account list contains only separators.
     */
    @Test
    public void testGetDisclosureOptionsBadRequestOnOnlyCommas() {
        Response response = DisclosureOptionsManagementApiImpl.getDisclosureOptions(" , , ");

        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
        ErrorResponse body = (ErrorResponse) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertEquals(body.getErrorDescription(), "At least one valid accountId is required");
    }

    /**
     * Verifies disclosure options are returned for multiple account identifiers.
     */
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

    /**
     * Verifies disclosure option lookup trims whitespace around account identifiers.
     */
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

    /**
     * Verifies add creates disclosure options when all requested accounts are new.
     */
    @Test
    public void testAddDisclosureOptionsCreatedWhenAllNew() throws Exception {
        List<DisclosureOptionItem> request = buildRequest("no-sharing");
        Map<String, String> batchResult = new HashMap<>();

        Mockito.when(metadataDAO.getBatchDisclosureOptions(connection, Arrays.asList("acc-1")))
                .thenReturn(batchResult);

        Response response = DisclosureOptionsManagementApiImpl.addDisclosureOptions(request);

        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
        // Success payload is now a list of added disclosure option items.
        @SuppressWarnings("unchecked")
        List<DisclosureOptionItem> body = (List<DisclosureOptionItem>) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertEquals(body.size(), 1);
        Assert.assertEquals(body.get(0).getAccountId(), "acc-1");
        Assert.assertEquals(body.get(0).getDisclosureOption(), "no-sharing");
        Mockito.verify(metadataDAO).addBatchDisclosureOptions(connection,
                Collections.singletonMap("acc-1", "no-sharing"));
    }

    /**
     * Verifies add returns OK without persistence when the account already exists.
     */
    @Test
    public void testAddDisclosureOptionsOkWhenExisting() throws Exception {
        List<DisclosureOptionItem> request = buildRequest("no-sharing");
        Map<String, String> existingMap = Collections.singletonMap("acc-1", "pre-approval");

        Mockito.when(metadataDAO.getBatchDisclosureOptions(connection, Collections.singletonList("acc-1")))
                .thenReturn(existingMap);

        Response response = DisclosureOptionsManagementApiImpl.addDisclosureOptions(request);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        // Existing accounts are not added; implementation returns an empty list.
        @SuppressWarnings("unchecked")
        List<DisclosureOptionItem> body = (List<DisclosureOptionItem>) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertTrue(body.isEmpty());
    }

    /**
     * Verifies bad request handling when add receives an invalid disclosure option status.
     */
    @Test
    public void testAddDisclosureOptionsBadRequestOnInvalidStatus() {
        List<DisclosureOptionItem> request = buildRequest("invalid");

        Response response = DisclosureOptionsManagementApiImpl.addDisclosureOptions(request);

        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
        ErrorResponse body = (ErrorResponse) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertEquals(body.getErrorDescription(),
                "Invalid disclosure option status provided for acc-1 Allowed values: no-sharing, pre-approval");
    }

    /**
     * Verifies add returns an internal server error when database access fails.
     */
    @Test
    public void testAddDisclosureOptionsServiceError() throws Exception {
        List<DisclosureOptionItem> request = buildRequest("pre-approval");
        Mockito.when(metadataDAO.getBatchDisclosureOptions(connection, Collections.singletonList("acc-1")))
                .thenThrow(new AccountMetadataException("fail"));

        Response response = DisclosureOptionsManagementApiImpl.addDisclosureOptions(request);

        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        ErrorResponse body = (ErrorResponse) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertTrue(body.getErrorDescription().startsWith("Failed to add disclosure options:"));
    }

    /**
     * Verifies that getDisclosureOptions returns 500 error when service throws an exception.
     * Tests exception handling path in the get method.
     *
     * @throws Exception if mock setup fails
     */
    @Test
    public void testGetDisclosureOptionsServiceException() throws Exception {
        Mockito.when(metadataDAO.getBatchDisclosureOptions(connection, Arrays.asList("acc-100")))
                .thenThrow(new AccountMetadataException("database error"));

        Response response = DisclosureOptionsManagementApiImpl.getDisclosureOptions("acc-100");

        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        ErrorResponse body = (ErrorResponse) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertTrue(body.getErrorDescription().startsWith("Failed to retrieve disclosure options:"));
    }

    /**
     * Verifies that updateDisclosureOptions validates null status in request items.
     * Tests null status branch in isValidDOMSStatus method.
     */
    @Test
    public void testUpdateDisclosureOptionsNullStatus() {
        DisclosureOptionItem item = new DisclosureOptionItem();
        item.setAccountId("acc-null");
        item.setDisclosureOption(null);
        List<DisclosureOptionItem> request = Collections.singletonList(item);

        Response response = DisclosureOptionsManagementApiImpl.updateDisclosureOptions(request);

        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
        ErrorResponse body = (ErrorResponse) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertTrue(body.getErrorDescription().contains("Invalid disclosure option status"));
    }

    /**
     * Verifies that addDisclosureOptions validates null status in request items.
     * Tests null status branch in isValidDOMSStatus method.
     */
    @Test
    public void testAddDisclosureOptionsNullStatus() {
        DisclosureOptionItem item = new DisclosureOptionItem();
        item.setAccountId("acc-null-add");
        item.setDisclosureOption(null);
        List<DisclosureOptionItem> request = Collections.singletonList(item);

        Response response = DisclosureOptionsManagementApiImpl.addDisclosureOptions(request);

        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
        ErrorResponse body = (ErrorResponse) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertTrue(body.getErrorDescription().contains("Invalid disclosure option status"));
    }

    /**
     * Verifies that addDisclosureOptions returns 200 OK when all accounts already exist.
     * Tests the response code differentiation when no new accounts are added.
     *
     * @throws Exception if mock setup fails
     */
    @Test
    public void testAddDisclosureOptionsAllAccountsExist() throws Exception {
        List<DisclosureOptionItem> request = buildRequest("pre-approval");
        // Mock: account already exists (no new accounts to add)
        Mockito.when(metadataDAO.getBatchDisclosureOptions(connection, Collections.singletonList("acc-1")))
                .thenReturn(Collections.singletonMap("acc-1", "no-sharing"));

        Response response = DisclosureOptionsManagementApiImpl.addDisclosureOptions(request);

        // Should return 200 OK (not 201 CREATED) because no new accounts were added
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        List<DisclosureOptionItem> body = (List<DisclosureOptionItem>) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertEquals(body.size(), 0); // No items added
    }

    /**
     * Verifies that updateDisclosureOptions returns 200 OK even when no matching accounts found.
     * Tests response when update finds no matching accounts in database.
     *
     * @throws Exception if mock setup fails
     */
    @Test
    public void testUpdateDisclosureOptionsNoMatchingAccounts() throws Exception {
        List<DisclosureOptionItem> request = buildRequest("no-sharing");
        // Mock: no accounts exist in database
        Mockito.when(metadataDAO.getBatchDisclosureOptions(connection, Collections.singletonList("acc-1")))
                .thenReturn(new HashMap<>());

        Response response = DisclosureOptionsManagementApiImpl.updateDisclosureOptions(request);

        // Should return 200 OK with empty list (no accounts matched for update)
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        List<DisclosureOptionItem> body = (List<DisclosureOptionItem>) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertEquals(body.size(), 0); // No items updated
    }

    /**
     * Verifies that getDisclosureOptions handles multiple comma-separated account IDs.
     * Tests the parsing and retrieval of multiple accounts.
     *
     * @throws Exception if mock setup fails
     */
    @Test
    public void testGetDisclosureOptionsMultipleAccounts() throws Exception {
        Map<String, String> mockResult = new HashMap<>();
        mockResult.put("acc-201", "pre-approval");
        mockResult.put("acc-202", "no-sharing");

        Mockito.when(metadataDAO.getBatchDisclosureOptions(connection, Arrays.asList("acc-201", "acc-202")))
                .thenReturn(mockResult);

        Response response = DisclosureOptionsManagementApiImpl.getDisclosureOptions("acc-201,acc-202");

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        List<DisclosureOptionItem> body = (List<DisclosureOptionItem>) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertEquals(body.size(), 2);
    }

    /**
     * Verifies that getDisclosureOptions returns empty list when no matching accounts found.
     * Tests the successful response path with zero results.
     *
     * @throws Exception if mock setup fails
     */
    @Test
    public void testGetDisclosureOptionsNoResults() throws Exception {
        Mockito.when(metadataDAO.getBatchDisclosureOptions(connection, Collections.singletonList("acc-nonexistent")))
                .thenReturn(new HashMap<>());

        Response response = DisclosureOptionsManagementApiImpl.getDisclosureOptions("acc-nonexistent");

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        List<DisclosureOptionItem> body = (List<DisclosureOptionItem>) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertEquals(body.size(), 0);
    }

    /**
     * Builds a one-item request payload for convenience.
     *
     * @param status disclosure option status
     * @return single-item request list
     */
    private List<DisclosureOptionItem> buildRequest(String status) {
        return Collections.singletonList(buildRequestItem("acc-1", status));
    }

    /**
     * Builds a disclosure option item.
     *
     * @param accountId account identifier
     * @param status disclosure option status
     * @return disclosure option item
     */
    private DisclosureOptionItem buildRequestItem(String accountId, String status) {
        DisclosureOptionItem item = new DisclosureOptionItem();
        item.setAccountId(accountId);
        item.setDisclosureOption(status);
        return item;
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

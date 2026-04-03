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

import org.apache.commons.lang3.tuple.Pair;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.openbanking.consumerdatastandards.account.metadata.exceptions.AccountMetadataException;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.ErrorResponse;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.SecondaryAccountInstructionItem;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.core.AccountMetadataServiceImpl;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.dao.AccountMetadataDAO;
import org.wso2.openbanking.consumerdatastandards.account.metadata.utils.connection.provider.ConnectionProvider;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.Response;

/**
 * Unit tests for {@link SecondaryAccountsManagementApiImpl}.
 */
public class SecondaryAccountsManagementApiImplTest {

    private AccountMetadataDAO metadataDAO;
    private ConnectionProvider connectionProvider;
    private Connection connection;

    /**
     * Initializes static service dependencies for API tests.
     *
     * @throws Exception if class loading or singleton reset fails
     */
    @BeforeClass
    public void setUpClass() throws Exception {
        metadataDAO = Mockito.mock(AccountMetadataDAO.class);
        connectionProvider = Mockito.mock(ConnectionProvider.class);
        connection = Mockito.mock(Connection.class);
        Mockito.when(connectionProvider.getConnection()).thenReturn(connection);

        resetSingleton();
        AccountMetadataServiceImpl.getInstance(metadataDAO, connectionProvider);
        Class.forName(SecondaryAccountsManagementApiImpl.class.getName(), true,
                SecondaryAccountsManagementApiImpl.class.getClassLoader());
    }

    /**
     * Resets mocks before each test.
     *
     * @throws Exception if mock setup fails
     */
    @BeforeMethod
    public void setUp() throws Exception {
        Mockito.reset(metadataDAO, connectionProvider, connection);
        Mockito.when(connectionProvider.getConnection()).thenReturn(connection);
    }

    /**
     * Verifies create response when all secondary account instructions are new.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testAddSecondaryAccountInstructionsCreatedWhenAllNew() throws Exception {
        List<SecondaryAccountInstructionItem> request = Collections.singletonList(
                buildItem("acc-1", "user-1", true, "active"));
        Mockito.when(metadataDAO.getBatchSecondaryAccountInstructions(Mockito.eq(connection), Mockito.anyList()))
                .thenReturn(Collections.emptyList());

        Response response = SecondaryAccountsManagementApiImpl.addSecondaryAccountInstructions(request);

        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
        @SuppressWarnings("unchecked")
        List<SecondaryAccountInstructionItem> body = (List<SecondaryAccountInstructionItem>) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertEquals(body.size(), 1);
        Assert.assertEquals(body.get(0).getAccountId(), "acc-1");
        Assert.assertEquals(body.get(0).getSecondaryUserId(), "user-1");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<SecondaryAccountInstructionItem>> captor =
                (ArgumentCaptor<List<SecondaryAccountInstructionItem>>) (ArgumentCaptor<?>)
                        ArgumentCaptor.forClass(List.class);
        Mockito.verify(metadataDAO).addBatchSecondaryAccountInstructions(Mockito.eq(connection), captor.capture());
        Assert.assertEquals(captor.getValue().size(), 1);
        Assert.assertEquals(captor.getValue().get(0).getAccountId(), "acc-1");
        Assert.assertEquals(captor.getValue().get(0).getSecondaryUserId(), "user-1");
    }

    /**
     * Verifies ok response when all requested secondary instruction records already exist.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testAddSecondaryAccountInstructionsOkWhenExisting() throws Exception {
        SecondaryAccountInstructionItem existing = buildItem(
                "acc-1", "user-1", true, "active");
        List<SecondaryAccountInstructionItem> request = Collections.singletonList(existing);
        Mockito.when(metadataDAO.getBatchSecondaryAccountInstructions(connection,
                        buildAccountUserPairs("acc-1", "user-1")))
                .thenReturn(Collections.singletonList(existing));

        Response response = SecondaryAccountsManagementApiImpl.addSecondaryAccountInstructions(request);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        @SuppressWarnings("unchecked")
        List<SecondaryAccountInstructionItem> body = (List<SecondaryAccountInstructionItem>) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertEquals(body.size(), 1);
        Assert.assertEquals(body.get(0).getAccountId(), "acc-1");
        Assert.assertEquals(body.get(0).getSecondaryUserId(), "user-1");
        Mockito.verify(metadataDAO, Mockito.never())
                .addBatchSecondaryAccountInstructions(Mockito.any(Connection.class), Mockito.anyList());
        Mockito.verify(metadataDAO).updateBatchSecondaryAccountInstructions(Mockito.eq(connection), Mockito.anyList());
    }

    /**
     * Verifies partial add behavior when only some secondary instruction records exist.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testAddSecondaryAccountInstructionsOkWhenPartialExisting() throws Exception {
        SecondaryAccountInstructionItem existing = buildItem(
                "acc-10", "user-10", true, "active");
        SecondaryAccountInstructionItem newItem = buildItem(
                "acc-11", "user-11", false, "inactive");
        List<SecondaryAccountInstructionItem> request = Arrays.asList(existing, newItem);
        Mockito.when(metadataDAO.getBatchSecondaryAccountInstructions(Mockito.eq(connection), Mockito.anyList()))
                .thenReturn(Collections.singletonList(existing));

        Response response = SecondaryAccountsManagementApiImpl.addSecondaryAccountInstructions(request);

        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
        @SuppressWarnings("unchecked")
        List<SecondaryAccountInstructionItem> body = (List<SecondaryAccountInstructionItem>) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertEquals(body.size(), 2);
        Assert.assertTrue(body.stream().anyMatch(item -> "acc-11".equals(item.getAccountId())
                && "user-11".equals(item.getSecondaryUserId())));
        Assert.assertTrue(body.stream().anyMatch(item -> "acc-10".equals(item.getAccountId())
                && "user-10".equals(item.getSecondaryUserId())));
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<SecondaryAccountInstructionItem>> addCaptor =
                (ArgumentCaptor<List<SecondaryAccountInstructionItem>>) (ArgumentCaptor<?>)
                        ArgumentCaptor.forClass(List.class);
        Mockito.verify(metadataDAO).addBatchSecondaryAccountInstructions(Mockito.eq(connection), addCaptor.capture());
        Assert.assertEquals(addCaptor.getValue().size(), 1);
        Assert.assertEquals(addCaptor.getValue().get(0).getAccountId(), "acc-11");
        Assert.assertEquals(addCaptor.getValue().get(0).getSecondaryUserId(), "user-11");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<SecondaryAccountInstructionItem>> updateCaptor =
                (ArgumentCaptor<List<SecondaryAccountInstructionItem>>) (ArgumentCaptor<?>)
                        ArgumentCaptor.forClass(List.class);
        Mockito.verify(metadataDAO)
                .updateBatchSecondaryAccountInstructions(Mockito.eq(connection), updateCaptor.capture());
        Assert.assertEquals(updateCaptor.getValue().size(), 1);
        Assert.assertEquals(updateCaptor.getValue().get(0).getAccountId(), "acc-10");
        Assert.assertEquals(updateCaptor.getValue().get(0).getSecondaryUserId(), "user-10");
    }

    /**
     * Verifies internal server error response when add operation fails.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testAddSecondaryAccountInstructionsServiceError() throws Exception {
        List<SecondaryAccountInstructionItem> request = Collections.singletonList(
                buildItem("acc-12", "user-12", true, "active"));
        Mockito.when(metadataDAO.getBatchSecondaryAccountInstructions(connection,
                        buildAccountUserPairs("acc-12", "user-12")))
                .thenThrow(new AccountMetadataException("fail"));

        Response response = SecondaryAccountsManagementApiImpl.addSecondaryAccountInstructions(request);

        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        ErrorResponse body = (ErrorResponse) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertTrue(body.getErrorDescription().startsWith("Failed to add secondary account instructions:"));
    }

    /**
     * Verifies successful update for existing secondary instruction records.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testUpdateSecondaryAccountInstructionsSuccess() throws Exception {
        SecondaryAccountInstructionItem existing = buildItem(
                "acc-2", "user-2", false, "inactive");
        List<SecondaryAccountInstructionItem> request = Collections.singletonList(existing);
        Mockito.when(metadataDAO.getBatchSecondaryAccountInstructions(Mockito.eq(connection), Mockito.anyList()))
                .thenReturn(Collections.singletonList(existing));

        Response response = SecondaryAccountsManagementApiImpl.updateSecondaryAccountInstructions(request);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        @SuppressWarnings("unchecked")
        List<SecondaryAccountInstructionItem> body = (List<SecondaryAccountInstructionItem>) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertEquals(body.size(), 1);
        Assert.assertEquals(body.get(0).getAccountId(), "acc-2");
        Mockito.verify(metadataDAO).updateBatchSecondaryAccountInstructions(Mockito.eq(connection), Mockito.anyList());
    }

    /**
     * Verifies update response when none of the requested records exist.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testUpdateSecondaryAccountInstructionsOkWhenNoAccountsExist() throws Exception {
        List<SecondaryAccountInstructionItem> request = Collections.singletonList(
                buildItem("acc-3", "user-3", true, "active"));
        Mockito.when(metadataDAO.getBatchSecondaryAccountInstructions(Mockito.eq(connection), Mockito.anyList()))
                .thenReturn(Collections.emptyList());

        Response response = SecondaryAccountsManagementApiImpl.updateSecondaryAccountInstructions(request);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        @SuppressWarnings("unchecked")
        List<SecondaryAccountInstructionItem> body = (List<SecondaryAccountInstructionItem>) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertEquals(body.size(), 0);
        Mockito.verify(metadataDAO, Mockito.never())
                .updateBatchSecondaryAccountInstructions(Mockito.any(Connection.class), Mockito.anyList());
    }

    /**
     * Verifies partial update behavior when only a subset of requested records exist.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testUpdateSecondaryAccountInstructionsOkWhenPartialExists() throws Exception {
        SecondaryAccountInstructionItem existing = buildItem(
                "acc-13", "user-13", true, "active");
        SecondaryAccountInstructionItem missing = buildItem(
                "acc-14", "user-14", false, "inactive");
        List<SecondaryAccountInstructionItem> request = Arrays.asList(existing, missing);
        Mockito.when(metadataDAO.getBatchSecondaryAccountInstructions(Mockito.eq(connection), Mockito.anyList()))
                .thenReturn(Collections.singletonList(existing));

        Response response = SecondaryAccountsManagementApiImpl.updateSecondaryAccountInstructions(request);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        @SuppressWarnings("unchecked")
        List<SecondaryAccountInstructionItem> body = (List<SecondaryAccountInstructionItem>) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertEquals(body.size(), 1);
        Assert.assertEquals(body.get(0).getAccountId(), "acc-13");
        Assert.assertEquals(body.get(0).getSecondaryUserId(), "user-13");
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<SecondaryAccountInstructionItem>> updateCaptor =
                (ArgumentCaptor<List<SecondaryAccountInstructionItem>>) (ArgumentCaptor<?>)
                        ArgumentCaptor.forClass(List.class);
        Mockito.verify(metadataDAO).updateBatchSecondaryAccountInstructions(Mockito.eq(connection),
                updateCaptor.capture());
        Assert.assertEquals(updateCaptor.getValue().size(), 1);
        Assert.assertEquals(updateCaptor.getValue().get(0).getAccountId(), "acc-13");
        Assert.assertEquals(updateCaptor.getValue().get(0).getSecondaryUserId(), "user-13");
    }

    /**
     * Verifies internal server error response when update operation fails.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testUpdateSecondaryAccountInstructionsServiceError() throws Exception {
        List<SecondaryAccountInstructionItem> request = Collections.singletonList(
                buildItem("acc-15", "user-15", true, "active"));
        Mockito.when(metadataDAO.getBatchSecondaryAccountInstructions(connection,
                        buildAccountUserPairs("acc-15", "user-15")))
                .thenThrow(new AccountMetadataException("fail"));

        Response response = SecondaryAccountsManagementApiImpl.updateSecondaryAccountInstructions(request);

        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        ErrorResponse body = (ErrorResponse) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertTrue(body.getErrorDescription().startsWith("Failed to update secondary account instructions:"));
    }

    /**
     * Verifies bad request response when account and user ids are missing.
     */
    @Test
    public void testGetSecondaryAccountInstructionsBadRequestOnEmpty() {
        Response response = SecondaryAccountsManagementApiImpl
                .getSecondaryAccountInstructions("", "");

        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
        ErrorResponse body = (ErrorResponse) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertEquals(body.getErrorDescription(), "At least one accountId and userId are required");
    }

    /**
     * Verifies bad request response when account and user ids are blank.
     */
    @Test
    public void testGetSecondaryAccountInstructionsBadRequestOnBlank() {
        Response response = SecondaryAccountsManagementApiImpl
                .getSecondaryAccountInstructions("   ", "   ");

        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
        ErrorResponse body = (ErrorResponse) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertEquals(body.getErrorDescription(), "At least one accountId and userId are required");
    }

    /**
     * Verifies bad request response when user ids are missing.
     */
    @Test
    public void testGetSecondaryAccountInstructionsBadRequestOnMissingUserId() {
        Response response = SecondaryAccountsManagementApiImpl.getSecondaryAccountInstructions(
                "acc-1,acc-2", " ");

        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
        ErrorResponse body = (ErrorResponse) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertEquals(body.getErrorDescription(), "At least one accountId and userId are required");
    }

    /**
     * Verifies successful retrieval of secondary account instructions.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testGetSecondaryAccountInstructionsSuccess() throws Exception {
        List<SecondaryAccountInstructionItem> batchResult = Arrays.asList(
                buildItem("acc-4", "user-4", true, "active"),
                buildItem("acc-5", "user-5", false, "inactive"));

        Mockito.when(metadataDAO.getBatchSecondaryAccountInstructions(Mockito.eq(connection), Mockito.anyList()))
                .thenReturn(batchResult);

        Response response = SecondaryAccountsManagementApiImpl
                .getSecondaryAccountInstructions("acc-4,acc-5", "user-4");

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        @SuppressWarnings("unchecked")
        List<SecondaryAccountInstructionItem> body = (List<SecondaryAccountInstructionItem>) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertEquals(body.size(), 2);
        Assert.assertEquals(body.get(0).getAccountId(), "acc-4");
    }

    /**
     * Verifies internal server error response when retrieval fails.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testGetSecondaryAccountInstructionsServiceError() throws Exception {
        Mockito.when(metadataDAO.getBatchSecondaryAccountInstructions(
                Mockito.eq(connection), Mockito.anyList()))
                .thenThrow(new AccountMetadataException("fail"));

        Response response = SecondaryAccountsManagementApiImpl.getSecondaryAccountInstructions(
                "acc-6", "user-6");

        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        ErrorResponse body = (ErrorResponse) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertTrue(body.getErrorDescription().startsWith("Failed to retrieve secondary account instructions:"));
    }

    /**
     * Verifies internal server error response when accountId or secondaryUserId is blank in the add request.
     */
    @Test
    public void testAddSecondaryAccountInstructionsErrorOnBlankIds() {
        List<SecondaryAccountInstructionItem> request = Collections.singletonList(
                buildItem("   ", "   ", true, "active"));

        Response response = SecondaryAccountsManagementApiImpl.addSecondaryAccountInstructions(request);

        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        ErrorResponse body = (ErrorResponse) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertTrue(body.getErrorDescription().startsWith("Failed to add secondary account instructions:"));
    }

    /**
     * Verifies internal server error response when duplicate account-user pairs are present in the add request.
     */
    @Test
    public void testAddSecondaryAccountInstructionsErrorOnDuplicateItems() {
        SecondaryAccountInstructionItem item1 = buildItem("acc-dup", "user-dup", true, "active");
        SecondaryAccountInstructionItem item2 = buildItem("acc-dup", "user-dup", false, "inactive");
        List<SecondaryAccountInstructionItem> request = Arrays.asList(item1, item2);

        Response response = SecondaryAccountsManagementApiImpl.addSecondaryAccountInstructions(request);

        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        ErrorResponse body = (ErrorResponse) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertTrue(body.getErrorDescription().startsWith("Failed to add secondary account instructions:"));
    }

    /**
     * Builds a secondary instruction item.
     *
     * @param accountId account id
     * @param userId secondary user id
     * @param otherAccountsAvailable whether other accounts are available
     * @param status instruction status
     * @return populated test item
     */
    private SecondaryAccountInstructionItem buildItem(String accountId, String userId, boolean otherAccountsAvailable,
            String status) {
        SecondaryAccountInstructionItem item = new SecondaryAccountInstructionItem();
        item.setAccountId(accountId);
        item.setSecondaryUserId(userId);
        item.setOtherAccountsAvailability(otherAccountsAvailable);
        item.setSecondaryAccountInstructionStatus(
                SecondaryAccountInstructionItem.SecondaryAccountInstructionStatusEnum.fromValue(status));
        return item;
    }

        private List<Pair<String, String>> buildAccountUserPairs(String accountId, String userId) {
                return Collections.singletonList(Pair.of(accountId, userId));
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

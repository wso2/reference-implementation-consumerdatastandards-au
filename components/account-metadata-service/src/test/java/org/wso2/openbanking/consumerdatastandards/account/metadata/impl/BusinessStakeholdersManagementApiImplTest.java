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
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.BusinessStakeholderDeleteItem;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.BusinessStakeholderItem;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.BusinessStakeholderPermissionItem;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.BusinessStakeholderRepresentative;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.ErrorResponse;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.core.AccountMetadataServiceImpl;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.dao.AccountMetadataDAO;
import org.wso2.openbanking.consumerdatastandards.account.metadata.utils.connection.provider.ConnectionProvider;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.Response;

/**
 * Unit tests for {@link BusinessStakeholdersManagementApiImpl}.
 */
public class BusinessStakeholdersManagementApiImplTest {

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
        Class.forName(BusinessStakeholdersManagementApiImpl.class.getName(), true,
                BusinessStakeholdersManagementApiImpl.class.getClassLoader());
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
     * Verifies bad request when add request contains null item.
     */
    @Test
    public void testAddBusinessStakeholdersSendBadRequestOnNullRequestItem() {
        Response response = BusinessStakeholdersManagementApiImpl.addBusinessStakeholders(
            Collections.singletonList(null));

        assertSendBadRequest(response, "Request contains null business stakeholder item");
    }

    /**
     * Verifies bad request when account id is missing.
     */
    @Test
    public void testAddBusinessStakeholdersSendBadRequestOnMissingAccountId() {
        BusinessStakeholderItem item = buildUpsertItem(" ",
            buildRepresentative("user-1", "AUTHORIZE"));

        Response response = BusinessStakeholdersManagementApiImpl.addBusinessStakeholders(
            Collections.singletonList(item));

        assertSendBadRequest(response, "accountID is required");
    }

    /**
     * Verifies add returns OK with empty list when request has no representatives.
     */
    @Test
    public void testAddBusinessStakeholdersOkOnEmptyRepresentatives() throws AccountMetadataException {
        BusinessStakeholderItem item = buildUpsertItem("acc-1");

        Response response = BusinessStakeholdersManagementApiImpl.addBusinessStakeholders(
            Collections.singletonList(item));

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        Assert.assertEquals(asStringList(response).size(), 0);
        Mockito.verify(metadataDAO, Mockito.never())
            .getBatchBusinessStakeholderPermissions(Mockito.any(Connection.class), Mockito.anyList());
        Mockito.verify(metadataDAO, Mockito.never())
            .addBatchBusinessStakeholderPermissions(Mockito.any(Connection.class), Mockito.anyList());
    }

    /**
     * Verifies create response when all requested business stakeholder records are new.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testAddBusinessStakeholdersCreatedWhenAllNew() throws Exception {
        BusinessStakeholderItem item = buildUpsertItem("acc-1",
            buildRepresentative("user-1", "AUTHORIZE"),
            buildRepresentative("user-2", "VIEW"));

        Mockito.when(metadataDAO.getBatchBusinessStakeholderPermissions(Mockito.eq(connection), Mockito.anyList()))
            .thenReturn(Collections.emptyList());

        Response response = BusinessStakeholdersManagementApiImpl.addBusinessStakeholders(
            Collections.singletonList(item));

        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
        List<BusinessStakeholderItem> body = asUpsertItemList(response);
        Assert.assertEquals(body.size(), 1);
        Assert.assertEquals(body.get(0).getAccountID(), "acc-1");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<BusinessStakeholderPermissionItem>> captor =
            (ArgumentCaptor<List<BusinessStakeholderPermissionItem>>) (ArgumentCaptor<?>)
                ArgumentCaptor.forClass(List.class);
        Mockito.verify(metadataDAO).addBatchBusinessStakeholderPermissions(
                Mockito.eq(connection), captor.capture());
        Assert.assertEquals(captor.getValue().size(), 2);
    }

    /**
     * Verifies add persists account owners with VIEW permission.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testAddBusinessStakeholdersCreatedWithAccountOwners() throws Exception {
        BusinessStakeholderItem item = buildUpsertItem("acc-1", Arrays.asList("owner-1", "owner-2"),
            buildRepresentative("user-1", "AUTHORIZE"));

        Mockito.when(metadataDAO.getBatchBusinessStakeholderPermissions(Mockito.eq(connection), Mockito.anyList()))
            .thenReturn(Collections.emptyList());

        Response response = BusinessStakeholdersManagementApiImpl.addBusinessStakeholders(
            Collections.singletonList(item));

        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<BusinessStakeholderPermissionItem>> captor =
            (ArgumentCaptor<List<BusinessStakeholderPermissionItem>>) (ArgumentCaptor<?>)
                ArgumentCaptor.forClass(List.class);
        Mockito.verify(metadataDAO).addBatchBusinessStakeholderPermissions(
                Mockito.eq(connection), captor.capture());
        Assert.assertEquals(captor.getValue().size(), 3);
        Assert.assertTrue(captor.getValue().stream().anyMatch(itemToAdd ->
            "owner-1".equals(itemToAdd.getUserId()) && "VIEW".equals(itemToAdd.getPermission().value())));
        Assert.assertTrue(captor.getValue().stream().anyMatch(itemToAdd ->
            "owner-2".equals(itemToAdd.getUserId()) && "VIEW".equals(itemToAdd.getPermission().value())));
        Assert.assertTrue(captor.getValue().stream().anyMatch(itemToAdd ->
            "user-1".equals(itemToAdd.getUserId()) && "AUTHORIZE".equals(itemToAdd.getPermission().value())));
    }

    /**
     * Verifies that duplicate account-user pairs in the add payload return a bad request.
     */
    @Test
    public void testAddBusinessStakeholdersDeduplicatesAccountUserPairs() {
        BusinessStakeholderItem item = buildUpsertItem("acc-1",
            buildRepresentative("user-1", "AUTHORIZE"),
            buildRepresentative("user-1", "VIEW"));

        Response response = BusinessStakeholdersManagementApiImpl.addBusinessStakeholders(
            Collections.singletonList(item));

        assertSendBadRequest(response, "Duplicate entry for accountID acc-1 and user user-1");
    }

    /**
     * Verifies ok response when all requested add records already exist.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testAddBusinessStakeholdersOkWhenAllExisting() throws Exception {
        BusinessStakeholderPermissionItem existing = buildItem("acc-1", "user-1", "AUTHORIZE");
        BusinessStakeholderItem requestItem = buildUpsertItem("acc-1",
            buildRepresentative("user-1", "AUTHORIZE"));

        Mockito.when(metadataDAO.getBatchBusinessStakeholderPermissions(Mockito.eq(connection), Mockito.anyList()))
            .thenReturn(Collections.singletonList(existing));

        Response response = BusinessStakeholdersManagementApiImpl.addBusinessStakeholders(
            Collections.singletonList(requestItem));

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        Assert.assertEquals(asStringList(response).size(), 0);
        Mockito.verify(metadataDAO, Mockito.never())
            .addBatchBusinessStakeholderPermissions(Mockito.any(Connection.class), Mockito.anyList());
    }

    /**
     * Verifies partial add behavior when some records already exist.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testAddBusinessStakeholdersCreatedWhenPartialExisting() throws Exception {
        BusinessStakeholderPermissionItem existing = buildItem("acc-1", "user-1", "AUTHORIZE");
        BusinessStakeholderItem requestItem = buildUpsertItem("acc-1",
            buildRepresentative("user-1", "AUTHORIZE"),
            buildRepresentative("user-2", "VIEW"));

        Mockito.when(metadataDAO.getBatchBusinessStakeholderPermissions(Mockito.eq(connection), Mockito.anyList()))
            .thenReturn(Collections.singletonList(existing));

        Response response = BusinessStakeholdersManagementApiImpl.addBusinessStakeholders(
            Collections.singletonList(requestItem));

        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
        List<BusinessStakeholderItem> body = asUpsertItemList(response);
        Assert.assertEquals(body.size(), 1);
        Assert.assertEquals(body.get(0).getAccountID(), "acc-1");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<BusinessStakeholderPermissionItem>> addCaptor =
            (ArgumentCaptor<List<BusinessStakeholderPermissionItem>>) (ArgumentCaptor<?>)
                ArgumentCaptor.forClass(List.class);
        Mockito.verify(metadataDAO).addBatchBusinessStakeholderPermissions(Mockito.eq(connection), addCaptor.capture());
        Assert.assertEquals(addCaptor.getValue().size(), 1);
        Assert.assertEquals(addCaptor.getValue().get(0).getUserId(), "user-2");
    }

    /**
     * Verifies internal server error when add retrieval fails.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testAddBusinessStakeholdersServiceErrorOnGetBatch() throws Exception {
        BusinessStakeholderItem item = buildUpsertItem("acc-1",
            buildRepresentative("user-1", "AUTHORIZE"));

        Mockito.when(metadataDAO.getBatchBusinessStakeholderPermissions(Mockito.eq(connection), Mockito.anyList()))
            .thenThrow(new AccountMetadataException("fail"));

        Response response = BusinessStakeholdersManagementApiImpl.addBusinessStakeholders(
            Collections.singletonList(item));

        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        ErrorResponse body = (ErrorResponse) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertTrue(body.getErrorDescription().startsWith("Failed to add business stakeholder records:"));
    }

    /**
     * Verifies internal server error when add persistence fails.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testAddBusinessStakeholdersServiceErrorOnAddBatch() throws Exception {
        BusinessStakeholderItem item = buildUpsertItem("acc-1",
            buildRepresentative("user-1", "AUTHORIZE"));

        Mockito.when(metadataDAO.getBatchBusinessStakeholderPermissions(Mockito.eq(connection), Mockito.anyList()))
            .thenReturn(Collections.emptyList());
        Mockito.doThrow(new AccountMetadataException("fail"))
            .when(metadataDAO)
            .addBatchBusinessStakeholderPermissions(Mockito.eq(connection), Mockito.anyList());

        Response response = BusinessStakeholdersManagementApiImpl.addBusinessStakeholders(
            Collections.singletonList(item));

        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        ErrorResponse body = (ErrorResponse) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertTrue(body.getErrorDescription().startsWith("Failed to add business stakeholder records:"));
    }

    /**
     * Verifies bad request response when account and user ids are empty.
     */
    @Test
    public void testGetBusinessStakeholdersSendBadRequestOnEmpty() {
        Response response = BusinessStakeholdersManagementApiImpl.getBusinessStakeholders("", "");
        assertSendBadRequest(response, "At least one accountId and userId are required");
    }

    /**
     * Verifies bad request response when account and user ids are blank.
     */
    @Test
    public void testGetBusinessStakeholdersSendBadRequestOnBlank() {
        Response response = BusinessStakeholdersManagementApiImpl.getBusinessStakeholders("   ", "   ");
        assertSendBadRequest(response, "At least one accountId and userId are required");
    }

    /**
     * Verifies bad request response when account ids parse to an empty set.
     */
    @Test
    public void testGetBusinessStakeholdersSendBadRequestOnOnlyCommas() {
        Response response = BusinessStakeholdersManagementApiImpl.getBusinessStakeholders(" , , ", "user-1");
        assertSendBadRequest(response, "At least one accountId and userId are required");
    }

    /**
     * Verifies bad request response when user id is missing.
     */
    @Test
    public void testGetBusinessStakeholdersSendBadRequestOnMissingUserId() {
        Response response = BusinessStakeholdersManagementApiImpl
                .getBusinessStakeholders("acc-1,acc-2", " ");
        assertSendBadRequest(response, "At least one accountId and userId are required");
    }

    /**
     * Verifies successful retrieval of business stakeholder permissions.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testGetBusinessStakeholdersSuccess() throws Exception {
        List<BusinessStakeholderPermissionItem> batchResult = Arrays.asList(
                buildItem("acc-1", "user-1", "AUTHORIZE"),
                buildItem("acc-2", "user-1", "VIEW"));

        Mockito.when(metadataDAO.getBatchBusinessStakeholderPermissions(Mockito.eq(connection), Mockito.anyList()))
                .thenReturn(batchResult);

        Response response = BusinessStakeholdersManagementApiImpl
                .getBusinessStakeholders("acc-1,acc-2", "user-1");

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        @SuppressWarnings("unchecked")
        List<BusinessStakeholderPermissionItem> body = (List<BusinessStakeholderPermissionItem>) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertEquals(body.size(), 2);
        Assert.assertEquals(body.get(0).getAccountId(), "acc-1");
        Assert.assertEquals(body.get(0).getUserId(), "user-1");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Pair<String, String>>> captor =
                (ArgumentCaptor<List<Pair<String, String>>>) (ArgumentCaptor<?>)
                        ArgumentCaptor.forClass(List.class);
        Mockito.verify(metadataDAO).getBatchBusinessStakeholderPermissions(Mockito.eq(connection), captor.capture());
        Assert.assertEquals(captor.getValue().size(), 2);
        Assert.assertEquals(captor.getValue().get(0).getLeft(), "acc-1");
        Assert.assertEquals(captor.getValue().get(0).getRight(), "user-1");
        Assert.assertEquals(captor.getValue().get(1).getLeft(), "acc-2");
        Assert.assertEquals(captor.getValue().get(1).getRight(), "user-1");
    }

    /**
     * Verifies account ids are trimmed and blanks are ignored for get flow.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testGetBusinessStakeholdersTrimsAndFiltersAccountIds() throws Exception {
        Mockito.when(metadataDAO.getBatchBusinessStakeholderPermissions(Mockito.eq(connection), Mockito.anyList()))
            .thenReturn(Collections.emptyList());

        Response response = BusinessStakeholdersManagementApiImpl
            .getBusinessStakeholders(" acc-1 , , acc-2 ", " user-1 ");

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Pair<String, String>>> captor =
            (ArgumentCaptor<List<Pair<String, String>>>) (ArgumentCaptor<?>)
                ArgumentCaptor.forClass(List.class);
        Mockito.verify(metadataDAO).getBatchBusinessStakeholderPermissions(Mockito.eq(connection), captor.capture());
        Assert.assertEquals(captor.getValue().size(), 2);
        Assert.assertEquals(captor.getValue().get(0).getLeft(), "acc-1");
        Assert.assertEquals(captor.getValue().get(1).getLeft(), "acc-2");
        Assert.assertEquals(captor.getValue().get(0).getRight(), "user-1");
        Assert.assertEquals(captor.getValue().get(1).getRight(), "user-1");
        }

    /**
     * Verifies internal server error response when retrieval fails.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testGetBusinessStakeholdersServiceError() throws Exception {
        Mockito.when(metadataDAO.getBatchBusinessStakeholderPermissions(
                Mockito.eq(connection), Mockito.anyList()))
                .thenThrow(new AccountMetadataException("fail"));

        Response response = BusinessStakeholdersManagementApiImpl.getBusinessStakeholders(
                "acc-1", "user-1");

        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        ErrorResponse body = (ErrorResponse) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertTrue(body.getErrorDescription().startsWith("Failed to retrieve business stakeholder " +
                "permissions:"));
    }

    /**
     * Verifies bad request when update payload has invalid representative data.
     */
    @Test
    public void testUpdateBusinessStakeholdersSendBadRequestOnMissingRepresentativeName() {
        BusinessStakeholderItem item = buildUpsertItem("acc-1",
            buildRepresentative(" ", "AUTHORIZE"));

        Response response = BusinessStakeholdersManagementApiImpl.updateBusinessStakeholders(
            Collections.singletonList(item));

        assertSendBadRequest(response, "Representative name is required for accountID acc-1");
    }

    /**
     * Verifies update returns OK with empty body when payload has no representatives.
     */
    @Test
    public void testUpdateBusinessStakeholdersOkOnEmptyRepresentatives() throws AccountMetadataException {
        BusinessStakeholderItem item = buildUpsertItem("acc-1");

        Response response = BusinessStakeholdersManagementApiImpl.updateBusinessStakeholders(
            Collections.singletonList(item));

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        Assert.assertEquals(asStringList(response).size(), 0);
        Mockito.verify(metadataDAO, Mockito.never())
            .updateBatchBusinessStakeholderPermissions(Mockito.any(Connection.class), Mockito.anyList());
    }

    /**
     * Verifies successful update when all requested records exist.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testUpdateBusinessStakeholdersSuccess() throws Exception {
        BusinessStakeholderPermissionItem existing = buildItem("acc-2", "user-2", "VIEW");
        BusinessStakeholderItem requestItem = buildUpsertItem("acc-2",
            buildRepresentative("user-2", "AUTHORIZE"));

        Mockito.when(metadataDAO.getBatchBusinessStakeholderPermissions(Mockito.eq(connection), Mockito.anyList()))
            .thenReturn(Collections.singletonList(existing));

        Response response = BusinessStakeholdersManagementApiImpl.updateBusinessStakeholders(
            Collections.singletonList(requestItem));

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        List<BusinessStakeholderItem> body = asUpsertItemList(response);
        Assert.assertEquals(body.size(), 1);
        Assert.assertEquals(body.get(0).getAccountID(), "acc-2");
        Mockito.verify(metadataDAO).updateBatchBusinessStakeholderPermissions(Mockito.eq(connection),
                Mockito.anyList());
    }

    /**
     * Verifies update returns OK without persistence when no records exist.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testUpdateBusinessStakeholdersOkWhenNoneExist() throws Exception {
        BusinessStakeholderItem requestItem = buildUpsertItem("acc-2",
            buildRepresentative("user-2", "AUTHORIZE"));

        Mockito.when(metadataDAO.getBatchBusinessStakeholderPermissions(Mockito.eq(connection), Mockito.anyList()))
            .thenReturn(Collections.emptyList());

        Response response = BusinessStakeholdersManagementApiImpl.updateBusinessStakeholders(
            Collections.singletonList(requestItem));

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        Assert.assertEquals(asStringList(response).size(), 0);
        Mockito.verify(metadataDAO, Mockito.never())
            .updateBatchBusinessStakeholderPermissions(Mockito.any(Connection.class), Mockito.anyList());
    }

    /**
     * Verifies update only persists existing subset in partial-existing scenario.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testUpdateBusinessStakeholdersOkWhenPartialExisting() throws Exception {
        BusinessStakeholderPermissionItem existing = buildItem("acc-2", "user-2", "VIEW");
        BusinessStakeholderItem requestItem = buildUpsertItem("acc-2",
            buildRepresentative("user-2", "AUTHORIZE"),
            buildRepresentative("user-3", "VIEW"));

        Mockito.when(metadataDAO.getBatchBusinessStakeholderPermissions(Mockito.eq(connection), Mockito.anyList()))
            .thenReturn(Collections.singletonList(existing));

        Response response = BusinessStakeholdersManagementApiImpl.updateBusinessStakeholders(
            Collections.singletonList(requestItem));

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        List<BusinessStakeholderItem> body = asUpsertItemList(response);
        Assert.assertEquals(body.size(), 1);
        Assert.assertEquals(body.get(0).getAccountID(), "acc-2");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<BusinessStakeholderPermissionItem>> updateCaptor =
            (ArgumentCaptor<List<BusinessStakeholderPermissionItem>>) (ArgumentCaptor<?>)
                ArgumentCaptor.forClass(List.class);
        Mockito.verify(metadataDAO).updateBatchBusinessStakeholderPermissions(Mockito.eq(connection),
            updateCaptor.capture());
        Assert.assertEquals(updateCaptor.getValue().size(), 1);
        Assert.assertEquals(updateCaptor.getValue().get(0).getUserId(), "user-2");
    }

    /**
     * Verifies internal server error when update retrieval fails.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testUpdateBusinessStakeholdersServiceErrorOnGetBatch() throws Exception {
        BusinessStakeholderItem item = buildUpsertItem("acc-1",
            buildRepresentative("user-1", "AUTHORIZE"));

        Mockito.when(metadataDAO.getBatchBusinessStakeholderPermissions(Mockito.eq(connection), Mockito.anyList()))
            .thenThrow(new AccountMetadataException("fail"));

        Response response = BusinessStakeholdersManagementApiImpl.updateBusinessStakeholders(
            Collections.singletonList(item));

        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        ErrorResponse body = (ErrorResponse) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertTrue(body.getErrorDescription().startsWith("Failed to update business stakeholder records:"));
    }

    /**
     * Verifies internal server error when update persistence fails.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testUpdateBusinessStakeholdersServiceErrorOnUpdateBatch() throws Exception {
        BusinessStakeholderPermissionItem existing = buildItem("acc-1", "user-1", "VIEW");
        BusinessStakeholderItem item = buildUpsertItem("acc-1",
            buildRepresentative("user-1", "AUTHORIZE"));

        Mockito.when(metadataDAO.getBatchBusinessStakeholderPermissions(Mockito.eq(connection), Mockito.anyList()))
            .thenReturn(Collections.singletonList(existing));
        Mockito.doThrow(new AccountMetadataException("fail"))
            .when(metadataDAO)
            .updateBatchBusinessStakeholderPermissions(Mockito.eq(connection), Mockito.anyList());

        Response response = BusinessStakeholdersManagementApiImpl.updateBusinessStakeholders(
            Collections.singletonList(item));

        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        ErrorResponse body = (ErrorResponse) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertTrue(body.getErrorDescription().startsWith("Failed to update business stakeholder records:"));
    }

    /**
     * Verifies bad request when delete request contains null item.
     */
    @Test
    public void testDeleteBusinessStakeholdersSendBadRequestOnNullRequestItem() {
        Response response = BusinessStakeholdersManagementApiImpl.deleteBusinessStakeholders(
            Collections.singletonList(null));

        assertSendBadRequest(response, "Request contains null business stakeholder item");
    }

    /**
     * Verifies bad request when delete account id is missing.
     */
    @Test
    public void testDeleteBusinessStakeholdersSendBadRequestOnMissingAccountId() {
        BusinessStakeholderDeleteItem item = buildDeleteItem(" ", "user-1");

        Response response = BusinessStakeholdersManagementApiImpl.deleteBusinessStakeholders(
            Collections.singletonList(item));

        assertSendBadRequest(response, "accountID is required");
    }

    /**
     * Verifies bad request when delete nominated representative is blank.
     */
    @Test
    public void testDeleteBusinessStakeholdersSendBadRequestOnBlankRepresentative() {
        BusinessStakeholderDeleteItem item = buildDeleteItem("acc-1", " ");

        Response response = BusinessStakeholdersManagementApiImpl.deleteBusinessStakeholders(
            Collections.singletonList(item));

        assertSendBadRequest(response, "Representative name is required for accountID acc-1");
    }

    /**
     * Verifies delete returns OK and empty body when request has no representatives.
     */
    @Test
    public void testDeleteBusinessStakeholdersOkOnEmptyRepresentatives() throws AccountMetadataException {
        BusinessStakeholderDeleteItem item = buildDeleteItem("acc-1");

        Response response = BusinessStakeholdersManagementApiImpl.deleteBusinessStakeholders(
            Collections.singletonList(item));

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        Assert.assertEquals(asStringList(response).size(), 0);
        Mockito.verify(metadataDAO, Mockito.never())
            .deleteBatchBusinessStakeholderPermissions(Mockito.any(Connection.class), Mockito.anyList());
    }

    /**
     * Verifies successful delete when all requested records exist.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testDeleteBusinessStakeholdersSuccess() throws Exception {
        BusinessStakeholderDeleteItem requestItem = buildDeleteItem("acc-3", "user-3");
        BusinessStakeholderPermissionItem existing = buildItem("acc-3", "user-3", "VIEW");
            BusinessStakeholderPermissionItem accountPermission = buildItem("acc-3", "user-3", "REVOKE");

        Mockito.when(metadataDAO.getBatchBusinessStakeholderPermissions(Mockito.eq(connection), Mockito.anyList()))
            .thenReturn(Collections.singletonList(existing));
            Mockito.when(metadataDAO.getBatchBusinessStakeholderPermissionsByAccountIds(Mockito.eq(connection),
                    Mockito.eq(Collections.singletonList("acc-3"))))
                .thenReturn(Collections.singletonList(accountPermission));

        Response response = BusinessStakeholdersManagementApiImpl.deleteBusinessStakeholders(
            Collections.singletonList(requestItem));

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        List<BusinessStakeholderDeleteItem> body = asDeleteItemList(response);
        Assert.assertEquals(body.size(), 1);
        Assert.assertEquals(body.get(0).getAccountID(), "acc-3");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<BusinessStakeholderPermissionItem>> revokeCaptor =
            (ArgumentCaptor<List<BusinessStakeholderPermissionItem>>) (ArgumentCaptor<?>)
                ArgumentCaptor.forClass(List.class);
        Mockito.verify(metadataDAO)
            .updateBatchBusinessStakeholderPermissions(Mockito.eq(connection), revokeCaptor.capture());
        Assert.assertEquals(revokeCaptor.getValue().size(), 1);
        Assert.assertEquals(revokeCaptor.getValue().get(0).getAccountId(), "acc-3");
        Assert.assertEquals(revokeCaptor.getValue().get(0).getUserId(), "user-3");
        Assert.assertEquals(revokeCaptor.getValue().get(0).getPermission().value(), "REVOKE");

        Mockito.verify(metadataDAO).deleteBatchBusinessStakeholderPermissions(Mockito.eq(connection),
                Mockito.anyList());
    }

    /**
     * Verifies delete revokes account owners as well as nominated representatives.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testDeleteBusinessStakeholdersRevokesAccountOwnersAsWell() throws Exception {
        BusinessStakeholderDeleteItem requestItem = buildDeleteItem(
            "acc-4", Arrays.asList("owner-1"), "user-4");
        List<BusinessStakeholderPermissionItem> existingItems = Arrays.asList(
            buildItem("acc-4", "owner-1", "VIEW"),
            buildItem("acc-4", "user-4", "VIEW"));
        List<BusinessStakeholderPermissionItem> accountPermissions = Arrays.asList(
            buildItem("acc-4", "owner-1", "REVOKE"),
            buildItem("acc-4", "user-4", "REVOKE"));

        Mockito.when(metadataDAO.getBatchBusinessStakeholderPermissions(Mockito.eq(connection), Mockito.anyList()))
            .thenReturn(existingItems);
        Mockito.when(metadataDAO.getBatchBusinessStakeholderPermissionsByAccountIds(Mockito.eq(connection),
                Mockito.eq(Collections.singletonList("acc-4"))))
            .thenReturn(accountPermissions);

        Response response = BusinessStakeholdersManagementApiImpl.deleteBusinessStakeholders(
            Collections.singletonList(requestItem));

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        List<BusinessStakeholderDeleteItem> body = asDeleteItemList(response);
        Assert.assertEquals(body.size(), 1);
        Assert.assertEquals(body.get(0).getAccountID(), "acc-4");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<BusinessStakeholderPermissionItem>> revokeCaptor =
            (ArgumentCaptor<List<BusinessStakeholderPermissionItem>>) (ArgumentCaptor<?>)
                ArgumentCaptor.forClass(List.class);
        Mockito.verify(metadataDAO).updateBatchBusinessStakeholderPermissions(Mockito.eq(connection),
            revokeCaptor.capture());
        Assert.assertEquals(revokeCaptor.getValue().size(), 2);
        Assert.assertTrue(revokeCaptor.getValue().stream().anyMatch(item ->
            "owner-1".equals(item.getUserId()) && "REVOKE".equals(item.getPermission().value())));
        Assert.assertTrue(revokeCaptor.getValue().stream().anyMatch(item ->
            "user-4".equals(item.getUserId()) && "REVOKE".equals(item.getPermission().value())));
    }

    /**
     * Verifies delete returns OK and does not persist when no records exist.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testDeleteBusinessStakeholdersOkWhenNoneExist() throws Exception {
        BusinessStakeholderDeleteItem requestItem = buildDeleteItem("acc-3", "user-3");

        Mockito.when(metadataDAO.getBatchBusinessStakeholderPermissions(Mockito.eq(connection), Mockito.anyList()))
            .thenReturn(Collections.emptyList());

        Response response = BusinessStakeholdersManagementApiImpl.deleteBusinessStakeholders(
            Collections.singletonList(requestItem));

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        Assert.assertEquals(asStringList(response).size(), 0);
        Mockito.verify(metadataDAO, Mockito.never())
            .updateBatchBusinessStakeholderPermissions(Mockito.any(Connection.class), Mockito.anyList());
        Mockito.verify(metadataDAO, Mockito.never())
            .getBatchBusinessStakeholderPermissionsByAccountIds(Mockito.any(Connection.class), Mockito.anyList());
        Mockito.verify(metadataDAO, Mockito.never())
            .deleteBatchBusinessStakeholderPermissions(Mockito.any(Connection.class), Mockito.anyList());
    }

    /**
     * Verifies delete only removes existing subset in partial-existing scenario.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testDeleteBusinessStakeholdersOkWhenPartialExisting() throws Exception {
        BusinessStakeholderDeleteItem requestItem = buildDeleteItem("acc-3", "user-3", "user-4");
        BusinessStakeholderPermissionItem existing = buildItem("acc-3", "user-3", "VIEW");
            List<BusinessStakeholderPermissionItem> accountPermissions = Arrays.asList(
                buildItem("acc-3", "user-3", "REVOKE"),
                buildItem("acc-3", "user-5", "VIEW"));

        Mockito.when(metadataDAO.getBatchBusinessStakeholderPermissions(Mockito.eq(connection), Mockito.anyList()))
            .thenReturn(Collections.singletonList(existing));
            Mockito.when(metadataDAO.getBatchBusinessStakeholderPermissionsByAccountIds(Mockito.eq(connection),
                    Mockito.eq(Collections.singletonList("acc-3"))))
                .thenReturn(accountPermissions);

        Response response = BusinessStakeholdersManagementApiImpl.deleteBusinessStakeholders(
            Collections.singletonList(requestItem));

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        List<BusinessStakeholderDeleteItem> body = asDeleteItemList(response);
        Assert.assertEquals(body.size(), 1);
        Assert.assertEquals(body.get(0).getAccountID(), "acc-3");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<BusinessStakeholderPermissionItem>> revokeCaptor =
            (ArgumentCaptor<List<BusinessStakeholderPermissionItem>>) (ArgumentCaptor<?>)
                ArgumentCaptor.forClass(List.class);
        Mockito.verify(metadataDAO).updateBatchBusinessStakeholderPermissions(Mockito.eq(connection),
            revokeCaptor.capture());
        Assert.assertEquals(revokeCaptor.getValue().size(), 1);
        Assert.assertEquals(revokeCaptor.getValue().get(0).getUserId(), "user-3");
        Assert.assertEquals(revokeCaptor.getValue().get(0).getPermission().value(), "REVOKE");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<BusinessStakeholderPermissionItem>> deleteCaptor =
            (ArgumentCaptor<List<BusinessStakeholderPermissionItem>>) (ArgumentCaptor<?>)
                ArgumentCaptor.forClass(List.class);
        Mockito.verify(metadataDAO).deleteBatchBusinessStakeholderPermissions(Mockito.eq(connection),
                deleteCaptor.capture());
        Assert.assertEquals(deleteCaptor.getValue().size(), 2);
    }

    /**
     * Verifies revoke-only behavior when at least one AUTHORIZE permission remains for the account.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testDeleteBusinessStakeholdersKeepsAccountWhenAuthorizeExists() throws Exception {
        BusinessStakeholderDeleteItem requestItem = buildDeleteItem("acc-3", "user-3");
        BusinessStakeholderPermissionItem existing = buildItem("acc-3", "user-3", "VIEW");
        List<BusinessStakeholderPermissionItem> accountPermissions = Arrays.asList(
            buildItem("acc-3", "user-3", "REVOKE"),
            buildItem("acc-3", "user-admin", "AUTHORIZE"));

        Mockito.when(metadataDAO.getBatchBusinessStakeholderPermissions(Mockito.eq(connection), Mockito.anyList()))
            .thenReturn(Collections.singletonList(existing));
        Mockito.when(metadataDAO.getBatchBusinessStakeholderPermissionsByAccountIds(Mockito.eq(connection),
                Mockito.eq(Collections.singletonList("acc-3"))))
            .thenReturn(accountPermissions);

        Response response = BusinessStakeholdersManagementApiImpl.deleteBusinessStakeholders(
            Collections.singletonList(requestItem));

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        List<BusinessStakeholderDeleteItem> body = asDeleteItemList(response);
        Assert.assertEquals(body.size(), 1);
        Assert.assertEquals(body.get(0).getAccountID(), "acc-3");
        Mockito.verify(metadataDAO).updateBatchBusinessStakeholderPermissions(Mockito.eq(connection),
                Mockito.anyList());
        Mockito.verify(metadataDAO, Mockito.never())
            .deleteBatchBusinessStakeholderPermissions(Mockito.any(Connection.class), Mockito.anyList());
    }

    /**
     * Verifies internal server error when delete retrieval fails.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testDeleteBusinessStakeholdersServiceErrorOnGetBatch() throws Exception {
        BusinessStakeholderDeleteItem item = buildDeleteItem("acc-1", "user-1");

        Mockito.when(metadataDAO.getBatchBusinessStakeholderPermissions(Mockito.eq(connection), Mockito.anyList()))
            .thenThrow(new AccountMetadataException("fail"));

        Response response = BusinessStakeholdersManagementApiImpl.deleteBusinessStakeholders(
            Collections.singletonList(item));

        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        ErrorResponse body = (ErrorResponse) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertTrue(body.getErrorDescription().startsWith("Failed to delete business stakeholder records:"));
    }

    /**
     * Verifies internal server error when account-level permission retrieval fails.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testDeleteBusinessStakeholdersServiceErrorOnGetBatchByAccountIds() throws Exception {
        BusinessStakeholderDeleteItem item = buildDeleteItem("acc-1", "user-1");
        BusinessStakeholderPermissionItem existing = buildItem("acc-1", "user-1", "VIEW");

        Mockito.when(metadataDAO.getBatchBusinessStakeholderPermissions(Mockito.eq(connection), Mockito.anyList()))
            .thenReturn(Collections.singletonList(existing));
        Mockito.when(metadataDAO.getBatchBusinessStakeholderPermissionsByAccountIds(Mockito.eq(connection),
                Mockito.eq(Collections.singletonList("acc-1"))))
            .thenThrow(new AccountMetadataException("fail"));

        Response response = BusinessStakeholdersManagementApiImpl.deleteBusinessStakeholders(
            Collections.singletonList(item));

        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        ErrorResponse body = (ErrorResponse) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertTrue(body.getErrorDescription().startsWith("Failed to delete business stakeholder records:"));
    }

    /**
     * Verifies internal server error when delete persistence fails.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testDeleteBusinessStakeholdersServiceErrorOnDeleteBatch() throws Exception {
        BusinessStakeholderDeleteItem item = buildDeleteItem("acc-1", "user-1");
        BusinessStakeholderPermissionItem existing = buildItem("acc-1", "user-1", "VIEW");

        Mockito.when(metadataDAO.getBatchBusinessStakeholderPermissions(Mockito.eq(connection), Mockito.anyList()))
            .thenReturn(Collections.singletonList(existing));
            Mockito.when(metadataDAO.getBatchBusinessStakeholderPermissionsByAccountIds(Mockito.eq(connection),
                    Mockito.eq(Collections.singletonList("acc-1"))))
                .thenReturn(Collections.singletonList(buildItem("acc-1", "user-1", "REVOKE")));
        Mockito.doThrow(new AccountMetadataException("fail"))
            .when(metadataDAO)
            .deleteBatchBusinessStakeholderPermissions(Mockito.eq(connection), Mockito.anyList());

        Response response = BusinessStakeholdersManagementApiImpl.deleteBusinessStakeholders(
            Collections.singletonList(item));

        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        ErrorResponse body = (ErrorResponse) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertTrue(body.getErrorDescription().startsWith("Failed to delete business stakeholder records:"));
    }

    /**
     * Verifies internal server error when revoke update fails.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testDeleteBusinessStakeholdersServiceErrorOnRevokeUpdate() throws Exception {
        BusinessStakeholderDeleteItem item = buildDeleteItem("acc-1", "user-1");
        BusinessStakeholderPermissionItem existing = buildItem("acc-1", "user-1", "VIEW");

        Mockito.when(metadataDAO.getBatchBusinessStakeholderPermissions(Mockito.eq(connection), Mockito.anyList()))
            .thenReturn(Collections.singletonList(existing));
        Mockito.doThrow(new AccountMetadataException("fail"))
            .when(metadataDAO)
            .updateBatchBusinessStakeholderPermissions(Mockito.eq(connection), Mockito.anyList());

        Response response = BusinessStakeholdersManagementApiImpl.deleteBusinessStakeholders(
            Collections.singletonList(item));

        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        ErrorResponse body = (ErrorResponse) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertTrue(body.getErrorDescription().startsWith("Failed to delete business stakeholder records:"));
    }

    /**
     * Builds a business stakeholder permission item.
     */
    private BusinessStakeholderPermissionItem buildItem(String accountId, String userId, String permission) {
        BusinessStakeholderPermissionItem item = new BusinessStakeholderPermissionItem();
        item.setAccountId(accountId);
        item.setUserId(userId);
        item.setPermission(permission != null
                ? BusinessStakeholderPermissionItem.PermissionEnum.fromValue(permission) : null);
        return item;
    }

    /**
     * Builds a business stakeholder representative model.
     */
    private BusinessStakeholderRepresentative buildRepresentative(String name, String permission) {
        BusinessStakeholderRepresentative representative = new BusinessStakeholderRepresentative();
        representative.setName(name);
        if (permission != null && !permission.trim().isEmpty()) {
            representative.setPermission(
                    BusinessStakeholderRepresentative.PermissionEnum.fromValue(permission.trim()));
        }
        return representative;
    }

    /**
     * Builds an upsert request item.
     */
    private BusinessStakeholderItem buildUpsertItem(String accountId,
                                                    BusinessStakeholderRepresentative... representatives) {
        return buildUpsertItem(accountId, new ArrayList<>(), representatives);
    }

    /**
     * Builds an upsert request item with account owners.
     */
    private BusinessStakeholderItem buildUpsertItem(String accountId, List<String> accountOwners,
                                                    BusinessStakeholderRepresentative... representatives) {
        BusinessStakeholderItem item = new BusinessStakeholderItem();
        item.setAccountID(accountId);
        item.setAccountOwners(new ArrayList<>(accountOwners));
        item.setNominatedRepresentatives(new ArrayList<>(Arrays.asList(representatives)));
        return item;
    }

    /**
     * Builds a delete request item.
     */
    private BusinessStakeholderDeleteItem buildDeleteItem(String accountId, String... representativeUserIds) {
        return buildDeleteItem(accountId, new ArrayList<>(), representativeUserIds);
    }

    /**
     * Builds a delete request item with account owners.
     */
    private BusinessStakeholderDeleteItem buildDeleteItem(String accountId, List<String> accountOwners,
            String... representativeUserIds) {
        BusinessStakeholderDeleteItem item = new BusinessStakeholderDeleteItem();
        item.setAccountID(accountId);
        item.setAccountOwners(new ArrayList<>(accountOwners));
        item.setNominatedRepresentatives(new ArrayList<>(Arrays.asList(representativeUserIds)));
        return item;
    }

    /**
     * Asserts a bad request response with expected error description.
     */
    private void assertSendBadRequest(Response response, String expectedDescription) {
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
        ErrorResponse body = (ErrorResponse) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertEquals(body.getErrorDescription(), expectedDescription);
    }

    /**
     * Casts response entity to list of account ids.
     */
    @SuppressWarnings("unchecked")
    private List<String> asStringList(Response response) {
        return (List<String>) response.getEntity();
    }

    @SuppressWarnings("unchecked")
    private List<BusinessStakeholderItem> asUpsertItemList(Response response) {
        return (List<BusinessStakeholderItem>) response.getEntity();
    }

    @SuppressWarnings("unchecked")
    private List<BusinessStakeholderDeleteItem> asDeleteItemList(Response response) {
        return (List<BusinessStakeholderDeleteItem>) response.getEntity();
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

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

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.openbanking.consumerdatastandards.account.metadata.exceptions.AccountMetadataException;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.LegalEntitySharingItem;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.core.AccountMetadataServiceImpl;
import org.wso2.openbanking.consumerdatastandards.account.metadata.service.dao.AccountMetadataDAO;
import org.wso2.openbanking.consumerdatastandards.account.metadata.utils.connection.provider.ConnectionProvider;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.Response;

import static org.wso2.openbanking.consumerdatastandards.account.metadata.utils.CommonTestUtils.buildLegalEntityItem;

/**
 * Unit tests for {@link CeasingSecondaryUserSharingApiImpl}.
 */
public class CeasingSecondaryUserSharingApiImplTest {

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
        Class.forName(CeasingSecondaryUserSharingApiImpl.class.getName(), true,
                CeasingSecondaryUserSharingApiImpl.class.getClassLoader());
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
     * Verifies legal entity sharing update delegates to service upsert API and returns the processed payload.
     *
     * @throws Exception if setup or invocation fails
     */
    @Test
    public void testUpdateLegalEntitySharingStatusUpsertsItems() throws Exception {
        LegalEntitySharingItem blockRequest = buildLegalEntityItem("user-1", "acc-1", "le-003",
                String.valueOf(LegalEntitySharingItem.LegalEntitySharingStatusEnum.blocked));
        LegalEntitySharingItem activeRequest = buildLegalEntityItem("user-1", "acc-2", "le-001",
                String.valueOf(LegalEntitySharingItem.LegalEntitySharingStatusEnum.active));

        Response response = CeasingSecondaryUserSharingApiImpl.updateLegalEntitySharingStatus(
                Arrays.asList(blockRequest, activeRequest));

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        @SuppressWarnings("unchecked")
        List<LegalEntitySharingItem> body = (List<LegalEntitySharingItem>) response.getEntity();
        Assert.assertEquals(body.size(), 2);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<LegalEntitySharingItem>> upsertCaptor =
                (ArgumentCaptor<List<LegalEntitySharingItem>>) (ArgumentCaptor<?>)
                        ArgumentCaptor.forClass(List.class);
        Mockito.verify(metadataDAO).upsertBatchLegalEntitySharingStatuses(Mockito.eq(connection),
                upsertCaptor.capture());
        Assert.assertEquals(upsertCaptor.getValue().size(), 2);
    }

    /**
     * Verifies GET endpoint returns 400 Bad Request when clientId is not provided.
     */
    @Test
    public void testGetLegalEntitySharingStatusBadRequestWhenClientIdMissing() throws AccountMetadataException {
        Response response = CeasingSecondaryUserSharingApiImpl.getLegalEntitySharingStatus("acc-1,acc-2", "user-1",
                null);

        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
        Mockito.verify(metadataDAO, Mockito.never())
                .getBatchLegalEntitySharingStatuses(Mockito.any(Connection.class), Mockito.anyList());
    }

     /**
     * Resets the singleton instance so each test runs with a clean service state.
     *
     * @throws Exception if reflection access fails
     */
     private void resetSingleton() throws Exception {
        Field instanceField = AccountMetadataServiceImpl.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }
}

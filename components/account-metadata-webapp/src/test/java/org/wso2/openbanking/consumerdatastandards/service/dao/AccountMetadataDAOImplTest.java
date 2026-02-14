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

package org.wso2.openbanking.consumerdatastandards.service.dao;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.openbanking.consumerdatastandards.exceptions.AccountMetadataException;
import org.wso2.openbanking.consumerdatastandards.service.dao.queries.AccountMetadataDBQueries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountMetadataDAOImplTest {

    private static class TestQueries implements AccountMetadataDBQueries {

        @Override
        public String getAddDisclosureOptionQuery() {
            return "INSERT";
        }

        @Override
        public String getUpdateDisclosureOptionQuery() {
            return "UPDATE";
        }

        @Override
        public String getGetDisclosureOptionQuery() {
            return "SELECT";
        }
    }

    @Test
    public void testAddDisclosureOptionSuccess() throws Exception {
        AccountMetadataDAO dao = new AccountMetadataDAOImpl(new TestQueries());
        Connection connection = Mockito.mock(Connection.class);
        PreparedStatement statement = Mockito.mock(PreparedStatement.class);

        Mockito.when(connection.prepareStatement("INSERT")).thenReturn(statement);
        Mockito.when(statement.executeUpdate()).thenReturn(1);

        dao.addDisclosureOption(connection, "acc-100", "no-sharing");

        Mockito.verify(statement).setString(1, "acc-100");
        Mockito.verify(statement).setString(2, "no-sharing");
        Mockito.verify(statement).executeUpdate();
    }

    @Test(expectedExceptions = AccountMetadataException.class)
    public void testAddDisclosureOptionSqlException() throws Exception {
        AccountMetadataDAO dao = new AccountMetadataDAOImpl(new TestQueries());
        Connection connection = Mockito.mock(Connection.class);

        Mockito.when(connection.prepareStatement("INSERT"))
                .thenThrow(new SQLException("bad"));

        dao.addDisclosureOption(connection, "acc-101", "pre-approval");
    }

    @Test
    public void testUpdateDisclosureOptionSuccess() throws Exception {
        AccountMetadataDAO dao = new AccountMetadataDAOImpl(new TestQueries());
        Connection connection = Mockito.mock(Connection.class);
        PreparedStatement statement = Mockito.mock(PreparedStatement.class);

        Mockito.when(connection.prepareStatement("UPDATE")).thenReturn(statement);
        Mockito.when(statement.executeUpdate()).thenReturn(1);

        dao.updateDisclosureOption(connection, "acc-200", "pre-approval");

        Mockito.verify(statement).setString(1, "pre-approval");
        Mockito.verify(statement).setString(2, "acc-200");
        Mockito.verify(statement).executeUpdate();
    }

    @Test(expectedExceptions = AccountMetadataException.class)
    public void testUpdateDisclosureOptionSqlException() throws Exception {
        AccountMetadataDAO dao = new AccountMetadataDAOImpl(new TestQueries());
        Connection connection = Mockito.mock(Connection.class);

        Mockito.when(connection.prepareStatement("UPDATE"))
                .thenThrow(new SQLException("bad"));

        dao.updateDisclosureOption(connection, "acc-201", "no-sharing");
    }

    @Test
    public void testGetDisclosureOptionFound() throws Exception {
        AccountMetadataDAO dao = new AccountMetadataDAOImpl(new TestQueries());
        Connection connection = Mockito.mock(Connection.class);
        PreparedStatement statement = Mockito.mock(PreparedStatement.class);
        ResultSet resultSet = Mockito.mock(ResultSet.class);

        Mockito.when(connection.prepareStatement("SELECT")).thenReturn(statement);
        Mockito.when(statement.executeQuery()).thenReturn(resultSet);
        Mockito.when(resultSet.next()).thenReturn(true);
        Mockito.when(resultSet.getString("DISCLOSURE_OPTION_STATUS"))
                .thenReturn("no-sharing");

        String status = dao.getDisclosureOption(connection, "acc-300");

        Assert.assertEquals(status, "no-sharing");
    }

    @Test
    public void testGetDisclosureOptionNotFound() throws Exception {
        AccountMetadataDAO dao = new AccountMetadataDAOImpl(new TestQueries());
        Connection connection = Mockito.mock(Connection.class);
        PreparedStatement statement = Mockito.mock(PreparedStatement.class);
        ResultSet resultSet = Mockito.mock(ResultSet.class);

        Mockito.when(connection.prepareStatement("SELECT")).thenReturn(statement);
        Mockito.when(statement.executeQuery()).thenReturn(resultSet);
        Mockito.when(resultSet.next()).thenReturn(false);

        String status = dao.getDisclosureOption(connection, "acc-301");

        Assert.assertNull(status);
    }

    @Test(expectedExceptions = AccountMetadataException.class)
    public void testGetDisclosureOptionSqlException() throws Exception {
        AccountMetadataDAO dao = new AccountMetadataDAOImpl(new TestQueries());
        Connection connection = Mockito.mock(Connection.class);

        Mockito.when(connection.prepareStatement("SELECT"))
                .thenThrow(new SQLException("bad"));

        dao.getDisclosureOption(connection, "acc-302");
    }
}

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

package org.wso2.openbanking.consumerdatastandards.account.metadata.utils;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.openbanking.consumerdatastandards.account.metadata.configurations.ConfigurableProperties;
import org.wso2.openbanking.consumerdatastandards.account.metadata.utils.connection.provider.DatabaseConnectionProvider;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.spi.InitialContextFactory;
import javax.sql.DataSource;

/**
 * Unit tests for {@link DatabaseUtil} and {@link DatabaseConnectionProvider}.
 */
public class DatabaseUtilTest {

    private static final String INITIAL_CONTEXT_FACTORY_KEY = "java.naming.factory.initial";
    private static final String ORIGINAL_CONTEXT_FACTORY = System.getProperty(INITIAL_CONTEXT_FACTORY_KEY);
    private DataSource dataSource;
    private Connection connection;

    /**
     * Initializes a mock datasource and registers a test initial context factory.
     */
    @BeforeClass
    public void setUp() {
        dataSource = Mockito.mock(DataSource.class);
        connection = Mockito.mock(Connection.class);
        try {
            Mockito.when(dataSource.getConnection()).thenReturn(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        TestInitialContextFactory.setDataSource(dataSource);
        System.setProperty(INITIAL_CONTEXT_FACTORY_KEY, TestInitialContextFactory.class.getName());
    }

    /**
     * Restores the original initial context factory system property.
     */
    @AfterClass
    public void tearDown() {
        if (ORIGINAL_CONTEXT_FACTORY == null) {
            System.clearProperty(INITIAL_CONTEXT_FACTORY_KEY);
        } else {
            System.setProperty(INITIAL_CONTEXT_FACTORY_KEY, ORIGINAL_CONTEXT_FACTORY);
        }
    }

    /**
     * Resets mock interactions and stubbing before each test to avoid order dependency.
     */
    @BeforeMethod
    public void resetMocks() throws SQLException {
        Mockito.reset(dataSource, connection);
        Mockito.when(dataSource.getConnection()).thenReturn(connection);
    }

    /**
     * Verifies that a JDBC connection can be obtained via {@link DatabaseUtil}.
     *
     * @throws Exception if connection retrieval fails
     */
    @Test
    public void testDatabaseUtilGetConnection() throws Exception {
        Connection connection = DatabaseUtil.getConnection();
        Assert.assertNotNull(connection);
        Assert.assertSame(connection, this.connection);
        Assert.assertEquals(TestInitialContextFactory.getLastLookupName(),
                ConfigurableProperties.ACCOUNT_METADATA_DATASOURCE_JNDI_NAME);
        Mockito.verify(dataSource).getConnection();
    }

    /**
     * Verifies that {@link DatabaseConnectionProvider} delegates to the utility connection retrieval.
     *
     * @throws Exception if connection retrieval fails
     */
    @Test
    public void testDatabaseConnectionProviderDelegates() throws Exception {
        DatabaseConnectionProvider provider = new DatabaseConnectionProvider();
        Connection connection = provider.getConnection();
        Assert.assertNotNull(connection);
        Assert.assertSame(connection, this.connection);
        Mockito.verify(dataSource).getConnection();
    }

    /**
     * Verifies that connection failures from the DataSource are propagated as SQLExceptions.
     *
     * @throws Exception if setup fails
     */
    @Test
    public void testDatabaseUtilGetConnectionFailure() throws Exception {
        SQLException expected = new SQLException("connection down");
        Mockito.when(dataSource.getConnection()).thenThrow(expected);

        try {
            DatabaseUtil.getConnection();
            Assert.fail("Expected SQLException to be thrown");
        } catch (SQLException ex) {
            Assert.assertSame(ex, expected);
        }
    }

    /**
     * Test initial context factory that returns a lightweight in-memory {@link Context}.
     */
    public static class TestInitialContextFactory implements InitialContextFactory {

        private static DataSource dataSource;
        private static String lastLookupName;

        /**
         * Sets the datasource returned by lookups from the test context.
         *
         * @param dataSource datasource to expose
         */
        public static void setDataSource(DataSource dataSource) {
            TestInitialContextFactory.dataSource = dataSource;
        }

        public static String getLastLookupName() {
            return lastLookupName;
        }

        /**
         * Returns a test context instance.
         *
         * @param environment naming environment
         * @return test context
         */
        @Override
        public Context getInitialContext(Hashtable<?, ?> environment) {
            return new TestContext();
        }

        /**
         * Minimal {@link Context} implementation for datasource lookup in tests.
         */
        private static class TestContext implements Context {

            @Override
            public Object lookup(String name) {
                lastLookupName = name;
                return dataSource;
            }

            @Override
            public Object lookup(Name name) {
                lastLookupName = name == null ? null : name.toString();
                return dataSource;
            }

            @Override
            public void close() {
            }

            @Override
            public Object addToEnvironment(String propName, Object propVal) {
                return null;
            }

            @Override
            public Object removeFromEnvironment(String propName) {
                return null;
            }

            @Override
            public Hashtable<?, ?> getEnvironment() {
                return new Hashtable<>();
            }

            @Override
            public String getNameInNamespace() {
                return "";
            }

            @Override
            public NameParser getNameParser(String name) {
                throw new UnsupportedOperationException("not used");
            }

            @Override
            public NameParser getNameParser(Name name) {
                throw new UnsupportedOperationException("not used");
            }

            @Override
            public NamingEnumeration<NameClassPair> list(String name) {
                throw new UnsupportedOperationException("not used");
            }

            @Override
            public NamingEnumeration<NameClassPair> list(Name name) {
                throw new UnsupportedOperationException("not used");
            }

            @Override
            public NamingEnumeration<javax.naming.Binding> listBindings(String name) {
                throw new UnsupportedOperationException("not used");
            }

            @Override
            public NamingEnumeration<javax.naming.Binding> listBindings(Name name) {
                throw new UnsupportedOperationException("not used");
            }

            @Override
            public void bind(String name, Object obj) {
                throw new UnsupportedOperationException("not used");
            }

            @Override
            public void bind(Name name, Object obj) {
                throw new UnsupportedOperationException("not used");
            }

            @Override
            public void rebind(String name, Object obj) {
                throw new UnsupportedOperationException("not used");
            }

            @Override
            public void rebind(Name name, Object obj) {
                throw new UnsupportedOperationException("not used");
            }

            @Override
            public void unbind(String name) {
                throw new UnsupportedOperationException("not used");
            }

            @Override
            public void unbind(Name name) {
                throw new UnsupportedOperationException("not used");
            }

            @Override
            public void rename(String oldName, String newName) {
                throw new UnsupportedOperationException("not used");
            }

            @Override
            public void rename(Name oldName, Name newName) {
                throw new UnsupportedOperationException("not used");
            }

            @Override
            public Context createSubcontext(String name) {
                throw new UnsupportedOperationException("not used");
            }

            @Override
            public Context createSubcontext(Name name) {
                throw new UnsupportedOperationException("not used");
            }

            @Override
            public void destroySubcontext(String name) {
                throw new UnsupportedOperationException("not used");
            }

            @Override
            public void destroySubcontext(Name name) {
                throw new UnsupportedOperationException("not used");
            }

            @Override
            public Object lookupLink(String name) {
                throw new UnsupportedOperationException("not used");
            }

            @Override
            public Object lookupLink(Name name) {
                throw new UnsupportedOperationException("not used");
            }

            @Override
            public Name composeName(Name name, Name prefix) {
                throw new UnsupportedOperationException("not used");
            }

            @Override
            public String composeName(String name, String prefix) {
                throw new UnsupportedOperationException("not used");
            }
        }
    }
}

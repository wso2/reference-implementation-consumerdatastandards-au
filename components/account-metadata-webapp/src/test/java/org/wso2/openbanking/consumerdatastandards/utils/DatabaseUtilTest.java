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

package org.wso2.openbanking.consumerdatastandards.utils;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.openbanking.consumerdatastandards.utils.connection.provider.DatabaseConnectionProvider;

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

public class DatabaseUtilTest {

    private static final String INITIAL_CONTEXT_FACTORY_KEY = "java.naming.factory.initial";
    private static final String ORIGINAL_CONTEXT_FACTORY = System.getProperty(INITIAL_CONTEXT_FACTORY_KEY);

    @BeforeClass
    public void setUp() {
        DataSource dataSource = Mockito.mock(DataSource.class);
        Connection connection = Mockito.mock(Connection.class);
        try {
            Mockito.when(dataSource.getConnection()).thenReturn(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        TestInitialContextFactory.setDataSource(dataSource);
        System.setProperty(INITIAL_CONTEXT_FACTORY_KEY, TestInitialContextFactory.class.getName());
    }

    @AfterClass
    public void tearDown() {
        if (ORIGINAL_CONTEXT_FACTORY == null) {
            System.clearProperty(INITIAL_CONTEXT_FACTORY_KEY);
        } else {
            System.setProperty(INITIAL_CONTEXT_FACTORY_KEY, ORIGINAL_CONTEXT_FACTORY);
        }
    }

    @Test
    public void testDatabaseUtilGetConnection() throws Exception {
        Connection connection = DatabaseUtil.getConnection();
        Assert.assertNotNull(connection);
    }

    @Test
    public void testDatabaseConnectionProviderDelegates() throws Exception {
        DatabaseConnectionProvider provider = new DatabaseConnectionProvider();
        Connection connection = provider.getConnection();
        Assert.assertNotNull(connection);
    }

    public static class TestInitialContextFactory implements InitialContextFactory {

        private static DataSource dataSource;

        public static void setDataSource(DataSource dataSource) {
            TestInitialContextFactory.dataSource = dataSource;
        }

        @Override
        public Context getInitialContext(Hashtable<?, ?> environment) {
            return new TestContext();
        }

        private static class TestContext implements Context {

            @Override
            public Object lookup(String name) {
                return dataSource;
            }

            @Override
            public Object lookup(Name name) {
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

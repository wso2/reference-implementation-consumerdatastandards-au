/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.cds.test.framework.utility

import org.wso2.cds.test.framework.configuration.AUConfigurationService

import java.lang.reflect.InvocationTargetException
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement

import static java.lang.System.err
import static java.lang.System.exit

/**
 * Class contains methods for DB Connections.
 */
class DbConnection {

    static Statement stmt;
    static Connection con;
    static String dbUrl;
    static int metricsValue;
    static AUConfigurationService auConfiguration = new AUConfigurationService();

    /**
     * Method to connect with DataBase.
     * @param databaseName - Name of the Database
     * @return Statement object to send the SQL statement to the Database
     */
    public static Statement dbConnection (String databaseName) {
        try {
            String dbUrl = generateDbUrl(databaseName);
            String username = auConfiguration.getDbUsername();
            String password = auConfiguration.getDbPassword();
            String driverClass = auConfiguration.getDbDriverClass();

            // Database connection
            String dbClass = driverClass;
            Class.forName(dbClass).getDeclaredConstructor().newInstance();

            // Get connection to DB
            con = DriverManager.getConnection(dbUrl, username, password);

            // Statement object to send the SQL statement to the Database
            stmt = con.createStatement();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return stmt;
    }

    /**
     * Method to retrieve records from a particular database.
     * @param databaseName
     * @param query
     * @return query response
     */
    public static Object[][] executeSelectQuery(String databaseName, String query) {

        List<Object[]> resultList = new ArrayList<>();

        //Create DB Connection
        dbConnection(databaseName);

        try {
            // Execute a query
            ResultSet resultSet = stmt.executeQuery(query);

            // Get the number of columns in the result set
            int columnCount = resultSet.getMetaData().getColumnCount();

            // Iterate through the result set and generate the list
            while (resultSet.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = resultSet.getObject(i);
                }
                resultList.add(row);
            }

            // Convert the list to a 2D array
            Object[][] resultArray = new Object[resultList.size()][columnCount];
            for (int i = 0; i < resultList.size(); i++) {
                resultArray[i] = resultList.get(i);
            }

            // Close the result set and connection
            stmt.close();
            con.close();
            return resultArray;

        } catch (SQLException ex) {
            err.println("Transaction rollback with error: " + ex);
            exit(0);
            return null;
        }
    }

    /**
     * Generate DB URL based on the provided DB Type.
     * @param databaseName Database
     * @return dbUrl
     */
    public static String generateDbUrl(String databaseName) {

        String dbType = auConfiguration.getDbType();
        String dbServerHost = auConfiguration.getDbServerHost();

        switch (dbType) {
            case "MySQL":
            case "mysql":
                dbUrl = "jdbc:mysql://" + dbServerHost + ":3306/" + databaseName + "?autoReconnect=true&amp&useSSL=false";
                break;
            case "MSSQL":
            case "mssql":
                dbUrl = "jdbc:sqlserver://" + dbServerHost + ":1433;databaseName=" + databaseName + ";encrypt=false";
                break;
            case "Oracle":
            case "oracle":
//				dbUrl = "jdbc:oracle:thin:@" + dbServerHost + ":1521/" + auConfiguration.getOracleDbSid() + "";
                break;
            case "PostgreSQL":
            case "postgresql":
                dbUrl = "jdbc:postgresql://" + dbServerHost + ":5432/" + databaseName + "";
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + dbType);
        }
        return dbUrl;
    }
}

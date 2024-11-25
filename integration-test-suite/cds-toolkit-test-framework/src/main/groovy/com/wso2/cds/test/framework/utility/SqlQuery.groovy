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

package com.wso2.cds.test.framework.utility

import com.wso2.cds.test.framework.configuration.AUConfigurationService

/**
 * Class contains the SQL queries related to testing.
 */
class SqlQuery {

    /**
     * Retrieve Records from API_INVOCATION_RAW_DATA Within Specified Period.
     * @param executionStartTime
     * @param executionEndTime
     * @return results set
     */
    static String retrieveRecordsWithinSpecifiedPeriod(long executionStartTime, long executionEndTime) {

        AUConfigurationService auConfiguration = new AUConfigurationService()
        String dbType = auConfiguration.getDbType()
        def query

        switch (dbType) {
            case "MySQL":
            case "mysql":
                query = "SELECT alr.RESPONSE_LATENCY as RESPONSE_TIME, api.ELECTED_RESOURCE, api.STATUS_CODE, api.CUSTOMER_STATUS \n" +
                        "FROM openbank_ob_reporting_statsdb.API_INVOCATION_RAW_DATA api, " +
                        "openbank_ob_reporting_statsdb.API_LATENCY_RAW_DATA alr\n" +
                        "WHERE api.MESSAGE_ID = alr.MESSAGE_ID AND api.`TIMESTAMP` BETWEEN \"$executionStartTime\" " +
                        "AND \"$executionEndTime\" \n" +
                        "ORDER BY api.`TIMESTAMP` DESC;"
                break
            default:
                throw new IllegalStateException("Unexpected value: " + dbType)
                break
        }
        return query
    }

    /**
     * Outage Details from SERVER_OUTAGES_RAW_DATA.
     * @param outageStartTime
     * @param outageEndTime
     * @return results set
     */
    static String retrieveOutageDetails(long outageStartTime, long outageEndTime) {

        AUConfigurationService auConfiguration = new AUConfigurationService()
        String dbType = auConfiguration.getDbType()
        def query

        switch (dbType) {
            case "MySQL":
            case "mysql":
                query = "SELECT x.TYPE, x.ASPECT, x.TIME_TO - x.TIME_FROM as OUTAGE_TIME \n" +
                        "FROM openbank_ob_reporting_statsdb.SERVER_OUTAGES_RAW_DATA x \n" +
                        "WHERE x.TIME_TO BETWEEN \"$outageStartTime\" AND \"$outageEndTime\" " +
                        "ORDER BY x.TIME_FROM DESC;"
                break
            default:
                throw new IllegalStateException("Unexpected value: " + dbType)
                break
        }
        return query
    }
}

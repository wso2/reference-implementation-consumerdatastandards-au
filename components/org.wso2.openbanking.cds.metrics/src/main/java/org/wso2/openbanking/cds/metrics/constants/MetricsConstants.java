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

package org.wso2.openbanking.cds.metrics.constants;

import java.math.BigDecimal;

/**
 * CDS Metrics management constants.
 */
public class MetricsConstants {

    private MetricsConstants() {
    }

    public static final String RECORDS = "records";
    public static final String METRICS_VERSION_5 = "5";

    // customer profile
    public static final String INDIVIDUAL = "individual";
    public static final String NON_INDIVIDUAL = "non-individual";

    // sp api endpoint
    public static final String SP_API_PATH = "/stores/query";
    public static final String REST_API_URL_KEY = "stream.processor.rest.api.url";
    public static final String SP_USERNAME_KEY = "stream.processor.rest.api.username";
    public static final String SP_PASSWORD_KEY = "stream.processor.rest.api.password";

    //siddhi-app constants
    public static final String CDS_INVOCATION_METRICS_APP = "CDSInvocationMetricsApp";
    public static final String CDS_AVAILABILITY_METRICS_APP = "CDSAvailabilityMetricsApp";
    public static final String CDS_SESSION_METRICS_APP = "CDSSessionMetricsApp";
    public static final String CDS_CUSTOMER_RECIPIENT_METRICS_APP = "CDSCustomerRecipientMetricsApp";
    public static final String API_RAW_DATA_SUBMISSION_APP = "APIRawDataSubmissionApp";
    public static final String CDS_AUTHORISATION_METRICS_APP = "CDSAuthorisationMetricsApp";

    //date-time constants
    public static final String SP_TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss XXX";
    public static final String REQUEST_TIMESTAMP_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    public static final BigDecimal SECONDS_IN_DAY = BigDecimal.valueOf(86400); // 60 * 60 * 24

    //peak TPS metrics constants
    public static final String EVENT = "event";
    public static final String ASPECT = "aspect";
    public static final String TOTAL_COUNT = "total_count";
    public static final String TIMESTAMP = "TIMESTAMP";

    //availability constants
    public static final String SCHEDULED_OUTAGE = "scheduled";
    public static final String INCIDENT_OUTAGE = "incident";

    // metrics logging constants
    public static final String ASYNC_FETCH_ERROR = "Error occurred while asynchronously fetching %s metrics.";
    public static final String RETRIEVAL_ERROR = "Error occurred while retrieving %s data from analytics server.";
    public static final String NO_DATA_ERROR = "No data returned from the analytics server for %s metrics.";
    public static final String AVAILABILITY = "availability";
    public static final String INVOCATION = "invocation";
    public static final String PERFORMANCE = "performance";
    public static final String SESSION_COUNT = "session count";
    public static final String AVERAGE_TPS = "average TPS";
    public static final String PEAK_TPS = "peak TPS";
    public static final String ERROR = "error";
    public static final String REJECTION = "rejection";
    public static final String RECIPIENT_COUNT = "recipient count";
    public static final String CUSTOMER_COUNT = "customer count";
    public static final String SUCCESSFUL_INVOCATIONS = "successful invocations";
    public static final String TOTAL_RESPONSE_TIME = "total response time";
    public static final String AVERAGE_RESPONSE_TIME = "average response time";
    public static final String AUTHORISATION = "authorisation";

}

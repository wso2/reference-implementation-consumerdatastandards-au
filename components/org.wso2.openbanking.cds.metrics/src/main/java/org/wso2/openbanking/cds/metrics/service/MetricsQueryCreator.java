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

package org.wso2.openbanking.cds.metrics.service;

import net.minidev.json.JSONObject;

/**
 * Interface of Metrics Query Creator.
 */
public interface MetricsQueryCreator {

    /**
     * Returns the query for getting availability metrics data.
     *
     * @return Query as a String
     */
    String getAvailabilityMetricsQuery();

    /**
     * Returns the query for getting api invocation data.
     *
     * @return Query as a String
     */
    String getInvocationMetricsQuery();

    /**
     * Returns the query for getting api invocation data by aspect.
     *
     * @return Query as a String
     */
    String getInvocationByAspectMetricsQuery();

    /**
     * Returns the query for getting hourly performance by priority data.
     *
     * @return Query as a String
     */
    String getHourlyPerformanceByPriorityMetricsQuery();

    /**
     * Returns the query for getting session count.
     *
     * @return Query as a String
     */
    String getSessionCountMetricsQuery();

    /**
     * Returns the query for getting peak tps.
     *
     * @return Query as a String
     */
    String getPeakTPSMetricsQuery();

    /**
     * Returns the event for getting peak tps from siddhi app.
     *
     * @return Query as a String
     */
    JSONObject getPeakTPSMetricsEvent();

    /**
     * Return the query for getting error data.
     *
     * @return Query as a String
     */
    String getErrorMetricsQuery();

    /**
     * Return the query for getting error by aspect data.
     *
     * @return Query as a String
     */
    String getErrorByAspectMetricsQuery();

    /**
     * Return the query for getting rejection data (throttled out requests).
     *
     * @return Query as a String
     */
    String getRejectionMetricsQuery();

    /**
     * Return the query for getting active authorisation count data.
     *
     * @return Query as a String
     */
    String getActiveAuthorisationCountMetricsQuery();

    /**
     * Return the query for getting authorisation data.
     *
     * @return Query as a String
     */
    String getAuthorisationMetricsQuery();

    /**
     * Return the query for getting abandoned consent flow count data.
     *
     * @return Query as a String
     */
    String getAbandonedConsentFlowCountMetricsQuery();

    /**
     * Returns the query for getting data recipient count.
     *
     * @return Query as a String
     */
    String getRecipientCountMetricsQuery();

    /**
     * Returns the query for getting customer count with active authorizations.
     *
     * @return Query as a String
     */
    String getCustomerCountMetricsQuery();

    /**
     * Returns the query for getting successful invocations count.
     *
     * @return Query as a String
     */
    String getSuccessfulInvocationsQuery();

    /**
     * Returns the query for getting response time data.
     *
     * @return Query as a String
     */
    String getTotalResponseTimeQuery();

}

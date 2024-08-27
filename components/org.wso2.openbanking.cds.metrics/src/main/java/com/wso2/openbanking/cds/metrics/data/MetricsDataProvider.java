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

package org.wso2.openbanking.cds.metrics.data;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.ParseException;

/**
 * Interface for providing metrics data.
 * The expected JSON structure for each data type can be found in MockMetricsDataProvider class.
 * This interface should be implemented if there is a requirement to support a new analytics engine.
 */
public interface MetricsDataProvider {

    JSONObject getAvailabilityMetricsData() throws OpenBankingException;

    JSONObject getInvocationMetricsData() throws OpenBankingException;

    JSONObject getHourlyPerformanceByPriorityMetricsData() throws OpenBankingException;

    JSONObject getInvocationByAspectMetricsData() throws OpenBankingException;

    JSONObject getSessionCountMetricsData() throws OpenBankingException;

    JSONArray getPeakTPSMetricsData() throws ParseException;

    JSONObject getErrorMetricsData() throws OpenBankingException;

    JSONObject getErrorByAspectMetricsData() throws OpenBankingException;

    JSONObject getRejectionMetricsData() throws OpenBankingException;

    JSONObject getActiveAuthorisationCountMetricsData() throws OpenBankingException;

    JSONObject getAuthorisationMetricsData() throws OpenBankingException;

    JSONObject getAbandonedConsentFlowCountMetricsData() throws OpenBankingException;

    JSONObject getRecipientCountMetricsData() throws OpenBankingException;

    JSONObject getCustomerCountMetricsData() throws OpenBankingException;

    JSONObject getTotalResponseTimeMetricsData() throws OpenBankingException;

    JSONObject getSuccessfulInvocationMetricsData() throws OpenBankingException;
}

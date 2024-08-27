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

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import org.wso2.openbanking.cds.metrics.model.AbandonedConsentFlowByStageMetricDay;
import org.wso2.openbanking.cds.metrics.model.AuthorisationMetricDay;
import org.wso2.openbanking.cds.metrics.model.ErrorMetricDay;
import org.wso2.openbanking.cds.metrics.util.AspectEnum;
import org.wso2.openbanking.cds.metrics.util.PriorityEnum;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Class containing methods for processing metrics data.
 */
public interface MetricsProcessor {

    /**
     * Get availability metrics.
     *
     * @return Map of availability metrics
     * @throws OpenBankingException - OpenBankingException
     */
    Map<AspectEnum, List<BigDecimal>> getAvailabilityMetrics() throws OpenBankingException;

    /**
     * Get invocation metrics.
     *
     * @return Map of invocation metrics with priority tiers
     * @throws OpenBankingException - OpenBankingException
     */
    Map<PriorityEnum, List<Integer>> getInvocationMetrics() throws OpenBankingException;

    /**
     * Get performance metrics.
     *
     * @param invocationMetricsMap - Map of invocation metrics
     * @return List of performance metrics
     * @throws OpenBankingException - OpenBankingException
     */
    List<BigDecimal> getPerformanceMetrics(Map<PriorityEnum, List<Integer>> invocationMetricsMap)
            throws OpenBankingException;

    /**
     * Get hourly performance by priority metrics.
     *
     * @return Map of hourly performance metrics with priority tiers
     * @throws OpenBankingException - OpenBankingException
     */
    Map<PriorityEnum, List<List<BigDecimal>>> getHourlyPerformanceByPriorityMetrics() throws OpenBankingException;

    /**
     * Get average response time metrics.
     *
     * @param invocationMetricsMap - Map of invocation metrics
     * @return Map of average response time metrics with priority tiers
     * @throws OpenBankingException - OpenBankingException
     */
    Map<PriorityEnum, List<BigDecimal>> getAverageResponseTimeMetrics(
            Map<PriorityEnum, List<Integer>> invocationMetricsMap) throws OpenBankingException;

    /**
     * Get session count metrics.
     *
     * @return List of session count metrics
     * @throws OpenBankingException - OpenBankingException
     */
    List<Integer> getSessionCountMetrics() throws OpenBankingException;

    /**
     * Get average TPS metrics.
     * Used formula: averageTPS = (total no. of transactions for a day / no. of seconds in a day)
     *
     * @return Map of average TPS
     * @throws OpenBankingException - OpenBankingException
     */
    Map<AspectEnum, List<BigDecimal>> getAverageTPSMetrics() throws OpenBankingException;

    /**
     * Get peak TPS metrics.
     *
     * @return Map of peak TPS metrics
     * @throws OpenBankingException - OpenBankingException
     */
    Map<AspectEnum, List<BigDecimal>> getPeakTPSMetrics() throws OpenBankingException;

    /**
     * Get error metrics.
     *
     * @return List of error metrics
     * @throws OpenBankingException - OpenBankingException
     */
    List<Integer> getErrorMetrics() throws OpenBankingException;

    /**
     * Get error by aspect metrics.
     *
     * @return List of error by aspect metrics
     * @throws OpenBankingException - OpenBankingException
     */
    List<ErrorMetricDay> getErrorByAspectMetrics() throws OpenBankingException;

    /**
     * Get rejection metrics.
     *
     * @return Map of rejection metrics with aspect tiers
     * @throws OpenBankingException - OpenBankingException
     */
    Map<AspectEnum, List<Integer>> getRejectionMetrics() throws OpenBankingException;

    /**
     * Get active authorisation count metrics.
     *
     * @return Map of active authorisation count metrics
     * @throws OpenBankingException - OpenBankingException
     */
    Map<String, Integer> getActiveAuthorisationCountMetrics() throws OpenBankingException;

    /**
     * Get authorisation metrics.
     *
     * @return List of authorisation metrics
     * @throws OpenBankingException - OpenBankingException
     */
    List<AuthorisationMetricDay> getAuthorisationMetrics() throws OpenBankingException;

    /**
     * Get abandoned consent flow count metrics.
     *
     * @return List of abandoned consent flow count metrics
     * @throws OpenBankingException - OpenBankingException
     */
    List<AbandonedConsentFlowByStageMetricDay> getAbandonedConsentFlowCountMetrics() throws OpenBankingException;

    /**
     * Get recipient count metrics.
     *
     * @return count of data recipients
     * @throws OpenBankingException - OpenBankingException
     */
    int getRecipientCountMetrics() throws OpenBankingException;

    /**
     * Get customer count metrics.
     *
     * @return count of customers with active authorizations
     * @throws OpenBankingException - OpenBankingException
     */
    int getCustomerCountMetrics() throws OpenBankingException;

}

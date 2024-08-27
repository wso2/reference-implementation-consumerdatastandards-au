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

package org.wso2.openbanking.cds.metrics.util;

import org.wso2.openbanking.cds.metrics.constants.MetricsConstants;
import org.wso2.openbanking.cds.metrics.model.MetricsResponseModel;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Util methods used in Metrics Service.
 */
public class MetricsServiceUtil {

    /**
     * Append historic metrics to current day metrics.
     *
     * @param currentDayMetrics - current day metrics
     * @param historicMetrics   - historic metrics
     */
    public static void appendHistoricMetricsToCurrentDayMetrics(
            MetricsResponseModel currentDayMetrics, MetricsResponseModel historicMetrics) {

        currentDayMetrics.getSessionCount().addAll(historicMetrics.getSessionCount());
        appendAvailabilityMetrics(currentDayMetrics, historicMetrics);
        appendPerformanceMetrics(currentDayMetrics, historicMetrics);
        appendInvocationMetrics(currentDayMetrics, historicMetrics);
        appendAverageTPSMetrics(currentDayMetrics, historicMetrics);
        appendPeakTPSMetrics(currentDayMetrics, historicMetrics);
        appendErrorMetrics(currentDayMetrics, historicMetrics);
        appendAverageResponseMetrics(currentDayMetrics, historicMetrics);
        appendRejectionMetrics(currentDayMetrics, historicMetrics);
        appendAuthorisationMetrics(currentDayMetrics, historicMetrics);
    }

    /**
     * Append historic availability metrics to current day availability metrics.
     *
     * @param currentDayMetrics - current day metrics
     * @param historicMetrics   - historic metrics
     */
    private static void appendAvailabilityMetrics(
            MetricsResponseModel currentDayMetrics, MetricsResponseModel historicMetrics) {

        currentDayMetrics.getAvailability().addAll(historicMetrics.getAvailability());
        currentDayMetrics.getAuthenticatedAvailability().addAll(historicMetrics.getAuthenticatedAvailability());
        currentDayMetrics.getUnauthenticatedAvailability().addAll(historicMetrics.getUnauthenticatedAvailability());
    }

    /**
     * Append historic performance metrics to current day performance metrics.
     *
     * @param currentDayMetrics - current day metrics
     * @param historicMetrics   - historic metrics
     */
    private static void appendPerformanceMetrics(
            MetricsResponseModel currentDayMetrics, MetricsResponseModel historicMetrics) {

        currentDayMetrics.getPerformance().addAll(historicMetrics.getPerformance());
        currentDayMetrics.getPerformanceHighPriority().addAll(historicMetrics.getPerformanceHighPriority());
        currentDayMetrics.getPerformanceLowPriority().addAll(historicMetrics.getPerformanceLowPriority());
        currentDayMetrics.getPerformanceUnattended().addAll(historicMetrics.getPerformanceUnattended());
        currentDayMetrics.getPerformanceUnauthenticated().addAll(historicMetrics.getPerformanceUnauthenticated());
        currentDayMetrics.getPerformanceLargePayload().addAll(historicMetrics.getPerformanceLargePayload());
    }

    /**
     * Append historic invocation metrics to current day invocation metrics.
     *
     * @param currentDayMetrics - current day metrics
     * @param historicMetrics   - historic metrics
     */
    private static void appendInvocationMetrics(
            MetricsResponseModel currentDayMetrics, MetricsResponseModel historicMetrics) {

        currentDayMetrics.getInvocationHighPriority().addAll(historicMetrics.getInvocationHighPriority());
        currentDayMetrics.getInvocationLowPriority().addAll(historicMetrics.getInvocationLowPriority());
        currentDayMetrics.getInvocationUnattended().addAll(historicMetrics.getInvocationUnattended());
        currentDayMetrics.getInvocationUnauthenticated().addAll(historicMetrics.getInvocationUnauthenticated());
        currentDayMetrics.getInvocationLargePayload().addAll(historicMetrics.getInvocationLargePayload());
    }

    /**
     * Append historic average TPS metrics to current day average TPS metrics.
     *
     * @param currentDayMetrics - current day metrics
     * @param historicMetrics   - historic metrics
     */
    private static void appendAverageTPSMetrics(
            MetricsResponseModel currentDayMetrics, MetricsResponseModel historicMetrics) {

        currentDayMetrics.getAverageTPS().addAll(historicMetrics.getAverageTPS());
        currentDayMetrics.getAuthenticatedAverageTPS().addAll(historicMetrics.getAuthenticatedAverageTPS());
        currentDayMetrics.getUnauthenticatedAverageTPS().addAll(historicMetrics.getUnauthenticatedAverageTPS());
    }

    /**
     * Append historic peak TPS metrics to current day peak TPS metrics.
     *
     * @param currentDayMetrics - current day metrics
     * @param historicMetrics   - historic metrics
     */
    private static void appendPeakTPSMetrics(
            MetricsResponseModel currentDayMetrics, MetricsResponseModel historicMetrics) {

        currentDayMetrics.getPeakTPS().addAll(historicMetrics.getPeakTPS());
        currentDayMetrics.getAuthenticatedPeakTPS().addAll(historicMetrics.getAuthenticatedPeakTPS());
        currentDayMetrics.getUnauthenticatedPeakTPS().addAll(historicMetrics.getUnauthenticatedPeakTPS());
    }

    /**
     * Append historic error metrics to current day error metrics.
     *
     * @param currentDayMetrics - current day metrics
     * @param historicMetrics   - historic metrics
     */
    private static void appendErrorMetrics(
            MetricsResponseModel currentDayMetrics, MetricsResponseModel historicMetrics) {

        currentDayMetrics.getErrors().addAll(historicMetrics.getErrors());
        currentDayMetrics.getAuthenticatedErrors().addAll(historicMetrics.getAuthenticatedErrors());
        currentDayMetrics.getUnauthenticatedErrors().addAll(historicMetrics.getUnauthenticatedErrors());
    }

    /**
     * Append historic average response metrics to current day average response metrics.
     *
     * @param currentDayMetrics - current day metrics
     * @param historicMetrics   - historic metrics
     */
    private static void appendAverageResponseMetrics(
            MetricsResponseModel currentDayMetrics, MetricsResponseModel historicMetrics) {

        currentDayMetrics.getAverageResponseUnauthenticated().addAll(historicMetrics.
                getAverageResponseUnauthenticated());
        currentDayMetrics.getAverageResponseHighPriority().addAll(historicMetrics.getAverageResponseHighPriority());
        currentDayMetrics.getAverageResponseLowPriority().addAll(historicMetrics.getAverageResponseLowPriority());
        currentDayMetrics.getAverageResponseUnattended().addAll(historicMetrics.getAverageResponseUnattended());
        currentDayMetrics.getAverageResponseLargePayload().addAll(historicMetrics.getAverageResponseLargePayload());
    }

    /**
     * Append historic rejection metrics to current day rejection metrics.
     *
     * @param currentDayMetrics - current day metrics
     * @param historicMetrics   - historic metrics
     */
    private static void appendRejectionMetrics(
            MetricsResponseModel currentDayMetrics, MetricsResponseModel historicMetrics) {

        currentDayMetrics.getAuthenticatedEndpointRejections().addAll(historicMetrics.
                getAuthenticatedEndpointRejections());
        currentDayMetrics.getUnauthenticatedEndpointRejections().addAll(historicMetrics.
                getUnauthenticatedEndpointRejections());
    }

    /**
     * Append historic authorisation metrics to current day authorisation metrics.
     *
     * @param currentDayMetrics - current day metrics
     * @param historicMetrics   - historic metrics
     */
    private static void appendAuthorisationMetrics(
            MetricsResponseModel currentDayMetrics, MetricsResponseModel historicMetrics) {

        currentDayMetrics.getNewAuthorisationCount().addAll(historicMetrics.getNewAuthorisationCount());
        currentDayMetrics.getAmendedAuthorisationCount().addAll(historicMetrics.getAmendedAuthorisationCount());
        currentDayMetrics.getExpiredAuthorisationCount().addAll(historicMetrics.getExpiredAuthorisationCount());
        currentDayMetrics.getRevokedAuthorisationCount().addAll(historicMetrics.getRevokedAuthorisationCount());

        currentDayMetrics.getAbandonedConsentFlowCount().addAll(historicMetrics.getAbandonedConsentFlowCount());

        currentDayMetrics.getPreIdentificationAbandonedConsentFlowCount().addAll(historicMetrics
                .getPreIdentificationAbandonedConsentFlowCount());
        currentDayMetrics.getPreAuthenticationAbandonedConsentFlowCount().addAll(historicMetrics
                .getPreAuthenticationAbandonedConsentFlowCount());
        currentDayMetrics.getPreAccountSelectionAbandonedConsentFlowCount().addAll(historicMetrics
                .getPreAccountSelectionAbandonedConsentFlowCount());
        currentDayMetrics.getPreAuthorisationAbandonedConsentFlowCount().addAll(historicMetrics
                .getPreAuthorisationAbandonedConsentFlowCount());
        currentDayMetrics.getRejectedAbandonedConsentFlowCount().addAll(historicMetrics
                .getRejectedAbandonedConsentFlowCount());
        currentDayMetrics.getFailedTokenExchangeAbandonedConsentFlowCount().addAll(historicMetrics
                .getFailedTokenExchangeAbandonedConsentFlowCount());
    }

    /**
     * Check whether the response model is expired using the requestTime parameter.
     *
     * @param metricsResponseModel - MetricsResponseModel
     * @return boolean - whether the response model is expired
     */
    public static boolean isResponseModelExpired(MetricsResponseModel metricsResponseModel) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(MetricsConstants.REQUEST_TIMESTAMP_PATTERN);
        LocalDate requestDate = ZonedDateTime.parse(metricsResponseModel.getRequestTime(), formatter).toLocalDate();
        return requestDate.isBefore(LocalDate.now());
    }
}

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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.cds.metrics.constants.MetricsConstants;
import org.wso2.openbanking.cds.metrics.model.AbandonedConsentFlowByStageMetricDay;
import org.wso2.openbanking.cds.metrics.model.AuthorisationMetricDay;
import org.wso2.openbanking.cds.metrics.model.ErrorMetricDay;
import org.wso2.openbanking.cds.metrics.model.MetricsResponseModel;
import org.wso2.openbanking.cds.metrics.util.AspectEnum;
import org.wso2.openbanking.cds.metrics.util.PriorityEnum;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.wso2.openbanking.cds.metrics.constants.MetricsConstants.ASYNC_FETCH_ERROR;

/**
 * This class will initialize a metrics processor based on the period (Current/Historic/All) and
 * calculate the metrics asynchronously.
 * <p>
 * The asynchronous calculation is introduced to overcome the performance
 * degrade caused by multiple http calls to the analytics server.
 * Errors that occur during the asynchronous calculation of metrics are handled by the
 * handleMetricsFutureCompletionErrors method.
 */
public class MetricsV5FetcherImpl implements MetricsFetcher {

    MetricsProcessor metricsProcessor;
    private static final Log log = LogFactory.getLog(MetricsV5FetcherImpl.class);

    private CompletableFuture<Map<AspectEnum, List<BigDecimal>>> availabilityFuture;
    private CompletableFuture<Map<PriorityEnum, List<Integer>>> invocationFuture;
    private CompletableFuture<List<Integer>> sessionCountFuture;
    private CompletableFuture<Map<AspectEnum, List<BigDecimal>>> peakTPSFuture;
    private CompletableFuture<List<Integer>> errorFuture;
    private CompletableFuture<List<ErrorMetricDay>> errorByAspectFuture;
    private CompletableFuture<Map<AspectEnum, List<Integer>>> rejectionFuture;
    private CompletableFuture<Integer> recipientCountFuture;
    private CompletableFuture<Integer> customerCountFuture;
    private CompletableFuture<Map<AspectEnum, List<BigDecimal>>> averageTPSFuture;
    private CompletableFuture<List<BigDecimal>> performanceFuture;
    private CompletableFuture<Map<PriorityEnum, List<List<BigDecimal>>>> hourlyPerformanceByPriorityFuture;
    private CompletableFuture<Map<PriorityEnum, List<BigDecimal>>> averageResponseTimeFuture;
    private CompletableFuture<Map<String, Integer>> activeAuthorisationCountFuture;
    private CompletableFuture<List<AuthorisationMetricDay>> authorisationFuture;
    private CompletableFuture<List<AbandonedConsentFlowByStageMetricDay>> abandonedConsentFlowCountFuture;

    public MetricsV5FetcherImpl(MetricsProcessor metricsProcessor) throws OpenBankingException {
        this.metricsProcessor = metricsProcessor;
    }

    @Override
    public MetricsResponseModel getResponseMetricsListModel(String requestTime) throws OpenBankingException {
        MetricsResponseModel metricsResponseModel = new MetricsResponseModel(requestTime);

        // Fetch metrics asynchronously
        availabilityFuture = fetchAvailabilityMetricsAsync();
        invocationFuture = fetchInvocationMetricsAsync();
        sessionCountFuture = fetchSessionCountMetricsAsync();
        averageTPSFuture = fetchAverageTPSAsync();
        peakTPSFuture = fetchPeakTPSMetricsAsync();
        errorFuture = fetchErrorMetricsAsync();
        errorByAspectFuture = fetchErrorByAspectMetricsAsync();
        rejectionFuture = fetchRejectionMetricsAsync();
        recipientCountFuture = fetchRecipientCountMetricsAsync();
        customerCountFuture = fetchCustomerCountMetricsAsync();
        hourlyPerformanceByPriorityFuture = fetchHourlyPerformanceByPriorityMetricsAsync();
        activeAuthorisationCountFuture = fetchActiveAuthorisationCountMetricsAsync();
        authorisationFuture = fetchAuthorisationMetricsAsync();
        abandonedConsentFlowCountFuture = fetchAbandonedConsentFlowCountMetricsAsync();
        // Dependent futures that require results from the invocationFuture
        performanceFuture = fetchPerformanceMetricsAsync(invocationFuture);
        averageResponseTimeFuture = fetchAverageResponseTimeAsync(invocationFuture);

        populateMetricsModel(metricsResponseModel);
        return metricsResponseModel;
    }

    /**
     * Get availability metrics asynchronously.
     *
     * @return CompletableFuture of availability metrics
     */
    private CompletableFuture<Map<AspectEnum, List<BigDecimal>>> fetchAvailabilityMetricsAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return metricsProcessor.getAvailabilityMetrics();
            } catch (OpenBankingException e) {
                String errorMessage = String.format(ASYNC_FETCH_ERROR, MetricsConstants.AVAILABILITY);
                log.debug(errorMessage, e);
                throw new RuntimeException(errorMessage, e);
            }
        });
    }

    /**
     * Get invocation metrics asynchronously.
     *
     * @return CompletableFuture of invocation metrics
     */
    private CompletableFuture<Map<PriorityEnum, List<Integer>>> fetchInvocationMetricsAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return metricsProcessor.getInvocationMetrics();
            } catch (OpenBankingException e) {
                String errorMessage = String.format(ASYNC_FETCH_ERROR, MetricsConstants.INVOCATION);
                log.debug(errorMessage, e);
                throw new RuntimeException(errorMessage, e);
            }
        });
    }

    /**
     * Get session count metrics asynchronously.
     *
     * @return CompletableFuture of session count metrics
     */
    private CompletableFuture<List<Integer>> fetchSessionCountMetricsAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return metricsProcessor.getSessionCountMetrics();
            } catch (OpenBankingException e) {
                String errorMessage = String.format(ASYNC_FETCH_ERROR, MetricsConstants.SESSION_COUNT);
                log.debug(errorMessage, e);
                throw new RuntimeException(errorMessage, e);
            }
        });
    }

    /**
     * Get peak TPS metrics asynchronously.
     *
     * @return CompletableFuture of peak TPS metrics
     */
    private CompletableFuture<Map<AspectEnum, List<BigDecimal>>> fetchPeakTPSMetricsAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return metricsProcessor.getPeakTPSMetrics();
            } catch (OpenBankingException e) {
                String errorMessage = String.format(ASYNC_FETCH_ERROR, MetricsConstants.PEAK_TPS);
                log.debug(errorMessage, e);
                throw new RuntimeException(errorMessage, e);
            }
        });
    }

    /**
     * Get error metrics asynchronously.
     *
     * @return CompletableFuture of error metrics
     */
    private CompletableFuture<List<Integer>> fetchErrorMetricsAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return metricsProcessor.getErrorMetrics();
            } catch (OpenBankingException e) {
                String errorMessage = String.format(ASYNC_FETCH_ERROR, MetricsConstants.ERROR);
                log.debug(errorMessage, e);
                throw new RuntimeException(errorMessage, e);
            }
        });
    }

    /**
     * Get error by aspect metrics asynchronously.
     *
     * @return CompletableFuture of error by aspect metrics
     */
    private CompletableFuture<List<ErrorMetricDay>> fetchErrorByAspectMetricsAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return metricsProcessor.getErrorByAspectMetrics();
            } catch (OpenBankingException e) {
                String errorMessage = String.format(ASYNC_FETCH_ERROR, MetricsConstants.ERROR);
                log.debug(errorMessage, e);
                throw new RuntimeException(errorMessage, e);
            }
        });
    }

    /**
     * Get rejection metrics asynchronously.
     *
     * @return CompletableFuture of rejection metrics
     */
    private CompletableFuture<Map<AspectEnum, List<Integer>>> fetchRejectionMetricsAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return metricsProcessor.getRejectionMetrics();
            } catch (OpenBankingException e) {
                String errorMessage = String.format(ASYNC_FETCH_ERROR, MetricsConstants.REJECTION);
                log.debug(errorMessage, e);
                throw new RuntimeException(errorMessage, e);
            }
        });
    }

    /**
     * Get recipient count metrics asynchronously.
     *
     * @return CompletableFuture of recipient count metrics
     */
    private CompletableFuture<Integer> fetchRecipientCountMetricsAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return metricsProcessor.getRecipientCountMetrics();
            } catch (OpenBankingException e) {
                String errorMessage = String.format(ASYNC_FETCH_ERROR, MetricsConstants.RECIPIENT_COUNT);
                log.debug(errorMessage, e);
                throw new RuntimeException(errorMessage, e);
            }
        });
    }

    /**
     * Get customer count metrics asynchronously.
     *
     * @return CompletableFuture of customer count metrics
     */
    private CompletableFuture<Integer> fetchCustomerCountMetricsAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return metricsProcessor.getCustomerCountMetrics();
            } catch (OpenBankingException e) {
                String errorMessage = String.format(ASYNC_FETCH_ERROR, MetricsConstants.CUSTOMER_COUNT);
                log.debug(errorMessage, e);
                throw new RuntimeException(errorMessage, e);
            }
        });
    }

    /**
     * Get average TPS metrics asynchronously.
     *
     * @return CompletableFuture of average TPS metrics
     */
    private CompletableFuture<Map<AspectEnum, List<BigDecimal>>> fetchAverageTPSAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return metricsProcessor.getAverageTPSMetrics();
            } catch (OpenBankingException e) {
                String errorMessage = String.format(ASYNC_FETCH_ERROR, MetricsConstants.AVERAGE_TPS);
                log.debug(errorMessage, e);
                throw new RuntimeException(errorMessage, e);
            }
        });
    }

    /**
     * Get performance metrics asynchronously.
     *
     * @param invocationFuture - CompletableFuture of invocation metrics
     * @return CompletableFuture of performance metrics
     */
    private CompletableFuture<List<BigDecimal>> fetchPerformanceMetricsAsync(
            CompletableFuture<Map<PriorityEnum, List<Integer>>> invocationFuture) {
        return invocationFuture.thenApplyAsync(invocationMetrics -> {
            try {
                return metricsProcessor.getPerformanceMetrics(invocationMetrics);
            } catch (OpenBankingException e) {
                String errorMessage = String.format(ASYNC_FETCH_ERROR, MetricsConstants.PERFORMANCE);
                log.debug(errorMessage, e);
                throw new RuntimeException(errorMessage, e);
            }
        });
    }

    /**
     * Get hourly performance by priority metrics asynchronously.
     *
     * @return CompletableFuture of hourly performance by priority metrics
     */
    private CompletableFuture<Map<PriorityEnum, List<List<BigDecimal>>>>
    fetchHourlyPerformanceByPriorityMetricsAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return metricsProcessor.getHourlyPerformanceByPriorityMetrics();
            } catch (OpenBankingException e) {
                String errorMessage = String.format(ASYNC_FETCH_ERROR, MetricsConstants.PERFORMANCE);
                log.debug(errorMessage, e);
                throw new RuntimeException(errorMessage, e);
            }
        });
    }

    /**
     * Get average response time metrics asynchronously.
     *
     * @param invocationFuture - CompletableFuture of invocation metrics
     * @return CompletableFuture of average response time metrics
     */
    private CompletableFuture<Map<PriorityEnum, List<BigDecimal>>> fetchAverageResponseTimeAsync(
            CompletableFuture<Map<PriorityEnum, List<Integer>>> invocationFuture) {
        return invocationFuture.thenApplyAsync(invocationMetrics -> {
            try {
                return metricsProcessor.getAverageResponseTimeMetrics(invocationMetrics);
            } catch (OpenBankingException e) {
                String errorMessage = String.format(ASYNC_FETCH_ERROR, MetricsConstants.AVERAGE_RESPONSE_TIME);
                log.debug(errorMessage, e);
                throw new RuntimeException(errorMessage, e);
            }
        });
    }

    /**
     * Get active authorisation count metrics asynchronously.
     *
     * @return CompletableFuture of active authorisation count metrics
     */
    private CompletableFuture<Map<String, Integer>> fetchActiveAuthorisationCountMetricsAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return metricsProcessor.getActiveAuthorisationCountMetrics();
            } catch (OpenBankingException e) {
                String errorMessage = String.format(ASYNC_FETCH_ERROR, MetricsConstants.ERROR);
                log.debug(errorMessage, e);
                throw new RuntimeException(errorMessage, e);
            }
        });
    }

    /**
     * Get authorisation metrics asynchronously.
     *
     * @return CompletableFuture of authorisation metrics
     */
    private CompletableFuture<List<AuthorisationMetricDay>> fetchAuthorisationMetricsAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return metricsProcessor.getAuthorisationMetrics();
            } catch (OpenBankingException e) {
                String errorMessage = String.format(ASYNC_FETCH_ERROR, MetricsConstants.AUTHORISATION);
                log.debug(errorMessage, e);
                throw new RuntimeException(errorMessage, e);
            }
        });
    }

    /**
     * Get abandoned consent flow count metrics asynchronously.
     *
     * @return CompletableFuture of abandoned consent flow count metrics
     */
    private CompletableFuture<List<AbandonedConsentFlowByStageMetricDay>> fetchAbandonedConsentFlowCountMetricsAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return metricsProcessor.getAbandonedConsentFlowCountMetrics();
            } catch (OpenBankingException e) {
                String errorMessage = String.format(ASYNC_FETCH_ERROR, MetricsConstants.AUTHORISATION);
                log.debug(errorMessage, e);
                throw new RuntimeException(errorMessage, e);
            }
        });
    }

    /**
     * Populate the metrics model with the calculated metrics data.
     * Errors occurred during the asynchronous calculation of metrics are also handled here.
     *
     * @param metricsResponseModel - MetricsResponseModel object
     * @throws OpenBankingException - OpenBankingException
     */
    private void populateMetricsModel(MetricsResponseModel metricsResponseModel)
            throws OpenBankingException {

        try {
            metricsResponseModel.setAvailability(availabilityFuture.get());
            metricsResponseModel.setInvocations(invocationFuture.get());
            metricsResponseModel.setSessionCount(sessionCountFuture.get());
            metricsResponseModel.setPeakTPS(peakTPSFuture.get());
            metricsResponseModel.setErrors(errorFuture.get());
            metricsResponseModel.setErrorsByAspect(errorByAspectFuture.get());
            metricsResponseModel.setRejections(rejectionFuture.get());
            metricsResponseModel.setRecipientCount(recipientCountFuture.get());
            metricsResponseModel.setCustomerCount(customerCountFuture.get());
            metricsResponseModel.setAverageTPS(averageTPSFuture.get());
            metricsResponseModel.setPerformance(performanceFuture.get());
            metricsResponseModel.setHourlyPerformanceByPriority(hourlyPerformanceByPriorityFuture.get());
            metricsResponseModel.setAverageResponseTime(averageResponseTimeFuture.get());
            metricsResponseModel.setActiveAuthorisationCount(activeAuthorisationCountFuture.get());
            metricsResponseModel.setAuthorisation(authorisationFuture.get());
            metricsResponseModel.setAbandonedConsentFlow(abandonedConsentFlowCountFuture.get());
        } catch (InterruptedException | ExecutionException e) {
            // Handle errors that occurred during the asynchronous calculation of metrics.
            log.error("Error occurred while calculating metrics. " + e.getMessage(), e);
            throw new OpenBankingException("Failed to populate metrics v5 model with data", e);
        }
    }
}

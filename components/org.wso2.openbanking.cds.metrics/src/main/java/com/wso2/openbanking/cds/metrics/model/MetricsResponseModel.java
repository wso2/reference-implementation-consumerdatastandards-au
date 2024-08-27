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

package org.wso2.openbanking.cds.metrics.model;

import org.wso2.openbanking.cds.metrics.constants.MetricsConstants;
import org.wso2.openbanking.cds.metrics.util.AspectEnum;
import org.wso2.openbanking.cds.metrics.util.PriorityEnum;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Model class for CDS Metrics V5 data.
 * In this model, List<BigDecimal> is used to represent metrics that require representation of data for multiple days.
 * <p>
 * For the CURRENT period, each list will contain a single value representing the metric for the current day.
 * For the HISTORIC period, each list will contain seven elements representing the metrics for the last seven days.
 * For the ALL period, the first element in the list represents the current day and the subsequent elements
 * represent the metrics for the previous 7 days starting from yesterday.
 */
public class MetricsResponseModel {

    private String requestTime;
    private int customerCount;
    private int recipientCount;
    private List<Integer> sessionCount;

    // Availability
    private List<BigDecimal> availability;
    private List<BigDecimal> authenticatedAvailability;
    private List<BigDecimal> unauthenticatedAvailability;

    // Performance
    private List<BigDecimal> performance;
    private List<List<BigDecimal>> performanceUnauthenticated;
    private List<List<BigDecimal>> performanceHighPriority;
    private List<List<BigDecimal>> performanceLowPriority;
    private List<List<BigDecimal>> performanceUnattended;
    private List<List<BigDecimal>> performanceLargePayload;

    // Invocations
    private List<Integer> invocationUnauthenticated;
    private List<Integer> invocationHighPriority;
    private List<Integer> invocationLowPriority;
    private List<Integer> invocationUnattended;
    private List<Integer> invocationLargePayload;

    // Average response
    private List<BigDecimal> averageResponseUnauthenticated;
    private List<BigDecimal> averageResponseHighPriority;
    private List<BigDecimal> averageResponseLowPriority;
    private List<BigDecimal> averageResponseUnattended;
    private List<BigDecimal> averageResponseLargePayload;

    // Average TPS
    private List<BigDecimal> averageTPS;
    private List<BigDecimal> authenticatedAverageTPS;
    private List<BigDecimal> unauthenticatedAverageTPS;

    // Peak TPS
    private List<BigDecimal> peakTPS;
    private List<BigDecimal> authenticatedPeakTPS;
    private List<BigDecimal> unauthenticatedPeakTPS;

    // Errors
    private List<Integer> errors;
    private List<Map<String, Integer>> authenticatedErrors;
    private List<Map<String, Integer>> unauthenticatedErrors;

    // Rejections
    private List<Integer> authenticatedEndpointRejections;
    private List<Integer> unauthenticatedEndpointRejections;

    // Authorisations
    private int activeIndividualAuthorisationCount;
    private int activeNonIndividualAuthorisationCount;
    private List<AuthorisationMetric> newAuthorisationCount;
    private List<CustomerTypeCount> revokedAuthorisationCount;
    private List<CustomerTypeCount> amendedAuthorisationCount;
    private List<CustomerTypeCount> expiredAuthorisationCount;

    // Abandonment by stage
    private List<Integer> abandonedConsentFlowCount;
    private List<Integer> preIdentificationAbandonedConsentFlowCount;
    private List<Integer> preAuthenticationAbandonedConsentFlowCount;
    private List<Integer> preAccountSelectionAbandonedConsentFlowCount;
    private List<Integer> preAuthorisationAbandonedConsentFlowCount;
    private List<Integer> rejectedAbandonedConsentFlowCount;
    private List<Integer> failedTokenExchangeAbandonedConsentFlowCount;

    public MetricsResponseModel(String requestTime) {
        this.requestTime = requestTime;
    }

    public String getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }

    public int getCustomerCount() {
        return customerCount;
    }

    public void setCustomerCount(int customerCount) {
        this.customerCount = customerCount;
    }

    public int getRecipientCount() {
        return recipientCount;
    }

    public void setRecipientCount(int recipientCount) {
        this.recipientCount = recipientCount;
    }

    public List<Integer> getSessionCount() {
        return sessionCount;
    }

    public void setSessionCount(List<Integer> sessionCount) {
        this.sessionCount = sessionCount;
    }

    public List<BigDecimal> getAvailability() {
        return availability;
    }

    public void setAvailability(List<BigDecimal> availability) {
        this.availability = availability;
    }

    public List<BigDecimal> getAuthenticatedAvailability() {
        return authenticatedAvailability;
    }

    public void setAuthenticatedAvailability(List<BigDecimal> authenticatedAvailability) {
        this.authenticatedAvailability = authenticatedAvailability;
    }

    public List<BigDecimal> getUnauthenticatedAvailability() {
        return unauthenticatedAvailability;
    }

    public void setUnauthenticatedAvailability(List<BigDecimal> unauthenticatedAvailability) {
        this.unauthenticatedAvailability = unauthenticatedAvailability;
    }

    public void setAvailability(Map<AspectEnum, List<BigDecimal>> availabilityMap) {

        setAvailability(availabilityMap.get(AspectEnum.ALL));
        setAuthenticatedAvailability(availabilityMap.get(AspectEnum.AUTHENTICATED));
        setUnauthenticatedAvailability(availabilityMap.get(AspectEnum.UNAUTHENTICATED));
    }

    public List<BigDecimal> getPerformance() {
        return performance;
    }

    public void setPerformance(List<BigDecimal> performance) {
        this.performance = performance;
    }

    public List<List<BigDecimal>> getPerformanceUnauthenticated() {
        return performanceUnauthenticated;
    }

    public void setPerformanceUnauthenticated(List<List<BigDecimal>> performanceUnauthenticated) {
        this.performanceUnauthenticated = performanceUnauthenticated;
    }

    public List<List<BigDecimal>> getPerformanceHighPriority() {
        return performanceHighPriority;
    }

    public void setPerformanceHighPriority(List<List<BigDecimal>> performanceHighPriority) {
        this.performanceHighPriority = performanceHighPriority;
    }

    public List<List<BigDecimal>> getPerformanceLowPriority() {
        return performanceLowPriority;
    }

    public void setPerformanceLowPriority(List<List<BigDecimal>> performanceLowPriority) {
        this.performanceLowPriority = performanceLowPriority;
    }

    public List<List<BigDecimal>> getPerformanceUnattended() {
        return performanceUnattended;
    }

    public void setPerformanceUnattended(List<List<BigDecimal>> performanceUnattended) {
        this.performanceUnattended = performanceUnattended;
    }

    public List<List<BigDecimal>> getPerformanceLargePayload() {
        return performanceLargePayload;
    }

    public void setPerformanceLargePayload(List<List<BigDecimal>> performanceLargePayload) {
        this.performanceLargePayload = performanceLargePayload;
    }

    public void setHourlyPerformanceByPriority(Map<PriorityEnum, List<List<BigDecimal>>> performanceMap) {

        setPerformanceUnauthenticated(performanceMap.get(PriorityEnum.UNAUTHENTICATED));
        setPerformanceHighPriority(performanceMap.get(PriorityEnum.HIGH_PRIORITY));
        setPerformanceLowPriority(performanceMap.get(PriorityEnum.LOW_PRIORITY));
        setPerformanceUnattended(performanceMap.get(PriorityEnum.UNATTENDED));
        setPerformanceLargePayload(performanceMap.get(PriorityEnum.LARGE_PAYLOAD));
    }

    public List<Integer> getInvocationUnauthenticated() {
        return invocationUnauthenticated;
    }

    public void setInvocationUnauthenticated(List<Integer> invocationUnauthenticated) {
        this.invocationUnauthenticated = invocationUnauthenticated;
    }

    public List<Integer> getInvocationHighPriority() {
        return invocationHighPriority;
    }

    public void setInvocationHighPriority(List<Integer> invocationHighPriority) {
        this.invocationHighPriority = invocationHighPriority;
    }

    public List<Integer> getInvocationLowPriority() {
        return invocationLowPriority;
    }

    public void setInvocationLowPriority(List<Integer> invocationLowPriority) {
        this.invocationLowPriority = invocationLowPriority;
    }

    public List<Integer> getInvocationUnattended() {
        return invocationUnattended;
    }

    public void setInvocationUnattended(List<Integer> invocationUnattended) {
        this.invocationUnattended = invocationUnattended;
    }

    public List<Integer> getInvocationLargePayload() {
        return invocationLargePayload;
    }

    public void setInvocationLargePayload(List<Integer> invocationLargePayload) {
        this.invocationLargePayload = invocationLargePayload;
    }

    public void setInvocations(Map<PriorityEnum, List<Integer>> invocationMap) {

        setInvocationUnauthenticated(invocationMap.get(PriorityEnum.UNAUTHENTICATED));
        setInvocationHighPriority(invocationMap.get(PriorityEnum.HIGH_PRIORITY));
        setInvocationLowPriority(invocationMap.get(PriorityEnum.LOW_PRIORITY));
        setInvocationUnattended(invocationMap.get(PriorityEnum.UNATTENDED));
        setInvocationLargePayload(invocationMap.get(PriorityEnum.LARGE_PAYLOAD));
    }

    public List<BigDecimal> getAverageResponseUnauthenticated() {
        return averageResponseUnauthenticated;
    }

    public void setAverageResponseUnauthenticated(List<BigDecimal> averageResponseUnauthenticated) {
        this.averageResponseUnauthenticated = averageResponseUnauthenticated;
    }

    public List<BigDecimal> getAverageResponseHighPriority() {
        return averageResponseHighPriority;
    }

    public void setAverageResponseHighPriority(List<BigDecimal> averageResponseHighPriority) {
        this.averageResponseHighPriority = averageResponseHighPriority;
    }

    public List<BigDecimal> getAverageResponseLowPriority() {
        return averageResponseLowPriority;
    }

    public void setAverageResponseLowPriority(List<BigDecimal> averageResponseLowPriority) {
        this.averageResponseLowPriority = averageResponseLowPriority;
    }

    public List<BigDecimal> getAverageResponseUnattended() {
        return averageResponseUnattended;
    }

    public void setAverageResponseUnattended(List<BigDecimal> averageResponseUnattended) {
        this.averageResponseUnattended = averageResponseUnattended;
    }

    public List<BigDecimal> getAverageResponseLargePayload() {
        return averageResponseLargePayload;
    }

    public void setAverageResponseLargePayload(List<BigDecimal> averageResponseLargePayload) {
        this.averageResponseLargePayload = averageResponseLargePayload;
    }

    public void setAverageResponseTime(Map<PriorityEnum, List<BigDecimal>> averageResponseMap) {

        setAverageResponseUnauthenticated(averageResponseMap.get(PriorityEnum.UNAUTHENTICATED));
        setAverageResponseHighPriority(averageResponseMap.get(PriorityEnum.HIGH_PRIORITY));
        setAverageResponseLowPriority(averageResponseMap.get(PriorityEnum.LOW_PRIORITY));
        setAverageResponseUnattended(averageResponseMap.get(PriorityEnum.UNATTENDED));
        setAverageResponseLargePayload(averageResponseMap.get(PriorityEnum.LARGE_PAYLOAD));
    }

    public List<BigDecimal> getAverageTPS() {
        return averageTPS;
    }

    public void setAverageTPS(List<BigDecimal> averageTPS) {
        this.averageTPS = averageTPS;
    }

    public List<BigDecimal> getAuthenticatedAverageTPS() {
        return authenticatedAverageTPS;
    }

    public void setAuthenticatedAverageTPS(List<BigDecimal> authenticatedAverageTPS) {
        this.authenticatedAverageTPS = authenticatedAverageTPS;
    }

    public List<BigDecimal> getUnauthenticatedAverageTPS() {
        return unauthenticatedAverageTPS;
    }

    public void setUnauthenticatedAverageTPS(List<BigDecimal> unauthenticatedAverageTPS) {
        this.unauthenticatedAverageTPS = unauthenticatedAverageTPS;
    }

    public void setAverageTPS(Map<AspectEnum, List<BigDecimal>> averageTPSMap) {

        setAverageTPS(averageTPSMap.get(AspectEnum.ALL));
        setAuthenticatedAverageTPS(averageTPSMap.get(AspectEnum.AUTHENTICATED));
        setUnauthenticatedAverageTPS(averageTPSMap.get(AspectEnum.UNAUTHENTICATED));
    }

    public List<BigDecimal> getPeakTPS() {
        return peakTPS;
    }

    public void setPeakTPS(List<BigDecimal> peakTPS) {
        this.peakTPS = peakTPS;
    }

    public List<BigDecimal> getAuthenticatedPeakTPS() {
        return authenticatedPeakTPS;
    }

    public void setAuthenticatedPeakTPS(List<BigDecimal> authenticatedPeakTPS) {
        this.authenticatedPeakTPS = authenticatedPeakTPS;
    }

    public List<BigDecimal> getUnauthenticatedPeakTPS() {
        return unauthenticatedPeakTPS;
    }

    public void setUnauthenticatedPeakTPS(List<BigDecimal> unauthenticatedPeakTPS) {
        this.unauthenticatedPeakTPS = unauthenticatedPeakTPS;
    }

    public void setPeakTPS(Map<AspectEnum, List<BigDecimal>> peakTPSMap) {

        setPeakTPS(peakTPSMap.get(AspectEnum.ALL));
        setAuthenticatedPeakTPS(peakTPSMap.get(AspectEnum.AUTHENTICATED));
        setUnauthenticatedPeakTPS(peakTPSMap.get(AspectEnum.UNAUTHENTICATED));
    }

    public List<Integer> getErrors() {
        return errors;
    }

    public void setErrors(List<Integer> errors) {
        this.errors = errors;
    }

    public List<Map<String, Integer>> getAuthenticatedErrors() {
        return authenticatedErrors;
    }

    public void setAuthenticatedErrors(List<Map<String, Integer>> authenticatedErrors) {
        this.authenticatedErrors = authenticatedErrors;
    }

    public List<Map<String, Integer>> getUnauthenticatedErrors() {
        return unauthenticatedErrors;
    }

    public void setUnauthenticatedErrors(List<Map<String, Integer>> unauthenticatedErrors) {
        this.unauthenticatedErrors = unauthenticatedErrors;
    }

    public void setErrorsByAspect(List<ErrorMetricDay> errorMetricDayList) {

        setUnauthenticatedErrors(errorMetricDayList
                .stream()
                .map(ErrorMetricDay::getUnauthenticatedErrorMap)
                .collect(Collectors.toList()));
        setAuthenticatedErrors(errorMetricDayList
                .stream()
                .map(ErrorMetricDay::getAuthenticatedErrorMap)
                .collect(Collectors.toList()));
    }

    public List<Integer> getAuthenticatedEndpointRejections() {
        return authenticatedEndpointRejections;
    }

    public void setAuthenticatedEndpointRejections(List<Integer> authenticatedEndpointRejections) {
        this.authenticatedEndpointRejections = authenticatedEndpointRejections;
    }

    public List<Integer> getUnauthenticatedEndpointRejections() {
        return unauthenticatedEndpointRejections;
    }

    public void setUnauthenticatedEndpointRejections(List<Integer> unauthenticatedEndpointRejections) {
        this.unauthenticatedEndpointRejections = unauthenticatedEndpointRejections;
    }

    public void setRejections(Map<AspectEnum, List<Integer>> rejectionsMap) {

        setAuthenticatedEndpointRejections(rejectionsMap.get(AspectEnum.AUTHENTICATED));
        setUnauthenticatedEndpointRejections(rejectionsMap.get(AspectEnum.UNAUTHENTICATED));
    }

    public int getActiveIndividualAuthorisationCount() {
        return activeIndividualAuthorisationCount;
    }

    public void setActiveIndividualAuthorisationCount(int activeIndividualAuthorisationCount) {
        this.activeIndividualAuthorisationCount = activeIndividualAuthorisationCount;
    }

    public int getActiveNonIndividualAuthorisationCount() {
        return activeNonIndividualAuthorisationCount;
    }

    public void setActiveNonIndividualAuthorisationCount(int activeNonIndividualAuthorisationCount) {
        this.activeNonIndividualAuthorisationCount = activeNonIndividualAuthorisationCount;
    }

    public void setActiveAuthorisationCount(Map<String, Integer> activeAuthorisationCountMap) {

        setActiveIndividualAuthorisationCount(activeAuthorisationCountMap.get(MetricsConstants.INDIVIDUAL));
        setActiveNonIndividualAuthorisationCount(activeAuthorisationCountMap.get(MetricsConstants.NON_INDIVIDUAL));
    }

    public List<AuthorisationMetric> getNewAuthorisationCount() {
        return newAuthorisationCount;
    }

    public void setNewAuthorisationCount(List<AuthorisationMetric> newAuthorisationCount) {
        this.newAuthorisationCount = newAuthorisationCount;
    }

    public List<CustomerTypeCount> getRevokedAuthorisationCount() {
        return revokedAuthorisationCount;
    }

    public void setRevokedAuthorisationCount(List<CustomerTypeCount> revokedAuthorisationCount) {
        this.revokedAuthorisationCount = revokedAuthorisationCount;
    }

    public List<CustomerTypeCount> getAmendedAuthorisationCount() {
        return amendedAuthorisationCount;
    }

    public void setAmendedAuthorisationCount(List<CustomerTypeCount> amendedAuthorisationCount) {
        this.amendedAuthorisationCount = amendedAuthorisationCount;
    }

    public List<CustomerTypeCount> getExpiredAuthorisationCount() {
        return expiredAuthorisationCount;
    }

    public void setExpiredAuthorisationCount(List<CustomerTypeCount> expiredAuthorisationCount) {
        this.expiredAuthorisationCount = expiredAuthorisationCount;
    }

    public void setAuthorisation(List<AuthorisationMetricDay> authorisationMetricDayList) {

        setNewAuthorisationCount(authorisationMetricDayList
                .stream()
                .map(AuthorisationMetricDay::getNewAuthorisationMetric)
                .collect(Collectors.toList()));
        setRevokedAuthorisationCount(authorisationMetricDayList
                .stream()
                .map(AuthorisationMetricDay::getRevokedAuthorisationMetric)
                .map(AuthorisationMetric::getOngoing)
                .collect(Collectors.toList()));
        setAmendedAuthorisationCount(authorisationMetricDayList
                .stream()
                .map(AuthorisationMetricDay::getAmendedAuthorisationMetric)
                .map(AuthorisationMetric::getOngoing)
                .collect(Collectors.toList()));
        setExpiredAuthorisationCount(authorisationMetricDayList
                .stream()
                .map(AuthorisationMetricDay::getExpiredAuthorisationMetric)
                .map(AuthorisationMetric::getOngoing)
                .collect(Collectors.toList()));
    }

    public List<Integer> getAbandonedConsentFlowCount() {
        return abandonedConsentFlowCount;
    }

    public void setAbandonedConsentFlowCount(List<Integer> abandonedConsentFlowCount) {
        this.abandonedConsentFlowCount = abandonedConsentFlowCount;
    }

    public List<Integer> getPreIdentificationAbandonedConsentFlowCount() {
        return preIdentificationAbandonedConsentFlowCount;
    }

    public void setPreIdentificationAbandonedConsentFlowCount(
            List<Integer> preIdentificationAbandonedConsentFlowCount) {
        this.preIdentificationAbandonedConsentFlowCount = preIdentificationAbandonedConsentFlowCount;
    }

    public List<Integer> getPreAuthenticationAbandonedConsentFlowCount() {
        return preAuthenticationAbandonedConsentFlowCount;
    }

    public void setPreAuthenticationAbandonedConsentFlowCount(
            List<Integer> preAuthenticationAbandonedConsentFlowCount) {
        this.preAuthenticationAbandonedConsentFlowCount = preAuthenticationAbandonedConsentFlowCount;
    }

    public List<Integer> getPreAccountSelectionAbandonedConsentFlowCount() {
        return preAccountSelectionAbandonedConsentFlowCount;
    }

    public void setPreAccountSelectionAbandonedConsentFlowCount(
            List<Integer> preAccountSelectionAbandonedConsentFlowCount) {
        this.preAccountSelectionAbandonedConsentFlowCount = preAccountSelectionAbandonedConsentFlowCount;
    }

    public List<Integer> getPreAuthorisationAbandonedConsentFlowCount() {
        return preAuthorisationAbandonedConsentFlowCount;
    }

    public void setPreAuthorisationAbandonedConsentFlowCount(List<Integer> preAuthorisationAbandonedConsentFlowCount) {
        this.preAuthorisationAbandonedConsentFlowCount = preAuthorisationAbandonedConsentFlowCount;
    }

    public List<Integer> getRejectedAbandonedConsentFlowCount() {
        return rejectedAbandonedConsentFlowCount;
    }

    public void setRejectedAbandonedConsentFlowCount(List<Integer> rejectedAbandonedConsentFlowCount) {
        this.rejectedAbandonedConsentFlowCount = rejectedAbandonedConsentFlowCount;
    }

    public List<Integer> getFailedTokenExchangeAbandonedConsentFlowCount() {
        return failedTokenExchangeAbandonedConsentFlowCount;
    }

    public void setFailedTokenExchangeAbandonedConsentFlowCount(
            List<Integer> failedTokenExchangeAbandonedConsentFlowCount) {
        this.failedTokenExchangeAbandonedConsentFlowCount = failedTokenExchangeAbandonedConsentFlowCount;
    }

    public void setAbandonedConsentFlow(List<AbandonedConsentFlowByStageMetricDay>
                                                abandonedConsentFlowByStageCountMetricDayList) {

        // set abandoned consent flow metrics
        setAbandonedConsentFlowCount(abandonedConsentFlowByStageCountMetricDayList
                .stream()
                .map(AbandonedConsentFlowByStageMetricDay::getAbandonedConsentFlowCount)
                .collect(Collectors.toList()));

        // set abandoned consent flow by stage metrics
        setPreIdentificationAbandonedConsentFlowCount(abandonedConsentFlowByStageCountMetricDayList
                .stream()
                .map(AbandonedConsentFlowByStageMetricDay::getAbandonedByPreIdentificationStageCount)
                .collect(Collectors.toList()));
        setPreAuthenticationAbandonedConsentFlowCount(abandonedConsentFlowByStageCountMetricDayList
                .stream()
                .map(AbandonedConsentFlowByStageMetricDay::getAbandonedByPreAuthenticationStageCount)
                .collect(Collectors.toList()));
        setPreAccountSelectionAbandonedConsentFlowCount(abandonedConsentFlowByStageCountMetricDayList
                .stream()
                .map(AbandonedConsentFlowByStageMetricDay::getAbandonedByPreAccountSelectionStageCount)
                .collect(Collectors.toList()));
        setPreAuthorisationAbandonedConsentFlowCount(abandonedConsentFlowByStageCountMetricDayList
                .stream()
                .map(AbandonedConsentFlowByStageMetricDay::getAbandonedByPreAuthorisationStageCount)
                .collect(Collectors.toList()));
        setRejectedAbandonedConsentFlowCount(abandonedConsentFlowByStageCountMetricDayList
                .stream()
                .map(AbandonedConsentFlowByStageMetricDay::getAbandonedByRejectedStageCount)
                .collect(Collectors.toList()));
        setFailedTokenExchangeAbandonedConsentFlowCount(abandonedConsentFlowByStageCountMetricDayList
                .stream()
                .map(AbandonedConsentFlowByStageMetricDay::getAbandonedByFailedTokenExchangeStageCount)
                .collect(Collectors.toList()));
    }
}

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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.openbanking.cds.metrics.model.AuthorisationMetric;
import org.wso2.openbanking.cds.metrics.model.CustomerTypeCount;
import org.wso2.openbanking.cds.metrics.model.MetricsResponseModel;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class MetricsServiceUtilTest {

    private MetricsResponseModel currentDayMetrics;
    private MetricsResponseModel historicMetrics;
    private static final int TOTAL_DAYS = 8;

    @BeforeMethod
    public void setUp() {
        currentDayMetrics = createMetricsResponseModelWithValues(1);
        historicMetrics = createMetricsResponseModelWithValues(7);
    }

    private MetricsResponseModel createMetricsResponseModelWithValues(int numberOfEntries) {

        String currentDate = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
        MetricsResponseModel model = new MetricsResponseModel(currentDate);

        model.setAvailability(createBigDecimalList(numberOfEntries));
        model.setAuthenticatedAvailability(createBigDecimalList(numberOfEntries));
        model.setUnauthenticatedAvailability(createBigDecimalList(numberOfEntries));

        model.setErrors(createIntegerList(numberOfEntries));
        model.setAuthenticatedErrors(createErrorList(numberOfEntries));
        model.setUnauthenticatedErrors(createErrorList(numberOfEntries));

        model.setPeakTPS(createBigDecimalList(numberOfEntries));
        model.setAuthenticatedPeakTPS(createBigDecimalList(numberOfEntries));
        model.setUnauthenticatedPeakTPS(createBigDecimalList(numberOfEntries));

        model.setAverageTPS(createBigDecimalList(numberOfEntries));
        model.setAuthenticatedAverageTPS(createBigDecimalList(numberOfEntries));
        model.setUnauthenticatedAverageTPS(createBigDecimalList(numberOfEntries));

        model.setPerformance(createBigDecimalList(numberOfEntries));
        model.setPerformanceHighPriority(createBigDecimalListOfLists(numberOfEntries));
        model.setPerformanceLowPriority(createBigDecimalListOfLists(numberOfEntries));
        model.setPerformanceUnattended(createBigDecimalListOfLists(numberOfEntries));
        model.setPerformanceUnauthenticated(createBigDecimalListOfLists(numberOfEntries));
        model.setPerformanceLargePayload(createBigDecimalListOfLists(numberOfEntries));

        model.setSessionCount(createIntegerList(numberOfEntries));

        model.setInvocationUnauthenticated(createIntegerList(numberOfEntries));
        model.setInvocationHighPriority(createIntegerList(numberOfEntries));
        model.setInvocationLowPriority(createIntegerList(numberOfEntries));
        model.setInvocationUnattended(createIntegerList(numberOfEntries));
        model.setInvocationLargePayload(createIntegerList(numberOfEntries));

        model.setAverageResponseUnauthenticated(createBigDecimalList(numberOfEntries));
        model.setAverageResponseHighPriority(createBigDecimalList(numberOfEntries));
        model.setAverageResponseLowPriority(createBigDecimalList(numberOfEntries));
        model.setAverageResponseUnattended(createBigDecimalList(numberOfEntries));
        model.setAverageResponseLargePayload(createBigDecimalList(numberOfEntries));

        model.setAuthenticatedEndpointRejections(createIntegerList(numberOfEntries));
        model.setUnauthenticatedEndpointRejections(createIntegerList(numberOfEntries));

        model.setNewAuthorisationCount(createAuthorisationMetricList(numberOfEntries));
        model.setAmendedAuthorisationCount(createCustomerTypeCountList(numberOfEntries));
        model.setExpiredAuthorisationCount(createCustomerTypeCountList(numberOfEntries));
        model.setRevokedAuthorisationCount(createCustomerTypeCountList(numberOfEntries));

        model.setAbandonedConsentFlowCount(createIntegerList(numberOfEntries));

        model.setPreIdentificationAbandonedConsentFlowCount(createIntegerList(numberOfEntries));
        model.setPreAuthenticationAbandonedConsentFlowCount(createIntegerList(numberOfEntries));
        model.setPreAccountSelectionAbandonedConsentFlowCount(createIntegerList(numberOfEntries));
        model.setPreAuthorisationAbandonedConsentFlowCount(createIntegerList(numberOfEntries));
        model.setRejectedAbandonedConsentFlowCount(createIntegerList(numberOfEntries));
        model.setFailedTokenExchangeAbandonedConsentFlowCount(createIntegerList(numberOfEntries));

        return model;
    }

    private List<BigDecimal> createBigDecimalList(int size) {
        return IntStream.range(0, size)
                .mapToObj(i -> BigDecimal.valueOf(Math.random() * 10))
                .collect(Collectors.toList());
    }

    private List<List<BigDecimal>> createBigDecimalListOfLists(int size) {
        return IntStream.range(0, size)
                .mapToObj(i -> IntStream.range(0, 24)
                        .mapToObj(j -> BigDecimal.valueOf(Math.random() * 10))
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    private List<Integer> createIntegerList(int size) {
        return IntStream.range(0, size)
                .map(i -> (int) (Math.random() * 10))
                .boxed()
                .collect(Collectors.toList());
    }

    private List<Map<String, Integer>> createErrorList(int size) {
        return IntStream.range(0, size)
                .mapToObj(i -> {
                    Map<String, Integer> map = new HashMap<>();
                    map.put("400", (int) (Math.random() * 10));
                    return map;
                })
                .collect(Collectors.toList());
    }

    private List<AuthorisationMetric> createAuthorisationMetricList(int size) {
        return IntStream.range(0, size)
                .mapToObj(i -> new AuthorisationMetric())
                .collect(Collectors.toList());
    }

    private List<CustomerTypeCount> createCustomerTypeCountList(int size) {
        return IntStream.range(0, size)
                .mapToObj(i -> new CustomerTypeCount())
                .collect(Collectors.toList());
    }

    @Test
    public void testAppendHistoricMetricsToCurrentDayMetrics() {

        MetricsServiceUtil.appendHistoricMetricsToCurrentDayMetrics(currentDayMetrics, historicMetrics);
        // Test if all metrics lists have been appended correctly
        assertEquals(currentDayMetrics.getAvailability().size(), TOTAL_DAYS);
        assertEquals(currentDayMetrics.getAuthenticatedAvailability().size(), TOTAL_DAYS);
        assertEquals(currentDayMetrics.getUnauthenticatedAvailability().size(), TOTAL_DAYS);

        assertEquals(currentDayMetrics.getErrors().size(), TOTAL_DAYS);
        assertEquals(currentDayMetrics.getAuthenticatedErrors().size(), TOTAL_DAYS);
        assertEquals(currentDayMetrics.getUnauthenticatedErrors().size(), TOTAL_DAYS);

        assertEquals(currentDayMetrics.getPeakTPS().size(), TOTAL_DAYS);
        assertEquals(currentDayMetrics.getAuthenticatedPeakTPS().size(), TOTAL_DAYS);
        assertEquals(currentDayMetrics.getUnauthenticatedPeakTPS().size(), TOTAL_DAYS);

        assertEquals(currentDayMetrics.getAverageTPS().size(), TOTAL_DAYS);
        assertEquals(currentDayMetrics.getAuthenticatedAverageTPS().size(), TOTAL_DAYS);
        assertEquals(currentDayMetrics.getUnauthenticatedAverageTPS().size(), TOTAL_DAYS);

        assertEquals(currentDayMetrics.getPerformance().size(), TOTAL_DAYS);
        assertEquals(currentDayMetrics.getPerformanceHighPriority().size(), TOTAL_DAYS);
        assertEquals(currentDayMetrics.getPerformanceLowPriority().size(), TOTAL_DAYS);
        assertEquals(currentDayMetrics.getPerformanceUnattended().size(), TOTAL_DAYS);
        assertEquals(currentDayMetrics.getPerformanceUnauthenticated().size(), TOTAL_DAYS);
        assertEquals(currentDayMetrics.getPerformanceLargePayload().size(), TOTAL_DAYS);

        assertEquals(currentDayMetrics.getSessionCount().size(), TOTAL_DAYS);

        assertEquals(currentDayMetrics.getInvocationUnauthenticated().size(), TOTAL_DAYS);
        assertEquals(currentDayMetrics.getInvocationHighPriority().size(), TOTAL_DAYS);
        assertEquals(currentDayMetrics.getInvocationLowPriority().size(), TOTAL_DAYS);
        assertEquals(currentDayMetrics.getInvocationUnattended().size(), TOTAL_DAYS);
        assertEquals(currentDayMetrics.getInvocationLargePayload().size(), TOTAL_DAYS);

        assertEquals(currentDayMetrics.getAverageResponseUnauthenticated().size(), TOTAL_DAYS);
        assertEquals(currentDayMetrics.getAverageResponseHighPriority().size(), TOTAL_DAYS);
        assertEquals(currentDayMetrics.getAverageResponseLowPriority().size(), TOTAL_DAYS);
        assertEquals(currentDayMetrics.getAverageResponseUnattended().size(), TOTAL_DAYS);
        assertEquals(currentDayMetrics.getAverageResponseLargePayload().size(), TOTAL_DAYS);

        assertEquals(currentDayMetrics.getAuthenticatedEndpointRejections().size(), TOTAL_DAYS);
        assertEquals(currentDayMetrics.getUnauthenticatedEndpointRejections().size(), TOTAL_DAYS);

        assertEquals(currentDayMetrics.getNewAuthorisationCount().size(), TOTAL_DAYS);
        assertEquals(currentDayMetrics.getAmendedAuthorisationCount().size(), TOTAL_DAYS);
        assertEquals(currentDayMetrics.getExpiredAuthorisationCount().size(), TOTAL_DAYS);
        assertEquals(currentDayMetrics.getRevokedAuthorisationCount().size(), TOTAL_DAYS);

        assertEquals(currentDayMetrics.getAbandonedConsentFlowCount().size(), TOTAL_DAYS);

        assertEquals(currentDayMetrics.getPreIdentificationAbandonedConsentFlowCount().size(), TOTAL_DAYS);
        assertEquals(currentDayMetrics.getPreAuthenticationAbandonedConsentFlowCount().size(), TOTAL_DAYS);
        assertEquals(currentDayMetrics.getPreAccountSelectionAbandonedConsentFlowCount().size(), TOTAL_DAYS);
        assertEquals(currentDayMetrics.getPreAuthorisationAbandonedConsentFlowCount().size(), TOTAL_DAYS);
        assertEquals(currentDayMetrics.getRejectedAbandonedConsentFlowCount().size(), TOTAL_DAYS);
        assertEquals(currentDayMetrics.getFailedTokenExchangeAbandonedConsentFlowCount().size(), TOTAL_DAYS);
    }

    @Test
    public void testIsResponseModelExpired() {
        assertFalse(MetricsServiceUtil.isResponseModelExpired(currentDayMetrics));
        String pastDate = ZonedDateTime.now().minusDays(2).format(DateTimeFormatter.
                ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
        MetricsResponseModel expiredModel = new MetricsResponseModel(pastDate);
        assertTrue(MetricsServiceUtil.isResponseModelExpired(expiredModel));
    }
}

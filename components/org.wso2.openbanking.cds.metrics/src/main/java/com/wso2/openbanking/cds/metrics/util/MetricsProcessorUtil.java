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

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import net.minidev.asm.DefaultConverter;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;
import org.wso2.openbanking.cds.common.enums.AbandonmentsByStageEnum;
import org.wso2.openbanking.cds.common.enums.AuthorisationFlowTypeEnum;
import org.wso2.openbanking.cds.common.enums.AuthorisationStageEnum;
import org.wso2.openbanking.cds.common.enums.ConsentDurationTypeEnum;
import org.wso2.openbanking.cds.common.enums.ConsentStatusEnum;
import org.wso2.openbanking.cds.metrics.constants.MetricsConstants;
import org.wso2.openbanking.cds.metrics.model.AbandonedConsentFlowByStageMetricDay;
import org.wso2.openbanking.cds.metrics.model.AbandonedConsentFlowMetricDataModel;
import org.wso2.openbanking.cds.metrics.model.ActiveAuthorisationMetricDataModel;
import org.wso2.openbanking.cds.metrics.model.AuthorisationMetricDataModel;
import org.wso2.openbanking.cds.metrics.model.AuthorisationMetricDay;
import org.wso2.openbanking.cds.metrics.model.AuthorisationStageTimestamp;
import org.wso2.openbanking.cds.metrics.model.ErrorMetricDataModel;
import org.wso2.openbanking.cds.metrics.model.ErrorMetricDay;
import org.wso2.openbanking.cds.metrics.model.PerformanceMetric;
import org.wso2.openbanking.cds.metrics.model.ServerOutageDataModel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.wso2.openbanking.cds.metrics.constants.MetricsConstants.RECORDS;

/**
 * Contains utility methods for calculating metrics.
 */
public class MetricsProcessorUtil {

    private static final Log log = LogFactory.getLog(MetricsProcessorUtil.class);
    private static final OpenBankingCDSConfigParser configParser = OpenBankingCDSConfigParser.getInstance();
    private static final ZoneId timeZone = ZoneId.of(configParser.getMetricsTimeZone());
    private static final LocalDate metricsV5StartDate = LocalDate.parse(configParser.getMetricsV5StartDate());
    private static final long consentAbandonmentTime = configParser.getConsentAbandonmentTime();
    private static final long authCodeValidityPeriod = configParser.getAuthorizationCodeValidityPeriod();

    private MetricsProcessorUtil() {
    }

    /**
     * Perform division between two lists.
     *
     * @param list1        - dividend list
     * @param list2        - divisor list
     * @param defaultValue - default big decimal value
     * @param <T1>         - type of the first list elements
     * @param <T2>         - type of the second list elements
     * @return resulting list of BigDecimal
     * @throws OpenBankingException if lists have different sizes or division by zero occurs
     */
    public static <T1, T2> List<BigDecimal> divideLists(List<T1> list1, List<T2> list2, BigDecimal defaultValue)
            throws OpenBankingException {

        int listSize = list1.size();
        List<BigDecimal> resultList = new ArrayList<>();

        if (listSize != list2.size()) {
            throw new OpenBankingException("Cannot perform division between lists with different sizes");
        }

        for (int i = 0; i < listSize; i++) {
            BigDecimal dividend = convertToBigDecimal(list1.get(i));
            BigDecimal divisor = convertToBigDecimal(list2.get(i));
            if (!divisor.equals(BigDecimal.ZERO)) {
                BigDecimal currentResult = dividend.divide(divisor, 3, RoundingMode.HALF_UP);
                resultList.add(currentResult);
            } else {
                resultList.add(defaultValue);
            }
        }
        return resultList;
    }

    /**
     * Convert an object to BigDecimal.
     *
     * @param value - the value to be converted
     * @param <T>   - type of the value
     * @return the converted BigDecimal
     * @throws IllegalArgumentException if the value type is not supported
     */
    private static <T> BigDecimal convertToBigDecimal(T value) {

        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        } else if (value instanceof Integer) {
            return BigDecimal.valueOf((Integer) value);
        } else {
            throw new IllegalArgumentException("Unsupported type: " + value.getClass().getName());
        }
    }

    /**
     * Initialize new priority-tier map for Metrics data with a given type.
     *
     * @param numberOfDays - Number of days to initialize
     * @param initialValue - Initial value for the list elements
     * @param <T>          - Type of the list elements
     * @return - Priority-tier map initialized for given number of days
     */
    public static <T> Map<PriorityEnum, List<T>> initializePriorityMap(int numberOfDays, T initialValue) {

        Map<PriorityEnum, List<T>> map = new HashMap<>();
        for (PriorityEnum priority : PriorityEnum.values()) {
            map.put(priority, initializeList(numberOfDays, initialValue));
        }
        return map;
    }

    /**
     * Initialize new aspect map for Metrics data with a given type.
     *
     * @param numberOfDays - Number of days to initialize
     * @param initialValue - Initial value for the list elements
     * @param <T>          - Type of the list elements
     * @return - Aspect map initialized for given number of days
     */
    public static <T> Map<AspectEnum, List<T>> initializeAspectMap(int numberOfDays, T initialValue) {

        Map<AspectEnum, List<T>> map = new HashMap<>();
        for (AspectEnum aspect : AspectEnum.values()) {
            map.put(aspect, initializeList(numberOfDays, initialValue));
        }
        return map;
    }

    /**
     * Initializes a list with a specified number of items, each set to an initial value.
     *
     * @param <T>           The type of elements in the list.
     * @param numberOfItems The number of items to be included in the list.
     * @param initialValue  The initial value for each item in the list.
     * @return An ArrayList initialized with the specified number of items, each set to the provided initial value.
     */
    public static <T> ArrayList<T> initializeList(int numberOfItems, T initialValue) {

        return new ArrayList<>(Collections.nCopies(numberOfItems, initialValue));
    }

    /**
     * Get last value from the elements in the JSON object.
     *
     * @param jsonObject - JSON object
     * @return latest value
     */
    public static int getLastElementValueFromJsonObject(JSONObject jsonObject) {

        JSONArray recordsArray = (JSONArray) jsonObject.get(RECORDS);
        if (recordsArray.isEmpty()) {
            return 0;
        }
        JSONArray countArray = (JSONArray) recordsArray.get(recordsArray.size() - 1);
        return Integer.parseInt(countArray.get(0).toString());
    }

    /**
     * Merge keys together to get a single list of invocations for each day.
     *
     * @param invocationMetricsMap - Map of invocation metrics
     * @return - Merged list of invocations
     */
    public static <K> List<Integer> getTotalInvocationsForEachDay(Map<K, List<Integer>> invocationMetricsMap,
                                                                  K[] keys) {

        List<Integer> totalTransactionsList = new ArrayList<>();

        // Get number of days by list size of the first key in the array
        int dayCount = invocationMetricsMap.get(keys[0]).size();

        for (int day = 0; day < dayCount; day++) {
            int totalTransactions = 0;
            List<Integer> currentList;
            for (K key : keys) {
                currentList = invocationMetricsMap.get(key);
                if (!currentList.isEmpty()) {
                    totalTransactions += currentList.get(day);
                }
            }
            totalTransactionsList.add(totalTransactions);
        }
        return totalTransactionsList;
    }

    /**
     * Populates the average TPS list based on the given invocation counts.
     *
     * @param invocationList the list of integer invocation counts
     * @param averageTPSList the list to be populated with the calculated average TPS values
     */
    public static void populateAverageTPSList(List<Integer> invocationList, List<BigDecimal> averageTPSList) {

        for (Integer transactionCount : invocationList) {
            BigDecimal avgTPS = new BigDecimal(transactionCount).divide(MetricsConstants.SECONDS_IN_DAY, 3,
                    RoundingMode.HALF_UP);
            if (avgTPS.compareTo(BigDecimal.ZERO) == 0) {
                averageTPSList.add(BigDecimal.valueOf(0).setScale(3, RoundingMode.HALF_UP));
            } else {
                averageTPSList.add(avgTPS);
            }
        }
    }

    /**
     * Populate a map of invocation metrics data categorized to priority tiers.
     *
     * @param metricsJsonObject         - Json object with invocation metrics
     * @param numberOfDays              - Number of days to consider
     * @param metricsCountLastDateEpoch - Epoch timestamp of the last date that metrics are required
     * @return - populated map
     */
    public static Map<PriorityEnum, List<Integer>> getPopulatedInvocationByPriorityMetricsMap(
            JSONObject metricsJsonObject, int numberOfDays, long metricsCountLastDateEpoch) {

        Map<PriorityEnum, List<Integer>> dataMap = initializePriorityMap(numberOfDays, 0);
        JSONArray records = (JSONArray) metricsJsonObject.get(RECORDS);
        for (Object recordObj : records) {
            JSONArray record = (JSONArray) recordObj;
            PriorityEnum priority = PriorityEnum.fromValue((String) record.get(0));
            Integer count = (Integer) record.get(1);
            long recordTimestamp = (Long) record.get(2);
            int daysAgo = DateTimeUtil.getDayDifference(recordTimestamp / 1000, metricsCountLastDateEpoch);

            // Number of days ago can be used as the index to insert data to the list
            if (daysAgo >= 0 && daysAgo < numberOfDays) {
                dataMap.get(priority).set(daysAgo, dataMap.get(priority).get(daysAgo) + count);
            }
        }
        return dataMap;
    }

    /**
     * Populate a map of invocation metrics data categorized by aspects.
     *
     * @param metricsJsonObject         - Json object with invocation metrics
     * @param numberOfDays              - Number of days to consider
     * @param metricsCountLastDateEpoch - Epoch timestamp of the last date that metrics are required
     * @return - populated map
     */
    public static Map<AspectEnum, List<Integer>> getPopulatedInvocationByAspectMetricsMap(
            JSONObject metricsJsonObject, int numberOfDays, long metricsCountLastDateEpoch) {

        Map<AspectEnum, List<Integer>> dataMap = initializeAspectMap(numberOfDays, 0);
        JSONArray records = (JSONArray) metricsJsonObject.get(RECORDS);
        for (Object recordObj : records) {
            JSONArray record = (JSONArray) recordObj;
            AspectEnum aspect = AspectEnum.fromValue((String) record.get(0));
            Integer count = (Integer) record.get(1);
            long recordTimestamp = (Long) record.get(2);
            int daysAgo = DateTimeUtil.getDayDifference(recordTimestamp / 1000, metricsCountLastDateEpoch);

            // Number of days ago can be used as the index to insert data to the list
            if (daysAgo >= 0 && daysAgo < numberOfDays) {
                dataMap.get(aspect).set(daysAgo, dataMap.get(aspect).get(daysAgo) + count);
            }
        }
        return dataMap;
    }

    /**
     * Populate a map of hourly performance metrics data categorized to priority tiers.
     *
     * @param metricsJsonObject - Json object with invocation metrics
     * @param numberOfDays      - Number of days to consider
     * @return - populated map
     */
    public static Map<PriorityEnum, List<List<BigDecimal>>> getPopulatedHourlyPerformanceByPriorityMetricsMap(
            JSONObject metricsJsonObject, int numberOfDays) throws OpenBankingException {

        boolean isCurrentDay = numberOfDays == 1;
        int alteredNoOfDays = getNumberOfDaysAccordingToV5StartDay(numberOfDays);
        Map<PriorityEnum, List<List<BigDecimal>>> performanceMetricsMap = getInitialPerformanceMetricsMap(
                alteredNoOfDays, isCurrentDay);
        List<PerformanceMetric> performanceRecords = parsePerformanceMetricsJson(metricsJsonObject);

        if (!performanceRecords.isEmpty()) {
            validateDateRangeOfRecords(performanceRecords, alteredNoOfDays);
            populatePerformanceMetricsMap(performanceRecords, performanceMetricsMap);
        }

        return performanceMetricsMap;
    }

    /**
     * Check if the date range of the records matches with the given number of days.
     *
     * @param noOfDays - Number of days
     * @throws OpenBankingException - OpenBankingException
     */
    private static void validateDateRangeOfRecords(List<PerformanceMetric> performanceRecords, int noOfDays)
            throws OpenBankingException {

        // Get the starting and ending dates of the records
        LocalDate recordsStartingDate = Instant.ofEpochMilli(getStartingTimestampOfTheFirstDay(performanceRecords)).
                atZone(timeZone).toLocalDate();
        LocalDate recordsEndingDate = Instant.ofEpochMilli(getEndingTimestampOfTheLastDay(performanceRecords)).
                atZone(timeZone).toLocalDate();
        final String invalidV5DateErrorMsg = "Metrics V5 start date is not configured correctly.";

        // Compare the starting date of records with the v5 start date config
        if (recordsStartingDate.isBefore(metricsV5StartDate)) {
            log.error("Performance metrics records for the date " + recordsStartingDate + " which is before the " +
                    "configured Metrics V5 start date " + metricsV5StartDate + "were found. Please configure the " +
                    "correct starting date.");
            throw new OpenBankingException(invalidV5DateErrorMsg);
        }

        // Check if the day difference in records is greater than the considered number of days for performance metrics
        long dayDifference = ChronoUnit.DAYS.between(recordsStartingDate, recordsEndingDate);
        if (dayDifference > noOfDays) {
            log.error("Range of performance metrics records: " + recordsStartingDate + " to " + recordsEndingDate +
                    " is greater than the considered number of days: " + noOfDays);
            throw new OpenBankingException(invalidV5DateErrorMsg);
        }

    }

    /**
     * Populate the performance metrics map with the performance values of the records.
     *
     * @param performanceRecords    - performance records retrieved from the stream processor
     * @param performanceMetricsMap - performance metrics map to be populated
     */
    private static void populatePerformanceMetricsMap(List<PerformanceMetric> performanceRecords,
                                                      Map<PriorityEnum, List<List<BigDecimal>>> performanceMetricsMap) {

        // Populate the performance metrics map going through each record
        for (PerformanceMetric record : performanceRecords) {
            String priorityTier = record.getPriorityTier();
            long timestamp = record.getTimestamp();
            BigDecimal performance = BigDecimal.valueOf(record.getPerformanceValue())
                    .setScale(3, RoundingMode.HALF_UP);

            // Calculate which day the record belongs to
            ZonedDateTime recordDateTime = Instant.ofEpochMilli(timestamp).atZone(timeZone);
            int dayDifference = getDayDifferenceForTheRecord(recordDateTime.toLocalDate());
            PriorityEnum priorityEnum = PriorityEnum.fromValue(priorityTier);
            performanceMetricsMap.get(priorityEnum).get(dayDifference).set(recordDateTime.getHour(), performance);

        }
    }

    /**
     * Get the day difference for the given date from the current day.
     * If the record is for the current day, return 0. Otherwise, return the day difference minus 1.
     *
     * @param recordDate - date of the record
     * @return - day difference
     */
    private static int getDayDifferenceForTheRecord(LocalDate recordDate) {

        LocalDate currentDate = LocalDate.now(timeZone);

        // Check if the recordDate is the current date
        if (recordDate.equals(currentDate)) {
            return 0;
        } else {
            // Calculate the difference in days. Subtract 1 to support zero-indexing for the previous days.
            return (int) ChronoUnit.DAYS.between(recordDate, currentDate) - 1;
        }
    }

    /**
     * Get the starting timestamp of the first day from the performance records.
     *
     * @param performanceRecords - performance records retrieved from the stream processor
     * @return - starting timestamp of the first day
     */
    private static long getStartingTimestampOfTheFirstDay(List<PerformanceMetric> performanceRecords) {

        PerformanceMetric firstRecord = performanceRecords.get(0);
        long firstDayStartTimestamp = firstRecord.getTimestamp();
        if (log.isDebugEnabled()) {
            log.debug("Earliest timestamp retrieved from performance metrics records: " + firstDayStartTimestamp);
        }
        return firstDayStartTimestamp;
    }

    /**
     * Get the ending timestamp of the last day from the performance records.
     *
     * @param performanceRecords - performance records retrieved from the stream processor
     * @return - ending timestamp of the last day
     */
    private static long getEndingTimestampOfTheLastDay(List<PerformanceMetric> performanceRecords) {

        PerformanceMetric lastRecord = performanceRecords.get(performanceRecords.size() - 1);
        long lastDayEndTimestamp = lastRecord.getTimestamp();
        if (log.isDebugEnabled()) {
            log.debug("Last timestamp retrieved from performance metrics records: " + lastDayEndTimestamp);
        }
        return lastDayEndTimestamp;
    }

    /**
     * Get the correct number of days to display records considering the starting date of Metrics V5 feature.
     *
     * @param noOfDays - given number of days
     * @return - corrected number of days
     */
    private static int getNumberOfDaysAccordingToV5StartDay(int noOfDays) throws OpenBankingException {

        LocalDate currentDate = LocalDate.now(timeZone);
        if (metricsV5StartDate.isAfter(currentDate)) {
            throw new OpenBankingException("Metrics V5 start date is configured incorrectly. Please set a " +
                    "past date as the starting date.");
        } else {
            int dayDifference = (int) ChronoUnit.DAYS.between(metricsV5StartDate, currentDate);
            if (noOfDays > dayDifference) {
                if (log.isDebugEnabled()) {
                    log.debug("Calculated day difference according to the configured Metrics V5 start date: " +
                            dayDifference);
                }
                noOfDays = dayDifference;
            }
        }
        return noOfDays;
    }

    /**
     * Get a map to add performance metrics values for given number of days initialized with 1.00 for 24 hours.
     * If it's the current day, the map will be initialized with 1.00 upto current hour.
     *
     * @param noOfDays - Number of days
     * @return - Initial performance map
     */
    private static Map<PriorityEnum, List<List<BigDecimal>>> getInitialPerformanceMetricsMap(
            int noOfDays, boolean isCurrentDay) {

        Map<PriorityEnum, List<List<BigDecimal>>> initialPerformanceMap = new EnumMap<>(PriorityEnum.class);
        int noOfHours = isCurrentDay ? ZonedDateTime.now(timeZone).getHour() + 1 : 24;

        for (PriorityEnum priority : PriorityEnum.values()) {
            List<List<BigDecimal>> daysPerformance = getInitialHourlyPerformanceListForDays(noOfDays, noOfHours);
            initialPerformanceMap.put(priority, daysPerformance);
        }
        return initialPerformanceMap;
    }

    /**
     * Get a list of hourly performance values for given number of days initialized with
     * 1.00 for given number of hours.
     *
     * @param noOfDays  - Number of days
     * @param noOfHours - Number of hours
     * @return - Initial hourly performance list
     */
    private static List<List<BigDecimal>> getInitialHourlyPerformanceListForDays(int noOfDays, int noOfHours) {

        List<List<BigDecimal>> daysPerformance = new ArrayList<>();
        BigDecimal initialValue = new BigDecimal("1.000");
        for (int day = 0; day < noOfDays; day++) {
            List<BigDecimal> hourlyPerformance = new ArrayList<>(Collections.nCopies(noOfHours, initialValue));
            daysPerformance.add(hourlyPerformance);
        }
        return daysPerformance;
    }

    /**
     * Parse performance metrics JSON object to a List.
     * Json object should contain an array of records with performance tier, hourly timestamp and performance value.
     * Expected JSON object sample:
     * <pre>{@code
     * {
     *     "records": [
     *         [
     *             "LargePayload",
     *             1702278000000,
     *             0.92
     *         ],
     *         .....
     *     ]
     * }
     * }</pre>
     *
     * @param performanceJsonObject - JSON object with performance metrics
     * @return - List of performance records
     */
    protected static List<PerformanceMetric> parsePerformanceMetricsJson(JSONObject performanceJsonObject) {

        JSONArray performanceRecordsArray = (JSONArray) performanceJsonObject.get(RECORDS);
        List<PerformanceMetric> performanceRecords = new ArrayList<>();

        for (Object record : performanceRecordsArray) {
            JSONArray recordArray = (JSONArray) record;
            PerformanceMetric performanceRecord = new PerformanceMetric();
            performanceRecord.setPriorityTier(recordArray.get(0).toString());
            performanceRecord.setTimestamp((long) recordArray.get(1));
            performanceRecord.setPerformanceValue(DefaultConverter.convertToDouble(recordArray.get(2)));
            performanceRecords.add(performanceRecord);
        }
        return performanceRecords;
    }

    /**
     * Populate a map of average TPS metrics data.
     *
     * @param invocationByAspectMetricsJsonObject - Json object with invocation metrics
     * @param numberOfDays                        - Number of days to consider
     * @param metricsCountLastDateEpoch           - Epoch timestamp of the last date that metrics are required
     * @return - populated map
     */
    public static Map<AspectEnum, List<BigDecimal>> getPopulatedAverageTPSMetricsMap(
            JSONObject invocationByAspectMetricsJsonObject, int numberOfDays, long metricsCountLastDateEpoch) {

        Map<AspectEnum, List<Integer>> invocationByAspectMetricsMap =
                getPopulatedInvocationByAspectMetricsMap(invocationByAspectMetricsJsonObject, numberOfDays,
                        metricsCountLastDateEpoch);

        Map<AspectEnum, List<BigDecimal>> averageTPSMap = new HashMap<>();
        List<BigDecimal> aggregateAverageTPSList = new ArrayList<>();
        List<BigDecimal> authenticatedAverageTPSList = new ArrayList<>();
        List<BigDecimal> unauthenticatedAverageTPSList = new ArrayList<>();

        Map<AspectEnum, List<Integer>> authenticatedInvocationMetricsMap = new HashMap<>();
        authenticatedInvocationMetricsMap.put(AspectEnum.AUTHENTICATED,
                invocationByAspectMetricsMap.get(AspectEnum.AUTHENTICATED));

        Map<AspectEnum, List<Integer>> unAuthenticatedInvocationMetricsMap = new HashMap<>();
        unAuthenticatedInvocationMetricsMap.put(AspectEnum.UNAUTHENTICATED,
                invocationByAspectMetricsMap.get(AspectEnum.UNAUTHENTICATED));

        List<Integer> totalInvocationList =
                MetricsProcessorUtil.getTotalInvocationsForEachDay(invocationByAspectMetricsMap,
                        AspectEnum.values());
        List<Integer> authenticatedInvocationList =
                MetricsProcessorUtil.getTotalInvocationsForEachDay(authenticatedInvocationMetricsMap,
                        new AspectEnum[]{AspectEnum.AUTHENTICATED});
        List<Integer> unauthenticatedInvocationList =
                MetricsProcessorUtil.getTotalInvocationsForEachDay(unAuthenticatedInvocationMetricsMap,
                        new AspectEnum[]{AspectEnum.UNAUTHENTICATED});

        MetricsProcessorUtil.populateAverageTPSList(totalInvocationList, aggregateAverageTPSList);
        MetricsProcessorUtil.populateAverageTPSList(authenticatedInvocationList, authenticatedAverageTPSList);
        MetricsProcessorUtil.populateAverageTPSList(unauthenticatedInvocationList, unauthenticatedAverageTPSList);

        averageTPSMap.put(AspectEnum.ALL, aggregateAverageTPSList);
        averageTPSMap.put(AspectEnum.AUTHENTICATED, authenticatedAverageTPSList);
        averageTPSMap.put(AspectEnum.UNAUTHENTICATED, unauthenticatedAverageTPSList);

        return averageTPSMap;
    }

    /**
     * Populate a map of total response time data categorized to priority tiers.
     *
     * @param metricsJsonObject         - Json object with total response metrics
     * @param numberOfDays              - Number of days to consider
     * @param metricsCountLastDateEpoch - Epoch timestamp of the last date that metrics are required
     * @return - populated map
     */
    public static Map<PriorityEnum, List<BigDecimal>> getPopulatedTotalResponseTimeMetricsMap(
            JSONObject metricsJsonObject, int numberOfDays, long metricsCountLastDateEpoch) {

        Map<PriorityEnum, List<BigDecimal>> dataMap = initializePriorityMap(numberOfDays, BigDecimal.ZERO);
        JSONArray records = (JSONArray) metricsJsonObject.get(RECORDS);
        for (Object recordObj : records) {
            JSONArray record = (JSONArray) recordObj;
            PriorityEnum priority = PriorityEnum.fromValue((String) record.get(0));
            BigDecimal count = BigDecimal.valueOf((Double) record.get(1));
            long recordTimestamp = (Long) record.get(2);
            int daysAgo = DateTimeUtil.getDayDifference(recordTimestamp / 1000, metricsCountLastDateEpoch);

            // Number of days ago can be used as the index to insert data to the list
            if (daysAgo >= 0 && daysAgo < numberOfDays) {
                dataMap.get(priority).set(
                        daysAgo, dataMap.get(priority).get(daysAgo).add(count));
            }
        }
        return dataMap;
    }

    /**
     * Populate a list of metrics data.
     * List elements are grouped by days.
     *
     * @param metricsJsonObject - Json object with metrics
     * @return - populated map
     */
    public static List<Integer> getPopulatedMetricsList(
            JSONObject metricsJsonObject, int numberOfDays, long metricsCountLastDateEpoch) {

        List<Integer> dataList = initializeList(numberOfDays, 0);
        JSONArray records = (JSONArray) metricsJsonObject.get(RECORDS);
        for (Object recordObj : records) {
            JSONArray record = (JSONArray) recordObj;
            Integer count = (Integer) record.get(0);
            long recordTimestamp = (Long) record.get(1);
            int daysAgo = DateTimeUtil.getDayDifference(recordTimestamp / 1000, metricsCountLastDateEpoch);

            // Number of days ago can be used as the index to insert data to the list
            if (daysAgo >= 0 && daysAgo < numberOfDays) {
                dataList.set(daysAgo, dataList.get(daysAgo) + count);
            }
        }
        return dataList;
    }

    /**
     * This method maps the retrieved error metrics to a list of ErrorMetricDataModel objects.
     *
     * @param errorMetricsJsonObject - retrieved error metrics data
     * @return List of ErrorMetricDataModel objects
     */
    public static List<ErrorMetricDataModel> mapToErrorMetricDataModel(JSONObject errorMetricsJsonObject) {

        List<ErrorMetricDataModel> errorMetricDataModelList = new ArrayList<>();
        JSONArray recordsArray = (JSONArray) errorMetricsJsonObject.get(RECORDS);

        for (Object singleRecordObject : recordsArray) {
            if (singleRecordObject instanceof JSONArray) {
                JSONArray singleRecordArray = (JSONArray) singleRecordObject;

                ErrorMetricDataModel errorMetricDataModel = new ErrorMetricDataModel(singleRecordArray);

                errorMetricDataModelList.add(errorMetricDataModel);
            }
        }

        return errorMetricDataModelList;
    }

    /**
     * This method maps the retrieved authorisation metrics to a list of ActiveAuthorisationMetricDataModel objects.
     *
     * @param authorisationMetricsJsonObject - retrieved authorisation metrics data
     * @return List of ActiveAuthorisationMetricDataModel objects
     */
    public static List<ActiveAuthorisationMetricDataModel> mapToActiveAuthorisationMetricDataModel(
            JSONObject authorisationMetricsJsonObject) {

        List<ActiveAuthorisationMetricDataModel> activeAuthorisationMetricDataModelList = new ArrayList<>();
        JSONArray recordsArray = (JSONArray) authorisationMetricsJsonObject.get(RECORDS);

        for (Object singleRecordObject : recordsArray) {
            if (singleRecordObject instanceof JSONArray) {
                JSONArray singleRecordArray = (JSONArray) singleRecordObject;

                ActiveAuthorisationMetricDataModel activeAuthorisationMetricDataModel =
                        new ActiveAuthorisationMetricDataModel(singleRecordArray);

                activeAuthorisationMetricDataModelList.add(activeAuthorisationMetricDataModel);
            }
        }

        return activeAuthorisationMetricDataModelList;
    }

    /**
     * This method maps the retrieved authorisation metrics to a list of AuthorisationMetricDataModel objects.
     *
     * @param authorisationMetricsJsonObject - retrieved authorisation metrics data
     * @return List of AuthorisationMetricDataModel objects
     */
    public static List<AuthorisationMetricDataModel> mapToAuthorisationMetricDataModel(
            JSONObject authorisationMetricsJsonObject) {

        List<AuthorisationMetricDataModel> authorisationMetricDataModelList = new ArrayList<>();
        JSONArray recordsArray = (JSONArray) authorisationMetricsJsonObject.get(RECORDS);

        for (Object singleRecordObject : recordsArray) {
            if (singleRecordObject instanceof JSONArray) {
                JSONArray singleRecordArray = (JSONArray) singleRecordObject;

                AuthorisationMetricDataModel authorisationMetricDataModel =
                        new AuthorisationMetricDataModel(singleRecordArray);

                authorisationMetricDataModelList.add(authorisationMetricDataModel);
            }
        }

        return authorisationMetricDataModelList;
    }

    /**
     * This method maps the retrieved abandoned consent flow metrics to a list of
     * AbandonedConsentFlowMetricDataModel objects.
     *
     * @param abandonedConsentFlowMetricsJsonObject - retrieved abandoned consent flow metrics data
     * @return List of AbandonedConsentFlowMetricDataModel objects
     */
    public static List<AbandonedConsentFlowMetricDataModel> mapToAbandonedConsentFlowMetricDataModel(
            JSONObject abandonedConsentFlowMetricsJsonObject) {

        List<AbandonedConsentFlowMetricDataModel> abandonedConsentFlowMetricDataModelList = new ArrayList<>();
        JSONArray recordsArray = (JSONArray) abandonedConsentFlowMetricsJsonObject.get(RECORDS);

        for (Object singleRecordObject : recordsArray) {
            if (singleRecordObject instanceof JSONArray) {
                JSONArray singleRecordArray = (JSONArray) singleRecordObject;

                AbandonedConsentFlowMetricDataModel abandonedConsentFlowMetricDataModel =
                        new AbandonedConsentFlowMetricDataModel(singleRecordArray);

                abandonedConsentFlowMetricDataModelList.add(abandonedConsentFlowMetricDataModel);
            }
        }

        return abandonedConsentFlowMetricDataModelList;
    }

    /**
     * Maps all the AbandonedConsentFlowMetricDataModel to AuthorisationStageTimestamp,
     * this will help to identify the timestamps of each stage of an authorisation.
     *
     * @param abandonedConsentFlowMetricDataModelList - list of AbandonedConsentFlowMetricDataModel
     * @return - list of AuthorisationStageTimestamp
     */
    public static List<AuthorisationStageTimestamp> getAuthorisationStageTimestampList(
            List<AbandonedConsentFlowMetricDataModel> abandonedConsentFlowMetricDataModelList) {

        Map<String, AuthorisationStageTimestamp> authorisationStageTimestampMap = new HashMap<>();
        for (AbandonedConsentFlowMetricDataModel abandonedConsentFlowMetricDataModel :
                abandonedConsentFlowMetricDataModelList) {
            String requestUriKey = abandonedConsentFlowMetricDataModel.getRequestUriKey();
            AuthorisationStageEnum stage = abandonedConsentFlowMetricDataModel.getStage();
            long timestamp = abandonedConsentFlowMetricDataModel.getTimestamp();

            // Check if the request URI key already exists in the map, if true then update that object.
            if (authorisationStageTimestampMap.containsKey(requestUriKey)) {
                AuthorisationStageTimestamp authorisationStageTimestamp = authorisationStageTimestampMap
                        .get(requestUriKey);

                authorisationStageTimestamp.setTimestampByStage(stage, timestamp);
            } else {
                AuthorisationStageTimestamp authorisationStageTimestamp = new AuthorisationStageTimestamp();
                authorisationStageTimestamp.setRequestUriKey(requestUriKey);

                authorisationStageTimestamp.setTimestampByStage(stage, timestamp);

                authorisationStageTimestampMap.put(requestUriKey, authorisationStageTimestamp);
            }
        }

        return new ArrayList<>(authorisationStageTimestampMap.values());
    }

    /**
     * Returns the authorisations that are currently in Authorised state.
     *
     * @param activeAuthorisationMetricDataModelList - list of ActiveAuthorisationMetricDataModel
     * @return - map of active authorisations
     */
    public static Map<String, ActiveAuthorisationMetricDataModel> getActiveAuthorisationsMap(
            List<ActiveAuthorisationMetricDataModel> activeAuthorisationMetricDataModelList) {

        Map<String, ActiveAuthorisationMetricDataModel> latestConsentStatusMap = new HashMap<>();
        for (ActiveAuthorisationMetricDataModel activeAuthorisationMetricDataModel :
                activeAuthorisationMetricDataModelList) {
            String consentId = activeAuthorisationMetricDataModel.getConsentId();
            long timestamp = activeAuthorisationMetricDataModel.getTimestamp();

            // Check if the consent id is already in the map and if the timestamp is greater
            if (!latestConsentStatusMap.containsKey(consentId) ||
                    timestamp > latestConsentStatusMap.get(consentId).getTimestamp()) {
                latestConsentStatusMap.put(consentId, activeAuthorisationMetricDataModel);
            }
        }

        // filtering only the consents whose latest status is authorised
        return latestConsentStatusMap.entrySet()
                .stream()
                .filter(entry -> ConsentStatusEnum.AUTHORISED.equals(entry.getValue().getConsentStatus()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * This method initializes the errorMetricDayList with date values.
     *
     * @param errorMetricDayList - List of ErrorMetricDay objects
     */
    public static void initializeErrorMetricDayList(List<ErrorMetricDay> errorMetricDayList, int numberOfDays) {

        LocalDate today = LocalDate.now(timeZone);

        // If the request is only historic, then populate days starting from yesterday
        LocalDate startingDate = numberOfDays == 7 ? today.minusDays(1) : today;

        for (int i = 0; i < numberOfDays; i++) {
            LocalDate date = startingDate.minusDays(i);
            ErrorMetricDay errorMetricDay = new ErrorMetricDay();
            errorMetricDay.setDate(date);
            errorMetricDayList.add(errorMetricDay);
        }
    }

    /**
     * This method initializes the authorisationMetricDayList with date values.
     *
     * @param authorisationMetricDayList - List of AuthorisationMetricDay objects
     */
    public static void initializeAuthorisationMetricDayList(List<AuthorisationMetricDay> authorisationMetricDayList,
                                                            int numberOfDays) {

        LocalDate today = LocalDate.now(timeZone);

        // If the request is only historic, then populate days starting from yesterday
        LocalDate startingDate = numberOfDays == 7 ? today.minusDays(1) : today;

        for (int i = 0; i < numberOfDays; i++) {
            LocalDate date = startingDate.minusDays(i);
            AuthorisationMetricDay authorisationMetricDay = new AuthorisationMetricDay();
            authorisationMetricDay.setDate(date);
            authorisationMetricDayList.add(authorisationMetricDay);
        }
    }

    /**
     * This method initializes the abandonedConsentFlowByStageMetricsDayList with date values.
     *
     * @param abandonedConsentFlowByStageMetricsDayList - List of AbandonedConsentFlowByStageMetricDay objects
     */
    public static void initializeAbandonedConsentFlowByStageMetricDayList(
            List<AbandonedConsentFlowByStageMetricDay> abandonedConsentFlowByStageMetricsDayList, int numberOfDays) {

        LocalDate today = LocalDate.now(timeZone);

        // If the request is only historic, then populate days starting from yesterday
        LocalDate startingDate = numberOfDays == 7 ? today.minusDays(1) : today;

        for (int i = 0; i < numberOfDays; i++) {
            LocalDate date = startingDate.minusDays(i);
            AbandonedConsentFlowByStageMetricDay abandonedConsentFlowByStageMetricDay =
                    new AbandonedConsentFlowByStageMetricDay();
            abandonedConsentFlowByStageMetricDay.setDate(date);
            abandonedConsentFlowByStageMetricsDayList.add(abandonedConsentFlowByStageMetricDay);
        }
    }

    /**
     * Compose error metrics data per day.
     *
     * @param errorMetricDataModelList - list of error metrics data model
     * @param errorMetricDayList       - list of error metrics per day
     * @return - list of error metrics per day
     */
    public static List<ErrorMetricDay> populateErrorMetricDayList(List<ErrorMetricDataModel> errorMetricDataModelList,
                                                                  List<ErrorMetricDay> errorMetricDayList) {

        for (ErrorMetricDataModel errorMetricDataModel : errorMetricDataModelList) {
            for (int i = 0; i < errorMetricDayList.size(); i++) {
                ErrorMetricDay errorMetricDay = errorMetricDayList.get(i);
                if (isTimestampInDate(errorMetricDataModel.getTimestamp(), errorMetricDay.getDate())) {

                    Map<String, Integer> unauthenticatedErrorMap = errorMetricDay.getUnauthenticatedErrorMap();
                    Map<String, Integer> authenticatedErrorMap = errorMetricDay.getAuthenticatedErrorMap();

                    if (errorMetricDataModel.getAspect().equals(AspectEnum.UNAUTHENTICATED)) {
                        // updating the unauthenticated errors map
                        if (unauthenticatedErrorMap.containsKey(errorMetricDataModel.getStatusCode())) {
                            Integer errorCount = unauthenticatedErrorMap.get(errorMetricDataModel.getStatusCode());
                            Integer newErrorCount = errorCount + errorMetricDataModel.getCount();
                            unauthenticatedErrorMap.put(errorMetricDataModel.getStatusCode(), newErrorCount);
                        } else {
                            unauthenticatedErrorMap.put(errorMetricDataModel.getStatusCode(),
                                    errorMetricDataModel.getCount());
                        }
                    } else {
                        // updating the authenticated errors map
                        if (authenticatedErrorMap.containsKey(errorMetricDataModel.getStatusCode())) {
                            Integer errorCount = authenticatedErrorMap.get(errorMetricDataModel.getStatusCode());
                            Integer newErrorCount = errorCount + errorMetricDataModel.getCount();
                            authenticatedErrorMap.put(errorMetricDataModel.getStatusCode(), newErrorCount);
                        } else {
                            authenticatedErrorMap.put(errorMetricDataModel.getStatusCode(),
                                    errorMetricDataModel.getCount());
                        }
                    }

                    // setting the updated error maps
                    errorMetricDay.setUnauthenticatedErrorMap(unauthenticatedErrorMap);
                    errorMetricDay.setAuthenticatedErrorMap(authenticatedErrorMap);
                }
            }
        }

        return errorMetricDayList;
    }

    /**
     * Compose authorisation metrics data per day.
     *
     * @param authorisationMetricDayList       - list of authorisation metrics per day
     * @param authorisationMetricDataModelList - list of authorisation metrics data model
     * @return - list of authorisation metrics per day
     */
    public static List<AuthorisationMetricDay> populateAuthorisationMetricDayList(
            List<AuthorisationMetricDataModel> authorisationMetricDataModelList,
            List<AuthorisationMetricDay> authorisationMetricDayList) {

        for (AuthorisationMetricDataModel authorisationMetricDataModel : authorisationMetricDataModelList) {
            for (int i = 0; i < authorisationMetricDayList.size(); i++) {
                AuthorisationMetricDay authorisationMetricDay = authorisationMetricDayList.get(i);
                if (isTimestampInDate(authorisationMetricDataModel.getTimestamp(),
                        authorisationMetricDay.getDate())) {

                    // populating new authorisation metrics
                    if (ConsentStatusEnum.AUTHORISED.equals(authorisationMetricDataModel.getConsentStatus()) &&
                            AuthorisationFlowTypeEnum.CONSENT_AUTHORISATION.equals(
                                    authorisationMetricDataModel.getAuthFlowType()) &&
                            ConsentDurationTypeEnum.ONGOING.equals(authorisationMetricDataModel
                                    .getConsentDurationType())) {
                        if (authorisationMetricDataModel.getCustomerProfile().contains(MetricsConstants.INDIVIDUAL)) {
                            authorisationMetricDay.getNewAuthorisationMetric().getOngoing()
                                    .updateIndividual(authorisationMetricDataModel.getCount());
                        } else {
                            authorisationMetricDay.getNewAuthorisationMetric().getOngoing()
                                    .updateNonIndividual(authorisationMetricDataModel.getCount());
                        }
                    }

                    if (ConsentStatusEnum.AUTHORISED.equals(authorisationMetricDataModel.getConsentStatus()) &&
                            AuthorisationFlowTypeEnum.CONSENT_AUTHORISATION.equals(
                                    authorisationMetricDataModel.getAuthFlowType()) &&
                            ConsentDurationTypeEnum.ONCE_OFF.equals(authorisationMetricDataModel
                                    .getConsentDurationType())) {
                        if (authorisationMetricDataModel.getCustomerProfile().contains(MetricsConstants.INDIVIDUAL)) {
                            authorisationMetricDay.getNewAuthorisationMetric().getOnceOff()
                                    .updateIndividual(authorisationMetricDataModel.getCount());
                        } else {
                            authorisationMetricDay.getNewAuthorisationMetric().getOnceOff()
                                    .updateNonIndividual(authorisationMetricDataModel.getCount());
                        }
                    }

                    // populating revoked authorisation metrics
                    if (ConsentStatusEnum.REVOKED.equals(authorisationMetricDataModel.getConsentStatus()) &&
                            ConsentDurationTypeEnum.ONGOING.equals(authorisationMetricDataModel
                                    .getConsentDurationType())) {
                        if (authorisationMetricDataModel.getCustomerProfile().contains(MetricsConstants.INDIVIDUAL)) {
                            authorisationMetricDay.getRevokedAuthorisationMetric().getOngoing()
                                    .updateIndividual(authorisationMetricDataModel.getCount());
                        } else {
                            authorisationMetricDay.getRevokedAuthorisationMetric().getOngoing()
                                    .updateNonIndividual(authorisationMetricDataModel.getCount());
                        }
                    }

                    // populating amended authorisation metrics
                    if (ConsentStatusEnum.AUTHORISED.equals(authorisationMetricDataModel.getConsentStatus()) &&
                            AuthorisationFlowTypeEnum.CONSENT_AMENDMENT_AUTHORISATION.equals(
                                    authorisationMetricDataModel.getAuthFlowType()) &&
                            ConsentDurationTypeEnum.ONGOING.equals(authorisationMetricDataModel
                                    .getConsentDurationType())) {
                        if (authorisationMetricDataModel.getCustomerProfile().contains(MetricsConstants.INDIVIDUAL)) {
                            authorisationMetricDay.getAmendedAuthorisationMetric().getOngoing()
                                    .updateIndividual(authorisationMetricDataModel.getCount());
                        } else {
                            authorisationMetricDay.getAmendedAuthorisationMetric().getOngoing()
                                    .updateNonIndividual(authorisationMetricDataModel.getCount());
                        }
                    }

                    // populating expired authorisation metrics
                    if (ConsentStatusEnum.EXPIRED.equals(authorisationMetricDataModel.getConsentStatus()) &&
                            ConsentDurationTypeEnum.ONGOING.equals(authorisationMetricDataModel
                                    .getConsentDurationType())) {
                        if (authorisationMetricDataModel.getCustomerProfile().contains(MetricsConstants.INDIVIDUAL)) {
                            authorisationMetricDay.getExpiredAuthorisationMetric().getOngoing()
                                    .updateIndividual(authorisationMetricDataModel.getCount());
                        } else {
                            authorisationMetricDay.getExpiredAuthorisationMetric().getOngoing()
                                    .updateNonIndividual(authorisationMetricDataModel.getCount());
                        }
                    }

                }
            }
        }

        return authorisationMetricDayList;
    }

    /**
     * Compose abandoned consent flow by stage metrics data per day.
     *
     * @param authorisationStageTimeList                - list of authorisation stage timestamps
     * @param abandonedConsentFlowByStageMetricsDayList - list of abandoned consent flow by stage metrics per day
     * @return - list of abandoned consent flow by stage metrics per day
     */
    public static List<AbandonedConsentFlowByStageMetricDay> populateAbandonedConsentFlowByStageMetricDayList(
            List<AuthorisationStageTimestamp> authorisationStageTimeList,
            List<AbandonedConsentFlowByStageMetricDay> abandonedConsentFlowByStageMetricsDayList) {

        // Looping through the table records
        for (AuthorisationStageTimestamp authorisationStageTimestamp : authorisationStageTimeList) {

            // Skipping the records that are completed
            if (authorisationStageTimestamp.getCompletedTimestamp() == 0) {
                long timestampToCheck = 0;
                AbandonmentsByStageEnum stage = null;

                if (authorisationStageTimestamp.getConsentRejectedTimestamp() > 0) {
                    // Updating the rejected metric if the consent flow was rejected
                    timestampToCheck = authorisationStageTimestamp.getConsentRejectedTimestamp();
                    stage = AbandonmentsByStageEnum.REJECTED;
                } else if (authorisationStageTimestamp.getTokenExchangeFailedTimestamp() > 0) {
                    // Updating the failedTokenExchange metric if the token exchange failed
                    timestampToCheck = authorisationStageTimestamp.getTokenExchangeFailedTimestamp();
                    stage = AbandonmentsByStageEnum.FAILED_TOKEN_EXCHANGE;
                } else if (authorisationStageTimestamp.getConsentApprovedTimestamp() > 0) {
                    long elapsedTimeMillis = getElapsedTimeMillis(authorisationStageTimestamp
                            .getConsentApprovedTimestamp());
                    if (elapsedTimeMillis > authCodeValidityPeriod) {
                        // Updating the failedTokenExchange metric if the consent flow has consentApproved but
                        // not completed after the auth code has expired
                        timestampToCheck = authorisationStageTimestamp.getConsentApprovedTimestamp() +
                                authCodeValidityPeriod;
                        stage = AbandonmentsByStageEnum.FAILED_TOKEN_EXCHANGE;
                    }
                } else if (authorisationStageTimestamp.getAccountSelectedTimestamp() > 0) {
                    long elapsedTimeMillis = getElapsedTimeMillis(authorisationStageTimestamp
                            .getAccountSelectedTimestamp());
                    if (elapsedTimeMillis > consentAbandonmentTime) {
                        // Updating the preAuthorisation metric if the consent flow has accountSelected but
                        // not consentApproved or consentRejected after the consent flow
                        // abandonment cut off time has passed
                        timestampToCheck = authorisationStageTimestamp.getAccountSelectedTimestamp() +
                                consentAbandonmentTime;
                        stage = AbandonmentsByStageEnum.PRE_AUTHORISATION;
                    }
                } else if (authorisationStageTimestamp.getUserAuthenticatedTimestamp() > 0) {
                    long elapsedTimeMillis = getElapsedTimeMillis(authorisationStageTimestamp
                            .getUserAuthenticatedTimestamp());
                    if (elapsedTimeMillis > consentAbandonmentTime) {
                        // Updating the preAccountSelection metric if the consent flow has userAuthenticated but
                        // not accountSelected after the consent flow abandonment cut off time has passed
                        timestampToCheck = authorisationStageTimestamp.getUserAuthenticatedTimestamp() +
                                consentAbandonmentTime;
                        stage = AbandonmentsByStageEnum.PRE_ACCOUNT_SELECTION;
                    }
                } else if (authorisationStageTimestamp.getUserIdentifiedTimestamp() > 0) {
                    long elapsedTimeMillis = getElapsedTimeMillis(authorisationStageTimestamp
                            .getUserIdentifiedTimestamp());
                    if (elapsedTimeMillis > consentAbandonmentTime) {
                        // Updating the preAuthentication metric if the consent flow has userIdentified but
                        // not userAuthenticated after the consent flow abandonment cut off time has passed
                        timestampToCheck = authorisationStageTimestamp.getUserIdentifiedTimestamp() +
                                consentAbandonmentTime;
                        stage = AbandonmentsByStageEnum.PRE_AUTHENTICATION;
                    }
                } else if (authorisationStageTimestamp.getStartedTimestamp() > 0) {
                    long elapsedTimeMillis = getElapsedTimeMillis(authorisationStageTimestamp
                            .getStartedTimestamp());
                    if (elapsedTimeMillis > consentAbandonmentTime) {
                        // Updating the preIdentification metric if the consent flow has started but
                        // not userIdentified after the consent flow abandonment cut off time has passed
                        timestampToCheck = authorisationStageTimestamp.getStartedTimestamp() +
                                consentAbandonmentTime;
                        stage = AbandonmentsByStageEnum.PRE_IDENTIFICATION;
                    }
                }

                if (stage != null) {
                    for (AbandonedConsentFlowByStageMetricDay metricDay : abandonedConsentFlowByStageMetricsDayList) {
                        if (isTimestampInDate(timestampToCheck, metricDay.getDate())) {
                            updateMetricCount(stage, metricDay);
                            break;
                        }
                    }
                }
            }
        }

        return abandonedConsentFlowByStageMetricsDayList;
    }

    private static long getElapsedTimeMillis(long timestampInMillis) {

        return System.currentTimeMillis() - timestampInMillis;
    }

    private static void updateMetricCount(AbandonmentsByStageEnum stage,
                                          AbandonedConsentFlowByStageMetricDay metricDay) {
        switch (stage) {
            case REJECTED:
                metricDay.setAbandonedByRejectedStageCount(metricDay
                        .getAbandonedByRejectedStageCount() + 1);
                break;
            case PRE_IDENTIFICATION:
                metricDay.setAbandonedByPreIdentificationStageCount(metricDay
                        .getAbandonedByPreIdentificationStageCount() + 1);
                break;
            case PRE_AUTHENTICATION:
                metricDay.setAbandonedByPreAuthenticationStageCount(metricDay
                        .getAbandonedByPreAuthenticationStageCount() + 1);
                break;
            case PRE_ACCOUNT_SELECTION:
                metricDay.setAbandonedByPreAccountSelectionStageCount(metricDay
                        .getAbandonedByPreAccountSelectionStageCount() + 1);
                break;
            case PRE_AUTHORISATION:
                metricDay.setAbandonedByPreAuthorisationStageCount(metricDay
                        .getAbandonedByPreAuthorisationStageCount() + 1);
                break;
            case FAILED_TOKEN_EXCHANGE:
                metricDay.setAbandonedByFailedTokenExchangeStageCount(metricDay
                        .getAbandonedByFailedTokenExchangeStageCount() + 1);
                break;
            default:
                break;
        }
    }

    /**
     * Checks if a timestamp is in a specific date.
     *
     * @param timestamp  - timestamp to be checked
     * @param targetDate - date to be checked against
     * @return boolean
     */
    private static boolean isTimestampInDate(long timestamp, LocalDate targetDate) {

        // Convert GMT timestamp to Instant
        Instant instant = Instant.ofEpochMilli(timestamp);

        // Convert Instant to ZonedDateTime using GMT time zone
        ZonedDateTime zonedDateTime = instant.atZone(timeZone);

        // Extract the date from the ZonedDateTime
        LocalDate timestampDate = zonedDateTime.toLocalDate();

        // Compare the extracted date with the target date
        return timestampDate.equals(targetDate);
    }

    /**
     * Get list of elements from the JSON object.
     * Groups elements to authenticated and unauthenticated lists.
     *
     * @param jsonObject       - JSON object
     * @param numberOfPastDays - Number of past days to consider
     * @return - List of elements
     */
    @Generated(message = "Excluded from code coverage")
    public static List<ArrayList<Integer>> getListFromRejectionsJson(
            JSONObject jsonObject, int numberOfPastDays, long metricsCountLastDateEpoch) {

        JSONArray recordsArray = (JSONArray) jsonObject.get(RECORDS);
        ArrayList<Integer> elementListAuthenticated = initializeList(numberOfPastDays, 0);
        ArrayList<Integer> elementListUnauthenticated = initializeList(numberOfPastDays, 0);
        populateRejectionLists(recordsArray, elementListAuthenticated, elementListUnauthenticated,
                metricsCountLastDateEpoch);

        // 2 lists for authenticated and unauthenticated elements
        List<ArrayList<Integer>> elementList = new ArrayList<>(2);
        elementList.add(elementListAuthenticated);
        elementList.add(elementListUnauthenticated);

        return elementList;
    }

    /**
     * Populate the lists with elements from the JSON array.
     * Used for rejection metrics where elements need to be grouped as authenticated and unauthenticated.
     *
     * @param recordsArray    - JSON array with elements
     * @param authenticated   - List of authenticated elements
     * @param unauthenticated - List of unauthenticated elements
     */
    private static void populateRejectionLists(JSONArray recordsArray, ArrayList<Integer> authenticated,
                                               ArrayList<Integer> unauthenticated, long metricsCountLastDateEpoch) {

        for (Object object : recordsArray) {
            JSONArray countArray = (JSONArray) object;
            long currentElement = Long.parseLong(countArray.get(0).toString());
            long recordTimestamp = Long.parseLong(countArray.get(1).toString());
            String username = (String) countArray.get(2);
            int daysAgo = DateTimeUtil.getDayDifference(recordTimestamp, metricsCountLastDateEpoch);

            // Number of days ago can be used as the index to insert data to the list
            Integer currentCount = (int) currentElement;
            if (StringUtils.isNotBlank(username)) {
                authenticated.set(daysAgo, authenticated.get(daysAgo) + currentCount);
            } else {
                unauthenticated.set(daysAgo, unauthenticated.get(daysAgo) + currentCount);
            }
        }
    }

    /**
     * Get availability from server outages for the given number of months.
     *
     * @param serverOutageDataList - Server Outage Data List
     * @param noOfMonths           - Number of months
     * @return Availability map
     */
    public static Map<AspectEnum, List<BigDecimal>> getAvailabilityMapFromServerOutages(
            List<ServerOutageDataModel> serverOutageDataList, int noOfMonths, ZonedDateTime endOfMonth) {

        Map<AspectEnum, List<BigDecimal>> availabilityMap = new HashMap<>();

        noOfMonths = calculateNumberOfMonths(serverOutageDataList, noOfMonths);
        List<BigDecimal> availabilityAggregatedList = initializeList(noOfMonths, BigDecimal.ONE);
        List<BigDecimal> availabilityAuthenticatedList = initializeList(noOfMonths, BigDecimal.ONE);
        List<BigDecimal> availabilityUnauthenticatedList = initializeList(noOfMonths, BigDecimal.ONE);

        ZonedDateTime currentEndOfMonth = endOfMonth;
        ZonedDateTime currentStartOfMonth;

        // Iterate through the months in reverse order
        for (int monthIndex = 0; monthIndex < noOfMonths; monthIndex++) {
            currentStartOfMonth = currentEndOfMonth.withHour(0).withMinute(0).withSecond(0).withDayOfMonth(1);
            long startTimestamp = currentStartOfMonth.toEpochSecond();
            long endTimestamp = currentEndOfMonth.toEpochSecond();

            // Computing availability for the particular month for the relevant aspect
            BigDecimal aggregateAvailability = getAvailabilityFromServerOutagesForTimeRange(serverOutageDataList,
                    startTimestamp, endTimestamp, AspectEnum.ALL);
            BigDecimal authenticatedAvailability = getAvailabilityFromServerOutagesForTimeRange(serverOutageDataList,
                    startTimestamp, endTimestamp, AspectEnum.AUTHENTICATED);
            BigDecimal unauthenticatedAvailability = getAvailabilityFromServerOutagesForTimeRange(serverOutageDataList,
                    startTimestamp, endTimestamp, AspectEnum.UNAUTHENTICATED);

            // Set availability for the month using current index
            availabilityAggregatedList.set(monthIndex, aggregateAvailability);
            availabilityAuthenticatedList.set(monthIndex, authenticatedAvailability);
            availabilityUnauthenticatedList.set(monthIndex, unauthenticatedAvailability);

            // Get end date of previous month for the next iteration
            currentEndOfMonth = currentStartOfMonth.minusDays(1).with(LocalTime.MAX);
        }

        availabilityMap.put(AspectEnum.ALL, availabilityAggregatedList);
        availabilityMap.put(AspectEnum.AUTHENTICATED, availabilityAuthenticatedList);
        availabilityMap.put(AspectEnum.UNAUTHENTICATED, availabilityUnauthenticatedList);

        return availabilityMap;
    }

    /**
     * Calculate the number of months based on the server outage data list
     *
     * @param serverOutageDataList server outage list
     * @param noOfMonths           number of months
     * @return computed number of months based on period
     */
    private static int calculateNumberOfMonths(List<ServerOutageDataModel> serverOutageDataList, int noOfMonths) {

        // The only values for noOfMonths that can come to this method are
        // 1 for CURRENT, 12 for HISTORIC and 13 for ALL.
        if (noOfMonths == 1) {
            return 1;
        }

        if (serverOutageDataList.isEmpty()) {
            return noOfMonths == 12 ? 0 : 1;
        }

        long earliestOutageTimestamp = serverOutageDataList.stream()
                .mapToLong(ServerOutageDataModel::getTimeFrom)
                .min()
                .getAsLong();

        LocalDate earliestOutageToDate = Instant.ofEpochSecond(earliestOutageTimestamp).atZone(timeZone).toLocalDate();
        LocalDate currentDate = LocalDate.now(timeZone);
        int numOfMonthsFromEarliestOutage = (int) ChronoUnit.MONTHS.between(
                earliestOutageToDate.withDayOfMonth(1), currentDate.withDayOfMonth(1));

        return Math.min(numOfMonthsFromEarliestOutage, noOfMonths);
    }

    /**
     * Get server availability between given time period from the list of ServerOutages.
     *
     * @param serverOutageDataList - Server Outage Data List
     * @param fromTime             - From epoch timestamp
     * @param toTime               - To epoch timestamp
     * @param aspect               - Aspect of the outages
     * @return availability value
     */
    public static BigDecimal getAvailabilityFromServerOutagesForTimeRange(
            List<ServerOutageDataModel> serverOutageDataList, long fromTime, long toTime, AspectEnum aspect) {

        long timeDurationOfReportingPeriod = toTime - fromTime;
        long totalScheduledOutages;
        long totalIncidentOutages;

        List<ServerOutageDataModel> scheduledOutages = new ArrayList<>();
        List<ServerOutageDataModel> incidentOutages = new ArrayList<>();

        // filter the outages. scheduled vs incidents
        for (ServerOutageDataModel dataModel : serverOutageDataList) {
            /*
            Filtering by aspect.
            If the aspect sent is ALL, then all the data models will be added to the list.
            If a specific aspect is requested, only the data models with that aspect will be added to the list along
             with common data models.
            If the aspect of the data model is ALL, then it belongs to any requested aspect.
            */
            if (aspect.equals(dataModel.getAspect()) || AspectEnum.ALL.equals(dataModel.getAspect()) || AspectEnum
                    .ALL.equals(aspect)) {
                if (dataModel.getTimeFrom() >= fromTime && dataModel.getTimeFrom() < toTime) {
                    if (MetricsConstants.SCHEDULED_OUTAGE.equals(dataModel.getType())) {
                        scheduledOutages.add(dataModel);
                    } else {
                        incidentOutages.add(dataModel);
                    }
                }
            }
        }

        // Calculate the summation of total time
        totalScheduledOutages = calculateServerOutageTime(scheduledOutages);
        totalIncidentOutages = calculateServerOutageTime(incidentOutages);

        // Formula to calculate the availability from total time
        double availability = ((double) timeDurationOfReportingPeriod - (double) totalScheduledOutages
                - (double) totalIncidentOutages) /
                ((double) timeDurationOfReportingPeriod - (double) totalScheduledOutages);

        return BigDecimal.valueOf(availability).setScale(3, RoundingMode.HALF_UP);
    }

    /**
     * Calculate total server outage time from ServerOutageDataModel.
     *
     * @param serverOutages - Server Outages
     * @return server outage time
     */
    @Generated(message = "Excluded from code coverage")
    private static long calculateServerOutageTime(List<ServerOutageDataModel> serverOutages) {

        long totalTime = 0;
        List<ServerOutageDataModel> filteredServerOutages = serverOutages.stream()
                .filter(outage -> outage.getTimeTo() >= outage.getTimeFrom())
                .distinct()
                .sorted(Comparator.comparingLong(ServerOutageDataModel::getTimeFrom))
                .collect(Collectors.toList());

        if (!filteredServerOutages.isEmpty()) {
            long currentEndTime = 0;
            for (int outageIndex = 0; outageIndex < filteredServerOutages.size(); outageIndex++) {
                ServerOutageDataModel serverOutage = filteredServerOutages.get(outageIndex);
                if (serverOutage.getTimeFrom() >= currentEndTime) {
                    // Not an overlap
                    totalTime += serverOutage.getTimeTo() - serverOutage.getTimeFrom();
                    currentEndTime = serverOutage.getTimeTo();
                } else if (serverOutage.getTimeTo() <= currentEndTime) {
                    // Complete overlap = ignore
                } else if (serverOutage.getTimeTo() > currentEndTime) {
                    // Overlap
                    totalTime += serverOutage.getTimeTo() - currentEndTime;
                    currentEndTime = serverOutage.getTimeTo();
                }
            }
        }
        return totalTime;
    }

    /**
     * Map server outages JSONObject to list of ServerOutageDataModels.
     *
     * @param availabilityMetricsJsonObject - Availability Metrics JsonObject
     * @return list of server outage data
     */
    public static List<ServerOutageDataModel> getServerOutageDataFromJson(JSONObject availabilityMetricsJsonObject) {

        List<ServerOutageDataModel> serverOutageDataModelList = new ArrayList<>();
        JSONArray records = (JSONArray) availabilityMetricsJsonObject.get(RECORDS);

        if (records != null) {
            for (Object record : records) {
                JSONArray serverOutageDateJsonObject = (JSONArray) record;
                ServerOutageDataModel dataModel = getServerOutageDataModel(serverOutageDateJsonObject);
                serverOutageDataModelList.add(dataModel);
            }
        }
        return serverOutageDataModelList;
    }

    /**
     * Map server outage JSONObject to ServerOutageDataModel.
     * <p>
     * [
     * "outageId",
     * timestamp (epoch seconds),
     * "type" (scheduled/incident),
     * time_from (epoch seconds),
     * time_to (epoch seconds),
     * aspect (all/authenticated/authenticated)
     * ]
     *
     * @param serverOutageDateJsonObject - Server Outage Date JsonObject
     * @return ServerOutageDataModel
     */
    private static ServerOutageDataModel getServerOutageDataModel(JSONArray serverOutageDateJsonObject) {

        return new ServerOutageDataModel(
                serverOutageDateJsonObject.get(0).toString(),
                Long.parseLong(serverOutageDateJsonObject.get(1).toString()),
                serverOutageDateJsonObject.get(2).toString(),
                Long.parseLong(serverOutageDateJsonObject.get(3).toString()),
                Long.parseLong(serverOutageDateJsonObject.get(4).toString()),
                AspectEnum.fromValue(serverOutageDateJsonObject.get(5).toString()));
    }

    /**
     * Get peak TPS map from the JSON array containing TPS data.
     *
     * @param tpsMetricsJsonArray - JSON array
     * @return - Peak TPS map with aspects and peak TPS for given number of days
     */
    public static Map<AspectEnum, List<BigDecimal>> getPeakTPSMapFromJsonArray(
            JSONArray tpsMetricsJsonArray, int numberOfDays, long metricsCountLastDateEpoch) {

        Map<AspectEnum, List<BigDecimal>> peakTpsMap = new HashMap<>();
        JSONArray authenticatedRecordsArray = getArrayByAspect(tpsMetricsJsonArray, AspectEnum.AUTHENTICATED);
        JSONArray unAuthenticatedRecordsArray = getArrayByAspect(tpsMetricsJsonArray, AspectEnum.UNAUTHENTICATED);

        peakTpsMap.put(AspectEnum.ALL, getPeakTpsList(tpsMetricsJsonArray, numberOfDays, metricsCountLastDateEpoch));
        peakTpsMap.put(AspectEnum.AUTHENTICATED, getPeakTpsList(authenticatedRecordsArray, numberOfDays,
                metricsCountLastDateEpoch));
        peakTpsMap.put(AspectEnum.UNAUTHENTICATED, getPeakTpsList(unAuthenticatedRecordsArray, numberOfDays,
                metricsCountLastDateEpoch));

        return peakTpsMap;
    }

    /**
     * Process TPS records array to get peak TPS list for given number of days.
     *
     * @param tpsRecordsArray - JSONArray
     * @return - List of peak TPS values
     */
    private static ArrayList<BigDecimal> getPeakTpsList(
            JSONArray tpsRecordsArray, int numberOfDays, long metricsCountLastDateEpoch) {

        ArrayList<BigDecimal> elementList = new ArrayList<>(Arrays.asList(new BigDecimal[numberOfDays]));
        Collections.fill(elementList, BigDecimal.valueOf(0));

        JSONObject eventObject;
        long currentElement;
        for (Object object : tpsRecordsArray) {
            JSONObject jsonObject = (JSONObject) object;
            eventObject = (JSONObject) jsonObject.get(MetricsConstants.EVENT);
            currentElement = ((Number) eventObject.get(MetricsConstants.TOTAL_COUNT)).longValue();
            long recordTimestamp = ((Number) eventObject.get(MetricsConstants.TIMESTAMP)).longValue();
            int daysAgo = DateTimeUtil.getDayDifference(recordTimestamp, metricsCountLastDateEpoch);

            if (daysAgo >= 0 && daysAgo < numberOfDays) {
                // Setting the max value to the aggregated peak TPS list
                BigDecimal newValue = BigDecimal.valueOf(currentElement);
                BigDecimal currentValueInList = elementList.get(daysAgo);
                int comparisonResult = newValue.compareTo(currentValueInList);
                if (comparisonResult > 0) {
                    elementList.set(daysAgo, newValue);
                }
            }
        }
        return elementList;
    }


    /**
     * Filter records by aspect and get an array for a single aspect
     *
     * @param recordsArray - records array
     * @param aspectEnum   - aspect (Authenticated / Unauthenticated)
     * @return - filtered array
     */
    private static JSONArray getArrayByAspect(JSONArray recordsArray, AspectEnum aspectEnum) {

        JSONArray filteredArray = new JSONArray();
        for (Object object : recordsArray) {
            JSONObject record = (JSONObject) object;
            JSONObject event = (JSONObject) record.get(MetricsConstants.EVENT);
            if (aspectEnum.toString().equals(event.get(MetricsConstants.ASPECT))) {
                filteredArray.add(record);
            }
        }
        return filteredArray;
    }


}

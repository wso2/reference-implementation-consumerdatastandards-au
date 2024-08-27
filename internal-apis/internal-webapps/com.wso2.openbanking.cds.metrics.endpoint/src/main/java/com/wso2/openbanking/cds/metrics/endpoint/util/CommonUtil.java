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

package org.wso2.openbanking.cds.metrics.endpoint.util;

import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class for common metrics utility functions.
 */
public class CommonUtil {

    private static final Log log = LogFactory.getLog(CommonUtil.class);

    /**
     * Get Admin API Self URL.
     *
     * @param period - period (ALL, CURRENT, HISTORIC)
     * @return self-url string
     */
    public static String getCDSAdminSelfLink(String period) {

        String adminAPIBaseURL = OpenBankingCDSConfigParser.getInstance().getAdminAPISelfLink();
        return String.format("%smetrics?period=%s", adminAPIBaseURL, period);
    }

    /**
     * Add missing months of full availability data from the start date to the current date or the first outage
     *
     * @param list availability data list
     * @return availability data list with additional values for missing months
     */
    public static List<BigDecimal> addMissingMonths(List<BigDecimal> list) {

        try {
            OpenBankingCDSConfigParser configParser = OpenBankingCDSConfigParser.getInstance();
            ZoneId timeZone = ZoneId.of(configParser.getMetricsTimeZone());
            String availabilityStartDateString = configParser.getAvailabilityStartDate();

            if (availabilityStartDateString == null || availabilityStartDateString.isEmpty()) {
                return list;
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            ZonedDateTime availabilityStartDate = LocalDate.parse(availabilityStartDateString, formatter).atStartOfDay()
                    .atZone(timeZone);
            ZonedDateTime currentDateTime = LocalDateTime.now().atZone(timeZone);
            if (list.size() < 12) {
                int monthsCount = list.size();
                int monthsFromStart = (int) ChronoUnit.MONTHS.between(availabilityStartDate.withDayOfMonth(1),
                        currentDateTime.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0));
                if (monthsFromStart > 12) {
                    monthsFromStart = 12;
                }
                if (monthsCount < monthsFromStart) {
                    int missingMonths = monthsFromStart - monthsCount;
                    for (int i = 0; i < missingMonths; i++) {
                        list.add(BigDecimal.ONE);
                    }
                }
            }
        } catch (DateTimeParseException e) {
            log.error("Error while adding missing months. Proceeding with available data", e);
        }
        return list;
    }

    /**
     * Converts a list of BigDecimal values to a list of Strings with a scale of 3,
     * rounding half up.
     *
     * @param list - the list of BigDecimal values
     * @return - the list of String representations of BigDecimal values with scale of 3
     */
    public static List<String> convertToStringListWithScale(List<BigDecimal> list) {

        return list.stream().map((BigDecimal decimal) -> decimal.setScale(3, RoundingMode.HALF_UP).toString())
                .collect(Collectors.toList());
    }

    /**
     * Converts a nested list of BigDecimal values to a nested list of Strings.
     *
     * @param nestedList - the nested list of BigDecimal values
     * @return - the nested list of String representations of BigDecimal values
     */
    public static List<List<String>> convertToNestedStringList(List<List<BigDecimal>> nestedList) {

        return nestedList.stream()
                .map(CommonUtil::convertToStringList)
                .collect(Collectors.toList());
    }

    /**
     * Convert BigDecimal list to String list.
     *
     * @param list - BigDecimal list
     * @return - String list
     */
    public static List<String> convertToStringList(List<BigDecimal> list) {

        return list.stream()
                .map(BigDecimal::toString)
                .collect(Collectors.toList());
    }

}

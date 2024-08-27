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

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.mockito.Mockito.doReturn;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

@PrepareForTest({OpenBankingCDSConfigParser.class})
@PowerMockIgnore({"javax.crypto.*", "jdk.internal.reflect.*"})
public class DateTimeUtilTest extends PowerMockTestCase {

    OpenBankingCDSConfigParser configParserMock;

    @BeforeClass
    void setUp() {

        configParserMock = PowerMockito.mock(OpenBankingCDSConfigParser.class);
        PowerMockito.mockStatic(OpenBankingCDSConfigParser.class);
        PowerMockito.when(OpenBankingCDSConfigParser.getInstance()).thenReturn(configParserMock);
        doReturn("GMT").when(configParserMock).getMetricsTimeZone();
    }

    @Test
    public void testGetDayDifference() {

        long startEpoch = 1609459200; // 2021-01-01 00:00:00 GMT
        long endEpoch = 1609545600; // 2021-01-02 00:00:00 GMT
        int days = DateTimeUtil.getDayDifference(startEpoch, endEpoch);
        assertEquals(1, days, "Day difference should be 1");
    }

    @Test
    public void testGetTimeRangeCurrent() {
        String[] timeRange = DateTimeUtil.getTimeRange(PeriodEnum.CURRENT);
        assertNotNull(timeRange, "Time range should not be null");
        assertEquals(2, timeRange.length, "Should return start and end times");
    }

    @Test
    public void testGetTimeRangeHistoric() {
        String[] timeRange = DateTimeUtil.getTimeRange(PeriodEnum.HISTORIC);
        assertNotNull(timeRange, "Time range should not be null");
        assertEquals(2, timeRange.length, "Should return start and end times");
    }

    @Test
    public void testGetAvailabilityMetricsTimeRangeCurrent() {
        String[] timeRange = DateTimeUtil.getAvailabilityMetricsTimeRange(PeriodEnum.CURRENT);
        assertNotNull(timeRange, "Time range should not be null");
        assertEquals(2, timeRange.length, "Should return start and end times for the current month");
    }

    @Test
    public void testGetAvailabilityMetricsTimeRangeHistoric() {
        String[] timeRange = DateTimeUtil.getAvailabilityMetricsTimeRange(PeriodEnum.HISTORIC);
        assertNotNull(timeRange, "Time range should not be null");
        assertEquals(2, timeRange.length, "Should return start and end times for the previous 12 months");
    }

    @Test
    public void testFormatZonedDateTime() {
        ZonedDateTime dateTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(1609459200), ZoneId.of("GMT"));
        String formatted = DateTimeUtil.formatZonedDateTime(dateTime);
        assertNotNull(formatted, "Formatted date should not be null");
    }

    @Test
    public void testGetEpochTimestamp() {
        String timestamp = "2021-01-01 00:00:00";
        long epoch = DateTimeUtil.getEpochTimestamp(timestamp);
        assertEquals(1609459200, epoch, "Epoch timestamp should match the expected value");
    }
}

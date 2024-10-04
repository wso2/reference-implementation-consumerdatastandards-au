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
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@PrepareForTest({OpenBankingCDSConfigParser.class})
@PowerMockIgnore({"javax.crypto.*", "jdk.internal.reflect.*"})
public class MetricsProcessorUtilTest extends PowerMockTestCase {

    private JSONObject metricsJsonObject;
    private int numberOfDays;
    private long metricsCountLastDateEpoch;
    private OpenBankingCDSConfigParser openBankingCDSConfigParserMock;

    @BeforeMethod
    public void setup() {
        openBankingCDSConfigParserMock = PowerMockito.mock(OpenBankingCDSConfigParser.class);
        PowerMockito.mockStatic(OpenBankingCDSConfigParser.class);
        PowerMockito.when(OpenBankingCDSConfigParser.getInstance()).thenReturn(openBankingCDSConfigParserMock);
        doReturn("GMT").when(openBankingCDSConfigParserMock).getMetricsTimeZone();
        doReturn("2024-05-01").when(openBankingCDSConfigParserMock).getMetricsV5StartDate();

        metricsJsonObject = Mockito.mock(JSONObject.class);
        numberOfDays = 7;
        metricsCountLastDateEpoch = 1715299199L;
    }

    @Test
    public void testDivideListsEqualSizeNoZeroDivisor() throws OpenBankingException {
        List<BigDecimal> list1 = Arrays.asList(BigDecimal.valueOf(10), BigDecimal.valueOf(20));
        List<BigDecimal> list2 = Arrays.asList(BigDecimal.valueOf(2), BigDecimal.valueOf(5));
        List<BigDecimal> expected = Arrays.asList(new BigDecimal("5.000"), new BigDecimal("4.000"));
        List<BigDecimal> result = MetricsProcessorUtil.divideLists(list1, list2, new BigDecimal("0"));
        Assert.assertEquals(result, expected);
    }

    @Test(expectedExceptions = OpenBankingException.class)
    public void testDivideListsUnequalSizes() throws OpenBankingException {
        List<BigDecimal> list1 = Arrays.asList(BigDecimal.valueOf(10), BigDecimal.valueOf(20));
        List<BigDecimal> list2 = Arrays.asList(BigDecimal.valueOf(2));
        MetricsProcessorUtil.divideLists(list1, list2, new BigDecimal("0"));
    }

    @Test
    public void testDivideListsWithZeroDivisor() throws OpenBankingException {
        List<BigDecimal> list1 = Arrays.asList(BigDecimal.valueOf(10), BigDecimal.valueOf(20));
        List<BigDecimal> list2 = Arrays.asList(BigDecimal.valueOf(2), BigDecimal.ZERO);
        List<BigDecimal> expected = Arrays.asList(new BigDecimal("5.000"), new BigDecimal("0"));
        List<BigDecimal> result = MetricsProcessorUtil.divideLists(list1, list2, new BigDecimal("0"));
        Assert.assertEquals(result, expected);
    }

    @Test
    public void testGetLastElementValueFromJsonObject() {
        JSONObject jsonObject = new JSONObject();
        JSONArray records = new JSONArray();
        JSONArray innerArray = new JSONArray();
        innerArray.add("5");
        records.add(innerArray);
        jsonObject.put("records", records);

        int result = MetricsProcessorUtil.getLastElementValueFromJsonObject(jsonObject);
        Assert.assertEquals(result, 5);
    }

    @Test
    public void testGetLastElementValueFromEmptyJsonObject() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("records", new JSONArray());

        int result = MetricsProcessorUtil.getLastElementValueFromJsonObject(jsonObject);
        Assert.assertEquals(result, 0);
    }

    @Test
    public void testGetTotalInvocationsForEachDay_BasicScenario() {
        Map<PriorityEnum, List<Integer>> invocationMetricsMap = new HashMap<>();
        invocationMetricsMap.put(PriorityEnum.HIGH_PRIORITY, Arrays.asList(10, 20));
        invocationMetricsMap.put(PriorityEnum.LOW_PRIORITY, Arrays.asList(30, 40));
        invocationMetricsMap.put(PriorityEnum.UNATTENDED, Arrays.asList(5, 10));
        invocationMetricsMap.put(PriorityEnum.UNAUTHENTICATED, Arrays.asList(5, 10));
        invocationMetricsMap.put(PriorityEnum.LARGE_PAYLOAD, Arrays.asList(5, 10));

        List<Integer> result = MetricsProcessorUtil
                .getTotalInvocationsForEachDay(invocationMetricsMap, PriorityEnum.values());
        List<Integer> expected = Arrays.asList(55, 90);

        Assert.assertEquals(result, expected);
    }

    @Test
    public void testInitializeList() {
        int numberOfDays = 7;
        ArrayList<Integer> result = MetricsProcessorUtil.initializeList(numberOfDays, 0);
        Assert.assertEquals(result.size(), numberOfDays, "The list size should be exactly " + numberOfDays);
        for (Integer value : result) {
            Assert.assertEquals(value, Integer.valueOf(0), "Each entry in the list should be Zero.");
        }
    }

    @Test
    public void testGetPopulatedInvocationByPriorityMetricsMap() {

        JSONArray records = new JSONArray();
        records.add(new JSONArray().appendElement("Unattended").appendElement(13).appendElement(1715273999000L));
        records.add(new JSONArray().appendElement("LowPriority").appendElement(12).appendElement(1715187599000L));
        records.add(new JSONArray().appendElement("HighPriority").appendElement(13).appendElement(1715101199000L));

        when(metricsJsonObject.get("records")).thenReturn(records);

        Map<PriorityEnum, List<Integer>> result = MetricsProcessorUtil.getPopulatedInvocationByPriorityMetricsMap(
                metricsJsonObject, numberOfDays, metricsCountLastDateEpoch);

        // Assert the size of the map
        Assert.assertEquals(result.size(), PriorityEnum.values().length);

        // Assert the size of the lists in the map
        for (PriorityEnum priority : PriorityEnum.values()) {
            Assert.assertEquals(result.get(priority).size(), numberOfDays);
        }

        // Assert specific values based on the provided JSON
        Assert.assertEquals(result.get(PriorityEnum.UNATTENDED).get(0), Integer.valueOf(13));
        Assert.assertEquals(result.get(PriorityEnum.LOW_PRIORITY).get(1), Integer.valueOf(12));
        Assert.assertEquals(result.get(PriorityEnum.HIGH_PRIORITY).get(2), Integer.valueOf(13));
    }

    @Test
    public void testGetPopulatedTotalResponseTimeMetricsMap() {

        JSONArray records = new JSONArray();
        records.add(new JSONArray().appendElement("Unattended").appendElement(112.0).appendElement(1715273999000L));
        records.add(new JSONArray().appendElement("LowPriority").appendElement(234.0).appendElement(1715187599000L));
        records.add(new JSONArray().appendElement("HighPriority").appendElement(177.0).appendElement(1715101199000L));

        when(metricsJsonObject.get("records")).thenReturn(records);

        Map<PriorityEnum, List<BigDecimal>> result = MetricsProcessorUtil.getPopulatedTotalResponseTimeMetricsMap(
                metricsJsonObject, numberOfDays, metricsCountLastDateEpoch);

        // Assert the size of the map
        Assert.assertEquals(result.size(), PriorityEnum.values().length);

        // Assert the size of the lists in the map
        for (PriorityEnum priority : PriorityEnum.values()) {
            Assert.assertEquals(result.get(priority).size(), numberOfDays);
        }

        // Assert specific values based on the provided JSON
        Assert.assertEquals(result.get(PriorityEnum.UNATTENDED).get(0), BigDecimal.valueOf(112.0));
        Assert.assertEquals(result.get(PriorityEnum.LOW_PRIORITY).get(1), BigDecimal.valueOf(234.0));
        Assert.assertEquals(result.get(PriorityEnum.HIGH_PRIORITY).get(2), BigDecimal.valueOf(177.0));
    }

    @Test
    public void testInitializePriorityMap() {
        int numberOfDays = 5;
        Map<PriorityEnum, List<BigDecimal>> result = MetricsProcessorUtil
                .initializePriorityMap(numberOfDays, BigDecimal.ZERO);

        // Assert the size of the map
        Assert.assertEquals(result.size(), PriorityEnum.values().length);

        // Assert the size of the lists in the map
        for (PriorityEnum priority : PriorityEnum.values()) {
            Assert.assertEquals(result.get(priority).size(), numberOfDays);
        }

        // Assert that all values in the lists are BigDecimal.ZERO
        for (PriorityEnum priority : PriorityEnum.values()) {
            for (BigDecimal value : result.get(priority)) {
                Assert.assertEquals(value, BigDecimal.ZERO);
            }
        }
    }

    @Test
    public void testGetPopulatedMetricsList() {

        JSONArray records = new JSONArray();
        records.add(new JSONArray().appendElement(13).appendElement(1715273999000L));
        records.add(new JSONArray().appendElement(12).appendElement(1715187599000L));
        records.add(new JSONArray().appendElement(13).appendElement(1715101199000L));

        when(metricsJsonObject.get("records")).thenReturn(records);

        List<Integer> result = MetricsProcessorUtil.getPopulatedMetricsList(metricsJsonObject, numberOfDays,
                metricsCountLastDateEpoch);

        // Assert the size of the list
        Assert.assertEquals(result.size(), numberOfDays);

        // Assert specific values based on the provided JSON
        Assert.assertEquals(result.get(0), Integer.valueOf(13));
        Assert.assertEquals(result.get(1), Integer.valueOf(12));
        Assert.assertEquals(result.get(2), Integer.valueOf(13));
    }

    @Test
    public void testGetListFromRejectionsJson() {
        JSONArray records = new JSONArray();
        records.add(new JSONArray().appendElement(13).appendElement(1715273999L).appendElement("authenticated"));
        records.add(new JSONArray().appendElement(12).appendElement(1715187599L).appendElement(null));

        when(metricsJsonObject.get("records")).thenReturn(records);

        List<ArrayList<Integer>> result = MetricsProcessorUtil.getListFromRejectionsJson(metricsJsonObject,
                numberOfDays, metricsCountLastDateEpoch);

        // Assert the size of the list
        Assert.assertEquals(result.size(), 2);

        // Assert specific values based on the provided JSON
        Assert.assertEquals(result.get(0).get(0), Integer.valueOf(13));
        Assert.assertEquals(result.get(1).get(1), Integer.valueOf(12));
    }

    @Test
    public void testGetPeakTPSMapFromJsonArray() {
        JSONObject record1 = new JSONObject();
        record1.put("event", new JSONObject().appendField("total_count", 1)
                .appendField("aspect", "authenticated")
                .appendField("TIMESTAMP", 1715273999));
        JSONObject record2 = new JSONObject();
        record2.put("event", new JSONObject().appendField("total_count", 1)
                .appendField("aspect", "unauthenticated")
                .appendField("TIMESTAMP", 1715101199));

        JSONArray tpsMetricsJsonArray = new JSONArray();
        tpsMetricsJsonArray.add(record1);
        tpsMetricsJsonArray.add(record2);

        Map<AspectEnum, List<BigDecimal>> result = MetricsProcessorUtil.getPeakTPSMapFromJsonArray(tpsMetricsJsonArray,
                numberOfDays, metricsCountLastDateEpoch);

        // Assert the size of the map
        Assert.assertEquals(result.size(), AspectEnum.values().length);

        // Assert the size of the lists in the map
        for (AspectEnum aspect : AspectEnum.values()) {
            Assert.assertEquals(result.get(aspect).size(), numberOfDays);
        }

        // Assert specific values based on the provided JSON
        Assert.assertEquals(result.get(AspectEnum.AUTHENTICATED).get(0), BigDecimal.valueOf(1));
        Assert.assertEquals(result.get(AspectEnum.UNAUTHENTICATED).get(2), BigDecimal.valueOf(1));
    }


}

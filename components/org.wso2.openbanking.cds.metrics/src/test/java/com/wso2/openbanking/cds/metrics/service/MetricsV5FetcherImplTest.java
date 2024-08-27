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
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;
import org.wso2.openbanking.cds.metrics.data.MockMetricsDataProvider;
import org.wso2.openbanking.cds.metrics.model.MetricsResponseModel;
import org.wso2.openbanking.cds.metrics.util.PeriodEnum;

import java.time.ZoneId;

import static org.mockito.Mockito.doReturn;

@PrepareForTest({OpenBankingCDSConfigParser.class})
@PowerMockIgnore({"javax.crypto.*", "jdk.internal.reflect.*"})
public class MetricsV5FetcherImplTest extends PowerMockTestCase {

    private MetricsV5FetcherImpl metricsFetcher;
    private MetricsProcessor metricsProcessor;
    private OpenBankingCDSConfigParser openBankingCDSConfigParserMock;

    @BeforeClass
    public void init() throws OpenBankingException {

        metricsProcessor = new MetricsV5ProcessorImpl(PeriodEnum.ALL, new MockMetricsDataProvider(),
                ZoneId.of("GMT"));
        metricsFetcher = new MetricsV5FetcherImpl(metricsProcessor);

    }

    @BeforeMethod
    public void setup() throws OpenBankingException {

        openBankingCDSConfigParserMock = PowerMockito.mock(OpenBankingCDSConfigParser.class);
        PowerMockito.mockStatic(OpenBankingCDSConfigParser.class);
        PowerMockito.when(OpenBankingCDSConfigParser.getInstance()).thenReturn(openBankingCDSConfigParserMock);
        doReturn("GMT").when(openBankingCDSConfigParserMock).getMetricsTimeZone();
        doReturn("2024-05-01").when(openBankingCDSConfigParserMock).getMetricsV5StartDate();
        doReturn(300000L).when(openBankingCDSConfigParserMock).getConsentAbandonmentTime();
        doReturn(300000L).when(openBankingCDSConfigParserMock).getAuthorizationCodeValidityPeriod();
    }

    @Test
    public void testGetResponseMetricsListModel() throws Exception {

        MetricsResponseModel metricsResponseModel = metricsFetcher.getResponseMetricsListModel(
                "2024-05-30T01:56:28+05:30");
        Assert.assertNotNull(metricsResponseModel, "Metrics response model should not be null");
    }
}

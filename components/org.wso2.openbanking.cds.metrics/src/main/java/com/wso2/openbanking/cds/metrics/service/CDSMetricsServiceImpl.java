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

import com.google.gson.Gson;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;
import org.wso2.openbanking.cds.metrics.cache.MetricsCache;
import org.wso2.openbanking.cds.metrics.constants.MetricsConstants;
import org.wso2.openbanking.cds.metrics.data.MetricsDataProvider;
import org.wso2.openbanking.cds.metrics.data.MetricsV5DataProvider;
import org.wso2.openbanking.cds.metrics.model.MetricsResponseModel;
import org.wso2.openbanking.cds.metrics.util.MetricsServiceUtil;
import org.wso2.openbanking.cds.metrics.util.PeriodEnum;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;

/**
 * Implementation of CDS Admin metrics service.
 */
public class CDSMetricsServiceImpl implements CDSMetricsService {

    private static final Log log = LogFactory.getLog(CDSMetricsServiceImpl.class);
    private static final ZoneId TIME_ZONE = ZoneId.of(OpenBankingCDSConfigParser.getInstance().getMetricsTimeZone());

    /**
     * {@inheritDoc}
     */
    @Override
    public MetricsResponseModel getMetrics(String xV, PeriodEnum period) throws OpenBankingException {

        MetricsResponseModel metricsResponseModel;
        String requestTime = new SimpleDateFormat(MetricsConstants.REQUEST_TIMESTAMP_PATTERN).format(new Date());

        switch (period) {
            case CURRENT:
                metricsResponseModel = getCurrentDayMetrics(requestTime);
                break;
            case HISTORIC:
                metricsResponseModel = getHistoricMetrics(requestTime);
                break;
            default:
                metricsResponseModel = getAllMetrics(requestTime);
        }
        return metricsResponseModel;
    }

    /**
     * Get current day metrics.
     *
     * @param requestTime Request time
     * @return ResponseMetricsListModel
     * @throws OpenBankingException OpenBankingException
     */
    public MetricsResponseModel getCurrentDayMetrics(String requestTime) throws OpenBankingException {

        MetricsQueryCreator metricsV5QueryCreator = new MetricsV5QueryCreatorImpl(PeriodEnum.CURRENT);
        MetricsDataProvider metricsV5DataProvider = new MetricsV5DataProvider(metricsV5QueryCreator);
        MetricsProcessor metricsV5Processor = new MetricsV5ProcessorImpl(PeriodEnum.CURRENT,
                metricsV5DataProvider, TIME_ZONE);
        MetricsFetcher metricsV5FetcherCurrent = new MetricsV5FetcherImpl(metricsV5Processor);
        return metricsV5FetcherCurrent.getResponseMetricsListModel(requestTime);
    }

    /**
     * Get historic metrics.
     * Uses cache if available, otherwise fetches from analytics server.
     *
     * @param requestTime Request time
     * @return ResponseMetricsListModel
     * @throws OpenBankingException OpenBankingException
     */
    public MetricsResponseModel getHistoricMetrics(String requestTime) throws OpenBankingException {

        MetricsResponseModel metricsResponseModelHistoric;
        metricsResponseModelHistoric = getCachedHistoricMetrics();
        if (metricsResponseModelHistoric == null) {
            log.debug("Getting historic metrics from analytics server since cached model is not found.");
            metricsResponseModelHistoric = getRealtimeHistoricMetrics(requestTime);
            log.debug("Historic metrics retrieval completed.");
        } else {
            metricsResponseModelHistoric.setRequestTime(requestTime);
        }
        return metricsResponseModelHistoric;
    }

    /**
     * Get historic metrics from analytics server.
     *
     * @param requestTime Request time
     * @return ResponseMetricsListModel
     * @throws OpenBankingException OpenBankingException
     */
    private MetricsResponseModel getRealtimeHistoricMetrics(String requestTime) throws OpenBankingException {

        MetricsQueryCreator metricsV5QueryCreator = new MetricsV5QueryCreatorImpl(PeriodEnum.HISTORIC);
        MetricsDataProvider metricsV5DataProvider = new MetricsV5DataProvider(metricsV5QueryCreator);
        MetricsProcessor metricsV5Processor = new MetricsV5ProcessorImpl(
                PeriodEnum.HISTORIC, metricsV5DataProvider, TIME_ZONE);
        MetricsFetcher metricsV5FetcherHistoric = new MetricsV5FetcherImpl(metricsV5Processor);
        return metricsV5FetcherHistoric.getResponseMetricsListModel(requestTime);
    }

    /**
     * Get historic metrics from cache.
     *
     * @return ResponseMetricsListModel, or null if cache is not available or model is expired.
     */
    private MetricsResponseModel getCachedHistoricMetrics() {

        MetricsCache metricsCache = MetricsCache.getInstance();
        Object cachedResponseMetricsJson = metricsCache.getFromCache(MetricsCache.getHistoricMetricsCacheKey());
        if (cachedResponseMetricsJson != null) {
            log.debug("Historic metrics model found in cache.");
            MetricsResponseModel metricsResponseModel = new Gson().fromJson(
                    (String) cachedResponseMetricsJson, MetricsResponseModel.class);
            return MetricsServiceUtil.isResponseModelExpired(metricsResponseModel) ? null : metricsResponseModel;
        }
        log.debug("Historic metrics model not found in cache.");
        return null;
    }

    /**
     * Get all metrics.
     * Uses cache for historic metrics if available, otherwise fetches all metrics from analytics server.
     *
     * @param requestTime Request time
     * @return ResponseMetricsListModel
     * @throws OpenBankingException OpenBankingException
     */
    public MetricsResponseModel getAllMetrics(String requestTime) throws OpenBankingException {

        MetricsResponseModel metricsResponseModelHistoric = getCachedHistoricMetrics();
        MetricsResponseModel metricsResponseModel;

        if (metricsResponseModelHistoric != null) {
            metricsResponseModel = getCurrentDayMetrics(requestTime);
            MetricsServiceUtil.appendHistoricMetricsToCurrentDayMetrics(metricsResponseModel,
                    metricsResponseModelHistoric);
        } else {
            log.debug("Getting all metrics from analytics server since cached model is not found.");
            MetricsQueryCreator metricsV5QueryCreator = new MetricsV5QueryCreatorImpl(PeriodEnum.ALL);
            MetricsDataProvider metricsV5DataProvider = new MetricsV5DataProvider(metricsV5QueryCreator);
            MetricsProcessor metricsV5Processor = new MetricsV5ProcessorImpl(
                    PeriodEnum.ALL, metricsV5DataProvider, TIME_ZONE);
            MetricsFetcher metricsV5FetcherCurrent = new MetricsV5FetcherImpl(metricsV5Processor);
            metricsResponseModel = metricsV5FetcherCurrent.getResponseMetricsListModel(requestTime);
            log.debug("All metrics retrieval completed.");
        }
        return metricsResponseModel;
    }
}

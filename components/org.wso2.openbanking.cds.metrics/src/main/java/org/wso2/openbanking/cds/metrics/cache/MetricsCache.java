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

package org.wso2.openbanking.cds.metrics.cache;

import com.wso2.openbanking.accelerator.common.caching.OpenBankingBaseCache;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;

/**
 * Cache definition to store Metrics aggregated data.
 */
public class MetricsCache extends OpenBankingBaseCache<MetricsCacheKey, Object> {

    private static final String CACHE_NAME = "CDS_METRICS_CACHE";
    private static volatile MetricsCache instance;
    private final Integer accessExpiryMinutes;
    private final Integer modifiedExpiryMinutes;
    private static final MetricsCacheKey historicMetricsCacheKey = new MetricsCacheKey("HistoricMetricsData");

    /**
     * Initialize with unique cache name.
     */
    private MetricsCache() {

        super(CACHE_NAME);

        accessExpiryMinutes = OpenBankingCDSConfigParser.getInstance().getMetricCacheExpiryInMinutes();
        modifiedExpiryMinutes = OpenBankingCDSConfigParser.getInstance().getMetricCacheExpiryInMinutes();
    }

    public static MetricsCache getInstance() {

        if (instance == null) {
            synchronized (MetricsCache.class) {
                if (instance == null) {
                    instance = new MetricsCache();
                }
            }
        }
        return instance;
    }

    @Override
    public int getCacheAccessExpiryMinutes() {
        return accessExpiryMinutes;
    }

    @Override
    public int getCacheModifiedExpiryMinutes() {
        return modifiedExpiryMinutes;
    }

    public static MetricsCacheKey getHistoricMetricsCacheKey() {
        return historicMetricsCacheKey;
    }
}

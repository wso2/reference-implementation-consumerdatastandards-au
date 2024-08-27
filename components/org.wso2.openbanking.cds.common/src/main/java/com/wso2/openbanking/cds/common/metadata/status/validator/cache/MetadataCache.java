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

package org.wso2.openbanking.cds.common.metadata.status.validator.cache;

import com.wso2.openbanking.accelerator.common.caching.OpenBankingBaseCache;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;

import java.util.Map;

/**
 * Cache definition to store ADR and SP statuses as String - String key value pair Maps.
 */
public class MetadataCache extends OpenBankingBaseCache<MetadataCacheKey, Map<String, String>> {

    private static final String CACHE_NAME = "CDS_METADATA_RESPONSE";
    private static volatile MetadataCache instance;
    private final Integer accessExpiryMinutes;
    private final Integer modifiedExpiryMinutes;

    /**
     * Initialize with unique cache name.
     */
    private MetadataCache() {

        super(CACHE_NAME);

        accessExpiryMinutes = OpenBankingCDSConfigParser.getInstance().getCacheExpiryInMinutes();
        modifiedExpiryMinutes = OpenBankingCDSConfigParser.getInstance().getCacheExpiryInMinutes();
    }

    public static MetadataCache getInstance() {

        if (instance == null) {
            synchronized (MetadataCache.class) {
                if (instance == null) {
                    instance = new MetadataCache();
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
}

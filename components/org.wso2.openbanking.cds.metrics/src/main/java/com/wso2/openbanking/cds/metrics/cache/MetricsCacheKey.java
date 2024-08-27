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

import com.wso2.openbanking.accelerator.common.caching.OpenBankingBaseCacheKey;

import java.io.Serializable;

/**
 * Metrics cache key implementation.
 */
public class MetricsCacheKey extends OpenBankingBaseCacheKey implements Serializable {
    private static final long serialVersionUID = -8083228768863423682L;

    /**
     * public constructor for OpenBankingBaseCacheKey.
     *
     * @param cacheKey String cache key.
     */
    public MetricsCacheKey(String cacheKey) {

        super(cacheKey);
    }
}

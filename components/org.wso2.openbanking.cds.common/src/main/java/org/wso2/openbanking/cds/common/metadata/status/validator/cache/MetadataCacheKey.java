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

import com.wso2.openbanking.accelerator.common.caching.OpenBankingBaseCacheKey;

import java.io.Serializable;
import java.util.Objects;

/**
 * The definition of Cache Key to create Metadata Cache.
 */
public class MetadataCacheKey extends OpenBankingBaseCacheKey implements Serializable {

    static final long serialVersionUID = 1382340305L;
    private String status;

    public static MetadataCacheKey from(String statusType) {

        MetadataCacheKey metadataCacheKey = new MetadataCacheKey();
        metadataCacheKey.setStatus(statusType); // Metadata element is either DR map or SP map.
        return metadataCacheKey;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MetadataCacheKey that = (MetadataCacheKey) o;
        return Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status);
    }
}

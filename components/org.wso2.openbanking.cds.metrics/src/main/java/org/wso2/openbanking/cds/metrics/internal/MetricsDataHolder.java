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

package org.wso2.openbanking.cds.metrics.internal;

import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * Metrics Data Holder class
 */
public class MetricsDataHolder {
    private static final MetricsDataHolder instance = new MetricsDataHolder();
    private RealmService realmService;
    private APIManagerConfigurationService apiManagerConfigurationService;

    private MetricsDataHolder() {

    }

    public static MetricsDataHolder getInstance() {

        return instance;
    }

    public RealmService getRealmService() {

        if (realmService == null) {
            throw new RuntimeException("Realm Service is not available. Component did not start correctly.");
        }
        return realmService;
    }

    void setRealmService(RealmService realmService) {

        this.realmService = realmService;
    }

    /**
     * Return APIM configuration service.
     *
     * @return APIManagerConfigurationService
     */
    public APIManagerConfigurationService getApiManagerConfigurationService() {
        return apiManagerConfigurationService;
    }

    /**
     * Set APIM configuration service.
     */
    public void setApiManagerConfigurationService(APIManagerConfigurationService apiManagerConfigurationService) {
        this.apiManagerConfigurationService = apiManagerConfigurationService;
    }

}

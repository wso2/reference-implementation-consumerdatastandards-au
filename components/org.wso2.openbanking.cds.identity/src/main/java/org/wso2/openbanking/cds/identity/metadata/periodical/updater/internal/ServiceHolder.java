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

package org.wso2.openbanking.cds.identity.metadata.periodical.updater.internal;

import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonHelper;

/**
 * Simple holder to store Services and Service Stubs.
 */
public class ServiceHolder {
    private static volatile ServiceHolder instance;

    private ConsentCoreService consentCoreService;
    private IdentityCommonHelper identityCommonHelper;

    private ServiceHolder() {
    }

    public static ServiceHolder getInstance() {

        if (instance == null) {
            synchronized (ServiceHolder.class) {
                if (instance == null) {
                    instance = new ServiceHolder();
                }
            }
        }
        return instance;
    }

    public ConsentCoreService getConsentCoreService() {

        if (consentCoreService == null) {
            consentCoreService = new ConsentCoreServiceImpl();
        }
        return consentCoreService;
    }

    public IdentityCommonHelper getIdentityCommonHelper() {
        if (identityCommonHelper == null) {
            this.identityCommonHelper = new IdentityCommonHelper();
        }
        return identityCommonHelper;
    }
}

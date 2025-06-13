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
package org.wso2.openbanking.cds.identity.internal;

import org.wso2.carbon.identity.oauth2.client.authentication.OAuthClientAuthnService;
import org.wso2.carbon.user.core.service.RealmService;


/**
 * Data Holder for Open Banking Common.
 */
public class CDSIdentityDataHolder {

    private static volatile CDSIdentityDataHolder instance = new CDSIdentityDataHolder();
    private RealmService realmService;
    private OAuthClientAuthnService oAuthClientAuthnService;

    private CDSIdentityDataHolder() {

    }

    public static CDSIdentityDataHolder getInstance() {

        if (instance == null) {
            synchronized (CDSIdentityDataHolder.class) {
                if (instance == null) {
                    instance = new CDSIdentityDataHolder();
                }
            }
        }
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
     * Return OAuthClientAuthnService.
     *
     * @return OAuthClientAuthnService
     */
    public OAuthClientAuthnService getOAuthClientAuthnService() {
        return oAuthClientAuthnService;
    }

    /**
     * Set OAuthClientAuthnService.
     */
    public void setOAuthClientAuthnService(OAuthClientAuthnService oAuthClientAuthnService) {
        this.oAuthClientAuthnService = oAuthClientAuthnService;
    }
}

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

package org.wso2.openbanking.cds.common.data.publisher;

import java.util.Map;

/**
 * Data Publishing Service for CDS.
 */
public interface CDSDataPublishingService {

    /**
     * Method to get the CDSDataPublishingService instance.
     *
     * @return An instance of CDSDataPublishingService
     */
    static CDSDataPublishingService getCDSDataPublishingService() {

        return CDSDataPublishingServiceImpl.getInstance();
    }

    /**
     * Method to publish API Invocation related data.
     *
     * @param apiInvocationData Map containing the data that needs to be published
     */
    void publishApiInvocationData(Map<String, Object> apiInvocationData);

    /**
     * Method to publish Access Token related data.
     *
     * @param accessTokenData Map containing the data that needs to be published
     */
    void publishUserAccessTokenData(Map<String, Object> accessTokenData);

    /**
     * Method to publish Consent related data.
     *
     * @param consentData Map containing the data that needs to be published
     */
    void publishConsentData(Map<String, Object> consentData);

    /**
     * Method to publish API latency related data.
     *
     * @param apiLatencyData Map containing the data that needs to be published
     */
    void publishApiLatencyData(Map<String, Object> apiLatencyData);

    /**
     * Method to publish Authorisation data.
     *
     * @param authorisationData Map containing the data that needs to be published
     */
    void publishAuthorisationData(Map<String, Object> authorisationData);

    /**
     * Method to publish Abandoned Consent Flow data.
     *
     * @param abandonedConsentFlowData Map containing the data that needs to be published
     */
    void publishAbandonedConsentFlowData(Map<String, Object> abandonedConsentFlowData);

}

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

package org.wso2.openbanking.cds.gateway.throttling;

import com.wso2.openbanking.accelerator.gateway.throttling.ThrottleDataPublisher;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.common.gateway.dto.RequestContextDTO;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;
import org.wso2.openbanking.cds.common.utils.CDSCommonUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * CDS Throttle data publisher to add custom throttling properties based on request headers.
 */
public class CDSThrottleDataPublisherImpl implements ThrottleDataPublisher {

    private static final Log LOG = LogFactory.getLog(CDSThrottleDataPublisherImpl.class);

    private static final String X_FAPI_CUSTOMER_IP_ADDRESS = "x-fapi-customer-ip-address";
    private static final String CUSTOMER_STATUS = "customerStatus";
    private static final String CUSTOMER_PRESENT_STATUS = "customerPresent";
    private static final String UNATTENDED_STATUS = "unattended";
    private static final String AUTHORIZATION_STATUS = "authorizationStatus";
    private static final String SECURED_STATUS = "secured";
    private static final String PUBLIC_STATUS = "public";
    private static final String NULL_STRING = "null";
    private static final String AUTHORIZATION = "Authorization";
    private static final String AUTHORIZATION_HEADER = "authorizationHeader";

    @Override
    public Map<String, Object> getCustomProperties(RequestContextDTO requestContextDTO) {

        Map<String, Object> customPropertyMap = new HashMap<>();
        Object xFapiCustomerIpAddress = requestContextDTO.getMsgInfo().getHeaders().get(X_FAPI_CUSTOMER_IP_ADDRESS);
        String authorizationHeader = requestContextDTO.getMsgInfo().getHeaders().get(AUTHORIZATION);
        String accessToken = (authorizationHeader != null && authorizationHeader.split(" ").length > 1) ?
                authorizationHeader.split(" ")[1] : null;

        // Encrypt access token
        if (accessToken != null && OpenBankingCDSConfigParser.getInstance().isTokenEncryptionEnabled()) {
            accessToken = CDSCommonUtils.encryptAccessToken(accessToken);
        }

        //Adding x-fapi-customer-ip-address header as a custom property
        if (xFapiCustomerIpAddress != null) {
            LOG.debug("Adding x-fapi-customer-ip-address details to the custom property map");
            customPropertyMap.put(X_FAPI_CUSTOMER_IP_ADDRESS, xFapiCustomerIpAddress);
            customPropertyMap.put(CUSTOMER_STATUS, CUSTOMER_PRESENT_STATUS);
        } else {
            LOG.debug("x-fapi-customer-ip-address header was not found in the request");
            customPropertyMap.put(X_FAPI_CUSTOMER_IP_ADDRESS, NULL_STRING);
            customPropertyMap.put(CUSTOMER_STATUS, UNATTENDED_STATUS);
        }

        //Adding authorization header as a custom property
        if (authorizationHeader != null && accessToken != null) {
            LOG.debug("Adding authorization header details to the custom property map");
            customPropertyMap.put(AUTHORIZATION_HEADER, accessToken);
            customPropertyMap.put(AUTHORIZATION_STATUS, SECURED_STATUS);
        } else {
            LOG.debug("Authorization header was not found in the request");
            customPropertyMap.put(AUTHORIZATION_HEADER, NULL_STRING);
            customPropertyMap.put(AUTHORIZATION_STATUS, PUBLIC_STATUS);
        }
        return customPropertyMap;
    }
}

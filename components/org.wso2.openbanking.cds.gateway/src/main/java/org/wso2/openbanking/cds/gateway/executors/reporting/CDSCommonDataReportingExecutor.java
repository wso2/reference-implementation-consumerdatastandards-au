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

package org.wso2.openbanking.cds.gateway.executors.reporting;

import com.wso2.openbanking.accelerator.gateway.executor.core.OpenBankingGatewayExecutor;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;
import org.wso2.openbanking.cds.common.utils.CDSCommonUtils;
import org.wso2.openbanking.cds.gateway.utils.GatewayConstants;

import java.util.Map;

/**
 * CDS common data reporting executor to populate additional data publishing elements.
 */
public class CDSCommonDataReportingExecutor implements OpenBankingGatewayExecutor {

    @Override
    public void preProcessRequest(OBAPIRequestContext obapiRequestContext) {

        // Add customer status to the analytics data by the presence of x-fapi-customer-ip-address header.
        // Customer status is undefined for infosec endpoints.
        Map<String, String> headers = obapiRequestContext.getMsgInfo().getHeaders();
        Object xFapiCustomerIpAddress = headers.get(GatewayConstants.X_FAPI_CUSTOMER_IP_ADDRESS);
        String electedResource = obapiRequestContext.getMsgInfo().getElectedResource();
        String restApiContext = obapiRequestContext.getApiRequestInfo().getContext();
        String customerStatus;
        if (GatewayConstants.INFOSEC_ENDPOINTS.contains(restApiContext) ||
                GatewayConstants.INFOSEC_ENDPOINTS.contains(electedResource)) {
            customerStatus = GatewayConstants.UNDEFINED;
        } else if (xFapiCustomerIpAddress == null) {
            customerStatus = GatewayConstants.UNATTENDED;
        } else {
            customerStatus = GatewayConstants.CUSTOMER_PRESENT;
        }

        // Add data publishing elements
        Map<String, Object> analyticsData = obapiRequestContext.getAnalyticsData();
        analyticsData.put(GatewayConstants.CUSTOMER_STATUS, customerStatus);
        obapiRequestContext.setAnalyticsData(analyticsData);

    }

    @Override
    public void postProcessRequest(OBAPIRequestContext obapiRequestContext) {

        // Add access token to the analytics data.
        Map<String, String> headers = obapiRequestContext.getMsgInfo().getHeaders();
        String authorizationHeader = headers.get(GatewayConstants.AUTHORIZATION);
        String accessToken = (authorizationHeader != null && authorizationHeader.split(" ").length > 1) ?
                authorizationHeader.split(" ")[1] : null;

        // Encrypt access token if configured.
        if (accessToken != null && OpenBankingCDSConfigParser.getInstance().isTokenEncryptionEnabled()) {
            accessToken = CDSCommonUtils.encryptAccessToken(accessToken);
        }
        // Add data publishing elements
        Map<String, Object> analyticsData = obapiRequestContext.getAnalyticsData();
        analyticsData.put(GatewayConstants.ACCESS_TOKEN_ID, accessToken);
        obapiRequestContext.setAnalyticsData(analyticsData);
    }

    @Override
    public void preProcessResponse(OBAPIResponseContext obapiResponseContext) {

    }

    @Override
    public void postProcessResponse(OBAPIResponseContext obapiResponseContext) {

    }
}

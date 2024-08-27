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

package org.wso2.openbanking.cds.consent.extensions.authorize.worker;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.data.publisher.common.constants.DataPublishingConstants;
import com.wso2.openbanking.accelerator.identity.auth.extensions.adaptive.function.OpenBankingAuthenticationWorker;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsAuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsServletRequest;
import org.wso2.openbanking.cds.common.data.publisher.CDSDataPublishingService;
import org.wso2.openbanking.cds.common.enums.AuthorisationStageEnum;
import org.wso2.openbanking.cds.common.utils.CDSCommonUtils;
import org.wso2.openbanking.cds.common.utils.CommonConstants;

import java.util.Map;

/**
 * Authentication Worker implementation to publish abandoned consent flow data from the common auth script.
 */
public class AbandonedConsentFlowDataPublisherWorker implements OpenBankingAuthenticationWorker {

    private static final Log log = LogFactory.getLog(AbandonedConsentFlowDataPublisherWorker.class);

    @Override
    public JSONObject handleRequest(JsAuthenticationContext context, Map<String, String> map) {

        if (Boolean.parseBoolean((String) OpenBankingConfigParser.getInstance().getConfiguration()
                .get(DataPublishingConstants.DATA_PUBLISHING_ENABLED))) {

            String requestUri = getRequestUri(context);
            String requestUriKey = CDSCommonUtils.getRequestUriKey(requestUri);

            if (StringUtils.isBlank(requestUriKey)) {
                log.error("Request URI key not found.");
                return new JSONObject();
            }

            Map<String, Object> abandonedConsentFlowDataMap = CDSCommonUtils
                    .generateAbandonedConsentFlowDataMap(requestUriKey, null,
                            AuthorisationStageEnum.fromValue(map.get(CommonConstants.STAGE)));

            CDSDataPublishingService.getCDSDataPublishingService()
                    .publishAbandonedConsentFlowData(abandonedConsentFlowDataMap);
        }

        return new JSONObject();
    }

    private String getRequestUri(JsAuthenticationContext context) {

        JsServletRequest jsServletRequest = (JsServletRequest) context.getMember(CommonConstants.REQUEST);
        String requestUri = jsServletRequest.getWrapped().getWrapped().getParameter(CommonConstants.REQUEST_URI);

        if (StringUtils.isNotBlank(requestUri)) {
            // Setting the retrieved request URI to the context since it is not available after the first
            // retrieval from the request
            context.getWrapped().setProperty(CommonConstants.REQUEST_URI, requestUri);
            return requestUri;
        }

        return (String) context.getWrapped().getProperty(CommonConstants.REQUEST_URI);
    }
}

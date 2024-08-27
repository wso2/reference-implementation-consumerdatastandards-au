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

package org.wso2.openbanking.cds.identity.grant.type.handlers;

import com.wso2.openbanking.accelerator.identity.grant.type.handlers.OBRefreshGrantHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.openbanking.cds.common.data.publisher.CDSDataPublishingService;
import org.wso2.openbanking.cds.identity.grant.type.handlers.utils.CDSGrantHandlerUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * CDS specific refresh grant handler.
 */
public class CDSRefreshGrantHandler extends OBRefreshGrantHandler {

    private static Log log = LogFactory.getLog(CDSRefreshGrantHandler.class);
    private CDSDataPublishingService dataPublishingService = CDSDataPublishingService.getCDSDataPublishingService();

    /**
     * Add cdr_arrangement_id.
     *
     * @param oAuth2AccessTokenRespDTO
     * @param tokReqMsgCtx
     */
    @Override
    public void executeInitialStep(OAuth2AccessTokenRespDTO oAuth2AccessTokenRespDTO,
                                   OAuthTokenReqMessageContext tokReqMsgCtx) throws IdentityOAuth2Exception {

        CDSGrantHandlerUtil.populateCDRArrangementID(oAuth2AccessTokenRespDTO, tokReqMsgCtx.getScope());
    }

    /**
     * Publish refresh token related data.
     *
     * @param oAuth2AccessTokenRespDTO
     */
    @Override
    public void publishUserAccessTokenData(OAuth2AccessTokenRespDTO oAuth2AccessTokenRespDTO)
            throws IdentityOAuth2Exception {

        log.debug("Publishing user access token data for metrics.");
        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("accessTokenID", CDSGrantHandlerUtil.retrieveAccessToken(oAuth2AccessTokenRespDTO));
        dataPublishingService.publishUserAccessTokenData(tokenData);
    }

}

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

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentAttributes;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.accelerator.identity.grant.type.handlers.OBAuthorizationCodeGrantHandler;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;
import org.wso2.openbanking.cds.common.data.publisher.CDSDataPublishingService;
import org.wso2.openbanking.cds.common.enums.AuthorisationStageEnum;
import org.wso2.openbanking.cds.common.utils.CDSCommonUtils;
import org.wso2.openbanking.cds.common.utils.CommonConstants;
import org.wso2.openbanking.cds.identity.grant.type.handlers.utils.CDSGrantHandlerUtil;
import org.wso2.openbanking.cds.identity.utils.CDSIdentityUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * CDS specific authorization code grant handler.
 */
public class CDSAuthorizationCodeGrantHandler extends OBAuthorizationCodeGrantHandler {

    private static Log log = LogFactory.getLog(CDSAuthorizationCodeGrantHandler.class);
    private final ConsentCoreServiceImpl consentCoreService;
    private CDSDataPublishingService dataPublishingService = CDSDataPublishingService.getCDSDataPublishingService();

    public CDSAuthorizationCodeGrantHandler() {
        this.consentCoreService = new ConsentCoreServiceImpl();
    }

    public CDSAuthorizationCodeGrantHandler(ConsentCoreServiceImpl consentCoreService) {
        this.consentCoreService = consentCoreService;
    }

    /**
     * Set refresh token validity period and add cdr_arrangement_id.
     *
     * @param oAuth2AccessTokenRespDTO - OAuth2 Access Token Response DTO
     * @param tokReqMsgCtx             - Token Request Message Context
     * @throws IdentityOAuth2Exception - Identity OAuth2 Exception
     */
    @Override
    public void executeInitialStep(OAuth2AccessTokenRespDTO oAuth2AccessTokenRespDTO,
                                   OAuthTokenReqMessageContext tokReqMsgCtx) throws IdentityOAuth2Exception {

        // add cdr_arrangement_id to the token response dto
        CDSGrantHandlerUtil.populateCDRArrangementID(oAuth2AccessTokenRespDTO, tokReqMsgCtx.getScope());
    }

    /**
     * Publish token related data.
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

        try {
            String cdrArrangementId = oAuth2AccessTokenRespDTO.getParameter(CommonConstants.CDR_ARRANGEMENT_ID);
            ConsentAttributes consentAttributes = consentCoreService.getConsentAttributes(cdrArrangementId);
            String requestUriKey = consentAttributes.getConsentAttributes().get(CommonConstants.REQUEST_URI_KEY);

            Map<String, Object> abandonedConsentFlowData = CDSCommonUtils
                    .generateAbandonedConsentFlowDataMap(
                            requestUriKey,
                            cdrArrangementId,
                            AuthorisationStageEnum.COMPLETED);

            dataPublishingService.publishAbandonedConsentFlowData(abandonedConsentFlowData);
        } catch (ConsentManagementException e) {
            log.error("Error while retrieving request URI from consent attributes", e);
        }
    }

    /**
     * Extend this method to perform any actions related when issuing refresh token.
     *
     * @return
     */
    @Override
    public OAuth2AccessTokenRespDTO issue(OAuthTokenReqMessageContext tokReqMsgCtx) throws IdentityOAuth2Exception {

        OAuthTokenReqMessageContext tokenReqMessageContext = getTokenMessageContext();

        if (isRegulatory(tokenReqMessageContext)) {
            long sharingDuration;
            String[] scopes = tokenReqMessageContext.getScope();
            String consentId = CDSIdentityUtil.getConsentId(scopes);
            sharingDuration = CDSIdentityUtil.getRefreshTokenValidityPeriod(consentId);
            // set refresh token validity period for token request message context
            tokenReqMessageContext.setRefreshTokenvalidityPeriod(sharingDuration);
            if (log.isDebugEnabled()) {
                log.debug("Refresh token validity period is set to: " + sharingDuration);
            }

        }
        return super.issue(tokReqMsgCtx);
    }

    @Override
    public boolean issueRefreshToken() throws IdentityOAuth2Exception {

        OAuthTokenReqMessageContext tokenReqMessageContext = getTokenMessageContext();
        if (isRegulatory(tokenReqMessageContext)) {
            long sharingDuration;
            String[] scopes = tokenReqMessageContext.getScope();
            String consentId = CDSIdentityUtil.getConsentId(scopes);
            sharingDuration = CDSIdentityUtil.getRefreshTokenValidityPeriod(consentId);
            return sharingDuration != 0;
        }
        return true;
    }

    @Generated(message = "Excluding from code coverage since it requires a service call")
    protected OAuthTokenReqMessageContext getTokenMessageContext() {

        return OAuth2Util.getTokenRequestContext();
    }

    @Generated(message = "Excluding from code coverage since it requires a service call")
    protected boolean isRegulatory(OAuthTokenReqMessageContext tokenReqMessageContext) throws IdentityOAuth2Exception {

        try {
            return IdentityCommonUtil.getRegulatoryFromSPMetaData(tokenReqMessageContext.getOauth2AccessTokenReqDTO()
                    .getClientId());
        } catch (OpenBankingException e) {
            throw new IdentityOAuth2Exception("Error occurred while getting sp property from sp meta data");
        }

    }
}

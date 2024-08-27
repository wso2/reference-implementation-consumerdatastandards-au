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

package org.wso2.openbanking.cds.identity.listener;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentAttributes;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.accelerator.data.publisher.common.constants.DataPublishingConstants;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth.cache.OAuthCache;
import org.wso2.carbon.identity.oauth.cache.OAuthCacheKey;
import org.wso2.carbon.identity.oauth.event.AbstractOAuthEventInterceptor;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dao.AuthorizationCodeValidationResult;
import org.wso2.carbon.identity.oauth2.dao.OAuthTokenPersistenceFactory;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.model.AuthzCodeDO;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;
import org.wso2.openbanking.cds.common.data.publisher.CDSDataPublishingService;
import org.wso2.openbanking.cds.common.enums.AuthorisationStageEnum;
import org.wso2.openbanking.cds.common.utils.CDSCommonUtils;
import org.wso2.openbanking.cds.common.utils.CommonConstants;
import org.wso2.openbanking.cds.identity.utils.CDSIdentityUtil;

import java.util.Map;

/**
 * Listener for events triggered during the token issuing process.
 */
public class CDSTokenIssueListener extends AbstractOAuthEventInterceptor {

    private static Log log = LogFactory.getLog(CDSTokenIssueListener.class);
    private final ConsentCoreServiceImpl consentCoreService;

    public CDSTokenIssueListener() {
        this.consentCoreService = new ConsentCoreServiceImpl();
    }

    public CDSTokenIssueListener(ConsentCoreServiceImpl consentCoreService) {
        this.consentCoreService = consentCoreService;
    }

    @Override
    public void onPreTokenIssue(OAuth2AccessTokenReqDTO tokenReqDTO, OAuthTokenReqMessageContext tokReqMsgCtx,
                                Map<String, Object> params) throws IdentityOAuth2Exception {

        try {
            if (!isTokenIssueListenerExecutable(tokReqMsgCtx)) {
                return;
            }

            AuthzCodeDO authzCodeDO = getPersistedAuthzCode(tokenReqDTO);

            if (authzCodeDO == null) {
                log.debug("Returning without setting the cdr-arrangement-id property to the " +
                        "token request message context.");
                return;
            }

            String cdrArrangementId = CDSIdentityUtil.getConsentId(authzCodeDO.getScope());

            /* Adding the cdr-arrangement-id to the tokReqMsgCtx to be used in the onPostTokenIssue method since
             it is not available in that flow. */
            tokReqMsgCtx.addProperty(CommonConstants.CDR_ARRANGEMENT_ID, cdrArrangementId);
        } catch (OpenBankingException e) {
            log.error("Error while retrieving SP meta data", e);
        }
    }

    @Override
    public void onPostTokenIssue(OAuth2AccessTokenReqDTO tokenReqDTO, OAuth2AccessTokenRespDTO tokenRespDTO,
                                 OAuthTokenReqMessageContext tokReqMsgCtx, Map<String, Object> params) {

        try {
            if (!isTokenIssueListenerExecutable(tokReqMsgCtx)) {
                return;
            }

        /* These are the only 2 types of errors that will make the token flow abandoned instantly since the
        token exchange is not possible after one of the following errors. */
            String errorMsg = tokenRespDTO.getErrorMsg();
            if (!("Callback url mismatch".equalsIgnoreCase(errorMsg) ||
                    "PKCE validation failed".equalsIgnoreCase(errorMsg))) {
                return;
            }

            String cdrArrangementId = (String) tokReqMsgCtx.getProperty(CommonConstants.CDR_ARRANGEMENT_ID);
            ConsentAttributes consentAttributes = consentCoreService.getConsentAttributes(cdrArrangementId);
            String requestUriKey = consentAttributes.getConsentAttributes().get(CommonConstants.REQUEST_URI_KEY);

            Map<String, Object> abandonedConsentFlowData = CDSCommonUtils
                    .generateAbandonedConsentFlowDataMap(
                            requestUriKey,
                            cdrArrangementId,
                            AuthorisationStageEnum.TOKEN_EXCHANGE_FAILED);

            CDSDataPublishingService.getCDSDataPublishingService()
                    .publishAbandonedConsentFlowData(abandonedConsentFlowData);
        } catch (ConsentManagementException e) {
            log.error("Error while retrieving request URI from consent attributes", e);
        } catch (OpenBankingException e) {
            log.error("Error while retrieving SP meta data", e);
        }
    }

    private AuthzCodeDO getPersistedAuthzCode(OAuth2AccessTokenReqDTO tokenReqDTO) throws IdentityOAuth2Exception {

        if (OAuthCache.getInstance().isEnabled()) {
            OAuthCacheKey cacheKey = new OAuthCacheKey(OAuth2Util.buildCacheKeyStringForAuthzCode(
                    tokenReqDTO.getClientId(), tokenReqDTO.getAuthorizationCode()));
            AuthzCodeDO authzCodeDO = (AuthzCodeDO) OAuthCache.getInstance().getValueFromCache(cacheKey);
            if (authzCodeDO != null) {
                return authzCodeDO;
            }
            if (log.isDebugEnabled()) {
                log.debug("Authorization Code Info was not available in cache for client id : " +
                        tokenReqDTO.getClientId());
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Retrieving authorization code information from db for client id : " +
                    tokenReqDTO.getClientId());
        }
        AuthorizationCodeValidationResult validationResult = OAuthTokenPersistenceFactory.getInstance()
                .getAuthorizationCodeDAO().validateAuthorizationCode(tokenReqDTO.getClientId(),
                        tokenReqDTO.getAuthorizationCode());
        return validationResult != null ? validationResult.getAuthzCodeDO() : null;
    }

    private boolean isTokenIssueListenerExecutable(OAuthTokenReqMessageContext tokReqMsgCtx)
            throws OpenBankingException {

        boolean dataPublishingEnabled = Boolean.parseBoolean((String) OpenBankingConfigParser
                .getInstance()
                .getConfiguration()
                .get(DataPublishingConstants.DATA_PUBLISHING_ENABLED));
        boolean isRegulatoryApplication = IdentityCommonUtil.getRegulatoryFromSPMetaData(tokReqMsgCtx
                .getOauth2AccessTokenReqDTO()
                .getClientId());
        String grantType = tokReqMsgCtx.getOauth2AccessTokenReqDTO().getGrantType();

        return isRegulatoryApplication && dataPublishingEnabled &&
                CommonConstants.AUTHORIZATION_CODE.equalsIgnoreCase(grantType);
    }

}

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

package org.wso2.openbanking.cds.identity.grant.type.handlers.utils;

import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;
import org.wso2.openbanking.cds.common.utils.CDSCommonUtils;
import org.wso2.openbanking.cds.common.utils.CommonConstants;
import org.wso2.openbanking.cds.identity.utils.CDSIdentityUtil;

/**
 * Utility class for grant handlers.
 */
public class CDSGrantHandlerUtil {

    /**
     * Populates cdr_arrangement_id parameter in the token response.
     *
     * @param oAuth2AccessTokenRespDTO oAuth2AccessTokenResponseDTO
     * @param scopes                   token scopes
     */
    public static void populateCDRArrangementID(OAuth2AccessTokenRespDTO oAuth2AccessTokenRespDTO, String[] scopes) {

        String consentId = CDSIdentityUtil.getConsentId(scopes);
        oAuth2AccessTokenRespDTO.addParameter(CommonConstants.CDR_ARRANGEMENT_ID, consentId);
    }

    /**
     * Retrieve access token from the oAuth2AccessTokenRespDTO and encrypt if required.
     *
     * @param oAuth2AccessTokenRespDTO oAuth2AccessTokenRespDTO
     * @return access token string
     */
    public static String retrieveAccessToken(OAuth2AccessTokenRespDTO oAuth2AccessTokenRespDTO) {

        String accessToken = oAuth2AccessTokenRespDTO.getAccessToken();
        // Encrypt access token
        if (accessToken != null && OpenBankingCDSConfigParser.getInstance().isTokenEncryptionEnabled()) {
            accessToken = CDSCommonUtils.encryptAccessToken(accessToken);
        }
        return accessToken;
    }

}

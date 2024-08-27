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
import com.wso2.openbanking.accelerator.identity.interceptor.OBIntrospectionDataProvider;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants;
import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dto.OAuth2IntrospectionResponseDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationRequestDTO;
import org.wso2.openbanking.cds.common.utils.CommonConstants;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * CDS Introspection data provider.
 */
public class CDSIntrospectionDataProvider extends OBIntrospectionDataProvider {

    @Override
    public Map<String, Object> getIntrospectionData(OAuth2TokenValidationRequestDTO oAuth2TokenValidationRequestDTO,
                                                    OAuth2IntrospectionResponseDTO oAuth2IntrospectionResponseDTO)
            throws IdentityOAuth2Exception {

        /* No need to check whether the request is for a refresh token introspection because it is validated before
        this point in CDSTokenIntrospectionListener. */
        Map<String, Object> additionalIntrospectionData = new HashMap<>();
        String cdrArrangementId = getCdrArrangementIdFromScopes(oAuth2IntrospectionResponseDTO);
        if (StringUtils.isNotBlank(cdrArrangementId)) {
            additionalIntrospectionData.put(CommonConstants.CDR_ARRANGEMENT_ID, cdrArrangementId);
        }
        return additionalIntrospectionData;
    }

    /**
     * Extracts CDR arrangement ID from scopes.
     *
     * @return CDR arrangement ID
     */
    private String getCdrArrangementIdFromScopes(OAuth2IntrospectionResponseDTO oAuth2IntrospectionResponseDTO) {

        String consentIdClaim = OpenBankingConfigParser.getInstance().getConfiguration()
                .get(IdentityCommonConstants.CONSENT_ID_CLAIM_NAME).toString();
        String scopes = oAuth2IntrospectionResponseDTO.getScope();
        String cdrArrangementIdWithPrefix = Arrays.stream(scopes.split(IdentityCommonConstants.SPACE_SEPARATOR))
                .filter(word -> word.startsWith(consentIdClaim))
                .findFirst()
                .orElse(null);
        if (StringUtils.isNotBlank(cdrArrangementIdWithPrefix)) {
            return StringUtils.removeStart(cdrArrangementIdWithPrefix, consentIdClaim);
        }
        return null;
    }
}

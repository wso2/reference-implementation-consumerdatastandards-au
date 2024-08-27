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

import org.wso2.carbon.identity.oauth.event.AbstractOAuthEventInterceptor;
import org.wso2.carbon.identity.oauth2.dto.OAuth2IntrospectionResponseDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationRequestDTO;

import java.util.Map;

/**
 * Event listener for token introspection.
 */
public class CDSTokenIntrospectionListener extends AbstractOAuthEventInterceptor {
    private static final String REFRESH = "Refresh";

    /**
     * Allow token introspection for only the refresh token.
     *
     * @param oAuth2TokenValidationRequestDTO
     * @param oAuth2IntrospectionResponseDTO
     * @param params
     */
    @Override
    public void onPostTokenValidation(OAuth2TokenValidationRequestDTO oAuth2TokenValidationRequestDTO,
                                      OAuth2IntrospectionResponseDTO oAuth2IntrospectionResponseDTO,
                                      Map<String, Object> params) {


        if (oAuth2IntrospectionResponseDTO.isActive() &&
                !REFRESH.equalsIgnoreCase(oAuth2IntrospectionResponseDTO.getTokenType())) {
            // CDS specified only to support refresh token introspection.
            // As in rfc7662 section-2.2 : returning as inactive, for the tokens that are not allowed to introspect.
            oAuth2IntrospectionResponseDTO.setActive(false);
            oAuth2IntrospectionResponseDTO.setError("Introspection is supported only for refresh tokens.");
            return;
        }
    }
}

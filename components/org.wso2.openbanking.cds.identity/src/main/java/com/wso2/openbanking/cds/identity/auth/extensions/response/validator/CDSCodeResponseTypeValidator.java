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

package org.wso2.openbanking.cds.identity.auth.extensions.response.validator;

import com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oltu.oauth2.as.validator.TokenValidator;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.wso2.openbanking.cds.identity.utils.CDSIdentityConstants;

import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

/**
 * Validator for code flow token requests.
 */
public class CDSCodeResponseTypeValidator extends TokenValidator {

    private static final Log log = LogFactory.getLog(CDSCodeResponseTypeValidator.class);

    @Override
    public void validateRequiredParameters(HttpServletRequest request) throws OAuthProblemException {

        this.requiredParams = new ArrayList(Arrays.asList(OAuth.OAUTH_CLIENT_ID, IdentityCommonConstants.REQUEST_URI));
        this.notAllowedParams.add(IdentityCommonConstants.REQUEST);

        String responseType = IdentityCommonUtil
                .decodeRequestObjectAndGetKey(request, CDSIdentityConstants.RESPONSE_TYPE);
        String responseMode = IdentityCommonUtil
                .decodeRequestObjectAndGetKey(request, CDSIdentityConstants.RESPONSE_MODE);
        String state = IdentityCommonUtil
                .decodeRequestObjectAndGetKey(request, CDSIdentityConstants.STATE);

        //If the response type is "code", only the "jwt" response mode can be used.
        if (CDSIdentityConstants.CODE_RESPONSE_TYPE.equalsIgnoreCase(responseType) &&
                !CDSIdentityConstants.JWT_RESPONSE_MODE.equalsIgnoreCase(responseMode)) {
            log.error(CDSIdentityConstants.UNSUPPORTED_RESPONSE_MODE);
            throw OAuthProblemException.error(CDSIdentityConstants.UNSUPPORTED_RESPONSE_TYPE_ERROR)
                    .description("Unsupported Response Mode")
                    .state(state);
        }
    }
}

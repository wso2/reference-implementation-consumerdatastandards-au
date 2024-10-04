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
package org.wso2.openbanking.cds.identity.authenticator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth2.bean.OAuthClientAuthnContext;
import org.wso2.openbanking.cds.identity.authenticator.util.Constants;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * CDSArrangementPrivateKeyJWTClientAuthenticator for authenticating CDS arrangement revocation requests.
 */
public class CDSArrangementPrivateKeyJWTClientAuthenticator extends CDSBasePrivateKeyJWTClientAuthenticator {

    private static final Log log = LogFactory.getLog(CDSArrangementPrivateKeyJWTClientAuthenticator.class);

    @Override
    public boolean canAuthenticate(HttpServletRequest httpServletRequest, Map<String, List> bodyParameters,
                                   OAuthClientAuthnContext oAuthClientAuthnContext) {

        if (httpServletRequest.getRequestURI().contains(Constants.ARRANGEMENTS)) {
            log.debug("Request can be handled by CDSArrangementPrivateKeyJWTClientAuthenticator");
            return canSuperAuthenticate(httpServletRequest, bodyParameters, oAuthClientAuthnContext);
        }
        log.debug("CDSArrangementPrivateKeyJWTClientAuthenticator cannot handle the request.");

        return false;
    }

    /**
     * Check if base private key jwt authenticator can authenticate the client.
     *
     * @param httpServletRequest
     * @param bodyParameters
     * @param oAuthClientAuthnContext
     * @return boolean
     */
    private boolean canSuperAuthenticate(HttpServletRequest httpServletRequest, Map<String, List> bodyParameters,
                                         OAuthClientAuthnContext oAuthClientAuthnContext) {
        return super.canAuthenticate(httpServletRequest, bodyParameters, oAuthClientAuthnContext);
    }

}

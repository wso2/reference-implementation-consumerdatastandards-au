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

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth.common.OAuth2ErrorCodes;
import org.wso2.carbon.identity.oauth2.bean.OAuthClientAuthnContext;
import org.wso2.carbon.identity.oauth2.client.authentication.AbstractOAuthClientAuthenticator;
import org.wso2.carbon.identity.oauth2.client.authentication.OAuthClientAuthnException;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;
import org.wso2.openbanking.cds.identity.authenticator.util.CDSJWTValidator;
import org.wso2.openbanking.cds.identity.authenticator.util.Constants;
import org.wso2.openbanking.cds.identity.dcr.constants.CDSValidationConstants;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

/**
 * CDS Customized Client Authentication handler to implement oidc private_key_jwt client authentication
 * Supports validating multiple audience claim values according to the CDS Specification
 * http://openid.net/specs/openid-connect-core-1_0.html#ClientAuthentication.
 */
public class CDSBasePrivateKeyJWTClientAuthenticator extends AbstractOAuthClientAuthenticator {

    private static final Log LOG = LogFactory.getLog(CDSBasePrivateKeyJWTClientAuthenticator.class);
    private CDSJWTValidator jwtValidator;

    public CDSBasePrivateKeyJWTClientAuthenticator() {

        int rejectBeforePeriod = Constants.DEFAULT_VALIDITY_PERIOD_IN_MINUTES;
        boolean preventTokenReuse = true;
        String endpointAlias = Constants.DEFAULT_AUDIENCE;
        try {
            if (isNotEmpty(properties.getProperty(Constants.ENDPOINT_ALIAS))) {
                endpointAlias = properties.getProperty(Constants.ENDPOINT_ALIAS);
            }
            if (isNotEmpty(properties.getProperty(Constants.PREVENT_TOKEN_REUSE))) {
                preventTokenReuse = Boolean.parseBoolean(properties.getProperty(Constants.PREVENT_TOKEN_REUSE));
            }
            if (isNotEmpty(properties.getProperty(Constants.REJECT_BEFORE_IN_MINUTES))) {
                rejectBeforePeriod = Integer.parseInt(properties.getProperty(Constants.REJECT_BEFORE_IN_MINUTES));
            }
            jwtValidator = createJWTValidator(endpointAlias, preventTokenReuse, rejectBeforePeriod);
        } catch (NumberFormatException e) {
            LOG.warn("Invalid PrivateKeyJWT Validity period found in the configuration. Using default value: " +
                    rejectBeforePeriod);
        }
    }

    /**
     * To check whether the authentication is successful.
     *
     * @param httpServletRequest      http servelet request
     * @param bodyParameters          map of request body params
     * @param oAuthClientAuthnContext oAuthClientAuthnContext
     * @return true if the authentication is successful.
     * @throws OAuthClientAuthnException
     */
    @Override
    public boolean authenticateClient(HttpServletRequest httpServletRequest, Map<String, List> bodyParameters,
                                      OAuthClientAuthnContext oAuthClientAuthnContext)
            throws OAuthClientAuthnException {

        validateClientIdAgainstSubClaim(httpServletRequest, bodyParameters, oAuthClientAuthnContext);
        return jwtValidator.isValidAssertion(getSignedJWT(bodyParameters, oAuthClientAuthnContext));
    }

    /**
     * Returns whether the incoming request can be handled by the particular authenticator.
     *
     * @param httpServletRequest      http servelet request
     * @param bodyParameters          map of request body params
     * @param oAuthClientAuthnContext oAuthClientAuthnContext
     * @return true if the incoming request can be handled.
     */
    @Override
    public boolean canAuthenticate(HttpServletRequest httpServletRequest, Map<String, List> bodyParameters,
                                   OAuthClientAuthnContext oAuthClientAuthnContext) {

        String oauthJWTAssertionType = getBodyParameters(bodyParameters).get(Constants.OAUTH_JWT_ASSERTION_TYPE);
        String oauthJWTAssertion = getBodyParameters(bodyParameters).get(Constants.OAUTH_JWT_ASSERTION);
        return isValidJWTClientAssertionRequest(oauthJWTAssertionType, oauthJWTAssertion);
    }

    /**
     * Retrievs the client ID which is extracted from the JWT.
     *
     * @param httpServletRequest
     * @param bodyParameters
     * @param oAuthClientAuthnContext
     * @return jwt 'sub' value as the client id
     * @throws OAuthClientAuthnException
     */
    @Override
    public String getClientId(HttpServletRequest httpServletRequest, Map<String, List> bodyParameters,
                              OAuthClientAuthnContext oAuthClientAuthnContext) throws OAuthClientAuthnException {

        SignedJWT signedJWT = getSignedJWT(bodyParameters, oAuthClientAuthnContext);
        JWTClaimsSet claimsSet = jwtValidator.getClaimSet(signedJWT);
        return jwtValidator.resolveSubject(claimsSet);
    }

    private SignedJWT getSignedJWT(Map<String, List> bodyParameters, OAuthClientAuthnContext oAuthClientAuthnContext)
            throws OAuthClientAuthnException {

        Object signedJWTFromContext = oAuthClientAuthnContext.getParameter(Constants.PRIVATE_KEY_JWT);
        if (signedJWTFromContext != null) {
            return (SignedJWT) signedJWTFromContext;
        }
        String assertion = getBodyParameters(bodyParameters).get(Constants.OAUTH_JWT_ASSERTION);
        String errorMessage = "No Valid Assertion was found for " + Constants.OAUTH_JWT_BEARER_GRANT_TYPE;
        SignedJWT signedJWT;
        if (StringUtils.isEmpty(assertion)) {
            throw new OAuthClientAuthnException(errorMessage, OAuth2ErrorCodes.INVALID_REQUEST);
        }
        try {
            signedJWT = SignedJWT.parse(assertion);
        } catch (ParseException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(e.getMessage());
            }
            throw new OAuthClientAuthnException("Error while parsing the JWT.", OAuth2ErrorCodes.INVALID_REQUEST);
        }
        if (signedJWT == null) {
            throw new OAuthClientAuthnException(errorMessage, OAuth2ErrorCodes.INVALID_REQUEST);
        }
        oAuthClientAuthnContext.addParameter(Constants.PRIVATE_KEY_JWT, signedJWT);
        return signedJWT;
    }

    private boolean isValidJWTClientAssertionRequest(String clientAssertionType, String clientAssertion) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Authenticate Requested with clientAssertionType : " + clientAssertionType);
            if (IdentityUtil.isTokenLoggable(IdentityConstants.IdentityTokens.ACCESS_TOKEN)) {
                LOG.debug("Authenticate Requested with clientAssertion : " + clientAssertion);
            }
        }
        return Constants.OAUTH_JWT_BEARER_GRANT_TYPE.equals(clientAssertionType) && isNotEmpty(clientAssertion);
    }

    private CDSJWTValidator createJWTValidator(String accessedEndpoint, boolean preventTokenReuse, int rejectBefore) {

        // Adding accepted audience value as per CDS Specification
        String tokenEndpoint = OAuth2Util.OAuthURL.getOAuth2TokenEPUrl();
        String issuer = OAuth2Util.getIDTokenIssuer();

        List<String> acceptedAudienceList = new ArrayList<>();
        acceptedAudienceList.add(accessedEndpoint);
        acceptedAudienceList.add(tokenEndpoint);
        acceptedAudienceList.add(issuer);

        return new CDSJWTValidator(preventTokenReuse, acceptedAudienceList, rejectBefore, null,
                populateMandatoryClaims(), Constants.DEFAULT_ENABLE_JTI_CACHE);
    }

    private List<String> populateMandatoryClaims() {

        return CDSValidationConstants.MANDATORY_ASSERTION_PARAMS_LIST;
    }

    /**
     * Validates the client_id parameter value against the 'sub' claim in the client_assertion if the client_id is
     * sent as a request body parameter (RFC7521).
     *
     * @param httpServletRequest      - HTTP Servlet Request
     * @param bodyParameters          - map of request body params
     * @param oAuthClientAuthnContext - OAuth Client Authentication Context
     * @throws OAuthClientAuthnException - if the client_id does not match the 'sub' claim in the client_assertion
     */
    private void validateClientIdAgainstSubClaim(HttpServletRequest httpServletRequest,
                                                 Map<String, List> bodyParameters,
                                                 OAuthClientAuthnContext oAuthClientAuthnContext)
            throws OAuthClientAuthnException {

        if (httpServletRequest.getParameter(Constants.CLIENT_ID) != null) {
            String subClaim = getClientId(httpServletRequest, bodyParameters, oAuthClientAuthnContext);
            String clientId = httpServletRequest.getParameter(Constants.CLIENT_ID);
            if (!subClaim.equals(clientId)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Mismatch between client_id parameter and sub claim. client_id: " + clientId +
                            ", sub claim: " + subClaim);
                }
                throw new OAuthClientAuthnException("Request Parameter 'client_id' does not match the 'sub' claim " +
                        "in the client_assertion", OAuth2ErrorCodes.INVALID_CLIENT);
            }
        }
    }
}

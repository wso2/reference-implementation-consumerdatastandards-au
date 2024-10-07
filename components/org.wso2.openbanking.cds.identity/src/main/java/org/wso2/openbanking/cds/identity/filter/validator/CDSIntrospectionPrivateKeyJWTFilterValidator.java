package org.wso2.openbanking.cds.identity.filter.validator;

import com.wso2.openbanking.accelerator.identity.token.util.TokenFilterException;
import com.wso2.openbanking.accelerator.identity.token.validators.OBIdentityFilterValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth.common.OAuth2ErrorCodes;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.identity.oauth2.bean.OAuthClientAuthnContext;
import org.wso2.carbon.identity.oauth2.client.authentication.OAuthClientAuthenticator;
import org.wso2.carbon.identity.oauth2.client.authentication.OAuthClientAuthnException;
import org.wso2.carbon.identity.oauth2.client.authentication.OAuthClientAuthnService;
import org.wso2.openbanking.cds.identity.internal.CDSIdentityDataHolder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * CDS Introspection PrivateKeyJWT Filter Validator.
 * This class explicitly invokes the CDSIntrospectionPrivateKeyJWTClientAuthenticator since authenticators are not
 * engaged to the IAM introspect endpoint by default.
 */
public class CDSIntrospectionPrivateKeyJWTFilterValidator implements OBIdentityFilterValidator {

    protected OAuthClientAuthenticator privateKeyJwtAuthenticator = null;
    private static final String PRIVATE_KEY_JWT_AUTHENTICATOR = "CDSIntrospectionPrivateKeyJWTClientAuthenticator";
    private static final Log log = LogFactory.getLog(CDSIntrospectionPrivateKeyJWTFilterValidator.class);

    protected void init() {
        OAuthClientAuthnService oAuthClientAuthnService = CDSIdentityDataHolder.getInstance().
                getOAuthClientAuthnService();
        List<OAuthClientAuthenticator> authenticators = oAuthClientAuthnService.getClientAuthenticators();
        for (OAuthClientAuthenticator authenticator : authenticators) {
            if (PRIVATE_KEY_JWT_AUTHENTICATOR.equals(authenticator.getName())) {
                privateKeyJwtAuthenticator = authenticator;
                if (log.isDebugEnabled()) {
                    log.debug("Setting " + authenticator.getName() + " Private Key JWT authenticator to the filter");
                }
                return;
            }
        }
    }

    @Override
    public void validate(ServletRequest request, String clientId) throws TokenFilterException, ServletException {

        if (request instanceof HttpServletRequest) {
            HttpServletRequest servletRequest = (HttpServletRequest) request;
            OAuthClientAuthnContext clientAuthnContext = new OAuthClientAuthnContext();
            try {
                authenticateClient(clientAuthnContext, servletRequest,
                        getContentParams(servletRequest));
            } catch (OAuthClientAuthnException e) {
                log.error(e.getMessage());
                throw new TokenFilterException(HttpServletResponse.SC_UNAUTHORIZED, e.getErrorCode(),
                        e.getMessage());
            }
            if (!clientAuthnContext.isAuthenticated()) {
                log.error("Introspection client authentication failed. Invalid client");
                throw new TokenFilterException(HttpServletResponse.SC_UNAUTHORIZED, clientAuthnContext.getErrorCode(),
                        clientAuthnContext.getErrorMessage());
            }
        }

    }

    /**
     * Authenticate an OAuth client using a given client authenticator.
     *
     * @param oAuthClientAuthnContext OAuth client authentication context.
     * @param request                 Incoming HttpServletRequest.
     * @param bodyContentMap          Content of the body as a parameter map.
     * @throws OAuthClientAuthnException OAuth Client Authentication Exception.
     */
    private void authenticateClient(OAuthClientAuthnContext oAuthClientAuthnContext, HttpServletRequest request,
                                    Map<String, List> bodyContentMap) throws OAuthClientAuthnException {

        // Initialize if authenticator is null
        if (privateKeyJwtAuthenticator == null) {
            init();
        }
        //Invoke authenticator
        boolean isAuthenticated = privateKeyJwtAuthenticator.authenticateClient(request, bodyContentMap,
                oAuthClientAuthnContext);
        if (log.isDebugEnabled()) {
            log.debug("Authentication result from " + privateKeyJwtAuthenticator.getName()
                    + " is : " + isAuthenticated);
        }
        oAuthClientAuthnContext.setAuthenticated(isAuthenticated);
        if (isAuthenticated) {
            oAuthClientAuthnContext.setClientId(privateKeyJwtAuthenticator.getClientId(request, bodyContentMap,
                    oAuthClientAuthnContext));
            setContextToRequest(request, oAuthClientAuthnContext);
        } else {
            setErrorToContext(OAuth2ErrorCodes.INVALID_CLIENT, "Client credentials are invalid.",
                    oAuthClientAuthnContext);
        }
    }

    /**
     * Retrieve body content as a String, List map.
     *
     * @param servletRequest
     * @return Body parameter of the incoming request message
     */
    private Map<String, List> getContentParams(ServletRequest servletRequest) {

        Map<String, List> bodyContentMap = new HashMap<>();
        Map<String, String[]> paramMap = servletRequest.getParameterMap();

        for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
            bodyContentMap.put(entry.getKey(), Arrays.asList(entry.getValue()));
        }

        return bodyContentMap;
    }

    /**
     * Set client authentication context to the request.
     *
     * @param request
     * @param oAuthClientAuthnContext
     */
    private void setContextToRequest(HttpServletRequest request, OAuthClientAuthnContext oAuthClientAuthnContext) {

        request.setAttribute(OAuthConstants.CLIENT_AUTHN_CONTEXT,
                oAuthClientAuthnContext);
    }

    /**
     * Sets error messages to context after failing authentication.
     *
     * @param errorCode               Error code.
     * @param errorMessage            Error message.
     * @param oAuthClientAuthnContext OAuth client authentication context.
     */
    private void setErrorToContext(String errorCode, String errorMessage, OAuthClientAuthnContext
            oAuthClientAuthnContext) {

        if (log.isDebugEnabled()) {
            log.debug("Setting error to client authentication context : Error code : " + errorCode + ", Error " +
                    "message : " + errorMessage);
        }
        oAuthClientAuthnContext.setAuthenticated(false);
        oAuthClientAuthnContext.setErrorCode(errorCode);
        oAuthClientAuthnContext.setErrorMessage(errorMessage);
    }
}

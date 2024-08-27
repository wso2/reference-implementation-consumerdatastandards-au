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

package org.wso2.openbanking.cds.identity.filter;

import com.nimbusds.jwt.SignedJWT;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.identity.token.util.TokenFilterException;
import com.wso2.openbanking.accelerator.identity.token.validators.OBIdentityFilterValidator;
import com.wso2.openbanking.accelerator.identity.token.wrapper.RequestWrapper;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonHelper;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.openbanking.cds.identity.filter.exception.CDSFilterException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

/**
 * Base class for CDS specific tomcat filters.
 */
public class CDSBaseFilter implements Filter {

    private static final Log log = LogFactory.getLog(CDSBaseFilter.class);
    protected List<OBIdentityFilterValidator> validators = new ArrayList<>();

    @Override
    @Generated(message = "Excluding from code coverage since it requires a service call")
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        String clientId;
        try {
            clientId = this.extractClientId(servletRequest);
        } catch (CDSFilterException e) {
            log.error("Error occurred while extracting client id from the request");
            handleValidationFailure((HttpServletResponse) servletResponse, e.getErrorCode(),
                    e.getMessage(), e.getErrorDescription());
            return;
        }

        try {
            if (IdentityCommonUtil.getRegulatoryFromSPMetaData(clientId)) {
                servletRequest = setTransportCertAsHeader(servletRequest, servletResponse);
                for (OBIdentityFilterValidator validator : validators) {
                    validator.validate(servletRequest, clientId);
                }
            }
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (TokenFilterException e) {
            log.error(String.format("Validation failure occurred. %s", e.getErrorDescription()));
            handleValidationFailure((HttpServletResponse) servletResponse,
                    e.getErrorCode(), e.getMessage(), e.getErrorDescription());
        } catch (OpenBankingException e) {
            log.error(String.format("Validation failure occurred. %s", e.getMessage()));
            if (e.getMessage().contains("Error occurred while retrieving OAuth2 application data")) {
                handleValidationFailure((HttpServletResponse) servletResponse,
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "OAuth2 application data retrieval failed",
                        e.getMessage());
            } else {
                handleValidationFailure((HttpServletResponse) servletResponse,
                        HttpServletResponse.SC_BAD_REQUEST, "Service provider metadata retrieval failed",
                        e.getMessage());
            }
        }
    }

    /**
     * Append the transport header to the request.
     *
     * @param request
     * @param response
     * @return ServletRequest
     * @throws ServletException
     */
    protected ServletRequest setTransportCertAsHeader(ServletRequest request, ServletResponse response) throws
            ServletException, IOException {

        if (request instanceof HttpServletRequest) {
            Object certAttribute = request.getAttribute(IdentityCommonConstants.JAVAX_SERVLET_REQUEST_CERTIFICATE);
            String x509Certificate = ((HttpServletRequest) request).getHeader(IdentityCommonUtil.getMTLSAuthHeader());
            if (certAttribute != null) {
                RequestWrapper requestWrapper = new RequestWrapper((HttpServletRequest) request);
                X509Certificate certificate = getCertificateFromAttribute(certAttribute);
                if (certificate != null) {
                    requestWrapper.setHeader(IdentityCommonUtil.getMTLSAuthHeader(),
                            getCertificateContent(certificate));
                    return requestWrapper;
                } else {
                    String errorMessage = "Transport certificate not found in the request";
                    log.error(errorMessage);
                    handleValidationFailure((HttpServletResponse) response,
                            HttpServletResponse.SC_BAD_REQUEST, "Transport certificate not found", errorMessage);
                }
            } else if (new IdentityCommonHelper().isTransportCertAsHeaderEnabled() && x509Certificate != null) {
                return request;
            } else {
                String errorMessage = "Transport certificate not found in the request";
                log.error(errorMessage);
                handleValidationFailure((HttpServletResponse) response,
                        HttpServletResponse.SC_BAD_REQUEST, "Transport certificate not found", errorMessage);
            }
        } else {
            throw new ServletException("Error occurred when handling the request, passed request is not a " +
                    "HttpServletRequest");
        }
        return request;
    }

    private String getCertificateContent(X509Certificate certificate) throws ServletException {

        if (certificate != null) {
            try {
                Base64 encoder = new Base64();
                byte[] encodedContent = certificate.getEncoded();
                return IdentityCommonConstants.BEGIN_CERT + new String(encoder.encode(encodedContent),
                        StandardCharsets.UTF_8) + IdentityCommonConstants.END_CERT;
            } catch (CertificateEncodingException e) {
                log.error(String.format("Certificate not valid. %s", e.getMessage()));
                throw new ServletException("Certificate not valid", e);
            }
        } else {
            return null;
        }
    }

    private X509Certificate getCertificateFromAttribute(Object certObject) throws ServletException {

        if (certObject instanceof X509Certificate[]) {
            X509Certificate[] cert = (X509Certificate[]) certObject;
            return cert[0];
        } else if (certObject instanceof X509Certificate) {
            return (X509Certificate) certObject;
        }
        return null;
    }

    /**
     * Extracts the client id from the request parameter or from the assertion.
     *
     * @param request servlet request containing the request data
     * @return clientId
     * @throws ParseException
     */
    @Generated(message = "Excluding from code coverage since it requires a service call")
    protected String extractClientId(ServletRequest request) throws CDSFilterException {

        try {
            Optional<String> signedObject =
                    Optional.ofNullable(request.getParameter(IdentityCommonConstants.OAUTH_JWT_ASSERTION));
            Optional<String> clientIdAsReqParam =
                    Optional.ofNullable(request.getParameter(IdentityCommonConstants.CLIENT_ID));
            if (signedObject.isPresent()) {
                SignedJWT signedJWT = SignedJWT.parse(signedObject.get());
                return signedJWT.getJWTClaimsSet().getIssuer();
            } else if (clientIdAsReqParam.isPresent()) {
                return clientIdAsReqParam.get();
            } else {
                throw new CDSFilterException(HttpServletResponse.SC_BAD_REQUEST, "Client ID not retrieved",
                        "Unable to find client id in the request");
            }
        } catch (ParseException e) {
            log.error(String.format("Error occurred while parsing the JWT. %s", e.getMessage()));
            throw new CDSFilterException(HttpServletResponse.SC_UNAUTHORIZED, "Invalid assertion", "Error " +
                    "occurred while parsing the signed assertion", e);
        }
    }

    /**
     * Respond when there is a failure in filter validation.
     *
     * @param response     HTTP servlet response object
     * @param status       HTTP status code
     * @param error        error
     * @param errorMessage error description
     * @throws IOException
     */
    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
    // Suppressed content - try (OutputStream outputStream = response.getOutputStream())
    // Suppression reason - False Positive : This occurs with Java 11 when using try-with-resources and when that
    //                                       resource is being referred within the try block. This is a known issue in
    //                                       the plugin and therefore it is being suppressed.
    //                                       https://github.com/spotbugs/spotbugs/issues/1694
    protected void handleValidationFailure(HttpServletResponse response, int status, String error, String errorMessage)
            throws IOException {

        JSONObject errorJSON = new JSONObject();
        errorJSON.put(IdentityCommonConstants.OAUTH_ERROR, error);
        errorJSON.put(IdentityCommonConstants.OAUTH_ERROR_DESCRIPTION, errorMessage);

        try (OutputStream outputStream = response.getOutputStream()) {
            response.setStatus(status);
            response.setContentType(MediaType.APPLICATION_JSON);
            outputStream.write(errorJSON.toString().getBytes(StandardCharsets.UTF_8));
        }
    }
}

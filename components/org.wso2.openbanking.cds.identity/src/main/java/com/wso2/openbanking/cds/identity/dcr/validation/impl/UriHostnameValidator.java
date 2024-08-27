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

package org.wso2.openbanking.cds.identity.dcr.validation.impl;

import com.wso2.openbanking.accelerator.common.util.JWTUtils;
import com.wso2.openbanking.accelerator.identity.dcr.model.RegistrationRequest;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;
import org.wso2.openbanking.cds.identity.dcr.constants.CDSValidationConstants;
import org.wso2.openbanking.cds.identity.dcr.validation.annotation.ValidateUriHostnames;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator class to validate Redirect Urls are valid Urls.
 */
public class UriHostnameValidator implements ConstraintValidator<ValidateUriHostnames, Object> {

    private static final Log log = LogFactory.getLog(RedirectUriFormatValidator.class);
    private String registrationRequestPath;
    private String ssaPath;

    @Override
    public void initialize(ValidateUriHostnames validateUriHostnames) {
        this.registrationRequestPath = validateUriHostnames.registrationRequestProperty();
        this.ssaPath = validateUriHostnames.ssa();
    }

    @Override
    public boolean isValid(Object cdsRegistrationRequest, ConstraintValidatorContext constraintValidatorContext) {

        try {
            RegistrationRequest registrationRequest = (RegistrationRequest) new PropertyUtilsBean()
                    .getProperty(cdsRegistrationRequest, registrationRequestPath);

            String softwareStatement = BeanUtils.getProperty(registrationRequest, ssaPath);
            List<String> redirectURIsFromSSA = (List<String>) JWTUtils
                    .decodeRequestJWT(softwareStatement, "body").get(CDSValidationConstants.SSA_REDIRECT_URIS);
            String logoURI = JWTUtils.decodeRequestJWT(softwareStatement, "body")
                    .getAsString(CDSValidationConstants.SSA_LOGO_URI);
            String clientURI = JWTUtils.decodeRequestJWT(softwareStatement, "body")
                    .getAsString(CDSValidationConstants.SSA_CLIENT_URI);
            String policyURI = JWTUtils.decodeRequestJWT(softwareStatement, "body")
                    .getAsString(CDSValidationConstants.SSA_POLICY_URI);
            String termOfServiceURI = JWTUtils.decodeRequestJWT(softwareStatement, "body")
                    .getAsString(CDSValidationConstants.SSA_TOS_URI);

            return validateURIHostNames(redirectURIsFromSSA, logoURI, clientURI, policyURI, termOfServiceURI);

        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error("Error while resolving validation fields", e);
        } catch (ParseException e) {
            log.error("Error while parsing the softwareStatement", e);
        }
        return false;
    }

    /**
     * Check the hostnames of redirect uris and other uris.
     *
     * @param redirectURIs     redirect uris included in the software statement
     * @param logoURI          logo uri in the software statement
     * @param clientURI        client uri included in the software statement
     * @param policyURI        policy uri included in the software statement
     * @param termOfServiceURI termOfService uri included in the software statement
     * @return true if the uris are validated
     */
    public static boolean validateURIHostNames(List<String> redirectURIs, String logoURI, String clientURI,
                                               String policyURI, String termOfServiceURI) {

        String logoURIHost;
        String clientURIHost;
        String policyURIHost;
        String termsOfServiceURIHost;
        try {
            if (StringUtils.isNotBlank(logoURI) && StringUtils.isNotBlank(clientURI)) {
                logoURIHost = new URI(logoURI).getHost();
                clientURIHost = new URI(clientURI).getHost();
            } else {
                return false;
            }
            if (StringUtils.isNotBlank(policyURI) && StringUtils.isNotBlank(termOfServiceURI)) {
                policyURIHost = new URI(policyURI).getHost();
                termsOfServiceURIHost = new URI(termOfServiceURI).getHost();

            } else {
                policyURIHost = StringUtils.EMPTY;
                termsOfServiceURIHost = StringUtils.EMPTY;
            }
            //check whether the hostnames of policy,logo,client and terms of service uris match with redirect uri
            //hostname if the validation is set to true
            String isHostNameValidationEnabled = (String) OpenBankingCDSConfigParser.getInstance().getConfiguration()
                    .get(CDSValidationConstants.DCR_VALIDATE_URI_HOSTNAME);
            if (Boolean.parseBoolean(isHostNameValidationEnabled)) {
                for (String redirectURI : redirectURIs) {
                    //check whether the redirect uris and other given uris have same host name
                    String uriHost = new URI(redirectURI).getHost();
                    if (!(logoURIHost.equals(uriHost) && clientURIHost.equals(uriHost)
                            && policyURIHost.equals(uriHost) && termsOfServiceURIHost.equals(uriHost))) {
                        return false;
                    }
                }
            }
        } catch (URISyntaxException e) {
            log.error("Malformed redirect uri", e);
            return false;
        }
        return true;
    }
}

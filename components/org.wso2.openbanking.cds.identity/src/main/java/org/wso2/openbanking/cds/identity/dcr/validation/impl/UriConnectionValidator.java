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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;
import org.wso2.openbanking.cds.identity.dcr.constants.CDSValidationConstants;
import org.wso2.openbanking.cds.identity.dcr.validation.annotation.ValidateUriConnection;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;

import javax.net.ssl.HttpsURLConnection;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator class to check validity of tos, policy, client and logo Urls.
 */
public class UriConnectionValidator implements ConstraintValidator<ValidateUriConnection, Object> {

    private static final Log log = LogFactory.getLog(UriConnectionValidator.class);
    private String registrationRequestPath;
    private String ssaPath;

    @Override
    public void initialize(ValidateUriConnection validateUriConnection) {
        this.registrationRequestPath = validateUriConnection.registrationRequestProperty();
        this.ssaPath = validateUriConnection.ssa();
    }

    @Override
    public boolean isValid(Object cdsRegistrationRequest, ConstraintValidatorContext constraintValidatorContext) {

        try {
            RegistrationRequest registrationRequest = (RegistrationRequest) new PropertyUtilsBean()
                    .getProperty(cdsRegistrationRequest, registrationRequestPath);

            String softwareStatement = BeanUtils.getProperty(registrationRequest, ssaPath);
            String logoURI = JWTUtils.decodeRequestJWT(softwareStatement, "body")
                    .getAsString(CDSValidationConstants.SSA_LOGO_URI);
            String clientURI = JWTUtils.decodeRequestJWT(softwareStatement, "body")
                    .getAsString(CDSValidationConstants.SSA_CLIENT_URI);
            String policyURI = JWTUtils.decodeRequestJWT(softwareStatement, "body")
                    .getAsString(CDSValidationConstants.SSA_POLICY_URI);
            String termOfServiceURI = JWTUtils.decodeRequestJWT(softwareStatement, "body")
                    .getAsString(CDSValidationConstants.SSA_TOS_URI);

            // check whether the policy,logo,client and terms of service uris are valid uris if the
            // validation is set to true
            String isURIValidationEnabled = (String) OpenBankingCDSConfigParser.getInstance().getConfiguration()
                    .get(CDSValidationConstants.DCR_VALIDATE_URI);

            if (Boolean.parseBoolean(isURIValidationEnabled)) {
                return checkValidityOfURI(logoURI, clientURI, policyURI, termOfServiceURI);
            } else {
                return true;
            }

        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error("Error while resolving validation fields", e);
        } catch (ParseException e) {
            log.error("Error while parsing the softwareStatement", e);
        }
        return false;
    }

    /**
     * validate the policy,terms of service,logo and client uris.
     */
    public static boolean checkValidityOfURI(String logoURI, String clientURI, String policyURI,
                                             String termOfServiceURI) {

        HttpsURLConnection connection = null;
        String[] uris = new String[]{logoURI, clientURI, policyURI, termOfServiceURI};
        for (String uri : uris) {
            try {
                connection = (HttpsURLConnection) new URL(uri).openConnection();
                //check whether uri resolves to valid web page
                connection.connect();
                if (connection.getResponseCode() != 200) {
                    return false;
                }
            } catch (MalformedURLException e) {
                log.error("Malformed redirect uri", e);
                return false;
            } catch (IOException e) {
                log.error("Error while connecting to the redirect uri", e);
                return false;
            } finally {
                assert connection != null;
                connection.disconnect();
            }
        }
        return true;
    }
}

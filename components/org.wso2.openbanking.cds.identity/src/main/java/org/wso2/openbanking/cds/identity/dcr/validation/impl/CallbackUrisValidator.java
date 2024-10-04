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
import org.wso2.openbanking.cds.identity.dcr.constants.CDSValidationConstants;
import org.wso2.openbanking.cds.identity.dcr.validation.annotation.ValidateCallbackUris;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator class for validating the callback URIs of the registration request.
 */
public class CallbackUrisValidator implements ConstraintValidator<ValidateCallbackUris, Object> {

    private static final Log log = LogFactory.getLog(CallbackUrisValidator.class);

    private String registrationRequestPath;
    private String redirectUriPath;
    private String ssaPath;

    @Override
    public void initialize(ValidateCallbackUris validateCallbackUris) {

        this.registrationRequestPath = validateCallbackUris.registrationRequestProperty();
        this.redirectUriPath = validateCallbackUris.callbackUrisProperty();
        this.ssaPath = validateCallbackUris.ssa();
    }

    @Override
    public boolean isValid(Object cdsRegistrationRequest, ConstraintValidatorContext constraintValidatorContext) {

        try {

            RegistrationRequest registrationRequest = (RegistrationRequest) new PropertyUtilsBean()
                    .getProperty(cdsRegistrationRequest, registrationRequestPath);
            String softwareStatement = BeanUtils.getProperty(cdsRegistrationRequest, ssaPath);
            List<String> callbackUris = registrationRequest.getCallbackUris();
            if (callbackUris != null && !callbackUris.isEmpty()) {
                final Object ssaCallbackUris = JWTUtils.decodeRequestJWT(softwareStatement, "body")
                        .get(CDSValidationConstants.SSA_REDIRECT_URIS);

                return matchRedirectURI(callbackUris, ssaCallbackUris);
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error("Error while resolving validation fields", e);
            return false;
        } catch (ParseException e) {
            log.error("Error while parsing the softwareStatement", e);
            return false;
        }
        return true;
    }

    /**
     * check whether the redirect uris in the request are a subset of the redirect uris in the software statement.
     * assertion
     */
    private boolean matchRedirectURI(List<String> callbackUrisRequest, Object callbackUrisSoftwareStatement) {

        int matchedURis = 0;
        if (callbackUrisSoftwareStatement instanceof List) {
            List callbackUrisSoftwareStatementValues = (List) callbackUrisSoftwareStatement;
            for (String requestURI : callbackUrisRequest) {
                for (Object callbackUrisSoftwareStatementObject : callbackUrisSoftwareStatementValues) {
                    String softwareStatementURI = (String) callbackUrisSoftwareStatementObject;
                    if (requestURI.equals(softwareStatementURI)) {
                        matchedURis = matchedURis + 1;
                    }
                }
            }
        }
        return matchedURis == callbackUrisRequest.size();
    }
}

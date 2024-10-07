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
import org.wso2.openbanking.cds.identity.dcr.validation.annotation.ValidateRedirectUriFormat;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator class to validate Redirect Urls are valid Urls.
 */
public class RedirectUriFormatValidator implements ConstraintValidator<ValidateRedirectUriFormat, Object> {

    private static final Log log = LogFactory.getLog(RedirectUriFormatValidator.class);
    private String registrationRequestPath;
    private String ssaPath;

    @Override
    public void initialize(ValidateRedirectUriFormat validateRedirectUriFormat) {
        this.registrationRequestPath = validateRedirectUriFormat.registrationRequestProperty();
        this.ssaPath = validateRedirectUriFormat.ssa();
    }

    @Override
    public boolean isValid(Object cdsRegistrationRequest, ConstraintValidatorContext constraintValidatorContext) {

        try {
            RegistrationRequest registrationRequest = (RegistrationRequest) new PropertyUtilsBean()
                    .getProperty(cdsRegistrationRequest, registrationRequestPath);
            String softwareStatement = BeanUtils.getProperty(registrationRequest, ssaPath);

            List<String> redirectURIsFromSSA = (List<String>) JWTUtils
                    .decodeRequestJWT(softwareStatement, "body").get(CDSValidationConstants.SSA_REDIRECT_URIS);

            return validateRedirectURIs(redirectURIsFromSSA);

        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error("Error while resolving validation fields", e);
        } catch (ParseException e) {
            log.error("Error while parsing the softwareStatement", e);
        }
        return false;
    }

    /**
     * Check validity and connection of redirect uris.
     *
     * @param redirectURIs redirect uris included in the software statement
     * @return true if the uris are validated
     */
    public static boolean validateRedirectURIs(List<String> redirectURIs) {

        for (String redirectURI : redirectURIs) {
            if (!(redirectURI != null && redirectURI.contains("https"))) {
                return false;
            }
        }
        return true;
    }
}

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.cds.identity.dcr.constants.CDSValidationConstants;
import org.wso2.openbanking.cds.identity.dcr.validation.annotation.ValidateSSAScopes;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


/**
 * Validator class for validating the scopes in software statement.
 */
public class SSAScopesValidator implements ConstraintValidator<ValidateSSAScopes, Object> {

    private static final Log log = LogFactory.getLog(SSAScopesValidator.class);

    @Override
    public boolean isValid(Object scopes, ConstraintValidatorContext constraintValidatorContext) {

        return validateScopes(scopes);
    }

    /**
     * Checks if the scopes contain the mandatory 'cdr:register' and 'openid' scopes.
     *
     * @param scopes scopes included in the software statement
     */
    private boolean validateScopes(Object scopes) {

        boolean containsRegistrationScope = false;
        boolean containsOpenIdScope = false;
        if (scopes instanceof String) {
            String scopeString = (String) scopes;
            for (String scope : scopeString.split(" ")) {
                if (CDSValidationConstants.CDR_REGISTRATION_SCOPE.equals(scope)) {
                    containsRegistrationScope = true;
                } else if (CDSValidationConstants.OPENID.equals(scope)) {
                    containsOpenIdScope = true;
                }
            }
        }
        return containsRegistrationScope && containsOpenIdScope;
    }
}

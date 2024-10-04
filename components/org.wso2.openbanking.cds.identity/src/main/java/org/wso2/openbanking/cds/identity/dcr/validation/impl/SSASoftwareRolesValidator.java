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
import org.wso2.openbanking.cds.identity.dcr.validation.annotation.ValidateSSASoftwareRoles;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator class for validating the software roles in software statement.
 */
public class SSASoftwareRolesValidator implements ConstraintValidator<ValidateSSASoftwareRoles, Object> {

    private static final Log log = LogFactory.getLog(SSASoftwareRolesValidator.class);

    @Override
    public boolean isValid(Object softwareRoles, ConstraintValidatorContext constraintValidatorContext) {

        return CDSValidationConstants.DATA_RECIPIENT_SOFTWARE_PRODUCT.equals(softwareRoles);
    }
}

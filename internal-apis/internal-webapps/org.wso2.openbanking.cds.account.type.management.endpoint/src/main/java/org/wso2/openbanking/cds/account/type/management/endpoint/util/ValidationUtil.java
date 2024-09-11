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
package org.wso2.openbanking.cds.account.type.management.endpoint.util;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 * This class contains the common validation methods used
 * in account type management endpoints.
 */
public class ValidationUtil {

    /**
     * Validate the passed DTO and return the first violation message.
     *
     * @param dto DTO to be validated
     * @return first violation message
     */
    public static String getFirstViolationMessage(Object dto) {

        String firstViolationMessage = "";
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<Object>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            ConstraintViolation<?> firstViolation = violations.iterator().next();
            firstViolationMessage = firstViolation.getMessage().replaceAll("\\.$", "") +
                    ". Error path :" + firstViolation.getPropertyPath();
        }
        return firstViolationMessage;
    }
}

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
package org.wso2.openbanking.cds.identity.dcr.utils;

import com.wso2.openbanking.accelerator.common.validator.OpenBankingValidator;
import com.wso2.openbanking.accelerator.identity.dcr.exception.DCRValidationException;
import com.wso2.openbanking.accelerator.identity.dcr.utils.ValidatorUtils;
import com.wso2.openbanking.accelerator.identity.dcr.validation.validationorder.ValidationOrder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.cds.identity.dcr.constants.CDSValidationConstants;
import org.wso2.openbanking.cds.identity.dcr.model.CDSRegistrationRequest;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Util class for validation logic implementation.
 */
public class ValidationUtils {

    private static final Log log = LogFactory.getLog(ValidationUtils.class);

    public static void validateRequest(CDSRegistrationRequest cdsRegistrationRequest)
            throws DCRValidationException {

        //do SSA claim validations
        String error = OpenBankingValidator.getInstance()
                .getFirstViolation(cdsRegistrationRequest.getSoftwareStatementBody(), ValidationOrder.class);
        if (error != null) {
            String[] errors = error.split(":");
            throw new DCRValidationException(errors[1], errors[0]);
        }
        //do validations related to registration request
        ValidatorUtils.getValidationViolations(cdsRegistrationRequest);

        //remove the unsupported scopes in the software statement
        String requestedScopes = cdsRegistrationRequest.getSoftwareStatementBody().getScopes();
        String filteredScopes = ValidationUtils.filterOnlySupportedScopes(requestedScopes);
        cdsRegistrationRequest.getSoftwareStatementBody().setScopes(filteredScopes);
    }

    /**
     * Removes the unsupported scopes sent by data recipient.
     *
     * @param requestedScopes the scopes requested by the data recipient
     * @return filtered scopes string
     */
    public static String filterOnlySupportedScopes(String requestedScopes) {
        List<String> scopesList = Arrays.asList(requestedScopes.split(" "));
        List<String> filteredScopes = scopesList.stream().filter(CDSValidationConstants.VALID_SSA_SCOPES::contains)
                .collect(Collectors.toList());
        return String.join(" ", filteredScopes);
    }
}

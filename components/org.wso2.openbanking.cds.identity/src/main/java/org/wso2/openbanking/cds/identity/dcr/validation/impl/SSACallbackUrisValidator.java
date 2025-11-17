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
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;
import org.wso2.openbanking.cds.identity.dcr.constants.CDSValidationConstants;
import org.wso2.openbanking.cds.identity.dcr.validation.annotation.ValidateSSACallbackUris;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator class for validating the redirect URIs in software statement.
 */
public class SSACallbackUrisValidator implements ConstraintValidator<ValidateSSACallbackUris, Object> {

    private static final Log log = LogFactory.getLog(SSACallbackUrisValidator.class);

    @Override
    public boolean isValid(Object callbackUris, ConstraintValidatorContext constraintValidatorContext) {

        /* Check whether EnableRedirectURIHostNameValidation configuration is set to true. This will validate
            whether all redirect_uris contain the same hostname. */
        String isHostNameValidationEnabled = (String) OpenBankingCDSConfigParser.getInstance().getConfiguration()
                .get(CDSValidationConstants.DCR_VALIDATE_REDIRECT_URI_HOSTNAME);
        if (Boolean.parseBoolean(isHostNameValidationEnabled)) {
            return validateRedirectURIHostNames(callbackUris);
        }
        return true;
    }

    /**
     * check the hostnames of redirect uris and other uris.
     *
     * @param callbackUrisSoftwareStatement callback uris included in the software statement
     * @return true if the uris are validated
     */
    private boolean validateRedirectURIHostNames(Object callbackUrisSoftwareStatement) {

        try {
            List<String> hostNameList = new ArrayList<>();
            if (callbackUrisSoftwareStatement instanceof List) {
                List callbackUrisSoftwareStatementValues = (List) callbackUrisSoftwareStatement;
                for (Object redirectURIObject : callbackUrisSoftwareStatementValues) {
                    hostNameList.add(new URI((String) redirectURIObject).getHost());
                }
            }
            //if all the redirect uris contain the same hostname, size of the set will be 1
            if ((new HashSet<>(hostNameList)).size() != 1) {
                return false;
            }
        } catch (URISyntaxException e) {
            log.error("Malformed redirect uri", e);
            return false;
        }
        return true;
    }
}

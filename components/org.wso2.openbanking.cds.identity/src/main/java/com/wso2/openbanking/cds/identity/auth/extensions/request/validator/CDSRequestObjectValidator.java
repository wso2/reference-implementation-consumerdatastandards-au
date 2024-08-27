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
package org.wso2.openbanking.cds.identity.auth.extensions.request.validator;

import com.nimbusds.jwt.JWTClaimsSet;
import com.wso2.openbanking.accelerator.common.validator.OpenBankingValidator;
import com.wso2.openbanking.accelerator.identity.auth.extensions.request.validator.OBRequestObjectValidator;
import com.wso2.openbanking.accelerator.identity.auth.extensions.request.validator.models.OBRequestObject;
import com.wso2.openbanking.accelerator.identity.auth.extensions.request.validator.models.ValidationResponse;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth2.RequestObjectException;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;
import org.wso2.openbanking.cds.common.metadata.domain.MetadataValidationResponse;
import org.wso2.openbanking.cds.common.metadata.status.validator.service.MetadataService;
import org.wso2.openbanking.cds.identity.auth.extensions.request.validator.model.CDSRequestObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * The extension class for enforcing CDS Request Object Validations.
 */
public class CDSRequestObjectValidator extends OBRequestObjectValidator {

    private static final Log log = LogFactory.getLog(CDSRequestObjectValidator.class);
    private static final String CDR_ARRANGEMENT_ID = "cdr_arrangement_id";
    private static final String PAR_INITIATED_REQ_OBJ = "par_initiated_request_object";

    @Override
    public ValidationResponse validateOBConstraints(OBRequestObject obRequestObject, Map<String, Object> dataMap) {

        ValidationResponse superValidationResponse = super.validateOBConstraints(obRequestObject, dataMap);

        if (superValidationResponse.isValid()) {
            CDSRequestObject cdsRequestObject = new CDSRequestObject(obRequestObject);
            String violation = validateScope(obRequestObject, dataMap);

            violation = StringUtils.isEmpty(violation) ? validateConsentAmendment(obRequestObject) : violation;

            violation = StringUtils.isEmpty(violation) ? validateMetadata(obRequestObject
                    .getClaimValue("client_id")) : violation;

            violation = StringUtils.isEmpty(violation) ? OpenBankingValidator.getInstance()
                    .getFirstViolation(cdsRequestObject) : violation;

            if (StringUtils.isEmpty(violation)) {
                return new ValidationResponse(true);
            } else {
                return new ValidationResponse(false, violation);
            }
        } else {
            return superValidationResponse;
        }
    }

    private String validateScope(OBRequestObject obRequestObject, Map<String, Object> dataMap) {

        try {
            //remove scope claim
            JWTClaimsSet claimsSet = obRequestObject.getClaimsSet();
            JSONObject claimsSetJsonObject = claimsSet.toJSONObject();
            if (claimsSetJsonObject.containsKey("scope")) {
                String scopeClaimString = claimsSetJsonObject.remove("scope").toString();
                List allowedScopes = (List) dataMap.get("scope");
                List<String> requestedScopes = new ArrayList<>(Arrays.asList(scopeClaimString.split(" ")));
                StringBuilder stringBuilder = new StringBuilder();

                // iterate through requested scopes and remove if not allowed
                for (String scope : requestedScopes) {
                    if (allowedScopes.contains(scope)) {
                        stringBuilder.append(scope).append(" ");
                        if (log.isDebugEnabled()) {
                            log.debug(String.format("Removed scope %s from the request object", scope));
                        }
                    }
                }
                String modifiedScopeString = stringBuilder.toString().trim();
                // throw an error if no valid scopes found or only openid scope is found
                if (StringUtils.isBlank(modifiedScopeString) || modifiedScopeString.split(" ").length <= 1) {
                    throw new RequestObjectException("No valid scopes found in the request");
                }
                claimsSetJsonObject.put("scope", modifiedScopeString);
                //Set claims set to request object
                JWTClaimsSet validatedClaimsSet = JWTClaimsSet.parse(claimsSetJsonObject);
                obRequestObject.setClaimSet(validatedClaimsSet);
                log.debug("Successfully set the modified claims-set to the request object");
            }
        } catch (ParseException | RequestObjectException e) {
            return e.getMessage();
        }
        return StringUtils.EMPTY;
    }

    private String validateMetadata(String clientId) {
        if (OpenBankingCDSConfigParser.getInstance().isMetadataCacheEnabled() && StringUtils.isNotBlank(clientId)) {
            MetadataValidationResponse metadataValidationResp =
                    MetadataService.shouldFacilitateConsentAuthorisation(clientId);

            if (!metadataValidationResp.isValid()) {
                log.error("Metadata cache validation failed, cannot facilitate consent authorisation. Client ID " +
                        clientId + ". Caused by, " + metadataValidationResp.getErrorMessage());
                return metadataValidationResp.getErrorMessage();
            }
        }
        return StringUtils.EMPTY;
    }

    private String validateConsentAmendment(OBRequestObject obRequestObject) {

        // Check for par request if cdr_arrangement_id is available in the request object.
        if (obRequestObject.getRequestedClaims().containsKey(CDR_ARRANGEMENT_ID) &&
                !Boolean.parseBoolean(obRequestObject.getClaimValue(PAR_INITIATED_REQ_OBJ))) {
            String errorMessage = "Request object validation failed.The claim cdr_arrangement_id is only accepted " +
                    "in par initiated requests.";
            log.error(errorMessage);
            return errorMessage;
        }
        return StringUtils.EMPTY;
    }
}

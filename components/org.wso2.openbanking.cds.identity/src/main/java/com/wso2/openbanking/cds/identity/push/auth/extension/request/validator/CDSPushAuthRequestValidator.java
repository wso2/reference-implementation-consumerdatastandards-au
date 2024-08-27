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

package org.wso2.openbanking.cds.identity.push.auth.extension.request.validator;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.accelerator.identity.push.auth.extension.request.validator.PushAuthRequestValidator;
import com.wso2.openbanking.accelerator.identity.push.auth.extension.request.validator.constants.PushAuthRequestConstants;
import com.wso2.openbanking.accelerator.identity.push.auth.extension.request.validator.exception.PushAuthRequestValidatorException;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.wso2.openbanking.cds.identity.utils.CDSIdentityConstants;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

/**
 * The extension class for enforcing CDS Push Auth Request Validations.
 */
public class CDSPushAuthRequestValidator extends PushAuthRequestValidator {

    private static final Log log = LogFactory.getLog(CDSPushAuthRequestValidator.class);
    private static final String CLAIMS = "claims";
    private static final String SHARING_DURATION = "sharing_duration";
    private static final String CDR_ARRANGEMENT_ID = "cdr_arrangement_id";
    private static final String CLIENT_ID = "client_id";
    private final ConsentCoreServiceImpl consentCoreService;
    private static final String CODE_RESPONSE_TYPE = "code";
    private static final String RESPONSE_MODE = "response_mode";
    private static final String JWT_RESPONSE_MODE = "jwt";

    public CDSPushAuthRequestValidator() {
        this.consentCoreService = new ConsentCoreServiceImpl();
    }

    public CDSPushAuthRequestValidator(ConsentCoreServiceImpl consentCoreService) {
        this.consentCoreService = consentCoreService;
    }

    @Override
    public void validateAdditionalParams(Map<String, Object> parameters) throws PushAuthRequestValidatorException {

        JSONObject requestObjectJsonBody;
        if (parameters.containsKey(PushAuthRequestConstants.DECODED_JWT_BODY) &&
                parameters.get(PushAuthRequestConstants.DECODED_JWT_BODY) instanceof JSONObject) {

            requestObjectJsonBody = (JSONObject) parameters.get(PushAuthRequestConstants.DECODED_JWT_BODY);
        } else {
            log.error(CDSIdentityConstants.INVALID_PUSH_AUTH_REQUEST);
            throw new PushAuthRequestValidatorException(HttpStatus.SC_BAD_REQUEST,
                    PushAuthRequestConstants.INVALID_REQUEST_OBJECT, CDSIdentityConstants.INVALID_PUSH_AUTH_REQUEST);
        }

        if (!isValidSharingDuration(requestObjectJsonBody)) {
            log.error(CDSIdentityConstants.INVALID_SHARING_DURATION);
            throw new PushAuthRequestValidatorException(HttpStatus.SC_BAD_REQUEST,
                    PushAuthRequestConstants.INVALID_REQUEST_OBJECT,
                    CDSIdentityConstants.INVALID_SHARING_DURATION);
        }

        validateCDRArrangementId(requestObjectJsonBody);

        // Validate response type - only code response type is allowed
        Object responseType = requestObjectJsonBody.get(PushAuthRequestConstants.RESPONSE_TYPE);
        if (responseType != null && !CODE_RESPONSE_TYPE.equalsIgnoreCase(responseType.toString())) {
            log.error(CDSIdentityConstants.UNSUPPORTED_RESPONSE_TYPE);
            throw new PushAuthRequestValidatorException(HttpStatus.SC_BAD_REQUEST,
                    PushAuthRequestConstants.INVALID_REQUEST, CDSIdentityConstants.UNSUPPORTED_RESPONSE_TYPE);
        }

        // Validate response mode - only jwt response mode is allowed with response type code
        Object responseMode = requestObjectJsonBody.get(RESPONSE_MODE);
        if (responseMode != null && !JWT_RESPONSE_MODE.equalsIgnoreCase(responseMode.toString())) {
            log.error(CDSIdentityConstants.UNSUPPORTED_RESPONSE_MODE);
            throw new PushAuthRequestValidatorException(HttpStatus.SC_BAD_REQUEST,
                    PushAuthRequestConstants.INVALID_REQUEST, CDSIdentityConstants.UNSUPPORTED_RESPONSE_MODE);
        }
    }

    private boolean isValidSharingDuration(JSONObject requestObjectJsonBody) {

        String sharingDurationString = StringUtils.EMPTY;
        JSONObject claims = requestObjectJsonBody.get(CLAIMS) != null ?
                (JSONObject) requestObjectJsonBody.get(CLAIMS) : null;
        if (claims != null && claims.containsKey(SHARING_DURATION)
                && StringUtils.isNotBlank(claims.getAsString(SHARING_DURATION))) {
            sharingDurationString = claims.getAsString(SHARING_DURATION);
        }

        int sharingDuration;
        try {
            sharingDuration = StringUtils.isEmpty(sharingDurationString) ? 0 : Integer.parseInt(sharingDurationString);
        } catch (NumberFormatException e) {
            log.error(String.format("Error while parsing %s value: '%s' to a number.",
                    SHARING_DURATION, sharingDurationString), e);
            return false;
        }

        //If the sharing_duration value is negative then the authorisation should fail.
        return sharingDuration >= 0;
    }

    private void validateCDRArrangementId(JSONObject requestObjectJsonBody) throws PushAuthRequestValidatorException {

        JSONObject claims = requestObjectJsonBody.get(CLAIMS) != null ?
                (JSONObject) requestObjectJsonBody.get(CLAIMS) : null;

        String cdrArrangementId;
        String clientId;
        String errorDescription = "Invalid cdr_arrangement_id";

        if (claims != null && claims.containsKey(CDR_ARRANGEMENT_ID)
                && StringUtils.isNotBlank(claims.getAsString(CDR_ARRANGEMENT_ID))) {

            cdrArrangementId = claims.getAsString(CDR_ARRANGEMENT_ID);
            clientId = requestObjectJsonBody.getAsString(CLIENT_ID);

            DetailedConsentResource detailedConsentResource;
            try {
                detailedConsentResource = consentCoreService.getDetailedConsent(cdrArrangementId);
            } catch (ConsentManagementException e) {
                log.error(errorDescription, e);
                throw new PushAuthRequestValidatorException(HttpStatus.SC_BAD_REQUEST,
                        PushAuthRequestConstants.INVALID_REQUEST_OBJECT,
                        errorDescription);
            }

            if (!detailedConsentResource.getClientID().equals(clientId)) {
                log.error("Unauthorized access to the cdr arrangement");
                throw new PushAuthRequestValidatorException(HttpStatus.SC_BAD_REQUEST,
                        PushAuthRequestConstants.INVALID_REQUEST_OBJECT,
                        errorDescription);
            } else if (!CDSIdentityConstants.AUTHORIZED.equalsIgnoreCase(detailedConsentResource.getCurrentStatus())) {
                errorDescription = "Invalid cdr-arrangement-id or consent is not in Authorised state";
                log.error(errorDescription);
                throw new PushAuthRequestValidatorException(HttpStatus.SC_BAD_REQUEST,
                        PushAuthRequestConstants.INVALID_REQUEST_OBJECT,
                        errorDescription);
            } else if (isConsentExpired(detailedConsentResource)) {
                errorDescription = "Expired cdr-arrangement-id";
                log.error(errorDescription);
                throw new PushAuthRequestValidatorException(HttpStatus.SC_BAD_REQUEST,
                        PushAuthRequestConstants.INVALID_REQUEST_OBJECT,
                        errorDescription);
            }

        } else if (claims != null && claims.containsKey(CDR_ARRANGEMENT_ID)
                && claims.getAsString(CDR_ARRANGEMENT_ID) != null
                && StringUtils.isBlank(claims.getAsString(CDR_ARRANGEMENT_ID))) {
            // A null cdr_arrangement_id should be ignored, Sending an error for empty cdr_arrangement_id.
            log.error("Empty cdr-arrangement-id sent in the request");
            throw new PushAuthRequestValidatorException(HttpStatus.SC_BAD_REQUEST,
                    PushAuthRequestConstants.INVALID_REQUEST_OBJECT,
                    errorDescription);
        }
    }

    /**
     * Method to check if the consent is expired.
     *
     * @param detailedConsentResource consent's AccountSetupResponse
     * @return true if the consent is expired, false otherwise
     */
    private boolean isConsentExpired(DetailedConsentResource detailedConsentResource) {
        long nowInEpochSeconds = OffsetDateTime.now(ZoneOffset.UTC).toEpochSecond();
        long validityDurationInSeconds = detailedConsentResource.getValidityPeriod() - nowInEpochSeconds;

         /* Consents that have expired cannot be amended.
         As a consent with a 0 sharing duration is, in effect, immediately expired then amendment would not be
         possible. */
        return 0 == detailedConsentResource.getValidityPeriod() || validityDurationInSeconds <= 0;
    }
}

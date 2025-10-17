package org.wso2.openbanking.consumerdatastandards.au.extensions.validators.consent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.CDSErrorEnum;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.CommonConstants;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.ErrorConstants;
import org.wso2.openbanking.consumerdatastandards.au.extensions.utils.CDSConsentValidatorUtil;
import org.wso2.openbanking.consumerdatastandards.au.extensions.utils.CommonConsentExtensionUtils;
import org.wso2.openbanking.consumerdatastandards.au.extensions.utils.ErrorUtil;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CDSAccountValidator {

    private static final Log log = LogFactory.getLog(CDSAccountValidator.class);

    public static JSONObject validateConsent(String requestId, Object dataPayload, Object consentResource,
                                             String consentType) throws Exception {

        JSONObject validationResponse = null;

        // Validate the consent
        validationResponse = validateBankingApiSubmission(dataPayload,
                consentResource, requestId);

        return validationResponse;
    }

    public static JSONObject validateBankingApiSubmission(Object dataPayload, Object consentPayload, String requestId)
            throws Exception {

        JSONObject jsonDataRequestBody = CommonConsentExtensionUtils.convertObjectToJson(dataPayload);
        JSONObject jsonConsentRequestBody = CommonConsentExtensionUtils.convertObjectToJson(consentPayload);

        String resourcePath = jsonDataRequestBody.getJSONObject(CommonConstants.RESOURCE_PARAMS)
                .getString(CommonConstants.RESOURCE_PATH);
        String electedResource = jsonDataRequestBody.getString(CommonConstants.ELECTED_RESOURCE);

        // Perform URI Validation.
        if (resourcePath == null || !CDSConsentValidatorUtil.isAccountURIValid(electedResource)) {

            log.error(ErrorConstants.PAYLOAD_FORMAT_ERROR);
            return ErrorUtil.createErrorResponse(CDSErrorEnum.UNEXPECTED_FIELD).toJSONObject();
        }

        //Retrieve Consent Payload
        JSONObject receiptJson = jsonConsentRequestBody.getJSONObject(CommonConstants.RECEIPT);

        //Check whether required permissions provided.
        JSONArray permissions = (JSONArray) ((JSONObject) receiptJson.get(CommonConstants.DATA))
                .get(CommonConstants.PERMISSIONS);

//        if (!UKConsentValidatorUtil.validateAccountPermissions(electedResource, permissions)) {
//
//            log.error(ErrorConstants.PERMISSION_MISMATCH_ERROR);
//            return CommonConsentExtensionUtils.getErrorResponse(CommonConstants.BAD_REQUEST,
//                    ErrorConstants.FIELD_INVALID, ErrorConstants.PERMISSION_MISMATCH_ERROR);
//        }
//
//        //Check whether the consent is expired.
//        if (UKConsentValidatorUtil.isConsentExpired((String) ((JSONObject) receiptJson.get(CommonConstants.DATA))
//                .get(CommonConstants.EXPIRATION_DATE))) {
//
//            log.error(ErrorConstants.CONSENT_EXPIRED_ERROR);
//            return CommonConsentExtensionUtils.getErrorResponse(CommonConstants.BAD_REQUEST,
//                    ErrorConstants.FIELD_INVALID, ErrorConstants.CONSENT_EXPIRED_ERROR);
//        }
//
//        //Consent Status Validation
//        String consentStatus = jsonConsentRequestBody.getString(CommonConstants.STATUS.toLowerCase(Locale.ROOT));
//
//        if (!CommonConstants.AUTHORIZED_STATUS.equalsIgnoreCase(consentStatus)) {
//
//            log.error(ErrorConstants.ACCOUNT_CONSENT_STATE_INVALID);
//            return CommonConsentExtensionUtils.getErrorResponse(CommonConstants.BAD_REQUEST,
//                    ErrorConstants.RESOURCE_INVALID_CONSENT_STATUS, ErrorConstants.ACCOUNT_CONSENT_STATE_INVALID);
//        }
//
//        //Account ID Validation
//        String isAccountIdValidationEnabled = ConfigurableProperties.VALIDATE_ACCOUNT_ID;
//
//        if (Boolean.parseBoolean(isAccountIdValidationEnabled) &&
//                !UKConsentValidatorUtil.isAccountIdValid(jsonConsentRequestBody, resourcePath)) {
//
//            log.error(ErrorConstants.ACCOUNT_ID_NOT_AVAILABLE_MSG);
//            return CommonConsentExtensionUtils.getErrorResponse(CommonConstants.BAD_REQUEST,
//                    ErrorConstants.FIELD_INVALID, ErrorConstants.ACCOUNT_ID_NOT_AVAILABLE_MSG);
//        }
//
//        // Perform Query Param Validation.
//        Map<String, String> resourceParams = new HashMap<>();
//        resourceParams = CommonConsentExtensionUtils.extractQueryParams(jsonDataRequestBody);
//
//        JSONObject queryParamValidity = UKConsentValidatorUtil.checkTransactionTimePeriodValidity(resourcePath,
//                receiptJson, resourceParams, requestId);
//
//        if (queryParamValidity != null && queryParamValidity.toString().contains(CommonConstants.ERROR)) {
//            return queryParamValidity;
//        }
//
//        //Validating 90 days re-authentication removal for account refresh tokens with 3.1.10 update.
//        Integer lastAuthorizeDate = jsonConsentRequestBody.getInt(CommonConstants.UPDATED_TIME);
//
//        if (CommonConsentExtensionUtils.isLastAuthorizedDateOutOfLimit(lastAuthorizeDate) &&
//                CommonConsentExtensionUtils.isFromDateOutOfDateLimit(resourceParams)) {
//
//            log.error(ErrorConstants.OLDER_REFRESH_TOKEN);
//            return CommonConsentExtensionUtils.getErrorResponse(CommonConstants.BAD_REQUEST,
//                    ErrorConstants.INVALID_QUERY_PARAMS, ErrorConstants.OLDER_REFRESH_TOKEN);
//        }

        return CommonConsentExtensionUtils.getSuccessResponse(requestId);
    }


}

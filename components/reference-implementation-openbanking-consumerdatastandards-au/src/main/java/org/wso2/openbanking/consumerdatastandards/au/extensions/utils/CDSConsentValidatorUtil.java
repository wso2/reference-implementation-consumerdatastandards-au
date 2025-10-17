/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.openbanking.consumerdatastandards.au.extensions.utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.CommonConstants;

import java.util.Arrays;
import java.util.List;

/**
 * Util class for CDS Consent Validator.
 */
public class CDSConsentValidatorUtil {

    private static final Log log = LogFactory.getLog(CDSConsentValidatorUtil.class);


    /**
     * Util method to validate the Account request URI.
     *
     * @param uri  Request URI
     * @return
     */
    public static boolean isAccountURIValid(String uri) {
        List<String> accountPaths = getAccountAPIPathRegexArray();
        boolean isValid = false;

        for (String entry : accountPaths) {
            if (uri.contains(entry)) {
                isValid = true;
                break;
            }
        }

        return isValid;
    }

    /**
     * Method provides API resource paths applicable for UK Account API.
     *
     * @return map of API Resources.
     */
    public static List<String> getAccountAPIPathRegexArray() {

//        List<String> requestUrls = Arrays.asList(CommonConstants.ACCOUNT_REGEX,
//                CommonConstants.BALANCES_REGEX,
//                CommonConstants.TRANSACTIONS_REGEX,
//                CommonConstants.BENEFICIARY_REGEX,
//                CommonConstants.DIRECT_DEBITS_REGEX,
//                CommonConstants.OFFERS_REGEX,
//                CommonConstants.PARTY_REGEX,
//                CommonConstants.PRODUCT_REGEX,
//                CommonConstants.SCHEDULED_PAYMENTS_REGEX,
//                CommonConstants.STANDING_ORDER_REGEX,
//                CommonConstants.STATEMENTS_REGEX,
//                CommonConstants.ACCOUNT_ID_REGEX,
//                CommonConstants.BALANCES_ID_REGEX,
//                CommonConstants.TRANSACTIONS_ID_REGEX,
//                CommonConstants.BENEFICIARY_ID_REGEX,
//                CommonConstants.DIRECT_DEBITS_ID_REGEX,
//                CommonConstants.OFFERS_ID_REGEX,
//                CommonConstants.PARTIES_ID_REGEX,
//                CommonConstants.PARTY_ID_REGEX,
//                CommonConstants.PRODUCT_ID_REGEX,
//                CommonConstants.SCHEDULED_PAYMENTS_ID_REGEX,
//                CommonConstants.STANDING_ORDER_ID_REGEX,
//                CommonConstants.ACCOUNT_ID_STATEMENTS_REGEX,
//                CommonConstants.STATEMENTS_ID_REGEX,
//                CommonConstants.STATEMENTS_FILE_REGEX,
//                CommonConstants.STATEMENTS_TRANSACTIONS_REGEX);

        List<String> requestUrls = Arrays.asList(CommonConstants.ACCOUNTS);

        return requestUrls;
    }

//    /**
//     * Validate Account Permissions.
//     *
//     * @param requestedURL    The URL of the request
//     * @param permissions     The permissions of the request
//     * @return Status of the validation
//     */
//    public static boolean validateAccountPermissions(String requestedURL, JSONArray permissions) {
//
//        Set<String> permissionSet = new HashSet<>();
//        for (int i = 0; i < permissions.length(); i++) {
//            permissionSet.add(permissions.getString(i));
//        }
//
//        if (requestedURL != null) {
//
//            if (requestedURL.matches(CommonConstants.ACCOUNT_REGEX) ||
//                    requestedURL.matches(CommonConstants.ACCOUNT_ID_REGEX)) {
//
//                return permissionSet.contains(CommonConstants.READACCOUNTSBASIC) ||
//                        permissionSet.contains(CommonConstants.READACCOUNTSDETAIL);
//            } else if (requestedURL.matches(CommonConstants.BALANCES_REGEX) ||
//                    requestedURL.matches(CommonConstants.BALANCES_ID_REGEX)) {
//                return permissionSet.contains(CommonConstants.READBALANCES);
//            } else if (requestedURL.matches(CommonConstants.TRANSACTIONS_REGEX) ||
//                    requestedURL.matches(CommonConstants.TRANSACTIONS_ID_REGEX) ||
//                    requestedURL.matches(CommonConstants.STATEMENTS_TRANSACTIONS_REGEX)) {
//                return (permissionSet.contains(CommonConstants.READTRANSACTIONSBASIC) ||
//                        permissionSet.contains(CommonConstants.READTRANSACTIONSDETAIL)) &&
//                        (permissionSet.contains(CommonConstants.READTRANSACTIONSCREDITS) ||
//                                permissionSet.contains(CommonConstants.READTRANSACTIONSDEBITS));
//            } else if (requestedURL.matches(CommonConstants.BENEFICIARY_REGEX) ||
//                    requestedURL.matches(CommonConstants.BENEFICIARY_ID_REGEX)) {
//                return permissionSet.contains(CommonConstants.READBENEFICIARIESBASIC) ||
//                        permissionSet.contains(CommonConstants.READBENEFICIARIESDETAIL);
//            } else if (requestedURL.matches(CommonConstants.DIRECT_DEBITS_REGEX) ||
//                    requestedURL.matches(CommonConstants.DIRECT_DEBITS_ID_REGEX)) {
//                return permissionSet.contains(CommonConstants.READDIRECTDEBITS);
//            } else if (requestedURL.matches(CommonConstants.OFFERS_REGEX) ||
//                    requestedURL.matches(CommonConstants.OFFERS_ID_REGEX)) {
//                return permissionSet.contains(CommonConstants.READOFFERS);
//            } else if (requestedURL.matches(CommonConstants.PARTY_REGEX)) {
//                return permissionSet.contains(CommonConstants.READPARTYPSU);
//            } else if (requestedURL.matches(CommonConstants.PARTIES_ID_REGEX) ||
//                    requestedURL.matches(CommonConstants.PARTY_ID_REGEX)) {
//                return permissionSet.contains(CommonConstants.READPARTY);
//            } else if (requestedURL.matches(CommonConstants.PRODUCT_REGEX) ||
//                    requestedURL.matches(CommonConstants.PRODUCT_ID_REGEX)) {
//                return permissionSet.contains(CommonConstants.READPRODUCTS);
//            } else if (requestedURL.contains(CommonConstants.SCHEDULED_PAYMENTS_REGEX) ||
//                    requestedURL.matches(CommonConstants.SCHEDULED_PAYMENTS_ID_REGEX)) {
//                return permissionSet.contains(CommonConstants.READSCHEDULEDPAYMENTSBASIC) ||
//                        permissionSet.contains(CommonConstants.READSCHEDULEDPAYMENTSDETAIL);
//            } else if (requestedURL.matches(CommonConstants.STANDING_ORDER_REGEX) ||
//                    requestedURL.matches(CommonConstants.STANDING_ORDER_ID_REGEX)) {
//                return permissionSet.contains(CommonConstants.READSTANDINGORDERSBASIC) ||
//                        permissionSet.contains(CommonConstants.READSTANDINGORDERSDETAIL);
//            } else if (requestedURL.matches(CommonConstants.STATEMENTS_REGEX) ||
//                    requestedURL.matches(CommonConstants.ACCOUNT_ID_STATEMENTS_REGEX) ||
//                    requestedURL.matches(CommonConstants.STATEMENTS_ID_REGEX)) {
//                return permissionSet.contains(CommonConstants.READSTATEMENTSBASIC) ||
//                        permissionSet.contains(CommonConstants.READSTATEMENTSDETAIL);
//            } else if (requestedURL.matches(CommonConstants.STATEMENTS_FILE_REGEX)) {
//                return permissionSet.contains(CommonConstants.READSTATEMENTSDETAIL);
//            }
//
//        }
//        if (log.isDebugEnabled()) {
//            log.debug("Validation of permissions failed for resource: " + requestedURL
//                    + "\nPermissions: " + permissions.toString());
//        }
//        return false;
//    }
//
//    /**
//     * Validate whether consent is expired.
//     *
//     * @param expDateVal     Expiration Date Time
//     * @return
//     */
//    public static boolean isConsentExpired(String expDateVal) {
//
//        if (expDateVal != null && !expDateVal.isEmpty()) {
//            OffsetDateTime expDate = OffsetDateTime.parse(expDateVal);
//            return OffsetDateTime.now().isAfter(expDate);
//        } else {
//            return false;
//        }
//    }
//
//    /**
//     * Method to extract query parameters from the submission request body.
//     * @param jsonSubmissionRequestBody
//     * @return
//     */
//    public static Map<String, String> extractQueryParams(JSONObject jsonSubmissionRequestBody){
//
//        JSONObject resourceParams = jsonSubmissionRequestBody.getJSONObject(CommonConstants.RESOURCE_PARAMS);
//
//        // Initialize Map to store resourceParams values
//        Map<String, String> resourceParamsMap = new HashMap<>();
//
//        // Dynamically add key-value pairs to the map
//        Iterator<String> keys = resourceParams.keys();
//        while (keys.hasNext()) {
//            String key = keys.next();
//            String value = resourceParams.getString(key);
//            resourceParamsMap.put(key, value);
//        }
//        return resourceParamsMap;
//    }
//
//    /**
//     * Check whether the time period of the transaction is valid.
//     * @param resourcePath              URI of the request
//     * @param jsonRequestBody      Initiation object
//     * @param queryParams      Query Parameters
//     * @return is transaction time period valid
//     */
//    public static JSONObject checkTransactionTimePeriodValidity(String resourcePath, JSONObject jsonRequestBody,
//                                                                Map<String, String> queryParams, String requestId) throws Exception {
//
//        LocalDateTime fromDateTime = null;
//        LocalDateTime toDateTime = null;
//        LocalDateTime transactionFromDateTime = null;
//        LocalDateTime transactionToDateTime = null;
//        boolean isFromDateValid = true;
//        boolean isToDateValid = true;
//
//        try {
//            if (resourcePath.contains(CommonConstants.TRANSACTIONS)) {
//
//                fromDateTime = extractURIDateParameter(queryParams
//                        .get(CommonConstants.FROM_BOOKING_DATE_TIME));
//                toDateTime = extractURIDateParameter(queryParams
//                        .get(CommonConstants.TO_BOOKING_DATE_TIME));
//
//            } else if (resourcePath.contains(CommonConstants.STATEMENTS)) {
//                fromDateTime = extractURIDateParameter(queryParams
//                        .get(CommonConstants.FROM_STATEMENT_DATE_TIME));
//                toDateTime = extractURIDateParameter(queryParams
//                        .get(CommonConstants.TO_STATEMENT_DATE_TIME));
//            }
//
//            String transactionFromDateFromRequest = ((JSONObject) jsonRequestBody.get(CommonConstants.DATA))
//                    .getString(CommonConstants.TRANSACTION_FROM_DATE);
//            String transactionToDateFromRequest = ((JSONObject) jsonRequestBody.get(CommonConstants.DATA))
//                    .getString(CommonConstants.TRANSACTION_TO_DATE);
//
//            if (transactionFromDateFromRequest != null) {
//                transactionFromDateTime = LocalDateTime.parse(transactionFromDateFromRequest,
//                        DateTimeFormatter.ISO_DATE_TIME);
//            }
//
//            if (transactionToDateFromRequest != null) {
//                transactionToDateTime =  LocalDateTime.parse(transactionToDateFromRequest,
//                        DateTimeFormatter.ISO_DATE_TIME);
//            }
//
//        } catch (DateTimeParseException e) {
//            log.error(ErrorConstants.WRONG_DATE_FORMAT_QUERY);
//            return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                    ErrorConstants.FIELD_INVALID_DATE,
//                    ErrorConstants.WRONG_DATE_FORMAT_QUERY);
//        }
//
//        if ((transactionFromDateTime != null) || (transactionToDateTime != null)) {
//            if (fromDateTime != null) {
//                if (transactionFromDateTime != null &&
//                        fromDateTime.compareTo(transactionFromDateTime) < 0) {
//                    isFromDateValid = false;
//                }
//                if (transactionToDateTime != null && fromDateTime.compareTo(transactionToDateTime) > 0) {
//                    isFromDateValid = false;
//                }
//            }
//            if (toDateTime != null) {
//                if (transactionToDateTime != null && toDateTime.compareTo(transactionToDateTime) > 0) {
//                    isToDateValid = false;
//                }
//                if (transactionFromDateTime != null && toDateTime.compareTo(transactionFromDateTime) < 0) {
//                    isToDateValid = false;
//                }
//            }
//
//            if (!(isFromDateValid && isToDateValid)) {
//                log.error(ErrorConstants.INVALID_QUERY_PARAMS);
//                return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                        ErrorConstants.FIELD_INVALID_DATE,
//                        ErrorConstants.INVALID_QUERY_PARAMS);
//            }
//        }
//        return CommonConsentValidationUtil.getSuccessResponse(requestId);
//    }
//
//    /**
//     * Extracts date and time from a URL.
//     *
//     * @param queryParam    Query Param string
//     * @return
//     * @throws DateTimeParseException
//     * @throws java.io.UnsupportedEncodingException
//     */
//    public static LocalDateTime extractURIDateParameter(String queryParam)
//            throws DateTimeParseException {
//
//        LocalDateTime dateTime = null;
//        if (queryParam != null) {
//            URI uri = URI.create(queryParam.replaceAll(":", "%3A"));
//            String decodedRequestedDateTime = uri.getPath();
//            if (decodedRequestedDateTime != null) {
//                if (decodedRequestedDateTime.charAt(4) == '-') {
//                    dateTime = LocalDateTime.parse(decodedRequestedDateTime,
//                            DateTimeFormatterConstants.EXTENDED_ISO_DATE_TIME);
//                } else {
//                    dateTime = LocalDateTime.parse(decodedRequestedDateTime,
//                            DateTimeFormatterConstants.BASIC_ISO_DATE_TIME);
//                }
//            }
//        }
//
//        return dateTime;
//    }
//
//    /**
//     * Validates if the LastAuthorizedDate for an account consent is beyond the configured Date Limit.
//     * @param  lastAuthorizedDate seconds value of the last authorized date
//     **/
//    public static boolean isLastAuthorizedDateOutOfLimit(long lastAuthorizedDate) {
//
//        String zoneId = ConfigurableProperties.ZONE_ID;
//        LocalDate lastAuthorizeDate = Instant.ofEpochSecond(lastAuthorizedDate).
//                atZone(ZoneId.of(zoneId)).toLocalDate();
//
//        return ChronoUnit.DAYS.between(Date.valueOf(lastAuthorizeDate).toLocalDate(), LocalDate.now()) >
//                Integer.parseInt(ConfigurableProperties.ACCOUNT_REFRESH_TOKEN_LAST_AUTHORIZED_DATE_LIMIT);
//    }
//
//    /**
//     * Validates if the query param fromBookingDateTime for an account consent is beyond the configured Date Limit.
//     * @params resourceParams requested query parameters
//     **/
//    public static boolean isFromDateOutOfDateLimit(Map<String, String> resourceParams) {
//
//        if (resourceParams.containsKey(CommonConstants.FROM_BOOKING_DATE_TIME)) {
//
//            LocalDateTime fromBookingDate = null;
//            URI uri = URI.create(resourceParams.get(CommonConstants.FROM_BOOKING_DATE_TIME)
//                    .replaceAll(":", "%3A"));
//            String decodedRequestedDateTime = uri.getPath();
//
//            if (decodedRequestedDateTime != null) {
//                if (decodedRequestedDateTime.charAt(4) == '-') {
//
//                    fromBookingDate = LocalDateTime.parse(decodedRequestedDateTime,
//                            DateTimeFormatterConstants.EXTENDED_ISO_DATE_TIME);
//                } else {
//
//                    fromBookingDate = LocalDateTime.parse(decodedRequestedDateTime,
//                            DateTimeFormatterConstants.BASIC_ISO_DATE_TIME);
//                }
//                return ChronoUnit.DAYS.between(fromBookingDate, LocalDateTime.now()) >
//                        Integer.parseInt(ConfigurableProperties.ACCOUNT_REFRESH_TOKEN_LAST_AUTHORIZED_DATE_LIMIT);
//            }
//            return true;
//        } else {
//            //return false when there is no fromBookingDateTime in the query params.
//            return false;
//        }
//    }
//
//    /**
//     * Validates whether Cutoffdatetime is enabled, if the request is arriving past the cut off time and if it
//     * should be rejected by policy.
//     *
//     * @return if the request should be rejected, or not.
//     */
//    public static boolean shouldSubmissionRequestBeRejected() {
//
//        if (Boolean.parseBoolean(ConfigurableProperties.CUTOFF_DATE_ENABLED) && hasCutOffTimeElapsed() && CommonConstants.REJECT
//                .equals(ConfigurableProperties.CUTOFF_DATE_POLICY)) {
//            if (log.isDebugEnabled()) {
//                log.debug("Request Rejected as CutOffDateTime has elapsed.");
//            }
//            return true;
//        }
//        return false;
//    }
//
//    /**
//     * Validates whether the CutOffTime for the day has elapsed.
//     *
//     * @return has elapsed
//     */
//    public static boolean hasCutOffTimeElapsed() {
//
//        OffsetTime dailyCutOffTime = OffsetTime.parse(ConfigurableProperties.DAILY_CUTOFF);
//        OffsetTime currentTime = LocalTime.now().atOffset(dailyCutOffTime.getOffset());
//        if (log.isDebugEnabled()) {
//            log.debug("Request received at" + currentTime + " daily cut off time set to " + dailyCutOffTime);
//        }
//        return currentTime.isAfter(dailyCutOffTime);
//    }
//
//    /**
//     * Convert long date values to ISO 8601 format.
//     * @param dateValue
//     * @return
//     */
//    public static String convertToISO8601(long dateValue) {
//
//        DateFormat simple = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
//        java.util.Date simpleDateVal = new java.util.Date(dateValue * 1000);
//        return simple.format(simpleDateVal);
//    }
//
//    /**
//     * Method to validate whether account id is valid.
//     * @param jsonConsentRequestBody
//     * @param resourcePath
//     * @return
//     * @throws Exception
//     */
//    public static boolean isAccountIdValid(JSONObject jsonConsentRequestBody, String resourcePath){
//
//        JSONArray authorizations = jsonConsentRequestBody
//                .getJSONArray(CommonConstants.AUTHORIZATIONS_KEY);
//        JSONObject authorisationResource  = authorizations.getJSONObject(0);
//
//        JSONArray consentMappingResources = authorisationResource.getJSONArray(CommonConstants.RESOURCES);
//
//        String accountID = consentMappingResources.getJSONObject(0).getString(CommonConstants.ACCOUNT_ID);
//
//
//        // If no valid account IDs exist, return false
//        if (accountID.isEmpty()) {
//            return false;
//        }
//
//        // Check if /accounts/{accountID} is present
//        Pattern pattern = Pattern.compile("/accounts/\\d+");
//        Matcher matcher = pattern.matcher(resourcePath);
//
//        if (!matcher.find()) {
//            return true;
//        }
//
//        // If resourcePath does not contain "{AccountId}", return true
//        if (resourcePath.contains("/accounts/"+accountID)) {
//            return true;
//        }
//
//        // Check if resourcePath contains any valid account ID
//        if (resourcePath.contains(accountID)) {
//            return true;
//        }
//        return false;
//    }
//
//    /**
//     * Validates whether Cutoffdatetime is enabled, if the request is arriving past the cut off date and if it
//     * should be rejected by policy.
//     *
//     * @return if the request should be rejected, or not.
//     */
//    public static boolean shouldSubmissionRequestBeRejected(String timeStamp) {
//
//        String isCutOffDateEnabled = ConfigurableProperties.CUTOFF_DATE_ENABLED;
//        String cutOffDatePolicy = ConfigurableProperties.CUTOFF_DATE_POLICY;
//
//        if (Boolean.parseBoolean(isCutOffDateEnabled) && CommonConstants.REJECT.equals(cutOffDatePolicy)) {
//            if (hasCutOffTimeElapsed()) {
//                if (log.isDebugEnabled()) {
//                    log.debug("Request Rejected as CutOffTime has elapsed.");
//                }
//                return true;
//            }
//            if (hasCutOffDateElapsed(timeStamp)) {
//                if (log.isDebugEnabled()) {
//                    log.debug("Request Rejected as CutOffDate has elapsed.");
//                }
//                return true;
//            }
//        }
//        return false;
//    }
//
//    /**
//     * Validates whether the cutOffDate and the initiation date are the same.
//     *
//     * @return if the request should be rejected, or not.
//     */
//    private static boolean hasCutOffDateElapsed(String initiationTimestamp) {
//
//        OffsetDateTime initiationDateTime = OffsetDateTime.parse(initiationTimestamp);
//        OffsetDateTime currentDateTime = OffsetDateTime.parse(getCurrentCutOffDateTime());
//        return initiationDateTime.getMonth() != currentDateTime.getMonth() ||
//                initiationDateTime.getDayOfMonth() != currentDateTime.getDayOfMonth();
//    }
//
//    /**
//     * Returns the CutOffDateTime from the CutOffTime.
//     *
//     * @return CutOffDateTime value for the day
//     */
//    public static String getCurrentCutOffDateTime() {
//
//        return LocalDate.now() + "T" + (ConfigurableProperties.DAILY_CUTOFF);
//    }
//
//    /**
//     * Method to filter active consent mappings from consentValidateData.
//     * @param jsonSubmissionRequestBody
//     * @return
//     * @throws Exception
//     */
//    public static List<String> filterActiveConsentMappings(JSONObject jsonSubmissionRequestBody) throws Exception {
//
//        JSONObject consentMappingResources = jsonSubmissionRequestBody
//                .getJSONObject(CommonConstants.CONSENT_MAPPING_RESOURCES_KEY);
//
//        // Extract account IDs from active mappings
//        List<String> activeAccountIds = new ArrayList<>();
//        JSONArray accountIdArray = consentMappingResources.getJSONArray(CommonConstants.ACCOUNT_ID);
//
//        JSONArray activeMappings = new JSONArray();
//
//        for (int i = 0; i < accountIdArray.length(); i++) {
//            JSONObject mapping = accountIdArray.getJSONObject(i);
//            if (mapping != null) {
//
//                String status = mapping.getString(CommonConstants.MAPPING_STATUS).trim();
//
//                if (CommonConstants.ACTIVE_MAPPING_STATUS.equals(status)) {
//                    // Add active account IDs
//                    String accountId = mapping.getString(CommonConstants.ACCOUNT_ID).trim();
//                    if (!accountId.isEmpty()) {
//                        activeAccountIds.add(accountId);
//                    }
//                    activeMappings.put(mapping); // Store active mappings
//                }
//            }
//        }
//
//        // remove inactive consent mapping resources from consentValidateData
//        jsonSubmissionRequestBody.put(CommonConstants.CONSENT_MAPPING_RESOURCES_KEY, activeMappings);
//
//        return activeAccountIds;
//    }
//
//    /**
//     * Utility method to validate mandatory parameters.
//     *
//     * @param str1   First String to validate
//     * @param str2   Second String to validate
//     * @return
//     */
//    public static  boolean compareMandatoryParameter(String str1, String str2) {
//
//        if ((str1 == null) || (str2 == null)) {
//            return false;
//        } else {
//            return str1.equals(str2);
//        }
//    }
//
//    /**
//     * Utility method to validate optional parameters.
//     *
//     * @param field        Field to validate
//     * @param submission   submisison object
//     * @param consent      initiation object
//     * @return
//     */
//    public static boolean compareOptionalParameter(String field, JSONObject submission, JSONObject consent) {
//
//        boolean isStr1Empty = !submission.has(field) || submission.getString(field) == null || submission.getString(field).isEmpty();
//        boolean isStr2Empty = !consent.has(field) || (consent.getString(field) == null || consent.getString(field).isEmpty());
//
//        if (!(isStr1Empty || isStr2Empty)) {
//            return submission.getString(field).equals(consent.getString(field));
//        } else {
//            return (isStr1Empty && isStr2Empty);
//        }
//    }
//
//    /**
//     * Method to validate Remittance Information.
//     *
//     * @param remittanceInformationSubmission  Remittance Information in Submission Request
//     * @param remittanceInformationInitiation Remittance Information in Initiation Request
//     * @param invokedAPIVersion
//     * @return validation result object
//     */
//    public static JSONObject validateRemittanceInfo(JSONObject remittanceInformationSubmission,
//                                                    JSONObject remittanceInformationInitiation,
//                                                    CommonConstants.UK_API_VERSION invokedAPIVersion, String requestId) {
//
//        if ((!remittanceInformationSubmission.has(CommonConstants.REFERENCE)
//                && remittanceInformationInitiation.has(CommonConstants.REFERENCE)) ||
//                (remittanceInformationSubmission.has(CommonConstants.REFERENCE)
//                        && !remittanceInformationInitiation.has(CommonConstants.REFERENCE))) {
//
//            return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                    ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                    ErrorConstants.REMMITANCE_REFERENCE_NOT_FOUND);
//
//        } else if (remittanceInformationSubmission.has(CommonConstants.REFERENCE)
//                && remittanceInformationInitiation.has(CommonConstants.REFERENCE)) {
//            if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.REFERENCE,
//                    remittanceInformationSubmission, remittanceInformationInitiation)) {
//
//                return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                        ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                        ErrorConstants.REMMITANCE_REFERENCE_MISMATCH);
//            }
//        }
//
//        if (CommonConstants.UK_API_VERSION.UK_API_V400.equals(invokedAPIVersion)) {
//
//            if ((!remittanceInformationSubmission.has(CommonConstants.UNSTRUCTURED)
//                    && remittanceInformationInitiation.has(CommonConstants.UNSTRUCTURED)) ||
//                    (remittanceInformationSubmission.has(CommonConstants.UNSTRUCTURED)
//                            && !remittanceInformationInitiation.has(CommonConstants.UNSTRUCTURED))) {
//
//                return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                        ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                        ErrorConstants.REMMITANCE_UNSTRUCTURED_NOT_FOUND);
//
//            } else if (remittanceInformationSubmission.has(CommonConstants.UNSTRUCTURED)
//                    && remittanceInformationInitiation.has(CommonConstants.UNSTRUCTURED)) {
//
//                JSONArray remittanceInformationUnstructuredSub = (JSONArray) remittanceInformationSubmission
//                        .get(CommonConstants.UNSTRUCTURED);
//                JSONArray remittanceInformationUnstructuredInit = (JSONArray) remittanceInformationInitiation
//                        .get(CommonConstants.UNSTRUCTURED);
//
//                if (!remittanceInformationUnstructuredSub.equals(remittanceInformationUnstructuredInit)) {
//
//                    return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                            ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                            ErrorConstants.REMMITANCE_UNSTRUCTURED_MISMATCH);
//                }
//            }
//        } else {
//
//            if ((!remittanceInformationSubmission.has(CommonConstants.UNSTRUCTURED)
//                    && remittanceInformationInitiation.has(CommonConstants.UNSTRUCTURED)) ||
//                    (remittanceInformationSubmission.has(CommonConstants.UNSTRUCTURED)
//                            && !remittanceInformationInitiation.has(CommonConstants.UNSTRUCTURED))) {
//
//                return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                        ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                        ErrorConstants.REMMITANCE_UNSTRUCTURED_NOT_FOUND);
//
//            } else if (remittanceInformationSubmission.has(CommonConstants.UNSTRUCTURED)
//                    && remittanceInformationInitiation.has(CommonConstants.UNSTRUCTURED)) {
//                if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.UNSTRUCTURED,
//                        remittanceInformationSubmission, remittanceInformationInitiation)) {
//
//                    return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                            ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                            ErrorConstants.REMMITANCE_UNSTRUCTURED_MISMATCH);
//                }
//            }
//        }
//
//        return CommonConsentValidationUtil.getSuccessResponse(requestId);
//    }
//
//    public static JSONObject validateInstructionIdentification(String requestPath, JSONObject submissionInitiation,
//                                                               JSONObject consentInitiation, String requestId) {
//
//        if (!requestPath.contains("standing-orders") && !requestPath.contains(
//                CommonConstants.DOMESTIC_VRP_PAYMENT_PATH)) {
//
//            if (submissionInitiation.has(CommonConstants.INSTRUCTION_IDENTIFICATION)) {
//
//                if (StringUtils.isEmpty(submissionInitiation.getString(CommonConstants.INSTRUCTION_IDENTIFICATION))
//                        || !UKConsentValidatorUtil.compareMandatoryParameter(
//                        submissionInitiation.getString(CommonConstants.INSTRUCTION_IDENTIFICATION),
//                        consentInitiation.getString(CommonConstants.INSTRUCTION_IDENTIFICATION))) {
//
//                    return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                            ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                            ErrorConstants.INSTRUCTION_IDENTIFICATION_MISMATCH);
//                }
//            } else {
//                return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                        ErrorConstants.FIELD_MISSING,
//                        ErrorConstants.INSTRUCTION_IDENTIFICATION_NOT_FOUND);
//            }
//
//            return CommonConsentValidationUtil.getSuccessResponse(requestId);
//        }
//        return CommonConsentValidationUtil.getSuccessResponse(requestId);
//    }
//
//    /**
//     * Method to validate EndToEndIdentification.
//     * @param requestPath
//     * @param submissionInitiation
//     * @param consentInitiation
//     * @param requestId
//     * @return
//     */
//    public static JSONObject validateEndToEndIdentification(String requestPath, JSONObject submissionInitiation,
//                                                            JSONObject consentInitiation, String requestId) {
//
//        if (requestPath.contains("domestic-payments") || requestPath.contains("international-payments")) {
//
//            if (submissionInitiation.has(CommonConstants.END_TO_END_IDENTIFICATION)) {
//
//                if (StringUtils.isEmpty(submissionInitiation.getString(CommonConstants.END_TO_END_IDENTIFICATION))
//                        || !UKConsentValidatorUtil.compareMandatoryParameter(
//                        submissionInitiation.getString(CommonConstants.END_TO_END_IDENTIFICATION),
//                        consentInitiation.getString(CommonConstants.END_TO_END_IDENTIFICATION))) {
//
//                        return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                                ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                                ErrorConstants.END_TO_END_IDENTIFICATION_MISMATCH);
//                }
//            } else {
//                return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                        ErrorConstants.FIELD_MISSING,
//                        ErrorConstants.END_TO_END_IDENTIFICATION_NOT_FOUND);
//            }
//        } else if (requestPath.contains("scheduled-payments")) {
//            if (submissionInitiation.has(CommonConstants.END_TO_END_IDENTIFICATION) &&
//                    consentInitiation.has(CommonConstants.END_TO_END_IDENTIFICATION) &&
//                    !UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.END_TO_END_IDENTIFICATION,
//                            submissionInitiation, consentInitiation)) {
//
//                return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                        ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                        ErrorConstants.END_TO_END_IDENTIFICATION_MISMATCH);
//
//            } else if ((submissionInitiation.has(CommonConstants.END_TO_END_IDENTIFICATION) &&
//                    !consentInitiation.has(CommonConstants.END_TO_END_IDENTIFICATION)) ||
//                    (!submissionInitiation.has(CommonConstants.END_TO_END_IDENTIFICATION) &&
//                            consentInitiation.has(CommonConstants.END_TO_END_IDENTIFICATION))) {
//
//                return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                        ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                        ErrorConstants.END_TO_END_IDENTIFICATION_NOT_FOUND);
//            }
//
//            return CommonConsentValidationUtil.getSuccessResponse(requestId);
//        }
//        return CommonConsentValidationUtil.getSuccessResponse(requestId);
//    }
//
//
//    /**
//     * Method to validate InstructedAmount.
//     * @param requestPath
//     * @param submissionInitiation
//     * @param consentInitiation
//     * @param requestId
//     * @return
//     */
//    public static JSONObject validateInstructedAmount(String requestPath, JSONObject submissionInitiation,
//                                                      JSONObject consentInitiation, String requestId) {
//
//        if (!requestPath.contains("domestic-standing-orders")) {
//
//            if (submissionInitiation.has(CommonConstants.INSTRUCTED_AMOUNT)) {
//
//                JSONObject subInstrAmount = (JSONObject) submissionInitiation.get(CommonConstants.INSTRUCTED_AMOUNT);
//                JSONObject initInstrAmount = (JSONObject) consentInitiation.get(CommonConstants.INSTRUCTED_AMOUNT);
//
//                if (subInstrAmount.has(CommonConstants.AMOUNT)) {
//
//                    if (StringUtils.isEmpty(subInstrAmount.getString(CommonConstants.AMOUNT)) ||
//                            !UKConsentValidatorUtil.compareMandatoryParameter(
//                                    subInstrAmount.getString(CommonConstants.AMOUNT),
//                                    initInstrAmount.getString(CommonConstants.AMOUNT))) {
//
//                        return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                                ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                                ErrorConstants.INSTRUCTED_AMOUNT_AMOUNT_MISMATCH);
//                    }
//                } else {
//                    return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                            ErrorConstants.FIELD_MISSING,
//                            ErrorConstants.INSTRUCTED_AMOUNT_AMOUNT_NOT_FOUND);
//                }
//
//                if (subInstrAmount.has(CommonConstants.CURRENCY)) {
//                    if (StringUtils.isEmpty(subInstrAmount.getString(CommonConstants.CURRENCY)) ||
//                            !UKConsentValidatorUtil.compareMandatoryParameter(
//                                    subInstrAmount.getString(CommonConstants.CURRENCY),
//                                    initInstrAmount.getString(CommonConstants.CURRENCY))) {
//
//                        return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                                ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                                ErrorConstants.INSTRUCTED_AMOUNT_CURRENCY_MISMATCH);
//                    }
//                } else {
//                    return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                            ErrorConstants.FIELD_MISSING,
//                            ErrorConstants.INSTRUCTED_AMOUNT_CURRENCY_NOT_FOUND);
//                }
//            } else {
//                return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                        ErrorConstants.FIELD_MISSING,
//                        ErrorConstants.INSTRUCTED_AMOUNT_NOT_FOUND);
//            }
//
//            return CommonConsentValidationUtil.getSuccessResponse(requestId);
//        }
//        return CommonConsentValidationUtil.getSuccessResponse(requestId);
//    }
//
//    /**
//     * Method to validate RequestedExecutionDate.
//     * @param requestPath
//     * @param submissionInitiation
//     * @param consentInitiation
//     * @param requestId
//     * @return
//     */
//    public static JSONObject validateRequestedExecutionDate(String requestPath, JSONObject submissionInitiation,
//                                                            JSONObject consentInitiation, String requestId) {
//
//        if (requestPath.contains("scheduled-payments")) {
//            if (submissionInitiation.has(CommonConstants.REQUEST_EXECUTION_DATE)) {
//
//                if (StringUtils.isEmpty(submissionInitiation.getString(CommonConstants.REQUEST_EXECUTION_DATE))
//                        || !UKConsentValidatorUtil.compareMandatoryParameter(
//                        submissionInitiation.getString(CommonConstants.REQUEST_EXECUTION_DATE),
//                        consentInitiation.getString(CommonConstants.REQUEST_EXECUTION_DATE))) {
//
//                    return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                            ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                            ErrorConstants.REQUESTED_EXECUTION_DATE_MISMATCH);
//                }
//            } else {
//                return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                        ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                        ErrorConstants.REQUESTED_EXECUTION_DATE_NOT_FOUND);
//            }
//
//            return CommonConsentValidationUtil.getSuccessResponse(requestId);
//        }
//        return CommonConsentValidationUtil.getSuccessResponse(requestId);
//    }
//
//    /**
//     * Method to validate Frequency.
//     * @param requestPath
//     * @param submissionInitiation
//     * @param consentInitiation
//     * @param invokedAPIVersion
//     * @param requestId
//     * @return
//     */
//    public static JSONObject validateFrequency(String requestPath, JSONObject submissionInitiation,
//                                               JSONObject consentInitiation,
//                                               CommonConstants.UK_API_VERSION invokedAPIVersion, String requestId) {
//
//        if (requestPath.contains("standing-orders")) {
//
//            if (CommonConstants.UK_API_VERSION.UK_API_V400.equals(invokedAPIVersion)) {
//
//                if (submissionInitiation.has(CommonConstants.MANDATE_RELATED_INFORMATION)) {
//
//                    JSONObject submissionMandateRelatedInfo = (JSONObject) submissionInitiation.get(
//                            CommonConstants.MANDATE_RELATED_INFORMATION);
//                    if (submissionMandateRelatedInfo != null &&
//                            submissionMandateRelatedInfo.has(CommonConstants.FREQUENCY)) {
//
//                        JSONObject initiationMandateRelatedInfo = (JSONObject) consentInitiation.get(
//                                CommonConstants.MANDATE_RELATED_INFORMATION);
//                        if (initiationMandateRelatedInfo == null || (initiationMandateRelatedInfo != null &&
//                                !initiationMandateRelatedInfo.has(CommonConstants.FREQUENCY))) {
//
//                            return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                                    ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                                    ErrorConstants.MANDATE_RELATED_INFORMATION_MISMATCH);
//                        }
//                        JSONObject initiationFrequency = (JSONObject) initiationMandateRelatedInfo.get(
//                                CommonConstants.FREQUENCY);
//                        JSONObject submissionFrequency = (JSONObject) submissionMandateRelatedInfo.get(
//                                CommonConstants.FREQUENCY);
//
//                        if (StringUtils.isEmpty(
//                                submissionFrequency.getString(CommonConstants.TYPE)) ||
//                                !UKConsentValidatorUtil.compareMandatoryParameter(
//                                        submissionFrequency.getString(CommonConstants.TYPE),
//                                        initiationFrequency.getString(CommonConstants.TYPE))) {
//
//                            return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                                    ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                                    ErrorConstants.FREQUENCY_MISMATCH_API_V4);
//                        }
//                        if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.COUNT_PER_PERIOD,
//                                submissionFrequency, initiationFrequency)){
//
//                            return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                                    ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                                    ErrorConstants.FREQUENCY_MISMATCH_API_V4);
//                        }
//                        if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.POINT_IN_TIME,
//                                submissionFrequency, initiationFrequency)){
//
//                            return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                                    ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                                    ErrorConstants.FREQUENCY_MISMATCH_API_V4);
//                        }
//                    } else {
//                        return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                                ErrorConstants.FIELD_MISSING,
//                                ErrorConstants.FREQUENCY_NOT_FOUND_API_V4);
//                    }
//                }
//            } else {
//                if (submissionInitiation.has(CommonConstants.FREQUENCY)) {
//                    if (StringUtils.isEmpty(submissionInitiation.getString(CommonConstants.FREQUENCY)) ||
//                            !UKConsentValidatorUtil.compareMandatoryParameter(
//                                    submissionInitiation.getString(CommonConstants.FREQUENCY),
//                                    consentInitiation.getString(CommonConstants.FREQUENCY))) {
//                        return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                                ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                                ErrorConstants.FREQUENCY_MISMATCH);
//                    }
//                } else {
//                    return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                            ErrorConstants.FIELD_MISSING,
//                            ErrorConstants.FREQUENCY_NOT_FOUND);
//                }
//            }
//
//            return CommonConsentValidationUtil.getSuccessResponse(requestId);
//        }
//        return CommonConsentValidationUtil.getSuccessResponse(requestId);
//    }
//
//    /**
//     * Method to validate FirstPaymentDate.
//     * @param requestPath
//     * @param submissionInitiation
//     * @param consentInitiation
//     * @param invokedAPIVersion
//     * @param requestId
//     * @return
//     */
//    public static JSONObject validateFirstPaymentDate(String requestPath, JSONObject submissionInitiation,
//                                                      JSONObject consentInitiation,
//                                                      CommonConstants.UK_API_VERSION invokedAPIVersion,
//                                                      String requestId) {
//
//        if (requestPath.contains("standing-orders")) {
//
//            if (CommonConstants.UK_API_VERSION.UK_API_V400.equals(invokedAPIVersion)) {
//
//                if (submissionInitiation.has(CommonConstants.MANDATE_RELATED_INFORMATION)) {
//
//                    JSONObject submissionMandateRelatedInfo = (JSONObject) submissionInitiation.get(
//                            CommonConstants.MANDATE_RELATED_INFORMATION);
//
//                    if (submissionMandateRelatedInfo != null &&
//                            submissionMandateRelatedInfo.has(CommonConstants.FIRST_PAYMENT_DATE)) {
//
//                        JSONObject initiationMandateRelatedInfo = (JSONObject) consentInitiation.get(
//                                CommonConstants.MANDATE_RELATED_INFORMATION);
//
//                        if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.FIRST_PAYMENT_DATE,
//                                submissionMandateRelatedInfo, initiationMandateRelatedInfo)) {
//
//                            return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                                    ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                                    ErrorConstants.FIRST_PAYMENT_DATE_MISMATCH_API_V4);
//                        }
//                    }
//                }
//            } else {
//                if (submissionInitiation.has(CommonConstants.FIRST_PAYMENT_DATE)) {
//
//                    if (StringUtils.isEmpty(submissionInitiation.getString(CommonConstants.FIRST_PAYMENT_DATE)) ||
//                            !UKConsentValidatorUtil.compareMandatoryParameter(
//                                    submissionInitiation.getString(CommonConstants.FIRST_PAYMENT_DATE),
//                                    consentInitiation.getString(CommonConstants.FIRST_PAYMENT_DATE))) {
//
//                        return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                                ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                                ErrorConstants.FIRST_PAYMENT_DATE_MISMATCH);
//                    }
//                } else {
//                    return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                            ErrorConstants.FIELD_MISSING,
//                            ErrorConstants.FIRST_PAYMENT_DATE_NOT_FOUND);
//                }
//            }
//
//            return CommonConsentValidationUtil.getSuccessResponse(requestId);
//        }
//        return CommonConsentValidationUtil.getSuccessResponse(requestId);
//    }
//
//    /**
//     * Method to validate FinalPaymentDate.
//     * @param requestPath
//     * @param submissionInitiation
//     * @param consentInitiation
//     * @param requestId
//     * @return
//     */
//    public static JSONObject validateCurrencyOfTransfer(String requestPath, JSONObject submissionInitiation,
//                                                        JSONObject consentInitiation, String requestId) {
//
//        if (requestPath.contains("international")) {
//            if (submissionInitiation.has(CommonConstants.CURRENCY_OF_TRANSFER)) {
//                if (StringUtils.isEmpty(submissionInitiation.getString(CommonConstants.CURRENCY_OF_TRANSFER)) ||
//                        !UKConsentValidatorUtil.compareMandatoryParameter(
//                                submissionInitiation.getString(CommonConstants.CURRENCY_OF_TRANSFER),
//                                consentInitiation.getString(CommonConstants.CURRENCY_OF_TRANSFER))) {
//
//                    return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                            ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                            ErrorConstants.CURRENCY_TRANSFER_MISMATCH);
//                }
//            } else {
//                return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                        ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                        ErrorConstants.CURRENCY_TRANSFER_NOT_FOUND);
//            }
//
//            return CommonConsentValidationUtil.getSuccessResponse(requestId);
//        }
//        return CommonConsentValidationUtil.getSuccessResponse(requestId);
//    }
//
//    /**
//     * Method to validate ChargeBearer.
//     * @param requestPath
//     * @param submissionInitiation
//     * @param consentInitiation
//     * @param requestId
//     * @return
//     */
//    public static JSONObject validateChargeBearer(String requestPath, JSONObject submissionInitiation,
//                                                        JSONObject consentInitiation, String requestId) {
//
//        if (requestPath.contains("international")) {
//            if ((submissionInitiation.has(CommonConstants.CHARGE_BEARER) &&
//                    !consentInitiation.has(CommonConstants.CHARGE_BEARER)) ||
//                    (!submissionInitiation.has(CommonConstants.CHARGE_BEARER) &&
//                            consentInitiation.has(CommonConstants.CHARGE_BEARER))) {
//
//                return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                        ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                        ErrorConstants.CHARGE_BEARER_NOT_FOUND);
//
//            } else if (submissionInitiation.has(CommonConstants.CHARGE_BEARER) &&
//                    consentInitiation.has(CommonConstants.CHARGE_BEARER) &&
//                    !UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.CHARGE_BEARER,
//                            submissionInitiation, consentInitiation)) {
//
//                return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                        ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                        ErrorConstants.CHARGE_BEARER_MISMATCH);
//            }
//
//            return CommonConsentValidationUtil.getSuccessResponse(requestId);
//        }
//        return CommonConsentValidationUtil.getSuccessResponse(requestId);
//    }
//
//    /**
//     * Method to validate ChargeBearer.
//     * @param requestPath
//     * @param submissionInitiation
//     * @param consentInitiation
//     * @param requestId
//     * @return
//     */
//    public static JSONObject validateDestinationCountryCode(String requestPath, JSONObject submissionInitiation,
//                                                  JSONObject consentInitiation, String requestId) {
//
//        if (requestPath.contains("international")) {
//            if ((submissionInitiation.has(CommonConstants.DESTINATION_COUNTRY_CODE) &&
//                    !consentInitiation.has(CommonConstants.DESTINATION_COUNTRY_CODE)) ||
//                    (!submissionInitiation.has(CommonConstants.DESTINATION_COUNTRY_CODE) &&
//                            consentInitiation.has(CommonConstants.DESTINATION_COUNTRY_CODE))) {
//
//                return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                        ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                        ErrorConstants.DESTINATION_COUNTRY_CODE_NOT_FOUND);
//
//            } else if (submissionInitiation.has(CommonConstants.DESTINATION_COUNTRY_CODE) &&
//                    consentInitiation.has(CommonConstants.DESTINATION_COUNTRY_CODE) &&
//                    !UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.DESTINATION_COUNTRY_CODE,
//                            submissionInitiation, consentInitiation)) {
//
//                return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                        ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                        ErrorConstants.DESTINATION_COUNTRY_CODE_MISMATCH);
//            }
//
//            return CommonConsentValidationUtil.getSuccessResponse(requestId);
//        }
//        return CommonConsentValidationUtil.getSuccessResponse(requestId);
//    }
//
//    /**
//     * Method to validate payment submission risk payload.
//     *
//     * @param submissionRisk Risk from submission request
//     * @param initiationRisk Risk from submission request
//     * @return Validation Result
//     */
//    public static JSONObject validateRisk(JSONObject submissionRisk, JSONObject initiationRisk,
//                                      String requestId) {
//
//        if (submissionRisk != null && initiationRisk != null) {
//
//            if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.CONTEXT_CODE,
//                    submissionRisk, initiationRisk)) {
//
//                return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                        ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                        ErrorConstants.PAYMENT_CONTEXT_CODE_MISMATCH);
//            }
//
//            if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.MERCHANT_CATEGORY_CODE,
//                    submissionRisk, initiationRisk)) {
//
//                return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                        ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                        ErrorConstants.MERCHANT_CATEGORY_CODE_MISMATCH);
//            }
//
//            if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.MERCHANT_IDENTIFICATION,
//                    submissionRisk, initiationRisk)) {
//
//                return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                        ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                        ErrorConstants.MERCHANT_CUSTOMER_IDENTIFICATION_MISMATCH);
//            }
//
//            if ((!initiationRisk.has(CommonConstants.DELIVERY_ADDRESS) &&
//                    submissionRisk.has(CommonConstants.DELIVERY_ADDRESS)) ||
//                    (initiationRisk.has(CommonConstants.DELIVERY_ADDRESS) &&
//                            !submissionRisk.has(CommonConstants.DELIVERY_ADDRESS))) {
//
//                return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                        ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                        ErrorConstants.DELIVERY_ADDRESS_MISMATCH);
//
//            } else if (submissionRisk.has(CommonConstants.DELIVERY_ADDRESS)) {
//
//                JSONObject subAddress = (JSONObject) submissionRisk.get(CommonConstants.DELIVERY_ADDRESS);
//                JSONObject initAddress = (JSONObject) initiationRisk.get(CommonConstants.DELIVERY_ADDRESS);
//
//                if (!UKConsentValidatorUtil
//                        .compareOptionalParameter(CommonConstants.STREET_NAME, subAddress, initAddress)) {
//
//                    return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                            ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                            ErrorConstants.STREET_NAME_MISMATCH);
//                }
//
//                if (!UKConsentValidatorUtil
//                        .compareOptionalParameter(CommonConstants.BUILDING_NUMBER, subAddress, initAddress)) {
//
//                    return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                            ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                            ErrorConstants.BUILDING_NUMBER_MISMATCH);
//                }
//
//                if (!UKConsentValidatorUtil
//                        .compareOptionalParameter(CommonConstants.POST_CODE, subAddress, initAddress)) {
//
//                    return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                            ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                            ErrorConstants.POST_CODE_MISMATCH);
//                }
//
//                if (!UKConsentValidatorUtil
//                        .compareOptionalParameter(CommonConstants.TOWN_NAME, subAddress, initAddress)) {
//
//                    return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                            ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                            ErrorConstants.TOWN_NAME_MISMATCH);
//                }
//
//                if (subAddress.has(CommonConstants.COUNTRY)) {
//                    if (StringUtils.isEmpty(subAddress.getString(CommonConstants.COUNTRY)) ||
//                            !UKConsentValidatorUtil.compareMandatoryParameter(
//                                    subAddress.getString(CommonConstants.COUNTRY),
//                                    initAddress.getString(CommonConstants.COUNTRY))) {
//
//                        return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                                ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                                ErrorConstants.COUNTRY_MISMATCH);
//                    }
//                } else {
//                    return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                            ErrorConstants.FIELD_MISSING,
//                            ErrorConstants.COUNTRY_MISMATCH);
//                }
//
//                if ((!initAddress.has(CommonConstants.ADDRESS_LINE) &&
//                        subAddress.has(CommonConstants.ADDRESS_LINE)) ||
//                        (initAddress.has(CommonConstants.ADDRESS_LINE) &&
//                                !subAddress.has(CommonConstants.ADDRESS_LINE))) {
//
//                    return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                            ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                            ErrorConstants.ADDRESS_LINE_NOT_FOUND);
//
//                } else if (subAddress.has(CommonConstants.ADDRESS_LINE)) {
//
//                    JSONArray subAddressLine = subAddress.getJSONArray(CommonConstants.ADDRESS_LINE);
//                    JSONArray initiationAddressLine = initAddress.getJSONArray(CommonConstants.ADDRESS_LINE);
//
//                    if (!new HashSet<>(subAddressLine.toList()).containsAll(initiationAddressLine.toList())) {
//
//                        return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                                ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                                ErrorConstants.ADDRESS_LINE_MISMATCH);
//                    }
//                }
//
//                if ((!initAddress.has(CommonConstants.COUNTRY_SUB_DIVISION) &&
//                        subAddress.has(CommonConstants.COUNTRY_SUB_DIVISION)) ||
//                        (initAddress.has(CommonConstants.COUNTRY_SUB_DIVISION) &&
//                                !subAddress.has(CommonConstants.COUNTRY_SUB_DIVISION))) {
//
//                    return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                            ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                            ErrorConstants.COUNTRY_SUB_DIVISION_NOT_FOUND);
//
//                } else if (subAddress.has(CommonConstants.COUNTRY_SUB_DIVISION) &&
//                        (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.COUNTRY_SUB_DIVISION,
//                                subAddress, initAddress))) {
//
//                    return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                            ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                            ErrorConstants.COUNTRY_SUB_DIVISION_MISMATCH);
//                }
//            }
//
//            return CommonConsentValidationUtil.getSuccessResponse(requestId);
//        }
//        return CommonConsentValidationUtil.getSuccessResponse(requestId);
//    }
//
//    /**
//     * Method to validate payment submission MandateRelatedInformation payload.
//     *
//     * @param subMandateRelatedInformation   MandateRelatedInformation from submission request
//     * @param initMandateRelatedInformation  MandateRelatedInformation from initiation request
//     * @return Validation Result
//     */
//    public static JSONObject validateMandateRelatedInformation(JSONObject subMandateRelatedInformation,
//                                                           JSONObject initMandateRelatedInformation,
//                                                           String requestId) {
//
//        if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.MANDATE_IDENTIFICATION,
//                subMandateRelatedInformation, initMandateRelatedInformation)) {
//
//            return CommonConsentValidationUtil
//                    .getErrorResponse(CommonConstants.BAD_REQUEST, ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                            ErrorConstants.MANDATE_IDENTIFICATION_MISMATCH);
//        }
//        if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.CLASSIFICATION,
//                subMandateRelatedInformation, initMandateRelatedInformation)) {
//
//            return CommonConsentValidationUtil
//                    .getErrorResponse(CommonConstants.BAD_REQUEST, ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                            ErrorConstants.CLASSIFICATION_MISMATCH);
//        }
//        if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.CATEGORY_PURPOSE_CODE,
//                subMandateRelatedInformation, initMandateRelatedInformation)) {
//
//            return CommonConsentValidationUtil
//                    .getErrorResponse(CommonConstants.BAD_REQUEST, ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                            ErrorConstants.CATEGORY_PURPOSE_CODE_MISMATCH);
//        }
//        if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.REASON,
//                subMandateRelatedInformation, initMandateRelatedInformation)) {
//
//            return CommonConsentValidationUtil
//                    .getErrorResponse(CommonConstants.BAD_REQUEST, ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                            ErrorConstants.REASON_MISMATCH);
//        }
//        if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.FIRST_PAYMENT_DATE,
//                subMandateRelatedInformation, initMandateRelatedInformation)) {
//
//            return CommonConsentValidationUtil
//                    .getErrorResponse(CommonConstants.BAD_REQUEST, ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                            ErrorConstants.FIRST_PAYMENT_DATE_MISMATCH_API_V4);
//        }
//        if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.FINAL_PAYMENT_DATE,
//                subMandateRelatedInformation, initMandateRelatedInformation)) {
//
//            return CommonConsentValidationUtil
//                    .getErrorResponse(CommonConstants.BAD_REQUEST, ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                            ErrorConstants.FINAL_PAYMENT_DATE_MISMATCH_API_V4);
//        }
//        if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.RECURRING_PAYMENT_DATE,
//                subMandateRelatedInformation, initMandateRelatedInformation)) {
//
//            return CommonConsentValidationUtil
//                    .getErrorResponse(CommonConstants.BAD_REQUEST, ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                            ErrorConstants.RECURRING_PAYMENT_DATE_MISMATCH_API_V4);
//        }
//        if (!subMandateRelatedInformation.has(CommonConstants.FREQUENCY) ||
//                !initMandateRelatedInformation.has(CommonConstants.FREQUENCY)) {
//
//            return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                    ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                    ErrorConstants.FREQUENCY_NOT_FOUND_API_V4);
//        }
//
//        return CommonConsentValidationUtil.getSuccessResponse(requestId);
//    }
//
//    /**
//     * Validate whether fields in creditor account from initiation and submission are same.
//     *
//     * @param subCreditorAccount   Creditor Account from submission request
//     * @param initCreditorAccount  Creditor Account from initiation request
//     * @return Validation Result
//     */
//    public static JSONObject validateCreditorAcc(JSONObject subCreditorAccount, JSONObject initCreditorAccount,
//                                             String requestId) {
//
//        JSONObject validationResponse = null;
//
//        if (subCreditorAccount.has(CommonConstants.SCHEME_NAME)) {
//            if (StringUtils.isEmpty(subCreditorAccount.getString(CommonConstants.SCHEME_NAME)) ||
//                    !UKConsentValidatorUtil.compareMandatoryParameter(
//                            subCreditorAccount.getString(CommonConstants.SCHEME_NAME),
//                            initCreditorAccount.getString(CommonConstants.SCHEME_NAME))) {
//
//                return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                        ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                        ErrorConstants.CREDITOR_ACC_SCHEME_NAME_MISMATCH);
//            }
//        } else {
//            return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                    ErrorConstants.FIELD_MISSING,
//                    ErrorConstants.CREDITOR_ACC_SCHEME_NAME_NOT_FOUND);
//        }
//
//        if (subCreditorAccount.has(CommonConstants.IDENTIFICATION)) {
//            if (StringUtils.isEmpty(subCreditorAccount.getString(CommonConstants.IDENTIFICATION)) ||
//                    !UKConsentValidatorUtil.compareMandatoryParameter(
//                            subCreditorAccount.getString(CommonConstants.IDENTIFICATION),
//                            initCreditorAccount.getString(CommonConstants.IDENTIFICATION))) {
//
//                return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                        ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                        ErrorConstants.CREDITOR_ACC_IDENTIFICATION_MISMATCH);
//            }
//        } else {
//            return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                    ErrorConstants.FIELD_MISSING,
//                    ErrorConstants.CREDITOR_ACC_IDENTIFICATION_NOT_FOUND);
//        }
//
//        if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.NAME,
//                subCreditorAccount, initCreditorAccount)) {
//
//            return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                    ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                    ErrorConstants.CREDITOR_ACC_NAME_MISMATCH);
//        }
//
//        if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.SECONDARY_IDENTIFICATION,
//                subCreditorAccount, initCreditorAccount)) {
//
//            return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                    ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                            ErrorConstants.CREDITOR_ACC_SEC_IDENTIFICATION_MISMATCH);
//        }
//
//        if ((!subCreditorAccount.has(CommonConstants.PROXY) &&
//                initCreditorAccount.has(CommonConstants.PROXY)) ||
//                (subCreditorAccount.has(CommonConstants.PROXY) &&
//                        !initCreditorAccount.has(CommonConstants.PROXY))) {
//
//            return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                    ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                    ErrorConstants.CREDITOR_ACC_PROXY_MISMATCH);
//
//        } else if (subCreditorAccount.has(CommonConstants.PROXY) &&
//                initCreditorAccount.has(CommonConstants.PROXY)) {
//
//            JSONObject subCreditorAccountProxy = (JSONObject) subCreditorAccount.get(CommonConstants.PROXY);
//            JSONObject initCreditorAccountProxy =
//                    (JSONObject) initCreditorAccount.get(CommonConstants.PROXY);
//
//            validationResponse = UKConsentValidatorUtil.validateCreditorAccProxy(
//                    subCreditorAccountProxy, initCreditorAccountProxy, requestId);
//            if (validationResponse != null && validationResponse.toString().contains(CommonConstants.ERROR)) {
//                return validationResponse;
//            }
//        }
//
//        return CommonConsentValidationUtil.getSuccessResponse(requestId);
//    }
//
//    /**
//     * Validate whether fields in debtor account from initiation and submission are same.
//     *
//     * @param subDebtorAccount   Debtor Account from submission request
//     * @param initDebtorAccount  Debtor Account from initiation request
//     * @return Validation Result
//     */
//    public static JSONObject validateDebtorAcc(JSONObject subDebtorAccount, JSONObject initDebtorAccount, String requestId) {
//
//        JSONObject validationResponse = null;
//
//        if (subDebtorAccount.has(CommonConstants.SCHEME_NAME)) {
//            if (StringUtils.isEmpty(subDebtorAccount.getString(CommonConstants.SCHEME_NAME)) ||
//                    !UKConsentValidatorUtil.compareMandatoryParameter(
//                            subDebtorAccount.getString(CommonConstants.SCHEME_NAME),
//                            initDebtorAccount.getString(CommonConstants.SCHEME_NAME))) {
//
//                return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                        ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                        ErrorConstants.DEBTOR_ACC_SCHEME_NAME_MISMATCH);
//            }
//        } else {
//            return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                    ErrorConstants.FIELD_MISSING,
//                    ErrorConstants.DEBTOR_ACC_SCHEME_NAME_NOT_FOUND);
//        }
//
//        if (subDebtorAccount.has(CommonConstants.IDENTIFICATION)) {
//            if (StringUtils.isEmpty(subDebtorAccount.getString(CommonConstants.IDENTIFICATION)) ||
//                    !UKConsentValidatorUtil.compareMandatoryParameter(
//                            subDebtorAccount.getString(CommonConstants.IDENTIFICATION),
//                            initDebtorAccount.getString(CommonConstants.IDENTIFICATION))) {
//
//                return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                        ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                                ErrorConstants.DEBTOR_ACC_IDENTIFICATION_MISMATCH);
//            }
//        } else {
//            return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                    ErrorConstants.FIELD_MISSING,
//                    ErrorConstants.DEBTOR_ACC_IDENTIFICATION_NOT_FOUND);
//        }
//
//        if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.NAME,
//                subDebtorAccount, initDebtorAccount)) {
//
//            return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                    ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                    ErrorConstants.DEBTOR_ACC_NAME_MISMATCH);
//        }
//
//        if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.SECONDARY_IDENTIFICATION,
//                subDebtorAccount, initDebtorAccount)) {
//
//            return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                    ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                            ErrorConstants.DEBTOR_ACC_SEC_IDENTIFICATION_MISMATCH);
//        }
//
//        if ((!subDebtorAccount.has(CommonConstants.PROXY) &&
//                initDebtorAccount.has(CommonConstants.PROXY)) ||
//                (subDebtorAccount.has(CommonConstants.PROXY) &&
//                        !initDebtorAccount.has(CommonConstants.PROXY))) {
//
//            return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                    ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                    ErrorConstants.DEBTOR_ACC_PROXY_MISMATCH);
//        } else if (subDebtorAccount.has(CommonConstants.PROXY) &&
//                initDebtorAccount.has(CommonConstants.PROXY)) {
//
//            JSONObject subDebtorAccountProxy = (JSONObject) subDebtorAccount.get(CommonConstants.PROXY);
//            JSONObject initDebtorAccountProxy =
//                    (JSONObject) initDebtorAccount.get(CommonConstants.PROXY);
//
//            validationResponse = UKConsentValidatorUtil.validateDebtorAccProxy(subDebtorAccountProxy,
//                    initDebtorAccountProxy, requestId);
//
//            if (validationResponse != null && validationResponse.toString().contains(CommonConstants.ERROR)) {
//                return validationResponse;
//            }
//        }
//
//        return CommonConsentValidationUtil.getSuccessResponse(requestId);
//    }
//
//    /**
//     * Validate whether fields in creditor account proxy from initiation and submission are same.
//     *
//     * @param subCreditorAccountProxy   Creditor Account Proxy from submission request
//     * @param initCreditorAccountProxy  Creditor Account Proxy from initiation request
//     * @return Validation Result
//     */
//    public static JSONObject validateCreditorAccProxy(JSONObject subCreditorAccountProxy,
//                                                      JSONObject initCreditorAccountProxy, String requestId) {
//
//        if (subCreditorAccountProxy.has(CommonConstants.IDENTIFICATION_TITLE)) {
//            if (StringUtils.isEmpty(subCreditorAccountProxy.getString(
//                    CommonConstants.IDENTIFICATION_TITLE)) ||
//                    !UKConsentValidatorUtil.compareMandatoryParameter(
//                            subCreditorAccountProxy.getString(CommonConstants.IDENTIFICATION_TITLE),
//                            initCreditorAccountProxy.getString(CommonConstants.IDENTIFICATION_TITLE))) {
//
//                return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                        ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                        ErrorConstants.CREDITOR_ACC_PROXY_IDENTIFICATION_MISMATCH);
//            }
//        } else {
//            return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                    ErrorConstants.FIELD_MISSING,
//                    ErrorConstants.CREDITOR_ACC_PROXY_IDENTIFICATION_NOT_FOUND);
//        }
//
//        // validate code
//        if (subCreditorAccountProxy.has(CommonConstants.CODE)) {
//            if (StringUtils.isEmpty(subCreditorAccountProxy.getString(
//                    CommonConstants.CODE)) ||
//                    !UKConsentValidatorUtil.compareMandatoryParameter(
//                            subCreditorAccountProxy.getString(CommonConstants.CODE),
//                            initCreditorAccountProxy.getString(CommonConstants.CODE))) {
//
//                return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                        ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                        ErrorConstants.CREDITOR_ACC_PROXY_CODE_MISMATCH);
//            }
//        } else {
//            return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                    ErrorConstants.FIELD_MISSING,
//                    ErrorConstants.CREDITOR_ACC_PROXY_CODE_NOT_FOUND);
//        }
//
//        // validate optional Type
//        if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.TYPE,
//                subCreditorAccountProxy,  initCreditorAccountProxy)) {
//
//            return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                    ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                            ErrorConstants.CREDITOR_ACC_PROXY_TYPE_MISMATCH);
//        }
//
//        JSONObject validationResult = new JSONObject();
//        validationResult.put(CommonConstants.IS_VALID_PAYLOAD, true);
//
//        return CommonConsentValidationUtil.getSuccessResponse(requestId);
//    }
//
//    /**
//     * Method to validate payment submission UltimateCreditor payload.
//     *
//     * @param subUltimateCreditor   UltimateCreditor from submission request
//     * @param initUltimateCreditor  UltimateCreditor Proxy from initiation request
//     * @return Validation Result
//     */
//    public static JSONObject validateUltimateCreditor(JSONObject subUltimateCreditor, JSONObject initUltimateCreditor,
//                                                  String requestId) {
//
//        if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.NAME_TITLE,
//                subUltimateCreditor, initUltimateCreditor)) {
//
//            return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                    ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                            ErrorConstants.ULTIMATE_CREDITOR_NAME_MISMATCH);
//        }
//        if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.IDENTIFICATION_TITLE,
//                subUltimateCreditor, initUltimateCreditor)) {
//
//            return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                    ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                            ErrorConstants.ULTIMATE_CREDITOR_IDENTIFICATION_MISMATCH);
//        }
//        if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.LEI,
//                subUltimateCreditor, initUltimateCreditor)) {
//
//            return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                    ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                            ErrorConstants.ULTIMATE_CREDITOR_LEI_MISMATCH);
//        }
//        if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.SCHEME_NAME,
//                subUltimateCreditor, initUltimateCreditor)) {
//
//            return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                    ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                            ErrorConstants.ULTIMATE_CREDITOR_SCHEME_NAME_MISMATCH);
//        }
//
//        if (!UKConsentValidatorUtil.isValidAddress((JSONObject) subUltimateCreditor
//                        .get(CommonConstants.POSTAL_ADDRESS),
//                (JSONObject) initUltimateCreditor.get(CommonConstants.POSTAL_ADDRESS))) {
//
//            return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                    ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                    ErrorConstants.ULTIMATE_CREDITOR_POSTAL_ADDRESS_MISMATCH);
//        }
//
//        return CommonConsentValidationUtil.getSuccessResponse(requestId);
//    }
//
//    /**
//     * Method to validate whether address is valid.
//     *
//     * @param submissionAddress Address from submission request
//     * @param initiationAddress Address from initiation request
//     * @return is valid address
//     */
//    public static boolean isValidAddress(JSONObject submissionAddress, JSONObject initiationAddress) {
//
//        if ((submissionAddress == null && initiationAddress != null) ||
//                (submissionAddress != null && initiationAddress == null)) {
//            return false;
//
//        } else if (submissionAddress != null) {
//
//            if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.ADDRESS_TYPE,
//                    submissionAddress, initiationAddress)) {
//                return false;
//            }
//
//            if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.DEPARTMENT,
//                    submissionAddress, initiationAddress)) {
//                return false;
//            }
//
//            if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.SUB_DEPARTMENT,
//                    submissionAddress, initiationAddress)) {
//                return false;
//            }
//
//            if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.STREET_NAME,
//                    submissionAddress, initiationAddress)) {
//                return false;
//            }
//
//            if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.BUILDING_NUMBER,
//                    submissionAddress, initiationAddress)) {
//                return false;
//            }
//
//            if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.BUILDING_NAME,
//                    submissionAddress, initiationAddress)) {
//                return false;
//            }
//
//            if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.FLOOR,
//                    submissionAddress, initiationAddress)) {
//                return false;
//            }
//
//            if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.UNIT_NUMBER,
//                    submissionAddress, initiationAddress)) {
//                return false;
//            }
//
//            if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.ROOM,
//                    submissionAddress, initiationAddress)) {
//                return false;
//            }
//
//            if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.POST_BOX,
//                    submissionAddress, initiationAddress)) {
//                return false;
//            }
//
//            if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.TOWN_LOCATION_NAME,
//                    submissionAddress, initiationAddress)) {
//                return false;
//            }
//
//            if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.DISTRICT_NAME,
//                    submissionAddress, initiationAddress)) {
//                return false;
//            }
//
//            if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.CARE_OF,
//                    submissionAddress, initiationAddress)) {
//                return false;
//            }
//
//            if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.POST_CODE,
//                    submissionAddress, initiationAddress)) {
//                return false;
//            }
//
//            if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.TOWN_NAME,
//                    submissionAddress, initiationAddress)) {
//                return false;
//            }
//
//            if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.COUNTRY,
//                    submissionAddress, initiationAddress)) {
//                return false;
//            }
//
//            if ((!initiationAddress.has(CommonConstants.ADDRESS_LINE) &&
//                    submissionAddress.has(CommonConstants.ADDRESS_LINE)) ||
//                    (initiationAddress.has(CommonConstants.ADDRESS_LINE) &&
//                            !submissionAddress.has(CommonConstants.ADDRESS_LINE))) {
//                return false;
//
//            } else {
//                JSONArray subAddressLine = submissionAddress.getJSONArray(CommonConstants.ADDRESS_LINE);
//                JSONArray initiationAddressLine = initiationAddress.getJSONArray(CommonConstants.ADDRESS_LINE);
//
//                if (!new HashSet<>(subAddressLine.toList()).containsAll(initiationAddressLine.toList())) {
//
//                    return false;
//                }
//            }
//
//            if ((!initiationAddress.has(CommonConstants.COUNTRY_SUB_DIVISION) &&
//                    submissionAddress.has(CommonConstants.COUNTRY_SUB_DIVISION)) ||
//                    (initiationAddress.has(CommonConstants.COUNTRY_SUB_DIVISION) &&
//                            !submissionAddress.has(CommonConstants.COUNTRY_SUB_DIVISION))) {
//                return false;
//
//            } else if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.COUNTRY_SUB_DIVISION,
//                    submissionAddress, initiationAddress)) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    /**
//     * Method to validate payment submission UltimateDebtor payload.
//     *
//     * @param subUltimateDebtor   UltimateDebtor from submission request
//     * @param initUltimateDebtor  UltimateDebtor Proxy from initiation request
//     * @return Validation Result
//     */
//    public static JSONObject validateUltimateDebtor(JSONObject subUltimateDebtor, JSONObject initUltimateDebtor,
//                                                String requestId) {
//
//        if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.NAME_TITLE,
//                subUltimateDebtor, initUltimateDebtor)) {
//
//            return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                    ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                            ErrorConstants.ULTIMATE_DEBTOR_NAME_MISMATCH);
//        }
//        if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.IDENTIFICATION_TITLE,
//                subUltimateDebtor, initUltimateDebtor)) {
//
//            return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                    ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                            ErrorConstants.ULTIMATE_DEBTOR_IDENTIFICATION_MISMATCH);
//        }
//        if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.LEI,
//                subUltimateDebtor, initUltimateDebtor)) {
//
//            return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                    ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                            ErrorConstants.ULTIMATE_DEBTOR_LEI_MISMATCH);
//        }
//        if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.SCHEME_NAME,
//                subUltimateDebtor, initUltimateDebtor)) {
//
//            return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                    ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                            ErrorConstants.ULTIMATE_DEBTOR_SCHEME_NAME_MISMATCH);
//        }
//
//        if (!UKConsentValidatorUtil.isValidAddress((JSONObject) subUltimateDebtor
//                        .get(CommonConstants.POSTAL_ADDRESS),
//                (JSONObject) initUltimateDebtor.get(CommonConstants.POSTAL_ADDRESS))) {
//
//            return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                    ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                    ErrorConstants.ULTIMATE_DEBTOR_POSTAL_ADDRESS_MISMATCH);
//        }
//
//        return CommonConsentValidationUtil.getSuccessResponse(requestId);
//    }
//
//    /**
//     * Validate whether fields in debtor account from initiation and submission are same.
//     *
//     * @param subDebtorAccountProxy   Debtor Account from submission request
//     * @param initDebtorAccountProxy  Debtor Account from initiation request
//     * @return Validation Result
//     */
//    public static JSONObject validateDebtorAccProxy(JSONObject subDebtorAccountProxy,
//                                                    JSONObject initDebtorAccountProxy, String requestId) {
//
//        if (subDebtorAccountProxy.has(CommonConstants.IDENTIFICATION_TITLE)) {
//
//            if (StringUtils.isEmpty(subDebtorAccountProxy.getString(CommonConstants.IDENTIFICATION_TITLE)) ||
//                    !UKConsentValidatorUtil.compareMandatoryParameter(
//                            subDebtorAccountProxy.getString(CommonConstants.IDENTIFICATION_TITLE),
//                            initDebtorAccountProxy.getString(CommonConstants.IDENTIFICATION_TITLE))) {
//
//                return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                        ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                        ErrorConstants.DEBTOR_ACC_PROXY_IDENTIFICATION_MISMATCH);
//            }
//        } else {
//            return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                    ErrorConstants.FIELD_MISSING,
//                    ErrorConstants.DEBTOR_ACC_PROXY_IDENTIFICATION_NOT_FOUND);
//        }
//
//        // validate code
//        if (subDebtorAccountProxy.has(CommonConstants.CODE)) {
//            if (StringUtils.isEmpty(subDebtorAccountProxy.getString(CommonConstants.CODE)) ||
//                    !UKConsentValidatorUtil.compareMandatoryParameter(
//                            subDebtorAccountProxy.getString(CommonConstants.CODE),
//                            initDebtorAccountProxy.getString(CommonConstants.CODE))) {
//
//                return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                        ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                        ErrorConstants.DEBTOR_ACC_PROXY_CODE_MISMATCH);
//            }
//        } else {
//            return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                    ErrorConstants.FIELD_MISSING,
//                    ErrorConstants.DEBTOR_ACC_PROXY_CODE_NOT_FOUND);
//        }
//
//        // validate optional Type
//        if (!UKConsentValidatorUtil.compareOptionalParameter(CommonConstants.TYPE,
//                subDebtorAccountProxy, initDebtorAccountProxy)) {
//
//            return CommonConsentValidationUtil.getErrorResponse(CommonConstants.BAD_REQUEST,
//                    ErrorConstants.RESOURCE_CONSENT_MISMATCH,
//                            ErrorConstants.DEBTOR_ACC_PROXY_TYPE_MISMATCH);
//        }
//        return CommonConsentValidationUtil.getSuccessResponse(requestId);
//    }
//
//    /**
//     * Util method to validate the Confirmation of Funds request URI.
//     *
//     * @param uri  Request URI
//     * @return
//     */
//    public static boolean isCOFURIValid(String uri) {
//        List<String> accountPaths = getCOFAPIPathRegexArray();
//
//        for (String entry : accountPaths) {
//            if (uri.equals(entry)) {
//                return true;
//            }
//        }
//
//        return false;
//    }
//
//    /**
//     * Method provides API resource paths applicable for UK Confirmtaion of Funds API.
//     *
//     * @return map of API Resources.
//     */
//    public static List<String> getCOFAPIPathRegexArray() {
//
//        List<String> requestUrls = Arrays.asList(CommonConstants.COF_CONSENT_INITIATION_PATH,
//                CommonConstants.COF_CONSENT_CONSENT_ID_PATH,
//                CommonConstants.COF_SUBMISSION_PATH);
//
//        return requestUrls;
//
//    }

}

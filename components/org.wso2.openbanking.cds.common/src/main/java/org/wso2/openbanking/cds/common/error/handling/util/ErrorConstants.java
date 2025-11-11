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

package org.wso2.openbanking.cds.common.error.handling.util;

import com.wso2.openbanking.accelerator.common.util.Generated;

/**
 * Error Constant Class.
 */
public class ErrorConstants {

    // Error Response Structure constants
    public static final String ERRORS = "errors";
    public static final String ERROR = "error";
    public static final String ERROR_DESCRIPTION = "error_description";
    public static final String CODE = "code";
    public static final String TITLE = "title";
    public static final String DETAIL = "detail";
    public static final String META_URN = "metaURN";
    public static final String ACCOUNT_ID = "accountId";
    public static final String META = "meta";
    public static final String URN = "urn";

    // HTTP Error Codes
    public static final String HTTP_BAD_REQUEST = "400";
    public static final String HTTP_UNAUTHORIZED = "401";
    public static final String HTTP_FORBIDDEN = "403";
    public static final String HTTP_NOT_FOUND = "404";
    public static final String HTTP_NOT_ALLOWED = "405";
    public static final String HTTP_NOT_ACCEPTABLE = "406";
    public static final String HTTP_TOO_MANY_REQUESTS = "429";
    public static final String HTTP_UNPROCESSABLE_ENTITY = "422";
    public static final String HTTP_SERVER_ERROR = "500";
    public static final String HTTP_CONFLICT = "409";
    public static final String HTTP_UNSUPPORTED_MEDIA_TYPE = "415";

    // High level textual error code, to help categorize the errors.
    public static final String BAD_REQUEST_CODE = "400 Bad Request";
    public static final String UNAUTHORIZED_CODE = "401 Unauthorized";
    public static final String FORBIDDEN_CODE = "403 Forbidden";
    public static final String NOT_FOUND_CODE = "404 Not Found";
    public static final String NOT_ALLOWED_CODE = "405 Method Not Allowed";
    public static final String NOT_ACCEPTABLE_CODE = "406 Not Acceptable";
    public static final String TOO_MANY_REQUESTS_CODE = "429 Too Many Requests";
    public static final String SERVER_ERROR_CODE = "500 Internal Server Error";
    public static final String CONFLICT_CODE = "409 Conflict";
    public static final String UNSUPPORTED_MEDIA_TYPE_CODE = "415 Unsupported Media Type";

    // Low level textual error code
    public static final String RESOURCE_INVALID_BANKING_ACCOUNT =
            "urn:au-cds:error:cds-banking:Authorisation/InvalidBankingAccount";
    public static final String RESOURCE_INVALID = "urn:au-cds:error:cds-all:Resource/Invalid";
    public static final String INVALID_CONSENT_STATUS = "urn:au-cds:error:cds-all:Authorisation/InvalidConsent";
    public static final String REVOKED_CONSENT_STATUS = "urn:au-cds:error:cds-all:Authorisation/RevokedConsent";

    // Error object references
    public static final String ERROR_ENUM = "auErrorEnum";
    public static final String METADATA = "metadata";
    public static final String SUCCESS = "SUCCESS";
    public static final String CONSENT_ENFORCEMENT_ERROR = "Consent Enforcement Error";


    /**
     * AU Error enumerations.
     */
    @Generated(message = "Excluding constants from code coverage")
    public enum AUErrorEnum {

        /**
         * Error enumerations defined in the specification.
         * <p>
         * Custom values in the enum will be in the following order
         * (HTTP Status Code, Error Code, Error Code Title, Error Message)
         */
        //Query or Request Body Parameter missing
        FIELD_MISSING(400, "urn:au-cds:error:cds-all:Field/Missing", "Missing Required Field",
                "Missing Required Field %s in the request"),
        //one or more http headers are missing
        HEADER_MISSING(400, "urn:au-cds:error:cds-all:Header/Missing", "Missing Required Header",
                "Mandatory header %s is missing"),
        //Query or Request Body Parameter is not defined by the API
        UNEXPECTED_FIELD(400, "AU.CDR.Unexpected.Field", "Unexpected Field Not Allowed", "Unexpected Field %s is " +
                "not allowed by the API"),
        //Header is not defined by the API
        UNEXPECTED_HEADER(400, "AU.CDR.Unexpected.Header", "Unexpected Header Not Allowed", "Unexpected Header " +
                "Not Allowed"),
        //Query, PAth or Request Body Parameter is invalid
        INVALID_FIELD(400, "urn:au-cds:error:cds-all:Field/Invalid", "Invalid Field",
                "Invalid Field %s found in the request"),
        INVALID_PAGE(400, "urn:au-cds:error:cds-all:Field/InvalidPage", "Invalid Page",
                "Invalid Page %s found in the request"),
        //Header is invalid type or violates the constraints
        INVALID_HEADER(400, "urn:au-cds:error:cds-all:Header/Invalid", "Invalid Header",
                "Requested %s header is not supported"),
        //Invalid date is provided in query parameters
        INVALID_DATE(400, "AU.CDR.Invalid.DateTime", "Invalid Date", "Date found in the query parameters is not" +
                " in the accepted format"),
        //Page size query parameter does not contains positive value
        INVALID_PAGE_SIZE(400, "AU.CDR.Invalid.PageSize", "Invalid Page Size", "Page size should contains a positive" +
                " value"),
        //Brand provided in invalid
        INVALID_BRAND(400, "AU.CDR.Register.InvalidBrand", "Invalid Brand", "Invalid Brand found in the request"),
        //Industry provided in path parameter is invalid / does not exist and cannot be found.
        INVALID_INDUSTRY(400, "AU.CDR.Register.InvalidIndustry", "Invalid Industry Requested", "Requested Industry " +
                "in the request does not exist or invalid"),
        BAD_REQUEST(400, "AU.CDR.BadRequest", "Invalid Request", "Invalid Request passed"),
        //AccountIDs field is missing in the request body
        MISSING_FIELD_ACCOUNTIDS(400, "urn:au-cds:error:cds-all:Field/Missing",
                "Missing Required Field", "accountIds field is missing in the request"),
        //Invalid authorization header
        UNAUTHORIZED(401, "AU.CDR.Unauthorized", "Invalid Authorisation Header",
                "Authorization header not found in the request"),
        //Invalid authorization header
        CLIENT_AUTH_FAILED(401, "urn:au-cds:error:cds-all:Authorisation/Unauthorized",
                "Invalid Client", "Client authentication failed"),
        //The ADR is not in an "active" state in the CDR Register
        INVALID_ADR_STATUS(403, "urn:au-cds:error:cds-banking:Authorisation/AdrStatusNotActive",
                "ADR Status Is Invalid", "The ADR is not in an active state in the CDR Register"),
        //The ADR software product is not in an "active" state in the CDR Register.
        INVALID_PRODUCT_STATUS(403, "urn:au-cds:error:cds-banking:Authorisation/AdrStatusNotActive",
                "ADR Software Product Status Is Invalid", "The software product of ADR is not in an " +
                "active state in the CDR Register"),
        //Consent status of the resource is not accepted by the API
        INVALID_CONSENT(403, "urn:au-cds:error:cds-all:Authorisation/InvalidConsent",
                "Consent Is Invalid", "Consent status is not" +
                " acceptable by the API"),
        //Consent is in the revoked state
        REVOKED_CONSENT(403, "AU.CDR.Entitlements.ConsentIsRevoked", "Consent Is Revoked",
                "Provided consent is in the revoked state"),
        //The resource requested is forbidden
        RESOURCE_FORBIDDEN(403, "urn:au-cds:error:cds-all:Authorisation/InvalidConsent",
                "Resource Is Forbidden", "The requested resource %s is forbidden"),
        //Software product requested is invalid or cannot be found.
        INVALID_SOFTWARE_PRODUCT(404, "AU.CDR.Register.InvalidSoftwareProduct", "Invalid Software Product",
                "Software product requested is invalid or cannot be found"),
        //Requested resource is available in the spec but not implemented
        RESOURCE_NOT_IMPLEMENTED(404, "AU.CDR.Resource.NotImplemented", "Resource Not Implemented",
                "Requested resource %s is available in the specification but not implemented"),
        //Requested resource is not available in the spec
        RESOURCE_NOT_FOUND(404, "urn:au-cds:error:cds-all:Resource/NotFound", "Resource Not Found",
                "Requested resource is not available in the specification"),
        //Resource requested is invalid, does not exist or will not be disclosed
        INVALID_RESOURCE_PATH(404, "urn:au-cds:error:cds-all:Resource/Invalid", "Invalid Resource",
                "Resource requested is invalid, does not exist or will not be disclosed at the moment"),
        INVALID_RESOURCE_BODY(422, "urn:au-cds:error:cds-all:Resource/Invalid",
                "Invalid Resource", "Resource requested is invalid, does not exist or will " +
                "not be disclosed at the moment"),
        //The requested resource is currently in a state that makes it unavailable
        RESOURCE_UNAVAILABLE_PATH(404, "AU.CDR.Resource.Unavailable", "Resource Is Unavailable",
                "The requested resource is currently in a state that makes it unavailable for consumers"),
        RESOURCE_UNAVAILABLE_BODY(422, "AU.CDR.Resource.Unavailable", "Resource Is Unavailable",
                "The requested resource is currently in a state that makes it unavailable for consumers"),
        //Bank account does not exist or does nt associate with active consent
        INVALID_BANK_ACCOUNT_BODY(422, "urn:au-cds:error:cds-banking:Authorisation/InvalidBankingAccount",
                "Invalid Banking Account", "ID of the account not found or invalid"),
        INVALID_BANK_ACCOUNT_PATH(404, "urn:au-cds:error:cds-banking:Authorisation/InvalidBankingAccount",
                "Invalid Banking Account", "ID of the account not found or invalid"),
        //Bank account is no longer associated to the active consent, a joint-account holder has withdrawn consent
        // election or the account is currently in a state that makes it unavailable
        UNAVAILABLE_BANK_ACCOUNT(422, "AU.CDR.Resource.UnavailableBankingAccount", "Banking Account Is Unavailable",
                "Banking Account ID provided in the request is Unavailable"),
        //Requested version is less than the minimum version or greater than the maximum version
        UNSUPPORTED_VERSION(406, "urn:au-cds:error:cds-all:Header/UnsupportedVersion", "Unsupported Version",
                "Requested endpoint version %s is not supported"),
        INVALID_ACCEPT_HEADER(406, "urn:au-cds:error:cds-all:GeneralError/Expected", "Expected Error Encountered",
                "Invalid Accept Header"),
        //Page out of range
        PAGE_OUT_OF_RANGE(400, "urn:au-cds:error:cds-all:Field/InvalidPageSize", "Invalid Page Size",
                "Page Size Value Exceeds Valid Limits"),
        //Page size is greater than max
        PAGE_SIZE_EXCEED(422, "AU.CDR.Invalid.PageSizeTooLarge", "Page Size Exceeded", "Page Size Exceeded"),
        //Requested version is not a positive integer
        INVALID_VERSION(400, "urn:au-cds:error:cds-all:Header/InvalidVersion", "Invalid Version",
                "Requested %s version is not valid"),
        TEAPOT(418, "AU.CDR.IAmATeapot", "I'm A Teapot", "I'm A Teapot"),
        //API unavailable as part of a partial outage.
        SERVICE_UNAVAILABLE(503, "AU.CDR.Service.Unavailable", "Service Unavailable", "Service Unavailable"),
        TOO_MANY_REQUESTS(429, "AU.CDR.TooManyRequests", "Message throttled out", "You have exceeded your quota"),
        //An unexpected error occurred
        UNEXPECTED_ERROR(500, "urn:au-cds:error:cds-all:GeneralError/Unexpected",
                "Unexpected Error", "Unexpected Error"),
        //Expected general error occurred
        EXPECTED_GENERAL_ERROR(404, "urn:au-cds:error:cds-all:GeneralError/Expected",
                "Expected Error Encountered", "Expected Error Encountered"),
        INVALID_ARRANGEMENT(422, "urn:au-cds:error:cds-all:Authorisation/InvalidArrangement",
                "Invalid Arrangement ID", "Arrangement ID is invalid, does not exist or the arrangement " +
                "is not in authorised state"),

        /**
         * Error enumerations defined by the WSO2.
         * <p>
         * Custom values in the enum will be in the following order
         * (HTTP Status Code, Error Code, Error Code Title, Error Message)
         */
        //Data Validation Error
        VALIDATION_ERROR(400, "WSO2.Data.ValidationError", "Error has occurred while validating account consent",
                "Error has occurred while validating account consent");

        private final int httpCode;
        private final String code;
        private final String title;
        private final String detail;

        /**
         * Initialize AU Error Object.
         *
         * @param httpCode http code.
         * @param code     error code.
         * @param title    error title.
         * @param detail   error detail.
         */
        @Generated(message = "Excluding constants from code coverage")
        AUErrorEnum(int httpCode, String code, String title, String detail) {

            this.httpCode = httpCode;
            this.code = code;
            this.title = title;
            this.detail = detail;
        }

        /**
         * Get HTTP Code.
         *
         * @return
         */
        @Generated(message = "Excluding constants from code coverage")
        public int getHttpCode() {

            return httpCode;
        }

        /**
         * Get Code.
         *
         * @return
         */
        @Generated(message = "Excluding constants from code coverage")
        public String getCode() {

            return code;
        }

        /**
         * Get Title.
         *
         * @return
         */
        @Generated(message = "Excluding constants from code coverage")
        public String getTitle() {

            return title;
        }

        /**
         * Get Detail.
         *
         * @return
         */
        @Generated(message = "Excluding constants from code coverage")
        public String getDetail() {

            return detail;
        }

        private String value;

        public static AUErrorEnum fromValue(String text) {

            for (AUErrorEnum b : AUErrorEnum.values()) {
                if (String.valueOf(b).equals(text)) {
                    return b;
                }
            }

            return null;
        }
    }
}

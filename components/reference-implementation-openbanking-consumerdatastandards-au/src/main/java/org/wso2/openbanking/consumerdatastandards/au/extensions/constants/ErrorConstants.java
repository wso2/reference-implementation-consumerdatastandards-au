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

package org.wso2.openbanking.consumerdatastandards.au.extensions.constants;

/**
 * This class holds error constants for the CDS Open Banking implementation.
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
    public static final String CONSENT_ID_NOT_FOUND = "Consent ID not available in consent data";
    public static final String AUTH_RESOURCE_NOT_FOUND = "Auth resource not available in consent data";
    public static final String ACCOUNT_ID_NOT_FOUND_ERROR = "Account IDs not available in persist request";
    public static final String PAYLOAD_FORMAT_ERROR = "Request Payload is not in correct JSON format";
    public static final String INVALID_URI_ERROR = "Path requested is invalid. :" + ErrorConstants.PATH_URL;
    public static final String PATH_URL = "Data.Url";
    public static final String INVALID_CDR_ARRANGEMENT_ID = "Invalid cdr_arrangement_id";
    public static final String INVALID_REQUEST_OBJECT = "invalid_request_object";
    public static final String EMPTY_CDR_ARRANGEMENT_ID = "Empty cdr-arrangement-id sent in the request";
    public static final String INVALID_SHARING_DURATION = "Invalid sharing_duration value";

}

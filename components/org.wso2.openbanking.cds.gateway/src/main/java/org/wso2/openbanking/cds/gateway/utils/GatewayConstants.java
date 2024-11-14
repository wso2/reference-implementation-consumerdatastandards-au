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

package org.wso2.openbanking.cds.gateway.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Gateway Constant Class.
 */
public class GatewayConstants {

    private GatewayConstants() {
    }

    //Error handling constants
    public static final String ERROR_CODE = "ERROR_CODE";
    public static final String ERROR_MSG = "ERROR_MESSAGE";
    public static final String ERROR_DETAIL = "ERROR_DETAIL";
    public static final String ERROR_RESPONSE = "errorResponse";
    public static final String STATUS_CODE = "statusCode";
    public static final String MESSAGE_TYPE = "messageType";
    public static final String JSON_CONTENT_TYPE = "application/json";
    public static final String RESPONSE_CAPS = "RESPONSE";
    public static final String TRUE = "true";
    public static final String NO_ENTITY_BODY = "NO_ENTITY_BODY";
    public static final String CONTENT_TYPE_TAG = "Content-Type";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String ACCEPT_HEADER = "Accept header";
    public static final String EMPTY_SOAP_BODY =
            "<soapenv:Body xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\"/>";

    public static final int API_AUTH_GENERAL_ERROR = 900900;
    public static final int API_AUTH_INVALID_CREDENTIALS = 900901;
    public static final int API_AUTH_MISSING_CREDENTIALS = 900902;
    public static final int API_AUTH_ACCESS_TOKEN_EXPIRED = 900903;
    public static final int API_AUTH_ACCESS_TOKEN_INACTIVE = 900904;
    public static final int API_AUTH_INCORRECT_ACCESS_TOKEN_TYPE = 900905;
    public static final int API_AUTH_INCORRECT_API_RESOURCE = 900906;
    public static final int API_BLOCKED = 900907;
    public static final int API_AUTH_FORBIDDEN = 900908;
    public static final int SUBSCRIPTION_INACTIVE = 900909;
    public static final int INVALID_SCOPE = 900910;
    public static final int API_AUTH_MISSING_OPEN_API_DEF = 900911;
    public static final int GRAPHQL_INVALID_QUERY = 900422;

    public static final String PAYLOAD_FORMING_ERROR = "Error while forming fault payload";
    public static final String PAYLOAD_SETTING_ERROR = "Error while setting the json payload";
    public static final String SCHEMA_FAIL_MSG = "Schema validation failed";
    public static final String THROTTLED_OUT_ERROR = "Message throttled out. You have exceeded your quota";
    public static final String MAXIMUM_PAGE_SIZE_ERROR = "Numeric instance is greater than the required maximum";
    public static final String MINIMUM_PAGE_SIZE_ERROR = "Numeric instance is lower than the required minimum";

    public static final String X_FAPI_CUSTOMER_IP_ADDRESS = "x-fapi-customer-ip-address";
    public static final String CUSTOMER_STATUS = "customerStatus";
    public static final String AUTHORIZATION = "Authorization";
    public static final String ACCESS_TOKEN_ID = "accessTokenID";

    //Data publishing constants

    //Customer status types
    public static final String UNATTENDED = "unattended";
    public static final String CUSTOMER_PRESENT = "customer-present";
    public static final String UNDEFINED = "undefined";

    //Infosec endpoint constants
    public static final String TOKEN_API = "TokenAPI";
    public static final String AUTHORIZE_API = "AuthorizeAPI";
    public static final String USERINFO_API = "UserInfoAPI";
    public static final String INTROSPECT_API = "IntrospectAPI";
    public static final String JWKS_API = "JWKSAPI";
    public static final String TOKEN_REVOCATION_API = "TokenRevocationAPI";
    public static final String WELL_KNOWN_API = "WellKnownAPI";
    public static final String PAR_API = "PARAPI";

    public static final String TOKEN_ENDPOINT = "/oauth2/token";
    public static final String AUTHORIZE_ENDPOINT = "/oauth2/authorize";
    public static final String JWKS_ENDPOINT = "/oauth2/jwks";
    public static final String USERINFO_ENDPOINT = "/oauth2/userinfo";
    public static final String REVOKE_ENDPOINT = "/oauth2/revoke";
    public static final String INTROSPECTION_ENDPOINT = "/oauth2/introspect";
    public static final String PAR_ENDPOINT = "/api/openbanking/push-authorization/par";
    public static final String WELL_KNOWN_ENDPOINT = "/oauth2/token/.well-known/openid-configuration";
    public static final String REGISTER_ENDPOINT = "/register";
    public static final String REGISTER_CLIENT_ID_ENDPOINT = "/register/{ClientId}";
    public static final String CDR_ARRANGEMENT_ENDPOINT = "/arrangements/1.0.0";
    public static final String DISCOVERY_OUTAGES_ENDPOINT = "/discovery/outages";
    public static final String DISCOVERY_STATUS_ENDPOINT = "/discovery/status";
    public static final String PRODUCTS_ENDPOINT = "/banking/products";
    public static final String PRODUCT_DETAILS_ENDPOINT = "/banking/products/{productId}";
    public static final String COMMON_CUSTOMER_ENDPOINT = "/common/customer";
    public static final String COMMON_CUSTOMER_DETAIL_ENDPOINT = "/common/customer/detail";

    public static final List<String> INFOSEC_ENDPOINTS = Collections.unmodifiableList(Arrays.asList(AUTHORIZE_ENDPOINT,
            TOKEN_ENDPOINT, USERINFO_ENDPOINT, PAR_ENDPOINT, INTROSPECTION_ENDPOINT, JWKS_ENDPOINT, REVOKE_ENDPOINT,
            REGISTER_ENDPOINT, REGISTER_CLIENT_ID_ENDPOINT, WELL_KNOWN_ENDPOINT, CDR_ARRANGEMENT_ENDPOINT,
            DISCOVERY_OUTAGES_ENDPOINT, DISCOVERY_STATUS_ENDPOINT));

    public static final List<String> UNAUTHENTICATED_ENDPOINTS = Collections.unmodifiableList(Arrays.asList(
            PRODUCTS_ENDPOINT, PRODUCT_DETAILS_ENDPOINT, DISCOVERY_STATUS_ENDPOINT, DISCOVERY_OUTAGES_ENDPOINT));

    public static final List<String> COMMON_ENDPOINTS = Collections.unmodifiableList(Arrays.asList(
            COMMON_CUSTOMER_ENDPOINT, COMMON_CUSTOMER_DETAIL_ENDPOINT));

    public static final String UNKNOWN = "Unknown";
    public static final String CLIENT_USER_AGENT = "User-Agent";
    public static final String USER_NAME = "api.ut.userName";
    public static final String CONSUMER_KEY = "api.ut.consumerKey";
    public static final String HTTP_METHOD = "HTTP_METHOD";
    public static final String API_NAME = "api.ut.api";
    public static final String API_SPEC_VERSION = "SYNAPSE_REST_API_VERSION";
    public static final String SYNAPSE_REST_API = "SYNAPSE_REST_API";
    public static final String CORRELATION_ID = "correlation_id";
    public static final String REST_API_CONTEXT = "REST_API_CONTEXT";
    public static final String REST_API_NAME = "api.ut.api";
    public static final String HTTP_RESPONSE_STATUS_CODE = "HTTP_RESPONSE_STATUS_CODE";
    public static final String CUSTOM_HTTP_SC = "CUSTOM_HTTP_SC";
    public static final String HTTP_SC = "HTTP_SC";
    public static final String REST_METHOD = "REST_METHOD";
    public static final String OAUTH_JWT_ASSERTION = "client_assertion";
    public static final String CLIENT_ID = "client_id";
    public static final String ENDPOINT_ADDRESS = "ENDPOINT_ADDRESS";


    // Constants related to executors
    public static final String HTTP_GET = "GET";
    public static final String HTTP_POST = "POST";

    // CDSHeaderValidationExecutor constants
    public static final String X_CDS_CLIENT_HEADERS = "x-cds-client-headers";
    public static final String X_FAPI_AUTH_DATE = "x-fapi-auth-date";
    public static final String X_FAPI_INTERACTION_ID = "x-fapi-interaction-id";
    public static final String MAX_REQUESTED_ENDPOINT_VERSION = "x-v";
    public static final String MIN_REQUESTED_ENDPOINT_VERSION = "x-min-v";
    public static final String X_VERSION = "x-version";
    public static final String IMF_FIX_DATE_PATTERN = "EEE, dd MMM uuuu HH:mm:ss 'GMT'";
    public static final String RFC850_DATE_PATTERN = "EEEE, dd-MMM-uu HH:mm:ss 'GMT'";
    public static final String ASC_TIME_DATE_PATTERN = "EE MMM dd HH:mm:ss uuuu";
    public static final Pattern UUID_REGEX_PATTERN =
            Pattern.compile("^[{]?[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}[}]?$");

    // JWTAuthenticationExecutor constants
    public static final String EXP_CLAIM = "exp";
    public static final String ISS_CLAIM = "iss";
    public static final String SUB_CLAIM = "sub";
    public static final String AUD_CLAIM = "aud";
}

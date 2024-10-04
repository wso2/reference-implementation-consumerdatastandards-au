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

package org.wso2.openbanking.cds.identity.filter.constants;

/**
 * Field names used for data publishing related to InfoSec endpoints.
 */
public class CDSFilterConstants {

    public static final String TOKEN_ENDPOINT = "/token";
    public static final String AUTHORIZE_ENDPOINT = "/authorize";
    public static final String JWKS_ENDPOINT = "/jwks";
    public static final String USERINFO_ENDPOINT = "/userinfo";
    public static final String REVOKE_ENDPOINT = "/revoke";
    public static final String INTROSPECTION_ENDPOINT = "/introspect";
    public static final String PAR_ENDPOINT = "/par";
    public static final String WELL_KNOWN_ENDPOINT = "/.well-known/openid-configuration";

    public static final String TOKEN_REQUEST_URI = "/oauth2/token";
    public static final String AUTHORIZE_REQUEST_URI = "/oauth2/authorize";
    public static final String JWKS_REQUEST_URI = "/oauth2/jwks";
    public static final String USERINFO_REQUEST_URI = "/oauth2/userinfo";
    public static final String REVOKE_REQUEST_URI = "/oauth2/revoke";
    public static final String INTROSPECTION_REQUEST_URI = "/oauth2/introspect";
    public static final String PAR_REQUEST_URI = "/api/openbanking/push-authorization/par";
    public static final String WELL_KNOWN_REQUEST_URI = "/oauth2/token/.well-known/openid-configuration";

    public static final String TOKEN_API = "TokenAPI";
    public static final String AUTHORIZE_API = "AuthorizeAPI";
    public static final String USERINFO_API = "UserInfoAPI";
    public static final String INTROSPECT_API = "IntrospectAPI";
    public static final String JWKS_API = "JwksAPI";
    public static final String TOKEN_REVOCATION_API = "TokenRevocationAPI";
    public static final String WELL_KNOWN_API = "WellKnownAPI";
    public static final String PAR_API = "PAR";

    public static final String REQUEST_IN_TIME = "REQUEST_IN_TIME";
    public static final String UNDEFINED = "undefined";
    public static final String CONTENT_LENGTH = "Content-Length";

}

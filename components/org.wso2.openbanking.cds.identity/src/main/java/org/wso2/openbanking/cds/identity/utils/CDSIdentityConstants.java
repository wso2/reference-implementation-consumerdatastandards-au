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

package org.wso2.openbanking.cds.identity.utils;

import org.wso2.openbanking.cds.identity.authenticator.util.Constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Identity Constants Class.
 */
public class CDSIdentityConstants {

    // Error messages.
    public static final String INVALID_PUSH_AUTH_REQUEST = "Invalid push authorisation request";
    public static final String INVALID_SHARING_DURATION = "Invalid sharing_duration value";
    public static final String UNSUPPORTED_RESPONSE_TYPE = "Unsupported response_type value. Only code response type " +
            "is allowed.";
    public static final String UNSUPPORTED_RESPONSE_MODE = "Unsupported response_mode value. Only jwt response mode " +
            "is allowed.";

    // Consent status constants.
    public static final String AUTHORIZED = "authorized";

    // Request object parameters and values.
    public static final String STATE = "state";
    public static final String RESPONSE_TYPE = "response_type";
    public static final String RESPONSE_MODE = "response_mode";
    public static final String CODE_RESPONSE_TYPE = "code";
    public static final String JWT_RESPONSE_MODE = "jwt";
    public static final String UNSUPPORTED_RESPONSE_TYPE_ERROR = "unsupported_response_type";
    public static final String SESSION_DATA_KEY_PARAMETER = "sessionDataKey";
    public static final List<String> MANDATORY_ASSERTION_PARAMS_LIST = Collections
            .unmodifiableList(Arrays.asList(Constants.ISSUER_CLAIM, Constants.SUBJECT_CLAIM, Constants.AUDIENCE_CLAIM,
                    Constants.EXPIRATION_TIME_CLAIM, Constants.JWT_ID_CLAIM));
    public static final String CLIENT_ASSERTION = "client_assertion";

}

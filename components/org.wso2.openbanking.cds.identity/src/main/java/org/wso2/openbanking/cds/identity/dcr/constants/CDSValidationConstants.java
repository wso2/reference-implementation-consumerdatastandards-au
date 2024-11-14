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
package org.wso2.openbanking.cds.identity.dcr.constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Field names used for CDS specific validations.
 */
public class CDSValidationConstants {

    // ssa claims
    public static final String SSA_REDIRECT_URIS = "redirect_uris";
    public static final String SSA_SECTOR_IDENTIFIER_URI = "sector_identifier_uri";
    public static final String CDR_REGISTER = "cdr-register";
    public static final String OPENID = "openid";
    public static final String SSA_LOGO_URI = "logo_uri";
    public static final String SSA_POLICY_URI = "policy_uri";
    public static final String SSA_TOS_URI = "tos_uri";
    public static final String SSA_CLIENT_URI = "client_uri";
    public static final String CLIENT_ASSERTION = "client_assertion";
    public static final String DATA_RECIPIENT_SOFTWARE_PRODUCT = "data-recipient-software-product";
    public static final List<String> VALID_SSA_SCOPES = Collections.unmodifiableList(Arrays.asList(
            "openid", "profile", "bank:accounts.basic:read", "bank:accounts.detail:read", "bank:transactions:read",
            "bank:payees:read", "bank:regular_payments:read", "common:customer.basic:read",
            "common:customer.detail:read", "cdr:registration"));
    public static final String CDR_REGISTRATION_SCOPE = "cdr:registration";

    // registration request params
    public static final String INVALID_REDIRECT_URI = "invalid_redirect_uri";
    public static final String ID_TOKEN_ENCRYPTION_RESPONSE_ALG = "id_token_encrypted_response_alg";
    public static final String ID_TOKEN_ENCRYPTION_RESPONSE_ENC = "id_token_encrypted_response_enc";

    // dcr config constants
    public static final String DCR_VALIDATE_REDIRECT_URI = "DCR.EnableURIValidation";
    public static final String DCR_VALIDATE_URI_HOSTNAME = "DCR.EnableHostNameValidation";
    public static final String DCR_VALIDATE_SECTOR_IDENTIFIER_URI = "DCR.EnableSectorIdentifierUriValidation";
    public static final String JTI = "jti";
    public static final String JTI_REPLAYED = "JTI value of the registration request has been replayed";
}

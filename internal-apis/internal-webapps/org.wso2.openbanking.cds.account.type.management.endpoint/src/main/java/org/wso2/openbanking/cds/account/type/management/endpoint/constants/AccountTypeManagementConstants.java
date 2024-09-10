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

package org.wso2.openbanking.cds.account.type.management.endpoint.constants;

/**
 * Constant Class for Account Type Management.
 */
public class AccountTypeManagementConstants {

    // Common
    public static final String CARBON_TENANT_DOMAIN = "@carbon.super";
    public static final String AUTHORIZED_STATUS = "authorized";

    // Secondary User Instructions
    public static final String PRIMARY_MEMBER_AUTH_TYPE = "primary_member";
    public static final String ACTIVE_STATUS = "active";

    // Ceasing Secondary User Sharing
    public static final String METADATA_KEY_BLOCKED_LEGAL_ENTITIES = "BLOCKED_LEGAL_ENTITIES";
    public static final String LEGAL_ENTITY_ID = "legal_entity_id";
    public static final String LEGAL_ENTITY_NAME = "legal_entity_name";

    // Disclosure Options Management
    public static final String DATA = "data";
    public static final String ACCOUNT_ID = "accountID";
    public static final String DISCLOSURE_OPTION = "disclosureOption";
    public static final String DISCLOSURE_OPTION_STATUS = "DISCLOSURE_OPTIONS_STATUS";

    // Business Nominated Representative
    public static final String BNR_PERMISSION = "bnr-permission";
    public static final String NOMINATED_REPRESENTATIVE_AUTH_TYPE = "nominated_representative";
    public static final String REVOKED_CONSENT_STATUS = "Revoked";
    public static final String METADATA_SERVICE_ERROR = "\"Error occurred while persisting nominated " +
            "representative data using the account metadata service\"";

}

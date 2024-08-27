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

package org.wso2.openbanking.cds.gateway.executors.core;

/**
 * Constants required for CDSAPIRequestRouter.
 */
public class RequestRouterConstants {

    // Executor list names
    public static final String DEFAULT = "Default";
    public static final String DCR = "DCR";
    public static final String CDS = "CDS";
    public static final String CDS_COMMON = "CDSCommon";
    public static final String CONSENT = "Consent";
    public static final String ADMIN = "Admin";
    public static final String ARRANGEMENT = "Arrangement";

    // API Type constants
    public static final String API_TYPE_CUSTOM_PROP = "x-wso2-api-type";
    public static final String API_TYPE_CONSENT = "consent";
    public static final String API_TYPE_NON_REGULATORY = "non-regulatory";
    public static final String API_TYPE_DCR = "dcr";
    public static final String API_TYPE_CDS = "cds";
    public static final String API_TYPE_COMMON = "common";
    public static final String API_TYPE_ARRANGEMENT = "arrangement";
    public static final String API_TYPE_ADMIN = "admin";

    // API Name constants
    // API Name should be a string without whitespaces.
    public static final String DCR_API_NAME = "CDRDynamicClientRegistrationAPI";
    public static final String CDS_API_NAME = "ConsumerDataStandards";
    public static final String COMMON_API_NAME = "ConsumerDataStandardsCommon";
    public static final String ADMIN_API_NAME = "ConsumerDataStandardsAdminAPI";
    public static final String ARRANGEMENT_API_NAME = "CDRArrangementManagementAPI";

    public static final String WHITESPACE_REGEX = "\\s";
}

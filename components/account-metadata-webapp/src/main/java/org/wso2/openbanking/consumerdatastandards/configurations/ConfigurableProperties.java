/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.openbanking.consumerdatastandards.configurations;

/**
 * This class holds configurable properties for the CDS Open Banking implementation.
 */
public class ConfigurableProperties {

    public static final String SHARABLE_ENDPOINT = "http://localhost:9766/api/openbanking/cds/backend/" +
            "services/bankaccounts/bankaccountservice/sharable-accounts";
    public static final String ACCOUNT_METADATA_DATASOURCE_JNDI_NAME = "jdbc/ACCOUNT_METADATA_DB";

}

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

package org.wso2.openbanking.consumerdatastandards.service.dao.queries;


public interface AccountMetadataDBQueries {

    // DB Queries related to DOMS Feature.

    /**
     * Get the SQL query for adding a new disclosure option.
     *
     * @return the SQL query
     */
    String getAddDisclosureOptionQuery();

    /**
     * Get the SQL query for updating an existing disclosure option.
     *
     * @return the SQL query
     */
    String getUpdateDisclosureOptionQuery();

    /**
     * Get the SQL query for retrieving a disclosure option.
     *
     * @return the SQL query
     */
    String getGetDisclosureOptionQuery();

}

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

package org.wso2.openbanking.cds.consent.extensions.model;

import java.sql.Timestamp;

/**
 * Model class for consent sharing date data.
 */
public class DataClusterSharingDateModel {
    private String dataCluster;
    private Timestamp sharingStartDate;
    private Timestamp lastSharedDate;

    public String getDataCluster() {
        return dataCluster;
    }

    public void setDataCluster(String dataCluster) {
        this.dataCluster = dataCluster;
    }

    public Timestamp getSharingStartDate() {
        if (sharingStartDate == null) {
            return null;
        } else {
            return (Timestamp) sharingStartDate.clone();
        }
    }

    public void setSharingStartDate(Timestamp sharingStartDate) {
        if (sharingStartDate == null) {
            this.sharingStartDate = null;
        } else {
            this.sharingStartDate = (Timestamp) sharingStartDate.clone();
        }
    }

    public Timestamp getLastSharedDate() {
        if (lastSharedDate == null) {
            return null;
        } else {
            return (Timestamp) lastSharedDate.clone();
        }
    }

    public void setLastSharedDate(Timestamp lastSharedDate) {
        if (lastSharedDate == null) {
            this.lastSharedDate = null;
        } else {
            this.lastSharedDate = (Timestamp) lastSharedDate.clone();
        }
    }
}

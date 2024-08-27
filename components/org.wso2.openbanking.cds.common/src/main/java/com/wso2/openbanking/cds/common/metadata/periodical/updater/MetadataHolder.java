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

package org.wso2.openbanking.cds.common.metadata.periodical.updater;

import java.util.Map;

/**
 * MetadataHolder
 * <p>
 * Holds the JSON data of software product and data recipients.
 * Will be used by status validator to get the data.
 * Can be considered as primary cache that gets overridden every n minutes.
 */
public class MetadataHolder {

    private static volatile MetadataHolder instance;
    private Map<String, String> softwareProduct;
    private Map<String, String> dataRecipient;

    private MetadataHolder() {
    }

    public static MetadataHolder getInstance() {

        if (instance == null) {
            synchronized (MetadataHolder.class) {
                if (instance == null) {
                    instance = new MetadataHolder();
                }
            }
        }
        return instance;
    }


    public Map<String, String> getSoftwareProduct() {
        return this.softwareProduct;
    }

    public void setSoftwareProduct(Map<String, String> softwareProduct) {
        this.softwareProduct = softwareProduct;
    }

    public Map<String, String> getDataRecipient() {
        return this.dataRecipient;
    }

    public void setDataRecipient(Map<String, String> dataRecipient) {
        this.dataRecipient = dataRecipient;
    }
}

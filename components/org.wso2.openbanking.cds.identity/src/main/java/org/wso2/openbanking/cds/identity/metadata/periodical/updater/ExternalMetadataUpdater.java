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

package org.wso2.openbanking.cds.identity.metadata.periodical.updater;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.cds.identity.metadata.periodical.updater.internal.MetaDataUpdate;
import org.wso2.openbanking.cds.identity.metadata.periodical.updater.internal.PeriodicalMetaDataUpdateJob;

/**
 * Interface exposed to trigger metadata cache update externally.
 */
public class ExternalMetadataUpdater {

    private static volatile ExternalMetadataUpdater instance;
    private static final Log log = LogFactory.getLog(ExternalMetadataUpdater.class);

    private ExternalMetadataUpdater() {

    }

    public static ExternalMetadataUpdater getInstance() {

        if (instance == null) {
            synchronized (ExternalMetadataUpdater.class) {
                if (instance == null) {
                    instance = new ExternalMetadataUpdater();
                }
            }
        }
        return instance;
    }

    public void updateMetadata() {
        log.info("Updating data recipient and software product metadata by external call.");
        MetaDataUpdate updateInterface = new PeriodicalMetaDataUpdateJob();
        updateInterface.updateMetaDataValues();
    }
}

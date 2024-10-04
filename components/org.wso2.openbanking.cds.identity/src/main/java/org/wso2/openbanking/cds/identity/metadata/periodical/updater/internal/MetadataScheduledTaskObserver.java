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

package org.wso2.openbanking.cds.identity.metadata.periodical.updater.internal;

import com.wso2.openbanking.accelerator.service.activator.OBServiceObserver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;

/**
 * MetadataScheduledTaskObserver
 * <p>
 * Service to initiate Metadata Cache updater Scheduled Task.
 */
public class MetadataScheduledTaskObserver implements OBServiceObserver {

    private static final Log LOG = LogFactory.getLog(MetadataScheduledTaskObserver.class);

    @Override
    public void activate() {
        if (OpenBankingCDSConfigParser.getInstance().isMetadataCacheEnabled()) {
            new MetadataUpdater().run();
            LOG.debug("Periodical metadata updater is activated");
        }
    }
}

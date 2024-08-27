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

package org.wso2.openbanking.cds.identity.metadata.periodical.updater.service.dataholder.responsibility;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * DataHolderResponsibilitiesExecutor.
 * <p>
 * Executes CDS data holder responsibilities in DataHolderResponsibility list
 */
public class DataHolderResponsibilitiesExecutor {
    private static final Log LOG = LogFactory.getLog(DataHolderResponsibilitiesExecutor.class);

    private static volatile DataHolderResponsibilitiesExecutor instance;
    private final Map<String, DataHolderResponsibility> responsibilityMap;

    private DataHolderResponsibilitiesExecutor() {
        this.responsibilityMap = new HashMap<>();
    }

    public static DataHolderResponsibilitiesExecutor getInstance() {

        if (instance == null) {
            synchronized (DataHolderResponsibilitiesExecutor.class) {
                if (instance == null) {
                    instance = new DataHolderResponsibilitiesExecutor();
                }
            }
        }
        return instance;
    }

    public void addResponsibilities(Map<String, DataHolderResponsibility> newResponsibilityMap) {
        this.responsibilityMap.putAll(newResponsibilityMap);
    }

    public void addResponsibility(DataHolderResponsibility responsibility) {
        this.responsibilityMap.put(responsibility.getResponsibilityId(), responsibility);
    }

    public synchronized void execute() {
        LOG.debug("Executing data holder responsibilities");

        responsibilityMap.values().parallelStream()
                .filter(Objects::nonNull)
                .filter(DataHolderResponsibility::shouldPerform)
                .forEach(DataHolderResponsibility::perform);

        this.responsibilityMap.clear();
    }
}

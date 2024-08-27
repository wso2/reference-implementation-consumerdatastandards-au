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

package org.wso2.openbanking.cds.metrics.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;
import org.wso2.openbanking.cds.metrics.periodic.job.HistoricMetricsCacheJob;
import org.wso2.openbanking.cds.metrics.periodic.scheduler.MetricsPeriodicJobScheduler;

/**
 * Metrics Service Component
 */
@Component(
        name = "org.wso2.openbanking.cds.metrics.internal.MetricsServiceComponent",
        immediate = true
)
public class MetricsServiceComponent {
    private static final Log log = LogFactory.getLog(MetricsServiceComponent.class);
    private final OpenBankingCDSConfigParser configParser = OpenBankingCDSConfigParser.getInstance();

    @Activate
    protected void activate(ComponentContext context) {

        if (configParser.isMetricsPeriodicalJobEnabled()) {
            MetricsPeriodicJobScheduler.getInstance().initScheduler();
            log.debug("CDS Metrics periodic scheduler is initialized");

            // Cache historic metrics at server startup
            HistoricMetricsCacheJob job = new HistoricMetricsCacheJob();
            job.execute(null);
            log.debug("HistoricMetricsCacheJob executed at server startup");
        }
        log.debug("CDS Metrics bundle is activated");

    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        log.debug("CDS Metrics bundle is deactivated");
    }

    public static RealmService getRealmService() {
        return (RealmService) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .getOSGiService(RealmService.class, null);
    }

    @Reference(
            name = "realm.service",
            service = RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService"
    )
    protected void setRealmService(RealmService realmService) {

        log.debug("Setting the Realm Service");
        MetricsDataHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {

        log.debug("UnSetting the Realm Service");
        MetricsDataHolder.getInstance().setRealmService(null);
    }

    @Reference(name = "api.manager.config.service",
            service = APIManagerConfigurationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetAPIManagerConfigurationService"
    )
    protected void setAPIConfigurationService(APIManagerConfigurationService confService) {

        MetricsDataHolder.getInstance().setApiManagerConfigurationService(confService);
        log.debug("API manager configuration service bound to the CDS Metrics data holder");
    }

    protected void unsetAPIManagerConfigurationService(APIManagerConfigurationService amcService) {

        MetricsDataHolder.getInstance().setApiManagerConfigurationService(null);
        log.debug("API manager configuration service unbound from the CDS Metrics data holder");

    }

}

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;
import org.wso2.openbanking.cds.identity.metadata.periodical.updater.service.dataholder.responsibility.DataHolderResponsibilitiesBulkExecutorJob;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Scheduled Task definition and trigger to perform primary cache-update job every n minutes.
 */
class MetadataUpdater {

    private static final Log LOG = LogFactory.getLog(MetadataUpdater.class);
    private static final String JOB_1 = "PeriodicalMetaDataUpdateJob";
    private static final String JOB_2 = "DataHolderResponsibilitiesBulkExecutorJob";
    private static final String TRIGGER_1 = "PeriodicalMetaDataUpdateTrigger";
    private static final String TRIGGER_2 = "DataHolderResponsibilitiesBulkExecutorTrigger";
    private static final String GROUP_1 = "group1";
    private static final String GROUP_2 = "group2";

    void run() {
        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();

            // configuring periodical meta data update job
            JobDetail periodicalMetaDataUpdateJob = newJob(PeriodicalMetaDataUpdateJob.class)
                    .withIdentity(JOB_1, GROUP_1)
                    .build();

            Trigger periodicalMetaDataUpdateTrigger = newTrigger()
                    .withIdentity(TRIGGER_1, GROUP_1)
                    .withPriority(1)
                    .startNow()
                    .withSchedule(simpleSchedule().withIntervalInMinutes(OpenBankingCDSConfigParser.getInstance()
                            .getMetaDataCacheUpdatePeriodInMinutes()).repeatForever())
                    .build();

            scheduler.scheduleJob(periodicalMetaDataUpdateJob, periodicalMetaDataUpdateTrigger);

            if (OpenBankingCDSConfigParser.getInstance().isBulkOperation()) {
                // configuring data holder responsibilities execution
                JobDetail responsibilitiesBulkExecutorJob = newJob(DataHolderResponsibilitiesBulkExecutorJob.class)
                        .withIdentity(JOB_2, GROUP_2)
                        .build();

                Trigger responsibilitiesBulkExecutorTrigger = newTrigger()
                        .withIdentity(TRIGGER_2, GROUP_2)
                        .withPriority(2)
                        .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(OpenBankingCDSConfigParser.getInstance()
                                .getBulkExecutionHour(), 0))
                        .build();

                scheduler.scheduleJob(responsibilitiesBulkExecutorJob, responsibilitiesBulkExecutorTrigger);
            }
        } catch (SchedulerException e) {
            LOG.error("Error while creating and starting Metadata Update Scheduled Task", e);
        }
    }
}

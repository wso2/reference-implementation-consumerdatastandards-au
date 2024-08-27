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

package org.wso2.openbanking.cds.metrics.periodic.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.simpl.SimpleClassLoadHelper;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.xml.XMLSchedulingDataProcessor;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.nio.file.Paths;

/**
 * Periodic job scheduler class.
 * This class initialize the scheduler on osgi activation and schedule configured jobs and triggers.
 */
public class MetricsPeriodicJobScheduler {

    private static Log log = LogFactory.getLog(MetricsPeriodicJobScheduler.class);
    private static final String QUARTZ_PROPERTY_FILE = "quartz.properties";
    private static final String QUARTZ_JOB_CONFIG_FILE = "quartz_jobs.xml";
    private volatile Scheduler scheduler;
    private static volatile MetricsPeriodicJobScheduler instance;


    private MetricsPeriodicJobScheduler() {

        initScheduler();
    }

    /**
     * Get an instance of the PeriodicalConsentJobScheduler. It implements a double checked locking initialization.
     *
     * @return PeriodicalConsentJobScheduler instance
     */
    public static synchronized MetricsPeriodicJobScheduler getInstance() {

        if (instance == null) {
            synchronized (MetricsPeriodicJobScheduler.class) {
                if (instance == null) {
                    instance = new MetricsPeriodicJobScheduler();
                }
            }
        }
        return instance;
    }

    public void initScheduler() {

        if (instance != null) {
            return;
        }

        try {
            // Load properties file if exists. Else scheduler factory will be initialized with default configs. Add
            // the properties file to set up clustering and to configure the job configuration file.
            // The jobs and triggers will be configured from the xml file.
            String quartzConfigFile = Paths.get(CarbonUtils.getCarbonConfigDirPath()).toString() + "/"
                    + QUARTZ_PROPERTY_FILE;
            String jobConfigFile = Paths.get(CarbonUtils.getCarbonConfigDirPath()).toString() + "/"
                    + QUARTZ_JOB_CONFIG_FILE;
            boolean isCustomQuartzConfigDefined = new File(quartzConfigFile).exists();
            boolean isQuartzJobConfigExists = new File(jobConfigFile).exists();

            if (isCustomQuartzConfigDefined) {
                StdSchedulerFactory stdSchedulerFactory = new StdSchedulerFactory();
                stdSchedulerFactory.initialize(quartzConfigFile);
                scheduler = stdSchedulerFactory.getScheduler();
            } else {
                scheduler = StdSchedulerFactory.getDefaultScheduler();
            }

            // Specify the job configuration file dynamically
            if (isQuartzJobConfigExists) {
                ClassLoadHelper classLoadHelper = new SimpleClassLoadHelper();
                XMLSchedulingDataProcessor processor = new XMLSchedulingDataProcessor(classLoadHelper);
                processor.processFileAndScheduleJobs(jobConfigFile, scheduler);
                scheduler.start();
            } else {
                log.error("Quartz job configuration file is not found in the path: " + jobConfigFile);
            }

        } catch (Exception e) {
            log.error("Error while initializing MetricsPeriodicJobScheduler", e);
        }
    }

    /**
     * Returns the scheduler
     *
     * @return Scheduler scheduler.
     */
    public Scheduler getScheduler() {

        return scheduler;
    }
}

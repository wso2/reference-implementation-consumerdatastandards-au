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

package org.wso2.openbanking.cds.metrics.service;

import net.minidev.json.JSONObject;
import org.wso2.openbanking.cds.metrics.util.DateTimeUtil;
import org.wso2.openbanking.cds.metrics.util.PeriodEnum;
import org.wso2.openbanking.cds.metrics.util.TimeGranularityEnum;

/**
 * Implementation of Metrics Query Creator for CDS Metrics V5.
 * This class will initialize a query creator with timestamps relevant to the given period.
 */
public class MetricsV5QueryCreatorImpl implements MetricsQueryCreator {

    private final String fromTimestamp;
    private final String toTimestamp;
    private final long fromTimestampEpochSecond;
    private final long toTimestampEpochSecond;
    private final long fromTimestampEpochMilliSecond;
    private final long toTimestampEpochMilliSecond;
    private final long availabilityFromTimestamp;
    private final long availabilityToTimestamp;
    private final String dailyTimeGranularity;
    private final String hourlyTimeGranularity;

    public MetricsV5QueryCreatorImpl(PeriodEnum period) {

        String[] timeRangeArray = DateTimeUtil.getTimeRange(period);
        String[] availabilityTimeRangeArray = DateTimeUtil.getAvailabilityMetricsTimeRange(period);
        this.dailyTimeGranularity = TimeGranularityEnum.DAYS.toString();
        this.hourlyTimeGranularity = TimeGranularityEnum.HOURS.toString();
        this.fromTimestamp = timeRangeArray[0];
        this.toTimestamp = timeRangeArray[1];
        this.fromTimestampEpochSecond = DateTimeUtil.getEpochTimestamp(fromTimestamp);
        this.toTimestampEpochSecond = DateTimeUtil.getEpochTimestamp(toTimestamp);
        this.fromTimestampEpochMilliSecond = fromTimestampEpochSecond * 1000;
        this.toTimestampEpochMilliSecond = toTimestampEpochSecond * 1000;
        this.availabilityFromTimestamp = DateTimeUtil.getEpochTimestamp(availabilityTimeRangeArray[0]);
        this.availabilityToTimestamp = DateTimeUtil.getEpochTimestamp(availabilityTimeRangeArray[1]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAvailabilityMetricsQuery() {

        return "from SERVER_OUTAGES_RAW_DATA select OUTAGE_ID, TIMESTAMP, TYPE, TIME_FROM, TIME_TO, ASPECT group by " +
                "OUTAGE_ID, TIMESTAMP, TYPE, TIME_FROM, TIME_TO, ASPECT having TIME_FROM >= "
                + availabilityFromTimestamp + " AND TIME_TO <= " + availabilityToTimestamp + ";";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInvocationMetricsQuery() {

        return "from CDSMetricsAgg within '" + fromTimestamp + "', '" + toTimestamp + "' per '" + dailyTimeGranularity +
                "' select priorityTier, totalReqCount, AGG_TIMESTAMP group by priorityTier, AGG_TIMESTAMP " +
                "having priorityTier != 'Uncategorized' order by AGG_TIMESTAMP desc;";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInvocationByAspectMetricsQuery() {

        return "from CDSMetricsAspectAgg within '" + fromTimestamp + "', '" + toTimestamp + "' per '" +
                dailyTimeGranularity + "' select aspect, totalReqCount, AGG_TIMESTAMP group by aspect, AGG_TIMESTAMP " +
                "having aspect != 'uncategorized' order by AGG_TIMESTAMP desc;";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHourlyPerformanceByPriorityMetricsQuery() {

        return "from CDSMetricsPerfPriorityAgg within '" + fromTimestamp + "', '" + toTimestamp + "' per '" +
                hourlyTimeGranularity + "' select priorityTier, AGG_TIMESTAMP, " +
                "withinThresholdCount/totalReqCount as performance group by priorityTier, AGG_TIMESTAMP " +
                "having priorityTier != 'Uncategorized' order by AGG_TIMESTAMP asc;";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSessionCountMetricsQuery() {

        return "from CDSMetricsSessionAgg within '" + fromTimestamp + "', '" + toTimestamp + "' per '" +
                dailyTimeGranularity + "' select sessionCount, AGG_TIMESTAMP;";
    }

    /**
     * {@inheritDoc}
     * This method is not implemented for CDS Metrics V3 since we are getting PeakTPS from a siddhi app.
     */
    @Override
    public String getPeakTPSMetricsQuery() {
        return "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONObject getPeakTPSMetricsEvent() {

        JSONObject event = new JSONObject();
        event.put("start_time", fromTimestampEpochSecond);
        event.put("end_time", toTimestampEpochSecond);
        return event;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getErrorMetricsQuery() {

        return "from CDSMetricsStatusAgg within '" + fromTimestamp + "', '" + toTimestamp + "' per '" +
                dailyTimeGranularity + "' select totalReqCount, AGG_TIMESTAMP having statusCode >= 500 and " +
                "statusCode < 600;";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getErrorByAspectMetricsQuery() {

        return "from CDSMetricsStatusAspectAgg within '" + fromTimestamp + "', '" + toTimestamp + "' per '" +
                dailyTimeGranularity + "' select AGG_TIMESTAMP, statusCode, aspect, totalReqCount " +
                "having aspect != 'uncategorized' and statusCode >= 400 and statusCode < 600 " +
                "order by AGG_TIMESTAMP desc;";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRejectionMetricsQuery() {

        return "from API_INVOCATION_RAW_DATA on str:contains(API_NAME, 'ConsumerDataStandards') and " +
                "STATUS_CODE == 429 and TIMESTAMP > " + fromTimestampEpochSecond + " and TIMESTAMP < " +
                toTimestampEpochSecond + " select count(STATUS_CODE) as throttledOutCount, TIMESTAMP, CONSUMER_ID " +
                "group by TIMESTAMP, CONSUMER_ID";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getActiveAuthorisationCountMetricsQuery() {

        return "from AUTHORISATION_METRICS_DATA select CONSENT_ID, CONSENT_STATUS, CUSTOMER_PROFILE, " +
                "CONSENT_DURATION_TYPE, TIMESTAMP, AUTH_FLOW_TYPE " +
                "group by CONSENT_ID, CONSENT_STATUS, CUSTOMER_PROFILE, CONSENT_DURATION_TYPE, TIMESTAMP, " +
                "AUTH_FLOW_TYPE " +
                "having CONSENT_DURATION_TYPE == 'ongoing' AND AUTH_FLOW_TYPE == 'consentAuthorisation';";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAuthorisationMetricsQuery() {

        return "from CDSAuthorisationMetricsAgg within '" + fromTimestamp + "', '" + toTimestamp + "' per '" +
                dailyTimeGranularity + "' select * order by AGG_TIMESTAMP desc;";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAbandonedConsentFlowCountMetricsQuery() {

        return "from ABANDONED_CONSENT_FLOW_METRICS_DATA select REQUEST_URI_KEY, STAGE, TIMESTAMP " +
                "group by REQUEST_URI_KEY, STAGE, TIMESTAMP having TIMESTAMP > " +
                fromTimestampEpochMilliSecond + "l AND TIMESTAMP < " + toTimestampEpochMilliSecond + "l;";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRecipientCountMetricsQuery() {

        return "from CDSMetricsCustomerRecipientSummary on ACTIVE_AUTH_COUNT>0 select distinctCount(CLIENT_ID) " +
                "as recipientCount;";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCustomerCountMetricsQuery() {

        return "from CDSMetricsCustomerRecipientSummary on ACTIVE_AUTH_COUNT > 0 select distinctCount(USER_ID) as " +
                "customerCount;";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSuccessfulInvocationsQuery() {

        return "from CDSMetricsPerfAgg within '" + fromTimestamp + "', '" + toTimestamp + "' per '" +
                dailyTimeGranularity + "' select totalReqCount, AGG_TIMESTAMP having withinThreshold == true;";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTotalResponseTimeQuery() {

        return "from CDSMetricsAgg within '" + fromTimestamp + "', '" + toTimestamp + "' per '" + dailyTimeGranularity +
                "' select priorityTier, (totalRespTime / 1000.0) as totalRespTime, AGG_TIMESTAMP " +
                "group by priorityTier, AGG_TIMESTAMP having priorityTier != 'Uncategorized' " +
                "order by AGG_TIMESTAMP desc;";
    }

}

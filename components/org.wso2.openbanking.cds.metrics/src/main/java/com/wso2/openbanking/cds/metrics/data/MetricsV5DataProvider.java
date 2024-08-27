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

package org.wso2.openbanking.cds.metrics.data;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;
import org.wso2.openbanking.cds.metrics.constants.MetricsConstants;
import org.wso2.openbanking.cds.metrics.service.MetricsQueryCreator;
import org.wso2.openbanking.cds.metrics.util.SPQueryExecutorUtil;

import java.io.IOException;

import static org.wso2.openbanking.cds.metrics.constants.MetricsConstants.RETRIEVAL_ERROR;

/**
 * Implementation of MetricsDataProvider interface.
 * This class provides data required to calculate metrics by interacting with WSO2 Streaming Integrator.
 * This class is excluded from code coverage since it requires an external dependency to function.
 */
public class MetricsV5DataProvider implements MetricsDataProvider {

    MetricsQueryCreator metricsQueryCreator;
    private static final OpenBankingCDSConfigParser configParser = OpenBankingCDSConfigParser.getInstance();
    private static final String tpsDataRetrievalUrl = configParser.getMetricsTPSDataRetrievalUrl();
    private static final Log log = LogFactory.getLog(MetricsV5DataProvider.class);

    public MetricsV5DataProvider(MetricsQueryCreator metricsQueryCreator) {
        this.metricsQueryCreator = metricsQueryCreator;
    }

    @Override
    public JSONObject getAvailabilityMetricsData() throws OpenBankingException {

        JSONObject availabilityMetricsJsonObject;
        String spQuery = metricsQueryCreator.getAvailabilityMetricsQuery();
        try {
            availabilityMetricsJsonObject = SPQueryExecutorUtil.executeQueryOnStreamProcessor(
                    MetricsConstants.CDS_AVAILABILITY_METRICS_APP, spQuery);
        } catch (ParseException | IOException e) {
            String errorMessage = String.format(RETRIEVAL_ERROR, MetricsConstants.AVAILABILITY);
            log.error(errorMessage, e);
            throw new OpenBankingException(errorMessage, e);
        }
        return availabilityMetricsJsonObject;
    }

    @Override
    public JSONObject getInvocationMetricsData() throws OpenBankingException {

        JSONObject invocationMetricsJsonObject;
        String spQuery = metricsQueryCreator.getInvocationMetricsQuery();

        try {
            invocationMetricsJsonObject = SPQueryExecutorUtil.executeQueryOnStreamProcessor(
                    MetricsConstants.CDS_INVOCATION_METRICS_APP, spQuery);
        } catch (ParseException | IOException e) {
            String errorMessage = String.format(RETRIEVAL_ERROR, MetricsConstants.INVOCATION);
            log.error(errorMessage, e);
            throw new OpenBankingException(errorMessage, e);
        }
        return invocationMetricsJsonObject;
    }

    @Override
    public JSONObject getInvocationByAspectMetricsData() throws OpenBankingException {

        JSONObject invocationByAspectMetricsJsonObject;
        String spQuery = metricsQueryCreator.getInvocationByAspectMetricsQuery();

        try {
            invocationByAspectMetricsJsonObject = SPQueryExecutorUtil.executeQueryOnStreamProcessor(
                    MetricsConstants.CDS_INVOCATION_METRICS_APP, spQuery);
        } catch (ParseException | IOException e) {
            String errorMessage = String.format(RETRIEVAL_ERROR, MetricsConstants.INVOCATION);
            log.error(errorMessage, e);
            throw new OpenBankingException(errorMessage, e);
        }
        return invocationByAspectMetricsJsonObject;
    }

    @Override
    public JSONObject getHourlyPerformanceByPriorityMetricsData() throws OpenBankingException {

        JSONObject performanceMetricsJsonObject;
        String spQuery = metricsQueryCreator.getHourlyPerformanceByPriorityMetricsQuery();

        try {
            performanceMetricsJsonObject = SPQueryExecutorUtil.executeQueryOnStreamProcessor(
                    MetricsConstants.CDS_INVOCATION_METRICS_APP, spQuery);
        } catch (ParseException | IOException e) {
            String errorMessage = String.format(RETRIEVAL_ERROR, MetricsConstants.PERFORMANCE);
            log.error(errorMessage, e);
            throw new OpenBankingException(errorMessage, e);
        }
        return performanceMetricsJsonObject;
    }

    @Override
    public JSONObject getSessionCountMetricsData() throws OpenBankingException {

        JSONObject sessionCountMetricsJsonObject;
        String spQuery = metricsQueryCreator.getSessionCountMetricsQuery();

        try {
            sessionCountMetricsJsonObject = SPQueryExecutorUtil.executeQueryOnStreamProcessor(
                    MetricsConstants.CDS_SESSION_METRICS_APP, spQuery);
        } catch (ParseException | IOException e) {
            String errorMessage = String.format(RETRIEVAL_ERROR, MetricsConstants.SESSION_COUNT);
            log.error(errorMessage, e);
            throw new OpenBankingException(errorMessage, e);
        }
        return sessionCountMetricsJsonObject;
    }

    @Override
    public JSONArray getPeakTPSMetricsData() throws ParseException {

        JSONObject peakTPSEvent = metricsQueryCreator.getPeakTPSMetricsEvent();
        String responseStr = SPQueryExecutorUtil.executeRequestOnStreamProcessor(peakTPSEvent, tpsDataRetrievalUrl);
        //ToDO: Address vulnerable usage of JSONParser
        Object jsonResponse = new JSONParser(JSONParser.MODE_PERMISSIVE).parse(responseStr);

        if (jsonResponse instanceof JSONArray) {
            return (JSONArray) jsonResponse;
        } else if (jsonResponse instanceof JSONObject) {
            JSONObject jsonResponseObt = (JSONObject) jsonResponse;
            JSONArray responseArray = new JSONArray();
            responseArray.add(jsonResponseObt);
            return responseArray;
        } else {
            return new JSONArray();
        }
    }

    @Override
    public JSONObject getErrorMetricsData() throws OpenBankingException {

        JSONObject errorMetricsJsonObject;
        String spQuery = metricsQueryCreator.getErrorMetricsQuery();

        try {
            errorMetricsJsonObject = SPQueryExecutorUtil.executeQueryOnStreamProcessor(
                    MetricsConstants.CDS_INVOCATION_METRICS_APP, spQuery);
        } catch (ParseException | IOException e) {
            String errorMessage = String.format(RETRIEVAL_ERROR, MetricsConstants.ERROR);
            log.error(errorMessage, e);
            throw new OpenBankingException(errorMessage, e);
        }
        return errorMetricsJsonObject;
    }

    @Override
    public JSONObject getErrorByAspectMetricsData() throws OpenBankingException {

        JSONObject errorMetricsJsonObject;
        String spQuery = metricsQueryCreator.getErrorByAspectMetricsQuery();

        try {
            errorMetricsJsonObject = SPQueryExecutorUtil.executeQueryOnStreamProcessor(
                    MetricsConstants.CDS_INVOCATION_METRICS_APP, spQuery);
        } catch (ParseException | IOException e) {
            String errorMessage = String.format(RETRIEVAL_ERROR, MetricsConstants.ERROR);
            log.error(errorMessage, e);
            throw new OpenBankingException(errorMessage, e);
        }
        return errorMetricsJsonObject;
    }

    @Override
    public JSONObject getRejectionMetricsData() throws OpenBankingException {

        JSONObject rejectionMetricsJsonObject;
        String spQuery = metricsQueryCreator.getRejectionMetricsQuery();

        try {
            rejectionMetricsJsonObject = SPQueryExecutorUtil.executeQueryOnStreamProcessor(
                    MetricsConstants.API_RAW_DATA_SUBMISSION_APP, spQuery);
        } catch (ParseException | IOException e) {
            String errorMessage = String.format(RETRIEVAL_ERROR, MetricsConstants.REJECTION);
            log.error(errorMessage, e);
            throw new OpenBankingException(errorMessage, e);
        }
        return rejectionMetricsJsonObject;
    }

    @Override
    public JSONObject getActiveAuthorisationCountMetricsData() throws OpenBankingException {

        JSONObject activeAuthorisationCountMetricsJsonObject;
        String spQuery = metricsQueryCreator.getActiveAuthorisationCountMetricsQuery();

        try {
            activeAuthorisationCountMetricsJsonObject = SPQueryExecutorUtil.executeQueryOnStreamProcessor(
                    MetricsConstants.CDS_AUTHORISATION_METRICS_APP, spQuery);
        } catch (ParseException | IOException e) {
            String errorMessage = String.format(RETRIEVAL_ERROR, MetricsConstants.AUTHORISATION);
            log.error(errorMessage, e);
            throw new OpenBankingException(errorMessage, e);
        }
        return activeAuthorisationCountMetricsJsonObject;
    }

    @Override
    public JSONObject getAuthorisationMetricsData() throws OpenBankingException {

        JSONObject authorisationMetricsJsonObject;
        String spQuery = metricsQueryCreator.getAuthorisationMetricsQuery();

        try {
            authorisationMetricsJsonObject = SPQueryExecutorUtil.executeQueryOnStreamProcessor(
                    MetricsConstants.CDS_AUTHORISATION_METRICS_APP, spQuery);
        } catch (ParseException | IOException e) {
            String errorMessage = String.format(RETRIEVAL_ERROR, MetricsConstants.AUTHORISATION);
            log.error(errorMessage, e);
            throw new OpenBankingException(errorMessage, e);
        }
        return authorisationMetricsJsonObject;
    }

    @Override
    public JSONObject getAbandonedConsentFlowCountMetricsData() throws OpenBankingException {

        JSONObject abandonedConsentFlowCountMetricsJsonObject;
        String spQuery = metricsQueryCreator.getAbandonedConsentFlowCountMetricsQuery();

        try {
            abandonedConsentFlowCountMetricsJsonObject = SPQueryExecutorUtil.executeQueryOnStreamProcessor(
                    MetricsConstants.CDS_AUTHORISATION_METRICS_APP, spQuery);
        } catch (ParseException | IOException e) {
            String errorMessage = String.format(RETRIEVAL_ERROR, MetricsConstants.AUTHORISATION);
            log.error(errorMessage, e);
            throw new OpenBankingException(errorMessage, e);
        }
        return abandonedConsentFlowCountMetricsJsonObject;
    }

    @Override
    public JSONObject getRecipientCountMetricsData() throws OpenBankingException {

        JSONObject recipientCountMetricsJsonObject;
        String spQuery = metricsQueryCreator.getRecipientCountMetricsQuery();

        try {
            recipientCountMetricsJsonObject = SPQueryExecutorUtil.executeQueryOnStreamProcessor(
                    MetricsConstants.CDS_CUSTOMER_RECIPIENT_METRICS_APP, spQuery);
        } catch (ParseException | IOException e) {
            String errorMessage = String.format(RETRIEVAL_ERROR, MetricsConstants.RECIPIENT_COUNT);
            log.error(errorMessage, e);
            throw new OpenBankingException(errorMessage, e);
        }
        return recipientCountMetricsJsonObject;
    }

    @Override
    public JSONObject getCustomerCountMetricsData() throws OpenBankingException {

        JSONObject customerCountMetricsJsonObject;
        String spQuery = metricsQueryCreator.getCustomerCountMetricsQuery();

        try {
            customerCountMetricsJsonObject = SPQueryExecutorUtil.executeQueryOnStreamProcessor(
                    MetricsConstants.CDS_CUSTOMER_RECIPIENT_METRICS_APP, spQuery);
        } catch (ParseException | IOException e) {
            String errorMessage = String.format(RETRIEVAL_ERROR, MetricsConstants.CUSTOMER_COUNT);
            log.error(errorMessage, e);
            throw new OpenBankingException(errorMessage, e);
        }
        return customerCountMetricsJsonObject;
    }

    @Override
    public JSONObject getTotalResponseTimeMetricsData() throws OpenBankingException {

        JSONObject totalResponseTimeJsonObject;
        String spQuery = metricsQueryCreator.getTotalResponseTimeQuery();

        try {
            totalResponseTimeJsonObject = SPQueryExecutorUtil.executeQueryOnStreamProcessor(
                    MetricsConstants.CDS_INVOCATION_METRICS_APP, spQuery);
        } catch (ParseException | IOException e) {
            String errorMessage = String.format(RETRIEVAL_ERROR, MetricsConstants.TOTAL_RESPONSE_TIME);
            log.error(errorMessage, e);
            throw new OpenBankingException(errorMessage, e);
        }
        return totalResponseTimeJsonObject;
    }

    @Override
    public JSONObject getSuccessfulInvocationMetricsData() throws OpenBankingException {

        JSONObject successInvocationsJsonObject;
        String spQuery = metricsQueryCreator.getSuccessfulInvocationsQuery();

        try {
            successInvocationsJsonObject = SPQueryExecutorUtil.executeQueryOnStreamProcessor(
                    MetricsConstants.CDS_INVOCATION_METRICS_APP, spQuery);
        } catch (ParseException | IOException e) {
            String errorMessage = String.format(RETRIEVAL_ERROR, MetricsConstants.SUCCESSFUL_INVOCATIONS);
            log.error(errorMessage, e);
            throw new OpenBankingException(errorMessage, e);
        }
        return successInvocationsJsonObject;
    }
}

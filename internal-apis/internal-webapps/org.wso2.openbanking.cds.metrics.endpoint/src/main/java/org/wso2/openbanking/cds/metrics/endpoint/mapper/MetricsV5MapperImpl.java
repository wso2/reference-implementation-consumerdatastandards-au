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

package org.wso2.openbanking.cds.metrics.endpoint.mapper;

import org.wso2.openbanking.cds.metrics.endpoint.model.v5.AuthorisationMetricsV2AbandonedConsentFlowCountDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.AuthorisationMetricsV2AbandonmentsByStageDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.AuthorisationMetricsV2AbandonmentsByStageFailedTokenExchangeDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.AuthorisationMetricsV2AbandonmentsByStagePreAccountSelectionDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.AuthorisationMetricsV2AbandonmentsByStagePreAuthenticationDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.AuthorisationMetricsV2AbandonmentsByStagePreAuthorisationDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.AuthorisationMetricsV2AbandonmentsByStagePreIdentificationDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.AuthorisationMetricsV2AbandonmentsByStageRejectedDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.AuthorisationMetricsV2ActiveAuthorisationCountDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.AuthorisationMetricsV2AmendedAuthorisationCountDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.AuthorisationMetricsV2AuthorisationCountDayDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.AuthorisationMetricsV2DTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.AuthorisationMetricsV2ExpiredAuthorisationCountDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.AuthorisationMetricsV2NewAuthorisationCountDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.AuthorisationMetricsV2NewAuthorisationCountDayDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.AuthorisationMetricsV2RevokedAuthorisationCountDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.AvailabilityMetricsV2AggregateDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.AvailabilityMetricsV2AuthenticatedDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.AvailabilityMetricsV2DTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.AvailabilityMetricsV2UnauthenticatedDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.AverageResponseMetricsV2DTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.AverageResponseMetricsV2HighPriorityDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.AverageResponseMetricsV2LargePayloadDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.AverageResponseMetricsV2LowPriorityDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.AverageResponseMetricsV2UnattendedDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.AverageResponseMetricsV2UnauthenticatedDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.AverageTPSMetricsV2AggregateDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.AverageTPSMetricsV2AuthenticatedDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.AverageTPSMetricsV2DTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.AverageTPSMetricsV2UnauthenticatedDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.ErrorMetricsV2AggregateDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.ErrorMetricsV2AuthenticatedDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.ErrorMetricsV2DTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.ErrorMetricsV2UnauthenticatedDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.InvocationMetricsV3DTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.InvocationMetricsV3HighPriorityDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.InvocationMetricsV3LargePayloadDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.InvocationMetricsV3LowPriorityDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.InvocationMetricsV3UnattendedDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.InvocationMetricsV3UnauthenticatedDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.LinksDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.PeakTPSMetricsV2AggregateDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.PeakTPSMetricsV2AuthenticatedDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.PeakTPSMetricsV2DTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.PeakTPSMetricsV2UnauthenticatedDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.PerformanceMetricsV3AggregateDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.PerformanceMetricsV3DTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.PerformanceMetricsV3HighPriorityDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.PerformanceMetricsV3LargePayloadDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.PerformanceMetricsV3LowPriorityDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.PerformanceMetricsV3UnattendedDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.PerformanceMetricsV3UnauthenticatedDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.RejectionMetricsV3AuthenticatedDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.RejectionMetricsV3DTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.RejectionMetricsV3UnauthenticatedDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.ResponseMetricsListV5DTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.ResponseMetricsListV5DataDTO;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.SessionCountMetricsV2DTO;
import org.wso2.openbanking.cds.metrics.endpoint.util.CommonUtil;
import org.wso2.openbanking.cds.metrics.model.AuthorisationMetric;
import org.wso2.openbanking.cds.metrics.model.CustomerTypeCount;
import org.wso2.openbanking.cds.metrics.model.MetricsResponseModel;
import org.wso2.openbanking.cds.metrics.util.PeriodEnum;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class containing methods to map MetricsResponseModel to Metrics V5 API Response DTO.
 */
public class MetricsV5MapperImpl implements MetricsMapper {

    /**
     * {@inheritDoc}
     */
    public ResponseMetricsListV5DTO getResponseMetricsListDTO(MetricsResponseModel metricsListModel,
                                                                String period) {

        ResponseMetricsListV5DTO metricsListV5DTO = new ResponseMetricsListV5DTO();
        LinksDTO linksDTO = new LinksDTO();
        PeriodEnum periodEnum = PeriodEnum.fromString(period);

        ResponseMetricsListV5DataDTO metricsListV5DataDTO =
                getResponseMetricsListV5DataDTO(metricsListModel, periodEnum);
        metricsListV5DTO.setData(metricsListV5DataDTO);
        linksDTO.setSelf(CommonUtil.getCDSAdminSelfLink(period));
        metricsListV5DTO.setLinks(linksDTO);

        return metricsListV5DTO;
    }

    /**
     * Get ResponseMetricsListV5DataDTO from MetricsResponseModel and period enum.
     *
     * @param metricsListModel - Metrics model
     * @param period           - period enum
     * @return ResponseMetricsListV5DataDTO
     */
    private ResponseMetricsListV5DataDTO getResponseMetricsListV5DataDTO(MetricsResponseModel metricsListModel
            , PeriodEnum period) {

        ResponseMetricsListV5DataDTO responseMetricsListV5DataDTO = new ResponseMetricsListV5DataDTO();
        responseMetricsListV5DataDTO.setRequestTime(metricsListModel.getRequestTime());
        responseMetricsListV5DataDTO.setCustomerCount(metricsListModel.getCustomerCount());
        responseMetricsListV5DataDTO.setRecipientCount(metricsListModel.getRecipientCount());
        responseMetricsListV5DataDTO.setInvocations(getInvocationsDTO(metricsListModel, period));
        responseMetricsListV5DataDTO.setAverageResponse(getAverageResponseDTO(metricsListModel, period));
        responseMetricsListV5DataDTO.setSessionCount(getSessionCountDTO(metricsListModel.getSessionCount(), period));
        responseMetricsListV5DataDTO.setRejections(getRejectionsDTO(metricsListModel, period));
        responseMetricsListV5DataDTO.setAvailability(getAvailabilityDTO(metricsListModel, period));
        responseMetricsListV5DataDTO.setAverageTps(getAverageTpsDTO(metricsListModel, period));
        responseMetricsListV5DataDTO.setPeakTps(getPeakTpsDTO(metricsListModel, period));
        responseMetricsListV5DataDTO.setPerformance(getPerformanceDTO(metricsListModel, period));
        responseMetricsListV5DataDTO.setErrors(getErrorsDTO(metricsListModel, period));
        responseMetricsListV5DataDTO.setAuthorisations(getAuthorisationsDTO(metricsListModel, period));

        return responseMetricsListV5DataDTO;
    }

    private AvailabilityMetricsV2DTO getAvailabilityDTO(MetricsResponseModel metricsListModel, PeriodEnum period) {

        AvailabilityMetricsV2DTO availabilityMetricsDTO = new AvailabilityMetricsV2DTO();

        AvailabilityMetricsV2AggregateDTO aggregate = new AvailabilityMetricsV2AggregateDTO();
        AvailabilityMetricsV2UnauthenticatedDTO unauthenticated = new AvailabilityMetricsV2UnauthenticatedDTO();
        AvailabilityMetricsV2AuthenticatedDTO authenticated = new AvailabilityMetricsV2AuthenticatedDTO();

        List<BigDecimal> allAvailability = metricsListModel.getAvailability();
        List<BigDecimal> authenticatedAvailability = metricsListModel.getAuthenticatedAvailability();
        List<BigDecimal> unauthenticatedAvailability = metricsListModel.getUnauthenticatedAvailability();

        if (PeriodEnum.ALL == period || PeriodEnum.CURRENT == period) {
            aggregate.setCurrentMonth(allAvailability.remove(0).toString());
            authenticated.setCurrentMonth(authenticatedAvailability.remove(0).toString());
            unauthenticated.setCurrentMonth(unauthenticatedAvailability.remove(0).toString());
        }
        if (PeriodEnum.ALL == period || PeriodEnum.HISTORIC == period) {
            aggregate.setPreviousMonths(CommonUtil.convertToStringListWithScale(CommonUtil
                    .addMissingMonths(allAvailability)));
            authenticated.setPreviousMonths(CommonUtil.convertToStringListWithScale(CommonUtil
                    .addMissingMonths(authenticatedAvailability)));
            unauthenticated.setPreviousMonths(CommonUtil.convertToStringListWithScale(CommonUtil
                    .addMissingMonths(unauthenticatedAvailability)));
        }

        availabilityMetricsDTO.setAuthenticated(authenticated);
        availabilityMetricsDTO.setUnauthenticated(unauthenticated);
        availabilityMetricsDTO.setAggregate(aggregate);

        return availabilityMetricsDTO;
    }

    private PerformanceMetricsV3DTO getPerformanceDTO(MetricsResponseModel metricsListModel, PeriodEnum period) {

        PerformanceMetricsV3DTO performanceMetricsDTO = new PerformanceMetricsV3DTO();

        performanceMetricsDTO.setAggregate(getAggregatePerformanceDTO(metricsListModel
                .getPerformance(), period));
        performanceMetricsDTO.setHighPriority(getHighPriorityPerformanceDTO(metricsListModel
                .getPerformanceHighPriority(), period));
        performanceMetricsDTO.setLargePayload(getLargePayloadPerformanceDTO(metricsListModel
                .getPerformanceLargePayload(), period));
        performanceMetricsDTO.setLowPriority(getLowPriorityPerformanceDTO(metricsListModel
                .getPerformanceLowPriority(), period));
        performanceMetricsDTO.setUnattended(getUnattendedPerformanceDTO(metricsListModel
                .getPerformanceUnattended(), period));
        performanceMetricsDTO.setUnauthenticated(getUnauthenticatedPerformanceDTO(metricsListModel
                .getPerformanceUnauthenticated(), period));

        return performanceMetricsDTO;
    }

    private PerformanceMetricsV3AggregateDTO getAggregatePerformanceDTO(List<BigDecimal> performanceList,
                                                                        PeriodEnum period) {

        List<String> performanceStringList = CommonUtil.convertToStringList(performanceList);

        PerformanceMetricsV3AggregateDTO performanceMetricsAggregateDTO = new PerformanceMetricsV3AggregateDTO();
        if (!performanceStringList.isEmpty() && (PeriodEnum.ALL == period || PeriodEnum.CURRENT == period)) {
            performanceMetricsAggregateDTO.setCurrentDay(performanceStringList.remove(0));
        }
        if (PeriodEnum.ALL == period || PeriodEnum.HISTORIC == period) {
            performanceMetricsAggregateDTO.setPreviousDays(performanceStringList);
        }
        return performanceMetricsAggregateDTO;
    }

    private PerformanceMetricsV3HighPriorityDTO getHighPriorityPerformanceDTO(
            List<List<BigDecimal>> nestedPerformanceList, PeriodEnum period) {

        List<List<String>> nestedPerformanceStringList = CommonUtil.convertToNestedStringList(nestedPerformanceList);

        PerformanceMetricsV3HighPriorityDTO performanceMetricsHighPriorityDTO =
                new PerformanceMetricsV3HighPriorityDTO();
        if (!nestedPerformanceStringList.isEmpty() && (PeriodEnum.ALL == period || PeriodEnum.CURRENT == period)) {
            performanceMetricsHighPriorityDTO.setCurrentDay(nestedPerformanceStringList.remove(0));
        }
        if (PeriodEnum.ALL == period || PeriodEnum.HISTORIC == period) {
            performanceMetricsHighPriorityDTO.setPreviousDays(nestedPerformanceStringList);
        }
        return performanceMetricsHighPriorityDTO;
    }

    private PerformanceMetricsV3LargePayloadDTO getLargePayloadPerformanceDTO(
            List<List<BigDecimal>> nestedPerformanceList, PeriodEnum period) {

        List<List<String>> nestedPerformanceStringList = CommonUtil.convertToNestedStringList(nestedPerformanceList);

        PerformanceMetricsV3LargePayloadDTO performanceMetricsLargePayloadDTO =
                new PerformanceMetricsV3LargePayloadDTO();
        if (!nestedPerformanceStringList.isEmpty() && (PeriodEnum.ALL == period || PeriodEnum.CURRENT == period)) {
            performanceMetricsLargePayloadDTO.setCurrentDay(nestedPerformanceStringList.remove(0));
        }
        if (PeriodEnum.ALL == period || PeriodEnum.HISTORIC == period) {
            performanceMetricsLargePayloadDTO.setPreviousDays(nestedPerformanceStringList);
        }
        return performanceMetricsLargePayloadDTO;
    }

    private PerformanceMetricsV3LowPriorityDTO getLowPriorityPerformanceDTO(
            List<List<BigDecimal>> nestedPerformanceList, PeriodEnum period) {

        List<List<String>> nestedPerformanceStringList = CommonUtil.convertToNestedStringList(nestedPerformanceList);

        PerformanceMetricsV3LowPriorityDTO performanceMetricsLowPriorityDTO =
                new PerformanceMetricsV3LowPriorityDTO();
        if (!nestedPerformanceStringList.isEmpty() && (PeriodEnum.ALL == period || PeriodEnum.CURRENT == period)) {
            performanceMetricsLowPriorityDTO.setCurrentDay(nestedPerformanceStringList.remove(0));
        }
        if (PeriodEnum.ALL == period || PeriodEnum.HISTORIC == period) {
            performanceMetricsLowPriorityDTO.setPreviousDays(nestedPerformanceStringList);
        }
        return performanceMetricsLowPriorityDTO;
    }

    private PerformanceMetricsV3UnattendedDTO getUnattendedPerformanceDTO(
            List<List<BigDecimal>> nestedPerformanceList, PeriodEnum period) {

        List<List<String>> nestedPerformanceStringList = CommonUtil.convertToNestedStringList(nestedPerformanceList);

        PerformanceMetricsV3UnattendedDTO performanceMetricsUnattendedDTO =
                new PerformanceMetricsV3UnattendedDTO();
        if (!nestedPerformanceStringList.isEmpty() && (PeriodEnum.ALL == period || PeriodEnum.CURRENT == period)) {
            performanceMetricsUnattendedDTO.setCurrentDay(nestedPerformanceStringList.remove(0));
        }
        if (PeriodEnum.ALL == period || PeriodEnum.HISTORIC == period) {
            performanceMetricsUnattendedDTO.setPreviousDays(nestedPerformanceStringList);
        }
        return performanceMetricsUnattendedDTO;
    }

    private PerformanceMetricsV3UnauthenticatedDTO getUnauthenticatedPerformanceDTO(
            List<List<BigDecimal>> nestedPerformanceList, PeriodEnum period) {

        List<List<String>> nestedPerformanceStringList = CommonUtil.convertToNestedStringList(nestedPerformanceList);

        PerformanceMetricsV3UnauthenticatedDTO performanceMetricsUnauthenticatedDTO =
                new PerformanceMetricsV3UnauthenticatedDTO();
        if (!nestedPerformanceStringList.isEmpty() && (PeriodEnum.ALL == period || PeriodEnum.CURRENT == period)) {
            performanceMetricsUnauthenticatedDTO.setCurrentDay(nestedPerformanceStringList.remove(0));
        }
        if (PeriodEnum.ALL == period || PeriodEnum.HISTORIC == period) {
            performanceMetricsUnauthenticatedDTO.setPreviousDays(nestedPerformanceStringList);
        }
        return performanceMetricsUnauthenticatedDTO;
    }

    private InvocationMetricsV3DTO getInvocationsDTO(MetricsResponseModel metricsListModel, PeriodEnum period) {

        InvocationMetricsV3DTO invocationMetricsDTO = new InvocationMetricsV3DTO();

        invocationMetricsDTO.setUnauthenticated(getInvocationMetricsUnauthenticatedDTO(
                metricsListModel.getInvocationUnauthenticated(), period));
        invocationMetricsDTO.setHighPriority(getInvocationMetricsHighPriorityDTO(
                metricsListModel.getInvocationHighPriority(), period));
        invocationMetricsDTO.setLowPriority(getInvocationMetricsLowPriorityDTO(
                metricsListModel.getInvocationLowPriority(), period));
        invocationMetricsDTO.setUnattended(getInvocationMetricsUnattendedDTO(
                metricsListModel.getInvocationUnattended(), period));
        invocationMetricsDTO.setLargePayload(getInvocationMetricsLargePayloadDTO(
                metricsListModel.getInvocationLargePayload(), period));

        return invocationMetricsDTO;
    }

    private InvocationMetricsV3UnauthenticatedDTO getInvocationMetricsUnauthenticatedDTO(
            List<Integer> invocationUnauthenticatedList, PeriodEnum period) {

        InvocationMetricsV3UnauthenticatedDTO invocationMetricsUnauthenticatedDTO =
                new InvocationMetricsV3UnauthenticatedDTO();
        if (!invocationUnauthenticatedList.isEmpty() && (PeriodEnum.ALL == period || PeriodEnum.CURRENT == period)) {
            invocationMetricsUnauthenticatedDTO.setCurrentDay(invocationUnauthenticatedList.remove(0));
        }
        if (PeriodEnum.ALL == period || PeriodEnum.HISTORIC == period) {
            invocationMetricsUnauthenticatedDTO.setPreviousDays(invocationUnauthenticatedList);
        }
        return invocationMetricsUnauthenticatedDTO;
    }

    private InvocationMetricsV3HighPriorityDTO getInvocationMetricsHighPriorityDTO(
            List<Integer> invocationHighPriorityList, PeriodEnum period) {

        InvocationMetricsV3HighPriorityDTO invocationMetricsHighPriorityDTO =
                new InvocationMetricsV3HighPriorityDTO();
        if (!invocationHighPriorityList.isEmpty() && (PeriodEnum.ALL == period || PeriodEnum.CURRENT == period)) {
            invocationMetricsHighPriorityDTO.setCurrentDay(invocationHighPriorityList.remove(0));
        }
        if (PeriodEnum.ALL == period || PeriodEnum.HISTORIC == period) {
            invocationMetricsHighPriorityDTO.setPreviousDays(invocationHighPriorityList);
        }
        return invocationMetricsHighPriorityDTO;
    }

    private InvocationMetricsV3LowPriorityDTO getInvocationMetricsLowPriorityDTO(
            List<Integer> invocationLowPriorityList, PeriodEnum period) {

        InvocationMetricsV3LowPriorityDTO invocationMetricsLowPriorityDTO = new InvocationMetricsV3LowPriorityDTO();
        if (!invocationLowPriorityList.isEmpty() && (PeriodEnum.ALL == period || PeriodEnum.CURRENT == period)) {
            invocationMetricsLowPriorityDTO.setCurrentDay(invocationLowPriorityList.remove(0));
        }
        if (PeriodEnum.ALL == period || PeriodEnum.HISTORIC == period) {
            invocationMetricsLowPriorityDTO.setPreviousDays(invocationLowPriorityList);
        }
        return invocationMetricsLowPriorityDTO;
    }

    private InvocationMetricsV3UnattendedDTO getInvocationMetricsUnattendedDTO(
            List<Integer> invocationUnattendedList, PeriodEnum period) {

        InvocationMetricsV3UnattendedDTO invocationMetricsUnattendedDTO =
                new InvocationMetricsV3UnattendedDTO();
        if (!invocationUnattendedList.isEmpty() && (PeriodEnum.ALL == period || PeriodEnum.CURRENT == period)) {
            invocationMetricsUnattendedDTO.setCurrentDay(invocationUnattendedList.remove(0));
        }
        if (PeriodEnum.ALL == period || PeriodEnum.HISTORIC == period) {
            invocationMetricsUnattendedDTO.setPreviousDays(invocationUnattendedList);
        }
        return invocationMetricsUnattendedDTO;
    }

    private InvocationMetricsV3LargePayloadDTO getInvocationMetricsLargePayloadDTO(
            List<Integer> invocationLargePayloadList, PeriodEnum period) {

        InvocationMetricsV3LargePayloadDTO invocationMetricsLargePayloadDTO =
                new InvocationMetricsV3LargePayloadDTO();
        if (!invocationLargePayloadList.isEmpty() && (PeriodEnum.ALL == period || PeriodEnum.CURRENT == period)) {
            invocationMetricsLargePayloadDTO.setCurrentDay(invocationLargePayloadList.remove(0));
        }
        if (PeriodEnum.ALL == period || PeriodEnum.HISTORIC == period) {
            invocationMetricsLargePayloadDTO.setPreviousDays(invocationLargePayloadList);
        }
        return invocationMetricsLargePayloadDTO;
    }

    private AverageResponseMetricsV2DTO getAverageResponseDTO(MetricsResponseModel metricsListModel,
                                                              PeriodEnum period) {

        AverageResponseMetricsV2DTO averageResponseMetricsDTO = new AverageResponseMetricsV2DTO();

        averageResponseMetricsDTO.setUnauthenticated(getAverageResponseMetricsUnauthenticatedDTO(
                metricsListModel.getAverageResponseUnauthenticated(), period));
        averageResponseMetricsDTO.setHighPriority(getAverageResponseMetricsHighPriorityDTO(
                metricsListModel.getAverageResponseHighPriority(), period));
        averageResponseMetricsDTO.setLowPriority(getAverageResponseMetricsLowPriorityDTO(
                metricsListModel.getAverageResponseLowPriority(), period));
        averageResponseMetricsDTO.setUnattended(getAverageResponseMetricsUnattendedDTO(
                metricsListModel.getAverageResponseUnattended(), period));
        averageResponseMetricsDTO.setLargePayload(getAverageResponseMetricsLargePayloadDTO(
                metricsListModel.getAverageResponseLargePayload(), period));

        return averageResponseMetricsDTO;
    }

    private AverageResponseMetricsV2UnauthenticatedDTO getAverageResponseMetricsUnauthenticatedDTO(
            List<BigDecimal> averageResponseUnauthenticatedList, PeriodEnum period) {

        AverageResponseMetricsV2UnauthenticatedDTO averageResponseMetricsUnauthenticatedDTO =
                new AverageResponseMetricsV2UnauthenticatedDTO();
        if (!averageResponseUnauthenticatedList.isEmpty() && (PeriodEnum.ALL == period ||
                PeriodEnum.CURRENT == period)) {
            averageResponseMetricsUnauthenticatedDTO.setCurrentDay(averageResponseUnauthenticatedList.remove(0));
        }
        if (PeriodEnum.ALL == period || PeriodEnum.HISTORIC == period) {
            averageResponseMetricsUnauthenticatedDTO.setPreviousDays(averageResponseUnauthenticatedList);
        }
        return averageResponseMetricsUnauthenticatedDTO;
    }

    private AverageResponseMetricsV2HighPriorityDTO getAverageResponseMetricsHighPriorityDTO(
            List<BigDecimal> averageResponseHighPriorityList, PeriodEnum period) {

        AverageResponseMetricsV2HighPriorityDTO averageResponseMetricsHighPriorityDTO =
                new AverageResponseMetricsV2HighPriorityDTO();
        if (!averageResponseHighPriorityList.isEmpty() && (PeriodEnum.ALL == period || PeriodEnum.CURRENT == period)) {
            averageResponseMetricsHighPriorityDTO.setCurrentDay(averageResponseHighPriorityList.remove(0));
        }
        if (PeriodEnum.ALL == period || PeriodEnum.HISTORIC == period) {
            averageResponseMetricsHighPriorityDTO.setPreviousDays(averageResponseHighPriorityList);
        }
        return averageResponseMetricsHighPriorityDTO;
    }

    private AverageResponseMetricsV2LowPriorityDTO getAverageResponseMetricsLowPriorityDTO(
            List<BigDecimal> averageResponseLowPriorityList, PeriodEnum period) {

        AverageResponseMetricsV2LowPriorityDTO averageResponseMetricsLowPriorityDTO =
                new AverageResponseMetricsV2LowPriorityDTO();
        if (!averageResponseLowPriorityList.isEmpty() && (PeriodEnum.ALL == period || PeriodEnum.CURRENT == period)) {
            averageResponseMetricsLowPriorityDTO.setCurrentDay(averageResponseLowPriorityList.remove(0));
        }
        if (PeriodEnum.ALL == period || PeriodEnum.HISTORIC == period) {
            averageResponseMetricsLowPriorityDTO.setPreviousDays(averageResponseLowPriorityList);
        }
        return averageResponseMetricsLowPriorityDTO;
    }

    private AverageResponseMetricsV2UnattendedDTO getAverageResponseMetricsUnattendedDTO(
            List<BigDecimal> averageResponseUnattendedList, PeriodEnum period) {

        AverageResponseMetricsV2UnattendedDTO averageResponseMetricsUnattendedDTO =
                new AverageResponseMetricsV2UnattendedDTO();
        if (!averageResponseUnattendedList.isEmpty() && (PeriodEnum.ALL == period || PeriodEnum.CURRENT == period)) {
            averageResponseMetricsUnattendedDTO.setCurrentDay(averageResponseUnattendedList.remove(0));
        }
        if (PeriodEnum.ALL == period || PeriodEnum.HISTORIC == period) {
            averageResponseMetricsUnattendedDTO.setPreviousDays(averageResponseUnattendedList);
        }
        return averageResponseMetricsUnattendedDTO;
    }

    private AverageResponseMetricsV2LargePayloadDTO getAverageResponseMetricsLargePayloadDTO(
            List<BigDecimal> averageResponseLargePayloadList, PeriodEnum period) {

        AverageResponseMetricsV2LargePayloadDTO averageResponseMetricsLargePayloadDTO =
                new AverageResponseMetricsV2LargePayloadDTO();
        if (!averageResponseLargePayloadList.isEmpty() && (PeriodEnum.ALL == period || PeriodEnum.CURRENT == period)) {
            averageResponseMetricsLargePayloadDTO.setCurrentDay(averageResponseLargePayloadList.remove(0));
        }
        if (PeriodEnum.ALL == period || PeriodEnum.HISTORIC == period) {
            averageResponseMetricsLargePayloadDTO.setPreviousDays(averageResponseLargePayloadList);
        }
        return averageResponseMetricsLargePayloadDTO;
    }

    private SessionCountMetricsV2DTO getSessionCountDTO(List<Integer> sessionCountList, PeriodEnum period) {

        SessionCountMetricsV2DTO sessionCountMetricsDTO = new SessionCountMetricsV2DTO();
        if (!sessionCountList.isEmpty() && (PeriodEnum.ALL == period || PeriodEnum.CURRENT == period)) {
            sessionCountMetricsDTO.setCurrentDay(sessionCountList.remove(0));
        }
        if (PeriodEnum.ALL == period || PeriodEnum.HISTORIC == period) {
            sessionCountMetricsDTO.setPreviousDays(sessionCountList);
        }
        return sessionCountMetricsDTO;
    }

    private AverageTPSMetricsV2DTO getAverageTpsDTO(MetricsResponseModel metricsListModel,
                                                    PeriodEnum period) {

        AverageTPSMetricsV2DTO averageTPSMetricsDTO = new AverageTPSMetricsV2DTO();

        AverageTPSMetricsV2AggregateDTO aggregate = new AverageTPSMetricsV2AggregateDTO();
        AverageTPSMetricsV2AuthenticatedDTO authenticated = new AverageTPSMetricsV2AuthenticatedDTO();
        AverageTPSMetricsV2UnauthenticatedDTO unAuthenticated = new AverageTPSMetricsV2UnauthenticatedDTO();

        List<BigDecimal> averageTPS = metricsListModel.getAverageTPS();
        List<BigDecimal> authenticatedAverageTPS = metricsListModel.getAuthenticatedAverageTPS();
        List<BigDecimal> unauthenticatedAverageTPS = metricsListModel.getUnauthenticatedAverageTPS();

        if (PeriodEnum.ALL == period || PeriodEnum.CURRENT == period) {
            aggregate.setCurrentDay(averageTPS.remove(0));
            authenticated.setCurrentDay(authenticatedAverageTPS.remove(0));
            unAuthenticated.setCurrentDay(unauthenticatedAverageTPS.remove(0));
        }
        if (PeriodEnum.ALL == period || PeriodEnum.HISTORIC == period) {
            aggregate.setPreviousDays(averageTPS);
            authenticated.setPreviousDays(authenticatedAverageTPS);
            unAuthenticated.setPreviousDays(unauthenticatedAverageTPS);
        }

        averageTPSMetricsDTO.setAggregate(aggregate);
        averageTPSMetricsDTO.setAuthenticated(authenticated);
        averageTPSMetricsDTO.setUnauthenticated(unAuthenticated);

        return averageTPSMetricsDTO;
    }

    private PeakTPSMetricsV2DTO getPeakTpsDTO(MetricsResponseModel metricsListModel, PeriodEnum period) {

        PeakTPSMetricsV2DTO peakTPSMetricsDTO = new PeakTPSMetricsV2DTO();

        PeakTPSMetricsV2AggregateDTO aggregate = new PeakTPSMetricsV2AggregateDTO();
        PeakTPSMetricsV2AuthenticatedDTO authenticated = new PeakTPSMetricsV2AuthenticatedDTO();
        PeakTPSMetricsV2UnauthenticatedDTO unAuthenticated = new PeakTPSMetricsV2UnauthenticatedDTO();

        List<BigDecimal> peakTPS = metricsListModel.getPeakTPS();
        List<BigDecimal> authenticatedPeakTPS = metricsListModel.getAuthenticatedPeakTPS();
        List<BigDecimal> unauthenticatedPeakTPS = metricsListModel.getUnauthenticatedPeakTPS();

        if (PeriodEnum.ALL == period || PeriodEnum.CURRENT == period) {
            aggregate.setCurrentDay(peakTPS.remove(0));
            authenticated.setCurrentDay(authenticatedPeakTPS.remove(0));
            unAuthenticated.setCurrentDay(unauthenticatedPeakTPS.remove(0));
        }
        if (PeriodEnum.ALL == period || PeriodEnum.HISTORIC == period) {
            aggregate.setPreviousDays(peakTPS);
            authenticated.setPreviousDays(authenticatedPeakTPS);
            unAuthenticated.setPreviousDays(unauthenticatedPeakTPS);
        }

        peakTPSMetricsDTO.setAggregate(aggregate);
        peakTPSMetricsDTO.setAuthenticated(authenticated);
        peakTPSMetricsDTO.setUnauthenticated(unAuthenticated);

        return peakTPSMetricsDTO;
    }

    private ErrorMetricsV2DTO getErrorsDTO(MetricsResponseModel metricsListModel, PeriodEnum period) {

        ErrorMetricsV2DTO errorMetricsDTO = new ErrorMetricsV2DTO();

        ErrorMetricsV2AggregateDTO aggregate = new ErrorMetricsV2AggregateDTO();
        ErrorMetricsV2AuthenticatedDTO authenticated = new ErrorMetricsV2AuthenticatedDTO();
        ErrorMetricsV2UnauthenticatedDTO unAuthenticated = new ErrorMetricsV2UnauthenticatedDTO();

        List<Integer> errors = metricsListModel.getErrors();
        List<Map<String, Integer>> authenticatedErrors = metricsListModel.getAuthenticatedErrors();
        List<Map<String, Integer>> unauthenticatedErrors = metricsListModel.getUnauthenticatedErrors();

        if (PeriodEnum.ALL == period || PeriodEnum.CURRENT == period) {
            aggregate.setCurrentDay(errors.remove(0));
            authenticated.setCurrentDay(authenticatedErrors.remove(0));
            unAuthenticated.setCurrentDay(unauthenticatedErrors.remove(0));
        }
        if (PeriodEnum.ALL == period || PeriodEnum.HISTORIC == period) {
            aggregate.setPreviousDays(errors);
            authenticated.setPreviousDays(authenticatedErrors);
            unAuthenticated.setPreviousDays(unauthenticatedErrors);
        }

        errorMetricsDTO.setAggregate(aggregate);
        errorMetricsDTO.setAuthenticated(authenticated);
        errorMetricsDTO.setUnauthenticated(unAuthenticated);

        return errorMetricsDTO;
    }

    private RejectionMetricsV3DTO getRejectionsDTO(MetricsResponseModel metricsListModel, PeriodEnum period) {

        RejectionMetricsV3DTO rejectionMetricsDTO = new RejectionMetricsV3DTO();

        RejectionMetricsV3AuthenticatedDTO authenticated = new RejectionMetricsV3AuthenticatedDTO();
        RejectionMetricsV3UnauthenticatedDTO unAuthenticated = new RejectionMetricsV3UnauthenticatedDTO();

        List<Integer> authenticatedRejections = metricsListModel.getAuthenticatedEndpointRejections();
        List<Integer> unauthenticatedRejections = metricsListModel.getUnauthenticatedEndpointRejections();

        if (PeriodEnum.ALL == period || PeriodEnum.CURRENT == period) {
            authenticated.setCurrentDay(authenticatedRejections.remove(0));
            unAuthenticated.setCurrentDay(unauthenticatedRejections.remove(0));
        }
        if (PeriodEnum.ALL == period || PeriodEnum.HISTORIC == period) {
            authenticated.setPreviousDays(authenticatedRejections);
            unAuthenticated.setPreviousDays(unauthenticatedRejections);
        }

        rejectionMetricsDTO.setAuthenticated(authenticated);
        rejectionMetricsDTO.setUnauthenticated(unAuthenticated);

        return rejectionMetricsDTO;
    }

    private AuthorisationMetricsV2DTO getAuthorisationsDTO(MetricsResponseModel metricsListModel,
                                                           PeriodEnum period) {

        AuthorisationMetricsV2DTO authorisationMetricsDTO = new AuthorisationMetricsV2DTO();

        authorisationMetricsDTO.setActiveAuthorisationCount(getActiveAuthorisationDTO(
                metricsListModel.getActiveIndividualAuthorisationCount(),
                metricsListModel.getActiveNonIndividualAuthorisationCount()));
        authorisationMetricsDTO.setNewAuthorisationCount(getNewAuthorisationDTO(
                metricsListModel.getNewAuthorisationCount(),
                period));
        authorisationMetricsDTO.setRevokedAuthorisationCount(getRevokedAuthorisationDTO(
                metricsListModel.getRevokedAuthorisationCount(),
                period));
        authorisationMetricsDTO.setAmendedAuthorisationCount(getAmendedAuthorisationDTO(
                metricsListModel.getAmendedAuthorisationCount(),
                period));
        authorisationMetricsDTO.setExpiredAuthorisationCount(getExpiredAuthorisationDTO(
                metricsListModel.getExpiredAuthorisationCount(),
                period));
        authorisationMetricsDTO.setAbandonedConsentFlowCount(getAbandonedAuthorisationDTO(
                metricsListModel.getAbandonedConsentFlowCount(),
                period));
        authorisationMetricsDTO.setAbandonmentsByStage(getAbandonedByStageDTO(metricsListModel, period));

        return authorisationMetricsDTO;
    }

    private AuthorisationMetricsV2ActiveAuthorisationCountDTO getActiveAuthorisationDTO(
            int activeIndividualAuthorisationCount, int activeNonIndividualAuthorisationCount) {

        AuthorisationMetricsV2ActiveAuthorisationCountDTO activeAuthorisationCountDTO =
                new AuthorisationMetricsV2ActiveAuthorisationCountDTO();

        activeAuthorisationCountDTO.setIndividual(activeIndividualAuthorisationCount);
        activeAuthorisationCountDTO.setNonIndividual(activeNonIndividualAuthorisationCount);

        return activeAuthorisationCountDTO;
    }

    private AuthorisationMetricsV2NewAuthorisationCountDTO getNewAuthorisationDTO(
            List<AuthorisationMetric> authorisationMetricCountList, PeriodEnum period) {

        AuthorisationMetricsV2NewAuthorisationCountDTO newAuthorisationCountDTO =
                new AuthorisationMetricsV2NewAuthorisationCountDTO();

        if (!authorisationMetricCountList.isEmpty() && (PeriodEnum.ALL == period ||
                PeriodEnum.CURRENT == period)) {
            newAuthorisationCountDTO.setCurrentDay(
                    getCurrentDayNewAuthorisationCountDTO(authorisationMetricCountList.remove(0)));
        }
        if (PeriodEnum.ALL == period || PeriodEnum.HISTORIC == period) {
            newAuthorisationCountDTO.setPreviousDays(
                    getPreviousDaysNewAuthorisationCountDTO(authorisationMetricCountList));
        }

        return newAuthorisationCountDTO;
    }

    private AuthorisationMetricsV2NewAuthorisationCountDayDTO getCurrentDayNewAuthorisationCountDTO(
            AuthorisationMetric authorisationMetric) {

        AuthorisationMetricsV2NewAuthorisationCountDayDTO currentDayNewAuthorisationCountDTO =
                new AuthorisationMetricsV2NewAuthorisationCountDayDTO();

        AuthorisationMetricsV2AuthorisationCountDayDTO onceOffDTO =
                new AuthorisationMetricsV2AuthorisationCountDayDTO();
        onceOffDTO.setIndividual(authorisationMetric.getOnceOff().getIndividual());
        onceOffDTO.setNonIndividual(authorisationMetric.getOnceOff().getNonIndividual());
        currentDayNewAuthorisationCountDTO.setOnceOff(onceOffDTO);

        AuthorisationMetricsV2AuthorisationCountDayDTO ongoingDTO =
                new AuthorisationMetricsV2AuthorisationCountDayDTO();
        ongoingDTO.setIndividual(authorisationMetric.getOngoing().getIndividual());
        ongoingDTO.setNonIndividual(authorisationMetric.getOngoing().getNonIndividual());
        currentDayNewAuthorisationCountDTO.setOngoing(ongoingDTO);

        return currentDayNewAuthorisationCountDTO;
    }

    private List<AuthorisationMetricsV2NewAuthorisationCountDayDTO> getPreviousDaysNewAuthorisationCountDTO(
            List<AuthorisationMetric> authorisationMetricCountList) {

        List<AuthorisationMetricsV2NewAuthorisationCountDayDTO> previousDaysNewAuthorisationCountDTO =
                new ArrayList<>();

        for (AuthorisationMetric authorisationMetric : authorisationMetricCountList) {
            previousDaysNewAuthorisationCountDTO.add(getCurrentDayNewAuthorisationCountDTO(authorisationMetric));
        }

        return previousDaysNewAuthorisationCountDTO;
    }

    private AuthorisationMetricsV2RevokedAuthorisationCountDTO getRevokedAuthorisationDTO(
            List<CustomerTypeCount> revokedAuthorisationCountList, PeriodEnum period) {

        AuthorisationMetricsV2RevokedAuthorisationCountDTO revokedAuthorisationCountDTO =
                new AuthorisationMetricsV2RevokedAuthorisationCountDTO();

        if (!revokedAuthorisationCountList.isEmpty() && (PeriodEnum.ALL == period ||
                PeriodEnum.CURRENT == period)) {
            revokedAuthorisationCountDTO.setCurrentDay(
                    getCurrentDayAuthorisationCountDTO(revokedAuthorisationCountList.remove(0)));
        }
        if (PeriodEnum.ALL == period || PeriodEnum.HISTORIC == period) {
            revokedAuthorisationCountDTO.setPreviousDays(
                    getPreviousDaysAuthorisationCountDTO(revokedAuthorisationCountList));
        }

        return revokedAuthorisationCountDTO;
    }

    private AuthorisationMetricsV2AmendedAuthorisationCountDTO getAmendedAuthorisationDTO(
            List<CustomerTypeCount> amendedAuthorisationCountList, PeriodEnum period) {

        AuthorisationMetricsV2AmendedAuthorisationCountDTO amendedAuthorisationCountDTO =
                new AuthorisationMetricsV2AmendedAuthorisationCountDTO();

        if (!amendedAuthorisationCountList.isEmpty() && (PeriodEnum.ALL == period || PeriodEnum.CURRENT == period)) {
            amendedAuthorisationCountDTO.setCurrentDay(
                    getCurrentDayAuthorisationCountDTO(amendedAuthorisationCountList.remove(0)));
        }
        if (PeriodEnum.ALL == period || PeriodEnum.HISTORIC == period) {
            amendedAuthorisationCountDTO.setPreviousDays(
                    getPreviousDaysAuthorisationCountDTO(amendedAuthorisationCountList));
        }

        return amendedAuthorisationCountDTO;
    }

    private AuthorisationMetricsV2ExpiredAuthorisationCountDTO getExpiredAuthorisationDTO(
            List<CustomerTypeCount> expiredAuthorisationCountList, PeriodEnum period) {

        AuthorisationMetricsV2ExpiredAuthorisationCountDTO expiredAuthorisationCountDTO =
                new AuthorisationMetricsV2ExpiredAuthorisationCountDTO();

        if (!expiredAuthorisationCountList.isEmpty() && (PeriodEnum.ALL == period || PeriodEnum.CURRENT == period)) {
            expiredAuthorisationCountDTO.setCurrentDay(
                    getCurrentDayAuthorisationCountDTO(expiredAuthorisationCountList.remove(0)));
        }
        if (PeriodEnum.ALL == period || PeriodEnum.HISTORIC == period) {
            expiredAuthorisationCountDTO.setPreviousDays(
                    getPreviousDaysAuthorisationCountDTO(expiredAuthorisationCountList));
        }

        return expiredAuthorisationCountDTO;
    }

    private AuthorisationMetricsV2AuthorisationCountDayDTO getCurrentDayAuthorisationCountDTO(
            CustomerTypeCount customerTypeCount) {

        AuthorisationMetricsV2AuthorisationCountDayDTO currentDayExpiredAuthorisationCountDTO =
                new AuthorisationMetricsV2AuthorisationCountDayDTO();
        currentDayExpiredAuthorisationCountDTO.setIndividual(customerTypeCount.getIndividual());
        currentDayExpiredAuthorisationCountDTO.setNonIndividual(customerTypeCount.getNonIndividual());
        return currentDayExpiredAuthorisationCountDTO;
    }

    private List<AuthorisationMetricsV2AuthorisationCountDayDTO> getPreviousDaysAuthorisationCountDTO(
            List<CustomerTypeCount> customerTypeCounts) {

        List<AuthorisationMetricsV2AuthorisationCountDayDTO> previousDaysAuthorisationCountDTO = new ArrayList<>();

        for (CustomerTypeCount customerTypeCount : customerTypeCounts) {
            previousDaysAuthorisationCountDTO.add(getCurrentDayAuthorisationCountDTO(customerTypeCount));
        }

        return previousDaysAuthorisationCountDTO;
    }

    private AuthorisationMetricsV2AbandonedConsentFlowCountDTO getAbandonedAuthorisationDTO(
            List<Integer> abandonedConsentFlowCountList, PeriodEnum period) {

        AuthorisationMetricsV2AbandonedConsentFlowCountDTO abandonedConsentFlowCountDTO =
                new AuthorisationMetricsV2AbandonedConsentFlowCountDTO();

        if (!abandonedConsentFlowCountList.isEmpty() && (PeriodEnum.ALL == period || PeriodEnum.CURRENT == period)) {
            abandonedConsentFlowCountDTO.setCurrentDay(abandonedConsentFlowCountList.remove(0));
        }
        if (PeriodEnum.ALL == period || PeriodEnum.HISTORIC == period) {
            abandonedConsentFlowCountDTO.setPreviousDays(abandonedConsentFlowCountList);
        }

        return abandonedConsentFlowCountDTO;
    }

    private AuthorisationMetricsV2AbandonmentsByStageDTO getAbandonedByStageDTO(
            MetricsResponseModel metricsListModel, PeriodEnum period) {

        AuthorisationMetricsV2AbandonmentsByStageDTO abandonmentByStageDTO =
                new AuthorisationMetricsV2AbandonmentsByStageDTO();

        abandonmentByStageDTO.setPreIdentification(getAbandonmentByPreIdentificationDTO(metricsListModel
                .getPreIdentificationAbandonedConsentFlowCount(), period));
        abandonmentByStageDTO.setPreAuthentication(getAbandonmentByPreAuthenticationDTO(metricsListModel
                .getPreAuthenticationAbandonedConsentFlowCount(), period));
        abandonmentByStageDTO.setPreAccountSelection(getAbandonmentByPreAccountSelectionDTO(metricsListModel
                .getPreAccountSelectionAbandonedConsentFlowCount(), period));
        abandonmentByStageDTO.setPreAuthorisation(getAbandonmentByPreAuthorisationDTO(metricsListModel
                .getPreAuthorisationAbandonedConsentFlowCount(), period));
        abandonmentByStageDTO.setRejected(getAbandonmentByRejectedDTO(metricsListModel
                .getRejectedAbandonedConsentFlowCount(), period));
        abandonmentByStageDTO.setFailedTokenExchange(getAbandonmentByFailedTokenExchangeDTO(metricsListModel
                .getFailedTokenExchangeAbandonedConsentFlowCount(), period));

        return abandonmentByStageDTO;
    }

    private AuthorisationMetricsV2AbandonmentsByStagePreIdentificationDTO
    getAbandonmentByPreIdentificationDTO(List<Integer> abandonedByPreIdentificationCountList, PeriodEnum period) {

        AuthorisationMetricsV2AbandonmentsByStagePreIdentificationDTO abandonedByPreIdentificationCountDTO =
                new AuthorisationMetricsV2AbandonmentsByStagePreIdentificationDTO();

        if (!abandonedByPreIdentificationCountList.isEmpty() && (PeriodEnum.ALL == period ||
                PeriodEnum.CURRENT == period)) {
            abandonedByPreIdentificationCountDTO.setCurrentDay(abandonedByPreIdentificationCountList.remove(0));
        }
        if (PeriodEnum.ALL == period || PeriodEnum.HISTORIC == period) {
            abandonedByPreIdentificationCountDTO.setPreviousDays(abandonedByPreIdentificationCountList);
        }

        return abandonedByPreIdentificationCountDTO;
    }

    private AuthorisationMetricsV2AbandonmentsByStagePreAuthenticationDTO
    getAbandonmentByPreAuthenticationDTO(List<Integer> abandonedByPreAuthenticationCountList, PeriodEnum period) {

        AuthorisationMetricsV2AbandonmentsByStagePreAuthenticationDTO abandonedByPreAuthenticationCountDTO =
                new AuthorisationMetricsV2AbandonmentsByStagePreAuthenticationDTO();

        if (!abandonedByPreAuthenticationCountList.isEmpty() && (PeriodEnum.ALL == period ||
                PeriodEnum.CURRENT == period)) {
            abandonedByPreAuthenticationCountDTO.setCurrentDay(abandonedByPreAuthenticationCountList.remove(0));
        }
        if (PeriodEnum.ALL == period || PeriodEnum.HISTORIC == period) {
            abandonedByPreAuthenticationCountDTO.setPreviousDays(abandonedByPreAuthenticationCountList);
        }

        return abandonedByPreAuthenticationCountDTO;
    }

    private AuthorisationMetricsV2AbandonmentsByStagePreAccountSelectionDTO
    getAbandonmentByPreAccountSelectionDTO(List<Integer> abandonedByPreAccountSelectionCountList, PeriodEnum period) {

        AuthorisationMetricsV2AbandonmentsByStagePreAccountSelectionDTO abandonedByPreAccountSelectionCountDTO =
                new AuthorisationMetricsV2AbandonmentsByStagePreAccountSelectionDTO();

        if (!abandonedByPreAccountSelectionCountList.isEmpty() && (PeriodEnum.ALL == period ||
                PeriodEnum.CURRENT == period)) {
            abandonedByPreAccountSelectionCountDTO.setCurrentDay(abandonedByPreAccountSelectionCountList.remove(0));
        }
        if (PeriodEnum.ALL == period || PeriodEnum.HISTORIC == period) {
            abandonedByPreAccountSelectionCountDTO.setPreviousDays(abandonedByPreAccountSelectionCountList);
        }

        return abandonedByPreAccountSelectionCountDTO;
    }

    private AuthorisationMetricsV2AbandonmentsByStagePreAuthorisationDTO
    getAbandonmentByPreAuthorisationDTO(List<Integer> abandonedByPreAuthorisationCountList, PeriodEnum period) {

        AuthorisationMetricsV2AbandonmentsByStagePreAuthorisationDTO abandonedByPreAuthorisationCountDTO =
                new AuthorisationMetricsV2AbandonmentsByStagePreAuthorisationDTO();

        if (!abandonedByPreAuthorisationCountList.isEmpty() && (PeriodEnum.ALL == period ||
                PeriodEnum.CURRENT == period)) {
            abandonedByPreAuthorisationCountDTO.setCurrentDay(abandonedByPreAuthorisationCountList.remove(0));
        }
        if (PeriodEnum.ALL == period || PeriodEnum.HISTORIC == period) {
            abandonedByPreAuthorisationCountDTO.setPreviousDays(abandonedByPreAuthorisationCountList);
        }

        return abandonedByPreAuthorisationCountDTO;
    }

    private AuthorisationMetricsV2AbandonmentsByStageRejectedDTO
    getAbandonmentByRejectedDTO(List<Integer> abandonedByRejectedCountList, PeriodEnum period) {

        AuthorisationMetricsV2AbandonmentsByStageRejectedDTO abandonedByRejectedCountDTO =
                new AuthorisationMetricsV2AbandonmentsByStageRejectedDTO();

        if (!abandonedByRejectedCountList.isEmpty() && (PeriodEnum.ALL == period ||
                PeriodEnum.CURRENT == period)) {
            abandonedByRejectedCountDTO.setCurrentDay(abandonedByRejectedCountList.remove(0));
        }
        if (PeriodEnum.ALL == period || PeriodEnum.HISTORIC == period) {
            abandonedByRejectedCountDTO.setPreviousDays(abandonedByRejectedCountList);
        }

        return abandonedByRejectedCountDTO;
    }

    private AuthorisationMetricsV2AbandonmentsByStageFailedTokenExchangeDTO
    getAbandonmentByFailedTokenExchangeDTO(List<Integer> abandonedByFailedTokenExchangeCountList, PeriodEnum period) {

        AuthorisationMetricsV2AbandonmentsByStageFailedTokenExchangeDTO abandonedByFailedTokenExchangeCountDTO =
                new AuthorisationMetricsV2AbandonmentsByStageFailedTokenExchangeDTO();

        if (!abandonedByFailedTokenExchangeCountList.isEmpty() && (PeriodEnum.ALL == period ||
                PeriodEnum.CURRENT == period)) {
            abandonedByFailedTokenExchangeCountDTO.setCurrentDay(abandonedByFailedTokenExchangeCountList.remove(0));
        }
        if (PeriodEnum.ALL == period || PeriodEnum.HISTORIC == period) {
            abandonedByFailedTokenExchangeCountDTO.setPreviousDays(abandonedByFailedTokenExchangeCountList);
        }

        return abandonedByFailedTokenExchangeCountDTO;
    }
}

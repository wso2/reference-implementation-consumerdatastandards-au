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

package org.wso2.openbanking.cds.metrics.endpoint.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import net.minidev.json.JSONArray;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.cds.common.error.handling.models.CDSErrorMeta;
import org.wso2.openbanking.cds.common.error.handling.util.ErrorConstants;
import org.wso2.openbanking.cds.common.error.handling.util.ErrorUtil;
import org.wso2.openbanking.cds.metrics.endpoint.api.MetricsApi;
import org.wso2.openbanking.cds.metrics.endpoint.mapper.MetricsMapper;
import org.wso2.openbanking.cds.metrics.endpoint.mapper.MetricsV5MapperImpl;
import org.wso2.openbanking.cds.metrics.endpoint.model.v5.ResponseMetricsListV5DTO;
import org.wso2.openbanking.cds.metrics.model.MetricsResponseModel;
import org.wso2.openbanking.cds.metrics.service.CDSMetricsService;
import org.wso2.openbanking.cds.metrics.service.CDSMetricsServiceImpl;
import org.wso2.openbanking.cds.metrics.util.PeriodEnum;

import java.util.Arrays;
import javax.ws.rs.core.Response;

/**
 * Implementation of the Metrics API
 */
public class MetricsApiImpl implements MetricsApi {

    private static final Log log = LogFactory.getLog(MetricsApiImpl.class);
    CDSMetricsService cdsMetricsServiceImpl = new CDSMetricsServiceImpl();
    MetricsMapper metricsMapper = new MetricsV5MapperImpl();
    private static final String XV_HEADER = "x-v";
    private static final String[] SUPPORTED_X_VERSIONS = {"5"};

    /**
     * {@inheritDoc}
     */
    public Response getMetrics(String xV, String period, String xMinV) {

        if (!Arrays.asList(SUPPORTED_X_VERSIONS).contains(xV)) {
            log.error("Error occurred due to request API version mismatch.");
            JSONArray errorList = new JSONArray();
            errorList.add(ErrorUtil.getErrorObject(ErrorConstants.AUErrorEnum.UNSUPPORTED_VERSION,
                    "Requested x-v version is not supported", new CDSErrorMeta()));
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(ErrorUtil.getErrorJson(errorList)).build();
        }

        if (!PeriodEnum.isValidPeriodEnum(period)) {
            log.error("Error occurred due to invalid period value.");
            JSONArray errorList = new JSONArray();
            errorList.add(ErrorUtil.getErrorObject(ErrorConstants.AUErrorEnum.INVALID_FIELD,
                    "Requested period value is invalid", new CDSErrorMeta()));
            return Response.status(Response.Status.BAD_REQUEST).entity(ErrorUtil.getErrorJson(errorList)).build();
        }

        PeriodEnum periodEnum = PeriodEnum.fromString(period);

        try {
            MetricsResponseModel metricsListModel = cdsMetricsServiceImpl.getMetrics(xV, periodEnum);
            ResponseMetricsListV5DTO metricsListDTO = metricsMapper.getResponseMetricsListDTO(metricsListModel, period);

            // Return the metrics list as a JSON object after excluding null values
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            String serializedMetricsListDTO = mapper.writeValueAsString(metricsListDTO);

            return Response.ok().entity(serializedMetricsListDTO).header(XV_HEADER, xV).build();
        } catch (OpenBankingException | JsonProcessingException e) {
            log.error("Error occurred while computing metrics.", e);
            JSONArray errorList = new JSONArray();
            errorList.add(ErrorUtil.getErrorObject(ErrorConstants.AUErrorEnum.UNEXPECTED_ERROR,
                    "Unexpected error occurred while calculating metrics", new CDSErrorMeta()));
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ErrorUtil.getErrorJson(errorList))
                    .header(XV_HEADER, xV).build();
        }
    }

}


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

package org.wso2.openbanking.cds.gateway.executors.reporting;

import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.common.gateway.dto.APIRequestInfoDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.MsgInfoDTO;
import org.wso2.openbanking.cds.gateway.utils.GatewayConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * Test class for CDSCommonDataReportingExecutor.
 */
public class CDSCommonDataReportingExecutorTest {

    OBAPIRequestContext obApiRequestContextMock;
    OBAPIResponseContext obApiResponseContextMock;
    CDSCommonDataReportingExecutor cdsCommonDataReportingExecutor;
    MsgInfoDTO msgInfoDTOMock;
    APIRequestInfoDTO apiRequestInfoDTOMock;
    Map<String, Object> analyticsData = new HashMap<>();

    @BeforeClass
    public void initClass() {

        cdsCommonDataReportingExecutor = Mockito.spy(new CDSCommonDataReportingExecutor());
        obApiRequestContextMock = Mockito.mock(OBAPIRequestContext.class);
        obApiResponseContextMock = Mockito.mock(OBAPIResponseContext.class);
        msgInfoDTOMock = Mockito.mock(MsgInfoDTO.class);
        apiRequestInfoDTOMock = Mockito.mock(APIRequestInfoDTO.class);
    }


    @Test
    public void testPreProcessRequest() {

        Map<String, String> headers = new HashMap<>();
        headers.put(GatewayConstants.X_FAPI_CUSTOMER_IP_ADDRESS, "ip-address");

        Mockito.doReturn(msgInfoDTOMock).when(obApiRequestContextMock).getMsgInfo();
        Mockito.doReturn(headers).when(msgInfoDTOMock).getHeaders();
        CDSCommonDataReportingExecutor cdsCommonDataReportingExecutor = new CDSCommonDataReportingExecutor();
        Mockito.doReturn(apiRequestInfoDTOMock).when(obApiRequestContextMock).getApiRequestInfo();
        Mockito.doReturn(analyticsData).when(obApiRequestContextMock).getAnalyticsData();
        cdsCommonDataReportingExecutor.preProcessRequest(obApiRequestContextMock);

        Assert.assertEquals(obApiRequestContextMock.getAnalyticsData().get(GatewayConstants.CUSTOMER_STATUS),
                GatewayConstants.CUSTOMER_PRESENT);
    }

    @Test
    public void testPostProcessRequest() {

        Map<String, String> headers = new HashMap<>();
        headers.put(GatewayConstants.AUTHORIZATION, null);

        Mockito.doReturn(msgInfoDTOMock).when(obApiRequestContextMock).getMsgInfo();
        Mockito.doReturn(headers).when(msgInfoDTOMock).getHeaders();
        CDSCommonDataReportingExecutor cdsCommonDataReportingExecutor = new CDSCommonDataReportingExecutor();
        Mockito.doReturn(analyticsData).when(obApiRequestContextMock).getAnalyticsData();
        cdsCommonDataReportingExecutor.postProcessRequest(obApiRequestContextMock);

        Assert.assertEquals(obApiRequestContextMock.getAnalyticsData().get(GatewayConstants.ACCESS_TOKEN_ID),
                null);
    }
}

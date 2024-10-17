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
package org.wso2.openbanking.cds.gateway.executors.core;

import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.common.gateway.dto.MsgInfoDTO;
import org.wso2.openbanking.cds.gateway.test.util.TestUtil;
import org.wso2.openbanking.cds.gateway.utils.GatewayConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * Test class for CDSAPIRequestRouter.
 */
public class CDSAPIRequestRouterTest {

    CDSAPIRequestRouter cdsApiRequestRouter;
    OpenAPI openAPI;

    @BeforeClass
    public void beforeClass() {

        cdsApiRequestRouter = new CDSAPIRequestRouter();
        cdsApiRequestRouter.setExecutorMap(TestUtil.initExecutors());
        openAPI = new OpenAPI();
        openAPI.setExtensions(new HashMap<>());
    }

    @Test(priority = 1)
    public void testDCRRequestsForRouter() {

        OBAPIRequestContext obapiRequestContext = Mockito.mock(OBAPIRequestContext.class);
        OBAPIResponseContext obapiResponseContext = Mockito.mock(OBAPIResponseContext.class);
        Info apiInfo = Mockito.mock(Info.class);
        openAPI.setInfo(apiInfo);
        Map<String, Object> extensions = new HashMap<>();
        Map<String, String> contextProps = new HashMap<>();
        extensions.put(RequestRouterConstants.API_TYPE_CUSTOM_PROP, RequestRouterConstants.API_TYPE_DCR);
        contextProps.put(RequestRouterConstants.API_TYPE_CUSTOM_PROP, RequestRouterConstants.API_TYPE_DCR);
        openAPI.setExtensions(extensions);
        Mockito.when(obapiRequestContext.getOpenAPI()).thenReturn(openAPI);

        MsgInfoDTO msgInfoDTO = new MsgInfoDTO();
        Mockito.when(obapiRequestContext.getMsgInfo()).thenReturn(msgInfoDTO);

        Mockito.when(obapiResponseContext.getContextProps()).thenReturn(contextProps);
        Mockito.when(apiInfo.getTitle()).thenReturn(RequestRouterConstants.DCR_API_NAME);
        Assert.assertNotNull(cdsApiRequestRouter.getExecutorsForRequest(obapiRequestContext));
        Assert.assertNotNull(cdsApiRequestRouter.getExecutorsForResponse(obapiResponseContext));

    }

    @Test(priority = 1)
    public void testCDSRequestsForRouter() {

        OBAPIRequestContext obapiRequestContext = Mockito.mock(OBAPIRequestContext.class);
        OBAPIResponseContext obapiResponseContext = Mockito.mock(OBAPIResponseContext.class);
        Info apiInfo = Mockito.mock(Info.class);
        openAPI.setInfo(apiInfo);
        Map<String, Object> extensions = new HashMap<>();
        Map<String, String> contextProps = new HashMap<>();
        extensions.put(RequestRouterConstants.API_TYPE_CUSTOM_PROP, RequestRouterConstants.API_TYPE_CDS);
        contextProps.put(RequestRouterConstants.API_TYPE_CUSTOM_PROP, RequestRouterConstants.API_TYPE_CDS);
        openAPI.setExtensions(extensions);
        Mockito.when(obapiRequestContext.getOpenAPI()).thenReturn(openAPI);

        MsgInfoDTO msgInfoDTO = new MsgInfoDTO();
        Mockito.when(obapiRequestContext.getMsgInfo()).thenReturn(msgInfoDTO);

        Mockito.when(obapiResponseContext.getContextProps()).thenReturn(contextProps);
        Mockito.when(apiInfo.getTitle()).thenReturn(RequestRouterConstants.CDS_API_NAME);
        Assert.assertNotNull(cdsApiRequestRouter.getExecutorsForRequest(obapiRequestContext));
        Assert.assertNotNull(cdsApiRequestRouter.getExecutorsForResponse(obapiResponseContext));

    }

    @Test(priority = 1)
    public void testOtherRequestsForRouter() {

        OBAPIRequestContext obapiRequestContext = Mockito.mock(OBAPIRequestContext.class);
        OBAPIResponseContext obapiResponseContext = Mockito.mock(OBAPIResponseContext.class);
        Info apiInfo = Mockito.mock(Info.class);
        openAPI.setInfo(apiInfo);
        Map<String, Object> extensions = new HashMap<>();
        Map<String, String> contextProps = new HashMap<>();
        openAPI.setExtensions(extensions);
        Mockito.when(obapiRequestContext.getOpenAPI()).thenReturn(openAPI);

        MsgInfoDTO msgInfoDTO = new MsgInfoDTO();
        Mockito.when(obapiRequestContext.getMsgInfo()).thenReturn(msgInfoDTO);

        Mockito.when(obapiResponseContext.getContextProps()).thenReturn(contextProps);
        Mockito.when(apiInfo.getTitle()).thenReturn(RequestRouterConstants.CDS_API_NAME);
        Assert.assertNotNull(cdsApiRequestRouter.getExecutorsForRequest(obapiRequestContext));
        Assert.assertNotNull(cdsApiRequestRouter.getExecutorsForResponse(obapiResponseContext));
    }

    @Test(priority = 2)
    public void testNonRegulatoryAPIcall() {

        OBAPIRequestContext obapiRequestContext = Mockito.mock(OBAPIRequestContext.class);
        OBAPIResponseContext obapiResponseContext = Mockito.mock(OBAPIResponseContext.class);
        Map<String, Object> extensions = new HashMap<>();
        Map<String, String> contextProps = new HashMap<>();
        extensions.put(RequestRouterConstants.API_TYPE_CUSTOM_PROP, RequestRouterConstants.API_TYPE_NON_REGULATORY);
        contextProps.put(RequestRouterConstants.API_TYPE_CUSTOM_PROP, RequestRouterConstants.API_TYPE_NON_REGULATORY);
        openAPI.setExtensions(extensions);
        Mockito.when(obapiRequestContext.getOpenAPI()).thenReturn(openAPI);
        Mockito.when(obapiResponseContext.getContextProps()).thenReturn(contextProps);
        Assert.assertEquals(cdsApiRequestRouter.getExecutorsForRequest(obapiRequestContext).size(), 0);
        Assert.assertEquals(cdsApiRequestRouter.getExecutorsForResponse(obapiResponseContext).size(), 0);
    }

    @Test(priority = 3)
    public void testUnauthenticatedAPIcall() {

        OBAPIRequestContext obapiRequestContext = Mockito.mock(OBAPIRequestContext.class);
        OBAPIResponseContext obapiResponseContext = Mockito.mock(OBAPIResponseContext.class);
        Map<String, Object> extensions = new HashMap<>();
        Map<String, String> contextProps = new HashMap<>();
        extensions.put(RequestRouterConstants.API_TYPE_CUSTOM_PROP,
                RequestRouterConstants.API_TYPE_CDS_UNAUTHENTICATED);
        contextProps.put(RequestRouterConstants.API_TYPE_CUSTOM_PROP,
                RequestRouterConstants.API_TYPE_CDS_UNAUTHENTICATED);
        openAPI.setExtensions(extensions);
        Mockito.when(obapiRequestContext.getOpenAPI()).thenReturn(openAPI);

        MsgInfoDTO msgInfoDTO = new MsgInfoDTO();
        msgInfoDTO.setElectedResource(GatewayConstants.PRODUCT_DETAILS_ENDPOINT);
        Mockito.when(obapiRequestContext.getMsgInfo()).thenReturn(msgInfoDTO);

        Mockito.when(obapiResponseContext.getContextProps()).thenReturn(contextProps);
        Assert.assertEquals(cdsApiRequestRouter.getExecutorsForRequest(obapiRequestContext).size(), 1);
        Assert.assertEquals(cdsApiRequestRouter.getExecutorsForResponse(obapiResponseContext).size(), 1);
    }
}

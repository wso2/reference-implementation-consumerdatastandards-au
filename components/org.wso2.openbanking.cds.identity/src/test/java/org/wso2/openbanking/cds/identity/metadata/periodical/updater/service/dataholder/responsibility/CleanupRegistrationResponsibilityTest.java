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

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.identity.util.HTTPClientUtils;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;
import org.wso2.openbanking.cds.common.metadata.periodical.updater.constants.MetadataConstants;
import org.wso2.openbanking.cds.common.metadata.periodical.updater.utils.DataRecipientStatusEnum;
import org.wso2.openbanking.cds.common.metadata.periodical.updater.utils.SoftwareProductStatusEnum;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * Test class for Cleanup Registration Responsibility.
 */
@PrepareForTest({OpenBankingCDSConfigParser.class, HTTPClientUtils.class})
@PowerMockIgnore("jdk.internal.reflect.*")
public class CleanupRegistrationResponsibilityTest extends PowerMockTestCase {

    OpenBankingCDSConfigParser obCDSConfigParserMock;
    ServiceProvider serviceProviderMock;
    CleanupRegistrationResponsibility uut;
    CleanupRegistrationResponsibility uutSpy;

    private static final String APPLICATION_NAME = "adbbd203-84c2-45cc-b007-1bfe05b75737";

    @BeforeClass
    public void init() {
        mockServiceProvider();
        mockOpenBankingCDSConfigParser();

        this.uut = new CleanupRegistrationResponsibility(DataRecipientStatusEnum.ACTIVE.toString(),
                SoftwareProductStatusEnum.REMOVED.toString(), this.serviceProviderMock);
        this.uutSpy = Mockito.spy(this.uut);
    }

    @Test(priority = 1, description = "when software product status is REMOVED, should return true")
    public void testShouldPerform() {
        Assert.assertTrue(uut.shouldPerform());

        CleanupRegistrationResponsibility uutShouldNotPerform = new CleanupRegistrationResponsibility(
                DataRecipientStatusEnum.ACTIVE.toString(), SoftwareProductStatusEnum.ACTIVE.toString(), null);
        Assert.assertFalse(uutShouldNotPerform.shouldPerform());
    }

    @Test(priority = 1)
    public void testPerform() throws OpenBankingException, IOException {
        // testing regular path
        Mockito.doNothing().when(uutSpy).executeDeleteRequest(Mockito.any(String.class), Mockito.any(String.class),
                Mockito.anyListOf(Integer.class));
        mockHttpGetResponse(HttpStatus.SC_OK);
        this.uutSpy.perform();

        // testing exception path
        mockHttpGetResponse(HttpStatus.SC_BAD_REQUEST);
        this.uutSpy.perform();
    }

    @Test(priority = 2)
    public void testExecuteDeleteRequest() throws OpenBankingException, IOException {
        mockHttpDeleteResponse();
        uut.executeDeleteRequest("test-app-id", "https://test.com/url", Collections.singletonList(204));
    }

    @Test(priority = 2, description = "when empty URL provided, should throw OpenBankingException",
            expectedExceptions = OpenBankingException.class)
    public void testExecuteDeleteRequestWithEmptyUrl() throws OpenBankingException, IOException {
        mockHttpDeleteResponse();
        uut.executeDeleteRequest("test-app-id", "", Collections.singletonList(204));
    }

    @Test(priority = 2, description = "when unexpected response received, should throw OpenBankingException",
            expectedExceptions = OpenBankingException.class)
    public void testExecuteDeleteRequestWithUnexpectedResponse() throws OpenBankingException, IOException {
        mockHttpDeleteResponse();
        uut.executeDeleteRequest("test-app-id", "https://test.com/url/", Collections.singletonList(200));
    }

    private void mockOpenBankingCDSConfigParser() {
        obCDSConfigParserMock = Mockito.mock(OpenBankingCDSConfigParser.class);
        Mockito.when(obCDSConfigParserMock.getApimApplicationsSearchUrl())
                .thenReturn("https://localhost:9443/api/am/admin/v2/applications");
        Mockito.when(obCDSConfigParserMock.getDcrInternalUrl())
                .thenReturn("https://localhost:9446/api/openbanking/dynamic-client-registration/register");

        PowerMockito.mockStatic(OpenBankingCDSConfigParser.class);
        PowerMockito.when(OpenBankingCDSConfigParser.getInstance()).thenReturn(obCDSConfigParserMock);
    }

    private void mockServiceProvider() {
        InboundAuthenticationRequestConfig reqConfigMock = Mockito.mock(InboundAuthenticationRequestConfig.class);
        Mockito.when(reqConfigMock.getInboundAuthKey()).thenReturn("s3qziY_X47dyXfsNjFyevKaJYkUa");

        InboundAuthenticationConfig configMock = Mockito.mock(InboundAuthenticationConfig.class);
        Mockito.when(configMock.getInboundAuthenticationRequestConfigs())
                .thenReturn(new InboundAuthenticationRequestConfig[]{reqConfigMock});

        serviceProviderMock = Mockito.mock(ServiceProvider.class);
        Mockito.when(serviceProviderMock.getApplicationName()).thenReturn(APPLICATION_NAME);
        Mockito.when(serviceProviderMock.getInboundAuthenticationConfig()).thenReturn(configMock);
    }

    private void mockHttpDeleteResponse() throws IOException, OpenBankingException {
        StatusLine statusLineMock = Mockito.mock(StatusLine.class);
        Mockito.when(statusLineMock.getStatusCode()).thenReturn(HttpStatus.SC_NO_CONTENT);

        CloseableHttpResponse httpResponseMock = Mockito.mock(CloseableHttpResponse.class);
        Mockito.when(httpResponseMock.getStatusLine()).thenReturn(statusLineMock);

        CloseableHttpClient httpClientMock = Mockito.mock(CloseableHttpClient.class);
        Mockito.when(httpClientMock.execute(Mockito.any(HttpUriRequest.class))).thenReturn(httpResponseMock);

        PowerMockito.mockStatic(HTTPClientUtils.class);
        PowerMockito.when(HTTPClientUtils.getHttpsClient()).thenReturn(httpClientMock);
    }

    private void mockHttpGetResponse(int statusCode) throws IOException, OpenBankingException {
        StatusLine statusLineMock = Mockito.mock(StatusLine.class);
        Mockito.when(statusLineMock.getStatusCode()).thenReturn(statusCode);

        JSONObject application = new JSONObject();
        application.put(MetadataConstants.APPLICATION_ID, "c74e2fcc-6801-4340-83bf-732659272874");
        application.put(MetadataConstants.APPLICATION_NAME, APPLICATION_NAME);
        JSONArray applicationList = new JSONArray();
        applicationList.put(application);

        JSONObject response = new JSONObject();
        response.put(MetadataConstants.LIST, applicationList);
        InputStream inputStream = new ByteArrayInputStream(response.toString().getBytes(StandardCharsets.UTF_8));

        HttpEntity httpEntityMock = Mockito.mock(HttpEntity.class);
        Mockito.when(httpEntityMock.getContent()).thenReturn(inputStream);
        Mockito.when(httpEntityMock.toString()).thenReturn(response.toString());

        CloseableHttpResponse httpResponseMock = Mockito.mock(CloseableHttpResponse.class);
        Mockito.when(httpResponseMock.getStatusLine()).thenReturn(statusLineMock);
        Mockito.when(httpResponseMock.getEntity()).thenReturn(httpEntityMock);

        CloseableHttpClient httpClientMock = Mockito.mock(CloseableHttpClient.class);
        Mockito.when(httpClientMock.execute(Mockito.any(HttpGet.class))).thenReturn(httpResponseMock);

        PowerMockito.mockStatic(HTTPClientUtils.class);
        PowerMockito.when(HTTPClientUtils.getHttpsClient()).thenReturn(httpClientMock);
    }
}

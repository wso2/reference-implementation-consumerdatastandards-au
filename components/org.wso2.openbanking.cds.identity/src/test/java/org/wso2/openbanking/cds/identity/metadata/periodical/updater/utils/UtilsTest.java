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

package org.wso2.openbanking.cds.identity.metadata.periodical.updater.utils;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.identity.util.HTTPClientUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
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
import org.wso2.openbanking.cds.common.metadata.periodical.updater.constants.MetadataConstants;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Test class for metadata Utils.
 */
@PrepareForTest({HTTPClientUtils.class})
@PowerMockIgnore("jdk.internal.reflect.*")
public class UtilsTest extends PowerMockTestCase {

    private static final String TEST_URL = "https://localhost:8080/test-url.com";

    @BeforeClass
    public void init() throws OpenBankingException, IOException {
        mockHttpGetResponse();
    }

    @Test
    public void testReadJsonFromUrl() throws OpenBankingException, IOException {
        Assert.assertNull(Utils.readJsonFromUrl(""));
        Assert.assertNull(Utils.readJsonFromUrl(TEST_URL));
        Assert.assertThrows(OpenBankingException.class, () -> Utils.readJsonFromUrl(TEST_URL));
        Assert.assertNotNull(Utils.readJsonFromUrl(TEST_URL));
        Assert.assertNull(Utils.readJsonFromUrl(TEST_URL));
        Assert.assertThrows(OpenBankingException.class, () -> Utils.readJsonFromUrl(TEST_URL));
    }

    private void mockHttpGetResponse() throws IOException, OpenBankingException {
        StatusLine statusLineMock = Mockito.mock(StatusLine.class);
        Mockito.when(statusLineMock.getStatusCode())
                .thenReturn(HttpStatus.SC_OK)
                .thenReturn(HttpStatus.SC_BAD_REQUEST)
                .thenReturn(HttpStatus.SC_GATEWAY_TIMEOUT);

        JSONObject application = new JSONObject();
        application.put(MetadataConstants.APPLICATION_ID, "test-application-id");
        application.put(MetadataConstants.APPLICATION_NAME, "test-application-name");
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
        Mockito.when(httpResponseMock.getEntity())
                .thenReturn(null)
                .thenReturn(httpEntityMock);

        CloseableHttpClient httpClientMock = Mockito.mock(CloseableHttpClient.class);
        Mockito.when(httpClientMock.execute(Mockito.any(HttpGet.class)))
                .thenThrow(new NullPointerException())
                .thenReturn(httpResponseMock);

        PowerMockito.mockStatic(HTTPClientUtils.class);
        PowerMockito.when(HTTPClientUtils.getHttpsClient()).thenReturn(httpClientMock);
    }
}

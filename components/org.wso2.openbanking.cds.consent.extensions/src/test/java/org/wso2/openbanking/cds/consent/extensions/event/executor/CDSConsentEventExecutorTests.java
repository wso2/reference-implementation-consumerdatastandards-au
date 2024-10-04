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

package org.wso2.openbanking.cds.consent.extensions.event.executor;

import com.wso2.openbanking.accelerator.common.event.executor.model.OBEvent;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.data.publisher.common.util.OBDataPublisherUtil;
import com.wso2.openbanking.accelerator.identity.util.HTTPClientUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;
import org.wso2.openbanking.cds.identity.utils.CDSIdentityUtil;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Test class for CDS Consent Event Executor.
 */
@PrepareForTest({OpenBankingCDSConfigParser.class, HTTPClientUtils.class, ServerConfiguration.class,
        CDSIdentityUtil.class, OBDataPublisherUtil.class})
@PowerMockIgnore("jdk.internal.reflect.*")
public class CDSConsentEventExecutorTests extends PowerMockTestCase {

    private static ByteArrayOutputStream outContent;
    private static Logger logger = null;
    private static PrintStream printStream;

    @BeforeClass
    public void beforeTests() {

        outContent = new ByteArrayOutputStream();
        printStream = new PrintStream(outContent);
        System.setOut(printStream);
        logger = LogManager.getLogger(CDSConsentEventExecutorTests.class);
    }

    @Test
    public void testProcessEventSuccess() throws Exception {

        CDSConsentEventExecutor cdsConsentEventExecutorSpy = Mockito.spy(new CDSConsentEventExecutor());

        outContent.reset();
        Map<String, Object> configs = new HashMap<>();
        configs.put("RecipientConsentRevocationEndpoint.Enable", true);
        configs.put("DataHolder.ClientId", "dummyHolderId");

        OpenBankingCDSConfigParser openBankingCDSConfigParserMock = mock(OpenBankingCDSConfigParser.class);
        mockStatic(OpenBankingCDSConfigParser.class);
        when(OpenBankingCDSConfigParser.getInstance()).thenReturn(openBankingCDSConfigParserMock);
        when(openBankingCDSConfigParserMock.getConfiguration()).thenReturn(configs);

        mockStatic(OBDataPublisherUtil.class);
        doNothing().when(OBDataPublisherUtil.class, "publishData", Mockito.anyString(), Mockito.anyString(),
                Mockito.anyObject());

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("Reason", "Revoke the consent from dashboard");
        eventData.put("ConsentId", "dummyConsentId");
        eventData.put("ClientId", "dummyClientId");

        HashMap<String, Object> consentDataMap = new HashMap<>();

        HashMap<String, String> consentAttributes = new HashMap<>();
        consentAttributes.put("customerProfileType", "individual-profile");
        consentAttributes.put("sharing_duration_value", "6000");

        ConsentResource consentResource = new ConsentResource();
        consentResource.setConsentAttributes(consentAttributes);

        DetailedConsentResource detailedConsentResource = new DetailedConsentResource();
        detailedConsentResource.setConsentAttributes(consentAttributes);

        consentDataMap.put("ConsentResource", consentResource);
        consentDataMap.put("DetailedConsentResource", detailedConsentResource);

        eventData.put("ConsentDataMap", consentDataMap);

        OBEvent obEvent = new OBEvent("revoked", eventData);

        doNothing().when(cdsConsentEventExecutorSpy).sendArrangementRevocationRequestToADR(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString());
        cdsConsentEventExecutorSpy.processEvent(obEvent);

        Assert.assertTrue(outContent.toString().contains("Publishing consent data for metrics."));
    }

    @Test
    public void testProcessEventFailure() throws Exception {

        CDSConsentEventExecutor cdsConsentEventExecutorSpy = Mockito.spy(new CDSConsentEventExecutor());

        outContent.reset();
        Map<String, Object> configs = new HashMap<>();
        configs.put("RecipientConsentRevocationEndpoint.Enable", true);
        configs.put("DataHolder.ClientId", "dummyHolderId");

        OpenBankingCDSConfigParser openBankingCDSConfigParserMock = mock(OpenBankingCDSConfigParser.class);
        mockStatic(OpenBankingCDSConfigParser.class);
        when(OpenBankingCDSConfigParser.getInstance()).thenReturn(openBankingCDSConfigParserMock);
        when(openBankingCDSConfigParserMock.getConfiguration()).thenReturn(configs);

        mockStatic(OBDataPublisherUtil.class);
        doNothing().when(OBDataPublisherUtil.class, "publishData", Mockito.anyString(), Mockito.anyString(),
                Mockito.anyObject());

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("Reason", "Revoke the consent from dashboard");
        eventData.put("ConsentId", "dummyConsentId");
        eventData.put("ClientId", "dummyClientId");

        HashMap<String, Object> consentDataMap = new HashMap<>();

        HashMap<String, String> consentAttributes = new HashMap<>();
        consentAttributes.put("customerProfileType", "individual-profile");
        consentAttributes.put("sharing_duration_value", "6000");

        ConsentResource consentResource = new ConsentResource();
        consentResource.setConsentAttributes(consentAttributes);

        DetailedConsentResource detailedConsentResource = new DetailedConsentResource();
        detailedConsentResource.setConsentAttributes(consentAttributes);

        consentDataMap.put("ConsentResource", consentResource);
        consentDataMap.put("DetailedConsentResource", detailedConsentResource);

        eventData.put("ConsentDataMap", consentDataMap);

        OBEvent obEvent = new OBEvent("revoked", eventData);

        Mockito.doThrow(OpenBankingException.class).when(cdsConsentEventExecutorSpy)
                .sendArrangementRevocationRequestToADR(Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyString());
        cdsConsentEventExecutorSpy.processEvent(obEvent);

        Assert.assertTrue(outContent.toString().contains("Something went wrong when sending " +
                "the arrangement revocation request to ADR"));
    }

    @Test
    public void testSendArrangementRevocationToADRSuccess() throws Exception {

        CDSConsentEventExecutor cdsConsentEventExecutorSpy = Mockito.spy(new CDSConsentEventExecutor());

        doReturn("dummyUri").when(cdsConsentEventExecutorSpy).getRecipientBaseUri(Mockito.anyString());

        CloseableHttpResponse httpResponseMock = mock(CloseableHttpResponse.class);
        CloseableHttpClient closeableHttpClientMock = mock(CloseableHttpClient.class);
        doReturn(httpResponseMock).when(closeableHttpClientMock).execute(Mockito.any(HttpPost.class));

        mockStatic(HTTPClientUtils.class);
        when(HTTPClientUtils.getHttpsClient()).thenReturn(closeableHttpClientMock);

        StatusLine statusLineMock = mock(StatusLine.class);
        doReturn(HttpStatus.SC_OK).when(statusLineMock).getStatusCode();
        when(httpResponseMock.getStatusLine()).thenReturn(statusLineMock);
        HttpEntity httpEntityMock = mock(HttpEntity.class);
        when(httpResponseMock.getEntity()).thenReturn(httpEntityMock);

        doReturn("dummyJWT").when(cdsConsentEventExecutorSpy).generateJWT(Mockito.anyString(),
                Mockito.anyObject());
        try {
            cdsConsentEventExecutorSpy.sendArrangementRevocationRequestToADR("dummyClientId",
                    "dummyConsentId", "dummyDataHolderId");
        } catch (OpenBankingException e) {
            Assert.fail("Should not throw exception");
        }

    }

    @Test(expectedExceptions = OpenBankingException.class)
    public void testSendArrangementRevocationToADRFailure() throws Exception {

        CDSConsentEventExecutor cdsConsentEventExecutorSpy = Mockito.spy(new CDSConsentEventExecutor());

        doReturn("dummyUri").when(cdsConsentEventExecutorSpy).getRecipientBaseUri(Mockito.anyString());

        CloseableHttpResponse httpResponseMock = mock(CloseableHttpResponse.class);
        CloseableHttpClient closeableHttpClientMock = mock(CloseableHttpClient.class);
        doReturn(httpResponseMock).when(closeableHttpClientMock).execute(Mockito.any(HttpPost.class));

        mockStatic(HTTPClientUtils.class);
        when(HTTPClientUtils.getHttpsClient()).thenReturn(closeableHttpClientMock);

        StatusLine statusLineMock = mock(StatusLine.class);
        doReturn(HttpStatus.SC_NOT_FOUND).when(statusLineMock).getStatusCode();
        when(httpResponseMock.getStatusLine()).thenReturn(statusLineMock);
        HttpEntity httpEntityMock = mock(HttpEntity.class);
        when(httpResponseMock.getEntity()).thenReturn(httpEntityMock);

        doReturn("dummyJWT").when(cdsConsentEventExecutorSpy).generateJWT(Mockito.anyString(),
                Mockito.anyObject());
        cdsConsentEventExecutorSpy.sendArrangementRevocationRequestToADR("dummyClientId",
                "dummyConsentId", "dummyDataHolderId");
    }
}

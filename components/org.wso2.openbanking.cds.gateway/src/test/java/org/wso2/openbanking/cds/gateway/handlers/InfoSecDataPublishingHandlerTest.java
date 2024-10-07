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

package org.wso2.openbanking.cds.gateway.handlers;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.data.publisher.common.util.OBDataPublisherUtil;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.openbanking.cds.gateway.utils.GatewayConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Test class for InfoSecDataPublishingHandler.
 */
@PrepareForTest({OpenBankingConfigParser.class, OBDataPublisherUtil.class, JsonUtil.class})
@PowerMockIgnore("jdk.internal.reflect.*")
public class InfoSecDataPublishingHandlerTest extends PowerMockTestCase {

    MessageContext messageContext;

    @BeforeMethod
    public void beforeMethod() throws Exception {

        Map<String, Object> configs = new HashMap<>();
        configs.put("DataPublishing.Enabled", "true");
        configs.put(GatewayConstants.CLIENT_USER_AGENT, "dummyAgent");

        mockStatic(OpenBankingConfigParser.class);
        OpenBankingConfigParser openBankingConfigParserMock = mock(OpenBankingConfigParser.class);
        when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParserMock);
        when(openBankingConfigParserMock.getConfiguration()).thenReturn(configs);

        SynapseConfiguration synapseConfigurationMock = mock(SynapseConfiguration.class);
        SynapseEnvironment synapseEnvironmentMock = mock(SynapseEnvironment.class);
        org.apache.axis2.context.MessageContext messageContextMock =
                mock(org.apache.axis2.context.MessageContext.class);
        messageContext = new Axis2MessageContext(messageContextMock, synapseConfigurationMock,
                synapseEnvironmentMock);

        messageContext.setProperty(GatewayConstants.HTTP_RESPONSE_STATUS_CODE, 500);
        messageContext.setProperty(GatewayConstants.REST_API_CONTEXT, "/token");
        messageContext.setProperty(GatewayConstants.REST_METHOD, "POST");
        org.apache.axis2.context.MessageContext axis2MessageContext = new org.apache.axis2.context.MessageContext();
        axis2MessageContext.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, configs);

        axis2MessageContext.setProperty(GatewayConstants.HTTP_SC, "500");
        ((Axis2MessageContext) messageContext).setAxis2MessageContext(axis2MessageContext);

        mockStatic(OBDataPublisherUtil.class);
        doNothing().when(OBDataPublisherUtil.class, "publishData", Mockito.anyString(), Mockito.anyString(),
                Mockito.anyObject());

        mockStatic(JsonUtil.class);
        OMElement omElementMock = mock(OMElement.class);
        when(JsonUtil.getNewJsonPayload(Mockito.anyObject(), Mockito.anyString(), Mockito.anyBoolean(),
                Mockito.anyBoolean())).thenReturn(omElementMock);
    }

    @Test(description = "Test the attributes in the invocation data map")
    public void invocationDataMapAttributesTest() {

        InfoSecDataPublishingHandler handler = Mockito.spy(InfoSecDataPublishingHandler.class);
        String messageId = UUID.randomUUID().toString();
        messageContext.setProperty("REQUEST_IN_TIME", System.currentTimeMillis());
        Map<String, Object> latencyData = handler.generateInvocationDataMap(messageContext, messageId);
        assertEquals(latencyData.get("messageId"), messageId);
        assertEquals(latencyData.get("customerStatus"), GatewayConstants.UNDEFINED);
        assertEquals(latencyData.get("apiName"), GatewayConstants.TOKEN_API);
        assertEquals(latencyData.get("electedResource"), GatewayConstants.TOKEN_ENDPOINT);
        assertNotNull(latencyData.get("timestamp"));
        assertNotNull(latencyData.get("responsePayloadSize"));
        assertNotNull(latencyData.get("httpMethod"));
        assertNotNull(latencyData.get("statusCode"));
        assertNotNull(latencyData.get("userAgent"));
    }

    @Test(description = "Test the attributes in the latency data map")
    public void latencyDataMapAttributesTest() {

        InfoSecDataPublishingHandler handler = Mockito.spy(InfoSecDataPublishingHandler.class);
        String messageId = UUID.randomUUID().toString();
        messageContext.setProperty("REQUEST_IN_TIME", System.currentTimeMillis());
        Map<String, Object> latencyData = handler.generateLatencyDataMap(messageContext, messageId);
        assertEquals(latencyData.get("correlationId"), messageId);
        assertNotNull(latencyData.get("requestTimestamp"));
        assertNotNull(latencyData.get("backendLatency"));
        assertNotNull(latencyData.get("requestMediationLatency"));
        assertNotNull(latencyData.get("responseLatency"));
        assertNotNull(latencyData.get("responseMediationLatency"));
    }

    @Test(description = "Test the ResponseLatency attribute in the latency data map")
    public void latencyDataMapNegativeResponseLatencyTest() {

        InfoSecDataPublishingHandler handler = Mockito.spy(InfoSecDataPublishingHandler.class);
        String messageId = UUID.randomUUID().toString();
        messageContext.setProperty("REQUEST_IN_TIME", System.currentTimeMillis() + (60 * 1000));
        Map<String, Object> latencyData = handler.generateLatencyDataMap(messageContext, messageId);
        assertEquals(latencyData.get("responseLatency"), 0L);
    }

}

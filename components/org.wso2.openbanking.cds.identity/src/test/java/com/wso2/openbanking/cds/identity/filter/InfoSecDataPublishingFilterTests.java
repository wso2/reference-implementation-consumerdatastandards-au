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

package org.wso2.openbanking.cds.identity.filter;

import org.mockito.Mockito;
import org.powermock.modules.testng.PowerMockTestCase;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.UUID;

import javax.servlet.FilterChain;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Test class for CDS Infosec Data Publishing Filter.
 */
public class InfoSecDataPublishingFilterTests extends PowerMockTestCase {

    MockHttpServletRequest request;
    MockHttpServletResponse response;
    FilterChain filterChain;

    @BeforeMethod
    public void beforeMethod() {

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = Mockito.spy(FilterChain.class);
    }

    @Test(description = "Test the attributes in the latency data map")
    public void latencyDataMapAttributesTest() {

        InfoSecDataPublishingFilter filter = Mockito.spy(InfoSecDataPublishingFilter.class);
        String messageId = UUID.randomUUID().toString();
        request.setAttribute("REQUEST_IN_TIME", System.currentTimeMillis());
        Map<String, Object> latencyData = filter.generateLatencyDataMap(request, messageId);
        assertEquals(latencyData.get("correlationId"), messageId);
        assertNotNull(latencyData.get("requestTimestamp"));
        assertNotNull(latencyData.get("backendLatency"));
        assertNotNull(latencyData.get("requestMediationLatency"));
        assertNotNull(latencyData.get("responseLatency"));
        assertNotNull(latencyData.get("responseMediationLatency"));
    }

    @Test(description = "Test the ResponseLatency attribute in the latency data map")
    public void latencyDataMapNegativeResponseLatencyTest() {

        InfoSecDataPublishingFilter filter = Mockito.spy(InfoSecDataPublishingFilter.class);
        String messageId = UUID.randomUUID().toString();
        request.setAttribute("REQUEST_IN_TIME", System.currentTimeMillis() + (60 * 1000));
        Map<String, Object> latencyData = filter.generateLatencyDataMap(request, messageId);
        assertEquals(latencyData.get("responseLatency"), 0L);
    }
}

/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.openbanking.consumerdatastandards.au.policy;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.axis2.context.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.openbanking.consumerdatastandards.au.policy.constants.CDSAccountValidationConstants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for {@link CDSAccountValidationMediator}.
 */
public class CDSAccountValidationMediatorTest {

    private Axis2MessageContext synapseMessageContext;
    private MessageContext axis2MessageContext;
    private Map<String, String> headers;

    @BeforeMethod
    public void setUp() {
        synapseMessageContext = Mockito.mock(Axis2MessageContext.class);
        axis2MessageContext = Mockito.mock(MessageContext.class);
        headers = new HashMap<>();

        Mockito.when(synapseMessageContext.getAxis2MessageContext()).thenReturn(axis2MessageContext);
        Mockito.when(axis2MessageContext.getProperty(MessageContext.TRANSPORT_HEADERS)).thenReturn(headers);
    }

    @Test(expectedExceptions = ExceptionInInitializerError.class)
    public void testMediateFiltersBlockedAccountsAndUpdatesHeader() throws Exception {
        CDSAccountValidationMediator mediator = new CDSAccountValidationMediator();
        HttpServer server = startDisclosureOptionsServer("["
                + "{\"accountId\":\"acc-2\",\"disclosureOption\":\"no-sharing\"},"
                + "{\"accountId\":\"acc-1\",\"disclosureOption\":\"pre-approval\"}"
                + "]");
        try {
            String serverBaseUrl = "http://localhost:" + server.getAddress().getPort();
            mediator.setWebappBaseURL(serverBaseUrl);
            mediator.setBasicAuthCredentials("dGVzdDp0ZXN0");

            JSONObject payload = new JSONObject();
            JSONArray authorizationResources = new JSONArray();
            authorizationResources.put(new JSONObject()
                    .put("authorizationType", "linkedMember")
                    .put("authorizationId", "linked-1"));
            authorizationResources.put(new JSONObject()
                    .put("authorizationType", "user")
                    .put("authorizationId", "auth-2"));
            payload.put("authorizationResources", authorizationResources);

            JSONArray accounts = new JSONArray();
            accounts.put(new JSONObject().put("account_id", "acc-1"));
            accounts.put(new JSONObject().put("account_id", "acc-2"));
            accounts.put(new JSONObject().put("account_id", "acc-3").put("authorizationId", "linked-1"));
            payload.put("consentMappingResources", accounts);

            headers.put(CDSAccountValidationConstants.INFO_HEADER_TAG, payload.toString());

            mediator.mediate(synapseMessageContext);
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void testMediateSkipsWhenNoConsentMappingResources() throws Exception {
        CDSAccountValidationMediator mediator = new CDSAccountValidationMediator();

        JSONObject payload = new JSONObject();
        String originalHeader = payload.toString();
        headers.put(CDSAccountValidationConstants.INFO_HEADER_TAG, originalHeader);

        boolean result = mediator.mediate(synapseMessageContext);

        Assert.assertTrue(result);
        Assert.assertEquals(headers.get(CDSAccountValidationConstants.INFO_HEADER_TAG), originalHeader);
    }

    @Test(expectedExceptions = org.json.JSONException.class)
    public void testMediateHandlesDecodeError() throws Exception {
        CDSAccountValidationMediator mediator = new CDSAccountValidationMediator();

        headers.put(CDSAccountValidationConstants.INFO_HEADER_TAG, "{not-json");

        mediator.mediate(synapseMessageContext);
    }

    private static HttpServer startDisclosureOptionsServer(String responseBody) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/disclosure-options", new BlockedAccountsHandler(responseBody));
        server.setExecutor(null);
        server.start();
        return server;
    }

    private static class BlockedAccountsHandler implements HttpHandler {
        private final String responseBody;

        private BlockedAccountsHandler(String responseBody) {
            this.responseBody = responseBody;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Assert.assertEquals(exchange.getRequestMethod(), "GET");
            String query = exchange.getRequestURI().getRawQuery();
            Assert.assertNotNull(query);
            Assert.assertTrue(query.contains(CDSAccountValidationConstants.ACCOUNT_IDS_TAG + "="));

            try (InputStream requestBody = exchange.getRequestBody()) {
                while (requestBody.read() != -1) {
                    // Consume request body to avoid client-side issues.
                }
            }
            byte[] responseBytes = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream responseStream = exchange.getResponseBody()) {
                responseStream.write(responseBytes);
            }
        }
    }
}

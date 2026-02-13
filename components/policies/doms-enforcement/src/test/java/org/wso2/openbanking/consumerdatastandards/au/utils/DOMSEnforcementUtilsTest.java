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

package org.wso2.openbanking.consumerdatastandards.au.utils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

public class DOMSEnforcementUtilsTest {

    @Test(expectedExceptions = ParseException.class)
    public void testDecodeJwtRejectsBlank() throws Exception {
        DOMSEnforcementUtils.decodeJWT(" ");
    }

    @Test(expectedExceptions = ParseException.class)
    public void testDecodeJwtRejectsInvalidFormat() throws Exception {
        DOMSEnforcementUtils.decodeJWT("one.two");
    }

    @Test(expectedExceptions = ParseException.class)
    public void testDecodeJwtRejectsInvalidBase64() throws Exception {
        DOMSEnforcementUtils.decodeJWT("aaa.###.bbb");
    }

    @Test
    public void testDecodeJwtReturnsPayload() throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("sub", "user-1");
        payload.put("aud", "doms");

        String encodedPayload = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(payload.toString().getBytes(StandardCharsets.UTF_8));
        String jwt = "header." + encodedPayload + ".signature";

        JSONObject decoded = DOMSEnforcementUtils.decodeJWT(jwt);

        Assert.assertEquals(decoded.getString("sub"), "user-1");
        Assert.assertEquals(decoded.getString("aud"), "doms");
    }

    @Test
    public void testFetchBlockedAccountsFromServiceSuccess() throws Exception {
        HttpServer server = startBlockedAccountsServer(
                "{\"blockedAccountIds\":[\"acc-1\",\"acc-3\"]}");
        try {
            String serverUrl = "http://localhost:" + server.getAddress().getPort() + "/blocked";
            Set<String> accounts = new HashSet<>();
            accounts.add("acc-1");
            accounts.add("acc-2");
            accounts.add("acc-3");

            Set<String> blocked = DOMSEnforcementUtils.fetchBlockedAccountsFromService(
                    accounts, serverUrl, "");

            Assert.assertEquals(blocked.size(), 2);
            Assert.assertTrue(blocked.contains("acc-1"));
            Assert.assertTrue(blocked.contains("acc-3"));
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void testFetchBlockedAccountsFromServiceNon200() throws Exception {
        HttpServer server = startBlockedAccountsServerWithStatus(
                "{\"blockedAccountIds\":[\"acc-1\"]}", 500);
        try {
            String serverUrl = "http://localhost:" + server.getAddress().getPort() + "/blocked";
            Set<String> accounts = new HashSet<>();
            accounts.add("acc-1");

            Set<String> blocked = DOMSEnforcementUtils.fetchBlockedAccountsFromService(
                    accounts, serverUrl, "");

            Assert.assertTrue(blocked.isEmpty());
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void testFetchBlockedAccountsFromServiceIoError() {
        Set<String> accounts = new HashSet<>();
        accounts.add("acc-1");

        Set<String> blocked = DOMSEnforcementUtils.fetchBlockedAccountsFromService(
                accounts, "http://localhost:1/blocked", "");

        Assert.assertTrue(blocked.isEmpty());
    }

    @Test
    public void testFetchBlockedAccountsWithAuthHeader() throws Exception {
        String expectedAuth = "Basic " + Base64.getEncoder().encodeToString("user:pass".getBytes());
        HttpServer server = startAuthVerifyingServer(expectedAuth);
        try {
            String serverUrl = "http://localhost:" + server.getAddress().getPort() + "/blocked";
            Set<String> accounts = new HashSet<>();
            accounts.add("acc-1");

            String basicAuth = Base64.getEncoder().encodeToString("user:pass".getBytes());
            Set<String> blocked = DOMSEnforcementUtils.fetchBlockedAccountsFromService(
                    accounts, serverUrl, basicAuth);

            Assert.assertEquals(blocked.size(), 1);
            Assert.assertTrue(blocked.contains("acc-1"));
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void testFetchBlockedAccountsWithoutAuthHeader() throws Exception {
        HttpServer server = startAuthVerifyingServer(null);
        try {
            String serverUrl = "http://localhost:" + server.getAddress().getPort() + "/blocked";
            Set<String> accounts = new HashSet<>();
            accounts.add("acc-2");

            Set<String> blocked = DOMSEnforcementUtils.fetchBlockedAccountsFromService(
                    accounts, serverUrl, "");

            Assert.assertEquals(blocked.size(), 1);
            Assert.assertTrue(blocked.contains("acc-2"));
        } finally {
            server.stop(0);
        }
    }

    private static HttpServer startAuthVerifyingServer(String expectedAuthHeader) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/blocked", new AuthVerifyingHandler(expectedAuthHeader));
        server.setExecutor(null);
        server.start();
        return server;
    }

    private static HttpServer startBlockedAccountsServer(String responseBody) throws IOException {
        return startBlockedAccountsServerWithStatus(responseBody, 200);
    }

    private static HttpServer startBlockedAccountsServerWithStatus(String responseBody, int statusCode)
            throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/blocked", new BlockedAccountsHandler(responseBody, statusCode));
        server.setExecutor(null);
        server.start();
        return server;
    }

    private static class BlockedAccountsHandler implements HttpHandler {
        private final String responseBody;
        private final int statusCode;

        private BlockedAccountsHandler(String responseBody, int statusCode) {
            this.responseBody = responseBody;
            this.statusCode = statusCode;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try (InputStream requestBody = exchange.getRequestBody()) {
                while (requestBody.read() != -1) {
                    // Consume request body to avoid client-side issues.
                }
            }
            byte[] responseBytes = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, responseBytes.length);
            try (OutputStream responseStream = exchange.getResponseBody()) {
                responseStream.write(responseBytes);
            }
        }
    }

    private static class AuthVerifyingHandler implements HttpHandler {
        private final String expectedAuthHeader;

        private AuthVerifyingHandler(String expectedAuthHeader) {
            this.expectedAuthHeader = expectedAuthHeader;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try (InputStream requestBody = exchange.getRequestBody()) {
                while (requestBody.read() != -1) {
                    // Consume request body
                }
            }

            // Verify auth header is set correctly
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if (expectedAuthHeader != null) {
                Assert.assertEquals(authHeader, expectedAuthHeader, "Authorization header mismatch");
            } else {
                Assert.assertNull(authHeader, "Authorization header should not be present");
            }

            // Return a valid response
            JSONObject response = new JSONObject();
            response.put("blockedAccountIds", new org.json.JSONArray()
                    .put(expectedAuthHeader != null ? "acc-1" : "acc-2"));
            byte[] responseBytes = response.toString().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream responseStream = exchange.getResponseBody()) {
                responseStream.write(responseBytes);
            }
        }
    }
}

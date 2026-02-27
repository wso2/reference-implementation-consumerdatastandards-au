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

package org.wso2.openbanking.consumerdatastandards.au.policy.utils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.openbanking.consumerdatastandards.au.policy.constants.CDSAccountValidationConstants;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

/**
 * Unit tests for {@link CDSAccountValidationUtils}.
 */
public class CDSAccountValidationUtilsTest {

        private static final String ACCOUNT_METADATA_WEBAPP_BASE_URL = "http://account-metadata-webapp-base-url";
        private static final String DOMS_ENDPOINT = ACCOUNT_METADATA_WEBAPP_BASE_URL + "/disclosure-options";

    @Test
    public void testFetchBlockedAccountsFromServiceSuccess() throws Exception {
        HttpClient client = Mockito.mock(HttpClient.class);
        HttpClient.Builder clientBuilder = Mockito.mock(HttpClient.Builder.class);
        HttpResponse<String> response = Mockito.mock(HttpResponse.class);

        Mockito.when(clientBuilder.connectTimeout(Mockito.any())).thenReturn(clientBuilder);
        Mockito.when(clientBuilder.build()).thenReturn(client);
        Mockito.when(response.statusCode()).thenReturn(200);
        Mockito.when(response.body()).thenReturn("["
                + "{\"accountId\":\"acc-1\",\"disclosureOption\":\"no-sharing\"},"
                + "{\"accountId\":\"acc-2\",\"disclosureOption\":\"pre-approval\"},"
                + "{\"accountId\":\"acc-3\",\"disclosureOption\":\"no-sharing\"}"
                + "]");
        Mockito.when(client.send(Mockito.any(HttpRequest.class), Mockito.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(response);

        try (MockedStatic<HttpClient> mockedHttpClient = Mockito.mockStatic(HttpClient.class)) {
            mockedHttpClient.when(HttpClient::newBuilder).thenReturn(clientBuilder);

            Set<String> accounts = new HashSet<>();
            accounts.add("acc-1");
            accounts.add("acc-2");
            accounts.add("acc-3");

            Set<String> blocked = CDSAccountValidationUtils.fetchBlockedJointAccountsFromService(
                    accounts, DOMS_ENDPOINT, "");

            Assert.assertEquals(blocked.size(), 2);
            Assert.assertTrue(blocked.contains("acc-1"));
            Assert.assertTrue(blocked.contains("acc-3"));

            ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
            Mockito.verify(client).send(requestCaptor.capture(), Mockito.<HttpResponse.BodyHandler<String>>any());
            Assert.assertTrue(requestCaptor.getValue().uri().toString().contains("accountIds="));
        }
    }

    @Test
    public void testFetchBlockedAccountsFromServiceNon200() throws Exception {
        HttpClient client = Mockito.mock(HttpClient.class);
        HttpClient.Builder clientBuilder = Mockito.mock(HttpClient.Builder.class);
        HttpResponse<String> response = Mockito.mock(HttpResponse.class);

        Mockito.when(clientBuilder.connectTimeout(Mockito.any())).thenReturn(clientBuilder);
        Mockito.when(clientBuilder.build()).thenReturn(client);
        Mockito.when(response.statusCode()).thenReturn(500);
        Mockito.when(response.body()).thenReturn("[]");
        Mockito.when(client.send(Mockito.any(HttpRequest.class), Mockito.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(response);

        try (MockedStatic<HttpClient> mockedHttpClient = Mockito.mockStatic(HttpClient.class)) {
            mockedHttpClient.when(HttpClient::newBuilder).thenReturn(clientBuilder);

            Set<String> accounts = new HashSet<>();
            accounts.add("acc-1");

            Set<String> blocked = CDSAccountValidationUtils.fetchBlockedJointAccountsFromService(
                    accounts, DOMS_ENDPOINT, "");

            Assert.assertTrue(blocked.isEmpty());
        }
    }

    @Test
    public void testFetchBlockedAccountsFromServiceIoError() throws Exception {
        HttpClient client = Mockito.mock(HttpClient.class);
        HttpClient.Builder clientBuilder = Mockito.mock(HttpClient.Builder.class);

        Mockito.when(clientBuilder.connectTimeout(Mockito.any())).thenReturn(clientBuilder);
        Mockito.when(clientBuilder.build()).thenReturn(client);
        Mockito.when(client.send(Mockito.any(HttpRequest.class), Mockito.<HttpResponse.BodyHandler<String>>any()))
                .thenThrow(new IOException("Connection failed"));

        try (MockedStatic<HttpClient> mockedHttpClient = Mockito.mockStatic(HttpClient.class)) {
            mockedHttpClient.when(HttpClient::newBuilder).thenReturn(clientBuilder);

            Set<String> accounts = new HashSet<>();
            accounts.add("acc-1");

            Set<String> blocked = CDSAccountValidationUtils.fetchBlockedJointAccountsFromService(
                    accounts, DOMS_ENDPOINT, "");

            Assert.assertTrue(blocked.isEmpty());
        }
    }

    @Test
    public void testFetchBlockedAccountsWithAuthHeader() throws Exception {
        HttpClient client = Mockito.mock(HttpClient.class);
        HttpClient.Builder clientBuilder = Mockito.mock(HttpClient.Builder.class);
        HttpResponse<String> response = Mockito.mock(HttpResponse.class);

        Mockito.when(clientBuilder.connectTimeout(Mockito.any())).thenReturn(clientBuilder);
        Mockito.when(clientBuilder.build()).thenReturn(client);
        Mockito.when(response.statusCode()).thenReturn(200);
        Mockito.when(response.body()).thenReturn(new JSONArray()
                .put(new JSONObject()
                        .put(CDSAccountValidationConstants.CDS_ACCOUNT_ID_TAG, "acc-1")
                        .put(CDSAccountValidationConstants.DISCLOSURE_OPTION_TAG,
                                CDSAccountValidationConstants.DOMS_STATUS_NO_SHARING))
                .toString());
        Mockito.when(client.send(Mockito.any(HttpRequest.class), Mockito.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(response);

        try (MockedStatic<HttpClient> mockedHttpClient = Mockito.mockStatic(HttpClient.class)) {
            mockedHttpClient.when(HttpClient::newBuilder).thenReturn(clientBuilder);

            Set<String> accounts = new HashSet<>();
            accounts.add("acc-1");

            String basicAuth = Base64.getEncoder().encodeToString("user:pass".getBytes());
            Set<String> blocked = CDSAccountValidationUtils.fetchBlockedJointAccountsFromService(
                    accounts, DOMS_ENDPOINT, basicAuth);

            Assert.assertEquals(blocked.size(), 1);
            Assert.assertTrue(blocked.contains("acc-1"));

            ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
            Mockito.verify(client).send(requestCaptor.capture(), Mockito.<HttpResponse.BodyHandler<String>>any());
            Assert.assertEquals(requestCaptor.getValue().headers().firstValue("Authorization").orElse(null),
                    "Basic " + basicAuth);
        }
    }

    @Test
    public void testFetchBlockedAccountsWithoutAuthHeader() throws Exception {
        HttpClient client = Mockito.mock(HttpClient.class);
        HttpClient.Builder clientBuilder = Mockito.mock(HttpClient.Builder.class);
        HttpResponse<String> response = Mockito.mock(HttpResponse.class);

        Mockito.when(clientBuilder.connectTimeout(Mockito.any())).thenReturn(clientBuilder);
        Mockito.when(clientBuilder.build()).thenReturn(client);
        Mockito.when(response.statusCode()).thenReturn(200);
        Mockito.when(response.body()).thenReturn(new JSONArray()
                .put(new JSONObject()
                        .put(CDSAccountValidationConstants.CDS_ACCOUNT_ID_TAG, "acc-2")
                        .put(CDSAccountValidationConstants.DISCLOSURE_OPTION_TAG,
                                CDSAccountValidationConstants.DOMS_STATUS_NO_SHARING))
                .toString());
        Mockito.when(client.send(Mockito.any(HttpRequest.class), Mockito.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(response);

        try (MockedStatic<HttpClient> mockedHttpClient = Mockito.mockStatic(HttpClient.class)) {
            mockedHttpClient.when(HttpClient::newBuilder).thenReturn(clientBuilder);

            Set<String> accounts = new HashSet<>();
            accounts.add("acc-2");

            Set<String> blocked = CDSAccountValidationUtils.fetchBlockedJointAccountsFromService(
                    accounts, DOMS_ENDPOINT, "");

            Assert.assertEquals(blocked.size(), 1);
            Assert.assertTrue(blocked.contains("acc-2"));

            ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
            Mockito.verify(client).send(requestCaptor.capture(), Mockito.<HttpResponse.BodyHandler<String>>any());
            Assert.assertFalse(requestCaptor.getValue().headers().firstValue("Authorization").isPresent());
        }
    }
}

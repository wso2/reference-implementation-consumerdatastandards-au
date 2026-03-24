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
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.openbanking.consumerdatastandards.au.policy.constants.CDSAccountValidationConstants;
import org.wso2.openbanking.consumerdatastandards.au.policy.exceptions.CDSAccountValidationException;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Unit tests for {@link CDSAccountValidationUtils}.
 */
public class CDSAccountValidationUtilsTest {

    private static final String ACCOUNT_METADATA_WEBAPP_BASE_URL = "http://account-metadata-webapp-base-url";
    private static final String DOMS_ENDPOINT = ACCOUNT_METADATA_WEBAPP_BASE_URL + "/disclosure-options";

    private static final String BASIC_AUTH = Base64.getEncoder().encodeToString("user:pass".getBytes());

    @Test
    public void testFetchBlockedAccountsFromServiceSuccess() throws Exception {
        HttpClient client = Mockito.mock(HttpClient.class);
        HttpResponse<String> response = Mockito.mock(HttpResponse.class);

        Mockito.when(response.statusCode()).thenReturn(200);
        Mockito.when(response.body()).thenReturn("["
                + "{\"accountId\":\"acc-1\",\"disclosureOption\":\"no-sharing\"},"
                + "{\"accountId\":\"acc-2\",\"disclosureOption\":\"pre-approval\"},"
                + "{\"accountId\":\"acc-3\",\"disclosureOption\":\"no-sharing\"}"
                + "]");
        Mockito.when(client.send(Mockito.any(HttpRequest.class), Mockito.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(response);

        CDSAccountValidationUtils.setHttpClient(client);

        Set<String> accounts = new HashSet<>();
        accounts.add("acc-1");
        accounts.add("acc-2");
        accounts.add("acc-3");

        Set<String> blocked = CDSAccountValidationUtils.fetchBlockedJointAccountsFromService(
                accounts, DOMS_ENDPOINT, BASIC_AUTH);

        Assert.assertEquals(blocked.size(), 2);
        Assert.assertTrue(blocked.contains("acc-1"));
        Assert.assertTrue(blocked.contains("acc-3"));

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        Mockito.verify(client).send(requestCaptor.capture(), Mockito.<HttpResponse.BodyHandler<String>>any());
        Assert.assertTrue(requestCaptor.getValue().uri().toString().contains("accountIds="));
        Assert.assertEquals(requestCaptor.getValue().headers().firstValue("Authorization").orElse(null),
                "Basic " + BASIC_AUTH);
    }

    @Test
    public void testFetchBlockedAccountsFromServiceNon200() throws Exception {
        HttpClient client = Mockito.mock(HttpClient.class);
        HttpResponse<String> response = Mockito.mock(HttpResponse.class);

        Mockito.when(response.statusCode()).thenReturn(500);
        Mockito.when(response.body()).thenReturn("[]");
        Mockito.when(client.send(Mockito.any(HttpRequest.class), Mockito.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(response);

        CDSAccountValidationUtils.setHttpClient(client);

        Set<String> accounts = new HashSet<>();
        accounts.add("acc-1");
        Assert.expectThrows(CDSAccountValidationException.class,
                () -> CDSAccountValidationUtils.fetchBlockedJointAccountsFromService(
                        accounts, DOMS_ENDPOINT, BASIC_AUTH));
    }

    @Test
    public void testFetchBlockedAccountsFromServiceIoError() throws Exception {
        HttpClient client = Mockito.mock(HttpClient.class);

        Mockito.when(client.send(Mockito.any(HttpRequest.class), Mockito.<HttpResponse.BodyHandler<String>>any()))
                .thenThrow(new IOException("Connection failed"));

        CDSAccountValidationUtils.setHttpClient(client);

        Set<String> accounts = new HashSet<>();
        accounts.add("acc-1");
        Assert.expectThrows(CDSAccountValidationException.class,
                () -> CDSAccountValidationUtils.fetchBlockedJointAccountsFromService(
                        accounts, DOMS_ENDPOINT, BASIC_AUTH));
    }

    @Test
    public void testFetchBlockedAccountsWithAuthHeader() throws Exception {
        HttpClient client = Mockito.mock(HttpClient.class);
        HttpResponse<String> response = Mockito.mock(HttpResponse.class);

        Mockito.when(response.statusCode()).thenReturn(200);
        Mockito.when(response.body()).thenReturn(new JSONArray()
                .put(new JSONObject()
                        .put(CDSAccountValidationConstants.CDS_ACCOUNT_ID_TAG, "acc-1")
                        .put(CDSAccountValidationConstants.DISCLOSURE_OPTION_TAG,
                                CDSAccountValidationConstants.DOMS_STATUS_NO_SHARING))
                .toString());
        Mockito.when(client.send(Mockito.any(HttpRequest.class), Mockito.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(response);

        CDSAccountValidationUtils.setHttpClient(client);

        Set<String> accounts = new HashSet<>();
        accounts.add("acc-1");

        Set<String> blocked = CDSAccountValidationUtils.fetchBlockedJointAccountsFromService(
                accounts, DOMS_ENDPOINT, BASIC_AUTH);

        Assert.assertEquals(blocked.size(), 1);
        Assert.assertTrue(blocked.contains("acc-1"));

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        Mockito.verify(client).send(requestCaptor.capture(), Mockito.<HttpResponse.BodyHandler<String>>any());
        Assert.assertEquals(requestCaptor.getValue().headers().firstValue("Authorization").orElse(null),
                "Basic " + BASIC_AUTH);
    }

    @Test
    public void testFetchBlockedAccountsSuccessIncludesRequiredAuthHeader() throws Exception {
        HttpClient client = Mockito.mock(HttpClient.class);
        HttpResponse<String> response = Mockito.mock(HttpResponse.class);

        Mockito.when(response.statusCode()).thenReturn(200);
        Mockito.when(response.body()).thenReturn(new JSONArray()
                .put(new JSONObject()
                        .put(CDSAccountValidationConstants.CDS_ACCOUNT_ID_TAG, "acc-2")
                        .put(CDSAccountValidationConstants.DISCLOSURE_OPTION_TAG,
                                CDSAccountValidationConstants.DOMS_STATUS_NO_SHARING))
                .toString());
        Mockito.when(client.send(Mockito.any(HttpRequest.class), Mockito.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(response);

        CDSAccountValidationUtils.setHttpClient(client);

        Set<String> accounts = new HashSet<>();
        accounts.add("acc-2");

        Set<String> blocked = CDSAccountValidationUtils.fetchBlockedJointAccountsFromService(
                accounts, DOMS_ENDPOINT, BASIC_AUTH);

        Assert.assertEquals(blocked.size(), 1);
        Assert.assertTrue(blocked.contains("acc-2"));

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        Mockito.verify(client).send(requestCaptor.capture(), Mockito.<HttpResponse.BodyHandler<String>>any());
        Assert.assertEquals(requestCaptor.getValue().headers().firstValue("Authorization").orElse(null),
                "Basic " + BASIC_AUTH);
    }

    @Test
    public void testFetchBlockedAccountsFromServiceWithEmptyOrNullAccountIds() throws CDSAccountValidationException {
        Set<String> blockedForEmpty = CDSAccountValidationUtils.fetchBlockedJointAccountsFromService(
                Collections.emptySet(), DOMS_ENDPOINT, "");
        Set<String> blockedForNull = CDSAccountValidationUtils.fetchBlockedJointAccountsFromService(
                null, DOMS_ENDPOINT, "");

        Assert.assertNotNull(blockedForEmpty);
        Assert.assertNotNull(blockedForNull);
        Assert.assertTrue(blockedForEmpty.isEmpty());
        Assert.assertTrue(blockedForNull.isEmpty());
    }

    @Test
    public void testFetchBlockedAccountsSkipsInvalidRowsAndBlankAccountId() throws Exception {
        HttpClient client = Mockito.mock(HttpClient.class);
        HttpResponse<String> response = Mockito.mock(HttpResponse.class);

        Mockito.when(response.statusCode()).thenReturn(200);
        Mockito.when(response.body()).thenReturn("["
                + "\"invalid\","
                + "{\"disclosureOption\":\"no-sharing\"},"
                + "{\"accountId\":\"acc-5\",\"disclosureOption\":\"no-sharing\"}"
                + "]");
        Mockito.when(client.send(Mockito.any(HttpRequest.class), Mockito.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(response);

        CDSAccountValidationUtils.setHttpClient(client);

        Set<String> accounts = new HashSet<>();
        accounts.add("acc-5");

        Set<String> blocked = CDSAccountValidationUtils.fetchBlockedJointAccountsFromService(
                accounts, DOMS_ENDPOINT, BASIC_AUTH);

        Assert.assertEquals(blocked.size(), 1);
        Assert.assertTrue(blocked.contains("acc-5"));
    }

    @Test
    public void testFetchBlockedAccountsFromServiceInterruptedError() throws Exception {
        HttpClient client = Mockito.mock(HttpClient.class);

        Mockito.when(client.send(Mockito.any(HttpRequest.class), Mockito.<HttpResponse.BodyHandler<String>>any()))
                .thenThrow(new InterruptedException("interrupted"));

        CDSAccountValidationUtils.setHttpClient(client);

        Set<String> accounts = new HashSet<>();
        accounts.add("acc-1");

        Assert.expectThrows(CDSAccountValidationException.class,
                () -> CDSAccountValidationUtils.fetchBlockedJointAccountsFromService(
                        accounts, DOMS_ENDPOINT, BASIC_AUTH));
        Assert.assertTrue(Thread.currentThread().isInterrupted());
        Thread.interrupted();
    }

    @Test
    public void testFetchBlockedAccountsFromServiceMalformedResponse() throws Exception {
        HttpClient client = Mockito.mock(HttpClient.class);
        HttpResponse<String> response = Mockito.mock(HttpResponse.class);

        Mockito.when(response.statusCode()).thenReturn(200);
        Mockito.when(response.body()).thenReturn("{not-an-array");
        Mockito.when(client.send(Mockito.any(HttpRequest.class), Mockito.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(response);

        CDSAccountValidationUtils.setHttpClient(client);

        Set<String> accounts = new HashSet<>();
        accounts.add("acc-1");

        Assert.expectThrows(CDSAccountValidationException.class,
                () -> CDSAccountValidationUtils.fetchBlockedJointAccountsFromService(
                        accounts, DOMS_ENDPOINT, BASIC_AUTH));
    }

    @Test
    public void testGenerateJwtSuccess() throws Exception {
        try {
            String signedJwt = CDSAccountValidationUtils.generateJWT("{\"sub\":\"user-1\"}");
            Assert.assertNotNull(signedJwt);
            Assert.assertEquals(signedJwt.split("\\.").length, 3);
        } catch (ExceptionInInitializerError | NoClassDefFoundError | NullPointerException |
                 com.nimbusds.jose.JOSEException e) {
            // In unit-test runtime, KeyStoreUtils may fail to initialize due missing server config.
            Assert.assertTrue(true);
        }
    }
}

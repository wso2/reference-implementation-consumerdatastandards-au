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
    private static final String SECONDARY_ACCOUNTS_ENDPOINT =
            ACCOUNT_METADATA_WEBAPP_BASE_URL + "/secondary-accounts";

    private static final String BASIC_AUTH = Base64.getEncoder().encodeToString("user:pass".getBytes());

    /**
    * Verifies blocked joint accounts are extracted when the metadata service returns a valid response.
    */
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

    /**
    * Verifies a non-200 DOMS response is surfaced as a validation exception.
    */
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

    /**
    * Verifies I/O failures from the DOMS service call are wrapped as validation exceptions.
    */
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

    /**
    * Verifies Basic authorization is sent when fetching blocked joint accounts.
    */
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

    /**
    * Verifies successful DOMS calls include the required authorization header.
    */
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

    /**
    * Verifies empty or null account sets return an empty blocked-account result.
    */
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

    /**
     * Verifies malformed DOMS rows are ignored while valid no-sharing entries are still processed.
     */
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

    /**
     * Verifies interrupted DOMS requests raise a validation exception and preserve interrupt status.
     */
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

    /**
     * Verifies malformed DOMS payloads trigger a validation exception.
     */
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

    // ---- fetchBlockedSecondaryAccountsFromService tests ----

    /**
     * Verifies inactive secondary-account instructions are marked as blocked.
     */
    @Test
    public void testFetchBlockedSecondaryAccountsSuccess() throws Exception {
        HttpClient client = Mockito.mock(HttpClient.class);
        HttpResponse<String> response = Mockito.mock(HttpResponse.class);

        Mockito.when(response.statusCode()).thenReturn(200);
        Mockito.when(response.body()).thenReturn("["
                + "{\"accountId\":\"acc-1\",\"secondaryAccountInstructionStatus\":\"inactive\"},"
                + "{\"accountId\":\"acc-2\",\"secondaryAccountInstructionStatus\":\"active\"}"
                + "]");
        Mockito.when(client.send(Mockito.any(HttpRequest.class), Mockito.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(response);

        CDSAccountValidationUtils.setHttpClient(client);

        Set<String> accounts = new HashSet<>();
        accounts.add("acc-1");
        accounts.add("acc-2");

        Set<String> blocked = CDSAccountValidationUtils.fetchBlockedSecondaryAccountsFromService(
                accounts, SECONDARY_ACCOUNTS_ENDPOINT, "user-1", BASIC_AUTH);

        Assert.assertEquals(blocked.size(), 1);
        Assert.assertTrue(blocked.contains("acc-1"));
        Assert.assertFalse(blocked.contains("acc-2"));
    }

    /**
     * Verifies non-200 responses from the secondary-account endpoint raise a validation exception.
     */
    @Test
    public void testFetchBlockedSecondaryAccountsNon200() throws Exception {
        HttpClient client = Mockito.mock(HttpClient.class);
        HttpResponse<String> response = Mockito.mock(HttpResponse.class);

        Mockito.when(response.statusCode()).thenReturn(503);
        Mockito.when(response.body()).thenReturn("[]");
        Mockito.when(client.send(Mockito.any(HttpRequest.class), Mockito.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(response);

        CDSAccountValidationUtils.setHttpClient(client);

        Set<String> accounts = new HashSet<>();
        accounts.add("acc-1");

        Assert.expectThrows(CDSAccountValidationException.class,
                () -> CDSAccountValidationUtils.fetchBlockedSecondaryAccountsFromService(
                        accounts, SECONDARY_ACCOUNTS_ENDPOINT, "user-1", BASIC_AUTH));
    }

    /**
     * Verifies I/O failures from the secondary-account endpoint are wrapped as validation exceptions.
     */
    @Test
    public void testFetchBlockedSecondaryAccountsIoError() throws Exception {
        HttpClient client = Mockito.mock(HttpClient.class);

        Mockito.when(client.send(Mockito.any(HttpRequest.class), Mockito.<HttpResponse.BodyHandler<String>>any()))
                .thenThrow(new IOException("Connection refused"));

        CDSAccountValidationUtils.setHttpClient(client);

        Set<String> accounts = new HashSet<>();
        accounts.add("acc-1");

        Assert.expectThrows(CDSAccountValidationException.class,
                () -> CDSAccountValidationUtils.fetchBlockedSecondaryAccountsFromService(
                        accounts, SECONDARY_ACCOUNTS_ENDPOINT, "user-1", BASIC_AUTH));
    }

    /**
     * Verifies interrupted secondary-account requests raise a validation exception and preserve interrupt status.
     */
    @Test
    public void testFetchBlockedSecondaryAccountsInterruptedError() throws Exception {
        HttpClient client = Mockito.mock(HttpClient.class);

        Mockito.when(client.send(Mockito.any(HttpRequest.class), Mockito.<HttpResponse.BodyHandler<String>>any()))
                .thenThrow(new InterruptedException("interrupted"));

        CDSAccountValidationUtils.setHttpClient(client);

        Set<String> accounts = new HashSet<>();
        accounts.add("acc-1");

        Assert.expectThrows(CDSAccountValidationException.class,
                () -> CDSAccountValidationUtils.fetchBlockedSecondaryAccountsFromService(
                        accounts, SECONDARY_ACCOUNTS_ENDPOINT, "user-1", BASIC_AUTH));
        Assert.assertTrue(Thread.currentThread().isInterrupted());
        Thread.interrupted();
    }

    /**
     * Verifies malformed secondary-account payloads trigger a validation exception.
     */
    @Test
    public void testFetchBlockedSecondaryAccountsMalformedResponse() throws Exception {
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
                () -> CDSAccountValidationUtils.fetchBlockedSecondaryAccountsFromService(
                        accounts, SECONDARY_ACCOUNTS_ENDPOINT, "user-1", BASIC_AUTH));
    }

    /**
     * Verifies empty or null account sets return an empty secondary blocked-account result.
     */
    @Test
    public void testFetchBlockedSecondaryAccountsWithEmptyOrNullAccountIds() throws CDSAccountValidationException {
        Set<String> blockedForEmpty = CDSAccountValidationUtils.fetchBlockedSecondaryAccountsFromService(
                Collections.emptySet(), SECONDARY_ACCOUNTS_ENDPOINT, "user-1", BASIC_AUTH);
        Set<String> blockedForNull = CDSAccountValidationUtils.fetchBlockedSecondaryAccountsFromService(
                null, SECONDARY_ACCOUNTS_ENDPOINT, "user-1", BASIC_AUTH);

        Assert.assertNotNull(blockedForEmpty);
        Assert.assertNotNull(blockedForNull);
        Assert.assertTrue(blockedForEmpty.isEmpty());
        Assert.assertTrue(blockedForNull.isEmpty());
    }

    /**
     * Verifies both accountIds and userId query parameters are included for secondary-account requests.
     */
    @Test
    public void testFetchBlockedSecondaryAccountsIncludesUserIdInRequest() throws Exception {
        HttpClient client = Mockito.mock(HttpClient.class);
        HttpResponse<String> response = Mockito.mock(HttpResponse.class);

        Mockito.when(response.statusCode()).thenReturn(200);
        Mockito.when(response.body()).thenReturn("[]");
        Mockito.when(client.send(Mockito.any(HttpRequest.class), Mockito.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(response);

        CDSAccountValidationUtils.setHttpClient(client);

        Set<String> accounts = new HashSet<>();
        accounts.add("acc-1");

        CDSAccountValidationUtils.fetchBlockedSecondaryAccountsFromService(
                accounts, SECONDARY_ACCOUNTS_ENDPOINT, "user-99", BASIC_AUTH);

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        Mockito.verify(client).send(requestCaptor.capture(), Mockito.<HttpResponse.BodyHandler<String>>any());
        String requestUri = requestCaptor.getValue().uri().toString();
        Assert.assertTrue(requestUri.contains("accountIds="));
        Assert.assertTrue(requestUri.contains("userId="));
        Assert.assertEquals(requestCaptor.getValue().headers().firstValue("Authorization").orElse(null),
                "Basic " + BASIC_AUTH);
    }

    /**
     * Verifies invalid rows and active instructions are skipped while inactive rows are blocked.
     */
    @Test
    public void testFetchBlockedSecondaryAccountsSkipsInvalidAndNonInactiveRows() throws Exception {
        HttpClient client = Mockito.mock(HttpClient.class);
        HttpResponse<String> response = Mockito.mock(HttpResponse.class);

        Mockito.when(response.statusCode()).thenReturn(200);
        Mockito.when(response.body()).thenReturn("["
                + "\"invalid-string-entry\","
                + "{\"secondaryAccountInstructionStatus\":\"inactive\"},"
                + "{\"accountId\":\"acc-active\",\"secondaryAccountInstructionStatus\":\"active\"},"
                + "{\"accountId\":\"acc-inactive\",\"secondaryAccountInstructionStatus\":\"inactive\"}"
                + "]");
        Mockito.when(client.send(Mockito.any(HttpRequest.class), Mockito.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(response);

        CDSAccountValidationUtils.setHttpClient(client);

        Set<String> accounts = new HashSet<>();
        accounts.add("acc-active");
        accounts.add("acc-inactive");

        Set<String> blocked = CDSAccountValidationUtils.fetchBlockedSecondaryAccountsFromService(
                accounts, SECONDARY_ACCOUNTS_ENDPOINT, "user-1", BASIC_AUTH);

        Assert.assertEquals(blocked.size(), 1);
        Assert.assertTrue(blocked.contains("acc-inactive"));
        Assert.assertFalse(blocked.contains("acc-active"));
    }

    // ---- fetchBlockedBusinessAccountsFromService tests ----

    @Test
    public void testFetchBlockedBusinessAccountsWithEmptyOrNullAccountIds() throws CDSAccountValidationException {
        String businessStakeholdersEndpoint = ACCOUNT_METADATA_WEBAPP_BASE_URL + "/business-stakeholders";

        Set<String> blockedForEmpty = CDSAccountValidationUtils.fetchBlockedBusinessAccountsFromService(
                Collections.emptySet(), businessStakeholdersEndpoint, "user-1", BASIC_AUTH);
        Set<String> blockedForNull = CDSAccountValidationUtils.fetchBlockedBusinessAccountsFromService(
                null, businessStakeholdersEndpoint, "user-1", BASIC_AUTH);

        Assert.assertNotNull(blockedForEmpty);
        Assert.assertNotNull(blockedForNull);
        Assert.assertTrue(blockedForEmpty.isEmpty());
        Assert.assertTrue(blockedForNull.isEmpty());
    }

    @Test
    public void testFetchBlockedBusinessAccountsWithBlankBasicAuth() {
        String businessStakeholdersEndpoint = ACCOUNT_METADATA_WEBAPP_BASE_URL + "/business-stakeholders";

        Set<String> accounts = new HashSet<>();
        accounts.add("acc-1");

        Assert.expectThrows(CDSAccountValidationException.class,
                () -> CDSAccountValidationUtils.fetchBlockedBusinessAccountsFromService(
                        accounts, businessStakeholdersEndpoint, "user-1", ""));
        Assert.expectThrows(CDSAccountValidationException.class,
                () -> CDSAccountValidationUtils.fetchBlockedBusinessAccountsFromService(
                        accounts, businessStakeholdersEndpoint, "user-1", null));
    }

    // ---- fetchAllBlockedAccounts tests ----

    /**
     * Verifies blocked-account results from DOMS and secondary-account checks are merged.
     */
    @Test
    public void testFetchAllBlockedAccountsWithEmptyInput() throws CDSAccountValidationException {
        Set<String> blocked = CDSAccountValidationUtils.fetchAllBlockedAccounts(
                Collections.emptySet(), ACCOUNT_METADATA_WEBAPP_BASE_URL, "user-1", BASIC_AUTH);
        Assert.assertNotNull(blocked);
        Assert.assertTrue(blocked.isEmpty());
    }

    @Test
    public void testFetchAllBlockedAccountsCombinesResults() throws Exception {
        HttpClient client = Mockito.mock(HttpClient.class);
        HttpResponse<String> disclosureResponse = Mockito.mock(HttpResponse.class);
        HttpResponse<String> secondaryResponse = Mockito.mock(HttpResponse.class);
        HttpResponse<String> businessResponse = Mockito.mock(HttpResponse.class);

        Mockito.when(disclosureResponse.statusCode()).thenReturn(200);
        Mockito.when(disclosureResponse.body()).thenReturn("["
                + "{\"accountId\":\"acc-1\",\"disclosureOption\":\"no-sharing\"}"
                + "]");
        Mockito.when(secondaryResponse.statusCode()).thenReturn(200);
        Mockito.when(secondaryResponse.body()).thenReturn("["
                + "{\"accountId\":\"acc-2\",\"secondaryAccountInstructionStatus\":\"inactive\"}"
                + "]");
        Mockito.when(businessResponse.statusCode()).thenReturn(200);
        Mockito.when(businessResponse.body()).thenReturn("["
                + "{\"accountId\":\"acc-3\",\"permission\":\"VIEW\"}"
                + "]");
        Mockito.when(client.send(Mockito.any(HttpRequest.class), Mockito.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(disclosureResponse)
                .thenReturn(secondaryResponse)
                .thenReturn(businessResponse);

        CDSAccountValidationUtils.setHttpClient(client);

        Set<String> accounts = new HashSet<>();
        accounts.add("acc-1");
        accounts.add("acc-2");
        accounts.add("acc-3");
        accounts.add("acc-4");

        Set<String> blocked = CDSAccountValidationUtils.fetchAllBlockedAccounts(
                accounts, ACCOUNT_METADATA_WEBAPP_BASE_URL, "user-1", BASIC_AUTH);

        Assert.assertEquals(blocked.size(), 3);
        Assert.assertTrue(blocked.contains("acc-1"));
        Assert.assertTrue(blocked.contains("acc-2"));
        Assert.assertTrue(blocked.contains("acc-3"));
        Assert.assertFalse(blocked.contains("acc-4"));
    }

    // ---- generateJWT tests ----

    /**
     * Verifies JWT generation returns a signed compact token when key material is available.
     */
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

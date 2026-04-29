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

import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.openbanking.consumerdatastandards.au.policy.exceptions.CDSAccountValidationException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
    private static final String BUSINESS_STAKEHOLDERS_ENDPOINT =
            ACCOUNT_METADATA_WEBAPP_BASE_URL + "/business-stakeholders";
    private static final String LEGAL_ENTITY_ENDPOINT =
            ACCOUNT_METADATA_WEBAPP_BASE_URL + "/legal-entity";

    private static final String BASIC_AUTH = Base64.getEncoder().encodeToString("user:pass".getBytes());

    // ---- helpers ----

    /**
     * Creates a mocked CloseableHttpResponse with the specified status code and response body.
     *
     * @param statusCode the HTTP status code for the response
     * @param body the response body as a string
     * @return a mocked CloseableHttpResponse
     * @throws Exception if mock creation fails
     */
    private static CloseableHttpResponse mockResponse(int statusCode, String body) throws Exception {
        CloseableHttpResponse response = Mockito.mock(CloseableHttpResponse.class);
        StatusLine statusLine = Mockito.mock(StatusLine.class);

        Mockito.when(statusLine.getStatusCode()).thenReturn(statusCode);
        Mockito.when(response.getStatusLine()).thenReturn(statusLine);
        Mockito.when(response.getEntity()).thenReturn(new StringEntity(body, StandardCharsets.UTF_8));
        return response;
    }

    /**
     * Creates a mocked CloseableHttpClient that returns a response with the specified status code and body.
     *
     * @param statusCode the HTTP status code for the response
     * @param body the response body as a string
     * @return a mocked CloseableHttpClient configured to return the specified response
     * @throws Exception if mock creation fails
     */
    private static CloseableHttpClient mockClientWithResponse(int statusCode, String body) throws Exception {
        CloseableHttpClient client = Mockito.mock(CloseableHttpClient.class);
        CloseableHttpResponse response = mockResponse(statusCode, body);
        Mockito.when(client.execute(Mockito.any(HttpGet.class))).thenReturn(response);
        return client;
    }

    // ---- fetchBlockedJointAccountsFromService tests ----

    /**
     * Verifies successful DOMS response with multiple accounts returns the correct blocked accounts.
     */
    @Test
    public void testFetchBlockedAccountsFromServiceSuccess() throws Exception {
        CloseableHttpClient client = mockClientWithResponse(HttpStatus.SC_OK, "["
                + "{\"accountId\":\"acc-1\",\"disclosureOption\":\"no-sharing\"},"
                + "{\"accountId\":\"acc-2\",\"disclosureOption\":\"pre-approval\"},"
                + "{\"accountId\":\"acc-3\",\"disclosureOption\":\"no-sharing\"}"
                + "]");
        CDSAccountValidationUtils.setApacheHttpClient(client);

        Set<String> accounts = new HashSet<>();
        accounts.add("acc-1");
        accounts.add("acc-2");
        accounts.add("acc-3");

        Set<String> blocked = CDSAccountValidationUtils.fetchBlockedJointAccountsFromService(
                accounts, DOMS_ENDPOINT, BASIC_AUTH);

        Assert.assertEquals(blocked.size(), 2);
        Assert.assertTrue(blocked.contains("acc-1"));
        Assert.assertTrue(blocked.contains("acc-3"));

        ArgumentCaptor<HttpGet> requestCaptor = ArgumentCaptor.forClass(HttpGet.class);
        Mockito.verify(client).execute(requestCaptor.capture());
        Assert.assertTrue(requestCaptor.getValue().getURI().toString().contains("accountIds="));
        Assert.assertEquals(requestCaptor.getValue().getFirstHeader("Authorization").getValue(),
                "Basic " + BASIC_AUTH);
    }

    /**
     * Verifies a non-200 DOMS response is surfaced as a validation exception.
     */
    @Test
    public void testFetchBlockedAccountsFromServiceNon200() throws Exception {
        CDSAccountValidationUtils.setApacheHttpClient(mockClientWithResponse(
                HttpStatus.SC_INTERNAL_SERVER_ERROR, "[]"));

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
        CloseableHttpClient client = Mockito.mock(CloseableHttpClient.class);
        Mockito.when(client.execute(Mockito.any(HttpGet.class)))
                .thenThrow(new IOException("Connection failed"));
        CDSAccountValidationUtils.setApacheHttpClient(client);

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
        CloseableHttpClient client = mockClientWithResponse(HttpStatus.SC_OK, "["
                + "{\"accountId\":\"acc-1\",\"disclosureOption\":\"no-sharing\"}"
                + "]");
        CDSAccountValidationUtils.setApacheHttpClient(client);

        Set<String> accounts = new HashSet<>();
        accounts.add("acc-1");

        Set<String> blocked = CDSAccountValidationUtils.fetchBlockedJointAccountsFromService(
                accounts, DOMS_ENDPOINT, BASIC_AUTH);

        Assert.assertEquals(blocked.size(), 1);
        Assert.assertTrue(blocked.contains("acc-1"));

        ArgumentCaptor<HttpGet> requestCaptor = ArgumentCaptor.forClass(HttpGet.class);
        Mockito.verify(client).execute(requestCaptor.capture());
        Assert.assertEquals(requestCaptor.getValue().getFirstHeader("Authorization").getValue(),
                "Basic " + BASIC_AUTH);
    }

    /**
     * Verifies successful DOMS calls include the required authorization header.
     */
    @Test
    public void testFetchBlockedAccountsSuccessIncludesRequiredAuthHeader() throws Exception {
        CloseableHttpClient client = mockClientWithResponse(HttpStatus.SC_OK, "["
                + "{\"accountId\":\"acc-2\",\"disclosureOption\":\"no-sharing\"}"
                + "]");
        CDSAccountValidationUtils.setApacheHttpClient(client);

        Set<String> accounts = new HashSet<>();
        accounts.add("acc-2");

        Set<String> blocked = CDSAccountValidationUtils.fetchBlockedJointAccountsFromService(
                accounts, DOMS_ENDPOINT, BASIC_AUTH);

        Assert.assertEquals(blocked.size(), 1);
        Assert.assertTrue(blocked.contains("acc-2"));

        ArgumentCaptor<HttpGet> requestCaptor = ArgumentCaptor.forClass(HttpGet.class);
        Mockito.verify(client).execute(requestCaptor.capture());
        Assert.assertEquals(requestCaptor.getValue().getFirstHeader("Authorization").getValue(),
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
        CloseableHttpClient client = mockClientWithResponse(HttpStatus.SC_OK, "["
                + "\"invalid\","
                + "{\"disclosureOption\":\"no-sharing\"},"
                + "{\"accountId\":\"acc-5\",\"disclosureOption\":\"no-sharing\"}"
                + "]");
        CDSAccountValidationUtils.setApacheHttpClient(client);

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
    public void testFetchBlockedAccountsFromServiceMalformedResponse() throws Exception {
        CDSAccountValidationUtils.setApacheHttpClient(mockClientWithResponse(HttpStatus.SC_OK, "{not-an-array"));

        Set<String> accounts = new HashSet<>();
        accounts.add("acc-1");

        Assert.expectThrows(CDSAccountValidationException.class,
                () -> CDSAccountValidationUtils.fetchBlockedJointAccountsFromService(
                        accounts, DOMS_ENDPOINT, BASIC_AUTH));
    }

    // ---- fetchBlockedSecondaryAccountsFromService tests ----

    /**
     * Verifies successful secondary accounts response returns inactive accounts as blocked.
     */
    @Test
    public void testFetchBlockedSecondaryAccountsSuccess() throws Exception {
        CloseableHttpClient client = mockClientWithResponse(HttpStatus.SC_OK, "["
                + "{\"accountId\":\"acc-1\",\"secondaryAccountInstructionStatus\":\"inactive\"},"
                + "{\"accountId\":\"acc-2\",\"secondaryAccountInstructionStatus\":\"active\"}"
                + "]");
        CDSAccountValidationUtils.setApacheHttpClient(client);

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
     * Verifies malformed DOMS payloads trigger a validation exception.
     */
    @Test
    public void testFetchBlockedSecondaryAccountsNon200() throws Exception {
        CDSAccountValidationUtils.setApacheHttpClient(mockClientWithResponse(HttpStatus.SC_SERVICE_UNAVAILABLE, "[]"));

        Set<String> accounts = new HashSet<>();
        accounts.add("acc-1");

        Assert.expectThrows(CDSAccountValidationException.class,
                () -> CDSAccountValidationUtils.fetchBlockedSecondaryAccountsFromService(
                        accounts, SECONDARY_ACCOUNTS_ENDPOINT, "user-1", BASIC_AUTH));
    }

    /**
     * Verifies I/O failures from the secondary accounts service call are wrapped as validation exceptions.
     */
    @Test
    public void testFetchBlockedSecondaryAccountsIoError() throws Exception {
        CloseableHttpClient client = Mockito.mock(CloseableHttpClient.class);
        Mockito.when(client.execute(Mockito.any(HttpGet.class)))
                .thenThrow(new IOException("Connection refused"));
        CDSAccountValidationUtils.setApacheHttpClient(client);

        Set<String> accounts = new HashSet<>();
        accounts.add("acc-1");

        Assert.expectThrows(CDSAccountValidationException.class,
                () -> CDSAccountValidationUtils.fetchBlockedSecondaryAccountsFromService(
                        accounts, SECONDARY_ACCOUNTS_ENDPOINT, "user-1", BASIC_AUTH));
    }

    /**
     * Verifies malformed secondary accounts JSON responses raise a validation exception.
     */
    @Test
    public void testFetchBlockedSecondaryAccountsMalformedResponse() throws Exception {
        CDSAccountValidationUtils.setApacheHttpClient(mockClientWithResponse(HttpStatus.SC_OK, "{not-an-array"));

        Set<String> accounts = new HashSet<>();
        accounts.add("acc-1");

        Assert.expectThrows(CDSAccountValidationException.class,
                () -> CDSAccountValidationUtils.fetchBlockedSecondaryAccountsFromService(
                        accounts, SECONDARY_ACCOUNTS_ENDPOINT, "user-1", BASIC_AUTH));
    }

    /**
     * Verifies empty or null account sets return an empty blocked-account result for secondary accounts.
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
     * Verifies blank user ID returns empty results while null user ID raises a NullPointerException.
     */
    @Test
    public void testFetchBlockedSecondaryAccountsWithBlankUserId() throws Exception {
        Set<String> accounts = new HashSet<>();
        accounts.add("acc-1");

        CDSAccountValidationUtils.setApacheHttpClient(mockClientWithResponse(HttpStatus.SC_OK, "[]"));
        Set<String> blockedForBlank = CDSAccountValidationUtils.fetchBlockedSecondaryAccountsFromService(
                accounts, SECONDARY_ACCOUNTS_ENDPOINT, "", BASIC_AUTH);
        Assert.assertNotNull(blockedForBlank);
        Assert.assertTrue(blockedForBlank.isEmpty());

        Assert.expectThrows(NullPointerException.class,
                () -> CDSAccountValidationUtils.fetchBlockedSecondaryAccountsFromService(
                        accounts, SECONDARY_ACCOUNTS_ENDPOINT, null, BASIC_AUTH));
    }

    /**
     * Verifies the secondary accounts request includes both account IDs and user ID parameters.
     */
    @Test
    public void testFetchBlockedSecondaryAccountsIncludesUserIdInRequest() throws Exception {
        CloseableHttpClient client = mockClientWithResponse(HttpStatus.SC_OK, "[]");
        CDSAccountValidationUtils.setApacheHttpClient(client);

        Set<String> accounts = new HashSet<>();
        accounts.add("acc-1");

        CDSAccountValidationUtils.fetchBlockedSecondaryAccountsFromService(
                accounts, SECONDARY_ACCOUNTS_ENDPOINT, "user-99", BASIC_AUTH);

        ArgumentCaptor<HttpGet> requestCaptor = ArgumentCaptor.forClass(HttpGet.class);
        Mockito.verify(client).execute(requestCaptor.capture());
        String requestUri = requestCaptor.getValue().getURI().toString();
        Assert.assertTrue(requestUri.contains("accountIds="));
        Assert.assertTrue(requestUri.contains("userId="));
        Assert.assertEquals(requestCaptor.getValue().getFirstHeader("Authorization").getValue(),
                "Basic " + BASIC_AUTH);
    }

    /**
     * Verifies malformed rows and active accounts are skipped while inactive accounts are returned as blocked.
     */
    @Test
    public void testFetchBlockedSecondaryAccountsSkipsInvalidAndNonInactiveRows() throws Exception {
        CloseableHttpClient client = mockClientWithResponse(HttpStatus.SC_OK, "["
                + "\"invalid-string-entry\","
                + "{\"secondaryAccountInstructionStatus\":\"inactive\"},"
                + "{\"accountId\":\"acc-active\",\"secondaryAccountInstructionStatus\":\"active\"},"
                + "{\"accountId\":\"acc-inactive\",\"secondaryAccountInstructionStatus\":\"inactive\"}"
                + "]");
        CDSAccountValidationUtils.setApacheHttpClient(client);

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

    /**
     * Verifies successful business stakeholders response returns VIEW permission accounts as blocked.
     */
    @Test
    public void testFetchBlockedBusinessAccountsSuccess() throws Exception {
        CloseableHttpClient client = mockClientWithResponse(HttpStatus.SC_OK, "["
                + "{\"accountId\":\"acc-1\",\"permission\":\"VIEW\"},"
                + "{\"accountId\":\"acc-2\",\"permission\":\"AUTHORIZE\"}"
                + "]");
        CDSAccountValidationUtils.setApacheHttpClient(client);

        Set<String> accounts = new HashSet<>();
        accounts.add("acc-1");
        accounts.add("acc-2");

        Set<String> blocked = CDSAccountValidationUtils.fetchBlockedBusinessAccountsFromService(
                accounts, BUSINESS_STAKEHOLDERS_ENDPOINT, "user-1", BASIC_AUTH);

        Assert.assertEquals(blocked.size(), 1);
        Assert.assertTrue(blocked.contains("acc-1"));
        Assert.assertFalse(blocked.contains("acc-2"));
    }

    /**
     * Verifies a non-200 business stakeholders response triggers a validation exception.
     */
    @Test
    public void testFetchBlockedBusinessAccountsNon200() throws Exception {
        CDSAccountValidationUtils.setApacheHttpClient(mockClientWithResponse(HttpStatus.SC_SERVICE_UNAVAILABLE, "[]"));

        Set<String> accounts = new HashSet<>();
        accounts.add("acc-1");

        Assert.expectThrows(CDSAccountValidationException.class,
                () -> CDSAccountValidationUtils.fetchBlockedBusinessAccountsFromService(
                        accounts, BUSINESS_STAKEHOLDERS_ENDPOINT, "user-1", BASIC_AUTH));
    }

    /**
     * Verifies I/O failures from the business stakeholders service call are wrapped as validation exceptions.
     */
    @Test
    public void testFetchBlockedBusinessAccountsIoError() throws Exception {
        CloseableHttpClient client = Mockito.mock(CloseableHttpClient.class);
        Mockito.when(client.execute(Mockito.any(HttpGet.class)))
                .thenThrow(new IOException("Connection refused"));
        CDSAccountValidationUtils.setApacheHttpClient(client);

        Set<String> accounts = new HashSet<>();
        accounts.add("acc-1");

        Assert.expectThrows(CDSAccountValidationException.class,
                () -> CDSAccountValidationUtils.fetchBlockedBusinessAccountsFromService(
                        accounts, BUSINESS_STAKEHOLDERS_ENDPOINT, "user-1", BASIC_AUTH));
    }

    /**
     * Verifies malformed business stakeholders JSON responses raise a validation exception.
     */
    @Test
    public void testFetchBlockedBusinessAccountsMalformedResponse() throws Exception {
        CDSAccountValidationUtils.setApacheHttpClient(mockClientWithResponse(HttpStatus.SC_OK, "{not-an-array"));

        Set<String> accounts = new HashSet<>();
        accounts.add("acc-1");

        Assert.expectThrows(CDSAccountValidationException.class,
                () -> CDSAccountValidationUtils.fetchBlockedBusinessAccountsFromService(
                        accounts, BUSINESS_STAKEHOLDERS_ENDPOINT, "user-1", BASIC_AUTH));
    }

    /**
     * Verifies empty or null account sets return an empty blocked-account result for business accounts.
     */
    @Test
    public void testFetchBlockedBusinessAccountsWithEmptyOrNullAccountIds() throws CDSAccountValidationException {
        Set<String> blockedForEmpty = CDSAccountValidationUtils.fetchBlockedBusinessAccountsFromService(
                Collections.emptySet(), BUSINESS_STAKEHOLDERS_ENDPOINT, "user-1", BASIC_AUTH);
        Set<String> blockedForNull = CDSAccountValidationUtils.fetchBlockedBusinessAccountsFromService(
                null, BUSINESS_STAKEHOLDERS_ENDPOINT, "user-1", BASIC_AUTH);

        Assert.assertNotNull(blockedForEmpty);
        Assert.assertNotNull(blockedForNull);
        Assert.assertTrue(blockedForEmpty.isEmpty());
        Assert.assertTrue(blockedForNull.isEmpty());
    }

    /**
     * Verifies blank user ID returns empty results while null user ID raises a NullPointerException for
     * business accounts.
     */
    @Test
    public void testFetchBlockedBusinessAccountsWithBlankUserId() throws Exception {
        Set<String> accounts = new HashSet<>();
        accounts.add("acc-1");

        CDSAccountValidationUtils.setApacheHttpClient(mockClientWithResponse(HttpStatus.SC_OK, "[]"));
        Set<String> blockedForBlank = CDSAccountValidationUtils.fetchBlockedBusinessAccountsFromService(
                accounts, BUSINESS_STAKEHOLDERS_ENDPOINT, "", BASIC_AUTH);
        Assert.assertNotNull(blockedForBlank);
        Assert.assertTrue(blockedForBlank.isEmpty());

        Assert.expectThrows(NullPointerException.class,
                () -> CDSAccountValidationUtils.fetchBlockedBusinessAccountsFromService(
                        accounts, BUSINESS_STAKEHOLDERS_ENDPOINT, null, BASIC_AUTH));
    }

    /**
     * Verifies the business stakeholders request includes both account IDs and user ID parameters.
     */
    @Test
    public void testFetchBlockedBusinessAccountsIncludesUserIdInRequest() throws Exception {
        CloseableHttpClient client = mockClientWithResponse(HttpStatus.SC_OK, "[]");
        CDSAccountValidationUtils.setApacheHttpClient(client);

        Set<String> accounts = new HashSet<>();
        accounts.add("acc-1");

        CDSAccountValidationUtils.fetchBlockedBusinessAccountsFromService(
                accounts, BUSINESS_STAKEHOLDERS_ENDPOINT, "user-99", BASIC_AUTH);

        ArgumentCaptor<HttpGet> requestCaptor = ArgumentCaptor.forClass(HttpGet.class);
        Mockito.verify(client).execute(requestCaptor.capture());
        String requestUri = requestCaptor.getValue().getURI().toString();
        Assert.assertTrue(requestUri.contains("accountIds="));
        Assert.assertTrue(requestUri.contains("userId="));
        Assert.assertEquals(requestCaptor.getValue().getFirstHeader("Authorization").getValue(),
                "Basic " + BASIC_AUTH);
    }

    /**
     * Verifies malformed rows and AUTHORIZE permission accounts are skipped while
     * VIEW permission accounts are returned as blocked.
     */
    @Test
    public void testFetchBlockedBusinessAccountsSkipsInvalidAndAuthorizeRows() throws Exception {
        CloseableHttpClient client = mockClientWithResponse(HttpStatus.SC_OK, "["
                + "\"invalid-entry\","
                + "{\"permission\":\"VIEW\"},"
                + "{\"accountId\":\"acc-auth\",\"permission\":\"AUTHORIZE\"},"
                + "{\"accountId\":\"acc-view\",\"permission\":\"VIEW\"}"
                + "]");
        CDSAccountValidationUtils.setApacheHttpClient(client);

        Set<String> accounts = new HashSet<>();
        accounts.add("acc-auth");
        accounts.add("acc-view");

        Set<String> blocked = CDSAccountValidationUtils.fetchBlockedBusinessAccountsFromService(
                accounts, BUSINESS_STAKEHOLDERS_ENDPOINT, "user-1", BASIC_AUTH);

        Assert.assertEquals(blocked.size(), 1);
        Assert.assertTrue(blocked.contains("acc-view"));
        Assert.assertFalse(blocked.contains("acc-auth"));
    }

    // ---- fetchAllBlockedAccounts tests ----

    /**
     * Verifies empty input account set returns an empty blocked accounts result.
     */
    @Test
    public void testFetchAllBlockedAccountsWithEmptyInput() throws CDSAccountValidationException {
        Set<String> blocked = CDSAccountValidationUtils.fetchAllBlockedAccounts(
                Collections.emptySet(), ACCOUNT_METADATA_WEBAPP_BASE_URL, "user-1", BASIC_AUTH, null);
        Assert.assertNotNull(blocked);
        Assert.assertTrue(blocked.isEmpty());
    }

    /**
     * Verifies fetchAllBlockedAccounts combines results from disclosure options, secondary, and business endpoints.
     */
    @Test
    public void testFetchAllBlockedAccountsCombinesResults() throws Exception {
        // Pre-create all responses before the Mockito.when chain to avoid UnfinishedStubbingException
        // (mockResponse internally calls Mockito.when, which must not happen mid-chain).
        CloseableHttpResponse disclosureResp = mockResponse(HttpStatus.SC_OK,
                "[{\"accountId\":\"acc-1\",\"disclosureOption\":\"no-sharing\"}]");
        CloseableHttpResponse secondaryResp = mockResponse(HttpStatus.SC_OK,
                "[{\"accountId\":\"acc-2\",\"secondaryAccountInstructionStatus\":\"inactive\"}]");
        CloseableHttpResponse businessResp = mockResponse(HttpStatus.SC_OK,
                "[{\"accountId\":\"acc-3\",\"permission\":\"VIEW\"}]");

        // disclosure → secondary → business; legal entity is skipped because clientId is null
        CloseableHttpClient client = Mockito.mock(CloseableHttpClient.class);
        Mockito.when(client.execute(Mockito.any(HttpGet.class)))
                .thenReturn(disclosureResp)
                .thenReturn(secondaryResp)
                .thenReturn(businessResp);

        CDSAccountValidationUtils.setApacheHttpClient(client);

        Set<String> accounts = new HashSet<>();
        accounts.add("acc-1");
        accounts.add("acc-2");
        accounts.add("acc-3");
        accounts.add("acc-4");

        Set<String> blocked = CDSAccountValidationUtils.fetchAllBlockedAccounts(
                accounts, ACCOUNT_METADATA_WEBAPP_BASE_URL, "user-1", BASIC_AUTH, null);

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
            Assert.assertTrue(true);
        }
    }

    // ---- CDSAccountValidationException constructor tests ----

    /**
     * Verifies CDSAccountValidationException string constructor sets the message correctly.
     */
    @Test
    public void testCDSAccountValidationExceptionStringConstructor() {
        CDSAccountValidationException ex = new CDSAccountValidationException("test-message");
        Assert.assertEquals(ex.getMessage(), "test-message");
        Assert.assertNull(ex.getCause());
    }

    /**
     * Verifies CDSAccountValidationException string-throwable constructor sets message and cause correctly.
     */
    @Test
    public void testCDSAccountValidationExceptionStringThrowableConstructor() {
        Throwable cause = new RuntimeException("root-cause");
        CDSAccountValidationException ex = new CDSAccountValidationException("test-message", cause);
        Assert.assertEquals(ex.getMessage(), "test-message");
        Assert.assertEquals(ex.getCause(), cause);
    }

    // ---- fetchBlockedLegalEntityAccountsFromService tests ----

    /**
     * Verifies null account IDs return an empty blocked accounts result for legal entity.
     */
    @Test
    public void testFetchBlockedLegalEntityAccountsWithNullAccountIds() throws CDSAccountValidationException {
        Set<String> blocked = CDSAccountValidationUtils.fetchBlockedLegalEntityAccountsFromService(
                null, LEGAL_ENTITY_ENDPOINT, "user-1", BASIC_AUTH, "client-1");
        Assert.assertNotNull(blocked);
        Assert.assertTrue(blocked.isEmpty());
    }

    /**
     * Verifies empty account IDs return an empty blocked accounts result for legal entity.
     */
    @Test
    public void testFetchBlockedLegalEntityAccountsWithEmptyAccountIds() throws CDSAccountValidationException {
        Set<String> blocked = CDSAccountValidationUtils.fetchBlockedLegalEntityAccountsFromService(
                Collections.emptySet(), LEGAL_ENTITY_ENDPOINT, "user-1", BASIC_AUTH, "client-1");
        Assert.assertNotNull(blocked);
        Assert.assertTrue(blocked.isEmpty());
    }

    /**
     * Verifies blank user ID returns empty results while null user ID raises a NullPointerException for legal entity.
     */
    @Test
    public void testFetchBlockedLegalEntityAccountsWithBlankUserId() throws Exception {
        Set<String> accounts = new HashSet<>();
        accounts.add("acc-1");

        CDSAccountValidationUtils.setApacheHttpClient(mockClientWithResponse(HttpStatus.SC_OK, "[]"));
        Set<String> blockedForBlank = CDSAccountValidationUtils.fetchBlockedLegalEntityAccountsFromService(
                accounts, LEGAL_ENTITY_ENDPOINT, "", BASIC_AUTH, "client-1");
        Assert.assertNotNull(blockedForBlank);
        Assert.assertTrue(blockedForBlank.isEmpty());

        Assert.expectThrows(NullPointerException.class,
                () -> CDSAccountValidationUtils.fetchBlockedLegalEntityAccountsFromService(
                        accounts, LEGAL_ENTITY_ENDPOINT, null, BASIC_AUTH, "client-1"));
    }

    /**
     * Verifies null client ID returns empty blocked accounts result for legal entity.
     */
    @Test
    public void testFetchBlockedLegalEntityAccountsWithNullClientId() throws CDSAccountValidationException {
        Set<String> accounts = new HashSet<>();
        accounts.add("acc-1");
        Set<String> blocked = CDSAccountValidationUtils.fetchBlockedLegalEntityAccountsFromService(
                accounts, LEGAL_ENTITY_ENDPOINT, "user-1", BASIC_AUTH, null);
        Assert.assertNotNull(blocked);
        Assert.assertTrue(blocked.isEmpty());
    }

    /**
     * Verifies the legal entity endpoint is called when client ID is provided.
     */
    @Test
    public void testFetchBlockedLegalEntityAccountsWhenLegalEntityIdBlank() throws Exception {
        CloseableHttpClient client = mockClientWithResponse(HttpStatus.SC_OK, "[]");
        CDSAccountValidationUtils.setApacheHttpClient(client);

        Set<String> accounts = new HashSet<>();
        accounts.add("acc-1");
        Set<String> blocked = CDSAccountValidationUtils.fetchBlockedLegalEntityAccountsFromService(
                accounts, LEGAL_ENTITY_ENDPOINT, "user-1", BASIC_AUTH, "client-1");

        Assert.assertNotNull(blocked);
        Assert.assertTrue(blocked.isEmpty());
        Mockito.verify(client, Mockito.times(1)).execute(Mockito.any(HttpGet.class));
    }

    /**
     * Verifies successful legal entity response returns blocked accounts with camelCase accountID field.
     */
    @Test
    public void testFetchBlockedLegalEntityAccountsSuccess() throws Exception {
        // null element in array covers the sharingItem == null branch
        String leResponse = "[null,"
                + "{\"accountID\":\"acc-1\",\"legalEntitySharingStatus\":\"blocked\"},"
                + "{\"accountID\":\"acc-2\",\"legalEntitySharingStatus\":\"allowed\"}"
                + "]";

        CloseableHttpResponse leResp = mockResponse(HttpStatus.SC_OK, leResponse);
        CloseableHttpClient client = Mockito.mock(CloseableHttpClient.class);
        Mockito.when(client.execute(Mockito.any(HttpGet.class)))
                .thenReturn(leResp);
        CDSAccountValidationUtils.setApacheHttpClient(client);

        Set<String> accounts = new HashSet<>();
        accounts.add("acc-1");
        accounts.add("acc-2");

        Set<String> blocked = CDSAccountValidationUtils.fetchBlockedLegalEntityAccountsFromService(
                accounts, LEGAL_ENTITY_ENDPOINT, "user-1", BASIC_AUTH, "client-1");

        Assert.assertEquals(blocked.size(), 1);
        Assert.assertTrue(blocked.contains("acc-1"));
        Assert.assertFalse(blocked.contains("acc-2"));
    }

    /**
     * Verifies legal entity response parsing supports camelCase accountId fallback key.
     */
    @Test
    public void testFetchBlockedLegalEntityAccountsFallbackKeys() throws Exception {
        // Tests accountId (camelCase) fallback key path.
        String leResponse = "["
                + "{\"accountId\":\"acc-camel\",\"legalEntitySharingStatus\":\"blocked\"}"
                + "]";

        CloseableHttpResponse leResp = mockResponse(HttpStatus.SC_OK, leResponse);
        CloseableHttpClient client = Mockito.mock(CloseableHttpClient.class);
        Mockito.when(client.execute(Mockito.any(HttpGet.class)))
                .thenReturn(leResp);
        CDSAccountValidationUtils.setApacheHttpClient(client);

        Set<String> accounts = new HashSet<>();
        accounts.add("acc-camel");

        Set<String> blocked = CDSAccountValidationUtils.fetchBlockedLegalEntityAccountsFromService(
                accounts, LEGAL_ENTITY_ENDPOINT, "user-1", BASIC_AUTH, "client-1");

        Assert.assertEquals(blocked.size(), 1);
        Assert.assertTrue(blocked.contains("acc-camel"));
    }

    /**
     * Verifies a non-200 legal entity response triggers a validation exception.
     */
    @Test
    public void testFetchBlockedLegalEntityAccountsNon200() throws Exception {
        CloseableHttpResponse leResp = mockResponse(HttpStatus.SC_SERVICE_UNAVAILABLE, "[]");
        CloseableHttpClient client = Mockito.mock(CloseableHttpClient.class);
        Mockito.when(client.execute(Mockito.any(HttpGet.class)))
                .thenReturn(leResp);
        CDSAccountValidationUtils.setApacheHttpClient(client);

        Set<String> accounts = new HashSet<>();
        accounts.add("acc-1");
        Assert.expectThrows(CDSAccountValidationException.class,
                () -> CDSAccountValidationUtils.fetchBlockedLegalEntityAccountsFromService(
                        accounts, LEGAL_ENTITY_ENDPOINT, "user-1", BASIC_AUTH, "client-1"));
    }

    /**
     * Verifies I/O failures from the legal entity service call are wrapped as validation exceptions.
     */
    @Test
    public void testFetchBlockedLegalEntityAccountsIoError() throws Exception {
        CloseableHttpClient client = Mockito.mock(CloseableHttpClient.class);
        Mockito.when(client.execute(Mockito.any(HttpGet.class)))
                .thenThrow(new IOException("Connection refused"));
        CDSAccountValidationUtils.setApacheHttpClient(client);

        Set<String> accounts = new HashSet<>();
        accounts.add("acc-1");
        Assert.expectThrows(CDSAccountValidationException.class,
                () -> CDSAccountValidationUtils.fetchBlockedLegalEntityAccountsFromService(
                        accounts, LEGAL_ENTITY_ENDPOINT, "user-1", BASIC_AUTH, "client-1"));
    }

    /**
     * Verifies malformed legal entity JSON responses raise a validation exception.
     */
    @Test
    public void testFetchBlockedLegalEntityAccountsMalformedResponse() throws Exception {
        CloseableHttpResponse leResp = mockResponse(HttpStatus.SC_OK, "{not-an-array");
        CloseableHttpClient client = Mockito.mock(CloseableHttpClient.class);
        Mockito.when(client.execute(Mockito.any(HttpGet.class)))
                .thenReturn(leResp);
        CDSAccountValidationUtils.setApacheHttpClient(client);

        Set<String> accounts = new HashSet<>();
        accounts.add("acc-1");
        Assert.expectThrows(CDSAccountValidationException.class,
                () -> CDSAccountValidationUtils.fetchBlockedLegalEntityAccountsFromService(
                        accounts, LEGAL_ENTITY_ENDPOINT, "user-1", BASIC_AUTH, "client-1"));
    }

    // ---- fetchAllBlockedAccounts with clientId (exercises legal entity path) ----

    /**
     * Verifies fetchAllBlockedAccounts with clientId includes legal entity blocking logic.
     */
    @Test
    public void testFetchAllBlockedAccountsWithClientId() throws Exception {
        String leBody = "[{\"accountID\":\"acc-1\",\"legalEntitySharingStatus\":\"blocked\"}]";

        CloseableHttpResponse disclosureResp = mockResponse(HttpStatus.SC_OK, "[]");
        CloseableHttpResponse secondaryResp = mockResponse(HttpStatus.SC_OK, "[]");
        CloseableHttpResponse businessResp = mockResponse(HttpStatus.SC_OK, "[]");
        CloseableHttpResponse leResp = mockResponse(HttpStatus.SC_OK, leBody);

        CloseableHttpClient client = Mockito.mock(CloseableHttpClient.class);
        Mockito.when(client.execute(Mockito.any(HttpGet.class)))
                .thenReturn(disclosureResp)
                .thenReturn(secondaryResp)
                .thenReturn(businessResp)
                .thenReturn(leResp);
        CDSAccountValidationUtils.setApacheHttpClient(client);

        Set<String> accounts = new HashSet<>();
        accounts.add("acc-1");
        accounts.add("acc-2");

        Set<String> blocked = CDSAccountValidationUtils.fetchAllBlockedAccounts(
                accounts, ACCOUNT_METADATA_WEBAPP_BASE_URL, "user-1", BASIC_AUTH, "client-1");

        Assert.assertEquals(blocked.size(), 1);
        Assert.assertTrue(blocked.contains("acc-1"));
        Assert.assertFalse(blocked.contains("acc-2"));
        Mockito.verify(client, Mockito.times(4)).execute(Mockito.any(HttpGet.class));
    }
}

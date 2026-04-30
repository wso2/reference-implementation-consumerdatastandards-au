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

package org.wso2.openbanking.consumerdatastandards.account.metadata.utils;

import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.openbanking.consumerdatastandards.account.metadata.exceptions.AccountMetadataException;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.SecondaryAccountInstructionItem;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.SecondaryAccountInstructionItem.SecondaryAccountInstructionStatusEnum;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for {@link ConsentExpiryUtil}.
 */
public class ConsentExpiryUtilTest {

    private CloseableHttpClient mockHttpClient;
    private ConsentExpiryUtil.HttpClientSupplier originalSupplier;

    @BeforeMethod
    public void setUp() throws Exception {
        mockHttpClient = Mockito.mock(CloseableHttpClient.class);

        Field supplierField = ConsentExpiryUtil.class.getDeclaredField("httpClientSupplier");
        supplierField.setAccessible(true);
        originalSupplier = (ConsentExpiryUtil.HttpClientSupplier) supplierField.get(null);
        supplierField.set(null, (ConsentExpiryUtil.HttpClientSupplier) () -> mockHttpClient);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        Field supplierField = ConsentExpiryUtil.class.getDeclaredField("httpClientSupplier");
        supplierField.setAccessible(true);
        supplierField.set(null, originalSupplier);
    }

    // -------------------------------------------------------------------------
    // fetchConsentIdsForUsers
    // -------------------------------------------------------------------------

    /**
     * Verifies consent ID lookup fails when the token request cannot be executed.
     */
    @Test(expectedExceptions = AccountMetadataException.class)
    public void testFetchConsentIdsForUsers_tokenFailure_throwsException() throws Exception {
        Mockito.when(mockHttpClient.execute(Mockito.any()))
                .thenThrow(new IOException("conn refused"));

        List<SecondaryAccountInstructionItem> items = Collections.singletonList(
                buildItem("acc1", "user1"));

        ConsentExpiryUtil.fetchConsentIdsForUsers(items);
    }

    /**
     * Verifies consent ID lookup deduplicates repeated user IDs before searching.
     */
    @Test
    public void testFetchConsentIdsForUsers_deduplicatesUserIds() throws Exception {
        CloseableHttpResponse tokenResp =
                buildResponse(200, "{\"access_token\":\"tok123\"}");
        CloseableHttpResponse consentResp =
                buildResponse(200, "{\"data\":[{\"consentId\":\"c1\",\"clientId\":\"client1\"}]}");

        Mockito.when(mockHttpClient.execute(Mockito.any()))
                .thenReturn(tokenResp)
                .thenReturn(consentResp);

        List<SecondaryAccountInstructionItem> items = Arrays.asList(
                buildItem("acc1", "user1"),
                buildItem("acc2", "user1"));

        Map<String, String> result = ConsentExpiryUtil.fetchConsentIdsForUsers(items);
        Assert.assertEquals(result.size(), 1);
        Assert.assertTrue(result.containsKey("c1"));
        Assert.assertEquals(result.get("c1"), "client1");

        // token call + 1 consent search (userId deduplicated)
        Mockito.verify(mockHttpClient, Mockito.times(2)).execute(Mockito.any());
    }

    /**
     * Verifies consent ID lookup returns consent-client mappings for multiple users.
     */
    @Test
    public void testFetchConsentIdsForUsers_multipleUsers() throws Exception {
        CloseableHttpResponse tokenResp =
                buildResponse(200, "{\"access_token\":\"tok123\"}");
        CloseableHttpResponse consentUser1 =
                buildResponse(200, "{\"data\":[{\"consentId\":\"c1\",\"clientId\":\"clientA\"}]}");
        CloseableHttpResponse consentUser2 =
                buildResponse(200,
                        "{\"data\":[{\"consentId\":\"c2\",\"clientId\":\"clientB\"}," +
                        "{\"consentId\":\"c3\",\"clientId\":\"clientB\"}]}");

        Mockito.when(mockHttpClient.execute(Mockito.any()))
                .thenReturn(tokenResp)
                .thenReturn(consentUser1)
                .thenReturn(consentUser2);

        List<SecondaryAccountInstructionItem> items = Arrays.asList(
                buildItem("acc1", "user1"),
                buildItem("acc2", "user2"));

        Map<String, String> result = ConsentExpiryUtil.fetchConsentIdsForUsers(items);
        Assert.assertEquals(result.size(), 3);
        Assert.assertEquals(result.get("c1"), "clientA");
        Assert.assertEquals(result.get("c2"), "clientB");
        Assert.assertEquals(result.get("c3"), "clientB");
    }

    // -------------------------------------------------------------------------
    // fetchAccessToken
    // -------------------------------------------------------------------------

    /**
     * Verifies access token retrieval returns the token value from a successful response.
     */
    @Test
    public void testFetchAccessToken_success_returnsToken() throws Exception {
        CloseableHttpResponse resp =
                buildResponse(200, "{\"access_token\":\"my-token\",\"token_type\":\"Bearer\"}");
        Mockito.when(mockHttpClient.execute(Mockito.any())).thenReturn(resp);

        String token = ConsentExpiryUtil.fetchAccessToken();
        Assert.assertEquals(token, "my-token");
    }

    /**
     * Verifies access token retrieval fails for a non-success HTTP response.
     */
    @Test(expectedExceptions = AccountMetadataException.class)
    public void testFetchAccessToken_non200_throwsException() throws Exception {
        CloseableHttpResponse resp = buildResponse(401, "{}");
        Mockito.when(mockHttpClient.execute(Mockito.any())).thenReturn(resp);

        ConsentExpiryUtil.fetchAccessToken();
    }

    /**
     * Verifies access token retrieval fails when the HTTP client throws an I/O error.
     */
    @Test(expectedExceptions = AccountMetadataException.class)
    public void testFetchAccessToken_ioException_throwsException() throws Exception {
        Mockito.when(mockHttpClient.execute(Mockito.any()))
                .thenThrow(new IOException("connection refused"));

        ConsentExpiryUtil.fetchAccessToken();
    }

    // -------------------------------------------------------------------------
    // fetchConsentIdsForUser
    // -------------------------------------------------------------------------

    /**
     * Verifies consent lookup rejects a blank user ID.
     */
    @Test(expectedExceptions = AccountMetadataException.class)
    public void testFetchConsentIdsForUser_blankUserId_throwsException() throws Exception {
        ConsentExpiryUtil.fetchConsentIdsForUser("", "token");
    }

    /**
     * Verifies consent lookup rejects a null user ID.
     */
    @Test(expectedExceptions = AccountMetadataException.class)
    public void testFetchConsentIdsForUser_nullUserId_throwsException() throws Exception {
        ConsentExpiryUtil.fetchConsentIdsForUser(null, "token");
    }

    /**
     * Verifies consent lookup rejects a blank token.
     */
    @Test(expectedExceptions = AccountMetadataException.class)
    public void testFetchConsentIdsForUser_blankToken_throwsException() throws Exception {
        ConsentExpiryUtil.fetchConsentIdsForUser("user1", "  ");
    }

    /**
     * Verifies consent lookup rejects a null token.
     */
    @Test(expectedExceptions = AccountMetadataException.class)
    public void testFetchConsentIdsForUser_nullToken_throwsException() throws Exception {
        ConsentExpiryUtil.fetchConsentIdsForUser("user1", null);
    }

    /**
     * Verifies consent lookup returns consent-client mappings for a single user.
     */
    @Test
    public void testFetchConsentIdsForUser_success_returnsConsentClientMap() throws Exception {
        CloseableHttpResponse resp = buildResponse(200,
                "{\"data\":[{\"consentId\":\"id1\",\"clientId\":\"cA\"},{\"consentId\":\"id2\",\"clientId\":\"cB\"}]}");
        Mockito.when(mockHttpClient.execute(Mockito.any())).thenReturn(resp);

        Map<String, String> result = ConsentExpiryUtil.fetchConsentIdsForUser("user1", "tok123");
        Assert.assertEquals(result.size(), 2);
        Assert.assertEquals(result.get("id1"), "cA");
        Assert.assertEquals(result.get("id2"), "cB");
    }

    /**
     * Verifies consent lookup fails when the response status is not successful.
     */
    @Test(expectedExceptions = AccountMetadataException.class)
    public void testFetchConsentIdsForUser_non200_throwsException() throws Exception {
        CloseableHttpResponse resp = buildResponse(403, "{}");
        Mockito.when(mockHttpClient.execute(Mockito.any())).thenReturn(resp);

        ConsentExpiryUtil.fetchConsentIdsForUser("user1", "tok123");
    }

    /**
     * Verifies consent lookup fails when the HTTP client throws an I/O error.
     */
    @Test(expectedExceptions = AccountMetadataException.class)
    public void testFetchConsentIdsForUser_ioException_throwsException() throws Exception {
        Mockito.when(mockHttpClient.execute(Mockito.any()))
                .thenThrow(new IOException("network error"));

        ConsentExpiryUtil.fetchConsentIdsForUser("user1", "tok123");
    }

    /**
     * Verifies consent lookup returns an empty map when the response data array is empty.
     */
    @Test
    public void testFetchConsentIdsForUser_emptyDataArray_returnsEmptyMap() throws Exception {
        CloseableHttpResponse resp = buildResponse(200, "{\"data\":[]}");
        Mockito.when(mockHttpClient.execute(Mockito.any())).thenReturn(resp);

        Map<String, String> result = ConsentExpiryUtil.fetchConsentIdsForUser("user1", "tok123");
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
    }

    /**
     * Verifies consent lookup fails when the expected data field is missing.
     */
    @Test(expectedExceptions = AccountMetadataException.class)
    public void testFetchConsentIdsForUser_missingDataField_throwsException() throws Exception {
        CloseableHttpResponse resp =
                buildResponse(200, "{\"unexpected\":\"shape\"}");
        Mockito.when(mockHttpClient.execute(Mockito.any())).thenReturn(resp);

        ConsentExpiryUtil.fetchConsentIdsForUser("user1", "tok123");
    }

    /**
     * Verifies consent lookup fails when the data field is not an array.
     */
    @Test(expectedExceptions = AccountMetadataException.class)
    public void testFetchConsentIdsForUser_dataNotArray_throwsException() throws Exception {
        CloseableHttpResponse resp =
                buildResponse(200, "{\"data\":{\"total\":0}}");
        Mockito.when(mockHttpClient.execute(Mockito.any())).thenReturn(resp);

        ConsentExpiryUtil.fetchConsentIdsForUser("user1", "tok123");
    }

    /**
     * Verifies consent lookup fails when the response body contains invalid JSON.
     */
    @Test(expectedExceptions = AccountMetadataException.class)
    public void testFetchConsentIdsForUser_invalidJson_throwsException() throws Exception {
        CloseableHttpResponse resp = buildResponse(200, "not-json");
        Mockito.when(mockHttpClient.execute(Mockito.any())).thenReturn(resp);

        ConsentExpiryUtil.fetchConsentIdsForUser("user1", "tok123");
    }

    /**
     * Verifies consent lookup stores an empty string when a consent item has no client ID.
     */
    @Test
    public void testFetchConsentIdsForUser_missingClientId_storesEmptyString() throws Exception {
        CloseableHttpResponse resp = buildResponse(200, "{\"data\":[{\"consentId\":\"id1\"}]}");
        Mockito.when(mockHttpClient.execute(Mockito.any())).thenReturn(resp);

        Map<String, String> result = ConsentExpiryUtil.fetchConsentIdsForUser("user1", "tok123");
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.get("id1"), "");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Build a mocked HTTP response for the given status code and body.
     *
     * @param statusCode the HTTP status code to return
     * @param body the response payload to return
     * @return a mocked closeable HTTP response
     */
    private CloseableHttpResponse buildResponse(int statusCode, String body) {
        CloseableHttpResponse response = Mockito.mock(CloseableHttpResponse.class);
        StatusLine statusLine = Mockito.mock(StatusLine.class);
        Mockito.when(statusLine.getStatusCode()).thenReturn(statusCode);
        Mockito.when(response.getStatusLine()).thenReturn(statusLine);
        Mockito.when(response.getEntity())
                .thenReturn(new StringEntity(body, StandardCharsets.UTF_8));
        return response;
    }

    // -------------------------------------------------------------------------
    // expireConsents
    // -------------------------------------------------------------------------

    /**
     * Verifies consent expiry does nothing when the input map is empty.
     */
    @Test
    public void testExpireConsents_emptyMap() throws AccountMetadataException {
        ConsentExpiryUtil.expireConsents(Collections.emptyMap());
        Mockito.verifyZeroInteractions(mockHttpClient);
    }

    /**
     * Verifies consent expiry issues one update request per consent in the input.
     */
    @Test
    public void testExpireConsents_success_callsPutForEachConsent() throws Exception {
        CloseableHttpResponse ok1 = buildResponse(200, "{}");
        CloseableHttpResponse ok2 = buildResponse(200, "{}");
        Mockito.when(mockHttpClient.execute(Mockito.any()))
                .thenReturn(ok1)
                .thenReturn(ok2);

        Map<String, String> input = new LinkedHashMap<>();
        input.put("consent-1", "clientA");
        input.put("consent-2", "clientA");

        ConsentExpiryUtil.expireConsents(input);

        Mockito.verify(mockHttpClient, Mockito.times(2)).execute(Mockito.any());
    }

    /**
     * Verifies consent expiry processes multiple consent entries.
     */
    @Test
    public void testExpireConsents_multipleConsents_callsPutForAll() throws Exception {
        CloseableHttpResponse ok1 = buildResponse(200, "{}");
        CloseableHttpResponse ok2 = buildResponse(200, "{}");
        CloseableHttpResponse ok3 = buildResponse(200, "{}");
        Mockito.when(mockHttpClient.execute(Mockito.any()))
                .thenReturn(ok1)
                .thenReturn(ok2)
                .thenReturn(ok3);

        Map<String, String> input = new LinkedHashMap<>();
        input.put("c1", "clientA");
        input.put("c2", "clientA");
        input.put("c3", "clientB");

        ConsentExpiryUtil.expireConsents(input);

        Mockito.verify(mockHttpClient, Mockito.times(3)).execute(Mockito.any());
    }

    /**
     * Verifies consent expiry throws when one of the updates fails after attempting all entries.
     */
    @Test(expectedExceptions = AccountMetadataException.class)
    public void testExpireConsents_non200_throwsAfterAttemptingAll() throws Exception {
        CloseableHttpResponse fail = buildResponse(500, "{}");
        CloseableHttpResponse ok = buildResponse(200, "{}");
        Mockito.when(mockHttpClient.execute(Mockito.any()))
                .thenReturn(fail)
                .thenReturn(ok);

        Map<String, String> input = new LinkedHashMap<>();
        input.put("c-fail", "clientA");
        input.put("c-ok", "clientA");

        // Attempts both consents, then throws because one failed
        ConsentExpiryUtil.expireConsents(input);

        Mockito.verify(mockHttpClient, Mockito.times(2)).execute(Mockito.any());
    }

    /**
     * Verifies consent expiry throws when the HTTP client raises an I/O error during processing.
     */
    @Test(expectedExceptions = AccountMetadataException.class)
    public void testExpireConsents_ioException_throwsAfterAttemptingAll() throws Exception {
        Mockito.when(mockHttpClient.execute(Mockito.any()))
                .thenThrow(new IOException("network error"))
                .thenReturn(buildResponse(200, "{}"));

        Map<String, String> input = new LinkedHashMap<>();
        input.put("c-fail", "clientA");
        input.put("c-ok", "clientA");

        ConsentExpiryUtil.expireConsents(input);

        Mockito.verify(mockHttpClient, Mockito.times(2)).execute(Mockito.any());
    }

    /**
     * Build a secondary account instruction test item.
     *
     * @param accountId the account identifier
     * @param userId the user identifier
     * @return the constructed instruction item
     */
    private SecondaryAccountInstructionItem buildItem(String accountId, String userId) {
        return new SecondaryAccountInstructionItem(
                accountId, userId, false, SecondaryAccountInstructionStatusEnum.inactive);
    }
}

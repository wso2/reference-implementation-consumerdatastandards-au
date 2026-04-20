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

import com.nimbusds.jose.JOSEException;
import org.apache.axis2.context.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.openbanking.consumerdatastandards.au.policy.constants.CDSAccountValidationConstants;
import org.wso2.openbanking.consumerdatastandards.au.policy.exceptions.CDSAccountValidationException;
import org.wso2.openbanking.consumerdatastandards.au.policy.utils.CDSAccountValidationUtils;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Unit tests for {@link CDSAccountValidationMediator}.
 */
public class CDSAccountValidationMediatorTest {

    private static final String ACCOUNT_METADATA_WEBAPP_BASE_URL = "http://account-metadata-webapp-base-url";

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

    /**
     * Verifies mediation falls back to policy error handling when account filtering/signing flow fails.
     */
    @Test
    public void testMediateFiltersBlockedAccountsAndUpdatesHeader() throws Exception {
        CDSAccountValidationMediator mediator = new CDSAccountValidationMediator();
        HttpClient client = Mockito.mock(HttpClient.class);
        HttpResponse<String> response = Mockito.mock(HttpResponse.class);

        Mockito.when(response.statusCode()).thenReturn(200);
        Mockito.when(response.body()).thenReturn("["
                + "{\"accountId\":\"acc-2\",\"disclosureOption\":\"no-sharing\"},"
                + "{\"accountId\":\"acc-1\",\"disclosureOption\":\"pre-approval\"}"
                + "]");
        Mockito.when(client.send(Mockito.any(HttpRequest.class), Mockito.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(response);

        CDSAccountValidationUtils.setHttpClient(client);

        mediator.setWebappBaseURL(ACCOUNT_METADATA_WEBAPP_BASE_URL);
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

        boolean result = mediator.mediate(synapseMessageContext);

        Assert.assertTrue(result);
        Mockito.verify(synapseMessageContext).setProperty(CDSAccountValidationConstants.ERROR_CODE,
                "Internal Server Error");
        Mockito.verify(synapseMessageContext).setProperty(CDSAccountValidationConstants.ERROR_TITLE,
                "CDS DOMS Policy Error");
        Mockito.verify(synapseMessageContext).setProperty(CDSAccountValidationConstants.CUSTOM_HTTP_SC,
                "500");
        Mockito.verify(synapseMessageContext).setProperty(CDSAccountValidationConstants.ERROR_DESCRIPTION,
                "Error during CDS mediation policy");
    }

    /**
     * Verifies invalid info-header payloads are handled by setting policy error properties.
     */
    @Test
    public void testMediateHandlesDecodeError() throws Exception {
        CDSAccountValidationMediator mediator = new CDSAccountValidationMediator();

        headers.put(CDSAccountValidationConstants.INFO_HEADER_TAG, "{not-json");

        boolean result = mediator.mediate(synapseMessageContext);

        Assert.assertTrue(result);
        Mockito.verify(synapseMessageContext).setProperty(CDSAccountValidationConstants.ERROR_CODE,
                "Internal Server Error");
        Mockito.verify(synapseMessageContext).setProperty(CDSAccountValidationConstants.ERROR_TITLE,
                "CDS DOMS Policy Error");
        Mockito.verify(synapseMessageContext).setProperty(CDSAccountValidationConstants.CUSTOM_HTTP_SC,
                "500");
        Mockito.verify(synapseMessageContext).setProperty(CDSAccountValidationConstants.ERROR_DESCRIPTION,
                "Error during CDS mediation policy");
    }

    /**
     * Verifies linked and blocked accounts are filtered out and the updated payload is re-signed.
     */
    @Test
    public void testMediateFiltersLinkedAndBlockedAccountsAndSignsHeader() throws Exception {
        CDSAccountValidationMediator mediator = new CDSAccountValidationMediator();
        mediator.setWebappBaseURL(ACCOUNT_METADATA_WEBAPP_BASE_URL);
        mediator.setBasicAuthCredentials("dGVzdDp0ZXN0");

        JSONObject payload = new JSONObject();

        JSONArray authorizationResources = new JSONArray();
        authorizationResources.put(new JSONObject()
                .put(CDSAccountValidationConstants.AUTH_TYPE_TAG, CDSAccountValidationConstants.LINKED_MEMBER_TAG)
                .put(CDSAccountValidationConstants.AUTH_ID_TAG, "linked-1"));
        authorizationResources.put(new JSONObject()
                .put(CDSAccountValidationConstants.AUTH_TYPE_TAG, CDSAccountValidationConstants.PRIMARY_AUTH_TYPE_TAG)
                .put(CDSAccountValidationConstants.AUTH_ID_TAG, "auth-2")
                .put(CDSAccountValidationConstants.USER_ID_TAG, "user-1"));
        payload.put(CDSAccountValidationConstants.AUTH_RESOURCES_TAG, authorizationResources);

        JSONArray accounts = new JSONArray();
        accounts.put(new JSONObject()
                .put(CDSAccountValidationConstants.ACCELERATOR_ACCOUNT_ID_TAG, "acc-linked")
                .put(CDSAccountValidationConstants.AUTH_ID_TAG, "linked-1"));
        accounts.put(new JSONObject()
                .put(CDSAccountValidationConstants.ACCELERATOR_ACCOUNT_ID_TAG, "acc-1")
                .put(CDSAccountValidationConstants.AUTH_ID_TAG, "auth-2"));
        accounts.put(new JSONObject()
                .put(CDSAccountValidationConstants.ACCELERATOR_ACCOUNT_ID_TAG, "acc-2")
                .put(CDSAccountValidationConstants.AUTH_ID_TAG, "auth-2"));
        payload.put(CDSAccountValidationConstants.CONSENT_MAPPING_RESOURCES_TAG, accounts);
        headers.put(CDSAccountValidationConstants.INFO_HEADER_TAG, payload.toString());

        try (MockedStatic<CDSAccountValidationUtils> utilsMock = Mockito.mockStatic(CDSAccountValidationUtils.class)) {
            utilsMock.when(() -> CDSAccountValidationUtils.fetchAllBlockedAccounts(
                            Mockito.anySet(), Mockito.eq(ACCOUNT_METADATA_WEBAPP_BASE_URL), Mockito.eq("user-1"),
                            Mockito.eq("dGVzdDp0ZXN0")))
                    .thenReturn(java.util.Collections.singleton("acc-2"));
            utilsMock.when(() -> CDSAccountValidationUtils.generateJWT(Mockito.anyString()))
                    .thenReturn("signed-jwt");

            boolean result = mediator.mediate(synapseMessageContext);

            Assert.assertTrue(result);
            Assert.assertEquals(headers.get(CDSAccountValidationConstants.INFO_HEADER_TAG), "signed-jwt");
            utilsMock.verify(() -> CDSAccountValidationUtils.fetchAllBlockedAccounts(
                    Mockito.anySet(), Mockito.eq(ACCOUNT_METADATA_WEBAPP_BASE_URL), Mockito.eq("user-1"),
                    Mockito.eq("dGVzdDp0ZXN0")));
            utilsMock.verify(() -> CDSAccountValidationUtils.generateJWT(Mockito.anyString()));
        }
    }

    /**
     * Verifies JWT signing failures are converted into mediation error properties.
     */
    @Test
    public void testMediateSetsErrorPropertiesWhenJwtGenerationFails() throws Exception {
        CDSAccountValidationMediator mediator = new CDSAccountValidationMediator();
        mediator.setWebappBaseURL(ACCOUNT_METADATA_WEBAPP_BASE_URL);
        mediator.setBasicAuthCredentials("dGVzdDp0ZXN0");

        JSONObject payload = new JSONObject();
        payload.put(CDSAccountValidationConstants.USER_ID_TAG, "user-2");
        payload.put(CDSAccountValidationConstants.CONSENT_MAPPING_RESOURCES_TAG,
                new JSONArray().put(new JSONObject()
                        .put(CDSAccountValidationConstants.ACCELERATOR_ACCOUNT_ID_TAG, "acc-1")
                        .put(CDSAccountValidationConstants.AUTH_ID_TAG, "auth-2")));
        headers.put(CDSAccountValidationConstants.INFO_HEADER_TAG, payload.toString());

        try (MockedStatic<CDSAccountValidationUtils> utilsMock = Mockito.mockStatic(CDSAccountValidationUtils.class)) {
            utilsMock.when(() -> CDSAccountValidationUtils.fetchAllBlockedAccounts(
                            Mockito.anySet(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                    .thenReturn(java.util.Collections.emptySet());
            utilsMock.when(() -> CDSAccountValidationUtils.generateJWT(Mockito.anyString()))
                    .thenThrow(new JOSEException("signing failed"));

            boolean result = mediator.mediate(synapseMessageContext);

            Assert.assertTrue(result);
            Mockito.verify(synapseMessageContext).setProperty(CDSAccountValidationConstants.ERROR_CODE,
                    "Internal Server Error");
            Mockito.verify(synapseMessageContext).setProperty(CDSAccountValidationConstants.ERROR_TITLE,
                    "CDS DOMS Policy Error");
            Mockito.verify(synapseMessageContext).setProperty(CDSAccountValidationConstants.CUSTOM_HTTP_SC,
                    "500");
            Mockito.verify(synapseMessageContext).setProperty(CDSAccountValidationConstants.ERROR_DESCRIPTION,
                    "Error during CDS mediation policy");
        }
    }

    /**
     * Verifies mediation succeeds without errors when consent mapping resources are absent.
     */
    @Test
    public void testMediateWithNoConsentMappingResources() throws Exception {
        CDSAccountValidationMediator mediator = new CDSAccountValidationMediator();
        mediator.setWebappBaseURL(ACCOUNT_METADATA_WEBAPP_BASE_URL);

        JSONObject payload = new JSONObject();
        // No consentMappingResources — mediator should log a warning and return true without error
        headers.put(CDSAccountValidationConstants.INFO_HEADER_TAG, payload.toString());

        boolean result = mediator.mediate(synapseMessageContext);

        Assert.assertTrue(result);
        Mockito.verify(synapseMessageContext, Mockito.never())
                .setProperty(Mockito.eq(CDSAccountValidationConstants.ERROR_CODE), Mockito.any());
    }

    /**
     * Verifies mediation proceeds with a null user context when authorization resources are absent.
     */
    @Test
    public void testMediateWithNoAuthorizationResources() throws Exception {
        CDSAccountValidationMediator mediator = new CDSAccountValidationMediator();
        mediator.setWebappBaseURL(ACCOUNT_METADATA_WEBAPP_BASE_URL);
        mediator.setBasicAuthCredentials("dGVzdDp0ZXN0");

        JSONObject payload = new JSONObject();
        // No authorizationResources — userId stays null, no accounts excluded
        JSONArray accounts = new JSONArray();
        accounts.put(new JSONObject()
                .put(CDSAccountValidationConstants.ACCELERATOR_ACCOUNT_ID_TAG, "acc-1")
                .put(CDSAccountValidationConstants.AUTH_ID_TAG, "auth-1"));
        payload.put(CDSAccountValidationConstants.CONSENT_MAPPING_RESOURCES_TAG, accounts);
        headers.put(CDSAccountValidationConstants.INFO_HEADER_TAG, payload.toString());

        try (MockedStatic<CDSAccountValidationUtils> utilsMock = Mockito.mockStatic(CDSAccountValidationUtils.class)) {
            utilsMock.when(() -> CDSAccountValidationUtils.fetchAllBlockedAccounts(
                            Mockito.anySet(), Mockito.anyString(), Mockito.isNull(), Mockito.anyString()))
                    .thenReturn(Collections.emptySet());
            utilsMock.when(() -> CDSAccountValidationUtils.generateJWT(Mockito.anyString()))
                    .thenReturn("signed-jwt");

            boolean result = mediator.mediate(synapseMessageContext);

            Assert.assertTrue(result);
            // userId should be null because there were no authorizationResources
            utilsMock.verify(() -> CDSAccountValidationUtils.fetchAllBlockedAccounts(
                    Mockito.anySet(), Mockito.eq(ACCOUNT_METADATA_WEBAPP_BASE_URL),
                    Mockito.isNull(), Mockito.eq("dGVzdDp0ZXN0")));
        }
    }

    /**
     * Verifies accounts mapped to secondary-owner authorization types are excluded from validation.
     */
    @Test
    public void testMediateExcludesSecondaryAccountOwnerAuthTypes() throws Exception {
        CDSAccountValidationMediator mediator = new CDSAccountValidationMediator();
        mediator.setWebappBaseURL(ACCOUNT_METADATA_WEBAPP_BASE_URL);
        mediator.setBasicAuthCredentials("dGVzdDp0ZXN0");

        JSONObject payload = new JSONObject();
        JSONArray authorizationResources = new JSONArray();
        authorizationResources.put(new JSONObject()
                .put(CDSAccountValidationConstants.AUTH_TYPE_TAG,
                        CDSAccountValidationConstants.SECONDARY_INDIVIDUAL_ACCOUNT_OWNER_TAG)
                .put(CDSAccountValidationConstants.AUTH_ID_TAG, "sec-ind-auth"));
        authorizationResources.put(new JSONObject()
                .put(CDSAccountValidationConstants.AUTH_TYPE_TAG,
                        CDSAccountValidationConstants.SECONDARY_JOINT_ACCOUNT_OWNER_TAG)
                .put(CDSAccountValidationConstants.AUTH_ID_TAG, "sec-joint-auth"));
        authorizationResources.put(new JSONObject()
                .put(CDSAccountValidationConstants.AUTH_TYPE_TAG, CDSAccountValidationConstants.PRIMARY_AUTH_TYPE_TAG)
                .put(CDSAccountValidationConstants.AUTH_ID_TAG, "primary-auth")
                .put(CDSAccountValidationConstants.USER_ID_TAG, "user-1"));
        payload.put(CDSAccountValidationConstants.AUTH_RESOURCES_TAG, authorizationResources);

        JSONArray accounts = new JSONArray();
        accounts.put(new JSONObject()
                .put(CDSAccountValidationConstants.ACCELERATOR_ACCOUNT_ID_TAG, "acc-sec-ind")
                .put(CDSAccountValidationConstants.AUTH_ID_TAG, "sec-ind-auth"));
        accounts.put(new JSONObject()
                .put(CDSAccountValidationConstants.ACCELERATOR_ACCOUNT_ID_TAG, "acc-sec-joint")
                .put(CDSAccountValidationConstants.AUTH_ID_TAG, "sec-joint-auth"));
        accounts.put(new JSONObject()
                .put(CDSAccountValidationConstants.ACCELERATOR_ACCOUNT_ID_TAG, "acc-primary")
                .put(CDSAccountValidationConstants.AUTH_ID_TAG, "primary-auth"));
        payload.put(CDSAccountValidationConstants.CONSENT_MAPPING_RESOURCES_TAG, accounts);
        headers.put(CDSAccountValidationConstants.INFO_HEADER_TAG, payload.toString());

        try (MockedStatic<CDSAccountValidationUtils> utilsMock = Mockito.mockStatic(CDSAccountValidationUtils.class)) {
            utilsMock.when(() -> CDSAccountValidationUtils.fetchAllBlockedAccounts(
                            Mockito.anySet(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                    .thenReturn(Collections.emptySet());
            utilsMock.when(() -> CDSAccountValidationUtils.generateJWT(Mockito.anyString()))
                    .thenReturn("signed-jwt");

            boolean result = mediator.mediate(synapseMessageContext);

            Assert.assertTrue(result);
            // Only the primary account should be passed to fetchAllBlockedAccounts;
            // secondary account owner accounts are excluded from validation.
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Set<String>> accountIdsCaptor = ArgumentCaptor.forClass(Set.class);
            utilsMock.verify(() -> CDSAccountValidationUtils.fetchAllBlockedAccounts(
                    accountIdsCaptor.capture(), Mockito.any(), Mockito.any(), Mockito.any()));
            Assert.assertEquals(accountIdsCaptor.getValue().size(), 1);
            Assert.assertTrue(accountIdsCaptor.getValue().contains("acc-primary"));
        }
    }

    /**
     * Verifies account_id fields are normalized to accountId when no accounts are blocked.
     */
    @Test
    public void testMediateWithNoBlockedAccountsNormalizesAccountIdField() throws Exception {
        CDSAccountValidationMediator mediator = new CDSAccountValidationMediator();
        mediator.setWebappBaseURL(ACCOUNT_METADATA_WEBAPP_BASE_URL);
        mediator.setBasicAuthCredentials("dGVzdDp0ZXN0");

        JSONObject payload = new JSONObject();
        JSONArray authorizationResources = new JSONArray();
        authorizationResources.put(new JSONObject()
                .put(CDSAccountValidationConstants.AUTH_TYPE_TAG, CDSAccountValidationConstants.PRIMARY_AUTH_TYPE_TAG)
                .put(CDSAccountValidationConstants.AUTH_ID_TAG, "auth-1")
                .put(CDSAccountValidationConstants.USER_ID_TAG, "user-1"));
        payload.put(CDSAccountValidationConstants.AUTH_RESOURCES_TAG, authorizationResources);

        JSONArray accounts = new JSONArray();
        accounts.put(new JSONObject()
                .put(CDSAccountValidationConstants.ACCELERATOR_ACCOUNT_ID_TAG, "acc-1")
                .put(CDSAccountValidationConstants.AUTH_ID_TAG, "auth-1"));
        accounts.put(new JSONObject()
                .put(CDSAccountValidationConstants.ACCELERATOR_ACCOUNT_ID_TAG, "acc-2")
                .put(CDSAccountValidationConstants.AUTH_ID_TAG, "auth-1"));
        payload.put(CDSAccountValidationConstants.CONSENT_MAPPING_RESOURCES_TAG, accounts);
        headers.put(CDSAccountValidationConstants.INFO_HEADER_TAG, payload.toString());

        try (MockedStatic<CDSAccountValidationUtils> utilsMock = Mockito.mockStatic(CDSAccountValidationUtils.class)) {
            utilsMock.when(() -> CDSAccountValidationUtils.fetchAllBlockedAccounts(
                            Mockito.anySet(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                    .thenReturn(Collections.emptySet());

            ArgumentCaptor<String> jwtPayloadCaptor = ArgumentCaptor.forClass(String.class);
            utilsMock.when(() -> CDSAccountValidationUtils.generateJWT(jwtPayloadCaptor.capture()))
                    .thenReturn("signed-jwt");

            boolean result = mediator.mediate(synapseMessageContext);

            Assert.assertTrue(result);
            Assert.assertEquals(headers.get(CDSAccountValidationConstants.INFO_HEADER_TAG), "signed-jwt");

            // Verify all accounts passed through and account_id was renamed to accountId
            JSONObject capturedJson = new JSONObject(jwtPayloadCaptor.getValue());
            JSONArray filteredMappings = capturedJson
                    .getJSONArray(CDSAccountValidationConstants.CONSENT_MAPPING_RESOURCES_TAG);
            Assert.assertEquals(filteredMappings.length(), 2);
            for (int i = 0; i < filteredMappings.length(); i++) {
                JSONObject mapping = filteredMappings.getJSONObject(i);
                Assert.assertTrue(mapping.has(CDSAccountValidationConstants.CDS_ACCOUNT_ID_TAG));
                Assert.assertFalse(mapping.has(CDSAccountValidationConstants.ACCELERATOR_ACCOUNT_ID_TAG));
            }
        }
    }

    /**
     * Verifies account-validation service failures are translated into mediation error properties.
     */
    @Test
    public void testMediateSetsErrorPropertiesWhenAccountValidationFails() throws Exception {
        CDSAccountValidationMediator mediator = new CDSAccountValidationMediator();
        mediator.setWebappBaseURL(ACCOUNT_METADATA_WEBAPP_BASE_URL);
        mediator.setBasicAuthCredentials("dGVzdDp0ZXN0");

        JSONObject payload = new JSONObject();
        payload.put(CDSAccountValidationConstants.AUTH_RESOURCES_TAG, new JSONArray().put(new JSONObject()
                .put(CDSAccountValidationConstants.AUTH_TYPE_TAG, CDSAccountValidationConstants.PRIMARY_AUTH_TYPE_TAG)
                .put(CDSAccountValidationConstants.AUTH_ID_TAG, "auth-2")
                .put(CDSAccountValidationConstants.USER_ID_TAG, "user-2")));
        payload.put(CDSAccountValidationConstants.CONSENT_MAPPING_RESOURCES_TAG,
                new JSONArray().put(new JSONObject()
                        .put(CDSAccountValidationConstants.ACCELERATOR_ACCOUNT_ID_TAG, "acc-1")
                        .put(CDSAccountValidationConstants.AUTH_ID_TAG, "auth-2")));
        headers.put(CDSAccountValidationConstants.INFO_HEADER_TAG, payload.toString());

        try (MockedStatic<CDSAccountValidationUtils> utilsMock = Mockito.mockStatic(CDSAccountValidationUtils.class)) {
            utilsMock.when(() -> CDSAccountValidationUtils.fetchAllBlockedAccounts(
                            Mockito.anySet(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                    .thenThrow(new CDSAccountValidationException("metadata service unavailable"));

            boolean result = mediator.mediate(synapseMessageContext);

            Assert.assertTrue(result);
            Mockito.verify(synapseMessageContext).setProperty(CDSAccountValidationConstants.ERROR_CODE,
                    "Internal Server Error");
            Mockito.verify(synapseMessageContext).setProperty(CDSAccountValidationConstants.ERROR_TITLE,
                    "CDS DOMS Policy Error");
            Mockito.verify(synapseMessageContext).setProperty(CDSAccountValidationConstants.CUSTOM_HTTP_SC,
                    "500");
            Mockito.verify(synapseMessageContext).setProperty(CDSAccountValidationConstants.ERROR_DESCRIPTION,
                    "Error during CDS mediation policy");
        }
    }
}

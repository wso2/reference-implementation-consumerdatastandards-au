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
package org.wso2.openbanking.cds.consent.extensions.authorize.impl.retrieval;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.accelerator.identity.push.auth.extension.request.validator.util.PushAuthRequestValidatorUtils;
import net.minidev.json.JSONObject;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.oauth.cache.SessionDataCache;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheEntry;
import org.wso2.carbon.identity.oauth2.model.OAuth2Parameters;
import org.wso2.openbanking.cds.consent.extensions.authorize.utils.CDSConsentCommonUtil;
import org.wso2.openbanking.cds.consent.extensions.authorize.utils.CDSDataRetrievalUtil;
import org.wso2.openbanking.cds.consent.extensions.common.CDSConsentExtensionConstants;
import org.wso2.openbanking.cds.consent.extensions.util.CDSConsentAuthorizeTestConstants;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.anyString;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Test class for CDS Consent Retrieval.
 */
@PrepareForTest({SessionDataCacheEntry.class, SessionDataCache.class, PushAuthRequestValidatorUtils.class,
        CDSDataRetrievalUtil.class, CDSConsentCommonUtil.class})
@PowerMockIgnore({"com.wso2.openbanking.accelerator.consent.extensions.common.*", "jdk.internal.reflect.*"})
public class CDSConsentRetrievalStepTests extends PowerMockTestCase {

    private CDSConsentRetrievalStep cdsConsentRetrievalStep;
    private ConsentData consentDataMock;
    private ConsentResource consentResourceMock;

    @BeforeClass
    public void initClass() {

        cdsConsentRetrievalStep = new CDSConsentRetrievalStep();
        consentDataMock = mock(ConsentData.class);
        consentResourceMock = mock(ConsentResource.class);
    }

    @Test(priority = 2)
    public void testConsentRetrievalWithValidRequestObject() throws OpenBankingException {

        JSONObject jsonObject = new JSONObject();
        String request = "request=" + CDSConsentAuthorizeTestConstants.VALID_REQUEST_OBJECT;
        String redirectUri = "redirect_uri=https://www.google.com/redirects/redirect1&";
        String scopeString = "common:customer.basic:read common:customer.detail:read openid profile";
        String clientId = "client-id";
        String spFullName = "sp-full-name";
        String sampleQueryParams = redirectUri + request;
        when(consentDataMock.getSpQueryParams()).thenReturn(sampleQueryParams);
        when(consentDataMock.getScopeString()).thenReturn(scopeString);
        when(consentDataMock.getClientId()).thenReturn(clientId);
        PowerMockito.stub(PowerMockito.method(CDSDataRetrievalUtil.class, "getServiceProviderFullName"))
                .toReturn(spFullName);
        when(consentDataMock.isRegulatory()).thenReturn(true);
        cdsConsentRetrievalStep.execute(consentDataMock, jsonObject);
        Assert.assertTrue(!jsonObject.isEmpty());
    }

    @Test(priority = 2)
    public void testConsentRetrievalWithMoreThanOneYearSharingDuration() {

        JSONObject jsonObject = new JSONObject();
        String request = "request=" + CDSConsentAuthorizeTestConstants.VALID_REQUEST_OBJECT_DIFF;
        String redirectUri = "redirect_uri=https://www.google.com/redirects/redirect1&";
        String scopeString = "common:customer.basic:read common:customer.detail:read openid profile";
        String clientId = "client-id";
        String spFullName = "sp-full-name";
        String sampleQueryParams = redirectUri + request;
        when(consentDataMock.getSpQueryParams()).thenReturn(sampleQueryParams);
        when(consentDataMock.getScopeString()).thenReturn(scopeString);
        when(consentDataMock.getClientId()).thenReturn(clientId);
        PowerMockito.stub(PowerMockito.method(CDSDataRetrievalUtil.class, "getServiceProviderFullName"))
                .toReturn(spFullName);
        when(consentDataMock.isRegulatory()).thenReturn(true);
        cdsConsentRetrievalStep.execute(consentDataMock, jsonObject);
        Assert.assertTrue(!jsonObject.isEmpty());
    }

    @Test(priority = 2)
    public void testConsentRetrievalWithNoSharingDurationValueInRequestObject() {

        JSONObject jsonObject = new JSONObject();
        String request = "request=" + CDSConsentAuthorizeTestConstants.REQUEST_OBJECT_WITHOUT_SHARING_VAL;
        String redirectUri = "redirect_uri=https://www.google.com/redirects/redirect1&";
        String scopeString = "common:customer.basic:read common:customer.detail:read openid profile";
        String clientId = "client-id";
        String spFullName = "sp-full-name";
        String sampleQueryParams = redirectUri + request;
        when(consentDataMock.getSpQueryParams()).thenReturn(sampleQueryParams);
        when(consentDataMock.getScopeString()).thenReturn(scopeString);
        when(consentDataMock.getClientId()).thenReturn(clientId);
        PowerMockito.stub(PowerMockito.method(CDSDataRetrievalUtil.class, "getServiceProviderFullName"))
                .toReturn(spFullName);
        when(consentDataMock.isRegulatory()).thenReturn(true);
        cdsConsentRetrievalStep.execute(consentDataMock, jsonObject);
        Assert.assertTrue(!jsonObject.isEmpty());
    }

    @Test(priority = 2)
    public void testRequestUriFlow() {

        JSONObject jsonObject = new JSONObject();
        String request = "request_uri=" + "urn:ietf:params:oauth:request_uri:XKnDFSbXJWjuf0AY6gOT1EIuvdP8BQLo";
        String redirectUri = "redirect_uri=https://www.google.com/redirects/redirect1&";
        String scopeString = "common:customer.basic:read common:customer.detail:read openid profile";
        String clientId = "client-id";
        String spFullName = "sp-full-name";
        String sampleQueryParams = redirectUri + request;
        when(consentDataMock.getSpQueryParams()).thenReturn(sampleQueryParams);
        when(consentDataMock.getScopeString()).thenReturn(scopeString);
        when(consentDataMock.getClientId()).thenReturn(clientId);
        PowerMockito.stub(PowerMockito.method(CDSDataRetrievalUtil.class, "getServiceProviderFullName"))
                .toReturn(spFullName);

        String requestObjectString = CDSConsentAuthorizeTestConstants.VALID_REQUEST_OBJECT;
        OAuth2Parameters oAuth2Parameters = new OAuth2Parameters();
        oAuth2Parameters.setEssentialClaims(requestObjectString + ":" + "3600666666");

        SessionDataCache sessionDataCacheMock = mock(SessionDataCache.class);
        SessionDataCacheEntry sessionDataCacheEntry = new SessionDataCacheEntry();
        mockStatic(SessionDataCacheEntry.class);
        mockStatic(SessionDataCache.class);
        when(SessionDataCache.getInstance()).thenReturn(sessionDataCacheMock);
        when(sessionDataCacheMock.getValueFromCache(Mockito.anyObject())).thenReturn(sessionDataCacheEntry);

        sessionDataCacheEntry.setoAuth2Parameters(oAuth2Parameters);

        when(consentDataMock.isRegulatory()).thenReturn(true);
        cdsConsentRetrievalStep.execute(consentDataMock, jsonObject);
        Assert.assertTrue(!jsonObject.isEmpty());
    }

    @Test(priority = 2)
    public void testRequestUriFlowWithEncryptedReqObj() throws Exception {

        JSONObject jsonObject = new JSONObject();
        String request = "request_uri=" + "urn:ietf:params:oauth:request_uri:XKnDFSbXJWjuf0AY6gOT1EIuvdP8BQLo";
        String redirectUri = "redirect_uri=https://www.google.com/redirects/redirect1&";
        String scopeString = "common:customer.basic:read common:customer.detail:read openid profile";
        String clientId = "client-id";
        String spFullName = "sp-full-name";
        String sampleQueryParams = redirectUri + request;
        when(consentDataMock.getSpQueryParams()).thenReturn(sampleQueryParams);
        when(consentDataMock.getScopeString()).thenReturn(scopeString);
        when(consentDataMock.getClientId()).thenReturn(clientId);
        PowerMockito.stub(PowerMockito.method(CDSDataRetrievalUtil.class, "getServiceProviderFullName"))
                .toReturn(spFullName);

        String requestObjectString = CDSConsentAuthorizeTestConstants.ENCRYPTED_JWT;
        OAuth2Parameters oAuth2Parameters = new OAuth2Parameters();
        oAuth2Parameters.setEssentialClaims(requestObjectString + ":" + "3600666666");

        SessionDataCache sessionDataCacheMock = mock(SessionDataCache.class);
        SessionDataCacheEntry sessionDataCacheEntry = new SessionDataCacheEntry();
        mockStatic(SessionDataCacheEntry.class);
        mockStatic(SessionDataCache.class);
        mockStatic(PushAuthRequestValidatorUtils.class);
        when(SessionDataCache.getInstance()).thenReturn(sessionDataCacheMock);
        when(sessionDataCacheMock.getValueFromCache(Mockito.anyObject())).thenReturn(sessionDataCacheEntry);
        when(PushAuthRequestValidatorUtils.decrypt(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(CDSConsentAuthorizeTestConstants.VALID_REQUEST_OBJECT);

        sessionDataCacheEntry.setoAuth2Parameters(oAuth2Parameters);

        when(consentDataMock.isRegulatory()).thenReturn(true);
        cdsConsentRetrievalStep.execute(consentDataMock, jsonObject);
        Assert.assertTrue(!jsonObject.isEmpty());
    }

    @Test(expectedExceptions = ConsentException.class, priority = 1)
    public void testConsentRetrievalWithoutClientId() {
        JSONObject jsonObject = new JSONObject();
        String request = "request=" + CDSConsentAuthorizeTestConstants.VALID_REQUEST_OBJECT;
        String redirectUri = "redirect_uri=https://www.google.com/redirects/redirect1&";
        String scopeString = "common:customer.basic:read common:customer.detail:read openid profile";
        String sampleQueryParams = redirectUri + request;
        doReturn(sampleQueryParams).when(consentDataMock).getSpQueryParams();
        doReturn(scopeString).when(consentDataMock).getScopeString();
        when(consentDataMock.isRegulatory()).thenReturn(true);
        cdsConsentRetrievalStep.execute(consentDataMock, jsonObject);
    }

    @Test(priority = 2)
    public void testConsentRetrievalWithConsentAmendment() throws OpenBankingException {

        PowerMockito.mockStatic(CDSConsentCommonUtil.class);
        Mockito.when(CDSConsentCommonUtil.getUserIdWithTenantDomain(anyString())).
                thenReturn("user1@wso2.com@carbon.super");
        JSONObject jsonObject = new JSONObject();
        String request = "request=" + CDSConsentAuthorizeTestConstants.VALID_AMENDMENT_REQUEST_OBJECT;
        String redirectUri = "redirect_uri=https://www.google.com/redirects/redirect1&";
        String scopeString = "common:customer.basic:read common:customer.detail:read openid profile";
        String clientId = "client-id";
        String spFullName = "sp-full-name";
        String sampleQueryParams = redirectUri + request;
        final String userId = "user1@wso2.com@carbon.super";
        when(consentDataMock.getSpQueryParams()).thenReturn(sampleQueryParams);
        when(consentDataMock.getScopeString()).thenReturn(scopeString);
        when(consentDataMock.getClientId()).thenReturn(clientId);
        PowerMockito.stub(PowerMockito.method(CDSDataRetrievalUtil.class, "getServiceProviderFullName"))
                .toReturn(spFullName);
        when(consentDataMock.isRegulatory()).thenReturn(true);
        when(consentDataMock.getUserId()).thenReturn(userId);

        final String receipt = "{\"accountData\":{\"permissions\":[\"CDRREADACCOUNTSBASIC\"], " +
                "\"expirationDateTime\": \"" + LocalDateTime.now(ZoneOffset.UTC).plusDays(1L) + "Z\"}}";
        AuthorizationResource authorizationResource = new AuthorizationResource();
        authorizationResource.setUserID(userId);
        authorizationResource.setAuthorizationID("AUTH_123");
        authorizationResource.setAuthorizationStatus("authorized");
        ArrayList<AuthorizationResource> authResourceList = new ArrayList<>();
        authResourceList.add(authorizationResource);

        ConsentMappingResource consentMappingResource = new ConsentMappingResource();
        consentMappingResource.setMappingStatus("active");
        consentMappingResource.setAccountID("ACCOUNT_123");
        ArrayList<ConsentMappingResource> mappingResourceList = new ArrayList<>();
        mappingResourceList.add(consentMappingResource);

        DetailedConsentResource detailedConsentResource = new DetailedConsentResource();
        detailedConsentResource.setReceipt(receipt);
        detailedConsentResource.setAuthorizationResources(authResourceList);
        detailedConsentResource.setConsentMappingResources(mappingResourceList);
        Map<String, String> consentAttributes = new HashMap<>();
        consentAttributes.put(CDSConsentExtensionConstants.SELECTED_PROFILE_ID,
                CDSConsentExtensionConstants.INDIVIDUAL_PROFILE_ID);
        detailedConsentResource.setConsentAttributes(consentAttributes);

        ConsentCoreServiceImpl consentCoreServiceMock = mock(ConsentCoreServiceImpl.class);
        when(consentCoreServiceMock.getDetailedConsent(anyString())).thenReturn(detailedConsentResource);

        new CDSConsentRetrievalStep(consentCoreServiceMock).execute(consentDataMock, jsonObject);
        Assert.assertTrue(!jsonObject.isEmpty());
    }

    @Test(expectedExceptions = ConsentException.class, priority = 2)
    public void testConsentRetrievalWithConsentAmendmentAndExpiredConsent() throws OpenBankingException {

        JSONObject jsonObject = new JSONObject();
        String request = "request=" + CDSConsentAuthorizeTestConstants.VALID_AMENDMENT_REQUEST_OBJECT;
        String redirectUri = "redirect_uri=https://www.google.com/redirects/redirect1&";
        String scopeString = "common:customer.basic:read common:customer.detail:read openid profile";
        String clientId = "client-id";
        String spFullName = "sp-full-name";
        String sampleQueryParams = redirectUri + request;
        final String userId = "mark@gold.com";
        when(consentDataMock.getSpQueryParams()).thenReturn(sampleQueryParams);
        when(consentDataMock.getScopeString()).thenReturn(scopeString);
        when(consentDataMock.getClientId()).thenReturn(clientId);
        PowerMockito.stub(PowerMockito.method(CDSDataRetrievalUtil.class, "getServiceProviderFullName"))
                .toReturn(spFullName);
        when(consentDataMock.isRegulatory()).thenReturn(true);
        when(consentDataMock.getUserId()).thenReturn(userId);

        final String receipt = "{\"accountData\":{\"permissions\":[\"CDRREADACCOUNTSBASIC\"], " +
                "\"expirationDateTime\": \"" + LocalDateTime.now(ZoneOffset.UTC).minusDays(1L) + "Z\"}}";

        DetailedConsentResource detailedConsentResource = new DetailedConsentResource();
        detailedConsentResource.setReceipt(receipt);

        ConsentCoreServiceImpl consentCoreServiceMock = mock(ConsentCoreServiceImpl.class);
        when(consentCoreServiceMock.getDetailedConsent(anyString())).thenReturn(detailedConsentResource);

        new CDSConsentRetrievalStep(consentCoreServiceMock).execute(consentDataMock, jsonObject);
    }

    @Test(expectedExceptions = ConsentException.class, priority = 2)
    public void testConsentRetrievalWithConsentAmendmentAndInvalidConsentId() throws OpenBankingException {

        JSONObject jsonObject = new JSONObject();
        String request = "request=" + CDSConsentAuthorizeTestConstants.VALID_AMENDMENT_REQUEST_OBJECT;
        String redirectUri = "redirect_uri=https://www.google.com/redirects/redirect1&";
        String scopeString = "common:customer.basic:read common:customer.detail:read openid profile";
        String clientId = "client-id";
        String spFullName = "sp-full-name";
        String sampleQueryParams = redirectUri + request;
        final String userId = "mark@gold.com";
        when(consentDataMock.getSpQueryParams()).thenReturn(sampleQueryParams);
        when(consentDataMock.getScopeString()).thenReturn(scopeString);
        when(consentDataMock.getClientId()).thenReturn(clientId);
        PowerMockito.stub(PowerMockito.method(CDSDataRetrievalUtil.class, "getServiceProviderFullName"))
                .toReturn(spFullName);
        when(consentDataMock.isRegulatory()).thenReturn(true);
        when(consentDataMock.getUserId()).thenReturn(userId);

        ConsentCoreServiceImpl consentCoreServiceMock = mock(ConsentCoreServiceImpl.class);
        when(consentCoreServiceMock.getDetailedConsent(anyString())).thenReturn(null);

        new CDSConsentRetrievalStep(consentCoreServiceMock).execute(consentDataMock, jsonObject);
    }
}

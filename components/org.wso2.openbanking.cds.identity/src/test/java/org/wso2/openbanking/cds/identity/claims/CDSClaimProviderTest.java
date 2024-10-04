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

package org.wso2.openbanking.cds.identity.claims;

import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.oauth.cache.SessionDataCache;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheEntry;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeReqDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeRespDTO;
import org.wso2.carbon.identity.oauth2.model.OAuth2Parameters;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.openbanking.cds.identity.claims.utils.CDSClaimProviderUtil;

import java.util.Map;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Test class for CDS Claim Provider.
 */
@PrepareForTest({SessionDataCache.class, CDSClaimProviderUtil.class})
@PowerMockIgnore("jdk.internal.reflect.*")
public class CDSClaimProviderTest extends PowerMockTestCase {

    private CDSClaimProvider cdsClaimProvider;

    @BeforeClass
    public void beforeClass() {

        cdsClaimProvider = new CDSClaimProvider();
    }

    @Test
    public void getAdditionalClaimsAuthFlowSuccess() throws Exception {

        OAuth2AuthorizeReqDTO oAuth2AuthorizeReqDTO = new OAuth2AuthorizeReqDTO();
        oAuth2AuthorizeReqDTO.setSessionDataKey("DummySessionDataKey");
        OAuthAuthzReqMessageContext oAuthAuthzReqMessageContext =
                new OAuthAuthzReqMessageContext(oAuth2AuthorizeReqDTO);
        oAuthAuthzReqMessageContext.setCodeIssuedTime(1672782000); //Set the value in milliseconds
        oAuthAuthzReqMessageContext.setAccessTokenIssuedTime(1672432);
        oAuthAuthzReqMessageContext.setRefreshTokenvalidityPeriod(7776000);
        OAuth2AuthorizeRespDTO oAuth2AuthorizeRespDTOMock = mock(OAuth2AuthorizeRespDTO.class);

        mockStatic(SessionDataCache.class);
        SessionDataCache sessionDataCacheMock = mock(SessionDataCache.class);
        when(SessionDataCache.getInstance()).thenReturn(sessionDataCacheMock);
        SessionDataCacheEntry sessionDataCacheEntryMock = mock(SessionDataCacheEntry.class);
        when(sessionDataCacheMock.getValueFromCache(Mockito.anyObject())).thenReturn(sessionDataCacheEntryMock);
        OAuth2Parameters oAuth2ParametersMock = mock(OAuth2Parameters.class);
        when(sessionDataCacheEntryMock.getoAuth2Parameters()).thenReturn(oAuth2ParametersMock);
        when(oAuth2ParametersMock.getState()).thenReturn("DummyStateValue");

        mockStatic(CDSClaimProviderUtil.class);
        when(CDSClaimProviderUtil.getHashValue(Mockito.anyString(), Mockito.anyString())).thenReturn("DummyHashValue");
        when(CDSClaimProviderUtil.getEpochDateTime(Mockito.anyLong())).thenReturn((long) 7776000);

        Map<String, Object> results = cdsClaimProvider
                .getAdditionalClaims(oAuthAuthzReqMessageContext, oAuth2AuthorizeRespDTOMock);
        Assert.assertEquals(results.get("s_hash"), "DummyHashValue");
        Assert.assertEquals(results.get("nbf"), (long) 1672432);
        Assert.assertEquals(results.get("auth_time"), (long) 1672782);
        Assert.assertEquals(results.get("sharing_expires_at"), (long) 7776000);
        Assert.assertEquals(results.get("refresh_token_expires_at"), (long) 7776000);
    }

    @Test
    public void getAdditionalClaimsTokenFlowSuccess() throws Exception {

        OAuth2AccessTokenReqDTO oAuth2AccessTokenReqDTO = mock(OAuth2AccessTokenReqDTO.class);
        OAuthTokenReqMessageContext oAuthTokenReqMessageContext =
                new OAuthTokenReqMessageContext(oAuth2AccessTokenReqDTO);
        oAuthTokenReqMessageContext.setRefreshTokenvalidityPeriod(7776000);
        OAuth2AccessTokenRespDTO oAuth2AccessTokenRespDTO = new OAuth2AccessTokenRespDTO();
        oAuth2AccessTokenRespDTO.addParameter("cdr_arrangement_id", "DummyConsentId");

        mockStatic(CDSClaimProviderUtil.class);
        when(CDSClaimProviderUtil.getEpochDateTime(Mockito.anyLong())).thenReturn((long) 7776000);

        Map<String, Object> results = cdsClaimProvider
                .getAdditionalClaims(oAuthTokenReqMessageContext, oAuth2AccessTokenRespDTO);

        Assert.assertEquals(results.get("cdr_arrangement_id"), "DummyConsentId");
        Assert.assertEquals(results.get("sharing_expires_at"), (long) 7776000);
        Assert.assertEquals(results.get("refresh_token_expires_at"), (long) 7776000);
    }
}

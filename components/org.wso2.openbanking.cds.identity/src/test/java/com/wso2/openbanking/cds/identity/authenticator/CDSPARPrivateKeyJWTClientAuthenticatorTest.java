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

package org.wso2.openbanking.cds.identity.authenticator;

import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth2.bean.OAuthClientAuthnContext;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertTrue;

/**
 * Test class for CDS PAR Private Key JWT Authenticator.
 */
@PrepareForTest({OAuth2Util.class, OAuthServerConfiguration.class, CDSPARPrivateKeyJWTClientAuthenticator.class})
@PowerMockIgnore("jdk.internal.reflect.*")
public class CDSPARPrivateKeyJWTClientAuthenticatorTest extends PowerMockTestCase {

    @BeforeClass
    public void beforeClass() {
    }

    @BeforeMethod
    public void beforeMethod() {
    }

    @Test(description = "Test whether can authenticate is engaged for par request")
    public void canAuthenticateTest() throws Exception {

        OAuthClientAuthnContext clientAuthnContext = new OAuthClientAuthnContext();
        OAuthServerConfiguration oAuthServerConfigurationMock = Mockito.mock(OAuthServerConfiguration.class);
        PowerMockito.mockStatic(OAuthServerConfiguration.class);
        when(OAuthServerConfiguration.getInstance()).thenReturn(oAuthServerConfigurationMock);
        MockHttpServletRequest request = new MockHttpServletRequest();
        PowerMockito.mockStatic(OAuth2Util.class);
        CDSPARPrivateKeyJWTClientAuthenticator authenticator =
                PowerMockito.spy(new CDSPARPrivateKeyJWTClientAuthenticator());
        Map<String, List> bodyParams = new HashMap<>();
        request.setRequestURI("baseUri/par");
        PowerMockito.doReturn(true).when(authenticator, "canSuperAuthenticate",
                any(MockHttpServletRequest.class), any(Map.class), any(OAuthClientAuthnContext.class));
        boolean response = authenticator.canAuthenticate(request, bodyParams, clientAuthnContext);
        assertTrue(response);
    }
}

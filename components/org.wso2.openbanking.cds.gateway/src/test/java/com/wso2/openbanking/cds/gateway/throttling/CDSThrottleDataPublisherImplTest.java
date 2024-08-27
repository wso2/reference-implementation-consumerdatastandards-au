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

package org.wso2.openbanking.cds.gateway.throttling;

import com.wso2.openbanking.accelerator.gateway.internal.GatewayDataHolder;
import com.wso2.openbanking.accelerator.gateway.throttling.OBThrottlingExtensionImpl;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.common.gateway.dto.ExtensionResponseDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.MsgInfoDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.RequestContextDTO;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;
import org.wso2.openbanking.cds.common.utils.CDSCommonUtils;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.doReturn;

/**
 * Test class for CDSThrottleDataPublisherImpl.
 */
@PrepareForTest({OpenBankingCDSConfigParser.class, CDSCommonUtils.class})
@PowerMockIgnore("jdk.internal.reflect.*")
public class CDSThrottleDataPublisherImplTest extends PowerMockTestCase {

    private static final String X_FAPI_CUSTOMER_IP_ADDRESS = "x-fapi-customer-ip-address";
    private static final String CUSTOMER_STATUS = "customerStatus";
    private static final String CUSTOMER_PRESENT_STATUS = "customerPresent";
    private static final String UNATTENDED_STATUS = "unattended";
    private static final String AUTHORIZATION_STATUS = "authorizationStatus";
    private static final String SECURED_STATUS = "secured";
    private static final String PUBLIC_STATUS = "public";
    private static final String NULL_STRING = "null";
    private static final String AUTHORIZATION = "Authorization";
    private static final String AUTHORIZATION_HEADER = "authorizationHeader";
    private static final String ENCRYPTED_TOKEN = "encrypted-token";
    private static final String SAMPLE_AUTH_HEADER = "Bearer some-string";
    private static final String SAMPLE_IP_ADDRESS = "192.168.1.1";

    OpenBankingCDSConfigParser openBankingCDSConfigParserMock;
    CDSCommonUtils cdsCommonUtilsMock;
    CDSThrottleDataPublisherImpl cdsThrottleDataPublisher;
    OBThrottlingExtensionImpl obThrottlingExtension;
    RequestContextDTO requestContextDTO;
    MsgInfoDTO msgInfoDTO;

    @BeforeClass
    public void beforeClass() {

        cdsThrottleDataPublisher = new CDSThrottleDataPublisherImpl();
        GatewayDataHolder.getInstance().setThrottleDataPublisher(cdsThrottleDataPublisher);
        obThrottlingExtension = new OBThrottlingExtensionImpl();
        requestContextDTO = Mockito.mock(RequestContextDTO.class);
        msgInfoDTO = Mockito.mock(MsgInfoDTO.class);

    }

    @Test
    public void testGetCustomPropertiesWithAuthHeader() {

        openBankingCDSConfigParserMock = PowerMockito.mock(OpenBankingCDSConfigParser.class);
        cdsCommonUtilsMock = PowerMockito.mock(CDSCommonUtils.class);
        PowerMockito.mockStatic(OpenBankingCDSConfigParser.class);
        PowerMockito.mockStatic(CDSCommonUtils.class);
        PowerMockito.when(OpenBankingCDSConfigParser.getInstance()).thenReturn(openBankingCDSConfigParserMock);
        PowerMockito.when(CDSCommonUtils.encryptAccessToken(Mockito.anyString())).thenReturn(ENCRYPTED_TOKEN);
        doReturn(true).when(openBankingCDSConfigParserMock).isTokenEncryptionEnabled();
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put(AUTHORIZATION, SAMPLE_AUTH_HEADER);
        Mockito.when(requestContextDTO.getMsgInfo()).thenReturn(msgInfoDTO);
        Mockito.when(msgInfoDTO.getHeaders()).thenReturn(headerMap);
        ExtensionResponseDTO extensionResponseDTO = obThrottlingExtension.preProcessRequest(requestContextDTO);

        Assert.assertEquals(extensionResponseDTO.getCustomProperty().get(AUTHORIZATION_HEADER), ENCRYPTED_TOKEN);
        Assert.assertEquals(extensionResponseDTO.getCustomProperty().get(AUTHORIZATION_STATUS), SECURED_STATUS);
    }

    @Test
    public void testGetCustomPropertiesWithCustomerIp() {

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put(X_FAPI_CUSTOMER_IP_ADDRESS, SAMPLE_IP_ADDRESS);
        Mockito.when(requestContextDTO.getMsgInfo()).thenReturn(msgInfoDTO);
        Mockito.when(msgInfoDTO.getHeaders()).thenReturn(headerMap);
        ExtensionResponseDTO extensionResponseDTO = obThrottlingExtension.preProcessRequest(requestContextDTO);

        Assert.assertEquals(extensionResponseDTO.getCustomProperty().get(X_FAPI_CUSTOMER_IP_ADDRESS),
                SAMPLE_IP_ADDRESS);
        Assert.assertEquals(extensionResponseDTO.getCustomProperty().get(CUSTOMER_STATUS), CUSTOMER_PRESENT_STATUS);
    }

    @Test
    public void testGetCustomPropertiesWithoutAuthHeaderOrIP() {

        Map<String, String> headerMap = new HashMap<>();
        Mockito.when(requestContextDTO.getMsgInfo()).thenReturn(msgInfoDTO);
        Mockito.when(msgInfoDTO.getHeaders()).thenReturn(headerMap);
        ExtensionResponseDTO extensionResponseDTO = obThrottlingExtension.preProcessRequest(requestContextDTO);

        Assert.assertEquals(extensionResponseDTO.getCustomProperty().get(AUTHORIZATION_HEADER), NULL_STRING);
        Assert.assertEquals(extensionResponseDTO.getCustomProperty().get(AUTHORIZATION_STATUS), PUBLIC_STATUS);
        Assert.assertEquals(extensionResponseDTO.getCustomProperty().get(X_FAPI_CUSTOMER_IP_ADDRESS), NULL_STRING);
        Assert.assertEquals(extensionResponseDTO.getCustomProperty().get(CUSTOMER_STATUS), UNATTENDED_STATUS);
    }

}

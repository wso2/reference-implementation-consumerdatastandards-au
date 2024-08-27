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
package org.wso2.openbanking.cds.common.utils;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;

import static org.mockito.Mockito.doReturn;

/**
 * Test class for CDSCommonUtils.
 */
@PrepareForTest({OpenBankingCDSConfigParser.class})
@PowerMockIgnore({"javax.crypto.*", "jdk.internal.reflect.*"})
public class CDSCommonUtilsTest extends PowerMockTestCase {

    private static final String STRING_TO_ENCRYPT = "sample-access-token";
    private static final String ENCRYPTED_STRING = "7b7e65d4a069ec690bf45b0ecded4ae6376dee2c6193c5284823314563011536";

    OpenBankingCDSConfigParser openBankingCDSConfigParserMock;
    String encryptedToken;

    @Test
    public void testEncryptAccessToken() {

        openBankingCDSConfigParserMock = PowerMockito.mock(OpenBankingCDSConfigParser.class);
        PowerMockito.mockStatic(OpenBankingCDSConfigParser.class);
        PowerMockito.when(OpenBankingCDSConfigParser.getInstance()).thenReturn(openBankingCDSConfigParserMock);
        doReturn("wso2").when(openBankingCDSConfigParserMock).getTokenEncryptionSecretKey();
        encryptedToken = CDSCommonUtils.encryptAccessToken(STRING_TO_ENCRYPT);

        Assert.assertEquals(encryptedToken, ENCRYPTED_STRING);
    }

    @Test
    public void testValidRequestUriKey() {
        String expectedRequestUriKey = "abc123";
        String actualRequestUriKey = CDSCommonUtils
                .getRequestUriKey("abc:123:def:request_uri:" + expectedRequestUriKey);

        Assert.assertEquals(actualRequestUriKey, expectedRequestUriKey);
    }

    @Test
    public void testNullRequestUriKey() {
        String expectedRequestUriKey = null;
        String actualRequestUriKey = CDSCommonUtils.getRequestUriKey(null);

        Assert.assertEquals(actualRequestUriKey, expectedRequestUriKey);
    }

}

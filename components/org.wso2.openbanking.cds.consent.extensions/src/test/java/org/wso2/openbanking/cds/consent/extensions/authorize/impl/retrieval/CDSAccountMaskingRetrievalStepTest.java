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

import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;
import org.wso2.openbanking.cds.consent.extensions.common.CDSConsentExtensionConstants;

import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Test class for CDSAccountMaskingRetrievalStep.
 */
@PrepareForTest({OpenBankingCDSConfigParser.class})
@PowerMockIgnore({"jdk.internal.reflect.*"})
public class CDSAccountMaskingRetrievalStepTest extends PowerMockTestCase {

    @Mock
    OpenBankingCDSConfigParser openBankingCDSConfigParserMock;
    private static CDSAccountMaskingRetrievalStep cdsAccountMaskingRetrievalStep;
    private JSONObject accountJson;
    private String testJson;
    private ConsentData consentDataMock;

    @BeforeClass
    public void init() throws ParseException {

        cdsAccountMaskingRetrievalStep = new CDSAccountMaskingRetrievalStep();
        testJson = "{\n" +
                "    \"accounts\": [\n" +
                "        {\n" +
                "            \"accountId\": \"30080012343456\",\n" +
                "            \"authorizationMethod\": \"single\",\n" +
                "            \"isSecondaryAccount\": false,\n" +
                "            \"isPreSelectedAccount\": \"true\",\n" +
                "            \"displayName\": \"account_1\",\n" +
                "            \"nickName\": \"not-working\",\n" +
                "            \"customerAccountType\": \"Individual\",\n" +
                "            \"isEligible\": true,\n" +
                "            \"jointAccountConsentElectionStatus\": false,\n" +
                "            \"type\": \"TRANS_AND_SAVINGS_ACCOUNTS\",\n" +
                "            \"isJointAccount\": false\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        accountJson = (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE).parse(testJson);
        consentDataMock = PowerMockito.mock(ConsentData.class);
    }

    @Test
    public void testGetDisplayableAccountNumber() {

        // 2 character account number (highly unlikely)
        String displayableAccountNumber1 = cdsAccountMaskingRetrievalStep
                .getDisplayableAccountNumber("12");
        Assert.assertEquals(displayableAccountNumber1, "*2");

        // 3 character account number (highly unlikely)
        String displayableAccountNumber2 = cdsAccountMaskingRetrievalStep
                .getDisplayableAccountNumber("123");
        Assert.assertEquals(displayableAccountNumber2, "**3");

        // 4 character account number (highly unlikely)
        String displayableAccountNumber3 = cdsAccountMaskingRetrievalStep
                .getDisplayableAccountNumber("1234");
        Assert.assertEquals(displayableAccountNumber3, "**34");

        // 5 character account number
        String displayableAccountNumber4 = cdsAccountMaskingRetrievalStep
                .getDisplayableAccountNumber("12345");
        Assert.assertEquals(displayableAccountNumber4, "*2345");

        // An account ID with 14 characters
        String displayableAccountNumber = cdsAccountMaskingRetrievalStep
                .getDisplayableAccountNumber("12345678901234");
        Assert.assertEquals(displayableAccountNumber, "**********1234");
    }

    @Test
    public void testAccountMaskingEnabled() {


        openBankingCDSConfigParserMock = mock(OpenBankingCDSConfigParser.class);
        PowerMockito.mockStatic(OpenBankingCDSConfigParser.class);
        PowerMockito.when(OpenBankingCDSConfigParser.getInstance()).thenReturn(openBankingCDSConfigParserMock);
        PowerMockito.when(openBankingCDSConfigParserMock.isAccountMaskingEnabled()).thenReturn(true);

        JSONObject testAccountsJson = new JSONObject();
        testAccountsJson.put(CDSConsentExtensionConstants.ACCOUNTS, testJson);
        when(consentDataMock.isRegulatory()).thenReturn(true);
        cdsAccountMaskingRetrievalStep.execute(consentDataMock, accountJson);
        Assert.assertNotNull(accountJson.get(CDSConsentExtensionConstants.ACCOUNTS));
        JSONArray returnedAccountsJsonArray = (JSONArray) accountJson.get(CDSConsentExtensionConstants.ACCOUNTS);
        JSONObject returnedAccountJson = (JSONObject) returnedAccountsJsonArray.get(0);
        Assert.assertNotNull(returnedAccountJson.get(CDSConsentExtensionConstants.ACCOUNT_ID_DISPLAYABLE));
        Assert.assertEquals("**********3456", returnedAccountJson
                .get(CDSConsentExtensionConstants.ACCOUNT_ID_DISPLAYABLE));
    }

    @Test
    public void testAccountMaskingDisabled() {

        openBankingCDSConfigParserMock = mock(OpenBankingCDSConfigParser.class);
        PowerMockito.mockStatic(OpenBankingCDSConfigParser.class);
        PowerMockito.when(OpenBankingCDSConfigParser.getInstance()).thenReturn(openBankingCDSConfigParserMock);
        PowerMockito.when(openBankingCDSConfigParserMock.isAccountMaskingEnabled()).thenReturn(false);

        JSONObject testAccountsJson = new JSONObject();
        testAccountsJson.put(CDSConsentExtensionConstants.ACCOUNTS, testJson);
        cdsAccountMaskingRetrievalStep.execute(mock(ConsentData.class), accountJson);
        Assert.assertNotNull(accountJson.get(CDSConsentExtensionConstants.ACCOUNTS));
        JSONArray returnedAccountsJsonArray = (JSONArray) accountJson.get(CDSConsentExtensionConstants.ACCOUNTS);
        JSONObject returnedAccountJson = (JSONObject) returnedAccountsJsonArray.get(0);
        Assert.assertNull(returnedAccountJson.get(CDSConsentExtensionConstants.ACCOUNT_ID_DISPLAYABLE));
    }
}

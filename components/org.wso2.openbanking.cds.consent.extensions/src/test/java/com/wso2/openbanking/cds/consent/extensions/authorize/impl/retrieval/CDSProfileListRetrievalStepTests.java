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

import com.wso2.openbanking.accelerator.account.metadata.service.service.AccountMetadataServiceImpl;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
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
import org.wso2.openbanking.cds.consent.extensions.authorize.utils.CDSConsentCommonUtil;
import org.wso2.openbanking.cds.consent.extensions.common.CDSConsentExtensionConstants;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Test class for CDS Account Retrieval.
 */
@PrepareForTest({OpenBankingCDSConfigParser.class, CDSConsentCommonUtil.class, AccountMetadataServiceImpl.class})
@PowerMockIgnore({"com.wso2.openbanking.accelerator.consent.extensions.common.*", "jdk.internal.reflect.*"})
public class CDSProfileListRetrievalStepTests extends PowerMockTestCase {

    private static final String TEST_ACCOUNT_DATA_JSON = "{\n" +
            "  \"accounts\": [\n" +
            "    {\n" +
            "      \"accountId\": \"30080012343456\",\n" +
            "      \"authorizationMethod\": \"single\",\n" +
            "      \"displayName\": \"account_1\",\n" +
            "      \"nickName\": \"not-working\",\n" +
            "      \"customerAccountType\": \"Individual\",\n" +
            "      \"isEligible\": true,\n" +
            "      \"jointAccountConsentElectionStatus\": false,\n" +
            "      \"type\": \"TRANS_AND_SAVINGS_ACCOUNTS\",\n" +
            "      \"isJointAccount\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"profileName\": \"Organization A\",\n" +
            "      \"authorizationMethod\": \"single\",\n" +
            "      \"isSecondaryAccount\": false,\n" +
            "      \"businessAccountInfo\": {\n" +
            "        \"AccountOwners\": [\n" +
            "          {\n" +
            "            \"meta\": {},\n" +
            "            \"memberId\": \"user1@wso2.com@carbon.super\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"meta\": {},\n" +
            "            \"memberId\": \"user2@wso2.com@carbon.super\"\n" +
            "          }\n" +
            "        ],\n" +
            "        \"NominatedRepresentatives\": [\n" +
            "          {\n" +
            "            \"meta\": {},\n" +
            "            \"memberId\": \"nominatedUser1@wso2.com@carbon.super\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"meta\": {},\n" +
            "            \"memberId\": \"nominatedUser2@wso2.com@carbon.super\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"meta\": {},\n" +
            "            \"memberId\": \"admin@wso2.com@carbon.super\"\n" +
            "          }\n" +
            "        ]\n" +
            "      },\n" +
            "      \"displayName\": \"business_account_1\",\n" +
            "      \"nickName\": \"not-working\",\n" +
            "      \"customerAccountType\": \"Business\",\n" +
            "      \"jointAccountConsentElectionStatus\": false,\n" +
            "      \"type\": \"TRANS_AND_SAVINGS_ACCOUNTS\",\n" +
            "      \"accountId\": \"143-000-B1234\",\n" +
            "      \"profileId\": \"00001\",\n" +
            "      \"isEligible\": true,\n" +
            "      \"isJointAccount\": false\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    @Mock
    OpenBankingCDSConfigParser openBankingCDSConfigParserMock;

    @Mock
    AccountMetadataServiceImpl accountMetadataServiceMock;

    @Mock
    ConsentData consentDataMock;
    private static CDSProfileListRetrievalStep cdsProfileListRetrievalStep;

    @BeforeClass
    public void initClass() {
        openBankingCDSConfigParserMock = mock(OpenBankingCDSConfigParser.class);
        PowerMockito.mockStatic(OpenBankingCDSConfigParser.class);
        PowerMockito.when(OpenBankingCDSConfigParser.getInstance()).thenReturn(openBankingCDSConfigParserMock);

        this.accountMetadataServiceMock = mock(AccountMetadataServiceImpl.class);
        mockStatic(AccountMetadataServiceImpl.class);
        when(AccountMetadataServiceImpl.getInstance()).thenReturn(accountMetadataServiceMock);

        cdsProfileListRetrievalStep = new CDSProfileListRetrievalStep();
        consentDataMock = mock(ConsentData.class);
    }

    @Test
    public void testProfileDataRetrievalSuccessScenario() throws ParseException, OpenBankingException {

        openBankingCDSConfigParserMock = mock(OpenBankingCDSConfigParser.class);
        PowerMockito.mockStatic(OpenBankingCDSConfigParser.class);
        PowerMockito.when(OpenBankingCDSConfigParser.getInstance()).thenReturn(openBankingCDSConfigParserMock);
        doReturn(true).when(openBankingCDSConfigParserMock).
                isBNRPrioritizeSharableAccountsResponseEnabled();
        doReturn("profile_selection").when(openBankingCDSConfigParserMock).
                getBNRCustomerTypeSelectionMethod();
        doReturn("AUTHORIZE").when(accountMetadataServiceMock).getAccountMetadataByKey(anyString(),
                anyString(), anyString());

        PowerMockito.mockStatic(OpenBankingCDSConfigParser.class);
        PowerMockito.when(OpenBankingCDSConfigParser.getInstance()).thenReturn(openBankingCDSConfigParserMock);

        PowerMockito.mockStatic(CDSConsentCommonUtil.class);
        when(CDSConsentCommonUtil.getUserIdWithTenantDomain(anyString())).thenReturn("user1@wso2.com@carbon.super");

        JSONObject jsonObject = (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE).parse(TEST_ACCOUNT_DATA_JSON);
        doReturn(CDSConsentExtensionConstants.ACCOUNTS).when(consentDataMock).getType();

        doReturn(true).when(consentDataMock).isRegulatory();
        doReturn("user1@wso2.com").when(consentDataMock).getUserId();

        cdsProfileListRetrievalStep.execute(consentDataMock, jsonObject);

        Assert.assertNotNull(jsonObject.get(CDSConsentExtensionConstants.CUSTOMER_PROFILES_ATTRIBUTE));
    }

    @Test
    public void testProfileDataRetrievalWithEmptyAccountData() throws ParseException, OpenBankingException {
        PowerMockito.mockStatic(CDSConsentCommonUtil.class);
        PowerMockito.mockStatic(OpenBankingCDSConfigParser.class);
        PowerMockito.when(OpenBankingCDSConfigParser.getInstance()).thenReturn(openBankingCDSConfigParserMock);
        JSONObject jsonObject = new JSONObject();
        doReturn(CDSConsentExtensionConstants.ACCOUNTS).when(consentDataMock).getType();
        doReturn(true).when(consentDataMock).isRegulatory();
        doReturn("user1@wso2.com").when(consentDataMock).getUserId();

        cdsProfileListRetrievalStep.execute(consentDataMock, jsonObject);

        // Assert that no customer profiles are returned when there's no account data
        Assert.assertNull(jsonObject.get(CDSConsentExtensionConstants.CUSTOMER_PROFILES_ATTRIBUTE));
    }
}

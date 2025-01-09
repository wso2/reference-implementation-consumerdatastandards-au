/**
 * Copyright (c) 2024-2025, WSO2 LLC. (https://www.wso2.com).
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
package org.wso2.openbanking.cds.consent.extensions.authorize.impl.persist;

import com.wso2.openbanking.accelerator.account.metadata.service.service.AccountMetadataServiceImpl;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistData;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;
import org.wso2.openbanking.cds.consent.extensions.common.CDSConsentExtensionConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Test class for CDSJointAccountConsentPersistenceStep.
 */
@PrepareForTest({OpenBankingCDSConfigParser.class, AccountMetadataServiceImpl.class})
@PowerMockIgnore("jdk.internal.reflect.*")
public class CDSBusinessAccountConsentPersistenceStepTest extends PowerMockTestCase {
    private static final String TEST_ACCOUNT_DATA_JSON = "[" +
            "        {" +
            "            \"" + CDSConsentExtensionConstants.ACCOUNT_ID + "\": \"regular-account-id\"," +
            "            \"" + CDSConsentExtensionConstants.DISPLAY_NAME + "\": \"account_2\"," +
            "            \"authorizationMethod\": \"single\"," +
            "            \"nickName\": \"not-working\"," +
            "            \"customerAccountType\": \"Individual\"," +
            "            \"type\": \"TRANS_AND_SAVINGS_ACCOUNTS\"," +
            "            \"isEligible\": true," +
            "            \"isJointAccount\": false," +
            "            \"jointAccountConsentElectionStatus\": false" +
            "        }," +
            "        {" +
            "          \"" + CDSConsentExtensionConstants.ACCOUNT_ID + "\": \"business-account-id\"," +
            "          \"" + CDSConsentExtensionConstants.DISPLAY_NAME + "\": \"business-account_1\"," +
            "          \"authorizationMethod\": \"single\"," +
            "          \"nickName\": \"not-working\"," +
            "          \"customerAccountType\": \"Business\"," +
            "          \"profileName\": \"Organization A\"," +
            "          \"profileId\": \"00001\"," +
            "          \"type\": \"TRANS_AND_SAVINGS_ACCOUNTS\"," +
            "          \"isEligible\": true," +
            "          \"isJointAccount\": false," +
            "          \"jointAccountConsentElectionStatus\": false," +
            "          \"isSecondaryAccount\": false," +
            "          \"businessAccountInfo\": {" +
            "              \"AccountOwners\": [" +
            "                  {" +
            "                  \"memberId\":\"user1@wso2.com@carbon.super\"," +
            "                  \"meta\":{}" +
            "                  }," +
            "                  {" +
            "                  \"memberId\":\"user2@wso2.com@carbon.super\"," +
            "                  \"meta\":{}" +
            "                 }" +
            "             ]," +
            "             \"NominatedRepresentatives\":[" +
            "                 {" +
            "                 \"memberId\":\"nominatedUser1@wso2.com@carbon.super\"," +
            "                 \"meta\":{}" +
            "                 }," +
            "                 {" +
            "                 \"memberId\":\"nominatedUser2@wso2.com@carbon.super\"," +
            "                 \"meta\":{}" +
            "                 }," +
            "                 {" +
            "                 \"memberId\":\"nominatedUser3@wso2.com@carbon.super\"," +
            "                 \"meta\":{}" +
            "                 }" +
            "              ]" +
            "           }" +
            "        }," +
            "        {" +
            "            \"meta\": {}" +
            "        }" +
            "    ]";
    private ConsentPersistData consentPersistDataMock;
    private AccountMetadataServiceImpl accountMetadataServiceMock;

    @BeforeClass
    public void setUp() throws Exception {
        this.consentPersistDataMock = mock(ConsentPersistData.class);
        this.accountMetadataServiceMock = mock(AccountMetadataServiceImpl.class);
        mockStatic(AccountMetadataServiceImpl.class);
        when(AccountMetadataServiceImpl.getInstance()).thenReturn(accountMetadataServiceMock);

        JSONArray accounts = new JSONArray();
        accounts.add("regular-account-id");
        accounts.add("business-account-id");

        JSONObject payload = new JSONObject();
        payload.put(CDSConsentExtensionConstants.ACCOUNT_IDS, accounts);

        Map<String, Object> consentDataMap = new HashMap<>();
        consentDataMap.put(CDSConsentExtensionConstants.ACCOUNTS,
                new JSONParser(JSONParser.MODE_PERMISSIVE).parse(TEST_ACCOUNT_DATA_JSON));
        ConsentData consentDataMock = mock(ConsentData.class);

        when(consentDataMock.getUserId()).thenReturn("test-business-account-user-id");
        when(consentDataMock.getMetaDataMap()).thenReturn(consentDataMap);

        when(this.consentPersistDataMock.getConsentData()).thenReturn(consentDataMock);
        when(this.consentPersistDataMock.getPayload()).thenReturn(payload);
        when(this.consentPersistDataMock.getApproval()).thenReturn(true);

        Map<String, Object> configs = new HashMap<>();
        configs.put("PrioritizeSharableAccountsResponse", true);
        configs.put("ValidateAccountsOnRetrieval", true);

        OpenBankingCDSConfigParser openBankingCDSConfigParserMock = PowerMockito.mock(OpenBankingCDSConfigParser.class);
        mockStatic(OpenBankingCDSConfigParser.class);
        PowerMockito.when(OpenBankingCDSConfigParser.getInstance()).thenReturn(openBankingCDSConfigParserMock);
        PowerMockito.when(openBankingCDSConfigParserMock.getConfiguration()).thenReturn(configs);
    }

    @Test
    public void testExecute() throws Exception {
        doReturn(1).when(accountMetadataServiceMock).addOrUpdateAccountMetadata(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyMap());
        CDSBusinessAccountConsentPersistenceStep businessAccountConsentPersistenceStep =
                new CDSBusinessAccountConsentPersistenceStep();
        businessAccountConsentPersistenceStep.execute(this.consentPersistDataMock);

        ArgumentCaptor<Object> businessAccountIdWithUsersCaptor = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<Object> usersWithMultipleBusinessAccountsCaptor = ArgumentCaptor.forClass(Object.class);

        verify(consentPersistDataMock).addMetadata(eq(CDSConsentExtensionConstants.
                NON_PRIMARY_ACCOUNT_ID_AGAINST_USERS_MAP), businessAccountIdWithUsersCaptor.capture());
        Map<String, Map<String, String>> jointAccountIdWithUsersCaptorValue =
                (Map<String, Map<String, String>>) businessAccountIdWithUsersCaptor.getValue();

        verify(consentPersistDataMock).addMetadata(eq(CDSConsentExtensionConstants.
                USER_ID_AGAINST_NON_PRIMARY_ACCOUNTS_MAP), usersWithMultipleBusinessAccountsCaptor.capture());
        Map<String, Map<String, List<String>>> usersWithMultipleJointAccountsCaptorValue =
                (Map<String, Map<String, List<String>>>) usersWithMultipleBusinessAccountsCaptor.getValue();

        assertTrue(jointAccountIdWithUsersCaptorValue.containsKey("business-account-id"));
        assertTrue(jointAccountIdWithUsersCaptorValue.get("business-account-id").
                containsKey("nominatedUser1@wso2.com@carbon.super"));
        assertTrue(jointAccountIdWithUsersCaptorValue.get("business-account-id").
                containsKey("user1@wso2.com@carbon.super"));

        assertTrue(usersWithMultipleJointAccountsCaptorValue.containsKey("nominatedUser2@wso2.com@carbon.super"));
        assertTrue(usersWithMultipleJointAccountsCaptorValue.containsKey("user2@wso2.com@carbon.super"));
        assertTrue(usersWithMultipleJointAccountsCaptorValue.get("nominatedUser3@wso2.com@carbon.super")
                .get(CDSConsentExtensionConstants.NOMINATED_REPRESENTATIVE).contains("business-account-id"));
        assertTrue(usersWithMultipleJointAccountsCaptorValue.get("user1@wso2.com@carbon.super")
                .get(CDSConsentExtensionConstants.BUSINESS_ACCOUNT_OWNER).contains("business-account-id"));
    }
}

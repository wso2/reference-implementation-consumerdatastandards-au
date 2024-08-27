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

package org.wso2.openbanking.cds.consent.extensions.authorize.impl.persist;

import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistData;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.openbanking.cds.consent.extensions.common.CDSConsentExtensionConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test class for CDSJointAccountConsentPersistenceStep.
 */
public class CDSJointAccountConsentPersistenceStepTest {
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
            "            \"" + CDSConsentExtensionConstants.ACCOUNT_ID + "\": \"joint-account-id\"," +
            "            \"" + CDSConsentExtensionConstants.DISPLAY_NAME + "\": \"joint_account_1\"," +
            "            \"authorizationMethod\": \"single\"," +
            "            \"nickName\": \"joint-account-1\"," +
            "            \"customerAccountType\": \"Individual\"," +
            "            \"type\": \"TRANS_AND_SAVINGS_ACCOUNTS\"," +
            "            \"isEligible\": true," +
            "            \"isJointAccount\": true," +
            "            \"jointAccountConsentElectionStatus\": \"ELECTED\"," +
            "            \"jointAccountinfo\": {" +
            "                \"linkedMember\": [" +
            "                    {" +
            "                        \"memberId\": \"john@wso2.com@carbon.super\"," +
            "                        \"meta\": {}" +
            "                    }," +
            "                    {" +
            "                        \"memberId\": \"amy@wso2.com@carbon.super\"," +
            "                        \"meta\": {}" +
            "                    }" +
            "                ]" +
            "            }," +
            "            \"meta\": {}" +
            "        }" +
            "    ]";
    private ConsentPersistData consentPersistDataMock;

    @BeforeClass
    public void setUp() throws ParseException {
        this.consentPersistDataMock = mock(ConsentPersistData.class);
        JSONArray accounts = new JSONArray();
        accounts.add("regular-account-id");
        accounts.add("joint-account-id");

        JSONObject payload = new JSONObject();
        payload.put(CDSConsentExtensionConstants.ACCOUNT_IDS, accounts);

        Map<String, Object> consentDataMap = new HashMap<>();
        consentDataMap.put(CDSConsentExtensionConstants.ACCOUNTS,
                new JSONParser(JSONParser.MODE_PERMISSIVE).parse(TEST_ACCOUNT_DATA_JSON));
        ConsentData consentDataMock = mock(ConsentData.class);

        when(consentDataMock.getUserId()).thenReturn("test-joint-account-user-id");
        when(consentDataMock.getMetaDataMap()).thenReturn(consentDataMap);

        when(this.consentPersistDataMock.getConsentData()).thenReturn(consentDataMock);
        when(this.consentPersistDataMock.getPayload()).thenReturn(payload);
        when(this.consentPersistDataMock.getApproval()).thenReturn(true);
    }

    @Test
    public void testExecute() {
        CDSJointAccountConsentPersistenceStep uut = new CDSJointAccountConsentPersistenceStep();
        uut.execute(this.consentPersistDataMock);

        ArgumentCaptor<Object> jointAccountIdWithUsersCaptor = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<Object> usersWithMultipleJointAccountsCaptor = ArgumentCaptor.forClass(Object.class);

        verify(consentPersistDataMock).addMetadata(eq(CDSConsentExtensionConstants.
                NON_PRIMARY_ACCOUNT_ID_AGAINST_USERS_MAP), jointAccountIdWithUsersCaptor.capture());
        Map<String, Map<String, String>> jointAccountIdWithUsersCaptorValue =
                (Map<String, Map<String, String>>) jointAccountIdWithUsersCaptor.getValue();

        verify(consentPersistDataMock).addMetadata(eq(CDSConsentExtensionConstants.
                USER_ID_AGAINST_NON_PRIMARY_ACCOUNTS_MAP), usersWithMultipleJointAccountsCaptor.capture());
        Map<String, List<String>> usersWithMultipleJointAccountsCaptorValue =
                (Map<String, List<String>>) usersWithMultipleJointAccountsCaptor.getValue();

        assertTrue(jointAccountIdWithUsersCaptorValue.containsKey("joint-account-id"));
        assertTrue(jointAccountIdWithUsersCaptorValue.get("joint-account-id").
                containsKey("john@wso2.com@carbon.super"));
        assertTrue(jointAccountIdWithUsersCaptorValue.get("joint-account-id").containsKey("amy@wso2.com@carbon.super"));

        assertTrue(usersWithMultipleJointAccountsCaptorValue.containsKey("john@wso2.com@carbon.super"));
        assertTrue(usersWithMultipleJointAccountsCaptorValue.containsKey("amy@wso2.com@carbon.super"));
        assertTrue(usersWithMultipleJointAccountsCaptorValue.get("john@wso2.com@carbon.super")
                .contains("joint-account-id"));
        assertTrue(usersWithMultipleJointAccountsCaptorValue.get("amy@wso2.com@carbon.super")
                .contains("joint-account-id"));
    }
}

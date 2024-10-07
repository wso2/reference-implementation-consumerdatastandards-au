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
package org.wso2.openbanking.cds.consent.extensions.util;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.SPQueryExecutorUtil;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.ParseException;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;
import org.wso2.openbanking.cds.common.utils.CommonConstants;
import org.wso2.openbanking.cds.consent.extensions.model.DataClusterSharingDateModel;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Test class for Data Cluster Sharing Date Utils.
 */
@PrepareForTest({OpenBankingCDSConfigParser.class, SPQueryExecutorUtil.class})
@PowerMockIgnore("jdk.internal.reflect.*")

public class DataClusterSharingDateUtilTest extends PowerMockTestCase {
    private static final String consentId = "test_consent_id";
    private static final String dataCluster = "account_basic_read";
    private OpenBankingCDSConfigParser openBankingCDSConfigParser;
    private Map<String, Object> cdsConfigMap = new HashMap<>();

    @Test
    public void testGetSharingDateMap() throws OpenBankingException, IOException, ParseException {

        JSONObject spQueryResponse = new JSONObject();
        JSONArray records = new JSONArray();
        JSONArray recordObj = new JSONArray();
        recordObj.add(0, consentId);
        recordObj.add(1, dataCluster);
        recordObj.add(2, 111111);
        recordObj.add(3, 222222);
        records.add(recordObj);
        spQueryResponse.put("records", records);

        cdsConfigMap.put(CommonConstants.SP_SERVER_URL, "server_url");
        cdsConfigMap.put(CommonConstants.SP_USERNAME, "username");
        cdsConfigMap.put(CommonConstants.SP_PASSWORD, "password");

        mockStatic(OpenBankingCDSConfigParser.class);
        openBankingCDSConfigParser = mock(OpenBankingCDSConfigParser.class);
        PowerMockito.when(OpenBankingCDSConfigParser.getInstance()).thenReturn(openBankingCDSConfigParser);
        PowerMockito.when(openBankingCDSConfigParser.getConfiguration()).thenReturn(cdsConfigMap);

        mockStatic(SPQueryExecutorUtil.class);
        when(SPQueryExecutorUtil
                .executeQueryOnStreamProcessor(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(spQueryResponse);

        Map<String, DataClusterSharingDateModel> sharingDateDataMap =
                DataClusterSharingDateUtil.getSharingDateMap(consentId);

        DataClusterSharingDateModel sharingDateModel = sharingDateDataMap.get(dataCluster);
        Assert.assertNotNull(sharingDateModel);
        Assert.assertNotNull(sharingDateModel.getDataCluster());
        Assert.assertNotNull(sharingDateModel.getSharingStartDate());
        Assert.assertNotNull(sharingDateModel.getLastSharedDate());
    }
}

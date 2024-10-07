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

package org.wso2.openbanking.cds.common;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.openbanking.cds.common.error.handling.models.CDSErrorMeta;
import org.wso2.openbanking.cds.common.error.handling.util.ErrorConstants;
import org.wso2.openbanking.cds.common.error.handling.util.ErrorUtil;
import org.wso2.openbanking.cds.common.testutils.CommonTestDataProvider;

import java.util.HashSet;

/**
 * Test class for Error Util Functionality.
 */
public class ErrorUtilTest {

    @Test(dataProvider = "ClientErrorTestDataProvider", dataProviderClass = CommonTestDataProvider.class)
    public void testClientErrorScenarios(String errorCode, Boolean assertion) {

        HashSet<String> statusCodes = new HashSet<>();
        statusCodes.add(errorCode);
        Boolean isClientError = ErrorUtil.isAnyClientErrors(statusCodes);

        Assert.assertEquals(isClientError, assertion);
    }

    @Test(dataProvider = "HttpsCodeTestDataProvider", dataProviderClass = CommonTestDataProvider.class)
    public void testGetHttpsErrorCodeMethod(String errorCode, int assertion) {

        HashSet<String> statusCodes = new HashSet<>();
        statusCodes.add(errorCode);
        int httpsCode = ErrorUtil.getHTTPErrorCode(statusCodes);

        Assert.assertEquals(httpsCode, assertion);
    }

    @Test(dataProvider = "ErrorObjectTestDataProvider", dataProviderClass = CommonTestDataProvider.class)
    public void testGetErrorObject(ErrorConstants.AUErrorEnum error, String errorMessage, CDSErrorMeta metaData) {

        JSONObject errorJson = ErrorUtil.getErrorObject(error, errorMessage, metaData);
        Assert.assertEquals(errorJson.get(ErrorConstants.ERROR_ENUM), error);
    }

    @Test(dataProvider = "ErrorObjectTestDataProvider", dataProviderClass = CommonTestDataProvider.class)
    public void testGetErrorJson(ErrorConstants.AUErrorEnum error, String errorMessage, CDSErrorMeta metaData) {

        JSONObject errorJson = ErrorUtil.getErrorObject(error, errorMessage, metaData);
        JSONArray errorJsonArray = new JSONArray();
        errorJsonArray.add(errorJson);
        String errorJsonString = ErrorUtil.getErrorJson(errorJsonArray);
        Assert.assertNotNull(errorJsonString);
    }

    @Test
    public void testGetErrorJsonWithJsonErrorMessage() {

        ErrorConstants.AUErrorEnum error = ErrorConstants.AUErrorEnum.EXPECTED_GENERAL_ERROR;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("detail", "Expected error");
        jsonObject.put("metaURN", "cds-standard-error-code");

        JSONObject errorJson = ErrorUtil.getErrorObject(error, jsonObject.toString(), new CDSErrorMeta());
        JSONArray errorJsonArray = new JSONArray();
        errorJsonArray.add(errorJson);
        String errorJsonString = ErrorUtil.getErrorJson(errorJsonArray);
        Assert.assertNotNull(errorJsonString);
    }
}

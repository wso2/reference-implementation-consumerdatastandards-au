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
package org.wso2.openbanking.cds.consent.extensions.authservlet.impl;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;
import org.wso2.openbanking.cds.common.utils.CommonConstants;
import org.wso2.openbanking.cds.consent.extensions.common.CDSConsentExtensionConstants;
import org.wso2.openbanking.cds.consent.extensions.util.CDSConsentExtensionsUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Test class for CDS Auth Servlet.
 */
@PrepareForTest({OpenBankingCDSConfigParser.class, CDSConsentExtensionsUtil.class})
@PowerMockIgnore({"com.wso2.openbanking.accelerator.consent.extensions.common.*", "jdk.internal.reflect.*"})
public class OBCDSAuthServletImplTests extends PowerMockTestCase {


    private static OpenBankingCDSConfigParser openBankingCDSConfigParserMock;
    private static OBCDSAuthServletImpl obCdsAuthServlet;
    private static HttpServletRequest httpServletRequest;
    private static ResourceBundle resourceBundle;
    private static Map<String, Boolean> configMap;

    @BeforeClass
    public void initClass() {
        configMap = new HashMap<>();
        configMap.put(CommonConstants.ACCOUNT_MASKING, true);

        obCdsAuthServlet = new OBCDSAuthServletImpl();
        httpServletRequest = mock(HttpServletRequest.class);
        resourceBundle = mock(ResourceBundle.class);
        openBankingCDSConfigParserMock = mock(OpenBankingCDSConfigParser.class);
    }

    @Test(expectedExceptions = JSONException.class)
    public void testUpdateRequestAttributeWithEmptyDataset() {
        obCdsAuthServlet.updateRequestAttribute(httpServletRequest, new JSONObject(), resourceBundle);
    }

    @Test
    public void testUpdateRequestAttributeWithValidDataset() {

        mockStatic(OpenBankingCDSConfigParser.class);
        when(OpenBankingCDSConfigParser.getInstance()).thenReturn(openBankingCDSConfigParserMock);
        doReturn(configMap).when(openBankingCDSConfigParserMock).getConfiguration();

        JSONArray dataRequested = new JSONArray();
        JSONArray accounts = new JSONArray();
        JSONObject dataSet = new JSONObject();

        dataSet.put(CDSConsentExtensionConstants.DATA_REQUESTED, dataRequested);
        dataSet.put(CDSConsentExtensionConstants.BUSINESS_DATA_CLUSTER, dataRequested);
        dataSet.put(CDSConsentExtensionConstants.ACCOUNTS, accounts);
        dataSet.put(CDSConsentExtensionConstants.CLIENT_ID, "TestClientID");
        dataSet.put(CDSConsentExtensionConstants.SP_FULL_NAME, "TestServiceProvider");
        dataSet.put(CDSConsentExtensionConstants.REDIRECT_URL, "TestRedirectUrl");
        dataSet.put(CDSConsentExtensionConstants.CONSENT_EXPIRY, "ConsentExpiryDate");
        dataSet.put(CDSConsentExtensionConstants.SHARING_DURATION_VALUE, 0);
        dataSet.put(CDSConsentExtensionConstants.USER_ID, "user1@gold.com");
        dataSet.put(CDSConsentExtensionConstants.STATE, "suite");
        dataSet.put(CDSConsentExtensionConstants.CUSTOMER_SCOPES_ONLY, false);

        Map<String, Object> returnMap = obCdsAuthServlet.updateRequestAttribute(
                httpServletRequest, dataSet, resourceBundle);

        Assert.assertFalse(returnMap.isEmpty());
    }

    @Test
    public void testUpdateRequestAttributeWithValidDatasetWithElements() throws OpenBankingException {

        mockStatic(OpenBankingCDSConfigParser.class);
        when(OpenBankingCDSConfigParser.getInstance()).thenReturn(openBankingCDSConfigParserMock);
        doReturn(configMap).when(openBankingCDSConfigParserMock).getConfiguration();
        mockStatic(CDSConsentExtensionsUtil.class);
        when(CDSConsentExtensionsUtil.isDOMSStatusEligibleForDataSharing(anyString())).thenReturn(true);

        JSONArray dataRequested = new JSONArray();
        JSONArray accounts = new JSONArray();
        JSONObject dataSet = new JSONObject();
        JSONArray testArray = new JSONArray();
        JSONObject dataReqJsonElement = new JSONObject();
        JSONObject accJsonElement = new JSONObject();
        JSONObject jointAccountInfo = new JSONObject();

        // add data to 'data_requested' section
        dataReqJsonElement.put(CDSConsentExtensionConstants.TITLE, "testTitle");
        dataReqJsonElement.put(CDSConsentExtensionConstants.DATA, testArray);
        dataRequested.put(dataReqJsonElement);

        // add data to jointAccountInfo
        jointAccountInfo.put(CDSConsentExtensionConstants.LINKED_MEMBER, new JSONArray());

        // add accounts data
        accJsonElement.put(CDSConsentExtensionConstants.ACCOUNT_ID, "1234");
        accJsonElement.put(CDSConsentExtensionConstants.DISPLAY_NAME, "test-account");
        accJsonElement.put(CDSConsentExtensionConstants.IS_ELIGIBLE, true);
        accJsonElement.put(CDSConsentExtensionConstants.CUSTOMER_ACCOUNT_TYPE,
                CDSConsentExtensionConstants.INDIVIDUAL_PROFILE_TYPE);
        accJsonElement.put(CDSConsentExtensionConstants.IS_JOINT_ACCOUNT_RESPONSE, true);
        accJsonElement.put(CDSConsentExtensionConstants.JOINT_ACCOUNT_CONSENT_ELECTION_STATUS,
                CDSConsentExtensionConstants.JOINT_ACCOUNT_PRE_APPROVAL);
        accJsonElement.put(CDSConsentExtensionConstants.JOINT_ACCOUNT_INFO, jointAccountInfo);
        accJsonElement.put(CDSConsentExtensionConstants.IS_SECONDARY_ACCOUNT_RESPONSE, false);
        accounts.put(accJsonElement);

        dataSet.put(CDSConsentExtensionConstants.DATA_REQUESTED, dataRequested);
        dataSet.put(CDSConsentExtensionConstants.BUSINESS_DATA_CLUSTER, dataRequested);
        dataSet.put(CDSConsentExtensionConstants.ACCOUNTS, accounts);
        dataSet.put(CDSConsentExtensionConstants.CLIENT_ID, "TestClientID");
        dataSet.put(CDSConsentExtensionConstants.SP_FULL_NAME, "TestServiceProvider");
        dataSet.put(CDSConsentExtensionConstants.REDIRECT_URL, "TestRedirectUrl");
        dataSet.put(CDSConsentExtensionConstants.CONSENT_EXPIRY, "ConsentExpiryDate");
        dataSet.put(CDSConsentExtensionConstants.SHARING_DURATION_VALUE, 0);
        dataSet.put(CDSConsentExtensionConstants.USER_ID, "user1@gold.com");
        dataSet.put(CDSConsentExtensionConstants.STATE, "suite");
        dataSet.put(CDSConsentExtensionConstants.CUSTOMER_SCOPES_ONLY, false);

        Map<String, Object> returnMap = obCdsAuthServlet.updateRequestAttribute(
                httpServletRequest, dataSet, resourceBundle);

        Assert.assertFalse(returnMap.isEmpty());
    }

    @Test
    public void testUpdateSessionAttribute() {
        JSONObject testJsonObject = new JSONObject();
        testJsonObject.put(CommonConstants.REQUEST_URI_KEY, "test-key");
        Map<String, Object> returnMap = obCdsAuthServlet.updateSessionAttribute(
                httpServletRequest, testJsonObject, resourceBundle);
        Assert.assertTrue(returnMap.containsKey(CommonConstants.REQUEST_URI_KEY));
    }

    @Test
    public void testUpdateConsentData() {
        when(httpServletRequest.getParameter("accounts[]")).thenReturn("1:2:3");
        Map<String, Object> returnMap = obCdsAuthServlet.updateConsentData(httpServletRequest);
        Assert.assertFalse(returnMap.isEmpty());
    }

    @Test
    public void testUpdateConsentMetaData() {
        Map<String, String> returnMap = obCdsAuthServlet.updateConsentMetaData(httpServletRequest);
        Assert.assertTrue(returnMap.isEmpty());
    }

    @Test
    public void testGetJSPPath() {
        String jspPath = obCdsAuthServlet.getJSPPath();
        Assert.assertEquals(jspPath, "/ob_cds_profile_selection.jsp");
    }
}

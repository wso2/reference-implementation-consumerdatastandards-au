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

package org.wso2.openbanking.cds.gateway.executors.error.handler;

import com.google.gson.JsonObject;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OpenBankingExecutorError;
import com.wso2.openbanking.accelerator.gateway.util.GatewayConstants;
import net.minidev.json.JSONObject;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.common.gateway.dto.APIRequestInfoDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.MsgInfoDTO;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;
import org.wso2.openbanking.cds.common.error.handling.util.ErrorConstants;
import org.wso2.openbanking.cds.gateway.executors.idpermanence.utils.IdPermanenceUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Test class for CDS Error Handler class.
 */
@PrepareForTest({IdPermanenceUtils.class, OpenBankingCDSConfigParser.class})
@PowerMockIgnore("jdk.internal.reflect.*")
public class CDSErrorHandlerTest extends PowerMockTestCase {

    OBAPIRequestContext obApiRequestContextMock;
    OBAPIResponseContext obApiResponseContextMock;
    CDSErrorHandler cdsErrorHandler;
    MsgInfoDTO msgInfoDTOMock;
    APIRequestInfoDTO apiRequestInfoDTOMock;

    ArrayList<OpenBankingExecutorError> errors = new ArrayList<>();
    ArrayList<OpenBankingExecutorError> emptyErrors = new ArrayList<>();
    ArrayList<OpenBankingExecutorError> dcrErrors = new ArrayList<>();
    ArrayList<OpenBankingExecutorError> accountErrors = new ArrayList<>();
    Map<String, String> addedHeaders = new HashMap<>();
    private static final Boolean TRUE = true;
    private static final Boolean FALSE = false;
    private static final String DCR_PATH = "/register";
    private static final String ACCOUNTS_PATH = "/banking/accounts/";

    @BeforeClass
    public void initClass() {

        cdsErrorHandler = Mockito.spy(CDSErrorHandler.class);
        obApiRequestContextMock = Mockito.mock(OBAPIRequestContext.class);
        obApiResponseContextMock = Mockito.mock(OBAPIResponseContext.class);
        msgInfoDTOMock = Mockito.mock(MsgInfoDTO.class);
        apiRequestInfoDTOMock = Mockito.mock(APIRequestInfoDTO.class);

        dcrErrors.add(new OpenBankingExecutorError("invalid_software_statement", "invalid_software_statement",
                "Duplicate registrations for a given software_id are not valid",
                ErrorConstants.BAD_REQUEST_CODE));

        accountErrors.add(new OpenBankingExecutorError("AU.CDR.Resource.InvalidBankingAccount",
                "Invalid Banking Account", "ID of the account not found or invalid",
                ErrorConstants.NOT_FOUND_CODE));
    }

    @Test
    public void testPreProcessRequestSuccessScenario() {
        Mockito.doReturn(FALSE).when(obApiRequestContextMock).isError();

        cdsErrorHandler.preProcessRequest(obApiRequestContextMock);
    }

    @Test
    public void testPostProcessRequestSuccessScenario() {
        Mockito.doReturn(FALSE).when(obApiRequestContextMock).isError();

        cdsErrorHandler.postProcessRequest(obApiRequestContextMock);
    }

    @Test
    public void testPreProcessResponseSuccessScenario() {
        Mockito.doReturn(FALSE).when(obApiResponseContextMock).isError();

        cdsErrorHandler.preProcessResponse(obApiResponseContextMock);
    }

    @Test
    public void testPostProcessResponseSuccessScenario() {
        Mockito.doReturn(FALSE).when(obApiResponseContextMock).isError();

        cdsErrorHandler.preProcessResponse(obApiResponseContextMock);
    }

    @Test
    public void testDCRPreProcessRequestErrorScenario() {
        Mockito.doReturn(TRUE).when(obApiRequestContextMock).isError();
        Mockito.doReturn(errors).when(obApiRequestContextMock).getErrors();
        Mockito.doReturn(msgInfoDTOMock).when(obApiRequestContextMock).getMsgInfo();
        Mockito.doReturn(DCR_PATH).when(msgInfoDTOMock).getResource();
        Mockito.doReturn(addedHeaders).when(obApiRequestContextMock).getAddedHeaders();
        Mockito.doReturn("dummyPayload").when(obApiRequestContextMock).getModifiedPayload();

        cdsErrorHandler.preProcessRequest(obApiRequestContextMock);
        Assert.assertNotNull(obApiRequestContextMock.getAddedHeaders());
        Assert.assertEquals(obApiRequestContextMock.getAddedHeaders().get(GatewayConstants.CONTENT_TYPE_TAG),
                GatewayConstants.JSON_CONTENT_TYPE);
    }

    @Test
    public void testAccountsPreProcessRequestErrorScenario() {
        Mockito.doReturn(TRUE).when(obApiRequestContextMock).isError();
        Mockito.doReturn(accountErrors).when(obApiRequestContextMock).getErrors();
        Mockito.doReturn(msgInfoDTOMock).when(obApiRequestContextMock).getMsgInfo();
        Mockito.doReturn(ACCOUNTS_PATH).when(msgInfoDTOMock).getResource();
        Mockito.doReturn(addedHeaders).when(obApiRequestContextMock).getAddedHeaders();
        Mockito.doReturn("dummyPayload").when(obApiRequestContextMock).getModifiedPayload();
        Mockito.doReturn(apiRequestInfoDTOMock).when(obApiRequestContextMock).getApiRequestInfo();
        Mockito.doReturn("mockUsername").when(apiRequestInfoDTOMock).getUsername();
        Mockito.doReturn("mockConsumerKey").when(apiRequestInfoDTOMock).getConsumerKey();

        cdsErrorHandler.preProcessRequest(obApiRequestContextMock);
        Assert.assertNotNull(obApiRequestContextMock.getAddedHeaders());
        Assert.assertEquals(obApiRequestContextMock.getAddedHeaders().get(GatewayConstants.CONTENT_TYPE_TAG),
                GatewayConstants.JSON_CONTENT_TYPE);
    }

    @Test
    public void testDCRPostProcessRequestErrorScenario() {
        Mockito.doReturn(TRUE).when(obApiRequestContextMock).isError();
        Mockito.doReturn(errors).when(obApiRequestContextMock).getErrors();
        Mockito.doReturn(msgInfoDTOMock).when(obApiRequestContextMock).getMsgInfo();
        Mockito.doReturn(DCR_PATH).when(msgInfoDTOMock).getResource();
        Mockito.doReturn(addedHeaders).when(obApiRequestContextMock).getAddedHeaders();
        Mockito.doReturn("dummyPayload").when(obApiRequestContextMock).getModifiedPayload();

        cdsErrorHandler.postProcessRequest(obApiRequestContextMock);
        Assert.assertNotNull(obApiRequestContextMock.getAddedHeaders());
        Assert.assertEquals(obApiRequestContextMock.getAddedHeaders().get(GatewayConstants.CONTENT_TYPE_TAG),
                GatewayConstants.JSON_CONTENT_TYPE);
    }

    @Test
    public void testAccountsPostProcessRequestErrorScenario() {
        Mockito.doReturn(TRUE).when(obApiRequestContextMock).isError();
        Mockito.doReturn(accountErrors).when(obApiRequestContextMock).getErrors();
        Mockito.doReturn(msgInfoDTOMock).when(obApiRequestContextMock).getMsgInfo();
        Mockito.doReturn(ACCOUNTS_PATH).when(msgInfoDTOMock).getResource();
        Mockito.doReturn(addedHeaders).when(obApiRequestContextMock).getAddedHeaders();
        Mockito.doReturn("dummyPayload").when(obApiRequestContextMock).getModifiedPayload();
        Mockito.doReturn(apiRequestInfoDTOMock).when(obApiRequestContextMock).getApiRequestInfo();
        Mockito.doReturn("mockUsername").when(apiRequestInfoDTOMock).getUsername();
        Mockito.doReturn("mockConsumerKey").when(apiRequestInfoDTOMock).getConsumerKey();

        cdsErrorHandler.postProcessRequest(obApiRequestContextMock);
        Assert.assertNotNull(obApiRequestContextMock.getAddedHeaders());
        Assert.assertEquals(obApiRequestContextMock.getAddedHeaders().get(GatewayConstants.CONTENT_TYPE_TAG),
                GatewayConstants.JSON_CONTENT_TYPE);
    }

    @Test
    public void testDCRPreProcessResponseErrorScenario() {
        Mockito.doReturn(TRUE).when(obApiResponseContextMock).isError();
        Mockito.doReturn(errors).when(obApiResponseContextMock).getErrors();
        Mockito.doReturn(msgInfoDTOMock).when(obApiResponseContextMock).getMsgInfo();
        Mockito.doReturn(DCR_PATH).when(msgInfoDTOMock).getResource();
        Mockito.doReturn(addedHeaders).when(obApiResponseContextMock).getAddedHeaders();
        Mockito.doReturn("dummyPayload").when(obApiResponseContextMock).getModifiedPayload();

        cdsErrorHandler.preProcessResponse(obApiResponseContextMock);
        Assert.assertNotNull(obApiResponseContextMock.getAddedHeaders());
        Assert.assertEquals(obApiResponseContextMock.getAddedHeaders().get(GatewayConstants.CONTENT_TYPE_TAG),
                GatewayConstants.JSON_CONTENT_TYPE);
        Assert.assertNotNull(obApiResponseContextMock.getAnalyticsData());
    }

    @Test
    public void testAccountsPreProcessResponseErrorScenario() {
        Mockito.doReturn(TRUE).when(obApiResponseContextMock).isError();
        Mockito.doReturn(accountErrors).when(obApiResponseContextMock).getErrors();
        Mockito.doReturn(msgInfoDTOMock).when(obApiResponseContextMock).getMsgInfo();
        Mockito.doReturn(ACCOUNTS_PATH).when(msgInfoDTOMock).getResource();
        Mockito.doReturn(addedHeaders).when(obApiResponseContextMock).getAddedHeaders();
        Mockito.doReturn("dummyPayload").when(obApiResponseContextMock).getModifiedPayload();
        Mockito.doReturn(apiRequestInfoDTOMock).when(obApiResponseContextMock).getApiRequestInfo();
        Mockito.doReturn("mockUsername").when(apiRequestInfoDTOMock).getUsername();
        Mockito.doReturn("mockConsumerKey").when(apiRequestInfoDTOMock).getConsumerKey();

        cdsErrorHandler.preProcessResponse(obApiResponseContextMock);
        Assert.assertNotNull(obApiResponseContextMock.getAddedHeaders());
        Assert.assertEquals(obApiResponseContextMock.getAddedHeaders().get(GatewayConstants.CONTENT_TYPE_TAG),
                GatewayConstants.JSON_CONTENT_TYPE);
        Assert.assertNotNull(obApiResponseContextMock.getAnalyticsData());
    }

    @Test
    public void testDCRPostProcessResponseErrorScenario() {
        Mockito.doReturn(TRUE).when(obApiResponseContextMock).isError();
        Mockito.doReturn(errors).when(obApiResponseContextMock).getErrors();
        Mockito.doReturn(msgInfoDTOMock).when(obApiResponseContextMock).getMsgInfo();
        Mockito.doReturn(DCR_PATH).when(msgInfoDTOMock).getResource();
        Mockito.doReturn(addedHeaders).when(obApiResponseContextMock).getAddedHeaders();
        Mockito.doReturn("dummyPayload").when(obApiResponseContextMock).getModifiedPayload();

        cdsErrorHandler.postProcessResponse(obApiResponseContextMock);
        Assert.assertNotNull(obApiResponseContextMock.getAddedHeaders());
        Assert.assertEquals(obApiResponseContextMock.getAddedHeaders().get(GatewayConstants.CONTENT_TYPE_TAG),
                GatewayConstants.JSON_CONTENT_TYPE);
        Assert.assertNotNull(obApiResponseContextMock.getAnalyticsData());
    }

    @Test
    public void testAccountsPostProcessResponseErrorScenario() {
        Mockito.doReturn(TRUE).when(obApiResponseContextMock).isError();
        Mockito.doReturn(accountErrors).when(obApiResponseContextMock).getErrors();
        Mockito.doReturn(msgInfoDTOMock).when(obApiResponseContextMock).getMsgInfo();
        Mockito.doReturn(ACCOUNTS_PATH).when(msgInfoDTOMock).getResource();
        Mockito.doReturn(addedHeaders).when(obApiResponseContextMock).getAddedHeaders();
        Mockito.doReturn("dummyPayload").when(obApiResponseContextMock).getModifiedPayload();
        Mockito.doReturn(apiRequestInfoDTOMock).when(obApiResponseContextMock).getApiRequestInfo();
        Mockito.doReturn("mockUsername").when(apiRequestInfoDTOMock).getUsername();
        Mockito.doReturn("mockConsumerKey").when(apiRequestInfoDTOMock).getConsumerKey();


        cdsErrorHandler.postProcessResponse(obApiResponseContextMock);
        Assert.assertNotNull(obApiResponseContextMock.getAddedHeaders());
        Assert.assertEquals(obApiResponseContextMock.getAddedHeaders().get(GatewayConstants.CONTENT_TYPE_TAG),
                GatewayConstants.JSON_CONTENT_TYPE);
        Assert.assertNotNull(obApiResponseContextMock.getAnalyticsData());
    }

    @Test
    public void testGetDCRErrorJson() {

        JSONObject errorJson = CDSErrorHandler.getOAuthErrorJSON(dcrErrors);
        Assert.assertNotNull(errorJson);
        Assert.assertEquals(errorJson.get(ErrorConstants.ERROR), "invalid_software_statement");
        Assert.assertEquals(errorJson.get(ErrorConstants.ERROR_DESCRIPTION),
                "Duplicate registrations for a given software_id are not valid");
    }

    @Test
    public void testGetEmptyDCRErrorJson() {

        JSONObject errorJson = CDSErrorHandler.getOAuthErrorJSON(emptyErrors);
        Assert.assertTrue(errorJson.isEmpty());
    }

    @Test
    public void testGetErrorJsonWithAccountId() {

        JSONObject errorDetails = new JSONObject();
        errorDetails.put("accountId", "1421414");
        errorDetails.put("metaURN", "cds-standard-error-code");
        errorDetails.put("detail", "errorMessage");

        PowerMockito.mockStatic(OpenBankingCDSConfigParser.class);
        OpenBankingCDSConfigParser openBankingCDSConfigParserMock = PowerMockito.mock(OpenBankingCDSConfigParser.class);
        PowerMockito.when(OpenBankingCDSConfigParser.getInstance()).thenReturn(openBankingCDSConfigParserMock);
        PowerMockito.when(openBankingCDSConfigParserMock.getIdPermanenceSecretKey()).thenReturn("wso2123");

        PowerMockito.mockStatic(IdPermanenceUtils.class);
        Mockito.when(IdPermanenceUtils.encryptAccountIdInErrorResponse(Mockito.anyObject(), Mockito.anyString(),
                Mockito.anyString())).thenReturn("encryptedAccountId");

        ArrayList<OpenBankingExecutorError> accountErrorList = new ArrayList<>();

        accountErrorList.add(new OpenBankingExecutorError("AU.CDR.Resource.InvalidBankingAccount",
                "Consent Enforcement Error", errorDetails.toString(),
                ErrorConstants.NOT_FOUND_CODE));
        JsonObject errorJson = CDSErrorHandler.getErrorJson(accountErrorList, "", "", new HashMap<>());

        Assert.assertNotNull(errorJson);
    }

    @Test
    public void testGetErrorJsonWithoutAccountId() {

        JSONObject errorDetails = new JSONObject();
        errorDetails.put("metaURN", "cds-standard-error-code");
        errorDetails.put("detail", "errorMessage");

        ArrayList<OpenBankingExecutorError> accountErrorList = new ArrayList<>();
        accountErrorList.add(new OpenBankingExecutorError("AU.CDR.Resource.InvalidBankingAccount",
                "Consent Enforcement Error", errorDetails.toString(),
                ErrorConstants.NOT_FOUND_CODE));
        JsonObject errorJson = CDSErrorHandler.getErrorJson(accountErrorList, "", "", new HashMap<>());

        Assert.assertNotNull(errorJson);
    }
}

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

package org.wso2.openbanking.cds.gateway.executors.idpermanence;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.common.gateway.dto.APIRequestInfoDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.MsgInfoDTO;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;
import org.wso2.openbanking.cds.common.idpermanence.IdEncryptorDecryptor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;


/**
 * Test class for IDPermanenceExecutor.
 */
@PrepareForTest({IdEncryptorDecryptor.class, OpenBankingCDSConfigParser.class})
@PowerMockIgnore("jdk.internal.reflect.*")
public class IDPermanenceExecutorTest extends PowerMockTestCase {

    private static final String API_CONTEXT = "/cds-au/version";
    private static final String ACCOUNTS_URL = "/banking/accounts";
    private static final String DIRECT_DEBITS_URL = "banking/accounts/direct-debits";
    private static final String USERNAME = "mark@gold.com@carbon.super@carbon.super";
    private static final String APP_ID = "7";
    private static final String ENCRYPTION_KEY = "wso2";
    private static final String ENCRYPTED_ID = "encrypted-account-id";
    private static final String DECRYPTED_ACCOUNT_STRING = "mark@gold.com@carbon.super@carbon.super:7:30080012343456";

    // TODO : Add "meta" to Json
    private static final String ENCRYPTED_ACCOUNT_IDS_JSON = "{\"data\":{\"accountIds\":[\"encrypted-account-id\"," +
            "\"encrypted-account-id\"]}}";
    public static final String POST_METHOD = "POST";
    OpenBankingCDSConfigParser openBankingCDSConfigParserMock;

    @BeforeClass
    public void initClass() {

    }

    @Test
    public void testPostProcessRequest() {
        openBankingCDSConfigParserMock = PowerMockito.mock(OpenBankingCDSConfigParser.class);
        PowerMockito.mockStatic(OpenBankingCDSConfigParser.class);
        PowerMockito.when(OpenBankingCDSConfigParser.getInstance()).thenReturn(openBankingCDSConfigParserMock);
        doReturn(ENCRYPTION_KEY).when(openBankingCDSConfigParserMock).getIdPermanenceSecretKey();

        IDPermanenceExecutor idPermanenceExecutor = Mockito.spy(IDPermanenceExecutor.class);
        OBAPIRequestContext obapiRequestContext = Mockito.mock(OBAPIRequestContext.class);
        MsgInfoDTO msgInfoDTO = Mockito.mock(MsgInfoDTO.class);
        APIRequestInfoDTO apiRequestInfoDTO = Mockito.mock(APIRequestInfoDTO.class);
        PowerMockito.mockStatic(IdEncryptorDecryptor.class);

        Mockito.doReturn(msgInfoDTO).when(obapiRequestContext).getMsgInfo();
        Mockito.doReturn(apiRequestInfoDTO).when(obapiRequestContext).getApiRequestInfo();
        Mockito.doReturn(API_CONTEXT).when(apiRequestInfoDTO).getContext();
        Mockito.doReturn(DIRECT_DEBITS_URL).when(msgInfoDTO).getElectedResource();
        Mockito.doReturn(DIRECT_DEBITS_URL).when(msgInfoDTO).getResource();
        Mockito.doReturn(POST_METHOD).when(msgInfoDTO).getHttpMethod();
        Mockito.doReturn(ENCRYPTED_ACCOUNT_IDS_JSON).when(obapiRequestContext).getRequestPayload();
        Mockito.when(IdEncryptorDecryptor.decrypt(ENCRYPTED_ID, ENCRYPTION_KEY)).thenReturn(DECRYPTED_ACCOUNT_STRING);

        idPermanenceExecutor.postProcessRequest(obapiRequestContext);
        Mockito.verify(obapiRequestContext, times(0)).setError(true);
    }

    @Test
    public void testPostProcessResponse() throws IOException {
        openBankingCDSConfigParserMock = PowerMockito.mock(OpenBankingCDSConfigParser.class);
        PowerMockito.mockStatic(OpenBankingCDSConfigParser.class);
        PowerMockito.when(OpenBankingCDSConfigParser.getInstance()).thenReturn(openBankingCDSConfigParserMock);
        doReturn(ENCRYPTION_KEY).when(openBankingCDSConfigParserMock).getIdPermanenceSecretKey();

        IDPermanenceExecutor idPermanenceExecutor = Mockito.spy(IDPermanenceExecutor.class);
        OBAPIResponseContext obapiResponseContext = Mockito.mock(OBAPIResponseContext.class);
        MsgInfoDTO msgInfoDTO = Mockito.mock(MsgInfoDTO.class);
        APIRequestInfoDTO apiRequestInfoDTO = Mockito.mock(APIRequestInfoDTO.class);
        PowerMockito.mockStatic(IdEncryptorDecryptor.class);

        // Read accounts response
        File file = new File("src/test/resources/test-account-response.json");
        byte[] crlBytes = FileUtils.readFileToString(file, String.valueOf(StandardCharsets.UTF_8))
                .getBytes(StandardCharsets.UTF_8);
        InputStream inputStream = new ByteArrayInputStream(crlBytes);
        JsonParser jsonParser = new JsonParser();
        JsonObject accountsResponse = (JsonObject) jsonParser.parse(new InputStreamReader(inputStream));
        String accountsResponseString = new Gson().toJson(accountsResponse);

        Mockito.doReturn(msgInfoDTO).when(obapiResponseContext).getMsgInfo();
        Mockito.doReturn(apiRequestInfoDTO).when(obapiResponseContext).getApiRequestInfo();
        Mockito.doReturn(USERNAME).when(apiRequestInfoDTO).getUsername();
        Mockito.doReturn(APP_ID).when(apiRequestInfoDTO).getConsumerKey();
        Mockito.doReturn(API_CONTEXT).when(apiRequestInfoDTO).getContext();
        Mockito.doReturn(ACCOUNTS_URL).when(msgInfoDTO).getElectedResource();
        Mockito.doReturn(HttpStatus.SC_OK).when(obapiResponseContext).getStatusCode();
        Mockito.doReturn(accountsResponseString).when(obapiResponseContext).getResponsePayload();
        Mockito.when(IdEncryptorDecryptor.encrypt(DECRYPTED_ACCOUNT_STRING, ENCRYPTION_KEY)).thenReturn(ENCRYPTED_ID);

        idPermanenceExecutor.postProcessResponse(obapiResponseContext);
        Mockito.verify(obapiResponseContext, times(0)).setError(true);
    }
}

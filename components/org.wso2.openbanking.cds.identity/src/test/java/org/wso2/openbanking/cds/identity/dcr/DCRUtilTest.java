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

package org.wso2.openbanking.cds.identity.dcr;

import com.google.gson.Gson;
import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.common.util.JWTUtils;
import com.wso2.openbanking.accelerator.identity.dcr.exception.DCRValidationException;
import com.wso2.openbanking.accelerator.identity.dcr.model.RegistrationRequest;
import com.wso2.openbanking.accelerator.identity.dcr.validation.DCRCommonConstants;
import com.wso2.openbanking.accelerator.identity.dcr.validation.RegistrationValidator;
import com.wso2.openbanking.accelerator.identity.internal.IdentityExtensionsDataHolder;
import com.wso2.openbanking.accelerator.identity.util.HTTPClientUtils;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;
import org.wso2.openbanking.cds.identity.dcr.model.CDSRegistrationRequest;
import org.wso2.openbanking.cds.identity.dcr.util.RegistrationTestConstants;
import org.wso2.openbanking.cds.identity.dcr.utils.ValidationUtils;
import org.wso2.openbanking.cds.identity.dcr.validation.CDSRegistrationValidatorImpl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Test class for DCR functionalities.
 */
@PrepareForTest({JWTUtils.class, OpenBankingCDSConfigParser.class, HTTPClientUtils.class,
        OpenBankingConfigParser.class})
@PowerMockIgnore({"javax.net.ssl.*", "jdk.internal.reflect.*"})
public class DCRUtilTest {

    private static final Log log = LogFactory.getLog(DCRUtilTest.class);

    private RegistrationValidator registrationValidator;
    private CDSRegistrationValidatorImpl extendedValidator = new CDSRegistrationValidatorImpl();
    private RegistrationRequest registrationRequest;
    private OpenBankingCDSConfigParser openBankingCDSConfigParser;
    private OpenBankingConfigParser openBankingConfigParser;
    private Map<String, Object> cdsConfigMap = new HashMap<>();
    private static final String NULL = "null";

    @BeforeClass
    public void beforeClass() {

        Map<String, Object> confMap = new HashMap<>();
        Map<String, Map<String, Object>> dcrRegistrationConfMap = new HashMap<>();
        List<String> registrationParams = Arrays.asList("Issuer:true:null",
                "TokenEndPointAuthentication:true:private_key_jwt", "ResponseTypes:true:code id_token",
                "GrantTypes:true:authorization_code,refresh_token", "ApplicationType:false:web",
                "IdTokenSignedResponseAlg:true:null", "SoftwareStatement:true:null", "Scope:false:accounts,payments");
        confMap.put(DCRCommonConstants.DCR_VALIDATOR, "org.wso2.openbanking.cds.identity.dcr.validation" +
                ".CDSRegistrationValidatorImpl");
        confMap.put("DCR.JwksUrlProduction",
                "https://keystore.openbankingtest.org.uk/0015800001HQQrZAAX/9b5usDpbNtmxDcTzs7GzKp.jwks");
        confMap.put("DCR.JwksUrlSandbox",
                "https://keystore.openbankingtest.org.uk/0015800001HQQrZAAX/9b5usDpbNtmxDcTzs7GzKp.jwks");
        cdsConfigMap.put("DCR.EnableHostNameValidation", "false");
        cdsConfigMap.put("DCR.EnableURIValidation", "false");
        cdsConfigMap.put("DCR.EnableSectorIdentifierUriValidation", "false");
        List<String> validAlgorithms = new ArrayList<>();
        validAlgorithms.add("PS256");
        validAlgorithms.add("ES256");
        confMap.put(OpenBankingConstants.SIGNATURE_ALGORITHMS, validAlgorithms);
        IdentityExtensionsDataHolder.getInstance().setConfigurationMap(confMap);

        String dcrValidator = confMap.get(DCRCommonConstants.DCR_VALIDATOR).toString();
        registrationValidator = getDCRValidator(dcrValidator);
        registrationRequest = getRegistrationRequestObject(RegistrationTestConstants.registrationRequestJson);
        for (String param : registrationParams) {
            setParamConfig(param, dcrRegistrationConfMap);
        }
        IdentityExtensionsDataHolder.getInstance().setDcrRegistrationConfigMap(dcrRegistrationConfMap);
    }

    @Test
    public void testInvalidCallbackUris() throws Exception {

        registrationRequest.setCallbackUris(Arrays.asList("https://www.google.com"));

        initiateData();
        CDSRegistrationRequest cdsRegistrationRequest = new CDSRegistrationRequest(registrationRequest);
        try {
            ValidationUtils.validateRequest(cdsRegistrationRequest);
        } catch (DCRValidationException e) {
            Assert.assertTrue(e.getErrorDescription().contains("Invalid callback uris"));
        }
    }

    @Test(dependsOnMethods = "testInvalidCallbackUris")
    public void testInvalidUriHostnames() throws Exception {

        registrationRequest.setCallbackUris(Arrays.asList("https://www.google.com/redirects/redirect1",
                "https://www.google.com/redirects/redirect2"));
        cdsConfigMap.put("DCR.EnableHostNameValidation", "true");

        initiateData();
        CDSRegistrationRequest cdsRegistrationRequest = new CDSRegistrationRequest(registrationRequest);
        try {
            ValidationUtils.validateRequest(cdsRegistrationRequest);
        } catch (DCRValidationException e) {
            Assert.assertTrue(e.getErrorDescription().contains("Host names of logo_uri/tos_uri/policy_uri/client_uri " +
                    "does not match with the redirect_uris"));
        }
    }

    @Test(dependsOnMethods = "testInvalidUriHostnames")
    public void testInvalidUriConnection() throws Exception {

        cdsConfigMap.put("DCR.EnableHostNameValidation", "false");
        cdsConfigMap.put("DCR.EnableURIValidation", "true");

        initiateData();
        CDSRegistrationRequest cdsRegistrationRequest = new CDSRegistrationRequest(registrationRequest);
        try {
            ValidationUtils.validateRequest(cdsRegistrationRequest);
        } catch (DCRValidationException e) {
            Assert.assertTrue(e.getErrorDescription().contains("Provided logo_uri/client_uri/policy_uri/tos_uri " +
                    "in the request does not resolve to a valid web page"));
        }
    }

    @Test(dependsOnMethods = "testInvalidUriConnection")
    public void testValidUriConnection() throws Exception {

        mockStatic(JWTUtils.class);
        when(JWTUtils.validateJWTSignature(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        mockStatic(OpenBankingCDSConfigParser.class);
        openBankingCDSConfigParser = mock(OpenBankingCDSConfigParser.class);
        when(OpenBankingCDSConfigParser.getInstance()).thenReturn(openBankingCDSConfigParser);
        when(openBankingCDSConfigParser.getConfiguration()).thenReturn(cdsConfigMap);

        mockStatic(OpenBankingConfigParser.class);
        openBankingConfigParser = mock(OpenBankingConfigParser.class);
        when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParser);
        PowerMockito.when(OpenBankingConfigParser.getInstance()
                .getSoftwareEnvIdentificationSSAPropertyValueForSandbox()).thenReturn("sandbox");
        PowerMockito.when(OpenBankingConfigParser.getInstance()
                .getSoftwareEnvIdentificationSSAPropertyName()).thenReturn("software_environment");
        when(openBankingConfigParser.getConfiguration()).thenReturn(new HashMap<>());


        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(RegistrationTestConstants.ssaBodyJsonWithDummyWorkingURLs);

        when(JWTUtils.decodeRequestJWT(Mockito.anyString(), Mockito.anyString())).thenReturn(json);

        String decodedSSA = null;
        try {
            decodedSSA = JWTUtils
                    .decodeRequestJWT(registrationRequest.getSoftwareStatement(), "body").toJSONString();
        } catch (ParseException e) {
            log.error("Error while parsing the SSA", e);
        }
        registrationValidator.setSoftwareStatementPayload(registrationRequest, decodedSSA);
        CDSRegistrationRequest cdsRegistrationRequest = new CDSRegistrationRequest(registrationRequest);
        try {
            ValidationUtils.validateRequest(cdsRegistrationRequest);
        } catch (DCRValidationException e) {
            Assert.fail("should not throw exception");
        }
    }

    @Test
    public void testInvalidSSACallBackUrisSet() throws Exception {

        cdsConfigMap.put("DCR.EnableHostNameValidation", "false");
        cdsConfigMap.put("DCR.EnableURIValidation", "false");

        mockStatic(JWTUtils.class);
        when(JWTUtils.validateJWTSignature(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        mockStatic(OpenBankingCDSConfigParser.class);
        openBankingCDSConfigParser = mock(OpenBankingCDSConfigParser.class);
        when(OpenBankingCDSConfigParser.getInstance()).thenReturn(openBankingCDSConfigParser);
        when(openBankingCDSConfigParser.getConfiguration()).thenReturn(cdsConfigMap);

        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser
                .parse(RegistrationTestConstants.ssaBodyJsonWithDifferentRedirectUriHostnames);

        when(JWTUtils.decodeRequestJWT(Mockito.anyString(), Mockito.anyString())).thenReturn(json);

        String decodedSSA = null;
        try {
            decodedSSA = JWTUtils
                    .decodeRequestJWT(registrationRequest.getSoftwareStatement(), "body").toJSONString();
        } catch (ParseException e) {
            log.error("Error while parsing the SSA", e);
        }
        registrationValidator.setSoftwareStatementPayload(registrationRequest, decodedSSA);
        CDSRegistrationRequest cdsRegistrationRequest = new CDSRegistrationRequest(registrationRequest);
        try {
            ValidationUtils.validateRequest(cdsRegistrationRequest);
        } catch (DCRValidationException e) {
            Assert.assertTrue(e.getErrorDescription().contains("Redirect URIs do not contain the same hostname"));
        }
    }

    @Test
    public void testSectorIdentifierUriErrorScenario() throws Exception {

        cdsConfigMap.put("DCR.EnableHostNameValidation", "false");
        cdsConfigMap.put("DCR.EnableURIValidation", "false");
        cdsConfigMap.put("DCR.EnableSectorIdentifierUriValidation", "true");

        mockStatic(JWTUtils.class);
        when(JWTUtils.validateJWTSignature(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        mockStatic(OpenBankingCDSConfigParser.class);
        openBankingCDSConfigParser = mock(OpenBankingCDSConfigParser.class);
        when(OpenBankingCDSConfigParser.getInstance()).thenReturn(openBankingCDSConfigParser);
        when(openBankingCDSConfigParser.getConfiguration()).thenReturn(cdsConfigMap);

        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser
                .parse(RegistrationTestConstants.ssaBodyJson);

        when(JWTUtils.decodeRequestJWT(Mockito.anyString(), Mockito.anyString())).thenReturn(json);

        String decodedSSA = null;
        try {
            decodedSSA = JWTUtils
                    .decodeRequestJWT(registrationRequest.getSoftwareStatement(), "body").toJSONString();
        } catch (ParseException e) {
            log.error("Error while parsing the SSA", e);
        }
        registrationValidator.setSoftwareStatementPayload(registrationRequest, decodedSSA);
        CDSRegistrationRequest cdsRegistrationRequest = new CDSRegistrationRequest(registrationRequest);

        StatusLine statusLineMock = Mockito.mock(StatusLine.class);
        Mockito.doReturn(HttpStatus.SC_OK).when(statusLineMock).getStatusCode();

        File file = new File("src/test/resources/test-sector-identifier-uri.json");
        byte[] crlBytes = FileUtils.readFileToString(file, String.valueOf(StandardCharsets.UTF_8))
                .getBytes(StandardCharsets.UTF_8);
        InputStream inStream = new ByteArrayInputStream(crlBytes);

        HttpEntity httpEntityMock = Mockito.mock(HttpEntity.class);
        Mockito.doReturn(inStream).when(httpEntityMock).getContent();

        CloseableHttpResponse httpResponseMock = Mockito.mock(CloseableHttpResponse.class);
        Mockito.doReturn(statusLineMock).when(httpResponseMock).getStatusLine();
        Mockito.doReturn(httpEntityMock).when(httpResponseMock).getEntity();

        CloseableHttpClient closeableHttpClientMock = Mockito.mock(CloseableHttpClient.class);
        Mockito.doReturn(httpResponseMock).when(closeableHttpClientMock).execute(Mockito.any(HttpGet.class));

        PowerMockito.mockStatic(HTTPClientUtils.class);
        Mockito.when(HTTPClientUtils.getHttpsClient()).thenReturn(closeableHttpClientMock);
        try {
            ValidationUtils.validateRequest(cdsRegistrationRequest);
        } catch (DCRValidationException e) {
            Assert.assertTrue(e.getErrorDescription().contains("redirect_uris do not match with" +
                    " the elements in sector identifier uri"));
            cdsConfigMap.put("DCR.EnableSectorIdentifierUriValidation", "false");
        }
    }

    @Test(priority = 1)
    public void testExtendedValidatePostFailure() throws Exception {

        cdsConfigMap.put("DCR.EnableHostNameValidation", "false");
        cdsConfigMap.put("DCR.EnableURIValidation", "false");
        registrationRequest = getRegistrationRequestObject(RegistrationTestConstants.extendedRegistrationRequestJson);
        initiateData();
        try {
            extendedValidator.validatePost(registrationRequest);
        } catch (DCRValidationException e) {
            Assert.assertTrue(e.getErrorDescription().contains("Invalid issuer"));
        }
    }

    @Test(priority = 1)
    public void testExtendedValidateUpdateFailure() throws Exception {

        cdsConfigMap.put("DCR.EnableHostNameValidation", "false");
        cdsConfigMap.put("DCR.EnableURIValidation", "false");
        registrationRequest = getRegistrationRequestObject(RegistrationTestConstants.extendedRegistrationRequestJson);
        initiateData();
        try {
            extendedValidator.validateUpdate(registrationRequest);
        } catch (DCRValidationException e) {
            Assert.assertTrue(e.getErrorDescription().contains("Invalid issuer"));
        }
    }

    private void initiateData() throws Exception {

        mockStatic(JWTUtils.class);
        when(JWTUtils.validateJWTSignature(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        mockStatic(OpenBankingCDSConfigParser.class);
        openBankingCDSConfigParser = mock(OpenBankingCDSConfigParser.class);
        when(OpenBankingCDSConfigParser.getInstance()).thenReturn(openBankingCDSConfigParser);
        when(openBankingCDSConfigParser.getConfiguration()).thenReturn(cdsConfigMap);

        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(RegistrationTestConstants.ssaBodyJson);

        when(JWTUtils.decodeRequestJWT(Mockito.anyString(), Mockito.anyString())).thenReturn(json);

        String decodedSSA = null;
        try {
            decodedSSA = JWTUtils
                    .decodeRequestJWT(registrationRequest.getSoftwareStatement(), "body").toJSONString();
        } catch (ParseException e) {
            log.error("Error while parsing the SSA", e);
        }
        registrationValidator.setSoftwareStatementPayload(registrationRequest, decodedSSA);
    }

    private static RegistrationRequest getRegistrationRequestObject(String request) {

        Gson gson = new Gson();
        return gson.fromJson(request, RegistrationRequest.class);
    }

    public static RegistrationValidator getDCRValidator(String dcrValidator) {

        if (StringUtils.isEmpty(dcrValidator)) {
            return new CDSRegistrationValidatorImpl();
        }
        try {
            return (RegistrationValidator) Class.forName(dcrValidator).newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            log.error("Error instantiating " + dcrValidator, e);
            return new CDSRegistrationValidatorImpl();
        } catch (ClassNotFoundException e) {
            log.error("Cannot find class: " + dcrValidator, e);
            return new CDSRegistrationValidatorImpl();
        }
    }

    private void setParamConfig(String configParam, Map<String, Map<String, Object>> dcrRegistrationConfMap) {
        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put(DCRCommonConstants.DCR_REGISTRATION_PARAM_REQUIRED, configParam.split(":")[1]);
        if (!NULL.equalsIgnoreCase(configParam.split(":")[2])) {
            List<String> allowedValues = new ArrayList<>();
            allowedValues.addAll(Arrays.asList(configParam.split(":")[2].split(",")));
            parameterValues.put(DCRCommonConstants.DCR_REGISTRATION_PARAM_ALLOWED_VALUES, allowedValues);
        }
        dcrRegistrationConfMap.put(configParam.split(":")[0], parameterValues);
    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {

        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }
}

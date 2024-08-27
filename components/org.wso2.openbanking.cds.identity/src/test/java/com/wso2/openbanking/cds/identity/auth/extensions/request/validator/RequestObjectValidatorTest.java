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

package org.wso2.openbanking.cds.identity.auth.extensions.request.validator;

import com.nimbusds.jose.JOSEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import com.wso2.openbanking.accelerator.common.validator.OpenBankingValidator;
import com.wso2.openbanking.accelerator.identity.auth.extensions.request.validator.models.OBRequestObject;
import com.wso2.openbanking.accelerator.identity.auth.extensions.request.validator.models.ValidationResponse;
import org.apache.commons.lang3.StringUtils;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.oauth2.RequestObjectException;
import org.wso2.carbon.identity.openidconnect.model.RequestObject;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;
import org.wso2.openbanking.cds.common.metadata.domain.MetadataValidationResponse;
import org.wso2.openbanking.cds.common.metadata.status.validator.service.MetadataService;
import org.wso2.openbanking.cds.identity.auth.extensions.request.validator.impl.SharingDurationValidator;
import org.wso2.openbanking.cds.identity.auth.extensions.request.validator.model.CDSRequestObject;
import org.wso2.openbanking.cds.identity.auth.extensions.request.validator.util.ReqObjectTestDataProvider;

import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.method;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.stub;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Test class for Request Object Validator functionality.
 */
@PrepareForTest({OpenBankingValidator.class, OpenBankingCDSConfigParser.class, MetadataService.class})
@PowerMockIgnore("jdk.internal.reflect.*")
public class RequestObjectValidatorTest extends PowerMockTestCase {

    private CDSRequestObjectValidator cdsRequestObjectValidator = new CDSRequestObjectValidator();
    private Map<String, Object> scopeData = new HashMap<>();
    private static final String scopeString = "openid profile bank:accounts.basic:read bank:accounts.detail:read " +
            "bank:transactions:read bank:payees:read bank:regular_payments:read common:customer.basic:read " +
            "common:customer.detail:read cdr:registration";

    @BeforeClass
    public void beforeClass() {

        scopeData.put("scope", Arrays.asList(scopeString.split(" ")));
    }

    @Test
    public void checkRequestObjectWithValidScopes() throws Exception {

        OpenBankingValidator openBankingValidatorMock = mock(OpenBankingValidator.class);
        mockStatic(OpenBankingValidator.class);
        when(OpenBankingValidator.getInstance()).thenReturn(openBankingValidatorMock);
        when(openBankingValidatorMock.getFirstViolation(Mockito.anyObject())).thenReturn("");

        OpenBankingCDSConfigParser openBankingCDSConfigParserMock = mock(OpenBankingCDSConfigParser.class);
        mockStatic(OpenBankingCDSConfigParser.class);
        when(OpenBankingCDSConfigParser.getInstance()).thenReturn(openBankingCDSConfigParserMock);

        RequestObject requestObject = getObRequestObject(ReqObjectTestDataProvider.REQUEST_STRING);
        OBRequestObject obRequestObject = new OBRequestObject(requestObject);
        cdsRequestObjectValidator.validateOBConstraints(obRequestObject, scopeData);
    }

    @Test
    public void checkRequestObjectWithInvalidScopes() throws Exception {

        OpenBankingValidator openBankingValidatorMock = mock(OpenBankingValidator.class);
        mockStatic(OpenBankingValidator.class);
        when(OpenBankingValidator.getInstance()).thenReturn(openBankingValidatorMock);
        when(openBankingValidatorMock.getFirstViolation(Mockito.anyObject())).thenReturn("");

        RequestObject requestObject = getObRequestObject(ReqObjectTestDataProvider.SCOPES_INVALID_REQ);
        OBRequestObject obRequestObject = new OBRequestObject(requestObject);
        ValidationResponse response = cdsRequestObjectValidator.validateOBConstraints(obRequestObject, scopeData);
        Assert.assertEquals("No valid scopes found in the request", response.getViolationMessage());
    }

    @Test
    public void checkRequestObjectWithValidSharingDuration() throws Exception {

        RequestObject requestObject = getObRequestObject(ReqObjectTestDataProvider.REQUEST_STRING);
        OBRequestObject obRequestObject = new OBRequestObject(requestObject);

        CDSRequestObject cdsRequestObject = new CDSRequestObject(obRequestObject);
        SharingDurationValidator sharingDurationValidator = new SharingDurationValidator();
        boolean result = sharingDurationValidator.isValid(cdsRequestObject, null);
        Assert.assertTrue(result);
    }

    @Test
    public void testValidateOBConstraintsWithInvalidMetadata() throws Exception {

        OpenBankingValidator openBankingValidatorMock = mock(OpenBankingValidator.class);
        mockStatic(OpenBankingValidator.class);
        when(OpenBankingValidator.getInstance()).thenReturn(openBankingValidatorMock);
        when(openBankingValidatorMock.getFirstViolation(Mockito.anyObject())).thenReturn("");

        OpenBankingCDSConfigParser openBankingCDSConfigParserMock = mock(OpenBankingCDSConfigParser.class);
        when(openBankingCDSConfigParserMock.isMetadataCacheEnabled()).thenReturn(true);

        mockStatic(OpenBankingCDSConfigParser.class);
        when(OpenBankingCDSConfigParser.getInstance()).thenReturn(openBankingCDSConfigParserMock);

        stub(method(MetadataService.class, "shouldFacilitateConsentAuthorisation"))
                .toReturn(new MetadataValidationResponse("test error - invalid ADR"));

        RequestObject requestObject = getObRequestObject(ReqObjectTestDataProvider.REQUEST_STRING);
        OBRequestObject<?> obRequestObject = new OBRequestObject<>(requestObject);
        ValidationResponse response = cdsRequestObjectValidator.validateOBConstraints(obRequestObject, scopeData);

        Assert.assertTrue(StringUtils.isNotBlank(response.getViolationMessage()));
    }

    private OBRequestObject<?> getObRequestObject(String request) throws ParseException, RequestObjectException {
        RequestObject requestObject = new RequestObject();
        JOSEObject jwt = JOSEObject.parse(request);
        if (jwt.getHeader().getAlgorithm() == null || jwt.getHeader().getAlgorithm().equals(JWSAlgorithm.NONE)) {
            requestObject.setPlainJWT(PlainJWT.parse(request));
        } else {
            requestObject.setSignedJWT(SignedJWT.parse(request));
        }
        return new OBRequestObject<>(requestObject);
    }

}

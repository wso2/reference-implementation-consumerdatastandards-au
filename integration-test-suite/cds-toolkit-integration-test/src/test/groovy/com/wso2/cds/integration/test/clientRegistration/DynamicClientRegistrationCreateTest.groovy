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
package com.wso2.cds.integration.test.clientRegistration

import com.wso2.cds.test.framework.AUTest
import com.wso2.cds.test.framework.configuration.AUConfigurationService
import com.wso2.cds.test.framework.constant.AUConstants
import com.wso2.cds.test.framework.constant.ContextConstants
import com.wso2.cds.test.framework.request_builder.AUJWTGenerator
import com.wso2.cds.test.framework.request_builder.AURegistrationRequestBuilder
import com.wso2.cds.test.framework.utility.AUTestUtil
import io.restassured.http.ContentType
import org.testng.Assert
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.testng.ITestContext

import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDate
import java.time.ZoneOffset

/**
 *Test cases to validate DCR create request.
 */
class DynamicClientRegistrationCreateTest extends AUTest{

    AUJWTGenerator generator = new AUJWTGenerator()
    String clientId

    @BeforeClass
    void "Delete Application if exists"() {
        deleteApplicationIfExists(auConfiguration.getAppInfoClientID())
    }

    @Test(priority = 1,dependsOnMethods = "TC0101008_Verify Dynamic client registration test")
    void "TC0101009_Verify Get Application Access Token"(ITestContext context){

        // retrieve from context using key
        accessToken = getApplicationAccessToken(context.getAttribute(ContextConstants.CLIENT_ID).toString())
        Assert.assertNotNull(accessToken)
    }

    @Test(priority = 1)
    void "TC0101008_Verify Dynamic client registration test"(ITestContext context){

        jtiVal = String.valueOf(System.currentTimeMillis())
        AURegistrationRequestBuilder registrationRequestBuilder = new AURegistrationRequestBuilder()
        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(registrationRequestBuilder.getAURegularClaims())
                .when()
                .post(AUConstants.DCR_REGISTRATION_ENDPOINT)

        clientId = AUTestUtil.parseResponseBody(registrationResponse, AUConstants.CLIENT_ID)

        // add to context using key value pair
        context.setAttribute(ContextConstants.CLIENT_ID,clientId)
        AUTestUtil.writeToConfigFile(clientId)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_201)
        Assert.assertEquals(parseResponseBody(registrationResponse, "software_statement"),
                registrationRequestBuilder.getSSA())

    }

    @Test(priority = 1, dependsOnMethods = "TC0101008_Verify Dynamic client registration test")
    void "TC0101011_Create application with already available SSA"(ITestContext context) {

        AURegistrationRequestBuilder registrationRequestBuilder = new AURegistrationRequestBuilder()
        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(registrationRequestBuilder.getAURegularClaims())
                .when()
                .post(AUConstants.DCR_REGISTRATION_ENDPOINT)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR),
                AUConstants.INVALID_CLIENT_METADATA)
        Assert.assertTrue(parseResponseBody(registrationResponse, AUConstants.ERROR_DESCRIPTION).
                contains("Application with the name " + AUConstants.DCR_SOFTWARE_PRODUCT_ID +
                        " already exist in the system"))

        deleteApplicationIfExists(context.getAttribute(ContextConstants.CLIENT_ID).toString())
    }

    @Test
    void "TC0101001_Create application without Aud"() {

        AURegistrationRequestBuilder dcr = new AURegistrationRequestBuilder()
        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(dcr.getClaimsWithoutAud())
                .when()
                .post(AUConstants.DCR_REGISTRATION_ENDPOINT)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR),
                AUConstants.INVALID_CLIENT_METADATA)
    }

    @Test
    void "TC0101002_Create application with non matching redirect uris"() {

        AURegistrationRequestBuilder dcr = new AURegistrationRequestBuilder()
        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(dcr.getRegularClaimsWithNonMatchingRedirectUri())
                .when()
                .post(AUConstants.DCR_REGISTRATION_ENDPOINT)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR_DESCRIPTION),
                AUConstants.DCR_INVALID_REDIRECT_DESCRIPTION)
    }

    @Test
    void "TC0101003_Create application without TokenEndpointAuthSigningAlg"() {

        AURegistrationRequestBuilder dcr = new AURegistrationRequestBuilder()
        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(dcr.getClaimsWithoutTokenEndpointAuthSigningAlg())
                .when()
                .post(AUConstants.DCR_REGISTRATION_ENDPOINT)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR),
                AUConstants.INVALID_CLIENT_METADATA)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR_DESCRIPTION),
                AUConstants.DCR_WITHOUT_TOKEN_ENDPOINT_SIGNINGALGO)
    }

    @Test
    void "TC0101004_Create application without TokenEndpointAuthMethod"() {

        AURegistrationRequestBuilder dcr = new AURegistrationRequestBuilder()
        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(dcr.getClaimsWithoutTokenEndpointAuthMethod())
                .when()
                .post(AUConstants.DCR_REGISTRATION_ENDPOINT)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR_DESCRIPTION),
                AUConstants.DCR_WITHOUT_TOKEN_ENDPOINT_AUTHMETHOD)
    }

    @Test
    void "TC0101005_Create application without GrantTypes"() {

        AURegistrationRequestBuilder dcr = new AURegistrationRequestBuilder()
        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(dcr.getClaimsWithoutGrantTypes())
                .when()
                .post(AUConstants.DCR_REGISTRATION_ENDPOINT)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR_DESCRIPTION),
                AUConstants.DCR_GRANT_TYPES_NULL)
    }

    @Test
    void "TC0101006_Create application without ResponseTypes"() {

        AURegistrationRequestBuilder dcr = new AURegistrationRequestBuilder()
        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(dcr.getClaimsWithoutResponseTypes())
                .when()
                .post(AUConstants.DCR_REGISTRATION_ENDPOINT)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR),
                AUConstants.INVALID_CLIENT_METADATA)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR_DESCRIPTION),
                AUConstants.DCR_WITHOUT_RESPONSE_TYPES)
    }

    @Test
    void "TC0101007_Create application without SSA"() {

        AURegistrationRequestBuilder dcr = new AURegistrationRequestBuilder()
        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(dcr.getClaimsWithoutSSA())
                .when()
                .post(AUConstants.DCR_REGISTRATION_ENDPOINT)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR_DESCRIPTION),
                AUConstants.DCR_WITHOUT_SSA)
    }

    @Test
    void "TC0101012_Create application without ID Token Encrypted Response Algorithm"(ITestContext context) {

        deleteApplicationIfExists(clientId)
        AUConfigurationService auConfiguration = new AUConfigurationService()
        AURegistrationRequestBuilder dcr = new AURegistrationRequestBuilder()

        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(dcr.getClaimsWithoutIdTokenAlg())
                .when()
                .post(AUConstants.DCR_REGISTRATION_ENDPOINT)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_201)
        clientId = parseResponseBody(registrationResponse, AUConstants.CLIENT_ID)

        context.setAttribute(ContextConstants.CLIENT_ID,clientId)
        AUTestUtil.writeToConfigFile(clientId)

        deleteApplicationIfExists(clientId)
    }

    @Test
    void "TC0101013_Create application without ID Token Encrypted Response Encryption Method"(ITestContext context) {

        deleteApplicationIfExists(clientId)
        AUConfigurationService auConfiguration = new AUConfigurationService()
        AURegistrationRequestBuilder dcr = new AURegistrationRequestBuilder()

        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(dcr.getClaimsWithoutIdTokenEnc())
                .when()
                .post(AUConstants.DCR_REGISTRATION_ENDPOINT)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_201)
        clientId = parseResponseBody(registrationResponse, AUConstants.CLIENT_ID)

        context.setAttribute(ContextConstants.CLIENT_ID,clientId)
        AUTestUtil.writeToConfigFile(clientId)

        deleteApplicationIfExists(clientId)
    }

    @Test
    void "TC0101014_Create application with invalid ID Token Encrypted Response Algorithm"() {

        AURegistrationRequestBuilder dcr = new AURegistrationRequestBuilder()
        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(dcr.getClaimsWithInvalidIdTokenAlg())
                .when()
                .post(AUConstants.DCR_REGISTRATION_ENDPOINT)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR),
                AUConstants.INVALID_CLIENT_METADATA)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR_DESCRIPTION),
                AUConstants.DCR_INVALID_ID_TOKEN_ENCRYPTION_ALGO)
    }

    @Test
    void "TC0101015_Create application with invalid ID Token Encrypted Response Encryption Method"() {

        AURegistrationRequestBuilder dcr = new AURegistrationRequestBuilder()
        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(dcr.getClaimsWithInvalidIdTokenEnc())
                .when()
                .post(AUConstants.DCR_REGISTRATION_ENDPOINT)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR),
                AUConstants.INVALID_CLIENT_METADATA)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR_DESCRIPTION),
                AUConstants.DCR_INVALID_ID_TOKEN_ENCRYPTION_METHOD)
    }

    @Test
    void "TC0101016_Create application with different values for software ID in SSA and ISS in request JWT"() {

        AURegistrationRequestBuilder dcr = new AURegistrationRequestBuilder()
        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(dcr.getClaimsWithNonMatchingSoftwareIDandISS())
                .when()
                .post(AUConstants.DCR_REGISTRATION_ENDPOINT)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR),
                AUConstants.INVALID_CLIENT_METADATA)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR_DESCRIPTION),
                "Invalid issuer")
    }

    @Test
    void "TC0101017_Create application with a replayed JTI value in JWT request"() {

        deleteApplicationIfExists(clientId)
        jtiVal = String.valueOf(System.currentTimeMillis())
        AURegistrationRequestBuilder registrationRequestBuilder = new AURegistrationRequestBuilder()
        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(registrationRequestBuilder.getRegularClaimsWithGivenJti(jtiVal))
                .when()
                .post(AUConstants.DCR_REGISTRATION_ENDPOINT)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_201)

        clientId = parseResponseBody(registrationResponse, AUConstants.CLIENT_ID)

        registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(registrationRequestBuilder.getRegularClaimsWithGivenJti(jtiVal))
                .when()
                .post(AUConstants.DCR_REGISTRATION_ENDPOINT)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR),
                AUConstants.INVALID_CLIENT_METADATA)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR_DESCRIPTION),
                "JTI value of the registration request has been replayed")

        deleteApplicationIfExists(clientId)
    }

    @Test
    void "OB-1160_Create application with unsupported TokenEndpointAuthMethod"() {

        AURegistrationRequestBuilder dcr = new AURegistrationRequestBuilder()
        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(dcr.getClaimsWithUnsupportedTokenEndpointAuthMethod())
                .when()
                .post(AUConstants.DCR_REGISTRATION_ENDPOINT)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR),
                AUConstants.INVALID_CLIENT_METADATA)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR_DESCRIPTION),
                "Invalid tokenEndPointAuthentication provided")
    }

    @Test
    void "OB-1161_Create application with unsupported GrantTypes"() {

        AURegistrationRequestBuilder dcr = new AURegistrationRequestBuilder()
        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(dcr.getRegularClaimsWithInvalidGrantTypes())
                .when()
                .post(AUConstants.DCR_REGISTRATION_ENDPOINT)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR),
                AUConstants.INVALID_CLIENT_METADATA)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR_DESCRIPTION),
                "Invalid grantTypes provided")
    }

    @Test
    void "OB-1162_Create application with unsupported ResponseTypes"() {

        AURegistrationRequestBuilder dcr = new AURegistrationRequestBuilder()
        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(dcr.getRegularClaimsWithInvalidResponseTypes())
                .when()
                .post(AUConstants.DCR_REGISTRATION_ENDPOINT)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR),
                AUConstants.INVALID_CLIENT_METADATA)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR_DESCRIPTION),
                "Invalid responseTypes provided")
    }

    @Test
    void "OB-1163_Create application with unsupported ApplicationType"() {

        AURegistrationRequestBuilder dcr = new AURegistrationRequestBuilder()
        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(dcr.getRegularClaimsWithUnsupportedApplicationType())
                .when()
                .post(AUConstants.DCR_REGISTRATION_ENDPOINT)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR),
                AUConstants.INVALID_CLIENT_METADATA)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR_DESCRIPTION),
                "Invalid applicationType provided")
    }

    @Test
    void "OB-1164_Create application with malformed SSA"() {

        AURegistrationRequestBuilder dcr = new AURegistrationRequestBuilder()
        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(dcr.getRegularClaimsWithMalformedSSA())
                .when()
                .post(AUConstants.DCR_REGISTRATION_ENDPOINT)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR),
                AUConstants.INVALID_CLIENT_METADATA)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR_DESCRIPTION),
                "Malformed request JWT")
    }

    @Test(priority = 2)
    void "OB-1165_Create application without request_object_signing_alg"(ITestContext context) {

        AURegistrationRequestBuilder dcr = new AURegistrationRequestBuilder()

        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(dcr.getRegularClaimsWithoutRequestObjectSigningAlg())
                .when()
                .post(AUConstants.DCR_REGISTRATION_ENDPOINT)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_201)
        Assert.assertNotNull(parseResponseBody(registrationResponse, "request_object_signing_alg"))

        clientId = parseResponseBody(registrationResponse, AUConstants.CLIENT_ID)
        context.setAttribute(ContextConstants.CLIENT_ID,clientId)
        deleteApplicationIfExists(context.getAttribute(ContextConstants.CLIENT_ID).toString())

        registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(dcr.getAURegularClaims())
                .when()
                .post(AUConstants.DCR_REGISTRATION_ENDPOINT)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_201)
        clientId = parseResponseBody(registrationResponse, AUConstants.CLIENT_ID)

        context.setAttribute(ContextConstants.CLIENT_ID,clientId)
        AUTestUtil.writeToConfigFile(clientId)

        deleteApplicationIfExists(context.getAttribute(ContextConstants.CLIENT_ID).toString())
    }

    @Test
    void "OB-1166_Create application without redirect_uris"(ITestContext context) {

        deleteApplicationIfExists(clientId)
        AURegistrationRequestBuilder dcr = new AURegistrationRequestBuilder()
        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(dcr.getRegularClaimsWithoutRedirectUris())
                .when()
                .post(AUConstants.DCR_REGISTRATION_ENDPOINT)

        clientId = parseResponseBody(registrationResponse, AUConstants.CLIENT_ID)
        context.setAttribute(ContextConstants.CLIENT_ID,clientId)
        AUTestUtil.writeToConfigFile(clientId)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_201)
        Assert.assertNotNull(parseResponseBody(registrationResponse, "redirect_uris"))

        deleteApplicationIfExists(clientId)
    }

    @Test
    void "CDS-651_Create application with hybrid response type"() {

        jtiVal = String.valueOf(System.currentTimeMillis())
        AURegistrationRequestBuilder registrationRequestBuilder = new AURegistrationRequestBuilder()
        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(registrationRequestBuilder.getAURegularClaimsWithHybridResponseType())
                .when()
                .post(AUConstants.DCR_REGISTRATION_ENDPOINT)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR),
                AUConstants.INVALID_CLIENT_METADATA)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR_DESCRIPTION),
                "Invalid responseTypes provided")
    }

    @Test
    void "CDS-1104_Create application with invalid TokenEndpointAuthSigningAlgorithm"() {

        AURegistrationRequestBuilder dcr = new AURegistrationRequestBuilder()
        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(dcr.getRegularClaimsWithInvalidTokenAuthSignAlg())
                .when()
                .post(AUConstants.DCR_REGISTRATION_ENDPOINT)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR),
                AUConstants.INVALID_CLIENT_METADATA)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR_DESCRIPTION),
                AUConstants.INVALID_SIGNING_ALG)
    }

    @Test
    void "CDS-1105_Create application with invalid Aud"() {

        AURegistrationRequestBuilder dcr = new AURegistrationRequestBuilder()
        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(dcr.getClaimsWithInvalidAud())
                .when()
                .post(AUConstants.DCR_REGISTRATION_ENDPOINT)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR),
                AUConstants.INVALID_CLIENT_METADATA)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR_DESCRIPTION),
                AUConstants.INVALID_AUDIENCE_ERROR)
    }

    @Test
    void "CDS-1106_Create application without ApplicationType"() {

        AURegistrationRequestBuilder dcr = new AURegistrationRequestBuilder()
        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(dcr.getRegularClaimsWithoutApplicationType())
                .when()
                .post(AUConstants.DCR_REGISTRATION_ENDPOINT)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_201)
        deleteApplicationIfExists(clientId)
    }

    @Test
    void "CDS-673_DCR registration request with localhost url in the SSA"(ITestContext context) {

        Path dcrArtifactsPath = Paths.get(auConfiguration.getAppDCRSSAPath())
        String filePath = Paths.get(dcrArtifactsPath.getParent().toString(), "ssa_localhost.txt")

        jtiVal = String.valueOf(System.currentTimeMillis())
        AURegistrationRequestBuilder registrationRequestBuilder = new AURegistrationRequestBuilder()
        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(registrationRequestBuilder
                        .getAURegularClaims(auConfiguration.getAppDCRSoftwareId(), AUTestUtil.readFileContent(filePath),
                                AUConstants.LOCALHOST_REDIRECT_URL))
                .when()
                .post(AUConstants.DCR_REGISTRATION_ENDPOINT)

        clientId = AUTestUtil.parseResponseBody(registrationResponse, AUConstants.CLIENT_ID)

        context.setAttribute(ContextConstants.CLIENT_ID,clientId)
        AUTestUtil.writeToConfigFile(clientId)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_201)
        Assert.assertEquals(parseResponseBody(registrationResponse, "software_statement"),
                AUTestUtil.readFileContent(filePath))
        Assert.assertTrue(parseResponseBody(registrationResponse, "redirect_uris")
                .contains(AUConstants.LOCALHOST_REDIRECT_URL))

        deleteApplicationIfExists(clientId)
    }

    @Test
    void "CDS-674_DCR registration request with different hostnames for redirect url in SSA"() {

        Path dcrArtifactsPath = Paths.get(auConfiguration.getAppDCRSSAPath())
        String filePath = Paths.get(dcrArtifactsPath.getParent().toString(), "ssa_differentHostNames.txt")

        jtiVal = String.valueOf(System.currentTimeMillis())
        AURegistrationRequestBuilder registrationRequestBuilder = new AURegistrationRequestBuilder()
        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(registrationRequestBuilder
                        .getAURegularClaims(auConfiguration.getAppDCRSoftwareId(), AUTestUtil.readFileContent(filePath),
                                AUConstants.LOCALHOST_REDIRECT_URL))
                .when()
                .post(AUConstants.DCR_REGISTRATION_ENDPOINT)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR),
                AUConstants.INVALID_CLIENT_METADATA)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR_DESCRIPTION),
                AUConstants.ERROR_REDIRECT_URL_WITH_DIFF_HOSTNAMES)
    }

    @Test
    void "Create application without redirect uri in SSA"() {

        Path dcrArtifactsPath = Paths.get(auConfiguration.getAppDCRSSAPath())
        String filePath = Paths.get(dcrArtifactsPath.getParent().toString(), "ssa_withoutRedirectUrl.txt")

        jtiVal = String.valueOf(System.currentTimeMillis())
        AURegistrationRequestBuilder registrationRequestBuilder = new AURegistrationRequestBuilder()
        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(registrationRequestBuilder
                        .getAURegularClaims(auConfiguration.getAppDCRSoftwareId(), AUTestUtil.readFileContent(filePath),
                                AUConstants.LOCALHOST_REDIRECT_URL))
                .when()
                .post(AUConstants.DCR_REGISTRATION_ENDPOINT)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR),
                AUConstants.INVALID_CLIENT_METADATA)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR_DESCRIPTION),
                AUConstants.ERROR_EMPTY_REDIRECT_URL_IN_SSA)
    }

    @Test
    void "CDS-1108_Create application with invalid request_object_signing_alg"() {

        AURegistrationRequestBuilder dcr = new AURegistrationRequestBuilder()
        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(dcr.getRegularClaimsWithInvalidRequestObjectSigningAlg())
                .when()
                .post(AUConstants.DCR_REGISTRATION_ENDPOINT)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR),
                AUConstants.INVALID_CLIENT_METADATA)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR_DESCRIPTION),
                AUConstants.INVALID_SIGNING_ALG)
    }

    @Test
    void "CDS-1109_Create application with invalid id_token_signed_response_alg"() {

        AURegistrationRequestBuilder dcr = new AURegistrationRequestBuilder()
        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(dcr.getRegularClaimsWithInvalidIdTokenSigningResponseAlg())
                .when()
                .post(AUConstants.DCR_REGISTRATION_ENDPOINT)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR),
                AUConstants.INVALID_CLIENT_METADATA)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR_DESCRIPTION),
                AUConstants.INVALID_SIGNING_ALG)
    }

    @Test
    void "CDS-1110_Create application without id_token_signed_response_alg"() {

        AURegistrationRequestBuilder dcr = new AURegistrationRequestBuilder()
        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(dcr.getRegularClaimsWithoutIdTokenSigningResponseAlg())
                .when()
                .post(AUConstants.DCR_REGISTRATION_ENDPOINT)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR),
                AUConstants.INVALID_CLIENT_METADATA)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR_DESCRIPTION),
                AUConstants.ERROR_WITHOUT_IDTOKEN_SINGED_ALG)
    }

    @Test (priority = 2, groups = "SmokeTest")
    void "CDS-476_Create application without ID_Token Response Type and verify id_token encryption not Mandatory"() {

        deleteApplicationIfExists(clientId)
        AUConfigurationService auConfiguration = new AUConfigurationService()
        AURegistrationRequestBuilder dcr = new AURegistrationRequestBuilder()

        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(dcr.getClaimsWithoutIdTokenEnc())
                .when()
                .post(AUConstants.DCR_REGISTRATION_ENDPOINT)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_201)
        clientId = parseResponseBody(registrationResponse, AUConstants.CLIENT_ID)

        deleteApplicationIfExists(clientId)
    }

    @Test
    void "CDS-1111_Create application with unsupported content-type"() {

        jtiVal = String.valueOf(System.currentTimeMillis())
        AURegistrationRequestBuilder registrationRequestBuilder = new AURegistrationRequestBuilder()
        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(registrationRequestBuilder.getAURegularClaims())
                .contentType(ContentType.JSON)
                .post(AUConstants.DCR_REGISTRATION_ENDPOINT)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_415)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR_DESCRIPTION),
                AUConstants.INVALID_CONTENT_TYPE)
    }

    @Test
    void "CDS-1112_Create application with expired Request JWT"() {

        Long expiredDate = LocalDate.now().minusDays(1).atTime(currentTime, 00, 00)
                .toEpochSecond(ZoneOffset.UTC)

        AURegistrationRequestBuilder dcr = new AURegistrationRequestBuilder()
        def registrationResponse = AURegistrationRequestBuilder
                .buildRegistrationRequest(dcr.getExpiredRequestClaims(expiredDate))
                .when()
                .post(AUConstants.DCR_REGISTRATION_ENDPOINT)

        Assert.assertEquals(registrationResponse.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(parseResponseBody(registrationResponse, AUConstants.ERROR),
                AUConstants.INVALID_CLIENT_METADATA)
        //TODO: Add Error Description after fixing https://github.com/wso2-enterprise/ob-compliance-toolkit-cds/issues/403
    }

}


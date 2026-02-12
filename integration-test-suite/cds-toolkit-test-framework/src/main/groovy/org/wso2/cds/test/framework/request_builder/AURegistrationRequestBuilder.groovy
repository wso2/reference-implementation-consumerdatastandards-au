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

package org.wso2.cds.test.framework.request_builder

import com.nimbusds.oauth2.sdk.ResponseType
import org.wso2.bfsi.test.framework.request_builder.RegistrationRequestBuilder
import org.wso2.cds.test.framework.configuration.AUConfigurationService
import io.restassured.RestAssured
import io.restassured.config.EncoderConfig
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import org.json.JSONArray
import org.wso2.cds.test.framework.utility.AUMockCDRIntegrationUtil
import org.wso2.cds.test.framework.utility.AURestAsRequestBuilder
import org.wso2.cds.test.framework.utility.AUTestUtil
import org.wso2.cds.test.framework.constant.AUConstants

/**
 * Class that provides DCR functions for tests
 */
class AURegistrationRequestBuilder extends RegistrationRequestBuilder {

    private AUConfigurationService auConfiguration

    private String SSA
    private String softwareProductId

    AURegistrationRequestBuilder() {
        auConfiguration = new AUConfigurationService()
    }

    /**
     * Provide Software ID
     * Helper function for other functions
     * @return
     */
    String getSoftwareID() {
        if (softwareProductId == null) {
            softwareProductId = ((auConfiguration.getMockCDREnabled())
                    ? AUConstants.MOCK_ADR_BRAND_ID_1_SOFTWARE_PRODUCT_1 : AUConstants.DCR_SOFTWARE_PRODUCT_ID)
        }
        return softwareProductId
    }

    /**
     * Provide SSA from file
     * Helper function for other functions
     * @return
     */
    String getSSA() {
        if (SSA == null) {
            if (auConfiguration.getMockCDREnabled()) {
                SSA = AUMockCDRIntegrationUtil.getSSAFromMockCDRRegister(AUConstants.MOCK_ADR_BRAND_ID_1, softwareProductId)
            } else {
                SSA = new File(auConfiguration.getAppDCRSSAPath()).text
            }
        }
        return SSA
    }

    void setSoftwareID(String id) {
        softwareProductId = id
    }

    void setSSA(String statement) {
        SSA = statement
    }

    /**
     * Provide subscription payload for DCR
     * @return
     */
    static String getSubscriptionPayload(String applicationId, String apiId) {
        return """
            {
              "applicationId": "$applicationId",
              "apiId": "$apiId",
              "throttlingPolicy": "Unlimited"
            }
            """.stripIndent()
    }

    /**
     * Provide regular payload for DCR
     * @return
     */
    String getAURegularClaims() {
        return regularClaims.addIssuer(getSoftwareID()).addSoftwareStatement(getSSA())
                .removeKeyValue(AUConstants.RESPONSE_TYPES_KEY).addResponseType(ResponseType.CODE.toString())
                .addIDTokenEncResponseAlg().addIDTokenEncResponseEnc()
                .addCustomValue(AUConstants.DCR_CLAIM_LEGAL_ENTITY_ID, AUConstants.SAMPLE_LEGAL_ENTITY_ID)
                .addCustomValue(AUConstants.DCR_CLAIM_LEGAL_ENTITY_NAME, AUConstants.SAMPLE_LEGAL_ENTITY_NAME)
                .getClaimsJsonAsString()
    }

    /**
     * Provide regular payload for DCR
     * @return
     * @param softID
     * @param ssa
     * @return
     */
    String getAURegularClaims(String softID, String ssa) {
        return regularClaims.addIssuer(softID).addSoftwareStatement(ssa)
                .removeKeyValue(AUConstants.RESPONSE_TYPES_KEY).addResponseType(ResponseType.CODE.toString())
                .addIDTokenEncResponseAlg().addIDTokenEncResponseEnc().getClaimsJsonAsString()
    }

    /**
     * Provide regular payload for DCR
     * @return
     * @param softID
     * @param ssa
     * @param redirectURI
     * @return
     */
    String getAURegularClaims(String softID, String ssa, String redirectURI) {
        return regularClaims.addIssuer(softID).addSoftwareStatement(ssa).addCustomRedirectURI(new JSONArray().put(redirectURI))
                .removeKeyValue(AUConstants.RESPONSE_TYPES_KEY).addResponseType(ResponseType.CODE.toString())
                .addIDTokenEncResponseAlg().addIDTokenEncResponseEnc().getClaimsJsonAsString()
    }

    String getClaimsWithoutAud() {
        return regularClaims.addIssuer(getSoftwareID()).addSoftwareStatement(getSSA()).addIDTokenEncResponseAlg()
                .addIDTokenEncResponseEnc().removeKeyValue(AUConstants.RESPONSE_TYPES_KEY).addResponseType(ResponseType.CODE.toString())
                .removeKeyValue(AUConstants.AUDIENCE_KEY).getClaimsJsonAsString()
    }

    String getRegularClaimsWithNonMatchingRedirectUri(String softID = getSoftwareID(), String ssa = getSSA()) {
        return regularClaims.addIssuer(softID).addSoftwareStatement(ssa)
                .addCustomRedirectURI(new JSONArray().put("https://www.google.com/redirects/non-matching-redirect"))
                .removeKeyValue(AUConstants.RESPONSE_TYPES_KEY).addResponseType(ResponseType.CODE.toString())
                .addIDTokenEncResponseAlg().addIDTokenEncResponseEnc().getClaimsJsonAsString()
    }

    String getRegularClaimsWithNewRedirectUri() {
        return regularClaims.addIssuer(getSoftwareID()).addSoftwareStatement(getSSA())
                .addCustomRedirectURI(new JSONArray().put(AUConstants.DCR_ALTERNATE_REDIRECT_URI))
                .removeKeyValue(AUConstants.RESPONSE_TYPES_KEY).addResponseType(ResponseType.CODE.toString())
                .addIDTokenEncResponseAlg().addIDTokenEncResponseEnc().getClaimsJsonAsString()
    }

    String getClaimsWithoutTokenEndpointAuthSigningAlg() {
        return regularClaims.addIssuer(getSoftwareID()).addSoftwareStatement(getSSA())
                .addIDTokenEncResponseAlg().addIDTokenEncResponseEnc()
                .removeKeyValue(AUConstants.RESPONSE_TYPES_KEY).addResponseType(ResponseType.CODE.toString())
                .removeKeyValue(AUConstants.TOKEN_ENDPOINT_AUTH_SIGNING_ALG_KEY).getClaimsJsonAsString()
    }

    String getClaimsWithoutTokenEndpointAuthMethod() {
        return regularClaims.addIssuer(getSoftwareID()).addSoftwareStatement(getSSA())
                .addIDTokenEncResponseAlg().addIDTokenEncResponseEnc()
                .removeKeyValue(AUConstants.RESPONSE_TYPES_KEY).addResponseType(ResponseType.CODE.toString())
                .removeKeyValue(AUConstants.TOKEN_ENDPOINT_AUTH_METHOD_KEY).getClaimsJsonAsString()
    }

    String getClaimsWithoutGrantTypes() {
        return regularClaims.addIssuer(getSoftwareID()).addSoftwareStatement(getSSA())
                .addIDTokenEncResponseAlg().addIDTokenEncResponseEnc()
                .removeKeyValue(AUConstants.RESPONSE_TYPES_KEY).addResponseType(ResponseType.CODE.toString())
                .removeKeyValue(AUConstants.GRANT_TYPES_KEY).getClaimsJsonAsString()
    }

    String getClaimsWithoutResponseTypes() {
        return regularClaims.addIssuer(getSoftwareID()).addSoftwareStatement(getSSA())
                .removeKeyValue(AUConstants.RESPONSE_TYPES_KEY).addIDTokenEncResponseAlg().addIDTokenEncResponseEnc()
                .removeKeyValue(AUConstants.RESPONSE_TYPES_KEY).getClaimsJsonAsString()
    }

    String getClaimsWithoutSSA() {
        return regularClaims.addIssuer(getSoftwareID())
                .addIDTokenEncResponseAlg().addIDTokenEncResponseEnc()
                .removeKeyValue(AUConstants.RESPONSE_TYPES_KEY).addResponseType(ResponseType.CODE.toString())
                .removeKeyValue(AUConstants.SOFTWARE_STATEMENT_KEY).getClaimsJsonAsString()
    }

    String getClaimsWithoutIdTokenAlg(String softID = getSoftwareID(), String ssa = getSSA()) {
        return regularClaims.addIssuer(softID).addSoftwareStatement(ssa)
                .removeKeyValue(AUConstants.RESPONSE_TYPES_KEY).addResponseType(ResponseType.CODE.toString())
                .addIDTokenEncResponseEnc().getClaimsJsonAsString()
    }

    String getClaimsWithoutIdTokenEnc(String softID = getSoftwareID(), String ssa = getSSA()) {
        return regularClaims.addIssuer(softID).addSoftwareStatement(ssa)
                .removeKeyValue(AUConstants.RESPONSE_TYPES_KEY).addResponseType(ResponseType.CODE.toString())
                .addIDTokenEncResponseAlg().getClaimsJsonAsString()
    }

    String getClaimsWithInvalidIdTokenAlg() {
        return regularClaims.addIssuer(getSoftwareID()).addSoftwareStatement(getSSA())
                .removeKeyValue(AUConstants.RESPONSE_TYPES_KEY).addResponseType(ResponseType.CODE.toString())
                .addIDTokenEncResponseAlg("RSA-OAEP-X").addIDTokenEncResponseEnc()
                .getClaimsJsonAsString()
    }

    String getClaimsWithInvalidIdTokenEnc() {
        return regularClaims.addIssuer(getSoftwareID()).addSoftwareStatement(getSSA())
                .removeKeyValue(AUConstants.RESPONSE_TYPES_KEY).addResponseType(ResponseType.CODE.toString())
                .addIDTokenEncResponseAlg().addIDTokenEncResponseEnc("A128GCM")
                .getClaimsJsonAsString()
    }

    String getClaimsWithNonMatchingSoftwareIDandISS() {
        return regularClaims.addIssuer("Mock Company").addSoftwareStatement(getSSA())
                .removeKeyValue(AUConstants.RESPONSE_TYPES_KEY).addResponseType(ResponseType.CODE.toString())
                .addIDTokenEncResponseAlg().addIDTokenEncResponseEnc()
                .getClaimsJsonAsString()
    }

    String getRegularClaimsWithGivenJti(String jti, String softID = getSoftwareID(), String ssa = getSSA()) {
        return regularClaims.addIssuer(softID).addSoftwareStatement(ssa)
                .removeKeyValue(AUConstants.RESPONSE_TYPES_KEY).addResponseType(ResponseType.CODE.toString())
                .addIDTokenEncResponseAlg().addIDTokenEncResponseEnc()
                .addCustomValue(AUConstants.DCR_CLAIM_LEGAL_ENTITY_ID, AUConstants.SAMPLE_LEGAL_ENTITY_ID)
                .addCustomValue(AUConstants.DCR_CLAIM_LEGAL_ENTITY_NAME, AUConstants.SAMPLE_LEGAL_ENTITY_NAME)
                .removeKeyValue(AUConstants.JTI_KEY).addJti(jti)
                .getClaimsJsonAsString()
    }

    String getClaimsWithUnsupportedTokenEndpointAuthMethod() {
        return regularClaims.addIssuer(getSoftwareID()).addSoftwareStatement(getSSA())
                .removeKeyValue(AUConstants.RESPONSE_TYPES_KEY).addResponseType(ResponseType.CODE.toString())
                .addIDTokenEncResponseAlg().addIDTokenEncResponseEnc()
                .addTokenEndpointAuthMethod("mutual_tls").getClaimsJsonAsString()
    }

    String getRegularClaimsWithInvalidGrantTypes() {
        return regularClaims.addIssuer(getSoftwareID()).addSoftwareStatement(getSSA())
                .removeKeyValue(AUConstants.RESPONSE_TYPES_KEY).addResponseType(ResponseType.CODE.toString())
                .addIDTokenEncResponseAlg().addIDTokenEncResponseEnc()
                .addGrantType(new JSONArray().put("urn:ietf:params:oauth:grant-type:jwt-bearer")).getClaimsJsonAsString()
    }

    String getRegularClaimsWithInvalidResponseTypes() {
        return regularClaims.addIssuer(getSoftwareID()).addSoftwareStatement(getSSA())
                .addIDTokenEncResponseAlg().addIDTokenEncResponseEnc()
                .addCustomResponseTypes(new JSONArray().put("id_token")).getClaimsJsonAsString()
    }

    String getRegularClaimsWithUnsupportedApplicationType() {
        return regularClaims.addIssuer(getSoftwareID()).addSoftwareStatement(getSSA())
                .removeKeyValue(AUConstants.RESPONSE_TYPES_KEY).addResponseType(ResponseType.CODE.toString())
                .addIDTokenEncResponseAlg().addIDTokenEncResponseEnc()
                .addApplicationType("Mobile").getClaimsJsonAsString()
    }

    String getRegularClaimsWithoutRequestObjectSigningAlg(String softID = getSoftwareID(), String ssa = getSSA()) {
        return regularClaims.addIssuer(softID).addSoftwareStatement(ssa)
                .removeKeyValue(AUConstants.RESPONSE_TYPES_KEY).addResponseType(ResponseType.CODE.toString())
                .addIDTokenEncResponseAlg().addIDTokenEncResponseEnc()
                .removeKeyValue(AUConstants.REQUEST_OBJECT_SIGNING_ALG_KEY).getClaimsJsonAsString()
    }

    String getRegularClaimsWithMalformedSSA() {
        return regularClaims.addIssuer(getSoftwareID()).addSoftwareStatement("fejkfhweuifhuiweufwhfweiio")
                .removeKeyValue(AUConstants.RESPONSE_TYPES_KEY).addResponseType(ResponseType.CODE.toString())
                .addIDTokenEncResponseAlg().addIDTokenEncResponseEnc()
                .getClaimsJsonAsString()
    }

    String getRegularClaimsWithoutRedirectUris(String softID, String ssa) {
        return regularClaims.addIssuer(softID).addSoftwareStatement(ssa)
                .removeKeyValue(AUConstants.RESPONSE_TYPES_KEY).addResponseType(ResponseType.CODE.toString())
                .addIDTokenEncResponseAlg().addIDTokenEncResponseEnc()
                .removeKeyValue(AUConstants.REDIRECT_URIS_KEY).getClaimsJsonAsString()
    }

    String getRegularClaimsWithFieldsNotSupported(String softID, String ssa) {
        return regularClaims.addIssuer(softID).addSoftwareStatement(ssa)
                .removeKeyValue(AUConstants.RESPONSE_TYPES_KEY).addResponseType(ResponseType.CODE.toString())
                .addIDTokenEncResponseAlg().addIDTokenEncResponseEnc()
                .addCustomValue("adr_name", "ADR").getClaimsJsonAsString()
    }

    String getRegularClaimsWithInvalidTokenAuthSignAlg() {
        return regularClaims.addIssuer(getSoftwareID()).addSoftwareStatement(getSSA())
                .removeKeyValue(AUConstants.RESPONSE_TYPES_KEY).addResponseType(ResponseType.CODE.toString())
                .addTokenEndpointAuthSignAlg("ES512")
                .addIDTokenEncResponseAlg().addIDTokenEncResponseEnc()
                .getClaimsJsonAsString()
    }

    String getClaimsWithInvalidAud() {
        return regularClaims.addIssuer(getSoftwareID()).addSoftwareStatement(getSSA()).addIDTokenEncResponseAlg()
                .addIDTokenEncResponseEnc().removeKeyValue(AUConstants.RESPONSE_TYPES_KEY).addResponseType(ResponseType.CODE.toString())
                .addAudience("https://obiam:9446/client-registration").getClaimsJsonAsString()
    }

    String getRegularClaimsWithoutApplicationType(String softID, String ssa) {
        return regularClaims.addIssuer(softID).addSoftwareStatement(ssa)
                .removeKeyValue(AUConstants.RESPONSE_TYPES_KEY).addResponseType(ResponseType.CODE.toString())
                .addIDTokenEncResponseAlg().addIDTokenEncResponseEnc()
                .removeKeyValue(AUConstants.APPLICATION_TYPE_KEY).getClaimsJsonAsString()
    }

    String getRegularClaimsWithInvalidRequestObjectSigningAlg() {
        return regularClaims.addIssuer(getSoftwareID()).addSoftwareStatement(getSSA())
                .removeKeyValue(AUConstants.RESPONSE_TYPES_KEY).addResponseType(ResponseType.CODE.toString())
                .addIDTokenEncResponseAlg().addIDTokenEncResponseEnc()
                .addRequestObjectSigningAlgo("ES512").getClaimsJsonAsString()
    }

    String getRegularClaimsWithInvalidIdTokenSigningResponseAlg() {
        return regularClaims.addIssuer(getSoftwareID()).addSoftwareStatement(getSSA())
                .removeKeyValue(AUConstants.RESPONSE_TYPES_KEY).addResponseType(ResponseType.CODE.toString())
                .addIDTokenEncResponseAlg().addIDTokenEncResponseEnc()
                .addIDTokenSignedResponseAlg("ES512").getClaimsJsonAsString()
    }

    String getRegularClaimsWithoutIdTokenSigningResponseAlg() {
        return regularClaims.addIssuer(getSoftwareID()).addSoftwareStatement(getSSA())
                .removeKeyValue(AUConstants.RESPONSE_TYPES_KEY).addResponseType(ResponseType.CODE.toString())
                .addIDTokenEncResponseAlg().addIDTokenEncResponseEnc()
                .removeKeyValue(AUConstants.ID_TOKEN_SIGNED_RESPONSE_ALG_KEY).getClaimsJsonAsString()
    }

    String getExpiredRequestClaims(long date) {
        return regularClaims.addIssuer(getSoftwareID()).addSoftwareStatement(getSSA())
                .removeKeyValue(AUConstants.RESPONSE_TYPES_KEY).addResponseType(ResponseType.CODE.toString())
                .addIDTokenEncResponseAlg().addIDTokenEncResponseEnc()
                .addExpireDate(date)
                .getClaimsJsonAsString()
    }

    /**
     * Provide regular payload with Hybrid Response Type for DCR
     * @return
     */
    String getAURegularClaimsWithHybridResponseType() {
        return regularClaims.addIssuer(getSoftwareID()).addSoftwareStatement(getSSA())
                .removeKeyValue(AUConstants.RESPONSE_TYPES_KEY).addResponseType(ResponseType.CODE_IDTOKEN.toString())
                .addIDTokenEncResponseAlg().addIDTokenEncResponseEnc().getClaimsJsonAsString()
    }

    /**
     * Get a basic request.
     *
     * @param accessToken
     * @return
     */
    static RequestSpecification buildBasicRequest(String accessToken) {

        return AURestAsRequestBuilder.buildRequest()
                .header("charset", "UTF-8")
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, "${AUConstants.AUTHORIZATION_BEARER_TAG}${accessToken}")
                .accept("application/json")
                .config(RestAssured.config()
                        .sslConfig(RestAssured.config().getSSLConfig().sslSocketFactory(AUTestUtil.getSslSocketFactory()))
                        .encoderConfig(new EncoderConfig().encodeContentTypeAs(
                                "application/jwt", ContentType.TEXT)))
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.DCR_BASE_PATH_TYPE))
    }

    /**
     * Get a registration request for application creation.
     *
     * @param accessToken
     * @return
     */
    static RequestSpecification buildRegistrationRequest(String claims) {
        AUJWTGenerator generator = new AUJWTGenerator()
        return AURestAsRequestBuilder.buildRequest()
                .contentType("application/jwt")
                .body(generator.getSignedRequestObject(claims))
                .accept("application/json")
                .config(RestAssured.config()
                        .sslConfig(RestAssured.config().getSSLConfig().sslSocketFactory(AUTestUtil.getSslSocketFactory()))
                        .encoderConfig(new EncoderConfig().encodeContentTypeAs(
                                "application/jwt", ContentType.TEXT)))
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.DCR_BASE_PATH_TYPE))
    }

}


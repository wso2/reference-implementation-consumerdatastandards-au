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

import com.nimbusds.oauth2.sdk.http.HTTPResponse
import org.testng.Assert
import org.wso2.cds.test.framework.configuration.APIConfigurationService
import org.wso2.cds.test.framework.configuration.AUConfigurationService
import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.utility.AURestAsRequestBuilder
import org.wso2.cds.test.framework.utility.AUTestUtil

import java.nio.charset.Charset

/**
 * Generate Custom Key Manager.
 */
class RESTKeyManagerRequestBuilder {

    private static APIConfigurationService apiConfiguration
    private static AUConfigurationService auConfiguration

    String clientId
    String clientSecret
    String baseURL = AUConstants.REST_API_ADMIN_ENDPOINT
    String keyManagerId
    String residentKeyMangerId

    RESTKeyManagerRequestBuilder() {
        auConfiguration = new AUConfigurationService()
        apiConfiguration = new APIConfigurationService()
    }

    /**
     * Create rest_api_admin service provider.
     */
    void createDCRApplication() {
        URI dcrEndpoint = new URI("${auConfiguration.getServerGatewayURL()}" + AUConstants.REST_API_CLIENT_REGISTRATION_ENDPOINT)
        def response = AURestAsRequestBuilder.buildRequest()
                .contentType(AUConstants.CONTENT_TYPE_APPLICATION_JSON)
                .header(AUConstants.AUTHORIZATION_HEADER_KEY,
                        generateBasicHeader(auConfiguration.getUserPublisherName(), auConfiguration.getUserPublisherPWD()))
                .body(getDCRPayload())
                .post(dcrEndpoint.toString())
        Assert.assertEquals(response.statusCode(), HTTPResponse.SC_OK)
        clientId = AUTestUtil.parseResponseBody(response, "clientId")
        clientSecret = AUTestUtil.parseResponseBody(response, "clientSecret")
    }

    /**
     * Generate Application Access Token.
     * @return
     */
    String obtainAccessToken() {
        URI tokenEndpoint = new URI("${auConfiguration.getServerGatewayURL()}/oauth2/token")
        def response = AURestAsRequestBuilder.buildRequest()
                .param(AUConstants.GRANT_TYPE_KEY, AUConstants.PASSWORD_GRANT)
                .param(AUConstants.SCOPE_KEY, "openid apim:admin apim:tier_view apim:tier_manage apim:bl_view" +
                        "apim:bl_manage apim:mediation_policy_view apim:mediation_policy_create apim:app_owner_change" +
                        "apim:app_import_export apim:api_import_export apim:api_product_import_export apim:environment_manage" +
                        "apim:environment_read apim:monetization_usage_publish apim:api_workflow_approve apim:bot_data" +
                        "apim:tenantInfo apim:tenant_theme_manage apim:admin_operations apim:admin_settings " +
                        "apim:admin_alert_manage apim:api_workflow_view apim:scope_manage apim:role_manage")
                .param(AUConstants.USER_NAME, auConfiguration.getUserKeyManagerAdminName())
                .param(AUConstants.PASSWORD, auConfiguration.getUserKeyManagerAdminPWD())
                .header(AUConstants.AUTHORIZATION_HEADER_KEY,
                        generateBasicHeader(clientId, clientSecret))
                .post(tokenEndpoint.toString())
        Assert.assertEquals(response.statusCode(), HTTPResponse.SC_OK)
        return AUTestUtil.parseResponseBody(response, AUConstants.ACCESS_TOKEN)
    }

    /**
     * Generate Basic Header.
     * @param username
     * @param password
     * @return
     */
    static String generateBasicHeader(String username, String password) {
        String authToken = "${username}:${password}"
        return "Basic ${Base64.encoder.encodeToString(authToken.getBytes(Charset.defaultCharset()))}"
    }

    /**
     * Get Application Payload.
     * @return
     */
    static String getDCRPayload() {
        return """
             {
             "callbackUrl":"www.google.lk",
             "clientName":"rest_api_admin",
             "owner":"admin@wso2.com",
             "grantType":"client_credentials password refresh_token",
             "saasApp":true
             }
             """.stripIndent()
    }

    /**
     * Add Custom Key Manager.
     * @param accessToken
     */
    void addKeyManager(String accessToken){

        URI keyManagerEndpoint = new URI("${auConfiguration.getServerGatewayURL()}" + baseURL + "key-managers")
        def response = AURestAsRequestBuilder.buildRequest()
                .contentType(AUConstants.CONTENT_TYPE_APPLICATION_JSON)
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, AUConstants.AUTHORIZATION_BEARER_TAG + accessToken)
                .body(getKeyManagerPayload())
                .post(keyManagerEndpoint.toString())

        Assert.assertEquals(response.statusCode(), HTTPResponse.SC_CREATED)
        keyManagerId = AUTestUtil.parseResponseBody(response, "id")
    }

    /**
     * Get Key Manager Payload.
     * @return
     */
    static String getKeyManagerPayload() {

        String isServerUrl = auConfiguration.getServerAuthorisationServerURL()

        return """
            {
              "name": "OBKM",
              "displayName": "OBKM",
              "type": "ObKeyManager",
              "description": "This is a custom key manager for Open Banking",
              "wellKnownEndpoint": "${isServerUrl}/oauth2/token/.well-known/openid-configuration",
              "introspectionEndpoint": "${isServerUrl}/oauth2/introspect",
              "clientRegistrationEndpoint": "${isServerUrl}/keymanager-operations/dcr/register",
              "tokenEndpoint": "${isServerUrl}/oauth2/token",
              "displayTokenEndpoint": "${isServerUrl}/oauth2/token",
              "revokeEndpoint": "${isServerUrl}/oauth2/revoke",
              "displayRevokeEndpoint": "${isServerUrl}/oauth2/revoke",
              "userInfoEndpoint": "${isServerUrl}/oauth2/userinfo?schema=openid",
              "authorizeEndpoint": "${isServerUrl}/oauth2/authorize",
              "certificates": {
                "type": "JWKS",
                "value": "${isServerUrl}/oauth2/jwks"
              },
              "issuer": "${isServerUrl}/oauth2/token",
              "scopeManagementEndpoint": "${isServerUrl}/api/identity/oauth2/v1.0/scopes",
              "availableGrantTypes": [
                "client_credentials", "refresh_token", "password", "authorization_code"
              ],
              "enableTokenGeneration": true,
              "enableTokenEncryption": false,
              "enableTokenHashing": false,
              "enableMapOAuthConsumerApps": true,
              "enableOAuthAppCreation": true,
              "enableSelfValidationJWT": true,
              "claimMapping": [
                {
                  "remoteClaim": "http://idp.org/username",
                  "localClaim": "http://wso2.org/username"
                }
              ],
              "consumerKeyClaim": "azp",
              "scopesClaim": "scope",
              "tokenValidation": [
                {
                  "id": 0,
                  "enable": false,
                  "type": "JWT",
                  "value": {}
                }
              ],
              "enabled": true,
              "additionalProperties": {
                "self_validate_jwt": true,
                "Username": "${auConfiguration.getUserKeyManagerAdminName()}",
                "Password": "${auConfiguration.getUserKeyManagerAdminPWD()}"
              }
            }
             """.stripIndent()
    }

    /**
     * Get resident key manager information to disable it after adding the ob key manager.
     * @param accessToken
     */
    void getResidentKeyManager(String accessToken) {

        URI keyManagerEndpoint = new URI("${auConfiguration.getServerGatewayURL()}" + baseURL + "key-managers")
        def response = AURestAsRequestBuilder.buildRequest()
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, AUConstants.AUTHORIZATION_BEARER_TAG + accessToken)
                .get(keyManagerEndpoint.toString())

        Assert.assertEquals(response.statusCode(), HTTPResponse.SC_OK)
        residentKeyMangerId = AUTestUtil.parseResponseBody(response, "list.id").replaceAll("[\\[\\]]", "")

    }

    /**
     * Disable the resident key manager after adding the OBKeyManager.
     * @param accessToken
     */
    void disableResidentKeyManager(String accessToken) {

        URI keyManagerEndpoint = new URI("${auConfiguration.getServerGatewayURL()}" + baseURL + "key-managers/" + residentKeyMangerId)
        def response = AURestAsRequestBuilder.buildRequest()
                .contentType(AUConstants.CONTENT_TYPE_APPLICATION_JSON)
                .header(AUConstants.AUTHORIZATION_HEADER_KEY, AUConstants.AUTHORIZATION_BEARER_TAG + accessToken)
                .body(getResidentKeyManagerUpdatePayload())
                .put(keyManagerEndpoint.toString())

        Assert.assertEquals(response.statusCode(), HTTPResponse.SC_OK)
    }

    /**
     * Get Resident Key Manager Update Payload.
     * @return
     */
    static String getResidentKeyManagerUpdatePayload() {
        return """
         {
            "name": "Resident Key Manager",
            "type": "default",
            "enabled": false
          }
            """.stripIndent()
    }
}

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
import org.wso2.cds.test.framework.configuration.AUConfigurationService
import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.utility.AURestAsRequestBuilder
import org.wso2.cds.test.framework.utility.AUTestUtil

import java.nio.charset.Charset

/**
 * Class contains REST Api implementation for token generation to invoke publisher and dev portal APIs.
 */
class RESTApiAccessTokenGeneration {

    private static AUConfigurationService auConfiguration
    String clientId
    String clientSecret

    RESTApiAccessTokenGeneration() {
        auConfiguration = new AUConfigurationService()
    }

    /**
     * Create DCR application to invoke publisher and dev portal.
     */
    void createDCRApplication() {
        URI dcrEndpoint = new URI("${auConfiguration.getServerGatewayURL()}" + AUConstants.REST_API_CLIENT_REGISTRATION_ENDPOINT)
        def response = AURestAsRequestBuilder.buildRequest()
                .contentType(AUConstants.CONTENT_TYPE_APPLICATION_JSON)
                .header(AUConstants.AUTHORIZATION_HEADER_KEY,
                generateBasicHeader(auConfiguration.getUserKeyManagerAdminName(), auConfiguration.getUserKeyManagerAdminPWD()))
                .body(getDCRPayload())
                .post(dcrEndpoint.toString())

        Assert.assertEquals(response.statusCode(), HTTPResponse.SC_OK)
        clientId = AUTestUtil.parseResponseBody(response, "clientId")
        clientSecret = AUTestUtil.parseResponseBody(response, "clientSecret")
    }

    /**
     * Obtain access token to invoke publisher and dev portal.
     */
    String obtainAccessToken() {
        URI tokenEndpoint = new URI("${auConfiguration.getServerGatewayURL()}/oauth2/token")
        def response = AURestAsRequestBuilder.buildRequest()
                .param(AUConstants.GRANT_TYPE_KEY, AUConstants.CLIENT_CREDENTIALS)
                .param(AUConstants.SCOPE_KEY, "apim:api_view apim:api_create apim:api_publish apim:subscription_view " +
                "apim:subscribe apim:api_key apim:app_manage apim:mediation_policy_create " +
                "apim:mediation_policy_view apim:mediation_policy_manage")
                .param(AUConstants.CLIENT_ID, clientId)
                .header(AUConstants.AUTHORIZATION_HEADER_KEY,
                generateBasicHeader(clientId, clientSecret))
                .post(tokenEndpoint.toString())
        Assert.assertEquals(response.statusCode(), HTTPResponse.SC_OK)
        return AUTestUtil.parseResponseBody(response, AUConstants.ACCESS_TOKEN)
    }

    /**
     * Generate Basic Header.
     */
    static String generateBasicHeader(String username, String password) {
        String authToken = "${username}:${password}"
        return "Basic ${Base64.encoder.encodeToString(authToken.getBytes(Charset.defaultCharset()))}"
    }

    /**
     * Get DCR payload.
     */
    static String getDCRPayload() {
        return """
             {
             "callbackUrl":"www.google.lk",
             "clientName":"rest_api_publisher",
             "owner":"${auConfiguration.getUserKeyManagerAdminName()}",
             "grantType":"client_credentials password refresh_token",
             "saasApp":true
             }
             """.stripIndent()
    }
}

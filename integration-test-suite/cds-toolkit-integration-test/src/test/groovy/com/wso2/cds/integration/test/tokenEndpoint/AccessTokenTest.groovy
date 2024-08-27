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

package com.wso2.cds.integration.test.tokenEndpoint

import com.nimbusds.oauth2.sdk.AccessTokenResponse
import com.wso2.cds.test.framework.AUTest
import com.wso2.cds.test.framework.constant.AUAccountScope
import com.wso2.cds.test.framework.constant.AUConstants
import com.wso2.cds.test.framework.request_builder.AURequestBuilder
import com.wso2.cds.test.framework.utility.AUTestUtil
import java.nio.charset.Charset
import org.testng.Assert
import org.testng.annotations.Test
import org.testng.asserts.SoftAssert


/**
 * for testing User access token with the test context.
 * new User access token will be generated if there is no already generated user access token.
 */
class AccessTokenTest extends AUTest {

    private List<AUAccountScope> scopeArrayList = [
            AUAccountScope.BANK_ACCOUNT_BASIC_READ,
            AUAccountScope.BANK_TRANSACTION_READ,
            AUAccountScope.BANK_CUSTOMER_DETAIL_READ
    ]

    String user_AccessToken, idToken

    private final String ACCOUNTS_BASIC_OPENID_SCOPE_LIST = "bank:accounts.basic:read bank:accounts.detail:" +
            "read openid"
    private final String ACCOUNTS_BASIC_ACCOUNT_DETAIL_OPENID_SCOPE_LIST = "bank:accounts.basic:read bank:" +
            "accounts.detail:read openid"
    private AccessTokenResponse userAccessToken

    @Test
    void "OB-1264-Invoke token endpoint for user access token without private-key JWT client authentication"() {

        doConsentAuthorisation( auConfiguration.getAppInfoClientID())
        Assert.assertNotNull(authorisationCode)

        def errorObject = AURequestBuilder.getUserTokenErrorResponse(authorisationCode,
                auConfiguration.getAppInfoRedirectURL(),auConfiguration.getAppInfoClientID(),false)
        Assert.assertEquals(errorObject.toJSONObject().get(AUConstants.ERROR_DESCRIPTION), "Request does not follow the" +
                " registered token endpoint auth method private_key_jwt")
    }

    @Test
    void "OB-1265-Invoke token endpoint for user access token without MTLS transport security"() {

        doConsentAuthorisation( auConfiguration.getAppInfoClientID())
        Assert.assertNotNull(authorisationCode)

        def errorObject = AURequestBuilder.getUserTokenErrorResponse(authorisationCode,
                auConfiguration.getAppInfoRedirectURL(), auConfiguration.getAppInfoClientID(), true,
                false)
        Assert.assertEquals(errorObject.toJSONObject().get(AUConstants.ERROR_DESCRIPTION), "Transport certificate" +
                " not found in the request")
    }

    @Test
    void "OB-1266_Invoke token endpoint for user access token with a different redirect uri"() {

        doConsentAuthorisation( auConfiguration.getAppInfoClientID())
        Assert.assertNotNull(authorisationCode)

        def errorObject = AURequestBuilder.getUserTokenErrorResponse(authorisationCode,
                AUConstants.DCR_ALTERNATE_REDIRECT_URI)
        Assert.assertEquals(errorObject.toJSONObject().get(AUConstants.ERROR_DESCRIPTION), "Callback url mismatch")

    }

    @Test
    void "OB-1272_Invoke token endpoint for user access token with 'RS256' as the signature algorithm"() {

        doConsentAuthorisation( auConfiguration.getAppInfoClientID())
        Assert.assertNotNull(authorisationCode)

        def errorObject = AURequestBuilder.getUserTokenErrorResponse(authorisationCode,
                auConfiguration.getAppInfoRedirectURL(), auConfiguration.getAppInfoClientID(), true,
                true,"RS256")
        Assert.assertEquals(errorObject.toJSONObject().get(AUConstants.ERROR_DESCRIPTION), "Registered algorithm " +
                "does not match with the token signed algorithm")

    }

    @Test
    void "OB-1273_Invoke token endpoint for user access token with 'PS512' as the signature algorithm"() {

        doConsentAuthorisation( auConfiguration.getAppInfoClientID())
        Assert.assertNotNull(authorisationCode)

        def errorObject = AURequestBuilder.getUserTokenErrorResponse(authorisationCode,
                auConfiguration.getAppInfoRedirectURL(), auConfiguration.getAppInfoClientID(), true,
                true,"PS512")
        Assert.assertEquals(errorObject.toJSONObject().get(AUConstants.ERROR_DESCRIPTION), "Registered algorithm " +
                "does not match with the token signed algorithm")

    }

    @Test (priority = 1)
    void "OB-1267_Invoke token endpoint for user access token with a subset of authorized scopes"() {

        // scopes authorized for the consent
        scopes = [
                AUAccountScope.BANK_ACCOUNT_BASIC_READ,
                AUAccountScope.BANK_ACCOUNT_DETAIL_READ,
        ]
        doConsentAuthorisation(auConfiguration.getAppInfoClientID())
        Assert.assertNotNull(authorisationCode)

        //scopes requested for the user access token
        scopes = [
                AUAccountScope.BANK_ACCOUNT_BASIC_READ,
        ]
        userAccessToken = AURequestBuilder.getUserToken(authorisationCode, scopes,
                AUConstants.CODE_VERIFIER)
        Assert.assertNotNull(userAccessToken.tokens.accessToken)
        Assert.assertNotNull(userAccessToken.tokens.refreshToken)
        Assert.assertEquals(userAccessToken.toJSONObject().get("scope"), ACCOUNTS_BASIC_OPENID_SCOPE_LIST)
    }

    @Test(priority = 1, dependsOnMethods = "OB-1267_Invoke token endpoint for user access token with a subset of authorized scopes")
    void "OB-1268_Invoke accounts retrieval with access token only bound to bank account basic read scopes"() {

        def cdsClient = "${auConfiguration.getAppInfoClientID()}:${auConfiguration.getAppInfoClientSecret()}"
        def clientHeader = "${Base64.encoder.encodeToString(cdsClient.getBytes(Charset.defaultCharset()))}"

        def response = AURequestBuilder
                .buildBasicRequest(userAccessToken.tokens.accessToken.toString(), AUConstants.X_V_HEADER_ACCOUNTS)
                .header(AUConstants.X_FAPI_AUTH_DATE, AUConstants.DATE)
                .header(AUConstants.X_FAPI_CUSTOMER_IP_ADDRESS , AUConstants.IP)
                .header(AUConstants.X_CDS_CLIENT_HEADERS , clientHeader)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ACCOUNT))
                .get("${AUConstants.BULK_ACCOUNT_PATH}")

        SoftAssert softAssertion= new SoftAssert()
        softAssertion.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        softAssertion.assertAll()
    }

    @Test
    void "OB-1270_Invoke token endpoint for user access token with a unauthorized scope"() {

        scopes = [
                AUAccountScope.BANK_ACCOUNT_BASIC_READ
        ]

        doConsentAuthorisation( auConfiguration.getAppInfoClientID())
        Assert.assertNotNull(authorisationCode)

        scopes = [
                AUAccountScope.BANK_REGULAR_PAYMENTS_READ
        ]

        userAccessToken = AURequestBuilder.getUserToken(authorisationCode, scopes, AUConstants.CODE_VERIFIER)
        Assert.assertNotNull(userAccessToken.tokens.accessToken)
        Assert.assertNotNull(userAccessToken.tokens.refreshToken)
        Assert.assertEquals(userAccessToken.toJSONObject().get("scope"), "bank:accounts.basic:read openid")
    }

    @Test
    void "OB-1271_Invoke token endpoint for user access token with a set of authorized and unauthorized scopes"() {

        scopes = [
                AUAccountScope.BANK_ACCOUNT_BASIC_READ,
                AUAccountScope.BANK_ACCOUNT_DETAIL_READ
        ]

        doConsentAuthorisation( auConfiguration.getAppInfoClientID())
        Assert.assertNotNull(authorisationCode)

        //Profile scope is additionally requested
        scopes = [
                AUAccountScope.PROFILE,
                AUAccountScope.BANK_ACCOUNT_BASIC_READ,
                AUAccountScope.BANK_ACCOUNT_DETAIL_READ
        ]

        userAccessToken = AURequestBuilder.getUserToken(authorisationCode, scopes, AUConstants.CODE_VERIFIER)
        Assert.assertNotNull(userAccessToken.tokens.accessToken)
        Assert.assertNotNull(userAccessToken.tokens.refreshToken)
        Assert.assertNotNull(userAccessToken.getCustomParameters().get("cdr_arrangement_id"))
        Assert.assertEquals(userAccessToken.toJSONObject().get("scope"),ACCOUNTS_BASIC_ACCOUNT_DETAIL_OPENID_SCOPE_LIST)
    }

    @Test
    void "CDS-705_Verify introspection response not returning username field"() {

        // Generating a new authorisation code
        doConsentAuthorisation()
        Assert.assertNotNull(authorisationCode)

        //Generate Access Token
        AccessTokenResponse accessTokenResponse = getUserAccessTokenResponse(clientId)
        cdrArrangementId = accessTokenResponse.getCustomParameters().get(AUConstants.CDR_ARRANGEMENT_ID)
        refreshToken = accessTokenResponse.tokens.refreshToken
        user_AccessToken = accessTokenResponse.tokens.accessToken
        idToken = accessTokenResponse.getCustomParameters().get(AUConstants.ID_TOKEN_KEY)

        String scopeString = "${String.join(" ", scopes.collect({ it.scopeString }))} openid"

        //Check the status of the refresh token
        def introspectResponse = AURequestBuilder.buildIntrospectionRequest(refreshToken, auConfiguration.getAppInfoClientID())
                .post(AUConstants.INTROSPECTION_ENDPOINT)

        Assert.assertTrue(introspectResponse.jsonPath().get("active").equals(true))
        Assert.assertNotNull(introspectResponse.jsonPath().get("exp"))
        Assert.assertNotNull(introspectResponse.jsonPath().get("scope"))
        Assert.assertNull(introspectResponse.jsonPath().get("username"))
    }

    @Test (dependsOnMethods = "CDS-705_Verify introspection response not returning username field")
    void "CDS-718_Send introspection call for user access token"() {

        def introspectResponse = AURequestBuilder.buildIntrospectionRequest(user_AccessToken,
                auConfiguration.getAppInfoClientID())
                .post(AUConstants.INTROSPECTION_ENDPOINT)

        Assert.assertTrue(introspectResponse.jsonPath().get("active").equals(false))
    }

    @Test (dependsOnMethods = "CDS-705_Verify introspection response not returning username field")
    void "CDS-718_Send introspection call for id_token"() {

        def introspectResponse = AURequestBuilder.buildIntrospectionRequest(idToken,
                auConfiguration.getAppInfoClientID())
                .post(AUConstants.INTROSPECTION_ENDPOINT)

        Assert.assertTrue(introspectResponse.jsonPath().get("active").equals(false))
    }

    //TODO: Issue: https://github.com/wso2-enterprise/financial-open-banking/issues/8456
    @Test(dependsOnMethods = "CDS-705_Verify introspection response not returning username field")
    void "CDS-1023_Verify introspection request return cdr_arrangement_id"() {

        def response = AURequestBuilder.buildIntrospectionRequest(refreshToken,
                auConfiguration.getAppInfoClientID(), 0)
                .post(AUConstants.INTROSPECTION_ENDPOINT)

        //Introspection validation can only be done for refresh token
        Assert.assertTrue(response.jsonPath().get("active").equals(true))
        Assert.assertNotNull(response.jsonPath().get("scope"))
        Assert.assertNotNull(response.jsonPath().get("exp"))
        Assert.assertEquals(response.jsonPath().get("cdr_arrangement_id"), cdrArrangementId)
    }
}

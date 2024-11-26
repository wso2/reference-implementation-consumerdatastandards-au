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

package org.wso2.cds.keymanager.test.jarm

import com.nimbusds.jose.JWSHeader
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.oauth2.sdk.ResponseMode
import com.nimbusds.oauth2.sdk.ResponseType
import org.wso2.cds.test.framework.AUTest
import org.wso2.cds.test.framework.constant.AUAccountProfile
import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.request_builder.AUJWTGenerator
import org.testng.Assert
import org.testng.annotations.Test
import org.testng.annotations.BeforeClass
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Jwt Secured Authorization Response Validation Tests.
 */
class JwtSecuredAuthorizationResponseValidationTests extends AUTest {

    String authResponseUrl
    String responseJwt
    JWTClaimsSet jwtPayload
    String clientId
    JWSHeader jwtHeaders


    @BeforeClass (alwaysRun = true)
    void "Send Authorisation Request"() {

        doConsentAuthorisation(ResponseMode.JWT, ResponseType.CODE, auConfiguration.getAppInfoClientID())
        authResponseUrl = automationResponse.currentUrl.get()
        responseJwt = authResponseUrl.split(AUConstants.HTML_RESPONSE_ATTR)[1]
        Assert.assertNotNull(responseJwt)
        jwtPayload = AUJWTGenerator.extractJwt(responseJwt)
        jwtHeaders = AUJWTGenerator.extractJwtHeaders(responseJwt)
    }

    @Test
    void "CDS-565_Verify the JWT response contains the client id as aud value"() {

        def audience = jwtPayload.getAudience()[0].toString()
        Assert.assertTrue(audience.equalsIgnoreCase(auConfiguration.getAppInfoClientID()))
    }

    @Test
    void "CDS-564_Verify the JWT response contains the valid issuer of the authorisation server"() {

        def issuer = jwtPayload.getIssuer()
        Assert.assertTrue(issuer.equalsIgnoreCase(auConfiguration.getConsentAudienceValue()))
    }

    @Test
    void "CDS-566_Verify the JWT response contains a future expiration date"() {

        Long exp = jwtPayload.getExpirationTime().getTime() / 1000
        LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(exp, 0, ZoneOffset.UTC)
        Assert.assertTrue(localDateTime >= LocalDateTime.now())
    }

    @Test
    void "CDS-567_Verify the alg of the JWT response not be none"() {

        def alg = jwtHeaders.algorithm.toString()
        Assert.assertFalse(alg.equals("none"))
        Assert.assertTrue(alg.equals(auConfiguration.getCommonSigningAlgorithm()))
    }

    @Test
    void "CDS-568_Verify the JWT response does not contains state param if it is not included in request"() {

        doConsentAuthorisation(ResponseMode.JWT, ResponseType.CODE, auConfiguration.getAppInfoClientID(),
                AUAccountProfile.INDIVIDUAL, false)
        authResponseUrl = automationResponse.currentUrl.get()
        responseJwt = authResponseUrl.split(AUConstants.HTML_RESPONSE_ATTR)[1]
        jwtPayload = AUJWTGenerator.extractJwt(responseJwt)
        Assert.assertNull(jwtPayload.getStringClaim(AUConstants.ALGORITHM_KEY))
    }
}

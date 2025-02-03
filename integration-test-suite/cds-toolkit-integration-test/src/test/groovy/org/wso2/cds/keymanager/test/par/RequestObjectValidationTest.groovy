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

package org.wso2.cds.keymanager.test.par

import com.nimbusds.oauth2.sdk.ResponseMode
import io.restassured.response.Response
import org.testng.annotations.BeforeClass
import org.wso2.cds.test.framework.AUTest
import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.request_builder.AUJWTGenerator
import org.wso2.cds.test.framework.utility.AURestAsRequestBuilder
import org.wso2.cds.test.framework.utility.AUTestUtil
import org.testng.Assert
import org.testng.annotations.Test

import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * This class contains the test cases to validate the request object of the push authorisation request.
 */
class RequestObjectValidationTest extends AUTest {

    AUJWTGenerator generator = new AUJWTGenerator()

    @BeforeClass
    void "setTppNumber"() {
        auConfiguration.setTppNumber(0)
    }

    @Test
    void "OB-1231_Initiate push authorisation flow without request object parameter"() {

        response = auAuthorisationBuilder.doPushAuthorisationRequestWithoutRequestObject(auConfiguration.getAppInfoClientID())

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION), AUConstants.UNABLE_TO_DECODE_JWT)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR), AUConstants.INVALID_REQUEST_OBJECT)
    }

    @Test
    void "OB-1232_Initiate push authorisation flow with unsigned request object"() {

        def response = auAuthorisationBuilder.doPushAuthorisationRequestWithUnsignedRequestObject(scopes,
                AUConstants.DEFAULT_SHARING_DURATION, true, "")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION),
                AUConstants.UNABLE_TO_DECODE_JWT)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR), AUConstants.INVALID_REQUEST_OBJECT)
    }

    @Test (priority = 1)
    void "OB-1233_Initiate authorisation consent flow with 'RS256' signature algorithm"() {

        String claims = generator.getRequestObjectClaim(scopes, AUConstants.DEFAULT_SHARING_DURATION, true, "",
                auConfiguration.getAppInfoRedirectURL(), auConfiguration.getAppInfoClientID(),
                auAuthorisationBuilder.getResponseType().toString(), true,
                auAuthorisationBuilder.getState().toString())

        def response = auAuthorisationBuilder.doPushAuthorisationRequest(claims,
                auConfiguration.getAppInfoClientID(), true, "RS256")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION),
                AUConstants.INVALID_ALGORITHM)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR), AUConstants.INVALID_REQUEST_OBJECT)
    }

    @Test (priority = 1)
    void "OB-1234_Initiate authorisation consent flow with 'PS512' signature algorithm"() {

        String claims = generator.getRequestObjectClaim(scopes, AUConstants.DEFAULT_SHARING_DURATION, true, "",
                auConfiguration.getAppInfoRedirectURL(), auConfiguration.getAppInfoClientID(),
                auAuthorisationBuilder.getResponseType().toString(), true,
                auAuthorisationBuilder.getState().toString())

        def response = auAuthorisationBuilder.doPushAuthorisationRequest(claims,
                auConfiguration.getAppInfoClientID(), true, "PS512")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION),
                AUConstants.INVALID_ALGORITHM)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR), AUConstants.INVALID_REQUEST_OBJECT)
    }

    @Test
    void "OB-1241_Initiate authorisation consent flow without 'aud' claim in request object"() {

        String claims = generator.getRequestObjectClaim(scopes, AUConstants.DEFAULT_SHARING_DURATION, true, "",
                auConfiguration.getAppInfoRedirectURL(), auConfiguration.getAppInfoClientID(),
                auAuthorisationBuilder.getResponseType().toString(), true,
                auAuthorisationBuilder.getState().toString())

        String modifiedClaimSet = generator.removeClaimsFromRequestObject(claims, "aud")

        def response = auAuthorisationBuilder.doPushAuthorisationRequest(modifiedClaimSet)

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION), AUConstants.MISSING_AUD_VALUE)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR), AUConstants.INVALID_REQUEST_OBJECT)
    }

    @Test
    void "OB-1242_Initiate authorisation consent flow without 'iss' claim in request object"() {

        String claims = generator.getRequestObjectClaim(scopes, AUConstants.DEFAULT_SHARING_DURATION, true, "",
                auConfiguration.getAppInfoRedirectURL(), auConfiguration.getAppInfoClientID(),
                auAuthorisationBuilder.getResponseType().toString(), true,
                auAuthorisationBuilder.getState().toString())

        String modifiedClaimSet = generator.removeClaimsFromRequestObject(claims, "iss")

        def response = auAuthorisationBuilder.doPushAuthorisationRequest(modifiedClaimSet)

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION), AUConstants.MISSING_ISS_VALUE)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR), AUConstants.INVALID_REQUEST_OBJECT)
    }

    @Test
    void "OB-1243_Initiate authorisation consent flow without 'exp' claim in request object"() {

        String claims = generator.getRequestObjectClaim(scopes, AUConstants.DEFAULT_SHARING_DURATION, true, "",
                auConfiguration.getAppInfoRedirectURL(), auConfiguration.getAppInfoClientID(),
                auAuthorisationBuilder.getResponseType().toString(), true,
                auAuthorisationBuilder.getState().toString())

        String modifiedClaimSet = generator.removeClaimsFromRequestObject(claims, "exp")

        def response = auAuthorisationBuilder.doPushAuthorisationRequest(modifiedClaimSet)

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION), AUConstants.MISSING_EXP_VALUE)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR), AUConstants.INVALID_REQUEST_OBJECT)
    }

    @Test
    void "OB-1244_Initiate authorisation consent flow without 'nbf' claim in request object"() {

        String claims = generator.getRequestObjectClaim(scopes, AUConstants.DEFAULT_SHARING_DURATION, true, "",
                auConfiguration.getAppInfoRedirectURL(), auConfiguration.getAppInfoClientID(),
                auAuthorisationBuilder.getResponseType().toString(), true,
                auAuthorisationBuilder.getState().toString())

        String modifiedClaimSet = generator.removeClaimsFromRequestObject(claims, "nbf")

        def response = auAuthorisationBuilder.doPushAuthorisationRequest(modifiedClaimSet)

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION), AUConstants.MISSING_NBF_VALUE)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR), AUConstants.INVALID_REQUEST_OBJECT)
    }

    @Test
    void "OB-1245_Initiate authorisation consent flow with expired 'exp' claim in request object"() {

        String claims = generator.getRequestObjectClaim(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "", auConfiguration.getAppInfoRedirectURL(),
                auConfiguration.getAppInfoClientID(), auAuthorisationBuilder.getResponseType().toString(), true,
                auAuthorisationBuilder.getState().toString(), ResponseMode.JWT.toString(),
                Instant.now().minus(1, ChronoUnit.HOURS))

        def response = auAuthorisationBuilder.doPushAuthorisationRequest(claims)

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION),
                AUConstants.INVALID_FUTURE_EXPIRY_TIME)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR), AUConstants.INVALID_REQUEST_OBJECT)
    }

    @Test
    void "OB-1246_Initiate authorisation consent flow with 'nbf' claim with a future time"() {

        Instant time = Instant.now().plus(1, ChronoUnit.HOURS)

        String claims = generator.getRequestObjectClaim(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "", auConfiguration.getAppInfoRedirectURL(),
                auConfiguration.getAppInfoClientID(), auAuthorisationBuilder.getResponseType().toString(), true,
                auAuthorisationBuilder.getState().toString(), ResponseMode.JWT.toString(), time, time)

        def response = auAuthorisationBuilder.doPushAuthorisationRequest(claims)

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION),
                AUConstants.INVALID_FUTURE_NBF_TIME)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR), AUConstants.INVALID_REQUEST_OBJECT)
    }

    @Test
    void "OB-1248_Initiate push authorisation flow with 'exp' having a lifetime longer than 60 minutes after 'nbf'"() {

        Instant time = Instant.now().plus(2, ChronoUnit.HOURS)

        String claims = generator.getRequestObjectClaim(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "", auConfiguration.getAppInfoRedirectURL(),
                auConfiguration.getAppInfoClientID(), auAuthorisationBuilder.getResponseType().toString(), true,
                auAuthorisationBuilder.getState().toString(), ResponseMode.JWT.toString(), time)

        def response = auAuthorisationBuilder.doPushAuthorisationRequest(claims)

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION),
                AUConstants.INVALID_EXPIRY_TIME)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR), AUConstants.INVALID_REQUEST_OBJECT)
    }

    @Test
    void "CDS-658_Send PAR request without redirect URL"() {

        String claims = generator.getRequestObjectClaim(scopes, AUConstants.DEFAULT_SHARING_DURATION, true, "",
                auConfiguration.getAppInfoRedirectURL(), auConfiguration.getAppInfoClientID(),
                auAuthorisationBuilder.getResponseType().toString(), true,
                auAuthorisationBuilder.getState().toString())

        String modifiedClaimSet = generator.removeClaimsFromRequestObject(claims, "redirect_uri")
        def response = auAuthorisationBuilder.doPushAuthorisationRequest(modifiedClaimSet)

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION),
                AUConstants.MISSING_REDIRECT_URL_VALUE)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR), AUConstants.INVALID_REQUEST)
    }

    @Test
    void "CDS-669_Send PAR request with invalid redirect URL"() {

        String claims = generator.getRequestObjectClaim(scopes, AUConstants.DEFAULT_SHARING_DURATION, true, "",
                auConfiguration.getAppInfoRedirectURL(), auConfiguration.getAppInfoClientID(),
                auAuthorisationBuilder.getResponseType().toString(), true,
                auAuthorisationBuilder.getState().toString())

        String modifiedClaimSet = generator.addClaimsFromRequestObject(claims, "redirect_uri",
                "https://www.google.com")
        def response = auAuthorisationBuilder.doPushAuthorisationRequest(modifiedClaimSet)

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION),
                AUConstants.CALLBACK_NOT_MATCH)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR), AUConstants.INVALID_REQUEST)
    }

    @Test
    void "CDS-670_Send PAR request with empty cdr_arrangement_id"() {

        String claims = generator.getRequestObjectClaim(scopes, AUConstants.DEFAULT_SHARING_DURATION, true, "",
                auConfiguration.getAppInfoRedirectURL(), auConfiguration.getAppInfoClientID(),
                auAuthorisationBuilder.getResponseType().toString(), true,
                auAuthorisationBuilder.getState().toString())

        def response = auAuthorisationBuilder.doPushAuthorisationRequest(claims)

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_201)
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI))
    }

    @Test
    void "CDS-671_Send PAR request with null cdr_arrangement_id"() {

        String claims = generator.getRequestObjectClaim(scopes, AUConstants.DEFAULT_SHARING_DURATION,
                true, "null", auConfiguration.getAppInfoRedirectURL(),
                auConfiguration.getAppInfoClientID(), auAuthorisationBuilder.getResponseType().toString(),
                true, auAuthorisationBuilder.getState().toString())

        def response = auAuthorisationBuilder.doPushAuthorisationRequest(claims)

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION),
                AUConstants.INVALID_CDR_ARRANGEMENT_ID)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR), AUConstants.INVALID_REQUEST_OBJECT)
    }

    @Test
    void "CDS-684_PAR call with an expired request object"() {

        Long expiryDate = Instant.now().minus(1, ChronoUnit.HOURS).getEpochSecond().toLong()

        String claims = generator.getRequestObjectClaim(scopes, AUConstants.DEFAULT_SHARING_DURATION, true, "",
                auConfiguration.getAppInfoRedirectURL(), auConfiguration.getAppInfoClientID(),
                auAuthorisationBuilder.getResponseType().toString(), true,
                auAuthorisationBuilder.getState().toString())

        String modifiedClaimSet = generator.addClaimsFromRequestObject(claims, "exp", expiryDate)

        def response = auAuthorisationBuilder.doPushAuthorisationRequest(modifiedClaimSet)

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION),
                "Invalid expiry time. 'exp' claim must be a future value.")
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR), AUConstants.INVALID_REQUEST_OBJECT)
    }

    @Test
    void "CDS-685_PAR call with an invalid audience"() {

        String claims = generator.getRequestObjectClaim(scopes, AUConstants.DEFAULT_SHARING_DURATION, true, "",
                auConfiguration.getAppInfoRedirectURL(), auConfiguration.getAppInfoClientID(),
                auAuthorisationBuilder.getResponseType().toString(), true,
                auAuthorisationBuilder.getState().toString())

        String modifiedClaimSet = generator.addClaimsFromRequestObject(claims, "aud", "123")

        def response = auAuthorisationBuilder.doPushAuthorisationRequest(modifiedClaimSet)

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION),
                AUConstants.INVALID_AUDIENCE)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR), AUConstants.INVALID_REQUEST_OBJECT)
    }

    @Test
    void "CDS-688_Verify null sharing_duration in PAR authorisation url"() {

        String claims = generator.getRequestObjectClaim(scopes, null, true, "",
                auConfiguration.getAppInfoRedirectURL(), auConfiguration.getAppInfoClientID(),
                auAuthorisationBuilder.getResponseType().toString(), true,
                auAuthorisationBuilder.getState().toString())

        def response = auAuthorisationBuilder.doPushAuthorisationRequest(claims)

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_201)
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, AUConstants.REQUEST_URI))
    }

    @Test
    void "CDS-717_Send PAR request by passing resource path as the aud value in request object"() {

        String claims = generator.getRequestObjectClaim(scopes, AUConstants.DEFAULT_SHARING_DURATION, true, "",
                auConfiguration.getAppInfoRedirectURL(), auConfiguration.getAppInfoClientID(),
                auAuthorisationBuilder.getResponseType().toString(), true,
                auAuthorisationBuilder.getState().toString())

        def audienceValue = AUConstants.PUSHED_AUTHORISATION_BASE_PATH + AUConstants.PAR_ENDPOINT
        String modifiedClaimSet = generator.addClaimsFromRequestObject(claims, "aud", audienceValue)

        def response = auAuthorisationBuilder.doPushAuthorisationRequest(modifiedClaimSet)

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_400)
        Assert.assertEquals(AUTestUtil.parseResponseBody(response, AUConstants.ERROR_DESCRIPTION),
                AUConstants.INVALID_AUDIENCE)
    }

    @Test
    void "Send PAR request by passing resource path as the aud value in client assertion"() {

        clientId = auConfiguration.getAppInfoClientID()

        String claims = generator.getRequestObjectClaim(scopes, AUConstants.DEFAULT_SHARING_DURATION, true, "",
                auConfiguration.getAppInfoRedirectURL(), clientId,
                auAuthorisationBuilder.getResponseType().toString(), true,
                auAuthorisationBuilder.getState().toString())

        def audienceValueForClientAssertion = AUConstants.PUSHED_AUTHORISATION_BASE_PATH + AUConstants.PAR_ENDPOINT
        String assertionString = generator.getClientAssertionJwt(clientId, audienceValueForClientAssertion)

        def bodyContent = [
                (AUConstants.CLIENT_ID_KEY)            : (clientId),
                (AUConstants.CLIENT_ASSERTION_TYPE_KEY): (AUConstants.CLIENT_ASSERTION_TYPE),
                (AUConstants.CLIENT_ASSERTION_KEY)     : assertionString,
        ]

        String signedRequest = generator.getSignedAuthRequestObject(claims).serialize()

        Response parResponse = AURestAsRequestBuilder.buildRequest()
                    .contentType(AUConstants.ACCESS_TOKEN_CONTENT_TYPE)
                    .formParams(bodyContent)
                    .formParams(AUConstants.REQUEST_KEY, signedRequest)
                    .baseUri(AUConstants.PUSHED_AUTHORISATION_BASE_PATH)
                    .post(AUConstants.PAR_ENDPOINT)

        Assert.assertEquals(parResponse.statusCode(), AUConstants.STATUS_CODE_201)
    }
}

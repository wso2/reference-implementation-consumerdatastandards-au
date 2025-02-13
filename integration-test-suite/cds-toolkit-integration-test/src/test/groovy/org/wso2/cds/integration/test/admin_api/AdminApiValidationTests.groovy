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

package org.wso2.cds.integration.test.admin_api

import org.testng.annotations.BeforeClass
import org.wso2.cds.test.framework.AUTest
import org.wso2.cds.test.framework.constant.AUConstants
import org.wso2.cds.test.framework.request_builder.AURequestBuilder
import org.wso2.cds.test.framework.utility.AUTestUtil
import org.testng.Assert
import org.testng.annotations.Test

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Admin API Validation Tests.
 */
class AdminApiValidationTests extends AUTest {

    @BeforeClass
    void "setTppNumber"() {
        auConfiguration.setTppNumber(0)
    }

    @Test
    void "TC1001001_Retrieve critical update to the metadata for Accredited Data Recipients"() {

        String assertionString = generator.getClientAssertionJwt(AUConstants.ADMIN_API_ISSUER, AUConstants.ADMIN_API_AUDIENCE)

        String requestBody = """
            {
                "data": {
                "action": "REFRESH"
            },
                "meta": {}
            }
        """.stripIndent()

        def response = AURequestBuilder.buildBasicRequest(assertionString, AUConstants.X_V_HEADER_METADATA)
                .header(AUConstants.CONTENT_TYPE, "application/json")
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ADMIN))
                .body(requestBody)
                .post("${AUConstants.CDS_ADMIN_PATH}${AUConstants.GET_META}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(response.getHeader(AUConstants.X_V_HEADER), AUConstants.X_V_HEADER_METRICS)
    }

    @Test
    void "TC1002001_Retrieve operational statistics from the Data Holder"() {

        String assertionString = generator.getClientAssertionJwt(AUConstants.ADMIN_API_ISSUER, AUConstants.ADMIN_API_AUDIENCE)

        def response = AURequestBuilder.buildBasicRequest(assertionString, AUConstants.X_V_HEADER_METRICS)
                .header(AUConstants.CONTENT_TYPE, "application/json")
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ADMIN))
                .get("${AUConstants.CDS_ADMIN_PATH}${AUConstants.GET_STAT}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)
    }

    @Test
    void "Meta Data"() {

        String assertionString = generator.getClientAssertionJwt(AUConstants.ADMIN_API_ISSUER, AUConstants.ADMIN_API_AUDIENCE)

        String requestBody = """
            {
                "data": {
                "action": "REFRESH"
            },
                "meta": {}
            }
         """.stripIndent()

        def response = AURequestBuilder.buildBasicRequest(assertionString, AUConstants.X_V_HEADER_METADATA)
                .header(AUConstants.CONTENT_TYPE, "application/json")
                .header(AUConstants.X_MIN_HEADER, AUConstants.X_V_MIN_HEADER_METRICS)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ADMIN))
                .body(requestBody)
                .post("${AUConstants.CDS_ADMIN_PATH}${AUConstants.GET_META}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
    }

    @Test
    void "Meta Data Update with authorisation code type access token"() {

        doConsentAuthorisation()
        String assertionString = generator.getClientAssertionJwt(AUConstants.ADMIN_API_ISSUER, AUConstants.ADMIN_API_AUDIENCE)

        String requestBody = """
            {
                "data": {
                "action": "REFRESH"
            },
                "meta": {}
            }
         """.stripIndent()

        def response = AURequestBuilder.buildBasicRequest(assertionString, AUConstants.X_V_HEADER_METADATA)
                .header(AUConstants.CONTENT_TYPE, "application/json")
                .header(AUConstants.X_MIN_HEADER, AUConstants.X_V_MIN_HEADER_METRICS)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ADMIN))
                .body(requestBody)
                .post("${AUConstants.CDS_ADMIN_PATH}${AUConstants.GET_META}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
    }

    @Test
    void "TC1002002_Metrics Data Current"() {

        String assertionString = generator.getClientAssertionJwt(AUConstants.ADMIN_API_ISSUER, AUConstants.ADMIN_API_AUDIENCE)

        def response = AURequestBuilder.buildBasicRequest(assertionString, AUConstants.X_V_HEADER_METRICS)
                .header(AUConstants.CONTENT_TYPE, "application/json")
                .header(AUConstants.X_MIN_HEADER, AUConstants.X_V_MIN_HEADER_METRICS)
                .queryParam(AUConstants.PERIOD, AUConstants.CURRENT)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ADMIN))
                .get("${AUConstants.CDS_ADMIN_PATH}${AUConstants.ADMIN_METRICS}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.requestTime"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.availability.aggregate.currentMonth"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.availability.unauthenticated.currentMonth"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.availability.authenticated.currentMonth"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.performance.aggregate.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.performance.highPriority.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.performance.largePayload.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.performance.lowPriority.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.performance.unattended.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.performance.unauthenticated.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.invocations.highPriority.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.invocations.largePayload.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.invocations.lowPriority.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.invocations.unattended.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.invocations.unauthenticated.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.averageResponse.highPriority.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.averageResponse.largePayload.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.averageResponse.lowPriority.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.averageResponse.unattended.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.averageResponse.unauthenticated.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.averageTps.aggregate.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.averageTps.unauthenticated.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.averageTps.authenticated.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.errors.aggregate.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.errors.unauthenticated.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.errors.authenticated.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.rejections.authenticated.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.rejections.unauthenticated.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.customerCount"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.recipientCount"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.activeAuthorisationCount.individual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.activeAuthorisationCount.nonIndividual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.newAuthorisationCount.currentDay.onceOff.individual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.newAuthorisationCount.currentDay.onceOff.nonIndividual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.newAuthorisationCount.currentDay.ongoing.individual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.newAuthorisationCount.currentDay.ongoing.nonIndividual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.revokedAuthorisationCount.currentDay.individual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.revokedAuthorisationCount.currentDay.nonIndividual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.amendedAuthorisationCount.currentDay.individual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.amendedAuthorisationCount.currentDay.nonIndividual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.expiredAuthorisationCount.currentDay.individual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.expiredAuthorisationCount.currentDay.nonIndividual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.abandonedConsentFlowCount.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.abandonmentsByStage.preIdentification.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.abandonmentsByStage.preAuthentication.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.abandonmentsByStage.preAccountSelection.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.abandonmentsByStage.preAuthorisation.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.abandonmentsByStage.rejected.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.abandonmentsByStage.failedTokenExchange.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "links.self"))
    }

    @Test
    void "TC1002003_Metrics Data Historic"() {

        String assertionString = generator.getClientAssertionJwt(AUConstants.ADMIN_API_ISSUER, AUConstants.ADMIN_API_AUDIENCE)

        def response = AURequestBuilder.buildBasicRequest(assertionString, AUConstants.X_V_HEADER_METRICS)
                .header(AUConstants.CONTENT_TYPE, "application/json")
                .header(AUConstants.X_MIN_HEADER, AUConstants.X_V_MIN_HEADER_METRICS)
                .queryParam(AUConstants.PERIOD, AUConstants.HISTORIC)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ADMIN))
                .get("${AUConstants.CDS_ADMIN_PATH}${AUConstants.ADMIN_METRICS}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)
        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.requestTime"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.availability.aggregate.previousMonths"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.availability.unauthenticated.previousMonths"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.availability.authenticated.previousMonths"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.performance.aggregate.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.performance.highPriority.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.performance.largePayload.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.performance.lowPriority.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.performance.unattended.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.performance.unauthenticated.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.invocations.highPriority.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.invocations.largePayload.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.invocations.lowPriority.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.invocations.unattended.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.invocations.unauthenticated.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.averageResponse.highPriority.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.averageResponse.largePayload.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.averageResponse.lowPriority.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.averageResponse.unattended.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.averageResponse.unauthenticated.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.averageTps.aggregate.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.averageTps.unauthenticated.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.averageTps.authenticated.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.errors.aggregate.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.errors.unauthenticated.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.errors.authenticated.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.rejections.authenticated.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.rejections.unauthenticated.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.customerCount"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.recipientCount"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.activeAuthorisationCount.individual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.activeAuthorisationCount.nonIndividual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.newAuthorisationCount.previousDays.onceOff.individual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.newAuthorisationCount.previousDays.onceOff.nonIndividual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.newAuthorisationCount.previousDays.ongoing.individual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.newAuthorisationCount.previousDays.ongoing.nonIndividual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.revokedAuthorisationCount.previousDays.individual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.revokedAuthorisationCount.previousDays.nonIndividual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.amendedAuthorisationCount.previousDays.individual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.amendedAuthorisationCount.previousDays.nonIndividual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.expiredAuthorisationCount.previousDays.individual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.expiredAuthorisationCount.previousDays.nonIndividual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.abandonedConsentFlowCount.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.abandonmentsByStage.preIdentification.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.abandonmentsByStage.preAuthentication.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.abandonmentsByStage.preAccountSelection.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.abandonmentsByStage.preAuthorisation.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.abandonmentsByStage.rejected.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.abandonmentsByStage.failedTokenExchange.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "links.self"))
    }

    @Test (groups = "SmokeTest")
    void "TC1002004_Metrics Data All"() {

        String assertionString = generator.getClientAssertionJwt(AUConstants.ADMIN_API_ISSUER, AUConstants.ADMIN_API_AUDIENCE)

        def response = AURequestBuilder.buildBasicRequest(assertionString, AUConstants.X_V_HEADER_METRICS)
                .header(AUConstants.CONTENT_TYPE, "application/json")
                .header(AUConstants.X_MIN_HEADER, AUConstants.X_V_MIN_HEADER_METRICS)
                .queryParam(AUConstants.PERIOD, AUConstants.ALL)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ADMIN))
                .get("${AUConstants.CDS_ADMIN_PATH}${AUConstants.ADMIN_METRICS}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.requestTime"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.availability.aggregate.currentMonth"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.availability.aggregate.previousMonths"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.availability.unauthenticated.currentMonth"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.availability.unauthenticated.previousMonths"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.availability.authenticated.currentMonth"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.availability.authenticated.previousMonths"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.performance.aggregate.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.performance.aggregate.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.performance.highPriority.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.performance.highPriority.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.performance.largePayload.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.performance.largePayload.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.performance.lowPriority.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.performance.lowPriority.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.performance.unattended.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.performance.unattended.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.performance.unauthenticated.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.performance.unauthenticated.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.invocations.highPriority.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.invocations.highPriority.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.invocations.largePayload.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.invocations.largePayload.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.invocations.lowPriority.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.invocations.lowPriority.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.invocations.unattended.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.invocations.unattended.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.invocations.unauthenticated.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.invocations.unauthenticated.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.averageResponse.highPriority.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.averageResponse.highPriority.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.averageResponse.largePayload.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.averageResponse.largePayload.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.averageResponse.lowPriority.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.averageResponse.lowPriority.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.averageResponse.unattended.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.averageResponse.unattended.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.averageResponse.unauthenticated.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.averageResponse.unauthenticated.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.averageTps.aggregate.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.averageTps.aggregate.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.averageTps.unauthenticated.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.averageTps.unauthenticated.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.averageTps.authenticated.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.averageTps.authenticated.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.errors.aggregate.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.errors.aggregate.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.errors.unauthenticated.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.errors.unauthenticated.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.errors.authenticated.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.errors.authenticated.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.rejections.authenticated.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.rejections.authenticated.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.rejections.unauthenticated.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.rejections.unauthenticated.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.customerCount"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.recipientCount"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.activeAuthorisationCount.individual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.activeAuthorisationCount.nonIndividual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.newAuthorisationCount.currentDay.onceOff.individual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.newAuthorisationCount.currentDay.onceOff.nonIndividual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.newAuthorisationCount.currentDay.ongoing.individual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.newAuthorisationCount.currentDay.ongoing.nonIndividual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.newAuthorisationCount.previousDays.onceOff.individual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.newAuthorisationCount.previousDays.onceOff.nonIndividual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.newAuthorisationCount.previousDays.ongoing.individual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.newAuthorisationCount.previousDays.ongoing.nonIndividual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.revokedAuthorisationCount.currentDay.individual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.revokedAuthorisationCount.currentDay.nonIndividual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.revokedAuthorisationCount.previousDays.individual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.revokedAuthorisationCount.previousDays.nonIndividual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.amendedAuthorisationCount.currentDay.individual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.amendedAuthorisationCount.currentDay.nonIndividual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.amendedAuthorisationCount.previousDays.individual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.amendedAuthorisationCount.previousDays.nonIndividual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.expiredAuthorisationCount.currentDay.individual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.expiredAuthorisationCount.currentDay.nonIndividual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.expiredAuthorisationCount.previousDays.individual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.expiredAuthorisationCount.previousDays.nonIndividual"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.abandonedConsentFlowCount.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.abandonedConsentFlowCount.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.abandonmentsByStage.preIdentification.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.abandonmentsByStage.preIdentification.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.abandonmentsByStage.preAuthentication.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.abandonmentsByStage.preAuthentication.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.abandonmentsByStage.preAccountSelection.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.abandonmentsByStage.preAccountSelection.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.abandonmentsByStage.preAuthorisation.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.abandonmentsByStage.preAuthorisation.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.abandonmentsByStage.rejected.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.abandonmentsByStage.rejected.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.abandonmentsByStage.failedTokenExchange.currentDay"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "data.authorisations.abandonmentsByStage.failedTokenExchange.previousDays"))
        Assert.assertNotNull(AUTestUtil.parseResponseBody(response, "links.self"))
    }

    @Test
    void "Verify requestTime is display the current date and time of the server"() {

        LocalDateTime currentTime, utcTime = null
        String assertionString = generator.getClientAssertionJwt(AUConstants.ADMIN_API_ISSUER, AUConstants.ADMIN_API_AUDIENCE)

        def response = AURequestBuilder.buildBasicRequest(assertionString, AUConstants.X_V_HEADER_METRICS)
                .header(AUConstants.CONTENT_TYPE, "application/json")
                .header(AUConstants.X_MIN_HEADER, AUConstants.X_V_MIN_HEADER_METRICS)
                .queryParam(AUConstants.PERIOD, AUConstants.ALL)
                .baseUri(AUTestUtil.getBaseUrl(AUConstants.BASE_PATH_TYPE_ADMIN))
                .get("${AUConstants.CDS_ADMIN_PATH}${AUConstants.ADMIN_METRICS}")

        Assert.assertEquals(response.statusCode(), AUConstants.STATUS_CODE_200)
        Assert.assertEquals(response.getHeader(AUConstants.X_V_HEADER).toInteger(), AUConstants.X_V_HEADER_METRICS)
        def requestTime = AUTestUtil.parseResponseBody(response, "data.requestTime")

        utcTime = LocalDateTime.now(ZoneId.of("Asia/Colombo"))

        if(AUTestUtil.getHostname().equalsIgnoreCase(AUConstants.LOCALHOST)) {
            // Get the response UTC time
            currentTime = utcTime
        } else {
            // Convert UTC time to GMT time and current date time format
            currentTime = utcTime.atOffset(ZoneOffset.UTC).atZoneSameInstant(ZoneOffset.ofHours(0)).toLocalDateTime()
        }

        String formattedCurrentTime = currentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))

        //Assert request time gives the current date time
        Assert.assertNotNull(requestTime)
        Assert.assertTrue(requestTime.contains(formattedCurrentTime), "Response Time in not in Expected Format")

        //Assert whether the request time in expected format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        try {
            formatter.parse(requestTime)
            Assert.assertTrue(true, "Request Time is in the correct format.")
        } catch (DateTimeParseException e) {
            Assert.fail("Request Time is not in the correct format.")
        }
    }
}

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

package org.wso2.cds.test.framework.utility


import org.wso2.bfsi.test.framework.exception.TestFrameworkException
import org.wso2.openbanking.test.framework.configuration.OBConfigurationService
import org.wso2.openbanking.test.framework.utility.RestAsRequestBuilder
import io.restassured.RestAssured
import io.restassured.config.EncoderConfig
import io.restassured.config.RestAssuredConfig
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import org.apache.http.conn.ssl.SSLSocketFactory
import org.wso2.cds.test.framework.constant.AUConstants

/**
 * AU Class for provide Basic Rest-assured Request Objects
 */
class AURestAsRequestBuilder extends RestAsRequestBuilder{

    private static OBConfigurationService configurationService = new OBConfigurationService()

    /**
     * Get Base Request Specification to invoke mock CDR register.
     *
     * @return request specification.
     */
    static RequestSpecification buildRequestToMockCDRRegister(boolean isMTLSRequired) throws TestFrameworkException {

        if (isMTLSRequired) {
            RestAssuredConfig config = null;
            SSLSocketFactory sslSocketFactory = AUTestUtil.getSslSocketFactoryForMockCDRRegister();

            if (sslSocketFactory != null) {

                config = RestAssuredConfig.newConfig().sslConfig(RestAssured.config()
                        .getSSLConfig()
                        .sslSocketFactory(AUTestUtil.getSslSocketFactoryForMockCDRRegister()));
            } else {
                throw new TestFrameworkException("Unable to retrieve the SSL socket factory");
            }
            return RestAssured.given()
                    .config(config.encoderConfig(EncoderConfig.encoderConfig()
                            .encodeContentTypeAs(AUConstants.CONTENT_TYPE_APPLICATION_JWT, ContentType.TEXT)))
                    .urlEncodingEnabled(true);

        } else {
            // Use relaxed HTTPS validation if MTLS is disabled.
            return RestAssured.given()
                    .relaxedHTTPSValidation()
                    .urlEncodingEnabled(true);
        }
    }
}


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

package org.wso2.openbanking.cds.common.testutils;

import org.apache.http.HttpStatus;
import org.testng.annotations.DataProvider;
import org.wso2.openbanking.cds.common.error.handling.models.CDSErrorMeta;
import org.wso2.openbanking.cds.common.error.handling.util.ErrorConstants;

/**
 * Data Provider for CDS Common Tests.
 */
public class CommonTestDataProvider {

    @DataProvider(name = "ClientErrorTestDataProvider")
    Object[][] getClientErrorTestDataProvider() {

        return new Object[][]{
                {"400", true},
                {"200", false},
                {"500", false}
        };
    }

    @DataProvider(name = "HttpsCodeTestDataProvider")
    Object[][] getHttpsCodeTestDataProvider() {

        return new Object[][]{
                {"400", HttpStatus.SC_BAD_REQUEST},
                {"401", HttpStatus.SC_UNAUTHORIZED},
                {"415", HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE},
                {"403", HttpStatus.SC_FORBIDDEN},
                {"404", HttpStatus.SC_NOT_FOUND},
                {"406", HttpStatus.SC_NOT_ACCEPTABLE},
                {"422", HttpStatus.SC_UNPROCESSABLE_ENTITY}
        };
    }

    @DataProvider(name = "ErrorObjectTestDataProvider")
    Object[][] getErrorObjectTestDataProvider() {

        return new Object[][]{
                {ErrorConstants.AUErrorEnum.CLIENT_AUTH_FAILED, "Client authentication failed", new CDSErrorMeta()},
                {ErrorConstants.AUErrorEnum.UNEXPECTED_ERROR, "Unexpected error", new CDSErrorMeta()},
                {ErrorConstants.AUErrorEnum.BAD_REQUEST, "Bad request", new CDSErrorMeta()}
        };
    }
}

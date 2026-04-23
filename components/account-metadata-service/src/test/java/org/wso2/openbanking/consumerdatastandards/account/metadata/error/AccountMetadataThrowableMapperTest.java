/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.openbanking.consumerdatastandards.account.metadata.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.ErrorResponse;

import javax.ws.rs.core.Response;

/**
 * Unit tests for {@link AccountMetadataThrowableMapper}.
 */
public class AccountMetadataThrowableMapperTest {

    private final AccountMetadataThrowableMapper mapper = new AccountMetadataThrowableMapper();

    /**
     * Verifies that a direct InvalidFormatException returns 400 with the "Invalid format in request:" prefix.
     */
    @Test
    public void testDirectInvalidFormatExceptionReturns400() {
        InvalidFormatException ife = Mockito.mock(InvalidFormatException.class);
        Mockito.when(ife.getMessage()).thenReturn("bad enum value");

        Response response = mapper.toResponse(ife);

        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
        ErrorResponse body = (ErrorResponse) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertTrue(body.getErrorDescription().startsWith("Invalid format in request:"));
    }

    /**
     * Verifies that a direct JsonProcessingException (non-InvalidFormat) returns 400 with "Invalid request:" prefix.
     */
    @Test
    public void testDirectJsonProcessingExceptionReturns400() {
        JsonProcessingException jpe = Mockito.mock(JsonProcessingException.class);
        Mockito.when(jpe.getMessage()).thenReturn("malformed json");

        Response response = mapper.toResponse(jpe);

        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
        ErrorResponse body = (ErrorResponse) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertTrue(body.getErrorDescription().startsWith("Invalid request:"));
    }

    /**
     * Verifies that an InvalidFormatException wrapped in a RuntimeException is unwrapped and returns 400.
     */
    @Test
    public void testWrappedInvalidFormatExceptionReturns400() {
        InvalidFormatException ife = Mockito.mock(InvalidFormatException.class);
        Mockito.when(ife.getMessage()).thenReturn("wrapped format error");
        RuntimeException wrapped = new RuntimeException("outer wrapper", ife);

        Response response = mapper.toResponse(wrapped);

        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
        ErrorResponse body = (ErrorResponse) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertTrue(body.getErrorDescription().startsWith("Invalid format in request:"));
    }

    /**
     * Verifies that an unrelated RuntimeException with no Jackson cause returns 500.
     */
    @Test
    public void testUnrelatedRuntimeExceptionReturns500() {
        RuntimeException rte = new RuntimeException("something unexpected happened");

        Response response = mapper.toResponse(rte);

        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        ErrorResponse body = (ErrorResponse) response.getEntity();
        Assert.assertNotNull(body);
        Assert.assertEquals(body.getErrorDescription(), "An unexpected error occurred");
    }
}

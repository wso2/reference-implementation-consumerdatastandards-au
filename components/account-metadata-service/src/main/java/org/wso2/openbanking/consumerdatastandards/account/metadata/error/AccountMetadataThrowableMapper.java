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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.consumerdatastandards.account.metadata.model.ErrorResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Maps all throwables to structured error responses.
 * Catches Jackson deserialization errors (invalid enum values, malformed JSON)
 * and returns 400 Bad Request instead of the default 500.
 */
public class AccountMetadataThrowableMapper implements ExceptionMapper<Throwable> {

    private static final Log log = LogFactory.getLog(AccountMetadataThrowableMapper.class);

    @Override
    public Response toResponse(Throwable throwable) {

        Throwable cause = throwable;
        while (cause != null) {
            if (isCausedBy(cause, InvalidFormatException.class)) {
                log.error("[AccountMetadata] Invalid format in request body: " + cause.getMessage());
                return Response.status(Response.Status.BAD_REQUEST)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(new ErrorResponse().errorDescription("Invalid format in request: "
                                + cause.getMessage()))
                        .build();
            } else if (isCausedBy(cause, JsonProcessingException.class)) {
                log.error("[AccountMetadata] Invalid request body: " + cause.getMessage());
                return Response.status(Response.Status.BAD_REQUEST)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(new ErrorResponse().errorDescription("Invalid request: " + cause.getMessage()))
                        .build();
            }
            cause = cause.getCause();
        }

        log.error("[AccountMetadata] Unhandled exception: " + throwable.getMessage(), throwable);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse().errorDescription("An unexpected error occurred"))
                .build();
    }

    /**
     * Checks if a throwable is an instance of the given class by name, tolerating classloader boundaries.
     * Using Class.getName() instead of instanceof handles the case where the exception is thrown by a class
     * loaded in a different classloader (e.g. the server's OSGi Jackson bundle vs the WAR's WEB-INF/lib copy).
     */
    private static boolean isCausedBy(Throwable throwable, Class<?> targetClass) {
        String targetName = targetClass.getName();
        Class<?> cls = throwable.getClass();
        while (cls != null) {
            if (targetName.equals(cls.getName())) {
                return true;
            }
            cls = cls.getSuperclass();
        }
        return false;
    }
}

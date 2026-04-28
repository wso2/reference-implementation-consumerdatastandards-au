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

import javax.validation.ConstraintViolationException;
import javax.ws.rs.WebApplicationException;
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

        WebApplicationException webApplicationException = findWebApplicationException(throwable);
        if (webApplicationException != null) {
            Response response = webApplicationException.getResponse();
            int statusCode = response != null ? response.getStatus()
                : Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
            String message = webApplicationException.getMessage();
            if (message == null || message.trim().isEmpty()) {
            message = response != null && response.getStatusInfo() != null
                ? response.getStatusInfo().getReasonPhrase()
                : "Request failed";
            }

            log.warn("[AccountMetadata] WebApplicationException mapped with status " + statusCode +
                ": " + message);
            return Response.status(statusCode)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse().errorDescription(message))
                .build();
        }

        Throwable cause = throwable;
        while (cause != null) {
            if (isCausedBy(cause, ConstraintViolationException.class)) {
                log.error("[AccountMetadata] Constraint violation in request: " + cause.getMessage());
                return Response.status(Response.Status.BAD_REQUEST)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(new ErrorResponse().errorDescription("Validation failed: " + cause.getMessage()))
                        .build();
            } else if (isCausedBy(cause, InvalidFormatException.class)) {
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
     * Search the causal chain for a {@link WebApplicationException} and return it if found.
     * This allows existing JAX-RS exceptions to be mapped through transparently.
     *
     * @param throwable the root throwable to inspect
     * @return the first {@link WebApplicationException} found in the cause chain, or {@code null}
     *         if none is present
     */
    private static WebApplicationException findWebApplicationException(Throwable throwable) {
        Throwable cause = throwable;
        while (cause != null) {
            if (cause instanceof WebApplicationException) {
                return (WebApplicationException) cause;
            }
            cause = cause.getCause();
        }
        return null;
    }

    /**
     * Determine whether the provided {@code throwable} is an instance of the given
     * {@code targetClass}, comparing by class name to tolerate classloader boundaries.
     *
     * <p>Using the class name rather than {@code instanceof} allows detection of
     * exception types that may have been loaded by a different classloader (common
     * in servlet/OSGi environments).</p>
     *
     * @param throwable the throwable to inspect
     * @param targetClass the exception class to match (by name)
     * @return {@code true} if the throwable is an instance of {@code targetClass}
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

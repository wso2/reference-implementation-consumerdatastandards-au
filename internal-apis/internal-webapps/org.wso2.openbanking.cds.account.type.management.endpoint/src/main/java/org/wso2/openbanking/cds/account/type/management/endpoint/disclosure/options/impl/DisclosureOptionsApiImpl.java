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

package org.wso2.openbanking.cds.account.type.management.endpoint.disclosure.options.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.cds.account.type.management.endpoint.disclosure.options.api.DisclosureOptionsApi;
import org.wso2.openbanking.cds.account.type.management.endpoint.disclosure.options.handler.DisclosureOptionsApiHandler;
import org.wso2.openbanking.cds.account.type.management.endpoint.disclosure.options.model.DOMSStatusUpdateListDTO;
import org.wso2.openbanking.cds.account.type.management.endpoint.model.ErrorDTO;
import org.wso2.openbanking.cds.account.type.management.endpoint.model.ErrorStatusEnum;
import org.wso2.openbanking.cds.account.type.management.endpoint.util.ValidationUtil;

import javax.ws.rs.core.Response;

/**
 * Disclosure Options API Implementation.
 */
public class DisclosureOptionsApiImpl implements DisclosureOptionsApi {

    private static final Log log = LogFactory.getLog(DisclosureOptionsApiImpl.class);
    DisclosureOptionsApiHandler disclosureOptionsApiHandler = new DisclosureOptionsApiHandler();

    /**
     * The following method updates the Disclosure Options.
     */
    public Response updateCDSAccountDisclosureOptions(String requestBody) {

        ObjectMapper objectMapper = new ObjectMapper();
        DOMSStatusUpdateListDTO domsStatusUpdateListDTO;

        try {
            domsStatusUpdateListDTO = objectMapper.readValue(requestBody, DOMSStatusUpdateListDTO.class);

            String validationError = ValidationUtil.getFirstViolationMessage(domsStatusUpdateListDTO);

            if (validationError.isEmpty()) {
                disclosureOptionsApiHandler.updateCDSAccountDisclosureOptions(requestBody);
                return Response.ok().entity("").build();
            } else {
                ErrorDTO errorDTO = new ErrorDTO(ErrorStatusEnum.INVALID_REQUEST, validationError);
                return Response.status(Response.Status.BAD_REQUEST).entity(errorDTO).build();
            }

        } catch (JsonProcessingException e) {
            ErrorDTO errorDTO = new ErrorDTO(ErrorStatusEnum.INVALID_REQUEST, "Error occurred while " +
                    "parsing the request body");
            return Response.status(Response.Status.BAD_REQUEST).entity(errorDTO).build();
        }
    }
}


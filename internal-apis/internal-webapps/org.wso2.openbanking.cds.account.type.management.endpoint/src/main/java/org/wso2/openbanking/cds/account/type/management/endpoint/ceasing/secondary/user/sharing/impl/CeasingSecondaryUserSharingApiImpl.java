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
package org.wso2.openbanking.cds.account.type.management.endpoint.ceasing.secondary.user.sharing.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.cds.account.type.management.endpoint.ceasing.secondary.user.sharing.api.CeasingSecondaryUserSharingApi;
import org.wso2.openbanking.cds.account.type.management.endpoint.ceasing.secondary.user.sharing.handler.CeasingSecondaryUserSharingHandler;
import org.wso2.openbanking.cds.account.type.management.endpoint.ceasing.secondary.user.sharing.models.LegalEntityListUpdateDTO;
import org.wso2.openbanking.cds.account.type.management.endpoint.ceasing.secondary.user.sharing.models.UsersAccountsLegalEntitiesDTO;
import org.wso2.openbanking.cds.account.type.management.endpoint.constants.AccountTypeManagementConstants;
import org.wso2.openbanking.cds.account.type.management.endpoint.model.ErrorDTO;
import org.wso2.openbanking.cds.account.type.management.endpoint.model.ErrorStatusEnum;
import org.wso2.openbanking.cds.account.type.management.endpoint.model.SecondaryAccountOwnerTypeEnum;
import org.wso2.openbanking.cds.account.type.management.endpoint.util.ValidationUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import javax.ws.rs.core.Response;


/**
 * Implementation Class for Ceasing Secondary User Sharing.
 */
public class CeasingSecondaryUserSharingApiImpl implements CeasingSecondaryUserSharingApi {

    private static final Log log = LogFactory.getLog(CeasingSecondaryUserSharingApiImpl.class);
    CeasingSecondaryUserSharingHandler ceasingSecondaryUserHandler = new CeasingSecondaryUserSharingHandler();

    /**
     * {@inheritDoc}
     */
    public Response updateLegalEntitySharingStatus(String requestBody) throws OpenBankingException {
        ObjectMapper objectMapper = new ObjectMapper();
        LegalEntityListUpdateDTO legalEntityListDTO;

        try {
            legalEntityListDTO = objectMapper.readValue(requestBody, LegalEntityListUpdateDTO.class);

            //Validating the requestBody
            String validationError = ValidationUtil.getFirstViolationMessage(legalEntityListDTO);

            if (validationError.isEmpty()) {
                ceasingSecondaryUserHandler.updateLegalEntitySharingStatus(legalEntityListDTO);

                log.debug("Success!, the sharing status for legal entity/entities has been unblocked.");
                return Response.ok().build();
            } else {
                log.error("Error occurred while updating the sharing status for a legal entity/entities.");
                ErrorDTO errorDTO = new ErrorDTO(ErrorStatusEnum.INVALID_REQUEST,
                        "Error occurred while updating the sharing status for a legal entity/entities.");
                return Response.status(Response.Status.BAD_REQUEST).entity(errorDTO).build();
            }

        } catch (JsonProcessingException e) {
            log.error("Error occurred while processing the JSON object.", e);
            ErrorDTO errorDTO = new ErrorDTO(ErrorStatusEnum.INVALID_REQUEST,
                    "Error occurred while processing the JSON object.");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorDTO).build();
        }
    }

    /**
     * {@inheritDoc}
     */
    public Response getUsersAccountsLegalEntities(String userID) {

        String userIDError = null;

        // Add carbon tenant domain to the userID if it does not exist
        if (!userID.toLowerCase(Locale.ENGLISH).endsWith(AccountTypeManagementConstants.CARBON_TENANT_DOMAIN)) {
            userID = userID + AccountTypeManagementConstants.CARBON_TENANT_DOMAIN;
        }

        try {
            ConsentCoreService consentCoreService = new ConsentCoreServiceImpl();

            // Creating an array list to append the userID
            ArrayList<String> userIDAL = new ArrayList<>();
            userIDAL.add(userID);

            UsersAccountsLegalEntitiesDTO responseUsersAccountsLegalEntitiesDTO =
                    new UsersAccountsLegalEntitiesDTO(userID);

            ArrayList<DetailedConsentResource> responseDetailedConsents = consentCoreService.searchDetailedConsents
                    (null, null, null, new ArrayList<>(Collections.singletonList(AccountTypeManagementConstants.
                            AUTHORIZED_STATUS)), userIDAL, null, null, null, null, false);

            // Checking the validity of the userID
            if (responseDetailedConsents.size() == 0) {
                userIDError = "Error!, user not found with userID: " + userID;
                log.error(userIDError);
                ErrorDTO errorDTO = new ErrorDTO(ErrorStatusEnum.INVALID_REQUEST,
                        userIDError);
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(errorDTO).build();
            }

            // Checking if the user is a secondary account owner
            boolean isSecondaryAccountOwner = false;

            for (DetailedConsentResource detailedConsent : responseDetailedConsents) {

                for (AuthorizationResource authorizationResource : detailedConsent.getAuthorizationResources()) {
                    if (authorizationResource.getUserID().equals(userID) &&
                            SecondaryAccountOwnerTypeEnum.isValidOwnerType(
                                    authorizationResource.getAuthorizationType())) {
                        isSecondaryAccountOwner = true;
                        break;
                    }
                }
            }

            if (!isSecondaryAccountOwner) {
                userIDError = "Error, UserID: " + userID + " is not a secondary account owner";
                log.error(userIDError);
                ErrorDTO errorDTO = new ErrorDTO(ErrorStatusEnum.INVALID_REQUEST,
                        userIDError);
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(errorDTO).build();
            }

            UsersAccountsLegalEntitiesDTO responseUsersAccountsLegalEntities = ceasingSecondaryUserHandler.
                    getUsersAccountsLegalEntities(responseDetailedConsents, responseUsersAccountsLegalEntitiesDTO);
            return Response.ok().entity(responseUsersAccountsLegalEntities).build();

        } catch (OpenBankingException e) {
            log.error("Error occurred while retrieving users,accounts and legal entities.", e);
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }
}


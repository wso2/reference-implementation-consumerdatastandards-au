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

package org.wso2.openbanking.cds.account.type.management.endpoint.secondary.user.instruction.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.wso2.openbanking.accelerator.account.metadata.service.service.AccountMetadataService;
import com.wso2.openbanking.accelerator.account.metadata.service.service.AccountMetadataServiceImpl;
import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.cds.account.type.management.endpoint.constants.AccountTypeManagementConstants;
import org.wso2.openbanking.cds.account.type.management.endpoint.model.ErrorDTO;
import org.wso2.openbanking.cds.account.type.management.endpoint.model.ErrorStatusEnum;
import org.wso2.openbanking.cds.account.type.management.endpoint.secondary.user.instruction.api.SecondaryUserInstructionApi;
import org.wso2.openbanking.cds.account.type.management.endpoint.secondary.user.instruction.model.SecondaryUserAccountStatusData;
import org.wso2.openbanking.cds.account.type.management.endpoint.util.ValidationUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response;

/**
 * Handler class for handling Secondary Account Status update requests.
 */
public class SecondaryUserInstructionApiImpl implements SecondaryUserInstructionApi {

    private static final Log log = LogFactory.getLog(SecondaryUserInstructionApiImpl.class);

    private static final String DATA = "data";
    private static final String AUTHORIZED = "authorized";
    private static final String EXPIRED = "Expired";
    private final AccountMetadataService accountMetadataService = AccountMetadataServiceImpl.getInstance();
    private final ConsentCoreServiceImpl consentCoreService = new ConsentCoreServiceImpl();


    /**
     * {@inheritDoc}
     */
    public Response updateSecondaryAccountStatus(String requestObject) {

        log.debug("Handling secondary account status update request.");
        String errMsg;
        Gson gson = new Gson();

        try {
            JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
            JSONObject requestJson = (JSONObject) parser.parse(requestObject);
            JSONArray accountsArray = (JSONArray) requestJson.get(DATA);
            ObjectMapper objectMapper = new ObjectMapper();
            SecondaryUserAccountStatusData secondaryUserAccountStatusData;

            // Map request body data to SecondaryUserAccountStatusData
            for (int accountDataEntry = 0; accountDataEntry < accountsArray.size(); accountDataEntry++) {

                String secondaryAccountData = accountsArray.get(accountDataEntry).toString();
                // validate the secondary user account update data
                secondaryUserAccountStatusData = objectMapper.readValue(secondaryAccountData,
                        SecondaryUserAccountStatusData.class);
                String validationError = ValidationUtil.getFirstViolationMessage(secondaryUserAccountStatusData);

                if (!validationError.isEmpty()) {
                    log.error("Invalid request body in secondary account status update request: " +
                            validationError);
                    ErrorDTO errorDTO = new ErrorDTO(ErrorStatusEnum.INVALID_REQUEST, validationError);
                    return Response.status(Response.Status.BAD_REQUEST).entity(gson.toJson(errorDTO)).build();
                } else {
                    // Proceed to store secondary account status data in database
                    if (accountMetadataService.addOrUpdateAccountMetadata(
                            secondaryUserAccountStatusData.getSecondaryAccountID(),
                            secondaryUserAccountStatusData.getSecondaryUserID(),
                            secondaryUserAccountStatusData.getAccountMetadataMap()) > 0) {

                        // handle the consents based on the otherAccountsAvailability flag
                        expireConsentsBasedOnOtherAccountAvailability(secondaryUserAccountStatusData);

                        log.debug("Successfully updated secondary account status data for account id: " +
                                secondaryUserAccountStatusData.getSecondaryAccountID());
                    } else {
                        errMsg = "Error occurred while persisting secondary account status data for " +
                                "account id:" + secondaryUserAccountStatusData.getSecondaryAccountID();
                        return getInternalServerErrorResponse(errMsg);
                    }
                }
            }
            return Response.status(Response.Status.OK).build();
        } catch (ConsentException e) {
            errMsg = "Error occurred while expiring consents for the secondary user." + e.getMessage();
            return getInternalServerErrorResponse(errMsg);
        } catch (OpenBankingException e) {
            errMsg = "Error occurred while persisting secondary account status data using account metadata service. " +
                    "Database not updated:" + e.getMessage();
            return getInternalServerErrorResponse(errMsg);
        } catch (ParseException | JsonProcessingException e) {
            errMsg = "Error occurred while parsing the json request object.";
            return getInternalServerErrorResponse(errMsg);
        }
    }

    /**
     * Expire all the consents created by the secondary user that includes the given secondary account
     * of which the account owner has removed the secondary account instruction status
     * if secondary user don't have any other accounts with the DH.
     *
     * @param secondaryUserAccountStatusData
     * @throws ConsentException
     */
    private void expireConsentsBasedOnOtherAccountAvailability(
            SecondaryUserAccountStatusData secondaryUserAccountStatusData) throws ConsentException {

        Boolean isDataSharingActivation = AccountTypeManagementConstants.ACTIVE_STATUS
                .equalsIgnoreCase(secondaryUserAccountStatusData.getSecondaryAccountInstructionStatus());

        try {
            ArrayList<DetailedConsentResource> consentsWithActiveSecondaryAccountMappings =
                    getActiveConsentsWithSecondaryAccountMappings(secondaryUserAccountStatusData.getSecondaryUserID(),
                            secondaryUserAccountStatusData.getSecondaryAccountID());
            if (!isDataSharingActivation) {

                for (DetailedConsentResource detailedConsentResource : consentsWithActiveSecondaryAccountMappings) {
                    if (!secondaryUserAccountStatusData.getOtherAccountsAvailability()) {
                        // expire consent
                        consentCoreService.updateConsentStatus(
                                detailedConsentResource.getConsentID(), EXPIRED);
                    }
                }
            }
        } catch (ConsentManagementException e) {
            log.error("Error occurred while handling the consent upon secondary user account update:", e);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Retrieve active consents with secondary accounts mappings.
     *
     * @param userId
     * @param accountId
     * @throws ConsentManagementException
     */
    private ArrayList<DetailedConsentResource> getActiveConsentsWithSecondaryAccountMappings(
            String userId, String accountId)
            throws ConsentManagementException {

        ArrayList<String> consentStatuses = new ArrayList<>(Collections.singletonList(AUTHORIZED));
        ArrayList<String> userIds = new ArrayList<>(Collections.singletonList(userId));

        // retrieve active consent for the current user
        ArrayList<DetailedConsentResource> activeAccountConsentsBoundToGivenUserId = consentCoreService.
                searchDetailedConsents(null, null, null, consentStatuses,
                        userIds, null, null, null, null);

        // filter consents with secondary account mappings of given mapping status
        ArrayList<DetailedConsentResource> activeConsentsWithSecondaryAccountMappings =
                activeAccountConsentsBoundToGivenUserId
                        .stream()
                        .filter(detailedConsentResource -> detailedConsentResource.getConsentMappingResources()
                                .stream()
                                .anyMatch(consentMappingResource ->
                                        consentMappingResource.getAccountID().equals(accountId)
                                                && consentMappingResource.getMappingStatus().equals(
                                                AccountTypeManagementConstants.ACTIVE_STATUS)))
                        .collect(Collectors.toCollection(ArrayList::new));
        return activeConsentsWithSecondaryAccountMappings;
    }

    /**
     * Get Internal Server Error Response.
     *
     * @param errMsg - error messege
     * @return error response
     */
    private Response getInternalServerErrorResponse(String errMsg) {
        log.error(errMsg);
        Gson gson = new Gson();
        ErrorDTO errorDTO = new ErrorDTO(ErrorStatusEnum.INTERNAL_SERVER_ERROR, errMsg);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(gson.toJson(errorDTO)).build();
    }

}

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

package org.wso2.openbanking.cds.consent.extensions.validate;

import com.wso2.openbanking.accelerator.account.metadata.service.service.AccountMetadataServiceImpl;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidateData;
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidationResult;
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidator;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;
import org.wso2.openbanking.cds.common.error.handling.util.ErrorConstants;
import org.wso2.openbanking.cds.common.metadata.domain.MetadataValidationResponse;
import org.wso2.openbanking.cds.common.metadata.status.validator.service.MetadataService;
import org.wso2.openbanking.cds.consent.extensions.common.CDSConsentExtensionConstants;
import org.wso2.openbanking.cds.consent.extensions.util.CDSConsentExtensionsUtil;
import org.wso2.openbanking.cds.consent.extensions.validate.utils.CDSConsentValidatorUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import static com.wso2.openbanking.accelerator.consent.mgt.service.constants.ConsentCoreServiceConstants.INACTIVE_MAPPING_STATUS;

/**
 * Consent validator CDS implementation.
 */
public class CDSConsentValidator implements ConsentValidator {

    private static final Log log = LogFactory.getLog(CDSConsentValidator.class);
    AccountMetadataServiceImpl accountMetadataService = AccountMetadataServiceImpl.getInstance();

    @Override
    public void validate(ConsentValidateData consentValidateData, ConsentValidationResult consentValidationResult)
            throws ConsentException {

        JSONObject receiptJSON;
        OpenBankingCDSConfigParser openBankingCDSConfigParser = OpenBankingCDSConfigParser.getInstance();

        try {
            receiptJSON = (JSONObject) (new JSONParser(JSONParser.MODE_PERMISSIVE)).
                    parse(consentValidateData.getComprehensiveConsent().getReceipt());
        } catch (ParseException e) {
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    "Error occurred while parsing consent data");
        }

        // consent status validation
        if (!CDSConsentExtensionConstants.AUTHORIZED_STATUS
                .equalsIgnoreCase(consentValidateData.getComprehensiveConsent().getCurrentStatus())) {
            consentValidationResult.setErrorMessage(generateErrorPayload("Consent Is Revoked",
                    "The consumer's consent is revoked", null, null));
            consentValidationResult.setErrorCode(ErrorConstants.REVOKED_CONSENT_STATUS);
            consentValidationResult.setHttpCode(HttpStatus.SC_FORBIDDEN);
            return;
        }

        // consent expiry validation
        if (CDSConsentValidatorUtil
                .isConsentExpired(((JSONObject) receiptJSON.get(CDSConsentExtensionConstants.ACCOUNT_DATA))
                        .getAsString(CDSConsentExtensionConstants.EXPIRATION_DATE_TIME))) {
            String description = "The associated consent for resource is not in a status " +
                    "that would allow the resource to be executed";
            consentValidationResult.setErrorMessage(generateErrorPayload("Consent Is Invalid", description,
                    null, null));
            consentValidationResult.setErrorCode(ErrorConstants.INVALID_CONSENT_STATUS);
            consentValidationResult.setHttpCode(HttpStatus.SC_FORBIDDEN);
            return;
        }

        // perform consent bounded account filtration
        filterConsentAccountMappings(consentValidateData);

        // account ID Validation
        String isAccountIdValidationEnabled = openBankingCDSConfigParser.getConfiguration()
                .get(CDSConsentExtensionConstants.ENABLE_ACCOUNT_ID_VALIDATION_ON_RETRIEVAL).toString();

        if (Boolean.parseBoolean(isAccountIdValidationEnabled) &&
                !CDSConsentValidatorUtil.isAccountIdValid(consentValidateData)) {

            ArrayList<String> requestPathResources = new ArrayList<>(Arrays.asList(consentValidateData.
                    getRequestPath().split("/")));
            int indexOfAccountID = requestPathResources.indexOf("{accountId}");
            String accountId = new ArrayList<>(Arrays.asList(consentValidateData.getResourceParams()
                    .get("ResourcePath").split("/"))).get(indexOfAccountID);

            consentValidationResult.setErrorMessage(generateErrorPayload("Invalid Banking Account",
                    "ID of the account not found or invalid", null, accountId));
            consentValidationResult.setErrorCode(ErrorConstants.RESOURCE_INVALID_BANKING_ACCOUNT);
            consentValidationResult.setHttpCode(HttpStatus.SC_NOT_FOUND);
            return;
        }

        // validate requested account ids for POST calls
        String httpMethod = consentValidateData.getResourceParams().get(CDSConsentExtensionConstants.HTTP_METHOD);
        if (CDSConsentExtensionConstants.POST_METHOD.equals(httpMethod)) {

            String validationResult = CDSConsentValidatorUtil.validAccountIdsInPostRequest(consentValidateData);
            if (!ErrorConstants.SUCCESS.equals(validationResult)) {
                consentValidationResult.setErrorMessage(generateErrorPayload("Invalid Banking Account",
                        "ID of the account not found or invalid", null, validationResult));
                consentValidationResult.setErrorCode(ErrorConstants.RESOURCE_INVALID_BANKING_ACCOUNT);
                consentValidationResult.setHttpCode(HttpStatus.SC_UNPROCESSABLE_ENTITY);
                return;
            }
        }

        // validate metadata status
        if (openBankingCDSConfigParser.isMetadataCacheEnabled()) {
            MetadataValidationResponse metadataValidationResp =
                    MetadataService.shouldDiscloseCDRData(consentValidateData.getClientId());
            if (!metadataValidationResp.isValid()) {
                consentValidationResult.setErrorMessage(generateErrorPayload("ADR Status Is Not Active",
                        metadataValidationResp.getErrorMessage(), null, null));
                consentValidationResult.setErrorCode(ErrorConstants.AUErrorEnum.INVALID_ADR_STATUS.getCode());
                consentValidationResult.setHttpCode(ErrorConstants.AUErrorEnum.INVALID_ADR_STATUS.getHttpCode());
                return;
            }
        }

        consentValidationResult.setValid(true);
    }

    /**
     * Method to remove inactive mappings from consentValidateData.
     *
     * @param consentValidateData consentValidateData
     */
    private void removeInactiveConsentMappings(ConsentValidateData consentValidateData) {
        ArrayList<ConsentMappingResource> activeMappingResources = new ArrayList<>();

        consentValidateData.getComprehensiveConsent().getConsentMappingResources().stream()
                .filter(mapping -> !INACTIVE_MAPPING_STATUS.equals(mapping.getMappingStatus()))
                .forEach(distinctMapping -> {
                    activeMappingResources.add(distinctMapping);
                });
        consentValidateData.getComprehensiveConsent().setConsentMappingResources(activeMappingResources);
    }

    /**
     * Method to remove duplicate mappings from consentValidateData.
     *
     * @param consentValidateData consentValidateData
     */
    private void removeDuplicateConsentMappings(ConsentValidateData consentValidateData) {
        ArrayList<ConsentMappingResource> distinctMappingResources = new ArrayList<>();
        List<String> duplicateAccountIds = new ArrayList<>();

        consentValidateData.getComprehensiveConsent().getConsentMappingResources().stream()
                .filter(mapping -> !duplicateAccountIds.contains(mapping.getAccountID()))
                .forEach(distinctMapping -> {
                    distinctMappingResources.add(distinctMapping);
                    duplicateAccountIds.add(distinctMapping.getAccountID());
                });
        consentValidateData.getComprehensiveConsent().setConsentMappingResources(distinctMappingResources);
    }

    /**
     * Remove joint accounts mappings if disclosure options status is not 'pre-approval'.
     *
     * @param consentValidateData consentValidateData
     * @throws ConsentException ConsentException
     */
    public void removeInactiveDOMSAccountConsentMappings(ConsentValidateData consentValidateData)
            throws ConsentException {

        ArrayList<ConsentMappingResource> consentMappingResources = new ArrayList<>(consentValidateData
                .getComprehensiveConsent().getConsentMappingResources());
        Iterator<ConsentMappingResource> consentMappingIterator = consentMappingResources.iterator();

        // Remove joint account mappings if disclosure options status is not 'pre-approval'
        while (consentMappingIterator.hasNext()) {
            ConsentMappingResource mappingResource = consentMappingIterator.next();
            try {
                if (!CDSConsentExtensionsUtil.isDOMSStatusEligibleForDataSharing(mappingResource.getAccountID())) {
                    consentMappingIterator.remove();
                    log.info("Removed mapping resource for accountID: " + mappingResource.getAccountID());
                }
            } catch (OpenBankingException e) {
                log.error("Error occurred while retrieving account metadata", e);
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                        "Error occurred while retrieving account metadata");
            }
        }
        consentValidateData.getComprehensiveConsent().setConsentMappingResources(consentMappingResources);
    }

    /**
     * Method to filter accounts based on the sharing status of legal entity.
     *
     * @param consentValidateData consentValidateData
     */
    private void removeBlockedLegalEntityConsentMappings(ConsentValidateData consentValidateData) throws
            ConsentException {
        ArrayList<ConsentMappingResource> validMappingResources = new ArrayList<>();

        try {

            String secondaryUserID = consentValidateData.getUserId();

            for (ConsentMappingResource consentMappingResource : consentValidateData.
                    getComprehensiveConsent().getConsentMappingResources()) {

                String accountID = consentMappingResource.getAccountID();
                String clientID = consentValidateData.getClientId();

                if (StringUtils.isNotBlank(clientID) && StringUtils.isNotBlank(accountID) &&
                        StringUtils.isNotBlank(secondaryUserID)) {
                    boolean isLegalEntitySharingStatusBlocked = CDSConsentExtensionsUtil.
                            isLegalEntityBlockedForAccountAndUser(accountID, secondaryUserID, clientID);

                    if (!isLegalEntitySharingStatusBlocked) {
                        validMappingResources.add(consentMappingResource);
                    }
                }
            }
            consentValidateData.getComprehensiveConsent().setConsentMappingResources(validMappingResources);
        } catch (ConsentException e) {
            log.error("Error occurred while retrieving account metadata");
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    "Error occurred while retrieving account metadata");
        }
    }

    /**
     * Method to remove inactive secondary user account consent mappings from consentValidateData.
     *
     * @param consentValidateData consentValidateData
     */
    private void removeInactiveSecondaryUserAccountConsentMappings(ConsentValidateData consentValidateData) {
        ArrayList<ConsentMappingResource> consentMappingResources =
                consentValidateData.getComprehensiveConsent().getConsentMappingResources();
        List<String> blockedSecondaryAccounts = new ArrayList<>();

        // remove inactive secondary user account consent mappings
        for (ConsentMappingResource mappingResource : consentMappingResources) {
            if (CDSConsentExtensionConstants.SECONDARY_ACCOUNT_USER.equals(mappingResource.getPermission()) &&
                    !CDSConsentExtensionsUtil
                            .isUserEligibleForSecondaryAccountDataSharing(mappingResource.getAccountID(),
                                    consentValidateData.getUserId())) {
                blockedSecondaryAccounts.add(mappingResource.getAccountID());
            }
        }

        // remove inactive secondary user account mappings
        Predicate<ConsentMappingResource> blockedSecondaryAccountDuplicateFilter = mapping ->
                blockedSecondaryAccounts.contains(mapping.getAccountID());
        consentMappingResources.removeIf(blockedSecondaryAccountDuplicateFilter);

        consentValidateData.getComprehensiveConsent().setConsentMappingResources(consentMappingResources);
    }

    /**
     * Method to remove accounts which the user has "REVOKED" nominated representative permissions.
     *
     * @param consentValidateData consentValidateData
     */
    private void removeAccountsWithRevokedBNRPermission(ConsentValidateData consentValidateData) throws
            ConsentException {
        ArrayList<ConsentMappingResource> validMappingResources = new ArrayList<>();
        ArrayList<ConsentMappingResource> consentMappingResources = consentValidateData.getComprehensiveConsent().
                getConsentMappingResources();
        try {
            for (ConsentMappingResource consentMappingResource : consentMappingResources) {
                String accountId = consentMappingResource.getAccountID();
                String userId = consentValidateData.getUserId();
                userId = userId.replaceAll("(@carbon\\.super)+", "@carbon.super");
                //Todo: improve accelerator to do this with single database call.
                String bnrPermission = accountMetadataService.getAccountMetadataByKey(accountId, userId,
                        CDSConsentExtensionConstants.BNR_PERMISSION);
                if (StringUtils.isBlank(bnrPermission) || !bnrPermission.equals(CDSConsentExtensionConstants.
                        BNR_REVOKE_PERMISSION)) {
                    validMappingResources.add(consentMappingResource);
                }
            }
            consentValidateData.getComprehensiveConsent().setConsentMappingResources(validMappingResources);
        } catch (OpenBankingException e) {
            log.error("Error occurred while retrieving account metadata", e);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    "Error occurred while retrieving account metadata");
        }
    }

    /**
     * Method to filter accounts based on following criteria.
     * 1. Remove inactive consent mappings
     * 2. Remove joint accounts with no-sharing DOMs status
     * 3. Remove inactive secondary user accounts
     * 4. Remove accounts based on the sharing status of legal entity
     * 5. Remove accounts which the user has "REVOKED" nominated representative permissions.
     *
     * @param consentValidateData consentValidateData
     */
    private void filterConsentAccountMappings(ConsentValidateData consentValidateData) throws
            ConsentException {
        OpenBankingCDSConfigParser openBankingCDSConfigParser = OpenBankingCDSConfigParser.getInstance();
        // Remove inactive consent mappings
        removeInactiveConsentMappings(consentValidateData);

        // Filter joint accounts with no-sharing DOMs status
        if (openBankingCDSConfigParser.getDOMSEnabled()) {
            removeInactiveDOMSAccountConsentMappings(consentValidateData);
        }

        // filter inactive secondary user accounts
        if (openBankingCDSConfigParser.getSecondaryUserAccountsEnabled()) {
            removeInactiveSecondaryUserAccountConsentMappings(consentValidateData);
        }

        // Filter accounts based on the sharing status of legal entity
        if (openBankingCDSConfigParser.isCeasingSecondaryUserSharingEnabled()) {
            removeBlockedLegalEntityConsentMappings(consentValidateData);
        }

        // Remove accounts with revoked BNR permission if the configuration is enabled.
        if (openBankingCDSConfigParser.isBNRValidateAccountsOnRetrievalEnabled()) {
            removeAccountsWithRevokedBNRPermission(consentValidateData);
        }

        // Remove duplicate consent mappings
        removeDuplicateConsentMappings(consentValidateData);
    }

    private String generateErrorPayload(String title, String detail, String metaURN, String accountId) {

        JSONObject errorPayload = new JSONObject();
        errorPayload.put(ErrorConstants.DETAIL, detail);
        errorPayload.put(ErrorConstants.TITLE, title);

        if (StringUtils.isNotBlank(metaURN)) {
            errorPayload.put(ErrorConstants.META_URN, metaURN);
        }
        if (StringUtils.isNotBlank(accountId)) {
            errorPayload.put(ErrorConstants.ACCOUNT_ID, accountId);
        }
        return errorPayload.toString();
    }
}

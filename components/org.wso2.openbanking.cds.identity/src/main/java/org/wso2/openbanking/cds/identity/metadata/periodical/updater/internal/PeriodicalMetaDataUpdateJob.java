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

package org.wso2.openbanking.cds.identity.metadata.periodical.updater.internal;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;
import org.wso2.openbanking.cds.common.metadata.periodical.updater.MetadataHolder;
import org.wso2.openbanking.cds.identity.metadata.periodical.updater.service.dataholder.responsibility.CleanupRegistrationResponsibility;
import org.wso2.openbanking.cds.identity.metadata.periodical.updater.service.dataholder.responsibility.DataHolderResponsibilitiesExecutor;
import org.wso2.openbanking.cds.identity.metadata.periodical.updater.service.dataholder.responsibility.InvalidateConsentsResponsibility;
import org.wso2.openbanking.cds.identity.metadata.periodical.updater.service.retryer.Retryer;
import org.wso2.openbanking.cds.identity.metadata.periodical.updater.utils.Utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static org.wso2.openbanking.cds.common.metadata.periodical.updater.constants.MetadataConstants.DATA;
import static org.wso2.openbanking.cds.common.metadata.periodical.updater.constants.MetadataConstants.LEGAL_ENTITY_ID;
import static org.wso2.openbanking.cds.common.metadata.periodical.updater.constants.MetadataConstants.MAP_DATA_RECIPIENTS;
import static org.wso2.openbanking.cds.common.metadata.periodical.updater.constants.MetadataConstants.MAP_SOFTWARE_PRODUCTS;
import static org.wso2.openbanking.cds.common.metadata.periodical.updater.constants.MetadataConstants.SOFTWARE_ID;
import static org.wso2.openbanking.cds.common.metadata.periodical.updater.constants.MetadataConstants.STATUS;
import static org.wso2.openbanking.cds.common.metadata.periodical.updater.constants.MetadataConstants.STATUS_JSON_LEGAL_ENTITY_KEY;
import static org.wso2.openbanking.cds.common.metadata.periodical.updater.constants.MetadataConstants.STATUS_JSON_SP_KEY;

/**
 * Scheduled Task to get data every n minutes and perform data holder responsibilities depending
 * on the dataRecipient and softwareProduct statuses.
 * <p>
 * Within KM:
 * Uses AuthenticationAdminStub admin service HTTP endpoint to get session cookies.
 * Uses IdentityApplicationManagementServiceStub admin service HTTP endpoint to get applications list.
 * Uses IdentityApplicationManagementServiceStub admin service HTTP endpoint to get Service Provider details.
 * <p>
 * Outside KM:
 * Uses ACCC statuses HTTP endpoint to get statuses.
 * DCR internal HTTP endpoint to delete application.
 * <p>
 * The data returned from directory is a map between softwareProductId - status / dataRecipientId - status.
 * But during authorization flow / data retrieval request, when status validation needs to be done,
 * request only contains ClientID or UserID or ApplicationName. Thus at runtime there is an
 * overhead of doing APIM Admin service call for every request to get the required application attributes,
 * softwareProductId and dataRecipientId, to validate status. To eliminate this, the
 * dataRecipientID-status / softwareProductID-status maps are converted to clientID-dataRecipientIdStatus /
 * clientID-softwareProductIdStatus maps. So the CRON job is defined as follows,
 * <p>
 * Get softwareProductId and dataRecipientId status arrays from directory.
 * Get the list of applications in store.
 * For each application name,
 * Get the KM service provider and its application attributes.
 * Get its softwareProductId and dataRecipientId
 * For each of the OAuth Client IDs of the service provider,
 * Add a new entry of Client ID - status in softwareProductStatus Map.
 * Add a new entry of Client ID - status in dataRecipientStatus Map.
 * If the softwareProductId / dataRecipientId has status that needs to clean up registration,
 * For each of the OAuth Client IDs of the service provider,
 * Delete the application registered with the ClientID.
 * If the softwareProductId / dataRecipientId has status that needs to expire consents,
 * For each of the OAuth Client IDs of the service provider,
 * Revoke consents registered with the ClientID, UserID.
 * Return the newly created softwareProductStatus / dataRecipientStatus map.
 * Store the returned ClientID - status maps of softwareProductStatus and dataRecipientStatus in data holder.
 * <p>
 * StatusValidator will retrieve the maps in data holder when its cache expires. These maps will contain
 * statuses of dataRecipient and softwareProduct against clientID, but only for the applications
 * found in store, not the complete status list retrieved from directory. Complete list is dropped during the Job.
 * It won't affect the new registrations, as in DCR, status check is not performed.
 */
@DisallowConcurrentExecution
public class PeriodicalMetaDataUpdateJob implements Job, MetaDataUpdate {

    private static final Log LOG = LogFactory.getLog(PeriodicalMetaDataUpdateJob.class);

    /**
     * method used to enforce periodic metadata update.
     *
     * @param context JobExecutionContext
     */
    @Override
    public void execute(JobExecutionContext context) {
        LOG.info("Metadata update scheduled task is executing.");
        updateMetaDataValues();
    }

    /**
     * Method which triggers metadata update.
     */
    @Override
    @Generated(message = "Ignoring since all cases are covered from other unit tests")
    public void updateMetaDataValues() {

        LOG.debug("Metadata update is executing.");
        Map<String, Map<String, String>> metaDataStatuses = new HashMap<>();
        try {
            Utils.initializeTenantContextIfAbsent();
            Retryer<JSONObject> retryer = new Retryer<>(1000, OpenBankingCDSConfigParser.getInstance().
                    getRetryCount());
            Map<String, String> registerApiRequestHeaders =
                    OpenBankingCDSConfigParser.getInstance().getRegisterApiRequestHeaders();
            JSONObject drStatusResponseJson = retryer.execute(() -> Utils
                    .readJsonFromUrl(OpenBankingCDSConfigParser.getInstance().getDataRecipientStatusUrl(),
                            registerApiRequestHeaders));
            JSONObject spStatusResponseJson = retryer.execute(() -> Utils
                    .readJsonFromUrl(OpenBankingCDSConfigParser.getInstance().getSoftwareProductStatusUrl(),
                            registerApiRequestHeaders));

            if (drStatusResponseJson == null || spStatusResponseJson == null) {
                // CDR response is null, possible because Common HttpPool is not initialized yet.
                return;
            }
            Map<String, String> dataRecipientStatuses = getDataRecipientStatusesFromRegister(drStatusResponseJson);
            Map<String, String> softwareProductStatuses = getSoftwareProductStatusesFromRegister(spStatusResponseJson);

            metaDataStatuses.put(MAP_DATA_RECIPIENTS, dataRecipientStatuses);
            metaDataStatuses.put(MAP_SOFTWARE_PRODUCTS, softwareProductStatuses);

        } catch (OpenBankingException e) {
            /*
             * Continue to operate with existing metadata as per CDR expectations
             * https://cdr-register.github.io/register/#cdr-register-unavailable
             */
            LOG.error("Error while updating data recipient and service provider status. " +
                    "Continue to operate from existing metadata. Caused by, ", e);
            return;
        }

        // Perform responsibilities and set data only relevant to our store to holders
        try {
            Map<String, Map<String, String>> metaDataStatusMapsInStore = processMetadataStatus(metaDataStatuses
                    .get(MAP_DATA_RECIPIENTS), metaDataStatuses.get(MAP_SOFTWARE_PRODUCTS));

            // updating primary cache
            MetadataHolder.getInstance().setSoftwareProduct(metaDataStatusMapsInStore.get(MAP_SOFTWARE_PRODUCTS));
            MetadataHolder.getInstance().setDataRecipient(metaDataStatusMapsInStore.get(MAP_DATA_RECIPIENTS));

            if (!OpenBankingCDSConfigParser.getInstance().isBulkOperation()) {
                DataHolderResponsibilitiesExecutor.getInstance().execute();
            }
        } catch (OpenBankingException e) {
            LOG.error("Data holder responsibilities were not performed successfully", e);
        }

        LOG.debug("Metadata update is complete.");
    }

    /**
     * Get data recipient status map from CDR Register.
     *
     * @return map of data recipient IDs and statuses
     * @throws OpenBankingException - OpenBankingException
     */
    public Map<String, String> getDataRecipientStatusesFromRegister(@NotNull JSONObject responseJson)
            throws OpenBankingException {

        JSONArray dataRecipientsArray = responseJson.getJSONArray(DATA);
        Map<String, String> dataRecipientsMap = new HashMap<>();
        for (int jsonElementIndex = 0; jsonElementIndex < dataRecipientsArray.length(); jsonElementIndex++) {
            JSONObject softwareProduct = dataRecipientsArray.getJSONObject(jsonElementIndex);
            dataRecipientsMap.put(softwareProduct.getString(STATUS_JSON_LEGAL_ENTITY_KEY).toLowerCase(Locale.ENGLISH),
                    softwareProduct.getString(STATUS));
        }
        return dataRecipientsMap;
    }

    /**
     * Get software product status map from CDR Register.
     *
     * @return map of software product IDs and statuses
     * @throws OpenBankingException - OpenBankingException
     */
    public Map<String, String> getSoftwareProductStatusesFromRegister(@NotNull JSONObject responseJson)
            throws OpenBankingException {

        JSONArray softwareProductsArray = responseJson.getJSONArray(DATA);
        Map<String, String> softwareProductsMap = new HashMap<>();
        for (int jsonElementIndex = 0; jsonElementIndex < softwareProductsArray.length(); jsonElementIndex++) {
            JSONObject softwareProduct = softwareProductsArray.getJSONObject(jsonElementIndex);
            softwareProductsMap.put(softwareProduct.getString(STATUS_JSON_SP_KEY).toLowerCase(Locale.ENGLISH),
                    softwareProduct.getString(STATUS));
        }
        return softwareProductsMap;
    }

    /**
     * This method executes two tasks,
     * task 1: Modify metadata statuses map by putting client ids.
     * task 2: Add data holder responsibilities to DataHolderResponsibilitiesExecutor
     *
     * @param dataRecipientsMap   data recipients fetched from ACCC
     * @param softwareProductsMap software products fetched from ACCC
     * @return in store software products and data recipients map
     */
    protected Map<String, Map<String, String>> processMetadataStatus(Map<String, String> dataRecipientsMap,
                                                                     Map<String, String> softwareProductsMap)
            throws OpenBankingException {

        final List<ServiceProvider> serviceProviders;
        // Get service providers of all applications in store
        try {
            serviceProviders = ServiceHolder.getInstance().getIdentityCommonHelper().getAllServiceProviders();
        } catch (IdentityApplicationManagementException | UserStoreException e) {
            throw new OpenBankingException(
                    "Data holder responsibilities were not performed: service providers details was not retrieved.", e);
        }

        // Maps to store Client-ID status mapping that will be sent to secondary cache
        Map<String, String> softwareProductsMapInStore = new HashMap<>();
        Map<String, String> dataRecipientsMapInStore = new HashMap<>();

        for (ServiceProvider serviceProvider : serviceProviders) {
            String dataRecipientId = getIdFromProperties(serviceProvider.getSpProperties(), LEGAL_ENTITY_ID);
            String softwareProductId = getIdFromProperties(serviceProvider.getSpProperties(), SOFTWARE_ID);

            String dataRecipientsStatus = dataRecipientsMap.get(dataRecipientId.toLowerCase(Locale.ENGLISH));
            String softwareProductsStatus = softwareProductsMap.get(softwareProductId.toLowerCase(Locale.ENGLISH));

            if (dataRecipientsStatus != null && softwareProductsStatus != null) {

                // Add ClientIDs - Status Map
                for (InboundAuthenticationRequestConfig config :
                        serviceProvider.getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs()) {
                    softwareProductsMapInStore.put(config.getInboundAuthKey(), softwareProductsStatus);
                    dataRecipientsMapInStore.put(config.getInboundAuthKey(), dataRecipientsStatus);
                }

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Performing data holder responsibilities. dataRecipientId: " + dataRecipientId +
                            ", softwareProductId: " + softwareProductId);
                }
                // Adding responsibilities to the executor
                DataHolderResponsibilitiesExecutor.getInstance().addResponsibility(new CleanupRegistrationResponsibility
                        (dataRecipientsStatus, softwareProductsStatus, serviceProvider));
                DataHolderResponsibilitiesExecutor.getInstance().addResponsibility(new InvalidateConsentsResponsibility
                        (dataRecipientsStatus, softwareProductsStatus, serviceProvider));
            }
        }

        Map<String, Map<String, String>> metaDataStatusMap = new HashMap<>();
        metaDataStatusMap.put(MAP_DATA_RECIPIENTS, dataRecipientsMapInStore);
        metaDataStatusMap.put(MAP_SOFTWARE_PRODUCTS, softwareProductsMapInStore);

        return metaDataStatusMap;
    }

    private String getIdFromProperties(ServiceProviderProperty[] providerProperties, String displayName) {

        return Arrays.stream(providerProperties)
                .filter(property -> displayName.equalsIgnoreCase(property.getDisplayName()))
                .map(ServiceProviderProperty::getValue)
                .filter(Objects::nonNull).findFirst().orElse("");
    }
}

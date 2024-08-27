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

package org.wso2.openbanking.cds.consent.extensions.authorize.impl.retrieval;

import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentRetrievalStep;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.cds.consent.extensions.authorize.utils.PermissionsEnum;
import org.wso2.openbanking.cds.consent.extensions.common.CDSConsentExtensionConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Data cluster retrieval step CDS implementation to get human readable scopes.
 */
public class CDSDataClusterRetrievalStep implements ConsentRetrievalStep {

    private static final Log log = LogFactory.getLog(CDSDataClusterRetrievalStep.class);

    @Override
    public void execute(ConsentData consentData, JSONObject jsonObject) throws ConsentException {

        if (consentData.isRegulatory()) {
            if (!consentData.getMetaDataMap().containsKey(CDSConsentExtensionConstants.PERMISSIONS)) {
                log.error("Error: Scopes are not found in consent data.");
                return;
            }
            JSONArray scopes = new JSONArray();
            scopes.addAll((ArrayList) consentData.getMetaDataMap().get(CDSConsentExtensionConstants.PERMISSIONS));
            // Consent amendment flow
            if (jsonObject.containsKey(CDSConsentExtensionConstants.IS_CONSENT_AMENDMENT) &&
                    (boolean) jsonObject.get(CDSConsentExtensionConstants.IS_CONSENT_AMENDMENT)) {
                //get existing scopes
                JSONArray existingScopes = new JSONArray();
                JSONArray commonScopes = new JSONArray(); //scopes that are in both old and new request objects
                JSONArray newScopes = new JSONArray();
                if (jsonObject.containsKey(CDSConsentExtensionConstants.EXISTING_PERMISSIONS)) {
                    JSONArray existingPermissions = (JSONArray) jsonObject.get(CDSConsentExtensionConstants.
                            EXISTING_PERMISSIONS);
                    for (Object permission : existingPermissions) {
                        existingScopes.add(PermissionsEnum.valueOf(permission.toString()).toString());
                    }
                    for (Object scope : scopes) {
                        if (existingScopes.contains(scope.toString())) {
                            commonScopes.add(scope);
                        } else {
                            newScopes.add(scope);
                        }
                    }
                    JSONArray dataCluster = getDataClusterFromScopes(commonScopes,
                            CDSConsentExtensionConstants.INDIVIDUAL_PROFILE_TYPE);
                    JSONArray newDataCluster = getDataClusterFromScopes(newScopes,
                            CDSConsentExtensionConstants.INDIVIDUAL_PROFILE_TYPE);
                    JSONArray businessDataCluster = getDataClusterFromScopes(commonScopes,
                            CDSConsentExtensionConstants.ORGANISATION);
                    JSONArray newBusinessDataCluster = getDataClusterFromScopes(newScopes,
                            CDSConsentExtensionConstants.ORGANISATION);
                    // Add profile scope and standard claim data clusters
                    JSONObject profileDataJsonObject = processProfileDataClusters(dataCluster, newDataCluster,
                            businessDataCluster, newBusinessDataCluster, commonScopes, newScopes);
                    dataCluster = (JSONArray) profileDataJsonObject.get(CDSConsentExtensionConstants.DATA_CLUSTER);
                    newDataCluster = (JSONArray) profileDataJsonObject
                            .get(CDSConsentExtensionConstants.NEW_DATA_CLUSTER);
                    businessDataCluster = (JSONArray) profileDataJsonObject
                            .get(CDSConsentExtensionConstants.BUSINESS_DATA_CLUSTER);
                    newBusinessDataCluster = (JSONArray) profileDataJsonObject
                            .get(CDSConsentExtensionConstants.NEW_BUSINESS_DATA_CLUSTER);
                    jsonObject.put(CDSConsentExtensionConstants.DATA_REQUESTED, dataCluster);
                    jsonObject.put(CDSConsentExtensionConstants.NEW_DATA_REQUESTED, newDataCluster);
                    jsonObject.put(CDSConsentExtensionConstants.BUSINESS_DATA_CLUSTER, businessDataCluster);
                    jsonObject.put(CDSConsentExtensionConstants.NEW_BUSINESS_DATA_CLUSTER, newBusinessDataCluster);
                    jsonObject.put(CDSConsentExtensionConstants.NAME_CLAIMS,
                            profileDataJsonObject.get(CDSConsentExtensionConstants.NAME_CLAIMS));
                    jsonObject.put(CDSConsentExtensionConstants.CONTACT_CLAIMS,
                            profileDataJsonObject.get(CDSConsentExtensionConstants.CONTACT_CLAIMS));
                } else {
                    log.error("Permissions not found for the given consent");
                    throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                            "Permissions not found for the given consent");
                }
            } else {
                JSONArray dataCluster = getDataClusterFromScopes(scopes,
                        CDSConsentExtensionConstants.INDIVIDUAL_PROFILE_TYPE);
                JSONArray businessDataCluster = getDataClusterFromScopes(scopes,
                        CDSConsentExtensionConstants.ORGANISATION);
                // Add profile scope and standard claim data clusters
                JSONObject processedDataClusters = processProfileDataClusters(new JSONArray(), dataCluster,
                        new JSONArray(), businessDataCluster, new JSONArray(), scopes);
                dataCluster = (JSONArray) processedDataClusters.get(CDSConsentExtensionConstants.NEW_DATA_CLUSTER);
                businessDataCluster = (JSONArray) processedDataClusters
                        .get(CDSConsentExtensionConstants.NEW_BUSINESS_DATA_CLUSTER);
                jsonObject.put(CDSConsentExtensionConstants.DATA_REQUESTED, dataCluster);
                jsonObject.put(CDSConsentExtensionConstants.BUSINESS_DATA_CLUSTER, businessDataCluster);
            }
        }
    }

    /**
     * Get data clusters mapping to the given scopes.
     *
     * @param scopes       cds scopes
     * @param customerType customer type
     * @return data cluster
     */
    private static JSONArray getDataClusterFromScopes(JSONArray scopes, String customerType) {

        JSONArray dataCluster = new JSONArray();
        for (Object scopeEnum : scopes) {
            JSONObject dataClusterItem = new JSONObject();
            String scope = scopeEnum.toString();
            if (CDSConsentExtensionConstants.COMMON_CUSTOMER_BASIC_READ_SCOPE.equalsIgnoreCase(scope) &&
                    scopes.contains(CDSConsentExtensionConstants.COMMON_CUSTOMER_DETAIL_READ_SCOPE)) {
                continue;
            } else if (CDSConsentExtensionConstants.COMMON_ACCOUNTS_BASIC_READ_SCOPE.equalsIgnoreCase(scope) &&
                    scopes.contains(CDSConsentExtensionConstants.COMMON_ACCOUNTS_DETAIL_READ_SCOPE)) {
                continue;
            }
            Map<String, List<String>> cluster;
            if (scope.contains(CDSConsentExtensionConstants.COMMON_SUBSTRING) &&
                    CDSConsentExtensionConstants.ORGANISATION.equalsIgnoreCase(customerType)) {
                cluster = CDSConsentExtensionConstants.BUSINESS_CDS_DATA_CLUSTER.get(scope);
            } else if (scope.contains(CDSConsentExtensionConstants.COMMON_SUBSTRING)) {
                cluster = CDSConsentExtensionConstants.INDIVIDUAL_CDS_DATA_CLUSTER.get(scope);
            } else if (scope.equals(CDSConsentExtensionConstants.PROFILE_SCOPE)) {
                cluster = CDSConsentExtensionConstants.PROFILE_DATA_CLUSTER.get(scope);
            } else {
                cluster = CDSConsentExtensionConstants.CDS_DATA_CLUSTER.get(scope);
            }
            if (cluster == null) {
                log.warn(String.format("No data found for scope: %s requested.", scope));
                continue;
            }
            for (Map.Entry<String, List<String>> entry : cluster.entrySet()) {
                dataClusterItem.put(CDSConsentExtensionConstants.TITLE, entry.getKey());
                JSONArray requestedData = new JSONArray();
                requestedData.addAll(entry.getValue());
                dataClusterItem.put(CDSConsentExtensionConstants.DATA, requestedData);
            }
            dataCluster.add(dataClusterItem);
        }
        return dataCluster;
    }

    /**
     * Get profile data clusters for the given scopes.
     *
     * @param dataCluster            previous data cluster
     * @param newDataCluster         new data cluster
     * @param businessDataCluster    previous business data cluster
     * @param newBusinessDataCluster new business data cluster
     * @param previousScopes         previous scopes array
     * @param newScopes              new scopes array
     * @return modified data clusters with profile data
     */
    private static JSONObject processProfileDataClusters(JSONArray dataCluster, JSONArray newDataCluster,
                                                         JSONArray businessDataCluster,
                                                         JSONArray newBusinessDataCluster, JSONArray previousScopes,
                                                         JSONArray newScopes) {

        ArrayList<String> newlyRequestedProfileClaims = new ArrayList<>();
        ArrayList<String> previouslyRequestedProfileClaims = new ArrayList<>();
        ArrayList<String> nameClaims = new ArrayList<>();
        ArrayList<String> contactClaims = new ArrayList<>();

        // Processing name cluster related scopes and claims.
        for (String claim : CDSConsentExtensionConstants.NAME_CLUSTER_PERMISSIONS) {
            if (newScopes.contains(PermissionsEnum.fromValue(claim))) {
                newlyRequestedProfileClaims.add(CDSConsentExtensionConstants.NAME_CLUSTER);
                // Remove previous name data clusters if exists.
                previouslyRequestedProfileClaims.remove(CDSConsentExtensionConstants.NAME_CLUSTER);
                break;
            } else if (!previouslyRequestedProfileClaims.contains(CDSConsentExtensionConstants.NAME_CLUSTER) &&
                    previousScopes.contains(PermissionsEnum.fromValue(claim))) {
                previouslyRequestedProfileClaims.add(CDSConsentExtensionConstants.NAME_CLUSTER);
            }
        }
        //Add all requested claims related to name cluster for consent amendment UI display
        for (String claim : CDSConsentExtensionConstants.NAME_CLUSTER_PERMISSIONS) {
            // Add all requested claims related to name cluster for consent amendment UI display
            if (previousScopes.contains(PermissionsEnum.fromValue(claim)) ||
                    newScopes.contains(PermissionsEnum.fromValue(claim))) {
                nameClaims.add(claim);
            }
        }

        // Processing contact cluster related claims.
        ArrayList<String> newContactPermissions = new ArrayList<>();
        ArrayList<String> previousContactPermissions = new ArrayList<>();
        for (Map.Entry<String, List<String>> cluster : CDSConsentExtensionConstants.CONTACT_CLUSTER_CLAIMS.entrySet()) {
            String clusterName = cluster.getKey();
            List<String> clusterClaims = cluster.getValue();
            for (String claim : clusterClaims) {
                if (newScopes.contains(PermissionsEnum.fromValue(claim))) {
                    newContactPermissions.add(clusterName);
                    break;
                } else if (previousScopes.contains(PermissionsEnum.fromValue(claim))) {
                    previousContactPermissions.add(clusterName);
                }
            }
            // Add all requested claims related to contact cluster for consent amendment UI display
            for (String claim : clusterClaims) {
                if (previousScopes.contains(PermissionsEnum.fromValue(claim)) ||
                        newScopes.contains(PermissionsEnum.fromValue(claim))) {
                    contactClaims.add(claim);
                }
            }
        }

        if (newContactPermissions.size() > 0) {
            newContactPermissions.addAll(previousContactPermissions);
            newlyRequestedProfileClaims.add(processContactClaims(newContactPermissions));
        } else if (previousContactPermissions.size() > 0) {
            previouslyRequestedProfileClaims.add(processContactClaims(previousContactPermissions));
        }

        Map<String, JSONArray> newDataClusters =
                addProfileDataClusters(newlyRequestedProfileClaims, newDataCluster, newBusinessDataCluster);
        Map<String, JSONArray> previousDataClusters =
                addProfileDataClusters(previouslyRequestedProfileClaims, dataCluster, businessDataCluster);

        JSONObject profileDataObject = new JSONObject();
        profileDataObject.put(CDSConsentExtensionConstants.DATA_CLUSTER,
                previousDataClusters.get(CDSConsentExtensionConstants.DATA_CLUSTER));
        profileDataObject.put(CDSConsentExtensionConstants.NEW_DATA_CLUSTER,
                newDataClusters.get(CDSConsentExtensionConstants.DATA_CLUSTER));
        profileDataObject.put(CDSConsentExtensionConstants.BUSINESS_DATA_CLUSTER,
                previousDataClusters.get(CDSConsentExtensionConstants.BUSINESS_DATA_CLUSTER));
        profileDataObject.put(CDSConsentExtensionConstants.NEW_BUSINESS_DATA_CLUSTER,
                newDataClusters.get(CDSConsentExtensionConstants.BUSINESS_DATA_CLUSTER));
        profileDataObject.put(CDSConsentExtensionConstants.NAME_CLAIMS, String.join(", ", nameClaims));
        profileDataObject.put(CDSConsentExtensionConstants.CONTACT_CLAIMS, String.join(", ", contactClaims));

        return profileDataObject;
    }

    /**
     * Process contact claims and build the contact cluster name.
     *
     * @param contactPermissions contact permissions
     * @return contact cluster name
     */
    private static String processContactClaims(ArrayList<String> contactPermissions) {

        HashSet<String> contactPermissionSet = new HashSet<>(contactPermissions);
        Object[] permissionsArray = contactPermissionSet.toArray();
        Arrays.sort(permissionsArray);
        StringBuilder clusterName = new StringBuilder(CDSConsentExtensionConstants.CONTACT_CLUSTER);

        for (Object permission : permissionsArray) {
            clusterName.append("_");
            clusterName.append(permission.toString());
        }
        return String.valueOf(clusterName);
    }

    /**
     * Add profile scope and other standard claims related data clusters for the provided claims.
     *
     * @param profileClaims         profile claims
     * @param individualDataCluster individual customer data cluster
     * @param businessDataCluster   business customer data cluster
     * @return map of individual and business data clusters
     */
    private static Map<String, JSONArray> addProfileDataClusters(ArrayList<String> profileClaims,
                                                                 JSONArray individualDataCluster,
                                                                 JSONArray businessDataCluster) {

        for (String claimCluster : profileClaims) {
            Map<String, List<String>> cluster =
                    CDSConsentExtensionConstants.PROFILE_DATA_CLUSTER.get(claimCluster);
            if (cluster == null) {
                log.warn(String.format("No data found for profile scope claim: %s requested.", claimCluster));
            } else {
                JSONObject dataClusterItem = new JSONObject();
                for (Map.Entry<String, List<String>> entry : cluster.entrySet()) {
                    dataClusterItem.put(CDSConsentExtensionConstants.TITLE, entry.getKey());
                    JSONArray requestedData = new JSONArray();
                    requestedData.addAll(entry.getValue());
                    dataClusterItem.put(CDSConsentExtensionConstants.DATA, requestedData);
                }

                individualDataCluster.add(dataClusterItem);
                businessDataCluster.add(dataClusterItem);
            }
        }
        Map<String, JSONArray> dataClusters = new HashMap<>();
        dataClusters.put(CDSConsentExtensionConstants.DATA_CLUSTER, individualDataCluster);
        dataClusters.put(CDSConsentExtensionConstants.BUSINESS_DATA_CLUSTER, businessDataCluster);

        return dataClusters;
    }
}

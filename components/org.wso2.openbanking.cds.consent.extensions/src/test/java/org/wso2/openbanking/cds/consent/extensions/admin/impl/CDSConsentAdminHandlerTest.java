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
package org.wso2.openbanking.cds.consent.extensions.admin.impl;

import com.wso2.openbanking.accelerator.account.metadata.service.service.AccountMetadataServiceImpl;
import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.consent.extensions.admin.model.ConsentAdminData;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentHistoryResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.openbanking.cds.consent.extensions.common.CDSConsentExtensionConstants;
import org.wso2.openbanking.cds.consent.extensions.model.DataClusterSharingDateModel;
import org.wso2.openbanking.cds.consent.extensions.util.CDSConsentValidateTestConstants;
import org.wso2.openbanking.cds.consent.extensions.util.DataClusterSharingDateUtil;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.testng.AssertJUnit.assertEquals;
import static org.wso2.openbanking.cds.consent.extensions.common.CDSConsentExtensionConstants.AUTH_RESOURCE_TYPE_LINKED;
import static org.wso2.openbanking.cds.consent.extensions.common.CDSConsentExtensionConstants.AUTH_RESOURCE_TYPE_PRIMARY;

/**
 * Test class for CDSConsentAdminHandler.
 */
@PrepareForTest({OpenBankingConfigParser.class, AccountMetadataServiceImpl.class, ConsentAdminData.class,
        DataClusterSharingDateUtil.class, JSONObject.class, JSONArray.class})
@PowerMockIgnore({"com.wso2.openbanking.accelerator.consent.extensions.common.*", "jdk.internal.reflect.*"})
public class CDSConsentAdminHandlerTest extends PowerMockTestCase {

    public static final String USER_ID_SECONDARY = "test-secondary-user-id";
    public static final String USER_ID_PRIMARY = "test-primary-user-id";
    public static final String AUTH_ID_PRIMARY = "test-primary-auth-id";
    public static final String AUTH_ID_SECONDARY = "test-secondary-auth-id";
    public static final String JOINT_ACCOUNT_ID = "test-joint-account-id";
    public static final String MAPPING_ID_1 = "test-mapping-id-1";
    public static final String MAPPING_ID_2 = "test-mapping-id-2";
    public static final String MAPPING_ID_3 = "test-mapping-id-3";

    private CDSConsentAdminHandler uut;
    private ConsentCoreServiceImpl consentCoreServiceMock;
    private DetailedConsentResource detailedConsentResource;
    @Mock
    private AccountMetadataServiceImpl accountMetadataServiceMock;

    @BeforeClass
    public void setUp() throws ConsentManagementException {
        //mock
        consentCoreServiceMock = mock(ConsentCoreServiceImpl.class);

        //when
        AuthorizationResource authResource1 = new AuthorizationResource();
        authResource1.setAuthorizationID(AUTH_ID_PRIMARY);
        authResource1.setUserID(USER_ID_PRIMARY);
        authResource1.setAuthorizationType(AUTH_RESOURCE_TYPE_PRIMARY);

        AuthorizationResource authResource2 = new AuthorizationResource();
        authResource2.setAuthorizationID(AUTH_ID_SECONDARY);
        authResource2.setUserID(USER_ID_SECONDARY);
        authResource2.setAuthorizationType(AUTH_RESOURCE_TYPE_LINKED);

        ConsentMappingResource mapping1 = new ConsentMappingResource();
        mapping1.setMappingID(MAPPING_ID_1);
        mapping1.setAccountID(JOINT_ACCOUNT_ID);
        mapping1.setAuthorizationID(AUTH_ID_PRIMARY);
        mapping1.setMappingStatus("active");

        ConsentMappingResource mapping2 = new ConsentMappingResource();
        mapping2.setMappingID(MAPPING_ID_2);
        mapping2.setAccountID("test-regular-account-id");
        mapping2.setAuthorizationID(AUTH_ID_PRIMARY);
        mapping2.setMappingStatus("active");

        ConsentMappingResource mapping3 = new ConsentMappingResource();
        mapping3.setMappingID(MAPPING_ID_3);
        mapping3.setAccountID(JOINT_ACCOUNT_ID);
        mapping3.setAuthorizationID(AUTH_ID_SECONDARY);
        mapping3.setMappingStatus("active");

        detailedConsentResource = new DetailedConsentResource();
        detailedConsentResource.setAuthorizationResources(new ArrayList<>(Arrays.asList(authResource1, authResource2)));
        detailedConsentResource
                .setConsentMappingResources(new ArrayList<>(Arrays.asList(mapping1, mapping2, mapping3)));

        doReturn(true).when(consentCoreServiceMock).deactivateAccountMappings(any(ArrayList.class));
        doReturn(detailedConsentResource).when(consentCoreServiceMock).getDetailedConsent(anyString());
        doReturn(true).when(consentCoreServiceMock)
                .revokeConsentWithReason(anyString(), anyString(), anyString(), anyString());
        doReturn(true).when(consentCoreServiceMock)
                .storeConsentAmendmentHistory(anyString(), anyObject(), anyObject());

        this.uut = new CDSConsentAdminHandler(consentCoreServiceMock, null);
    }

    @Test
    public void testHandleRevoke() throws ConsentManagementException {

        Map<String, Object> configs = new HashMap<>();
        configs.put("Consent.AmendmentHistory.Enabled", "false");

        mockStatic(OpenBankingConfigParser.class);
        OpenBankingConfigParser openBankingConfigParserMock = PowerMockito.mock(OpenBankingConfigParser.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParserMock);
        PowerMockito.when(openBankingConfigParserMock.getConfiguration()).thenReturn(configs);

        Map<String, List<String>> queryParams = new HashMap<>();
        queryParams.put(CDSConsentAdminHandler.CONSENT_ID,
                new ArrayList<>(Collections.singletonList("test-consent-id")));
        queryParams.put(CDSConsentAdminHandler.USER_ID, new ArrayList<>(Collections.singletonList(USER_ID_SECONDARY)));

        ConsentAdminData consentAdminDataMock = mock(ConsentAdminData.class);
        when(consentAdminDataMock.getQueryParams()).thenReturn(queryParams);

        this.accountMetadataServiceMock = mock(AccountMetadataServiceImpl.class);
        mockStatic(AccountMetadataServiceImpl.class);
        when(AccountMetadataServiceImpl.getInstance()).thenReturn(accountMetadataServiceMock);

        uut.handleRevoke(consentAdminDataMock);

        ArgumentCaptor<ArrayList> argumentCaptor = ArgumentCaptor.forClass(ArrayList.class);
        verify(consentCoreServiceMock).deactivateAccountMappings(argumentCaptor.capture());
        ArrayList capturedArgument = argumentCaptor.getValue();

        assertTrue(capturedArgument.contains(MAPPING_ID_1));
        assertTrue(capturedArgument.contains(MAPPING_ID_3));
        verify(consentAdminDataMock).setResponseStatus(ResponseStatus.NO_CONTENT);
    }

    @Test
    public void testHandleRevokeForPrimaryUser() {
        Map<String, List<String>> queryParams = new HashMap<>();
        queryParams.put(CDSConsentAdminHandler.CONSENT_ID,
                new ArrayList<>(Collections.singletonList("test-consent-id")));
        queryParams.put(CDSConsentAdminHandler.USER_ID, new ArrayList<>(Collections.singletonList(USER_ID_PRIMARY)));

        ConsentAdminData consentAdminDataMock = mock(ConsentAdminData.class);
        when(consentAdminDataMock.getQueryParams()).thenReturn(queryParams);

        this.accountMetadataServiceMock = mock(AccountMetadataServiceImpl.class);
        mockStatic(AccountMetadataServiceImpl.class);
        when(AccountMetadataServiceImpl.getInstance()).thenReturn(accountMetadataServiceMock);

        uut.handleRevoke(consentAdminDataMock);

        verify(consentAdminDataMock).setResponseStatus(ResponseStatus.NO_CONTENT);
    }

    @Test(description = "if consent id is missing in query params, should throw ConsentException",
            expectedExceptions = ConsentException.class)
    public void testHandleRevokeForConsentException() {
        ConsentAdminData consentAdminDataMock = mock(ConsentAdminData.class);
        when(consentAdminDataMock.getQueryParams()).thenReturn(Collections.EMPTY_MAP);

        uut.handleRevoke(consentAdminDataMock);
    }

    @Test
    public void testRetrieveConsentAmendmentHistory() throws ConsentManagementException {

        detailedConsentResource.setReceipt(CDSConsentValidateTestConstants.VALID_RECEIPT);
        detailedConsentResource.setConsentAttributes(CDSConsentValidateTestConstants.SAMPLE_CONSENT_ATTRIBUTES_MAP);
        detailedConsentResource.setCurrentStatus("revoked");

        HashMap<String, ConsentHistoryResource> mockResults = new LinkedHashMap<>();

        ConsentHistoryResource consentHistoryResource = new ConsentHistoryResource();
        consentHistoryResource.setReason("sample-reason");
        consentHistoryResource.setDetailedConsentResource(detailedConsentResource);
        mockResults.put("sample-history-id", consentHistoryResource);

        doReturn(mockResults).when(consentCoreServiceMock)
                .getConsentAmendmentHistoryData(anyString());
        doReturn(detailedConsentResource).when(consentCoreServiceMock)
                .getDetailedConsent(anyString());

        Map<String, List<String>> queryParams = new HashMap<>();
        queryParams.put(CDSConsentAdminHandler.CDR_ARRANGEMENT_ID,
                new ArrayList<>(Collections.singletonList("test-id")));
        queryParams.put(CDSConsentAdminHandler.USER_ID,
                new ArrayList<>(Collections.singletonList(USER_ID_PRIMARY)));

        ConsentAdminData consentAdminDataMock = mock(ConsentAdminData.class);
        when(consentAdminDataMock.getQueryParams()).thenReturn(queryParams);

        uut.handleConsentAmendmentHistoryRetrieval(consentAdminDataMock);

        verify(consentAdminDataMock).setResponseStatus(ResponseStatus.OK);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testRetrieveConsentAmendmentHistoryWithoutCdrArrangementIdQueryParam() {

        Map<String, List<String>> queryParams = new HashMap<>();
        ConsentAdminData consentAdminDataMock = mock(ConsentAdminData.class);
        when(consentAdminDataMock.getQueryParams()).thenReturn(queryParams);

        uut.handleConsentAmendmentHistoryRetrieval(consentAdminDataMock);
    }

    @Test
    public void testUpdateDOMSStatusForConsentData() throws OpenBankingException {

        this.accountMetadataServiceMock = mock(AccountMetadataServiceImpl.class);
        mockStatic(AccountMetadataServiceImpl.class);
        when(AccountMetadataServiceImpl.getInstance()).thenReturn(accountMetadataServiceMock);

        Map<String, String> accountMetadataMap = new HashMap<>();
        accountMetadataMap.put("6500001232", CDSConsentExtensionConstants.DOMS_STATUS_PRE_APPROVAL);

        JSONObject payload = detailedConsentToJSON(detailedConsentResource);
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = Mockito.mock(HttpServletResponse.class);
        ConsentAdminData consentAdminData = new ConsentAdminData(new HashMap<>(), payload, new HashMap<>(),
                "", httpServletRequest, httpServletResponse);
        consentAdminData.setResponsePayload(payload);

        String accountId = "6500001232";
        Map<String, String> disclosureOptionsMap = new HashMap<>();
        if (accountMetadataMap.containsKey(accountId)) {
            String domsStatus = accountMetadataMap.get(accountId);
            disclosureOptionsMap.put(CDSConsentExtensionConstants.DOMS_STATUS, domsStatus);
        }
        when(accountMetadataServiceMock.getAccountMetadataMap(anyString()))
                .thenReturn(disclosureOptionsMap);

        uut.updateDOMSStatusForConsentData(consentAdminData);

        String domsStatus = disclosureOptionsMap.get(CDSConsentExtensionConstants.DOMS_STATUS);
        assertEquals(CDSConsentExtensionConstants.DOMS_STATUS_PRE_APPROVAL, domsStatus);
    }

    @Test
    public void testAddSharingDatesToPermissions() throws OpenBankingException {

        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = Mockito.mock(HttpServletResponse.class);
        JSONObject payload = detailedConsentToJSON(detailedConsentResource);

        ConsentAdminData consentAdminData = new ConsentAdminData(new HashMap<>(), payload, new HashMap<>(),
                "", httpServletRequest, httpServletResponse);
        consentAdminData.setResponsePayload(payload);

        Map<String, DataClusterSharingDateModel> sharingDateDataMap = new HashMap<>();
        DataClusterSharingDateModel dataClusterSharingDate = new DataClusterSharingDateModel();
        dataClusterSharingDate.setDataCluster("CDRREADACCOUNTSBASIC");
        dataClusterSharingDate.setSharingStartDate(Timestamp.from(Instant.now()));
        dataClusterSharingDate.setLastSharedDate(Timestamp.from(Instant.now()));
        sharingDateDataMap.put("bank_account_data", dataClusterSharingDate);

        PowerMockito.mockStatic(DataClusterSharingDateUtil.class);
        String consentId = "test_consent_id";
        when(DataClusterSharingDateUtil.getSharingDateMap(consentId)).thenReturn(sharingDateDataMap);

        uut.addSharingDatesToPermissions(consentAdminData);

        // Assert that the sharing dates are added to the response payload
        JSONArray responsePayload = (JSONArray) consentAdminData.getResponsePayload()
                .get(CDSConsentExtensionConstants.DATA);
        for (Object item : responsePayload) {
            JSONObject itemJSONObject = (JSONObject) item;
            JSONObject receipt = (JSONObject) itemJSONObject.get(CDSConsentExtensionConstants.RECEIPT);
            JSONObject accountData = (JSONObject) receipt.get(CDSConsentExtensionConstants.ACCOUNT_DATA);
            JSONArray permissionsWithSharingDate = (JSONArray)
                    accountData.get(CDSConsentExtensionConstants.PERMISSIONS_WITH_SHARING_DATE);

            assertNotNull(permissionsWithSharingDate);
        }
    }

    public JSONObject detailedConsentToJSON(@NotNull DetailedConsentResource detailedConsentResource) {
        JSONObject dataObject = new JSONObject();

        JSONObject jsonObject = new JSONObject();
        JSONArray authorizationResourcesArray = new JSONArray();
        JSONArray consentMappingResourcesArray = new JSONArray();
        JSONArray permissions = new JSONArray();
        JSONObject accountData = new JSONObject();
        JSONObject receipt = new JSONObject();

        for (AuthorizationResource authorizationResource : detailedConsentResource.getAuthorizationResources()) {
            JSONObject authResourceJsonObject = new JSONObject();
            authResourceJsonObject.put("authorizationId", authorizationResource.getAuthorizationID());
            authResourceJsonObject.put("consentId", authorizationResource.getConsentID());
            authResourceJsonObject.put("userId", authorizationResource.getUserID());
            authResourceJsonObject.put("authorizationStatus", authorizationResource.getAuthorizationStatus());
            authResourceJsonObject.put("authorizationType", authorizationResource.getAuthorizationType());
            authResourceJsonObject.put("updatedTime", authorizationResource.getUpdatedTime());
            authorizationResourcesArray.add(authResourceJsonObject);
        }

        for (ConsentMappingResource consentMappingResource : detailedConsentResource.getConsentMappingResources()) {
            JSONObject consentMappingResourceObject = new JSONObject();
            consentMappingResourceObject.put("mappingId", consentMappingResource.getMappingID());
            consentMappingResourceObject.put("authorizationId", consentMappingResource.getAuthorizationID());
            consentMappingResourceObject.put("accountId", consentMappingResource.getAccountID());
            consentMappingResourceObject.put("permission", consentMappingResource.getPermission());
            consentMappingResourceObject.put("mappingStatus", consentMappingResource.getMappingStatus());
            consentMappingResourcesArray.add(consentMappingResourceObject);
        }

        permissions.add("CDRREADACCOUNTSBASIC");
        permissions.add("CDRREADACCOUNTSDETAILS");
        permissions.add("CDRREADPAYEES");
        permissions.add("CDRREADTRANSACTION");
        permissions.add("READCUSTOMERDETAILS");
        permissions.add("READCUSTOMERDETAILSBASIC");
        permissions.add("PROFILE");

        accountData.put("permissions", permissions);
        receipt.put("accountData", accountData);
        jsonObject.put("authorizationResources", authorizationResourcesArray);
        jsonObject.put("consentMappingResources", consentMappingResourcesArray);
        jsonObject.put("consentId", "test-consent-id");
        jsonObject.put("receipt", receipt);

        JSONArray dataArray = new JSONArray();
        dataArray.add(jsonObject);

        dataObject.put("data", dataArray);

        return dataObject;
    }
}

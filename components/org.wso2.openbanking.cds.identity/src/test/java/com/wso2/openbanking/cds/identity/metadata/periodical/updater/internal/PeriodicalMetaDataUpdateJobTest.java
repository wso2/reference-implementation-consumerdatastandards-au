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
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.openbanking.cds.common.metadata.periodical.updater.constants.MetadataConstants;
import org.wso2.openbanking.cds.common.metadata.periodical.updater.utils.DataRecipientStatusEnum;
import org.wso2.openbanking.cds.common.metadata.periodical.updater.utils.SoftwareProductStatusEnum;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.wso2.openbanking.cds.common.metadata.periodical.updater.constants.MetadataConstants.DATA;
import static org.wso2.openbanking.cds.common.metadata.periodical.updater.constants.MetadataConstants.MAP_DATA_RECIPIENTS;
import static org.wso2.openbanking.cds.common.metadata.periodical.updater.constants.MetadataConstants.MAP_SOFTWARE_PRODUCTS;
import static org.wso2.openbanking.cds.common.metadata.periodical.updater.constants.MetadataConstants.STATUS;
import static org.wso2.openbanking.cds.common.metadata.periodical.updater.constants.MetadataConstants.STATUS_JSON_LEGAL_ENTITY_KEY;
import static org.wso2.openbanking.cds.common.metadata.periodical.updater.constants.MetadataConstants.STATUS_JSON_SP_KEY;

/**
 * Test class for Periodical MetaData Update Job.
 */
@PrepareForTest({ServiceHolder.class})
@PowerMockIgnore("jdk.internal.reflect.*")
public class PeriodicalMetaDataUpdateJobTest extends PowerMockTestCase {

    private static final String DUMMY_SOFTWARE_PRODUCT_ID_1 = "af9f578f-3d96-ea11-a831-000d3a8842e1";
    private static final String DUMMY_SOFTWARE_PRODUCT_ID_2 = "12316470-f7ae-eb11-a822-000d3a884a20";
    private static final String DUMMY_LEGAL_ENTITY_ID_1 = "379f578f-3d96-ea11-a831-000d3a8842e1";
    private static final String DUMMY_LEGAL_ENTITY_ID_2 = "b850b3ab-4096-ea11-a831-000d3a8842e1";

    private JSONObject dataRecipientsWrapper = new JSONObject();
    private JSONObject softwareProductsWrapper = new JSONObject();
    private PeriodicalMetaDataUpdateJob uut;

    @BeforeClass
    public void init() {

        this.uut = new PeriodicalMetaDataUpdateJob();


        JSONArray dataRecipients = new JSONArray();
        JSONArray softwareProducts = new JSONArray();
        // creating dummy data responses
        JSONObject dataRecipient1 = getDataRecipient(DUMMY_LEGAL_ENTITY_ID_1, DataRecipientStatusEnum.ACTIVE);
        JSONObject dataRecipient2 = getDataRecipient(DUMMY_LEGAL_ENTITY_ID_2, DataRecipientStatusEnum.SUSPENDED);
        dataRecipients.put(dataRecipient1);
        dataRecipients.put(dataRecipient2);

        JSONObject softwareProduct1 = getSoftwareProduct(DUMMY_SOFTWARE_PRODUCT_ID_1, SoftwareProductStatusEnum.ACTIVE);
        JSONObject softwareProduct2 = getSoftwareProduct(DUMMY_SOFTWARE_PRODUCT_ID_2,
                SoftwareProductStatusEnum.INACTIVE);
        softwareProducts.put(softwareProduct1);
        softwareProducts.put(softwareProduct2);

        dataRecipientsWrapper.put(DATA, dataRecipients);
        softwareProductsWrapper.put(DATA, softwareProducts);

    }

    @Test(description = "when valid data recipient response json provided, should return data recipient maps")
    public void testGetDataRecipientStatusesFromRegister() throws OpenBankingException {

        Map<String, String> statuses = uut.getDataRecipientStatusesFromRegister(this.dataRecipientsWrapper);
        Assert.assertFalse(statuses.isEmpty());
    }

    @Test(description = "when valid software product response json provided, should return software product maps")
    public void testGetSoftwareProductStatusesFromRegister() throws OpenBankingException {

        Map<String, String> statuses = uut.getSoftwareProductStatusesFromRegister(this.softwareProductsWrapper);
        Assert.assertFalse(statuses.isEmpty());
    }

    @Test(description = "when valid data provided, should return modified maps")
    public void testProcessMetaDataStatus()
            throws IdentityApplicationManagementException, UserStoreException, OpenBankingException {

        //mock
        ServiceProviderProperty spProperty1 = new ServiceProviderProperty();
        spProperty1.setDisplayName(MetadataConstants.LEGAL_ENTITY_ID);
        spProperty1.setValue(DUMMY_LEGAL_ENTITY_ID_1);

        ServiceProviderProperty spProperty2 = new ServiceProviderProperty();
        spProperty2.setDisplayName(MetadataConstants.SOFTWARE_ID);
        spProperty2.setValue(DUMMY_SOFTWARE_PRODUCT_ID_1);

        InboundAuthenticationRequestConfig config = new InboundAuthenticationRequestConfig();
        config.setInboundAuthKey("apim-store-client-id-1");

        InboundAuthenticationConfig inboundAuthenticationConfig = new InboundAuthenticationConfig();
        inboundAuthenticationConfig
                .setInboundAuthenticationRequestConfigs(new InboundAuthenticationRequestConfig[]{config});

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setSpProperties(new ServiceProviderProperty[]{spProperty1, spProperty2});
        serviceProvider.setInboundAuthenticationConfig(inboundAuthenticationConfig);

        IdentityCommonHelper identityCommonHelperMock = Mockito.mock(IdentityCommonHelper.class);
        Mockito.when(identityCommonHelperMock.getAllServiceProviders())
                .thenReturn(Collections.singletonList(serviceProvider));

        ServiceHolder serviceHolderMock = Mockito.mock(ServiceHolder.class);
        Mockito.when(serviceHolderMock.getIdentityCommonHelper()).thenReturn(identityCommonHelperMock);

        PowerMockito.mockStatic(ServiceHolder.class);
        PowerMockito.when(ServiceHolder.getInstance()).thenReturn(serviceHolderMock);

        //assert
        Map<String, String> dataRecipientsMap = new HashMap<>();
        dataRecipientsMap.put(DUMMY_LEGAL_ENTITY_ID_1, DataRecipientStatusEnum.ACTIVE.toString());

        Map<String, String> softwareProductsMap = new HashMap<>();
        softwareProductsMap.put(DUMMY_SOFTWARE_PRODUCT_ID_1, SoftwareProductStatusEnum.ACTIVE.toString());

        Map<String, Map<String, String>> modifiedMaps = uut.processMetadataStatus(dataRecipientsMap,
                softwareProductsMap);

        Assert.assertFalse(modifiedMaps.get(MAP_DATA_RECIPIENTS).isEmpty());
        Assert.assertFalse(modifiedMaps.get(MAP_SOFTWARE_PRODUCTS).isEmpty());

    }

    private JSONObject getSoftwareProduct(String softwareProductId, SoftwareProductStatusEnum status) {

        JSONObject softwareProduct = new JSONObject();
        softwareProduct.put(STATUS_JSON_SP_KEY, softwareProductId);
        softwareProduct.put(STATUS, status.toString());

        return softwareProduct;
    }


    private JSONObject getDataRecipient(String legalEntityId, DataRecipientStatusEnum status) {

        JSONObject dataRecipient = new JSONObject();
        dataRecipient.put(STATUS_JSON_LEGAL_ENTITY_KEY, legalEntityId);
        dataRecipient.put(STATUS, status.toString());
        return dataRecipient;
    }
}

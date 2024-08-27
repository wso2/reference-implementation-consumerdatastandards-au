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
package org.wso2.openbanking.cds.common.metadata.status.validator.service;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.openbanking.cds.common.metadata.periodical.updater.constants.MetadataConstants;
import org.wso2.openbanking.cds.common.metadata.status.validator.cache.MetadataCache;
import org.wso2.openbanking.cds.common.metadata.status.validator.cache.MetadataCacheKey;

import java.util.HashMap;

/**
 * Test class for MetadataService.
 */
@PrepareForTest({MetadataCache.class})
@PowerMockIgnore("jdk.internal.reflect.*")
public class MetadataServiceTest extends PowerMockTestCase {
    private static final String CLIENT_ID_ACTIVE_ACTIVE = "client-id-active-active";
    private static final String CLIENT_ID_ACTIVE_INACTIVE = "client-id-active-inactive";
    private static final String CLIENT_ID_ACTIVE_REMOVED = "client-id-active-removed";
    private static final String CLIENT_ID_SUSPENDED_ACTIVE = "client-id-suspended-active";
    private static final String CLIENT_ID_SUSPENDED_INACTIVE = "client-id-suspended-inactive";
    private static final String CLIENT_ID_SUSPENDED_REMOVED = "client-id-suspended-removed";

    @BeforeMethod
    public void init() throws OpenBankingException {
        HashMap<String, String> adrStatusMap = new HashMap<>();
        adrStatusMap.put(CLIENT_ID_ACTIVE_ACTIVE, "ACTIVE");
        adrStatusMap.put(CLIENT_ID_ACTIVE_INACTIVE, "ACTIVE");
        adrStatusMap.put(CLIENT_ID_ACTIVE_REMOVED, "ACTIVE");
        adrStatusMap.put(CLIENT_ID_SUSPENDED_ACTIVE, "SUSPENDED");
        adrStatusMap.put(CLIENT_ID_SUSPENDED_INACTIVE, "SUSPENDED");
        adrStatusMap.put(CLIENT_ID_SUSPENDED_REMOVED, "SUSPENDED");

        HashMap<String, String> spStatusMap = new HashMap<>();
        spStatusMap.put(CLIENT_ID_ACTIVE_ACTIVE, "ACTIVE");
        spStatusMap.put(CLIENT_ID_ACTIVE_INACTIVE, "INACTIVE");
        spStatusMap.put(CLIENT_ID_ACTIVE_REMOVED, "REMOVED");
        spStatusMap.put(CLIENT_ID_SUSPENDED_ACTIVE, "ACTIVE");
        spStatusMap.put(CLIENT_ID_SUSPENDED_INACTIVE, "INACTIVE");
        spStatusMap.put(CLIENT_ID_SUSPENDED_REMOVED, "REMOVED");

        MetadataCache metadataCacheMock = Mockito.mock(MetadataCache.class);
        Mockito.when(metadataCacheMock.getFromCacheOrRetrieve(Mockito
                        .eq(MetadataCacheKey.from(MetadataConstants.MAP_DATA_RECIPIENTS)), Mockito.any()))
                .thenReturn(adrStatusMap);
        Mockito.when(metadataCacheMock.getFromCacheOrRetrieve(Mockito
                        .eq(MetadataCacheKey.from(MetadataConstants.MAP_SOFTWARE_PRODUCTS)), Mockito.any()))
                .thenReturn(spStatusMap);

        PowerMockito.mockStatic(MetadataCache.class);
        PowerMockito.when(MetadataCache.getInstance()).thenReturn(metadataCacheMock);
    }

    @Test
    public void testShouldDiscloseCDRData() {
        Assert.assertTrue(MetadataService.shouldDiscloseCDRData(CLIENT_ID_ACTIVE_ACTIVE).isValid());
        Assert.assertFalse(MetadataService.shouldDiscloseCDRData(CLIENT_ID_ACTIVE_INACTIVE).isValid());
        Assert.assertFalse(MetadataService.shouldDiscloseCDRData(CLIENT_ID_SUSPENDED_INACTIVE).isValid());
    }

    @Test
    public void testShouldFacilitateConsentAuthorisation() {
        Assert.assertTrue(MetadataService.shouldFacilitateConsentAuthorisation(CLIENT_ID_ACTIVE_ACTIVE).isValid());
        Assert.assertFalse(MetadataService.shouldFacilitateConsentAuthorisation(CLIENT_ID_ACTIVE_INACTIVE).isValid());
        Assert.assertFalse(MetadataService
                .shouldFacilitateConsentAuthorisation(CLIENT_ID_SUSPENDED_INACTIVE).isValid());
    }

    @Test
    public void testShouldFacilitateConsentWithdrawal() {
        Assert.assertTrue(MetadataService.shouldFacilitateConsentWithdrawal(CLIENT_ID_ACTIVE_ACTIVE).isValid());
        Assert.assertTrue(MetadataService.shouldFacilitateConsentWithdrawal(CLIENT_ID_ACTIVE_INACTIVE).isValid());
        Assert.assertTrue(MetadataService.shouldFacilitateConsentWithdrawal(CLIENT_ID_SUSPENDED_INACTIVE).isValid());
        Assert.assertFalse(MetadataService.shouldFacilitateConsentWithdrawal(CLIENT_ID_SUSPENDED_ACTIVE).isValid());
        Assert.assertFalse(MetadataService.shouldFacilitateConsentWithdrawal(CLIENT_ID_ACTIVE_REMOVED).isValid());
        Assert.assertFalse(MetadataService.shouldFacilitateConsentWithdrawal(CLIENT_ID_SUSPENDED_REMOVED).isValid());
    }
}

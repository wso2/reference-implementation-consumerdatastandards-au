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
package org.wso2.openbanking.cds.common;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingRuntimeException;
import com.wso2.openbanking.accelerator.common.util.CarbonUtils;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * Test class for Config Parser functionality.
 */
public class OpenBankingCDSConfigParserTest {

    String absolutePathForTestResources;

    @BeforeClass
    public void beforeClass() throws ReflectiveOperationException {

        //to execute util class initialization
        new CarbonUtils();
        System.setProperty("some.property", "property.value");
        System.setProperty("carbon.home", ".");
        injectEnvironmentVariable("CARBON_HOME", ".");
        String path = "src/test/resources";
        File file = new File(path);
        absolutePathForTestResources = file.getAbsolutePath();
    }

    //Runtime exception is thrown here because carbon home is not defined properly for an actual carbon product
    @Test(expectedExceptions = OpenBankingRuntimeException.class, priority = 1)
    public void testConfigParserInitiationWithoutPath() {

        OpenBankingCDSConfigParser openBankingCDSConfigParser = OpenBankingCDSConfigParser.getInstance();
    }

    @Test(expectedExceptions = OpenBankingRuntimeException.class, priority = 2)
    public void testRuntimeExceptionInvalidConfigFile() {

        String path = absolutePathForTestResources + "/open-banking-cds-empty.xml";
        OpenBankingCDSConfigParser openBankingCDSConfigParser = OpenBankingCDSConfigParser.getInstance(path);
    }

    @Test(expectedExceptions = OpenBankingRuntimeException.class, priority = 3)
    public void testRuntimeExceptionNonExistentFile() {

        String path = absolutePathForTestResources + "/open-banking.xml" + "/value";
        OpenBankingCDSConfigParser openBankingCDSConfigParser = OpenBankingCDSConfigParser.getInstance(path);

    }

    @Test(priority = 4)
    public void testConfigParserInit() {

        String dummyConfigFile = absolutePathForTestResources + "/open-banking-cds.xml";

        OpenBankingCDSConfigParser openBankingCDSConfigParser = OpenBankingCDSConfigParser.getInstance(dummyConfigFile);
        Map<String, Object> dcrConfigs = openBankingCDSConfigParser.getConfiguration();

        dcrConfigs.get("DCR.EnableURIValidation").equals("false");
    }

    @Test(priority = 5)
    public void testSingleton() {

        String dummyConfigFile = absolutePathForTestResources + "/open-banking-cds.xml";

        OpenBankingCDSConfigParser instance1 = OpenBankingCDSConfigParser.getInstance(dummyConfigFile);
        OpenBankingCDSConfigParser instance2 = OpenBankingCDSConfigParser.getInstance(dummyConfigFile);
        Assert.assertEquals(instance2, instance1);
    }

    @Test(priority = 6)
    public void testCarbonPath() {

        String carbonConfigDirPath = CarbonUtils.getCarbonConfigDirPath();
        System.setProperty("carbon.config.dir.path", carbonConfigDirPath);
        Assert.assertEquals(CarbonUtils.getCarbonConfigDirPath(), carbonConfigDirPath);
    }

    @Test(priority = 7)
    public void testIsMetadataCacheEnabled() {
        String dummyConfigFile = absolutePathForTestResources + "/open-banking-cds.xml";
        OpenBankingCDSConfigParser openBankingCDSConfigParser = OpenBankingCDSConfigParser.getInstance(dummyConfigFile);
        Assert.assertTrue(openBankingCDSConfigParser.isMetadataCacheEnabled());
    }

    @Test(priority = 7)
    public void testGetMetaDataCacheUpdatePeriodInMinutes() {
        String dummyConfigFile = absolutePathForTestResources + "/open-banking-cds.xml";
        OpenBankingCDSConfigParser openBankingCDSConfigParser = OpenBankingCDSConfigParser.getInstance(dummyConfigFile);
        Assert.assertEquals(openBankingCDSConfigParser.getMetaDataCacheUpdatePeriodInMinutes(), 2);
    }

    @Test(priority = 7)
    public void testGetDataRecipientsDiscoveryUrl() {
        String dummyConfigFile = absolutePathForTestResources + "/open-banking-cds.xml";
        OpenBankingCDSConfigParser openBankingCDSConfigParser = OpenBankingCDSConfigParser.getInstance(dummyConfigFile);
        Assert.assertEquals(openBankingCDSConfigParser.getDataRecipientsDiscoveryUrl(), "https://test-discovery/");
    }

    @Test(priority = 7)
    public void testGetDataRecipientStatusUrl() {
        String dummyConfigFile = absolutePathForTestResources + "/open-banking-cds.xml";
        OpenBankingCDSConfigParser openBankingCDSConfigParser = OpenBankingCDSConfigParser.getInstance(dummyConfigFile);
        Assert.assertEquals(openBankingCDSConfigParser.getDataRecipientStatusUrl(), "https://test-dr/");
    }

    @Test(priority = 7)
    public void testGetSoftwareProductStatusUrl() {
        String dummyConfigFile = absolutePathForTestResources + "/open-banking-cds.xml";
        OpenBankingCDSConfigParser openBankingCDSConfigParser = OpenBankingCDSConfigParser.getInstance(dummyConfigFile);
        Assert.assertEquals(openBankingCDSConfigParser.getSoftwareProductStatusUrl(), "https://test-sp/");
    }

    @Test(priority = 7)
    public void testGetDcrInternalUrl() {
        String dummyConfigFile = absolutePathForTestResources + "/open-banking-cds.xml";
        OpenBankingCDSConfigParser openBankingCDSConfigParser = OpenBankingCDSConfigParser.getInstance(dummyConfigFile);
        Assert.assertTrue(StringUtils.isNotBlank(openBankingCDSConfigParser.getDcrInternalUrl()));
    }

    @Test(priority = 7)
    public void testGetApimApplicationsSearchUrl() {
        String dummyConfigFile = absolutePathForTestResources + "/open-banking-cds.xml";
        OpenBankingCDSConfigParser openBankingCDSConfigParser = OpenBankingCDSConfigParser.getInstance(dummyConfigFile);
        Assert.assertTrue(StringUtils.isNotBlank(openBankingCDSConfigParser.getApimApplicationsSearchUrl()));
    }

    @Test(priority = 7)
    public void testGetRetryCount() {
        String dummyConfigFile = absolutePathForTestResources + "/open-banking-cds.xml";
        OpenBankingCDSConfigParser openBankingCDSConfigParser = OpenBankingCDSConfigParser.getInstance(dummyConfigFile);
        Assert.assertEquals(openBankingCDSConfigParser.getRetryCount(), 3);
    }

    @Test(priority = 7)
    public void testGetCacheExpiryInMinutes() {
        String dummyConfigFile = absolutePathForTestResources + "/open-banking-cds.xml";
        OpenBankingCDSConfigParser openBankingCDSConfigParser = OpenBankingCDSConfigParser.getInstance(dummyConfigFile);
        Assert.assertEquals(openBankingCDSConfigParser.getCacheExpiryInMinutes(), 120);
    }

    @Test(priority = 7)
    public void testIsBulkOperation() {
        String dummyConfigFile = absolutePathForTestResources + "/open-banking-cds.xml";
        OpenBankingCDSConfigParser openBankingCDSConfigParser = OpenBankingCDSConfigParser.getInstance(dummyConfigFile);
        Assert.assertFalse(openBankingCDSConfigParser.isBulkOperation());
    }

    @Test(priority = 7)
    public void testGetBulkExecutionHour() {
        String dummyConfigFile = absolutePathForTestResources + "/open-banking-cds.xml";
        OpenBankingCDSConfigParser openBankingCDSConfigParser = OpenBankingCDSConfigParser.getInstance(dummyConfigFile);
        Assert.assertEquals(openBankingCDSConfigParser.getBulkExecutionHour(), 12);
    }

    @Test(priority = 7)
    public void testGetHolderSpecificIdentifier() {
        String dummyConfigFile = absolutePathForTestResources + "/open-banking-cds.xml";
        OpenBankingCDSConfigParser openBankingCDSConfigParser = OpenBankingCDSConfigParser.getInstance(dummyConfigFile);
        Assert.assertEquals(openBankingCDSConfigParser.getHolderSpecificIdentifier(), "HID");
    }

    @Test(priority = 7)
    public void testGetIntrospectFilterValidators() {
        String dummyConfigFile = absolutePathForTestResources + "/open-banking-cds.xml";
        OpenBankingCDSConfigParser openBankingCDSConfigParser = OpenBankingCDSConfigParser.getInstance(dummyConfigFile);
        Assert.assertNotNull(openBankingCDSConfigParser.getIntrospectFilterValidators());
    }

    @Test(priority = 7)
    public void testGetRevokeFilterValidators() {
        String dummyConfigFile = absolutePathForTestResources + "/open-banking-cds.xml";
        OpenBankingCDSConfigParser openBankingCDSConfigParser = OpenBankingCDSConfigParser.getInstance(dummyConfigFile);
        Assert.assertNotNull(openBankingCDSConfigParser.getRevokeFilterValidators());
    }

    @Test(priority = 7)
    public void testGetParFilterValidators() {
        String dummyConfigFile = absolutePathForTestResources + "/open-banking-cds.xml";
        OpenBankingCDSConfigParser openBankingCDSConfigParser = OpenBankingCDSConfigParser.getInstance(dummyConfigFile);
        Assert.assertNotNull(openBankingCDSConfigParser.getParFilterValidators());
    }

    @Test(priority = 8)
    public void testGetJWTAuthEnabled() {
        String dummyConfigFile = absolutePathForTestResources + "/open-banking-cds.xml";
        OpenBankingCDSConfigParser openBankingCDSConfigParser = OpenBankingCDSConfigParser.getInstance(dummyConfigFile);
        Assert.assertTrue(openBankingCDSConfigParser.getJWTAuthEnabled());
    }

    @Test(priority = 8)
    public void testGetJWTIssuer() {
        String dummyConfigFile = absolutePathForTestResources + "/open-banking-cds.xml";
        OpenBankingCDSConfigParser openBankingCDSConfigParser = OpenBankingCDSConfigParser.getInstance(dummyConfigFile);
        Assert.assertEquals(openBankingCDSConfigParser.getJWTAuthIssuer(), "DummyIssuer");
    }

    @Test(priority = 8)
    public void testGetJWTSubject() {
        String dummyConfigFile = absolutePathForTestResources + "/open-banking-cds.xml";
        OpenBankingCDSConfigParser openBankingCDSConfigParser = OpenBankingCDSConfigParser.getInstance(dummyConfigFile);
        Assert.assertEquals(openBankingCDSConfigParser.getJWTAuthSubject(), "DummySubject");
    }

    @Test(priority = 8)
    public void testGetJWTAudience() {
        String dummyConfigFile = absolutePathForTestResources + "/open-banking-cds.xml";
        OpenBankingCDSConfigParser openBankingCDSConfigParser = OpenBankingCDSConfigParser.getInstance(dummyConfigFile);
        Assert.assertEquals(openBankingCDSConfigParser.getJWTAuthAudience(), "DummyAudience");
    }

    @Test(priority = 8)
    public void testGetJWTJWKS() {
        String dummyConfigFile = absolutePathForTestResources + "/open-banking-cds.xml";
        OpenBankingCDSConfigParser openBankingCDSConfigParser = OpenBankingCDSConfigParser.getInstance(dummyConfigFile);
        Assert.assertEquals(openBankingCDSConfigParser.getJWTAuthJWKSUrl(), "DummyJWKS");
    }

    @Test(priority = 8)
    public void testIsSecondaryAccountsEnabled() {
        String dummyConfigFile = absolutePathForTestResources + "/open-banking-cds.xml";
        OpenBankingCDSConfigParser openBankingCDSConfigParser = OpenBankingCDSConfigParser.getInstance(dummyConfigFile);
        Assert.assertTrue(openBankingCDSConfigParser.getSecondaryUserAccountsEnabled());
    }

    @Test(priority = 8)
    public void testIsCeasingSecondaryUserSharingEnabled() {
        String dummyConfigFile = absolutePathForTestResources + "/open-banking-cds.xml";
        OpenBankingCDSConfigParser openBankingCDSConfigParser = OpenBankingCDSConfigParser.getInstance(dummyConfigFile);
        Assert.assertTrue(openBankingCDSConfigParser.isCeasingSecondaryUserSharingEnabled());
    }

    @Test(priority = 8)
    public void testIsBNRPrioritizeSharableAccountsResponseEnabled() {
        String dummyConfigFile = absolutePathForTestResources + "/open-banking-cds.xml";
        OpenBankingCDSConfigParser openBankingCDSConfigParser = OpenBankingCDSConfigParser.getInstance(dummyConfigFile);
        Assert.assertEquals(openBankingCDSConfigParser.isBNRPrioritizeSharableAccountsResponseEnabled(), true);
    }

    @Test(priority = 8)
    public void testIsBNRValidateAccountsOnRetrievalEnabled() {
        String dummyConfigFile = absolutePathForTestResources + "/open-banking-cds.xml";
        OpenBankingCDSConfigParser openBankingCDSConfigParser = OpenBankingCDSConfigParser.getInstance(dummyConfigFile);
        Assert.assertEquals(openBankingCDSConfigParser.isBNRValidateAccountsOnRetrievalEnabled(), true);
    }

    @Test(priority = 8)
    public void testIsBNRConsentRevocationEnabled() {
        String dummyConfigFile = absolutePathForTestResources + "/open-banking-cds.xml";
        OpenBankingCDSConfigParser openBankingCDSConfigParser = OpenBankingCDSConfigParser.getInstance(dummyConfigFile);
        Assert.assertEquals(openBankingCDSConfigParser.isBNRConsentRevocationEnabled(), true);
    }

    @Test(priority = 8)
    public void testIsDOMSEnabled() {
        String dummyConfigFile = absolutePathForTestResources + "/open-banking-cds.xml";
        OpenBankingCDSConfigParser openBankingCDSConfigParser = OpenBankingCDSConfigParser.getInstance(dummyConfigFile);
        Assert.assertTrue(openBankingCDSConfigParser.getDOMSEnabled());
    }

    @Test(priority = 8)
    public void testGetBNRCustomerTypeSelectionMethod() {
        String dummyConfigFile = absolutePathForTestResources + "/open-banking-cds.xml";
        OpenBankingCDSConfigParser openBankingCDSConfigParser = OpenBankingCDSConfigParser.getInstance(dummyConfigFile);
        Assert.assertEquals(openBankingCDSConfigParser.getBNRCustomerTypeSelectionMethod(), "profile_selection");
    }

    private void injectEnvironmentVariable(String key, String value)
            throws ReflectiveOperationException {

        Class<?> processEnvironment = Class.forName("java.lang.ProcessEnvironment");

        Field unmodifiableMapField = getAccessibleField(processEnvironment, "theUnmodifiableEnvironment");
        Object unmodifiableMap = unmodifiableMapField.get(null);
        injectIntoUnmodifiableMap(key, value, unmodifiableMap);

        Field mapField = getAccessibleField(processEnvironment, "theEnvironment");
        Map<String, String> map = (Map<String, String>) mapField.get(null);
        map.put(key, value);
    }

    private Field getAccessibleField(Class<?> clazz, String fieldName)
            throws NoSuchFieldException {

        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }

    private void injectIntoUnmodifiableMap(String key, String value, Object map)
            throws ReflectiveOperationException {

        Class unmodifiableMap = Class.forName("java.util.Collections$UnmodifiableMap");
        Field field = getAccessibleField(unmodifiableMap, "m");
        Object obj = field.get(map);
        ((Map<String, String>) obj).put(key, value);
    }
}

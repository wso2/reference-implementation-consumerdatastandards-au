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

package org.wso2.openbanking.cds.consent.extensions.util;

import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Constant class for consent validate tests.
 */
public class CDSConsentValidateTestConstants {

    public static final String ACCOUNT_PATH = "/banking/accounts";
    public static final String INVALID_ACCOUNT_PATH = "/account";

    public static DetailedConsentResource getDetailedConsentResource(String receipt, String accountId) {
        DetailedConsentResource detailedConsentResource = new DetailedConsentResource();
        detailedConsentResource.setConsentID("1234");
        detailedConsentResource.setCurrentStatus("awaitingAuthorization");
        detailedConsentResource.setCreatedTime(System.currentTimeMillis() / 1000);
        detailedConsentResource.setUpdatedTime(System.currentTimeMillis() / 1000);
        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        authorizationResources.add(CDSConsentValidateTestConstants.getAuthorizationResource());
        detailedConsentResource.setAuthorizationResources(authorizationResources);
        detailedConsentResource.setReceipt(receipt);
        ArrayList<ConsentMappingResource> consentMappingResources = new ArrayList<>();
        consentMappingResources.add(CDSConsentValidateTestConstants.getConsentMappingResource(accountId));
        detailedConsentResource.setConsentMappingResources(consentMappingResources);

        return detailedConsentResource;
    }

    public static DetailedConsentResource getDetailedConsentResourceForDOMS(String receipt, String accountId) {
        DetailedConsentResource detailedConsentResource = new DetailedConsentResource();
        detailedConsentResource.setConsentID("1234");
        detailedConsentResource.setCurrentStatus("awaitingAuthorization");
        detailedConsentResource.setCreatedTime(System.currentTimeMillis() / 1000);
        detailedConsentResource.setUpdatedTime(System.currentTimeMillis() / 1000);
        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        authorizationResources.add(CDSConsentValidateTestConstants.getAuthorizationResourceForDOMS());
        detailedConsentResource.setAuthorizationResources(authorizationResources);
        detailedConsentResource.setReceipt(receipt);
        ArrayList<ConsentMappingResource> consentMappingResources = new ArrayList<>();
        consentMappingResources.add(CDSConsentValidateTestConstants.getConsentMappingResourceForDOMS(accountId));
        detailedConsentResource.setConsentMappingResources(consentMappingResources);

        return detailedConsentResource;
    }

    public static AuthorizationResource getAuthorizationResource() {
        AuthorizationResource authorizationResource = new AuthorizationResource();
        authorizationResource.setAuthorizationID("DummyAuthId");
        authorizationResource.setAuthorizationStatus("created");
        authorizationResource.setUpdatedTime((long) 163979797);
        return authorizationResource;
    }

    public static AuthorizationResource getAuthorizationResourceForDOMS() {
        AuthorizationResource authorizationResource = new AuthorizationResource();
        authorizationResource.setAuthorizationID("DummyAuthId");
        authorizationResource.setAuthorizationStatus("created");
        authorizationResource.setUpdatedTime((long) 163979797);
        authorizationResource.setAuthorizationType("linked_member");
        return authorizationResource;
    }

    public static ConsentMappingResource getConsentMappingResource(String accountId) {
        ConsentMappingResource consentMappingResource = new ConsentMappingResource();
        consentMappingResource.setMappingID("DummyMappingID");
        consentMappingResource.setAuthorizationID("DummyAuthId");
        consentMappingResource.setMappingStatus("active");
        consentMappingResource.setAccountID(accountId);
        return consentMappingResource;
    }

    public static ConsentMappingResource getConsentMappingResourceForDOMS(String accountId) {
        ConsentMappingResource consentMappingResource = new ConsentMappingResource();
        consentMappingResource.setMappingID("DummyMappingID");
        consentMappingResource.setAuthorizationID("DummyAuthId");
        consentMappingResource.setMappingStatus("active");
        consentMappingResource.setAccountID("accountId");
        return consentMappingResource;
    }

    public static final String VALID_RECEIPT = "{\"accountData\": {\"permissions\": [\"CDRREADACCOUNTSBASIC\", " +
            "\"CDRREADACCOUNTSDETAILS\", \"CDRREADPAYEES\", \"CDRREADTRANSACTION\", \"READCUSTOMERDETAILS\", " +
            "\"READCUSTOMERDETAILSBASIC\", \"CDRREADPAYMENTS\"], \"expirationDateTime\": " +
            "\"" + Instant.now().plusSeconds(86400) + "\"}}";

    public static final String EXPIRED_CONSENT_RECEIPT = "{\"accountData\": {\"permissions\": " +
            "[\"CDRREADACCOUNTSBASIC\", \"CDRREADACCOUNTSDETAILS\", \"CDRREADPAYEES\", \"CDRREADTRANSACTION\", " +
            "\"READCUSTOMERDETAILS\", \"READCUSTOMERDETAILSBASIC\", \"CDRREADPAYMENTS\"], \"expirationDateTime\": " +
            "\"2021-01-05T05:03:35.958Z\"}}";

    public static final String PAYLOAD = "{\n" +
            "  \"data\": {\n" +
            "          \"accountIds\": [\"123456\"],\n" +
            "        },\n" +
            "  \"meta\": {}\n" +
            "}";

    public static final Map<String, String> SAMPLE_CONSENT_ATTRIBUTES_MAP = new HashMap<String, String>() {
        {
            put("sharing_duration_value", "sample_value");
        }
    };
}

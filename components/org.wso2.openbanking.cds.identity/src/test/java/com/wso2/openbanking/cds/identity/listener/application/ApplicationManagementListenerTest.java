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

package org.wso2.openbanking.cds.identity.listener.application;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.oauth.dto.OAuthConsumerAppDTO;

import java.util.HashMap;
import java.util.Map;

/**
 * Test class for Application Management Listener functionality.
 */
public class ApplicationManagementListenerTest {

    private Map<String, Object> spMetaData = new HashMap<>();

    @BeforeClass
    public void beforeClass() {

        spMetaData.put("id_token_encrypted_response_alg", "RSA-OEAP");
        spMetaData.put("id_token_encrypted_response_enc", "A256GCM");
    }

    @Test
    public void testSetOauthPropertiesSuccessScenario() throws OpenBankingException {

        CDSApplicationUpdaterImpl cdsApplicationUpdater = new CDSApplicationUpdaterImpl();
        OAuthConsumerAppDTO oAuthConsumerAppDTO = new OAuthConsumerAppDTO();
        cdsApplicationUpdater.setOauthAppProperties(true, oAuthConsumerAppDTO, spMetaData);
        Assert.assertEquals(oAuthConsumerAppDTO.getIdTokenEncryptionAlgorithm(), "RSA-OEAP");
        Assert.assertEquals(oAuthConsumerAppDTO.getIdTokenEncryptionMethod(), "A256GCM");
    }
}

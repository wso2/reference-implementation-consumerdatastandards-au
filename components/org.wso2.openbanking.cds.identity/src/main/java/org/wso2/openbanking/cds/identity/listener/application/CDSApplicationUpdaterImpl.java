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
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.accelerator.identity.listener.application.ApplicationUpdaterImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.oauth.dto.OAuthConsumerAppDTO;
import org.wso2.openbanking.cds.identity.dcr.constants.CDSValidationConstants;

import java.util.Map;

/**
 * Implementation class extended from ApplicationUpdaterImpl.
 */
public class CDSApplicationUpdaterImpl extends ApplicationUpdaterImpl {

    private static final String CDR_ACCOUNTS = "CDR_ACCOUNTS";
    private static final String AUTHORIZED = "authorized";
    private static final String REVOKED = "revoked";
    private static final String AM_RESTAPI_INVOKER = "AM_RESTAPI_INVOKER";
    private static final String CARBON_SUPER_TENANT_DOMAIN = "@carbon.super";
    private static final ConsentCoreServiceImpl consentCoreService = new ConsentCoreServiceImpl();
    private static final Log log = LogFactory.getLog(CDSApplicationUpdaterImpl.class);

    @Override
    public void setOauthAppProperties(boolean isRegulatoryApp, OAuthConsumerAppDTO oauthApplication,
                                      Map<String, Object> spMetaData) throws OpenBankingException {

        if (spMetaData.get(CDSValidationConstants.ID_TOKEN_ENCRYPTION_RESPONSE_ALG) != null &&
                spMetaData.get(CDSValidationConstants.ID_TOKEN_ENCRYPTION_RESPONSE_ENC) != null) {
            oauthApplication.setIdTokenEncryptionEnabled(true);
            oauthApplication.setIdTokenEncryptionAlgorithm(spMetaData
                    .get(CDSValidationConstants.ID_TOKEN_ENCRYPTION_RESPONSE_ALG).toString());
            oauthApplication.setIdTokenEncryptionMethod(spMetaData
                    .get(CDSValidationConstants.ID_TOKEN_ENCRYPTION_RESPONSE_ENC).toString());
        }

        if (isRegulatoryApp) {
            oauthApplication.setPkceMandatory(true);
        }
    }

    @Override
    public void doPostDeleteApplication(ServiceProvider serviceProvider, String tenantDomain, String userName)
            throws OpenBankingException {

        // Revoke tokens and consents bound to the application.
        if (!serviceProvider.getApplicationName().equals(AM_RESTAPI_INVOKER)) {
            String clientId = null;
            InboundAuthenticationRequestConfig inboundAuthRequestConfigs =
                    serviceProvider.getInboundAuthenticationConfig()
                            .getInboundAuthenticationRequestConfigs()[0];
            if (inboundAuthRequestConfigs != null) {
                clientId = inboundAuthRequestConfigs.getInboundAuthKey();
            }
            if (!StringUtils.isEmpty(clientId)) {


                consentCoreService.revokeExistingApplicableConsents(clientId, null, CDR_ACCOUNTS, AUTHORIZED,
                        REVOKED, true);
                log.debug("Applicable tokens and consents revoked successfully.");
            }
        }
    }

}

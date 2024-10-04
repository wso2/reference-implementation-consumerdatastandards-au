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

package org.wso2.openbanking.cds.identity.utils;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.accelerator.identity.util.HTTPClientUtils;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.openbanking.cds.common.utils.CommonConstants;

import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.Cookie;

/**
 * Util class to provide services identity module.
 */
public class CDSIdentityUtil {

    private static Log log = LogFactory.getLog(CDSIdentityUtil.class);
    private static final String COMMON_AUTH_ID = "commonAuthId";
    private static final String SHARING_DURATION_VALUE = "sharing_duration_value";
    private static final String ZERO_SHARING_DURATION = "0";
    private static volatile Key key;

    /**
     * method to retrieve the commonAuthId from the oauth message context.
     *
     * @param oAuthAuthzReqMessageContext
     * @return commonAuthId
     */
    public static String getCommonAuthId(OAuthAuthzReqMessageContext oAuthAuthzReqMessageContext) {

        Cookie[] cookies = oAuthAuthzReqMessageContext.getAuthorizationReqDTO().getCookie();
        String commonAuthId = StringUtils.EMPTY;
        ArrayList<Cookie> cookieList = new ArrayList<>(Arrays.asList(cookies));
        for (Cookie cookie : cookieList) {
            if (COMMON_AUTH_ID.equals(cookie.getName())) {
                commonAuthId = cookie.getValue();
                break;
            }
        }
        return commonAuthId;
    }

    /**
     * method to get the refresh token validity period.
     *
     * @param consentId consent Id
     * @return validity period for the refresh token
     */
    @Generated(message = "Excluding from code coverage since it requires a service call")
    public static long getRefreshTokenValidityPeriod(String consentId) {

        long sharingDuration = 0;
        if (StringUtils.isNotBlank(consentId)) {
            try {
                String sharingDurationValue = new ConsentCoreServiceImpl().getConsentAttributes(consentId)
                        .getConsentAttributes().get(SHARING_DURATION_VALUE);

                if (!ZERO_SHARING_DURATION.equals(sharingDurationValue)
                        && StringUtils.isNotBlank(sharingDurationValue)) {
                    sharingDuration = Long.parseLong(sharingDurationValue);
                }
            } catch (ConsentManagementException e) {
                log.error("Error while retrieving sharing duration. ", e);
            }
        }
        return sharingDuration;
    }

    /**
     * retrieve the consent id from the scopes.
     *
     * @param scopes array of scopes bound to the token
     * @return consent Id
     */
    public static String getConsentId(String[] scopes) {

        String consentIdClaimName = OpenBankingConfigParser.getInstance().getConfiguration().get(
                IdentityCommonConstants.CONSENT_ID_CLAIM_NAME).toString();

        if (scopes != null && scopes.length > 0) {
            List<String> scopesList = new LinkedList<>(Arrays.asList(scopes));
            for (String scope : scopesList) {
                if (scope.startsWith(consentIdClaimName)) {
                    return scope.split(consentIdClaimName)[1];
                }
            }
        }
        return StringUtils.EMPTY;
    }

    public static String getConsentIdWithCommonAuthId(String commonAuthId) {
        String consentId;
        try {
            consentId = new ConsentCoreServiceImpl()
                    .getConsentIdByConsentAttributeNameAndValue(COMMON_AUTH_ID, commonAuthId).get(0);
        } catch (ConsentManagementException e) {
            log.error("Error occurred while retrieving consent id");
            return StringUtils.EMPTY;
        }
        return consentId;
    }

    /**
     * Method to obtain signing key.
     *
     * @return Key as an Object
     */
    public static Key getJWTSigningKey() throws OpenBankingException {

        if (key == null) {
            synchronized (CDSIdentityUtil.class) {
                if (key == null) {
                    KeyStore keyStore = HTTPClientUtils.loadKeyStore(ServerConfiguration.getInstance()
                                    .getFirstProperty(CommonConstants.KEYSTORE_LOCATION),
                            ServerConfiguration.getInstance()
                                    .getFirstProperty(CommonConstants.KEYSTORE_PASSWORD));
                    try {
                        key = keyStore.getKey(ServerConfiguration.getInstance()
                                        .getFirstProperty(CommonConstants.KEYSTORE_KEY_ALIAS),
                                ServerConfiguration.getInstance()
                                        .getFirstProperty(CommonConstants.KEYSTORE_KEY_PASSWORD)
                                        .toCharArray());
                    } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
                        log.error("Error occurred while retrieving private key from keystore ", e);
                        throw new OpenBankingException("Error occurred while retrieving private key from keystore ", e);
                    }
                }
            }
        }
        return key;
    }
}

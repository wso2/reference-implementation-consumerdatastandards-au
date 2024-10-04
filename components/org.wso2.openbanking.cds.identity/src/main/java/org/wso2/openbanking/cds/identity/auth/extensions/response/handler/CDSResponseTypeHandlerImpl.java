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
package org.wso2.openbanking.cds.identity.auth.extensions.response.handler;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.identity.auth.extensions.response.handler.OBResponseTypeHandler;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.openbanking.cds.identity.utils.CDSIdentityUtil;

import java.util.Arrays;

/**
 * ResponseType Handler implementation for CDS specification.
 */
public class CDSResponseTypeHandlerImpl implements OBResponseTypeHandler {

    private static final Log log = LogFactory.getLog(CDSResponseTypeHandlerImpl.class);

    @SuppressFBWarnings("PZLA_PREFER_ZERO_LENGTH_ARRAYS")
    // Suppressed content - return null
    // Suppression reason - False Positive : Returning of null values are handled accordingly just like the other
    //                                       values returned in this method.
    // Suppressed warning count - 1
    @Override
    public String[] updateApprovedScopes(OAuthAuthzReqMessageContext oAuthAuthzReqMessageContext) {

        if (oAuthAuthzReqMessageContext != null && oAuthAuthzReqMessageContext.getAuthorizationReqDTO() != null) {

            boolean regulatory = false;
            String[] scopes = oAuthAuthzReqMessageContext.getApprovedScope();
            String commonAuthId = CDSIdentityUtil.getCommonAuthId(oAuthAuthzReqMessageContext);
            try {
                if (StringUtils.isNotBlank(oAuthAuthzReqMessageContext.getAuthorizationReqDTO().getConsumerKey())) {
                    regulatory = IdentityCommonUtil.getRegulatoryFromSPMetaData(oAuthAuthzReqMessageContext
                            .getAuthorizationReqDTO().getConsumerKey());
                }
            } catch (OpenBankingException e) {
                log.error("Regulatory property is null", e);
            }
            if (regulatory && StringUtils.isNotBlank(commonAuthId)) {

                String consentId = CDSIdentityUtil.getConsentIdWithCommonAuthId(commonAuthId);
                if (consentId.isEmpty()) {
                    log.error("Consent id retrieved using common auth id is empty");
                    return null;
                }
                // Add consent id to scopes
                String consentIdClaimName = OpenBankingConfigParser.getInstance().getConfiguration().get(
                        IdentityCommonConstants.CONSENT_ID_CLAIM_NAME).toString();
                String consentScope = consentIdClaimName + consentId;
                String[] updatedScopes = (String[]) ArrayUtils.addAll(scopes, new String[]{consentScope});
                if (log.isDebugEnabled()) {
                    log.debug("Updated scopes: " + Arrays.toString(updatedScopes));
                }
                return updatedScopes;
            }
            if (StringUtils.isBlank(commonAuthId)) {
                log.error("Failed to update scopes.");
                return null;
            }
        } else {
            return new String[0];
        }
        return oAuthAuthzReqMessageContext.getApprovedScope();
    }

    @Override
    public long updateRefreshTokenValidityPeriod(OAuthAuthzReqMessageContext oAuthAuthzReqMessageContext) {

        String commonAuthId = CDSIdentityUtil.getCommonAuthId(oAuthAuthzReqMessageContext);
        if (StringUtils.isNotBlank(commonAuthId)) {
            String consentId = CDSIdentityUtil.getConsentIdWithCommonAuthId(commonAuthId);
            if (consentId.isEmpty()) {
                log.error("Consent id retrieved using common auth id is empty");
                return oAuthAuthzReqMessageContext.getRefreshTokenvalidityPeriod();
            }
            long sharingDuration = CDSIdentityUtil.getRefreshTokenValidityPeriod(consentId);
            if (sharingDuration != 0) {
                return sharingDuration;
            }
        } else {
            // In case of an error (in this case due to common auth id being null or empty),
            // we do not set or update a value. We just pass the value already set to the authorization
            // context using the getter method.
            log.error("Failed to get refresh token validity period due to empty common auth id");
            return oAuthAuthzReqMessageContext.getRefreshTokenvalidityPeriod();
        }
        return oAuthAuthzReqMessageContext.getRefreshTokenvalidityPeriod();
    }
}

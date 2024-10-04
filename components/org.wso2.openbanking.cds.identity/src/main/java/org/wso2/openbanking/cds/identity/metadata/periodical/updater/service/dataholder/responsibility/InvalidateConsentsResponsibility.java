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

package org.wso2.openbanking.cds.identity.metadata.periodical.updater.service.dataholder.responsibility;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.openbanking.cds.common.metadata.periodical.updater.utils.SoftwareProductStatusEnum;
import org.wso2.openbanking.cds.identity.metadata.periodical.updater.internal.ServiceHolder;

/**
 * InvalidateConsentsResponsibility.
 * <p>
 * Uses to execute CDS invalidate consents data holder responsibility
 */
public class InvalidateConsentsResponsibility implements DataHolderResponsibility {

    private static final Log LOG = LogFactory.getLog(InvalidateConsentsResponsibility.class);
    private static final String CONSENT_TYPE = "CDR_ACCOUNTS";
    private static final String APPLICABLE_STATUS_TO_REVOKE = "authorized";
    private static final String REVOKED_CONSENT_STATUS = "revoked";

    private final String dataRecipientsStatus;
    private final String softwareProductStatus;
    private final ServiceProvider serviceProvider;


    public InvalidateConsentsResponsibility(String dataRecipientsStatus, String softwareProductStatus,
                                            ServiceProvider serviceProvider) {

        this.dataRecipientsStatus = dataRecipientsStatus;
        this.softwareProductStatus = softwareProductStatus;
        this.serviceProvider = serviceProvider;
    }

    /**
     * Check if the application has cascading status rules suitable to expire consents.
     *
     * @return if validation success return true
     * @see <a href="https://cdr-register.github.io/register/#data-holder-responsibilities">
     * data-holder-responsibilities</a>
     */
    @Override
    public boolean shouldPerform() {

        if (SoftwareProductStatusEnum.REMOVED.toString().equalsIgnoreCase(this.softwareProductStatus)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Can perform invalidate consents responsibility. software product status: " +
                        this.softwareProductStatus + ", data recipient status: " + this.dataRecipientsStatus);
            }
            return true;
        }
        return false;
    }

    @Override
    public void perform() {

        final String userId = String.format("%s@%s", serviceProvider.getOwner().getUserName(),
                serviceProvider.getOwner().getTenantDomain());

        for (InboundAuthenticationRequestConfig config :
                this.serviceProvider.getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs()) {

            try {
                LOG.debug("Revoking consent and relevant tokens. consentId: " + config.getInboundAuthKey());

                ServiceHolder.getInstance().getConsentCoreService()
                        .revokeExistingApplicableConsents(config.getInboundAuthKey(), userId,
                                CONSENT_TYPE, APPLICABLE_STATUS_TO_REVOKE, REVOKED_CONSENT_STATUS, true);

                LOG.debug("Consent of consentId " + config.getInboundAuthKey()
                        + " was expired as DataHolder Responsibility");
            } catch (ConsentManagementException e) {
                LOG.error("Exception occurred while performing invalidate consents responsibility. Caused by, ", e);
            }
        }
    }

    /**
     * Used when inserting responsibility to responsibilities map in DataHolderResponsibilitiesExecutor class.
     *
     * @return responsibilityId with format of applicationName-id-responsibilityType
     */
    @Override
    public String getResponsibilityId() {
        return String.format("%s-%s-%s", this.serviceProvider.getApplicationID(),
                this.serviceProvider.getApplicationName(), "InvalidateConsents");
    }
}

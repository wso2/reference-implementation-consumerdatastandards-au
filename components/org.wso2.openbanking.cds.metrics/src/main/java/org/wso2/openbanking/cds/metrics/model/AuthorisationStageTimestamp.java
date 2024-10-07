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

package org.wso2.openbanking.cds.metrics.model;

import org.wso2.openbanking.cds.common.enums.AuthorisationStageEnum;

/**
 * Model class for AuthorisationStageTimestamp.
 */
public class AuthorisationStageTimestamp {

    private String requestUriKey;
    private long startedTimestamp;
    private long userIdentifiedTimestamp;
    private long userAuthenticatedTimestamp;
    private long accountSelectedTimestamp;
    private long consentApprovedTimestamp;
    private long consentRejectedTimestamp;
    private long tokenExchangeFailedTimestamp;
    private long completedTimestamp;

    public String getRequestUriKey() {
        return requestUriKey;
    }

    public void setRequestUriKey(String requestUriKey) {
        this.requestUriKey = requestUriKey;
    }

    public long getStartedTimestamp() {
        return startedTimestamp;
    }

    public void setStartedTimestamp(long startedTimestamp) {
        this.startedTimestamp = startedTimestamp;
    }

    public long getUserIdentifiedTimestamp() {
        return userIdentifiedTimestamp;
    }

    public void setUserIdentifiedTimestamp(long userIdentifiedTimestamp) {
        this.userIdentifiedTimestamp = userIdentifiedTimestamp;
    }

    public long getUserAuthenticatedTimestamp() {
        return userAuthenticatedTimestamp;
    }

    public void setUserAuthenticatedTimestamp(long userAuthenticatedTimestamp) {
        this.userAuthenticatedTimestamp = userAuthenticatedTimestamp;
    }

    public long getAccountSelectedTimestamp() {
        return accountSelectedTimestamp;
    }

    public void setAccountSelectedTimestamp(long accountSelectedTimestamp) {
        this.accountSelectedTimestamp = accountSelectedTimestamp;
    }

    public long getConsentApprovedTimestamp() {
        return consentApprovedTimestamp;
    }

    public void setConsentApprovedTimestamp(long consentApprovedTimestamp) {
        this.consentApprovedTimestamp = consentApprovedTimestamp;
    }

    public long getConsentRejectedTimestamp() {
        return consentRejectedTimestamp;
    }

    public void setConsentRejectedTimestamp(long consentRejectedTimestamp) {
        this.consentRejectedTimestamp = consentRejectedTimestamp;
    }

    public long getTokenExchangeFailedTimestamp() {
        return tokenExchangeFailedTimestamp;
    }

    public void setTokenExchangeFailedTimestamp(long tokenExchangeFailedTimestamp) {
        this.tokenExchangeFailedTimestamp = tokenExchangeFailedTimestamp;
    }

    public long getCompletedTimestamp() {
        return completedTimestamp;
    }

    public void setCompletedTimestamp(long completedTimestamp) {
        this.completedTimestamp = completedTimestamp;
    }

    public void setTimestampByStage(AuthorisationStageEnum stage, long timestamp) {
        switch (stage) {
            case STARTED:
                if (this.getStartedTimestamp() <= 0 || timestamp < this.getStartedTimestamp()) {
                    this.setStartedTimestamp(timestamp);
                }
                break;
            case USER_IDENTIFIED:
                if (timestamp > this.getUserIdentifiedTimestamp()) {
                    this.setUserIdentifiedTimestamp(timestamp);
                }
                break;
            case USER_AUTHENTICATED:
                if (timestamp > this.getUserAuthenticatedTimestamp()) {
                    this.setUserAuthenticatedTimestamp(timestamp);
                }
                break;
            case ACCOUNT_SELECTED:
                if (timestamp > this.getAccountSelectedTimestamp()) {
                    this.setAccountSelectedTimestamp(timestamp);
                }
                break;
            case CONSENT_APPROVED:
                if (timestamp > this.getConsentApprovedTimestamp()) {
                    this.setConsentApprovedTimestamp(timestamp);
                }
                break;
            case CONSENT_REJECTED:
                if (timestamp > this.getConsentRejectedTimestamp()) {
                    this.setConsentRejectedTimestamp(timestamp);
                }
                break;
            case TOKEN_EXCHANGE_FAILED:
                if (timestamp > this.getTokenExchangeFailedTimestamp()) {
                    this.setTokenExchangeFailedTimestamp(timestamp);
                }
                break;
            case COMPLETED:
                if (timestamp > this.getCompletedTimestamp()) {
                    this.setCompletedTimestamp(timestamp);
                }
                break;
            default:
                break;
        }
    }
}

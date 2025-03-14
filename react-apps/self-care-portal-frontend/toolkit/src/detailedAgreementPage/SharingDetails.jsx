/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

import React from 'react'
import { KeyDatesInfo, AccountsInfo, DataSharedInfo } from '../detailedAgreementPage'
import { SecondaryUserInfo } from 'AppOverride/src/detailedAgreementPage/SecondaryUserInfo.jsx';
import "../css/SharingDetails.css"

export const SharingDetails = ({consent, infoLabels, consentType}) => {

    return(
        <>
            <hr id = "sharingDetailsHr" className = "horizontalLine" />
            <div id = "keyDatesBox" className = "infoBox">
                <KeyDatesInfo consent = {consent} infoLabels = {infoLabels} consentType={consentType}/>
            </div>
            <hr id = "sharingDetailsHr" className = "horizontalLine" />
            <div id = "secondaryUserInfoBox" className = "infoBox">
                <SecondaryUserInfo consent = {consent} infoLabels = {infoLabels}/>
            </div>
            <hr id = "sharingDetailsHr" className = "horizontalLine" />
            <div id = "accountInfoBox" className = "infoBox">
                <AccountsInfo consent = {consent} infoLabels = {infoLabels} consentType={consentType} />
            </div>
            <hr id = "sharingDetailsHr" className = "horizontalLine" />
            <div id = "dataInfoBox" className = "infoBox">
                <DataSharedInfo consent = {consent} infoLabels = {infoLabels} />
            </div>
        </>
    )
}

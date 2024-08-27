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

import React, { useEffect, useState } from 'react';
import { ProfileMain, StatusLabel } from '../../../accelerator/src/detailedAgreementPage';
import { Accreditation } from '../../../toolkit/src/detailedAgreementPage/Accreditation.jsx';
import { dataTypes } from '../specConfigs/common';
import { getExpireTimeFromConsent } from '../../../accelerator/src/services/utils.js';
import '../../../accelerator/src/css/Profile.css';
import '../../../accelerator/src/css/Buttons.css';

export const Profile = ({
  consent,
  infoLabel,
  appicationName,
  logoURL,
  consentType,
  dataRecipientName,
}) => {
  const [expireTime, setExpireTime] = useState(() => {
    return getExpireTimeFromConsent(consent, dataTypes.timestamp);
  });

  useEffect(() => {
    setExpireTime(getExpireTimeFromConsent(consent, dataTypes.timestamp));
  }, [consent]);

  return (
    <>
      <div className="profileBody">
        <StatusLabel infoLabel={infoLabel} expireDate={expireTime} />
        <ProfileMain
          consent={consent}
          infoLabel={infoLabel}
          appicationName={appicationName}
          logoURL={logoURL}
        />
        <hr className="horizontalLine" />
        <div className="infoBox">
          <Accreditation
            infoLabel={infoLabel}
            dataRecipientName={dataRecipientName}
            applicationName={appicationName}
          />
        </div>
        <div className="infoBox">
          <h6>Other important information</h6>
          <p>
            There may be additional important information not shown here. Please check this sharing
            arrangement of {dataRecipientName}’s website/app.
          </p>
          <br />
          <h6>Disclaimer</h6>
          <p>
            You should check with {dataRecipientName} for more information on how they are handling
            your data, and for any other permissions you may have given them. See
            {" "+dataRecipientName}’s CDR policy or their Dashboard for more information
          </p>
        </div>
      </div>
    </>
  );
};

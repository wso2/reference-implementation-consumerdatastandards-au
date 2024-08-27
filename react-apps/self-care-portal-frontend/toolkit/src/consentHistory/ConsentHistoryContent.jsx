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

import React from 'react';
import { ConsentReceipt } from '../../../toolkit/src/consentHistory/ConsentReceipt.jsx';
import { specConfigurations } from '../specConfigs/specConfigurations.js';

/**
 * ConsentHistoryContent manages the fetching and displaying of consent history data.
 *
 * @param {Object} props - The properties passed to the component.
 * @param {boolean} props.isLoading - A flag indicating if the data is currently being loaded.
 * @param {Object} props.error - An error object if there was an error loading the data.
 * @param {Object} props.consentHistoryData - The consent history data.
 *
 * This component does the following:
 * 1. Displays a loading message while the data is being fetched.
 * 2. Displays an error message if there was an error loading the data.
 * 3. Renders the ConsentReceipt component if the data is available.
 * 4. Displays a message if no data is found.
 *
 * @returns {JSX.Element} - A JSX element that includes a message or the ConsentReceipt component.
 */

export const ConsentHistoryContent = ({ isLoading, error, consentHistoryData }) => {
  //Method to check if consent history data is available
  const isConsentDataAvailable = (consentHistoryData?.consentAmendmentHistory?.length) > 0;

  let historyContent;

  //Loading text while fetching consent history data
  if (isLoading) {
    historyContent = specConfigurations.consentHistory.consentHistoryFetchingMessage;
    //Error message if consent history data fetch fails
  } else if (error) {
    historyContent = specConfigurations.consentHistory.consentHistoryFailMessage;
    //Check if consent history data is available
  } else if (isConsentDataAvailable) {
    return <ConsentReceipt consentHistory={consentHistoryData.consentAmendmentHistory} />;
    //If no consent history data found
  } else {
    historyContent = specConfigurations.consentHistory.consentHistoryAbsentMessage;
  }

  return <p className="no-content">{historyContent}</p>;
};

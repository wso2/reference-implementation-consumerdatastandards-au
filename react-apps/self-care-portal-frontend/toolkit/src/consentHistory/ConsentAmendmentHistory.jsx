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
import { ConsentHistoryViewAccordion } from './ConsentHistoryViewAccordion.jsx';
import { specConfigurations } from '../specConfigs/specConfigurations.js';

import '../../../accelerator/src/css/Buttons.css';
import '../../../toolkit/src/css/ConsentHistoryStyles.css';

/**
 * This component displays the history of consent amendments.
 *
 * @param {Object} props - The properties passed to the component.
 * @param {Object} props.consent - The consent data.
 *
 * This component does the following:
 * 1. Displays a title for the consent history section.
 * 2. Renders the ConsentHistoryViewAccordion component, passing the consent data to it.
 *
 * @returns {JSX.Element} - A JSX element that includes a title and the ConsentHistoryViewAccordion component.
 */

export const ConsentAmendmentHistory = ({ consent }) => {
  return (
    <>
      <h5>{specConfigurations.consentHistory.consentHistoryLabel}</h5>
      <ConsentHistoryViewAccordion consent={consent} />
    </>
  );
};


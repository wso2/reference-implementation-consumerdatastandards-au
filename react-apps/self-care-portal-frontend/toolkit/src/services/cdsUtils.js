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
import moment from 'moment'
import { specConfigurations } from '../specConfigs/specConfigurations.js';
import { dataTypes } from '../specConfigs/common.js';

export const getAmendmendedReason = (amendedReason) => {
  const reason = Object.entries(
    specConfigurations.consentHistory.consentAmendmentReasonLabels
  ).find(([key, value]) => key == amendedReason);
  return reason ? reason[1] : amendedReason;
};

export const convertSecondsToDaysHoursMinutes = (seconds) => {
  const days = Math.floor(seconds / (24 * 60 * 60));
  const hours = Math.floor((seconds % (24 * 60 * 60)) / (60 * 60));
  const minutes = Math.floor((seconds % (60 * 60)) / 60);

  let timeString = '';
  if (days > 0) timeString += `${days} Days `;
  if (days === 0 && hours > 0) timeString += `${hours} Hours ${minutes} Minutes `;
  if (days === 0 && hours === 0 && minutes > 0) timeString += `${minutes} Minutes`;

  return timeString.trim();
};

export const getUserId = (user) => {
  return user.email.endsWith('@carbon.super') ? user.email : user.email + '@carbon.super';  
}; 

export const getTimeStamp = (timestamp) => {
  return moment(timestamp * 1000).format(dataTypes.daysHours);
};

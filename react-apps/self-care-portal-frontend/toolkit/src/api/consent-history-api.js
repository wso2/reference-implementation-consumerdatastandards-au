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

import Cookies from 'js-cookie';
import axios from "axios";
import User from '../../../accelerator/src/data/User';
import { CONFIG } from '../../../accelerator/src/config';
import { getUserId } from '../services/cdsUtils.js';

/**
 * Get the consent amendments history of a consent from the API.
 */
export const getConsentHistoryFromAPI = (consentId, user) => {
  const userId = getUserId(user);
  const consentHistoryAdminUrl = `${CONFIG.BACKEND_URL}/admin/consent-amendment-history?cdrArrangementID=${consentId}`;
  const consentHistoryDefaultUrl = `${CONFIG.BACKEND_URL}/admin/consent-amendment-history?cdrArrangementID=${consentId}&userID=${userId}`;
  const consentHistoryUrl = (user.role === "customerCareOfficer") ? consentHistoryAdminUrl : consentHistoryDefaultUrl;

  const requestConfig = {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + Cookies.get(User.CONST.OB_SCP_ACC_TOKEN_P1),
    },
    method: 'GET',
    url: consentHistoryUrl,
  };

  return axios
    .request(requestConfig)
    .then(response => {
      return Promise.resolve(response.data);
    })
    .catch(error => {
      console.error("There's a problem in retrieving your consent history");
      return Promise.reject(error);
    });
};

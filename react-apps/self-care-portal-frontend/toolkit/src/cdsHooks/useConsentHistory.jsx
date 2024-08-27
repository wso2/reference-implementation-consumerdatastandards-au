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
 *
 */
import { useState, useEffect } from 'react';
import { getConsentHistoryFromAPI } from '../api/consent-history-api.js';
import { getValueFromConsent } from '../../../accelerator/src/services/utils.js';
import { keyValues } from '../../../toolkit/src/specConfigs/common.js';

/**
 * useConsentHistory is a custom React hook that fetches and manages consent history data.
 * This hook does the following:
 * 1. Initializes state variables for consentHistoryData, isLoading, and error.
 * 2. Defines a fetchData function that:
 *    a. Checks if consentHistoryData is null. If it is, it sets isLoading to true and makes an API call.
 *    b. The API call fetches consent history data using the consentId from the consent object and the user object.
 *    c. If the API call is successful, it updates consentHistoryData with the returned data.
 *    d. If the API call fails, it updates the error state with the error returned from the API call.
 * 
 * @param {Object} consent - The the current consent.
 * @param {Object} user - The the current user.
 * @param {Boolean} isTriggered - The state of the trigger component which is used to trigger the fetchConsentHistory function.
 * @returns {Object} An object containing the following properties:
 *   - consentHistoryData: Consent History Data.
 *   - isLoading: The indicator while the consent hostory data is fetched.
 *   - error: Any errors that occur during the fetch.-
 * @example
 * const { consentHistoryData, isLoading, error } = useConsentHistory(
    consent,
    user,
    isTriggered
  );
 */
export const useConsentHistory = (consent, user, isTriggered) => {
  const [consentHistoryData, setConsentHistoryData] = useState();
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchConsentHistory = () => {
    if (!consentHistoryData) {
      setIsLoading(true);
      getConsentHistoryFromAPI(getValueFromConsent(keyValues.consentIdKey, consent), user)
        .then((data) => setConsentHistoryData(data))
        .catch((error) => {
          setError(error);
        })
        .finally(() => setIsLoading(false));
    }
  };

  //The useEffect hook is used to call the fetchConsentHistory function when the isTriggered state changes .
  useEffect(() => {
    if (isTriggered) {
      fetchConsentHistory();
    }
  }, [isTriggered]);

  return {
    consentHistoryData,
    isLoading,
    error,
  };
};

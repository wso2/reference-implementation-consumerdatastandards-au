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

import { useContext } from 'react';
import { ConsentContext } from '../../../accelerator/src/context/ConsentContext.js';
import { UserContext } from '../../../accelerator/src/context/UserContext.js';
import { permissionDataLanguageBusiness } from '../specConfigs/permissionDataLanguageBusiness.js';
import { permissionDataLanguageIndividual } from '../specConfigs/permissionDataLanguageIndividual.js';
import { keyValues, keyPermissionScopes } from '../specConfigs/common.js';

/**
 * usePermissionData is a custom React hook that processes and manages permission data.
 *
 * This hook does the following:
 * 1. Retrieves the consent context.
 * 2. Determines the consumer type (business or individual) based on the consent context.
 * 3. Selects the appropriate permission data language based on the consumer type.
 * 4. Extracts the account list, permission array, and sharing duration from the permission data.
 * 5. Maps the permission array to a data clusters array. Each element in the data clusters array corresponds to a matching permission in the permission data language.
 * 
 * @param {Object} permissionData - The permission data object.
 * @returns {Object} - An object containing the account array, sharing duration, data clusters, and permission data language.
 * @example
 * const { accountArray, sharingDuration, dataClusters, permissionDataLanguage } = usePermissionData(permissionData);
 */

export const usePermissionData = (permissionData) => {
  const { allContextConsents } = useContext(ConsentContext);
  const {currentContextUser} = useContext(UserContext);
  const user = currentContextUser.user;
  const consumerType = allContextConsents.consents.data[0].consentAttributes.customerProfileType;

  const permissionDataLanguage =
    consumerType === keyValues.consumerTypeBusiness
      ? permissionDataLanguageBusiness
      : permissionDataLanguageIndividual;

  const accountList = permissionData.permissionData.previousConsentData.userList;
  let matchedUser;
  if (user.role === "customerCareOfficer") {
    // If the user is a customer care officer, the primary member's account list is shown
    matchedUser = accountList.find(u => u.authType === "primary_member");
  } else {
    matchedUser = accountList.find(u => u.userId === user.email);
  }
  const accountArray = matchedUser['accountList'];
  const permissionArray = permissionData.permissionData.previousConsentData.permissions;
  const sharingDuration = permissionData.permissionData.previousConsentData.sharingDuration;

  //Checking whether the permissions contain both basic and detailed permissions, if so, remove basic permissions
  const checkIfDetailed = (permissions) => {
    if (permissions.includes(keyPermissionScopes.bankAccountDetailRead)) {
      permissions = permissions.filter(permission => permission !== keyPermissionScopes.bankAccountBasicRead);
    }

    if (permissions.includes(keyPermissionScopes.commonCustomerDetailsRead)) {
      permissions = permissions.filter(permission => permission !== keyPermissionScopes.commonCustomerBasicRead);
    }
    return permissions;
  };

  const filteredPermissionArray = checkIfDetailed(permissionArray);

  const dataClusters = permissionDataLanguage
  .filter(pdl => filteredPermissionArray.includes(pdl.scope))
  .map(pdl => pdl.dataCluster);

  return { accountArray, sharingDuration, dataClusters, permissionDataLanguage };
};

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

//DataSharedInfo for Default application
import React from 'react';
import { PermissionItem } from '../detailedAgreementPage/PermissionItem.jsx';
import { getValueFromConsent } from '../services/utils.js';
import { permissionBindTypes, keyPermissionScopes } from '../specConfigs/common';
import { permissionDataLanguageBusiness } from '../../../toolkit/src/specConfigs/permissionDataLanguageBusiness.js';
import { permissionDataLanguageIndividual } from '../../../toolkit/src/specConfigs/permissionDataLanguageIndividual.js';
import { profilePermissions } from '../../../toolkit/src/specConfigs/permissionDataLanguageIndividual.js';
import { specConfigurations } from '../specConfigs/specConfigurations.js';

export const DataSharedInfo = ({ consent, infoLabels }) => {
  //Assigning permissionDataLanguage according to the consumer type
  const isBusinessProfile = consent.consentAttributes['customerProfileType'] === 'business-profile';
  const permissionDataLanguage = isBusinessProfile ? permissionDataLanguageBusiness : permissionDataLanguageIndividual;

  //Check if detailed permissions are granted by the ADR, if so, remove basic permissions
  const checkIfDetailed = (permissions) => {
    if (permissions.includes(keyPermissionScopes.bankAccountDetailRead)) {
      permissions = permissions.filter(permission => permission !== keyPermissionScopes.bankAccountBasicRead);
    }

    if (permissions.includes(keyPermissionScopes.commonCustomerDetailsRead)) {
      permissions = permissions.filter(permission => permission !== keyPermissionScopes.commonCustomerBasicRead);
    }

    return permissions;
  };

  const filterProfilePermissions = (permissions) => {
    const updatedProfilePermissions = [];
    for (let i = permissions.length - 1; i >= 0; i--) {
      const permission = permissions[i];
      profilePermissions.forEach(({ name, scopes }) => {
        if (scopes.includes(permission)) {
          permissions.splice(i, 1);
          if (!updatedProfilePermissions.includes(name)) {
            updatedProfilePermissions.push(name);
          }
        }
      });
    }

    updatedProfilePermissions.sort();
    let contactPermission = '';

    updatedProfilePermissions.forEach(permission => {
      if (keyPermissionScopes.name === permission) {
        permissions.push(keyPermissionScopes.name);
      } else {
        contactPermission = `${contactPermission}_${permission}`;
      }
    });

    if (contactPermission !== '') {
      permissions.push(contactPermission.slice(1));
    }

    permissions = checkIfDetailed(permissions);
    return permissions;
  };


  let permissions = [];
  if (specConfigurations.consent.permissionsView.permissionBindType === permissionBindTypes.samePermissionSetForAllAccounts) {
    permissions = getValueFromConsent(specConfigurations.consent.permissionsView.permissionsAttribute, consent);
    permissions = filterProfilePermissions(permissions);

    if (!permissions || permissions.length === 0) {
      permissions = [];
    }
  } else {
    permissions = {};
    const detailedAccountsList = getValueFromConsent('consentMappingResources', consent);
    detailedAccountsList.forEach(({ accountId, permission }) => {
      permissions[accountId] = permissions[accountId] ? [...permissions[accountId], permission] : [permission];
    });
  }

  return (
    <div className="dataSharedBody">
      <h5>{infoLabels.dataSharedLabel}</h5>
      {specConfigurations.consent.permissionsView.permissionBindType === permissionBindTypes.differentPermissionsForEachAccount ?
      //If different permissions are shared with different accounts
        Object.entries(permissions).map(([account, permissionList]) => (
          <div key={account}>
            <h5>Account : {account}</h5>
            <div className="dataClusters">
              {permissionList.map((permission, index) => (
                <PermissionItem
                  permissionScope={permission}
                  permissionDataLanguage={permissionDataLanguage}
                  key={index}
                />
              ))}
            </div>
          </div>
        ))
        :
        //If the same permission set is shared with all accounts
        <div className="dataClusters">
          {permissions.map((permission, index) => (
            <PermissionItem
              permissionScope={permission}
              permissionDataLanguage={permissionDataLanguage}
              key={index}
            />
          ))}
        </div>
      }
    </div>
  );
};

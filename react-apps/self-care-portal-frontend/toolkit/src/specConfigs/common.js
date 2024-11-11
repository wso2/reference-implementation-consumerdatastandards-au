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

export const common = {
  footerContent: 'WSO2 Open Banking Solution | ' + new Date().getFullYear()
};

export const keyDateTypes = {
  date: 'Date',
  dateRange: 'Date Range',
  text: 'Text',
  value: 'Value',
};

export const permissionBindTypes = {
  // Each account is bound to different permissions
  samePermissionSetForAllAccounts: 'SamePermissionSetForAllAccounts',
  // All the accounts in the consent bind to same set of permissions
  differentPermissionsForEachAccount: 'DifferentPermissionsForEachAccount',
};

export const dataOrigins = {
  // To fetch data from consent
  consent: 'CONSENT',
  // To fetch data from application information
  applicationInfo: 'APPLICATION_INFO',
  // For table action button
  action: 'ACTION',
  // For table status
  status: 'STATUS',
};

export const dataTypes = {
  // To indicate the dataType is a ISO 8601 date
  date: 'DATE_ISO_8601',
  // To indicate the dataType is a raw text
  rawData: 'APPLICATION_INFO',
  // To indicate the dataType is a ISO 8601 date
  timestamp: 'DATE_TIMESTAMP',
  // To Display date in 'YYYY-MM-DD' format and time in 'hh:mm A' format
  daysHours: 'YYYY-MM-DD hh:mm:ss A',
};

export const keyValues = {
  //A consentId key to retireve the consentId from the consent object
  consentIdKey: 'consentId',
  //A consentId key to obtain business permission scopes from the consent object
  consumerTypeBusiness: 'business-profile',
};

export const keyPermissionScopes = {
  // Name permission scope
  name: 'NAME',
   // CDS Permission scope to read basic bank account information
  bankAccountBasicRead: 'CDRREADACCOUNTSBASIC',
  // CDS Permission to read detailed bank account information
  bankAccountDetailRead: 'CDRREADACCOUNTSDETAILS',
  // CDS Permission to read basic customer information
  commonCustomerBasicRead: 'READCUSTOMERDETAILSBASIC',
  // CDS Permission to read detailed customer information
  commonCustomerDetailsRead: 'READCUSTOMERDETAILS',
  // Permission scope to read basic bank account information
  bankAccountBasicReadScope: 'bank:accounts.basic:read',
  // Permission to read detailed bank account information
  bankAccountDetailReadScope: 'bank:accounts.detail:read',
  // Permission to read basic customer information
  commonCustomerBasicReadScope: 'common:customer.basic:read',
  // Permission to read detailed customer information
  commonCustomerDetailsReadScope: 'common:customer.detail:read',
};

export const consentTypes = [
  {
    id: 'CDR_ACCOUNTS',
    label: 'Account Information',
    image: require('../../../accelerator/src/images/accounts.png'),
  },
];

export const consentPdfProperties = {
  orientation: 'p',
  measurement: 'mm',
  pageSize: 'a4'
};

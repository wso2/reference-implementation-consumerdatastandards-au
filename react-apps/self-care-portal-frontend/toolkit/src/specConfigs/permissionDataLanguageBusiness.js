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
export const permissionDataLanguageBusiness = [
  // bank:accounts.basic:read
  {
    scope: 'CDRREADACCOUNTSBASIC',
    permissionScope: 'bank:accounts.basic:read',
    dataCluster: 'Account name, type and balance',
    permissions: ['Name of account', 'Type of account', 'Account balance'],
  },
  // bank:accounts.detail:read
  {
    scope: 'CDRREADACCOUNTSDETAILS',
    permissionScope: 'bank:accounts.detail:read',
    dataCluster: 'Account balance and details',
    permissions: [
      'Name of account',
      'Type of account',
      'Account balance',
      'Account number',
      'Interest rates',
      'Fees',
      'Discounts',
      'Account terms',
      'Account mail address',
    ],
  },
  // bank:transactions:read
  {
    scope: 'CDRREADTRANSACTION',
    permissionScope: 'bank:transactions:read',
    dataCluster: 'Transaction details',
    permissions: [
      'Incoming and outgoing transactions',
      'Amounts',
      'Dates',
      'Descriptions of transactions',
      'Who you have sent money to and received money from',
    ],
  },
  // bank:regular_payments:read
  {
    scope: 'CDRREADPAYMENTS',
    permissionScope: 'bank:regular_payments:read',
    dataCluster: 'Direct debits and scheduled payments',
    permissions: ['Direct debits', 'Scheduled payments'],
  },
  // bank:payees:read
  {
    scope: 'CDRREADPAYEES',
    permissionScope: 'bank:payees:read',
    dataCluster: 'Saved payees',
    permissions: ['Names and details of accounts you have saved'],
  },
  // common:customer.basic:read
  {
    scope: 'READCUSTOMERDETAILSBASIC',
    permissionScope: 'common:customer.basic:read',
    dataCluster: 'Organisation profile',
    permissions: [
      'Agent name and role',
      'Organisation name',
      'Organisation numbers (ABN or ACN)',
      'Charity status',
      'Establishment date',
      'Industry',
      'Organisation type',
      'Country of registration',
    ],
  },
  // common:customer.detail:read
  {
    scope: 'READCUSTOMERDETAILS',
    permissionScope: 'common:customer.detail:read',
    dataCluster: 'Organisation profile and contact details',
    permissions: [
      'Agent name and role',
      'Organisation name',
      'Organisation numbers (ABN or ACN),†',
      'Charity status',
      'Establishment date',
      'Industry',
      'Organisation type',
      'Country of registration',
      'Organisation address',
      'Mail address',
      'Phone number',
    ],
  },
  // profile scope and standard claim clusters
  {
    scope: 'NAME',
    permissionScope: 'name',
    dataCluster: 'Name',
    permissions: ['Full name and title(s)'],
  },
  {
    scope: 'EMAIL',
    permissionScope: 'email',
    dataCluster: 'Contact Details',
    permissions: ['Email address'],
  },
  {
    scope: 'MAIL',
    permissionScope: 'mailaddress',
    dataCluster: 'Contact Details',
    permissions: ['Mail address'],
  },
  {
    scope: 'PHONE',
    permissionScope: 'phone',
    dataCluster: 'Contact Details',
    permissions: ['Phone number'],
  },
  {
    scope: 'EMAIL_MAIL',
    permissionScope: 'emailmailaddress',
    dataCluster: 'Contact Details',
    permissions: ['Email address', 'Mail address'],
  },
  {
    scope: 'EMAIL_PHONE',
    permissionScope: 'emailphone',
    dataCluster: 'Contact Details',
    permissions: ['Email address', 'Phone number'],
  },
  {
    scope: 'MAIL_PHONE',
    permissionScope: 'mailaddress phone',
    dataCluster: 'Contact Details',
    permissions: ['Mail address', 'Phone number'],
  },
  {
    scope: 'EMAIL_MAIL_PHONE',
    permissionScope: 'emailmailaddressphone',
    dataCluster: 'Contact Details',
    permissions: ['Email address', 'Mail address', 'Phone number'],
  },
  //3.0.0 clusters
  {
    scope: 'ReadAccountsDetail',
    permissionScope: 'readaccountsdetail',
    dataCluster: 'Account numbers and features',
    permissions: [
      'Account number',
      'Account mail address',
      'Interest rates',
      'Fees',
      'Discounts',
      'Account terms',
    ],
  },

  {
    scope: 'ReadBalances',
    permissionScope: 'readbalances',
    dataCluster: 'Ability to read all balance information',
    permissions: [
      'Account number',
      'Account mail address',
      'Interest rates',
      'Fees',
      'Discounts',
      'Account terms',
    ],
  },

  {
    scope: 'ReadTransactionsDetail',
    permissionScope: 'readtransactionsdetail',
    dataCluster: 'Ability to read transaction data elements which may hold silent party details',
    permissions: [
      'Account number',
      'Account mail address',
      'Interest rates',
      'Fees',
      'Discounts',
      'Account terms',
    ],
  },
];

export const profilePermissions = [
  {
    name: 'NAME',
    scopes: ['PROFILE', 'NAME', 'GIVENNAME', 'FAMILYNAME', 'UPDATEDAT'],
  },
  {
    name: 'EMAIL',
    scopes: ['EMAIL', 'EMAILVERIFIED'],
  },
  {
    name: 'MAIL',
    scopes: ['ADDRESS'],
  },
  {
    name: 'PHONE',
    scopes: ['PHONENUMBER', 'PHONENUMBERVERIFIED'],
  },
];

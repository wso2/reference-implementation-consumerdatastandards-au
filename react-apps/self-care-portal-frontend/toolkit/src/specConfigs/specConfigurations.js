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

import { consentTypes, dataOrigins, dataTypes, keyDateTypes, permissionBindTypes } from './common';

export const specConfigurations = {
  // key wordings for the relevant statuses.
  status: {
    authorised: 'authorized',
    expired: 'Expired',
    revoked: 'revoked',
  },
  consent: {
    // if consent is in `authorised` state, `expirationTimeAttribute` parameter from consent data
    // will provide the expirationTime for UI validations.
    expirationTimeAttribute: 'receipt.Data.expirationDateTime',
    expirationTimeDataType: dataTypes.timestamp,
    // permissionBindTypes status the type of permission binding to the account
    permissionsView: {
      permissionBindType: permissionBindTypes.samePermissionSetForAllAccounts,
      permissionsAttribute: 'receipt.accountData.permissions',
    },
    consentMappingResources: 'consentMappingResources',
  },
  application: {
    logoURLAttribute: 'logo_uri',
    displayNameAttribute: 'client_name',
    failOverDisplayNameAttribute: 'software_id',
    displayDataRecipientNameAttribute: 'org_name',
    failOverDataRecipientNameAttribute: 'software_id',
  },
  consentHistory: {
    consentHistoryLabel: 'Consent Amendments History',
    consentHistoryAbsentMessage: 'No consent history data found',
    consentHistoryFetchingMessage: 'Loading...',
    consentHistoryFailMessage: 'Failed to fetch consent history data, Try again later',
    consentHistoryView: 'View Details',
    consentHistoryModalHeader: 'Consent Amendment at',
    consentHistoryTableHeaders: {
      historyUpdatedTime: 'Amended At',
      reason: 'Reason',
      previousConsent: 'Consent prior to Amendment',
      sharingDuration: 'Sharing Duration',
      amendedPermissions: 'Permissions',
      accounts: 'Accounts',
    },
    consentAmendmentReasonLabels: {
      ConsentAmendmentFlow: 'Consent Amendment',
      ConsentRevocation: 'Consent Revocation',
      ConsentExpiration: 'Consent Expiration',
      JAMAccountWithdrawal: 'Joint Account Withdrawal',
    },
  },
};
export const account_lang = [
  {
    id: 'authorized',
    label: 'Active',
    labelBadgeVariant: 'success',
    isRevocableConsent: true,
    description: 'A list of applications that have active access to your account information',
    tableHeaders: [
      {
        heading: 'Software Product',
        dataOrigin: dataOrigins.applicationInfo,
        dataParameterKey: 'client_name',
        failOverDataParameterKey: '',
        dataType: dataTypes.rawData,
      },
      {
        heading: 'Consented Date',
        dataOrigin: dataOrigins.consent,
        dataParameterKey: 'createdTimestamp',
        failOverDataParameterKey: '',
        dataType: dataTypes.timestamp,
        dateFormat: 'DD MMM YYYY',
      },
      {
        heading: 'Expiry Date',
        dataOrigin: dataOrigins.consent,
        dataParameterKey: 'consentAttributes.ExpirationDateTime',
        failOverDataParameterKey: 'validityPeriod',
        dataType: dataTypes.timestamp,
        dateFormat: 'DD MMM YYYY',
      },
      {
        heading: 'Action',
        dataOrigin: dataOrigins.action,
        dataParameterKey: '',
        failOverDataParameterKey: '',
        dataType: dataTypes.rawData,
      },
    ],
    profile: {
      confirmation: 'Download confirmation of consent',
    },
    keyDatesInfoLabel: 'Key Dates',
    keyDates: [
      {
        title: 'You granted consent on',
        type: keyDateTypes.date,
        dateParameterKey: 'createdTimestamp',
        dateFormat: 'DD MMM YYYY',
      },
      {
        title: 'Your consent will expire on',
        type: keyDateTypes.date,
        dateParameterKey: 'validityPeriod',
        dateFormat: 'DD MMM YYYY',
      },
      {
        title: 'Sharing period',
        type: keyDateTypes.dateRange,
        dateParameterKey: 'createdTimestamp,validityPeriod',
        dateFormat: 'DD MMM YYYY',
      },
      {
        title: 'How often your data will be shared',
        type: keyDateTypes.text,
        dateParameterKey: '',
        dateFormat: '',
        text: 'Ongoing',
      },
    ],
    accountsInfoLabel: 'Accounts',
    dataSharedLabel: 'Data we are sharing',
    accreditation: {
      accreditationLabel: 'Accreditation',
      accreditWebsite: 'is an accredited data recipient. You can check their accreditation at',
      accreditWebsiteLinkText: 'website',
      accreditWebsiteLink: 'https://www.cdr.gov.au/find-a-provider',
      accreditDR: 'Accrediated Data Recipient:',
    },
  },
  {
    id: 'expired',
    label: 'Expired',
    labelBadgeVariant: 'secondary',
    isRevocableConsent: true,
    description: 'A list of applications that have expired access to your account information',
    tableHeaders: [
      {
        heading: 'Software Product',
        dataOrigin: dataOrigins.applicationInfo,
        dataParameterKey: 'client_name',
        failOverDataParameterKey: 'software_id',
        dataType: dataTypes.rawData,
      },
      {
        heading: 'Consented Date',
        dataOrigin: dataOrigins.consent,
        dataParameterKey: 'createdTimestamp',
        failOverDataParameterKey: '',
        dataType: dataTypes.timestamp,
        dateFormat: 'DD MMM YYYY',
      },
      {
        heading: 'Expiry Date',
        dataOrigin: dataOrigins.consent,
        dataParameterKey: 'consentAttributes.ExpirationDateTime',
        failOverDataParameterKey: 'validityPeriod',
        dataType: dataTypes.timestamp,
        dateFormat: 'DD MMM YYYY',
      },
      {
        heading: 'Action',
        dataOrigin: dataOrigins.action,
        dataParameterKey: '',
        failOverDataParameterKey: '',
        dataType: dataTypes.rawData,
      },
    ],
    profile: {
      confirmation: 'Download consent expiry confirmation',
    },
    keyDatesInfoLabel: 'Key Dates',
    keyDates: [
      {
        title: 'When you gave consent',
        type: keyDateTypes.date,
        dateParameterKey: 'createdTimestamp',
        dateFormat: 'DD MMM YYYY',
      },
      {
        title: 'When consent was expired',
        type: keyDateTypes.date,
        dateParameterKey: 'consentAttributes.ExpirationDateTime',
        dateFormat: 'DD MMM YYYY',
      },
    ],
    accountsInfoLabel: 'Accounts',
    dataSharedLabel: 'Data we shared',
    accreditation: {
      accreditationLabel: 'Accreditation',
      accreditWebsite:
        'is an accredited API consumer application. You can check their accrediation on at',
      accreditWebsiteLinkText: 'website',
      accreditWebsiteLink: 'https://www.cdr.gov.au/find-a-provider',
      accreditDR: 'Accredited Data Recipient:',
    },
  },
  {
    id: 'revoked',
    label: 'Withdrawn',
    labelBadgeVariant: 'secondary',
    isRevocableConsent: true,
    description: 'A list of applications of which consent to access your information was withdrawn',
    tableHeaders: [
      {
        heading: 'Software Product',
        dataOrigin: dataOrigins.applicationInfo,
        dataParameterKey: 'client_name',
        failOverDataParameterKey: 'software_id',
        dataType: dataTypes.rawData,
      },
      {
        heading: 'Consented Date',
        dataOrigin: dataOrigins.consent,
        dataParameterKey: 'createdTimestamp',
        failOverDataParameterKey: '',
        dataType: dataTypes.timestamp,
        dateFormat: 'DD MMM YYYY',
      },
      {
        heading: 'Withdrawn Date',
        dataOrigin: dataOrigins.consent,
        dataParameterKey: 'updatedTimestamp',
        failOverDataParameterKey: '',
        dataType: dataTypes.timestamp,
        dateFormat: 'DD MMM YYYY',
      },
      {
        heading: 'Action',
        dataOrigin: dataOrigins.action,
        dataParameterKey: '',
        failOverDataParameterKey: '',
        dataType: dataTypes.rawData,
      },
    ],
    profile: {
      confirmation: 'Download consent withdrawal confirmation',
    },
    keyDatesInfoLabel: 'Key Dates',
    keyDates: [
      {
        title: 'When you gave consent',
        type: keyDateTypes.date,
        dateParameterKey: 'createdTimestamp',
        dateFormat: 'DD MMM YYYY',
      },
      {
        title: 'You cancelled your consent on',
        type: keyDateTypes.date,
        dateParameterKey: 'updatedTimestamp',
        dateFormat: 'DD MMM YYYY',
      },
    ],
    accountsInfoLabel: 'Accounts',
    dataSharedLabel: 'Data we shared',
    accreditation: {
      accreditationLabel: 'Accreditation',
      accreditWebsite: 'is an accredited data recipient. You can check their accrediation on at',
      accreditWebsiteLinkText: 'website',
      accreditWebsiteLink: 'https://www.cdr.gov.au/find-a-provider',
      accreditDR: 'Accredited Data Recipient:',
    },
  },
];

export const lang = {
  [consentTypes[0].id]: account_lang,
};

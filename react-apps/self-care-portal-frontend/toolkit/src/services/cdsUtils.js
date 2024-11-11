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
import jsPDF from "jspdf";
import { specConfigurations } from '../specConfigs/specConfigurations.js';
import { dataTypes, consentPdfProperties } from '../specConfigs/common.js';

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

export function generatePDF(consent, applicationName, consentStatus) {

  const pdf = new jsPDF(consentPdfProperties.orientation, consentPdfProperties.measurement, consentPdfProperties.size);
  pdf.setFontSize(consentPdfProperties.fontSize);
  pdf.rect(10, 10, 190, 275);

  const keyDateTitles = document.getElementsByClassName('keyDateTitle');
  const keyDateValues = document.getElementsByClassName('keyDateValue');
  const permissionCategories = document.getElementsByClassName('clusterLabelText');
  const permissions = document.getElementsByClassName('permissionsUL');
  const accountIDs = document.getElementsByClassName('permittedAccount');

  let contents = [];
  let accounts = [];

  try {

    for (let i = 0; i < permissions.length; i++) {
      let permissionTexts = permissions[i].getElementsByClassName('permissionText');
      let permissionTextsJoined = "";
      for (let j = 0; j < permissionTexts.length; j++) {
        permissionTextsJoined += permissionTexts[j].innerHTML;
        if (j < permissionTexts.length - 1) {
          permissionTextsJoined += ", ";
        }
      }
      contents.push(permissionTextsJoined);
    }

    for (let i = 0; i < accountIDs.length; i++) {
      accounts.push(accountIDs[i].innerHTML);
    }

  } catch (e) {
  }

  let x = 20;
  let y = 20;

  pdf.text(x, y, 'Consent ID : ' + consent.consentId);
  y += 10;
  pdf.text(x, y, 'Status : ' + consentStatus);
  y += 10;
  pdf.text(x, y, 'API Consumer Application : ' + applicationName);
  y += 10;
  for (let i = 0; i < keyDateTitles.length; i++) {
    pdf.text(x, y, keyDateTitles[i].innerHTML + ' ' + keyDateValues[i].innerHTML);
    y += 10;
  }
  if (accounts.length > 0) {
    pdf.text(x, y, 'Accounts : ' + accounts.join(', '));
    y += 10;
  }
  pdf.text(x, y, 'Data we are sharing on : ');
  y += 10;

  const maxWidth = 140; // Maximum width of the text in the PDF
  const lineHeight = 5; // Height between lines

  for (let i = 0; i < contents.length; i++) {
    const categoryText = permissionCategories[i].innerHTML + ' :';
    const contentText = contents[i];

    // Split the category text and content text to fit within the maxWidth
    const categoryLines = pdf.splitTextToSize(categoryText, maxWidth - 10);
    const contentLines = pdf.splitTextToSize(contentText, maxWidth);

    // Add each line of the category text
    categoryLines.forEach((line, index) => {
      pdf.text(30, y + (index * lineHeight), line);
    });

    // Adjust yPosition for the content text
    y += categoryLines.length * lineHeight;

    // Add each line of the content text
    contentLines.forEach((line, index) => {
      pdf.text(40, y + (index * lineHeight), line);
    });

    // Adjust yPosition for the next category
    y += contentLines.length * lineHeight + lineHeight;
  }
  pdf.save("consent_" + consent.consentId + ".pdf");
}

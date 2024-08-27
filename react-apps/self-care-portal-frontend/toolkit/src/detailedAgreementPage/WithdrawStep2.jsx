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

import React, { useContext, useState } from 'react';
import { Link } from 'react-router-dom';
import Container from 'react-bootstrap/Container';
import ProgressBar from 'react-bootstrap/ProgressBar';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {faCheckCircle, faExclamationCircle, faExclamationTriangle,} from "@fortawesome/free-solid-svg-icons";
import { Modal } from 'react-bootstrap';
import { getDisplayName, getValueFromConsent } from '../services';
import { UserContext } from '../context/UserContext';
import { ConsentContext } from '../context/ConsentContext';
import { AppInfoContext } from '../context/AppInfoContext';
import { permissionDataLanguageBusiness } from '../../../toolkit/src/specConfigs/permissionDataLanguageBusiness.js';
import { permissionDataLanguageIndividual } from '../../../toolkit/src/specConfigs/permissionDataLanguageIndividual.js';
import '../css/Buttons.css';
import '../css/DetailedAgreement.css';
import '../css/withdrawal.css';
import { common, withdrawLang, specConfigurations } from '../specConfigs';
import { FourOhFourError } from '../errorPage';
import { PermissionItem } from '../detailedAgreementPage';
import { useToggles } from '../../../toolkit/src/cdsHooks/useToggles.jsx';
import { keyValues, keyPermissionScopes } from '../specConfigs/common.js';
import {revokeConsent} from "../api";

export const WithdrawStep2 = ({ match }) => {
  const { currentContextUser } = useContext(UserContext);
  const { allContextConsents } = useContext(ConsentContext);
  const { contextAppInfo } = useContext(AppInfoContext);

  const [show, setShow] = useState(false);
  const [message, setMessage] = useState('');
  const [withdrawMessageIcon, setWithdrawMessageIcon] = useState({});
  const [withdrawIconId, setWithdrawIconId] = useState('');

  const { handleToggle } = useToggles();

  const consentId = match.params.id;
  const user = currentContextUser.user;
  const appInfo = contextAppInfo.appInfo;
  const consents = allContextConsents.consents;

  const matchedConsent = consents.data.find((consent) => consent.consentId === consentId);

  const clientId = matchedConsent?.clientId;
  const consentStatus = matchedConsent?.currentStatus;
  const consentConsentId = matchedConsent?.consentId;

  const applicationName = getDisplayName(appInfo, clientId);

  const permissionDataLang =
    matchedConsent?.consentAttributes['customerProfileType'] === 'business-profile'
      ? permissionDataLanguageBusiness
      : permissionDataLanguageIndividual;

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

  const consentAccountResponseDataPermissions = checkIfDetailed (getValueFromConsent(
    specConfigurations.consent.permissionsView.permissionsAttribute,
    matchedConsent
  ) || [] );
  
  

  const handleRevokeConsent = () => {
    revokeConsent(clientId, consentId, user)
        .then(response => {
            if (response.status === 204) {
                setMessage(withdrawLang.withdrawModalSuccessMsg + applicationName);
                setWithdrawMessageIcon(faCheckCircle);
                setWithdrawIconId('withdrawSuccess');
            } else {
                setMessage(withdrawLang.withdrawModalFailMsg);
                setWithdrawMessageIcon(faExclamationCircle);
                setWithdrawIconId('withdrawFail');
            }
            setShow(true);
        })
        .catch(error => {
            setMessage(withdrawLang.withdrawModalFailMsg + ': ' + error);
            setWithdrawMessageIcon(faExclamationCircle);
            setWithdrawIconId('withdrawFail');
            setShow(true);
        });
  };

  return (
    <>
      {consentStatus.toLowerCase() === specConfigurations.status.authorised.toLowerCase() ? (
        <>
          <Modal show={show} onHide={handleToggle} backdrop="static" keyboard={false} centered>
            <Modal.Header className="withdrawMsgModalHeader">
              <Modal.Title>
                <FontAwesomeIcon
                  className="withdrawStatusIcon fa-3x"
                  id={withdrawIconId}
                  icon={withdrawMessageIcon}
                />
              </Modal.Title>
            </Modal.Header>

            <Modal.Body className="withdrawMsgModalBody">{message}</Modal.Body>
            <Modal.Footer className="withdrawMsgModalFooter">
              <Link
                to={`/consentmgr/${consentConsentId}`}
                className="comButton"
                variant="secondary"
                onClick={handleToggle}
              >
                {withdrawLang.closeWithdrawMsgModal}
              </Link>
            </Modal.Footer>
          </Modal>

          {consentStatus.toLowerCase() === specConfigurations.status.authorised.toLowerCase() ? (
            <Container fluid className="withdrawContainer">
              <div className="withdrawTitle">
                <FontAwesomeIcon className="withdrawWarnIcon fa-5x" icon={faExclamationTriangle} />

                <h4 className="withdrawalHeading">
                  {withdrawLang.stepHeading}
                  {applicationName}
                </h4>
                <ProgressBar now={100} label="2" />
                <p className="infoHeading">{withdrawLang.infoHeading}</p>
              </div>
              <div className="withdrawInfo">
                <p className="subHeadings">{withdrawLang.collectedData}</p>
                <div className="withdrawalPermissions">
                  {consentAccountResponseDataPermissions.map((permission) => (
                    <PermissionItem
                      permissionScope={permission}
                      permissionDataLanguage={permissionDataLang}
                    />
                  ))}
                </div>
              </div>
              <div className="actionButtons" id="withdrawStep2ActionBtns">
                <div className="actionInfo">
                  <h6 className="subHeadings confirmationHeading">
                    {withdrawLang.confirmationHeading}
                  </h6>
                  <p>{withdrawLang.confirmationPara}</p>
                </div>
                <div className="actionBtnDiv">
                  <Link
                    to={`/consentmgr/${consentConsentId}/withdrawal-step-1`}
                    className="comButton"
                    id="withdrawFlowBackBtn"
                  >
                    {withdrawLang.backBtn}
                  </Link>
                </div>
                <div className="actionBtnDiv">
                  <button onClick={handleRevokeConsent} className="withdrawBtn" id="withdrawBtn2">
                    {withdrawLang.nextBtnStep2}
                  </button>
                </div>
              </div>
            </Container>
          ) : (
            <FourOhFourError />
          )}
        </>
      ) : (
        <FourOhFourError />
      )}
    </>
  );
};

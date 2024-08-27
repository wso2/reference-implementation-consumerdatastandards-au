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

import React from 'react';
import { Accordion, Card, Col, Row } from 'react-bootstrap';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faCaretDown, faCaretUp } from '@fortawesome/free-solid-svg-icons';

import { specConfigurations } from '../specConfigs/specConfigurations.js';
import { useToggles, usePermissionData } from '../cdsHooks';
import { convertSecondsToDaysHoursMinutes } from '../services/cdsUtils.js';

/**
 * PermissionModal displays the details of a consent amendment in a modal.
 *
 * @param {Object} permissionData - The data for the consent amendment.
 *
 * This component does the following:
 * 1. Uses the useToggles and usePermissionData hooks to manage the state of the accordion and to process the permission data.
 * 2. Displays the accounts and sharing duration for the consent amendment.
 * 3. Maps over the dataClusters array to render an Accordion component for each item.
 * 4. Each Accordion component displays the data cluster and a list of permissions.
 *
 * @returns {JSX.Element} - A JSX element that includes a list of accounts, the sharing duration, and a list of Accordion components.
 */

export const PermissionModal = (permissionData) => {
  const { activeItem, handleSelect } = useToggles();
  const { accountArray, sharingDuration, dataClusters, permissionDataLanguage } =
    usePermissionData(permissionData);
    
  return (
    <>
      <div className="row justify-content-between">
        <div className="section">
          <h6 className="modal-title">
            {specConfigurations.consentHistory.consentHistoryTableHeaders.accounts}
          </h6>
          <div className="content">
            {accountArray.map((account, index) => (
              <p key={index}>{account}</p>
            ))}
          </div>
        </div>
        <div className="section pr-5">
          <h6 className="modal-title">
            {specConfigurations.consentHistory.consentHistoryTableHeaders.sharingDuration}
          </h6>
          <p className="content">{convertSecondsToDaysHoursMinutes(sharingDuration)}</p>
        </div>
      </div>
        <div className="section">
          <h6 className="modal-title">
            {specConfigurations.consentHistory.consentHistoryTableHeaders.amendedPermissions}
          </h6>
        </div>
      <div className="content scrollable">
        {dataClusters.map((dataCluster, index) => {
          const matchingPermission = permissionDataLanguage.find(
            (p) => p.dataCluster === dataCluster
          );
          return (
            <Accordion key={index} onSelect={handleSelect}>
              <Card className="clusterContainer">
                <Accordion.Toggle
                  className="clusterRow"
                  as={Card.Header}
                  eventKey={index.toString()}
                >
                  <Col className="clusterLabel">
                    <h6>{dataCluster}</h6>
                  </Col>
                  <Col className="clusterValue">
                    <FontAwesomeIcon
                      className="clusToggle fa-lg"
                      id="clusterToggle"
                      icon={activeItem === index.toString() ? faCaretDown : faCaretUp}
                    />
                  </Col>
                </Accordion.Toggle>
                <Accordion.Collapse eventKey={index.toString()}>
                  <Card.Body>
                    <Row>
                      {matchingPermission
                        ? matchingPermission.permissions.map((permission, index) => (
                            <Col xs={12} sm={6} md={6} lg={4} key={index}>
                              <ul>
                                <li key={index}>{permission}</li>
                              </ul>
                            </Col>
                          ))
                        : null}
                    </Row>
                  </Card.Body>
                </Accordion.Collapse>
              </Card>
            </Accordion>
          );
        })}
      </div>
    </>
  );
};

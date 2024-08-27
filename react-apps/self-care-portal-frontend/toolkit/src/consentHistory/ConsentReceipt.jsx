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
import { Card, Modal } from 'react-bootstrap';
import moment from 'moment';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faExternalLinkAlt } from '@fortawesome/free-solid-svg-icons';

import { PermissionModal } from './PermissionModal.jsx';
import { useToggles } from '../cdsHooks';
import { getAmendmendedReason, getTimeStamp } from '../services/cdsUtils.js';
import { dataTypes } from '../specConfigs/common.js';
import { specConfigurations } from '../../../toolkit/src/specConfigs/specConfigurations.js';

/**
 * ConsentReceipt displays the history of consent amendments in a card view.
 *
 * @param {Object} props - The properties passed to the component.
 * @param {Array} props.consentHistory - The consent history data.
 *
 * This component does the following:
 * 1. Uses the useToggles hook to manage the state of the modal.
 * 2. Maps over the consentHistory array to render a Card component for each item.
 * 3. Each Card component displays the timestamp and the amended reason.
 * 4. When a Card is clicked, it opens a Modal that displays the PermissionModal component with the selected item's data.
 *
 * @returns {JSX.Element} - A JSX element that includes a list of Card components and a Modal.
 */

export const ConsentReceipt = ({ consentHistory }) => {
  const { isOpen, activeItem, handleClose, handleSelect } = useToggles();
  return (
    <>
      <div className="scrollable-plane">
        {consentHistory.map((item, index) => (
          <Card key={index} className="accordion-border" onClick={() => handleSelect(item)}>
            <Card.Body>
              <div className="d-flex justify-content-between">
                <div>
                  <Card.Text className="text-muted">
                    {getTimeStamp(item.amendedTime)}
                  </Card.Text>
                  <Card.Title as="h6">{getAmendmendedReason(item.amendedReason)}</Card.Title>
                </div>
                <FontAwesomeIcon icon={faExternalLinkAlt} rotation={270} className="icon" size='xs'/>
              </div>
            </Card.Body>
          </Card>
        ))}
      </div>
      <Modal show={isOpen} onHide={handleClose} size="lg" dialogClassName="modal-dialog-centered">
        <Modal.Header closeButton>
          <div className="flex-column">
            <Modal.Title as="h3">{specConfigurations.consentHistory.consentHistoryModalHeader}</Modal.Title>
            {activeItem && (
              <p className="text-muted">
                {moment(activeItem.amendedTime * 1000).format(dataTypes.daysHours)}
              </p>
            )}
          </div>
        </Modal.Header>
        <Modal.Body className="modal-padding">
          {activeItem && <PermissionModal permissionData={activeItem} />}
        </Modal.Body>
      </Modal>
    </>
  );
};

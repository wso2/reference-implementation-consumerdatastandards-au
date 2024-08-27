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

import React, { useContext, useState, useEffect } from 'react';
import { Accordion, AccordionCollapse, Card, Col, Spinner } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faCaretDown, faCaretUp } from '@fortawesome/free-solid-svg-icons';

import { UserContext } from '../../../accelerator/src/context/UserContext.js';
import { specConfigurations } from '../specConfigs/specConfigurations.js';
import { useConsentHistory, useToggles } from '../cdsHooks/index.js';
import { ConsentHistoryContent } from './ConsentHistoryContent.jsx';
/**
 * ConsentHistoryViewAccordion is a React component that displays the history of consent amendments in an accordion view.
 *
 * @param {Object} props - The properties passed to the component.
 * @param {Object} props.consent - The consent data.
 *
 * This component does the following:
 * 1. Uses the UserContext to get the current user.
 * 2. Uses the useToggles hook to manage the state of the accordion.
 * 3. Uses the useConsentHistory hook to fetch the consent history data.
 * 4. Renders an Accordion component with a toggle button and a ConsentHistoryContent component.
 *
 * @returns {JSX.Element} - A JSX element that includes an Accordion component.
 */

export const ConsentHistoryViewAccordion = ({ consent }) => {
  const { currentContextUser } = useContext(UserContext);

  const { isOpen, handleToggle } = useToggles();

  const { consentHistoryData, isLoading, error} = useConsentHistory(
    consent,
    currentContextUser.user,
    isOpen
  );
  
  return (
    <>
      <div className="consentHistoryActionBtnDiv">
        <Accordion>
          <Card className="clusterContainer">
            <Accordion.Toggle
              className="clusterRow"
              as={Card.Header}
              eventKey="toggleConsentHistory"
              onClick={handleToggle}
            >
              <Col className="clusterLabel">
                <h6>{specConfigurations.consentHistory.consentHistoryView}</h6>
              </Col>
              <Col className="clusterValue">
                {isLoading ? (
                  <Spinner>
                    <div class="spinner-border spinner-border-sm" role="status">
                      <span class="sr-only">Loading...</span>
                    </div>
                  </Spinner>
                ) : (
                  <FontAwesomeIcon
                    className="clusToggle fa-lg"
                    id="clusterToggle"
                    icon={isOpen ? faCaretUp : faCaretDown}
                  />
                )}
              </Col>
            </Accordion.Toggle>
            
            <AccordionCollapse className="padding" eventKey="toggleConsentHistory">
              <Card.Body className="content-view">
                <ConsentHistoryContent
                  isLoading={isLoading}
                  error={error}
                  consentHistoryData={consentHistoryData}
                />
              </Card.Body>
            </AccordionCollapse>
          </Card>
        </Accordion>
      </div>
    </>
  );
};
